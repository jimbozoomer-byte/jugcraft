"""Audit native shaped upgrade costs without turning admin stores into sources."""
from collections import defaultdict
import json
from pathlib import Path
import sys
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / 'tools'))
from survival_newest_storage_routes import extend, PERSONAL_CONTAINERS


class NewestStorageRoutesTests(unittest.TestCase):
    def test_native_upgrade_keeps_each_pattern_slot_and_crafting_station(self):
        rows = []
        extend(ROOT, lambda *row: rows.append(row), defaultdict(list), set())
        row = next(r for r in rows if r[0].endswith('/advanced_alchemy_upgrade.json'))
        self.assertEqual(['#c:gems/diamond', '#c:ingots/gold', 'jugcraft:packworks_alchemy_upgrade',
                          '#c:ingots/gold', '#c:dusts/redstone', '#c:dusts/redstone',
                          '#c:dusts/redstone', 'minecraft:crafting_table'], row[2])
        self.assertEqual(['jugcraft:packworks_advanced_alchemy_upgrade'], row[3])
        self.assertTrue(all(r[1] == 'component_preserving_shaped_upgrade' for r in rows))
        self.assertGreater(len(rows), 20)

    def test_no_generic_upgrade_or_administrative_source_is_invented(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            folder = root / 'src/main/resources/data/jugcraft/recipe/newest/packworks'
            folder.mkdir(parents=True)
            (folder / 'unknown.json').write_text(json.dumps({'type': 'unported:upgrade'}))
            rows, evidence = [], defaultdict(list)
            registered = {'jugcraft:personal_loot_' + n for n in PERSONAL_CONTAINERS}
            registered |= {'jugcraft:packworks_infinity_upgrade', 'jugcraft:packworks_survival_infinity_upgrade',
                           'jugcraft:packworks_ordinary_upgrade', 'jugcraft:personal_loot_unrecognized'}
            excluded = extend(root, lambda *row: rows.append(row), evidence, registered)
            self.assertEqual([], rows)
            self.assertEqual({}, evidence)
            self.assertEqual(16, len(excluded))
            self.assertNotIn('jugcraft:packworks_ordinary_upgrade', excluded)
            self.assertNotIn('jugcraft:personal_loot_unrecognized', excluded)

    def test_personal_containers_have_native_empty_drop_tables(self):
        for name in PERSONAL_CONTAINERS:
            if name in ('minecart', 'item_frame'):
                continue
            path = ROOT / 'src/main/resources/data/jugcraft/loot_table/blocks' / ('personal_loot_' + name + '.json')
            self.assertEqual([], json.loads(path.read_text(encoding='utf-8'))['pools'])


if __name__ == '__main__':
    unittest.main()
