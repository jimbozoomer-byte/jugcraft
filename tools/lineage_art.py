"""The Astrawand's textures (star-bred lines, docs/features/star-bred-lines.md): its 16x16 icon, from the map
tools/item_icons/astrawand.txt (drawn by item_icons.draw, docs/ITEM_ICONS.md), and the small texture its hand model
(tools/lineage.py hand_model) is painted from: four bands of four columns, each band one material's tones lightest first
(the pale shaft, the gold grip, the bright gold of its band and collar, and the star), so a face lit from the top left
takes a lighter column. The colours are the icon's own (tools/icon_materials.py STEM and GOLD).

The Astrawand is a fan homage to the Wind Waker of The Legend of Zelda, with its own name; every pixel here is drawn by
code, and nothing from that game, or from Mojang, is read.

Called from crop_textures.crop_textures().
"""
from PIL import Image

import icon_materials as im
import item_icons
import lineage

# Each band of the model texture, lightest first.
MODEL_BANDS = {
    "stem": [im.STEM.highlight, im.STEM.light, im.STEM.mid, im.STEM.dark],
    "gold": [im.GOLD.light, im.GOLD.mid, im.GOLD.dark, im.GOLD.outline_light],
    "bright": [im.GOLD.highlight, im.GOLD.light, im.GOLD.mid, im.GOLD.dark],
    "star": [(255, 252, 226), im.GOLD.highlight, im.GOLD.light, im.GOLD.mid],
}


def model_texture():
    image = Image.new("RGBA", (16, 16))
    for band, tones in MODEL_BANDS.items():
        start = lineage.BAND_START[band]
        for shade, colour in enumerate(tones):
            for y in range(16):
                image.putpixel((start + shade, y), (*colour, 255))
    return image


def lineage_textures():
    return {("item", lineage.ITEM): item_icons.draw(lineage.ITEM), ("item", f"{lineage.ITEM}_model"): model_texture()}
