package com.example.sewerverdict.content;

public record MunicipalityResolution(
	String matchedAddress,
	String municipalityName,
	String stateCode,
	String geographyType,
	GeoProfile profile
) {

	public boolean matchedCoveredProfile() {
		return profile != null;
	}

	public boolean exactMunicipalityMatch() {
		return !"county-subdivision".equalsIgnoreCase(geographyType);
	}

	public String municipalityLabel() {
		return municipalityName + ", " + stateCode;
	}

	public String geographyLabel() {
		return exactMunicipalityMatch() ? "municipality" : "county subdivision";
	}

	public String coveredLabel() {
		if (profile != null) {
			return profile.getCityName() + ", " + profile.getStateCode();
		}
		return municipalityLabel();
	}
}
