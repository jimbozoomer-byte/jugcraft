# Machine screens: five looks by tech level

Status: implemented (pending CI and review). Replaces the batch 22 screens (#78, see History).
Proposal issue: none. The owner, 14 October 2026: "refining user interfaces to match the images I have provided ... Make them more stylistic depending on the tech level of the machine and increase the simplicity and user experience so that its more intuitive." The owner attached five reference images:
- a cyan holographic heads-up display;
- a chunky blue pixel device with icon buttons and a status dot;
- a grey monitor with green text, a preview and two green buttons;
- a dark blueprint skill tree with coloured tabs;
- an orange-on-brown pixel device with an icon toolbar and a side gauge.

The owner approved the offline mockups and the machines' grouping ("Looks right, go ahead and build it").

Owner: jimbozoomer-byte
Target milestone and tier: every machine and every industrial form
Primary specialty and supported player role: every player who uses a machine

## Player experience

**One layout for every machine.** The screen is still 268 by 166.
- **The machine's body:**
  - a title strip with a **status lamp**: green while it works, a dim lamp when it is idle, red when something is wrong;
  - the **machine pane**, holding the energy bar, slots, tanks and progress arrow where they always were;
  - the **status pane**.
- **The inventory** sits on its own tray under the body.
- **Beside the inventory:** a row of **icon buttons**, and a **help box** that says in plain words what the machine is for.

**The status pane says what is happening and what to do about it:**
- One word: RUNNING, IDLE, NO POWER, OUTPUT FULL, GENERATING, CHARGING, DISCHARGING or STORING.
- When the machine is stuck, the fix:
  - "Connect a cable or a generator.";
  - "Take the results from OUT, or turn on Eject.";
  - "Put something to work on in IN.";
  - "Put fuel in its slot."
- While it works: a progress meter with its percentage, and the time left. The time comes from the pace over the last second, so speed upgrades and helpers are counted.
- Power stored, its rate, and the machine's own condition where it has one (formed, oil or dry, and so on).

**Controls are icons with tooltips:**
- Redstone mode: the icon turns red with its mode.
- Eject: a small lit dot when on.
- Sides.

**The side settings stay out of the way until wanted.** The Sides button turns the status pane into the machine's six faces: front in the middle, back to the right, top, bottom, left and right. Each face is coloured by what it does; click one to change it. The help box shows the colour key, and the button turns into a Back arrow.

**Empty slots explain themselves:**
- IN and OUT labels sit over a processor's slots where they fit.
- Upgrade slots and fuel slots show a faint hint icon while empty.
- Hovering an empty slot says what goes there ("Input: what the machine works on", "Upgrade: a speed or efficiency card").
- Hovering the energy bar or a tank still gives exact numbers.

**Industrial forms** use the same layout:
- the status pane gives the form's state and the one thing to fix, with progress, the time left, power, and batches when there are several;
- icon buttons pause or resume it, cancel its batches, and choose whether excess byproduct is vented;
- its tool sockets and upgrade slots sit in the help box. A form without them says there what it is for.

**Five looks by tech level.** The references set the mood and colours; everything is drawn fresh.

| Look | After | Machines |
| --- | --- | --- |
| Workshop | the orange device | the first machines: coal and steam generators, water wheel, wind turbine, crusher, electric furnace, alloy smelter, metal press, wire drawer, pulverizer, ore washer, sieve, sawmill, cobblestone generator, tree farm, crop harvester, auto-crafter, large steam engine |
| Industrial | the green-text monitor | steel and oil: every other machine (foundry, coke oven, arc furnace, drills, pumpjack, refinery and cracking, generators and engines, tanks and the rest), and every industrial form |
| Electric | the blue device | power gear: battery box, capacitor bank, solar panels, fuel cell, lithium battery bank, flow battery, advanced engine |
| Lab | the blueprint screen | chemistry: chemical reactor, electrolytic cell, air separation unit, synthesis converter, polymerization reactor |
| Advanced | the holographic display | electronics and nuclear fuel: circuit assembler, lithography station, gas centrifuge cascade, fuel fabricator |

How each look is drawn:
- **Workshop**: a warm brass casing with soft blotches, an orange-on-brown screen with a faint grid and a scan band, orange-rimmed keys.
- **Industrial**: a brushed steel bezel, green phosphor panes and green keys.
- **Electric**: a chunky blue device with cyan glass, side vents and a blue tray.
- **Lab**: a dark blueprint grid in a pale frame, outlined slots and coloured tabs for the controls (red redstone, green eject, blue sides).
- **Advanced**: dark teal with cyan hologram frames and cut corners.

## Connections

Every machine and form screen. No gameplay change.

## Balance and automation

None. The screen shows only what the server already syncs: the machine's data, its slots and its tanks. The rate, the time left and the hints are worked out on the client from those.

## Multiplayer and persistence

- Client-only drawing.
- The menus, slots, synced data and buttons' messages to the server are unchanged, so servers and saves are unaffected.
- The sides view is a client-only toggle.

## Dependencies and assets

- No new dependencies.
- `tools/gui_textures.py` draws the five backgrounds (`textures/gui/machine_<look>.png`, replacing `machine_diesel.png` and redrawing `machine_electric.png` and `machine_lab.png`). It also holds:
  - which look each machine uses, and the forms' look;
  - the looks' colours (`gui/machine_themes.json`);
  - the taglines for machines and forms;
  - the status words, hints and slot help, as lang keys.
- `client/MachineSkin` draws the moving parts and the buttons: slots, the energy bar, the arrow, the lamp, eleven 8×8 pixel icons, the icon buttons and the face buttons.
- The companion wheel (`local/peepo/client/WheelScreen`) borrows the coal generator's background, so it takes the workshop look.

## Verification

Local checks:
- `tools/check_mod_data.py`, `scripts/check_repository.py` and the script unit tests: see Results.
- Every form has a tagline. `gui_textures.write_all` fails on a form without one.
- An overlay of every slot, tank, tool socket, button and face position on the new backgrounds showed each inside its pane, with no clipping at a pane's edge.

Client game test (`JugcraftClientGameTests`):
- one screen per look: the crusher (workshop), the battery box (electric), the circuit assembler (advanced), the chemical reactor (lab) and the distillation tower (industrial, the screen with the most outputs);
- the crusher's sides view;
- the auto-crafter's screen.

The industrial forms' own client tests photograph their screens in the industrial look.

### Results

Local, without Gradle or a game:
- `python tools/generate_material_data.py` regenerated `gui/machine_themes.json` (the five looks; 18 workshop, 23 industrial, 8 electric, 5 lab and 4 advanced machines; forms industrial) and added 29 lang strings (hints, slot help, form taglines, the Sides button).
- `python tools/check_mod_data.py`: **PASS**, including the art check of every model.
- `python scripts/check_repository.py`: **PASS**.
- `python -m unittest discover -s scripts/tests`: **OK**.
- A tree-sitter syntax parse of the five changed Java files: 0 errors. This is not a compile.

CI: pending.

Not run:
- a human trying every machine's screen;
- other GUI scales;
- recipe viewers' (JEI/EMI) placement beside the screen.

## World and event applicability

Not applicable.

## Rollout and open questions

- The other custom screens (storage tank, rocket pad, reactor console, logic controller and the rest) keep their own looks for now. They could take the look of their tech level next.
- A machine's look can be changed by moving it between the sets in `tools/gui_textures.py`.

## History

Batch 22 (#78, 1 October 2026) gave every machine a 268-wide screen with three themes (dieselpunk amber, electric green, lab teal), a CRT terminal with the machine's tagline and readouts, side controls under it, and a decorative keyboard. This version keeps its slot positions, gauges and taglines. It replaces:
- the three themes, with five looks by tech level;
- the terminal and keyboard, with the status pane, icon buttons and help box;
- the always-shown side buttons, with the sides view.
