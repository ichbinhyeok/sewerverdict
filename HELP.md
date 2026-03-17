# SewerClarity Engineering Help

This file replaces the Spring Boot boilerplate help page.
Use it as the quick operational reference for the current repo state.

## What This Repo Is

SewerClarity is a server-rendered Spring Boot site for:

- buyer-first sewer scope decisions
- defect interpretation
- repair-vs-replacement cost framing
- inspection-first and quote-first lead capture
- geo-specific sewer responsibility and market-context pages

The site is intentionally file-backed and easy to inspect.

## Current Footprint

As of **2026-03-17** the repo ships:

- homepage, estimator, noindex results page
- `/find-sewer-scope/` and `/get-sewer-quotes/`
- **24** national/trust pages
- **11** geo city hubs and **52** geo pages
- file-backed estimator drafts, leads, and event logs
- source registry, geo profiles, responsibility registry, material profiles, cost profiles, and review queue

Current geo hub cities:

- Philadelphia
- Pittsburgh
- Cleveland
- Chicago
- Buffalo
- St. Louis
- Washington, DC
- Milwaukee
- Cincinnati
- Baltimore
- Detroit

## Core Commands

Run tests:

```powershell
./gradlew test
```

Run locally:

```powershell
./gradlew bootRun
```

Run locally on another port:

```powershell
./gradlew bootRun --args="--server.port=18097"
```

## Main Source Files

Controllers:

- `src/main/java/com/example/sewerverdict/web/SiteController.java`
- `src/main/java/com/example/sewerverdict/web/EstimatorController.java`
- `src/main/java/com/example/sewerverdict/web/LeadController.java`
- `src/main/java/com/example/sewerverdict/web/SeoController.java`

Content and geo:

- `src/main/resources/content/pages.json`
- `src/main/resources/data/geo/geo_profiles.json`
- `src/main/resources/data/raw/source_registry.csv`
- `src/main/resources/data/raw/responsibility_registry.csv`
- `src/main/resources/data/raw/cost_profiles.json`
- `src/main/resources/data/raw/material_profiles.json`
- `src/main/resources/data/raw/review_queue.csv`

Templates:

- `src/main/resources/templates/home.html`
- `src/main/resources/templates/estimator.html`
- `src/main/resources/templates/result.html`
- `src/main/resources/templates/lead.html`
- `src/main/resources/templates/content-page.html`
- `src/main/resources/templates/cities.html`
- `src/main/resources/templates/city-hub.html`

Frontend assets:

- `src/main/resources/static/site.css`
- `src/main/resources/static/site.js`

## Storage Layout

Runtime storage lives under `storage/`.

- `storage/estimator-drafts/estimator-drafts.jsonl`
- `storage/leads/leads.jsonl`
- `storage/events/events.jsonl`

Current event types:

- `estimator_start`
- `estimator_step_view`
- `estimator_step_validation_error`
- `estimator_complete`
- `result_primary_cta_click`
- `result_secondary_cta_click`
- `lead_form_view`
- `lead_submit`
- `lead_submit_invalid`
- `page_cta_click`
- `summary_copy_click`

## Product Rules That Matter In Code

- Results page must stay `noindex`.
- Deterministic routing only. No fake machine intelligence.
- Unconfirmed issues should bias to inspection-first.
- Confirmed major issues should bias to quote-ready.
- No fake legal, insurance, or city certainty.
- Local responsibility language must stay source-backed.
- Mobile quality on iPhone SE width is a product requirement, not polish.

## Documentation Map

- `AGENTS.md`: current product direction and execution rules
- `DESIGN.md`: visual and UX posture
- `PREDEPLOY_PERSONA_COUNCIL.md`: pre-deploy judgment rules
- `SUCCESS_COUNCIL.md`: six-month success doctrine
- `SUCCESS_BACKLOG.md`: current execution backlog

## Current Best Next Work

If you are resuming work from the current repo state, the best next areas are:

1. deploy and measure real impressions and lead quality
2. tighten winner-page titles, metas, and first screens
3. reduce lead and estimator friction further
4. weight city hubs and internal links using real data
5. add the next national pages only after current winners stabilize

