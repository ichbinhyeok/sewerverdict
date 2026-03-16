package com.example.sewerverdict.content;

import java.util.List;

public record CityHubEntry(
	GeoProfile profile,
	List<SitePage> pages
) {
}
