"""Offline geometry and texture checks for the art (docs/ART_DIRECTION.md, Rules for everything). NOT a game test.

Called from tools/check_mod_data.py main(); `python3 tools/art_check.py` runs it alone and prints the findings.
Each rule returns error strings, grouped one line per model or part; the allow-lists below name the only accepted
offenders, each with its reason.

Block, item and classic-pack models (assets/jugcraft/models, resourcepacks/alternate_machines):
  Z1  two faces on one plane, facing the same way, overlapping, drawing different texels (they flicker; rotated
      elements included through their turned faces).
  N1  the same with the planes 0 < gap < N1_GAP px apart: too close for the depth buffer at a distance.
  U1  a face's UV (written, or automatic from the element's position) leaves the sprite (0..16): the block atlas's
      neighbouring sprites show through.
  O1  an unrotated opaque box leaves out a face that nothing covers (a hole to look into); faces on the block's
      0/16 planes are the seams of models cut into several blocks and are not counted. Machines must have none.

Quad parts (assets/jugcraft/*_quads.json, kinetic_rotors.json, worn_models.json, drawn by client/QuadModel: plain quads
with RenderTypes.entitySolid, which culls back faces and ignores alpha; "cutout" with entityCutout and "nocull" with
entityTranslucent, which do not cull in 26.3):
  Z2  same-facing coplanar overlaps drawing different texels that are not buried inside the part, and opposite-facing
      pairs on one plane where either quad does not cull (draw a two-sided plane as two lifted sides instead).
  H1  the back of a culled quad is the nearest surface for more than H1_LIMIT of the part's silhouette, from 12
      views: a see-through hole (missing faces).
  A1  an entitySolid quad (or a Java RenderTypes.entitySolid texture) samples texels that are not fully opaque.
  A2  an entityCutout quad (or a Java RenderTypes.entityCutout texture) samples half-transparent texels (cutout draws
      them opaque).
"""
import json
import math
import re
import sys
import time
from collections import defaultdict
from pathlib import Path

import numpy as np
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src" / "main" / "resources" / "assets" / "jugcraft"
PACK = ROOT / "src" / "main" / "resources" / "resourcepacks" / "alternate_machines" / "assets" / "jugcraft"
CLIENT = ROOT / "src" / "client" / "java"

N1_GAP = 0.09          # faces closer than this (px) count as near-coplanar; model_writer pushes to COPLANAR_NUDGE = 0.1
PLANE_EPS = 1e-3       # px: faces this close count as one plane
H1_LIMIT = 0.005       # share of the silhouette
H1_SCALE = 1.5         # raster pixels per model pixel

# ------------------------------------------------------------------ allow-lists (every entry with its reason)

# O1: models that leave out faces on purpose, or decor whose own art fixes will close them (5 October 2026: the
# shared render fixes left these as they were). Keys are model families: a key also covers every model whose name
# starts with it plus "_" (numbered pieces, wear stages, lit/open states and the _item model). Machines may not be
# listed (check_block_models ignores this table for them).
_ARM = "multipart arm: its open inner end sits inside the core model drawn with it"
_VESSEL = "vessel or lantern: the open face is its mouth (contents drawn inside it, or meant to be seen into)"
_DECOR = "decor built from open boxes (left-out faces); to be closed with its own decor art fixes"
_FLORA = "graveyard flora: thin stems, leaves and petals left open; to be closed with the graveyard flora art fixes"
_STONE = "graveyard memorial built from open boxes; to be closed with the graveyard art fixes"
O1_ALLOW = {
    "aluminum_cable_arm": _ARM, "copper_cable_arm": _ARM, "silver_cable_arm": _ARM, "data_cable_arm": _ARM,
    "fluid_filter_arm": _ARM, "bronze_fluid_pipe_arm": _ARM, "brass_item_pipe_arm": _ARM,
    "bobbing_tub": _VESSEL, "bubbling_cauldron": _VESSEL, "candy_kettle": _VESSEL, "cider_press": _VESSEL,
    "cooking_pot": _VESSEL, "luminaria": _VESSEL, "witchs_lantern": _VESSEL,
    "black_pillar_candle": _DECOR, "ivory_pillar_candle": _DECOR, "bowling_pumpkin": _DECOR, "corn_maze": _DECOR,
    "corn_plush": _DECOR, "hedgehog_plush": _DECOR, "owl_plush": _DECOR, "costume_trunk_open": _DECOR,
    "dead_hollow_tree": _DECOR, "flying_eyeball": _DECOR, "fog_machine": _DECOR,
    "judging_stand": _DECOR, "monster_head": _DECOR, "phantom_pipe_organ": _DECOR, "spirit_mirror": _DECOR,
    "asphodel": _FLORA, "black_rose": _FLORA, "bleeding_heart": _FLORA, "deadly_nightshade": _FLORA, "ghost_pipe": _FLORA,
    "snowdrop": _FLORA, "spider_lily": _FLORA, "potted_bleeding_heart": _FLORA, "potted_deadly_nightshade": _FLORA,
    "potted_ghost_pipe": _FLORA, "potted_snowdrop": _FLORA, "potted_spider_lily": _FLORA,
    "angel_at_the_tomb": _STONE, "bone_pile": _STONE, "cemetery_fence_side": _STONE, "cemetery_gate": _STONE,
    "draped_urn": _STONE, "faithful_hound": _STONE, "family_mausoleum": _STONE, "giant_bone_hand": _STONE,
    "mourning_angel": _STONE, "ossuary_wall": _STONE,
}
# H1: quad parts whose open side is only ever drawn against another part, or decor whose own fixes will close it.
H1_ALLOW = {
    "ferris_wheel_lights": "the bulbs leave out the face against the rim; always drawn on ferris_wheel_section",
    "ferris_wheel_section": "rim and spoke members end open against the next section's copy (the renderer draws 16); "
                            "the ferris wheel's own art fix owns ferris_wheel_data.py",
    "jump_scare_spring": "the spring's coil is a stack of open rings; to be closed with its own decor art fix",
    "bowling_pumpkin": "the stem's open end sits on the pumpkin; to be closed with its own decor art fix",
}
# A2: cutout textures whose soft edge is wanted (entityCutout draws it opaque, so keep the list short).
A2_ALLOW = {
    "entity/candle_flame": "AuraCandleRenderer tints its flame; the soft rim reads as glow and the renderer owner keeps it",
}


# ------------------------------------------------------------------ textures

_images = {}


def _image(path):
    """RGBA array of a texture (the first frame of an animated strip), or None."""
    if path not in _images:
        img = None
        if path and path.exists():
            img = Image.open(path).convert("RGBA")
            w, h = img.size
            if Path(str(path) + ".mcmeta").exists() and h > w:
                img = img.crop((0, 0, w, w))
            img = np.asarray(img)
        _images[path] = img
    return _images[path]


def _tex_path(ref):
    """'jugcraft:block/x' (a block model texture) -> its png; None for other namespaces."""
    if isinstance(ref, dict):
        ref = ref.get("sprite", "")
    if not isinstance(ref, str) or not ref.startswith("jugcraft:"):
        return None
    return ASSETS / "textures" / (ref.split(":", 1)[1] + ".png")


def _quad_tex(name):
    return ASSETS / "textures" / ((name if "/" in name else "block/" + name) + ".png")


def _region_alpha(img, uvs):
    """(min alpha, any half-transparent) over the texel box a face's UVs (0..1) cover."""
    h, w = img.shape[:2]
    us = [u for u, _ in uvs]
    vs = [v for _, v in uvs]
    shift_u, shift_v = math.floor(min(us) + 1e-6), math.floor(min(vs) + 1e-6)
    x0 = max(0, int((min(us) - shift_u) * w + 1e-6))
    x1 = min(w, max(x0 + 1, int(math.ceil((max(us) - shift_u) * w - 1e-6))))
    y0 = max(0, int((min(vs) - shift_v) * h + 1e-6))
    y1 = min(h, max(y0 + 1, int(math.ceil((max(vs) - shift_v) * h - 1e-6))))
    alpha = img[y0:y1, x0:x1, 3]
    if alpha.size == 0:
        return 255, False
    return int(alpha.min()), bool(((alpha > 0) & (alpha < 255)).any())


# ------------------------------------------------------------------ plane geometry

def _sub(a, b):
    return (a[0] - b[0], a[1] - b[1], a[2] - b[2])


def _dot(a, b):
    return a[0] * b[0] + a[1] * b[1] + a[2] * b[2]


def _cross(a, b):
    return (a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0])


def _norm(v):
    length = math.sqrt(_dot(v, v))
    return (v[0] / length, v[1] / length, v[2] / length) if length > 1e-12 else (0.0, 0.0, 0.0)


def _basis(n):
    u = _norm(_cross(n, (1, 0, 0) if abs(n[0]) < 0.9 else (0, 1, 0)))
    return u, _cross(n, u)


def _area(poly):
    return sum(poly[i][0] * poly[(i + 1) % len(poly)][1] - poly[(i + 1) % len(poly)][0] * poly[i][1]
               for i in range(len(poly))) / 2


def _ccw(poly):
    return poly if _area(poly) >= 0 else poly[::-1]


def _clip(subject, clipper):
    out = subject
    for i in range(len(clipper)):
        if not out:
            break
        a, b = clipper[i], clipper[(i + 1) % len(clipper)]
        inp, out = out, []

        def inside(p):
            return (b[0] - a[0]) * (p[1] - a[1]) - (b[1] - a[1]) * (p[0] - a[0]) >= -1e-9

        def cut(p, q):
            dx1, dy1, dx2, dy2 = q[0] - p[0], q[1] - p[1], b[0] - a[0], b[1] - a[1]
            den = dx1 * dy2 - dy1 * dx2
            if abs(den) < 1e-15:
                return q
            t = ((a[0] - p[0]) * dy2 - (a[1] - p[1]) * dx2) / den
            return (p[0] + t * dx1, p[1] + t * dy1)
        for j in range(len(inp)):
            p, q = inp[j], inp[(j + 1) % len(inp)]
            if inside(q):
                if not inside(p):
                    out.append(cut(p, q))
                out.append(q)
            elif inside(p):
                out.append(cut(p, q))
    return out


class Face:
    """A planar quad: corners (pixels), UVs (0..1), texture path, outward normal, render kind and flags."""
    __slots__ = ("pts", "uvs", "tex", "normal", "kind", "emit", "label")

    def __init__(self, pts, uvs, tex, normal, kind="solid", emit=0, label=""):
        self.pts, self.uvs, self.tex, self.normal, self.kind, self.emit, self.label = pts, uvs, tex, normal, kind, emit, label

    def uv_at(self, point):
        p0, p1, p3 = self.pts[0], self.pts[1], self.pts[3]
        e1, e2, d = _sub(p1, p0), _sub(p3, p0), _sub(point, p0)
        a11, a12, a22 = _dot(e1, e1), _dot(e1, e2), _dot(e2, e2)
        b1, b2 = _dot(d, e1), _dot(d, e2)
        det = a11 * a22 - a12 * a12
        if abs(det) < 1e-12:
            return self.uvs[0]
        s, t = (b1 * a22 - b2 * a12) / det, (b2 * a11 - b1 * a12) / det
        return (self.uvs[0][0] + s * (self.uvs[1][0] - self.uvs[0][0]) + t * (self.uvs[3][0] - self.uvs[0][0]),
                self.uvs[0][1] + s * (self.uvs[1][1] - self.uvs[0][1]) + t * (self.uvs[3][1] - self.uvs[0][1]))

    def texel(self, point):
        img = _image(self.tex)
        if img is None:
            return None
        u, v = self.uv_at(point)
        h, w = img.shape[:2]
        x = int(math.floor((u - math.floor(u + 1e-9)) * w + 1e-9)) % w
        y = int(math.floor((v - math.floor(v + 1e-9)) * h + 1e-9)) % h
        return img[y, x]


def _differ(a, b, polygon, u, v, n, d, step=0.25, budget=400):
    """Whether two faces draw visibly different texels somewhere in their overlap (a polygon in the plane's basis)."""
    if a.emit != b.emit:
        return True
    points = [tuple(u[k] * x + v[k] * y + n[k] * d for k in range(3)) for x, y in polygon]
    if a.tex == b.tex and all(abs(p - q) < 1e-4 for point in points for p, q in zip(a.uv_at(point), b.uv_at(point))):
        return False  # one texture, one UV map
    xs, ys = [p[0] for p in polygon], [p[1] for p in polygon]
    w, h = max(xs) - min(xs), max(ys) - min(ys)
    nx = max(1, min(int(w / step) + 1, int(math.sqrt(budget * max(w, 1e-6) / max(h, 1e-6))) + 1))
    ny = max(1, min(int(h / step) + 1, max(1, budget // nx)))
    for i in range(nx):
        for j in range(ny):
            x = min(xs) + (i + 0.5) / nx * w
            y = min(ys) + (j + 0.5) / ny * h
            if any((polygon[k][0] - polygon[k - 1][0]) * (y - polygon[k - 1][1]) -
                   (polygon[k][1] - polygon[k - 1][1]) * (x - polygon[k - 1][0]) < -1e-9 for k in range(len(polygon))):
                continue
            point = tuple(u[k] * x + v[k] * y + n[k] * d for k in range(3))
            c1, c2 = a.texel(point), b.texel(point)
            if c1 is None or c2 is None:
                if a.tex != b.tex:
                    return True
                continue
            if c1[3] < 26 and c2[3] < 26:
                continue  # neither is drawn there
            if (c1[3] < 26) != (c2[3] < 26) or max(abs(int(c1[k]) - int(c2[k])) for k in range(3)) > 6:
                return True
    return False


def _pairs(faces, near_gap, opposite=False):
    """Pairs of faces on (nearly) one plane: yields (i, j, gap, overlap polygon, u, v, n, d) for overlapping same-facing
    pairs with 0 <= gap < near_gap, and (with opposite) opposite-facing pairs on one plane with gap None."""
    groups = defaultdict(list)
    for index, f in enumerate(faces):
        key = tuple(round(c, 3) + 0.0 for c in f.normal)
        groups[key].append((_dot(f.normal, f.pts[0]), index))
    for key, items in groups.items():
        items.sort()
        n = _norm(key)
        u, v = _basis(n)
        polys = {}

        def poly(i):
            if i not in polys:
                polys[i] = _ccw([(_dot(p, u), _dot(p, v)) for p in faces[i].pts])
            return polys[i]
        for a in range(len(items)):
            d1, i = items[a]
            for b in range(a + 1, len(items)):
                d2, j = items[b]
                gap = d2 - d1
                if gap >= near_gap:
                    break
                overlap = _clip(poly(i), poly(j))
                if len(overlap) < 3 or abs(_area(overlap)) < 1e-3:
                    continue
                yield i, j, (0.0 if gap <= PLANE_EPS else gap), overlap, u, v, n, (d1 + d2) / 2
        if opposite:
            other = tuple(-c + 0.0 for c in key)
            if other in groups and key < other:
                for d1, i in items:
                    for d2, j in groups[other]:
                        if abs(d1 + d2) > PLANE_EPS:
                            continue
                        overlap = _clip(poly(i), _ccw([(_dot(p, u), _dot(p, v)) for p in faces[j].pts]))
                        if len(overlap) < 3 or abs(_area(overlap)) < 1e-3:
                            continue
                        yield i, j, None, overlap, u, v, n, d1


# ------------------------------------------------------------------ block models

_NORMAL = {"north": (0, 0, -1), "south": (0, 0, 1), "east": (1, 0, 0), "west": (-1, 0, 0), "up": (0, 1, 0), "down": (0, -1, 0)}
_AXIS = {"north": 2, "south": 2, "east": 0, "west": 0, "up": 1, "down": 1}


def _auto_uv(face, f, t):
    return {"north": (16 - t[0], 16 - t[1], 16 - f[0], 16 - f[1]), "south": (f[0], 16 - t[1], t[0], 16 - f[1]),
            "east": (16 - t[2], 16 - t[1], 16 - f[2], 16 - f[1]), "west": (f[2], 16 - t[1], t[2], 16 - f[1]),
            "up": (f[0], f[2], t[0], t[2]), "down": (f[0], 16 - t[2], t[0], 16 - f[2])}[face]


def _face_point(face, f, t, a, b):
    x0, y0, z0 = f
    x1, y1, z1 = t
    return {"north": (x1 - a * (x1 - x0), y1 - b * (y1 - y0), z0), "south": (x0 + a * (x1 - x0), y1 - b * (y1 - y0), z1),
            "east": (x1, y1 - b * (y1 - y0), z1 - a * (z1 - z0)), "west": (x0, y1 - b * (y1 - y0), z0 + a * (z1 - z0)),
            "up": (x0 + a * (x1 - x0), y1, z0 + b * (z1 - z0)), "down": (x0 + a * (x1 - x0), y0, z1 - b * (z1 - z0))}[face]


def _turner(rotation):
    """(point, normal) functions for a block model element rotation (right-handed, rescale stretching across the axis)."""
    if not rotation:
        return (lambda p: tuple(p)), (lambda n: tuple(n))
    ox, oy, oz = rotation.get("origin", (8, 8, 8))
    a = math.radians(rotation["angle"])
    c, s = math.cos(a), math.sin(a)
    axis = rotation["axis"]

    def turn(x, y, z):
        if axis == "y":
            return (x * c + z * s, y, -x * s + z * c)
        if axis == "x":
            return (x, y * c - z * s, y * s + z * c)
        return (x * c - y * s, x * s + y * c, z)
    k = 1 / c if rotation.get("rescale") and abs(c) > 1e-9 else 1.0

    def point(p):
        x, y, z = turn(p[0] - ox, p[1] - oy, p[2] - oz)
        if axis != "x":
            x *= k
        if axis != "y":
            y *= k
        if axis != "z":
            z *= k
        return (x + ox, y + oy, z + oz)
    return point, (lambda n: turn(*n))


def _uv_turn(a, b, r):
    return {90: (b, 1 - a), 180: (1 - a, 1 - b), 270: (1 - b, a)}.get(int(r or 0) % 360, (a, b))


def _model_faces(elements, lookup):
    """Faces of a model's elements, with the raw UVs (pixels) of each, and (element index, face) labels."""
    faces, raw = [], []
    for index, el in enumerate(elements):
        f, t = el["from"], el["to"]
        point, turn_normal = _turner(el.get("rotation"))
        for face, spec in el.get("faces", {}).items():
            uv = spec.get("uv") or _auto_uv(face, f, t)
            u0, v0, u1, v1 = uv
            pts, uvs = [], []
            # Corners counter-clockwise seen from outside (the UV order reversed), so the winding agrees with the normal.
            for a, b in ((0, 1), (1, 1), (1, 0), (0, 0)):
                pts.append(point(_face_point(face, f, t, a, b)))
                ra, rb = _uv_turn(a, b, spec.get("rotation"))
                uvs.append(((u0 + ra * (u1 - u0)) / 16, (v0 + rb * (v1 - v0)) / 16))
            faces.append(Face(pts, uvs, lookup(spec.get("texture", "")), _norm(turn_normal(_NORMAL[face])),
                              emit=el.get("light_emission", 0), label=(index, face)))
            raw.append(uv)
    return faces, raw


def _resolve(model, roots):
    chain = [model]
    while chain[-1].get("parent", "").startswith("jugcraft:") and len(chain) < 20:
        ref = chain[-1]["parent"].split(":", 1)[1]
        parent = next((json.loads(p.read_text(encoding="utf-8")) for p in (r / "models" / f"{ref}.json" for r in roots)
                       if p.exists()), None)
        if parent is None:
            break
        chain.append(parent)
    textures, elements = {}, None
    for m in reversed(chain):
        textures.update(m.get("textures", {}))
        if "elements" in m:
            elements = m["elements"]

    def lookup(ref):
        for _ in range(10):
            if not (isinstance(ref, str) and ref.startswith("#")):
                break
            ref = textures.get(ref[1:], "")
        return _tex_path(ref)
    return elements, lookup, textures


def _opaque(face_list):
    for f in face_list:
        img = _image(f.tex)
        if img is None:
            return False
        low, _ = _region_alpha(img, f.uvs)
        if low < 255:
            return False
    return True


def _open_faces(elements, faces):
    """O1: (element index, face) of unrotated opaque boxes whose left-out face is exposed (not on a 0/16 plane)."""
    by_element = defaultdict(list)
    for f in faces:
        by_element[f.label[0]].append(f)
    boxes = [(i, el["from"], el["to"]) for i, el in enumerate(elements) if not el.get("rotation")]
    out = []
    for i, f, t in boxes:
        present = set(elements[i].get("faces", {}))
        if not present or len(present) == 6:
            continue
        if any(t[k] - f[k] <= 1e-6 for k in range(3)):
            continue  # a plane, not a box
        if not _opaque(by_element[i]):
            continue  # see-through boxes may leave faces out on purpose
        for face in _NORMAL:
            if face in present:
                continue
            axis = _AXIS[face]
            sign = sum(_NORMAL[face])
            plane = t[axis] if sign > 0 else f[axis]
            if abs(plane) < 1e-6 or abs(plane - 16) < 1e-6 or (plane < 0 if sign < 0 else plane > 16):
                continue  # a seam of a model cut into blocks, or a face out against the block below or beside
            others = [k for k in range(3) if k != axis]
            free = 0
            for a in range(4):
                for b in range(4):
                    p = [0.0, 0.0, 0.0]
                    p[axis] = plane + sign * 0.01
                    p[others[0]] = f[others[0]] + (a + 0.5) / 4 * (t[others[0]] - f[others[0]])
                    p[others[1]] = f[others[1]] + (b + 0.5) / 4 * (t[others[1]] - f[others[1]])
                    if not any(all(F[k] - 1e-6 <= p[k] <= T[k] + 1e-6 for k in range(3)) for j, F, T in boxes if j != i):
                        free += 1
            if free:
                out.append((i, face))
    return out


def _family(stem):
    """A model's family name: without part numbers, lit/active states and wear stages."""
    stem = re.sub(r"_part\d+", "", stem)
    stem = re.sub(r"_(on|active|turning|lit|lower|upper|lower_lit|upper_lit)$", "", stem)
    return stem


def _allowed(table, stem):
    """The allow-list key covering a model, or None."""
    family = _family(stem)
    return next((name for name in table if family == name or family.startswith(name + "_")), None)


def check_block_models(machines):
    errors, allowed = [], defaultdict(set)
    seen = set()
    jobs = [(p, (ASSETS,)) for p in sorted((ASSETS / "models").rglob("*.json"))]
    jobs += [(p, (PACK, ASSETS)) for p in sorted((PACK / "models").rglob("*.json"))]
    count = 0
    for path, roots in jobs:
        model = json.loads(path.read_text(encoding="utf-8"))
        elements, lookup, textures = _resolve(model, roots)
        if not elements:
            continue
        key = json.dumps([elements, {k: textures[k] for k in sorted(textures)}], sort_keys=True)
        if key in seen:
            continue
        seen.add(key)
        count += 1
        name = str(path.relative_to(ROOT / "src" / "main" / "resources"))
        faces, raw = _model_faces(elements, lookup)
        u1 = [f"{f.label[1]} face of element {f.label[0]} reads uv {list(uv)}" for f, uv in zip(faces, raw)
              if min(uv) < -1e-3 or max(uv) > 16 + 1e-3]
        if u1:
            errors.append(f"U1 {name}: {len(u1)} face(s) read outside their sprite (0..16), e.g. {u1[0]}: give it a uv inside "
                          "the sprite (model_writer.fit_uvs does)")
        z1, n1 = [], []
        for i, j, gap, overlap, u, v, n, d in _pairs(faces, N1_GAP):
            if _differ(faces[i], faces[j], overlap, u, v, n, d):
                (z1 if gap == 0 else n1).append((faces[i], faces[j], gap, abs(_area(overlap))))
        for rule, found, what in (("Z1", z1, "share a plane"), ("N1", n1, f"lie closer than {N1_GAP} px")):
            if found:
                a, b, gap, area = found[0]
                errors.append(f"{rule} {name}: {len(found)} pair(s) of differently drawn faces {what} (they flicker), e.g. "
                              f"element {a.label[0]} {a.label[1]} / element {b.label[0]} {b.label[1]}, {area:.2f} px2"
                              f"{f', gap {gap:.3f}' if gap else ''}: model_writer.separate_coplanar pulls them apart")
        open_faces = _open_faces(elements, faces)
        if open_faces:
            stem = path.stem
            key = None if _family(stem) in machines else _allowed(O1_ALLOW, stem)
            if key:
                allowed["O1"].add(key)
            else:
                i, face = open_faces[0]
                errors.append(f"O1 {name}: {len(open_faces)} left-out face(s) of opaque boxes are exposed (holes), e.g. "
                              f"the {face} face of element {i} {elements[i]['from']}..{elements[i]['to']}")
    return errors, allowed, count


# ------------------------------------------------------------------ quad parts

def _quad_kind(q):
    return "translucent" if q.get("nocull") else "cutout" if q.get("cutout") else "solid"


def _part_faces(raw):
    faces = []
    for q in raw:
        pts = [tuple(v[:3]) for v in q["vertices"]]
        n = _norm(_cross(_sub(pts[2], pts[0]), _sub(pts[3], pts[1])))
        if n == (0.0, 0.0, 0.0):
            continue
        faces.append(Face(pts, [tuple(v[3:5]) for v in q["vertices"]], _quad_tex(q["texture"]), n, _quad_kind(q),
                          label=q["texture"]))
    return faces


class _Shells:
    """How many closed shells of a part contain a point (signed ray crossings, majority of three rays): a coplanar pair
    inside other geometry can never be seen."""
    RAYS = [_norm((0.5377, 0.8191, 0.2007)), _norm((-0.3213, 0.2711, -0.9073)), _norm((0.7741, -0.5134, 0.3701))]

    def __init__(self, faces):
        tris, normals = [], []
        for f in faces:
            for tri in ((0, 1, 2), (0, 2, 3)):
                tris.append([f.pts[k] for k in tri])
                normals.append(f.normal)
        self.t = np.array(tris, dtype=np.float64).reshape(-1, 3, 3)
        self.n = np.array(normals, dtype=np.float64).reshape(-1, 3)

    def depth(self, point):
        if len(self.t) == 0:
            return 0
        p = np.array(point, dtype=np.float64)
        v0 = self.t[:, 0]
        e1, e2 = self.t[:, 1] - v0, self.t[:, 2] - v0
        votes = []
        for d in self.RAYS:
            d = np.array(d)
            h = np.cross(d, e2)
            a = np.einsum("ij,ij->i", e1, h)
            ok = np.abs(a) > 1e-12
            f = np.zeros_like(a)
            f[ok] = 1.0 / a[ok]
            s = p - v0
            u = f * np.einsum("ij,ij->i", s, h)
            q = np.cross(s, e1)
            v = f * (q @ d)
            t = f * np.einsum("ij,ij->i", e2, q)
            hit = ok & (u >= 0) & (v >= 0) & (u + v <= 1) & (t > 1e-6)
            votes.append(int(np.sign(self.n[hit] @ d).sum()))
        votes.sort()
        return votes[1]


def _buried(shells, overlap, u, v, n, d, sides):
    cx = sum(p[0] for p in overlap) / len(overlap)
    cy = sum(p[1] for p in overlap) / len(overlap)
    centre = [u[k] * cx + v[k] * cy + n[k] * d for k in range(3)]
    return all(shells.depth([centre[k] + n[k] * side * 0.01 for k in range(3)]) > 0 for side in sides)


_VIEWS = [(yaw, pitch) for pitch in (25, -20, 60) for yaw in (20, 110, 200, 290)]


def _see_through(faces, scale=H1_SCALE):
    """(back-face pixels, silhouette pixels) over 12 views, rasterized without culling: pixels whose nearest surface is
    the back of a culled (solid) quad are holes the game shows the world through."""
    back_total = sil_total = 0
    pts = np.array([f.pts for f in faces], dtype=np.float64)
    solid = np.array([f.kind == "solid" for f in faces])
    for yaw, pitch in _VIEWS:
        cy, sy = math.cos(math.radians(yaw)), math.sin(math.radians(yaw))
        cp, sp = math.cos(math.radians(pitch)), math.sin(math.radians(pitch))
        x = pts[..., 0] * cy - pts[..., 2] * sy
        z = pts[..., 0] * sy + pts[..., 2] * cy
        y = pts[..., 1] * cp - z * sp
        z = pts[..., 1] * sp + z * cp
        minx, maxy = x.min(), y.max()
        W = int((x.max() - minx) * scale) + 4
        H = int((maxy - y.min()) * scale) + 4
        depth = np.full((H, W), -1e9)
        back = np.zeros((H, W), dtype=bool)
        sx = (x - minx) * scale + 2
        sy_ = (maxy - y) * scale + 2
        for qi in range(len(faces)):
            vx, vy, vz = sx[qi], sy_[qi], z[qi]
            # Facing: the view-space normal's z (towards the viewer is +z).
            e1 = (x[qi, 2] - x[qi, 0], y[qi, 2] - y[qi, 0])
            e2 = (x[qi, 3] - x[qi, 1], y[qi, 3] - y[qi, 1])
            facing = e1[0] * e2[1] - e1[1] * e2[0]
            is_back = bool(solid[qi]) and facing < 0
            for tri in ((0, 1, 2), (0, 2, 3)):
                ax, ay = vx[tri[0]], vy[tri[0]]
                bx, by = vx[tri[1]], vy[tri[1]]
                cx, cy2 = vx[tri[2]], vy[tri[2]]
                area = (bx - ax) * (cy2 - ay) - (by - ay) * (cx - ax)
                if abs(area) < 1e-9:
                    continue
                x0, x1 = max(0, int(min(ax, bx, cx))), min(W - 1, int(max(ax, bx, cx)) + 1)
                y0, y1 = max(0, int(min(ay, by, cy2))), min(H - 1, int(max(ay, by, cy2)) + 1)
                if x0 > x1 or y0 > y1:
                    continue
                gx, gy = np.meshgrid(np.arange(x0, x1 + 1) + 0.5, np.arange(y0, y1 + 1) + 0.5)
                w0 = ((bx - gx) * (cy2 - gy) - (by - gy) * (cx - gx)) / area
                w1 = ((cx - gx) * (ay - gy) - (cy2 - gy) * (ax - gx)) / area
                w2 = 1 - w0 - w1
                inside = (w0 >= -1e-6) & (w1 >= -1e-6) & (w2 >= -1e-6)
                zz = w0 * vz[tri[0]] + w1 * vz[tri[1]] + w2 * vz[tri[2]]
                sub = depth[y0:y1 + 1, x0:x1 + 1]
                # Front faces win ties, so a front/back pair on one plane is not counted as a hole.
                closer = inside & ((zz > sub + 1e-4) | ((np.abs(zz - sub) <= 1e-4) & (not is_back)))
                sub[closer] = zz[closer]
                back[y0:y1 + 1, x0:x1 + 1][closer] = is_back
        sil = depth > -1e8
        back_total += int((back & sil).sum())
        sil_total += int(sil.sum())
    return back_total, sil_total


def _quad_files():
    files = sorted(ASSETS.glob("*_quads.json")) + [ASSETS / "kinetic_rotors.json", ASSETS / "worn_models.json"]
    for path in files:
        if not path.exists():
            continue
        data = json.loads(path.read_text(encoding="utf-8"))
        for name, raw in data.items():
            raw = raw.get("quads") if isinstance(raw, dict) else raw
            if isinstance(raw, list) and raw:
                yield path.name, name, raw


def check_quad_parts():
    errors, allowed = [], defaultdict(set)
    count = 0
    for file, name, raw in _quad_files():
        count += 1
        where = f"{file}:{name}"
        faces = _part_faces(raw)
        shells = None
        z2, b2b = [], []
        for i, j, gap, overlap, u, v, n, d in _pairs(faces, PLANE_EPS * 2, opposite=True):
            a, b = faces[i], faces[j]
            if gap is None:
                if a.kind == "solid" and b.kind == "solid":
                    continue
                shells = shells or _Shells(faces)
                if not _buried(shells, overlap, u, v, n, d, (1, -1)):
                    b2b.append((a, b))
                continue
            if not _differ(a, b, overlap, u, v, n, d):
                continue
            shells = shells or _Shells(faces)
            if not _buried(shells, overlap, u, v, n, d, (1,)):
                z2.append((a, b))
        if z2:
            errors.append(f"Z2 {where}: {len(z2)} pair(s) of differently drawn quads share a plane (they flicker), e.g. "
                          f"{z2[0][0].label} / {z2[0][1].label}: separate the boxes before exporting (model_writer.separate_boxes)")
        if b2b:
            errors.append(f"Z2 {where}: {len(b2b)} back-to-back pair(s) on one plane under a type that does not cull, e.g. "
                          f"{b2b[0][0].label} / {b2b[0][1].label}: lift each side off the plane (TWO_SIDED_LIFT)")
        back, silhouette = _see_through(faces)
        if back > H1_LIMIT * silhouette:
            if name in H1_ALLOW:
                allowed["H1"].add(name)
            else:
                errors.append(f"H1 {where}: {100 * back / silhouette:.1f}% of its silhouette shows the backs of culled quads "
                              "(see-through holes): close the model (every exposed face drawn)")
        for f in faces:
            img = _image(f.tex)
            if img is None:
                continue
            low, partial = _region_alpha(img, f.uvs)
            if f.kind == "solid" and low < 255:
                errors.append(f"A1 {where}: an entitySolid quad samples see-through texels of {f.label} (solid ignores alpha): "
                              "mark it cutout or make the texels opaque")
                break
            if f.kind == "cutout" and partial:
                key = str(f.tex.relative_to(ASSETS / "textures"))[:-4]
                if key in A2_ALLOW:
                    allowed["A2"].add(key)
                    continue
                errors.append(f"A2 {where}: an entityCutout quad samples half-transparent texels of {f.label} (drawn opaque)")
                break
    return errors, allowed, count


def check_java_render_types():
    """A1/A2 for textures a Java renderer binds with RenderTypes.entitySolid / entityCutout."""
    errors, allowed = [], defaultdict(set)
    pattern = re.compile(r'RenderTypes\.(entitySolid|entityCutout)\(Jugcraft\.id\("textures/([^"]+)\.png"\)')
    for path in sorted(CLIENT.rglob("*.java")):
        for kind, texture in pattern.findall(path.read_text(encoding="utf-8")):
            img = _image(ASSETS / "textures" / f"{texture}.png")
            if img is None:
                continue
            alpha = img[..., 3]
            if kind == "entitySolid" and alpha.min() < 255:
                errors.append(f"A1 {path.name}: entitySolid draws {texture}, which has see-through texels")
            if kind == "entityCutout" and ((alpha > 0) & (alpha < 255)).any():
                if texture in A2_ALLOW:
                    allowed["A2"].add(texture)
                else:
                    errors.append(f"A2 {path.name}: entityCutout draws {texture}, which has half-transparent texels (drawn opaque)")
    return errors, allowed


def run(machines=()):
    """All rules: (errors, summary line)."""
    start = time.time()
    machines = set(machines)
    errors, allowed = [], defaultdict(set)
    found, used, models = check_block_models(machines)
    errors += found
    middle = time.time()
    quad_errors, quad_allowed, parts = check_quad_parts()
    java_errors, java_allowed = check_java_render_types()
    errors += quad_errors + java_errors
    for table in (used, quad_allowed, java_allowed):
        for rule, names in table.items():
            allowed[rule] |= names
    stale = [f"{rule} allow-list entry {name} is no longer needed: remove it"
             for rule, table in (("H1", H1_ALLOW), ("A2", A2_ALLOW), ("O1", O1_ALLOW))
             for name in table if name not in allowed[rule]]
    errors += stale
    listed = ", ".join(f"{rule} {len(names)}" for rule, names in sorted(allowed.items())) or "none"
    summary = (f"art check: {models} block/item models ({middle - start:.1f} s) and {parts} quad parts "
               f"({time.time() - middle:.1f} s); allow-listed: {listed}")
    return errors, summary


if __name__ == "__main__":
    sys.path.insert(0, str(Path(__file__).resolve().parent))
    from machines import machine_blocks
    problems, line = run(machine_blocks())
    for problem in problems:
        print(problem)
    print(line)
    sys.exit(1 if problems else 0)
