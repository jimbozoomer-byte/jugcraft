"""Pipeworks (docs/features/pipeworks.md): the data, textures, block models and Blockbench projects of the
twelve pipe and tank props in tools/pipeworks_models.py.

Each prop is one multi-block decoration (building/PipeworksBlock.java): its whole model is cut into one model a block
(model_writer.split_model, the pieces of one cut element kept together so no step opens at a seam), its blockstate
picks the block's model by part and turns it by facing, the master block alone drops it, and its item is the whole prop
scaled into a slot. The whole model is also saved as a Blockbench project (art/pipeworks/<id>.bbmodel, the free format:
every box in structure space with the textures embedded), to open, review and edit in Blockbench; a changed project is
not read back, so a change there is carried into tools/pipeworks_models.py to keep the models reproducible.

Textures (16 x 16, drawn here in the Dieselworks steel palette, tools/dieselworks.STEEL; no noise, no rust):
  pw_steel   an even brushed steel for every pipe, tank shell and post: no edge seam, so a stepped cylinder reads as
             one smooth tube (docs/ART_DIRECTION.md, Tubes and barrels)
  pw_bore    the dark open mouth of a pipe
  pw_flange  dark flange steel with raised bolt heads on a four-pixel lattice, so any flange face shows bolts
  pw_tread   tread plate: raised lozenges on a four-pixel lattice, for walkways and floors
"""
import json
import sys
from pathlib import Path

import clean_metal
from dieselworks import STEEL, shaped
from model_writer import FACING_Y, PART_STATES, rid, scaled_elements, split_model, texture_names
from pipeworks_models import MODELS, PROPS, cells, footprint

MOD = "jugcraft"
ROOT = Path(__file__).resolve().parents[1]
ART = ROOT / "art" / "pipeworks"
PARTICLE = "pw_steel"
STRENGTH = (5.0, 6.0)
PLATE_TAG = "#c:plates/steel"
PIPE = f"{MOD}:steel_fluid_pipe"
BEAM = f"{MOD}:steel_i_beam"

# (pattern, key, count): steel plates, steel fluid pipes and I-beams make each prop.
RECIPES = {
    "pipe_stand_run": (["FFF", "S S"], {"F": PIPE, "S": PLATE_TAG}, 1),
    "blind_flange_stub": (["SF", "SS"], {"F": PIPE, "S": PLATE_TAG}, 1),
    "flanged_pipe": (["FFF", "SSS"], {"F": PIPE, "S": PLATE_TAG}, 1),
    "pipe_rack": (["FFF", "FFF", "S S"], {"F": PIPE, "S": PLATE_TAG}, 1),
    "pipe_bridge": (["FFF", "BBB", "S S"], {"F": PIPE, "B": BEAM, "S": PLATE_TAG}, 1),
    "standpipe_frame": (["FFF", "BFB", "SSS"], {"F": PIPE, "B": BEAM, "S": PLATE_TAG}, 1),
    "horizontal_tank": (["SSS", "SFS", "SSS"], {"F": PIPE, "S": PLATE_TAG}, 1),
    "tank_walkway": (["BBB", "SFS", "SSS"], {"F": PIPE, "B": BEAM, "S": PLATE_TAG}, 1),
    "stacked_tanks": (["SSS", "BFB", "SSS"], {"F": PIPE, "B": BEAM, "S": PLATE_TAG}, 1),
    "pipeline_hoops": (["FFF", "BBB", "S S"], {"F": PIPE, "B": BEAM, "S": PLATE_TAG}, 1),
    "ribbed_drum": (["SSS", "FSF", "BBB"], {"F": PIPE, "B": BEAM, "S": PLATE_TAG}, 1),
    "pipe_overpass": (["FFF", "B B", "B B"], {"F": PIPE, "B": BEAM}, 1),
}


def blocks():
    return list(PROPS)


def items():
    return []


def size_text(size):
    w, h, d = size
    return f"{w} wide, {h} tall, {d} long"


# ------------------------------------------------------------------ textures

def steel():
    """Even brushed steel: the fill with short streaks one shade up, staggered so they tile without a seam."""
    img = clean_metal.canvas(STEEL[2])
    for x0, x1, y in ((1, 6, 2), (9, 14, 5), (3, 8, 8), (11, 15, 11), (0, 3, 11), (5, 10, 14)):
        clean_metal.rect(img, x0, y, x1, y, STEEL[3])
    return img


def bore():
    """The open mouth of a pipe: dark, with the wall's rim one shade lighter round the edge."""
    img = clean_metal.canvas(STEEL[0])
    for i in range(16):
        clean_metal.put(img, i, 0, STEEL[1])
        clean_metal.put(img, 0, i, STEEL[1])
    return img


def flange_steel():
    """Flange steel: a darker sheet with raised bolt heads every four pixels, so any face cut from it shows bolts."""
    img = clean_metal.canvas(STEEL[1])
    for x in (1, 5, 9, 13):
        for y in (1, 5, 9, 13):
            clean_metal.bolt(img, x, y, (STEEL[0], STEEL[3], STEEL[4]))
    return img


def tread():
    """Tread plate: a raised two-pixel diagonal lozenge every four pixels, staggered course by course, each lit at its
    top left with its shadow one shade under the fill."""
    img = clean_metal.canvas(STEEL[2])
    for y in range(0, 16, 4):
        for x in range(0, 16, 4):
            ox = x + (2 if (y // 4) % 2 else 0)
            clean_metal.put(img, (ox + 1) % 16, y + 1, STEEL[4])
            clean_metal.put(img, (ox + 2) % 16, y + 2, STEEL[3])
            clean_metal.put(img, (ox + 2) % 16, y + 1, STEEL[3])
            clean_metal.put(img, (ox + 3) % 16, y + 2, STEEL[1])
    return img


TEXTURES = {"pw_steel": steel, "pw_bore": bore, "pw_flange": flange_steel, "pw_tread": tread}


def draw_all(save):
    for name, draw in TEXTURES.items():
        save(draw(), "block", name)


# ------------------------------------------------------------------ models and data

def part_models(name):
    """The prop's elements cut into one list a block, in part order."""
    return split_model(name, MODELS[name], footprint(PROPS[name][1]))


def textures_of(elements):
    return {**{t: rid(f"block/{t}") for t in texture_names(elements)}, "particle": rid(f"block/{PARTICLE}")}


def drawn_together():
    """For tools/art_check.py: every prop's part models at their block offsets (pixels)."""
    out = {}
    for name, (_, size, _, _) in PROPS.items():
        out[name] = [(f"{name}_part{index}", tuple(16 * o for o in offset)) for index, offset in enumerate(footprint(size))]
    return out


def part_count(size):
    return len(cells(size))


def java_cells(size):
    """The prop's size as the Java registration writes it."""
    w, h, d = size
    return f"{w}, {h}, {d}"


def java_boxes(name):
    """The collision boxes as building/Pipeworks.java writes them: in the frame of the whole prop facing north, x across
    to the placer's left from its far block (structure x + 16 x (across - 1)), y up, z away."""
    w = PROPS[name][1][0]
    shift = 16 * (w - 1)
    parts = []
    for x0, y0, z0, x1, y1, z1 in PROPS[name][3]:
        parts.append("{%s, %s, %s, %s, %s, %s}" % tuple(_num(v) for v in (x0 + shift, y0, z0, x1 + shift, y1, z1)))
    return "{" + ", ".join(parts) + "}"


def _num(v):
    return str(int(v)) if float(v).is_integer() else str(v)


def write_all(write, assets, data, lang, condition, self_drop):
    models, states, items = assets / "models" / "block", assets / "blockstates", assets / "models" / "item"
    for name, (display, size, _, _) in PROPS.items():
        elements = MODELS[name]
        textures = textures_of(elements)
        lang[f"block.{MOD}.{name}"] = display
        lang[f"tooltip.{MOD}.{name}"] = f"{size_text(size)}. Placed and broken as one."
        parts = part_models(name)
        for index, part in enumerate(parts):
            write(models / f"{name}_part{index}.json", {"ambientocclusion": False, "textures": textures, "elements": part})
        variants = {}
        for facing, y in FACING_Y.items():
            rotation = {"y": y} if y else {}
            for part in range(PART_STATES):
                model = rid(f"block/{name}_part{part}") if part < len(parts) else rid("block/large_machine_empty")
                variants[f"facing={facing},part={part}"] = {"model": model, **rotation}
        write(states / f"{name}.json", {"variants": variants})
        write(items / f"{name}.json", {"parent": "minecraft:block/block", "textures": textures,
                                       "elements": scaled_elements(elements)})
        write(assets / "items" / f"{name}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{name}")}})
        table = self_drop(name)
        table["pools"][0]["condition"] = {"type": "minecraft:all_of", "terms": [
            {"type": "minecraft:survives_explosion"},
            {"type": "minecraft:match_block", "blocks": rid(name), "state": {"part": "0"}}]}
        write(data / "loot_table" / "blocks" / f"{name}.json", table)
        pattern, key, count = RECIPES[name]
        write(data / "recipe" / f"{name}.json", shaped(condition, pattern, key, name, count))
    write_bbmodels()


# ------------------------------------------------------------------ Blockbench projects

def bbmodel(name):
    """A Blockbench project of the whole prop: one cube an element, in structure space, with the game's UVs."""
    import blockbench_export
    return blockbench_export.project(name, [(name, (0, 0, 0), None, MODELS[name])], draw=lambda t: TEXTURES[t]())


def write_bbmodels(folder=ART):
    folder.mkdir(parents=True, exist_ok=True)
    for name in PROPS:
        import blockbench_export
        blockbench_export.write(folder / f"{name}.bbmodel", name, [(name, (0, 0, 0), None, MODELS[name])],
                                draw=lambda t: TEXTURES[t]())


if __name__ == "__main__":
    for name, (display, size, _, _) in PROPS.items():
        parts = part_models(name)
        print(f"{name}: {display}, {size_text(size)}, {len(MODELS[name])} elements, {sum(1 for p in parts if p)} of {len(parts)} parts drawn")
    if "--bbmodel" in sys.argv:
        write_bbmodels()
        print(f"Blockbench projects written to {ART}")
