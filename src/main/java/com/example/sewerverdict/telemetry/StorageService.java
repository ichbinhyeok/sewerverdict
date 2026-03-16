package com.example.sewerverdict.telemetry;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@Service
public class StorageService {

	private static final Set<String> ALLOWED_EVENT_TYPES = Set.of(
		"estimator_start",
		"estimator_complete",
		"result_primary_cta_click",
		"result_secondary_cta_click",
		"lead_form_view",
		"lead_submit",
		"page_cta_click",
		"summary_copy_click"
	);

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final Path leadsFile;
	private final Path eventsFile;

	public StorageService(@Value("${app.storage.root}") String storageRoot) {
		Path root = Path.of(storageRoot);
		this.leadsFile = root.resolve("leads").resolve("leads.jsonl");
		this.eventsFile = root.resolve("events").resolve("events.jsonl");
	}

	@PostConstruct
	void initialize() {
		try {
			Files.createDirectories(leadsFile.getParent());
			Files.createDirectories(eventsFile.getParent());
			if (Files.notExists(leadsFile)) {
				Files.createFile(leadsFile);
			}
			if (Files.notExists(eventsFile)) {
				Files.createFile(eventsFile);
			}
		}
		catch (IOException exception) {
			throw new UncheckedIOException("Failed to initialize storage directories", exception);
		}
	}

	public boolean isValidLead(LeadForm form) {
		return StringUtils.hasText(form.getServiceNeeded())
			&& StringUtils.hasText(form.getZipOrCity())
			&& StringUtils.hasText(form.getName())
			&& StringUtils.hasText(form.getEmail())
			&& StringUtils.hasText(form.getPhone())
			&& form.isConsentGiven();
	}

	public void storeLead(LeadForm form, String pageSlug, String referrer, Map<String, String> utmValues) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("leadId", UUID.randomUUID().toString());
		payload.put("timestamp", Instant.now().toString());
		payload.put("pageSlug", pageSlug);
		payload.put("serviceNeeded", form.getServiceNeeded());
		payload.put("role", form.getRole());
		payload.put("zipOrCity", form.getZipOrCity());
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
		payload.putAll(utmValues);
		appendJsonLine(leadsFile, payload);
	}

	public void logEvent(String eventType, String pageSlug, String referrer, Map<String, Object> payload) {
		if (!ALLOWED_EVENT_TYPES.contains(eventType)) {
			return;
		}
		Map<String, Object> record = new LinkedHashMap<>();
		record.put("eventId", UUID.randomUUID().toString());
		record.put("timestamp", Instant.now().toString());
		record.put("eventType", eventType);
		record.put("pageSlug", pageSlug);
		record.put("referrer", referrer);
		record.put("payload", payload);
		appendJsonLine(eventsFile, record);
	}

	public Map<String, String> buildUtmMap(String source, String medium, String campaign) {
		Map<String, String> utmValues = new LinkedHashMap<>();
		utmValues.put("utmSource", source);
		utmValues.put("utmMedium", medium);
		utmValues.put("utmCampaign", campaign);
		return utmValues;
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
