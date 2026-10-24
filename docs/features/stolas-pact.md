# Stolas's pact

Status: implemented, unmerged (Concordance Chunk D, unit 2: D-03, D-04, D-05); owner checkpoint pending. None of its
Java has been compiled against Minecraft and none of its game tests has run: there is no Minecraft build where it was
written, so CI is its first compile and run (see Verification).
Proposal issue: none; the owner's selected scope ([the chunk's contract](concordance-chunk-d.md), D-D0 to D-D2).
Owner: @jimbozoomer-byte. AI-assisted implementation with Claude Code; the model is named in the commit trailers.
Target milestone and tier: Concordance Chunk D; Practitioner stage, Spiritbinders (tier: practitioner).
Primary specialty and supported player role: the Binding Arts, with the sky's observers. A pact with Stolas, the raven
of the Ars Goetia who teaches the stars, gives its holder a chosen recall of a sky pattern they observed themselves.

Builds on [spirits, familiars and constructs](arcane-concordance-workers.md) (the shared agreement holder, the Bound
Will's pact and escrow from unit 1), [celestial cycles](arcane-concordance-celestial.md) (observations, the astrolabe
and its recall) and [Styxhexenhammer](styxhexenhammer.md). Session decisions D-S1 to D-S23 and this unit's proposals
D-P17 to D-P50 are in the [contract](concordance-chunk-d.md#decisions); none is the owner's.

## Player experience

Stolas's Regard, a Spiritbinders research entry, is found through Styxhexenhammer's hints or without him (the codex's
Binding category, the journal's Pacts section and a feather). With the Binding Arts understood:

- examine a feather or one of Styx's three flowers, harvest a ripe organism crop by hand, and dissolve a sample at an
  Assayer's Scale (in any order; Research Notes never stand in): Stolas's Regard is understood;
- record a sky observation at an Orrery Observatory while a pattern is up and visible (not research: the invitation's
  precondition, which Styx and the journal name apart);
- build a **Pact Perch** where you may build and use it: Stolas arrives on it and speaks three beats, then names every
  term before anything is spent;
- **accept**: the Open Hand Compact is paid and the pact sealed in the same server action; **decline**, closing the
  screen or any refusal spends nothing, and a refusal says why;
- once a game day at the perch, **Star-reckoning** recalls a sky pattern you observed yourself, paid from your
  astrolabe; recording a sky observation while the pact is active keeps its first obligation and masters Stolas's
  Regard;
- pay offerings ahead, **suspend**, **resume** or **end** it at the perch (ending asks first).

Breaking the perch ends nothing: Stolas leaves the world and the pact, with everything paid ahead, waits until it is
seated at a new perch of its holder's, or ended.

Styx tells each listener their own next step (he teaches and grants nothing). His four lines and his once-a-player gift
of cuttings are unchanged.

## Connections

- Tier: Practitioner (Stolas's Regard requires the Binding Arts understood, and nothing else in code: D-P17).
- Input producers:
  - the Binding Arts (understood);
  - ripe organism crops from Verdant Beds (the cultivation practice);
  - the Assayer's Scale (the assay practice);
  - Orrery Observatories (observations, and Astral Resonance carried in an astrolabe);
  - a feather, or Styx's flowers (the specimens, `#jugcraft:stolas_specimens`);
  - the perch's recipe: two sticks, an amethyst shard, a dark oak log and polished deepslate;
  - offerings (`#jugcraft:stolas_offerings`: Stolas Starflower, Ravenquill Lupine, Amethyst Mourningbell, or an
    amethyst shard).
- Output consumers:
  - recalled attunements (the six patterns' effects, 2,400 ticks each);
  - Stolas's Regard mastered (the first obligation), which D-08's workshop hand (unit 4) will consume;
  - the Starbound Conclave hears `pact_service` once a player, as it hears every recorded practice.
- Technology and magic connections:
  - the shared agreement holder (`AgreementHolder`), the Bound Will's pact, escrow and upkeep rules, and the departure
    history (unit 1);
  - the astral ledger and the one recall path (`AstrolabeItem.recall`, shared with a master's recall);
  - the research engine (examination, practices, advancements, the codex and the journal).
- Unlocks:
  - Stolas's Regard understood: the invitation (with one observation recorded);
  - the pact: Star-reckoning;
  - the first obligation: Stolas's Regard mastered.
- Reachable entry path: `tools/concordance_journey.py`'s Stolas's Regard route, five steps, one player alone in the
  Overworld, nothing past the Practitioner stage and no structure loot (a feather stands in for Styx's flowers);
  `python3 tools/concordance_journey.py --check` holds it and the [journey record](arcane-concordance-journey.md)
  shows it. It is a check over the data, not a played journey.
- Required versus optional:
  - Styx is optional: the feather route reaches every step;
  - offerings have a source without Styx (the amethyst shard);
  - nothing gates core progression.
- Specialty: the pact is personal, one with Stolas a holder; its holder's party does not benefit. An observer without
  the pact keeps the master's own recall of the pattern they last observed.

## Costs, balance and automation

- **The Open Hand Compact:** 2 amethyst shards, 3 feathers and 8 Astral Resonance from one carried astrolabe, paid by
  the seal itself (`Sealing.affords` and `pay`, unit 1's D-P14) and only once it is recorded. A player with infinite
  materials (creative) pays nothing, as vanilla's crafting does (D-P26). It counts as the first offering.
- **Upkeep (D-S3, Gentle):**
  - 1 offering every 7 game days, from the offerings paid ahead into the pact's escrow (at most 4);
  - an offering due with none paid ahead: a warning, 1 game day of grace, then suspension for upkeep, with nothing
    taken or lost; an offering resumes it;
  - looked at only while the perch is loaded and its holder is online, alive, in its level and within 64 blocks, never
    through a stand-in; time away, unloaded or switched off is never owed;
  - after an absence (over 1,200 ticks without a look) the holder is warned again: a first warning gives the whole
    grace, and one given before the absence gives the grace they had left, its end moved on by the time away (D-P50,
    the unit review's proposal amending D-S3);
  - two routes around it, found by the unit review (checkpoint question 2): before D-P50, a warned holder away for over
    a minute at least every 20 minutes got the whole grace again at each return and was never suspended, while
    Star-reckoning kept serving (closed by D-P50); and suspending, recalling and resuming once a day moves the due date
    on by the time held, so the 7-day period counts only a few ticks of active time a day (open, left to the owner);
  - a pact never ends by itself.
- **Star-reckoning:** the pattern's own recall cost (6 to 16 resonance), 1 a game day a pact, within 8 blocks of the
  perch at any hour, for patterns the holder observed; one recall an occurrence of each pattern, shared with a master's
  recall under the same `recall:<pattern>` claim. The effect lasts 2,400 ticks whatever the sky or the season.
- **Balance, stated plainly (a checkpoint question):**
  - a holder who is not a master and once observed the Harvest Moon may recall it (Hero of the Village, with its
    villager trade discounts) any game day, and the Winter Crown (Fire Resistance) every other day, outside their event
    and season;
  - the once-a-day quota is the only hard limit. Resonance does not bound it: Star-reckoning pays from any carried
    astrolabe that holds enough (`PactCompact.astrolabe`), claims are each keeper's own
    (`ObservatoryBlockEntity`), and astrolabes can be stockpiled, handed over or filled at a keeper's observatory by
    their party, so stockpiled or traded astrolabes allow a recall every day;
  - one keeper's averages, for a player relying on their own observatory alone: it gathers about 2.09 resonance a day
    from the four year-round patterns, 3.59 with the Winter Crown (in winter, or all year with seasons off, when the
    Winter Crown can be observed in any season), so about one Harvest Moon recall (12) every 5.7 days, or every 3.3
    days with the Winter Crown, if every pattern is seen; an astrolabe holds 32;
  - a master holding the pact gains no second recall of an occurrence (both routes claim the same key), but gains a
    free choice of any pattern they observed, where a master's own recall takes only the last one;
  - ending a pact and sealing a new one could give a second Star-reckoning in the same game day, but only for a whole
    new compact (its materials and resonance) each time: no positive-gain loop (review correction 10).
- **Economy (D-P49):** Star-reckoning is a sink (`astral_resonance` → `recalled_attunement`, no way out), modelled at
  the cheapest recall; offerings are a sink of at most 1/7 of an offering a day a pact. `tools/concordance_economy.py`
  still reports "gaining cycles: none" ([economy record](arcane-concordance-economy.md)). Consumed offerings stay in
  equivalence (D-S19); only the perch's item is excluded, and `values.json` values it from its recipe at 74.
- **Automation:** none. Every action is a player's, in person, at the perch: no hopper, pipe or machine pays, reckons
  or ends (the perch holds no item and has no slots).

## Failure behaviour, multiplayer and persistence

- **Every action is server-side,** behind the rate gate (`RateGate`, one key for opening, one for acting and one for
  hearing the terms, D-P35):
  - a request is five bounded numbers (container, sequence, action, choice, revision): no text, ids or totals;
  - only the perch's owner, in person, may accept there (never their party, never a stand-in);
  - only the pact's holder, in person, may reckon, pay, suspend, resume, end or seat it;
  - a stand-in never answers for upkeep, and pays, reckons, ends and suspends nothing for its player.
- One pact with Stolas a holder; a second acceptance at one perch is refused as taken and spends nothing, and a
  player who is not the perch's owner is refused as not permitted. A perch set by a machine (a blueprint stake or a
  drone build, which never ask the placement rules) records no owner and seals and seats for nobody (D-P24, as
  corrected by the unit review); break it and place it by hand. Its screen says only its owner may invite Stolas.
- Decline closes the screen in the same call, so its answer is shown on the action bar (which the narrator reads), not
  as a state for a screen that has closed (the unit review).
- A definition whose work serves its holder alone (Star-reckoning) must name `"personal"` beneficiaries; `"party"` is
  refused with a reason (`PactParser.HOLDER_ONLY`, the unit review), so the screen never promises a party a service
  that serves only its holder.
- A stale terms revision is answered "changed" with the terms again, and nothing is spent (D-P23). Every refusal
  names its reason; Accept is offered only when nothing stands in the way, and a forced Accept is still answered with
  its reason (D-P34).
- **Breaking or blowing up the perch** (the Concordance on): the pact is left without its perch, its escrow, hold and
  terms kept; Stolas leaves the world. Seat it at a new perch of yours (`AgreementHolder.seat`: the same record,
  terms, day's count, upkeep clock and escrow; D-P45), or end it from any perch that keeps nothing. Broken while
  switched off: the record is untouched and stays kept at its vanished place, and once its place is loaded keeping
  nothing it can be seated again (`AgreementHolder.stranded`, D-P25).
- **Ending** returns what was paid ahead once, into the living holder's inventory (the rest at their feet), never to a
  dead holder; Stolas departs and the departure is kept in the holder's history. Suspend and resume are idempotent
  (D-P46).
- **A lost Stolas** (killed by damage that bypasses invulnerability, or removed) returns to the perch at no cost while
  his holder is within 64 blocks of it, or when the holder opens the perch, if their roster has room (D-P28); a lost
  presence never ends the pact.
- **Conversations** live in memory only: one a player, bound to the open screen, ending after 6,000 ticks without a
  request, when the screen closes or when the player leaves (cleared on leaving the player list and on disconnect;
  D-P27). A visiting Stolas waits while any conversation at his perch is live, the owner's or anyone else's, and leaves
  when none is (review correction 9), with the Concordance on or off (the unit review).
- **The first obligation** is kept only while the pact is active and kept at its perch: not while it is suspended, held,
  without its perch, or stranded at a perch broken while the Concordance was off (the unit review).
- **Switched off** (`concordance.enabled=false`): every clock waits; nothing is reckoned, paid, ended or seated, and the
  screen says the Concordance is off. Records, escrow and registrations are untouched.
- **Saves:**
  - the perch's block entity: `owner`, `agreement` (the terms as sealed there) and `spirit` (its presence), stamped
    version 1; no items, and no visitor;
  - Stolas: `perch` and `record`, with `WorkerEntity`'s stamp. A visiting Stolas is written with his chunk too, with
    `perch` only; he is no perch's visitor when he loads, and leaves at his first think, with the Concordance on or off;
  - the Bound Will's pact and escrow, and the history: unit 1's keys, no new key; a pact without its perch is saved with
    no `place` and no `presence` (both optional);
  - recall claims: `recall:<pattern>` on the player's astral ledger, the format unchanged, so a master's recalls before
    and after D-05 share one ledger;
  - conversations: memory only;
  - `data/jugcraft/concordance/pact/stolas.json`: schema 1, read strictly.
- **A definition that fails to load** is reported with the rules' problems and refuses new invitations with the reason;
  sealed pacts keep the terms copied onto their record.
- **Chunk unload** is modelled in the tests as a gap between the perch's looks, not as a chunk-load listener.

## The pact screen (D-04)

`PactScreen`, a native narrated screen (a plain `Screen` with `MenuAccess<PactMenu>`, the lectern's pattern; D-P38),
opened by using the perch (`PactMenu`, an extended menu with no slots):

- his three beats, a page each, then the terms page (D-P39): service, hours, area, quota, upkeep, grace, suspension,
  duration, breach, ending, returns, the perch, beneficiaries and the Open Hand Compact, its materials drawn, and what
  stands in the way;
- a kept pact: how it stands, Star-reckoning with today's remaining count and the observed patterns (each saying whether
  this occurrence can still be recalled), the offerings carried, Suspend or Resume, and End, which asks first through
  vanilla's confirmation, where Esc keeps the pact (D-P40);
- a pact without its perch: Seat here, or end it;
- operable by keyboard alone: the keyboard lands on the terms' first line, never on Accept; Esc, Leave and Decline agree
  to nothing; every line is a focusable, narrated paragraph and the server's answer is shown and read aloud.

It fits windows down to 320×180 scaled pixels and scrolls its body; the client test draws it at GUI scale 4 in a
1920×1080 window and at GUI scale 2 in a 640×360 one. `PactClient` sends only the five bounded numbers for the open menu
and decides nothing. `PactStatePayload`'s lists are capped at 3 beats, 16 terms, 12 notes, 8 patterns, 8 offerings and 8
compact stacks.

## Dependencies and assets

No new dependency (Fabric API and GeckoLib as pinned). Stolas's provisional body (D-S5, D-P41) is the owner library's
Flashjack rig, copied byte for byte under his name by `tools/owner_art.py` from `tools/concordance_pacts.py`'s
`OWNER_FILES` (`python3 tools/owner_art.py --provenance pacts` prints the rows):

| Runtime file (`src/main/resources/assets/jugcraft/`) | Library file (`art/owner-library/originals/Magic/assets/ars_jymbaumental/`) | SHA-256 | Change |
|---|---|---|---|
| `geckolib/models/entity/stolas.geo.json` | `geo/flashjack.geo.json` | `d23b5372d27cb76ea5974b2a1e78f3cba7b5a8f96cd14bef54d1e14249adc837` | none, byte for byte |
| `geckolib/animations/entity/stolas.animation.json` | `animations/flashjack.animation.json` | `c4015c9351dd54c3fec3589a544db4110809dc11ce60e040ad1cc7870c9491f8` | none |
| `textures/entity/stolas.png` | `textures/entity/flashjack.png` | `567b44a51e083f2a9ec81db92a8743435985b630f8aca98ef62baed2393ea514` | none |

- Drawn by GeckoLib at 1.4×: `idle.sitting` played and held while serving or hearing a visitor, `idle.ground` off duty
  (suspended, held, his holder away, switched off), `idle.flapping` astir (an offering due). `attack` and `idle.air` are
  copied but unused. Not imported: `flashjack.bbmodel`, `flapjack.png` and `bluejay.png`. `check_geckolib` checks his
  model by face (three UV regions of the rig overlap as drawn; no face overlaps, D-P42).
- Supplied labels and metadata, retained: the folder's leftover mod metadata names Ars Elemental, author Alexthw,
  `license="GNU Lesser General Public License v3.0"`
  (`art/owner-library/ports/jug-nouveau/ars_jymbaumental-1.21.1-0.7.10.4/META-INF/neoforge.mods.toml:7, 15, 25`). The
  owner's declaration is [PROVENANCE.md](../../art/owner-library/PROVENANCE.md) (lines 3-7). Catalog rows:
  `art/owner-library/catalog/files.csv:21291` (animations), `:21330` (model) and `:21676` (skin); `:21329` is the
  Blockbench project, not imported.
- Unit 2 shows the supplied yellow-and-blue skin. The raven recolour and authored clips are D-06's (unit 3); the look
  and the licence label are checkpoint questions.
- The perch's item icon is drawn fresh by `tools/item_icons.py` from its map (`tools/item_icons/pact_perch.txt`,
  docs/ITEM_ICONS.md). Its block model is three boxes (a stone foot, a post and a crossbar) on Jugcraft's own
  Lampwright's Bench wood and Spirit Anchor stone; provisional, D-06 may refine it.
- No sounds or voice yet (D-06 adds his sounds with subtitles); his words are the generators' (English only, D-S21).

## Verification

Each entry is what actually ran. No server or client game test of this feature has run: CI is their first run.

- **Server game tests (46 new, none yet run in CI):**
  - D-03 (9): `StyxGameTests` `styxHintsByTheListenersOwnProgress`, `talkingGrantsNoResearch`,
    `styxNamesTheSkyStepUntilOneIsRecorded`; `ConcordanceSharingGameTests.notesNeverStandInForStolasEvidence`;
    `ConcordanceProgressionGameTests.stolasRegardIsReachable`; `ConcordanceSkyGameTests`
    `aRecordedObservationCountsWithoutAWindow`, `aTurnedClockMakesNoNewClaim`; `ConcordanceJournalGameTests`
    `thePactsSectionNamesTheSkyStep`, `thePactsSectionWaitsForTheBindingArts`.
  - D-04 (17): `ConcordanceWorkerGameTests` `stolasExplainsBeforeAgreeing`, `aStaleTermsRevisionIsRefused`,
    `aRefusalSpendsNothing`, `acceptingSealsOnceAndSpendsOnce`, `oneStolasPactPerHolder`,
    `anotherPlayerCannotAcceptForTheHolder`, `twoNegotiationsAtOnePerchSealOnce`, `aVisitingStolasLeavesWithoutARecord`,
    `aCrateRefusesStolas`, `stolasTakesNoOrdinaryDamage`, `aBrokenPactDefinitionIsReported`,
    `thePactScreensMessagesAreBounded`; `ConcordancePersistenceGameTests` `aPerchKeepsItsOwnerAndPact`,
    `thePerchHoldsNothing`; `ConcordanceSharingGameTests.aCopiedPerchIsInert`;
    `ConcordanceAuthorityGameTests.aPerchIsPlacedOnlyWhereItsPlacerMayBuild`;
    `ConcordanceResourceGameTests.thePactDefinitionParsesStrictly`. Extended: `blockEntitiesStampTheirVersion` (the
    perch) and `aChangeOfOwnerMovesNoPact` (a perch whose land changes hands).
  - D-05 (20): `ConcordanceSkyGameTests` `stolasRecallsAnObservedPatternOncePerOccurrence`,
    `aMastersRecallAndStolassShareOneClaim`, `aChangedClockGivesNoSecondRecall`, `theHarvestMoonRecallsOutsideItsEvent`,
    `anUnobservedPatternIsRefused`; `ConcordanceWorkerGameTests` `theObligationMastersTheEntryOnce`,
    `aSuspendedPactRecallsNothing`, `theQuotaAndTheAreaRefuseWithReasons`, `aStandInNeverAnswersForUpkeep`,
    `endingReturnsTheEscrowOnce`, `aDeadHolderIsNeverHandedTheEscrow`, `aBrokenPerchLeavesThePactWithoutItsPerch`,
    `aPactIsSeatedAgainAtANewPerch`, `aLostPresenceIsCalledBackAndEndsNothing`, and the unit review's
    `shortAbsencesNeverRenewTheGrace`; `ConcordanceSharingGameTests`
    `aCopiedPerchRecallsNothing`, `anotherPlayerCannotUseTheHoldersService`; `ConcordancePersistenceGameTests`
    `thePactAndItsEscrowSurviveARestart`, `aRestartWhileSuspendedStaysSuspended`;
    `ConcordanceResourceGameTests.theServiceRulesKeepTheirShape`. Extended: unit 1's
    `upkeepWarnsThenSuspendsAfterGraceAndResumes`, `aReturnAfterAbsenceGetsTheFullGrace`,
    `absentAndUnloadedTimeIsNeverOwed` and `theConcordanceOffHoldsEveryClock`, each now shown on Stolas's own pact too,
    and `aChangeOfOwnerMovesNoPact` (the perch's new owner is not served).
  - Spending is shown with `TestPlayers.survivor`; vanilla's mock players are creative and pay nothing (D-P26).
  - Changed by the unit review: `twoNegotiationsAtOnePerchSealOnce` (the other player is not permitted; a perch no
    player placed seals for nobody), `theObligationMastersTheEntryOnce` (a stranded pact keeps nothing),
    `aVisitingStolasLeavesWithoutARecord` (a visitor loaded while switched off leaves), `thePactScreensMessagesAreBounded`
    (a 209-character choice id is sent and read back) and `thePactDefinitionParsesStrictly` (`"party"` for
    Star-reckoning is refused).
- **Client game test (not yet run):** `ConcordancePactClientGameTests`, twelve shots: the conversation, the terms at
  three sizes, acceptance refused (`cannot_afford`) and then sealed by keyboard, Stolas on his perch, a recall and an
  offering, the end's confirmation and the ended pact. One client on the game's dedicated server; no two-client
  playtest. `tools/select_client_tests.py` weighs it at 150 s (an estimate), and
  `scripts/tests/test_client_test_selection.py` checks that Stolas's assets select it.
- **Audits** (`tools/check_mod_data.py`, run and passing): `check_stolas_regard` (D-03), `check_pacts` (D-04, D-05),
  `check_pact_screen` (the client, the screen, the body and the client test), `check_economy` (Stolas's terms, offerings
  and recall costs against the data), `check_geckolib` (his model by face), `AUTHORITY_ROUTES` (Star-reckoning),
  `concordance_journey --check`, `owner_art --check` and the icon maps.
- **Audit mutations** (run in place on scratch edits and restored, per code commit): D-03's 18, D-04's 26, the client
  commit's 17 and D-05's 19 were each reported, and each commit's unchanged tree reported nothing. The unit review's
  are listed in the [contract](concordance-chunk-d.md#d-05-star-reckoning-the-obligation-upkeep-renew-suspend-and-end-unit-2).
- **Design-pass simulations** (scratch copies, before the build): the progression graph with the perch had 0 problems;
  a slab in the perch's recipe gave 3 (hence polished deepslate, D-P32); the journey's fifth step was reached; the
  perch's icon map had no errors.
- **Not exercised:** a real chunk unload (modelled as a gap), the narrator's audio, two clients, an aimed click on the
  perch (the client test opens it by the server's use), a human playthrough.

## World and event applicability

- The perch is placed in any dimension where its placer may build (claims, spawn and town protection apply), never in
  Styx's home (`StyxConservatory.covers`: the conservatory's footprint, and a town-built home's district and road,
  D-P31), and never by a stand-in. A perch a machine sets records no owner and seals and seats for nobody (D-P24, as
  corrected by the unit review). Star-reckoning's patterns are observed only in the Overworld.
- No structure loot, biome or boss dependency.
- The Harvest Moon is observed only on the Halloween event's Harvest Moon nights and the Winter Crown only in winter (or
  any season with seasons off); the earned pact and observations persist after the event and the season.

## Rollout and open questions

Known limits:
- The roster screen (D-07, unit 3) is not built: the terms say a pact can be ended from the roster, but until then it
  is ended at a perch (its own, or for a pact without its perch, any perch that keeps nothing).
- His body is provisional (the Flashjack rig and skin); he has no sounds, gestures or Jade lines of his own yet (D-06).
- The first obligation counts once a player, not once a pact (D-P44), which is stricter than the contract's wording.
- A pact broken while switched off whose old place stays unloaded cannot be seated until that place loads; it can
  still be ended.
- A perch set by a machine is inert (no owner) and its screen says only its owner may invite Stolas; it does not say
  that it has none.
- Suspending, recalling and resuming once a day keeps the upkeep period counting only a few ticks of active time a day
  (checkpoint question).

Checkpoint questions:
- Stolas's character and words (D-06);
- the provisional look and its licence label;
- the Harvest Moon balance for non-masters (the quota is the only hard limit);
- once-a-player versus once-a-pact obligation (D-P44);
- the compact's numbers and the upkeep's strictness, with the two routes around upkeep (D-P50 closes one).

Ids are stable once released: `jugcraft:pact_perch` (block, item, block entity and menu), `jugcraft:stolas` (entity),
`jugcraft:stolas_regard` (research), `jugcraft:pact_service` (practice), `#jugcraft:stolas_offerings` and
`#jugcraft:stolas_specimens`, `data/jugcraft/concordance/pact/stolas.json`, and the payload types
`jugcraft:concordance_pact_request` and `jugcraft:concordance_pact_state`. Disabling the Concordance keeps every
registration and every record.
