# Changelog

Every change to Jugcraft, newest first, grouped by pull request. For what each thing does in game, see [docs/TECH_TREE.md](docs/TECH_TREE.md). For a full inventory with APIs and file locations, see [docs/WHAT_EXISTS.md](docs/WHAT_EXISTS.md).

**Testing so far:** everything compiles in CI, and the in-game tests (from #14 on) pass on a headless server. Nothing has been play-tested in a client or with two players on a dedicated server yet.

## Unreleased

No numbered release yet. Entries on feature branches remain proposed until their pull requests merge.


### Unmerged: Madame Tatterlace (boss 2, part 2)
- **Madame Tatterlace**, a great spider seamstress, waits sewing on the white silk over the Spindle Loft's doily and lowers herself onto it when a player steps onto the lace. She has 360 health, scaled up for a party.
- **Her rule, the floor is her work:** she unravels rings and wedges of the doily, which drop away into the dark and are knitted back 12 seconds later; never the band round a spool or the tape's foot.
- **Phase 1, the Fitting:** Needlepoint, Thimble Toss, the Binding Thread (strike it to snap it), the Lace Snare and the Spool Roll.
- **At half health, Taking In the Seams:** she climbs into the threads and spits six egg sacs round the doily. Then come Pin Rain, Unravel, the Drop Strike (she lies open on the lace after it) and her Brood of spiderlings.
- **Below a fifth, Frenzied Stitching:** her cuffs glow red, her cooldowns are a third shorter and she unravels two segments at once.
- **Left alone for 10 seconds** she knits her doily whole and goes back to her sewing, healed.
- **Her loot is each participant's own:**
  - Gossamer Silk, which makes a cheaper Cursed Spindle or three string;
  - the **Needle Rapier**, an Arms VII trophy with the Stitch boon, certain on a first kill;
  - the Golden Thimble, which turns a projectile aside every 15 seconds from the offhand;
  - Tatterlace's Headdress, a costume;
  - shared experience and the advancement Unravelled.
- When she falls, her doily is whole again and Grey Mist opens in its middle.
- What every lair boss shares (who may fight one, party health, the damage and health settings) is now one class for both bosses. Animated with GeckoLib. Record: [tatterlace.md](docs/features/tatterlace.md).

### Unmerged: The Spindle Loft and the Cursed Spindle (boss 2, part 1)
- **The Cursed Spindle** (two gold ingots, an amethyst shard, two spider eyes, three string and a stick) opens the Spindle Loft:
  - use it on a **Spinning Wheel** at night in the Overworld. You prick your finger, fall asleep and wake on the loft's pincushion, blind for a moment. The spindle is used up;
  - for 60 seconds the wheel spins wild, and anyone who uses it with an empty hand follows;
  - by day, out of season, on a wheel already spinning or with every loft taken, it says why and is kept.
- **The Spindle Loft** is a colossal sewing room's attic seen at a spider's size:
  - a tomato pincushion stuck with pins taller than a house, where you arrive, with Grey Mist in a needle's eye beside you as the way home;
  - a measuring tape sloping down to a lace doily 41 blocks across, hung over darkness between four giant thread spools;
  - a thimble and the blades of a pair of shears beside the doily;
  - threads up to the rafters, a grimy skylight, and the Spider's Larder's cocoons and egg sacs.
- Twelve lair-only blocks: unbreakable, no items, no drops.
- The lair framework now takes a second lair: a lair with no moon, hooks run as an instance closes, and words for coming into each lair. Record: [spindle-loft.md](docs/features/spindle-loft.md).

### Unmerged: Vesperine, the Last Reaper (boss 1, part 2)
- **Vesperine** waits on the Bone Throne of every Hollow Acre and rises when a player steps into the Mown Circle. She has 400 health, scaled up for a party.
- **Her skulls, Dirge and Requiem**, halve every blow she takes while both live. They fire homing Grief Bolts that a player can strike back.
- **Phase 1:** the Reaping Arc, the Harvest Lunge, the Scythe Throw (unarmed, she takes a quarter more) and the Grave Call's thralls.
- **At half health, the Last Toll** turns the moon red and re-forms her skulls. Then come the Twin Beam, Crop Circles and the Shadow Step.
- **At a quarter, Death's Harvest:** souls stream from the black wheat to heal her until they are struck down or the four ward braziers are lit again, then she slams down.
- **Left alone for 10 seconds** she returns to her throne, healed.
- **Her loot is each participant's own:**
  - Reaper's Shade, which makes the Shade Wreath (a cheaper Mourning Wreath) and the Reaper's Hood;
  - the **Vesper Scythe**, an Arms VII trophy with the Harvest boon, certain on a first kill;
  - the Dirge and Requiem skull trophies, which glow and are worn as costumes;
  - the Reaper's Hood;
  - shared experience and the advancement The Last Harvest.
- When she falls, Grey Mist opens in the circle.
- Settings: `lairs.boss_health`, `lairs.boss_damage`, `lairs.event_loot`. Animated with GeckoLib, which was already pinned. Record: [vesperine.md](docs/features/vesperine.md).

### Unmerged: The Hollow Acre: the lairs and the Last Rites (boss 1, part 1)
- **The Last Rites** open the Hollow Acre:
  - at night, at any Jugcraft headstone in the Overworld, with four lit candles round it and a **Mourning Wreath** (four mourning flowers round a vine) laid on it;
  - ring the **Death Knell** (a gold ingot over an iron nugget over a bone) beside it.
  - The wreath is taken, and a gate of grey mist stands over the grave for 60 seconds: the ringer goes through, and anyone who uses the gate follows, four at most.
  - When a step is missing the knell says which, and nothing is used up.
- **The Hollow Acre**, a pocket dimension of its own: a floating island of black earth under an endless starless night and a vast harvest moon. Players arrive at the graveyard's Lych Gate, then cross a field of black wheat and weathered headstones to the Mown Circle, its arena ringed with soul braziers. At the north end stands a ruined bone chapel, with the Bone Throne at its open side. Vesperine comes in part 2.
- **The lairs' rules**, shared with the Spindle Loft to come:
  - every ritual places the lair fresh into its own instance;
  - nothing in a lair can be built or broken;
  - the lych gate's Grey Mist or `/jugcraft lair leave` takes a player back to exactly where they stood;
  - straying off the island costs 4 health, never below half a heart;
  - dying there keeps everything as **Grave Goods**, handed back on respawning;
  - empty instances close after 30 seconds, and a restart closes all of them.
- Settings: `lairs.instances`, `lairs.party_size`, `lairs.gate_seconds`, `lairs.off_season`. Record: [hollow-acre.md](docs/features/hollow-acre.md).
