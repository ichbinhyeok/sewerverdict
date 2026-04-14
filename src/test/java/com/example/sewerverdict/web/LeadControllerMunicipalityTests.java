package com.example.sewerverdict.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;

import com.example.sewerverdict.content.MunicipalityResolution;
import com.example.sewerverdict.content.MunicipalityResolver;
import com.example.sewerverdict.telemetry.LeadForm;
import com.example.sewerverdict.telemetry.StorageService;

class LeadControllerMunicipalityTests {

	@Test
	void exactMunicipalityWithoutStoredProfileDoesNotKeepCityConfirmationOpen() {
		StorageService storageService = mock(StorageService.class);
		SeoMetadataService seoMetadataService = mock(SeoMetadataService.class);
		MunicipalityResolver municipalityResolver = mock(MunicipalityResolver.class);
		stubCommonDependencies(storageService, seoMetadataService);
		when(municipalityResolver.resolve("123 Main St", "62704")).thenReturn(Optional.of(
			new MunicipalityResolution("123 MAIN ST, SPRINGFIELD, IL, 62704", "Springfield", "IL",
				"incorporated-place", null)
		));
		LeadController controller = new LeadController(storageService, seoMetadataService, municipalityResolver);
		LeadForm leadForm = new LeadForm();
		leadForm.setStreetAddress("123 Main St");
		leadForm.setZipOrCity("62704");

		ExtendedModelMap model = new ExtendedModelMap();
		controller.findSewerScope(leadForm, new MockHttpServletRequest(), model);

		assertFalse((Boolean) model.getAttribute("cityConfirmationNeeded"));
		assertTrue(((String) model.getAttribute("leadMunicipalitySummary"))
			.contains("does not yet have a stored local profile for that municipality"));
	}

	@Test
	void countySubdivisionFallbackKeepsCityConfirmationOpen() {
		StorageService storageService = mock(StorageService.class);
		SeoMetadataService seoMetadataService = mock(SeoMetadataService.class);
		MunicipalityResolver municipalityResolver = mock(MunicipalityResolver.class);
		stubCommonDependencies(storageService, seoMetadataService);
		when(municipalityResolver.resolve("999 Sample St", "20001")).thenReturn(Optional.of(
			new MunicipalityResolution("999 SAMPLE ST, WASHINGTON, DC, 20001", "Washington", "DC",
				"county-subdivision", null)
		));
		LeadController controller = new LeadController(storageService, seoMetadataService, municipalityResolver);
		LeadForm leadForm = new LeadForm();
		leadForm.setStreetAddress("999 Sample St");
		leadForm.setZipOrCity("20001");

		ExtendedModelMap model = new ExtendedModelMap();
		controller.findSewerScope(leadForm, new MockHttpServletRequest(), model);

		assertTrue((Boolean) model.getAttribute("cityConfirmationNeeded"));
		assertTrue(((String) model.getAttribute("leadMunicipalitySummary"))
			.contains("does not prove exact municipality or city-rule certainty"));
	}

	@Test
	void leadTemplateUsesLocalitySignalHeadingWhenCityConfirmationIsStillNeeded() throws IOException {
		String template = Files.readString(Path.of("src/main/resources/templates/lead.html"));
		assertTrue(template.contains("cityConfirmationNeeded ? 'Street address locality signal' : 'Street address municipality match'"));
	}

	private void stubCommonDependencies(StorageService storageService, SeoMetadataService seoMetadataService) {
		when(storageService.getEstimatorDraft(anyString())).thenReturn(Optional.empty());
		doNothing().when(storageService).logEvent(anyString(), anyString(), any(), anyString(), any(Map.class));
		doNothing().when(seoMetadataService).apply(any(), any(), anyString(), anyString(), anyString(),
			any(List.class), any(List.class), anyBoolean());
	}
}
