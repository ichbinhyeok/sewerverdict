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
			.andExpect(content().string(containsString("Philadelphia Sewer Line Repair vs Replacement")))
			.andExpect(content().string(containsString("Chicago Sewer Line Repair vs Replacement")))
			.andExpect(content().string(containsString("Milwaukee, WI")))
			.andExpect(content().string(containsString("Detroit Sewer Line Replacement Cost")))
			.andExpect(content().string(containsString("Milwaukee Sewer Scope Before Buying a House")))
			.andExpect(content().string(containsString("Baltimore Sewer Scope Before Buying a House")))
			.andExpect(content().string(containsString("Detroit Sewer Scope Negotiation With Seller")))
			.andExpect(content().string(containsString("Detroit Sewer Line Repair vs Replacement")));
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

	@Test
	void newBuyerAndBackupGeoPagesRenderSuccessfully() throws Exception {
		mockMvc.perform(get("/cities/detroit/sewer-scope-before-buying-house/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Detroit Sewer Scope Before Buying a House")))
			.andExpect(content().string(containsString("Local market signal")));

		mockMvc.perform(get("/cities/cincinnati/sewer-backup-risk/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Cincinnati Sewer Backup Risk")))
			.andExpect(content().string(containsString("Cincinnati responsibility guide")));
	}

	@Test
	void newNegotiationAndBaltimoreBuyerPagesRenderSuccessfully() throws Exception {
		mockMvc.perform(get("/cities/milwaukee/sewer-scope-negotiation-with-seller/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Milwaukee Sewer Scope Negotiation With Seller")))
			.andExpect(content().string(containsString("Buyer decision lens")));

		mockMvc.perform(get("/cities/baltimore/sewer-scope-before-buying-house/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Baltimore Sewer Scope Before Buying a House")))
			.andExpect(content().string(containsString("Official responsibility boundary")));
	}

	@Test
	void newTierTwoNegotiationAndBackupPagesRenderSuccessfully() throws Exception {
		mockMvc.perform(get("/cities/milwaukee/sewer-backup-risk/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Milwaukee Sewer Backup Risk")))
			.andExpect(content().string(containsString("Defect interpretation lens")));

		mockMvc.perform(get("/cities/cincinnati/sewer-scope-negotiation-with-seller/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Cincinnati Sewer Scope Negotiation With Seller")))
			.andExpect(content().string(containsString("Buyer decision lens")));

		mockMvc.perform(get("/cities/baltimore/sewer-scope-negotiation-with-seller/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Baltimore Sewer Scope Negotiation With Seller")))
			.andExpect(content().string(containsString("Official responsibility boundary")));

		mockMvc.perform(get("/cities/detroit/sewer-scope-negotiation-with-seller/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Detroit Sewer Scope Negotiation With Seller")))
			.andExpect(content().string(containsString("Buyer decision lens")));
	}

	@Test
	void localRepairVsReplacementPagesRenderSuccessfully() throws Exception {
		mockMvc.perform(get("/cities/milwaukee/sewer-line-repair-vs-replacement/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Milwaukee Sewer Line Repair vs Replacement")))
			.andExpect(content().string(containsString("Quote comparison lens")));

		mockMvc.perform(get("/cities/cincinnati/sewer-line-repair-vs-replacement/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Cincinnati Sewer Line Repair vs Replacement")))
			.andExpect(content().string(containsString("Quote comparison lens")));

		mockMvc.perform(get("/cities/baltimore/sewer-line-repair-vs-replacement/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Baltimore Sewer Line Repair vs Replacement")))
			.andExpect(content().string(containsString("Official responsibility boundary")));

		mockMvc.perform(get("/cities/detroit/sewer-line-repair-vs-replacement/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Detroit Sewer Line Repair vs Replacement")))
			.andExpect(content().string(containsString("Quote comparison lens")));
	}

	@Test
	void tierOneLocalRepairVsReplacementPagesRenderSuccessfully() throws Exception {
		mockMvc.perform(get("/cities/philadelphia/sewer-line-repair-vs-replacement/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Philadelphia Sewer Line Repair vs Replacement")))
			.andExpect(content().string(containsString("Official responsibility boundary")));

		mockMvc.perform(get("/cities/pittsburgh/sewer-line-repair-vs-replacement/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Pittsburgh Sewer Line Repair vs Replacement")))
			.andExpect(content().string(containsString("Official responsibility boundary")));

		mockMvc.perform(get("/cities/cleveland/sewer-line-repair-vs-replacement/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Cleveland Sewer Line Repair vs Replacement")))
			.andExpect(content().string(containsString("Quote comparison lens")));

		mockMvc.perform(get("/cities/chicago/sewer-line-repair-vs-replacement/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Chicago Sewer Line Repair vs Replacement")))
			.andExpect(content().string(containsString("Quote comparison lens")));

		mockMvc.perform(get("/cities/buffalo/sewer-line-repair-vs-replacement/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Buffalo Sewer Line Repair vs Replacement")))
			.andExpect(content().string(containsString("Quote comparison lens")));
	}
}
