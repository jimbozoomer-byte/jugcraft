# Concordance garden cohort: six garden spirits on the shared pact record (contract)

Status: **contract (GC-00); unit 1 (GC-01 to GC-04) and unit 2 (GC-05, Bathin) built, run in CI and recorded (see
[unit 1 as built and its evidence](#unit-1-as-built-and-its-evidence) and
[unit 2 as built and its evidence](#unit-2-as-built-and-its-evidence)); unit 3 (GC-06, Marax) built and recorded, not
yet compiled or run in CI (see [unit 3 as built and its evidence](#unit-3-as-built-and-its-evidence)); units 4 to 7 not
built here; nothing of the cohort is merged.** The contract was written read-only from
the worktree of `claude/chunk-d-pact-hall` at `5eba71ced` (main's
`78f9353a7` + Chunk B #465 + Chunk C #466 + Chunk D #474, all then open and unmerged), from the five survey reports
written for it (`survey-pacts.md`, `survey-gardens.md`, `survey-assets.md`, `survey-sources.md`, `survey-plans.md`; kept with the session's working notes, not committed), from a critique of its
first draft (27 corrections, each checked against the code before it was applied; see
[Critique corrections not applied](#critique-corrections-not-applied)) and from the code they cite, re-read where this
page relies on it (see [How this contract was checked](#how-this-contract-was-checked)). It is committed as
`docs/features/concordance-garden-cohort.md` (GC-00) on a new branch, `claude/garden-cohort`, stacked on
`claude/chunk-d-pact-hall` (the owner chose "New stacked branch" on 26 October 2026; GC-S16), to be reviewed as the
fourth stacked pull request. That stack has ended: on 27 October 2026 the owner's #494 (`46bac6b34`) brought Chunks B
to D to main in one squashed commit with the October 26 community batch, and #465, #466 and #474 were closed without
merging. The cohort has merged main since (`cfeded41e`, made against Chunk D's final head `301d64028` as its base, and
`10566d074`) and is reviewed as draft pull request [#526](https://github.com/jimbozoomer-byte/jugcraft/pull/526)
against main.

Proposal: the owner answered Chunk D's checkpoint on 26 October 2026 (recorded there as D-D3 to D-D5 and here as
**GC-D1 to GC-D3**): "keep stolas", "do D-P50", "gardens next". The garden cohort is the first of the twelve suggested
cohorts of six (03:163-165): six spirits, one a unit, each briefed (identity, silhouette, service, upkeep, conflict,
refusal) before its art, each service mechanically distinct (03:167-177), built on the shared pact record. "Keep stolas"
kept his look and manner. The other two parts of that recommendation (turning Stolas toward whoever speaks to him, and
giving each later spirit its own silhouette) were not answered separately (CD:2780), so this contract carries them as
the session's decision **GC-S23**, not as the owner's: "a spirit faces whoever speaks to it" is shared groundwork that
Stolas also uses. Chunk D's proposal said the cohort "needs Chunks B to D merged" (CD:2710); this contract stacks it on
the unmerged Chunk D instead (GC-S16), also the session's decision. (Chunk D reached main with #494 on 27 October
2026, so since then the cohort stands on main.)

Decision provenance. **GC-D*n*** is the owner's. **GC-S*n*** is "Decided by the session": the owner told Chunk D's
session "dont ask me to approve just finish this whole part" (D-D2, 23 October 2026, given for Chunk D); this workflow
carries it as a standing instruction not to be asked for approvals inside a chunk. That reading is the session's, not
the owner's, so **no GC-S decision is the owner's**; each stays reviewable at the cohort's checkpoint, which asks at most
three questions (09:18). **GC-P*n*** is a unit's proposal (a starting value or a choice inside a ticket), numbered in
reserved blocks so units built in parallel never collide: unit 1 GC-P1 to GC-P39, unit 2 GC-P40 to GC-P59, unit 3 GC-P60
to GC-P79, unit 4 GC-P80 to GC-P99, unit 5 GC-P100 to GC-P119, unit 6 GC-P120 to GC-P139, unit 7 GC-P140 to GC-P159.
Every D-S and D-P of Chunk D is still a proposal the owner has not answered (only D-D3 to D-D5 were answered); this
cohort builds on them as built and inherits that status.

Owner: @jimbozoomer-byte. AI-assisted implementation with Claude Code; the model is named in each commit's trailer.

Citations. Plan chapters as `NN:line` (`docs/plans/concordance-expansion-2026-10-10/NN-*.md`), `MP` its
`CLAUDE-MASTER-PROMPT.txt`; `CD` is
[Chunk D's contract](concordance-chunk-d.md), `CC` Chunk C's, `SP` [stolas-pact.md](stolas-pact.md), `ECO`
`arcane-concordance-ecology.md`. Code as `file:line` at `5eba71ced`: `S/` =
`src/main/java/io/github/jimbozoomer/jugcraft/concordance/spirits/`, `W/` = `.../concordance/worker/`, `G/` =
`.../concordance/garden/`, `E/` = `.../concordance/ecology/`, `C/` = `src/client/java/io/github/jimbozoomer/jugcraft/client/`,
`T/` = `src/gametest/java/io/github/jimbozoomer/jugcraft/test/`, `D/` = `src/main/resources/data/jugcraft/`. `mc263src` is
the decompiled 26.3 reference (its `HEAD`); "GeckoLib" names the 5.5.7 sources and "SmartBrainLib" its sources as read
from a reference copy whose version is unrecorded (the pin is 2.0.3; GC-02 confirms against it). A fact marked *(survey)* comes from a survey report and was not re-read for this page. Source quotations carry the source survey's
labels: **[S]** exact wording matched by a web search over transcriptions of the 1904 text; **[s]** the sense confirmed,
the wording not; **[U]** recollection, unverified. No scan was opened (`survey-sources.md` §1-2). Prefixes used here and
nowhere else in Chunks A to D: tickets **GC-01** to **GC-10**; completion evidence **GE1** to **GE9**; baseline gaps
**GB1** to **GB28**; shared generalizations **SG1** to **SG12**.

## Player outcome

The plan's bar (03:167-177; 09:16): six spirits whose services differ in what they do, each with a discoverable
invitation, transparent upkeep, one conflict, a refusal and a clean departure, two memorable details and evidence; the
first complete before the rest multiply; no universal scripting language. With the session's decisions GC-S1 to GC-S26,
a survival player who understands the Binding Arts and Verdant Husbandry can:

- **Discover (unit 1):** learn **The Garden's Regard** in the codex's Binding category (examine a garden specimen, whose
  tooltip and examine line say what it teaches; harvest an organism crop by hand; keep a bound worker at work); the journal's pacts section names their next step. Each of the
  six spirits has its own codex page saying what it does, what it costs and what it needs beside the perch. Styx is not
  needed: the garden route never passes through him.
- **Choose and invite (unit 1):** at an empty pact perch they own, a short list names every spirit they may invite
  (Stolas among them) and what is still missing for the rest; choosing one brings that spirit, which turns to face them.
- **Negotiate (unit 1, the same screen as Stolas's):** three dialogue beats in the spirit's own voice, then every term
  before anything is spent; accept pays the compact (ordinary materials, no Astral Resonance; no garden term or refusal mentions an
  astrolabe); decline or any refusal
  spends nothing and names its reason, including the spirit's own ("there is nothing here I could mend").
- **Keep six garden pacts, each a different kind of help (units 1 to 6):**
  - **Buer** eases harvest strain on their beds by daylight, one bed at a time, by the bed's own tending rule.
  - **Bathin** sets Verdance down at once where it is wanted: from a Verdant Heart into a far Gleaner, or into a Ley
    Pylon away from the beds, with nothing ever in transit.
  - **Marax**, when asked, sends his familiar over the garden; it names one bed and the one change that would make it
    grow better, and changes nothing.
  - **Foras** grafts host crops that would gain by it, spending one donor from a store beside his perch for each, and
    grafts them again when a replanting has cleared it.
  - **Bifrons**, by night, runs a root from a full bed to a hungry one: two nutrients taken, one given.
  - **Amdusias** bends the tree beside his perch over their beds, so the stir of a nearby workshop reaches the crops
    softer (the area's disturbance read up to 2 lower for crops that need no stir, never below 0, never for a Heart,
    never easing harvest strain, and never summed with another canopy).
- **Settle a matter with each (units 1 to 6):** every spirit raises one complication in play (an overworked bed, a
  Heart that already has its own pylon, a proud familiar, a graft that would gain nothing, which beds are reserves, the
  stir a Gloamcap wants); the player resolves it by a task in the world or a choice at the perch (the roster says
  "awaiting your answer" while a choice waits). Nothing is lost while it waits, and killing the spirit is never a route.
- **Keep, suspend, end (shared, unit 1):** upkeep as Stolas's (D-D4: D-P50 accepted, suspension still pauses the clock);
  the roster shows every pact with its own portrait; ending returns the escrow once and the spirit departs in its own
  manner.
- **Be faced (unit 1, GC-S23):** every pact spirit, Stolas included, turns to whoever is speaking to it, and while it works
  turns to the bed or reservoir it is working on.

Already built, and not rebuilt here:

| System | Where |
|---|---|
| The pact record: `BoundWill` with an optional `Pact` (place, hold, beneficiaries, presence, upkeep, service, window, day's count); escrow; history of 8 departures | `resource/BoundWill.java:15, 58-107`; `S/BoundWills.java:44-86, 163-233`; `S/PactHistory.java:27-38` *(survey)* |
| The shared holder: seal, verify, recall, toggle, look, deposit, end, seat, stranded, broken; `OnBreak.UNSEAT` for the perch | `S/AgreementHolder.java:123-864` (outline re-read) |
| Pure pact rules: refusal order, upkeep, grace and absence with D-P50, `serves` | `W/Pacts.java:92-106, 215-248, 384-433` (`serves` re-read) |
| An offer as strict data (schema 1) | `W/PactParser.java:29-98` (re-read); `D/concordance/pact/stolas.json` |
| The pact perch: owner, one pact a place, holds nothing, ticks once a second | `S/PactPerchBlockEntity.java:33-180` (re-read) |
| The pact screen protocol: 9 actions (append only), 4 modes, bounded lists (3 beats, 16 terms, 12 notes, 8 choices, 8 offerings); the state payload's 13 fields in one `StreamCodec.composite`, whose largest 26.3 overload takes 14 | `S/PactRequestPayload.java:22-24`; `S/PactStatePayload.java:30-95` (re-read); mc263src `net/minecraft/network/codec/StreamCodec.java:533`; `tools/concordance_pacts.py:102-103` |
| Stolas and Star-reckoning, his Regard, sounds, body and evidence | `S/StolasEntity.java`, `S/StarReckoning.java`, `S/StolasRegard.java`; SP |
| The roster screen, the journal's pacts section, Jade for perch and spirit | `S/Roster.java`; `journal/Journal.java:95`; `compat/jade/PactDataProvider.java`, `WorkerDataProvider.java` |
| Worker base: think every 20 ticks, SmartBrainLib brain with `LookAtTarget` always running (its default target test refuses an entity target in a worker's brain: GB21), no portals, persistent | `S/WorkerEntity.java:59, 187-194, 197-201, 223, 229` (re-read) |
| Verdant Beds: nutrients 0-32 through `take` (exact) and `give` (never makes what does not fit); strain 0-3 with `settle`, `rest`, `compost`, `tend` (600 ticks a bed, `tendedAt` unsaved); one graft, cleared only when another organism grows | `G/VerdantBedBlockEntity.java:62-82, 115-135, 304-386, 453-489` (re-read) |
| Habitat: 5 factors, strain against the disturbance limit, a 5x5 area sample of 100 reads, 16 samples a level a tick, a sample cached 200 ticks; `Habitat.with(factor, value)` | `E/Sampler.java:16-77`; `E/SampleBudget.java:9`; `E/Habitat.java:56-58`; `E/Verdict.java:37-58` (re-read) |
| The Verdant Heart (2 Verdance a thriving beat, 64 cap, pours 3 for 2 only into adjacent pylons its keeper's party owns, or no one owns; `drawInto` through `Transfers`; its habitat is its bed's `habitat`, as a crop's is), the Gleaner (1 Verdance a harvest, draws up to 8 from Hearts within 4) | `G/VerdantHeartBlockEntity.java:44-170` (`:99`, `:152-154`); `G/GleanerBlockEntity.java:34-45, 124-137` (re-read) |
| Authority: `present`, `presentNear`, `answering`, `mayChange`, `mayChangeFor` | `concordance/Authority.java:59-140` (re-read) |
| The economy audit's conversions ("gaining cycles: none") | `tools/concordance_economy.py:42-59` (re-read) |
| Load card machinery (`Measure`, `Traffic`, `Stats`), the opted-in `load_card` input; the two-JVM client pattern | `T/ConcordancePactHallLoadGameTests.java`; `.github/workflows/build.yml:8-15, 90-139` *(survey)*; `T/ConcordancePactNetworkTests.java` |
| Survival test players (vanilla's mock is creative) | `TestPlayers.survivor`; CD Facts |

## Units

Each unit is a review boundary: its own commits on `claude/garden-cohort`, its records, and its checks green before the
next starts. D-D5 asks for one spirit a unit; unit 1 carries only the shared groundwork Buer uses, so the first spirit is
complete end to end (invitation, terms, service, upkeep, conflict, refusal, departure, body, sounds, evidence) before any
second spirit exists (09:16; MP:66), and nothing ships in unit 1 that only a stub service would exercise (MP:67:
"extend only seams needed by working content"). Each later
shared mechanism lands with its first user: the accord machinery and the bed index's devices with Bathin (unit 2), the
synced work target with Marax (unit 3), the bed index's containers with Foras (unit 4), the canopy registry with Amdusias
(unit 6); each first obligation's hook lands with its spirit. The plan's "three to five units" (09:3) is exceeded: seven
plus the baseline, recorded here as Chunk D recorded its own count (CD:133; GC-S19). Unit 2 follows unit 1; units 3 to 6
may then be built in parallel, each from unit 2's head, with their reserved GC-P blocks; the player's order is
unchanged. Every Java compile and game test runs only in CI (GC-S24; [Checks](#checks-before-each-unit-lands)).

| Unit | Tickets | What it delivers | Record (proposed) | State |
|---|---|---|---|---|
| 0 | GC-00 | This contract | `docs/features/concordance-garden-cohort.md` | Contract |
| 1 | GC-01, GC-02, GC-03, GC-04 | A compile-only first push (SG1 to SG3, Stolas a thin subclass); one perch for many spirits (Stolas unchanged); a spirit faces whoever speaks; the groundwork Buer uses (the bed index for beds, the beneficiary rule, the read-only `mayTendAt` and `cachedHabitat`, obligations with `TEND`, The Garden's Regard reachable to understood); **Buer complete**: strain tending, the overworked bed, his body from the owner's Decor Blossom | this contract; new `buer-pact.md`; `stolas-pact.md`; `arcane-concordance-workers.md`; `arcane-concordance-ecology.md` | Built (`62b0095fa` to `4ca363d38`) and recorded; in CI run 40605564166 every unit-1 server and client test passed (its one remaining failure, after a re-run, is main's `HerobrineClientGameTests`); not merged (draft #526); [evidence](#unit-1-as-built-and-its-evidence) |
| 2 | GC-05 | **Bathin**: Verdance carrying, two roads from one Heart; with him, the shared accord machinery (`resolve`, the perch's `accord`, the payload's matter and answers, the roster's "awaiting your answer") and the bed index's Hearts, Gleaners and pylons | new `bathin-pact.md` | Built (`28a963b63` to `85eb1a0ba`, and `d7185041b` since) and recorded; in CI run 40777920181 every unit-2 server test passed with and without the optional integrations, and the garden client class passed; not merged (draft #526); [evidence](#unit-2-as-built-and-its-evidence) |
| 3 | GC-06 | **Marax**: one-correction reading by his familiar, the proud familiar; the synced work target and its renderer hook; The Garden's Regard mastery reachable from here (three spirits) | new `marax-pact.md` | Built (`736d94541` to `1473e99f1`) and recorded; the shared prefix (GC-06a, GC-06b and its review's fixes) was in the head of CI run 40777920181, which passed in full; Marax himself (`bed9f73b4` on) not yet compiled or run in CI; not merged; [evidence](#unit-3-as-built-and-its-evidence) |
| 4 | GC-07 | **Foras**: graft keeping from a store beside the perch, the logician's objection; the bed index's containers; the owner library's second edited part in `tools/owner_art.py` | new `foras-pact.md` | Not started |
| 5 | GC-08 | **Bifrons**: root-running with half lost, which beds are reserves | new `bifrons-pact.md`; `tools/concordance_economy.py` | Not started |
| 6 | GC-09 | **Amdusias**: the bending canopy (a one-factor disturbance shelter: the pure rule in `ecology`, the registry in `garden`), the stir a Gloamcap wants | new `amdusias-pact.md`; `arcane-concordance-ecology.md` | Not started |
| 7 | GC-10 | Combined fixes, the cohort's whole path, its load card, client evidence (the line-up), the two-client test, records, the checkpoint and handoff | this page; the six records; `arcane-concordance-journey.md`; `docs/TESTING.md` | Not started |

Order of the spirits after Buer goes from least to most invasive: Bathin moves an existing essence through existing
transfer rules; Marax reads and changes nothing; Foras spends items; Bifrons adds an economy conversion; Amdusias changes
how a habitat is read.

## Baseline (GC-00)

### Facts

- **Base:** `5eba71ced` (26 October 2026 16:38:13Z, "Concordance Chunk D: the owner's checkpoint answers"), the head of
  `claude/chunk-d-pact-hall` (#474). Its main is `78f9353a7` (23 October 2026).
- **Main has moved:** `origin/main` is `7317e5d44` (#475, "Integrate Jimtafacts workshops, spellbooks and the
  aircraft/furniture/graves batch", 25 October 2026 18:56:57Z, read by REST today), **not an ancestor** of `5eba71ced`.
  Between the merge-base and it: 59,927 files (33,508 under `art/`, 230 Java). It touches shared hot files the cohort
  also edits (lang, `sounds.json`, both `fabric.mod.json`, `tools/check_mod_data.py`, `JugcraftClient.java`,
  `.github/workflows/build.yml`, `values.json`, the survival registry, `generated-material-resources.json`) and three
  Concordance files: `Authority.java` (adds `remoteModemActor`, `unownedTinyTntActor` and a `Level` overload of
  `mayChange`; `mayChangeFor` unchanged), `ConcordanceEffects.java` and `ConcordanceProgress.java` (Focus capacity
  bonuses). Nothing in `spirits/`, `worker/`, `garden/` or `ecology/`. Merge it down the stack before unit 1 (GC-S16).
- **No local game build:** the session's environment has no Minecraft jar and no Loom cache (`~/.gradle/caches/fabric-loom`
  is absent), so every Java compile and every game test runs only in CI: the cohort pull request's `pull_request` run (#526),
  or a `workflow_dispatch` of the Build workflow (`.github/workflows/build.yml:1-15`). Locally, only the Python generators
  and audits run. A compile error (an over-long `StreamCodec.composite`, a wrong SmartBrainLib signature) shows only in
  CI (GC-S24).
- **The stack beneath:** #465 (Chunk B, base main), #466 (Chunk C, base B), #474 (Chunk D, base C), all open; a review
  change to any is a merge into the cohort. *(Since 27 October 2026 all three are closed without merging; their content
  is on main through the owner's #494.)*
- **No spirit but Stolas has a brief, a roster entry or any code** (`survey-sources.md` §1: no candidate name in lang,
  assets or code, so no name collides).
- **The pact system is spirit-agnostic in its record and rules and Stolas-specific around them** (`survey-pacts.md` §0,
  §3): the perch, menu, keeping page, entity, service, Regard, journal hook, Jade, roster portrait, 36 generic lang lines,
  generators, audits and about 120 test references name Stolas.
- **Garden facts the services rest on** (re-read): `tend(now)`, `take`, `give`, `setGraft`, `clearGraftUnlessFor`,
  `strainNow` and `keeper()` are public on the bed; the gestures that call them (`VerdantBedBlock.tendBy`, `pour`,
  `OrganismCropBlock.graftBed`) are player-keyed and spend the player's `garden` rate gate (`G/VerdantBedBlock.java:189,
  208`; `G/OrganismCropBlock.java:305`). Only the cultivation practice is recorded by garden code
  (`G/OrganismCropBlock.java:348`).
- **The owner library** has two unused, unregistered creature rigs (Ars Elemental's Siren and Flarecannon); every other
  creature rig is a registered Jugcraft identity (`survey-assets.md` §1) *(survey)*. No unicorn, bull, serpent-man or
  treant identity is registered (grep of lang and the menagerie catalogs for this page).

### Status delta: the plan's text against the code at `5eba71ced`

| The plan says | What is true | Evidence |
|---|---|---|
| "Build twelve cohorts of six after the pilot" (03:163) | The pilot (Stolas) and a contrasting service (the workshop hand) are built; the perch offers only Stolas | `W/PactParser.java:38` (`REQUIRED`); `S/PactPerchBlockEntity.java:68-69, 95-106` |
| "Assign each actual roster member only after checking ... source material" (03:165) | Checked through search transcriptions of the 1904 text only; no scan opened; no brief exists | `survey-sources.md` §1-2 |
| "One signature service ... mechanically distinct" (03:170) | The service is a closed list with one implementation | `W/PactParser.java:29-31`; `S/PactMenu.java:327-336` |
| "Transparent upkeep and suspension" (03:171) | Built for any pact; D-P50 accepted (D-D4); suspension pauses the clock | `W/Pacts.java:215-248`; CD D-D4 |
| "One conflict resolved by a player choice or task" (03:172) | No conflict mechanism exists; Stolas's records name none | `survey-plans.md` §2.1 row 5 |
| "Two small interaction details" (03:174) | None named for Stolas | CD:2921-2922 |
| "Implement Stolas and one deliberately different service before freezing the shared schema" (03:177) | Done; S1-S6 proposed, not frozen; the owner's answers did not address it | CD:2099-2115 |
| "Tend an eligible garden bed after checking permission" (03:186) | No such job; tending is a player gesture | `G/VerdantBedBlock.java:208-218` |
| "A job adapter needs canPlan, reserve, begin, progress, complete, cancel and recover" (03:191) | None; the courier decision is shared by porter and hand; "after a third service" (CD:2113) | `S/CourierWork.java` |
| Season shelter (03:75) | Not built (deferred by Chunk C) | CC:2319 *(survey)* |
| Grafting (03:76, 03:80-81) | Built: one graft (Dewlit), one a bed | `D/concordance/graft/dewlit.json`; `tools/concordance_ecology.py:176` *(survey)* |
| Bounded neighbourhoods, cached habitat (03:91) | 5x5 sample, 16 a level a tick, cached 200 ticks; reading a bed samples when stale | `E/Sampler.java:16-21`; `E/SampleBudget.java:9`; `G/VerdantBedBlockEntity.java:179-193` |
| "Search the authorized library before modeling" (03:159) | Searched (`survey-assets.md`); see the art plan | this page |
| Chunk D's recommendation, not answered separately by D-D3 (CD:2780): "a spirit faces whoever speaks to it" | Summoned at yaw 0, no look target ever set; `LookAtTarget` registered, but its default target test would refuse an entity target at once (GB21) | `S/PactPerchBlockEntity.java:137`; `S/WorkerEntity.java:186-194` |

### Defects and gaps found (the cohort must not inherit them)

| # | Gap | Evidence | Closed by |
|---|---|---|---|
| GB1 | The perch offers only Stolas: `definition()`, `call()` refuses any other counterpart, `summon` creates only `Workers.STOLAS` | `S/PactPerchBlockEntity.java:68-69, 95-106, 129-145` | GC-01 (closed as built, unit 1) |
| GB2 | Stolas's sky observation is required for every invitation and acceptance | `S/PactMenu.java:181, 189-191, 290, 475` | GC-01 (closed as built, unit 1) |
| GB3 | The service action, choices and keeping rows are Star-reckoning's | `S/PactMenu.java:327-336, 413-440`; `S/PactKeeping.java:219-234` *(survey)* | GC-01 (closed as built, unit 1) |
| GB4 | End, seat and the stranded view pass `PactDefinition.STOLAS`: a stranded garden pact could never be seated or ended at a perch | `S/PactMenu.java:345, 352, 392` | GC-01 (closed as built, unit 1) |
| GB5 | Mastery leak: `stolas_regard`'s mastered rule matches any `jugcraft:pact_service` key, so a garden obligation recorded there would master Stolas's Regard | `D/concordance/research/stolas_regard.json`; `rules/EvidenceRule.java:77-78, 111` (re-read) | GC-03 (own practice): closed as built, unit 1: a separate practice recorded only by `PactObligations` (`anObligationNeverMastersStolassRegard`) |
| GB6 | Roster: kind `pact` only for Stolas's counterpart; an unloaded pact presence is portraited as Stolas | `S/Roster.java:86-87, 391, 488` (re-read) | GC-01 (closed as built, unit 1) |
| GB7 | Jade's entity reading is typed to Stolas | `compat/jade/WorkerDataProvider.java:60-62`; `S/PactReading.java:50-55, 70-99` *(survey)* | GC-01 (closed as built, unit 1) |
| GB8 | The screen's sign is fixed to `jugcraft:stolas_starflower`; its service section is Star-reckoning's | `C/PactScreen.java:62` (re-read), `:162-176, 200-223` *(survey)* | GC-01 (closed as built, unit 1) |
| GB9 | 36 generic pact lang lines are worded for Stolas ("Stolas keeps this perch", "he returns") | `tools/concordance_pacts.py:153-222, 327-376, 437-454, 514-535` *(survey)* | GC-01 (closed as built, unit 1: ten lines take the name, GC-P12) |
| GB10 | Spirits face south; the evidence camera stands south (D-P133) | `S/PactPerchBlockEntity.java:137` | GC-02 (closed as built, unit 1) |
| GB11 | Reading a bed's habitat spends the level's sample budget when the sample is stale; no cached-only read | `G/VerdantBedBlockEntity.java:179-193` | GC-03 (closed as built, unit 1) |
| GB12 | `Garden.nearest` builds and sorts every position of its box (867 at reach 8 for `bedsAround`); the Gleaner's 729-position search measured 333 µs | `G/Garden.java:329-348`; ECO:203-209 *(survey)* | GC-03 (bed index; closed as built, unit 1) |
| GB13 | The graft gesture is private and player-keyed | `G/OrganismCropBlock.java:305-328` | GC-07 |
| GB14 | `tendedAt` is not saved: after a restart a bed may be tended again at once | `G/VerdantBedBlockEntity.java:77-78` | Not changed; bounded by the quota (Risks) |
| GB15 | `check_geckolib` reads only box UV (`u, v = cube["uv"]`); the Decor Blossom is per-face UV | `tools/check_mod_data.py:17415` | GC-04 (closed as built, unit 1) |
| GB16 | `check_stolas_body` pins `OWNER_FILES` and `ADAPTED` to Stolas's three files | `tools/check_mod_data.py:18904` | GC-01, GC-04 (closed as built, unit 1) |
| GB17 | The journal's pacts section goes through `StolasRegard::report` only | `journal/Journal.java:95` (re-read) | GC-01 (closed as built, unit 1) |
| GB18 | The repository's only Goetia citation names the work "The Lesser Key of Solomon" and links Gutenberg #72679, which Gutenberg records as L. W. de Laurence's 1916 reprint of the Mathers text | `docs/features/styxhexenhammer.md:37` (re-read); Gutenberg's record *(survey)* | GC-04 records, after the record is checked: **open**: Gutenberg's record not read (`www.gutenberg.org` was not reachable from the session on 28 October 2026), so `styxhexenhammer.md:37` is unchanged |
| GB19 | Main's #475 is not in the stack | REST today | before unit 1 (GC-S16): closed (`54a2acb6c`) |
| GB20 | No conflict or complication mechanism exists for any pact | `survey-plans.md` §2.1 | GC-04 (task): closed as built, unit 1 (Buer's overworked bed); GC-05 (choice): closed as built, unit 2 (Bathin's two roads from one Heart, the accord machinery) |
| GB21 | `LookAtTarget` is registered with SmartBrainLib's default `invalidTarget` (`!pos.isVisibleBy(entity)`, checked as it starts); vanilla `EntityTracker.isVisibleBy` needs the target in `NEAREST_VISIBLE_LIVING_ENTITIES`, which the worker's only sensor (`NearbyPlayersSensor`) never fills, so an entity look target would never start | `S/WorkerEntity.java:187-194`; SmartBrainLib `api/core/behaviour/custom/look/LookAtTarget.java:31, 171`; mc263src `net/minecraft/world/entity/ai/behavior/EntityTracker.java:51`; SmartBrainLib `NearbyPlayersSensor.java:30` | GC-02 (closed as built, unit 1) |
| GB22 | `PactStatePayload` already has 13 fields in one `StreamCodec.composite`; 26.3's largest overload takes 14, so four new top-level fields would not compile | `S/PactStatePayload.java:81-95`; mc263src `StreamCodec.java:533` | GC-01 (one nested field; closed as built, unit 1) |
| GB23 | The terms page and the cannot-afford refusal name "Astral Resonance from your astrolabe" whatever the resonance (`d.resonance()`); `term.quota` reads "%s a game day", wrong for a cap | `S/PactMenu.java:301, 489-490, 514`; lang `message.jugcraft.concordance.pact.term.compact`, `.answer.cannot_afford`, `.term.quota` | GC-01 (resonance-free keys): closed as built, unit 1; GC-09 (a per-work quota key): open |
| GB24 | The owner's Decor Blossom has four zero-thickness cubes each drawing two opposite faces on one plane (GeckoLib draws entities with `entityCutout`, which does not cull in 26.3); `tools/art_check.py` reads block, item and quad models only, not `.geo.json` | `Magic/assets/ars_jimbaux/geo/decor_blossom.geo.json`; GeckoLib `renderer/base/GeoRenderer.java:73-75`; `docs/ART_DIRECTION.md:31`; `tools/art_check.py:489-490, 662` | GC-04 (closed as built, unit 1) |
| GB25 | `tools/owner_art.py` reads only `Blocks/` and `Magic/`, and adapts only through Stolas's table and `stolas_body` | `tools/owner_art.py:75, 78, 130-140` | GC-04 (the cohort's table and adapters): partly closed, unit 1; the second edited part waits for GC-07 or GC-09 |
| GB26 | `tools/concordance_progression.py` honours `distinct` only for `examine`, so a practice needing three spirits would read as reachable with one | `tools/concordance_progression.py:527` | GC-03 (closed as built, unit 1) |
| GB27 | The roster's status is a saved id from a closed enum; nothing can say "awaiting your answer" | `S/WorkerRoster.java:33-40`; `W/Status.java` | GC-05 (closed as built, unit 2: `Status.AWAITING_ANSWER`, appended, saved on the roster as `awaiting_answer`) |
| GB28 | Three ambience families the asset survey suggested are stereo in every file, which `check_pacts` refuses: `cavern.candy_cavity_ambience_additions`, `cavern.forlorn_hollows_ambience_additions`, `cavern.abyssal_chasm_ambience_additions` | Vorbis headers read for this contract (every file two channels) | Art plan (mono events named) |

### Open pull requests (REST, 26 October 2026, about 17:30Z; refresh before each unit, 09:11)

| PR | What it is | Files it shares with the cohort's likely edits | Bearing |
|---|---|---|---|
| #465, #466, #474 | Chunks B, C, D: the cohort's base | everything | Conflict by construction; merge main (#475) down the stack first |
| #478 (on main), #480 (on #478) | Screen styles; the trade look | `C/ScreenKit.java` (#478) | The pact screens use `ScreenKit.Fitted`; merge before GC-01's screen work if it lands first |
| #472 | The Crowfield and the Harvest Crown | lang, gametest `fabric.mod.json`, `check_mod_data.py` | Hot files only (no `agriculture/Crow*` file in its list) |
| #468, #476 | The fall's wild harvest; wildlife in landscape biomes | gametest `fabric.mod.json`, `check_mod_data.py` | Hot files only |
| #452 | Peepo jobs, stock targets, uniforms | `.github/workflows/build.yml`, gametest `fabric.mod.json` | The cohort edits nothing under `local/peepo`; workflow additions must merge with it |
| #467 | Storage crates | `JugcraftClient.java`, `JugcraftJadeClient.java`, `JugcraftJadePlugin.java`, gametest `fabric.mod.json`, `docs/TESTING.md`, the encyclopedia and handbook | Textual risk in the Jade plugin and client (SG8) and the client registrations |
| #470, #473, #459, #455, #458 | Guns 14D to 15A; Retro Trader | lang, `sounds.json` (generated), gametest `fabric.mod.json` (#455 also `check_mod_data.py`; #458 also `JugcraftClient.java`) | `sounds.json` is generated: the cohort's 42 events go through the generator |
| #454, #449, #448, #453 | Seasonal audit, quartz, Frostward College, lair inventory | gametest `fabric.mod.json`, `check_mod_data.py`, lang, `JugcraftClient.java` (#448) | Hot files only |
| #477, #479 | Tower cost order; blueprint table fit | none of the cohort's files | None |

**Refreshed for unit 1's records (REST, 28 October 2026):** #465, #466 and #474 were closed without merging on 27
October 2026 (about 01:16Z); their content is on main in the owner's #494 (`46bac6b34`, merged 01:14Z the same day,
one squashed commit with the October 26 community batch), so the first row above is history. Main's head at the
cohort's last CI run was `233cca018` (#525). The other rows were not re-read for this refresh.

**Hot shared files:** lang `en_us.json`, `sounds.json` (generated), both `fabric.mod.json`, `tools/check_mod_data.py`,
`tools/concordance.py`, `JugcraftClient.java`, `JugcraftJadePlugin.java`, `JugcraftJadeClient.java`, `ScreenKit.java`,
`values.json`, the survival registry, `generated-material-resources.json`, `.github/workflows/build.yml`,
`docs/TESTING.md`, `docs/WHAT_EXISTS.md`, `CHANGELOG.md`.

### Checks before each unit lands

1. Refresh main and the open pull requests (REST, read only); merge main into `claude/garden-cohort` if it moved,
   regenerating generated files, never hand-merging them *(amended unit 1: the B to D stack ended with #494 on 27
   October 2026)*.
2. Before any vanilla, GeckoLib or SmartBrainLib call is written, check it against mc263src, GeckoLib 5.5.7's sources and
   SmartBrainLib 2.0.3's (the pinned `smartbrainlib-fabric-26.3-2.0.3`); each ticket records the check (class, member,
   line). The build cannot be compiled locally (Facts), so this is the only check before CI.
3. Locally: run the generators the unit changes (generated JSON, lang and `sounds.json` must be current,
   `docs/TESTING.md:5`); `python3 scripts/check_repository.py`; `python3 tools/check_mod_data.py`;
   `python -m unittest discover -s scripts/tests`; `python3 tools/concordance_journey.py --check`;
   `python3 tools/owner_art.py --check` (units grafting owner parts or palettes); `python3 tools/concordance_economy.py`
   (unit 5); `git diff --check`.
4. In CI only: `./gradlew build` (compilation and server game tests), by the cohort pull request's `pull_request` run;
   `-PjugcraftOptionalIntegrations=false` for every unit (each changes Jade).
5. In CI only: `./gradlew runClientGameTest -PclientTests=...` for the classes `tools/select_client_tests.py` picks, with a
   `WEIGHTS` entry for each new class and its selection unit test.
6. CI by workflow_dispatch of the Build workflow with every client class (and `load_card`, `clients_without_optional` in
   unit 7). Each command reported on its own as run and passed, run and failed, or not run; a local generator or audit
   is never reported as a build.
7. Unit 1's first push is a compile-only slice (SG1 to SG3, Stolas a thin subclass of `PactSpiritEntity`, no new content),
   so CI shows the extraction compiles and every Stolas test passes before anything is added.

## The six spirits

### The roster and why (GC-S1)

The 1904 text (Mathers, translator, credited as "a Dead Hand"; Crowley, editor; *The Book of the Goetia of Solomon the
King*, 1904) ties seven of its 72 spirits to herbs, plants, trees or woods; one is Stolas (built). That the other 61
entries name none is the surveyor's recollection, not a checked sweep **[U]**, to be confirmed against a scan
(`survey-sources.md` §3, "The other 61 entries"; an extended web search for herb-teaching spirits returned only Buer,
Bathin, Morax, Foras and Stolas [s]). The other six are the cohort, so no plant claim is stretched (03:165): **Buer (10), Bathin (18), Marax (21), Foras (31), Bifrons (46),
Amdusias (67)**. Alternates: Decarabia (69; herbs only in Weyer/Scot, birds in Mathers), then Barbatos (8; birdsong, no
plant link). Three of the six (Bathin, Marax, Foras) share Stolas's phrase "the virtues of herbs and precious stones",
so **every service below is built on each spirit's other, distinct trait** in the source, never on that shared phrase
(`survey-sources.md` §1, §4).

Rules for all six (GC-S14): use the 1904 headings as names and record the other spellings; quote the source in game only
after the quotation is checked against a scan (Gutenberg #72679, recorded correctly as de Laurence's 1916 reprint of the
Mathers text, or archive.org `ac_goetia`); cite edition and entry number in each record as `styxhexenhammer.md:37` does
for Stolas; import no seal, conjuration, invocation, "Exorcist commands" framing, "Hell" title or legion count; nothing
traced from the 1863 *Dictionnaire Infernal* plates; no modern adaptation's design, personality or lore (Genshin Impact,
Gundam: Iron-Blooded Orphans, the Megami Tensei series, Fate/Grand Order); family-friendly limits: no real herb as real
medicine, no corpses or graves, no pentagram, no wine transmutation. Every form below is Jugcraft's, drawn from the
source's words; where the game goes beyond the text it says "Jugcraft adaptation".

### Common terms of every garden pact (GC-S7, GC-S8, GC-S10)

- **Where:** the pact perch (`jugcraft:pact_perch`, unchanged); one pact a perch; the area is the service radius round the
  perch, same dimension (`Place.within`, cube).
- **Whose land:** a service acts on a bed, crop or reservoir only when (1) its keeper is a beneficiary (`personal`: the
  holder; `party`: the holder's party at that moment), (2) the holder is present near the perch (online, alive, in the
  perch's level and within `Pacts.REACH` = 64 blocks of it: `Authority.presentNear(level, holder, perch, Pacts.REACH)`,
  the test upkeep already uses, `S/AgreementHolder.java:515`, and Stolas's `decide`, `S/StolasEntity.java:295`), and
  (3) that holder may change the block by hand (`Authority.mayChange`), each checked at the moment of the change. So a
  service never runs while no upkeep accrues: time away is never owed, and never served either. Every `work` step runs
  only after the base's `decide` has returned `IDLE` (the presence's UUID and record verified, its pact's state read),
  or `UPKEEP_DUE` with the holder near *(amended unit 1, GC-P27: an offering due serves through its grace, as its terms
  say, the status staying `UPKEEP_DUE`; `decide` returns the pact's own state before it asks presence, so the base serves
  under `UPKEEP_DUE` only when `Authority.presentNear(level, holder, perch, Pacts.REACH)` finds the holder, and a garden
  service asks again at each change through `GardenPacts.target`)*.
  No stand-in ever answers for a garden service; with the holder away or far off the spirit waits (`owner_offline`).
  Research is checked at invitation and sealing only (the hand's precedent, CD schema review).
- **Bounded work:** one think every 20 ticks (`WorkerEntity.THINK_TICKS`); at most one change a think; targets from the
  bed index (GC-03: loaded chunks only, at most 64 beds, refreshed every 200 ticks; 16 of each device from unit 2 and 16
  containers from unit 4); no sample taken by any spirit (readings use cached samples).
- **Presence:** each spirit keeps its perch (no walking, no paths, no chunk loading); it works at a distance inside its
  area, facing the target, playing its work gesture, with one bounded particle burst at the target. Its station is on
  the perch (rooted or hovering bodies) or on the free side of the perch facing its inviter (standing bodies).
- **Upkeep:** the Gentle shape as built and as D-D4 left it: one offering set every 7 game days from escrow, one game day
  of grace after a warning, up to 4 periods ahead, the compact paying the first; suspension stops the service, takes
  nothing, loses nothing; a pact never ends by itself; time away is never owed; D-P50 applies.
- **Compact:** ordinary materials only, resonance 0 (no Celestial Attunement on the garden route; contrast D-P123). The
  terms page and the cannot-afford refusal use resonance-free wording, so no garden term names an astrolabe (GC-01).
- **Matters:** a complication resolved by a task (Buer's, Marax's) is a service row on the kept page and in Jade; one
  resolved by a choice is asked on the kept page, its answer kept as the perch's `accord`, and the roster says "awaiting
  your answer" until it is given (unit 2 onward; GC-05).
- **Discovery:** one cohort research, **The Garden's Regard** (`jugcraft:garden_regard`), for all six (GC-S9); each
  spirit's own invitation needs its own fixture in the area, and its own codex page.
- **First obligation:** each spirit's own by-hand garden act inside its area while its pact serves, recorded once a
  player as practice `jugcraft:garden_pact_service` keyed by the counterpart; three distinct spirits' obligations master
  The Garden's Regard (so mastery is reachable from unit 3, the third spirit).

### Dossier 1: Buer (entry 10), strain tending

1. **Identity.**
   - *Source phrases:* "The Tenth Spirit is Buer" [S]; "He appeareth in Sagittary, and that is his shape when the Sun is
     there." [S]; "He teaches Philosophy, both Moral and Natural, and the Logic Art, and also the Virtues of all Herbs and
     Plants." [S]; "He healeth all distempers in man, and giveth good Familiars." [S]; "a Great President" [s].
     **The identity rests on "the Virtues of all Herbs and Plants" (the only entry naming plants, by the surveyor's
     recollection of the other entries [U], to be confirmed against a scan) and "He healeth all distempers".** Jugcraft adaptation: his healing is of a garden's distemper, a bed's harvest strain; no real remedy
     for a real illness.
   - *Silhouette:* a rooted **corolla**, about 1.7 blocks across: four broad petals round an upright pistil that leans like
     an arrow toward whatever he attends (Sagittary as a motif only, never a centaur or archer); legless, the only rooted
     spirit. Body: the owner's Decor Blossom, its texture byte for byte and its geometry adapted (stem lengthened,
     twin faces dropped; art plan). Not imported: the lion-headed wheel of five
     goat legs (de Plancy and Le Breton, 1863), Genshin Impact's Buer (Nahida).
   - *Personality:* a physician's calm; exact and kind; disapproves of overwork; speaks in diagnoses.
   - *Beats (proposals, GC-P20):* greeting "A bed that is worked must also be mended. I am Buer; I mend."; service "By
     daylight I ease what your harvests strain, one bed at a time, by the bed's own rule and no faster."; price "A
     Mendvetch each week, for my own mending. And when I call a bed overworked, feed it before you ask me again."
2. **Invitation and entry route.** The Garden's Regard understood; an awake Verdant Bed the inviter keeps within 8 blocks
   of the perch. Codex: Binding, The Garden's Regard, page "Buer". The journal's pacts section names the next step.
3. **Service: strain tending** (`strain_tending`). Each think while serving: from the bed index, the awake beneficiary bed
   with the highest `strainNow()` (then nearest) that the bed's own rule allows tending now (`mayTendAt(now)`, new and
   read-only), is not overworked and that the present holder may change; `bed.tend(now)` eases one strain (it first
   counts a harvest seen, as by hand); the task is counted on the record. The 600-tick rule belongs to the bed, so his
   tending and the holder's share it. No nutrients, items or practice for the holder.
4. **Upkeep and suspension (GC-P21).** `#jugcraft:buer_offerings` (Mendvetch) ×1 every 7 game days, 1 day's grace, 4
   ahead; compact: 4 Mendvetch, 4 bone meal. Terms: radius 8, hours 0 to 12,000 (daylight), 16 beds eased a day,
   beneficiaries `party`. Suspended: no tending; nothing taken or lost.
5. **Conflict: the overworked bed** (resolved by a task). A bed he eased today that stands at strain 3 again is
   overworked: he names it in a service row on the kept page and in Jade ("worked faster than it heals: feed it"; not on
   the roster, whose status is a saved id, GC-S11), and will not tend it while it stays at 3. Composting it (bone meal or fertilizer by hand, or a Mulch Maw's nutrient) eases a strain
   by the bed's own rule; at 2 he takes it back. Derived from the bed's strain and his memory of today's eases (at most
   16 positions, not saved: a restart forgets the complaint, never a debt).
6. **Refusal and departure.** Refusal: "There is nothing here I could mend." (no awake bed of the inviter's within 8).
   Departure: his petals close and he sinks into the perch; struck off the roster, escrow returned once, a history entry.
7. **Interaction details.** (a) Outside his hours his petals fold shut and he sleeps (the `waiting` loop at night, with
   the kept page saying "asleep until dawn"). (b) Before he unfurls over a bed, his pistil points at it for a moment
   (attend, then the work gesture), so a watcher sees which bed he chose.
8. **Evidence.** *Accessibility:* every cue subtitled and every gesture paired with words on the kept page, the roster and Jade; its screens keyboard-only and narrated, fitting 320x180 and GUI 4; the overworked row read aloud; asleep and overworked said in words, never by pose alone. *Ownership:* `buerTendsOnlyBedsOfHisBeneficiaries`, `buerNeverTouchesAClaimedOrDormantBed`, SG12's `anotherPlayerCannotAcceptForTheHolder` and `...WaitsForANearHolder`. *Save/reload:* SG12's entity-key case for `jugcraft:buer`; his memory of eases is unsaved by design (`anOverworkedComplaintIsForgottenOnRestart`). *No duplication:* he moves no item and no nutrient (`buerEasesOneStrainAtTheBedsOwnRate` checks both); SG12's `aCopiedPerchIsInert` and `onePresencePerPact`; `twoBuersShareOneBedsTendWindow` (two holders' Buers over one party bed ease it once a window).

### Dossier 2: Bathin (entry 18), Verdance carrying

1. **Identity.**
   - *Source phrase:* "The Eighteenth Spirit is Bathin. He is a Mighty and Strong Duke, and appeareth like a Strong Man
     with the tail of a Serpent, sitting upon a Pale-Coloured Horse. He knoweth the Virtues of Herbs and Precious Stones,
     and can transport men suddenly from one country to another." [S]. Other spellings: Bathim (the seal's caption),
     Bathym, Mathim, Marthim (Weyer) [s]. **The identity rests on "transport ... suddenly from one country to another"**
     (Jugcraft adaptation: an essence, not people; no player transport, which belongs to a travel cohort).
   - *Silhouette:* a broad-shouldered upper body rising from a thick, coiled serpent tail, about 1.9 blocks; no legs; he
     hovers coiled on the perch. The horse is left out (its "Pale-Coloured" echoes Revelation's pale horse). Built
     geometry. Not imported: the Dodge SRT Demon badge; any game or anime design.
   - *Personality:* brisk, proud of speed, scornful of roads and waiting; few words.
   - *Beats (GC-P40):* greeting "Roads are for those who must walk them. I am Bathin."; service "What your Hearts hold, I
     set down where it is wanted: a Gleaner far off, a pylon away from your beds. Nothing is ever on the way."; price
     "Chaff, four to the week. I carry the green; keep me in husk."
2. **Invitation.** The Garden's Regard understood; a Verdant Heart the inviter keeps within 10 blocks, and a Gleaner or Ley
   Pylon within 10.
3. **Service: Verdance carrying** (`verdance_carrying`). Each think while serving, one move (a task): from a beneficiary
   Heart with Verdance to either a beneficiary Gleaner with room that is not within the Gleaner's own reach (4) of that
   Heart, moving up to 8 through the Heart's `drawInto` (both ownerships checked by `Transfers`), or a Ley Pylon owned by
   the holder's party (or no one) that is not beside that Heart, through the existing `verdance_to_ley` conversion (3
   for 2), at most 3 batches. One server step: the source falls and the target rises in the same tick; nothing is held by
   Bathin (S1). No new conversion. *(Amended unit 2, GC-S25: a Heart still beating is his source only once it holds a
   whole move of 8; one that makes no more, whatever it holds.)*
4. **Upkeep (GC-P41).** `#jugcraft:bathin_offerings` (Verdant Chaff) ×4 every 7 game days; compact: 2 ender pearls, 8
   Verdant Chaff. Terms: radius 10, any hour, 12 moves a day, `party`.
5. **Conflict: two roads from one Heart** (choice). A Heart beside its own pylon already pours into it. Until the holder
   answers, he leaves such Hearts alone and raises it once: "Your Heart already has a road. Shall I take from it too?"
   Answers: **leave it** (`yield`) or **take from it** (`claim`), kept as the perch's `accord`; changeable at the perch.
   Until answered the roster says "awaiting your answer". He is the accord machinery's first user (GC-05).
6. **Refusal and departure.** Refusal: "Nothing here to carry, or nowhere to carry it." Departure: none seen to leave: his
   coil tightens and he is gone in a ring of dust.
7. **Details.** (a) He never travels: arrival and departure are instant, a dust ring and no path. (b) When he carries,
   both reservoirs shimmer on the same tick and nothing crosses between them (no travelling particle).
8. **Evidence.** *Accessibility:* every cue subtitled and every gesture paired with words on the kept page, the roster and Jade; its screens keyboard-only and narrated, fitting 320x180 and GUI 4; its matter and answers read aloud; the both-ends shimmer has a subtitle and a kept-page line ("moved 8 Verdance to the Gleaner at x, y, z"). *Ownership:* `bathinHonoursTheHeartsParty`, `aResolveFromAnotherPlayerIsRefused`, SG12's `...WaitsForANearHolder`. *Save/reload:* SG12's entity-key case; `anAnsweredAccordSurvivesARestart` for `yield`/`claim`; `anUnknownAccordReadsAsUnanswered`. *No duplication:* `bathinMovesVerdanceExactly`, `bathinNeverHoldsVerdance`, `noVerdanceIsMintedByCarrying`, SG12's `onePresencePerPact`.

### Dossier 3: Marax (entry 21), one-correction reading through his familiar

1. **Identity.**
   - *Source phrases:* "The Twenty-first Spirit is Marax." [S]; "a Great Earl and President" [s]; "like a great Bull
     with a Man's face" [s]; "His office is to make Men very knowing in Astronomy, and all other Liberal Sciences" [S];
     "also he can give good Familiars, and wise" [S], who know "Herbs and Stones which be precious" [S] (the words joining
     those fragments [U]). Other spelling: Morax (Weyer/Scot). **The identity rests on "give good Familiars, and wise"
     who know herbs: the knowledge is the familiar's, not his.**
   - *Silhouette:* a low, massive bull about 1.4 blocks high and 2.2 long, with a broad, calm, simple face (the "man's
     face" kept gentle, never uncanny), mossed shoulders; a tiny leaf-winged familiar rides between his horns. Built
     geometry. Not imported: Genshin Impact's Morax (Zhongli); the name is the 1904 heading "Marax" (GC-S1).
   - *Personality:* slow, courteous, scholarly; defers to his familiar on plants; looks at the sky by night.
   - *Beats (GC-P60):* greeting "I am Marax. I do not dig; I keep a friend who knows the ground."; service "Ask, and my
     familiar will read your beds and tell you one thing to change. Only one: the rest you learn by doing it."; price
     "Two sheets of paper a week. My familiar writes small."
2. **Invitation.** The Garden's Regard understood; an awake bed with an organism crop the inviter keeps within 8 blocks.
3. **Service: one-correction reading** (`habitat_reading`, holder only, `personal`). On the holder's request at the perch
   (the screen's service action), the familiar reads up to 8 beneficiary beds from their **cached** samples
   (`cachedHabitat`, new; no sample is taken), picks the bed whose verdict is worst, and names the **one** factor change
   that would raise it a step (stalled to tolerating, tolerating to thriving); when no single change would (two or more
   factors outside), it names the first outside factor in `Factor` order and says it is "one of n" to mend. With the
   factor it names the existing remedy: moisture
   (water within 4, a sprinkler, a canteen), light (open sky, a lamp, or the Dewlit graft for a Sunpetal at 9 or 10),
   nutrients (bone meal, the Maw, a fixer), diversity (another kind within 2), disturbance (magic within 2 of the plant;
   for a plant that needs some, a quiet source), strain (rest, compost, tending). A pure function over `Niche`, `Habitat`
   and `Verdict` (`HabitatAdvice`, new). It changes nothing. This takes Stolas's deferred "botanical interpretation"
   (03:150; CD:2722) for the garden cohort (GC-S3).
4. **Upkeep (GC-P61).** `#jugcraft:marax_offerings` (paper) ×2 every 7 game days; compact: 1 book, 2 Sunpetal, 2
   Dewmoss. Terms: radius 8, any hour, 2 readings a day, `personal`.
5. **Conflict: the proud familiar** (task). When the familiar has named the same bed and factor at three readings running
   with no change to that bed's verdict, it refuses to read that bed again ("It will not repeat itself a fourth time")
   and reads the next-worst instead, until that bed's verdict changes. Not saved (a restart forgets the pique).
6. **Refusal and departure.** Refusal: "My familiar reads what grows. Nothing grows here." Departure: the familiar settles
   between his horns, he turns, walks two steps and fades.
7. **Details.** (a) By night, between readings, he lifts his head to the sky (the astronomy of his entry; cosmetic loop).
   (b) During a reading the familiar flies to the bed it names and hovers there while the answer is on screen (a synced
   work target, network only, and a renderer hook that moves the familiar's bone toward it, its culling box widened to
   the target; GC-06).
8. **Evidence.** *Accessibility:* every cue subtitled and every gesture paired with words on the kept page, the roster and Jade; its screens keyboard-only and narrated, fitting 320x180 and GUI 4; its matter read aloud; the reading is a sentence naming bed, factor, value, need and remedy, never a colour or a particle alone. *Ownership:* `onlyTheHolderMayAskForAReading` (holder only); `maraxNeverReadsAStrangersBed`; SG12's `...WaitsForANearHolder`. *Save/reload:* SG12's entity-key case; the familiar's pique unsaved by design. *No duplication:* `aReadingNamesOneCorrectionAndChangesNothing` (nothing changes, no sample taken); SG12's `onePresencePerPact`.

### Dossier 4: Foras (entry 31), graft keeping

1. **Identity.**
   - *Source phrases:* "The Thirty-first Spirit is Foras. He is a Mighty President, and appeareth in the Form of a Strong
     Man in Human Shape." [S]; "He can give the understanding to Men how they may know the Virtues of all Herbs and
     Precious Stones" [S]; he "teacheth the Arts of Logic and Ethics in all their parts" [S]; "If desired he maketh men
     invincible [or invisible], and to live long, and to be eloquent." [S] (only "to live long" is used; the doubtful
     "invincible" is left out); he can "recover things Lost" [S]. Other spelling: Forcas (Weyer 1563); not Furcas (50).
     **The identity rests on "to live long" and "recover things Lost"** (Jugcraft adaptation: a graft's life, and a graft
     lost to replanting recovered), with "Logic and Ethics" giving his conflict.
   - *Silhouette:* a tall, broad, stooped elder about 2.2 blocks, robe of bark and moss, a satchel of cuttings at his hip,
     long arms, the owner's acorn cap on his head (a non-creature owner part:
     `Edited-2026-10-18-Part2/jims mobs/geo/item/armor/acorn.geo.json` with its texture
     `jims mobs/textures/item/armor/acorn.png`; art plan). Built geometry plus that graft.
   - *Personality:* deliberate; argues kindly; a keeper who hates waste; asks "why" before he does anything.
   - *Beats (GC-P80):* greeting "I am Foras. I keep what would be lost, and I argue with whoever wastes it."; service
     "Leave cuttings in the store beside me, and I will graft your hosts that would gain by it, and graft them again when
     a planting loses it."; price "A honeycomb a week. Long keeping wants something sweet."
2. **Invitation.** The Garden's Regard understood; a plain container beside the perch (his store); a host crop of a known
   graft (today Sunpetal, for Dewlit) on a bed the inviter keeps within 8 blocks.
3. **Service: graft keeping** (`graft_keeping`). Each think while serving, one graft (a task): a beneficiary bed whose crop
   is the host of a known graft, carrying no graft the rules know; the present holder may change both crop and bed; a
   donor in a plain container touching the perch that the holder may change. In one server step: check every
   precondition first (the host, no graft the rules know, both Authority checks, a donor present); then extract one donor
   in a Fabric transaction and commit it; then apply the graft through `Grafting.apply` (extracted from `graftBed`:
   `setGraft`, which cannot fail, and `forgetVerdict`). A failed precondition or a failed extraction spends nothing, and
   nothing in the world changes inside the open transaction. "Again when lost": a graft survives replanting of its host
   and is cleared only when another organism grows on the bed (`G/VerdantBedBlockEntity.java:475-480`); a bed whose host
   has returned after that is simply a host without a graft, so he grafts it again. Under his prudent default he
   grafts only where the graft improves the cached verdict.
4. **Upkeep (GC-P81).** `#jugcraft:foras_offerings` (honeycomb) ×1 every 7 game days; compact: 4 Dewmoss, 1 honeycomb.
   Terms: radius 8, any hour, 8 grafts a day, `party`.
5. **Conflict: the logician's objection** (choice). Hosts he skips because a graft would not improve them (a Dewlit graft
   narrows moisture: on a dry bed it would harm) he names: "Three hosts here would gain nothing; shall I graft them
   anyway?" Answers: **only where it helps** (`prudent`, the default) or **graft every host** (`always`), kept as `accord`.
6. **Refusal and departure.** Refusal: "Where shall I keep the cuttings? Set a store beside me." Departure: he bows, folds
   his satchel and fades.
7. **Details.** (a) His acorn cap tips at the greeting and at each graft. (b) He counts aloud: his assent line and the kept
   page name how many grafts he has made today, against his quota.
8. **Evidence.** *Accessibility:* every cue subtitled and every gesture paired with words on the kept page, the roster and Jade; its screens keyboard-only and narrated, fitting 320x180 and GUI 4; its matter and answers read aloud; the graft count on the kept page. *Ownership:* `forasTakesOnlyFromAStoreTheHolderMayChange`, `forasRefusedWhereTheHolderMayNotBuild`, `forasNeverGraftsAStrangersBed`, SG12's `...WaitsForANearHolder`. *Save/reload:* SG12's entity-key case; the accord across a restart. *No duplication:* `forasSpendsOneDonorAGraftAndNeverTwo` (and a refused graft spends nothing); `twoForasNeverGraftOneBedTwice`; SG12's `onePresencePerPact`.

### Dossier 5: Bifrons (entry 46), root-running

1. **Identity.**
   - *Source phrases:* Bifrons "appeareth in the Form of a Monster" [S], and at the Exorcist's command a man's shape [s];
     he makes one knowing in "Astrology, Geometry" [S]; "He teacheth the Virtues of Precious Stones and Woods." [S]; an
     Earl [s]. **Jugcraft adaptation: a root's measure ("Geometry") carries matter between beds, and "Woods" gives him
     roots; the entry's corpse-moving line, the nearest source for moving things to another place, is left out.** The
     survey grounded root-running on a softened reading of that line, "if the owner accepts" it, and offered a wood or
     orchard service built on "Woods" as the alternative (`survey-sources.md`, Bifrons hook); checkpoint question 1 asks
     which the owner prefers. The two-stage appearance gives his shape. Left out entirely (GC-S14): "He changeth Dead
     Bodies, and putteth them in another place; also he lighteth seeming Candles upon the Graves of the Dead." [S]. The Latin meaning "two-faced" and any Janus link are
     not used.
   - *Silhouette:* a low mass of roots studded with stones, about 0.6 block high and 1.2 long, that rises into a lean
     man's shape about 1.8 blocks tall while spoken to. Built geometry. Not imported: the Megami Tensei Bifrons; the
     *Dictionnaire Infernal* plate.
   - *Personality:* shy in his first shape, exact once risen; measures before he acts; dry about his own looks.
   - *Beats (GC-P100):* greeting "Pardon the shape. I am Bifrons; I take a better one when spoken to."; service "Where
     one bed is full and another hungry, I run a root between them. Half is lost along it: that is a root's measure.";
     price "Two logs a week. A root must remember wood."
2. **Invitation.** The Garden's Regard understood; two awake beds the inviter keeps within 8 blocks.
3. **Service: root-running** (`root_running`). Each think while serving, one unit (a task): a source bed (beneficiary,
   awake, at least 24 nutrients; under the default, fallow: nothing growing on it and no Heart on it) and a destination
   (the poorest awake beneficiary bed with room, `Garden.poorest`, at least 3 poorer than the source); the present holder
   may change both. Checks the destination has room first; then `source.take(2)` (exact) and `destination.give(1)` in one
   server step. Never composts, never eases strain, never carries (S1). New economy row
   `("root_running", "nutrient", 2, "nutrient", 1)`; "gaining cycles: none" must still hold.
4. **Upkeep (GC-P101).** `#jugcraft:bifrons_offerings` (`#minecraft:logs`) ×2 every 7 game days; compact: 4 oak logs, 1
   emerald. Terms: radius 8, hours 13,000 to 23,000 (night), 24 units given a night, `party`.
5. **Conflict: which beds are reserves** (choice). When no fallow bed can give but a planted full bed could, he asks:
   "Your only full beds are growing. Shall I draw from them?" Answers: **fallow beds only** (`fallow`, the default) or
   **any full bed** (`full`; never the bed under a Heart), kept as `accord`.
6. **Refusal and departure.** Refusal: "A root runs between two places. Show me two." Departure: he sinks back into roots
   and burrows away.
7. **Details.** (a) He rises into a man's shape only while someone speaks to him, and sinks when the screen closes (his
   `listen` pose is the stand). (b) Before each run he measures: he attends the source and a ring of root-marks rises
   there, then a beat later a second ring rises at the destination (bounded server particles at each, his inspect
   gesture; no travelling particle and no bone pointing, so no client hook).
8. **Evidence.** *Accessibility:* every cue subtitled and every gesture paired with words on the kept page, the roster and Jade; its screens keyboard-only and narrated, fitting 320x180 and GUI 4; its matter and answers read aloud; each run named in words (from, to, given, lost). *Ownership:* `aRootRunnerNeverTouchesAStrangersBed`, SG12's `...WaitsForANearHolder`. *Save/reload:* SG12's entity-key case; the accord across a restart. *No duplication:* `aRootRunnerLosesHalfAndMintsNone`, `rootRunningLeavesNoGainingCycle`, SG12's `onePresencePerPact`.

### Dossier 6: Amdusias (entry 67), the bending canopy

1. **Identity.**
   - *Source phrases:* "The Sixty-seventh Spirit is Amdusias, or Amdukias. He is a Duke Great and Strong, appearing at
     first like a Unicorn" [S]; at request a human shape [s]; "Also he can cause Trees to bend and incline according to
     the Exorcist's Will." [S]; "He giveth Excellent Familiars." [S]. The music line is unverified and its readings
     disagree ([U], [s]): **not quoted, and not attributed to the source in game, until checked against a scan.** Other
     spelling: Amdukias. **The identity rests on "cause Trees to bend and incline"** (the only tree-shaping power among
     the 72 by the surveyor's recollection [U], to be confirmed against a scan; Jugcraft adaptation: a living
     windbreak).
   - *Silhouette:* a tall, slender unicorn about 2.0 blocks, long neck, a horn that is a living branch with a few leaves,
     a garland of small blooms at the neck (the owner's flower-crown ring, a non-creature part:
     `Edited-2026-10-18-Part2/cnb/geo/armor/flower_crown.geo.json` with `cnb/textures/armor/flower_crown.png`; art plan),
     long fine legs. Built
     geometry, drawn fresh: no vanilla horse model copied. Not imported: thunder; any game design ("Amdusias' Screamer").
   - *Personality:* courtly, remote; answers as if listening to something far off. *Jugcraft adaptation* drawn from the
     unverified music line, to be revised if the scan disagrees.
   - *Beats (GC-P120):* greeting "You hear me before you see me. I am Amdusias." (*Jugcraft adaptation*, from the same
     unverified line; it quotes nothing); service "Give me a tree beside this
     perch, and I will bend its shelter over your beds, so your workshop's stir reaches them softer."; price "A sapling
     each week, so there is always a tree to bend."
2. **Invitation.** The Garden's Regard understood; a log within 2 blocks of the perch (his tree); an awake bed the inviter
   keeps within 6 blocks.
3. **Service: the bending canopy** (`canopy_shelter`). While his pact serves, his presence is at its perch, the holder is
   near (common terms) and his tree stands (a log within 2 blocks, looked at every 200 ticks: 125 reads), the habitat of a
   **crop** (the block above a beneficiary bed in his area is an `OrganismCropBlock`) whose niche needs no disturbance
   (its disturbance minimum is 0; any crop once answered `all`) reads the area's disturbance up to 2 lower, never below
   0. Never for a Verdant Heart (a producer; the block above the bed decides, since a Heart's habitat is its bed's
   `habitat`), never raising it, and never easing harvest strain: the strained judgement keeps the undamped value
   (`Verdict.of` judges disturbance as the worse of the area alone and the area plus strain, `E/Verdict.java:44-52`), so
   a canopy never does Buer's work. Where two holders' canopies cover one bed, the largest damping applies, never the sum.
   The rule is pure, in `ecology` (`Habitat.sheltered(int damping)`); it is applied where a bed builds its habitat
   (`VerdantBedBlockEntity.habitat`, and the cached read) from a per-level registry of active canopies (`G/Canopies`,
   memory only, at most one a holder, refreshed each think and lapsing 40 ticks after the last), so growth, the Gauge,
   the Inspect page and Jade all read the same sheltered value, with a line saying so. The quota caps the beds covered at
   once (the nearest 16), worded on the terms page as a cap ("At most 16 beds sheltered at once"), never as a daily
   count. It changes a step's chance, never its cost or output (as the mixed bed). It is not the plan's season shelter
   structure (GC-S21).
4. **Upkeep (GC-P121).** `#jugcraft:amdusias_offerings` (`#minecraft:saplings`) ×1 every 7 game days; compact: 1 note
   block, 2 oak saplings. Terms: radius 6, any hour, 16 beds covered, `party`.
5. **Conflict: the stir a Gloamcap wants** (choice). A crop whose niche needs disturbance (Gloamcap's minimum is 1, ideal
   3 to 10) is pushed down by his canopy. He spares such crops until answered, and asks: "This one wants the stir I
   take away. Shall I spare it?" Answers: **spare them** (`spare`, the default) or **shelter all** (`all`), kept as
   `accord`.
6. **Refusal and departure.** Refusal: "I bend trees. There is no tree here." Departure: he bows his horn, the tree beside
   the perch sways once, and he is gone; a beat later, a distant horn (subtitled).
7. **Details.** (a) A horn heard a beat late: his cue (`pocket_horn_fade`, mono) sounds after his gesture, never with it,
   subtitled "A distant horn" (a *Jugcraft adaptation* of the unverified music line, never attributed to the source
   until a scan checks it). (b) His tree sways: every 10 s while sheltering, a few
   leaf particles fall from the tree beside his perch.
8. **Evidence.** *Accessibility:* every cue subtitled and every gesture paired with words on the kept page, the roster and Jade; its screens keyboard-only and narrated, fitting 320x180 and GUI 4; its matter and answers read aloud; "Sheltered by Amdusias" on the Inspect page, the Gauge's reading and Jade; the late horn subtitled. *Ownership:* `aStrangersCropIsNeverSheltered`, SG12's `...WaitsForANearHolder`. *Save/reload:* the canopy is never saved and returns within one think after a restart (`aCanopyReturnsAfterARestartWithinOneThink`); the accord across a restart. *No duplication:* it makes and moves nothing (`theEconomyStillHasNoGainingCycle`); `twoCanopiesNeverStack`; `aCanopyNeverEasesStrain`; SG12's `onePresencePerPact`.

## Distinctness: six services that differ in mechanism (03:177)

| Service | Verb | Trigger | Input producer | Output consumer | State it changes | Authority it goes through | Cost | One task | Failure behaviour | Tier |
|---|---|---|---|---|---|---|---|---|---|---|
| Buer, strain tending | ease | autonomous, one bed a think | harvest strain from every harvest path (`settle`) | the disturbance fit and the crop's pace (Sunpetal at strain 3 tolerates, half pace) | a bed's `strain` | the bed's own tend rule, its 600-tick cooldown shared with the holder | upkeep only | one bed eased | holder away: waits; claimed bed or stranger's: skipped; tended lately: skipped; overworked: refused until composted | The Garden's Regard (Practitioner) |
| Bathin, Verdance carrying | move an essence at once | autonomous, one move a think | Heart Verdance (up to 64) | a far Gleaner's harvests (1 a harvest) or a pylon's Ley for rites | two reservoirs | `Transfers.move` with both ownerships; the existing 3-for-2 conversion into pylons | upkeep; the existing conversion loss into pylons | one move | Heart empty or target full: idle; ownership refused: skipped; unloaded: skipped; a Heart with its own pylon: left until answered | Regard; a pylon needs Circle Lore (optional) |
| Marax, one-correction reading | read and advise | on request, in person | cached samples, niches, grafts | the holder's own remedies | nothing | `Verdict`/`Niche` over cached samples; no sample budget | upkeep only | one reading | no planted bed: "nothing grows"; all thriving: "nothing to correct"; no cached sample: bed skipped | Regard |
| Foras, graft keeping | transform, spending a donor | autonomous, one graft a think | donors (Dewmoss) in a store beside the perch | the host's grafted niche on that bed | a bed's `graft` | the graft rule (one a bed, Authority at crop and bed) and a Fabric transaction | one donor a graft | one graft | no donor: waits; a known graft held: skipped; refused: nothing spent; useless graft: skipped until answered | Regard |
| Bifrons, root-running | move with loss | autonomous, one unit a think, by night | surplus nutrients (Maw, fixers, bone meal) | hungry beds, a Heart's bed included | two beds' `nutrients` | `take` (exact) then `give` (never makes) | half of what moves; upkeep | one unit given | no source or no room: idle; claimed: skipped; only planted sources: asks | Regard |
| Amdusias, the bending canopy | damp a reading | passive presence | the workshop's disturbance (pylon 2, crucible 2, anchor 3, ...) | the area term of a crop's disturbance fit (crops needing no stir; the strained judgement keeps the undamped value) | no stored state: the habitat read while he serves | the habitat build's canopy registry and the pure `Habitat.sheltered` rule | upkeep; his tree | one bed covered (at most 16 at once) | suspended, unseated, tree gone, presence away or holder far: lapses within 40 ticks; never below 0; never a Heart; a Gloamcap spared until answered; the largest of two canopies, never the sum | Regard |
| *Stolas, Star-reckoning (built)* | recall | on request, in person | observed patterns, astrolabe resonance | a recalled attunement for 2,400 ticks | the shared `recall:<pattern>` claim | `AstralClaims` | the recall's resonance | one recall | refusals in order, nothing spent | Stolas's Regard |
| *Workshop hand (built)* | carry items | autonomous worker | a post's requests | exact deliveries | the courier ledger | `CourierLedger` claims | none | one load | stranded cargo, recovery once | Stolas's Regard mastered |

No two change the same state the same way: strain (Buer), reservoirs (Bathin), nothing (Marax), graft (Foras), bed
nutrients with loss (Bifrons), a derived reading (Amdusias). Buer and Amdusias share a consumer (the disturbance fit) but
not a state or an authority: Buer lowers a bed's stored strain through the bed's own tend rule; Amdusias damps only the
area's disturbance in a derived reading, and the strained judgement keeps the undamped value, so a canopy never does
Buer's work (`aCanopyNeverEasesStrain`: strain 3 near a crucible gives the same strained fit with and without it). Two inform (Marax and Stolas) by different mechanisms
(advice from cached ecology against a recall through a claim). Two move (Bathin and Bifrons) different things through
different authorities (an essence between reservoirs at no loss, through `Transfers`; bed nutrients at half loss, through
`take`/`give`). None moves items between containers (the hand's), plants, harvests, stocks the Maw, empties the Gleaner,
waters or fertilizes ordinary crops (Peepo's), forces growth (the Spire's and alchemy's), or forecasts (the sky's).

**What each needed that the others did not:** Buer, a read-only tend predicate and his memory of today's eases; Bathin,
a Gleaner's receive, a Heart's bounded pour into a named pylon and (as its first user) the accord machinery; Marax, a
cached-only habitat read, a pure advice function and a synced work target for his familiar; Foras, a container touching
the perch and a transaction; Bifrons, an economy conversion; Amdusias, a canopy registry consulted where habitats are
built and a pure shelter rule. None needed a job adapter (GC-S15).

## Tickets

### GC-01. One perch, many spirits; Stolas unchanged (unit 1)

**What the player sees.** Nothing changes for a player who may invite only Stolas. A player who may invite more than one
spirit, at an empty perch they own, first sees a list: each spirit's sign, its name and "ready", or what is missing
(research, its fixture). Choosing one summons that spirit as the visitor; its beats, terms and kept page name it. The
roster portraits every pact by its own spirit; Jade names each spirit's service. A garden spirit's terms and refusals
never mention an astrolabe.

**First push (GC-S24):** a compile-only slice: SG1 to SG3 with Stolas a thin subclass of `PactSpiritEntity` and no new
content. CI must compile it and pass every Stolas test before the rest of GC-01 is added.

**Changes (the smallest shared generalization, `survey-pacts.md` §5).**
- **SG1 `S/PactService` and `S/PactServices`** (new): an interface per `work` id, registered once at startup; every id in
  `PactParser.WORK` must have one (startup check and audit). Members: `work()`; `holderOnly()`; `missing(level, inviter,
  perch, definition)` (extra invitation notes; Stolas: the sky observation, moved out of `PactMenu.observed`);
  `choices(level, player, record)` (Stolas: observed patterns); `perform(level, player, holder, choice, time, now)`
  (Stolas: `StarReckoning.reckon` and `told`); `rows(level, player, record, now)` (service and obligation rows, including
  a task-resolved complication such as Buer's overworked bed); `work(level, spirit, record, holder, now)` (the autonomous
  step, at most one change, default none: Stolas); `obligation()` (GC-03). `complication(...)` and `accords()` are added
  with their first user (GC-05; default none). A record whose work has no registered service loads, lists, suspends and
  ends; its action says "unavailable". `PactParser.WORK` and `HOLDER_ONLY` stay plain lists (the parser stays free of
  game classes), appended per unit and mirrored by `tools/concordance_pacts.py` (`check_pacts`).
- **SG2 `S/PactSpiritEntity<T>`** (new abstract, extracted from `StolasEntity`): perch and record (saved as `perch`,
  `record`, unchanged), synced `LISTENING` and `DEPARTING`, departure (40 ticks, never saved while leaving, one sweep for
  every type), `remove`, invulnerability but bypass damage, the `decide` skeleton (a visitor leaves without a live
  session; verify the presence's UUID and record against its perch, else it leaves; the pact's state;
  `OWNER_OFFLINE` unless `Authority.presentNear(level, holder, perch, Pacts.REACH)`, as Stolas's today), cue rate limit
  and sound play, the gesture controller, `listen`, `kind() = "pact"`, `joinsRosterOnStatus() = false`, station keeping
  (`stay()`: on the perch, or on the side chosen at summon; teleport only by `teleportTo(x, y, z)`), and a `Body` table
  per subclass (loop and gesture clip names, lengths, telling windows; cue ids `jugcraft:<spirit>.<cue>`; the reaction
  table; the station kind). A service's `work` runs only after `decide` has returned `IDLE` *(amended unit 1, GC-P27: or
  `UPKEEP_DUE` with the holder within `Pacts.REACH` of the perch)*, in either case only for a presence verified at its own
  perch, so a copied or duplicate presence never works. **`StolasEntity` becomes a thin subclass** keeping `COUNTERPART`, its `Gesture` and `Loop` enums
  (tests and the client name them), its entity id and save keys; his behaviour is unchanged.
- **SG3 `S/PactSpirits`** (new): counterpart to entity type, filled in `Workers.register`. `PactPerchBlockEntity`:
  `presence(level)` and `visitor(level)` typed to the base (replacing `stolas()` and `unbound()`), `welcome(level,
  inviter, counterpart)`, `call` creating whatever the counterpart names, `heard`/`react` on the base. The perch block,
  item, block entity, menu and their ids are unchanged; its saved keys are unchanged (GC-05 adds one optional key).
- **SG4 the chooser** (`survey-pacts.md` G4 option (a)): `PactMenu` keeps the offered counterpart per conversation
  (memory only). At an empty perch whose owner may invite more than one definition, a new mode `invite` (appended,
  index 4) lists them (`MAX_CHOICES` 8 holds seven) and a new action `INVITE` (appended, index 9), accepted only in the
  `invite` mode, chooses by index into the list the server sent; with one, the step is skipped (today's flow). Once a
  visitor is chosen, choosing again means declining (the visitor leaves) and reopening. A player who is not the perch's
  owner never sees the chooser: they see the visitor's offer with `missing.owner` (as `twoNegotiationsAtOnePerchSealOnce`
  shows today), or the plain notes when no visitor is present. `revise()` counts the chosen offer. `end`, `seat` and the
  stranded view iterate the player's stranded pacts as choices instead of `PactDefinition.STOLAS`. Stolas's sky
  precondition is his service's `missing` (GB2).
- **SG5 payload and screen:** `PactStatePayload` gains **one nested field**, `spirit`: a record
  `PactStatePayload.Spirit(String sign, String work)` with its own composite codec (strings bounded by `MAX_ID`). The
  payload then has 14 fields, the most one 26.3 `StreamCodec.composite` takes (mc263src `StreamCodec.java:533`; GB22);
  unit 2 appends `matter` and `answers` to the nested record, never a fifteenth top-level field. `sign` is the spirit's
  sign item from the `SPIRITS` table (Stolas: `jugcraft:stolas_starflower`), never derived from a tag offering; `work` is
  the kept pact's work id. `patterns` is read as "service choices" (wire unchanged); network only, no save impact.
  `PactScreen` takes the sign from the state (GB8) and titles its service section `screen.jugcraft.pact.service.<work>.*`;
  it draws the chooser.
- **SG6 keeping page:** `PactKeeping.page` takes its rows and choices from the service.
- **SG7 discovery and journal:** `S/PactRegards` (new) reads the next step of each loaded definition's research
  (`ResearchEngine.explain`, as `StolasRegard.step` does) for the journal's pacts section (GB17); `StolasRegard` stays
  Stolas's, and Styx's hint stays Stolas's.
- **SG8 Jade:** `WorkerDataProvider` reads any `PactSpiritEntity<?>`; `PactReading.presence(level, spirit, now)`; the
  perch's `visiting` reads its generic visitor; lang takes the visitor's name.
- **SG9 roster:** a record row is kind `pact` for any counterpart in `PactSpirits` (GB6); `KIND_PORTRAITS` maps `pact`
  to `""` and a row with a record is portraited by its counterpart.
- **SG10 words:** the 36 Stolas-worded generic keys take the spirit's name as `%s`; **no key is moved or renamed**
  (Stolas's tests assert key names, for example `PACT_KEY + "missing.owner"`). Their values change, so `en_us.json`
  changes; `stolasLinesRenderAsBefore` renders each changed key with Stolas's name and compares it with today's rendered
  line. The generator adds resonance-free keys, `message.jugcraft.concordance.pact.term.compact.items` and
  `message.jugcraft.concordance.pact.answer.cannot_afford.items`, which `PactMenu.offer` and the sealing refusal choose
  when the definition's resonance is 0 (today both name "Astral Resonance ... astrolabe" from `d.resonance()`,
  `S/PactMenu.java:301, 489-490`, though `PactCompact.affords` skips the astrolabe at 0; GB23), and a per-work quota key
  `term.quota.<work>` with the generic `term.quota` ("%s a game day") as the fallback (first used by GC-09).
- **SG11 generators and audits:** `tools/concordance_pacts.py` grows a `SPIRITS` table (entity size, scale, station,
  sign item, loops, gestures and times, cues and sound files, body source and palette files, offerings, research, work,
  beats, terms, compact) from which `PACTS`, `ENTITY_NAMES`, `SOUND_EVENTS`, Jade and roster names and codex pages are
  derived; **with Stolas's entry alone it regenerates today's data, sound, codex and asset files byte for byte**, and the
  lang differs only by SG10's `%s` and the new keys, Stolas's rendered lines unchanged. `check_pacts` and
  `check_pact_screen` loop over it; `check_stolas_body` becomes `check_spirit_bodies` with Stolas's pins as his entry
  (GB16); `check_roster` reads the generic portrait; a new `PactServices` coverage audit; `check_workers` unchanged
  except for GC-02's pin (pact spirits are not worker definitions; no fourth worker kind).
- **SG12 shared tests once:** a package-private helper in `T/` runs the shared pact cases over every loaded pact
  definition: seal once and spend once, one pact a holder per counterpart, a refusal spends nothing, upkeep warns, gives
  its grace, suspends and resumes (D-P50), a stand-in never answers upkeep, a broken perch leaves the pact without its
  perch, seating again, a removed presence never ends the pact, ending returns the escrow once, a copied perch is inert,
  `onePresencePerPact` (a copied or duplicate spirit entity never works: `T/ConcordanceWorkerGameTests.java:2955`
  today for Stolas), `...WaitsForANearHolder` (the holder online in the same level but 100 blocks from the perch: no
  change, no upkeep taken, no canopy), `twoNegotiationsAtOnePerchSealOnce` (the other player `NOT_PERMITTED`), and the
  entity's keys survive a save; for garden definitions also `noGardenTermOrRefusalNamesAnAstrolabe`. Each new spirit
  inherits them by adding its definition.

**Reuses:** `AgreementHolder`, `BoundWills`, `Pacts`, `PactSessions`, `PactCompact`, `PactKeeping`, `PactHistory`,
`Roster`, `WorkerRoster`, `RateGate`, `Authority`, `ResearchEngine`.

**Interfaces changed:** `PactPerchBlockEntity` (public members typed to the base), `PactMenu`, `PactKeeping.page`,
`PactStatePayload` (one nested field `spirit`; network only), `PactRequestPayload.ACTIONS` (+`invite`), modes
(+`invite`), `StolasEntity` (split), `StarReckoning` (behind `PactService`), `PactReading`, `WorkerDataProvider`,
`Roster`, `Journal`, `PactScreen`, `WorkerClient` (a renderer table: `PactSpiritRenderers`), `PactParser.REQUIRED`
(`W/PactParser.java:38`; every shipped definition), `tools/concordance_pacts.py`, `tools/concordance_roster.py`,
`tools/check_mod_data.py`.

**Saved data and ids:** none changed or added (Stolas's `perch`, `record`, the perch's `owner`, `agreement`, `spirit`, the
Bound Will's keys, `jugcraft:pact_history`, the roster's kind `pact` all as built). Payload type ids unchanged.

**Tests (server):** every existing Stolas test passes unchanged except for class references that move;
`aPerchOffersOnlyWhatThePlayerMayInvite`, `theChooserIsSkippedWithOneSpirit`, `anInviteIndexOutOfRangeIsDropped`,
`anInviteOutsideTheInviteModeIsDropped`, `aNonOwnerNeverSeesTheChooser` (the visitor's offer with `missing.owner`, or
the plain notes), `aStaleChooserRevisionIsRefused`, `aStrandedPactOfAnySpiritIsSeatedOrEnded`,
`aRecordWithAnUnknownWorkStillEnds`, `theJournalNamesEachRegardsNextStep`, `theRosterPortraitsEachPactBySpirit`,
`stolasLinesRenderAsBefore`, and SG12's shared cases (run for Stolas in unit 1, then for every spirit). **Client:**
`ConcordancePactClientGameTests` gains the chooser at GUI 4 and in a 320x180 window, keyboard-only. **Audits:**
`check_pacts` (the `SPIRITS` table, `ACTIONS` and modes appended only, the payload's nested field and its 14 top-level
fields), `check_pact_screen`, `check_spirit_bodies` (Stolas's entry), `check_roster`, the service-coverage audit, and
audit mutations (a deliberate fault for each new pin, each reported).

**Acceptance:** Stolas's behaviour, ids, saved keys and every existing test unchanged, and his rendered lines identical;
the generators with Stolas alone reproduce today's data, sounds, codex and assets byte for byte; E1 and E5 (Chunk D's
codes) hold for any definition through SG12.

**As built (GC-01; `62b0095fa` compile-only, then `202e75969`; CI in
[unit 1's evidence](#unit-1-as-built-and-its-evidence)):** SG1 to SG3 first, compile-only, with no lang, data, asset,
client or test change; then, in one commit, the `SPIRITS` table with its zero-diff gate (with Stolas's row alone the
generators changed no generated file, and the probe printed `True 6`, Stolas's adapted model, animations, skin and
glowmask; the generators' incidental rewrites, which predate unit 1, were restored first:
[Corrections](#corrections-to-unit-1s-commit-messages)) and the rest of GC-01. Departures from this ticket, each a
proposal in [Decisions](#decisions): the chooser's tests that need a second spirit (`aPerchOffersOnlyWhatThePlayerMayInvite`,
`anInviteIndexOutOfRangeIsDropped`, `aNonOwnerNeverSeesTheChooser`, `aStaleChooserRevisionIsRefused`, the two-spirit half
of `aStrandedPactOfAnySpiritIsSeatedOrEnded`, `theJournalNamesEachRegardsNextStep`, `theRosterPortraitsEachPactBySpirit`)
and the chooser's pictures landed with Buer in GC-04, in the cohort's own client class, not in
`ConcordancePactClientGameTests` (GC-P18); a standing body's side cell waits for its first standing body (GC-P5, GC-P11);
three existing test lines changed (GC-P17); ten shared lines take the spirit's name, not 36 (GC-P12, R9); `PactService`'s
members are `serviceRows`, `obligationRows`, `suspendedRow` and `work(..., time, now)`, not this ticket's `rows` and
`work(level, spirit, record, holder, now)` (GC-P14). The chooser lists only spirits whose research the player understands
(`PactMenu.listed`), each "ready" or with what its own service says is missing (Buer: an awake bed of the inviter's within
8); a spirit whose research is not understood is not listed at all, so "what is missing (research, its fixture)" above holds
for the fixture only. The unit review added `aVisitorOfAnotherSpiritLeavesWhenTheChosenCannotCome` (choosing a spirit that
cannot come sends another spirit's waiting visitor away). Interfaces as listed under Changes, plus `PactServices`,
`PactRegards` and `PactSpiritRenderers` (new). Nothing saved was added or renamed. GB1 to GB4, GB6 to GB9, GB16
(Stolas's half), GB17, GB22 and GB23 (its resonance half) closed.

### GC-02. A spirit faces whoever speaks to it (unit 1; GC-S23)

**What the player sees.** Stolas, and every garden spirit, arrives facing whoever invited it; while a conversation is open
it turns its head, then its body, to whoever spoke last; when the screen closes its head eases back and the body keeps
its facing. A garden spirit at work turns to the bed or reservoir it is working on, then back to its speaker if one is
talking to it.

**Changes (server only; `survey-pacts.md` §6):**
- Summon facing the inviter: `PactMenu.begin` already has the player; the bearing from perch to inviter sets `yRot`,
  `yHeadRot` and `yBodyRot` before `addFreshEntity` (`snapTo` sets only `yRot`).
- `WorkerEntity.attend(@Nullable Entity)` and `attend(BlockPos)` (the only class allowed to touch the brain,
  `check_workers`) set or erase SmartBrainLib's `LOOK_TARGET` memory (`EntityTracker`, `BlockPosTracker`);
  `LookAtTarget`, already always running, then turns the head at most 10° a tick (vanilla `getHeadRotSpeed`), and each
  client's `BodyRotationControl` turns the body; GeckoLib draws the body yaw with no client change.
- **`LookAtTarget`'s target test (GB21):** the brain registers a plain `new LookAtTarget<T>()`
  (`S/WorkerEntity.java:193`), whose default `invalidTarget` is `!pos.isVisibleBy(entity)`, checked in
  `checkExtraStartConditions` (SmartBrainLib `LookAtTarget.java:31, 171`). Vanilla `EntityTracker.isVisibleBy` needs the
  target in `NEAREST_VISIBLE_LIVING_ENTITIES` (mc263src `EntityTracker.java:51`), which the worker's only sensor,
  `NearbyPlayersSensor`, never fills; an `EntityTracker(speaker)` would be refused at once and every spirit would stay at
  yaw 0 but for the summon bearing. `WorkerEntity.getAlwaysRunningBehaviours` therefore registers
  `new LookAtTarget<T>().invalidateTargetIf((e, t) -> t instanceof EntityTracker tracked && (!tracked.getEntity().isAlive()
  || tracked.getEntity().level() != e.level() || e.distanceToSqr(tracked.getEntity()) > 32 * 32))`. A `BlockPosTracker`
  (bed or reservoir targets) is never invalid, as today. No living-entities sensor is added (that would be new per-tick
  scanning). Nothing sets `LOOK_TARGET` today, so other workers do not change. Before the change, the default predicate
  is confirmed against the pinned `smartbrainlib-fabric-26.3-2.0.3` sources (the reference copy's version is unrecorded);
  the predicate is pinned in `check_workers`.
- `PactSessions.speaker(server, perch, now)` (read only): the player of the most recently touched live session at that
  perch. The base's think (every 20 ticks) and `listen()` attend the speaker and set `yRot` to the bearing, so a
  teleport back to the station keeps the facing; with no live session the memory is erased. While its work gesture
  plays, a garden spirit attends its target, then the speaker again if one is live.
- No head bone is turned on the client (GeckoLib 5.5.7's helper would overwrite Stolas's keyed head); an additive head
  turn ahead of the body is deferred.
- Chunk D's evidence camera (D-P133, south of Stolas) moves to the speaker's side; D-P133 is superseded by GC-S6.

**Tests (server):** `aSpiritFacesWhoeverSpeaksToIt` (`LOOK_TARGET` still present 5 ticks after `attend`; head within 10°
of the bearing 40 ticks after opening, body within 15° 60 ticks after), `theLatestSpeakerIsFaced` (two players at one perch), `aTeleportBackKeepsTheFacing`,
`aClosedConversationLeavesTheFacing`, `aSpiritFacesTheBedItWorks` (Buer, GC-04). **Client:** Stolas photographed from
the speaker's side at four bearings (`jugcraft_pact_stolas_faces_{north,east,south,west}`), with `[pact-check] facing`
lines logging the drawn body yaw against the bearing. **Two clients (unit 7):** the peer sees the spirit turn to the
holder, then to the peer when the peer speaks. **Audits:** `check_workers` still finds brain memories only in
`WorkerEntity` and pins the `LookAtTarget` predicate; `check_pact_screen` holds the new shot names.

**Acceptance:** GC-S23's groundwork, Stolas included; no client class changed for facing; no sensor added and no
per-tick work beyond the existing think and the brain's existing behaviour.

**As built (GC-02, `adc822110`):** server only, as specified: `WorkerEntity` `attend`, `attending`, `unseen` and
`LOOK_REACH` (32); `PactSessions.speaker` with an order counter; the base's `face`, `bearing`, `turnTo` and `attendWork`;
the perch summons a spirit facing whoever it comes for. SmartBrainLib's default `LookAtTarget` test was confirmed against
2.0.3's source tree (its "Update to 2.0.3" commit, `deb0150`), not the pinned jar; the tests that rely on the replacement
test pass in CI. With A-03 a spirit faces whoever speaks unless it is switched off: a spent day's quota (FINISHED) no longer
stops it. Tests: this ticket's four, `aSpiritArrivesFacingItsInviter`, `theLookTargetIsDroppedOnlyWhenGoneElsewhereOrFar`,
and `aWorkTargetOutranksTheSpeakerWhileItsGesturePlays`, which is beyond the specification (a work target outranks the
speaker while its gesture plays, then the speaker again). This ticket's `aSpiritFacesTheBedItWorks` is GC-04's
`buerFacesTheBedHeTends` (R8: one test, Buer's name; since the unit review it checks the bed, then the speaker, then no
one). `ConcordancePactClientGameTests` checks the arrival facing, photographs Stolas from four sides
(`jugcraft_pact_stolas_faces_*`, looked at for this record: [Stolas's Verification](stolas-pact.md#verification)), re-sends
him as to a late watcher, and photographs his body, poses and Jade from the north (D-P133 superseded); its client weight
is 260. No sensor, packet, payload field, saved key, id or lang line was added. GB10 and GB21 closed. GC-P8 and GC-P9 kept,
GC-P8's wording amended (Decisions).

### GC-03. The garden service groundwork (unit 1)

Only what Buer uses, plus `cachedHabitat`, which units 3, 4 and 6 (built in parallel) all read and which its own test
exercises. The accord machinery and the bed index's devices come with Bathin (GC-05), the containers with Foras (GC-07).

**What the player sees.** The Garden's Regard in the codex and journal; its specimens say what they teach when examined
and in their tooltip; nothing else until Buer (GC-04).

**Changes:**
- **Bed index** (`S/GardenReach`, new): for a presence, the awake Verdant Beds inside its area, read from the
  block-entity maps of the chunks the area overlaps (the shape of `CourierWork.reserve`'s chunk scan,
  `S/CourierWork.java:248-266` *(survey)*), loaded chunks only, capped at 64 beds, nearest first, cached 200 ticks; never
  `Garden.nearest` (GB12). Per-presence memory, not saved. Unit 2 adds Hearts, Gleaners and Ley Pylons (16 of each), unit
  4 plain containers touching the perch (16).
- **Beneficiary rule** (`S/GardenPacts.target(level, record, holder, pos, keeper)`, new; called only after the base's
  `decide` returned `IDLE`, or `UPKEEP_DUE` with the holder near: GC-P27): the record serves at that place now (`Pacts.serves(record, Place(pos), time, now)`: hold,
  area, hours, quota), the keeper is a beneficiary at this moment, the holder is present near the perch
  (`Authority.presentNear(level, holder, perch, Pacts.REACH)`, as upkeep looks), and that player may change the block
  (`Authority.mayChange`). Returns a `Status` reason or null. No stand-in, ever.
- **Bed reads** (`G/VerdantBedBlockEntity`): `mayTendAt(long now)` (read only: awake, strained, outside the 600-tick
  window) and `cachedHabitat(ServerLevel)` (the habitat from the cached area only; null without one; never samples).
- **Obligations** (`S/PactObligations.performed(level, player, Act, pos)`, new): `Act` holds `TEND` in unit 1, with its
  one line in `VerdantBedBlock.tendBy` on success. Each later unit appends its spirit's act with its one-line hook:
  `DEVICE` (unit 2: an awake living device used with an empty hand, one line after `useEmpty` in
  `LivingDeviceBlock.useWithoutItem`), `READ` (unit 3: the empty-hand bed report), `GRAFT` (unit 4:
  `OrganismCropBlock.graftBed` on success), `FEED` (unit 5: bone meal on a bed or crop on success), `HARVEST` (unit 6: the
  hand harvest beside its cultivation record). It looks through the player's Bound Wills (at most 32) for a pact that
  serves now, whose service's `obligation()` is that act and whose area holds `pos`, and records practice
  `jugcraft:garden_pact_service` keyed by the counterpart, once a player, never for a pact without its perch. Stolas's
  first obligation stays in the observatory (`StarReckoning.observed`), recording `pact_service` as built (GB5 avoided).
- **The Garden's Regard** (`jugcraft:garden_regard`; GC-P6): Spiritbinders, Practitioner, principle tether, icon the
  Verdant Bed; requires the Binding Arts and Verdant Husbandry understood; encountered: examine
  `#jugcraft:garden_regard_specimens` (garden produce and Verdant Chaff, existing items; GC-P6); observed: practice
  `jugcraft:cultivation`; understood: practice `jugcraft:worker_service` (a bound worker kept at work); mastered: practice
  `jugcraft:garden_pact_service` with `distinct: 3` (reachable from unit 3, the third spirit). Its practice gate
  `D/concordance/practice/garden_pact_service.json` requires it understood. Examining a specimen says what it teaches
  (`message.jugcraft.concordance.examine.garden_regard`, a branch in `concordance/Examination.java` beside Stolas's at
  `:97-99`) and its tooltip says it is one (`tooltip.jugcraft.concordance.garden_regard_specimen`, in
  `C/ConcordanceClient.java` beside `:133-134`). Four advancements, Modonomicon facts, hooks and nodes, one codex entry
  `binding/garden_regard` (a page per spirit, added by each unit), all generated (`tools/concordance_garden_pacts.py`,
  new, merged by `tools/concordance.py`); `tools/concordance_progression.py` `PRACTICES["garden_pact_service"]` (gated
  on the Regard, a perch and a bed). The tool honours `distinct` only for `examine` today
  (`tools/concordance_progression.py:527`; GB26), so for this practice only it counts the shipped pact definitions whose
  service records a garden obligation and reports mastery unreachable while fewer than 3 ship; other practices are
  unchanged (Verdant Husbandry's `cultivation` rule keeps today's reading). Mastery unlocks nothing new in this cohort
  (Deferrals).
- **Audits:** `check_garden_pacts` (new: every garden work's constants against the generator, the obligation hooks in
  place for the acts shipped, `GardenReach`'s caps, no `Garden.nearest` and no `Garden.sample` in `spirits/`, no
  `answering(` in garden services, `presentNear` (not `present`) in `GardenPacts`).

**Interfaces changed:** `VerdantBedBlockEntity` (+2 reads), `VerdantBedBlock` (one hook), `Examination` and
`ConcordanceClient` (the specimens' message and tooltip), new `GardenReach`, `GardenPacts`, `PactObligations`.

**Saved data and ids:** research `jugcraft:garden_regard`; practice `jugcraft:garden_pact_service`; tag
`#jugcraft:garden_regard_specimens`; advancements `concordance_garden_regard_{encountered,observed,understood,mastered}`;
codex entry `binding/garden_regard`; lang for the examine line and the tooltip. No saved key.

**Tests (server):** `theGardenReachNeverLoadsAChunk`, `theGardenReachIsCappedAndCached`, `aCachedHabitatNeverSamples`
(the level's `SampleBudget.used` unchanged), `anObligationIsRecordedOncePerPlayerAndSpirit`,
`anObligationNeverMastersStolassRegard` (GB5), `anObligationOutsideTheAreaOrWhileSuspendedCountsNothing`,
`theGardensRegardSpecimensSayWhatTheyTeach`, `theGardensRegardIsReachable` (progression: up to understood in units 1
and 2, mastered from unit 3), `talkingTeachesNothing` extended to garden spirits. **Audits:** as above;
`check_concordance`'s unique codex places; `check_economy` unchanged.

**Acceptance:** S1 (nothing in transit), S3 (beneficiaries checked at each use), S5 and S6 hold; no spirit takes a sample;
the hook changes no existing behaviour of the gesture (its existing tests unchanged).

**As built (GC-03, `80c311c93`, with two of its tests' setup fixed in `6101ad482`):** `S/GardenReach` indexes the awake
beds of the keepers a pact benefits in its area from the loaded chunks' block-entity maps (at most 9 chunks), keeps the 64
nearest the perch, re-reads every 200 ticks or when the clock runs back, and resolves each place in loaded chunks at each
read; it never builds a box of positions, loads a chunk or samples. `S/GardenPacts.target` asks, in order, whether the
pact serves there now, whether the keeper benefits now, whether the holder is within 64 blocks of the perch in its level,
and whether the holder could change the block; it never asks a stand-in (its Javadoc says since the unit review that
`think` calls services under `IDLE`, or under `UPKEEP_DUE` with the holder near: GC-P27). `VerdantBedBlockEntity.mayTendAt`
reads the bed's own tending rule (the window it shares with every tender) and changes nothing; `cachedHabitat` builds a
habitat from the last sample and never takes one, and `habitat` now reads through it. `S/PactObligations` records
`jugcraft:garden_pact_service` keyed by the spirit when the holder tends a bed by hand inside an active garden pact's
area, once a player a spirit; `PactService.obligation()` names the act. The Garden's Regard is begun after the Binding
Arts and Verdant Husbandry are understood: examine a garden specimen (Sunpetal, Dewmoss, Gloamcap, Mendvetch or Verdant
Chaff), harvest an organism crop by hand, keep a bound worker at work; it is mastered by three garden spirits' first
obligations (reachable from unit 3). The progression tool honours the garden practice's `distinct` (GB26) and expects
mastery out of reach until three spirits ship; the journey's `GARDEN` route reaches understood; `GARDEN_PACTS` names its
three things. Tests: this ticket's, with `talkingTeachesNothing` a new test over every offered spirit (R10: no such test
existed to extend; Styx's `talkingGrantsNoResearch` is unchanged), and two beyond the specification for the validation
plan's named suites: `ConcordanceSharingGameTests.notesNeverStandInForTheGardensRegard` and
`ConcordancePersistenceGameTests.theGardenGroundworkSavesNothing`. The guide's research table was re-pasted with this
commit (A-09). GB5, GB11, GB12 and GB26 closed.

CLAUDE.md fields for The Garden's Regard: **Tier** Practitioner (Spiritbinders). **Input producer** the player's own
hands and workers: garden specimens (the garden's produce and chaff), a hand harvest, a bound worker's service; for
mastery, garden pacts' obligations. **Output consumer** the garden pacts' invitations (each needs the Regard understood)
and nothing else. **Costs** none of its own (a Sunpetal: a sunflower, an amethyst shard, bone meal; a Bonding Charm).
**Unlocks** invitations to garden spirits; mastery unlocks nothing in this cohort. **Failure behaviour** nothing before
both prerequisites; notes never stand in; a repeat records nothing; mastery unreachable while fewer than three spirits ship
(the tool says so). **Test evidence** GC03_TESTS's twelve, passed in CI run 40605564166
([unit 1's evidence](#unit-1-as-built-and-its-evidence)).

### GC-04. Buer, end to end (unit 1)

**What the player sees.** Dossier 1, whole: the Garden's Regard page for Buer; the chooser lists him once a bed of theirs
is within 8 of the perch; he arrives rooted on the perch, facing them; three beats; the terms; accept spends 4 Mendvetch
and 4 bone meal in the sealing step; by day he eases strained beds one a second at most, each by its own rule; the
overworked bed; his offering each week; his night sleep; ending sends him back into the perch.

**Changes:**
- `S/BuerEntity` (thin subclass: body table, station rooted on the perch, reaction table, cue ids) registered as
  `jugcraft:buer` (MISC, about 1.2 x 1.2, fire immune; drawn at the scale that gives about 1.7 blocks across).
- `S/StrainTending` (the `PactService` for `strain_tending`): its `work` step, its rows ("eased today n of 16", "asleep
  until dawn", the overworked bed: a task-resolved complication, a service row with no accord), its `missing` (an awake
  bed of yours within 8) and `obligation() = TEND`. Appended to `PactParser.WORK` (not `HOLDER_ONLY`).
- Data (generated): `D/concordance/pact/buer.json`; `#jugcraft:buer_offerings`; sound events `jugcraft:buer.{arrive,
  listen,inspect,assent,refuse,depart,work}` bound to the mono files the art plan names; lang (entity name, beats,
  terms, rows, refusal, departure, the overworked row, subtitles, codex page).
- Body (art plan): the Decor Blossom's texture copied byte for byte (`tools/owner_art.py` `OWNER_FILES`); its geometry
  adapted through `tools/garden_bodies.py` (new): the `base` stem cube repeated upward, and one face of each
  zero-thickness twin dropped (the `down` face of the 10x10 leaf plate `[10,0,10]` and of the 4x4 pistil cap `[4,0,4]`, the
  `south` face of each 2x6 pistil blade `[2,6,0]`). GeckoLib draws entities with `entityCutout` (GeckoLib
  `GeoRenderer.java:73-75`), which does not cull in 26.3, so each pair would draw two faces on one plane, the "exact
  reversed twin" `docs/ART_DIRECTION.md:31` forbids; one quad is seen from both sides. Each is a recorded technical
  change. The owner's `open`, `close`, `active` clips are kept and his loops and gestures added. `tools/owner_art.py`
  learns the cohort's `OWNER_FILES` and `ADAPTED` (from `tools/concordance_pacts.py`'s `SPIRITS`) with adapters from
  `tools/garden_bodies.py`, beside Stolas's (today `adaptations` loops only over Stolas's table with `stolas_body`,
  `tools/owner_art.py:130-140`; GB25). `check_geckolib` gains a per-face UV reader (GB15); `check_spirit_bodies` gains his
  entry and a rule that no zero-thickness cube draws both opposite faces (GB24).
- Client: his renderer row (scale, culling box, glow layer for the pistil).
- Records: new `docs/features/buer-pact.md` on SP's shape (CLAUDE.md fields, connections, costs, failure, multiplayer and
  saved keys, the screen, the character table, provenance with SHA-256, verification, rollout and stable ids); GB18's
  citation corrected in `styxhexenhammer.md` after Gutenberg's record is read.

**Tests (server):** `buerEasesOneStrainAtTheBedsOwnRate` (strain 3 to 2; nothing within 600 ticks; a player tending in
that window is told "tended lately"; nutrients and items unchanged), `buerTendsOnlyBedsOfHisBeneficiaries` (a stranger's
bed in the area untouched; a party member's tended under `party`, not after they leave the party),
`buerNeverTouchesAClaimedOrDormantBed`, `buerWaitsForAPresentHolder` (offline, other level, dead, or 100 blocks away in
the same level: `owner_offline`; no stand-in with the absent-owner option on), `twoBuersShareOneBedsTendWindow`, `buerSleepsOutsideHisHours`, `buerStopsAtHisQuota`,
`anOverworkedBedWaitsForCompost` (eased, harvested back to 3: refused; bone meal by hand: tended again),
`buerChangesAtMostOneBedAThink`, `buerRefusesWithoutABedToMend` (nothing spent), `buersFirstObligationIsATendingByHand`,
`buerFacesTheBedHeTends`, `aSuspendedBuerTendsNothing`, SG12's shared cases for `jugcraft:buer` (including
`onePresencePerPact`, `...WaitsForANearHolder` and `noGardenTermOrRefusalNamesAnAstrolabe`), and the whole path
`ConcordanceJourneyGameTests.buerFromTheCodexToDeparture` (one survival player: research, beds, perch, chooser, terms,
accept, a tend, an overworked bed composted, an offering, suspend, resume, end, escrow once). **Client:**
`ConcordanceGardenPactClientGameTests` (new; GC-P30): the chooser, his beats, terms and kept page at GUI 4 and 320x180,
keyboard-only; his poses frozen inside each telling window from his front (`idle`, `listen`, `inspect`, `assent`,
`refuse`, `depart`, `work`) and asleep; his plate and pistil cap seen from the front and from below (no surface
fighting); facing from four bearings; his Jade line; the overworked row; his terms page (no astrolabe named). Logged
`[garden-check]` lines with frozen times. **Audits:** `check_pacts` and `check_garden_pacts` (his entry),
`check_spirit_bodies` (his entry, the twin-face rule), `check_geckolib` (per-face), `owner_art --check` (the cohort's
table), `check_acquisition` (his compact and offering
reachable), `concordance_journey --check` (his route), `select_client_tests` `WEIGHTS`.

**Acceptance (dossier items 1 to 8):** every item of Dossier 1 shown by a named test, shot or record line; GE1 to GE8
for Buer; the first complete spirit before any second (09:16).

**As built (GC-04; brief `9742f240b`, Buer `3e736655a`, the unit review's fixes `4b9a038bb`, three test fixes
`4ca363d38`; [record](buer-pact.md)):** `S/BuerEntity` (rooted on the perch; entity box 1.2 x 1.6, eyes 1.4, drawn at 2.2;
GC-P25) and `S/StrainTending` (the `PactService` for `strain_tending`), his definition, offerings tag, seven sounds,
words, codex pages, roster and journey rows from the generators, and his body adapted from the owner's Decor Blossom by
`tools/garden_bodies.py` through `tools/owner_art.py`, the texture byte for byte. Departures from this ticket and the parts
(each recorded, none hidden):

- **His body** (GC-P24, GC-P25): the clear face of each two-faced plane is dropped, and for the blades that is `north`,
  not `south` as this ticket says (the owner's sheet draws `south`; dropping it would erase the stalk). His box is 1.2 x 1.6,
  not about 1.2 x 1.2 (the drawn flower is 1.6 tall; R21). Stolas's six two-faced box-UV cubes are recorded
  (`STOLAS_TWINS`), not changed (R23).
- **Through the grace** (GC-P27, R3): a pact's service steps under `UPKEEP_DUE` too, the status staying `UPKEEP_DUE`;
  since the unit review (finding 11) only while the holder is near, so no service runs for an absent holder. This departs
  from GC-S7's literal "after decide returned IDLE"; the common terms, SG2 and GC-03 above are amended to match.
- **At night with an offering due** (GC-P25, GC-P27; the unit review's finding 17): his loop maps `UPKEEP_DUE` to astir
  (half open) whatever the hour, while the kept page and Jade say "asleep until dawn" at night. Recorded, not changed; a
  synced "sleeping" flag would remove it.
- **Listening** (A-06): his loop shows listening over everything but switched off; a spent quota (FINISHED) shows him shut
  only while nobody speaks (A-03 for facing, A-07 in `buerStopsAtHisQuota` and `buerAnswersEachActionWithHisGesture`).
- **The chooser** (finding 16): he is listed whenever The Garden's Regard is understood, marked not ready with his reason
  ("There is nothing here I could mend": an awake bed of the inviter's within 8) until a bed is there, not only "once a bed
  of theirs is within 8" as above.
- **The overworked bed's remedies:** bone meal on a full bed composts nothing (`VerdantBedBlock.useItemOn` returns before
  `compost()` when nothing is given), so his row also names rest (a bed with no crop growing rests a strain away).
- **Sounds** (GC-P26): his seven cues draw on three recordings (two of the art plan's four files are one recording), told
  apart by pitch through `sounds.json` (`listen` 1.25, `refuse` 0.6, `depart` 0.7).
- **The whole path** (GC-P29, R27): `buerFromTheCodexToDeparture` has no chooser (it would need Stolas's Regard); the
  chooser is held by the GC-01 server tests and the client class. The client's small size is 320x180.
- **The unit review** (34 findings, 31 confirmed in full or narrowed; fixed in `4b9a038bb`): beds refused for themselves
  (not a beneficiary's now, not the holder's to change, outside the area) are skipped until the bed index is next read, so
  eight refused beds nearer the perch no longer stall him (`buerSkipsBedsHeMayNotChange`); his term names both of its
  conditions (only while you are within 64 blocks of the perch, never on a bed you could not change by hand); his words
  read his numbers from his definition and the shared constants (the journey's Buer steps in
  `tools/concordance_journey.py` still write "within 8 blocks", "(4 Mendvetch, 4 bone meal)", "a week" and "a day's grace"
  as literals: open); the geometry audit holds every bone key and cube pivot; GB26's probe tests totals of exactly 2 and 3.
- **Forced test lines** (GC-P17, R11, A-08): appending `jugcraft:buer` to `PactParser.REQUIRED` makes a catalog of Stolas
  alone report Buer's definition missing, so `thePactDefinitionParsesStrictly` now allows only the other shipped
  definitions' "no definition loaded". Two audits were widened for him (R12, A-01: Stolas's legacy sound-id check reads
  only `stolas.` events; R16, A-02: each `SPIRITS` row's offerings tag).
- **Client evidence** (looked at for this record, [Buer's Verification](buer-pact.md#verification)): the shot named
  `jugcraft_garden_buer_listen` has a 480x270 preview in the log that is byte for byte `jugcraft_garden_buer_idle`'s, so
  no picture shows his listening pose, though the test checked his client was drawing that clip when it froze; the asleep
  shot shows his petals open in the same V as by day, not folded shut as Dossier 1 item 7(a) says; the depart shot shows
  a shut bud at full height, not yet sunk; the refused picture shows Accept's tooltip and the terms' last lines, not his
  refusal. Each is a question for the review, not a passed claim.

Saved: nothing new. GB15, GB20 (task half), GB24 and GB25 (in part) closed; GB18 open (Gutenberg's record not read).

### GC-05. Bathin (unit 2)

**Changes:** `S/BathinEntity` (`jugcraft:bathin`, hovering station on the perch); `S/VerdanceCarrying` (`verdance_carrying`;
`missing`: a Heart and a Gleaner or pylon of yours within 10; complication with accords `yield`, `claim`;
`obligation() = DEVICE`); `G/GleanerBlockEntity.receiveFrom(heart, actor, max, level)` (its own draw, from a named Heart
at any distance, through `drawInto`) and `G/VerdantHeartBlockEntity.pourInto(pylon, level, maxBatches)` (the existing
pour's body, for one named pylon, its ownership rule unchanged: the keeper's party, or no one); data, sounds, lang, body
(built), renderer, record `bathin-pact.md`.

**The shared machinery that lands with him (its first user):**
- **Bed index devices:** `GardenReach` adds the awake Verdant Hearts, Gleaners and Ley Pylons in the area, 16 of each,
  nearest first, under the same scan and cache.
- **Obligation `DEVICE`:** an awake living device used with an empty hand (its report or setting) inside his area while
  his pact serves; the hook is one line after `useEmpty` in `LivingDeviceBlock.useWithoutItem`. (A device placed by a
  player who knows Verdant Husbandry is awake at once, `G/LivingDeviceBlockEntity.java:56`, and a bed too,
  `G/VerdantBedBlock.java:131`, so a waking could never be a holder's ordinary first obligation.)
- **Complications and accords:** `PactService` gains `complication(level, record, perch, now)`, returning at most one
  `Complication(id, line, pos?, answers)` (answers from the service's closed `accords()` list, at most 3), and
  `accords()`; shown on the kept page and in Jade. A new action `RESOLVE` (appended, index 10), accepted only in the
  `keep` mode from the record's holder, chooses by index into the answers the server sent; the perch saves the answer as
  an optional block-entity key **`accord`** (a string of at most 16 characters from that service's list; absent when
  unanswered; read back as unanswered when unknown; cleared when its pact ends or is unseated; the perch's stamp stays 1;
  a copied perch is inert as today). Until answered, the service takes its safe default. `PactStatePayload.Spirit` gains
  `matter` (one component, may be empty) and `answers` (at most `MAX_ANSWERS` = 3 choices); network only, the payload
  still 14 top-level fields.
- **The roster's waiting reason:** `W/Status` gains `AWAITING_ANSWER("awaiting_answer", false)`, appended (GB27): a
  presence whose `decide` would return `IDLE` while a choice waits reports it, so the roster's saved status and its row
  say "awaiting your answer"; it still works under the safe default. A new saved string in `jugcraft:worker_roster`,
  listed under Saved strings and pinned by `check_pacts` and `check_roster`.

**Tests (server):** `bathinMovesVerdanceExactly` (Heart down by what the Gleaner gains; at most 8),
`bathinPoursIntoAFarPylonAtThreeForTwo`, `bathinNeverHoldsVerdance` (a kill, an unload and a restart between thinks
change no reservoir), `bathinHonoursTheHeartsParty` (a stranger's Heart and a stranger's pylon untouched),
`bathinLeavesAHeartWithItsOwnPylonUntilAnswered`, `aClaimAnswerLetsBathinTakeFromIt`, `bathinStopsAtHisQuota`,
`bathinWaitsForAPresentHolder`, `bathinRefusesWithNothingToCarry`, `bathinsFirstObligationIsADeviceReadByHand` (a
survival player places a Heart, which is awake at once, and uses it with an empty hand inside his area),
`anAnsweredAccordSurvivesARestart`, `anUnknownAccordReadsAsUnanswered`, `aResolveFromAnotherPlayerIsRefused`,
`aResolveOutsideTheKeepModeIsDropped`, `anAccordIsClearedWhenItsPactEnds`, `theRosterSaysAwaitingYourAnswer`,
`theGardenReachCapsItsDevices`, `noVerdanceIsMintedByCarrying` (`ConcordanceEconomyGameTests`), SG12's cases.
**Client:** his poses and the both-ends shimmer (frozen on the carrying tick); his accord row; the roster's "awaiting
your answer".

**Interfaces changed:** `GleanerBlockEntity` (+`receiveFrom`), `VerdantHeartBlockEntity` (+`pourInto`, the pour's body
shared), `LivingDeviceBlock` (one hook), `PactObligations.Act` (+`DEVICE`), `GardenReach` (+devices), `PactService`
(+`complication`, `accords`), `PactRequestPayload.ACTIONS` (+`resolve`), `PactPerchBlockEntity` (+`accord`),
`PactStatePayload.Spirit` (+`matter`, `answers`), `PactKeeping` (the matter row), `Roster` and `W/Status`
(+`AWAITING_ANSWER`), `PactParser.WORK` (+`verdance_carrying`). **Saved data and ids:** `jugcraft:bathin`,
`jugcraft:bathin_pact`, `concordance/pact/bathin.json`, `#jugcraft:bathin_offerings`, `jugcraft:bathin.*` (7 events),
the perch's optional `accord` with values `yield`, `claim`; the roster status string `awaiting_answer`; the Gleaner and
Heart save `verdance` as built. **Client shots:** beats, terms, kept page with the accord question; poses `idle`,
`listen`, `inspect`, `assent`, `refuse`, `depart`, `work` frozen in their windows; the shimmer at both ends frozen on the
carrying tick; his Jade line; the roster row. **Audits:** `check_garden_pacts` (his entry, the move caps, no item
storage call in `VerdanceCarrying`, each accord list closed and at most 3, the `DEVICE` hook), `check_pacts` (the
`resolve` action appended, the perch's `accord` key and its length, `awaiting_answer`), `check_roster`
(`awaiting_answer`), `check_spirit_bodies`, `check_economy` (no new row), `check_acquisition` (ender pearls, chaff). The
perch's `accord` is pinned there, not in `check_persistence`'s `ENTRY_BY_ENTRY`, which lists SavedData read entry by
entry with `Saved.keeping` (`tools/check_mod_data.py:21758`). **Acceptance:** Dossier 2's eight items each shown by a
named test, shot or record line; GE1 to GE9 for Bathin; Verdance conserved in every test.

**As built (GC-05; brief `28a963b63`; the garden side `2e4f0acd2` and `c36b9758c`; the bed index's devices `5499bf505`;
the accord machinery `08cb27921` and `2ad18f703`; Bathin `63cf1a83d`; the unit review's fixes `4fcf55513`; whole moves
`4d035fe64` and `7af654e9a`; client test fixes `3f2bde5cf`, `85eb1a0ba` and `d7185041b`; [record](bathin-pact.md)):**

- **Bathin as a character:** `S/BathinEntity` on the shared base, registered 0.9 x 1.9 (eyes 1.65), hovering coiled on
  the perch; his body built by `tools/garden_bodies.py` (`bathin_model`: 12 box-UV cubes on 8 bones, 11 UV regions, a
  128 x 64 sheet; `bathin_skin`, `bathin_glowmask`, `bathin_animations`: five loops, six gestures) and written and
  checked by `tools/owner_art.py` (`BUILT`), painted only in colours read from the willow log and the Spanish moss (art
  plan); his ring of dust through the base's new `gestured(ServerLevel, Clip)` hook and his `shimmer` at both ends of a
  carry, nothing between; his seven cues on the art plan's five mono files, told apart by pitch; his sign the ender pearl;
  his codex pages `p8` to `p11` of `binding/garden_regard`; the contract's silhouette rule (a fifth apart on one axis)
  built in `check_spirit_bodies` for every pair of spirits.
- **Verdance carrying:** `S/VerdanceCarrying` (one move a think, the Gleaner road before the pylon road, at most 8
  places asked), `S/GardenReach` (`devices(record)`: 16 Hearts, Gleaners and pylons each, in the beds' one pass),
  `G/GleanerBlockEntity.receiveFrom`, `G/VerdantHeartBlockEntity.pourInto`, `PactObligations.DEVICE` with its one line
  in `LivingDeviceBlock`, `GardenPacts.presence`; tests as listed in `check_mod_data.py`'s `GC05_CARRYING_TESTS`.
- **The accord machinery:** `S/PactAccords` holds the accord's rules (the answer kept, the course, the matter shown, a
  choice waiting, the answer given); `PactService` gained `Complication`, `complication(...)` and `accords()` (after
  `work()`); the perch keeps the optional `accord` (GC-P42, GC-P46); `resolve` is action 10 and answered `resolved`
  (GC-P43); `PactStatePayload.Spirit` carries `matter` and at most three `answers`, the payload still fourteen top-level
  fields; the kept page's matter row; `Status.AWAITING_ANSWER` appended and reported as GC-P47 says, with one sign of
  want; Jade's readings carry `matter` and `accord` (GC-P48; `PactReading.KEYS` ends `visitor`, `note`, `note_count`,
  `matter`, `accord`, which `check_pacts` reads as `JADE_KEYS[12:14]`); words as GC-P49; tests as listed in
  `GC05_ACCORD_TESTS`.

Departures from this ticket and the unit's specification (each recorded, none hidden):

- **The matter's id** is `two_roads` everywhere (B-01; the part design's `own_road` renamed).
- **The invitation** is this contract's (B-02): an awake Heart of the inviter's within 10, and an awake Gleaner of theirs
  or a pylon of theirs or no one's within 10; the narrower "somewhere" rule of one part design is not taken (where the
  Verdance can go is the work's question: Jade's `nowhere`). Since `4fcf55513` the refusal, the perch's tooltip and codex
  page "Bathin" say "awake", as the rule always counted.
- **His two details are the shared signs** (B-03 as built): `check_signs` forbids `sendParticles` in the Concordance, so
  the ring is `Sign.SHOVE` (a few puffs of cloud at his coil) and the shimmer `Sign.WORK` (light rising at each end),
  shown by `BathinEntity` (`RING`, `SHIMMER`; his `SPIRITS` row's `"ring": "shove"`, `"shimmer": "work"`), the shimmer
  kept in memory for tests (`lastShimmer`) and the signs counted through the tests' `SignWatch`. Neither the part design's
  twelve dust puffs in a ring nor B-03's eight `GLOW` glints was built; Dossier 2 item 7's "dust ring" is drawn as puffs.
  A true ring needs a new shared sign (`Sign`, `SIGNS`, `SignClient`), a coordinated change.
- **A carry made while the matter waits reports `AWAITING_ANSWER`** (B-06), whatever the step returned, never over an
  offering due (GC-P44 as amended).
- **One Jade test**, `bathinsReadingSaysItsNoteAndNamesNoPlayer` (B-10).
- **`_poured` is not photographed** (B-12): the pylon road is held by the server tests.
- **The hook is `gestured`** (B-15), since the base already keeps a private `shown` deque.
- **`VerdanceCarrying.Memory.forget()`** (B-16): the client's raise calls it, since his device index serves 200 ticks.
- **The accord machinery landed first with no user** (R26, `08cb27921`), a compile-and-regression slice as GC-01a was;
  GC-S19 holds per unit.
- **Interfaces not in this ticket's list:** `PactSpiritEntity.gestured`, `GardenPacts.presence`, `tools/owner_art.py`'s
  built bodies (`BUILT`), `VerdanceCarrying.Memory.forget`, `GleanerBlockEntity.reaches` and `setVerdance`,
  `VerdantHeartBlockEntity.mayPourInto`, `beside` and `feedsAPylonBeside`.
- **Whole moves from a beating Heart** (GC-S25, decided by the session, `4d035fe64` and `7af654e9a`):
  `bathinCarriesWholeMovesFromABeatingHeart`.
- **The unit review's fixes** (`4fcf55513`): his matter, its note and his inspect turn count only a Heart whose keeper his
  pact still serves (`Memory.notice` and `complication` ask again; `aFormerMembersHeartRaisesNoMatter`); a place refused
  for itself is skipped until his index is next read, asking it costs no question, at most 48 are remembered, and
  `Idle.NOWHERE` is set when his questions run out (`refusedGleanersNeverStarveBathinsPylonRoad`;
  `bathinNeverTouchesAClaimedReservoir` now carries only once his index is read again, as Buer's refused beds wait); the
  two SG12 upkeep tests and `everySpiritWaitsForANearHolder` deposit each spirit's own offering count (his is 4); the
  term names whose Gleaner and whose pylon; the codex audit wants his four pages in order.
- **Words:** his Jade notes and matter are in the third person (Jade shows them to anyone looking); his term reads its
  numbers from the tables and names GC-S7's two conditions; the page row's 8 and 3 batches are tied to `CONSTANTS` by an
  audit, not derived (importing back would be circular).
- **Tools:** `tools/concordance_progression.py`'s `VANILLA_SOURCES` gains vine, hopper and flower pot (the journey walks
  to the Gleaner's recipe; a file outside the specification's list); `check_acquisition` accepts a compact item listed in
  `concordance.progression.PRODUCERS` (Verdant Chaff, grown); `check_spirit_bodies` keeps a local `import garden_bodies`
  (R24 assumed a module-level one that the file lacks, so later blocks sit after it); the garden side's audit is
  `check_verdance_roads`; `GC05_ACCORD_TESTS` stands beside `GC05_ACCORDS` and `GC05_BATHIN_TESTS` beside `GC04_TESTS`;
  `REACH["MAX_DEVICES"]` is set on its own line; `owner_art.errors` names a hand-edited built file as differing from what
  `tools/garden_bodies.py` builds.
- **Forced unit-1 lines** (GC-P59): those the specification lists, and one more in
  `aVisitorOfAnotherSpiritLeavesWhenTheChosenCannotCome` (now at least three entries, Bathin's found by his sign and not
  ready); the journey's goes through a `chooseBuer` helper; the journal test names Buer and Bathin as siblings;
  `aStaleChooserRevisionIsRefused` needed none (it reads Stolas's definition only). Tests use `Workers.BATHIN` for his
  type and `BathinEntity.COUNTERPART` for his id.
- **The client flow** uses `tabToNamed`, waits by condition (`waitFor`) rather than fixed ticks, the shared
  `standSouth` (3.0 blocks off, not 2.5) and an explicit list of facing shots; binds the roster's unbound key for its one
  look (`3f2bde5cf`); waits for the matter's controls on the page (`85eb1a0ba`); and aims the facing camera at the
  spirit's middle (`d7185041b`; no CI result recorded here).
- **The screens** are the magic interfaces' relic scroll card (GC-S26): his beats shortened, his terms as chips, his kept
  page's gauge, sockets, matter plates and rows ([his record](bathin-pact.md#the-pact-screen)).

Saved: the perch's optional `accord` (`yield`, `claim`) and the roster status `awaiting_answer`; no migration. GB20 (choice
half) and GB27 closed; GB25 unchanged (the second edited part waits for GC-07 or GC-09). Evidence:
[unit 2 as built and its evidence](#unit-2-as-built-and-its-evidence).

### GC-06. Marax (unit 3)

**Changes:** `S/MaraxEntity` (`jugcraft:marax`, standing station beside the perch; a familiar bone group);
`S/HabitatReading` (`habitat_reading`, `HOLDER_ONLY`, `personal`; `perform` gives one reading; `obligation() = READ`, its
hook one line in the empty-hand bed report; complication by task, a service row); `E/HabitatAdvice.one(niche, habitat)`
(new, pure; in the pure-package list with a harness): the one factor change that raises the verdict a step, or, when none
does, the first outside factor in `Factor` order with "one of n"; `VerdantBedBlockEntity.cachedHabitat` used; data,
sounds, lang, body (built), renderer, record `marax-pact.md`. **The synced work target (its first user):**
`PactSpiritEntity` gains `TARGET`, an `Optional<BlockPos>` entity data (network only, never saved), set while a reading's
answer is on screen and cleared after; `PactSpiritRenderers` gains a hook that moves a named bone (his familiar's group)
toward it, and the entity's culling box is widened to cover the target (at most 8 blocks away). Body yaw through
`LookAtTarget` and `BodyRotationControl` lags about 20 ticks, too slow for this, so the hook is needed; it is client-only
drawing of server state.

**Tests (server):** `aReadingNamesOneCorrectionAndChangesNothing` (a stalled Sunpetal at light 9: "light", the Dewlit
graft; every bed's nutrients, moisture, strain, graft and verdict unchanged; `SampleBudget.used` 0 for the reading;
the quota counts once; a third reading the same day `quota_spent`), `aReadingNeverAdvisesDisturbanceToASunpetal`,
`aReadingAdvisesAQuietSourceForAGloamcap`, `aReadingSkipsBedsWithoutACachedSample`, `aReadingWithTwoFaultsNamesTheFirst`,
`onlyTheHolderMayAskForAReading`, `maraxNeverReadsAStrangersBed`, `theTargetIsNeverSaved`,
`theGardensRegardIsMasteredThroughThreeSpirits` (the progression tool and a game test: Buer's, Bathin's and Marax's first
obligations),
`theProudFamiliarSkipsABedNamedThreeTimes`, `maraxRefusesWhereNothingGrows`, `maraxsFirstObligationIsABedReadByHand`,
SG12's cases; the pure harness for `HabitatAdvice` over every organism and factor. *As built, ten more (GC-P78):*
`maraxStandsBesideThePerchFacingHisInviter`, `aStandingSpiritFallsBackToThePerchWithNoFreeSide`,
`theFamiliarHookReachesItsTargetAtEveryBearing`, `theTargetClearsWhenTheConversationEnds`,
`maraxsReadingSaysItsNoteAndNamesNoPlayer`, `maraxAnswersEachActionWithHisGesture`,
`habitatAdviceHoldsForEveryOrganism` (the harness's CI form, `ConcordanceGardenGameTests`), and from his review
`maraxNeverReadsABedHeMayNotChange`, `maraxReadsPastBedsHeMayNotChange` and
`theProudCountStartsAgainWhenTheBedChanges`: 22 in all (`GC06B_TESTS`, `GC06_TESTS`), none yet run. **Client:** a
reading on screen with the familiar hovering over the named bed (frozen); his night look.

**Interfaces changed:** `VerdantBedBlockEntity.cachedHabitat` (used, from GC-03), new `E/HabitatAdvice` (pure),
`PactSpiritEntity` (+`TARGET`, synced), `PactSpiritRenderers` (+the bone hook and culling box), `VerdantBedBlock` (the
`READ` hook), `PactObligations.Act` (+`READ`), `PactParser.WORK` and `HOLDER_ONLY` (+`habitat_reading`). **Saved data and ids:** `jugcraft:marax`, `jugcraft:marax_pact`,
`concordance/pact/marax.json`, `#jugcraft:marax_offerings`, `jugcraft:marax.*`; no saved key (the pique is memory).
**Client shots:** a reading on screen with the familiar frozen over the named bed; his night look; poses; Jade.
**Audits:** `check_garden_pacts` (no `Garden.sample` or `habitat(` call from `HabitatReading`, only `cachedHabitat`;
`TARGET` never written to a save), the pure-package list, bodies. **Acceptance:** Dossier 3's items; a reading takes no sample (budget unchanged) and
changes nothing; the advice never names supplying disturbance to a plant whose niche forbids it.

**As built (brief `736d94541`; GC-06b `8cf0ef994`, the shared prefix units 4 to 6 branch from, with its review's fixes
`ee362c3cd` and `cc9db6afa`; Marax `bed9f73b4`; his review's fixes `1473e99f1`; [record](marax-pact.md); not yet compiled
or run in CI from `bed9f73b4` on):** the shared prefix first. A standing body's station
side is clear when both cells out to it are loaded and they and the cells over them, up to the body's height (at least
two: Marax, 1.4 tall, needs two; Foras, 2.2, and Amdusias, 2.4, need three), have no collision shape and the outer cell's
floor is sturdy on top. Known limits, decided by the session: the check does not read a body's width past its cell (a
1.6-wide body stands 0.3 into the cells beside its own and into the cell three out, so a full block there, such as Buer's
bed at `west(3)` in the client yard, is accepted), nor fluids (water has no collision shape, so a flooded cell counts as
clear). A target cleared or shown again mid-flight carries the familiar's reach on from where it is drawn. Marax's own
part:

- **Marax as a character:** `S/MaraxEntity` on the shared base, registered 1.6 x 1.4 (eyes 1.0), standing on the ground
  beside the perch (`Station.BESIDE`); his body built by `tools/garden_bodies.py` (`marax_model`: 16 box-UV cubes on 12
  bones, 10 UV regions, a 128 x 64 sheet, the familiar a root child with two wings; `marax_animations`: six loops, his
  night look `stargaze` among them, and six gestures; `marax_skin` and `marax_glowmask`, the familiar's two eyes) and
  written and checked by `tools/owner_art.py` (`BUILT`), painted only in colours read from the mahogany log, the Spanish
  moss and the cypress leaves (art plan); his renderer row reaches with `familiar`; his seven cues on the four gaia files,
  told apart by pitch; his sign the writable book; his codex pages `p12` to `p15` of `binding/garden_regard` and the
  entry's last page, "Mastered" (`p16`).
- **Habitat reading:** `S/HabitatReading` (holder only, on request through the kept page's Recall; `READ_BEDS` 8,
  `MAX_ASKED` 16, `PIQUE` 3, `SHOW_TICKS` 400; its `Memory`, never saved, for the proud familiar); `E/HabitatAdvice`
  (GC-06b) for the one change; `PactObligations.READ` with its one line in `VerdantBedBlock`; The Garden's Regard's
  mastery reachable through three garden spirits (GC-P75); tests as listed in `check_mod_data.py`'s `GC06B_TESTS` and
  `GC06_TESTS`.

Departures from this ticket and the unit's specification (each recorded, none hidden; the reasons in
[his record](marax-pact.md#decisions-recorded-with-the-code)):

- **The station** is two blocks out on the first clear side, a quarter turn clockwise from the inviter first, the
  inviter's side last (GC-P67; GC-P5 amended).
- **The one change** is the value nearest the present one that alone raises the crop a step, harvest strain first where
  it decided the disturbance fit (GC-P64), not a factor's ideal edge. The graft clause is offered only for a bed carrying
  no graft. `HabitatReading.planted` is public for the tests.
- **The `READ` hook** stands after the bed's report, as this ticket says; a crop's own report counts nothing (GC-P70).
- **The reach and its culling box** go to `TARGET_REACH` (24 blocks), not 8 (GC-P62 amended).
- **The restyled screens** (decided by the session, GC-S26): his beats within the limits (the service beat shortened, the
  stage direction a key of its own); every word on his pages a work glance in `tools/magic_ui.py`; the reading's remedy
  and clause arguments of their own, so the kept page draws the change ("Nutrients 0 → 1") with the sentence as its tip;
  the Recall plate drawn "Ask him", primary.
- **His reading's sign** is the shared work sign at the bed (`Signs.show`, `Sign.WORK`), not six `ENCHANT` particles:
  `check_signs` sends the Concordance's server particles only through the shared signs.
- **A copied perch** answers a reading with the shared diagnostic (`Code.CONFLICTING_AGREEMENT`), as Star-reckoning does.
- **His review's fixes** (`1473e99f1`, 21 of its 22 findings): a bed `GardenPacts.target` refuses uses none of his eight
  (`MAX_ASKED` 16; `maraxNeverReadsABedHeMayNotChange`, `maraxReadsPastBedsHeMayNotChange`); every crop thriving is an
  answer given, with his assent, counting nothing; the proud count starts again when the bed changes
  (`theProudCountStartsAgainWhenTheBedChanges`); the kept page's rows drawn short and the proud row marked; "awake" in his
  blocker and the perch's tooltip; each codex page at most 60 words; the chooser's word budget grows with its cards
  ([magic-interfaces.md](magic-interfaces.md)); the proud picture made with two server-side readings at the next day's
  start. Not applied: item 8 (Jade's short form of his note, on the magic interfaces' unit 2 line only;
  [Where to resume](#where-to-resume)); item 10 in part (the unit markers are on this line only, so units 4 to 6 take them
  before their merges).
- **Interfaces not in this ticket's list:** `S/GardenAsking` (GC-06b; Bathin's per-think questions, shared),
  `PactSpiritEntity.Station`, `showTarget`, `posed`, `hover` and `reach`, `PactScreen.workService`'s choices,
  `PactGlance.WORK_ICONS` as a `Map.ofEntries` with Star-reckoning's entry last and the proud rows' mark and icon,
  `ConcordanceGardenGameTests.afterSampling`, the client test's generic `standingFacing`.
- **Tools:** `check_pacts` and `check_garden_pacts` gain GC-06b's and GC-06's blocks, `check_spirit_bodies` his row,
  `check_magic_ui` his glances; `tools/select_client_tests.py` weighs the garden class 820 (560 + 260); Dewmoss joins
  `tools/acquisition.py` and the journey test's `GARDEN_RECIPES` (GC-P76).

Saved: nothing new (his entity writes only the base's `perch` and `record`; the target and the pique are never saved);
no migration. No GB closes here (GB26 was closed in unit 1; GB25 still waits for GC-07 or GC-09). Evidence:
[unit 3 as built and its evidence](#unit-3-as-built-and-its-evidence).

### GC-07. Foras (unit 4)

**Changes:** `S/ForasEntity` (`jugcraft:foras`, standing station); `S/GraftKeeping` (`graft_keeping`; `missing`: a store
beside the perch and a host on a bed of yours; accords `prudent`, `always`; `obligation() = GRAFT`, its hook one line in
`OrganismCropBlock.graftBed` on success); `G/Grafting.apply(level, cropPos, bed, graft)` extracted from
`OrganismCropBlock.graftBed` (`setGraft`, `forgetVerdict`, the particles; the gesture keeps its rate gate, Authority,
messages and donor spending; the spirit checks every precondition and Authority as the present holder, then extracts
and commits one donor through a Fabric transaction, then applies); `GardenReach` adds plain containers touching the
perch (16); `tools/owner_art.py` gains the `Edited-2026-10-18-Part2` root and the acorn cap's rows (GB25; whichever of
units 4 and 6 lands first adds the root, the other merges it); data, sounds, lang, body (built plus the acorn cap),
renderer, record `foras-pact.md`.

**Tests (server):** `forasSpendsOneDonorAGraftAndNeverTwo` (a full store, three hosts: three donors gone, three grafts;
a failed precondition or a failed extraction spends nothing and changes no bed), `forasNeverReplacesAKnownGraft`, `forasGraftsAgainAfterAReplanting`,
`forasTakesOnlyFromAStoreTheHolderMayChange`, `forasRefusedWhereTheHolderMayNotBuild` (crop or bed),
`forasNeverGraftsAStrangersBed`, `twoForasNeverGraftOneBedTwice`, `aPrudentForasSkipsAUselessGraft`,
`anAlwaysAnswerGraftsEveryHost`, `forasRefusesWithoutAStore`, `forasStopsAtHisQuota`, `forasWaitsForAPresentHolder`,
`forassFirstObligationIsAGraftByHand`, `theGraftGestureBehavesAsBefore` (existing graft tests unchanged), SG12's cases.

**Interfaces changed:** new `G/Grafting.apply`; `OrganismCropBlock.graftBed` calls it (its behaviour unchanged) and
holds the `GRAFT` hook; `PactObligations.Act` (+`GRAFT`); `GardenReach` (+containers); `tools/owner_art.py` (+the second
edited part's root); `PactParser.WORK` (+`graft_keeping`). **Saved data and ids:** `jugcraft:foras`, `jugcraft:foras_pact`,
`concordance/pact/foras.json`, `#jugcraft:foras_offerings`, `jugcraft:foras.*`, accord values `prudent`, `always`; the
bed's `graft` key as built. **Client shots:** the graft gesture frozen with the cap tipped; the kept page's graft count
and accord question; poses; Jade. **Audits:** `check_garden_pacts` (every precondition checked before the transaction, one
transaction for the extraction, `setGraft` only after its commit, no `ItemStack.consume` from a spirit), `check_graft` (unchanged rules), `owner_art --check` (the acorn cap),
bodies, `check_acquisition` (honeycomb, Dewmoss). **Acceptance:** Dossier 4's items; exactly one donor for each graft,
none for a refusal; no graft replaced while the rules know it.

### GC-08. Bifrons (unit 5)

**Changes:** `S/BifronsEntity` (`jugcraft:bifrons`, low station beside the perch; stands while listening);
`S/RootRunning` (`root_running`; accords `fallow`, `full`; `obligation() = FEED`, its hook one line where bone meal on a
bed or crop succeeds; the measuring marks as bounded server particles at source and destination, no client hook); `tools/concordance_economy.py`
`CONVERSIONS` + `("root_running", "nutrient", 2, "nutrient", 1)` with its `ANCHORS`; data, sounds, lang, body (built),
renderer, record `bifrons-pact.md`.

**Tests (server):** `aRootRunnerLosesHalfAndMintsNone` (sum before minus sum after equals what was given),
`aRootRunnerNeverTakesWithoutRoom`, `aRootRunnerNeverEasesStrain`, `aRootRunnerTakesOnlyFromFallowBedsUntilAnswered`,
`aFullAnswerNeverTakesFromAHeartsBed`, `aRootRunnerNeverTouchesAStrangersBed`, `aRootRunnerRunsOnlyByNight`,
`aRootRunnerWaitsForAPresentHolder`, `bifronsRefusesWithFewerThanTwoBeds`, `bifronssFirstObligationIsAFeedingByHand`,
`rootRunningLeavesNoGainingCycle` (`ConcordanceEconomyGameTests` and the tool), SG12's cases. **Client:** his two shapes
(low, risen) and the measuring gesture's two beats (the marks at source, then destination).

**Interfaces changed:** `tools/concordance_economy.py` (`CONVERSIONS` and `ANCHORS` +`root_running`), the bone-meal
hook (`FEED`), `PactObligations.Act` (+`FEED`), `PactParser.WORK` (+`root_running`). **Saved data and ids:** `jugcraft:bifrons`, `jugcraft:bifrons_pact`,
`concordance/pact/bifrons.json`, `#jugcraft:bifrons_offerings`, `jugcraft:bifrons.*`, accord values `fallow`, `full`.
**Client shots:** his low shape and his risen shape (listening), the two-beat measuring gesture, a run's kept-page line,
poses, Jade. **Audits:** `check_garden_pacts` (`take(2)` before `give(1)` only after a room check; no `compost` or
`setNutrients` call), `check_economy` ("gaining cycles: none"), bodies, `check_acquisition` (logs, emerald).
**Acceptance:** Dossier 5's items; nutrients never rise in sum; half of every unit lost and accounted.

### GC-09. Amdusias (unit 6)

**Changes:** `S/AmdusiasEntity` (`jugcraft:amdusias`, standing station); `S/CanopyShelter` (`canopy_shelter`; accords
`spare`, `all`; `obligation() = HARVEST`, its hook one line beside the hand harvest's cultivation record); the pure rule in
`ecology` (`check_baselines` keeps `ecology` pure Java, `tools/check_mod_data.py:14890-14902`): `Habitat` gains a
seventh component `shelter` (0 through the existing constructors, carried by `with`) and `Habitat.sheltered(int
damping)`, which lowers `disturbance` by up to `damping`, never below 0, records what it took in `shelter`, and leaves
`strained()` at `min(max, disturbance + shelter + strain)`, the undamped value, with harness cases; the runtime registry
`G/Canopies` (new, in the garden package, memory only: a per-level registry keyed by holder, an index by chunk, at most
one a holder, lapsing 40 ticks after its last refresh); `VerdantBedBlockEntity.habitat` and `cachedHabitat` apply the
largest covering canopy's damping (at most 2; never a sum) only when the block above is an `OrganismCropBlock` whose
niche's disturbance minimum is 0, or the canopy's accord is `all` (so never for a Verdant Heart, whose habitat is the same
`bed.habitat(level)`, `G/VerdantHeartBlockEntity.java:99`), adding a line "Sheltered by Amdusias" to the reasons;
`term.quota.canopy_shelter` ("At most %s beds sheltered at once", GB23: his `done_today` never rises, so the generic
"%s a game day" would be wrong); `tools/owner_art.py` the second edited part's root and the flower crown's rows (if unit 4
has not added the root); data, sounds, lang, body (built plus the flower ring), renderer, record `amdusias-pact.md`;
ECO's record notes the reading change.

**Tests (server):** `aCanopyLowersOnlyDisturbanceForCropsInItsArea`, `aCanopyNeverSuppliesDisturbance` (a Gloamcap with no
magic block still stalls), `aCanopyNeverHelpsAHeart` (a Heart on a covered bed beats as before),
`aCanopyNeverEasesStrain` (strain 3 near a crucible: the same strained fit with and without the canopy),
`twoCanopiesNeverStack` (two holders' canopies over one party bed: the larger damping, not the sum),
`aCanopySparesAGloamcapUntilAnswered`, `aCanopyLapsesWithin40TicksOfSuspension` (and of the tree's felling, the perch's
breaking, the holder leaving or going 100 blocks away), `aCanopyCoversAtMost16Beds`, `aStrangersCropIsNeverSheltered`,
`amdusiasRefusesWithoutATree`, `amdusiassFirstObligationIsAHarvestUnderHisCanopy`,
`theInspectPageAndTheGaugeReadTheShelteredValue`, `theCanopysQuotaIsWordedAsACap`, `theEconomyStillHasNoGainingCycle`,
SG12's cases; the habitat harness's existing cases unchanged with no canopy, and new `Habitat.sheltered` cases (never
below 0, `strained()` unchanged). **Client:** the late horn cue (subtitle frozen on screen), the swaying tree, his poses.

**Interfaces changed:** `E/Habitat` (+`shelter`, +`sheltered`; pure), `VerdantBedBlockEntity.habitat` and
`cachedHabitat` (the canopy), new `G/Canopies`, the harvest hook (`HARVEST`), `PactObligations.Act` (+`HARVEST`),
`PactParser.WORK` (+`canopy_shelter`); `InspectPage` and the bed's Jade line read the sheltered value and its line.
**Saved data and ids:** `jugcraft:amdusias`, `jugcraft:amdusias_pact`, `concordance/pact/amdusias.json`,
`#jugcraft:amdusias_offerings`, `jugcraft:amdusias.*`, accord values `spare`, `all`; nothing saved for a canopy.
**Client shots:** the Inspect page and a bed's Jade showing "Sheltered by Amdusias"; the swaying tree; the late horn's
subtitle frozen on screen; his terms page with the quota as a cap; poses. **Audits:** `check_garden_pacts` (the damping
bound 2, never below 0, never a Heart, the largest not the sum, the strained value undamped, the canopy cap and lapse),
`check_baselines` (`ecology` still pure), `check_garden` (habitat rules unchanged without a canopy), `owner_art --check`
(the flower ring), bodies, `check_acquisition` (note block, saplings). **Acceptance:** Dossier 6's items; with no canopy
every existing habitat, garden and Gauge test unchanged; a canopy lapses within 40 ticks of the pact ceasing to serve.

### GC-10. Evidence, checkpoint and handoff (unit 7)

**Changes:** combined fixes from units 2 to 6's reviews; `ConcordanceJourneyGameTests.theGardenPactsFromCodexToDeparture`
(one survival player: research to the Regard mastered through three spirits' first obligations, every refusal shown
once, each spirit's service and conflict exercised, ending each with its escrow once); the garden load card; the
client line-up and evidence; the two-client test with one shared second-client helper; the records; the checkpoint;
the handoff on 10:133-146.

**Tests:** as in [Load and multiplayer evidence](#load-and-multiplayer-evidence) and the validation plan.

**Files:** `T/ConcordanceJourneyGameTests` (the whole path), new `T/ConcordanceGardenLoadGameTests`, new
`T/ConcordanceGardenNetworkTests` and the shared second-client helper; `tools/concordance_journey.py` (`GARDEN`),
`tools/acquisition.py` (`GARDEN_PACTS`), `tools/check_mod_data.py` (`check_garden_load_card`, `check_pact_network`),
`.github/workflows/build.yml` (the two additive, opted-in steps), the six spirit records, this page (Unit 7 sections on
D-09's shape: candidate and CI, combined fixes, the demonstration GE1 to GE9, client evidence with a "Seen" column, the
load card verbatim, the two-client run, not run, known defects, record fields, the handoff, the checkpoint),
`docs/TESTING.md`, `docs/WHAT_EXISTS.md`, `docs/INTEGRATION_STATUS.md`, `CHANGELOG.md`. **Client evidence:** the
line-up shot (all seven pact spirits side by side from the front at the same distance), each spirit's poses, facing and
matter, the chooser with seven entries, the roster with seven pact portraits, screen fit. **Acceptance:** CI by
workflow_dispatch with every client class, `load_card` and `clients_without_optional` ticked, every job passing on one
candidate commit; the load card's block pasted verbatim; the two-client run passed or recorded as failed with its log;
every shot named in the records looked at and marked; the checkpoint at most three questions, 300 to 500 words (10:87-121);
the handoff on 10:133-146.

### The per-spirit template (every spirit ticket, GC-04 to GC-09)

Each spirit ticket also delivers, and is accepted only with:
- its brief (its dossier above) committed in its record **before** its body files (D-D5), naming its palette files
  (library path, catalog row, SHA-256) before any body file;
- the source quotations it puts in game checked against a scan, or no quotation in game (GC-S14);
- every vanilla, GeckoLib and SmartBrainLib call it writes checked against the reference sources first, the check
  recorded in the ticket (GC-S24);
- its `concordance/pact/<spirit>.json` (schema 1, strict) generated from `SPIRITS`; its work id appended to
  `PactParser.WORK` (and `HOLDER_ONLY` for Marax) and the generator; its service class registered;
- its first obligation's `Act` and one-line hook;
- its entity on `PactSpiritEntity` (no copy of Stolas's class), its body table, station and reaction table;
- its body, clips and skin from `tools/garden_bodies.py` (no I/O, no randomness), checked by `check_spirit_bodies`
  (including the twin-face rule) and `check_geckolib` (`tools/art_check.py` reads block, item and quad models only, not
  `.geo.json`, so it is not a body check); provenance rows with library path, catalog row, SHA-256 and change;
- seven sound events bound to the existing runtime mono files the art plan names (channels checked there), each
  subtitled; no voice;
- its codex page under `binding/garden_regard`; its lang through the generators only;
- its beats within the magic interfaces' limits (18 words a line, a stage direction of 8; GC-S26) and its work's row
  in `tools/magic_ui.py` `WORK_GLANCES` (its name, service line, icon, kept-page rows, blockers and chooser reason), a
  glance for its first task's act when that row would run past 12 words, and its icon's line in `PactGlance.WORK_ICONS`;
- SG12's shared cases passing for its definition (including `onePresencePerPact`, `...WaitsForANearHolder` and
  `noGardenTermOrRefusalNamesAnAstrolabe`); its own service, conflict, refusal, obligation, stranger and departure tests;
- its client shots (beats, terms, kept page with its matter, poses in their telling windows from its front, facing, Jade)
  recorded with a "Seen" column;
- its record on SP's shape with the CLAUDE.md fields; `docs/WHAT_EXISTS.md` and `CHANGELOG.md` lines;
- its unit's audit mutations reported.

## Progression route, journey and acquisition

**Route** (`tools/concordance_journey.py`, a new `GARDEN` route and `GARDEN_TEST = "theGardenPactsFromCodexToDeparture"`;
per-spirit sub-routes in each unit, Buer's first): First Light understood; the Binding Arts understood (Spiritbinders);
Verdant Husbandry understood (Greenwardens); beds woken and organism crops grown; The Garden's Regard encountered
(examine), observed (an organism crop harvested by hand), understood (a familiar, shade, porter or hand kept at work);
a pact perch crafted and placed where the player may build (existing recipe); each spirit's fixture (a bed; a Heart and a
Gleaner, or a pylon with Circle Lore; a planted bed; a store and a host; two beds; a tree); the chooser; the compact;
three spirits' first obligations master the Regard (from unit 3; the route asserts only up to understood in units 1 and
2). Everything Practitioner, Overworld, solo; no Celestial Attunement
(compacts carry no resonance); Circle Lore only for Bathin's optional pylon branch.

**No circular unlock:** the Regard needs only Binding Arts and Verdant Husbandry understood; invitations need the Regard
understood, not mastered; mastery needs pacts. The hand's gate (Stolas's Regard mastered) is untouched.

**Acquisition** (`tools/acquisition.py`): the cohort registers **no item, block or block entity type** (GC-S22). A
`GARDEN_PACTS` table names every compact material and offering and its survival source: Mendvetch, Dewmoss, Sunpetal,
Verdant Chaff (the garden); bone meal; paper and book; honeycomb (bees and shears); ender pearls (endermen, or a cleric's
trade); emerald (mining, trades); oak logs, any logs, oak and any saplings; a note block (vanilla recipe). Held by
`check_acquisition` through `made_as` as the pact hall's rows are. **Equivalence:** consumed offerings and compact
materials stay in equivalence (no new item to exclude); `values.json` and the survival registry are unchanged by the
cohort.

**Economy:** only Bifrons adds a conversion (nutrient 2 to 1); Bathin uses `verdance_to_ley` (3 for 2) as the Heart does;
no service makes nutrients, Verdance or items; `tools/concordance_economy.py` must still report "gaining cycles: none".

**Progression graph:** `PRACTICES["garden_pact_service"] = ("spiritbinders", "garden_regard", ["pact_perch",
"verdant_bed"], [])`, and for this practice only the tool counts the shipped garden pact definitions against its
`distinct` (3), so mastery reads unreachable until three ship (GB26); `stolas_regard`, `pact_service` and every other
practice unchanged.

## Art plan (owner library first)

Searched: `survey-assets.md` (every creature rig in the library, its registration and its fit) *(survey for the
library's contents)*. Every file named below was read for this contract: its path, its row in the catalog
(`art/owner-library/catalog/files.csv` line, "CSV n"; or entry n of `catalog/edited-2026-10-18-part2/files.json`
`files[]`, counted from 0, "P2 #n") and its full SHA-256. Rules applied: owner files used directly when suitable,
runtime copies only, originals intact, provenance with SHA-256; no registered creature's identity (CD don't 15, kept
strict, GC-S12); creatures "cute and clean": two or three tones a part, no noise, simple faces
(`docs/ART_DIRECTION.md:71-83, 243-251`); every model closed, no exact reversed twin on one plane
(`docs/ART_DIRECTION.md:31`), no shared face planes unless recorded as the owner's, UVs inside the sheet; never
executing or bulk-importing the library. The palette files below are the brief's; a unit may change them only in its
brief, before any body file.

| Spirit | Body source | Runtime files | Generator and change | Provenance to record |
|---|---|---|---|---|
| Buer | The owner's **Decor Blossom**: `Magic/assets/ars_jimbaux/geo/decor_blossom.geo.json` (CSV 19364, `3209e72155163331b462fb5cf1d2062917c34ec7c972987b51886146adfe4d4e`), `animations/decor_blossom_animations.json` (CSV 19091, `368a8b72ec7d921eca0cb3f89b764df55e29ddf6aecf3fc16659a41572f191db`), `textures/block/decor_blossom.png` (CSV 20417, 32x32, `2334749d3e11db8e6880bc60fb5d76081c24f11fe14e4e26ad02b330a263a68b`) | `geckolib/models/entity/buer.geo.json`, `geckolib/animations/entity/buer.animation.json`, `textures/entity/buer.png`, `buer_glowmask.png` | **Texture byte for byte; geometry adapted (stem lengthened, twin faces dropped); clips kept and extended.** `tools/owner_art.py` `OWNER_FILES` (the texture) and `ADAPTED` through `tools/garden_bodies.py`: the `base` stem cube repeated upward; the `down` face of the leaf plate (`[10,0,10]`) and of the pistil cap (`[4,0,4]`) and the `south` face of each pistil blade (`[2,6,0]`) dropped (GB24); the owner's `open`, `close`, `active` clips kept; his loops and gestures keyed on `base`, `petal1`-`4`, `petalNa`, `pistil`; glowmask the pistil's brightest texels | Paths, catalog rows, SHA-256, each geometry change, the folder's label and the owner's declaration (`PROVENANCE.md`); the decorative block `nouveau_decor_blossom` keeps its own use (a block, not a creature) |
| Bathin | Built. The library's only unused upper body on a long tail, Ars Elemental's **Siren**, is not used: it is a mermaid with a girl's face and hair, and the Ember record maps "the mermaid skins" to a later slice (one identity a body) | `bathin.*` | `tools/garden_bodies.py` box-UV tables in `tools/concordance_worker_models.py`'s manner; coil from `Blocks/biomes and tree blocks/willow_log.png` (CSV 2164, `977f1d86eb2a9d30bd3ca7a1f69d592f9c3fc881eb167c2e4b4359e7e974b895`), moss from `spanish_moss.png` (CSV 2069, `063d9313efbe9a01921461829d92eab3ecaa3c0ef6bcaf0b95c4aef06d13e3c7`) | Each palette's path, row and SHA-256 |
| Marax | Built; the familiar a small bone group | `marax.*` | as above; hide from `mahogany_log.png` (CSV 1925, `1ed1e99490963ed3137a583c259dfd1440d23a4b391f0e1b761f073106e7c180`), shoulder moss from `spanish_moss.png` (CSV 2069), the familiar's leaves from `cypress_leaves.png` (CSV 1758, `d85109e4f50b37908248dda43511d38741b1be9780c6e554d18421320f434ae5`) | as above |
| Foras | Built, plus the owner's **acorn cap**: `Edited-2026-10-18-Part2/jims mobs/geo/item/armor/acorn.geo.json` (P2 #23467, `913962495527ed87e4bbe2d0e5fa29a79c04200bb762c602ded852b34f658b13`; 128x128 box UV, 4 cubes on `armorHead`) and its texture `jims mobs/textures/item/armor/acorn.png` (P2 #25844, 128x128, `947202e4d51ba69035d7fef89566f6619236de79270bfdf79285d538c05db2d3`) | `foras.*` | the cap's cubes copied and re-parented under `head`, its texels copied into Foras's sheet; robe from `fir_log.png` (CSV 1808, `26eb9fba8fccfce7d07526531a5a59dd6a6253361f0575f5857e26a081c17e83`) and `spanish_moss.png` (CSV 2069) | the acorn files' paths, entries, SHA-256 and change |
| Bifrons | Built | `bifrons.*` | roots from `pine_log.png` (CSV 2013, `a239df254b764327558f06a24604b5a6091b50016d7b283978258b3160e10d26`), stones from `sphalerite.png` (CSV 2071, `a32a3989555df6650fdf9a8888d328ffbfea274e0200545b0bd1f04feafe8b23`) | palettes' sources |
| Amdusias | Built, plus the owner's **flower-crown ring**: `Edited-2026-10-18-Part2/cnb/geo/armor/flower_crown.geo.json` (P2 #19, `c3cfaa055b62910d96e009c510ad50e4c5e4be6e2952258351b7dbeb2dff47fe`) and `cnb/textures/armor/flower_crown.png` (P2 #177, 48x48, per-face, `61b8e6fa10914bddc7f091d7fca62f488230b488b2d4c35648a8ef7e10b1c1c4`) | `amdusias.*` | the ring's cubes and per-face texels copied onto the neck; coat from `white_petals.png` (CSV 2151, `20b88497e849470631ac740a8e646c365784063d0c74762a85853be9123c3fc0`); horn from `origin_oak_log.png` (CSV 1976, `c147b6cdc087ce197f4dfd422595d8216ba131efef2ed0f3727014cd033b7969`) | the ring's files, entries, SHA-256 and change |

The palette files are under `art/owner-library/originals/Blocks/biomes and tree blocks/`, 16x16, which
`tools/owner_art.py` already reads (`Blocks/`). The two non-creature parts are under
`art/owner-library/originals/Edited-2026-10-18-Part2/`, which it does not read today (`tools/owner_art.py:75, 78`; GB25):
GC-07 or GC-09, whichever lands first, adds that root and the parts' rows.

Not used (GC-S12): the **Siren** (an unused, unregistered upper body on a long curled tail: a small mermaid with a girl's
face and hair; the Ember record maps "the mermaid skins" to a later slice) and the **Flarecannon** (a fire golem; no
garden fit; the open `fire_golem.bbmodel` point and Ember's planned familiar); registered rigs (the moth and nudibranch
mounts, the weald walkers, the Fable and grove creatures) under don't 15. **Line-up by size:** Foras 2.2, Amdusias 2.0,
Bathin 1.9, Bifrons 1.8 risen and 0.6 low, Buer 1.7 across, Marax 1.4 high and 2.2 long, Stolas about 1; ways of being
present: rooted, hovering coiled, lying massive, standing tall, crawling then rising, standing slender. None is a bird.

**Checks once, in unit 1:** `check_geckolib` reads per-face UV (bounds per face rectangle; overlap is not meaningful
there) and accepts owner-drawn shared texels only through a recorded list; `check_spirit_bodies` holds each spirit's
`SPIRITS` row (bones, clips, telling windows, scale, triggerable names, a bounding box that differs from every other
spirit's by at least 20% on one axis) and refuses a zero-thickness cube that draws both opposite faces.

**Sounds (GC-S13):** existing runtime mono files under new events `jugcraft:<spirit>.<cue>` through
`tools/concordance.py`'s `SOUND_EVENTS`/`SOUND_FILES`. Every file of every event named here was checked for this contract
(its Vorbis identification header: one channel), so no unit picks a cue family later:
- **Buer** (bloom, healing): `fable_butterfly_heal`, `grove_entity.cactem.heal`, `grove_item.heal_spell_book.player_heal`,
  `alchemy_peheal`.
- **Bathin** (water and conch): `cavern.magic_conch_summon`, `cavern.magic_conch_cast`, `cavern.sea_staff_bubble`.
- **Marax** (earth): `nouveau.gaia_family`.
- **Foras** (wood): `fable_mojo_idle`, `fable_mojo_spawn`, `fable_mojo_step`, `fable_mojo_hurt`, `fable_mojo_death`,
  `fable_mojo_base_attack_hit`, `cavern.thornwood_branch_break`.
- **Bifrons** (roots): `beast.banana_slug_slime_expand`.
- **Amdusias** (a far horn and whistles): `pocket_horn_fade`, `cavern.tephra_whistle`, `wildlife_item.whistle`.

Not used, because every file is stereo, which `check_pacts` refuses (GB28): `cavern.candy_cavity_ambience_additions`
(once proposed as Buer's chimes), `cavern.forlorn_hollows_ambience_additions` (Amdusias's bells),
`cavern.abyssal_chasm_ambience_additions` (Bathin's bubbles). No downmixed copy is added. Never Stolas's
doorbell chime or flutter. Every cue subtitled.

## Load and multiplayer evidence

**Load card (GC-S17; GC-P141):** `T/ConcordanceGardenLoadGameTests.loadCard`, a server game test that passes at once
unless Fabric's filter property names it, sharing Chunk C's `Measure`, `Traffic` and `Stats`, force-loading only its own
yard. Workload: two survival holders, each keeping all six garden pacts and Stolas's (14 pacts, 14 perches) over a
garden of 48 awake beds with crops, 2 Hearts, 2 Gleaners (one beyond 4), 2 pylons (one not beside a Heart), a Maw, 8
disturbing blocks and a tree; strain and nutrients seeded so every service has work; a baseline span and an active span
of 6,000 ticks each. Reported: median and p95 server tick net of the test's own work, retained heap, packets per holder;
per-call costs timed after the spans (each service's think, a `GardenReach` refresh, a canopy lookup per habitat build, a
reading, a graft transaction). Asserted from the run: no Verdance or nutrient minted (sums), one donor per graft, no
sample taken by a spirit, at most one change per spirit per think, at most one canopy a holder and no two canopies
summed. Budget in C-P19's form,
proposed: +1.0 ms median, +2.5 ms p95, +4 MiB retained beyond the baseline (reported; asserted at five times). Run in the
existing `load-card` job after the two cards, under the same `load_card` input and guard (an additive, opted-in step:
for the owner's review in the pull request). Held by a new `check_garden_load_card`.

**Server multiplayer cases** (several test players; never called a party of people): a stranger's bed, Heart, pylon,
store and crop inside each spirit's area are never touched; a party member's are served only while they are in the
party; at one perch only its owner may seal, and the other player is refused `NOT_PERMITTED`
(`twoNegotiationsAtOnePerchSealOnce`, every definition); a stranger cannot `RESOLVE` or `INVITE` at another's perch; a
holder 100 blocks away in the same level is not served; the latest speaker is faced.

**Two clients (GC-S18; GC-P142):** `T/ConcordanceGardenNetworkTests`, the third two-client test, so the second-client
launcher is moved onto one shared helper used by it and `ConcordancePactNetworkTests` (`OwnerCollectionsNetworkTests`
is not edited). On a loopback dedicated server: the holder seals Buer; the peer (offline `GardenPeer`) stands by a
strained bed of its own inside Buer's area and by the perch: the peer's bed is never eased; the peer opens the
holder's perch, Buer turns to the peer while it speaks, and the peer's End is refused as not the holder's (as
`ConcordancePactNetworkTests` shows for Stolas); the peer sees Buer's work gesture
and the bed's strain fall on the holder's bed. Evidence under `owner-network-evidence/`, `peer.log` uploaded by every
shard. Added to the opted-in `clients_without_optional` class list (an additive change; `check_pact_network` updated).
Recorded as two automated clients on one machine, never as two people.

**Not claimable without people:** a human playtest alone or with others; balance; frame time on a player's hardware;
any sound heard (CI has no sound device; subtitles are checked as defined and fired).

## Completion evidence

| Code | Requirement | Tickets | Tests |
|---|---|---|---|
| GE1 | No one binds another player or works another's land (E1) | GC-01, GC-03 to GC-09 | `anotherPlayerCannotAcceptForAnySpiritsHolder` (SG12, every spirit), `aResolveFromAnotherPlayerIsRefused`, each spirit's stranger test (`buerTendsOnlyBedsOfHisBeneficiaries`, `buerSkipsBedsHeMayNotChange`, `bathinHonoursTheHeartsParty`, `bathinNeverTouchesAClaimedReservoir`, `onlyTheHolderMayAskForAReading`, `maraxNeverReadsAStrangersBed`, `forasTakesOnlyFromAStoreTheHolderMayChange`, `forasNeverGraftsAStrangersBed`, `aRootRunnerNeverTouchesAStrangersBed`, `aStrangersCropIsNeverSheltered`); SG12's `everySpiritWaitsForANearHolder`; the network test |
| GE2 | Upkeep suspends without hidden offline punishment (E2) | GC-01 (SG12) | SG12's upkeep cases for every definition; each spirit's `...WaitsForAPresentHolder` (`buerWaitsForAPresentHolder`, `bathinWaitsForAPresentHolder`); `aSuspendedBathinCarriesNothing`; SG12's `everySpiritWaitsForANearHolder` (no service where no upkeep accrues) |
| GE3 | Copying multiplies nothing (E3) | GC-01, GC-05 | `aCopiedPerchIsInertForEverySpirit` and `onePresencePerPactForEverySpirit` (every definition: a copied or duplicate spirit entity never works); `aDuplicateBathinCarriesNothing`; `anAnsweredAccordSurvivesARestart` with a copy reading nothing |
| GE4 | End, death, unload and restart return property once (E4) | GC-01, GC-05, GC-07, GC-08 | SG12's ending and removal cases; `bathinNeverHoldsVerdance`, `noVerdanceIsMintedByCarrying`; `forasSpendsOneDonorAGraftAndNeverTwo`; `aRootRunnerLosesHalfAndMintsNone` |
| GE5 | Two users cannot both own one pact place or one unique service (E5) | GC-01, GC-04, GC-05, GC-07, GC-09 | `twoNegotiationsAtOnePerchSealOnceForEverySpirit` (every definition; the other player `NOT_PERMITTED`); `twoBuersShareOneBedsTendWindow`, `twoBathinsShareOneHeartExactly`, `twoForasNeverGraftOneBedTwice`, `twoCanopiesNeverStack` |
| GE6 | The named suites extended (09:172) | every ticket | Worker, Sharing, Persistence, Logistics (none expected), Economy, Journey, Progression, Journal (unit 1: Sharing's `notesNeverStandInForTheGardensRegard` and Persistence's `theGardenGroundworkSavesNothing`, beyond the specification; copies only by the Worker class's `aCopiedPerchIsInertForEverySpirit`; Economy no new test; unit 2: Economy's `noVerdanceIsMintedByCarrying`, Persistence's `anAwaitingAnswerSurvivesARestart`, Garden's four named-road and device tests, Sharing unchanged); `ConcordanceGardenPactClientGameTests`, `ConcordancePactClientGameTests`, `ConcordanceRosterClientGameTests`, `ScreenFitClientGameTests`, `ConcordanceGardenNetworkTests` |
| GE7 | A conversation-to-service-to-departure sequence (09:174) | GC-04, GC-10 | `buerFromTheCodexToDeparture`; `theGardenPactsFromCodexToDeparture`; the client classes |
| GE8 | Mechanically distinct services (03:170, 03:177) | GC-04 to GC-09 | the distinctness table held by `check_garden_pacts` (one work, one service class, one state changed, distinct verbs); each service's own tests |
| GE9 | Each dossier's eight items (03:167-175) | GC-04 to GC-09 | the per-spirit template's checklist in each record, every line naming its test, shot or record |

### Unit 1 as built and its evidence

Written on 28 October 2026 for unit 1's records commit from the CI runs' own job lists and logs (read through GitHub's
REST API and its job-log tool) and from the commits' messages. "Passed" means the run's log says so. Nothing here was run
by a person, and one client is not two: the garden's two-client test is unit 7's. AI-assisted implementation with Claude
Code; the model is named in the commit trailers.

**Commits** (branch `claude/garden-cohort`; draft pull request [#526](https://github.com/jimbozoomer-byte/jugcraft/pull/526)
against main, not merged):

| Commit | What it holds |
|---|---|
| `62b0095fa` | GC-01a: the compile-only slice (SG1 to SG3; Stolas a thin subclass; Chunk D's audit needles retargeted; no lang, data, asset, client or test change) |
| `202e75969` | GC-01: the `SPIRITS` table with its zero-diff gate, then the chooser, wire, words, Jade, roster, journal, renderer table, SG12 for Stolas and GC-01's tests |
| `adc822110` | GC-02: facing |
| `80c311c93` | GC-03: the garden groundwork, The Garden's Regard, its journey and guide blocks |
| `6101ad482` | two GC-03 server tests' setup fixed ([Corrections](#corrections-to-unit-1s-commit-messages)) |
| `9742f240b` | GC-04a: Buer's brief, before any body file (D-D5) |
| `3e736655a` | GC-04: Buer, end to end |
| `4b9a038bb` | the unit review's fixes ([GC-04's As built](#gc-04-buer-end-to-end-unit-1)) |
| `1129567eb` | merge of the branch as pushed (`18ac27f2c`: GC-01a with Chunk D's merges of main's #488 and of two game-test changes, releasing what the server sends to mock players and logging the heap) |
| `cfeded41e` | merge of main through #524: #494 carries Chunks B to D as one squashed commit, so the merge was made against Chunk D's final head `301d64028` as its base; only the generated lang conflicted, and it was regenerated |
| `10566d074` | merge of main's #525 |
| `4ca363d38` | the three server tests run 40597482976 failed, each a fault of the test, fixed in the test (the game code met its contract in each) |
| this commit | the records |

**CI runs** (the pull request runs test #526 merged into main's `233cca018`):

| Run | Event and commit | Result |
|---|---|---|
| [40421207295](https://github.com/jimbozoomer-byte/jugcraft/actions/runs/40421207295) | workflow_dispatch (every client class) on `18ac27f2c` (GC-01a with Chunk D's merges) | Compiled. `mod` and `optional integrations absent` each passed all 2,788 required server tests, Stolas's unchanged. Of 16 client shards 12 passed, GC-01a's three named classes among them (`ConcordancePactNetworkTests` in shard 3, `ConcordancePactClientGameTests` in shard 5, `ConcordanceRosterClientGameTests` in shard 14); shard 12 failed in `local.peepo.PeepoPopulationClientTests` (a timed-out cow-breeding wait, `CompanionPopulationChecks.java:94`, after the server fell 121 ticks behind; not a unit-1 class); shards 2, 9 and 10 were cancelled with the run when the base moved. GC-01b to GC-04 had no CI run of their own: they were pushed together and first ran in the next run |
| [40597482976](https://github.com/jimbozoomer-byte/jugcraft/actions/runs/40597482976) | pull_request on `10566d074` | Compiled. `mod` and `optional integrations absent` each ran 3,109 server tests and failed the same three, all unit 1's: `theGardenReachIsCappedAndCached` ("The nearest broken, it is gone at once, and nothing is scanned again"), `buerEasesOneStrainAtTheBedsOwnRate` ("2 to 1, still nothing else moved, counted again") and `buerFromTheCodexToDeparture` ("A Mendvetch paid ahead; he looks it over"), fixed in `4ca363d38`. The selection picked 31 of 274 client classes; all 16 client shards passed, `ConcordanceGardenPactClientGameTests` in shard 0 |
| [40605564166](https://github.com/jimbozoomer-byte/jugcraft/actions/runs/40605564166) | pull_request on `4ca363d38` | Compiled. `mod` passed all 3,109 required server tests with the optional integrations: 73 more than main's own run of that base ran (3,036 in run 40596966503's `optional integrations absent` job), the number of tests unit 1 adds (`GC01_TESTS` 30, `GC02_TESTS` 7, `GC03_TESTS` 12, `GC04_TESTS` 24). `optional integrations absent` failed one test, main's `herobrine_marks_game_tests_a_grove_takes_only_the_leaves_and_leaves_the_logs` ("16 leaves are left on the bare tree"; main's run 40596966503 fails it the same way, "5 leaves"), so every unit-1 test passed there too. Of 16 client shards 15 passed, the garden class in shard 0 and Stolas's in shard 3; shard 13 failed in main's `HerobrineClientGameTests` ("Test encounter placement failed: NO_VISIBLE_SPOT"; it passed on `10566d074` and in main's run; unit 1 changes no Herobrine file; not investigated here). The two failed jobs were re-run once (attempt 2): `optional integrations absent` then passed all 3,109 required tests, the grove test among them; shard 13 failed again the same way, so the run's conclusion stays failure, on that main class alone |

**Server tests** (all passed in run 40605564166, with and without the optional integrations):

| Ticket | Tests |
|---|---|
| GC-01 (`GC01_TESTS`, 30) | the eight of GC-01's first part; SG12's fifteen (`everySpiritSealsOnceAndSpendsOnce` to `everySpiritsKeysSurviveASave`), each run for every offered spirit, Stolas and Buer; the five that need a second spirit (`aPerchOffersOnlyWhatThePlayerMayInvite`, `anInviteIndexOutOfRangeIsDropped`, `aNonOwnerNeverSeesTheChooser`, `aStaleChooserRevisionIsRefused`, `theRosterPortraitsEachPactBySpirit`); the review's `aVisitorOfAnotherSpiritLeavesWhenTheChosenCannotCome`; `ConcordanceJournalGameTests.theJournalNamesEachRegardsNextStep` |
| GC-02 (`GC02_TESTS`, 7) | `aSpiritArrivesFacingItsInviter`, `aSpiritFacesWhoeverSpeaksToIt`, `theLatestSpeakerIsFaced`, `aTeleportBackKeepsTheFacing`, `aClosedConversationLeavesTheFacing`, `theLookTargetIsDroppedOnlyWhenGoneElsewhereOrFar`, `aWorkTargetOutranksTheSpeakerWhileItsGesturePlays` |
| GC-03 (`GC03_TESTS`, 12) | six in `ConcordanceGardenGameTests`; `aGardenPactActsOnlyForABeneficiaryWhileItsHolderIsNear`, `talkingTeachesNothing`; `theGardensRegardIsReachable`; `theGardenPactsRecipesLoad`; `notesNeverStandInForTheGardensRegard`; `theGardenGroundworkSavesNothing` |
| GC-04 (`GC04_TESTS`, 24) | Buer's twenty (the specification's nineteen and the review's `buerSkipsBedsHeMayNotChange`); the three obligation tests; `buerFromTheCodexToDeparture` |
| Edited lines (GC-P17) | `thePactScreensMessagesAreBounded`, `ConcordancePactNetworkTests` (its `missing.owner` line), `thePactDefinitionParsesStrictly`; and every Chunk D test of Stolas's, unchanged |

**Client tests** (one client each except `ConcordancePactNetworkTests`, which runs two client JVMs; run 40605564166
attempt 1): `ConcordanceGardenPactClientGameTests` (new, weight 330) passed in shard 0; its 33 pictures were looked at
for this record and each is described in [Buer's Verification](buer-pact.md#verification), with what it does not show. `ConcordancePactClientGameTests` passed in
shard 3; its four facing pictures were looked at ([Stolas's Verification](stolas-pact.md#verification)).
`ConcordancePactNetworkTests` (two client JVMs at Stolas's perch, as Chunk D built it) passed in shard 1,
`ConcordanceRosterClientGameTests` (unchanged: no picture of Buer's portrait) in shard 6 and `ScreenFitClientGameTests`
in shard 15. The same classes passed in run 40597482976.

**Completion evidence for Buer** (the codes above; every test named passed in run 40605564166):

| Code | Buer (unit 1), as built |
|---|---|
| GE1 | `buerTendsOnlyBedsOfHisBeneficiaries`, `buerNeverTouchesAClaimedOrDormantBed`, `buerSkipsBedsHeMayNotChange`, `aGardenPactActsOnlyForABeneficiaryWhileItsHolderIsNear`, `anotherPlayerCannotAcceptForAnySpiritsHolder`, `everySpiritWaitsForANearHolder` |
| GE2 | `everySpiritsUpkeepWarnsGivesGraceSuspendsAndResumes`, `aStandInNeverAnswersAnySpiritsUpkeep`, `buerWaitsForAPresentHolder`, `buerTendsThroughHisGrace`, `everySpiritWaitsForANearHolder` |
| GE3 | `aCopiedPerchIsInertForEverySpirit`, `onePresencePerPactForEverySpirit`, `aDuplicateBuerTendsNothing`; he moves no item or nutrient (`buerEasesOneStrainAtTheBedsOwnRate`) |
| GE4 | `endingAnySpiritsPactReturnsTheEscrowOnce`, `aRemovedPresenceNeverEndsAnySpiritsPact`, `everySpiritsBrokenPerchLeavesThePactWithoutIt`, `everySpiritsPactIsSeatedAgain`, `everySpiritsKeysSurviveASave`, `anOverworkedComplaintIsForgottenOnRestart` |
| GE5 | `twoNegotiationsAtOnePerchSealOnceForEverySpirit`, `everySpiritIsOnePactAHolder`, `twoBuersShareOneBedsTendWindow` |
| GE6 | Worker, Journal, Progression, Journey, Resource (the A-08 line), Sharing (`notesNeverStandInForTheGardensRegard`; copies only by the Worker class's `aCopiedPerchIsInertForEverySpirit`), Persistence (`theGardenGroundworkSavesNothing`); Economy and Logistics: no new test; the client classes above. `ConcordanceGardenNetworkTests` is unit 7's |
| GE7 | `buerFromTheCodexToDeparture` (without the chooser: GC-P29); the client class from the specimen's tooltip to the ended pact |
| GE8 | one work, one service class, `check_garden_pacts`; the distinctness table needs a second garden service (unit 2) |
| GE9 | Buer's checklist: [his Verification](buer-pact.md#verification) |

**APIs the specification left unconfirmed** (critique 12, refreshed): every unit-1 class compiled in runs 40597482976 and
40605564166, so the compile-level ones hold (the 14-pair `StreamCodec.composite` with the 13-argument constructor, the
`AutoGlowingGeoLayer::new` inference, the `LinkedHashMap` fixture table, the type-use `PactMenu.@Nullable Answered`,
`Comparator.comparingInt(...).reversed()`). The tests that rest on the run-time ones passed:
`Language.getInstance().has` on the game-test server (`stolasLinesRenderAsBefore`), `Entity.setUUID` on an unadded entity
(`aDuplicateBuerTendsNothing`) and `load` on one (`anOverworkedComplaintIsForgottenOnRestart`, `everySpiritsKeysSurviveASave`), bone meal through
`useItemOn` (`anOverworkedBedWaitsForCompost`), `ServerChunkCache.removeEntity/addEntity` (the late watcher in
`ConcordancePactClientGameTests`), `time set midnight` and `waitForScreen(null)` after the level loads (the garden client
class). Still unconfirmed: the pinned SmartBrainLib jar's own sources (behaviour matches in the facing tests); Gutenberg's
record #72679 (GB18).

**Local checks for this records commit** (no Minecraft build here): `python3 scripts/check_repository.py` (structure and links), `python3 tools/concordance_journey.py
--check`, `python3 -B tools/check_mod_data.py` (its `check_delivery` holding the re-pasted delivery matrix) and
`git diff --check`, each run on this commit's tree and passed; `python3 tools/owner_art.py --provenance pacts` printed
Buer's provenance rows. The generators were not run: this commit changes no generated file, and the one generated block
it changes, the delivery matrix, was printed by `tools/concordance_delivery.py` and pasted. No Minecraft build, game test
or client test ran here.

**Not run:** a workflow_dispatch of every client class on unit 1's head (the specification's C5 asked for one; the pull
request's selection ran 31 of 274 classes); the opted-in `pact clients without optional integrations` job and the load
cards (skipped in pull request runs; the garden class without Jade and the garden load card are unit 7's); a second
client at Buer's perch; any person playing, alone or with others; any sound (CI has no sound device; subtitles are
checked as defined and fired).

#### Corrections to unit 1's commit messages

The commits are pushed and are not rewritten, so their messages are corrected here (the unit review's findings 10, 22
and 23, and `4ca363d38`'s from the records' review):

- **`202e75969` does not say that the generators' incidental rewrites were restored, and `9742f240b` says the generator
  "left src/main/resources unchanged" though the files it rewrote are there.** On the session's machine `tools/generate_material_data.py` rewrites 71 files under `src/main/resources` from CRLF to LF with no change
  of content and reorders `docs/features/jug-nouveau-content-map.json` with identical content, and `tools/owner_art.py`
  rewrites two ornamental corn textures with identical pixels (the logs count 72 to 74 rewritten files in all); each was
  restored with `git checkout` before a gate or check was read. So GC-01b's "git status listed only
  tools/concordance_pacts.py" held only after that restore, and GC-04a's "left src/main/resources unchanged" (its message
  does name the rewrites and their restore) means unchanged once the CRLF-only files it rewrote there were restored.
  The noise is not unit 1's: GC-01a's builder reported the same rewrites on the merged base `54a2acb6c` before any
  unit-1 edit (the saved baseline log ran only the audits, so this is the builder's report, not a log); it is recorded
  as the merge's gap.
- **`6101ad482`** is titled "GC-03: complete the groundwork" but adds no groundwork: it fixes two GC-03 server tests'
  setup (`aBedSaysWhetherItMayBeTendedWithoutChangingIt` grows its crop lit and sampled as the other harvest tests do;
  `theGardenGroundworkSavesNothing` lets the presence think once before its record is compared).
- **`3e736655a`** says "all 26 of the contract's audit mutations were caught locally". The mutations B1 to B26 are the
  Buer part design's, not this contract's, and the harness logged B26 as missed: its audit fired, with a different
  message than the harness expected (the unit review's re-run found the same).
- **`4ca363d38`** says the "optional integrations absent" job failed the three tests; in run 40597482976 the `mod` job
  failed the same three.

### Unit 2 as built and its evidence

Written on 28 October 2026 for unit 2's records commit from CI run 40777920181's job list and logs (read through GitHub's
REST API and its job-log tool) and from the commits' messages. "Passed" means the run's log says so. Nothing here was run
by a person, and one client is not two: the garden's two-client test is unit 7's. AI-assisted implementation with Claude
Code; the model is named in the commit trailers.

**Commits** (branch `claude/garden-cohort`; draft pull request [#526](https://github.com/jimbozoomer-byte/jugcraft/pull/526)
against main, not merged):

| Commit | What it holds |
|---|---|
| `28a963b63` | GC-05a: Bathin's brief, before any body file (D-D5) |
| `2e4f0acd2`, `c36b9758c` | GC-05b: the Heart's pour and the Gleaner's draw as shared bodies (`pourInto`, `receiveFrom`), their three garden tests and `check_verdance_roads`; the early slice's review fixes (a Javadoc, the brief's owner declaration) |
| `5499bf505` | GC-05b2: the bed index's devices (`GardenReach.devices`, `MAX_DEVICES`), `theGardenReachCapsItsDevices` |
| `08cb27921` | GC-05c: the accord machinery with no user yet (R26) |
| `2ad18f703` | the devices and accord review's fixes (Jade's answer line in the third person; `resolve`'s living-player rule in the audit) |
| `63cf1a83d` | GC-05: Bathin, Verdance carrying and his matter |
| `4fcf55513` | the unit review's fixes ([GC-05's As built](#gc-05-bathin-unit-2)) |
| `4d035fe64`, `7af654e9a` | whole moves from a beating Heart (GC-S25) and its review's fixes; his pose windows checked |
| `3f2bde5cf`, `85eb1a0ba` | two faults of the garden client test that CI found, fixed in the test (the roster's unbound key; the matter's controls a frame after its state) |
| `d7185041b` | his facing pictures framed whole (the camera aimed at the spirit's middle); no CI result recorded here |
| the merges | unit 1's line and main (#525, #528, #537, #541, #567) merged in, generated files regenerated, never hand-merged |
| this commit | the records |

**CI run** (the pull request run tests #526 merged into main's `a5803a77e`):

| Run | Event and commit | Result |
|---|---|---|
| [40777920181](https://github.com/jimbozoomer-byte/jugcraft/actions/runs/40777920181) | pull_request on `8af703330` (unit 2 whole, unit 3's shared prefix GC-06a and GC-06b, and the magic interfaces' unit 1) | Passed in full. `mod` and `optional integrations absent` each passed all 3,206 required server tests (each re-ran three of main's tests within the job, none of the cohort's). The selection picked 33 of 282 client classes; all 16 client shards passed: `ConcordanceGardenPactClientGameTests` in shard 0 (67 pictures), `ConcordancePactNetworkTests` in 1, `ConcordancePactClientGameTests` in 3, `ConcordanceRosterClientGameTests` in 6, `ScreenFitClientGameTests` in 15. The opted-in `pact clients without optional integrations` job and the load cards were skipped, as in every pull request run |

Unit 2's earlier pushes ran in CI too; their runs are not recorded here. As the fixing commits' messages say, the garden
client test failed on two faults of the test before this run: the roster's key has no default binding, so the test's
press to show his matter on the roster was refused (`3f2bde5cf`), and the matter step looked for the page's controls a
frame before the screen built them (`85eb1a0ba`).

**Server tests** (all passed in run 40777920181, with and without the optional integrations):

| Table (`tools/check_mod_data.py`) | Tests |
|---|---|
| `GC05_CARRYING_TESTS` (24) | `ConcordanceGardenGameTests`: `theGardenReachCapsItsDevices`, `aGleanerReceivesFromANamedHeartAtAnyDistance`, `aHeartPoursIntoANamedPylonByItsOwnRule`, `aHeartSaysWhetherItHasItsOwnRoad`; `ConcordanceWorkerGameTests`: `bathinMovesVerdanceExactly`, `bathinPoursIntoAFarPylonAtThreeForTwo`, `bathinPrefersAFarGleanerToAPylon`, `bathinChangesAtMostOneMoveAThink`, `bathinNeverHoldsVerdance`, `bathinHonoursTheHeartsParty`, `bathinNeverTouchesAClaimedReservoir`, `bathinLeavesAHeartWithItsOwnPylonUntilAnswered`, `aClaimAnswerLetsBathinTakeFromIt`, `bathinStopsAtHisQuota`, `bathinWaitsForAPresentHolder`, `aSuspendedBathinCarriesNothing`, `twoBathinsShareOneHeartExactly`, `bathinFacesWhereHeSetsItDown`, `bathinsFirstObligationIsADeviceReadByHand`, `bathinsReadingSaysItsNoteAndNamesNoPlayer`, `refusedGleanersNeverStarveBathinsPylonRoad`, `aFormerMembersHeartRaisesNoMatter`, `bathinCarriesWholeMovesFromABeatingHeart`; `ConcordanceEconomyGameTests`: `noVerdanceIsMintedByCarrying` |
| `GC05_ACCORD_TESTS` (9) | `theKeptPageAsksTheMatterAndKeepsTheAnswer`, `theRosterSaysAwaitingYourAnswer`, `anAnsweredAccordSurvivesARestart`, `anUnknownAccordReadsAsUnanswered`, `aResolveFromAnotherPlayerIsRefused`, `aResolveOutsideTheKeepModeIsDropped`, `anAccordIsClearedWhenItsPactEnds`, `theAccordWireIsBounded`; `ConcordancePersistenceGameTests.anAwaitingAnswerSurvivesARestart` |
| `GC05_BATHIN_TESTS` (5) | `bathinRefusesWithNothingToCarry`, `bathinArrivesAndDepartsAtOnceInARingOfDust`, `bathinsCarryShimmersBothEndsAtOnce`, `bathinAnswersEachActionWithHisGesture`, `aDuplicateBathinCarriesNothing` |
| SG12 and shared | the fifteen SG12 cases, `noGardenTermOrRefusalNamesAnAstrolabe` and `talkingTeachesNothing`, each run for every offered spirit, Bathin among them |
| Forced lines (GC-P59) | `everySpiritSealsOnceAndSpendsOnce` (through `sealThrough`), `everySpiritIsOnePactAHolder`, the two SG12 upkeep tests and `everySpiritWaitsForANearHolder` (each spirit's own offering count), `buerRefusesWithoutABedToMend`, `aPerchOffersOnlyWhatThePlayerMayInvite`, `anInviteIndexOutOfRangeIsDropped`, `aVisitorOfAnotherSpiritLeavesWhenTheChosenCannotCome`, `ConcordanceJourneyGameTests.buerFromTheCodexToDeparture` (through `chooseBuer`), `ConcordanceJournalGameTests.theJournalNamesEachRegardsNextStep`; every other unit-1 and Chunk D test unchanged by unit 2. *Corrected (unit 3):* the commit run 40777920181 tested (`8af703330`) already held GC-06b (`8cf0ef994`), which had rewritten `aPerchOffersOnlyWhatThePlayerMayInvite` and `theJournalNamesEachRegardsNextStep` generic over the shipped garden spirits and moved the second perch of `everySpiritIsOnePactAHolder` and `everySpiritsPactIsSeatedAgain` north of the first (GC-P77): those ran in GC-06b's generic form, not unit 2's |

Thirty-eight new server tests: the specification's thirty-five and three from the review and the follow-up
(`refusedGleanersNeverStarveBathinsPylonRoad`, `aFormerMembersHeartRaisesNoMatter`,
`bathinCarriesWholeMovesFromABeatingHeart`).

**Client tests** (one client each except `ConcordancePactNetworkTests`, which runs two client JVMs; run 40777920181):
`ConcordanceGardenPactClientGameTests` (weight 560) passed in shard 0; its 34 pictures of Bathin and his roster row were
extracted from the log, compared byte for byte with the log's previews, looked at for this record and each described in
[Bathin's Verification](bathin-pact.md#verification), with what it does not show (Buer's 33 in the same run were not
described again). Its log names each step's word budget, the chooser's three entries, one carry ("working 0 8 true"), the
roster's waiting row, Jade's readings and his gestures each frozen inside its window. The other four pact classes passed
as above.

**Completion evidence for Bathin** (the codes above; every test named passed in run 40777920181):

| Code | Bathin (unit 2), as built |
|---|---|
| GE1 | `bathinHonoursTheHeartsParty`, `bathinNeverTouchesAClaimedReservoir`, `aFormerMembersHeartRaisesNoMatter`, `aResolveFromAnotherPlayerIsRefused`, `bathinWaitsForAPresentHolder`, `anotherPlayerCannotAcceptForAnySpiritsHolder`, `everySpiritWaitsForANearHolder` |
| GE2 | `everySpiritsUpkeepWarnsGivesGraceSuspendsAndResumes`, `aStandInNeverAnswersAnySpiritsUpkeep` (each with his count of 4), `bathinWaitsForAPresentHolder`, `aSuspendedBathinCarriesNothing`, `everySpiritWaitsForANearHolder` |
| GE3 | `aCopiedPerchIsInertForEverySpirit`, `onePresencePerPactForEverySpirit`, `aDuplicateBathinCarriesNothing`, `bathinChangesAtMostOneMoveAThink`; a copy reads no answer (`anAnsweredAccordSurvivesARestart`) |
| GE4 | `bathinNeverHoldsVerdance` (a kill, an unload and a restart between thinks change no reservoir), `noVerdanceIsMintedByCarrying`, `endingAnySpiritsPactReturnsTheEscrowOnce`, `anAccordIsClearedWhenItsPactEnds`, `everySpiritsKeysSurviveASave`, `anAwaitingAnswerSurvivesARestart` |
| GE5 | `twoNegotiationsAtOnePerchSealOnceForEverySpirit`, `everySpiritIsOnePactAHolder`, `twoBathinsShareOneHeartExactly` |
| GE6 | Worker, Garden, Economy (`noVerdanceIsMintedByCarrying`), Persistence (`anAwaitingAnswerSurvivesARestart`), Journey and Journal (forced lines); Sharing, Logistics and Progression unchanged; the client classes above |
| GE7 | no whole-path server test of his (his journey steps 7 to 9 name the worker tests that hold them); the client class from the chooser to the ended pact |
| GE8 | `check_garden_pacts`' distinctness table with two garden services (`strain_tending`, `verdance_carrying`): one work, one service class, one state changed, distinct verbs |
| GE9 | Bathin's checklist: [his Verification](bathin-pact.md#verification) |

**APIs the specification left unconfirmed** (refreshed): every unit-2 class compiled in run 40777920181, so the
compile-level ones hold (`AnimatableManager<PactSpiritEntity<?>>` inferred in the `gestured` hook's controller, the
payload's nested `Spirit` with `matter` and `answers`, `PactRequestPayload.RESOLVE`). The tests that rest on the run-time
ones passed: `GameTestHelper.absoluteVec` and `Vec3` equality (`bathinArrivesAndDepartsAtOnceInARingOfDust`),
`Language.getInstance().has` on the server (`bathinRefusesWithNothingToCarry`), `Sign.SHOVE` and `Sign.WORK` counted by
`SignWatch` (`bathinArrivesAndDepartsAtOnceInARingOfDust`, `bathinsCarryShimmersBothEndsAtOnce`), `RosterClient.key()`
with a bound key and `pressKey` (the garden class), `TestClaims.open().block` and the party manager's invite, accept and leave
(`bathinNeverTouchesAClaimedReservoir`, `bathinHonoursTheHeartsParty`, `aFormerMembersHeartRaisesNoMatter`). Still unconfirmed: how the two signs look
to a person (the pictures show cloud puffs and white sparkles); any sound (no sound device).

**Local checks for this records commit** (no Minecraft build here): `python3 tools/owner_art.py --check`, `python3 -B
tools/check_mod_data.py`, `python3 scripts/check_repository.py`, `python3 tools/concordance_journey.py --check`,
`python3 tools/concordance_economy.py` ("gaining cycles: none"), `python3 tools/check_icon_maps.py`,
`node --test tools/world-designer/model.test.cjs`, `python3 -m unittest discover -s scripts/tests` and `git diff --check`,
each run on this commit's tree and passed; `python3 tools/owner_art.py --provenance pacts` printed Bathin's rows. This
commit changes no generated file. No Minecraft build, game test or client test ran here.

**Not run:** a workflow_dispatch of every client class on the cohort's head (the pull request's selection ran 33 of 282
classes); the opted-in `pact clients without optional integrations` job and the load cards; a second client at
Bathin's perch; any person playing, alone or with others; any sound; the pictures of `d7185041b`'s reframed facing
shots and of `73464e938`'s screens.

#### Corrections to unit 2's commit messages

The commits are pushed and are not rewritten, so their messages are corrected or completed here:

- **`63cf1a83d`** says he "arrives in a ring of dust"; as built the ring is the shared movement sign, a few puffs of cloud,
  as the same message's "His signs go through the shared Signs" implies (GC-05's As built). Its "all 54 of the C4 audit
  mutations were caught" holds for the commit as made: two (T13, an extra eye texel, and T14, a texel changed in the
  skin) were caught only after an audit was added and the mutation harness corrected, before the commit.
- **`4fcf55513`** leaves "small moves still spend his daily quota" for these records and the checkpoint; `4d035fe64`
  then chose whole moves (GC-S25), so that limit no longer holds.
- **`2ad18f703`** asks these records to list its review's deviations: unit 2's Jade-key check read `JADE_KEYS[-2:]` until
  the merge with GC-04 (it now reads `JADE_KEYS[12:14]`); `PactService`'s GC-05 members stand after `work()`; the
  docstring of `tools/concordance_pacts.py` names `spirits/PactAccords` since `63cf1a83d`; the device needles stand
  inside `check_garden_pacts`; `REACH["MAX_DEVICES"]` is set on its own line; and the devices commit is titled GC-05b2,
  since the early slice had taken GC-05b.

### Unit 3 as built and its evidence

Written on 29 October 2026 for unit 3's records commit, from the commits and their messages and from this session's own
local runs. **No CI run has compiled or run Marax:** his commits (`bed9f73b4` on) were made where no Minecraft jar was
available, every Java file of his was parse-checked only, and every server test and client shot of his is still to run.
"Passed" below means a local Python check, audit or harness passed. Nothing here was run by a person. AI-assisted
implementation with Claude Code; the model is named in the commit trailers.

**Commits** (the cohort's unit-3 line; not merged; `bed9f73b4`, `1473e99f1` and these records were not yet pushed to
draft pull request [#526](https://github.com/jimbozoomer-byte/jugcraft/pull/526) when this was written):

| Commit | What it holds |
|---|---|
| `736d94541` | GC-06a: Marax's brief, before any body file (D-D5) |
| `8cf0ef994` | GC-06b: the shared prefix units 4 to 6 branch from (the standing station, the synced work target and its renderer hook, a work's on-request choices on the kept page, `HabitatAdvice`, `GardenAsking`, the generic forced test lines, a marker line for each of units 3 to 6 at every shared append point) and its two server tests |
| `ee362c3cd`, `cc9db6afa` | the prefix review's fixes (the station's room read to the body's height, `afterSampling` one request a tick, the act list's lone closing line, SG12's copied perch inside the structure, a flight carried on from where it is drawn) and two hand-offs to Bathin's lines |
| the merges | unit 2's line, main (#537, #541, #567) and the magic interfaces' unit 1 merged in, generated files regenerated, never hand-merged |
| `bed9f73b4` | GC-06: Marax, the one-correction reading and the proud familiar |
| `1473e99f1` | his review's fixes (21 of its 22 findings; [GC-06's As built](#gc-06-marax-unit-3)) |
| this commit | the records |

**CI.** None for Marax. The head of run [40777920181](https://github.com/jimbozoomer-byte/jugcraft/actions/runs/40777920181)
(`8af703330`) held GC-06a, GC-06b and its review's fixes, and that run passed in full (both server jobs passed all 3,206
required tests; the garden client class passed with Buer's and Bathin's flows); GC-06b's two tests are tests of that
tree, and their bodies, `HabitatAdvice` and `PactSpiritEntity` are unchanged since, but their lines in its log were not
read for this record. Still to run: the `mod` and `optional integrations absent` jobs on a head holding these records;
the client classes `tools/select_client_tests.py` picks (the garden class among them, weight 820); then one
workflow_dispatch of every client class (unit 2's practice).

**Server tests written** (none run for Marax):

| Table (`tools/check_mod_data.py`) | Tests |
|---|---|
| `GC06B_TESTS` (2) | `ConcordanceWorkerGameTests.theFamiliarHookReachesItsTargetAtEveryBearing`; `ConcordanceGardenGameTests.habitatAdviceHoldsForEveryOrganism` |
| `GC06_TESTS` (20) | `ConcordanceWorkerGameTests`: `aReadingNamesOneCorrectionAndChangesNothing`, `aReadingNeverAdvisesDisturbanceToASunpetal`, `aReadingAdvisesAQuietSourceForAGloamcap`, `aReadingSkipsBedsWithoutACachedSample`, `aReadingWithTwoFaultsNamesTheFirst`, `onlyTheHolderMayAskForAReading`, `maraxNeverReadsAStrangersBed`, `theTargetIsNeverSaved`, `theProudFamiliarSkipsABedNamedThreeTimes`, `maraxRefusesWhereNothingGrows`, `maraxsFirstObligationIsABedReadByHand`, `maraxStandsBesideThePerchFacingHisInviter`, `aStandingSpiritFallsBackToThePerchWithNoFreeSide`, `theTargetClearsWhenTheConversationEnds`, `maraxsReadingSaysItsNoteAndNamesNoPlayer`, `maraxAnswersEachActionWithHisGesture`, `maraxNeverReadsABedHeMayNotChange`, `maraxReadsPastBedsHeMayNotChange`, `theProudCountStartsAgainWhenTheBedChanges`; `ConcordanceProgressionGameTests.theGardensRegardIsMasteredThroughThreeSpirits` |
| SG12 and shared | the fifteen SG12 cases through his fixture (a ripe Sunpetal on an awake bed of the player's beside the perch); `noGardenTermOrRefusalNamesAnAstrolabe` and `talkingTeachesNothing` for every offered spirit; `ConcordanceJourneyGameTests.theGardenPactsRecipesLoad` with Dewmoss in `GARDEN_RECIPES` |
| Forced lines (GC-P77) | made generic in GC-06b, before any unit-3 spirit shipped: `aPerchOffersOnlyWhatThePlayerMayInvite`, `ConcordanceJournalGameTests.theJournalNamesEachRegardsNextStep`, the second perch of `everySpiritIsOnePactAHolder` and `everySpiritsPactIsSeatedAgain` moved north (these ran in run 40777920181, as [unit 2's evidence](#unit-2-as-built-and-its-evidence) corrects); with Marax shipped they read three garden offers (four chooser entries with Stolas's), untested |

Twenty-two new server tests: the contract's twelve, seven the specification added (`maraxStandsBesideThePerchFacingHisInviter`,
`aStandingSpiritFallsBackToThePerchWithNoFreeSide`, `theFamiliarHookReachesItsTargetAtEveryBearing`,
`theTargetClearsWhenTheConversationEnds`, `maraxsReadingSaysItsNoteAndNamesNoPlayer`,
`maraxAnswersEachActionWithHisGesture`, `habitatAdviceHoldsForEveryOrganism`) and three from his review.

**Client test written** (not run): `ConcordanceGardenPactClientGameTests`, his flow after Bathin's at the same perch, 33
shots ([his record's Verification](marax-pact.md#verification) names them), each held to its word budget.

**The advice harness** (scratch, not in Git), re-run for this record against `HabitatAdvice` and the ecology classes as
committed: 30,604,024 checks, 0 failures ([the rule](marax-pact.md#the-rule-gc-p64)).

**Completion evidence for Marax** (the codes above; **none of these tests has run**):

| Code | Marax (unit 3), as built |
|---|---|
| GE1 | `onlyTheHolderMayAskForAReading` (a stranger, a party member and a stand-in refused), `maraxNeverReadsAStrangersBed`, `maraxNeverReadsABedHeMayNotChange`, `maraxReadsPastBedsHeMayNotChange`, `anotherPlayerCannotAcceptForAnySpiritsHolder`, `everySpiritWaitsForANearHolder` |
| GE2 | `everySpiritsUpkeepWarnsGivesGraceSuspendsAndResumes`, `aStandInNeverAnswersAnySpiritsUpkeep` (each with his count of 2), `everySpiritWaitsForANearHolder`; a suspended or held pact refuses a reading (`HabitatReading.read`'s refusals) |
| GE3 | `aCopiedPerchIsInertForEverySpirit`, `onePresencePerPactForEverySpirit`; nothing to copy (a reading changes nothing: `aReadingNamesOneCorrectionAndChangesNothing`) |
| GE4 | `aReadingNamesOneCorrectionAndChangesNothing` (no bed changed, no sample taken), `theTargetIsNeverSaved` (the target and the pique unsaved), `endingAnySpiritsPactReturnsTheEscrowOnce`, `everySpiritsKeysSurviveASave` |
| GE5 | `twoNegotiationsAtOnePerchSealOnceForEverySpirit`, `everySpiritIsOnePactAHolder`; holder only, so no two players share his readings |
| GE6 | Worker, Garden (`habitatAdviceHoldsForEveryOrganism`), Progression (`theGardensRegardIsMasteredThroughThreeSpirits`), Journey (`theGardenPactsRecipesLoad`); Sharing, Logistics, Economy and Persistence unchanged; the garden client class |
| GE7 | no whole-path server test of his (his journey steps 10 to 13 name the tests that hold them; unit 7's `theGardenPactsFromCodexToDeparture` walks them); the client class from the chooser to the ended pact |
| GE8 | `check_garden_pacts`' distinctness table with three garden services (`strain_tending`, `verdance_carrying`, `habitat_reading`, "read and advise", changing nothing): one work, one service class, one call, distinct verbs |
| GE9 | Marax's checklist: [his Verification](marax-pact.md#verification) |

**Local checks and audit mutations, by commit** (each commit's message): GC-06b, 23 mutations each caught; its review's
fixes, 15 caught; GC-06, the specification's X1 to X29 caught, X30 left to CI as specified
(`habitatAdviceHoldsForEveryOrganism`, `aReadingWithTwoFaultsNamesTheFirst`), and twelve more caught (X31 to X42); his
review's fixes, 19 of 21 caught, the chooser's budget formula (held only by the client test that uses it) and the
reading's full stop held by no audit. Each commit's tree passed `tools/owner_art.py --check`, `tools/check_mod_data.py`,
`scripts/check_repository.py`, `tools/concordance_journey.py --check`, `tools/concordance_economy.py` ("gaining cycles:
none"), `tools/check_icon_maps.py`, the world designer's node tests, `scripts/tests` and `git diff --check`, and each
edited Java file the parse check. **For this records commit:** the same checks on its tree, each passed; no generated
file changes; its documentation mutations are in its message.

**APIs not confirmed against a build** (each checked only against the repository's own uses, the 26.3 sources and
GeckoLib 5.5.7): `Map.ofEntries` with `Map.entry` inferred as `Map<String, String>` (`PactGlance.WORK_ICONS`); the
`MagicButton(Look, Component, Component, OnPress)` constructor from `PactScreen`; `ServerLevel.getOverworldClockTime()`
from the client test; `attendWork(pos, 0, now)` ending his work facing at once; whether 26.3's
`PlayerBlockBreakEvents.BEFORE` claim (`TestClaims`) stops `Authority.mayChange` for the claimed beds of his two claim
tests, as it does in Buer's; and, for the client, the words actually drawn ("Nutrients 0 → 1"), the client under `tick
freeze` while the window is resized, `isDarkOutside` after `time set` (the night look), the test structure's light and
sky, `PactSessions.live`'s timing that clears the target, and the framing of his four facing pictures.

**Not run:** everything of Marax in CI (compilation, the server jobs, the garden client class, a dispatch of every client
class); a second client at his perch; any person, alone or with others; any sound.

#### Corrections to unit 3's commit messages

The commits are not rewritten, so their messages are corrected or completed here:

- **`736d94541`** names the gaia sounds' catalog rows `files.csv:20370` to `20373`; main's #537, merged into this line
  since, added seven rows above them, so they are `20377` to `20380` (his record's table is corrected).
- **`bed9f73b4`** says his familiar "reads up to eight of your planted Verdant Beds in his area" and passes over a bed
  named "three readings running, with nothing changed"; as committed, a bed `GardenPacts.target` refused used one of the
  eight, and a bed that changed between readings and fell back to the same verdict still counted toward three.
  `1473e99f1` fixed both (its items 16 and 3).
- **`1473e99f1`**'s Decisions rows GC-P63 to GC-P79 carry 27 October 2026, the date of the unit's specification; they
  were added to this page on 29 October.

## Schema review (the shared schema against eight services)

| Concern | The six garden services | For the shared schema |
|---|---|---|
| Identity, place, terms on the record | unchanged `BoundWill` and `Pact`; work ids appended | Shared, unchanged |
| Area subject (S2) | the target block's place (bed, Heart, Gleaner, pylon, container) for five; the bed's for the canopy | S2 holds: the work says what the area covers |
| Beneficiaries (S3) | a target's keeper (or owner) checked against `personal`/`party` at each change | S3 holds; proposed **S7**: a service acts on a block only for a beneficiary keeper, while the holder is present within the reach upkeep uses, and where that holder may change it, checked at each change |
| Claims and property in flight (S1) | none: every change is one server step; Foras's donor through one transaction | S1 holds |
| Bindings to other places (S4) | Foras's store is adjacency, read at each use; the accord is perch block data (unit 2) | S4 holds |
| Gates (S5) | holds and hours stop new work; nothing is in progress between thinks | S5 holds |
| Death and unload (S6) | never end a pact; a presence's memory (eases, the familiar's pique) is lost, never owed | S6 holds |
| World modifiers | the canopy changes a derived reading only while serving | proposed **S8**: a service's effect on world rules is derived, never stored, and lapses within one think of the pact ceasing to serve |
| Job adapter (03:191; CD:2113) | none needed: no reservation, path, property over time or multi-step task | **GC-S15**: not built; due with the first service that holds property over time |

The cohort uses S1 to S6 unchanged and proposes S7 and S8 for the freeze; it does not mark the schema frozen (the owner's
call at a checkpoint, 03:177).

## Interfaces expected to change (summary)

- **Pact system (unit 1):** new `PactService`, `PactServices`, `PactSpiritEntity`, `PactSpirits`, `PactRegards`,
  `PactObligations` (`TEND`), `GardenReach` (beds), `GardenPacts`; edited `StolasEntity` (split), `StarReckoning` (a
  service), `PactPerchBlockEntity`, `PactMenu` (the chooser, resonance-free wording), `PactKeeping`, `PactReading`,
  `PactStatePayload` (+one nested field `spirit`: `sign`, `work`), `PactRequestPayload.ACTIONS` (+`invite` 9), modes
  (+`invite` 4), `PactParser.REQUIRED`, `Roster`, `Journal`, `WorkerEntity` (`attend`; the `LookAtTarget` predicate),
  `PactSessions` (`speaker`), `Workers`, `WorkerDataProvider`, `PactScreen`, `WorkerClient` (+`PactSpiritRenderers`),
  `Examination` and `ConcordanceClient` (the Regard's specimens).
- **Pact system (unit 2, with Bathin):** `PactService` (+`complication`, `accords`), `PactRequestPayload.ACTIONS`
  (+`resolve` 10), `PactPerchBlockEntity` (+`accord`), `PactStatePayload.Spirit` (+`matter`, `answers`), `PactKeeping`
  (the matter row), `W/Status` (+`AWAITING_ANSWER`) and `Roster`, `GardenReach` (+Hearts, Gleaners, pylons),
  `PactObligations` (+`DEVICE`).
- **Pact system (unit 3, with Marax):** `PactSpiritEntity` (+`TARGET`, synced, never saved), `PactSpiritRenderers` (+a
  bone hook and a widened culling box). Units 4 to 6: `GardenReach` (+containers, unit 4); `PactObligations` (+`GRAFT`,
  `FEED`, `HARVEST`).
- **Garden and ecology:** `VerdantBedBlockEntity` (`mayTendAt`, `cachedHabitat`; unit 6: the canopy in `habitat` and
  `cachedHabitat`), `VerdantBedBlock` (`TEND` unit 1, `READ` unit 3, `FEED` unit 5), `LivingDeviceBlock` (`DEVICE`,
  unit 2), `OrganismCropBlock` (unit 4: `Grafting` extracted and `GRAFT`; unit 6: `HARVEST`), `GleanerBlockEntity.receiveFrom`
  and `VerdantHeartBlockEntity.pourInto` (unit 2), new `E/HabitatAdvice` (unit 3, pure), `E/Habitat` (+`shelter`,
  +`sheltered`; unit 6, pure), new `G/Canopies` (unit 6), new `G/Grafting` (unit 4).
- **Per spirit:** `<Spirit>Entity` (thin) and its service class; data, tags, sounds, lang, body files.
- **Tools:** `tools/concordance_pacts.py` (`SPIRITS`, resonance-free and per-work quota keys), new
  `tools/concordance_garden_pacts.py`, new `tools/garden_bodies.py`, `tools/owner_art.py` (the cohort's table, unit 1;
  the second edited part's root, unit 4 or 6), `tools/concordance.py`, `tools/concordance_roster.py`,
  `tools/concordance_progression.py` (the garden practice's `distinct`), `tools/concordance_journey.py`,
  `tools/acquisition.py`, `tools/concordance_economy.py` (unit 5), `tools/select_client_tests.py`,
  `tools/check_mod_data.py` (`check_pacts`, `check_pact_screen`, `check_spirit_bodies`, `check_geckolib`, `check_roster`,
  `check_workers`, `check_garden_pacts`, `check_garden_load_card`, `check_pact_network`, `check_acquisition`,
  `check_economy`).
- **Workflow (unit 7, additive and opted in, for the owner's review):** a third `runGameTest` in the `load-card` job; two
  classes added to `clients_without_optional`'s list. No new job, no dependency, no pin change.

### Saved data and stable ids

| Kind | Added | Unchanged |
|---|---|---|
| Saved data | the perch's optional `accord` (string of at most 16 characters; stamp 1; unit 2) | `jugcraft:bound_wills` v1, `jugcraft:pact_history` v1, `jugcraft:worker_roster` (kind `pact` for every pact), the perch's `owner`, `agreement`, `spirit`; Stolas's `perch`, `record` (the base writes the same keys for every spirit) |
| Saved strings | work ids `strain_tending`, `verdance_carrying`, `habitat_reading`, `graft_keeping`, `root_running`, `canopy_shelter` (on Bound Wills and the perch's `agreement.work`; append only); accord values per service; the roster status `awaiting_answer` (`W/Status`, appended, unit 2) | `star_reckoning`; every existing status id |
| Entities | `jugcraft:buer`, `jugcraft:bathin`, `jugcraft:marax`, `jugcraft:foras`, `jugcraft:bifrons`, `jugcraft:amdusias` | `jugcraft:stolas`, `jugcraft:workshop_hand` |
| Agreements | `jugcraft:<spirit>_pact` (six) | `jugcraft:stolas_pact` |
| Data | `concordance/pact/<spirit>.json` (six); research `jugcraft:garden_regard`; practice `jugcraft:garden_pact_service`; tags `#jugcraft:<spirit>_offerings` (six), `#jugcraft:garden_regard_specimens`; advancements `concordance_garden_regard_*` (four); codex `binding/garden_regard` | `stolas_regard`, `pact_service`, Stolas's tags |
| Lang | the spirits' keys; `term.compact.items`, `answer.cannot_afford.items`, `term.quota.canopy_shelter`; the Regard's examine line and tooltip | every existing key (values of the 36 generic keys gain `%s`; none moved or renamed) |
| Sound events | `jugcraft:<spirit>.{arrive,listen,inspect,assent,refuse,depart,work}` (42) | `jugcraft:stolas.*` |
| Wire | action `invite` (9, unit 1), `resolve` (10, unit 2); mode `invite` (4); one nested payload field `spirit` (`sign`, `work` in unit 1; `matter`, `answers` in unit 2); the synced entity data `TARGET` (unit 3, never saved) | payload type ids; the payload's other 13 fields |

Never renamed after release; a spirit's entity lang is never removed (history names resolve from it); disabling the
Concordance keeps every registration and record (SP:578-579).

## Dependencies and order

1. GC-00 (this contract); merge main (#475) down the stack (GC-S16).
2. Unit 1: GC-01's compile-only slice first (SG1 to SG3, Stolas a thin subclass; CI green), then the rest of GC-01, then
   GC-02 (needs the base), then GC-03, then GC-04 (needs all three). Buer's body may be built beside GC-01 once the
   `SPIRITS` table exists.
3. Unit 2 after unit 1: it brings the accord machinery, the roster's waiting status and the bed index's devices.
4. Units 3 to 6 after unit 2: each needs only unit 2's head; they may run in parallel with their reserved GC-P blocks and
   merge in order 3, 4, 5, 6 (each merge re-runs SG12 for every definition). Unit 3 needs `cachedHabitat` (GC-03) and
   brings the synced target (no other unit uses it); unit 4 or unit 6, whichever lands first, adds the second edited
   part's root to `tools/owner_art.py`; unit 6 changes `habitat` and goes last. Mastery of the Regard becomes reachable
   with the first of these to merge.
5. Unit 7: after unit 6.

The smallest first step is GC-01's compile-only slice, pushed for CI; then the `SPIRITS` table with Stolas alone
reproducing today's generated data, sounds, codex and assets, with every Stolas test unchanged.

## Risks

- **The stack** (until 27 October 2026): four unmerged stacked pull requests plus main's #475; every review change below
  was a merge, and #475 touched the same hot files. Since #494 brought Chunks B to D to main, the cohort merges main
  directly (`cfeded41e`, `10566d074`); main still moves the hot files (#524 and #525 did), and generated files are
  regenerated after each merge, never hand-merged.
- **Stolas regression:** extracting the base from a 525-line class while every Stolas test and generated file stays
  unchanged; mitigated by the compile-only first push, the byte-for-byte generator check (data, sounds, codex, assets),
  `stolasLinesRenderAsBefore` and the unchanged tests.
- **CI-only build:** there is no Minecraft jar or Loom cache in the session's environment, so every compile error and
  game-test failure shows only in CI, a run at a time. Mitigated by the compile-only first push, by checking every
  vanilla, GeckoLib and SmartBrainLib call against the reference sources before it is written (the payload's field limit
  and `LookAtTarget`'s default target test were both found that way), and by the local generators and audits.
- **Source claims:** every quotation is search-checked at best ([S]) and some only [s] or [U]; a quote in game before a
  scan check would risk a fabricated claim. Amdusias's music line and Marax's joining words are unverified. Three
  universal claims rest on the surveyor's recollection of the other 61 entries and are labelled [U]: that seven of the
  72 are plant-linked, that Buer's is the only entry naming plants, and that Amdusias's is the only tree-shaping power.
  Bifrons's grounding ("Geometry" and "Woods") and Amdusias's music-derived manner, greeting and late horn are labelled
  Jugcraft adaptations, to be revised if a scan disagrees.
- **Briefs not approved by the owner:** 03:165 speaks of an approved character brief; D-D5 asks only that each is briefed
  before its art. Under GC-S the briefs here are built without approval and the checkpoint's first question covers them.
- **Silhouettes:** two quadrupeds (Marax, Amdusias) and two bipeds (Foras, Bifrons risen) must still read apart; the
  bounding-box audit and the line-up shot test it; only Buer uses an owner rig, against the owner's preference for
  library assets (the checkpoint's first question).
- **A perch for a bull:** the "pact perch" block and name serve every spirit; standing bodies stand beside it.
- **Load:** the canopy is consulted on every habitat build (random ticks of every crop); the bed index scans chunk maps;
  readings iterate up to 8 beds. Measured by the card; the canopy registry's index by chunk keeps a lookup to the few
  canopies near.
- **Foras nearly idle in steady state:** `GRAFT_CAP` is 1, and a graft survives replanting of its host (it is cleared only
  when another organism grows, `G/VerdantBedBlockEntity.java:475-480`), so after his first pass he grafts almost nothing
  while his weekly honeycomb continues. Recorded for the owner (checkpoint question 2); more grafts are deferred.
- **Garden balance:** Buer automates the Greenwardens' first satisfying action (02:39), kept purposeful by the overworked
  bed and his by-hand obligation; Bathin relaxes the Heart's adjacency layout (bounded by quota and the accord's default);
  Amdusias makes "garden beside workshop" possible ahead of the planned season shelter and the Rimekeepers' conservatory
  (only for unstrained crops that need no stir: the strained judgement keeps the undamped value);
  Marax takes Stolas's deferred botanical topic. All for the owner after play (checkpoint question 2).
- **`tendedAt` unsaved (GB14):** after a restart Buer may tend a bed once early; bounded by his quota; not changed.
- **Peepo:** Peepo's harvests strain beds as any harvest does, so Buer's overworked complaint may come often in a
  Peepo garden; no Peepo file is edited.
- **Roster status:** `awaiting_answer` is a new saved string in `jugcraft:worker_roster`; an older build reading a roster
  with it would not know the id (no downgrade path for new keys, Deferrals).
- **Caps:** 32 Bound Wills a holder and 32 roster places (shared with familiars, Nouveau helpers and Starbuncles); seven
  pacts fit; a full roster refuses a seal first. Codex places in the Binding category (one entry for six spirits);
  journal sections unchanged (one pacts section).
- **Upkeep chores:** six more weekly offerings for a player who keeps all six; each is renewable and Gentle.
- **Practice side effects:** the Conclave hears every recorded practice; `garden_pact_service` is once a player a spirit.
- **Client CI cost:** a new client class and a changed shared helper widen the selection; `WEIGHTS` entries keep shards
  balanced.
- **Workflow edits:** additive and opted in, but still workflow changes for the owner's review.
- **Every D-S and D-P below is unanswered:** the cohort builds on Chunk D's proposals as built.

## Explicit don'ts

1. Do not copy Stolas's classes, perch, menu, keeping page or ledger per spirit; extend the shared ones (SG1 to SG12).
2. No scripting language, rule interpreter or data-driven behaviour engine for services: each service is a small Java
   class behind one interface (03:177).
3. Do not build a second UUID system, ledger, roster, courier ledger, worker base, permission model or currency; no
   "garden favour", tally or token.
4. No stand-in answers for a garden service or for upkeep; no offline punishment, debt or owing time away.
5. Do not make combat the way a pact ends; killing a spirit is never the fastest route; spirits take no ordinary damage.
6. No research for talking or for a spirit's work; no cultivation practice from a spirit; notes never stand in.
7. Do not record `worker_service` or Stolas's `pact_service` for a garden spirit; no fourth worker kind.
8. No chat commands, vanilla Dialog or mixin as the protocol; requests carry bounded indexes only (no totals, player ids,
   positions or free text); no menu slots.
9. No item in transit in a spirit's body; no Verdance or nutrients held between thinks.
10. Do not set nutrients, ages or strain directly; do not force growth; do not sample beyond the level's budget; do not
    use `Garden.nearest` or `Garden.sample` from a spirit; no chunk loading, no dimension crossing, no walking; no
    living-entities sensor added for facing.
11. Do not plant, harvest, stock the Maw, empty the Gleaner, water, or fertilize ordinary crops (Peepo's and the devices'
    work); no edits under `src/main/java/local/peepo`.
12. Do not add a conversion except Bifrons's (2 to 1); do not fix a loop with a cooldown; pact items stay out of
    equivalence (none is added).
13. Do not edit Styx or his blueprints; no garden spirit in his district (the perch's rule).
14. Do not execute or bulk-import from the owner library; keep the originals; no registered creature's identity; no
    modern adaptation's design; no tracing from the *Dictionnaire Infernal*.
15. No seals, conjurations, invocations, "Hell" titles, corpses, graves, pentagrams or real medicine; no source quotation
    in game before a scan check.
16. Do not hand-edit generated data, lang or `sounds.json`.
17. Do not rename or remove any id Chunk D shipped; append to every sent vocabulary.
18. Do not mark a GC-S or GC-P decision as the owner's, count one client as two or several test players as a party of
    people, or call the cohort complete because tests compile.
19. No new dependency, pin change or workflow change beyond the two additive, opted-in steps named here.
20. No stereo cue and no downmixed copy: each cue is an existing mono runtime file, its channels checked here.
21. Do not move or rename a generic pact lang key to reword it; do not add a fifteenth top-level field to
    `PactStatePayload`; do not mutate the world inside an open Fabric transaction.

## Deferrals

- **What The Garden's Regard's mastery unlocks** (nothing in this cohort).
- **The plan's season shelter structure** (03:75) and the Rimekeepers' conservatory; Amdusias's canopy is neither.
- **The job-adapter interface** (03:191): with the first service that holds property over time.
- **Beneficiaries beyond the party** (a shared or community garden).
- **More grafts** (`GRAFT_CAP` 1, one graft today), graft cuttings and harvest choice (CC deferrals).
- **The alternates' services:** the stall watch, the crow watch (as a detail), the bone-meal round.
- **Bees stepping organism crops outside `grow`** (ECO; the owner's choice).
- **The Siren and Flarecannon rigs; registered rigs as spirit bodies** (checkpoint question 1).
- **A wood or orchard service for Bifrons** built on "Woods", if the owner prefers it to root-running (checkpoint
  question 1).
- **A head turn ahead of the body** (an additive bone rotation in the renderer).
- **Walking spirits.**
- **An encyclopedia Pacts page; voice lines; translations; a downgrade path for new keys.**
- **Stolas's mineral comparison** (to the minerals cohort); Stolas's other changes beyond facing.
- **A terms page for worker pacts** (the hand), untouched here.
- **The other 65 spirits** (eleven more cohorts), each after its own source check and briefs.

## Validation plan

| Unit | Server game tests (CI) | Client game tests (CI) | Checks (local) | Save tests |
|---|---|---|---|---|
| 1 | Worker (Stolas unchanged; SG12 for Stolas and Buer, with `onePresencePerPact` and `...WaitsForANearHolder`; facing with the `LookAtTarget` predicate; chooser), Sharing (copies), Journal, Progression (to understood), Journey (`buerFromTheCodexToDeparture`), Economy (unchanged) | `ConcordancePactClientGameTests` (chooser, facing shots), new `ConcordanceGardenPactClientGameTests` (Buer, his plate and cap from below), `ConcordanceRosterClientGameTests` (portraits), `ScreenFitClientGameTests` | generators: data, sounds, codex and assets byte for byte with Stolas alone; `check_pacts` (14 top-level payload fields), `check_pact_screen`, `check_spirit_bodies` (twin faces), `check_geckolib` per-face, `check_workers` (the predicate), `check_garden_pacts`, `check_roster`, `check_acquisition`, `owner_art --check` (the cohort's table), `concordance_journey --check`, client-test selection | Buer's entity keys; Stolas's keys unchanged |
| 2 | Worker (Bathin; the accord cases; `DEVICE`), Economy (no Verdance minted), Persistence (`accord`, `awaiting_answer`), Sharing | Bathin's poses and shimmer; the accord row; the roster's "awaiting your answer" | `check_garden_pacts`, `check_pacts` (`resolve`, `accord`), `check_roster`, bodies, economy | the perch's `accord`; a restart between thinks moves nothing |
| 3 | Worker (Marax), the `HabitatAdvice` harness (first fault), Progression (mastered through three spirits) | a reading with the familiar at the bed | the pure-package list; bodies; `TARGET` never saved | none new |
| 4 | Worker (Foras), the existing graft tests unchanged | Foras's graft gesture | bodies; `owner_art --check` (acorn, the second edited part) | a store across a restart |
| 5 | Worker (Bifrons), Economy (`rootRunningLeavesNoGainingCycle`) | his two shapes and his measuring marks | `tools/concordance_economy.py`, `check_economy` | none new |
| 6 | Worker (Amdusias: strain, stacking, Heart, cap wording), the habitat harness unchanged without a canopy and the `Habitat.sheltered` cases, Inspect and Gauge | the late horn and the tree | bodies; `owner_art --check` (ring); `check_baselines` (`ecology` pure) | a canopy never saved |
| 7 | the whole path; the load card (opted in) | the line-up, every spirit's evidence; `ConcordanceGardenNetworkTests`; the opted-in run without optional integrations | every check; `check_garden_load_card`, `check_pact_network` | none |

Every unit also runs `tools/check_mod_data.py`, `scripts/check_repository.py`, `scripts/tests` and the generators (output
current) locally, and the build with its server game tests in CI (the cohort pull request's run), then CI by
workflow_dispatch with every client class. Evidence rules (10:5-22): each command reported as run and passed, run and
failed, or not run; CI counts only for its commit; spending tests use `TestPlayers.survivor`; a screenshot counts once
someone has looked at it ("Seen" column); generator-model numbers stay labelled so until a test measures them; nothing is
called balanced without a human playtest.

**Unit 1 as run** (row 1 above against the build): Worker, Journal, Progression, Journey, Resource, Sharing and
Persistence ran in CI (run 40605564166); Sharing's "copies" are held only by the Worker class's
`aCopiedPerchIsInertForEverySpirit` (Sharing's own new test is `notesNeverStandInForTheGardensRegard`); Economy gained no
test. Of the client classes, `ConcordanceGardenPactClientGameTests` and `ConcordancePactClientGameTests` (facing; the
chooser moved to the garden class, GC-P18) ran and passed; `ConcordanceRosterClientGameTests` and
`ScreenFitClientGameTests` ran unchanged and passed, so there is no picture of Buer's roster portrait. The save tests are
`everySpiritsKeysSurviveASave` (both spirits) and `theGardenGroundworkSavesNothing`. The row's local checks were run on each commit
by the unit's builders and reported passing (in the commit messages that list them and the session's working notes), with
the generators' incidental rewrites restored ([Corrections](#corrections-to-unit-1s-commit-messages)).

**Unit 2 as run** (row 2 above against the build; run 40777920181): Worker (Bathin's carrying, character and accord
tests, `DEVICE`, SG12 for all three spirits), Garden (the named roads and the device cap), Economy
(`noVerdanceIsMintedByCarrying`), Persistence and the Journal and Journey tests with their forced lines ran and passed
in both server jobs. The accord's save cases are in `ConcordancePersistenceGameTests` (`anAwaitingAnswerSurvivesARestart`)
and `ConcordanceWorkerGameTests`; the copy case is in `anAnsweredAccordSurvivesARestart`; `ConcordanceSharingGameTests` is
unchanged. Of the client classes, `ConcordanceGardenPactClientGameTests` (Bathin's poses, shimmer, matter and the
roster's "awaiting your answer") passed, and `ConcordancePactClientGameTests`, `ConcordancePactNetworkTests`,
`ConcordanceRosterClientGameTests` and `ScreenFitClientGameTests` passed with the restyled screens. The row's local
checks were run on each commit by the unit's builders and reported passing in the commit messages that list them.

## Decisions

The owner's (GC-D), the session's under the standing instruction (GC-S, "Decided by the session"), and the units'
proposals (GC-P, numbered in the reserved blocks). Chunk A to D decisions are unchanged and keep their status.

| ID | Date | Decision | Status | Affects |
|---|---|---|---|---|
| GC-D1 | 26 October 2026 | "keep stolas" (D-D3): Stolas's look and manner are kept as built (the raven adapted from the Flashjack rig, dry and patient, five gestures, sounds and subtitles, no voice). Nothing else: facing and later spirits' silhouettes were not answered separately (CD:2780; GC-S23) | Accepted (owner) | GC-01 (Stolas unchanged) |
| GC-D2 | 26 October 2026 | "do D-P50" (D-D4): a warning given before an absence keeps only the grace left; suspension still pauses the upkeep clock; already built | Accepted (owner) | every garden pact's upkeep |
| GC-D3 | 26 October 2026 | "gardens next" (D-D5): the next six-spirit cohort is gardens, the first of 03:165's cohorts, built as Chunk D's checkpoint proposed it: one spirit a unit on the shared pact record, each briefed before its art, each service mechanically distinct. Where it is developed is GC-S16 | Accepted (owner) | the cohort |
| GC-S1 | 26 October 2026 | The six are Buer, Bathin, Marax, Foras, Bifrons and Amdusias (the 1904 text's plant-linked spirits besides Stolas, the other 61 entries' silence being the surveyor's recollection [U]); alternates Decarabia, then Barbatos; 1904 headings as names (Marax, not Morax), other spellings recorded | Decided by the session | dossiers |
| GC-S2 | 26 October 2026 | Services: Buer strain tending, Bathin Verdance carrying, Marax a one-correction reading through his familiar, Foras graft keeping, Bifrons root-running, Amdusias the bending canopy; each on the spirit's distinct source trait (Bifrons's a Jugcraft adaptation of "Geometry" and "Woods") | Decided by the session | GC-04 to GC-09 |
| GC-S3 | 26 October 2026 | Stolas's deferred botanical interpretation becomes Marax's familiar's reading; Stolas keeps Star-reckoning; mineral comparison waits for the minerals cohort | Decided by the session | GC-06 |
| GC-S4 | 26 October 2026 | One perch offers every pact through a chooser at an empty perch, for its owner only (appended action and mode; `INVITE` only in the `invite` mode; choosing again means declining and reopening); no new holder block | Decided by the session | GC-01 |
| GC-S5 | 26 October 2026 | The shared generalization SG1 to SG12; Stolas keeps his class, ids, keys, rendered words and tests; generic lang keys gain `%s` and never move; no scripting language | Decided by the session | GC-01 |
| GC-S6 | 26 October 2026 | Facing: summoned facing the inviter, attending the latest speaker, facing its work; server only, through `LookAtTarget` with an explicit target test (alive, same level, within 32 blocks), no new sensor; Chunk D's south camera (D-P133) moves to the speaker's side | Decided by the session | GC-02 |
| GC-S7 | 26 October 2026 | Garden services act only for a beneficiary keeper, with the holder present within `Pacts.REACH` of the perch (`Authority.presentNear`, the test upkeep uses) and able to change the block, checked at each change, after the base's `decide` returned `IDLE` (refined by GC-P27: or `UPKEEP_DUE` with the holder near); never a stand-in; research checked at invitation and sealing | Decided by the session | GC-03 to GC-09 |
| GC-S8 | 26 October 2026 | Garden spirits keep their perch and work at a distance in their area; no walking or paths | Decided by the session | every spirit |
| GC-S9 | 26 October 2026 | One cohort research, The Garden's Regard, with one practice `garden_pact_service` keyed by spirit (mastery: three spirits, reachable from unit 3); each spirit's own by-hand first obligation; Stolas's Regard and practice untouched | Decided by the session | GC-03 |
| GC-S10 | 26 October 2026 | Compacts of ordinary materials, no resonance, worded without an astrolabe; upkeep the Gentle shape with each spirit's own offering | Decided by the session | every spirit |
| GC-S11 | 26 October 2026 | Conflicts: Buer and Marax by a task (derived, unsaved memory; a service row, not a roster status); Bathin, Foras, Bifrons, Amdusias by a choice kept as the perch's optional `accord`, the safe answer until then, the roster saying "awaiting your answer" through an appended `Status.AWAITING_ANSWER`; one appended `resolve` action; the machinery lands with Bathin (unit 2) | Decided by the session | GC-04 to GC-09 |
| GC-S12 | 26 October 2026 | Bodies: Buer from the owner's Decor Blossom (texture byte for byte, geometry adapted: stem lengthened, twin faces dropped); five built with Jugcraft's generator and painted from named owner paintings, grafting only non-creature owner parts (the acorn cap, the flower-crown ring); Siren and Flarecannon not used; Chunk D's don't 15 kept strict | Decided by the session | art plan |
| GC-S13 | 26 October 2026 | Sounds from existing runtime mono files under new events, subtitled, no voice; every named file's channels checked in this contract (three stereo ambience families refused) | Decided by the session | every spirit |
| GC-S14 | 26 October 2026 | Content and citation limits (Bifrons's corpse lines out; no pale horse; no "invincible"; plant healing only; Amdusias's music unattributed until checked; the three universal claims labelled [U]; Bifrons's grounding and Amdusias's music-derived manner labelled Jugcraft adaptations; edition named correctly; no quotation in game before a scan check) | Decided by the session | dossiers, records |
| GC-S15 | 26 October 2026 | No job adapter: none of the six holds property over time; S1 to S6 used unchanged; S7 and S8 proposed; the schema not marked frozen | Decided by the session | schema review |
| GC-S16 | 26 October 2026 | Branch `claude/garden-cohort` on `claude/chunk-d-pact-hall`; main's #475 merged down the stack (B, C, D) before unit 1, as Chunk D merged main. This departs from Chunk D's "Next proposed chunk", which said the cohort "needs Chunks B to D merged" (CD:2710); the owner's "gardens next" did not address it, so it is reviewable at the checkpoint | Decided by the session | baseline |
| GC-S17 | 26 October 2026 | A garden load card, its own opted-in server test, run in the existing load-card job under the same input; budget in C-P19's form | Decided by the session | GC-10 |
| GC-S18 | 26 October 2026 | The third two-client test shares one second-client helper with the pact network test (main's test untouched); added to the opted-in job without the optional integrations | Decided by the session | GC-10 |
| GC-S19 | 26 October 2026 | Seven units plus the baseline (one spirit a unit; unit 1 carries only the groundwork Buer uses; each later shared mechanism lands with its first user: accords and devices with Bathin, the work target with Marax, containers with Foras, the canopy with Amdusias): a recorded departure from 09:3 | Decided by the session | Units |
| GC-S20 | 26 October 2026 | Bathin is bounded (12 moves a day of at most 8, or 3 batches into a pylon) and leaves Hearts with their own pylon alone until answered, so the Heart's adjacent pour stays the high-throughput layout | Decided by the session | GC-05 |
| GC-S21 | 26 October 2026 | Amdusias's canopy damps only the area's disturbance, by at most 2, never below 0, never for a Heart, only for crops whose niche needs no stir unless answered `all`, never easing harvest strain (the strained judgement keeps the undamped value), the largest of covering canopies and never their sum, only while he serves, the holder is near and his tree stands; it is not the season shelter or the Rimekeepers' conservatory | Decided by the session | GC-09 |
| GC-S22 | 26 October 2026 | No new item, block or block entity type; ids added are entities, data, tags, sounds, lang, research, practice, advancements, codex, one perch key, one roster status and the wire additions | Decided by the session | every unit |
| GC-S23 | 26 October 2026 | Chunk D's recommendation to turn Stolas toward whoever speaks to him and to give later spirits their own silhouettes was not answered separately by "keep stolas" (CD:2780); the cohort carries both as the session's decision (don't 18): "a spirit faces whoever speaks to it" as shared groundwork Stolas also uses (GC-02), and each spirit's own silhouette in its brief | Decided by the session | GC-02, every dossier |
| GC-S24 | 26 October 2026 | The build runs in CI only (no local game build); unit 1's first push is a compile-only slice; every vanilla, GeckoLib and SmartBrainLib call is checked against the reference sources before it is written and the check recorded in its ticket | Decided by the session | every unit |
| GC-S25 | 28 October 2026 | Bathin takes from a Verdant Heart only when it holds a whole move (`MOVE_MAX`, 8) or makes no more Verdance (its status is not `working`: full, starved, stalled, on no bed), and then whatever it holds, so a nearly empty, idle Heart is still emptied into a Gleaner with room for a whole move (a pylon takes whole batches of 3, so 1 or 2 may stay, as before). A Heart beating on its bed makes 1 or 2 a beat every 200 ticks and he thinks every 20, so before this each of his 12 moves a day carried a beat's 1 or 2 (12 to 24 a day); now a beating Heart gives a whole move each time (8 into a Gleaner, up to 96 a day; usually 2 whole batches a move into a pylon, 6 Verdance for 4 Ley Charge, so 72 Verdance a day by that road at that rate, 3 only when it held 9 or more). A Heart answered "take from it" whose own pylon takes each beat's whole batches never gathers 8 while it beats, so he takes only what its road cannot (its pylon full) or what is left once it stops. Option (a) of unit 2's review finding 4, which the review left to the owner; the session chose it under the standing instruction, and it is reviewable at the checkpoint. The term ("up to 8 at a time") and his Jade note ("hold no Verdance he may take") are unchanged; the note is what he shows while a beating Heart gathers its move. The codex's "His Service" page says so: from a Heart still beating he waits for a whole move, from one that has stopped he takes what is left. Evidence: `bathinCarriesWholeMovesFromABeatingHeart` (written, not yet run) and its audit needles in `check_garden_pacts` (the source rule; `working` left only by a beat that made Verdance and set nowhere else in the Heart or its base class) | Decided by the session | GC-05 |
| GC-S26 | 28 October 2026 | The pact screens move onto the magic interfaces ([magic-interfaces.md](magic-interfaces.md), unit 1): variant B (the relic scroll card); D-S15 amended (before Accept every term is a chip narrated and hovered with its whole sentence, the six standing terms one chip whose More shows their lines, every blocker above the terms, the price in view); every spirit's beats at most 18 words a line with a stage direction of at most 8, a Terms skip on beats 1 and 2, and beats once heard not shown again. In answer to the owner's request for far less reading (28 October 2026); reviewable at the checkpoint, not owner-accepted. Units 3 to 6 write each spirit's beats to these limits and its work's row in `tools/magic_ui.py` `WORK_GLANCES` (its name, service line, icon, kept-page rows, blockers and chooser reason), a glance for its first task's act when that row would run past 12 words (`check_magic_ui` renders the row with every act), and its icon's line in `PactGlance.WORK_ICONS`, which `check_magic_ui` holds equal; a row named like Bathin's draws no icon of his (his rows' icons are keyed by their exact keys) | Decided by the session | GC-01, GC-04 to GC-09 |
| GC-P1 | 26 October 2026 | Work ids: `strain_tending`, `verdance_carrying`, `habitat_reading` (holder only), `graft_keeping`, `root_running`, `canopy_shelter` | Proposed (unit 1) | all |
| GC-P2 | 26 October 2026 | `GardenReach`: 64 beds, nearest first, refreshed every 200 ticks, loaded chunks only | Proposed (unit 1) | GC-03 |
| GC-P3 | 26 October 2026 | Wire in unit 1: `invite` action 9, mode `invite` 4; one nested payload field `spirit` (`sign`, `work`), the payload at 14 top-level fields | Proposed (unit 1) | GC-01 |
| GC-P4 | 26 October 2026 | Resonance-free wording: `term.compact.items` and `answer.cannot_afford.items`, chosen at resonance 0; per-work quota keys `term.quota.<work>` with the generic key as fallback | Proposed (unit 1) | GC-01, GC-09 |
| GC-P5 | 26 October 2026 | Stations: rooted or hovering bodies on the perch; standing bodies on the first free side cell toward the inviter (clockwise), else on the perch *(amended unit 1: not used in unit 1, whose spirits are all on the perch, GC-P11; it stays the rule for the first standing body, unit 3)* *(amended unit 3, GC-P67: standing bodies two blocks out on the first clear side, a quarter turn clockwise from the inviter first; else on the perch)* | Proposed (unit 1) | GC-01, GC-06 |
| GC-P6 | 26 October 2026 | The Garden's Regard: examine a garden specimen (with an examine line and a tooltip); cultivation; worker service; mastered by `garden_pact_service` distinct 3 | Proposed (unit 1) | GC-03 |
| GC-P7 | 26 October 2026 | First obligations: Buer `TEND`, Bathin `DEVICE` (an awake living device used with an empty hand), Marax `READ`, Foras `GRAFT`, Bifrons `FEED`, Amdusias `HARVEST`, each act and hook added with its spirit | Proposed (unit 1) | GC-03 to GC-09 |
| GC-P8 | 26 October 2026 | Facing: head at vanilla's 10° a tick; the body by vanilla's body control; tests allow 10° (head) and 15° (body) *(amended unit 1: the speaker is the latest to open the screen or send a request; a work target outranks the speaker while its gesture plays; a spirit faces whoever speaks whatever its status but switched off, a spent quota included, A-03)* | Proposed (unit 1) | GC-02 |
| GC-P9 | 26 October 2026 | The `LookAtTarget` target test: an entity target is invalid when dead, in another level or beyond 32 blocks; a block target never | Proposed (unit 1) | GC-02 |
| GC-P10 | 26 October 2026 | The chooser: an empty perch's owner who may invite two or more spirits (offered at that perch, Regard understood, no pact with it held; at most 8, in `PactParser.REQUIRED`'s order) chooses among them, each "ready" or with what its service says is still missing; with exactly one, its offer at once (no chooser: Stolas's path as built); with none, the first offer whose research they have begun, else the first offered. Anyone else sees the waiting visitor's offer, else the same single offer, with "Only this perch's owner may invite (that spirit) here"; never the chooser. A choice holds for the conversation while it stays offered; choosing a spirit that cannot come sends another spirit's waiting visitor away (the unit review) | Proposed (unit 1) | GC-01 |
| GC-P11 | 26 October 2026 | Every unit-1 spirit is summoned on the perch (Stolas; Buer is rooted). GC-P5's side cell for standing bodies lands with the first standing body (Marax, unit 3); `SPIRITS` records each row's `station` ("perch" until then) | Proposed (unit 1) | GC-01, GC-06 |
| GC-P12 | 26 October 2026 | Words: the ten lines every spirit shares that named Stolas take the spirit's name as `%s` (keys unchanged; his rendering identical, `stolasLinesRenderAsBefore`); the eleven only Star-reckoning shows and the fifteen with only "he"/"his" stay as written; each caller passes the name (R9: not GC-01's 36) | Proposed (unit 1) | GC-01 |
| GC-P13 | 26 October 2026 | The wire beyond GC-P3: `Spirit(sign, work)`, each at most `MAX_ID` characters, `Spirit.NONE` for the chooser; a thirteen-argument constructor keeps Chunk D's calls; INVITE answered `invited`; INVITE (owner only) and RECALL honoured only in the mode that sent their list and within it, else `not_offered`; SEAT and END index the seat page's list there (past it, `not_offered`) and elsewhere name, as Chunk D's did, the reader's first pact without its perch, else the pact kept here; the revision moves when the chooser's list does | Proposed (unit 1) | GC-01 |
| GC-P14 | 26 October 2026 | `PactService` in unit 1: `work`, `holderOnly`, `missing`, `choices`, `perform`, `serviceRows`, `obligationRows`, `suspendedRow` and a default `work` step returning IDLE *(amended unit 1, R2: `obligation()` landed with GC-03 and Jade's `Note` and `note(...)` with GC-04; `complication` lands with GC-05)*. `PactServices` registers each once, checked against `PactParser.WORK` and `HOLDER_ONLY`, and the game refuses to start without one for every work. A departure from GC-01's SG1 member names (`rows`, `work(level, spirit, record, holder, now)`) | Proposed (unit 1) | GC-01 |
| GC-P15 | 26 October 2026 | `PactParser.REQUIRED` lists every shipped offer in `SPIRITS`'s order, which is the perch's; a missing one is reported as "offers it to no one until one does"; a loaded offer whose spirit is not registered is never offered | Proposed (unit 1) | GC-01 |
| GC-P16 | 26 October 2026 | The journal's pacts section: Stolas's two lines as built; another Regard a perch offers gets its line only once its reader has begun it, so a reader who never began one sees Chunk D's section exactly | Proposed (unit 1) | GC-01 |
| GC-P17 | 26 October 2026 | Forced edits of existing test lines *(amended unit 1, R11, A-08: three lines, not two)*: `thePactScreensMessagesAreBounded` holds Chunk D's nine actions in place (`ACTIONS.subList(0, 9)`) instead of counting them; the network test renders `missing.owner` with Stolas's name; `ConcordanceResourceGameTests.thePactDefinitionParsesStrictly` allows only the other shipped definitions' "no definition loaded" (GC-04) | Proposed (unit 1) | GC-01, GC-04 |
| GC-P18 | 26 October 2026 | *(Reworded unit 1, R7:)* GC-01 adds no client step; `ConcordancePactClientGameTests` gains no chooser (GC-02 changes it only for facing); the chooser is photographed with a second spirit in the cohort's client class (GC-P30) with GC-04 (default GUI, scale 4, 320x180, keyboard only). A departure from GC-01's "Client" line | Proposed (unit 1) | GC-01, GC-04 |
| GC-P19 | 26 October 2026 | GC-01 in three steps *(amended unit 1: landed as two commits, `62b0095fa` compile-only and `202e75969` with the zero-diff gate inside it)*; Jade names a visiting spirit from the reading's appended key `visitor` (its counterpart), never from the offer | Proposed (unit 1) | GC-01 |
| GC-P20, GC-P21 | 26 October 2026 | Buer's beats; his terms (radius 8, 0 to 12,000, 16 a day, party), offering (Mendvetch, 1 a week) and compact (4 Mendvetch, 4 bone meal) | Proposed (unit 1) | GC-04 |
| GC-P22 | 26 October 2026 | The overworked bed: eased today and at strain 3 again; resolved at strain 2 or below; at most 16 remembered, unsaved; a service row on the kept page and Jade | Proposed (unit 1) | GC-04 |
| GC-P23 | 26 October 2026 | The overworked memory (refines GC-P22): the presence's `serviceMemory` (never saved) holds the beds he eased today and his complaints, together at most 16, the oldest eased forgotten first; a new game day forgets the eases, a complaint stands until its bed eases to 2 or less (by any of the bed's rules) or is broken or asleep; a resolved bed is his again and is a fresh complaint only after he eases it again; beds in unloaded chunks are left as remembered; shown only while loaded, awake and at 3. *(Unit review:)* the beds refused for themselves since the index was last read, at most 64, also never saved, are skipped until its next read | Proposed (unit 1) | GC-04 |
| GC-P24 | 26 October 2026 | Of each zero-thickness per-face pair, the generator drops the face whose texels are all clear, computed from the texture, and stops when a pair is both drawn or both clear; for the Decor Blossom: plate `down`, cap `down`, both blades `north` (corrects the contract's "south" of the blades, which would erase the drawn stalk); Stolas's six box-UV twins recorded, not changed (R22, R23) | Proposed (unit 1) | GC-04, the twin rule for every spirit |
| GC-P25 | 26 October 2026 | Buer's body numbers: the stem repeated once (2.7 px), scale 2.2, entity box 1.2 x 1.6 (eyes 1.4; R21), shadow 0.6, culling 0.4 / 1.0 below / 0.5 above, his pistil's three full-red texels lit; his clips: the owner's three kept, four loops and six gestures added with their lengths and telling windows as `SPIRITS` records *(amended unit 1, A-06: his listening loop shows over everything but switched off; a spent quota shows him shut only while nobody speaks)* | Proposed (unit 1) | GC-04 |
| GC-P26 | 26 October 2026 | His seven cues use the art plan's three recordings (two of the four named files are one), told apart by pitch: listen 1.25, refuse 0.6, depart 0.7; `sounds.json` gains `pitch` for pact cues through `SOUND_PITCH` | Proposed (unit 1) | GC-04 |
| GC-P27 | 26 October 2026 | A pact's service steps under `UPKEEP_DUE` too (its terms promise service through the grace), the status staying `UPKEEP_DUE`, and a garden service asks the holder's presence at each change (`GardenPacts.target`), since `decide` returns the pact's own state before asking it. *(Unit review, finding 11:)* the base steps under `UPKEEP_DUE` only when `Authority.presentNear` finds the holder within `Pacts.REACH`, so no service runs for an absent holder; the common terms, SG2 and GC-03's beneficiary rule are amended to say so (R3, R24) | Proposed (unit 1) | GC-04 onward (every service) |
| GC-P28 | 26 October 2026 | Strain tending's choice: from the index, beds the bed's own rule allows and not overworked, the most strained first then the index's order (nearest the perch), at most 8 asked a think, the first that may be changed tended; per-bed refusals (not a beneficiary's now, not the holder's to change, outside the area) skip that bed (since the unit review until the index's next read), the pact's own (suspended, held, quota, holder away) stop the think | Proposed (unit 1) | GC-04 |
| GC-P29 | 26 October 2026 | Words and evidence: each garden work's words in `SERVICE_WORDS` (messages, its kept-page section, Jade notes, its own reason ids, never one of PactMenu's ANSWERS: Buer's `nothing_to_mend`); a generic `PactService.note` and two Jade keys (a shared hook, not a spirit's code in PactReading); the greeting's stage direction as Stolas's has; his codex pages in GC-03's entry; the whole-path test without the chooser (it needs Stolas's Regard), which the GC-01 server tests and the client class show (R27); the client's small size 320x180; his astir loop not photographed | Proposed (unit 1) | GC-04 to GC-10 |
| GC-P30 | 26 October 2026 | One garden client class for every spirit, shots named `jugcraft_garden_<spirit>_<view>` *(amended unit 1: or `jugcraft_garden_<view>` for a view of no one spirit, such as the chooser and the specimen tooltip; R19)* | Proposed (unit 1) | GC-04 to GC-10 |
| GC-P31 | 26 October 2026 | A garden obligation counts while its pact is active (seated, kept by its perch, neither suspended nor held for upkeep) and the act is inside its area, whatever its hours or its day's quota (Stolas's precedent): the act is the holder's, so a sleeping or spent spirit never hides it; the bed's keeper is not asked (the gesture already asked the holder's own hands) | Proposed (unit 1) | GC-03, GC-04 to GC-09 |
| GC-P32 | 26 October 2026 | The bed index indexes only beds whose keeper the pact benefits at each refresh (strangers never crowd the 64; `target` asks again at each change, S3); it keeps places, not entities, resolved in loaded chunks at each read; a clock run back refreshes it | Proposed (unit 1) | GC-03 |
| GC-P33 | 26 October 2026 | A presence's service memory (`PactSpiritEntity.serviceMemory`, typed, never saved) holds each garden service's index and its day's memory | Proposed (unit 1) | GC-03 onward |
| GC-P34 | 26 October 2026 | `GardenPacts.target`'s reasons and order: OUTSIDE_AGREEMENT (no service or perch, another level, outside the area or hours), SUSPENDED, UPKEEP_HELD, FINISHED (quota), IDLE (not a beneficiary's), OWNER_OFFLINE (holder not within `Pacts.REACH`), BLOCKED_BY_ACCESS; cheapest first, Authority's events last | Proposed (unit 1) | GC-03 onward |
| GC-P35 | 26 October 2026 | The Regard's specimens are the four organism crops' produce and Verdant Chaff; its icon the Verdant Bed; its codex entry at Binding (6, 4) with the perch's recipe; its words as built | Proposed (unit 1) | GC-03 |
| GC-P36 | 26 October 2026 | GB26: only the garden practice's `distinct` is honoured, as distinct shipped garden definitions each a way to the practice; the audit expects the practice unreachable while none ships and the mastered state while fewer than three do, and flags either if reached early | Proposed (unit 1) | GC-03, unit 3 |
| GC-P37 | 26 October 2026 | The journey's `GARDEN` route (three steps to understood) and acquisition's `GARDEN_PACTS` (`sunpetal`, `verdant_bed`, `bonding_charm`); `verdant_chaff` a `PRODUCERS` row (the cultivation practice) | Proposed (unit 1) | GC-03 onward |
| GC-P38 | 26 October 2026 | One shared first-obligation row for every garden service (`PactObligations.rows`: the act to do, or "kept: n of 3 toward mastery") | Proposed (unit 1) | GC-04 to GC-09 |
| GC-P39 | 26 October 2026 | `habitat()` reads through `cachedHabitat()` (one place for unit 6's canopy); `tend` and `mayTendAt` share `tendedLately` | Proposed (unit 1) | GC-03, GC-09 |
| GC-P40, GC-P41 | 26 October 2026 | Bathin's beats; terms (radius 10, any hour, 12 moves, party), offering (4 Verdant Chaff a week), compact (2 ender pearls, 8 chaff) | Proposed (unit 2) | GC-05 |
| GC-P42 | 26 October 2026 | The perch's `accord`: a string of at most 16 characters from the service's list; absent when unanswered; read as unanswered when unknown; cleared on end or unseat *(amended unit 2, wording: forgotten with its pact: never read or saved while its perch keeps no pact, and reset when a pact is sealed or seated there; the same for every reader)* | Proposed (unit 2) | GC-05 |
| GC-P43 | 26 October 2026 | Wire in unit 2: `resolve` action 10 (the `keep` mode, the holder only); `spirit.matter`, `spirit.answers` (at most 3) | Proposed (unit 2) | GC-05 |
| GC-P44 | 26 October 2026 | `Status.AWAITING_ANSWER` ("awaiting_answer"), appended, reported when a choice waits and the presence would otherwise be idle *(amended unit 2: reported while a choice waits once `decide` returned IDLE, whatever the service's step returned; never over an offering due or a reason of `decide`'s; GC-P47)* | Proposed (unit 2) | GC-05 |
| GC-P45 | 26 October 2026 | `GardenReach` devices: 16 each of Hearts, Gleaners and Ley Pylons *(as built; detailed by GC-P54)* | Proposed (unit 2) | GC-05 |
| GC-P46 | 26 October 2026 | The accord's rules in one class, `PactAccords`: a service's accords list its safe course first; the matter shown while a choice is raised or an answer kept, as the kept page's matter row and its answers; any answer may be changed at any time, the same answer again kept as it was, answering spends nothing and is allowed whatever the pact's hold; only the record's holder, in person, alive, with the Concordance on, for the page's work, writes it; the perch reads it only while a pact counts there, keeps an unknown value as written, drops one over 16 characters or without a pact at load, and starts every pact sealed or seated there unanswered; no hook in the shared `AgreementHolder`; no `course` for a spirit (B-05: the service reads `PactAccords.answer` for its perch) | Proposed (unit 2) | GC-05 to GC-09 |
| GC-P47 | 26 October 2026 | `AWAITING_ANSWER` replaces whatever the service's step returned once `decide` returned `IDLE` and a choice waits; never over an offering due or any reason of `decide`'s (the holder away is said instead); `Sign.WANT` on the change; said by the page, the roster, Jade and that one sign; no chat line | Proposed (unit 2) | GC-05 to GC-09 |
| GC-P48 | 26 October 2026 | Jade's readings append `matter` (the matter raised now) and `accord` (the answer kept); a choice waiting is said by the presence's status (`waiting`); Bathin's notes `two_roads` (a count), `empty`, `nowhere`; his matter's id `two_roads` (B-01) | Proposed (unit 2) | GC-05 |
| GC-P49 | 26 October 2026 | Words per work in `SERVICE_WORDS` (`accords`, `question`, `matters`); the shared lines `row.matter.awaiting`, `row.matter.answered`, `answer.resolved`, the screen's `matter.*`, Jade's `jade.pact.accord`, the status "awaiting your answer at its perch"; each work's shipped accords pinned in `check_pacts`' `GC05_ACCORDS` (append only) | Proposed (unit 2) | GC-05 to GC-09 |
| GC-P50 | 26 October 2026 | The move: one a think; the Gleaner road before the pylon road; the fullest Heart, the emptiest Gleaner, nearest the perch among equals; a Gleaner only with room for 8, a pylon only with room for a batch, at most 3 batches; "within reach" = `HEART_REACH` on every axis, "beside" = a shared face, "its own road" = an awake Heart with a pylon on a face it may feed, full or not; no one's pylon fed as the Heart's own pour feeds it and asked as the holder's own *(amended unit 2, GC-S25: a Heart still beating is a source only with a whole move)* | Proposed (unit 2) | GC-05 |
| GC-P51 | 26 October 2026 | The matter `two_roads`; answers `yield`, `claim` (append only); `yield` the safe course and the reading of an unanswered or unknown value (through `PactAccords.answer`, B-05); raised once a presence by his inspect gesture toward the nearest such Heart; the matter stands whenever such a Heart is indexed, answered or not *(amended unit 2, `4fcf55513`: only a Heart whose keeper the pact still serves)* | Proposed (unit 2) | GC-05 |
| GC-P52 | 26 October 2026 | Failure: IDLE with Jade's `empty` or `nowhere`; a place's refusal skips it, the pact's ends the think with its status; at most 8 places asked a think *(amended unit 2, `4fcf55513`: a place refused for itself is skipped until the index is next read, at no question, at most 48 remembered; `nowhere` when the questions run out)* | Proposed (unit 2) | GC-05 |
| GC-P53 | 26 October 2026 | `DEVICE`: an awake living device (Heart, Gleaner, Mulch Maw, Habitat Gauge) read or set with an empty hand in his area while his pact is active; a waking is not; one line after `useEmpty` | Proposed (unit 2) | GC-05 |
| GC-P54 | 26 October 2026 | The device index: one pass with the beds; `GardenReach.devices(record)` apart from `of(record)` (a bed index costs what it did); pylons by owner, no one's kept; 16 each | Proposed (unit 2) | GC-05 |
| GC-P55 | 26 October 2026 | Interfaces and economy: `receiveFrom` and `pourInto` return the amount (0: nothing moved); `GleanerBlockEntity.setVerdance` for operators and tests; `GardenPacts.presence`; `VerdanceCarrying.Memory.forget` (B-16); no conversion and no installation row; `check_garden_pacts` and `check_verdance_roads` hold his calls, the pour's body and the conversion's loss | Proposed (unit 2) | GC-05 |
| GC-P56 | 26 October 2026 | His body built by `tools/garden_bodies.py` (no owner rig fits; the Siren is not used): 12 box-UV cubes on 8 bones (11 UV regions; a coil and tail tip as one group, the waist, chest, head and arms apart, so a coil drawn tight never squeezes his chest), a 128 x 64 sheet painted only in the willow log's and the Spanish moss's colours, his eyes lit; entity 0.9 x 1.9, eyes 1.65; `tools/owner_art.py` writes and checks built files (`BUILT`); the silhouette rule (a fifth apart on one axis) for every pair of spirits | Proposed (unit 2) | GC-05; every later built body |
| GC-P57 | 26 October 2026 | His clips: five loops (idle 4.0, listen 3.0, waiting 2.0, suspended 6.0, astir 1.5 s) and six gestures with telling windows (arrive 1.5 s / 0.1-0.6, inspect 2.0 / 0.4-1.2, assent 1.5 / 0.3-0.9, refuse 1.6 / 0.3-1.0, depart 1.0 / 0.25-0.75, gone at 0.8-0.85, work 1.5 / 0.15-0.75); a protected no-op hook `PactSpiritEntity.gestured(ServerLevel, Clip)` called once a cue shows (B-15); his ring as he arrives and departs and his shimmer at both ends of a carry, the only particles a carry shows, kept in memory only *(amended unit 2: as the shared signs `SHOVE` and `WORK`, not `DUST_PLUME` puffs or `GLOW` glints: `check_signs`)* | Proposed (unit 2) | GC-05; the hook for later spirits |
| GC-P58 | 26 October 2026 | Cues, words and sign: seven cues on the art plan's five mono files, told apart by pitch (arrive 1.5, listen 1.2, refuse 0.7, depart 1.8), subtitled; his sign the ender pearl (unique among `SPIRITS`, audited); the greeting's stage direction; his term; his refusal with two arguments and the reason `nothing_to_carry`; four codex pages; the perch tooltip's clause; Jade's and the roster's service names; the invitation the contract's (B-02) | Proposed (unit 2) | GC-05 |
| GC-P59 | 26 October 2026 | Evidence: his flow in the one garden client class after Buer's, at the same perch (the chooser with three entries), shots `jugcraft_garden_bathin_*` and `jugcraft_garden_roster_awaiting`; waiting posed with the Concordance off; the shimmer photographed frozen just after the carry; `offerOf` and the forced unit-1 lines a second garden spirit makes (as GC-P17 records unit 1's); one Jade test (B-10); the client's carrying Heart on the floor, no bed (B-18); `WEIGHTS` 560 | Proposed (unit 2) *(amended unit 3: its forced lines are superseded by GC-06b's generic lines, GC-P77)* | GC-05 to GC-10 |
| GC-P60, GC-P61 | 26 October 2026 | Marax's beats; terms (radius 8, any hour, 2 readings, personal), offering (2 paper a week), compact (1 book, 2 Sunpetal, 2 Dewmoss); 8 beds a reading; pique after 3. *(GC-P60 amended unit 3, held to GC-S26's limits: a stage direction before the greeting, "A man-faced bull treads up beside the perch."; the greeting and price as Dossier 3 words them; the service beat shortened to "Ask, and my familiar names one change. Only one: the rest you learn by doing it." (16 words, keeping "Ask": he reads only when asked). Decided by the session, reviewable at the checkpoint, not owner-accepted)* | Proposed (unit 3); the service beat's amendment decided by the session | GC-06 |
| GC-P62 | 26 October 2026 | The first-fault rule (the first outside factor in `Factor` order, "one of n"); the synced `TARGET` and the familiar's bone hook, culling box to 8 blocks *(amended unit 3: the reach and its culling box go to `TARGET_REACH`, 24 blocks, the farthest a hover point can be from a station two out at the largest radius a pact may name, 12)* | Proposed (unit 3) | GC-06 |
| GC-P63 | 27 October 2026 | Upkeep grace 1 day, cap 4 (as Bathin's); sign `minecraft:writable_book` (pairwise unique: Stolas's starflower, Buer's Mendvetch, Bathin's ender pearl); box 1.6 x 1.4, eyes 1.0 | Proposed (unit 3) | GC-06 |
| GC-P64 | 27 October 2026 | `HabitatAdvice.one` (built in GC-06b): null while thriving; the factors holding the plant at its step (`outside()` when stalled, `tolerable()` when tolerating), in `Factor` order; one holding it: the nearest value of it (or, where strain decided the disturbance fit, of the strain, only lowered, tried first) that alone raises it a step; otherwise the first, "one of max(2, n)", at its nearest ideal value (strain to 0). A change only moves toward the ideal range, so a niche with no disturbance minimum is never told to add disturbance. The graft clause: the first graft by id whose host is the plant, offered only when the bed carries none, that alone raises it a step on this reading. At most `MOST_VERDICTS` (129) verdicts a call, plus one a graft | Proposed (unit 3) | GC-06 |
| GC-P65 | 27 October 2026 | `TARGET` (`Optional<BlockPos>`, synced, never saved; GC-06b) set only through `showTarget(pos, until)` while the spirit listens; cleared at its first think after the conversation ends or `SHOW_TICKS` (400) pass, and when it leaves; the client flies the reach out or back over `FLIGHT_TICKS` (10) to 2.5 blocks over the block's centre | Proposed (unit 3) | GC-06 |
| GC-P66 | 27 October 2026 | `PactSpiritRenderers.Row` gains a seventh field, `reach` (a root-child bone; null for every earlier row; GC-06b); render data `jugcraft_pact_reach` (Vec3) and `jugcraft_pact_flight` (Float) filled every frame; the bone lerped by the flight toward `PactSpiritEntity.posed(...)`, shared arithmetic a server test checks; the culling box widened to a one-block cube at the reach point | Proposed (unit 3) | GC-06 |
| GC-P67 | 27 October 2026 | `Station.BESIDE` (GC-06b): two blocks out on the first clear side, a quarter turn clockwise from the inviter first, round clockwise, the inviter's side last; clear = the two cells out and the cells over them up to the body's height (at least two) loaded and without collision, the outer cell's floor sturdy on top; none clear: the perch's top; kept each think, the side it was stationed on first (a memory, never saved; after a load, the side it stands on), no saved key; its known limits in GC-06's As built. Amends GC-P5's distance and order | Proposed (unit 3) | GC-06 to GC-09 |
| GC-P68 | 27 October 2026 | Which bed: the index's beds (the holder's, `GardenReach.of`), planted (an `OrganismCropBlock` with an organism above), nearest first, each asked of `GardenPacts.target`; cached samples only; the worst growth (lowest pace), ties to the nearer. Counted on the quota only when a change is named (`read`); `nothing_grows`, `no_sample`, `nothing_to_correct` and every refusal spend nothing. *(Amended unit 3, Marax's review: at most `READ_BEDS` (8) read and `MAX_ASKED` (16) asked, a bed `target` refuses using none of the 8; `nothing_to_correct` is an answer given, drawn done with his assent, still counting nothing)* | Proposed (unit 3) | GC-06 |
| GC-P69 | 27 October 2026 | The proud familiar: the presence's memory (never saved) of its last named bed, factor and verdict and how many readings running named them; at `PIQUE` the bed is proud (with its verdict), at most `READ_BEDS` proud beds, the oldest forgotten; a proud bed is skipped while its verdict (from its cached sample) is unchanged, forgiven when it changes or the bed is gone; a reading that skipped one says so; all left proud: `nothing_to_correct` with that line; a kept-page row and a Jade note `proud`. *(Amended unit 3, Marax's review: the count starts again when the last bed named has changed by the next reading, so a bed that changes and falls back is not proud at once; the proud row is marked as a row that asks something of the player)* | Proposed (unit 3) | GC-06 |
| GC-P70 | 27 October 2026 | `PactObligations.Act.READ` ("read"), appended after `DEVICE`; its hook one line after `bed.report(serverLevel, server);` in `VerdantBedBlock.useWithoutItem`; act words "read a Verdant Bed itself (its side, where a crop covers it)", drawn "use a Verdant Bed's side" (Marax's review) | Proposed (unit 3) | GC-06 |
| GC-P71 | 27 October 2026 | Body built by `tools/garden_bodies.py` (no owner rig fits a bull with a man's face): root > body (torso, moss hump) > head > horns, tail; four legs; the familiar a root child with two wing children; a 128 x 64 skin in the three paintings' colours only; a glowmask of the familiar's two eyes only; twelve clips (six loops, six gestures) | Proposed (unit 3) | GC-06 |
| GC-P72 | 27 October 2026 | Cues on the four runtime gaia files: arrive gaia_1, listen gaia_2 at 1.3, inspect gaia_3, assent gaia_4, refuse gaia_2 at 0.7, depart gaia_1 at 0.85, work gaia_3 at 1.5; each subtitled | Proposed (unit 3) | GC-06 |
| GC-P73 | 27 October 2026 | His night look: his idle loop is `stargaze` while the client's sky is dark (`Level.isDarkOutside()`), cosmetic, never a status | Proposed (unit 3) | GC-06 |
| GC-P74 | 27 October 2026 | The kept page draws each choice of a work's on-request action (not Star-reckoning's) as a plate sending RECALL with its index, its hint "%s left today" or "None left today" (`screen.jugcraft.pact.work.left`, `.spent`). *(As built on the magic interfaces, decided by the session: the plate draws its glance, "Ask him", the choice's whole name its message; a primary plate, as Star-reckoning's Recall is, since Marax's review)* | Proposed (unit 3) | GC-06 |
| GC-P75 | 27 October 2026 | Mastery lands here: the codex's mastered page ("Mastered", condition The Garden's Regard mastered, the entry's last page), the journey's mastery step and `theGardensRegardIsMasteredThroughThreeSpirits`; GB26 unchanged | Proposed (unit 3) | GC-06 |
| GC-P76 | 27 October 2026 | Dewmoss joins `tools/acquisition.py` `GARDEN_PACTS` and `GARDEN_RECIPES` (his compact), in the words unit 4 writes for Foras's compact too; his journey step names it | Proposed (unit 3) | GC-06, GC-07 |
| GC-P77 | 27 October 2026 | The unit-1 and unit-2 test lines a further garden spirit forces are rewritten generic over the shipped garden spirits (GC-06b: the perch's offers, the journal's names; the second perch of the SG12 tests moved north of the first), so units 4 to 6 need no edit to them | Proposed (unit 3) | GC-05 to GC-09 |
| GC-P78 | 27 October 2026 | Evidence: his flow in the one garden client class after Bathin's, shots `jugcraft_garden_marax_*` (his page with a proud bed among them since Marax's review), a standing-body facing helper (the player 3 blocks from him on each side), `WEIGHTS` 820 (560 + 260); his server tests (`GC06_TESTS`, twenty since Marax's review) and SG12's cases | Proposed (unit 3) | GC-06 |
| GC-P79 | 27 October 2026 | The shared-line register for units 4 to 6 (merge order 3, 4, 5, 6): later standing bodies reuse `Station.BESIDE`, later targets reuse `showTarget` and `Row.reach`; unit 3 alone adds mastery; each unit's lines under its own marker (`GC-0n unit n`), `PactGlance.WORK_ICONS` a `Map.ofEntries` that ends with Star-reckoning's entry (Marax's review) | Proposed (unit 3) | GC-07 to GC-09 |
| GC-P80, GC-P81 | 26 October 2026 | Foras's beats; terms (radius 8, any hour, 8 grafts, party), offering (1 honeycomb a week), compact (4 Dewmoss, 1 honeycomb); prudent by default | Proposed (unit 4) | GC-07 |
| GC-P82 | 26 October 2026 | `GardenReach` containers: 16 plain containers touching the perch; the graft order (preconditions, then extract and commit, then `setGraft`) | Proposed (unit 4) | GC-07 |
| GC-P100, GC-P101 | 26 October 2026 | Bifrons's beats; terms (radius 8, 13,000 to 23,000, 24 units, party), offering (2 logs a week), compact (4 oak logs, 1 emerald); source at 24 or more, destination 3 poorer | Proposed (unit 5) | GC-08 |
| GC-P102 | 26 October 2026 | His measuring as bounded server particles at source, then destination; no client hook | Proposed (unit 5) | GC-08 |
| GC-P120, GC-P121 | 26 October 2026 | Amdusias's beats; terms (radius 6, any hour, 16 beds covered at once, party), offering (1 sapling a week), compact (1 note block, 2 oak saplings); damping 2; tree within 2, looked at every 200 ticks; lapse 40 ticks | Proposed (unit 6) | GC-09 |
| GC-P122 | 26 October 2026 | The canopy rules: crops whose disturbance minimum is 0 (all once answered); the largest covering canopy; `strained()` undamped; the quota worded "At most %s beds sheltered at once"; his cue `pocket_horn_fade` | Proposed (unit 6) | GC-09 |
| GC-P141 | 26 October 2026 | The garden load card's workload and budget (above) | Proposed (unit 7) | GC-10 |
| GC-P142 | 26 October 2026 | The garden two-client scenario (above) | Proposed (unit 7) | GC-10 |

### Unit 1's amendments and reconciliations (cited in code and commits)

Unit 1 was built from four part designs reconciled into one specification (kept with the session's working notes, not
committed). Its amendments (A-n) and the reconciliations (R-n) that code, commits or this page cite are recorded here so
every citation resolves; none is the owner's.

| Id | What changed | Why |
|---|---|---|
| A-01 | `check_pacts`' legacy `StolasEntity.SOUND_IDS` check reads only `stolas.` events (R12) | it compared with every pact sound event, so Buer's seven would fail it |
| A-02 | the offerings rule per `SPIRITS` row inside `check_pacts`' existing loop: each tag is its row's offerings, one of them reachable without Styx (R16) | Buer's tag is Mendvetch alone, not Stolas's vanilla-item rule |
| A-03 | `PactSpiritEntity.face` attends unless switched off (`next != DISABLED`); a spent quota (FINISHED) no longer stops a spirit facing its speaker | Buer returns FINISHED for a spent quota, and every FINISHED from `decide` comes after the spirit has left |
| A-04 | SG12's fixture table declared empty and filled in a `static {}` block | avoids a lambda inferred through a diamond |
| A-05 | SG12's `think` calls pass world time 0 (06:00) and an explicit game time | a garden spirit is outside its agreement outside its hours, so the real clock would make the cases depend on the hour of the run |
| A-06 | `BuerEntity.loop` shows listening over everything but switched off; FINISHED shows him shut only while nobody speaks | with Stolas's rule he would stay shut while spoken to after his quota was spent |
| A-07 | `buerStopsAtHisQuota` opens the perch after FINISHED (he attends and listens); `buerAnswersEachActionWithHisGesture` asserts the FINISHED loops | pins A-03 and A-06 |
| A-08 | `thePactDefinitionParsesStrictly` allows only the other shipped definitions' "no definition loaded" (R11) | appending `jugcraft:buer` to `PactParser.REQUIRED` makes a catalog of Stolas alone report Buer's definition missing |
| A-09 | the guide's research table re-pasted with GC-03 (R13) | `check_delivery` holds it to `tools/concordance_delivery.py`, which lists every research entry |
| A-10 | `tools/owner_art.py` run after the generator in every commit that changes art | `generate_material_data.py` never runs it |
| R2 | `PactService`'s final members: GC-01's, `obligation()` with GC-03, `Note` and `note()` with GC-04 | GC-P14 |
| R3, R24 | a service steps under `UPKEEP_DUE` (GC-P27), its presence asked at each change | the terms promise service through the grace |
| R7 | GC-01 adds no client step; the chooser is photographed in the cohort's client class | GC-P18 |
| R8 | GC-02's `aSpiritFacesTheBedItWorks` and GC-04's `buerFacesTheBedHeTends` are one test, Buer's | the two tickets named the same check |
| R9 | ten shared lines take `%s`, not 36 | GC-P12 |
| R10 | `talkingTeachesNothing` is a new test over every offered spirit | GC-03 called it "extended", but no such test existed (Styx's is `talkingGrantsNoResearch`, unchanged) |
| R19 | shot names `jugcraft_garden_<view>` for a view of no one spirit | GC-P30 |
| R21 | Buer's box 1.2 x 1.6 | GC-P25 |
| R22, R23 | the blades drop `north`; Stolas's six twins recorded | GC-P24 |
| R27 | the whole-path test without the chooser | GC-P29 |
| R28 | the delivery matrix's SmartBrainLib row names the look target a pact spirit's own decision sets, and `aSpiritFacesWhoeverSpeaksToIt` | GC-02 sets a look target |

### The cohort checkpoint's three questions (09:18; at the end, after unit 7)

1. **The six looks, manners and groundings.** Buer is your Decor Blossom grown into a rooted healer (your texture as
   painted, its stem lengthened); the other five are new bodies built for Jugcraft and painted from your paintings (a
   serpent-tailed carrier, a mossy bull with a familiar, a tall acorn-capped elder, a root-mass that stands up to speak,
   a branch-horned unicorn), because your library's only unused creature rigs fit none of them (the Siren is a mermaid
   with a girl's face and hair, already meant for Ember's mermaid skins; the Flarecannon is a fire golem) and the rest
   are creatures already in the game. Bifrons's root-running rests on "Geometry" and "Woods", not on his entry's line
   about moving the dead; the alternative is a wood or orchard service. I recommend keeping them all; or name any
   library rig, or Bifrons's alternative, you want instead. (Facing and own silhouettes were the session's call, GC-S23.)
2. **How much a garden spirit should do.** After play: the quotas; Bathin letting a Heart feed a far pylon; Amdusias
   letting a garden sit beside a workshop before the season shelter exists; Marax giving the botanical advice once planned
   for Stolas; Buer refusing overworked beds until they are fed; Foras, who after his first pass grafts almost nothing
   while his honeycomb continues. I recommend keeping them as built, or name which to narrow or widen.
3. **The next six.** I recommend minerals (Stolas's other deferred topic, and the "precious stones" half of Bathin's,
   Foras's and Marax's entries), or Gloam Ink first if you prefer a small outcome.

## Where to resume

Units 1 (GC-01 to GC-04) and 2 (GC-05) are built, their CI evidence and records are in this page and their records, and
they wait in draft pull request #526 for the owner's review. Unit 3 (GC-06, Marax) is built and recorded, not yet
compiled or run in CI. Next:

1. Look at the pictures of a CI run that includes `d7185041b` (his facing pictures framed whole) and the magic
   interfaces' `73464e938` (no tip repeating a beat): both are pushed, and no CI result for them is recorded here.
2. Refresh main and the open pull requests; merge main into `claude/garden-cohort` if it moved (generated files
   regenerated, never hand-merged).
3. Run CI on unit 3 (Marax): the server jobs and the garden client class. Fix in the test what is the test's, in the
   code what is the code's; then look at his 33 pictures and complete [his Verification](marax-pact.md#verification)
   (a Seen column, as Bathin's) and the results in [unit 3's evidence](#unit-3-as-built-and-its-evidence). His two
   kept-page states estimated over budget (the answer just after the third reading, about 47 words; a different bed named
   while one stays proud, about 50) are to be measured or narrowed then. At the merge that brings the magic interfaces'
   unit 2 (`wip/magic-ui-roster`: its `6cb85aef0` is already on unit 4's line, its review's fixes `57a324264` not) into
   this line, Marax's Jade note needs its short form and mark (`SENTENCES` and `SENTENCE_MARKS` in
   `tools/magic_ui.py`, `MagicGlance.MARKS`: "Proud beds: %1$s", the hourglass), and GC-S26's per-spirit list gains "a
   Jade note's short form and mark".
4. Units 4 to 6 (built in parallel from the shared prefix; merged in the order 3, 4, 5, 6, GC-P79): each takes unit 3's
   markers in the shared tables (`tools/magic_ui.py`'s act glances and `WORK_GLANCES`; `PactGlance.WORK_ICONS`, a
   `Map.ofEntries` whose last entry stays Star-reckoning's) and adds its lines under its own; stands on `Station.BESIDE`
   with no station code of its own; adds codex pages to `PAGES` only (the mastered page, the mastery step and its test are
   unit 3's, and the mastered page stays the entry's last, so its index moves at each merge); drops its own `dewmoss`
   addition (unit 4); adds its client flow after Marax's, calling `yard` first (his two Sunpetal beds lie in it), and its
   weight under its own line; and, for unit 6, keeps `HabitatAdvice` and Marax's reading right under the sheltered
   habitat (re-running the advice harness and `habitatAdviceHoldsForEveryOrganism`).
5. Open items units 1 to 3 leave: Buer's listen, asleep and depart pictures and his refused picture (GC-04's As built);
   Bathin's ring and shimmer drawn as the shared movement and work signs, his arrive and depart pictures alike, his assent
   picture showing no fist at the chest, the matter pictures with the row's tip over the plates, the pylon road not
   photographed (GC-05's As built and [his record](bathin-pact.md#rollout-and-open-questions)); the journey's Buer steps
   still write his numbers as literals; Marax's astir loop not photographed and his reading's sign the shared work sign
   ([his record](marax-pact.md#rollout-and-open-questions)); GB18 (Gutenberg's record); a dispatch of every client class
   on the cohort's head.

## How this contract was checked

- **Read in full:** the five survey reports in this folder; plan chapter 03 (all, for the dossier rules and line numbers);
  Chunk D's contract at its opening, units, baseline, D-04's ticket, completion evidence, schema review, interfaces,
  dependencies, risks (opening), explicit don'ts, known defects, record fields, handoff, checkpoint, deferrals, validation
  plan, decisions and where to resume; `docs/features/TEMPLATE.md`'s opening.
- **Re-read in code at `5eba71ced` for this page:** `G/VerdantBedBlockEntity.java` (whole); `G/Garden.java` (whole);
  `G/VerdantBedBlock.java:130-247`; `G/OrganismCropBlock.java:255-352`; `G/VerdantHeartBlockEntity.java` (whole);
  `G/GleanerBlockEntity.java`'s constants and draw; `G/LivingDeviceBlock.java`'s awaken; `E/Sampler.java`,
  `E/Habitat.java`, `E/SampleBudget.java`, `E/Verdict.java`, `E/Factor.java`; `D/concordance/organism/sunpetal.json`,
  `graft/dewlit.json`, `pact/stolas.json`, `research/stolas_regard.json`, `research/verdant_husbandry.json`, every
  practice gate; `W/PactParser.java` (whole); `W/Pacts.java:380-435`; `S/PactPerchBlockEntity.java:30-180`;
  `S/PactRequestPayload.java`; `S/PactStatePayload.java:37-102`; `S/PactMenu.java`'s Stolas-specific lines (by grep);
  `S/WorkerEntity.java:50-70, 180-260`; `S/AgreementHolder.java`'s outline; `S/Roster.java:82-87, 391, 488`;
  `S/CourierWork.java`'s storage lookups; `C/PactScreen.java:62`; `journal/Journal.java:95`; `concordance/Authority.java:55-140`;
  `rules/EvidenceRule.java:60-120`; the practice recorders (by grep); `compat/jade/JugcraftJadePlugin.java:33-52`;
  `tools/concordance_economy.py:20-75`; `tools/concordance_journey.py:82, 235-262`; `tools/acquisition.py:134-143`;
  `tools/concordance_progression.py:63, 84`; `tools/concordance_worker_models.py:1-40`; `tools/stolas_body.py:1-30`;
  `tools/check_mod_data.py`'s function positions; `docs/features/styxhexenhammer.md:37`;
  `docs/features/arcane-concordance-ember-regalia.md:280-302`; the menagerie catalog's mount ids and lang (no unicorn,
  bull, serpent-man or treant identity).
- **Re-read for the critique pass (26 October 2026):** a critique of the first draft raised 27 corrections; each was
  checked against the code before it was applied: `S/WorkerEntity.java:180-205`; SmartBrainLib's `LookAtTarget`
  (`invalidTarget`, `invalidateTargetIf`, `checkExtraStartConditions`) and `NearbyPlayersSensor` (a reference copy whose
  version is unrecorded); mc263src `EntityTracker.java:40-60` and `StreamCodec.java` (largest `composite`: 14, `:533`);
  `S/PactStatePayload.java:30-102`; `tools/check_mod_data.py:14890-14902` (`check_baselines`) and `:21750-21760`
  (`ENTRY_BY_ENTRY`); `G/LivingDeviceBlockEntity.java:45-65`, `G/LivingDeviceBlock.java:35-90`,
  `G/VerdantBedBlock.java:120-140`, `Garden.knows`; `S/AgreementHolder.java:238-252, 505-525`;
  `S/StolasEntity.java:280-305`; `concordance/Authority.java:72-75`; `S/PactMenu.java:290-303, 480-495, 514`;
  `S/PactCompact.java:46-76`; the lang's `term.compact`, `answer.cannot_afford`, `term.quota`; the Vorbis channel count of
  every file of every cue event named in the art plan and of the three stereo families; the Decor Blossom's cubes;
  GeckoLib `GeoRenderer.java:68-80`; `docs/ART_DIRECTION.md:31`; `tools/art_check.py`'s model globs;
  `tools/owner_art.py:70-80, 130-140`; the second edited part's `files.json` entries and the main catalog's CSV rows and
  SHA-256 of every library file named; the average colours of the candidate palette files;
  `G/VerdantHeartBlockEntity.java:92-104, 135-158`; `G/VerdantBedBlockEntity.java:170-200, 463-480`;
  `E/Verdict.java:35-60`; `E/Habitat.java`; `CD:2700-2716, 2770-2790`; `T/ConcordanceWorkerGameTests.java:2124,
  2165-2200, 2955`; `~/.gradle` (no caches); `.github/workflows/build.yml:1-20`;
  `tools/concordance_progression.py:515-535`; `S/WorkerRoster.java:25-45`; `W/Status.java`;
  `concordance/Examination.java:60-102`; `C/ConcordanceClient.java:128-137`; `W/PactParser.java:38`;
  `G/OrganismCropBlock.java:300-328`; `distribution/frameworks.lock.json` (SmartBrainLib 2.0.3); `survey-sources.md`
  §1, §3, §4; `survey-assets.md` §3a, §3b; the plan's 09:1-20 and 03:160-192.
- **Looked at:** the asset survey's preview sheets (`look/sil1.png` to `sil3.png`, `textures1.png`, `textures2.png`).
- **Read by REST on 26 October 2026 (read only):** the open pull requests (20), main's head `7317e5d44`, and the file
  lists of every open pull request but the stack's for the cohort's files; locally, the diff from the merge-base to
  `7317e5d44` (its stat and its `Authority.java` and `ConcordanceProgress.java` changes).
- **Taken from the surveys without re-reading, marked *(survey)*:** `StolasEntity`'s internals and line numbers (but
  `decide`, re-read), `PactKeeping`'s rows, `PactReading`, `WorkerDataProvider`, `WorkerClient`, the vanilla and GeckoLib
  rotation paths (SmartBrainLib's `LookAtTarget`, re-read), the owner library's files, catalog rows and hashes, every source quotation and its
  label, the Gleaner's measured search cost, Peepo's work, the crow code, `Transfers` and `CourierWork.reserve`'s chunk
  scan.
- **Unit 1's records (28 October 2026):** the CI runs 40421207295, 40597482976 and 40605564166 and main's
  40596966503 read through GitHub's REST API (jobs, attempts) and its job-log tool (each job's last 5,000 lines: the
  server tests' summaries and failures, the client shards' `[garden-check]` and `[pact-check]` lines and pictures);
  the 33 pictures of `ConcordanceGardenPactClientGameTests` (shard 0) and Stolas's four facing pictures (shard 3) of
  run 40605564166 extracted from the logs and looked at one by one; the pull requests #465, #466, #474, #494 and #526
  by REST; the unit's commits and messages; the code lines the As built notes name. Gutenberg's record #72679 could not
  be read (the host was not reachable from the session).
- **Unit 2's records (28 October 2026):** CI run 40777920181 read through GitHub's REST API (jobs) and its job-log tool
  (the `mod`, `optional integrations absent`, client-selection and shard 0 jobs' last 5,000 lines: the server tests'
  summaries, the selection, the garden class's `[garden-check]` lines and pictures); Bathin's 34 pictures of
  `ConcordanceGardenPactClientGameTests` (shard 0) extracted from the log, compared byte for byte with the log's previews
  and looked at one by one; the unit's commits and messages; the unit's build and review reports kept with the
  session's working notes; the code lines the As built notes name; `python3 tools/owner_art.py --provenance pacts`.
- **Not done:** no scan of the 1904 text was opened; nothing was built, generated, run or tested (no local game build
  exists); the default `LookAtTarget` predicate was read from an unversioned reference copy, not the pinned jar's
  sources; no repository file was edited.

## Critique corrections not applied

None of the critique's 27 corrections was rejected: each held against the code and the surveys, and each is applied
above. Where a correction offered alternatives, or named an example, the choice made and why:

- **1 (facing):** applied; the critique's request to confirm the default predicate against the pinned
  `smartbrainlib-fabric-26.3-2.0.3` sources could not be done here (no jar or sources of that version locally), so it is
  an explicit step of GC-02 before the change is written.
- **7 (the per-work quota wording):** applied with the key family `term.quota.<work>`; only Amdusias's is shipped in this
  cohort, the generic key staying the fallback for the other five.
- **9 (unit 1's scope):** applied as written, including keeping `cachedHabitat` in unit 1, although unit 1 itself does
  not call it: units 3, 4 and 6, built in parallel from unit 2's head, all read it, and its own test
  (`aCachedHabitatNeverSamples`) exercises it.
- **10 (Buer's twin faces):** the critique's choice, dropping one face of each twin, rather than the 0.05-pixel lift
  `docs/ART_DIRECTION.md:31` also allows: under `entityCutout` one quad is seen from both sides, and the leaf plate and
  pistil cap are rarely seen from below.
- **11 (palette files):** `Blocks/biomes and tree blocks/glowing_moss_block.png`, one of the critique's examples, is not
  used for moss: its colour is blue (average `#255779`). `spanish_moss.png` (average `#94b684`) is named instead; the
  critique's `fir_log.png` is kept for Foras's robe, and its petal-block example is replaced by `white_petals.png` for
  Amdusias's coat. Both suggested remedies are applied: concrete files named now, and "palette files in the brief
  before body files" as an acceptance line.
- **17 (mastery before unit 3):** both remedies combined: the reachability test asserts only up to understood in units 1
  and 2, and the progression tool counts shipped garden definitions for this one practice. Honouring `distinct` for
  every practice was not chosen, because it would also change how the tool reads Verdant Husbandry's `cultivation`
  rule (`distinct: 4`), outside this cohort's scope.
- **18 (showing a complication on the roster):** the preferred option, an appended `Status.AWAITING_ANSWER`, not a
  `RosterStatePayload` field; it lands with its first user in unit 2, not unit 1.
- **19 (client work for two details):** the synced target and renderer hook for Marax's familiar (unit 3, his signature
  detail); Bifrons's two-beat measuring re-worded to bounded server particles at source and destination, so units 3 and
  5 stay independent and Bifrons needs no client hook.

Also corrected while applying the critique, though it did not raise them: the two-client scenario no longer says the
peer's opening of the holder's perch is refused (a stranger may open it and is refused only its requests, as
`ConcordancePactNetworkTests` shows for Stolas, so Buer can turn to the peer); `MP` (the plan's master prompt) is now
defined among the citations; the survey reports are named as the session's working notes, since they are not committed
beside this page.
