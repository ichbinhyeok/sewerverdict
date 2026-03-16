package com.example.sewerverdict.web;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.sewerverdict.content.SiteContentService;
import com.example.sewerverdict.content.SitePage;

@Controller
public class SiteController {

	private final SiteContentService siteContentService;

	public SiteController(SiteContentService siteContentService) {
		this.siteContentService = siteContentService;
	}

	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("pageTitle", "SewerVerdict | Sewer scope risk and next-step estimator");
		model.addAttribute("metaDescription",
			"Calm, buyer-first sewer scope risk guidance for buyers, sellers, and owners. Estimate the next step, understand cost direction, and find inspection or quote paths.");
		model.addAttribute("featuredPages", siteContentService.getFeaturedPages(List.of(
			"/sewer-scope-before-buying-house/",
			"/sewer-line-replacement-cost/",
			"/sewer-scope-red-flags/",
			"/cast-iron-sewer-pipe-replacement-cost/",
			"/orangeburg-pipe-replacement-cost/",
			"/who-pays-for-sewer-line-repair-buyer-or-seller/"
		)));
		model.addAttribute("issuePages", siteContentService.getFeaturedPages(List.of(
			"/root-intrusion-sewer-line-what-to-do/",
			"/trenchless-sewer-replacement-cost/",
			"/sewer-line-replacement-cost/",
			"/sewer-scope-red-flags/"
		)));
		model.addAttribute("geoPages", siteContentService.getFeaturedPages(List.of(
			"/cities/chicago/sewer-line-replacement-cost/",
			"/cities/philadelphia/sewer-scope-before-buying-house/",
			"/cities/pittsburgh/cast-iron-sewer-line-risk/"
		)));
		return "home";
	}

	@GetMapping({
		"/{slug:[a-z0-9-]+}",
		"/{slug:[a-z0-9-]+}/",
		"/cities/{city:[a-z0-9-]+}/{slug:[a-z0-9-]+}",
		"/cities/{city:[a-z0-9-]+}/{slug:[a-z0-9-]+}/"
	})
	public String page(HttpServletRequest request, Model model) {
		SitePage page = siteContentService.requirePage(request.getRequestURI());
		model.addAttribute("page", page);
		model.addAttribute("relatedPages", siteContentService.getRelatedPages(page));
		model.addAttribute("pageTitle", page.getMetaTitle());
		model.addAttribute("metaDescription", page.getMetaDescription());
		model.addAttribute("breadcrumbs", buildBreadcrumbs(page));
		return "content-page";
	}

	private List<Breadcrumb> buildBreadcrumbs(SitePage page) {
		if (page.isGeoPage()) {
			String[] parts = page.getSlug().split("/");
			String city = parts.length > 2 ? parts[2] : "cities";
			return List.of(
				new Breadcrumb("Home", "/"),
				new Breadcrumb("Cities", "/cities/chicago/sewer-line-replacement-cost/"),
				new Breadcrumb(titleCase(city), page.getSlug())
			);
		}
		return switch (page.getFamily()) {
			case "buyer" -> List.of(
				new Breadcrumb("Home", "/"),
				new Breadcrumb("Buying a House", "/sewer-scope-before-buying-house/")
			);
			case "cost" -> List.of(
				new Breadcrumb("Home", "/"),
				new Breadcrumb("Costs", "/sewer-line-replacement-cost/")
			);
			case "defect" -> List.of(
				new Breadcrumb("Home", "/"),
				new Breadcrumb("Defects", "/sewer-scope-red-flags/")
			);
			case "coverage" -> List.of(
				new Breadcrumb("Home", "/"),
				new Breadcrumb("Responsibility and Coverage", "/homeowner-vs-city-sewer-responsibility/")
			);
			case "trust" -> List.of(
				new Breadcrumb("Home", "/"),
				new Breadcrumb("Methodology", "/methodology/")
			);
			default -> List.of(new Breadcrumb("Home", "/"));
		};
	}

	private String titleCase(String text) {
		if (text == null || text.isBlank()) {
			return "";
		}
		return Character.toUpperCase(text.charAt(0)) + text.substring(1).replace("-", " ");
	}

	public record Breadcrumb(String label, String href) {
	}
}
