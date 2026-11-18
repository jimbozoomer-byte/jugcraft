"""Architectural acceptance: real generated cells, not a duplicated layout fixture."""
import sys
import unittest
from collections import deque
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parents[1]))
from western_city import build
from town_voxels import parse


class WesternCityTests(unittest.TestCase):
    walk_bounds=(-195,-27,14,16,42,178)
    @classmethod
    def setUpClass(cls):
        cls.v,cls.rooms,cls.plots=build()
        cls.names={state:parse(state)[0] for state in set(cls.v.b.values())}
        cls.walked=cls.walk((-102,1,96))

    @classmethod
    def name(cls,p):return cls.names.get(cls.v.b.get(p),'minecraft:air')
    @classmethod
    def clear(cls,p):
        n=cls.name(p)
        return n=='minecraft:air' or n.endswith(('_door','_carpet'))
    @classmethod
    def walk(cls,start):
        x0,y0,z0,x1,y1,z1=cls.walk_bounds
        seen={start};todo=deque([start])
        while todo:
            x,y,z=todo.popleft()
            for dx,dz in [(1,0),(-1,0),(0,1),(0,-1)]:
                for dy in [0,1,-1]:
                    p=(x+dx,y+dy,z+dz);px,py,pz=p
                    if p in seen or not(x0<=px<=x1 and y0<=py<=y1 and z0<=pz<=z1):continue
                    if not cls.clear(p) or not cls.clear((px,py+1,pz)):continue
                    under=cls.name((px,py-1,pz))
                    if under=='minecraft:air' or under.endswith(('_door','_carpet','_bars','_wall')) or under=='minecraft:water':continue
                    if dy==1 and not under.endswith('_stairs'):continue
                    # A one-block descent is also possible from a stair's high half.
                    seen.add(p);todo.append(p)
        return seen

    def test_all_48_apartments_reached_without_jumping(self):
        self.assertEqual(len(self.rooms),48)
        self.assertEqual(len({r['id'] for r in self.rooms}),48)
        missing=[r['id'] for r in self.rooms if tuple(r['door']) not in self.walked]
        self.assertEqual(missing,[],f'Inaccessible apartments: {missing}')

    def test_both_underground_entrances_main_halls_and_gallery_reached(self):
        for p in [(-166,1,68),(-38,1,68),(-160,-25,105),(-40,-25,105),(-102,-11,105),(-102,-25,120)]:
            # Entrance path itself is x+1; listed x+5 is the lower returning stair.
            if p[1]==1:p=(p[0]-4,p[1],p[2])
            self.assertTrue(p in self.walked,f'No ordinary walking route to {p}')

    def test_original_city_has_no_excavation_or_buildings(self):
        for (x,y,z),s in self.v.b.items():
            if x>=0:
                self.assertTrue(y==0 and 94<=z<=98 and x<=16,(x,y,z,s))

    def test_lanterns_are_attached_to_supports(self):
        for (x,y,z),s in self.v.b.items():
            name,properties=parse(s)
            if name=='minecraft:lantern':
                dy=1 if properties.get('hanging')=='true' else -1
                self.assertNotEqual(self.name((x,y+dy,z)),'minecraft:air',f'Unsupported lantern at {x},{y},{z}')

    def test_every_apartment_has_a_complete_bed(self):
        for room in self.rooms:
            x0,y0,z0,x1,y1,z1=room['bounds']
            beds=[s for (x,y,z),s in self.v.b.items() if x0<=x<=x1 and y0<=y<=y1 and z0<=z<=z1 and '_bed[' in s]
            self.assertEqual(len(beds),2,room['id'])

    def test_no_authored_fixture_suffocates_grass_into_a_rollback_conflict(self):
        for (x,y,z),state in self.v.b.items():
            if y==0 and self.names[state]=='minecraft:grass_block':
                self.assertEqual(self.name((x,1,z)),'minecraft:air',f'Covered grass at {x},{z}')

if __name__=='__main__':unittest.main()
