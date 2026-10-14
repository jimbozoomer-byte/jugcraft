"""Writes Adam-ondi-Ahman's data from tools/adam_ondi_ahman.py: the knoll's structure, structure set, template pool and
template, the Ancient Altar's block assets, loot table and tags, and names. The valley's biome is tools/biomes.py's
(placed round its sites, "placement": "site"). Called by agriculture_data.py, as the lairs' data is."""
import json

import adam_ondi_ahman as aa

MOD = "jugcraft"


def rid(path):
    return path if ":" in path else f"{MOD}:{path}"


def assets(root, write, lang):
    models, states = root / "models" / "block", root / "blockstates"
    write(models / f"{aa.ALTAR}.json", {"parent": "minecraft:block/cube_bottom_top", "textures": {
        "top": rid(f"block/{aa.ALTAR}_top"), "side": rid(f"block/{aa.ALTAR}_side"), "bottom": rid(f"block/{aa.ALTAR}_bottom")}})
    write(states / f"{aa.ALTAR}.json", {"variants": {"": {"model": rid(f"block/{aa.ALTAR}")}}})
    lang[f"block.{MOD}.{aa.ALTAR}"] = aa.ALTAR_DISPLAY
    lang[f"message.{MOD}.adam_ondi_ahman.dormant"] = aa.DORMANT


def loot(out, write):
    """The altar gives nothing: it cannot be broken."""
    write(out / f"{aa.ALTAR}.json", {"type": "minecraft:block", "pools": [], "random_sequence": rid(f"blocks/{aa.ALTAR}")})


def tags(tags):
    for tag in ("minecraft:wither_immune", "minecraft:dragon_immune", "minecraft:features_cannot_replace"):
        tags.add("block", tag, rid(aa.ALTAR))


def structure():
    """The knoll: vanilla's jigsaw structure of the one template, started under the land's surface at its middle and
    blended into the land round it (beard_thin), in the valley only."""
    return {"type": "minecraft:jigsaw", "biomes": f"#{aa.STRUCTURE_TAG}", "max_distance_from_center": 64,
            "project_start_to_heightmap": "WORLD_SURFACE_WG", "size": 1, "spawn_overrides": {},
            "start_height": {"absolute": aa.START_HEIGHT}, "start_pool": rid(aa.POOL), "step": "surface_structures",
            "terrain_adaptation": "beard_thin", "use_expansion_hack": False}


def structure_set():
    return {"placement": {"type": rid(aa.PLACEMENT)}, "structures": [{"structure": rid(aa.STRUCTURE), "weight": 1}]}


def template_pool():
    return {"fallback": "minecraft:empty", "elements": [{"weight": 1, "element": {
        "element_type": "minecraft:single_pool_element", "location": rid(aa.TEMPLATE), "processors": "minecraft:empty",
        "projection": "rigid"}}]}


def worldgen(data, write):
    folder = data / MOD / "worldgen"
    write(folder / "structure" / f"{aa.STRUCTURE}.json", structure())
    write(folder / "structure_set" / f"{aa.STRUCTURE_SET}.json", structure_set())
    # Written as it is: the shared writer takes anything with "elements" for a block model.
    pool = folder / "template_pool" / f"{aa.POOL}.json"
    pool.parent.mkdir(parents=True, exist_ok=True)
    pool.write_text(json.dumps(template_pool(), indent=2) + "\n", encoding="utf-8")
    aa.write(data / MOD / "structure" / f"{aa.TEMPLATE}.nbt")
