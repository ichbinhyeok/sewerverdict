package com.example.sewerverdict.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OpsReportIntegrationTests {

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
	void seedReportFiles() throws IOException {
		Files.createDirectories(LEADS_FILE.getParent());
		Files.createDirectories(EVENTS_FILE.getParent());
		Files.createDirectories(DRAFTS_FILE.getParent());
		String now = Instant.now().toString();
		Files.writeString(LEADS_FILE, """
			{"timestamp":"%s","referrerPath":"/sewer-scope-before-buying-house/","utmSource":"google","utmCampaign":"buyers","routingBucket":"inspection-first"}
			{"timestamp":"%s","referrerPath":"/sewer-line-under-slab-repair-cost/","utmSource":"google","utmCampaign":"repair","routingBucket":"quote-ready"}
			""".formatted(now, now));
		Files.writeString(EVENTS_FILE, """
			{"timestamp":"%s","eventType":"estimator_complete","pageSlug":"/estimator/results/"}
			{"timestamp":"%s","eventType":"page_cta_click","pageSlug":"/","payload":{"label":"Start with buyer diligence","placement":"home-hero:inspection-first","route":"inspection-first","destination":"buyer-page"}}
			{"timestamp":"%s","eventType":"page_cta_click","pageSlug":"/cities/milwaukee/","payload":{"label":"city-hub:Milwaukee Sewer Backup Risk","placement":"city-hub-tertiary","route":"interpretation-first","destination":"defect-page"}}
			{"timestamp":"%s","eventType":"result_primary_cta_click","pageSlug":"/estimator/results/"}
			""".formatted(now, now, now, now));
		Files.writeString(DRAFTS_FILE, """
			{"timestamp":"%s","utmCampaign":"buyers"}
			{"timestamp":"%s","utmCampaign":"repair"}
			""".formatted(now, now));
	}

	@Test
	void opsReportRendersTelemetrySummary() throws Exception {
		mockMvc.perform(get("/ops/report/"))
			.andExpect(status().isOk())
			.andExpect(header().string("X-Robots-Tag", "noindex, nofollow"))
			.andExpect(content().string(containsString("Revenue-spine telemetry snapshot")))
			.andExpect(content().string(containsString("/sewer-scope-before-buying-house/")))
			.andExpect(content().string(containsString("google")))
			.andExpect(content().string(containsString("inspection-first")))
			.andExpect(content().string(containsString("home-hero:inspection-first")))
			.andExpect(content().string(containsString("city-hub-tertiary")))
			.andExpect(content().string(containsString("defect-page")))
			.andExpect(content().string(containsString("Search Console")))
			.andExpect(content().string(containsString("Noindex internal view")));
	}

	private static Path createTempStorageRoot() {
		try {
			return Files.createTempDirectory("sewerverdict-ops-report-test");
		}
		catch (IOException exception) {
			throw new IllegalStateException("Failed to create temp storage root", exception);
		}
	}
}
