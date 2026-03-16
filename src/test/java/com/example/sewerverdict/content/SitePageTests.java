package com.example.sewerverdict.content;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SitePageTests {

	@Test
	void geoBuyerPageInheritsBuyerDecisionLensFamily() {
		SitePage page = new SitePage();
		page.setSlug("/cities/philadelphia/sewer-scope-before-buying-house/");
		page.setFamily("geo");
		page.setSecondaryCtaHref("/sewer-scope-before-buying-house/");

		assertTrue(page.isGeoPage());
		assertTrue(page.isBuyerPage());
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
}
