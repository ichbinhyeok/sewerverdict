package com.example.sewerverdict.content;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CensusMunicipalityResolver implements MunicipalityResolver {

	private final GeoProfileService geoProfileService;
	private final ObjectMapper objectMapper;
	private final HttpClient httpClient;
	private final boolean enabled;
	private final String baseUrl;
	private final String benchmark;
	private final String vintage;

	@Autowired
	public CensusMunicipalityResolver(
		GeoProfileService geoProfileService,
		@Value("${app.census.geocoder.enabled:true}") boolean enabled,
		@Value("${app.census.geocoder.base-url:https://geocoding.geo.census.gov/geocoder}") String baseUrl,
		@Value("${app.census.geocoder.benchmark:Public_AR_Current}") String benchmark,
		@Value("${app.census.geocoder.vintage:Current_Current}") String vintage
	) {
		this(geoProfileService, enabled, baseUrl, benchmark, vintage, new ObjectMapper(),
			HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build());
	}

	CensusMunicipalityResolver(GeoProfileService geoProfileService, boolean enabled, String baseUrl, String benchmark,
		String vintage, ObjectMapper objectMapper, HttpClient httpClient) {
		this.geoProfileService = geoProfileService;
		this.enabled = enabled;
		this.baseUrl = baseUrl;
		this.benchmark = benchmark;
		this.vintage = vintage;
		this.objectMapper = objectMapper;
		this.httpClient = httpClient;
	}

	@Override
	public Optional<MunicipalityResolution> resolve(String streetAddress, String location) {
		String normalizedStreet = trimToNull(streetAddress);
		String normalizedLocation = trimToNull(location);
		if (!enabled || normalizedStreet == null || normalizedLocation == null) {
			return Optional.empty();
		}

		try {
			HttpRequest request = HttpRequest.newBuilder(buildRequestUri(normalizedStreet, normalizedLocation))
				.timeout(Duration.ofSeconds(4))
				.GET()
				.build();
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() < 200 || response.statusCode() >= 300) {
				return Optional.empty();
			}
			return parseResolution(response.body());
		}
		catch (IOException exception) {
			return Optional.empty();
		}
		catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			return Optional.empty();
		}
	}

	private URI buildRequestUri(String streetAddress, String location) {
		String endpoint = trimTrailingSlash(baseUrl)
			+ (looksLikeZipOnly(location) ? "/geographies/address" : "/geographies/onelineaddress");
		Map<String, String> query = new LinkedHashMap<>();
		if (looksLikeZipOnly(location)) {
			query.put("street", streetAddress);
			query.put("zip", location);
		}
		else {
			query.put("address", streetAddress + ", " + location);
		}
		query.put("benchmark", benchmark);
		query.put("vintage", vintage);
		query.put("format", "json");
		return URI.create(endpoint + "?" + encodeQuery(query));
	}

	private Optional<MunicipalityResolution> parseResolution(String body) throws IOException {
		JsonNode root = objectMapper.readTree(body);
		JsonNode addressMatches = root.path("result").path("addressMatches");
		if (!addressMatches.isArray() || addressMatches.isEmpty()) {
			return Optional.empty();
		}
		JsonNode match = addressMatches.get(0);
		JsonNode geographies = match.path("geographies");
		String stateCode = firstText(geographies.path("States"), "STUSAB");
		if (!StringUtils.hasText(stateCode)) {
			stateCode = trimToNull(match.path("addressComponents").path("state").asText(null));
		}
		CensusMunicipality municipality = firstMunicipality(geographies);
		if (municipality == null || !StringUtils.hasText(stateCode)) {
			return Optional.empty();
		}
		GeoLocationMatch locationMatch = geoProfileService.resolveLocationMatch(municipality.name() + ", " + stateCode);
		GeoProfile profile = locationMatch == null || locationMatch.zipBased() ? null : locationMatch.profile();
		return Optional.of(new MunicipalityResolution(
			trimToNull(match.path("matchedAddress").asText(null)),
			municipality.name(),
			stateCode,
			municipality.geographyType(),
			profile
		));
	}

	private CensusMunicipality firstMunicipality(JsonNode geographies) {
		String place = firstText(geographies.path("Incorporated Places"), "BASENAME");
		if (StringUtils.hasText(place)) {
			return new CensusMunicipality(place, "incorporated-place");
		}
		String subdivision = firstText(geographies.path("County Subdivisions"), "BASENAME");
		if (StringUtils.hasText(subdivision)) {
			return new CensusMunicipality(subdivision, "county-subdivision");
		}
		return null;
	}

	private String firstText(JsonNode arrayNode, String fieldName) {
		if (!arrayNode.isArray() || arrayNode.isEmpty()) {
			return null;
		}
		return trimToNull(arrayNode.get(0).path(fieldName).asText(null));
	}

	private String encodeQuery(Map<String, String> query) {
		return query.entrySet().stream()
			.map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
			.reduce((left, right) -> left + "&" + right)
			.orElse("");
	}

	private String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	private String trimTrailingSlash(String value) {
		String normalized = trimToNull(value);
		if (normalized == null) {
			return "";
		}
		return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
	}

	private String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}

	private boolean looksLikeZipOnly(String value) {
		return value != null && value.matches("^\\d{5}(?:-\\d{4})?$");
	}

	private record CensusMunicipality(String name, String geographyType) {
	}
}
