"""The town feast's layout (town/TownFeast.java LAYOUT) against the town blueprint (data/jugcraft/town/town.json.gz).

Every cell must be open blueprint ground inside the wall, off every decor site and townsperson's place, and keep its
measured distance from the church, the market stalls, the gate roads and the square's centrepiece site
(docs/features/town-feast.md). Distances are clear blocks between footprints at feet level: the Chebyshev distance less
one, over every pair of cells.
"""
from pathlib import Path
import base64
import gzip
import json
import re
import unittest
from array import array

ROOT = Path(__file__).resolve().parents[2]
JAVA = ROOT / 'src/main/java/io/github/jimbozoomer/jugcraft/town/TownFeast.java'
TOWN = ROOT / 'src/main/resources/data/jugcraft/town/town.json.gz'


def rect(x0, x1, z0, z1):
    return [(x, z) for x in range(x0, x1 + 1) for z in range(z0, z1 + 1)]


# Protected areas, in town coordinates (x, z), from the blueprint (docs/features/town-feast.md).
AREAS = {
    'NW stall': (rect(73, 79, 88, 92), 3),
    'NE stall': (rect(113, 119, 88, 92), 3),
    'west stalls': (rect(74, 78, 98, 111), 3),
    'east stalls': (rect(115, 119, 98, 111), 3),
    'church building': (rect(84, 108, 60, 84), 3),
    'church door path': (rect(95, 98, 85, 92), 3),
    'south gate road': (rect(94, 98, 112, 130), 10),
    'centrepiece site': (rect(101, 107, 101, 107), 10),
    'fountain and benches': (rect(81, 95, 100, 111), 5),
}
# The main east-west gate road: three clear rows from the tables, two from the turkey.
MAIN_ROAD = rect(60, 130, 93, 97)


def gap(a, b):
    return min(max(abs(x1 - x2), abs(z1 - z2)) for x1, z1 in a for x2, z2 in b) - 1


def layout():
    """Each site's cells (x, y, z) from the Java layout: tables run east along x; the turkey, facing south, is 3 x 2 x 3."""
    text = JAVA.read_text(encoding='utf-8')
    sites = {}
    for name, x, y, z, lengths in re.findall(r'Site\.table\("(\w+)", new BlockPos\((\d+), (\d+), (\d+)\), (\d+),', text):
        sites[name] = ('table', [(int(x) + i, int(y), int(z)) for i in range(int(lengths))])
    for name, x, y, z, facing in re.findall(r'Site\.turkey\("(\w+)", new BlockPos\((\d+), (\d+), (\d+)\), Direction\.(\w+)\)', text):
        if facing != 'SOUTH':
            raise AssertionError('the test knows only a south-facing turkey')
        # MultiDecorationBlock.partPos: right is facing.getCounterClockWise() (east), away is facing.getOpposite() (north).
        sites[name] = ('turkey', [(int(x) + right, int(y) + up, int(z) - away)
                                  for away in range(3) for up in range(2) for right in range(3)])
    return sites


class Town:
    def __init__(self):
        data = json.load(gzip.open(TOWN))
        self.size, self.y_min, self.height = data['size'], data['y_min'], data['height']
        blocks = array('H')
        blocks.frombytes(base64.b64decode(data['blocks']))
        if blocks.itemsize != 2:
            raise AssertionError('unexpected short size')
        import sys
        if sys.byteorder != 'little':
            blocks.byteswap()
        self.blocks = blocks
        self.mask = base64.b64decode(data['mask'])
        self.palette = data['palette']
        self.sites = data['sites']
        self.spots = data['spots']

    def name(self, x, y, z):
        dy = y - self.y_min
        if not (0 <= x < self.size and 0 <= z < self.size and 0 <= dy < self.height):
            return 'keep'
        return self.palette[self.blocks[(dy * self.size + z) * self.size + x]]

    def inside(self, x, z):
        return self.mask[z * self.size + x] == 255


class TownFeastLayoutTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.town = Town()
        cls.sites = layout()

    def test_the_layout_is_the_approved_one(self):
        self.assertEqual(set(self.sites), {'W1', 'E2', 'T'})
        self.assertEqual(self.sites['W1'][1], [(x, 1, 89) for x in range(85, 91)])
        self.assertEqual(self.sites['E2'][1], [(x, 1, 89) for x in range(106, 110)])
        turkey = self.sites['T'][1]
        self.assertEqual(turkey[0], (103, 1, 90))
        self.assertEqual({(x, z) for x, _, z in turkey}, set(rect(103, 105, 88, 90)))
        cells = [cell for _, site in self.sites.values() for cell in site]
        self.assertEqual(len(cells), len(set(cells)), 'sites share a cell')

    def test_every_cell_is_open_blueprint_ground_inside_the_wall(self):
        for name, (_, cells) in self.sites.items():
            for x, y, z in cells:
                with self.subTest(site=name, cell=(x, y, z)):
                    self.assertTrue(self.town.inside(x, z))
                    self.assertEqual(self.town.name(x, y, z), 'minecraft:air')
                    self.assertEqual(self.town.name(x, y + 1, z), 'minecraft:air')
                    floor = self.town.name(x, 0, z)
                    self.assertNotIn(floor, ('minecraft:air', 'keep'))
                    self.assertNotIn('grass', floor)

    def test_no_cell_is_a_decor_site_or_a_townspersons_place(self):
        decor = {(b[0], b[1], b[2]) for site in self.town.sites for b in site['blocks']}
        places = set()
        for spot in self.town.spots:
            for key in ('pos', 'work', 'evening', 'bed'):
                if key in spot:
                    places.add(tuple(spot[key]))
        for name, (_, cells) in self.sites.items():
            for cell in cells:
                with self.subTest(site=name, cell=cell):
                    self.assertNotIn(cell, decor)
                    self.assertNotIn(cell, places)
                    self.assertNotIn((cell[0], 1, cell[2]), places)

    def test_measured_clearances_hold(self):
        guards = [(s['pos'][0], s['pos'][2]) for s in self.town.spots if s['role'] in ('guard', 'mecha_guard')]
        flak = [(s['pos'][0], s['pos'][2]) for s in self.town.spots if s['role'] == 'flak_guard']
        for name, (kind, cells) in self.sites.items():
            ground = sorted({(x, z) for x, _, z in cells})
            for area, (area_cells, least) in AREAS.items():
                with self.subTest(site=name, area=area):
                    self.assertGreaterEqual(gap(ground, area_cells), least)
            with self.subTest(site=name, area='main gate road'):
                self.assertGreaterEqual(gap(ground, MAIN_ROAD), 3 if kind == 'table' else 2)
            with self.subTest(site=name, area='guards'):
                self.assertGreaterEqual(gap(ground, guards), 3)
            with self.subTest(site=name, area='flak guards'):
                self.assertGreater(gap(ground, flak), 40)


if __name__ == '__main__':
    unittest.main()
