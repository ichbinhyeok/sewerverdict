package com.example.sewerverdict.content;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GeoProfileService {

	private static final String GEO_PROFILE_PATH = "data/geo/geo_profiles.json";
	private static final String ZIP_CITY_MARKET_PATH = "data/geo/zip_city_markets.json";
	private static final String RESPONSIBILITY_REGISTRY_PATH = "data/raw/responsibility_registry.csv";
	private static final String CSV_SPLIT_REGEX = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
	private static final Pattern ZIP_PATTERN = Pattern.compile("\\b(\\d{5})(?:-\\d{4})?\\b");

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final SourceRegistryService sourceRegistryService;
	private final Map<String, GeoProfile> profilesByCity;
	private final Map<String, ZipCityMarket> zipMarketsByZip;
	private final Map<String, List<ResponsibilityRule>> rulesByCity;

	public GeoProfileService(SourceRegistryService sourceRegistryService) {
		this.sourceRegistryService = sourceRegistryService;
		this.profilesByCity = loadProfiles();
		this.zipMarketsByZip = loadZipMarkets();
		this.rulesByCity = loadResponsibilityRules();
	}

	public GeoProfile getProfileForPage(SitePage page) {
		if (page == null || !page.isGeoPage()) {
			return null;
		}
		return profilesByCity.get(page.getGeoCitySlug());
	}

	public GeoProfile getProfileByCitySlug(String citySlug) {
		if (citySlug == null) {
			return null;
		}
		return profilesByCity.get(citySlug);
	}

	public GeoProfile findProfileForLocation(String location) {
		GeoLocationMatch match = resolveLocationMatch(location);
		return match == null ? null : match.profile();
	}

	public GeoLocationMatch resolveLocationMatch(String location) {
		if (!StringUtils.hasText(location)) {
			return null;
		}
		String normalized = normalizeLocation(location);
		GeoProfile direct = profilesByCity.get(normalized);
		if (direct != null) {
			return new GeoLocationMatch(direct, direct.getCityName() + ", " + direct.getStateCode(), false, null, "city",
				"City-name matching uses the stored local profile directly.", false);
		}
		GeoProfile profile = profilesByCity.values().stream()
			.filter(candidate -> matchesLocation(normalized, candidate))
			.findFirst()
			.orElse(null);
		if (profile != null) {
			return new GeoLocationMatch(profile, profile.getCityName() + ", " + profile.getStateCode(), false, null, "city",
				"City-name matching uses the stored local profile directly.", false);
		}
		String zipCode = extractZip(location);
		if (zipCode == null) {
			return null;
		}
		ZipCityMarket market = zipMarketsByZip.get(zipCode);
		if (market == null) {
			return null;
		}
		GeoProfile zipProfile = profilesByCity.get(market.getCitySlug());
		if (zipProfile == null) {
			return null;
		}
		return new GeoLocationMatch(zipProfile, zipProfile.getCityName() + ", " + zipProfile.getStateCode(), true, zipCode,
			market.getMatchScope(), market.getMatchCaution(), true);
	}

	public List<SourceReference> getProfileSources(SitePage page) {
		GeoProfile profile = getProfileForPage(page);
		if (profile == null || profile.getSourceIds().isEmpty()) {
			return List.of();
		}
		return profile.getSourceIds().stream()
			.map(sourceRegistryService::getSourceById)
			.filter(Objects::nonNull)
			.toList();
	}

	public List<ResponsibilityRuleView> getResponsibilityRuleViews(SitePage page) {
		if (page == null || !page.isGeoPage()) {
			return List.of();
		}
		return getResponsibilityRuleViews(page.getGeoCitySlug());
	}

	public List<ResponsibilityRuleView> getResponsibilityRuleViews(String citySlug) {
		if (citySlug == null) {
			return List.of();
		}
		return rulesByCity.getOrDefault(citySlug, List.of()).stream()
			.map(rule -> new ResponsibilityRuleView(
				rule.cityLabel(),
				rule.ownerScope(),
				rule.publicScope(),
				rule.programNote(),
				sourceRegistryService.getSourceById(rule.sourceId())
			))
			.toList();
	}

	public List<SourceReference> getProfileSources(String citySlug) {
		GeoProfile profile = getProfileByCitySlug(citySlug);
		if (profile == null || profile.getSourceIds().isEmpty()) {
			return List.of();
		}
		return profile.getSourceIds().stream()
			.map(sourceRegistryService::getSourceById)
			.filter(Objects::nonNull)
			.toList();
	}

	public List<SitePage> getGeoCompanionPages(SitePage page, List<SitePage> allPages, int limit) {
		if (page == null || page.isGeoPage()) {
			return List.of();
		}
		String targetSlug = page.getSlug();
		return allPages.stream()
			.filter(SitePage::isGeoPage)
			.filter(candidate -> candidate.getRelatedSlugs().contains(targetSlug))
			.filter(SitePage::isGeoLocalSignalPage)
			.sorted(Comparator
				.comparingInt((SitePage candidate) -> priorityRank(candidate.getGeoCitySlug()))
				.thenComparing(SitePage::getTitle))
			.limit(limit)
			.toList();
	}

	public List<CityHubEntry> getCityHubEntries(List<SitePage> allPages) {
		return profilesByCity.values().stream()
			.sorted(Comparator
				.comparingInt((GeoProfile profile) -> priorityRank(profile.getCitySlug()))
				.thenComparing(GeoProfile::getCityName))
			.map(profile -> {
				List<SitePage> pagesForCity = getPagesForCity(profile.getCitySlug(), allPages);
				return new CityHubEntry(profile, pagesForCity, getStarterPages(pagesForCity));
			})
			.filter(entry -> !entry.pages().isEmpty())
			.toList();
	}

	public CityHubEntry getCityHubEntry(String citySlug, List<SitePage> allPages) {
		GeoProfile profile = getProfileByCitySlug(citySlug);
		if (profile == null) {
			return null;
		}
		List<SitePage> pagesForCity = getPagesForCity(citySlug, allPages);
		if (pagesForCity.isEmpty()) {
			return null;
		}
		return new CityHubEntry(profile, pagesForCity, getStarterPages(pagesForCity));
	}

	public List<CityHubEntry> getCityHubEntriesByTier(List<SitePage> allPages, String priorityTier) {
		return getCityHubEntries(allPages).stream()
			.filter(entry -> priorityTier.equalsIgnoreCase(entry.profile().getPriorityTier()))
			.toList();
	}

	public List<SitePage> getPagesForCity(String citySlug, List<SitePage> allPages) {
		return allPages.stream()
			.filter(SitePage::isGeoPage)
			.filter(page -> citySlug.equals(page.getGeoCitySlug()))
			.sorted(Comparator
				.comparingInt(this::familyRank)
				.thenComparingInt(this::starterRank)
				.thenComparing(SitePage::getTitle))
			.toList();
	}

	private List<SitePage> getStarterPages(List<SitePage> pagesForCity) {
		List<SitePage> acquisitionStarters = pagesForCity.stream()
			.filter(this::isAcquisitionStarterPage)
			.sorted(Comparator
				.comparingInt(this::starterRank)
				.thenComparing(SitePage::getTitle))
			.limit(3)
			.toList();
		if (!acquisitionStarters.isEmpty()) {
			return acquisitionStarters;
		}
		return pagesForCity.stream()
			.sorted(Comparator
				.comparingInt(this::starterRank)
				.thenComparing(SitePage::getTitle))
			.limit(3)
			.toList();
	}

	private int priorityRank(String citySlug) {
		GeoProfile profile = profilesByCity.get(citySlug);
		if (profile == null || profile.getPriorityTier() == null) {
			return 9;
		}
		return switch (profile.getPriorityTier().toLowerCase()) {
			case "tier-1" -> 0;
			case "tier-2" -> 1;
			default -> 9;
		};
	}

	private int familyRank(SitePage page) {
		if (page == null) {
			return 9;
		}
		if (page.isTransferPage() || page.isBuyerPage()) {
			return 0;
		}
		if (page.isDefectPage()) {
			return 1;
		}
		if (page.isCostPage()) {
			return 2;
		}
		if (page.isCompliancePage() || page.isCoveragePage()) {
			return 3;
		}
		return 9;
	}

	private int starterRank(SitePage page) {
		String slug = page.getSlug() == null ? "" : page.getSlug().toLowerCase();
		if (slug.contains("before-buying-house")) {
			return 0;
		}
		if (slug.contains("red-flags") || slug.contains("scope-report") || slug.contains("backup-risk")
			|| slug.contains("signs") || slug.contains("meaning") || slug.contains("what-to-do")) {
			return 1;
		}
		if (slug.contains("repair-vs-replacement")) {
			return 2;
		}
		if (slug.contains("old-house") || slug.contains("before-1970")
			|| slug.contains("cast-iron")) {
			return 3;
		}
		if (slug.contains("replacement-cost")) {
			return 4;
		}
		if (slug.contains("point-of-sale") || slug.contains("certificate") || slug.contains("required-inspection")
			|| slug.contains("compliance") || slug.contains("homeowner-vs-city")) {
			return 5;
		}
		if (slug.contains("scope-worth-it") || slug.contains("scope-inspection")) {
			return 6;
		}
		if (slug.contains("negotiation-with-seller")) {
			return 7;
		}
		if (slug.contains("buyer-or-seller") || slug.contains("wet-weather")) {
			return 8;
		}
		if (page.isBuyerPage()) {
			return 9;
		}
		if (page.isCoveragePage()) {
			return 10;
		}
		if (page.isDefectPage()) {
			return 11;
		}
		if (page.isCostPage()) {
			return 12;
		}
		return 13;
	}

	private boolean isAcquisitionStarterPage(SitePage page) {
		if (page == null || page.isCompliancePage() || page.isCoveragePage()) {
			return false;
		}
		String slug = page.getSlug() == null ? "" : page.getSlug().toLowerCase();
		return !slug.contains("negotiation-with-seller")
			&& !slug.contains("buyer-or-seller");
	}

	private Map<String, GeoProfile> loadProfiles() {
		Resource resource = new ClassPathResource(GEO_PROFILE_PATH);
		try (InputStream inputStream = resource.getInputStream()) {
			List<GeoProfile> loadedProfiles = objectMapper.readValue(inputStream, new TypeReference<List<GeoProfile>>() {
			});
			Map<String, GeoProfile> loaded = new LinkedHashMap<>();
			loadedProfiles.forEach(profile -> loaded.put(profile.getCitySlug(), profile));
			return loaded;
		}
		catch (IOException exception) {
			throw new UncheckedIOException("Failed to load geo profiles from " + GEO_PROFILE_PATH, exception);
		}
	}

	private Map<String, ZipCityMarket> loadZipMarkets() {
		Resource resource = new ClassPathResource(ZIP_CITY_MARKET_PATH);
		try (InputStream inputStream = resource.getInputStream()) {
			List<ZipCityMarket> loadedMarkets = objectMapper.readValue(inputStream,
				new TypeReference<List<ZipCityMarket>>() {
				});
			Map<String, ZipCityMarket> loaded = new LinkedHashMap<>();
			loadedMarkets.forEach(market -> market.getZipCodes().forEach(zipCode -> loaded.put(zipCode, market)));
			return loaded;
		}
		catch (IOException exception) {
			throw new UncheckedIOException("Failed to load ZIP city markets from " + ZIP_CITY_MARKET_PATH, exception);
		}
	}

	private boolean matchesLocation(String normalizedLocation, GeoProfile profile) {
		String cityName = normalizeLocation(profile.getCityName());
		String citySlug = normalizeLocation(profile.getCitySlug());
		String cityWithState = normalizeLocation(profile.getCityName() + ", " + profile.getStateCode());
		String cityWithStateSpaced = normalizeLocation(profile.getCityName() + " " + profile.getStateCode());
		return normalizedLocation.equals(cityName)
			|| normalizedLocation.equals(citySlug)
			|| normalizedLocation.equals(cityWithState)
			|| normalizedLocation.equals(cityWithStateSpaced)
			|| normalizedLocation.contains(cityName)
			|| normalizedLocation.contains(citySlug.replace("-", " "));
	}

	private String normalizeLocation(String value) {
		if (!StringUtils.hasText(value)) {
			return "";
		}
		return value.trim().toLowerCase();
	}

	private String extractZip(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		Matcher matcher = ZIP_PATTERN.matcher(value.trim());
		if (!matcher.find()) {
			return null;
		}
		return matcher.group(1);
	}

	private Map<String, List<ResponsibilityRule>> loadResponsibilityRules() {
		Resource resource = new ClassPathResource(RESPONSIBILITY_REGISTRY_PATH);
		Map<String, List<ResponsibilityRule>> loaded = new LinkedHashMap<>();
		try (BufferedReader reader = new BufferedReader(
			new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
			String header = reader.readLine();
			if (header == null) {
				return loaded;
			}

			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isBlank()) {
					continue;
				}
				String[] cells = Arrays.stream(line.split(CSV_SPLIT_REGEX, -1))
					.map(this::cleanCsvValue)
					.toArray(String[]::new);
				if (cells.length < 6) {
					continue;
				}
				ResponsibilityRule rule = new ResponsibilityRule(
					cells[0],
					cells[1],
					cells[2],
					cells[3],
					cells[4],
					cells[5]
				);
				loaded.computeIfAbsent(rule.citySlug(), key -> new ArrayList<>()).add(rule);
			}
			return loaded;
		}
		catch (IOException exception) {
			throw new UncheckedIOException("Failed to load responsibility registry from " + RESPONSIBILITY_REGISTRY_PATH,
				exception);
		}
	}

	private String cleanCsvValue(String value) {
		String cleaned = value == null ? "" : value.trim();
		if (cleaned.startsWith("\"") && cleaned.endsWith("\"") && cleaned.length() >= 2) {
			cleaned = cleaned.substring(1, cleaned.length() - 1);
		}
		return cleaned.replace("\"\"", "\"");
	}
}
