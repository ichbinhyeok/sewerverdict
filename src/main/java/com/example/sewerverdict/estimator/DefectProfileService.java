package com.example.sewerverdict.estimator;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DefectProfileService {

	private static final String DEFECT_PROFILE_PATH = "data/raw/defect_profiles.json";

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final Map<String, DefectProfile> profilesByType;

	public DefectProfileService() {
		this.profilesByType = loadProfiles();
	}

	public DefectProfile getProfile(String defectType) {
		return profilesByType.getOrDefault(defectType, profilesByType.get("unknown"));
	}

	private Map<String, DefectProfile> loadProfiles() {
		Resource resource = new ClassPathResource(DEFECT_PROFILE_PATH);
		try (InputStream inputStream = resource.getInputStream()) {
			List<DefectProfile> profiles = objectMapper.readValue(inputStream, new TypeReference<List<DefectProfile>>() {
			});
			Map<String, DefectProfile> loaded = new LinkedHashMap<>();
			profiles.forEach(profile -> loaded.put(profile.defectType(), profile));
			return loaded;
		}
		catch (IOException exception) {
			throw new UncheckedIOException("Failed to load defect profiles from " + DEFECT_PROFILE_PATH, exception);
		}
	}
}
