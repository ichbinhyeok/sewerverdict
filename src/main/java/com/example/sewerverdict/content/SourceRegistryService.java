package com.example.sewerverdict.content;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class SourceRegistryService {

	private static final String REGISTRY_PATH = "data/raw/source_registry.csv";
	private static final String CSV_SPLIT_REGEX = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

	private final Map<String, SourceReference> sourcesById;

	public SourceRegistryService() {
		this.sourcesById = loadSources();
	}

	public List<SourceReference> getSourcesForPage(SitePage page) {
		List<String> sourceIds = page.getSourceIds().isEmpty() ? defaultSourceIds(page) : page.getSourceIds();
		return sourceIds.stream()
			.map(sourcesById::get)
			.filter(Objects::nonNull)
			.toList();
	}

	public SourceReference getSourceById(String sourceId) {
		return sourcesById.get(sourceId);
	}

	public List<SourceReference> getSourcesByIds(List<String> sourceIds) {
		return sourceIds.stream()
			.map(sourcesById::get)
			.filter(Objects::nonNull)
			.toList();
	}

	private List<String> defaultSourceIds(SitePage page) {
		String slug = page.getSlug();
		if (slug.contains("home-insurance-cover")) {
			return List.of("ins-001", "ins-002", "iii-001");
		}
		if (slug.contains("service-line-coverage")) {
			return List.of("ins-001", "nyc-001", "war-001");
		}
		if (slug.contains("homeowner-vs-city")) {
			return List.of("pwd-001", "pdx-001", "pdx-002", "nyc-001");
		}
		if (slug.contains("buyer-or-seller")) {
			return List.of("pwd-001", "phl-001", "pdx-001", "nyc-001");
		}
		if (page.isTrustPage()) {
			return List.of("cost-001", "cost-002", "rdf-001", "nyc-001", "ins-001");
		}
		if (page.isTransferPage() || page.isBuyerPage() || slug.contains("scope")) {
			return List.of("rdf-001", "cost-004", "cost-002");
		}
		if (page.isCostPage() || page.isDefectPage() || page.isGeoPage()) {
			return List.of("cost-001", "cost-002");
		}
		return List.of();
	}

	private Map<String, SourceReference> loadSources() {
		Resource resource = new ClassPathResource(REGISTRY_PATH);
		Map<String, SourceReference> loaded = new LinkedHashMap<>();
		try (BufferedReader reader = new BufferedReader(
			new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
			String header = reader.readLine();
			if (header == null) {
				return loaded;
			}

			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isBlank()) {
					continue;
				}
				String[] cells = Arrays.stream(line.split(CSV_SPLIT_REGEX, -1))
					.map(this::cleanCsvValue)
					.toArray(String[]::new);
				if (cells.length < 9) {
					continue;
				}
				SourceReference source = new SourceReference(
					cells[0],
					cells[1],
					cells[2],
					cells[3],
					cells[4],
					cells[5],
					cells[6],
					cells[7],
					cells[8]
				);
				loaded.put(source.sourceId(), source);
			}
			return loaded;
		}
		catch (IOException exception) {
			throw new UncheckedIOException("Failed to load source registry from " + REGISTRY_PATH, exception);
		}
	}

	private String cleanCsvValue(String value) {
		String cleaned = value == null ? "" : value.trim();
		if (cleaned.startsWith("\"") && cleaned.endsWith("\"") && cleaned.length() >= 2) {
			cleaned = cleaned.substring(1, cleaned.length() - 1);
		}
		return cleaned.replace("\"\"", "\"");
	}
}
