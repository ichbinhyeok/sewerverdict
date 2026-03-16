package com.example.sewerverdict.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SiteControllerIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void citiesHubRendersTierOneCitiesAndPages() throws Exception {
		mockMvc.perform(get("/cities/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("City pages built only where the local signal is real.")))
			.andExpect(content().string(containsString("Philadelphia, PA")))
			.andExpect(content().string(containsString("Buffalo Sewer Scope Before Buying a House")))
			.andExpect(content().string(containsString("Milwaukee, WI")))
			.andExpect(content().string(containsString("Detroit Sewer Line Replacement Cost")));
	}

	@Test
	void deepenedGeoPageRendersSuccessfully() throws Exception {
		mockMvc.perform(get("/cities/chicago/sewer-scope-negotiation-with-seller/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Chicago Sewer Scope Negotiation With Seller")))
			.andExpect(content().string(containsString("Local system context")));
	}

	@Test
	void newResponsibilityGeoPageRendersSuccessfully() throws Exception {
		mockMvc.perform(get("/cities/baltimore/homeowner-vs-city-sewer-responsibility/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Baltimore Homeowner vs City Sewer Responsibility")))
			.andExpect(content().string(containsString("Official responsibility boundary")));
	}
}
