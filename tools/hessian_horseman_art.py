"""Original textures for the Headless Horseman in his lair (docs/features/hessian-horseman.md) (requires Pillow): the
sheets of his GeckoLib bodies (tools/hessian_horseman_models.py) and their glowmasks, painted box by box and face by face
at TEXTURE_SCALE times the size the models declare (docs/ART_DIRECTION.md, "High resolution"), in Vesperine's clean style
(tools/vesperine_art.py, whose sheet and helpers these use): two or three flat tones a material, hard edges, no noise,
patterns on the model's pixel grid.

- The Horseman: a black horse with a blue sheen along its back and quarters, its mane, forelock and tail long black hair
  in strands, a greyer muzzle, dark horn hooves and black feathering at the fetlocks; red eyes (on the glowmask); a
  plain black bridle with a brass buckle; a red shabraque edged with tarnished gold braid under a black saddle; iron
  stirrups on black leathers; a pumpkin in a rope net at each side. The trooper: a dark blue coat with red facings and
  turnbacks, white crossbelts crossing on his chest, brass buttons and gold-fringed epaulettes; a collar banded red with
  his embers glowing in it; buff breeches; tall black riding boots with a shine, gilt spurs; black gauntlets; a black
  cloak in folds, its hem ragged; a curved steel sabre, a brass knuckle-bow and a black grip bound in brass wire; an
  iron lantern, its panes open on the fire inside (orange, or white, on the glowmask); and the jack-o'-lanterns, ribbed
  orange with carved faces, their fire on the glowmask.
- His phantom riders: the same horse and trooper drawn all in the blue of soul fire, darker in the folds and brighter on
  the edges, the whole of them on the glowmask too, so they burn in the dark.
- The pumpkin he tosses, and his lantern head, their carved faces lit.
- The Hessian's Tricorn's worn textures (felt and braid), and the icons of the Hessian's Spur and Tricorn
  (tools/item_icons/).

Called from crop_textures.crop_textures(). Every pixel is drawn here by code; no Mojang texture, nor any other texture,
is read, traced or recoloured.
"""
from PIL import Image

import hessian_horseman_models as hm
import item_icons
from crop_textures import rgb
from pumpkin_king_art import FLESH, FLAME, HOLLOW, PUMPKIN, STEM, WHITE, ribbed
from vesperine_art import Sheet, folds, ragged, strands

S = hm.TEXTURE_SCALE

# Palettes, darkest first.
COAT_HORSE = [rgb("050507"), rgb("0b0b10"), rgb("13141c"), rgb("1d2030"), rgb("2c3248")]
HAIR = [rgb("030304"), rgb("08080b"), rgb("101016"), rgb("1a1b24"), rgb("2a2d3c")]
MUZZLE = [rgb("15141a"), rgb("23222a"), rgb("33323c")]
HORN = [rgb("100e0c"), rgb("1e1a16"), rgb("2e2822")]
LEATHER = [rgb("060506"), rgb("0e0c0d"), rgb("191617"), rgb("2a2526"), rgb("4a4446")]
BRASS = [rgb("4e3a10"), rgb("7e6022"), rgb("ae8a38"), rgb("d6b65e"), rgb("f0dc98")]
RED = [rgb("2e0608"), rgb("520c10"), rgb("761418"), rgb("982026")]
BLUE = [rgb("080c18"), rgb("0e1528"), rgb("16203a"), rgb("223050"), rgb("304468")]
WHITE_BELT = [rgb("8a8478"), rgb("b2ac9e"), rgb("d4cebf"), rgb("ece8dc")]
BUFF = [rgb("5a4e38"), rgb("7a6c50"), rgb("9a8a6a"), rgb("b8a886")]
CLOAK = [rgb("040305"), rgb("0a080c"), rgb("120f15"), rgb("1c1820")]
IRON = [rgb("15161a"), rgb("26282e"), rgb("3a3d46"), rgb("565a66")]
STEEL = [rgb("3a3e48"), rgb("6a707e"), rgb("9aa2b0"), rgb("cdd3de"), rgb("f2f4f8")]
ROPE = [rgb("3a2e1c"), rgb("5c4a2e"), rgb("7e6844")]
EYE = [rgb("6e0808"), rgb("c01414"), rgb("ff4230")]
EMBER = [rgb("2a0c02"), rgb("5a1a04"), rgb("a43a08"), rgb("ff8a1c"), rgb("ffd060")]
# The phantoms' blue fire, darkest first: every material of theirs is drawn from it.
GHOST = [rgb("06142a"), rgb("0c2448"), rgb("14406e"), rgb("2468a0"), rgb("4a9ad0"), rgb("9ad8f4")]

# Where each jack-o'-lantern's face is carved, in model pixels from its front face's top left (its face plane is a pixel
# in from the jack's sides and top: tools/hessian_horseman_models.py): two eyes, a nose and a wide grin.
JACK_HOLES = {"eye_right": (1.25, 1.25, 1.5, 1.25), "eye_left": (4.25, 1.25, 1.5, 1.25), "nose": (3.25, 2.75, 0.5, 0.5),
              "grin": (1.25, 3.75, 4.5, 0.75)}


def carve(sheet, rect, holes):
    """Carves `holes` ({name: (cx, cy, cw, ch)} in model pixels, quarters allowed) into a face from its top left at
    `rect`: a rim of pale flesh, dark inside; the eyes cut as triangles (as tools/pumpkin_king_art.py carves, on the
    painted grid)."""
    x, y, _, _ = rect
    for name, (cx, cy, cw, ch) in holes.items():
        px, py, pw, ph = x + round(cx * S), y + round(cy * S), max(1, round(cw * S)), max(1, round(ch * S))
        sheet.fill((px - 1, py - 1, pw + 2, ph + 2), FLESH[0])
        sheet.fill((px, py, pw, ph), HOLLOW[0])
        sheet.fill((px, py, pw, min(2, ph)), HOLLOW[1])
        if name.startswith("eye"):
            for j in range(ph):
                inset = int(j * pw / (2 * ph))
                sheet.fill((px - 1, py + j, inset + 1, 1), FLESH[1] if j else FLESH[0])
                sheet.fill((px + pw - inset, py + j, inset + 1, 1), FLESH[1] if j else FLESH[0])


# ------------------------------------------------------------------------------------------------- materials

def hide(sheet, part, pal, sheen=True):
    """A horse's coat: flat black, its top and the upper sides catching a blue sheen, its underside in shadow."""
    for name, rect in sheet.faces(part).items():
        x, y, w, h = rect
        if name == "bottom":
            sheet.fill(rect, pal[0])
            continue
        sheet.fill(rect, pal[2])
        if name == "top":
            sheet.fill(rect, pal[3])
            if sheen:
                sheet.fill((x + w // 4, y, max(1, w // 2), h), pal[4])
            continue
        sheet.fill((x, y, w, S), pal[3])
        if sheen:
            sheet.fill((x, y, w, max(1, S // 2)), pal[4])
        sheet.fill((x, y + h - S, w, S), pal[1])


def hair(sheet, part, pal, phase=0, ragged_ends=True):
    """Long hair (mane, forelock, tail, fetlock feathering): flat strands, a band of sheen near the top, its ends ragged."""
    for name, rect in sheet.faces(part).items():
        x, y, w, h = rect
        if name in ("top", "bottom"):
            sheet.fill(rect, pal[1] if name == "bottom" else pal[2])
            continue
        strands(sheet, rect, pal + [pal[-1]], period=3, sheen=(S, S), phase=phase)
        if ragged_ends and h >= 2 * S:
            ragged(sheet, rect, every=2 * S, depth=S, offset=phase)


def flat(sheet, part, pal, top=None, bottom=None):
    """A plain part: its sides in pal[1], lit along the top edge; its top and bottom a tone up and down."""
    for name, rect in sheet.faces(part).items():
        x, y, w, h = rect
        if name == "top":
            sheet.fill(rect, pal[top if top is not None else min(2, len(pal) - 1)])
        elif name == "bottom":
            sheet.fill(rect, pal[bottom if bottom is not None else 0])
        else:
            sheet.fill(rect, pal[1])
            sheet.fill((x, y, w, 1), pal[min(2, len(pal) - 1)])
            sheet.fill((x, y + h - 1, w, 1), pal[0])


def cloth(sheet, part, pal, phase=0, hem=True):
    """Cloth in folds, darker toward the hem."""
    for name, rect in sheet.faces(part).items():
        if name in ("top", "bottom"):
            sheet.fill(rect, pal[1] if name == "top" else pal[0])
        else:
            folds(sheet, rect, pal, period=3 * S, phase=phase, shade_bottom=hem)


def gloss(sheet, part, pal):
    """Polished black leather: dark, a bright vertical shine down the front and sides, lighter at the top edge."""
    for name, rect in sheet.faces(part).items():
        x, y, w, h = rect
        sheet.fill(rect, pal[1])
        if name in ("top", "bottom"):
            sheet.fill(rect, pal[2] if name == "top" else pal[0])
            continue
        sheet.fill((x, y, w, 1), pal[3])
        sheet.fill((x + w // 3, y + S, max(1, S // 2), h - 2 * S), pal[3])
        sheet.fill((x + w // 3, y + S, 1, max(1, h // 3)), pal[4])


def metal(sheet, part, pal):
    """Metal: mid-toned, lit along the top and left, shaded along the bottom and right."""
    for name, rect in sheet.faces(part).items():
        x, y, w, h = rect
        sheet.fill(rect, pal[2])
        sheet.fill((x, y, w, 1), pal[3])
        sheet.fill((x, y, 1, h), pal[3])
        sheet.fill((x, y + h - 1, w, 1), pal[0])
        sheet.fill((x + w - 1, y, 1, h), pal[1])


def trim_edges(sheet, rect, pal, width=S // 2):
    """A braid along a face's edges."""
    x, y, w, h = rect
    sheet.fill((x, y, w, width), pal[3])
    sheet.fill((x, y + h - width, w, width), pal[2])
    sheet.fill((x, y, width, h), pal[3])
    sheet.fill((x + w - width, y, width, h), pal[2])


def jack(sheet, part, holes=JACK_HOLES, offset=(1, 1)):
    """A jack-o'-lantern: ribbed rind, its foot in shadow, its carved face dark inside (the fire is on its face plane)."""
    for name, rect in sheet.faces(part).items():
        x, y, w, h = rect
        if name == "top":
            sheet.fill(rect, PUMPKIN[3])
            for i in range(0, w, 2 * S):
                sheet.fill((x + i, y, S // 2, h), PUMPKIN[2])
        elif name == "bottom":
            sheet.fill(rect, PUMPKIN[1])
        else:
            ribbed(sheet, rect, PUMPKIN, rib=2)
            if name == "front" and holes:
                ox, oy = offset
                carve(sheet, (x + ox * S, y + oy * S, w, h), holes)


def flames(sheet, part, holes, pal, offset=(0, 0)):
    """A face plane's fire: bright at its heart and darker at its rim in each hole, on the sheet and the glowmask; clear
    elsewhere, so the carved rind shows round it."""
    for name, rect in sheet.faces(part).items():
        if name not in ("front", "back"):
            continue
        x, y, w, h = rect
        ox, oy = offset
        for hole, (cx, cy, cw, ch) in holes.items():
            px, py = x + round((cx - ox) * S), y + round((cy - oy) * S)
            pw, ph = max(1, round(cw * S)), max(1, round(ch * S))
            for glow in (False, True):
                sheet.fill((px, py, pw, ph), pal[1], glow=glow)
                sheet.fill((px + 1, py + 1, max(1, pw - 2), max(1, ph - 2)), pal[2], glow=glow)
                sheet.fill((px + pw // 4, py + ph // 4, max(1, pw // 2), max(1, ph // 2)), pal[3], glow=glow)


# ------------------------------------------------------------------------------------------------- the horse

def paint_horse(sheet, ghost=False):
    hide_pal = GHOST[:5] if ghost else COAT_HORSE
    hair_pal = GHOST[:5] if ghost else HAIR
    for part in ("barrel", "breast", "rump", "neck", "skull", "leg", "cannon", "ear"):
        hide(sheet, part, hide_pal, sheen=part not in ("cannon",))
    for name, rect in sheet.faces("muzzle").items():
        x, y, w, h = rect
        pal = GHOST[1:4] if ghost else MUZZLE
        sheet.fill(rect, pal[1])
        sheet.fill((x, y, w, S), pal[2])
        if name == "front":
            for nx in (S, w - 2 * S):
                sheet.fill((x + nx, y + h // 2, S, S), pal[0])
    for part, phase in (("mane", 0), ("forelock", 1), ("tail", 2), ("tail_tip", 3), ("feather", 1)):
        hair(sheet, part, hair_pal, phase=phase)
    for name, rect in sheet.faces("hoof").items():
        x, y, w, h = rect
        pal = GHOST[:3] if ghost else HORN
        sheet.fill(rect, pal[1])
        sheet.fill((x, y, w, 1), pal[2])
    flat(sheet, "noseband", GHOST[1:4] if ghost else LEATHER[:3])
    if not ghost:
        x, y, w, h = sheet.faces("noseband")["right"]
        sheet.fill((x + w // 2 - 1, y, 3, h), BRASS[3])
    for name, rect in sheet.faces("eye").items():
        x, y, w, h = rect
        for glow in (False, True):
            pal = GHOST[3:] if ghost else EYE
            sheet.fill(rect, pal[1], glow=glow)
            sheet.fill((x + w // 4, y + h // 4, max(1, w // 2), max(1, h // 2)), pal[2], glow=glow)


def paint_saddle(sheet, ghost=False):
    """The red shabraque edged in gold braid, the black saddle on it, the stirrups on their leathers, and the netted
    pumpkins."""
    red = GHOST[1:5] if ghost else RED
    gold = GHOST[2:] if ghost else BRASS
    for name, rect in sheet.faces("shabraque").items():
        x, y, w, h = rect
        sheet.fill(rect, red[1])
        if name in ("top", "bottom"):
            continue
        sheet.fill((x, y, w, S), red[2])
        sheet.fill((x, y + h - 2 * S, w, S), gold[3])
        sheet.fill((x, y + h - S, w, S), gold[2])
        if name in ("right", "left"):
            # The Horseman's cipher at the corner: a gold crown over a ring.
            sheet.fill((x + w - 4 * S, y + h - 5 * S, 2 * S, S), gold[3])
            sheet.fill((x + w - 4 * S, y + h - 4 * S, 2 * S, S // 2), gold[2])
    leather = GHOST[:5] if ghost else LEATHER
    for part in ("seat", "pommel", "cantle", "stirrup_leather"):
        gloss(sheet, part, leather)
    metal(sheet, "stirrup", GHOST[1:5] if ghost else IRON)
    if ghost:
        return
    jack(sheet, "bag_pumpkin", holes=None)
    for name, rect in sheet.faces("bag_net").items():
        x, y, w, h = rect
        # A rope net: a grid of rope over clear holes (the sheet is a cut-out).
        for i in range(0, w, 2 * S):
            sheet.fill((x + i, y, 1, h), ROPE[2])
        for j in range(0, h, 2 * S):
            sheet.fill((x, y + j, w, 1), ROPE[1])


# ------------------------------------------------------------------------------------------------- the trooper

def paint_trooper(sheet, ghost=False):
    blue = GHOST[:5] if ghost else BLUE
    red = GHOST[1:5] if ghost else RED
    white = GHOST[2:] if ghost else WHITE_BELT
    gold = GHOST[1:] if ghost else BRASS
    buff = GHOST[1:5] if ghost else BUFF
    leather = GHOST[:5] if ghost else LEATHER
    # His coat: dark blue, red facings either side of its buttoned front, white crossbelts crossing on the chest and the
    # back, brass buttons, a black belt.
    for name, rect in sheet.faces("coat").items():
        x, y, w, h = rect
        if name in ("top", "bottom"):
            sheet.fill(rect, blue[1])
            continue
        folds(sheet, rect, blue, period=3 * S, shade_bottom=False)
        if name == "front":
            sheet.fill((x + w // 2 - 2 * S, y, S + S // 2, h - 3 * S), red[2])
            sheet.fill((x + w // 2 + S // 2, y, S + S // 2, h - 3 * S), red[2])
            for j in range(S, h - 3 * S, 2 * S):
                sheet.fill((x + w // 2 - 1, y + j, 2, 2), gold[3])
        if name in ("front", "back"):
            for i in range(h - 3 * S):
                for k in range(S):
                    sheet.put(x + round(i * w / (h - 3 * S)) + k - S // 2, y + i, white[2])
                    sheet.put(x + w - 1 - round(i * w / (h - 3 * S)) - k + S // 2, y + i, white[1])
        sheet.fill((x, y + h - 3 * S, w, S), leather[1])
        sheet.fill((x + w // 2 - S // 2, y + h - 3 * S, S, S), gold[3])
    for name, rect in sheet.faces("coat_tail").items():
        x, y, w, h = rect
        folds(sheet, rect, blue, period=3 * S, phase=1)
        if name == "back":
            sheet.fill((x + w // 2 - S // 2, y, S, h), red[2])
    for part in ("sleeve", "forearm"):
        cloth(sheet, part, blue, phase=1, hem=False)
    for part in ("gauntlet", "glove"):
        gloss(sheet, part, leather)
    # His collar: the coat's blue, a red band round its top edge, charred black on top round the embers.
    for name, rect in sheet.faces("collar").items():
        x, y, w, h = rect
        if name == "top":
            sheet.fill(rect, (GHOST if ghost else CLOAK)[0])
            continue
        sheet.fill(rect, blue[2])
        sheet.fill((x, y, w, S), red[2])
        sheet.fill((x, y, w, 1), red[3])
    for name, rect in sheet.faces("epaulette").items():
        x, y, w, h = rect
        sheet.fill(rect, gold[2])
        sheet.fill((x, y, w, 1), gold[4] if len(gold) > 4 else gold[3])
        if name not in ("top", "bottom"):
            for i in range(0, w, 2):
                sheet.fill((x + i, y + h - S, 1, S), gold[1])
    # His embers: the glow inside his empty collar.
    for name, rect in sheet.faces("embers").items():
        x, y, w, h = rect
        pal = GHOST[1:] if ghost else EMBER
        for glow in (False, True):
            sheet.fill(rect, pal[1], glow=glow)
            for i in range(0, w, 3):
                for j in range(i % 2, h, 3):
                    sheet.fill((x + i, y + j, 2, 2), pal[3], glow=glow)
            sheet.fill((x + w // 3, y + h // 3, max(1, w // 3), max(1, h // 3)), pal[4], glow=glow)
    cloth(sheet, "breeches", buff)
    cloth(sheet, "thigh", buff, phase=1)
    for part in ("boot", "boot_cuff", "boot_foot"):
        gloss(sheet, part, leather)
    metal(sheet, "spur", GHOST[2:] if ghost else BRASS)
    # His cloak: black in folds, the lower part's hem cut ragged.
    cloak = GHOST[:4] if ghost else CLOAK
    for part, phase in (("cloak", 0), ("cloak_low", 2)):
        for name, rect in sheet.faces(part).items():
            if name in ("top", "bottom"):
                sheet.fill(rect, cloak[1])
                continue
            folds(sheet, rect, cloak, period=3 * S, phase=phase, shade_bottom=part == "cloak_low")
            if part == "cloak_low" and name in ("front", "back"):
                ragged(sheet, rect, every=3 * S, depth=2 * S, offset=phase * S)


def paint_sabre(sheet, ghost=False):
    """The sabre: a black grip bound in brass wire, a brass guard and knuckle-bow, a curved steel blade, its edge bright
    and a dark fuller along its back."""
    gold = GHOST[1:] if ghost else BRASS
    steel = GHOST[1:] if ghost else STEEL
    for name, rect in sheet.faces("hilt").items():
        x, y, w, h = rect
        sheet.fill(rect, (GHOST if ghost else LEATHER)[1])
        for j in range(0, h, 2):
            sheet.fill((x, y + j, w, 1), gold[2])
    metal(sheet, "guard", gold)
    metal(sheet, "knuckle_bow", gold)
    for i in range(len(hm.SABRE)):
        for name, rect in sheet.faces(f"blade_{i}").items():
            x, y, w, h = rect
            sheet.fill(rect, steel[2])
            if name in ("right", "left"):
                sheet.fill((x, y, max(1, w // 3), h), steel[4])          # the edge, bright
                sheet.fill((x + w - max(1, w // 3), y, max(1, w // 3), h), steel[1])
                sheet.fill((x + w // 2, y, 1, h), steel[0])               # the fuller
            elif name == "top":
                sheet.fill(rect, steel[3])


def paint_lantern(sheet):
    """The iron lantern: a bail, cap and foot of dark iron, its cage four bars a side round open panes, and the fire
    inside it, orange or white (both on the glowmask)."""
    for part in ("lantern_bail", "lantern_cap", "lantern_foot"):
        metal(sheet, part, IRON)
    for name, rect in sheet.faces("lantern_cage").items():
        x, y, w, h = rect
        if name in ("top", "bottom"):
            sheet.fill(rect, IRON[1])
            continue
        # Open panes: only the frame and a middle bar are drawn; the rest stays clear, so the fire shows through.
        sheet.fill((x, y, w, S // 2), IRON[2])
        sheet.fill((x, y + h - S // 2, w, S // 2), IRON[1])
        sheet.fill((x, y, S // 2, h), IRON[2])
        sheet.fill((x + w - S // 2, y, S // 2, h), IRON[1])
        sheet.fill((x + w // 2 - 1, y, 2, h), IRON[2])
    for part, pal in (("fire", FLAME), ("fire_white", WHITE)):
        for name, rect in sheet.faces(part).items():
            x, y, w, h = rect
            for glow in (False, True):
                sheet.fill(rect, pal[1], glow=glow)
                sheet.fill((x + 1, y + h // 3, max(1, w - 2), h - h // 3 - 1), pal[2], glow=glow)
                sheet.fill((x + w // 3, y + h // 2, max(1, w // 3), max(1, h // 3)), pal[3], glow=glow)


def paint_jacks(sheet):
    """The pumpkins he holds and carries: the held pumpkin (its small face lit), and the jack-o'-lantern head on his
    pommel or in his hand, its face carved, its fire on the face planes: orange, and white for when he is revealed."""
    jack(sheet, "held", holes={"eye_right": (1.25, 1.25, 1.25, 1.0), "eye_left": (3.5, 1.25, 1.25, 1.0),
                               "grin": (1.25, 3.0, 3.5, 0.75)}, offset=(1, 1))
    flames(sheet, "held_face", {"eye_right": (1.25, 1.25, 1.25, 1.0), "eye_left": (3.5, 1.25, 1.25, 1.0),
                                "grin": (1.25, 3.0, 3.5, 0.75)}, FLAME, offset=(1, 1))
    jack(sheet, "jack")
    for name, rect in sheet.faces("jack_stem").items():
        sheet.fill(rect, STEM[2])
        sheet.fill((rect[0], rect[1], rect[2], 1), STEM[3])
    flames(sheet, "jack_face", JACK_HOLES, FLAME, offset=(1, 1))
    flames(sheet, "jack_face_white", JACK_HOLES, WHITE, offset=(1, 1))


def hessian_horseman():
    sheet = Sheet(hm.hessian_horseman_model())
    paint_horse(sheet)
    paint_saddle(sheet)
    paint_trooper(sheet)
    paint_sabre(sheet)
    paint_lantern(sheet)
    paint_jacks(sheet)
    return sheet


def phantom_rider():
    """A phantom rider: the horse and trooper in soul fire, the whole of it on the glowmask too."""
    sheet = Sheet(hm.phantom_rider_model())
    paint_horse(sheet, ghost=True)
    paint_saddle(sheet, ghost=True)
    paint_trooper(sheet, ghost=True)
    paint_sabre(sheet, ghost=True)
    sheet.glow = sheet.img.copy()
    sheet.gx = sheet.glow.load()
    return sheet


# ------------------------------------------------------------------------------------------------- what he throws

def hessian_pumpkin():
    sheet = Sheet(hm.hessian_pumpkin_model())
    holes = {"eye_right": (1.5, 1.0, 1.5, 1.25), "eye_left": (4.5, 1.0, 1.5, 1.25), "grin": (1.5, 3.0, 4.5, 0.75)}
    for name, rect in sheet.faces("small_pumpkin").items():
        x, y, w, h = rect
        if name in ("top", "bottom"):
            sheet.fill(rect, PUMPKIN[3] if name == "top" else PUMPKIN[1])
        else:
            ribbed(sheet, rect, PUMPKIN, rib=3)
            if name == "front":
                carve(sheet, (x + round(1.5 * S), y + 2 * S, w, h), holes)
    for name, rect in sheet.faces("small_stem").items():
        sheet.fill(rect, STEM[2])
    flames(sheet, "small_face", holes, FLAME)
    return sheet


def hessian_head():
    sheet = Sheet(hm.hessian_head_model())
    holes = {"eye_right": (1.0, 1.0, 3.0, 2.5), "eye_left": (6.0, 1.0, 3.0, 2.5), "nose": (4.5, 3.75, 1.0, 1.0),
             "grin": (1.0, 5.5, 8.0, 1.75)}
    for name, rect in sheet.faces("big_pumpkin").items():
        x, y, w, h = rect
        if name in ("top", "bottom"):
            sheet.fill(rect, PUMPKIN[3] if name == "top" else PUMPKIN[1])
        else:
            ribbed(sheet, rect, PUMPKIN, rib=3)
            if name == "front":
                carve(sheet, (x + 2 * S, y + 2 * S, w, h), holes)
    for name, rect in sheet.faces("big_stem").items():
        sheet.fill(rect, STEM[2])
        sheet.fill((rect[0], rect[1], max(1, rect[2] // 3), rect[3]), STEM[3])
    flames(sheet, "big_face", holes, FLAME)
    return sheet


# ------------------------------------------------------------------------------------------------- the worn tricorn

def _square(fill):
    return Image.new("RGBA", (16, 16), tuple(fill) + (255,))


def tricorn_felt():
    """The worn tricorn's felt (16 x 16): black, a faint nap in diagonal rows, lighter toward the top."""
    img = _square(LEATHER[2])
    px = img.load()
    for x in range(16):
        for y in range(16):
            if (x + y) % 4 == 0:
                px[x, y] = tuple(LEATHER[3]) + (255,)
            if y > 12:
                px[x, y] = tuple(LEATHER[1]) + (255,)
    return img


def tricorn_braid():
    """The worn tricorn's braid (16 x 16): tarnished gold, twisted in short diagonals."""
    img = _square(BRASS[2])
    px = img.load()
    for x in range(16):
        for y in range(16):
            if (x + y) % 3 == 0:
                px[x, y] = tuple(BRASS[3]) + (255,)
            elif (x + y) % 3 == 2:
                px[x, y] = tuple(BRASS[1]) + (255,)
    return img


def hessian_horseman_textures():
    """Every texture of the Headless Horseman's lair fight: (folder, name) -> image."""
    out = {}
    for entity, build in (("hessian_horseman", hessian_horseman), ("phantom_rider", phantom_rider),
                          ("hessian_pumpkin", hessian_pumpkin), ("hessian_head", hessian_head)):
        sheet = build()
        out[("entity", entity)] = sheet.img
        out[("entity", f"{entity}_glowmask")] = sheet.glow
    out[("item", "hessians_tricorn_felt")] = tricorn_felt()
    out[("item", "hessians_tricorn_braid")] = tricorn_braid()
    for item in ("hessians_spur", "hessians_tricorn"):
        out[("item", item)] = item_icons.draw(item)
    return out


if __name__ == "__main__":
    import sys
    for (folder, name), image in hessian_horseman_textures().items():
        image.save(f"{sys.argv[1]}/{name}.png")
