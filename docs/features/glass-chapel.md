# The Glass Chapel and the Glass Accord

Status: implemented in source on branch `claude/glass-abbot`; not yet run in CI (its results and the client test's pictures will be added here when they are). Part 1 of the Glass Abbot, the planning pack's first wholly new boss, in the [bosses plan](../branches/BOSSES.md#the-glass-abbot-the-plan-being-built), built by [the boss playbook](../branches/BOSS_PLAYBOOK.md): the Glass Chapel, a lair on the shared framework ([hollow-acre.md](hollow-acre.md)), and the Glass Accord that opens it. The Glass Abbot, his images and his loot are part 2, in their own pull request. It has not been played by hand, and the two-client dedicated-server playtest is still to do.
Proposal issue: none. On 27 October 2026 the owner chose the Glass Abbot as the next thing to build ("Glass Abbot"), from the planning pack's Chunk E ([chapter 09](../plans/concordance-expansion-2026-10-10/09-claude-delivery-chunks.md#e1-glass-abbot)), built the same two-part way, his lair and summoning first.
Owner: @jimbozoomer-byte. AI-assisted implementation with Claude Code; the model is named in the commit trailers.

Target milestone and tier: Discovery tier. The Accord takes glass, gold, amethyst and a spyglass, all made in the first days; finding sunlight through glass by day is the rest of the price. For one who understands the Concordance's First Light, the Lampwright's Bench makes it cheaper.
Primary specialty and supported player role: boss fights and the Arcane Concordance's Radiance: the players who read light. Anyone can open the chapel; the magic is a help, never a key.

## Player experience

### The Glass Accord

1. Make a **Glass Accord**, a disc of leaded glass in a gold rim round a spyglass's lens:
   - at a crafting table, from four glass in the corners, an amethyst shard above and below a spyglass in the middle, and a gold ingot either side of it;
   - or, for a player who understands First Light, at a **Lampwright's Bench** from a spyglass worked with a block of glass, for 8 Radiance (`concordance/working/glass_accord`).
2. Hold it up (use it) in the Overworld:
   - **by day**;
   - **in clear weather** (no rain, no thunder);
   - **in the sun**: full sky light where your head is, so no roof that keeps the sun out;
   - **under glass**: a block or pane of glass somewhere straight over you within 16 blocks, a skylight or a window roof (`#jugcraft:glass_accord_glass`: clear and stained glass and panes, and any mod's panes; not tinted glass, which keeps the light out).
3. The sunlight gathers in the Accord until it blazes, the Accord is used up, and the light takes you to the Glass Chapel's narthex.
4. **Grey Mist** rises where you stood, shot through with light, motes rising in it like dust in a sunbeam and a chime sounding in it now and then. For 60 seconds (`lairs.gate_seconds`) anyone who uses the mist follows, while there is room. Nobody is taken in by standing near it.
5. At night, in rain, out of the sun, with no glass over you, outside the Overworld, or with a mist already open within 3 blocks, the Accord only says why, and nothing is used up. The same happens if every chapel is taken. Every holding up, answered or not, rests the Accord for 2 seconds.

It works all year: the Glass Abbot is no Witching Season boss.

### The Glass Chapel

An abandoned chapel of pale stone, lit through tall windows, its pews long gone: a sky the colour of old parchment, a warm grey fog, a little ash drifting, and the music of the grove. North is the apse.
- **The narthex**, the arrival: a porch at the chapel's south end. Behind it, in the south door, the **Grey Mist**: the way home at any time. A low **rood screen** of stone bricks with a gap in its middle parts the narthex from the nave. The narthex is safe.
- **The nave**, the arena: 31 blocks wide and 29 long, floored with worn stone in patches (polished andesite, bricks, cracked and mossy bricks), under a ribbed barrel vault. North of it, in the **apse**, the **Abbot's Lectern**, a stone lectern with an open book of leaded glass on its desk, stands under the **rose window**; the Abbot will kneel before it (part 2).
- **The three Chapel Mirrors**: tall mirrors of silvered glass in brass frames on turntables, east, south and west of the nave's middle, 12 blocks from it. With the Abbot's place they make a diamond. A use of either block of a mirror turns it to its next angle, round its four; the turntable's pointer shows which way it faces. The floor round each mirror is inlaid with lines of copper pointing to the Abbot's place and the other two mirrors.
- **The four pillars**: polished columns in the nave's four corners, from the floor to the vault.
- **The lancets**: tall windows of stained glass (amber, rose, blue and green diamonds in lead) down both walls, a sill standing in from the wall under each, a lit candle on it.
- **Below**: the chapel is closed all round. A player who falls out of it some other way is caught by the mist and thrown back to the narthex for the lair's toll (4 damage). It is never a death and never a loss of belongings.

The lair-only blocks (the mirrors, the lectern, the lancet glass and the rose window) cannot be broken, have no item and drop nothing. The mirrors are the only things in the chapel a player can use. In this part the nave is empty: the Glass Abbot comes in part 2. An instance nobody is in closes after 30 seconds, as every lair's does.

### The mirrors and the Abbot's beam

The beam is part 2's, but the chapel is built for it, and the audit holds it to this now:
- The beam runs level over the floor. It leaves the Abbot's place aimed at the middle of one of the two mirrors he marks, and turns at each marked mirror's middle about the way its glass faces.
- An unmarked mirror is dark and stops it, and so does a mirror it has struck before: a mirror square to it would send it straight back the way it came. A pillar stops it; it fades at the walls and at the rood screen, so it never reaches the narthex.
- It **comes home** when it passes back within three quarters of a block of the Abbot's place after striking a mirror.
- Each mirror's four angles are steps of 22.5°: one for each of its two pairs, and two that send the beam into a wall. For each pair, both mirrors at that pair's angle send the beam home, aimed at either first. Of the 64 settings of the three mirrors, for each pair only those four do (the third mirror's angle is free), and the mirrors' resting angles send it home for no pair.
- From every open cell of the nave a player alone, sprinting, reaches both mirrors of any pair within 13 seconds: the plan's 10 seconds from the Abbot's marking to his gathering the sun, and the 3 he gathers it.

## Changes from the plan
- **Only the marked mirrors turn the beam.** The plan's beam "must strike two marked mirrors". Left to turn at every mirror, it found other ways home through the third, some needing only two turns of the wheel. A dark unmarked mirror keeps each pair's answer to the one setting the floor's lines point to.
- **A mirror struck twice stops the beam.** At some angles a mirror faces the beam square on and throws it straight back along its path, home through the first mirror again. That was a way home with one mirror at a wrong angle, so it is shut.
- **The rood screen stops the beam**, so the narthex stays safe whatever the mirrors do.
- **The nave** is 29 rows long and 31 wide, with the apse 7 more north of it: the plan's 36 blocks from the rood screen to the lectern.
- **The candles** stand on sills over everyone's head, not on the floor.
- **The Accord's glass** leaves out tinted glass, and the conventional glass-blocks tag, which counts it; a mod's panes still count.
- **Light.** Every lair's sky gives none, so the chapel is lit by hidden light blocks in a staggered lattice three blocks over the floor, and the candles. The least block light anywhere anyone can stand is 8.

## Connections
- Existing input producer: glass (sand and a furnace), gold ingots, amethyst shards (geodes), a spyglass (copper and amethyst); daylight and fair weather; any glass roof or skylight a player builds. The Concordance's Lampwright's Bench and First Light for the cheaper route.
- Existing output consumer: the shared lair framework (`lair/Lairs`, instances, keeping belongings on death, the mist gate, the lair rules). Part 2's Abbot, images and loot.
- Technology connection: glassmaking and the spyglass.
- Magic connection: the Concordance's Radiance. The Lampwright's Bench working (`glass_accord`, opened by understanding First Light alongside the lantern workings) makes the Accord from a spyglass and a block of glass for 8 Radiance, instead of four glass, two gold and two amethyst. Part 2's fight adds Radiance's preview of the beam and Strata's Brace.
- Reachable entry path (prove no circular unlock): every ingredient is a vanilla item made long before; daylight and glass are everywhere. Nothing in the chapel is needed to make the Accord.
- Which connections are required vs optional; trade and solo routes: none beyond the Accord; Accords can be traded; the chapel can be entered alone or with up to `lairs.party_size` players. The Concordance route is optional and only cheaper.
- How this specialty stays useful without mastering every other branch: the crafting table's route needs only vanilla items and the Overworld.

## Balance and automation
- An Accord is spent each time the chapel opens. A refused holding spends nothing and rests it for 2 seconds.
- Nothing is produced in this part: the chapel gives no loot yet, and nothing in it can be broken or carried out.
- No conversion loop: the Accord is consumed and makes nothing. Its essence value is its recipe's (`tools/build_alchemy_values.py`: four glass, two amethyst, two gold and a spyglass); the Lampwright route spends Radiance, which no exchange gives back.

## Multiplayer and persistence
- **Server authority.** The checks, the mist and the follow run on the server. In the chapel only the mirrors and the Grey Mist can be used (`LairRules`, `#jugcraft:lair_fixtures`). A mirror turns one angle a use, on the server, so two players turning one mirror at once turn it once each, in order.
- **Instances.** Placed fresh from the template each time one opens: every mirror at rest. Gates are never saved: a restart closes every instance, and anyone found inside a closed one is sent home.
- **Per tick:** nothing of the chapel's runs while nobody is in it. The Accord's mist sends a few particles a tick while it is open.
- **Disable behavior:** the lairs' switch (`agriculture`) off refuses the Accord; with the Concordance off the Lampwright's route is gone and the crafting table's stays; everything stays registered.

## Dependencies and assets
- No new dependency.
- New IDs:
  - the item `jugcraft:glass_accord`, its recipe and the Lampwright working `jugcraft:glass_accord`;
  - the dimension `jugcraft:glass_chapel` (its dimension type, level stem and biome) and the template `jugcraft:lair/glass_chapel`;
  - the lair-only blocks `jugcraft:chapel_mirror` (states `half` lower or upper, `mirror` east, south or west, `stop` 0 to 3, `marked`, `veiled`), `abbot_lectern` (`facing`), `lancet_glass` and `rose_glass`;
  - the block tag `jugcraft:glass_accord_glass`.
- Every texture is drawn by code (`tools/glass_chapel_textures.py`; the Accord's icon from its map, `tools/item_icons/glass_accord.txt`, a disc of blue leaded glass in a gold rim with an amethyst lens). No Mojang file is read, traced or copied. The layout and the beam's geometry are `tools/glass_chapel.py`.
- A mirror's model is turned to its angle by the blockstate's quarter turns and its boxes' own turn of 22.5° or 45°, so every angle is drawn with plain block models.

## Verification
- `python3 tools/check_mod_data.py`:
  - `check_lairs`: the `Lair.java` line from the layout; the Accord's rest, reach and glass (the tag is the table's, without tinted glass); the mirrors' halves and light; the fixtures' registrations, the mirrors' names and the gate's follow; the recipe; the dimension, its type, biome, template and words.
  - `check_glass_chapel`:
    - the Java's numbers, mirrors, stops, pillars and beam reach are the layout's, and its trace keeps the layout's rules (a dark mirror and a mirror struck twice stop it; it comes home only past the Abbot);
    - for each pair, each mirror has a stop for it and rests at none; every one of the 64 settings for each pair sends the beam home exactly when both marked mirrors stand at the pair's stops; every line between the Abbot's place and the mirrors keeps 2 blocks from the pillars and the third mirror;
    - the arrival stands in the narthex with room overhead, and the Grey Mist fills the south door; each mirror stands whole and at rest; the lectern stands before the Abbot's place, which has room for him;
    - a walk from the arrival reaches every open cell of the chapel;
    - a player alone reaches both mirrors of any pair, sprinting, from anywhere in the nave within 13 seconds;
    - block light flooded from the hidden lights, the candles and the mist is at least 8 on all 1,146 open cells;
    - nothing changes by itself once placed.
- Server game tests (`GlassChapelGameTests`):
  - the dimension type and biome load;
  - the template loads at the Lair's size, in the game's data version, with its blocks, and nothing in it that changes once placed;
  - the Accord's checks: answered by day in clear weather under a pane or stained glass; refused at night, in rain, with no glass over the holder or the glass too high, and, once the light settles, under a stone roof; tinted glass does not count;
  - when no instance can open the Accord is kept, nobody moves and no mist rises; held up from the hand it is rested;
  - the recipe;
  - a mirror turns round its four stops from either half, both halves alike, and with an empty hand's use; marked, its frame gives light; veiled; set back to rest; read from its place; the mirrors, the lectern and the glass cannot be broken and have no item, and only the mirrors are a fixture; the arrival is in the narthex, the mirrors in the nave and the Abbot's place in the apse, whose back corners are cut off;
  - the beam, worked out in Java: each mirror's stop for each pair is the layout's; all 64 settings for each pair come home exactly when they should (four each); the path runs the Abbot's place, both mirrors and home; an unmarked mirror stops it; at rest nothing comes home.
- Client game test (`GlassChapelClientGameTests`), in a real world on a stone pad built at 130 with a glass skylight over one spot and stone over glass over another:
  - at night, then in the rain, then by day with nothing over the holder and under the stone roof, the Accord is refused and kept;
  - held up by hand by day under the skylight it is used up and rested, opens an instance in the chapel's own dimension with its narthex, Grey Mist, three mirrors at rest and lectern, brings the player into the narthex unable to build, and raises a mist where it was held;
  - a mirror is a fixture and turns with an empty hand's use; the lectern is no fixture; a player in the nave is not thrown back; a fall out of the chapel ends in the narthex for the toll;
  - leaving takes the player back to where they stood; a second Accord at the open mist is refused and kept; the mist takes them back into the same chapel; closing it sends them home and the mist goes;
  - pictures: the mist on the pad, the narthex, the nave, the apse with the lectern under the rose window, the east mirror at rest, marked and veiled, the east and south mirrors set for their pair (the Java confirms they send the beam home), the lancets and the south door; then the game's own block light on every open cell of the chapel (1,146, at least 8).
- Not run: play by hand, a dedicated server, two clients, performance measurements.

### Results
- Local, on this branch (no Minecraft toolchain here, so neither the build nor the game tests ran locally; CI runs them):
  - `python3 tools/check_mod_data.py`: the whole audit ran; its one error, the chapel's biome missing from `tools/seasons.py` OTHER, is fixed, and `check_seasonal_biomes` and `check_glass_chapel` were rerun clean.
  - Deliberate breaks, each put back: a mirror's stop in `GlassChapel.Mirror`, `GlassChapel.BOUNCES`, `ChapelMirrorBlock.MARKED_LIGHT` and `GlassAccordRite.COOLDOWN`, each changed by one; the audit named every one.
  - `python3 scripts/check_repository.py` passed; `python3 -m unittest discover -s scripts/tests` passed (287 tests); `tools/check_icon_maps.py` passed for the Glass Accord's map; every changed Java file parses.
- `./gradlew build` and `./gradlew runClientGameTest`: CI's results go on the pull request.

## World and event applicability

The chapel is its own dimension, so nothing here changes the Overworld. The Accord works on any clear day, all year, under any glass in the Overworld; no season gates it.

## Rollout and open questions

The shared framework's defaults hold: four players and eight instances, belongings kept on death in a lair.

Part 2, the Glass Abbot (`glass-abbot.md`), adds the fight in the nave as the plan describes it: his mantle, which takes a quarter of every blow until his own beam, sent home by the mirrors, cracks it for eight seconds; the Beam, with two mirrors marked ten seconds ahead and three seconds of gathering the sun; the Shard Fan and the Prism Line; at half health the Veil, with Glass Images, turned mirrors and a veiled mirror; below a fifth the Breaking; Radiance's preview of the beam and Strata's Brace against his shards; and his loot, the Prism Shards, the Glass Reliquary (the Lantern Lens or the Mirror Inscription), the Abbot's Cowl and the advancement Seen Clearly.
