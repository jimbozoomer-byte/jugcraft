# Halloween nights

Status: implemented in source, not yet played. The Build workflow compiles it, and CI's game tests are recorded below.
Proposal issue: none; requested directly by the owner on 1 October 2026. Of ten Halloween ideas, the owner chose to do five at a time, one pull request per batch. Batch A was the festivities ([halloween-festivities.md](halloween-festivities.md)). This is batch B ("can we do the previous other 5 things you had suggested"): will-o'-wisps, a Pumpkin Chunkin' trebuchet, a bigger Candy Bag, the Harvest Moon and the Headless Horseman.
Owner: @jimbozoomer-byte
Target milestone and tier: Milestone 3 (first homestead); Discovery tier (logs, planks, string, an iron ingot, a glass bottle, a scarecrow and a lit pumpkin; no machines). The Horseman is a fight for iron gear.
Primary specialty and supported player role: farming and play; supports groups (a boss for a party, a throwing contest), fighters (the Horseman) and builders (lanterns and jars of light).

## Player experience
### Will-o'-wisps (only on event nights)
1. **Where they come out.** On Halloween-event nights (overworld, from dusk at 13000 to dawn at 23000), small glowing wisps drift over **swamps** and **cornfields** (within 2 blocks of a corn plant), under the open sky, 1 to 3 blocks above the ground.
2. **They're shy.** A wisp darts away from a player within 6 blocks. Sneak and you can creep to within 2.5 blocks.
3. **Catch one** by using a **glass bottle** on it: the bottle becomes a **Wisp in a Jar** and you earn **Bottled Light**. A Wisp in a Jar is a lantern (light 13) that stands or hangs and keeps glowing all year.
4. **They fade** at dawn, when the event ends, or at any blow, leaving nothing.

### The Pumpkin Chunkin' Trebuchet (all year)
5. **Build one** (string, three logs, an iron ingot and three planks; see Balance) and place it: it throws the way you face.
6. **Load it** by using a pumpkin on it: a pumpkin, carved pumpkin, jack o'lantern, the white, Jarrahdale and Cinderella pumpkins, or any hand-carved one. One at a time.
7. **Aim it.** Sneak and use it with an empty hand to step the release angle from 30° to 60° in 5° steps (45° to start).
8. **Let fly.** Use it with an empty hand. The pumpkin is flung about 50 blocks; the arm swings back after 1.5 seconds. Where it lands, it bursts and a **landing marker** (a stake with a chequered flag and the distance) stands for 30 seconds.
9. **The board.** Using an unloaded trebuchet shows its angle and the three longest throws (each thrower's best). The first time you make the board you get that place's **First, Second or Third Prize Ribbon** (once per trebuchet). A throw of 50 blocks or more earns **Pumpkin Chunkin'**.
10. **Which pumpkin?** Hollow carved pumpkins are lighter and fly farther; squat heavy ones fall shorter. A small random gust adds or takes up to 4 %.

### The Candy Bag, bigger (all year)
11. The **Candy Bag** now **holds treats** like a bundle: candy (`#c:foods/candy`) and cookies, up to a bundle's worth (64 candy corn; fewer of a treat that stacks smaller). Click a treat onto it, or the bag onto a treat, in your inventory; anything else won't go in.
12. Trick-or-treating, treats go **straight into the bag** in your hand or inventory (what doesn't fit goes in your inventory).
13. Its tooltip shows **how many homes you've visited tonight** (of the 10 that fill the bag). The next night the count starts again; the treats stay.

### The Harvest Moon (on Halloween itself)
14. On the **nights of 31 October** (the server's date in the operator's time zone, configurable) while the event runs, every in-game night is a Harvest Moon. Players are told when it rises and sets.
15. Under it, Jugcraft crops, trellis crops, gourd stems and giant pumpkin vines **grow twice as fast**, giant pumpkins **swell twice as fast**, and lit carved and giant pumpkins **throw off sparks**.

### The Headless Horseman (only at midnight in the event)
16. **Summon him.** During the event, in the overworld, within about a minute of midnight (game time 17000 to 19000), not in Peaceful: give a Scarecrow a **jack o'lantern or a lit hand-carved pumpkin** for a head, under the open sky, then **sneak and use the scarecrow with an empty hand**. The head is taken in a flash of (harmless) lightning, and he rides in 12 to 16 blocks away, hunting whoever called him. If something is missing, the scarecrow says what.
17. **The fight.** A headless rider on a black horse with a burning lantern: 160 health, armour 8, strong charges (9 damage), and **flaming pumpkins** thrown from 4 to 28 blocks every 3 seconds (6 damage and 3 seconds of fire to everything within 2 blocks of where one bursts; they never break or light blocks). At half health he is **enraged**: a quarter faster, throwing three at a time twice as often. He keeps to his arena (32 blocks around the scarecrow), and a boss bar shows his health. Only one rides within 128 blocks.
18. **He rides off**, leaving nothing, at dawn, when the event ends, or after 30 seconds with nobody within 48 blocks of his arena.
19. **Defeat him** and he drops the **Horseman's Lantern** (light 15, stands or hangs, epic), the **Horseman's Cloak** (worn on the chest, for looks), 1–2 king-size candy bars and 3–6 candy corn; everyone within 48 blocks earns **Lost His Head**.

## Connections
- Existing input producer: Halloween event timing (`HalloweenSeason`), the Scarecrow and hand-carved pumpkins (summoning), corn (wisps), the pumpkin varieties (ammunition), trick-or-treating (the bag's count and treats), the Harvest Scale's ribbons (trebuchet prizes); vanilla glass bottles, jack o'lanterns, logs, planks, string and iron.
- Existing output consumer: light sources (jar, lantern), trophies (ribbons, the cloak), food (the Horseman's candy), faster growth for every farm (the Harvest Moon), a reason to grow corn and carve.
- Technology connection: none needed; the trebuchet is a first step toward the dieselpunk launchers of later tiers.
- Magic connection: none yet; wisps and the Harvest Moon are natural hooks for the magic branch.
- Reachable entry path: the trebuchet has an all-year recipe from vanilla materials; the scarecrow, corn, glass bottles and hand-carved pumpkins all have their own routes ([../branches/AGRICULTURE.md](../branches/AGRICULTURE.md)). Nothing here needs anything made here.
- Required vs optional: all optional. The Wisp in a Jar and the Horseman's Lantern and Cloak are trophies only these events give; nothing in progression needs them, and vanilla lanterns and Jugcraft lights do the same job.
- How this stays useful without other branches: night events for anyone who farms, and a boss and a contest for groups.

## Balance and automation
- **Trebuchet recipe** (`data/jugcraft/recipe/trebuchet.json`): string top right; three logs and an iron ingot across the middle; three planks below. It throws 1 pumpkin a time and uses it up (it bursts), so it's a pumpkin sink, not a source.
- **Throw speed** = 1.5 blocks/tick × the pumpkin's factor × (1 ± up to 0.04). Factors: pumpkin 1.0, carved 1.06, jack o'lantern 1.03, white 1.02, Jarrahdale 0.97, Cinderella 0.95, hand-carved 1.06, hand-carved white 1.08, hand-carved Jarrahdale 1.03, hand-carved Cinderella 1.01. With vanilla projectile drag and gravity, a plain pumpkin flies about 51 blocks at 40° and 50 at 45°; a hand-carved white pumpkin at 40° about 58. Measured across the ground on the server, from the sling to where it burst.
- **Ribbons** have no use or value; one per thrower per trebuchet (it remembers the last 64 throwers). A new trebuchet has a new board, like a new regatta flag.
- **Wisps:** every 5 seconds, for each player in the overworld, a 50 % chance to try one spot 10 to 32 blocks away; never more than 4 near the spot (48 blocks) or 64 in the world. One wisp makes one jar from one glass bottle; there is no other recipe and no way back.
- **The Horseman:** one summon uses one lit head; at most one per scarecrow area at a time, and only within about a minute of each in-game midnight during the event. His drops are trophies and a little candy (`data/jugcraft/loot_table/entities/headless_horseman.json`; servers can change them), only for a player's kill; 50 experience.
- **Harvest Moon:** growth ×2 (`HarvestMoon.GROWTH_BONUS`), giant pumpkin swelling ×2, only on its nights.
- **Candy Bag:** a bundle's capacity (64 treats); filling it is only moving items.
- No conversion loops: nothing here turns back into its inputs or into a currency.

## Multiplayer and persistence
- **Server authority.**
  - Every use goes through vanilla's block and entity use paths, which check reach and spawn protection: loading, aiming and firing the trebuchet, catching a wisp, summoning at a scarecrow.
  - The season, date, hour, difficulty, sky, head and "one at a time" are all checked on the server, from the server's clocks and config, never the client's.
  - Throw distances, the board and ribbons are decided on the server where the pumpkin lands. The Candy Bag's count is written by the server; its tooltip only reads it.
  - The only new packet is server-to-client: whether the Harvest Moon is up (`jugcraft:harvest_moon`), for the sparks. Clients send nothing new.
- **Saved state.**
  - A trebuchet keeps its loaded pumpkin, angle, board and ribbon list (block entity `jugcraft:trebuchet`); breaking it drops the pumpkin.
  - The Horseman is saved with his arena, rage and loneliness; a flying pumpkin with where it left and its trebuchet; wisps like any mob.
  - A Candy Bag keeps its treats (`minecraft:bundle_contents`) and tonight's count (`jugcraft:candy_bag_night`) on the item.
  - The Harvest Moon keeps no state: it is worked out from the date and the overworld clock. Landing markers are not saved.
- **Bounded work.**
  - The wisp spawner runs every 100 ticks; each try reads at most about 30 blocks and never loads a chunk.
  - A wisp looks for the nearest player each tick. The Horseman checks his arena each tick and for players every 20 ticks.
  - The Harvest Moon is checked every 100 ticks; a crop's random tick only reads a field.
  - A Candy Bag looks at its count once a second. A flying pumpkin lasts at most 200 ticks (10 s), a flaming one 100.
- **Ending the event.** With the event off (out of its dates, or `halloween.mode=off`): no wisps spawn and those out fade; no Horseman can be summoned and one riding leaves without loot; the Harvest Moon sets. Jars, lanterns, cloaks, ribbons, treats and trebuchets all stay and keep working.
- **The `agriculture` switch** off: no wisps, no Horseman, no Harvest Moon, no trebuchet recipe; placed blocks and items stay.
- New IDs only: blocks with items `wisp_in_a_jar`, `trebuchet`, `horseman_lantern`; item `horseman_cloak`; entities `will_o_wisp`, `flying_pumpkin`, `throw_marker`, `headless_horseman`, `flaming_pumpkin`; block entity `trebuchet`; data component `candy_bag_night`; equipment asset `horseman_cloak`. The `candy_bag` ID is unchanged; it gains `minecraft:bundle_contents` (empty by default), so bags made before this change still work.
- New config option in `config/jugcraft.properties`: `halloween.harvest_moon` (month-day, default `10-31`).

## Dependencies and assets
No new dependencies. Every texture is drawn by code (`tools/night_textures.py`): the wisp, the Horseman and his glowing eyes and lantern, the landing marker, the cloak (worn and as an item), the jar's glass, light, lid and string, the trebuchet's wood, beams, iron and rope, and the lantern's pumpkin, face, glow and iron. Models and data come from `tools/night_data.py`; the Horseman, wisp and marker are drawn by Java models in the client (`HorsemanModel`, `WispModel`, `ThrowMarkerRenderer`).

## Verification
VERIFICATION_PENDING

## World and event applicability
- **Seasonal rules** (docs/CONTENT_BRANCHES.md): the same operator settings as trick-or-treating (`halloween.start`, `halloween.end`, `halloween.timezone`, `halloween.mode`) plus `halloween.harvest_moon`. Clients' clocks never count. Ending the event stops wisps, summoning and the Harvest Moon; everything earned stays. Nothing in progression needs the event.
- **Boss containment:** the Horseman never breaks or burns blocks, keeps to a 32-block arena around his scarecrow (riding back if he strays), lets go of anyone who runs far away, leaves on his own at dawn or when left alone, and is never summoned in Peaceful, outside the overworld, or a second time within 128 blocks.
- Wisps are ambient creatures: they never attack, can't be pushed and take no fall damage.

## Rollout and open questions
- The Horseman's numbers (health, damage, throw rate) are first guesses that only a playtest can tune.
- The Harvest Moon covers every in-game night of its real-world day; on a server with a normal day length that is about 72 nights, which is intended ("Halloween night") but may want a shorter window later.
- Removing this content needs no migration beyond the usual: placed jars, lanterns and trebuchets would become air, and the Horseman and wisps would be dropped from saved chunks.
