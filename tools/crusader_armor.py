"""Paladin and Templar: the owner's two crusader knights (two of the three designs they sent on 8 October 2026, "I made
these 3": Blockbench renders of each worn, from the front and from a little to the side) as 3D worn models for
jugcraft:paladin_* and jugcraft:templar_* (helmet, chestplate, leggings, boots), on the toolkit in
tools/armor_models.py (shapes) and tools/armor_paint.py (the atlas). The two share one build below the neck, as the
owner's do, and differ in colour and in their helms.

What the owner drew, and where it is here (the owner asked for "complex models ... really capturing the crazy unique
geometry of each armor", so each shape is built, not painted):
    helmet      a great helm. The Paladin's white, its visor carrying a raised white H (two uprights and a bar at the
                eyes) over a dark breath, its sides dark mail, a white comb along its crown and a purple sprig like a
                cross behind it. The Templar's slate, barred with raised ribs, a pale gable guard over its brow
                overhanging the helm's sides, and a pale crest curling up from its crown. Here: the helm, the visor
                plate and on it the Paladin's uprights and bar or the Templar's three ribs; a row of rivets down each
                side; the Paladin's comb, stepped down behind, and the sprig's stem and two bars; the Templar's two
                brow bars meeting at the peak (the left 0.15 nearer, so they never share a plane) and its crest: a
                stalk leaning forward, a bend and a flag tipped back
    chestplate  a breastplate shaped like a heater shield carrying a cross, white on the Paladin, pale on slate on the
                Templar; dark mail at the Paladin's sides and arms, red cloth at the Templar's collar; big square
                pauldrons with a square spiral; a band at the elbow, banded forearms; hoops round the waist. Here: the
                cuirass; the shield in three steps narrowing to its foot, and the cross raised on it; a back plate
                with a spine ridge; two waist hoops, the lower wider; on each arm the pauldron with a raised dome on
                top and the spiral boss on its outer side, two lames fanning out under it, the mail sleeve, the elbow
                band with a fan plate on its outer side, the vambrace, a cuff flaring out from the wrist and the glove
    leggings    tassets over the front and sides of the thighs carrying the spiral (the Paladin's with a blue gem), a
                cloth underskirt to the knee between and under them, purple on the Paladin, dark red on the Templar.
                Here: the belt and its buckle (the Paladin's with a gem) on the body; on each leg the cloth, flaps
                before, behind and outside it flaring to below the knee, the front tasset and the side tasset, both
                hinged out from the hip, a raised spiral boss on the front one (with the Paladin's gem)
    boots       banded greaves and sabatons. Here: the greave, a knee cop on its front, the sabaton, two instep lames
                overlapping toward the toe and a toe cap
Colours: armor_paint.PALADIN and armor_paint.TEMPLAR.

The renders show the front and a three-quarter view; the backs are drawn in the designs' own words (the bars and
spirals run round, a plain back plate with a spine, the tassets' and the cloth's flaps behind too). The weapons in two
of the pictures are not armor; they are left for another time. Every box is closed: a face is left out only where
another box of the same piece and bone covers it (the visor's, the reliefs', the shield's, the back plate's and the
bosses' backs, the crest's and the sprig's feet, each arm plate's end inside the next, the boot under the sabaton), so
the sets show no holes alone, on an armor stand's thin limbs (tools/art_check.py, H1).

Space (tools/armor_models.py): each bone's own space in model pixels; x is the model's LEFT, y is DOWN, z is the BACK
(the face is at -z). Right-hand parts are built and mirrored to the left.
Preview: python3 tools/armor_preview.py --set paladin --set templar
"""
from dataclasses import replace

import armor_models as am
from armor_paint import PALADIN, TEMPLAR, P


def rows(*tones):
    """One tone per texel row, cycling, each row broken into runs of the next tone darker."""
    return P("lames", rows=1, tones=tuple((t, t) for t in tones), centre=None)


def solid(tone):
    return P("solid", tone=tone)


def marks(*rects, symmetric=False):
    return P("marks", rects=list(rects), symmetric=symmetric)


def cols(*tones, width=12):
    """Upright strips, one tone per texel column, cycling."""
    return marks(*[(i, 0, 1, 99, tones[i % len(tones)]) for i in range(width)])


def rings(*bands, core):
    """Concentric square rings from the edge in about a core: the owner's square spiral."""
    return P("chevron", corner="o", bands=bands, border=None, core=core, outside=bands[0])


MAIL = P("checker", tones=("under_dark", "under_mid"))   # the dark mail, in checks

# The palettes' names (armor_paint.PALADIN, armor_paint.TEMPLAR).
H, L, M, D, S, V = "light", "mid_light", "mid", "dark", "seam", "void"       # plate, light to dark
G, g = "gold_light", "gold_dark"                                            # Paladin: the blue gem; Templar: near-white
C, c, q, Q, K = "leather_light", "leather_mid_light", "leather_mid", "leather_dark", "leather_darkest"   # the cloth
U, u, x, X = "under_light", "under_mid", "under_dark", "under_darkest"       # the mail and the dark under-layer


# ---------------------------------------------------------------- paint
def paints(kind):
    """Role -> paint spec for "paladin" or "templar", the owner's patterns read off their renders."""
    paladin = kind == "paladin"
    brow = H if paladin else L                    # the light edge every plate's top carries
    plate = (L, M) if paladin else (D, S)         # a plate's face and its shadow
    relief = (H, L) if paladin else (L, M)        # the raised work: the H, the ribs, the cross
    spiral = rings(H, M, V, L, core=(D, S)) if paladin else rings(L, V, M, V, core=(S, D))
    side = ([MAIL, marks((0, 0, 99, 1, H), (1, 1, 4, 1, M), (1, 1, 1, 3, M))] if paladin
            else [rows(S, V, S, D, S, V), marks((0, 0, 99, 1, D))])
    edged = [solid(relief[1]), marks((0, 0, 99, 1, relief[0]))]
    # the shield on the chest: plain, with a light rim down its sides and a darker one inside it
    shield = [solid(M), marks((1, 0, 1, 99, L if paladin else D), (-2, 0, 1, 99, L if paladin else D),
                              (0, 0, 1, 99, brow), (-1, 0, 1, 99, brow))]
    return {
        "helm": {"front": ([solid(M), marks((0, 0, 99, 1, brow), (0, 0, 1, 99, V), (-1, 0, 1, 99, V))] if paladin else
                           [solid(V), marks((0, 0, 99, 1, brow), (0, 0, 1, 99, plate[0]), (-1, 0, 1, 99, plate[1]))]),
                 "top": [solid(plate[0]), marks((0, 0, 99, 1, brow), (0, 0, 1, 99, brow))], "bottom": solid(X),
                 "right": side, "left": side,
                 "back": [rows(*((L, M, D, M) if paladin else (D, S, V, S))), marks((0, 0, 99, 1, brow))]},
        # the visor plate: the Paladin's grey with a dark breath under the H and dark edges; the Templar's slots
        "visor": {"front": ([solid(L), marks((0, 0, 1, 99, V), (-1, 0, 1, 99, V), (3, 3, 2, 3, V), (0, 0, 99, 1, H),
                                             (0, -1, 99, 1, M))] if paladin else
                            [cols(X, M, X, D, D, X, M, X, width=8), marks((0, 0, 99, 1, L), (0, -1, 99, 1, S))]),
                  "*": solid(plate[1])},
        "upright": {"top": solid(H), "*": edged},
        "crossbar": {"*": [solid(H if paladin else L)]},
        "rib": {"top": solid(L), "bottom": solid(V), "*": [solid(M), marks((0, 0, 99, 1, L))]},
        "rivet": {"*": solid(brow), "top": solid(H)},
        # the Paladin's comb and purple sprig
        "comb": {"top": solid(H), "sides": [solid(L), marks((0, 0, 99, 1, H))], "bottom": solid(M)},
        "sprig": {"top": solid(C), "bottom": solid(Q), "*": [solid(c), marks((0, 0, 99, 1, C))]},
        # the Templar's brow guard and crest, pale over the slate
        "brow": {"top": solid(H), "bottom": solid(S), "*": [rows(L, M), marks((0, 0, 99, 1, H))]},
        "crest": {"top": solid(g), "bottom": solid(M), "*": [solid(L), marks((0, 0, 99, 1, H))]},
        "flag": {"top": solid(g), "bottom": solid(L), "*": [solid(H), marks((0, -1, 99, 1, L), (0, 0, 1, 99, g))]},
        # the cuirass: the Paladin's mail with a white collar line; the Templar's slate, the red cloth at its collar
        "cuirass": ({"front": [MAIL, marks((0, 0, 99, 1, H))], "top": [solid(L), marks((0, 0, 99, 1, H))],
                     "bottom": solid(X), "*": [MAIL, marks((0, 0, 99, 1, L))]} if paladin else
                    {"front": [rows(S, V), marks((0, 0, 3, 2, C), (-3, 0, 3, 2, C), (0, 2, 2, 1, c), (-2, 2, 2, 1, c))],
                     "top": [solid(S), marks((0, 0, 3, 99, c), (-3, 0, 3, 99, c))], "bottom": solid(X),
                     "*": rows(S, V, S, D)}),
        "shield": {"front": shield, "top": solid(brow), "bottom": solid(S), "*": solid(plate[1])},
        "shield_foot": {"front": [*shield, marks((0, -1, 99, 1, plate[0]))], "top": solid(brow),
                        "bottom": solid(S), "*": solid(plate[1])},
        "cross": {"front": solid(H if paladin else g), "*": solid(relief[1])},
        "back_plate": {"back": [rings(*((L, M) if paladin else (D, S)), core=(M, L) if paladin else (S, D))],
                       "*": solid(plate[1])},
        "spine": {"top": solid(brow), "*": [solid(plate[0]), marks((0, 0, 99, 1, brow))]},
        "fauld": {"top": solid(brow), "bottom": solid(S), "sides": [rows(*((H, L) if paladin else (L, S))),
                                                                     marks((0, -1, 99, 1, M if paladin else V))]},
        # the arms: the pauldron's square spiral on its faces, the boss's on its own; the lames; the mail sleeve; the
        # elbow band and its fan; the banded vambrace; the cuff and glove
        "pauldron": {"top": [solid(plate[0]), marks((0, 0, 99, 1, brow), (0, 0, 1, 99, brow))], "bottom": solid(S),
                     "*": spiral},
        "dome": {"top": [solid(brow), marks((0, -1, 99, 1, plate[0]))],
                 "*": [solid(plate[0]), marks((0, 0, 99, 1, brow))]},
        "boss": {"right": spiral, "left": spiral, "*": [solid(plate[0]), marks((0, 0, 99, 1, brow))]},
        "paul_lame": {"top": solid(plate[0]), "bottom": solid(X), "sides": rows(*((H, M) if paladin else (L, S)))},
        "sleeve": {"sides": MAIL, "ends": solid(x)},
        "couter": {"top": solid(H if paladin else L), "bottom": solid(S),
                   "sides": rows(*((H, L) if paladin else (L, M)))},
        "wing": {"right": rings(*((H, M) if paladin else (L, V)), core=(M, L) if paladin else (S, D)),
                 "*": solid(brow)},
        "vambrace": {"bottom": solid(S), "*": rows(*((H, M, V, L) if paladin else (M, S, V, L)))},
        "cuff": {"top": solid(brow), "bottom": solid(S),
                 "*": [rows(*((L, M) if paladin else (M, S))), marks((0, 0, 99, 1, brow))]},
        "glove": {"top": solid(x), "bottom": solid(X),
                  "*": [rows(*((M, D) if paladin else (S, V))), marks((0, 0, 99, 1, plate[0]))]},
        # the legs: the belt and buckle, the cloth and its flaps in folds, the tassets' spiral (the Paladin's gem)
        "belt": {"top": solid(u), "bottom": solid(X), "sides": [rows(u, x), marks((0, 0, 99, 1, U))]},
        "buckle": {"front": rings(*((H, M) if paladin else (L, S)), core=(G, g) if paladin else (D, S)),
                   "*": solid(brow)},
        "hip": {"top": solid(c), "bottom": solid(K), "sides": [rows(c, q), marks((0, 0, 99, 1, C))]},
        "underskirt": {"top": solid(c), "bottom": solid(K),
                       "sides": [cols(c, q, c, Q, q, c), marks((0, -1, 99, 1, K), (0, -2, 99, 1, Q))]},
        "flap": {"top": solid(c), "bottom": solid(K),
                 "sides": [cols(c, q, Q, q, c, q), marks((0, -1, 99, 1, K), (0, 0, 99, 1, C))]},
        "tasset": {"front": rings(*((H, M, V, M) if paladin else (L, V, M, V)), core=(S, D) if paladin else (S, V)),
                   "top": solid(brow), "bottom": solid(S),
                   "*": [rows(*((L, M, D) if paladin else (M, S, V))), marks((0, 0, 99, 1, brow))]},
        "tasset_boss": {"front": rings(*((H, L) if paladin else (L, M)), core=(M, D) if paladin else (S, V)),
                        "*": solid(brow)},
        "gem": {"front": [solid(g), marks((0, 0, 1, 1, G))], "*": solid(g)},
        "greave": {"top": solid(brow), "bottom": solid(S), "sides": rows(*((H, V, L, S) if paladin else (L, V, M, X)))},
        "poleyn": rings(*((H, M) if paladin else (L, S)), core=(L, D) if paladin else (M, V)),
        "sabaton": {"top": solid(L), "bottom": solid(X), "front": [rows(H, M), marks((0, -1, 99, 1, X))],
                    "*": [rows(*((H, M) if paladin else (L, S))), marks((0, -1, 99, 1, X))]},
        "instep": {"top": solid(H if paladin else L), "bottom": solid(S),
                   "*": [solid(L if paladin else M), marks((0, 0, 99, 1, H if paladin else L))]},
        "toe": {"top": solid(H if paladin else L), "bottom": solid(X), "*": [rows(*((H, M) if paladin else (L, S))), marks((0, -1, 99, 1, X))]},
    }


# ---------------------------------------------------------------- shape helpers
def rod(name, base, length, section, out, back, **options):
    """A square bar standing on `base` (its bottom centre), tipped `out` degrees toward the model's right (-x) and
    leaning `back` degrees backward (negative: forward); build it on the model's right and mirror it for the left."""
    bx, by, bz = base
    s = section / 2
    return am.box(name, (bx - s, by - length, bz - s), (section, length, section), pivot=base,
                  rotation=(-back, 0, -out), **options)


def riding(part, on):
    """`part`, built in `on`'s unturned frame, given the same turns, so it rides a hinged plate (a boss, a gem)."""
    return replace(part, turns=on.turns)


# ---------------------------------------------------------------- helmet
def helmet(r, kind):
    """The helm, 0.65 clear of the head's sides and 0.75 of its front and back, its top 1.0 above it (0.5 off the hat
    layer), closed underneath 0.65 below it; the visor plate 0.5 proud of its front, the reliefs on the visor; a row
    of four rivets down each side at the brow. Its sides keep 0.2 inside the cuirass's, so the two never share a plane
    while the head turns."""
    parts = [
        am.span("helm", (-4.65, -9.0, -4.75), (4.65, 0.65, 4.75), paint=r["helm"]),
        am.span("visor", (-3.8, -7.25, -5.25), (3.8, -0.5, -4.75), skip="back", paint=r["visor"]),
        *am.rivets("rivet_right", (-4.65, -7.6, -3.3), (0.0, 0.0, 2.2), 4, size=(0.5, 0.75, 0.75), face="right",
                   paint=r["rivet"]),
        *am.rivets("rivet_left", (4.65, -7.6, -3.3), (0.0, 0.0, 2.2), 4, size=(0.5, 0.75, 0.75), face="left",
                   paint=r["rivet"]),
    ]
    if kind == "paladin":
        # the H: two uprights 0.4 proud of the visor, the bar between them 0.25 proud (0.15 behind their fronts)
        upright = am.span("upright_right", (-2.5, -7.0, -5.65), (-1.5, -0.75, -5.25), skip="back",
                          paint=r["upright"])
        # the sprig: a stem behind the comb's tail with two bars across it, like a cross; the bars 0.15 inside the
        # stem's front and back, so they never share its planes
        sx, sz = -2.0, 2.6
        parts += [*am.pair(upright),
                  am.span("crossbar", (-1.5, -5.4, -5.5), (1.5, -4.4, -5.25), skip=("back", "right", "left"),
                          paint=r["crossbar"]),
                  am.span("comb", (-0.75, -10.0, -4.25), (0.75, -9.0, 2.15), skip="bottom", paint=r["comb"]),
                  am.span("comb_tail", (-0.6, -9.6, 2.0), (0.6, -9.0, 4.25), skip=("bottom", "front"),
                          paint=r["comb"]),
                  am.span("sprig", (sx - 0.4, -13.2, sz - 0.4), (sx + 0.4, -9.0, sz + 0.4), skip="bottom",
                          paint=r["sprig"]),
                  am.span("sprig_bar", (sx - 1.4, -12.2, sz - 0.25), (sx + 1.4, -11.45, sz + 0.25),
                          paint=r["sprig"]),
                  am.span("sprig_low", (sx - 0.4, -10.9, sz - 0.25), (sx + 1.3, -10.25, sz + 0.25),
                          skip="right", paint=r["sprig"])]
    else:
        right, left = am.chevron("brow", (0.0, -8.15, -5.75), 7.4, 1.1, 1.0, angle=-30.0, paint=r["brow"])
        left = replace(left, origin=(left.origin[0], left.origin[1], left.origin[2] - 0.15))
        rib = am.span("rib_right", (-2.4, -6.9, -5.6), (-1.6, -0.75, -5.25), skip="back", paint=r["rib"])
        # the crest: a stalk from the crown leaning forward, a bend, and a pale flag tipped back over the crown
        stalk = rod("crest_stalk", (0.0, -9.0, 1.4), 2.0, 1.0, out=0, back=-14, skip="bottom", paint=r["crest"])
        top = am.place(stalk, (0.0, -11.0, 1.4))
        bend = rod("crest_bend", top, 1.1, 0.7, out=0, back=35, skip="bottom", paint=r["crest"])
        tip = am.place(bend, (top[0], top[1] - 1.1, top[2]))
        flag = am.box("crest_flag", (tip[0] - 0.65, tip[1] - 2.2, tip[2] - 0.2), (1.3, 2.4, 1.7), pivot=tip,
                      rotation=(-20, 0, 0), paint=r["flag"])
        parts += [right, left, *am.pair(rib),
                  am.span("rib_centre", (-0.3, -6.9, -5.6), (0.3, -0.75, -5.25), skip="back", paint=r["rib"]),
                  stalk, bend, flag]
    return parts


# ---------------------------------------------------------------- chestplate
def body(r):
    """The cuirass, 0.85 off the body, from 0.65 above the shoulders into the lower waist hoop; the shield 0.5 proud
    of its front in three steps narrowing to its foot, the cross 0.4 proud of the shield (its bar 0.15 behind the
    upright's front); a plain back plate with a spine ridge; two hoops round the waist, the lower one wider."""
    return [
        am.span("cuirass", (-4.85, -0.65, -2.85), (4.85, 10.75, 2.85), skip="bottom", paint=r["cuirass"]),
        am.span("shield", (-3.6, 0.35, -3.35), (3.6, 6.0, -2.85), skip="back", paint=r["shield"]),
        am.span("shield_waist", (-2.85, 6.0, -3.35), (2.85, 7.75, -2.85), skip=("back", "top"), paint=r["shield"]),
        am.span("shield_foot", (-1.6, 7.75, -3.35), (1.6, 9.0, -2.85), skip=("back", "top"), paint=r["shield_foot"]),
        am.span("cross_upright", (-0.5, 0.85, -3.75), (0.5, 8.5, -3.35), skip="back", paint=r["cross"]),
        am.span("cross_bar", (-2.75, 2.6, -3.6), (2.75, 3.6, -3.35), skip="back", paint=r["cross"]),
        am.span("back_plate", (-3.25, 0.5, 2.85), (3.25, 8.75, 3.35), skip="front", paint=r["back_plate"]),
        am.span("spine", (-0.5, 0.75, 3.35), (0.5, 8.5, 3.6), skip="front", paint=r["spine"]),
        am.span("fauld_0", (-5.0, 8.75, -3.1), (5.0, 10.0, 3.1), skip="bottom", paint=r["fauld"]),
        am.span("fauld_1", (-5.2, 9.75, -3.3), (5.2, 11.1, 3.3), paint=r["fauld"]),
    ]


def arm(r):
    """The right arm (arm space: x -3..1, y -2..10, z -2..2): the square pauldron, 1.6 past the arm's outer side and
    1.25 above it, a dome on top and the spiral boss 0.5 proud of its outer side; two lames under it hinged at the
    inner edge, fanning down and out; the mail sleeve, 0.45 off the arm and 0.6 off its front and back; the elbow
    band, 0.7 off, and a fan plate on its outer side; the vambrace, 0.6 off, to 0.45 below the hand; a cuff ring round
    the wrist, 1.0 off; the glove, 1.2 off its front and back. Fronts and backs keep 0.15 or more off the cuirass's,
    the shield's, the cross's, the hoops' and the belt's planes and the legs' hip bands', which an idle arm keeps."""
    return [
        am.span("pauldron_right", (-4.6, -3.25, -3.15), (1.7, 2.5, 3.15), paint=r["pauldron"]),
        am.span("dome_right", (-4.1, -4.0, -2.65), (1.2, -3.25, 2.65), skip="bottom", paint=r["dome"]),
        am.span("boss_right", (-5.1, -2.25, -2.0), (-4.6, 1.5, 2.0), skip="left", paint=r["boss"]),
        *am.lames("paul_lame_right", origin=(-4.85, 2.25, -3.55), size=(6.3, 1.5, 7.1), count=2,
                  step=(-0.25, 1.2, 0.0), grow=(0.25, 0.0, 0.3), flare=5, flare_step=5, edge="left",
                  toward="bottom", paint=r["paul_lame"]),
        am.span("sleeve_right", (-3.45, 2.0, -2.6), (1.45, 5.5, 2.6), skip=("top", "bottom"), paint=r["sleeve"]),
        am.span("couter_right", (-3.8, 5.5, -2.7), (1.8, 6.75, 2.7), paint=r["couter"]),
        am.span("wing_right", (-4.3, 4.6, -1.9), (-3.8, 7.6, 1.9), skip="left", paint=r["wing"]),
        am.span("vambrace_right", (-3.6, 6.75, -2.45), (1.6, 10.45, 2.45), skip=("top", "bottom"),
                paint=r["vambrace"]),
        am.span("cuff_right", (-4.0, 9.35, -3.05), (2.0, 10.6, 3.05), paint=r["cuff"]),
        am.span("glove_right", (-3.85, 10.45, -3.2), (1.85, 11.5, 3.2), paint=r["glove"]),
    ]


# ---------------------------------------------------------------- leggings
LEFT_OUT = 0.12   # the left leg's parts stand this much further out, so the two legs' never share a plane where they
                  # overlap at the centre line


def waist(r, kind):
    """The belt, 0.6 off the body's front and back and 0.45 off its sides, closed underneath 0.65 below it; its
    buckle in front, 1.15 off the body (clear of vanilla leggings' 1.0 shell and the hip bands' planes; the Paladin's
    with a gem)."""
    out = [am.span("belt", (-4.45, 10.25, -2.6), (4.45, 12.65, 2.6), paint=r["belt"]),
           am.span("buckle", (-1.25, 10.75, -3.15), (1.25, 12.35, -2.6), skip="back", paint=r["buckle"])]
    if kind == "paladin":
        out.append(am.span("buckle_gem", (-0.5, 11.15, -3.45), (0.5, 11.95, -3.15), skip="back", paint=r["gem"]))
    return out


def leg(r, kind):
    """The right leg (leg space: x -2..2, y 0..12, z -2..2): a cloth band round the top of the leg, 0.77 off it, from
    up under the belt (so a leg swung back shows cloth at the hip, not the wearer); the cloth round the leg, 0.45 off
    it, from inside that band to the knee, and three flaps (before, behind and outside it) hinged out 7 degrees from under the tassets, to below
    the knee; the front tasset, a plate hinged 10 degrees out from the hip, its raised spiral boss (and the Paladin's
    gem) riding it, and the side tasset hinged out over the thigh's outer side. The left leg's are mirrored and stand
    LEFT_OUT further out (model())."""
    tasset = am.hinge(am.span("tasset_right", (-2.6, -0.45, -3.3), (1.3, 5.75, -2.8), paint=r["tasset"]), "top", 10)
    out = [am.span("hip_right", (-2.77, -0.45, -2.77), (2.77, 1.0, 2.77), paint=r["hip"]),
           am.span("underskirt_right", (-2.45, 0.8, -2.45), (2.45, 7.5, 2.45), paint=r["underskirt"]),
           am.hinge(am.span("flap_front_right", (-2.3, 1.0, -2.85), (1.0, 7.75, -2.6), paint=r["flap"]), "top", 7),
           am.hinge(am.span("flap_back_right", (-2.3, 1.0, 3.15), (1.0, 8.4, 3.4), paint=r["flap"]), "top", 7),
           am.hinge(am.span("flap_side_right", (-2.85, 1.0, -1.9), (-2.6, 8.1, 1.9), paint=r["flap"]), "top", 7),
           tasset,
           riding(am.span("tasset_boss_right", (-1.75, 0.9, -3.65), (0.75, 3.6, -3.3), skip="back",
                          paint=r["tasset_boss"]), tasset),
           am.hinge(am.span("tasset_side_right", (-3.3, -0.45, -2.15), (-2.8, 5.25, 2.15), paint=r["tasset"]), "top",
                    10)]
    if kind == "paladin":
        out.append(riding(am.span("tasset_gem_right", (-1.0, 1.75, -3.9), (0.0, 2.75, -3.65), skip="back",
                                  paint=r["gem"]), tasset))
    return out


# ---------------------------------------------------------------- boots
def boot(r):
    """The right boot: the greave from below the knee, 0.75 off the leg (clear of vanilla leggings' 0.5 shell), a knee
    cop on its front; the sabaton, 0.95 off and 1.05 behind, longer at the toe and closed 0.65 below the foot; two
    instep lames overlapping toward the toe, each drooping at its front; a toe cap."""
    out = [am.span("greave_right", (-2.75, 7.0, -2.75), (2.75, 11.0, 2.75), skip="bottom", paint=r["greave"]),
           am.diamond("poleyn_right", (-0.4, 8.1, -3.1), 2.4, 0.6, pitch=12, paint=r["poleyn"]),
           am.span("sabaton_right", (-2.95, 11.0, -3.6), (2.95, 12.65, 3.05), paint=r["sabaton"]),
           am.span("toe_right", (-1.6, 11.45, -4.2), (1.6, 12.35, -3.6), skip="back", paint=r["toe"])]
    for i in range(2):   # instep lames, scales overlapping toward the toe
        x0 = -3.1 - 0.15 * i   # 0.15 past the sabaton's sides, and each 0.15 past the one above
        out.append(am.span(f"instep_right_{i}", (x0, 10.2 + 0.55 * i, -3.45 - 0.5 * i),
                           (-x0, 11.1 + 0.55 * i, -1.7 - 0.5 * i),
                           pivot=(0, 10.2 + 0.55 * i, -1.7 - 0.5 * i), rotation=(12, 0, 0), paint=r["instep"]))
    return out


# ---------------------------------------------------------------- the sets
def left(parts):
    """The left leg's parts: mirrored, LEFT_OUT larger all round."""
    return [replace(p, inflate=LEFT_OUT) for p in am.mirror_all(parts)]


def model(kind):
    """{item: {bone: [parts]}} for one knight's four pieces."""
    r = paints(kind)
    a, l, b = arm(r), leg(r, kind), boot(r)
    return {
        f"{kind}_helmet": {"head": helmet(r, kind)},
        f"{kind}_chestplate": {"body": body(r), "right_arm": a, "left_arm": am.mirror_all(a)},
        f"{kind}_leggings": {"body": waist(r, kind), "right_leg": l, "left_leg": left(l)},
        f"{kind}_boots": {"right_leg": b, "left_leg": left(b)},
    }


SETS = [am.ArmorSet("paladin", PALADIN, model("paladin")), am.ArmorSet("templar", TEMPLAR, model("templar"))]
