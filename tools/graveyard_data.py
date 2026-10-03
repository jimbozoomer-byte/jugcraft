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
from graveyard import FEATURE, STAGES, HEADSTONES, STONES, EPITAPH, CHISEL_RECIPE
import graveyard_models as gm

MODELS = {"gothic": gm.gothic, "willow_urn": gm.willow_urn, "winged_skull": gm.winged_skull, "lamb": gm.lamb,
          "broken_column": gm.broken_column, "celtic_cross": gm.celtic_cross, "scroll": gm.scroll, "table_tomb": gm.table_tomb,
          "ledger": gm.ledger}
AXES = {"north": None, "south": None, "east": None, "west": None}
FACE_AXES = {0: ("west", "east"), 1: ("down", "up"), 2: ("north", "south")}


def textures(stone, stage, upper):
    """The texture variables for `stone` at `stage`; above the first block every face is moss-free from the ground."""
    ground = f"{stage}_upper" if upper else stage
    out = {"stone": f"gy_{stone}_{ground}", "top": f"gy_{stone}_{stage}_upper", "relief": f"gy_{stone}_relief_{ground}",
           "relief_top": f"gy_{stone}_relief_{stage}_upper", "ivy": "gy_ivy"}
    if stone == "granite":
        out.update({"rough": f"gy_granite_rough_{stage}", "knot": f"gy_granite_knot_{stage}_upper"})
    return out


def whole(headstone, stage):
    """Every element of a headstone at `stage`, before it is cut into blocks."""
    info = HEADSTONES[headstone]
    elements = MODELS[info["model"]]()
    if stage == "overgrown":
        elements = elements + gm.overgrowth(info["overgrowth"])
    return elements


def cut(elements, cell, axis):
    """The part of `elements` in the block at `cell` (right, up, back), moved into it. `axis` is the way the memorial
    runs (1: up, 2: back); boxes are cut only along it."""
    shift = cell[axis] * 16
    out = []
    for element in elements:
        frm, to = element["from"], element["to"]
        if "rotation" in element:
            middle = (frm[axis] + to[axis]) / 2
            if shift <= middle < shift + 16 or (cell[axis] == 0 and middle < 0):
                moved = {**element, "from": list(frm), "to": list(to), "rotation": dict(element["rotation"])}
                moved["from"][axis] -= shift
                moved["to"][axis] -= shift
                origin = list(moved["rotation"]["origin"])
                origin[axis] -= shift
                moved["rotation"]["origin"] = origin
                out.append(moved)
            continue
        lo, hi = max(frm[axis], shift), min(to[axis], shift + 16)
        if cell[axis] == 0:
            lo = frm[axis] if frm[axis] < 0 else lo
        if hi - lo <= 1e-6:
            continue
        faces = dict(element["faces"])
        low_face, high_face = FACE_AXES[axis]
        if lo > frm[axis]:
            faces.pop(low_face, None)
        if hi < to[axis]:
            faces.pop(high_face, None)
        if not faces:
            continue
        new_from, new_to = list(frm), list(to)
        new_from[axis], new_to[axis] = round(lo - shift, 4), round(hi - shift, 4)
        out.append({**element, "from": new_from, "to": new_to, "faces": faces})
    return out


def axis_of(cells):
    if len(cells) == 1:
        return 1
    return 1 if any(c[1] for c in cells) else 2


def model_name(headstone, part, stage):
    return f"{headstone}_{stage}" if len(HEADSTONES[headstone]["cells"]) == 1 else f"{headstone}_{part}_{stage}"


def part_model(headstone, part, stage):
    info = HEADSTONES[headstone]
    cells = info["cells"]
    elements = whole(headstone, stage)
    if len(cells) > 1:
        elements = cut(elements, cells[part], axis_of(cells))
    tex = textures(info["stone"], stage, upper=cells[part][1] > 0)
    used = {face["texture"][1:] for e in elements for face in e["faces"].values()}
    tex = {k: v for k, v in tex.items() if k in used}
    particle = tex.get("stone") or tex.get("rough") or next(iter(tex.values()))
    return fitted(block_model(tex, elements, particle))


def item_model(headstone):
    """A memorial of more than one block, scaled down whole to fit an item's square."""
    info = HEADSTONES[headstone]
    elements = whole(headstone, "clean")
    tex = textures(info["stone"], "clean", upper=True)
    used = {face["texture"][1:] for e in elements for face in e["faces"].values()}
    tex = {k: v for k, v in tex.items() if k in used}
    height = max(e["to"][1] for e in elements)
    length = max(e["to"][2] for e in elements)
    factor = min(1.0, 16 / max(height, length))
    offset = (8 * (1 - factor), 0, 8 * (1 - factor) if length <= 16 else (16 - length * factor) / 2)
    return fitted(block_model(tex, scaled(elements, factor, offset), tex.get("stone") or tex.get("rough")))


def assets(root, write, lang):
    models = root / "models" / "block"
    for headstone, info in HEADSTONES.items():
        cells = info["cells"]
        for part in range(len(cells)):
            for stage in STAGES:
                write(models / f"{model_name(headstone, part, stage)}.json", part_model(headstone, part, stage))
        variants = {}
        for facing in HORIZONTAL:
            for part in range(3):
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
