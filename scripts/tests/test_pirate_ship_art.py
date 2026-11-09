"""The pirate ship's art (tools/pirate_ship.py) against the crew interface it is drawn for: the ten stations of
PirateCrew.STATIONS, the fixed 6 x 16 layout, the rig file client/PirateShipRenderer reads, standing room at every
station, opaque ps_ textures and the quad budget."""
import json
import math
import re
import sys
import unittest
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))
import pirate_ship  # noqa: E402

ASSETS = ROOT / "src" / "main" / "resources" / "assets" / "jugcraft"
CREW = ROOT / "src" / "main" / "java" / "io" / "github" / "jimbozoomer" / "jugcraft" / "menagerie" / "PirateCrew.java"
# The stations as agreed with the crew interface (role, seat, facing, arc, muzzle), ship-local blocks, unrotated.
AGREED = [
    ("HELM", (0, 1, -6), 0, 0, (0, 1, -6)),
    ("LOOKOUT", (0, 9.5, 1), 0, 180, (0, 9.5, 1)),
    ("GUNNER", (2, 1, 2.5), -90, 45, (3.2, 1.6, 2.5)),
    ("GUNNER", (2, 1, -2), -90, 45, (3.2, 1.6, -2)),
    ("GUNNER", (-2, 1, 2.5), 90, 45, (-3.2, 1.6, 2.5)),
    ("GUNNER", (-2, 1, -2), 90, 45, (-3.2, 1.6, -2)),
    ("DECK", (1, 1, 5.5), 0, 0, (1, 1, 5.5)),
    ("DECK", (-1, 1, 5.5), 0, 0, (-1, 1, 5.5)),
    ("DECK", (1, 1, -3.5), 0, 0, (1, 1, -3.5)),
    ("DECK", (-1, 1, -3.5), 0, 0, (-1, 1, -3.5)),
]
RENDERER_PARTS = {"pirate_ship_hull", "pirate_ship_sail_fore", "pirate_ship_sail_main", "pirate_ship_jib",
                  "pirate_ship_flag", "pirate_ship_wheel", "pirate_ship_rudder", "pirate_ship_cannon"}


def java_stations(source):
    """PirateCrew.STATIONS parsed from its Java source: [(role, seat, facing, arc, muzzle)]."""
    number = r"(-?\d+(?:\.\d+)?)"
    vec = rf"new Vec3\({number}, {number}, {number}\)"
    pattern = rf"new Station\(Role\.(\w+), {vec}, {number}F, {number}F, {vec}\)"
    out = []
    for m in re.finditer(pattern, source):
        g = [m.group(1)] + [float(v) for v in m.groups()[1:]]
        out.append((g[0], tuple(g[1:4]), g[4], g[5], tuple(g[6:9])))
    return out


def same(a, b):
    role_a, seat_a, facing_a, arc_a, muzzle_a = a
    role_b, seat_b, facing_b, arc_b, muzzle_b = b
    return role_a == role_b and all(math.isclose(x, y) for x, y in zip(seat_a + muzzle_a + (facing_a, arc_a),
                                                                        seat_b + muzzle_b + (facing_b, arc_b)))


class PirateShipArtTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.quads = json.loads((ASSETS / "pirate_ship_quads.json").read_text(encoding="utf-8"))
        cls.rig = json.loads((ASSETS / "pirate_ship_rig.json").read_text(encoding="utf-8"))

    def test_stations_mirror_the_crew_interface(self):
        self.assertEqual(len(pirate_ship.STATIONS), 10)
        for mine, agreed in zip(pirate_ship.STATIONS, AGREED):
            self.assertTrue(same(mine, agreed), (mine, agreed))
        rig = [(s["role"], tuple(s["seat"]), s["facing"], s["arc"], tuple(s["muzzle"])) for s in self.rig["stations"]]
        self.assertEqual([s["index"] for s in self.rig["stations"]], list(range(10)))
        for mine, agreed in zip(rig, AGREED):
            self.assertTrue(same(mine, agreed), (mine, agreed))
        if CREW.is_file():  # once the crew interface is on the same branch, the Java itself
            java = java_stations(CREW.read_text(encoding="utf-8"))
            self.assertEqual(len(java), 10)
            for mine, theirs in zip(pirate_ship.STATIONS, java):
                self.assertTrue(same(mine, theirs), (mine, theirs))
        self.assertEqual(self.rig["local_deck"], [-2.5, 1.0, -7.5, 2.5, 3.0, 7.5])

    def test_rig_is_in_the_renderers_schema(self):
        pivots, cannons, culling = self.rig["pivots"], self.rig["cannons"], self.rig["culling"]
        self.assertEqual(set(pivots), RENDERER_PARTS - {"pirate_ship_hull", "pirate_ship_cannon"})
        for part, pivot in pivots.items():
            self.assertEqual(len(pivot), 3, part)
            self.assertEqual(pivot, [round(v, 4) for v in pirate_ship.PIVOTS[part]])
        self.assertEqual(len(cannons), 4)
        self.assertTrue(all(len(c) == 3 for c in cannons))
        self.assertEqual(len(culling), 6)
        # Each trunnion: the gunner's z, the barrel's muzzle face at the station's muzzle when turned by -facing.
        gunners = [s for s in AGREED if s[0] == "GUNNER"]
        tip = max(v[2] for q in self.quads["pirate_ship_cannon"] for v in q["vertices"])
        self.assertAlmostEqual(tip, pirate_ship.MUZZLE)
        for trunnion, (role, seat, facing, arc, muzzle) in zip(cannons, gunners):
            x, y, z = pirate_ship.place((0, 0, tip), trunnion, facing)
            self.assertTrue(math.isclose(x / 16, muzzle[0], abs_tol=1e-6) and math.isclose(y / 16, muzzle[1])
                            and math.isclose(z / 16, muzzle[2], abs_tol=1e-6), (x, y, z, muzzle))
        # The culling box holds the whole drawn ship.
        for part, pivot, yaw in pirate_ship.placements():
            for quad in self.quads[part]:
                for vertex in quad["vertices"]:
                    p = pirate_ship.place(vertex, pivot, yaw)
                    for k in range(3):
                        self.assertTrue(culling[k] <= p[k] / 16 <= culling[k + 3], (part, p))

    def test_export_is_current_and_holds_only_parts(self):
        self.assertEqual(set(self.quads), RENDERER_PARTS)
        self.assertEqual(set(pirate_ship.PARTS), RENDERER_PARTS)
        self.assertEqual(self.quads, json.loads(json.dumps(pirate_ship.export())),
                         "pirate_ship_quads.json is stale: run tools/generate_material_data.py")
        self.assertEqual(self.rig, json.loads(json.dumps(pirate_ship.rig(self.quads))))

    def test_fixed_layout(self):
        hull = pirate_ship.hull_body()
        sides = [b for b in hull if b[1][1] == pirate_ship.DECK_PX]
        self.assertEqual(min(b[0][0] for b in sides), -48)
        self.assertEqual(max(b[1][0] for b in sides), 48)
        self.assertEqual(min(b[0][2] for b in sides), -128)
        self.assertEqual(max(b[1][2] for b in sides), 128)
        self.assertEqual(min(b[0][1] for b in hull), pirate_ship.KEEL_PX)
        self.assertEqual(max(b[1][1] for b in pirate_ship.bulwarks() if abs(b[0][2]) < 96 and len(b) == 3), 32)
        # Each mast with its topmast (stepped just aft of the lower mast's head) tops out at its agreed height.
        tops = {name: max(b[1][1] for b in pirate_ship.masts() if len(b) == 3 and b[0][2] < z + 14 and b[1][2] > z - 14
                          and b[0][0] < 0 < b[1][0]) for name, z in (("main", 16), ("fore", 80))}
        self.assertEqual(tops, {"main": 192, "fore": 160})
        ports = pirate_ship.gun_ports()
        self.assertEqual(sorted((side, (p0 + p1) / 32) for side, p0, p1 in ports),
                         [(-1, -2.0), (-1, 2.5), (1, -2.0), (1, 2.5)])
        self.assertEqual((pirate_ship.PORT_LOW, pirate_ship.PORT_HIGH), (1.4 * 16, 1.9 * 16))

    def test_every_crewman_stands_clear(self):
        self.assertEqual(pirate_ship.standing_clear(self.quads), [])
        floors = {i: pirate_ship.STATIONS[i][1][1] * 16 for i in range(10)}
        self.assertEqual(floors[1], pirate_ship.MAIN_TOP_FLOOR)
        self.assertTrue(all(floors[i] == pirate_ship.DECK_PX for i in floors if i != 1))

    def test_textures_are_fresh_and_opaque(self):
        used = {q["texture"] for part in self.quads.values() for q in part}
        self.assertEqual(used, set(pirate_ship.TEXTURES))
        for name in used:
            self.assertTrue(name.startswith("ps_"), name)
            with Image.open(ASSETS / "textures" / "block" / f"{name}.png") as image:
                self.assertEqual(image.size, (16, 16), name)
                self.assertEqual(image.convert("RGBA").getextrema()[3], (255, 255), name)

    def test_quad_budget(self):
        drawn = sum(len(self.quads[p]) for p, pivot, yaw in pirate_ship.placements())
        self.assertLess(drawn, 3600)


if __name__ == "__main__":
    unittest.main()
