package com.example.sewerverdict.web;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.sewerverdict.estimator.EstimatorForm;
import com.example.sewerverdict.estimator.EstimatorResult;
import com.example.sewerverdict.estimator.EstimatorService;
import com.example.sewerverdict.content.SourceRegistryService;
import com.example.sewerverdict.telemetry.StorageService;

@Controller
public class EstimatorController {

	private final EstimatorService estimatorService;
	private final StorageService storageService;
	private final SourceRegistryService sourceRegistryService;
	private final SeoMetadataService seoMetadataService;

	public EstimatorController(EstimatorService estimatorService, StorageService storageService,
		SourceRegistryService sourceRegistryService, SeoMetadataService seoMetadataService) {
		this.estimatorService = estimatorService;
		this.storageService = storageService;
		this.sourceRegistryService = sourceRegistryService;
		this.seoMetadataService = seoMetadataService;
	}

	@GetMapping({"/estimator", "/estimator/"})
	public String estimator(@ModelAttribute("form") EstimatorForm form, HttpServletRequest request, Model model) {
		applyEstimatorDefaults(form);
		populateEstimatorModel(model, request);
		return "estimator";
	}

	@GetMapping({"/estimator/results", "/estimator/results/"})
	public String estimatorResultsFallback() {
		return "redirect:/estimator/";
	}

	@PostMapping({"/estimator/results", "/estimator/results/"})
	public String estimatorResults(@ModelAttribute("form") EstimatorForm form, HttpServletRequest request,
		HttpServletResponse response, Model model) {
		applyEstimatorDefaults(form);
		List<String> validationErrors = validateEstimatorForm(form);
		if (!validationErrors.isEmpty()) {
			populateEstimatorModel(model, request);
			model.addAttribute("estimatorError", "Finish the missing steps before SewerClarity builds a directional call.");
			model.addAttribute("estimatorErrorDetails", validationErrors);
			return "estimator";
		}

		EstimatorResult result = estimatorService.evaluate(form);
		HttpSession session = request.getSession(true);
		String sessionId = session.getId();
		String draftId = storageService.storeEstimatorDraft(form, result, "/estimator/results/", request.getHeader("Referer"),
			sessionId);
		response.setHeader("X-Robots-Tag", "noindex, nofollow");

		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("draftId", draftId);
		payload.put("role", form.getRole());
		payload.put("issueState", form.getIssueState());
		payload.put("defectType", form.getDefectType());
		payload.put("urgency", form.getUrgency());
		payload.put("riskTier", result.riskTier());
		payload.put("routingBucket", result.routingBucket());
		storageService.logEvent("estimator_complete", "/estimator/results/", request.getHeader("Referer"), sessionId, payload);

		model.addAttribute("pageTitle", "Estimator results | SewerClarity");
		model.addAttribute("metaDescription", "Educational next-step estimate for sewer scope and sewer line risk.");
		model.addAttribute("noindex", true);
		model.addAttribute("result", result);
		model.addAttribute("resultSources", sourceRegistryService.getSourcesByIds(result.sourceIds()));
		model.addAttribute("form", form);
		model.addAttribute("draftId", draftId);
		model.addAttribute("primaryCtaHref", buildLeadHref(result.primaryCtaHref(), result.primaryServiceNeeded(), draftId, form));
		model.addAttribute("secondaryCtaHref", buildLeadHref(result.secondaryCtaHref(), result.secondaryServiceNeeded(), draftId, form));
		return "result";
	}

	private void populateEstimatorModel(Model model, HttpServletRequest request) {
		model.addAttribute("pageTitle", "Estimator | SewerClarity");
		model.addAttribute("metaDescription",
			"Estimate sewer-line risk, likely next step, and rough cost direction for buyers, sellers, and owners.");
		seoMetadataService.apply(model, request,
			"Estimator | SewerClarity",
			"Estimate sewer-line risk, likely next step, and rough cost direction for buyers, sellers, and owners.",
			"website",
			List.of(new SiteController.Breadcrumb("Home", "/"), new SiteController.Breadcrumb("Estimator", "/estimator/")),
			List.of(),
			false);
		seoMetadataService.appendWebApplicationSchema(model, request,
			"SewerClarity Estimator",
			"Web tool for estimating sewer-line risk, likely next step, and rough cost direction for buyers, sellers, and owners.");
	}

	private void applyEstimatorDefaults(EstimatorForm form) {
		if (!StringUtils.hasText(form.getDefectType())) {
			form.setDefectType("unknown");
		}
		if (!StringUtils.hasText(form.getAccessType())) {
			form.setAccessType("unknown");
		}
	}

	private List<String> validateEstimatorForm(EstimatorForm form) {
		List<String> errors = new java.util.ArrayList<>();
		if (!StringUtils.hasText(form.getRole())) {
			errors.add("Choose whether you are the buyer, seller, or owner.");
		}
		if (!StringUtils.hasText(form.getLocation())) {
			errors.add("Add the property city or ZIP so the result anchors to a real covered market.");
		}
		if (!StringUtils.hasText(form.getHouseAgeBand())) {
			errors.add("Choose the house age band so the buried-line risk is not too generic.");
		}
		if (!StringUtils.hasText(form.getIssueState())) {
			errors.add("Choose whether you have no scope yet, symptoms only, or a scope finding.");
		}
		if (!StringUtils.hasText(form.getUrgency())) {
			errors.add("Choose whether this is research, an active transaction decision, or an urgent repair problem.");
		}
		return errors;
	}

	private String buildLeadHref(String basePath, String serviceNeeded, String draftId, EstimatorForm form) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath(basePath)
			.queryParam("serviceNeeded", serviceNeeded)
			.queryParam("draftId", draftId)
			.queryParam("recommendedServicePath", serviceNeeded);
		appendIfPresent(builder, "role", form.getRole());
		appendIfPresent(builder, "zipOrCity", form.getLocation());
		appendIfPresent(builder, "streetAddress", form.getStreetAddress());
		appendIfPresent(builder, "houseAgeBand", form.getHouseAgeBand());
		appendIfPresent(builder, "issueState", form.getIssueState());
		appendIfPresent(builder, "defectType", form.getDefectType());
		appendIfPresent(builder, "urgency", form.getUrgency());
		appendIfPresent(builder, "utmSource", form.getUtmSource());
		appendIfPresent(builder, "utmMedium", form.getUtmMedium());
		appendIfPresent(builder, "utmCampaign", form.getUtmCampaign());
		appendIfPresent(builder, "utmTerm", form.getUtmTerm());
		appendIfPresent(builder, "utmContent", form.getUtmContent());
		appendIfPresent(builder, "gclid", form.getGclid());
		appendIfPresent(builder, "wbraid", form.getWbraid());
		appendIfPresent(builder, "gbraid", form.getGbraid());
		return builder.build().encode().toUriString();
	}

	private void appendIfPresent(UriComponentsBuilder builder, String key, String value) {
		if (value != null && !value.isBlank()) {
			builder.queryParam(key, value);
		}
	}
}

