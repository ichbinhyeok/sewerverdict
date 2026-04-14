package com.example.sewerverdict.web;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

import com.example.sewerverdict.telemetry.OpsReportService;

@Controller
public class OpsController {

	private static final DateTimeFormatter REPORT_TIME_FORMAT =
		DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.of("Asia/Seoul"));

	private final OpsReportService opsReportService;
	private final SeoMetadataService seoMetadataService;
	private final boolean opsReportEnabled;
	private final String opsReportToken;

	public OpsController(OpsReportService opsReportService, SeoMetadataService seoMetadataService,
		@Value("${app.ops.report.enabled:false}") boolean opsReportEnabled,
		@Value("${app.ops.report.token:}") String opsReportToken) {
		this.opsReportService = opsReportService;
		this.seoMetadataService = seoMetadataService;
		this.opsReportEnabled = opsReportEnabled;
		this.opsReportToken = opsReportToken;
	}

	@GetMapping({"/ops/report", "/ops/report/"})
	public String report(HttpServletRequest request, HttpServletResponse response, Model model) {
		if (!opsReportEnabled || !isAuthorized(request)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		var report = opsReportService.buildReport();
		response.setHeader("X-Robots-Tag", "noindex, nofollow");
		response.setHeader("Cache-Control", "no-store");
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

	private boolean isAuthorized(HttpServletRequest request) {
		if (!StringUtils.hasText(opsReportToken)) {
			return false;
		}
		return opsReportToken.equals(request.getHeader("X-Ops-Token"));
	}
}
