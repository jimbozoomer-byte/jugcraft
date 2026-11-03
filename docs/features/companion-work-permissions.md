# Companion work with mob griefing disabled

Owner-requested 29 October 2026. Implemented with OpenAI Codex (GPT-6), under the existing owner exception.

## Behavior

Peepo and Jughead job actions no longer require the vanilla `minecraft:mob_griefing` gamerule to be true. Servers can keep that rule false to restrict other mobs while companions perform their assigned work. No new toggle, item, recipe or migration is required, and the implementation never changes the world's gamerule.

The exception covers garden planting, harvesting and maintenance (including the shared orchard/magic-crop paths), shearing, milking, egg collection, configured breeding/slaughter, fishing, smoked hive harvesting, water collection, tree taps, bat-house collection and permitted automatic fence-gate opening. Machine jobs and Supply/Output already use their own assignment and permission checks; their behavior is unchanged.

This is an exception for explicit companion job actions, not a global entity or gamerule override. Other mobs, vanilla farmland trampling and unrelated game rules retain their normal behavior. Sheep still regrow wool through their existing native eating behavior. Breeding and slaughter remain off by default until enabled for a Livestock Management zone.

## Permissions, cost and compatibility

Loaded chunks, owner/claim and town protection, exact assignments, enabled feature settings, safe approaches, required tools/inputs, smoke, cargo space, output routing, leases and energy remain authoritative. Gates still require an owned companion, deny powered gates and retain their existing animal barrier/closing behavior. No fake player, temporary global-rule change, chunk loading, scan or new network message is added. The legacy disabled-work status keeps its numeric position and uses the general label **Work disabled**.

The change removes direct gamerule gates only from the companion implementations, including their readiness, supply/tool probes and completion paths. It leaves ordinary mob code untouched. Existing job inputs, products, tiers, resource/energy costs, unlocks and shared server budgets do not change. There are no new art assets, dependency changes or save fields.

## Validation

Common/client compilation passed. Focused client tests passed with mob griefing disabled: `PeepoHarvestClientTests` (467 assertions), `PeepoActionClientTests` (2,411), `PeepoTreeTapClientTests` (76), and `PeepoPopulationClientTests` (41). These exercise real planting/harvesting, animal care, water collection, tree-tap Supply/Output and breeding/slaughter. Additional native job checks cover both variants' hive and bat-house completion, fishing startup, gate eligibility, foreign-claim denial, required smoke, and an ordinary crow continuing to obey the global rule. The world rule remains false after companion work.

The first run passed the action and tree-tap suites, then exposed a test-fixture error: synchronous hive actions exceeded the existing per-tick energy limit. The fixture now advances through actual server ticks without weakening that limit; the harvest and population suites passed on the corrected run. Logs are retained locally as `build/companion-work-permissions-tests.log` and `build/companion-work-permissions-remaining.log`. Commands used `./build-local.ps1 -Tasks @('runClientGameTest','-PclientTests=PeepoHarvestClientTests,PeepoActionClientTests,PeepoTreeTapClientTests,PeepoPopulationClientTests','-x','processResources')`, then the harvest/population subset. Already-built main resources were reused; no runtime resources changed for this feature.

Repository structure/documentation-link validation and `git diff --check` passed. The tests use disposable integrated-server worlds, not a multi-client or large-server benchmark. No full-pack gameplay suite, distribution update or live-server restart was performed.
