# Changelog

Every change to Jugcraft, newest first, grouped by pull request. For what each thing does in game, see [docs/TECH_TREE.md](docs/TECH_TREE.md). For a full inventory with APIs and file locations, see [docs/WHAT_EXISTS.md](docs/WHAT_EXISTS.md).

**Testing:** automated server and rendered-client game tests run in CI. See the exact feature/integration commit for results; human two-client playtesting and release readiness remain unverified.

## Unreleased

No numbered release yet. Entries on feature branches remain proposed until their pull requests merge.

### Added: The Foundry Trench and Sealed Orders (boss 7, the Iron Dreadnought, part 1)
- **The Foundry Trench,** a seventh lair: a ruined test pit sunk behind the front line under a sky of drifting smoke, 47 blocks by 41 of blast-scorched concrete eight blocks below the rim, walled with reinforced concrete and blast doors. Three grounded pylons of steel lattice stand at its west, east and south, each crowned with a tesla coil, a breaker at its foot facing the pit and a copper ground terminal in the floor before it, with a hazard-striped lane from the pit's middle to each. Three concrete bastions and a landship's burnt-out hull are its cover; steel catwalks run along its side walls, and the Dreadnought's assembly gantry stands in its north wall. A fire trench of sandbags and duckboards runs along its south rim, a ramp of sandbag steps goes down into the pit, and the dugout off it is where you arrive and where Grey Mist waits in the back doorway. Every open cell of the pit's floor has block light 7 or more.
- **Its fixtures:** the breakers, which any player may use; until part 2 there is no current, and a breaker only sparks. The crowns, breakers' lamps and terminals can burn live, and a breaker can be thrown or fouled by a plate of armour: part 2's fight does all of it.
- **Sealed Orders** (three paper, a steel plate, copper wire and a honeycomb) open it: use them on a Field Telephone that stands in a trench (four or more sandbags, revetments or duckboards round it) under the open sky, at any hour. The bell rings, a whistle shrills, and you go over the top; anyone who uses the open line or the telephone follows for a minute. Under a roof, out of a trench or outside the Overworld they say why and are kept. Record: [foundry-trench.md](docs/features/foundry-trench.md).

### Added: The Howitzer Walker, playable
- Owner accepted the updated local playtest on October 13: aim without holding fire, no close-ground aim freeze, and hide the pilot model/equipment while mounted. Implementation and correction record: [Howitzer Walker](docs/features/howitzer-walker.md).
- **The owner's Howitzer Walker is in the game:** a tall, two-legged artillery walker in riveted olive plate with a big howitzer in an open cradle, made from a Diesel Walker beside the Armoured Walker (`SDS / PWP / R R`: two steel blocks, a dispenser, steel plates, the Diesel Walker and pistons). It walks, turns, climbs and refuels as the other walkers do.
- **Mouse aims; hold use to fire the howitzer:** on the pilot's own Range Finder mark, else on the block they look at. The whole walker turns to face it and the barrel elevates (15 degrees down to 60 up) to the arc that lands a Heavy Shell there, about 115 blocks at most. It fires once laid, every 4 seconds, a shell from the pilot's inventory; the blast is damage-only, as every gun's.
- **Jump** crouches, then springs about three blocks up. **Attack stomps:** a hop (or, in the air, a slam) whose landing hurts and throws everything within 3.5 blocks for 14 damage.
- **Its animations, drawn from the owner-reviewed clips:** the floaty, bouncy walk; the shot's recoil, rocking hull, cartoon star burst and rolling smoke puffs; and the jump's crouch, tuck and squashy landing, fitted to the real leap. Game tests and a screenshot scene. Record: [howitzer-walker.md](docs/features/howitzer-walker.md).
