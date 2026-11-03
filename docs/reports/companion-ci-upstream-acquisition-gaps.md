# Remaining upstream Survival acquisition gaps

## November 3 main reconciliation

Merged main through `a279a133ba` (PRs #605–#608) into the companion branch with OpenAI Codex (GPT-6). Preserve the companion features, 24 client groups, component-safe acquisition recipes, source-bounded 60-model finishing pass and prepared-vanilla-archive lookup. Integrate upstream dry-block water-placement safety, harvest/pet/machine updates, Pumpkling wild-death versus owned-knockout tests, Chima pre-save health setup, and completed mock-transport cleanup.

Resolve the shared Lua fixture in favor of upstream's bounded startup-aware polling. Keep the stronger native-recipe and inventory-conservation assertions in the Peepo crafting-table suite. Retain both common and client-only test mixin configurations. Remove duplicate `TestPlayersGameTests` and `PeerClientStartupGameTests` entrypoints introduced by the overlapping additions; the first focused startup caught the duplicate registry key before tests ran. Adapt upstream's archive regression to the retained explicit archive/root injection points, keeping its fresh-cache behavior and missing-model failure assertion.

Validation so far: common/client/game-test compilation passed; all 35 focused Python regressions passed; repository/documentation and staged whitespace checks passed. All 19 selected server tests passed (`PumpklingGameTests`, `TestPlayersGameTests`, `JimtaLuaGameTests`, `PeerClientStartupGameTests`; `build/nov3-merge-server-verified.log`). The rerun reused unchanged Kotlin outputs from the successful compilation after an expensive redundant cache snapshot was interrupted. The actual `UndergroundDepositClientGameTests` client suite passed its world save/reopen check in 7m 53s (`build/nov3-merge-chima-verified.log`). Its initial local startup needed `processClientResources` to prepare the client mixin configuration. Canonical resource regeneration completed successfully and reproduced the staged merge exactly: zero additional generated-file differences (`build/nov3-merge-generation.log`). Full remote CI and the formerly cancelled group remain separate checks. No live deployment or restart is included.


## November 2 release reconciliation

The main release `c0a9532563` supplies the remaining acquisition routes. Merging it with the native repairs gives **zero unresolved routes and zero ingredient diagnostics** in the refreshed audit. The 78 entries already classified by upstream as deferred acquisition design remain unchanged; this reconciliation adds no exclusions. Earlier counts below are historical, not current blockers.

The combined implementation retains component-preserving present conversions instead of the release's ordinary shapeless recoloring recipes, uses a single registration for botanical special recipes, and retains the owner-approved Divine Pact crafting route alongside the release's forge ritual. Both audit adapters remain active so existing native evidence is preserved.

Imported model generation now uses the release's source-preserving writer before bounded finishing. The two hanging baskets and two paper-lantern templates retain the PR's surface/UV-preserving plane conversion and its dedicated regression tests; the redundant cuboid patches for those four models were removed from the release's exact-repair manifest. The other 108 exact repairs remain checked against their before/after hashes. The transparent-cap check uses the release's actual pixel sampling rather than the older overlapping shortcut.

The previous PR build and optional-library absence checks passed. Fourteen of sixteen rendered client groups passed; two reached the 60-minute job limit while still making progress, without a recorded assertion or out-of-memory exception. The workflow now divides the same selected suites across 24 groups, preserving the three isolated independent-peer suites and existing failure diagnostics. Passing the new remote run is still required; changing the grouping is not proof that every rendered test passes.

Reconciled with OpenAI Codex (GPT-6). No live server was restarted or updated by this work.

Local validation of the combined release: common/client/game-test compilation and canonical resource generation completed. All 13 `AcquisitionRepairGameTests` and `SurvivalRecipeGameTests` passed, exporting the actual registry used for the refreshed audit. The 399-test Foundation run passed 398 tests; its only failure was a stale scaffold crafting fixture, corrected to the retained recipe, after which all six recipe-conflict tests passed. Geometry regressions also passed for transparent caps, rotated occluders, lantern surfaces/UVs and repeatable candy structures. The test runtime used isolated resources linked to this checkout, with only the two server suites selected; it did not use or restart the live server.

GitHub Foundation passed for `010dd7ad7f`. Its Build run exposed a clean-checkout generation prerequisite: the architectural shape reader still required a Gradle-created client-only archive, although the workflow now prepares Mojang's pinned client archive directly. The reader now accepts that prepared archive first and retains the local Loom-cache fallback. Four focused regressions pass, including no-Gradle-cache generation, the farmland-parent alias and missing-reference rejection; all 4,192 vanilla model definitions match the previously used derived archive exactly. No model output or collision shape is changed by this lookup fix.

## November 1 native acquisition follow-up

The owner-authorized [native acquisition repairs](../features/native-acquisition-repairs.md) reduced the refreshed audit to **84 unresolved entries: 78 without a documented route and 6 blocked chains**, with **zero invalid ingredients**. This resolves 62 entries through present conversions, botanical upgrades and stripping, native compatibility decorations, Silk Touch Ashen Basalt and the approved Divine Pact recipe. The strict all-items assertion still fails on the remaining content; no exclusions were added. The counts and examples below are the historical 146-entry baseline and describe the state before those repairs.

## Previous baseline

The merged Peepo CI checkout still has **146 unresolved acquisition entries: 127 without a documented route and 19 with a blocked recipe chain**. The combined audit covers 15,091 registered Jugcraft items and reports **zero invalid ingredients and zero invalid template loot links**. These counts were refreshed after canonical generation completed, using the native recipe/interaction audit additions and the recovery of eight existing Supplementaries recipes.

**The strict Survival acquisition assertion still rejects these entries.** It must remain failing until these routes are implemented or existing native acquisition is verified and represented in the audit. No suppression, blanket exclusion, or invented recipe was added for these 146 entries. An unresolved audit entry is not, by itself, proof that the item is unobtainable in game.

| Family | Missing documented route | Blocked chain | Total |
| --- | ---: | ---: | ---: |
| Supplementaries (`supps`) | 43 | 0 | 43 |
| Forbidden Arcane (`forbidden`) | 34 | 6 | 40 |
| Jugtania (`jugtania`) | 13 | 12 | 25 |
| Homeworks (`homeworks`) | 19 | 1 | 20 |
| Holidayworks (`holidayworks`) | 8 | 0 | 8 |
| Ars Additions (`ars_additions`) | 4 | 0 | 4 |
| Twigworks (`twigworks`) | 3 | 0 | 3 |
| Wilds (`wilds`) | 2 | 0 | 2 |
| Aquatic (`aquatic`) | 1 | 0 | 1 |
| **Total** | **127** | **19** | **146** |

## Verified existing Supplementaries routes

The [Supplementaries acquisition adapter](../../tools/survival_supps_routes.py) removes 30 false missing/blocked entries without changing gameplay. [Natural wild flax](../../src/main/java/io/github/jimbozoomer/jugcraft/menagerie/OwnerSupplementFlora.java) provides seeds, and [planted flax](../../src/main/java/io/github/jimbozoomer/jugcraft/menagerie/OwnerSupplementFlax.java) grows to the mature lower-half harvest that produces fiber. This resolves the flax/block conversion loop and all 20 blocked awning/fodder entries. The adapter also records native barnacle generation, charged-creeper heads, treasure injections, globe purchases/conversions, paid rope-arrow and hidden-item crafting, and Silk Touch recovery of raked gravel.

The 43 remaining Supplementaries entries have these specific causes:

- **32 colored presents and trapped presents:** the supplied [present-dye recipe](../../art/owner-library/originals/Edited-2026-10-29/SUPPS/data/supplementaries/recipe/present_dye.json) and [trapped-present conversion](../../art/owner-library/originals/Edited-2026-10-29/SUPPS/data/supplementaries/recipe/trapped_present.json) use custom serializers with no native implementation. Ordinary uncolored presents remain craftable.
- **Eight candle/sconce compatibility variants:** their source recipes require items from absent external mods; the [native importer](../../tools/newest_supplementaries.py) correctly rejects those conditions rather than substituting materials. These are the Cupric, Ender, Soul and Spectacle candle holders, and Ender, Glow, Green and Nether Brass sconces.
- **Two compatibility rods:** `supps_edelwood_stick` and `supps_propelplant_cane` are registered decorative rods without a verified native acquisition conversion.
- **One formed block state:** [falling ash converts basalt to Ashen Basalt](../../src/main/java/io/github/jimbozoomer/jugcraft/menagerie/OwnerSupplementTerrain.java), but its [actual loot](../../src/main/resources/data/jugcraft/loot_table/blocks/supps_ashen_basalt.json) returns basalt. That transformation does not establish acquisition of the registered Ashen Basalt item. It remains visible for explicit review rather than receiving a guessed recipe or new exclusion.

## Confirmed causes and examples

- **Unsupported original recipe types:** the supplied [Terrasteel Helmet recipe](../../art/owner-library/originals/Edited-2026-10-29/Jugtania/data/botania/recipes/terrasteel_helmet.json) uses `botania:armor_upgrade`; the [Water Rod recipe](../../art/owner-library/originals/Edited-2026-10-29/Jugtania/data/botania/recipes/water_rod.json) uses `botania:water_bottle_matching_shaped`. The [native Jugtania importer](../../tools/newest_jugtania.py) does not import those serializers. They need proper native recipe support, including their original matching/component behavior.
- **Missing roots propagate into valid derived recipes:** `jugcraft:jugtania_stripped_livingwood_log` and `jugcraft:jugtania_stripped_dreamwood_log` block stripped and glimmering wood variants. Their initial acquisition remains unresolved; downstream crafting recipes do not establish a source. The native log-stripping registration is missing. The supplied Gaia-head beheading reward is also absent from [native Elementium Axe rewards](../../src/main/java/io/github/jimbozoomer/jugcraft/botany/BotanicalAdvancedTools.java).
- **Remaining Arcane progression roots:** the audit now includes the actual [forge rituals](../../src/main/java/io/github/jimbozoomer/jugcraft/arcane/ArcaneRituals.java), forge formation/upgrades, and Clibano processing through the [Arcane acquisition adapter](../../tools/survival_arcane_routes.py). It still lacks a supported route for `jugcraft:forbidden_divine_pact`, which blocks the Soul Binding Crystal, and for several tools/decorative entries. Source-backed imports or explicit acquisition design are still required; item registration alone is insufficient.

The smaller families also remain visible for follow-up: glass/iron Homeworks furniture, Holidayworks stockings and awake bat, Ars codex entries and Memory Crystal, Twigworks novelty/terrain entries, Wilds blessing/staff entries, and the Aquatic Largemouth Bass Bucket. Their exact IDs and blockers are retained in the [native acquisition audit](../audits/survival-recipes.json); this report does not classify them as intentionally unavailable.

## Evidence and scope

The measurement comes from `python tools/audit_survival_recipes.py --registry build/ci-survival-item-registry.json --write` after canonical resource generation; the output is retained in `build/ci-survival-audit-supps.log` and the compact review snapshot in `build/ci-native-routes-review.json`. The committed [registered-item snapshot](../audits/survival-item-registry.json), [audit implementation](../../tools/audit_survival_recipes.py), [botanical acquisition adapter](../../tools/survival_newest_routes.py), [native interaction adapter](../../tools/survival_newest_interactions.py), and [Supplementaries adapter](../../tools/survival_supps_routes.py) provide the reproducible inputs. Eighteen focused acquisition-graph regressions passed; the full Foundation run owns the strict all-items assertion. Source recipes and runtime handlers take precedence over item names, self-drops, or conversion loops. These are upstream content/acquisition issues exposed by the merged checkout, separate from the Staff Post behavior.
