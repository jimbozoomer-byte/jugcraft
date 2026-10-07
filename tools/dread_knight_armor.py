"""Dread Knight: the owner's dark crowned knight (the first of the four designs they sent on 7 October 2026, a sheet of
the four on a blank mannequin and a screenshot of each worn in game) as 3D worn models for jugcraft:dread_knight_helmet,
_chestplate, _leggings and _boots, on the toolkit in tools/armor_models.py (shapes) and tools/armor_paint.py (the atlas).

What the owner drew, and where it is here:
    helmet      a great helm a little bigger than the head, near-black, under a crown of light grey merlons: a broad one
                over the brow, smaller ones at the corners and the sides, notched between; a light grey brow band, the
                eyes two black slits either side of a nasal bar, a light frame down beside each eye and under it, the
                mouth and chin dark; a dark window framed light on each side. Here: the helm (closed underneath, 0.65
                below the head); the crown band round its top and the merlons on it, the front and back ones the tallest;
                the brow band, the nasal bar, the posts and the cheek plates standing 0.5 proud of the helm's front, its
                slits painted between them; the windows painted on the sides
    chestplate  a dark cuirass under a mottled grey muscle plate (the chest and the ridges of the stomach, a dark line
                down the middle); big blocky pauldrons in light and dark bands with two small spikes standing on each;
                the arms banded light and dark to the wrist, a black band at the elbow. Here: the cuirass, the muscle
                plate on its front and a banded plate on its back; on each arm the pauldron, a flared lame under it, its
                two spikes leaning out, then the upper arm plate, the black elbow band, the vambrace and a flared cuff
    leggings    a black belt over a black skirt of upright strips riveted grey along its foot, the legs black to the
                knee. Here: the belt with a grey buckle, and on each leg a dark cuisse to the ankle and the strip skirt
                over it to mid-thigh, riveted along its foot
    boots       banded greaves: a light grey cuff at the knee, then dark, a light band, dark, and light grey sabatons.
                Here: the cuff, the greave and the sabaton, longer at the toe
Colours: armor_paint.DREAD_KNIGHT: steel greys from the crown's light grey to near-black, the owner's faint pink sheen
on the lit greys, and a near-black under-layer whose darkest is the eye slits.

The sheet shows the set from the front, a little above and to the left; the screenshot from the front. The back is
drawn in the design's own words: the crown runs all round; the cuirass's back carries bands; the pauldrons, arms, skirt
and boots are banded all round. Every box is closed: a face is left out only where another box of the same piece and
bone covers it (the helm's top inside the crown band, the merlons' feet on it, the face plates' backs on the helm, each
arm plate's end inside the next), so the set shows no holes alone, on an armor stand's thin limbs (tools/art_check.py,
H1). A box is its own plate; the bands, slits, strips and rivets are paint, one texel per model pixel.

Space (tools/armor_models.py): each bone's own space in model pixels; x is the model's LEFT, y is DOWN, z is the BACK
(the face is at -z). Right-hand parts are built and mirrored to the left.
Preview: python3 tools/armor_preview.py --set dread_knight
"""
from dataclasses import replace

import armor_models as am
from armor_paint import DREAD_KNIGHT, P


def rows(*tones):
    """One tone per texel row, cycling, each row broken into runs of the next tone darker."""
    return P("lames", rows=1, tones=tuple((t, t) for t in tones), centre=None)


def solid(tone):
    return P("solid", tone=tone)


def marks(*rects):
    return P("marks", rects=list(rects))


def strips(*tones, width=6):
    """Upright strips, one tone per texel column, cycling: the skirt's strips."""
    return marks(*[(i, 0, 1, 99, tones[i % len(tones)]) for i in range(width)])


# The palette's names (armor_paint.DREAD_KNIGHT).
H, L, M, D, S, V = "light", "mid_light", "mid", "dark", "seam", "void"       # steel, light grey to near-black
PK, pk = "gold_light", "gold_dark"                                          # the pink sheen on the lit greys
U, u, x, X = "under_light", "under_mid", "under_dark", "under_darkest"       # the near-black under-layer


# ---------------------------------------------------------------- paint
def paints():
    """Role -> paint spec, the owner's patterns read off the sheet and the screenshot: light grey frames, bands and
    merlons over near-black plate; the muscle plate mottled in mid greys; bands on the arms and boots."""
    return {
        # the helm: near-black under the crown's light rim, its top row on every side (the merlons rise from it); on the
        # front the eye slits (black) between the brow, the nasal bar and the posts, the mouth a shade lighter and the
        # chin; on each side a window framed light grey; banded behind; seen from above a light rim round a dark top
        "helm": {"front": [solid(S), marks((0, 0, 99, 1, H), (0, 1, 99, 1, V), (1, 3, 3, 2, X), (6, 3, 3, 2, X),
                                           (4, 7, 2, 2, V), (0, -1, 99, 1, V))],
                 "right": [solid(S), marks((0, 0, 99, 1, H), (0, 1, 99, 1, V), (3, 3, 4, 1, L), (3, 4, 1, 2, L),
                                           (6, 4, 1, 2, L), (4, 4, 2, 2, X), (3, 6, 4, 1, M), (0, -1, 99, 1, V))],
                 "left": [solid(S), marks((0, 0, 99, 1, H), (0, 1, 99, 1, V), (3, 3, 4, 1, L), (3, 4, 1, 2, L),
                                          (6, 4, 1, 2, L), (4, 4, 2, 2, X), (3, 6, 4, 1, M), (0, -1, 99, 1, V))],
                 "back": [rows(S, V, S, D, S, V, S, D, S, V), marks((0, 0, 99, 1, H))],
                 "top": [solid(V), marks((0, 0, 99, 1, H), (0, -1, 99, 1, L), (0, 0, 1, 99, H), (-1, 0, 1, 99, L),
                                         (3, 3, 4, 4, S), (4, 4, 2, 2, D))],
                 "bottom": solid(V)},
        # the merlons: light grey, the pink sheen on their lit tops and fronts' upper texels
        "merlon": {"top": [solid(H), marks((0, 0, 1, 1, PK))], "front": [solid(L), marks((0, 0, 99, 1, H), (1, 0, 1, 1, PK))],
                   "*": [solid(L), marks((0, 0, 99, 1, H))]},
        "merlon_tall": {"top": [solid(H), marks((1, 0, 2, 1, PK))],
                        "front": [solid(L), marks((0, 0, 99, 1, H), (1, 0, 2, 1, PK), (0, -1, 99, 1, M))],
                        "*": [solid(L), marks((0, 0, 99, 1, H), (0, -1, 99, 1, M))]},
        # the face's frames: the brow band and the nasal bar light grey with the sheen, the posts and cheeks light grey
        # shading down to mid grey
        "brow": {"front": [solid(H), marks((4, 0, 2, 1, PK), (0, -1, 99, 1, L))], "bottom": solid(D), "*": solid(L)},
        "nasal": {"front": [solid(L), marks((0, 0, 2, 1, PK), (0, 1, 2, 1, pk), (0, -1, 99, 1, M))], "*": solid(M)},
        "post": {"front": [solid(H), marks((0, -1, 99, 1, L))], "*": solid(L)},
        "cheek": {"front": [solid(L), marks((0, 0, 99, 1, H), (-1, 2, 1, 99, M), (0, -1, 99, 1, M), (-1, 1, 1, 1, PK))],
                  "*": solid(M)},
        # the cuirass: near-black, a light line along the collar
        "cuirass": {"front": [rows(S, V, S, V), marks((0, 0, 99, 1, D))], "top": [solid(S), marks((0, 0, 99, 1, D))],
                    "back": [rows(S, V), marks((0, 0, 99, 1, D))], "*": rows(S, V, S, V)},
        # the muscle plate: the owner's mottled greys, a light collar line, the chest's two plates, the stomach's ridges
        # between dark lines, a dark line down the middle
        "breast": {"front": [solid(M), marks((0, 0, 99, 1, H), (1, 1, 2, 2, L), (4, 1, 2, 2, L), (0, 3, 99, 1, D),
                                             (1, 4, 2, 1, L), (4, 4, 2, 1, L), (0, 5, 99, 1, S), (1, 6, 2, 1, M),
                                             (4, 6, 2, 1, L), (0, 7, 99, 1, D), (3, 1, 1, 99, D), (0, -1, 99, 1, S),
                                             (1, 2, 1, 1, H), (5, 1, 1, 1, H))],
                   "*": solid(D)},
        "back_plate": {"back": [rows(D, M, D, S), marks((0, 0, 99, 1, L))], "*": solid(D)},
        # the pauldrons: light and dark bands, lit at the top; seen from above near-black with a light rim
        "pauldron": {"top": [solid(S), marks((0, 0, 99, 1, L), (0, -1, 99, 1, M), (0, 0, 1, 99, L))],
                     "bottom": solid(V), "*": rows(S, H, L, D, V, L, M, D)},
        "lame": {"top": solid(D), "bottom": solid(V), "*": rows(H, L, S)},
        "spike": {"top": solid(PK), "bottom": solid(M), "*": [solid(L), marks((0, 0, 99, 1, H))]},
        # the arm: the upper arm plate banded, the black elbow band, the vambrace banded, the light cuff
        "rerebrace": {"*": rows(H, L, D, M)},
        "couter": {"top": solid(S), "bottom": solid(V), "sides": rows(x, X)},
        "vambrace": {"*": rows(M, H, L, D)},
        "cuff": {"top": solid(H), "bottom": solid(D), "*": rows(H, L)},
        # the belt: black leather, its grey buckle; the cuisse near-black; the skirt's strips, riveted grey at the foot
        "belt": {"top": solid(u), "bottom": solid(X), "*": [rows(u, x), marks((0, 0, 99, 1, U))]},
        "buckle": {"front": [solid(M), marks((0, 0, 99, 1, H), (1, 1, 1, 1, X))], "*": solid(D)},
        "cuisse": {"top": solid(x), "bottom": solid(X), "*": [solid(x), marks((0, 0, 99, 1, u))]},
        "skirt": {"front": [strips(u, x, x, X), marks((0, 0, 99, 1, U), (1, -2, 1, 1, M), (4, -2, 1, 1, M),
                                                        (0, -1, 99, 1, X))],
                  "back": [strips(u, x, x, X), marks((0, 0, 99, 1, U), (1, -2, 1, 1, M), (4, -2, 1, 1, M),
                                                       (0, -1, 99, 1, X))],
                  "top": solid(u), "bottom": solid(X),
                  "*": [strips(u, x, X), marks((0, 0, 99, 1, U), (2, -2, 1, 1, M), (0, -1, 99, 1, X))]},
        # boots: the light cuff at the knee, the greave banded dark, light and dark, the sabaton light over a dark sole
        "boot_cuff": {"top": solid(H), "bottom": solid(D), "sides": [rows(H, L), marks((0, -1, 99, 1, M))]},
        "greave": {"front": [rows(V, S, H, L, S, V), marks((2, 2, 1, 2, S))], "*": rows(V, S, H, L, S, V)},
        "sabaton": {"top": solid(L), "bottom": solid(X), "front": [solid(H), marks((0, -1, 99, 1, V), (2, 0, 2, 1, PK))],
                    "*": [rows(H, L, V), marks((0, -1, 99, 1, X))]},
    }


# ---------------------------------------------------------------- helmet
def helmet(r):
    """The helm, 0.75 clear of the head's sides, 1.0 above it (0.5 off the hat layer) and closed 0.65 below it, so a
    turned head never shows the wearer from below. Its top edge is the crown, as drawn: the merlons stand on it flush
    with its sides, a broad one over the brow and one behind, smaller ones at the four corners and the middle of each
    side, the light rim between them notched dark. On its front, 0.5 proud: the brow band, the nasal bar down from it, a
    post beside each eye and a cheek plate under it from the outer edge toward the nasal bar; the eye slits, the mouth
    and the chin are paint between them."""
    parts = [
        am.span("helm", (-4.75, -9.0, -4.75), (4.75, 0.65, 4.75), paint=r["helm"]),
        am.span("brow", (-4.75, -7.1, -5.25), (4.75, -5.9, -4.75), skip="back", paint=r["brow"]),
        am.span("nasal", (-0.75, -5.9, -5.25), (0.75, -2.65, -4.75), skip=("back", "top"), paint=r["nasal"]),
        # the merlons, standing on the helm's top: the tall ones over the brow and behind, the rest at the corners and
        # the middle of each side
        am.span("merlon_front", (-1.75, -11.0, -4.75), (1.75, -9.0, -3.5), skip="bottom", paint=r["merlon_tall"]),
        am.span("merlon_back", (-1.75, -11.0, 3.5), (1.75, -9.0, 4.75), skip="bottom", paint=r["merlon_tall"]),
    ]
    post = am.span("post_right", (-4.75, -5.9, -5.25), (-3.9, -3.9, -4.75), skip=("back", "top"), paint=r["post"])
    cheek = am.span("cheek_right", (-4.75, -3.9, -5.25), (-1.5, -0.75, -4.75), skip="back", paint=r["cheek"])
    corner = am.span("merlon_corner_right", (-4.75, -10.25, -4.75), (-3.0, -9.0, -3.0), skip="bottom",
                     paint=r["merlon"])
    corner_back = am.span("merlon_corner_back_right", (-4.75, -10.25, 3.0), (-3.0, -9.0, 4.75), skip="bottom",
                          paint=r["merlon"])
    # the side merlon sits 0.15 forward of the middle, so a head looking straight up or down never lays its front or
    # back in the plane of a pauldron's top
    side = am.span("merlon_side_right", (-4.75, -10.25, -1.4), (-3.5, -9.0, 1.1), skip="bottom", paint=r["merlon"])
    for part in (post, cheek, corner, corner_back, side):
        parts += am.pair(part)
    return parts


# ---------------------------------------------------------------- chestplate
def body(r):
    """The cuirass, 1.0 off the body's sides (0.25 outside the helm's) and 0.9 off its front and back, from 0.65 above
    the shoulders to above the belt; the muscle plate 0.5 proud of its front and a banded plate on its back."""
    return [
        am.span("cuirass", (-5.0, -0.65, -2.9), (5.0, 10.5, 2.9), paint=r["cuirass"]),
        am.span("breast", (-3.5, 0.75, -3.4), (3.5, 9.75, -2.9), skip="back", paint=r["breast"]),
        am.span("back_plate", (-3.5, 0.75, 2.9), (3.5, 9.75, 3.4), skip="front", paint=r["back_plate"]),
    ]


def spike(name, base, height, out, back, paint):
    """A small spike standing on `base` (its foot's centre), 1 px square, leaning `out` degrees toward the model's right
    and `back` degrees backward."""
    bx, by, bz = base
    return am.box(name, (bx - 0.5, by - height, bz - 0.5), (1.0, height, 1.0), pivot=base, rotation=(-back, 0, -out),
                  skip="bottom", paint=paint)


def arm(r):
    """The right arm (arm space: x -3..1, y -2..10, z -2..2): the pauldron, 1.75 past the arm's outer side and 1.25
    above it, its two spikes on top leaning out; a lame under it flared 8 degrees; then, round the arm and each 0.6 to
    1.3 off it, the upper arm plate, the black elbow band, the vambrace and the flared cuff, each a little wider than the
    one above, the end of each inside the next. Their fronts and backs keep 0.1 or more off the cuirass's and the
    muscle plate's planes, which an idle arm keeps."""
    pauldron = am.span("pauldron_right", (-4.75, -3.25, -3.25), (1.5, 2.25, 3.25), paint=r["pauldron"])
    lame = am.hinge(am.span("lame_right", (-5.0, 1.75, -3.55), (2.0, 3.75, 3.55), paint=r["lame"]), "top", 8,
                    toward="right", axis="z")
    return [
        pauldron, lame,
        spike("spike_right", (-3.0, -3.25, -0.75), 2.5, 14, 6, r["spike"]),
        spike("spike_inner_right", (-0.6, -3.25, -0.75), 2.0, 8, 6, r["spike"]),
        am.span("rerebrace_right", (-3.6, 2.0, -2.6), (1.6, 4.75, 2.6), skip=("top", "bottom"), paint=r["rerebrace"]),
        am.span("couter_right", (-3.75, 4.75, -2.75), (1.75, 5.75, 2.75), skip="bottom", paint=r["couter"]),
        am.span("vambrace_right", (-3.85, 5.75, -3.05), (1.85, 9.25, 3.05), skip="bottom", paint=r["vambrace"]),
        am.span("cuff_right", (-4.1, 9.25, -3.3), (2.1, 10.45, 3.3), paint=r["cuff"]),
    ]


# ---------------------------------------------------------------- leggings
LEFT_OUT = 0.12   # the left leg's parts stand this much further out, so the two legs' never share a plane where they
                  # overlap at the centre line


def waist(r):
    """The black belt, 0.6 off the body's front and back and 0.45 off its sides (clear of vanilla armor's shells and of
    the skirt's planes), closed underneath 0.65 below the body, so a sneaking body's tipped underside shows the belt,
    not the wearer; the grey buckle on its front, between the cuirass and the skirt."""
    return [am.span("belt", (-4.45, 10.25, -2.6), (4.45, 12.65, 2.6), paint=r["belt"]),
            am.span("buckle", (-1.25, 10.55, -2.85), (1.25, 11.35, -2.6), skip="back", paint=r["buckle"])]


def leg(r):
    """The right leg (leg space: x -2..2, y 0..12, z -2..2): the cuisse round the leg, 0.45 off it, from below the belt
    (so the belt's planes and its never meet) to the ankle; the strip skirt over it, 0.77 off the leg, from the hip, up
    under the belt, to mid-thigh. The left leg's are mirrored and stand LEFT_OUT further out (model()), still clear of
    vanilla boots' 1.0 shell."""
    return [am.span("cuisse_right", (-2.45, 1.0, -2.45), (2.45, 11.5, 2.45), paint=r["cuisse"]),
            am.span("skirt_right", (-2.77, -0.45, -2.77), (2.77, 5.75, 2.77), paint=r["skirt"])]


# ---------------------------------------------------------------- boots
def boot(r):
    """The right boot: the light cuff at the knee, 1.05 off the leg; the greave under it, 0.75 off (clear of vanilla
    leggings' 0.5 shell); the sabaton, 0.95 off and 1.05 behind, longer at the toe and closed underneath 0.65 below the
    foot. Their backs keep 0.1 or more off the other leg's, which stand LEFT_OUT further out."""
    return [am.span("boot_cuff_right", (-3.05, 6.75, -3.05), (3.05, 7.75, 3.05), paint=r["boot_cuff"]),
            am.span("greave_right", (-2.75, 7.75, -2.75), (2.75, 11.0, 2.75), skip=("top", "bottom"), paint=r["greave"]),
            am.span("sabaton_right", (-2.95, 11.0, -3.6), (2.95, 12.65, 3.05), paint=r["sabaton"])]


# ---------------------------------------------------------------- the set
def model():
    """{item: {bone: [parts]}} for the four pieces."""
    r = paints()
    a, l, b = arm(r), leg(r), boot(r)
    return {
        "dread_knight_helmet": {"head": helmet(r)},
        "dread_knight_chestplate": {"body": body(r), "right_arm": a, "left_arm": am.mirror_all(a)},
        "dread_knight_leggings": {"body": waist(r), "right_leg": l,
                                  "left_leg": [replace(p, inflate=LEFT_OUT) for p in am.mirror_all(l)]},
        "dread_knight_boots": {"right_leg": b, "left_leg": [replace(p, inflate=LEFT_OUT) for p in am.mirror_all(b)]},
    }


SETS = [am.ArmorSet("dread_knight", DREAD_KNIGHT, model())]
