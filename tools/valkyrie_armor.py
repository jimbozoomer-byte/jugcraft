"""Valkyrie: the owner's white and gold winged design (the second of the four designs they sent on 7 October 2026, a sheet
of the four on a blank mannequin and a screenshot of each worn in game) as 3D worn models for jugcraft:valkyrie_helmet,
_chestplate, _leggings and _boots, on the toolkit in tools/armor_models.py (shapes) and tools/armor_paint.py (the
atlas).

What the owner drew, and where it is here:
    helmet      no helm: a gold laurel wreath round the head with big leaves over the brow and a curled boss at each
                front corner, and a white feathered wing rising up and back from each temple, tinted pink and lilac,
                its feathers stepping out to a ragged edge; the wearer's face shows. Here: the wreath, four bars round the
                head 0.65 off it (0.15 off the hat layer), its leaves standing on it (a broad pair over the brow, two
                swept back along each side), the bosses at the front corners; each wing a fan of four feather planks
                from the temple, the highest the longest, rolled out from the head and swept back
    chestplate  a white muscle cuirass, the chest and the stomach's ridges in mauve and blue-grey; brown straps over the
                shoulders, buckled gold on the chest; red cloth wound round each shoulder in bands, its ends fluttering
                out, and red ribbons hanging behind the arm past the hand; dark red cloth on the upper arm; a bracer on
                each forearm, gold bands round a white panel. Here: the cuirass; the straps and their buckles; on each
                arm three wound bands (each rising a little toward the outside), a ribbon end standing out from the top
                one, two streamers hanging behind the arm, the red sleeve, the bracer
    leggings    a skirt of brown leather strips with gold studs along their ends over the thighs, white linen strips
                hanging at its front, a brown belt studded gold. Here: the belt on the body, and on each leg the strip
                skirt to mid-thigh, its front inner strips white
    boots       a greave round the shin, gold bands round a white band, a small white wing at its outer side; the feet
                bare. Here: the greave and, on its outer side, a fan of three feather planks swept back
Colours: armor_paint.VALKYRIE: ivory, cream and beige plate shaded blue-grey and mauve; three golds; the red cloth from a
lit coral to a wine; the brown leather; the feathers' pink, lilac and violet.

The sheet shows the set from the front, a little above and to the left, the screenshot from the front; they differ
only in the boots' wings, which the screenshot shows. The back is drawn in the design's own words: the wreath runs round
the head, the cuirass's back is white with a spine line, the straps cross the shoulders to buckles behind, the skirt's
strips and studs run all round. The face and the feet are left open, as drawn: the wreath, the wings and the greave
are rings and fans, every box of them closed, so nothing shows through them alone or on an armor stand
(tools/art_check.py, H1). A box is its own plate; the muscle lines, strips, studs and feather tints are paint, one texel
per model pixel.

Space (tools/armor_models.py): each bone's own space in model pixels; x is the model's LEFT, y is DOWN, z is the BACK
(the face is at -z). Right-hand parts are built and mirrored to the left.
Preview: python3 tools/armor_preview.py --set valkyrie
"""
from dataclasses import replace

import armor_models as am
from armor_paint import VALKYRIE, P


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


# The palette's names (armor_paint.VALKYRIE).
W, C, B, G, M, V = "light", "mid_light", "mid", "dark", "seam", "void"           # ivory, cream, beige, blue-grey, mauve
Y, y, o = "gold_light", "gold_mid", "gold_dark"                                  # gold, light to dark
R, r, q, Q, K = "leather_light", "leather_mid_light", "leather_mid", "leather_dark", "leather_darkest"   # red cloth
T, t, n, N = "under_light", "under_mid", "under_dark", "under_darkest"           # brown leather
FP, FL, FV = "feather_pink", "feather_lilac", "feather_violet"                   # the feathers' tints


def feather(*tones):
    """A feather plank's broad faces: barbs across it, one tone per texel column from its root, as the owner's wings
    mix white with pink and lilac; its thin edges white, its root and tip ends cream."""
    return {"right": cols(*tones, width=12), "left": cols(*tones, width=12), "top": solid(W), "bottom": solid(C),
            "*": solid(C)}


# ---------------------------------------------------------------- paint
def paints():
    """Role -> paint spec, the owner's patterns read off the sheet and the screenshot."""
    return {
        # the wreath: gold, lit along the top; its leaves gold with a darker vein; the bosses a gold coil round a dark eye
        "wreath": {"top": solid(Y), "bottom": solid(o), "*": rows(Y, y)},
        "leaf": {"front": [solid(Y), marks((1, 0, 1, 99, y), (0, -1, 99, 1, o))], "back": [solid(y), marks((1, 0, 1, 99, o))],
                 "top": solid(Y), "*": solid(y)},
        "boss": {"front": [solid(Y), marks((0, -1, 99, 1, y), (-1, 0, 1, 99, y), (1, 1, 1, 1, N))], "top": solid(Y),
                 "*": [solid(y), marks((0, -1, 99, 1, o))]},
        # the wings' feathers, each plank its own mix of white, pink and lilac; the lowest shaded violet at the root
        "feather_0": feather(W, W, FP, W, FL, W, W, FP, W, W, FL, W),
        "feather_1": feather(C, W, FL, W, W, FP, W, FL, W, FP, W, W),
        "feather_2": feather(FP, W, W, FL, FP, W, W, FL, W, W),
        "feather_3": feather(FV, FL, W, FP, W, FL, W),
        # the cuirass: ivory, the chest's two plates and the stomach's ridges between mauve and blue-grey lines, a mauve
        # line down the middle and under the chest; its back white with a spine line
        "cuirass": {"front": [solid(W), marks((0, 0, 99, 1, C), (1, 1, 3, 2, C), (6, 1, 3, 2, C), (0, 3, 99, 1, M),
                                              (2, 4, 2, 1, B), (6, 4, 2, 1, B), (0, 5, 99, 1, G), (2, 6, 2, 1, B),
                                              (6, 6, 2, 1, B), (0, 7, 99, 1, G), (2, 8, 2, 1, C), (6, 8, 2, 1, C),
                                              (4, 1, 2, 99, M), (0, -2, 99, 1, C), (0, -1, 99, 1, B))],
                     "back": [solid(W), marks((4, 0, 2, 99, G), (0, -2, 99, 1, C), (0, -1, 99, 1, B))],
                     "top": solid(W), "bottom": solid(B), "*": [rows(W, W, C, W, C, B), marks((0, -1, 99, 1, B))]},
        # the straps: brown leather, lit along the top; their buckles gold
        "strap": {"top": solid(T), "*": [solid(t), marks((0, 0, 99, 1, T), (0, 0, 1, 99, n))]},
        "buckle": {"front": [solid(Y), marks((1, 1, 1, 1, N), (0, -1, 99, 1, o))], "back": [solid(Y), marks((1, 1, 1, 1, N))],
                   "*": solid(y)},
        # the red cloth: each band lit along its top edge, a darker fold under it; the ends and streamers the same red
        "wrap_0": {"top": solid(R), "bottom": solid(Q), "*": rows(R, r, q)},
        "wrap_1": {"top": solid(r), "bottom": solid(K), "*": rows(r, q, Q)},
        "wrap_2": {"top": solid(r), "bottom": solid(K), "*": rows(R, q, Q)},
        "ribbon": {"top": solid(R), "*": [solid(r), marks((0, 0, 99, 1, R))]},
        "streamer": {"top": solid(r), "bottom": solid(Q), "sides": rows(r, r, q, r, q, Q)},
        "sleeve": {"top": solid(q), "*": rows(Q, q, Q, K)},
        # the bracer: a gold band at each end of a white panel checked blue-grey, as drawn
        "bracer": {"top": solid(Y), "bottom": solid(o),
                   "*": [solid(W), marks((0, 0, 99, 1, Y), (0, 1, 99, 1, y), (1, 2, 1, 1, G), (4, 2, 1, 1, G),
                                         (2, 3, 1, 1, C), (5, 3, 1, 1, G), (0, 4, 99, 1, C), (0, -2, 99, 1, y),
                                         (0, -1, 99, 1, o))]},
        # the belt: brown leather studded gold
        "belt": {"top": solid(t), "bottom": solid(N),
                 "*": [solid(n), marks((0, 0, 99, 1, t), (1, 1, 1, 1, Y), (4, 1, 1, 1, Y), (7, 1, 1, 1, Y))]},
        # the skirt: brown leather strips with dark gaps and a gold stud near each strip's end; on the front, its two
        # strips nearest the centre line white linen
        "skirt": {"front": [cols(t, n, t, n, N, n, t, n), marks((0, 0, 99, 1, T), (-2, 1, 2, 99, W), (-2, 3, 1, 99, C),
                                                                (1, -2, 1, 1, Y), (4, -2, 1, 1, Y), (0, -1, 4, 1, N))],
                  "back": [cols(t, n, N, n, t, n), marks((0, 0, 99, 1, T), (1, -2, 1, 1, Y), (4, -2, 1, 1, Y),
                                                         (0, -1, 99, 1, N))],
                  "top": solid(t), "bottom": solid(N),
                  "*": [cols(t, n, N, n, t, n), marks((0, 0, 99, 1, T), (2, -2, 1, 1, Y), (0, -1, 99, 1, N))]},
        # the greave: a gold band at its top and foot, two white rows checked blue-grey between, as drawn
        "greave": {"top": solid(Y), "bottom": solid(o),
                   "sides": [solid(W), marks((0, 0, 99, 1, Y), (1, 1, 1, 1, G), (4, 1, 1, 1, G), (2, 2, 1, 1, C),
                                             (5, 2, 1, 1, G), (0, -1, 99, 1, y))]},
    }


# ---------------------------------------------------------------- shape helpers
def plank(name, root, length, width, thick, angle, x_in, paint):
    """A feather plank on the model's right: from `root` (bone space: the middle of its root end's inner face) it runs
    `length` px backward (+z), `width` tall and `thick` deep (outward, -x, from x_in), turned up `angle` degrees about x
    at the root, so it rises back. Turned further as a wing by the caller."""
    rx, ry, rz = root
    return am.box(name, (x_in - thick, ry - width / 2, rz), (thick, width, length), pivot=(x_in, ry, rz),
                  rotation=(angle, 0, 0), paint=paint)


def wing(name, root, feathers, roll, sweep, paint_of):
    """A fan of feather planks from `root`, each (length, width, angle), the first the outermost: each stands 0.15 further
    out than the next so their broad faces never share a plane; then the fan is rolled `roll` degrees (tops out, away
    from the head) and swept `sweep` degrees about the root, which swings its back out until, at 60 or so, its broad
    face looks forward and out, as the owner's wings do."""
    out = []
    rx, ry, rz = root
    for i, (length, width, angle) in enumerate(feathers):
        x_in = rx - 0.15 * (len(feathers) - 1 - i)
        part = plank(f"{name}_{i}_right", (rx, ry, rz), length, width, 0.6, angle, x_in, paint_of(i))
        part = am.turned(part, "z", -roll, root)
        out.append(am.turned(part, "y", -sweep, root))
    return out


# ---------------------------------------------------------------- helmet
def helmet(r):
    """The wreath: four bars round the head at the brow, 0.65 off it (0.15 off the hat layer), the side bars between the
    front and back ones; over the brow a broad pair of leaves standing on it, leaning out, and a smaller pair over
    them; two leaves swept back along each side; a boss at each front corner. Behind each side's first leaf a wing of
    five feathers rises from the temple, rolled 20 degrees out and swept 58 degrees round, so that it spreads out to the
    side and up with its broad face forward, the highest feather the longest and the feathers overlapping to their
    ragged tips. The face, the crown and the back of the head are open, as the owner drew them."""
    parts = [
        am.span("wreath_front", (-5.15, -7.85, -5.15), (5.15, -6.6, -4.65), paint=r["wreath"]),
        am.span("wreath_back", (-5.15, -7.85, 4.65), (5.15, -6.6, 5.15), paint=r["wreath"]),
    ]
    side = am.span("wreath_right", (-5.15, -7.85, -4.65), (-4.65, -6.6, 4.65), skip=("front", "back"), paint=r["wreath"])
    # over the brow: the broad leaf on each side of the centre line, on the wreath's front and rolled out from its
    # inner foot; a smaller one above it, leaning out further
    leaf = am.box("leaf_right", (-3.9, -9.6, -5.75), (2.9, 2.5, 0.6), pivot=(-1.0, -7.1, -5.45), rotation=(0, 0, -14),
                  paint=r["leaf"])
    leaf_top = am.box("leaf_top_right", (-3.0, -10.1, -5.55), (1.5, 1.4, 0.5), pivot=(-1.5, -8.9, -5.3),
                      rotation=(0, 0, -30), paint=r["leaf"])
    boss = am.span("boss_right", (-5.6, -8.85, -5.9), (-3.6, -6.35, -4.8), paint=r["boss"])
    # along each side, two leaves swept back and up from the wreath's outer face
    leaves = [am.box(f"leaf_side_{i}_right", (-5.75, -8.6, z), (0.6, 1.25, 2.4), pivot=(-5.45, -7.6, z),
                     rotation=(-24, 0, 0), paint=r["leaf"]) for i, z in enumerate((-2.9, 1.6))]
    wings = wing("feather", (-5.4, -7.6, -0.6), [(7.0, 2.0, 78), (6.8, 2.0, 63), (6.2, 2.0, 48), (5.3, 2.0, 33),
                                                (4.2, 2.0, 18)],
                 roll=20, sweep=58, paint_of=lambda i: r[f"feather_{min(i, 3)}"])
    for part in [side, leaf, leaf_top, boss] + leaves + wings:
        parts += am.pair(part)
    return parts


# ---------------------------------------------------------------- chestplate
def body(r):
    """The cuirass, 0.85 off the body, from 0.65 above the shoulders to the waist; a brown strap over each shoulder
    from a gold buckle on the chest to one on the back, 0.25 proud of the cuirass, near enough the neck that the
    shoulder's wound cloth never meets it."""
    parts = [am.span("cuirass", (-4.85, -0.65, -2.85), (4.85, 10.75, 2.85), paint=r["cuirass"])]
    strap = am.span("strap_right", (-2.9, -0.95, -3.1), (-1.6, 2.25, 3.1), paint=r["strap"])
    buckle = am.span("buckle_right", (-2.75, 1.5, -3.35), (-1.75, 2.75, 3.35), paint=r["buckle"])
    return parts + am.pair(strap) + am.pair(buckle)


def arm(r):
    """The right arm (arm space: x -3..1, y -2..10, z -2..2): three bands of red cloth wound round the shoulder, each
    rising a little toward the outside, the top one's end standing out from it; two streamers hanging from the back
    of the shoulder past the hand; the red sleeve on the upper arm, 0.45 off it; the bracer on the forearm, 0.7 off it,
    its gold bands at each end. The fronts and backs keep 0.15 or more off the cuirass's plane, which an idle arm
    keeps."""
    wraps = [
        am.box("wrap_0_right", (-4.0, -3.0, -3.0), (5.6, 1.6, 6.0), pivot=(1.6, -1.4, 0), rotation=(0, 0, 10),
               paint=r["wrap_0"]),
        am.box("wrap_1_right", (-4.15, -1.5, -3.2), (5.75, 1.75, 6.4), pivot=(1.6, 0.25, 0), rotation=(0, 0, 6),
               paint=r["wrap_1"]),
        am.box("wrap_2_right", (-3.95, 0.25, -3.05), (5.55, 1.75, 6.1), pivot=(1.6, 2.0, 0), rotation=(0, 0, 3),
               paint=r["wrap_2"]),
    ]
    end = am.box("ribbon_right", (-4.6, -5.9, -0.75), (0.75, 3.0, 0.75), pivot=(-4.25, -2.9, -0.4),
                 rotation=(-10, 0, -62), paint=r["ribbon"])
    return wraps + [
        end,
        am.span("streamer_right", (-3.3, 1.5, 3.25), (-2.55, 12.75, 3.95), paint=r["streamer"]),
        am.span("streamer_short_right", (-1.9, 1.5, 3.25), (-1.15, 9.75, 3.95), paint=r["streamer"]),
        am.span("sleeve_right", (-3.45, 1.75, -2.45), (1.45, 4.5, 2.45), skip="bottom", paint=r["sleeve"]),
        am.span("bracer_right", (-3.7, 4.5, -2.7), (1.7, 9.25, 2.7), paint=r["bracer"]),
    ]


# ---------------------------------------------------------------- leggings
LEFT_OUT = 0.12   # the left leg's parts stand this much further out, so the two legs' never share a plane where they
                  # overlap at the centre line


def waist(r):
    """The brown belt studded gold, 0.6 off the body's front and back and 0.45 off its sides (clear of vanilla armor's
    shells and of the skirt's planes), closed underneath 0.65 below the body."""
    return [am.span("belt", (-4.45, 10.25, -2.6), (4.45, 12.65, 2.6), paint=r["belt"])]


def leg(r):
    """The right leg's strip skirt, 0.77 off the leg, from the hip, up under the belt, to mid-thigh; its foot left
    open below the knee as drawn. The left leg's is mirrored and stands LEFT_OUT further out (model())."""
    return [am.span("skirt_right", (-2.77, -0.45, -2.77), (2.77, 6.0, 2.77), paint=r["skirt"])]


# ---------------------------------------------------------------- boots
def boot(r):
    """The right boot: the greave round the shin, 0.75 off it (clear of vanilla leggings' 0.5 shell), from below the
    knee to above the ankle; on its outer side a wing of three feathers swept back and up, rolled out a little."""
    greave = am.span("greave_right", (-2.75, 6.75, -2.75), (2.75, 10.5, 2.75), paint=r["greave"])
    feathers = wing("heel", (-2.9, 8.6, 0.6), [(3.6, 1.1, 34), (3.1, 1.1, 18), (2.5, 1.1, 4)], roll=14, sweep=24,
                    paint_of=lambda i: r[f"feather_{i + 1}"])
    return [greave] + feathers


# ---------------------------------------------------------------- the set
def model():
    """{item: {bone: [parts]}} for the four pieces."""
    r = paints()
    a, l, b = arm(r), leg(r), boot(r)
    return {
        "valkyrie_helmet": {"head": helmet(r)},
        "valkyrie_chestplate": {"body": body(r), "right_arm": a, "left_arm": am.mirror_all(a)},
        "valkyrie_leggings": {"body": waist(r), "right_leg": l,
                              "left_leg": [replace(p, inflate=LEFT_OUT) for p in am.mirror_all(l)]},
        "valkyrie_boots": {"right_leg": b, "left_leg": [replace(p, inflate=LEFT_OUT) for p in am.mirror_all(b)]},
    }


SETS = [am.ArmorSet("valkyrie", VALKYRIE, model())]
