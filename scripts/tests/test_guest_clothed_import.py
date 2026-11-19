"""Reze uses the exact owner-approved, clothed Blender import in the normal wardrobe."""
import hashlib
import json
import struct
import unittest
from pathlib import Path

from PIL import Image


def geometry_signature(model):
    """Canonicalize joint-local positions so splitting the hair onto a head child is allowed."""
    joints = {"$mesh": (0, 0, 0)}
    faces = []
    for part in model["parts"]:
        name = part["name"]
        pivot = tuple(a + b for a, b in zip(joints[part["parent"]], part["pose"]))
        joints[name] = pivot
        if name.startswith(("spring_", "harvest_", "winter_")):
            continue
        group = ("head" if name == "hair_base" else name).encode()
        vertices = []
        for vertex in part["vertices"]:
            values = [vertex[i] + pivot[i] for i in range(3)] + vertex[3:]
            vertices.append(struct.pack("<8i", *(round(n * 1_000_000) for n in values)))
        faces.extend(group + b"".join(vertices[i] for i in face) for face in part["faces"])
    return hashlib.sha256(b"".join(sorted(faces))).hexdigest()


def material_signature(atlas):
    # Two of the nine material assignments share one of these eight source tiles.
    tiles = ((4, 4, 1012), (1028, 4, 1012), (4, 1032, 512), (524, 1032, 512),
             (1048, 1032, 288), (1348, 1032, 128), (1490, 1032, 256), (1770, 1032, 16))
    digest = hashlib.sha256()
    for x, y, side in tiles:
        digest.update(atlas.crop((x, y, x + side, y + side)).tobytes())
    return digest.hexdigest()


class ClothedImportTest(unittest.TestCase):
    def test_reze_geometry_and_materials_match_the_approved_import(self):
        assets = Path(__file__).resolve().parents[2] / "src/main/resources/assets/jugcraft"
        model = json.loads((assets / "town_models/guests/reze.json").read_text())
        self.assertEqual(geometry_signature(model),
                         "a40a77039e66c0aa005c1e06efb2eee90b0ce870a8110a1f42449417cfb54a66")
        with Image.open(assets / "textures/entity/town_guests/guest_reze.png") as texture:
            self.assertEqual(material_signature(texture.convert("RGBA")),
                             "0432b63f461964e98f23a52b1484eefd0c775c45b5b9f1940bbfbc8296e1ff92")
