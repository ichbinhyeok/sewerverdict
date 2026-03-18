package com.example.sewerverdict.content;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SitePage {

	private String slug;
	private String family;
	private String clusterRole;
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
	private List<String> sourceIds = new ArrayList<>();
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

	public String getClusterRole() {
		return clusterRole;
	}

	public void setClusterRole(String clusterRole) {
		this.clusterRole = clusterRole;
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

	public List<String> getSourceIds() {
		return sourceIds;
	}

	public void setSourceIds(List<String> sourceIds) {
		this.sourceIds = sourceIds == null ? new ArrayList<>() : new ArrayList<>(sourceIds);
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

	public boolean isBuyerPage() {
		return matchesFamily("buyer");
	}

	public boolean isCostPage() {
		return matchesFamily("cost");
	}

	public boolean isDefectPage() {
		return matchesFamily("defect");
	}

	public boolean isCoveragePage() {
		return matchesFamily("coverage");
	}

	public boolean isGeoPage() {
		return slug != null && slug.startsWith("/cities/");
	}

	public boolean isClusterWinner() {
		return "winner".equalsIgnoreCase(clusterRole);
	}

	public String getClusterRoleLabel() {
		if (isClusterWinner()) {
			return "Primary page";
		}
		if ("support".equalsIgnoreCase(clusterRole)) {
			return "Support page";
		}
		return "Topic page";
	}

	public int getClusterRolePriority() {
		if (isClusterWinner()) {
			return 0;
		}
		if ("support".equalsIgnoreCase(clusterRole)) {
			return 1;
		}
		return 2;
	}

	public String getTrackingFamily() {
		return isGeoPage() ? inferGeoFamily() : family;
	}

	public String getGeoCitySlug() {
		if (!isGeoPage()) {
			return null;
		}
		String[] segments = slug.split("/");
		return segments.length > 2 ? segments[2] : null;
	}

	public String getGeoTopicSlug() {
		if (!isGeoPage()) {
			return null;
		}
		String[] segments = slug.split("/");
		return segments.length > 3 ? segments[3] : null;
	}

	private boolean matchesFamily(String expectedFamily) {
		if (expectedFamily.equalsIgnoreCase(family)) {
			return true;
		}
		if (!isGeoPage()) {
			return false;
		}
		String inferredFamily = inferGeoFamily();
		return expectedFamily.equalsIgnoreCase(inferredFamily);
	}

	private String inferGeoFamily() {
		String slugHint = slug == null ? "" : slug.toLowerCase();
		String secondaryHint = secondaryCtaHref == null ? "" : secondaryCtaHref.toLowerCase();
		String familyFromSlug = inferFamilyFromHint(slugHint);
		if (familyFromSlug != null) {
			return familyFromSlug;
		}
		String familyFromSecondary = inferFamilyFromHint(secondaryHint);
		if (familyFromSecondary != null) {
			return familyFromSecondary;
		}
		return family;
	}

	private String inferFamilyFromHint(String hint) {
		if (hint == null || hint.isBlank()) {
			return null;
		}
		if (hint.contains("before-buying-house") || hint.contains("buyer-or-seller")
				|| hint.contains("old-house") || hint.contains("before-1970")
				|| hint.contains("negotiation-with-seller")
				|| hint.contains("scope-worth-it")
				|| hint.contains("scope-inspection")) {
			return "buyer";
		}
		if (hint.contains("homeowner-vs-city")
				|| hint.contains("home-insurance-cover")
				|| hint.contains("service-line-coverage")) {
			return "coverage";
		}
		if (hint.contains("risk") || hint.contains("red-flags")
				|| hint.contains("what-to-do") || hint.contains("meaning")
				|| hint.contains("signs")) {
			return "defect";
		}
		if (hint.contains("cost") || hint.contains("repair-vs-replacement")
				|| hint.contains("trenchless")) {
			return "cost";
		}
		return null;
	}
}
