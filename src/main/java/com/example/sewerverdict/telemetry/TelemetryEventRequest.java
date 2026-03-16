package com.example.sewerverdict.telemetry;

public class TelemetryEventRequest {

	private String eventType;
	private String pageSlug;
	private String label;

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
}
