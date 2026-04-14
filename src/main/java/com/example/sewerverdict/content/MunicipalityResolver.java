package com.example.sewerverdict.content;

import java.util.Optional;

@FunctionalInterface
public interface MunicipalityResolver {

	Optional<MunicipalityResolution> resolve(String streetAddress, String location);
}
