# Pirate ship crews and boarding

Status: crew and boarding code implemented on branch `claude/nov10-b-pirate-crew` (Claude Code, Agent B), 10 November 2026, with the review follow-ups of the same day (victory by roster, tether, own broadside, deck fighting, standing crew). The common and client Java of the first snapshot compiled for Codex; the follow-ups are not compiled here, and no game test has run; see Verification. The moving ship itself (`PirateShip`: movement, patrol, spawning, persistence, hold and loot, renderer) is a separate change by the coordinator, built against the interface here. The art is a separate change by Agent C. Scope follows the owner's decisions of 10 November 2026, relayed by Codex.

## What the player meets

A pirate ship carries a visible crew of ten plunderers: a helmsman at the wheel, a lookout in the crow's nest, four gunners at broadside cannon (two a side) and four deck crew. They stand at their stations, feet on the deck (or the crow's nest), facing the station's way: a ship's crew never takes the seated riding pose. Plunderers in boats and cannon boats still sit.

- **Gunnery.** Gunners lay their guns on a player within their arc (±45° of their side) and 5 to 40 blocks away. They fire only if the shell's flight reaches the player through loaded chunks. Each shot spends one gunpowder and one cannonball from the gunner's own supplies (8 of each, never refilled), with an 80-tick cooldown. Cannonballs from crew damage players only and never break blocks. They never hit a pirate ship's hull (`jugcraft:pirate_ship`) or deck (`jugcraft:pirate_ship_deck`), nor the gunner's shipmates; players' cannonballs are unaffected.
- **Boarding.** A player within 12 blocks of the deck who is in a boat or other vehicle, or who stands on the deck, makes the helm heave to. The deck crew step off their stations onto the deck and fight hand to hand. On the deck they move straight at the target and strike within 2 blocks, every 20 ticks, because paths do not run on an entity deck. Off the ship, the ordinary melee goal fights.
- **Tether.** Off its ship, a crew member fights only while it and its target are both within 24 blocks of the deck (of the ship's last seen place while the ship is not loaded). Beyond that it gives the target up and goes back aboard: by path where the ground allows, else straight at its seat, climbing aboard within 3 blocks of the deck. Crew aboard (gunners) are not tethered.
- **Release.** With nobody to fight for 200 ticks and none of its crew off the ship near the deck, the helm lets the ship go.
- **Defeat.** A ship is defeated once every crew member on its roster has died. The ship rolls its finite hold once; the crew code adds no loot. Crew that vanish without dying (Peaceful's despawn, an admin's discard) leave the ship undefeated, so it never gives up its hold without a fight.
- **Sinking.** When the ship sinks, its crew leave it and fight on as ordinary plunderers. A destroyed ship's surviving crew do the same.

## Contract with the ship

`menagerie/CrewedVessel` holds the stations, deck bounds, `sunk` and the saved heave-to flag. `menagerie/PirateCrew` holds `STATIONS`, `LOCAL_DECK`, `seat`, `mayBoard`, `helmManned`, `provision`, `defeated`, `forget`, `deckBounds` and the `Roster`.

- **Frame.** Ship-local blocks from the entity position: x to port, y up from the waterline, z to the bow. Offsets turn with the ship as `local.yRot(-yaw * DEG_TO_RAD)`, as `Zeppelin`, `Landship` and `RaiderCrew.seat` do. Facing is a Minecraft yaw relative to the ship.
- **Stations, in this index order:**

  | Index | Role | Seat | Facing | Arc | Muzzle |
  |---|---|---|---|---|---|
  | 0 | Helm | (0, 1, -6) | bow | 0 | seat |
  | 1 | Lookout | (0, 9.5, 1) | bow | 180 | seat |
  | 2, 3 | Gunners, port | (+2, 1, +2.5), (+2, 1, -2) | -90 | 45 | (+3.2, 1.6, z) |
  | 4, 5 | Gunners, starboard | (-2, 1, +2.5), (-2, 1, -2) | +90 | 45 | (-3.2, 1.6, z) |
  | 6-9 | Deck crew | (+1, 1, +5.5), (-1, 1, +5.5), (+1, 1, -3.5), (-1, 1, -3.5) | bow | 0 | seat |

  The deck is x -2.5..2.5, y 1..3, z -7.5..7.5 on a 6 by 16 hull.
- **What the ship does.**
  - Its `getPassengerAttachmentPoint` returns `PirateCrew.seat` unchanged; `seat` is already turned by the yaw.
  - Its `canAddPassenger` returns `PirateCrew.mayBoard`: the ship's own crew only, one to a station.
  - It moves only while `helmManned && !heaveTo && !sunk`.
  - Its spawner calls `PirateCrew.provision` once, before `addFreshEntityWithPassengers`.
  - Its `remove(reason)` calls `PirateCrew.forget(level, getUUID())` when `reason.shouldDestroy()`, never when it is only unloaded.
  - It rolls its hold once, when `PirateCrew.defeated` first answers true.
  - It never carries the static galleon's `JugcraftGalleon` tag.

## Saved state and identities

Crew ride the ship as passengers, so vanilla saves them inside the ship and brings them back with it: there are no duplicates and no separate ledger.

- **The plunderer** additionally saves `Ship` (UUID), `ShipStation` (int, also synced so both sides seat and face it) and `ShipPos` (where its ship was last seen).
- **The roster** (`PirateCrew.Roster`, saved data `jugcraft:pirate_crews` in the overworld, one for the server) keeps each ship's crew by UUID and which of them are lost.
  - `provision` puts each seated crew member on it.
  - Only a linked crew member's death marks it lost: its `die`, or removal as `KILLED`. A discard (`DISCARDED`: Peaceful's despawn, an admin's discard), unloading with its chunk, changing dimension or any other removal never does; that member stays unaccounted for.
  - `defeated` is true once the ship's roster is non-empty and every member is lost. A living crew member aboard answers false at once. Absence is never taken for death, so a crew member lured far away, or unloaded in a far chunk, keeps its ship undefeated until it actually dies. A ship with even one discarded crew member is never defeated and never rolls its hold.
  - `forget` drops a destroyed ship's entry.
- **Off the ship** (boarding), a crew member looks its ship up among loaded entities only.
  - While the roster still holds the ship and it is not loaded, the crew member waits, linked, however long.
  - Once the ship has been destroyed (forgotten) or has sunk, it unlinks.
- **The ship** saves only its heave-to flag beyond vanilla.
- **Galleon plunderers.** A plunderer with no `Ship` field (every static-galleon plunderer) saves, targets and steers boats exactly as before. Its `supps_*` IDs are unchanged.

## Limits

- **Targets.** Crew target players in survival or adventure only: never villagers (the existing rule) and never townsfolk.
- **Claims and settlements.** The crew fight only where a town's protection does not cover the spot, and where claim listeners allow an unprivileged pirate identity, `Authority.pirateCrewActor` (made in `concordance/Authority` beside the Tiny TNT stand-in, as the repository's check requires). It is checked at both the crew member's and the target's positions.
- **No chunk loading.** Ship lookups use loaded entities, aiming steps the flight with `WalkerAim.flight`, and every path check goes through `WalkerAim.loadedIn` (from `claude/nov10-cockpit`). Nothing adds a ticket or reads an unloaded chunk.
- **Untouched code.** No raider or town file changes. Outside `menagerie/`, the only main-code change is the one stand-in method added to `concordance/Authority`. On the client, `OwnerPlundererRenderer` draws passengers of a `CrewedVessel` standing (no riding pose, legs straight, skirt shown). Their feet are at the seat because the plunderer has no vehicle attachment offset.

## Verification

- **Java.** `javasyntax` parses all changed and new Java files with no errors. Every API used was checked against existing uses in the repository. Nothing was compiled; Gradle cannot run here.
- **Server GameTests**, `PirateCrewGameTests`, 13 tests on a test-only stub vessel (`jugcraft-test:stub_vessel`, `TestVessels`). Written but **not run**:
  - provisioning;
  - seats turned once at yaw 90, with feet at the seat's height;
  - save and reload without duplicates, the roster kept through unloading and its own save;
  - waiting for an unloaded ship and giving up a destroyed one;
  - boarding, the deck fight by direct movement, and defeat by roster;
  - victory by roster: a crew member 80 blocks off and one unloaded keep the ship undefeated until each dies;
  - only deaths defeat a ship: all discarded (as Peaceful's despawn) or 9 killed and 1 discarded is not defeated, all killed is;
  - the tether;
  - sinking;
  - no villagers or townsfolk;
  - claims;
  - one finite, damage-only shot;
  - unlinked plunderers unchanged.
- **Client GameTest**, `PirateCrewClientGameTests`, written but **not run**. It crews the stub vessel at yaw 0 and yaw 90 over a stand-in plank deck, mast and crow's-nest slab. It checks on the client that all ten stand on their seats, render without the riding pose and face their stations, and that an unlinked plunderer in a boat still sits. It saves the screenshots `pirate_crew_stations_yaw_0` and `pirate_crew_stations_yaw_90`. The stub draws nothing, so these show the stand-in deck only. Take them again on the real hull once `PirateShip` and `PirateShipRenderer` are in.
- **Python** `scripts/tests/test_pirate_crew_data.py`, 6 tests. They check that the 36 galleon data files are unchanged (JSON compared with LF line endings, so a CRLF checkout matches; NBT byte for byte, with a check of that rule itself), the station layout, the registration of the server and client tests, that the crew code adds no loot and stays out of raider and town code, and that the crew ask Authority's stand-in and their cannonballs name the pirate hull and deck types.
