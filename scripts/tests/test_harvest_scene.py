"""Saved layout stability and bounded town display geometry."""
import gzip
import json
import sys
import unittest
from pathlib import Path
ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))
import town_decor

class HarvestSceneTests(unittest.TestCase):
    def test_old_harvest_remains_available(self):
        legacy = json.loads(gzip.decompress((ROOT / "src/main/resources/data/jugcraft/town/revisions/3.json.gz").read_bytes()))
        self.assertEqual(town_decor.data()["centerpiece"]["harvest"], legacy["decor"]["centerpiece"]["harvest"])
        self.assertIn("harvest", town_decor.THEMES)
        self.assertNotIn("harvest_v2", town_decor.THEMES)  # site layout, not NPC/shop theme

    def test_packaged_decor_matches_generator(self):
        live = json.loads(gzip.decompress((ROOT / "src/main/resources/data/jugcraft/town/town.json.gz").read_bytes()))
        self.assertEqual(live["decor"], town_decor.data())

    def test_all_parts_fit_with_walkway_margin(self):
        scene = town_decor.harvest_centerpiece()
        self.assertEqual(len(scene), 75)
        parts = set()
        for slot, state in scene.items():
            x, z, y = slot % 7, slot // 7 % 7, slot // 49 + 1
            self.assertTrue(1 <= x <= 5 and 1 <= z <= 5 and 1 <= y <= 3)
            part = int(state.split("part=")[1].split("]")[0])
            self.assertEqual(part, (5-x) + 5*((y-1)+3*(z-1)))
            parts.add(part)
        self.assertEqual(parts, set(range(75)))

if __name__ == "__main__": unittest.main()
