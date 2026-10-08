"""Sentinel: the owner's gold-and-black knight (one of the two designs they sent later on 8 October 2026, "Just made
these ones aswell want them done weapons too please": three Blockbench renders, from the front and a little to the
right, from a little to the left, and from behind) as a 3D worn model for jugcraft:sentinel_* (helmet, chestplate,
leggings, boots), on the toolkit in tools/armor_models.py (shapes) and tools/armor_paint.py (the atlas). Its sword and
shield are arms of their own (tools/arms_variants.py).

What the owner drew, and where it is here (built part for part, as the owner asked: "complex models ... really
capturing the crazy unique geometry of each armor"):
    helmet      a gold bucket helm with a keyhole: a dark window at the eyes framed in pale gold and a dark slit down
                from it to the chin, rimmed; a loop like a little house on its crown; a pale ridge down its back between
                two dark hooks of a meander; scratches on its left side. Here: the helm; the window's frame (its top,
                sides and feet) and the slit's rims raised on its front; the loop's two posts and its peaked roof; the
                ridge behind
    chestplate  a near-black coat under a gold mantle across the shoulders; on the right shoulder a great pauldron of
                gold plates fanning out over the arm, on the left a smaller one of two flat plates; a gold square
                ring on the right upper arm; gold bands on the black sleeves and gold gauntlets. Here: the coat and the
                mantle; on the right arm the pauldron's cap and its three shell plates, each hinged further out, the
                square ring, the sleeve, two bands and the gauntlet; on the left the two plates, the sleeve, the bands
                and the gauntlet
    leggings    the coat's black skirt to the knee, parted at the middle, with a gold plate before and behind the
                left thigh and a dark one on the right. Here: the belt; on each leg the hose, the skirt and a plate
                before and behind it hinged out from the hip, gold on the left, dark iron on the right
    boots       gold boots with a brown band and a pale toe. Here: the greave, the band round it, the sabaton and the
                toe cap
Colours: armor_paint.SENTINEL.

The renders show the front, both sides and the back. The design is not symmetric: the pauldrons differ and only the left
thigh's plates are gold, so the left arm and leg are built as drawn rather than mirrored from the right. Every box is
closed: a face is left out only where another box of the same piece and bone covers it (the reliefs' and the plates'
backs, the cap's foot inside the shells, the sleeve's ends, the greave's foot inside the sabaton).

Space (tools/armor_models.py): each bone's own space in model pixels; x is the model's LEFT, y is DOWN, z is the BACK
(the face is at -z).
Preview: python3 tools/armor_preview.py --set sentinel
"""
from dataclasses import replace

import armor_models as am
from armor_paint import SENTINEL, P


def rows(*tones):
    """One tone per texel row, cycling, each row broken into runs of the next tone darker."""
    return P("lames", rows=1, tones=tuple((t, t) for t in tones), centre=None)


def solid(tone):
    return P("solid", tone=tone)


def marks(*rects, symmetric=False):
    return P("marks", rects=list(rects), symmetric=symmetric)


def plate(tone, strips="h"):
    return P("plate", tone=tone, strips=strips)


H, L, M, D, S, V = "light", "mid_light", "mid", "dark", "seam", "void"       # the gold, cream to brown
G, g = "gold_light", "gold_dark"                                            # the palest cream, the deep brown
C, c, q, Q, K = "leather_light", "leather_mid_light", "leather_mid", "leather_dark", "leather_darkest"   # browns
U, u, x, X = "under_light", "under_mid", "under_dark", "under_darkest"       # the black cloth

CLOTH = P("plate", tone=x, strips="v")   # the near-black cloth, in long runs a tone lighter or darker


# ---------------------------------------------------------------- paint
PAINT = {
    # the helm: gold, its top row pale; on its front the keyhole's window and slit dark; behind, the meander's two
    # dark hooks either side of the ridge; on its left side the owner's scratches
    "helm": {"front": [plate(M), marks((0, 0, 99, 1, H), (1, 1, 1, 3, D), (-2, 1, 1, 3, D), (3, 3, 4, 3, X),
                                       (4, 6, 2, 4, X), (1, 6, 2, 2, D), (-3, 6, 2, 2, D), (0, -1, 99, 1, S))],
             "back": [plate(M), marks((0, 0, 99, 1, H), (1, 2, 3, 1, g), (1, 2, 1, 3, g), (1, 4, 2, 1, g),
                                      (1, 6, 1, 3, g), (1, 8, 3, 1, g), (3, 6, 1, 2, g), symmetric=True)],
             "left": [plate(M), marks((0, 0, 99, 1, H), (5, 2, 1, 3, Q), (7, 2, 2, 1, Q), (8, 3, 1, 1, Q),
                                      (7, 4, 2, 1, Q))],
             "top": [plate(L), marks((0, 0, 99, 1, H))], "bottom": solid(V),
             "*": [plate(M), marks((0, 0, 99, 1, H), (2, 3, 2, 2, D), (6, 6, 2, 2, D), (0, -1, 99, 1, S))]},
    "frame": {"front": [solid(H), marks((0, 0, 99, 1, G))], "top": solid(G), "bottom": solid(M), "*": solid(L)},
    "rim": {"front": solid(L), "top": solid(H), "*": solid(M)},
    "loop": {"top": solid(H), "bottom": solid(D), "*": [solid(L), marks((0, 0, 99, 1, H))]},
    "ridge": {"back": [solid(H), marks((0, 0, 99, 1, G))], "*": solid(L)},
    # the coat: near-black cloth, a darker line down its middle; the mantle: gold rows, a pale top
    "coat": {"front": [CLOTH, marks((4, 0, 2, 99, X))], "back": [CLOTH, marks((4, 0, 2, 99, X))],
             "top": solid(x), "bottom": solid(X), "*": CLOTH},
    "mantle": {"top": [solid(H), marks((0, -1, 99, 1, L))], "bottom": solid(S),
               "*": [rows(L, M, L, D), marks((0, 0, 99, 1, H))]},
    # the right pauldron: the cap pale on top; each shell plate gold with a pale edge and a brown groove
    "cap": {"top": [plate(L), marks((0, 0, 99, 1, H), (0, -1, 99, 1, D))], "bottom": solid(S),
            "*": [plate(L), marks((0, 0, 99, 1, H))]},
    "shell": {"top": solid(G), "bottom": solid(S),
              "*": [plate(M, "v"), marks((0, 0, 99, 1, H), (0, 0, 1, 99, H), (-1, 0, 1, 99, D), (2, 2, 1, 99, Q))]},
    "baldric": {"front": [solid(u), marks((0, 0, 99, 1, U))], "*": solid(x)},
    "medallion": {"front": P("chevron", corner="o", bands=(H, L), border=None, core=(X, X), outside=H),
                  "top": solid(H), "*": solid(M)},
    "sleeve": {"sides": CLOTH, "ends": solid(X)},
    "band": {"top": solid(H), "bottom": solid(S), "*": [solid(L), marks((0, 0, 99, 1, H), (0, -1, 99, 1, M))]},
    "gauntlet": {"top": solid(H), "bottom": solid(V), "*": [rows(L, M, D), marks((0, 0, 99, 1, H))]},
    # the left pauldron: two flat plates, gold in brown stripes
    "slab": {"top": [plate(L), marks((0, 0, 99, 1, H))], "bottom": solid(S), "*": rows(L, Q, M, Q)},
    # the legs: the belt, the hose, the black skirt with its folds; the plates gold on the left, dark iron on the right
    "belt": {"top": solid(x), "bottom": solid(X), "sides": [solid(u), marks((0, 0, 99, 1, L), (0, -1, 99, 1, X))]},
    "hose": {"sides": CLOTH, "ends": solid(X)},
    "skirt": {"front": [CLOTH, marks((1, 0, 1, 99, X), (-2, 0, 1, 99, u))], "back": [CLOTH, marks((2, 0, 1, 99, X))],
              "top": solid(x), "bottom": solid(X), "*": CLOTH},
    "plate_gold": {"front": [plate(L), marks((0, 0, 99, 1, H), (0, 0, 1, 99, H), (-1, 0, 1, 99, D), (1, 1, 2, 2, G))],
                   "back": [plate(M), marks((0, 0, 99, 1, H), (0, 0, 1, 99, H), (-1, 0, 1, 99, D))],
                   "top": solid(H), "bottom": solid(S), "*": solid(M)},
    "plate_iron": {"front": [rows(U, u), marks((0, 0, 99, 1, U), (0, 0, 1, 99, U), (-1, 0, 1, 99, X))],
                   "back": [rows(U, u), marks((0, 0, 99, 1, U), (0, 0, 1, 99, U), (-1, 0, 1, 99, X))],
                   "top": solid(U), "bottom": solid(X), "*": solid(u)},
    "greave": {"top": solid(H), "*": [plate(L), marks((0, 0, 99, 1, H), (0, 2, 99, 1, M))]},
    "boot_band": {"top": solid(c), "bottom": solid(K), "*": rows(q, Q)},
    "sabaton": {"top": solid(L), "bottom": solid(V), "*": [plate(M), marks((0, 0, 99, 1, L), (0, -1, 99, 1, S))]},
    "toe": {"top": solid(G), "bottom": solid(D), "*": [solid(H), marks((0, -1, 99, 1, L))]},
    "instep": {"top": solid(H), "bottom": solid(S), "*": [solid(L), marks((0, 0, 99, 1, H), (0, -1, 99, 1, D))]},
    "rivet": {"top": solid(G), "*": solid(H)},
}


# ---------------------------------------------------------------- the helmet
def helmet():
    """The helm, 1.0 clear of the head's sides, front and back, its top 1.0 above it, closed underneath 0.65 below;
    the keyhole's frame 0.4 proud of its front and the slit's rims 0.2 proud; the loop on its crown, its roof's left bar
    0.15 behind the right so the two never share a plane where they cross; the ridge 0.35 proud behind."""
    frame = [am.span("frame_side_right", (-3.0, -6.25, -5.4), (-2.0, -3.25, -5.0), skip="back", paint=PAINT["frame"]),
             am.span("frame_foot_right", (-3.0, -3.25, -5.4), (-1.0, -2.25, -5.0), skip="back", paint=PAINT["frame"]),
             am.span("slit_rim_right", (-2.0, -2.25, -5.2), (-1.0, 0.0, -5.0), skip="back", paint=PAINT["rim"])]
    post = am.span("post_right", (-2.1, -10.6, -0.7), (-1.35, -9.0, 0.7), skip="bottom", paint=PAINT["loop"])
    roof_right, roof_left = am.chevron("roof", (0.0, -11.45, -0.35), 2.45, 0.75, 0.7, angle=-32.0, paint=PAINT["loop"])
    roof_left = replace(roof_left, origin=(roof_left.origin[0], roof_left.origin[1], roof_left.origin[2] + 0.15))
    return [am.span("helm", (-5.0, -9.0, -5.0), (5.0, 0.65, 5.0), paint=PAINT["helm"]),
            am.span("frame_top", (-3.0, -7.25, -5.4), (3.0, -6.25, -5.0), skip="back", paint=PAINT["frame"]),
            *frame, *am.mirror_all(frame),
            *am.pair(post), roof_right, roof_left,
            am.span("ridge", (-0.5, -8.75, 5.0), (0.5, 0.0, 5.35), skip="front", paint=PAINT["ridge"])]


# ---------------------------------------------------------------- the chestplate
def body():
    """The coat, 0.65 off the body's sides and 0.95 off its front and back; the mantle across the shoulders, 1.25 off
    its sides and front and back (0.25 outside the helm's sides, so the two never share a plane as the head turns)."""
    # the baldric: a strap from the right shoulder down to the left hip, 0.15 proud of the coat's front, its back
    # against the coat; cut to the coat's front so it never overhangs it
    baldric = am.box("baldric", (-6.6, 4.25, -3.1), (13.2, 1.5, 0.15), pivot=(0.0, 5.0, -3.1), rotation=(0, 0, 52),
                     skip="back", paint=PAINT["baldric"])
    return [am.span("coat", (-4.65, -0.65, -2.95), (4.65, 10.6, 2.95), paint=PAINT["coat"]),
            am.span("mantle", (-5.25, -0.95, -3.25), (5.25, 1.4, 3.25), paint=PAINT["mantle"]),
            baldric]


def arm_right():
    """The right arm (arm space: x -3..1, y -2..10, z -2..2): the pauldron's cap over the shoulder and three shell
    plates down its outer side, each further out and further down, hinged out at its foot (6, 10 and 13 degrees); the
    square ring on the upper arm's front; the black sleeve from inside the cap to the gauntlet; two gold bands on the
    forearm; the gauntlet, to 0.6 below the hand."""
    shells = [am.hinge(am.span("shell_0_right", (-5.0, -3.75, -3.3), (-4.25, 2.0, 3.3), paint=PAINT["shell"]),
                       "top", 6),
              am.hinge(am.span("shell_1_right", (-5.5, -3.0, -3.05), (-4.75, 3.25, 3.05), paint=PAINT["shell"]),
                       "top", 10),
              am.hinge(am.span("shell_2_right", (-6.0, -2.25, -2.75), (-5.25, 4.5, 2.75), paint=PAINT["shell"]),
                       "top", 13)]
    # three rivets along the foot of each of the two inner shells, riding their hinges
    rivets = [replace(r, turns=shell.turns) for shell, (x, y) in zip(shells, ((-5.0, 1.0), (-5.5, 2.25)))
              for r in am.rivets(f"{shell.name.replace('_right', '')}_rivet_right", (x, y, -2.0), (0.0, 0.0, 2.0), 3,
                                 size=(0.4, 0.6, 0.6), face="right", paint=PAINT["rivet"])]
    return [am.span("cap_right", (-4.5, -4.0, -3.45), (1.75, -2.5, 3.45), paint=PAINT["cap"]),
            am.span("cap_crown_right", (-3.75, -4.75, -2.75), (0.75, -4.0, 2.75), skip="bottom", paint=PAINT["cap"]),
            *shells, *rivets,
            am.span("medallion_right", (-2.25, 3.0, -3.1), (0.25, 5.5, -2.6), paint=PAINT["medallion"]),
            *sleeve_and_hand("right")]


def sleeve_and_hand(side):
    """The black sleeve, 0.45 off the arm, from inside the pauldron (so the shoulder never shows under it); two gold
    bands, 0.7 off, and the gold gauntlet, 0.9 off its sides and 1.05 off its front and back (their fronts and backs
    clear of the coat's, the belt's and the skirt's planes)."""
    out = [am.span("sleeve_right", (-3.45, -2.75, -2.45), (1.45, 9.0, 2.45), skip=("top", "bottom"),
                   paint=PAINT["sleeve"]),
           am.span("band_a_right", (-3.7, 5.75, -2.75), (1.7, 6.6, 2.75), paint=PAINT["band"]),
           am.span("band_b_right", (-3.7, 7.6, -2.75), (1.7, 8.45, 2.75), paint=PAINT["band"]),
           am.span("gauntlet_right", (-3.9, 8.75, -3.05), (1.9, 10.6, 3.05), paint=PAINT["gauntlet"])]
    return out if side == "right" else am.mirror_all(out)


def arm_left():
    """The left arm (arm space: x -1..3, y -2..10, z -2..2, the outer side +x): a low crown and two flat plates over the
    shoulder, the lower one reaching further out; the sleeve, bands and gauntlet as the right's."""
    return [am.span("slab_crown_left", (-1.0, -4.0, -2.75), (3.75, -3.25, 2.75), skip="bottom", paint=PAINT["cap"]),
            am.span("slab_left", (-1.75, -3.25, -3.65), (4.5, -1.75, 3.65), paint=PAINT["slab"]),
            am.span("slab_low_left", (-1.25, -1.75, -3.45), (4.85, -0.25, 3.45), paint=PAINT["slab"]),
            *sleeve_and_hand("left")]


# ---------------------------------------------------------------- the leggings and boots
LEFT_OUT = 0.12   # the left leg's parts this much larger, so where the legs meet their faces never share a plane


def waist():
    """The belt, 0.6 off the body's front and back and 0.45 off its sides."""
    return [am.span("belt", (-4.45, 10.25, -2.6), (4.45, 12.65, 2.6), paint=PAINT["belt"])]


def leg(plate_paint):
    """The right leg (leg space: x -2..2, y 0..12, z -2..2): the hose, 0.45 off the leg from inside the skirt into the
    boot (closed at its foot, which shows when the leggings are worn alone), so the leg never shows between skirt and
    boot; the skirt round the leg, 0.77 off, from up under the belt to
    below the knee; a plate before and behind the thigh, 1.15 off (clear of vanilla leggings' 1.0 shell), hinged out
    8 degrees from the hip."""
    return [am.span("hose_right", (-2.45, 0.8, -2.45), (2.45, 11.0, 2.45), skip="top", paint=PAINT["hose"]),
            am.span("skirt_right", (-2.77, -0.45, -2.77), (2.77, 7.75, 2.77), paint=PAINT["skirt"]),
            am.hinge(am.span("plate_front_right", (-2.55, 0.25, -3.45), (1.45, 6.25, -3.15), paint=plate_paint),
                     "top", 8),
            am.hinge(am.span("plate_back_right", (-2.55, 0.25, 3.15), (1.45, 6.25, 3.45), paint=plate_paint),
                     "top", 8)]


def boot():
    """A gold cuff at the greave's top, 1.1 off the leg; the greave from just below the skirt (the hose covers the leg
    between), 0.75 off (clear of vanilla leggings' 0.5 shell); the brown band round it, 1.0 off; the sabaton, 0.95 off
    and 1.05 behind, closed 0.65 below the foot; two instep lames overlapping toward the toe; the pale toe cap. Where
    the legs overlap, each of these keeps 0.1 or more off the other leg's planes."""
    out = [am.span("cuff_right", (-3.1, 8.0, -3.1), (3.1, 8.75, 3.1), paint=PAINT["band"]),
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
    is the right one's mirror with the plates repainted (each under a name of its own, so it has a texture of its own)."""
    right_leg = leg(PAINT["plate_iron"])
    left_leg = [replace(p, paint=PAINT["plate_gold"], net="") if p.name.startswith("plate_") else p
                for p in left(right_leg)]
    b = boot()
    return {"sentinel_helmet": {"head": helmet()},
            "sentinel_chestplate": {"body": body(), "right_arm": arm_right(), "left_arm": arm_left()},
            "sentinel_leggings": {"body": waist(), "right_leg": right_leg, "left_leg": left_leg},
            "sentinel_boots": {"right_leg": b, "left_leg": left(b)}}


SETS = [am.ArmorSet("sentinel", SENTINEL, model())]
