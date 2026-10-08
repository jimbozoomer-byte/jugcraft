"""Sentinel: the owner's gold-and-black knight (one of the two designs they sent later on 8 October 2026, "Just made
these ones aswell want them done weapons too please": three Blockbench renders, from the front and a little to the
right, from a little to the left, and from behind) as a 3D worn model for jugcraft:sentinel_* (helmet, chestplate,
leggings, boots), on the toolkit in tools/armor_models.py (shapes) and tools/armor_paint.py (the atlas). Its sword and
shield are arms of their own (tools/arms_variants.py). Rebuilt in more detail when the owner saw the first one: "the
Gold Knight / Sentinel is missing very important details and is way to simplified"; its right pauldron copied again,
plate for plate and texel for texel, when they saw the second: "the shoulder pauldron on the sentinel doesn't look good
or match the art I gave you".

What the owner drew, and where it is here (built part for part, as the owner asked: "complex models ... really
capturing the crazy unique geometry of each armor"):
    helmet      a gold bucket helm, a pale rim round its top; a keyhole: a dark window at the eyes in a raised pale
                frame and a dark slit down from it to the chin, rimmed; dark channels down the cheeks; a loop like a
                little house on its crown; behind, a pale ridge between two hooks of a meander; scratches on its left
                side. Here: the helm; the rim, four bars round the top; the window's frame (its top, sides and feet)
                and the slit's rims raised on its front; two ribs down each cheek, the channels between them; the
                loop's two posts and its peaked roof; the ridge and, either side of it, the meander's hook in five
                raised bars over a dark field
    chestplate  a near-black coat under a gold gorget across the shoulders, a pale strip down its middle ending in a
                boss; a dark baldric from the left shoulder to the right hip; on the right shoulder a great pauldron, a
                thick gold plate bent at the shoulder's outer corner: rising steeply in to a pale hook by the helm and
                hanging down the outside of the upper arm to a step out at its foot, its ends banded gold outside, a
                dark groove and pale cream inside, the groove turning the bend like a 7; on the left a shelf of flat
                plates; a square gold stud with a dark centre on the right upper arm, turned; a gold bracer on the right
                forearm; gold bands on the black sleeves and gold gauntlets. Here: the coat; the gorget in two tiers,
                the strip and the boss; the baldric before and behind; on the right arm the pauldron's upper and lower
                plates, the hook and the step, their ends painted texel for texel from the owner's renders (the second
                time: "can you just 1:1 copy the pixel art from the source I gave you"); the stud; the sleeve, the
                bracer and its two rims, the gauntlet; on the left the three plates of the shelf, the sleeve, two bands
                and the gauntlet
    leggings    the coat's black skirt to the knee, parted at the middle, with a gold plate before and behind the
                left thigh and a dark one on the right. Here: the belt and its buckle; on each leg the hose, the skirt
                and a plate before and behind it hinged out from the hip, each with a raised rim round it, gold on the
                left, dark iron on the right
    boots       gold boots, chequered gold and brown at their tops, with a brown band and a pale toe. Here: the cuff,
                the greave, the band round it, the sabaton, two instep lames and the toe cap
Colours: armor_paint.SENTINEL.

The renders show the front, both sides and the back. The design is not symmetric: the pauldrons differ, only the right
forearm wears the bracer and only the left thigh's plates are gold, so the arms are built as drawn rather than mirrored.
Every box is closed: a face is left out only where another box of the same piece and bone covers it (the reliefs' and
the plates' backs, the pauldron's upper plate's end under the hook and the step's inner side on the lower plate, the
sleeves' feet and the left one's top, the greave's foot inside the sabaton).

Space (tools/armor_models.py): each bone's own space in model pixels; x is the model's LEFT, y is DOWN, z is the BACK
(the face is at -z).
Preview: python3 tools/armor_preview.py --set sentinel
"""
import math
from dataclasses import replace

import armor_models as am
from armor_paint import SENTINEL, P


H, L, M, D, S, V = "light", "mid_light", "mid", "dark", "seam", "void"       # the gold, cream to brown
G, g = "gold_light", "gold_dark"                                            # the palest cream, the deep brown
C, c, q, Q, K = "leather_light", "leather_mid_light", "leather_mid", "leather_dark", "leather_darkest"   # browns
U, u, x, X = "under_light", "under_mid", "under_dark", "under_darkest"       # the black cloth


def rows(*tones):
    """One tone per texel row, cycling, each row broken into runs of the next tone darker."""
    return P("lames", rows=1, tones=tuple((t, t) for t in tones), centre=None)


def solid(tone):
    return P("solid", tone=tone)


def marks(*rects, symmetric=False):
    return P("marks", rects=list(rects), symmetric=symmetric)


TONE = {"H": H, "L": L, "M": M, "D": D, "Q": Q, "S": S, "G": G}


def pixmap(*rows):
    """A face painted texel for texel, one string per texel row and one letter per texel (TONE names them)."""
    return marks(*[(x, y, 1, 1, TONE[ch]) for y, row in enumerate(rows) for x, ch in enumerate(row)])


def plate(tone, strips="h"):
    return P("plate", tone=tone, strips=strips)



CLOTH = P("plate", tone=x, strips="v")   # the near-black cloth, in long runs a tone lighter or darker


# ---------------------------------------------------------------- paint
PAINT = {
    # the helm: gold, its top row pale; on its front the keyhole's window and slit dark, the cheeks' channels brown;
    # behind, a dark field for the meander's raised bars; on its left side the owner's scratches
    "helm": {"front": [plate(M), marks((0, 0, 99, 1, H), (3, 3, 4, 3, X), (4, 6, 2, 4, X), (1, 5, 1, 99, Q),
                                       (-2, 5, 1, 99, Q), (0, -1, 99, 1, S))],
             "back": [solid(g), marks((0, 0, 99, 1, D), (0, -1, 99, 1, K))],
             "left": [plate(M), marks((0, 0, 99, 1, H), (5, 2, 1, 3, Q), (7, 2, 2, 1, Q), (8, 3, 1, 1, Q),
                                      (7, 4, 2, 1, Q))],
             "top": [plate(L), marks((0, 0, 99, 1, H))], "bottom": solid(V),
             "*": [plate(M), marks((0, 0, 99, 1, H), (2, 3, 2, 2, D), (6, 6, 2, 2, D), (0, -1, 99, 1, S))]},
    "rim": {"top": solid(G), "bottom": solid(D), "*": [solid(H), marks((0, -1, 99, 1, L))]},
    "frame": {"front": [solid(H), marks((0, 0, 99, 1, G))], "top": solid(G), "bottom": solid(M), "*": solid(L)},
    "slit_rim": {"front": solid(L), "top": solid(H), "*": solid(M)},
    "rib": {"front": [solid(L), marks((0, 0, 1, 99, H), (-1, 0, 1, 99, D))], "top": solid(H), "*": solid(M)},
    "loop": {"top": solid(H), "bottom": solid(D), "*": [solid(L), marks((0, 0, 99, 1, H))]},
    "ridge": {"back": [solid(H), marks((0, 0, 99, 1, G))], "*": solid(L)},
    "key": {"back": [solid(L), marks((0, 0, 99, 1, H))], "top": solid(H), "bottom": solid(D), "*": solid(M)},
    # the coat: near-black cloth, a darker line down its middle; the gorget: gold rows, a pale top
    "coat": {"front": [CLOTH, marks((4, 0, 2, 99, X))], "back": [CLOTH, marks((4, 0, 2, 99, X))],
             "top": solid(x), "bottom": solid(X), "*": CLOTH},
    "mantle": {"top": [solid(H), marks((0, -1, 99, 1, L))], "bottom": solid(S),
               "*": [rows(L, M, L, D), marks((0, 0, 99, 1, H))]},
    "gorget": {"front": [rows(M, L), marks((0, 0, 99, 1, H), (0, -1, 99, 1, D))], "top": solid(H), "bottom": solid(S),
               "*": solid(M)},
    "strip": {"front": [solid(G), marks((0, 0, 1, 99, H), (-1, 0, 1, 99, L))], "*": solid(L)},
    "boss": {"front": P("chevron", corner="o", bands=(H, L), border=None, core=(D, D), outside=H), "top": solid(H),
             "*": solid(M)},
    # the cap over the left shoulder's plates
    "cap": {"top": [plate(L), marks((0, 0, 99, 1, H), (0, -1, 99, 1, D))], "bottom": solid(S),
            "*": [plate(L), marks((0, 0, 99, 1, H))]},
    # the right pauldron, its plates' ends painted texel for texel from the owner's renders (pixmap rows: the outer
    # edge first on the upper plate and the hook, from the top on the lower plate): a pale cream band inside, a dark
    # groove, gold outside, the groove turning the bend as a 7 and stopping short of the foot; the outer faces gold
    # between a pale rim and a dark groove at each edge
    "pauldron_upper": {"front": pixmap("LLMM", "QQDD", "HHHL"), "back": pixmap("MMLL", "DDQQ", "LHHH"),
                       "top": [plate(L), marks((0, 0, 99, 1, H), (0, 1, 99, 1, Q), (0, -2, 99, 1, Q), (0, -1, 99, 1, H))],
                       "bottom": solid(Q), "*": solid(L)},
    "pauldron_hook": {"front": pixmap("HH", "LL", "LL", "LL", "LL"), "back": pixmap("HH", "LL", "LL", "LL", "LL"),
                      "top": [solid(H), marks((0, 0, 99, 1, L), (0, -1, 99, 1, L))], "bottom": solid(D),
                      "*": [solid(L), marks((0, 0, 99, 1, H))]},
    "pauldron_lower": {"front": pixmap("LQQ", "MQH", "MQL", "MQL", "MLL"),
                       "back": pixmap("QQL", "HQM", "LQM", "LQM", "LLM"),
                       "right": pixmap("HLLLLH", "HQMMQH", "HQMMQH", "HQMMQH", "HQMMQH"),
                       "left": solid(S), "bottom": solid(D), "*": solid(L)},
    "pauldron_flare": {"right": pixmap("HQMMQH", "HQMMQH"), "top": solid(L), "bottom": solid(D), "*": solid(M)},
    # the square stud on the upper arm's front: a frame lit from the top right round a dark centre
    "stud": {"front": pixmap("LHH", "MQH", "MML"), "top": solid(H), "bottom": solid(M), "back": solid(D),
             "*": solid(L)},
    "baldric": {"front": [solid(u), marks((0, 0, 99, 1, U))], "back": [solid(u), marks((0, 0, 99, 1, U))], "*": solid(x)},
    "sleeve": {"sides": CLOTH, "ends": solid(X)},
    "band": {"top": solid(H), "bottom": solid(S), "*": [solid(L), marks((0, 0, 99, 1, H), (0, -1, 99, 1, M))]},
    "bracer": {"top": solid(L), "bottom": solid(S),
               "*": [plate(M), marks((0, 1, 99, 1, D), (0, -2, 99, 1, D), (2, 2, 1, 2, Q), (-3, 2, 1, 2, Q))]},
    "gauntlet": {"top": solid(H), "bottom": solid(V), "*": [rows(L, M, D), marks((0, 0, 99, 1, H))]},
    # the left pauldron: flat plates, gold in brown stripes, a pale edge
    "slab": {"top": [plate(L), marks((0, 0, 99, 1, H))], "bottom": solid(S), "*": [rows(L, Q, M, Q), marks((0, 0, 99, 1, H))]},
    # the legs: the belt and buckle, the hose, the black skirt with its folds; the plates gold on the left, dark iron
    # on the right, each rimmed
    "belt": {"top": solid(x), "bottom": solid(X), "sides": [solid(u), marks((0, 0, 99, 1, L), (0, -1, 99, 1, X))]},
    "buckle": {"front": [solid(H), marks((1, 1, 1, 1, D))], "top": solid(G), "*": solid(L)},
    "hose": {"sides": CLOTH, "ends": solid(X)},
    "skirt": {"front": [CLOTH, marks((1, 0, 1, 99, X), (-2, 0, 1, 99, u))], "back": [CLOTH, marks((2, 0, 1, 99, X))],
              "top": solid(x), "bottom": solid(X), "*": CLOTH},
    "plate_gold": {"front": [plate(L), marks((1, 1, 2, 1, Q), (1, 1, 1, 3, Q), (-3, -2, 2, 1, Q), (-2, -4, 1, 3, Q))],
                   "back": [plate(M), marks((1, 1, 2, 1, Q), (-2, -4, 1, 3, Q))],
                   "top": solid(H), "bottom": solid(S), "*": solid(M)},
    "plate_iron": {"front": [rows(U, u), marks((1, 1, 1, 99, X))], "back": [rows(U, u), marks((1, 1, 1, 99, X))],
                   "top": solid(U), "bottom": solid(X), "*": solid(u)},
    "plate_rim_gold": {"top": solid(G), "bottom": solid(D), "*": [solid(H), marks((0, -1, 99, 1, L))]},
    "plate_rim_iron": {"top": solid(U), "bottom": solid(X), "*": [solid(U), marks((0, -1, 99, 1, u))]},
    "cuff": {"top": solid(H), "bottom": solid(S), "*": P("checker", tones=(L, Q), size=1)},
    "greave": {"top": solid(H), "*": [plate(L), marks((0, 0, 99, 1, H), (0, 2, 99, 1, M))]},
    "boot_band": {"top": solid(c), "bottom": solid(K), "*": rows(q, Q)},
    "sabaton": {"top": solid(L), "bottom": solid(V), "*": [plate(M), marks((0, 0, 99, 1, L), (0, -1, 99, 1, S))]},
    "toe": {"top": solid(G), "bottom": solid(D), "*": [solid(H), marks((0, -1, 99, 1, L))]},
    "instep": {"top": solid(H), "bottom": solid(S), "*": [solid(L), marks((0, 0, 99, 1, H), (0, -1, 99, 1, D))]},
    "rivet": {"top": solid(G), "*": solid(H)},
}


# ---------------------------------------------------------------- the helmet
def key(side):
    """The meander's hook behind the helm on the model's right (mirrored for the left): five raised bars 0.3 proud of
    the dark field, meeting end to end, from the outer top round and in to a hook beside the ridge."""
    bars = [((-4.3, -7.7), (-1.0, -7.0)),    # along the top
            ((-4.3, -7.0), (-3.75, -2.4)),   # down the outer side
            ((-3.75, -3.35), (-1.0, -2.4)),  # back along the foot
            ((-1.7, -5.4), (-1.0, -3.35)),   # up the inner side
            ((-2.9, -5.4), (-1.7, -4.7))]    # the hook, turning out
    out = [am.span(f"key_{i}_right", (x0, y0, 5.0), (x1, y1, 5.3), skip="front", paint=PAINT["key"])
           for i, ((x0, y0), (x1, y1)) in enumerate(bars)]
    return out if side == "right" else am.mirror_all(out)


def helmet():
    """The helm, 1.0 clear of the head's sides, front and back, its top 1.0 above it, closed underneath 0.65 below;
    the rim round its top, four bars 0.3 proud and 0.3 above it (the front and back bars the full width, the sides
    between them); the keyhole's frame 0.4 proud of its front and the slit's rims 0.2 proud; two ribs down each cheek,
    0.3 proud; the loop on its crown, its roof's left bar 0.15 behind the right so the two never share a plane where
    they cross; the ridge 0.35 proud behind and the meander's two hooks either side of it."""
    frame = [am.span("frame_side_right", (-3.0, -6.25, -5.4), (-2.0, -3.4, -5.0), skip="back", paint=PAINT["frame"]),
             am.span("frame_foot_right", (-3.0, -3.4, -5.4), (-1.0, -2.4, -5.0), skip="back", paint=PAINT["frame"]),
             am.span("slit_rim_right", (-2.0, -2.4, -5.2), (-1.0, 0.0, -5.0), skip="back", paint=PAINT["slit_rim"]),
             am.span("rib_outer_right", (-4.45, -5.6, -5.3), (-3.9, 0.5, -5.0), skip="back", paint=PAINT["rib"]),
             am.span("rib_inner_right", (-3.7, -2.3, -5.3), (-3.1, 0.5, -5.0), skip="back", paint=PAINT["rib"])]
    post = am.span("post_right", (-2.1, -10.6, -0.7), (-1.35, -9.3, 0.7), skip="bottom", paint=PAINT["loop"])
    roof_right, roof_left = am.chevron("roof", (0.0, -11.45, -0.35), 2.45, 0.75, 0.7, angle=-32.0, paint=PAINT["loop"])
    roof_left = replace(roof_left, origin=(roof_left.origin[0], roof_left.origin[1], roof_left.origin[2] + 0.15))
    rim_side = am.span("rim_right", (-5.3, -9.3, -4.7), (-4.7, -8.3, 4.7), paint=PAINT["rim"])
    return [am.span("helm", (-5.0, -9.0, -5.0), (5.0, 0.65, 5.0), paint=PAINT["helm"]),
            am.span("rim_front", (-5.3, -9.3, -5.3), (5.3, -8.3, -4.7), paint=PAINT["rim"]),
            am.span("rim_back", (-5.3, -9.3, 4.7), (5.3, -8.3, 5.3), paint=PAINT["rim"]),
            *am.pair(rim_side),
            am.span("frame_top", (-3.0, -7.25, -5.4), (3.0, -6.25, -5.0), skip="back", paint=PAINT["frame"]),
            *frame, *am.mirror_all(frame),
            *am.pair(post), roof_right, roof_left,
            am.span("ridge", (-0.5, -8.0, 5.0), (0.5, 0.0, 5.35), skip="front", paint=PAINT["ridge"]),
            *key("right"), *key("left")]


# ---------------------------------------------------------------- the chestplate
def body():
    """The coat, 0.65 off the body's sides and 0.95 off its front and back; the gorget across the shoulders, 1.25 off
    its sides and front and back (0.25 outside the helm's sides, so the two never share a plane as the head turns),
    and its lower tier before the chest, 0.2 prouder; the pale strip down the middle from it, and the boss at its foot;
    the baldric, a strap from the left shoulder down to the right hip, 0.15 proud of the coat before and behind, cut
    to the coat so it never overhangs it."""
    baldric = am.box("baldric", (-6.6, 4.25, -3.1), (13.2, 1.5, 0.15), pivot=(0.0, 5.0, -3.1), rotation=(0, 0, -52),
                     skip="back", paint=PAINT["baldric"])
    baldric_back = am.box("baldric_back", (-6.6, 4.25, 2.95), (13.2, 1.5, 0.15), pivot=(0.0, 5.0, 2.95),
                          rotation=(0, 0, -52), skip="front", paint=PAINT["baldric"])
    return [am.span("coat", (-4.65, -0.65, -2.95), (4.65, 10.6, 2.95), paint=PAINT["coat"]),
            am.span("mantle", (-5.25, -0.95, -3.25), (5.25, 1.4, 3.25), paint=PAINT["mantle"]),
            am.span("gorget", (-3.6, 1.4, -3.45), (3.6, 2.7, -2.95), skip="back", paint=PAINT["gorget"]),
            am.span("strip", (-0.75, 2.7, -3.3), (0.75, 6.1, -2.95), skip="back", paint=PAINT["strip"]),
            am.span("boss", (-1.0, 6.1, -3.55), (1.0, 7.3, -2.95), skip="back", paint=PAINT["boss"]),
            baldric, baldric_back]


def framed(name, pivot, angle, x, y, z, **options):
    """A box in a frame turned `angle` degrees about z round `pivot` (an arm-space (x, y) point): x, y and z are its
    (lo, hi) bounds in that frame."""
    (x0, x1), (y0, y1), (z0, z1) = x, y, z
    return am.box(name, (pivot[0] + x0, pivot[1] + y0, z0), (x1 - x0, y1 - y0, z1 - z0),
                  pivot=(pivot[0], pivot[1], 0.0), rotation=(0, 0, angle), **options)


# The right pauldron's two plates, measured off the owner's renders (the arm 20 degrees out there): the upper plate's
# frame runs up and in along it from the bend's inner corner (y across it, the outer face at -3), the lower plate's runs
# down it from the inner face (x across it, the outer face at -3). The plates are 3 px thick and meet at the bend: the
# upper one starts, and the lower one's top ends, at the outer corner where their outer faces cross.
UPPER = ((-2.6, 0.35), -48.0)
LOWER = ((-2.45, 1.7), -10.0)


def arm_right():
    """The right arm (arm space: x -3..1, y -2..10, z -2..2, the outer side -x): the pauldron as the owner drew it, a
    bent plate standing out from the shoulder: the upper plate rising steeply in to the neck, the hook at its top end
    (1.2 prouder than the plate, a pale lip by the helm), the lower plate down the outside of the upper arm leaning in
    a little toward its foot, and the step out at its foot; the lower plate 0.15 deeper before and behind than the upper,
    so where they cross at the bend their faces never meet. The square stud on the upper arm's front below the
    pauldron's inner edge, turned as drawn; the black sleeve from the shoulder to the gauntlet; the gold bracer on the
    forearm and its two rims; the gauntlet, to 0.6 below the hand."""
    (up, up_turn), (low, low_turn) = UPPER, LOWER
    pauldron = [framed("pauldron_upper_right", up, up_turn, (-1.572, 2.428), (-3.0, 0.0), (-2.85, 2.85), skip="left",
                       paint=PAINT["pauldron_upper"]),
                framed("pauldron_hook_right", up, up_turn, (2.428, 4.428), (-4.2, 0.0), (-3.0, 3.0),
                       paint=PAINT["pauldron_hook"]),
                framed("pauldron_lower_right", low, low_turn, (-3.0, 0.0), (-2.752, 2.248), (-3.0, 3.0),
                       paint=PAINT["pauldron_lower"]),
                framed("pauldron_flare_right", low, low_turn, (-4.0, -3.0), (0.248, 2.248), (-3.0, 3.0), skip="left",
                       paint=PAINT["pauldron_flare"])]
    stud = am.box("stud_right", (-1.85, 1.15, -3.35), (3.0, 3.0, 0.75), pivot=(-0.35, 2.65, -2.975),
                  rotation=(0, 0, -20), paint=PAINT["stud"])
    return [*pauldron, stud,
            am.span("sleeve_right", (-3.45, -2.75, -2.45), (1.45, 9.0, 2.45), skip="bottom", paint=PAINT["sleeve"]),
            am.span("bracer_right", (-3.8, 5.6, -2.8), (1.8, 8.4, 2.8), paint=PAINT["bracer"]),
            am.span("bracer_rim_top_right", (-4.1, 5.3, -3.25), (2.1, 5.9, 3.25), paint=PAINT["band"]),
            am.span("bracer_rim_foot_right", (-4.1, 8.1, -3.25), (2.1, 8.7, 3.25), paint=PAINT["band"]),
            am.span("gauntlet_right", (-3.9, 8.75, -3.05), (1.9, 10.6, 3.05), paint=PAINT["gauntlet"])]


def arm_left():
    """The left arm (arm space: x -1..3, y -2..10, z -2..2, the outer side +x): a low crown and three flat plates over the
    shoulder, each lower one reaching further out; the black sleeve, two gold bands and the gauntlet."""
    return [am.span("slab_crown_left", (-1.0, -4.0, -2.75), (3.75, -3.25, 2.75), skip="bottom", paint=PAINT["cap"]),
            am.span("slab_left", (-1.75, -3.25, -3.65), (4.5, -1.75, 3.65), paint=PAINT["slab"]),
            am.span("slab_low_left", (-1.25, -1.75, -3.45), (4.85, -0.25, 3.45), paint=PAINT["slab"]),
            am.span("slab_lowest_left", (-0.75, -0.25, -3.25), (5.2, 1.0, 3.25), paint=PAINT["slab"]),
            am.span("sleeve_left", (-1.45, -2.75, -2.45), (3.45, 9.0, 2.45), skip=("top", "bottom"), paint=PAINT["sleeve"]),
            am.span("band_a_left", (-1.7, 5.75, -2.75), (3.7, 6.6, 2.75), paint=PAINT["band"]),
            am.span("band_b_left", (-1.7, 7.6, -2.75), (3.7, 8.45, 2.75), paint=PAINT["band"]),
            am.span("gauntlet_left", (-1.9, 8.75, -3.05), (3.9, 10.6, 3.05), paint=PAINT["gauntlet"])]


# ---------------------------------------------------------------- the leggings and boots
LEFT_OUT = 0.12   # the left leg's parts this much larger, so where the legs meet their faces never share a plane


def waist():
    """The belt, 0.6 off the body's front and back and 0.45 off its sides, and its gold buckle before, below the coat
    and 1.25 off (clear of vanilla leggings' 1.0 shell)."""
    return [am.span("belt", (-4.45, 10.25, -2.6), (4.45, 12.65, 2.6), paint=PAINT["belt"]),
            am.span("buckle", (-1.2, 10.75, -3.25), (1.2, 12.35, -2.6), skip="back", paint=PAINT["buckle"])]


def thigh_plate(name, z0, z1, paint, rim_paint):
    """A plate before (z0 < z1 < 0) or behind the thigh, hinged out 8 degrees from the hip, and the raised rim round
    its face, riding its hinge: the top and foot bars 0.15 in from the plate's edges and 0.2 proud, the sides between
    them 0.15 further in and 0.35 proud, so no two of them share a plane where they meet."""
    plate_part = am.hinge(am.span(name, (-2.55, 0.25, z0), (1.45, 6.25, z1), paint=paint), "top", 8)
    front = z0 < 0
    rims = []
    for i, ((x0, y0), (x1, y1), lift) in enumerate((((-2.4, 0.4), (1.3, 0.95), 0.2), ((-2.4, 5.55), (1.3, 6.1), 0.2),
                                                   ((-2.25, 0.95), (-1.7, 5.55), 0.35), ((0.6, 0.95), (1.15, 5.55), 0.35))):
        lo, hi = (z0 - lift, z0) if front else (z1, z1 + lift)
        rims.append(replace(am.span(f"{name}_rim_{i}", (x0, y0, lo), (x1, y1, hi), skip="back" if front else "front",
                                    paint=rim_paint), turns=plate_part.turns))
    return [plate_part, *rims]


def leg(plate_paint, rim_paint):
    """The right leg (leg space: x -2..2, y 0..12, z -2..2): the hose, 0.45 off the leg from inside the skirt into the
    boot (closed at its foot, which shows when the leggings are worn alone), so the leg never shows between skirt and
    boot; the skirt round the leg, 0.77 off, from up under the belt to below the knee; a plate before and behind the
    thigh, 1.15 off (clear of vanilla leggings' 1.0 shell), hinged out 8 degrees from the hip, each rimmed."""
    return [am.span("hose_right", (-2.45, 0.8, -2.45), (2.45, 11.0, 2.45), skip="top", paint=PAINT["hose"]),
            am.span("skirt_right", (-2.77, -0.45, -2.77), (2.77, 7.75, 2.77), paint=PAINT["skirt"]),
            *thigh_plate("plate_front_right", -3.45, -3.15, plate_paint, rim_paint),
            *thigh_plate("plate_back_right", 3.15, 3.45, plate_paint, rim_paint)]


def boot():
    """A cuff chequered gold and brown at the greave's top, 1.1 off the leg, as the owner's; the greave from just below the skirt (the hose covers the leg
    between), 0.75 off (clear of vanilla leggings' 0.5 shell); the brown band round it, 1.0 off; the sabaton, 0.95 off
    and 1.05 behind, closed 0.65 below the foot; two instep lames overlapping toward the toe; the pale toe cap. Where
    the legs overlap, each of these keeps 0.1 or more off the other leg's planes."""
    out = [am.span("cuff_right", (-3.1, 8.0, -3.1), (3.1, 8.75, 3.1), paint=PAINT["cuff"]),
           am.span("greave_right", (-2.75, 8.35, -2.75), (2.75, 11.0, 2.75), skip="bottom", paint=PAINT["greave"]),
           am.span("boot_band_right", (-3.0, 9.0, -3.0), (3.0, 9.85, 3.0), paint=PAINT["boot_band"]),
           am.span("sabaton_right", (-2.95, 11.0, -3.6), (2.95, 12.65, 3.05), paint=PAINT["sabaton"]),
           am.span("toe_right", (-2.2, 11.3, -3.95), (2.2, 12.35, -3.6), skip="back", paint=PAINT["toe"])]
    for i in range(2):   # instep lames, scales overlapping toward the toe, each 0.15 past the one above at its sides
        x0 = -3.1 - 0.15 * i
        out.append(am.span(f"instep_right_{i}", (x0, 10.2 + 0.55 * i, -3.45 - 0.5 * i), (-x0, 11.1 + 0.55 * i, -1.7 - 0.5 * i),
                           pivot=(0, 10.2 + 0.55 * i, -1.7 - 0.5 * i), rotation=(12, 0, 0), paint=PAINT["instep"]))
    return out


def left(parts):
    return [replace(p, inflate=LEFT_OUT) for p in am.mirror_all(parts)]


def model():
    """{item: {bone: [parts]}} for the four pieces. The left leg's plates are gold, the right's dark iron: the left leg
    is the right one's mirror with the plates and their rims repainted (each under a name of its own, so it has a
    texture of its own)."""
    right_leg = leg(PAINT["plate_iron"], PAINT["plate_rim_iron"])
    left_leg = [replace(p, paint=PAINT["plate_rim_gold"] if "_rim_" in p.name else PAINT["plate_gold"], net="")
                if p.name.startswith("plate_") else p for p in left(right_leg)]
    b = boot()
    return {"sentinel_helmet": {"head": helmet()},
            "sentinel_chestplate": {"body": body(), "right_arm": arm_right(), "left_arm": arm_left()},
            "sentinel_leggings": {"body": waist(), "right_leg": right_leg, "left_leg": left_leg},
            "sentinel_boots": {"right_leg": b, "left_leg": left(b)}}


SETS = [am.ArmorSet("sentinel", SENTINEL, model())]
