package com.example.sewerverdict.content;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SitePageTests {

	@Test
	void geoBuyerPageInheritsTransferDecisionLensFamily() {
		SitePage page = new SitePage();
		page.setSlug("/cities/philadelphia/sewer-scope-before-buying-house/");
		page.setFamily("geo");
		page.setSecondaryCtaHref("/sewer-scope-before-buying-house/");

		assertTrue(page.isGeoPage());
		assertTrue(page.isTransferPage());
		assertFalse(page.isBuyerPage());
		assertFalse(page.isCostPage());
	}

	@Test
	void geoCostPageInheritsCostDecisionLensFamily() {
		SitePage page = new SitePage();
		page.setSlug("/cities/chicago/sewer-line-replacement-cost/");
		page.setFamily("geo");
		page.setSecondaryCtaHref("/sewer-line-replacement-cost/");

		assertTrue(page.isCostPage());
		assertFalse(page.isBuyerPage());
	}

	@Test
	void geoDefectPageInheritsDefectDecisionLensFamily() {
		SitePage page = new SitePage();
		page.setSlug("/cities/pittsburgh/cast-iron-sewer-line-risk/");
		page.setFamily("geo");
		page.setSecondaryCtaHref("/cast-iron-sewer-pipe-replacement-cost/");

		assertTrue(page.isDefectPage());
		assertFalse(page.isCostPage());
	}

	@Test
	void geoOldHouseRiskPageStaysBuyerIntentInsteadOfGenericRisk() {
		SitePage page = new SitePage();
		page.setSlug("/cities/cleveland/old-house-sewer-line-risk/");
		page.setFamily("geo");
		page.setSecondaryCtaHref("/old-house-sewer-line-risk/");

		assertTrue(page.isBuyerPage());
		assertFalse(page.isDefectPage());
	}

	@Test
	void geoNegotiationPageInheritsTransferDecisionLensFamily() {
		SitePage page = new SitePage();
		page.setSlug("/cities/philadelphia/sewer-scope-negotiation-with-seller/");
		page.setFamily("geo");
		page.setSecondaryCtaHref("/sewer-scope-negotiation-with-seller/");

		assertTrue(page.isTransferPage());
		assertFalse(page.isBuyerPage());
		assertFalse(page.isDefectPage());
	}

	@Test
	void geoResponsibilityPageInheritsComplianceFamily() {
		SitePage page = new SitePage();
		page.setSlug("/cities/philadelphia/homeowner-vs-city-sewer-responsibility/");
		page.setFamily("geo");
		page.setSecondaryCtaHref("/homeowner-vs-city-sewer-responsibility/");

		assertTrue(page.isCompliancePage());
		assertFalse(page.isCoveragePage());
		assertFalse(page.isCostPage());
	}

	@Test
	void geoHelpersDistinguishLocalSignalPagesFromGenericCityDuplicates() {
		SitePage localSignalPage = new SitePage();
		localSignalPage.setSlug("/cities/philadelphia/homeowner-vs-city-sewer-responsibility/");
		localSignalPage.setFamily("geo");

		SitePage genericIntentPage = new SitePage();
		genericIntentPage.setSlug("/cities/chicago/sewer-line-replacement-cost/");
		genericIntentPage.setFamily("geo");

		assertTrue(localSignalPage.isGeoLocalSignalPage());
		assertFalse(localSignalPage.isGeoGenericIntentPage());
		assertFalse(genericIntentPage.isGeoLocalSignalPage());
		assertTrue(genericIntentPage.isGeoGenericIntentPage());
	}

	@Test
	void transactionPagePromotesTransferLensAheadOfGenericBuyerBucket() {
		SitePage page = new SitePage();
		page.setSlug("/sewer-scope-negotiation-with-seller/");
		page.setFamily("transfer");

		assertTrue(page.isTransferPage());
		assertEquals("inspection-first", page.getRecommendedRouteBucket());
		assertEquals("transfer-page", page.getMeasurementDestination());
	}

	@Test
	void transferPageDoesNotBecomeComplianceJustBecauseItLinksThere() {
		SitePage page = new SitePage();
		page.setSlug("/sewer-scope-before-buying-house/");
		page.setFamily("transfer");
		page.setSecondaryCtaHref("/who-pays-for-sewer-line-repair-buyer-or-seller/");

		assertTrue(page.isTransferPage());
		assertFalse(page.isCompliancePage());
		assertEquals("inspection-first", page.getRecommendedRouteBucket());
		assertEquals("transfer-page", page.getMeasurementDestination());
	}

	@Test
	void buyerSellerResponsibilityPageUsesTransferDestination() {
		SitePage page = new SitePage();
		page.setSlug("/who-pays-for-sewer-line-repair-buyer-or-seller/");
		page.setFamily("transfer");

		assertTrue(page.isTransferPage());
		assertFalse(page.isCompliancePage());
		assertEquals("inspection-first", page.getRecommendedRouteBucket());
		assertEquals("transfer-page", page.getMeasurementDestination());
	}

	@Test
	void insuranceCoveragePageStaysCoverageWithoutCompliancePromotion() {
		SitePage page = new SitePage();
		page.setSlug("/does-home-insurance-cover-sewer-line-replacement/");
		page.setFamily("coverage");

		assertTrue(page.isCoveragePage());
		assertFalse(page.isCompliancePage());
		assertEquals("needs-clarification", page.getRecommendedRouteBucket());
		assertEquals("page", page.getMeasurementDestination());
	}

	@Test
	void costPageDoesNotBecomeComplianceJustBecauseItsSecondaryCtaClarifiesOwnership() {
		SitePage page = new SitePage();
		page.setSlug("/sewer-lateral-repair-cost/");
		page.setFamily("cost");
		page.setSecondaryCtaHref("/homeowner-vs-city-sewer-responsibility/");

		assertTrue(page.isCostPage());
		assertFalse(page.isCompliancePage());
		assertEquals("quote-ready", page.getRecommendedRouteBucket());
		assertEquals("quote-page", page.getMeasurementDestination());
	}
}
