"""The garden cohort's spirit bodies (Concordance garden cohort, docs/features/concordance-garden-cohort.md, Art plan; GC-04
first). Each is adapted from the owner library's own files by pure functions of their bytes (no I/O, no randomness: the
same library files always give the same runtime files), and written and checked by tools/owner_art.py through
tools/concordance_pacts.py SPIRITS[<spirit>]["body"]["adapted"], whose adapter names are "garden_bodies.<function>".
Nothing in the library is executed or changed.

Buer (GC-04, GC-S12; docs/features/buer-pact.md): the owner's Decor Blossom (art/owner-library/originals/Magic/
assets/ars_jimbaux/: geo/decor_blossom.geo.json, animations/decor_blossom_animations.json, textures/block/decor_blossom.png).
Its texture is copied byte for byte (SPIRITS["buer"]["body"]["owner_files"]); made here:
- buer_model(read): the geometry with two recorded technical changes and nothing else. The stem is lengthened: the base
  bone's stem cube is repeated stem_copies times upward, and every other bone's pivot and every cube on it (its own pivot
  too) is raised by that height, so the flower rises on its stem and every clip still turns each bone about its own pivot.
  And of each zero-thickness cube drawing two opposite faces on one plane, the face whose texels are all clear is dropped
  (GB24; docs/ART_DIRECTION.md, "Two-sided planes": entityCutout does not cull in 26.3, so the one quad left is seen from
  both sides): the leaf plate's and the pistil cap's down faces and the pistil blades' north faces. A pair both drawn or
  both clear stops the generator: which to keep is the owner's choice, never this tool's.
- buer_glowmask(read): the pistil cap's brightest texels alone (its up face's texels whose red is full), as the skin draws
  them, for GeckoLib's AutoGlowingGeoLayer.
- buer_animations(read): the owner's open, close and active clips unchanged (active is his idle loop), then his own loops
  and gestures, keyed on the rig's bones base, petal1 to petal4 and pistil (the petalNa bones stay unkeyed, as the owner's
  clips leave them). Rotations in degrees, as the owner's clips give them: a petal at k times its own open pose is shut at
  0, open at 1, flared past open above 1; +x pitches the pistil's top toward his front.

Bathin (GC-05, GC-S12; docs/features/bathin-pact.md): built here, not adapted, since no rig of the owner's fits a strong
man on a serpent's coil (the Siren, the library's only unused long-tailed upper body, is a mermaid with a girl's face,
kept for Ember's mermaid skins). bathin_model and bathin_animations read nothing; bathin_skin and bathin_glowmask paint
only in the colours of two of the owner's paintings under art/owner-library/originals/Blocks/ (the willow log's bark and
the Spanish moss), whose `read(path)` is relative to that folder (tools/concordance_pacts.py SPIRITS["bathin"]["body"]
["built"], written and checked by tools/owner_art.py). Shared rules for built bodies: box_faces (GeckoLib's box-UV layout),
palette (a painting's colours), coplanar (docs/ART_DIRECTION.md's "no shared face planes").

For Buer, `read(path)` returns a library file's bytes, `path` relative to art/owner-library/originals/Magic/.
"""
import copy
import io
import json

from PIL import Image

import concordance_pacts as pacts
from stolas_body import _final, _keys, _wave  # the same keyframe helpers (pure)

BLOSSOM = pacts.BLOSSOM
BUER_GEO = BLOSSOM + "geo/decor_blossom.geo.json"
BUER_TEXTURE = BLOSSOM + "textures/block/decor_blossom.png"
BUER_ANIMATIONS = BLOSSOM + "animations/decor_blossom_animations.json"
_BUER = pacts.SPIRITS["buer"]["body"]
# The two faces a zero-thickness cube draws on one plane, by the axis it is flat across (Bedrock's face names).
OPPOSITE = {0: ("east", "west"), 1: ("up", "down"), 2: ("north", "south")}
PETALS = ("petal1", "petal2", "petal3", "petal4")
EASE = "easeInOutSine"


# ------------------------------------------------------------------------------------------------- shared rules

def flat_axis(cube):
    """The axis (0 x, 1 y, 2 z) a cube has no thickness across, or None."""
    for axis, size in enumerate(cube["size"]):
        if size == 0:
            return axis
    return None


def face_texels(face):
    """The texels a per-face UV face draws: (x, y) inside its uv rectangle (a negative size reads backwards)."""
    (u, v), (w, h) = face["uv"], face["uv_size"]
    return [(x, y) for y in range(min(v, v + h), max(v, v + h)) for x in range(min(u, u + w), max(u, u + w))]


def clear(sheet, face):
    """Whether every texel a face draws is clear (alpha 0), or it draws none inside the sheet."""
    return all(not (0 <= x < sheet.width and 0 <= y < sheet.height) or sheet.getpixel((x, y))[3] == 0
               for x, y in face_texels(face))


def drop_clear_twins(geometry, sheet):
    """Of each zero-thickness per-face cube drawing both faces of its flat axis, drops the clear one, in place; returns
    {(bone, cube index): the face dropped}."""
    dropped = {}
    for bone in geometry["minecraft:geometry"][0]["bones"]:
        for index, cube in enumerate(bone.get("cubes", [])):
            axis, faces = flat_axis(cube), cube.get("uv")
            if axis is None or not isinstance(faces, dict):
                continue
            a, b = OPPOSITE[axis]
            if a not in faces or b not in faces:
                continue
            clear_a, clear_b = clear(sheet, faces[a]), clear(sheet, faces[b])
            if clear_a == clear_b:
                raise SystemExit(f"tools/garden_bodies.py: {bone['name']} cube {index} draws {a} and {b} on one plane, "
                                 f"{'both clear' if clear_a else 'both drawn'}: the owner decides which to keep")
            gone = a if clear_a else b
            del faces[gone]
            dropped[(bone["name"], index)] = gone
    return dropped


# ------------------------------------------------------------------------------------------------- Buer

def _up(value, rise):
    return round(value + rise, 4)


def buer_geometry(read):
    """Buer's geometry, and the faces dropped from it ({(bone, cube index): face}; check_spirit_bodies pins them)."""
    out = json.loads(read(BUER_GEO))
    sheet = Image.open(io.BytesIO(read(BUER_TEXTURE))).convert("RGBA")
    bones = out["minecraft:geometry"][0]["bones"]
    base = next(bone for bone in bones if bone["name"] == "base")
    stem = base["cubes"][0]
    height = stem["size"][1]
    rise = round(height * _BUER["stem_copies"], 4)
    copies = []
    for n in range(1, _BUER["stem_copies"] + 1):
        piece = copy.deepcopy(stem)
        piece["origin"] = [stem["origin"][0], _up(stem["origin"][1], height * n), stem["origin"][2]]
        copies.append(piece)
    base["cubes"][1:1] = copies
    for bone in bones:
        if bone["name"] == "base":
            continue
        bone["pivot"] = [bone["pivot"][0], _up(bone["pivot"][1], rise), bone["pivot"][2]]
        for cube in bone.get("cubes", []):
            cube["origin"] = [cube["origin"][0], _up(cube["origin"][1], rise), cube["origin"][2]]
            if "pivot" in cube:
                cube["pivot"] = [cube["pivot"][0], _up(cube["pivot"][1], rise), cube["pivot"][2]]
    return out, drop_clear_twins(out, sheet)


def buer_model(read):
    return buer_geometry(read)[0]


def buer_glowmask(read):
    """The pistil cap's brightest texels (its up face's texels whose red is full: the pale yellow at its heart), as the
    skin draws them; everything else clear."""
    geometry = json.loads(read(BUER_GEO))
    sheet = Image.open(io.BytesIO(read(BUER_TEXTURE))).convert("RGBA")
    pistil = next(bone for bone in geometry["minecraft:geometry"][0]["bones"] if bone["name"] == "pistil")
    cap = next(cube for cube in pistil["cubes"] if flat_axis(cube) == 1)
    out = Image.new("RGBA", sheet.size, (0, 0, 0, 0))
    for x, y in face_texels(cap["uv"]["up"]):
        pixel = sheet.getpixel((x, y))
        if pixel[3] and pixel[0] == 255:
            out.putpixel((x, y), pixel)
    return out


def buer_clips(owner):
    """Buer's own clips, keyed on the Decor Blossom's bones. His screen gestures (arrive, inspect, assent, refuse, depart)
    start and end on his listening pose (ATTEND: every petal open, the pistil leaned to his speaker), as Stolas's do; his
    work starts and ends on the owner's open pose, the frame his idle (the owner's active) begins on."""
    opened = {petal: _final(owner["animations"]["open"], petal, "rotation") for petal in PETALS}
    lean = [float(v) for v in _BUER["attend"]["pistil"]]
    zero = [0.0, 0.0, 0.0]
    whole = [1.0, 1.0, 1.0]
    lengths = _BUER["loop_lengths"]
    times = {name: length for name, (length, _) in _BUER["gesture_times"].items()}
    loops, gestures = _BUER["loops"], _BUER["gestures"]

    def petals(*frames):
        """Every petal from (time, k) pairs: k times its own open pose."""
        return {p: {"rotation": _keys(*((t, [k * v for v in opened[p]]) for t, k in frames), easing=EASE)} for p in PETALS}

    def breathing(length, cycles, k, amplitude):
        return {p: {"rotation": _wave(length, cycles, [k * v for v in opened[p]], [amplitude * v for v in opened[p]])}
                for p in PETALS}

    def loop(length, bones):
        return {"loop": True, "animation_length": length, "bones": bones}

    def once(length, bones, hold=False):
        return {"loop": "hold_on_last_frame" if hold else False, "animation_length": length, "bones": bones}

    return {
        # Listening: open, breathing, the pistil leaned toward his speaker and nodding a little.
        loops["listen"]: loop(lengths["listen"], dict(breathing(lengths["listen"], 1, 1.0, 0.12), **{
            "pistil": {"rotation": _wave(lengths["listen"], 1, lean, (3, 0, 0)), "position": _keys((0, (0, 0.5, 0)))}})),
        # Waiting, and asleep by night: shut, the whole flower swaying slowly.
        loops["waiting"]: loop(lengths["waiting"], dict(petals((0, 0.0)), **{
            "base": {"rotation": _wave(lengths["waiting"], 1, zero, (0, 0, 2))}})),
        # Suspended: shut and drooping, a slow sway.
        loops["suspended"]: loop(lengths["suspended"], dict(petals((0, 0.0)), **{
            "base": {"rotation": _wave(lengths["suspended"], 1, (14, 0, 0), (1.5, 0, 0))}})),
        # Astir (an offering due): half open and quivering, the pistil looking about.
        loops["astir"]: loop(lengths["astir"], dict(breathing(lengths["astir"], 2, 0.5, 0.15), **{
            "pistil": {"rotation": _wave(lengths["astir"], 1, zero, (0, 25, 0))}})),
        # Arrive: he rises out of the perch shut and small, grows, unfurls past open (the telling pose) and settles listening.
        gestures["arrive"]: once(times["arrive"], dict(petals((0, 0.0), (1.0, 0.0), (1.6, 1.3), (2.5, 1.0)), **{
            "base": {"position": _keys((0, (0, -6, 0)), (1.0, zero), (2.5, zero), easing=EASE),
                     "scale": _keys((0, (0.2, 0.2, 0.2)), (1.0, whole), (2.5, whole), easing=EASE)},
            "pistil": {"rotation": _keys((0, zero), (1.6, zero), (2.5, lean), easing=EASE)}})),
        # Inspect: petals half close while the pistil bows low over the offering (the telling pose), looks it over, rises.
        gestures["inspect"]: once(times["inspect"], dict(petals((0, 1.0), (0.4, 0.5), (1.2, 0.5), (2.0, 1.0)), **{
            "pistil": {"rotation": _keys((0, lean), (0.4, (40, 0, 0)), (0.7, (40, 12, 0)), (0.95, (40, -12, 0)), (1.2, (40, 0, 0)),
                                         (2.0, lean), easing=EASE)}})),
        # Assent: the whole flower bows and its petals flare wide (the telling pose), then settle.
        gestures["assent"]: once(times["assent"], dict(petals((0, 1.0), (0.5, 1.4), (1.3, 1.4), (2.0, 1.0)), **{
            "base": {"rotation": _keys((0, zero), (0.5, (12, 0, 0)), (1.3, (12, 0, 0)), (1.8, zero), (2.0, zero), easing=EASE)},
            "pistil": {"rotation": _keys((0, lean), (0.5, zero), (1.3, zero), (2.0, lean), easing=EASE)}})),
        # Refuse: the petals snap nearly shut and the pistil turns aside, raised (the telling pose), shakes once, reopens.
        gestures["refuse"]: once(times["refuse"], dict(petals((0, 1.0), (0.3, 0.25), (1.3, 0.25), (2.0, 1.0)), **{
            "pistil": {"rotation": _keys((0, lean), (0.4, (-8, 60, 0)), (0.9, (-8, 60, 0)), (1.0, (-8, 45, 0)), (1.1, (-8, 60, 0)),
                                         (1.3, (-8, 60, 0)), (2.0, lean), easing=EASE)}})),
        # Depart: his petals close (shut by 0.6 s), then he sinks into the perch from 0.8 s, dwindling to nothing (1.1 to
        # 1.6 s, shut and half sunk: the telling pose); the last frame holds until the server removes him.
        gestures["depart"]: once(times["depart"], dict(petals((0, 1.0), (0.6, 0.0), (2.0, 0.0)), **{
            "pistil": {"rotation": _keys((0, lean), (0.6, zero), (2.0, zero), easing=EASE)},
            "base": {"position": _keys((0, zero), (0.8, zero), (2.0, (0, -6, 0)), easing=EASE),
                     "scale": _keys((0, whole), (0.8, whole), (2.0, zero), easing=EASE)}}), hold=True),
        # Work (Dossier 1, detail b): first the pistil rises and points at the bed he chose while his body turns to it
        # (0.3 to 1.2 s), then he unfurls over it, petals flared (1.4 to 2.2 s: the telling pose), and settles open.
        gestures["work"]: once(times["work"], dict(petals((0, 1.0), (1.4, 1.0), (1.9, 1.5), (2.2, 1.5), (3.0, 1.0)), **{
            "pistil": {"rotation": _keys((0, zero), (0.3, zero), (1.2, (35, 0, 0)), (2.2, (35, 0, 0)), (3.0, zero), easing=EASE),
                       "position": _keys((0, zero), (0.3, zero), (1.2, (0, 1.5, 0)), (2.2, (0, 1.5, 0)), (3.0, zero), easing=EASE)}})),
    }


def buer_animations(read):
    """The runtime animation file: the owner's three clips as they are, then Buer's own (sorted by name); the owner's other
    keys (format_version, geckolib_format_version) kept."""
    owner = json.loads(read(BUER_ANIMATIONS))
    clips = dict(owner["animations"])
    for name, clip in sorted(buer_clips(owner).items()):
        if name in clips:
            raise SystemExit(f"tools/garden_bodies.py: {name} is already one of the Decor Blossom's clips")
        clips[name] = clip
    return dict(owner, animations=clips)


# ------------------------------------------------------------------------------------------------- built bodies (GC-05)

def box_faces(uv, size):
    """A box-UV region's six faces as (x, y, width, height) in the sheet, as GeckoLib bakes them (GeometryQuadUvs.ofBoxUv):
    up and down in the first row, then right (the model's -x, GeckoLib's EAST), front (north), left (+x, WEST) and back
    (south). On the front, column 0 is the model's right. The down face is read upside down; painted evenly, it shows so."""
    (u, v), (w, h, d) = uv, size
    return {"up": (u + d, v, w, d), "down": (u + d + w, v, w, d), "right": (u, v + d, d, h),
            "front": (u + d, v + d, w, h), "left": (u + d + w, v + d, d, h), "back": (u + 2 * d + w, v + d, w, h)}


SIDES = ("right", "front", "left", "back")


def _luma(colour):
    return 0.299 * colour[0] + 0.587 * colour[1] + 0.114 * colour[2]


def palette(read, path, count):
    """The opaque colours of one of the owner's paintings, darkest first; stops unless it has exactly {count} (a changed
    painting is the owner's to re-brief, never this tool's to guess)."""
    image = Image.open(io.BytesIO(read(path))).convert("RGBA")
    colours = sorted({pixel[:3] for _, pixel in image.getcolors(1 << 16) if pixel[3]}, key=lambda c: (_luma(c), c))
    if len(colours) != count:
        raise SystemExit(f"tools/garden_bodies.py: {path} has {len(colours)} colours, not the {count} the brief reads")
    return colours


def coplanar(geometry, gap=0.09):
    """Each pair of faces of two different cubes lying on one plane (closer than {gap} pixels) and facing the same way whose
    rectangles overlap: docs/ART_DIRECTION.md's "no shared face planes". Cubes as placed at rest (nothing turned)."""
    faces = []
    for bone in geometry["minecraft:geometry"][0]["bones"]:
        for index, cube in enumerate(bone.get("cubes", [])):
            low = cube["origin"]
            high = [a + b for a, b in zip(low, cube["size"])]
            for axis in range(3):
                rect = tuple((low[a], high[a]) for a in range(3) if a != axis)
                faces.append((bone["name"], index, axis, low[axis], -1, rect))
                faces.append((bone["name"], index, axis, high[axis], 1, rect))
    found = []
    for i, a in enumerate(faces):
        for b in faces[i + 1:]:
            if (a[0], a[1]) == (b[0], b[1]) or a[2] != b[2] or a[4] != b[4] or abs(a[3] - b[3]) >= gap:
                continue
            if all(min(p[1], q[1]) - max(p[0], q[0]) > 0 for p, q in zip(a[5], b[5])):
                found.append(((a[0], a[1]), (b[0], b[1]), "xyz"[a[2]], a[3]))
    return found


def _fill(img, rect, colour_at):
    """Every texel of one face, coloured by colour_at(x, y, width, height); opaque."""
    x0, y0, w, h = rect
    for y in range(h):
        for x in range(w):
            img.putpixel((x0 + x, y0 + y), colour_at(x, y, w, h) + (255,))


def _bevel(fill, light, shade):
    """Flat, lit along the top row and left column, shaded along the bottom row and right column (light from the top left)."""
    def at(x, y, w, h):
        if y == 0 or x == 0:
            return light
        if y == h - 1 or x == w - 1:
            return shade
        return fill
    return at


def _scales(base, rim, crown, seam):
    """A serpent's scales in offset rows (docs/ART_DIRECTION.md: pattern, not scatter): a lit crown row, every third row a
    rim broken once in four and shifted by two each band, a dark seam where one coil lies on the next."""
    def at(x, y, w, h):
        if y == h - 1:
            return seam
        if y == 0:
            return crown
        band = y // 3
        if y % 3 == 2 and (x + 2 * (band % 2)) % 4 != 3:
            return rim
        if y % 3 == 0 and (x + 2 * (band % 2)) % 4 == 1:
            return crown
        return base
    return at


# ------------------------------------------------------------------------------------------------- Bathin (GC-05)

WILLOW = pacts.WILLOW
MOSS = pacts.MOSS
_BATHIN = pacts.SPIRITS["bathin"]["body"]
BATHIN_TEXTURE = (128, 64)
# Box-UV regions and sizes (pixels; tools/concordance_worker_models.py's manner: a box (w, h, d) at (u, v) uses 2(w + d) x
# (d + h) from (u, v)); none overlaps another, and the two arms share one, drawn alike (check_geckolib allows a repeat).
BATHIN_UV = {"coil_base": (0, 0), "coil_mid": (56, 0), "tail_a": (104, 0), "tail_b": (116, 0), "beard": (104, 6),
             "coil_top": (0, 18), "chest": (40, 18), "head": (76, 18), "waist": (100, 18), "mantle": (0, 33), "arm": (54, 33)}
BATHIN_SIZES = {"coil_base": (14, 4, 14), "coil_mid": (12, 4, 12), "tail_a": (3, 3, 3), "tail_b": (2, 4, 2), "beard": (4, 4, 2),
                "coil_top": (10, 3, 10), "chest": (12, 9, 6), "head": (6, 7, 6), "waist": (7, 5, 5), "mantle": (20, 3, 7),
                "arm": (4, 10, 4)}
# His face (the head's front, 6 x 7, column 0 his right): H moss hair, h moss sideburn, S bark skin, B his brow, E an eye
# (the moss's palest: his glowmask lights exactly these), M his mouth. No nose holes (docs/ART_DIRECTION.md).
BATHIN_FACE = ["HHHHHH", "hSSSSh", "SBSSBS", "SESSES", "SSSSSS", "SSMMSS", "SSSSSS"]


def _bathin_box(name, origin):
    u, v = BATHIN_UV[name]
    return {"origin": list(origin), "size": list(BATHIN_SIZES[name]), "uv": [u, v]}


def bathin_model(read=None):
    """Bathin's geometry: a broad-shouldered man rising from a coiled serpent's tail, 29.5 pixels (1.84 blocks) tall at
    scale 1 and hovering half a pixel above his perch in his loops (16 pixels a block; y = 0 is the perch's top; he faces
    north, -z; his left is +x). root > coil (three coils narrowing upward) > tail (its tip, curling up behind at his left);
    root > waist (the serpent's body rising from the coils) > chest (and the moss mantle over his shoulders) > head (and his
    moss beard), arm_left, arm_right. The coil and the upper body are siblings, so a coil drawn tight never squeezes his
    chest. No cube is flat or turned at rest, and no two faces lie on one plane facing the same way (coplanar is empty)."""
    width, height = BATHIN_TEXTURE
    return {"format_version": "1.12.0", "minecraft:geometry": [{
        "description": {"identifier": "geometry.bathin", "texture_width": width, "texture_height": height,
                        "visible_bounds_width": 3, "visible_bounds_height": 3, "visible_bounds_offset": [0, 1.5, 0]},
        "bones": [
            {"name": "root", "pivot": [0, 0, 0]},
            {"name": "coil", "parent": "root", "pivot": [0, 0, 0], "cubes": [
                _bathin_box("coil_base", (-7, 0, -7)), _bathin_box("coil_mid", (-6, 3.5, -6)), _bathin_box("coil_top", (-5, 7, -5))]},
            {"name": "tail", "parent": "coil", "pivot": [4.5, 2, 6.5], "cubes": [
                _bathin_box("tail_a", (3, 0.5, 6)), _bathin_box("tail_b", (3.5, 3, 8))]},
            {"name": "waist", "parent": "root", "pivot": [0, 9.5, 0], "cubes": [_bathin_box("waist", (-3.5, 9.5, -2.5))]},
            {"name": "chest", "parent": "waist", "pivot": [0, 14, 0], "cubes": [
                _bathin_box("chest", (-6, 14, -3)), _bathin_box("mantle", (-10, 20.5, -3.5))]},
            {"name": "head", "parent": "chest", "pivot": [0, 23, -1], "cubes": [
                _bathin_box("head", (-3, 22.5, -4)), _bathin_box("beard", (-2, 18.5, -4.75))]},
            {"name": "arm_left", "parent": "chest", "pivot": [7.5, 22, 0], "cubes": [_bathin_box("arm", (5.5, 12.5, -2))]},
            {"name": "arm_right", "parent": "chest", "pivot": [-7.5, 22, 0], "cubes": [_bathin_box("arm", (-9.5, 12.5, -2))]},
        ]}]}


def bathin_sheet(read):
    """His 128 x 64 skin, every texel opaque and one of the two paintings' colours (cute and clean: two or three tones a
    part, light from the top left, regular patterns; docs/ART_DIRECTION.md "Creatures and faces"). The willow's five tones,
    darkest first, W0 to W4: his coil and tail in scales of W1 to W3 with a W0 seam, his skin W3 lit W4 and shaded W2, his
    brow and mouth W0. The moss's eight, M0 to M7: his mantle M2 to M5 with a fringe, his beard and hair M1 to M5, his belly
    scutes M5 to M7 and his eyes M7."""
    W0, W1, W2, W3, W4 = palette(read, WILLOW, 5)
    M0, M1, M2, M3, M4, M5, M6, M7 = palette(read, MOSS, 8)
    img = Image.new("RGBA", BATHIN_TEXTURE, (0, 0, 0, 0))

    def part(name):
        return box_faces(BATHIN_UV[name], BATHIN_SIZES[name])

    skin = _bevel(W3, W4, W2)
    for name in ("coil_base", "coil_mid", "coil_top", "tail_a", "tail_b"):
        faces = part(name)
        for side in SIDES:
            _fill(img, faces[side], _scales(W2, W1, W3, W0))
        _fill(img, faces["up"], _bevel(W4, W4, W3) if name == "tail_b" else _scales(W3, W2, W4, W2))
        _fill(img, faces["down"], lambda x, y, w, h: W0)
    faces = part("waist")
    _fill(img, faces["front"], lambda x, y, w, h: M7 if x == 0 else M5 if y % 2 == 1 or x == w - 1 else M6)
    for side in ("right", "left", "back"):
        _fill(img, faces[side], _scales(W2, W1, W3, W1))
    _fill(img, faces["up"], lambda x, y, w, h: W3)
    _fill(img, faces["down"], lambda x, y, w, h: W1)
    faces = part("chest")

    def chest_front(x, y, w, h):
        if y <= 1:
            return W4
        if y == 2:
            return W2
        return skin(x, y, w, h)
    _fill(img, faces["front"], chest_front)
    for side in ("right", "left", "back"):
        _fill(img, faces[side], skin)
    _fill(img, faces["up"], lambda x, y, w, h: W3)
    _fill(img, faces["down"], lambda x, y, w, h: W2)
    faces = part("arm")

    def arm(x, y, w, h):
        if y == h - 4:
            return M4
        if y == h - 3:
            return M2
        return skin(x, y, w, h)
    for side in SIDES:
        _fill(img, faces[side], arm)
    _fill(img, faces["up"], lambda x, y, w, h: W3)
    _fill(img, faces["down"], lambda x, y, w, h: W4)
    faces = part("mantle")
    _fill(img, faces["up"], lambda x, y, w, h: M4 if y == 0 or x == 0 else M5 if x % 3 == 1 and y % 2 == 1 else M3)
    _fill(img, faces["down"], lambda x, y, w, h: M1)
    for side in SIDES:
        _fill(img, faces[side], lambda x, y, w, h: M4 if y == 0 else M3 if y < h - 1 else M2 if x % 2 == 0 else M1)
    faces = part("head")

    def face(x, y, w, h):
        key = BATHIN_FACE[y][x]
        if key == "S":
            return W4 if x == 0 else W2 if x == w - 1 else W3
        return {"H": M2, "h": M3, "B": W0, "E": M7, "M": W0}[key]
    _fill(img, faces["front"], face)
    _fill(img, faces["up"], lambda x, y, w, h: M4 if y == 0 or x == 0 else M5 if (x + y) % 3 == 0 else M3)
    _fill(img, faces["back"], lambda x, y, w, h: M2 if y < h - 2 else skin(x, y, w, h))
    for side in ("right", "left"):
        _fill(img, faces[side], lambda x, y, w, h: M2 if y < 2 else skin(x, y, w, h))
    _fill(img, faces["down"], lambda x, y, w, h: W2)
    faces = part("beard")
    _fill(img, faces["front"], lambda x, y, w, h: (M2 if x % 2 == 0 else M1) if y == h - 1 else M5 if x % 2 == 0 else M3)
    for side in ("right", "left", "back"):
        _fill(img, faces[side], lambda x, y, w, h: M2)
    _fill(img, faces["up"], lambda x, y, w, h: M3)
    _fill(img, faces["down"], lambda x, y, w, h: M1)
    return img


def bathin_skin(read):
    return bathin_sheet(read)


def bathin_glowmask(read):
    """His two eyes alone (SPIRITS["bathin"]["body"]["eye_texels"]), as his skin draws them; everything else clear."""
    sheet = bathin_sheet(read)
    out = Image.new("RGBA", sheet.size, (0, 0, 0, 0))
    for texel in _BATHIN["eye_texels"]:
        out.putpixel(tuple(texel), sheet.getpixel(tuple(texel)))
    return out


def bathin_clips():
    """His own clips, keyed on bathin_model's bones (degrees, Bedrock's sense: +x pitches a bone's top forward; a left arm,
    +x, turns outward and up with -z, a right one with +z). His screen gestures start and end on his listening pose
    (ATTEND: arms folded, chest up, head inclined); his work starts and ends at rest, the frame his idle begins on. Detail (a):
    he arrives already whole and coiled tight (no growing in, no path) and uncoils; he departs by drawing his coil tight and
    is gone in one frame, 0.8 to 0.85 s in, the last frame held until the server removes him."""
    attend = {bone: [float(v) for v in angles] for bone, angles in _BATHIN["attend"].items()}
    lengths = _BATHIN["loop_lengths"]
    times = {name: length for name, (length, _) in _BATHIN["gesture_times"].items()}
    loops, gestures = _BATHIN["loops"], _BATHIN["gestures"]
    zero, whole = [0.0, 0.0, 0.0], [1.0, 1.0, 1.0]
    tight = [0.78, 1.22, 0.78]
    folded = {"arm_left": [-80.0, 0.0, 55.0], "arm_right": [-80.0, 0.0, -55.0]}

    def still(angles):
        return {"0.0": [round(float(v), 3) + 0.0 for v in angles]}

    def hover(length, cycles=1):
        return {"position": _wave(length, cycles, (0, 0.5, 0), (0, 0.5, 0), phase=-0.25)}

    def loop(length, bones):
        return {"loop": True, "animation_length": length, "bones": bones}

    def once(length, bones, hold=False):
        return {"loop": "hold_on_last_frame" if hold else False, "animation_length": length, "bones": bones}

    def from_attend(bone, *frames, length):
        return _keys((0, attend[bone]), *frames, (length, attend[bone]), easing=EASE)

    return {
        # Idle: hovering, a slow sway, his head turning a little, his tail tip flicking.
        loops["idle"]: loop(lengths["idle"], {
            "root": hover(lengths["idle"]),
            "waist": {"rotation": _wave(lengths["idle"], 1, zero, (0, 0, 1.5))},
            "head": {"rotation": _wave(lengths["idle"], 1, zero, (0, 6, 0), phase=0.25)},
            "arm_left": {"rotation": _wave(lengths["idle"], 1, (0, 0, -3), (2, 0, 0))},
            "arm_right": {"rotation": _wave(lengths["idle"], 1, (0, 0, 3), (2, 0, 0))},
            "tail": {"rotation": _wave(lengths["idle"], 2, zero, (0, 12, 0))}}),
        # Listening: arms folded, chest up, head inclined to his speaker, breathing.
        loops["listen"]: loop(lengths["listen"], {
            "root": hover(lengths["listen"]),
            "waist": {"rotation": _wave(lengths["listen"], 1, attend["waist"], (1.5, 0, 0))},
            "head": {"rotation": _wave(lengths["listen"], 1, attend["head"], (2, 0, 0), phase=0.25)},
            "arm_left": {"rotation": still(attend["arm_left"])},
            "arm_right": {"rotation": still(attend["arm_right"])}}),
        # Waiting (off his service, or switched off): arms folded, head turned away, tail tip tapping: he scorns waiting.
        loops["waiting"]: loop(lengths["waiting"], {
            "root": hover(lengths["waiting"]),
            "head": {"rotation": still((-6, 25, 0))},
            "arm_left": {"rotation": still(attend["arm_left"])},
            "arm_right": {"rotation": still(attend["arm_right"])},
            "tail": {"rotation": _wave(lengths["waiting"], 2, zero, (18, 0, 0))}}),
        # Suspended: his coil slumped, bowed, arms hanging, his tail limp; no hover.
        loops["suspended"]: loop(lengths["suspended"], {
            "coil": {"scale": still((1.05, 0.9, 1.05))},
            "waist": {"rotation": _wave(lengths["suspended"], 1, (14, 0, 0), (0, 0, 2))},
            "head": {"rotation": still((24, 0, 0))},
            "arm_left": {"rotation": still((6, 0, 4))},
            "arm_right": {"rotation": still((6, 0, -4))},
            "tail": {"rotation": still((20, 0, 0))}}),
        # Astir (an offering due): fingers drumming, tail lashing, head jerking about.
        loops["astir"]: loop(lengths["astir"], {
            "root": hover(lengths["astir"]),
            "head": {"rotation": _wave(lengths["astir"], 3, (-4, 0, 0), (0, 8, 0))},
            "arm_left": {"rotation": still((0, 0, -6))},
            "arm_right": {"rotation": _wave(lengths["astir"], 3, (-35, 0, -10), (10, 0, 0))},
            "tail": {"rotation": _wave(lengths["astir"], 2, zero, (0, 25, 0))}}),
        # Arrive: whole at once, coiled tight with his arms folded hard and head down (the telling pose, 0.1 to 0.6 s, in
        # his ring of dust), then he uncoils and settles listening.
        gestures["arrive"]: once(times["arrive"], {
            "coil": {"scale": _keys((0, tight), (0.6, tight), (1.2, whole), (1.5, whole), easing=EASE)},
            "waist": {"position": _keys((0, (0, -1.5, 0)), (0.6, (0, -1.5, 0)), (1.2, zero), (1.5, zero), easing=EASE),
                      "rotation": _keys((0, zero), (0.6, zero), (1.2, attend["waist"]), (1.5, attend["waist"]), easing=EASE)},
            "head": {"rotation": _keys((0, (25, 0, 0)), (0.6, (25, 0, 0)), (1.2, attend["head"]), (1.5, attend["head"]), easing=EASE)},
            "arm_left": {"rotation": _keys((0, folded["arm_left"]), (0.6, folded["arm_left"]), (1.2, attend["arm_left"]),
                                           (1.5, attend["arm_left"]), easing=EASE)},
            "arm_right": {"rotation": _keys((0, folded["arm_right"]), (0.6, folded["arm_right"]), (1.2, attend["arm_right"]),
                                            (1.5, attend["arm_right"]), easing=EASE)}}),
        # Inspect: he leans over the perch, one hand out palm up, weighing what is offered (0.4 to 1.2 s).
        gestures["inspect"]: once(times["inspect"], {
            "waist": {"rotation": from_attend("waist", (0.4, (14, 0, 0)), (1.2, (14, 0, 0)), length=times["inspect"])},
            "head": {"rotation": from_attend("head", (0.4, (25, 0, 0)), (1.2, (25, 0, 0)), length=times["inspect"])},
            "arm_right": {"rotation": from_attend("arm_right", (0.4, (-75, 0, -5)), (0.65, (-75, 12, -5)), (0.95, (-75, -12, -5)),
                                                  (1.2, (-75, 0, -5)), length=times["inspect"])},
            "arm_left": {"rotation": still(attend["arm_left"])}}),
        # Assent: chest out, a curt bow, fist to his chest (0.3 to 0.9 s).
        gestures["assent"]: once(times["assent"], {
            "waist": {"rotation": from_attend("waist", (0.3, (-10, 0, 0)), (0.9, (-10, 0, 0)), length=times["assent"])},
            "head": {"rotation": from_attend("head", (0.3, (22, 0, 0)), (0.9, (22, 0, 0)), length=times["assent"])},
            "arm_right": {"rotation": from_attend("arm_right", (0.3, (-100, 0, -50)), (0.9, (-100, 0, -50)), length=times["assent"])},
            "arm_left": {"rotation": still(attend["arm_left"])}}),
        # Refuse: he turns his shoulder, his head away, a flat hand cutting out sideways, his tail lashing (0.3 to 1.0 s).
        gestures["refuse"]: once(times["refuse"], {
            "waist": {"rotation": from_attend("waist", (0.3, (-2, 35, 0)), (1.0, (-2, 35, 0)), length=times["refuse"])},
            "head": {"rotation": from_attend("head", (0.3, (0, 30, 0)), (1.0, (0, 30, 0)), length=times["refuse"])},
            "arm_left": {"rotation": from_attend("arm_left", (0.3, (0, 0, -80)), (1.0, (0, 0, -80)), length=times["refuse"])},
            "arm_right": {"rotation": still(attend["arm_right"])},
            "tail": {"rotation": _keys((0, zero), (0.3, (0, 30, 0)), (0.55, (0, -30, 0)), (0.8, (0, 30, 0)), (1.0, zero),
                                       (times["refuse"], zero), easing=EASE)}}),
        # Depart: his coil draws tight, arms folded hard, head down (0.25 to 0.75 s), then he is gone (0.8 to 0.85 s), held.
        gestures["depart"]: once(times["depart"], {
            "coil": {"scale": _keys((0, whole), (0.25, tight), (0.8, tight), easing=EASE)},
            "waist": {"position": _keys((0, zero), (0.25, (0, -1.5, 0)), (0.8, (0, -1.5, 0)), easing=EASE)},
            "head": {"rotation": _keys((0, attend["head"]), (0.25, (25, 0, 0)), (0.8, (25, 0, 0)), easing=EASE)},
            "arm_left": {"rotation": _keys((0, attend["arm_left"]), (0.25, folded["arm_left"]), (0.8, folded["arm_left"]), easing=EASE)},
            "arm_right": {"rotation": _keys((0, attend["arm_right"]), (0.25, folded["arm_right"]), (0.8, folded["arm_right"]),
                                            easing=EASE)},
            "root": {"scale": _keys((0, whole), (0.8, whole), (0.85, zero), (times["depart"], zero))}}, hold=True),
        # Work: one sharp point of his right arm at where the Verdance is wanted, his coil and tail drawn in (0.15 to 0.75 s);
        # the two reservoirs shimmer on the server in the same tick (BathinEntity.shimmer), nothing crossing between.
        gestures["work"]: once(times["work"], {
            "arm_right": {"rotation": _keys((0, zero), (0.15, (-90, 0, 0)), (0.75, (-90, 0, 0)), (1.2, zero), (times["work"], zero),
                                            easing=EASE)},
            "coil": {"scale": _keys((0, whole), (0.15, (0.95, 1.05, 0.95)), (0.75, (0.95, 1.05, 0.95)), (1.2, whole),
                                    (times["work"], whole), easing=EASE)},
            "tail": {"rotation": _keys((0, zero), (0.15, (0, -25, 0)), (0.75, (0, -25, 0)), (1.2, zero), (times["work"], zero),
                                       easing=EASE)}}),
    }


def bathin_animations(read=None):
    """The runtime animation file: his eleven clips, sorted by name."""
    return {"format_version": "1.8.0", "animations": dict(sorted(bathin_clips().items()))}


# Each later spirit's body (GC-06 to GC-09) under its unit's line.
# GC-06 unit 3
# GC-07 unit 4
# Foras (GC-07, GC-S12, GC-P92, GC-P93; docs/features/foras-pact.md): built here, as Bathin is, since no rig of the owner's is
# a tall elder. foras_model reads the owner's acorn cap (art/owner-library/originals/Edited-2026-10-18-Part2/jims mobs/,
# through tools/owner_art.py's second root: read(path) with the part's folder name first) and copies its four armorHead
# cubes onto his head; foras_skin paints only in the colours of the fir log's bark and the Spanish moss (Blocks-folder
# paintings) around the cap's texels, copied unchanged; foras_animations reads nothing. No glowmask.
# ------------------------------------------------------------------------------------------------- Foras (GC-07)

FIR = pacts.FIR
MOSS = pacts.MOSS
ACORN_GEO = pacts.ACORN_GEO
ACORN_TEXTURE = pacts.ACORN_TEXTURE
_FORAS = pacts.SPIRITS["foras"]["body"]
FORAS_TEXTURE = (128, 64)
# The owner's acorn cap draws only inside this rectangle of its 128 x 128 sheet (x, y, width, height): copied texel for
# texel to the same place in Foras's sheet, so its cubes keep the owner's UVs unchanged and none of his own parts uses it.
ACORN_REGION = (0, 0, 40, 24)
# The owner's armorHead cubes sit on a biped head whose 8-pixel cube starts at (-4, 24, -4); Foras's head cube starts at
# FORAS_HEAD, so each cap cube moves by the difference and nothing else of it changes (but the leaf's clear face).
BIPED_HEAD = (-4, 24, -4)
FORAS_HEAD = (-4, 21, -7)
# Box-UV regions and sizes (pixels; a box (w, h, d) at (u, v) uses 2(w + d) x (d + h) from (u, v)); none overlaps another
# or the cap's rectangle; the two arms, the two feet and the two cuttings each share one region, drawn alike.
FORAS_UV = {"mantle": (40, 0), "head": (88, 0), "nose": (120, 0), "beard": (40, 14), "satchel": (56, 14), "cutting": (70, 14),
            "foot": (76, 16), "chest": (0, 24), "skirt": (40, 24), "hem": (78, 24), "arm": (0, 41), "flap": (16, 41)}
FORAS_SIZES = {"mantle": (14, 4, 10), "head": (8, 8, 8), "nose": (2, 2, 1), "beard": (6, 5, 2), "satchel": (5, 5, 2),
               "cutting": (2, 3, 1), "foot": (3, 2, 4), "chest": (12, 9, 8), "skirt": (11, 9, 8), "hem": (12, 3, 9),
               "arm": (4, 15, 4), "flap": (6, 1, 3)}
# His face (the head's front, 8 x 8, row 0 its top, column 0 his right). The cap's brim covers rows 0 to 2 (the owner's
# hat sits so on any head); B his moss brows, E an eye (the fir's darkest), S pale bark skin, h a moss sideburn. His nose and
# beard are cubes of their own. No nose holes (docs/ART_DIRECTION.md).
FORAS_FACE = ["SSSSSSSS", "SSSSSSSS", "SSSSSSSS", "BBBSSBBB", "SESSSSES", "SSSSSSSS", "hSSSSSSh", "hhSSSShh"]


def _foras_box(name, origin):
    u, v = FORAS_UV[name]
    return {"origin": [float(c) for c in origin], "size": list(FORAS_SIZES[name]), "uv": [u, v]}


def acorn_cap(read):
    """The owner's acorn cap, as Foras wears it: the four cubes of its armorHead bone (art/owner-library/originals/
    Edited-2026-10-18-Part2/jims mobs/geo/item/armor/acorn.geo.json), each moved by FORAS_HEAD - BIPED_HEAD and otherwise
    the owner's (size, box UV). Its fourth cube is a leaf of no thickness drawing a north and a south face on one plane;
    it is written with per-face UVs (the same rectangles box UV gives it, GeckoLib GeometryQuadUvs.ofBoxUv) so the clear
    face can be dropped (drop_clear_twins: the south, 0 of 16 texels drawn), as Buer's twins are (GB24)."""
    owner = json.loads(read(ACORN_GEO))
    head = next(bone for bone in owner["minecraft:geometry"][0]["bones"] if bone["name"] == "armorHead")
    shift = [a - b for a, b in zip(FORAS_HEAD, BIPED_HEAD)]
    cubes = []
    for cube in head["cubes"]:
        moved = dict(cube, origin=[round(o + s, 4) for o, s in zip(cube["origin"], shift)])
        if flat_axis(cube) is not None:
            (u, v), (w, h, d) = cube["uv"], cube["size"]
            moved["uv"] = {"north": {"uv": [u + d, v + d], "uv_size": [w, h]},
                           "south": {"uv": [u + 2 * d + w, v + d], "uv_size": [w, h]}}
        cubes.append(moved)
    return cubes


def foras_model(read):
    """Foras's geometry: a tall, broad, stooped elder, 35 pixels (2.19 blocks) tall at scale 1 (16 pixels a block; y = 0
    the floor beside his perch; he faces north, -z; his left is +x). root > robe (the hem, the skirt and his two feet),
    satchel (at his left hip, in front of his robe: its flap and two cuttings), body (the chest, a hand ahead of the robe: his stoop) > head
    (and his nose and moss beard) > cap (the owner's acorn cap), arm_left, arm_right (to his knees). Built here but the cap
    (copied, moved, its leaf's clear face dropped); no cube turned at rest, none flat but the cap's leaf, and no two faces
    of different cubes on one plane facing the same way (coplanar is empty)."""
    width, height = FORAS_TEXTURE
    sheet = Image.open(io.BytesIO(read(ACORN_TEXTURE))).convert("RGBA")
    hx, hy, hz = FORAS_HEAD
    out = {"format_version": "1.12.0", "minecraft:geometry": [{
        "description": {"identifier": "geometry.foras", "texture_width": width, "texture_height": height,
                        "visible_bounds_width": 3, "visible_bounds_height": 3.5, "visible_bounds_offset": [0, 1.75, 0]},
        "bones": [
            {"name": "root", "pivot": [0, 0, 0]},
            {"name": "robe", "parent": "root", "pivot": [0, 0, 0], "cubes": [
                _foras_box("hem", (-6, 1, -4.5)), _foras_box("skirt", (-5.5, 4, -4)),
                _foras_box("foot", (-4, 0, -6)), _foras_box("foot", (1, 0, -6))]},
            {"name": "satchel", "parent": "root", "pivot": [3.5, 10, -5], "cubes": [
                _foras_box("satchel", (1, 5, -6)), _foras_box("flap", (0.5, 9.5, -6.5))]},
            {"name": "cuttings", "parent": "satchel", "pivot": [3.5, 10, -5], "cubes": [
                _foras_box("cutting", (1.5, 10, -5.5)), _foras_box("cutting", (3.75, 10.5, -5.25))]},
            {"name": "body", "parent": "root", "pivot": [0, 13, 0], "cubes": [
                _foras_box("chest", (-6, 13, -4.5)), _foras_box("mantle", (-7, 19, -5.5))]},
            {"name": "head", "parent": "body", "pivot": [0, 21, -3], "cubes": [
                _foras_box("head", (hx, hy, hz)), _foras_box("nose", (-1, 23.5, -8.0)), _foras_box("beard", (-3, 17, -8.75))]},
            {"name": "cap", "parent": "head", "pivot": [0, hy + 8, hz], "cubes": acorn_cap(read)},
            {"name": "arm_left", "parent": "body", "pivot": [8, 21, -1], "cubes": [_foras_box("arm", (6.5, 6, -3))]},
            {"name": "arm_right", "parent": "body", "pivot": [-8, 21, -1], "cubes": [_foras_box("arm", (-10.5, 6, -3))]},
        ]}]}
    dropped = drop_clear_twins(out, sheet)
    if dropped != {("cap", 3): "south"}:
        raise SystemExit(f"tools/garden_bodies.py: the acorn cap's leaf drops {dropped}, not its clear south face: re-brief")
    return out


def foras_sheet(read):
    """His 128 x 64 skin: the owner's acorn cap texel for texel in ACORN_REGION, and every other texel opaque or clear,
    painted only in the colours of two of the owner's paintings (cute and clean: two or three tones a part, light from the
    top left, regular patterns; docs/ART_DIRECTION.md "Creatures and faces"). The fir log's seven tones, darkest first,
    F0 to F6: his robe F3 lit F5 and shaded F1, in bark furrows of F1 every third column; his skin and hands F5 lit F6 and
    shaded F4; his satchel F1 lit F3, its flap F0; his eyes F0. The moss's eight, M0 to M7: his mantle M3 to M5 with a fringe,
    his beard and brows M4 to M7, a moss band round his hem, the cuttings' leaves M4 and M5 on F1 stems."""
    F = palette(read, FIR, 7)
    M = palette(read, MOSS, 8)
    img = Image.new("RGBA", FORAS_TEXTURE, (0, 0, 0, 0))
    acorn = Image.open(io.BytesIO(read(ACORN_TEXTURE))).convert("RGBA")
    x0, y0, w0, h0 = ACORN_REGION
    img.paste(acorn.crop((x0, y0, x0 + w0, y0 + h0)), (x0, y0))

    def part(name):
        return box_faces(FORAS_UV[name], FORAS_SIZES[name])

    robe = _bevel(F[3], F[5], F[1])
    skin = _bevel(F[5], F[6], F[4])

    def furrowed(x, y, w, h):
        return F[1] if x % 3 == 2 and 0 < y < h - 1 else robe(x, y, w, h)

    for name in ("skirt", "chest"):
        faces = part(name)
        for side in SIDES:
            _fill(img, faces[side], furrowed)
        _fill(img, faces["up"], lambda x, y, w, h: F[4])
        _fill(img, faces["down"], lambda x, y, w, h: F[1])
    faces = part("hem")
    for side in SIDES:
        _fill(img, faces[side], lambda x, y, w, h: M[5] if y == 0 else M[3] if y == 1 else M[2] if x % 2 == 0 else M[1])
    _fill(img, faces["up"], lambda x, y, w, h: M[4])
    _fill(img, faces["down"], lambda x, y, w, h: M[0])
    faces = part("foot")
    for side in SIDES + ("up",):
        _fill(img, faces[side], _bevel(F[2], F[4], F[0]))
    _fill(img, faces["down"], lambda x, y, w, h: F[0])
    faces = part("mantle")
    _fill(img, faces["up"], lambda x, y, w, h: M[5] if y == 0 or x == 0 else M[6] if x % 3 == 1 and y % 2 == 1 else M[4])
    _fill(img, faces["down"], lambda x, y, w, h: M[1])
    for side in SIDES:
        _fill(img, faces[side], lambda x, y, w, h: M[5] if y == 0 else M[4] if y < h - 1 else M[3] if x % 2 == 0 else M[2])
    faces = part("head")

    def face(x, y, w, h):
        key = FORAS_FACE[y][x]
        if key == "S":
            return skin(x, y, w, h)
        return {"B": M[6], "E": F[0], "h": M[5]}[key]
    _fill(img, faces["front"], face)
    for side in ("right", "left", "back"):
        _fill(img, faces[side], lambda x, y, w, h: M[5] if y >= h - 2 else skin(x, y, w, h))
    _fill(img, faces["up"], lambda x, y, w, h: F[5])
    _fill(img, faces["down"], lambda x, y, w, h: M[4])
    faces = part("nose")
    for side in SIDES + ("up", "down"):
        _fill(img, faces[side], _bevel(F[5], F[6], F[4]))
    faces = part("beard")
    _fill(img, faces["front"], lambda x, y, w, h: (M[5] if x % 2 == 0 else M[4]) if y == h - 1 else M[7] if x % 2 == 0 else M[6])
    for side in ("right", "left", "back"):
        _fill(img, faces[side], lambda x, y, w, h: M[5])
    _fill(img, faces["up"], lambda x, y, w, h: M[6])
    _fill(img, faces["down"], lambda x, y, w, h: M[4])
    faces = part("arm")

    def arm(x, y, w, h):
        if y >= h - 3:
            return skin(x, y - (h - 3), w, 3)
        if y == h - 4:
            return M[4]
        return robe(x, y, w, h - 4)
    for side in SIDES:
        _fill(img, faces[side], arm)
    _fill(img, faces["up"], lambda x, y, w, h: F[4])
    _fill(img, faces["down"], lambda x, y, w, h: F[5])
    faces = part("satchel")
    for side in SIDES + ("up",):
        _fill(img, faces[side], _bevel(F[1], F[3], F[0]))
    _fill(img, faces["down"], lambda x, y, w, h: F[0])
    faces = part("flap")
    for side in SIDES + ("up", "down"):
        _fill(img, faces[side], lambda x, y, w, h: F[0] if y == h - 1 or x == w - 1 else F[2])
    faces = part("cutting")
    for side in SIDES:
        _fill(img, faces[side], lambda x, y, w, h: M[5] if y == 0 else M[4] if y == 1 else F[1])
    _fill(img, faces["up"], lambda x, y, w, h: M[5])
    _fill(img, faces["down"], lambda x, y, w, h: F[1])
    return img


def foras_skin(read):
    return foras_sheet(read)


def foras_clips():
    """His own clips, keyed on foras_model's bones (degrees, Bedrock's sense: +x pitches a bone's top forward; a left arm,
    +x, swings outward with -z, a right one with +z). His screen gestures start and end on his listening pose (ATTEND: his
    stoop deepened toward his speaker, his head raised to them, his right hand on his satchel's strap); his work starts and
    ends at rest, the frame his idle begins on. Detail (a): his cap tips forward on its own pivot at his greeting (arrive)
    and at each graft (work), and only then."""
    attend = {bone: [float(v) for v in angles] for bone, angles in _FORAS["attend"].items()}
    lengths = _FORAS["loop_lengths"]
    times = {name: length for name, (length, _) in _FORAS["gesture_times"].items()}
    loops, gestures = _FORAS["loops"], _FORAS["gestures"]
    zero, whole = [0.0, 0.0, 0.0], [1.0, 1.0, 1.0]
    stoop = [6.0, 0.0, 0.0]
    tipped = [float(v) for v in _FORAS["cap_tip"]]

    def still(angles):
        return {"0.0": [round(float(v), 3) + 0.0 for v in angles]}

    def loop(length, bones):
        return {"loop": True, "animation_length": length, "bones": bones}

    def once(length, bones, hold=False):
        return {"loop": "hold_on_last_frame" if hold else False, "animation_length": length, "bones": bones}

    def from_attend(bone, *frames, length):
        return _keys((0, attend[bone]), *frames, (length, attend[bone]), easing=EASE)

    return {
        # Idle: stooped, breathing slowly, his head turning a little, his long arms swinging a hair.
        loops["idle"]: loop(lengths["idle"], {
            "body": {"rotation": _wave(lengths["idle"], 1, stoop, (1.5, 0, 0))},
            "head": {"rotation": _wave(lengths["idle"], 1, (-4, 0, 0), (0, 8, 0), phase=0.25)},
            "arm_left": {"rotation": _wave(lengths["idle"], 1, zero, (3, 0, 0))},
            "arm_right": {"rotation": _wave(lengths["idle"], 1, zero, (-3, 0, 0))}}),
        # Listening: his stoop deepened toward his speaker, his head raised to them, one hand on his satchel's strap.
        loops["listen"]: loop(lengths["listen"], {
            "body": {"rotation": _wave(lengths["listen"], 1, attend["body"], (1, 0, 0))},
            "head": {"rotation": _wave(lengths["listen"], 1, attend["head"], (2, 0, 0), phase=0.25)},
            "arm_right": {"rotation": still(attend["arm_right"])}}),
        # Waiting (off his service, or switched off): arms folded behind him, head bowed in thought, swaying.
        loops["waiting"]: loop(lengths["waiting"], {
            "body": {"rotation": _wave(lengths["waiting"], 1, stoop, (0, 0, 1.5))},
            "head": {"rotation": still((14, 0, 0))},
            "arm_left": {"rotation": still((28, 0, 10))},
            "arm_right": {"rotation": still((28, 0, -10))}}),
        # Suspended: bent low, arms hanging, his satchel slack.
        loops["suspended"]: loop(lengths["suspended"], {
            "body": {"rotation": _wave(lengths["suspended"], 1, (20, 0, 0), (1, 0, 0))},
            "head": {"rotation": still((18, 0, 0))},
            "satchel": {"rotation": still((0, 0, 8))}}),
        # Astir (an offering due): tapping his satchel, looking about.
        loops["astir"]: loop(lengths["astir"], {
            "body": {"rotation": still(stoop)},
            "head": {"rotation": _wave(lengths["astir"], 2, zero, (0, 20, 0))},
            "arm_left": {"rotation": _wave(lengths["astir"], 3, (-20, 0, -8), (8, 0, 0))}}),
        # Arrive: he grows out of the air beside the perch, straightens, and tips his cap to whoever he came for (the
        # telling pose, 0.6 to 1.4 s), then settles listening.
        gestures["arrive"]: once(times["arrive"], {
            "root": {"scale": _keys((0, (0.3, 0.3, 0.3)), (0.5, whole), (times["arrive"], whole), easing=EASE)},
            "body": {"rotation": _keys((0, (20, 0, 0)), (0.5, (14, 0, 0)), (0.6, (14, 0, 0)), (1.4, (14, 0, 0)), (2.0, attend["body"]), easing=EASE)},
            "cap": {"rotation": _keys((0, zero), (0.5, zero), (0.6, tipped), (1.4, tipped), (1.7, zero), (2.0, zero), easing=EASE)},
            "arm_right": {"rotation": _keys((0, zero), (0.5, (-110, 0, -20)), (1.4, (-110, 0, -20)), (2.0, attend["arm_right"]), easing=EASE)}}),
        # Inspect: he leans in over what is offered, a hand at his beard (0.4 to 1.2 s).
        gestures["inspect"]: once(times["inspect"], {
            "body": {"rotation": from_attend("body", (0.4, (24, 0, 0)), (1.2, (24, 0, 0)), length=times["inspect"])},
            "head": {"rotation": from_attend("head", (0.4, (10, 0, 0)), (0.7, (10, 12, 0)), (0.95, (10, -12, 0)), (1.2, (10, 0, 0)),
                                             length=times["inspect"])},
            "arm_right": {"rotation": from_attend("arm_right", (0.4, (-120, 0, 20)), (1.2, (-120, 0, 20)), length=times["inspect"])}}),
        # Assent: a slow nod, one open hand raised (0.4 to 1.0 s).
        gestures["assent"]: once(times["assent"], {
            "head": {"rotation": from_attend("head", (0.4, (16, 0, 0)), (1.0, (16, 0, 0)), length=times["assent"])},
            "arm_left": {"rotation": _keys((0, zero), (0.4, (-60, 0, -20)), (1.0, (-60, 0, -20)), (times["assent"], zero), easing=EASE)},
            "arm_right": {"rotation": still(attend["arm_right"])}}),
        # Refuse: arms folded across his chest, his head shaking once (0.4 to 1.3 s).
        gestures["refuse"]: once(times["refuse"], {
            "arm_left": {"rotation": _keys((0, zero), (0.4, (-70, 0, 45)), (1.3, (-70, 0, 45)), (times["refuse"], zero), easing=EASE)},
            "arm_right": {"rotation": from_attend("arm_right", (0.4, (-70, 0, -45)), (1.3, (-70, 0, -45)), length=times["refuse"])},
            "head": {"rotation": from_attend("head", (0.4, (4, -18, 0)), (0.7, (4, 18, 0)), (1.0, (4, -18, 0)), (1.3, (4, 0, 0)),
                                             length=times["refuse"])}}),
        # Depart: he bows (0.4 to 1.2 s), folds his satchel shut, and fades to nothing; the last frame holds until the server
        # removes him.
        gestures["depart"]: once(times["depart"], {
            "body": {"rotation": _keys((0, attend["body"]), (0.4, (32, 0, 0)), (1.2, (32, 0, 0)), (1.6, stoop), easing=EASE)},
            "satchel": {"scale": _keys((0, whole), (1.2, whole), (1.6, (1.0, 0.4, 1.0)), easing=EASE)},
            "root": {"scale": _keys((0, whole), (1.8, whole), (2.5, zero), (times["depart"], zero), easing=EASE)}}, hold=True),
        # Work (each graft): his hand goes to his satchel for a cutting, then reaches toward the host he grafts as his cap tips
        # (the telling pose, 0.8 to 1.5 s), and comes back.
        gestures["work"]: once(times["work"], {
            "arm_left": {"rotation": _keys((0, zero), (0.4, (-30, 0, -25)), (0.8, (-85, 0, 0)), (1.5, (-85, 0, 0)), (2.0, zero),
                                           (times["work"], zero), easing=EASE)},
            "body": {"rotation": _keys((0, stoop), (0.8, (18, 0, 0)), (1.5, (18, 0, 0)), (2.0, stoop), (times["work"], stoop), easing=EASE)},
            "cap": {"rotation": _keys((0, zero), (0.8, tipped), (1.5, tipped), (1.9, zero), (times["work"], zero), easing=EASE)},
            "cuttings": {"scale": _keys((0, whole), (0.4, whole), (0.6, (1.0, 0.6, 1.0)), (2.0, (1.0, 0.6, 1.0)), (times["work"], whole),
                                        easing=EASE)}}),
    }


def foras_animations(read=None):
    """The runtime animation file: his eleven clips, sorted by name."""
    return {"format_version": "1.8.0", "animations": dict(sorted(foras_clips().items()))}
# GC-08 unit 5
# GC-09 unit 6
