"""City raid approaches against the town's own blueprint (raiders/CityRaidApproach.java, docs/features/raiders.md).

Mirrors CityRaidApproach's arithmetic in Python, reading its constants from the Java source so the two cannot drift,
and checks the result against src/main/resources/data/jugcraft/town/town.json.gz: every approach is a gate or the
middle of a straight run of wall that a guard post can see, never a tower, always outside the wall on open ground;
routes round the ring never cross the wall; a failed gate falls back to a watched wall section; guard walkers fit
through every gate passage the guard vantages use. Standard library only.
"""
import base64
import gzip
import json
import math
import re
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
TOWN = ROOT / "src" / "main" / "resources" / "data" / "jugcraft" / "town" / "town.json.gz"
APPROACH = ROOT / "src" / "main" / "java" / "io" / "github" / "jimbozoomer" / "jugcraft" / "raiders" / "CityRaidApproach.java"
ROUTE = ROOT / "src" / "main" / "java" / "io" / "github" / "jimbozoomer" / "jugcraft" / "raiders" / "CityRaidRoute.java"
INSIDE = 255
AIR = {"minecraft:air", "minecraft:cave_air", "minecraft:void_air"}


def constant(source, name):
    match = re.search(r"static final (?:int|double) " + name + r" = ([0-9.]+);", source)
    if not match:
        raise AssertionError(f"{name} not found in {APPROACH.name}")
    return float(match.group(1))


class Blueprint:
    def __init__(self, path):
        data = json.load(gzip.open(path))
        self.data = data
        self.size, self.y_min, self.height = data["size"], data["y_min"], data["height"]
        self.blocks = base64.b64decode(data["blocks"])
        self.mask_bytes = base64.b64decode(data["mask"])
        self.air = [state.split("[")[0] in AIR for state in data["palette"]]

    def index(self, x, y, z):
        dy = y - self.y_min
        if x < 0 or z < 0 or x >= self.size or z >= self.size or dy < 0 or dy >= self.height:
            return 0
        i = (dy * self.size + z) * self.size + x
        return self.blocks[2 * i] | self.blocks[2 * i + 1] << 8

    def mask(self, x, z):
        if x < 0 or z < 0 or x >= self.size or z >= self.size:
            return 0
        return self.mask_bytes[z * self.size + x]

    def solid(self, x, y, z):
        """CityRaidApproach.blocks: an authored block that is not air, or the land's own ground (y <= 0)."""
        i = self.index(x, y, z)
        return y <= 0 if i == 0 else not self.air[i]


class CityRaidApproachTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        source = APPROACH.read_text(encoding="utf-8")
        cls.FORECOURT = int(constant(source, "FORECOURT"))
        cls.WALL_STANDOFF = int(constant(source, "WALL_STANDOFF"))
        cls.RING = constant(source, "RING")
        cls.WALL_PENALTY = constant(source, "WALL_PENALTY")
        cls.POST_SIGHT = constant(source, "POST_SIGHT")
        cls.SECTIONS = int(constant(source, "SECTIONS"))
        cls.STEP = float(re.search(r"SIGHT_STEP = ([0-9.]+);", source).group(1))
        cls.town = Blueprint(TOWN)
        cls.half = cls.town.size // 2
        route = ROUTE.read_text(encoding="utf-8")
        offsets = re.findall(r"new Gate\(c\.offset\((-?\d+), 0, (-?\d+)\), (-?\d+), (-?\d+)\)", route)
        cls.gates = [(cls.half + int(ox), cls.half + int(oz), int(dx), int(dz)) for ox, oz, dx, dz in offsets]
        cls.approaches = cls.build()

    # ------------------------------------------------------------------ the mirror of CityRaidApproach

    @classmethod
    def sight(cls, a, b):
        delta = [b[i] - a[i] for i in range(3)]
        steps = max(1, math.ceil(math.sqrt(sum(d * d for d in delta)) / cls.STEP))
        for i in range(1, steps):
            t = i / steps
            if cls.town.solid(*(math.floor(a[k] + delta[k] * t) for k in range(3))):
                return False
        return True

    @classmethod
    def posts(cls):
        eyes = {"guard": 1.62, "mecha_guard": 2.3, "flak_guard": 2.0}
        posts = [(s["pos"][0] + .5, s["pos"][1] + eyes[s["role"]], s["pos"][2] + .5)
                 for s in cls.town.data["spots"] if s["role"] in eyes]
        for gx, gz, dx, dz in cls.gates:
            posts.append((gx + .5, 1 + 1.62, gz + .5))
            posts.append((gx - dx * 14 + .5, 1 + 1.62, gz - dz * 14 + .5))
        return posts

    @classmethod
    def face(cls, bearing):
        r = 30.0
        while r < cls.town.size:
            x = math.floor(cls.half + math.cos(bearing) * r)
            z = math.floor(cls.half + math.sin(bearing) * r)
            if cls.town.mask(x, z) != INSIDE:
                return r
            r += 0.25
        return -1

    @classmethod
    def open_ground(cls, x, z, top=3):
        return cls.town.mask(x, z) != INSIDE and not any(cls.town.solid(x, y, z) for y in range(1, top + 1))

    @classmethod
    def build(cls):
        out = []
        for gx, gz, dx, dz in cls.gates:
            out.append({"kind": "gate", "bearing": math.atan2(dz, dx), "front": (gx + dx * cls.FORECOURT, gz + dz * cls.FORECOURT),
                        "staging": (gx + dx * 16, gz + dz * 16), "gate": (gx, gz, dx, dz)})
        posts = cls.posts()
        for k in range(cls.SECTIONS):
            bearing = 2 * math.pi * (k + 0.5) / cls.SECTIONS
            face = cls.face(bearing)
            if face < 0:
                continue
            fx = math.floor(cls.half + math.cos(bearing) * (face + cls.WALL_STANDOFF))
            fz = math.floor(cls.half + math.sin(bearing) * (face + cls.WALL_STANDOFF))
            aim = (fx + .5, 2.0, fz + .5)
            seen = [p for p in posts if math.dist(p, aim) <= cls.POST_SIGHT and cls.sight(p, aim)]
            if not cls.open_ground(fx, fz) or not seen:
                continue
            ring = max(cls.RING, face + cls.WALL_STANDOFF + 4)
            out.append({"kind": "wall", "bearing": bearing, "front": (fx, fz), "seen": len(seen),
                        "staging": (math.floor(cls.half + math.cos(bearing) * ring), math.floor(cls.half + math.sin(bearing) * ring))})
        for i, a in enumerate(out):
            a["index"] = i
        return out

    def choose(self, frm, failed):
        best, cost = None, float("inf")
        for a in self.approaches:
            if a["index"] in failed or not failed and a["kind"] != "gate":
                continue
            sx, sz = a["staging"]
            c = math.hypot(sx + .5 - frm[0], sz + .5 - frm[1]) + (0 if a["kind"] == "gate" else self.WALL_PENALTY)
            if c < cost:
                best, cost = a, c
        return best

    def ring_route(self, frm, approach):
        """CityRaidApproach.route's ring points (outside the Styx detour, which the town's east side alone needs)."""
        c = self.half + .5
        start = math.atan2(frm[1] - c, frm[0] - c)
        arc = math.atan2(math.sin(approach["bearing"] - start), math.cos(approach["bearing"] - start))
        steps = max(1, math.ceil(abs(arc) / .18))
        return [(math.floor(c + math.cos(start + arc * i / steps) * self.RING), math.floor(c + math.sin(start + arc * i / steps) * self.RING))
                for i in range(steps + 1)]

    # ------------------------------------------------------------------ checks

    def test_gates_come_first_and_match_the_blueprint(self):
        self.assertEqual(len(self.gates), 3)
        self.assertEqual(sorted((g[0], g[1]) for g in self.gates), sorted((g[0], g[1]) for g in self.town.data["gates"]))
        self.assertEqual([a["kind"] for a in self.approaches[:3]], ["gate"] * 3)

    def test_gate_forecourts_are_open_outside_and_watched(self):
        posts = self.posts()
        for a in self.approaches[:3]:
            fx, fz = a["front"]
            with self.subTest(front=a["front"]):
                # Open for a siege walker (4.6 blocks tall): five clear blocks.
                self.assertTrue(self.open_ground(fx, fz, top=5), "forecourt is open ground outside the wall")
                aim = (fx + .5, 2.0, fz + .5)
                self.assertTrue(any(math.dist(p, aim) <= self.POST_SIGHT and self.sight(p, aim) for p in posts),
                                "a guard post sees the forecourt")
                gx, gz, dx, dz = a["gate"]
                self.assertTrue(self.sight((gx - dx * 14 + .5, 2.62, gz - dz * 14 + .5), aim), "the gate's inner road sees its forecourt")

    def test_wall_sections_are_watched_mid_wall_never_towers(self):
        walls = [a for a in self.approaches if a["kind"] == "wall"]
        self.assertGreaterEqual(len(walls), 6, "several watched stretches of wall")
        self.assertLess(len(walls), self.SECTIONS, "blind stretches of wall are left out")
        towers = [(s["pos"][0], s["pos"][2]) for s in self.town.data["spots"] if s["role"] == "flak_guard"]
        for a in walls:
            with self.subTest(front=a["front"]):
                self.assertGreaterEqual(a["seen"], 1)
                # Halfway between two of the sixteen vertices: never within 8 degrees of a tower or gate vertex.
                vertex = (a["bearing"] / (2 * math.pi / self.SECTIONS)) % 1.0
                self.assertAlmostEqual(vertex, 0.5, delta=1e-6)
                for tx, tz in towers:
                    self.assertGreater(math.dist(a["front"], (tx, tz)), 12, "a wall section's front is not at a tower")
        # The north wall has no gate and its foot is out of every guard's sight: no section there.
        for a in walls:
            degrees = math.degrees(a["bearing"]) % 360
            self.assertFalse(240 < degrees < 300, f"north wall section at {degrees:.2f} degrees is blind")

    def test_routes_choose_a_gate_and_keep_off_the_wall(self):
        c = self.half + .5
        for degrees in range(0, 360, 15):
            frm = (c + math.cos(math.radians(degrees)) * 130, c + math.sin(math.radians(degrees)) * 130)
            first = self.choose(frm, set())
            with self.subTest(degrees=degrees):
                self.assertEqual(first["kind"], "gate", "a raid makes for a gate first")
                for x, z in self.ring_route(frm, first):
                    self.assertNotEqual(self.town.mask(x, z), INSIDE, "the ring stays outside the wall")
                second = self.choose(frm, {first["index"]})
                self.assertIsNotNone(second)
                self.assertNotEqual(second["index"], first["index"])
                for x, z in self.ring_route(frm, second):
                    self.assertNotEqual(self.town.mask(x, z), INSIDE, "the fallback ring stays outside the wall")
        everything = {a["index"] for a in self.approaches}
        self.assertIsNone(self.choose((0, 0), everything), "with every approach failed there is none: the raider holds")

    def test_a_failed_gate_falls_back_beside_it_in_sight_of_its_guards(self):
        for gate in self.approaches[:3]:
            sx, sz = gate["staging"]
            fallback = self.choose((sx + .5, sz + .5), {gate["index"]})
            with self.subTest(gate=gate["front"]):
                self.assertEqual(fallback["kind"], "wall", "a section beside the gate before the long march to another gate")
                turn = abs(math.atan2(math.sin(fallback["bearing"] - gate["bearing"]), math.cos(fallback["bearing"] - gate["bearing"])))
                self.assertLess(math.degrees(turn), 30, "the section is next to the failed gate")

    def test_guard_vantages_at_each_gate_have_room(self):
        # CityGuardVantage tries the inner road, the passage's inner end, the passage, the posts outside and the forecourt.
        for gx, gz, dx, dz in self.gates:
            for k, top in ((-14, 4), (-6, 4), (0, 4), (4, 4), (self.FORECOURT, 4)):
                x, z = gx + dx * k, gz + dz * k
                with self.subTest(gate=(gx, gz), step=k):
                    # A guard walker is 3.8 blocks tall: four clear blocks over the road.
                    self.assertFalse(any(self.town.solid(x, y, z) for y in range(1, top + 1)), "room for a guard walker")
                    self.assertTrue(self.town.solid(x, 0, z), "ground to stand on")


if __name__ == "__main__":
    unittest.main()
