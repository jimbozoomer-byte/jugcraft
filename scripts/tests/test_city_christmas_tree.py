"""The packaged square has to fit the old city and preserve its saved slot addresses."""
import base64
import gzip
import json
import sys
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))
import civic_fir


class CityFirTest(unittest.TestCase):
    def test_existing_square_can_accept_the_full_canopy(self):
        town = json.loads(gzip.decompress((ROOT / "src/main/resources/data/jugcraft/town/town.json.gz").read_bytes()))
        site, = [s for s in town["sites"] if s["kind"] == "centerpiece"]
        old = [[x, y, z, x-101+7*(z-101)+49*(y-1)] for x in range(101,108)
               for z in range(101,108) for y in range(1,14)]
        self.assertEqual(site["blocks"][:len(old)], old)
        indexed = {i: (x,y,z) for x,y,z,i in site["blocks"]}
        self.assertEqual(len(indexed), len(site["blocks"]))
        scene = town["decor"]["centerpiece"]["december_v2"]
        self.assertEqual({int(k):v for k,v in scene.items()}, civic_fir.scene())
        occupied = {tuple(row[:3]) for other in town["sites"] if other is not site for row in other["blocks"]}
        volume = base64.b64decode(town["blocks"])
        for (dx,y,dz), block in civic_fir.blocks().items():
            x,z = dx+104,dz+104
            self.assertEqual(indexed[civic_fir.slot(dx,y,dz)],(x,y,z))
            self.assertNotIn((x,y,z), occupied)
            offset = 2*((y-town["y_min"])*town["size"]**2 + z*town["size"] + x)
            self.assertEqual(volume[offset:offset+2], b"\x01\x00")
            if y < 5:
                self.assertLessEqual(max(abs(dx),abs(dz)),2)
        self.assertEqual(max(p[1] for p in civic_fir.blocks()),31)
        self.assertGreaterEqual(max(p[0] for p in civic_fir.blocks())-min(p[0] for p in civic_fir.blocks()),17)
        self.assertEqual(sum("civic_gift_skirt" in s for s in scene.values()),9)
