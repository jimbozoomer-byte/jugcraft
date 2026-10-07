"""Wayfarer: the owner's blue hooded cloak (the third of the four designs they sent on 7 October 2026, a sheet of the
four on a blank mannequin and a screenshot of each worn in game) as 3D worn models for jugcraft:wayfarer_helmet,
_chestplate, _leggings and _boots, on the toolkit in tools/armor_models.py (shapes) and tools/armor_paint.py (the
atlas).

What the owner drew, and where it is here:
    helmet      a deep hood in mottled navy and blue, a little bigger than the head, its face opening edged in light
                teal; the face shows. Here: the hood as closed blocks round the head, 1.25 off it (a top block down to
                the brow, a block down each side and one behind), the opening between them; a teal brim over the
                opening and a teal rim down each side of it, 0.5 proud
    chestplate  a cloak over the shoulders and the upper arms, open down the front over a dark tunic, its front edges
                light teal, a round silver clasp with a teal heart at the chest; it hangs longer on the model's right
                side, to the knee; the forearms bare. Here: the mantle over the shoulders, the tunic, the two front
                panels with their teal edges and the back panel to the waist, the clasp on the left panel; on each arm
                the cloak to the elbow; behind each thigh the cloak's tail, the right one to the knee, the left shorter
                (on the legs, so it follows them)
    leggings    a short kilt of dark brown leather over the thighs, a belt, a row of light studs along its hem. Here: the
                belt on the body and on each leg the kilt to mid-thigh; the knees and shins bare, as drawn
    boots       dark brown boots from mid-shin, a lighter band and two pale laces on the front, and a small wing at each
                ankle, white and ice blue with pink tips. Here: the boot, its cuff and, on its outer side, a fan of three
                feather planks swept back
Colours: armor_paint.WAYFARER: the cloak's blues from a light teal to the darkest navy, the clasp's pale silver, the
dark red-brown leather, the hood's shadowed inside and the feathers' white, ice blue, pink and steel blue.

The sheet shows the set from the front, a little above and to the left, the screenshot from the front. The back is
drawn in the design's own words: the hood and the cloak run round in the same mottled blue, the cloak's tail behind the
thighs, the kilt's studs and the boots' bands all round. The face and the forearms are left open, as drawn; every box
is closed, so nothing shows through them alone or on an armor stand (tools/art_check.py, H1). A box is its own plate;
the mottling, the edges, the studs and the laces are paint, one texel per model pixel.

Space (tools/armor_models.py): each bone's own space in model pixels; x is the model's LEFT, y is DOWN, z is the BACK
(the face is at -z). Right-hand parts are built and mirrored to the left.
Preview: python3 tools/armor_preview.py --set wayfarer
"""
from dataclasses import replace

import armor_models as am
from armor_paint import WAYFARER, P


def rows(*tones):
    """One tone per texel row, cycling, each row broken into runs of the next tone darker."""
    return P("lames", rows=1, tones=tuple((t, t) for t in tones), centre=None)


def solid(tone):
    return P("solid", tone=tone)


def marks(*rects):
    return P("marks", rects=list(rects))


def cloth(tone="seam", strips="v"):
    """The cloak's mottled navy: runs of a tone lighter or darker, upright like folds (or level)."""
    return P("plate", tone=tone, strips=strips, bevel=False)


# The palette's names (armor_paint.WAYFARER).
A, a, B, D, N, K = "light", "mid_light", "mid", "dark", "seam", "void"           # light teal to the darkest navy
S, s = "gold_light", "gold_dark"                                                # the clasp's silver
L, l, m, d, k = "leather_light", "leather_mid_light", "leather_mid", "leather_dark", "leather_darkest"   # brown leather
U, u, x, X = "under_light", "under_mid", "under_dark", "under_darkest"           # the hood's inside, the tunic
FW, FI, FP, FS = "feather_white", "feather_ice", "feather_pink", "feather_steel"   # the boots' wings


def feather(*tones):
    """A feather plank's broad faces: barbs across it, one tone per texel column from its root, its tip pink."""
    return {"right": marks(*[(i, 0, 1, 99, t) for i, t in enumerate(tones)]),
            "left": marks(*[(i, 0, 1, 99, t) for i, t in enumerate(tones)]), "top": solid(FW), "*": solid(FI)}


# ---------------------------------------------------------------- paint
def paints():
    """Role -> paint spec, the owner's patterns read off the sheet and the screenshot: the hood and cloak mottled navy
    and blue in upright folds, their edges light teal; the clasp silver round teal; the leather dark, its studs light."""
    return {
        # the hood: mottled navy and blue; its brim and rims light teal, shading to teal; under it the shadowed inside
        "hood": {"top": cloth("dark", "h"), "bottom": solid(x), "*": cloth("dark")},
        "peak": {"top": cloth("dark", "h"), "bottom": solid(x), "*": [cloth("dark", "h"), marks((0, 0, 99, 1, B))]},
        "brim": {"front": [solid(A), marks((0, -1, 99, 1, a))], "top": solid(a), "bottom": solid(U), "*": solid(a)},
        "rim": {"front": [solid(a), marks((0, 0, 99, 1, A), (0, -1, 99, 1, B))], "*": solid(B)},
        # the cloak: the mantle and panels mottled, their front edges teal; the tunic between them the dark inside
        "mantle": {"top": cloth("dark", "h"), "bottom": solid(N), "front": [cloth("dark"), marks((0, -1, 99, 1, N))],
                   "*": cloth("dark")},
        "tunic": {"front": [P("under"), marks((0, 0, 99, 1, u))], "*": P("under")},
        "panel": {"front": [cloth(), marks((-1, 0, 1, 99, a), (-2, 0, 1, 99, B), (0, -1, 99, 1, N))],
                  "left": [solid(a), marks((0, -1, 99, 1, B))], "bottom": solid(N), "*": cloth()},
        "back_panel": {"bottom": solid(N), "back": [cloth(), marks((0, -1, 99, 1, N))], "*": cloth()},
        "clasp": {"front": [solid(S), marks((0, 0, 1, 1, s), (-1, 0, 1, 1, s), (0, -1, 1, 1, s), (-1, -1, 1, 1, s),
                                            (1, 1, 1, 1, B))],
                  "*": solid(s)},
        "cape": {"top": cloth("dark", "h"), "bottom": solid(N), "*": [cloth("dark"), marks((0, -1, 99, 1, N))]},
        "tail": {"bottom": solid(K), "*": [cloth(), marks((0, -1, 99, 1, N), (0, -2, 99, 1, D))]},
        # the leather: the belt, the kilt with its light studs along the hem, the boots banded lighter at the cuff
        "belt": {"top": solid(l), "bottom": solid(k), "sides": [rows(m, d), marks((0, 0, 99, 1, l))]},
        "kilt": {"front": [rows(m, m, d, m), marks((0, 0, 99, 1, l), (1, -2, 1, 1, s), (3, -2, 1, 1, s), (5, -2, 1, 1, s),
                                                   (0, -1, 99, 1, k))],
                 "back": [rows(m, m, d, m), marks((0, 0, 99, 1, l), (1, -2, 1, 1, s), (4, -2, 1, 1, s), (0, -1, 99, 1, k))],
                 "top": solid(l), "bottom": solid(k),
                 "*": [rows(m, d, m), marks((0, 0, 99, 1, l), (2, -2, 1, 1, s), (0, -1, 99, 1, k))]},
        "boot": {"front": [rows(d, d, l, d, k), marks((1, 1, 1, 1, S), (-2, 1, 1, 1, S), (0, -1, 99, 1, k))],
                 "top": solid(m), "bottom": solid(k), "*": [rows(d, d, l, d, k), marks((0, -1, 99, 1, k))]},
        "cuff": {"top": solid(L), "bottom": solid(d), "sides": rows(l, m)},
        # the ankle wings: white and ice blue barbs, pink at the tips, a steel blue root
        "feather_0": feather(FS, FW, FI, FW, FW, FP),
        "feather_1": feather(FS, FI, FW, FI, FP),
        "feather_2": feather(FS, FW, FI, FP),
    }


# ---------------------------------------------------------------- shape helpers
def plank(name, root, length, width, thick, angle, x_in, paint):
    """A feather plank on the model's right: from `root` (bone space) it runs `length` px backward (+z), `width` tall
    and `thick` deep (outward, -x, from x_in), turned up `angle` degrees about x at the root."""
    rx, ry, rz = root
    return am.box(name, (x_in - thick, ry - width / 2, rz), (thick, width, length), pivot=(x_in, ry, rz),
                  rotation=(angle, 0, 0), paint=paint)


def wing(name, root, feathers, roll, sweep, paint_of):
    """A fan of feather planks from `root`, each (length, width, angle), the first the outermost, each 0.15 further out
    than the next; then rolled `roll` degrees (tops out) and swept `sweep` degrees (back out) about the root."""
    out = []
    rx, ry, rz = root
    for i, (length, width, angle) in enumerate(feathers):
        x_in = rx - 0.15 * (len(feathers) - 1 - i)
        part = plank(f"{name}_{i}_right", root, length, width, 0.6, angle, x_in, paint_of(i))
        part = am.turned(part, "z", -roll, root)
        out.append(am.turned(part, "y", -sweep, root))
    return out


# ---------------------------------------------------------------- helmet
def helmet(r):
    """The hood, 1.25 off the head all round: a top block from above the head down to the brow, a block down each side
    to 1.0 below the head and one behind between them, so the face shows through the opening between the side blocks
    (6 wide, from the brow to the chin); a lower step on its top toward the back, so the hood rounds off rather than
    ending square. The teal brim over the opening and a rim down each side of it, 0.5 proud. Each block is closed:
    through the opening an empty hood shows the side blocks' and the back block's own faces."""
    parts = [
        am.span("hood_top", (-5.25, -9.25, -5.25), (5.25, -6.75, 5.25), paint=r["hood"]),
        am.span("hood_peak", (-4.25, -9.9, -3.75), (4.25, -9.25, 4.5), skip="bottom", paint=r["peak"]),
        am.span("hood_back", (-3.0, -6.75, 3.0), (3.0, 1.0, 5.25), skip=("right", "left"), paint=r["hood"]),
        am.span("brim", (-5.0, -7.5, -5.75), (5.0, -6.4, -5.25), paint=r["brim"]),
    ]
    side = am.span("hood_side_right", (-5.25, -6.75, -5.25), (-3.0, 1.0, 5.25), paint=r["hood"])
    rim = am.span("rim_right", (-3.9, -6.4, -5.75), (-3.0, 1.0, -5.25), skip="back", paint=r["rim"])
    return parts + am.pair(side) + am.pair(rim)


# ---------------------------------------------------------------- chestplate
def body(r):
    """The mantle over the shoulders, 1.25 off the body's front and back and 1.5 off its sides (0.25 outside the hood's
    sides, which meet it at the neck), to the chest; the tunic under the cloak, 0.4 off it; the two
    front panels from under the mantle to the waist, 1.0 off the body's front, open by 2 down the middle, their inner
    edges teal; the back panel; the clasp on the left panel's top inner corner, its foot 0.25 off the plane the hood's
    front takes when the head looks straight down."""
    panel = am.span("panel_right", (-5.0, 3.75, -3.0), (-1.0, 12.25, -2.7), paint=r["panel"])
    return [
        am.span("mantle", (-5.5, -0.75, -3.25), (5.5, 4.0, 3.25), paint=r["mantle"]),
        am.span("tunic", (-4.6, 3.5, -2.4), (4.6, 11.25, 2.4), skip="top", paint=r["tunic"]),
        am.span("back_panel", (-5.0, 3.75, 2.7), (5.0, 12.25, 3.0), paint=r["back_panel"]),
        am.span("clasp", (1.1, 3.0, -3.6), (3.1, 5.0, -3.0), skip="back", paint=r["clasp"]),
    ] + am.pair(panel)


def arm(r):
    """The right arm (arm space: x -3..1, y -2..10, z -2..2): the cloak over the shoulder and the upper arm, 0.85 off
    it, to the elbow; the forearm bare, as drawn."""
    return [am.span("cape_right", (-3.85, -2.85, -2.85), (1.6, 4.5, 2.85), paint=r["cape"])]


def tail(r, name, length):
    """The cloak's tail behind the right thigh (leg space), 1.1 to 1.45 off the leg's back, `length` long from above
    the hip, on the leg so it follows it; part of the chestplate. Its planes keep 0.1 or more off the kilt's and the
    back panel's, the left one standing LEFT_OUT further out."""
    return am.span(name, (-2.75, -0.75, 3.1), (2.4, length, 3.45), paint=r["tail"])


# ---------------------------------------------------------------- leggings
LEFT_OUT = 0.12   # the left leg's parts stand this much further out, so the two legs' never share a plane where they
                  # overlap at the centre line


def waist(r):
    """The brown belt, 0.6 off the body's front and back and 0.45 off its sides, closed underneath 0.65 below it."""
    return [am.span("belt", (-4.45, 10.25, -2.6), (4.45, 12.65, 2.6), paint=r["belt"])]


def leg(r):
    """The right leg's kilt, 0.77 off the leg, from the hip, up under the belt, to mid-thigh; the knee and shin bare.
    The left leg's is mirrored and stands LEFT_OUT further out (model())."""
    return [am.span("kilt_right", (-2.77, -0.45, -2.77), (2.77, 5.25, 2.77), paint=r["kilt"])]


# ---------------------------------------------------------------- boots
def boot(r):
    """The right boot from mid-shin, 0.75 off the leg (clear of vanilla leggings' 0.5 shell), longer at the toe and
    closed 0.65 below the foot; its cuff, 1.0 off; on its outer side at the ankle a wing of three feathers swept back
    and up."""
    feathers = wing("heel", (-3.05, 9.6, 0.4), [(3.8, 1.2, 38), (3.2, 1.2, 22), (2.6, 1.2, 6)], roll=16, sweep=26,
                    paint_of=lambda i: r[f"feather_{i}"])
    return [am.span("boot_right", (-2.75, 8.0, -3.3), (2.75, 12.65, 2.75), paint=r["boot"]),
            am.span("cuff_right", (-3.0, 7.25, -3.0), (3.0, 8.25, 3.0), paint=r["cuff"])] + feathers


# ---------------------------------------------------------------- the set
def model():
    """{item: {bone: [parts]}} for the four pieces."""
    r = paints()
    a, l, b = arm(r), leg(r), boot(r)
    return {
        "wayfarer_helmet": {"head": helmet(r)},
        "wayfarer_chestplate": {"body": body(r), "right_arm": a, "left_arm": am.mirror_all(a),
                                "right_leg": [tail(r, "tail_right", 7.25)],
                                "left_leg": [replace(p, inflate=LEFT_OUT)
                                             for p in am.mirror_all([tail(r, "tail_short_right", 4.0)])]},
        "wayfarer_leggings": {"body": waist(r), "right_leg": l,
                              "left_leg": [replace(p, inflate=LEFT_OUT) for p in am.mirror_all(l)]},
        "wayfarer_boots": {"right_leg": b, "left_leg": [replace(p, inflate=LEFT_OUT) for p in am.mirror_all(b)]},
    }


SETS = [am.ArmorSet("wayfarer", WAYFARER, model())]
