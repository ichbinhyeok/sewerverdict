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
public class CostProfileService {

	private static final String COST_PROFILE_PATH = "data/raw/cost_profiles.json";

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final Map<String, CostProfile> profilesById;

	public CostProfileService() {
		this.profilesById = loadProfiles();
	}

	public CostProfile getRequiredProfile(String profileId) {
		CostProfile profile = profilesById.get(profileId);
		if (profile == null) {
			throw new IllegalArgumentException("Unknown cost profile: " + profileId);
		}
		return profile;
	}

	private Map<String, CostProfile> loadProfiles() {
		Resource resource = new ClassPathResource(COST_PROFILE_PATH);
		try (InputStream inputStream = resource.getInputStream()) {
			List<CostProfile> profiles = objectMapper.readValue(inputStream, new TypeReference<List<CostProfile>>() {
			});
			Map<String, CostProfile> loaded = new LinkedHashMap<>();
			profiles.forEach(profile -> loaded.put(profile.profileId(), profile));
			return loaded;
		}
		catch (IOException exception) {
			throw new UncheckedIOException("Failed to load cost profiles from " + COST_PROFILE_PATH, exception);
		}
	}
}
