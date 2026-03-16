package com.example.sewerverdict.web;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.sewerverdict.content.SiteContentService;
import com.example.sewerverdict.content.SitePage;

@Controller
public class SeoController {

	private final SiteContentService siteContentService;
	private final String reviewDate;

	public SeoController(SiteContentService siteContentService, @Value("${app.review-date}") String reviewDate) {
		this.siteContentService = siteContentService;
		this.reviewDate = reviewDate;
	}

	@GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
	@ResponseBody
	public String robots(HttpServletRequest request) {
		String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
		return """
			User-agent: *
			Allow: /
			Disallow: /estimator/results/

			Sitemap: %s/sitemap.xml
			""".formatted(baseUrl);
	}

	@GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
	@ResponseBody
	public String sitemap(HttpServletRequest request) {
		String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
		List<SitemapUrl> urls = new ArrayList<>();
		urls.add(new SitemapUrl(baseUrl + "/", reviewDate));
		urls.add(new SitemapUrl(baseUrl + "/estimator/", reviewDate));
		urls.add(new SitemapUrl(baseUrl + "/find-sewer-scope/", reviewDate));
		urls.add(new SitemapUrl(baseUrl + "/get-sewer-quotes/", reviewDate));
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

	private record SitemapUrl(String loc, String lastmod) {
	}
}
