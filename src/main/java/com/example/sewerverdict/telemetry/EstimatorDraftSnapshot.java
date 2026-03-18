package com.example.sewerverdict.telemetry;

import java.util.List;

public record EstimatorDraftSnapshot(
	String draftId,
	String location,
	String riskTier,
	String likelyNextStep,
	String routingBucket,
	String evidenceState,
	String evidenceSummary,
	String estimateMethodSummary,
	String localContextSummary,
	String summaryBlock,
	List<String> callDrivers
) {
}
