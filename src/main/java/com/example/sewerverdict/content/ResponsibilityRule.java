package com.example.sewerverdict.content;

public record ResponsibilityRule(
	String citySlug,
	String cityLabel,
	String ownerScope,
	String publicScope,
	String programNote,
	String sourceId
) {
}
