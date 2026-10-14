# Changelog

Every change to Jugcraft, newest first, grouped by pull request. For what each thing does in game, see [docs/TECH_TREE.md](docs/TECH_TREE.md). For a full inventory with APIs and file locations, see [docs/WHAT_EXISTS.md](docs/WHAT_EXISTS.md).

**Testing:** automated server and rendered-client game tests run in CI. See the exact feature/integration commit for results; human two-client playtesting and release readiness remain unverified.

## Unreleased

No numbered release yet. Entries on feature branches remain proposed until their pull requests merge.

### Added: The Cydonian Sanctum and the Altar Keystone (part 2 of the owner's Mars boss)
- **The Altar Keystone,** made in the factories (titanium, tungsten, an advanced circuit) or by the Concordance (dreamglass and an astrolabe). Set into the Ancient Altar at night, the three altars kindle with red fire, a column of red light rises, and the **red mist** opens onto Mars for 60 seconds.
- **The Cydonian Sanctum,** a lair on Mars under a butterscotch sky with two moons: thin air (Jump Boost I, half fall damage), a gate chamber with the Red Mist home, the **Dust Gallery** with dust vents that puff on a rhythm and three **star dials** that light the **Star Door**'s constellations and grind it open, and the crater where the Cydonian Sentinel will wait (part 3). Record: [cydonian-sanctum.md](docs/features/cydonian-sanctum.md).

### Added: Adam-ondi-Ahman, the valley and its altar (part 1 of the owner's Mars boss)
- **A quiet valley near the start of every new world,** 800 to 1,600 blocks away: rolling prairie grass, wildflowers and goldenrod, lone oaks and birches, grazing animals and few monsters, with four seasons. **Rare valleys** grow elsewhere (`adam_ondi_ahman.spacing`, `adam_ondi_ahman.chance`). Modelled on the real Adam-ondi-Ahman in northern Missouri.
- **A knoll of three stacked stone altars** at each valley's heart, with a path up it, the log footings of two cabins, a fallen chimney, a spring and a ring of gathering stones at its foot. On the top lies the **Ancient Altar**, carved with a red star: unbreakable, and dormant until part 2's Altar Keystone wakes it.
- The plan for all three parts, the Mars dungeon and the Cydonian Sentinel included: [branches/BOSSES.md](docs/branches/BOSSES.md). Record: [adam-ondi-ahman.md](docs/features/adam-ondi-ahman.md).


### Added: The Howitzer Walker, playable
- Owner accepted the updated local playtest on October 13: aim without holding fire, no close-ground aim freeze, and hide the pilot model/equipment while mounted. Implementation and correction record: [Howitzer Walker](docs/features/howitzer-walker.md).
- **The owner's Howitzer Walker is in the game:** a tall, two-legged artillery walker in riveted olive plate with a big howitzer in an open cradle, made from a Diesel Walker beside the Armoured Walker (`SDS / PWP / R R`: two steel blocks, a dispenser, steel plates, the Diesel Walker and pistons). It walks, turns, climbs and refuels as the other walkers do.
- **Mouse aims; hold use to fire the howitzer:** on the pilot's own Range Finder mark, else on the block they look at. The whole walker turns to face it and the barrel elevates (15 degrees down to 60 up) to the arc that lands a Heavy Shell there, about 115 blocks at most. It fires once laid, every 4 seconds, a shell from the pilot's inventory; the blast is damage-only, as every gun's.
- **Jump** crouches, then springs about three blocks up. **Attack stomps:** a hop (or, in the air, a slam) whose landing hurts and throws everything within 3.5 blocks for 14 damage.
- **Its animations, drawn from the owner-reviewed clips:** the floaty, bouncy walk; the shot's recoil, rocking hull, cartoon star burst and rolling smoke puffs; and the jump's crouch, tuck and squashy landing, fitted to the real leap. Game tests and a screenshot scene. Record: [howitzer-walker.md](docs/features/howitzer-walker.md).
