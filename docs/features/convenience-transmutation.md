# Convenience transmutation and automated outputs

Owner-directed local change, 21 October 2026. Implementation: OpenAI Codex (GPT-6). Not published or deployed.

## Player behavior

The tablet remains useful for ordinary building supplies and convenience items. The server's explicit `jugcraft:tablet_outputs` item tag permits common wood, stone, glass, wool, concrete, terracotta, torches, lanterns, ladders and scaffolding. Unlisted items default to denied, including modded items, ores, metals, gems, storage blocks of valuable materials, machines, equipment, currencies and rare loot. A low essence price or being a placeable block is never enough to permit an item.

Dissolving still accepts the existing safe default items and credits their established essence value. It requires confirmation and protects stored contents/modified stacks. **Dissolving a valuable does not allow withdrawing it later.** The confirmation tooltip explains this. Existing knowledge, favorites and essence balances remain saved. Only permitted learned patterns appear in the browser; creating them still spends their value with no surcharge. Wallet/star payment, batch selection, favorites and search remain available.

Both the block table and portable tablet share this policy, as do the philosopher stone's menu, legacy transmutation menu, handheld forming action and the Mercurial Eye's building selection. Server checks run before payment and output, including for an old selection. Data packs can deliberately customize the item tag.

Condensers and density gems use the same server allowlist. Existing valuable targets remain saved but pause before consuming inputs, star charge or essence. Existing products remain recoverable. Owners can replace their targets with approved supplies to resume. Collector/relay transfers also stop at a blocked condenser target. New density gems default to oak logs; their picker cycles oak logs, glass, torches, stone bricks and ladders, skipping entries removed by a data pack.

This is **not a complete economy rebalance**. Recipe conversion, collector fuel upgrades, ore processing and NPC buying/selling retain their existing behavior; the [12 November economy audit](#economy-audit-12-november-2026) below checked them and closed the town-shop path. Ordinary allowed wood and stone can feed legitimate crafting and trade chains.

## Interface

Uses the owner's Exchange `transmute.png` unchanged: grey panel, purple alchemical circles and recessed inventory. Twelve learned-item slots surround the output circle; search, confirm-dissolve, withdraw, batch size, favorites and page controls sit alongside it. Unused controls baked into the source atlas are covered by the screen renderer; they are not presented as nonfunctional buttons. Transaction details appear in button tooltips. The interface retains keyboard-accessible controls and the existing reduced-motion preference.

Provenance: [Exchange tablet import](../../art/owner-library/ports/exchange-tablet/README.md).

## Validation

Local validation, October 21:

- The final gameplay implementation ran all **2,256 required server GameTests**. The only two failures were in `AlchemyPlayabilityGameTests`: its old Gem-armor manufacturing expectation and a new assertion that assumed output would not reuse an emptied feed slot. Both test assertions were corrected, with no production-code changes. The affected **23-test group then passed**; the other **2,233 tests had passed** in the combined run.
- The rendered `ConvenienceTabletClientGameTests` completed in the combined run: real menu packets, paging, confirmation/dissolution, paid torch output, valuable-pattern exclusion, two GUI scales and keyboard focus. The final screenshots confirm the supplied circles, unobstructed labels, inventory slots and visible output. A separate final client-only run passed; its captures are retained in the local `tablet-review/screenshots` folder.
- Repository/documentation checks passed. The data/recipe/art audit passed: **6,240 material IDs**, 13,187 block/item models, 3,729 quad parts and 176 rendered model checks. All 394 output-tag entries are unique; required supplies are included and tested valuables excluded. The supplied and runtime tablet atlases have identical SHA-256 hashes.
- Regression coverage includes permitted paid output, already-learned valuables, legacy/handheld bypasses, saved knowledge, ghost slots, confirmation, wallet/star billing, inventory capacity, paused saved condenser targets, preserved samples/products/buffers, rejected relay intake and density target selection.

These are local automated tests. Owner survival balancing and the coordinated hosted/client release remain pending. No server files or public update manifest were changed.

## November 7 local building catalogue expansion

The shared allowlist now has 995 explicit entries, including 601 additional ordinary wood, Chiselworks and Stoneworks variants. Existing values, learned-item requirements, paid withdrawals and component checks are unchanged. This expansion does not authorize valuable materials, machines or storage devices. Other decorative families whose pricing or recovery routes remain unreviewed stay excluded. See the [local batch record](nov7-priorities.md) for implementation scope and current validation; the October results above are historical, not evidence for this expansion.


## Economy audit (12 November 2026)

Claude Code (Claude Opus 5.5), branch `claude/efgh-f-economy`, base `81472369a5`. This answers the open follow-up: audit recipe conversions, collector fuel upgrades and NPC buy/sell prices against the convenience-only output rule. Essence is renewable by design (daylight collectors) and every default item dissolves into it, so the rule only holds if nothing the tablet, condensers or density gems form can turn into currency or a denied valuable. Prices below are the shipped catalogue (`alchemy/values.json`) and the shipped town data. Game tests are written, not run.

### Fixed: the town paid Jugs for formable supplies

The General Store bought five items in `jugcraft:tablet_outputs`: cobblestone (64 for 1 Jug), oak, spruce and birch logs (64 for 1) and white wool (32 for 1).

- **A closed, unbounded loop.** Cobblestone forms for 1 essence, so a Jug cost 64 essence. The store sells a bucket for 6 Jugs and a bucket dissolves for 768 essence. 384 essence -> 384 cobblestone -> 6 Jugs -> 1 bucket -> 768 essence: double on every lap, limited only by the two-tick trade gate. The compass (8 Jugs, 1,088 essence) and the Curiosities jukebox (12 Jugs, 8,256 essence) did the same.
- **Daylight became currency.** Logs and wool cost 2,048 and 1,536 essence a Jug, above the 688 essence a Jug buys at best (the jukebox), so they made no lap gain, but a collector's essence still became Jugs through them (cobblestone: every 64 essence a collector gathered).
- **Laundering.** Dyed wool, also formable, washes back to white wool in the wool extractor (48 essence for 48), and stone mines as cobblestone, so dropping these items from the tablet instead would have needed a much wider cut.

Fix, server-side and narrow:

- `ShopMenu.sell` refuses any item in `jugcraft:tablet_outputs`, read live, so a data pack that widens the tag narrows what the town buys. The player sees "The town does not buy ...: transmutation can make it".
- `tools/town_shops.py` drops the five offers (33 -> 28 General Store purchases) and `loop_errors()` now rejects any buy offer on the tag; `shops.json` is regenerated. No other price changed. The 1 gold ingot = 1 Jug anchor stands, and the diamond peg stays undecided.
- Player effect: cobblestone, logs and wool no longer earn Jugs. Crops, animal and fish products, coal, metals, redstone, lapis, emeralds and amethyst still do. Existing balances are untouched.

### Checked, no change needed

| Path | Finding |
| --- | --- |
| Shop buy-then-sell spreads | No item is both sold and bought. Buying any town good and processing it with the mod's recipes never makes a bought item for less than the town pays (checked to a fixpoint over every local recipe). |
| Civic commissions | Four orders (32 wheat, 32 arrows, 8 starflowers, 16 copper wire) for 1, 1, 1 and 2 Jugs, once per player per 72,000-tick cycle. None is formable. The server computes the cycle and stores the paid flag in the same saved data as the balance, so a replay or second claim fails. |
| Collector fuel upgrades | Price is max(1, form(next) - dissolve(current)): 393, 601 and 3,385 essence for value gains of 392.25, 600.25 and 3,384.5 (coal 128 -> 2081/4 -> 2241/2 -> 4505). Fuels only climb, by crafting or the collector, and feed Dark Matter, Red Matter and Nova charges; their storage blocks round-trip 9:1. No recipe returns coal or a formable item. A collector's buffer fills only from daylight, so minted essence from elsewhere cannot buy upgrades. |
| Tablet, condensers, density gems, void ring, legacy menu, Scale, Mercurial Eye | All gate on the tag before payment and output. The philosopher's world transforms only cycle stone, cobblestone, dirt, sand, grass, gravel and sandstone (all formable), at least 8 essence a block. The amulets charge 4,096 essence to fill a lava bucket worth 832. |
| Rounding | Dissolving rounds down and forming rounds up, so no fractional round trip gains. |
| Mineral sands | None of the four sands is formable or in any item tag, no recipe recreates one, and every sand-line conversion returns no more essence than it spends (`test_mineral_sand_processing.py`). None appears in the minting list below. |

### Recorded, awaiting the owner: machine conversions that mint essence

These machine conversions return more essence than forming their inputs costs. Each runs on machine time and power, and with the town fix the extra essence can only form more convenience supplies or pay for essence services (furnace, pedestals, repairs, charges), which collectors already fund. Fixing any of them needs a recipe-yield change, a change to a reference value (cobblestone 1, gravel 4, planks 8, logs 32, clay ball 16) or removing a core building supply from the tablet, so they are left for the owner. `test_economy_loops.py` pins the exact list and numbers, so any new one fails.

| Recipe | Essence in | Essence out (chanced outputs whole) |
| --- | --- | --- |
| Water treatment: 1 mud -> 4 clay balls | 1 (mud is unvalued: one-essence fallback) | 64 |
| Sawmill: 1 log -> 6 planks + 50% sawdust (19 woods including the locally integrated trees) | 32 | 49 (48.5 expected) |
| Walnut cutting: log/wood -> stripped log/wood + sawdust | 32 | 33 |
| Sawmill: 1 plank -> 3 sticks | 8 | 12 |
| Crusher: 1 cobblestone -> 1 gravel | 1 | 4 |
| Sifter: 1 gravel -> flint + 12% iron nugget + 8% tin nugget | 4 | 548/9 (9.69 expected) |
| Sifter: 1 coarse dirt -> dirt + 15% cotton seeds + 10% wheat seeds | 5/2 | 18 (2.75 expected) |
| Mud and mud-brick decorations still at the one-essence fallback (fountains, spouts, 15 pavings, 3 stair pieces) and cavern cinder bricks | 1 to 8 | 2 to 16 |

The sifter's iron and tin nuggets are the one metal output, but a cobblestone generator supplies unlimited cobblestone without essence, so the tablet adds no new metal route. Vanilla villagers are a similar case: they buy formable sticks, stone, wool and glass panes for emeralds, and an emerald dissolves for 16,384 essence. Their trade uses and restocks bound this, and tree farms supply the same sticks without the tablet. A fix would mean revaluing emeralds or cutting core supplies from the tablet, so it is the owner's call. 26.3's villager trade data is not in the repository, so exact trade rates were not checked.

### Tests

- `scripts/tests/test_economy_loops.py` (ran here, see the delivery notes): no formable town purchase, the five pre-audit offers rejected by the generator (negative case), the loop's exact numbers, no profit from town goods through recipes, commission items not formable, the recorded minting list, no single conversion from formable supplies to a town purchase, and fuel-upgrade conservation.
- `EconomyLoopGameTests` (written, not run): the 384-essence cobblestone loop refused at the store and conserved on dissolving back, every audited offer refused at any price, an ordinary sale paid once with replays and short counts refused, and the fuel ladder's prices against the live catalogue.
