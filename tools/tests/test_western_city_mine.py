"""Acceptance of Deepstead's physical geometry in the authored city coordinate frame."""
import sys
import unittest
from collections import deque
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from town_voxels import S, Voxels, parse
from western_city_mine import AIR, BASIN, CX, CZ, MARKET, NEIGHBORS, SIDES, UPPER, WATER, build_mine


def walking_routes(v, start):
    # Feet coordinates. A full block above another needs a real stair facing
    # in the direction of ascent, or the high half of a correctly facing stair.
    # Require the same step to be usable on the return trip as well.
    visited = {tuple(start)}
    queue = deque(visited)
    directions = ("east","south","west","north")

    def surface(x,y,z):
        state = v.get(x,y-1,z)
        if v.get(x,y,z) != AIR or v.get(x,y+1,z) != AIR or not state:
            return None
        name, props = parse(state)
        if state in (AIR,WATER) or name.endswith(("_wall","_fence","_pane","_slab","lantern","torch")):
            return None
        return props.get("facing") if name.endswith("_stairs") else "full"

    while queue:
        x,y,z = queue.popleft()
        current = surface(x,y,z)
        for i,(dx,dz) in enumerate(SIDES):
            for dy in (0,1,-1):
                q = x+dx,y+dy,z+dz
                if q in visited:
                    continue
                target = surface(*q)
                if not target or dy == 1 and target != directions[i] and current != directions[i]:
                    continue
                opposite = directions[(i+2)%4]
                if dy == -1 and current != opposite and target != opposite:
                    continue
                visited.add(q)
                queue.append(q)
    return visited


class MineCityTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.v = Voxels()
        cls.plan = build_mine(cls.v)
        cls.walked = walking_routes(cls.v, cls.plan["entrance"])

    def test_each_tier_and_every_workstation_has_a_returnable_stair_route(self):
        destinations = dict(self.plan["landmarks"])
        for i, shop in enumerate(self.plan["utilities"]):
            self.assertEqual(self.v.get(*shop["position"]), S(shop["block"]))
            destinations[f"{shop['block']}-{i}"] = shop["approach"]
        missing = {k:p for k,p in destinations.items() if tuple(p) not in self.walked}
        self.assertFalse(missing, missing)

    def test_gallery_is_a_full_circuit_on_each_level(self):
        for floor,radius in ((UPPER,31),(MARKET,26),(BASIN,22)):
            # Cut the west half of the ring and check both east half-routes
            # independently. A blocked quadrant cannot pass via the tower,
            # different storeys, or by doubling back around the other side.
            east, west = (CX+radius,CZ), (CX-radius,CZ)
            for side in (-1,1):
                seen = {east}
                pending = deque([east])
                while pending:
                    x,z = pending.popleft()
                    for dx,dz in SIDES:
                        xx,zz = x+dx,z+dz
                        if side*(zz-CZ)<0 or (xx-CX)**2+(zz-CZ)**2<18**2:
                            continue
                        if (xx,zz) in seen or (xx,floor+1,zz) not in self.walked:
                            continue
                        seen.add((xx,zz))
                        pending.append((xx,zz))
                self.assertTrue(west in seen,f"Interrupted gallery at floor {floor} side {side}")

    def test_shell_and_surface_footprint(self):
        for (x,y,z), state in self.v.b.items():
            if x >= 17:
                self.assertLess(y, -8)
            if y >= 0:
                self.assertTrue(-14 <= x <= 0 and 84 <= z <= 93, (x,y,z))
            if state in (AIR,WATER) and y < 0:
                for dx,dy,dz in NEIGHBORS:
                    self.assertIn((x+dx,y+dy,z+dz), self.v.b)
        self.assertGreater(114 + self.plan["bounds"][1], -64)

    def test_every_lantern_is_supported(self):
        for (x,y,z), state in self.v.b.items():
            name, props = parse(state)
            if name == "minecraft:lantern":
                support = self.v.get(x,y+(1 if props.get("hanging")=="true" else -1),z,AIR)
                self.assertNotIn(support,(AIR,WATER), (x,y,z))

    def test_water_stays_in_its_basin_and_glazed_flume(self):
        wet = [p for p,b in self.v.b.items() if b == WATER]
        self.assertGreater(len(wet), 30)
        for x,y,z in wet:
            self.assertLess(y,BASIN)
            if y > -94:
                self.assertTrue(CX+16 <= x <= CX+17 and CZ+18 <= z <= CZ+19)
            for dx,dy,dz in ((1,0,0),(-1,0,0),(0,0,1),(0,0,-1),(0,-1,0)):
                self.assertNotIn(self.v.get(x+dx,y+dy,z+dz),(AIR,None),(x,y,z))


if __name__ == "__main__":
    unittest.main()
