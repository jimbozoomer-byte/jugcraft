"""Apply the exact shipped retail overlay to Deepstead, including the old route tests."""
import gzip
import json
import unittest

import test_western_city_mine as architecture
from deepstead_guild_shops import AIR, BOUNDS, SHOPS, TOWN, build_asset
from town_voxels import S, Voxels
from western_city_mine import build_mine


class DeepsteadGuildShopTests(architecture.MineCityTests):
    @classmethod
    def setUpClass(cls):
        cls.v = Voxels()
        cls.plan = build_mine(cls.v)
        cls.overlay = json.loads(gzip.decompress((TOWN/"deepstead_guild_shops.json.gz").read_bytes()))
        cls.changed = set()
        for x,y,z,old,new in cls.overlay["cells"]:
            p = (x,y,z)
            if p in cls.changed or cls.v.get(*p)!=cls.overlay["palette"][old]:
                raise ValueError(f"Stale or duplicate retail change at {p}")
            cls.changed.add(p)
            cls.v.set(*p,cls.overlay["palette"][new])
        cls.walked = architecture.walking_routes(cls.v, cls.plan["entrance"])

    def test_shipped_patch_rebuilds_from_production_before_image(self):
        self.assertEqual(self.overlay, build_asset())
        self.assertEqual(self.overlay["shops"],list(SHOPS))
        self.assertTrue(all(any(a<=x<=d and b<=y<=e and c<=z<=f for a,b,c,d,e,f in BOUNDS)
                            for x,y,z in self.changed))

    def test_shops_and_service_sides_reached_from_bridge(self):
        for s in self.overlay["shops"]:
            for field in ("merchant","entrance"):
                self.assertTrue(tuple(s[field]) in self.walked, (s["id"],field,s[field]))
        # Guests approach the opposite side of the counter with an unobstructed
        # eye line. Neither merchant needs to stand in the workbench or the bar.
        for x,y,z in ((91,-81,56),(87,-67,118),(82,-81,54),(85,-67,123),(81,-67,120)):
            self.assertTrue((x,y,z) in self.walked,(x,y,z))
        for x,y,z in ((91,-80,55),(87,-66,119)):
            self.assertEqual(self.v.get(x,y,z),AIR)
        self.assertEqual(self.v.get(81,-81,54),S("stonecutter",facing="east"))
        self.assertEqual(self.v.get(86,-67,123),S("crafting_table"))

    def test_original_posts_and_patrols_keep_floor_and_headroom(self):
        # The ten external posts are at the surface, on the bridge or in the
        # western ring/harbour. None lie within either shop volume.
        posts = ((68,1,96,12,0),(128,1,96,12,0),(-171,-47,96,15,0),
                 (-60,-11,96,15,0),(-105,-29,96,11,0),(-237,-49,95,0,12),
                 (-235,-49,119,0,13),(-164,-47,57,0,0),(-111,-29,54,0,0),(-56,-11,74,0,0),
                 (121,-53,73,0,0),(124,-67,84,0,0),(122,-81,96,0,0),(55,-81,84,0,0),
                 (64,-67,62,0,0),(106,-67,62,0,0),(87,-53,102,0,10),(62,-67,86,10,0),(110,-81,86,4,0))
        for i,(x,y,z,dx,dz) in enumerate(posts):
            for step in range(max(dx,dz)+1):
                xx,zz = x+min(dx,step),z+min(dz,step)
                self.assertTrue(all((xx,y+h,zz) not in self.changed for h in (-1,0,1)),(xx,y,zz))
                if i>=10:
                    self.assertTrue((xx,y,zz) in self.walked,(i+1,xx,y,zz))


if __name__=="__main__":
    unittest.main()
