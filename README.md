# SewerClarity

SewerClarity is a buyer-first sewer scope and sewer-line risk decision site for a US audience.
It is built as a server-rendered Spring Boot product with file-backed content, geo data, and attribution storage.

## What It Does

- explains sewer risk for buyers, sellers, and owners
- routes uncertain cases to inspection-first
- routes confirmed major issues to quote-ready paths
- gives broad cost direction with uncertainty disclosure
- captures leads with attribution and routing context
- connects national pages to source-backed local city hubs

## Current Product State

As of **2026-03-17**, the repo includes:

- homepage, estimator, noindex results page
- inspection-first and quote-first lead routes
- **24** national/trust pages
- **11** city hubs and **52** geo pages
- sitemap coverage for city hubs
- file-backed estimator drafts, lead storage, and event logging

Current city hubs:

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

```powershell
./gradlew test
./gradlew bootRun
./gradlew bootRun --args="--server.port=18097"
```

## Important Files

- `src/main/resources/content/pages.json`
- `src/main/resources/data/geo/geo_profiles.json`
- `src/main/resources/data/raw/source_registry.csv`
- `src/main/resources/data/raw/responsibility_registry.csv`
- `src/main/resources/templates/`
- `src/main/resources/static/site.css`
- `src/main/resources/static/site.js`

## Storage

Runtime JSONL files are written under `storage/`:

- `storage/estimator-drafts/estimator-drafts.jsonl`
- `storage/leads/leads.jsonl`
- `storage/events/events.jsonl`

## Documentation

- `AGENTS.md` for product and execution rules
- `DESIGN.md` for visual direction
- `PREDEPLOY_PERSONA_COUNCIL.md` for launch judgment
- `SUCCESS_COUNCIL.md` for six-month success rules
- `SUCCESS_BACKLOG.md` for the current backlog
- `HELP.md` for engineering operations

