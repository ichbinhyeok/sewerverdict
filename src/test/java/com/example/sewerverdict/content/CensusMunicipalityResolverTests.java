package com.example.sewerverdict.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

class CensusMunicipalityResolverTests {

	private HttpServer server;
	private String responseBody;

	@BeforeEach
	void startServer() throws IOException {
		server = HttpServer.create(new InetSocketAddress(0), 0);
		server.createContext("/geocoder/geographies/address", this::writeResponse);
		server.createContext("/geocoder/geographies/onelineaddress", this::writeResponse);
		server.start();
	}

	@AfterEach
	void stopServer() {
		if (server != null) {
			server.stop(0);
		}
	}

	@Test
	void zipBasedAddressLookupResolvesCoveredCityThroughCensusResponse() {
		responseBody = """
			{
			  "result": {
			    "addressMatches": [{
			      "matchedAddress": "121 N LA SALLE ST, CHICAGO, IL, 60602",
			      "addressComponents": { "state": "IL", "zip": "60602" },
			      "geographies": {
			        "States": [{ "STUSAB": "IL" }],
			        "Incorporated Places": [{ "BASENAME": "Chicago" }]
			      }
			    }]
			  }
			}
			""";
		CensusMunicipalityResolver resolver = new CensusMunicipalityResolver(
			new GeoProfileService(new SourceRegistryService()),
			true,
			"http://localhost:" + server.getAddress().getPort() + "/geocoder",
			"Public_AR_Current",
			"Current_Current",
			new ObjectMapper(),
			HttpClient.newHttpClient()
		);

		Optional<MunicipalityResolution> resolution = resolver.resolve("121 N LaSalle St", "60602");

		assertTrue(resolution.isPresent());
		assertTrue(resolution.get().matchedCoveredProfile());
		assertTrue(resolution.get().exactMunicipalityMatch());
		assertEquals("Chicago, IL", resolution.get().coveredLabel());
	}

	@Test
	void countySubdivisionFallbackStaysDistinctFromExactMunicipalityMatch() {
		responseBody = """
			{
			  "result": {
			    "addressMatches": [{
			      "matchedAddress": "999 SAMPLE ST, WASHINGTON, DC, 20001",
			      "addressComponents": { "state": "DC", "zip": "20001" },
			      "geographies": {
			        "States": [{ "STUSAB": "DC" }],
			        "County Subdivisions": [{ "BASENAME": "Washington" }]
			      }
			    }]
			  }
			}
			""";
		CensusMunicipalityResolver resolver = new CensusMunicipalityResolver(
			new GeoProfileService(new SourceRegistryService()),
			true,
			"http://localhost:" + server.getAddress().getPort() + "/geocoder",
			"Public_AR_Current",
			"Current_Current",
			new ObjectMapper(),
			HttpClient.newHttpClient()
		);

		Optional<MunicipalityResolution> resolution = resolver.resolve("999 Sample St", "20001");

		assertTrue(resolution.isPresent());
		assertEquals("Washington, DC", resolution.get().municipalityLabel());
		assertEquals("county subdivision", resolution.get().geographyLabel());
		assertFalse(resolution.get().exactMunicipalityMatch());
	}

	private void writeResponse(HttpExchange exchange) throws IOException {
		byte[] body = responseBody.getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().add("Content-Type", "application/json");
		exchange.sendResponseHeaders(200, body.length);
		try (OutputStream outputStream = exchange.getResponseBody()) {
			outputStream.write(body);
		}
	}
}
