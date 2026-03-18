package com.example.sewerverdict.estimator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.example.sewerverdict.content.GeoProfileService;
import com.example.sewerverdict.content.SourceRegistryService;

class EstimatorServiceTests {

	private final EstimatorService estimatorService =
		new EstimatorService(
			new CostProfileService(),
			new DefectProfileService(),
			new MaterialProfileService(),
			new GeoProfileService(new SourceRegistryService())
		);

	@Test
	void buyerWithOldHomeAndNoScopeRoutesToInspectionFirst() {
		EstimatorForm form = new EstimatorForm();
		form.setRole("buyer");
		form.setLocation("Philadelphia, PA");
		form.setHouseAgeBand("pre-1950");
		form.setIssueState("no-scope-yet");
		form.setDefectType("unknown");
		form.setUrgency("active-decision");

		EstimatorResult result = estimatorService.evaluate(form);

		assertEquals("inspection-first", result.routingBucket());
		assertEquals("Find sewer camera inspection options", result.primaryCtaLabel());
		assertEquals("inspection", result.primaryServiceNeeded());
		assertTrue(result.localContextSummary().contains("Matched Philadelphia, PA."));
		assertTrue(result.findingReadSummary().contains("Without footage"));
	}

	@Test
	void scopedOrangeburgRoutesToQuoteReady() {
		EstimatorForm form = new EstimatorForm();
		form.setRole("owner");
		form.setHouseAgeBand("1950-1969");
		form.setIssueState("scope-found-issue");
		form.setDefectType("orangeburg");
		form.setUrgency("active-decision");

		EstimatorResult result = estimatorService.evaluate(form);

		assertEquals("quote-ready", result.routingBucket());
		assertEquals("Get sewer repair or replacement quotes", result.primaryCtaLabel());
		assertEquals("replacement", result.primaryServiceNeeded());
		assertTrue(result.findingReadSummary().contains("Orangeburg"));
		assertTrue(result.methodFitSummary().contains("Pipe bursting"));
	}

	@Test
	void urgentSystemicSymptomsDoNotPretendScopeAlreadyConfirmed() {
		EstimatorForm form = new EstimatorForm();
		form.setRole("owner");
		form.setHouseAgeBand("1950-1969");
		form.setIssueState("symptoms-only");
		form.setDefectType("cast-iron");
		form.setAccessType("slab");
		form.setUrgency("urgent-repair");

		EstimatorResult result = estimatorService.evaluate(form);

		assertEquals("quote-ready", result.routingBucket());
		assertEquals("Strong failure signals without a settled work plan", result.evidenceState());
		assertTrue(result.materialReadSummary().contains("Older cast iron"));
	}

	@Test
	void slabAccessAndMatchedMarketWidenReplacementLanguageWithoutFakeLocalQuote() {
		EstimatorForm form = new EstimatorForm();
		form.setRole("owner");
		form.setLocation("Chicago");
		form.setHouseAgeBand("1950-1969");
		form.setIssueState("scope-found-issue");
		form.setDefectType("cast-iron");
		form.setAccessType("slab");
		form.setUrgency("active-decision");

		EstimatorResult result = estimatorService.evaluate(form);

		assertTrue(result.estimateMethodSummary().contains("Under-slab access keeps the upper range wider"));
		assertTrue(result.localContextSummary().contains("Matched Chicago, IL."));
		assertTrue(result.methodFitSummary().contains("Under-slab access keeps excavation"));
	}
}
