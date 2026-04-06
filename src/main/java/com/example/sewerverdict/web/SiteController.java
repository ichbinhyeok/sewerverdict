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
		model.addAttribute("pageTitle", "SewerClarity | Buyer-first sewer lateral decisions and next-step estimator");
		model.addAttribute("metaDescription",
			"Calm, buyer-first sewer lateral guidance for buyers, sellers, and owners. Choose inspection first when footage is missing, responsibility first when ownership is unclear, and interpretation first when a finding is already on the report.");
		model.addAttribute("featuredPages", siteContentService.getFeaturedPages(List.of(
			"/sewer-scope-before-buying-house/",
			"/homeowner-vs-city-sewer-responsibility/",
			"/sewer-scope-red-flags/",
			"/who-pays-for-sewer-line-repair-buyer-or-seller/",
			"/how-to-read-sewer-scope-report/",
			"/sewer-lateral-repair-cost/"
		)));
		model.addAttribute("issuePages", siteContentService.getFeaturedPages(List.of(
			"/sewer-scope-red-flags/",
			"/how-to-read-sewer-scope-report/",
			"/cast-iron-pipe-deterioration-signs/",
			"/root-intrusion-sewer-line-what-to-do/",
			"/sewer-lateral-repair-cost/"
		)));
		model.addAttribute("homeDecisionPaths", buildHomeDecisionPaths());
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
			"SewerClarity | Buyer-first sewer lateral decisions and next-step estimator",
			"Calm, buyer-first sewer lateral guidance for buyers, sellers, and owners. Choose inspection first when footage is missing, responsibility first when ownership is unclear, and interpretation first when a finding is already on the report.",
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
		var primaryStarter = firstPage(starterPages);
		var secondaryStarter = starterPages.size() > 1 ? starterPages.get(1) : null;
		var tertiaryStarter = starterPages.size() > 2 ? starterPages.get(2) : null;

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
		model.addAttribute("primaryStarter", primaryStarter);
		model.addAttribute("responsibilityRuleViews", geoProfileService.getResponsibilityRuleViews(city));
		model.addAttribute("geoProfileSources", geoProfileService.getProfileSources(city));
		model.addAttribute("cityHubSlug", "/cities/" + cityEntry.profile().getCitySlug() + "/");
		model.addAttribute("secondaryStarter", secondaryStarter);
		model.addAttribute("tertiaryStarter", tertiaryStarter);
		model.addAttribute("pageTitle",
			cityEntry.profile().getCityName() + ", " + cityEntry.profile().getStateCode() + " Sewer Pages | SewerClarity");
		model.addAttribute("metaDescription",
			"Start with the best sewer pages for " + cityEntry.profile().getCityName()
				+ ": buyer diligence first, private-lateral responsibility next, defect interpretation after that, and quote-ready comparison only when the evidence is stronger.");
		seoMetadataService.apply(model, request,
			cityEntry.profile().getCityName() + ", " + cityEntry.profile().getStateCode() + " Sewer Pages | SewerClarity",
			"Start with the best sewer pages for " + cityEntry.profile().getCityName()
				+ ": buyer diligence first, private-lateral responsibility next, defect interpretation after that, and quote-ready comparison only when the evidence is stronger.",
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
		model.addAttribute("heroDecisionPaths", buildHeroDecisionPaths(page));
		model.addAttribute("pageActionHeading", pageActionHeading(page));
		model.addAttribute("pageActionSummary", pageActionSummary(page));
		model.addAttribute("primaryCtaRoute", pageRouteForHref(page, page.getPrimaryCtaHref()));
		model.addAttribute("secondaryCtaRoute", pageRouteForHref(page, page.getSecondaryCtaHref()));
		model.addAttribute("primaryCtaTarget", targetForHref(page.getPrimaryCtaHref()));
		model.addAttribute("secondaryCtaTarget", targetForHref(page.getSecondaryCtaHref()));
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

	private List<DecisionPath> buildHomeDecisionPaths() {
		return List.of(
			decisionPath(
				"No footage yet",
				"Inspection first before credits, quotes, or waiver decisions",
				"Use the buyer path when the line is still an unknown and the next valuable move is better evidence before closing.",
				"/sewer-scope-before-buying-house/",
				"Start with buyer diligence"),
			decisionPath(
				"Boundary or ownership unclear",
				"Responsibility first before you price or blame the line",
				"Use the responsibility path when the real question is private lateral ownership, seller leverage, or a city-boundary assumption.",
				"/homeowner-vs-city-sewer-responsibility/",
				"Clarify responsibility"),
			decisionPath(
				"Report finding or known defect",
				"Interpret the finding before you decide to monitor, clarify, or quote",
				"Use the finding path when roots, cast iron, orangeburg, backup clues, or report language are already on the table.",
				"/sewer-scope-red-flags/",
				"Interpret the finding")
		);
	}

	private List<DecisionPath> buildHeroDecisionPaths(SitePage page) {
		if (page.isCoveragePage()) {
			return List.of(
				decisionPath(
					"Boundary still unclear",
					"Use local responsibility context before you assume who pays",
					"Start with owner-versus-city rules, then decide whether the next call is utility, inspection, claim, or quote comparison.",
					"/cities/",
					"See city responsibility pages"),
				decisionPath(
					"Buying or selling the home",
					"Switch to leverage and responsibility inside the deal",
					"Use the buyer-versus-seller page when the boundary question is tied to credits, repairs, or whether the seller has to move.",
					"/who-pays-for-sewer-line-repair-buyer-or-seller/",
					"See buyer vs seller leverage"),
				decisionPath(
					"Owner-side lateral looks likely",
					"Only move into cost once private-lateral exposure is real",
					"Use the narrower private-lateral cost page once the owner-side story is strong enough to compare repair versus replacement paths.",
					"/sewer-lateral-repair-cost/",
					"See private lateral cost")
			);
		}
		if (page.isDefectPage()) {
			return List.of(
				decisionPath(
					"Monitor vs clarify",
					"Interpret the wording before you call it failure",
					"Use the report-reading path when the finding sounds scary but you still need to separate watch-items from quote-ready defects.",
					"/how-to-read-sewer-scope-report/",
					"Read the report calmly"),
				decisionPath(
					"Evidence still weak",
					"Get clearer footage before you chase big numbers",
					"Use the inspection path if the current video, report note, or symptom is too thin to support confident repair pricing.",
					"/find-sewer-scope/",
					"Find sewer camera inspection options"),
				decisionPath(
					"Quote-ready defect",
					"Move into quotes only when the finding already looks real",
					"Use the quote path when the footage points to a systemic problem, a blocked flow path, or a repair decision that now needs bids.",
					"/get-sewer-quotes/",
					"Move into quote-ready follow-up")
			);
		}
		if (page.isCostPage()) {
			return List.of(
				decisionPath(
					"No footage yet",
					"Do not price a maybe-problem before you know the line story",
					"Use inspection first when the cost question is still running ahead of footage, location, or evidence strength.",
					"/find-sewer-scope/",
					"Get inspection options first"),
				decisionPath(
					"Boundary still fuzzy",
					"Clarify private-lateral responsibility before owner-side quotes",
					"Use the responsibility path if the repair may not be yours, the city boundary is unclear, or seller leverage still matters.",
					"/homeowner-vs-city-sewer-responsibility/",
					"Clarify who owns the line"),
				decisionPath(
					"Private lateral confirmed",
					"Compare quotes only when the owner-side repair path is real",
					"Use the quote path once footage, access, and owner-side responsibility are strong enough to compare repair or replacement bids.",
					"/get-sewer-quotes/",
					"Get sewer repair or replacement quotes")
			);
		}
		return List.of(
			decisionPath(
				"No footage yet",
				"Inspection first before credits, quotes, or repair arguments",
				"Use the inspection path when the sewer line is still an unknown and better evidence will change what the next decision should be.",
				"/find-sewer-scope/",
				"Find sewer camera inspection options"),
			decisionPath(
				"Boundary or leverage unclear",
				"Responsibility first before you negotiate or blame the city",
				"Use the responsibility path when private-lateral exposure, seller leverage, or the owner-versus-city line boundary still needs a cleaner read.",
				page.getSlug().contains("who-pays")
					? "/homeowner-vs-city-sewer-responsibility/"
					: "/who-pays-for-sewer-line-repair-buyer-or-seller/",
				page.getSlug().contains("who-pays")
					? "Clarify homeowner vs city responsibility"
					: "See buyer vs seller leverage"),
			decisionPath(
				"Finding already in hand",
				"Interpret the footage before you turn it into a quote fight",
				"Use the interpretation path when roots, bellies, cast iron, or another finding already exists but the meaning still needs calmer context.",
				"/sewer-scope-red-flags/",
				"Interpret the finding")
		);
	}

	private String pageActionHeading(SitePage page) {
		if (page.isBuyerPage()) {
			return "Choose the evidence-first next move";
		}
		if (page.isCoveragePage()) {
			return "Clarify the boundary before you price or blame the line";
		}
		if (page.isDefectPage()) {
			return "Interpret the finding before you treat it like a replacement verdict";
		}
		if (page.isCostPage()) {
			return "Compare quotes only after the private-lateral story is strong enough";
		}
		return "Choose the next move before you start pricing the problem";
	}

	private String pageActionSummary(SitePage page) {
		if (page.isBuyerPage()) {
			return "Use this page to decide whether the next move is inspection, responsibility clarification, or finding interpretation before quotes and credits start driving the conversation.";
		}
		if (page.isCoveragePage()) {
			return "Use this page to choose whether the next move is local responsibility checking, buyer-versus-seller leverage, or a narrower private-lateral cost read once ownership is clearer.";
		}
		if (page.isDefectPage()) {
			return "Use this page to sort watch-items from clarify-first findings and quote-ready defects without treating every scary phrase like immediate replacement.";
		}
		if (page.isCostPage()) {
			return "Use this page once owner-side responsibility and the line condition are real enough to compare repair, replacement, or quote-ready follow-up without generic cost-site guessing.";
		}
		return "Use this page to choose whether the next move is inspection, responsibility clarification, or interpretation before quotes and credits start driving the conversation.";
	}

	private DecisionPath decisionPath(String eyebrow, String title, String summary, String href, String ctaLabel) {
		return new DecisionPath(eyebrow, title, summary, href, ctaLabel, routeForHref(href), targetForHref(href));
	}

	private String routeForHref(String href) {
		if (href == null) {
			return "needs-clarification";
		}
		String normalized = href.toLowerCase();
		if (normalized.startsWith("/find-sewer-scope")) {
			return "inspection-first";
		}
		if (normalized.startsWith("/get-sewer-quotes")) {
			return "quote-ready";
		}
		if (normalized.startsWith("/estimator")) {
			return "needs-clarification";
		}
		if (normalized.equals("/cities/") || normalized.contains("homeowner-vs-city") || normalized.contains("who-pays")) {
			return "responsibility-first";
		}
		if (normalized.contains("scope-report") || normalized.contains("red-flags")) {
			return "interpretation-first";
		}
		if (normalized.contains("before-buying") || normalized.contains("negotiation-with-seller")
			|| normalized.contains("point-of-sale") || normalized.contains("certificate")
			|| normalized.contains("compliance")) {
			return "inspection-first";
		}
		if (normalized.contains("repair-cost") || normalized.contains("repair-vs-replacement")
			|| normalized.contains("replacement-cost") || normalized.contains("lateral-repair-cost")) {
			return "quote-ready";
		}
		return "needs-clarification";
	}

	private String targetForHref(String href) {
		if (href == null) {
			return "page";
		}
		String normalized = href.toLowerCase();
		if (normalized.startsWith("/find-sewer-scope")) {
			return "inspection-route";
		}
		if (normalized.startsWith("/get-sewer-quotes")) {
			return "lead-route";
		}
		if (normalized.startsWith("/estimator")) {
			return "estimator";
		}
		if (normalized.equals("/cities/")) {
			return "city-hub";
		}
		if (normalized.contains("homeowner-vs-city") || normalized.contains("who-pays")) {
			return "responsibility-page";
		}
		if (normalized.contains("scope-report") || normalized.contains("red-flags")) {
			return "defect-page";
		}
		if (normalized.contains("before-buying") || normalized.contains("negotiation-with-seller")) {
			return "buyer-page";
		}
		if (normalized.contains("repair-cost") || normalized.contains("repair-vs-replacement")
			|| normalized.contains("replacement-cost") || normalized.contains("lateral-repair-cost")) {
			return "quote-page";
		}
		return "page";
	}

	private String pageRouteForHref(SitePage page, String href) {
		if (href == null) {
			return page.getRecommendedRouteBucket();
		}
		if (href.toLowerCase().startsWith("/estimator")) {
			return page.getRecommendedRouteBucket();
		}
		return routeForHref(href);
	}

	public record Breadcrumb(String label, String href) {
	}

	public record DecisionPath(String eyebrow, String title, String summary, String href, String ctaLabel,
							   String route, String target) {
	}
}

