import test_western_city as architectural
from western_city_terraces import build_terraced, PRESERVE, outer_ring, street_height, RING_X_OFFSET
from town_voxels import parse


class TerracedCityTests(architectural.WesternCityTests):
    walk_bounds=(-195,-76,14,16,42,178)

    @classmethod
    def setUpClass(cls):
        cls.v,cls.rooms,cls.plots=build_terraced()
        cls.names={s:parse(s)[0] for s in set(cls.v.b.values())}
        cls.walked=cls.walk((0,1,96))

    def test_both_underground_entrances_main_halls_and_gallery_reached(self):
        for p in [(-185,-47,96),(-160,-47,68),(-188,-47,148),(-160,-73,40),(-40,-73,40),(-102,-59,105),(-102,-73,120)]:
            self.assertTrue(p in self.walked,f'No stair/walking route to {p}')

    def test_no_authored_fixture_suffocates_grass_into_a_rollback_conflict(self):
        for (x,y,z),s in self.v.b.items():
            if self.names[s]=='minecraft:grass_block':self.assertEqual(self.name((x,y+1,z)),'minecraft:air')

    def test_known_build_pockets_are_absent_from_authored_cells(self):
        for zone in PRESERVE:
            x0,y0,z0,x1,y1,z1=zone['bounds']
            for x in range(x0,x1+1):
                for y in range(y0,y1+1):
                    for z in range(z0,z1+1):self.assertNotIn((x,y,z),self.v.b,zone['reason'])

    def test_all_three_courts_have_ground_level_access(self):
        for p in [(-180,-47,96),(-122,-29,96),(-65,-11,96)]:self.assertTrue(p in self.walked, str(p))

    def test_underground_air_has_closed_authored_boundary(self):
        for (x,y,z),state in self.v.b.items():
            if self.names[state]!='minecraft:air' or y>=-49:continue
            for dx,dy,dz in ((1,0,0),(-1,0,0),(0,1,0),(0,-1,0),(0,0,1),(0,0,-1)):
                self.assertTrue((x+dx,y+dy,z+dz) in self.v.b,f'Open underground boundary at {x},{y},{z}')

    def test_original_city_has_no_excavation_or_buildings(self):
        # The only construction east of the new gatehouse is a bridge ending
        # just before the original gatehouse. The terrain on either side survives.
        for (x,y,z),state in self.v.b.items():
            if x>=-26 and y>-48:
                self.assertTrue(x<=16 and 93<=z<=99,(x,y,z,state))
            if x>=0:
                self.assertNotEqual(self.names[state],'minecraft:air')

    def test_ring_encloses_the_courts_and_has_two_clear_gates(self):
        ring=outer_ring()
        for room in self.rooms:
            x0,_,z0,x1,_,z1=room['bounds']
            for x in (x0,x1):
                for z in (z0,z1):
                    self.assertLess(ring.sdist(x-RING_X_OFFSET,z),-3,room['id'])
        for x in range(-213,17):
            y=street_height(x)
            for z in range(94,99):
                self.assertTrue(self.clear((x,y+1,z)) and self.clear((x,y+2,z)),(x,y,z))
                self.assertNotEqual(self.name((x,y,z)),'minecraft:air',(x,y,z))
        # Sample all four sides at the wall walk's masonry height. Flood-fill
        # from the market with gate passages capped to verify a continuous ring.
        from collections import deque
        start=(-110,96)
        seen={start}
        pending=deque([start])
        while pending:
            x,z=pending.popleft()
            for dx,dz in ((1,0),(-1,0),(0,1),(0,-1)):
                q=(x+dx,z+dz)
                if q in seen:continue
                xx,zz=q
                if xx in (-210,-36) and 93<=zz<=99:continue
                self.assertTrue(-219<xx<-20 and 0<zz<192,f'Ring leaks at {q}')
                if self.name((xx,street_height(xx)+3,zz))=='minecraft:air':
                    seen.add(q);pending.append(q)
        for q in ((-130,95),(-80,95),(-40,95),(-190,95)):
            self.assertTrue(q in seen, f'Interior lane cut off at {q}')

    def test_foundations_leave_the_surveyed_field_and_buried_masonry_alone(self):
        for x0,y0,z0,x1,y1,z1 in ((-92,-53,-5,-86,-50,15),(-157,-83,3,-153,-79,13)):
            for x in range(x0,x1+1):
                for y in range(y0,y1+1):
                    for z in range(z0,z1+1):
                        self.assertTrue((x,y,z) not in self.v.b, f'Foundation overlaps existing site at {(x,y,z)}')
