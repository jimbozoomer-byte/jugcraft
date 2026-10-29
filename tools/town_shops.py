"""The town's shops and their prices in Jugs (the town's credit, kept per player by the server: town/Jugs.java).

Each shop sells offers (the player pays Jugs) and may buy goods (the player is paid Jugs). An offer is
[item, count, price]; a seasonal offer also names the themes it is on sale in (tools/town_decor.py THEMES). Prices
are whole Jugs for the whole count.

Balance: the General Store is where Jugs come from: it buys a farm's and a mine's ordinary output at modest prices,
which turns surplus into Jugs at the player's own pace. Everything for sale is decoration, fun or convenience; nothing
sold is needed for progression. No shop sells anything any shop buys, so no item can be bought and sold back at a
profit (tools/check_mod_data.py checks it, and that per-item sale prices beat buy prices for anything in both lists).
Emeralds are bought, never sold, so villager trading cannot be looped through the town either.
"""

SHOPS = {
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
            ["minecraft:white_wool", 32, 1], ["minecraft:feather", 64, 1], ["minecraft:string", 64, 1],
            ["minecraft:bone", 64, 1], ["minecraft:rotten_flesh", 64, 1], ["minecraft:gunpowder", 32, 1],
            ["minecraft:honeycomb", 16, 1], ["minecraft:oak_log", 64, 1], ["minecraft:spruce_log", 64, 1],
            ["minecraft:birch_log", 64, 1], ["minecraft:cobblestone", 64, 1], ["minecraft:coal", 32, 1],
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
            ["minecraft:tropical_fish_bucket", 1, 12, ["summer"]], ["minecraft:yellow_candle", 2, 3, ["summer"]],
            ["minecraft:firework_rocket", 8, 5, ["summer", "december"]],
            ["minecraft:carved_pumpkin", 2, 3, ["autumn", "halloween", "harvest"]],
            ["minecraft:jack_o_lantern", 2, 4, ["autumn", "halloween"]],
            ["minecraft:hay_block", 2, 3, ["autumn", "harvest"]], ["minecraft:orange_candle", 2, 3, ["autumn", "halloween"]],
            ["minecraft:pumpkin_pie", 2, 3, ["autumn", "harvest"]],
            ["minecraft:snow_block", 8, 3, ["winter", "december"]], ["minecraft:packed_ice", 8, 4, ["winter"]],
            ["minecraft:powder_snow_bucket", 1, 6, ["winter"]], ["minecraft:cookie", 8, 3, ["winter", "december"]],
            ["minecraft:white_candle", 2, 3, ["winter", "december"]],
            ["minecraft:black_candle", 2, 3, ["halloween"]], ["minecraft:cobweb", 4, 6, ["halloween"]],
            ["minecraft:soul_lantern", 2, 4, ["halloween"]], ["minecraft:skeleton_skull", 1, 40, ["halloween"]],
            ["minecraft:zombie_head", 1, 40, ["halloween"]],
            ["minecraft:cake", 1, 6, ["harvest", "december"]], ["minecraft:baked_potato", 8, 4, ["harvest"]],
            ["minecraft:golden_carrot", 2, 6, ["harvest"]], ["minecraft:honey_bottle", 2, 4, ["harvest"]],
            ["minecraft:red_candle", 2, 3, ["december"]], ["minecraft:green_candle", 2, 3, ["december"]],
            ["minecraft:spruce_sapling", 2, 3, ["december", "winter"]], ["minecraft:red_wool", 4, 3, ["december"]],
        ],
        "buys": [],
    },
    "curios": {
        "name": "Curiosities",
        "sells": [
            ["minecraft:firework_rocket", 8, 5], ["minecraft:name_tag", 1, 15], ["minecraft:lead", 1, 6],
            ["minecraft:saddle", 1, 20], ["minecraft:spyglass", 1, 12], ["minecraft:bundle", 1, 8],
            ["minecraft:painting", 2, 4], ["minecraft:item_frame", 4, 4], ["minecraft:armor_stand", 1, 6],
            ["minecraft:jukebox", 1, 12], ["minecraft:note_block", 2, 4], ["minecraft:bell", 1, 25],
            ["minecraft:music_disc_cat", 1, 30], ["minecraft:music_disc_blocks", 1, 30],
            ["minecraft:music_disc_chirp", 1, 30], ["minecraft:music_disc_mall", 1, 30],
            ["minecraft:music_disc_mellohi", 1, 30], ["minecraft:music_disc_strad", 1, 30],
            ["minecraft:glow_ink_sac", 4, 4], ["minecraft:slime_ball", 4, 5], ["minecraft:candle", 4, 3],
            ["minecraft:glow_item_frame", 2, 5],
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


def offers(shop, side):
    for offer in SHOPS[shop][side]:
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


def loop_errors():
    """No item is both sold and bought, and any per-item buy price stays under every per-item sale price."""
    errors = []
    sold, bought = {}, {}
    for shop in SHOPS:
        for item, count, price, _ in offers(shop, "sells"):
            sold.setdefault(item, []).append(price / count)
        for item, count, price, _ in offers(shop, "buys"):
            bought.setdefault(item, []).append(price / count)
    for item in sold.keys() & bought.keys():
        if max(bought[item]) >= min(sold[item]):
            errors.append(f"{item}: bought at {max(bought[item]):.3f} a piece, sold at {min(sold[item]):.3f}")
        errors.append(f"{item} is both sold and bought by the town")
    for shop in SHOPS:
        for side in ("sells", "buys"):
            for item, count, price, themes in offers(shop, side):
                if count < 1 or count > 64 or price < 1:
                    errors.append(f"{shop} {side} {item}: count {count}, price {price}")
        open_at, close_at = HOURS[shop]
        if not 0 <= open_at < close_at <= 18000:
            errors.append(f"{shop}: hours {open_at}-{close_at} must fall between dawn and midnight")
    always = {item for item, _, _, themes in offers(ESSENTIALS_FROM, "sells") if themes is None}
    for item in ESSENTIALS:
        if item not in always:
            errors.append(f"essential {item} is not always on sale at the {ESSENTIALS_FROM} shop")
    return errors
