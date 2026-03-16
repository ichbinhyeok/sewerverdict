package com.example.sewerverdict.estimator;

import java.util.List;

public record EstimatorResult(
	String riskTier,
	String riskClass,
	String interpretation,
	String likelyNextStep,
	String primaryCtaLabel,
	String primaryCtaHref,
	String secondaryCtaLabel,
	String secondaryCtaHref,
	List<CostBand> costBands,
	List<String> uncertaintyDrivers,
	List<String> questions,
	String summaryBlock
) {
}
