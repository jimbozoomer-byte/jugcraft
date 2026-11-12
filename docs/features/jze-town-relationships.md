# JZE town guests and relationships

Status: gameplay foundation implemented and validated locally on November 11. Named appearances, placement planning and the roster-eligibility discrepancy remain unfinished. Not released. OpenAI Codex coordinates local integration and Claude Code delegation. Preserve all existing dirty November work. No main merges, releases, updater or live-server actions.

## Approved player outcome

One NPC for each approved identity across the server, shared by all players. Players complete gift/fetch requests to raise individual affection to a cap. At the cap an available NPC can marry one player; each player may have only one spouse. Two competing requests must never create two spouses. Marriage is a nonsexual fictional game relationship, not a claim about a real person's private life. No ownership trading, cash purchase or monetization is requested.

Divorce frees both partners, with a seven-real-day remarriage cooldown on both the player and NPC. Store absolute server timestamps, not game day/time, and preserve the cooldown across restarts. Death, logout, travel and chunk unload do not erase relationships or allow duplicates. Unloaded NPCs remain reserved; absence is not proof of death. Operator repair tools need explicit permissions and must not silently reset relationships.

## Requested roster

- Public-personality guests: Emiru, Lauren Chen, **Brittany Venti**, AOC, Sarah Stock, Pokimane, Rooni.
- Sarah Stock: optional fictional zombie wedding-dress variant requested; ordinary nongraphic Minecraft styling.
- Chainsaw Man: Power, Makima, Reze, Asa, Himeno.
- Uma: Teio, **Agnes Tachyon**, Gold Ship, Oguri, Special Week.
- Evangelion: Rei, Asuka, Misato.
- Other anime: Frieren, Rem, Megumin, Zero Two (02), Rin/Ishtar (one requested roster slot; clarify before splitting).
- Games: Tifa, 2B, Chun Li, Ada Wong, Lara Croft, Kafka, Miyabi, Remielle, Nicole (ZZZ).
- K-pop: choices pending from Jakeey; no names supplied.
- An additional unnamed guest was requested; identity/reference still needed. Do not infer an identity from a description or encode the reported hostile characterization as dialogue.

Identity/appearance holds: Rooni is described by the owner as a slim Black woman, but that does not identify a particular person or establish her likeness. Need a profile/reference. Owner does not know who Remielle refers to. Both stay pending. Do not substitute another character. Names alone do not supply usable skins: inspect the existing owner library and record provenance; do not claim unrelated generic skins reproduce the named characters.

## Integration and balance

Reuse Townsfolk, CustomNpcs, saved world data and existing menu/network patterns. No new library, remote asset downloader, chunk loader or research gate. Keep quest-item costs modest and within existing survival sources; no automatic valuable item or currency rewards. Quest completion consumes only requested matching items, awards affection once, and saves completion/cooldown atomically. Repeat gift spam, forged UI actions and overflow must not bypass progression. Numeric affection/request cadence is an implementation choice to document for owner review.

Do not overwrite buildings or move owner objects to place guests. Establish a reviewed spawn-city placement plan with collision and loaded-position checks. NPC removal/respawn repairs must preserve the single canonical identity and relationships. Art/UI should match existing town and companion interfaces; show current quest, affection, unavailable/married state and clear confirmation for marriage/divorce. No secret attraction changes or marriage from a single accidental click.

## Work split

- Claude A: saved relationship ledger, unique NPC identity, exclusive marriage/divorce/cooldown and concurrency/save tests.
- Claude B: bounded gift/fetch progression, inventory-safe completion and abuse/regression tests, against A's agreed service API.
- Claude C: roster validation and existing-asset audit, town-safe NPC interaction/menu and client-facing states; coordinate shared files explicitly.
- Claude coordinator: agree API first, isolate worktrees, review delivered patches and preserve prior work; no broad unrelated changes.
- Codex: local TODO/spec, API/security/integration review, merge patches locally and run relevant combined server/client checks. Do not label partial foundations or placeholder skins complete.

## Verification required

One-per-identity across dimensions/unloads/restarts; duplicate-copy rejection; simultaneous proposals; one-spouse constraints in both directions; no item loss on failed requests; exact item consumption and once-only affection; full cap requirement; divorce confirmation and seven-real-day cooldown on both sides; backward clock behavior; saved schema round trips; permission/distance/identity checks; owner city placement preservation; rendered menus and skins. Report actual results and unimplemented roster/art separately.

## Local review and handoff — November 9

The existing Claude Code session received the three-worker assignment and the clarified roster. Its coordinator is surveying the base checkout and defining the shared API; this is not evidence that the workers have delivered code. The account displayed 99% of its five-hour allowance, resetting at 9:40 PM local time. Do not buy extra usage or change the selected model/effort to bypass that limit.

Local filename audit scanned 48,060 supplied PNG, BBMODEL and GEO.JSON files and found no matches for unambiguous roster-name tokens (`build/jze-asset-name-audit.json`). This does not exclude differently named art. No finished named-character skins have been verified. Repository documentation/link checking passed after adding this plan.

Integration findings sent to Claude:

- `CustomNpcs.register` forgets destroyed entities from its registry. Guest canonical identity and relationships therefore need durable independent reservations; ordinary entity removal must not erase a marriage.
- `Townsfolk.talkTo` dispatches custom NPC interaction before mentors and shops. Coordinate that hook rather than replacing unrelated interactions.
- `CommissionMenu` is a useful server-bound menu pattern, but its build-permission requirement is inappropriate for public guest conversations.

Concrete review cases before accepting delivery:

| Case | Required outcome |
| --- | --- |
| Two capped players propose in the same server tick | Exactly one succeeds; rejected request changes neither relationship nor inventory |
| Married player proposes to another guest | Rejected even if the first spouse is unloaded or in another dimension |
| Copied entity NBT loads alongside canonical guest | Copy cannot offer quests, accept marriage or replace the reserved entity |
| Guest is unloaded or removed | Ledger, affection and marriage persist; repair is explicit and permission checked |
| Request is completed twice or a stale menu is replayed | Items and affection change once only |
| Requested items span inventory stacks | Consume exact eligible amount; insufficient items consume none |
| Player leaves range, changes dimension or becomes spectator | Menu actions reject before changing inventory or ledger |
| Divorce then restart | Both cooldowns retain the same absolute deadline |
| Clock moves backward or game time is changed | No early cooldown expiry or affection farming |
| City placement or repair | No owner blocks, furniture or slot machines removed; no forced chunk loading |

No new relationship gameplay has been integrated or tested locally at this checkpoint. Existing November fixes and their test evidence remain separate.

### Concrete delegation blocker

Claude reached its five-hour usage limit during the shared scaffold, before the three workers were launched. The page explicitly shows automatic continuation enabled for **9:41 PM local time**. No additional usage was purchased and no model/effort settings were changed. Evidence: `build/jze-claude-usage-limit.png`. Do not claim background Codex supervision continues after this chat turn ends.

Observed cloud scaffold: `GuestRoster`, `GuestBalance`, `GuestResult`, `GuestLedger`/`GuestFavors`/`GuestMarriage` API stubs, `GuestMenu`, `GuestService`, and Townsfolk/custom-NPC/registration hooks in an isolated cloud worktree. No delivery commit, complete implementation or successful tests were supplied. Nothing from that scaffold was copied into the local runtime.

Queued review correction: remove unverified exact canon-age claims and the unsolicited friendship-only eligibility split; the requested feature is nonsexual fictional companionship/marriage. Preserve pending-identity holds. Require explicit confirmation for proposals as well as divorce, and test that reopening menus cannot bypass quest/cooldown protection. Verify this correction was processed when Claude resumes, then verify actual worker starts, obtain review-branch hashes and review/integrate/test deliveries locally.

### November 11 resumption

The promised automatic continuation did not occur: the session still ended at its November 9 quota message when inspected November 11, with the new worker tasks unfinished. Usage had reset to 0%. At the owner's explicit request, Codex sent a fresh continuation instruction and observed Claude begin checking the isolated worktree. Preserve the existing local `feature/nov7-priorities-local` changes. Worker delivery, integration and testing remain pending; a running coordinator is not proof that its three workers have started.

All three actual Claude workers subsequently appeared under **Running 3**: A `claude/nov10-jze-a-ledger`, B `claude/nov10-jze-b-favors`, C `claude/nov10-jze-c-roster-ui`. The shared scaffold is on `claude/nov10-jze-api` through `cb6b71618c` (base `8075c6d58b`). Codex fetched it for read-only review; it is not integrated runtime code. Review fixes invalidate menu revisions on open, validate the current guest identity/availability and clear connection-only sessions on disconnect. Native Minecraft 26.3 packet bytecode confirms button IDs use VAR_INT (`build/nov11-guest-button-packet-review.txt`); the coordinator was informed that the temporary 127 ceiling is unnecessary.

Scope discrepancy to report rather than conceal: Claude retained marriage-disabled flags for 11 roster entries it considers source-depicted minors/students (Asa, five Uma entries, Rei, Asuka, Rem, Megumin, Rin/Ishtar), despite the requested nonsexual uniform mechanics. Other progression is assigned for those entries. This is Claude's implementation limitation, not an owner-approved roster change. Character appearances remain explicitly unfinished, with existing town skins used only for development placeholders.

### November 11 delivery review (implementation in progress)

Claude A/B/C have pushed incremental isolated review branches; no guest runtime files are integrated into this dirty local checkout yet. Codex reviewed the actual sources, not only agent summaries. Concrete corrections sent and observed in subsequent sources:

- Cooldowns now retain a persisted wall-clock high-water mark, preventing backward/restore clock cycles from counting elapsed time twice.
- Daily favors retain a persisted per-player high-water day, preventing old pruned guest records from resetting limits after a backward game clock.
- Gift/quest public methods validate the living canonical NPC, matching guest ID, same dimension, interaction range and non-spectator player independently of the menu.
- Unreadable saved relationship records are retained and reject new marriages instead of silently becoming fresh records.
- Cross-worker correction pending: damaged relationship records must reject gift/quest consumption before inventory changes, rather than accepting an item for zero affection.
- Menu button IDs use the pinned platform's VAR_INT wire codec. Shared revisions were expanded to 14 bits and invalidated on reopening; this is stale-click protection, not a cryptographic token.

Worker B has delivered eight gameplay tests and was resumed for integration corrections. A and C are still finishing ledger/placement/UI tests. The coordinator is assigned to integrate all three branches, register test entry points and supply a review hash. Combined local compilation, server/client tests and visual inspection remain pending. Do not report these source reviews as passing gameplay tests.


### Integrated local candidate — November 11

Imported Claude review commit `2e2304675bd0e7051c2c1953ae2b756cce61040e` onto the existing dirty checkout without committing it. All three actual Claude Code workers (Opus 5.5, existing selected effort) delivered; OpenAI Codex reviewed and integrated the patches. Prior shared Java changes, translation keys and test registrations were preserved; backup manifest is `build/jze-before-integration/manifest.json`. The review branches are frozen during testing.

Implemented foundation: durable unique guest identities, saved per-player affection, exact plain-item gifts and fetch quests, per-player/guest daily limits, exclusive two-step proposals, confirmed divorce with seven-real-day cooldowns on both parties, operator placement/state/repair commands, conversation screen and an accepted-gift/favourite help panel. Initial balance: 1,000 affection cap, 3 gifts and at most 75 affection per guest per game day, 30-affection fetch quest, one diamond consumed on successful confirmed marriage. No money or valuable-item rewards.

All identified review corrections above are present, including damage gates before consumption and the unidentified damaged-player fallback. Test clock rewinds restore other players' pending requests rather than clearing unrelated parallel tests. Compilation of the common gameplay code succeeded; resource processing and remaining compilation/game tests are still running in `build/nov11-guest-combined-server.log`. This is not yet a passing combined result.

Operator entry points: `/guests roster`, `/guests place <id> [pos] [yaw]`, `/guests state guest <id>`, `/guests state player <name-or-UUID>`. The `repair` subcommands support explicit rebind, affection corrections, divorce and clearing cooldowns. No guests are automatically inserted into the existing city and no owner blocks are rewritten.


### Final local validation — November 11

- Full compilation and server suite: **3,744/3,745 passed** in `build/nov11-guest-combined-server.log`. The sole failure was a new damaged-record test expecting zero rather than preserved pre-existing affection. The gameplay correctly refused the mutation. Codex corrected the assertion to require both the returned value and saved affection to remain unchanged. No runtime change was needed.
- Final combined guest server regression: **26/26 passed**, `build/nov11-guest-server-regression.log` (1m39s including startup). Covers identity duplicates/removal/rebind, exclusive marriage, replay/expiry, exact inventory handling, caps, different-dimension refusals, saved data, damaged records, real-time cooldowns and placement protection. The full suite was not rerun after the test-only correction.
- Real automated client: `GuestClientGameTests` passed, `build/nov11-guest-client-final.log` (2m12s). Codex extended the delivered test to click proposal/confirmation/divorce/confirmation over actual menu packets; it verifies no premature diamond consumption, successful exclusive marriage and both post-divorce cooldowns. It also opens gift help, accepts an errand, checks unavailable/companionship states, and checks the smallest 320x240 GUI. Eleven screenshots are preserved under `build/jze-validation-nov11/`; offered/help/small-layout/confirmation/cooldown views were visually inspected.
- Repository/documentation check passed. All 33,650 prior translation values and all prior test entry points were preserved; 133 guest translations added.

Limits: these are automated local tests, not a two-human multiplayer or live-city playtest. Final named-character skins/models are **not implemented**: all 32 placeable entries use explicitly labelled development stand-ins. Rooni, Remielle, unnamed guest and K-pop selection remain pending identity/reference information. Sarah Stock's optional zombie wedding appearance remains pending. Actual city placement/design and owner visual/balance approval remain pending. The 11 marriage-disabled entries described above are still an unapproved scope discrepancy; do not silently call the requested whole roster complete.

No local commit, main push/merge, PR, release, updater promotion, personal Modrinth change, live-world placement or server restart was performed. Claude isolated review branches were the authorized handoff mechanism; all three workers finished, and the review branch is frozen at `2e2304675b`.


### Character appearances and safe placement — November 11 follow-up

Owner requested completing the remaining appearances. Codex implemented 32 individual player-UV skins using the existing editable town skin generator, plus head-attached ears/horns/hat geometry for the relevant characters. [Per-guest art record](../audits/jze-guest-appearances.md). Sarah uses the requested nongraphic zombie bridal costume; Rin/Ishtar uses Rin's outfit within the single saved identity. These are Minecraft-style interpretations and still need owner likeness acceptance.

Saved guests using an old development stand-in migrate to their named skin on the normal service tick. Other operator-selected skins are preserved. Added `/guests city preview` and `/guests city place`: creative operators may populate only clear, loaded town lawns, with a three-by-three empty grass margin, separation from residents/entities and all existing claim/owner-object checks. Never writes terrain, never loads chunks, never duplicates or relocates a canonical identity. No live-world placements were made.

Owner rule, 11 November 2026: proposal, marriage and the church ceremony are only for unambiguous adults. Fourteen guests are companionship-only (the eleven their source works depict as minors or school students, plus Power, Reze and Zero Two, whose ages are not stated); every guest uses the same nonsexual companionship mechanics. The four unidentified entries remain held. No changes to exclusivity, diamond cost, affection caps or seven-day divorce cooldown.

Validation is running; results will be recorded below. No commits, pushes, releases, updater promotion or server operations.


Appearance validation results:

- Packaged art check (`python -m unittest scripts.tests.test_guest_skin_assets -v`) passed: all 32 skins match the generator, have opaque player base faces, preserve the transparent face layer and have unique pixel data.
- First guest server pass: **27/27**, including the new city-lawn block preservation check. Final placement/migration pass before the pending-player refinement: **27/27**, `build/nov11-guest-appearance-server-final.log` (2m3s).
- Real client `GuestClientGameTests` passed twice. The initial render exposed sparse outer hair marks that looked detached; those layers were filled and the client run repeated. Final run: `build/nov11-guest-appearance-client-final.log`. The test checks resource availability and captures all 32 skins on an actual NPC, plus gift/quest/proposal/divorce/small-layout/unavailable menu checks. Screenshots preserved in `build/jze-appearance-validation-nov11/screenshots/`. Megumin, Tokai Teio and Power final renders were visually inspected; they show the repaired solid hair layers and attached accessories. This is automated local rendering, not owner visual acceptance or a two-human playtest.
- Repository/documentation check passed after the initial check collided with resource copying; log `build/nov11-guest-appearance-repository-final.log`. All 33,650 pre-existing language values are preserved.
- Final exact-placeholder/pending-player-skin preservation regression: **27/27 passed**, `build/nov11-guest-appearance-migration-final.log`. It preserves a different operator-selected town skin, a named skin and an unresolved player-skin request. No network lookup is needed by the test fixture.

Four identity/reference holds remain (Rooni, Remielle, unnamed guest, Jakeey's picks). The current town blueprint has 58 candidate three-by-three lawn sites at the planned spacing before loaded-world/entity checks. Existing owner builds may reduce that; the placement command reports leftovers rather than clearing or moving anything. No live-city placement, deployment, profile change or release was performed.


Final integrated client rerun also passed (`build/nov11-guest-appearance-client-verified.log`), now after the exact-placeholder/pending-player-skin preservation refinement. Its 43 screenshots, including all 32 character skins, were copied after Gradle exited to `build/jze-appearance-validation-nov11/screenshots/` so later test-run cleanup will not remove them. Final repository check: `build/nov11-guest-appearance-docs-verified.log`, passed. All current requested identified appearances and placement tooling are implemented locally; only the four unidentified roster holds and owner visual acceptance remain. No claim of a live-city or two-human multiplayer playtest.
