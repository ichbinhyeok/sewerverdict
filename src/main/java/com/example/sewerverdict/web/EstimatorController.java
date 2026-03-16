package com.example.sewerverdict.web;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.sewerverdict.estimator.EstimatorForm;
import com.example.sewerverdict.estimator.EstimatorResult;
import com.example.sewerverdict.estimator.EstimatorService;
import com.example.sewerverdict.telemetry.StorageService;

@Controller
public class EstimatorController {

	private final EstimatorService estimatorService;
	private final StorageService storageService;

	public EstimatorController(EstimatorService estimatorService, StorageService storageService) {
		this.estimatorService = estimatorService;
		this.storageService = storageService;
	}

	@GetMapping({"/estimator", "/estimator/"})
	public String estimator(@ModelAttribute("form") EstimatorForm form, Model model) {
		model.addAttribute("pageTitle", "Estimator | SewerVerdict");
		model.addAttribute("metaDescription",
			"Estimate sewer-line risk, likely next step, and rough cost direction for buyers, sellers, and owners.");
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
		response.setHeader("X-Robots-Tag", "noindex, nofollow");

		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("role", form.getRole());
		payload.put("issueState", form.getIssueState());
		payload.put("defectType", form.getDefectType());
		payload.put("urgency", form.getUrgency());
		payload.put("riskTier", result.riskTier());
		storageService.logEvent("estimator_complete", "/estimator/results/", request.getHeader("Referer"), payload);

		model.addAttribute("pageTitle", "Estimator results | SewerVerdict");
		model.addAttribute("metaDescription", "Educational next-step estimate for sewer scope and sewer line risk.");
		model.addAttribute("noindex", true);
		model.addAttribute("result", result);
		return "result";
	}
}
