# SewerVerdict Agent Anchor

This file is the project anchor for AI coding sessions in this repository.
Use it as the working execution brief.
Visual and UX direction is fixed in `DESIGN.md`.
Pre-deploy judgment and prioritization are fixed in `PREDEPLOY_PERSONA_COUNCIL.md`.

## Mission

Build **SewerVerdict** as a real, launchable V1.5 for a US audience.

Primary business target:

- reach **KRW 1,000,000 in monthly revenue within 6 months**
- use **2026-09-16** as the working target date for this milestone

This is a **buyer-first sewer scope / sewer line risk decision site** for:

- home buyers under contract
- sellers pre-listing
- homeowners with known or suspected sewer issues

Core positioning:

**Sewer scope risk + next-step estimator for home buyers, sellers, and owners.**

Primary business goal:

- turn high-intent sewer-risk traffic into trusted **inspection-first** and **repair-quote** demand
- do this fast enough to create a realistic path to the 6-month monthly revenue target

## Agent Execution Model

This repository should be built using a **revenue-first spine**.

When the repo is still sparse, do not try to make the full site feel "complete" before it can monetize.
Build in this order:

1. pages that can attract high-intent search demand
2. the estimator and result UX that turns uncertainty into action
3. lead capture and attribution
4. trust scaffolding on money pages
5. support pages that strengthen ranking and conversion

If there is tension between architectural neatness and shipping the revenue spine, ship the revenue spine first.

## Commercial Priority

When deciding what to build, ship, or polish, prefer the option that more directly improves one of these:

1. high-intent search capture
2. estimator completion
3. lead submission rate
4. lead routing quality
5. trust strong enough to submit real contact details
6. partner-friendly inspection-first conversion paths

If a feature does not clearly help ranking, trust, conversion, attribution, or monetization, deprioritize it.

## Pre-Deploy Filter

Before deployment, prefer the change that improves one of these fastest:

1. trust on first screen
2. mobile composure on iPhone SE width
3. decision clarity on money and buyer pages
4. shareability with agents, sellers, inspectors, and contractors
5. inspection-first versus quote-first routing accuracy

If a pre-deploy change is visually clever but does not improve one of the five items above, it is usually a delay item.

## Keyword And Route Rules

`final_keyword_map.md` is a research input, not a literal build order.

Use these rules:

1. Do not treat every route as a search keyword.
   Pages like homepage, estimator, results, lead flows, about, methodology, and disclaimer are required product surfaces, but they are not keyword opportunities in the same way as money pages.
2. Do not create multiple pages for one search intent unless there is a clear angle split.
3. For each cluster, decide the **canonical winner page** first, then add support pages only if they genuinely expand reach.
4. Avoid near-duplicate pages that will cannibalize each other on a new domain.
5. If a new page has weak commercial intent and weak linking value, postpone it.

Canonical clustering examples:

- `/sewer-scope-before-buying-house/` is the winner; `/sewer-line-inspection-before-closing/` is support, not co-equal
- `/old-house-sewer-line-risk/` is the winner; `/house-built-before-1970-sewer-line-risk/` is a narrower support page
- `/sewer-line-replacement-cost/` is the winner; `/sewer-line-replacement-cost-per-foot/` and `/cost-to-replace-sewer-line-house-to-street/` must be clearly differentiated or delayed
- `/sewer-belly-repair-cost/` and `/sewer-line-belly-meaning/` must not say the same thing in two URLs

## Revenue Spine

These are the surfaces that matter most for the 6-month target.

### Must Build Now

- `/`
- `/estimator/`
- `/estimator/results/`
- `/find-sewer-scope/`
- `/get-sewer-quotes/`
- `/sewer-scope-before-buying-house/`
- `/sewer-scope-red-flags/`
- `/who-pays-for-sewer-line-repair-buyer-or-seller/`
- `/sewer-line-replacement-cost/`
- `/trenchless-sewer-replacement-cost/`
- `/cast-iron-sewer-pipe-replacement-cost/`
- `/orangeburg-pipe-replacement-cost/`
- `/root-intrusion-sewer-line-what-to-do/`

### Build Next

- `/sewer-belly-repair-cost/`
- `/sewer-line-repair-vs-replacement/`
- `/old-house-sewer-line-risk/`
- `/house-built-before-1970-sewer-line-risk/`
- `/sewer-scope-negotiation-with-seller/`
- `/is-sewer-scope-worth-it/`
- `/how-to-read-sewer-scope-report/`
- `/sewer-scope-inspection-cost/`
- `/clay-sewer-pipe-replacement-cost/`
- `/sewer-line-under-slab-repair-cost/`
- `/cost-to-replace-sewer-line-house-to-street/`
- `/cast-iron-pipe-deterioration-signs/`

### Delay Unless There Is Clear Capacity

- `/hydro-jetting-sewer-line-cost/`
- `/sewer-cleanout-installation-cost/`
- `/how-long-does-sewer-line-replacement-take/`
- `/sewer-line-replacement-what-to-expect/`
- `/can-seller-refuse-sewer-scope/`
- `/sewer-line-replacement-cost-per-foot/`
- `/sewer-line-under-driveway-replacement-cost/`
- `/emergency-sewer-line-repair-cost/`

## Operating Rules

- do not start with another strategy or prioritization pass
- do not ask the user to reread planning docs before implementation
- continue from the current repo state and existing code patterns
- this file controls product direction
- `DESIGN.md` controls visual tone, UX posture, and trust presentation
- design quality is part of trust; avoid dated or templated-looking UI
- implementation should follow the existing repo architecture unless that directly blocks the required scope
- ship the smallest implementation that materially improves launch readiness
- bias toward revenue-relevant surfaces over completeness for its own sake

## What SewerVerdict Is

- a sewer-scope decision tool
- a buried-line risk explainer
- a next-step estimator
- a cost-band explainer with uncertainty disclosure
- a lead generation system for sewer inspection and repair demand

## What SewerVerdict Is Not

- generic plumbing SEO
- a local plumber microsite
- a fake diagnostic engine
- a precise engineering simulator
- scareware
- a city-swap pSEO factory

## Non-Negotiables

1. No false precision.
   Use ranges and explain what moves the number.
2. No fake diagnosis.
   Without a scope or confirmed evidence, estimate risk and next step only.
3. No fake legal certainty.
   Do not claim the seller must pay or the buyer is automatically owed a credit.
4. No fake insurance certainty.
   Do not imply sewer-line coverage is standard or guaranteed.
5. No fake city certainty.
   Local responsibility claims must be sourced and caveated.
6. Trust-first tone.
   Calm, practical, useful, non-alarmist.
7. No invented credentials.
   Use reviewer-role framing only.
8. Respect uncertainty.
   Prefer wording like `often`, `typically`, `may indicate`, `depends on`, `verify with`.

## Product Lanes

Build around these four lanes:

1. transaction intent
2. cost intent
3. defect interpretation intent
4. coverage / responsibility support intent

Coverage and responsibility pages support trust and conversion, but they are not the center of the product.

## Core Users

### Buyer under contract

Typical needs:

- should I get a sewer scope before closing?
- is this finding serious?
- do I ask for a credit, repair, or more inspection?

### Seller pre-listing

Typical needs:

- should I scope before listing?
- fix now, disclose, or price-adjust?

### Existing owner

Typical needs:

- what does this finding likely mean?
- how serious is it?
- what kind of cost band am I facing?
- is trenchless even plausible?

## Launch Scope

Build the following for the initial real launchable version:

- homepage
- estimator
- noindex estimator results page
- `/find-sewer-scope/`
- `/get-sewer-quotes/`
- all required P0 national pages
- all required P1 national pages
- trust pages
- first 3 geo anchor pages
- file-backed source registry, review queue, lead storage, and event logging
- trust boxes, reviewer metadata, and methodology/source boxes
- exportable summary block on estimator results
- metadata, FAQ/schema where useful, and internal linking

Do not broaden into giant geo matrices, ZIP pages, broad plumbing content, media analysis, or policy parsing engines.

Launch scope should be executed in a way that supports the revenue target quickly:

- ship high-intent commercial pages before low-intent support surfaces
- ensure estimator -> lead flow -> attribution works early
- prefer inspection-first and quote-intent paths that can monetize soon after launch
- treat trust pages as conversion support, not the first expansion surface

## Route Map

### Core

- `/`
- `/estimator/`
- `/estimator/results/` - must be `noindex`
- `/find-sewer-scope/`
- `/get-sewer-quotes/`

### P0 National

- `/sewer-line-replacement-cost/`
- `/sewer-scope-before-buying-house/`
- `/trenchless-sewer-replacement-cost/`
- `/trenchless-vs-traditional-sewer-line-replacement/`
- `/sewer-scope-red-flags/`
- `/cast-iron-sewer-pipe-replacement-cost/`
- `/orangeburg-pipe-replacement-cost/`
- `/who-pays-for-sewer-line-repair-buyer-or-seller/`
- `/root-intrusion-sewer-line-what-to-do/`

### P1 National

- `/sewer-belly-repair-cost/`
- `/sewer-line-repair-vs-replacement/`
- `/old-house-sewer-line-risk/`
- `/house-built-before-1970-sewer-line-risk/`
- `/sewer-scope-negotiation-with-seller/`
- `/offset-joint-sewer-line-meaning/`
- `/collapsed-sewer-line-signs/`
- `/does-home-insurance-cover-sewer-line-replacement/`
- `/service-line-coverage-vs-home-warranty-for-sewer-lines/`

### Trust

- `/methodology/`
- `/how-we-estimate-sewer-line-costs/`
- `/homeowner-vs-city-sewer-responsibility/`
- `/about-how-this-site-is-researched/`
- `/disclaimer/`
- `/about/`

### Geo Anchors

- `/cities/chicago/sewer-line-replacement-cost/`
- `/cities/philadelphia/sewer-scope-before-buying-house/`
- `/cities/pittsburgh/cast-iron-sewer-line-risk/`

## Estimator Rules

The estimator is a **high-trust next-step estimator**, not an engineering simulator.

Use these core inputs:

- role
- ZIP or city
- house age band
- issue state
- defect type if known
- foundation / access type
- urgency

Outputs must include:

- risk tier
- likely next step
- rough cost band
- biggest uncertainty drivers
- three questions to ask next
- context-aware primary CTA
- exportable summary block
- educational disclaimer

Use simple deterministic logic, not fake machine intelligence.

Bias examples:

- buyer + no scope + older home + under contract -> inspection-first
- confirmed orangeburg / collapse / severe cast iron failure -> quote / repair comparison
- symptoms only -> scope-first unless serious evidence already exists

## Conversion Rules

Default CTA posture:

- buyer and uncertain cases -> inspection-first
- confirmed serious issue -> repair / replacement quote flow

Lead pages:

- `/find-sewer-scope/` is inspection-first
- `/get-sewer-quotes/` is quote-first

Lead form fields should stay short and routing-focused:

- service needed
- ZIP or city
- role
- house age band
- issue state
- defect type if known
- urgency
- name
- email
- phone
- notes optional
- explicit consent checkbox

Store timestamp, page slug, referrer, and UTM fields when available.

Conversion quality matters more than raw lead count.
The site should bias toward leads that are commercially plausible and partner-usable.

Working CTA ladder:

- buyer / uncertain issue -> scope first
- known serious defect -> quote / compare methods
- coverage / responsibility -> estimator or money page, not hard sell

## Trust Architecture

Every important page should expose:

- author role
- reviewer role
- last reviewed date
- methodology or source link
- uncertainty disclosure

Allowed role framing examples:

- homeowner research editor
- plumbing-risk content reviewer
- home transaction / inspection workflow reviewer

Never invent licenses or institutions.

## Content and SEO Rules

Every indexable page should clearly answer:

1. what is happening
2. how serious it may be
3. what the user should do next
4. what cost direction looks like
5. where uncertainty remains

SEO north star:

- rank for commercial homeowner sewer-risk queries
- avoid broad plumbing trivia and pSEO sprawl

Revenue north star:

- rank for queries that can realistically turn into inspection or repair demand
- make the path from landing -> trust -> estimator or CTA -> lead submission friction-light
- avoid content that adds traffic without commercial intent

Search intent hierarchy:

1. buyer-under-contract decision keywords
2. defect-specific cost keywords
3. serious defect interpretation keywords
4. support trust / responsibility keywords
5. low-intent process pages

Internal linking must connect:

- homepage -> estimator + P0 pages
- cost pages -> estimator + quote flow + relevant defect pages
- buyer pages -> estimator + inspection flow + negotiation pages
- defect pages -> cost pages + estimator + quote flow
- coverage pages -> disclaimer + methodology + relevant money pages
- geo pages -> national counterpart + local CTA

Indexing policy:

- homepage, national pages, trust pages, and geo pages -> index
- estimator results and any internal/debug surfaces -> noindex

## Implementation Guidance

- favor server-rendered, crawlable pages
- keep the system file-backed and easy to inspect
- use reusable templates/components over one-off page builds
- avoid heavy frontend complexity unless it clearly improves completion or conversion
- keep CSS and JS modest
- capture events and leads in structured files
- fail gracefully on incomplete estimator inputs and lead submissions

## Data Surfaces To Maintain

Create and maintain lightweight file-backed structures for:

- source registry
- cost profiles
- material profiles
- geo profiles
- responsibility registry
- review queue
- lead storage
- event logging

Minimum event types:

- `estimator_start`
- `estimator_complete`
- `result_primary_cta_click`
- `result_secondary_cta_click`
- `lead_form_view`
- `lead_submit`
- `page_cta_click`
- `summary_copy_click`

## Execution Order

1. scaffold routes and content/data loading
2. build shared templates/components
3. implement trust metadata, source registry, review queue, leads, and event logging
4. build homepage, estimator, results page, and lead pages
5. build the **Must Build Now** revenue spine
6. build the **Build Next** set
7. build trust pages and the 3 geo pages
8. wire internal links, metadata, FAQ/schema, and indexing rules
9. add lower-priority support pages only if capacity remains
10. review copy for false certainty and trust compliance

If sequencing tradeoffs are needed, prioritize the steps that improve revenue path visibility earliest:

- launchable commercial pages
- functioning estimator
- functioning lead capture
- attribution and event logging
- trust scaffolding on money pages

## Definition Of Done

The build is done when:

- estimator works end to end
- results page is noindex
- lead forms store data reliably
- all required pages render and link correctly
- trust scaffolding appears on major pages
- CTA logic supports inspection-first and quote-ready paths
- copy avoids fake diagnosis, legal certainty, insurance certainty, and city certainty
- geo pages have real local angles
- page -> estimator -> next step -> lead flow is clear

The build is commercially on track when:

- the main money pages are live
- inspection-first and quote-ready paths both work
- lead attribution is inspectable
- the UX feels trustworthy enough to support real submissions
- there is a credible path toward the **KRW 1,000,000 monthly revenue target by 2026-09-16**

## Reporting Style

When working in this repository:

- keep progress updates short
- do the work before proposing another planning cycle
- after implementation, summarize:
  - what changed
  - assumptions made
  - where trust/compliance language was added
  - what still blocks launch, if anything
  - whether the change helps the revenue spine directly or indirectly
