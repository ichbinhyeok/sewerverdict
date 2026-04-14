package com.example.sewerverdict.content;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZipCityMarket {

	private String citySlug;
	private String cityName;
	private String stateCode;
	private String matchLabel;
	private String matchScope;
	private String matchCaution;
	private List<String> sourceIds = new ArrayList<>();
	private List<String> zipCodes = new ArrayList<>();

	public String getCitySlug() {
		return citySlug;
	}

	public void setCitySlug(String citySlug) {
		this.citySlug = citySlug;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public String getStateCode() {
		return stateCode;
	}

	public void setStateCode(String stateCode) {
		this.stateCode = stateCode;
	}

	public String getMatchLabel() {
		return matchLabel;
	}

	public void setMatchLabel(String matchLabel) {
		this.matchLabel = matchLabel;
	}

	public String getMatchScope() {
		return matchScope;
	}

	public void setMatchScope(String matchScope) {
		this.matchScope = matchScope;
	}

	public String getMatchCaution() {
		return matchCaution;
	}

	public void setMatchCaution(String matchCaution) {
		this.matchCaution = matchCaution;
	}

	public List<String> getSourceIds() {
		return sourceIds;
	}

	public void setSourceIds(List<String> sourceIds) {
		this.sourceIds = sourceIds == null ? new ArrayList<>() : new ArrayList<>(sourceIds);
	}

	public List<String> getZipCodes() {
		return zipCodes;
	}

	public void setZipCodes(List<String> zipCodes) {
		this.zipCodes = zipCodes == null ? new ArrayList<>() : new ArrayList<>(zipCodes);
	}
}
