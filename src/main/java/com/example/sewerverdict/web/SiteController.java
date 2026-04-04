package com.example.sewerverdict.web;

import java.util.List;
import java.util.Comparator;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import com.example.sewerverdict.content.PageFaq;
import com.example.sewerverdict.content.GeoProfileService;
import com.example.sewerverdict.content.SiteContentService;
import com.example.sewerverdict.content.SitePage;
import com.example.sewerverdict.content.SourceRegistryService;

@Controller
public class SiteController {

	private final SiteContentService siteContentService;
	private final SourceRegistryService sourceRegistryService;
	private final GeoProfileService geoProfileService;
	private final SeoMetadataService seoMetadataService;

	public SiteController(SiteContentService siteContentService, SourceRegistryService sourceRegistryService,
		GeoProfileService geoProfileService, SeoMetadataService seoMetadataService) {
		this.siteContentService = siteContentService;
		this.sourceRegistryService = sourceRegistryService;
		this.geoProfileService = geoProfileService;
		this.seoMetadataService = seoMetadataService;
	}

	@GetMapping("/")
	public String home(HttpServletRequest request, Model model) {
		model.addAttribute("pageTitle", "SewerClarity | Sewer lateral risk and next-step estimator");
		model.addAttribute("metaDescription",
			"Calm, buyer-first sewer lateral risk guidance for buyers, sellers, and owners. Clarify the next step, understand private-lateral responsibility, and know when inspection should come before quotes.");
		model.addAttribute("featuredPages", siteContentService.getFeaturedPages(List.of(
			"/sewer-scope-before-buying-house/",
			"/is-sewer-scope-worth-it/",
			"/who-pays-for-sewer-line-repair-buyer-or-seller/",
			"/homeowner-vs-city-sewer-responsibility/",
			"/sewer-lateral-repair-cost/",
			"/sewer-scope-red-flags/",
			"/sewer-scope-inspection-cost/"
		)));
		model.addAttribute("issuePages", siteContentService.getFeaturedPages(List.of(
			"/sewer-scope-red-flags/",
			"/cast-iron-sewer-pipe-replacement-cost/",
			"/orangeburg-pipe-replacement-cost/",
			"/cast-iron-pipe-deterioration-signs/",
			"/root-intrusion-sewer-line-what-to-do/"
		)));
		model.addAttribute("geoPages", siteContentService.getFeaturedPages(List.of(
			"/cities/philadelphia/homeowner-vs-city-sewer-responsibility/",
			"/cities/pittsburgh/sewer-scope-before-buying-house/",
			"/cities/milwaukee/homeowner-vs-city-sewer-responsibility/",
			"/cities/baltimore/homeowner-vs-city-sewer-responsibility/",
			"/cities/detroit/sewer-scope-before-buying-house/",
			"/cities/washington-dc/homeowner-vs-city-sewer-responsibility/"
		)));
		List<PageFaq> homeFaq = List.of(
			faq("What can the estimator do?", "It narrows the likely next step, rough cost direction, private-lateral responsibility questions, and biggest uncertainty drivers for buyers, sellers, and owners."),
			faq("Does it replace a sewer scope?", "No. It is an educational next-step tool, not a substitute for a sewer camera inspection or an in-person quote."),
			faq("Does the city usually pay for the lateral?", "No. Responsibility often depends on where the line sits, local utility rules, and whether the problem is on the private lateral or public side."),
			faq("Should buyers get quotes before they get better evidence?", "Usually not. Buyers often need clearer footage and a cleaner responsibility story before repair pricing becomes trustworthy.")
		);
		model.addAttribute("homeFaq", homeFaq);
		seoMetadataService.apply(model, request,
			"SewerClarity | Sewer lateral risk and next-step estimator",
			"Calm, buyer-first sewer lateral risk guidance for buyers, sellers, and owners. Clarify the next step, understand private-lateral responsibility, and know when inspection should come before quotes.",
			"website",
			List.of(new Breadcrumb("Home", "/")),
			homeFaq,
			true);
		return "home";
	}

	@GetMapping({"/cities", "/cities/"})
	public String cities(HttpServletRequest request, Model model) {
		List<SitePage> allPages = siteContentService.getAllPages();
		model.addAttribute("pageTitle", "Cities | SewerClarity");
		model.addAttribute("metaDescription",
			"City-specific sewer risk pages that connect national guides to local housing age, system context, and responsibility signals.");
		model.addAttribute("tierOneCities", geoProfileService.getCityHubEntriesByTier(allPages, "tier-1"));
		model.addAttribute("tierTwoCities", geoProfileService.getCityHubEntriesByTier(allPages, "tier-2"));
		seoMetadataService.apply(model, request,
			"Cities | SewerClarity",
			"City-specific sewer risk pages that connect national guides to local housing age, system context, and responsibility signals.",
			"website",
			List.of(
				new Breadcrumb("Home", "/"),
				new Breadcrumb("Cities", "/cities/")
			),
			List.of(),
			true);
		return "cities";
	}

	@GetMapping({"/cities/{city:[a-z0-9-]+}", "/cities/{city:[a-z0-9-]+}/"})
	public String cityHub(@PathVariable String city, HttpServletRequest request, Model model) {
		List<SitePage> allPages = siteContentService.getAllPages();
		var cityEntry = geoProfileService.getCityHubEntry(city, allPages);
		if (cityEntry == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		var starterPages = cityEntry.starterPages();
		var buyerPages = cityEntry.pages().stream().filter(SitePage::isBuyerPage).toList();
		var coveragePages = cityEntry.pages().stream().filter(SitePage::isCoveragePage).toList();
		var costPages = cityEntry.pages().stream().filter(SitePage::isCostPage).toList();
		var defectPages = cityEntry.pages().stream().filter(SitePage::isDefectPage).toList();
		var secondaryStarter = starterPages.isEmpty() ? null : starterPages.get(0);

		model.addAttribute("cityEntry", cityEntry);
		model.addAttribute("starterPages", starterPages);
		model.addAttribute("buyerPages", buyerPages);
		model.addAttribute("coveragePages", coveragePages);
		model.addAttribute("costPages", costPages);
		model.addAttribute("defectPages", defectPages);
		model.addAttribute("buyerStarter", firstPage(buyerPages));
		model.addAttribute("coverageStarter", firstPage(coveragePages));
		model.addAttribute("costStarter", firstPage(costPages));
		model.addAttribute("defectStarter", firstPage(defectPages));
		model.addAttribute("responsibilityRuleViews", geoProfileService.getResponsibilityRuleViews(city));
		model.addAttribute("geoProfileSources", geoProfileService.getProfileSources(city));
		model.addAttribute("cityHubSlug", "/cities/" + cityEntry.profile().getCitySlug() + "/");
		model.addAttribute("secondaryStarter", secondaryStarter);
		model.addAttribute("pageTitle",
			cityEntry.profile().getCityName() + ", " + cityEntry.profile().getStateCode() + " Sewer Pages | SewerClarity");
		model.addAttribute("metaDescription",
			"Start with the best sewer pages for " + cityEntry.profile().getCityName()
				+ ", from buyer-first inspection pages to private-lateral responsibility, backup, and quote-ready comparison guides.");
		seoMetadataService.apply(model, request,
			cityEntry.profile().getCityName() + ", " + cityEntry.profile().getStateCode() + " Sewer Pages | SewerClarity",
			"Start with the best sewer pages for " + cityEntry.profile().getCityName()
				+ ", from buyer-first inspection pages to private-lateral responsibility, backup, and quote-ready comparison guides.",
			"website",
			List.of(
				new Breadcrumb("Home", "/"),
				new Breadcrumb("Cities", "/cities/"),
				new Breadcrumb(cityEntry.profile().getCityName() + ", " + cityEntry.profile().getStateCode(),
					"/cities/" + cityEntry.profile().getCitySlug() + "/")
			),
			List.of(),
			true);
		return "city-hub";
	}

	@GetMapping({
		"/{slug:[a-z0-9-]+}",
		"/{slug:[a-z0-9-]+}/",
		"/cities/{city:[a-z0-9-]+}/{slug:[a-z0-9-]+}",
		"/cities/{city:[a-z0-9-]+}/{slug:[a-z0-9-]+}/"
	})
	public String page(HttpServletRequest request, Model model) {
		List<SitePage> allPages = siteContentService.getAllPages();
		SitePage page = siteContentService.requirePage(request.getRequestURI());
		List<Breadcrumb> breadcrumbs = buildBreadcrumbs(page);
		var pageSources = sourceRegistryService.getSourcesForPage(page);
		var geoProfileSources = geoProfileService.getProfileSources(page).stream()
			.filter(source -> pageSources.stream().noneMatch(existing -> existing.sourceId().equals(source.sourceId())))
			.toList();
		var cityHubEntry = page.isGeoPage() ? geoProfileService.getCityHubEntry(page.getGeoCitySlug(), allPages) : null;
		var cityStarterPages = cityHubEntry == null ? List.<SitePage>of() : cityHubEntry.starterPages().stream()
			.filter(candidate -> !candidate.getSlug().equals(page.getSlug()))
			.limit(3)
			.toList();
		var sameCityFamilyPages = cityHubEntry == null ? List.<SitePage>of() : cityHubEntry.pages().stream()
			.filter(candidate -> !candidate.getSlug().equals(page.getSlug()))
			.filter(candidate -> candidate.getTrackingFamily().equalsIgnoreCase(page.getTrackingFamily()))
			.limit(3)
			.toList();
		var familyClusterPages = page.isGeoPage() || page.getFamily() == null ? List.<SitePage>of() : siteContentService.getPagesByFamily(page.getFamily()).stream()
			.filter(candidate -> !candidate.getSlug().equals(page.getSlug()))
			.filter(candidate -> !candidate.isGeoPage())
			.sorted(Comparator
				.comparingInt(SitePage::getClusterRolePriority)
				.thenComparing(SitePage::getTitle))
			.limit(5)
			.toList();
		model.addAttribute("page", page);
		model.addAttribute("relatedPages", siteContentService.getRelatedPages(page));
		model.addAttribute("familyClusterPages", familyClusterPages);
		model.addAttribute("geoCompanionPages", geoProfileService.getGeoCompanionPages(page, allPages, 4));
		model.addAttribute("pageSources", pageSources);
		model.addAttribute("geoProfile", geoProfileService.getProfileForPage(page));
		model.addAttribute("geoProfileSources", geoProfileSources);
		model.addAttribute("responsibilityRuleViews", geoProfileService.getResponsibilityRuleViews(page));
		model.addAttribute("cityHubEntry", cityHubEntry);
		model.addAttribute("cityHubSlug", cityHubEntry == null ? null : "/cities/" + cityHubEntry.profile().getCitySlug() + "/");
		model.addAttribute("cityStarterPages", cityStarterPages);
		model.addAttribute("sameCityFamilyPages", sameCityFamilyPages);
		model.addAttribute("pageTitle", page.getMetaTitle());
		model.addAttribute("metaDescription", page.getMetaDescription());
		model.addAttribute("breadcrumbs", breadcrumbs);
		seoMetadataService.apply(model, request, page.getMetaTitle(), page.getMetaDescription(), "article", breadcrumbs,
			page.getFaq(), false, page.getLastReviewed());
		return "content-page";
	}

	private List<Breadcrumb> buildBreadcrumbs(SitePage page) {
		if (page.isGeoPage()) {
			var geoProfile = geoProfileService.getProfileForPage(page);
			if (geoProfile != null) {
				return List.of(
					new Breadcrumb("Home", "/"),
					new Breadcrumb("Cities", "/cities/"),
					new Breadcrumb(geoProfile.getCityName() + ", " + geoProfile.getStateCode(),
						"/cities/" + geoProfile.getCitySlug() + "/"),
					new Breadcrumb(page.getTitle(), page.getSlug())
				);
			}
			SitePage nationalCounterpart = siteContentService.getRelatedPages(page).stream().findFirst().orElse(null);
			return List.of(
				new Breadcrumb("Home", "/"),
				new Breadcrumb(nationalCounterpart != null ? nationalCounterpart.getTitle() : "National guide",
					nationalCounterpart != null ? nationalCounterpart.getSlug() : "/"),
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

	private SitePage firstPage(List<SitePage> pages) {
		return pages == null || pages.isEmpty() ? null : pages.get(0);
	}

	public record Breadcrumb(String label, String href) {
	}
}

