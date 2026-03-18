package com.example.sewerverdict.estimator;

import java.util.List;

public record EstimatorResult(
	String riskTier,
	String riskClass,
	String evidenceState,
	String routingBucket,
	String routingRationale,
	String interpretation,
	String evidenceSummary,
	String findingReadSummary,
	String severityUpgradeSummary,
	String severityDowngradeSummary,
	String methodFitSummary,
	String materialReadSummary,
	String estimateMethodSummary,
	List<String> sourceIds,
	String localContextSummary,
	String likelyNextStep,
	String primaryCtaLabel,
	String primaryCtaHref,
	String primaryServiceNeeded,
	String secondaryCtaLabel,
	String secondaryCtaHref,
	String secondaryServiceNeeded,
	List<String> callDrivers,
	List<String> inputSummary,
	List<CostBand> costBands,
	List<String> uncertaintyDrivers,
	List<String> questions,
	String summaryBlock
) {
}
