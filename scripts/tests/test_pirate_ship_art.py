"""The pirate ship's art (tools/pirate_ship.py) against the crew interface it is drawn for: the ten stations of
PirateCrew.STATIONS, the fixed 6 x 16 layout, the rig file client/PirateShipRenderer reads, standing room at every
station, the drawn ship inside PirateWaters' clearance boxes on the swell, its language keys, opaque ps_ textures and
the quad budget."""
import json
import math
import re
import sys
import tempfile
import unittest
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))
import pirate_ship  # noqa: E402

ASSETS = ROOT / "src" / "main" / "resources" / "assets" / "jugcraft"
MENAGERIE = ROOT / "src" / "main" / "java" / "io" / "github" / "jimbozoomer" / "jugcraft" / "menagerie"
CREW = MENAGERIE / "PirateCrew.java"
WATERS = MENAGERIE / "PirateWaters.java"
# PirateWaters' clearance, ship-local blocks turned with the ship: the hull box (x within HALF_BEAM, z within
# HALF_LENGTH, down DEPTH from the waterline) and the rig box above the waterline. Read from the Java when it is in the
# tree; these literals otherwise.
WATERS_AGREED = {"HALF_BEAM": 3.0, "HALF_LENGTH": 8.0, "DEPTH": 2.0, "RIG_HALF_BEAM": 4.0, "RIG_STERN": -9.5,
                 "RIG_BOW": 11.75, "RIG_TOP": 13.0}
# The swell rolls the ship's exactly 6-wide sides 1.2 degrees, which carries the hull's waterline 0.00066 of a block
# past x 3 (a hundredth of a pixel): the hull box is checked to within this, every other bound exactly.
SWELL_TOLERANCE = 0.001
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


def waters_constants():
    """PirateWaters' clearance constants: each from the Java source when it declares it, else the agreed literal."""
    out = dict(WATERS_AGREED)
    if WATERS.is_file():
        source = WATERS.read_text(encoding="utf-8")
        for name in out:
            m = re.search(rf"static final (?:double|float|int) {name} = (-?\d+(?:\.\d+)?)[FfDd]?;", source)
            if m:
                out[name] = float(m.group(1))
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

    def test_drawn_ship_stays_inside_the_clearance_boxes(self):
        c = waters_constants()
        (alo, ahi), (blo, bhi) = (([v / 16 for v in lo], [v / 16 for v in hi]) for lo, hi in pirate_ship.envelope(self.quads))
        # (a) Above the waterline: inside the rig box, with every swing and the swell.
        self.assertGreaterEqual(min(alo[0], -ahi[0]), -c["RIG_HALF_BEAM"], (alo, ahi))
        self.assertGreaterEqual(alo[2], c["RIG_STERN"], alo)
        self.assertLessEqual(ahi[2], c["RIG_BOW"], ahi)
        self.assertLessEqual(ahi[1], c["RIG_TOP"], ahi)
        # (b) At or below it: inside the hull box down to DEPTH.
        self.assertLessEqual(max(-blo[0], bhi[0]), c["HALF_BEAM"] + SWELL_TOLERANCE, (blo, bhi))
        self.assertLessEqual(max(-blo[2], bhi[2]), c["HALF_LENGTH"], (blo, bhi))
        self.assertGreaterEqual(blo[1], -c["DEPTH"], blo)
        # Still water: the hull under the waterline is exactly the 6 x 16 hull.
        roll, pitch = pirate_ship.ROLL, pirate_ship.PITCH
        try:
            pirate_ship.ROLL = pirate_ship.PITCH = 0.0
            (slo, shi) = pirate_ship.envelope(self.quads)[1]
        finally:
            pirate_ship.ROLL, pirate_ship.PITCH = roll, pitch
        self.assertEqual((max(-slo[0], shi[0]) / 16, max(-slo[2], shi[2]) / 16), (c["HALF_BEAM"], c["HALF_LENGTH"]))
        # The rig file's culling box holds all of it.
        culling = self.rig["culling"]
        self.assertTrue(all(culling[k] <= min(alo[k], blo[k]) and max(ahi[k], bhi[k]) <= culling[k + 3] for k in range(3)))

    def test_language_keys_come_from_the_generator(self):
        expected = {"entity.jugcraft.pirate_ship": "Pirate Ship", "entity.jugcraft.pirate_ship_deck": "Pirate Ship",
                    "container.jugcraft.pirate_ship_hold": "Pirate Ship's Hold",
                    "message.jugcraft.pirate_ship.crewed": "The crew still hold this ship. Beat them to open the hold."}
        self.assertEqual(pirate_ship.LANG, expected)
        lang = {}
        with tempfile.TemporaryDirectory() as folder:  # write_all feeds the base language generate_material_data writes
            pirate_ship.write_all(None, Path(folder), None, lang, None)
        self.assertEqual({k: lang[k] for k in expected}, expected)
        path = ASSETS / "lang" / "en_us.json"
        text = path.read_text(encoding="utf-8")
        self.assertEqual(pirate_ship.merge_lang(text), text, "en_us.json lacks the pirate ship's keys: run "
                                                             "tools/pirate_ship.py")
        keys = list(json.loads(text))
        for key, value in expected.items():
            self.assertEqual(json.loads(text)[key], value)
            i = keys.index(key)
            self.assertTrue(keys[i - 1] < key < keys[i + 1], key)  # at its sorted place in the generated base

    def test_quad_budget(self):
        drawn = sum(len(self.quads[p]) for p, pivot, yaw in pirate_ship.placements())
        self.assertLess(drawn, 3600)


if __name__ == "__main__":
    unittest.main()
