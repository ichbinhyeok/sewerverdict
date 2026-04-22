package com.example.sewerverdict.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SeoControllerIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void sitemapIncludesCitiesHubAndCityHubRoutes() throws Exception {
		mockMvc.perform(get("/sitemap.xml"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("https://sewerclarity.com/")))
			.andExpect(content().string(containsString("/cities/")))
			.andExpect(content().string(containsString("/cities/philadelphia/")))
			.andExpect(content().string(containsString("/cities/milwaukee/")))
			.andExpect(content().string(containsString("/cities/philadelphia/homeowner-vs-city-sewer-responsibility/")))
			.andExpect(content().string(not(containsString("/cities/chicago/sewer-line-replacement-cost/"))));
	}

	@Test
	void robotsUsesCanonicalBaseUrlEvenWhenRequestHostDiffers() throws Exception {
		mockMvc.perform(get("/robots.txt").header("Host", "www.sewerclarity.com"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Disallow: /ops/")))
			.andExpect(content().string(containsString("Sitemap: https://sewerclarity.com/sitemap.xml")));
	}
}
