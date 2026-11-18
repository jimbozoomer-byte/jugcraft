import sys
import json
import subprocess
import tempfile
import unittest
from pathlib import Path

TOOLS = Path(__file__).resolve().parents[2] / "tools"
sys.path.insert(0, str(TOOLS))
from nouveau_nbt import read, write
from prepare_climate_migration import migrate


class ClimateMigrationTests(unittest.TestCase):
    def setUp(self):
        self.source = {'type': (8, 'minecraft:multi_noise'), 'preset': (8, 'minecraft:overworld')}
        self.generator = {'type': (8, 'minecraft:noise'), 'settings': (8, 'minecraft:overworld'),
                          'biome_source': (10, self.source)}
        self.dimensions = {'minecraft:overworld': (10, {'type': (8, 'minecraft:overworld'), 'generator': (10, self.generator)}),
                           'minecraft:the_nether': (10, {'preserve_me': (12, [1, -3, 9223372036854775807])}),
                           'jugcraft:custom': (10, {'unknown': (7, b'\x00\xff')})}
        self.root = (10, '', {'DataVersion': (3, 5023), 'data': (10, {'seed': (4, -12345),
                    'dimensions': (10, self.dimensions), 'generate_structures': (1, 1)})})
        self.town = (10, '', {'data': (10, {'origin': (11, [1296, 114, 400]), 'built': (12, [17, 99])})})

    def run_migration(self):
        return migrate(write(self.root), write(self.town))

    def test_only_wraps_biome_source(self):
        raw, report = self.run_migration()
        changed = read(raw)
        g = changed[2]['data'][1]['dimensions'][1]['minecraft:overworld'][1]['generator'][1]
        wrapped = g['biome_source'][1]
        self.assertEqual(wrapped['type'], (8, 'jugcraft:climate'))
        self.assertEqual(wrapped['base'], (10, self.source))
        g['biome_source'] = wrapped['base']
        self.assertEqual(changed, self.root)
        self.assertEqual(report['boundaries_z'], {'north_frigid': -2100, 'hot_start': 2400, 'temperate_start': 4400, 'south_frigid': 5900})

    def test_idempotent_bytes(self):
        raw, _ = self.run_migration()
        again, report = migrate(raw, write(self.town))
        self.assertEqual(again, raw)
        self.assertEqual(report['status'], 'already_migrated')

    def test_anchor_change_refused(self):
        raw, _ = self.run_migration()
        self.town[2]['data'][1]['origin'] = (11, [1296, 114, 800])
        with self.assertRaisesRegex(ValueError, 'differs'):
            migrate(raw, write(self.town))

    def test_existing_wrapper_cannot_hide_custom_base(self):
        raw, _ = self.run_migration()
        tree = read(raw)
        source = tree[2]['data'][1]['dimensions'][1]['minecraft:overworld'][1]['generator'][1]['biome_source'][1]
        source['base'][1]['type'] = (8, 'other:custom')
        with self.assertRaisesRegex(ValueError, 'unexpected base'):
            migrate(write(tree), write(self.town))

    def test_cli_bundle_preserves_inputs_and_refuses_overwrite(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            original = write(self.root)
            world_gen, town = root / 'world_gen_settings.dat', root / 'town.dat'
            world_gen.write_bytes(original)
            town.write_bytes(write(self.town))
            command = [sys.executable, str(TOOLS / 'prepare_climate_migration.py'),
                       '--world-gen', str(world_gen), '--town', str(town), '--out']
            result = subprocess.run(command + [str(root / 'prepared')], capture_output=True)
            self.assertEqual(result.returncode, 0, result.stderr)
            self.assertEqual(world_gen.read_bytes(), original)
            self.assertEqual((root / 'prepared/world_gen_settings.original.dat').read_bytes(), original)
            report = json.loads((root / 'prepared/migration.json').read_text(encoding='utf-8'))
            self.assertFalse(report['installed'])
            self.assertEqual(report['target'], 'world/data/minecraft/world_gen_settings.dat')
            prepared = (root / 'prepared/world_gen_settings.dat').read_bytes()
            self.assertNotEqual(subprocess.run(command + [str(root / 'prepared')], capture_output=True).returncode, 0)
            self.assertEqual((root / 'prepared/world_gen_settings.dat').read_bytes(), prepared)
            self.assertNotEqual(subprocess.run(command + [str(root)], capture_output=True).returncode, 0)
            self.assertEqual(world_gen.read_bytes(), original)

    def test_refuses_other_data_version(self):
        self.root[2]['DataVersion'] = (3, 5000)
        with self.assertRaisesRegex(ValueError, '26.3'):
            self.run_migration()

    def test_refuses_designed_and_custom_sources(self):
        for source in ('jugcraft:design', 'minecraft:fixed', 'other:custom'):
            self.source['type'] = (8, source)
            with self.assertRaisesRegex(ValueError, 'Unexpected biome source'):
                self.run_migration()

    def test_refuses_wrong_preset(self):
        self.source['preset'] = (8, 'minecraft:nether')
        with self.assertRaisesRegex(ValueError, 'Unexpected biome source'):
            self.run_migration()

    def test_refuses_non_noise_generator(self):
        self.generator['type'] = (8, 'minecraft:flat')
        with self.assertRaisesRegex(ValueError, 'noise Overworld'):
            self.run_migration()

    def test_invalid_or_missing_origin_fails_closed(self):
        for invalid in ((11, [1, 2]), (11, [30_000_000, 70, 0]), (8, '0,70,0')):
            self.town[2]['data'][1]['origin'] = invalid
            with self.assertRaises(ValueError):
                self.run_migration()
        del self.town[2]['data'][1]['origin']
        with self.assertRaises(KeyError):
            self.run_migration()


if __name__ == '__main__':
    unittest.main()
