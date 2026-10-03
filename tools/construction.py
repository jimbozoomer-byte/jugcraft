"""Construction chemistry (batch 32, docs/features/construction-chemistry.md): spray foam, cement and concrete.

Java: chemistry/ConstructionChemistry.java (numbers, blocks and items), FoamSprayerItem and CementItem.
tools/check_mod_data.py keeps the numbers here and in Java the same. The foam canister is made in the chemical reactor
(tools/petro.py, FLUID_RECIPES["chemical_reactor"]).
"""
import random

from PIL import Image

MOD = "jugcraft"

# The foam sprayer: how far it reaches (blocks), how many blocks one spray fills at most and how far they spread from
# where it lands, ticks between sprays, and blocks of foam in a canister.
SPRAY_RANGE = 16
SPRAY_BLOCKS = 12
SPRAY_RADIUS = 2.5
SPRAY_COOLDOWN = 8
CANISTER_FOAM = 32

# Blocks: display name, hardness, blast resistance, and whether they come as slabs and stairs too.
BLOCKS = {
    "construction_foam": ("Construction Foam", 0.3, 0.5, False),
    "concrete": ("Concrete", 2.5, 9.0, True),
    "blastproof_concrete": ("Blast-Proof Concrete", 15.0, 1200.0, True),
}
ITEMS = {
    "cement_mix": "Cement Mix",
    "cement": "Cement",
    "rebar": "Rebar",
    "foam_canister": "Foam Canister",
    "foam_sprayer": "Foam Sprayer",
}
TOOLTIPS = {
    "cement": "Use on construction foam to set it into concrete. Mix with gravel and water to make concrete.",
    "foam_canister": "Foam for the foam sprayer: fills 32 blocks.",
    "foam_sprayer": "Sprays construction foam where you aim, filling up to 12 open blocks: bridge gaps, seal caves and "
                    "hold back water or lava. Uses foam canisters from your inventory.",
    "construction_foam": "Light and quick to break. Use cement on it to set it into concrete.",
    "blastproof_concrete": "As blast-proof as obsidian.",
}


def blocks():
    out = []
    for block, (_, _, _, variants) in BLOCKS.items():
        out.append(block)
        if variants:
            out += [f"{block}_slab", f"{block}_stairs"]
    return out


def items():
    return list(ITEMS)


def stairs_blockstate(model):
    """The vanilla stairs blockstate (facing x half x shape), pointing at model, model_inner and model_outer."""
    import tower
    return tower.stairs_blockstate(model)


def write_all(write, assets, data, lang, condition, self_drop):
    for block, (name, _, _, variants) in BLOCKS.items():
        lang[f"block.{MOD}.{block}"] = name
        texture = {"all": f"{MOD}:block/{block}"}
        write(assets / "models" / "block" / f"{block}.json", {"parent": "minecraft:block/cube_all", "textures": texture})
        write(assets / "blockstates" / f"{block}.json", {"variants": {"": {"model": f"{MOD}:block/{block}"}}})
        write(assets / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": f"{MOD}:block/{block}"}})
        if block == "construction_foam":
            # Foam crumbles to nothing: it is made by the sprayer, not carried about.
            write(data / "loot_table" / "blocks" / f"{block}.json", {"type": "minecraft:block", "pools": []})
        else:
            write(data / "loot_table" / "blocks" / f"{block}.json", self_drop(block))
        if not variants:
            continue
        side = {"bottom": f"{MOD}:block/{block}", "top": f"{MOD}:block/{block}", "side": f"{MOD}:block/{block}"}
        slab, stairs = f"{block}_slab", f"{block}_stairs"
        lang[f"block.{MOD}.{slab}"] = f"{name} Slab"
        lang[f"block.{MOD}.{stairs}"] = f"{name} Stairs"
        models = assets / "models" / "block"
        write(models / f"{slab}.json", {"parent": "minecraft:block/slab", "textures": side})
        write(models / f"{slab}_top.json", {"parent": "minecraft:block/slab_top", "textures": side})
        write(assets / "blockstates" / f"{slab}.json", {"variants": {
            "type=bottom": {"model": f"{MOD}:block/{slab}"}, "type=top": {"model": f"{MOD}:block/{slab}_top"},
            "type=double": {"model": f"{MOD}:block/{block}"}}})
        write(assets / "items" / f"{slab}.json", {"model": {"type": "minecraft:model", "model": f"{MOD}:block/{slab}"}})
        for suffix in ("", "_inner", "_outer"):
            write(models / f"{stairs}{suffix}.json", {"parent": f"minecraft:block/{'stairs' if not suffix else suffix[1:] + '_stairs'}",
                                                     "textures": side})
        write(assets / "blockstates" / f"{stairs}.json", stairs_blockstate(f"{MOD}:block/{stairs}"))
        write(assets / "items" / f"{stairs}.json", {"model": {"type": "minecraft:model", "model": f"{MOD}:block/{stairs}"}})
        table = self_drop(slab)
        table["pools"][0]["entries"][0]["modifier"] = [
            {"type": "minecraft:set_count", "count": 2, "add": False,
             "condition": {"type": "minecraft:match_block", "blocks": f"{MOD}:{slab}", "state": {"type": "double"}}},
            {"type": "minecraft:explosion_decay"}]
        write(data / "loot_table" / "blocks" / f"{slab}.json", table)
        write(data / "loot_table" / "blocks" / f"{stairs}.json", self_drop(stairs))
        write(data / "recipe" / f"{slab}.json", shaped(condition, ["BBB"], {"B": f"{MOD}:{block}"}, slab, 6, "building"))
        write(data / "recipe" / f"{stairs}.json", shaped(condition, ["B  ", "BB ", "BBB"], {"B": f"{MOD}:{block}"}, stairs,
                                                         4, "building"))
    for item, name in ITEMS.items():
        lang[f"item.{MOD}.{item}"] = name
        parent = "minecraft:item/handheld" if item == "foam_sprayer" else "minecraft:item/generated"
        write(assets / "models" / "item" / f"{item}.json", {"parent": parent, "textures": {"layer0": f"{MOD}:item/{item}"}})
        write(assets / "items" / f"{item}.json", {"model": {"type": "minecraft:model", "model": f"{MOD}:item/{item}"}})
    for key, text in TOOLTIPS.items():
        lang[f"tooltip.{MOD}.{key}"] = text
    lang[f"message.{MOD}.foam_sprayer.empty"] = "No foam canisters"

    recipes = data / "recipe"
    # Cement: crushed limestone (calcite, or a bone block for its lime) burnt with clay and sand to clinker.
    for name, lime in (("cement_mix", "minecraft:calcite"), ("cement_mix_from_bone_block", "minecraft:bone_block")):
        write(recipes / f"{name}.json", {
            "fabric:load_conditions": condition("machines"), "type": "minecraft:crafting_shapeless", "category": "building",
            "ingredients": [lime, "minecraft:clay_ball", "minecraft:sand"],
            "result": {"id": f"{MOD}:cement_mix", "count": 4}})
    for kind, ticks in (("smelting", 200), ("blasting", 100)):
        write(recipes / f"cement_from_{kind}.json", {
            "fabric:load_conditions": condition("machines"), "type": f"minecraft:{kind}", "category": "misc",
            "ingredient": f"{MOD}:cement_mix", "result": {"id": f"{MOD}:cement"}, "experience": 0.1, "cookingtime": ticks})
    write(recipes / "concrete.json", shaped(condition, ["CGC", "GWG", "CGC"], {
        "C": f"{MOD}:cement", "G": "minecraft:gravel", "W": "minecraft:water_bucket"}, "concrete", 8, "building"))
    write(recipes / "rebar.json", shaped(condition, ["  S", " S ", "S  "], {"S": "#c:ingots/steel"}, "rebar", 6, "misc"))
    write(recipes / "blastproof_concrete.json", shaped(condition, ["CCC", "CRC", "CCC"], {
        "C": f"{MOD}:concrete", "R": f"{MOD}:rebar"}, "blastproof_concrete", 8, "building"))
    write(recipes / "foam_sprayer.json", shaped(condition, ["PPN", "RT ", "S  "], {
        "P": "#c:plates/steel", "N": "minecraft:iron_nugget", "R": f"{MOD}:rubber", "T": "minecraft:piston",
        "S": "#c:plates/steel"}, "foam_sprayer", 1, "equipment"))


def shaped(condition, pattern, key, result, count, category):
    return {"fabric:load_conditions": condition("machines"), "type": "minecraft:crafting_shaped", "category": category,
            "pattern": pattern, "key": key, "result": {"id": f"{MOD}:{result}", "count": count}}


# ------------------------------------------------------------------ art

def _speckle(base, spread, seed, dots=()):
    rng = random.Random(seed)
    img = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            d = rng.randint(-spread, spread)
            img.putpixel((x, y), tuple(max(0, min(255, c + d)) for c in base) + (255,))
    for (x, y), c in dots:
        img.putpixel((x, y), c + (255,))
    return img


def concrete():
    """Poured concrete: an even grey with fine aggregate flecks."""
    rng = random.Random(3201)
    dots = [((rng.randrange(16), rng.randrange(16)), rng.choice([(120, 122, 124), (176, 176, 172), (100, 102, 104)]))
            for _ in range(18)]
    return _speckle((148, 150, 150), 5, 3202, dots)


def blastproof_concrete():
    """Cast panel: darker concrete with formwork seams and the four tie-holes of the shuttering."""
    img = _speckle((122, 126, 128), 4, 3203)
    for i in range(16):
        img.putpixel((i, 0), (96, 100, 102, 255))
        img.putpixel((0, i), (96, 100, 102, 255))
        img.putpixel((i, 15), (150, 154, 156, 255))
        img.putpixel((15, i), (150, 154, 156, 255))
    for x, y in ((3, 3), (12, 3), (3, 12), (12, 12)):
        img.putpixel((x, y), (54, 56, 58, 255))
        img.putpixel((x + 1, y), (84, 86, 88, 255))
        img.putpixel((x, y + 1), (84, 86, 88, 255))
    return img


def construction_foam():
    """Expanded foam: pale yellow with round bubble pores."""
    rng = random.Random(3204)
    img = _speckle((226, 210, 150), 6, 3205)
    for _ in range(14):
        x, y = rng.randrange(1, 15), rng.randrange(1, 15)
        img.putpixel((x, y), (186, 168, 104, 255))
        img.putpixel((x - 1, y - 1), (246, 236, 190, 255))
    return img


def _px(img, x, y, c):
    img.putpixel((x, y), tuple(c) + (255,))


def cement_mix():
    """A heap of grey-brown powder with clay flecks."""
    rng = random.Random(3206)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(7, 15):
        half = (y - 6) * 0.9
        for x in range(16):
            if abs(x - 7.5) <= half:
                c = rng.choice([(150, 140, 126), (170, 160, 146), (132, 122, 110), (176, 120, 90)])
                if y == 14 or abs(x - 7.5) > half - 1:
                    c = tuple(v - 30 for v in c)
                _px(img, x, y, c)
    return img


def cement():
    """A paper sack of cement, folded at the top, with a grey band."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(3, 15):
        for x in range(3, 13):
            c = (214, 196, 150) if x < 9 else (190, 172, 128)
            if y in (8, 9, 10):
                c = (140, 142, 144) if x < 9 else (118, 120, 122)
            _px(img, x, y, c)
    for x in range(4, 12):
        _px(img, x, 2, (170, 152, 108))
    for y in range(3, 15):
        _px(img, 12, y, (150, 134, 96))
    for x in range(3, 13):
        _px(img, x, 14, (150, 134, 96))
    return img


def rebar():
    """Three ribbed steel bars laid diagonally."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for offset in (-3, 0, 3):
        for i in range(2, 14):
            x, y = i + offset, 15 - i
            if 0 <= x < 16 and 0 <= y < 16:
                _px(img, x, y, (96, 76, 62) if i % 2 else (132, 106, 86))
                if x + 1 < 16:
                    _px(img, x + 1, y, (70, 56, 46))
    return img


def foam_canister():
    """A spray can: yellow body, white band, black nozzle."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(4, 15):
        for x in range(5, 11):
            f = (x - 5) / 5
            c = (246, 214, 70) if f < 0.3 else (226, 188, 40) if f < 0.7 else (186, 150, 26)
            if y in (8, 9):
                c = (236, 236, 232) if f < 0.6 else (200, 200, 196)
            _px(img, x, y, c)
    for x in range(6, 10):
        _px(img, x, 3, (176, 180, 186))
    _px(img, 7, 2, (30, 30, 34))
    _px(img, 8, 2, (30, 30, 34))
    _px(img, 8, 1, (30, 30, 34))
    return img


def foam_sprayer():
    """A spray gun held like a tool: a steel nozzle up to the top right, a yellow canister and a rubber grip."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    steel = [(60, 64, 72), (110, 116, 126), (170, 176, 186)]
    for i in range(6):  # the lance
        x, y = 9 + i, 6 - i
        _px(img, x, y, steel[2])
        if y + 1 < 16:
            _px(img, x, y + 1, steel[0])
    _px(img, 15, 0, (226, 210, 150))
    for y in range(6, 11):  # body
        for x in range(5, 10):
            _px(img, x, y, steel[1] if y > 7 else steel[2])
    for y in range(3, 7):  # the canister on top
        for x in range(4, 7):
            _px(img, x, y, (236, 200, 50) if x < 6 else (196, 160, 30))
    for i in range(4):  # grip
        _px(img, 6 - i, 11 + i, (30, 30, 34))
        _px(img, 7 - i, 11 + i, (54, 54, 60))
    _px(img, 8, 11, steel[0])  # trigger
    return img


def draw_all(save):
    for block in BLOCKS:
        save(globals()[block](), "block", block)
    # Items are drawn at 64x64 with the high-detail renderer (tools/hd_art.py, tools/construction_art.py).
    import construction_art
    for item in ITEMS:
        save(construction_art.ITEMS[item](), "item", item)
