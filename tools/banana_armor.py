"""Banana: the owner's banana costume (one of the designs they sent on 8 October 2026 with no words: a render of a player
inside a tall yellow banana, the face showing through a hole in it, a brown stem on top, the arms bare out of its sides
and the banana's brown end sticking out at the feet) as a 3D worn model for jugcraft:banana_* (helmet, chestplate,
leggings, boots), on the toolkit in tools/armor_models.py (shapes) and tools/armor_paint.py (the atlas). The name is a
placeholder.

What the owner drew, and where it is here:
    helmet      the banana's top: a tall yellow box round the head, the face showing through a hole in its front, a
                brown stem on its flat top. Here: the box built of five closed blocks round the hole (the crown above
                it, the chin below, a cheek either side, and the back between the cheeks behind the head), so the face
                shows in game; on top the banana's darker end and the stem
    chestplate  the banana's middle round the body, as deep as the head's box; the arms bare out of its sides. Here: the
                tube round the body, its ridges raised down its front and back, its speckles painted
    leggings    the banana's lower part round the legs, to below the knee. Here: a tube round each leg (so the legs
                can swing), meeting at the middle
    boots       the banana's bottom, its brown end sticking out forward by the right foot. Here: the tube's foot round
                each shin, darkening, and the brown end on the right
Colours: armor_paint.BANANA.

The arms and the feet are bare, as drawn. Every box is closed: a face is left out only where another box of the same
piece and bone covers it (the cheeks' and the back's ends inside the crown and the chin, the feet of the collar, the
stem and its tip on what each stands on, the nub's back on the brown end).

Space (tools/armor_models.py): each bone's own space in model pixels; x is the model's LEFT, y is DOWN, z is the BACK
(the face is at -z).
Preview: python3 tools/armor_preview.py --set banana
"""
from dataclasses import replace

import armor_models as am
from armor_paint import BANANA, P


def solid(tone):
    return P("solid", tone=tone)


def marks(*rects, symmetric=False):
    return P("marks", rects=list(rects), symmetric=symmetric)


def plate(tone, strips="v"):
    return P("plate", tone=tone, strips=strips)


H, L, M, D, S, V = "light", "mid_light", "mid", "dark", "seam", "void"     # the yellow, lit to shade
G, g = "gold_light", "gold_dark"                                          # its brown speckles
C, c, q, Q, K = "leather_light", "leather_mid_light", "leather_mid", "leather_dark", "leather_darkest"   # stem, end
U, u, x, X = "under_light", "under_mid", "under_dark", "under_darkest"     # inside the hole

SKIN = plate(M)   # the yellow skin, in runs down its length


def speckled(*spots):
    """The skin with brown speckles at (x, y) texels (negative: from the far edge)."""
    return [SKIN, marks(*[(sx, sy, 1, 1, g if i % 2 else G) for i, (sx, sy) in enumerate(spots)])]


# ---------------------------------------------------------------- paint
PAINT = {
    "crown": {"top": [solid(L), marks((0, 0, 99, 1, H))], "bottom": solid(U),
              "front": speckled((2, 1), (7, 3), (4, 6)), "back": speckled((1, 2), (6, 5)),
              "*": [speckled((3, 2), (1, 6)), marks((0, 0, 1, 99, D), (-1, 0, 1, 99, D))]},
    "chin": {"top": solid(U), "bottom": solid(S), "front": speckled((1, 0), (8, 1)), "*": SKIN},
    "cheek": {"top": solid(U), "bottom": solid(U), "right": [SKIN, marks((0, 0, 1, 99, D))], "left": solid(u),
              "front": SKIN, "*": SKIN},
    "back": {"back": speckled((2, 1)), "*": solid(u)},
    "collar": {"top": solid(D), "bottom": solid(S), "*": [solid(D), marks((0, 0, 99, 1, M), (1, 0, 1, 1, g))]},
    "stem": {"top": [solid(Q), marks((0, 0, 99, 1, q))], "bottom": solid(K),
             "*": [solid(q), marks((0, 0, 1, 99, C), (-1, 0, 1, 99, Q), (0, -1, 99, 1, Q))]},
    "tube": {"top": [solid(L), marks((0, 0, 99, 1, H))], "bottom": solid(S),
             "front": speckled((1, 2), (6, 4), (3, 9), (7, 11)), "back": speckled((2, 3), (5, 8), (1, 11)),
             "*": [speckled((2, 4), (4, 9)), marks((0, 0, 1, 99, D), (-1, 0, 1, 99, D))]},
    "ridge": {"*": [solid(M), marks((0, 0, 1, 99, L), (-1, 0, 1, 99, D))]},
    "leg": {"top": solid(L), "bottom": solid(S), "front": speckled((1, 1), (3, 5)), "*": SKIN},
    "foot": {"top": solid(L), "bottom": solid(Q), "*": [SKIN, marks((0, -1, 99, 1, g), (0, -2, 99, 1, D))]},
    "end": {"top": [solid(q), marks((0, 0, 99, 1, c))], "bottom": solid(K),
            "*": [solid(Q), marks((0, 0, 99, 1, q), (1, 1, 1, 1, K), (0, 0, 1, 99, q))]},
}


# ---------------------------------------------------------------- the helmet
def helmet():
    """The banana's top, 0.75 clear of the head, its box rising 5 above it and closed 0.65 below, built of closed
    blocks round the hole for the face (x -3..3, y -6.5..-1.5): the crown above the hole, the chin below it, a cheek
    either side reaching to the back, and the back between the cheeks behind the head (0.25 off it); on the crown's flat
    top the banana's darker end round the foot of the stem, and the stem, its tip bent over a little, its cut top
    dark."""
    cheek = am.span("cheek_right", (-4.75, -6.5, -4.75), (-3.0, -1.5, 4.75), skip=("top", "bottom"),
                    paint=PAINT["cheek"])
    return [am.span("crown", (-4.75, -13.0, -4.75), (4.75, -6.5, 4.75), paint=PAINT["crown"]),
            am.span("chin", (-4.75, -1.5, -4.75), (4.75, 0.65, 4.75), paint=PAINT["chin"]),
            *am.pair(cheek),
            am.span("back", (-3.0, -6.5, 4.25), (3.0, -1.5, 4.75), skip=("top", "bottom", "right", "left"),
                    paint=PAINT["back"]),
            am.span("collar", (-2.25, -13.5, -2.25), (2.25, -13.0, 2.25), skip="bottom", paint=PAINT["collar"]),
            am.span("stem", (-1.75, -16.5, -1.75), (1.75, -13.5, 1.75), skip="bottom", paint=PAINT["stem"]),
            am.box("stem_tip", (-1.25, -18.0, -1.25), (2.5, 1.75, 2.5), pivot=(0.0, -16.5, 0.0), rotation=(-8, 0, 14),
                   skip="bottom", paint=PAINT["stem"])]


# ---------------------------------------------------------------- the chestplate
def body():
    """The banana round the body: the tube 0.95 off its sides (0.2 outside the head's box, so the two never share a
    plane as the head turns) and 2.6 off its front and back, as deep as the head's box; two ridges raised down its front
    and two down its back, from below the head's box."""
    ridges = [am.span("ridge_front_right", (-3.1, 0.85, -4.85), (-2.3, 12.25, -4.6), skip="back", paint=PAINT["ridge"]),
              am.span("ridge_back_right", (-3.1, 0.85, 4.6), (-2.3, 12.25, 4.85), skip="front", paint=PAINT["ridge"])]
    return [am.span("tube", (-4.95, -0.65, -4.6), (4.95, 12.75, 4.6), paint=PAINT["tube"]),
            *ridges, *am.mirror_all(ridges)]


# ---------------------------------------------------------------- the leggings and boots
LEFT_OUT = 0.12   # the left leg's parts this much larger, so where the legs meet their faces never share a plane


def leg():
    """The right leg (leg space: x -2..2, y 0..12, z -2..2): the banana round it, 0.7 off its outer side (clear of
    vanilla leggings' 1.0 shell) and 2.25 off its front and back, inside the body's tube where they meet, reaching 0.45
    past the centre line into the other leg's (the two overlap, so no gap opens between them); from up under the
    body's tube to below the knee."""
    return [am.span("leg_right", (-2.7, -0.45, -4.25), (2.45, 8.25, 4.25), paint=PAINT["leg"])]


def boot():
    """The banana's foot round the shin, 0.9 off its outer side and 2.0 off its front and back (inside the leg's),
    overlapping the other foot's at the middle as the legs' do, from up under the leg's to above the ankle, darkening
    at its foot; on the right foot the banana's brown end, sticking out
    forward."""
    return [am.span("foot_right", (-2.9, 7.9, -4.0), (2.65, 10.25, 4.0), paint=PAINT["foot"])]


def end():
    """The banana's brown end, sticking out forward from the right shin (its back sunk into the foot, and drawn where
    it hangs below it), and the nub at its tip."""
    return [am.span("end_right", (-2.4, 8.6, -6.9), (1.0, 11.4, -3.6), paint=PAINT["end"]),
            am.span("nub_right", (-1.6, 9.3, -7.6), (0.2, 10.75, -6.9), skip="back", paint=PAINT["end"])]


def left(parts):
    return [replace(p, inflate=LEFT_OUT) for p in am.mirror_all(parts)]


def model():
    l, b = leg(), boot()
    return {"banana_helmet": {"head": helmet()},
            "banana_chestplate": {"body": body()},
            "banana_leggings": {"right_leg": l, "left_leg": left(l)},
            "banana_boots": {"right_leg": b + end(), "left_leg": left(b)}}


SETS = [am.ArmorSet("banana", BANANA, model())]
