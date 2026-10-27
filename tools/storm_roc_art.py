"""Original textures for the Storm Roc and his fight (docs/features/storm-roc.md) (requires Pillow): the sheets of his
GeckoLib bodies (tools/storm_roc_models.py) and their glowmasks, painted box by box and face by face at TEXTURE_SCALE
times the size the models declare (docs/ART_DIRECTION.md, "High resolution"), in Vesperine's clean style
(tools/vesperine_art.py, whose sheet and helpers these use): two or three flat tones a material, hard edges, no noise,
patterns on the model's pixel grid.

- The Roc: slate-grey feathers with a storm-blue sheen, laid in staggered rows, each row lit along its top and dark at its
  lower edge; a darker cap to his head and a pale brow over his eyes; his crest swept back, its feathers edged blue; a
  hooked beak of dark horn with a pale ridge; yellow shanks scaled across, black talons; his wings' coverts in short
  rows and their flight feathers in long ones, run along the wing; the tips of his flight feathers white like lightning.
  His glowmask holds his eyes and those tips.
- A Gale Fledgling: the same slate, paler and fluffed with grey down at the breast, its eyes as blue as his.
- The worn Roc's Crest's feathers, and the icons of the Roc Feather, the Weather-Vane Lens and the Roc's Crest
  (tools/item_icons/).

Called from crop_textures.crop_textures(). Every pixel is drawn here by code; no Mojang texture, nor any other texture,
is read, traced or recoloured.
"""
import storm_roc as numbers
import storm_roc_models as rm
from crop_textures import rgb
from vesperine_art import CLEAR, Sheet, cell

S = rm.TEXTURE_SCALE

# Palettes, darkest first.
SLATE = [rgb("161a22"), rgb("242a35"), rgb("343c4a"), rgb("47515f"), rgb("5d6979"), rgb("7a8798")]
SHEEN = [rgb("1c2a48"), rgb("2a4070"), rgb("3c5a9a"), rgb("5a7ec4")]
DOWN = [rgb("4a4e56"), rgb("62666e"), rgb("7c8088"), rgb("989ca4")]
HORN = [rgb("101012"), rgb("1e1d1f"), rgb("323034"), rgb("4a474c"), rgb("6a666b")]
YELLOW = [rgb("5a4410"), rgb("8a6a1c"), rgb("b89028"), rgb("dcb444")]
STORM = [rgb("1a3aa8"), rgb("3a70f0"), rgb("8ec0ff"), rgb("d6ecff"), rgb("ffffff")]


# ------------------------------------------------------------------------------------------------- materials

def feathers(sheet, part, pal=SLATE, row=3, sheen=True, phase=0):
    """Contour feathers: a mid tone, laid in rows `row` model pixels deep, each row lit along its top and dark along its
    lower edge, the rows' feathers staggered (a dark notch every third pixel, shifted row to row); a storm-blue sheen
    along the upper rows of each side. The top face lit, the bottom dark."""
    for name, rect in sheet.faces(part).items():
        x, y, w, h = rect
        if name == "bottom":
            sheet.fill(rect, pal[1])
            continue
        sheet.fill(rect, pal[3] if name != "top" else pal[4])
        rows = max(1, h // S)
        cols = max(1, w // S)
        for cy in range(rows):
            band = (cy + phase) // row
            if (cy + phase) % row == 0:
                sheet.fill((x, y + cy * S, w, max(1, S // 2)), pal[4] if name != "top" else pal[5])
            if (cy + phase) % row == row - 1:
                sheet.fill((x, y + cy * S + S - max(1, S // 2), w, max(1, S // 2)), pal[2])
                for cx in range(cols):
                    if (cx + band) % 3 == 0:
                        sheet.fill(cell(rect, cx, cy), pal[2])
        if sheen and name in ("left", "right", "top") and rows >= 3:
            for cx in range(cols):
                if (cx + phase) % 4 == 1:
                    sheet.fill(cell(rect, cx, 1, 1, 1), SHEEN[2])
                    sheet.fill((x + cx * S, y + S, max(1, S // 2), S), SHEEN[3])
        sheet.fill((x, y, 1, h), pal[4])
        sheet.fill((x + w - 1, y, 1, h), pal[1])


def quills(sheet, part, pal=SLATE, sheen=True):
    """Flight feathers on a wing's plate: long feathers running along its span (the face's width), a dark shaft line
    between each, the leading edge (the front's rows) lighter coverts; the underside paler."""
    for name, rect in sheet.faces(part).items():
        x, y, w, h = rect
        if name in ("top", "bottom"):
            sheet.fill(rect, pal[3] if name == "top" else pal[4])
            rows = max(1, h // S)
            for cy in range(rows):
                if cy % 2 == 1:
                    sheet.fill((x, y + cy * S + S - 1, w, 1), pal[1] if name == "top" else pal[2])
            # The leading edge (the last rows: the front) in short lit coverts.
            sheet.fill((x, y + h - 2 * S, w, 2 * S), pal[4] if name == "top" else pal[5])
            for cx in range(0, max(1, w // S), 2):
                sheet.fill(cell(rect, cx, rows - 2, 1, 1), pal[3])
            if sheen and name == "top":
                for cx in range(0, max(1, w // S), 3):
                    sheet.fill(cell(rect, cx, rows - 3, 2, 1), SHEEN[2])
            continue
        sheet.fill(rect, pal[2])
        sheet.fill((x, y, w, 1), pal[4])


def horn(sheet, part, ridge=True):
    """Dark horn: a beak's or talons' smooth dark, a pale ridge along its top."""
    for name, rect in sheet.faces(part).items():
        x, y, w, h = rect
        sheet.fill(rect, HORN[2] if name != "bottom" else HORN[1])
        if name == "top" and ridge:
            sheet.fill((x + w // 2 - S // 2, y, S, h), HORN[4])
        elif name != "bottom":
            sheet.fill((x, y, w, max(1, S // 2)), HORN[3])
            sheet.fill((x + w - 1, y, 1, h), HORN[0])


def scales(sheet, part):
    """Yellow shanks, scaled across: a band of shade every model pixel."""
    for name, rect in sheet.faces(part).items():
        x, y, w, h = rect
        sheet.fill(rect, YELLOW[2])
        for cy in range(max(1, h // S)):
            sheet.fill((x, y + cy * S + S - 1, w, 1), YELLOW[1])
        sheet.fill((x, y, 1, h), YELLOW[3])


def lightning(sheet, part, white=True):
    """Light: a white-hot middle deepening to storm-blue at its edges; drawn on the glowmask too."""
    for name, rect in sheet.faces(part).items():
        x, y, w, h = rect
        for py in range(y, y + h):
            for px in range(x, x + w):
                edge = min(px - x, x + w - 1 - px, py - y, y + h - 1 - py)
                step = edge * 2 // max(1, S)
                colour = STORM[min(len(STORM) - 1, 2 + step)] if white else STORM[min(3, 1 + step)]
                sheet.put(px, py, colour)
                sheet.put(px, py, colour, glow=True)


# ------------------------------------------------------------------------------------------------- the Roc

def paint_head(sheet, pal=SLATE):
    feathers(sheet, "head", pal, row=2, sheen=False)
    for name, rect in sheet.faces("head").items():
        x, y, w, h = rect
        if name == "top":
            # A darker cap.
            sheet.fill(rect, pal[2])
            sheet.fill((x, y, w, S), pal[3])
        elif name in ("left", "right"):
            # A pale brow over where the eye sits, and the cap's edge.
            sheet.fill((x, y, w, S), pal[2])
            sheet.fill(cell(rect, 0, 2, max(1, w // S), 1), pal[5])
        elif name == "front":
            sheet.fill(cell(rect, 0, 0, max(1, w // S), 1), pal[2])
            sheet.fill(cell(rect, 1, 2, max(1, w // S) - 2, 1), pal[5])


def paint_crest(sheet, part="crest"):
    for name, rect in sheet.faces(part).items():
        x, y, w, h = rect
        sheet.fill(rect, SLATE[3])
        sheet.fill((x, y, w, S), SLATE[5])
        sheet.fill((x, y + h - S, w, S), SHEEN[2])
        sheet.fill((x, y, 1, h), SHEEN[3])


def storm_roc():
    sheet = Sheet(rm.storm_roc_model())
    feathers(sheet, "torso", row=3)
    feathers(sheet, "breast", pal=SLATE, row=2, sheen=False, phase=1)
    for name, rect in sheet.faces("breast").items():
        if name == "front":
            # A paler breast, barred.
            x, y, w, h = rect
            sheet.fill(rect, SLATE[4])
            for cy in range(1, max(1, h // S), 2):
                sheet.fill(cell(rect, 0, cy, max(1, w // S), 1), SLATE[3])
    feathers(sheet, "neck", row=2, phase=1)
    paint_head(sheet)
    paint_crest(sheet)
    horn(sheet, "beak")
    horn(sheet, "beak_hook", ridge=False)
    lightning(sheet, "eye")
    feathers(sheet, "thigh", row=2, sheen=False)
    scales(sheet, "shank")
    horn(sheet, "talons", ridge=False)
    quills(sheet, "tail_fan")
    feathers(sheet, "folded_wing", row=3, phase=2)
    lightning(sheet, "folded_tips", white=False)
    quills(sheet, "inner_wing")
    quills(sheet, "primaries")
    lightning(sheet, "primary_tips")
    return sheet


def gale_fledgling():
    sheet = Sheet(rm.gale_fledgling_model())
    pale = [SLATE[1], SLATE[2], SLATE[3], SLATE[4], SLATE[5], rgb("97a3b2")]
    feathers(sheet, "torso", pal=pale, row=2, sheen=False)
    for name, rect in sheet.faces("down").items():
        x, y, w, h = rect
        sheet.fill(rect, DOWN[2])
        for cx in range(0, max(1, w // S), 2):
            sheet.fill(cell(rect, cx, 0), DOWN[3])
            sheet.fill(cell(rect, cx + 1, max(1, h // S) - 1), DOWN[1])
    feathers(sheet, "head", pal=pale, row=2, sheen=False)
    horn(sheet, "beak")
    lightning(sheet, "eye", white=False)
    quills(sheet, "tail_fan", pal=pale, sheen=False)
    quills(sheet, "inner_wing", pal=pale, sheen=False)
    quills(sheet, "primaries", pal=pale, sheen=False)
    horn(sheet, "talons", ridge=False)
    return sheet


# ------------------------------------------------------------------------------------------------- the worn crest

def crest_texture():
    """The worn Roc's Crest's feathers (16x16): slate quills down the middle, lit along the left, storm-blue at their
    edges and tips, a dark shaft line every fourth column."""
    from PIL import Image
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    for y in range(16):
        for x in range(16):
            tone = SLATE[4] if x % 4 == 0 else SLATE[3] if x % 4 in (1, 2) else SLATE[1]
            if y < 2:
                tone = SHEEN[3] if x % 4 != 3 else SHEEN[1]
            elif y < 4:
                tone = SHEEN[2] if x % 4 != 3 else SLATE[1]
            elif y > 13:
                tone = SLATE[2] if x % 4 != 3 else SLATE[0]
            px[x, y] = tone + (255,)
    return img


def storm_roc_textures():
    """Every texture of the Storm Roc's fight, keyed (kind, name) as crop_textures returns them."""
    import item_icons
    out = {("item", "rocs_crest_feathers"): crest_texture()}
    sheets = {"storm_roc": storm_roc(), "gale_fledgling": gale_fledgling()}
    for name, sheet in sheets.items():
        out[("entity", name)] = sheet.img
        if name in numbers.GLOWING:
            out[("entity", f"{name}_glowmask")] = sheet.glow
    for item in numbers.items():
        out[("item", item)] = item_icons.draw(item)
    return out


if __name__ == "__main__":
    import sys
    from PIL import Image
    sheets = {"storm_roc": storm_roc(), "gale_fledgling": gale_fledgling()}
    folder = sys.argv[1] if len(sys.argv) > 1 else "."
    for name, sheet in sheets.items():
        sheet.img.save(f"{folder}/{name}.png")
        sheet.glow.save(f"{folder}/{name}_glowmask.png")
    print("ok")
