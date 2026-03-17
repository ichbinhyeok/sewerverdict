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

import com.example.sewerverdict.telemetry.LeadForm;
import com.example.sewerverdict.telemetry.StorageService;

@Controller
public class LeadController {

	private final StorageService storageService;
	private final SeoMetadataService seoMetadataService;

	public LeadController(StorageService storageService, SeoMetadataService seoMetadataService) {
		this.storageService = storageService;
		this.seoMetadataService = seoMetadataService;
	}

	@GetMapping({"/find-sewer-scope", "/find-sewer-scope/"})
	public String findSewerScope(@ModelAttribute("leadForm") LeadForm leadForm, HttpServletRequest request, Model model) {
		if (!StringUtils.hasText(leadForm.getServiceNeeded())) {
			leadForm.setServiceNeeded("inspection");
		}
		if (!StringUtils.hasText(leadForm.getRecommendedServicePath())) {
			leadForm.setRecommendedServicePath("inspection");
		}
		HttpSession session = request.getSession(true);
		storageService.logEvent("lead_form_view", "/find-sewer-scope/", request.getHeader("Referer"), session.getId(),
			buildLeadViewPayload(leadForm));
		populateLeadModel(model,
			"Find sewer camera inspection options",
			"Inspection-first guidance for buyers, sellers, and owners who need a scope before making a bigger call.",
			List.of(
				"Best for buyers under contract, sellers pre-listing, and owners with symptoms but no confirmation.",
				"Useful when the next smart move is clarity, not a rushed repair commitment.",
				"Short form only asks for details that improve routing."
			),
			"/find-sewer-scope/"
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
		submitLead(leadForm, "/find-sewer-scope/", request, model);
		populateLeadModel(model,
			"Find sewer camera inspection options",
			"Inspection-first guidance for buyers, sellers, and owners who need a scope before making a bigger call.",
			List.of(
				"Best for buyers under contract, sellers pre-listing, and owners with symptoms but no confirmation.",
				"Useful when the next smart move is clarity, not a rushed repair commitment.",
				"Short form only asks for details that improve routing."
			),
			"/find-sewer-scope/"
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
		HttpSession session = request.getSession(true);
		storageService.logEvent("lead_form_view", "/get-sewer-quotes/", request.getHeader("Referer"), session.getId(),
			buildLeadViewPayload(leadForm));
		populateLeadModel(model,
			"Get sewer repair or replacement quotes",
			"Quote-first guidance for confirmed issues, higher-risk findings, or owners comparing repair paths.",
			List.of(
				"Best when the line has already been scoped or there is strong evidence of a serious issue.",
				"Useful for comparing spot repair, trenchless, and excavation logic.",
				"Keep the form short so qualified users actually submit it."
			),
			"/get-sewer-quotes/"
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
		submitLead(leadForm, "/get-sewer-quotes/", request, model);
		populateLeadModel(model,
			"Get sewer repair or replacement quotes",
			"Quote-first guidance for confirmed issues, higher-risk findings, or owners comparing repair paths.",
			List.of(
				"Best when the line has already been scoped or there is strong evidence of a serious issue.",
				"Useful for comparing spot repair, trenchless, and excavation logic.",
				"Keep the form short so qualified users actually submit it."
			),
			"/get-sewer-quotes/"
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
			"Thanks. Your details were saved for the next routing step. A real quote or inspection path still depends on scope, access, and local fit.");
		LeadForm blankForm = new LeadForm();
		blankForm.setServiceNeeded(leadForm.getServiceNeeded());
		blankForm.setRecommendedServicePath(leadForm.getRecommendedServicePath());
		model.addAttribute("leadForm", blankForm);
		model.addAttribute("fieldErrors", Map.of());
	}

	private void populateLeadModel(Model model, String title, String summary, List<String> highlights, String formAction) {
		model.addAttribute("pageTitle", title + " | SewerClarity");
		model.addAttribute("metaDescription", summary);
		model.addAttribute("leadTitle", title);
		model.addAttribute("leadSummary", summary);
		model.addAttribute("leadHighlights", highlights);
		model.addAttribute("formAction", formAction);
		model.asMap().putIfAbsent("fieldErrors", Map.of());
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

