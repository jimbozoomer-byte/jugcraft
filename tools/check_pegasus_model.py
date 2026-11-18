#!/usr/bin/env python3
"""Check that the Pegasus's Java model matches its editable Blockbench sources (docs/features/pegasus.md).

    python3 tools/check_pegasus_model.py

Offline, standard library only. It is NOT a Java compile, a Minecraft run or a look at the art. It checks:

- art/pegasus/pegasus.bbmodel's 12 vanilla bones and cubes are vanilla 26.3's AbstractEquineModel.createBodyMesh
  (CubeDeformation.NONE) numbers, which PegasusModel builds at run time;
- every wing part in PegasusModel.java's GENERATED block (parent, pivot, rotation, each cube's from, size,
  texOffs and mirror) converts to exactly the matching group and elements of pegasus.bbmodel;
- PegasusSaddleModel.java's bones are vanilla's PartPoses and its GENERATED saddle parts match pegasus_saddle.bbmodel;
- the texture sizes in Java match each .bbmodel's resolution, and the shipped textures are byte for byte the PNGs
  embedded in the .bbmodels;
- PegasusFlight.java's wing keyframes and stroke constants equal art/pegasus/pegasus_animation.json.

Conversion (Minecraft model space -> Blockbench, the same as the art tools): a root part's pivot p becomes the group
origin (-p.x, 24 - p.y, p.z); a child's origin is its parent's plus (-o.x, -o.y, o.z); rotations (radians) become
degrees (-x, -y, z); a cube at local from (x, y, z), size (w, h, d) becomes from = origin + (-(x+w), -(y+h), z),
to = origin + (-x, -y, z+d); texOffs is uv_offset and .mirror() is mirror_uv.
"""
import base64
import json
import math
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ART = ROOT / "art/pegasus"
CLIENT = ROOT / "src/client/java/io/github/jimbozoomer/jugcraft/client/pegasus"
FLIGHT = ROOT / "src/main/java/io/github/jimbozoomer/jugcraft/pegasus/PegasusFlight.java"
TEXTURES = ROOT / "src/main/resources/assets/jugcraft/textures/entity/pegasus"
EPS = 1e-4

# Vanilla 26.3 createBodyMesh(CubeDeformation.NONE): part -> (parent, offset, rotation, [(texOffs, mirror, from, size, inflate)])
VANILLA = {
    "body": (None, (0, 11, 5), (0, 0, 0), [((0, 32), False, (-5, -8, -17), (10, 10, 22), 0.05)]),
    "tail": ("body", (0, -5, 2), (0.5235988, 0, 0), [((42, 36), False, (-1.5, 0, 0), (3, 14, 4), 0.0)]),
    "head_parts": (None, (0, 4, -12), (0.5235988, 0, 0), [((0, 35), False, (-2.05, -6, -2), (4, 12, 7), 0.0)]),
    "head": ("head_parts", (0, 0, 0), (0, 0, 0), [((0, 13), False, (-3, -11, -2), (6, 5, 7), 0.0)]),
    "left_ear": ("head", (0, 0, 0), (0, 0, 0), [((19, 16), False, (0.55, -13, 4), (2, 3, 1), -0.001)]),
    "right_ear": ("head", (0, 0, 0), (0, 0, 0), [((19, 16), False, (-2.55, -13, 4), (2, 3, 1), -0.001)]),
    "mane": ("head_parts", (0, 0, 0), (0, 0, 0), [((56, 36), False, (-1, -11, 5.01), (2, 16, 2), 0.0)]),
    "upper_mouth": ("head_parts", (0, 0, 0), (0, 0, 0), [((0, 25), False, (-2, -11, -7), (4, 5, 5), 0.0)]),
    "left_hind_leg": (None, (4, 14, 7), (0, 0, 0), [((48, 21), True, (-3, -1.01, -1), (4, 11, 4), 0.0)]),
    "right_hind_leg": (None, (-4, 14, 7), (0, 0, 0), [((48, 21), False, (-1, -1.01, -1), (4, 11, 4), 0.0)]),
    "left_front_leg": (None, (4, 14, -10), (0, 0, 0), [((48, 21), True, (-3, -1.01, -1.9), (4, 11, 4), 0.0)]),
    "right_front_leg": (None, (-4, 14, -10), (0, 0, 0), [((48, 21), False, (-1, -1.01, -1.9), (4, 11, 4), 0.0)]),
}
SADDLE_BONES = ("body", "tail", "head_parts", "left_hind_leg", "right_hind_leg", "left_front_leg", "right_front_leg")

NUM = r"-?\d+(?:\.\d+)?F?"
PART_RE = re.compile(r'(?:PartDefinition\s+(\w+)\s*=\s*)?(\w+)\.addOrReplaceChild\("(\w+)",\s*CubeListBuilder\.create\(\)'
                     r'((?:\s*\.(?:mirror\(\)|texOffs\(\d+,\s*\d+\)\.addBox\([^)]*\)(?:\s*,\s*new CubeDeformation\([^)]*\))?\)?))*)'
                     r'\s*,\s*PartPose\.(offset|offsetAndRotation)\(([^)]*)\)\);')
CUBE_RE = re.compile(r'\.texOffs\((\d+),\s*(\d+)\)\.addBox\(([^)]*?)(?:,\s*new CubeDeformation\(([^)]*)\))?\)')


def num(s):
    return float(s.strip().rstrip("F"))


def generated(path, marker):
    text = path.read_text(encoding="utf-8")
    m = re.search(rf"// BEGIN GENERATED {marker}[^\n]*\n(.*?)// END GENERATED {marker}", text, re.S)
    if not m:
        raise SystemExit(f"{path.name}: no GENERATED {marker} block")
    return text, m.group(1)


def parse_parts(block):
    """Java part definitions -> [(name, parent variable, offset, rotation, [cubes])]."""
    parts = []
    for var, parent, name, body, kind, args in PART_RE.findall(block):
        values = [num(a) for a in args.split(",")]
        offset, rotation = tuple(values[:3]), tuple(values[3:6]) if kind == "offsetAndRotation" else (0.0, 0.0, 0.0)
        mirror = ".mirror()" in body
        cubes = []
        for u, v, box, grow in CUBE_RE.findall(body):
            b = [num(x) for x in box.split(",")]
            cubes.append(((int(u), int(v)), mirror, tuple(b[:3]), tuple(b[3:6]), num(grow) if grow else 0.0))
        parts.append((name, var, parent, offset, rotation, cubes))
    return parts


def bb_groups(model):
    groups, elements = {}, {e["uuid"]: e for e in model["elements"]}

    def walk(node, parent):
        kids = [elements[c] for c in node.get("children", []) if isinstance(c, str)]
        groups[node["name"]] = (node, parent, kids)
        for c in node.get("children", []):
            if isinstance(c, dict):
                walk(c, node["name"])
    for top in model["outliner"]:
        walk(top, None)
    return groups


def close(a, b):
    return all(abs(x - y) < EPS for x, y in zip(a, b))


def compare_part(errors, groups, name, parent, offset, rotation, cubes, origin_of):
    if name not in groups:
        errors.append(f"{name}: no such group in the .bbmodel")
        return
    node, bb_parent, elements = groups[name]
    if bb_parent != parent:
        errors.append(f"{name}: parent {bb_parent} in the .bbmodel, {parent} in Java")
    po = origin_of(parent) if parent else (0.0, 24.0, 0.0)
    sign = (-1, -1, 1)
    expect_origin = tuple(po[i] + sign[i] * offset[i] for i in range(3)) if parent else (-offset[0], 24 - offset[1], offset[2])
    if not close(node["origin"], expect_origin):
        errors.append(f"{name}: origin {node['origin']} in the .bbmodel, {expect_origin} from Java")
    expect_rot = (-math.degrees(rotation[0]), -math.degrees(rotation[1]), math.degrees(rotation[2]))
    if not all(abs(a - b) < 1e-3 for a, b in zip(node.get("rotation", [0, 0, 0]), expect_rot)):
        errors.append(f"{name}: rotation {node.get('rotation')} in the .bbmodel, {expect_rot} from Java")
    if len(elements) != len(cubes):
        errors.append(f"{name}: {len(elements)} cubes in the .bbmodel, {len(cubes)} in Java")
        return
    o = node["origin"]
    for el, (uv, mirror, frm, size, grow) in zip(elements, cubes):
        x, y, z = frm
        w, h, d = size
        f = (o[0] - (x + w), o[1] - (y + h), o[2] + z)
        t = (o[0] - x, o[1] - y, o[2] + z + d)
        if not (close(el["from"], f) and close(el["to"], t)):
            errors.append(f"{name}/{el['name']}: box {el['from']}..{el['to']} in the .bbmodel, {f}..{t} from Java")
        if list(el.get("uv_offset", [])) != list(uv):
            errors.append(f"{name}/{el['name']}: uv_offset {el.get('uv_offset')} in the .bbmodel, texOffs {uv} in Java")
        if bool(el.get("mirror_uv")) != mirror:
            errors.append(f"{name}/{el['name']}: mirror_uv {el.get('mirror_uv')} in the .bbmodel, mirror {mirror} in Java")
        if abs(float(el.get("inflate", 0) or 0) - grow) > EPS:
            errors.append(f"{name}/{el['name']}: inflate {el.get('inflate')} in the .bbmodel, {grow} in Java")


def check_model(errors):
    model = json.loads((ART / "pegasus.bbmodel").read_text(encoding="utf-8"))
    groups = bb_groups(model)
    origin = lambda n: tuple(groups[n][0]["origin"])
    for name, (parent, off, rot, cubes) in VANILLA.items():
        compare_part(errors, groups, name, parent, off, rot, cubes, origin)
    text, block = generated(CLIENT / "PegasusModel.java", "WINGS")
    variables = {"body": "body"}
    parts = parse_parts(block)
    if len(parts) != 6:
        errors.append(f"PegasusModel: {len(parts)} wing parts parsed, expected 6")
    for name, var, parent_var, off, rot, cubes in parts:
        variables[var] = name
        compare_part(errors, groups, name, variables.get(parent_var, parent_var), off, rot, cubes, origin)
    known = set(VANILLA) | {p[0] for p in parts}
    for extra in set(groups) - known:
        errors.append(f"pegasus.bbmodel: group {extra} is not in Java")
    width, height = (int(re.search(rf"{k} = (\d+);", text).group(1)) for k in ("TEXTURE_WIDTH", "TEXTURE_HEIGHT"))
    if [width, height] != [model["resolution"]["width"], model["resolution"]["height"]]:
        errors.append(f"PegasusModel texture {width}x{height}, pegasus.bbmodel {model['resolution']}")
    if "createBodyMesh(CubeDeformation.NONE)" not in text or "MeshTransformer.scaling(ADULT_SCALE)" not in text:
        errors.append("PegasusModel must build vanilla's createBodyMesh(CubeDeformation.NONE) and scale it once")
    return model


def check_saddle(errors):
    model = json.loads((ART / "pegasus_saddle.bbmodel").read_text(encoding="utf-8"))
    groups = bb_groups(model)
    text, block = generated(CLIENT / "PegasusSaddleModel.java", "SADDLE")
    head = text.split("// BEGIN GENERATED SADDLE")[0]
    bones = {name: (parent, off, rot) for name, var, parent, off, rot, cubes in
             parse_parts(head.replace("root.addOrReplaceChild", "root.addOrReplaceChild"))}
    for name in SADDLE_BONES:
        parent, off, rot, _ = VANILLA[name]
        if name not in bones:
            errors.append(f"PegasusSaddleModel: bone {name} missing")
            continue
        if not (close(bones[name][1], off) and close(bones[name][2], rot)):
            errors.append(f"PegasusSaddleModel: bone {name} pose {bones[name][1:]} is not vanilla's {(off, rot)}")
    vanilla_origin = {}
    for name in ("body", "head_parts"):
        parent, off, rot, _ = VANILLA[name]
        vanilla_origin[name] = (-off[0], 24 - off[1], off[2])
    for name, var, parent_var, off, rot, cubes in parse_parts(block):
        parent = {"body": "body", "headParts": "head_parts"}.get(parent_var, parent_var)
        compare_part(errors, groups, name, parent, off, rot, cubes, lambda n: vanilla_origin[n])
    if "LayerDefinition.create(mesh, 64, 64)" not in text or model["resolution"] != {"width": 64, "height": 64}:
        errors.append("the saddle layer and pegasus_saddle.bbmodel must both be 64x64")
    return model


def check_textures(errors, model, saddle):
    for name, m in (("pegasus", model), ("pegasus_saddle", saddle)):
        embedded = base64.b64decode(m["textures"][0]["source"].split(",", 1)[1])
        shipped = (TEXTURES / f"{name}.png").read_bytes()
        if embedded != shipped:
            errors.append(f"textures/entity/pegasus/{name}.png differs from the PNG embedded in {name}.bbmodel")


def check_animation(errors):
    anim = json.loads((ART / "pegasus_animation.json").read_text(encoding="utf-8"))
    java = FLIGHT.read_text(encoding="utf-8")

    def array(name):
        m = re.search(rf"static final double\[\] {name} = \{{([^}}]*)\}};", java)
        return [float(v) for v in m.group(1).split(",")] if m else None

    def matrix(name):
        m = re.search(rf"static final double\[\]\[\] {name} = \{{(.*?)\}};", java, re.S)
        return [[float(v) for v in row.split(",")] for row in re.findall(r"\{([^{}]*)\}", m.group(1))] if m else None

    def scalar(name):
        m = re.search(rf"static final double {name} = ({NUM});", java)
        return num(m.group(1)) if m else None

    pairs = [("FOLD_SPAN", array("FOLD_SPAN"), anim["fold_span"]), ("FOLD_CHORD", array("FOLD_CHORD"), anim["fold_chord"]),
             ("RAISE_SPAN", array("RAISE_SPAN"), anim["raise_span"]), ("RAISE_CHORD", array("RAISE_CHORD"), anim["raise_chord"]),
             ("SPREAD_EULER", array("SPREAD_EULER"), anim["spread_euler"]), ("ELBOW", matrix("ELBOW"), anim["elbow"]),
             ("HAND", matrix("HAND"), anim["hand"])]
    for key in ("flap_shoulder", "flap_elbow", "flap_hand", "lag_elbow", "lag_hand", "sweep"):
        pairs.append((key.upper(), scalar(key.upper()), anim[key]))
    for name, got, want in pairs:
        if got is None:
            errors.append(f"PegasusFlight.{name} not found")
        elif json.dumps(got) != json.dumps(want) and not (
                isinstance(want, float) and abs(got - want) < 1e-12):
            errors.append(f"PegasusFlight.{name} = {got}, pegasus_animation.json has {want}")


def main():
    errors = []
    model = check_model(errors)
    saddle = check_saddle(errors)
    check_textures(errors, model, saddle)
    check_animation(errors)
    for e in errors:
        print("ERROR", e)
    print(f"check_pegasus_model: {len(model['elements'])} model cubes, {len(saddle['elements'])} saddle cubes, "
          f"{len(errors)} problem(s)")
    return 1 if errors else 0


if __name__ == "__main__":
    sys.exit(main())
