package com.example.sewerverdict.estimator;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EstimatorService {

	public EstimatorResult evaluate(EstimatorForm form) {
		int seriousnessScore = 0;
		int uncertaintyScore = 0;

		String role = value(form.getRole(), "owner");
		String ageBand = value(form.getHouseAgeBand(), "1970-1989");
		String issueState = value(form.getIssueState(), "no-scope-yet");
		String defectType = value(form.getDefectType(), "unknown");
		String accessType = value(form.getAccessType(), "unknown");
		String urgency = value(form.getUrgency(), "researching");

		switch (ageBand) {
			case "pre-1950" -> {
				seriousnessScore += 2;
				uncertaintyScore += 2;
			}
			case "1950-1969" -> {
				seriousnessScore += 1;
				uncertaintyScore += 2;
			}
			case "1970-1989" -> uncertaintyScore += 1;
			default -> {
			}
		}

		switch (issueState) {
			case "scope-found-issue" -> seriousnessScore += 3;
			case "symptoms-only" -> {
				seriousnessScore += 2;
				uncertaintyScore += 2;
			}
			default -> uncertaintyScore += 2;
		}

		switch (defectType) {
			case "roots" -> seriousnessScore += 1;
			case "cast-iron" -> seriousnessScore += 2;
			case "orangeburg" -> seriousnessScore += 3;
			case "belly" -> seriousnessScore += 2;
			case "offset-crack" -> seriousnessScore += 2;
			case "collapse" -> seriousnessScore += 4;
			default -> uncertaintyScore += 1;
		}

		switch (urgency) {
			case "active-decision" -> seriousnessScore += 1;
			case "urgent-repair" -> seriousnessScore += 3;
			default -> {
			}
		}

		if ("slab".equals(accessType)) {
			seriousnessScore += 1;
			uncertaintyScore += 1;
		}

		if ("buyer".equals(role) && "no-scope-yet".equals(issueState)) {
			uncertaintyScore += 1;
		}

		boolean scopeFirst = shouldRecommendScopeFirst(role, issueState, defectType);
		String riskTier = determineRiskTier(seriousnessScore, uncertaintyScore, scopeFirst);
		String riskClass = determineRiskClass(riskTier);
		String likelyNextStep = determineNextStep(role, issueState, defectType, scopeFirst);
		String interpretation = buildInterpretation(role, ageBand, issueState, defectType, accessType, riskTier);

		List<CostBand> costBands = buildCostBands(defectType, issueState, scopeFirst);
		List<String> uncertaintyDrivers = buildUncertaintyDrivers(accessType, defectType, ageBand);
		List<String> questions = buildQuestions(role, issueState, defectType);

		String primaryCtaLabel = scopeFirst ? "Find sewer camera inspection options"
			: "Get sewer repair or replacement quotes";
		String primaryCtaHref = scopeFirst ? "/find-sewer-scope/" : "/get-sewer-quotes/";
		String secondaryCtaLabel = scopeFirst ? "Review repair quote path" : "Find inspection options first";
		String secondaryCtaHref = scopeFirst ? "/get-sewer-quotes/" : "/find-sewer-scope/";

		String summaryBlock = """
			SewerVerdict next-step summary
			Location: %s
			Role: %s
			Current situation: %s
			Likely issue: %s
			Risk tier: %s
			Most likely next step: %s
			Key uncertainty: %s
			""".formatted(
			value(form.getLocation(), "Not provided"),
			prettyLabel(role),
			prettyLabel(issueState),
			prettyLabel(defectType),
			riskTier,
			likelyNextStep,
			String.join("; ", uncertaintyDrivers)
		).trim();

		return new EstimatorResult(
			riskTier,
			riskClass,
			interpretation,
			likelyNextStep,
			primaryCtaLabel,
			primaryCtaHref,
			secondaryCtaLabel,
			secondaryCtaHref,
			costBands,
			uncertaintyDrivers,
			questions,
			summaryBlock
		);
	}

	private boolean shouldRecommendScopeFirst(String role, String issueState, String defectType) {
		if ("no-scope-yet".equals(issueState)) {
			return true;
		}
		if ("symptoms-only".equals(issueState)) {
			return !"collapse".equals(defectType) && !"orangeburg".equals(defectType);
		}
		return "buyer".equals(role) && "roots".equals(defectType);
	}

	private String determineRiskTier(int seriousnessScore, int uncertaintyScore, boolean scopeFirst) {
		if (seriousnessScore >= 7) {
			return "High urgency / likely serious issue";
		}
		if (seriousnessScore + uncertaintyScore >= 5 || scopeFirst) {
			return "Meaningful uncertainty / moderate risk";
		}
		return "Low immediate risk";
	}

	private String determineRiskClass(String riskTier) {
		if (riskTier.startsWith("High")) {
			return "high";
		}
		if (riskTier.startsWith("Meaningful")) {
			return "medium";
		}
		return "low";
	}

	private String determineNextStep(String role, String issueState, String defectType, boolean scopeFirst) {
		if (scopeFirst) {
			return "Order a sewer scope before making a repair or negotiation call.";
		}
		if ("buyer".equals(role) && "scope-found-issue".equals(issueState)) {
			return "Document the finding, ask for specialist detail, and compare repair paths before final negotiation.";
		}
		if ("collapse".equals(defectType) || "orangeburg".equals(defectType)) {
			return "Get 2-3 repair quotes and compare trenchless viability against excavation.";
		}
		return "Get 2-3 repair quotes and compare spot repair versus broader replacement logic.";
	}

	private String buildInterpretation(String role, String ageBand, String issueState, String defectType,
		String accessType, String riskTier) {
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
		return "%s A %s home with a %s state and %s concern usually points to %s.%s".formatted(
			rolePhrase,
			prettyLabel(ageBand),
			prettyLabel(issueState),
			prettyLabel(defectType),
			riskTier.toLowerCase(),
			accessPhrase
		);
	}

	private List<CostBand> buildCostBands(String defectType, String issueState, boolean scopeFirst) {
		List<CostBand> bands = new ArrayList<>();
		if (scopeFirst) {
			bands.add(new CostBand("Inspection only", "$250-$650", "Useful when the line has not been scoped yet."));
		}
		bands.add(new CostBand("Cleaning / root treatment", "$300-$1,200",
			"Often the low end when the issue is limited and structural failure is not confirmed."));
		if (!"no-scope-yet".equals(issueState)) {
			bands.add(new CostBand("Spot repair", "$1,500-$6,500",
				"More plausible when the issue is isolated and the line is otherwise serviceable."));
		}
		if ("orangeburg".equals(defectType) || "collapse".equals(defectType) || "cast-iron".equals(defectType)) {
			bands.add(new CostBand("Trenchless rehab / bursting", "$5,000-$18,000",
				"Only viable when layout, access, and line condition cooperate."));
			bands.add(new CostBand("Full replacement / excavation", "$7,000-$25,000+",
				"Usually the upper-end path when access is bad, defects are systemic, or trenchless is not viable."));
		}
		else {
			bands.add(new CostBand("Full replacement / excavation", "$7,000-$20,000+",
				"Moves up with depth, hardscape, long runs, and restoration needs."));
		}
		return bands;
	}

	private List<String> buildUncertaintyDrivers(String accessType, String defectType, String ageBand) {
		List<String> drivers = new ArrayList<>();
		drivers.add("Actual line length and depth");
		drivers.add("Cleanout access and camera confirmation");
		if ("slab".equals(accessType)) {
			drivers.add("Under-slab access and restoration scope");
		}
		if ("orangeburg".equals(defectType) || "cast-iron".equals(defectType)) {
			drivers.add("Whether the defect is isolated or systemic for this material");
		}
		if ("pre-1950".equals(ageBand) || "1950-1969".equals(ageBand)) {
			drivers.add("Older material assumptions versus actual field condition");
		}
		drivers.add("Trenchless suitability versus dig-up requirements");
		return drivers;
	}

	private List<String> buildQuestions(String role, String issueState, String defectType) {
		List<String> questions = new ArrayList<>();
		if ("buyer".equals(role)) {
			questions.add("Can you show the exact footage, severity, and whether the defect is isolated or systemic?");
			questions.add("Is the best next move a scope now, a repair quote now, or a specialist opinion before credits?");
			questions.add("If repair is needed, what documentation would support negotiation instead of a vague allowance?");
			return questions;
		}
		if ("seller".equals(role)) {
			questions.add("Is this something to document and disclose, or something worth clarifying before listing?");
			questions.add("Would a scope or a repair quote reduce negotiation risk more effectively?");
			questions.add("What evidence would make buyers trust the explanation?");
			return questions;
		}
		if ("scope-found-issue".equals(issueState)) {
			questions.add("Is this likely a localized fix or evidence of broader line deterioration?");
			questions.add("Is trenchless genuinely viable here, or does access force excavation?");
			questions.add("What would change the quote most: depth, access, line length, or restoration?");
			return questions;
		}
		questions.add("What evidence would confirm whether this is structural or just a maintenance issue?");
		questions.add("Does the situation justify a scope first, or is quote-ready evidence already strong enough?");
		questions.add("What would make this more urgent over the next 30-90 days?");
		return questions;
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
}
