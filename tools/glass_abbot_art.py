"""Original textures for the Glass Abbot and his fight (docs/features/glass-abbot.md) (requires Pillow): the sheet of his
GeckoLib body (tools/glass_abbot_models.py), which his Glass Images share, and its glowmask, painted box by box and face by
face at TEXTURE_SCALE times the size the model declares (docs/ART_DIRECTION.md, "High resolution"), in Vesperine's clean
style (tools/vesperine_art.py, whose sheet and helpers these use): two or three flat tones a material, hard edges, no
noise, patterns on the model's pixel grid.

- The Abbot: a habit of grey wool falling in long folds, each fold lit along its left edge; a darker hem; a rope of pale
  hemp at his waist; a deep hood, darker within; his mantle of leaded glass, panes of clear, amber and rose in dark lead,
  each pane lit at its top left; hands of clear glass; and where his face should be a round rose window, petals of rose
  and gold round a white-hot heart. His glowmask holds the rose window and a glint in each of the mantle's panes.
- The worn Abbot's Cowl's wool, the Abbey Glass block, and the icons of the Prism Shard, the Glass Reliquary, the Lantern
  Lens, the Mirror Inscription and the Abbot's Cowl (tools/item_icons/).

Called from crop_textures.crop_textures(). Every pixel is drawn here by code; no Mojang texture, nor any other texture,
is read, traced or recoloured.
"""
import glass_abbot as numbers
import glass_abbot_models as am
from crop_textures import rgb
from vesperine_art import CLEAR, Sheet, cell

S = am.TEXTURE_SCALE

# Palettes, darkest first.
WOOL = [rgb("2a2a2e"), rgb("3c3c42"), rgb("525258"), rgb("6a6a70"), rgb("84848a"), rgb("9e9ea4")]
HEMP = [rgb("5a4a30"), rgb("8a7450"), rgb("b8a070"), rgb("d8c494")]
LEAD = rgb("1c1e24")
PANES = {"clear": [rgb("6a8aa4"), rgb("9ec0d8"), rgb("d8eef8")], "amber": [rgb("9a5a14"), rgb("d8962c"), rgb("f6d07a")],
         "rose": [rgb("8a2a44"), rgb("d0607e"), rgb("f4a8bc")]}
ROSE = [rgb("6a1a30"), rgb("b83a5a"), rgb("e87a98"), rgb("f4c860"), rgb("fff4d0"), rgb("ffffff")]


# ------------------------------------------------------------------------------------------------- materials

def wool(sheet, part, pal=WOOL, fold=3):
    """Grey wool in long vertical folds `fold` model pixels apart, each lit along its left edge and shaded along its
    right; the top face lit, the bottom dark."""
    for name, rect in sheet.faces(part).items():
        x, y, w, h = rect
        if name == "bottom":
            sheet.fill(rect, pal[1])
            continue
        sheet.fill(rect, pal[3] if name != "top" else pal[4])
        if name == "top":
            continue
        for cx in range(max(1, w // S)):
            if cx % fold == 0:
                sheet.fill((x + cx * S, y, max(1, S // 2), h), pal[4])
            elif cx % fold == fold - 1:
                sheet.fill((x + cx * S + S - max(1, S // 2), y, max(1, S // 2), h), pal[2])
        sheet.fill((x, y, w, max(1, S // 2)), pal[4])


def hem(sheet, part):
    wool(sheet, part, pal=[WOOL[0], WOOL[1], WOOL[1], WOOL[2], WOOL[3], WOOL[4]], fold=4)


def rope(sheet, part):
    """Pale hemp, twisted: a lit and a shaded strand in turn."""
    for name, rect in sheet.faces(part).items():
        x, y, w, h = rect
        sheet.fill(rect, HEMP[2])
        for cx in range(max(1, w // S)):
            if cx % 2 == 0:
                sheet.fill(cell(rect, cx, 0, 1, max(1, h // S)), HEMP[3])
        sheet.fill((x, y + h - 1, w, 1), HEMP[0])


def leaded(sheet, part, glint=True):
    """Leaded glass: panes two model pixels square in clear, amber and rose, each lit at its top left, in dark lead; on
    the glowmask, a glint in each pane."""
    order = ("clear", "amber", "rose", "clear", "rose", "amber")
    for name, rect in sheet.faces(part).items():
        x, y, w, h = rect
        sheet.fill(rect, LEAD)
        cols, rows = max(1, w // S), max(1, h // S)
        for cy in range(0, rows, 2):
            for cx in range(0, cols, 2):
                dark, mid, light = PANES[order[(cx // 2 + cy // 2) % len(order)]]
                pane = cell(rect, cx, cy, min(2, cols - cx), min(2, rows - cy))
                px, py, pw, ph = pane
                inner = (px + 1, py + 1, pw - 2, ph - 2)
                sheet.fill(inner, mid)
                sheet.fill((px + 1, py + 1, pw - 2, max(1, S // 2)), light)
                sheet.fill((px + 1, py + 1, max(1, S // 2), ph - 2), light)
                sheet.fill((px + pw - 1 - max(1, S // 2), py + 1, max(1, S // 2), ph - 2), dark)
                if glint and name in ("front", "back", "top"):
                    sheet.fill((px + 2, py + 2, max(1, S // 2), max(1, S // 2)), light, glow=True)


def glass_hand(sheet, part):
    """Hands of clear glass: pale and cool, lit at the top, a dark edge."""
    for name, rect in sheet.faces(part).items():
        x, y, w, h = rect
        sheet.fill(rect, PANES["clear"][1])
        sheet.fill((x, y, w, max(1, S // 2)), PANES["clear"][2])
        sheet.fill((x + w - 1, y, 1, h), PANES["clear"][0])


def hood(sheet, part):
    """The hood's wool, its front darker within, where the face sits."""
    wool(sheet, part, fold=3)
    for name, rect in sheet.faces(part).items():
        if name == "front":
            x, y, w, h = rect
            sheet.fill(rect, WOOL[0])
            sheet.fill((x, y, w, S), WOOL[2])
            sheet.fill((x, y, S, h), WOOL[2])
            sheet.fill((x + w - S, y, S, h), WOOL[2])


def rose_window(sheet, part):
    """The face: a round rose window, petals of rose and gold round a white-hot heart, in lead; all of it on the glowmask."""
    for name, rect in sheet.faces(part).items():
        x, y, w, h = rect
        cx, cy = x + w / 2.0, y + h / 2.0
        r = min(w, h) / 2.0
        for py in range(y, y + h):
            for px in range(x, x + w):
                dx, dy = px + 0.5 - cx, py + 0.5 - cy
                d = (dx * dx + dy * dy) ** 0.5
                if d > r:
                    colour = WOOL[0]
                    sheet.put(px, py, colour)
                    continue
                if abs(d - r * 0.95) < S * 0.3 or abs(dx) < S * 0.25 or abs(dy) < S * 0.25:
                    colour = LEAD
                elif d < r * 0.3:
                    colour = ROSE[5] if d < r * 0.15 else ROSE[4]
                elif (dx * dy > 0):
                    colour = ROSE[2] if d < r * 0.65 else ROSE[1]
                else:
                    colour = ROSE[3] if d < r * 0.65 else ROSE[2]
                sheet.put(px, py, colour)
                if colour != LEAD:
                    sheet.put(px, py, colour, glow=True)


# ------------------------------------------------------------------------------------------------- the Abbot

def glass_abbot():
    sheet = Sheet(am.glass_abbot_model())
    wool(sheet, "robe", fold=3)
    hem(sheet, "hem")
    wool(sheet, "chest", fold=3)
    rope(sheet, "rope")
    leaded(sheet, "mantle_front")
    leaded(sheet, "mantle_back")
    leaded(sheet, "mantle_shoulder")
    hood(sheet, "hood")
    wool(sheet, "hood_peak", fold=2)
    rose_window(sheet, "face")
    wool(sheet, "sleeve", fold=2)
    hem(sheet, "cuff")
    glass_hand(sheet, "hand")
    return sheet


# ------------------------------------------------------------------------------------------------- the worn cowl, and Abbey Glass

def cowl_texture():
    """The worn Abbot's Cowl's wool (16x16): grey folds, lit along their left edges, a darker lining band at the bottom."""
    from PIL import Image
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    for y in range(16):
        for x in range(16):
            tone = WOOL[4] if x % 4 == 0 else WOOL[3] if x % 4 in (1, 2) else WOOL[2]
            if y > 12:
                tone = WOOL[1] if x % 4 != 0 else WOOL[2]
            px[x, y] = tone + (255,)
    return img


def abbey_glass():
    """Abbey Glass (16x16): stained glass in four panes, rose and amber crosswise round a gold heart, held in dark lead,
    each pane lit at its top left; see-through."""
    from PIL import Image
    img = Image.new("RGBA", (16, 16), CLEAR)
    px = img.load()
    for y in range(16):
        for x in range(16):
            if x in (0, 15) or y in (0, 15) or x in (7, 8) or y in (7, 8):
                px[x, y] = LEAD + (255,)
                continue
            if 5 <= x <= 10 and 5 <= y <= 10:
                tone = ROSE[3]
            else:
                pane = PANES["rose"] if (x < 8) == (y < 8) else PANES["amber"]
                tone = pane[2] if (x % 8 in (1, 2) or y % 8 in (1, 2)) else pane[1]
            px[x, y] = tone + (200,)
    return img


def glass_abbot_textures():
    """Every texture of the Glass Abbot's fight, keyed (kind, name) as crop_textures returns them."""
    import item_icons
    out = {("item", "abbots_cowl_wool"): cowl_texture(), ("block", "abbey_glass"): abbey_glass()}
    sheet = glass_abbot()
    for entity, body in numbers.GECKO.items():
        out[("entity", entity)] = sheet.img
        if entity in numbers.GLOWING:
            out[("entity", f"{entity}_glowmask")] = sheet.glow
    for item in list(numbers.ITEMS) + list(numbers.COSTUMES):
        out[("item", item)] = item_icons.draw(item)
    return out


if __name__ == "__main__":
    import sys
    folder = sys.argv[1] if len(sys.argv) > 1 else "."
    sheet = glass_abbot()
    sheet.img.save(f"{folder}/glass_abbot.png")
    sheet.glow.save(f"{folder}/glass_abbot_glowmask.png")
    abbey_glass().resize((128, 128), 0).save(f"{folder}/abbey_glass.png")
    print("ok")
