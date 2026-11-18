# Companion Staff Posts

Owner-requested on 31 October 2026, implemented with **OpenAI Codex (GPT-6)** under the standing owner model exception. Extends the existing Peepo/Jughead Planner, four-job assignment list, shared scheduler, seating reservations, entity renderer and menu permissions. No dependency, new block, recipe, item or production unlock.

## Player behavior

Select a tamed companion, shift-right-click the Companion Planner, choose **Staff Post: choose furniture or a block**, and click the actual support. One post per companion is enforced during assignment and saved-data loading. It consumes one of the four work slots and appends after existing jobs. The existing pencil, priority arrows and removal controls apply. Initial facing points toward the assigning player; the Planner displays a direction arrow. The post's gear offers Automatic/Standing/Sitting and left/right rotation.

Ordinary blocks and slabs provide standing-only posts. Furniture using either existing player-seat interface, including the companion stool, hay bale seat, household stools and suitable wooden chairs, can offer both poses. Standing uses a broad, central collision deck rather than player riding offsets or the highest point of a chair back. Seat positions put short legs beyond the front edge. Both positions require loaded, nonhazardous, unobstructed space and a safe approach. The post may be a slab; its adjacent entrance/exit uses the existing full, sturdy floor contract of companion beds and seats. Jughead's full jug clearance is checked.

Automatic furniture posts alternate standing/sitting after independently randomized 600–1,800 tick intervals. Smooth twenty-tick presentation transitions cover mounting, pose changes and departure. Standing uses a small upper-body weight shift with planted feet; seated legs kick. The Social setting allows occasional stationary greetings to the nearby owner. Held lights retain their existing pose. No shop sales, inventory manipulation, machine assistance or fake productive rewards occur at a post.

Posts follow normal work priority and shift/recovery gates. A high-priority post intentionally occupies the worker before lower-priority productive jobs; leave it last for a fallback station. Real Supply/Output and porter deliveries retain their physical lifecycle and can interrupt staffing. Food, commands, settings editing, permission loss, occupied furniture, removed blocks and unloading release the post safely. Standing has zero work cost and ordinary 2 JE/t passive recovery; actual sitting retains 8 JE/t before food buffs. No new energy source or production discount is created.

## Ownership, persistence and server bounds

Only authorized Planner/menu operations can create or edit a post. Existing distance, loaded-dimension, owner/party, town and land-claim checks remain. Menu edits validate the displayed row and anchor and use the existing rate limit. The support's actual block ID, horizontal facing, pose preference, priority and custom name are saved with the assignment. Existing targets default to no post. Duplicate saved post entries are rejected; no extra job slot is created.

Active poses, interpolation endpoints and reservations are transient. Normal saved safe-exit handling permits reacquisition after reload; unloading does not reposition an entity during section removal. Player and companion seating share reservations. A post attached to a machine or container does not expose that block's productive Filter or logistics through the decorative assignment.

Only the single assigned support and its bounded nearby approach geometry are inspected. Native shapes are capped at 128 boxes. Cached mounted-space validation, existing path/search admission, expiring reservations and randomized pose timers avoid global furniture searches or per-tick inventory scans. Greeting checks look up the known owner under the existing search budget; they do not scan a crowd. Clients animate from synchronized transition times and endpoints rather than receiving per-frame pose packets. No chunk tickets are added.

## Integration and provenance

The post supplies an aesthetic attendant for player-built shops, reception desks and food stalls. Existing furniture is its input; no product is emitted. Nearby farms, kitchens and sales blocks keep their original costs and operations. No asset library files are modified or newly imported. The owner catalog and approved framework catalog were inspected; existing companion rigs, textures and procedural rest/gesture conventions are reused with a small native pose helper.

## Validation

`PeepoStaffPostClientTests` passed **19 groups / 11,892 assertions** on 31 October 2026 (`build/peepo-staff-tests-4.log`). This covers both variants, real Planner/menu edits and stale packet rejection, the one-post limit including duplicate saved entries, settings persistence, native furniture/block/slab geometry, player/companion occupancy and stale-readiness races, overhead clearance along the whole mounting path, live claim permissions, real navigation, random pose deadlines and state changes, seated recovery, Social greetings, command/removal/obstruction interruption, unload cleanup, higher-priority gardening and return to the post.

The successful run compiled common, client and game-test sources. Earlier checks identified fragmented stool collision boxes and an incorrectly oriented chair fixture; the final run verifies the union-supported stool deck and rejects seated poses through a chair back. The run accelerates pose deadlines only after checking their bounds; it does not claim to wait through every full 30-90 second interval.

Native-render evidence is in `build/companion-staff-post-evidence/`: 81 source frames, `staff-post-phases.png`, Planner/options screenshots, the passing test log and `peepo-jughead-staff-post.gif`. The shareable GIF is also in the workspace's `Companion Staff Post Animation Previews 2026-10-31` folder. It shows mounting, standing, sitting with leg kicks, a greeting, rising and departure for both models. UI fit, foot/seat contact, attached limbs and jug clearance were visually reviewed. Preview metadata is controlled to show the complete cycle; live behavior is exercised separately.

Shared regressions passed in `build/peepo-staff-regressions.log`: `PeepoRestClientTests` (30,097 assertions), `PeepoWorkSessionClientTests` (62), and `PeepoDeliveryClientTests` (59). Repository structure/documentation links and scoped diff whitespace checks passed. The final Staff Post run also passed real porter departures for both variants: the route keeps its plan, movement and inventory transfers wait for the full departure, and physical cargo is delivered before the worker returns to its post. No independent two-client or large-server load result is claimed.
