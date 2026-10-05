"""Checks the big guns' moving parts against their fixed ones over their whole travel (tools/check_mod_data.py runs it).

On 5 October 2026 the owner found the big guns' barrels "see through" and their textures "conflicting". Part of that was
the barrels meeting their turntables and hulls: a face of the barrel sliding in the same plane as a face of the deck or
housing it swings past flickers, and a barrel swung through a roof or an engine cuts it. A per-part check cannot see
either, so this one poses the exported quads as client/TowerGunRenderer and client/ArtilleryRenderers do, at every few
degrees of elevation (and, for the howitzer, of traverse within its arc), and reports:
- any face of a moving part that lies in the same plane as an overlapping face of a fixed part, facing the same way;
- a moving part sunk into a fixed part's boxes by more than LIMIT of its volume (the Triple Battery's housing, which is
  meant to sit inside its drum, is exempt).
"""
import math

import numpy as np

from model_writer import unpack

LIMIT = 0.005
EXEMPT = {("triple_battery_barrel", "triple_battery_turntable")}


def _rx(deg):
    a = math.radians(deg)
    c, s = math.cos(a), math.sin(a)
    return np.array([[1, 0, 0], [0, c, -s], [0, s, c]])


def _ry(deg):
    a = math.radians(deg)
    c, s = math.cos(a), math.sin(a)
    return np.array([[c, 0, s], [0, 1, 0], [-s, 0, c]])


def _poses(artillery, tower_guns):
    """(gun, [(part, fixed?)], pose(pitch, yaw) -> {part: (matrix, offset)}, pitches, yaws) for every gun."""
    out = []
    for gun, g in tower_guns.GUNS.items():
        table, trunnion = np.array(g["turntable"], float), np.array(g["trunnion"], float)

        def pose(pitch, yaw, gun=gun, table=table, trunnion=trunnion):
            m = _ry(-yaw)
            return {f"{gun}_base": (np.eye(3), np.zeros(3)), f"{gun}_turntable": (m, table),
                    f"{gun}_barrel": (m @ _rx(-pitch), table + m @ (trunnion - table))}
        out.append((gun, [f"{gun}_base", f"{gun}_turntable", f"{gun}_barrel"], pose, g["pitch"], (0.0, 37.0)))
    table, trunnion = np.array(artillery.MORTAR_TURNTABLE, float), np.array(artillery.MORTAR_TRUNNION, float)
    out.append(("siege_mortar", ["mortar_base", "mortar_turntable", "mortar_barrel"],
                lambda p, y: {"mortar_base": (np.eye(3), np.zeros(3)), "mortar_turntable": (_ry(-y), table),
                              "mortar_barrel": (_ry(-y) @ _rx(-p), table + _ry(-y) @ (trunnion - table))}, (45, 85), (0.0, 37.0)))
    gun = np.array(artillery.HOWITZER_GUN, float)
    arc = artillery.HOWITZER_ARC
    out.append(("self_propelled_howitzer", ["howitzer_body", "howitzer_gun"],
                lambda p, y: {"howitzer_body": (np.eye(3), np.zeros(3)), "howitzer_gun": (_ry(-y) @ _rx(-p), gun)},
                (-5, 70), (-arc, -arc / 2, 0.0, arc / 2, arc)))
    head = np.array(artillery.FLAK_HEAD, float)
    out.append(("flak_gun", ["flak_mount", "flak_head"],
                lambda p, y: {"flak_mount": (np.eye(3), np.zeros(3)), "flak_head": (_ry(-y) @ _rx(-p), head)},
                (-5, 85), (0.0, 30.0, 45.0, 60.0)))
    return out


def _planes(quads, m, t):
    """Each quad posed: (unit normal, plane offset, its corners in the plane's 2D frame)."""
    out = []
    for q in quads:
        v = np.array([p[:3] for p in q["vertices"]], float) @ m.T + t
        n = m @ np.array(q["normal"], float)
        n /= np.linalg.norm(n)
        out.append((n, float(n @ v[0]), v))
    return out


def _overlap(n, a, b):
    """Overlap area of two coplanar convex quads, by clipping a against b in their plane."""
    h = np.array([1.0, 0, 0]) if abs(n[0]) < 0.9 else np.array([0, 1.0, 0])
    e1 = np.cross(n, h)
    e1 /= np.linalg.norm(e1)
    e2 = np.cross(n, e1)
    pa, pb = [tuple(p) for p in a @ np.array([e1, e2]).T], [tuple(p) for p in b @ np.array([e1, e2]).T]

    def area(poly):
        return 0.5 * sum(poly[i][0] * poly[i - 1][1] - poly[i - 1][0] * poly[i][1] for i in range(len(poly)))
    if area(pa) > 0:
        pa = pa[::-1]
    if area(pb) > 0:
        pb = pb[::-1]
    poly = pa
    for i in range(len(pb)):
        a0, a1 = pb[i - 1], pb[i]
        clipped = []
        for j in range(len(poly)):
            p, q = poly[j - 1], poly[j]
            side_p = (a1[0] - a0[0]) * (p[1] - a0[1]) - (a1[1] - a0[1]) * (p[0] - a0[0])
            side_q = (a1[0] - a0[0]) * (q[1] - a0[1]) - (a1[1] - a0[1]) * (q[0] - a0[0])
            if side_q <= 1e-9:
                if side_p > 1e-9:
                    clipped.append(_cut(p, q, side_p, side_q))
                clipped.append(q)
            elif side_p <= 1e-9:
                clipped.append(_cut(p, q, side_p, side_q))
        poly = clipped
        if len(poly) < 3:
            return 0.0
    return abs(area(poly))


def _cut(p, q, sp, sq):
    t = sp / (sp - sq)
    return (p[0] + (q[0] - p[0]) * t, p[1] + (q[1] - p[1]) * t)


def _coplanar(moving, fixed):
    index = {}
    for n, d, v in fixed:
        index.setdefault((round(n[0], 2), round(n[1], 2), round(n[2], 2), round(d * 20)), []).append((n, d, v))
    area = 0.0
    for n, d, v in moving:
        key = (round(n[0], 2), round(n[1], 2), round(n[2], 2))
        for k in (round(d * 20) - 1, round(d * 20), round(d * 20) + 1):
            for n2, d2, v2 in index.get(key + (k,), []):
                if abs(d2 - d) < 1e-3 and np.linalg.norm(n2 - n) < 1e-3:
                    area += _overlap(n, v, v2)
    return area


def _samples(elements):
    pts = []
    for e in elements:
        frm, to, _, options = unpack(e)
        if options.get("rotation"):
            continue
        f, t = np.array(frm, float), np.array(to, float)
        steps = np.maximum(1, np.ceil(t - f).astype(int))
        axes = [f[k] + (np.arange(steps[k]) + 0.5) * (t[k] - f[k]) / steps[k] for k in range(3)]
        pts.append(np.stack(np.meshgrid(*axes, indexing="ij"), -1).reshape(-1, 3))
    return np.concatenate(pts)


def _boxes(elements):
    lo, hi = [], []
    for e in elements:
        frm, to, _, options = unpack(e)
        if not options.get("rotation"):
            lo.append(frm)
            hi.append(to)
    return np.array(lo, float), np.array(hi, float)


def problems(artillery, tower_guns, quads, step=5):
    """Every coplanar or sunk pose, as text; `quads` maps part names to their exported quads."""
    sources = {}
    for gun, fns in tower_guns.PARTS.items():
        for kind, fn in zip(("base", "turntable", "barrel"), fns):
            sources[f"{gun}_{kind}"] = fn()
    for name in ("mortar_base", "mortar_turntable", "mortar_barrel", "howitzer_body", "howitzer_gun", "flak_mount", "flak_head"):
        sources[name] = getattr(artillery, name)()
    out = []
    cache = {}
    for gun, parts, pose, (low, high), yaws in _poses(artillery, tower_guns):
        moving = parts[-1]
        samples = _samples(sources[moving])
        pitches = sorted(set(list(range(int(low), int(high) + 1, step)) + [low, high]))
        for yaw in yaws:
            for pitch in pitches:
                placed = pose(pitch, yaw)
                posed = {}
                for part in parts:
                    key = (part, yaw, pitch if part == moving else None)
                    if key not in cache:
                        cache[key] = _planes(quads[part], *placed[part])
                    posed[part] = cache[key]
                pairs = [(moving, fixed) for fixed in parts[:-1]] + ([(parts[1], parts[0])] if len(parts) == 3 else [])
                for a, b in pairs:
                    area = _coplanar(posed[a], posed[b])
                    if area > 0.01:
                        out.append(f"{gun}: at elevation {pitch} and traverse {yaw}, {a} shares a plane with {b} over "
                                   f"{area:.2f} px^2 (it flickers)")
                m, t = placed[moving]
                world = samples @ m.T + t
                for fixed in parts[:-1]:
                    if (moving, fixed) in EXEMPT:
                        continue
                    fm, ft = placed[fixed]
                    local = (world - ft) @ fm
                    lo, hi = _boxes(sources[fixed])
                    inside = np.zeros(len(local), bool)
                    for a, b in zip(lo, hi):
                        inside |= np.all((local > a + 0.01) & (local < b - 0.01), axis=1)
                    if inside.mean() > LIMIT:
                        out.append(f"{gun}: at elevation {pitch} and traverse {yaw}, {inside.mean():.1%} of {moving} is "
                                   f"inside {fixed}")
    return out
