# Pegasus

Status: integrated locally, compiled and runtime-tested. Original worker checkpoints below are historical; see the latest November 14 evidence and [combined expansion record](thanksgiving-expansion-nov14.md). No release or live changes.
Proposal issue: none (owner request)
Owner: the project owner
Target milestone and tier: an exploration mount, found wild, rarely, on high mountain meadows (see [Connections](#connections))
Primary specialty and supported player role: travel and exploration, any player who can tame a horse

AI attribution: designed and written with Anthropic's Claude in Claude Code (the exact model is declared in the commit
trailers); the owner's integrator supplied the 26.3 bytecode evidence for the vanilla horse model it builds on.

## Player experience

A white, winged horse, `jugcraft:pegasus`. It is a vanilla horse in every way it is tamed (ride it until it stops
bucking, or feed it), saddled (the vanilla saddle, from the horse screen or by using a saddle on it), owned, fed,
healed, led and saved, and it walks and jumps on the ground like one. Its saddled rider can also fly it. No research is
needed to fly it. Horse armour does not fit over its wings.

**Light pack.** Saddled and tamed, its owner can strap a small pack behind the saddle by using a chest on it, and opens
it by sneak-using it with a stick: nine slots, the Pegasus's "lighter cargo" (the Shire's saddlebags hold 27). The pack
rides on the saddle: it is drawn, and opens, only while the saddle is on; its contents are kept either way. Cargo
conservation (recall, knock-out, reload, open menus, smaller capacities) is the breeds' own:
[horse-breeds.md](horse-breeds.md#saddlebags-and-cargo-conservation).

### Controls

Two keys of their own, in **Options > Controls > Key Binds > Jugcraft: Pegasus**, read only while you pilot a Pegasus
(ride it as its saddled, controlling rider) and never while chat or any other screen is open:

| Key | Default | Does |
| --- | --- | --- |
| Pegasus: Ascend / Take Off | Space | take off; in flight, climb |
| Pegasus: Descend / Land | Left Ctrl | in flight, descend and land |

- **Takeoff trigger.** On the ground, tap or charge Jump (Space) and release it within a second: the normal vanilla horse
  jump. **Hold Ascend for one second** (20 ticks, longer than a full jump charge of 10) and the pegasus takes off. In the
  air already (in the middle of a jump, or falling), **hold Ascend again for three ticks** and it takes off at once.
- **In flight:** hold Ascend to climb, hold Descend to descend (more slowly within 2.5 blocks of the ground) and land,
  release both to fly level. Both together also hold level. WASD steers as on the ground; speeding up and slowing down
  are eased, never instant.
- **Shift** gets off, as from any horse. In the air you fall by vanilla's rules: land first.
- The vanilla jump bar and rearing are off while it flies, and Left Ctrl does not make you sprint while you pilot it.

The spawn egg's tooltip shows these controls with the keys bound now (hold Shift for the steps).

### Wings

The wings follow the server's synced flight state on every player's screen: folded along the flanks on the ground
(lifted off a rider's legs while ridden), raised and unfolded on takeoff, beating in powered flight, with faster,
stronger strokes to climb and calmer, shallower ones to descend, held out with barely a beat in a glide, and folded
again after landing. Vanilla's legs, head, tail, grazing and rearing animation are unchanged.

## Connections

- Existing input producer: vanilla horse taming and its foods (wheat, sugar, apples, hay, golden carrots and apples) and
  the vanilla saddle. **Survival source:** a wild Pegasus spawns alone on grass in mountain biomes (`minecraft:is_mountain`:
  meadows and cherry groves) at y 100 or higher, at weight 1 with one natural try in three kept
  (`JugcraftPegasus.highMeadow`); two tamed Pegasi breed more. It drops 0-2 leather like a horse. The spawn egg (creative
  Spawn Eggs tab) and `/summon jugcraft:pegasus` also make one.
- Existing output consumer: travel across every branch (survey, Pixel Hollows caves, towns, lairs' approaches). Ownership
  is the vanilla horse's, so Jugcraft's pet rules (`PetBehaviors.owned`, Pets storage, the supplement flute, menagerie
  hunting and ward checks) treat a tamed Pegasus as a tamed horse already.
- Technology connection / magic connection: none in this slice; a survival source is the owner's choice (below).
- Reachable entry path: find and tame a wild one in the mountains; no research, season or recipe.
- Required vs optional: optional; solo play needs only a saddle once a survival source exists.
- How this specialty stays useful: a mount for the explorer, not a requirement for any branch.

## Balance and automation

Fixed attributes (not rolled like a wild horse's): 30 health, movement speed 0.3, jump strength 0.7. In blocks a tick:
flight top speed 0.6 forward (12 m/s), 0.45 gliding, accelerating at most 0.04 and slowing at most 0.03 a tick; climb
0.30, takeoff climb 0.42, descend 0.35, landing descent 0.15, glide sink 0.12; vertical easing at most 0.05 a tick.
All the numbers are in `pegasus/PegasusFlight.java`. No fuel, stamina or cooldown, and no item conversion of any kind,
so there is no duplication or conversion cycle. Breeding: a Pegasus pairs only with a Pegasus and their foal is a Pegasus
with the same fixed stats (the one rule by which a foal flies; [horse-breeds.md](horse-breeds.md#crossbreeding)); a
vanilla horse, donkey or ground breed never has a foal with it.

## Multiplayer and persistence

**Who decides what.** The server owns the flight state (`GROUNDED`, `TAKEOFF`, `FLYING`, `GLIDING`), synced to every
player in the entity's own data with the climb or descent intent, and `NoGravity` follows it. As for every vanilla horse
a player rides, the pilot's client simulates the movement and the server checks it with vanilla's vehicle move checks
(too far in one move, moving into blocks). This change does not take that movement away from the client. What the server
adds:

- It changes the flight state only from the pilot's validated input and from its own copy of the pegasus (on the ground,
  in water or lava, under a ceiling or at the build limit). The client flies only while the synced state says so.
- The input packet (`jugcraft:pegasus_input`: entity id, two key bits, a sequence number; three var-ints) is accepted
  only from the entity's current controlling passenger, for the entity it names, with no bits but the two keys and a
  newer sequence than the last; anything else is dropped (`JugcraftPegasus.receive`, `Pegasus.acceptInput`). Input
  expires 10 ticks after the last good packet (the client resends every 5): stale input glides. Mounting, dismounting,
  death and loading clear it.
- `NoGravity` is set only while the server's state flies. On a dedicated server without `allow-flight`, vanilla's
  floating-vehicle check (as we understand 26.3's `ServerGamePacketListenerImpl`; not verified here) therefore still
  disconnects a client that keeps a pegasus aloft while the server says it is grounded.
- Every 20 ticks of piloted flight the server checks the pegasus moved no faster than 0.6 blocks a tick across and 0.42
  up, with three times the room for lag (teleports excluded). A flight beyond that throws the rider off (the
  Broomstick's rule).

That is the whole of the server's authority: a modified client can still fly as fast as those limits and vanilla's
checks allow, as it can ride any horse.

**Safety.** Climbing stops under a ceiling with the rider's head clear, and at the top of the build limit, for the
pegasus and its rider both; vanilla collision keeps it out of blocks; it will not fly out of the world border. It loads
no chunks and bypasses no protection: mounting goes through Fabric's `UseEntityCallback` (Jugcraft's land claims and
towns ask there) and flying changes no blocks. No fall damage while it flies or within 10 ticks of landing; on the
ground it falls as any horse (no blanket invulnerability). A rider who gets off in the air falls by vanilla's rules (no
Jugcraft rule softens it); the pegasus glides down by itself and lands unhurt.

**Saving.** The pegasus saves as a horse (UUID, health, owner, tameness, saddle) plus one boolean, `PegasusAirborne`,
and its light pack (`JugcraftBagsFitted`, `JugcraftSaddlebags`).
Powered flight and input never survive a reload: one saved in the air comes back gliding (so it does not drop), one
saved on the ground comes back grounded. Unloading mid-glide is safe the same way. Removing the feature would need a
migration for saved `jugcraft:pegasus` entities, as for any Jugcraft entity.

Two-client dedicated-server play has not been tested.

## Dependencies and assets

No new dependency. Fabric API (networking, key mappings, renderers) and MixinExtras (one `@WrapOperation`) are already
on the classpath.

- Model: vanilla 26.3's horse rig, built at run time by `AbstractEquineModel.createBodyMesh(CubeDeformation.NONE)`,
  with two wings of 18 cuboids each (shoulder, elbow and hand bones; leading edge, coverts and broad flight feathers),
  and its own saddle layer (blanket, seat, billets, girth, breast collar, bridle and reins) that keeps clear of the
  wing roots in every pose. The adult layer is scaled once
  in Java by `MeshTransformer.scaling(1.1f)`, as Codex reported `ModelLayers.HORSE` is; the editable model is not.
- Editable sources: [art/pegasus](../../art/pegasus/README.md) (`pegasus.bbmodel`, `pegasus_saddle.bbmodel`,
  `pegasus_animation.json`), with their conversion. `tools/check_pegasus_model.py` checks the Java against them.
- Textures (`textures/entity/pegasus/pegasus.png` 128x64, `pegasus_saddle.png` 64x64, `textures/item/pegasus_spawn_egg.png`
  16x16): original, painted by script at one texel a model pixel; no Mojang or owner-library pixels.

## Verification

Results of this change, honestly: **nothing was compiled against Minecraft and nothing was run in the game.** The
environment had only Java 21 and no Minecraft or Fabric jars.

Ran:
- `PegasusFlight` with its test `PegasusFlightTests` (pure Java, no Minecraft classes), compiled and run with a local
  JDK 21 `javac`/`java` outside Gradle: 772 assertions passed. The project's Gradle task `pegasusFlightTest` (Java 25)
  was not run.
- `python3 tools/check_pegasus_model.py`: the Java model, saddle and animation constants match the `.bbmodel` files and
  animation data (0 problems), and it reports deliberately broken copies.
- The art tools' `.bbmodel` validator (JSON, UVs inside the texture, no zero-size cubes except the two reins,
  unique bones, every cube in a bone, the 12 vanilla bones and parents): pass for both files.
- A tree-sitter parse of every Java file changed (syntax only).

Written, not run:
- `PegasusGameTests` (server): bounded input and who may send it (malformed, stale, replayed, another entity, a player
  not riding, a rider who does not control); a short hold staying a horse, takeoff, climb, level flight, descent and a
  landing without damage; a ceiling and the build limit; a mid-air dismount gliding safely; stale input gliding and
  recovering; the impossible-climb check; save and reload (identity, health, owner, tameness, saddle, flight reset);
  breeding and armour; registration.
- `PegasusClientGameTests` (rendered client): the keys and their category and defaults; Space still pressing vanilla
  Jump; takeoff, climb, level flight and landing flown with real keys; no input with chat open; no sprint on Descend;
  the client's copy of the entity following the synced state and its wings opening and folding; screenshots
  `jugcraft_pegasus_*`.
- `pegasusFlightTest` (Gradle `check`).

Not covered: two clients on a dedicated server, a laggy connection, controllers, GUI scale, the look in game.

## World and event applicability

No natural spawning, structures, loot or seasons in this slice. Ownership, protection and pet behaviour are vanilla
horse rules as Jugcraft already extends them. Dimensions: it flies wherever a horse can be ridden, up to each
dimension's build limit.

## Rollout and open questions

- **Survival source:** a rare natural spawn on high mountain meadows (Thanksgiving batch 2); the biome tag, height
  and rarity are the owner's to tune.
- API names this change uses that no existing Jugcraft code or supplied bytecode confirms are listed for review:
  `AbstractHorse.canJump()`, `EntityType.Builder.passengerAttachments(float...)`, `MeshTransformer.scaling(float)` and
  its package with `LayerDefinition.apply`, `InputConstants.KEY_SPACE`/`KEY_LCONTROL`, and `KeyboardInput.tick` reading
  `KeyMapping.isDown()` (the sprint guard).
- If 26.3's key dispatch gives a key to only one mapping, the Space default would compete with vanilla Jump; the client
  test checks that Jump still works, and `PegasusClient.held` reads a shared key from either mapping.
- Known look: vanilla's riding pose puts the rider's thighs through the wing roots (about a pixel) when riding.
- Decisions to confirm: reload in the air comes back gliding; Pegasus-only breeding; the light pack; armour off; the rider falls normally after a
  mid-air dismount; the takeoff hold length; the baby (commands only) is the adult rig at 0.55 scale.
# November 14 local integration checkpoint

OpenAI Codex integrated the seven Claude delivery patches into the existing dirty
checkout, preserving the local client registration and language entries.
`compileJava compileClientJava compileGametestJava pegasusFlightTest` passed on
the pinned Java 25 / Minecraft 26.3 setup; the pure flight suite passed 773
assertions. The model checker passed for 52 horse cubes and 16 saddle cubes.

The first actual server GameTest run failed five of nine tests. Investigation
against the pinned Minecraft bytecode found a missing vanilla entity tag:
`Mob.isSaddled()` requires the saddle's Equippable allowlist to include the
Pegasus. A stack in the saddle slot alone was not enough. Codex added the
non-replacing `minecraft:can_equip_saddle` tag, generator ownership and a runtime
regression assertion. The corrected run is pending. Flight, ceiling, persistence
and client rendering are not yet accepted based only on compilation or pure
arithmetic. Evidence is under `build/nov14-review/`.


## November 14 runtime results

After the saddle-tag repair, all nine server tests passed in pegasus-r8.log.
The client test passed using actual key input for takeoff, climb, level flight,
chat suppression, Ctrl descent and landing. Pure flight tests: 773 assertions.
Other repaired test defects: Runnable identity reuse discarded intermediate
input callbacks, a landing success predicate could pass before takeoff, and
the default empty arena had a barrier ceiling. Tests now use the open-sky arena.
These results supersede the earlier written-not-run and five-failure checkpoints.
A separate observer client, survival source and visual acceptance remain open.

The nine flight cases passed again in the final 118-test combined cohort.
The key/flight/landing client passed again with the six-suite integration run;
its saddled, climbing, level and landed screenshots are preserved in
`build/nov14-review/client-r1/screenshots/0016_*` through `0019_*`. Codex
inspected the riding/flight captures. Survival acquisition and a separate
observer-client check remain open.


## November 14 expansion validation

Survival biome spawning, breeding restrictions and the light cargo pack are implemented and covered in the final combined 440-test server cohort. The actual pilot client passes flight/key/chat/landing and pack checks. Two independent client JVMs also pass saddle, pack, rider and grounded/airborne wing replication; Codex inspected the final second-player ground and airborne screenshots as well as all five ground breeds. These results close the earlier survival-source and observer gaps. The build-limit test now schedules its same 80 high-altitude input ticks up front, avoiding mutation during the native test scheduler's iteration; flight assertions and timeouts were not relaxed. No fitted horse armour, flight stamina or hybrid winged offspring are claimed.
