"""Berserker: the owner's horned red-and-white design (the first of the three they sent on 8 October 2026, "I made these
3": a Blockbench render of the set worn, from a little to the side, beside each piece on its own) as a 3D worn model
for jugcraft:berserker_* (helmet, chestplate, leggings, boots), on the toolkit in tools/armor_models.py (shapes) and
tools/armor_paint.py (the atlas).

What the owner drew, and where it is here (the owner asked for "complex models ... really capturing the crazy unique
geometry of each armor", so each shape is built, not painted):
    helmet      an open-faced white cap with a lump of red on its crown that spills down over the brow; on each side a
                square horn standing straight up from a short foot at the cap's side; under it, round the jaw, a thin
                white frame with teeth standing up from its chin bar. Here: the cap and a rim round its foot; the
                crest, a smaller lump on it, its front down the brow and two drips under that; on each side the horn,
                its narrower tip, a grey band round its root and its foot; the jaw frame: a post down each front
                corner, the chin bar, four teeth on it, a bar back along each side of the jaw and a post up from its
                end to the cap
    chestplate  a white breastplate keyed in grey with red bands down its sides and over the shoulders, a red band at
                the waist parted at the middle under a grey one, stepped red pauldrons edged in grey and white with a
                white line on their inner side, and plain white bracers on the forearms (the upper arms bare). Here:
                the breastplate; on each side a red band raised on its front, over the shoulder and down its back; a
                grey hoop and a red hoop round the waist; on each arm the pauldron, its grey-and-white trim, a raised
                step and a crown on its outer part, a white ridge along its inner edge; the bracer with a flange at
                each end and two studs on its outer side
    leggings    thigh guards banded red and white with a red edge, over dark grey mail; a red and grey belt. Here: the
                belt and a white buckle on the body; on each leg the mail and four bands stepping out as they go down,
                white, red, white, red
    boots       grey boots with a dark band and red soles. Here: the boot, a white cuff round its top, a dark band
                round its ankle, the sole and a red toe cap
Colours: armor_paint.BERSERKER, sampled from the render (white and greys, the reds, the dark greys of the mail).

The render shows the front and the pieces from the front; the back is drawn in the design's own words (the bands run
round, the red edges repeat behind). Every box is closed: a face is left out only where another box of the same piece
and bone covers it (the crest's foot and front, the reliefs' backs, the horn's foot at both ends, the teeth's feet, the
chin bar's ends, the steps' feet, each band's foot inside the next, the boot under the sole), so the set shows no holes
alone, on an armor stand's thin limbs (tools/art_check.py, H1).

Space (tools/armor_models.py): each bone's own space in model pixels; x is the model's LEFT, y is DOWN, z is the BACK
(the face is at -z). Right-hand parts are built and mirrored to the left.
Preview: python3 tools/armor_preview.py --set berserker
"""
from dataclasses import replace

import armor_models as am
from armor_paint import BERSERKER, P


def rows(*tones):
    """One tone per texel row, cycling, each row broken into runs of the next tone darker."""
    return P("lames", rows=1, tones=tuple((t, t) for t in tones), centre=None)


def solid(tone):
    return P("solid", tone=tone)


def marks(*rects, symmetric=False):
    return P("marks", rects=list(rects), symmetric=symmetric)


def plate(tone, strips="h"):
    return P("plate", tone=tone, strips=strips)


H, L, M, D, S, V = "light", "mid_light", "mid", "dark", "seam", "void"       # the white plate, light to dark
G, g = "gold_light", "gold_dark"                                            # the brightest whites
C, c, q, Q, K = "leather_light", "leather_mid_light", "leather_mid", "leather_dark", "leather_darkest"   # the reds
U, u, x, X = "under_light", "under_mid", "under_dark", "under_darkest"       # the mail and dark greys


# ---------------------------------------------------------------- paint
WHITE_BAND = {"front": [plate(H), marks((-1, 0, 1, 99, C))], "top": solid(H), "bottom": solid(S), "*": plate(L)}
RED_BAND = {"front": [plate(c), marks((-1, 0, 1, 99, C))], "top": solid(C), "bottom": solid(Q), "*": plate(c)}
PAINT = {
    "cap": {"bottom": solid(S), "*": plate(L)},
    "rim": {"top": solid(H), "bottom": solid(D), "*": [solid(M), marks((0, 0, 99, 1, L))]},
    "crest": {"*": plate(c)},
    "crest_lump": {"top": plate(C), "*": plate(c)},
    "crest_front": {"front": [plate(c), marks((0, -1, 1, 1, q), (-2, -1, 1, 1, q))], "*": plate(c)},
    "drip": {"bottom": solid(Q), "*": [solid(c), marks((0, -1, 99, 1, q))]},
    "horn": {"top": solid(H), "bottom": solid(S), "*": [plate(L, "v"), marks((0, 0, 99, 1, H))]},
    "horn_tip": {"top": solid(G), "*": [plate(H, "v"), marks((0, 0, 99, 1, G))]},
    "horn_band": {"top": solid(L), "bottom": solid(S), "*": rows(M, D)},
    "horn_foot": {"*": plate(M)},
    "jaw": {"top": solid(H), "bottom": solid(D), "*": [solid(L), marks((0, 0, 99, 1, H))]},
    "tooth": {"top": solid(G), "*": [solid(H), marks((0, -1, 99, 1, M))]},
    # the breastplate: white keyed in grey; the red bands are raised on it
    "breast": {"front": [plate(H), marks((3, 2, 1, 3, M), (3, 4, 2, 1, M), (4, 6, 1, 2, M), (3, 7, 1, 1, M),
                                         symmetric=True)],
               "back": plate(L), "top": solid(L), "*": [plate(L), marks((0, 0, 99, 1, H))]},
    "band": {"front": [solid(c), marks((0, 0, 1, 99, C))], "back": [solid(c), marks((0, 0, 1, 99, C))],
             "top": solid(C), "bottom": solid(Q), "*": solid(q)},
    # the waist: a grey hoop over a red one, the red parted at the middle in front and behind
    "fauld_grey": {"top": solid(L), "bottom": solid(S), "*": rows(M, D)},
    "fauld_red": {"top": solid(C), "bottom": solid(K),
                  "front": [rows(c, q), marks((4, 0, 2, 99, S), (0, -1, 99, 1, Q))],
                  "back": [rows(c, q), marks((4, 0, 2, 99, S), (0, -1, 99, 1, Q))],
                  "*": [rows(c, q), marks((0, -1, 99, 1, Q))]},
    # the pauldron: red, its grey-and-white trim a band of its own, a white line down its inner side (texture-right on
    # the front, texture-left behind; mirrored onto the left arm), a white ridge along its top's inner edge
    "pauldron": {"top": plate(c), "bottom": solid(Q),
                 "front": [plate(c), marks((-1, 0, 1, 99, H))], "back": [plate(c), marks((0, 0, 1, 99, H))],
                 "*": plate(c)},
    "trim": {"top": solid(M), "bottom": solid(D), "*": [solid(H), marks((0, 0, 99, 1, M))]},
    "step": {"top": plate(C), "*": [plate(c), marks((0, 0, 99, 1, C))]},
    "ridge": {"top": solid(G), "*": [solid(H), marks((0, -1, 99, 1, L))]},
    "bracer": {"top": solid(M), "bottom": solid(S), "*": [plate(L), marks((0, 0, 99, 1, H))]},
    "flange": {"top": solid(H), "bottom": solid(D), "*": [solid(H), marks((0, -1, 99, 1, M))]},
    "stud": {"top": solid(H), "*": solid(M)},
    "belt": {"top": solid(M), "bottom": solid(X), "sides": [rows(c, q), marks((0, 0, 99, 1, M))]},
    "buckle": {"front": P("chevron", corner="o", bands=(H, M), border=None, core=(L, D), outside=H), "*": solid(L)},
    "mail": {"sides": P("checker", tones=(x, u)), "ends": solid(X)},
    "boot": {"top": solid(X), "bottom": solid(X), "*": [plate(M), marks((0, -1, 99, 1, D))]},
    "cuff": {"top": solid(H), "bottom": solid(M), "*": [solid(H), marks((0, -1, 99, 1, L))]},
    "ankle": {"top": solid(u), "bottom": solid(X), "*": rows(x, u)},
    "sole": {"top": solid(C), "bottom": solid(K), "*": [solid(c), marks((0, 0, 99, 1, C), (0, -1, 99, 1, Q))]},
    "toe": {"top": solid(C), "bottom": solid(K), "*": [solid(c), marks((0, 0, 99, 1, C))]},
}


# ---------------------------------------------------------------- the helmet
def helmet():
    """The cap 0.85 clear of the head's sides and 0.75 of its front and back, its bottom 0.65 above the eyes, a rim
    0.2 proud round its foot; the crest, the horns and the jaw frame."""
    horn = [am.span("horn_right", (-8.5, -12.25, -1.5), (-5.5, -5.25, 1.5), paint=PAINT["horn"]),
            am.span("horn_tip_right", (-8.25, -13.75, -1.25), (-5.75, -12.25, 1.25), skip="bottom",
                    paint=PAINT["horn_tip"]),
            am.span("horn_band_right", (-8.75, -7.75, -2.0), (-5.25, -6.75, 2.0), paint=PAINT["horn_band"]),
            # the foot bridges the horn to the cap's side, its ends inside the horn and the cap
            am.span("horn_foot_right", (-5.75, -7.25, -1.25), (-4.75, -5.5, 1.25), skip=("right", "left"),
                    paint=PAINT["horn_foot"])]
    # the jaw frame, 0.75 to 1.65 in front of the face and beside the jaw (clear of the hat layer)
    jaw = [am.span("jaw_post_right", (-4.9, -4.65, -5.65), (-3.9, -1.25, -4.75), skip="bottom", paint=PAINT["jaw"]),
           am.span("jaw_side_right", (-5.65, -1.25, -5.65), (-4.75, 0.0, 1.3), paint=PAINT["jaw"]),
           am.span("jaw_back_right", (-5.65, -4.65, 0.4), (-4.75, -1.25, 1.3), skip="bottom", paint=PAINT["jaw"]),
           am.span("tooth_right", (-2.25, -2.25, -5.5), (-1.25, -1.25, -4.9), skip="bottom", paint=PAINT["tooth"]),
           am.span("tooth_outer_right", (-3.5, -2.0, -5.45), (-2.75, -1.25, -4.95), skip="bottom",
                   paint=PAINT["tooth"])]
    return [am.span("cap", (-4.85, -8.75, -4.75), (4.85, -4.65, 4.75), paint=PAINT["cap"]),
            am.span("rim", (-5.05, -5.45, -4.95), (5.05, -4.45, 4.95), paint=PAINT["rim"]),
            am.span("crest", (-2.5, -10.25, -4.75), (2.5, -8.75, 1.25), skip=("bottom", "front"),
                    paint=PAINT["crest"]),
            am.span("crest_lump", (-1.0, -10.95, -3.5), (1.75, -10.25, -0.5), skip="bottom",
                    paint=PAINT["crest_lump"]),
            am.span("crest_front", (-2.5, -10.25, -5.25), (2.5, -5.75, -4.75), skip="back",
                    paint=PAINT["crest_front"]),
            am.span("drip_right", (-2.0, -5.75, -5.15), (-1.0, -4.95, -4.75), skip=("back", "top"),
                    paint=PAINT["drip"]),
            am.span("drip_left", (0.75, -5.75, -5.15), (1.75, -5.15, -4.75), skip=("back", "top"),
                    paint=PAINT["drip"]),
            am.span("chin_bar", (-4.75, -1.25, -5.65), (4.75, 0.0, -4.75), skip=("right", "left"), paint=PAINT["jaw"]),
            *horn, *am.mirror_all(horn), *jaw, *am.mirror_all(jaw)]


# ---------------------------------------------------------------- the chestplate
def body():
    """The breastplate, 0.65 off the body's sides and 0.85 off its front and back; on each side a red band 0.55 proud
    of its front and back and 0.3 over its top; the grey hoop and the red hoop round the waist."""
    band = [am.span("band_front_right", (-4.65, -0.95, -3.4), (-3.15, 9.0, -2.85), skip="back", paint=PAINT["band"]),
            am.span("band_top_right", (-4.65, -0.95, -2.85), (-3.15, -0.65, 2.85), skip=("bottom", "front", "back"),
                    paint=PAINT["band"]),
            am.span("band_back_right", (-4.65, -0.95, 2.85), (-3.15, 9.0, 3.4), skip="front", paint=PAINT["band"])]
    return [am.span("breast", (-4.65, -0.65, -2.85), (4.65, 9.0, 2.85), skip="bottom", paint=PAINT["breast"]),
            *band, *am.mirror_all(band),
            am.span("fauld_grey", (-4.95, 8.6, -3.1), (4.95, 9.6, 3.1), skip="bottom", paint=PAINT["fauld_grey"]),
            am.span("fauld_red", (-5.15, 9.35, -3.3), (5.15, 11.1, 3.3), paint=PAINT["fauld_red"])]


def arm():
    """Right arm space: x -3..1, y -2..10 (the shoulder at y -2), z -2..2. The pauldron 1.25 past the arm's outer
    side and 0.75 above it, its trim round its foot, its step and crown over its outer part, the ridge on its inner
    edge; the bracer 0.6 off the forearm with a flange at each end (the wrist's 1.05 off the arm's front and back,
    clear of the belt's and the thigh bands' planes) and two studs on its outer side."""
    return [am.span("pauldron_right", (-4.25, -2.75, -3.05), (1.65, 1.0, 3.05), skip="bottom",
                    paint=PAINT["pauldron"]),
            am.span("trim_right", (-4.45, 0.75, -3.25), (1.85, 1.75, 3.25), paint=PAINT["trim"]),
            am.span("step_right", (-4.0, -3.75, -2.7), (-0.25, -2.75, 2.7), skip="bottom", paint=PAINT["step"]),
            am.span("crown_right", (-3.6, -4.5, -2.2), (-1.2, -3.75, 2.2), skip="bottom", paint=PAINT["step"]),
            am.span("ridge_right", (0.65, -3.05, -3.05), (1.65, -2.75, 3.05), skip="bottom", paint=PAINT["ridge"]),
            am.span("bracer_right", (-3.6, 5.25, -2.45), (1.6, 10.45, 2.45), skip=("top", "bottom"),
                    paint=PAINT["bracer"]),
            am.span("flange_top_right", (-3.85, 4.9, -2.7), (1.85, 5.75, 2.7), paint=PAINT["flange"]),
            am.span("flange_wrist_right", (-4.0, 9.6, -3.05), (2.0, 10.6, 3.05), paint=PAINT["flange"]),
            *am.rivets("stud_right", (-3.6, 7.5, -1.0), (0.0, 0.0, 2.0), 2, size=(0.5, 0.75, 0.75), face="right",
                       paint=PAINT["stud"])]


# ---------------------------------------------------------------- the leggings and boots
LEFT_OUT = 0.12   # the left leg's parts this much larger, so where the legs meet their faces never share a plane
# How far each thigh band stands off the leg: stepping out down the thigh, and clear of vanilla leggings' 1.0 shell
# (0.9 to 1.1) on both legs (the left's LEFT_OUT further out)
THIGH_OUT = (0.77, 1.15, 1.3, 1.45)


def waist():
    """The belt, 0.6 off the body's front and back; the buckle 1.15 off its front (clear of vanilla leggings' 1.0
    shell and of the thigh bands' planes)."""
    return [am.span("belt", (-4.45, 10.25, -2.6), (4.45, 12.65, 2.6), paint=PAINT["belt"]),
            am.span("buckle", (-1.25, 10.9, -3.15), (1.25, 12.25, -2.6), skip="back", paint=PAINT["buckle"])]


def leg():
    """Right leg space: x -2..2, y 0..12 (the hip at 0), z -2..2. The mail starts 0.8 down, under the belt; the four
    thigh bands overlap 0.25, each further out than the one above (THIGH_OUT), their inner edge 0.75 inside the leg
    or more so the mail shows between the two legs' bands."""
    out = [am.span("mail_right", (-2.45, 0.8, -2.45), (2.45, 7.0, 2.45), paint=PAINT["mail"])]
    for i, off in enumerate(THIGH_OUT):
        top = -0.45 + 1.6 * i
        # the first band wraps the whole thigh, up under the belt, so a leg swung back shows it at the hip, not the
        # wearer; below it each band's inner edge is 0.15 further in, so no two share that plane either
        inner = 2 + off if i == 0 else 1.25 + 0.15 * i
        out.append(am.span(f"thigh_{i}_right", (-2 - off, top, -2 - off), (inner, top + 1.85, 2 + off),
                           skip="bottom" if i < 3 else (), paint=WHITE_BAND if i % 2 == 0 else RED_BAND))
    return out


def boot():
    """The boot 0.75 off the leg, its white cuff 1.05 off, the dark ankle band 1.0 off; the sole and the toe cap."""
    return [am.span("boot_right", (-2.75, 6.75, -2.75), (2.75, 11.4, 2.75), skip=("top", "bottom"),
                    paint=PAINT["boot"]),
            am.span("cuff_right", (-3.05, 6.5, -3.05), (3.05, 7.5, 3.05), paint=PAINT["cuff"]),
            am.span("ankle_right", (-3.0, 8.75, -3.0), (3.0, 9.75, 3.0), paint=PAINT["ankle"]),
            am.span("sole_right", (-2.95, 11.15, -3.25), (2.95, 12.65, 3.05), paint=PAINT["sole"]),
            am.span("toe_right", (-2.4, 11.4, -3.85), (2.4, 12.3, -3.25), skip="back", paint=PAINT["toe"])]


def left(parts):
    return [replace(p, inflate=LEFT_OUT) for p in am.mirror_all(parts)]


def model():
    a, l, b = arm(), leg(), boot()
    return {"berserker_helmet": {"head": helmet()},
            "berserker_chestplate": {"body": body(), "right_arm": a, "left_arm": am.mirror_all(a)},
            "berserker_leggings": {"body": waist(), "right_leg": l, "left_leg": left(l)},
            "berserker_boots": {"right_leg": b, "left_leg": left(b)}}


SETS = [am.ArmorSet("berserker", BERSERKER, model())]
