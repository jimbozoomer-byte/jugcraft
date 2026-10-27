# Herobrine permanent underground discoveries

Owner approved on 27 October 2026; implementation by OpenAI Codex, paired with Claude's sky islands and surface ruins. This extends the existing event builder, not World Designer terrain generation. No live deployment is included.

## Player experience

- Nine cave layouts: redstone-torch trails, abandoned mining shelters with usable crafting table/furnace, watching rooms, stairs ending at sealed walls, geometric pillar grids, broken ravine bridges, balconies, buried shrines and empty columned halls. Rooms have entrances and navigable interiors.
- Four submerged glowstone patterns: rings, crosses, stepped pyramids and trails. Only source water is replaced; neither lava nor flowing water is accepted. The structures rest on the bottom rather than floating in the water.
- Selection varies by depth: upper caves favour shelters/trails; deep caves favour halls, watching rooms and spans; the bottom 32 blocks favour shrines and geometric chambers.
- All blocks persist indefinitely, through approach, logout and the November event cutoff. Only the apparition disappears. Players may explore, mine and adopt the structures. Operator undo remains available and preserves edited blocks and adopted containers; there is no automatic rebuild/reset.

## Discovery and protection

The existing per-player sighting scheduler remains unchanged. The world-bound exploration director additionally attempts a discovery after 180 active seconds underground or 900 active seconds on the surface. In quiet season these intervals are six times longer. AFK, unsafe, menu-open and offline time do not count. Placement is best-effort, not a guaranteed successful structure every interval. There is one search globally per ten seconds, at most six candidate columns per cave/water search, with failed searches waiting another minute. Surface selection can choose Claude's rarer sky islands or surface ruins. Existing records prevent same-depth repeats within 48 blocks underground or 64 blocks underwater, while allowing discoveries at least 20 blocks above/below. Reconnect/server restart resets the extra attempt clock conservatively; saved structure spacing persists.

The shared queue still limits all marks together to eight changed blocks per tick and two jobs. Site scans require all chunks already loaded, natural-looking surroundings, no containers, no claims and no town. Replacing/excavating rock requires operator-approved wilderness, excludes ores, and cannot open a liquid leak. Blocks are rechecked at placement. Additive cave rooms retain existing floor rather than replacing natural ground for appearance. The natural-block check remains a heuristic; unclaimed plain-stone structures are not provably natural, so excavation stays opt-in. No automatic wilderness approval is performed.

`/jugcraft herobrine mark <player> underground` and `underwater` search nearby; named cave/water variants place at the command's usual six-block offset if safe. Sky/surface group commands use the companion planners. Commands remain operator-only and Overworld-only.

## Economy / integration

Atmospheric exploration only, no research prerequisite, currency, loot chest, replenishing rewards or mandatory progression. The vanilla decorative materials and sparse glowstone are mineable once; placement spacing prevents repeated spawning at the same site. Built-in vanilla blocks reuse the installed game resources; no external texture assets or dependencies are introduced. Furnaces/crafting tables are usable normally. Existing saved marks remain compatible; new type names are strings in the same codec.

## Validation

Implementation in progress. Added native tests for every layout, real construction, SavedData round trips, 3D spacing, water/lava and container refusal, unloaded-chunk refusal, approval revocation/ore protection, approach permanence and adopted furnace inventory preservation. Added an isolated rendered gallery for every cave/water layout. Results must be recorded after the combined run; no passing result is claimed here yet.
