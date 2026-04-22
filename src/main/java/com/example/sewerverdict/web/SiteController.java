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
		model.addAttribute("pageTitle", "SewerClarity | Sewer scope, sewer findings, and next-step cost guidance");
		model.addAttribute("metaDescription",
			"Inspection-first sewer risk guidance for buyers, sellers, and owners. Decide whether you need a sewer scope, what a finding likely means, and when a sewer issue is real enough to compare costs or quotes.");
		model.addAttribute("featuredPages", siteContentService.getFeaturedPages(List.of(
			"/sewer-scope-before-buying-house/",
			"/is-sewer-scope-worth-it/",
			"/how-to-read-sewer-scope-report/",
			"/sewer-scope-red-flags/",
			"/sewer-line-replacement-cost/",
			"/cast-iron-sewer-pipe-replacement-cost/"
		)));
		model.addAttribute("issuePages", siteContentService.getFeaturedPages(List.of(
			"/orangeburg-pipe-replacement-cost/",
			"/root-intrusion-sewer-line-what-to-do/",
			"/sewer-line-under-slab-repair-cost/",
			"/trenchless-vs-traditional-sewer-line-replacement/"
		)));
		model.addAttribute("homeDecisionPaths", buildHomeDecisionPaths());
		model.addAttribute("geoPages", siteContentService.getFeaturedPages(List.of(
			"/cities/milwaukee/sewer-backup-risk/",
			"/cities/philadelphia/homeowner-vs-city-sewer-responsibility/",
			"/cities/chicago/cast-iron-sewer-line-risk/",
			"/cities/buffalo/old-house-sewer-line-risk/"
		)));
		List<PageFaq> homeFaq = List.of(
			faq("What can the estimator do?", "It narrows the likely next step, rough cost direction, and biggest uncertainty drivers for buyers, sellers, and owners without pretending to diagnose the line."),
			faq("Does it replace a sewer scope?", "No. It is an educational next-step tool, not a substitute for a sewer camera inspection or an in-person quote."),
			faq("Does local city context matter on every sewer decision?", "No. SewerClarity only leans hard on city-specific ownership, program, or transfer angles where the local signal is real and source-backed."),
			faq("Should buyers or owners compare quotes before they understand the finding?", "Usually not. Better footage and calmer interpretation usually improve the next decision before repair pricing becomes trustworthy.")
		);
		model.addAttribute("homeFaq", homeFaq);
		configureSurfaceRouter(model,
			"Start with the tool, not the whole guide library",
			"Choose the situation, your role, and the property city or ZIP. SewerClarity will reopen the estimator at the first missing step instead of making you browse for the right article first.",
			"/",
			"home-surface-router",
			null,
			"Philadelphia, PA or 19147",
			null,
			null,
			null,
			"Start my next-step check");
		seoMetadataService.apply(model, request,
			"SewerClarity | Sewer scope, sewer findings, and next-step cost guidance",
			"Inspection-first sewer risk guidance for buyers, sellers, and owners. Decide whether you need a sewer scope, what a finding likely means, and when a sewer issue is real enough to compare costs or quotes.",
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
			"City-specific sewer pages that add local housing, system, or ownership context only where local signals materially change buyer, finding, or cost decisions.");
		model.addAttribute("tierOneCities", geoProfileService.getCityHubEntriesByTier(allPages, "tier-1"));
		model.addAttribute("tierTwoCities", geoProfileService.getCityHubEntriesByTier(allPages, "tier-2"));
		configureSurfaceRouter(model,
			"Use a city only if it changes the call",
			"Start the estimator first when the question is still broad. Add a city or ZIP only when local housing, system, or responsibility context really changes the next move.",
			"/cities/",
			"cities-surface-router",
			null,
			"Philadelphia, PA or 19147",
			null,
			null,
			null,
			"Start with the estimator");
		seoMetadataService.apply(model, request,
			"Cities | SewerClarity",
			"City-specific sewer pages that add local housing, system, or ownership context only where local signals materially change buyer, finding, or cost decisions.",
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
		var transferPages = cityEntry.pages().stream()
			.filter(SitePage::isTransferPage)
			.sorted(Comparator.comparingInt(this::transferPriority).thenComparing(SitePage::getTitle))
			.toList();
		var compliancePages = cityEntry.pages().stream()
			.filter(SitePage::isCompliancePage)
			.sorted(Comparator.comparingInt(this::compliancePriority).thenComparing(SitePage::getTitle))
			.toList();
		var costPages = cityEntry.pages().stream().filter(SitePage::isCostPage).toList();
		var defectPages = cityEntry.pages().stream().filter(SitePage::isDefectPage).toList();
		var primaryStarter = firstPage(starterPages);
		var secondaryStarter = starterPages.size() > 1 ? starterPages.get(1) : null;
		var tertiaryStarter = starterPages.size() > 2 ? starterPages.get(2) : null;

		model.addAttribute("cityEntry", cityEntry);
		model.addAttribute("starterPages", starterPages);
		model.addAttribute("buyerPages", buyerPages);
		model.addAttribute("coveragePages", coveragePages);
		model.addAttribute("transferPages", transferPages);
		model.addAttribute("compliancePages", compliancePages);
		model.addAttribute("costPages", costPages);
		model.addAttribute("defectPages", defectPages);
		model.addAttribute("buyerStarter", firstPage(buyerPages));
		model.addAttribute("coverageStarter", firstPage(coveragePages));
		model.addAttribute("transferStarter", firstPage(transferPages));
		model.addAttribute("complianceStarter", firstPage(compliancePages));
		model.addAttribute("costStarter", firstPage(costPages));
		model.addAttribute("defectStarter", firstPage(defectPages));
		model.addAttribute("primaryStarter", primaryStarter);
		model.addAttribute("responsibilityRuleViews", geoProfileService.getResponsibilityRuleViews(city));
		model.addAttribute("geoProfileSources", geoProfileService.getProfileSources(city));
		model.addAttribute("transferSignalSource", geoProfileService.getProfileSources(city).stream()
			.filter(source -> source.topicArea() != null && source.topicArea().contains("transfer"))
			.findFirst()
			.orElse(null));
		model.addAttribute("cityHubSlug", "/cities/" + cityEntry.profile().getCitySlug() + "/");
		model.addAttribute("secondaryStarter", secondaryStarter);
		model.addAttribute("tertiaryStarter", tertiaryStarter);
		model.addAttribute("pageTitle",
			cityEntry.profile().getCityName() + ", " + cityEntry.profile().getStateCode() + " Sewer Pages | SewerClarity");
		model.addAttribute("metaDescription",
			"Start with the best sewer pages for " + cityEntry.profile().getCityName()
				+ ": buyer diligence and inspection-first guidance first, finding interpretation next, cost comparison after that, and local responsibility context only where the city signal materially changes the call.");
		configureSurfaceRouter(model,
			"Get the next move with " + cityEntry.profile().getCityName() + " context already carried forward",
			"Use the tool before you browse the whole local cluster. The estimator will start with this city context in place, then continue from the first missing step.",
			"/cities/" + cityEntry.profile().getCitySlug() + "/",
			"city-hub-surface-router",
			cityEntry.profile().getCityName() + ", " + cityEntry.profile().getStateCode(),
			"Philadelphia, PA or 19147",
			null,
			null,
			null,
			"Start with local context");
		seoMetadataService.apply(model, request,
			cityEntry.profile().getCityName() + ", " + cityEntry.profile().getStateCode() + " Sewer Pages | SewerClarity",
			"Start with the best sewer pages for " + cityEntry.profile().getCityName()
				+ ": buyer diligence and inspection-first guidance first, finding interpretation next, cost comparison after that, and local responsibility context only where the city signal materially changes the call.",
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
		var geoProfile = geoProfileService.getProfileForPage(page);
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
		model.addAttribute("geoCompanionPages", geoProfileService.getGeoCompanionPages(page, allPages, 3));
		model.addAttribute("pageSources", pageSources);
		model.addAttribute("geoProfile", geoProfile);
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
		if (!page.isTrustPage()) {
			configureSurfaceRouter(model,
				page.isGeoPage()
					? "Start with your case, not the whole "
						+ (geoProfile != null ? geoProfile.getCityName() : "city")
						+ " cluster"
					: "Use this page as context, then start the tool",
				page.isGeoPage()
					? "This page already tells you the local angle. Start the estimator with that city context in place instead of reading the whole cluster before you act."
					: "This page gives the context, but the product value is the next-step call. Start the estimator with this page's likely issue state already carried forward.",
				page.getSlug(),
				"page-surface-router",
				routerLocationValue(page),
				"Philadelphia, PA or 19147",
				routerIssueState(page),
				routerRole(page),
				routerDefectType(page),
				"Start the next-step check");
		}
		seoMetadataService.apply(model, request, page.getMetaTitle(), page.getMetaDescription(),
			page.isTrustPage() ? "article" : "website", breadcrumbs,
			page.getFaq(), false, page.isTrustPage() ? page.getLastReviewed() : null);
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
		if (page.isTransferPage()) {
			return List.of(
				new Breadcrumb("Home", "/"),
				new Breadcrumb("Transfer and Closing", "/sewer-scope-before-buying-house/"),
				new Breadcrumb(page.getTitle(), page.getSlug())
			);
		}
		if (page.isCompliancePage()) {
			return List.of(
				new Breadcrumb("Home", "/"),
				new Breadcrumb("Compliance and Responsibility", "/homeowner-vs-city-sewer-responsibility/"),
				new Breadcrumb(page.getTitle(), page.getSlug())
			);
		}
		if (page.isCoveragePage()) {
			return List.of(
				new Breadcrumb("Home", "/"),
				new Breadcrumb("Coverage and Insurance", "/does-home-insurance-cover-sewer-line-replacement/"),
				new Breadcrumb(page.getTitle(), page.getSlug())
			);
		}
		if (page.isCostPage()) {
			return List.of(
				new Breadcrumb("Home", "/"),
				new Breadcrumb("Costs", "/sewer-line-replacement-cost/"),
				new Breadcrumb(page.getTitle(), page.getSlug())
			);
		}
		if (page.isDefectPage()) {
			return List.of(
				new Breadcrumb("Home", "/"),
				new Breadcrumb("Defects", "/sewer-scope-red-flags/"),
				new Breadcrumb(page.getTitle(), page.getSlug())
			);
		}
		if (page.isTrustPage()) {
			return List.of(
				new Breadcrumb("Home", "/"),
				new Breadcrumb("Methodology", "/methodology/"),
				new Breadcrumb(page.getTitle(), page.getSlug())
			);
		}
		return List.of(new Breadcrumb("Home", "/"), new Breadcrumb(page.getTitle(), page.getSlug()));
	}

	private PageFaq faq(String question, String answer) {
		PageFaq item = new PageFaq();
		item.setQuestion(question);
		item.setAnswer(answer);
		return item;
	}

	private void configureSurfaceRouter(Model model, String title, String summary, String pageSlug, String placement,
		String locationValue, String locationPlaceholder, String selectedIssueState, String selectedRole,
		String selectedDefectType, String primaryLabel) {
		model.addAttribute("surfaceRouterTitle", title);
		model.addAttribute("surfaceRouterSummary", summary);
		model.addAttribute("surfaceRouterPageSlug", pageSlug);
		model.addAttribute("surfaceRouterPlacement", placement);
		model.addAttribute("surfaceRouterLocationValue", locationValue);
		model.addAttribute("surfaceRouterLocationPlaceholder", locationPlaceholder);
		model.addAttribute("surfaceRouterSelectedIssueState", selectedIssueState);
		model.addAttribute("surfaceRouterSelectedRole", selectedRole);
		model.addAttribute("surfaceRouterSelectedDefectType", selectedDefectType);
		model.addAttribute("surfaceRouterPrimaryLabel", primaryLabel);
	}

	private SitePage firstPage(List<SitePage> pages) {
		return pages == null || pages.isEmpty() ? null : pages.get(0);
	}

	private String routerLocationValue(SitePage page) {
		if (page == null || !page.isGeoPage()) {
			return null;
		}
		var geoProfile = geoProfileService.getProfileForPage(page);
		return geoProfile == null ? null : geoProfile.getCityName() + ", " + geoProfile.getStateCode();
	}

	private String routerIssueState(SitePage page) {
		if (page == null) {
			return null;
		}
		if (page.isTransferPage() || page.isBuyerPage()) {
			return "no-scope-yet";
		}
		if (page.isDefectPage() || page.isCostPage()) {
			return "scope-found-issue";
		}
		return null;
	}

	private String routerRole(SitePage page) {
		if (page == null) {
			return null;
		}
		if (page.isTransferPage() || page.isBuyerPage()) {
			return "buyer";
		}
		if (page.isDefectPage() || page.isCostPage()) {
			return "owner";
		}
		return null;
	}

	private String routerDefectType(SitePage page) {
		if (page == null || page.getSlug() == null) {
			return null;
		}
		String slug = page.getSlug().toLowerCase();
		if (slug.contains("root")) {
			return "roots";
		}
		if (slug.contains("cast-iron")) {
			return "cast-iron";
		}
		if (slug.contains("orangeburg")) {
			return "orangeburg";
		}
		if (slug.contains("belly")) {
			return "belly";
		}
		if (slug.contains("offset")) {
			return "offset-crack";
		}
		if (slug.contains("collapse")) {
			return "collapse";
		}
		return null;
	}

	private int transferPriority(SitePage page) {
		String slug = page.getSlug() == null ? "" : page.getSlug().toLowerCase();
		if (slug.contains("point-of-sale") || slug.contains("certificate") || slug.contains("required-inspection")) {
			return 0;
		}
		if (slug.contains("before-buying-house")) {
			return 1;
		}
		if (slug.contains("negotiation-with-seller")) {
			return 2;
		}
		if (slug.contains("buyer-or-seller")) {
			return 3;
		}
		return 9;
	}

	private int compliancePriority(SitePage page) {
		String slug = page.getSlug() == null ? "" : page.getSlug().toLowerCase();
		if (slug.contains("compliance")) {
			return 0;
		}
		if (slug.contains("homeowner-vs-city")) {
			return 1;
		}
		if (slug.contains("wet-weather")) {
			return 2;
		}
		return 9;
	}

	private List<DecisionPath> buildHomeDecisionPaths() {
		return List.of(
			decisionPath(
				"No footage yet",
				"Start with sewer scope before you negotiate, waive, or guess.",
				"Use the buyer and inspection path when the line has not been camera-confirmed and the next smart move is better evidence, not early pricing noise.",
				"/sewer-scope-before-buying-house/",
				"Start with inspection-first guidance"),
			decisionPath(
				"Report note or finding in hand",
				"Read the finding before you call it failure.",
				"Use the interpretation path when roots, cast iron, orangeburg, bellies, or backup language need calmer context before you turn them into quotes or credits.",
				"/how-to-read-sewer-scope-report/",
				"Interpret the finding"),
			decisionPath(
				"Known issue and money question",
				"Compare cost direction only when the problem looks real.",
				"Use the cost path once the issue is documented enough to compare repair, replacement, trenchless, or under-slab scenarios without generic guessing.",
				"/sewer-line-replacement-cost/",
				"See cost direction")
		);
	}

	private List<DecisionPath> buildHeroDecisionPaths(SitePage page) {
		if (page.isCompliancePage()) {
			return List.of(
				decisionPath(
					"No footage yet",
					"Start with inspection evidence before you lean on a local rule",
					"Use the buyer and inspection path when a local boundary note exists but the line itself is still not documented clearly enough to price or negotiate around.",
					"/sewer-scope-before-buying-house/",
					"Use inspection-first guidance"),
				decisionPath(
					"Finding already exists",
					"Read the finding before you let ownership language do all the work",
					"Use the interpretation path when the city rule matters less than understanding whether the footage shows a watch-item, a localized repair, or a broader failure pattern.",
					"/how-to-read-sewer-scope-report/",
					"Interpret the finding"),
				decisionPath(
					"Known issue and money question",
					"Move into cost direction only after the owner-side issue looks real",
					"Use the cost path when the line condition and owner-side exposure are strong enough to compare repair, replacement, or trenchless paths without generic guessing.",
					"/sewer-line-replacement-cost/",
					"See cost direction")
			);
		}
		if (page.isTransferPage()) {
			return List.of(
				decisionPath(
					"No footage yet",
					"Inspection first before credits, waivers, or seller asks",
					"Use the inspection path when the line is still not documented clearly enough for repair pricing, seller concessions, or closing pressure to be the main story.",
					"/find-sewer-scope/",
					"Find sewer camera inspection options"),
				decisionPath(
					"Finding or report note already exists",
					"Read the scope language before you price the problem",
					"Use the interpretation path when the buyer or seller conversation depends on what the footage really supports, not on the scariest phrase in the report.",
					"/how-to-read-sewer-scope-report/",
					"Read the scope calmly"),
				decisionPath(
					"Known issue and money question",
					"Use cost guidance only when the problem looks real",
					"Use the cost path when the line condition is documented enough to compare repair, replacement, or trenchless direction without generic transaction noise.",
					"/sewer-line-replacement-cost/",
					"See cost direction")
			);
		}
		if (page.isDefectPage()) {
			boolean reportPage = page.getSlug() != null && page.getSlug().contains("scope-report");
			return List.of(
				decisionPath(
					"Interpret the wording",
					"Calm the finding before you call it replacement",
					"Use the broader interpretation path when you need to separate watch-items from truly quote-ready defects before the price discussion takes over.",
					reportPage ? "/sewer-scope-red-flags/" : "/how-to-read-sewer-scope-report/",
					reportPage ? "See common red flags" : "Read the scope calmly"),
				decisionPath(
					"Evidence still weak",
					"Get clearer footage before you chase big numbers",
					"Use the inspection path if the current video, report note, or symptom is too thin to support confident repair pricing.",
					"/find-sewer-scope/",
					"Find sewer camera inspection options"),
				decisionPath(
					"Known issue and money question",
					"Move into cost direction when the finding already looks real",
					"Use the cost path when the footage points toward a broader problem and you need calmer repair-versus-replacement direction before quote comparison.",
					"/sewer-line-replacement-cost/",
					"See cost direction")
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
					"Finding meaning still unclear",
					"Read the finding before you trust the price range",
					"Use the interpretation path when the money question is live but the footage still needs calmer context before repair-versus-replacement decisions harden.",
					"/how-to-read-sewer-scope-report/",
					"Read the scope calmly"),
				decisionPath(
					"Quote-ready issue",
					"Compare quotes only when the owner-side repair path is real",
					"Use the quote path once footage, access, and owner-side responsibility are strong enough to compare repair or replacement bids.",
					"/get-sewer-quotes/",
					"Get sewer repair or replacement quotes")
			);
		}
		return List.of(
			decisionPath(
				"No footage yet",
				"Start with inspection-first guidance before you guess",
				"Use the buyer and inspection path when the sewer line is still an unknown and better evidence will change what the next decision should be.",
				"/sewer-scope-before-buying-house/",
				"Use inspection-first guidance"),
			decisionPath(
				"Finding already in hand",
				"Read the scope before you turn it into a quote fight",
				"Use the interpretation path when roots, bellies, cast iron, or another finding already exists but the meaning still needs calmer context.",
				"/how-to-read-sewer-scope-report/",
				"Read the scope calmly"),
			decisionPath(
				"Known issue and money question",
				"Move into cost direction only when the problem looks real",
				"Use the cost path when the line story is strong enough to compare repair or replacement direction without relying on generic numbers too early.",
				"/sewer-line-replacement-cost/",
				"See cost direction")
		);
	}

	private String pageActionHeading(SitePage page) {
		if (page.isTransferPage()) {
			return "Choose the transfer-safe next move before you negotiate, waive, or promise repairs";
		}
		if (page.isCompliancePage()) {
			return "Clarify the local boundary before you price, blame, or promise anything";
		}
		if (page.isCoveragePage()) {
			return "Reality-check coverage assumptions before you count on reimbursement";
		}
		if (page.isBuyerPage()) {
			return "Choose the evidence-first next move";
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
		if (page.isTransferPage()) {
			return "Use this page to decide whether the next move is city-rule checking, inspection, responsibility clarification, or report interpretation before credits and repair promises start driving the conversation.";
		}
		if (page.isCompliancePage()) {
			return "Use this page to choose whether the next move is local responsibility checking, transfer-path clarification, utility contact, or a narrower owner-side cost read once ownership is clearer.";
		}
		if (page.isCoveragePage()) {
			return "Use this page to decide whether the next move is policy review, a narrower owner-side estimate, or a better evidence trail before you assume a sewer claim or service-line product will pay.";
		}
		if (page.isBuyerPage()) {
			return "Use this page to decide whether the next move is inspection, responsibility clarification, or finding interpretation before quotes and credits start driving the conversation.";
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
		if (normalized.equals("/cities/") || normalized.contains("homeowner-vs-city")) {
			return "responsibility-first";
		}
		if (normalized.contains("scope-report") || normalized.contains("red-flags")) {
			return "interpretation-first";
		}
		if (normalized.contains("before-buying") || normalized.contains("negotiation-with-seller")
			|| normalized.contains("buyer-or-seller")
			|| normalized.contains("point-of-sale") || normalized.contains("certificate")
			|| normalized.contains("required-inspection")) {
			return "inspection-first";
		}
		if (normalized.contains("compliance") || normalized.contains("wet-weather")) {
			return "responsibility-first";
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
		if (normalized.contains("before-buying") || normalized.contains("negotiation-with-seller")
			|| normalized.contains("buyer-or-seller")
			|| normalized.contains("point-of-sale") || normalized.contains("certificate")
			|| normalized.contains("required-inspection")) {
			return "transfer-page";
		}
		if (normalized.contains("compliance")
			|| normalized.contains("homeowner-vs-city") || normalized.contains("wet-weather")) {
			return "compliance-page";
		}
		if (normalized.contains("scope-report") || normalized.contains("red-flags")) {
			return "defect-page";
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

