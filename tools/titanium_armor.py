"""Titanium armor (docs/features/titanium-gear.md): its worn 64x32 layers and inventory icons. tools/gear_textures.py
draws them (draw).

The pieces are the plain thallite armor's own hand-drawn maps (tools/thallite_armor.py ICONS and LAYERS), in titanium's
periwinkle grey: titanium is a fourth metal in the same family of shapes. Where thallite sets an emerald (the knees and
the boots' cuffs), titanium has a heat-blued, anodized inset, and its visor is a near-black blue. No Mojang texture is
read, traced or recoloured.

Symbols are thallite_armor's: O D M L H the metal, e E a A the inset (dark, mid, light, glint), v the visor.
"""
from PIL import Image

import armor_styles
import thallite_armor
from material_icons import METAL_RAMPS, hx

TITANIUM = METAL_RAMPS["titanium"]
# Anodized titanium: a deep heat-blue going to violet in the glint, so it reads as a coloured oxide on the metal and
# stays clear of the grey's own blue tint: dark, mid, light, glint.
ANODIZED = (hx("1c2a78"), hx("3352c4"), hx("7a8cf0"), hx("d8c8ff"))
VISOR = hx("10162a")

COLORS = {**dict(zip("ODMLH", TITANIUM)), **dict(zip("eEaA", ANODIZED)), "v": VISOR}
PIECES = thallite_armor.PIECES


def icon(piece):
    """A piece's inventory icon (16x16)."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    armor_styles._paint(img, (0, 0, 16, 16), thallite_armor.ICONS[piece], (), COLORS)
    return img


def layer(leggings):
    """A worn layer (64x32): layer 1 (humanoid: helmet, chestplate and boots), or layer 2 (humanoid_leggings)."""
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    for box, face_rows in thallite_armor.LAYERS["layer2" if leggings else "layer1"].items():
        rects = armor_styles.faces(box)
        for face, rows in face_rows.items():
            armor_styles._paint(img, rects[face], rows, (), COLORS)
    return img


def draw(save, save_armor):
    """save(img, kind, name) and save_armor(img, layer, name) as in tools/generate_textures.py."""
    for piece in PIECES:
        save(icon(piece), "item", f"titanium_{piece}")
    save_armor(layer(False), "humanoid", "titanium")
    save_armor(layer(True), "humanoid_leggings", "titanium")
