package com.example.sewerverdict.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class GeoProfileServiceTests {

	private final SiteContentService siteContentService = new SiteContentService("2026-03-17");
	private final SourceRegistryService sourceRegistryService = new SourceRegistryService();
	private final GeoProfileService geoProfileService = new GeoProfileService(sourceRegistryService);

	@Test
	void nationalPageGetsGeoCompanionPagesFromRelatedSlugs() {
		SitePage nationalPage = siteContentService.requirePage("/sewer-line-replacement-cost/");

		List<SitePage> companions = geoProfileService.getGeoCompanionPages(nationalPage, siteContentService.getAllPages(), 4);

		assertFalse(companions.isEmpty());
		assertEquals("/cities/buffalo/sewer-line-replacement-cost/", companions.get(0).getSlug());
		assertTrue(companions.stream().anyMatch(page -> page.getSlug().equals("/cities/chicago/sewer-line-replacement-cost/")));
		assertTrue(companions.stream().anyMatch(page -> page.getSlug().equals("/cities/cleveland/sewer-line-replacement-cost/")));
	}

	@Test
	void geoResponsibilityPageLoadsProfileAndOfficialRuleViews() {
		SitePage geoPage = siteContentService.requirePage("/cities/philadelphia/homeowner-vs-city-sewer-responsibility/");

		GeoProfile profile = geoProfileService.getProfileForPage(geoPage);
		List<ResponsibilityRuleView> ruleViews = geoProfileService.getResponsibilityRuleViews(geoPage);

		assertNotNull(profile);
		assertEquals("philadelphia", profile.getCitySlug());
		assertFalse(ruleViews.isEmpty());
		assertNotNull(ruleViews.get(0).source());
	}
}
