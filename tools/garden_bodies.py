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

`read(path)` returns a library file's bytes, `path` relative to art/owner-library/originals/Magic/.
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
        # Depart: his petals close (shut by 0.6 s: the telling pose), then he sinks into the perch, dwindling to nothing; the
        # last frame holds until the server removes him.
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
