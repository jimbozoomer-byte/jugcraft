# Changelog

Every change to Jugcraft, newest first, grouped by pull request. For what each thing does in game, see [docs/TECH_TREE.md](docs/TECH_TREE.md). For a full inventory with APIs and file locations, see [docs/WHAT_EXISTS.md](docs/WHAT_EXISTS.md).

**Testing:** automated server and rendered-client game tests run in CI. See the exact feature/integration commit for results; human two-client playtesting and release readiness remain unverified.

## Unreleased

No numbered release yet. Entries on feature branches remain proposed until their pull requests merge.

### Added: Guns, slice 13D (a revolving launcher, a carbine revolver and a musket of light)
- **Three guns** from the owner's models and animations: the **Fencepost Thumper**, a revolving grenade launcher of four grenades loaded one at a time, its barrel tipped down to load and its pump turning the cylinder after each shot; the **Parley Revolver**, a heavy single-action revolver of six rifle rounds held in one hand, which with a stock fitted is held in both as a carbine and plays the owner's carbine animations; and the **Lantern Musket**, an energy musket whose beam passes through every creature in its line, three shots to a core.
- The Fencepost Thumper is made from iron, brass, a lever and a plank; the Parley Revolver from steel, iron, brass and a lever; the Lantern Musket from steel, a lantern, a plank, an advanced circuit and brass. The Thumper takes the Light and Vertical Grips and the bayonets; the Parley Revolver the barrel attachments and the stocks; the Lantern Musket nothing.
- A carbine (the Parley Revolver with a stock) is not one of two guns held at once.
- Record: [guns.md, slice 13D](docs/features/guns.md#slice-13d-a-revolving-launcher-a-carbine-revolver-and-a-musket-of-light).

### Added: Guns, slice 13C (a punt gun, a repeater and a heavier rifle)
- **Three guns** from the owner's models and animations: the **Flatboat Gun**, a punt gun whose chamber swings out to take one shell, one blast of sixteen pellets and a long reload; the **Windlass Musket**, a flintlock repeater of seven paper cartridges, its drum turned between shots and its magazine changed whole; and the **Millstone Rifle**, a heavy lever-action rifle of eight rifle rounds loaded a round at a time, slower than the Drover Rifle and harder hitting.
- The Flatboat Gun is made from a block of iron, iron, brass, a lever and a plank; the Windlass Musket from iron, brass, a flint, a lever and a plank; the Millstone Rifle from steel, iron, brass, a lever and a plank. The Flatboat Gun takes the scopes and the Laser Sight; the Windlass Musket the Light and Vertical Grips and the bayonets; the Millstone Rifle what the Drover Rifle takes.
- Record: [guns.md, slice 13C](docs/features/guns.md#slice-13c-a-punt-gun-a-repeater-and-a-heavier-rifle).

### Added: Guns, slice 13B (more frontier guns)
- **Three guns** from the owner's models and animations: the **Fencepost Carbine**, a lever-action carbine of seven rifle rounds loaded a round at a time through a lid in its stock; the **Waystation Shotgun**, a lever-action shotgun of eight shells with a drum under its action, loaded a shell at a time; and the **Highball Rifle**, a single-shot rifle whose barrel swings aside to be loaded, one heavy, true shot at long range.
- The Fencepost Carbine and the Waystation Shotgun are made from iron, brass, a lever and planks, as the lever guns; the Highball Rifle from steel, a lever and a plank. Each takes the grips and the bayonets; the shotgun and the rifle the barrel attachments too, and the rifle the scopes and the Laser Sight.
- The owner's Fencer Thumper waits for its cylinder to turn with each shot.
- Record: [guns.md, slice 13B](docs/features/guns.md#slice-13b-more-frontier-guns).

### Added: Guns, slice 13A (the frontier guns)
- **Three guns** from the owner's models and animations: the **Tabby Revolver**, a gunmetal revolver held in one hand, its cylinder swung out to load a round at a time, tossed end over end as it is drawn; the **Lookout Rifle**, a lever-action rifle of eight rifle rounds loaded a round at a time, made to take a scope; and the **Ammonite Carbine**, a carbine of sixteen rifle rounds from a long curved magazine, its barrels caged in copper bands. The Tabby Revolver pairs with any other one-handed gun.
- The Tabby Revolver is made from iron, brass, a lever and a plank; the Lookout Rifle from iron, brass, a lever and planks; the Ammonite Carbine from copper, iron, a lever and a plank. The Tabby Revolver takes the barrel attachments; the Lookout Rifle the stocks, grips, bayonets, scopes and Laser Sight; the Ammonite Carbine the stocks.
- Record: [guns.md, slice 13A](docs/features/guns.md#slice-13a-the-frontier-guns).

### Added: The Howitzer Walker, playable
- Owner accepted the updated local playtest on October 13: aim without holding fire, no close-ground aim freeze, and hide the pilot model/equipment while mounted. Implementation and correction record: [Howitzer Walker](docs/features/howitzer-walker.md).
- **The owner's Howitzer Walker is in the game:** a tall, two-legged artillery walker in riveted olive plate with a big howitzer in an open cradle, made from a Diesel Walker beside the Armoured Walker (`SDS / PWP / R R`: two steel blocks, a dispenser, steel plates, the Diesel Walker and pistons). It walks, turns, climbs and refuels as the other walkers do.
- **Mouse aims; hold use to fire the howitzer:** on the pilot's own Range Finder mark, else on the block they look at. The whole walker turns to face it and the barrel elevates (15 degrees down to 60 up) to the arc that lands a Heavy Shell there, about 115 blocks at most. It fires once laid, every 4 seconds, a shell from the pilot's inventory; the blast is damage-only, as every gun's.
- **Jump** crouches, then springs about three blocks up. **Attack stomps:** a hop (or, in the air, a slam) whose landing hurts and throws everything within 3.5 blocks for 14 damage.
- **Its animations, drawn from the owner-reviewed clips:** the floaty, bouncy walk; the shot's recoil, rocking hull, cartoon star burst and rolling smoke puffs; and the jump's crouch, tuck and squashy landing, fitted to the real leap. Game tests and a screenshot scene. Record: [howitzer-walker.md](docs/features/howitzer-walker.md).
