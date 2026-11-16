"""The pirate ship's art (tools/pirate_ship.py) against the crew interface it is drawn for: the ten stations of
PirateCrew.STATIONS, the fixed 6 x 16 layout, the rig file client/PirateShipRenderer reads, standing room at every
station in every pose (yards braced, sails set and furled, the canvas swayed, the flag and helm at their extremes), the
braced sails clear of the standing rigging, masts and tops, the drawn ship inside PirateWaters' clearance on the swell
and heeled (the motion read from PirateMotion), its language keys, opaque ps_ textures with no thin repeating lines, and
the quad budget."""
import json
import math
import re
import sys
import tempfile
import unittest
from pathlib import Path

import numpy as np
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))
import pirate_ship  # noqa: E402

ASSETS = ROOT / "src" / "main" / "resources" / "assets" / "jugcraft"
MENAGERIE = ROOT / "src" / "main" / "java" / "io" / "github" / "jimbozoomer" / "jugcraft" / "menagerie"
CREW = MENAGERIE / "PirateCrew.java"
WATERS = MENAGERIE / "PirateWaters.java"
MOTION = MENAGERIE / "PirateMotion.java"
# PirateWaters' clearance, ship-local blocks turned with the ship: the hull box (x within HALF_BEAM, z within
# HALF_LENGTH, down DEPTH from the waterline) and the rig box above the waterline. Read from the Java when it is in the
# tree; these literals otherwise.
WATERS_AGREED = {"HALF_BEAM": 3.0, "HALF_LENGTH": 8.0, "DEPTH": 2.0, "RIG_HALF_BEAM": 4.0, "RIG_STERN": -9.5,
                 "RIG_BOW": 11.75, "MAST": 13.0, "KEEL": 1.75, "WATERLINE": 0.875}
# The stations as agreed with the crew interface (role, seat, facing, arc, muzzle), ship-local blocks, unrotated.
AGREED = [
    ("HELM", (0, 1, -6), 0, 0, (0, 1, -6)),
    ("LOOKOUT", (0, 9.5, 1.4), 0, 180, (0, 9.5, 1.4)),
    ("GUNNER", (2, 1, 2.5), -90, 45, (3.2, 1.6, 2.5)),
    ("GUNNER", (2, 1, -2), -90, 45, (3.2, 1.6, -2)),
    ("GUNNER", (-2, 1, 2.5), 90, 45, (-3.2, 1.6, 2.5)),
    ("GUNNER", (-2, 1, -2), 90, 45, (-3.2, 1.6, -2)),
    ("DECK", (1, 1, 5.5), 0, 0, (1, 1, 5.5)),
    ("DECK", (-1, 1, 5.5), 0, 0, (-1, 1, 5.5)),
    ("DECK", (1, 1, -3.5), 0, 0, (1, 1, -3.5)),
    ("DECK", (-1, 1, -3.5), 0, 0, (-1, 1, -3.5)),
]
RENDERER_PARTS = {"pirate_ship_hull", "pirate_ship_jib", "pirate_ship_furl_jib", "pirate_ship_flag", "pirate_ship_wheel",
                  "pirate_ship_rudder", "pirate_ship_cannon", "pirate_ship_rope"} | {
    f"pirate_ship_{kind}_{name}" for name in ("main", "main_top", "fore", "fore_top") for kind in ("yard", "sail", "furl")}


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


def motion_constants():
    """PirateMotion's swell and heel bounds (degrees, blocks), read from its Java source."""
    source = MOTION.read_text(encoding="utf-8")
    out = {}
    for name in ("ROLL", "PITCH", "HEAVE", "HEEL_MAX", "DECK_CAP"):
        m = re.search(rf"static final (?:double|float) {name} = ([-0-9.]+(?: / [0-9.]+)?)[FfDd]?;", source)
        out[name] = eval(m.group(1).rstrip("Ff"))  # a literal or a literal quotient
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
        self.assertEqual(set(pivots), RENDERER_PARTS - {"pirate_ship_hull", "pirate_ship_cannon", "pirate_ship_rope"})
        for part, pivot in pivots.items():
            self.assertEqual(len(pivot), 3, part)
            self.assertEqual(pivot, [round(v, 4) for v in pirate_ship.PIVOTS[part]])
        self.assertEqual(set(self.rig["axes"]), {"main", "main_top", "fore", "fore_top"})
        self.assertEqual(self.rig["brace_limits"], pirate_ship.BRACE_LIMITS)
        frames = {"hull"} | {f"{kind}_{name}" for name in self.rig["axes"] for kind in ("yard", "sail")}
        for rope in self.rig["ropes"]:
            for end in (rope["from"], rope["to"]):
                self.assertIn(end["frame"], frames, rope["name"])
                self.assertEqual(len(end["point"]), 3)
        self.assertEqual(len(self.rig["ropes"]), 12)
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
        # The culling box holds the whole drawn ship in every pose, ropes included.
        cache = {part: pirate_ship._points(self.quads, part) for part in pirate_ship.PARTS}
        for pose in pirate_ship.poses(2):
            pts = pirate_ship.placed_points(self.quads, pose, cache) / 16
            for k in range(3):
                self.assertTrue(culling[k] <= pts[:, k].min() and pts[:, k].max() <= culling[k + 3], k)

    def test_the_renderer_reads_this_rig(self):
        renderer = (ROOT / "src/client/java/io/github/jimbozoomer/jugcraft/client/PirateShipRenderer.java").read_text(encoding="utf-8")
        for key in ("pivots", "axes", "brace_limits", "ropes", "cannons", "culling"):
            self.assertIn(f'"{key}"', renderer)
        for part in RENDERER_PARTS - {f"pirate_ship_{kind}_{name}" for name in ("main", "main_top", "fore", "fore_top")
                                      for kind in ("yard", "sail", "furl")}:
            self.assertIn(f'"{part}"', renderer)
        for kind in ("yard", "sail", "furl"):
            self.assertIn(f'"pirate_ship_{kind}_"', renderer)
        self.assertIn("SHOWN = %sF" % pirate_ship.SHOWN, renderer)
        self.assertIn("FURLED_CLEW_Y = -3.0F", renderer)

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
        # The waist rail at y 2.0; the sheer rises aft (2.5, 2.75) and forward (2.375, 2.625).
        self.assertEqual(max(b[1][1] for b in pirate_ship.bulwarks() if -72 <= b[0][2] and b[1][2] <= 72 and len(b) == 3
                             and b[0][1] == pirate_ship.DECK_PX), 32)
        self.assertEqual(sorted({pirate_ship.rail_height(z0, z1) for z0, z1, w in pirate_ship.SLICES}), [32, 38, 40, 42, 44])
        # Each mast with its topmast (stepped just aft of the lower mast's head) tops out at its agreed height.
        tops = {name: max(b[1][1] for b in pirate_ship.masts() if len(b) == 3 and b[0][2] < z + 14 and b[1][2] > z - 14
                          and b[0][0] < 0 < b[1][0] and b[1][0] - b[0][0] > 2) for name, z in (("main", 16), ("fore", 80))}
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
        for name in pirate_ship.RETIRED:
            self.assertFalse((ASSETS / "textures" / "block" / f"{name}.png").exists(), name)
        # The timber has no thin repeating lines: in each plank texture at most two dark lines run the tile's width (the
        # two boards' soft seams), none across the boards (no butt joints repeating every block).
        for name in (pirate_ship.HULL, pirate_ship.HULL_SIDE, pirate_ship.BULWARK, pirate_ship.DECK, pirate_ship.WALE):
            with Image.open(ASSETS / "textures" / "block" / f"{name}.png") as image:
                rgb = np.asarray(image.convert("RGB"), dtype=float)
                if name == pirate_ship.DECK:
                    rgb = rgb.transpose(1, 0, 2)  # its boards run down the texture
                luma = rgb @ np.array([0.299, 0.587, 0.114])
                median = float(np.median(luma))
                rows = 16 - (3 if name == pirate_ship.HULL_SIDE else 0)  # the wet boot-top is one broad band, not lines
                along = [k for k in range(rows) if np.ptp(luma[k]) < 1 and luma[k].mean() < median - 8]
                across = [k for k in range(16) if np.ptp(luma[:, k]) < 1 and luma[:, k].mean() < median - 8]
                self.assertLessEqual(len(along), 2, (name, along))
                self.assertEqual(across, [], (name, across))
        for name in used:
            self.assertTrue(name.startswith("ps_"), name)
            with Image.open(ASSETS / "textures" / "block" / f"{name}.png") as image:
                self.assertEqual(image.size, (16, 16), name)
                self.assertEqual(image.convert("RGBA").getextrema()[3], (255, 255), name)

    def test_braced_sails_keep_clear(self):
        """Within its brace limit, every square sail's yard, canvas and bundle, swayed either way, set or furled, keeps
        clear of the shrouds, stays, masts and tops; past it (5 degrees more) at least one does not, so the limits are
        what the rigging allows, not arbitrary."""
        self.assertEqual(pirate_ship.brace_clear(self.quads), [])
        limits = dict(pirate_ship.BRACE_LIMITS)
        try:
            for name in limits:
                pirate_ship.BRACE_LIMITS[name] = min(pirate_ship.BRACE_MAX, limits[name] + 10.0)
            self.assertNotEqual(pirate_ship.brace_clear(self.quads, steps=4), [])
        finally:
            pirate_ship.BRACE_LIMITS.update(limits)

    def test_motion_matches_the_java(self):
        """The checks and previews use PirateMotion's own bounds: the most roll (the swell's and the heel), pitch and heave."""
        m = motion_constants()
        self.assertAlmostEqual(pirate_ship.ROLL, m["ROLL"] + m["HEEL_MAX"], places=6)
        self.assertAlmostEqual(pirate_ship.PITCH, m["PITCH"], places=6)
        self.assertAlmostEqual(pirate_ship.HEAVE, m["HEAVE"] * 16, places=6)
        self.assertEqual(pirate_ship.SWAY, 3.0)

    def test_the_drawn_deck_stays_on_the_walkable_deck(self):
        """Over every deck square, with the most roll, pitch and heave, the drawn deck parts from the level walkable deck
        by no more than PirateMotion.DECK_CAP (1.5 pixels)."""
        m = motion_constants()
        worst = 0.0
        for kz in (-5.5, -2.0, 2.0, 5.5):
            for dx in (-2.5, 2.5):
                for dz in (-2.5, 2.5):
                    for roll in (-pirate_ship.ROLL, pirate_ship.ROLL):
                        for pitch in (-pirate_ship.PITCH, pirate_ship.PITCH):
                            for heave in (-pirate_ship.HEAVE, pirate_ship.HEAVE):
                                mat = pirate_ship._move(0, heave, 0) @ pirate_ship._rot("z", roll) @ pirate_ship._rot("x", pitch)
                                y = pirate_ship._apply(mat, (dx * 16, 16.0, (kz + dz) * 16))[1]
                                worst = max(worst, abs(y - 16.0) / 16)
        self.assertLessEqual(worst, m["DECK_CAP"] + 1e-9, worst)

    def test_drawn_ship_stays_inside_the_clearance_boxes(self):
        """Every drawn vertex and rope end, in every swept pose, on the swell and heeled, lies in the water and air
        PirateWaters checks: from the waterline row up, inside the rig's box (x within RIG_HALF_BEAM, z RIG_STERN..RIG_BOW,
        up to the top of the MAST rows); under the waterline row, inside the hull's 6 x 16 box down DEPTH rows; and never
        deeper than KEEL under the waterline. The waterline row spans y -WATERLINE..1-WATERLINE about the model's origin."""
        c = waters_constants()
        row_bottom, row_top = -c["WATERLINE"], 1 - c["WATERLINE"]
        cache = {part: pirate_ship._points(self.quads, part) for part in pirate_ship.PARTS}
        every = np.concatenate([pirate_ship.placed_points(self.quads, pose, cache) for pose in pirate_ship.poses(2)])
        homogeneous = np.concatenate([every, np.ones((len(every), 1))], 1)
        deepest = 0.0
        for roll in (-pirate_ship.ROLL, 0.0, pirate_ship.ROLL):
            for pitch in (-pirate_ship.PITCH, 0.0, pirate_ship.PITCH):
                for heave in (-pirate_ship.HEAVE, pirate_ship.HEAVE):
                    mat = pirate_ship._move(0, heave, 0) @ pirate_ship._rot("z", roll) @ pirate_ship._rot("x", pitch)
                    p = (homogeneous @ mat.T)[:, :3] / 16
                    deepest = max(deepest, float(-p[:, 1].min()))
                    rig = p[p[:, 1] >= row_bottom]
                    self.assertLessEqual(np.abs(rig[:, 0]).max(), c["RIG_HALF_BEAM"])
                    self.assertGreaterEqual(rig[:, 2].min(), c["RIG_STERN"])
                    self.assertLessEqual(rig[:, 2].max(), c["RIG_BOW"])
                    self.assertLessEqual(rig[:, 1].max(), row_top + c["MAST"])
                    hull = p[p[:, 1] < row_bottom]
                    if len(hull):
                        self.assertLessEqual(np.abs(hull[:, 0]).max(), c["HALF_BEAM"])
                        self.assertLessEqual(np.abs(hull[:, 2]).max(), c["HALF_LENGTH"])
                        self.assertGreaterEqual(hull[:, 1].min(), row_bottom - c["DEPTH"])
        self.assertLessEqual(deepest, c["KEEL"], deepest)
        # The rig file's culling box holds all of it, at rest.
        culling = self.rig["culling"]
        self.assertTrue(all(culling[k] <= every[:, k].min() / 16 and every[:, k].max() / 16 <= culling[k + 3] for k in range(3)))

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
        """Quads drawn a frame with the sails set (the most), ropes included: under 6,000 (at most three ships are afloat)."""
        pose = pirate_ship.frames(brace=10.0)
        drawn = sum(len(self.quads[part]) * len(mats) for part, mats in pose.items())
        drawn += sum(len(pirate_ship.rope_segments(*pirate_ship.rope_ends(pose, spec))) for spec in pirate_ship.ropes()) * len(
            self.quads["pirate_ship_rope"])
        self.assertLess(drawn, 6000, drawn)


if __name__ == "__main__":
    unittest.main()
