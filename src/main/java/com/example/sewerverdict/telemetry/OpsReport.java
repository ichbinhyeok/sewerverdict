package com.example.sewerverdict.telemetry;

import java.time.Instant;
import java.util.List;

public record OpsReport(
	Instant generatedAt,
	long estimatorCompletes30d,
	long leadSubmits30d,
	long inspectionLeads30d,
	long quoteReadyLeads30d,
	long pageCtaClicks30d,
	long resultPrimaryClicks30d,
	String leadRate30d,
	List<OpsReportRow> topLeadEntryPages,
	List<OpsReportRow> topLeadSources,
	List<OpsReportRow> topLeadCampaigns,
	List<OpsReportRow> topLeadRoutingBuckets,
	List<OpsReportRow> topPageCtaPages,
	List<OpsReportRow> topPageCtaPlacements,
	List<OpsReportRow> topPageCtaRoutes,
	List<OpsReportRow> topPageCtaDestinations
) {
}
