"""The Cursed Mirror (docs/features/cursed-mirror.md): one gilt-framed standing mirror block and its item. A gold ring
around a glass pane makes it; breaking it brings seven days of vanilla Bad Luck. This module holds its block/item id,
name, model, blockstate, recipe, loot, mineable tag, lang, and the curse numbers; tools/check_mod_data.py checks the
Java (creatures-free, in curse/CursedMirror*.java) against them.

Java: curse/CursedMirrorBlock (the block, the break curse, the night flicker) and curse/CursedMirror (registration).
"""
from PIL import Image

MOD = "jugcraft"
BLOCK = "cursed_mirror"
ITEM = "cursed_mirror"
NAME = "Cursed Mirror"
# Keep in step with CursedMirrorBlock: seven in-game days of Bad Luck, applied once when a player breaks the mirror.
CURSE_DAYS = 7
CURSE_TICKS = CURSE_DAYS * 24000

# The gilt frame (bar ids) and the silvered pane, on the back three pixels of the block; the pane is set one pixel in.
_FRAME = f"block/{BLOCK}_frame"
_GLASS = f"block/{BLOCK}_glass"


def _rid(path):
    return path if ":" in path else f"{MOD}:{path}"


def block_model():
    def box(lo, hi, faces):
        return {"from": list(lo), "to": list(hi),
                "faces": {side: {"texture": tex, "cullface": side} if side in ("north", "south") else {"texture": tex}
                          for side, tex in faces.items()}}
    frame = {s: "#frame" for s in ("north", "south", "east", "west", "up", "down")}
    return {
        "parent": "minecraft:block/block",
        "textures": {"frame": _rid(_FRAME), "glass": _rid(_GLASS), "particle": _rid(_FRAME)},
        "elements": [
            box((0, 14, 13), (16, 16, 16), frame),
            box((0, 0, 13), (16, 2, 16), frame),
            box((0, 2, 13), (2, 14, 16), frame),
            box((14, 2, 13), (16, 14, 16), frame),
            box((2, 2, 14), (14, 14, 16), {"north": "#glass", "south": "#frame", "east": "#frame",
                                           "west": "#frame", "up": "#frame", "down": "#frame"}),
        ],
    }


def blockstate():
    return {"variants": {f"facing={facing}": ({"model": _rid(f"block/{BLOCK}")} if y == 0 else
                                              {"model": _rid(f"block/{BLOCK}"), "y": y})
                         for facing, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270))}}


def recipe():
    return {"type": "minecraft:crafting_shaped", "pattern": ["GGG", "GPG", "GGG"],
            "key": {"G": "minecraft:gold_ingot", "P": "minecraft:glass_pane"},
            "result": {"id": _rid(ITEM), "count": 1},
            "fabric:load_conditions": [{"condition": f"{MOD}:feature_enabled", "feature": "agriculture"}]}


def write_all(write, assets, data, lang, self_drop):
    lang[f"block.{MOD}.{BLOCK}"] = NAME
    write(assets / "blockstates" / f"{BLOCK}.json", blockstate())
    write(assets / "models" / "block" / f"{BLOCK}.json", block_model())
    write(assets / "models" / "item" / f"{ITEM}.json",
          {"parent": "minecraft:item/generated", "textures": {"layer0": _rid(f"item/{ITEM}")}})
    write(assets / "items" / f"{ITEM}.json", {"model": {"type": "minecraft:model", "model": _rid(f"item/{ITEM}")}})
    write(data / "recipe" / f"{BLOCK}.json", recipe())
    write(data / "loot_table" / "blocks" / f"{BLOCK}.json", self_drop(BLOCK))


# ---------------------------------------------------------------- textures

_GOLD = [(74, 54, 20), (150, 112, 44), (206, 166, 74), (240, 212, 130)]


def _frame_texture():
    """Gilt metal: a bevelled gold with a lit top-left and a dark bottom-right, flecked so bars don't look flat."""
    import random
    rng = random.Random(1031)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            edge = min(x, y, 15 - x, 15 - y)
            base = 3 if edge == 0 else 2 if (x + y) < 15 else 1
            shade = max(0, min(3, base + rng.choice([0, 0, -1, 1])))
            img.putpixel((x, y), _GOLD[shade] + (255,))
    return img


def _glass_texture():
    """A silvered pane: a cold vertical gradient with a diagonal glint and the faintest hint of a pale face."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    top = (58, 66, 86)
    bottom = (20, 24, 34)
    for y in range(16):
        t = y / 15
        row = tuple(round(top[i] + (bottom[i] - top[i]) * t) for i in range(3))
        for x in range(16):
            c = row
            if 2 <= x - y <= 4:  # a soft diagonal glint
                c = tuple(min(255, v + 46) for v in row)
            img.putpixel((x, y), c + (255,))
    # A pale oval face, barely there, in the upper middle.
    face = [(7, 5), (8, 5), (6, 6), (9, 6), (6, 7), (9, 7), (7, 9), (8, 9)]
    for x, y in face:
        r, g, b, _ = img.getpixel((x, y))
        img.putpixel((x, y), (min(255, r + 34), min(255, g + 36), min(255, b + 40), 255))
    return img


def _item_texture():
    """A small framed mirror for the inventory icon: a gold border round a dark silvered pane."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(2, 14):
        for x in range(4, 12):
            t = (y - 2) / 11
            img.putpixel((x, y), (round(54 + 8 * (1 - t)), round(62 + 8 * (1 - t)), round(82 - 40 * t), 255))
    for y in range(1, 15):
        for x in range(3, 13):
            if x in (3, 12) or y in (1, 14):
                edge = 3 if (x == 3 or y == 1) else 1
                img.putpixel((x, y), _GOLD[edge] + (255,))
    for y in range(3, 7):  # a glint on the pane
        img.putpixel((5 + (y - 3), y), (120, 130, 150, 255))
    return img


def draw_all(save):
    save(_frame_texture(), "block", f"{BLOCK}_frame")
    save(_glass_texture(), "block", f"{BLOCK}_glass")
    save(_item_texture(), "item", ITEM)
