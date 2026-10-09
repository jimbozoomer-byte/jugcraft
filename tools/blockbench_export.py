"""Blockbench projects (.bbmodel) of the generators' box models, for review and editing in Blockbench.

A project is written in Blockbench's free format: every box in model space with the game's own UVs (vanilla's
position mapping, kept inside the sprite as model_writer.fit_uv does) and the textures embedded, so the file opens
on its own. Boxes are grouped as the caller groups them (a multi-block prop as one group; a vehicle as one group a
moving part, each with its joint as the group's origin and its rest pose as the group's rotation).

The Python model stays the source: a changed project is not read back, so a change made in Blockbench is carried
into the generator to keep the models reproducible (docs/ART_DIRECTION.md).
"""
import base64
import io
import json
import uuid
from pathlib import Path

import model_writer

MOD = "jugcraft"
ROOT = Path(__file__).resolve().parents[1]
TEXTURES = ROOT / "src" / "main" / "resources" / "assets" / MOD / "textures" / "block"


def _uuid(*parts):
    return str(uuid.uuid5(uuid.NAMESPACE_URL, "jugcraft:blockbench:" + ":".join(str(p) for p in parts)))


def _texture_entry(name, index, draw=None):
    """A texture embedded as a data URI: the committed PNG, or `draw(name)` (a PIL image) when it is not written yet."""
    path = TEXTURES / f"{name}.png"
    if path.exists():
        data = path.read_bytes()
    else:
        buffer = io.BytesIO()
        draw(name).save(buffer, format="PNG")
        data = buffer.getvalue()
    return {"path": "", "name": f"{name}.png", "folder": "block", "namespace": MOD, "id": str(index), "particle": index == 0,
            "render_mode": "default", "visible": True, "mode": "bitmap", "saved": False, "uuid": _uuid("texture", name),
            "source": "data:image/png;base64," + base64.b64encode(data).decode("ascii")}


def cube(name, item, index, texture_index):
    """One Blockbench cube from a generator element (from, to, texture[, options])."""
    frm, to, texture, options = model_writer.unpack(item)
    rotation = options.get("rotation")
    spec = model_writer.element(frm, to, texture, rotation=rotation)
    faces = {}
    for face, entry in spec["faces"].items():
        tex = entry["texture"].lstrip("#")
        uv = entry.get("uv") or model_writer.fit_uv(model_writer.auto_uv(face, spec["from"], spec["to"]))
        faces[face] = {"uv": [round(v, 4) for v in uv], "texture": texture_index[tex]}
    out = {"name": f"{name}_{index}", "box_uv": False, "rescale": bool(rotation and len(rotation) > 3 and rotation[3]),
           "locked": False, "render_order": "default", "allow_mirror_modeling": True,
           "from": spec["from"], "to": spec["to"], "autouv": 0, "color": index % 8,
           "origin": [round(v, 4) for v in rotation[2]] if rotation else [0, 0, 0],
           "faces": faces, "type": "cube", "uuid": _uuid(name, index)}
    if rotation:
        turn = [0, 0, 0]
        turn["xyz".index(rotation[0])] = rotation[1]
        out["rotation"] = turn
    return out


def project(name, groups, draw=None):
    """A project of `groups`: (group name, origin (pixels), rotation (degrees about x, y, z) or None, elements)."""
    names = sorted({t for _, _, _, elements in groups for t in model_writer.texture_names(elements)})
    texture_index = {t: i for i, t in enumerate(names)}
    cubes, outliner = [], []
    for group, origin, rotation, elements in groups:
        children = []
        for index, item in enumerate(elements):
            box = cube(group, item, index, texture_index)
            cubes.append(box)
            children.append(box["uuid"])
        outliner.append({"name": group, "origin": [round(v, 4) for v in origin], "rotation": list(rotation or (0, 0, 0)),
                         "color": 0, "uuid": _uuid("group", name, group), "export": True, "mirror_uv": False,
                         "isOpen": True, "locked": False, "visibility": True, "autouv": 0, "children": children})
    return {"meta": {"format_version": "4.10", "model_format": "free", "box_uv": False},
            "name": name, "model_identifier": "", "visible_box": [1, 1, 0],
            "variable_placeholders": "", "variable_placeholder_buttons": [], "timeline_setups": [],
            "unhandled_root_fields": {}, "resolution": {"width": 16, "height": 16},
            "elements": cubes, "outliner": outliner,
            "textures": [_texture_entry(t, i, draw) for i, t in enumerate(names)]}


def write(path, name, groups, draw=None):
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(project(name, groups, draw), separators=(",", ":")) + "\n", encoding="utf-8")
    return path
