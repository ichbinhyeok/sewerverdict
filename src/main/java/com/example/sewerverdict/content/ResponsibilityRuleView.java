package com.example.sewerverdict.content;

public record ResponsibilityRuleView(
	String cityLabel,
	String ownerScope,
	String publicScope,
	String programNote,
	SourceReference source
) {
}
