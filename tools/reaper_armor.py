"""Reaper: the owner's hooded reaper wrapped in a great white crescent (one of the designs they sent on 8 October 2026
with no words: a render of a figure in a dark hooded robe, its face a black void behind three bars, a white clasp at its
throat, a brown pouch at its hip, white angular plates on its arms, and a crescent of white segments sweeping round it
from over its head to its feet) as a 3D worn model for jugcraft:reaper_* (helmet, chestplate, leggings, boots), on the
toolkit in tools/armor_models.py (shapes) and tools/armor_paint.py (the atlas). The name is a placeholder.

What the owner drew, and where it is here (built part for part, as the owner asked: "complex models ... really
capturing the crazy unique geometry of each armor"):
    helmet      a deep hood, its opening a black void behind three grey bars. Here: the hood, its cowl standing out
                round the opening (the brow and the two sides), the three bars 0.4 proud of the void, a peak on its
                crown, two folds down each side and the hood's fall down the back of the neck
    chestplate  the robe; a white V clasp at the throat; a brown pouch with a flap at the left hip; white angular plates
                on the arms: on the right a plate over the shoulder with a fin rising from it, two bands and a plate
                down the forearm; on the left a plate over the shoulder and two great square guards, each a white
                frame round a grey field; the crescent: a great arc of white segments behind the figure, from over its
                left shoulder, over its head and down its right side to its right foot, widest in the middle and
                narrowing to points. Here: the robe and the capelet over its shoulders, ragged along its foot with
                four tatters before and behind, three folds down its front, the clasp (two bars), the pouch and
                its flap; the sleeves, the plates, the fin, the bands and the guards; nine segments along the arc,
                6.5 behind the back and 2 deep, each laid along it and turned a little off it, one way and the next
                the other, as the owner's chain zig-zags, narrowing to the ends, and a point at each end
    leggings    the robe's skirt to below the knee, a darker apron of pleats down its front. Here: the sash under
                the robe's foot; on each leg the skirt and the apron, hinged out a little from the waist
    boots       dark wrapped boots, a white band at the ankle. Here: the boot, a grey wrap round the shin and the
                white band round the ankle
Colours: armor_paint.REAPER; the hood in its own four browns.

The crescent stands on the body, so it turns with it and never with the legs: it ends beside the right foot rather
than sweeping under the feet as the owner's does, where a walking leg would swing through it. The render shows the
front; the back is drawn in the design's words. Every box is closed: a face is left out only where another box of the
same piece and bone covers it.

Space (tools/armor_models.py): each bone's own space in model pixels; x is the model's LEFT, y is DOWN, z is the BACK
(the face is at -z).
Preview: python3 tools/armor_preview.py --set reaper
"""
import math
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


def frame(fill, border, inner=None):
    """A white frame round a field: `border` one texel round the face, `fill` within, and an `inner` square two texels
    in from the edges (painted from the corner, then the far margins painted back)."""
    rects = [(2, 2, 99, 99, inner), (-2, 0, 2, 99, fill), (0, -2, 99, 2, fill)] if inner else []
    rects += [(0, 0, 99, 1, border), (0, -1, 99, 1, border), (0, 0, 1, 99, border), (-1, 0, 1, 99, border)]
    return [solid(fill), marks(*rects)]


# ---------------------------------------------------------------- paint
PAINT = {
    "hood": {"top": [HOOD, marks((0, 0, 99, 1, W0))], "bottom": solid(W3),
             "front": [solid(X), marks((0, 0, 99, 2, x))], "*": [HOOD, marks((0, -1, 99, 1, W3))]},
    "cowl": {"top": solid(W0), "bottom": solid(W3), "front": [solid(W0), marks((0, -1, 99, 1, W2))],
             "*": [solid(W1), marks((0, 0, 99, 1, W0))]},
    "bar": {"front": [solid(M), marks((0, 0, 1, 99, L), (-1, 0, 1, 99, D))], "top": solid(L), "bottom": solid(S),
            "*": solid(D)},
    "peak": {"top": [solid(W0), marks((0, -1, 99, 1, W1))], "*": solid(W2)},
    "fold": {"*": [solid(W2), marks((0, 0, 1, 99, W1))]},
    "fall": {"back": [HOOD, marks((0, -1, 99, 1, W3))], "bottom": solid(W3), "*": solid(W2)},
    "robe": {"top": [ROBE, marks((0, 0, 99, 1, C))], "bottom": solid(K), "*": [ROBE, marks((0, -1, 99, 1, Q))]},
    "robe_fold": {"front": [solid(Q), marks((0, 0, 1, 99, q))], "*": solid(Q)},
    "capelet": {"top": [HOOD, marks((0, 0, 99, 1, W0))], "bottom": solid(W3),
                "*": [HOOD, marks((0, -1, 99, 1, W3), (1, -2, 1, 1, W3), (4, -2, 1, 1, W3), (7, -2, 1, 1, W3))]},
    "tatter": {"top": solid(W2), "bottom": solid(W3), "*": [solid(W2), marks((0, -1, 99, 1, W3))]},
    "clasp": {"front": [solid(H), marks((0, -1, 99, 1, M))], "top": solid(H), "bottom": solid(D), "*": solid(L)},
    "pouch": {"front": frame(g, G, K), "top": solid(G), "bottom": solid(K), "*": solid(g)},
    "flap": {"front": [solid(G), marks((0, -1, 99, 1, g))], "top": solid(G), "*": solid(g)},
    "sleeve": {"top": solid(C), "bottom": solid(K), "*": ROBE},
    "plate": {"top": [solid(H), marks((0, -1, 99, 1, L))], "bottom": solid(D),
              "*": [solid(L), marks((0, 0, 99, 1, H), (0, -1, 99, 1, M))]},
    "fin": {"top": solid(H), "*": [solid(L), marks((0, 0, 1, 99, H))]},
    "band": {"top": solid(H), "bottom": solid(D), "*": [solid(L), marks((0, 0, 99, 1, H), (0, -1, 99, 1, M))]},
    "guard": {"top": frame(D, H), "bottom": solid(S), "*": frame(D, H, S)},
    "glove": {"top": solid(u), "bottom": solid(X), "*": rows(u, x)},
    # the crescent: each segment a white frame round a dark hollow, as the owner's links; its edges lit above
    "segment": {"front": frame(S, H), "back": frame(S, L), "top": [solid(L), marks((0, 0, 1, 99, S), (-1, 0, 1, 99, S))],
                "bottom": [solid(D), marks((0, 0, 1, 99, V), (-1, 0, 1, 99, V))], "*": solid(S)},
    "tip": {"front": [solid(L), marks((0, 0, 99, 1, H))], "back": solid(M), "*": solid(D)},
    # the legs and feet
    "sash": {"top": solid(Q), "bottom": solid(K), "sides": [solid(Q), marks((0, 0, 99, 1, q))]},
    "skirt": {"top": solid(q), "bottom": solid(K), "*": [ROBE, marks((0, -1, 99, 1, Q))]},
    "apron": {"front": [plate(Q, "v"), marks((1, 0, 1, 99, K), (3, 0, 1, 99, K), (5, 0, 1, 99, K), (0, -1, 99, 1, K))],
              "*": solid(Q)},
    "boot": {"top": solid(u), "bottom": solid(X), "*": rows(U, u, u)},
    "ankle": {"top": solid(H), "bottom": solid(D), "*": [solid(L), marks((0, 0, 99, 1, H), (0, -1, 99, 1, M))]},
    "wrap": {"top": solid(M), "bottom": solid(S), "*": rows(M, D)},
    "toe": {"top": solid(U), "bottom": solid(X), "*": solid(u)},
}


# ---------------------------------------------------------------- shape helpers
def rod(name, base, length, section, out, back, **options):
    """A square bar standing on `base` (its bottom centre), tipped `out` degrees toward the model's right (-x) and
    leaning `back` degrees backward (negative: forward); build it on the model's right and mirror it for the left."""
    bx, by, bz = base
    s = section / 2
    return am.box(name, (bx - s, by - length, bz - s), (section, length, section), pivot=base,
                  rotation=(-back, 0, -out), **options)


# The crescent (body space): an arc of an ellipse about CENTRE, RADII across and up, in the plane DEPTH behind the body,
# from FROM degrees to TO (0 the model's left, 90 straight up, 180 its right), in SEGMENTS; each segment is its stretch
# of the arc less GAP long (GAP is negative: neighbours overlap a little where they meet, as the owner's chain does) and
# as wide as the crescent there: WIDEST at its middle, narrowing toward the ends to NARROWEST, and a point past each
# end. Taller than wide, so it rises clear over the hood without reaching far to the side.
CENTRE, RADII, DEPTH = (2.0, 5.5), (13.0, 17.0), (6.5, 8.5)
LOW_RADIUS = 19.0   # the arc's lower half reaches further down, toward the feet
FROM, TO, SEGMENTS, GAP = 50.0, 228.0, 9, -0.25
WIDEST, NARROWEST = 3.0, 2.0
ZIGZAG = 10.0   # each segment turned this much off the arc, one way and the next the other, as the owner's chain
STAGGER = 0.25   # every other segment this much nearer the back, so where neighbours cross their faces never meet


def radii(phi):
    """The ellipse's radii at phi: its lower half (below the centre) reaches LOW_RADIUS down."""
    rx, ry = RADII
    return rx, ry if math.sin(math.radians(phi)) >= 0 else LOW_RADIUS


def arc_point(phi):
    (cx, cy), (rx, ry) = CENTRE, radii(phi)
    return cx + rx * math.cos(math.radians(phi)), cy - ry * math.sin(math.radians(phi))


def arc_turn(phi):
    """Degrees about z that lay a box's length along the arc at phi."""
    rx, ry = radii(phi)
    return math.degrees(math.atan2(ry * math.cos(math.radians(phi)), rx * math.sin(math.radians(phi))))


def crescent():
    """The crescent's segments, each a box laid along its stretch of the arc (centred on the stretch's middle, as long
    as its chord less the gap) and turned ZIGZAG degrees off it, alternately; and a point at each end, narrower and
    laid on past the last segment. Every other segment stands STAGGER nearer the back."""
    z0, z1 = DEPTH
    step = (TO - FROM) / SEGMENTS
    out = []
    for i in range(SEGMENTS):
        a, b = FROM + step * i, FROM + step * (i + 1)
        phi = (a + b) / 2
        (ax, ay), (bx, by) = arc_point(a), arc_point(b)
        length = math.hypot(bx - ax, by - ay) - GAP
        width = max(NARROWEST, WIDEST * math.sin(math.pi * (i + 0.5) / SEGMENTS))
        px, py = arc_point(phi)
        turn = arc_turn(phi) + (ZIGZAG if i % 2 else -ZIGZAG)
        shift = STAGGER if i % 2 else 0.0
        out.append(am.box(f"segment_{i}", (px - length / 2, py - width / 2, z0 - shift), (length, width, z1 - z0),
                          pivot=(px, py, z0), rotation=(0, 0, turn), paint=PAINT["segment"]))
    for name, phi in (("tip_0", FROM - step * 0.3), ("tip_1", TO + step * 0.3)):
        px, py = arc_point(phi)
        out.append(am.box(name, (px - 1.25, py - 0.4, z0 + 0.4), (2.5, 0.8, z1 - z0 - 0.8), pivot=(px, py, z0),
                          rotation=(0, 0, arc_turn(phi)), paint=PAINT["tip"]))
    return out


# ---------------------------------------------------------------- the helmet
def helmet():
    """The hood, 0.75 clear of the head, its top 1.0 above it, closed underneath 0.65 below, its front the black void;
    the cowl round the opening (its brow and sides 0.75 proud of the void and sunk 0.15 into the hood, closed where
    they reach past it, the sides flaring 0.4 past the hood's); the
    three bars 0.4 proud of the void; the peak on the crown; two folds down each side, 0.45 proud; the fall down the back of the
    neck, 0.15 behind the hood."""
    fold = am.span("fold_0_right", (-5.2, -7.75, -1.5), (-4.75, -1.0, -0.75), skip="left", paint=PAINT["fold"])
    fold_1 = am.span("fold_1_right", (-5.2, -6.5, 1.75), (-4.75, 0.25, 2.5), skip="left", paint=PAINT["fold"])
    return [am.span("hood", (-4.75, -9.0, -4.75), (4.75, 0.65, 4.95), paint=PAINT["hood"]),
            am.span("cowl_brow", (-5.15, -9.25, -5.5), (5.15, -6.9, -4.6), paint=PAINT["cowl"]),
            *am.pair(am.span("cowl_right", (-5.15, -6.9, -5.5), (-3.0, 0.9, -4.6), paint=PAINT["cowl"])),
            *[am.span(f"bar_{i}", (x0, -6.5, -5.15), (x0 + 0.7, -0.6, -4.75), skip="back", paint=PAINT["bar"])
              for i, x0 in enumerate((-2.15, -0.35, 1.45))],
            am.span("peak", (-2.25, -9.75, -2.0), (2.25, -9.0, 3.25), skip="bottom", paint=PAINT["peak"]),
            fold, fold_1, *am.mirror_all([fold, fold_1]),
            am.span("fall", (-4.25, -5.0, 5.1), (4.25, 1.85, 5.6), paint=PAINT["fall"])]


# ---------------------------------------------------------------- the chestplate
def body():
    """The robe, 0.95 off the body's sides (0.2 outside the hood's, so the two never share a plane as the head turns)
    and 0.85 off its front and back, to just above the hips; over its shoulders the capelet, the hood's cloth falling
    to the chest, 1.45 off the sides (outside the hood's folds) and 1.55 off the front and back, ragged along its foot
    with tatters hanging before and behind; three folds down the robe's front from below the capelet; the clasp on the
    capelet, two bars meeting at the breastbone (the left one 0.15 further back where they cross); the pouch at the
    left hip and its flap; and the crescent behind."""
    clasp = am.chevron("clasp", (0.0, 3.6, -3.95), 3.0, 0.9, 0.4, angle=48, skip="back", paint=PAINT["clasp"])
    clasp_left = replace(clasp[1], origin=(clasp[1].origin[0], clasp[1].origin[1], clasp[1].origin[2] + 0.15))
    tatters = [am.span(f"tatter_{i}_right", (x0, 4.0, z0), (x0 + w, 4.0 + h, z0 + 0.3), paint=PAINT["tatter"])
               for i, (x0, w, h, z0) in enumerate(((-5.4, 1.6, 2.0, -3.5), (-2.9, 1.1, 2.4, -3.5), (-5.4, 1.8, 1.95, 3.2),
                                                   (-2.5, 1.2, 2.6, 3.2)))]
    return [am.span("robe", (-4.95, -0.65, -2.85), (4.95, 11.5, 2.85), paint=PAINT["robe"]),
            am.span("capelet", (-5.45, -0.9, -3.55), (5.45, 4.0, 3.55), paint=PAINT["capelet"]),
            *tatters, *am.mirror_all(tatters),
            *[am.span(f"robe_fold_{i}", (x0, 4.75, -3.1), (x0 + w, 11.5, -2.85), skip="back",
                      paint=PAINT["robe_fold"]) for i, (x0, w) in enumerate(((-3.4, 0.8), (-0.55, 1.1), (2.6, 0.8)))],
            clasp[0], clasp_left,
            am.span("pouch", (1.4, 7.9, -3.75), (4.6, 11.4, -3.1), skip="back", paint=PAINT["pouch"]),
            am.span("flap", (1.6, 7.9, -3.95), (4.4, 9.25, -3.75), skip="back", paint=PAINT["flap"]),
            *crescent()]


def arm(side):
    """An arm's robe and plates (arm space: x -3..1, y -2..10, z -2..2, the outer side -x; built as the right, the left
    its mirror image): the sleeve 0.75 off, from 0.4 above the shoulder to the wrist; the white plate over the
    shoulder, 1.35 past the arm's outer side, open below; the dark glove 0.5 off, closed 0.6 below the hand. Then the
    right arm's fin rising out of its plate, its two bands and the plate down its outer forearm, hinged out; or the
    left's two square guards, each a white frame round a grey field."""
    out = [am.span("sleeve_right", (-3.75, -2.4, -2.75), (1.75, 9.25, 2.75), paint=PAINT["sleeve"]),
           am.span("plate_right", (-4.35, -3.15, -3.15), (1.35, -0.4, 3.15), skip="bottom", paint=PAINT["plate"]),
           am.span("glove_right", (-3.5, 9.25, -2.5), (1.5, 10.6, 2.5), skip="top", paint=PAINT["glove"])]
    if side == "right":
        out += [rod("fin_right", (-3.6, -2.9, 0.0), 3.2, 0.8, out=32, back=4, paint=PAINT["fin"]),
                am.span("band_0_right", (-4.3, 4.25, -3.3), (2.3, 5.25, 3.3), paint=PAINT["band"]),
                am.span("band_1_right", (-4.3, 7.75, -3.3), (2.3, 8.75, 3.3), paint=PAINT["band"]),
                am.hinge(am.span("bracer_right", (-4.65, 5.5, -1.75), (-4.15, 7.5, 1.75), paint=PAINT["plate"]),
                         "top", 12)]
        return out
    out += [am.span("guard_0_right", (-4.6, 0.6, -3.4), (2.6, 3.8, 3.4), paint=PAINT["guard"]),
            am.span("guard_1_right", (-4.6, 5.6, -3.4), (2.6, 8.6, 3.4), paint=PAINT["guard"])]
    return am.mirror_all(out)


# ---------------------------------------------------------------- the leggings and boots
LEFT_OUT = 0.12   # the left leg's parts this much larger, so where the legs meet their faces never share a plane


def waist():
    """The sash at the robe's foot, 0.6 off the body's front and back, showing below the robe."""
    return [am.span("sash", (-4.45, 10.25, -2.6), (4.45, 12.65, 2.6), paint=PAINT["sash"])]


def leg():
    """The right leg (leg space: x -2..2, y 0..12, z -2..2): the robe's skirt round it, loose, 1.25 off (clear of
    vanilla leggings' 1.0 shell), from up under the sash to below the knee; the apron of pleats over its front, 1.65 to
    1.9 off, hinged out 4 degrees from the waist, stopping short of the centre line."""
    return [am.span("skirt_right", (-3.25, -0.45, -3.25), (3.25, 8.25, 3.25), paint=PAINT["skirt"]),
            am.hinge(am.span("apron_right", (-2.3, 0.25, -3.9), (1.75, 8.75, -3.65), paint=PAINT["apron"]), "top", 4)]


def boot():
    """The boot, 0.65 off the leg (clear of vanilla boots' 0.5 shell), from up under the skirt and closed 0.65 below
    the foot; a grey wrap round the shin and the white band round the ankle; the toe."""
    return [am.span("boot_right", (-2.65, 7.9, -2.65), (2.65, 12.65, 2.65), paint=PAINT["boot"]),
            am.span("ankle_right", (-2.95, 10.3, -2.95), (2.95, 11.05, 2.95), paint=PAINT["ankle"]),
            am.span("wrap_right", (-2.95, 8.55, -2.95), (2.95, 9.35, 2.95), paint=PAINT["wrap"]),
            am.span("toe_right", (-2.1, 11.2, -3.4), (2.1, 12.3, -2.65), skip="back", paint=PAINT["toe"])]


def left(parts):
    return [replace(p, inflate=LEFT_OUT) for p in am.mirror_all(parts)]


def model():
    l, b = leg(), boot()
    return {"reaper_helmet": {"head": helmet()},
            "reaper_chestplate": {"body": body(), "right_arm": arm("right"), "left_arm": arm("left")},
            "reaper_leggings": {"body": waist(), "right_leg": l, "left_leg": left(l)},
            "reaper_boots": {"right_leg": b, "left_leg": left(b)}}


SETS = [am.ArmorSet("reaper", REAPER, model())]
