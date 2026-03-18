package com.example.sewerverdict.estimator;

import java.util.List;

public record CostProfile(
	String profileId,
	String category,
	int nationalRangeLow,
	int nationalRangeHigh,
	List<String> accessModifiers,
	List<String> materialModifiers,
	List<String> severityModifiers,
	String notes,
	List<String> sourceRefs,
	String lastVerified
) {
}
