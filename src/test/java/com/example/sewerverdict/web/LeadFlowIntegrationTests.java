package com.example.sewerverdict.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class LeadFlowIntegrationTests {

	private static final Path TEST_STORAGE_ROOT = createTempStorageRoot();
	private static final Path LEADS_FILE = TEST_STORAGE_ROOT.resolve("leads").resolve("leads.jsonl");
	private static final Path EVENTS_FILE = TEST_STORAGE_ROOT.resolve("events").resolve("events.jsonl");
	private static final Path DRAFTS_FILE = TEST_STORAGE_ROOT.resolve("estimator-drafts").resolve("estimator-drafts.jsonl");

	@Autowired
	private MockMvc mockMvc;

	@DynamicPropertySource
	static void registerProperties(DynamicPropertyRegistry registry) {
		registry.add("app.storage.root", () -> TEST_STORAGE_ROOT.toString());
	}

	@BeforeEach
	void resetStorageFiles() throws IOException {
		Files.createDirectories(LEADS_FILE.getParent());
		Files.createDirectories(EVENTS_FILE.getParent());
		Files.createDirectories(DRAFTS_FILE.getParent());
		Files.writeString(LEADS_FILE, "");
		Files.writeString(EVENTS_FILE, "");
		Files.writeString(DRAFTS_FILE, "");
	}

	@Test
	void estimatorResultCreatesDraftAndBuildsLeadHandoff() throws Exception {
		mockMvc.perform(post("/estimator/results/")
				.param("role", "buyer")
				.param("location", "19103")
				.param("houseAgeBand", "pre-1950")
				.param("issueState", "no-scope-yet")
				.param("defectType", "unknown")
				.param("accessType", "unknown")
				.param("urgency", "active-decision")
				.param("utmSource", "google")
				.param("utmMedium", "cpc")
				.param("utmCampaign", "buyers"))
			.andExpect(status().isOk())
			.andExpect(header().string("X-Robots-Tag", "noindex, nofollow"))
			.andExpect(content().string(containsString("/find-sewer-scope/?")))
			.andExpect(content().string(containsString("draftId=")))
			.andExpect(content().string(containsString("utmSource=google")));

		String drafts = Files.readString(DRAFTS_FILE);
		String events = Files.readString(EVENTS_FILE);
		org.junit.jupiter.api.Assertions.assertTrue(drafts.contains("\"utmSource\":\"google\""));
		org.junit.jupiter.api.Assertions.assertTrue(drafts.contains("\"routingBucket\":\"inspection-first\""));
		org.junit.jupiter.api.Assertions.assertTrue(events.contains("\"eventType\":\"estimator_complete\""));
	}

	@Test
	void leadSubmitStoresDraftAndAttribution() throws Exception {
		MvcResult result = mockMvc.perform(post("/estimator/results/")
				.param("role", "buyer")
				.param("location", "60614")
				.param("houseAgeBand", "pre-1950")
				.param("issueState", "no-scope-yet")
				.param("defectType", "unknown")
				.param("accessType", "unknown")
				.param("urgency", "active-decision")
				.param("utmSource", "google")
				.param("utmMedium", "cpc")
				.param("utmCampaign", "buyers"))
			.andReturn();

		MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
		String drafts = Files.readString(DRAFTS_FILE).trim();
		String draftId = drafts.substring(drafts.indexOf("\"draftId\":\"") + 11);
		draftId = draftId.substring(0, draftId.indexOf('"'));

		mockMvc.perform(post("/find-sewer-scope/")
				.session(session)
				.param("draftId", draftId)
				.param("recommendedServicePath", "inspection")
				.param("serviceNeeded", "inspection")
				.param("zipOrCity", "60614")
				.param("role", "buyer")
				.param("houseAgeBand", "pre-1950")
				.param("issueState", "no-scope-yet")
				.param("defectType", "unknown")
				.param("urgency", "active-decision")
				.param("name", "Test Buyer")
				.param("email", "buyer@example.com")
				.param("phone", "555-0100")
				.param("consentGiven", "true")
				.param("utmSource", "google")
				.param("utmMedium", "cpc")
				.param("utmCampaign", "buyers"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Thanks. Your details were saved")));

		String leads = Files.readString(LEADS_FILE);
		org.junit.jupiter.api.Assertions.assertTrue(leads.contains(draftId));
		org.junit.jupiter.api.Assertions.assertTrue(leads.contains("\"routingBucket\":\"inspection-first\""));
		org.junit.jupiter.api.Assertions.assertTrue(leads.contains("\"utmSource\":\"google\""));
		org.junit.jupiter.api.Assertions.assertTrue(leads.contains("\"sessionId\":\"" + session.getId() + "\""));
	}

	@Test
	void leadPageHandlesShortDraftIdWithoutTemplateFailure() throws Exception {
		mockMvc.perform(get("/get-sewer-quotes/")
				.param("serviceNeeded", "replacement")
				.param("draftId", "test")
				.param("recommendedServicePath", "replacement")
				.param("role", "owner")
				.param("zipOrCity", "60614")
				.param("houseAgeBand", "1950-1969")
				.param("issueState", "scope-found-issue")
				.param("defectType", "orangeburg")
				.param("urgency", "urgent-repair"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Imported estimator draft test")))
			.andExpect(content().string(containsString("Recommended path: replacement")));
	}

	@Test
	void leadSubmitStoresGeoReferrerContextForPerformanceAnalysis() throws Exception {
		mockMvc.perform(post("/find-sewer-scope/")
				.header("Referer", "http://localhost/cities/chicago/sewer-scope-before-buying-house/")
				.param("serviceNeeded", "inspection")
				.param("recommendedServicePath", "inspection")
				.param("zipOrCity", "60614")
				.param("role", "buyer")
				.param("houseAgeBand", "pre-1950")
				.param("issueState", "no-scope-yet")
				.param("defectType", "unknown")
				.param("urgency", "active-decision")
				.param("name", "Geo Buyer")
				.param("email", "geo@example.com")
				.param("phone", "555-0111")
				.param("consentGiven", "true"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Thanks. Your details were saved")));

		String leads = Files.readString(LEADS_FILE);
		org.junit.jupiter.api.Assertions.assertTrue(leads.contains("\"sourceGeoPage\":true"));
		org.junit.jupiter.api.Assertions.assertTrue(leads.contains("\"sourceCitySlug\":\"chicago\""));
		org.junit.jupiter.api.Assertions.assertTrue(leads.contains("\"sourceGeoTopicSlug\":\"sewer-scope-before-buying-house\""));
	}

	@Test
	void invalidLeadSubmitShowsFieldLevelErrors() throws Exception {
		mockMvc.perform(post("/find-sewer-scope/"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Please check the highlighted fields and consent box before submitting.")))
			.andExpect(content().string(containsString("Add the ZIP or city so the request anchors to a real market.")))
			.andExpect(content().string(containsString("Consent is required before SewerVerdict can pass this request forward.")));

		String events = Files.readString(EVENTS_FILE);
		org.junit.jupiter.api.Assertions.assertTrue(events.contains("\"eventType\":\"lead_submit_invalid\""));
		org.junit.jupiter.api.Assertions.assertTrue(events.contains("\"missingFields\":[\"zipOrCity\""));
	}

	private static Path createTempStorageRoot() {
		try {
			return Files.createTempDirectory("sewerverdict-storage-test");
		}
		catch (IOException exception) {
			throw new IllegalStateException("Failed to create temp storage root", exception);
		}
	}
}
