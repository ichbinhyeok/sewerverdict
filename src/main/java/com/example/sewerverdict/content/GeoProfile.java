package com.example.sewerverdict.content;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoProfile {

	private String citySlug;
	private String cityName;
	private String stateCode;
	private String priorityTier;
	private String marketReason;
	private String housingAgeSignal;
	private String systemContext;
	private String responsibilitySummary;
	private String programSummary;
	private String ctaBias;
	private List<String> sourceIds = new ArrayList<>();

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

	public String getPriorityTier() {
		return priorityTier;
	}

	public void setPriorityTier(String priorityTier) {
		this.priorityTier = priorityTier;
	}

	public String getMarketReason() {
		return marketReason;
	}

	public void setMarketReason(String marketReason) {
		this.marketReason = marketReason;
	}

	public String getHousingAgeSignal() {
		return housingAgeSignal;
	}

	public void setHousingAgeSignal(String housingAgeSignal) {
		this.housingAgeSignal = housingAgeSignal;
	}

	public String getSystemContext() {
		return systemContext;
	}

	public void setSystemContext(String systemContext) {
		this.systemContext = systemContext;
	}

	public String getResponsibilitySummary() {
		return responsibilitySummary;
	}

	public void setResponsibilitySummary(String responsibilitySummary) {
		this.responsibilitySummary = responsibilitySummary;
	}

	public String getProgramSummary() {
		return programSummary;
	}

	public void setProgramSummary(String programSummary) {
		this.programSummary = programSummary;
	}

	public String getCtaBias() {
		return ctaBias;
	}

	public void setCtaBias(String ctaBias) {
		this.ctaBias = ctaBias;
	}

	public List<String> getSourceIds() {
		return sourceIds;
	}

	public void setSourceIds(List<String> sourceIds) {
		this.sourceIds = sourceIds == null ? new ArrayList<>() : new ArrayList<>(sourceIds);
	}
}
