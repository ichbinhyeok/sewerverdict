package com.example.sewerverdict.web;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.sewerverdict.telemetry.StorageService;
import com.example.sewerverdict.telemetry.TelemetryEventRequest;

@Controller
public class TelemetryController {

	private final StorageService storageService;

	public TelemetryController(StorageService storageService) {
		this.storageService = storageService;
	}

	@PostMapping("/api/events")
	public ResponseEntity<Void> logEvent(@RequestBody TelemetryEventRequest eventRequest, HttpServletRequest request) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("label", eventRequest.getLabel());
		payload.putAll(eventRequest.getMetadata());
		storageService.logEvent(
			eventRequest.getEventType(),
			eventRequest.getPageSlug(),
			request.getHeader("Referer"),
			request.getSession(true).getId(),
			payload
		);
		return ResponseEntity.noContent().build();
	}
}
