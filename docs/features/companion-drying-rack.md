# Companion Drying Rack logistics

Owner-requested 30 October 2026, implemented with OpenAI Codex (GPT-6). Extends the existing Drying Rack, Planner assignments, Filter, optional production stock targets and physical Supply/Output deliveries. No new recipes, items, artwork, dependencies or progression gate.

## Behavior and balance

Select either rail of a complete Drying Rack with the Planner. It occupies one job assignment. Peepo and Jughead carry fresh herb-tag items or Peppers from linked Supply containers and hang them on the native four hooks, at most four items per hook. The Filter accepts Dried Herbs and Dried Chili; blank allows both. Production targets count actual finished Output stock, dried bunches on the rack, fresh bunches still drying and registered inbound deliveries.

Drying remains passive: each bunch needs the native 2,400 game ticks and yields one dried item per fresh item. Companions collect only dried bunches and deliver them to Output. They never take down an unfinished bunch, accelerate drying or spend reserve energy while waiting; existing load-scaled hauling effort still applies. They can perform other assigned work during the wait. A full rack stops incoming supplies; a full Output leaves ready goods on the rack. Manual hanging, early fresh take-down and native break drops remain available.

The hook contents and original drying timestamps remain the source of truth. Transfers use Fabric transactions; input probes, nested aborts and failed/partial output transfers restore the real contents and timestamps. Only committed changes update visible hook models and schedule the next native completion tick. No extra per-tick block entity or companion world scan is added. Native scheduled ticks and bounded checks of four hooks are retained.

Every live transfer checks the assignment, companion ownership, actual loaded block entity, complete matching two-block rack, and permissions on both rails. Cached ports cannot continue after a removed assignment or claim change. No chunks are loaded to find missing parts. The frame is a pathfinding obstacle, so delivery routes go around its inset collision shape.

## Persistence and compatibility

Existing `bunch_0` through `bunch_3` and `hung_0` through `hung_3` keys are unchanged. The existing shared Filter and stock settings are saved alongside them. Existing racks start with an empty Filter and unlimited production. Native item conversion is preserved, including its ordinary finished-item components. No additional output buffer or duplicate inventory is created.

## Focused validation

`PeepoDryingRackClientTests` covers native capacity and the exact 2,400-tick boundary, native manual take-down and pepper conversion, Filter acceptance and GUI access, bulk/nested transfer rollback, dried-only partial extraction, native timestamp/filter reload, foreign claims, missing upper rails, revoked assignments, and autonomous Supply/Output stock-target trips for both companions, including a full Output that is subsequently freed.

The autonomous test waits for real ingredient deliveries, confirms they are still fresh, then ages their saved native timestamps to five ticks before completion. Native time checks and normal companion output hauling complete the fixture. It explicitly invokes the rack's native scheduled-tick body after the shortened timestamp deadline to inspect the dry display: Minecraft retains the original scheduled deadline when the fixture rewrites saved timestamps. This avoids waiting two real minutes per variant without replacing the processing or delivery implementation. Focused command: `build-local.ps1 -Tasks @('runClientGameTest','-PclientTests=PeepoDryingRackClientTests')`. No two-client or large-server load test has been performed.
## Focused result — 30 October 2026

Validation used the working tree based on `22a573abef948af56dadbbb8b914df4ddac426be`, Minecraft 26.3, Fabric Loader 0.19.5 and Fabric API 0.161.0+26.3, with the repository's pinned development dependencies.

`build-local.ps1 -Tasks @('runClientGameTest','-PclientTests=PeepoDryingRackClientTests')` passed **57 assertions** with compilation in a disposable flat world. Evidence: `build/peepo-drying-rack-tests.log`. Both variants completed physical stock-limited deliveries and full-Output recovery. The first run exposed a fixture-only mismatch between accelerated saved timestamps and the original scheduled tick; the corrected run explicitly exercises the native tick body as described above. No full modpack suite or populated multiplayer playtest ran.
