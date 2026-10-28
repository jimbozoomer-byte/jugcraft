# The Church Bridge and the Hessian's Bridle

Status: implemented in source on branch `claude/horseman-lair`; **not yet through CI**. Nothing here has been compiled or run as a game test. The CI results and the client test's pictures will be added here when they come. This is part 1 of the Headless Horseman's lair, to the [plan in BOSSES.md](../branches/BOSSES.md#the-headless-horseman-in-his-lair-the-plan-being-built), built by [the boss playbook](../branches/BOSS_PLAYBOOK.md). It adds the Church Bridge, a lair on the shared framework ([hollow-acre.md](hollow-acre.md)), and the Hessian's Bridle that opens it. The Horseman's lair fight and his loot are part 2, in their own pull request. Until then the hollow stands empty. It has not been played by hand, and the two-client dedicated-server playtest is still to do.
Proposal issue: none. On 27 October 2026 the owner asked for the Headless Horseman to come at players in the Halloween season, and for "the lair which is where he will reside when its not longer halloween season". They chose a lair open out of the season only ("Off-season only").
Owner: @jimbozoomer-byte. AI-assisted implementation with Claude Code; the model is named in the commit trailers.

Target milestone and tier: Milestone 3 (first homestead), as Halloween nights are ([halloween-nights.md](halloween-nights.md)). The bridle takes leather, iron and a jack-o'-lantern, all made in the first days. The lantern on its post is built by hand.
Primary specialty and supported player role: boss fights, out of the season. The hollow is the off-season home of the Horseman, whom the season brings into the Overworld.

## Player experience

### The Hessian's Bridle

1. **Craft it:** leather, an iron ingot and a jack-o'-lantern in the shape of a bridle (`L L` / `IJI` / ` L `): three leather, two iron ingots and the lantern.
2. **Set a lantern on a post:** a jack-o'-lantern standing on a fence or a wall, out under the open sky. A lit hand-carved pumpkin counts as a jack-o'-lantern.
3. **Use the bridle on the lantern at night, out of the Halloween season, in the Overworld.** Hoofbeats come out of the dark, a horse screams, something cackles, and the lantern flares. "Hoofbeats come out of the dark, the lantern flares, and the mist rises round you." Grey Mist rises where you stand, and you come to on the wood road in the Church Bridge. The bridle is used up. The lantern and its post are left as they were.
4. **Others follow:** for 60 seconds (`lairs.gate_seconds`) anyone can walk into the mist to follow, while there is room in the party. Nobody is taken in by standing near it.

When a step is missing, the bridle only says what is wrong and nothing is used up:
- on anything but a lantern on a post: "Use the bridle on a jack-o'-lantern standing on a fence or wall post";
- outside the Overworld: "No rider answers the bridle here";
- by day: "He rides only by night: use the bridle after dark";
- **in the Halloween season**: "In the Halloween season he rides the world: call him at midnight with a lit scarecrow head". In the season he is summoned in the Overworld ([halloween-nights.md](halloween-nights.md)), and with sightings he rides past players;
- under a roof: "The rider cannot find a lantern under a roof";
- where a mist into the hollow is already open: "The mist has already risen here: use it to follow";
- when every instance is taken: "Every The Church Bridge is taken; try again soon".

Every use on a lantern on a post rests the bridle for 2 seconds, answered or not. `lairs.off_season` does not change any of this: that option keeps the Witching Season's lairs to the season, and this hollow is the other way round.

### The Church Bridge

A wooded hollow at night under a full moon, the old church to the north.
- **The wood road (the arrival):** you come to on a dirt road between old trees, facing north up it into the hollow. Grey Mist fills the road's end behind you: the way home.
- **The hollow, the arena:** a clearing about 41 blocks across. Its floor is podzol, moss and coarse and rooted earth in patches. The road runs north through it between split-rail fences with gaps in them, and two fallen trunks give low cover.
- **The Soul Braziers:** eight of the lairs' braziers stand in a ring round the hollow's middle, 13 blocks out, lit, none on the road. Each is named for its compass point, from the north-north-east one clockwise. They are fixtures: a dark one is lit again by using it. In part 2 the Horseman snuffs them ([hessian-horseman.md](hessian-horseman.md)).
- **The brook and the covered bridge:** a brook runs west to east across the hollow's north edge, its water flush with the ground and two deep. The road crosses it by a covered bridge with a spruce deck, plank walls with windows on stripped spruce posts, and a shingled dark oak roof.
- **The church and its yard:** across the brook stands the old church, with fieldstone walls, a steep dark oak roof and a white wooden belfry over its door with a bell hanging in it. The door and an arch beyond it lead into the church. Its churchyard runs down to the brook inside a low cobblestone wall, with rows of old stone markers in it.
- **The wood:** everything else is old dark oak trunks with a thicket of Moonwood Leaves between them (the Moonlit Ring's own), too thick to pass, and a canopy over the wood and the hollow's rim.
- **The sky:** deep blue-black, ash drifting in the air, a full moon low over the church.

In the hollow a player cannot build or break anything. They can use the braziers and the Grey Mist, and nothing else. Straying past the wood, or falling into the void below the hollow, throws a player back to the road for the mist's toll, as at every lair.

## Changes from the plan

- The hollow uses no blocks of its own. It is vanilla blocks and the lairs' own (the Soul Braziers, Moonwood Leaves, Grey Mist and the moon), so it adds no textures. The only new art is the bridle's icon.
- The churchyard's markers are vanilla wall posts (stone brick, mossy stone brick and andesite), not the graveyard pack's headstones or the gravestones. Those take random ticks and stir restless spirits, and nothing in a lair may change by itself.
- The fallen trunks lie clear of the braziers, at the hollow's north-west and south-east.

## Connections

- **Input producer:** leather (cows, trading), iron and a jack-o'-lantern (a carved pumpkin and a torch): all year.
- **Output consumer:** the Horseman's lair fight and his loot ([hessian-horseman.md](hessian-horseman.md)).
- **Required vs optional:** optional. No progression needs the hollow.
- **The season:** the hollow is the off-season way to meet the Horseman; in the season he is summoned in the Overworld. Neither is the only way to meet him, and the season gates nothing core.
- **Trade and solo routes:** the bridle is crafted alone; a party of up to `lairs.party_size` can follow through the mist.

## Balance and automation

- Every opening costs a Hessian's Bridle (three leather, two iron, a jack-o'-lantern). Nothing in the hollow can be taken home, and there is nothing to farm there yet.
- At most `lairs.instances` hollows (8) are open at once, as for every lair.

## Multiplayer and persistence

- **Server authority:** the server checks the lantern, its post, the hour, the season, the dimension and the sky, and places the hollow. The client only draws it.
- **Instances:** each opening places the template fresh in its own slot of the `jugcraft:church_bridge` dimension. An instance nobody has been in for 30 seconds closes, and instances close on a restart, sending anyone inside home, as at every lair.
- **Bounded work:** a use reads the lantern, its post and the column above it, and looks for open mists within 3 blocks. Nothing runs while nobody uses a bridle.
- **Still to do:** the two-client dedicated-server playtest.

## Dependencies and assets

No new dependency. New IDs (all under `jugcraft`):
- the item `hessians_bridle` and its recipe;
- the dimension, dimension type and biome `church_bridge`, and its template `lair/church_bridge`.

The bridle's icon is drawn from its map, `tools/item_icons/hessians_bridle.txt` (black lacquer straps and an iron bit), by `tools/church_bridge_textures.py`. No Mojang texture is read, traced or copied. The hollow is laid out by `tools/church_bridge.py`. The sounds (galloping, a skeleton horse, a witch's cackle) and particles are vanilla's.

## Verification

Run locally (27 October 2026):

| Check | Result |
| --- | --- |
| `python3 tools/check_mod_data.py`'s lairs checks (`check_lairs`, which now also runs `check_church_bridge`) | Pass |
| Deliberate breaks of the Java, the layout and the words, one at a time (below) | 14 of 14 failed it |
| `python3 tools/check_icon_maps.py tools/item_icons/hessians_bridle.txt` | Pass |
| `python3 tools/generate_material_data.py`, then `git status` | Writes this feature's template, dimension files, recipe, item model and words only (the files it rewrote for other features were put back) |
| `javac` on the new and changed Java and the test classes, for syntax only | No syntax errors |
| `./gradlew build`, game tests and client game tests | Not run (the Fabric Maven is out of reach here); to be run by CI |

The audit checks:
- that `Lair.java`'s line for the hollow (size, arrival, centre, bounds, floor and moon) and `ChurchBridge.java`'s numbers (the road, the bridge, the brook, the braziers in ring order, where the Horseman waits, the Grey Mist) are `tools/church_bridge.py`'s;
- that the bridle's rest and reach are `tools/lairs.py` `BRIDLE`'s, and that it refuses the season and the day, wants its lantern on a fence or wall post, and opens the Church Bridge;
- that the bridle is registered and in the creative tab, every brazier has its words, and the mist takes followers in by the bridle's way;
- that the arrival stands on the road with room overhead, and the Grey Mist fills the road's end;
- that every brazier stands lit in the hollow, off the road;
- that a walk from the arrival comes beside every brazier, crosses the bridge end to end and reaches the belfry's door and where the Horseman waits, which is on the road at the bridge's south foot, in the hollow;
- that the brook's water is all still and held in on every side;
- that nothing in the hollow changes by itself: no vanilla leaves, grass blocks, farmland or vines.

The 14 deliberate breaks:
- a brazier moved in the Java, and the arrival moved;
- the season check, the night check and the post check gone;
- the bridle's rest changed;
- the bridle unregistered, and the mist's follow gone;
- the braziers' ring turned onto the road;
- the brook let out at the template's edge;
- vanilla leaves in the wood;
- the bridge's deck taken away;
- a brazier's words missing;
- where the Horseman waits moved onto the bridge (layout and Java).

The game tests (`ChurchBridgeGameTests`):
1. the hollow's dimension type and biome load;
2. its template loads at the Lair's size, with its braziers, Moonwood Leaves, water, road, planks, bell, stairs and Grey Mist, and no vanilla leaves, vines or grass blocks;
3. the bridle's checks:
   - it is answered on a jack-o'-lantern on a fence at night out of the season under the open sky, and on a lit hand-carved pumpkin on a wall;
   - it is refused in the season, by day, under a roof (but not under a barrier), on a lantern on stone, and on an unlit carved or hand-carved pumpkin;
4. a bridle that can open no hollow is kept and moves nobody. In the season it is refused the same way. Used on stone it only says what it wants; used on the lantern it is rested;
5. three leather, two iron ingots and a jack-o'-lantern craft it;
6. the hollow's parts are where the layout says, and a brazier is found lit or dark by its place.

The client test (`ChurchBridgeClientGameTests`) runs the bridle end to end in a real world, at midnight with the season switched off:
- the bridle is refused in the season, by day, on a lantern on bare stone and under a roof;
- used on the lantern on its post, it opens the hollow fresh: the road under the arrival, the mist at its end, every brazier lit, water in the brook and the deck over it;
- the bridle is used up and rested, the lantern and post are kept, and the player comes to on the road, unable to build;
- a brazier gone dark is lit by using it;
- leaving takes the player home, a second bridle at the open mist is refused, and the mist takes them back in;
- closing sends them home and the mist goes.

It then takes the pictures `jugcraft_hessians_bridle_mist`, `jugcraft_church_bridge_arrival`, `_hollow`, `_brazier`, `_bridge`, `_church`, `_above` and `_exit`.

Not run: all of the above game and client tests (CI will run them), the two-client dedicated-server playtest, and play by hand.

## World and event applicability

The bridle works only out of the Halloween season (20 October to 3 November by default, `halloween.mode` and its dates), in the Overworld, at night. Its hollow is its own dimension. Nothing is placed in the Overworld but the Grey Mist, which goes when its instance closes.

## Rollout and open questions

- **Part 2:** the Horseman's lair fight (the Dark Road, his attacks, the bridge he never crosses) and his loot (the trophies, the Hessian's Spur and Tricorn): [hessian-horseman.md](hessian-horseman.md).
