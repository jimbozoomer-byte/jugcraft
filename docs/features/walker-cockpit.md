# The walker cockpit

Status: implemented (pending CI and review)
Proposal issue: none. On 2 November 2026 the owner asked to "fix how driving the mechas looks from the drivers perspective, improve aiming and telegraphing where the attacks are going to go to the pilot and the overall experience of all of them".
Owner: jimbozoomer-byte
Target milestone and tier: the walkers' own tiers (the Diesel Walker, batch 47; the Armoured Walker, batch 58; the Howitzer Walker). This adds no tier.
Primary specialty and supported player role: combat and mining; the walker pilot

## Player experience
This pass covers all three walkers a player pilots. The Raider Walker and the town guard's mecha are not piloted, so they are unchanged.

### The view from the seat
Before this pass, two of the three pilots looked out from inside their own hulls:

| Walker | Before | Now |
| --- | --- | --- |
| Diesel Walker | The eye sat clear in its open cockpit. | Unchanged. |
| Armoured Walker | The eye sat at 69 px, under the 72 px roof, so the first-person view was the inside of the hull. | The pilot stands up in the roof hatch (`SEAT`, tools/armoured_walker.py). The eye is at 78 px, a third of a block over the roof, clear in every direction. |
| Howitzer Walker | The eye sat at 77 px, inside the cab under the hatch ring (80 to 81.5 px). | The pilot sits in a commander's seat over the roof, right of the gun cradle (`SEAT`, tools/howitzer_walker.py). The eye is at 88 px and sees ahead past the barrel at every elevation; the gun is about 20 degrees to the left of the view. As before, the pilot is not drawn in third person. |

Ray traces through each model's exported quads checked these eyes. The script was a scratch analysis and is not kept.

- **No hands.** A pilot's hands are on the controls, so the first-person hands and held item are hidden in all three walkers. Before, only the Howitzer hid them.
- **The view turns with the hull.**
  - Turning the Diesel or Armoured Walker with left/right turns the pilot's view with it, as a turret sight would, so the crosshair stays where it was on the walker. The mouse aims from there.
  - The Howitzer Walker's view follows only while left/right is held. Its hull also turns by itself to lay the gun where the pilot looks, and following that would chase the look round.
- **Third person sits back past the hull:** 7 blocks for the Diesel Walker, 7.5 for the Armoured and 9 for the Howitzer. Vanilla's 4 blocks put the camera inside the hull. This uses the aircraft's camera mixin.

### Telegraphs: where the attack will go
The telegraphs are drawn in the world on the pilot's own client and show through blocks. Each is worked out once a client tick from the same calls the server uses, so what is shown is what happens.

- **The Armoured Walker's cannon.**
  - It now aims at **what the crosshair is on**, up to 96 blocks: from its muzzle, it is turned toward that point and raised for its fall (the low arc; `ArmouredWalker.cannonVelocity`).
  - Before, it flew straight along the look from the muzzle, three blocks below and two ahead of the eye, and fell short of the crosshair. With nothing in reach it still flies along the look.
  - It fires with no spread, so it flies exactly the arc that is drawn.
  - The pilot sees the shell's whole arc as beads, a ring where it lands as wide as its blast (5 blocks), and a small ring at the landing point.
- **The Howitzer Walker's gun.**
  - The arc is drawn from **where the barrel points now**, with a cross where the pilot looks.
  - As the gun lays, the arc swings onto the cross.
  - The colour shows its state: green when it is on target, amber while laying or reloading, red with no shells or no fuel.
- **Fist, ram and stomp.**
  - The Diesel Walker's fist and the Armoured Walker's ram show their strike box in front of the walker. The Howitzer's stomp shows a ring round its feet.
  - Every creature the strike would hit now is outlined in red.
  - The box or ring is bright when the attack is ready and dim while it swings back.
  - The box is the server's own (`DieselWalker.attackArea`), and the outlines are its own test (`attackReaches`).
- **The drill.** The block the Diesel Walker's drill would cut is edged in amber. A block too hard for it gets no edge.

### Gauges
Gauges sit above the hotbar, on the left:
- the walker's name;
- fuel and hull bars with percentages, turning amber, then red;
- the weapon line: "Reloading 40%", "Laying the gun", "On target", "Cannon ready" or "No heavy shells", with the shells carried (not in creative); for the Diesel Walker, whether the drill has a block in reach;
- whether the fist, ram or stomp is ready.

They replace the "Fuel: n%" message that was sent every two seconds and pushed the gun's messages out of the action bar (a known limit in the Howitzer Walker's record). The fuel and the hull's wear are synced from the server for this. F1 hides the gauges.

## Connections
- Existing input producer: the walkers themselves; Heavy Shells (artillery) for the guns; diesel and kerosene for fuel.
- Existing output consumer: none; it is the pilot's view of existing vehicles.
- Technology connection: the walkers.
- Magic connection: none.
- Reachable entry path: unchanged. The cockpit comes with the walker.
- Which connections are required vs optional; trade and solo routes: none new.
- How this specialty stays useful without mastering every other branch: unchanged.
- For infrastructure/cosmetics, supported systems and reason resource links do not apply: presentation and aiming only; no recipe, item or resource.

## Balance and automation
- **The cannon's aim is better.** It now lands where the pilot looks, in reach of 96 blocks and an arc of −30 to 70 degrees. Its damage, blast, speed, reload and shell cost are unchanged. The Howitzer Walker already aimed this way, and the Self-Propelled Howitzer and big guns have range finders and firing tables.
- The fist's box is unchanged: 2.2 blocks ahead, 1.6 each way (now named `PUNCH_REACH` and `PUNCH_SPREAD`). The ram's and stomp's reaches are unchanged.
- The telegraph shows blocks only: a shell that meets a creature on the way bursts there, as before.

## Multiplayer and persistence
- **Server-authoritative:** the server still aims, fires and strikes. The client only draws what the server's own calls give for its own pilot; nothing it shows is sent back.
- **New synced data:** the walker's fuel (an int) and wear (a float), set each server tick only when they change, and the Armoured Walker's last shot (for the reload gauge). None is saved; fuel is saved as before.
- **Aiming loads no chunk:** the look and the shell's flight stop at the first chunk that is not loaded.
- **Bounded work on the pilot's client:** at most 200 steps of flight, one look trace and one entity query a tick. Nothing runs for anyone who is not piloting.
- **Seats:** the new seats move only where a mounted pilot sits. No saved data changes, and a pilot dismounts as before.
- Not verified with two players or on a dedicated server.

## Dependencies and assets
- No dependencies, no new assets.
- The telegraphs draw with the blueprint ghost texture already used for building previews, through the existing `LinePreview` boxes.
- New lang keys: `hud.jugcraft.walker.*` (tools/mech.py). `message.jugcraft.walker.fuel` is retired.
- Code:
  - `walker/WalkerAim`, shared: the look trace and the shell's flight;
  - `DieselWalker`: the gauges' data, `attackArea`, `attackReaches`, `attackReady` and `cameraDistance`;
  - the Armoured and Howitzer Walkers' seats, aim and muzzles;
  - `client/WalkerCockpitClient`: the telegraphs and gauges;
  - `WalkerClient`: the view turning with the hull;
  - the existing mixins: `HowitzerPilotHandsMixin` now covers every walker, and `AircraftCameraMixin` now covers walkers too.

## Verification
- Planned in CI, server game tests `WalkerCockpitGameTests`:
  - `walkerPilotsLookOutOverTheirHulls`: the Armoured and Howitzer pilots' eyes are over their roofs, and the camera sits back past each hull;
  - `armouredCannonLandsWhereItsArcShows`: the telegraph's landing is on the crosshair 28 blocks ahead, the shell fired comes down where the arc does, and its burst hurts a pig by the crosshair;
  - `howitzerArcComesDownOnTheCrosshairOnceLaid`: once laid, the arc drawn from the barrel comes down within 2.5 blocks of the crosshair;
  - `theDrawnReachIsWhatIsStruck`: the fist's box takes in the pig in front and not the one behind, and strikes only the one in front; the stomp's ring takes in the pig beside it and not one six blocks off;
  - `theGaugesReadTheWalker`: the fuel gauge reads a quarter tank, and a hit shows as wear.
- Planned in CI, client game test `WalkerCockpitClientGameTests`, for each walker:
  - the view from the seat, with the telegraphs and gauges, and an arc landing for the two guns;
  - holding left turns the hull and the view together;
  - the third-person camera sits back past the hull;
  - the Howitzer's view stays put while the gun lays itself.
  - It takes screenshots of each.
- Existing walker tests (walking, drilling, punching, the cannon firing twice in 60 ticks, ramming, the Howitzer's aim, firing, mark, reload, leap and stomp) still cover their behaviour.
- Done locally: the walker audits in `check_mod_data.py` (the seats, the new constants and the eye over the Armoured roof); see the PR for the full list. Gradle cannot run here, so the compile and tests are left to CI.
- Not done: piloting by hand, two players, a dedicated server.

## World and event applicability
Not applicable: walkers are crafted and placed by players.

## Rollout and open questions
- The telegraphs show through walls, which is the point for an artillery pilot, but the owner may prefer them hidden behind blocks.
- The Howitzer pilot's view is from the commander's seat, right of the gun. A gunner's sight on the barrel itself could come later.
- No key toggles the telegraphs; F1 hides the gauges with the rest of the HUD.
