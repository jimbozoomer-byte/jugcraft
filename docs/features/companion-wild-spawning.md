# Wild companion spawning

Owner requested 27 October 2026. Implemented with **OpenAI Codex (GPT-6)** on `peepo-companion`.

## Behavior and balance

Peepo and Jughead retain their existing eleven Overworld habitats, main/secondary spawn weights of 12/8, and ordinary groups of 1–4. The owner subsequently lowered the local limit to **four wild companions of each type within 128 horizontal blocks**. Only living, naturally spawned, unowned companions count. Taming frees a place immediately; existing natural-origin saved data and owner data are retained. Command/egg companions remain outside the natural budget. Existing companions above the new limit are preserved; further spawning stops until space is available.

Supplemental natural groups of 1–3 use a separate, bounded scheduler. They can appear when other passive animals fill Minecraft's creature budget. A region receives at most one opportunity per minute, starting after a one-minute server warmup. On a suitable sampled position the chance is 50% when that type has no nearby wild population, or 20% when some are present; secondary habitats multiply this chance by 8/12. One probability roll is made for the region, not for each player or surface sample. Failed placement or a full local population can mean no group. Terrain availability and server load affect encounter time; a spawn within a fixed time is not guaranteed.

These remain discoverable early companions: food from hunting/farming tames them, and existing companion jobs, energy, tools, Supply/Output and progression remain the downstream use. No new item, resource, recipe, currency, asset or dependency is introduced. No terrain regeneration is needed.

## Server work and restrictions

`CompanionSpawning` uses Fabric's server tick/lifecycle events. There is one schedule per server, with no entity or world references in its entries. It admits at most one batch per 20 game ticks across the server: eight cheap player checks, eight candidate surface columns, and at most six group placement attempts / three inserted companions. Overworld players are considered round-robin; dead players and spectators are skipped. Busy servers defer work instead of running catch-up loops. Existing work/path budgets are not consumed by spawning.

Cooldowns are keyed by 128-block regions, shared between all players; adjacent regions also block one another for the minute to prevent border crowding from multiplying attempts. Cooldown entries expire with bounded pruning and a hard 1,024-entry bound; at one admission per second ordinary active occupancy is much smaller. The transient schedule is cleared on server stop. Restarting starts a fresh one-minute warmup, and the live population cap is still enforced. Reconnecting or changing party does not reset a region's cooldown.

Surface samples are 32–80 horizontal blocks from the selected player. Final placements must be over 24 and at most 96 blocks from the nearest eligible player, outside the normal 24-block world-spawn exclusion, inside the world border, and in loaded, entity-ticking chunks eligible for natural spawning. Neighbor blocks used by collision tests must also be loaded. Height lookup uses the already-loaded chunk; no chunk ticket, load request, full terrain scan, path search or population scan across the dimension is added.

Both paths use the same wild population check, which stops collecting matches at four. It is not cached: taming, death, unload and insertion are reflected immediately, including each successive member of a group. The extra path preserves habitat, ground, dry-surface, native spawn placement, entity spawn-rule and collision checks, plus the current environment spawn table. It obeys `minecraft:do_mob_spawning`, uses natural spawn provenance and ordinary `addFreshEntity`, including Fabric's `ALLOW_LOAD` veto. Existing town/sanctuary hostile-only restrictions remain hostile-only. It does not reinterpret build permission as a new ban on friendly wildlife.

Chunk-generation spawning stays disabled because it cannot reliably see neighboring live companions for the cap. Supplemental spawning is intentionally independent of the global passive-creature budget; ordinary spawning still uses that budget. Limits concern loaded populations around the candidate, as before; no world-wide companion census is introduced.

## Validation

On Minecraft 26.3 / Fabric Loader 0.19.5 / Java 25, `PeepoSpawnClientTests` passed **872 assertions across all eleven habitats**, with `BUILD SUCCESSFUL` in 1m18s. The selected run checked unchanged weights/groups, native and supplemental local caps, taming replacements, a saturated passive budget (twelve cows), spawn vetoes, invalid surfaces, unloaded probes without a changed loaded-chunk count, dimensions, world borders, player distance, shared region borders and server admission frequency. The actual bounded surface sampler also found each habitat. Two hundred synthetic neighboring-player requests exercise the shared cooldown; this is not a two-client or large-server load test.

Reproduce with `./build-local.ps1 -Tasks @('runClientGameTest','-PclientTests=PeepoSpawnClientTests','-x','processResources')`; evidence is in `build/companion-wild-spawning-tests.log`. No resources changed. The fixture waits for its prepared chunks to become entity-ticking before exercising the stricter supplemental path. Existing general-companion/tree-tap cap assertions were adjusted to the four-companion policy and compiled, but those broader suites were not run. Repository documentation-link and whitespace checks passed. No distribution update, server deployment, commit or push was performed.
