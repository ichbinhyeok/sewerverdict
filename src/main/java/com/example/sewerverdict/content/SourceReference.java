package com.example.sewerverdict.content;

public record SourceReference(
	String sourceId,
	String title,
	String url,
	String sourceClass,
	String topicArea,
	String jurisdiction,
	String claimNotes,
	String lastVerified,
	String trustNotes
) {
}
