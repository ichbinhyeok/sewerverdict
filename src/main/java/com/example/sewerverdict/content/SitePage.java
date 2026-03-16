package com.example.sewerverdict.content;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SitePage {

	private String slug;
	private String family;
	private String title;
	private String metaTitle;
	private String metaDescription;
	private String eyebrow;
	private String summary;
	private String quickAnswer;
	private String seriousness;
	private String nextStep;
	private String costDirection;
	private String uncertainty;
	private String primaryCtaLabel;
	private String primaryCtaHref;
	private String secondaryCtaLabel;
	private String secondaryCtaHref;
	private String authorRole;
	private String reviewerRole;
	private String lastReviewed;
	private String sourceNote;
	private List<PageSection> sections = new ArrayList<>();
	private List<PageTable> tables = new ArrayList<>();
	private List<String> highlights = new ArrayList<>();
	private List<String> questions = new ArrayList<>();
	private List<PageFaq> faq = new ArrayList<>();
	private List<String> relatedSlugs = new ArrayList<>();

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public String getFamily() {
		return family;
	}

	public void setFamily(String family) {
		this.family = family;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMetaTitle() {
		return metaTitle;
	}

	public void setMetaTitle(String metaTitle) {
		this.metaTitle = metaTitle;
	}

	public String getMetaDescription() {
		return metaDescription;
	}

	public void setMetaDescription(String metaDescription) {
		this.metaDescription = metaDescription;
	}

	public String getEyebrow() {
		return eyebrow;
	}

	public void setEyebrow(String eyebrow) {
		this.eyebrow = eyebrow;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getQuickAnswer() {
		return quickAnswer;
	}

	public void setQuickAnswer(String quickAnswer) {
		this.quickAnswer = quickAnswer;
	}

	public String getSeriousness() {
		return seriousness;
	}

	public void setSeriousness(String seriousness) {
		this.seriousness = seriousness;
	}

	public String getNextStep() {
		return nextStep;
	}

	public void setNextStep(String nextStep) {
		this.nextStep = nextStep;
	}

	public String getCostDirection() {
		return costDirection;
	}

	public void setCostDirection(String costDirection) {
		this.costDirection = costDirection;
	}

	public String getUncertainty() {
		return uncertainty;
	}

	public void setUncertainty(String uncertainty) {
		this.uncertainty = uncertainty;
	}

	public String getPrimaryCtaLabel() {
		return primaryCtaLabel;
	}

	public void setPrimaryCtaLabel(String primaryCtaLabel) {
		this.primaryCtaLabel = primaryCtaLabel;
	}

	public String getPrimaryCtaHref() {
		return primaryCtaHref;
	}

	public void setPrimaryCtaHref(String primaryCtaHref) {
		this.primaryCtaHref = primaryCtaHref;
	}

	public String getSecondaryCtaLabel() {
		return secondaryCtaLabel;
	}

	public void setSecondaryCtaLabel(String secondaryCtaLabel) {
		this.secondaryCtaLabel = secondaryCtaLabel;
	}

	public String getSecondaryCtaHref() {
		return secondaryCtaHref;
	}

	public void setSecondaryCtaHref(String secondaryCtaHref) {
		this.secondaryCtaHref = secondaryCtaHref;
	}

	public String getAuthorRole() {
		return authorRole;
	}

	public void setAuthorRole(String authorRole) {
		this.authorRole = authorRole;
	}

	public String getReviewerRole() {
		return reviewerRole;
	}

	public void setReviewerRole(String reviewerRole) {
		this.reviewerRole = reviewerRole;
	}

	public String getLastReviewed() {
		return lastReviewed;
	}

	public void setLastReviewed(String lastReviewed) {
		this.lastReviewed = lastReviewed;
	}

	public String getSourceNote() {
		return sourceNote;
	}

	public void setSourceNote(String sourceNote) {
		this.sourceNote = sourceNote;
	}

	public List<PageSection> getSections() {
		return sections;
	}

	public void setSections(List<PageSection> sections) {
		this.sections = sections == null ? new ArrayList<>() : new ArrayList<>(sections);
	}

	public List<PageTable> getTables() {
		return tables;
	}

	public void setTables(List<PageTable> tables) {
		this.tables = tables == null ? new ArrayList<>() : new ArrayList<>(tables);
	}

	public List<String> getHighlights() {
		return highlights;
	}

	public void setHighlights(List<String> highlights) {
		this.highlights = highlights == null ? new ArrayList<>() : new ArrayList<>(highlights);
	}

	public List<String> getQuestions() {
		return questions;
	}

	public void setQuestions(List<String> questions) {
		this.questions = questions == null ? new ArrayList<>() : new ArrayList<>(questions);
	}

	public List<PageFaq> getFaq() {
		return faq;
	}

	public void setFaq(List<PageFaq> faq) {
		this.faq = faq == null ? new ArrayList<>() : new ArrayList<>(faq);
	}

	public List<String> getRelatedSlugs() {
		return relatedSlugs;
	}

	public void setRelatedSlugs(List<String> relatedSlugs) {
		this.relatedSlugs = relatedSlugs == null ? new ArrayList<>() : new ArrayList<>(relatedSlugs);
	}

	public boolean isTrustPage() {
		return "trust".equalsIgnoreCase(family);
	}

	public boolean isGeoPage() {
		return slug != null && slug.startsWith("/cities/");
	}
}
