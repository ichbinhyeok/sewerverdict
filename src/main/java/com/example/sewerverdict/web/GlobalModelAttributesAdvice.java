package com.example.sewerverdict.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributesAdvice {

	private final String contactEmail;
	private final String gaMeasurementId;

	public GlobalModelAttributesAdvice(@Value("${app.contact-email}") String contactEmail,
		@Value("${app.ga4.measurement-id:}") String gaMeasurementId) {
		this.contactEmail = contactEmail;
		this.gaMeasurementId = gaMeasurementId;
	}

	@ModelAttribute("contactEmail")
	public String contactEmail() {
		return contactEmail;
	}

	@ModelAttribute("contactMailto")
	public String contactMailto() {
		return StringUtils.hasText(contactEmail) ? "mailto:" + contactEmail : "";
	}

	@ModelAttribute("gaMeasurementId")
	public String gaMeasurementId() {
		return gaMeasurementId;
	}
}
