import unittest
import test_western_city as reference
from town_voxels import parse
from western_city_terraces import build_terraced


class WaterfrontCityTests(unittest.TestCase):
    walk_bounds = (-268,-98,12,130,27,192)
    walk = classmethod(reference.WesternCityTests.walk.__func__)
    name = classmethod(reference.WesternCityTests.name.__func__)
    clear = classmethod(reference.WesternCityTests.clear.__func__)

    @classmethod
    def setUpClass(cls):
        cls.v,cls.rooms,cls.plots=build_terraced(expanded=True)
        cls.names={s:parse(s)[0] for s in set(cls.v.b.values())}
        cls.walked=cls.walk((0,1,96))

    def test_all_apartments_and_roof_landings_reached_from_old_gate(self):
        self.assertEqual(len({r['id'] for r in self.rooms}),72)
        for r in self.rooms:
            self.assertIn(tuple(r['door']),self.walked,r['id'])
        for x,y,z,storeys in ((-182,-48,28,8),(-129,-30,25,7),(-74,-12,45,6)):
            self.assertIn((x+10,y+5*storeys+1,z+2),self.walked)

    def test_gate_reaches_mine_and_waterfront_in_one_world(self):
        for xyz in ((-10,1,93),(87,-67,86),(-235,-49,96)):
            self.assertIn(xyz,self.walked,f'No walking route to {xyz}')
        plots={p['id'] for p in self.plots}
        self.assertTrue({'deepstead','harbour_light','harbour_chart_house'} <= plots)

    def test_landlord_structure_is_outside_rental_boxes(self):
        for room in self.rooms:
            a,b,c,d,e,f=room['bounds']
            x,y,z=room['door']
            self.assertFalse(a<=x<=d and b<=y<=e and c<=z<=f)
            self.assertEqual(e-b,3)
            for xx,zz in ((a,c),(d,f)):
                self.assertEqual(self.name((xx,b-1,zz)),'minecraft:spruce_planks')
                self.assertNotEqual(self.name((xx,e+1,zz)),'minecraft:air')
