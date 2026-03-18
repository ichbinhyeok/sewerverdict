package com.example.sewerverdict.web;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.sewerverdict.telemetry.OpsReportService;

@Controller
public class OpsController {

	private static final DateTimeFormatter REPORT_TIME_FORMAT =
		DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.of("Asia/Seoul"));

	private final OpsReportService opsReportService;
	private final SeoMetadataService seoMetadataService;

	public OpsController(OpsReportService opsReportService, SeoMetadataService seoMetadataService) {
		this.opsReportService = opsReportService;
		this.seoMetadataService = seoMetadataService;
	}

	@GetMapping({"/ops/report", "/ops/report/"})
	public String report(HttpServletRequest request, HttpServletResponse response, Model model) {
		var report = opsReportService.buildReport();
		response.setHeader("X-Robots-Tag", "noindex, nofollow");
		model.addAttribute("pageTitle", "Ops Report | SewerClarity");
		model.addAttribute("metaDescription", "Noindex internal operations view for estimator, CTA, and lead telemetry.");
		model.addAttribute("noindex", true);
		model.addAttribute("report", report);
		model.addAttribute("generatedAtLabel", REPORT_TIME_FORMAT.format(report.generatedAt()));
		seoMetadataService.apply(model, request,
			"Ops Report | SewerClarity",
			"Noindex internal operations view for estimator, CTA, and lead telemetry.",
			"website",
			List.of(
				new SiteController.Breadcrumb("Home", "/"),
				new SiteController.Breadcrumb("Ops Report", "/ops/report/")
			),
			List.of(),
			false);
		return "ops-report";
	}
}
