"""Scarab: the owner's gold-and-lapis Egyptian set (one of the designs they sent on 8 October 2026 with no words: a
render of the set worn, from behind on the left and from the front, beside its four 16x16 icons) as a 3D worn model for
jugcraft:scarab_* (helmet, chestplate, leggings, boots), on the toolkit in tools/armor_models.py (shapes) and
tools/armor_paint.py (the atlas). Its icons are the owner's, transcribed to maps (tools/armor_icons/scarab/). The name
is a placeholder: the owner's Pharaoh Armor (tools/pharaoh_armor.py) is another set.

What the owner drew, and where it is here (built part for part, as the owner asked: "complex models ... really
capturing the crazy unique geometry of each armor"):
    helmet      a nemes headcloth striped gold and lapis: a band across the brow, the cloth over the crown and down
                behind the head, flaring out past it, and its lappets falling either side of the face onto the chest;
                the face open. Here: the crown over the head, the brow band round it (0.2 proud), the lappets either
                side of the face down past the chin, the cloth down behind the head to the neck, flaring 0.2 wider,
                three raised lapis bands round it, and a cobra rearing on the brow
    chestplate  gold plate: a broad collar of gold and lapis rows on the chest, lapis bands down either side of the
                belly and a lapis belt; on the shoulders square guards striped gold and lapis down to the elbow; the
                forearms bare. Here: the cuirass, the collar in three stepped rows, the two lapis bands, the gold plate
                between them, the belt; the back plate; on each arm the guard, a raised lapis band round it and a gold
                band round its foot
    leggings    a gold kilt to above the knee, a lapis key pattern down its front. Here: the kilt round each leg, loose
                as a kilt, and its front panel, hinged out a little from the waist
    boots       tall gold boots chequered in lapis. Here: the boot from below the knee, two raised bands round it, the
                gold sole and toe
Colours: armor_paint.SCARAB.

The forearms are bare, as drawn. The renders show the front and the left side from behind; the right side is drawn
from them. Every box is closed: a face is left out only where another box of the same piece and bone covers it.

Space (tools/armor_models.py): each bone's own space in model pixels; x is the model's LEFT, y is DOWN, z is the BACK
(the face is at -z).
Preview: python3 tools/armor_preview.py --set scarab
"""
from dataclasses import replace

import armor_models as am
from armor_paint import SCARAB, P


def rows(*tones):
    """One tone per texel row, cycling, each row broken into runs of the next tone darker."""
    return P("lames", rows=1, tones=tuple((t, t) for t in tones), centre=None)


def solid(tone):
    return P("solid", tone=tone)


def marks(*rects, symmetric=False):
    return P("marks", rects=list(rects), symmetric=symmetric)


def plate(tone, strips="h"):
    return P("plate", tone=tone, strips=strips)


H, L, M, D, S, V = "light", "mid_light", "mid", "dark", "seam", "void"      # the gold, pale to brown
G, g = "gold_light", "gold_dark"                                           # its brightest and its deepest
C, c, q, Q, K = "leather_light", "leather_mid_light", "leather_mid", "leather_dark", "leather_darkest"   # the lapis
U, u, x, X = "under_light", "under_mid", "under_dark", "under_darkest"      # the shadow under the headcloth

GOLD = plate(M)
STRIPES = rows(L, M, c, Q)   # the nemes: gold and lapis bands, a texel each


# ---------------------------------------------------------------- paint
PAINT = {
    "crown": {"top": [rows(c, M), marks((0, 0, 99, 1, C))], "bottom": solid(U), "*": STRIPES},
    "brow": {"top": solid(H), "bottom": solid(D),
             "*": [solid(L), marks((0, 0, 99, 1, H), (0, -1, 99, 1, M), (3, 0, 1, 99, c), (7, 0, 1, 99, c))]},
    "lappet": {"front": [rows(L, M, c, Q), marks((0, 0, 1, 99, H), (-1, 0, 1, 99, D))], "bottom": solid(D),
               "*": STRIPES},
    "fall": {"top": solid(U), "bottom": solid(D), "*": STRIPES},
    "band": {"top": solid(C), "bottom": solid(Q), "*": [solid(c), marks((0, 0, 99, 1, C), (0, -1, 99, 1, Q))]},
    "cobra": {"front": [solid(H), marks((0, -1, 99, 1, M))], "top": solid(H), "*": solid(L)},
    "hood": {"front": [solid(L), marks((1, 1, 1, 1, c), (0, -1, 99, 1, D))], "top": solid(H), "*": solid(M)},
    "cuirass": {"top": [GOLD, marks((0, 0, 99, 1, L))], "bottom": solid(S), "*": GOLD},
    "collar": {"front": rows(L, c, M, Q), "top": solid(H), "bottom": solid(D), "*": solid(M)},
    "lapis": {"front": [solid(c), marks((0, 0, 1, 99, C), (-1, 0, 1, 99, Q))], "*": solid(q)},
    "plate": {"front": [plate(L), marks((0, 0, 99, 1, H), (0, -1, 99, 1, D))], "*": solid(M)},
    "belt": {"top": solid(c), "bottom": solid(K), "*": [solid(c), marks((0, 0, 99, 1, C), (0, -1, 99, 1, Q),
                                                                         (2, 1, 1, 1, L), (6, 1, 1, 1, L))]},
    "back_plate": {"back": [plate(L), marks((0, 0, 99, 1, H), (0, -1, 99, 1, D))], "*": solid(M)},
    "guard": {"top": [rows(c, M), marks((0, 0, 99, 1, C))], "bottom": solid(D), "*": STRIPES},
    "guard_band": {"top": solid(C), "bottom": solid(Q), "*": [solid(c), marks((0, 0, 99, 1, C))]},
    "cuff": {"top": solid(H), "bottom": solid(D), "*": [solid(L), marks((0, 0, 99, 1, H), (0, -1, 99, 1, M))]},
    "sash": {"top": solid(L), "bottom": solid(D), "sides": [solid(M), marks((0, 0, 99, 1, L), (0, -1, 99, 1, D))]},
    "kilt": {"top": solid(L), "bottom": solid(D), "*": [plate(M, "v"), marks((0, -1, 99, 1, S))]},
    "panel": {"front": [solid(M), marks((0, 0, 99, 1, L), (1, 1, 3, 1, c), (1, 1, 1, 3, c), (1, 3, 3, 1, c),
                                         (3, 3, 1, 3, c), (1, 5, 3, 1, c), (0, -1, 99, 1, D))],
              "*": solid(D)},
    "boot": {"top": solid(L), "bottom": solid(D), "*": P("checker", tones=(M, c), size=1)},
    "boot_band": {"top": solid(H), "bottom": solid(D), "*": [solid(L), marks((0, 0, 99, 1, H), (0, -1, 99, 1, M))]},
    "sole": {"top": solid(M), "bottom": solid(V), "*": [solid(D), marks((0, 0, 99, 1, M))]},
    "toe": {"top": solid(H), "bottom": solid(D), "*": [solid(L), marks((0, -1, 99, 1, M))]},
}


# ---------------------------------------------------------------- the helmet
def helmet():
    """The nemes, 0.75 clear of the head: the crown over it (its top 0.9 above the head, reaching 1.75 behind it),
    the brow band round its foot (0.6 past its sides, 0.2 before it), the lappets either side of the face (x 3..4.75)
    down 1.75 past the chin, the fall down behind the head to the neck, flaring 0.8 past the crown's sides and 0.25
    further back, three lapis bands raised round it, and the cobra rearing on the brow. Their sides stand at least 0.15
    clear of the body's, so none shares a plane with them as the head turns."""
    lappet = am.span("lappet_right", (-4.75, -6.6, -4.75), (-3.0, 1.75, 1.0), skip="top", paint=PAINT["lappet"])
    bands = [am.span(f"band_{i}", (-5.75, y0, 1.0), (5.75, y0 + 0.75, 6.2), paint=PAINT["band"])
             for i, y0 in enumerate((-6.2, -4.85, -2.4))]
    return [am.span("crown", (-4.75, -8.9, -4.75), (4.75, -6.6, 5.75), paint=PAINT["crown"]),
            am.span("brow", (-5.35, -7.6, -4.95), (5.35, -6.45, 6.15), paint=PAINT["brow"]),
            *am.pair(lappet),
            am.span("fall", (-5.55, -6.8, 0.0), (5.55, 1.0, 6.0), paint=PAINT["fall"]),
            *bands,
            am.span("cobra", (-0.45, -9.0, -5.45), (0.45, -7.4, -4.9), paint=PAINT["cobra"]),
            am.span("cobra_hood", (-0.85, -9.7, -5.6), (0.85, -8.8, -5.05), paint=PAINT["hood"])]


# ---------------------------------------------------------------- the chestplate
def body():
    """The cuirass, 0.95 off the body's sides (0.2 outside the nemes's crown) and 0.85 off its front and back; the
    broad collar on the chest in three rows, each narrower and 0.15 prouder than the one above; the two lapis bands
    down either side of the belly and the gold plate between them; the lapis belt round the waist, 1.15 off its sides and 1.05 off its front and back; the back
    plate."""
    collar = [am.span("collar_0", (-4.6, 0.25, -3.25), (4.6, 1.5, -2.85), skip="back", paint=PAINT["collar"]),
              am.span("collar_1", (-3.85, 1.5, -3.4), (3.85, 2.75, -2.85), skip="back", paint=PAINT["collar"]),
              am.span("collar_2", (-2.85, 2.75, -3.55), (2.85, 3.85, -2.85), skip="back", paint=PAINT["collar"])]
    lapis = am.span("lapis_right", (-3.6, 4.1, -3.15), (-2.6, 9.4, -2.85), skip="back", paint=PAINT["lapis"])
    return [am.span("cuirass", (-4.95, -0.65, -2.85), (4.95, 10.5, 2.85), paint=PAINT["cuirass"]),
            *collar, *am.pair(lapis),
            am.span("plate", (-2.3, 4.3, -3.25), (2.3, 9.2, -2.85), skip="back", paint=PAINT["plate"]),
            am.span("belt", (-5.15, 9.5, -3.05), (5.15, 10.85, 3.05), paint=PAINT["belt"]),
            am.span("back_plate", (-3.75, 0.75, 2.85), (3.75, 9.25, 3.25), skip="front", paint=PAINT["back_plate"])]


def arm():
    """The right arm (arm space: x -3..1, y -2..10, z -2..2, the outer side -x): the square guard over the shoulder and
    upper arm, 1.25 off its outer side, 1.6 off its front and back and 0.65 above it, open below; a lapis band raised round it and a gold band round its foot.
    The forearm is bare."""
    return [am.span("guard_right", (-4.25, -2.65, -3.6), (1.6, 4.4, 3.6), skip="bottom", paint=PAINT["guard"]),
            am.span("guard_band_right", (-4.45, 0.75, -3.8), (1.8, 1.75, 3.8), paint=PAINT["guard_band"]),
            am.span("cuff_right", (-4.45, 4.4, -3.8), (1.8, 5.25, 3.8), paint=PAINT["cuff"])]


# ---------------------------------------------------------------- the leggings and boots
LEFT_OUT = 0.12   # the left leg's parts this much larger, so where the legs meet their faces never share a plane


def waist():
    """The sash at the kilt's top, 0.6 off the body's front and back, under the belt."""
    return [am.span("sash", (-4.45, 10.25, -2.6), (4.45, 12.65, 2.6), paint=PAINT["sash"])]


def leg():
    """The right leg (leg space: x -2..2, y 0..12, z -2..2): the kilt round it, loose, 1.25 off (clear of vanilla
    leggings' 1.0 shell), from up under the sash to above the knee; its front panel with the lapis key, 1.65 to 1.9 off,
    hinged out 4 degrees from the waist, stopping short of the centre line."""
    return [am.span("kilt_right", (-3.25, -0.45, -3.25), (3.25, 6.5, 3.25), paint=PAINT["kilt"]),
            am.hinge(am.span("panel_right", (-2.3, 0.25, -3.9), (1.75, 6.75, -3.65), paint=PAINT["panel"]), "top", 4)]


def boot():
    """The boot from below the kilt, 1.0 off the leg (clear of vanilla boots' 0.5 shell and 0.25 inside the kilt),
    chequered gold and lapis; two gold bands round it, 1.35 off; the sole, closed 0.65 below the foot; the toe."""
    return [am.span("boot_right", (-3.0, 6.25, -3.0), (3.0, 11.75, 3.0), paint=PAINT["boot"]),
            am.span("boot_band_0_right", (-3.35, 7.25, -3.35), (3.35, 8.0, 3.35), paint=PAINT["boot_band"]),
            am.span("boot_band_1_right", (-3.35, 10.25, -3.35), (3.35, 11.0, 3.35), paint=PAINT["boot_band"]),
            am.span("sole_right", (-2.9, 11.75, -3.5), (2.9, 12.65, 2.75), paint=PAINT["sole"]),
            am.span("toe_right", (-2.0, 10.85, -4.0), (2.0, 11.45, -3.0), skip="back", paint=PAINT["toe"])]


def left(parts):
    return [replace(p, inflate=LEFT_OUT) for p in am.mirror_all(parts)]


def model():
    l, b = leg(), boot()
    a = arm()
    return {"scarab_helmet": {"head": helmet()},
            "scarab_chestplate": {"body": body(), "right_arm": a, "left_arm": am.mirror_all(a)},
            "scarab_leggings": {"body": waist(), "right_leg": l, "left_leg": left(l)},
            "scarab_boots": {"right_leg": b, "left_leg": left(b)}}


SETS = [am.ArmorSet("scarab", SCARAB, model())]
