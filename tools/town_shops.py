"""The town's shops and their prices in Jugs (the town's credit, kept per player by the server: town/Jugs.java).

Each shop sells offers (the player pays Jugs) and may buy goods (the player is paid Jugs). An offer is
[item, count, price]; a seasonal offer also names the themes it is on sale in (tools/town_decor.py THEMES). Prices
are whole Jugs for the whole count.

Balance: the General Store is where Jugs come from: it buys a farm's and a mine's ordinary output at modest prices,
which turns surplus into Jugs at the player's own pace. Everything for sale is decoration, fun or convenience; nothing
sold is needed for progression. No shop sells anything any shop buys, so no item can be bought and sold back at a
profit (tools/check_mod_data.py checks it, and that per-item sale prices beat buy prices for anything in both lists).
Emeralds are bought, never sold, so villager trading cannot be looped through the town either.

No shop buys anything the transmutation tablet, condensers or density gems may form (the jugcraft:tablet_outputs tag,
docs/features/convenience-transmutation.md); the server refuses such a sale too (town/ShopMenu.java). Essence is
renewable and any dissolved item becomes essence, so a formable buy offer would turn essence into Jugs. Cobblestone at
64 for 1 Jug did exactly that: one essence formed one cobblestone, so 64 essence made a Jug, while a 6-Jug bucket
dissolves for 768 essence, doubling essence and Jugs on every lap (2026-11-12 economy audit). Logs (64 for 1) and
white wool (32 for 1) let daylight collectors mint Jugs the same way, at 2,048 and 1,536 essence a Jug.
"""

import json
from pathlib import Path
import deepstead_shop_goods as guild

# The server-owned convenience catalogue: the items transmutation may form (concordance/assay/TabletOutputs.java).
TABLET_OUTPUTS = Path(__file__).resolve().parents[1] / "src/main/resources/data/jugcraft/tags/item/tablet_outputs.json"

SHOPS = {
    **{shop: {"name": guild.SHOPS[shop], "sells": [["jugcraft:"+key, count, price] for key,count,price,_,_ in stock], "buys": []}
       for shop,stock in guild.STOCK.items()},
    "deepstead_lampwright": {
        "name": "Deepstead Lampwright",
        "sells": [
            ["jugcraft:miners_knot_tile", 4, 12], ["jugcraft:geode_reading_lamp", 1, 85],
            ["jugcraft:gallery_chimes", 1, 125],
        ],
        "buys": [],
    },
    "deepstead_outfitter": {
        "name": "Deepstead Survey Outfitters",
        "sells": [
            ["jugcraft:shaft_plummet", 1, 80], ["jugcraft:surveyors_reel", 1, 120],
            ["jugcraft:miners_knot_tile", 4, 12],
        ],
        "buys": [],
    },
    "general": {
        "name": "General Store",
        "sells": [
            ["minecraft:torch", 16, 3], ["minecraft:lantern", 1, 3], ["minecraft:bread", 4, 3],
            ["minecraft:cooked_beef", 4, 4], ["minecraft:ladder", 8, 2], ["minecraft:glass", 8, 4],
            ["minecraft:bone_meal", 8, 3], ["minecraft:bucket", 1, 6], ["minecraft:arrow", 16, 4],
            ["minecraft:shield", 1, 10], ["minecraft:map", 1, 5], ["minecraft:compass", 1, 8],
            ["minecraft:white_bed", 1, 8], ["minecraft:oak_sapling", 2, 2], ["minecraft:flower_pot", 2, 2],
        ],
        "buys": [
            # Owner currency anchor: one gold ingot is one Jug. Farm output is bulk income,
            # not several gold ingots for a handful of crops. No speculative diamond peg.
            ["minecraft:wheat", 64, 1], ["minecraft:carrot", 64, 1], ["minecraft:potato", 64, 1],
            ["minecraft:beetroot", 64, 1], ["minecraft:pumpkin", 32, 1], ["minecraft:melon_slice", 64, 1],
            ["minecraft:sugar_cane", 64, 1], ["minecraft:cocoa_beans", 64, 1], ["minecraft:sweet_berries", 64, 1],
            ["minecraft:apple", 32, 1], ["minecraft:egg", 16, 1], ["minecraft:leather", 16, 1],
            # No white wool, oak/spruce/birch logs or cobblestone: the tablet can form them (see the docstring).
            ["minecraft:feather", 64, 1], ["minecraft:string", 64, 1],
            ["minecraft:bone", 64, 1], ["minecraft:rotten_flesh", 64, 1], ["minecraft:gunpowder", 32, 1],
            ["minecraft:honeycomb", 16, 1], ["minecraft:coal", 32, 1],
            ["minecraft:copper_ingot", 32, 1], ["minecraft:iron_ingot", 16, 2], ["minecraft:gold_ingot", 4, 4],
            ["minecraft:redstone", 64, 1], ["minecraft:lapis_lazuli", 32, 1], ["minecraft:emerald", 4, 1],
            ["minecraft:amethyst_shard", 32, 1], ["minecraft:cod", 32, 1], ["minecraft:salmon", 32, 1],
        ],
    },
    "seasonal": {
        "name": "Seasonal Stall",
        "sells": [
            ["minecraft:pink_petals", 8, 3, ["spring"]], ["minecraft:cherry_sapling", 1, 3, ["spring"]],
            ["minecraft:flowering_azalea", 1, 4, ["spring"]], ["minecraft:lilac", 2, 3, ["spring"]],
            ["minecraft:pink_candle", 2, 3, ["spring"]],
            ["minecraft:sunflower", 2, 3, ["summer"]], ["minecraft:sea_pickle", 4, 4, ["summer"]],
            ["minecraft:tropical_fish_bucket", 1, 45, ["summer"]], ["minecraft:yellow_candle", 2, 3, ["summer"]],
            ["minecraft:firework_rocket", 8, 15, ["summer", "december"]],
            ["minecraft:carved_pumpkin", 2, 3, ["autumn", "halloween", "harvest"]],
            ["minecraft:jack_o_lantern", 2, 4, ["autumn", "halloween"]],
            ["minecraft:hay_block", 2, 3, ["autumn", "harvest"]], ["minecraft:orange_candle", 2, 3, ["autumn", "halloween"]],
            ["minecraft:pumpkin_pie", 2, 3, ["autumn", "harvest"]],
            ["minecraft:snow_block", 8, 3, ["winter", "december"]], ["minecraft:packed_ice", 8, 4, ["winter"]],
            ["minecraft:powder_snow_bucket", 1, 6, ["winter"]], ["minecraft:cookie", 8, 3, ["winter", "december"]],
            ["minecraft:white_candle", 2, 3, ["winter", "december"]],
            ["minecraft:black_candle", 2, 3, ["halloween"]], ["minecraft:cobweb", 4, 6, ["halloween"]],
            ["minecraft:soul_lantern", 2, 18, ["halloween"]], ["minecraft:skeleton_skull", 1, 200, ["halloween"]],
            ["minecraft:zombie_head", 1, 200, ["halloween"]],
            ["minecraft:cake", 1, 18, ["harvest", "december"]], ["minecraft:baked_potato", 8, 4, ["harvest"]],
            ["minecraft:golden_carrot", 2, 24, ["harvest"]], ["minecraft:honey_bottle", 2, 4, ["harvest"]],
            ["minecraft:red_candle", 2, 3, ["december"]], ["minecraft:green_candle", 2, 3, ["december"]],
            ["minecraft:spruce_sapling", 2, 3, ["december", "winter"]], ["minecraft:red_wool", 4, 3, ["december"]],
        ],
        "buys": [],
    },
    "curios": {
        "name": "Curiosities",
        "sells": [
            ["minecraft:firework_rocket", 8, 15], ["minecraft:name_tag", 1, 60], ["minecraft:lead", 1, 15],
            ["minecraft:saddle", 1, 95], ["minecraft:spyglass", 1, 45], ["minecraft:bundle", 1, 30],
            ["minecraft:painting", 2, 22], ["minecraft:item_frame", 4, 16], ["minecraft:armor_stand", 1, 35],
            ["minecraft:jukebox", 1, 150], ["minecraft:note_block", 2, 25], ["minecraft:bell", 1, 175],
            ["minecraft:music_disc_cat", 1, 125], ["minecraft:music_disc_blocks", 1, 125],
            ["minecraft:music_disc_chirp", 1, 125], ["minecraft:music_disc_mall", 1, 125],
            ["minecraft:music_disc_mellohi", 1, 125], ["minecraft:music_disc_strad", 1, 125],
            ["minecraft:glow_ink_sac", 4, 10], ["minecraft:slime_ball", 4, 12], ["minecraft:candle", 4, 8],
            ["minecraft:glow_item_frame", 2, 30],
        ],
        "buys": [],
    },
    "florist": {
        "name": "Florist",
        "sells": [
            ["minecraft:poppy", 4, 1], ["minecraft:dandelion", 4, 1], ["minecraft:cornflower", 4, 2],
            ["minecraft:allium", 4, 2], ["minecraft:azure_bluet", 4, 2], ["minecraft:red_tulip", 4, 2],
            ["minecraft:orange_tulip", 4, 2], ["minecraft:white_tulip", 4, 2], ["minecraft:pink_tulip", 4, 2],
            ["minecraft:oxeye_daisy", 4, 2], ["minecraft:lily_of_the_valley", 4, 2], ["minecraft:blue_orchid", 4, 3],
            ["minecraft:rose_bush", 2, 3], ["minecraft:peony", 2, 3], ["minecraft:birch_sapling", 2, 2],
            ["minecraft:jungle_sapling", 2, 3], ["minecraft:acacia_sapling", 2, 2], ["minecraft:dark_oak_sapling", 4, 3],
            ["minecraft:azalea", 1, 3], ["minecraft:moss_block", 4, 3], ["minecraft:lily_pad", 4, 3],
        ],
        "buys": [],
    },
}

# Opening hours, in the day's ticks (0 is 6 am, 1000 an hour): every shop opens at 7 am and shuts at 6 pm, when its
# keeper goes out for the evening (town/TownRoutine.java). The taverns never shut: at any hour their innkeepers sell the
# General Store's ESSENTIALS, at the store's own prices, so nobody is ever left without light, food or a bed.
HOURS = {shop: [1000, 12000] for shop in SHOPS}
# The guild counter below daylight serves all three mine shifts.
HOURS["deepstead_lampwright"] = [0, 24000]
HOURS["deepstead_outfitter"] = [0, 24000]
for shop in guild.SHOPS:
    HOURS[shop] = [0, 24000]
ESSENTIALS_FROM = "general"
ESSENTIALS = ["minecraft:torch", "minecraft:bread", "minecraft:cooked_beef", "minecraft:white_bed"]


def clock(ticks):
    """A time of day as a sign shows it: 1000 -> "7am"."""
    hour = (ticks // 1000 + 6) % 24
    return f"{hour % 12 or 12}{'am' if hour < 12 else 'pm'}"


def hours_line(shop):
    open_at, close_at = HOURS[shop]
    return f"Open {clock(open_at)}-{clock(close_at)}"


# Roles townsfolk can have, and the shop each shopkeeper keeps (see tools/town.py's spots).
WELCOME_JUGS = 20
MAX_BALANCE = 1_000_000_000
TRADE_RANGE = 8
ATM_RANGE = 6


def offers(shop, side, shops=None):
    for offer in (SHOPS if shops is None else shops)[shop][side]:
        item, count, price = offer[:3]
        themes = offer[3] if len(offer) > 3 else None
        yield item, count, price, themes


def data():
    out = {}
    for shop, info in SHOPS.items():
        out[shop] = {"name": info["name"], "sells": [], "buys": [], "hours": HOURS[shop]}
        if shop == ESSENTIALS_FROM:
            out[shop]["essentials"] = ESSENTIALS
        for side in ("sells", "buys"):
            for item, count, price, themes in offers(shop, side):
                entry = {"item": item, "count": count, "price": price}
                if themes:
                    entry["themes"] = themes
                out[shop][side].append(entry)
    return out


def tablet_outputs(path=TABLET_OUTPUTS):
    """The item IDs in the jugcraft:tablet_outputs tag (its entries are plain item IDs)."""
    return {value if isinstance(value, str) else value["id"]
            for value in json.loads(Path(path).read_text(encoding="utf-8"))["values"]}


def loop_errors(shops=None, formable=None):
    """No item is both sold and bought, any per-item buy price stays under every per-item sale price, and no shop buys
    an item transmutation may form. Defaults to SHOPS and the tablet_outputs tag; tests pass other data."""
    shops = SHOPS if shops is None else shops
    formable = tablet_outputs() if formable is None else set(formable)
    errors = []
    sold, bought = {}, {}
    for shop in shops:
        for item, count, price, _ in offers(shop, "sells", shops):
            sold.setdefault(item, []).append(price / count)
        for item, count, price, _ in offers(shop, "buys", shops):
            bought.setdefault(item, []).append(price / count)
            if item in formable:
                errors.append(f"{shop} buys {item} ({count} for {price}): transmutation can form it, so essence would become Jugs")
    for item in sold.keys() & bought.keys():
        if max(bought[item]) >= min(sold[item]):
            errors.append(f"{item}: bought at {max(bought[item]):.3f} a piece, sold at {min(sold[item]):.3f}")
        errors.append(f"{item} is both sold and bought by the town")
    for shop in shops:
        for side in ("sells", "buys"):
            for item, count, price, themes in offers(shop, side, shops):
                if count < 1 or count > 64 or price < 1:
                    errors.append(f"{shop} {side} {item}: count {count}, price {price}")
        open_at, close_at = HOURS[shop]
        if shop in ("deepstead_lampwright", "deepstead_outfitter", *guild.SHOPS):
            if [open_at, close_at] != [0, 24000]:
                errors.append("Deepstead's shift counter must cover the whole day")
        elif not 0 <= open_at < close_at <= 18000:
            errors.append(f"{shop}: hours {open_at}-{close_at} must fall between dawn and midnight")
    always = {item for item, _, _, themes in offers(ESSENTIALS_FROM, "sells", shops) if themes is None}
    for item in ESSENTIALS:
        if item not in always:
            errors.append(f"essential {item} is not always on sale at the {ESSENTIALS_FROM} shop")
    return errors
