package com.example.sewerverdict.estimator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.example.sewerverdict.content.GeoProfileService;
import com.example.sewerverdict.content.MunicipalityResolution;
import com.example.sewerverdict.content.MunicipalityResolver;
import com.example.sewerverdict.content.SourceRegistryService;

class EstimatorServiceTests {

	private final EstimatorService estimatorService =
		new EstimatorService(
			new CostProfileService(),
			new DefectProfileService(),
			new MaterialProfileService(),
			new GeoProfileService(new SourceRegistryService()),
			(streetAddress, location) -> Optional.empty()
		);

	private final MunicipalityResolver chicagoAddressResolver = (streetAddress, location) -> Optional.of(
		new MunicipalityResolution("121 N LA SALLE ST, CHICAGO, IL, 60602", "Chicago", "IL", "incorporated-place",
			new GeoProfileService(new SourceRegistryService()).getProfileByCitySlug("chicago"))
	);

	private final MunicipalityResolver chicagoSubdivisionResolver = (streetAddress, location) -> Optional.of(
		new MunicipalityResolution("121 N LA SALLE ST, CHICAGO, IL, 60602", "Chicago", "IL", "county-subdivision",
			new GeoProfileService(new SourceRegistryService()).getProfileByCitySlug("chicago"))
	);

	@Test
	void buyerWithOldHomeAndNoScopeRoutesToInspectionFirst() {
		EstimatorForm form = new EstimatorForm();
		form.setRole("buyer");
		form.setLocation("Philadelphia, PA");
		form.setHouseAgeBand("pre-1950");
		form.setIssueState("no-scope-yet");
		form.setDefectType("unknown");
		form.setUrgency("active-decision");

		EstimatorResult result = estimatorService.evaluate(form);

		assertEquals("inspection-first", result.routingBucket());
		assertEquals("Find sewer camera inspection options", result.primaryCtaLabel());
		assertEquals("inspection", result.primaryServiceNeeded());
		assertFalse(result.cityConfirmationNeeded());
		assertTrue(result.localContextSummary().contains("Matched Philadelphia, PA."));
		assertTrue(result.findingReadSummary().contains("Without footage"));
	}

	@Test
	void scopedOrangeburgRoutesToQuoteReady() {
		EstimatorForm form = new EstimatorForm();
		form.setRole("owner");
		form.setHouseAgeBand("1950-1969");
		form.setIssueState("scope-found-issue");
		form.setDefectType("orangeburg");
		form.setUrgency("active-decision");

		EstimatorResult result = estimatorService.evaluate(form);

		assertEquals("quote-ready", result.routingBucket());
		assertEquals("Get sewer repair or replacement quotes", result.primaryCtaLabel());
		assertEquals("replacement", result.primaryServiceNeeded());
		assertTrue(result.findingReadSummary().contains("Orangeburg"));
		assertTrue(result.methodFitSummary().contains("Pipe bursting"));
	}

	@Test
	void urgentSystemicSymptomsDoNotPretendScopeAlreadyConfirmed() {
		EstimatorForm form = new EstimatorForm();
		form.setRole("owner");
		form.setHouseAgeBand("1950-1969");
		form.setIssueState("symptoms-only");
		form.setDefectType("cast-iron");
		form.setAccessType("slab");
		form.setUrgency("urgent-repair");

		EstimatorResult result = estimatorService.evaluate(form);

		assertEquals("quote-ready", result.routingBucket());
		assertEquals("Strong failure signals without a settled work plan", result.evidenceState());
		assertTrue(result.materialReadSummary().contains("Older cast iron"));
	}

	@Test
	void slabAccessAndMatchedMarketWidenReplacementLanguageWithoutFakeLocalQuote() {
		EstimatorForm form = new EstimatorForm();
		form.setRole("owner");
		form.setLocation("Chicago");
		form.setHouseAgeBand("1950-1969");
		form.setIssueState("scope-found-issue");
		form.setDefectType("cast-iron");
		form.setAccessType("slab");
		form.setUrgency("active-decision");

		EstimatorResult result = estimatorService.evaluate(form);

		assertTrue(result.estimateMethodSummary().contains("Under-slab access keeps the upper range wider"));
		assertTrue(result.localContextSummary().contains("Matched Chicago, IL."));
		assertTrue(result.methodFitSummary().contains("Under-slab access keeps excavation"));
	}

	@Test
	void zipOnlyLocationAnchorsToCoveredDeliveryMarketWithBoundaryCaveat() {
		EstimatorForm form = new EstimatorForm();
		form.setRole("buyer");
		form.setLocation("19103");
		form.setHouseAgeBand("pre-1950");
		form.setIssueState("no-scope-yet");
		form.setDefectType("unknown");
		form.setUrgency("active-decision");

		EstimatorResult result = estimatorService.evaluate(form);

		assertTrue(result.cityConfirmationNeeded());
		assertTrue(result.inputSummary().contains("ZIP 19103 (Philadelphia, PA anchor only)"));
		assertTrue(result.localContextSummary().contains("ZIP 19103 anchored this to the Philadelphia, PA covered-market profile using a narrowed municipal-safe ZIP subset"));
		assertTrue(result.localContextSummary().contains("does not confirm the exact municipality, utility boundary, or parcel-level transfer requirement"));
	}

	@Test
	void deliveryMarketZipStillCarriesBoundaryCaveat() {
		EstimatorForm form = new EstimatorForm();
		form.setRole("buyer");
		form.setLocation("60614");
		form.setHouseAgeBand("pre-1950");
		form.setIssueState("no-scope-yet");
		form.setDefectType("unknown");
		form.setUrgency("active-decision");

		EstimatorResult result = estimatorService.evaluate(form);

		assertTrue(result.cityConfirmationNeeded());
		assertTrue(result.inputSummary().contains("ZIP 60614 (Chicago, IL anchor only)"));
		assertTrue(result.localContextSummary().contains("ZIP 60614 anchored this to the Chicago, IL covered delivery market"));
		assertTrue(result.localContextSummary().contains("does not prove the actual municipality, utility boundary, or parcel-level transfer rule"));
	}

	@Test
	void zipOnlyAnchorDoesNotGetExactCityPricingLift() {
		EstimatorForm exactCityForm = new EstimatorForm();
		exactCityForm.setRole("owner");
		exactCityForm.setLocation("Chicago");
		exactCityForm.setHouseAgeBand("1950-1969");
		exactCityForm.setIssueState("scope-found-issue");
		exactCityForm.setDefectType("cast-iron");
		exactCityForm.setUrgency("active-decision");

		EstimatorForm zipOnlyForm = new EstimatorForm();
		zipOnlyForm.setRole("owner");
		zipOnlyForm.setLocation("60614");
		zipOnlyForm.setHouseAgeBand("1950-1969");
		zipOnlyForm.setIssueState("scope-found-issue");
		zipOnlyForm.setDefectType("cast-iron");
		zipOnlyForm.setUrgency("active-decision");

		EstimatorResult exactCity = estimatorService.evaluate(exactCityForm);
		EstimatorResult zipOnly = estimatorService.evaluate(zipOnlyForm);

		assertTrue(exactCity.uncertaintyDrivers().contains("Chicago, IL contractor pricing and restoration conditions"));
		assertTrue(zipOnly.uncertaintyDrivers().contains("Local contractor pricing and restoration conditions once the exact municipality is confirmed"));
		assertTrue(highRangeValue(exactCity.costBands().get(0).range()) > highRangeValue(zipOnly.costBands().get(0).range()));
	}

	@Test
	void unsupportedZipStillFallsBackToNationalRanges() {
		EstimatorForm form = new EstimatorForm();
		form.setRole("buyer");
		form.setLocation("99999");
		form.setHouseAgeBand("pre-1950");
		form.setIssueState("no-scope-yet");
		form.setDefectType("unknown");
		form.setUrgency("active-decision");

		EstimatorResult result = estimatorService.evaluate(form);

		assertTrue(result.cityConfirmationNeeded());
		assertTrue(result.inputSummary().contains("ZIP 99999 (city still needed)"));
		assertTrue(result.localContextSummary().contains("ZIP-only entry saved for follow-up"));
		assertTrue(result.localContextSummary().contains("does not yet have a covered USPS delivery-market match"));
	}

	@Test
	void streetAddressCanUpgradeZipOnlyInputToExactMunicipalityMatch() {
		EstimatorService estimatorWithAddressResolution = new EstimatorService(
			new CostProfileService(),
			new DefectProfileService(),
			new MaterialProfileService(),
			new GeoProfileService(new SourceRegistryService()),
			chicagoAddressResolver
		);
		EstimatorForm form = new EstimatorForm();
		form.setRole("buyer");
		form.setLocation("60602");
		form.setStreetAddress("121 N LaSalle St");
		form.setHouseAgeBand("pre-1950");
		form.setIssueState("no-scope-yet");
		form.setDefectType("unknown");
		form.setUrgency("active-decision");

		EstimatorResult result = estimatorWithAddressResolution.evaluate(form);

		assertFalse(result.cityConfirmationNeeded());
		assertTrue(result.localContextSummary().contains("Street address matched Chicago, IL through the U.S. Census geocoder"));
		assertTrue(result.inputSummary().contains("Chicago, IL"));
	}

	@Test
	void countySubdivisionAddressMatchStaysCautiousAboutMunicipalityCertainty() {
		EstimatorService estimatorWithSubdivisionResolution = new EstimatorService(
			new CostProfileService(),
			new DefectProfileService(),
			new MaterialProfileService(),
			new GeoProfileService(new SourceRegistryService()),
			chicagoSubdivisionResolver
		);
		EstimatorForm form = new EstimatorForm();
		form.setRole("buyer");
		form.setLocation("60602");
		form.setStreetAddress("121 N LaSalle St");
		form.setHouseAgeBand("pre-1950");
		form.setIssueState("no-scope-yet");
		form.setDefectType("unknown");
		form.setUrgency("active-decision");

		EstimatorResult result = estimatorWithSubdivisionResolution.evaluate(form);

		assertTrue(result.cityConfirmationNeeded());
		assertTrue(result.localContextSummary().contains("Census county subdivision signal consistent with Chicago, IL"));
		assertTrue(result.inputSummary().contains("ZIP 60602 (Chicago, IL anchor only)"));
		assertTrue(result.uncertaintyDrivers().contains("Local contractor pricing and restoration conditions once the exact municipality is confirmed"));
	}

	private int highRangeValue(String range) {
		String normalized = range.replace("$", "").replace(",", "");
		int dashIndex = normalized.lastIndexOf('-');
		int suffixIndex = normalized.indexOf(' ', dashIndex);
		String high = suffixIndex >= 0 ? normalized.substring(dashIndex + 1, suffixIndex) : normalized.substring(dashIndex + 1);
		return Integer.parseInt(high);
	}
}
