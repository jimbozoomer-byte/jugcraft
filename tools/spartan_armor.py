"""Spartan: the owner's gold plumed design (the fourth of the four designs they sent on 7 October 2026, a sheet of the
four on a blank mannequin and a screenshot of each worn in game) as 3D worn models for jugcraft:spartan_helmet,
_chestplate, _leggings and _boots, on the toolkit in tools/armor_models.py (shapes) and tools/armor_paint.py (the
atlas).

What the owner drew, and where it is here:
    helmet      a gold Corinthian helm, its face cut in a T (a slit across the eyes, a gap down to the chin between the
                cheek guards), the face showing through it; a crest of red and orange horsehair running from the brow
                over the crown and falling down the back of the head to the shoulders. Here: the bowl down to the eye
                line, a jaw block behind the face, a temple block at each end of the eye slit and a cheek guard each
                side of the gap, all 1.0 off the head; the crest's four blocks along the top, stepped to a ragged ridge
                with tufts, and its tail hanging behind
    chestplate  a gold muscle cuirass (the chest's two plates, the stomach's ridges, bronze lines between); red cloth
                over the right shoulder and across the top of the chest, falling behind as a cape to the knee on that
                side; a gold pauldron on the left shoulder with a curled scroll on its outer face; gold bracers on the
                forearms; the upper arms bare. Here: the cuirass; the red mantle over the right half of the chest's top
                and the cape down the right half of the back; on the right arm the red drape over the shoulder and the
                upper arm, the cape's tail behind the right thigh (on the leg, so it follows it); on the left arm the
                pauldron and its scroll; a bracer on each forearm
    leggings    a skirt of brown leather strips (pteruges) studded gold at their ends over the thighs, a brown belt.
                Here: the belt on the body and on each leg the strip skirt to mid-thigh; the knees bare
    boots       gold greaves from the knee, a pale gold cap over each knee, bands round the shin. Here: the greave, the
                knee cap standing 0.4 proud of it, and a brown sandal sole under it
Colours: armor_paint.SPARTAN: the gold from a pale lit gold through the warm golds of the plates to the bronze browns
of their lines and edges; the plume's oranges and reds and the cape's wine; the brown leather.

The sheet shows the set from the front, a little above and to the left, the screenshot from the front. The back is
drawn in the design's own words: the helm is gold all round behind its crest, the cuirass's back carries a spine line
and shoulder blades, the cape hangs down the right half of the back, the skirt's strips and studs run all round. The
face and the upper arms are left open, as drawn; every box is closed, so nothing shows through them alone or on an
armor stand (tools/art_check.py, H1). A box is its own plate; the muscle lines, the scroll, the strips, the studs and
the plume's locks are paint, one texel per model pixel.

Space (tools/armor_models.py): each bone's own space in model pixels; x is the model's LEFT, y is DOWN, z is the BACK
(the face is at -z). Right-hand parts are built and mirrored to the left; the left arm's pauldron has no right twin.
Preview: python3 tools/armor_preview.py --set spartan
"""
from dataclasses import replace

import armor_models as am
from armor_paint import SPARTAN, P


def rows(*tones):
    """One tone per texel row, cycling, each row broken into runs of the next tone darker."""
    return P("lames", rows=1, tones=tuple((t, t) for t in tones), centre=None)


def solid(tone):
    return P("solid", tone=tone)


def marks(*rects):
    return P("marks", rects=list(rects))


def cols(*tones, width=8):
    """Upright strips, one tone per texel column, cycling."""
    return marks(*[(i, 0, 1, 99, tones[i % len(tones)]) for i in range(width)])


# The palette's names (armor_paint.SPARTAN).
H, L, M, D, S, V = "light", "mid_light", "mid", "dark", "seam", "void"            # pale gold to bronze brown
O, o, PY = "gold_light", "gold_dark", "plume_yellow"                               # the plume's oranges, its lit tips
R, r, q, Q, K = "leather_light", "leather_mid_light", "leather_mid", "leather_dark", "leather_darkest"   # reds
T, t, n, N = "under_light", "under_mid", "under_dark", "under_darkest"            # brown leather


def locks(*tones):
    """The plume's locks: upright runs of oranges and reds, one tone per texel column, cycling."""
    return cols(*tones, width=12)


# ---------------------------------------------------------------- paint
def paints():
    """Role -> paint spec, the owner's patterns read off the sheet and the screenshot."""
    return {
        # the helm: warm gold, bronze lines along its brow, round the eye slit and the cheek guards
        "bowl": {"front": [solid(M), marks((0, 0, 99, 1, L), (1, 1, 3, 1, H), (-4, 1, 3, 1, H), (0, 2, 99, 1, D),
                                           (4, 3, 3, 99, L), (0, -1, 99, 1, S))],
                 "top": [solid(L), marks((0, 0, 99, 1, H))], "bottom": solid(V),
                 "*": [rows(M, L, M, D), marks((0, 0, 99, 1, H), (0, -1, 99, 1, S))]},
        "jaw": {"front": solid(V), "bottom": solid(S),
                "*": [rows(M, M, D, M, L, M, D, S), marks((0, 0, 99, 1, D))]},
        "temple": {"front": [solid(L), marks((0, 0, 99, 1, H), (-1, 0, 1, 99, S))], "*": solid(D)},
        "cheek": {"front": [solid(M), marks((0, 0, 99, 1, H), (-1, 0, 1, 99, S), (1, 1, 2, 3, L), (0, -1, 99, 1, S))],
                  "*": [solid(D), marks((0, 0, 99, 1, M))]},
        # the crest: bright orange and red locks, lit tips; its tail darker, wine at the foot
        "crest_0": {"top": solid(PY), "*": locks(O, R, O, o, R, O, r)},
        "crest_1": {"top": solid(O), "*": locks(R, O, o, R, r, O, R)},
        "crest_2": {"top": solid(o), "*": locks(o, R, r, o, R, q)},
        "crest_3": {"top": solid(R), "*": locks(R, r, q, r, Q)},
        "tuft": {"top": solid(PY), "*": solid(O)},
        "tail": {"top": solid(R), "bottom": solid(K), "*": [locks(R, r, q, r, Q, q), marks((0, -1, 99, 1, Q))]},
        # the cuirass: the chest's two plates and the stomach's ridges pale gold, bronze lines between, a line down
        # the middle; behind, the shoulder blades and the spine
        "cuirass": {"front": [solid(M), marks((0, 0, 99, 1, L), (1, 1, 3, 2, H), (6, 1, 3, 2, H), (0, 3, 99, 1, D),
                                              (2, 4, 2, 1, L), (6, 4, 2, 1, L), (0, 5, 99, 1, D), (2, 6, 2, 1, L),
                                              (6, 6, 2, 1, L), (0, 7, 99, 1, S), (2, 8, 2, 1, M), (6, 8, 2, 1, M),
                                              (4, 1, 2, 99, D), (0, -2, 99, 1, L), (0, -1, 99, 1, S))],
                     "back": [solid(M), marks((1, 1, 3, 3, L), (6, 1, 3, 3, L), (4, 0, 2, 99, D), (0, -2, 99, 1, L),
                                              (0, -1, 99, 1, S))],
                     "top": solid(L), "bottom": solid(S), "*": [rows(M, L, M, D), marks((0, -1, 99, 1, S))]},
        # the red cloth: the mantle and drape lit along their tops, darker folds; the cape a wine red
        "mantle": {"top": solid(R), "bottom": solid(Q), "sides": [rows(r, q, r, Q), marks((0, 0, 99, 1, R))]},
        "sash": {"front": [rows(r, q), marks((0, 0, 99, 1, R))], "*": solid(Q)},
        "drape": {"top": solid(R), "bottom": solid(K), "sides": [rows(r, r, q, Q, q, Q), marks((0, 0, 99, 1, R))]},
        "fold": {"top": solid(q), "bottom": solid(K), "sides": [cols(Q, q, Q, K, Q), marks((0, -1, 99, 1, K))]},
        "cape": {"top": solid(q), "bottom": solid(K), "sides": [cols(Q, q, Q, Q, K, Q), marks((0, -1, 99, 1, K))]},
        # the pauldron: gold, the scroll on its outer face a bronze spiral round a pale heart; the scroll's knob gold
        "pauldron": {"left": [solid(L), marks((0, 0, 99, 1, H), (1, 1, 4, 1, S), (1, 1, 1, 4, S), (1, 4, 4, 1, S),
                                              (4, 2, 1, 3, S), (2, 2, 2, 1, D), (2, 3, 1, 1, H), (0, -1, 99, 1, S))],
                     "top": [solid(L), marks((0, 0, 99, 1, H))], "bottom": solid(S),
                     "*": [rows(L, M, M, D), marks((0, 0, 99, 1, H), (0, -1, 99, 1, S))]},
        "scroll": {"top": solid(H), "bottom": solid(S), "*": [solid(L), marks((0, 0, 99, 1, H), (0, -1, 99, 1, D))]},
        # the bracers: gold bands, the top one lit, bronze between
        "bracer": {"top": solid(H), "bottom": solid(S), "sides": [rows(H, L, M, L, M, D), marks((0, -1, 99, 1, S))]},
        # the belt: brown leather studded gold; the skirt's strips, a gold stud near each strip's end
        "belt": {"top": solid(t), "bottom": solid(N),
                 "sides": [solid(n), marks((0, 0, 99, 1, t), (1, 1, 1, 1, L), (4, 1, 1, 1, L), (7, 1, 1, 1, L))]},
        "skirt": {"front": [cols(t, n, t, n, N, n), marks((0, 0, 99, 1, T), (1, -2, 1, 1, L), (4, -2, 1, 1, L),
                                                          (0, -1, 99, 1, N))],
                  "back": [cols(t, n, N, n, t, n), marks((0, 0, 99, 1, T), (1, -2, 1, 1, L), (4, -2, 1, 1, L),
                                                         (0, -1, 99, 1, N))],
                  "top": solid(t), "bottom": solid(N),
                  "*": [cols(t, n, N, n, t, n), marks((0, 0, 99, 1, T), (2, -2, 1, 1, L), (0, -1, 99, 1, N))]},
        # the greave: gold, banded down the shin, a bronze band at its foot; the knee cap pale gold; the sandal brown
        "greave": {"top": solid(L), "bottom": solid(S), "sides": [rows(L, M, D, M, L, M, D), marks((0, -1, 99, 1, S))]},
        "knee": {"front": [solid(H), marks((0, -1, 99, 1, L), (-1, 0, 1, 99, L))], "*": solid(L)},
        "sandal": {"top": solid(t), "bottom": solid(N), "sides": [rows(t, n), marks((0, -1, 99, 1, N))]},
    }


# ---------------------------------------------------------------- helmet
def helmet(r):
    """The helm, 1.0 off the head: the bowl from above the head down to the eye line; behind the face the jaw block,
    from the eye line to 0.75 below the head (its front 1.0 inside the head, so it shows only through the T); a temple
    block at each end of the eye slit and a cheek guard each side of the gap, their fronts 1.0 off the face. The T
    shows the face: the slit across the eyes (1.75 tall, 7.5 wide) and the gap below it to the chin (2 wide). On the
    crown, the crest's four blocks from the brow back to behind the head, stepped up to the middle, tufts standing on
    them, and its tail in two locks falling behind the head toward the right shoulder."""
    parts = [
        am.span("bowl", (-5.0, -9.25, -5.0), (5.0, -4.75, 5.0), paint=r["bowl"]),
        am.span("jaw", (-5.0, -4.75, -3.0), (5.0, 0.75, 5.0), skip="top", paint=r["jaw"]),
        am.span("crest_0", (-1.5, -12.5, -5.5), (1.5, -9.25, -2.75), paint=r["crest_0"]),
        am.span("crest_1", (-1.5, -13.25, -2.75), (1.5, -9.25, 0.25), paint=r["crest_1"]),
        am.span("crest_2", (-1.5, -13.0, 0.25), (1.5, -9.25, 3.25), paint=r["crest_2"]),
        am.span("crest_3", (-1.5, -12.0, 3.25), (1.5, -9.25, 5.75), paint=r["crest_3"]),
    ]
    # the tail, falling from the crest's back end behind the head and sweeping toward the right shoulder, as drawn: an
    # upper lock leaning out behind and to the right, and a lower one from its foot, leaning further right
    upper = am.box("tail", (-1.35, -10.75, 5.0), (2.7, 5.5, 1.75), pivot=(0.0, -10.75, 5.0), rotation=(16, 0, 14),
                   paint=r["tail"])
    foot = am.place(upper, (0.0, -5.25 - 0.6, 5.875))
    lower = am.box("tail_low", (foot[0] - 1.15, foot[1], foot[2] - 0.8), (2.3, 4.5, 1.6), pivot=foot,
                   rotation=(6, 0, 34), paint=r["tail"])
    parts += [upper, lower]
    # tufts along the ridge, a little narrower than the crest, so the top reads ragged
    for i, (z, top) in enumerate(((-4.75, -13.25), (-1.5, -14.0), (1.75, -13.75), (4.25, -12.75))):
        parts.append(am.span(f"tuft_{i}", (-1.25, top, z), (1.25, top + 0.75, z + 1.25), skip="bottom", paint=r["tuft"]))
    temple = am.span("temple_right", (-5.0, -4.75, -5.0), (-3.75, -3.0, -3.0), skip=("top", "back"),
                     paint=r["temple"])
    cheek = am.span("cheek_right", (-5.0, -3.0, -5.0), (-1.0, 0.75, -3.0), skip="back", paint=r["cheek"])
    return parts + am.pair(temple) + am.pair(cheek)


# ---------------------------------------------------------------- chestplate
def body(r):
    """The cuirass, 0.85 off the body, from 0.65 above the shoulders to the waist; the red mantle over the right half of
    its top, front and back, 1.15 off (its side 0.25 outside the helm's, which meets it at the neck); on its front the sash, rising across the chest from the right shoulder toward
    the left of the neck, 0.25 proud; the cape down the right half of the back to the waist, 1.0 to 1.45 off, its side
    0.15 outside the helm's (a head looking up swings the helm's side into the cape's plane)."""
    return [
        am.span("cuirass", (-4.85, -0.65, -2.85), (4.85, 10.75, 2.85), paint=r["cuirass"]),
        am.span("mantle", (-5.25, -0.9, -3.15), (1.25, 2.75, 3.15), paint=r["mantle"]),
        am.box("sash", (-4.85, 1.5, -3.4), (6.6, 1.75, 0.25), pivot=(-4.85, 3.25, -3.15), rotation=(0, 0, -16),
               paint=r["sash"]),
        am.span("cape", (-5.15, 2.75, 3.0), (0.75, 12.25, 3.45), paint=r["cape"]),
    ]


def right_arm(r):
    """The right arm (arm space: x -3..1, y -2..10, z -2..2): the red drape over the shoulder and the upper arm, 0.6
    to 1.3 off it (its front and back 0.15 off the mantle's and the cape's planes, which an idle arm keeps), and its
    fold falling lower down the outer and back sides of the upper arm; the bracer on the forearm, 0.7 off it; the
    front of the upper arm below the drape bare, as drawn."""
    return [am.span("drape_right", (-3.9, -2.9, -3.3), (1.6, 2.75, 3.3), paint=r["drape"]),
            am.span("fold_right", (-4.15, 2.0, -1.25), (-0.5, 5.0, 3.55), paint=r["fold"]), bracer(r)]


def bracer(r):
    """The right bracer, 0.7 off the forearm; the left is its mirror."""
    return am.span("bracer_right", (-3.7, 4.5, -2.7), (1.7, 9.25, 2.7), paint=r["bracer"])


def left_arm(r):
    """The left arm (arm space: x -1..3, y -2..10, z -2..2): the gold pauldron, 0.6 to 1.2 off the shoulder (its front
    and back 0.2 off the helm's jaw, which an upright head keeps beside it), its scroll standing out of its outer face
    at the foot; the bracer, the right one's mirror."""
    return [am.span("pauldron_left", (-1.6, -2.9, -3.2), (3.9, 1.75, 3.2), paint=r["pauldron"]),
            am.span("scroll_left", (3.9, -0.25, -1.25), (4.65, 1.5, 1.25), skip="right", paint=r["scroll"]),
            am.mirror(bracer(r))]


def cape_tail(r):
    """The cape's tail behind the right thigh (leg space), 1.15 to 1.6 off the leg's back (0.15 off the cape's planes
    on the body), from above the hip to the knee, on the leg so it follows it; part of the chestplate."""
    return am.span("cape_tail_right", (-2.75, -0.75, 3.15), (2.25, 6.75, 3.6), paint=r["cape"])


# ---------------------------------------------------------------- leggings
LEFT_OUT = 0.12   # the left leg's parts stand this much further out, so the two legs' never share a plane where they
                  # overlap at the centre line


def waist(r):
    """The brown belt studded gold, 0.6 off the body's front and back and 0.45 off its sides, closed underneath 0.65
    below it."""
    return [am.span("belt", (-4.45, 10.25, -2.6), (4.45, 12.65, 2.6), paint=r["belt"])]


def leg(r):
    """The right leg's strip skirt, 0.77 off the leg, from the hip, up under the belt, to mid-thigh; the knee bare.
    The left leg's is mirrored and stands LEFT_OUT further out (model())."""
    return [am.span("skirt_right", (-2.77, -0.45, -2.77), (2.77, 5.5, 2.77), paint=r["skirt"])]


# ---------------------------------------------------------------- boots
def boot(r):
    """The right boot: the greave from below the knee, 0.75 off the leg (clear of vanilla leggings' 0.5 shell) and 1.0
    in front, so the shin stands forward; the knee cap 0.4 proud of it, a little under its top; the sandal sole under
    it, 0.65 below the foot, its heel 0.25 past the greave's back. The heights and depths keep the two legs' planes
    0.1 or more apart, the left standing LEFT_OUT further out."""
    return [am.span("greave_right", (-2.75, 6.25, -3.0), (2.75, 11.5, 2.75), paint=r["greave"]),
            am.span("knee_right", (-1.6, 6.6, -3.4), (1.6, 7.9, -3.0), skip="back", paint=r["knee"]),
            am.span("sandal_right", (-2.6, 11.5, -3.25), (2.6, 12.65, 3.0), paint=r["sandal"])]


# ---------------------------------------------------------------- the set
def model():
    """{item: {bone: [parts]}} for the four pieces."""
    r = paints()
    l, b = leg(r), boot(r)
    return {
        "spartan_helmet": {"head": helmet(r)},
        "spartan_chestplate": {"body": body(r), "right_arm": right_arm(r), "left_arm": left_arm(r),
                               "right_leg": [cape_tail(r)]},
        "spartan_leggings": {"body": waist(r), "right_leg": l,
                             "left_leg": [replace(p, inflate=LEFT_OUT) for p in am.mirror_all(l)]},
        "spartan_boots": {"right_leg": b, "left_leg": [replace(p, inflate=LEFT_OUT) for p in am.mirror_all(b)]},
    }


SETS = [am.ArmorSet("spartan", SPARTAN, model())]
