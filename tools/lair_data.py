"""JSON resources for the lairs (tools/lairs.py, docs/features/hollow-acre.md): the lair-only blocks, the Last Rites'
Mourning Wreath and Death Knell, the Mist Gate's name, the lairs' messages, and each lair's dimension (its dimension
type, level stem and biome) and structure template (tools/hollow_acre.py).

Called from tools/agriculture_data.py (assets, loot, recipes, tags, worldgen). Formats follow vanilla 26.3's own files:
the dimension type copies the shape of vanilla's End (logged from the running game by LairGameTests), the level stem a
flat generator with no layers, and the biome vanilla's void with no spawns.
"""
from decor_data import MOD, HORIZONTAL, rid, self_drop
import hollow_acre
from lairs import (FIXTURES, GATE, ITEMS, LAIR_BLOCKS, LAIRS, MOURNING_FLOWERS, RECIPES, RITE_CANDLES)


def cube(lo, hi, faces, texture, cull=None, shade=True, uvs=None):
    out = {"from": lo, "to": hi, "faces": {}}
    for face in faces:
        entry = {"texture": texture}
        if uvs and face in uvs:
            entry["uv"] = uvs[face]
        if cull and face in cull:
            entry["cullface"] = face
        out["faces"][face] = entry
    if not shade:
        out["shade"] = False
    return out


ALL = ("north", "south", "east", "west", "up", "down")


def brazier(lit):
    """A soul brazier: a squat black-stone pedestal, an iron bowl on it and, lit, a soul flame standing in the bowl."""
    bowl = cube([3, 8, 3], [13, 11, 13], ALL, "#iron")
    bowl["faces"]["up"]["texture"] = "#coals"  # the coals, seen inside the rim
    elements = [
        cube([5, 0, 5], [11, 2, 11], ALL, "#stone"),
        cube([6, 2, 6], [10, 8, 10], ALL, "#stone"),
        bowl,
        # The rim: four closed bars round the bowl's mouth.
        cube([2, 11, 2], [14, 12, 4], ALL, "#iron"),
        cube([2, 11, 12], [14, 12, 14], ALL, "#iron"),
        cube([2, 11, 4], [4, 12, 12], ALL, "#iron"),
        cube([12, 11, 4], [14, 12, 12], ALL, "#iron"),
    ]
    if lit:
        for angle in (45, -45):
            elements.append({"from": [3, 11, 8], "to": [13, 24, 8], "shade": False,
                             "rotation": {"origin": [8, 11, 8], "axis": "y", "angle": angle},
                             "faces": {"north": {"texture": "#flame", "uv": [0, 0, 16, 16]},
                                       "south": {"texture": "#flame", "uv": [0, 0, 16, 16]}}})
    textures = {"particle": rid("block/lair_brazier_stone"), "stone": rid("block/lair_brazier_stone"),
                "iron": rid("block/lair_brazier_iron"), "coals": rid("block/lair_brazier_coals")}
    if lit:
        textures["flame"] = rid("block/lair_brazier_flame")
    return {"parent": "minecraft:block/block", "textures": textures, "elements": elements}


def mist(axis):
    """Grey Mist: a curtain of mist across the middle of the block, see-through and without collision."""
    texture = {"force_translucent": True, "sprite": rid("block/lair_exit")}
    curtain = {"from": [0, 0, 7], "to": [16, 16, 9], "shade": False,
               "faces": {"north": {"texture": "#mist"}, "south": {"texture": "#mist"}}}
    return {"parent": "minecraft:block/block", "textures": {"particle": texture, "mist": texture}, "elements": [curtain]}


def wreath():
    """The Mourning Wreath lying flat: a ring of dark leaves and pale flowers on the ground, its ribbon trailing."""
    texture = rid("block/mourning_wreath")
    return {"parent": "minecraft:block/block", "textures": {"particle": texture, "wreath": texture},
            "elements": [{"from": [1, 0.25, 1], "to": [15, 1.25, 15], "faces": {
                "up": {"texture": "#wreath", "uv": [0, 0, 16, 16]},
                "down": {"texture": "#wreath", "uv": [0, 16, 16, 0]},
                "north": {"texture": "#wreath", "uv": [0, 7, 16, 8]}, "south": {"texture": "#wreath", "uv": [0, 8, 16, 9]},
                "east": {"texture": "#wreath", "uv": [7, 0, 8, 16]}, "west": {"texture": "#wreath", "uv": [8, 0, 9, 16]}}}]}


def assets(root, write, lang):
    models, states, items = root / "models" / "block", root / "blockstates", root / "items"

    def simple(name, model):
        write(models / f"{name}.json", model)
        write(states / f"{name}.json", {"variants": {"": {"model": rid(f"block/{name}")}}})

    simple("blighted_soil", {"parent": "minecraft:block/cube_bottom_top", "textures": {
        "top": rid("block/blighted_soil_top"), "side": rid("block/blighted_soil_side"), "bottom": rid("block/blighted_soil")}})
    for plant in ("black_wheat", "mown_stubble"):
        simple(plant, {"parent": "minecraft:block/crop", "textures": {"crop": rid(f"block/{plant}")}})
    write(models / "lair_brazier.json", brazier(True))
    write(models / "lair_brazier_unlit.json", brazier(False))
    write(states / "lair_brazier.json", {"variants": {"lit=true": {"model": rid("block/lair_brazier")},
                                                      "lit=false": {"model": rid("block/lair_brazier_unlit")}}})
    for name in ("lair_moon", "lair_moon_red"):
        write(models / f"{name}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": rid(f"block/{name}")}})
    write(states / "lair_moon.json", {"variants": {"red=false": {"model": rid("block/lair_moon")},
                                                   "red=true": {"model": rid("block/lair_moon_red")}}})
    write(models / "lair_exit.json", mist("x"))
    write(states / "lair_exit.json", {"variants": {"axis=x": {"model": rid("block/lair_exit")},
                                                   "axis=z": {"model": rid("block/lair_exit"), "y": 90}}})
    for name, display in LAIR_BLOCKS.items():
        lang[f"block.{MOD}.{name}"] = display

    # The Mourning Wreath (a block laid on a grave) and the Death Knell.
    write(models / "mourning_wreath.json", wreath())
    write(states / "mourning_wreath.json", {"variants": {f"facing={f}": {"model": rid("block/mourning_wreath"), **(
        {"y": {"north": 0, "east": 90, "south": 180, "west": 270}[f]} if f != "north" else {})} for f in HORIZONTAL}})
    for item in ITEMS:
        write(root / "models" / "item" / f"{item}.json", {"parent": "minecraft:item/generated",
                                                          "textures": {"layer0": rid(f"item/{item}")}})
        write(items / f"{item}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{item}")}})
    lang[f"block.{MOD}.mourning_wreath"] = ITEMS["mourning_wreath"]
    lang[f"item.{MOD}.death_knell"] = ITEMS["death_knell"]
    lang[f"tooltip.{MOD}.death_knell"] = "Rung at a grave at night, among lit candles, with a wreath laid on it"
    lang[f"tooltip.{MOD}.mourning_wreath"] = "Lay it on a grave for the Last Rites"
    lang[f"entity.{MOD}.{GATE['entity']}"] = GATE["display"]

    for lair, info in LAIRS.items():
        lang[f"lair.{MOD}.{lair}"] = info["display"]
    lang.update({
        f"message.{MOD}.lair.rite.no_grave": "The knell rings out over no grave",
        f"message.{MOD}.lair.rite.not_overworld": "The Last Rites are said only in the Overworld",
        f"message.{MOD}.lair.rite.not_night": "The Last Rites are said at night",
        f"message.{MOD}.lair.rite.candles": "The grave needs more lit candles round it: %s of %s",
        f"message.{MOD}.lair.rite.no_wreath": "Lay a Mourning Wreath on the grave first",
        f"message.{MOD}.lair.rite.off_season": "The Last Rites open the Hollow Acre only in the Halloween season",
        f"message.{MOD}.lair.rite.disabled": "The lairs are switched off on this server",
        f"message.{MOD}.lair.rite.opened": "The candles flare blue, and the mist opens over the grave",
        f"message.{MOD}.lair.full": "Every %s is taken; try again soon",
        f"message.{MOD}.lair.party_full": "%s is full",
        f"message.{MOD}.lair.gate_closed": "The mist has closed",
        f"message.{MOD}.lair.already_inside": "You are already in a lair",
        f"message.{MOD}.lair.enter": "You step through the mist into %s",
        f"message.{MOD}.lair.leave": "You step back out of the mist",
        f"message.{MOD}.lair.left_behind": "Anything left lying in a lair is lost when it closes",
        f"message.{MOD}.lair.closed": "The lair has closed; you are back where you were",
        f"message.{MOD}.lair.edge": "The mist throws you back",
        f"message.{MOD}.lair.protected": "Nothing in a lair can be changed",
        f"message.{MOD}.lair.grave_goods.kept": "Your belongings are gathered up as Grave Goods",
        f"message.{MOD}.lair.grave_goods.returned": "Your Grave Goods are returned to you",
        f"commands.{MOD}.lair.not_inside": "You are not in a lair",
        f"commands.{MOD}.lair.list.none": "No lair is open",
        f"commands.{MOD}.lair.list.entry": "%s, slot %s: %s inside, open %s seconds",
        f"commands.{MOD}.lair.close.done": "Closed %s, slot %s",
        f"commands.{MOD}.lair.close.none": "%s has no open slot %s",
    })


def loot(out, write):
    """The wreath gives itself; the lair-only blocks give nothing (they cannot be broken, and none may leave a lair)."""
    write(out / "mourning_wreath.json", self_drop("mourning_wreath"))
    for block in LAIR_BLOCKS:
        write(out / f"{block}.json", {"type": "minecraft:block", "pools": [], "random_sequence": rid(f"blocks/{block}")})


def recipes(out, write, conditions):
    for item, recipe in RECIPES.items():
        write(out / f"{item}.json", {"fabric:load_conditions": conditions(), "type": "minecraft:crafting_shaped",
                                     "category": "misc", "pattern": recipe["pattern"], "key": recipe["key"],
                                     "result": {"id": rid(item), "count": 1}})


def tags(tags):
    for flower in MOURNING_FLOWERS:
        tags.add("item", f"{MOD}:mourning_flowers", flower)
    for candle in RITE_CANDLES:
        tags.add("block", f"{MOD}:last_rites_candles", candle)
    for block in LAIR_BLOCKS:
        tags.add("block", f"{MOD}:lair_blocks", rid(block))
        tags.add("block", "minecraft:wither_immune", rid(block))
        tags.add("block", "minecraft:dragon_immune", rid(block))
    for block in FIXTURES:
        tags.add("block", f"{MOD}:lair_fixtures", rid(block))
    # Blighted soil is earth: plants stand on it.
    tags.add("block", "minecraft:dirt", rid("blighted_soil"))
    tags.add("block", "minecraft:mineable/hoe", rid("mourning_wreath"))


def dimension_type(info):
    """Night that never ends: a fixed time, no skybox (no sun, moon or stars, only the sky's colour), sky light that adds
    nothing, a little ambient light, no beds or respawn anchors, no raids, the lair's music, sky and fog."""
    never = {"can_sleep": "never", "can_set_spawn": "never", "destroy_on_use": False}
    return {
        "has_fixed_time": True, "has_skylight": True, "has_ceiling": False, "has_ender_dragon_fight": False,
        "coordinate_scale": 1.0, "min_y": 0, "height": 256, "logical_height": 256,
        "infiniburn": "#minecraft:infiniburn_overworld", "ambient_light": info["ambient_light"],
        "monster_spawn_light_level": 0, "monster_spawn_block_light_limit": 0, "skybox": "none",
        "attributes": {
            "minecraft:audio/background_music": {"default": {"sound": info["music"], "min_delay": 3000, "max_delay": 9000,
                                                             "replace_current_music": True}},
            "minecraft:gameplay/bed_rule": never,
            "minecraft:gameplay/straw_bed_rule": never,
            "minecraft:gameplay/respawn_anchor_works": False,
            "minecraft:gameplay/can_start_raid": False,
            "minecraft:visual/ambient_light_color": "#2a2030",
            "minecraft:visual/fog_color": info["fog"],
            "minecraft:visual/sky_color": info["sky"],
            "minecraft:visual/sky_light_color": "#3a3050",
            "minecraft:visual/sky_light_factor": 0.0,
        },
        "timelines": [],
    }


def biome(info):
    """No spawns, nothing generated, drifting ash; the sky and fog of the dimension (a biome's would win)."""
    return {
        "has_precipitation": False, "temperature": 0.5, "downfall": 0.0,
        "attributes": {
            "minecraft:gameplay/natural_mob_spawns": {"argument": {"spawns_by_category": {}, "spawn_costs": {}},
                                                      "modifier": "overlay"},
            "minecraft:visual/sky_color": info["sky"],
            "minecraft:visual/fog_color": info["fog"],
            "minecraft:visual/water_fog_color": info["water_fog"],
            "minecraft:visual/ambient_particles": [{"particle": {"type": "minecraft:white_ash"}, "probability": 0.006}],
        },
        "effects": {"water_color": "#3f3150"},
        "carvers": [],
        "features": [],
    }


def worldgen(data, write):
    for lair, info in LAIRS.items():
        write(data / MOD / "dimension_type" / f"{lair}.json", dimension_type(info))
        write(data / MOD / "worldgen" / "biome" / f"{lair}.json", biome(info))
        write(data / MOD / "dimension" / f"{lair}.json", {"type": rid(lair), "generator": {
            "type": "minecraft:flat", "settings": {"layers": [{"height": 1, "block": "minecraft:air"}], "lakes": False,
                                                   "features": False, "biome": rid(lair)}}})
    hollow_acre.write(data / MOD / "structure" / f"{hollow_acre.TEMPLATE}.nbt")
