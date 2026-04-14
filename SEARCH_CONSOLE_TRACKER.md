# Search Console Tracker

This document is the running log for search visibility work on SewerClarity. Use it to track:
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

## 2026-04-14

### Data
- No fresh Search Console export was pulled during this code pass.
- This entry tracks structural changes only and does not replace the `2026-04-12` performance read.

### Changes
- Default-closed `/ops/report` behind an explicit enable flag and token check, then blocked `/ops/` in `robots.txt`.
- Added a USPS-backed ZIP delivery-market JSON so supported ZIP-only inputs can anchor to covered city markets without pretending that a ZIP proves a municipal transfer or compliance boundary.
- Added explicit `city confirmation still needed` guardrails to estimator results and lead handoff so ZIP-anchored matches no longer read like settled municipal compliance or certificate matches.
- Added an optional street-address municipality lookup path using the official U.S. Census geocoder so ZIP-only users can upgrade into an exact municipality match without inventing new city rules or unsupported compliance pages.
- Narrowed Philadelphia further by surfacing the city's property sales certification on the city hub and treating supported `191xx` ZIPs as a stronger municipal-safe city anchor rather than a generic delivery-market guess.
- Reduced lead-form friction by making phone optional while keeping consent, service, and contact routing fields intact.
- Narrowed homepage, city hubs, page routing, and decision-path logic toward transfer, certificate, compliance, and owner-boundary questions instead of generic sewer-risk framing.
- Promoted transfer and compliance families in page classification without inventing new city compliance pages where no official local signal exists, and added Philadelphia's official property sales certification as a first-class local source.

### Insights
- The wedge-narrowing direction is correct for launch: local transfer and responsibility pages are more defensible than broad cost expansion on a young domain.
- This pass fixed a classification leak where CTA links were causing buyer and cost pages to masquerade as compliance pages, which weakened the intended IA.
- ZIP support is now real for covered USPS delivery markets, but it stays intentionally conservative: a ZIP can anchor the market, not certify the municipal rule.

### Next Check
- After deploy, inspect Search Console impressions and CTR for `/`, `/cities/`, `/sewer-scope-before-buying-house/`, `/who-pays-for-sewer-line-repair-buyer-or-seller/`, and official-signal city pages only.
- Confirm that no external crawler is hitting `/ops/report` and that no staging workflow depends on the old open route.
- Decide next whether to tighten delivery-market ZIPs into stricter municipal ZIP subsets for the strongest compliance cities.

### Decision
- Fix technical and tighten winners

## 2026-04-12

### Data
- Final GSC summary available at review time ran through `2026-04-09`.
- `2026-03-12` to `2026-04-09`: `1 click / 706 impressions / 0.14% CTR / avg position 40.92`.
- Compared with the `2026-04-01` read (`1 click / 400 impressions / avg position 46.96`), impressions increased by roughly `76.5%` while average position improved by about `6` spots.
- US search impressions rose from `287` to `473`, with the single recorded click still coming from US traffic.
- Device split remained desktop-heavy:
  - `MOBILE`: `1 click / 76 impressions / 1.32% CTR / avg position 43.09`
  - `DESKTOP`: `0 clicks / 630 impressions / 0% CTR / avg position 40.66`
- Sitemap status in GSC still showed only `https://www.sewerclarity.com/sitemap.xml`, last downloaded on `2026-04-10`, with `submitted 100 / indexed 0`.

### Top Pages
- `/cast-iron-sewer-pipe-replacement-cost/`: `247 impressions / avg position 37.78`
- `/cities/philadelphia/`: `102 impressions / avg position 46.97`
- `/sewer-scope-red-flags/`: `94 impressions / 1 click / avg position 5.18`
- `/cities/milwaukee/homeowner-vs-city-sewer-responsibility/`: `65 impressions / avg position 6.78`
- `/cities/`: `62 impressions / avg position 4.76`
- `/`: `54 impressions / avg position 2.81`

### Watchlist Pages
- `/cast-iron-sewer-pipe-replacement-cost/`: `247 impressions / avg position 37.78`
- `/orangeburg-pipe-replacement-cost/`: `166 impressions / avg position 33.90`
- `/sewer-line-replacement-cost/`: `162 impressions / avg position 54.84`
- `/cities/philadelphia/`: `102 impressions / avg position 46.97`
- `/sewer-scope-red-flags/`: `94 impressions / 1 click / avg position 5.18`
- `/`: `54 impressions / avg position 2.81`
- No measurable GSC row yet for:
  - `/sewer-scope-before-buying-house/`
  - `/who-pays-for-sewer-line-repair-buyer-or-seller/`
  - `/homeowner-vs-city-sewer-responsibility/`
  - `/sewer-lateral-repair-cost/`

### Top Queries
- `cost of replacing cast iron pipes`: `23 impressions / avg position 47.09`
- `cast iron pipes replacement cost`: `10 impressions / avg position 67`
- `cast iron plumbing replacement cost`: `9 impressions / avg position 47.89`
- `cast iron sewer pipe replacement cost`: `7 impressions / avg position 50.29`
- `"who pays for sewer line repair buyer or seller"`: `3 impressions / avg position 2`
- `"sewer scope before buying house"`: `2 impressions / avg position 2`

### Technical Findings
- Homepage inspection remains clean:
  - `https://sewerclarity.com/` -> `Submitted and indexed`
  - `http://sewerclarity.com/` -> `Page with redirect`
  - both homepage canonical values point to `https://sewerclarity.com/`
- Live responses on `2026-04-12` returned `200` with self-canonical tags for:
  - `/sewer-scope-before-buying-house/`
  - `/who-pays-for-sewer-line-repair-buyer-or-seller/`
  - `/homeowner-vs-city-sewer-responsibility/`
  - `/sewer-lateral-repair-cost/`
- Those same URLs are also present in the live sitemap.
- However, URL Inspection currently shows:
  - `/sewer-scope-before-buying-house/` -> `Discovered - currently not indexed`
  - `/sewer-lateral-repair-cost/` -> `Discovered - currently not indexed`
  - `/who-pays-for-sewer-line-repair-buyer-or-seller/` -> `URL is unknown to Google`
  - `/homeowner-vs-city-sewer-responsibility/` -> `URL is unknown to Google`
- `/cities/philadelphia/` still shows `https://www.sewerclarity.com/sitemap.xml` as a referring URL, so old discovery traces have not fully cleared.

### Changes
- No new code changes were made during this review.
- This review measured the first post-`2026-04-04` and post-`2026-04-06` Search Console response to the narrower buyer / responsibility / scope wedge.

### Insights
- The site is still growing, and this time the growth is better quality than the `2026-04-01` read because impressions increased while average position also improved.
- The main commercial winners are still `cast iron` and `orangeburg`. Their impression growth is real:
  - cast iron: `150 -> 247`
  - orangeburg: `94 -> 166`
- `/sewer-line-replacement-cost/` is still weaker than the two material pages and now lags them more clearly on rank quality.
- The geo layer is still present, but Philadelphia remains a weak commercial closer. It grew only slightly (`90 -> 102`) and still ranks too low for its core local-replacement terms.
- `/sewer-scope-red-flags/` remains the only page with a recorded click and continues to act like the easiest page for Google to trust quickly.
- The new wedge is not disproven, but the national pages carrying it are not yet eligible for a fair verdict because several are not indexed.
- The most important negative signal in this review is not weak CTR. It is that exact-match wedge queries are already appearing, but the wrong URLs are collecting them:
  - `"who pays for sewer line repair buyer or seller"` is showing the homepage, `/cities/`, and `/sewer-scope-red-flags/`, not the dedicated national page
  - `"sewer scope before buying house"` is showing city pages, not the national winner page
- That means the current site architecture may be producing the right query signals before Google has accepted the intended winner URLs.

### Next Check
- In GSC, manually inspect and request indexing for:
  - `/sewer-scope-before-buying-house/`
  - `/who-pays-for-sewer-line-repair-buyer-or-seller/`
  - `/homeowner-vs-city-sewer-responsibility/`
  - `/sewer-lateral-repair-cost/`
- Keep watching whether those four URLs move from `unknown` / `discovered` into `Submitted and indexed`.
- On the next review, verify whether exact-match buyer and responsibility queries begin resolving to the intended national pages instead of the homepage or city pages.
- Keep watching:
  - `cast iron`
  - `orangeburg`
  - `sewer-scope-red-flags`
- Do not treat the new wedge as a failure until the intended national pages are indexed and have had time to compete.

### Decision
- Hold broad expansion.
- Do not judge the `buyer / responsibility / scope` repositioning by CTR yet.
- First fix the indexation gap on the intended winner pages, then re-evaluate query-page matching.

---

## 2026-04-06

### Data
- No fresh Search Console export was captured in this update.
- This entry records a winner-tightening pass on top of the `2026-04-04` repositioning.

### Changes
- Made the homepage first screen explicit about the three main decision paths:
  - no footage yet -> inspection first
  - boundary or ownership unclear -> responsibility first
  - report finding or known defect -> interpretation first, then quote-ready follow-up only if warranted
- Reordered homepage support surfaces so buyer, responsibility, and report-interpretation paths read stronger than broad cost posture.
- Tightened the current priority winner pages:
  - `/sewer-scope-before-buying-house/`
  - `/sewer-scope-red-flags/`
  - `/who-pays-for-sewer-line-repair-buyer-or-seller/`
  - `/homeowner-vs-city-sewer-responsibility/`
  - `/sewer-lateral-repair-cost/`
- Corrected CTA handoffs so inspection-first pages feel inspection-first, responsibility pages route into ownership clarity, and defect pages distinguish clarify versus quote-ready follow-up more explicitly.
- Tightened city hub starter logic and presentation so the first screen now routes:
  - buyer / transfer first
  - responsibility / who-pays second
  - defect interpretation next when available
  - cost only after those paths
- Added richer CTA measurement for:
  - homepage hero path clicks
  - city hub starter clicks
  - winner-page CTA placement
  - route bucket
  - destination type
- Extended internal ops reporting so CTA paths can be reviewed by placement, route, and destination alongside leads and estimator activity.
- Updated tests to lock in the new framing, routing order, and measurement surfaces.

### Insights
- This pass is testing whether the first screen now makes the wedge hard to misread.
- SewerClarity should now read less like a generic sewer-cost publisher and more like a buyer-first sewer decision tool for private-lateral responsibility and report interpretation.
- This is still a hypothesis, not a result. The value of this pass depends on whether cleaner first-screen framing produces better query mix, CTR, and lead routing.

### Next Check
- Review these pages first after deploy:
  - `/`
  - `/cities/philadelphia/`
  - `/cities/milwaukee/`
  - `/sewer-scope-before-buying-house/`
  - `/sewer-scope-red-flags/`
  - `/who-pays-for-sewer-line-repair-buyer-or-seller/`
  - `/homeowner-vs-city-sewer-responsibility/`
  - `/sewer-lateral-repair-cost/`
- Inspect:
  - homepage hero path clicks
  - city hub starter clicks
  - winner-page primary versus secondary CTA clicks
  - estimator handoff versus inspection and lead-route handoff
- Use the next decision cycle for snippet and CTR tightening, not expansion.

### Decision
- Hold broad expansion.
- Measure the tightened winner paths before adding more cities, more cost pages, or broader families.

---

## 2026-04-04

### Data
- No fresh Search Console export was captured in this update.
- This entry records a product and SEO-layer repositioning pass made after the `2026-04-01` review.
- The latest confirmed GSC context still comes from the `2026-04-01` entry:
  - broad cost pages were leading impressions
  - buyer and responsibility wedges were not yet the clearest site-level read
  - this made the current page-role hierarchy a likely weakness rather than just a copy issue

### Changes
- Repositioned the homepage and global navigation to read more clearly as `buyer / private lateral / responsibility` guidance instead of a broad sewer-cost site.
- Reordered home featured links to push these pages harder:
  - `/sewer-scope-before-buying-house/`
  - `/is-sewer-scope-worth-it/`
  - `/who-pays-for-sewer-line-repair-buyer-or-seller/`
  - `/homeowner-vs-city-sewer-responsibility/`
  - `/sewer-lateral-repair-cost/`
- Reordered city hub starter logic so buyer and responsibility pages outrank cost pages when a city cluster loads.
- Sharpened the current winner pages:
  - `/sewer-scope-before-buying-house/`
  - `/who-pays-for-sewer-line-repair-buyer-or-seller/`
  - `/homeowner-vs-city-sewer-responsibility/`
- Added a narrower commercial support page:
  - `/sewer-lateral-repair-cost/`
- Tightened related-linking on adjacent support pages so the internal path now reinforces the new wedge instead of leaking to generic routes.
- Added or updated tests to lock in:
  - homepage wedge emphasis
  - city starter ordering
  - new page rendering
  - sharpened winner-page wording

### Insights
- If a high-agency or "genius" product/SEO persona reviewed this move, the verdict would likely be: this is the right correction, but not proof of success yet.
- The trigger for this pass was not just weak human reaction. It was weak current Google response to the site's broad positioning, which made a simple additive copy pass too shallow.
- The strongest part of today's work is not the new page count. It is the clearer claim about what SewerClarity is for: buyer diligence, private-lateral risk, and city-boundary clarity before quotes or blame.
- This is a more defensible search posture than continuing to look like a broad sewer-cost site while losing to larger publishers on generic cost terms.
- This is still an inference, not a confirmed result: Google may respond better once the site-level hierarchy and internal linking make the wedge clearer, but the response has to be measured rather than assumed.
- The next failure mode to avoid is broadening again too early. If this repositioning does not improve query mix, CTR, or page-role clarity, that will be a signal to tighten further, not to reopen generic expansion.

### Next Check
- After deploy, inspect these pages first in GSC:
  - `/sewer-scope-before-buying-house/`
  - `/who-pays-for-sewer-line-repair-buyer-or-seller/`
  - `/homeowner-vs-city-sewer-responsibility/`
  - `/sewer-lateral-repair-cost/`
- On the next review, verify whether query mix starts shifting toward:
  - buyer-under-contract intent
  - sewer lateral intent
  - responsibility / who-pays intent
  - inspection-before-closing intent
- Check whether the homepage and city hubs begin earning cleaner impressions for the new wedge rather than staying broad and cost-heavy.
- Use `2026-04-18` as the first post-change read if enough fresh GSC data exists by then.
- Use `2026-05-02` as the second read to decide whether to:
  - hold this wedge and tighten titles / CTR
  - add one more compliance-style page
  - or narrow the strategy even further

### Decision
- Hold broad page expansion.
- Measure the wedge response first.
- Treat this as a real repositioning pass, not a cosmetic copy tweak.
- Do not revert to layering more broad pages on top of the old read until this narrower set has been measured.

---

## 2026-04-01

### Data
- Final GSC summary available at review time ran through `2026-03-29`.
- `2026-03-01` to `2026-03-29`: `1 click / 400 impressions / 0.25% CTR / avg position 46.96`.
- Previous review window on `2026-03-23` had `0 clicks / 103 impressions / 0% CTR / avg position 31.41`.
- This means impressions rose by roughly `3.9x` and the property earned its first recorded click, but average position worsened as Google tested the site on a wider set of queries.
- Daily series showed:
  - `2026-03-20`: first recorded click, `1 click / 32 impressions / 3.13% CTR / avg position 32.28`
  - `2026-03-25`: highest daily impressions seen in the current dataset at `58`
  - `2026-03-29`: `34 impressions / avg position 55.74`
- Sitemap status in GSC still showed only `https://www.sewerclarity.com/sitemap.xml`, last downloaded on `2026-03-31`, with `submitted 99 / indexed 0`.
- URL Inspection now showed canonical normalization working on the homepage:
  - `https://sewerclarity.com/` -> `Submitted and indexed`
  - `google canonical = https://sewerclarity.com/`
  - `user canonical = https://sewerclarity.com/`
- URL Inspection for `http://sewerclarity.com/` no longer showed the old canonical drift. It now returned `Page with redirect` and both canonical values pointed to `https://sewerclarity.com/`.

### Top Pages
- `/cast-iron-sewer-pipe-replacement-cost/`: `150 impressions / avg position 41.63`
- `/sewer-scope-red-flags/`: `47 impressions / 1 click / avg position 5.13`
- `/cities/`: `36 impressions / avg position 3.53`
- `/cities/chicago/`: `30 impressions / avg position 16.27`
- `/`: `25 impressions / avg position 2.24`

### Watchlist Pages
- `/sewer-line-replacement-cost/`: `96 impressions / avg position 50.72`
- `/cast-iron-sewer-pipe-replacement-cost/`: `150 impressions / avg position 41.63`
- `/orangeburg-pipe-replacement-cost/`: `94 impressions / avg position 35.29`
- `/cities/philadelphia/`: `90 impressions / avg position 48.63`
- `/`: `25 impressions / avg position 2.24`

### Top Queries
- `cost of replacing cast iron pipes`: `12 impressions / avg position 51.5`
- `cast iron pipes replacement cost`: `7 impressions / avg position 70.29`
- `cast iron plumbing replacement cost`: `7 impressions / avg position 49.86`
- `cast iron sewer pipe replacement cost`: `5 impressions / avg position 52.4`
- `chicago sewer`: `4 impressions / avg position 70.75`

### Country / Device Notes
- US search impressions rose to `287` with `1 click` and avg position `52.55`.
- Canada added `57` impressions and Korea still showed `29` impressions with very strong average position but weak commercial usefulness. This remains an inference-heavy noise bucket, not a trusted demand signal.
- Device split moved from almost all desktop to still desktop-heavy but with the first mobile click:
  - `MOBILE`: `1 click / 47 impressions / 2.13% CTR / avg position 57`
  - `DESKTOP`: `0 clicks / 353 impressions / 0% CTR / avg position 45.63`

### Technical Findings
- Live output on `2026-04-01` was clean:
  - homepage canonical rendered as `https://sewerclarity.com/`
  - `robots.txt` referenced `https://sewerclarity.com/sitemap.xml`
  - `sitemap.xml` rendered only `https://sewerclarity.com/...` URLs
  - Inspection confirms the old `http` canonical bug is resolved at the homepage level.
- However, GSC discovery traces are not fully clean yet:
  - `/sewer-line-replacement-cost/` still shows a referring URL of `http://sewerclarity.com/`
  - `/cities/philadelphia/` still shows a referring URL of `https://www.sewerclarity.com/sitemap.xml`
- This suggests the canonical fix is live, but Google is still carrying some older discovery history.

### Changes
- No new SEO code changes were made during this review.
- This review validated the live deployment of the `2026-03-23` canonical fix and winner-page tightening work.

### Insights
- The site has moved from pure discovery into broader query testing. That is progress, but not yet ranking stability.
- The strongest current revenue-relevant cluster is still material-cost intent, especially `cast iron` and `orangeburg`.
- `/cast-iron-sewer-pipe-replacement-cost/` is now the clearest impression leader, but average rank is still too low for meaningful traffic.
- `/orangeburg-pipe-replacement-cost/` looks healthier than before because it has nearly tripled impressions while keeping better average position than cast iron.
- `/sewer-line-replacement-cost/` gained impressions, but its current query mix is broad and weakly ranked. It is not the strongest winner right now.
- `/cities/philadelphia/` grew from `52` to `90` impressions, but position worsened from `39.65` to `48.63`. The geo hub is still not a direct commercial winner.
- `/sewer-scope-red-flags/` produced the first click, but it is not the main revenue page. That means Google is finding some trust or problem-explainer relevance before it is finding the best money-page fit.
- The old cannibalization-like `Forbes Home` comparison query still appears across multiple pages, so query-page role separation is not fully clean yet.

### Next Check
- In GSC, submit `https://sewerclarity.com/sitemap.xml` directly if the property still only shows the `www` sitemap path.
- Reinspect:
  - `/sewer-line-replacement-cost/`
  - `/cast-iron-sewer-pipe-replacement-cost/`
  - `/orangeburg-pipe-replacement-cost/`
- On the next review, verify:
  - whether `referringUrls` stop showing `http://sewerclarity.com/` and `https://www.sewerclarity.com/sitemap.xml`
  - whether `submitted 99 / indexed 0` starts normalizing in the sitemap report
  - whether `orangeburg` and `cast iron` queries move from page 4-7 into page 2-3 range
  - whether `/sewer-line-replacement-cost/` begins matching cleaner replacement-cost intent rather than scattered long-tail testing

### Decision
- Hold page expansion.
- Keep observing the current winner set.
- If one more check still shows `cast iron` and `orangeburg` leading impressions, tighten those two pages again before adding more national pages.

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

### 2026-04-14 Note
- Closed the expert-review follow-up on transfer/compliance IA and locality certainty.
- `pages.json` now promotes transfer and compliance as first-class families instead of relying on overlapping slug inference.
- City hubs now render transfer and compliance clusters directly, so crawlable city IA matches the intended wedge.
- ZIP or Census county-subdivision matches now stay cautious: they can anchor to a covered market, but they no longer get exact-city pricing lift or municipality certainty language before confirmation.
