package com.example.sewerverdict.web;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.sewerverdict.estimator.EstimatorForm;
import com.example.sewerverdict.estimator.EstimatorResult;
import com.example.sewerverdict.estimator.EstimatorService;
import com.example.sewerverdict.telemetry.StorageService;

@Controller
public class EstimatorController {

	private final EstimatorService estimatorService;
	private final StorageService storageService;
	private final SeoMetadataService seoMetadataService;

	public EstimatorController(EstimatorService estimatorService, StorageService storageService,
		SeoMetadataService seoMetadataService) {
		this.estimatorService = estimatorService;
		this.storageService = storageService;
		this.seoMetadataService = seoMetadataService;
	}

	@GetMapping({"/estimator", "/estimator/"})
	public String estimator(@ModelAttribute("form") EstimatorForm form, HttpServletRequest request, Model model) {
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
		return "estimator";
	}

	@GetMapping({"/estimator/results", "/estimator/results/"})
	public String estimatorResultsFallback() {
		return "redirect:/estimator/";
	}

	@PostMapping({"/estimator/results", "/estimator/results/"})
	public String estimatorResults(@ModelAttribute("form") EstimatorForm form, HttpServletRequest request,
		HttpServletResponse response, Model model) {
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
		model.addAttribute("draftId", draftId);
		model.addAttribute("primaryCtaHref", buildLeadHref(result.primaryCtaHref(), result.primaryServiceNeeded(), draftId, form));
		model.addAttribute("secondaryCtaHref", buildLeadHref(result.secondaryCtaHref(), result.secondaryServiceNeeded(), draftId, form));
		return "result";
	}

	private String buildLeadHref(String basePath, String serviceNeeded, String draftId, EstimatorForm form) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath(basePath)
			.queryParam("serviceNeeded", serviceNeeded)
			.queryParam("draftId", draftId)
			.queryParam("recommendedServicePath", serviceNeeded);
		appendIfPresent(builder, "role", form.getRole());
		appendIfPresent(builder, "zipOrCity", form.getLocation());
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
		return builder.build().toUriString();
	}

	private void appendIfPresent(UriComponentsBuilder builder, String key, String value) {
		if (value != null && !value.isBlank()) {
			builder.queryParam(key, value);
		}
	}
}

