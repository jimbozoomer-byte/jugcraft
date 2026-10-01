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
- **Trades** (numbers in `TRADES` in `tools/pixel_hollows.py`; the checker compares them with `RetroTrader.java`):

| Level | Trade | Uses per restock | XP | Price multiplier |
| --- | --- | --- | --- | --- |
| Novice | 12 emeralds + compass → Pixel Hollows map (**always offered**) | 1 | 5 | 0.2 |
| Novice | 1 emerald → 8 circuitstone | 12 | 1 | 0.05 |
| Apprentice | 6 pixel shards → 1 emerald (buyback) | 12 | 5 | **0** |
| Apprentice | 3 emeralds → 1 pixel lamp | 4 | 5 | 0.05 |
| Journeyman | 4 emeralds → 2 pixel shards | 3 | 10 | 0.05 |

  A villager takes two trades per level, and the novice and apprentice levels have exactly two each, so all of them always appear. Expert and master add nothing.
- **No profit loop.** The proposal assumed discounts only lower what the trader charges; in fact reputation (curing, trading) and Hero of the Village also lower what he asks for in his *buy* trades. With ordinary discounts the buyback could fall to 1 shard → 1 emerald while shards cost 1 emerald for 2: buy 2, sell 2, gain an emerald. So the buyback has price multiplier 0 (reputation never discounts it), and Hero of the Village V (the strongest in survival) lowers it only to 3 shards → 1 emerald. The cheapest a shard can be bought is 1 emerald for 2 (0.5 each); the most selling one can pay is 1/3. The data checker and the game test `retroTraderHasNoProfitLoop` both check this.
- **Stock stays small and restock-limited,** so the cave remains the main source of the palette and ores. Automation is limited to vanilla trading.

## The Pixel Hollows map
- A normal explorer map (scale 2, tracking) centred on the cave, with an original Pixel Hollows marker (`jugcraft:pixel_hollows` map decoration, teal map ink). Because the biome is underground, its name gives the depth: "Pixel Hollows, around Y −16".
- **Server-only, bounded search** (`PixelHollowsMaps`): it samples the biome noise only, no chunks, in square rings of columns 64 blocks apart out to 40 rings (2,560 blocks), at Y −16, 0, −32, 16 and −48, and stops at the first ring that has the biome (nearest in that ring). At most 6,561 columns × 5 heights. It runs once, when the trader first gets the trade; the finished map is part of the saved offer, so it is never searched again (also across restarts).
- **No cave in reach:** the trade still appears, showing an unfilled map whose tooltip says "No Pixel Hollows within reach of this village", but with **zero uses**, so it is sold out and stays sold out after restocking. It never sells a blank or wrong map. (Open question answered: it does not retry on restock.)
- Purchases go through vanilla trading, validated by the server.

## Multiplayer and persistence
- Server authority: vanilla trading and villager AI, no custom packets. The map search runs on the server.
- Ownership: vanilla villager rules; restock and prices per villager.
- **Stable IDs:** `jugcraft:retro_trader` (profession), `jugcraft:arcade_cabinet` (block, item and job site point of interest), `jugcraft:pixel_hollows` (map decoration), the shop template `jugcraft:village/plains/retro_game_shop`, the sound `jugcraft:entity.villager.work_retro_trader`.
- **Existing worlds:** villages already generated are unchanged; shops appear only in new ones.
- **Disable switch:** `retro_trader.enabled=false` stops new shops, the map trade for traders who get their trades from then on, and the cabinet recipe. The profession, the cabinet, existing traders and their saved offers stay.

## Dependencies and assets
- Fabric API's villager helpers (`PointOfInterestHelper`, `TradeOfferHelper`). Fabric API has no village pool API, so a mixin accessor (`mixin/StructureTemplatePoolAccessor`) adds the shop to the plains houses pool when the server starts.
- Original assets: the trader's 64×64 villager overlay (also used for his zombie form) and the cabinet's textures are drawn by `tools/pixel_hollows_textures.py`; the cabinet model is in `tools/retro_models.py`; the shop is written by `tools/retro_game_shop.py` from vanilla blocks and Jugcraft's own; the work sound reuses the note block's "bit" sound by name. MIT.
- The shop template is written in 26.3's format with a DataVersion above any 26.x version, so the data fixer leaves it alone.

## Verification
Results are recorded in the PR. The checks:
- `python3 tools/check_mod_data.py`: the Java trade table against `tools/pixel_hollows.py`, the search bounds, and the buyback against the cheapest sale.
- Game tests (`PixelHollowsGameTests`):
  - `retroTraderNoviceTrades`: exactly two novice trades; in the superflat test world (no cave) the map trade is sold out, stays sold out after restocking, and costs 12 emeralds and a compass.
  - `retroTraderHasNoProfitLoop`: buyback multiplier 0; best buyback per shard below the cheapest sale.
  - `pixelHollowsMapIsMarked`: a map carries the marker and the depth in its name.
  - `pixelHollowsDistribution`: the map search on ten seeds, with its time.
  - `arcadeCabinetIsAJobSite`: the lower half is an acquirable job site for the profession; the upper half is not.
  - `villagerClaimsTheArcadeCabinet`: an unemployed villager walks to a cabinet and becomes a Retro Trader.
  - `retroGameShopTemplate`: the shop placed as the test structure has the cabinet, the "Retro Games" sign, the street jigsaw and a villager, and the shop is in the plains houses pool.
- Client screenshots: `jugcraft_retro_trader`, `jugcraft_retro_game_shop`, and the cabinet in `jugcraft_pixel_hollows_blocks`.
- **Not run:** shop frequency across generated villages on ten seeds (only the per-house weight is known); a shop joined to a real village street; buying a map in a client and following it; a dedicated server with two clients trading with one trader; restock and restart with a saved map; old-world upgrade; disable-switch behaviour in a running world.

## World and event applicability
- Village fit: plains villages only; the shop uses their oak and cobblestone with a few Jugcraft blocks inside.
- Not seasonal; no pets, bosses, dimensions or rare loot.

## Rollout and open questions
- Two shops can appear in one large village; the pool has no per-village limit.
- The villager in the shop usually takes its cabinet, but any unemployed villager may claim it first.
- Other village styles, wandering-trader stock and cosmetic outfits are later proposals.
