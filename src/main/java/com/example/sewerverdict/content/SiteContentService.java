package com.example.sewerverdict.content;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SiteContentService {

	private static final String CONTENT_PATH = "content/pages.json";

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final Map<String, SitePage> pagesBySlug;
	private final List<SitePage> pages;

	public SiteContentService(@Value("${app.review-date}") String reviewDate) {
		this.pages = loadPages(reviewDate);
		this.pagesBySlug = new LinkedHashMap<>();
		this.pages.forEach(page -> this.pagesBySlug.put(normalize(page.getSlug()), page));
	}

	public SitePage getPage(String slug) {
		return pagesBySlug.get(normalize(slug));
	}

	public SitePage requirePage(String slug) {
		SitePage page = getPage(slug);
		if (page == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		return page;
	}

	public List<SitePage> getRelatedPages(SitePage page) {
		return page.getRelatedSlugs().stream()
			.map(this::getPage)
			.filter(Objects::nonNull)
			.toList();
	}

	public List<SitePage> getFeaturedPages(List<String> slugs) {
		return slugs.stream()
			.map(this::getPage)
			.filter(Objects::nonNull)
			.toList();
	}

	public List<SitePage> getPagesByFamily(String family) {
		return pages.stream()
			.filter(page -> family.equalsIgnoreCase(page.getFamily()))
			.toList();
	}

	public List<SitePage> getAllPages() {
		return List.copyOf(pages);
	}

	private List<SitePage> loadPages(String reviewDate) {
		Resource resource = new ClassPathResource(CONTENT_PATH);
		try (InputStream inputStream = resource.getInputStream()) {
			List<SitePage> loadedPages = objectMapper.readValue(inputStream, new TypeReference<List<SitePage>>() {
			});
			loadedPages.forEach(page -> {
				page.setSlug(normalize(page.getSlug()));
				if (!StringUtils.hasText(page.getLastReviewed())) {
					page.setLastReviewed(reviewDate);
				}
				if (!StringUtils.hasText(page.getAuthorRole())) {
					page.setAuthorRole("Homeowner research editor");
				}
				if (!StringUtils.hasText(page.getReviewerRole())) {
					page.setReviewerRole("Plumbing-risk content reviewer");
				}
				if (!StringUtils.hasText(page.getSourceNote())) {
					page.setSourceNote("Reviewed against the SewerClarity source registry and range-based methodology.");
				}
			});
			return loadedPages;
		}
		catch (IOException exception) {
			throw new UncheckedIOException("Failed to load page content from " + CONTENT_PATH, exception);
		}
	}

	private String normalize(String slug) {
		if (!StringUtils.hasText(slug) || "/".equals(slug)) {
			return "/";
		}
		String value = slug.startsWith("/") ? slug : "/" + slug;
		return value.endsWith("/") ? value : value + "/";
	}
}

