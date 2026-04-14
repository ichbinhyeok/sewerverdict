package com.example.sewerverdict.web;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.sewerverdict.content.MunicipalityResolution;
import com.example.sewerverdict.content.MunicipalityResolver;
import com.example.sewerverdict.content.PageFaq;
import com.example.sewerverdict.telemetry.EstimatorDraftSnapshot;
import com.example.sewerverdict.telemetry.LeadForm;
import com.example.sewerverdict.telemetry.StorageService;

@Controller
public class LeadController {

	private final StorageService storageService;
	private final SeoMetadataService seoMetadataService;
	private final MunicipalityResolver municipalityResolver;

	public LeadController(StorageService storageService, SeoMetadataService seoMetadataService,
		MunicipalityResolver municipalityResolver) {
		this.storageService = storageService;
		this.seoMetadataService = seoMetadataService;
		this.municipalityResolver = municipalityResolver;
	}

	@GetMapping({"/find-sewer-scope", "/find-sewer-scope/"})
	public String findSewerScope(@ModelAttribute("leadForm") LeadForm leadForm, HttpServletRequest request, Model model) {
		if (!StringUtils.hasText(leadForm.getServiceNeeded())) {
			leadForm.setServiceNeeded("inspection");
		}
		if (!StringUtils.hasText(leadForm.getRecommendedServicePath())) {
			leadForm.setRecommendedServicePath("inspection");
		}
		hydrateLeadFormFromDraft(leadForm);
		HttpSession session = request.getSession(true);
		storageService.logEvent("lead_form_view", "/find-sewer-scope/", request.getHeader("Referer"), session.getId(),
			buildLeadViewPayload(leadForm));
		populateLeadModel(model,
			"Find sewer camera inspection options",
			"Inspection-first guidance for buyers, sellers, and owners who need a scope before making a bigger call.",
			List.of(
				"Best for buyers under contract, sellers pre-listing, and owners with symptoms but no confirmation.",
				"Useful when the next smart move is clarity, not a rushed repair commitment.",
				"Short form only asks for details that improve routing. Phone is optional."
			),
			"/find-sewer-scope/",
			leadForm
		);
		seoMetadataService.apply(model, request, "Find sewer camera inspection options | SewerClarity",
			"Inspection-first guidance for buyers, sellers, and owners who need a scope before making a bigger call.",
			"website",
			List.of(new SiteController.Breadcrumb("Home", "/"), new SiteController.Breadcrumb("Find sewer camera inspection options", "/find-sewer-scope/")),
			List.of(),
			false);
		return "lead";
	}

	@PostMapping({"/find-sewer-scope", "/find-sewer-scope/"})
	public String submitFindSewerScope(@ModelAttribute("leadForm") LeadForm leadForm, HttpServletRequest request,
		Model model) {
		leadForm.setServiceNeeded(StringUtils.hasText(leadForm.getServiceNeeded()) ? leadForm.getServiceNeeded() : "inspection");
		leadForm.setRecommendedServicePath(StringUtils.hasText(leadForm.getRecommendedServicePath())
			? leadForm.getRecommendedServicePath()
			: "inspection");
		hydrateLeadFormFromDraft(leadForm);
		submitLead(leadForm, "/find-sewer-scope/", request, model);
		populateLeadModel(model,
			"Find sewer camera inspection options",
			"Inspection-first guidance for buyers, sellers, and owners who need a scope before making a bigger call.",
			List.of(
				"Best for buyers under contract, sellers pre-listing, and owners with symptoms but no confirmation.",
				"Useful when the next smart move is clarity, not a rushed repair commitment.",
				"Short form only asks for details that improve routing. Phone is optional."
			),
			"/find-sewer-scope/",
			leadForm
		);
		seoMetadataService.apply(model, request, "Find sewer camera inspection options | SewerClarity",
			"Inspection-first guidance for buyers, sellers, and owners who need a scope before making a bigger call.",
			"website",
			List.of(new SiteController.Breadcrumb("Home", "/"), new SiteController.Breadcrumb("Find sewer camera inspection options", "/find-sewer-scope/")),
			List.of(),
			false);
		return "lead";
	}

	@GetMapping({"/get-sewer-quotes", "/get-sewer-quotes/"})
	public String getSewerQuotes(@ModelAttribute("leadForm") LeadForm leadForm, HttpServletRequest request, Model model) {
		if (!StringUtils.hasText(leadForm.getServiceNeeded())) {
			leadForm.setServiceNeeded("replacement");
		}
		if (!StringUtils.hasText(leadForm.getRecommendedServicePath())) {
			leadForm.setRecommendedServicePath("replacement");
		}
		hydrateLeadFormFromDraft(leadForm);
		HttpSession session = request.getSession(true);
		storageService.logEvent("lead_form_view", "/get-sewer-quotes/", request.getHeader("Referer"), session.getId(),
			buildLeadViewPayload(leadForm));
		populateLeadModel(model,
			"Get sewer repair or replacement quotes",
			"Quote-first guidance for confirmed issues, higher-risk findings, or owners comparing repair paths.",
			List.of(
				"Best when the line has already been scoped or there is strong evidence of a serious issue.",
				"Useful for comparing spot repair, trenchless, and excavation logic.",
				"Keep the form short so qualified users actually submit it. Phone is optional."
			),
			"/get-sewer-quotes/",
			leadForm
		);
		seoMetadataService.apply(model, request, "Get sewer repair or replacement quotes | SewerClarity",
			"Quote-first guidance for confirmed issues, higher-risk findings, or owners comparing repair paths.",
			"website",
			List.of(new SiteController.Breadcrumb("Home", "/"), new SiteController.Breadcrumb("Get sewer repair or replacement quotes", "/get-sewer-quotes/")),
			List.of(),
			false);
		return "lead";
	}

	@PostMapping({"/get-sewer-quotes", "/get-sewer-quotes/"})
	public String submitSewerQuotes(@ModelAttribute("leadForm") LeadForm leadForm, HttpServletRequest request,
		Model model) {
		leadForm.setServiceNeeded(StringUtils.hasText(leadForm.getServiceNeeded()) ? leadForm.getServiceNeeded() : "replacement");
		leadForm.setRecommendedServicePath(StringUtils.hasText(leadForm.getRecommendedServicePath())
			? leadForm.getRecommendedServicePath()
			: "replacement");
		hydrateLeadFormFromDraft(leadForm);
		submitLead(leadForm, "/get-sewer-quotes/", request, model);
		populateLeadModel(model,
			"Get sewer repair or replacement quotes",
			"Quote-first guidance for confirmed issues, higher-risk findings, or owners comparing repair paths.",
			List.of(
				"Best when the line has already been scoped or there is strong evidence of a serious issue.",
				"Useful for comparing spot repair, trenchless, and excavation logic.",
				"Keep the form short so qualified users actually submit it. Phone is optional."
			),
			"/get-sewer-quotes/",
			leadForm
		);
		seoMetadataService.apply(model, request, "Get sewer repair or replacement quotes | SewerClarity",
			"Quote-first guidance for confirmed issues, higher-risk findings, or owners comparing repair paths.",
			"website",
			List.of(new SiteController.Breadcrumb("Home", "/"), new SiteController.Breadcrumb("Get sewer repair or replacement quotes", "/get-sewer-quotes/")),
			List.of(),
			false);
		return "lead";
	}

	private void submitLead(LeadForm leadForm, String pageSlug, HttpServletRequest request, Model model) {
		HttpSession session = request.getSession(true);
		String sessionId = session.getId();
		Map<String, String> fieldErrors = storageService.getLeadValidationErrors(leadForm);
		if (!fieldErrors.isEmpty()) {
			Map<String, Object> payload = buildLeadViewPayload(leadForm);
			payload.put("missingFields", fieldErrors.keySet());
			model.addAttribute("fieldErrors", fieldErrors);
			model.addAttribute("formError",
				"Please check the highlighted fields and consent box before submitting.");
			storageService.logEvent("lead_submit_invalid", pageSlug, request.getHeader("Referer"), sessionId, payload);
			return;
		}

		storageService.storeLead(leadForm, pageSlug, request.getHeader("Referer"), sessionId);

		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("draftId", leadForm.getDraftId());
		payload.put("serviceNeeded", leadForm.getServiceNeeded());
		payload.put("recommendedServicePath", leadForm.getRecommendedServicePath());
		payload.put("routingBucket", storageService.determineLeadRoutingBucket(leadForm));
		payload.put("role", leadForm.getRole());
		payload.put("issueState", leadForm.getIssueState());
		storageService.logEvent("lead_submit", pageSlug, request.getHeader("Referer"), sessionId, payload);
		model.addAttribute("successMessage",
			"Thanks. Your details were saved for routing. A real inspection or quote path still depends on scope, access, local fit, and what the evidence can honestly support.");
		LeadForm blankForm = new LeadForm();
		blankForm.setServiceNeeded(leadForm.getServiceNeeded());
		blankForm.setRecommendedServicePath(leadForm.getRecommendedServicePath());
		model.addAttribute("leadForm", blankForm);
		model.addAttribute("fieldErrors", Map.of());
	}

	private void populateLeadModel(Model model, String title, String summary, List<String> highlights, String formAction,
		LeadForm leadForm) {
		model.addAttribute("pageTitle", title + " | SewerClarity");
		model.addAttribute("metaDescription", summary);
		model.addAttribute("leadTitle", title);
		model.addAttribute("leadSummary", summary);
		model.addAttribute("leadHighlights", highlights);
		model.addAttribute("leadTrustPoints", buildLeadTrustPoints(formAction));
		model.addAttribute("leadExpectationTitle", "What happens after you submit");
		model.addAttribute("leadExpectationSummary", "/find-sewer-scope/".equals(formAction)
			? "This form is for routing and triage, not for pushing you straight into a repair pitch."
			: "This form is for comparing repair paths honestly and routing the next quote conversation, not for pretending every concern is already a full replacement project.");
		model.addAttribute("leadGuideLinks", buildLeadGuideLinks(formAction));
		model.addAttribute("leadFaq", buildLeadFaq(formAction));
		model.addAttribute("formAction", formAction);
		model.asMap().putIfAbsent("fieldErrors", Map.of());
		EstimatorDraftSnapshot draftSummary = storageService.getEstimatorDraft(leadForm.getDraftId()).orElse(null);
		if (draftSummary != null) {
			model.addAttribute("draftSummary", draftSummary);
		}
		MunicipalityResolution municipalityResolution = municipalityResolver.resolve(leadForm.getStreetAddress(),
			leadForm.getZipOrCity()).orElse(null);
		if (municipalityResolution != null) {
			model.addAttribute("leadMunicipalitySummary", buildLeadMunicipalitySummary(municipalityResolution));
		}
		boolean cityConfirmationNeeded = municipalityResolution == null
			? looksLikeZipOnly(leadForm.getZipOrCity())
				|| (!StringUtils.hasText(leadForm.getZipOrCity()) && draftSummary != null && draftSummary.cityConfirmationNeeded())
			: !municipalityResolution.exactMunicipalityMatch();
		model.addAttribute("cityConfirmationNeeded", cityConfirmationNeeded);
	}

	private String buildLeadMunicipalitySummary(MunicipalityResolution municipalityResolution) {
		if (municipalityResolution.matchedCoveredProfile() && municipalityResolution.exactMunicipalityMatch()) {
			return "Street address matched " + municipalityResolution.coveredLabel()
				+ " through the U.S. Census geocoder, so this no longer depends on ZIP-only city guessing.";
		}
		if (municipalityResolution.matchedCoveredProfile()) {
			return "Street address resolved to a Census county subdivision signal consistent with "
				+ municipalityResolution.coveredLabel()
				+ ". That is better than ZIP-only guessing, but it does not prove the exact municipality or city-rule boundary yet.";
		}
		if (municipalityResolution.exactMunicipalityMatch()) {
			return "Street address matched " + municipalityResolution.municipalityLabel()
				+ " through the U.S. Census geocoder, which is better than ZIP-only guessing even though SewerClarity does not yet have a stored local profile for that municipality.";
		}
		return "Street address resolved to a Census county subdivision signal for "
			+ municipalityResolution.municipalityLabel()
			+ ". That is still better than ZIP-only guessing, but it does not prove exact municipality or city-rule certainty.";
	}

	private void hydrateLeadFormFromDraft(LeadForm leadForm) {
		storageService.getEstimatorDraft(leadForm.getDraftId()).ifPresent(draft -> {
			if (!StringUtils.hasText(leadForm.getZipOrCity()) && StringUtils.hasText(draft.location())) {
				leadForm.setZipOrCity(draft.location());
			}
			if (!StringUtils.hasText(leadForm.getStreetAddress()) && StringUtils.hasText(draft.streetAddress())) {
				leadForm.setStreetAddress(draft.streetAddress());
			}
		});
	}

	private List<LeadGuideLink> buildLeadGuideLinks(String formAction) {
		if ("/find-sewer-scope/".equals(formAction)) {
			return List.of(
				new LeadGuideLink("/sewer-scope-before-buying-house/", "Sewer Scope Before Buying a House",
					"When buried-line uncertainty is worth reducing before closing."),
				new LeadGuideLink("/sewer-scope-negotiation-with-seller/", "Sewer Scope Negotiation With Seller",
					"How better evidence can lead to a cleaner seller conversation."),
				new LeadGuideLink("/homeowner-vs-city-sewer-responsibility/", "Homeowner vs City Sewer Responsibility",
					"When the real question is city boundary, ownership, or local program fit.")
			);
		}
		return List.of(
			new LeadGuideLink("/sewer-line-repair-vs-replacement/", "Sewer Line Repair vs Replacement",
				"How to compare isolated repair logic against broader replacement logic."),
			new LeadGuideLink("/sewer-line-replacement-cost/", "Sewer Line Replacement Cost",
				"What usually moves sewer replacement pricing up or down."),
			new LeadGuideLink("/trenchless-vs-traditional-sewer-line-replacement/", "Trenchless vs Traditional Sewer Line Replacement",
				"When trenchless is viable and when excavation is still more honest.")
		);
	}

	private List<PageFaq> buildLeadFaq(String formAction) {
		if ("/find-sewer-scope/".equals(formAction)) {
			return List.of(
				faq("Is this a repair quote request?", "No. This page is for inspection-first routing when the evidence is still thin or the transaction risk is still unclear."),
				faq("Will submitting this form lock me into a vendor?", "No. The point is to start the right inspection or triage path, not to trap you in a rushed repair commitment."),
				faq("Why does the form still ask for location?", "City is the cleanest local match. Supported ZIPs can still anchor to a covered city profile or delivery market, but ZIP alone does not prove a parcel-specific transfer or compliance rule.")
			);
		}
		return List.of(
			faq("Should I use this page if I only have symptoms?", "Usually no. If the evidence is still thin, inspection-first is a more honest route than forcing quote comparison too early."),
			faq("Does a quote-ready route mean full replacement is certain?", "No. Quote-ready means the problem looks serious enough to compare repair paths, not that the answer is automatically full replacement."),
			faq("What usually changes the quote most?", "Line length, depth, access, restoration, and whether the issue is isolated or systemic usually move sewer quotes the most.")
		);
	}

	private PageFaq faq(String question, String answer) {
		PageFaq item = new PageFaq();
		item.setQuestion(question);
		item.setAnswer(answer);
		return item;
	}

	private record LeadGuideLink(String href, String label, String summary) {
	}

	private List<String> buildLeadTrustPoints(String formAction) {
		if ("/find-sewer-scope/".equals(formAction)) {
			return List.of(
				"Best for buyers, sellers, and owners who still need better evidence before pricing repairs.",
				"Submitting does not commit you to a repair quote or a marketplace blast.",
				"City still gives the cleanest local fit, supported ZIPs can anchor to a covered city profile or delivery market, and phone is optional if email is easier."
			);
		}
		return List.of(
			"Best for confirmed findings or situations that already look serious enough to compare repair paths.",
			"Quote-ready routing still depends on footage, access, and whether the issue is isolated or systemic.",
			"If the evidence is still too thin, the honest next recommendation may still be inspection or footage clarification first."
		);
	}

	private boolean looksLikeZipOnly(String value) {
		return StringUtils.hasText(value) && value.trim().matches("^\\d{5}(?:-\\d{4})?$");
	}

	private Map<String, Object> buildLeadViewPayload(LeadForm leadForm) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("draftId", leadForm.getDraftId());
		payload.put("serviceNeeded", leadForm.getServiceNeeded());
		payload.put("recommendedServicePath", leadForm.getRecommendedServicePath());
		payload.put("role", leadForm.getRole());
		payload.put("issueState", leadForm.getIssueState());
		payload.put("defectType", leadForm.getDefectType());
		return payload;
	}
}

