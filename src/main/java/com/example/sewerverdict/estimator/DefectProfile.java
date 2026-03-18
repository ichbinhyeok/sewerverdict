package com.example.sewerverdict.estimator;

import java.util.List;

public record DefectProfile(
	String defectType,
	String label,
	String issuePattern,
	String defaultRoutingBias,
	String whatItOftenMeans,
	String whyItGetsExpensive,
	String upgradeSignal,
	String downgradeSignal,
	String trenchlessRead,
	String inspectionSupportProfileId,
	String repairProfileId,
	String replacementProfileId,
	String trenchlessProfileId,
	List<String> sourceRefs,
	String lastVerified
) {

	public boolean isSystemic() {
		return issuePattern != null && issuePattern.toLowerCase().contains("systemic");
	}

	public boolean isLocalized() {
		return "localized".equalsIgnoreCase(issuePattern);
	}

	public boolean isMaterialDriven() {
		return issuePattern != null && issuePattern.toLowerCase().contains("material");
	}

	public boolean quoteBiased() {
		return "quote-ready".equalsIgnoreCase(defaultRoutingBias);
	}
}
