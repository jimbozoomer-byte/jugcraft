"""Frost Knight: the owner's white-and-ice knight (one of the two designs they sent later on 8 October 2026, "Just made
these ones aswell want them done weapons too please": two Blockbench renders, from the front and from behind on the
left) as a 3D worn model for jugcraft:frost_knight_* (helmet, chestplate, leggings, boots), on the toolkit in
tools/armor_models.py (shapes) and tools/armor_paint.py (the atlas). Its ice sword is an arm of its own
(tools/arms_variants.py).

What the owner drew, and where it is here (built part for part, as the owner asked: "complex models ... really
capturing the crazy unique geometry of each armor"; reworked twice at their word: first "The frost night looks bad
because of the head part and how you gave it like a mouth ... the pants are not detailed enough", then "I don't like
how robotic and square the helmet you made is or how you are missing so many details on the shoulder and stuff can you
really detail the frost knight accurately"):
    helmet      a frosted white great helm wrapped in a mane of white frost feathers, layered along its sides and back
                and standing up off its crown; a band round its brow set with ice, a white gable rising from it to a
                point; a dark eye slit across the face turning up at its ends, crossed by a white ridge running from the
                crown down to the chin; under the slit a white cheek band, then a grille of dark slots between white
                bars, then the chin; a crown of ice crystals, one tall on the gable's point, one at each of its feet and
                a shard leaning out on the left; an ice cross on the back. Here: the helm and the crown band; the brow
                plate (its ends shorter, so the slit turns up), the cheek band and the chin plate raised on its face,
                four vent bars over the dark slots, the ridge standing proudest; the gable's two bars; the crystals,
                each stacked prisms turned on their edge and tapering; the cross's two bars; and the mane: 39 cut-out
                feathers, three rows lying along each side lifted out and swept back, three fins standing out of each
                side, three framing each side of the face, five lifted off the back and two on the crown each side
    chestplate  a white cuirass with raised chest plates, a navy strap from the left shoulder to the right hip front and
                back; on the left shoulder a navy pauldron with white key spirals and a white rim in two steps; on the
                right a rounded mass of white frost with feathers hanging from it down the outer arm past the hand,
                standing out of its top and hanging before and behind it, and a feathered frost drape behind that
                shoulder; the forearms bare. Here: the cuirass, the two chest plates and a plate below them; the strap
                before and behind; on the left arm the pauldron, its dome, the spiral raised on its outer side in four
                bars and painted on its front and back, the rim's two steps; on the right the frost in three rounded
                steps and its 15 feathers; on the body the drape, hinged out behind the right shoulder, five feathers
                riding it
    leggings    white plated thighs in stacked lames, a navy belt with an ice gem at its buckle, a navy flap over the
                left hip with an ice gem, banded knees. Here: the belt and the gem; on each leg the cuisse; three lames
                stepping down it, each a plate over the front and one over the outer side, hinged out from their tops;
                a plate behind the thigh; the knee cop, its band and a wing on its outer side; on the left the hip flap,
                hinged out, its gem riding it
    boots       chunky white boots. Here: the cuff, the greave, two small frost feathers at the outer ankle, the
                sabaton and the toe cap
Colours: armor_paint.FROST_KNIGHT; the crown's ice in its own five tones.

The frost's feathers are cut-out blades, one face each: worn armor is drawn from both sides, so a blade needs no back,
and of its two faces the one turned upward is drawn, so the game's lights catch it as they would its upper side. The
renders show the front and the left side from behind; the right side is drawn from the front view. The forearms are
bare, as drawn (the owner's renders show the plain pale blue of their model there). Every box is closed: a face is left
out only where another box of the same piece and bone covers it (the reliefs' backs, the crystals' feet, the frost's
steps' feet, the spiral's and the hip gem's inner sides).

Space (tools/armor_models.py): each bone's own space in model pixels; x is the model's LEFT, y is DOWN, z is the BACK
(the face is at -z).
Preview: python3 tools/armor_preview.py --set frost_knight
"""
from dataclasses import replace

import armor_models as am
from armor_paint import FROST_KNIGHT, P


def rows(*tones):
    """One tone per texel row, cycling, each row broken into runs of the next tone darker."""
    return P("lames", rows=1, tones=tuple((t, t) for t in tones), centre=None)


def solid(tone):
    return P("solid", tone=tone)


def marks(*rects, symmetric=False):
    return P("marks", rects=list(rects), symmetric=symmetric)


def plate(tone, strips="h"):
    return P("plate", tone=tone, strips=strips)


H, L, M, D, S, V = "light", "mid_light", "mid", "dark", "seam", "void"       # the frosted white, white to slate
G, g = "gold_light", "gold_dark"                                            # the ice's palest and bright cyans
C, c, q, Q, K = "leather_light", "leather_mid_light", "leather_mid", "leather_dark", "leather_darkest"   # the navy
U, u, x, X = "under_light", "under_mid", "under_dark", "under_darkest"       # the eye slit's and vents' black
I0, I1, I2, I3, I4 = "ice_light", "ice", "ice_mid", "ice_dark", "ice_deep"   # the crown's ice

FROST = plate(L)   # the frosted white: its mottling the plate's runs a tone lighter or darker

TONE = {"H": H, "L": L, "M": M, "D": D, "S": S, "G": G, "g": g, "C": C, "c": c, "q": q, "Q": Q, "K": K,
        ".": "clear"}


def pixmap(*rows):
    """A face painted texel for texel, one string per texel row and one letter per texel (TONE names them; "." is
    see-through, for a cut-out part; "_" leaves the texel as the layers under it painted it)."""
    return marks(*[(x, y, 1, 1, TONE[ch]) for y, row in enumerate(rows) for x, ch in enumerate(row) if ch != "_"])


# The frost's feathers: cut-out blades of white frost, tip first (row 0), serrated, a little grey toward the foot.
# Worn armor is drawn from both sides, so each is one face.
FEATHERS = {
    "blade": (".H.", "HH.", "HLL", ".LL", "HLM"),
    "plume": (".H.", ".HH", "HHL", "HL.", "HLL", "LLM", ".M."),
    "tuft": ("H..H", "HH.L", "HLHL", "LLLL", ".LM."),
    "fan": ("H.H..", "HHL.H", "HLLHL", "LLLLL", "LMLLM", ".LMM."),
    "wisp": ("H.", "HL", "HL", "LM"),
}


# ---------------------------------------------------------------- paint
PAINT = {
    "helm": {"top": [FROST, marks((0, 0, 99, 1, H))], "bottom": solid(S),
             "front": [FROST, marks((1, 2, 99, 2, X), (1, 5, 99, 3, x), (-1, 2, 1, 6, L), (0, -1, 99, 1, D))],
             "*": [FROST, marks((0, -1, 99, 1, D))]},
    # the crown band, white with ice set in it every few texels
    "band": {"top": solid(H), "bottom": solid(D),
             "*": [solid(H), marks((0, -1, 99, 1, M), (1, 0, 1, 1, g), (4, 0, 1, 1, g), (7, 0, 1, 1, g),
                                   (1, 1, 1, 1, G), (4, 1, 1, 1, G), (7, 1, 1, 1, G))]},
    # the face's reliefs: the brow plate, the cheek band and the chin plate frosted white, the vent bars and the ridge
    # lit along one edge
    "visor": {"front": [FROST, marks((0, 0, 99, 1, H), (0, -1, 99, 1, M))], "top": solid(H), "bottom": solid(D),
              "*": solid(L)},
    "vent": {"front": [solid(H), marks((-1, 0, 1, 99, M))], "top": solid(H), "bottom": solid(D), "*": solid(L)},
    "ridge": {"front": [solid(H), marks((0, 0, 1, 99, H), (-1, 0, 1, 99, L))], "top": solid(H), "bottom": solid(M),
              "*": solid(L)},
    # the gable over the brow, white, lit along its top; the ice cross behind the helm
    "gable": {"front": [solid(H), marks((0, -1, 99, 1, L))], "top": solid(H), "bottom": solid(M), "*": solid(L)},
    "cross": {"back": [rows(I1, I0), marks((0, 0, 99, 1, I0))], "*": solid(I2)},
    # the ice: each prism lit on its upper faces, deep at its foot
    "ice": {"top": solid(I0), "bottom": solid(I3), "front": [rows(I1, I0), marks((0, 0, 1, 99, I0))],
            "right": [rows(I0, I1), marks((-1, 0, 1, 99, I2))], "left": [rows(I1, I2), marks((0, 0, 1, 99, I0))],
            "back": rows(I2, I3)},
    "ice_tip": {"top": solid(I0), "bottom": solid(I2), "front": solid(I0), "right": solid(I0), "left": solid(I1),
                "back": solid(I1)},
    "cuirass": {"top": [FROST, marks((0, 0, 99, 1, H))], "bottom": solid(S), "*": FROST},
    "pec": {"front": [plate(H), marks((0, 0, 99, 1, H), (0, -1, 99, 1, M))], "top": solid(H), "*": solid(L)},
    "abdomen": {"front": rows(H, L, M), "*": solid(L)},
    "strap": {"front": [solid(q), marks((0, 0, 99, 1, c))], "back": [solid(q), marks((0, 0, 99, 1, c))],
              "*": solid(Q)},
    # the left pauldron: navy, a white curl on its outer side, its trim white
    "pauldron": {"top": [solid(q), marks((0, 0, 99, 1, c))], "bottom": solid(K),
                 "front": [solid(q), marks((0, 0, 99, 1, c)),
                           pixmap("_______", "__HHHH_", "__H__H_", "__H_HH_", "__H____")],
                 "back": [solid(q), marks((0, 0, 99, 1, c)),
                          pixmap("_______", "_HHHH__", "_H__H__", "_HH_H__", "____H__")],
                 "*": [solid(q), marks((0, 0, 99, 1, c), (0, -1, 99, 1, Q))]},
    "dome": {"top": [solid(c), marks((0, -1, 99, 1, q))], "*": [solid(q), marks((0, 0, 99, 1, C))]},
    "trim": {"top": solid(H), "bottom": solid(M), "*": [solid(H), marks((0, -1, 99, 1, L))]},
    "trim_low": {"top": solid(L), "bottom": solid(D), "*": [solid(L), marks((0, 0, 99, 1, H), (0, -1, 99, 1, M))]},
    "spiral": {"top": solid(H), "bottom": solid(L), "*": [solid(H), marks((0, -1, 99, 1, L))]},
    # the right shoulder's frost and the mantle: white in runs, greyer below
    "fur": {"top": [plate(H), marks((0, 0, 99, 1, H))], "bottom": solid(D), "*": [plate(L, "v"), marks((0, -1, 99, 1, M))]},
    "mantle": {"top": solid(H), "bottom": solid(M), "*": [plate(L, "v"), marks((0, 0, 99, 1, H), (0, -1, 99, 1, D))]},
    # the legs: the navy belt, the ice gem, the white cuisses and lames, the knee cop
    "belt": {"top": solid(q), "bottom": solid(K), "sides": [solid(q), marks((0, 0, 99, 1, c), (0, -1, 99, 1, Q))]},
    "hip_flap": {"top": solid(c), "bottom": solid(K),
                 "*": [solid(q), marks((0, 0, 99, 1, c), (0, -1, 99, 1, Q), (1, 2, 1, 2, Q), (-2, 2, 1, 2, Q))]},
    "gem": {"front": [solid(I1), marks((0, 0, 1, 1, I0), (-1, -1, 1, 1, I3))], "top": solid(I0), "*": solid(I2)},
    "cuisse": {"top": solid(H), "bottom": solid(S), "*": [FROST, marks((0, 0, 99, 1, H))]},
    "lame": {"top": solid(H), "bottom": solid(D), "*": [rows(H, L), marks((0, -1, 99, 1, M))]},
    "lame_side": {"top": solid(H), "bottom": solid(D), "*": [rows(L, M), marks((0, 0, 99, 1, H), (0, -1, 99, 1, D))]},
    "thigh_back": {"top": solid(H), "bottom": solid(D), "*": [FROST, marks((0, 0, 99, 1, H), (0, -1, 99, 1, M))]},
    "knee_band": {"top": solid(H), "bottom": solid(D), "*": [solid(H), marks((0, -1, 99, 1, M), (2, 0, 1, 1, g),
                                                                              (-3, 0, 1, 1, g))]},
    "wing": {"top": solid(H), "*": [solid(L), marks((0, 0, 1, 99, H), (0, -1, 99, 1, M))]},
    "knee": P("chevron", corner="o", bands=(H, L), border=None, core=(M, D), outside=H),
    "cuff": {"top": solid(H), "bottom": solid(M), "*": [solid(H), marks((0, -1, 99, 1, M))]},
    "greave": {"top": solid(H), "*": [FROST, marks((0, -1, 99, 1, M))]},
    "sabaton": {"top": solid(L), "bottom": solid(S), "*": [FROST, marks((0, 0, 99, 1, H), (0, -1, 99, 1, D))]},
    "toe": {"top": solid(H), "bottom": solid(D), "*": [solid(H), marks((0, -1, 99, 1, L))]},
}


# ---------------------------------------------------------------- shape helpers
def crystal(name, base, steps, lean=0.0):
    """An ice crystal standing on `base`: prisms stacked up from it, (height, width) each, every one turned 45 degrees
    about the upright so its edge faces front, the last one the tip; the whole leaning `lean` degrees out toward the
    model's right (-x). Build it on the right and mirror it for the left."""
    bx, by, bz = base
    out, y = [], by
    for i, (h, w) in enumerate(steps):
        tip = i == len(steps) - 1
        part = am.box(f"{name}_{i}", (bx - w / 2, y - h, bz - w / 2), (w, h, w), pivot=(bx, y, bz),
                      rotation=(0, 45, 0), skip="bottom" if i else (), paint=PAINT["ice_tip" if tip else "ice"])
        out.append(am.turned(part, "z", -lean, base) if lean else part)
        y -= h
    return out


def feather(name, root, art, yaw=0.0, lift=0.0, sweep=0.0):
    """A frost feather: a flat cut-out blade drawn from FEATHERS[art] (one texel a pixel), standing on `root` (the
    middle of its foot), its face to the front and its tip up; swept `sweep` degrees in its own plane (toward the
    model's left for positive), lifted `lift` degrees toward its face, then turned `yaw` degrees about the upright (90:
    its face to the model's right, 180: behind). Build it on the model's right and mirror it for the left. Worn armor
    draws a face from both sides but lights it by its own normal, so of the blade's two faces the one turned upward is
    drawn: a feather lifted off the helm is lit as its upper side, white, not as its underside."""
    rows = FEATHERS[art]
    w, h = len(rows[0]), len(rows)
    rx, ry, rz = root
    part = am.box(name, (rx - w / 2, ry - h, rz - 0.05), (w, h, 0.1), pivot=root, rotation=(0, 0, sweep),
                  cutout=True, paint={"front": pixmap(*rows), "back": pixmap(*rows), "*": solid(L)})
    if lift:   # about the blade's width, so the tip tips toward the face however it was swept
        across = am._apply(am.axis_matrix("z", sweep), (1.0, 0.0, 0.0))
        part = am.turned(part, across, lift, root)
    if yaw:
        part = am.turned(part, "y", yaw, root)
    r, _ = am.transform(part)
    shown = "front" if am._apply(r, (0.0, 0.0, -1.0))[1] <= 0 else "back"   # y is down: the face turned up
    hidden = [f for f in ("top", "bottom", "right", "left", "front", "back") if f != shown]
    return replace(part, skip=am.faces_of(hidden))


def riding(part, on):
    """`part` given `on`'s turns as well as its own placement: it rides a hinged plate (a feather on the drape, the gem
    on the hip flap)."""
    return replace(part, turns=part.turns + on.turns)


# ---------------------------------------------------------------- the helmet
def bar(name, p0, p1, thick, z, **options):
    """A flat bar `thick` across from (x, y) point p0 to p1 (bone space), between z = z[0] and z[1]."""
    import math
    (x0, y0), (x1, y1) = p0, p1
    length = math.hypot(x1 - x0, y1 - y0)
    return am.box(name, (x0, y0 - thick / 2, z[0]), (length, thick, z[1] - z[0]), pivot=(x0, y0, 0.0),
                  rotation=(0, 0, math.degrees(math.atan2(y1 - y0, x1 - x0))), **options)


# The mane of frost round the helm, the model's right half (mirrored for the left): (name, root, feather, yaw, lift,
# sweep); each root is a little inside the helm, so a feather grows out of it. On each side three rows of feathers
# lie along the helm, lifted out and swept back, and three fins stand out from it, seen against the sky from the
# front; three more at its front corners frame the face; behind, feathers lifted back off the helm and fanned out, and
# two on its crown.
MANE = [("mane_0", (-4.45, -6.5, -2.75), "tuft", 90, 26, -12), ("mane_1", (-4.45, -6.25, -0.25), "fan", 90, 32, -28),
        ("mane_2", (-4.45, -6.0, 2.5), "fan", 90, 38, -44), ("mane_3", (-4.45, -3.25, -1.75), "blade", 90, 22, -22),
        ("mane_4", (-4.45, -3.0, 1.0), "tuft", 90, 28, -36), ("mane_5", (-4.45, -2.75, 3.5), "plume", 90, 34, -52),
        ("mane_6", (-4.45, 0.25, 0.25), "blade", 90, 19, -40), ("mane_7", (-4.45, 0.25, 2.75), "tuft", 90, 24, -56),
        ("fin_0", (-3.9, -7.75, -1.0), "tuft", 10, -6, -34), ("fin_1", (-4.0, -5.0, 0.5), "fan", 14, -10, -46),
        ("fin_2", (-4.0, -2.25, 1.75), "tuft", 18, -14, -58),
        ("nape_0", (-3.0, -8.25, 4.45), "fan", 180, 34, 14), ("nape_1", (-3.25, -5.5, 4.45), "tuft", 180, 26, 24),
        ("nape_2", (-3.5, -2.0, 4.45), "blade", 180, 21, 30),
        ("crest_0", (-0.6, -8.45, 1.0), "tuft", 90, 8, -18), ("crest_1", (-0.6, -8.45, 3.0), "tuft", 90, 12, -40),
        ("frame_0", (-4.35, -7.25, -4.1), "blade", 6, -4, -32), ("frame_1", (-4.4, -3.5, -4.1), "tuft", 10, -8, -52),
        ("frame_2", (-4.1, -8.55, -3.0), "wisp", 24, 6, -22)]


def helmet():
    """The helm, 0.75 clear of the head, its top 0.75 above it, closed underneath 0.65 below; the crown band 0.2 proud
    round it; on its face, 0.35 proud, the brow plate over the eye slit (its ends shorter, so the slit turns up at its
    ends as drawn), the cheek band under it and the chin plate, and between the cheek band and the chin four vent bars
    0.3 proud over the dark breathing slits; the ridge down its middle from the crown band to the vents, 0.55 proud,
    crossing the slit as the nose bar; the gable over the brow, two white bars rising from the crown band's corners to
    a point, the left one 0.15 prouder where they cross; the crystals: the tall one on the gable's point, one at each
    of its feet and a shard leaning out on the left; the mane of frost (MANE); behind, the ice cross, two bars 0.25 and
    0.4 proud."""
    vents = [am.span(f"vent_{i}_right", (x0, -3.75, -5.05), (x0 + 0.7, -1.6, -4.75), skip=("back", "top", "bottom"),
                     paint=PAINT["vent"]) for i, x0 in enumerate((-3.45, -2.05))]
    gable = bar("gable_right", (-4.3, -8.25), (0.35, -10.6), 0.8, (-5.2, -4.9), skip="back", paint=PAINT["gable"])
    gable_left = am.mirror(gable)
    gable_left = replace(gable_left, origin=(gable_left.origin[0], gable_left.origin[1], gable_left.origin[2] - 0.15))
    side = [*crystal("crystal_side_right", (-3.4, -8.5, -3.9), [(2.75, 1.3), (1.75, 0.7)], lean=6),
            *[feather(n, r, a, yaw, lift, sweep) for n, r, a, yaw, lift, sweep in MANE]]
    return [am.span("helm", (-4.75, -8.75, -4.75), (4.75, 0.65, 4.75), paint=PAINT["helm"]),
            am.span("band", (-4.95, -8.25, -4.95), (4.95, -6.75, 4.95), paint=PAINT["band"]),
            am.span("brow", (-2.85, -6.75, -5.1), (2.85, -6.0, -4.75), skip="back", paint=PAINT["visor"]),
            *am.pair(am.span("brow_end_right", (-4.25, -6.75, -5.1), (-2.85, -6.5, -4.75), skip="back",
                             paint=PAINT["visor"])),
            am.span("cheek", (-4.25, -4.75, -5.1), (4.25, -3.75, -4.75), skip="back", paint=PAINT["visor"]),
            *vents, *am.mirror_all(vents),
            am.span("chin", (-4.25, -1.6, -5.1), (4.25, -0.35, -4.75), skip="back", paint=PAINT["visor"]),
            am.span("ridge", (-0.45, -8.25, -5.3), (0.45, -1.6, -4.95), skip="back", paint=PAINT["ridge"]),
            gable, gable_left,
            *crystal("crystal_crown", (0.0, -8.5, -4.25), [(4.25, 1.8), (2.5, 1.2), (2.25, 0.6)]),
            *am.mirror_all(crystal("crystal_shard_right", (-2.0, -8.6, -2.75), [(2.4, 0.8)], lean=35)),
            *side, *am.mirror_all(side),
            feather("nape_middle", (0.0, -8.5, 4.45), "plume", 180, 46, 0),
            bar("cross_a", (-1.6, -5.0), (1.6, -0.9), 0.7, (4.75, 5.0), skip="front", paint=PAINT["cross"]),
            bar("cross_b", (1.6, -5.0), (-1.6, -0.9), 0.7, (4.75, 5.15), skip="front", paint=PAINT["cross"])]


# ---------------------------------------------------------------- the chestplate
STRAP = 52.7   # the strap's slope, from the left shoulder (x 4, y 0) to the right hip (x -4, y 10.5)


def body():
    """The cuirass, 0.95 off the body's sides (0.2 outside the helm's, so the two never share a plane as the head
    turns) and 0.85 off its front and back; the chest plates 0.5 proud of it and the plate below them 0.35; the strap
    0.65 proud before (0.15 off the chest plates) and behind; behind the right shoulder the frost's drape, a white
    plate hinged out 6 degrees at its top with feathers riding it, out from its outer edge and down from its foot."""
    strap = am.box("strap_front", (-7.0, 4.55, -3.5), (14.0, 1.4, 0.3), pivot=(0.0, 5.25, -3.5),
                   rotation=(0, 0, -STRAP), paint=PAINT["strap"])
    strap_back = am.box("strap_back", (-7.0, 4.55, 3.2), (14.0, 1.4, 0.3), pivot=(0.0, 5.25, 3.5),
                        rotation=(0, 0, -STRAP), paint=PAINT["strap"])
    pec = am.span("pec_right", (-4.0, 0.4, -3.35), (-0.3, 4.4, -2.85), skip="back", paint=PAINT["pec"])
    mantle = am.hinge(am.span("mantle", (-4.6, -0.4, 3.25), (-0.8, 7.25, 3.7), paint=PAINT["mantle"]), "top", 6)
    drape = [feather("drape_0", (-4.3, 0.75, 3.6), "fan", 180, 18, 64),
             feather("drape_1", (-4.3, 3.25, 3.6), "tuft", 180, 22, 98),
             feather("drape_2", (-4.3, 5.75, 3.6), "plume", 180, 20, 128),
             feather("drape_3", (-3.6, 7.0, 3.6), "fan", 180, 16, 160),
             feather("drape_4", (-1.75, 7.0, 3.6), "plume", 180, 20, 184)]
    return [am.span("cuirass", (-4.95, -0.65, -2.85), (4.95, 10.5, 2.85), paint=PAINT["cuirass"]),
            *am.pair(pec),
            am.span("abdomen", (-2.75, 4.75, -3.2), (2.75, 9.25, -2.85), skip="back", paint=PAINT["abdomen"]),
            strap, strap_back, mantle, *[riding(f, mantle) for f in drape]]


# The frost on the right shoulder and down the outer arm, as (name, root, feather, yaw, lift, sweep): three rows of
# feathers hanging from it down the outer arm, lifted out and fanned, the lowest reaching past the hand; fins standing
# up out of its top; feathers hanging before and behind it. Roots are inside the frost or 0.55 off the arm.
WING = [("wing_0", (-4.2, -0.5, -2.25), "fan", 90, 18, 168), ("wing_1", (-4.2, -0.75, 0.0), "tuft", 90, 24, 180),
        ("wing_2", (-4.2, -0.5, 2.25), "fan", 90, 18, 192), ("wing_3", (-3.55, 1.75, -1.6), "plume", 90, 22, 174),
        ("wing_4", (-3.55, 1.75, 0.2), "plume", 90, 28, 186), ("wing_5", (-3.55, 1.75, 1.9), "blade", 90, 24, 196),
        ("wing_6", (-3.55, 4.75, -0.9), "plume", 90, 16, 178), ("wing_7", (-3.55, 4.75, 1.1), "fan", 90, 20, 190),
        ("frost_fin_0", (-2.0, -3.75, -1.25), "fan", 8, -10, -28),
        ("frost_fin_1", (-2.5, -3.25, 1.25), "tuft", -12, 12, -38),
        ("frost_fin_2", (-0.5, -4.0, 0.25), "plume", 90, 10, -24),
        ("frost_front_0", (-3.0, -1.0, -3.2), "tuft", 0, 18, 205),
        ("frost_front_1", (-0.75, -1.25, -3.2), "blade", 0, 14, 172),
        ("frost_back_0", (-3.0, -1.0, 3.2), "tuft", 180, 18, 155),
        ("frost_back_1", (-0.75, -1.25, 3.2), "blade", 180, 14, 188)]


def arm_right():
    """The right arm (arm space: x -3..1, y -2..10, z -2..2): the frost over the shoulder, a white mass rounded in three
    steps, 1.5 past the arm's outer side; its feathers (WING). The forearm is bare."""
    return [am.span("frost_right", (-4.5, -2.5, -3.5), (1.6, 2.0, 3.5), paint=PAINT["fur"]),
            am.span("frost_mid_right", (-4.0, -3.3, -3.0), (1.1, -2.5, 3.0), skip="bottom", paint=PAINT["fur"]),
            am.span("frost_top_right", (-3.2, -4.45, -2.3), (0.4, -3.2, 2.3), skip="bottom", paint=PAINT["fur"]),
            *[feather(n, r, a, yaw, lift, sweep) for n, r, a, yaw, lift, sweep in WING]]


def arm_left():
    """The left arm (arm space: x -1..3, y -2..10, z -2..2, the outer side +x): the navy pauldron, 1.6 past the arm's
    outer side and 1.25 above it, its dome, a white key spiral raised 0.25 on its outer side (four bars, each 0.15 off
    the next), and its white rim in two steps under it, the lower one narrower. The forearm is bare."""
    spiral = [am.span("spiral_top_left", (4.6, -2.6, -2.2), (4.85, -2.1, 1.8), skip="right", paint=PAINT["spiral"]),
              am.span("spiral_side_left", (4.6, -2.1, 1.3), (5.0, 0.25, 1.8), skip="right", paint=PAINT["spiral"]),
              am.span("spiral_foot_left", (4.6, -0.25, -1.2), (4.85, 0.25, 1.3), skip="right", paint=PAINT["spiral"]),
              am.span("spiral_hook_left", (4.6, -1.55, -1.2), (5.0, -0.25, -0.7), skip="right", paint=PAINT["spiral"])]
    return [am.span("pauldron_left", (-1.7, -3.25, -3.15), (4.6, 1.25, 3.15), skip="bottom", paint=PAINT["pauldron"]),
            am.span("dome_left", (-1.2, -4.0, -2.65), (4.1, -3.25, 2.65), skip="bottom", paint=PAINT["dome"]),
            *spiral,
            am.span("trim_left", (-1.9, 1.0, -3.55), (4.8, 2.25, 3.55), paint=PAINT["trim"]),
            am.span("trim_low_left", (-1.45, 2.25, -3.05), (4.35, 2.55, 3.05), paint=PAINT["trim_low"])]


# ---------------------------------------------------------------- the leggings and boots
LEFT_OUT = 0.12   # the left leg's parts this much larger, so where the legs meet their faces never share a plane


def waist():
    """The navy belt, 0.6 off the body's front and back; the ice gem 1.15 off its front (clear of vanilla leggings'
    1.0 shell)."""
    return [am.span("belt", (-4.45, 10.25, -2.6), (4.45, 12.65, 2.6), paint=PAINT["belt"]),
            am.span("gem", (-1.25, 10.75, -3.15), (1.25, 12.25, -2.6), skip="back", paint=PAINT["gem"])]


def leg():
    """The right leg (leg space: x -2..2, y 0..12, z -2..2): the cuisse round the thigh, 0.77 off its front, back and
    inner side and 1.15 off its outer side, from up under the belt to the knee; three lames stepping down it, each a
    plate over the front, 1.15 and more off (clear of vanilla leggings' 1.0 shell) and each lower one 0.2 further out,
    and one over the outer side, 0.15 off the cuisse and each lower one 0.15 further out, hinged out from their tops; a
    plate behind the thigh; the knee cop, the band round the knee under it and a wing on the knee's outer side."""
    lames = []
    for i in range(3):   # each lower lame 0.2 further out, and its edges 0.15 in from the one above's
        y0, out, side, inset = 0.25 + 2.0 * i, 0.2 * i, 0.15 * i, 0.15 * i
        lames += [am.hinge(am.span(f"lame_{i}_right", (-2.45 - out, y0, -3.4 - out), (1.65 - inset, y0 + 2.4, -3.15 - out),
                                   paint=PAINT["lame"]), "top", 5 + 2 * i),
                  am.hinge(am.span(f"lame_side_{i}_right", (-3.55 - side, y0, -2.55 + inset), (-3.3 - side, y0 + 2.4, 1.85 - inset),
                                   paint=PAINT["lame_side"]), "top", 3 + 1.5 * i)]
    return [am.span("cuisse_right", (-3.15, -0.45, -2.77), (2.77, 6.75, 2.77), paint=PAINT["cuisse"]),
            *lames,
            am.hinge(am.span("thigh_back_right", (-2.45, 0.25, 3.15), (1.65, 5.0, 3.4), paint=PAINT["thigh_back"]),
                     "top", 4),
            am.span("knee_band_right", (-3.3, 5.75, -3.15), (3.15, 6.5, 3.15), paint=PAINT["knee_band"]),
            am.diamond("knee_right", (-0.3, 6.35, -3.45), 2.4, 0.6, pitch=12, paint=PAINT["knee"]),
            am.hinge(am.span("wing_right", (-3.65, 5.2, -1.6), (-3.4, 7.2, 1.2), paint=PAINT["wing"]), "top", 10)]


def boot():
    """The cuff at the knee, 1.0 off the leg; the greave below it, 0.75 off (clear of vanilla leggings' 0.5 shell); two
    small frost feathers out of its outer side at the ankle; the sabaton, 0.95 off and 1.05 behind, closed 0.65 below
    the foot; the toe cap."""
    return [am.span("cuff_right", (-3.0, 6.75, -3.0), (3.0, 7.75, 3.0), paint=PAINT["cuff"]),
            am.span("greave_right", (-2.75, 7.75, -2.75), (2.75, 11.0, 2.75), skip=("top", "bottom"),
                    paint=PAINT["greave"]),
            feather("ankle_frost_0_right", (-2.45, 9.5, -0.25), "tuft", 90, 34, -30),
            feather("ankle_frost_1_right", (-2.45, 10.5, 1.5), "wisp", 90, 40, -52),
            am.span("sabaton_right", (-2.95, 11.0, -3.6), (2.95, 12.65, 3.05), paint=PAINT["sabaton"]),
            am.span("toe_right", (-2.2, 11.3, -3.95), (2.2, 12.35, -3.6), skip="back", paint=PAINT["toe"])]


def left(parts):
    return [replace(p, inflate=LEFT_OUT) for p in am.mirror_all(parts)]


def hip_flap():
    """The navy flap hanging from the belt over the left hip (built on the right leg and mirrored): a plate outside the
    side lames, hinged out 9 degrees at its top, and the ice gem set at its top, riding it."""
    flap = am.hinge(am.span("hip_flap_right", (-4.3, -0.25, -1.9), (-4.0, 4.5, 1.5), paint=PAINT["hip_flap"]), "top", 9)
    gem = am.span("hip_gem_right", (-4.55, 0.35, -0.5), (-4.3, 1.35, 0.5), skip="left", paint=PAINT["gem"])
    return am.mirror_all([flap, riding(gem, flap)])


def model():
    l, b = leg(), boot()
    return {"frost_knight_helmet": {"head": helmet()},
            "frost_knight_chestplate": {"body": body(), "right_arm": arm_right(), "left_arm": arm_left()},
            "frost_knight_leggings": {"body": waist(), "right_leg": l,
                                      "left_leg": left(l) + [replace(p, inflate=LEFT_OUT) for p in hip_flap()]},
            "frost_knight_boots": {"right_leg": b, "left_leg": left(b)}}


SETS = [am.ArmorSet("frost_knight", FROST_KNIGHT, model())]
