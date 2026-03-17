# SewerVerdict Success Backlog

This file turns `SUCCESS_COUNCIL.md` into the **current** execution queue.
It is no longer a pre-build backlog.
It assumes the current product state is already shipped in the repo and needs to be tightened, measured, and expanded carefully.

## Current State

The repository already has:

- homepage, estimator, noindex results page
- inspection-first and quote-first lead pages
- source-backed national pages and trust pages
- geo profiles, `/cities/`, `/cities/{city}/`, and local city clusters
- attribution-preserving estimator -> result -> lead flow
- sitemap coverage for city hubs
- file-backed leads, events, and estimator drafts
- mobile-safe layouts verified on iPhone SE width

The project does **not** mainly risk failure because of missing product anymore.
The biggest risks now are:

- weak real-world measurement after deploy
- too much expansion before enough winners prove out
- CTR and internal-link opportunities going unmeasured
- lead quality not being inspected tightly enough

## Highest-Leverage Now

### 1. Deploy And Measure

Do first:

1. deploy the current build cleanly
2. verify production estimator -> result -> lead flow
3. connect Search Console and submit sitemap
4. verify robots, canonical, and city hub indexing
5. confirm JSONL event and lead writes in production

### 2. Tighten The Winners

These remain the pages to protect hardest:

1. `/sewer-scope-before-buying-house/`
2. `/sewer-scope-red-flags/`
3. `/who-pays-for-sewer-line-repair-buyer-or-seller/`
4. `/sewer-line-replacement-cost/`
5. `/cast-iron-sewer-pipe-replacement-cost/`
6. `/orangeburg-pipe-replacement-cost/`

What tightening means now:

- better title and meta CTR packaging
- stronger first-screen decision framing
- stronger internal links from hubs and related pages
- clearer estimator and CTA handoff
- stronger share-safe summaries

### 3. Tighten Geo Winners Before Widening

Current geo program is already meaningful.
Do not widen it blindly.

First tighten:

1. Philadelphia
2. Pittsburgh
3. Cleveland
4. Chicago
5. Buffalo

Then evaluate:

1. Milwaukee
2. Cincinnati
3. Baltimore
4. Detroit
5. St. Louis
6. Washington, DC

Use real impression, click, and lead data before adding more cities.

### 4. Improve Lead Quality Visibility

Do next:

1. review which pages create `lead_submit`
2. review which pages create `lead_submit_invalid`
3. log whether inspection-first or quote-ready routing matches later partner usability
4. compare city-hub entry pages against direct geo page entries

## Next Build Candidates

These are the highest-value **not-yet-built** national pages:

1. `/is-sewer-scope-worth-it/`
2. `/how-to-read-sewer-scope-report/`
3. `/sewer-scope-inspection-cost/`
4. `/clay-sewer-pipe-replacement-cost/`
5. `/sewer-line-under-slab-repair-cost/`
6. `/cost-to-replace-sewer-line-house-to-street/`
7. `/cast-iron-pipe-deterioration-signs/`

Add these only after deploy and only if the current winners are stable enough to deserve more support.

## Next Product Tightening Candidates

These do not add breadth.
They improve the current system:

1. Search Console-driven city hub starter weighting
2. lead form friction and field-error polish
3. first-screen share blocks on winner pages
4. page-to-estimator transition copy
5. geo hub internal-link weighting by evidence state

## Delay

Delay these until there is real traction or spare capacity:

1. `/hydro-jetting-sewer-line-cost/`
2. `/sewer-cleanout-installation-cost/`
3. `/how-long-does-sewer-line-replacement-take/`
4. `/sewer-line-replacement-what-to-expect/`
5. `/sewer-line-replacement-cost-per-foot/`
6. `/sewer-line-under-driveway-replacement-cost/`
7. `/emergency-sewer-line-repair-cost/`
8. more cities beyond the current 11-city program

## Cut By Default

Reject these unless hard evidence changes the case:

1. giant city-swap page expansion
2. ZIP code pages
3. neighborhood pSEO
4. broad plumbing maintenance content
5. generic homeowner blog posts
6. heavy dashboard behavior
7. fake insurance or legal decision engines
8. media analysis or upload tooling for this phase

## Working Rule

When unsure what to do next, ask:

1. does this improve a current winner?
2. does this improve trust on first screen?
3. does this improve estimator or lead completion?
4. does this improve shareability?
5. will this matter in the next 6 months?

If not, it can wait.
