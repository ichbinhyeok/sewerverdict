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
		model.addAttribute("pageTitle", "SewerClarity | City sewer lateral transfer and compliance next steps");
		model.addAttribute("metaDescription",
			"City-specific sewer lateral transfer, compliance, and next-step guidance for buyers, sellers, and owners. Start with official local signals, then move into inspection, responsibility, or report interpretation.");
		model.addAttribute("featuredPages", siteContentService.getFeaturedPages(List.of(
			"/sewer-scope-before-buying-house/",
			"/who-pays-for-sewer-line-repair-buyer-or-seller/",
			"/homeowner-vs-city-sewer-responsibility/",
			"/sewer-scope-negotiation-with-seller/",
			"/how-to-read-sewer-scope-report/",
			"/sewer-scope-red-flags/"
		)));
		model.addAttribute("issuePages", siteContentService.getFeaturedPages(List.of(
			"/sewer-scope-red-flags/",
			"/how-to-read-sewer-scope-report/",
			"/cast-iron-sewer-pipe-replacement-cost/",
			"/orangeburg-pipe-replacement-cost/",
			"/root-intrusion-sewer-line-what-to-do/"
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
			faq("What can the estimator do?", "It narrows the likely next step, rough cost direction, transfer or responsibility questions, and biggest uncertainty drivers for buyers, sellers, and owners."),
			faq("Does it replace a sewer scope?", "No. It is an educational next-step tool, not a substitute for a sewer camera inspection or an in-person quote."),
			faq("Do city transfer or compliance rules matter everywhere?", "No. SewerClarity only leans hard on city-specific transfer, ownership, or program angles where the local signal is real and source-backed."),
			faq("Should buyers get quotes before they get better evidence?", "Usually not. Buyers and sellers often need clearer footage and a cleaner responsibility story before repair pricing becomes trustworthy.")
		);
		model.addAttribute("homeFaq", homeFaq);
		seoMetadataService.apply(model, request,
			"SewerClarity | City sewer lateral transfer and compliance next steps",
			"City-specific sewer lateral transfer, compliance, and next-step guidance for buyers, sellers, and owners. Start with official local signals, then move into inspection, responsibility, or report interpretation.",
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
				+ ": transfer and closing pages first, official responsibility or program context next, defect interpretation after that, and quote-ready comparison only when the evidence is stronger.");
		seoMetadataService.apply(model, request,
			cityEntry.profile().getCityName() + ", " + cityEntry.profile().getStateCode() + " Sewer Pages | SewerClarity",
			"Start with the best sewer pages for " + cityEntry.profile().getCityName()
				+ ": transfer and closing pages first, official responsibility or program context next, defect interpretation after that, and quote-ready comparison only when the evidence is stronger.",
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

	private SitePage firstPage(List<SitePage> pages) {
		return pages == null || pages.isEmpty() ? null : pages.get(0);
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
				"Transfer or closing pressure",
				"Start with the city path before you guess at certificates, required inspection, or seller promises",
				"Use the city hub when official local signals may change whether the next smart move is inspection, responsibility clarification, or a narrower transaction ask.",
				"/cities/",
				"Open city transfer paths"),
			decisionPath(
				"Buyer, seller, or owner exposure unclear",
				"Clarify who is likely carrying the line risk before you negotiate or price it",
				"Use the transaction and responsibility path when private-lateral exposure, seller leverage, or local boundary language still need a calmer read.",
				"/who-pays-for-sewer-line-repair-buyer-or-seller/",
				"Clarify transfer risk"),
			decisionPath(
				"Report finding or known defect",
				"Read the scope language before you turn the transfer into a quote fight",
				"Use the report-reading path when roots, cast iron, orangeburg, backup clues, or report language are already on the table and the next ask depends on what the finding really means.",
				"/how-to-read-sewer-scope-report/",
				"Read the scope calmly")
		);
	}

	private List<DecisionPath> buildHeroDecisionPaths(SitePage page) {
		if (page.isCompliancePage()) {
			return List.of(
				decisionPath(
					"City rule or program still matters",
					"Use local compliance and responsibility context before you assume the transfer path",
					"Start with city-specific ownership, program, or boundary language, then decide whether the next call is utility, inspection, seller negotiation, or quote comparison.",
					"/cities/",
					"See city compliance pages"),
				decisionPath(
					"Buying or selling the home",
					"Switch to transfer leverage once the boundary story is clearer",
					"Use the buyer-versus-seller page when the boundary question is tied to credits, repairs, certificates, or whether the seller has to move.",
					"/who-pays-for-sewer-line-repair-buyer-or-seller/",
					"See buyer vs seller leverage"),
				decisionPath(
					"No footage yet",
					"Keep the next move inspection-first until the transfer story has evidence behind it",
					"Use the buyer and inspection path when the city rule is clearer than the actual line condition and you still need better footage before asking for money or promises.",
					"/sewer-scope-before-buying-house/",
					"Use the transfer inspection path")
			);
		}
		if (page.isTransferPage()) {
			return List.of(
				decisionPath(
					"City rule may change the ask",
					"Check the local transfer or responsibility path before you overstate the problem",
					"Use the city hub when the real question is whether a local requirement, utility boundary, or program note changes the next step before closing.",
					"/cities/",
					"Open city transfer paths"),
				decisionPath(
					"Boundary or seller leverage unclear",
					"Use responsibility context before you push for credits or repairs",
					"Use the homeowner-versus-city page when the transfer question still depends on who owns the lateral, where the boundary sits, or whether the city signal is even relevant.",
					"/homeowner-vs-city-sewer-responsibility/",
					"Clarify responsibility first"),
				decisionPath(
					"Scope finding already exists",
					"Read the report before you turn it into a transfer demand",
					"Use the report-reading path when you already have footage or a finding and the next ask depends on what the wording really supports.",
					"/how-to-read-sewer-scope-report/",
					"Read the scope calmly")
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

