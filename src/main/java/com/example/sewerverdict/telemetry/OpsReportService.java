package com.example.sewerverdict.telemetry;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class OpsReportService {

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final Path leadsFile;
	private final Path eventsFile;
	private final Path estimatorDraftsFile;

	public OpsReportService(@Value("${app.storage.root}") String storageRoot) {
		Path root = Path.of(storageRoot);
		this.leadsFile = root.resolve("leads").resolve("leads.jsonl");
		this.eventsFile = root.resolve("events").resolve("events.jsonl");
		this.estimatorDraftsFile = root.resolve("estimator-drafts").resolve("estimator-drafts.jsonl");
	}

	public OpsReport buildReport() {
		Instant now = Instant.now();
		Instant cutoff = now.minus(30, ChronoUnit.DAYS);
		List<Map<String, Object>> recentLeads = readJsonLines(leadsFile).stream()
			.filter(record -> isOnOrAfter(record, cutoff))
			.toList();
		List<Map<String, Object>> recentEvents = readJsonLines(eventsFile).stream()
			.filter(record -> isOnOrAfter(record, cutoff))
			.toList();
		List<Map<String, Object>> recentDrafts = readJsonLines(estimatorDraftsFile).stream()
			.filter(record -> isOnOrAfter(record, cutoff))
			.toList();

		long estimatorCompletes30d = recentEvents.stream()
			.filter(record -> "estimator_complete".equals(asString(record.get("eventType"))))
			.count();
		long leadSubmits30d = recentLeads.size();
		long inspectionLeads30d = recentLeads.stream()
			.filter(record -> "inspection-first".equals(asString(record.get("routingBucket"))))
			.count();
		long quoteReadyLeads30d = recentLeads.stream()
			.filter(record -> "quote-ready".equals(asString(record.get("routingBucket"))))
			.count();
		long pageCtaClicks30d = recentEvents.stream()
			.filter(this::isMeasuredPathClick)
			.count();
		long resultPrimaryClicks30d = recentEvents.stream()
			.filter(record -> "result_primary_cta_click".equals(asString(record.get("eventType"))))
			.count();
		List<Map<String, Object>> measuredPathClicks = recentEvents.stream()
			.filter(this::isMeasuredPathClick)
			.toList();

		return new OpsReport(
			now,
			estimatorCompletes30d,
			leadSubmits30d,
			inspectionLeads30d,
			quoteReadyLeads30d,
			pageCtaClicks30d,
			resultPrimaryClicks30d,
			formatLeadRate(leadSubmits30d, estimatorCompletes30d),
			topRows(recentLeads, "referrerPath", 6, "Entry page"),
			topRowsWithFallback(recentLeads, "utmSource", "(direct / unknown)", 6, "Lead source"),
			topRowsWithFallback(merge(recentLeads, recentDrafts), "utmCampaign", "(no campaign)", 6, "Campaign"),
			topRowsWithFallback(recentLeads, "routingBucket", "(unset)", 4, "Lead routing"),
			topRowsWithFallback(measuredPathClicks, "pageSlug", "(unknown page)", 6, "CTA page"),
			topRowsWithFallback(measuredPathClicks, "payload.placement", "(unset placement)", 8, "CTA placement"),
			topRowsWithFallback(measuredPathClicks, "payload.route", "(unset route)", 8, "CTA route"),
			topRowsWithFallback(measuredPathClicks, "payload.destination", "(unset destination)", 8, "CTA destination")
		);
	}

	private List<Map<String, Object>> merge(List<Map<String, Object>> first, List<Map<String, Object>> second) {
		return java.util.stream.Stream.concat(first.stream(), second.stream()).toList();
	}

	private List<OpsReportRow> topRows(List<Map<String, Object>> records, String key, int limit, String note) {
		return topRowsWithFallback(records, key, null, limit, note);
	}

	private List<OpsReportRow> topRowsWithFallback(List<Map<String, Object>> records, String key, String fallback, int limit,
		String note) {
		Map<String, Long> counts = new LinkedHashMap<>();
		for (Map<String, Object> record : records) {
			String value = readValue(record, key);
			if (!StringUtils.hasText(value)) {
				value = fallback;
			}
			if (!StringUtils.hasText(value)) {
				continue;
			}
			counts.merge(value, 1L, Long::sum);
		}
		return counts.entrySet().stream()
			.sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry::getKey))
			.limit(limit)
			.map(entry -> new OpsReportRow(entry.getKey(), entry.getValue(), note))
			.toList();
	}

	private String readValue(Map<String, Object> record, String key) {
		if (!StringUtils.hasText(key)) {
			return null;
		}
		String[] parts = key.split("\\.");
		Object current = record;
		for (String part : parts) {
			if (!(current instanceof Map<?, ?> currentMap)) {
				return null;
			}
			current = currentMap.get(part);
		}
		return asString(current);
	}

	private List<Map<String, Object>> readJsonLines(Path file) {
		if (Files.notExists(file)) {
			return List.of();
		}
		try {
			return Files.readAllLines(file).stream()
				.filter(StringUtils::hasText)
				.map(this::readJsonLine)
				.toList();
		}
		catch (IOException exception) {
			throw new UncheckedIOException("Failed to read ops report input from " + file, exception);
		}
	}

	private Map<String, Object> readJsonLine(String line) {
		try {
			return objectMapper.readValue(line, new TypeReference<Map<String, Object>>() {
			});
		}
		catch (IOException exception) {
			throw new UncheckedIOException("Failed to parse ops report record", exception);
		}
	}

	private boolean isOnOrAfter(Map<String, Object> record, Instant cutoff) {
		String timestamp = asString(record.get("timestamp"));
		if (!StringUtils.hasText(timestamp)) {
			return false;
		}
		try {
			return !Instant.parse(timestamp).isBefore(cutoff);
		}
		catch (Exception exception) {
			return false;
		}
	}

	private boolean isMeasuredPathClick(Map<String, Object> record) {
		String eventType = asString(record.get("eventType"));
		return "page_cta_click".equals(eventType)
			|| "home_path_click".equals(eventType)
			|| "winner_path_click".equals(eventType)
			|| "winner_primary_cta_click".equals(eventType)
			|| "winner_secondary_cta_click".equals(eventType)
			|| "city_hub_starter_click".equals(eventType);
	}

	private String formatLeadRate(long leads, long estimatorCompletes) {
		if (estimatorCompletes <= 0) {
			return "n/a";
		}
		double rate = (double) leads / estimatorCompletes * 100.0;
		return "%.1f%%".formatted(rate);
	}

	private String asString(Object value) {
		return value == null ? null : String.valueOf(value);
	}
}
