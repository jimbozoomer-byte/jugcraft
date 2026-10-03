"""JSON resources for the graveyard pack, from tools/graveyard.py: every headstone's models (one per block of it and
stage of weathering), blockstates, item models, words, loot (the epitaph goes with the item), recipes and tags.

Called from agriculture_data.py. Formats follow vanilla Minecraft 26.3's own files: blockstate variants, block models,
loot with copy_components from the block entity, stonecutting and shaped recipes. A memorial taller or longer than one
block is cut into one model per block: unturned boxes are cut at the block's edges (only along the way it is tall or
long, so arms and finials may reach out over the blocks beside it), turned boxes go whole to the block their middle is
in. Faces reaching outside a block's texture square are given UVs inside it (decor3_data.fitted).
"""
from decor_data import MOD, HORIZONTAL, rid, turned, block_model
from decor3_data import fitted
from decor7_data import scaled
from graveyard import FEATURE, STAGES, HEADSTONES, STONES, EPITAPH, CHISEL_RECIPE, PARTS
import graveyard_models as gm

MODELS = {"gothic": gm.gothic, "willow_urn": gm.willow_urn, "winged_skull": gm.winged_skull, "lamb": gm.lamb,
          "broken_column": gm.broken_column, "celtic_cross": gm.celtic_cross, "scroll": gm.scroll, "table_tomb": gm.table_tomb,
          "ledger": gm.ledger, "obelisk": gm.obelisk, "draped_urn": gm.draped_urn, "angel_of_grief": gm.angel_of_grief,
          "trumpet_angel": gm.trumpet_angel, "mortsafe": gm.mortsafe, "hound": gm.hound}
AXES = {"north": None, "south": None, "east": None, "west": None}
FACE_AXES = {0: ("west", "east"), 1: ("down", "up"), 2: ("north", "south")}


def textures(stone, stage, upper):
    """The texture variables for `stone` at `stage`; above the first block every face is moss-free from the ground."""
    if stone == "iron":
        return {"iron": f"gy_iron_{stage}", "soil": "minecraft:block/coarse_dirt", "ivy": "gy_ivy"}
    ground = f"{stage}_upper" if upper else stage
    out = {"stone": f"gy_{stone}_{ground}", "top": f"gy_{stone}_{stage}_upper", "relief": f"gy_{stone}_relief_{ground}",
           "relief_top": f"gy_{stone}_relief_{stage}_upper", "ivy": "gy_ivy"}
    if stone == "granite":
        out.update({"rough": f"gy_granite_rough_{stage}", "knot": f"gy_granite_knot_{stage}_upper"})
    # Metals weather with the stone they stand on; the soil in a mortsafe is vanilla's coarse dirt.
    out.update({"iron": f"gy_iron_{stage}", "bronze": f"gy_bronze_{stage}", "soil": "minecraft:block/coarse_dirt"})
    return out


def whole(headstone, stage):
    """Every element of a headstone at `stage`, before it is cut into blocks."""
    info = HEADSTONES[headstone]
    elements = MODELS[info["model"]]()
    if stage == "overgrown":
        elements = elements + gm.overgrowth(info["overgrowth"])
    return elements


def size_of(cells):
    """A memorial's size in blocks: right, up, back."""
    return tuple(max(c[k] for c in cells) + 1 for k in range(3))


def cut(elements, cell, size):
    """The part of `elements` in the block at `cell` (right, up, back), moved into it. The memorial is designed `size`
    blocks wide, tall and long, facing north: its first column to the placer's right (-x) is design x 16 * (width - 1);
    up is +y; back is +z. Boxes are cut only along the ways it is more than a block, so arms and finials may reach out
    over the blocks beside it; turned boxes go whole to the block their middle is in."""
    width = size[0]
    shift = [16 * (width - 1 - cell[0]), 16 * cell[1], 16 * cell[2]]
    axes = [k for k in range(3) if size[k] > 1]
    out = []
    for element in elements:
        frm, to = element["from"], element["to"]
        if "rotation" in element:
            inside = True
            for k in axes:
                middle = (frm[k] + to[k]) / 2
                index = middle // 16 if k else (width - 1) - middle // 16
                limit = size[k] - 1
                index = min(max(index, 0), limit)
                want = cell[k]
                inside &= index == want
            if inside:
                moved = {**element, "from": [frm[k] - shift[k] for k in range(3)], "to": [to[k] - shift[k] for k in range(3)],
                         "rotation": dict(element["rotation"])}
                moved["rotation"]["origin"] = [element["rotation"]["origin"][k] - shift[k] for k in range(3)]
                out.append(moved)
            continue
        lo, hi = list(frm), list(to)
        faces = dict(element["faces"])
        empty = False
        for k in axes:
            cell_lo, cell_hi = shift[k], shift[k] + 16
            # The outermost blocks keep whatever reaches past the memorial's edge.
            first = shift[k] == 0
            last = shift[k] == 16 * (size[k] - 1)
            a = frm[k] if (first and frm[k] < cell_lo) else max(frm[k], cell_lo)
            z = to[k] if (last and to[k] > cell_hi) else min(to[k], cell_hi)
            if z - a <= 1e-6:
                empty = True
                break
            low_face, high_face = FACE_AXES[k]
            if a > frm[k]:
                faces.pop(low_face, None)
            if z < to[k]:
                faces.pop(high_face, None)
            lo[k], hi[k] = a, z
        if empty or not faces:
            continue
        out.append({**element, "from": [round(lo[k] - shift[k], 4) for k in range(3)],
                    "to": [round(hi[k] - shift[k], 4) for k in range(3)], "faces": faces})
    return out


def model_name(headstone, part, stage):
    return f"{headstone}_{stage}" if len(HEADSTONES[headstone]["cells"]) == 1 else f"{headstone}_{part}_{stage}"


def part_model(headstone, part, stage):
    info = HEADSTONES[headstone]
    cells = info["cells"]
    elements = whole(headstone, stage)
    if len(cells) > 1:
        elements = cut(elements, cells[part], size_of(cells))
    tex = textures(info["stone"], stage, upper=cells[part][1] > 0)
    used = {face["texture"][1:] for e in elements for face in e["faces"].values()}
    tex = {k: v for k, v in tex.items() if k in used}
    particle = tex.get("stone") or tex.get("rough") or tex.get("iron") or next(iter(tex.values()))
    return fitted(block_model(tex, elements, particle))


def item_model(headstone):
    """A memorial of more than one block, scaled down whole to fit an item's square."""
    info = HEADSTONES[headstone]
    elements = whole(headstone, "clean")
    tex = textures(info["stone"], "clean", upper=True)
    used = {face["texture"][1:] for e in elements for face in e["faces"].values()}
    tex = {k: v for k, v in tex.items() if k in used}
    width = max(e["to"][0] for e in elements)
    height = max(e["to"][1] for e in elements)
    length = max(e["to"][2] for e in elements)
    factor = min(1.0, 16 / max(width, height, length))
    offset = ((16 - width * factor) / 2 if width > 16 else 8 * (1 - factor), 0,
              (16 - length * factor) / 2 if length > 16 else 8 * (1 - factor))
    particle = tex.get("stone") or tex.get("rough") or tex.get("iron") or next(iter(tex.values()))
    return fitted(block_model(tex, scaled(elements, factor, offset), particle))


def assets(root, write, lang):
    models = root / "models" / "block"
    for headstone, info in HEADSTONES.items():
        cells = info["cells"]
        for part in range(len(cells)):
            for stage in STAGES:
                write(models / f"{model_name(headstone, part, stage)}.json", part_model(headstone, part, stage))
        variants = {}
        for facing in HORIZONTAL:
            for part in range(PARTS):
                for weathering, stage in enumerate(STAGES):
                    for waxed in ("false", "true"):
                        model = rid(f"block/{model_name(headstone, min(part, len(cells) - 1), stage)}")
                        variants[f"facing={facing},part={part},waxed={waxed},weathering={weathering}"] = turned(model, facing)
        write(root / "blockstates" / f"{headstone}.json", {"variants": variants})
        if len(cells) == 1:
            item = rid(f"block/{model_name(headstone, 0, 'clean')}")
        else:
            write(models / f"{headstone}_item.json", item_model(headstone))
            item = rid(f"block/{headstone}_item")
        write(root / "items" / f"{headstone}.json", {"model": {"type": "minecraft:model", "model": item}})
        lang[f"block.{MOD}.{headstone}"] = info["display"]
    chisel = EPITAPH["chisel"]
    write(root / "models" / "item" / f"{chisel}.json", {"parent": "minecraft:item/handheld", "textures": {"layer0": rid(f"item/{chisel}")}})
    write(root / "items" / f"{chisel}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{chisel}")}})
    lang[f"item.{MOD}.{chisel}"] = EPITAPH["chisel_display"]
    lang.update({
        "screen.jugcraft.epitaph.title": "Cut an Epitaph",
        "screen.jugcraft.epitaph.line": "Line %s",
        "screen.jugcraft.epitaph.hint": "Each line is cut as large as it fits.",
        "tooltip.jugcraft.headstone.epitaph": "Epitaph:",
        "tooltip.jugcraft.headstone.waxed": "Waxed",
        "tooltip.jugcraft.chisel": "Use on a headstone to cut its epitaph",
        "message.jugcraft.epitaph.engraved": "The epitaph is cut",
        "message.jugcraft.epitaph.no_session": "Open the headstone with the chisel again",
        "message.jugcraft.epitaph.no_chisel": "You need the Stonemason's Chisel in hand",
        "message.jugcraft.epitaph.too_far": "Too far from the headstone",
        "message.jugcraft.epitaph.not_allowed": "You may not cut stone here",
        "message.jugcraft.epitaph.not_a_headstone": "That is no longer a headstone",
        "message.jugcraft.epitaph.invalid": "An epitaph has at most 4 lines of 24 characters",
        "message.jugcraft.headstone.waxed": "It is waxed and will not weather",
        "message.jugcraft.headstone.clean": "It is as clean as the day it was cut",
        "message.jugcraft.headstone.overgrown": "It cannot grow any wilder",
    })


def loot(out, write):
    for headstone in HEADSTONES:
        write(out / f"{headstone}.json", {"type": "minecraft:block", "pools": [{
            "condition": {"type": "minecraft:all_of", "terms": [
                {"type": "minecraft:survives_explosion"},
                {"type": "minecraft:match_block", "blocks": rid(headstone), "state": {"part": "0"}}]},
            "entries": [{"type": "minecraft:item", "name": rid(headstone), "modifier": [
                {"type": "minecraft:copy_components", "include": [rid(EPITAPH["component"])], "source": "block_entity"}]}],
            "rolls": 1}], "random_sequence": f"{MOD}:blocks/{headstone}"})


def recipes(out, write, conditions):
    for headstone, info in HEADSTONES.items():
        recipe = info["recipe"]
        if "stonecutting" in recipe:
            write(out / f"{headstone}_from_stonecutting.json", {"fabric:load_conditions": conditions(), "type": "minecraft:stonecutting",
                                                                "ingredient": recipe["stonecutting"], "result": {"id": rid(headstone), "count": 1}})
        else:
            write(out / f"{headstone}.json", {"fabric:load_conditions": conditions(), "type": "minecraft:crafting_shaped",
                                              "category": "building", "pattern": recipe["pattern"], "key": recipe["key"],
                                              "result": {"id": rid(headstone), "count": 1}})
    write(out / f"{EPITAPH['chisel']}.json", {"fabric:load_conditions": conditions(), "type": "minecraft:crafting_shaped",
                                             "category": "equipment", "pattern": CHISEL_RECIPE["pattern"], "key": CHISEL_RECIPE["key"],
                                             "result": {"id": rid(EPITAPH["chisel"]), "count": 1}})


def tags(tags):
    for headstone in HEADSTONES:
        tags.add("block", "minecraft:mineable/pickaxe", rid(headstone))
        tags.add("block", "jugcraft:headstones", rid(headstone))
        tags.add("item", "jugcraft:headstones", rid(headstone))
