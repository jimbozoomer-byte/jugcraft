# Changelog

Every change to Jugcraft, newest first, grouped by pull request. For what each thing does in game, see [docs/TECH_TREE.md](docs/TECH_TREE.md). For a full inventory with APIs and file locations, see [docs/WHAT_EXISTS.md](docs/WHAT_EXISTS.md).

**Testing:** automated server and rendered-client game tests run in CI. See the exact feature/integration commit for results; human two-client playtesting and release readiness remain unverified.

## Unreleased

No numbered release yet. Entries on feature branches remain proposed until their pull requests merge.

### Unmerged: The other machine screens in their looks
- **Eight more screens take the look of their tech level**, with the machine screens' status lamp, plain status and help:
  - the prospector in the workshop look;
  - the rocket pad and the storage tank in the industrial look;
  - the logic controller, reactor console and remote shutdown panel in the advanced look;
  - the drone depot terminal and drone tower in a drone variant of it (the tower's graphite and red).
- **Simpler to use:**
  - the rocket pad gets hint icons and help on its slots and a Launch key, with the last result by the title;
  - the logic controller gets headings, keys with tooltips, lit rules and channel readings;
  - the storage tank's readouts are grouped, with a fill bar and an outlet lamp;
  - the drone keys light when they can be used.
- Buttons, slots, menus and saves are unchanged. Record: [other-machine-screens.md](docs/features/other-machine-screens.md).

### Unmerged: Machine screens in five looks by tech level
- **Every machine screen takes the look of its tech level**, after the owner's five references:
  - workshop brass and orange for the first machines;
  - industrial steel and green for steel, oil and the industrial forms;
  - an electric blue device for the power gear;
  - a lab blueprint for chemistry;
  - an advanced cyan hologram for electronics and nuclear fuel.
- **Simpler to read:**
  - a status lamp by the title;
  - one word for what the machine is doing, and the fix when it is stuck ("Connect a cable or a generator.", "Take the results from OUT, or turn on Eject.");
  - progress with the time left;
  - a help box saying what the machine is for.
- **Controls are icon buttons with tooltips**, along the top of the help box. The six face buttons move behind one Sides button, which shows the faces with a colour key. Batteries and solar panels show a big charge gauge with its percentage. Empty slots show IN and OUT labels and hint icons, and say what goes in them when hovered. The decorative keyboard is gone.
- Industrial forms share the layout: pause, cancel, recipes and vent or drain icons, and their tool sockets in the help box. Slot positions, menus and saves are unchanged. Record: [machine-screens.md](docs/features/machine-screens.md).

### Added: The Howitzer Walker, playable
- Owner accepted the updated local playtest on October 13: aim without holding fire, no close-ground aim freeze, and hide the pilot model/equipment while mounted. Implementation and correction record: [Howitzer Walker](docs/features/howitzer-walker.md).
- **The owner's Howitzer Walker is in the game:** a tall, two-legged artillery walker in riveted olive plate with a big howitzer in an open cradle, made from a Diesel Walker beside the Armoured Walker (`SDS / PWP / R R`: two steel blocks, a dispenser, steel plates, the Diesel Walker and pistons). It walks, turns, climbs and refuels as the other walkers do.
- **Mouse aims; hold use to fire the howitzer:** on the pilot's own Range Finder mark, else on the block they look at. The whole walker turns to face it and the barrel elevates (15 degrees down to 60 up) to the arc that lands a Heavy Shell there, about 115 blocks at most. It fires once laid, every 4 seconds, a shell from the pilot's inventory; the blast is damage-only, as every gun's.
- **Jump** crouches, then springs about three blocks up. **Attack stomps:** a hop (or, in the air, a slam) whose landing hurts and throws everything within 3.5 blocks for 14 damage.
- **Its animations, drawn from the owner-reviewed clips:** the floaty, bouncy walk; the shot's recoil, rocking hull, cartoon star burst and rolling smoke puffs; and the jump's crouch, tuck and squashy landing, fitted to the real leap. Game tests and a screenshot scene. Record: [howitzer-walker.md](docs/features/howitzer-walker.md).
