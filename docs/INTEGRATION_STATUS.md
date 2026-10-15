# Jugcraft implementation and remaining work

## October 13 integration

The [October 13 integration record](features/oct13-community-integration.md) tracks the 31 source PRs,
their exact heads, integration corrections and verification. It includes community chemistry, Peepo automation,
guns, bosses/lairs, Concordance/encyclopedia, mineral deposits, power lines/wall runs and nuclear power.
Check the integration PR's merge state before treating these as shipped on main. The owner's separate local machine
consolidation and recipe-picker playtest remains outside this batch.


Updated 11 October 2026 for [integration PR #320](https://github.com/jimbozoomer-byte/jugcraft/pull/320) (source PRs #299–308 and #310–319), following [integration PR #277](https://github.com/jimbozoomer-byte/jugcraft/pull/277) and [follow-up PR #298](https://github.com/jimbozoomer-byte/jugcraft/pull/298). This is the owner-facing index: feature records contain the detailed behavior and historical test evidence. The PRs' current GitHub states determine which combined work has reached `main`.

## Not merged: Concordance Chunk C (living workshop)

Stacked on Chunk B, on branch `claude/chunk-c-living-workshop`; not merged, no pull request. Unit 1 (C-01, C-02),
unit 2 (C-03, C-04) and unit 4 (C-07 to C-09) are built and recorded. Their local checks pass (`check_mod_data`,
`check_repository`, `owner_art --check`, `concordance_journey --check`, `scripts/tests`; for units 2 and 4 also
`check_icon_maps` and the World Designer's node tests). None of their game code has been compiled against Minecraft,
and none of their game tests has run: no CI run yet. Unit 3 is being built beside unit 4, from the same base, to be
merged with it; unit 5 has not started. Not played by hand or with two clients
([the chunk's contract](features/concordance-chunk-c.md)).

| Area | Implemented change | Record |
| --- | --- | --- |
| The crucible says what it is doing (unit 1, C-01) | An Alembic Crucible following a formula says why it waits: the empty-hand status line, Jade (with its tank) and the encyclopedia's new Inspect page for a crucible within reach (more with an Assay Glass carried). A formula the rules can no longer make stays set, names its step and why, and carries on when they change back; an unreadable saved one is kept. Only someone who could break it by hand may work it or set or clear its formula. Peepo's crucible status reads the same reason, and is FULL only when the output is full or holds another brew | [alchemy](features/arcane-concordance-alchemy.md), [authority](features/arcane-concordance-authority.md), [Peepo port](features/companion-farm-stand-crucible.md), [first route](features/arcane-concordance-first-route.md) |
| Two workshop products (unit 1, C-02) | The Stillroot Poultice (Mendvetch stirred warm: a Regeneration II salve) and the Kiln Draught (magma cream or sugar stirred hot: Fire Resistance), named on the stack, as Winter Brine now is; contaminant fouls Verdance, so a dirty batch heals less, and the glass and the outcome say so. A poultice put on a friend in a lair fight earns its helper healing credit for each heal of its Regeneration; the Kiln Draught carries its drinker over the Cinder Kiln's slag, and the kiln's dossier names it | [alchemy](features/arcane-concordance-alchemy.md), [encounter credit](features/lair-encounter-credit.md), [Cinder Kiln](features/cinder-kiln.md), [the contract](features/concordance-chunk-c.md) |
| A circle says what a rite still needs (unit 2, C-03) | Looking at a Circle Anchor, the encyclopedia's Inspect page lists everything the rite the offerings answer still needs before it can begin, all at once (offerings, each pylon's charge for a step and for the whole rite, pylons that will not lend, place, Focus, light, the circle's faults), then the rite's stages and what each pays, its backlash included; a pylon short only of the whole run is a warning. While a rite runs, the page and Jade show what it has spent apart from what it holds; Jade's gathering line shows the participants needed and lists the offerings. Readings in chat and refused starts mark every faulted part where it stands; an interruption is told in one line (why, where the offerings are, what was lost). A rite the reader has not learned is never named. Tests written, not run | [rituals](features/arcane-concordance-rituals.md), [encyclopedia](features/jugcraft-encyclopedia.md), [the contract](features/concordance-chunk-c.md) |
| Root and Rivet (unit 2, C-04) | A rite of the Lesser Circle, taught by Circle Lore understood: a Thallite plate, 4 copper ingots and 4 Mendvetch become a Rooted Thallite Foundation (6 Focus, 32 Ley Charge, 8 s), the base unit 3's Crucible Stand is to be built on; nothing turns it back, and the Assayer's Scale never weighs it. A codex entry with its stage and payment table, linked from Stratawrighting's; JEI's ritual and information pages; the economy models it as a sink. The plate needs the `machines` and `thallite` switches. Its icon is drawn fresh and awaits the owner's review. Tests written, not run | [rituals](features/arcane-concordance-rituals.md), [Thallite](features/thallite.md), [economy](features/arcane-concordance-economy.md), [the contract](features/concordance-chunk-c.md) |
| The garden shows its workings (unit 4, C-07) | Looking at a Verdant Bed or a crop on one, the encyclopedia's Inspect page shows the bed's moisture and nutrients, the crop's step and pace, the verdict growth would reach, and each factor against the plant's range with how far its reading reaches, the light as sky and lamps; Jade on a ripe crop shows its last verdict, "As it grew", and a bed forgets a gone plant's verdict. An item held on a bed is its own use (a seed plants without sneaking), and a Gourd Canteen's sip wets a dry bed where you may build. JEI shows what a Mulch Maw makes of each thing it eats. Tests written, not run | [ecology](features/arcane-concordance-ecology.md), [encyclopedia](features/jugcraft-encyclopedia.md), [JEI](features/jei-integration.md), [the contract](features/concordance-chunk-c.md) |
| A mixed bed and harvest strain (unit 4, C-08) | Dewmoss and Gloamcap grow 25% faster with the other kind in the 5 by 5 round them (speed only: no nutrient is made). Every harvest strains its bed by one, up to 3, counted only against the plant's disturbance limit; rest while nothing grows, compost (bone meal, fertilizer, a Mulch Maw's nutrient) and a Greenwarden's tending ease it; it never kills and never reads industrial work. Bone meal and fertilizer now work on a growing crop by a plain use. Tests written, not run | [ecology](features/arcane-concordance-ecology.md), [the contract](features/concordance-chunk-c.md) |
| Dewlit, one graft (unit 4, C-09) | A Greenwarden's plain use of a Dewmoss on a Sunpetal crop spends the Dewmoss and grafts Dewlit onto its bed: a Sunpetal there bears shade (light from 9) but wants a wetter bed (moisture from 4), through every harvest, replanting and restart until another plant grows there; its produce, cost and alchemy are unchanged. The reports, the Inspect page and Jade name it. Tests written, not run | [ecology](features/arcane-concordance-ecology.md), [the contract](features/concordance-chunk-c.md) |

## Not merged: Concordance Chunk B (winter and stone)

On branch `claude/chunk-b-winter-stone`, not merged. Units 1 to 5 pass their tests in CI: run 38591108210 on
`25644502a` passed every job on its second attempt (all 1682 required server game tests, with and without the optional
integrations, and every client test class); its first attempt failed two tests outside Chunk B (a nuclear plant's
radiation reading, a Peepo delivery client test), which passed when re-run. Units 1 to 4 also passed run 38549876709
on `b012875ab`. The evidence is recorded and the owner's checkpoint is awaited; Chunk C, stacked on it, is under way
(above). Not played by hand or with two clients ([the chunk's contract](features/concordance-chunk-b.md),
[its evidence](features/concordance-chunk-b.md#unit-5-evidence-and-handoff-b-09-b-11)).

| Area | Implemented change | Record |
| --- | --- | --- |
| Boss rewards and guidance | The Yeti King and the Cinder Tyrant on the lairs' shared encounter credit: support (healing, wards and, against the Yeti, baiting his charge into a column) earns a share, kept across death and disconnect and paid once; the Glacier Hall's and the Cinder Kiln's dossiers in the encyclopedia, and JEI pages for both keepers' loot (unit 1) | [encounter credit](features/lair-encounter-credit.md), [Yeti](features/yeti-king.md), [Tyrant](features/cinder-tyrant.md) |
| The Glacier Hall and the Yeti fight | Tests for the hall's access in ordinary and designed worlds, refusals, exit and restart, and for his fight against the chunk's completion evidence; three fixes to the fight: End-gun Levitation lifts neither him nor his whelps, a charge into a column's foot stuns him, and his ice and whelps last only while he counts them his (unit 2) | [Glacier Hall](features/glacier-hall.md), [Yeti](features/yeti-king.md) |
| Winter magic | Rimekeeping and Stratawrighting; Rimebind, Brace and Preserve (unit 3). Winter Brine and its Frostward at the Alembic Crucible, the timed answer to the Yeti King's frost beside his Mitten; the Rimekeeper's travel kit from the owner's water sets (Nereid's, Aquamancer's, Winterguard's), water foci and Water Bangle; a Frost boon slow as Rimekeeping practice, with no Thallite or trophy number changed (unit 4) | [Rime and Strata](features/arcane-concordance-rime-strata.md), [winter workshop and travel kit](features/arcane-concordance-winter.md) |
| The Yeti counters and the combined fixes | Optional, neither needed to win: only a Brace now lessens the Yeti King's throws, halving the next; a Brace put on another player that takes one is its caster's share; Rimebind does not slow his steps but holds his wind-up (the one under way unless it is about to strike, else his next) 4 ticks (untuned), once each thaw; drawing his charge into an ice column is Rimekeeping's elective cold work, one kind with the Frost boon's slow. The encyclopedia guides' home shows each route card whole and scrolls in a small window; the client tests picture the charge into a column, the spell bar at GUI scale 1, the foci and bangle slotted (the shot shows neither; the server's checks are the proof) and both new dossiers (unit 5; passed in CI, run 38591108210) | [Yeti](features/yeti-king.md), [encounter credit](features/lair-encounter-credit.md), [encyclopedia](features/jugcraft-encyclopedia.md), [the contract](features/concordance-chunk-b.md) |

## October 11 additions

The 20 source PRs below are combined on `integrate/oct11-ready-content` for validation against current main. The integration PR's merge state determines whether this batch has reached main. Individual feature records retain their earlier branch test evidence; the combined PR records the final tested commit and results.

| Area | Implemented change | Source PRs / guide |
| --- | --- | --- |
| Guns | Eighteen more guns: launchers, coil/plasma, double-barrels, sculk and six Nether guns; 54 guns in total | #299, #301, #305–306, #314, #318; [guns](features/guns.md) |
| Gun systems | Six-cell charging rack, Raider Gunners in camps/raids, and dual wielding of compatible one-handed guns | #307–308, #311; [guns](features/guns.md) |
| Bosses and lairs | Glacier Hall/Frost Horn and the Yeti King; Cinder Kiln/Kiln Seal and the Cinder Tyrant; adds, encounter mechanics, loot and return routes | #300, #304, #313, #317; [Yeti](features/yeti-king.md), [Tyrant](features/cinder-tyrant.md) |
| Industrial machines | Shared multi-block forms with ports, saved batches and status screens; the Electrolytic Separator; the hydrogen Gas Burning Generator; the bioethanol and biogas Bio Generator; the Chemical Infuser (hydrogen chloride); the Chemical Oxidizer (sulfur dioxide); on the contributor's fork: the Industrial Fermenter (bioethanol and collected CO2), the Bulk Chemical Reactor (hydrochloric acid and chloride leaching), the Anaerobic Digester (biogas), coal gasification (Gasifier, cleanup rack, CO methanation), contact sulfuric acid (vanadium bed, SO3, absorption) and the PVC route with HCl recovery | #312, #316, #319; [foundation](features/industrial-machine-foundation.md), [separator](features/industrial-electrolytic-separator.md), [generator](features/industrial-gas-burning-generator.md), [bio generator](features/industrial-bio-generator.md), [biogas power](features/industrial-biogas-power.md), [infuser](features/industrial-chemical-infuser.md), [oxidizer](features/industrial-chemical-oxidizer.md), [fermenter](features/industrial-fermenter.md), [bulk reactor](features/industrial-bulk-chemical-reactor.md), [digester](features/industrial-anaerobic-digester.md), [gasification](features/industrial-coal-gasification.md), [contact acid](features/industrial-contact-sulfuric-acid.md), [PVC route](features/industrial-pvc-hcl-recovery.md) |
| Trinket presentation | Belt/boots hide under covering armor; each player's Show my worn trinkets setting is synchronized to viewers | #315; [Wayfaring](features/arcane-concordance-trinkets.md) |
| Fixes and integration | Thrown arms hit targets they start a tick inside; all shared registrations/audits retained; the five-client-job scheduler carries the expanded suite | #303 plus integration resolution; [Arms VIII](features/arms-viii.md) |
| Planning | Factory construction/operation, 36 production cards and eight example layouts; shared boss delivery playbook | #302, #310; [factory plan](features/industrial-factory-implementation-plan.md), [boss playbook](branches/BOSS_PLAYBOOK.md); proposals beyond the machines above remain unimplemented |

The generator's original repository-check failure was the four-job scheduling estimate reaching its 25-minute budget. The boss stack's five-job expansion resolves it without dropping classes or relaxing that check. The combined local repository/pack tests verify complete, duplicate-free class coverage.

Current limits: the generator burns hydrogen only (methane awaits its producer); the wider industrial plan is still being delivered. The Yeti and Tyrant are playable encounter implementations, while the October 10 magic planning pack contains further proposed school/quest interactions. Human two-client play, survival balance and owner art acceptance remain separate work.

## October 10 integration retained

| Area | Implemented content | Source PRs / guide |
| --- | --- | --- |
| Guns and attachments | Scopes; six sidearms/service guns; heavy, energy, marksman, automatic and pump weapons; Tactical Grip and Laser Sight; first-person aiming fixes; five grenade kinds for the Trench Lobber | #265, #268–269, #278, #280, #285–287, #289–291, #293; [guns](features/guns.md) |
| Farming and cooking | Ten pies/tarts, seven milkshakes, crop presentation updates, wild roots, mushroom colonies, rotten tomatoes, more vegetables, herbs, spices and cinnamon | #266–267, #270, #279, #281; [Agriculture](branches/AGRICULTURE.md) |
| Peepo and Jughead | Taming, ownership and care; Planner assignments; gardens, livestock and kitchen work; workshop assistance, physical deliveries, shared supplies, porter routes and transport crates | #282; [player setup guide](PEEPO_COMPANION_GUIDE.md) |
| Bosses and lairs | Shared lair entry/return framework, Hollow Acre and Vesperine, Spindle Loft and Madame Tatterlace, their encounter mechanics and special loot | #284, #288, #292, #294; [feature records](features/) |
| Armor and weapons | Thirteen owner armor sets in Blockbench projects with runtime integration, improved Sentinel/Frost Knight/Reaper/Paladin/Templar presentation, four matching weapons and Sentinel Shield | #273; acquisition and visual review limits below |
| Fire magic | Hearthbinding research; Hearthspark, Hearthguard, Cinderbolt and Hearthflare; Smoulder; fire foci, Fire Bangle, Pyromaniac and Pyromancer armor using the supplied assets | #295; [fire spells](features/arcane-concordance-ember.md), [fire equipment](features/arcane-concordance-ember-regalia.md) |
| Wearable magic | Leather Belt, Angelic Feather, Kraken Shell, Infernal Claws, Angelheart Vial, Phoenix Down, Amphibian Boot and Ice Breaker; belt and boot rendered on the wearer | #295; [Wayfaring](features/arcane-concordance-trinkets.md) |
| Integration and tests | Projectile aiming and village-test setup fixes; four client-test shards with 60-minute limits; companion test discovery; shared-code selection fallback; isolated, repeated javelin validation; preserved wild-crop/spice generation, shared enchantment tags, weapon/armor registrations, complete boss-test flooring and ticking area, fresh local test worlds, Windows Blockbench checks, giant-pumpkin identity persistence, valid single-cell crafting output and costume/attachment resource-load corrections | #271–272, #283 and #277 integration commits |
| Industrial planning | Owner-selected metals, polymer/electronics, grid-storage, fuel and cryogenics directions; 79 machine/module art briefs and 23 larger-variant specifications | #264, planning updates in #295 and #297; [machine art specifications](features/industrial-machine-models-and-textures.md); **documentation, not implemented models or new factory gameplay** |

This combines 28 source PRs (#264–273 and #278–295) through #277, plus the later documentation PR #297 through #298: **29 source PRs in total**, alongside integration fixes. #277 and #298 both merged on 10 October 2026, so all 29 are in `main`. (A source PR can show as merged into an integration branch before its parent PR reaches `main`; that alone does not make it available in a main build.)

## Existing foundation retained

| Area | What is already present | Remaining boundary |
| --- | --- | --- |
| Technology | Shared materials, machines, power, kinetic machinery, pipes/conveyors, logistics, chemistry/refining, tools and drones | Shared forms, the separator and hydrogen generator are now in the October 11 batch; the remaining production chains still require implementation |
| Magic | Concordance research, journal/codex, invocations, rituals, alchemy, cultivation, magical helpers/logistics, relics, progression and shared projects | A framework and some traditions being built does not complete every requested school or every owner-library asset |
| World and settlement | Surface/cave content, towns, Styx and his botanical/observatory content, plus an offline World Designer for new worlds | Existing chunks are not automatically rebuilt; inspect actual structures and layouts in a new test world |
| Halloween | A substantial permanent collection of farming, decorations, activities, creatures, equipment and encounters | Public-server event tuning and playtesting still needed; earned builds/items must survive the event ending |
| Rocketry | Rocket workshop, propellant supply, survey/weather rockets, flares and terrestrial rocket applications | Full planet/moon travel and galactic exploration are future work; terrestrial development was prioritized |
| Installation | Minecraft 26.3, Fabric Loader 0.19.5, pinned libraries and the Jugcraft Complete Modrinth import pack | Use an artifact from a successful combined build; no public Modrinth listing or server deployment is implied |

See [the source/API map](WHAT_EXISTS.md), [technology guide](TECH_TREE.md), [Concordance contract](ARCANE_CONCORDANCE.md), [rocketry](features/rocketry.md) and [distribution](DISTRIBUTION.md).

## Next work, in a useful order

| Priority | Task | Completion evidence |
| --- | --- | --- |
| 1 — Playtest this combination | Two independent clients on a dedicated staging server: join/rejoin, saves/restart, claims/PvP, trading, companion inventories/deliveries, new boss entry/death/return and worn equipment | A written run record with exact build, failures and fixes; automated client screenshots are not a substitute for this session |
| 1 — Review the new art in game | Check the 13 armor sets and their held weapons, fire sets, worn trinkets, crops, guns and boss presentation | Owner visual acceptance; no claim that Blockbench screenshots alone prove runtime appearance |
| 2 — Survival and balance | Give creative-only armor/weapon additions reviewed acquisition routes; test a fresh world's route to electricity and magic; balance guns, bosses, farming and automation together | Reachable recipes/loot, no circular gates or duplication; the 2–4 hour first-electricity target measured rather than assumed |
| 2 — Factory expansion | Implement the selected industrial plan in focused increments: independent starter reagents, gas/acid processing, metals, polymers/electronics, storage and rocket fuels | Exact recipes and resource/energy accounting, usable consumers, bounded processing and save tests |
| 2 — Blueprint completion | Add any-material/tag slots, comparator output and the exported design kit/in-JAR AI guide | [Remaining blueprint scope](features/blueprint-system.md#not-yet-done-from-23); imports, holograms and drone construction already exist |
| 2 — Encyclopedia | Build the requested inventory/keybind UI for both technology and magic, pathways and integrated quests. Its first slice (both ways in, one screen, the First Light route) is implemented in Concordance Chunk A and passes its server and client tests in CI (run 38435014953); not yet merged or played by a person ([record](features/arcane-concordance-first-route.md)) | The [Encyclopedia checklist](TODO.md#jugcraft-encyclopedia), including server-authoritative quest progress and rewards |
| 3 — More magic and supplied assets | Continue fire equipment/familiar work, remaining school content and further imports of appropriate owner assets | Each import has actual gameplay, provenance and tests; files in the library alone are not implemented content |
| 3 — Stolas and Ars Goetia | Start the shared summoning/pact system with Stolas and then cover the requested 72 spirits | Connected botanical/astronomical/mineral pilot with Styx; shared definitions, costs, persistence and protections |
| 3 — Space and December | Planet/moon exploration and Christmas-style seasonal additions | Focused feature proposals/implementations on the existing systems, while preserving earlier progression and earned content |
| Before a public server | Representative factory/companion load test, backup/restore, permissions and an identified release candidate | Staging evidence and installation instructions for that exact build |

Detailed scope: [owner TODO](TODO.md), [roadmap and Ars Goetia](ROADMAP.md), [multiplayer tests](TESTING.md#dedicated-server-and-two-clients).

## Owner decisions and approved next routes

The newest #264 update records all eight answers in [industrial batch 13](features/industrial-chemistry-and-fuels-plan.md#resource-reagent-and-residue-decisions-thirteenth-batch): galena; spodumene first and lithium brines later; separate primary cobalt ore; chromite concentrate; vanadium-bearing iron feed; a short powered peroxide game recipe; novolac/DNQ photoresist; and paid gypsum-residue preparation for construction. These are selected game directions, not implemented recipes. The [connected production map](features/industrial-chemical-catalog-and-routes.md) groups the proposed implementation work.

The Encyclopedia still needs quest/reward policy and visual direction. The new equipment needs owner visual review and survival acquisition choices where its feature record says creative-only. Those decisions do not block the completed content above from being tested and integrated.

## Save compatibility correction

Giant pumpkins now save their contest identity as `pumpkin_id`, separate from Minecraft's reserved block-entity `id`. Old custom UUID tags still load. If an older full world save already replaced that UUID with the block type, the lost UUID cannot be reconstructed; the pumpkin receives an identity while its growth, weight, carving and light data remain readable. Keep normal world backups before testing alpha updates.

## Verification and scope

The October 10 full combined validation is recorded in #277. The October 11 integration PR records this batch's combined validation; subsequent correction checks are available in the [Build history](https://github.com/jimbozoomer-byte/jugcraft/actions/workflows/build.yml). Individual branch successes are historical evidence, not a substitute for the combined build. Check the exact commit's results for compilation, data regeneration, both server configurations and the selected client tests. Human multiplayer/load testing remains outstanding unless a separate run record is supplied.

Integration work includes actual conflict resolution, generator corrections and test reliability fixes; it does not change platform pins, remove review protections, execute owner-library programs or deploy to the live server. Integration and this status index were prepared with OpenAI Codex; feature authors and their original attribution are retained.
