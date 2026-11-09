"""Data regressions for the pirate ship crew (docs/features/pirate-ship-crews.md).

The static galleon stays byte-for-byte as it is, the standard ten stations keep the layout the ship's art and
renderer are built to, the crew's server tests and test-only stub vessel are registered, the crew ask Authority's
stand-in, and their cannonballs spare pirate hulls. Runtime behavior is covered by PirateCrewGameTests.
"""
import hashlib
import json
from pathlib import Path
import re
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[2]
DATA = ROOT / 'src/main/resources/data/jugcraft'
CREW = ROOT / 'src/main/java/io/github/jimbozoomer/jugcraft/menagerie/PirateCrew.java'
# The 36 static-galleon data files (17 templates, structure, structure set, 10 pools, processor list, 6 loot tables)
# as of origin/main 8075c6d58b. Moving pirate ships must not change them. JSON is hashed with LF line endings, so a
# Windows (CRLF) checkout of unchanged files matches; NBT and anything else binary is hashed raw.
GALLEON_DIGEST = 'f89a28a6a6b62fad028c07a737b83738c65afa16433f5a3e18381491c66af799'


def galleon_files():
    return sorted([*DATA.glob('structure/supps_galleon/*.nbt'), DATA / 'worldgen/structure/supps_galleon.json',
                   DATA / 'worldgen/structure_set/supps_galleons.json',
                   *DATA.glob('worldgen/template_pool/supps_galleon/*.json'),
                   DATA / 'worldgen/processor_list/supps_galleon_data.json',
                   *DATA.glob('loot_table/supps_loot/galleon/*.json')])


def content(path):
    data = path.read_bytes()
    return data.replace(b'\r\n', b'\n') if path.suffix == '.json' else data


def stations():
    text = CREW.read_text(encoding='utf-8')
    block = text[text.index('STATIONS = List.of('):text.index('LOCAL_DECK')]
    number = r'(-?[0-9.]+)'
    vec = rf'new Vec3\({number}, {number}, {number}\)'
    pattern = rf'new Station\(Role\.(\w+), {vec}, {number}F, {number}F, {vec}\)'
    return [(role, tuple(map(float, (a, b, c))), float(facing), float(arc), tuple(map(float, (d, e, f))))
            for role, a, b, c, facing, arc, d, e, f in re.findall(pattern, block)]


class PirateCrewDataTests(unittest.TestCase):
    def test_static_galleon_data_is_unchanged(self):
        files = galleon_files()
        self.assertEqual(36, len(files))
        digest = hashlib.sha256()
        for path in files:
            digest.update((path.relative_to(DATA).as_posix() + ' '
                           + hashlib.sha256(content(path)).hexdigest() + '\n').encode())
        self.assertEqual(GALLEON_DIGEST, digest.hexdigest())

    def test_crlf_json_hashes_as_lf_and_nbt_stays_raw(self):
        source = DATA / 'worldgen/structure/supps_galleon.json'
        nbt = sorted(DATA.glob('structure/supps_galleon/*.nbt'))[0]
        with tempfile.TemporaryDirectory() as folder:
            crlf = Path(folder) / 'supps_galleon.json'
            crlf.write_bytes(source.read_bytes().replace(b'\r\n', b'\n').replace(b'\n', b'\r\n'))
            self.assertIn(b'\r\n', crlf.read_bytes())
            self.assertEqual(hashlib.sha256(content(source)).hexdigest(), hashlib.sha256(content(crlf)).hexdigest())
            raw = Path(folder) / 'template.nbt'
            raw.write_bytes(nbt.read_bytes() + b'\r\n')
            self.assertEqual(nbt.read_bytes() + b'\r\n', content(raw), 'Binary files are hashed exactly as they are')

    def test_standard_stations_keep_the_agreed_layout(self):
        rows = stations()
        self.assertEqual(['HELM', 'LOOKOUT'] + ['GUNNER'] * 4 + ['DECK'] * 4, [row[0] for row in rows])
        self.assertEqual((0.0, 1.0, -6.0), rows[0][1])
        self.assertEqual((0.0, 9.5, 1.0), rows[1][1])
        for role, seat, facing, arc, muzzle in rows[2:6]:
            port = seat[0] > 0
            self.assertEqual((2.0 if port else -2.0, 1.0), seat[:2])
            self.assertEqual(-90.0 if port else 90.0, facing, 'Port guns face -90, starboard +90')
            self.assertEqual(45.0, arc)
            self.assertEqual((3.2 if port else -3.2, 1.6, seat[2]), muzzle, 'Muzzle outboard of its own gunner')
        self.assertEqual({(1.0, 1.0, 5.5), (-1.0, 1.0, 5.5), (1.0, 1.0, -3.5), (-1.0, 1.0, -3.5)},
                         {row[1] for row in rows[6:]})
        for role, seat, facing, arc, muzzle in rows:
            self.assertTrue(abs(seat[0]) <= 3.0 and abs(seat[2]) <= 8.0, 'Every seat is on the 6 x 16 hull')

    def test_crew_tests_and_stub_vessel_are_registered(self):
        entry = json.loads((ROOT / 'src/gametest/resources/fabric.mod.json').read_text(encoding='utf-8'))['entrypoints']
        self.assertEqual(1, entry['fabric-gametest'].count('io.github.jimbozoomer.jugcraft.test.PirateCrewGameTests'))
        self.assertIn('io.github.jimbozoomer.jugcraft.test.TestVessels', entry['main'])
        self.assertIn('io.github.jimbozoomer.jugcraft.test.TestVesselsClient', entry['client'])

    def test_crew_code_adds_no_loot_and_stays_out_of_city_raid_and_town_code(self):
        text = CREW.read_text(encoding='utf-8')
        for forbidden in ('LootTable', 'loot_table', 'dropContents', 'raiders.'):
            self.assertNotIn(forbidden, text)
        self.assertNotIn('pirate', ' '.join(p.name.lower() for p in (ROOT / 'src/main/java/io/github/jimbozoomer/jugcraft/town').glob('*.java')))

    def test_crew_asks_authority_and_spares_its_own_hull(self):
        text = CREW.read_text(encoding='utf-8')
        self.assertNotIn('FakePlayer', text, 'Only concordance/Authority makes stand-ins')
        self.assertIn('Authority.pirateCrewActor(level)', text)
        ball = (CREW.parent / 'OwnerCannonball.java').read_text(encoding='utf-8')
        for name in ('"pirate_ship"', '"pirate_ship_deck"'):
            self.assertIn(name, ball, 'Crew cannonballs never hit a pirate ship hull or deck')


if __name__ == '__main__':
    unittest.main()
