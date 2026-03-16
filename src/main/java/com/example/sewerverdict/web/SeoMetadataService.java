package com.example.sewerverdict.web;

import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.sewerverdict.content.PageFaq;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SeoMetadataService {

	private final ObjectMapper objectMapper = new ObjectMapper();

	public void apply(Model model, HttpServletRequest request, String title, String description, String ogType,
		List<SiteController.Breadcrumb> breadcrumbs, List<PageFaq> faq, boolean includeOrganization) {
		String canonicalUrl = ServletUriComponentsBuilder.fromRequestUri(request)
			.replaceQuery(null)
			.build()
			.toUriString();

		model.addAttribute("canonicalUrl", canonicalUrl);
		model.addAttribute("ogType", ogType);
		model.addAttribute("ogUrl", canonicalUrl);

		List<String> schemaScripts = new ArrayList<>();
		if (includeOrganization) {
			schemaScripts.add(writeJsonLd(buildOrganizationSchema(canonicalUrl)));
		}
		schemaScripts.add(writeJsonLd(buildWebPageSchema(title, description, canonicalUrl)));
		if ("article".equalsIgnoreCase(ogType)) {
			schemaScripts.add(writeJsonLd(buildArticleSchema(title, description, canonicalUrl)));
		}
		if (breadcrumbs != null && !breadcrumbs.isEmpty()) {
			schemaScripts.add(writeJsonLd(buildBreadcrumbSchema(canonicalUrl, breadcrumbs)));
		}
		model.addAttribute("schemaScripts", schemaScripts);
	}

	private Map<String, Object> buildOrganizationSchema(String canonicalUrl) {
		Map<String, Object> schema = new LinkedHashMap<>();
		schema.put("@context", "https://schema.org");
		schema.put("@type", "Organization");
		schema.put("name", "SewerVerdict");
		schema.put("url", canonicalUrl);
		schema.put("description", "Buyer-first sewer scope risk and next-step guidance for buyers, sellers, and owners.");
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

	private Map<String, Object> buildArticleSchema(String title, String description, String canonicalUrl) {
		Map<String, Object> schema = new LinkedHashMap<>();
		schema.put("@context", "https://schema.org");
		schema.put("@type", "Article");
		schema.put("headline", title);
		schema.put("description", description);
		schema.put("mainEntityOfPage", canonicalUrl);
		schema.put("author", Map.of("@type", "Person", "name", "Homeowner research editor"));
		schema.put("reviewedBy", Map.of("@type", "Person", "name", "Plumbing-risk content reviewer"));
		schema.put("publisher", Map.of("@type", "Organization", "name", "SewerVerdict"));
		return schema;
	}

	private String writeJsonLd(Map<String, Object> schema) {
		try {
			return objectMapper.writeValueAsString(schema);
		}
		catch (JsonProcessingException exception) {
			throw new UncheckedIOException(exception);
		}
	}
}
