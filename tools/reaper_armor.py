"""Reaper: the owner's hooded reaper (one of the designs they sent on 8 October 2026 with no words: a render of a figure
in a dark hooded robe, its face a black void behind three bars under an arch, a white clasp at its throat, a brown
pouch at its hip, white angular plates on its arms, holding a short white scythe in each hand) as a 3D worn model for
jugcraft:reaper_* (helmet, chestplate, leggings, boots), on the toolkit in tools/armor_models.py (shapes) and
tools/armor_paint.py (the atlas). The name is a placeholder. The two scythes are weapons, not armor ("He is supposed to
be holding 2 short scythe weapons they arent part of the armor", the owner, 8 October 2026): the Reaper Scythe, an Arms
VII set arm (tools/arms_variants.py), one for each hand.

What the owner drew, and where it is here (built part for part, as the owner asked: "complex models ... really
capturing the crazy unique geometry of each armor"):
    helmet      a deep hood rising to a rounded crown, its opening an arch stepping in to its top, the black void within
                barred by three grey bars, the middle one the palest and longest. Here: the hood; its crown in two
                steps; the arch built of seven blocks round the opening, each stepping further back toward the top, so
                the arch reads as cut into the cloth; the three bars standing in the void, 0.35 behind the arch's foot;
                three folds down each side; the fall down the back of the neck and the hood's point behind the crown
    chestplate  the robe; the hood's cloth over the shoulders, ragged; a white V clasp at the throat; a strap stepping
                down across the chest from the right shoulder to the brown pouch at the left hip; on the right arm grey
                plates stepping down the shoulder under a white V, a white band, a white elbow plate and a grey bracer
                rimmed white; on the left arm an open white cage round the upper arm and a white cage round the fist.
                Here: each of those as its own boxes; the strap crosses the back too, and the pouch is a frame of four
                bars round a sunk field with a stud at its heart
    leggings    the robe hanging in strips of different lengths to below the knee, the legs dark beneath. Here: the
                sash at the robe's foot; on each leg the dark under-robe and five strips round it, two before, one at
                the side and two behind, each hinged out a little from the waist, every other one further out
    boots       dark wrapped boots, grey wraps round the shin, a white band at the ankle. Here: the boot, two grey wraps,
                the white band and the toe
Colours: armor_paint.REAPER; the hood in its own four browns.

The render shows the front; the back is drawn in the design's words. Every box is closed: a face is left out only where
another box of the same piece and bone covers it.

Space (tools/armor_models.py): each bone's own space in model pixels; x is the model's LEFT, y is DOWN, z is the BACK
(the face is at -z).
Preview: python3 tools/armor_preview.py --set reaper
"""
from dataclasses import replace

import armor_models as am
from armor_paint import REAPER, P


def rows(*tones):
    """One tone per texel row, cycling, each row broken into runs of the next tone darker."""
    return P("lames", rows=1, tones=tuple((t, t) for t in tones), centre=None)


def solid(tone):
    return P("solid", tone=tone)


def marks(*rects, symmetric=False):
    return P("marks", rects=list(rects), symmetric=symmetric)


def plate(tone, strips="h"):
    return P("plate", tone=tone, strips=strips)


H, L, M, D, S, V = "light", "mid_light", "mid", "dark", "seam", "void"      # the bone-white, white to grey
G, g = "gold_light", "gold_dark"                                           # the pouch's reddish brown
C, c, q, Q, K = "leather_light", "leather_mid_light", "leather_mid", "leather_dark", "leather_darkest"   # the robe
U, u, x, X = "under_light", "under_mid", "under_dark", "under_darkest"      # the void in the hood
W0, W1, W2, W3 = "hood_light", "hood", "hood_mid", "hood_dark"               # the hood's browns

ROBE = plate(q, "v")    # the robe: dark brown, in runs down its length
HOOD = plate(W1, "v")


# ---------------------------------------------------------------- paint
PAINT = {
    "hood": {"top": [HOOD, marks((0, 0, 99, 1, W0))], "bottom": solid(W3),
             "front": [solid(X), marks((0, 0, 99, 3, x))], "*": [HOOD, marks((0, -1, 99, 1, W3))]},
    "crown": {"top": [solid(W1), marks((0, 0, 99, 1, W0), (0, 0, 1, 99, W0))], "*": [solid(W1), marks((0, 0, 99, 1, W0))]},
    # the arch round the opening: lit along its inner edge, as the owner's
    "arch": {"front": [solid(W1), marks((0, 0, 99, 1, W0))], "top": solid(W0), "bottom": solid(W0),
             "right": solid(W0), "left": solid(W0), "*": solid(W2)},
    "jamb": {"front": [HOOD, marks((-1, 0, 1, 99, W0), (0, 0, 1, 99, W2))], "top": solid(W0), "bottom": solid(W3),
             "left": solid(W2), "*": solid(W2)},
    "bar": {"front": [solid(S), marks((0, -4, 99, 4, D), (0, -2, 99, 2, M))], "top": solid(D), "bottom": solid(M),
            "*": solid(V)},
    "bar_mid": {"front": [solid(D), marks((0, -5, 99, 5, M), (0, -2, 99, 2, L))], "top": solid(M), "bottom": solid(L),
                "*": solid(S)},
    "fold": {"*": [solid(W2), marks((0, 0, 1, 99, W1), (0, -1, 99, 1, W3))]},
    "fall": {"back": [HOOD, marks((0, -1, 99, 1, W3), (2, -2, 1, 1, W3), (5, -2, 1, 1, W3))], "bottom": solid(W3),
             "*": solid(W2)},
    "point": {"top": solid(W0), "bottom": solid(W3), "*": [solid(W1), marks((0, 0, 1, 99, W0), (0, -1, 99, 1, W2))]},
    "robe": {"top": [ROBE, marks((0, 0, 99, 1, C))], "bottom": solid(K), "*": [ROBE, marks((0, -1, 99, 1, Q))]},
    "robe_fold": {"front": [solid(Q), marks((0, 0, 1, 99, q))], "back": [solid(Q), marks((0, 0, 1, 99, q))],
                  "*": solid(Q)},
    "capelet": {"top": [HOOD, marks((0, 0, 99, 1, W0))], "bottom": solid(W3),
                "*": [HOOD, marks((0, -1, 99, 1, W3), (1, -2, 1, 1, W3), (4, -2, 1, 1, W3), (7, -2, 1, 1, W3))]},
    "tatter": {"top": solid(W2), "bottom": solid(W3), "*": [solid(W2), marks((0, -1, 99, 1, W3))]},
    "clasp": {"front": [solid(H), marks((0, -1, 99, 1, M))], "top": solid(H), "bottom": solid(D), "*": solid(L)},
    # the strap: the lighter brown band stepping across the owner's robe
    "strap": {"front": [solid(W0), marks((0, -1, 99, 1, W2))], "back": [solid(W0), marks((0, -1, 99, 1, W2))],
              "top": solid(W0), "*": solid(W1)},
    "buckle": {"front": [solid(M), marks((0, 0, 99, 1, L))], "top": solid(L), "*": solid(D)},
    "pouch": {"front": [solid(K), marks((1, 1, 99, 99, g))], "top": solid(g), "bottom": solid(K), "*": solid(g)},
    "pouch_frame": {"front": [solid(G), marks((0, -1, 99, 1, g))], "top": solid(G), "*": solid(g)},
    "stud": {"front": solid(G), "*": solid(g)},
    "sleeve": {"top": solid(C), "bottom": solid(K), "*": ROBE},
    "lame": {"top": [solid(D), marks((0, 0, 99, 1, M))], "bottom": solid(V),
             "*": [solid(S), marks((0, 0, 99, 1, D), (0, -1, 99, 1, V))]},
    "white": {"top": [solid(H), marks((0, -1, 99, 1, L))], "bottom": solid(D),
              "*": [solid(L), marks((0, 0, 99, 1, H), (0, -1, 99, 1, M))]},
    "white_bar": {"top": solid(H), "bottom": solid(M), "*": [solid(L), marks((0, 0, 1, 99, H), (-1, 0, 1, 99, M))]},
    "bracer": {"top": solid(D), "bottom": solid(V), "*": [solid(S), marks((0, 0, 99, 1, D), (2, 1, 1, 1, D),
                                                                          (5, 1, 1, 1, D))]},
    "glove": {"top": solid(u), "bottom": solid(X), "*": rows(u, x)},
    # the legs and feet
    "sash": {"top": solid(Q), "bottom": solid(K), "sides": [solid(Q), marks((0, 0, 99, 1, q))]},
    "under": {"top": solid(Q), "bottom": solid(K), "*": rows(Q, K, Q, Q)},
    "strip": {"top": solid(q), "bottom": solid(K),
              "*": [ROBE, marks((0, 0, 1, 99, c), (0, -1, 99, 1, K), (0, -2, 99, 1, Q))]},
    "strip_dark": {"top": solid(Q), "bottom": solid(K),
                   "*": [plate(Q, "v"), marks((0, 0, 1, 99, q), (0, -1, 99, 1, K))]},
    "boot": {"top": solid(u), "bottom": solid(X), "*": rows(U, u, u)},
    "wrap": {"top": solid(D), "bottom": solid(V), "*": [solid(S), marks((0, 0, 99, 1, D), (1, 1, 1, 1, V),
                                                                        (4, 1, 1, 1, V), (7, 1, 1, 1, V))]},
    "ankle": {"top": solid(H), "bottom": solid(D), "*": [solid(L), marks((0, 0, 99, 1, H), (0, -1, 99, 1, M))]},
    "toe": {"top": solid(U), "bottom": solid(X), "*": solid(u)},
}


# ---------------------------------------------------------------- the helmet
# The opening's arch, as columns from the jamb in to the middle: (inner edge x on the model's right, how far up the
# opening reaches there, how far the column's face stands before the head). Each column steps 0.15 further back than
# the one outside it, so the arch is cut into the hood.
ARCH = ((-3.0, 1.0, -5.45), (-2.0, -5.0, -5.3), (-1.0, -6.0, -5.15), (0.0, -6.75, -5.0))
ARCH_TOP = -9.4   # every column's top
ARCH_BACK = -4.6  # every column's back, inside the hood


def arch():
    """The arch round the opening: the jambs from the crown to below the chin, then a column either side stepping down
    to the opening's top, and the keystone in the middle; a column's side against the one outside it is left out (their
    backs are drawn: they stand above the hood, and the jambs past its sides)."""
    out = []
    (x3, y3, z3), (x2, y2, z2), (x1, y1, z1), (_, y0, z0) = ARCH
    jamb = am.span("jamb_right", (-5.15, ARCH_TOP, z3), (x3, y3, ARCH_BACK), paint=PAINT["jamb"])
    col_2 = am.span("arch_2_right", (x3, ARCH_TOP, z2), (x2, y2, ARCH_BACK), skip="right", paint=PAINT["arch"])
    col_1 = am.span("arch_1_right", (x2, ARCH_TOP, z1), (x1, y1, ARCH_BACK), skip="right", paint=PAINT["arch"])
    for part in (jamb, col_2, col_1):
        out += am.pair(part)
    out.append(am.span("keystone", (-1.0, ARCH_TOP, z0), (1.0, y0, ARCH_BACK), skip=("right", "left"),
                       paint=PAINT["arch"]))
    return out


def bars():
    """The three bars in the void, 0.35 behind the arch's foot: the middle one from the keystone to the chin, the
    palest; the other two from under the arch's inner columns."""
    side = am.span("bar_right", (-2.0, ARCH[2][1], -5.1), (-1.25, -0.05, ARCH_BACK), skip=("back", "top"),
                   paint=PAINT["bar"])
    return [am.span("bar_middle", (-0.3, ARCH[3][1], -5.1), (0.3, -0.05, ARCH_BACK), skip="back", paint=PAINT["bar_mid"]),
            *am.pair(side)]


def helmet():
    """The hood, 0.75 clear of the head, closed underneath 0.65 below it, its front the black void; its crown in two
    steps, 1.6 and 2.15 above the head; the arch and the bars; three folds down each side, 0.45 proud; the fall down the
    back of the neck; and the hood's point behind the crown, in two pieces, falling back and down."""
    folds = [am.hinge(am.span(f"fold_{i}_right", (-5.25, y0, z0), (-4.75, y1, z0 + 0.75), skip="left",
                              paint=PAINT["fold"]), "top", tilt, toward="back" if tilt > 0 else "front", axis="x")
             for i, (y0, y1, z0, tilt) in enumerate(((-8.2, -0.6, -2.4, 4), (-7.0, -0.1, 0.3, -4), (-8.6, -2.2, 2.9, 5)))]
    return [am.span("hood", (-4.75, -9.0, -4.75), (4.75, 0.65, 4.95), paint=PAINT["hood"]),
            am.span("crown_0", (-3.9, -9.6, -3.9), (3.9, -9.0, 4.6), skip="bottom", paint=PAINT["crown"]),
            am.span("crown_1", (-2.4, -10.15, -2.85), (2.4, -9.6, 3.65), skip="bottom", paint=PAINT["crown"]),
            *arch(), *bars(), *folds, *am.mirror_all(folds),
            am.span("fall", (-4.25, -6.0, 4.8), (4.25, 2.0, 5.45), paint=PAINT["fall"]),
            am.box("point_0", (-1.25, -9.4, 3.9), (2.5, 2.2, 2.4), pivot=(0.0, -9.4, 3.9), rotation=(-20, 0, 0),
                   paint=PAINT["point"]),
            am.box("point_1", (-0.7, -8.7, 5.6), (1.4, 3.0, 1.3), pivot=(0.0, -8.7, 5.6), rotation=(-12, 0, 0),
                   paint=PAINT["point"])]


# ---------------------------------------------------------------- the chestplate
def strap(z0, z1, name):
    """The strap's steps from the right shoulder down to the left hip, on one face of the robe (z0..z1 is the robe's
    face and the step's outer face); every other step 0.15 further out, so where they overlap their faces never meet."""
    out = []
    for i in range(6):
        x0, y0 = -4.4 + 1.3 * i, 1.7 + 1.3 * i
        lift = 0.15 if i % 2 else 0.0
        out.append(am.span(f"{name}_{i}", (x0, y0, z0), (x0 + 2.3, y0 + 1.45, z1 + (lift if z1 > z0 else -lift)),
                           skip="back" if z1 < z0 else "front", paint=PAINT["strap"]))
    return out


def pouch():
    """The pouch at the left hip, where the strap ends: its box, the frame of four bars round its sunk front, the stud
    at its heart, and the strap's buckle above it."""
    x0, x1, y0, y1, front = 1.25, 4.65, 8.8, 11.3, -3.65
    bar = 0.55
    return [am.span("pouch", (x0, y0, front), (x1, y1, -2.85), skip="back", paint=PAINT["pouch"]),
            am.span("pouch_top", (x0 - 0.15, y0 - 0.15, front - 0.3), (x1 + 0.15, y0 + bar, front), skip="back",
                    paint=PAINT["pouch_frame"]),
            am.span("pouch_bottom", (x0 - 0.15, y1 - bar, front - 0.3), (x1 + 0.15, y1 + 0.15, front), skip="back",
                    paint=PAINT["pouch_frame"]),
            am.span("pouch_right", (x0 - 0.15, y0 + bar, front - 0.45), (x0 + bar, y1 - bar, front), skip="back",
                    paint=PAINT["pouch_frame"]),
            am.span("pouch_left", (x1 - bar, y0 + bar, front - 0.45), (x1 + 0.15, y1 - bar, front), skip="back",
                    paint=PAINT["pouch_frame"]),
            am.span("stud", (2.55, 9.65, front - 0.25), (3.35, 10.45, front), skip="back", paint=PAINT["stud"]),
            am.span("buckle", (2.15, 8.0, -4.2), (3.15, 8.9, -3.45), skip="back", paint=PAINT["buckle"])]


def body():
    """The robe, 0.95 off the body's sides (0.2 outside the hood's, so the two never share a plane as the head turns)
    and 0.85 off its front and back, to just above the hips; over its shoulders the hood's cloth, 1.45 off the sides and
    1.55 off the front and back, ragged along its foot with tatters hanging before and behind; three folds down the
    robe's front and two down its back; the clasp, two bars meeting at the breastbone (the left one 0.15 further back
    where they cross); the strap across the front and the back; the pouch."""
    clasp = am.chevron("clasp", (0.0, 4.6, -3.95), 2.9, 0.9, 0.4, angle=46, skip="back", paint=PAINT["clasp"])
    clasp_left = replace(clasp[1], origin=(clasp[1].origin[0], clasp[1].origin[1], clasp[1].origin[2] + 0.15))
    front = am.span("tatter_front_right", (-5.4, 4.0, -3.5), (-3.8, 6.1, -3.2), paint=PAINT["tatter"])
    tatters = [*am.pair(front),
               am.span("tatter_back_right", (-5.4, 4.0, 3.2), (-3.9, 6.0, 3.5), paint=PAINT["tatter"]),
               am.span("tatter_back_left", (2.0, 4.0, 3.2), (3.4, 6.6, 3.5), paint=PAINT["tatter"])]
    folds = [am.span(f"robe_fold_{i}", (x0, 5.0, -3.1), (x0 + w, 11.5, -2.85), skip="back", paint=PAINT["robe_fold"])
             for i, (x0, w) in enumerate(((-3.6, 0.8), (-0.85, 1.7)))]
    back_folds = [am.span(f"back_fold_{i}", (x0, 4.4, 2.85), (x0 + w, 11.5, 3.1), skip="front",
                          paint=PAINT["robe_fold"]) for i, (x0, w) in enumerate(((-3.45, 0.9), (2.35, 0.9)))]
    return [am.span("robe", (-4.95, -0.65, -2.85), (4.95, 11.5, 2.85), paint=PAINT["robe"]),
            am.span("capelet", (-5.45, -0.9, -3.55), (5.45, 4.0, 3.55), paint=PAINT["capelet"]),
            *tatters, *folds, *back_folds,
            clasp[0], clasp_left,
            *strap(-2.85, -3.3, "strap"), *strap(2.85, 3.3, "strap_back"),
            *pouch()]


def right_arm():
    """The right arm (arm space: x -3..1, y -2..10, z -2..2, the outer side -x): the sleeve 0.75 off, from 0.4 above the
    shoulder to the wrist; three grey plates stepping down the shoulder, each wider and further out; the white V on
    their outer side, its two bars meeting at the bottom (the back one 0.15 further out); a white band round the upper
    arm; the white elbow plate, a band and a plate down the outer forearm, hinged out; the grey bracer, rimmed white at
    its foot; the dark glove, closed 0.6 below the hand."""
    lames = [am.span("lame_0_right", (-4.35, -3.15, -3.15), (1.35, -1.65, 3.15), paint=PAINT["lame"]),
             am.span("lame_1_right", (-4.55, -1.85, -3.35), (1.5, -0.55, 3.35), paint=PAINT["lame"]),
             am.span("lame_2_right", (-4.75, -0.75, -3.75), (1.95, 0.45, 3.75), paint=PAINT["lame"])]
    vee = [am.box("vee_front_right", (-5.2, -3.4, -0.4), (0.45, 3.6, 0.8), pivot=(-5.2, 0.2, 0.0), rotation=(34, 0, 0),
                  paint=PAINT["white_bar"]),
           am.box("vee_back_right", (-5.35, -3.4, -0.4), (0.45, 3.6, 0.8), pivot=(-5.35, 0.2, 0.0),
                  rotation=(-34, 0, 0), paint=PAINT["white_bar"])]
    return [am.span("sleeve_right", (-3.75, -2.4, -2.75), (1.75, 9.25, 2.75), paint=PAINT["sleeve"]),
            *lames, *vee,
            am.span("band_right", (-4.15, 1.2, -3.15), (2.15, 2.1, 3.15), paint=PAINT["white"]),
            am.span("elbow_right", (-4.3, 3.9, -3.3), (2.3, 4.9, 3.3), paint=PAINT["white"]),
            am.hinge(am.span("elbow_plate_right", (-4.75, 4.9, -1.6), (-4.3, 7.0, 1.6), paint=PAINT["white"]), "top", 10),
            am.span("bracer_right", (-3.95, 5.4, -2.95), (1.95, 8.4, 2.95), paint=PAINT["bracer"]),
            am.span("cuff_right", (-4.25, 7.5, -3.25), (2.25, 8.2, 3.25), paint=PAINT["white"]),
            am.span("glove_right", (-3.5, 9.25, -2.5), (1.5, 10.6, 2.5), skip="top", paint=PAINT["glove"])]


def left_arm():
    """The left arm, built as the right and mirrored (the outer side -x here): the sleeve; the white cap over the
    shoulder; the open cage round the upper arm, a white band at its top and at its foot and a white post at each of
    its four corners, the sleeve showing between; a grey band at the elbow; the cage round the fist, a band at the wrist
    and one round the knuckles, posts at the outer corners between them; the dark glove inside it."""
    posts = [am.span(f"cage_post_{i}_right", (-4.6, -0.6, z0), (-3.8, 1.35, z0 + 0.8), paint=PAINT["white_bar"])
             for i, z0 in enumerate((-3.95, 3.15))]
    fist_posts = [am.span(f"fist_post_{i}_right", (-4.45, 8.1, z0), (-3.65, 9.4, z0 + 0.8), paint=PAINT["white_bar"])
                  for i, z0 in enumerate((-3.45, 2.65))]
    out = [am.span("sleeve_right", (-3.75, -2.4, -2.75), (1.75, 9.25, 2.75), paint=PAINT["sleeve"]),
           am.span("cap_right", (-4.4, -3.15, -3.3), (2.0, -1.6, 3.3), paint=PAINT["white"]),
           am.span("cage_top_right", (-4.75, -1.6, -3.75), (2.35, -0.6, 3.75), paint=PAINT["white"]),
           *posts,
           am.span("cage_foot_right", (-4.75, 1.35, -3.75), (2.35, 2.35, 3.75), paint=PAINT["white"]),
           am.span("elbow_band_right", (-4.3, 4.4, -3.3), (1.4, 5.3, 3.3), paint=PAINT["lame"]),
           am.span("wrist_right", (-4.3, 7.2, -3.3), (2.3, 8.1, 3.3), paint=PAINT["white"]),
           *fist_posts,
           am.span("knuckles_right", (-4.6, 9.4, -3.6), (2.6, 10.45, 3.6), paint=PAINT["white"]),
           am.span("glove_right", (-3.5, 9.25, -2.5), (1.5, 10.6, 2.5), skip="top", paint=PAINT["glove"])]
    return am.mirror_all(out)


# ---------------------------------------------------------------- the leggings and boots
LEFT_OUT = 0.12   # the left leg's parts this much larger, so where the legs meet their faces never share a plane


def waist():
    """The sash at the robe's foot, outside the robe and its folds and clear of vanilla leggings' 1.0 shell: 1.45 off
    the body's front and back and 1.3 off its sides; the strips hang from it."""
    return [am.span("sash", (-5.3, 10.25, -3.45), (5.3, 12.65, 3.45), paint=PAINT["sash"])]


def leg():
    """The right leg (leg space: x -2..2, y 0..12, z -2..2): the dark under-robe, 1.15 off (clear of vanilla leggings'
    1.0 shell), from up under the sash to below the knee, reaching past the centre line into the other leg's so no gap
    opens between them; then the robe's five strips over it, 0.15 off it, from up under the sash, each a different
    length and hinged out a little from the waist: two before it (the inner one stopping short of the centre line),
    one at its outer side and two behind, every other one 0.15 further out."""
    strips = [am.hinge(am.span("strip_0_right", (-2.5, -0.4, -3.6), (-0.65, 7.9, -3.3), paint=PAINT["strip"]),
                       "top", 2),
              am.hinge(am.span("strip_1_right", (-0.5, -0.4, -3.75), (1.75, 6.9, -3.45), paint=PAINT["strip_dark"]),
                       "top", 1.5),
              am.hinge(am.span("strip_2_right", (-3.6, -0.4, -2.0), (-3.3, 7.4, 1.6), paint=PAINT["strip_dark"]),
                       "top", 2),
              am.hinge(am.span("strip_3_right", (-2.6, -0.4, 3.3), (-0.4, 7.2, 3.6), paint=PAINT["strip"]),
                       "top", 2),
              am.hinge(am.span("strip_4_right", (-0.25, -0.4, 3.45), (1.75, 8.1, 3.75), paint=PAINT["strip_dark"]),
                       "top", 1.5)]
    return [am.span("under_right", (-3.15, -0.45, -3.15), (2.45, 8.6, 3.15), paint=PAINT["under"]), *strips]


def boot():
    """The boot, 0.65 off the leg (clear of vanilla boots' 0.5 shell), from up under the under-robe and closed 0.65
    below the foot; two grey wraps round the shin and the white band round the ankle; the toe."""
    return [am.span("boot_right", (-2.85, 8.5, -2.85), (2.85, 12.65, 2.85), paint=PAINT["boot"]),
            am.span("wrap_0_right", (-3.15, 8.9, -3.15), (3.15, 9.6, 3.15), paint=PAINT["wrap"]),
            am.span("wrap_1_right", (-3.15, 9.9, -3.15), (3.15, 10.45, 3.15), paint=PAINT["wrap"]),
            am.span("ankle_right", (-3.15, 10.75, -3.15), (3.15, 11.45, 3.15), paint=PAINT["ankle"]),
            am.span("toe_right", (-2.1, 11.6, -3.6), (2.1, 12.35, -2.85), skip="back", paint=PAINT["toe"])]


def left(parts):
    return [replace(p, inflate=LEFT_OUT) for p in am.mirror_all(parts)]


def model():
    l, b = leg(), boot()
    return {"reaper_helmet": {"head": helmet()},
            "reaper_chestplate": {"body": body(), "right_arm": right_arm(), "left_arm": left_arm()},
            "reaper_leggings": {"body": waist(), "right_leg": l, "left_leg": left(l)},
            "reaper_boots": {"right_leg": b, "left_leg": left(b)}}


SETS = [am.ArmorSet("reaper", REAPER, model())]
