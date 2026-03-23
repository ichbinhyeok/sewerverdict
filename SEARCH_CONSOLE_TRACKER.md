# Search Console Tracker

This document is the running log for search visibility work on SewerClarity.

Use it to track:
- exact-date Search Console readings
- what was changed in the product or SEO layer
- what the data likely means
- what to check next before making more changes

## How To Use

- Add a new dated entry at the top. Do not rewrite older entries.
- Use exact dates like `2026-03-23`, not relative phrases like "today".
- Keep `Data`, `Changes`, `Insights`, and `Next Check` separate.
- Do not log guesses as facts. Mark them as inference when needed.
- Do not add new pages just because impressions are low. Log what the current winner set is doing first.

## Current Watchlist

- `/sewer-line-replacement-cost/`
- `/cast-iron-sewer-pipe-replacement-cost/`
- `/orangeburg-pipe-replacement-cost/`
- `/cities/philadelphia/`
- `/`

## Recurring Check Format

Copy this block for the next review:

```md
## YYYY-MM-DD

### Data
- Period checked:
- Total clicks:
- Total impressions:
- CTR:
- Avg position:
- Indexing / sitemap notes:
- Top pages:
- Top queries:

### Changes
- 

### Insights
- 

### Next Check
- 

### Decision
- Hold / tighten winners / fix technical / add page
```

---

## 2026-03-23

### Data
- Search Console access for `sc-domain:sewerclarity.com` was confirmed on `2026-03-23`.
- Final GSC data available at review time ran through `2026-03-19`.
- `2026-02-19` to `2026-03-19`: `0 clicks / 103 impressions / 0% CTR / avg position 31.41`.
- `2025-12-19` to `2026-03-19`: identical to the 28-day window, which indicates meaningful search visibility only started recently.
- Daily visibility effectively began on `2026-03-17`.
- `2026-03-17`: `35 impressions / avg position 2.46`.
- `2026-03-18`: `42 impressions / avg position 47.64`.
- `2026-03-19`: `26 impressions / avg position 44.15`.
- Sitemap status in GSC showed `submitted 99 / indexed 0`, but URL Inspection for key pages returned `Submitted and indexed`.
- URL Inspection confirmed indexed status for:
  - `/`
  - `/sewer-line-replacement-cost/`
  - `/cast-iron-sewer-pipe-replacement-cost/`
  - `/cities/philadelphia/`
- GSC also showed `http://sewerclarity.com/` as indexed with `google canonical = http://sewerclarity.com/` and `user canonical = https://sewerclarity.com/`.

### Top Pages
- `/cities/philadelphia/`: `52 impressions / avg position 39.65`
- `/cast-iron-sewer-pipe-replacement-cost/`: `47 impressions / avg position 17.32`
- `/sewer-scope-red-flags/`: `38 impressions / avg position 4.87`
- `/orangeburg-pipe-replacement-cost/`: `34 impressions / avg position 20.06`
- `/sewer-line-replacement-cost/`: `34 impressions / avg position 4.06`

### Top Queries
- `sewer line replacement philadelphia`: `9 impressions / avg position 78.22`
- `sewer line repair philadelphia`: `5 impressions / avg position 78.80`
- `orangeburg sewer pipe replacement cost`: `4 impressions / avg position 40.25`
- `orangeburg pipe replacement`: `3 impressions / avg position 77.67`
- `sewer lateral replacement near me`: `3 impressions / avg position 51`
- `sewer lateral inspection`: `1 impression / avg position 12`

### Country / Device Notes
- US search impressions: `59` with avg position `44.64`.
- Korea impressions: `28` with avg position `1.93`.
- `KOR` rows did not map cleanly to query/page detail, so this was treated as likely test or early-noise traffic. This is an inference, not a confirmed fact.
- Device split was heavily desktop at this stage: `100 desktop impressions / 3 mobile impressions`.

### Technical Findings
- `http`, `https`, `www`, and `non-www` signals were mixed in GSC.
- Live edge behavior on `2026-03-23` was already redirecting:
  - `http://sewerclarity.com/` -> `301` -> `https://sewerclarity.com/`
  - `https://www.sewerclarity.com/` -> `301` -> `https://sewerclarity.com/`
- Application code was still generating canonical URLs, sitemap URLs, and robots sitemap references from the request host and scheme, which likely allowed earlier host/protocol drift to leak into search signals.

### Changes
- Added `app.base-url` and set the canonical origin to `https://sewerclarity.com`.
- Stopped generating canonical, OG URL, schema URL, `robots.txt`, and `sitemap.xml` from the incoming request host.
- Updated deploy config to pass `APP_BASE_URL=https://sewerclarity.com`.
- Added integration tests to verify canonical and sitemap output remain on `https://sewerclarity.com` even when the request host is `www` or not secure.
- Tightened the winner pages:
  - `/sewer-line-replacement-cost/`
  - `/cast-iron-sewer-pipe-replacement-cost/`
  - `/orangeburg-pipe-replacement-cost/`
- Winner-page tightening focused on:
  - more direct commercial summaries
  - clearer inspection-first versus quote-ready next-step logic
  - stronger price-driver framing
  - broader but more useful directional cost ranges
  - stronger internal links to repair/replacement and trenchless follow-up paths
  - additional source IDs where cost sanity-check support was needed

### Files Changed
- `src/main/resources/application.properties`
- `src/main/java/com/example/sewerverdict/web/SeoController.java`
- `src/main/java/com/example/sewerverdict/web/SeoMetadataService.java`
- `src/main/resources/content/pages.json`
- `.github/workflows/deploy.yml`
- `docker-compose.yml`
- `src/test/java/com/example/sewerverdict/web/SeoControllerIntegrationTests.java`
- `src/test/java/com/example/sewerverdict/web/SiteControllerIntegrationTests.java`

### Validation
- `./gradlew.bat test` passed on `2026-03-23`.

### Insights
- The site is not in a mature ranking phase yet. It is in an initial discovery and trial-ranking phase.
- The highest-value actual commercial signals currently attach to the cost/material pages, not to broad trust pages and not to geo hubs by themselves.
- `/cast-iron-sewer-pipe-replacement-cost/` and `/orangeburg-pipe-replacement-cost/` are the best current early candidates for revenue-relevant traction.
- `/cities/philadelphia/` is getting impressions, but most US commercial queries attached to it are still ranking too low to convert.
- The biggest risk before content expansion was technical signal dilution, not a lack of pages.
- Adding more pages before cleaning canonical and winner-page signal quality would likely have made results harder to interpret.

### Next Check
- Deploy the canonical-fix and winner-page-tightening build.
- In GSC, resubmit `https://sewerclarity.com/sitemap.xml`.
- Reinspect:
  - `/`
  - `/sewer-line-replacement-cost/`
  - `/cast-iron-sewer-pipe-replacement-cost/`
  - `/orangeburg-pipe-replacement-cost/`
  - `/cities/philadelphia/`
- Wait `7-14` days before making another expansion decision.
- On the next review, check:
  - whether `http` canonical traces have disappeared
  - whether sitemap indexed counts start normalizing
  - whether the three winner pages gain impressions and better position on US queries
  - whether direct money pages outrank the city hub for the most commercial search intents

### Decision
- Hold page expansion.
- Tighten and observe the current winner set first.
