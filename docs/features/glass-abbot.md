# The Glass Abbot

Status: implemented in source on branch `claude/glass-abbot-boss`, stacked on part 1 (`claude/glass-abbot`); not yet run in CI (its results and the client test's pictures will be added here when they are). Part 2 of the Glass Abbot, the planning pack's first wholly new boss, in the [bosses plan](../branches/BOSSES.md#the-glass-abbot-the-plan-being-built), built by [the boss playbook](../branches/BOSS_PLAYBOOK.md): the Abbot himself, his Glass Images, his rule and his loot, in the Glass Chapel of part 1 ([glass-chapel.md](glass-chapel.md)). It has not been played by hand, and the two-client dedicated-server playtest is still to do.
Proposal issue: none. On 27 October 2026 the owner chose the Glass Abbot as the next thing to build ("Glass Abbot"), from the planning pack's Chunk E ([chapter 09](../plans/concordance-expansion-2026-10-10/09-claude-delivery-chunks.md#e1-glass-abbot)), and then asked to "keep going with the Abbot himself".
Owner: @jimbozoomer-byte. AI-assisted implementation with Claude Code; the model is named in the commit trailers.

Target milestone and tier: Discovery tier, as his chapel and the Glass Accord are. His fight asks for reading and teamwork more than gear: the mirrors do most of the work.
Primary specialty and supported player role: boss fights and the Arcane Concordance's Radiance. Anyone can beat him with ordinary weapons and a use of a mirror; the magic only helps.

## Player experience

### Waking him

The Glass Abbot kneels at his lectern in the apse, praying, under the rose window: a tall faceless figure in a monk's habit of grey wool, a mantle of leaded glass panes over his shoulders, and a round rose window glowing where his face should be. Nothing harms him while he kneels.

He wakes when a player who may fight him (`LairBosses`: not in creative or spectator) steps into the nave or the apse, or strikes him. He rises as the rose window floods with light, unhurt, for 2.5 seconds. His health is 340, half as much again for each player near the chapel after the first, at most two and a half times; his armour is 6. Falls and drowning never harm him, and knockback hardly moves him. He keeps to the nave: more than 26 blocks from the chapel's middle he is set back at his place. He keeps about six blocks from his foe: a caster, not a brawler.

### His mantle

While his glass mantle is whole, blows and shots do a quarter of their damage; a chime and a glint of glass say so, and now and then a line over the hotbar ("His glass mantle turns the blow aside").

### His rule: the Beam

Eight seconds after he wakes, and then every 20 seconds (18 in Compline, 15 in the Breaking):
1. **He marks two of the three mirrors.** Their brass frames burn gold and give light, a bell rings, and everyone near the chapel is told which ("Light gathers in the east mirror and the south mirror: turn them to send his light home").
2. **Ten seconds later he goes back to his place** before the lectern, kneels and gathers the sun for three seconds: light pours down from the rose window onto him.
3. **The beam** leaves his mantle aimed at one of the two marked mirrors and goes where the mirrors send it, worked out on the server from their angles alone (`GlassChapel.trace`, part 1's geometry):
   - **Sent home** off both marked mirrors, his mantle cracks: for 8 seconds he kneels dazed, and every blow does its whole damage and a quarter more. The beam's path shines gold for a second.
   - **Lost,** it burns along its whole path for a second: 6 damage once, and two seconds of fire, to anyone within a block of it. His mantle holds.
4. Either way the marks go out.

Each marked mirror has one angle that serves its pair; the floor's copper lines point the way. Whoever turned a marked mirror into place for a beam that came home has taken part in his fight, though they never struck him.

### His attacks

Every attack has a wind-up to read:

| Attack | Read it by | What it does |
| --- | --- | --- |
| Shard Fan | He raises his right hand and light glints over it (0.8 s) | Three glass shards in a fan at his foe, 15° apart: 6 damage and a push to whoever one strikes. A pillar or a mirror stops them. Strata's Brace halves the push |
| Prism Line | A line of gold is drawn on the floor from him toward his foe, 18 blocks long (1.0 s) | A lance of light down the line: 8 damage to anyone on it. Step off the line |
| False Images (from the Veil) | He crosses his arms over his mantle (1.5 s) | His Glass Images stand up round him, four blocks off, and he steps in among them |

### The Veil, Compline and the Breaking

- **At half health, the Veil:** he folds his hands and the windows dim, unhurt, for 2 seconds; then two Glass Images stand up and he steps in among them. The bar reads "The Glass Abbot: Compline".
- **Compline** (half to a fifth): his beam comes every 18 seconds; as he marks his mirrors, one of the two is veiled for 4 seconds (its glass clouds over; its pointer and the floor's lines still show where it faces); every 12 seconds he turns a mirror one angle himself, after it shudders and chimes for 1.5 seconds, but never in the last five seconds before he gathers the sun; and he calls his images again when they are gone.
- **Below a fifth, the Breaking:** his beam comes every 15 seconds and his images are three. The time from his marking to his beam stays 13 seconds.

### His Glass Images

His likeness in glass: his model, his sheet, his name. Each plays what he plays, steps as he steps and faces his foe from where it stands, so a wind-up is read on every one of them alike. Only he casts the rose window's tracery in shade on the floor under him. An image never hurts anyone; one blow of anything shatters it (whoever struck it has taken part), and it fades by itself after 10 seconds, and with him.

### Left alone, and his fall

Left alone for 10 seconds (no player who may fight him near), he goes back to his lectern and kneels, healed: his images fade, the mirrors turn back to rest, and who took part is forgotten.

When he falls his mantle shatters into light, his images fade, the mirrors turn back to rest, and the Grey Mist opens in the apse behind his lectern: the way home. His instance ends and closes once everyone has left.

### His loot (per participant)

A participant is anyone who hurt him or one of his images, turned a marked mirror into place for a beam that came home, or healed or warded another player in his fight enough (`lair/Encounter`), and is alive near the chapel when he falls; anyone who died in the fight or left the game during it is paid when they are back (`lair/LairRewards`). Each rolls `jugcraft:entities/glass_abbot` for themselves, straight into their inventory:

| Loot | Chance |
| --- | --- |
| **Prism Shards**, two or three | always |
| **The Glass Reliquary** | 15%; certain on a player's first win |
| **The Abbot's Cowl** | 20% |
| 300 experience, shared out | always |
| The advancement **Seen Clearly** | always |

- **Prism Shards:** one shard in four glass makes four **Abbey Glass**, a stained glass in rose and amber panes round a gold heart that glows faintly (light 4) and, like any glass, lets the sun through to a Glass Accord. Two shards and a spyglass make the next Glass Accord.
- **The Glass Reliquary,** his first-win choice: crafted with a glass pane it becomes the **Lantern Lens**; with an amethyst shard, the **Mirror Inscription**. Neither turns back.
  - **The Lantern Lens,** held in the offhand: once a second it outlines in light, for its holder alone, every invisible creature within 12 blocks, and the true Abbot among his images. It sets no glow on anyone and changes nothing.
  - **The Mirror Inscription,** held in the offhand by a player a ward protects (who has absorption hearts, from a Strata ward, a golden apple or anything else): the next arrow or other shot that would strike them for 8 or less is spent, and its shooter takes what it would have done, as magic, if the holder may harm them (never a party member, nor a player where players may not fight). Then it rests 30 seconds, its cooldown showing on it.
- **The Abbot's Cowl:** a costume worn on the head, his deep grey hood. It counts for trick-or-treating and the costume contest.

## Changes from the plan
- **His marked mirrors are named in chat,** to everyone near the chapel, not on the boss bar, which keeps his name and phase ("The Glass Abbot: Compline", "The Glass Abbot: the Breaking").
- **A lost beam** strikes each player on its path once, for its 6 damage, and sets them alight for two seconds.
- **Mirrors he turns himself** clear any player's turn of that mirror for the beam under way: a deed is the last turn into place.
- **The Mirror Inscription** turns back any projectile of 8 damage or less, not only arrows: the plan's "small projectile".
- **The Glass Accord's essence value** falls to its cheaper recipe's: two Prism Shards and a spyglass (259), as `tools/build_alchemy_values.py` takes the cheapest recipe.

## Connections
- Existing input producer: part 1's Glass Accord and chapel; ordinary weapons; the Concordance's support (healing, wards, Strata's Brace) and Radiance.
- Existing output consumer: the Glass Accord (two Prism Shards and a spyglass), Abbey Glass (a decorative glass that also serves the Accord), the Lantern Lens (seeing invisible creatures) and the Mirror Inscription (a defensive offhand that rewards a ward), the costume contest and trick-or-treating (the Cowl).
- Technology connection: glassmaking and the spyglass; Abbey Glass for builders.
- Magic connection: the Concordance.
  - **Radiance:** while his mirrors are marked, a player who understands First Light sees, every half second, the path the beam would take with the mirrors as they stand, traced in rose from mirror to mirror. Only they see it.
  - **Strata:** Brace halves a shard's push.
  - **Tolerance:** he joins `#jugcraft:concordance/resistant`, as his plan says, so harmful control reaches him at half strength.
  - **Support:** healing or warding another player in his fight earns a participant's share; the Mirror Inscription works only under a ward.
- Reachable entry path (prove no circular unlock): the Glass Accord's crafting-table recipe needs only vanilla items (part 1); Prism Shards make the next Accord more cheaply but never gate it.
- Which connections are required vs optional; trade and solo routes: none beyond an Accord. His loot can be traded. A player alone can win: from anywhere in the nave both marked mirrors are within reach in time (part 1's fairness audit).
- How this specialty stays useful without mastering every other branch: every part of the fight is read and done with ordinary play.

## Balance and automation
- Prism Shards come only from him: no recipe makes them. Abbey Glass (one shard and four glass for four) and the Accord from shards are recipes that gain nothing.
- Essence values (`data/jugcraft/alchemy/values.json`, by `tools/build_alchemy_values.py`'s rules): the Prism Shard, the Glass Reliquary and the Abbot's Cowl are at the fallback (1, balance pending), like every boss's drops; Abbey Glass 5/4 (a shard and four glass for four); the Lantern Lens 11/8 and the Mirror Inscription 33 from their recipes; the Glass Accord 259, its cheaper recipe's. Nothing breaks back into anything worth more.
- Per participant and per fight: rewards are rolled once, when paid. His experience (300) is shared among everyone paid.

## Multiplayer and persistence
- **Server authority.** His rule, the beam's path, every blow and every reward are decided on the server. A mirror turns one angle a use, on the server, in order. The beam's preview is sent only to the players who understand First Light, and the Lantern Lens's outlines only to its holder.
- **Saved:** his instance, his place, his phase. His beam is never saved: loaded mid-fight he stands at his place and fights on, his mirrors back to rest, and keeps a fresh record of who takes part. His images are never saved.
- **Per tick:** his own tick moves at most three shards and draws at most one beam's path; the Lantern Lens looks round each holder once a second; nothing loads a chunk or leaves his dimension.
- **Disable behavior:** the lairs' switch off refuses the Accord, so he is never met; with the Concordance off there is no preview, and the rest of his fight is the same. Everything stays registered.

## Dependencies and assets
- No new dependency. GeckoLib draws him and his images (approved, [FRAMEWORKS.md](../FRAMEWORKS.md)).
- New IDs:
  - the entities `jugcraft:glass_abbot` and `jugcraft:glass_image`;
  - the items `jugcraft:prism_shard`, `glass_reliquary`, `lantern_lens`, `mirror_inscription` and `abbots_cowl`;
  - the block and item `jugcraft:abbey_glass`, with its loot table;
  - the loot table `jugcraft:entities/glass_abbot`, the recipes `abbey_glass`, `glass_accord_from_prism_shards`, `lantern_lens` and `mirror_inscription`, and the advancement `jugcraft:seen_clearly`.
- Changed: the Glass Accord's glass (`#jugcraft:glass_accord_glass`, `tools/lairs.py` ACCORD_GLASS) takes Abbey Glass; `#jugcraft:frightless` and `#jugcraft:concordance/resistant` take him; the Chapel Mirror tells listeners of a player's turn (`ChapelMirrorBlock.onTurned`).
- Every texture is drawn by code (`tools/glass_abbot_art.py`, in Vesperine's clean style; the icons from their maps in `tools/item_icons/`). His model and clips are `tools/glass_abbot_models.py`; the numbers are `tools/glass_abbot.py`. No Mojang file is read, traced or copied.

## Verification
- `python3 tools/check_mod_data.py` (`check_glass_abbot`):
  - every number the Java uses is `tools/glass_abbot.py`'s, and his attacks are its table's;
  - his fight can be played: the Veil before the Breaking; his beam comes no less often as the fight goes on, with the marking, the gathering and his rest fitting between beams; a lone player has the marking and the gathering (13 seconds) to reach both marked mirrors (part 1's fairness walk); the veil lifts and his own turns end before he gathers; his mantle turns most of a blow and lets more through cracked; his shards reach the Shard Fan's farthest foe within their life, his line reaches whoever it is drawn at, and he keeps near enough for both;
  - his beam is the chapel's trace, comes home only off both marked mirrors, and its path is shown ahead only to those who understand First Light, each alone;
  - he kneels at the chapel's place with room for him, every cell of the nave and the apse lies within his leash, and the Grey Mist he leaves stands on open apse floor three high;
  - he is unhurt while he wakes and while the Veil falls; a beam home is a deed for whoever turned the mirrors; his images take one blow, shatter and fade with him;
  - every entity is registered, named and drawn; his GeckoLib body has its model, every clip the Java names and its sheet, its UV regions inside the sheet without overlapping, its sheet and glowmask four times the model's size and cut out;
  - his loot table is the table's and a first win brings the reliquary; his recipes are there and none makes Prism Shards; the Cowl is a costume; Seen Clearly is granted from code; Abbey Glass glows and serves the Accord; the Lantern Lens shows its holder alone and changes nothing; the Mirror Inscription turns back only small shots, only under a ward, only at those its holder may harm; he is resistant to the Concordance's control; every message has its words; nothing of his loads a chunk or leaves his dimension.
- Server game tests (`GlassAbbotGameTests`), over a chapel floor and three mirrors laid out from the middle of an arena:
  - he kneels at his place, unhurt, and a creative player's blow does not wake him; a player in the nave does; he is unhurt waking and stands to fight at the one player's health; his health scales with the party;
  - his whole mantle turns three quarters of a blow; two mirrors marked burn, and only they; with both turned to the pair's stops his beam comes home and cracks his mantle, he kneels dazed and the marks go out; cracked, a blow does a quarter more than its whole; the player who turned the mirrors takes part;
  - with the mirrors at rest the beam is lost: his mantle holds, its path runs mirror to mirror, a player on it is burned and one off it is not;
  - his shards strike the player they are thrown at and break; his lance strikes the player on his line, and not the one beside it;
  - at half health the Veil falls, unhurt; two images stand up playing what he plays; one blow shatters an image and its striker takes part; below a fifth the Breaking; left alone he kneels again at his place, healed, his images gone, his mirrors at rest and nobody's part kept;
  - his loot is each participant's own (Prism Shards, the reliquary on a first win, the experience shared, Seen Clearly), a bystander gets nothing, and the Grey Mist opens in the apse;
  - the Lantern Lens shows an invisible zombie and not a visible one, and among his images the true Abbot alone;
  - the Mirror Inscription turns no shot without a ward, nor a shot over 8; under a ward it spends the arrow and hurts the skeleton that shot it, and then rests;
  - his recipes, and Abbey Glass's light.
- Client game test (`GlassAbbotClientGameTests`), in a real world: an instance opens and he kneels at his lectern; stepping into the nave wakes him; his Shard Fan and Prism Line wound up; two mirrors marked, the player turns each to the pair's stop, he gathers the sun at his place and his light comes home and cracks his mantle (the player who turned the mirrors takes part); the Veil and his two images; his fall with the player's loot, Seen Clearly and the Grey Mist in the apse; a fresh chapel where he kneels again. Pictures of each.
- Not run: play by hand, a dedicated server, two clients, performance measurements.

### Results
- Local, on this branch (no Minecraft toolchain here, so neither the build nor the game tests ran locally; CI runs them):
  - `python3 tools/check_mod_data.py`: the whole audit ran; its one error, Abbey Glass's item having no name in the asset check (its block was missing from `agriculture.all_blocks()`), is fixed, and `check_assets` and `check_glass_abbot` were rerun clean.
  - Deliberate breaks, each put back: `GlassAbbotEntity.HEALTH`, the Prism Line's cooldown in `GlassAbbotEntity.Attack`, an image's credit to its striker (`GlassImageEntity`), the Mirror Inscription's check that its holder may harm the shooter, and `LanternLens.EVERY`; the audit named every one.
  - `python3 scripts/check_repository.py` passed; `python3 -m unittest discover -s scripts/tests` passed (287 tests); `tools/check_icon_maps.py` passed for the five maps; every changed Java file parses.
- `./gradlew build` and `./gradlew runClientGameTest`: CI's results go on the pull request.

## World and event applicability

He lives in his chapel's own dimension; nothing changes in the Overworld. He is met all year; no season gates him or his loot.

## Rollout and open questions

The shared framework's defaults hold: four players and eight instances, belongings kept on death in a lair. **Echo** (keeping the last good setting) and **Tether** (holding a mirror against his turn) wait for those principles' invocations, as the plan says.
