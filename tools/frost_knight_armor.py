"""Frost Knight: the owner's white-and-ice knight (one of the two designs they sent later on 8 October 2026, "Just made
these ones aswell want them done weapons too please": two Blockbench renders, from the front and from behind on the
left) as a 3D worn model for jugcraft:frost_knight_* (helmet, chestplate, leggings, boots), on the toolkit in
tools/armor_models.py (shapes) and tools/armor_paint.py (the atlas). Its ice sword is an arm of its own
(tools/arms_variants.py).

What the owner drew, and where it is here (built part for part, as the owner asked: "complex models ... really
capturing the crazy unique geometry of each armor"):
    helmet      a frosted white great helm: a band round its brow set with ice; below it a dark eye slit across the
                face, crossed by a white ridge running from the crown down the brow to the nose; under the slit a white
                cheek band, then a row of dark breathing slits between white bars, then the chin; a crown of ice
                crystals on its brow, one tall in the middle, shorter ones beside it; a mane of jagged white frost
                sticking out from its sides and back. Here: the helm and the crown band; the brow plate over the slit,
                the cheek band and the chin plate raised on its face, four vent bars between them over the dark slits,
                the ridge down its middle standing proudest; five crystals, each one to three stacked prisms turned on
                their edge and tapering, the middle one tallest and the outer ones leaning out; eight frost spikes,
                four out from each side and up or back, three of them flat blades with narrower tips, and two more
                blades behind
    chestplate  a white cuirass with raised chest plates, a navy strap from the left shoulder to the right hip front and
                back; on the left shoulder a navy pauldron trimmed white; on the right a mass of white frost spikes,
                and a white frost mantle hanging down behind that shoulder, spiked along its edge; the forearms bare.
                Here: the cuirass, the two chest plates and a plate below them; the strap before and behind; on the
                left arm the pauldron, its dome and its white trim; on the right the frost's base, six spikes out of
                it and a flap down the outer arm with two spikes; on the body the mantle, hinged out behind the right
                shoulder, with three spikes out of its edge and three hanging from its foot
    leggings    white plated thighs in stacked lames, a navy belt with an ice gem at its buckle, banded knees. Here:
                the belt and the gem; on each leg the cuisse; three lames stepping down it, each a plate over the front
                and one over the outer side, hinged out from their tops; a plate behind the thigh; the knee cop, its
                band and a wing on its outer side
    boots       chunky white boots. Here: the cuff, the greave, two frost spikes at the outer ankle, the sabaton and
                the toe cap
Colours: armor_paint.FROST_KNIGHT; the crown's ice in its own five tones.

The renders show the front and the left side from behind; the right side is drawn from the front view and the mantle's
spikes in the design's words. The forearms are bare, as drawn (the owner's renders show the plain pale blue of their
model there). Every box is closed: a face is left out only where another box of the same piece and bone covers it (the
reliefs' backs, the crystals' feet, the spikes' feet inside what they grow from).

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


# ---------------------------------------------------------------- paint
PAINT = {
    "helm": {"top": [FROST, marks((0, 0, 99, 1, H))], "bottom": solid(S),
             "front": [FROST, marks((1, 3, 99, 1, X), (1, 5, 99, 2, x), (-1, 3, 1, 4, L), (0, -1, 99, 1, D))],
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
    # the ice: each prism lit on its upper faces, deep at its foot
    "ice": {"top": solid(I0), "bottom": solid(I3), "front": [rows(I1, I0), marks((0, 0, 1, 99, I0))],
            "right": [rows(I0, I1), marks((-1, 0, 1, 99, I2))], "left": [rows(I1, I2), marks((0, 0, 1, 99, I0))],
            "back": rows(I2, I3)},
    "ice_tip": {"top": solid(I0), "bottom": solid(I2), "front": solid(I0), "right": solid(I0), "left": solid(I1),
                "back": solid(I1)},
    # the frost spikes: white, a little grey down their edges
    "spike": {"top": solid(H), "bottom": solid(L), "front": [solid(H), marks((0, 0, 1, 99, L))],
              "back": [solid(L), marks((-1, 0, 1, 99, M))], "*": solid(L)},
    "spike_tip": {"top": solid(H), "*": solid(H)},
    "cuirass": {"top": [FROST, marks((0, 0, 99, 1, H))], "bottom": solid(S), "*": FROST},
    "pec": {"front": [plate(H), marks((0, 0, 99, 1, H), (0, -1, 99, 1, M))], "top": solid(H), "*": solid(L)},
    "abdomen": {"front": rows(H, L, M), "*": solid(L)},
    "strap": {"front": [solid(q), marks((0, 0, 99, 1, c))], "back": [solid(q), marks((0, 0, 99, 1, c))],
              "*": solid(Q)},
    # the left pauldron: navy, a white curl on its outer side, its trim white
    "pauldron": {"top": [solid(q), marks((0, 0, 99, 1, c))], "bottom": solid(K),
                 "right": [solid(q), marks((1, 1, 3, 1, H), (1, 1, 1, 3, H), (1, 3, 2, 1, H))],
                 "left": [solid(q), marks((1, 1, 3, 1, H), (3, 1, 1, 3, H), (2, 3, 2, 1, H))],
                 "*": [solid(q), marks((0, 0, 99, 1, c), (0, -1, 99, 1, Q))]},
    "dome": {"top": [solid(c), marks((0, -1, 99, 1, q))], "*": [solid(q), marks((0, 0, 99, 1, C))]},
    "trim": {"top": solid(H), "bottom": solid(M), "*": [solid(H), marks((0, -1, 99, 1, L))]},
    # the right shoulder's frost and the mantle: white in runs, greyer below
    "fur": {"top": [plate(H), marks((0, 0, 99, 1, H))], "bottom": solid(D), "*": [plate(L, "v"), marks((0, -1, 99, 1, M))]},
    "mantle": {"top": solid(H), "bottom": solid(M), "*": [plate(L, "v"), marks((0, 0, 99, 1, H), (0, -1, 99, 1, D))]},
    # the legs: the navy belt, the ice gem, the white cuisses and lames, the knee cop
    "belt": {"top": solid(q), "bottom": solid(K), "sides": [solid(q), marks((0, 0, 99, 1, c), (0, -1, 99, 1, Q))]},
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
def rod(name, base, length, section, out, back, **options):
    """A square bar standing on `base` (its bottom centre), tipped `out` degrees toward the model's right (-x) and
    leaning `back` degrees backward (negative: forward); build it on the model's right and mirror it for the left."""
    bx, by, bz = base
    s = section / 2
    return am.box(name, (bx - s, by - length, bz - s), (section, length, section), pivot=base,
                  rotation=(-back, 0, -out), **options)


def flame(name, base, length, width, out, back, thickness=0.45, side=False):
    """A flat frost spike standing on `base`: a blade `width` wide and `thickness` thin, its broad face to the front
    (`side`: to the side), and a tip half as wide and 0.2 thinner on its last 0.4 of the length; tipped `out` degrees
    toward the model's right (-x) and leaning `back` degrees backward. The tip stands on the blade's top, which hides
    its foot."""
    bx, by, bz = base
    lb = length * 0.6
    w, t = (thickness, width) if side else (width, thickness)
    blade = am.box(name, (bx - w / 2, by - lb, bz - t / 2), (w, lb, t), pivot=base, rotation=(-back, 0, -out),
                   paint=PAINT["spike"])
    tw, tt = (w - 0.2, t / 2) if side else (w / 2, t - 0.2)
    tip = am.box(f"{name}_tip", (bx - tw / 2, by - length, bz - tt / 2), (tw, length - lb, tt), pivot=base,
                 rotation=(-back, 0, -out), skip="bottom", paint=PAINT["spike_tip"])
    return [blade, tip]


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


def riding(part, on):
    """`part` given `on`'s turns as well as its own placement: it rides a hinged plate (a spike on the mantle)."""
    return replace(part, turns=part.turns + on.turns)


# ---------------------------------------------------------------- the helmet
def helmet():
    """The helm, 0.75 clear of the head, its top 0.75 above it, closed underneath 0.65 below; the crown band 0.2 proud
    round it; on its face, 0.35 proud, the brow plate over the eye slit, the cheek band under it and the chin plate,
    and between the cheek band and the chin four vent bars 0.3 proud over the dark breathing slits; the ridge down its
    middle from the crown band to the vents, 0.55 proud, crossing the slit as the nose bar; the crystals on the brow;
    the frost spikes, each standing on the helm's side or back and leaning out."""
    vents = [am.span(f"vent_{i}_right", (x0, -3.75, -5.05), (x0 + 0.7, -2.2, -4.75), skip=("back", "top", "bottom"),
                     paint=PAINT["vent"]) for i, x0 in enumerate((-3.45, -2.05))]
    side = [*crystal("crystal_side_right", (-2.3, -8.25, -3.6), [(2.5, 1.4), (1.75, 0.7)], lean=8),
            *crystal("crystal_outer_right", (-3.9, -8.25, -3.5), [(1.75, 0.9)], lean=20),
            *flame("spike_0_right", (-4.75, -7.5, -2.75), 3.75, 1.2, out=48, back=5),
            *flame("spike_1_right", (-4.75, -6.0, -0.75), 4.5, 1.3, out=62, back=18),
            *flame("spike_2_right", (-4.75, -4.25, 1.5), 4.0, 1.2, out=76, back=30),
            rod("spike_3_right", (-4.25, -8.75, 1.25), 3.0, 0.6, out=30, back=28, paint=PAINT["spike"])]
    return [am.span("helm", (-4.75, -8.75, -4.75), (4.75, 0.65, 4.75), paint=PAINT["helm"]),
            am.span("band", (-4.95, -8.25, -4.95), (4.95, -6.75, 4.95), paint=PAINT["band"]),
            am.span("brow", (-4.25, -6.75, -5.1), (4.25, -5.75, -4.75), skip="back", paint=PAINT["visor"]),
            am.span("cheek", (-4.25, -4.75, -5.1), (4.25, -3.75, -4.75), skip="back", paint=PAINT["visor"]),
            *vents, *am.mirror_all(vents),
            am.span("chin", (-4.25, -2.2, -5.1), (4.25, -0.35, -4.75), skip="back", paint=PAINT["visor"]),
            am.span("ridge", (-0.45, -8.25, -5.3), (0.45, -2.2, -4.95), skip="back", paint=PAINT["ridge"]),
            *crystal("crystal_crown", (0.0, -8.25, -3.7), [(3.0, 1.8), (2.5, 1.2), (1.75, 0.6)]),
            *side, *am.mirror_all(side),
            *flame("spike_back_right", (-1.75, -6.5, 4.75), 3.5, 1.2, out=18, back=62, side=True),
            *flame("spike_back_left", (1.75, -4.25, 4.75), 3.25, 1.2, out=-18, back=75, side=True)]


# ---------------------------------------------------------------- the chestplate
STRAP = 52.7   # the strap's slope, from the left shoulder (x 4, y 0) to the right hip (x -4, y 10.5)


def body():
    """The cuirass, 0.95 off the body's sides (0.2 outside the helm's, so the two never share a plane as the head
    turns) and 0.85 off its front and back; the chest plates 0.5 proud of it and the plate below them 0.35; the strap 0.65 proud before (0.15 off the chest plates) and behind; the mantle, behind the
    right shoulder, hinged out 6 degrees at its top, its spikes riding it."""
    strap = am.box("strap_front", (-7.0, 4.55, -3.5), (14.0, 1.4, 0.3), pivot=(0.0, 5.25, -3.5),
                   rotation=(0, 0, -STRAP), paint=PAINT["strap"])
    strap_back = am.box("strap_back", (-7.0, 4.55, 3.2), (14.0, 1.4, 0.3), pivot=(0.0, 5.25, 3.5),
                        rotation=(0, 0, -STRAP), paint=PAINT["strap"])
    pec = am.span("pec_right", (-4.0, 0.4, -3.35), (-0.3, 4.4, -2.85), skip="back", paint=PAINT["pec"])
    mantle = am.hinge(am.span("mantle", (-4.6, -0.4, 3.25), (-0.8, 10.25, 3.7), paint=PAINT["mantle"]), "top", 6)
    spikes = [*flame("mantle_spike_0", (-4.6, 2.5, 3.5), 2.75, 1.1, out=88, back=10, side=True),
              *flame("mantle_spike_1", (-4.6, 5.5, 3.5), 3.0, 1.1, out=96, back=14, side=True),
              *flame("mantle_spike_2", (-4.6, 8.5, 3.5), 2.75, 1.1, out=110, back=18, side=True),
              *flame("mantle_spike_3", (-4.25, 10.25, 3.5), 2.5, 1.0, out=165, back=12),
              *flame("mantle_spike_4", (-2.85, 10.25, 3.5), 3.0, 1.1, out=180, back=10),
              *flame("mantle_spike_5", (-1.45, 10.25, 3.5), 2.25, 1.0, out=195, back=14)]
    return [am.span("cuirass", (-4.95, -0.65, -2.85), (4.95, 10.5, 2.85), paint=PAINT["cuirass"]),
            *am.pair(pec),
            am.span("abdomen", (-2.75, 4.75, -3.2), (2.75, 9.25, -2.85), skip="back", paint=PAINT["abdomen"]),
            strap, strap_back, mantle, *[riding(s, mantle) for s in spikes]]


def arm_right():
    """The right arm (arm space: x -3..1, y -2..10, z -2..2): the frost's base over the shoulder, 1.75 past the arm's
    outer side; six spikes out of it, out, up, forward, back and down; a flap down the outer upper arm hinged out 12
    degrees, two spikes at its foot. The forearm is bare."""
    flap = am.hinge(am.span("flap_right", (-5.0, 1.75, -2.6), (-4.35, 7.25, 2.6), paint=PAINT["fur"]), "top", 12)
    return [am.span("fur_right", (-4.75, -3.75, -3.55), (1.75, 2.0, 3.55), paint=PAINT["fur"]),
            *flame("fur_spike_0_right", (-4.75, -2.5, -2.0), 2.0, 1.2, out=70, back=-15),
            *flame("fur_spike_1_right", (-4.75, -1.0, 0.25), 2.1, 1.3, out=86, back=8),
            *flame("fur_spike_2_right", (-4.75, 0.75, 2.25), 2.0, 1.2, out=104, back=32),
            *flame("fur_spike_3_right", (-3.0, -3.5, 1.5), 3.0, 1.2, out=28, back=40),
            rod("fur_spike_4_right", (-1.0, -3.5, -2.0), 2.5, 0.6, out=14, back=-32, paint=PAINT["spike"]),
            rod("fur_spike_5_right", (-2.0, -3.5, 0.0), 2.75, 0.6, out=-8, back=12, paint=PAINT["spike"]),
            flap,
            riding(rod("flap_spike_0_right", (-4.65, 7.25, -1.25), 1.75, 0.6, out=160, back=-10, paint=PAINT["spike"]),
                   flap),
            riding(rod("flap_spike_1_right", (-4.65, 7.25, 1.25), 2.5, 0.6, out=175, back=15, paint=PAINT["spike"]),
                   flap)]


def arm_left():
    """The left arm (arm space: x -1..3, y -2..10, z -2..2, the outer side +x): the navy pauldron, 1.6 past the arm's
    outer side and 1.25 above it, its dome, and the white trim round its foot. The forearm is bare."""
    return [am.span("pauldron_left", (-1.7, -3.25, -3.15), (4.6, 1.25, 3.15), skip="bottom", paint=PAINT["pauldron"]),
            am.span("dome_left", (-1.2, -4.0, -2.65), (4.1, -3.25, 2.65), skip="bottom", paint=PAINT["dome"]),
            am.span("trim_left", (-1.9, 1.0, -3.55), (4.8, 2.25, 3.55), paint=PAINT["trim"])]


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
    frost spikes out of its outer side at the ankle; the sabaton, 0.95 off and 1.05 behind, closed 0.65 below the
    foot; the toe cap."""
    return [am.span("cuff_right", (-3.0, 6.75, -3.0), (3.0, 7.75, 3.0), paint=PAINT["cuff"]),
            am.span("greave_right", (-2.75, 7.75, -2.75), (2.75, 11.0, 2.75), skip=("top", "bottom"),
                    paint=PAINT["greave"]),
            rod("ankle_spike_0_right", (-2.75, 9.25, -0.25), 2.5, 0.6, out=62, back=22, paint=PAINT["spike"]),
            rod("ankle_spike_1_right", (-2.75, 10.25, 1.25), 2.0, 0.6, out=78, back=42, paint=PAINT["spike"]),
            am.span("sabaton_right", (-2.95, 11.0, -3.6), (2.95, 12.65, 3.05), paint=PAINT["sabaton"]),
            am.span("toe_right", (-2.2, 11.3, -3.95), (2.2, 12.35, -3.6), skip="back", paint=PAINT["toe"])]


def left(parts):
    return [replace(p, inflate=LEFT_OUT) for p in am.mirror_all(parts)]


def model():
    l, b = leg(), boot()
    return {"frost_knight_helmet": {"head": helmet()},
            "frost_knight_chestplate": {"body": body(), "right_arm": arm_right(), "left_arm": arm_left()},
            "frost_knight_leggings": {"body": waist(), "right_leg": l, "left_leg": left(l)},
            "frost_knight_boots": {"right_leg": b, "left_leg": left(b)}}


SETS = [am.ArmorSet("frost_knight", FROST_KNIGHT, model())]
