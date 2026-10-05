"""The flail's swinging head (docs/features/arms-restyle.md, "The flail's head swings"). On 5 October 2026 the owner
asked that "flails should have an animated ball that actually flails around". So in the hand a flail's model is its
handle alone (tools/arms_art.py flail_handle), and its chain and spiked ball are two small box models, a link and a
ball, that client/arms/FlailHeads.java draws one by one where a little chain simulation puts them each frame: hanging
under gravity, trailing the arm as it moves and whipping round after a blow.

What this writes:
- models/item/arms_<kind>_link.json and arms_<kind>_ball.json: the shapes, built from boxes as the house style asks
  (docs/ART_DIRECTION.md): closed boxes, no two faces facing the same way in one plane, flat tones from a short ramp.
  They have no parent and no display transforms, so FlailHeads places them exactly;
- models/item/<item>_link.json and <item>_ball.json: each metal's, on that flail's own _model texture, whose free
  corner holds the swatches the boxes are coloured from (paint_swatches);
- the flail's item definition picks them by the custom_model_data string FlailHeads puts on its render-only copies of
  the stack (LINK_CASE, BALL_CASE), and otherwise shows the flail as before (definition());
- arms_heads.json, per item: where the chain hangs from the handle and where the hand holds it (model pixels), the
  links and the ball, the haft's radius (the ball swings clear of it), and the hand poses the handle is drawn with, so
  FlailHeads can put the chain where the handle's eye is drawn.

The measures are in the arms' design units (tools/arms_art.py) and scaled by the handle's own layout, so the head is
always in proportion to the handle it hangs from. All original.
"""
import itertools
import math

import arms_pixel as px
from arms_pixel import DARK, HIGHLIGHT, LIGHT, MID

MOD = "jugcraft"
# The custom_model_data strings that pick a flail's head parts (FlailHeads.LINK and BALL in Java).
LINK_CASE = "flail_link"
BALL_CASE = "flail_ball"
# The kinds whose head swings free, and its measures in design units: how many links and their pitch (centre to
# centre), each link's outer width, the thickness of its bars, and the ball's core radius and spike lengths.
HEADS = {
    "flail": {"links": 4, "pitch": 2.4, "link_width": 1.9, "link_bar": 0.62, "ball": 3.6, "base": 1.2, "tip": 1.2,
              "diagonal": 2.3},
}
# The swatches: a 16-texel square in the bottom right corner of the 64x64 _model texture (the handle's upright image
# only fills the top left), as rows of four 4-texel squares, highlight to dark: the chain, the ball's metal and its
# fittings.
SWATCH = (48, 48)
ROWS = ("chain", "blade", "fitting")
TONES = (HIGHLIGHT, LIGHT, MID, DARK)
# Which tone each face of a box takes, lit from above and the front left as the icons are (an index into TONES), for
# a part's body and for its points, which are a step brighter so the spikes read against the ball.
FACE_TONE = {"up": 1, "north": 1, "west": 1, "south": 2, "east": 2, "down": 3}
POINT_TONE = {"up": 0, "north": 0, "west": 0, "south": 1, "east": 1, "down": 2}


def paint_swatches(texture, style):
    """Paints the head's swatches into a flail's _model texture (its free corner; texture is the 64x64 image)."""
    materials = {"chain": px.CHAIN, "blade": style.blade, "fitting": style.fitting}
    x0, y0 = SWATCH
    for row, name in enumerate(ROWS):
        tones = materials[name].tones()
        for k, tone in enumerate(TONES):
            for y in range(4):
                for x in range(4):
                    texture.putpixel((x0 + k * 4 + x, y0 + row * 4 + y), tuple(tones[tone]) + (255,))
    return texture


def _uv(row, tone):
    """The middle of a swatch square, in model UV (0 to 16 over the 64-texel texture): flat, and mipmaps stay its colour."""
    u = (SWATCH[0] + TONES.index(tone) * 4) / 4.0
    v = (SWATCH[1] + ROWS.index(row) * 4) / 4.0
    return [u + 0.25, v + 0.25, u + 0.75, v + 0.75]


def _box(frm, to, row, point=False, rotation=None):
    """A box from frm to to (model pixels), its faces in `row`'s tones by which way they face (a point: a step
    brighter)."""
    faces = {}
    for face, tone in (POINT_TONE if point else FACE_TONE).items():
        faces[face] = {"uv": _uv(row, TONES[tone]), "texture": "#tex"}
    element = {"from": [round(v, 4) for v in frm], "to": [round(v, 4) for v in to], "faces": faces}
    if rotation is not None:
        element["rotation"] = rotation
    return element


C = 8.0   # the parts are built about the middle of the model, their chain axis along y


def link_elements(head, unit):
    """One chain link, its long axis along y and centred on the model: two side bars and two end bars nested between
    them (nothing overlaps). It is `pitch` long between its bends, plus a bar at each end, so the links interlock."""
    half_len = (head["pitch"] + head["link_bar"]) * unit / 2.0
    half_w = head["link_width"] * unit / 2.0
    bar = head["link_bar"] * unit
    thick = bar / 2.0
    out = []
    for side in (-1, 1):
        x0, x1 = sorted((C + side * half_w, C + side * (half_w - bar)))
        out.append(_box((x0, C - half_len, C - thick), (x1, C + half_len, C + thick), "chain"))
    # End bars a little thinner than the sides, so their front and back faces lie in planes of their own.
    for end in (-1, 1):
        y0, y1 = sorted((C + end * half_len, C + end * (half_len - bar)))
        out.append(_box((C - half_w + bar, y0, C - thick * 0.75), (C + half_w - bar, y1, C + thick * 0.75), "chain"))
    return out


def ball_elements(head, unit):
    """The spiked ball, centred on the model with its lug up (+y, towards the chain): a core of three crossing slabs
    of different sizes (so no two faces share a plane), five two-tier spikes along the axes (the sixth place is the
    lug's), and twelve on the diagonals, each two boxes turned 45 degrees about one axis."""
    r = head["ball"] * unit
    base, tip = head["base"] * unit, head["tip"] * unit
    out = []
    # The core, rounded: a cube, and through it one slab long in x, one in y and one in z, each a different size across
    # (so no two faces share a plane), stepping the outline in towards the poles.
    half = ((r, 0.52 * r, 0.52 * r), (0.46 * r, r + 0.12, 0.46 * r), (0.4 * r, 0.4 * r, r + 0.24))
    out.append(_box((C - 0.72 * r,) * 3, (C + 0.72 * r,) * 3, "blade"))
    for hx, hy, hz in half:
        out.append(_box((C - hx, C - hy, C - hz), (C + hx, C + hy, C + hz), "blade"))
    reach = [h[i] for i, h in enumerate(half)]   # how far the core reaches along x, y and z
    # Axial spikes: a broad base sunk 0.1 px into the core and a narrow tip on it.
    wide, narrow = 0.26 * r, 0.12 * r
    for axis in range(3):
        for sign in (-1, 1):
            if axis == 1 and sign > 0:
                continue   # the lug's place
            for w, a, b in ((wide, reach[axis] - 0.1, reach[axis] + base), (narrow, reach[axis] + base - 0.1, reach[axis] + base + tip)):
                lo, hi = [C - w] * 3, [C + w] * 3
                lo[axis], hi[axis] = (C + a, C + b) if sign > 0 else (C - b, C - a)
                out.append(_box(lo, hi, "blade", True))
    # Diagonal spikes: in each axis plane, a bar along one axis turned +-45 degrees about the third, from inside the
    # core out past it, in two tiers as the axial ones (widths of their own, so no face shares a plane with theirs).
    out_to = r + head["diagonal"] * unit
    for turn, along in (("z", 0), ("x", 2), ("y", 0)):
        for sign in (-1, 1):
            for angle in (45.0, -45.0):
                for w, a, b in ((0.21 * r, 0.55 * r, out_to - tip * 0.8), (0.1 * r, out_to - tip * 0.8 - 0.1, out_to)):
                    lo, hi = [C - w] * 3, [C + w] * 3
                    lo[along], hi[along] = (C + a, C + b) if sign > 0 else (C - b, C - a)
                    out.append(_box(lo, hi, "blade", True, {"angle": angle, "axis": turn, "origin": [C, C, C]}))
    # The lug the chain's last link hangs from: a fitting collar on the core's top and an eye above it.
    top = reach[1]
    collar, eye = 0.34 * r, 0.16 * r
    out.append(_box((C - collar, C + top - 0.1, C - collar), (C + collar, C + top + 0.3 * r, C + collar), "fitting"))
    out.append(_box((C - eye, C + top + 0.3 * r - 0.1, C - eye * 1.6), (C + eye, C + top + 0.62 * r, C + eye * 1.6), "fitting"))
    return out


def ball_reach(head, unit):
    """How far the ball reaches from its centre (model pixels): the core and spikes, for the haft it swings clear of."""
    return (head["ball"] + head["diagonal"]) * unit


def lug(head, unit):
    """From the ball's centre to where the last link hooks its lug (model pixels)."""
    return (head["ball"] + 0.62 * head["ball"]) * unit - 0.5 * head["link_bar"] * unit


# ---------------------------------------------------------------- the models and the table


def models(kind, unit):
    """{"link": model, "ball": model}: the shared head models of a kind, textured #tex, with no parent."""
    head = HEADS[kind]
    return {"link": {"textures": {"particle": "#tex"}, "elements": link_elements(head, unit)},
            "ball": {"textures": {"particle": "#tex"}, "elements": ball_elements(head, unit)}}


def definition(item, fallback):
    """A flail's item definition model: its head parts by FlailHeads' custom_model_data string, else `fallback`."""
    return {"type": "minecraft:select", "property": "minecraft:custom_model_data", "index": 0,
            "cases": [{"when": LINK_CASE, "model": {"type": "minecraft:model", "model": f"{MOD}:item/{item}_link"}},
                      {"when": BALL_CASE, "model": {"type": "minecraft:model", "model": f"{MOD}:item/{item}_ball"}}],
            "fallback": fallback}


def entry(kind, layout, display):
    """An item's arms_heads.json entry: `layout` is (grip_model, unit, grip, eye): the hand's point in model pixels,
    model pixels a design unit, and the grip and eye along the haft in design units; `display` the handle's hand
    poses. The eye and the grip are given in model pixels, on the handle's 45-degree lean."""
    head = HEADS[kind]
    (gx, gy), unit, grip, eye = layout
    lean = math.sqrt(0.5)
    along = (eye - grip) * unit
    anchor = [round(gx + along * lean, 4), round(gy + along * lean, 4), 8.0]
    return {"anchor": anchor, "grip": [round(gx, 4), round(gy, 4), 8.0], "links": head["links"],
            "pitch": round(head["pitch"] * unit, 4), "lug": round(lug(head, unit), 4),
            "reach": round(ball_reach(head, unit), 4), "haft": round(0.85 * unit, 4),
            "display": {context: display[context] for context in sorted(display)}}


# ---------------------------------------------------------------- checks (tools/check_mod_data.py)

_NORMALS = {"north": (0, 0, -1), "south": (0, 0, 1), "west": (-1, 0, 0), "east": (1, 0, 0), "down": (0, -1, 0),
            "up": (0, 1, 0)}


def _turn(rotation, p):
    if not rotation:
        return p
    a = math.radians(rotation["angle"])
    c, s = math.cos(a), math.sin(a)
    o = rotation["origin"]
    x, y, z = p[0] - o[0], p[1] - o[1], p[2] - o[2]
    if rotation["axis"] == "x":
        x, y, z = x, y * c - z * s, y * s + z * c
    elif rotation["axis"] == "y":
        x, y, z = x * c + z * s, y, -x * s + z * c
    else:
        x, y, z = x * c - y * s, x * s + y * c, z
    return (x + o[0], y + o[1], z + o[2])


def _faces(element):
    """Each face of an element as (normal, plane offset, its four corners), after the element's rotation."""
    f, t = element["from"], element["to"]
    rot = element.get("rotation")
    o = rot["origin"] if rot else (0, 0, 0)
    out = []
    for face, n in _NORMALS.items():
        axis = [i for i in range(3) if n[i]][0]
        others = [i for i in range(3) if i != axis]
        corners = []
        for a, b in ((0, 0), (1, 0), (1, 1), (0, 1)):
            p = [0.0, 0.0, 0.0]
            p[axis] = t[axis] if n[axis] > 0 else f[axis]
            p[others[0]] = (f, t)[a][others[0]]
            p[others[1]] = (f, t)[b][others[1]]
            corners.append(_turn(rot, p))
        normal = _turn({"angle": rot["angle"], "axis": rot["axis"], "origin": (0, 0, 0)} if rot else None, n)
        out.append((normal, sum(c * k for c, k in zip(corners[0], normal)), corners))
    return out


def _overlap(a, b, normal):
    """Whether two convex quads in one plane overlap in area (separating axes in the plane)."""
    def axes(quad):
        for i in range(4):
            e = [quad[(i + 1) % 4][k] - quad[i][k] for k in range(3)]
            yield (normal[1] * e[2] - normal[2] * e[1], normal[2] * e[0] - normal[0] * e[2], normal[0] * e[1] - normal[1] * e[0])
    for axis in itertools.chain(axes(a), axes(b)):
        pa = [sum(p[k] * axis[k] for k in range(3)) for p in a]
        pb = [sum(p[k] * axis[k] for k in range(3)) for p in b]
        if min(max(pa), max(pb)) - max(min(pa), min(pb)) <= 1e-4:
            return False
    return True


def coplanar(elements, gap=0.1):
    """Pairs of faces (element indices, face normal) that face the same way in planes closer than `gap` pixels and
    overlap: they would flicker. Rotated boxes are compared too (after their turns)."""
    faces = [(i, normal, offset, corners) for i, e in enumerate(elements) for normal, offset, corners in _faces(e)]
    bad = []
    for (i, n1, o1, q1), (j, n2, o2, q2) in itertools.combinations(faces, 2):
        if i == j or sum(a * b for a, b in zip(n1, n2)) < 0.9999 or abs(o1 - o2) >= gap - 1e-6:
            continue
        if _overlap(q1, q2, n1):
            bad.append((i, j, tuple(round(v, 3) for v in n1)))
    return bad
