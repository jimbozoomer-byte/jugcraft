"""Wight King: the owner's slate-blue knight crowned with icicles and antlers (one of the designs they sent on 8 October
2026 with no words: two renders, from the front and from behind on the left, of a grey-blue knight with a crown of
icicles, two tall antlers, a black skull's face, cyan gems and a long sword) as a 3D worn model for
jugcraft:wight_king_* (helmet, chestplate, leggings, boots), on the toolkit in tools/armor_models.py (shapes) and
tools/armor_paint.py (the atlas). Its sword is an arm of its own (tools/arms_variants.py). The name is a placeholder:
the renders carry none.

What the owner drew, and where it is here (built part for part, as the owner asked: "complex models ... really
capturing the crazy unique geometry of each armor"):
    helmet      a slate helm whose face is a black skull: a brow bar over two black eyes, a bar down the nose, cheek
                plates either side of a mouth of teeth; a crown of icicles round its top, the middle one at the front
                the tallest; two tall antlers rising from its sides, bending out and then up, pale at their tips.
                Here: the helm and the crown's band; the black mask 0.4 proud of the helm's front, the brow bar, the
                nose bar, two cheek plates leaning out at their tops, four teeth and a chin bar on it; seven icicles on
                the band, each one to three stacked prisms turned on their edge and tapering, the front middle one
                tallest and the outer ones leaning out; two antlers of four bars each, every bar narrower and more
                upright than the one below it, the last one pale
    chestplate  slate plate: a raised V collar meeting at a cyan gem on the breastbone, two dark straps crossing over
                the belly; great shoulders, the right a mass of jagged ice shards, the left a layered block; mail under
                them, banded vambraces with flared cuffs, gauntlets. Here: the cuirass, the collar (two bars), the gem;
                the two straps crossing and a boss where they cross; a back plate and a spine; on the right arm the
                pauldron, its crown, four shards (flat blades with narrower tips) and two lames hinged out under it; on
                the left the pauldron, a cap on it and a second cap on that, a rim round its foot and one shard; on
                both the mail sleeve, the vambrace, the cuff with a fin swept back and the gauntlet
    leggings    a dark belt with a square cyan buckle, two plates hanging over each thigh's front, banded thighs, a
                knee cop. Here: the belt, the buckle's frame and its gem; on each leg the banded cuisse, two tassets
                hinged out from their tops, the knee band and the knee cop
    boots       greaves of bands, pointed sabatons. Here: three rings, the middle one sunk between the others; the
                sabaton and a pointed toe
Colours: armor_paint.WIGHT_KING; the icicles and antlers in its five ice tones.

The renders show the front and the left side from behind; the right is drawn from the front view. The glow round the
knight in the renders, and the bits of ice floating round it, were taken as the scene, not the armor. Every box is
closed: a face is left out only where another box of the same piece and bone covers it (the reliefs' backs on the
plates they sit on, the bars' feet inside the bar below).

Space (tools/armor_models.py): each bone's own space in model pixels; x is the model's LEFT, y is DOWN, z is the BACK
(the face is at -z).
Preview: python3 tools/armor_preview.py --set wight_king
"""
from dataclasses import replace

import armor_models as am
from armor_paint import WIGHT_KING, P


def rows(*tones):
    """One tone per texel row, cycling, each row broken into runs of the next tone darker."""
    return P("lames", rows=1, tones=tuple((t, t) for t in tones), centre=None)


def solid(tone):
    return P("solid", tone=tone)


def marks(*rects, symmetric=False):
    return P("marks", rects=list(rects), symmetric=symmetric)


def plate(tone, strips="h"):
    return P("plate", tone=tone, strips=strips)


H, L, M, D, S, V = "light", "mid_light", "mid", "dark", "seam", "void"      # the slate, pale ice to deep slate
G, g = "gold_light", "gold_dark"                                           # the gems' cyans
C, c, q, Q, K = "leather_light", "leather_mid_light", "leather_mid", "leather_dark", "leather_darkest"   # dark slate
U, u, x, X = "under_light", "under_mid", "under_dark", "under_darkest"      # the face's black
I0, I1, I2, I3, I4 = "ice_light", "ice", "ice_mid", "ice_dark", "ice_deep"  # the icicles and antlers

SLATE = plate(M)   # the slate plate: its mottling the plate's runs a tone lighter or darker


# ---------------------------------------------------------------- paint
PAINT = {
    "helm": {"top": [SLATE, marks((0, 0, 99, 1, L))], "bottom": solid(S), "*": [SLATE, marks((0, -1, 99, 1, D))]},
    "band": {"top": solid(L), "bottom": solid(S),
             "*": [solid(M), marks((0, 0, 99, 1, L), (0, -1, 99, 1, S), (2, 1, 1, 1, D), (6, 1, 1, 1, D))]},
    "mask": {"front": [solid(X), marks((0, 0, 99, 1, x))], "*": solid(x)},
    "brow": {"front": [solid(L), marks((0, 0, 99, 1, H), (0, -1, 99, 1, D))], "top": solid(H), "bottom": solid(S),
             "*": solid(M)},
    "nose": {"front": [solid(L), marks((0, 0, 1, 99, H))], "*": solid(M)},
    "cheek": {"front": [solid(L), marks((0, 0, 1, 99, H), (-1, 0, 1, 99, D))], "top": solid(H), "*": solid(M)},
    "tooth": {"front": [solid(H), marks((0, -1, 99, 1, L))], "top": solid(H), "*": solid(L)},
    "chin": {"front": [solid(L), marks((0, 0, 99, 1, H), (0, -1, 99, 1, D))], "top": solid(H), "*": solid(M)},
    # the icicles: lit on their upper faces, paling to the tip
    "ice": {"top": solid(I0), "bottom": solid(I3), "front": [rows(I2, I1), marks((0, 0, 1, 99, I1))],
            "right": [rows(I1, I2), marks((-1, 0, 1, 99, I3))], "left": [rows(I2, I3), marks((0, 0, 1, 99, I1))],
            "back": rows(I3, I4)},
    "ice_tip": {"top": solid(I0), "bottom": solid(I2), "front": solid(I0), "right": solid(I0), "left": solid(I1),
                "back": solid(I1)},
    # the antlers: slate at their feet, paler bar by bar
    "antler_0": {"top": solid(M), "bottom": solid(S), "front": [rows(D, M), marks((0, 0, 1, 99, M))],
                 "right": rows(M, D), "left": rows(D, S), "back": rows(S, D)},
    "antler_1": {"top": solid(L), "bottom": solid(D), "front": [rows(M, L), marks((0, 0, 1, 99, L))],
                 "right": rows(L, M), "left": rows(M, D), "back": rows(D, M)},
    "antler_2": {"top": solid(I1), "bottom": solid(M), "front": [rows(I2, I1), marks((0, 0, 1, 99, I1))],
                 "right": rows(I1, I2), "left": rows(I2, I3), "back": rows(I3, I2)},
    "antler_3": {"top": solid(I0), "*": [solid(I0), marks((-1, 0, 1, 99, I1))]},
    "cuirass": {"top": [SLATE, marks((0, 0, 99, 1, L))], "bottom": solid(S), "*": SLATE},
    "collar": {"front": [solid(L), marks((0, 0, 99, 1, H), (0, -1, 99, 1, D))], "top": solid(H), "bottom": solid(D),
               "*": solid(M)},
    "gem": {"front": [solid(g), marks((0, 0, 1, 1, G), (1, 0, 1, 1, G), (0, 1, 1, 1, G), (-1, -1, 1, 1, Q))],
            "*": solid(g)},
    "strap": {"front": [solid(q), marks((0, 0, 99, 1, c))], "*": solid(Q)},
    "boss": {"front": [solid(M), marks((0, 0, 99, 1, L), (0, 0, 1, 99, L), (-1, 0, 1, 99, D), (0, -1, 99, 1, D),
                                       (1, 1, 1, 1, G))],
             "*": solid(D)},
    "back_plate": {"back": [SLATE, marks((0, 0, 99, 1, L), (0, -1, 99, 1, S))], "*": solid(D)},
    "spine": {"back": [solid(L), marks((0, 0, 1, 99, H))], "top": solid(H), "*": solid(M)},
    # the shoulders: slate blocks, the shards ice
    "pauldron": {"top": [SLATE, marks((0, 0, 99, 1, L))], "bottom": solid(S),
                 "*": [SLATE, marks((0, 0, 99, 1, L), (0, -1, 99, 1, S))]},
    "cap": {"top": [solid(L), marks((0, 0, 99, 1, H))], "*": [solid(M), marks((0, 0, 99, 1, L), (0, -1, 99, 1, D))]},
    "rim": {"top": solid(L), "bottom": solid(S), "*": [solid(L), marks((0, 0, 99, 1, H), (0, -1, 99, 1, D))]},
    "shard": {"top": solid(L), "bottom": solid(S), "front": [SLATE, marks((0, 0, 1, 99, L), (-1, 0, 1, 99, D))],
              "back": [solid(D), marks((-1, 0, 1, 99, S))], "*": solid(M)},
    "shard_tip": {"top": solid(I0), "*": [solid(I1), marks((0, 0, 1, 99, I0))]},
    "lame": {"top": solid(L), "bottom": solid(S), "*": [rows(M, L), marks((0, -1, 99, 1, D))]},
    "sleeve": {"top": solid(c), "bottom": solid(K), "*": rows(q, c)},
    "vambrace": {"top": solid(L), "bottom": solid(S), "*": rows(L, M, D)},
    "cuff": {"top": solid(H), "bottom": solid(D), "*": [solid(L), marks((0, 0, 99, 1, H), (0, -1, 99, 1, D))]},
    "fin": {"top": solid(L), "*": [solid(M), marks((0, 0, 1, 99, L))]},
    "gauntlet": {"top": solid(M), "bottom": solid(S), "*": [rows(M, D), marks((0, 0, 99, 1, L))]},
    # the legs: the dark belt and its cyan buckle, the banded thighs, the tassets and the knee
    "belt": {"top": solid(q), "bottom": solid(K), "sides": [solid(q), marks((0, 0, 99, 1, c), (0, -1, 99, 1, Q))]},
    "buckle": {"front": [solid(L), marks((0, 0, 99, 1, H), (0, 0, 1, 99, H), (-1, 0, 1, 99, D), (0, -1, 99, 1, D))],
               "*": solid(M)},
    "buckle_gem": {"front": [solid(g), marks((0, 0, 1, 1, G), (1, 0, 1, 1, G))], "*": solid(g)},
    "cuisse": {"top": solid(L), "bottom": solid(S), "*": rows(M, M, Q)},
    "tasset": {"top": solid(H), "bottom": solid(D),
               "*": [SLATE, marks((0, 0, 99, 1, L), (0, 0, 1, 99, L), (-1, 0, 1, 99, D), (0, -1, 99, 1, S))]},
    "knee_band": {"top": solid(L), "bottom": solid(S), "*": [solid(M), marks((0, 0, 99, 1, L), (0, -1, 99, 1, D))]},
    "knee": P("chevron", corner="o", bands=(L, M), border=None, core=(D, S), outside=L),
    "ring": {"top": solid(L), "bottom": solid(S), "*": [rows(L, M), marks((0, -1, 99, 1, D))]},
    "ring_sunk": {"*": rows(Q, q)},
    "sabaton": {"top": solid(L), "bottom": solid(S), "*": [SLATE, marks((0, 0, 99, 1, L), (0, -1, 99, 1, S))]},
    "toe": {"top": solid(H), "bottom": solid(D), "*": [solid(L), marks((0, -1, 99, 1, M))]},
}


# ---------------------------------------------------------------- shape helpers
def prism(name, base, length, width, out=0.0, back=0.0, **options):
    """A square bar standing on `base` (its bottom centre), turned 45 degrees about its own length so an edge faces
    front, then tipped `out` degrees toward the model's right (-x) and leaning `back` degrees backward (negative:
    forward). Build it on the model's right and mirror it for the left."""
    bx, by, bz = base
    s = width / 2
    part = am.box(name, (bx - s, by - length, bz - s), (width, length, width), pivot=base, rotation=(0, 45, 0),
                  **options)
    if out:
        part = am.turned(part, "z", -out, base)
    return am.turned(part, "x", -back, base) if back else part


def icicle(name, base, steps, lean=0.0):
    """An icicle standing on `base`: prisms stacked up from it, (height, width) each, slate at the foot and paler up
    it, the last one the pale tip; the whole leaning `lean` degrees out toward the model's right (-x). Each stands on
    the top of the one below, which hides its foot."""
    bx, by, bz = base
    out, y = [], by
    for i, (h, w) in enumerate(steps):
        tip = i == len(steps) - 1
        part = prism(f"{name}_{i}", (bx, y, bz), h, w, skip="bottom" if i else (),
                     paint=PAINT["ice_tip" if tip else f"antler_{min(i, 2)}"])
        out.append(am.turned(part, "z", -lean, base) if lean else part)
        y -= h
    return out


def antler(name, base, bars, back):
    """An antler from `base` (its foot, in the helm's corner): bars of (length, width, out) stacked tip to foot, each
    tipped `out` degrees toward the model's right (-x) and all leaning `back` degrees backward; each bar starts 0.3
    down inside the tip of the one below, so the antler bends out and then up. Painted paler bar by bar."""
    out, point = [], base
    for i, (length, width, lean) in enumerate(bars):
        part = prism(f"{name}_{i}", point, length, width, out=lean, back=back, skip="bottom" if i else (),
                     paint=PAINT[f"antler_{min(i, 3)}"])
        out.append(part)
        point = am.place(part, (point[0], point[1] - length + 0.3, point[2]))
    return out


def shard(name, base, length, width, out, back, thickness=0.45):
    """A flat ice shard standing on `base`: a blade `width` wide and `thickness` thin, its broad face to the front, and a
    tip half as wide and 0.2 thinner on its last 0.4 of the length; tipped `out` degrees toward the model's right (-x)
    and leaning `back` degrees backward. The tip stands on the blade's top, which hides its foot."""
    bx, by, bz = base
    lb = length * 0.6
    blade = am.box(name, (bx - width / 2, by - lb, bz - thickness / 2), (width, lb, thickness), pivot=base,
                   rotation=(-back, 0, -out), paint=PAINT["shard"])
    tip = am.box(f"{name}_tip", (bx - width / 4, by - length, bz - (thickness - 0.2) / 2),
                 (width / 2, length - lb, thickness - 0.2), pivot=base, rotation=(-back, 0, -out), skip="bottom",
                 paint=PAINT["shard_tip"])
    return [blade, tip]


# ---------------------------------------------------------------- the helmet
def helmet():
    """The helm, 0.75 clear of the head, its top 0.75 above it, closed underneath 0.65 below; the crown's band 0.25
    proud round its top; the mask 0.4 proud of its front; on the mask the brow bar, the nose bar, the cheek plates (each
    leaning out 14 degrees at its top), the teeth and the chin bar; the icicles standing in the band, and the antlers
    rising from its top corners behind, 16 degrees out at their feet and straighter bar by bar."""
    cheek = am.box("cheek_right", (-2.85, -4.5, -5.45), (0.9, 3.4, 0.3), pivot=(-2.4, -1.1, -5.45),
                   rotation=(0, 0, -14), skip="back", paint=PAINT["cheek"])
    teeth = [am.span(f"tooth_{i}_right", (x0, -2.6, -5.4), (x0 + 0.7, -1.6, -5.15), skip="back", paint=PAINT["tooth"])
             for i, x0 in enumerate((-1.9, -0.85))]
    crown = [*icicle("icicle_1_right", (-1.9, -8.95, -4.0), [(1.75, 1.3), (1.5, 0.8), (0.75, 0.4)], lean=6),
             *icicle("icicle_2_right", (-3.6, -8.95, -3.8), [(1.5, 1.1), (1.0, 0.5)], lean=14),
             *icicle("icicle_back_right", (-2.2, -8.95, 3.6), [(1.75, 0.9)], lean=10)]
    horn = antler("antler_right", (-4.2, -8.6, 1.0), [(3.6, 1.8, 16), (3.3, 1.4, 13), (3.0, 1.0, 10), (2.0, 0.6, 7)],
                  back=8)
    return [am.span("helm", (-4.75, -8.75, -4.75), (4.75, 0.65, 4.75), paint=PAINT["helm"]),
            am.span("band", (-5.0, -9.1, -5.0), (5.0, -7.85, 5.0), paint=PAINT["band"]),
            am.span("mask", (-3.7, -7.2, -5.15), (3.7, -0.9, -4.75), skip="back", paint=PAINT["mask"]),
            am.span("brow", (-4.35, -7.7, -5.5), (4.35, -6.6, -5.0), skip="back", paint=PAINT["brow"]),
            am.span("nose", (-0.55, -6.6, -5.45), (0.55, -3.0, -5.15), skip=("top", "back"), paint=PAINT["nose"]),
            *am.pair(cheek), *teeth, *am.mirror_all(teeth),
            am.span("chin", (-1.75, -1.45, -5.5), (1.75, -0.95, -5.15), skip="back", paint=PAINT["chin"]),
            *icicle("icicle_crown", (0.0, -8.95, -4.0), [(2.0, 1.6), (1.75, 1.1), (1.25, 0.6)]),
            *crown, *am.mirror_all(crown), *horn, *am.mirror_all(horn)]


# ---------------------------------------------------------------- the chestplate
STRAP = 25.9   # the straps' slope across the belly, from a side at y 5.6 to the other at y 10.4


def body():
    """The cuirass, 0.95 off the body's sides (0.2 outside the helm's, so the two never share a plane as the head
    turns) and 0.85 off its front and back; the collar's two bars 0.45 proud, meeting at the gem on the breastbone (the
    left bar 0.15 further back, where they cross); the two straps across the belly, the second 0.15 before the first
    where they cross, and the boss through them there, standing on the cuirass; the back plate and the spine down it."""
    collar = am.chevron("collar", (0.0, 4.4, -3.3), 5.3, 1.1, 0.45, angle=50, skip="back", paint=PAINT["collar"])
    collar_left = replace(collar[1], origin=(collar[1].origin[0], collar[1].origin[1], collar[1].origin[2] + 0.15))
    return [am.span("cuirass", (-4.95, -0.65, -2.85), (4.95, 10.5, 2.85), paint=PAINT["cuirass"]),
            collar[0], collar_left,
            am.diamond("gem", (0.0, 4.75, -3.55), 1.6, 0.5, skip="back", paint=PAINT["gem"]),
            am.box("strap_0", (-5.4, 7.4, -3.15), (10.8, 1.2, 0.3), pivot=(0.0, 8.0, -3.15), rotation=(0, 0, STRAP),
                   skip="back", paint=PAINT["strap"]),
            am.box("strap_1", (-5.4, 7.4, -3.3), (10.8, 1.2, 0.3), pivot=(0.0, 8.0, -3.3), rotation=(0, 0, -STRAP),
                   paint=PAINT["strap"]),
            am.span("boss", (-0.8, 7.2, -3.75), (0.8, 8.8, -2.85), skip="back", paint=PAINT["boss"]),
            am.span("back_plate", (-3.6, 0.9, 2.85), (3.6, 9.6, 3.25), skip="front", paint=PAINT["back_plate"]),
            am.span("spine", (-0.55, 1.1, 3.25), (0.55, 9.4, 3.65), skip="front", paint=PAINT["spine"])]


def forearm():
    """The right arm's mail and plate below the shoulder (arm space: x -3..1, y -2..10, z -2..2, the outer side -x):
    the sleeve 0.6 off the upper arm, up inside the pauldron to 0.4 above the shoulder (so none of the arm shows under
    the pauldron as it swings); the vambrace 0.75 off; the cuff 1.45 off, a fin on its outer side swept back and
    up; the gauntlet 0.4 off, closed 0.6 below the hand."""
    return [am.span("sleeve_right", (-3.6, -2.4, -2.6), (1.6, 4.75, 2.6), paint=PAINT["sleeve"]),
            am.span("vambrace_right", (-3.75, 4.5, -2.75), (1.75, 9.0, 2.75), skip="bottom", paint=PAINT["vambrace"]),
            am.span("cuff_right", (-4.45, 7.9, -3.45), (2.45, 9.1, 3.45), paint=PAINT["cuff"]),
            *shard("fin_right", (-4.3, 8.6, 1.0), 2.2, 0.9, out=64, back=48, thickness=0.4),
            am.span("gauntlet_right", (-3.4, 9.1, -2.4), (1.4, 10.6, 2.4), skip="top", paint=PAINT["gauntlet"])]


def arm_right():
    """The right shoulder, a mass of ice: the pauldron 1.6 past the arm's outer side and 1.3 above it, its crown on top;
    four shards out of its top, out and up, the outer ones leaning out further; two lames under it down the outer upper
    arm, hinged out 10 and 14 degrees from their tops. Then the forearm."""
    return [am.span("pauldron_right", (-4.6, -3.3, -3.5), (1.75, 1.4, 3.5), skip="bottom", paint=PAINT["pauldron"]),
            am.span("crown_right", (-4.1, -4.05, -3.0), (1.25, -3.3, 3.0), skip="bottom", paint=PAINT["cap"]),
            *shard("shard_0_right", (-4.2, -3.1, -1.9), 3.8, 1.7, out=26, back=-8),
            *shard("shard_1_right", (-4.2, -3.1, 0.1), 4.6, 1.9, out=32, back=6),
            *shard("shard_2_right", (-4.2, -3.1, 2.0), 3.6, 1.7, out=44, back=20),
            *shard("shard_3_right", (-2.0, -3.8, -0.4), 3.0, 1.5, out=10, back=10),
            am.hinge(am.span("lame_0_right", (-4.0, 1.0, -2.95), (-3.6, 2.9, 2.95), paint=PAINT["lame"]), "top", 10),
            am.hinge(am.span("lame_1_right", (-4.3, 2.5, -3.1), (-3.9, 4.4, 3.1), paint=PAINT["lame"]), "top", 14),
            *forearm()]


def arm_left():
    """The left shoulder, a layered block (arm space: x -1..3, the outer side +x): the pauldron 1.7 past the arm's
    outer side, a cap on it and a narrower cap on that, the rim round its foot 0.2 proud of it, and one shard out of its
    top at the outer edge. Then the forearm, the right one's mirror image."""
    spike = shard("spike_right", (-4.0, -3.85, 0.0), 3.8, 1.7, out=20, back=8)
    return [am.span("pauldron_left", (-1.75, -3.3, -3.45), (4.7, 1.4, 3.45), skip="bottom", paint=PAINT["pauldron"]),
            am.span("cap_left", (-1.25, -4.05, -2.8), (4.2, -3.3, 2.8), skip="bottom", paint=PAINT["cap"]),
            am.span("cap_top_left", (-0.75, -4.8, -2.3), (3.7, -4.05, 2.3), skip="bottom", paint=PAINT["cap"]),
            am.span("rim_left", (-1.95, 0.55, -3.65), (4.9, 1.8, 3.65), paint=PAINT["rim"]),
            *am.mirror_all(spike), *am.mirror_all(forearm())]


# ---------------------------------------------------------------- the leggings and boots
LEFT_OUT = 0.12   # the left leg's parts this much larger, so where the legs meet their faces never share a plane


def waist():
    """The belt, 0.6 off the body's front and back; the buckle's frame 1.15 off its front (clear of vanilla leggings'
    1.0 shell) and its gem 0.3 proud of that."""
    return [am.span("belt", (-4.45, 10.25, -2.6), (4.45, 12.65, 2.6), paint=PAINT["belt"]),
            am.span("buckle", (-1.6, 10.45, -3.15), (1.6, 12.45, -2.6), skip="back", paint=PAINT["buckle"]),
            am.span("buckle_gem", (-0.85, 10.95, -3.45), (0.85, 11.95, -3.15), skip="back", paint=PAINT["buckle_gem"])]


def leg():
    """The right leg (leg space: x -2..2, y 0..12, z -2..2): the banded cuisse round the thigh, 0.77 off it, from up
    under the belt to the knee; two tassets over its front, 1.15 to 1.75 off (clear of vanilla leggings' 1.0 shell),
    hinged out from their tops, stopping short of the centre line; the knee band 1.35 off, and the knee cop on it."""
    return [am.span("cuisse_right", (-2.77, -0.45, -2.77), (2.77, 5.6, 2.77), paint=PAINT["cuisse"]),
            am.hinge(am.span("tasset_0_right", (-2.45, -0.3, -3.45), (1.6, 2.9, -3.15), paint=PAINT["tasset"]),
                     "top", 6),
            am.hinge(am.span("tasset_1_right", (-2.6, 2.55, -3.75), (1.75, 5.4, -3.45), paint=PAINT["tasset"]),
                     "top", 10),
            am.span("knee_band_right", (-3.35, 5.6, -3.35), (3.35, 6.75, 3.35), paint=PAINT["knee_band"]),
            am.diamond("knee_right", (-0.2, 6.2, -3.55), 2.2, 0.6, pitch=12, paint=PAINT["knee"])]


def boot():
    """Three rings down the shin, 1.05 off the leg and the middle one sunk to 0.8 between them, the top one reaching up
    inside the leggings' knee band; the sabaton, 0.9 off its
    sides (0.15 inside the rings) and 0.75 behind, closed 0.65 below the foot; the pointed toe."""
    return [am.span("ring_0_right", (-3.05, 6.6, -3.05), (3.05, 8.15, 3.05), paint=PAINT["ring"]),
            am.span("ring_1_right", (-2.8, 8.15, -2.8), (2.8, 9.35, 2.8), skip=("top", "bottom"),
                    paint=PAINT["ring_sunk"]),
            am.span("ring_2_right", (-3.05, 9.35, -3.05), (3.05, 10.55, 3.05), paint=PAINT["ring"]),
            am.span("sabaton_right", (-2.9, 10.55, -3.6), (2.9, 12.65, 2.75), paint=PAINT["sabaton"]),
            am.span("toe_right", (-1.9, 11.0, -4.35), (1.9, 12.4, -3.6), skip="back", paint=PAINT["toe"]),
            am.span("toe_tip_right", (-0.9, 11.4, -4.95), (0.9, 12.25, -4.35), skip="back", paint=PAINT["toe"])]


def left(parts):
    return [replace(p, inflate=LEFT_OUT) for p in am.mirror_all(parts)]


def model():
    l, b = leg(), boot()
    return {"wight_king_helmet": {"head": helmet()},
            "wight_king_chestplate": {"body": body(), "right_arm": arm_right(), "left_arm": arm_left()},
            "wight_king_leggings": {"body": waist(), "right_leg": l, "left_leg": left(l)},
            "wight_king_boots": {"right_leg": b, "left_leg": left(b)}}


SETS = [am.ArmorSet("wight_king", WIGHT_KING, model())]
