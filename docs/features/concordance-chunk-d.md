# Concordance Chunk D: the pact hall (contract and handoff)

Status: unit 1 (D-01, D-02) built on this branch (not merged; not yet pushed when its As built notes were written), its
local checks run and passed; none of its Java has been compiled against Minecraft and none of its game tests has run
(no Minecraft build where it was written; CI is its first compile). Units 2 to 5 are not started. Chunk D is developed
on branch `claude/chunk-d-pact-hall` (local `wip/chunk-d`), started from
Chunk C's head `779851cdb` ([#466](https://github.com/jimbozoomer-byte/jugcraft/pull/466), itself stacked on Chunk B's
[#465](https://github.com/jimbozoomer-byte/jugcraft/pull/465)); when it is ready it is reviewed as a third stacked pull
request against `claude/chunk-c-living-workshop` (D-D1). Not merged; Chunks B and C beneath it are not merged either.
Proposal: the owner chose Chunk D on 23 October 2026 ("begin chunk d"), from
[the expansion plan's delivery chunks](../plans/concordance-expansion-2026-10-10/09-claude-delivery-chunks.md) (section 6,
D1 to D4, 09:146-174), with [workshops, ecology and pacts](../plans/concordance-expansion-2026-10-10/03-workshops-ecology-and-pacts.md),
[magic progression](../plans/concordance-expansion-2026-10-10/02-magic-progression-and-spellcraft.md),
[technology and economy](../plans/concordance-expansion-2026-10-10/06-technology-and-economy.md),
[interfaces](../plans/concordance-expansion-2026-10-10/08-interfaces-animation-and-assets.md) and
[validation](../plans/concordance-expansion-2026-10-10/10-validation-and-checkins.md). The same day the owner chose the
branch (D-D1) and said "dont ask me to approve just finish this whole part" (D-D2): the session is not to stop for
approvals and is to finish Chunk D end to end. The session therefore settles the plan's open questions itself, taking
its delivery plan's recommendations as corrected by the plan's completeness critique, and records each choice as
D-S*n*, "Decided by the session under D-D2". **No D-S decision is the owner's.** Each stays open to the owner's review at
the checkpoint, which asks at most three questions (09:18).
Owner: @jimbozoomer-byte. AI-assisted implementation with Claude Code; the model is named in the commit trailers.

This page is the chunk's contract (chapter 9 §1 step 7 of the plan) and, as the units land, its technical handoff
(chapter 10 §7). Plan chapters are cited as `NN:line` (`docs/plans/concordance-expansion-2026-10-10/NN-*.md`); `MP` is
the plan's `CLAUDE-MASTER-PROMPT.txt`; `CC` is [Chunk C's contract](concordance-chunk-c.md). Code is cited as
`file:line` at `779851cdb`: Java paths are under `src/main/java/io/github/jimbozoomer/jugcraft/` (the Concordance's under
its `concordance/`), Peepo's under `src/main/java/` (`local/peepo/...`), client code under
`src/client/java/io/github/jimbozoomer/jugcraft/client/`, data under `src/main/resources/data/jugcraft/`, tests under
`src/gametest/java/io/github/jimbozoomer/jugcraft/test/` (Peepo's under `src/gametest/java/local/peepo/`), and
`mc263src` is the decompiled 26.3 reference (its `HEAD`). A fact marked *(survey)* comes from this chunk's seven survey
reports, its delivery plan or the plan's critique and was not re-read for this page. **D-D*n*** is the owner's decision;
**D-S*n*** is the session's, under D-D2.

## Player outcome

The plan's outcome: "Stolas is discoverable through Styx, has a distinct character, negotiates a clear agreement,
performs a useful service and leaves safely when the player ends it" (09:150); the chunk map's line is "Meet Stolas,
negotiate, use and safely end a working pact" (09:29). Corrected for what is already built (below), and with the
session's decisions D-S1 to D-S23, a survival player can:

- **Keep the agreements already in the game (unit 1):** a Gathering Shade that is killed leaves its load once, and the
  holder calls a new one under the same agreement at no cost; a failed seal says why; a new shade is on the roster at
  once; only a new anchor's owner or the owner's party seals it; a familiar no longer fades for days its person was
  logged out; and the holder can end an agreement without breaking its block.
- **Discover (unit 2):** talking to Styxhexenhammer gives that player a hint drawn from their own research progress
  (talking teaches nothing); a player without Styx finds the same entry, Stolas's Regard, through the codex's Binding
  category and its specimens. The entry asks for three kinds of personal evidence through research that already exists:
  examining a specimen (a Styx flower or a feather), harvesting an organism crop (Verdant Husbandry's practice) and
  dissolving a sample at the Assayer's Scale (the Assay's practice); the invitation also asks for one sky observation
  recorded at an observatory.
  Notes never stand in.
- **Negotiate (units 1 and 2):** the player builds a pact perch where they may build, at their own base, and invites
  Stolas. He arrives at the perch, and a conversation screen shows his terms before anything is spent: the service,
  hours, area, daily quota, upkeep and grace, what suspension does, how long the pact lasts (until ended), the breach
  rules, how to end it, what comes back, who benefits and what the Open Hand Compact costs. Accepting spends the
  compact's cost in the same server action that seals the pact. Declining, or any refusal, spends nothing and names its
  reason.
- **Use (unit 2): Star-reckoning (D-S1).** Once a game day, at the perch, the holder chooses any sky pattern they have
  personally observed and recalls it with their astrolabe's stored resonance, at that pattern's existing recall cost and
  under the same once-per-occurrence claim as a master's recall: no waiting for the 19-day Echo Comet or a clear night.
  The effect lasts 2,400 ticks wherever the holder goes.
- **Keep (units 1 and 2):** upkeep is one offering every 7 game days, paid into the pact's escrow (a Styx flower, or an
  amethyst shard), up to four periods ahead; the compact pays the first. A missed offering shows a warning, then a full
  game day of grace, then suspension: the service stops, nothing is taken and nothing is lost. Paying resumes it. Time
  the holder was away, or the perch unloaded, is never owed, and a pact never ends by itself.
- **End (units 1 to 3):** the holder ends the pact at the perch or from the roster. Stolas makes his departure gesture
  and leaves; the escrow comes back once to the holder's inventory; the reason is shown and kept. Breaking or blowing up
  the perch never ends the pact: Stolas leaves the world, and the holder seats the pact at a new perch or ends it.
- **Manage (unit 3):** a roster screen lists every familiar, spirit, construct and pact the player keeps, with its
  state, next obligation and waiting reason, and suspend, resume, end, cancel and recover actions that go through the
  existing authorities; the journal gains a "pacts" section.
- **Contrast (unit 4, D-S2):** mastering Stolas's Regard, by fulfilling the pact's first obligation, unlocks the
  **workshop hand**: a spirit sealed at a Spirit Anchor bound to the holder's Courier Post that carries that post's exact
  requests, the crucible's own formula shortage from Chunk C included, through the courier ledger.

Already built, and not rebuilt here:

| System | Where |
|---|---|
| Agreement terms and refusals (suspended, outside the agreement, finished); a day quota the clock cannot reset | `concordance/worker/Agreement.java:13-29, 43-50` |
| Bound Will record and ledger: seal, transfer, release; 32 a holder; `release` a second time is `UNKNOWN`; `transfer` has no game route | `concordance/resource/BoundWill.java:10`; `BoundWillLedger.java:16, 46-86`; `concordance/spirits/BoundWills.java:25-32, 60-76` |
| Worker roster: 32 a player, read while unloaded, written on a status change | `concordance/spirits/WorkerRoster.java:29, 69-97`; `WorkerEntity.java:93-99` |
| Worker base: think once a second, persistent, signs, `describe`, GeckoLib and SmartBrainLib, no portals, save stamp | `concordance/spirits/WorkerEntity.java:58-59, 71, 100-121, 183-189, 204-207, 216, 225, 243-258` |
| Status vocabulary: 5 intents, 14 reasons, worded by the generator | `concordance/worker/Status.java:10-31`; `tools/concordance_workers.py:48-68` |
| Spirit Anchor: seal, suspend, release, drop once; its slots unopenable by players | `concordance/spirits/SpiritAnchorBlockEntity.java:78-160, 219-222`; `SpiritAnchorBlock.java:48-78` |
| Authority: present, answering (the stand-in only when the server opts in), stand-in, mayChange | `concordance/Authority.java:59-65, 72-78, 106-108, 114-126` |
| Courier ledger (cargo held by the ledger, a courier holds its claim id), posts, the porter's courier decision | `concordance/courier/CourierLedger.java:34-42, 289-304`; `CourierPostBlockEntity.java:48-51`; `concordance/spirits/ClockworkPorterEntity.java:291-510, 586-602` |
| The crucible files its own courier requests (C-06, on Chunk C's branch only) | `concordance/CrucibleBlockEntity.java:475, 506`; failure table CC:1127-1136 |
| Sky: forecast command (no permission), observations, a master's recall of the last-observed pattern | `concordance/sky/Sky.java:310-319`; `AstralClaims.java:68-85`; `AstrolabeItem.java:107-145` |
| Upkeep precedent: one day a look, unloaded days owed nothing, a late look reschedules from now, nothing lost | `concordance/wonder/Spires.java:12-26, 152-175` (163-166) |
| Styx: an NPC hurt only by damage that bypasses invulnerability, a one-time gift, four rotating lines, never respawned | `styx/Styxhexenhammer.java:53-55, 71-90`; `styx/StyxState.java:17, 40-41`; `src/main/resources/assets/jugcraft/lang/en_us.json:12652-12657` |
| Styx flowers, renewable by bone meal | `styx/JugcraftStyx.java:24-25`; `styx/StyxFlowerBlock.java:31-36` |
| Diagnostic codes `UNAVAILABLE_WORKER` and `CONFLICTING_AGREEMENT`, declared and unused, parameters "fixed by their first user" (on Chunk C's branch only; main has no `diagnostic/` package) | `concordance/diagnostic/Code.java:31-34` |
| Journal sections: 24 at most, 13 used | `concordance/journal/Journal.java:53, 86-106`; the Academy's, `academy/JugcraftAcademy.java:47` |
| Encyclopedia: 11 pages of 11, read-only by audit | `guide/encyclopedia/Encyclopedia.java:61-75, 145`; `EncyclopediaPayload.java:27`; `tools/check_mod_data.py:19818` |
| An NPC-bound menu that re-checks every button | `town/ShopMenu.java:129-136, 143-160` |
| Native book widgets (`BookArt.Paragraph`/`Tab`, `ComposerScroll`) | `client/BookArt.java:69, 93`; `client/ComposerScroll.java` |
| Jade: one provider for every `WorkerEntity`; none for the Spirit Anchor | `compat/jade/JugcraftJadePlugin.java:28-47` (44) |
| A rite's result is `Transform` or `Effects` only | `concordance/ritual/RitualDefinition.java:56-71` |
| Equivalence exclusions (the bonding charm, porter key, astrolabe, research notes among them) | `data/jugcraft/tags/item/equivalence/excluded.json` |
| Test players that log out when their test ends; an ordinary survival test player | `TestPlayers.java:35`; `AlchemyArtifactGameTests.java:33-47` |

## Units

Each unit is a reviewable boundary: its own run of commits on `claude/chunk-d-pact-hall`, with its own records and
green checks before the next starts. The order puts the first end-to-end path (Styx, evidence, perch, conversation,
terms, acceptance, service, obligation, upkeep, end) first, in units 1 and 2, before Stolas's finished look, the roster
screen and the second service (09:16, "Implement the first end-to-end path before multiplying content variants"; MP:66).
Unit 1 lays what that path stands on (the shared holder's seal outcomes, ownership and holder-only end, the pact
record, its states and upkeep rules), together with the small routine fixes to the agreements already shipped; unit 2
completes the path. Nothing in units 3 and 4 comes before it.

| Unit | Tickets | What it delivers | Record (proposed) | State |
|---|---|---|---|---|
| 1 | D-01, D-02 | The existing agreements keep their promises (shade death and re-call, seal refusals, the roster at once, anchor ownership, the familiar's fade, ending without breaking); one pact record: the agreement holder extracted from the Spirit Anchor and shared, a place on the Bound Will, one source of truth for suspension, the upkeep rules, the departure history, the copy check and the offline, restart and feature-disable policy | `arcane-concordance-workers.md`; this contract | Built (not merged): D-01 and D-02 on `claude/chunk-d-pact-hall` (local `wip/chunk-d`; commits `56a74f4be`, `7477efbcd`, `fa5360d0d` and the records' commit). Local checks run and passed, 45 of 46 audit mutations reported (D-02's As built); `./gradlew build`, the server and client game tests and CI not run |
| 2 | D-03, D-04, D-05 | The first end-to-end path: Styx and the codex to Stolas's Regard and its evidence, the perch, Stolas's arrival, the conversation and terms, acceptance with the Open Hand Compact, Star-reckoning, the first obligation, upkeep, suspension, ending. A provisional body for Stolas is acceptable here | new `stolas-pact.md`; `styxhexenhammer.md`; `arcane-concordance-celestial.md` | Not started |
| 3 | D-06, D-07 | Stolas's character: the Flashjack-derived raven body, arrive, listen, inspect, assent, refuse and depart, sounds with subtitles, Jade; the roster screen with suspend, end, cancel and recovery, and the journal's pacts section | `stolas-pact.md`; `arcane-concordance-workers.md` | Not started |
| 4 | D-08 | The workshop hand (a courier agreement carrying a post's exact requests) and the schema review | `arcane-concordance-logistics.md`; `arcane-concordance-workers.md`; this contract | Not started |
| 5 | D-09 | Combined fixes, the whole-path test, load, client evidence, the two-client attempt, records, the checkpoint and the handoff | this page; `arcane-concordance-journey.md`; `docs/TESTING.md` | Not started |

The plan allows three to five units (09:3); this is five plus the baseline. The completion evidence E1 to E7 that every
ticket's acceptance names is defined, and mapped to tickets and tests, in [Completion evidence](#completion-evidence).

## Baseline (D-00)

### Facts

- **Base and main:** `HEAD` is `779851cdb` (Chunk C's head, committed 23 October 2026 19:08:54Z). `origin/main` is
  `f904df3fd` (#457), read again by REST at 22:15Z and 22:43Z on 23 October 2026; it is an ancestor of `HEAD`, so a
  merge of main would change nothing. The remote branch `claude/chunk-d-pact-hall` points at `779851cdb` (REST) and has
  no pull request.
- **The stack beneath:** #465 (Chunk B, head `5c6878efe`, base `main`) and #466 (Chunk C, head `779851cdb`, base
  `claude/chunk-b-winter-stone`) are open, mergeable (`clean`) and unreviewed. #465's 21 check runs passed; #466's 21
  passed and its `workshop load card` job was skipped (REST, 22:43Z).
- **Named suites, `@GameTest` count at `779851cdb`:**

| Suite | Tests | Covers today |
|---|---|---|
| `ConcordanceWorkerGameTests` | 5 | familiar, spirit, porter; a save of the familiar and porter (its Javadoc also claims "a shade's anchor", which it does not test: `:205-207`) |
| `ConcordanceLogisticsGameTests` | 15 | the courier ledger and the crucible's requests |
| `ConcordanceSharingGameTests` | 2 | research notes and the sconce only (`:76`, `:135`) |
| `ConcordancePersistenceGameTests` | 10 | legacy records (`:141-155`, a `BoundWill` built at `:147`); block-entity stamps without the anchor (`:190-209`) |
| `ConcordanceSkyGameTests` | 6 | observatories, attunement, a master's recall (`:206`), the sky through a save |
| `StyxGameTests` | 7 | gifts, growth, pots, codec, blueprints (frozen, `:82-90`), preview, the district opt-in |
| `ConcordanceAuthorityGameTests` | 5 | light, harm, a porter's keeper, a Spire Heart's store that hoppers cannot drain (`:243`), links |
| `ConcordanceResourceGameTests` | 4 | `boundWillAstralAndPrimaKeepTheirShape` (`:126`; `BoundWill` built at `:129`, `:135`) |
| `ConcordanceJournalGameTests` | 5 | sections shown by research |
| `ConcordanceProgressionGameTests` | 8 | the progression graph |

- **Client companion tests:** `src/gametest/java/local/peepo/PeepoCompanionClientTests.java` and
  `CompanionAutomationChecks.java` exist. No client test shows a Hearthling, Gathering Shade or porter
  (`docs/features/arcane-concordance-workers.md:191-192`).
- **No pact system exists:** no Stolas entity, pact data, invitation or negotiation in `src/` or `tools/`. "Stolas"
  names only the flower `stolas_starflower` (`styx/JugcraftStyx.java:24`), Styx's line 0 (`en_us.json:12654`, written by
  `tools/styx.py:193-202`), a comment on the conservatory's shrine (`tools/styx_structure_v2.py:184`) and the docs.
- **Owner direction outside the pack** (`docs/ROADMAP.md:26-34`, 5 October 2026): all 72 spirits; each with "clearly
  stated pact benefits, costs, limits, duration, and exit/breach rules"; "Players must knowingly accept pact terms;
  another player cannot bind them without consent" (`:29`); a data-driven roster (`:30`); "Keep the current decorative
  flowers usable before pacts exist" (`:31`); server authority, protected-area rules, concurrent summons, entity caps,
  restart and unload recovery and save compatibility designed before implementation (`:32`).
- **Design source for Stolas:** "The source describes a raven form; no unrelated contemporary adaptation supplies the
  character design" (`docs/features/styxhexenhammer.md:37`).
- **Test players:** vanilla's mock player has a random identity and always reports creative mode: its `gameMode()` is
  overridden to `CREATIVE` (`mc263src` `net/minecraft/gametest/framework/GameTestHelper.java:421-432`), and
  `Player.isCreative()` reads it (`mc263src` `net/minecraft/world/entity/player/Player.java:1541-1543`), so setting
  survival on it changes nothing a creative check reads. `AlchemyArtifactGameTests.java:33-47` already makes an ordinary
  `ServerPlayer` for that reason. `TestPlayers.mock` (`TestPlayers.java:35`) logs its players out when their test ends.

### Status delta: the plan's text against the code at `779851cdb`

| The plan says | What is true | Evidence |
|---|---|---|
| "Use existing agreement/Bond/BoundWill concepts" (03:142) | All three exist as separate models (the familiar's bond, the shade's agreement, the porter's body) sharing `WorkerEntity`, `Status` and `WorkerRoster` | `worker/Agreement.java:13-14`; `worker/Bond.java:10`; `resource/BoundWill.java:10`; `spirits/WorkerRoster.java:27-29` |
| The contract's fields (03:144) | Present: agreement id (`BoundWill.id` = `Agreement.record`), owner (`holder`), service (`work`), boundary (dimension, centre, radius), a day quota. Absent: beneficiaries, upkeep, fulfilment evidence, any state but `suspended`, departure reason, presentation profile. The spirit id is `counterpart`, written unnamespaced by the anchor (`"gathering_shade"`) while tests use namespaced ids | `Agreement.java:13-14`; `SpiritAnchorBlockEntity.java:83`; `ConcordanceResourceGameTests.java:129` |
| Six states (03:146); four (09:154) | Only `suspended` is stored, on the anchor's block entity; waiting is a derived `Status`; departure removes the record and keeps no reason | `Agreement.java:13-14, 52-54`; `SpiritAnchorBlockEntity.java:105-131` |
| "Missed upkeep suspends the benefit with a visible warning and a grace period. No offline punishment" (03:146) | No worker has upkeep. The Spire's is the precedent: at most one day a look, unloaded days owed nothing, a late look reschedules from now. The familiar's bond fades for days its person was logged out (G7) | `wonder/Spires.java:12-26, 152-175`; `worker/Bond.java:33-39` |
| "A stored sky route avoids calendar lock" (09:158) | A master may recall, only when nothing up is visible where they stand and only the pattern they observed last, once an occurrence under `recall:<pattern>`, for 2,400 ticks; a recalled attunement ignores its window and season | `sky/AstrolabeItem.java:107-131, 134-145`; `celestial/Calendar.java:18`; `sky/Sky.java:298` |
| "A recorded sky occurrence through existing research" (03:131) | An observatory records the observation (`AstralClaims.observe`) and `observation` practice for anyone who uses it while a pattern is up and visible, with no research check; only becoming its keeper asks `Sky.knows`. So the sky step has no research depth, and `ResearchEngine.explain` does not cover it | `sky/ObservatoryBlockEntity.java:187-220` |
| "Prepare three evidence tokens" (03:132) | No token items exist; evidence is per-player knowledge keys, and notes never reach mastered | `rules/EvidenceRule.java:74-76` |
| The Open Hand Compact, a rite (03:102, 03:133) | A rite's result is `Transform` or `Effects` only | `ritual/RitualDefinition.java:56-71` |
| Invitation and terms before acceptance (03:134; 08:67) | The anchor seals on its first use and prints the terms afterwards; a failed seal says nothing | `spirits/SpiritAnchorBlock.java:50-60` |
| Roster page with cancel and recovery (09:162; 08:65-69) | Text only: the journal's workers section and a command; the record lists a screen as open | `journal/Journal.java:91`; `spirits/Workers.java:133-146`; `arcane-concordance-workers.md:145-146` |
| An encyclopedia "Pacts" page (08:16) | 11 pages registered of 11 allowed; a twelfth is dropped | `EncyclopediaPayload.java:27`; `Encyclopedia.java:61-75, 145` |
| GeckoLib vocabulary: idle, listen, inspect, assent, refuse, departure (03:159) | Workers loop one clip per status; no worker has a one-shot gesture; `check_geckolib` checks only `thenLoop` clips | `WorkerEntity.java:243-258`; `tools/check_mod_data.py:17258-17267` |
| Agreement diagnostics | `UNAVAILABLE_WORKER` and `CONFLICTING_AGREEMENT` are declared and unused; the package is absent from `origin/main` | `diagnostic/Code.java:31-34`; `git ls-tree origin/main` |
| "The task record should refer to an authoritative claim" (03:191) | True of the porter in courier mode and of the crucible's own requests (C-06, #466 only) | `CourierLedger.java:34-42`; `CrucibleBlockEntity.java:475, 506` |
| Stolas, owl or raven art | None in the owner's library; the nearest unused bird rig is Ars Elemental's Flashjack | assets survey §1, §4.1 *(survey)*; the files, read for this page, in D-06 |

### Defects and gaps found (D must not inherit them)

| # | Gap | Evidence | Fixed by |
|---|---|---|---|
| G1 | A Gathering Shade that is killed loses what it carries: it has no `remove` override, its load lives on the entity, and its roster entry, the anchor's `spirit` and the Bound Will go stale. The anchor then says only "spirit away", and nothing calls a shade back | `GatheringShadeEntity.java:51` (no `remove`; the porter's is `ClockworkPorterEntity.java:586-602`, the familiar's `HearthlingEntity.java:146-153`); `SpiritAnchorBlock.java:72-77` | D-01 |
| G2 | A copied anchor (its block entity's data at a second place) is never checked against the ledger. Breaking the copy releases the original's Bound Will (release asks only record and holder), sends the original's shade away and strikes it off the roster (both copies name the same `spirit`), and drops the copy's contents. Every existing Bound Will has no place, so a place check alone would miss every existing copy | `SpiritAnchorBlockEntity.java:116-131, 154-160`; `BoundWills.java:25-29` | D-02 (with a legacy claim rule) |
| G3 | Who may seal: inside a claim a stranger already cannot use another's block (`UseBlockCallback`), but outside claims anyone with the Binding Arts may seal an anchor someone else placed, and no anchor records who placed it | `finance/LandProtection.java:32-37`; `SpiritAnchorBlock.java:48-59`; `Authority.java:114-121` | D-01 |
| G4 | A failed seal is silent (a message only on success) | `SpiritAnchorBlock.java:56-59` | D-01 |
| G5 | A newly sealed shade is missing from the roster until its status changes (`seal` never notes it; the roster is written only on a change). A roster at 32 silently ignores a new worker, and that 32 is shared with Nouveau helpers and Starbuncles; Bound Wills have their own 32 | `SpiritAnchorBlockEntity.java:78-103`; `WorkerEntity.java:93-99`; `WorkerRoster.java:69-76`; `nouveau/NouveauHelpers.java:40`; `nouveau/NouveauItem.java:33`; `BoundWillLedger.java:16, 50` | D-01 (a `ROSTER_FULL` refusal) |
| G6 | The holder can end an agreement only by breaking the anchor, though the agreement says the holder may release it | `SpiritAnchorBlock.java:48-78`; `Agreement.java:10` | D-01 (the method), D-07 (the action) |
| G7 | A familiar's bond fades for days its person spent logged out. `neglected` counts every game day since `lastSeen`, and a familiar only thinks while loaded: offline and other-dimension paths never move `lastSeen`, and a familiar is usually unloaded while its person is away. This contradicts `Bond.java:6-7` ("online elsewhere"). A routine adjacent fix, not E2 evidence: the bond is not upkeep | `worker/Bond.java:33-39`; `HearthlingEntity.java:100-114`; `WorkerEntity.java:183-189` | D-01 (D-S17) |
| G8 | Stale record: it says an offline holder's shade keeps working; the code waits | `arcane-concordance-workers.md:213-214` against `GatheringShadeEntity.java:148-155` | D-01 |
| G9 | `counterpart` is unnamespaced for the shade | `SpiritAnchorBlockEntity.java:83` | D-02 (new records only) |
| G10 | The anchor is missing from the stamp test, and the save test's Javadoc claims a shade's anchor it does not test | `ConcordancePersistenceGameTests.java:190-209`; `ConcordanceWorkerGameTests.java:205` | D-02 |
| G11 | No Jade provider for the Spirit Anchor | `JugcraftJadePlugin.java:28-47` | D-06 |
| G12 | Styx is absent from old saves (the district defaults off) and from worlds without a fresh town or an operator's placement, and is never respawned | `town/TownState.java:36`; `StyxState.java:17` | D-03 (the non-Styx route) |
| G13 | Nouveau helper kinds (`nouveau_<species>`) have no roster kind lang, so the roster shows raw keys | `nouveau/NouveauHelper.java:42`; `Workers.java:142` (lang has only `familiar`, `spirit`, `construct`, `starbuncle`) | D-07 |
| G14 | Suspension would have two sources of truth once the Bound Will holds a state: the anchor's `Agreement.suspended` cannot be read or changed from a roster while the anchor is unloaded, and the status text is "suspended by its holder", wrong for upkeep | `SpiritAnchorBlockEntity.java:48, 105-113`; `tools/concordance_workers.py:63` | D-02 (D-S9) |
| G15 | The anchor is a `WorldlyContainer` whose bottom face hoppers may empty, and an explosion removes it as breaking does: the removal releases the agreement and drops its slots, so an explosion ends a shade's agreement (its loot table's `survives_explosion` governs only the anchor's own item). Unchanged for the anchor, whose slots are deliveries meant to be emptied; the perch avoids both by holding nothing (D-S6) | `SpiritAnchorBlockEntity.java:38, 154-177`; `loot_table/blocks/spirit_anchor.json`; `mc263src` `net/minecraft/world/level/block/state/BlockBehaviour.java:187`, `net/minecraft/world/level/chunk/LevelChunk.java:290-293` | D-04 (for the perch); Risks (the anchor) |

**Record defects, fixed when each unit touches its record:** `arcane-concordance-workers.md:213-214` (G8) and its
open item for a management screen (`:145-146`), closed by D-07.

### Open pull requests and conflicts (REST, 23 October 2026 22:15Z, re-read 22:43Z; refresh before each unit, 09:11)

20 open at this page's first check (22:15Z), two more than the delivery plan's read: #471 (opened 21:44:05Z, stacked on
#469) and #472 (opened 22:10:19Z). 21 open at the re-read (22:43Z): #473 (opened 22:16:51Z, stacked on #470). File lists
for every row below were read by `/pulls/N/files` at 22:43Z, except #461's: its 30,836 changed files exceed what that
listing returns, so its row is the PR survey's. In those lists, no open pull request touches Concordance Java under
`spirits/`, `worker/`, `resource/`, `sky/` or `courier/`, Styx, the journal, the research data or `TestPlayers` (#461
by the survey only); only #462 touches Concordance generators (`tools/concordance_alchemy.py`,
`tools/concordance_ember.py`).

| PR | What it is | Files it shares with D's likely edits | Bearing on D |
|---|---|---|---|
| #465, #466 | Chunks B and C, D's base | everything | Conflict by construction: a change to either in review means a merge into D. `diagnostic/`, C-06's crucible requests and `TestPlayers.mock` exist only in #466 (Chunk C's branch) |
| #452 (fork) | Peepo machine support, tree taps, spawn rates; adds `LandRegistry.mayBuildFor` (`finance/LandRegistry.java`). Changes `src/main/java/local/peepo/MobTransportCrate.java`'s `permitted` (adding `LandRegistry.mayBuild`), not `friendly`, whose MISC rule refuses workers | `.github/workflows/build.yml`, gametest `fabric.mod.json`; Peepo gametest files (`CompanionPopulationChecks.java`, not `CompanionAutomationChecks.java`) | D edits nothing under `src/main/java/local/peepo` (D-S20). D's crate test reads `friendly` and `capture`, whose refusal of MISC #452 leaves as it is |
| #461 | Aircraft, furniture, death graves: a mixin captures a player's death drops *(survey)* | lang, `values.json`, `check_mod_data.py`, `generate_material_data.py` *(survey)* | The escrow returns only into a living holder's inventory (D-S6). D's return tests cannot run with #461 on D's branch; re-run them after #461 merges |
| #453 (draft) | Keep inventory in lair deaths | `CHANGELOG.md`, `docs/WHAT_EXISTS.md`, lang, `tools/encyclopedia.py` | As #461 |
| #463 (fork) | Visitor combat: wild `Enemy` mobs fightable on others' claims unless owned (its `mayFightWithoutClaimAccess`) | gametest `fabric.mod.json`; `finance/LandProtection.java` | Stolas and the workshop hand are `WorkerEntity`s, neither `Enemy` nor `OwnableEntity`, so they stay protected on claims |
| #467 (fork) | Storage crates; an opt-in two-client harness (`StorageCrateMultiplayerHarness.java`, `tools/storage-crate-multiplayer.init.gradle`) | lang, encyclopedia `en_us.json`, `values.json`, survival registry, gametest `fabric.mod.json`, `check_mod_data.py`, `generated-material-resources.json`, `JugcraftJadePlugin.java`, `JugcraftJadeClient.java`, `JugcraftClient.java`, `docs/TESTING.md` | Textual risk in the Jade plugin and client (D-06). Its harness is an unmerged fork contribution and is not imported (D-S22) |
| #462 (fork) | Recipe conflicts (`RecipeConflictGameTests`, `FoodRecipeConflictGameTests`) and the stirring rod's recipe | `values.json`, `check_mod_data.py`, `generated-material-resources.json`, gametest `fabric.mod.json`; `tools/concordance_alchemy.py`, `tools/concordance_ember.py` | The perch's recipe must pass its fixture once merged |
| #460 (fork) | Arrow stacking, Stalker contact damage, missing JEI recipes | gametest `fabric.mod.json` | Hot files only |
| #469 | `ScreenKit.Fitted` and a screen-fit check | `CHANGELOG.md`, `docs/WHAT_EXISTS.md`, `ScreenKit.java`, gametest `fabric.mod.json`, lang | D's screens adopt `Fitted` if it merges first |
| #470 (on #459) | Guns 14F | `sounds.json`, lang, `values.json`, gametest `fabric.mod.json`, `generated-material-resources.json`, `CHANGELOG.md`, owner-library catalog | `sounds.json` is generated: D's events go through the generator (D-S5). D does not edit the catalog |
| #471 (on #469; new) | The benches in the workshop look | `CHANGELOG.md`, `docs/WHAT_EXISTS.md`, `ScreenKit.java`, `LampwrightBenchScreen.java`, lang | `ScreenKit` contention if D adopts `Fitted` |
| #472 (new) | The Crowfield and the Harvest Crown (the Pumpkin King, part 1) | `CHANGELOG.md`, lang, `values.json`, gametest `fabric.mod.json`, `check_mod_data.py`, `generated-material-resources.json` | Hot files only; no Concordance, sky or worker file |
| #473 (on #470; new at the re-read) | Guns 15A | `sounds.json`, lang, `values.json`, gametest `fabric.mod.json`, `generated-material-resources.json`, `CHANGELOG.md`, owner-library catalog | As #470 |
| #458 (draft fork) | Retro Trader faces and gestures | lang, `sounds.json`, `JugcraftClient.java`, gametest `fabric.mod.json` | Reference only; hot files |
| #468, #459, #455, #454, #449, #448 | Seasonal harvest, guns 14E and 14D, the seasonal biome audit, quartz, Frostward College | #468 and #454: `CHANGELOG.md`, gametest `fabric.mod.json`, `check_mod_data.py`, `generated-material-resources.json`; #459 and #455: `CHANGELOG.md`, lang, `values.json`, `sounds.json`, gametest `fabric.mod.json`, `generated-material-resources.json` (#455 also `check_mod_data.py`); #449: lang, `values.json`, survival registry, gametest `fabric.mod.json`, `check_mod_data.py`, `generated-material-resources.json`; #448: `JugcraftClient.java` | Hot files only |

**Hot shared files** (from the lists at 22:43Z): lang `en_us.json`; the gametest `fabric.mod.json`;
`alchemy/values.json`; `docs/audits/survival-item-registry.json` (#449, #467); `tools/check_mod_data.py`;
`tools/data/generated-material-resources.json`; `CHANGELOG.md`; `sounds.json` (generated; #455, #458, #459, #470,
#473); `JugcraftJadePlugin.java` and `JugcraftJadeClient.java` (#467); `JugcraftClient.java` (#448, #458, #467; #461
*(survey)*); `docs/WHAT_EXISTS.md` (#453, #469, #471); `docs/TESTING.md` (#467); `docs/INTEGRATION_STATUS.md` (D edits
it; no open pull request in these lists does).

### Checks before each unit lands

1. Refresh main and the open pull requests (REST, read only); merge main if it has moved past `f904df3fd`, and merge
   Chunk C's branch if it has moved in review.
2. Run the generators the unit changes (`tools/generate_material_data.py` and those it calls): the generated JSON must
   be current (`docs/TESTING.md:5`).
3. `python3 scripts/check_repository.py`, `python3 tools/check_mod_data.py`, `python -m unittest discover -s scripts/tests`.
4. `./gradlew build` (the server game tests); `-PjugcraftOptionalIntegrations=false` for every unit that changes Jade
   code or another optional boundary.
5. `./gradlew runClientGameTest -PclientTests=...` for the classes `tools/select_client_tests.py` picks; with a new
   `WEIGHTS` entry, also `python -m unittest discover -s scripts/tests -p test_client_test_selection.py`.
6. `python3 tools/concordance_journey.py --check`; `tools/check_icon_maps.py` for new icons;
   `python3 tools/owner_art.py --check` when the owner's files are imported; `git diff --check`.
7. CI by workflow_dispatch of the Build workflow, every client class, as Chunks B and C did.

Each is reported on its own as run and passed, run and failed, or not run. Nothing has been run for Chunk D.

## Tickets

### D-01. The existing agreements keep their promises (unit 1)

**What the player sees.**
- A Gathering Shade that is killed puts its load into its anchor's slots when the anchor is loaded and still names it,
  otherwise drops it once where it fell (D-S18). The anchor then says the spirit was lost, and the holder's next use of
  the anchor calls a new shade under the same agreement and Bound Will, at no cost.
- A shade re-called while the old one was merely unloaded: the old one, when it loads, finds it is no longer its
  anchor's spirit, delivers its load into the anchor once and departs.
- A seal that fails says why: the Concordance is off; the Binding Arts are not understood; the anchor is not yours or
  your party's; you hold 32 agreements (`HOLDER_FULL`); your roster is full (`ROSTER_FULL`); the anchor already keeps an
  agreement.
- A new shade is on the roster at once.
- Only the anchor's owner or the owner's party may seal a new anchor (D-S8); an anchor placed before Chunk D, which
  records no owner, may be sealed by anyone who may change it (`Authority.mayChange`), as today plus that check; a
  stand-in never seals.
- A familiar fades only for its person's own play time away from it, never for days they were logged out (D-S17).

**Changes.**
- `concordance/spirits/GatheringShadeEntity.java`: a `remove(RemovalReason)` override on the porter's pattern
  (`ClockworkPorterEntity.java:586-602`): when the shade is destroyed, its load goes into its anchor through
  `SpiritAnchorBlockEntity.deliver` (`:134-151`) if the anchor is loaded and still names this shade, the rest (or all of
  it, the anchor unloaded) popped where it fell; the roster entry is struck. `depart` delivers into the anchor the same
  way instead of popping everything at it (`:197-207`), so a replaced shade delivers once.
- **"Lost" is read from the roster (D-S18):** the anchor's spirit is lost when its holder's roster no longer lists it,
  which the removal override guarantees for a dead shade; `level.getEntity(spirit) == null` means only "not loaded"
  (`SpiritAnchorBlock.java:72-77`).
- `SpiritAnchorBlockEntity`:
  - `seal` returns an outcome instead of null: `DONE`, `TAKEN`, `HOLDER_FULL`, `ROSTER_FULL`, `NOT_PERMITTED`,
    `DISABLED`, `UNKNOWN_RESEARCH`; it notes the shade on the roster at once (G5), and refuses `ROSTER_FULL` before
    sealing rather than letting `WorkerRoster.note` drop the note (`WorkerRoster.java:69-76`);
  - a re-call of a lost shade under the same record (no new Bound Will, nothing spent);
  - `end(ServerPlayer holder)`: holder only, once. It lands here with its test (the perch's end in D-05 uses the same
    method through the shared holder); the player reaches it for a shade from the roster in D-07. Until then, breaking
    the anchor stays the only in-game end for a shade (critique 22);
  - an optional `owner`, recorded when the anchor is placed (`setPlacedBy`); sealing asks the owner or the owner's party
    (`JugcraftParties.sameParty`, the Courier Post's rule, `CourierPostBlockEntity.java:48-51`) and
    `Authority.mayChange` at the anchor; without an owner, `mayChange` alone; `Authority.isStandIn` refuses.
- `SpiritAnchorBlock.use`: one message for each refusal (`message.jugcraft.concordance.workers.*`, from
  `tools/concordance_workers.py`).
- `HearthlingEntity` and `Bond` (D-S17): the bond stores its person's play-time statistic (`Stats.PLAY_TIME`,
  `mc263src` `net/minecraft/stats/Stats.java:28`, counted while they play, `ServerPlayer.java:715`) at the last visit, as
  an optional key `seen_played`. `neglected` counts whole days (24,000 ticks) of that play time since, so offline days
  never count, even when the familiar was unloaded all along; days played in another dimension still count, as
  `Bond.java:6-7` says. A bond without the key takes the current play time as its start and fades nothing on that look.
  The key must be optional: `HearthlingEntity` reads the bond with `orElse(Bond.NEW)` (`:179`), so a decode error would
  reset a familiar's bond.
- `docs/features/arcane-concordance-workers.md:213-214` corrected (G8).

**Reuses:** `WorkerRoster.note/remove`, `SpiritAnchorBlockEntity.deliver`, `Block.popResource`, `Authority`,
`JugcraftParties`, `RateGate` "workers", `tools/concordance_workers.py`.

**Tests (server):**

| Suite | New tests |
|---|---|
| `ConcordanceWorkerGameTests` | `aKilledShadeLeavesItsLoadOnce` (anchor loaded: into its slots; counts before and after; a second removal gives nothing); `aKilledShadeAwayFromItsAnchorDropsWhereItFell` (anchor unloaded); `aLostShadeIsCalledAgainUnderItsAgreement` (the same record id, no new Bound Will, nothing spent); `anUnloadedShadeReplacedLeavesOnceAndDeliversOnce`; `aSealedShadeIsOnTheRosterAtOnce`; `aFullRosterRefusesTheSeal` (`ROSTER_FULL`; no Bound Will made); `aFullHolderIsToldWhy` (`HOLDER_FULL`); `theHolderEndsWithoutBreaking` (once; a second end gives nothing; anyone else refused); `aFamiliarDoesNotFadeWhileItsPersonIsAway` (logged out across 3 game days, the familiar unloaded and then loaded; the same identity returns 20 blocks away; unchanged); `aFamiliarFadesWhileItsPersonPlaysElsewhere` (3 game days of the person's play time away: −12, tested for the first time) |
| `ConcordanceAuthorityGameTests` | `onlyTheOwnersSideSealsAnAnchor` (a stranger outside any claim refused; a party member allowed; an ownerless anchor: `mayChange`); `aStandInNeverSeals` |

`TestPlayers` gains `survivor(helper)`, an ordinary survival `ServerPlayer` built as
`AlchemyArtifactGameTests.player` builds one (`:33-47`) and logged out when its test ends, and `again(helper, uuid)`,
the same identity back after a logout (vanilla's mock gets a random identity each time). The existing
`aSpiritKeepsToItsAgreement` must still pass.

**Audits:** `check_workers` (the new message keys); `check_persistence` (the anchor's and the bond's optional keys keep
the stamp at 1).

**Acceptance:** E4 (a shade's death, unload and re-call return its load once); E1 (no stranger or stand-in seals
another's anchor). The familiar's fix is a routine adjacent fix and not E2 evidence (critique 6).

**Saves:** optional `owner` on the anchor; optional `seen_played` in the bond. Stamps stay 1.

**Review boundary:** fixes to existing behaviour only. No new id, block, entity or art.

**As built (unit 1: commits `56a74f4be`, D-01, and `7477efbcd`, D-S17; record
[arcane-concordance-workers.md](arcane-concordance-workers.md)).**
- **Built:**
  - `concordance/spirits/AgreementHolder` (new; the Spirit Anchor keeps one by composition and implements its `Holds`):
    the owner (`setOwner`, saved as the anchor's optional `owner`), `maySeal(level, player, partySeals)`, `seal`
    returning one `Pacts.Outcome`, the presence noted on its holder's roster at once, `spiritLost` (read from the
    roster, D-S18), `recall` (the same Bound Will, nothing spent; `ROSTER_FULL` refused), `end` (holder only, once: an
    instance method at the block and a static one by record id for D-07's roster), `toggle` and `broken`. The anchor's
    `AGREEMENT_CODEC` moved into it unchanged, so an anchor's save reads as before.
  - The pure `concordance/worker/Pacts`: `Outcome` (`DONE, DISABLED, NOT_PERMITTED, TAKEN, ALREADY_HELD, HOLDER_FULL,
    ROSTER_FULL, UNAVAILABLE, CANNOT_AFFORD`, mirrored by `SEAL_OUTCOMES` in `tools/concordance_workers.py` and each
    worded `message.jugcraft.concordance.workers.seal_<id>`), `refusal` (one reason, in that order), `holds`,
    `counterpart` (namespaced; records before G9 wrote none), `ENDED` and `BROKEN`; `WorkerRoster.full`.
  - `SpiritAnchorBlock`: `setPlacedBy` records the placer, never a stand-in; `use` says the switch, then a copy's
    `CONFLICTING_AGREEMENT` or an unreadable record (D-02), then seals (or says the one reason not), suspends or resumes
    (its holder, sneaking), calls a lost shade again (its holder), or reads its terms, state and spirit ("lost" or
    "away").
  - `GatheringShadeEntity`: a `remove(RemovalReason)` override (killed or discarded: its load into its anchor once, when
    loaded and keeping its own holder's agreement, otherwise where it fell; struck off the roster); `depart` and `leave`
    deliver the same way; it notes itself on its roster in `decide`, as its anchor's current spirit only.
  - D-S17: `Bond.seenPlayed` (key `seen_played`, read with `lenientOptionalFieldOf`, `UNSEEN` for a bond saved before),
    `neglected(played)`, `together(now, played)`, `restart(played)`; `HearthlingEntity` reads `Stats.PLAY_TIME` and
    counts the fade whenever its person is in its level, before a visit counts.
  - `TestPlayers.survivor`, `leave`, `again`, `played` and `play`; `mock` shares the end-of-test log-out.
  - Generators: `SEAL_OUTCOMES`, the D-01 messages, the anchor's tooltip, the spirits codex ("Its Terms" and a new "If
    It Is Lost" page) and "The Bond"; lang and the codex entry regenerated. G8's open item in the workers record
    removed.
- **Departures from this ticket and from the unit's specification** (proposals D-P6, D-P11 to D-P15 in Decisions):
  - the outcomes are the ticket's without `UNKNOWN_RESEARCH`, plus `ALREADY_HELD` (D-02's), `UNAVAILABLE` and
    `CANNOT_AFFORD`: "the Binding Arts are not understood" stays the anchor's own check before sealing, worded as before
    (`workers.unknown`), not an outcome (D-P6);
  - the holder was extracted once, in D-01's commit (not D-02's, as Dependencies and order planned), and extended by
    D-02, so the anchor was rewritten once;
  - `Holds.call(level, who, counterpart)` names the counterpart and returns a `WorkerEntity`; only `WorkerEntity`
    subclasses are presences (the review's corrections 7 and 8; D-P15);
  - `Sealing` carries `partySeals` (the anchor true, D-04's perch false, D-S8; correction 6; D-P6) and an optional cost,
    `affords` asked after the refusals and before anything is called, `pay` once the Bound Will is recorded, refused as
    `CANNOT_AFFORD` (correction 10; D-P14), so D-04's compact is spent in the sealing call;
  - a shade's load goes into its anchor only when that anchor keeps its own holder's agreement, never into one sealed
    again by someone else (correction 12; D-P12 as built);
  - the shade's self-note is in `decide`, for its anchor's current spirit only, so a replaced or switched-off shade
    never takes roster room (correction 11);
  - a familiar whose person is online while the Concordance is off restarts its play-time count, so no fade accrues
    while off; one not loaded meanwhile counts that play at its first look after the switch is on (correction 13;
    D-P13); the play time is read through one helper, `played(person)`, which `check_workers` audits;
  - G10's shade save is in `workersKeepTheirModelsThroughASave`; the anchor joins `blockEntitiesStampTheirVersion`
    (D-P11 for the test players).
- **Tests (none yet run in CI):** `ConcordanceWorkerGameTests` +10 (the eight above and the familiar pair),
  `ConcordanceAuthorityGameTests.onlyTheOwnersSideSealsAnAnchor` and `aStandInNeverSeals`,
  `ConcordancePersistenceGameTests.anAnchorsAgreementRoundTrips`; extended: `aSpiritKeepsToItsAgreement`,
  `workersKeepTheirModelsThroughASave`, `blockEntitiesStampTheirVersion`.
- **Audits:** `check_workers` (a worker that carries things leaves them once and is struck off its roster when killed;
  the familiar's play-time fade, its restart while switched off and its lenient key); `check_pacts` (new; D-01's part:
  the outcomes in order and worded, the refusal order, the cost's order, the stand-in, the owner saved and recorded,
  the cargo rule, the unit's test names); `AUTHORITY_ROUTES` names the holder's whole sealing check
  (`Authority.isStandIn(player) || !Authority.mayChange(...)`, correction 14).
- **Commit 1 alone is not shippable:** its interim `verify` compares holders only, so the copy check (G2) closes in
  D-02's commit; the unit lands as one.

### D-02. One pact record: the shared holder, states, copies, offline and restart (unit 1)

**What the player sees.**
- Every agreement is in one state, worded distinctly: active; waiting (with its reason); suspended by its holder;
  an offering due (with the grace's end); suspended for upkeep; without its place (a pact whose perch is gone); ended
  (with its reason, in the holder's history).
- A copied anchor (or, from D-04, a copied perch) does nothing and says why (`CONFLICTING_AGREEMENT`); breaking it
  releases nothing, sends no shade away and strikes no roster entry.
- Ending twice returns nothing the second time.

**Changes (D-S6, D-S9, D-S10, D-S16).**
- **The shared agreement holder** is extracted from `SpiritAnchorBlockEntity` into one class (seal, validate against
  the ledger, re-call, end, drop once, owner, place claim), used by the anchor here and by the perch in D-04, so the
  anchor is rewritten once (critique 27).
- **`BoundWill`** (`resource/BoundWill.java:10`) gains two optional fields: `place` (a `GlobalPos`) and `hold` (cause
  `holder` or `upkeep`, and since when). The codec (`BoundWills.java:25-29`) reads them with `optionalFieldOf`, so
  `Saved.versioned("bound_wills")` stays version 1 and `Saved.keeping` (`Saved.java:109-140`) keeps reading every record.
  A six-argument constructor stays, so `ConcordanceResourceGameTests.java:129, 135` and
  `ConcordancePersistenceGameTests.java:147` build records as before. New records name their counterpart namespaced
  (`jugcraft:gathering_shade`, G9); old ones are read as written.
- **One source of truth for suspension (D-S9):** the Bound Will's `hold` is the authority. The anchor's
  `Agreement.suspended` is read once to migrate (true with no `hold` sets `hold = holder`), stays in its codec (a
  required field, `SpiritAnchorBlockEntity.java:41-48`, so an older build still reads the anchor) and is written as the
  Bound Will says. Suspend and resume therefore work while the anchor is unloaded (from D-07's roster).
- **States (D-S9):** *invited* and *negotiating* are a transient server session: nothing is stored or spent, and a
  restart forgets them. *Active* and *suspended* (cause holder or upkeep) are stored on the Bound Will. *Waiting* is a
  derived `Status`, with two reasons appended in `tools/concordance_workers.py`'s `STATUSES`, each with its own lang:
  `upkeep_due` ("an offering is due") and `upkeep_held` ("suspended until an offering is made"), distinct from
  `suspended` ("suspended by its holder", `tools/concordance_workers.py:63`). *Departed* means the record is removed and
  one entry is added to the holder's history.
- **Pact terms and escrow live in a world record (D-S6):** a new `PactRecords` SavedData (`jugcraft:pacts`, Overworld,
  `Saved.versioned`, read entry by entry with `Saved.keeping`), keyed by Bound Will id: the terms copied at sealing (an
  `Agreement` for service, area, hours and quota; an `Upkeep` of offering tag, count, period, grace and prepaid cap; the
  beneficiaries; the definition's revision), the escrow (exact stacks, at most the cap), the upkeep clock (`due`,
  `grace_until`, `warned`, `last_look`). Nothing of it lives in a block. A copied block entity therefore copies no
  escrow, and a crash between a chunk save and the world data's save cannot split escrow from its pact.
- **Departure history (D-S6):** a separate `PactHistory` SavedData (`jugcraft:pact_history`), per holder at most 8
  entries (counterpart, reason code, game time), oldest dropped, on the courier ledger's bounded-history pattern
  (`logistics/Logistics.java:45, 441`). A separate record because a non-UUID key in the Bound Wills map would be kept as
  unread and never updated (critique 5).
- **A pure `Pacts` rules class** beside `Agreement`, holding no state:
  - seal outcomes: `DONE`, `TAKEN` (the place keeps one), `ALREADY_HELD` (the holder keeps this counterpart),
    `HOLDER_FULL`, `ROSTER_FULL`, `NOT_PERMITTED`;
  - `upkeep(...)` on the Spire's model (`Spires.java:152-175`): at most one period falls due a look, and the next is set
    a period after it or, when that is already past, a period from now (`:163-166`), so absence accrues no debt; an
    offering in escrow is taken; none means a warning (once) and `grace_until`; a passed grace with none means
    `hold = upkeep`; nothing is ever taken while held; paying clears the hold and resumes;
  - **rescheduling after an absence (D-S3, critique 2a):** a look that follows a gap (no look for more than 1,200 ticks,
    a starting value: the holder away or offline, the perch unloaded) moves any passed deadline forward: a passed due
    date becomes now, so the warning shows now; a passed grace end becomes now plus the whole grace. A returning holder
    always gets the warning and the full grace;
  - `end` is holder only and happens once (`BoundWillLedger.release` already returns `UNKNOWN` the second time,
    `:75-86`).
- **Who looks (D-S3, critique 2b):** upkeep is looked at only while the place is loaded and its holder is online, alive,
  in the place's level and within 64 blocks of it: `Authority.present` (`:59-65`) plus the distance. Never through a
  stand-in (`Authority.isStandIn`, `:106-108`), whatever `concordance.absent_owner_authority` says. Nothing simulates
  elapsed time; the checks repeat on load.
- **Feature disable (D-S16):** while the Concordance is disabled, no upkeep falls due and nothing suspends; workers and
  pacts wait with `DISABLED`; records, history and escrow are untouched (as workers already stop,
  `GatheringShadeEntity.java:87-90`; `docs/TESTING.md:43`; `CLAUDE.md:64`).
- **Consistency (D-S9):** a holder block entity's agreement counts only if the ledger holds its record with the same
  id, holder and place. **Legacy records** (no `place`) are claimed by the first anchor that loads naming the record (id
  and holder) whose `spirit` its holder's roster lists; first claim wins, and the claim writes the place. An anchor whose
  shade was already lost before Chunk D has no roster entry to match; it claims by record and holder alone on its first
  load (first claim wins again). Later copies are inert: they show `CONFLICTING_AGREEMENT` (this ticket is that code's
  first user and fixes its parameters, `Code.java:33-34`), serve nothing and release nothing when broken.
- **Uniqueness (D-S10):** one agreement per place; one Stolas pact per holder; any number of holders per world. Bound
  Wills stay non-transferable; `transfer` keeps no game route.

**Reuses:** `BoundWills`, `WorkerRoster`, `Saved`, `Authority`, `Code` (#466), the Spire's rules (as a pattern; the
Spire is not edited).

**Tests (server):**

| Suite | New tests |
|---|---|
| `ConcordancePersistenceGameTests` | `aBoundWillReadsItsOlderSave` (no `place` or `hold`); `aPactRecordRoundTrips`; `thePactHistoryKeepsEight`; an unreadable pact record kept as written; the Spirit Anchor added to `blockEntitiesStampTheirVersion` and `anAnchorsAgreementRoundTrips` (G10); `aSuspendedAnchorMigratesItsHold` (a legacy `suspended: true` becomes `hold = holder`, once) |
| `ConcordanceSharingGameTests` | `aCopiedAnchorGivesNothingAndReleasesNothing` (its data copied to a second place: the copy shows `CONFLICTING_AGREEMENT`; breaking it leaves the record, the shade and the roster entry); `aLegacyAnchorClaimsItsPlaceOnce` (a save from before Chunk D with two copies: the first to load claims); `endingTwiceReturnsOnce` |
| `ConcordanceWorkerGameTests` | The rules, with fixed times: `upkeepWarnsThenSuspendsAfterGraceAndResumes`; `aReturnAfterAbsenceGetsTheFullGrace` (critique 2a); `absentAndUnloadedTimeIsNeverOwed`. And `onlyTheHolderEnds`; `twoPlayersCannotBothSealOnePlace` (the same tick: one `DONE`, one `TAKEN`, nothing spent); `noOneBindsAnotherPlayer` (the actor is always the holder; a stand-in refused; `transfer` stays `NOT_TRANSFERABLE`); `theConcordanceOffHoldsEveryClock` |
| `ConcordanceResourceGameTests` | `boundWillAstralAndPrimaKeepTheirShape` unchanged and passing (critique 5) |

**Audits:**
- `check_persistence` (`tools/check_mod_data.py:20183-20211`): both new SavedData through `Saved.versioned`, and added to
  `ENTRY_BY_ENTRY`;
- `check_workers`: `STATUSES` in order with the two appended reasons and their lang (`:17885-17893`);
- a pact `problems()` audit, run after every step as the logistics tests run theirs: each live record has one place; no
  place keeps two; escrow is non-negative and within its cap; a history holds at most 8; no `hold` without its record.

**Acceptance:** E1 (only the holder ends; a stand-in is refused; nothing transfers); E3 (a copy is inert, a legacy copy
too); E4 (ending twice returns once); E5 (`TAKEN`). E2 is shown by the rules here and on Stolas's upkeep in D-05
(critique 7): the shade has no upkeep.

**Saves:** optional `place` and `hold` on Bound Wills (version 1 stays); the new `jugcraft:pacts` and
`jugcraft:pact_history` records; the anchor's `suspended` read once to migrate. A downgrade drops the new keys on its
next write; not supported, and said so in the record.

**Review boundary:** the schema, the shared holder and pure rules, shown on the existing anchor. No new entity, block or
art.

**As built (unit 1: commit `fa5360d0d`; records in the records' commit "Concordance Chunk D: unit 1's records":
[arcane-concordance-workers.md](arcane-concordance-workers.md),
[arcane-concordance-persistence.md](arcane-concordance-persistence.md)).**
- **Built:**
  - `resource/BoundWill`: an optional `Pact`: `place` (null: without its place), `Hold` (`none`, `holder`, `upkeep`) and
    since when, `beneficiaries` (`personal` or `party`), `presence`, `Upkeep` (offering tag, count, period, grace, cap,
    and its clock: due, grace end, last look), `Service` (work, radius, hours, quota) and the day's count (`window`,
    `done_today`). The six-argument constructor stays. `BoundWillLedger.get` and `update` (the same id and holder only).
  - `spirits/BoundWills`: each entry's optional `pact` and `escrow` (exact stacks), defaults not written, still version
    1 and read will by will; `release` hands a record's escrow back once (`Released`); `deposit` (up to the cap),
    `take`, `periods`, `escrow`, `unreadable` and `problems`.
  - `spirits/PactHistory` (`jugcraft:pact_history`, version 1, read holder by holder, the last 8 departures each).
  - `worker/Pacts` (pure): `claim`, `unclaimed`, `suspend`, `resume` (the clock moves on by the time held), `due` (the
    Spire model), `done` and `didTask` (the day's count on the record), `status`, `held`, `seated`, `unseat`, `keptAt`,
    `sealed`, `service`, `terms`, `benefits`, `valid`, `problems`, `departed` and `bounded`; the numbers `PACT` mirrors
    (`ABSENT_TICKS` 1,200, `WRITE_TICKS` 200, `REACH` 64, `HISTORY` 8, and the bounds of an upkeep's terms).
    `Status.UPKEEP_DUE` and `UPKEEP_HELD` are appended and show a want sign.
  - `AgreementHolder`: `verify` (`Standing`: `NONE`, `VALID`, `ELSEWHERE` for a copy, `UNREADABLE`), with the legacy
    claim (D-P3, D-P4) and a gone record forgotten (D-P7); `held` and `state`; `look` (D-S3, D-S16); `deposit`; `end`
    with the escrow back into the living holder's inventory (overflow at their feet) and a history entry; a static
    `toggle` by record id; `taskDone` on the record; `broken` only with the Concordance on. `Authority.presentNear`;
    `CourierLedger.PLACE` made public and shared; `Code.CONFLICTING_AGREEMENT` has its first user (no parameters).
  - The anchor seals with optional upkeep terms (`sealing(terms, upkeep)`; none in play); the shade waits on an
    unreadable record and shows an upkeep hold with its suspended clip.
  - Generators: `PACT`, `PACT_HOLDS`, `PACT_DEPARTURES`, the two statuses, the D-02 messages (`held_for_upkeep`,
    `unreadable_record`, `upkeep_paid`, `upkeep_warned`, `upkeep_held`, `upkeep_resumed`, `departed.ended`,
    `departed.broken`), `CONFLICTING_AGREEMENT`'s wording in `tools/concordance_diagnostics.py`, the codex's copy
    sentence; lang regenerated.
- **Departures from this ticket and from the unit's specification** (proposals D-P1 to D-P5, D-P7 to D-P10 and D-P16 in
  Decisions):
  - **no `jugcraft:pacts`:** the pact is on the Bound Will and its escrow in the same entry (D-P1). The ticket's
    `Agreement` "for service, area, hours and quota" became the record's `Service` with its day's count (`window`,
    `done_today`), copied at sealing or at the legacy claim (the review's correction 4), so a pact seated again at a new
    perch keeps its terms and its day (D-S1, D-S6, D-S23); the block's `Agreement` is a copy its presence and its
    reading use;
  - the legacy claim is by sealed coordinates, not "first to load" (D-P3; stricter than D-S9, and it never refuses a
    genuine anchor);
  - upkeep (D-P2 as built, correction 3): a due date that passed while nobody looked becomes the moment of return, and
    a pact resumed from an upkeep hold by an offering falls due a whole period from then, so neither time away nor time
    held is charged;
  - breaking a holder block while the Concordance is off releases nothing (correction 5; D-P16);
  - suspend and resume by record id alone (`AgreementHolder.toggle(level, player, id, now)`) for D-07's roster while the
    place is unloaded (correction 9; D-P8); `end` has the same static form;
  - a broken anchor's escrow drops where the block stood with `Containers.dropItemStack`, whatever the block-drop rule
    says (the specification had `Block.popResource`, which `doTileDrops` would suppress: the escrow was paid, not
    mined);
  - `Pacts.unclaimed` tells a record from before Chunk D that no block has claimed from one without its place
    (correction 16), for D-07's roster;
  - the anchor has no upkeep, so E2 is shown on test terms (the vanilla `#minecraft:wool` tag, one an offering, a
    7-day period, a day's grace, 4 ahead) until D-05; `look` has no production caller until D-04's perch ticker (D-P9);
  - the Gentle loophole stays (Risks); an unreadable record is tested inside `aBoundWillReadsItsOlderSave`;
    `aChangeOfOwnerMovesNoPact` and `thePactRulesKeepTheirShape` are new.
- **Tests (none yet run in CI):** `ConcordanceWorkerGameTests` +7 (`upkeepWarnsThenSuspendsAfterGraceAndResumes`,
  `aReturnAfterAbsenceGetsTheFullGrace`, `absentAndUnloadedTimeIsNeverOwed`, `onlyTheHolderEnds`,
  `twoPlayersCannotBothSealOnePlace`, `noOneBindsAnotherPlayer`, `theConcordanceOffHoldsEveryClock`),
  `ConcordanceAuthorityGameTests.aChangeOfOwnerMovesNoPact`, `ConcordancePersistenceGameTests` +4
  (`aBoundWillReadsItsOlderSave`, `aPactRecordRoundTrips`, `thePactHistoryKeepsEight`,
  `aSuspendedAnchorMigratesItsHold`), `ConcordanceSharingGameTests` +3 (`aCopiedAnchorGivesNothingAndReleasesNothing`,
  `aLegacyAnchorClaimsItsPlaceOnce`, `endingTwiceReturnsOnce`),
  `ConcordanceResourceGameTests.thePactRulesKeepTheirShape`; extended: `everyRecordReadsItsSaveFromBeforeVersions` (a
  pact, its escrow, the history) and `aSpiritKeepsToItsAgreement` (suspended on its Bound Will). With D-01's, the unit
  adds 29 server game tests: Worker 5 to 22, Authority 5 to 8, Persistence 10 to 15, Sharing 2 to 5, Resource +1. No
  test class is new.
- **Audits:** the rest of `check_pacts` (the rules' numbers, holds and departure reasons against the generator; the
  Bound Will's optional keys; no item list in a holder block; the copy check and the claim in `verify`; the day's count
  on the record; `end`'s order; `broken` while switched off; `toggle` by record; `look`; the anchor's
  `CONFLICTING_AGREEMENT`; the shade's `verify`); `ENTRY_BY_ENTRY` gains `PactHistory`; `AUTHORITY_ROUTES` gains
  `presentNear`.
- **Commands** (run and passed before each of the unit's four commits): `python3 tools/owner_art.py --check`,
  `python3 -B tools/check_mod_data.py`, `python3 scripts/check_repository.py`,
  `python3 tools/concordance_journey.py --check` ("every route holds"), `python3 tools/check_icon_maps.py`,
  `node --test tools/world-designer/model.test.cjs`, `python3 -m unittest discover -s scripts/tests` and
  `git diff --check`. For these notes: every changed Java file parse-checked (24 files, no syntax error); the pure
  classes (`worker/Pacts`, `Agreement`, `Status`, `Bond`, `resource/BoundWill`, `BoundWillLedger`, `logistics/Place`)
  compiled with JDK 21's `javac` against the jspecify jar, and two pure harnesses replaying the upkeep tests' traces and
  `thePactRulesKeepTheirShape`'s rules passed; `python3 tools/select_client_tests.py --base a8c7cd770` chose every
  client class (239: `TestPlayers.java` is a shared file, D-P11).
- **Audit mutations** (run in place on the final tree, every file restored after each and the tree verified
  unchanged): the specification's 30 (M1 to M12, M14 to M31) and 16 for the review's corrections (the cost asked after
  the call and paid before the record, the cargo into any anchor, `cannot_afford` dropped from `SEAL_OUTCOMES`, a
  renamed `theHolderEndsWithoutBreaking`, the play-time helper reading game time, no restart while switched off, the
  fade counted only beyond the follow distance, a renamed `aFamiliarFadesWhileItsPersonPlaysElsewhere`, `broken`
  releasing while switched off, the record `toggle` letting a stand-in through, `taskDone` counting only on the block,
  `done_today` and `service` made required, the legacy claim copying nothing, the generator's `REACH` 32). 45 of the
  46 are reported and the unchanged tree reports nothing. Not reported: counting the familiar's fade only beyond its
  follow distance, as before Chunk D; `aFamiliarFadesWhileItsPersonPlaysElsewhere`'s last step catches it when it runs.
  M5 (`ROSTER_FULL`'s id changed) is reported by the outcome checks, not by a missing lang key as the specification
  expected: the lang check reads the generator's list.
- **Not run:** `./gradlew build` (with or without the optional integrations) and every server and client game test (CI
  is their first compile and run), any client, two clients, a real world saved before Chunk D, a server restart.
- **Not confirmed until CI:** that DFU's `optionalFieldOf` fails on a present malformed value (so an unknown hold keeps
  its entry unread; read in the DFU source, not run); that `NbtOps` refuses a string tag read as a long (the unreadable
  `seen_played` step); the game-test server's Nether, writable player and statistics files and enabled parties; that a
  Fabric `FakePlayer`'s messages are harmless; that a SmartBrainLib brain works on a Hearthling loaded and added within
  the test's tick.

### D-03. Stolas is found through Styx, or without him (unit 2)

**What the player sees.**
- Talking to Styx, a player who has begun the Binding Arts hears a line chosen from their own progress toward Stolas's
  Regard: what kind of evidence Stolas values and what to do next, and, while they have recorded no sky observation,
  that Stolas wants a star seen through an observatory. Styx grants nothing; his gift and his four lines stay.
- The journal shows the new entry and its next step, and names the sky step separately.
- A player without Styx finds the entry through the codex's Binding category and the specimens.

**Changes (D-S12).**
- **New research `jugcraft:stolas_regard`** (`data/jugcraft/concordance/research/stolas_regard.json`, from the
  generator; stable once released): principle tether, tradition spiritbinders and stage practitioner, as the Binding
  Arts (`research/binding_arts.json:3-5`); requires `binding_arts` understood. States: encountered, examine
  `#jugcraft:stolas_specimens`; observed, `practice jugcraft:cultivation`; understood, `practice jugcraft:assay`;
  mastered, `practice jugcraft:pact_service` (the pact's first obligation, D-05). **No `notes` alternative** at any
  state: copied notes never stand in (03:132; notes never reach mastered anyway, `EvidenceRule.java:74-76`). The engine
  keeps evidence for a later state until the earlier ones are reached (`rules/ResearchEngine.java:21-22`), so the
  steps can be done in any order. The plan's "three evidence tokens" become knowledge keys, not items: nothing to copy
  (a recorded deviation from 03:132).
- **The practice gate ships here (critique 11):** `practice/pact_service.json` (tradition spiritbinders, requires
  `stolas_regard` understood). Without it the progression graph would find nothing providing `practice:pact_service`
  and mastery would be unreachable (`progression/ProgressionGraph.java:93, 112-130`).
- **The reward has a consumer (critique 18):** mastering Stolas's Regard unlocks the workshop hand (D-08), checked in
  code as the Binding Arts are (`Workers.knows`, `Workers.java:123-125`). An entry's `unlocks` names only invocations,
  workings and rituals (`rules/RulesParser.java:101-110`); `binding_arts.json:53` has none. The "bounded insight" of
  03:137 is this unlock; the delivery plan's forecast insight is dropped, because `Sky.obscured` reads the present
  heightmap and weather and cannot say whether a future window will be visible (`Sky.java:187-195`).
- **Specimens `#jugcraft:stolas_specimens`:** `stolas_starflower`, `ravenquill_lupine`, `amethyst_mourningbell` (Styx
  flowers, `JugcraftStyx.java:24-25`) and `minecraft:feather`, the non-Styx specimen, so the route is reachable without
  Styx (G12; `CLAUDE.md:57`). The tag joins `#jugcraft:concordance_specimens`, which examination reads; examination
  gains a line for these specimens instead of Circle Lore's "form" line (`concordance/Examination.java:96-98`).
- **The sky step (D-S12):** the invitation's precondition (D-04) is `AstralClaims.observed(player)` being non-empty
  (`AstralClaims.java:83-85`), written by any observatory use while a pattern is up and visible
  (`ObservatoryBlockEntity.java:205-220`). It is not research evidence and `ResearchEngine.explain` does not cover it, so
  Styx's hint and the journal name it separately. A player with none waits for the next visible window (Lantern Star
  every 3 days in clear weather, New Moon every 8 in any weather: `concordance/pattern/*.json`). Nothing is recorded
  retroactively.
- **Companion data, through the generators (critique 11):** four advancements
  (`advancement/concordance_stolas_regard_{encountered,observed,understood,mastered}.json`, as Binding Arts' four); the
  Modonomicon research facts, nodes and hooks (`modonomicon/research/concordance/{facts,nodes,hooks}.json`, written by
  `tools/concordance.py`); a codex entry `modonomicon/books/arcane_concordance/entries/binding/stolas.json` in the Binding
  category, the non-Styx route's reading.
- **Styx:** `Styxhexenhammer.mobInteract` (`:84-90`) keeps the gift and the rotating lines, and adds the hint built
  from the listener's own `ResearchEngine.explain` for `stolas_regard` and their observations. The lines come from
  `tools/styx.py` (`:193-202`), not by hand; the blueprints are frozen and untouched (`StyxGameTests.java:89-90`). Talking
  teaches nothing, as the Academy's lesson already says of its instructor (`en_us.json:7`).
- **The journal's "pacts" section** (the 14th of 24, `Journal.java:53, 86-106`) begins here: shown from Binding Arts
  understood, it names Stolas's Regard's next step and the sky step; D-07 adds the pacts.
- **Route depth:** the progression graph's practice gates put Verdant Husbandry and the Assay at understood before
  their practice (`practice/cultivation.json`, `practice/assay.json`), but only the graph reads them
  (`ProgressionGraph.java:112-130`): in play the practice is recorded for anyone who harvests a ripe organism crop or
  dissolves at the Assayer's Scale (`garden/OrganismCropBlock.java:338-349`; `assay/Assaying.java:224-233, 295`), so
  in code only the Binding Arts gate Stolas's Regard. Whether D-03 checks the two researches as well is settled when it
  is built and recorded in its As built note. The sky step asks no research. `tools/concordance_journey.py` and
  `check_acquisition` confirm the route.

**Reuses:** the research engine and journal explain, Styx's interaction and rate limit, examination, `AstralClaims`, the
Modonomicon Binding category, the advancement pattern.

**Tests (server):**

| Suite | New tests |
|---|---|
| `StyxGameTests` | `styxHintsByTheListenersOwnProgress`; `talkingGrantsNoResearch`; `styxNamesTheSkyStepUntilOneIsRecorded` |
| `ConcordanceSharingGameTests` | `notesNeverStandInForStolasEvidence` |
| `ConcordanceProgressionGameTests` | `stolasRegardIsReachable` (its gates exist; nothing it needs is unprovided) |
| `ConcordanceSkyGameTests` | `aRecordedObservationCountsWithoutAWindow` (observed earlier, read later) |
| `ConcordanceJournalGameTests` | `thePactsSectionNamesTheSkyStep`; hidden before the Binding Arts |

**Audits:** the research parse; the progression graph; `check_acquisition`; `concordance_journey.py --check`; the
advancement and codex audits as for other entries; `stolas_specimens` holds a non-Styx item.

**Acceptance:** E1 and E3 for personal evidence (knowledge is per player; notes never count). The route is reachable
with and without Styx.

**Saves:** none (the knowledge attachment is unchanged).

**Review boundary:** discovery only: nothing is spent, no entity.

### D-04. The perch, the invitation, the conversation, terms, acceptance and refusal (unit 2)

**What the player sees.**
- The player crafts a **pact perch** and places it where they may build (D-S15): claims and spawn protection apply, and
  Styx's protected district refuses it. The perch records its owner (D-S8).
- The owner uses it, with Stolas's Regard understood and a sky observation recorded: the invitation opens and Stolas
  arrives at the perch (D-S7). In unit 2 his body may be provisional; D-06 gives him his own.
- A conversation screen gives his three dialogue beats, then the terms page (D-S15): the service (Star-reckoning), hours
  (any hour, a starting value), area (8 blocks round the perch, a starting value), daily quota (1), upkeep (1 offering
  every 7 game days, up to 4 prepaid) and grace (1 game day), what suspension does (the service stops; nothing is taken
  or lost), duration (until ended), breach rules (a missed offering suspends; nothing else breaches; a pact never ends
  by itself), how to end (at the perch or from the roster), what returns (the escrow, once, to the holder), the
  beneficiaries (the holder alone) and the Open Hand Compact's cost.
- **Accept** spends the compact's cost in the same server action that seals (D-S13). **Decline**, or any refusal,
  spends nothing and names the reason: missing evidence (each step named); no sky observation; "this perch keeps
  another's agreement" (`TAKEN`); "you already keep a pact with Stolas" (`ALREADY_HELD`); "you hold 32 agreements"
  (`HOLDER_FULL`); a full roster (`ROSTER_FULL`); "only the perch's owner may invite here" (`NOT_PERMITTED`); short of
  the compact's materials or resonance; the Concordance is off; the pact's definition did not load.
- Two players negotiating at one perch: the second **Accept** is refused as `TAKEN` and spends nothing (D-S8).

**Changes.**
- **Block, item and block entity `jugcraft:pact_perch`** (stable once released): a place marker using the shared holder
  (D-02). It records its owner and the pact it seats and **holds no items** (D-S6): it is not a container, so hoppers,
  pipes and Fabric's item storage find nothing to take, and an explosion can destroy no escrow. Breaking or blowing it up
  never ends the pact: the pact is "without its perch", Stolas leaves the world, and the record and escrow stay
  (D-05). Its recipe is ordinary materials; its art is new under `docs/ITEM_ICONS.md` and `docs/ART_DIRECTION.md`.
- **The pact definition is data** (`data/jugcraft/concordance/pact/stolas.json`, a strict parser: `Json.only`, a
  schema version), not a fourth `WorkerDefinition` kind, so `check_workers`' three kinds and Binding Arts' "distinct 3"
  keep their meaning (`tools/check_mod_data.py:17896-17907`). It holds the service, terms, upkeep, compact cost and the
  dialogue keys. An invalid or missing definition reports cleanly on reload and refuses new invitations with a reason;
  sealed pacts keep the terms copied at sealing (D-S23; 11:59).
- **The Open Hand Compact is the acceptance cost, not a rite (D-S13):** ordinary materials and Astral Resonance from
  the holder's astrolabe (`AstrolabeItem.charge/setCharge`, `:42-56`; capacity 32, `:36`), checked and spent in the call
  that seals; a refusal spends nothing. No new ritual result kind (`RitualDefinition.java:56-71`; compare CC's Stillwater
  deferral). The compact counts as the first period's offering: the first offering falls due 7 game days after sealing.
  The materials and resonance are starting values in the generator, recorded in the As built notes.
- **Transport (D-S14):** a perch-bound `ExtendedMenuProvider` menu on the `ShopMenu` pattern
  (`town/ShopMenu.java:129-136, 143-160`): `stillValid` while the perch is loaded, the opener is within reach and the
  perch still stands. The menu has
  **no slots**, so nothing can be duplicated through them (its `quickMoveStack` returns nothing, as `ShopMenu.java:138-141`
  does); deposits are server-side actions (critique 21).
  Vanilla's `clickMenuButton` carries one int (`ShopMenu.java:144`), too little for a sequence and a revision, so
  requests are one small client-to-server payload, `PactRequestPayload(containerId, sequence, action, choice,
  revision)`, all bounded integers: `action` indexes a fixed list (choose, accept, decline, recall, deposit, suspend,
  resume, end, seat); `choice` indexes a list the server sent (an observed pattern, an offering kind); `sequence` rises
  per menu and a stale one is dropped; `revision` must equal the terms revision the server last sent, or the answer is
  "changed" and the terms are sent again. The server checks the menu's container id, `stillValid`, the actor (never a
  stand-in), a `RateGate` and the authority, then answers with a `PactStatePayload` (terms rows, choices, revision, last
  result; bounded lists). No free text, no player ids, no client totals (08:99); no chat commands (12:54); no vanilla
  Dialog, whose custom-click hook gets no player and would need a mixin (`mc263src`
  `net/minecraft/server/MinecraftServer.java:1955-1957`); no GuiLib (no narration *(survey)*).
- **A native `PactScreen`** from `BookArt.Tab`/`Paragraph` and `ComposerScroll` (`BookArt.java:69, 93`), narrated and keyboard
  operable; it adopts `ScreenKit.Fitted` if #469 merges first.
- **`StolasEntity extends WorkerEntity`**, in `concordance/spirits/` so `check_workers`' bans on chunk loading,
  dimension changes, level-taking teleports and brain memories cover it (`tools/check_mod_data.py:17925-17937`;
  critique 13); `MobCategory.MISC`, as the workers are (`Workers.java:76-81`), so a transport crate refuses him
  (`local/peepo/MobTransportCrate.java:53-57`); roster kind `pact`, with kind lang. **Lifecycle (D-S7):** he arrives at
  the perch when the invitation opens, as a visitor bound to that session; he leaves when he has neither a pact record
  nor a live session (decline, close, timeout, disconnect, restart), checked each think on the shade's pattern
  (`GatheringShadeEntity.java:99-103`), since a worker is persistent (`WorkerEntity.java:71`). After acceptance he is the
  pact's presence: one presence per pact; he takes no damage but damage that bypasses invulnerability (Styx's rule,
  `Styxhexenhammer.java:53-55`); a lost presence is called back at no cost and never ends the pact.
- New items (the perch's block item, any compact item) go into `equivalence/excluded`; `values.json` and the survival
  registry are regenerated (CC's Where to resume, item 1). Consumed offerings stay in equivalence (D-S19).

**Reuses:** D-02's holder, rules and records; `BoundWills`, `WorkerRoster`, `Authority`, `RateGate`, `AstralClaims`,
`ConcordanceProgress.knowledge`, `Code.CONFLICTING_AGREEMENT`.

**Tests (server):**

| Suite | New tests |
|---|---|
| `ConcordanceWorkerGameTests` | `stolasExplainsBeforeAgreeing` (the terms sent, nothing stored or spent); `aStaleTermsRevisionIsRefused`; `aRefusalSpendsNothing` (a survival player); `acceptingSealsOnceAndSpendsOnce` (a survival player: materials and resonance gone once; one record); `oneStolasPactPerHolder`; `anotherPlayerCannotAcceptForTheHolder` (a party member at the owner's perch: `NOT_PERMITTED`; a request naming another menu's container: dropped); `twoNegotiationsAtOnePerchSealOnce` (`TAKEN`, nothing spent); `aVisitingStolasLeavesWithoutARecord` (decline, close, timeout, disconnect, and a reload with no session); `aCrateRefusesStolas` (`MobTransportCrate.friendly` false, `capture` refused); `stolasTakesNoOrdinaryDamage`; `aBrokenPactDefinitionIsReported` (a reload with `stolas.json` invalid, then missing: invitations refused with the reason; a sealed pact keeps its terms) |
| `ConcordancePersistenceGameTests` | the perch in `blockEntitiesStampTheirVersion`; `thePerchHoldsNothing` (a hopper beneath takes nothing; an explosion drops only the perch's item; the escrow is unchanged) |
| `ConcordanceSharingGameTests` | `aCopiedPerchIsInert` |

The hopper and explosion tests are moot by design (the perch holds nothing) and are kept as proof of it.

**Tests (client):** a new `ConcordancePactClientGameTests`: the conversation and the terms page at GUI scale 2 and 4
and in a small window; acceptance by keyboard alone; toasts cleared before each shot. A `WEIGHTS` entry in
`tools/select_client_tests.py:53` if the class is long.

**Audits:** a new `check_pacts` (the definition's fields and bounds; its lang; the perch in the exclusion tag; the
roster kind's lang); the icon maps; the recipe-conflict fixture once #462 merges.

**Acceptance:** E1 (only the actor binds themself; another player cannot accept; the server trusts no client); E3 (a
copied perch is inert); E5 (one pact a place; one Stolas pact a holder; two negotiations seal once); refusal and failure
dialogue (09:158).

**Saves:** the perch's block entity (owner, pact id; stamp 1); the Bound Will's `place`; the pact record.

**Review boundary:** the first half of the end-to-end path.

### D-05. Star-reckoning, the obligation, upkeep, renew, suspend and end (unit 2)

**What the player sees (D-S1).**
- **Star-reckoning:** while the pact is active, once a game day a pact, the holder at the perch (inside the pact's area)
  chooses any sky pattern they have personally observed (`AstralClaims.observed`, `:83-85`) and recalls it with their
  astrolabe's stored resonance, at the pattern's existing `recallCost`, under the same `recall:<pattern>`
  once-per-occurrence claim as a master's recall, for `Calendar.RECALL_TICKS` (2,400 ticks, `Calendar.java:18`). The
  holder needs the pact, not celestial mastery. Season and window rules are the existing recall's: a recalled attunement
  ignores its window and season (`Sky.java:298`). It is a chosen recall, so it does not wait for an empty sky, as the
  master's fallback does (`AstrolabeItem.java:107-111`).
- **Why it is useful:** the holder chooses the pattern instead of getting the last one observed; nothing waits for the
  19-day Echo Comet (`pattern/echo_comet.json`), a winter or a clear night; the effect lasts 2,400 ticks wherever the
  holder goes.
- **What it changes (critique 10):** `Calendar.occurrence` is plain day arithmetic (`Calendar.java:51-54`) and the
  shortest gap between claims is half a period (`:94-96`). The Harvest Moon has a period of 1 (Hero of the Village,
  recall 12) and the Winter Crown a period of 2 (Fire Resistance, recall 9). A holder who has observed them (a pattern is
  up, and so observable, only in its season: the Harvest Moon during the Halloween event's Harvest Moon, the Winter Crown
  in winter or in any season when seasons are switched off, `Sky.java:169-178, 202-210`) can recall them as often as the one-a-day quota and the claim allow: daily and every other day, for 2,400 ticks, at 12 and 9 resonance. A master can already do this for whichever
  pattern they observed last (`AstrolabeItem.java:112-131`); the pact opens it to non-masters and to any observed
  pattern. No resonance is minted: it comes from the astrolabe, filled by observatories once per occurrence.
- **The first obligation:** record a sky observation at an observatory while the pact is active (not suspended). The
  pact then records `Practiced(jugcraft:pact_service, jugcraft:stolas)` once, which masters Stolas's Regard and unlocks
  the workshop hand (D-03, D-08).
- **Upkeep (D-S3, Gentle):** one offering every 7 game days from the pact's escrow (`#jugcraft:stolas_offerings`: the
  three Styx flowers and `minecraft:amethyst_shard`, the non-Styx fallback); up to 4 periods prepaid; the compact pays
  the first. Looked at only under D-02's rule (the perch loaded; the holder online, alive, in its level, within 64
  blocks; never a stand-in). A missed offering shows a warning (a sign over Stolas, a message to the holder, the
  `upkeep_due` status in Jade and the roster), then 1 game day of grace, then suspension for upkeep: the service stops,
  nothing is taken, nothing is lost, nothing accrues. Paying resumes it. A pact never ends by itself.
- **Renew:** deposit offerings at the perch (a menu action: the server takes the chosen offering from the player's
  inventory into the escrow, up to the cap). **Suspend and resume:** the holder's choice, at the perch or (D-07) from the
  roster.
- **End (D-S6):** holder only, at the perch or from the roster. Stolas makes his departure gesture and leaves; the
  escrow is returned once into the living holder's inventory, any overflow dropped at the holder's feet (as
  `CourierLedger.recover` does, `:289-304`), never into a dead player's (a dead holder cannot end until respawned); the
  reason goes to the history.
- **Without its perch:** breaking or blowing up the perch leaves the pact "without its perch": Stolas leaves the world;
  the record and escrow stay; the holder may seat the pact at a new perch they own (a menu action there) or end it from
  the roster.

**Changes.**
- **One recall path:** `AstrolabeItem`'s recall (`:107-131`) is extracted into one shared method (pattern, astrolabe,
  holder, times) that both the master's fallback and Star-reckoning call: one claim key, one cost, no minting (03:155).
  The master's route keeps its own rule (nothing up; the last pattern observed).
- The perch's server ticker looks at upkeep once a second under D-02's rule; Stolas thinks once a second
  (`WorkerEntity.java:59`).
- `#jugcraft:stolas_offerings` (generator). Consumed offerings stay in equivalence (D-S19, critique 14): an amethyst
  shard is a reference item worth 32 (`alchemy/values.json`), the Styx flowers are renewable with bone meal
  (`StyxFlowerBlock.java:31-36`; bone meal worth 1/6) and valued at the generator's fallback of 1, "balance pending"
  (`alchemy/values.json`). An offering a week is a sink, not a
  conversion; the record states the balance unit.
- `practice/pact_service.json` (shipped in D-03) is recorded from the pact's obligation only. Not `worker_service`,
  which would change Binding Arts' "distinct 3" (`research/binding_arts.json:43-50`) and feed the `bound_service`
  commission (`concordance/commission/bound_service.json:5`).

**Tests (server):**

| Suite | New tests |
|---|---|
| `ConcordanceSkyGameTests` | `stolasRecallsAnObservedPatternOncePerOccurrence`; `aMastersRecallAndStolassShareOneClaim`; `aChangedClockGivesNoSecondRecall` (10:53); `theHarvestMoonRecallsOutsideItsEvent` (the balance consequence pinned: a new occurrence each day); `anUnobservedPatternIsRefused` |
| `ConcordanceWorkerGameTests` | `theObligationMastersTheEntryOnce`; `aSuspendedPactRecallsNothing`; `theQuotaAndTheAreaRefuseWithReasons`; on Stolas (D-02's tests of these names, extended to the pact, not second copies): `upkeepWarnsThenSuspendsAfterGraceAndResumes`, `aReturnAfterAbsenceGetsTheFullGrace` (the holder away 10 game days with an empty escrow: on return the warning and a full day of grace, then suspension only if still unpaid), `absentAndUnloadedTimeIsNeverOwed` (logged out; the perch unloaded; online but 100 blocks away; in another level), `aStandInNeverAnswersForUpkeep` (the absent-owner option on), `theConcordanceOffHoldsEveryClock`; `endingReturnsTheEscrowOnce` (into the inventory; the overflow at the holder's feet); `aDeadHolderIsNeverHandedTheEscrow`; `aBrokenPerchLeavesThePactWithoutItsPerch`; `aPactIsSeatedAgainAtANewPerch`; `aLostPresenceIsCalledBackAndEndsNothing` |
| `ConcordanceSharingGameTests` | `aCopiedPerchRecallsNothing`; `anotherPlayerCannotUseTheHoldersService` |
| `ConcordancePersistenceGameTests` | `thePactAndItsEscrowSurviveARestart`; `aRestartWhileSuspendedStaysSuspended`; `aChangeOfOwnerMovesNoPact` (the holder leaves a party; the perch's land changes hands: the pact stays the holder's, D-S23) |

Spending tests use `TestPlayers.survivor` (critique 25; the Facts above). The economy check
(`tools/concordance_economy.py`) shows no gaining cycle and states the offering's daily bound.

**Acceptance:** E2 (Stolas's upkeep suspends, with nothing owed for absence and the full grace on return); E3 (one claim
an occurrence across both routes; a copy recalls nothing); E4 (the escrow back once on ending; a broken perch keeps it;
restart and unload change nothing); E7 (the path is complete); the reward has a consumer (10:168).

**Saves:** the pact record's escrow and upkeep clock; the Bound Will's `hold`; the history.

**Review boundary:** completes the first end-to-end path. The balance change (recall for non-masters) is limited to
this ticket.

### D-06. Stolas's character: body, gestures, sounds, subtitles, Jade (unit 3)

**What the player sees.**
- Stolas, a raven, idles on the perch, listens while the screen is open, inspects (an offering, the astrolabe), assents
  on acceptance, refuses with a gesture, a subtitle line and an icon, arrives and departs. Each important cue arrives
  through two channels (08:145).
- Jade shows his service, state, next offering and grace; a new provider does the same for the perch and the Spirit
  Anchor (G11). Neither shows the escrow's contents or the holder's name.

**Changes.**
- **Body (D-S5):** a raven-form runtime copy adapted from the owner library's Flashjack, a GeckoLib bird rig
  (`art/owner-library/originals/Magic/assets/ars_jymbaumental/`):

| File | SHA-256 | What it is |
|---|---|---|
| `geo/flashjack.geo.json` | `d23b5372d27cb76ea5974b2a1e78f3cba7b5a8f96cd14bef54d1e14249adc837` | format 1.12.0, a 64×64 sheet, 20 box-UV cubes, bones `master`, `head`, `body`, `upperBody`, `wing_left`, `wing_right`, `lowerBody`, `tail`, `legs`, `feets` |
| `animations/flashjack.animation.json` | `c4015c9351dd54c3fec3589a544db4110809dc11ce60e040ad1cc7870c9491f8` | clips `idle.flapping` (4.75 s), `attack` (1.5 s), `idle.air` (3 s, looped), `idle.ground` (2 s), `idle.sitting` (1.25 s) |
| `textures/entity/flashjack.png` | `567b44a51e083f2a9ec81db92a8743435985b630f8aca98ef62baed2393ea514` | 64×64 skin (also `flapjack.png`, `bluejay.png`) |
| `geo/flashjack.bbmodel` | `7f5f6189c3bd5976bbae49d4a96ed96810166812c1991ea83fda9f4b950e1b89` | Blockbench project |

  The runtime copy (`geckolib/models/entity/stolas.geo.json`, `geckolib/animations/entity/stolas.animation.json`,
  `textures/entity/stolas.png`) is scaled up and recoloured to black plumage by a generator (the owner-art route,
  `tools/owner_art.py`, for what is copied; the new tool for what is adapted), with the missing clips authored on its
  bones: arrive, listen, inspect, assent, refuse, depart, and idle and waiting loops from `idle.sitting` and
  `idle.ground`. The originals stay intact. Provenance in `stolas-pact.md`: each source path and hash, every change,
  the archive's label and licence metadata (Ars Elemental, author Alexthw, GNU LGPL v3.0:
  `art/owner-library/ports/jug-nouveau/ars_jymbaumental-1.21.1-0.7.10.4/META-INF/neoforge.mods.toml:7, 15, 25`) and the
  owner's declaration (`art/owner-library/PROVENANCE.md:3-7`). `CLAUDE.md` authorises direct reuse and adaptation
  without a per-asset request (critique 20); the appearance stays a checkpoint question. Not the crow, the courier bird
  or the Ars familiars, which are registered identities (`beast_crow`, `mount_courierbird`, `nouveau/NouveauFamiliars.java:27`,
  `nouveau/NouveauHelpers.java:22-24`).
- **Animation:** a base controller maps the pact's state and `Status` to loops (idle, listen, waiting, suspended);
  `triggerableAnim` one-shots (arrive, inspect, assent, refuse, depart) are fired by the server's action, so a late
  joiner sees the state, not a replay (`lair/vesperine/GraveThrallEntity.java:122, 189`). SmartBrainLib
  handles look-at and perch-bound movement only; the contract stays in Jugcraft code (03:159).
- **Sounds (D-S5):** new events `jugcraft:stolas.arrive`, `.listen`, `.inspect`, `.assent`, `.refuse` and `.depart`,
  each with a subtitle, routed through the generator: `tools/concordance.py`'s `SOUND_EVENTS` and `SOUND_FILES`
  (`:1529-1561`), which `tools/generate_material_data.py:1096-1097` writes into `sounds.json` (critique 23). Each is
  bound to `.ogg` files already in runtime: wing beats (`sounds/menagerie/beast/flutter_flap_0-2.ogg`), a raven's call
  (`crow_idle_0-3.ogg`, shared with `beast_crow`), page turns (`sounds/menagerie/cavern/item/cave_book/book_*.ogg`), a
  spirit's sigh (`sounds/menagerie/herbal/ghost_weep.ogg`) and a chime (`sounds/household/block/doorbell/chime.ogg`).
  Being in `SOUND_FILES`, they get no drawn cue (`tools/concordance_sounds.py:563`). No new audio and no voice.
- **Jade:** a pact branch in `WorkerDataProvider` and the client's `WorkerTooltip`; a new provider for the perch and the
  Spirit Anchor, behind the optional `jugcraft:jade_client` entrypoint (`JugcraftJadePlugin.java:50-61`).
- **Audits:** `check_geckolib` pins `triggerableAnim` clip names as it pins `thenLoop`'s (`tools/check_mod_data.py:17265-17267`);
  `check_pacts` pins the sound events, their subtitles and their files.

**Tests.**
- Server (`ConcordanceWorkerGameTests`): `aRemovedStolasNeverEndsThePact`; `onePresencePerPact`;
  `killingStolasIsNeverTheFastestRoute` (bypass damage: no end, no refund, a re-call at no cost; 02:151).
- Client (`ConcordancePactClientGameTests`): a frozen-tick pose for each clip (`CinderTyrantClientGameTests.java:90-94`);
  the refuse and assent subtitles visible; a Jade shot; the build without optional integrations.

**Acceptance:** E4 (a presence's death returns nothing twice and ends nothing); the accessibility rule of 03:159; the
presentation bundle (08:169-181).

**Saves:** the entity's keys under `WorkerEntity`'s stamp.

**Review boundary:** presentation over D-04 and D-05's state. The look is for the owner's checkpoint.

### D-07. The roster screen, with cancel and recovery (unit 3)

**What the player sees (D-S11).**
- A native roster screen, opened by a key (listed in the key help, as every key in `PartyClient.CATEGORY` is: `EncyclopediaKeys.java:26-30`), lists every
  familiar, spirit, construct and pact the player keeps, and every pact or agreement record without its place.
- Each row: a portrait (the pattern of `src/client/java/local/peepo/client/TransportCratePreview.java`; whether it draws a GeckoLib entity is
  undetermined), kind, service, location and whether it is loaded, current task, next obligation and waiting reason
  (08:65), in words and an icon, never colour alone. Nouveau helper kinds get their lang (G13).
- **Actions:** suspend, resume and end, through the shared holder's authority on the Bound Will (so they work while the
  place is unloaded); cancel and recover a courier request through `CourierLedger.cancel/recover` (`:266, 289-304`), showing
  blocked or unloaded cargo instead of moving it (08:69); end a record without its place. A confirmation (vanilla
  `ConfirmScreen`, `mc263src` `net/minecraft/client/gui/screens/ConfirmScreen.java`) comes before ending.
- The journal's "pacts" section (D-03) lists the pacts and their next obligations.

**Changes.**
- A state payload of at most 64 rows (critique 17), built from `WorkerRoster` and the reader's Bound Wills, so a pact
  without its perch and an orphaned record appear even when the roster's 32 are full. D-01's `ROSTER_FULL` refuses a
  seal rather than drop the roster note.
- A request payload `(sequence, action, rowId, revision)`: `rowId` is a record or request id the server listed, checked
  again against the reader; no player ids. A `RateGate` shared with the commands; the holder, the authority and the
  record checked on every action; no range needed to read.
- **No encyclopedia page:** the encyclopedia is full (11 of 11) and audited read-only (`EncyclopediaPayload.java:27`;
  `tools/check_mod_data.py:19818`), so it can host no actions. A recorded deviation from 08:16.

**Tests.**
- Server: `theRosterEndsOnlyWhatTheReaderHolds`; `aRequestNamingAnotherPlayersRecordIsRefused`; `theRosterListsAPactWithoutItsPerch`;
  `theRosterStopsAt64Rows`; `recoveryNeverTakesCargoOutOfAnUnloadedContainer` (`ConcordanceLogisticsGameTests`); the
  journal section shown only after research (`ConcordanceJournalGameTests`).
- Client: the roster at two scales and in a small window; an end by keyboard alone with its confirmation.

**Acceptance:** E1 (no action on another's record); E4 (cancel and recover once); 09:162's cancel and recovery path.

**Saves:** none.

**Review boundary:** an interface over authoritative state; the screen owns no second inventory.

### D-08. The contrasting service: the workshop hand (unit 4)

**What the player sees (D-S2).**
- A holder who has mastered Stolas's Regard binds a Spirit Anchor to their Courier Post (a Porter Key naming the post,
  `Couriers.KEY_POST`, `courier/Couriers.java:61-69`, used on the anchor) and seals a **workshop hand** there. Within its
  hours, area and daily quota it carries that post's exact requests through the courier ledger, the crucible's own
  formula shortage from C-06 included (a Peepo serving the same crucible subtracts the hand's live claim,
  `CrucibleBlockEntity.java:506`).
- A full destination keeps the cargo aboard; an unloaded one waits; ending the agreement strands the cargo at the post
  for one recovery.
- **Beneficiaries:** the holder and the holder's party, the Courier Post's own rule (`CourierPostBlockEntity.java:48-51`),
  stated in its terms and recorded on the agreement.

**Changes.**
- **Its own worker definition (critique 19):** `tools/concordance_workers.py` gains the work `courier` in `WORK` (`:44`;
  `worker/WorkerParser.java`'s `WORK` with it, which `check_workers` compares, `tools/check_mod_data.py:17882-17884`)
  and a definition `workshop_hand` in `WORKERS` (`:113`; kind `spirit`, so `check_workers`' three kinds stand), with its
  own entity type `jugcraft:workshop_hand`, lang and GeckoLib model, a generator variant of the Gathering Shade's body
  (`tools/concordance_worker_models.py`), not a new Goetia identity. `check_workers`' class map
  (`tools/check_mod_data.py:17910`) gains it. Its deliveries record `worker_service` with the key `spirit`, as the
  shade's do (`GatheringShadeEntity.java:132`), so Binding Arts' "distinct 3" is unchanged.
- **The porter's courier decision is extracted into a shared helper, not copied** (`ClockworkPorterEntity.java:291-510`,
  `mayTake` at `:508-510`): the porter and the hand call it; the porter behaves identically, as its existing logistics
  tests show. The hand holds only its claim id (saved `task`); its `remove` releases with `worker_removed`, stranding the
  cargo (`ClockworkPorterEntity.java:586-602`); an unload lets the lease lapse (200 ticks, `Logistics.java:41`).
- The agreement gains optional `beneficiaries` (holder, or holder and party); the anchor gains an optional `post`.
- `UNAVAILABLE_WORKER` (`Code.java:31-32`) gets its first user if the design needs it (a request waiting on a hand
  outside its hours), which fixes its parameters.
- **The schema review (09:166; 03:177):** a section of this contract written at this ticket, comparing what the
  knowledge service (Star-reckoning) needed with what the cargo service needs (claims, leases, reservations, a loaded
  destination, access for keeper and requester, exact variants, beneficiaries), and proposing the shared schema to
  freeze at the checkpoint, not before.

**Tests.**
- `ConcordanceLogisticsGameTests`: 03:193's eight cases for the hand, mirroring C-06's table (CC:1127-1136): the
  destination fills, access removed, the path fails, the target unloads, the hand dies, its holder logs out, the source
  breaks, the server restarts; and `twoHandsCannotHoldOneRequest` (`CLAIMED`), `endingWithCargoAboardStrandsItOnce`,
  `aHandServesOnlyItsBeneficiaries`. `problems()` and the conserved count checked after every step. The 15 existing
  tests pass unchanged (the porter after the extraction).
- `ConcordanceWorkerGameTests`: `aRequestOutsideTheHoursOrAreaIsRefusedWithItsReason`; `theHandNeedsStolasRegardMastered`.
- Client companion: in `CompanionAutomationChecks`, run by `PeepoCompanionClientTests`, the Peepo's need subtracts the
  hand's live claim (D-S20 allows extending the gametest companion checks).
- `ConcordancePersistenceGameTests`: a restart with a claim in progress.

**Acceptance:** E4 and E5 with real cargo; E6 (Logistics and the client companion tests); the different requirements of
a cargo service written down.

**Saves:** the hand's `task`; the anchor's `post`; the agreement's `beneficiaries`; the courier ledger's codec unchanged.

**Review boundary:** no file under `src/main/java/local/peepo` (D-S20); the porter's behaviour unchanged.

### D-09. Combined fixes, evidence, the checkpoint and the handoff (unit 5)

- **The whole path in one server test:** discovery, evidence, the sky step, the invitation, a refusal, acceptance,
  Star-reckoning, the obligation, upkeep suspension and resumption, ending and departure.
- `tools/concordance_journey.py` taught the pact (its routes are "served once by each kind of worker", `:93`).
- **Load:** Stolas thinks once a second; one presence a pact; one pact a holder; the perch looks once a second.
  Measured on the CI runner and recorded as that runner's.
- **Client evidence** (08:169-181): each screen at ordinary and high scale and in a small window; keyboard-only
  walkthroughs; the build without optional integrations; a timed sequence of Stolas's gestures; Jade.
- **Two clients (D-S22):** attempted only with what this branch has; #467's harness is an unmerged fork contribution
  and is not imported. Otherwise recorded as not run: one client is not two (09:20).
- **Records:** `stolas-pact.md` with `CLAUDE.md:58`'s fields (tier, input producer, output consumer, costs, unlocks,
  failure behaviour, test evidence) and the art's provenance; `arcane-concordance-workers.md`, `-logistics.md`,
  `-celestial.md`, `-journey.md`, `styxhexenhammer.md`, `docs/TESTING.md`, `CHANGELOG.md`, `docs/WHAT_EXISTS.md`,
  `docs/INTEGRATION_STATUS.md`; this contract's handoff (10:133-146) and a 300 to 500 word checkpoint (10:87-121) with
  three questions (below).
- **The pull request:** the third of the stack, against `claude/chunk-c-living-workshop` (D-D1).

## Completion evidence

The plan's evidence (09:168-174), used in every ticket's acceptance, with the scenario rows 10:51 (a worker's death,
unload or restart while carrying), 10:52 (a pact: copy a token, change owner, end twice) and 10:53 (the sky: a time
change, two observatories).

| Code | Requirement | Tickets | Tests |
|---|---|---|---|
| E1 | "No one can bind another player" | D-01, D-02, D-04, D-05, D-07 | `onlyTheOwnersSideSealsAnAnchor`, `aStandInNeverSeals`, `noOneBindsAnotherPlayer`, `anotherPlayerCannotAcceptForTheHolder`, `anotherPlayerCannotUseTheHoldersService`, `aRequestNamingAnotherPlayersRecordIsRefused`, `aChangeOfOwnerMovesNoPact` |
| E2 | "Upkeep can suspend without hidden offline punishment" | D-02 (the rules), D-05 (Stolas) | `upkeepWarnsThenSuspendsAfterGraceAndResumes`, `aReturnAfterAbsenceGetsTheFullGrace`, `absentAndUnloadedTimeIsNeverOwed`, `aStandInNeverAnswersForUpkeep`, `theConcordanceOffHoldsEveryClock`, `aRestartWhileSuspendedStaysSuspended` |
| E3 | "Agreement copying does not multiply benefits" | D-02, D-03, D-04, D-05 | `aCopiedAnchorGivesNothingAndReleasesNothing`, `aLegacyAnchorClaimsItsPlaceOnce`, `aCopiedPerchIsInert`, `aCopiedPerchRecallsNothing`, `aMastersRecallAndStolassShareOneClaim`, `aChangedClockGivesNoSecondRecall`, `notesNeverStandInForStolasEvidence` |
| E4 | "End/death/unload/restart returns appropriate property once" | D-01, D-02, D-04, D-05, D-06, D-07, D-08 | `aKilledShadeLeavesItsLoadOnce`, `aKilledShadeAwayFromItsAnchorDropsWhereItFell`, `anUnloadedShadeReplacedLeavesOnceAndDeliversOnce`, `endingTwiceReturnsOnce`, `thePerchHoldsNothing`, `endingReturnsTheEscrowOnce`, `aDeadHolderIsNeverHandedTheEscrow`, `aBrokenPerchLeavesThePactWithoutItsPerch`, `thePactAndItsEscrowSurviveARestart`, `killingStolasIsNeverTheFastestRoute`, `recoveryNeverTakesCargoOutOfAnUnloadedContainer`, `endingWithCargoAboardStrandsItOnce`, 03:193's eight cases |
| E5 | "Two users cannot both own a unique reserved service instance accidentally" | D-02, D-04, D-08, D-09 | `twoPlayersCannotBothSealOnePlace`, `twoNegotiationsAtOnePerchSealOnce`, `oneStolasPactPerHolder`, `twoHandsCannotHoldOneRequest`; the two-client attempt (D-09), or not run |
| E6 | Extend `ConcordanceWorkerGameTests`, `ConcordanceLogisticsGameTests`, `ConcordanceSharingGameTests`, `ConcordancePersistenceGameTests` and the relevant client companion tests (09:172) | every ticket | the four suites above; `CompanionAutomationChecks` through `PeepoCompanionClientTests` (D-08); `ConcordancePactClientGameTests` (D-04, D-06, D-07) |
| E7 | The checkpoint: "a complete conversation-to-service-to-departure sequence" (09:174) | D-09 | the whole-path server test; the client sequence of the conversation, a recall and the departure |

## Interfaces expected to change

**As built in unit 1** (D-01, D-02; see their As built notes): `concordance/spirits/AgreementHolder` (new: the shared
holder, its `Holds`, `Presence` and `Sealing`) and `spirits/PactHistory` (new, `jugcraft:pact_history`); the pure
`concordance/worker/Pacts` (new); `resource/BoundWill` (an optional `Pact` with `Hold`, `Upkeep` and `Service`, not the
ticket's `place` and `hold` fields) and `BoundWillLedger` (`get`, `update`); `spirits/BoundWills` (the pact and escrow
keys, `release` returning the escrow); `Status` (two appended reasons); `Bond`; `SpiritAnchorBlock` and
`SpiritAnchorBlockEntity`; `GatheringShadeEntity`; `HearthlingEntity`; `WorkerRoster.full`; `WorkerEntity.sign`;
`Authority.presentNear`; `CourierLedger.PLACE` (public); `Code`'s Javadoc; `Agreement`'s Javadoc; `TestPlayers`. No
`PactRecords` and no `jugcraft:pacts` (D-P1). `tools/concordance_workers.py`, `tools/concordance_diagnostics.py` and
`tools/check_mod_data.py` (`check_workers`, `check_pacts`, `AUTHORITY_ROUTES`, `ENTRY_BY_ENTRY`); the lang and the
spirits codex entry regenerated. No payload, block, item, entity or art.

**Existing files edited:**

| Area | Files |
|---|---|
| Records and spirits | `concordance/resource/BoundWill.java` (optional `place`, `hold`; a six-argument constructor kept); `concordance/spirits/BoundWills.java` (the codec's optional fields); `concordance/worker/Agreement.java` (optional `beneficiaries`, D-08); `Status.java` (two appended reasons); `WorkerParser.java` (`WORK`, D-08); `Bond.java`; `SpiritAnchorBlock.java` and `SpiritAnchorBlockEntity.java` (the extracted holder, owner, outcomes, `post`); `GatheringShadeEntity.java` (removal, delivery on departure); `HearthlingEntity.java`; `ClockworkPorterEntity.java` (the extracted courier decision); `WorkerRoster.java` (if the outcome needs a capacity query) |
| Sky, Styx, journal | `concordance/sky/AstrolabeItem.java` (the shared recall); `styx/Styxhexenhammer.java` (the hint); `concordance/journal/Journal.java` (the pacts section) |
| Jade | `compat/jade/JugcraftJadePlugin.java`, `WorkerDataProvider.java`; `client/compat/JugcraftJadeClient.java` |
| Client | `JugcraftClient.java` (registrations: renderer, screens, key); `WorkerClient.java` (the hand drawn by GeckoLib, as `check_workers` requires, `tools/check_mod_data.py:17918-17919`) |

**New code and data:**
- Java: the shared agreement holder and pure `Pacts` rules (beside `Agreement`); `PactHistory` (all three built in unit
  1; `PactRecords` is not built: the pact is on the Bound Will, D-P1); the perch
  block and block entity; the pact definition and its parser; `PactMenu`, `PactRequestPayload`, `PactStatePayload`; the
  roster payloads; `StolasEntity` and `WorkshopHandEntity` in `concordance/spirits/`; the shared courier helper.
- Client: `PactScreen`, `RosterScreen`, Stolas's and the hand's renderers.
- Data: `concordance/pact/stolas.json`; `concordance/research/stolas_regard.json`; `concordance/practice/pact_service.json`;
  `concordance/worker/workshop_hand.json`; the tags `stolas_specimens` (joined to `concordance_specimens`) and
  `stolas_offerings`; four advancements; the Modonomicon research facts, nodes and hooks; the codex entry
  `entries/binding/stolas.json` (and the hand's); the perch's recipe, loot table, blockstate and models; `equivalence/excluded`.
- Assets: Stolas's and the hand's GeckoLib models, animations and textures; the perch's art and icon; `sounds.json`
  entries through the generator; lang through the generators.
- Tools: a new `tools/concordance_pacts.py` (definition, research, tags, lang, sounds, codex), merged by
  `tools/concordance.py`'s `write_all` (`:1861`) and its `SOUND_EVENTS`/`SOUND_FILES` (`:1529-1561`);
  `tools/concordance_workers.py` (`WORK`, `STATUSES`, `WORKERS`, messages); `tools/concordance_worker_models.py` (the
  hand's body); `tools/styx.py` (the hint lines); `tools/concordance_journey.py`; `tools/owner_art.py` (a table for what
  is copied from the library); `tools/select_client_tests.py` (`WEIGHTS`).
- Audits (`tools/check_mod_data.py`): `check_workers` (`WORK`, the class map, `STATUSES`, the new kind lang);
  `check_pacts` (new); `check_geckolib` (triggerable clips); `check_persistence` (`ENTRY_BY_ENTRY`); the pure-package
  list if the rules class is pure.
- Regenerated: `alchemy/values.json` and `docs/audits/survival-item-registry.json` for the new items (the route CC's
  Where to resume, item 1, records).
- Tests: the suites named above; `TestPlayers` (`survivor`, `again`); `ConcordancePactClientGameTests`; the gametest
  `fabric.mod.json`.
- Network: two new payload pairs (the pact menu's, the roster's); a menu type for the perch.
- Saved: optional Bound Will keys (as built, `pact` and `escrow`); `jugcraft:pact_history` (no `jugcraft:pacts`, D-P1);
  the anchor's optional `owner` (built in unit 1) and `post`;
  the bond's optional `seen_played`; the perch's block entity; the entities' keys. Every stamp stays at 1.

## Dependencies and order

1. D-00: this contract.
2. Unit 1: D-01, then D-02 (the holder is extracted once, in D-02; D-02's copy check is shown on the anchor). As built,
   the holder was extracted once in D-01's commit and extended in D-02's; the unit lands as one.
3. Unit 2: D-03 (data and the gate file; nothing spent), then D-04 (D-02's holder and records; D-S1 and D-S3 for the
   terms page, critique 21), then D-05 (D-02's upkeep rules; D-03's gate).
4. Unit 3: D-06 after D-04's entity; D-07 after D-02 (states) and D-05 (actions).
5. Unit 4: D-08 only after Stolas works (09:164) and Stolas's Regard can be mastered, using C-06 from #466.
6. Unit 5: D-09.

The smallest first step is D-01: no new id, block, entity or art; it closes G1 and G3 to G8 on systems already shipped
(G6's roster action waits for D-07), and D-02 closes G2, G9, G10 and G14 in the same unit.

## Risks

- **Base:** D stacks on two unmerged pull requests; a review change to #465 or #466 means a merge. `diagnostic/`, C-06
  and `TestPlayers.mock` exist only on Chunk C's branch (#466).
- **Styx is not guaranteed** (old saves, worlds without a fresh town, a resident killed for good). The non-Styx route
  is required, not optional (`CLAUDE.md:57`).
- **Route depth:** in the progression graph, the Binding Arts, Verdant Husbandry and the Assay understood, plus one
  recorded observation (which asks no research); in code only the Binding Arts gate the entry, since the cultivation and
  assay practices are recorded without a research check (D-03). `concordance_journey.py` must find the route reachable
  and not excessive.
- **Balance (D-S1):** non-masters gain recall of any pattern they observed; the Harvest Moon (Hero of the Village) can
  then be recalled daily and the Winter Crown every other day, within the one-a-day quota. Tested and stated; the owner
  may narrow it at the checkpoint.
- **Gentle upkeep's loophole:** a holder who leaves before each grace ends and returns later is warned again with a
  full grace each time, and is never suspended. Accepted as gentle (the offering is a renewable flower); the checkpoint's
  strictness question covers it.
- **Practice side effects:** the Conclave hears every recorded practice (`ConcordanceProgress.java:101-127`;
  `starbound/Starbound.java:104`). `pact_service` is recorded once a pact, so its effect on Conclave projects is bounded;
  no commission names it.
- **Audit pins:** `check_workers` fixes the kinds, the class map and "distinct 3"; `check_geckolib` checks only loops and
  assumes box UV (Flashjack's cubes are box UV; whether its regions overlap is not yet checked).
- **Test players:** vanilla's mock is creative whatever its game mode is set to; spending tests that use it would pass
  without spending. `TestPlayers.survivor` must be used, and checked to report survival.
- **Copies by operators:** `/clone`, structure blocks or pick-block can copy a block entity: D makes the copy inert but
  cannot stop operator tools duplicating items elsewhere. Escrow in a world record cannot be copied this way.
- **The legacy claim (D-P3, as built in unit 1):** a record from before Chunk D is claimed only by the block whose
  agreement's own sealed coordinates are its place, and only while its spirit, if loaded, is seated there; a copy at
  another place never claims, whichever loads first, and stays inert. Tested (`aLegacyAnchorClaimsItsPlaceOnce`);
  documented.
- **Unit 1 has not run (as built):** none of its Java has been compiled against Minecraft and none of its 29 server game
  tests has run; CI is their first compile. Two of them teleport a test player into the Nether (the repository's
  first), and the familiar and upkeep tests rely on the test world's player and statistics files being written and
  read back (`TestPlayers.leave` and `again`). If CI shows the Nether is unavailable, the Nether steps can be dropped:
  the rules are still shown 20 and 70 blocks away.
- **The Spirit Anchor keeps its own rules:** hoppers below still take its deliveries, and an explosion still ends a
  shade's agreement and drops its contents once (G15). Claims shield explosions; unclaimed land does not. Changing that
  is not in this chunk.
- **Death-property pull requests (#461, #453):** the escrow goes only to a living holder; the tests run again once
  either merges.
- **UI limits:** the encyclopedia is full; GuiLib has no narration *(survey)*; the repository has no conversation,
  confirmation or toast precedent of its own *(survey)*; whether a GeckoLib entity draws as a roster portrait is
  undetermined.
- **Assets:** the Flashjack-derived look may be rejected at the checkpoint; the raven's call is shared with
  `beast_crow`; later cohorts meet a name collision (Goetia's Murmur against `beast_murmur` and the Murmur SMG, `en_us.json:18957, 10232`).
  The licence metadata found with the source is kept with the provenance.
- **Shared hot files:** above; `sounds.json` contention with #470, `ScreenKit` with #469 and #471.
- **Two clients:** likely not available; E5 may stay unverified in multiplayer.

## Explicit don'ts

1. Do not generate 71 shallow variations, or any second Goetia spirit, before the checkpoint (09:174; 03:177). The
   workshop hand is a worker, not a Goetia identity.
2. Do not build a second UUID system, ledger, roster, courier ledger, worker base or permission model: extend
   `BoundWills`, `WorkerRoster`, `CourierLedger`, `Authority`, `Saved` and `RateGate` (03:142; 03:181).
3. Do not copy `Bond.neglected` into upkeep. Owe nothing for absence or unloaded time, accumulate no debt, never look at
   upkeep through a stand-in (03:146).
4. Do not make combat the way a pact ends; killing Stolas or a hand is never the fastest route (03:146; 02:151).
5. No unlimited ore locator, no free rare resources, no resonance outside the ledger, no revealing all research, no
   moving another player's cargo (03:137; 03:155).
6. Do not grant research for talking. Do not accept notes as personal evidence (03:132).
7. Do not record `worker_service` for Stolas, and do not add a fourth worker kind.
8. No chat commands, vanilla Dialog or mixin as the interface's protocol (12:54); no client totals, player ids or free
   text in requests (08:99); no menu slots for deposits.
9. Do not put actions in the encyclopedia, and do not ship an empty Pacts page (08:20).
10. Do not edit Styx's frozen blueprints; the perch is refused in his protected district.
11. Do not make a Bound Will transferable or give `transfer` a route; no steward bypass (02:170). Threadwax and Sever
    never touch pacts (03:38; 02:83).
12. Pact items stay out of equivalence; consumed offerings stay in it. Do not fix duplication with cooldowns (06:158;
    06:164).
13. No chunk loading (MP:54) and no dimension crossing (`WorkerEntity.java:214-218`; `check_workers`,
    `tools/check_mod_data.py:17925-17931`).
14. No edits under `src/main/java/local/peepo` (#452); do not replace Peepo with clones (03:181). The gametest companion
    checks may be extended.
15. Do not execute or bulk-import from the owner library; keep the originals; do not reuse a registered creature's
    identity (the crow, the courier bird, the Ars familiars) as Stolas; do not import a modern adaptation's design
    (03:165; `styxhexenhammer.md:37`).
16. Do not hand-edit generated data, `sounds.json` or lang; change the generators.
17. Do not mark a session decision as the owner's, count one client as two, or call the chunk complete because tests
    compile (10:162; 09:20; 09:315).

## Deferrals

- **Gloam Ink** (D-S4): not part of Chunk D. It stays a separate small outcome (C-D6, CC:2363; its timing is not yet
  decided): one formula from a mixed bed's Gloamcap, consumed by Research Notes (CC:2304-2305). Research Notes never count as Stolas's
  evidence.
- **Further spirits and the other 71** (D-002): after the checkpoint chooses the next six-spirit cohort (03:163-165),
  each with its character brief.
- **Voice lines:** none exist; subtitles only, unless the owner supplies recordings.
- **Translations** beyond English (D-S21).
- **An encyclopedia Pacts page:** needs a raised page cap (a payload change) and stays read-only (08:16, 08:20).
- **Stolas's other two topics as services:** botanical interpretation and mineral comparison (03:136, 03:149-150).
- **Pact service toward Binding Arts mastery:** `pact_service` stays out of `worker_service`.
- **The ward keeper** (beneficiaries and escrowed sigils, the delivery plan's D-Q2 B) and a machine hand through
  `MachineCompanionPort`.
- **The observatory chart** (08:61-63) and a GuiLib view of the pact screens.
- **Importing #467's two-client harness:** for its author or the owner to bring in.
- **A downgrade path** for the new keys.

## Validation plan

| Unit | Server game tests | Client game tests | Checks | Save tests |
|---|---|---|---|---|
| 1 | Worker, Authority, Sharing, Persistence, Resource | none | `check_workers`, `check_persistence`, the pact `problems()` audit, `check_repository`, `gradlew build` | an older Bound Will; an anchor's agreement; a legacy `suspended`; an unreadable record kept; stamps |
| 2 | Worker, Sharing, Persistence, Sky, Styx, Progression, Journal | `ConcordancePactClientGameTests` (conversation, terms, keyboard) | `check_pacts`, `concordance_journey --check`, `check_acquisition`, the icon maps, the economy check, the recipe-conflict fixture if #462 has merged | the perch; the escrow and clock; a restart while suspended; a reload with a broken definition |
| 3 | Worker (presence), Journal, Logistics (recovery) | poses, subtitles, Jade, the roster; the build without optional integrations | `check_geckolib` (triggerables), `owner_art --check`, client-test selection | the entity's keys |
| 4 | Logistics (the eight cases and three more), Worker, Persistence | `PeepoCompanionClientTests` / `CompanionAutomationChecks` | `check_workers` (`WORK`, the class map), the logistics audit | a claim across a restart |
| 5 | the whole path; load | the evidence bundle; the two-client attempt | every check | none |

Every unit also runs `tools/check_mod_data.py`, `scripts/check_repository.py`, `scripts/tests`, the generators (their
output current) and the build with its server game tests, and its CI by workflow_dispatch of the Build workflow with
every client class.

**Evidence rules** (10:5-22):
- Each command is reported on its own as run and passed, run and failed, or not run. CI on a commit counts for that
  commit only.
- A server game test with several test players is not a party of people. One client on a dedicated server is not two
  clients; the two-client session and a human journey stay not run unless two accounts or a test environment are
  available (09:20).
- Vanilla's mock players are creative: a spending test counts only with a survival test player.
- A CI runner's timings are not a player's hardware; a software-rendered frame time is that renderer's.
- A screenshot shows presentation, not balance; nothing is called balanced without a human playtest.
- Numbers from a generator's model stay labelled so until a game test measures them.

## Decisions

Chunk A's, B's and C's decisions are unchanged: B-D0 to B-D3, C-D0, C-D0b, C-D1 to C-D3 and C-D5 to C-D8 are the
owner's, while C-D4 and every B-P and C-P stay Proposed (Chunk C's contract, Decisions and Where to resume, CC:2351,
CC:2444-2445). D-D decisions are the owner's. D-S decisions are the session's under D-D2: the owner
asked not to be asked, so they are recorded here as the session's choices, never as accepted by the owner, and stay
reviewable at the checkpoint (10:162). D-P decisions are the units' proposals: choices the session made in building a
unit where the tickets above left room or had to be corrected (D-P1 to D-P12 from unit 1's specification, D-P13 to
D-P16 from its review), each with what was built; none is the owner's, and each is reviewable at the checkpoint.

| ID | Date | Decision | Status | Affects |
|---|---|---|---|---|
| D-D0 | 23 October 2026 | "begin chunk d": Chunk D, the pact hall (09:146-174), is the next chunk | Accepted (owner) | All of Chunk D |
| D-D1 | 23 October 2026 | "New branch on Chunk C (Recommended)": Chunk D is developed on `claude/chunk-d-pact-hall`, started from Chunk C's head `779851cdb` (#466, itself stacked on Chunk B's #465), with "A third stacked PR (D on C) when it's ready", its base `claude/chunk-c-living-workshop` | Accepted (owner) | D-00, every unit |
| D-D2 | 23 October 2026 | "dont ask me to approve just finish this whole part": the session does not stop for approvals and finishes Chunk D end to end. It therefore settles the plan's open questions with its recommendations as corrected by the critique (D-S1 to D-S23), each recorded as the session's, reviewable at the checkpoint | Accepted (owner) | every D-S decision |
| D-002 | prior owner direction (10:155) | "Stolas first, then all 72 distinct spirits" | Accepted direction (carried from the pack) | D and later cohorts |
| D-S1 | 23 October 2026 | Stolas's service is Star-reckoning: once a game day a pact, the holder at the perch recalls any pattern they observed, with their astrolabe's resonance, at its existing recall cost, under the shared `recall:<pattern>` claim, for 2,400 ticks; one recall path shared with a master's; non-masters may recall the Harvest Moon and Winter Crown as often as the quota allows (D-05) | Decided by the session under D-D2 (23 October 2026) | D-04, D-05 |
| D-S2 | 23 October 2026 | The contrasting service is the workshop hand: its own worker definition, entity, lang and model (a variant of the shade's body); a Spirit Anchor bound to the holder's Courier Post; the post's exact requests within its hours, area and quota; beneficiaries the holder and party; unlocked by mastering Stolas's Regard; the porter's courier decision extracted and shared (D-08) | Decided by the session under D-D2 (23 October 2026) | D-08 |
| D-S3 | 23 October 2026 | Strictness is Gentle: one offering in 7 game days from escrow (`#jugcraft:stolas_offerings`: three Styx flowers, amethyst shard), up to 4 prepaid, the first by the compact; looked at only with the perch loaded and the holder online, alive, in its level and within 64 blocks, never a stand-in; deadlines reschedule from the first look after an absence; warning, 1 game day of grace, suspension, resumption on paying; never ends by itself; E2 shown on Stolas's upkeep (D-02, D-05) | Decided by the session under D-D2 (23 October 2026) | D-02, D-05 |
| D-S4 | 23 October 2026 | Gloam Ink is not part of Chunk D; it stays a separate small outcome. Research Notes never count as Stolas's evidence | Decided by the session under D-D2 (23 October 2026) | Deferrals |
| D-S5 | 23 October 2026 | Stolas's body adapts the library's Flashjack rig into a raven-form runtime copy with authored clips; originals intact; provenance recorded; sounds are new `jugcraft:stolas.*` events through the generator, bound to runtime files, each with a subtitle; no voice. Appearance stays a checkpoint question (D-06) | Decided by the session under D-D2 (23 October 2026) | D-06 |
| D-S6 | 23 October 2026 | Escrow and terms live in a world record; the perch is a place marker holding nothing; deposits are server-side actions; breaking or blowing up the perch never ends the pact ("without its perch"); ending returns the escrow once to the living holder, overflow at their feet; a bounded history of 8 departures in a separate saved record (D-02, D-04, D-05) | Decided by the session under D-D2 (23 October 2026) | D-02, D-04, D-05 |
| D-S7 | 23 October 2026 | Stolas arrives as a session-bound visitor and leaves without a record or session; after acceptance, one presence a pact; no damage but bypass damage; a lost presence called back at no cost, never ending the pact; in `concordance/spirits`, MISC, refused by transport crates (D-04, D-06) | Decided by the session under D-D2 (23 October 2026) | D-04, D-06 |
| D-S8 | 23 October 2026 | The perch records its owner; only the owner invites and accepts there. New anchors record their owner; sealing needs the owner or the owner's party; ownerless legacy anchors keep today's rule plus `Authority.mayChange`; a stand-in never seals; a second acceptance at one perch is `TAKEN` and spends nothing (D-01, D-04) | Decided by the session under D-D2 (23 October 2026) | D-01, D-04 |
| D-S9 | 23 October 2026 | States: invited and negotiating transient; active and suspended (holder or upkeep) on the Bound Will, the single authority, the anchor's `suspended` migrating on load; waiting derived with distinct upkeep reasons; departed removed plus a history entry. A holder block entity counts only if the record matches id, holder and place; legacy records claimed by the first matching anchor to load; later copies inert, `CONFLICTING_AGREEMENT`, releasing nothing; the shared holder extracted from the anchor in unit 1 (D-02) | Decided by the session under D-D2 (23 October 2026) | D-01, D-02 |
| D-S10 | 23 October 2026 | One agreement a place; one Stolas pact a holder; any number of holders a world; Bound Wills non-transferable (D-02) | Decided by the session under D-D2 (23 October 2026) | D-02, D-04 |
| D-S11 | 23 October 2026 | A native roster screen and a journal "pacts" section; no encyclopedia page; rows from the roster and Bound Wills, at most 64; a `ROSTER_FULL` seal refusal (D-01, D-07) | Decided by the session under D-D2 (23 October 2026) | D-01, D-03, D-07 |
| D-S12 | 23 October 2026 | Research `jugcraft:stolas_regard` (spiritbinders, practitioner, needs Binding Arts understood): examine `#jugcraft:stolas_specimens`, practise cultivation, practise assay; no notes; the sky step a separate precondition named by Styx and the journal; mastered by the first obligation (`practice jugcraft:pact_service`, its gate shipped with the research); companion advancements, Modonomicon data and a Binding codex entry; mastery unlocks the workshop hand; talking to Styx teaches nothing (D-03) | Decided by the session under D-D2 (23 October 2026) | D-03, D-05, D-08 |
| D-S13 | 23 October 2026 | The Open Hand Compact is the acceptance cost (ordinary materials and astrolabe resonance), spent in the call that seals; not a rite. Spending tests use a survival test player: vanilla's mock reports creative whatever game mode is set on it, so setting survival on the mock is not enough (Facts; D-04) | Decided by the session under D-D2 (23 October 2026) | D-04 |
| D-S14 | 23 October 2026 | A perch-bound menu (the `ShopMenu` pattern) with bounded actions and a terms revision, carried by one small client-to-server payload (container id, sequence, action, choice, revision); no free text, player ids, client totals, Dialog, chat commands, mixins or GuiLib; a native, narrated, keyboard-operable `PactScreen` (D-04) | Decided by the session under D-D2 (23 October 2026) | D-04, D-05 |
| D-S15 | 23 October 2026 | The terms page shows service, hours, area, quota, upkeep and grace, what suspension does, duration, breach rules, how to end, what returns, beneficiaries and the compact's cost. The perch is placed only where the placer may build, never in Styx's protected district; one Stolas a holder is the entity cap (D-04) | Decided by the session under D-D2 (23 October 2026) | D-04 |
| D-S16 | 23 October 2026 | With the Concordance disabled, no upkeep falls due and nothing suspends; Stolas waits (`DISABLED`); records and escrow untouched; tested (D-02, D-05) | Decided by the session under D-D2 (23 October 2026) | D-02, D-05 |
| D-S17 | 23 October 2026 | The familiar's fade, an adjacent routine fix and not E2 evidence: the bond fades only for its person's own play time away, so offline days never count, even unloaded; days played in another dimension still count (D-01) | Decided by the session under D-D2 (23 October 2026) | D-01 |
| D-S18 | 23 October 2026 | A shade's death: its load into the anchor's slots when the anchor is loaded, otherwise dropped where it fell; "lost" read from the roster, not from an absent entity; a re-call while the old shade is unloaded makes it leave once and deliver once (D-01) | Decided by the session under D-D2 (23 October 2026) | D-01 |
| D-S19 | 23 October 2026 | Consumed offerings stay in equivalence; only pact items (the perch's item, any compact item) are excluded; the balance is documented (D-04, D-05) | Decided by the session under D-D2 (23 October 2026) | D-04, D-05 |
| D-S20 | 23 October 2026 | No edits under `src/main/java/local/peepo` (#452); the gametest companion checks may be extended (D-08) | Decided by the session under D-D2 (23 October 2026) | D-08 |
| D-S21 | 23 October 2026 | Text comes from the generators; English only | Decided by the session under D-D2 (23 October 2026) | every ticket |
| D-S22 | 23 October 2026 | #467's two-client harness is an unmerged fork contribution and is not imported; two-client checks are attempted only with what this branch has, otherwise recorded as not run (D-09) | Decided by the session under D-D2 (23 October 2026) | D-09 |
| D-S23 | 23 October 2026 | An invalid or missing pact definition reports cleanly on reload, and sealed pacts keep the terms copied at sealing; a holder leaving a party or the perch's land changing hands never moves a pact to another player; both tested (D-04, D-05) | Decided by the session under D-D2 (23 October 2026) | D-04, D-05 |
| D-P1 | 24 October 2026 | Pact data is `BoundWill.Pact`, an optional `pact` key, and the escrow an optional `escrow` key (an `ItemStack` list) of the same `jugcraft:bound_wills` entry, still version 1; there is no `jugcraft:pacts` (the ticket's `PactRecords`), so release removes a record and its escrow in one call. The departure history is `PactHistory` (`jugcraft:pact_history`, version 1, read holder by holder, 8 entries each). As built, the pact also keeps its service terms (`service`) and day's count (`window`, `done_today`) as optional keys (the review's correction 4), so a pact seated again keeps its terms and its day | Proposed (unit 1, reviewable at the checkpoint) | D-02, D-04, D-05, D-07 |
| D-P2 | 24 October 2026 | Upkeep runs on game time, on the Spire's model. It is looked at only with the Concordance on and the holder present: online, alive, in the place's level, within `Pacts.REACH` = 64 blocks, never a stand-in. At most one period falls due a look. With none in escrow, a warning and the grace; the grace over, an `upkeep` hold; paying resumes the pact. After a gap of more than `ABSENT_TICKS` = 1,200 ticks without a look, a warned pact is warned again with the whole grace. The holder's own suspension pauses the clock: due date and grace move on by the time held. The last look is written at most every `WRITE_TICKS` = 200 ticks. As built (correction 3): paid, the next falls a period after the due date, or a period from now if that has passed; after an absence a passed due date becomes the moment of return; resumed from an upkeep hold by an offering, the next falls a whole period from then. Neither time away nor time held is charged | Proposed (unit 1, reviewable at the checkpoint) | D-02, D-05 |
| D-P3 | 24 October 2026 | A record from before Chunk D is claimed only by the block whose agreement's own `dimension`, `x`, `y` and `z` are its place, and only while its spirit, if loaded, is seated there; a copy elsewhere never claims, whichever loads first (stricter than D-S9's "first claim wins"). A new seal or claim at a place still keeping an orphan record leaves that record without its place, its holder and escrow kept. As built, the claim also copies the block's service terms and day's count onto the record | Proposed (unit 1, reviewable at the checkpoint) | D-02, D-04 |
| D-P4 | 24 October 2026 | After the claim the block's `Agreement.suspended` is written false; the record's hold is the only authority. A downgrade is unsupported | Proposed (unit 1, reviewable at the checkpoint) | D-02 |
| D-P5 | 24 October 2026 | `TAKEN` means the holder block already keeps terms, not that the ledger has some record at the place: safe with game-test structure clearing (flag 818 skips side effects) and with crash orphans | Proposed (unit 1, reviewable at the checkpoint) | D-02, D-04 |
| D-P6 | 24 October 2026 | Sealing an owned holder block needs its owner, or their party where the block lets parties seal, **and** `Authority.mayChange` at the block; an ownerless legacy anchor needs only `mayChange`; a stand-in never seals. As built: `Sealing.partySeals` (the anchor true; D-04's perch false, D-S8; correction 6); "the Binding Arts are not understood" stays the anchor's own check before sealing, worded as before, not a seal outcome | Proposed (unit 1, reviewable at the checkpoint) | D-01, D-04 |
| D-P7 | 24 October 2026 | A block whose Bound Will is gone (and not merely unreadable) forgets its terms when it next verifies, and may be sealed again; its shade then departs, its load dropped at the anchor (which no longer keeps its holder's agreement, D-P12). An unreadable record makes the block and its shade wait | Proposed (unit 1, reviewable at the checkpoint) | D-02 |
| D-P8 | 24 October 2026 | `end`, `toggle`, `recall` and `deposit` are not rate-gated themselves; the anchor's use keeps `RateGate.allow(server, "workers", 4)`. D-04's and D-07's menus and roster actions must call `RateGate.allow` before `end`, `toggle`, `deposit` and `recall` (a later `check_pacts` needle can hold it at their payload handlers). As built: suspend and resume by record id alone (`AgreementHolder.toggle(level, player, id, now)`), for D-07's roster while the place is unloaded (correction 9); `end` has the same static form | Proposed (unit 1, reviewable at the checkpoint) | D-02, D-04, D-07 |
| D-P9 | 24 October 2026 | The anchor gets no server ticker: its definition has no upkeep. `AgreementHolder.look` is the rule's driver, called by D-04's perch ticker. `OnBreak.UNSEAT` (a broken perch leaves its pact without its place) arrives with D-04 | Proposed (unit 1, reviewable at the checkpoint) | D-02, D-04 |
| D-P10 | 24 October 2026 | Status ids `upkeep_due` and `upkeep_held` are appended (each a want sign) | Proposed (unit 1, reviewable at the checkpoint) | D-02, D-07 |
| D-P11 | 24 October 2026 | `TestPlayers` gains `survivor`, `leave`, `again`, `played` and `play`. Editing `TestPlayers.java` makes `tools/select_client_tests.py` choose every client class (a shared file); accepted. As built: every class, 239 | Proposed (unit 1, reviewable at the checkpoint) | D-01, D-02 |
| D-P12 | 24 October 2026 | A departing or killed shade's load goes into the anchor at its seat when that anchor is loaded and keeps an agreement of the shade's own holder, whether or not it still names the shade; otherwise it drops at its anchor's place (departing) or where it fell (killed). The specification had any anchor at the seat; as built, never one sealed again by someone else (correction 12) | Proposed (unit 1, reviewable at the checkpoint) | D-01 |
| D-P13 | 24 October 2026 | Switched off, a loaded familiar whose person is online (in any level) restarts its play-time count at each look, so it fades for none of their play meanwhile (correction 13, D-S16). One not loaded while the Concordance was off counts its person's play meanwhile at its first look after the switch is on: no switch-off time is recorded. The residual case is accepted and said in the records | Proposed (unit 1, reviewable at the checkpoint) | D-01 |
| D-P14 | 24 October 2026 | A seal may carry a cost (`Sealing.affords` and `pay`): asked after the refusals and before anything is called, refused as the appended outcome `CANNOT_AFFORD` (`seal_cannot_afford`, nothing spent), and paid in the same server call only once the Bound Will is recorded (correction 10, D-S13). The anchor's seal costs nothing; D-04's Open Hand Compact uses it | Proposed (unit 1, reviewable at the checkpoint) | D-01, D-04 |
| D-P15 | 24 October 2026 | A holder block calls its presence by counterpart (`Holds.call(level, who, counterpart)`) and calls nothing (so `UNAVAILABLE`) for a counterpart it does not serve: the anchor serves only `jugcraft:gathering_shade`. Every presence (`AgreementHolder.Presence`) is a `WorkerEntity` subclass, unit 2's Stolas and unit 4's hand included, so each has a roster entry to note and strike (corrections 7 and 8) | Proposed (unit 1, reviewable at the checkpoint) | D-01, D-04, D-06, D-08 |
| D-P16 | 24 October 2026 | Breaking a holder block while the Concordance is off releases nothing: its record stays kept at that place with its escrow, no departure is written and its shade is not sent away (correction 5, D-S16). Once the switch is on, the shade finds no anchor and departs, its load dropped where the anchor stood; the next seal there leaves the record without its place; its holder can end it (by code until D-07's roster) | Proposed (unit 1, reviewable at the checkpoint) | D-02, D-07 |

**The checkpoint's three questions** (09:174; at most three, 09:18):
1. Stolas's personality and appearance: the Flashjack-derived raven, his three dialogue beats and two memorable details
   (03:168, 03:174).
2. How strict pacts should feel after play: the period, the grace, the offering, and whether a pact should ever end
   after missed offerings.
3. Which six-spirit cohort follows, from the twelve of 03:165.

The session's decisions are listed for review beside them in the checkpoint's technical handoff, not as further
questions.

## Where to resume

Unit 1 (D-01, D-02) is built on the local `wip/chunk-d` (four commits on `a8c7cd770`: `56a74f4be`, `7477efbcd`,
`fa5360d0d` and the records' commit), not pushed when these notes were written, and nothing of it has run in CI. Before
more work:
1. Push to `claude/chunk-d-pact-hall` and run the Build workflow by workflow_dispatch with every client class (CI is
   the unit's first compile); fix what fails and record each failure and fix in D-01's or D-02's As built notes, the
   units table and the workers record.
2. Refresh main and the open pull requests (REST, read only); merge main if it has moved past `f904df3fd`, and Chunk
   C's branch if it has moved in review.
3. Unit 2 next (D-03, D-04, D-05), on the holder and records unit 1 built: the perch is a second `Holds` (calling
   Stolas by counterpart, `partySeals` false, the compact through `Sealing.affords` and `pay`, D-P14, D-P15), its ticker
   drives `AgreementHolder.look` (D-P9), `OnBreak.UNSEAT` leaves a broken perch's pact without its place, a pact is
   seated again at a new perch with the record's own terms and day (D-P1), and its menu asks `RateGate` before `end`,
   `toggle`, `deposit` and `recall` (D-P8).
4. Keep this page's units table, decisions and evidence current as each unit lands, with an As built note per ticket.

## How this contract was checked

- **Re-read at `779851cdb` for this page:** `Agreement`, `Bond`, `BoundWill`, `BoundWillLedger`, `BoundWills`,
  `SpiritAnchorBlock`, `SpiritAnchorBlockEntity`, `GatheringShadeEntity` (its decision, departure and save),
  `HearthlingEntity` (its decision, removal and bond codec), `WorkerEntity`'s status, think and registration lines,
  `WorkerRoster`, `Workers`, `Status`; `Authority` (present to `mayChangeFor`); `AstrolabeItem` (charge and recall),
  `AstralClaims`, `Calendar`, `Sky` (season, obscured, up, pulse, command), `ObservatoryBlockEntity` (use and observe),
  the six pattern files; `Spires`' upkeep; `Styxhexenhammer`, `StyxState`, `JugcraftStyx`, `StyxFlowerBlock`, Styx's
  lang lines and `tools/styx.py`'s; `TownState`'s district default; `Journal`'s caps and contributors; `Encyclopedia`'s
  pages and `EncyclopediaPayload`'s cap; `ShopMenu`'s checks; `JugcraftJadePlugin`; `RitualDefinition`'s results; the
  exclusion tag; `TestPlayers`; `AlchemyArtifactGameTests.player`; `Code`; `CourierPostBlockEntity.mayUse`;
  `CourierLedger`'s cargo rule and `recover`; `Couriers.KEY_POST`; the porter's courier decision and removal (by line);
  `CrucibleBlockEntity`'s shortage and remainder (by line); `MobTransportCrate`'s `owned` and `friendly`;
  `NouveauHelpers`' roster use and `NouveauHelper.kind`; `LandProtection`'s callbacks; `Logistics`' limits;
  `EvidenceRule`'s notes rule; `ConcordanceProgress`' record and listen; `RulesParser`'s unlocks;
  `ProgressionGraph`'s practice gates; `binding_arts.json`; the practice gates `cultivation`, `assay`, `observation` and
  `worker_service`; the specimen tags; the anchor's loot table; `values.json` for the amethyst shard, feather, bone meal
  and potted Stolas Starflower; the commission `bound_service`; `tools/check_mod_data.py`'s `check_workers`,
  `check_geckolib`, `check_persistence` and the encyclopedia's forbidden list; `tools/concordance_workers.py`'s `WORK`,
  `STATUSES` and `WORKERS`; `tools/concordance.py`'s sound tables and `write_all`; `tools/generate_material_data.py`'s
  `sounds.json`; the named suites' `@GameTest` counts and the test lines cited; `docs/ROADMAP.md:26-34`,
  `styxhexenhammer.md:37`, `arcane-concordance-workers.md:145-146, 191-192, 213-214`, `docs/TESTING.md:5, 43`,
  `CLAUDE.md:57-58, 64`; plan 09:1-35 and 09:146-174, 09:294-315, 03:25-42 and 03:118-195, 10 (whole), 02:16, 41, 83,
  129, 146-153, 169-170, 06:26, 117, 152, 158, 164, 08:8-22, 55-127, 140-182, 11:51-61, 12:54, MP:50-87; Chunk C's
  contract at the lines cited.
- **Read in the owner library (nothing executed):** Flashjack's geometry (bones, UV style, sheet), animation (clips) and
  hashes; the Ars Elemental metadata; `PROVENANCE.md`; the runtime sound files named in D-06.
- **In `mc263src` `HEAD`:** `GameTestHelper.makeMockServerPlayerInLevel`, `Player.isCreative` and
  `hasInfiniteMaterials`, `ServerPlayer.gameMode` and its play-time award, `Stats.PLAY_TIME`; an explosion's block
  removal (`BlockBehaviour.onExplosionHit`, `LevelChunk.setBlockState`).
- **Read by REST on 23 October 2026 (read only):** the open pull requests (20, at 22:15Z), main's head, the branch
  `claude/chunk-d-pact-hall`, and `/pulls/N/files` for #452, #453, #462, #463, #467, #469, #470, #471 and #472
  (#452's patch to `MobTransportCrate.java` read). A verification pass at 22:43Z read again the open pull requests (21:
  #473 new), main's head, the branch and its pull requests (none), #465's and #466's mergeability, reviews and check
  runs, `/pulls/N/files` for every open pull request (#461's capped by the listing), and the patches of #452
  (`LandRegistry.mayBuildFor`) and #463 (`LandProtection`).
- **Also re-read after the first draft, so no longer marked *(survey)*:** `BookArt`'s `Paragraph` and `Tab`,
  `ComposerScroll` and `TransportCratePreview` (that they exist), `EncyclopediaKeys`' key list, `GraveThrallEntity`'s
  trigger, `CinderTyrantClientGameTests`' tick freeze, `mc263src`'s `MinecraftServer.handleCustomClickAction` and
  `ConfirmScreen`, the doorbell chime, `concordance_worker_models.py`, `concordance_journey.py:93`, `NouveauFamiliars`
  and `NouveauHelpers`, the Academy's lesson and section, the Murmur names, `Examination`'s branches.
- **Taken from the survey reports, the delivery plan or its critique without re-reading, and marked *(survey)*:**
  #461's files and mixin (its file list exceeds the REST listing); the owner library's lack of owl or raven art; the
  widgets' internals beyond their declarations; GuiLib's lack of narration and the repository's lack of its own
  conversation, confirmation or toast precedent. #463's, #467's and #462's contents, #452's
  `LandRegistry.mayBuildFor`, #465's and #466's CI state and the shared files of #468, #459, #455, #454, #449 and #448,
  marked *(survey)* in the first draft, were read by REST in the verification pass.
- **Not determined:** whether a GeckoLib entity draws as a roster portrait; whether Flashjack's UV regions pass
  `check_geckolib`; whether two clients can be run here.
- **For unit 1's As built notes and proposals (24 October 2026):** re-read at the unit's head `fa5360d0d`: `Pacts`,
  `BoundWill`, `BoundWillLedger`'s additions, `BoundWills`' codecs and methods, `PactHistory`, `AgreementHolder`
  (whole), `SpiritAnchorBlock`, `SpiritAnchorBlockEntity`, `GatheringShadeEntity`, `HearthlingEntity`'s and `Bond`'s
  changes, `Authority.present`, `presentNear` and `answering`, `Status`, `WorkerEntity.sign`, the generator tables and
  audits this unit changed, and every new test's name and Javadoc; the unit's specification and its review's 16
  corrections. Run: the unit's audit mutations, the parse check, the pure classes' `javac` and harnesses, client-test
  selection and the records commit's local checks (D-02's As built).
- **Run for this page, after the verification pass's corrections:** `python3 scripts/check_repository.py` (passed:
  repository structure and local documentation links) and `git diff --check` (clean; the page is untracked, so it was
  also checked as a new file with `git diff --no-index --check`, clean). Not run: any build, game test, client,
  generator or other check.
