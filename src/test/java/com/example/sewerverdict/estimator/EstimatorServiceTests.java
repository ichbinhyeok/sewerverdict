package com.example.sewerverdict.estimator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EstimatorServiceTests {

	private final EstimatorService estimatorService = new EstimatorService();

	@Test
	void buyerWithOldHomeAndNoScopeRoutesToInspectionFirst() {
		EstimatorForm form = new EstimatorForm();
		form.setRole("buyer");
		form.setHouseAgeBand("pre-1950");
		form.setIssueState("no-scope-yet");
		form.setDefectType("unknown");
		form.setUrgency("active-decision");

		EstimatorResult result = estimatorService.evaluate(form);

		assertEquals("inspection-first", result.routingBucket());
		assertEquals("Find sewer camera inspection options", result.primaryCtaLabel());
		assertEquals("inspection", result.primaryServiceNeeded());
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
	}
}
