package com.example.sewerverdict.estimator;

import java.util.List;

public record EstimatorResult(
	String riskTier,
	String riskClass,
	String evidenceState,
	String routingBucket,
	String routingRationale,
	String interpretation,
	String likelyNextStep,
	String primaryCtaLabel,
	String primaryCtaHref,
	String primaryServiceNeeded,
	String secondaryCtaLabel,
	String secondaryCtaHref,
	String secondaryServiceNeeded,
	List<CostBand> costBands,
	List<String> uncertaintyDrivers,
	List<String> questions,
	String summaryBlock
) {
}
