package com.example.sewerverdict.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class GeoProfileServiceTests {

	private final SiteContentService siteContentService = new SiteContentService("2026-03-17");
	private final SourceRegistryService sourceRegistryService = new SourceRegistryService();
	private final GeoProfileService geoProfileService = new GeoProfileService(sourceRegistryService);

	@Test
	void genericNationalCostPageNoLongerPromotesGeoDuplicatesAsCompanions() {
		SitePage nationalPage = siteContentService.requirePage("/sewer-line-replacement-cost/");

		List<SitePage> companions = geoProfileService.getGeoCompanionPages(nationalPage, siteContentService.getAllPages(), 4);

		assertFalse(companions.isEmpty());
		assertTrue(companions.stream().allMatch(SitePage::isGeoLocalSignalPage));
		assertFalse(companions.stream().anyMatch(SitePage::isGeoGenericIntentPage));
	}

	@Test
	void localSignalNationalPageStillGetsGeoCompanionPages() {
		SitePage nationalPage = siteContentService.requirePage("/homeowner-vs-city-sewer-responsibility/");

		List<SitePage> companions = geoProfileService.getGeoCompanionPages(nationalPage, siteContentService.getAllPages(), 4);

		assertFalse(companions.isEmpty());
		assertTrue(companions.stream().allMatch(SitePage::isGeoLocalSignalPage));
		assertTrue(companions.stream().anyMatch(page -> page.getSlug().equals("/cities/philadelphia/homeowner-vs-city-sewer-responsibility/")));
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

	@Test
	void cityHubStartersKeepBuyerAndCostAheadOfSupportRailsWhenNoDefectPageExists() {
		CityHubEntry entry = geoProfileService.getCityHubEntry("philadelphia", siteContentService.getAllPages());

		assertNotNull(entry);
		assertEquals("/cities/philadelphia/sewer-scope-before-buying-house/", entry.starterPages().get(0).getSlug());
		assertEquals("/cities/philadelphia/sewer-line-repair-vs-replacement/", entry.starterPages().get(1).getSlug());
		assertEquals("/cities/philadelphia/old-house-sewer-line-risk/", entry.starterPages().get(2).getSlug());
		assertEquals(3, entry.starterPages().size());
	}

	@Test
	void cityHubStartersPreferDefectInterpretationBeforeCostWhenAvailable() {
		CityHubEntry entry = geoProfileService.getCityHubEntry("milwaukee", siteContentService.getAllPages());

		assertNotNull(entry);
		assertEquals("/cities/milwaukee/sewer-scope-before-buying-house/", entry.starterPages().get(0).getSlug());
		assertEquals("/cities/milwaukee/sewer-backup-risk/", entry.starterPages().get(1).getSlug());
		assertEquals("/cities/milwaukee/sewer-line-repair-vs-replacement/", entry.starterPages().get(2).getSlug());
		assertEquals(3, entry.starterPages().size());
	}

	@Test
	void zipOnlyLocationCanAnchorToCoveredDeliveryMarket() {
		GeoLocationMatch match = geoProfileService.resolveLocationMatch("19147");

		assertNotNull(match);
		assertEquals("philadelphia", match.profile().getCitySlug());
		assertTrue(match.zipBased());
		assertEquals("19147", match.zipCode());
		assertEquals("municipal-safe", match.matchScope());
		assertTrue(match.cityConfirmationNeeded());
	}

	@Test
	void zipPlusFourAlsoMatchesCoveredDeliveryMarket() {
		GeoLocationMatch match = geoProfileService.resolveLocationMatch("60614");

		assertNotNull(match);
		assertEquals("chicago", match.profile().getCitySlug());
		assertTrue(match.zipBased());
		assertEquals("60614", match.zipCode());
		assertEquals("delivery-market", match.matchScope());
		assertTrue(match.cityConfirmationNeeded());
	}

	@Test
	void directCityMatchDoesNotRequireCityConfirmation() {
		GeoLocationMatch match = geoProfileService.resolveLocationMatch("Philadelphia, PA");

		assertNotNull(match);
		assertEquals("philadelphia", match.profile().getCitySlug());
		assertFalse(match.zipBased());
		assertFalse(match.cityConfirmationNeeded());
	}

	@Test
	void unsupportedZipDoesNotPretendToMatchACoveredCity() {
		GeoLocationMatch match = geoProfileService.resolveLocationMatch("99999");

		assertNull(match);
	}
}
