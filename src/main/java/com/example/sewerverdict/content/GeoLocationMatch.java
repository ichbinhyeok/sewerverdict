package com.example.sewerverdict.content;

public record GeoLocationMatch(
	GeoProfile profile,
	String label,
	boolean zipBased,
	String zipCode,
	String matchScope,
	String matchCaution,
	boolean cityConfirmationNeeded
) {
}
