# Moving pirate patrols — local integration

Owner-authorized November priority, after the existing equipment, mineral-processing,
garden-pact and harvest work. This record describes the agreed contract; it does
records local implementation and verification, not a shipped update.

## Responsibilities

- Claude coordinator: moving `PirateShip`, saved patrol/boarding state, bounded
  existing-ocean encounter spawning, finite loot and renderer integration.
- Claude B: `CrewedVessel` and `PirateCrew`, real plunderer passengers, boarding,
  finite ammunition and damage-only broadside fire.
- Claude C: repository-native ship quads, rig, textures and art checks.
- Codex: local integration, clearance/claim review, combined tests and rendered
  review. The draft clearance helper has been superseded by the integrated runtime
  `PirateWaters`; reviewed vessel, crew, renderer and resource code is present,
  with combined server and rendered verification passed locally November 9.

## Shared ship contract

Origin is the waterline; local +x is port, +z bow, +y up. Hull is 6 blocks wide
and 16 long, deck y=1, main mast reaches y=12. The ten stations are helm, lookout,
four broadside gunners and four deck crew. Seats and muzzles rotate by negative
entity yaw, exactly once. Vanilla passenger persistence saves the crew with the
ship; off-seat boarding crew retain their ship UUID and station.

The renderer, physical deck and stations must agree. Players must be able to
board a solid deck. A moving model without working collision/crew is incomplete.
Patrols stay within 64 blocks of their saved anchor, stop without a helmsman and
heave to during boarding. Existing static galleons and cannon-boat IDs remain.

Navigation must reject unloaded terrain before reading blocks, shallow water,
bridges, block entities, town/OP protection and claimed land. It must not force
chunks, break blocks or target villagers/townsfolk. Natural encounters use
bounded loaded-ocean attempts and persistent region/identity accounting, so
unloading a ship cannot cause its duplicate. Creative/spectator players do not
trigger an encounter. Loot and gunner supplies are finite and persisted; ending
boarding, unloading or restarting never refills them.

## Required validation before calling this complete

- Real vessel movement, turning and loaded/shore/claim boundaries.
- Actual passenger positions after a 90-degree turn and after save/reload.
- Boarding onto the physical deck, crew melee, broadside arcs and friendly fire.
- Helm loss, withdrawal, defeat and once-only loot across reloads.
- Natural encounter caps and persistent identity across unload/reload.
- Rendered hull, sails, guns, crew, rudder and culling at several viewpoints.

No live server, client updater, owner world or personal launcher profile changes
are authorized by this local work.


## Local verification outcome — November 9

All 3,718 combined server cases passed, followed by 35/35 pirate cases after the final anchor-save correction. The final actual-ship rendered client rerun passed at yaw 0 and 90; screenshot evidence is in `build/nov9-final-pirate-review/screenshots`. Twenty pirate/feast resource-layout checks and the full model/data audit passed. See [the combined ledger](nov7-priorities.md#current-completion--november-9-local-batch) for precise logs and limitations. Balance and a populated multiplayer ocean encounter remain owner playtest work; nothing was deployed.
