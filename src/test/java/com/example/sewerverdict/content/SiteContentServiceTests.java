package com.example.sewerverdict.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class SiteContentServiceTests {

	private final SiteContentService siteContentService = new SiteContentService("2026-03-17");

	@Test
	void transferPageRelatedPagesFavorInterpretationBeforeResponsibilityRails() {
		SitePage page = siteContentService.requirePage("/sewer-scope-before-buying-house/");

		List<SitePage> relatedPages = siteContentService.getRelatedPages(page);

		assertEquals("/sewer-scope-red-flags/", relatedPages.get(0).getSlug());
		assertEquals("/how-to-read-sewer-scope-report/", relatedPages.get(1).getSlug());
		assertTrue(indexOfSlug(relatedPages, "/how-to-read-sewer-scope-report/")
			< indexOfSlug(relatedPages, "/who-pays-for-sewer-line-repair-buyer-or-seller/"));
		assertTrue(indexOfSlug(relatedPages, "/how-to-read-sewer-scope-report/")
			< indexOfSlug(relatedPages, "/homeowner-vs-city-sewer-responsibility/"));
	}

	@Test
	void compliancePageRelatedPagesFavorCoreFlowBeforeMoreResponsibilityLooping() {
		SitePage page = siteContentService.requirePage("/homeowner-vs-city-sewer-responsibility/");

		List<SitePage> relatedPages = siteContentService.getRelatedPages(page);

		assertEquals("/sewer-scope-before-buying-house/", relatedPages.get(0).getSlug());
		assertTrue(indexOfSlug(relatedPages, "/is-sewer-scope-worth-it/")
			< indexOfSlug(relatedPages, "/who-pays-for-sewer-line-repair-buyer-or-seller/"));
		assertTrue(indexOfSlug(relatedPages, "/how-to-read-sewer-scope-report/")
			< indexOfSlug(relatedPages, "/who-pays-for-sewer-line-repair-buyer-or-seller/"));
	}

	private int indexOfSlug(List<SitePage> pages, String slug) {
		for (int index = 0; index < pages.size(); index++) {
			if (slug.equals(pages.get(index).getSlug())) {
				return index;
			}
		}
		return Integer.MAX_VALUE;
	}
}
