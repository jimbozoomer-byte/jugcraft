"""Model geometry for the graveyard pack's headstones (tools/graveyard.py), built from boxes in pixels, facing north (the
carved front toward -z). Each function returns the elements of a whole memorial; graveyard_data.py cuts tall and long
ones into one model per block and maps the texture variables to a stage of weathering.

Texture variables: #stone (dressed stone; its bottom rows carry the ground moss), #top (the same stone without the ground
moss, for top faces and parts above the first block), #relief (carved or polished work, likewise #relief_top), #rough
(rock-faced granite), #knot (Celtic knotwork), #ivy (overgrowth, drawn only when overgrown).

Block models may turn an element only by 22.5 or 45 degrees, and keep every coordinate within -16..32 of their block.
"""
from decor_data import box

SIDES = ("north", "south", "east", "west")
ALL = SIDES + ("up", "down")


def b(lo, hi, tex="#stone", top=None, faces=ALL, rotation=None, textures=None):
    """A box whose top face takes `top` (by default the moss-free form of `tex`)."""
    if top is None:
        top = {"#stone": "#top", "#relief": "#relief_top"}.get(tex, tex)
    face_textures = {"up": top}
    face_textures.update(textures or {})
    return box(lo, hi, tex, faces=faces, rotation=rotation, textures=face_textures)


def mirror_x(elements):
    """`elements` reflected about the block's middle (x -> 16 - x), as for the right half of a symmetric carving."""
    out = []
    for e in elements:
        f, t = e["from"], e["to"]
        copy = {**e, "from": [16 - t[0], f[1], f[2]], "to": [16 - f[0], t[1], t[2]], "faces": dict(e["faces"])}
        faces = copy["faces"]
        if "east" in faces or "west" in faces:
            east, west = faces.pop("east", None), faces.pop("west", None)
            if west:
                faces["east"] = west
            if east:
                faces["west"] = east
        if "rotation" in e:
            r = e["rotation"]
            copy["rotation"] = {**r, "origin": [16 - r["origin"][0], r["origin"][1], r["origin"][2]],
                                "angle": -r["angle"] if r["axis"] in ("y", "z") else r["angle"]}
        out.append(copy)
    return out


def sym(elements):
    """A carving and its mirror image."""
    return elements + mirror_x(elements)


def rot(axis, angle, origin):
    return {"axis": axis, "angle": angle, "origin": list(origin)}


# ---------------------------------------------------------------- the gothic headstone (marble)

ALLOWED = (-45.0, -22.5, 0.0, 22.5, 45.0)


def edge_box(p0, p1, thick, inside, z0, z1, tex, extend=0.25):
    """A box `thick` pixels deep lying along the line p0 -> p1 in the x-y plane, on the side `inside` (+1: to the left
    of the line's direction, -1: to its right), turned to the line's slope (which must be a multiple of 22.5 degrees),
    reaching `extend` past each end so neighbouring edges close."""
    import math
    dx, dy = p1[0] - p0[0], p1[1] - p0[1]
    length = math.hypot(dx, dy) + 2 * extend
    angle = math.degrees(math.atan2(dy, dx))
    mx, my = (p0[0] + p1[0]) / 2, (p0[1] + p1[1]) / 2
    # Unrotated, the box lies along x (turned by `spin`) or along y (turned by `spin`).
    along_x = None
    for a in (angle, angle - 180, angle + 180):
        if any(abs(a - r) < 0.5 for r in ALLOWED):
            along_x, spin = True, min(ALLOWED, key=lambda r: abs(a - r))
            break
    if along_x is None:
        for a in (angle - 90, angle + 90, angle - 270, angle + 270):
            if any(abs(a - r) < 0.5 for r in ALLOWED):
                along_x, spin = False, min(ALLOWED, key=lambda r: abs(a - r))
                break
    assert along_x is not None, f"slope {angle} is not a multiple of 22.5"
    # The inside normal in the world, and in the box's own (unturned) frame.
    nx, ny = (-dy, dx) if inside > 0 else (dy, -dx)
    c, s = math.cos(math.radians(-spin)), math.sin(math.radians(-spin))
    lx, ly = nx * c - ny * s, nx * s + ny * c
    if along_x:
        lo = (mx - length / 2, my if ly > 0 else my - thick, z0)
        hi = (mx + length / 2, my + thick if ly > 0 else my, z1)
    else:
        lo = (mx if lx > 0 else mx - thick, my - length / 2, z0)
        hi = (mx + thick if lx > 0 else mx, my + length / 2, z1)
    e = b(lo, hi, tex)
    if spin:
        e["rotation"] = rot("z", spin, (mx, my, (z0 + z1) / 2))
    return e


def lancet(x0, spring, lower):
    """The outline of a pointed (lancet) arch from (x0, spring) to the middle and down to (16 - x0, spring): a steep
    lower part (67.5 degrees) `lower` pixels long, then 45 degrees to the point."""
    import math
    dx1, dy1 = lower * math.cos(math.radians(67.5)), lower * math.sin(math.radians(67.5))
    knee = (x0 + dx1, spring + dy1)
    apex = (8.0, knee[1] + (8.0 - knee[0]))
    return [(x0, spring), knee, apex, (16 - knee[0], knee[1]), (16 - x0, spring)]


def arch_fill(points, z0, z1, tex, inset=0.9):
    """Solid boxes filling the arch under `points` in half-pixel layers, kept `inset` inside its outline."""
    out = []
    spring = points[0][1]
    apex = points[2][1]
    y = spring
    while y < apex - inset * 1.6:
        top = min(y + 0.5, apex)
        # The arch's half width at height `top`, from the outline.
        half = None
        for (ax, ay), (bx, by) in zip(points[:2], points[1:3]):
            if ay <= top <= by:
                half = 8.0 - (ax + (bx - ax) * (top - ay) / (by - ay))
        if half is None or half - inset <= 0.2:
            break
        out.append(b((8 - half + inset, y, z0), (8 + half - inset, top, z1), tex))
        y = top
    return out


def gothic():
    """A marble headstone with a pointed gothic head on a moulded plinth: slender colonnettes either side, a raised
    moulding that follows the arch, a carved trefoil in its head and a small cross for a finial. 21 pixels tall (1.3 m)."""
    z0, z1 = 6.5, 9.5
    out = [b((1.5, 0, 4.5), (14.5, 2, 11.5)), b((2, 2, 5), (14, 2.6, 11), "#relief"),
           b((2.5, 2.6, z0), (13.5, 12, z1), textures={"north": "#relief"})]
    outline = lancet(2.5, 12.0, 3.2)
    out += arch_fill(outline, z0, z1, "#stone")
    for p0, p1 in zip(outline[:-1], outline[1:]):
        out.append(edge_box(p0, p1, 1.6, -1, z0, z1, "#stone"))
    apex = outline[2]
    # Finial: a little cross on a block.
    out += [b((7.3, apex[1] - 0.6, 7.3), (8.7, apex[1] + 0.4, 8.7), "#relief"),
            b((7.6, apex[1] + 0.4, 7.6), (8.4, apex[1] + 2.8, 8.4), "#relief"), b((6.9, apex[1] + 1.5, 7.6), (9.1, apex[1] + 2.2, 8.4), "#relief")]
    # Colonnettes with their bases and capitals.
    out += sym([b((1.8, 2.6, 6.0), (2.9, 11.4, 7.2), "#relief"), b((1.5, 2.6, 5.8), (3.2, 3.3, 7.4), "#relief"),
                b((1.5, 11.4, 5.8), (3.2, 12.2, 7.4), "#relief"), b((1.7, 12.2, 6.0), (3.0, 12.6, 7.2), "#relief")])
    # The moulding: stiles and a rail half a pixel proud, and a bead following the arch inside its edge.
    out += sym([b((3.6, 3.2, 6.0), (4.3, 12.2, z0), "#relief")])
    out.append(b((4.3, 3.2, 6.0), (11.7, 3.8, z0), "#relief"))
    inner = lancet(3.6, 12.0, 2.9)
    for p0, p1 in zip(inner[:-1], inner[1:]):
        out.append(edge_box(p0, p1, 0.7, -1, 6.0, z0, "#relief", extend=0.15))
    # The trefoil under the point: three lobes and a short stem.
    head = inner[2][1]
    out += [b((7.3, head - 3.2, 6.15), (8.7, head - 1.9, z0), "#relief"), b((6.1, head - 4.2, 6.15), (7.5, head - 2.9, z0), "#relief"),
            b((8.5, head - 4.2, 6.15), (9.9, head - 2.9, z0), "#relief"), b((7.75, head - 5.0, 6.15), (8.25, head - 3.2, z0), "#relief")]
    return out


# ---------------------------------------------------------------- the New England slates

def slate_outline():
    """A thin slate with a round tympanum between two rounded shoulders, set straight into the ground."""
    z0, z1 = 7.25, 8.75
    out = [b((1.5, 0, z0), (14.5, 12, z1))]
    for y0, y1, x0, x1 in [(12, 12.8, 1.5, 4.4), (12.8, 13.4, 1.8, 4.1), (13.4, 13.8, 2.3, 3.6)]:
        out += [b((x0, y0, z0), (x1, y1, z1)), b((16 - x1, y0, z0), (16 - x0, y1, z1))]
    for y0, y1, x in [(12, 15, 4.4), (15, 16, 4.7), (16, 16.8, 5.3), (16.8, 17.4, 6.1), (17.4, 17.8, 7.0)]:
        out.append(b((x, y0, z0), (16 - x, y1, z1)))
    # Incised borders: down each pilaster, under the tympanum and round the shoulders' rosettes.
    cut = 7.1
    border = [b((2.3, 1.2, cut), (2.6, 12.2, z0), "#relief"), b((2.3, 1.2, cut), (4.0, 1.5, z0), "#relief")]
    rosette = [b((2.5, 12.3, cut), (3.5, 13.3, z0), "#relief"), b((2.8, 12.0, cut), (3.2, 13.6, z0), "#relief"),
               b((2.2, 12.6, cut), (3.8, 13.0, z0), "#relief")]
    out += sym(border + rosette)
    out += [b((4.0, 1.2, cut), (12.0, 1.5, z0), "#relief"), b((4.4, 11.7, cut), (11.6, 12.0, z0), "#relief")]
    return out


def willow_urn():
    """The tympanum carved with a weeping willow over a funerary urn, the emblem of mourning in 1800s New England."""
    cut = 7.05
    z0 = 7.25
    out = slate_outline()
    urn = [b((7.0, 12.9, cut), (9.0, 14.3, z0), "#relief"), b((6.8, 13.3, cut), (9.2, 14.0, z0), "#relief"),
           b((7.45, 14.3, cut), (8.55, 14.65, z0), "#relief"), b((7.2, 14.65, cut), (8.8, 14.95, z0), "#relief"),
           b((7.8, 14.95, cut), (8.2, 15.35, z0), "#relief"), b((7.4, 12.55, cut), (8.6, 12.9, z0), "#relief"),
           b((7.0, 12.25, cut), (9.0, 12.55, z0), "#relief")]
    # The willow: its trunk rising at the left, the crown arching over, fronds hanging down either side of the urn.
    willow = [b((5.3, 12.2, cut), (5.75, 15.9, z0), "#relief"), b((5.1, 15.7, cut), (10.9, 16.1, z0), "#relief"),
              b((5.6, 16.1, cut), (10.0, 16.45, z0), "#relief"), b((6.3, 16.45, cut), (9.2, 16.8, z0), "#relief")]
    for x, low in [(4.75, 13.2), (6.2, 13.6), (6.65, 14.8), (9.4, 14.6), (9.85, 13.7), (10.4, 13.0), (10.9, 13.9)]:
        willow.append(b((x, low, cut), (x + 0.28, 15.75, z0), "#relief"))
    return out + urn + willow


def winged_skull():
    """The tympanum carved with a winged death's head, the older emblem: a skull with hollow eyes, nose and teeth,
    and feathered wings spread either side."""
    cut = 7.05
    z0 = 7.25
    out = slate_outline()
    skull = [b((7.0, 13.3, cut), (9.0, 15.5, z0), "#relief"), b((7.3, 12.8, cut), (8.7, 13.3, z0), "#relief"),
             b((6.85, 14.2, cut), (9.15, 15.2, z0), "#relief")]
    # Hollows: the stone's own dark face, set just in front of the carved skull.
    hollows = [b((7.25, 14.25, 7.0), (7.85, 14.85, 7.06), "#stone"), b((8.15, 14.25, 7.0), (8.75, 14.85, 7.06), "#stone"),
               b((7.85, 13.7, 7.0), (8.15, 14.1, 7.06), "#stone"), b((7.4, 13.05, 7.0), (8.6, 13.25, 7.06), "#stone")]
    for x in (7.65, 8.0, 8.35):
        hollows.append(b((x, 12.85, 7.0), (x + 0.1, 13.3, 7.06), "#stone"))
    wing = []
    for i, (y0, x0) in enumerate([(15.0, 4.6), (14.4, 4.9), (13.8, 5.3), (13.2, 5.8), (12.7, 6.3)]):
        wing.append(b((x0, y0, cut), (7.0, y0 + 0.5, z0), "#relief"))
        # Feather tips: a notch of dark stone at each row's outer end.
        wing.append(b((x0, y0, 7.0), (x0 + 0.3, y0 + 0.25, 7.06), "#stone"))
    out += skull + hollows + sym(wing)
    # An hourglass above: time running out.
    out += [b((7.6, 15.8, cut), (8.4, 16.0, z0), "#relief"), b((7.6, 16.9, cut), (8.4, 17.1, z0), "#relief"),
            b((7.75, 16.0, cut), (8.25, 16.9, z0), "#relief")]
    return out


# ---------------------------------------------------------------- the lamb headstone (marble)

def lamb():
    """A small marble stone for a child: a rounded tablet on a low base with a lamb lying on its top, its head turned
    to whoever stands at the grave."""
    z0, z1 = 6.5, 9.5
    out = [b((3, 0, 5), (13, 1.5, 11)), b((3.5, 1.5, 5.5), (12.5, 2, 10.5), "#relief"),
           b((4, 2, z0), (12, 9, z1), textures={"north": "#relief"})]
    # A beaded border round the panel.
    out += [b((4.6, 2.6, 6.2), (11.4, 2.9, z0), "#relief"), b((4.6, 8.3, 6.2), (11.4, 8.6, z0), "#relief"),
            b((4.6, 2.9, 6.2), (4.9, 8.3, z0), "#relief"), b((11.1, 2.9, 6.2), (11.4, 8.3, z0), "#relief")]
    # A moulded cap, and the lamb in carved marble lying on it, legs folded, its head raised and turned to the front.
    r = "#relief"
    out += [b((3.7, 9.0, 6.2), (12.3, 9.5, 9.8), r)]
    out += [b((6.2, 9.5, 7.0), (11.0, 11.3, 9.0), r), b((5.8, 9.7, 7.2), (11.4, 11.0, 8.8), r), b((6.6, 11.3, 7.4), (10.6, 11.7, 8.6), r),
            b((10.6, 9.7, 7.3), (11.8, 10.9, 8.7), r)]
    # Neck, head, muzzle and drooping ears.
    out += [b((5.0, 10.4, 7.4), (6.6, 11.9, 8.6), r), b((4.0, 11.4, 6.9), (5.8, 13.0, 8.4), r), b((4.35, 11.5, 6.3), (5.45, 12.4, 6.9), r),
            b((3.5, 11.9, 7.3), (4.1, 12.5, 8.1), r), b((5.7, 11.9, 7.3), (6.3, 12.5, 8.1), r)]
    # Folded forelegs at its chest, a hind leg and the tail; a few curls along its back.
    out += [b((5.2, 9.5, 6.6), (6.8, 9.95, 7.3), r), b((9.6, 9.5, 6.75), (11.2, 10.2, 7.3), r), b((11.8, 10.2, 7.7), (12.25, 10.8, 8.3), r)]
    for x, z in ((7.0, 7.6), (8.5, 7.9), (9.8, 7.5)):
        out.append(b((x, 11.6, z), (x + 0.6, 11.95, z + 0.6), r))
    return out


# ---------------------------------------------------------------- the broken column (marble), two blocks tall

def broken_column():
    """A pedestal carrying a fluted column snapped off part-way, a shroud thrown over the break: a life cut short.
    The pedestal's die carries the epitaph. 29 pixels tall (1.8 m)."""
    out = [b((2.5, 0, 2.5), (13.5, 2, 13.5)), b((3, 2, 3), (13, 3, 13), "#relief"),
           b((3.5, 3, 3.5), (12.5, 11, 12.5)), b((3, 11, 3), (13, 12, 13), "#relief"),
           b((3.3, 12, 3.3), (12.7, 12.4, 12.7), "#relief")]
    # A raised frame round the die's front panel.
    out += [b((4.3, 4.0, 3.2), (11.7, 4.4, 3.5), "#relief"), b((4.3, 9.8, 3.2), (11.7, 10.2, 3.5), "#relief"),
            b((4.3, 4.4, 3.2), (4.7, 9.8, 3.5), "#relief"), b((11.3, 4.4, 3.2), (11.7, 9.8, 3.5), "#relief")]
    # Torus base and the shaft: an octagon of two crossed boxes, fluted by narrow ribs.
    out += [b((4.6, 12.4, 4.6), (11.4, 13.4, 11.4), "#relief"), b((5.2, 13.4, 5.2), (10.8, 14, 10.8), "#relief")]
    out += [b((5.5, 14, 5.0), (10.5, 26, 11.0), "#relief", top="#relief_top"), b((5.0, 14, 5.5), (11.0, 26, 10.5), "#relief")]
    for x in (6.4, 7.75, 9.1):
        out += [b((x, 14.3, 4.95), (x + 0.5, 25.6, 5.0), "#top"), b((x, 14.3, 11.0), (x + 0.5, 25.6, 11.05), "#top")]
    for z in (6.4, 7.75, 9.1):
        out += [b((4.95, 14.3, z), (5.0, 25.6, z + 0.5), "#top"), b((11.0, 14.3, z), (11.05, 25.6, z + 0.5), "#top")]
    # The break: jagged stumps of different heights.
    out += [b((5.5, 26, 5.5), (8.5, 27.6, 10.5), "#relief_top"), b((8.5, 26, 6.5), (10.5, 26.8, 10.5), "#relief_top"),
            b((6.5, 27.6, 7.0), (8.0, 28.6, 10.0), "#relief_top"), b((9.0, 26.8, 8.0), (10.2, 27.4, 10.0), "#relief_top")]
    # The shroud: over the front of the break and falling down the left side in folds.
    cloth = "#relief_top"
    out += [b((4.7, 25.2, 4.6), (9.4, 27.0, 5.3), cloth), b((5.2, 23.6, 4.5), (7.8, 25.2, 5.1), cloth),
            b((4.6, 21.0, 4.8), (5.3, 27.0, 8.6), cloth), b((4.5, 18.5, 5.3), (5.2, 21.0, 7.6), cloth),
            b((4.6, 26.6, 5.3), (8.6, 27.9, 9.0), cloth), b((5.6, 27.9, 6.0), (7.6, 28.3, 8.4), cloth)]
    return out


# ---------------------------------------------------------------- the Celtic high cross (granite), three blocks tall

def ring_arc(cx, cy, z0, z1, radius, tex):
    """A ring of stone round (cx, cy) in the x-y plane, `radius` to its middle, two pixels thick, drawn by boxes turned
    22.5 and 45 degrees in each quadrant (the arms and shaft cover it at 0 and 90)."""
    import math
    out = []
    for quadrant in range(4):
        for k, theta in enumerate((22.5, 45.0, 67.5)):
            angle = theta + 90 * quadrant
            x = cx + radius * math.cos(math.radians(angle))
            y = cy + radius * math.sin(math.radians(angle))
            # A box whose long side follows the ring: long in y turned by +theta, or long in x turned by theta - 90.
            phi = ((angle + 90 + 90) % 180) - 90
            if phi in (-45.0, 45.0, 0.0):
                lo, hi = (x - 1.6, y - 1.0, z0), (x + 1.6, y + 1.0, z1)
                spin = phi
            else:
                lo, hi = (x - 1.0, y - 1.6, z0), (x + 1.0, y + 1.6, z1)
                spin = ((phi - 90 + 90) % 180) - 90
            out.append(b(lo, hi, tex, rotation=rot("z", spin, (x, y, (z0 + z1) / 2))))
    return out


def celtic_cross():
    """A granite high cross: a stepped rock-faced base whose die carries the epitaph, a tall shaft and arms faced with
    knotwork, a ring binding the arms, and bosses at the crossing. 44 pixels tall (2.75 m)."""
    knot = {"north": "#knot", "south": "#knot"}
    out = [b((1, 0, 3), (15, 1.5, 13), "#rough"), b((2, 1.5, 4), (14, 6.5, 12)), b((2.5, 6.5, 4.5), (13.5, 7.5, 11.5), "#relief")]
    out += [b((5, 7.5, 5.5), (11, 31, 10.5), textures=knot, top="#top"),
            b((-1, 30.5, 5.5), (17, 36, 10.5), textures=knot, top="#top"),
            b((5, 36, 5.5), (11, 43, 10.5), textures=knot, top="#top")]
    # Flared arm ends and a capstone.
    out += [b((-1.6, 30.0, 5.3), (-0.4, 36.5, 10.7), "#relief"), b((16.4, 30.0, 5.3), (17.6, 36.5, 10.7), "#relief"),
            b((4.5, 42.6, 5.3), (11.5, 44, 10.7), "#relief")]
    # Raised edge mouldings down the shaft.
    out += sym([b((5, 7.5, 5.25), (5.6, 30.5, 5.5), "#relief")])
    out += ring_arc(8, 33.25, 6.2, 9.8, 6.4, "#relief")
    # Bosses: one at the crossing and one on each arm and the shaft.
    for x, y in ((8, 33.25), (1.6, 33.25), (14.4, 33.25), (8, 39.8), (8, 27.4)):
        out += [b((x - 0.9, y - 0.9, 5.0), (x + 0.9, y + 0.9, 5.5), "#relief"), b((x - 0.5, y - 0.5, 4.8), (x + 0.5, y + 0.5, 5.0), "#relief")]
    return out


# ---------------------------------------------------------------- the rustic scroll (granite)

def scroll():
    """A rough granite boulder, as a Victorian rustic memorial: a polished scroll unrolled down its face carries the
    epitaph, and a spray of carved fern lies across its shoulder."""
    rough = "#rough"
    out = [b((2, 0, 5), (14, 9, 11), rough), b((1.4, 0.5, 6), (14.6, 7.4, 10.4), rough), b((2.6, 9, 5.4), (13.6, 11.4, 10.6), rough),
           b((3.8, 11.4, 5.8), (12.4, 13.2, 10.2), rough), b((5.2, 13.2, 6.4), (10.8, 14.4, 9.8), rough), b((6.6, 14.4, 7.0), (9.6, 15.0, 9.2), rough),
           b((1.0, 0, 7.0), (2.4, 5.2, 10.0), rough), b((13.4, 1.5, 6.4), (15.0, 6.8, 9.4), rough),
           b((10.5, 9.4, 8.6), (14.0, 12.8, 11.4), rough), b((1.8, 7.2, 6.6), (3.2, 10.6, 10.0), rough),
           b((11.0, 12.4, 6.2), (13.0, 13.8, 9.6), rough, rotation=rot("z", -22.5, (12.0, 13.1, 7.9))),
           b((3.0, 12.0, 6.0), (5.2, 13.4, 9.8), rough, rotation=rot("z", 22.5, (4.1, 12.7, 7.9))),
           b((5.0, 0, 4.4), (11.0, 1.8, 5.2), rough)]
    # The scroll: a sheet with a roll at the top and the bottom.
    r = "#relief"
    out += [b((3.6, 3.2, 4.6), (12.4, 10.8, 5.0), r), b((3.2, 10.5, 4.2), (12.8, 11.7, 5.2), r), b((3.4, 10.3, 4.4), (12.6, 11.9, 5.0), r),
            b((3.2, 2.3, 4.2), (12.8, 3.5, 5.2), r), b((3.4, 2.1, 4.4), (12.6, 3.7, 5.0), r),
            b((2.9, 10.6, 4.4), (3.3, 11.6, 5.0), r), b((12.7, 2.4, 4.4), (13.1, 3.4, 5.0), r)]
    # Fern fronds across the left shoulder.
    for i, (x, y) in enumerate([(3.2, 12.4), (4.0, 13.1), (4.8, 13.7), (5.6, 14.3)]):
        out += [b((x, y, 5.0), (x + 0.9, y + 0.3, 5.5), r), b((x + 0.3, y + 0.3, 5.0), (x + 0.6, y + 0.9, 5.5), r)]
    out.append(b((3.0, 12.1, 5.2), (6.6, 12.4, 5.6), r, rotation=rot("z", 45, (4.8, 12.25, 5.4))))
    return out


# ---------------------------------------------------------------- the table tomb and ledger stone (sandstone), one by two

def table_tomb():
    """A chest tomb two blocks long (from its foot, z 0, to its head, z 32): a plinth, panelled sides with carved
    roundels between corner balusters, and an overhanging ledger slab whose top carries the epitaph."""
    out = [b((0.5, 0, 0.5), (15.5, 1.5, 31.5)), b((1, 1.5, 1), (15, 2.2, 31), "#relief"),
           b((1.8, 2.2, 1.8), (14.2, 11, 30.2))]
    # Corner balusters.
    for x0, x1 in ((1.2, 2.8), (13.2, 14.8)):
        for z0, z1 in ((1.2, 2.8), (29.2, 30.8)):
            out += [b((x0, 2.2, z0), (x1, 11, z1), "#relief"), b((x0 - 0.2, 5.8, z0 - 0.2), (x1 + 0.2, 6.6, z1 + 0.2), "#relief")]
    # Panels: a frame on each long side, divided in two, a roundel in each; one panel at the foot with an urn.
    for side_x, face_lo, face_hi in ((1.8, 1.5, 1.8), (14.2, 14.2, 14.5)):
        for z0, z1 in ((3.5, 15.5), (16.5, 28.5)):
            out += [b((face_lo, 3.2, z0), (face_hi, 3.7, z1), "#relief"), b((face_lo, 9.5, z0), (face_hi, 10.0, z1), "#relief"),
                    b((face_lo, 3.7, z0), (face_hi, 9.5, z0 + 0.5), "#relief"), b((face_lo, 3.7, z1 - 0.5), (face_hi, 9.5, z1), "#relief")]
            mid = (z0 + z1) / 2
            out += [b((face_lo, 5.4, mid - 1.6), (face_hi, 7.8, mid + 1.6), "#relief"), b((face_lo, 4.9, mid - 1.0), (face_hi, 8.3, mid + 1.0), "#relief")]
    out += [b((3.5, 3.2, 1.5), (12.5, 3.7, 1.8), "#relief"), b((3.5, 9.5, 1.5), (12.5, 10.0, 1.8), "#relief"),
            b((3.5, 3.7, 1.5), (4.0, 9.5, 1.8), "#relief"), b((12.0, 3.7, 1.5), (12.5, 9.5, 1.8), "#relief"),
            b((7.0, 4.6, 1.4), (9.0, 6.8, 1.8), "#relief"), b((7.4, 6.8, 1.4), (8.6, 7.4, 1.8), "#relief"),
            b((7.2, 7.4, 1.4), (8.8, 7.8, 1.8), "#relief"), b((7.4, 4.2, 1.4), (8.6, 4.6, 1.8), "#relief")]
    # The ledger slab, its moulded edge below.
    out += [b((0.6, 11, 0.6), (15.4, 11.7, 31.4)), b((0.9, 11.7, 0.9), (15.1, 12.6, 31.1), "#relief")]
    return out


def ledger():
    """A ledger stone two blocks long, lying on the grave: a bevelled slab with an incised border, a cross carved at
    its head, and the epitaph across its foot half."""
    r = "#relief"
    out = [b((1, 0, 1), (15, 1.8, 31)), b((1.5, 1.8, 1.5), (14.5, 2.4, 30.5), r)]
    out += [b((2.3, 2.4, 2.3), (13.7, 2.55, 2.7), "#stone"), b((2.3, 2.4, 29.3), (13.7, 2.55, 29.7), "#stone"),
            b((2.3, 2.4, 2.7), (2.7, 2.55, 29.3), "#stone"), b((13.3, 2.4, 2.7), (13.7, 2.55, 29.3), "#stone")]
    out += [b((7.2, 2.4, 19.0), (8.8, 2.75, 28.0), r), b((4.8, 2.4, 23.0), (11.2, 2.75, 24.6), r),
            b((6.6, 2.4, 18.4), (9.4, 2.65, 19.2), r)]
    return out


def overgrowth(kind):
    """Ivy and moss over a memorial when it is overgrown: clumps at its foot and a trail climbing one side."""
    ivy = "#ivy"
    if kind == "slab":
        return [b((0.6, 0, 0.4), (4.5, 2.3, 3.0), ivy), b((11.0, 0, 27.0), (15.6, 2.6, 31.6), ivy),
                b((12.6, 1.6, 2.0), (15.4, 2.8, 6.0), ivy), b((1.0, 1.8, 20.0), (3.6, 2.7, 26.0), ivy)]
    out = [b((1.0, 0, 4.0), (4.6, 1.8, 6.4), ivy), b((11.6, 0, 9.6), (15.2, 1.4, 12.0), ivy), b((5.2, 0, 4.4), (7.4, 1.0, 5.6), ivy)]
    if kind == "tall":
        out += [b((1.4, 1.8, 5.5), (2.2, 9.0, 6.4), ivy), b((1.8, 8.6, 5.3), (3.2, 9.6, 6.3), ivy), b((2.4, 9.4, 5.2), (2.9, 13.0, 6.1), ivy)]
    else:
        out += [b((1.6, 1.8, 6.2), (2.3, 7.5, 7.0), ivy), b((2.0, 7.0, 6.0), (3.3, 7.9, 6.9), ivy)]
    return out
