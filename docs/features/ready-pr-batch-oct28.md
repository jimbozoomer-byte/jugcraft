# Ready PR integration — October 28, 2026

Owner request: merge ready open PRs and prepare matching client/server updates. Integration by OpenAI Codex (GPT-6).

## Scope

Companion PR #528 merged separately to main as `ab5dfa6e25`. The following non-draft contributions had successful individual checks when selected; original authors and feature records remain intact.

- [#497](https://github.com/jimbozoomer-byte/jugcraft/pull/497): Guns: armour piercing for slices 15A's and 15B's guns (Vesper, Onrush, Kreuzer, Psalm) (`6d226901d094f9dc2d0687ab261b6c9bc481a01e`).
- [#499](https://github.com/jimbozoomer-byte/jugcraft/pull/499): Guns: darts and bleeding (the Dart, the Quill's falling darts, Bleeding) (`b313bd0782a933526492f2b18d0b06e797e46fad`).
- [#505](https://github.com/jimbozoomer-byte/jugcraft/pull/505): Guns slice 15D: two barrels and a burst (Auger Rifle, Auger Shotgun, Tumbler Rifle) (`bed8e3a37614f06ef67c6eb9823ba99307159b79`).
- [#510](https://github.com/jimbozoomer-byte/jugcraft/pull/510): Guns slice 15E: a microjet gun and two beams (Maelstrom Gun, Ermine Rifle, Geode Cutter) (`67879d522d072f19c6edba74daa0e4d3d41dee96`).
- [#511](https://github.com/jimbozoomer-byte/jugcraft/pull/511): Headless Horseman sightings in the Halloween season (`3d3d576c863ad137f4d938362906026ac0173ad2`).
- [#514](https://github.com/jimbozoomer-byte/jugcraft/pull/514): The Blood Moon: every fourth night of the Halloween season (`975083fb1ad5c2c7679920d1706fffc975a39357`).
- [#516](https://github.com/jimbozoomer-byte/jugcraft/pull/516): Guns: beams that mine (the Seam Cutter and the Geode Cutter cut blocks) (`6edd06585268377e41a01cedb5d292bf44455c36`).
- [#519](https://github.com/jimbozoomer-byte/jugcraft/pull/519): Graveyard Rising: wake a graveyard by its bell on any night of the Halloween season (`6fbc0f66c1c88ac44bd578b60a0c483645afd690`).
- [#522](https://github.com/jimbozoomer-byte/jugcraft/pull/522): The Church Bridge: the Headless Horseman's off-season lair and the Hessian's Bridle (part 1) (`ad0a95f3774c82a3ad778f75785f6fecbc5b8405`).
- [#523](https://github.com/jimbozoomer-byte/jugcraft/pull/523): Guns: hit effects (slowness, darkness, and bleeding on arcs) (`0b081d62de1f899ff18eca37e93ecee331689f61`).
- [#527](https://github.com/jimbozoomer-byte/jugcraft/pull/527): The Headless Horseman in his lair: his fight, phantom riders and loot (part 2) (`70b1932296d0a7e66b52a49139f75c5a027aad4a`).
- [#529](https://github.com/jimbozoomer-byte/jugcraft/pull/529): Guns: End-gun levitation at the owner's chances (`623f3514efe501ea9459310efea359605bf00e8b`).
- [#530](https://github.com/jimbozoomer-byte/jugcraft/pull/530): Guns: armour piercing and knockback for the older guns (`c36e79126ab4866a914adf47b85e9d92d6f00047`).

Stacked PRs are integrated together so downstream changes cannot ship without their prerequisites. Additive conflicts in changelog, translations, registrations and configuration retain both features. No draft, failed-check or still-running contribution is treated as ready merely because its files can merge.

## Integration correction

HorsemanSightings.enabled now checks HalloweenSeason.active. Previously the scheduler checked the feature switch and night but could schedule sightings outside the documented seasonal window. A native game test verifies the off/on override and restores the original global mode.

## Validation

Combined validation is in progress. No hosted playtest is claimed. Resource regeneration completed; the translation diff only restores generator ordering (zero changed keys/values). Repository checks passed. The data/recipe/model audit passed for 8,038 material IDs and 24,516 block/item models. All 26 locked libraries and 126 mixin classes verified. The configured Python run covered 287 tests; five cases read intermediate regeneration output, and all five passed in the subsequent 15-test focused rerun against completed resources.

The full native server run completed 3,089 tests: 3,088 passed; the bell-confirmation test failed because nearby test graves could prematurely arm its setup. Its prerequisite probe now runs by day, retaining the shortage/cooldown assertions without arming the event. An additional test explicitly prepopulates enough graves. The corrected Graveyard Rising suite passed all four tests, including the new prepopulated-graves regression; the follow-up build succeeded. The other 3,088 cases passed in the full run, including the seasonal on/off regression. No assertion or runtime restriction was removed. The updater's 79 offline assertions and Herobrine's 393 scheduling assertions also passed. Automated rendered tests are separate from these results; their final evidence and the exact GitHub check state are recorded on [integration PR #537](https://github.com/jimbozoomer-byte/jugcraft/pull/537). The first local build was stopped before tests to incorporate the newly passing Blood Moon and Graveyard Rising branches and the seasonal correction. The checkout initially omitted the owner art sources; repository/data checks from that incomplete sparse checkout are not acceptance evidence.

## Release boundary

The live server and public client channel remain on 0.1.0-alpha.20261027.1. Prepare and validate the next numbered artifacts before asking for restart confirmation. After confirmation, announce the maintenance in-game and wait the full 30 seconds. Preserve the world, player data and configurations; update only managed release files. Promote the client channel only after the matching server starts successfully.

## Deferred

Drafts remain proposals, including the Glass Abbot, Storm Roc, Cinder Tyrant, garden cohort and utility/economy documentation changes. The Cursed Mirror has conflicts and no current checks. Later gun and Pumpkin Patch Market branches, and costume disguises, were still awaiting validation at selection time. Their readiness should be reassessed in the next batch, not assumed.

## Release validation follow-up — October 28

Exact-main Build 40632227610 exposed a genuine queued-grove race: a natural leaf can decay after planning, and the strict expected-block check then aborted the remaining work. A deterministic regression reproduces the failure by removing a planned leaf before application. Grove removal now skips already-air leaves after claim/town/operator-approval checks; other replacement blocks still abort and are preserved. Skipped air creates no undo entry. A second regression verifies that a player replacement stops the grove and survives undo. The repaired full local build passed all 3,092 required server tests, and corrected-main CI's mod job passed. Owner restart approval is recorded, conditional on successful release checks and a full 30-second player warning. Repair by OpenAI Codex (GPT-6).

Corrected-main Build 40637757218 then found a separate real multiplayer race in client shard 14: Modonomicon's `sync_book_visual_states` packet retained live nested maps. Awarding research while Netty encoded the packet caused a fastutil iterator NullPointerException and disconnected the client; the roster screen timeout was a consequence. The new constructor mixin deep-copies books, categories and entries on the sender thread and copies bookmark lists via the pinned library constructor. This preserves all navigation/unread fields and leaves the live state mutable. A native packet round-trip regression mutates every source layer after queuing and verifies the complete send-time snapshot survives encoding. No test assertion or gameplay action is suppressed. Validation of this second repair is in progress. The optional-absence CI job also failed before compilation on a Fabric Maven download timeout. The release remains unpublished and the live server unchanged until the full corrected checks pass.
