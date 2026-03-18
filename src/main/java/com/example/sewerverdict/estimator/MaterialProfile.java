package com.example.sewerverdict.estimator;

import java.util.List;

public record MaterialProfile(
	String material,
	String commonRiskNarrative,
	List<String> commonIssues,
	List<String> typicalAgeAssociations,
	String cautionNotes,
	String trenchlessNotes,
	List<String> sourceRefs,
	String lastVerified
) {
}
