"""Acquisition graph regressions for existing native Supplementaries actions."""
from collections import defaultdict
import json
from pathlib import Path
import sys
import unittest

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / 'tools'))
from survival_supps_routes import extend
from audit_survival_recipes import reachable


class SupplementariesRoutesTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.rows, cls.evidence = [], defaultdict(list)
        cls.items = set(json.loads((ROOT / 'docs/audits/survival-item-registry.json').read_text(encoding='utf-8')))
        cls.exclusions = extend(ROOT, lambda *row: cls.rows.append(row), cls.evidence, cls.items)

    def test_flax_growth_breaks_fiber_block_cycle_but_requires_seed_and_farmland(self):
        row = next(r for r in self.rows if r[3] == ['jugcraft:supps_flax'])
        self.assertEqual(['jugcraft:supps_flax_seeds', 'minecraft:dirt', '#minecraft:hoes'], row[2])
        growth = {'source': row[0], 'inputs': [[n] for n in row[2]], 'outputs': row[3]}
        pack = {'source': 'pack', 'inputs': [['jugcraft:supps_flax']], 'outputs': ['jugcraft:supps_flax_block']}
        unpack = {'source': 'unpack', 'inputs': [['jugcraft:supps_flax_block']], 'outputs': ['jugcraft:supps_flax']}
        seeds = {n: 'source' for n in row[2]}
        self.assertIn('jugcraft:supps_flax_block', reachable([growth, pack, unpack], seeds))
        del seeds['jugcraft:supps_flax_seeds']
        self.assertNotIn('jugcraft:supps_flax', reachable([growth, pack, unpack], seeds))
        self.assertIn('jugcraft:supps_flax_seeds', self.evidence)

    def test_special_crafting_pays_both_inputs_and_has_no_free_evidence(self):
        expected = {
            'jugcraft:supps_rope_arrow': ['minecraft:arrow', 'jugcraft:supps_rope'],
            'jugcraft:supps_suspicious_gravel_bricks': ['jugcraft:supps_gravel_bricks', 'minecraft:stick'],
            'jugcraft:supps_raked_gravel': ['minecraft:gravel', '#minecraft:hoes', 'minecraft:enchanted_book'],
        }
        for output, needs in expected.items():
            self.assertEqual([needs], [r[2] for r in self.rows if r[3] == [output]])
            self.assertNotIn(output, self.evidence)

    def test_globe_cycle_does_not_seed_itself_and_trader_route_pays_emeralds(self):
        routes = [r for r in self.rows if r[3][0].startswith('jugcraft:supps_globe')]
        self.assertEqual(4, len(routes))
        edges = [{'source': r[0], 'inputs': [[i] for i in r[2]], 'outputs': r[3]} for r in routes]
        self.assertEqual({}, reachable(edges, {}))
        reached = reachable(edges, {'minecraft:emerald': 'vanilla'})
        self.assertIn('jugcraft:supps_globe', reached)
        self.assertIn('jugcraft:supps_globe_sepia', reached)

    def test_evidence_points_to_existing_handlers_and_rewards_are_narrow(self):
        self.assertEqual({'wild_flax', 'flax_seeds', 'barnacles', 'music_disc_heave_ho',
                          'blast_armor_trim_smithing_template', 'dragon_banner_pattern',
                          'enderman_head', 'spider_head'},
                         {n.removeprefix('jugcraft:supps_') for n in self.evidence})
        for name, records in self.evidence.items():
            self.assertIn(name, self.items)
            for record in records:
                self.assertTrue((ROOT / record['source']).is_file())
                self.assertTrue(record['note'])
        for source, _, inputs, outputs in self.rows:
            self.assertTrue((ROOT / source).is_file())
            self.assertTrue(inputs)
            self.assertTrue(set(outputs).issubset(self.items))

    def test_missing_compatibility_items_are_not_invented_or_excluded(self):
        offered = set(self.evidence) | {o for r in self.rows for o in r[3]}
        for name in ('present_red', 'trapped_present_blue', 'candle_holder_soul',
                     'propelplant_cane', 'edelwood_stick', 'ashen_basalt'):
            self.assertNotIn('jugcraft:supps_' + name, offered)
        self.assertEqual({}, self.exclusions)


if __name__ == '__main__':
    unittest.main()
