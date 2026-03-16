package com.example.sewerverdict.estimator;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EstimatorService {

	public EstimatorResult evaluate(EstimatorForm form) {
		String role = value(form.getRole(), "owner");
		String ageBand = value(form.getHouseAgeBand(), "1970-1989");
		String issueState = value(form.getIssueState(), "no-scope-yet");
		String defectType = value(form.getDefectType(), "unknown");
		String accessType = value(form.getAccessType(), "unknown");
		String urgency = value(form.getUrgency(), "researching");

		DecisionProfile decision = determineDecision(role, ageBand, issueState, defectType, accessType, urgency);
		List<CostBand> costBands = buildCostBands(decision, defectType, issueState);
		List<String> uncertaintyDrivers = buildUncertaintyDrivers(decision, accessType, defectType, ageBand);
		List<String> questions = buildQuestions(decision, role, issueState, defectType);
		String interpretation = buildInterpretation(decision, role, ageBand, issueState, defectType, accessType);
		String summaryBlock = buildSummary(form, decision, uncertaintyDrivers);

		return new EstimatorResult(
			decision.riskTier(),
			decision.riskClass(),
			decision.evidenceState(),
			decision.routingBucket(),
			decision.routingRationale(),
			interpretation,
			decision.likelyNextStep(),
			decision.primaryCtaLabel(),
			decision.primaryCtaHref(),
			decision.primaryServiceNeeded(),
			decision.secondaryCtaLabel(),
			decision.secondaryCtaHref(),
			decision.secondaryServiceNeeded(),
			costBands,
			uncertaintyDrivers,
			questions,
			summaryBlock
		);
	}

	private DecisionProfile determineDecision(String role, String ageBand, String issueState, String defectType,
		String accessType, String urgency) {
		boolean oldHome = "pre-1950".equals(ageBand) || "1950-1969".equals(ageBand);
		boolean buyer = "buyer".equals(role);
		boolean seller = "seller".equals(role);
		boolean underContract = buyer && "active-decision".equals(urgency);
		boolean urgentRepair = "urgent-repair".equals(urgency);
		boolean scopedIssue = "scope-found-issue".equals(issueState);
		boolean noScopeYet = "no-scope-yet".equals(issueState);
		boolean symptomsOnly = "symptoms-only".equals(issueState);
		boolean systemicDefect = isSystemicDefect(defectType);
		boolean localizedDefect = isLocalizedDefect(defectType);
		boolean slabAccess = "slab".equals(accessType);

		if ((scopedIssue && systemicDefect) || (urgentRepair && systemicDefect && !noScopeYet)) {
			return new DecisionProfile(
				"Scope-confirmed major issue",
				"quote-ready",
				"High urgency / quote-ready now",
				"high",
				"The issue already looks serious enough to compare repair methods and full-project scope now.",
				"Get 2-3 repair quotes and compare trenchless viability against excavation.",
				"Get sewer repair or replacement quotes",
				"/get-sewer-quotes/",
				"replacement",
				"Find inspection options first",
				"/find-sewer-scope/",
				"inspection"
			);
		}

		if (scopedIssue && "cast-iron".equals(defectType) && (oldHome || slabAccess)) {
			return new DecisionProfile(
				"Scope-confirmed deterioration with expensive access",
				"quote-ready",
				"Confirmed issue / compare repair paths",
				"high",
				"Cast iron plus older-house or slab context usually needs quote comparison, not just another generic opinion.",
				"Get 2-3 repair quotes and compare spot repair, trenchless rehab, and broader replacement logic.",
				"Get sewer repair or replacement quotes",
				"/get-sewer-quotes/",
				"replacement",
				"Find inspection options first",
				"/find-sewer-scope/",
				"inspection"
			);
		}

		if (scopedIssue && buyer && localizedDefect) {
			return new DecisionProfile(
				"Scope-confirmed issue inside a live transaction",
				"quote-ready",
				"Confirmed issue / compare repair paths",
				"medium",
				"Because the line is already scoped, the buyer usually needs quote-ready documentation and negotiation support more than another broad warning.",
				"Document the finding, ask for specialist detail, and compare repair paths before final negotiation.",
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
				"Confirmed issue / compare repair paths",
				"medium",
				"The line already has evidence behind it, so the next useful move is usually comparing repair scope and method fit.",
				"Get 2-3 repair quotes and compare spot repair versus broader replacement logic.",
				"Get sewer repair or replacement quotes",
				"/get-sewer-quotes/",
				"repair",
				"Find inspection options first",
				"/find-sewer-scope/",
				"inspection"
			);
		}

		if (urgentRepair && systemicDefect) {
			return new DecisionProfile(
				"Strong failure signals without a settled work plan",
				"quote-ready",
				"High urgency / quote-ready now",
				"high",
				"When symptoms are acute and the likely defect is a major material or collapse issue, delaying every quote until perfect certainty can waste time.",
				"Get quote-ready contractor input now, then confirm the full run and method assumptions as part of that process.",
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
				"Meaningful uncertainty / transaction-sensitive",
				"medium",
				"A buyer with an older home or an active contract usually benefits more from evidence first than from jumping straight to repair pricing.",
				"Order a sewer scope before making a repair or negotiation call.",
				"Find sewer camera inspection options",
				"/find-sewer-scope/",
				"inspection",
				"Review repair quote path",
				"/get-sewer-quotes/",
				"replacement"
			);
		}

		if (symptomsOnly || noScopeYet) {
			boolean lowEvidence = !oldHome && "researching".equals(urgency) && "unknown".equals(defectType) && !buyer && !seller;
			return new DecisionProfile(
				lowEvidence ? "Low-evidence research stage" : "Incomplete evidence",
				"inspection-first",
				lowEvidence ? "Low current evidence / inspect if the context justifies it" : "Meaningful uncertainty / inspect before pricing",
				lowEvidence ? "low" : "medium",
				lowEvidence
					? "There is not enough evidence yet to treat this like a repair project. Keep the next move light and evidence-first."
					: "Without a documented run, the honest next move is usually inspection-first so pricing and urgency do not get overstated.",
				"Order a sewer scope before making a repair or negotiation call.",
				"Find sewer camera inspection options",
				"/find-sewer-scope/",
				"inspection",
				"Review repair quote path",
				"/get-sewer-quotes/",
				"replacement"
			);
		}

		return new DecisionProfile(
			"Context needs more evidence",
			"inspection-first",
			"Meaningful uncertainty / inspect before pricing",
			"medium",
			"The next reliable move is still to reduce uncertainty before treating the situation like a settled repair job.",
			"Order a sewer scope before making a repair or negotiation call.",
			"Find sewer camera inspection options",
			"/find-sewer-scope/",
			"inspection",
			"Review repair quote path",
			"/get-sewer-quotes/",
			"replacement"
		);
	}

	private boolean isSystemicDefect(String defectType) {
		return "orangeburg".equals(defectType) || "collapse".equals(defectType) || "cast-iron".equals(defectType);
	}

	private boolean isLocalizedDefect(String defectType) {
		return "roots".equals(defectType) || "belly".equals(defectType) || "offset-crack".equals(defectType);
	}

	private List<CostBand> buildCostBands(DecisionProfile decision, String defectType, String issueState) {
		List<CostBand> bands = new ArrayList<>();
		if ("inspection-first".equals(decision.routingBucket())) {
			bands.add(new CostBand("Inspection only", "$250-$650", "Useful when the line has not been scoped clearly enough yet."));
			bands.add(new CostBand("Cleaning / root treatment", "$300-$1,200",
				"Often the low end if the eventual issue is maintenance-heavy instead of structural."));
			if ("no-scope-yet".equals(issueState) || "symptoms-only".equals(issueState)) {
				bands.add(new CostBand("If repair is later confirmed", "$1,500-$6,500",
					"Directional only until the footage, access, and actual failure pattern are documented."));
			}
			return bands;
		}

		bands.add(new CostBand("Targeted repair", "$1,500-$6,500",
			"More plausible when the problem is isolated and the rest of the run still looks serviceable."));
		if (isSystemicDefect(defectType)) {
			bands.add(new CostBand("Trenchless rehab / bursting", "$5,000-$18,000",
				"Only viable when layout, access, and line condition cooperate."));
			bands.add(new CostBand("Full replacement / excavation", "$7,000-$25,000+",
				"Usually the upper-end path when access is bad, defects are systemic, or trenchless is not viable."));
			return bands;
		}
		if (!"no-scope-yet".equals(issueState)) {
			bands.add(new CostBand("Broader repair or partial replacement", "$4,000-$12,000",
				"Comes into play when the defect is not limited to one easy section."));
		}
		bands.add(new CostBand("Full replacement / excavation", "$7,000-$20,000+",
			"Moves up with depth, hardscape, long runs, and restoration needs."));
		return bands;
	}

	private List<String> buildUncertaintyDrivers(DecisionProfile decision, String accessType, String defectType,
		String ageBand) {
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
		if ("orangeburg".equals(defectType) || "cast-iron".equals(defectType)) {
			drivers.add("Material condition across the rest of the run");
		}
		if ("pre-1950".equals(ageBand) || "1950-1969".equals(ageBand)) {
			drivers.add("Older material assumptions versus actual field condition");
		}
		drivers.add("Trenchless suitability versus dig-up requirements");
		return drivers;
	}

	private List<String> buildQuestions(DecisionProfile decision, String role, String issueState, String defectType) {
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
		if ("scope-found-issue".equals(issueState) || isSystemicDefect(defectType)) {
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

	private String buildInterpretation(DecisionProfile decision, String role, String ageBand, String issueState,
		String defectType, String accessType) {
		String rolePhrase = switch (role) {
			case "buyer" -> "As a buyer, the main job is protecting the transaction timeline without overreacting.";
			case "seller" -> "As a seller, the main job is clarifying the issue before it becomes negotiation drag.";
			default -> "As an owner, the main job is separating watch-items from quote-now situations.";
		};
		String accessPhrase = "slab".equals(accessType)
			? " Under-slab access raises both cost and method uncertainty."
			: "basement-crawlspace".equals(accessType)
				? " Basement or crawlspace access can keep some repairs more inspectable."
				: "";
		return "%s A %s home with a %s state and %s concern usually lands in the %s lane.%s".formatted(
			rolePhrase,
			prettyLabel(ageBand),
			prettyLabel(issueState),
			prettyLabel(defectType),
			decision.routingBucket().replace("-", " "),
			accessPhrase
		);
	}

	private String buildSummary(EstimatorForm form, DecisionProfile decision, List<String> uncertaintyDrivers) {
		return """
			SewerVerdict next-step summary
			Location: %s
			Role: %s
			Current situation: %s
			Likely issue: %s
			Evidence state: %s
			Risk tier: %s
			Most likely next step: %s
			Key uncertainty: %s
			""".formatted(
			value(form.getLocation(), "Not provided"),
			prettyLabel(value(form.getRole(), "owner")),
			prettyLabel(value(form.getIssueState(), "no-scope-yet")),
			prettyLabel(value(form.getDefectType(), "unknown")),
			decision.evidenceState(),
			decision.riskTier(),
			decision.likelyNextStep(),
			String.join("; ", uncertaintyDrivers)
		).trim();
	}

	private String value(String candidate, String fallback) {
		return StringUtils.hasText(candidate) ? candidate : fallback;
	}

	private String prettyLabel(String value) {
		return value
			.replace("-", " ")
			.replace("scope found issue", "scope done, issue found")
			.replace("no scope yet", "no scope yet")
			.replace("active decision", "under contract / active decision")
			.replace("urgent repair", "urgent repair needed")
			.replace("cast iron", "cast iron deterioration")
			.replace("offset crack", "offset / crack")
			.replace("basement crawlspace", "basement / crawlspace");
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
}
