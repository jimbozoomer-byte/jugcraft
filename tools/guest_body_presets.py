"""Build the interchangeable, unadorned Jugcraft wardrobe fitting forms."""
import argparse
import json
from dataclasses import asdict, dataclass
from pathlib import Path

import numpy as np
from PIL import Image

from guest_body_fit import body, fitting_joints, write_blockbench
from guest_portrait_models import Rig, SIZE, rectangle, unit


@dataclass(frozen=True)
class Fit:
    bust: str
    seat: str
    legs: str = "standard"
    waist: str = "standard"

    @property
    def key(self):
        suffix = [self.legs] if self.legs != "standard" else []
        if self.waist != "standard":
            suffix.append(self.waist)
        return "_".join([self.bust, self.seat, *suffix])


BASE = Fit("medium", "medium")
PRESETS = (
    Fit("small", "small"), Fit("small", "athletic"), Fit("small", "full"),
    BASE, Fit("medium", "athletic"), Fit("medium", "full"), Fit("medium", "full", "power"),
    Fit("large", "medium"), Fit("large", "athletic", "thick"), Fit("large", "full", "thick"),
    Fit("large", "compact", waist="lean"), Fit("small", "medium"), Fit("large", "athletic"),
)
SKIN = (232, 198, 181)
# Each limb and its opposite have separate paint space. Units are atlas pixels.
CHARTS = {
    ("body", "skin"): (4, 4, 116, 62), ("body", "top"): (4, 74, 228, 163),
    ("body", "pants"): (4, 245, 228, 132),
    ("left_leg", "pants"): (242, 4, 128, 262), ("right_leg", "pants"): (378, 4, 128, 262),
    ("left_arm", "skin"): (242, 274, 128, 153), ("right_arm", "skin"): (378, 274, 128, 153),
}


def ease(t):
    t = np.clip(t, 0, 1)
    return t * t * (3 - 2 * t)


class FittingForm(Rig):
    def __init__(self):
        super().__init__("wardrobe_form")
        self.palm_starts = {}

    def grid(self, bone, mat, rings, uv=None, caps=True):
        if (bone, mat) not in CHARTS:
            return
        rings = np.array(rings, dtype=float)
        if (bone, mat) == ("body", "top"):
            rings[:, :, 1] += .085 * ease((rings[:, :, 1] - 7.75) / .28)
        vertices = self.parts[bone]["vertices"]
        start = len(vertices)
        # Close the ground-facing soles even after the old cuffs have been removed.
        super().grid(bone, mat, rings, uv, caps=True)
        ox, oy, w, h = rectangle(mat)
        left, top, width, height = CHARTS[bone, mat]
        rows, sides, _ = rings.shape
        surface_end = start + rows * (sides + 1)
        for index in range(start, len(vertices)):
            v = vertices[index]
            u = (v[3] * SIZE - ox - .5) / (w - 1)
            t = (v[4] * SIZE - oy - .5) / (h - 1)
            # Separate the two cap disks from the main unwrap and other surfaces.
            if index >= surface_end:
                chart = list(CHARTS).index((bone, mat))
                cap = int(index >= surface_end + sides + 1)
                x, y, cw, ch = 4 + chart * 32, 387 + 32 * cap, 27, 27
            else:
                x, y, cw, ch = left, top, width, height
            v[3:5] = [round((x + .5 + (cw-1)*u) / SIZE, 6),
                      round((y + .5 + (ch-1)*t) / SIZE, 6)]
        if bone.endswith("_arm"):
            self.palm_starts[bone] = len(vertices)

    def unwrap_palms(self):
        # The source palm is ten outline loops, including duplicates on the two cap edges.
        # Pull apart the two cap UVs before mapping the bevel so all three can be painted.
        for side, (bone, start) in enumerate(self.palm_starts.items()):
            part = self.parts[bone]
            verts = part["vertices"]
            points = np.array(verts[start:])[:, :3]
            count = len(points) // 10
            for k, v in enumerate(verts[start:]):
                ring, corner = divmod(k, count)
                v[3:5] = [(246 + side*132 + 110*corner/count)/SIZE, (434 + 27*ring/9)/SIZE]
            for ring in (0, 9):
                first = start + ring*count
                copies = len(verts)
                face = points[ring*count:(ring+1)*count]
                xy = (face[:, :2] - face[:, :2].min(axis=0)) / np.ptp(face[:, :2], axis=0)
                for i, (u, v) in enumerate(xy):
                    vertex = list(verts[first+i])
                    vertex[3:5] = [(246 + side*132 + (ring//9)*64 + 46*u)/SIZE, (468 + 40*v)/SIZE]
                    verts.append(vertex)
                for polygon in part["faces"]:
                    if all(first <= i < first+count for i in polygon):
                        polygon[:] = [copies+i-first for i in polygon]
            # Duplicate seam vertices only where the last quad wraps back to corner zero.
            for ring in range(10):
                vertex = list(verts[start + ring*count])
                vertex[3] = (246 + side*132 + 110)/SIZE
                seam = len(verts)
                verts.append(vertex)
                for f in part["faces"]:
                    if start + ring*count in f and any(
                            start <= i < start + 10*count and (i-start)%count == count-1 for i in f):
                        f[:] = [seam if i == start + ring*count else i for i in f]


def bell(y, center, spread):
    return np.exp(-((y - center)/spread)**2)


def reshape(points, bone, fit):
    if bone != "body" and not bone.endswith("_leg"):
        return points
    x, y, z = points.T
    change_x = np.zeros_like(x)
    change_z = np.zeros_like(z)
    lateral = np.tanh(x/2.2)
    if bone == "body":
        # Shape the front panel independently of the shoulder socket and back.
        bust = {"small": -.32, "medium": 0., "large": .32}[fit.bust]
        chest = bell(y, 3.55, 2.08)
        change_z += bust * chest * np.minimum(z, 0) * ease((y-1)/1.15)
        change_x += bust * .85 * chest * lateral * ease((y-.85)/1.3)
        if fit.waist == "lean":
            waist = bell(y, 7.05, 1.62)
            change_x -= .37 * waist * lateral
            change_z -= .12 * waist * z
    # One hip field across both legs and the pelvis maintains the rest-pose seam.
    # Athletic seats crest high and tuck under earlier. Full seats carry volume lower.
    profiles = {"small": (-.62, -1.02, 11.85, 2.7), "medium": (0., 0., 12., 2.6),
                "compact": (-.37, -.48, 12.2, 2.55), "athletic": (.25, .84, 11.20, 1.95),
                "full": (.71, 1.44, 12.70, 3.05)}
    breadth, projection, peak, spread = profiles[fit.seat]
    hip = bell(y, peak, spread) * ease((y-7.9)/1.75)
    change_x += breadth * hip * lateral
    change_z += projection * hip * ease((z+.1)/3.0)
    if fit.seat == "athletic":
        change_z -= .30 * bell(y, 14.65, 1.25) * ease(z/2.9)
    if bone.endswith("_leg"):
        thickness = {"standard": 0., "thick": .81, "power": 1.05}[fit.legs]
        if fit.waist == "lean":
            thickness = -.33
        thigh = bell(y, 14.9, 2.36)
        knee = bell(y, 16.05, 1.62)
        outside = ease((abs(x)-.5)/3.2)
        change_x += thickness * lateral * outside * thigh
        change_z += thickness * (.17*z*thigh - .36*knee*ease(-z/2.65))
        if fit.legs == "power":
            change_x += .33 * lateral * outside * bell(y, 16.28, 1.25)
            change_z -= .45 * bell(y, 15.45, 1.7) * ease(-z/2.7)
    return points + np.column_stack((change_x, np.zeros_like(x), change_z))


def preset_model(base, fit):
    parts = []
    for part in base.parts.values():
        p = dict(part)
        if part["vertices"]:
            vertices = np.array(part["vertices"], dtype=float)
            pivot = np.array(part["pose"])
            world = vertices[:, :3] + pivot
            if fit != BASE:
                def morph(pos):
                    return reshape(pos, part["name"], fit)
                delta = .001
                jacobian = np.stack([(morph(world+a)-morph(world-a))/(2*delta)
                                     for a in delta*np.eye(3)], axis=-1)
                if np.min(np.linalg.det(jacobian)) <= 0:
                    raise ValueError(f"Fold in {fit.key}/{part['name']}")
                normal = np.linalg.solve(jacobian.swapaxes(1, 2), vertices[:, 5:, None])[:, :, 0]
                vertices[:, 5:] = unit(normal).round(5)
                vertices[:, :3] = (morph(world)-pivot).round(4)
            p["vertices"] = vertices.tolist()
            p["bounds"] = [*vertices[:, :3].min(axis=0), *vertices[:, :3].max(axis=0)]
        parts.append(p)
    return {"parts": parts}


def export(output):
    form = FittingForm()
    fitting_joints(form)
    body(form)
    form.unwrap_palms()
    output.mkdir(exist_ok=True, parents=True)
    texture = Image.new("RGB", (SIZE, SIZE), SKIN)
    texture.save(output/"mannequin.png")
    manifest = {"reference": BASE.key, "axes": ["bust", "seat", "legs", "waist"],
                "texture": "mannequin.png", "presets": []}
    for fit in PRESETS:
        model = preset_model(form, fit)
        folder = output / fit.key
        folder.mkdir(exist_ok=True)
        mesh = folder / (fit.key + ".json")
        editable = folder / (fit.key + ".bbmodel")
        mesh.write_text(json.dumps(model, separators=(",", ":")), encoding="utf8")
        write_blockbench(model, texture, editable, name=fit.key.replace("_"," ").title(),
                         texture_name="mannequin.png", keep_sockets=True)
        manifest["presets"].append(dict(id=fit.key, **asdict(fit), mesh=mesh.relative_to(output).as_posix(),
                                        blockbench=editable.relative_to(output).as_posix()))
    (output/"presets.json").write_text(json.dumps(manifest, indent=2)+"\n", encoding="utf8")


if __name__ == "__main__":
    cli = argparse.ArgumentParser(description=__doc__)
    cli.add_argument("output", type=Path)
    export(cli.parse_args().output)
