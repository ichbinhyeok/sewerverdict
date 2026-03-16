package com.example.sewerverdict.web;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.sewerverdict.content.PageFaq;
import com.example.sewerverdict.content.SiteContentService;
import com.example.sewerverdict.content.SitePage;

@Controller
public class SiteController {

	private final SiteContentService siteContentService;
	private final SeoMetadataService seoMetadataService;

	public SiteController(SiteContentService siteContentService, SeoMetadataService seoMetadataService) {
		this.siteContentService = siteContentService;
		this.seoMetadataService = seoMetadataService;
	}

	@GetMapping("/")
	public String home(HttpServletRequest request, Model model) {
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
		List<PageFaq> homeFaq = List.of(
			faq("What can the estimator do?", "It narrows the likely next step, rough cost direction, and biggest uncertainty drivers for buyers, sellers, and owners."),
			faq("Does it replace a sewer scope?", "No. It is an educational next-step tool, not a substitute for a sewer camera inspection or an in-person quote."),
			faq("Does seller responsibility always work the same way?", "No. Responsibility and leverage vary by evidence, contract stage, local practice, and line-location rules.")
		);
		model.addAttribute("homeFaq", homeFaq);
		seoMetadataService.apply(model, request,
			"SewerVerdict | Sewer scope risk and next-step estimator",
			"Calm, buyer-first sewer scope risk guidance for buyers, sellers, and owners. Estimate the next step, understand cost direction, and find inspection or quote paths.",
			"website",
			List.of(new Breadcrumb("Home", "/")),
			homeFaq,
			true);
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
		List<Breadcrumb> breadcrumbs = buildBreadcrumbs(page);
		model.addAttribute("page", page);
		model.addAttribute("relatedPages", siteContentService.getRelatedPages(page));
		model.addAttribute("pageTitle", page.getMetaTitle());
		model.addAttribute("metaDescription", page.getMetaDescription());
		model.addAttribute("breadcrumbs", breadcrumbs);
		seoMetadataService.apply(model, request, page.getMetaTitle(), page.getMetaDescription(), "article", breadcrumbs, page.getFaq(), false);
		return "content-page";
	}

	private List<Breadcrumb> buildBreadcrumbs(SitePage page) {
		if (page.isGeoPage()) {
			return List.of(
				new Breadcrumb("Home", "/"),
				new Breadcrumb("Cities", "/cities/chicago/sewer-line-replacement-cost/"),
				new Breadcrumb(page.getTitle(), page.getSlug())
			);
		}
		return switch (page.getFamily()) {
			case "buyer" -> List.of(
				new Breadcrumb("Home", "/"),
				new Breadcrumb("Buying a House", "/sewer-scope-before-buying-house/"),
				new Breadcrumb(page.getTitle(), page.getSlug())
			);
			case "cost" -> List.of(
				new Breadcrumb("Home", "/"),
				new Breadcrumb("Costs", "/sewer-line-replacement-cost/"),
				new Breadcrumb(page.getTitle(), page.getSlug())
			);
			case "defect" -> List.of(
				new Breadcrumb("Home", "/"),
				new Breadcrumb("Defects", "/sewer-scope-red-flags/"),
				new Breadcrumb(page.getTitle(), page.getSlug())
			);
			case "coverage" -> List.of(
				new Breadcrumb("Home", "/"),
				new Breadcrumb("Responsibility and Coverage", "/homeowner-vs-city-sewer-responsibility/"),
				new Breadcrumb(page.getTitle(), page.getSlug())
			);
			case "trust" -> List.of(
				new Breadcrumb("Home", "/"),
				new Breadcrumb("Methodology", "/methodology/"),
				new Breadcrumb(page.getTitle(), page.getSlug())
			);
			default -> List.of(new Breadcrumb("Home", "/"), new Breadcrumb(page.getTitle(), page.getSlug()));
		};
	}

	private PageFaq faq(String question, String answer) {
		PageFaq item = new PageFaq();
		item.setQuestion(question);
		item.setAnswer(answer);
		return item;
	}

	public record Breadcrumb(String label, String href) {
	}
}
