package com.example.sewerverdict.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributesAdvice {

	private final String contactEmail;

	public GlobalModelAttributesAdvice(@Value("${app.contact-email}") String contactEmail) {
		this.contactEmail = contactEmail;
	}

	@ModelAttribute("contactEmail")
	public String contactEmail() {
		return contactEmail;
	}

	@ModelAttribute("contactMailto")
	public String contactMailto() {
		return StringUtils.hasText(contactEmail) ? "mailto:" + contactEmail : "";
	}
}
