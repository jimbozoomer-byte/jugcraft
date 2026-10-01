# Retro Game Shop and Retro Trader

Status: implemented in source; **not yet played**. Compiles in CI; game tests cover it, and the client test screenshots the trader, his cabinet and the shop.
Proposal issue: none. The owner supplied the proposal ("Retro Game Shop & Retro Trader (Pixel Hollows companion)") with a reference picture of the trader and asked for it to be implemented on 1 October 2026.
Owner: @jimbozoomer-byte
Target milestone and tier: Discovery. Emeralds and a compass are all a player needs.
Primary specialty and supported player role: exploration and trade, supporting the [Pixel Hollows](pixel-hollows.md).

## Player experience
In some new plains villages there is a small **Retro Game Shop**: an oak storefront with a false front and a lit "Retro Games" sign, display windows, a chequered floor, shelves of chunky cartridges, a counter, pixel lamps and a glowing **arcade cabinet**. The villager who takes that cabinet becomes the **Retro Trader**: a bearded villager with brown hair, black glasses and green eyes, in a red-and-black flannel shirt over a white tee with a silver cross, backpack straps, navy plaid trousers and black-and-white sneakers (drawn from the owner's reference picture).

He:
- **always offers a Pixel Hollows map**, which is how most players will first find the rare cave;
- **sells a little of the cave's palette** (circuitstone, pixel lamps, a few shards), so builders can try it before the trip;
- **buys pixel shards back**, which gives explorers a reason to return.

Everything is original: no real shop names, logos, colours, uniforms, games or franchise characters.

## Connections
- Existing input producer: vanilla villages, villagers, emeralds and compasses; pixel shards from the Pixel Hollows.
- Existing output consumer: Pixel Hollows exploration (the map), cozy building (decor), the player economy (the buyback).
- Technology connection: none directly; the cave he points to holds the extra copper, redstone and tin.
- Magic connection: none.
- Reachable entry path (prove no circular unlock):
  1. Find a generated shop (its cabinet needs no cave items), pay 12 emeralds and a compass for the map, and travel to the cave.
  2. After visiting the cave, craft an **arcade cabinet** (2 pixel shards, planks, a glass pane and redstone). Any unemployed villager takes the profession at it: the solo, anywhere route.
  The generated shop never needs cave items, and the cabinet recipe is a later convenience.
- Which connections are required vs optional; trade and solo routes:

| Connection | Status |
| --- | --- |
| Pixel Hollows biome | Required (the map points to it) |
| Vanilla villages, emeralds, compass | Required, already exist |
| Finding a shop | Optional: the cave can be found by exploring, and the cabinet makes a trader anywhere |
| Trade between players | Optional: shards and maps can be traded |
| Solo route | Fully soloable |

- How this specialty stays useful without mastering every other branch: a map seller and a small decor shop; nothing requires him.

## Balance and automation
- **Shop rarity:** the shop joins `minecraft:village/plains/houses` with weight 1 (vanilla houses weigh 1 to 3 each). Each house slot of a new plains village has about a 1-in-N chance of being the shop, where N is the pool's total weight; the game test `retroGameShopTemplate` logs the actual share. Plains villages only in this slice.
- **Trades** are data (26.1+): `data/jugcraft/villager_trade/retro_trader/*.json`, one tag per level (`#jugcraft:retro_trader/level_1` …) and a trade set per level (`data/jugcraft/trade_set/retro_trader/level_<n>.json`) that the profession names. All are generated from `TRADES` in `tools/pixel_hollows.py`. The last column is each trade's `reputation_discount` (the old price multiplier):

| Level | Trade | Uses per restock | XP | Price multiplier |
| --- | --- | --- | --- | --- |
| Novice | 12 emeralds + compass → Pixel Hollows Map (**always offered**) | 1 | 5 | 0.2 |
| Novice | 1 emerald → 8 circuitstone | 12 | 1 | 0.05 |
| Apprentice | 6 pixel shards → 1 emerald (buyback) | 12 | 5 | **0** |
| Apprentice | 3 emeralds → 1 pixel lamp | 4 | 5 | 0.05 |
| Journeyman | 4 emeralds → 2 pixel shards | 3 | 10 | 0.05 |

  The novice and apprentice trade sets each draw two trades from a tag of exactly two, so all of them always appear. Expert and master add nothing.
- **No profit loop.** The proposal assumed discounts only lower what the trader charges; in fact reputation (curing, trading) and Hero of the Village also lower what he asks for in his *buy* trades. With ordinary discounts the buyback could fall to 1 shard → 1 emerald while shards cost 1 emerald for 2: buy 2, sell 2, gain an emerald. So the buyback's `reputation_discount` is 0 (reputation never discounts it), and Hero of the Village V (the strongest in survival) lowers it only to 3 shards → 1 emerald. The cheapest a shard can be bought is 1 emerald for 2 (0.5 each); the most selling one can pay is 1/3. The data checker and the game test `retroTraderHasNoProfitLoop` both check this.
- **Stock stays small and restock-limited,** so the cave remains the main source of the palette and ores. Automation is limited to vanilla trading.

## The Pixel Hollows map
- The trader sells a **Pixel Hollows Map**: an unmarked map. Use it and the server looks for the nearest Pixel Hollows from where you stand; the map becomes a normal explorer map (scale 2, tracking) centred on the cave, with an original Pixel Hollows marker (`jugcraft:pixel_hollows` map decoration). Because the biome is underground, the map's name gives the depth: "Pixel Hollows, around Y −16".
- **Server-only, bounded search** (`PixelHollowsMaps`): the game's own biome search (the one behind `/locate biome`), which samples the biome noise and loads or generates no chunks, in columns 64 blocks apart out to 2,048 blocks, every 32 blocks of height, starting at Y −16. At most 65 × 65 columns of 12 samples. It runs only when a player uses the map, and a 5-second cooldown stops repeated searches. It returns at once in a dimension or world where the biome cannot generate.
- **No cave in reach:** the player is told ("No Pixel Hollows within 2,048 blocks of here") and the map stays unmarked, to be used somewhere else. It never becomes a blank or wrong map.
- **Change from the proposal:** the proposal searched once when the trade was created, from the trader's village, and showed a sold-out trade when nothing was found. In 26.1+ trades are data files and their items are made by item modifiers, so a search at trade time would need a new item-modifier type; searching when the map is used is simpler, works anywhere the buyer takes it, and still never sells a dud. Open question answered: there is nothing to retry on restock.
- Purchases go through vanilla trading, validated by the server.

## Multiplayer and persistence
- Server authority: vanilla trading and villager AI, no custom packets. The map search runs on the server.
- Ownership: vanilla villager rules; restock and prices per villager.
- **Stable IDs:** `jugcraft:retro_trader` (profession), `jugcraft:arcade_cabinet` (block, item and job site point of interest), `jugcraft:pixel_hollows_map` (item), `jugcraft:pixel_hollows` (map decoration), the trades `jugcraft:retro_trader/*` and trade sets `jugcraft:retro_trader/level_1`–`3`, the shop template `jugcraft:village/plains/retro_game_shop`, the sound `jugcraft:entity.villager.work_retro_trader`.
- A marked map is an ordinary vanilla filled map; nothing about it depends on Jugcraft after it is made.
- **Existing worlds:** villages already generated are unchanged; shops appear only in new ones.
- **Disable switch:** `retro_trader.enabled=false` stops new shops, the map trade (for trades made from then on) and the cabinet recipe. The profession, the cabinet, the map item, existing traders and their saved offers stay. Trades that sell cave goods follow the `pixel_hollows` switch.

## Dependencies and assets
- Fabric API's `PoiHelper` registers the cabinet as a job site; trades are vanilla data. Fabric API has no village pool API, so a mixin accessor (`mixin/StructureTemplatePoolAccessor`) adds the shop to the plains houses pool when the server starts.
- Original assets: the trader's 64×64 villager overlay (also used for his zombie form) and the cabinet's textures are drawn by `tools/pixel_hollows_textures.py`; the cabinet model is in `tools/retro_models.py`; the shop is written by `tools/retro_game_shop.py` from vanilla blocks and Jugcraft's own; the work sound reuses the note block's "bit" sound by name. MIT.
- The shop template is written in 26.3's format with 26.3's DataVersion (5023), so the data fixer leaves it alone. A DataVersion above the game's made the whole template load as air; the game test `retroGameShopTemplateLoads` compares the two numbers, so after a platform bump `DATA_VERSION` in `tools/retro_game_shop.py` must follow.

## Verification
Results are recorded in the PR. The checks:
- `python3 tools/check_mod_data.py`: the Java trade table against `tools/pixel_hollows.py`, the search bounds, and the buyback against the cheapest sale.
- Game tests (`PixelHollowsGameTests`):
  - `retroTraderTrades`: novice, apprentice and journeyman traders get exactly their trades; the map costs 12 emeralds and a compass, once per restock.
  - `retroTraderHasNoProfitLoop`: the buyback's multiplier is 0; the best buyback per shard is below the cheapest sale.
  - `pixelHollowsMapNeedsACaveInReach`: in the superflat test world, using the map leaves it unmarked.
  - `pixelHollowsMapIsMarked`: a marked map carries the marker and the depth in its name.
  - `arcadeCabinetIsAJobSite`: the lower half is an acquirable job site for the profession; the upper half is not.
  - `villagerClaimsTheArcadeCabinet`: an unemployed villager walks to a cabinet and becomes a Retro Trader.
  - `retroGameShopTemplateLoads`: the template loads with its blocks and carries the game's DataVersion.
  - `retroGameShopTemplate`: the shop placed as the test structure has the cabinet, the "Retro Games" sign, the street jigsaw and a villager, and the shop is in the plains houses pool.
- Client screenshots: `jugcraft_retro_trader`, `jugcraft_retro_game_shop`, and the cabinet in `jugcraft_pixel_hollows_blocks`.
- **Not run:** shop frequency across generated villages on ten seeds (only the per-house weight is known); a shop joined to a real village street; buying a map in a client and following it to a cave; the map search's time in a real world; a dedicated server with two clients trading with one trader; restock and restart with a saved map; old-world upgrade; disable-switch behaviour in a running world.

## World and event applicability
- Village fit: plains villages only; the shop uses their oak and cobblestone with a few Jugcraft blocks inside.
- Not seasonal; no pets, bosses, dimensions or rare loot.

## Rollout and open questions
- Two shops can appear in one large village; the pool has no per-village limit.
- The villager in the shop usually takes its cabinet, but any unemployed villager may claim it first.
- Other village styles, wandering-trader stock and cosmetic outfits are later proposals.
