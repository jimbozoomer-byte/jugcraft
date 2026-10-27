# The Storm Roc

Status: implemented in source on branch `claude/storm-roc-boss` (stacked on part 1's `claude/storm-roc`, PR #493); not yet run in CI (its results and the client test's pictures will be added here when they are). Part 2 of the Storm Roc, the seventh of the first eight bosses in the [bosses plan](../branches/BOSSES.md#the-storm-roc-the-plan-being-built), built by [the boss playbook](../branches/BOSS_PLAYBOOK.md): the Roc himself, his Gale Fledglings and his loot, on the Crown of Thunder ([crown-of-thunder.md](crown-of-thunder.md)). It has not been played by hand, and the two-client dedicated-server playtest is still to do.
Proposal issue: none. On 26 October 2026 the owner chose the Storm Roc as the next boss to build ("Storm Roc boss"); part 1, his lair and summoning, came first.
Owner: @jimbozoomer-byte. AI-assisted implementation with Claude Code; the model is named in the commit trailers.

Target milestone and tier: Discovery tier, as his summoning is.
Primary specialty and supported player role: exploration and boss fights, for melee and ranged players alike: no flying gear is needed.

## Player experience

*An eagle as big as a cart, slate-grey with a storm-blue sheen, his flight feathers tipped white like lightning, a hooked beak of dark horn and eyes that burn electric blue.*

**Waking.** He perches in his nest on the crag north of the summit. He wakes when a player who can fight him steps onto the summit or strikes him: he rises screeching as lightning splits the sky and glides down to the summit (2.5 s, unhurt meanwhile). He has 400 health, half as much again for each player after the first, at most two and a half times; armour 8. Lightning, electric arcs and falls never harm him, and knockback hardly moves him. He keeps to the summit: more than 30 blocks from its middle, he is set back on it.

**His rule: the Storm Rods.** About every 16 seconds he dives (10 seconds after he wakes; every 14 in the Squall, every 12 in the Eye of the Storm). His dive clock runs only while he is on foot between attacks.
1. **A rod charges** (2 s): one of the four Storm Rods burns storm-blue and crackles, and the bar over the screen names it ("The north rod is charged"). Meanwhile he climbs over the summit's edge **behind that rod**, 8 blocks up.
2. **He marks his lane** (2 s): a line of sparks runs from him through the player he was fighting and on, following them. Then it holds still.
3. **He dives** down the lane: 12 damage, and a heavy shove aside, to everyone within 1.5 blocks of his path. He lands on the summit and fights on foot.
4. **If his lane passes within 2.5 blocks of the charged rod before it reaches the player,** the rod grounds him as he passes it: a bolt arcs from it into him, and he crashes onto the summit just past the rod. He lies stunned for 6 seconds, wings spread: every blow does half as much again, and he cannot fly. A rod he passes far from discharges harmlessly.

So the player he marks puts the charged rod between him and themselves while the lane follows them, and the others step out of the lane.

**The Gathering Storm** (full to half health):

| Attack | Read it by | What it does |
| --- | --- | --- |
| Wing Buffet | He rears back and spreads his wings (1.0 s) | A gust in a cone before him, 6 blocks deep and 50° either side: 7 damage, and thrown back. Step behind him or aside |
| Lightning Marks | He screeches; crackling blue rings on the summit, at least 6 blocks apart, the first under his foe and the rest under other players (1.5 s) | A bolt strikes each ring: 8 damage within 1.5 blocks, an electric arc (the Grounding Strap halves it). Step out of the ring |

**At half health, "The Squall Rises":** he climbs high and circles screaming (3 s, unhurt), and three Gale Fledglings leave the nest.

**The Squall** (half to a fifth) keeps every attack and adds:

| Attack | Read it by | What it does |
| --- | --- | --- |
| Wind Strips | His wings raised; white arrows on the summit floor in two strips 3 blocks wide, one through his foe, pointing the way the wind will blow (1.5 s) | For 4 seconds the wind blows along each strip, pushing everyone in it the way its arrows point, toward the summit's edge. A boulder upwind shelters. The arrows stay until the wind drops |
| Fledgling Call | He calls to the nest, head up (1 s) | Fledglings leave the nest until he has three again; at most every 30 seconds |

**Below a fifth, "The Eye of the Storm":** he dives every 12 seconds and lays five rings, not three. His tells stay as long as ever.

**His fledglings, the Gale Fledglings:** half-grown rocs, the size of a big dog, with 20 health. Each climbs out of the nest (1 s, unhurt), then darts after his foe, hangs over them and stoops to peck (3 damage) every 1.5 seconds. They drop nothing and give no experience.

**Left alone** for 10 seconds, he flies back to his nest, healed. His fledglings fly back, the rods go quiet, the wind drops, and who hurt him is forgotten.

**When he falls,** he crashes onto the summit with a last thunderclap, and the Grey Mist opens at the foot of his nest's crag, the way home. Each participant rolls his loot.

## Loot (per participant)

A participant is a player who hurt him or his fledglings, or stood by the rod that grounded him, and is within 64 blocks of the summit's middle when he falls.

| Loot | Chance |
| --- | --- |
| **Roc Feathers**, two or three | always |
| **A trophy:** the Stormcaller or the Galefeather (his `bosses/storm_roc` table, Arms VII's, with their Gale boon) | 15%; certain on a player's first kill |
| **The Weather-Vane Lens** | 25% |
| **The Roc's Crest** | 20% |
| 300 experience, shared out | always |
| The advancement **Eye of the Storm** | always |

- **Roc Feathers** are for those who never fight him: each makes four feathers, and two make the next Thunder Vane without its lightning rod (two Roc Feathers, three copper ingots and a compass). Neither recipe gains anything.
- **The Weather-Vane Lens,** held in the offhand in the Overworld, tells its holder the coming weather above the hotbar every 5 seconds, from the server's own weather clock: "fair weather for at least 2 minutes", "rain in about 5 minutes", "rain now, clearing in about 4 minutes", "thunder now". It changes nothing; lightning rods and the sky still do the same work.
- **The Roc's Crest:** a costume worn on the head, three swept-back plumes of slate feathers edged storm-blue. It counts for trick-or-treating and the costume contest.

## Changes from the plan
- **Where he dives from.** The plan left it open ("he climbs over the summit's edge"). He climbs over the edge **behind the charged rod**, never round from it. The audit walks the summit at sprinting pace: from straight behind the rod, the farthest any player must go to line it up is under 12 blocks against the 18.7 a sprint covers in the tell. From ten degrees round, a corner of the summit is already too far. It also makes the dive easy to read: the glowing rod shows where he will come from.
- **The Grey Mist he leaves** opens at the foot of his crag, on the summit's floor, not on the crag itself, which a player would have to jump up.
- **His Fledgling Call** is an attack of its own, with a 30-second cooldown, so his scream to the nest is a read like the rest. The plan had them "every 30 seconds while he has fewer than three".
- **The Wing Buffet's cone** is 50° either side; the plan gave only its depth.
- **His cry** is the owner's bald eagle's (below), pitched down for his size.

## Connections
- Existing input producer: part 1's Crown of Thunder and Thunder Vane; the shared lair framework.
- Existing output consumer: Arms VII's Stormcaller and Galefeather (their Gale boon), until now in the creative tab only; feathers for anything that takes them; the Thunder Vane again; the Witching Season's costume contest.
- Technology connection: the Iron Dreadnought's Grounding Strap halves his Lightning Marks, as it halves all electric harm.
- Magic connection: none needed. The planning pack's Tempest, Radiance and Strata links wait for those schools' spells.
- Reachable entry path (prove no circular unlock): the Thunder Vane is made from vanilla items. Nothing he drops is needed to reach him; the Roc Feathers' vane is only a cheaper way back.
- Which connections are required vs optional; trade and solo routes: none is required beyond the vane. His loot can be traded, and he can be fought alone or with up to `lairs.party_size` players.
- How this specialty stays useful without mastering every other branch: any weapon fights him; the rod rule and the grounding open him to melee.

## Balance and automation
- His numbers keep to the playbook's envelope: health 400, ×2.5 cap, armour 8, tells of 1.0 to 1.5 s for his attacks and 4 s for his dive (an arena action).
- A Thunder Vane is spent each time; his loot is rolled once per participant per kill.
- No conversion loop: a Roc Feather makes four feathers, and nothing makes Roc Feathers (the audit checks every recipe). Two Roc Feathers make a vane, and a vane makes nothing.

## Multiplayer and persistence
- **Server authority.** His phases, dives, the lane, grounding, attacks, fledglings and loot all run on the server; the lane's sparks, the rings and the wind's arrows are the server's particles.
- **His bar** shows the charged rod while it burns, and the Squall or the Eye once they come.
- **Who fights:** players who may fight a lair's boss (`LairBosses`: alive, not spectating, not creative).
- **Saving.** A passing moment is never resumed: loaded while waking or in flight, he comes down on the summit; while rising, the Squall begins. His dive and his fledglings are never saved, and every rod is placed calm with each instance.
- **Per tick:** his own fight, a few particles while he dives or attacks, and the wind's push once a tick on players in a strip. The lens looks at each player's offhand once every 5 seconds. Nothing loads a chunk or leaves its dimension (the audit checks).
- **Disable behavior:** with the lairs switched off the vane is refused, so he is never met; everything stays registered.

## Dependencies and assets
- No new dependency.
- New IDs:
  - the entities `jugcraft:storm_roc` and `jugcraft:gale_fledgling`;
  - the items `jugcraft:roc_feather`, `jugcraft:weather_vane_lens` and `jugcraft:rocs_crest`;
  - the recipes `jugcraft:feathers_from_roc_feather` and `jugcraft:thunder_vane_from_roc_feathers`;
  - the loot table `jugcraft:entities/storm_roc` and the advancement `jugcraft:eye_of_the_storm`.
- **His cry and his hurt cry** are the owner's bald eagle's sounds, from the supplied library (`art/owner-library/originals/Edited-2026-10-18-Part2/jims mobs/sounds/bald_eagle_*.ogg`). They are already in the mod as `jugcraft:beast.bald_eagle_idle` and `jugcraft:beast.bald_eagle_hurt` (the menagerie's copies), and he plays them pitched down. The owner supplied the library for Jugcraft and authorized its direct reuse ([PROVENANCE.md](../../art/owner-library/PROVENANCE.md)). The library's bald eagle texture belongs to a model that was not supplied, so his body is Jugcraft's own.
- His and his fledglings' GeckoLib bodies and clips are `tools/storm_roc_models.py`; their sheets are drawn by code in `tools/storm_roc_art.py`, as are the worn crest's feathers. The three icons come from their maps in `tools/item_icons/`, with two new palettes (`roc_slate` and `storm_light`, in `tools/icon_materials.py`). No Mojang file is read, traced or copied.

## Verification
- `python3 tools/check_mod_data.py`, `check_storm_roc`:
  - the Java's numbers and attacks are `tools/storm_roc.py`'s;
  - his fight can be played: the Squall before the Eye; dives no rarer as he weakens; no flight longer than 6 s; tells long enough to read; the Buffet reaches whoever it is used on; marks' rings never overlap;
  - his nest has room for his body; where he lands and where the Grey Mist opens are open summit floor; every place he dives from is open air within his leash;
  - **his rule is fair:** for each rod, walking the summit's open cells, from every one a sprinting player reaches a place that puts the charged rod between him and themselves within the tell;
  - the Java's lane, grounding, damage multiplier, immunities and arc damage are as the table and the audit take them;
  - registrations, names, renderers and glow layers; each GeckoLib body's model, clips, sheet size, UV regions and cut-out; no bone driven by both controllers;
  - the loot table is the table's, his trophies are Arms VII's, his recipes and costume tag are there, nothing makes Roc Feathers, and his advancement is granted from code;
  - his cry is the owner's eagle's, every message has its words, and nothing loads a chunk or leaves its dimension.
- Server game tests (`StormRocGameTests`):
  - he waits in his nest and a player on the summit wakes him;
  - his health scales with the party; lightning and arcs never harm him;
  - a charged rod between him and his foe grounds him, clear of its plinth, sparing the foe; grounded, a blow does half as much again; a bystander by the rod takes part; he rises after his stun;
  - a lane far from the rod does not ground him, strikes his foe, and the rod discharges;
  - the Buffet's cone, the marks' rings and the Grounding Strap's half, the wind's push and a boulder's shelter;
  - the Squall Rises with three fledglings (and no more); a fledgling pecks, and hurting one counts; the Eye comes below a fifth;
  - he keeps to the summit, and left alone flies back to his nest, healed;
  - his loot is each participant's own, a first kill brings a trophy, and the Grey Mist opens;
  - the lens reads the weather's clock; his feathers' recipes.
- Client game test (`StormRocClientGameTests`), on a real Crown: his fight from end to end, with pictures of him waiting, waking, his Buffet, his Marks, the Wind Strips, his fledglings, his lane behind the charged north rod, grounded, the Squall, and the summit after he falls. Then a fresh instance places him in his nest again.
- Not run: play by hand, a dedicated server, two clients, performance measurements.

### Results
- Local, on this branch:
  - `python3 tools/generate_material_data.py` and `python3 tools/generate_textures.py` ran clean. The texture generator's unrelated re-encodings were reverted.
  - `check_storm_roc` and `check_lairs`, run alone: PASS. The fairness walk first failed with dive points up to 50° round from the rod (a corner 30 blocks from any good place). That is why he now dives from straight behind it.
  - Breaking the Java on purpose failed `check_storm_roc` 6 times out of 6: an attack's wind-up, where he lands, the grounding's reach, the grounded multiplier, the lens's registration and the fledgling's glow layer. Each file was restored afterwards.
  - `python3 tools/check_icon_maps.py` on the three maps: PASS.
  - `javac`, syntax only, on the new and changed Java: no parse errors (the game compiles only in CI).

## World and event applicability

He lives only on the Crown of Thunder, his own dimension; nothing of his fight reaches the Overworld. The Thunder Vane opens his Crown in any storm, all year; no season gates him, and his loot is kept for good.

## Rollout and open questions

The shared framework's defaults hold: four players and eight instances, belongings kept on death in a lair, and the server's `lairs.boss_health` and `lairs.boss_damage` scaling.

For the owner:
- He always dives from behind the charged rod, which keeps the rule fair from anywhere on the summit. Varying it would need a longer tell or a wider grounding reach; the audit's walk will say which is enough.
- The Weather-Vane Lens reads the vanilla weather clock. A weather mod that runs its own clock would need an adapter.
