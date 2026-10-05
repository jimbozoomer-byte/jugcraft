"""The Runebound arms as real 3D meshes (docs/features/arms-vii.md, "Runebound meshes"; the owner, 5 October 2026:
"the runebound weapons need to look much better ... don't even use minecraft esque textures make like a nicer 3d
model and use that to make it really be cool and special").

The Runebound Nodachi, Moonblade, Staff and War Hammer are drawn in the hand as smooth quad meshes instead of the
restyle's boxes (tools/arms_pixel.py): lofted blades with a ground bevel to a real edge and a glowing rune channel cut
into the flat, round grips and shafts turned on a lathe, swept guards, claws and beaks, and faceted crystals. Normals
are given per corner and smoothed within each panel (a hard edge splits them), so curves shade smoothly and edges stay
crisp. Detail comes from the shapes; the textures are clean painted materials (runebound_mesh, 128x128) and a pulsing
strip of glowing glyphs (runebound_rune, animated).

Each mesh is built along the weapon's own axis in its design's units, exactly as long as the restyle design it
replaces (tools/arms_variants_art.py) and held at the same grip, then laid on the diagonal by the same 45-degree turn
about the hand, so every hand pose in tools/arms_variants.py holds it as it held the box model. The icon is rendered
from the same mesh (icon()).

In game the model is read by client/MeshItemModels.java (Fabric's model loading and renderer API meshes, already in
fabric-api: no new dependency). The model JSON also keeps the box model as "elements", so if that loader were
missing the arm still loads as before.

Space: points are (t, s, w): t across the weapon (negative towards the light, the icon's top left), s along it from
the butt, w through it (towards the viewer of the icon). All original geometry.
"""
import math

MOD = "jugcraft"
NAMES = ("runebound_nodachi", "runebound_moonblade", "runebound_staff", "runebound_war_hammer")
ATLAS = "runebound_mesh"          # textures/item/runebound_mesh.png: the painted materials
RUNE = "runebound_rune"           # textures/item/runebound_rune.png: the glowing glyphs, a pulsing animated strip
ATLAS_SIZE = 128
RUNE_SIZE = 64
RUNE_FRAMES = 8
RUNE_FRAMETIME = 3                # ticks a frame: a pulse every 1.2 s
ICON = 48                         # icon size (as the long arms' icons)
LIFT = 0.1                        # model pixels: the least a part sits proud of the surface it lies on
MAX_QUADS = 2000


# ---------------------------------------------------------------- vectors


def add(a, b):
    return (a[0] + b[0], a[1] + b[1], a[2] + b[2])


def sub(a, b):
    return (a[0] - b[0], a[1] - b[1], a[2] - b[2])


def mul(a, k):
    return (a[0] * k, a[1] * k, a[2] * k)


def dot(a, b):
    return a[0] * b[0] + a[1] * b[1] + a[2] * b[2]


def cross(a, b):
    return (a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0])


def length(a):
    return math.sqrt(dot(a, a))


def unit(a):
    n = length(a)
    return (0.0, 0.0, 0.0) if n < 1e-12 else (a[0] / n, a[1] / n, a[2] / n)


def lerp(a, b, k):
    return a + (b - a) * k


def smooth(x):
    x = min(1.0, max(0.0, x))
    return x * x * (3 - 2 * x)


def spline(points, x):
    """A Catmull-Rom curve through (x, y) points (x increasing), at x."""
    if x <= points[0][0]:
        return points[0][1]
    if x >= points[-1][0]:
        return points[-1][1]
    k = max(i for i in range(len(points) - 1) if points[i][0] <= x)
    p0 = points[max(0, k - 1)]
    p1, p2 = points[k], points[k + 1]
    p3 = points[min(len(points) - 1, k + 2)]
    u = (x - p1[0]) / (p2[0] - p1[0])
    m1 = (p2[1] - p0[1]) / (p2[0] - p0[0]) * (p2[0] - p1[0])
    m2 = (p3[1] - p1[1]) / (p3[0] - p1[0]) * (p2[0] - p1[0])
    u2, u3 = u * u, u * u * u
    return (2 * u3 - 3 * u2 + 1) * p1[1] + (u3 - 2 * u2 + u) * m1 + (-2 * u3 + 3 * u2) * p2[1] + (u3 - u2) * m2


# ---------------------------------------------------------------- materials (regions of the painted atlas)

class Mat:
    """A material: its sprite slot ("mesh" or "rune"), its region of that sprite in pixels, and whether it glows."""

    def __init__(self, slot, region, glow=False, size=ATLAS_SIZE):
        self.slot = slot
        self.region = region
        self.glow = glow
        self.size = size

    def uv(self, a, b):
        """The sprite's (u, v), 0 to 1, of the point (a, b) of this region (each 0 to 1), half a pixel inside its
        edges so a neighbour never bleeds in."""
        x0, y0, x1, y1 = self.region
        x = x0 + 0.5 + (x1 - x0 - 1.0) * min(1.0, max(0.0, a))
        y = y0 + 0.5 + (y1 - y0 - 1.0) * min(1.0, max(0.0, b))
        return (x / self.size, y / self.size)


# The atlas, 128x128 (atlas() paints it): each region a clean material, gradients running along v (b).
STEEL = Mat("mesh", (0, 0, 32, 32))           # moon steel flats: violet-silver
EDGE = Mat("mesh", (34, 0, 50, 32))           # the ground bevel: polished, lightest at the edge (b = 1)
SPINE = Mat("mesh", (52, 0, 68, 32))          # the back of a blade, a shade darker
IRON = Mat("mesh", (70, 0, 102, 32))          # dark violet iron: guards, caps, claws, bands
SILVER = Mat("mesh", (104, 0, 128, 32))       # moon-silver trim: the moonblade's bezel
SILK = Mat("mesh", (0, 34, 32, 66))           # the nodachi's diamond-wrapped tsuka (a repeat along a)
CORD = Mat("mesh", (34, 34, 66, 66))          # a spiral cord wrap (a repeat along a)
LEATHER = Mat("mesh", (68, 34, 100, 66))      # violet leather, two seams along it: the staff's grip
WOOD = Mat("mesh", (102, 34, 128, 66))        # dark ironwood, grain along a
CRYSTAL = Mat("mesh", (0, 68, 32, 100), glow=True)    # glowing cyan crystal facets
HEAD = Mat("mesh", (52, 68, 84, 100))         # the hammer's head: darker moon steel
CHANNEL = Mat("mesh", (86, 68, 102, 100))     # the walls of a rune channel: deep shadowed steel
HAMON = Mat("mesh", (0, 102, 64, 128))        # the nodachi's ground edge: a frosted temper line in waves (a along)
GILT = Mat("mesh", (66, 102, 98, 128))        # pale moon gold: the nodachi's habaki and the hammer's band
# The glyph strip (rune_frames()), 64x64 frames: 3 rows of 4 glyphs (8x8 each), a plain glowing band below them, a
# 32x32 sigil beside them, and a plain glow under all three.
GLYPH_ROWS, GLYPH_COLS = 3, 4
BAND = Mat("rune", (0, 24, 32, 32), glow=True, size=RUNE_SIZE)
SIGIL = Mat("rune", (32, 0, 64, 32), glow=True, size=RUNE_SIZE)
GLOW = Mat("rune", (0, 34, 64, 64), glow=True, size=RUNE_SIZE)


def glyph(index, half=None):
    """The glyph cell `index` (0 to 11) of the rune strip; `half`, 0 or 1, for the first or second half of it."""
    index %= GLYPH_ROWS * GLYPH_COLS
    x0, y0 = 8 * (index % GLYPH_COLS), 8 * (index // GLYPH_COLS)
    if half is None:
        return Mat("rune", (x0, y0, x0 + 8, y0 + 8), glow=True, size=RUNE_SIZE)
    return Mat("rune", (x0 + 4 * half, y0, x0 + 4 * half + 4, y0 + 8), glow=True, size=RUNE_SIZE)


# ---------------------------------------------------------------- the mesh


class Quad:
    __slots__ = ("mat", "points", "uvs", "normals")

    def __init__(self, mat, points, uvs, normals):
        self.mat = mat
        self.points = points
        self.uvs = uvs
        self.normals = normals


def face_normal(points):
    """A quad's area-weighted normal, from its diagonals (as Minecraft and Fabric's renderer take it)."""
    return cross(sub(points[2], points[0]), sub(points[3], points[1]))


class Mesh:
    def __init__(self):
        self.quads = []

    def add(self, mat, points, uvs, normals):
        """A quad, its corners turned anticlockwise as seen from the side its normals face; skipped if it has no area."""
        fn = face_normal(points)
        if length(fn) < 1e-7:
            return
        if dot(fn, add(add(normals[0], normals[1]), add(normals[2], normals[3]))) < 0:
            points, uvs, normals = points[::-1], uvs[::-1], normals[::-1]
        self.quads.append(Quad(mat, list(points), list(uvs), [unit(n) for n in normals]))


def surface(mesh, P, mat, closed=True, hard_j=(), hard_i=(), uv=None):
    """Quads over a grid of points P[i][j] (i along, j round; j a closed loop when `closed`), with per-corner normals
    smoothed over the quads round each grid point, except across a hard point j (a crease running along) or a hard
    ring i (a crease running round): there each side keeps its own normals.

    `mat` is a Mat or a function (i, j) -> Mat; `uv(i, j, di, dj)` gives the (a, b) in its region of corner (i + di,
    j + dj) of quad (i, j) (default: a along, b round). The normals face away from the rings' centres."""
    I, J = len(P), len(P[0])
    nj = J if closed else J - 1
    hard_j, hard_i = set(hard_j), set(hard_i)
    F = {}
    for i in range(I - 1):
        for j in range(nj):
            F[i, j] = face_normal((P[i][j], P[i + 1][j], P[i + 1][(j + 1) % J], P[i][(j + 1) % J]))
    # Which way is out: away from the ring's centre, summed over the whole surface.
    score = 0.0
    for (i, j), n in F.items():
        centre = mul(add(add(P[i][j], P[i + 1][j]), add(P[i + 1][(j + 1) % J], P[i][(j + 1) % J])), 0.25)
        ring = P[i] + P[i + 1]
        ref = mul(tuple(sum(p[k] for p in ring) for k in range(3)), 1.0 / len(ring))
        score += dot(n, sub(centre, ref))
    sign = -1.0 if score < 0 else 1.0
    for key in F:
        F[key] = mul(F[key], sign)

    def normal_at(i, j, gi, gj):
        total = (0.0, 0.0, 0.0)
        for qi in (gi - 1, gi):
            if qi < 0 or qi >= I - 1:
                continue
            if qi != i and gi in hard_i:
                continue
            for qj in (gj - 1, gj):
                if closed:
                    qj %= J
                elif qj < 0 or qj >= nj:
                    continue
                if qj != j and gj % J in hard_j:
                    continue
                total = add(total, F[qi, qj])
        n = unit(total)
        return n if length(n) > 0.5 else unit(F[i, j])

    for i in range(I - 1):
        for j in range(nj):
            m = mat(i, j) if callable(mat) else mat
            corners = ((0, 0), (1, 0), (1, 1), (0, 1))
            pts = [P[i + di][(j + dj) % J] for di, dj in corners]
            if uv is None:
                abs_ = [((i + di) / (I - 1), (j + dj) / nj) for di, dj in corners]
            else:
                abs_ = [uv(i, j, di, dj) for di, dj in corners]
            uvs = [m.uv(a, b) for a, b in abs_]
            ns = [normal_at(i, j, i + di, j + dj) for di, dj in corners]
            mesh.add(m, pts, uvs, ns)


def cap(mesh, ring, mat, outward):
    """A flat cap over a planar ring of an even number of points: quads from its centre over each pair of edges."""
    n = len(ring)
    centre = mul(tuple(sum(p[k] for p in ring) for k in range(3)), 1.0 / n)
    normal = unit(outward)
    # Planar UVs: two axes in the cap's plane.
    a_axis = unit(sub(ring[0], centre))
    b_axis = unit(cross(normal, a_axis))
    reach = max(length(sub(p, centre)) for p in ring) or 1.0

    def uvof(p):
        d = sub(p, centre)
        return mat.uv(0.5 + 0.5 * dot(d, a_axis) / reach, 0.5 + 0.5 * dot(d, b_axis) / reach)
    for j in range(0, n, 2):
        pts = [centre, ring[j], ring[(j + 1) % n], ring[(j + 2) % n]]
        mesh.add(mat, pts, [uvof(p) for p in pts], [normal] * 4)


# ---------------------------------------------------------------- shapes

AXIS = ((0.0, 1.0, 0.0), (1.0, 0.0, 0.0), (0.0, 0.0, 1.0))    # along s; round it, t then w


def ring_points(origin, frame, r_a, r_b, seg, phase=0.0, bump=None):
    """A ring of `seg` points round `origin` in the plane of frame[1], frame[2], radii r_a and r_b along them."""
    _axis, e1, e2 = frame
    out = []
    for k in range(seg):
        a = 2 * math.pi * (k + phase) / seg
        k_r = 1.0 + (bump(a) if bump else 0.0)
        out.append(add(origin, add(mul(e1, r_a * k_r * math.cos(a)), mul(e2, r_b * k_r * math.sin(a)))))
    return out


def lathe(mesh, profile, mat, seg=16, origin=(0.0, 0.0, 0.0), frame=AXIS, hard=(), caps=(True, True), phase=0.0,
          uv=None, flat_round=False):
    """A turned part round frame[0] through `origin`: profile [(s, r)] or [(s, r_a, r_b)] (an oval), each end capped.
    `hard`: profile points that are creases; `flat_round`: facets instead of a round (every j hard)."""
    axis = frame[0]
    P = []
    for entry in profile:
        s, ra = entry[0], entry[1]
        rb = entry[2] if len(entry) > 2 else ra
        P.append(ring_points(add(origin, mul(axis, s)), frame, ra, rb, seg, phase))
    m = mat
    surface(mesh, P, m, closed=True, hard_i=hard, hard_j=range(seg) if flat_round else (), uv=uv)
    caps_mat = mat if not callable(mat) else mat(0, 0)
    if caps[0]:
        cap(mesh, P[0], caps_mat, mul(axis, -1.0 if profile[1][0] > profile[0][0] else 1.0))
    if caps[1]:
        cap(mesh, P[-1], caps_mat, mul(axis, 1.0 if profile[-1][0] > profile[-2][0] else -1.0))
    return P


def frames_along(path, up):
    """Parallel-transport frames (tangent, n1, n2) along a polyline path, starting with n1 as near `up` as can be."""
    out = []
    n1 = None
    for k in range(len(path)):
        a = path[max(0, k - 1)]
        b = path[min(len(path) - 1, k + 1)]
        tangent = unit(sub(b, a))
        if n1 is None:
            n1 = unit(sub(up, mul(tangent, dot(up, tangent))))
        else:
            n1 = unit(sub(n1, mul(tangent, dot(n1, tangent))))
        n2 = cross(tangent, n1)
        out.append((tangent, n1, n2))
    return out


def sweep(mesh, path, radius, mat, seg=10, up=(0.0, 0.0, 1.0), caps=(True, True), squash=1.0, uv=None):
    """A tube along `path`, radius(k) at its k-th point (an oval `squash` times as wide along n2), each end capped."""
    frames = frames_along(path, up)
    P = []
    for k, c in enumerate(path):
        r = radius(k)
        P.append(ring_points(c, frames[k], r, r * squash, seg))
    surface(mesh, P, mat, closed=True, uv=uv)
    m0 = mat if not callable(mat) else mat(0, 0)
    if caps[0]:
        cap(mesh, P[0], m0, mul(frames[0][0], -1.0))
    if caps[1]:
        cap(mesh, P[-1], m0, frames[-1][0])
    return P


def crystal(mesh, origin, frame, length_, radius, mat, seg=6, waist=0.55, tip=0.12, phase=0.5):
    """A faceted crystal along frame[0]: a `seg`-sided prism `waist` of its length long, pointed at both ends to a
    small flat tip (`tip` of the radius), every facet flat."""
    half = length_ / 2.0
    w = waist * half
    profile = [(-half, radius * tip), (-w, radius), (w, radius), (half, radius * tip)]
    lathe(mesh, profile, mat, seg=seg, origin=origin, frame=frame, hard=range(4), flat_round=True, phase=phase,
          uv=lambda i, j, di, dj: ((i + di) / 3.0, (j + dj) / seg))


def frame_toward(axis, hint=(0.0, 0.0, 1.0)):
    """A frame whose first vector is `axis`."""
    a = unit(axis)
    h = hint if abs(dot(unit(hint), a)) < 0.95 else (1.0, 0.0, 0.0)
    e1 = unit(sub(h, mul(a, dot(h, a))))
    e2 = cross(a, e1)
    return (a, e1, e2)


# ---------------------------------------------------------------- blades

GROOVE_MIN = 0.03   # design units: a channel's width where it has run out (a sliver, so no quad turns into a triangle)
FLOOR = 0.16        # design units: the least half-thickness of a blade under its channel


def blade(mesh, s0, s1, rings, spine, edge, half_spine, half_flat, bevel, groove=None, glyph_seed=0,
          spine_round=0.35, edge_mat=EDGE, flat_mat=STEEL, spine_mat=SPINE, at=None, edge_period=None):
    """A single-edged blade lofted from s0 to s1 through `rings` sections (or at the sections `at`): at s its back is
    at t = spine(s) and its edge at t = edge(s); the back is half_spine(s) thick either side of w = 0, the flats
    half_flat(s), ground down over the last bevel(s) of the width to the edge. `groove(s)` -> (centre t, width,
    depth) cuts a rune channel into both flats; where it is sunk and over 40% of its widest, its floor is glowing
    glyphs, one every two sections. `edge_period`: the edge's material repeats every that many sections along (a
    pattern drawn along it, as the nodachi's temper line). Returns the sections (rings of points)."""
    ss = at or [s0 + (s1 - s0) * k / (rings - 1) for k in range(rings)]
    rows, depths = [], []
    for s in ss:
        te, ts = edge(s), spine(s)
        sign = 1.0 if te >= ts else -1.0
        width = abs(te - ts)
        hs, hf = half_spine(s), half_flat(s)
        bw = min(bevel(s), width * 0.6)
        tb = te - sign * bw
        r = min(spine_round, width * 0.18)
        g_c, g_w, g_d = groove(s) if groove else (lerp(tb, ts, 0.5), 0.0, 0.0)
        lo, hi = sorted((ts + sign * (r + 0.04), tb - sign * 0.04))
        gw = max(GROOVE_MIN, min(g_w, (hi - lo) * 0.9)) / 2.0
        g_c = min(max(g_c, lo + gw), hi - gw) if hi - lo > 2 * gw else (lo + hi) / 2.0
        # Each channel's floor keeps FLOOR from the middle, so the two floors stay over 0.1 px apart back to back.
        g_d = max(0.0, min(g_d, hf * 0.45, hf - FLOOR))
        inset = min(0.12, gw * 0.3)
        side = [(te, 0.0), (lerp(te, tb, 0.55), hf * 0.62), (tb, hf),
                (g_c + sign * gw, hf), (g_c + sign * (gw - inset), hf - g_d),
                (g_c - sign * (gw - inset), hf - g_d), (g_c - sign * gw, hf),
                (ts + sign * r, hs), (ts + sign * r * 0.3, hs * 0.75)]
        rows.append((side, ts))
        depths.append(gw if g_d > 0.02 else 0.0)
    deepest = max(depths) or 1.0
    n = len(rows[0][0])            # points a side; the loop is the edge, one side, the back, the other side
    P = []
    for k, (side, ts) in enumerate(rows):
        s = ss[k]
        P.append([(t, s, w) for t, w in side] + [(ts, s, 0.0)] + [(t, s, -w) for t, w in side[1:]][::-1])
    J = len(P[0])                  # 2n: point j of the top side is point J - j of the bottom
    hard = {0, 2, 3, 4, 5, 6, 7}   # the edge, bevel to flat, the channel's lips and floor corners, the back's shoulder
    hard_j = {h for h in hard} | {(J - h) % J for h in hard}

    def panel(j):
        """Which part segment j (point j to j + 1) is, counted as on the top side."""
        idx = j if j < n else J - 1 - j
        return ("edge", "edge", "flat", "wall", "floor", "wall", "flat", "spine", "spine")[idx]

    def lit(i):
        return depths[i] > deepest * 0.4 and depths[i + 1] > deepest * 0.4

    glyphs = {}
    count = 0
    for i in range(len(ss) - 1):
        if lit(i):
            glyphs[i] = count
            count += 1

    def mat(i, j):
        p = panel(j)
        if p == "floor":
            return glyph(glyph_seed + glyphs[i] // 2, glyphs[i] % 2) if i in glyphs else CHANNEL
        if p == "wall":
            return CHANNEL
        return {"edge": edge_mat, "flat": flat_mat, "spine": spine_mat}[p]

    def uvf(i, j, di, dj):
        top = j < n
        if panel(j) == "floor" and i in glyphs:
            return (di, dj if top else 1 - dj)
        a = (ss[i + di] - ss[0]) / (ss[-1] - ss[0])
        if edge_period and panel(j) == "edge":
            a = ((i % edge_period) + di) / edge_period
        q = P[i + di][(j + dj) % J]
        te, ts = edge(ss[i + di]), spine(ss[i + di])
        b = 0.5 if abs(te - ts) < 1e-6 else (q[0] - ts) / (te - ts)
        return (a, b)
    surface(mesh, P, mat, closed=True, hard_j=hard_j, uv=uvf)
    cap(mesh, P[0], flat_mat, (0.0, -1.0, 0.0))
    cap(mesh, P[-1], edge_mat, (0.0, 1.0, 0.0))
    return P


# ---------------------------------------------------------------- the four arms


def lathe_uv(seg, period=None, rings=None):
    """(a, b) for lathe quads: a along (repeating every `period` rings, or across all `rings`), b round."""
    def uv(i, j, di, dj):
        a = ((i % period) + di) / period if period else (i + di) / max(1, rings - 1)
        return (a, (j + dj) / seg)
    return uv


def runebound_moonblade():
    """A crescent moonblade of moon steel: a ground edge along its belly and a glowing channel of glyphs following
    it, a crescent-moon guard with glowing horn tips round a heart crystal in a silver bezel, a cord-wrapped grip
    and a faceted crystal pommel in an iron cup. Design units as tools/arms_variants_art.py (50 long, held at 7)."""
    m = Mesh()
    # The pommel: a crystal sitting in an iron cup.
    crystal(m, (0.0, 1.1, 0.0), AXIS, 2.2, 0.9, CRYSTAL, seg=6, waist=0.5)
    lathe(m, [(1.6, 0.98), (1.8, 1.2), (2.4, 1.22), (2.75, 1.06), (2.9, 1.0)], IRON, seg=14, hard=(0, 4))
    # The grip: a cord wrap between two iron ferrules.
    prof = [(3.25 + 8.3 * k / 17.0, 0.98 + 0.08 * math.sin(math.pi * k / 17.0)) for k in range(18)]
    lathe(m, prof, CORD, seg=14, uv=lathe_uv(14, period=2))
    for s0, s1, r in ((2.55, 3.6, 1.18), (11.2, 12.25, 1.2)):  # half a facet round from the grip's facets
        lathe(m, [(s0, r - 0.08), (s0 + 0.08, r), (s1 - 0.08, r), (s1, r - 0.08)], IRON, seg=14, hard=(1, 2), phase=0.5)
    # The guard: a crescent moon, horns curling up towards the blade, thickest in the middle.
    n = 25
    path, rad = [], []
    for k in range(n):
        u = -1.0 + 2.0 * k / (n - 1)
        a = u * 1.25
        path.append((4.7 * math.sin(a) / math.sin(1.25), 12.7 + 2.4 * (1 - math.cos(a)) / (1 - math.cos(1.25)), 0.0))
        rad.append(0.16 + 0.98 * (1 - abs(u) ** 1.7))
    sweep(m, path, lambda k: rad[k], IRON, seg=10, up=(0.0, 0.0, 1.0), squash=1.25,
          uv=lambda i, j, di, dj: ((i + di) / (n - 1), (j + dj) / 10))
    for end in (0, n - 1):
        tip = path[end]
        out = unit(sub(path[end], path[end - 1 if end else 1]))
        crystal(m, add(tip, mul(out, 0.35)), frame_toward(out), 1.4, 0.36, CRYSTAL, seg=6, waist=0.3)
    # The heart: a crystal through the guard in a silver bezel.
    w_axis = ((0.0, 0.0, 1.0), (1.0, 0.0, 0.0), (0.0, 1.0, 0.0))
    lathe(m, [(-1.42, 1.0), (-1.32, 1.12), (1.32, 1.12), (1.42, 1.0)], SILVER, seg=16, origin=(0.0, 12.7, 0.0),
          frame=w_axis, hard=(1, 2))
    crystal(m, (0.0, 12.7, 0.0), w_axis, 3.5, 0.82, CRYSTAL, seg=8, waist=0.35, tip=0.3)
    # The blade.
    spine_pts = [(13.0, -1.5), (13.6, -1.6), (22.0, -2.2), (30.0, -2.4), (38.0, -2.25), (44.0, -1.8), (47.5, -1.0),
                 (49.2, -0.35), (50.0, -0.02)]
    edge_pts = [(13.0, 1.55), (13.6, 1.8), (19.0, 4.6), (28.0, 6.3), (38.0, 5.4), (44.5, 2.9), (47.5, 1.3),
                (49.2, 0.4), (50.0, 0.02)]

    def spine(s):
        return spline(spine_pts, s)

    def edge(s):
        return spline(edge_pts, s)

    def thick(s):
        x = (s - 13.0) / 37.0
        return lerp(0.62, 0.12, x ** 1.15)

    def groove(s):
        ts, te = spine(s), edge(s)
        k = smooth((s - 15.2) / 2.2) * smooth((44.2 - s) / 2.6)
        return (ts + 0.5 * (te - ts), 0.8 * k, 0.2 * k)
    blade(m, 13.0, 50.0, 41, spine, edge, thick, lambda s: thick(s) * 0.86,
          lambda s: min(1.9, 0.3 * (edge(s) - spine(s))), groove=groove, glyph_seed=0)
    return m


def runebound_nodachi():
    """A long curved nodachi of moon steel: a frosted wave of temper along its ground edge, a glowing channel of
    glyphs in the flat by the back, a silver habaki, an oval tsuba round whose rim runs a ring of light, and a long
    diamond-wrapped tsuka with an iron kashira. 56 units long, held at 9."""
    m = Mesh()
    oval = 0.8                                     # the tsuka and its fittings are oval: thinner through w
    lathe(m, [(0.0, 0.8, 0.8 * oval), (0.12, 1.08, 1.08 * oval), (0.7, 1.2, 1.2 * oval), (1.6, 1.16, 1.16 * oval)],
          IRON, seg=16, hard=(1,))
    rings = 31
    prof = []
    for k in range(rings):
        s = 1.25 + 14.6 * k / (rings - 1)
        r = 1.06 + 0.05 * math.sin(math.pi * k / (rings - 1))
        prof.append((s, r, r * oval))
    lathe(m, prof, SILK, seg=16, uv=lathe_uv(16, period=6), phase=0.25)
    lathe(m, [(15.4, 1.12, 1.12 * oval), (15.48, 1.22, 1.22 * oval), (16.55, 1.22, 1.22 * oval)], IRON, seg=16,
          hard=(1,))
    # The tsuba: an oval plate, a glowing ring sunk round its rim.
    t_prof = [(16.2, 2.84), (16.28, 3.0), (16.5, 3.0), (16.52, 2.86), (16.88, 2.86), (16.9, 3.0), (17.12, 3.0),
              (17.2, 2.84)]

    def tsuba_mat(i, j):
        return BAND if i == 3 else IRON

    def tsuba_uv(i, j, di, dj):
        if i == 3:
            return (((j % 4) + dj) / 4.0, di)
        return ((i + di) / 7.0, (j + dj) / 24.0)
    lathe(m, [(s_, r, r * 0.86) for s_, r in t_prof], tsuba_mat, seg=24, hard=range(8), uv=tsuba_uv)
    # The habaki, a silver collar round the blade's root.
    lathe(m, [(16.85, 1.72, 0.66), (17.1, 1.8, 0.72), (18.7, 1.76, 0.7), (18.9, 1.66, 0.62)], GILT, seg=16,
          hard=(1, 2), origin=(-0.02, 0.0, 0.0))

    def bend(s):
        return 0.0016 * (s - 18.8) ** 2 if s > 18.8 else 0.0
    tip_s = 56.0
    tip_t = -0.6 - bend(52.5) * 1.3

    def spine(s):
        if s <= 52.5:
            return -bend(s) - 1.45 + 0.15 * (s - 18.6) / 33.9
        return spline([(52.5, -bend(52.5) - 1.3), (54.2, -bend(54.2) - 1.22), (tip_s, tip_t - 0.02)], s)

    def edge(s):
        if s <= 52.5:
            return -bend(s) + 1.5 - 0.2 * (s - 18.6) / 33.9
        x = (s - 52.5) / (tip_s - 52.5)
        e0 = -bend(52.5) + 1.3
        return lerp(e0, tip_t + 0.02, x ** 1.8)

    def thick(s):
        return lerp(0.5, 0.32, (s - 18.6) / 37.4) * (1.0 if s < 53.5 else max(0.08, 1 - (s - 53.5) / 2.5))

    def groove(s):
        ts, te = spine(s), edge(s)
        k = smooth((s - 20.4) / 1.8) * smooth((49.5 - s) / 2.2)
        return (ts + 0.24 * (te - ts), 0.62 * k, 0.16 * k)
    at = [18.6 + 0.9 * k for k in range(38)] + [53.0, 53.6, 54.2, 54.8, 55.4, 55.95]
    blade(m, 18.6, 56.0, 0, spine, edge, lambda s: thick(s) * 0.86, thick,
          lambda s: 0.6 * (edge(s) - spine(s)), groove=groove, glyph_seed=3, spine_round=0.2, edge_mat=HAMON,
          at=at, edge_period=4)
    return m


def staff_end(m, top):
    """An end of the staff: an iron ferrule whose three claws hold a floating crystal (top: the upper end)."""
    sign = 1.0 if top else -1.0
    base = 50.0 if top else 0.0

    def at(d):
        return base - sign * d          # d: how far in from the end
    prof = [(at(4.6), 0.98), (at(4.45), 1.14), (at(3.4), 1.12), (at(3.1), 1.26), (at(2.75), 1.26), (at(2.6), 1.06)]
    if not top:
        prof = prof[::-1]
    lathe(m, prof, IRON, seg=16, hard=range(len(prof)))
    crystal(m, (0.0, at(1.1), 0.0), AXIS, 2.3, 0.72, CRYSTAL, seg=6, waist=0.4)
    for k in range(3):
        a = 2 * math.pi * (k / 3.0 + 1 / 12.0)
        out = (math.cos(a), 0.0, math.sin(a))
        path = []
        for q in range(9):
            u = q / 8.0
            r = 1.02 + 0.42 * math.sin(math.pi * min(1.0, u * 1.15)) - 0.5 * u * u
            path.append(add(mul(out, r), (0.0, at(2.85 - 2.75 * u), 0.0)))
        sweep(m, path, lambda q: 0.3 * (1 - q / 8.0) + 0.05, IRON, seg=8, up=(0.0, 1.0, 0.0))


def runebound_staff():
    """A quarterstaff of dark ironwood: a glowing helix of runes winding up each half, a violet leather grip between
    iron collars, and at each end an iron ferrule whose three claws hold a floating crystal. 50 long, held at 25."""
    m = Mesh()
    for s0, s1 in ((3.0, 19.55), (30.45, 47.0)):     # each end sunk in its ferrule and collar
        n = 18
        prof = [(s0 + (s1 - s0) * k / (n - 1), 0.88 + 0.06 * math.sin(math.pi * ((s0 + (s1 - s0) * k / (n - 1)) - 2.6) / 44.8))
                for k in range(n)]
        lathe(m, prof, WOOD, seg=14, uv=lathe_uv(14, rings=n))
        # The helix: two turns of glowing light, half sunk into the wood.
        path = []
        turns, steps = 2.0, 40
        for q in range(steps + 1):
            u = q / steps
            s = lerp(s0 + 1.2, s1 - 1.2, u)
            a = 2 * math.pi * turns * u + (0.0 if s0 < 20 else math.pi)
            r = 0.9 + 0.06 * math.sin(math.pi * (s - 2.6) / 44.8) + 0.03
            path.append((r * math.cos(a), s, r * math.sin(a)))
        sweep(m, path, lambda q: 0.26, GLOW, seg=6, up=(0.0, 1.0, 0.0))
    n = 12
    lathe(m, [(19.9 + 10.2 * k / (n - 1), 1.02 + 0.03 * math.sin(math.pi * k / (n - 1))) for k in range(n)], LEATHER,
          seg=14, uv=lathe_uv(14, rings=n))
    for s0, s1 in ((19.2, 20.2), (29.8, 30.8)):       # half a facet round from the grip's facets
        lathe(m, [(s0, 1.08), (s0 + 0.1, 1.2), (s1 - 0.1, 1.2), (s1, 1.08)], IRON, seg=14, hard=(1, 2), phase=0.5)
    staff_end(m, True)
    staff_end(m, False)
    return m


def head_ring(t, s0, half_s, half_w, chamfer, recess):
    """A ring of the war hammer's head across t: an octagon in (s, w) round s0, its corners cut by `chamfer`, with a
    recess (half-length along s, depth) sunk in the middle of each broad face (w = +-half_w); 16 points."""
    d, depth = recess
    d = max(d, GROOVE_MIN)
    inset = min(0.12, d * 0.4)
    hs, hw, c = half_s, half_w, chamfer
    face = [(s0 - d, hw), (s0 - d + inset, hw - depth), (s0 + d - inset, hw - depth), (s0 + d, hw)]
    pts = [(s0 + hs, hw - c), (s0 + hs - c, hw)]
    pts += face[::-1]
    pts += [(s0 - hs + c, hw), (s0 - hs, hw - c), (s0 - hs, -hw + c), (s0 - hs + c, -hw)]
    pts += [(s_, -w) for s_, w in face]
    pts += [(s0 + hs - c, -hw), (s0 + hs, -hw + c)]
    return [(t, s_, w) for s_, w in pts]


def runebound_war_hammer():
    """A war hammer: a chamfered octagonal head of dark moon steel with a glowing sigil sunk in each side, a curved
    beak tapering behind it and a spike above, iron langets down an ironwood haft banded in iron, a cord-wrapped
    grip and an iron pommel. 38 long, held at 6."""
    m = Mesh()
    lathe(m, [(-0.2, 0.55), (0.0, 0.95), (0.5, 1.28), (1.05, 1.2), (1.5, 0.9), (1.7, 0.8)], IRON, seg=14, hard=(0,))
    lathe(m, [(1.2 + 24.8 * k / 16, 0.82) for k in range(17)], WOOD, seg=12, uv=lathe_uv(12, rings=17))
    n = 16
    lathe(m, [(2.4 + 7.6 * k / (n - 1), 0.96 + 0.04 * math.sin(math.pi * k / (n - 1))) for k in range(n)], CORD,
          seg=14, uv=lathe_uv(14, period=2))
    for s0, s1, r in ((2.05, 2.85, 1.1), (9.7, 10.5, 1.1), (15.6, 16.4, 0.98), (19.6, 20.2, 0.96)):
        # Turned half a facet against the haft, so no facet of a band lies parallel to one of the haft's.
        lathe(m, [(s0, r - 0.08), (s0 + 0.08, r), (s1 - 0.08, r), (s1, r - 0.08)], IRON, seg=12, hard=(1, 2), phase=0.5)
    # The socket, octagonal, with a lip, and the langets down the haft.
    lathe(m, [(24.6, 1.14), (24.7, 1.32), (25.4, 1.32), (25.5, 1.16), (31.0, 1.16)], IRON, seg=8, hard=range(5),
          flat_round=True, phase=0.5)
    for side in (1.0, -1.0):
        P = []
        for k in range(6):
            s = lerp(20.8, 25.0, k / 5.0)
            half = lerp(0.12, 0.4, smooth(k / 5.0))
            w0, w1 = 0.72, 0.98
            P.append([(-half, s, side * w0), (half, s, side * w0), (half, s, side * w1), (-half, s, side * w1)])
        surface(m, P, IRON, closed=True, hard_j=range(4))
        cap(m, P[0], IRON, (0.0, -1.0, 0.0))
    # The head and its beak, one piece lofted across t: flared towards its face, a sigil sunk in each side.
    s0, c = 30.4, 0.72

    def half_s(t):
        return 2.72 + 0.42 * smooth((t - 2.4) / 3.4) + 0.2 * smooth((0.6 - t) / 1.6)

    def half_w(t):
        return 1.74 + 0.28 * smooth((t - 2.4) / 3.4) + 0.14 * smooth((0.6 - t) / 1.6)

    def sigil(t):
        k = max(0.0, 1.0 - abs(t - 3.3) / 1.7)
        return (1.55 * k, min(0.3, 0.6 * 1.55 * k))
    rings, hard_i = [], [0, 1]
    for t in (6.55, 6.1):
        sc = 0.86 if t > 6.3 else 1.0
        rings.append(head_ring(t, s0, half_s(6.1) * sc, half_w(6.1) * sc, c * sc, (0.0, 0.0)))
    for t in (5.6, 5.0, 4.6, 4.2, 3.8, 3.3, 2.8, 2.4, 2.0, 1.6, 1.0, 0.3, -0.4, -1.0):
        rings.append(head_ring(t, s0, half_s(t), half_w(t), c, sigil(t)))
    hard_i.append(len(rings) - 1)
    beak = 7.2
    for k in range(10):
        u = k / 9.0
        t = -1.25 - (beak - 1.25) * u
        drop = 1.6 * u ** 1.6
        size = lerp(1.0, 0.04, u ** 0.9)
        rings.append(head_ring(t, s0 - drop, 1.45 * size, 1.05 * size, 0.5 * size, (0.0, 0.0)))
    hard_i.append(len(rings) - 10)
    lit = {i for i in range(len(rings) - 1) if sigil(rings[i][0][0])[1] > 0.05 and sigil(rings[i + 1][0][0])[1] > 0.05}
    J = len(rings[0])
    floors = (3, J - 5)

    def mat(i, j):
        if j in floors and i in lit:
            return SIGIL
        if j in (2, 4, J - 6, J - 4) and i in lit:
            return CHANNEL
        return HEAD

    def uvf(i, j, di, dj):
        q = rings[i + di][(j + dj) % J]
        if j in floors and i in lit:
            return ((q[0] - 1.6) / 3.4, (q[1] - (s0 - 1.7)) / 3.4)
        return ((q[0] + 7.2) / 13.8, (j + dj) / J)
    surface(m, rings, mat, closed=True, hard_i=hard_i, hard_j=range(J), uv=uvf)
    cap(m, rings[0], HEAD, (1.0, 0.0, 0.0))
    cap(m, rings[-1], HEAD, (-1.0, 0.0, 0.0))
    # The spike above.
    P = []
    for k in range(7):
        u = k / 6.0
        s = lerp(32.6, 37.6, u)
        r = lerp(0.95, 0.05, u ** 0.85)
        P.append([(r, s, 0.0), (0.0, s, r * 0.9), (-r, s, 0.0), (0.0, s, -r * 0.9)])
    surface(m, P, HEAD, closed=True, hard_j=range(4))
    cap(m, P[0], HEAD, (0.0, -1.0, 0.0))
    cap(m, P[-1], HEAD, (0.0, 1.0, 0.0))
    # A band of moon gold round the head behind its face, standing proud of it.
    P = []
    for t, grow in ((5.08, 0.0), (5.2, 0.24), (5.6, 0.24), (5.72, 0.0)):
        hs_, hw_ = half_s(t), half_w(t)
        ring = head_ring(t, s0, hs_ + grow, hw_ + grow, c + grow * 0.4, (0.0, 0.0))
        P.append(ring)
    surface(m, P, GILT, closed=True, hard_i=(1, 2), hard_j=range(len(P[0])))
    return m


DESIGNS = {"runebound_nodachi": runebound_nodachi, "runebound_moonblade": runebound_moonblade,
           "runebound_staff": runebound_staff, "runebound_war_hammer": runebound_war_hammer}


def design(name):
    return DESIGNS[name]()


# ---------------------------------------------------------------- the textures


def _ramp(c0, c1, k):
    return tuple(int(round(lerp(a, b, min(1.0, max(0.0, k))))) for a, b in zip(c0, c1)) + (255,)


def atlas():
    """The painted materials, 128x128: clean fills and smooth ramps, patterns drawn as shapes (no noise)."""
    from PIL import Image
    img = Image.new("RGBA", (ATLAS_SIZE, ATLAS_SIZE), (0, 0, 0, 0))
    px = img.load()

    def fill(mat, colour):
        x0, y0, x1, y1 = mat.region
        for y in range(y0, y1):
            for x in range(x0, x1):
                a, b = (x - x0 + 0.5) / (x1 - x0), (y - y0 + 0.5) / (y1 - y0)
                px[x, y] = colour(a, b)
    # Moon steel: violet silver, a shade lighter towards the edge, two fine polish lines along it.
    fill(STEEL, lambda a, b: _ramp((98, 90, 146), (130, 122, 178), b))
    fill(EDGE, lambda a, b: _ramp((170, 172, 212), (242, 244, 255), (b - 0.6) / 0.4))
    fill(SPINE, lambda a, b: _ramp((70, 64, 106), (96, 90, 136), b))
    fill(IRON, lambda a, b: (66, 60, 94, 255))
    fill(SILVER, lambda a, b: _ramp((176, 180, 210), (222, 226, 244), 1 - abs(b - 0.5) * 2))
    fill(HEAD, lambda a, b: (104, 98, 140, 255))
    fill(CHANNEL, lambda a, b: (34, 30, 56, 255))

    def silk(a, b):
        # Diamonds of pale ray skin between crossing violet cords: two round the grip, one along a repeat.
        u, v = a, (b * 2.0) % 1.0
        d = abs(u - 0.5) * 2 + abs(v - 0.5) * 2           # 0 at a diamond's centre, 1 at its corners
        if d < 0.62:
            return (198, 190, 222, 255) if d < 0.5 else (150, 140, 182, 255)
        if d < 0.7:
            return (44, 28, 66, 255)
        return (124, 82, 176, 255) if (u > 0.5) == (v > 0.5) else (98, 62, 146, 255)
    fill(SILK, silk)

    def cord(a, b):
        # Four cords spiralling round: bands on the diagonal with a dark line between.
        q = (a + b * 4.0) % 1.0
        if q < 0.12:
            return (48, 28, 74, 255)
        return _ramp((96, 60, 142), (136, 96, 188), 1 - abs(q - 0.56) / 0.44)
    fill(CORD, cord)
    fill(LEATHER, lambda a, b: (98, 62, 140, 255) if int(b * 16) % 8 else (78, 46, 116, 255))

    def wood(a, b):
        y = int(b * 32)
        if y % 8 == 3:
            return (66, 48, 66, 255)
        if y % 8 == 6:
            return (100, 78, 98, 255)
        return (84, 64, 84, 255)
    fill(WOOD, wood)
    fill(CRYSTAL, lambda a, b: _ramp((70, 200, 240), (200, 250, 255), 1 - abs(a - 0.5) * 1.6) if int(b * 8) % 4
         else _ramp((150, 240, 255), (240, 255, 255), 1 - abs(a - 0.5) * 1.6))
    def hamon(a, b):
        # The temper line: a frosted wave (b = 1 at the edge) over darker ground steel.
        line = 0.7 + 0.05 * math.sin(a * 2 * math.pi) + 0.02 * math.sin(a * 4 * math.pi + 1.0)
        if b > line + 0.05:
            return _ramp((214, 218, 242), (244, 246, 255), (b - line) / 0.3)
        if b > line:
            return (236, 240, 255, 255)
        return _ramp((112, 106, 156), (150, 146, 190), (b - 0.38) / 0.32)
    fill(HAMON, hamon)
    fill(GILT, lambda a, b: _ramp((176, 150, 96), (240, 222, 160), 1 - abs(b - 0.4) * 1.6))
    # The gaps between regions take their neighbours' colours, so the sprite is solid and no see-through edge bleeds
    # into a smaller mipmap.
    while True:
        empty = [(x, y) for y in range(ATLAS_SIZE) for x in range(ATLAS_SIZE) if px[x, y][3] == 0]
        if not empty:
            break
        grown = {}
        for x, y in empty:
            for dx, dy in ((-1, 0), (0, -1), (1, 0), (0, 1)):
                if 0 <= x + dx < ATLAS_SIZE and 0 <= y + dy < ATLAS_SIZE and px[x + dx, y + dy][3] == 255:
                    grown[x, y] = px[x + dx, y + dy]
                    break
        for (x, y), colour in grown.items():
            px[x, y] = colour
    return img


# The glyphs: original rune shapes on an 8x8 cell, strokes as (x0, y0, x1, y1) lines (x along the channel).
GLYPHS = [
    [(1, 1, 1, 6), (1, 1, 5, 3), (1, 4, 5, 6)],
    [(3, 1, 3, 6), (1, 2, 3, 4), (5, 2, 3, 4)],
    [(1, 1, 5, 6), (5, 1, 1, 6)],
    [(1, 1, 1, 6), (5, 1, 5, 6), (1, 3, 5, 4)],
    [(3, 1, 3, 6), (1, 1, 5, 1), (1, 6, 5, 6)],
    [(1, 6, 3, 1), (3, 1, 5, 6), (2, 4, 4, 4)],
    [(1, 1, 1, 6), (1, 3, 5, 1), (1, 3, 5, 6)],
    [(1, 3, 3, 1), (3, 1, 5, 3), (5, 3, 3, 6), (3, 6, 1, 3)],
    [(2, 1, 2, 6), (4, 1, 4, 6), (2, 3, 4, 3)],
    [(1, 1, 5, 1), (5, 1, 1, 6), (1, 6, 5, 6)],
    [(3, 1, 3, 6), (3, 3, 5, 1), (3, 3, 1, 1)],
    [(1, 6, 1, 1), (1, 1, 5, 6), (5, 6, 5, 1)],
]


def rune_frames():
    """The glowing glyph strip: RUNE_FRAMES frames of 32x32 (3 rows of 4 glyphs, then a plain band): white strokes on
    clean cyan light, so from afar a channel reads as one glowing line and up close as runes. It pulses from bright to
    brighter (Minecraft blends between the frames: "interpolate")."""
    from PIL import Image, ImageDraw
    frames = []
    for f in range(RUNE_FRAMES):
        k = 0.5 - 0.5 * math.cos(2 * math.pi * f / RUNE_FRAMES)       # 0 .. 1 .. 0
        light = _ramp((56, 188, 228), (104, 228, 255), k)
        edge = _ramp((40, 150, 196), (70, 196, 236), k)
        core = _ramp((214, 250, 255), (250, 255, 255), k)
        img = Image.new("RGBA", (RUNE_SIZE, RUNE_SIZE), light)
        draw = ImageDraw.Draw(img)
        for index, strokes in enumerate(GLYPHS):
            x0, y0 = 8 * (index % GLYPH_COLS), 8 * (index // GLYPH_COLS)
            draw.line((x0, y0, x0 + 7, y0), fill=edge)          # the channel's two edges, a shade deeper
            draw.line((x0, y0 + 7, x0 + 7, y0 + 7), fill=edge)
            for a0, b0, a1, b1 in strokes:
                draw.line((x0 + a0, y0 + b0, x0 + a1, y0 + b1), fill=core)
        for y, colour in ((24, edge), (25, light), (26, light), (27, core), (28, core), (29, light), (30, light),
                          (31, edge)):
            draw.line((0, y, 31, y), fill=colour)
        # The sigil: a diamond pierced by a stave, two bars across it (the Runecarver's Pattern's emblem).
        ox = 32
        draw.polygon([(ox + 16, 1), (ox + 30, 15), (ox + 16, 30), (ox + 1, 15)], outline=edge)
        for w in range(2):
            draw.polygon([(ox + 16, 6 + w), (ox + 25 - w, 15), (ox + 16, 25 - w), (ox + 7 + w, 15)], outline=core)
        draw.rectangle((ox + 15, 3, ox + 16, 28), fill=core)
        draw.rectangle((ox + 11, 12, ox + 20, 13), fill=core)
        draw.rectangle((ox + 11, 18, ox + 20, 19), fill=core)
        frames.append(img)
    return frames


def rune_strip():
    from PIL import Image
    frames = rune_frames()
    strip = Image.new("RGBA", (RUNE_SIZE, RUNE_SIZE * len(frames)))
    for k, f in enumerate(frames):
        strip.paste(f, (0, RUNE_SIZE * k))
    return strip


def rune_animation():
    return {"frametime": RUNE_FRAMETIME, "interpolate": True}


# ---------------------------------------------------------------- into the model


C45 = math.sqrt(0.5)


def placer(grip, grip_model, unit_):
    """Design (t, s, w) -> model pixels: `unit_` pixels a design unit, the hand (s = grip, t = 0) at grip_model, the
    weapon turned 45 degrees about the hand to lie along the diagonal (as tools/arms_pixel.py model_elements turns
    its boxes), so the pose that held the box model holds this one."""
    gx, gy = grip_model

    def point(p):
        x, y = p[0] * unit_, (p[1] - grip) * unit_
        return (gx + (x + y) * C45, gy + (y - x) * C45, 8.0 + p[2] * unit_)

    def normal(n):
        return (round((n[0] + n[1]) * C45, 2) + 0.0, round((n[1] - n[0]) * C45, 2) + 0.0, round(n[2], 2) + 0.0)
    return point, normal


def export(mesh, grip, grip_model, unit_):
    """The quads as the model JSON's "quads": {"t": slot, "e": 1 if it glows, "v": four corners of
    [x, y, z (model pixels), u, v (0 to 1 of the sprite), nx, ny, nz]}, rounded so the output is stable (positions to
    a thousandth of a pixel, normals to a hundredth: Minecraft packs normals into bytes)."""
    point, normal = placer(grip, grip_model, unit_)
    out = []
    for q in mesh.quads:
        v = []
        for p, (u, vv), n in zip(q.points, q.uvs, q.normals):
            x, y, z = point(p)
            v.append([round(x, 3) + 0.0, round(y, 3) + 0.0, round(z, 3) + 0.0, round(u, 4) + 0.0, round(vv, 4) + 0.0,
                      *normal(n)])
        entry = {"t": q.mat.slot, "v": v}
        if q.mat.glow:
            entry["e"] = 1
        out.append(entry)
    return out


def placement(name):
    """(the design's grip, the hand in model pixels, model pixels a design unit) of a Runebound arm: those of the
    restyle design it replaces (tools/arms_variants_art.py layout and model), so the hand poses hold it as they held
    the box model."""
    import arms
    import arms_variants
    import arms_variants_art
    held = arms.KINDS[arms_variants.kind(name)]["held"]
    size, grip_px, scale, _factor = arms_variants_art.layout(name, held)
    unit_ = scale * math.sqrt(2.0) * 16.0 / size
    grip_model = (grip_px[0] * 16.0 / size, 16.0 - grip_px[1] * 16.0 / size)
    return arms_variants_art.design(name).grip, grip_model, unit_


def held_model(name, box):
    """The arm's in-hand model: its mesh, read by client/MeshItemModels.java, with the box model `box` (as
    tools/arms_variants.py held_model makes it: textures, elements, display) kept as "elements" for a game without
    that loader ("optional"). The display (hand poses) is the box model's."""
    grip, grip_model, unit_ = placement(name)
    textures = dict(box["textures"])
    textures.update({"particle": f"{MOD}:item/{ATLAS}", "mesh": f"{MOD}:item/{ATLAS}", "rune": f"{MOD}:item/{RUNE}"})
    return {"fabric:type": {"id": f"{MOD}:mesh", "optional": True}, "textures": textures, "display": box["display"],
            "elements": box["elements"], "quads": export(design(name), grip, grip_model, unit_)}


def draw(name):
    """The arm's icon, rendered from its mesh."""
    grip, grip_model, unit_ = placement(name)
    return icon(design(name), grip, grip_model, unit_)


def write_model(path, model):
    """The model JSON, one quad a line (a mesh written with indent=2 would be ten times the size)."""
    import json
    quads = model["quads"]
    head = {k: v for k, v in model.items() if k != "quads"}
    text = json.dumps(head, separators=(",", ":"))[:-1]
    text += ',"quads":[\n' + ",\n".join(json.dumps(q, separators=(",", ":")) for q in quads) + "\n]}\n"
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8")


# ---------------------------------------------------------------- an offline rasterizer (icons and previews)


def rasterize(quads, textures, width, height, shade, background=None, persp=None, cull=False):
    """Draws quads [(slot, glow, [(x, y, depth), ...4] in pixels (bigger depth nearer), [(u, v)]*4, [normal]*4)] with a
    depth buffer and nearest texels; `shade(normals (n, 3), glow) -> brightness (n,)`. `persp`: per-quad lists of four
    clip-space w for perspective-correct texturing; `cull`: skip triangles turned away (their corners clockwise on
    screen, y down). Returns (rgb float array, coverage bool array)."""
    import numpy as np
    rgb = np.zeros((height, width, 3), np.float32)
    if background is not None:
        rgb[:] = background
    depth = np.full((height, width), -np.inf, np.float32)
    tex = {k: np.asarray(v.convert("RGBA"), np.float32) for k, v in textures.items()}
    for qi, (slot, glow, pts, uvs, ns) in enumerate(quads):
        img = tex[slot]
        th, tw = img.shape[:2]
        P = np.array(pts, np.float64)
        UV = np.array(uvs, np.float64)
        N = np.array(ns, np.float64)
        W = np.array(persp[qi], np.float64) if persp is not None else np.ones(4)
        for tri in ((0, 1, 2), (0, 2, 3)):
            x, y, z = P[list(tri), 0], P[list(tri), 1], P[list(tri), 2]
            den = (y[1] - y[2]) * (x[0] - x[2]) + (x[2] - x[1]) * (y[0] - y[2])
            if abs(den) < 1e-12 or (cull and den > 0):
                continue
            x0, x1 = int(max(0, math.floor(x.min()))), int(min(width - 1, math.ceil(x.max())))
            y0, y1 = int(max(0, math.floor(y.min()))), int(min(height - 1, math.ceil(y.max())))
            if x1 < x0 or y1 < y0:
                continue
            gx, gy = np.meshgrid(np.arange(x0, x1 + 1) + 0.5, np.arange(y0, y1 + 1) + 0.5)
            a = ((y[1] - y[2]) * (gx - x[2]) + (x[2] - x[1]) * (gy - y[2])) / den
            b = ((y[2] - y[0]) * (gx - x[2]) + (x[0] - x[2]) * (gy - y[2])) / den
            c = 1.0 - a - b
            inside = (a >= -1e-9) & (b >= -1e-9) & (c >= -1e-9)
            if not inside.any():
                continue
            zz = a * z[0] + b * z[1] + c * z[2]
            sub_d = depth[y0:y1 + 1, x0:x1 + 1]
            hit = inside & (zz > sub_d)
            if not hit.any():
                continue
            iw = 1.0 / W[list(tri)]
            pa, pb, pc = a * iw[0], b * iw[1], c * iw[2]
            norm = pa + pb + pc
            pa, pb, pc = pa / norm, pb / norm, pc / norm
            uv = pa[..., None] * UV[tri[0]] + pb[..., None] * UV[tri[1]] + pc[..., None] * UV[tri[2]]
            nn = pa[..., None] * N[tri[0]] + pb[..., None] * N[tri[1]] + pc[..., None] * N[tri[2]]
            nn /= np.linalg.norm(nn, axis=-1, keepdims=True) + 1e-12
            ui = np.clip((uv[..., 0] * tw).astype(int), 0, tw - 1)
            vi = np.clip((uv[..., 1] * th).astype(int), 0, th - 1)
            texel = img[vi, ui]
            hit &= texel[..., 3] > 0
            k = shade(nn[hit], glow)
            sub_rgb = rgb[y0:y1 + 1, x0:x1 + 1]
            sub_rgb[hit] = texel[..., :3][hit] * k[:, None]
            sub_d[hit] = zz[hit]
    return rgb, np.isfinite(depth)


# ---------------------------------------------------------------- checks


def coplanar_overlaps(quads, gap=LIFT, min_area=1e-4):
    """Pairs of flat quads (lists of four (x, y, z) in model pixels) that are parallel (within 0.8 degrees, either
    facing), closer than `gap` pixels (the project's least separation) and overlap seen along their normal by more than
    `min_area`: drawn without culling, such a pair could flicker. Every pair is compared."""
    import numpy as np
    P = np.array(quads, np.float64)
    if len(P) < 2:
        return []
    N = np.cross(P[:, 2] - P[:, 0], P[:, 3] - P[:, 1])
    size = np.linalg.norm(N, axis=1)
    ok = size > 1e-9
    N[ok] /= size[ok, None]
    flat = ok & (np.abs(np.einsum("nk,njk->nj", N, P - P[:, :1])).max(axis=1) < 1e-3)
    idx = np.nonzero(flat)[0]
    P, N = P[idx], N[idx]
    parallel = np.abs(N @ N.T) > 0.9999
    np.fill_diagonal(parallel, False)
    lo, hi = P.min(axis=1), P.max(axis=1)
    near = np.all((lo[:, None, :] <= hi[None, :, :] + gap) & (lo[None, :, :] <= hi[:, None, :] + gap), axis=2)
    out = []
    for a, b in zip(*np.nonzero(np.triu(parallel & near))):
        n = N[a]
        if np.abs((P[b] - P[a, 0]) @ n).max() >= gap:
            continue
        e1 = P[a, 1] - P[a, 0]
        e1 /= np.linalg.norm(e1) + 1e-12
        e2 = np.cross(n, e1)

        def poly(q):
            pts = [(float(p @ e1), float(p @ e2)) for p in q]
            return pts if _area(pts) >= 0 else pts[::-1]
        clipped = poly(P[a])
        other = poly(P[b])
        for m_ in range(4):
            clipped = _clip(clipped, other[m_], other[(m_ + 1) % 4])
            if not clipped:
                break
        if clipped and _area(clipped) > min_area:
            out.append((int(idx[a]), int(idx[b])))
    return out


def _area(poly):
    return 0.5 * sum(poly[k][0] * poly[(k + 1) % len(poly)][1] - poly[(k + 1) % len(poly)][0] * poly[k][1]
                     for k in range(len(poly)))


def _clip(poly, a, b):
    """The part of a polygon left of the edge a -> b (Sutherland-Hodgman)."""
    out = []
    for k in range(len(poly)):
        p, q = poly[k], poly[(k + 1) % len(poly)]
        side_p = (b[0] - a[0]) * (p[1] - a[1]) - (b[1] - a[1]) * (p[0] - a[0])
        side_q = (b[0] - a[0]) * (q[1] - a[1]) - (b[1] - a[1]) * (q[0] - a[0])
        if side_p >= 0:
            out.append(p)
        if (side_p >= 0) != (side_q >= 0):
            k_ = side_p / (side_p - side_q)
            out.append((p[0] + (q[0] - p[0]) * k_, p[1] + (q[1] - p[1]) * k_))
    return out


# ---------------------------------------------------------------- the icon

ICON_LIGHT = unit((-0.62, 0.66, 0.42))
ICON_OUTLINE = (22, 14, 38)
ICON_GLOW_OUTLINE = (24, 110, 150)
ICON_BANDS = (0.5, 0.7, 0.9, 1.08, 1.3)


def icon(mesh, grip, grip_model, unit_, size=ICON, roll=0.0, tilt=35.0, fatten=1.4, bands=ICON_BANDS, supersample=6):
    """The inventory icon, rendered from the mesh: the arm on the diagonal as it lies in the hand (rolled `roll`
    degrees about its length, its point tipped `tilt` degrees away from the viewer), made `fatten` times as thick across so it reads at 48 pixels (the owner: icons "maybe
    not even accurate but cooler"), lit from the top left in a few flat bands of light (`bands`: no smooth blur), with
    glowing parts at full light, fitted inside a one-pixel outline that turns cyan beside a glowing part; solid
    pixels only (binary alpha)."""
    import numpy as np
    from PIL import Image
    point, normal = placer(grip, grip_model, unit_)

    def fat(p):
        return (p[0] * fatten, p[1], p[2] * fatten)

    def fat_n(n):
        return unit((n[0] / fatten, n[1], n[2] / fatten))
    axis = np.array([C45, C45, 0.0])
    a = math.radians(roll)
    K = np.array([[0, -axis[2], axis[1]], [axis[2], 0, -axis[0]], [-axis[1], axis[0], 0]])
    R = np.eye(3) + math.sin(a) * K + (1 - math.cos(a)) * (K @ K)
    across = np.array([C45, -C45, 0.0])
    b = math.radians(tilt)
    K = np.array([[0, -across[2], across[1]], [across[2], 0, -across[0]], [-across[1], across[0], 0]])
    R = (np.eye(3) + math.sin(b) * K + (1 - math.cos(b)) * (K @ K)) @ R
    pts = np.array([point(fat(p)) for q in mesh.quads for p in q.points]) @ R.T
    lo, hi = pts.min(0), pts.max(0)
    big = size * supersample
    span = max(hi[0] - lo[0], hi[1] - lo[1])
    scale = (size - 2.6) * supersample / span
    cx, cy = (lo[0] + hi[0]) / 2, (lo[1] + hi[1]) / 2
    quads = []
    for q in mesh.quads:
        P = np.array([point(fat(p)) for p in q.points]) @ R.T
        sp = [(big / 2 + (p[0] - cx) * scale, big / 2 - (p[1] - cy) * scale, p[2]) for p in P]
        ns = [tuple(R @ np.array(normal(fat_n(n)))) for n in q.normals]
        quads.append((q.mat.slot, q.mat.glow, sp, q.uvs, ns))
    half = ICON_LIGHT + np.array([0.0, 0.0, 1.0])
    half /= np.linalg.norm(half)
    steps = np.array(bands)

    def shade(n, glow):
        if glow:
            return np.ones(len(n))
        k = 0.42 + 0.78 * np.maximum(0.0, n @ ICON_LIGHT) + 0.5 * np.maximum(0.0, n @ half) ** 20
        if bands:
            k = steps[np.abs(k[:, None] - steps[None, :]).argmin(axis=1)]
        return k
    textures = {"mesh": atlas(), "rune": rune_frames()[RUNE_FRAMES // 2]}
    rgb, cov = rasterize(quads, textures, big, big, shade)
    glow_quads = [(sl, True, sp, uv, ns) if g else (sl, False, sp, uv, ns) for sl, g, sp, uv, ns in quads]
    mask_tex = {k: Image.new("RGBA", (1, 1), (255, 255, 255, 255)) for k in textures}
    glow_rgb, _ = rasterize([(sl, g, sp, [(0.0, 0.0)] * 4, ns) for sl, g, sp, uv, ns in glow_quads], mask_tex, big, big,
                            lambda n, g: np.full(len(n), 1.0 if g else 0.0))
    rgb = np.clip(rgb, 0, 255).reshape(size, supersample, size, supersample, 3)
    cov = cov.reshape(size, supersample, size, supersample)
    lit = glow_rgb[..., 0].reshape(size, supersample, size, supersample) > 127
    count = cov.sum(axis=(1, 3))
    colour = (rgb * cov[..., None]).sum(axis=(1, 3)) / np.maximum(1, count)[..., None]
    solid = count >= supersample * supersample * 0.45
    glowing = lit.sum(axis=(1, 3)) >= supersample * supersample * 0.3
    out = np.zeros((size, size, 4), np.uint8)
    out[solid, :3] = np.round(colour[solid]).astype(np.uint8)
    out[solid, 3] = 255

    def grow(m):
        g = np.zeros_like(m)
        g[1:, :] |= m[:-1, :]
        g[:-1, :] |= m[1:, :]
        g[:, 1:] |= m[:, :-1]
        g[:, :-1] |= m[:, 1:]
        return g
    edge = grow(solid) & ~solid
    near_glow = grow(glowing & solid)
    out[edge, :3] = ICON_OUTLINE
    out[edge & near_glow, :3] = ICON_GLOW_OUTLINE
    out[edge, 3] = 255
    return Image.fromarray(out, "RGBA")
