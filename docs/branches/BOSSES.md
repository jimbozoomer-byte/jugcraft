# Bosses branch (brainstorm)

Status: **a brainstorm; the first boss is being built.** On 10 October 2026 the owner chose to build a new boss "the same way" as Madame Tatterlace ([features/tatterlace.md](../features/tatterlace.md)); the Yeti King is first, to the plan below. On 4 October 2026 the owner asked for variant weapons, many of them "drops from bosses ill make later", themed around those bosses ("example: Yeti King"), and to "brainstorm bosses we could make". This page is that brainstorm. The bosses' weapons already exist: Arms VII ([features/arms-vii.md](../features/arms-vii.md)) adds two trophies for each of the first eight bosses below, and their loot tables are ready (`data/jugcraft/loot_table/bosses/<boss>.json`, one of the two per roll). Building a boss is a separate, reviewed feature each time. Everything here is a proposal for the owner to pick from, change or drop.

## Rules every boss follows

From [CONTENT_BRANCHES.md](../CONTENT_BRANCHES.md) ("Caves, dungeons and bosses" and "Rare loot"):
- **An arena with edges:** a lair, or a summoning that builds one. The boss does not wander out of it, and it resets when everyone leaves.
- **Readable attacks:** every big attack is telegraphed (a wind-up, a sound, a shadow on the ground) and can be dodged or blocked.
- **Recovery:** dying in the fight is not the end; gear can be fetched back, and the boss heals and resets rather than keeping its gains.
- **Multiplayer:** its health scales with the players in the arena; every player who fought gets a roll of its loot table, not just the last hit.
- **No griefing:** a summoned boss cannot be called inside a town's protection or another player's claim, and its attacks do not break player builds.
- **No farming loop:** a long respawn timer per lair (or a costly summoning item), so a trophy stays a trophy.
- **Optional:** no core progression needs a boss. Its trophies are side-grades with a boon, and each boss also drops something useful to non-fighters (a crafting material, a decoration, a trophy head).

## The first eight (trophies built)

| Boss | Home | Encounter idea | Trophies (boon) | Loot table |
|---|---|---|---|---|
| **The Yeti King** | Tundra, Ice Sheet, Frost Rift | A white ape twice a player's height, wearing an ice crown. It throws ice boulders, its ground slam freezes the floor slick, and its roar calls lesser yetis. A cave lair in a glacier. | Glacier Maul (maul), Rimeclaw (katar): **Frost** | `bosses/yeti_king` |
| **The Cinder Tyrant** | Volcano, Magma Fields, Ashfall Wastes | A salamander lord in obsidian armour, with magma in its cracks. It rains cinders, a lava wave runs from it, and its armour sheds when cooled with water. | Cinderbrand (greatsword), Magmaw (earthbreaker): **Ember** | `bosses/cinder_tyrant` |
| **The Mire Hag** | Bog, Quagmire, Sludge Mire, Hallowed Bog | A witch of the deep swamp who hides in the fog among illusions of herself. She throws poison brews and binds players with roots; strike the real one, which casts a shadow. | Hagthorn (scythe), Bogfang (kama): **Venom** | `bosses/mire_hag` |
| **The Crypt Lich** | The graveyard's crypts, Ghost Forest | A crowned skeleton sorcerer below a crypt. It raises the restless dead in waves, drains life with a soul beam, and hides its heart in a phylactery that must be broken first. | Soulreaver (moonblade): **Drain**; Gravewarden (executioner): **Wither** | `bosses/crypt_lich` |
| **The Iron Dreadnought** | The front line (trench works) | A walking war machine bigger than the landship, with steam vents and tesla coils. It charges in straight lines, its coils arc between players who stand close, and its weak vents glow before they open. | Dynamo Halberd (halberd), Piston Hammer (war hammer): **Shock** | `bosses/iron_dreadnought` |
| **The Alpha Werewolf** | Gloomweald and dark forests, on a full moon | The pack leader of the werewolves already in the world, twice their size. It leaps between trees, howls to weaken players and call the pack, and flees to heal at dawn. | Moonfang (sabre), Howler (twinblade): **Howl** | `bosses/werewolf_alpha` |
| **The Storm Roc** | Highland, Karst Pinnacles | A thunderbird nesting on the tallest pinnacle. It dives with lightning strikes and gusts that throw players off the peak; its nest's lightning rods can be turned against it. | Stormcaller (glaive), Galefeather (estoc): **Gale** | `bosses/storm_roc` |
| **The Abyssal Leviathan** | Ocean Trench | A sea serpent in the trench's depths. It drags boats under, its whirlpool pulls players in, and it is fought from a sunken temple's ledges. | Tidebreaker (war fork), Leviathan's Hook (bill): **Tide** | `bosses/abyssal_leviathan` |

Each boss's table is ready for its encounter to roll on death. Until the bosses exist, their trophies are in the creative Combat tab only.

## The Yeti King: the plan (being built)

Built the way the Witching Season's bosses were (Vesperine in [features/vesperine.md](../features/vesperine.md), Madame Tatterlace in [features/tatterlace.md](../features/tatterlace.md)), on their lair framework ([features/hollow-acre.md](../features/hollow-acre.md)): first his lair and the summoning that opens it, then the King himself and his loot, each its own pull request with tests and pictures. The numbers here are the plan's; each part's feature record says what changed in building it. Part 1, the hall and the horn: [features/glacier-hall.md](../features/glacier-hall.md). Part 2, the King and his loot: [features/yeti-king.md](../features/yeti-king.md).

*A white ape twice a player's height, his fur thick and frosted, a crown of blue ice on his brow, his breath a cloud of frost.* He rules the glacier from a throne of ice, and the mountains' cold is his to command.

### The Glacier Hall

A vast cavern in the heart of a glacier, walled and vaulted in blue and packed ice, lit from within where daylight soaks through the ice:
- **The arrival:** a ledge high in the south wall, where a snow-choked crevasse lets in. Beside it, Grey Mist hangs in an arch of blue ice: the way home.
- **The snow ramp:** packed snow curving down the wall from the ledge to the lake, half a block lower each block, walked both ways without jumping.
- **The arena:** a frozen lake 41 blocks across, its ice crusted with drift snow, ringed by four great **ice columns** that rise to the vault. Round each column the snow is trampled hard.
- **The throne:** on the north shore, a dais of blue ice steps and a throne hewn from the ice under an arch of two mammoth tusks, his frozen hoard about it.
- **The dens:** two caves in the east and west walls at the lake's level, where his kin wait.
- **Overhead:** giant icicles hang from the vault, and a crack high in it lets a shaft of pale light fall onto the lake.

The lair-only blocks (drift snow, glare ice, giant icicles, mammoth tusk, the frozen hoard) are fixtures like the other lairs': unbreakable, no items, no drops.

### The summoning: the Frost Horn

1. Craft a **Frost Horn**: a goat horn bound with two gold ingots, two leather and two snow blocks.
2. At night, in the Overworld, blow it standing on snow or ice.
3. Its call rolls over the snow and is answered by a roar. The snow at your feet splits and a whirl of white mist rises: you fall through, and land on the ledge in the Glacier Hall, frost on your skin. The horn is used up.
4. For 60 seconds the whirl stays, and anyone who uses it follows.
5. By day, off the snow, or outside the Overworld, the horn only says why, and nothing is used up.

He is not a Witching Season boss: the horn works all year, with no Halloween bonus.

### The fight

The Yeti King: 420 health (scaled for the party: half as much again for each player after the first, at most two and a half times), armour 10, immune to knockback, fall damage, freezing and webs. He waits slumped on his throne, and wakes when a player who can fight him steps onto the lake, or strikes him: he roars and leaps down onto the ice (2 s, unhurt meanwhile). He keeps to his hall: more than 30 blocks from the lake's centre, he bounds back.

**His rule: the snow is his.** His Ground Slam blasts the drift snow from a ring of the lake round where he lands, baring **glare ice**, slick as blue ice, for 12 seconds before the snow drifts back. On it you slide, and every blow carries you further. He never bares the trampled snow round an ice column, the foot of the snow ramp or his dais, so there is always firm footing somewhere.

**Phase 1, "The Hunt"** (full to half health):

| Attack | Read it by | What it does |
| --- | --- | --- |
| Maul Swipe | He rears back, both fists raised (0.6 s) | A two-fisted swipe across his front, 120°, 4 blocks: 12 damage and a knockback. Up close, his most frequent attack |
| Boulder Throw | He tears a block of ice out of the lake (0.8 s) | He hurls it in an arc at his foe; it shatters where it lands: 10 damage within 2.5 blocks |
| Ground Slam | He crouches low (0.8 s) | He leaps onto his foe: 14 damage within 3 blocks, a knockback within 6, and the snow blasted from a ring 7 blocks round him (his rule) |
| Frost Breath | He draws in a breath, frost at his jaws (1 s) | A cone of freezing breath, 6 blocks long, for 1.5 s: 2 damage each quarter-second and the frost (slowed, and freezing damage once frozen through) |
| Avalanche Charge | He drops to all fours and paws the snow (0.8 s) | He charges up to 16 blocks in a straight line: 12 damage and a heavy knockback to each player he passes. **Into an ice column, he stuns himself for 3 s and takes a third more damage meanwhile** |

**At half health, "The King's Roar":** he bounds back onto his dais and roars for 3 seconds, unhurt. A blizzard rises in the hall, chilling everyone near (the frost, for a moment), and his kin answer: two **Yeti Whelps** climb out of the dens. Then he leaps back down, and the fight changes.

**Phase 2, "The Blizzard"** (half health to a fifth): he keeps his Maul Swipe, Boulder Throw, Ground Slam and Avalanche Charge, and adds:

| Attack | Read it by | What it does |
| --- | --- | --- |
| Icicle Fall | He roars at the vault, and icicles' shadows spread on the snow round his foe (1 s) | Eight icicles fall one by one: 6 damage to a player within a block of where one lands |
| Glacial Spikes | He drives a fist into the ice, and cracks race towards his foe (0.8 s) | Spikes of ice burst up along the cracks, 12 blocks: 10 damage, thrown up |
| Kin Call | He beats his chest and howls (1 s) | Two Yeti Whelps climb out of the dens; at most four at a time |

**Yeti Whelps:** young yetis, 16 health, their blows 3 damage, quick. They go for the nearest player, drop nothing, and flee into the snow when he falls, resets or is gone. Whoever hurts one has taken part in his fight.

**Below a fifth, "The Fury of the Peaks":** his crown blazes and his eyes burn blue. Every cooldown (and his pause between attacks) is a third shorter, his slam bares twice the ring, and a charge that does not stun him is followed at once by another.

**Left alone** for 10 seconds, he climbs back to his throne, healed; the snow drifts back, his whelps flee, and who hurt him is forgotten.

### His loot (per participant)

A participant is a player who hurt him or his whelps and is within 64 blocks of the lake's centre when he falls.

| Loot | Chance |
| --- | --- |
| **Yeti Fur**, a pelt of thick white fur | 4 to 8, always |
| **A trophy:** the Glacier Maul or the Rimeclaw (his `bosses/yeti_king` table, one of the two) | 15%; certain on a player's first kill |
| **Yeti Mitten** | 25% |
| **The Yeti King's Crown** | 20% |
| 300 experience, shared out | always |
| The advancement **Abominable** | always |

- **Yeti Fur:** a cheaper Frost Horn (fur in place of the two leather and a gold ingot), his "part of what summons him again"; or two white wool.
- **Yeti Mitten:** held in the offhand, you never freeze: powder snow, his Frost Breath and the blizzard leave you warm.
- **The Yeti King's Crown:** a costume worn on the head, his crown of blue ice in small.
- **The trophies** are Arms VII's ([features/arms-vii.md](../features/arms-vii.md)), with their Frost boon; until now they were in the creative tab only.
- When he falls, Grey Mist opens in the middle of the lake, the way home.

### Rules kept

He follows the rules every boss follows (above): his hall is his arena and resets when everyone leaves; every big attack is read before it lands; dying there keeps everything as Grave Goods; his health scales with the party and every participant rolls his loot; nothing in his hall can be built or broken, and the only blocks he changes are his own lake's snow, which drifts back; every opening costs a Frost Horn. Nothing a tier needs comes only from him.

## More bosses to consider (no trophies yet)

- **The Pumpkin King** (Halloween nights): a giant scarecrow made of the harvest. It would be summoned with heirloom pumpkins in a bounded field, and it ties into the giant pumpkins, the scarecrows and the Headless Horseman already in the world. It must not be the only source of anything core, and what it drops must be kept after the season ends.
- **The Headless Horseman, as a boss fight:** it already exists as a Halloween-night encounter ([features/halloween-nights.md](../features/halloween-nights.md)). It could gain a lair and a trophy, such as a lantern-headed flail.
- **The Marrow Colossus** (Nether, Marrow Heap): a giant made of bones that sheds parts of itself as it is hurt. Bone-themed trophies would fit the bonecarved style.
- **The Chorus Sovereign** (End, Chorus Reef and Phantom Garden): a crowned shulker-like being that teleports with its arena. It suits levitation or void boons.
- **The Hollow Monarch** (the Pixel Hollows): a retro-game boss with glitching sprites and pixel projectiles. A good fit for the arcade's style.
- **The Kaiser's Zeppelin** (the dieselpunk tiers): an airship fought from a sky platform or the player's own zeppelin. Its weak points are its gondolas.
- **The Frost Wyrm** (Ice Sheet): if the Yeti King is built first, a dragon of the far north for the late game.
- **The Hive Matriarch** (deep caves): an insectoid queen in a hive dungeon, for the underground milestone in [ROADMAP.md](../ROADMAP.md).

## Next steps (the owner's choice)

1. Pick the first boss to build. The Yeti King is the owner's own example, and its biomes are built: he is being built first (above).
2. Write its feature record (template: [features/TEMPLATE.md](../features/TEMPLATE.md)) with the arena, attacks, scaling, reset and loot rules above.
3. Have its death roll `bosses/<boss>.json` for each player who fought, as the trophies expect.
