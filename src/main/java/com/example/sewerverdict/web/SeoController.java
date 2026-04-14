package com.example.sewerverdict.web;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.sewerverdict.content.GeoProfileService;
import com.example.sewerverdict.content.SiteContentService;
import com.example.sewerverdict.content.SitePage;

@Controller
public class SeoController {

	private final SiteContentService siteContentService;
	private final GeoProfileService geoProfileService;
	private final String reviewDate;
	private final String baseUrl;

	public SeoController(SiteContentService siteContentService, GeoProfileService geoProfileService,
			@Value("${app.review-date}") String reviewDate,
			@Value("${app.base-url}") String baseUrl) {
		this.siteContentService = siteContentService;
		this.geoProfileService = geoProfileService;
		this.reviewDate = reviewDate;
		this.baseUrl = trimTrailingSlash(baseUrl);
	}

	@GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
	@ResponseBody
	public String robots(HttpServletRequest request) {
		return """
			User-agent: *
			Allow: /
			Disallow: /estimator/results/
			Disallow: /ops/

			Sitemap: %s/sitemap.xml
			""".formatted(baseUrl);
	}

	@GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
	@ResponseBody
	public String sitemap(HttpServletRequest request) {
		List<SitemapUrl> urls = new ArrayList<>();
		urls.add(new SitemapUrl(baseUrl + "/", reviewDate));
		urls.add(new SitemapUrl(baseUrl + "/estimator/", reviewDate));
		urls.add(new SitemapUrl(baseUrl + "/find-sewer-scope/", reviewDate));
		urls.add(new SitemapUrl(baseUrl + "/get-sewer-quotes/", reviewDate));
		urls.add(new SitemapUrl(baseUrl + "/cities/", reviewDate));
		geoProfileService.getCityHubEntries(siteContentService.getAllPages()).forEach(entry ->
			urls.add(new SitemapUrl(baseUrl + "/cities/" + entry.profile().getCitySlug() + "/", reviewDate)));
		for (SitePage page : siteContentService.getAllPages()) {
			urls.add(new SitemapUrl(baseUrl + page.getSlug(), page.getLastReviewed()));
		}

		StringBuilder xml = new StringBuilder();
		xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
		for (SitemapUrl url : urls) {
			xml.append("<url>");
			xml.append("<loc>").append(escapeXml(url.loc())).append("</loc>");
			xml.append("<lastmod>").append(escapeXml(url.lastmod())).append("</lastmod>");
			xml.append("</url>");
		}
		xml.append("</urlset>");
		return xml.toString();
	}

	private String escapeXml(String value) {
		return value
			.replace("&", "&amp;")
			.replace("<", "&lt;")
			.replace(">", "&gt;")
			.replace("\"", "&quot;")
			.replace("'", "&apos;");
	}

	private String trimTrailingSlash(String value) {
		return value != null && value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
	}

	private record SitemapUrl(String loc, String lastmod) {
	}
}
