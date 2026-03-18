package com.example.sewerverdict.estimator;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.sewerverdict.content.GeoProfile;
import com.example.sewerverdict.content.GeoProfileService;

@Service
public class EstimatorService {

	private final CostProfileService costProfileService;
	private final DefectProfileService defectProfileService;
	private final MaterialProfileService materialProfileService;
	private final GeoProfileService geoProfileService;

	public EstimatorService(CostProfileService costProfileService, DefectProfileService defectProfileService,
		MaterialProfileService materialProfileService, GeoProfileService geoProfileService) {
		this.costProfileService = costProfileService;
		this.defectProfileService = defectProfileService;
		this.materialProfileService = materialProfileService;
		this.geoProfileService = geoProfileService;
	}

	public EstimatorResult evaluate(EstimatorForm form) {
		String role = value(form.getRole(), "owner");
		String ageBand = value(form.getHouseAgeBand(), "1970-1989");
		String issueState = value(form.getIssueState(), "no-scope-yet");
		String defectType = value(form.getDefectType(), "unknown");
		String accessType = value(form.getAccessType(), "unknown");
		String urgency = value(form.getUrgency(), "researching");
		LocationContext locationContext = resolveLocationContext(form.getLocation());
		DefectProfile defectProfile = defectProfileService.getProfile(defectType);
		MaterialProfile materialProfile = materialProfileService.getProfile(defectType);

		DecisionProfile decision = determineDecision(role, ageBand, issueState, defectType, accessType, urgency, defectProfile,
			materialProfile);
		List<CostBand> costBands = buildCostBands(decision, defectProfile, materialProfile, issueState, accessType, ageBand,
			locationContext);
		List<String> callDrivers = buildCallDrivers(decision, defectProfile, materialProfile, role, ageBand, issueState,
			defectType, accessType, urgency, locationContext);
		List<String> inputSummary = buildInputSummary(form, role, ageBand, issueState, defectType, accessType, urgency,
			locationContext);
		List<String> uncertaintyDrivers = buildUncertaintyDrivers(decision, defectProfile, materialProfile, accessType,
			defectType, ageBand, locationContext);
		List<String> questions = buildQuestions(decision, defectProfile, role, issueState, defectType);
		String interpretation = buildInterpretation(decision, defectProfile, materialProfile, role, ageBand, issueState,
			defectType, accessType, locationContext);
		String evidenceSummary = buildEvidenceSummary(issueState, defectProfile, materialProfile);
		String findingReadSummary = buildFindingReadSummary(defectProfile);
		String severityUpgradeSummary = buildSeverityUpgradeSummary(defectProfile);
		String severityDowngradeSummary = buildSeverityDowngradeSummary(defectProfile);
		String methodFitSummary = buildMethodFitSummary(defectProfile, materialProfile, accessType);
		String materialReadSummary = buildMaterialReadSummary(materialProfile);
		String estimateMethodSummary = buildEstimateMethodSummary(decision, defectProfile, materialProfile, issueState,
			accessType, locationContext);
		List<String> sourceIds = buildResultSourceIds(decision, defectProfile, materialProfile, accessType);
		String summaryBlock = buildSummary(form, decision, defectProfile, callDrivers, uncertaintyDrivers, locationContext);

		return new EstimatorResult(
			decision.riskTier(),
			decision.riskClass(),
			decision.evidenceState(),
			decision.routingBucket(),
			decision.routingRationale(),
			interpretation,
			evidenceSummary,
			findingReadSummary,
			severityUpgradeSummary,
			severityDowngradeSummary,
			methodFitSummary,
			materialReadSummary,
			estimateMethodSummary,
			sourceIds,
			locationContext.summary(),
			decision.likelyNextStep(),
			decision.primaryCtaLabel(),
			decision.primaryCtaHref(),
			decision.primaryServiceNeeded(),
			decision.secondaryCtaLabel(),
			decision.secondaryCtaHref(),
			decision.secondaryServiceNeeded(),
			callDrivers,
			inputSummary,
			costBands,
			uncertaintyDrivers,
			questions,
			summaryBlock
		);
	}

	private DecisionProfile determineDecision(String role, String ageBand, String issueState, String defectType,
		String accessType, String urgency, DefectProfile defectProfile, MaterialProfile materialProfile) {
		boolean oldHome = "pre-1950".equals(ageBand) || "1950-1969".equals(ageBand);
		boolean buyer = "buyer".equals(role);
		boolean underContract = buyer && "active-decision".equals(urgency);
		boolean urgentRepair = "urgent-repair".equals(urgency);
		boolean scopedIssue = "scope-found-issue".equals(issueState);
		boolean noScopeYet = "no-scope-yet".equals(issueState);
		boolean symptomsOnly = "symptoms-only".equals(issueState);
		boolean systemicDefect = defectProfile.isSystemic();
		boolean localizedDefect = defectProfile.isLocalized();
		boolean slabAccess = "slab".equals(accessType);

		if (scopedIssue && systemicDefect) {
			return new DecisionProfile(
				"Scope-confirmed major issue",
				"quote-ready",
				"High urgency. Quote-ready now.",
				"high",
				"The issue already looks serious enough to compare repair scope, restoration burden, and method fit now.",
				"Get 2-3 repair quotes and confirm whether spot repair, trenchless, or excavation is actually realistic.",
				"Get sewer repair or replacement quotes",
				"/get-sewer-quotes/",
				"replacement",
				"Find inspection options first",
				"/find-sewer-scope/",
				"inspection"
			);
		}

		if (scopedIssue && materialProfile != null && "cast-iron".equals(defectType) && (oldHome || slabAccess)) {
			return new DecisionProfile(
				"Scope-confirmed deterioration with expensive access",
				"quote-ready",
				"Confirmed issue. Compare repair paths.",
				"high",
				"Cast iron in an older house or under a slab usually needs repair-path comparison, not another generic opinion.",
				"Get 2-3 repair quotes and compare spot repair, rehab, and broader replacement assumptions.",
				"Get sewer repair or replacement quotes",
				"/get-sewer-quotes/",
				"repair",
				"Find inspection options first",
				"/find-sewer-scope/",
				"inspection"
			);
		}

		if (scopedIssue && buyer && localizedDefect) {
			return new DecisionProfile(
				"Scope-confirmed issue inside a live transaction",
				"quote-ready",
				"Confirmed issue. Compare repair paths.",
				"medium",
				"Because the line is already scoped, the buyer usually needs documentation and scope-specific pricing more than another broad warning.",
				"Document the finding, get specialist detail, and compare repair options before final negotiation.",
				"Get sewer repair or replacement quotes",
				"/get-sewer-quotes/",
				"repair",
				"Find inspection options first",
				"/find-sewer-scope/",
				"inspection"
			);
		}

		if (scopedIssue) {
			return new DecisionProfile(
				"Scoped issue that still needs method clarity",
				"quote-ready",
				"Confirmed issue. Compare repair paths.",
				"medium",
				"The line already has evidence behind it, so the next useful move is usually comparing repair scope and method fit.",
				"Get 2-3 repair quotes and compare targeted repair against broader replacement assumptions.",
				"Get sewer repair or replacement quotes",
				"/get-sewer-quotes/",
				"repair",
				"Find inspection options first",
				"/find-sewer-scope/",
				"inspection"
			);
		}

		if (urgentRepair && (systemicDefect || defectProfile.quoteBiased()) && (symptomsOnly || !noScopeYet)) {
			return new DecisionProfile(
				"Strong failure signals without a settled work plan",
				"quote-ready",
				"High urgency. Quote-ready now.",
				"high",
				"When symptoms are acute and the likely defect is a major material or collapse issue, quote-first input makes sense, but the final scope still depends on confirming the full run and access.",
				"Get contractor input now, then confirm the full run and method assumptions before you commit.",
				"Get sewer repair or replacement quotes",
				"/get-sewer-quotes/",
				"replacement",
				"Find inspection options first",
				"/find-sewer-scope/",
				"inspection"
			);
		}

		if (buyer && noScopeYet && (oldHome || underContract)) {
			return new DecisionProfile(
				"Transaction risk with incomplete evidence",
				"inspection-first",
				"Meaningful uncertainty. Transaction-sensitive.",
				"medium",
				"A buyer with an older home or an active contract usually benefits more from real footage first than from repair pricing built on assumptions.",
				"Book a sewer scope before you price repairs or negotiate around the line.",
				"Find sewer camera inspection options",
				"/find-sewer-scope/",
				"inspection",
				"See quote-first option",
				"/get-sewer-quotes/",
				"not-sure"
			);
		}

		if (symptomsOnly || noScopeYet) {
			boolean lowEvidence = "researching".equals(urgency) && "unknown".equals(defectType) && !buyer && !oldHome;
			return new DecisionProfile(
				lowEvidence ? "Low-evidence concern" : "Incomplete evidence",
				"inspection-first",
				lowEvidence ? "Low current evidence. Inspect only if the context justifies it." : "Meaningful uncertainty. Inspect before pricing.",
				lowEvidence ? "low" : "medium",
				lowEvidence
					? "There is not enough evidence yet to treat this like a repair project. Keep the next move light and evidence-first."
					: "Without a documented run, the honest next move is usually a sewer scope so pricing and urgency do not get overstated.",
				"Book a sewer scope before you price repairs or negotiate around the line.",
				"Find sewer camera inspection options",
				"/find-sewer-scope/",
				"inspection",
				"See quote-first option",
				"/get-sewer-quotes/",
				"not-sure"
			);
		}

		return new DecisionProfile(
			"Context needs more evidence",
			"inspection-first",
			"Meaningful uncertainty. Inspect before pricing.",
			"medium",
			"The next reliable move is still to reduce uncertainty before treating the situation like a settled repair job.",
			"Book a sewer scope before you price repairs or negotiate around the line.",
			"Find sewer camera inspection options",
			"/find-sewer-scope/",
			"inspection",
			"See quote-first option",
			"/get-sewer-quotes/",
			"not-sure"
		);
	}

	private boolean isSystemicDefect(String defectType) {
		return "orangeburg".equals(defectType) || "collapse".equals(defectType) || "cast-iron".equals(defectType);
	}

	private boolean isLocalizedDefect(String defectType) {
		return "roots".equals(defectType) || "belly".equals(defectType) || "offset-crack".equals(defectType);
	}

	private List<CostBand> buildCostBands(DecisionProfile decision, DefectProfile defectProfile, MaterialProfile materialProfile,
		String issueState, String accessType, String ageBand, LocationContext locationContext) {
		List<CostBand> bands = new ArrayList<>();
		boolean oldHome = "pre-1950".equals(ageBand) || "1950-1969".equals(ageBand);
		RangeFactors inspectionFactors = rangeFactors(accessType, oldHome, false, locationContext, false);
		RangeFactors repairFactors = rangeFactors(accessType, oldHome, true, locationContext, false);
		RangeFactors replacementFactors = rangeFactors(accessType, oldHome, true, locationContext, true);
		CostProfile inspection = costProfileService.getRequiredProfile("inspection-camera");
		CostProfile supportProfile = costProfileService.getRequiredProfile(defectProfile.inspectionSupportProfileId());
		CostProfile repairProfile = costProfileService.getRequiredProfile(selectRepairProfileId(defectProfile, accessType));
		CostProfile partialReplacementProfile = costProfileService.getRequiredProfile("partial-replacement-national");
		CostProfile replacementProfile = costProfileService.getRequiredProfile(defectProfile.replacementProfileId());
		CostProfile trenchlessProfile = defectProfile.trenchlessProfileId() == null
			? null
			: costProfileService.getRequiredProfile(defectProfile.trenchlessProfileId());

		if ("inspection-first".equals(decision.routingBucket())) {
			bands.add(new CostBand("Inspection only", formatRange(inspection, inspectionFactors, false),
				"Use this as the first spend when the line has not been camera-confirmed yet." + accessRangeNote(accessType, false)));
			if (!"inspection-camera".equals(defectProfile.inspectionSupportProfileId())) {
				bands.add(new CostBand(labelForSupportProfile(defectProfile.inspectionSupportProfileId()),
					formatRange(supportProfile, inspectionFactors, false),
					"Directional only until the footage shows whether this stays maintenance-heavy or turns into a structural project."
						+ lengthPressureNote(false)));
			}
			if ("no-scope-yet".equals(issueState) || "symptoms-only".equals(issueState)) {
				bands.add(new CostBand("If a repair path is later confirmed", formatRange(repairProfile, repairFactors, false),
					"Directional only until footage, access, and the actual failure pattern are documented."
						+ accessRangeNote(accessType, true)));
			}
			return bands;
		}

		bands.add(new CostBand(labelForRepairProfile(repairProfile.profileId()), formatRange(repairProfile, repairFactors, false),
			defectProfile.whyItGetsExpensive() + accessRangeNote(accessType, true)));
		if (trenchlessProfile != null) {
			bands.add(new CostBand(labelForTrenchlessProfile(trenchlessProfile.profileId()),
				formatRange(trenchlessProfile, repairFactors, isPerFootProfile(trenchlessProfile.profileId())),
				defectProfile.trenchlessRead() + trenchlessLengthScenario(trenchlessProfile, repairFactors, accessType)));
		}
		if (defectProfile.isSystemic()) {
			bands.add(new CostBand("Full replacement / excavation", formatRange(replacementProfile, replacementFactors, false),
				"Usually the upper-end path when access is bad, defects are systemic, or trenchless is not viable."
					+ replacementPressureNote(accessType, locationContext)));
			return bands;
		}
		if (!"no-scope-yet".equals(issueState)) {
			bands.add(new CostBand("Broader repair or partial replacement",
				formatRange(partialReplacementProfile, replacementFactors, false),
				"Comes into play when the defect is not limited to one easy section or nearby cleanup expands the job."
					+ lengthPressureNote(true)));
		}
		bands.add(new CostBand("Full replacement / excavation", formatRange(replacementProfile, replacementFactors, false),
			"Moves up with depth, hardscape, long runs, and restoration needs."
				+ replacementPressureNote(accessType, locationContext)));
		return bands;
	}

	private List<String> buildCallDrivers(DecisionProfile decision, DefectProfile defectProfile, MaterialProfile materialProfile,
		String role, String ageBand, String issueState, String defectType, String accessType, String urgency,
		LocationContext locationContext) {
		List<String> drivers = new ArrayList<>();
		if ("buyer".equals(role) && "active-decision".equals(urgency)) {
			drivers.add("Buyer under contract raises the value of clean footage before negotiation or credits.");
		}
		else if ("seller".equals(role)) {
			drivers.add("Seller context favors a route that reduces negotiation drag without overstating the problem.");
		}
		else {
			drivers.add("Owner context shifts the call toward triage: watch item, inspection-first, or quote-ready.");
		}

		if ("scope-found-issue".equals(issueState)) {
			drivers.add("You entered a completed scope with a finding, so the route can move beyond generic caution.");
		}
		else if ("symptoms-only".equals(issueState)) {
			drivers.add("Symptoms can justify urgency, but they still do not confirm the exact failure pattern without footage.");
		}
		else {
			drivers.add("No confirmed scope footage keeps the honest next move closer to evidence gathering than fixed repair pricing.");
		}

		if (locationContext.matched()) {
			drivers.add("The location matched a stored " + locationContext.label()
				+ " market profile, so the call is anchored to real local housing and sewer context instead of a generic national read.");
		}
		if ("pre-1950".equals(ageBand) || "1950-1969".equals(ageBand)) {
			drivers.add("Older-home sewer materials widen both buried-line uncertainty and cost spread.");
		}
		if (defectProfile.isSystemic()) {
			drivers.add("The reported concern points toward a broader material or failure pattern, which is why replacement logic stays on the table.");
		}
		else if (defectProfile.isLocalized()) {
			drivers.add("The reported concern could still be localized, so targeted repair remains plausible if the rest of the run looks serviceable.");
		}
		if ("slab".equals(accessType)) {
			drivers.add("Under-slab access raises restoration and method risk even before a contractor prices the job.");
		}
		if (materialProfile != null) {
			drivers.add(materialProfile.commonRiskNarrative());
		}
		return drivers.stream().limit(5).toList();
	}

	private List<String> buildInputSummary(EstimatorForm form, String role, String ageBand, String issueState,
		String defectType, String accessType, String urgency, LocationContext locationContext) {
		List<String> summary = new ArrayList<>();
		summary.add(displayRole(role));
		if (locationContext.matched()) {
			summary.add(locationContext.label());
		}
		else if (StringUtils.hasText(form.getLocation())) {
			summary.add(form.getLocation().trim());
		}
		summary.add(displayAgeBand(ageBand));
		summary.add(displayIssueState(issueState));
		summary.add(displayDefectType(defectType));
		summary.add(displayAccessType(accessType));
		summary.add(displayUrgency(urgency));
		return summary;
	}

	private List<String> buildUncertaintyDrivers(DecisionProfile decision, DefectProfile defectProfile,
		MaterialProfile materialProfile, String accessType, String defectType, String ageBand, LocationContext locationContext) {
		List<String> drivers = new ArrayList<>();
		drivers.add("Actual line length and depth");
		if ("inspection-first".equals(decision.routingBucket())) {
			drivers.add("Whether the full run has been camera-confirmed yet");
		}
		else {
			drivers.add("How much of the documented issue is isolated versus systemic");
		}
		if ("slab".equals(accessType)) {
			drivers.add("Under-slab access and restoration scope");
		}
		if (materialProfile != null) {
			drivers.add("Material condition across the rest of the run");
		}
		if ("pre-1950".equals(ageBand) || "1950-1969".equals(ageBand)) {
			drivers.add("Older material assumptions versus actual field condition");
		}
		if (locationContext.matched()) {
			drivers.add(locationContext.label() + " contractor pricing and restoration conditions");
		}
		else {
			drivers.add("Local labor, permit, and restoration pricing");
		}
		drivers.add("Trenchless suitability versus dig-up requirements");
		return drivers;
	}

	private List<String> buildQuestions(DecisionProfile decision, DefectProfile defectProfile, String role, String issueState,
		String defectType) {
		List<String> questions = new ArrayList<>();
		if ("inspection-first".equals(decision.routingBucket()) && "buyer".equals(role)) {
			questions.add("Would a scope materially change the deal, the credit request, or your comfort before closing?");
			questions.add("If a problem is found, do you need a credit request, a repair quote, or more specialist detail?");
			questions.add("What part of the buried-line risk is still unknown without camera footage?");
			return questions;
		}
		if ("inspection-first".equals(decision.routingBucket()) && "seller".equals(role)) {
			questions.add("Would evidence first reduce negotiation drag more than a fast quote with weak assumptions?");
			questions.add("If a defect is confirmed, would buyers trust a scope, a quote, or both?");
			questions.add("What would make this issue look systemic instead of isolated?");
			return questions;
		}
		if ("inspection-first".equals(decision.routingBucket())) {
			questions.add("What evidence would confirm whether this is structural or just a maintenance issue?");
			questions.add("Does the situation justify a scope first, or is quote-ready evidence already strong enough?");
			questions.add("What would make this more urgent over the next 30-90 days?");
			return questions;
		}
		if ("buyer".equals(role)) {
			questions.add("Can you show the exact footage, severity, and whether the defect is isolated or systemic?");
			questions.add("Would a repair quote strengthen negotiation more than another vague warning?");
			questions.add("If repair is needed, what documentation would support credits or scope changes cleanly?");
			return questions;
		}
		if ("scope-found-issue".equals(issueState) || defectProfile.isSystemic()) {
			questions.add("Is this likely a localized fix or evidence of broader line deterioration?");
			questions.add("Is trenchless genuinely viable here, or does access force excavation?");
			questions.add("What would change the quote most: depth, access, line length, or restoration?");
			return questions;
		}
		questions.add("What part of the quote assumes repair versus replacement?");
		questions.add("What restoration, cleanup, and permit work is included or excluded?");
		questions.add("What would have to be true for this to become a full-line project?");
		return questions;
	}

	private String buildInterpretation(DecisionProfile decision, DefectProfile defectProfile, MaterialProfile materialProfile,
		String role, String ageBand, String issueState, String defectType, String accessType, LocationContext locationContext) {
		String rolePhrase = switch (role) {
			case "buyer" -> "As a buyer, the main job is protecting the transaction timeline without overreacting.";
			case "seller" -> "As a seller, the main job is clarifying the issue before it becomes negotiation drag.";
			default -> "As an owner, the main job is separating watch-items from quote-now situations.";
		};
		String concernPhrase = "unknown".equals(defectType)
			? "an unconfirmed buried-line concern"
			: "a " + displayDefectType(defectType).toLowerCase() + " concern";
		String routePhrase = "inspection-first".equals(decision.routingBucket())
			? "this points more toward an evidence-first inspection decision than a settled repair scope."
			: "this points more toward a quote-comparison decision than a watch-and-wait posture.";
		String accessPhrase = switch (accessType) {
			case "slab" -> " Under-slab access raises both cost and method uncertainty.";
			case "basement-crawlspace" -> " Basement or crawlspace access can keep some repairs more inspectable.";
			default -> "";
		};
		String locationPhrase = locationContext.matched()
			? " A stored local profile for " + locationContext.label() + " also keeps the call grounded in a real market context."
			: "";
		String defectPhrase = "unknown".equals(defectType) ? "" : " " + defectProfile.whatItOftenMeans();
		String materialPhrase = materialProfile == null ? "" : " " + materialProfile.cautionNotes();
		return "%s With a %s, a %s, and %s, %s%s%s".formatted(
			rolePhrase,
			displayAgeBand(ageBand).toLowerCase(),
			displayIssueState(issueState).toLowerCase(),
			concernPhrase,
			routePhrase,
			defectPhrase + accessPhrase + materialPhrase,
			locationPhrase
		);
	}

	private String buildEvidenceSummary(String issueState, DefectProfile defectProfile, MaterialProfile materialProfile) {
		if ("scope-found-issue".equals(issueState)) {
			return "This result treats the problem as documented because you entered a completed scope with a finding.";
		}
		if ("symptoms-only".equals(issueState) && defectProfile.isSystemic()) {
			return "This result treats the defect label as a serious user-reported concern, not a confirmed diagnosis. Footage could still upgrade or soften the call.";
		}
		if ("symptoms-only".equals(issueState)) {
			return "This result assumes symptoms are real but still under-documented. The exact failure pattern remains unconfirmed.";
		}
		return "This result assumes the line has not been camera-confirmed yet, so the call stays wide and evidence-aware.";
	}

	private String buildFindingReadSummary(DefectProfile defectProfile) {
		return defectProfile.label() + ": " + defectProfile.whatItOftenMeans();
	}

	private String buildSeverityUpgradeSummary(DefectProfile defectProfile) {
		return defectProfile.upgradeSignal();
	}

	private String buildSeverityDowngradeSummary(DefectProfile defectProfile) {
		return defectProfile.downgradeSignal();
	}

	private String buildMethodFitSummary(DefectProfile defectProfile, MaterialProfile materialProfile, String accessType) {
		String accessNote = switch (accessType) {
			case "slab" -> " Under-slab access keeps excavation, staging, and restoration more plausible even when trenchless is still worth checking.";
			case "basement-crawlspace" -> " Basement or crawlspace access can keep some verification and localized repair work simpler than slab access.";
			default -> "";
		};
		String materialNote = materialProfile == null ? "" : " " + materialProfile.trenchlessNotes();
		return defectProfile.trenchlessRead() + accessNote + materialNote;
	}

	private String buildMaterialReadSummary(MaterialProfile materialProfile) {
		if (materialProfile == null) {
			return "";
		}
		return materialProfile.commonRiskNarrative() + " " + materialProfile.cautionNotes();
	}

	private String buildEstimateMethodSummary(DecisionProfile decision, DefectProfile defectProfile, MaterialProfile materialProfile,
		String issueState, String accessType, LocationContext locationContext) {
		String base = "Cost direction uses wide national ranges anchored to current market sanity checks, then narrows the route using your role, issue state, urgency, and defect context.";
		String accessNote = "slab".equals(accessType)
			? " Under-slab access keeps the upper range wider because restoration and staging get harder."
			: " Replacement and trenchless headlines move quickly once line length and restoration scope are clearer.";
		String locationNote = locationContext.matched()
			? " " + locationContext.summary()
			: " The location is still used as a market anchor, but SewerClarity does not invent a city-specific contractor rate.";
		if ("inspection-first".equals(decision.routingBucket())) {
			return base + " It does not assume a repair scope until footage, access, and severity are better documented."
				+ accessNote + locationNote;
		}
		if (defectProfile.isSystemic() && !"scope-found-issue".equals(issueState)) {
			return base + " Because the issue is not fully documented yet, the quote-ready route should still be verified against real footage or contractor review."
				+ accessNote + locationNote;
		}
		String materialNote = materialProfile == null ? "" : " " + materialProfile.trenchlessNotes();
		return base + " It still does not know line length, depth, local labor rates, or restoration scope."
			+ accessNote + materialNote + locationNote;
	}

	private List<String> buildResultSourceIds(DecisionProfile decision, DefectProfile defectProfile,
		MaterialProfile materialProfile, String accessType) {
		LinkedHashSet<String> sourceIds = new LinkedHashSet<>();
		addSourceIds(sourceIds, defectProfile.sourceRefs());
		if (materialProfile != null) {
			addSourceIds(sourceIds, materialProfile.sourceRefs());
		}
		addSourceIds(sourceIds, costProfileService.getRequiredProfile("inspection-camera").sourceRefs());
		addSourceIds(sourceIds, costProfileService.getRequiredProfile(defectProfile.inspectionSupportProfileId()).sourceRefs());
		addSourceIds(sourceIds,
			costProfileService.getRequiredProfile(selectRepairProfileId(defectProfile, accessType)).sourceRefs());
		addSourceIds(sourceIds, costProfileService.getRequiredProfile(defectProfile.replacementProfileId()).sourceRefs());
		if (!"inspection-first".equals(decision.routingBucket()) && defectProfile.trenchlessProfileId() != null) {
			addSourceIds(sourceIds, costProfileService.getRequiredProfile(defectProfile.trenchlessProfileId()).sourceRefs());
		}
		return sourceIds.stream().toList();
	}

	private String buildSummary(EstimatorForm form, DecisionProfile decision, DefectProfile defectProfile, List<String> callDrivers,
		List<String> uncertaintyDrivers, LocationContext locationContext) {
		String role = displayRole(value(form.getRole(), "owner"));
		String location = locationContext.matched()
			? locationContext.label()
			: value(form.getLocation(), "Location not provided");
		String issueState = displayIssueState(value(form.getIssueState(), "no-scope-yet"));
		String defectType = value(form.getDefectType(), "unknown");
		String concernLine = "unknown".equals(defectType)
			? "Known concern: Nothing confirmed yet beyond the symptoms or context entered."
			: "Known concern: " + displayDefectType(defectType) + ". " + defectProfile.whatItOftenMeans();
		String costPosture = "inspection-first".equals(decision.routingBucket())
			? "Cost posture: keep repair pricing broad until footage confirms severity, access, and whether the issue is isolated."
			: "Cost posture: compare targeted repair, broader replacement, and restoration scope before you commit to one method.";
		return """
			SewerClarity decision memo
			Who this is for: %s
			Location: %s
			Current situation: %s
			Current read: %s. %s
			%s
			Local context: %s
			Best next move: %s
			Why this route: %s
			%s
			Biggest unknowns: %s
			""".formatted(
			role,
			location,
			issueState,
			decision.evidenceState(),
			decision.riskTier(),
			concernLine,
			locationContext.summary(),
			decision.likelyNextStep(),
			String.join(" ", callDrivers),
			costPosture,
			String.join("; ", uncertaintyDrivers)
		).trim();
	}

	private LocationContext resolveLocationContext(String location) {
		String normalizedLocation = trimToNull(location);
		if (normalizedLocation == null) {
			return new LocationContext("Unspecified market",
				"No market anchor was added, so this stays on national ranges and generic uncertainty only.", false, 1.0);
		}
		GeoProfile profile = geoProfileService.findProfileForLocation(normalizedLocation);
		if (profile == null) {
			return new LocationContext(normalizedLocation,
				"Location is used as a market anchor, but SewerClarity does not yet have a stored local profile for it. Exact pricing still depends on local labor, permits, and restoration.",
				false,
				1.0);
		}
		String label = profile.getCityName() + ", " + profile.getStateCode();
		double upperRangeFactor = "tier-1".equalsIgnoreCase(profile.getPriorityTier()) ? 1.06 : 1.04;
		String localSignal = firstSentence(value(profile.getHousingAgeSignal(), profile.getMarketReason()));
		return new LocationContext(label,
			"Matched " + label + ". " + localSignal
				+ " This does not create a local quote, but it keeps the upper range and routing logic grounded in a real market profile.",
			true,
			upperRangeFactor);
	}

	private RangeFactors rangeFactors(String accessType, boolean oldHome, boolean repairScope, LocationContext locationContext,
		boolean replacementScope) {
		double lowFactor = 1.0;
		double highFactor = 1.0;
		if ("slab".equals(accessType)) {
			if (repairScope) {
				lowFactor += 0.05;
			}
			highFactor += replacementScope ? 0.20 : 0.12;
		}
		if (oldHome && repairScope) {
			highFactor += 0.06;
		}
		if (locationContext.matched() && repairScope) {
			highFactor *= locationContext.upperRangeFactor();
		}
		return new RangeFactors(lowFactor, highFactor);
	}

	private String accessRangeNote(String accessType, boolean repairScope) {
		if (!repairScope) {
			return "";
		}
		if ("slab".equals(accessType)) {
			return " Under-slab access shifts both the lower and upper edge upward because cleanup and restoration can stop being minor.";
		}
		if ("basement-crawlspace".equals(accessType)) {
			return " Basement or crawlspace access can keep some repair setups simpler than slab access.";
		}
		return " Access uncertainty alone can move this band more than most users expect.";
	}

	private String trenchlessLengthScenario(CostProfile profile, RangeFactors factors, String accessType) {
		int lowFeet = "slab".equals(accessType) ? 40 : 30;
		int highFeet = "slab".equals(accessType) ? 70 : 60;
		int lowScenario = adjustedAmount(profile.nationalRangeLow() * lowFeet, factors.lowFactor());
		int highScenario = adjustedAmount(profile.nationalRangeHigh() * highFeet, factors.highFactor());
		return " At roughly %d-%d ft of viable run, that headline can imply about $%,d-$%,d before pits and restoration."
			.formatted(lowFeet, highFeet, lowScenario, highScenario);
	}

	private String lengthPressureNote(boolean repairScope) {
		return repairScope
			? " Once the affected run gets longer than a short isolated section, the band can move quickly."
			: " Long runs, repeat visits, and cleanup are what usually move this beyond the first-pass number.";
	}

	private String replacementPressureNote(String accessType, LocationContext locationContext) {
		String accessNote = "slab".equals(accessType)
			? " Slab access is one of the cleaner reasons the upper end stays wide."
			: " Long exterior runs and restoration scope are usually what push the upper end fast.";
		String localNote = locationContext.matched()
			? " " + locationContext.label() + " is also being treated as a real market anchor rather than a generic national placeholder."
			: "";
		return accessNote + localNote;
	}

	private String formatRange(CostProfile profile, RangeFactors factors, boolean perFoot) {
		return formatRange(profile.nationalRangeLow(), profile.nationalRangeHigh(), factors, perFoot);
	}

	private String formatRange(int low, int high, RangeFactors factors, boolean perFoot) {
		int adjustedLow = adjustedAmount(low, factors.lowFactor());
		int adjustedHigh = adjustedAmount(high, factors.highFactor());
		String suffix = perFoot ? " / linear ft" : "";
		return "$%,d-$%,d%s".formatted(adjustedLow, adjustedHigh, suffix);
	}

	private String selectRepairProfileId(DefectProfile defectProfile, String accessType) {
		if ("slab".equals(accessType) && "cast-iron".equals(defectProfile.defectType())) {
			return "under-slab-repair-national";
		}
		return defectProfile.repairProfileId();
	}

	private String labelForSupportProfile(String profileId) {
		return switch (profileId) {
			case "root-treatment-cleaning" -> "If roots stay in the cleaning / treatment lane";
			case "inspection-camera" -> "Inspection-first budget";
			default -> "Supportive first-step budget";
		};
	}

	private String labelForRepairProfile(String profileId) {
		return switch (profileId) {
			case "under-slab-repair-national" -> "Under-slab repair / short-run replacement";
			case "partial-replacement-national" -> "Partial replacement / longer damaged section";
			case "spot-repair-national" -> "Targeted repair / localized fix";
			default -> "Repair path";
		};
	}

	private String labelForTrenchlessProfile(String profileId) {
		return switch (profileId) {
			case "lining-cipp-national" -> "Trenchless lining candidate";
			case "pipe-bursting-national" -> "Pipe bursting candidate";
			case "trenchless-national" -> "Trenchless candidate";
			default -> "Trenchless method candidate";
		};
	}

	private boolean isPerFootProfile(String profileId) {
		return "trenchless-national".equals(profileId)
			|| "lining-cipp-national".equals(profileId)
			|| "pipe-bursting-national".equals(profileId);
	}

	private void addSourceIds(LinkedHashSet<String> collector, List<String> sourceIds) {
		if (sourceIds == null) {
			return;
		}
		collector.addAll(sourceIds);
	}

	private int adjustedAmount(int value, double factor) {
		int adjusted = (int) Math.round(value * factor);
		int rounding = adjusted < 1000 ? 25 : adjusted < 5000 ? 50 : 250;
		return Math.max(rounding, (int) Math.round((double) adjusted / rounding) * rounding);
	}

	private String value(String candidate, String fallback) {
		return StringUtils.hasText(candidate) ? candidate : fallback;
	}

	private String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}

	private String firstSentence(String value) {
		if (!StringUtils.hasText(value)) {
			return "";
		}
		String trimmed = value.trim();
		int stop = trimmed.indexOf('.');
		return stop >= 0 ? trimmed.substring(0, stop + 1) : trimmed;
	}

	private String displayRole(String role) {
		return switch (role) {
			case "buyer" -> "Buyer";
			case "seller" -> "Seller";
			default -> "Owner";
		};
	}

	private String displayAgeBand(String ageBand) {
		return switch (ageBand) {
			case "pre-1950" -> "Pre-1950 home";
			case "1950-1969" -> "1950-1969 home";
			case "1990+" -> "1990+ home";
			default -> "1970-1989 home";
		};
	}

	private String displayIssueState(String issueState) {
		return switch (issueState) {
			case "scope-found-issue" -> "Scope done, issue found";
			case "symptoms-only" -> "Symptoms only";
			default -> "No scope yet";
		};
	}

	private String displayDefectType(String defectType) {
		return switch (defectType) {
			case "roots" -> "Roots";
			case "cast-iron" -> "Cast iron deterioration";
			case "orangeburg" -> "Orangeburg";
			case "belly" -> "Belly";
			case "offset-crack" -> "Offset / crack";
			case "collapse" -> "Collapse";
			default -> "Unknown defect";
		};
	}

	private String displayAccessType(String accessType) {
		return switch (accessType) {
			case "slab" -> "Slab access";
			case "basement-crawlspace" -> "Basement / crawlspace access";
			default -> "Access not confirmed";
		};
	}

	private String displayUrgency(String urgency) {
		return switch (urgency) {
			case "active-decision" -> "Under contract / active decision";
			case "urgent-repair" -> "Urgent repair needed";
			default -> "Just researching";
		};
	}

	private record DecisionProfile(
		String evidenceState,
		String routingBucket,
		String riskTier,
		String riskClass,
		String routingRationale,
		String likelyNextStep,
		String primaryCtaLabel,
		String primaryCtaHref,
		String primaryServiceNeeded,
		String secondaryCtaLabel,
		String secondaryCtaHref,
		String secondaryServiceNeeded
	) {
	}

	private record LocationContext(String label, String summary, boolean matched, double upperRangeFactor) {
	}

	private record RangeFactors(double lowFactor, double highFactor) {
	}
}
