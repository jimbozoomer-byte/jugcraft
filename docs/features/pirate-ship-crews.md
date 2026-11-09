# Pirate ship crews and boarding

Status: crew and boarding code implemented on branch `claude/nov10-b-pirate-crew` (Claude Code, Agent B), 10 November 2026. Not compiled, and no game test has run; see Verification. The moving ship itself (`PirateShip`: movement, patrol, spawning, persistence, hold and loot, renderer) is a separate change by the coordinator, built against the interface here. The art is a separate change by Agent C. Scope follows the owner's decisions of 10 November 2026, relayed by Codex.

## What the player meets

A pirate ship carries a visible crew of ten plunderers: a helmsman at the wheel, a lookout in the crow's nest, four gunners at broadside cannon (two a side) and four deck crew.

- **Gunnery.** Gunners lay their guns on a player within their arc (±45° of their side) and 5 to 40 blocks away. They fire only if the shell's flight reaches the player through loaded chunks. Each shot spends one gunpowder and one cannonball from the gunner's own supplies (8 of each, never refilled), with an 80-tick cooldown. Cannonballs from crew damage players only and never break blocks.
- **Boarding.** A player within 12 blocks of the deck who is in a boat or other vehicle, or who stands on the deck, makes the helm heave to. The deck crew then go over to fight hand to hand. With nobody to fight for 200 ticks, the helm lets the ship go and the crew walk back to their stations.
- **Defeat.** With every crew member dead, the ship is defeated. The ship rolls its finite hold once; the crew code adds no loot.
- **Sinking.** When the ship sinks, its crew leave it and fight on as ordinary plunderers.

## Contract with the ship

`menagerie/CrewedVessel` holds the stations, deck bounds, `sunk` and the saved heave-to flag. `menagerie/PirateCrew` holds `STATIONS`, `LOCAL_DECK`, `seat`, `mayBoard`, `helmManned`, `provision`, `defeated` and `deckBounds`.

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
  - It never carries the static galleon's `JugcraftGalleon` tag.

## Saved state and identities

Crew ride the ship as passengers, so vanilla saves them inside the ship and brings them back with it: there are no duplicates and no separate ledger.

- **The plunderer** additionally saves `Ship` (UUID), `ShipStation` (int, also synced so both sides seat and face it) and `ShipPos` (where its ship was last seen).
- **Off the ship** (boarding), a crew member looks its ship up among loaded entities only.
  - While the ship's last place is in an unloaded chunk, it waits.
  - Once that place is loaded and the ship has not been there for 100 ticks, or the ship has sunk, it unlinks.
- **The ship** saves only its heave-to flag beyond vanilla.
- **Galleon plunderers.** A plunderer with no `Ship` field (every static-galleon plunderer) saves, targets and steers boats exactly as before. Its `supps_*` IDs are unchanged.

## Limits

- **Targets.** Crew target players in survival or adventure only: never villagers (the existing rule) and never townsfolk.
- **Claims and settlements.** The crew fight only where a town's protection does not cover the spot, and where claim listeners allow an unprivileged pirate identity. This is the same pattern as `Authority`'s Tiny TNT stand-in. It is checked at both the crew member's and the target's positions.
- **No chunk loading.** Ship lookups use loaded entities, aiming steps the flight with `WalkerAim.flight`, and every path check goes through `WalkerAim.loadedIn` (from `claude/nov10-cockpit`). Nothing adds a ticket or reads an unloaded chunk.
- **Untouched code.** No raider or town file changes.

## Verification

- **Java.** `javasyntax` parses all changed and new Java files with no errors. Every API used was checked against existing uses in the repository. Nothing was compiled; Gradle cannot run here.
- **Server GameTests**, `PirateCrewGameTests`, 10 tests on a test-only stub vessel (`jugcraft-test:stub_vessel`, `TestVessels`). Written but **not run**:
  - provisioning;
  - seats turned once at yaw 90;
  - save and reload without duplicates;
  - waiting for an unloaded ship and giving up a gone one;
  - boarding and defeat;
  - sinking;
  - no villagers or townsfolk;
  - claims;
  - one finite, damage-only shot;
  - unlinked plunderers unchanged.
- **Python** `scripts/tests/test_pirate_crew_data.py`, 4 tests. They check that the 36 galleon data files are byte-identical, the station layout, the registration, and that the crew code adds no loot and stays out of raider and town code.
