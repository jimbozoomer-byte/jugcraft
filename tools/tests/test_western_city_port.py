"""Acceptance checks against the authored harbour cells, including both decks."""
from collections import deque
from pathlib import Path
import sys
import unittest

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from town_voxels import Voxels, parse
from western_city_port import build_port, DECK, WATERLINE, PIER_CENTRES


class WesternCityPortTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.v = Voxels()
        cls.plots = build_port(cls.v)
        cls.names = {s:parse(s)[0].split(':')[1] for s in set(cls.v.b.values())}

    def name(self, p):
        return self.names.get(self.v.b.get(p), 'air')

    def test_gate_to_trade_houses_mole_finger_piers_and_both_cutter_decks(self):
        start = (-216,-47,96)
        seen = {start}
        queue = deque([start])

        def clear(p):
            name = self.name(p)
            return name in ('air','ladder') or name.endswith(('_door','_carpet'))

        while queue:
            x, y, z = queue.popleft()
            for dx,dz in ((1,0),(-1,0),(0,1),(0,-1)):
                for dy in (0,-1,1):
                    q = (x+dx,y+dy,z+dz)
                    if q in seen or not DECK-1 <= q[1] <= -47:
                        continue
                    if not clear(q) or not clear((q[0],q[1]+1,q[2])):
                        continue
                    floor = self.name((q[0],q[1]-1,q[2]))
                    if floor in ('air','ladder') or floor.endswith(('_wall','_fence','_door','_carpet')):
                        continue
                    if dy == 1 and not floor.endswith('_stairs'):
                        continue
                    seen.add(q)
                    queue.append(q)
        for z in PIER_CENTRES:
            self.assertIn((-262,DECK+1,z),seen)
        for x,y,z in [(-260,-49,50),(-262,-49,50),(-246,-49,98),
                      (-232,-49,57),(-232,-49,78),(-232,-49,114),(-232,-49,137),
                      (-254,-50,71),(-255,-50,124)]:
            self.assertIn((x,y,z),seen, f'No ordinary walking route from the gate to {(x,y,z)}')
        for z in range(94,99):
            for x in range(-216,-242,-1):
                self.assertTrue(any((x,y,z) in seen for y in range(DECK+1,-46)),(x,z))

    def test_lighthouse_and_clock_ladders_are_backed(self):
        for x,z,height in ((-262,50,19),(-223,99,15)):
            for h in range(1,height+1):
                self.assertEqual(self.name((x,DECK+h,z)),'ladder')
                self.assertNotEqual(self.name((x,DECK+h,z+1)),'air')
        for z in PIER_CENTRES:
            for y in range(WATERLINE-1,DECK+1):
                state = self.v.get(-261,y,z-2)
                self.assertEqual(parse(state)[1]['waterlogged'], str(y <= WATERLINE).lower())
                self.assertNotEqual(self.name((-261,y,z-1)), 'air')

    def test_all_lanterns_attached_and_no_clearing_of_water_or_shipwreck(self):
        for (x,y,z),state in self.v.b.items():
            name, props = parse(state)
            if name.endswith(':lantern'):
                dy = 1 if props.get('hanging') == 'true' else -1
                self.assertNotEqual(self.name((x,y+dy,z)), 'air', (x,y,z))
            if name.endswith(':air'):
                self.assertGreater(y,WATERLINE)
            self.assertFalse(x < -263 and 85 <= z <= 104, (x,y,z))
        # The wide centre entrance to the anchorage stays navigable in height,
        # width and depth, around the two moored vessels and the old wreck.
        for x in range(-266,-249):
            for z in range(100,110):
                for y in range(WATERLINE-2,WATERLINE+6):
                    self.assertNotIn((x,y,z),self.v.b)

    def test_service_plots_have_distinct_ids_and_actual_workstations(self):
        names = {p['id'] for p in self.plots}
        self.assertEqual(len(names),len(self.plots))
        self.assertTrue({'harbour_chart_house','harbour_smokehouse','harbour_inn',
                         'harbour_fish_market','harbour_light','harbour_customs_clock',
                         'harbour_chandlery'} <= names)
        for p, name in (((-228,-49,57),'cartography_table'),((-226,-49,78),'loom'),
                        ((-226,-49,115),'smoker'),((-226,-31,96),'bell')):
            self.assertEqual(self.name(p),name)


if __name__ == '__main__':
    unittest.main()
