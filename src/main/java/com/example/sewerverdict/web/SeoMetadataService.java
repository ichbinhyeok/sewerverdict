package com.example.sewerverdict.web;

import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;

import com.example.sewerverdict.content.PageFaq;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SeoMetadataService {

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final String contactEmail;
	private final String baseUrl;

	public SeoMetadataService(@Value("${app.contact-email}") String contactEmail,
		@Value("${app.base-url}") String baseUrl) {
		this.contactEmail = contactEmail;
		this.baseUrl = trimTrailingSlash(baseUrl);
	}

	public void apply(Model model, HttpServletRequest request, String title, String description, String ogType,
		List<SiteController.Breadcrumb> breadcrumbs, List<PageFaq> faq, boolean includeOrganization) {
		apply(model, request, title, description, ogType, breadcrumbs, faq, includeOrganization, null);
	}

	public void apply(Model model, HttpServletRequest request, String title, String description, String ogType,
		List<SiteController.Breadcrumb> breadcrumbs, List<PageFaq> faq, boolean includeOrganization, String articleDate) {
		String canonicalUrl = buildCanonicalUrl(request);
		String defaultImageUrl = extractOrigin(canonicalUrl) + "/og-default.svg";

		model.addAttribute("canonicalUrl", canonicalUrl);
		model.addAttribute("ogType", ogType);
		model.addAttribute("ogUrl", canonicalUrl);
		model.addAttribute("ogImage", defaultImageUrl);

		List<String> schemaScripts = new ArrayList<>();
		if (includeOrganization) {
			schemaScripts.add(writeJsonLd(buildOrganizationSchema(canonicalUrl)));
		}
		schemaScripts.add(writeJsonLd(buildWebPageSchema(title, description, canonicalUrl)));
		if ("article".equalsIgnoreCase(ogType)) {
			schemaScripts.add(writeJsonLd(buildArticleSchema(title, description, canonicalUrl, articleDate, defaultImageUrl)));
		}
		if (breadcrumbs != null && !breadcrumbs.isEmpty()) {
			schemaScripts.add(writeJsonLd(buildBreadcrumbSchema(canonicalUrl, breadcrumbs)));
		}
		model.addAttribute("schemaScripts", schemaScripts);
	}

	public void appendWebApplicationSchema(Model model, HttpServletRequest request, String name, String description) {
		appendSchema(model, buildWebApplicationSchema(name, description, buildCanonicalUrl(request)));
	}

	private Map<String, Object> buildOrganizationSchema(String canonicalUrl) {
		String origin = extractOrigin(canonicalUrl);
		Map<String, Object> schema = new LinkedHashMap<>();
		schema.put("@context", "https://schema.org");
		schema.put("@type", "Organization");
		schema.put("name", "SewerClarity");
		schema.put("url", origin);
		schema.put("description", "Buyer-first sewer scope risk and next-step guidance for buyers, sellers, and owners.");
		if (StringUtils.hasText(contactEmail)) {
			schema.put("email", "mailto:" + contactEmail);
			schema.put("contactPoint", List.of(Map.of(
				"@type", "ContactPoint",
				"contactType", "customer support",
				"email", contactEmail
			)));
		}
		return schema;
	}

	private Map<String, Object> buildWebPageSchema(String title, String description, String canonicalUrl) {
		Map<String, Object> schema = new LinkedHashMap<>();
		schema.put("@context", "https://schema.org");
		schema.put("@type", "WebPage");
		schema.put("name", title);
		schema.put("description", description);
		schema.put("url", canonicalUrl);
		return schema;
	}

	private Map<String, Object> buildBreadcrumbSchema(String canonicalUrl, List<SiteController.Breadcrumb> breadcrumbs) {
		String origin = canonicalUrl.replaceFirst("^(https?://[^/]+).*$", "$1");
		List<Map<String, Object>> items = new ArrayList<>();
		for (int index = 0; index < breadcrumbs.size(); index++) {
			SiteController.Breadcrumb crumb = breadcrumbs.get(index);
			Map<String, Object> item = new LinkedHashMap<>();
			item.put("@type", "ListItem");
			item.put("position", index + 1);
			item.put("name", crumb.label());
			item.put("item", origin + crumb.href());
			items.add(item);
		}
		Map<String, Object> schema = new LinkedHashMap<>();
		schema.put("@context", "https://schema.org");
		schema.put("@type", "BreadcrumbList");
		schema.put("itemListElement", items);
		return schema;
	}

	private Map<String, Object> buildArticleSchema(String title, String description, String canonicalUrl, String articleDate,
		String imageUrl) {
		Map<String, Object> schema = new LinkedHashMap<>();
		schema.put("@context", "https://schema.org");
		schema.put("@type", "Article");
		schema.put("headline", title);
		schema.put("description", description);
		schema.put("mainEntityOfPage", canonicalUrl);
		if (StringUtils.hasText(articleDate)) {
			schema.put("datePublished", articleDate);
			schema.put("dateModified", articleDate);
		}
		if (StringUtils.hasText(imageUrl)) {
			schema.put("image", List.of(imageUrl));
		}
		schema.put("author", Map.of("@type", "Person", "name", "Homeowner research editor"));
		schema.put("reviewedBy", Map.of("@type", "Person", "name", "Plumbing-risk content reviewer"));
		schema.put("publisher", Map.of("@type", "Organization", "name", "SewerClarity"));
		return schema;
	}

	private Map<String, Object> buildWebApplicationSchema(String name, String description, String canonicalUrl) {
		Map<String, Object> schema = new LinkedHashMap<>();
		schema.put("@context", "https://schema.org");
		schema.put("@type", "WebApplication");
		schema.put("name", name);
		schema.put("description", description);
		schema.put("url", canonicalUrl);
		schema.put("applicationCategory", "UtilityApplication");
		schema.put("operatingSystem", "Any");
		schema.put("isAccessibleForFree", true);
		schema.put("featureList", List.of(
			"Estimate sewer-line risk",
			"Route inspection-first versus quote-ready next steps",
			"Show rough cost direction and uncertainty drivers"
		));
		schema.put("offers", Map.of(
			"@type", "Offer",
			"price", "0",
			"priceCurrency", "USD"
		));
		schema.put("publisher", Map.of("@type", "Organization", "name", "SewerClarity"));
		return schema;
	}

	private void appendSchema(Model model, Map<String, Object> schema) {
		@SuppressWarnings("unchecked")
		List<String> schemaScripts = (List<String>) model.asMap().get("schemaScripts");
		if (schemaScripts == null) {
			schemaScripts = new ArrayList<>();
			model.addAttribute("schemaScripts", schemaScripts);
		}
		schemaScripts.add(writeJsonLd(schema));
	}

	private String writeJsonLd(Map<String, Object> schema) {
		try {
			return objectMapper.writeValueAsString(schema);
		}
		catch (JsonProcessingException exception) {
			throw new UncheckedIOException(exception);
		}
	}

	private String extractOrigin(String canonicalUrl) {
		return canonicalUrl.replaceFirst("^(https?://[^/]+).*$", "$1");
	}

	private String buildCanonicalUrl(HttpServletRequest request) {
		String requestUri = request.getRequestURI();
		if (!StringUtils.hasText(requestUri) || "/".equals(requestUri)) {
			return baseUrl + "/";
		}
		return baseUrl + requestUri;
	}

	private String trimTrailingSlash(String value) {
		return value != null && value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
	}
}

