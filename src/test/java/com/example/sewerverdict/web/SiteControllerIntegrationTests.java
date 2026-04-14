package com.example.sewerverdict.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SiteControllerIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void citiesHubRendersTierOneCitiesAndPages() throws Exception {
		mockMvc.perform(get("/cities/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("City pages built only where the local signal is real.")))
			.andExpect(content().string(containsString("Start here")))
			.andExpect(content().string(containsString("Philadelphia, PA")))
			.andExpect(content().string(containsString("Buffalo Sewer Scope Before Buying a House")))
			.andExpect(content().string(containsString("Philadelphia Sewer Line Repair vs Replacement")))
			.andExpect(content().string(containsString("Chicago Sewer Line Repair vs Replacement")))
			.andExpect(content().string(containsString("Milwaukee, WI")))
			.andExpect(content().string(containsString("Detroit Sewer Line Replacement Cost")))
			.andExpect(content().string(containsString("Milwaukee Sewer Scope Before Buying a House")))
			.andExpect(content().string(containsString("Baltimore Sewer Scope Before Buying a House")))
			.andExpect(content().string(containsString("Detroit Sewer Scope Negotiation With Seller")))
			.andExpect(content().string(containsString("Detroit Sewer Line Repair vs Replacement")));
	}

	@Test
	void homePageHighlightsBuyerLateralAndResponsibilityWedge() throws Exception {
		mockMvc.perform(get("/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Before transfer, closing, or city-boundary assumptions harden")))
			.andExpect(content().string(containsString("Use city sewer lateral transfer and compliance signals before you negotiate, quote, or guess who owns the problem.")))
			.andExpect(content().string(containsString("Transfer or closing pressure")))
			.andExpect(content().string(containsString("Buyer, seller, or owner exposure unclear")))
			.andExpect(content().string(containsString("Report finding or known defect")))
			.andExpect(content().string(containsString("Interpret the finding before you turn it into a transfer or quote ask.")))
			.andExpect(content().string(containsString("data-event-type=\"page_cta_click\"")))
			.andExpect(content().string(containsString("data-event-route=\"responsibility-first\"")))
			.andExpect(content().string(containsString("Homeowner vs City Sewer Responsibility")))
			.andExpect(content().string(containsString("Sewer Scope Negotiation With Seller")))
			.andExpect(content().string(containsString("How to Read a Sewer Scope Report")))
			.andExpect(content().string(containsString("Cast Iron Sewer Pipe Replacement Cost")))
			.andExpect(content().string(containsString("Orangeburg Pipe Replacement Cost")))
			.andExpect(content().string(containsString("Start with transfer, compliance, and report-first decisions")))
			.andExpect(content().string(containsString("Report, material, and defect paths once the transfer story has real evidence")))
			.andExpect(content().string(containsString("data-event-placement=\"home-featured\"")))
			.andExpect(content().string(containsString("data-event-placement=\"home-issue\"")))
			.andExpect(content().string(containsString("Who Pays")))
			.andExpect(content().string(containsString("Read the scope calmly")))
			.andExpect(content().string(containsString("Detroit Sewer Scope Before Buying a House")));
	}

	@Test
	void cityHubRendersStarterPagesAndSources() throws Exception {
		mockMvc.perform(get("/cities/philadelphia/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Philadelphia, PA Sewer Pages")))
			.andExpect(content().string(containsString("tier-1 lateral-risk hub")))
			.andExpect(content().string(containsString("Start Here in Philadelphia")))
			.andExpect(content().string(containsString("Boundary, program, or ownership unclear -> compliance first")))
			.andExpect(content().string(containsString("transfer and closing pages first, official responsibility or program context next")))
			.andExpect(content().string(containsString("Pick the path that matches the situation")))
			.andExpect(content().string(containsString("data-event-type=\"page_cta_click\"")))
			.andExpect(content().string(containsString("Philadelphia Who Pays for Sewer Line Repair: Buyer or Seller")))
			.andExpect(content().string(containsString("Philadelphia Sewer Line Repair vs Replacement")))
			.andExpect(content().string(containsString("Transfer path")))
			.andExpect(content().string(containsString("Compliance path")))
			.andExpect(content().string(containsString("Compliance, responsibility, and program pages")))
			.andExpect(content().string(containsString("Official transfer or certificate signal")))
			.andExpect(content().string(containsString("property sales certification")))
			.andExpect(content().string(containsString("Use estimator from city hub")))
			.andExpect(content().string(containsString("Sources used for this city hub")));

		mockMvc.perform(get("/cities/milwaukee/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Milwaukee, WI Sewer Pages")))
			.andExpect(content().string(containsString("Milwaukee Sewer Scope Before Buying a House")))
			.andExpect(content().string(containsString("Milwaukee Sewer Backup Risk")))
			.andExpect(content().string(containsString("Cost and quote-ready pages")));
	}

	@Test
	void geoPagesLinkBackToCityHubAndCityBreadcrumbs() throws Exception {
		mockMvc.perform(get("/cities/chicago/sewer-line-repair-vs-replacement/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("href=\"/cities/chicago/\"")))
			.andExpect(content().string(containsString("Keep moving inside Chicago")))
			.andExpect(content().string(containsString("Go back to Chicago hub")))
			.andExpect(content().string(containsString("Open Chicago city hub")))
			.andExpect(content().string(containsString("See Chicago starter links")))
			.andExpect(content().string(containsString("Chicago, IL")));
	}

	@Test
	void deepenedGeoPageRendersSuccessfully() throws Exception {
		mockMvc.perform(get("/cities/chicago/sewer-scope-negotiation-with-seller/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Chicago Sewer Scope Negotiation With Seller")))
			.andExpect(content().string(containsString("Local system context")));
	}

	@Test
	void newResponsibilityGeoPageRendersSuccessfully() throws Exception {
		mockMvc.perform(get("/cities/baltimore/homeowner-vs-city-sewer-responsibility/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Baltimore Homeowner vs City Sewer Responsibility")))
			.andExpect(content().string(containsString("Official responsibility boundary")));
	}

	@Test
	void newBuyerAndBackupGeoPagesRenderSuccessfully() throws Exception {
		mockMvc.perform(get("/cities/detroit/sewer-scope-before-buying-house/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Detroit Sewer Scope Before Buying a House")))
			.andExpect(content().string(containsString("Local market signal")));

		mockMvc.perform(get("/cities/cincinnati/sewer-backup-risk/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Cincinnati Sewer Backup Risk")))
			.andExpect(content().string(containsString("Cincinnati responsibility guide")));
	}

	@Test
	void newNegotiationAndBaltimoreBuyerPagesRenderSuccessfully() throws Exception {
		mockMvc.perform(get("/cities/milwaukee/sewer-scope-negotiation-with-seller/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Milwaukee Sewer Scope Negotiation With Seller")))
			.andExpect(content().string(containsString("Transfer and closing lens")));

		mockMvc.perform(get("/cities/baltimore/sewer-scope-before-buying-house/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Baltimore Sewer Scope Before Buying a House")))
			.andExpect(content().string(containsString("Official responsibility boundary")));
	}

	@Test
	void newTierTwoNegotiationAndBackupPagesRenderSuccessfully() throws Exception {
		mockMvc.perform(get("/cities/milwaukee/sewer-backup-risk/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Milwaukee Sewer Backup Risk")))
			.andExpect(content().string(containsString("Defect interpretation lens")));

		mockMvc.perform(get("/cities/cincinnati/sewer-scope-negotiation-with-seller/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Cincinnati Sewer Scope Negotiation With Seller")))
			.andExpect(content().string(containsString("Transfer and closing lens")));

		mockMvc.perform(get("/cities/baltimore/sewer-scope-negotiation-with-seller/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Baltimore Sewer Scope Negotiation With Seller")))
			.andExpect(content().string(containsString("Official responsibility boundary")));

		mockMvc.perform(get("/cities/detroit/sewer-scope-negotiation-with-seller/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Detroit Sewer Scope Negotiation With Seller")))
			.andExpect(content().string(containsString("Transfer and closing lens")));
	}

	@Test
	void localRepairVsReplacementPagesRenderSuccessfully() throws Exception {
		mockMvc.perform(get("/cities/milwaukee/sewer-line-repair-vs-replacement/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Milwaukee Sewer Line Repair vs Replacement")))
			.andExpect(content().string(containsString("Quote comparison lens")));

		mockMvc.perform(get("/cities/cincinnati/sewer-line-repair-vs-replacement/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Cincinnati Sewer Line Repair vs Replacement")))
			.andExpect(content().string(containsString("Quote comparison lens")));

		mockMvc.perform(get("/cities/baltimore/sewer-line-repair-vs-replacement/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Baltimore Sewer Line Repair vs Replacement")))
			.andExpect(content().string(containsString("Official responsibility boundary")));

		mockMvc.perform(get("/cities/detroit/sewer-line-repair-vs-replacement/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Detroit Sewer Line Repair vs Replacement")))
			.andExpect(content().string(containsString("Quote comparison lens")));
	}

	@Test
	void tierOneLocalRepairVsReplacementPagesRenderSuccessfully() throws Exception {
		mockMvc.perform(get("/cities/philadelphia/sewer-line-repair-vs-replacement/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Philadelphia Sewer Line Repair vs Replacement")))
			.andExpect(content().string(containsString("Official responsibility boundary")));

		mockMvc.perform(get("/cities/pittsburgh/sewer-line-repair-vs-replacement/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Pittsburgh Sewer Line Repair vs Replacement")))
			.andExpect(content().string(containsString("Official responsibility boundary")));

		mockMvc.perform(get("/cities/cleveland/sewer-line-repair-vs-replacement/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Cleveland Sewer Line Repair vs Replacement")))
			.andExpect(content().string(containsString("Quote comparison lens")));

		mockMvc.perform(get("/cities/chicago/sewer-line-repair-vs-replacement/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Chicago Sewer Line Repair vs Replacement")))
			.andExpect(content().string(containsString("Quote comparison lens")));

		mockMvc.perform(get("/cities/buffalo/sewer-line-repair-vs-replacement/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Buffalo Sewer Line Repair vs Replacement")))
			.andExpect(content().string(containsString("Quote comparison lens")));
	}

	@Test
	void trustPagesExposeEditorialAndPrivacySurfaces() throws Exception {
		mockMvc.perform(get("/editorial-standards/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Editorial Standards")))
			.andExpect(content().string(containsString("What SewerClarity will not claim")))
			.andExpect(content().string(containsString("contact@sewerclarity.com")))
			.andExpect(content().string(containsString("Privacy")));

		mockMvc.perform(get("/privacy/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Privacy and Data Handling")))
			.andExpect(content().string(containsString("What SewerClarity collects")))
			.andExpect(content().string(containsString("When information may be shared")))
			.andExpect(content().string(containsString("mailto:contact@sewerclarity.com")));
	}

	@Test
	void nationalPagesExposeInlineRelatedLinksAndCleanSourceMeta() throws Exception {
		mockMvc.perform(get("/sewer-scope-red-flags/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Most readers follow this page with")))
			.andExpect(content().string(containsString("Offset Joint Sewer Line Meaning")))
			.andExpect(content().string(containsString("How to Read a Sewer Scope Report")))
			.andExpect(content().string(containsString("More in this topic")))
			.andExpect(content().string(containsString("Primary page")))
			.andExpect(content().string(containsString("Support page")))
			.andExpect(content().string(containsString("Collapsed Sewer Line Signs")))
			.andExpect(content().string(containsString("Root Intrusion in a Sewer Line: What to Do")))
			.andExpect(content().string(containsString("| verified ")));
	}

	@Test
	void coveragePagesKeepCoverageLensInsteadOfComplianceLens() throws Exception {
		mockMvc.perform(get("/does-home-insurance-cover-sewer-line-replacement/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Coverage and responsibility lens")))
			.andExpect(content().string(not(containsString("Compliance and responsibility lens"))));
	}

	@Test
	void trustTopicClustersSurfaceWinnerBeforeSupportPages() throws Exception {
		mockMvc.perform(get("/privacy/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("More in this topic")))
			.andExpect(content().string(containsString("Methodology")))
			.andExpect(content().string(containsString("Primary page")))
			.andExpect(content().string(containsString("Support page")));
	}

	@Test
	void articlePagesExposeOgImageAndArticleDates() throws Exception {
		mockMvc.perform(get("/sewer-line-replacement-cost/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("href=\"https://sewerclarity.com/sewer-line-replacement-cost/\"")))
			.andExpect(content().string(containsString("/og-default.svg")))
			.andExpect(content().string(containsString("\"datePublished\"")))
			.andExpect(content().string(containsString("\"dateModified\"")))
			.andExpect(content().string(containsString("\"image\"")));
	}

	@Test
	void canonicalRemainsHttpsNonWwwWhenRequestHostIsHttpOrWww() throws Exception {
		mockMvc.perform(get("/sewer-line-replacement-cost/").secure(false).header("Host", "www.sewerclarity.com"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("href=\"https://sewerclarity.com/sewer-line-replacement-cost/\"")))
			.andExpect(content().string(containsString("\"url\":\"https://sewerclarity.com/sewer-line-replacement-cost/\"")));
	}

	@Test
	void newWinnerSupportPagesRenderSuccessfully() throws Exception {
		mockMvc.perform(get("/how-to-read-sewer-scope-report/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("How to Read a Sewer Scope Report")))
			.andExpect(content().string(containsString("How report language usually changes the next move")));

		mockMvc.perform(get("/sewer-scope-inspection-cost/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Sewer Scope Inspection Cost")))
			.andExpect(content().string(containsString("$175-$800")));

		mockMvc.perform(get("/sewer-line-under-slab-repair-cost/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Sewer Line Under Slab Repair Cost")))
			.andExpect(content().string(containsString("Short under-slab repair or short-run replacement")));

		mockMvc.perform(get("/cast-iron-pipe-deterioration-signs/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Cast Iron Pipe Deterioration Signs")))
			.andExpect(content().string(containsString("Cast iron sign versus what it changes")));

		mockMvc.perform(get("/is-sewer-scope-worth-it/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Is a Sewer Scope Worth It?")))
			.andExpect(content().string(containsString("When a sewer scope is worth it")));

		mockMvc.perform(get("/sewer-lateral-repair-cost/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Sewer Lateral Repair Cost")))
			.andExpect(content().string(containsString("Typical private-lateral work paths")));
	}

	@Test
	void sharpenedWinnerPagesReflectBuyerAndResponsibilityWedge() throws Exception {
		mockMvc.perform(get("/sewer-scope-before-buying-house/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("buyer-paid repair")))
			.andExpect(content().string(containsString("get a scope before you start arguing over credits, repair bids, or who should pay")))
			.andExpect(content().string(containsString("City rule may change the ask")))
			.andExpect(content().string(containsString("Open city transfer paths")))
			.andExpect(content().string(containsString("data-event-type=\"page_cta_click\"")));

		mockMvc.perform(get("/who-pays-for-sewer-line-repair-buyer-or-seller/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("seller credit")))
			.andExpect(content().string(containsString("private-lateral repair or broader replacement")))
			.andExpect(content().string(containsString("Clarify homeowner vs city responsibility")))
			.andExpect(content().string(containsString("data-event-type=\"page_cta_click\"")));

		mockMvc.perform(get("/homeowner-vs-city-sewer-responsibility/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("City examples that show why the answer changes")))
			.andExpect(content().string(containsString("See city compliance pages")))
			.andExpect(content().string(containsString("City rule or program still matters")))
			.andExpect(content().string(containsString("Sewer Scope Before Buying a House")));
	}

	@Test
	void defectAndCostPagesExposeInterpretationAndLeadRoutingChoices() throws Exception {
		mockMvc.perform(get("/sewer-scope-red-flags/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Read sewer scope report language")))
			.andExpect(content().string(containsString("Quote-ready defect")))
			.andExpect(content().string(containsString("Move into quote-ready follow-up")))
			.andExpect(content().string(containsString("data-event-route=\"quote-ready\"")))
			.andExpect(content().string(containsString("data-event-type=\"page_cta_click\"")));

		mockMvc.perform(get("/sewer-lateral-repair-cost/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Clarify who owns the line")))
			.andExpect(content().string(containsString("Private lateral confirmed")))
			.andExpect(content().string(containsString("data-event-destination=\"lead-route\"")));

		mockMvc.perform(get("/cast-iron-sewer-pipe-replacement-cost/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Find sewer camera inspection options")))
			.andExpect(content().string(containsString("Sewer Scope Red Flags")))
			.andExpect(content().string(not(containsString(">Use the estimator<"))));

		mockMvc.perform(get("/orangeburg-pipe-replacement-cost/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Find sewer camera inspection options")))
			.andExpect(content().string(containsString("How to Read a Sewer Scope Report")))
			.andExpect(content().string(not(containsString(">Use the estimator<"))));
	}

	@Test
	void redFlagsStaysVisibleOnNationalAndGeoFollowUpPaths() throws Exception {
		mockMvc.perform(get("/sewer-scope-inspection-cost/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("href=\"/sewer-scope-red-flags/\"")));

		mockMvc.perform(get("/is-sewer-scope-worth-it/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("href=\"/sewer-scope-red-flags/\"")));

		mockMvc.perform(get("/cities/milwaukee/homeowner-vs-city-sewer-responsibility/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("href=\"/sewer-scope-red-flags/\"")));

		mockMvc.perform(get("/cities/detroit/sewer-scope-before-buying-house/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("href=\"/sewer-scope-red-flags/\"")));
	}
}

