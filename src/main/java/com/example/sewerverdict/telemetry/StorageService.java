package com.example.sewerverdict.telemetry;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.sewerverdict.estimator.EstimatorForm;
import com.example.sewerverdict.estimator.EstimatorResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@Service
public class StorageService {

	private static final Set<String> ALLOWED_EVENT_TYPES = Set.of(
		"estimator_start",
		"estimator_step_view",
		"estimator_step_validation_error",
		"estimator_complete",
		"result_primary_cta_click",
		"result_secondary_cta_click",
		"lead_form_view",
		"lead_submit",
		"lead_submit_invalid",
		"page_cta_click",
		"summary_copy_click"
	);

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final Path leadsFile;
	private final Path eventsFile;
	private final Path estimatorDraftsFile;

	public StorageService(@Value("${app.storage.root}") String storageRoot) {
		Path root = Path.of(storageRoot);
		this.leadsFile = root.resolve("leads").resolve("leads.jsonl");
		this.eventsFile = root.resolve("events").resolve("events.jsonl");
		this.estimatorDraftsFile = root.resolve("estimator-drafts").resolve("estimator-drafts.jsonl");
	}

	@PostConstruct
	void initialize() {
		try {
			Files.createDirectories(leadsFile.getParent());
			Files.createDirectories(eventsFile.getParent());
			Files.createDirectories(estimatorDraftsFile.getParent());
			initializeFile(leadsFile);
			initializeFile(eventsFile);
			initializeFile(estimatorDraftsFile);
		}
		catch (IOException exception) {
			throw new UncheckedIOException("Failed to initialize storage directories", exception);
		}
	}

	public boolean isValidLead(LeadForm form) {
		return getLeadValidationErrors(form).isEmpty();
	}

	public Map<String, String> getLeadValidationErrors(LeadForm form) {
		Map<String, String> errors = new LinkedHashMap<>();
		if (!StringUtils.hasText(form.getServiceNeeded())) {
			errors.put("serviceNeeded", "Choose the service path so routing does not stay generic.");
		}
		if (!StringUtils.hasText(form.getZipOrCity())) {
			errors.put("zipOrCity", "Add the ZIP or city so the request anchors to a real market.");
		}
		if (!StringUtils.hasText(form.getName())) {
			errors.put("name", "Add your name so the next contact step is usable.");
		}
		if (!StringUtils.hasText(form.getEmail())) {
			errors.put("email", "Add an email address for the next routing step.");
		}
		if (!StringUtils.hasText(form.getPhone())) {
			errors.put("phone", "Add a phone number so inspection or quote follow-up is possible.");
		}
		if (!form.isConsentGiven()) {
			errors.put("consentGiven", "Consent is required before SewerClarity can pass this request forward.");
		}
		return errors;
	}

	public String storeEstimatorDraft(EstimatorForm form, EstimatorResult result, String pageSlug, String referrer,
		String sessionId) {
		String draftId = UUID.randomUUID().toString();
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("draftId", draftId);
		payload.put("timestamp", Instant.now().toString());
		payload.put("sessionId", sessionId);
		payload.put("pageSlug", pageSlug);
		payload.put("referrer", referrer);
		payload.putAll(buildGeoContext(pageSlug, referrer));
		payload.put("role", form.getRole());
		payload.put("location", form.getLocation());
		payload.put("locationNormalized", normalizeLocation(form.getLocation()));
		payload.put("houseAgeBand", form.getHouseAgeBand());
		payload.put("issueState", form.getIssueState());
		payload.put("defectType", form.getDefectType());
		payload.put("accessType", form.getAccessType());
		payload.put("urgency", form.getUrgency());
		payload.put("routingBucket", result.routingBucket());
		payload.put("evidenceState", result.evidenceState());
		payload.put("riskTier", result.riskTier());
		payload.put("likelyNextStep", result.likelyNextStep());
		payload.put("evidenceSummary", result.evidenceSummary());
		payload.put("estimateMethodSummary", result.estimateMethodSummary());
		payload.put("localContextSummary", result.localContextSummary());
		payload.put("callDrivers", result.callDrivers());
		payload.put("summaryBlock", result.summaryBlock());
		payload.put("primaryServiceNeeded", result.primaryServiceNeeded());
		payload.put("secondaryServiceNeeded", result.secondaryServiceNeeded());
		payload.putAll(buildAttributionMap(
			form.getUtmSource(),
			form.getUtmMedium(),
			form.getUtmCampaign(),
			form.getUtmTerm(),
			form.getUtmContent(),
			form.getGclid(),
			form.getWbraid(),
			form.getGbraid()
		));
		appendJsonLine(estimatorDraftsFile, payload);
		return draftId;
	}

	public Optional<EstimatorDraftSnapshot> getEstimatorDraft(String draftId) {
		if (!StringUtils.hasText(draftId)) {
			return Optional.empty();
		}
		try {
			List<String> lines = Files.readAllLines(estimatorDraftsFile);
			for (int index = lines.size() - 1; index >= 0; index--) {
				String line = lines.get(index);
				if (line == null || line.isBlank()) {
					continue;
				}
				Map<String, Object> payload = objectMapper.readValue(line, new TypeReference<Map<String, Object>>() {
				});
				if (!draftId.equals(payload.get("draftId"))) {
					continue;
				}
				return Optional.of(new EstimatorDraftSnapshot(
					asString(payload.get("draftId")),
					asString(payload.get("location")),
					asString(payload.get("riskTier")),
					asString(payload.get("likelyNextStep")),
					asString(payload.get("routingBucket")),
					asString(payload.get("evidenceState")),
					asString(payload.get("evidenceSummary")),
					asString(payload.get("estimateMethodSummary")),
					asString(payload.get("localContextSummary")),
					asString(payload.get("summaryBlock")),
					asStringList(payload.get("callDrivers"))
				));
			}
			return Optional.empty();
		}
		catch (IOException exception) {
			throw new UncheckedIOException("Failed to read estimator drafts", exception);
		}
	}

	public void storeLead(LeadForm form, String pageSlug, String referrer, String sessionId) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("leadId", UUID.randomUUID().toString());
		payload.put("timestamp", Instant.now().toString());
		payload.put("sessionId", sessionId);
		payload.put("draftId", form.getDraftId());
		payload.put("pageSlug", pageSlug);
		payload.putAll(buildGeoContext(pageSlug, referrer));
		payload.put("serviceNeeded", form.getServiceNeeded());
		payload.put("recommendedServicePath", form.getRecommendedServicePath());
		payload.put("routingBucket", determineLeadRoutingBucket(form));
		payload.put("opsStatus", "new");
		payload.put("role", form.getRole());
		payload.put("zipOrCity", form.getZipOrCity());
		payload.put("locationNormalized", normalizeLocation(form.getZipOrCity()));
		payload.put("houseAgeBand", form.getHouseAgeBand());
		payload.put("issueState", form.getIssueState());
		payload.put("defectType", form.getDefectType());
		payload.put("urgency", form.getUrgency());
		payload.put("name", form.getName());
		payload.put("email", form.getEmail());
		payload.put("phone", form.getPhone());
		payload.put("notes", form.getNotes());
		payload.put("consentGiven", form.isConsentGiven());
		payload.put("referrer", referrer);
		payload.putAll(buildAttributionMap(
			form.getUtmSource(),
			form.getUtmMedium(),
			form.getUtmCampaign(),
			form.getUtmTerm(),
			form.getUtmContent(),
			form.getGclid(),
			form.getWbraid(),
			form.getGbraid()
		));
		appendJsonLine(leadsFile, payload);
	}

	public void logEvent(String eventType, String pageSlug, String referrer, String sessionId, Map<String, Object> payload) {
		if (!ALLOWED_EVENT_TYPES.contains(eventType)) {
			return;
		}
		Map<String, Object> record = new LinkedHashMap<>();
		record.put("eventId", UUID.randomUUID().toString());
		record.put("timestamp", Instant.now().toString());
		record.put("sessionId", sessionId);
		record.put("eventType", eventType);
		record.put("pageSlug", pageSlug);
		record.put("referrer", referrer);
		record.putAll(buildGeoContext(pageSlug, referrer));
		record.put("payload", payload);
		appendJsonLine(eventsFile, record);
	}

	public Map<String, String> buildAttributionMap(String source, String medium, String campaign, String term,
		String content, String gclid, String wbraid, String gbraid) {
		Map<String, String> attribution = new LinkedHashMap<>();
		attribution.put("utmSource", trimToNull(source));
		attribution.put("utmMedium", trimToNull(medium));
		attribution.put("utmCampaign", trimToNull(campaign));
		attribution.put("utmTerm", trimToNull(term));
		attribution.put("utmContent", trimToNull(content));
		attribution.put("gclid", trimToNull(gclid));
		attribution.put("wbraid", trimToNull(wbraid));
		attribution.put("gbraid", trimToNull(gbraid));
		return attribution;
	}

	public String determineLeadRoutingBucket(LeadForm form) {
		if ("inspection".equals(form.getServiceNeeded())
			|| "inspection".equals(form.getRecommendedServicePath())
			|| "no-scope-yet".equals(form.getIssueState())
			|| "symptoms-only".equals(form.getIssueState()) && !isSystemicDefect(form.getDefectType())) {
			return "inspection-first";
		}
		if ("repair".equals(form.getServiceNeeded())
			|| "replacement".equals(form.getServiceNeeded())
			|| "replacement".equals(form.getRecommendedServicePath())
			|| "scope-found-issue".equals(form.getIssueState())
			|| isSystemicDefect(form.getDefectType())) {
			return "quote-ready";
		}
		return "needs-clarification";
	}

	private boolean isSystemicDefect(String defectType) {
		return "orangeburg".equals(defectType) || "collapse".equals(defectType) || "cast-iron".equals(defectType);
	}

	private String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}

	private Map<String, Object> buildGeoContext(String pageSlug, String referrer) {
		Map<String, Object> geoContext = new LinkedHashMap<>();
		String normalizedPagePath = normalizePath(pageSlug);
		String normalizedReferrerPath = normalizePath(referrer);
		geoContext.put("pagePath", normalizedPagePath);
		geoContext.put("pageGeoPage", isGeoPath(normalizedPagePath));
		geoContext.put("pageCitySlug", extractCitySlug(normalizedPagePath));
		geoContext.put("pageGeoTopicSlug", extractGeoTopicSlug(normalizedPagePath));
		geoContext.put("referrerPath", normalizedReferrerPath);
		geoContext.put("sourceGeoPage", isGeoPath(normalizedReferrerPath));
		geoContext.put("sourceCitySlug", extractCitySlug(normalizedReferrerPath));
		geoContext.put("sourceGeoTopicSlug", extractGeoTopicSlug(normalizedReferrerPath));
		return geoContext;
	}

	private String normalizePath(String value) {
		String raw = trimToNull(value);
		if (raw == null) {
			return null;
		}
		String candidate = raw;
		if (raw.startsWith("http://") || raw.startsWith("https://")) {
			try {
				candidate = new URI(raw).getPath();
			}
			catch (URISyntaxException exception) {
				return null;
			}
		}
		if (candidate == null || candidate.isBlank()) {
			return null;
		}
		String normalized = candidate.startsWith("/") ? candidate : "/" + candidate;
		return normalized.endsWith("/") ? normalized : normalized + "/";
	}

	private boolean isGeoPath(String path) {
		return path != null && path.startsWith("/cities/");
	}

	private String extractCitySlug(String path) {
		if (!isGeoPath(path)) {
			return null;
		}
		String[] segments = path.split("/");
		return segments.length > 2 ? segments[2] : null;
	}

	private String extractGeoTopicSlug(String path) {
		if (!isGeoPath(path)) {
			return null;
		}
		String[] segments = path.split("/");
		return segments.length > 3 ? segments[3] : null;
	}

	private String normalizeLocation(String value) {
		String normalized = trimToNull(value);
		return normalized == null ? null : normalized.toLowerCase();
	}

	private String asString(Object value) {
		return value == null ? null : String.valueOf(value);
	}

	private List<String> asStringList(Object value) {
		if (!(value instanceof List<?> items)) {
			return List.of();
		}
		return items.stream()
			.map(this::asString)
			.filter(StringUtils::hasText)
			.toList();
	}

	private void initializeFile(Path file) throws IOException {
		if (Files.notExists(file)) {
			Files.createFile(file);
		}
	}

	private synchronized void appendJsonLine(Path file, Map<String, ?> payload) {
		try {
			String line = objectMapper.writeValueAsString(payload) + System.lineSeparator();
			Files.writeString(file, line, java.nio.file.StandardOpenOption.APPEND);
		}
		catch (IOException exception) {
			throw new UncheckedIOException("Failed to write telemetry data", exception);
		}
	}
}

