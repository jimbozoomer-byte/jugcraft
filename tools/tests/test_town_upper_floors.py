import base64
import gzip
import json
from pathlib import Path
import sys
import unittest

sys.path.insert(0,str(Path(__file__).resolve().parents[1]))
from town_upper_floors import build, LIFT, TOWN


class UpperFloorGeometry(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.plan=build()
        town=json.loads(gzip.decompress((TOWN / "town.json.gz").read_bytes()))
        cls.size, cls.ymin, cls.palette=town["size"],town["y_min"],town["palette"]
        cls.blocks=base64.b64decode(town["blocks"])

    def old(self,x,y,z):
        i=((y-self.ymin)*self.size*self.size+z*self.size+x)*2
        return self.palette[int.from_bytes(self.blocks[i:i+2],"little")]

    def test_shipping_plan_matches_authored_changes(self):
        shipped=json.loads(gzip.decompress((TOWN / "old_town_upper_floors.json.gz").read_bytes()))
        self.assertEqual(self.plan,shipped)
        self.assertEqual(len(shipped["houses"]),4)

    def test_every_roof_is_raised_intact_above_an_original_street(self):
        for house in self.plan["houses"]:
            changes={(x,y,z): self.plan['palette'][s] for x,y,z,s in house['cells']}
            x0,deck,z0,x1,top,z1=house['roof']
            self.assertTrue(x1<78 or x0>114 or z0>87, "Church plot is excluded")
            self.assertTrue(any('light_gray_stained_glass' in s for s in changes.values()))
            for x in range(x0,x1+1):
                for z in range(z0,z1+1):
                    for y in range(deck+5,top+1):
                        self.assertEqual(changes.get((x,y,z),self.old(x,y,z)),self.old(x,y-5,z))
            self.assertTrue(all((x,z)==tuple(house['lift']) or (x0<=x<=x1 and z0<=z<=z1 and deck<=y<=top)
                                for x,y,z in changes))

    def test_one_clear_lift_landing_opens_onto_every_floor(self):
        for house in self.plan["houses"]:
            changes={(x,y,z): self.plan['palette'][s] for x,y,z,s in house['cells']}
            def placed(x,y,z):
                return changes.get((x,y,z),self.old(x,y,z))
            x,z=house['lift']
            deck=house['roof'][1]
            for y in range(deck+3):
                self.assertEqual(placed(x,y,z), LIFT if y%5==0 else "minecraft:air")
                if y%5==0:
                    self.assertTrue(any(placed(x+dx,y,z+dz)!="minecraft:air"
                                        and all(placed(x+dx,y+dy,z+dz)=="minecraft:air" for dy in (1,2))
                                        for dx,dz in ((1,0),(-1,0),(0,1),(0,-1))), house['id'])

    def test_jetties_have_open_larger_rooms_and_an_intact_roof(self):
        palette=self.plan["palette"]
        def lookup(stage):
            return {(x,y,z):palette[i] for x,y,z,i in stage["cells"]}
        originals={h["id"]:h for h in self.plan["houses"]}
        expected={"west_gate_house":[(31,97,41,109),(30,97,42,109)],
                  "south_square_inn":[(84,115,95,126),(84,115,96,126),(84,114,96,127)]}
        self.assertEqual(set(expected),{h["base"] for h in self.plan["jetties"]})
        for h in self.plan["jetties"]:
            rooms=expected[h["base"]]
            base=originals[h["base"]]
            before,after=lookup(base),lookup(h)
            old=lambda x,y,z:before.get((x,y,z),self.old(x,y,z))
            actual=lambda x,y,z:after.get((x,y,z),old(x,y,z))
            x0,deck,z0,x1,top,z1=h["roof"]
            sx,sz=base["lift"]
            self.assertEqual(h["rise"],5*len(rooms)+1)
            sizes=[]
            for floor,(w,n,e,s) in enumerate(rooms):
                y=deck+5*floor
                sizes.append((e-w-1)*(s-n-1))
                self.assertEqual(actual(sx,y,sz),LIFT)
                self.assertTrue(all(actual(sx,y+dy,sz)=="minecraft:air" for dy in (1,2,3,4)))
                open_cells={(x,z) for x in range(w+1,e) for z in range(n+1,s)
                            if actual(x,y,z)!="minecraft:air" and actual(x,y+2,z)=="minecraft:air"
                            and (actual(x,y+1,z)=="minecraft:air" or "carpet" in actual(x,y+1,z))}
                frontier=[(sx,sz)]
                reached=set(frontier)
                while frontier:
                    x,z=frontier.pop()
                    for p in ((x+1,z),(x-1,z),(x,z+1),(x,z-1)):
                        if p in open_cells and p not in reached:
                            reached.add(p)
                            frontier.append(p)
                self.assertEqual(reached,open_cells,h["id"])
                self.assertGreater(len(reached),sizes[-1]*.7)
                self.assertGreater(sum("stairs" in actual(x,y-1,z) for x in range(w-1,e+2)
                                       for z in range(n-1,s+2)),0)
            self.assertEqual(sizes,sorted(set(sizes)))
            w,n,e,s=rooms[-1]
            gate=h["base"]=="west_gate_house"
            window_x,window_z=((w+e)//2+1,n-1) if gate else (e+1,(n+s)//2+1)
            upper=deck+(len(rooms)-1)*5
            for depth in (0,1,2):
                x=window_x if gate else window_x-depth
                z=window_z+depth if gate else window_z
                self.assertNotEqual(actual(x,upper,z),"minecraft:air")
                self.assertTrue(all(actual(x,upper+dy,z)=="minecraft:air" for dy in (1,2)))
            for x in range(x0,x1+1):
                for z in range(z0,z1+1):
                    for y in range(deck,top-h["rise"]+1):
                        if old(x,y,z)!="minecraft:air":
                            self.assertEqual(actual(x,y+h["rise"],z),old(x,y,z))
            self.assertTrue(all(y>=deck-1 for x,y,z in after))

    def test_survey_margins_contain_other_buildings_but_no_original_roofs_touch(self):
        for h,expected in zip(self.plan["houses"],(0,1,13,53)):
            x0,deck,z0,x1,top,z1=h["roof"]
            neighbors=[]
            for x in range(x0-2,x1+3):
                for z in range(z0-2,z1+3):
                    if x0<=x<=x1 and z0<=z<=z1:
                        continue
                    for y in range(deck,deck+24):
                        if self.old(x,y,z)=="minecraft:air":
                            continue
                        neighbors.append((x,y,z))
                        for dx,dz in ((-1,0),(1,0),(0,1),(0,-1)):
                            if x0<=x+dx<=x1 and z0<=z+dz<=z1:
                                self.assertEqual(self.old(x+dx,y,z+dz),"minecraft:air")
            self.assertEqual(len(neighbors),expected,h["id"])
