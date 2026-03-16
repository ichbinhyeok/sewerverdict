package com.example.sewerverdict.telemetry;

public class LeadForm {

	private String serviceNeeded;
	private String zipOrCity;
	private String role;
	private String houseAgeBand;
	private String issueState;
	private String defectType;
	private String urgency;
	private String name;
	private String email;
	private String phone;
	private String notes;
	private boolean consentGiven;

	public String getServiceNeeded() {
		return serviceNeeded;
	}

	public void setServiceNeeded(String serviceNeeded) {
		this.serviceNeeded = serviceNeeded;
	}

	public String getZipOrCity() {
		return zipOrCity;
	}

	public void setZipOrCity(String zipOrCity) {
		this.zipOrCity = zipOrCity;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getHouseAgeBand() {
		return houseAgeBand;
	}

	public void setHouseAgeBand(String houseAgeBand) {
		this.houseAgeBand = houseAgeBand;
	}

	public String getIssueState() {
		return issueState;
	}

	public void setIssueState(String issueState) {
		this.issueState = issueState;
	}

	public String getDefectType() {
		return defectType;
	}

	public void setDefectType(String defectType) {
		this.defectType = defectType;
	}

	public String getUrgency() {
		return urgency;
	}

	public void setUrgency(String urgency) {
		this.urgency = urgency;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public boolean isConsentGiven() {
		return consentGiven;
	}

	public void setConsentGiven(boolean consentGiven) {
		this.consentGiven = consentGiven;
	}
}
