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
public class MaterialProfileService {

	private static final String MATERIAL_PROFILE_PATH = "data/raw/material_profiles.json";

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final Map<String, MaterialProfile> profilesByMaterial;

	public MaterialProfileService() {
		this.profilesByMaterial = loadProfiles();
	}

	public MaterialProfile getProfile(String material) {
		return profilesByMaterial.get(material);
	}

	private Map<String, MaterialProfile> loadProfiles() {
		Resource resource = new ClassPathResource(MATERIAL_PROFILE_PATH);
		try (InputStream inputStream = resource.getInputStream()) {
			List<MaterialProfile> profiles = objectMapper.readValue(inputStream, new TypeReference<List<MaterialProfile>>() {
			});
			Map<String, MaterialProfile> loaded = new LinkedHashMap<>();
			profiles.forEach(profile -> loaded.put(profile.material(), profile));
			return loaded;
		}
		catch (IOException exception) {
			throw new UncheckedIOException("Failed to load material profiles from " + MATERIAL_PROFILE_PATH, exception);
		}
	}
}
