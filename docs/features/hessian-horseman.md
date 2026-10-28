# The Headless Horseman in his lair

Status: implemented in source; **not yet through CI**. The Fabric Maven is out of reach here, so nothing of this part has been compiled or run as a game test (below). Part 2 of the Headless Horseman's lair in the [bosses plan](../branches/BOSSES.md#the-headless-horseman-in-his-lair-the-plan-being-built), built by [the boss playbook](../branches/BOSS_PLAYBOOK.md): the Horseman at the Church Bridge, his rule (the Dark Road), his attacks, his phantom riders and his loot, with his two Arms VII trophies and their boon. It is built on part 1, the hollow and the Hessian's Bridle that opens it ([church-bridge.md](church-bridge.md)), and on the lair framework ([hollow-acre.md](hollow-acre.md)). It has not been played by hand, and the two-client dedicated-server playtest is still to do.
Proposal issue: none. On 27 October 2026 the owner asked for "the lair which is where he will reside when its not longer halloween season", chose the lair for the off-season only, and gave permission for each Halloween piece to have its own branch and pull request ("Yes, all of them").
Owner: @jimbozoomer-byte

Target milestone and tier: Discovery tier, as the lair bosses are. He is reached only through the Hessian's Bridle (three leather, two iron ingots and a jack-o'-lantern; part 1), out of the Halloween season. Nothing a tier needs comes only from him.
Primary specialty and supported player role: adventuring. His loot also serves riders (the Hessian's Spur), decorators (the Horseman's Lantern) and costume wearers (trick-or-treating and the costume contest).

He is not the Headless Horseman summoned at midnight in the Halloween season ([halloween-nights.md](halloween-nights.md)), who rides the Overworld as he always has and keeps his own drops. This is the same rider at rest out of the season, fought as a lair boss. His id is `jugcraft:hessian_horseman`; both are called the Headless Horseman.

## Player experience

### He waits

The Headless Horseman waits mounted at the south foot of the covered bridge in every Church Bridge, placed with the hollow, facing south over it, his lantern dark. He is the Hessian trooper whose head was carried off by a cannonball:
- a big black horse with a blue sheen, its mane, forelock and tail long and ragged, red eyes in a plain black bridle;
- a red shabraque edged with gold braid under a black saddle, and a pumpkin in a rope net at each side of the saddle;
- on the pommel, the jack-o'-lantern he carries where his head should be;
- the trooper in a dark blue coat with red facings and turnbacks, white crossbelts, brass buttons and gold-fringed epaulettes, buff breeches and tall black riding boots with gilt spurs;
- a collar banded red, with nothing above it but embers;
- a black cloak streaming from his shoulders, a curved cavalry sabre in his right hand and an iron lantern in his left.

His body is 1.5 blocks wide and 3.1 high.

Waiting, he takes no harm. He wakes when a player who can fight him (anyone not in creative or spectator mode) steps off the wood road into the hollow (looked for every half-second), or strikes him. The road is safe: nobody on it wakes him, even where it runs through the hollow. Then:
- "His horse rears out of the dark, his lantern bursts into flame, and the Headless Horseman laughs": his horse rears for two and a half seconds, and nothing can hurt him meanwhile;
- his health is set for the party: 420, half as much again for each player after the first anywhere in his lair, at most two and a half times (1050 for four or more).

He has armour 8 and cannot be knocked back. Poison, Wither, fire, drowning and falls never harm him. He cannot be leashed or pushed. He trots at 0.24 blocks a tick (a little slower than a sprinting player), stops three blocks short of his foe, and his horse steps over the road's split rails and the fallen trunks. He fights players in the hollow, and throws at players on the bridge. He keeps to the hollow, and if he is ever more than 24 blocks from its middle, below its floor or on the bridge, he is set back at the bridge's foot.

**He never crosses the bridge.** His horse balks at it, as in the legend ("His horse balks at the bridge", said once every 10 seconds at most). With his foe on the bridge he trots to its foot and uses only what he throws: the Pumpkin Toss, and in the Headless Charge the Head Hurl.

### His rule: the Dark Road

Every 24 seconds (18 in the Headless Charge, 15 in the Last Ride) he wheels and lifts his lantern for 1.2 seconds, and the hollow's Soul Braziers nearest his foe go dark: two (three in the Last Ride), the nearest and on round the ring toward whichever of its neighbours is nearer the foe. "He rides the Dark Road: the north-north-east and the east-north-east braziers go dark. Light them again!" Any player lights a dark brazier by using it.
- **Hidden in the dark:** while four or more braziers are dark, every blow does half damage to him, and his bar says "The Headless Horseman: hidden in the dark". His tells stay as clear as ever.
- **Revealed:** if every brazier of the snuffing is lit again within 10 seconds, "Every brazier burns again: the Headless Horseman is revealed!" His lantern flares white, his horse rears (2 seconds, doing nothing), and for 8 seconds every blow does half as much again, however many braziers are dark. Each brazier lit counts on the lighter's screen ("Brazier lit: 1 of 2").
- Each player who lit a brazier of a snuffing that revealed him has taken part in the fight.

From every cell of the hollow, a sprinting player reaches both braziers of the snuffing he would make against them in a walk of at most 28 blocks (5 of the 10 seconds), and all three of a Last Ride snuffing in at most 39 (7 seconds): `check_hessian_horseman` walks the hollow cell by cell, never stepping more than half a block, and fails if a snuffing of two needs more than 60% of the time at a sprint, or one of three more than 85%.

### Phase 1, the Ride (full to half health)

| Attack | Read it by | What it does |
| --- | --- | --- |
| Sabre Pass | Hoofbeats quicken and he lowers his sabre along his line (1.0 s); crits mark the line | A gallop along a line from him through his foe's place, 16 blocks long (never off the hollow's floor or onto the bridge), in 1 s: 9 damage to each player he rides through (within 1.2 blocks of the line), thrown aside off it. Step out of the line. From 4 to 16 blocks; his most frequent attack |
| Pumpkin Toss | He lifts a flaming pumpkin from his saddle, and a ring of embers glows where his foe stood (1.0 s) | The pumpkin arcs onto the ring (0.6 s): 7 damage within 3 blocks of it, and set alight for 3 s. Step off the ring. From 5 to 20 blocks |
| Rearing Stomp | His horse rears, screaming (0.8 s) | 6 damage within 3.5 blocks. Step back |
| The Dark Road | He wheels and lifts his lantern (1.2 s) | Above |

### The Hollow Wakes (at half health)

"The Hollow Wakes: he lifts his lantern high, and every brazier gutters." He rides back to the bridge's foot (1.5 s) and lifts his lantern high (2 s), unhurt the while; every brazier smokes, and then two go dark, a snuffing like any other (lit again in time, it reveals him).

### Phase 2, the Headless Charge (half to a quarter)

He keeps every attack, snuffs braziers every 18 seconds, and adds:

| Attack | Read it by | What it does |
| --- | --- | --- |
| Head Hurl | He lifts the blazing jack-o'-lantern from his saddle and draws back (1.5 s); a pillar of flame and a ring mark where it will land | Thrown at anyone on the bridge first (the farthest of them), else the farthest player in the hollow, at most 30 blocks; it lands 0.8 s later: 12 damage within 2.5 blocks, and set alight for 4 s. Step off the mark |
| Phantom Riders | Blue fire runs along two lines across the hollow, crossing at right angles where his foe stands (1.5 s) | Two ghostly riders gallop down the lines, each a chord of the hollow, in 1.2 s: 6 damage to each player each rides through (within 1.2 blocks of its line). Step out of the lines. At any distance |

### The Last Ride (below a quarter)

"The Last Ride: the dark closes in faster." Three braziers go dark every 15 seconds. The braziers' 10 seconds and his tells stay as long as ever.

### His phantom riders

Ghosts of his troop: the same horse and trooper drawn in the blue of soul fire, glowing in the dark. They cannot be hurt or struck, drop nothing, give no experience and are never saved. At most two ride at once; each fades at its line's end, and all of them with him.

### His fall

"His lantern rolls from the saddle and goes out; his horse rears and fades into the mist." His riders fade, everything he threw burns out, every brazier burns again, and Grey Mist opens on the covered bridge over the brook (three blocks across its deck, three high): the way home. His lair's instance ends, and closes once everyone has left.

### Left alone

With no player who may fight him in the hollow or on the bridge for 10 seconds, "The Headless Horseman rides back to the bridge's foot; the braziers burn again, and his lantern goes dark": he waits there again at full health, his riders gone, and who hurt him is forgotten.

### His loot

Each participant rolls his loot for themselves (lair/LairRewards, as every lair boss's):

| Loot | Chance |
| --- | --- |
| **The Horseman's Lantern**, his old drop (a lantern block) | 1, always |
| **A trophy:** the Hollowflail or the Hessian Sabre, new to Arms VII (one of the two, his `bosses/hessian_horseman` table) | 15%; one is certain on a player's first win (whoever has not yet earned Laid to Rest) |
| **The Hessian's Spur** | 25% |
| **The Hessian's Tricorn** | 20% |
| 350 experience, shared out among the participants | always |
| The advancement **Laid to Rest** (a challenge in the Adventure tab): "Light the braziers he snuffs, and lay the Headless Horseman to rest by the Church Bridge" | always |

A participant is a player who hurt him (or whose tamed creature did), healed or warded others enough, or lit a brazier of a snuffing that revealed him (lair/Encounter), and is within 64 blocks of the hollow's middle when he falls. Anyone who died in the fight or left the game during it is paid once back. The loot goes straight into their inventory; what does not fit is dropped at their feet. His lair opens only out of the Halloween season, so no event candy is added.

- **The Hollowflail** (`jugcraft:hollowflail`, a flail): a dark wooden haft bound in brass with a black leather grip, an iron chain, and for its head a jack-o'-lantern with his fire in its face. Its head swings free in the hand, as every flail's does (the chain and the jack-o'-lantern are drawn live: a new "jack" head in `tools/arms_heads.py`).
- **The Hessian Sabre** (`jugcraft:hessian_sabre`, a sabre): a curved blade of blued steel with a dark fuller, a tarnished brass knuckle-bow, a grip of black bridle leather bound in brass wire, and an ember of his fire in the guard.
- **Dread** (their boon, new): a struck monster (a creature that hunts players), never a boss, is filled with dread (`jugcraft:dread`) for 4 seconds, and every blow on it from anything does a fifth more (counted after its armour). Each can be filled once every 8 seconds; at most 256 are remembered as lately filled. `#jugcraft:frightless` names the bosses it spares, as it does for Fright.
- **The Hessian's Spur** (`jugcraft:hessians_spur`): held in the offhand, the horse its holder rides gallops a fifth faster at night in the Overworld. The speed is a modifier on the horse, checked every half-second and taken off again when its rider dismounts, puts the spur away or day comes; it is never saved.
- **The Hessian's Tricorn** (`jugcraft:hessians_tricorn`): a black three-cornered hat edged with gold braid, worn on the head. A costume: it counts for trick-or-treating and the costume contest (`#jugcraft:trick_or_treat_costumes`, `#jugcraft:costume_hats`).

The Horseman's Lantern and the summoned Horseman's cloak still drop from the summoned Horseman in the season; the lair is the lantern's off-season route, and nothing core needs it.

### Settings

As at every lair: `lairs.boss_health` and `lairs.boss_damage` scale his health and his attacks' damage (0.25 to 4, 1 by default). Nothing of his has a setting of its own.

## Changes from the plan

- **The tells:** the plan's numbers are kept. The Sabre Pass's line is marked with crits during its wind-up, as the King's lines are marked with particles.
- **His riders' lines:** "two lines across the hollow" are two chords crossing at right angles where his foe stands, at a random bearing, so a player who stays still is in both and one who steps aside is in neither.
- **The Head Hurl's target:** "the farthest player, or anyone on the bridge first" is the farthest player on the bridge if any, else the farthest in the hollow; thrown at most 30 blocks (the mark is drawn toward a farther player at 30).
- **When revealed while hidden:** revealed wins: every blow does half as much again even if four braziers are still dark from earlier snuffings.
- **His leash:** 24 blocks from the hollow's middle, as the plan says, but he never stands on the bridge: one who ends up on it is set back at its foot.
- **The fall:** "his horse rears and fades into the mist" is a death clip (he rears, then horse and rider go down), and the usual smoke; models cannot fade.
- **Dread's damage:** vanilla has no attribute for the damage a creature takes, so a small mixin (`DreadDamageMixin`, on `LivingEntity.getDamageAfterMagicAbsorb`) adds the fifth; it reads the creature's own effects and nothing else.
- **The Hessian's Spur works in the Overworld only,** where there is a night.
- **The survival audit's item snapshot** (`docs/audits/survival-item-registry.json`) lists the four new items, and part 1's Hessian's Bridle, added by hand from their registrations (the exporting game test cannot run here).

## Connections

- **Inputs:** the Hessian's Bridle opens his hollow (part 1: leather, iron and a jack-o'-lantern, out of the season).
- **Outputs:** the Horseman's Lantern (decoration); two Arms VII trophies with a new boon; a riding aid (the spur); a costume.
- **Technology and magic:** none of his own; his trophies join Arms VII, whose other weapons need steel.
- **Ties to the Halloween content already in the world:** the summoned Headless Horseman of the season, whose lantern he drops out of it; the costume contest and trick-or-treating (the tricorn); and the flaming pumpkins (his own, drawn anew).

## Balance and automation

- No recipe makes the spur, the tricorn or the trophies, and nothing turns back into them: no loop.
- His experience (350) sits between the Pumpkin King's (300) and the later lair bosses'.
- Nothing of his can be automated: his loot is paid to players who took part, and his hollow is an instance.
- The Spur's fifth is about what a Speed I potion gives a horse, at night only, in the offhand (not a slot of its own).

## Multiplayer and persistence

- The server decides every hit, every snuffing, every relight, every reward and every block he changes.
- He is saved with his phase, his instance and his origin. His snuffing, his attacks, his riders and what he has thrown are not: loaded fighting, every brazier burns again and he fights on (a waking or a Hollow Wakes is not resumed: he goes on in the Ride or the Headless Charge).
- His riders and his pumpkins are never saved, and cannot outlive him.
- The Spur's modifier is transient: never saved, taken off when the server stops.
- Rewards are paid once per fight (lair/Encounter), as at every lair; players who died or left are paid when back.

## Dependencies and assets

- No new dependency. GeckoLib draws him, as it draws every lair boss.
- Every texture is drawn by code: `tools/hessian_horseman_art.py` (his sheet, his riders', the pumpkins', the tricorn's felt and braid) and the maps `tools/item_icons/hessians_spur.txt`, `hessians_tricorn.txt`, `tools/arms_icons/hollowflail.txt` and `hessian_sabre.txt`. No Mojang texture, nor any other, is read, traced or recoloured.
- New code: `lair/horseman/` (HessianHorsemanEntity, PhantomRiderEntity, HessianPumpkinEntity, HorsemanLoot, JugcraftHorseman), `weapons/DreadBoon.java`, `mixin/DreadDamageMixin.java`, `client/HessianHorsemanClient.java`; the numbers in `tools/hessian_horseman.py`, the bodies in `tools/hessian_horseman_models.py`, the data in `tools/hessian_horseman_data.py`.
- Changed: `lair/ChurchBridge.java` (the bridge's Grey Mist, the brazier run, the template's footprint), `tools/church_bridge.py` (`BRIDGE_EXIT`), `weapons/ArmVariants.java` and `ArmItem.java` (the trophies and Dread), `tools/arms_variants.py`, `tools/arms_variants_art.py`, `tools/arms_heads.py` (the jack-o'-lantern head), the generators' hooks.
- New ids: entities `hessian_horseman`, `phantom_rider`, `hessian_pumpkin`, `hessian_head`; items `hollowflail`, `hessian_sabre`, `hessians_spur`, `hessians_tricorn`; the effect `dread`; the advancement `laid_to_rest`. They must stay stable once released.

## Verification

| Check | Result |
| --- | --- |
| `python3 scripts/check_repository.py` | Pass |
| `python3 tools/check_mod_data.py`: now also checks him (`check_hessian_horseman`, below) | Pass (8030 material IDs, data files and recipe audit) |
| Deliberate breaks of his Java, the tables and the data, one at a time (below) | 19 of 19 failed it |
| `python3 -m unittest discover -s scripts/tests` | Pass, 287 tests |
| `python3 tools/check_icon_maps.py` on the two new item maps and the two trophy maps | Pass |
| `python3 tools/generate_material_data.py` and `tools/generate_textures.py`, then `git status` | Write this part's data and textures only (the files they rewrote for other features, with identical pixels, were put back) |
| `tools/geo_preview.py` on his body in 16 poses and on a phantom rider galloping | Looked at: the headless trooper reads, seated on the horse, the embers in his collar; the solid red collar first drawn read as a head and was lowered and banded instead |
| `javac` on the new and changed Java and the two test classes, for syntax only | No syntax errors |
| `./gradlew build`, game tests and client game tests | Not run (the Fabric Maven is out of reach here); to be run by CI |

**What `check_hessian_horseman` checks:** every number the Java uses is `tools/hessian_horseman.py`'s (his, the Dread boon's, the Spur's); his attacks are the table's, and what he can throw at a foe on the bridge; his rule can be played (the Hollow Wakes before the Last Ride; hidden bites less and revealed more; his snuffings come sooner as he weakens, each over before the next; his stagger shorter than his revealed time; one snuffing alone never hides him; each attack's reach is its range); the relight walk above; his clips last as long as the Java waits on them; his model stands as tall as his box; the bridge's Grey Mist is open deck in the layout, and where he waits has room for him; the braziers' lighting, his deeds, his exposure, the bridge he never rides onto and his riders' cap are wired as this record says; the Dread boon spares the bosses, is worked by ArmItem and added by its listed mixin; every entity registered, named and drawn, with its glow layer; his GeckoLib bodies' models, clips and sheets (UV regions inside and apart, sheets and glowmasks at four times, cut out) and no bone animated by two controllers; his loot table and his trophies (Arms VII's, with Dread); nothing makes his spur, tricorn or trophies; the costume tag, `#jugcraft:frightless` and his advancement; every message has its words; nothing of his loads a chunk or leaves its dimension.

**The deliberate breaks** (each put back after): his health; a wind-up; charging a foe on the bridge; no exposure to the dark or the light; revealed by one brazier of two; revealed too late; no deed for the lighters; his riders uncapped; a rider striking twice; a relight time too short to walk; his snuff tell longer than its clip; the Dread mixin unlisted; Dread on bosses; him missing from `#jugcraft:frightless`; a trophy chance; the Spur by day; the bridge's mist moved in Java only; a message's words missing; him never set waiting. Every one failed `check_hessian_horseman`.

**The tests, to be run by CI:**
- `HessianHorsemanGameTests` (12): he waits until a player steps off the road (and not before, nor for a creative player), with party health; the Dark Road snuffs the braziers nearest his foe and lighting them reveals him; late light does not, four dark hide him, revealed bites deeper; the Hollow Wakes and the Last Ride; the Sabre Pass; the Pumpkin Toss and the Rearing Stomp; he balks at the bridge and hurls his head at whoever is on it; the Phantom Riders; left alone he rides back; his loot is each player's own; the Dread boon; the Hessian's Spur.
- `HessianHorsemanClientGameTests`: his fight end to end in a real Church Bridge, with 12 pictures (waiting, waking, the Dark Road, the dark braziers, revealed, the Sabre Pass, the Pumpkin Toss, the Rearing Stomp, the Hollow Wakes, the Head Hurl, the Phantom Riders, his fall), and a fresh instance after.

## World and event applicability

His hollow opens only out of the Halloween season (part 1's rule). Everything he drops is kept for good. The Spur and the Dread boon work in every season.

## Rollout and open questions

- Not yet run: `./gradlew build`, the game tests and the client game test (CI); the two-client dedicated-server playtest; play by hand.
- The client test's pictures are to be looked at before they go in the record.
