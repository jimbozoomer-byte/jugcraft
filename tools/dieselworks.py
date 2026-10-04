"""Dieselworks (batch 45, docs/features/dieselworks.md): building blocks in the look of the dieselpunk giants.

Rusted and riveted plate, green patina panels, chipped red iron, banded copper dome plate, ribbed pillars, skid iron,
see-through rust grating for catwalks, steel I-beams, porthole windows and amber cage lamps. The plates reuse the
giants' textures (tools/dieselrust_textures.py, "dr_*"); the grating, porthole and lamp textures are drawn here.

Java: building/Dieselworks.java registers the blocks; tools/check_mod_data.py keeps the list and strengths the same.
Every recipe turns metal plate (or ingots, nuggets and glass) into blocks; nothing turns a block back into metal.
"""
import math
import random

from PIL import Image

MOD = "jugcraft"

# id: (display name, kind, hardness, blast resistance). Kinds: "family" is a full block with its slab and stairs,
# "full" a plain cube, "pillar" a log-like column, "grating" a see-through cube with its slab, "beam" a steel I-beam
# lying along an axis, "glass" a porthole window and "lamp" a small caged lamp.
BLOCKS = {
    "rust_plate": ("Rust Plate", "family", 5.0, 6.0),
    "riveted_rust_plate": ("Riveted Rust Plate", "family", 5.0, 6.0),
    "patina_plate": ("Patina Plate", "family", 5.0, 6.0),
    "perforated_patina_plate": ("Perforated Patina Plate", "full", 5.0, 6.0),
    "red_iron_plate": ("Red Iron Plate", "family", 5.0, 6.0),
    "copper_dome_plate": ("Copper Dome Plate", "family", 5.0, 6.0),
    "riveted_band_block": ("Riveted Band", "full", 5.0, 6.0),
    "skid_iron_block": ("Skid Iron", "full", 5.0, 6.0),
    "ribbed_patina_pillar": ("Ribbed Patina Pillar", "pillar", 5.0, 6.0),
    "ribbed_rust_pillar": ("Ribbed Rust Pillar", "pillar", 5.0, 6.0),
    "rust_grating": ("Rust Grating", "grating", 3.0, 6.0),
    "steel_i_beam": ("Steel I-Beam", "beam", 5.0, 6.0),
    "porthole_window": ("Porthole Window", "glass", 1.0, 3.0),
    "amber_cage_lamp": ("Amber Cage Lamp", "lamp", 1.5, 3.0),
}
# Textures of the cubes: one name for all faces, or (side, end) for pillars.
TEXTURES = {
    "rust_plate": "dr_rust_bare",
    "riveted_rust_plate": "dr_rust",
    "patina_plate": "dr_patina",
    "perforated_patina_plate": "dr_perforated",
    "red_iron_plate": "dr_red",
    "copper_dome_plate": "dr_dome",
    "riveted_band_block": "dr_band",
    "skid_iron_block": "dr_skid",
    "ribbed_patina_pillar": ("dr_ribbed_patina", "dr_patina"),
    "ribbed_rust_pillar": ("dr_ribbed_rust", "dr_rust"),
    "rust_grating": "dw_grating",
    "porthole_window": "dw_porthole",
}
LAMP_LIGHT = 14
TOOLTIPS = {
    "rust_grating": "See-through: build catwalks and walkways.",
    "steel_i_beam": "Lies along the axis you place it on, like a log.",
    "amber_cage_lamp": "Gives off warm amber light.",
}


def blocks():
    out = []
    for block, (_, kind, _, _) in BLOCKS.items():
        out.append(block)
        if kind == "family":
            out += [f"{block}_slab", f"{block}_stairs"]
        elif kind == "grating":
            out.append(f"{block}_slab")
    return out


def items():
    return []


# ------------------------------------------------------------------ models

def _faces(texture, uv=None):
    return {face: ({"texture": texture, "uv": uv} if uv else {"texture": texture})
            for face in ("north", "south", "east", "west", "up", "down")}


def i_beam_elements():
    """A steel I-beam along y (the blockstate turns it): two flanges joined by a web, with a rust band."""
    out = []
    for x0, x1 in ((1, 4), (12, 15)):
        out.append({"from": [x0, 0, 1], "to": [x1, 16, 15], "faces": _faces("#side")})
    out.append({"from": [4, 0, 6.5], "to": [12, 16, 9.5], "faces": _faces("#web")})
    return out


def lamp_elements():
    """A caged amber lamp: a rusty base and cap, four cage bars and the glowing glass between them."""
    return [
        {"from": [4, 0, 4], "to": [12, 2, 12], "faces": _faces("#metal")},
        {"from": [5, 2, 5], "to": [11, 11, 11], "faces": _faces("#glass"), "light_emission": 14},
        {"from": [4.5, 11, 4.5], "to": [11.5, 13, 11.5], "faces": _faces("#metal")},
        {"from": [7, 13, 7], "to": [9, 14, 9], "faces": _faces("#metal")},
    ] + [{"from": [x, 2, z], "to": [x + 1, 11, z + 1], "faces": _faces("#bar")}
         for x in (4.5, 10.5) for z in (4.5, 10.5)] + [
        {"from": [4.5, 6, 4.5], "to": [11.5, 7, 5], "faces": _faces("#bar")},
        {"from": [4.5, 6, 11], "to": [11.5, 7, 11.5], "faces": _faces("#bar")},
    ]


def pillar_blockstate(model):
    return {"variants": {"axis=y": {"model": model}, "axis=z": {"model": model, "x": 90},
                         "axis=x": {"model": model, "x": 90, "y": 90}}}


def write_all(write, assets, data, lang, condition, self_drop):
    from construction import stairs_blockstate
    models = assets / "models" / "block"
    for block, (name, kind, _, _) in BLOCKS.items():
        lang[f"block.{MOD}.{block}"] = name
        model = f"{MOD}:block/{block}"
        if kind == "pillar":
            side, end = TEXTURES[block]
            write(models / f"{block}.json", {"parent": "minecraft:block/cube_column",
                                             "textures": {"side": f"{MOD}:block/{side}", "end": f"{MOD}:block/{end}"}})
            write(assets / "blockstates" / f"{block}.json", pillar_blockstate(model))
        elif kind == "beam":
            write(models / f"{block}.json", {"textures": {"side": f"{MOD}:block/dr_skid", "web": f"{MOD}:block/dr_rust_bare",
                                                          "particle": f"{MOD}:block/dr_skid"},
                                             "elements": i_beam_elements()})
            write(assets / "blockstates" / f"{block}.json", pillar_blockstate(model))
        elif kind == "lamp":
            write(models / f"{block}.json", {"ambientocclusion": False, "textures": {
                "metal": f"{MOD}:block/dr_rust_bare", "bar": f"{MOD}:block/dr_band", "glass": f"{MOD}:block/dr_amber_on",
                "particle": f"{MOD}:block/dr_rust_bare"}, "elements": lamp_elements()})
            write(assets / "blockstates" / f"{block}.json", {"variants": {"": {"model": model}}})
        else:
            write(models / f"{block}.json", {"parent": "minecraft:block/cube_all",
                                             "textures": {"all": f"{MOD}:block/{TEXTURES[block]}"}})
            write(assets / "blockstates" / f"{block}.json", {"variants": {"": {"model": model}}})
        write(assets / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": model}})
        write(data / "loot_table" / "blocks" / f"{block}.json", self_drop(block))
        if kind not in ("family", "grating"):
            continue
        texture = f"{MOD}:block/{TEXTURES[block]}"
        side = {"bottom": texture, "top": texture, "side": texture}
        slab = f"{block}_slab"
        lang[f"block.{MOD}.{slab}"] = f"{name} Slab"
        write(models / f"{slab}.json", {"parent": "minecraft:block/slab", "textures": side})
        write(models / f"{slab}_top.json", {"parent": "minecraft:block/slab_top", "textures": side})
        write(assets / "blockstates" / f"{slab}.json", {"variants": {
            "type=bottom": {"model": f"{MOD}:block/{slab}"}, "type=top": {"model": f"{MOD}:block/{slab}_top"},
            "type=double": {"model": model}}})
        write(assets / "items" / f"{slab}.json", {"model": {"type": "minecraft:model", "model": f"{MOD}:block/{slab}"}})
        table = self_drop(slab)
        table["pools"][0]["entries"][0]["modifier"] = [
            {"type": "minecraft:set_count", "count": 2, "add": False,
             "condition": {"type": "minecraft:match_block", "blocks": f"{MOD}:{slab}", "state": {"type": "double"}}},
            {"type": "minecraft:explosion_decay"}]
        write(data / "loot_table" / "blocks" / f"{slab}.json", table)
        write(data / "recipe" / f"{slab}.json", shaped(condition, ["BBB"], {"B": f"{MOD}:{block}"}, slab, 6))
        if kind == "grating":
            continue
        stairs = f"{block}_stairs"
        lang[f"block.{MOD}.{stairs}"] = f"{name} Stairs"
        for suffix in ("", "_inner", "_outer"):
            write(models / f"{stairs}{suffix}.json", {"parent": f"minecraft:block/{'stairs' if not suffix else suffix[1:] + '_stairs'}",
                                                     "textures": side})
        write(assets / "blockstates" / f"{stairs}.json", stairs_blockstate(f"{MOD}:block/{stairs}"))
        write(assets / "items" / f"{stairs}.json", {"model": {"type": "minecraft:model", "model": f"{MOD}:block/{stairs}"}})
        write(data / "loot_table" / "blocks" / f"{stairs}.json", self_drop(stairs))
        write(data / "recipe" / f"{stairs}.json", shaped(condition, ["B  ", "BB ", "BBB"], {"B": f"{MOD}:{block}"}, stairs, 4))
    for key, text in TOOLTIPS.items():
        lang[f"tooltip.{MOD}.{key}"] = text

    recipes = data / "recipe"
    for name, result, pattern, key, count in RECIPES:
        write(recipes / f"{name}.json", shaped(condition, pattern, key, result, count))


R = f"{MOD}:rust_plate"
P = f"{MOD}:patina_plate"
# (recipe name, result, pattern, key, count). One plate of metal makes one block; trims add nuggets or a dye.
RECIPES = [
    ("rust_plate", "rust_plate", ["PP", "PP"], {"P": "#c:plates/iron"}, 4),
    ("riveted_rust_plate", "riveted_rust_plate", [" R ", "RNR", " R "], {"R": R, "N": "minecraft:iron_nugget"}, 4),
    ("patina_plate", "patina_plate", ["PP", "PP"], {"P": "#c:plates/copper"}, 4),
    ("perforated_patina_plate", "perforated_patina_plate", [" P ", "PNP", " P "], {"P": P, "N": "minecraft:iron_nugget"}, 4),
    ("red_iron_plate", "red_iron_plate", ["RRR", "RDR", "RRR"], {"R": R, "D": "minecraft:red_dye"}, 8),
    ("copper_dome_plate", "copper_dome_plate", [" P ", "PCP", " P "], {"P": P, "C": "minecraft:copper_ingot"}, 4),
    ("riveted_band_block", "riveted_band_block", ["NRN", "R R", "NRN"], {"R": R, "N": "minecraft:iron_nugget"}, 4),
    ("skid_iron_block", "skid_iron_block", ["SS", "SS"], {"S": "#c:plates/steel"}, 4),
    ("ribbed_patina_pillar", "ribbed_patina_pillar", ["P", "P"], {"P": P}, 2),
    ("ribbed_rust_pillar", "ribbed_rust_pillar", ["R", "R"], {"R": R}, 2),
    ("rust_grating", "rust_grating", ["BB", "BB"], {"B": "minecraft:iron_bars"}, 4),
    ("steel_i_beam", "steel_i_beam", ["SSS", " S ", "SSS"], {"S": "#c:plates/steel"}, 6),
    ("porthole_window", "porthole_window", [" R ", "RGR", " R "], {"R": R, "G": "minecraft:glass"}, 4),
    ("amber_cage_lamp", "amber_cage_lamp", ["NNN", "NGN", "NRN"],
     {"N": "minecraft:iron_nugget", "G": "minecraft:glowstone", "R": R}, 2),
]


def shaped(condition, pattern, key, result, count):
    return {"fabric:load_conditions": condition("machines"), "type": "minecraft:crafting_shaped", "category": "building",
            "pattern": pattern, "key": key, "result": {"id": f"{MOD}:{result}", "count": count}}


# ------------------------------------------------------------------ art

RUST = [(46, 26, 18), (70, 38, 22), (98, 52, 28), (128, 68, 34), (156, 88, 44), (184, 112, 58)]
IRON = [(22, 20, 20), (52, 48, 46), (92, 86, 82), (140, 132, 124)]


def grating():
    """Rust grating with see-through square holes, so catwalks show what is below."""
    rng = random.Random(4501)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            if x % 4 == 0 or y % 4 == 0 or x == 15 or y == 15:
                c = RUST[rng.choice([2, 3, 3, 4])]
                if x in (0, 15) or y in (0, 15):
                    c = RUST[1]
                img.putpixel((x, y), c + (255,))
    for x, y in ((0, 0), (12, 0), (0, 12), (12, 12)):
        img.putpixel((x, y), IRON[3] + (255,))
    return img


def porthole():
    """A round porthole: a riveted rust ring round clear glass with a bright glint."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    rng = random.Random(4502)
    for y in range(16):
        for x in range(16):
            r = math.hypot(x - 7.5, y - 7.5)
            if r > 5.2:
                c = RUST[rng.choice([2, 3, 3, 4])]
                if r > 7.6:
                    c = RUST[1]
                img.putpixel((x, y), c + (255,))
            elif r > 4.4:
                img.putpixel((x, y), IRON[2] + (255,))
    for angle in range(0, 360, 45):
        x = round(7.5 + 6.4 * math.cos(math.radians(angle)))
        y = round(7.5 + 6.4 * math.sin(math.radians(angle)))
        img.putpixel((x, y), IRON[3] + (255,))
    for x, y in ((5, 5), (6, 4), (4, 6)):
        img.putpixel((x, y), (230, 240, 236, 255))
    return img


def draw_all(save):
    save(grating(), "block", "dw_grating")
    save(porthole(), "block", "dw_porthole")
