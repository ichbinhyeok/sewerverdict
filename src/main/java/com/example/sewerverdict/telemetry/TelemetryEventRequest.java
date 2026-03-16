package com.example.sewerverdict.telemetry;

import java.util.LinkedHashMap;
import java.util.Map;

public class TelemetryEventRequest {

	private String eventType;
	private String pageSlug;
	private String label;
	private Map<String, String> metadata = new LinkedHashMap<>();

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getPageSlug() {
		return pageSlug;
	}

	public void setPageSlug(String pageSlug) {
		this.pageSlug = pageSlug;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Map<String, String> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata == null ? new LinkedHashMap<>() : new LinkedHashMap<>(metadata);
	}
}
