"""Native interaction edges retain costs and do not invent missing acquisitions."""
from collections import defaultdict
import json
from pathlib import Path
import sys
import unittest

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / 'tools'))
from survival_newest_interactions import extend


class NewestInteractionsTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.rows, cls.evidence = [], defaultdict(list)
        cls.items = set(json.loads((ROOT / 'docs/audits/survival-item-registry.json').read_text(encoding='utf-8')))
        cls.excluded = extend(ROOT, lambda *row: cls.rows.append(row), cls.evidence, cls.items)

    def test_capture_requires_bucket_and_actual_spawn_membership(self):
        data = json.loads((ROOT / 'src/main/resources/jugcraft/newest_aquatic.json').read_text(encoding='utf-8'))
        spawned = {row['spawn']['type'] + '_bucket' for row in data['spawns'] if row['spawn']['weight'] > 0}
        routes = [r for r in self.rows if r[1].startswith('water_bucket_capture_')]
        self.assertEqual(spawned, {r[3][0] for r in routes})
        self.assertTrue(all(r[2] == ['minecraft:water_bucket'] for r in routes))
        self.assertIn('jugcraft:aquatic_largemouth_bass_bucket', {r[3][0] for r in routes})
        bass = next(row for row in data['spawns'] if row['spawn']['type'] == 'jugcraft:aquatic_largemouth_bass')
        self.assertEqual(['minecraft:river', 'minecraft:swamp'], bass['includeBiomes'])

    def test_fillet_and_worm_routes_retain_real_ingredients(self):
        fillet = next(r for r in self.rows if r[1] == 'native_one_fish_one_knife_crafting')
        self.assertEqual(2, len(fillet[2]))
        self.assertIn('minecraft:cod', fillet[2][0])
        self.assertEqual(6, len(fillet[2][1]))
        self.assertTrue(all(n.endswith('_fillet_knife') for n in fillet[2][1]))
        self.assertNotIn('jugcraft:aquatic_algae', fillet[2][0])
        worm = next(r for r in self.rows if r[3] == ['jugcraft:aquatic_worm'])
        self.assertEqual(['jugcraft:aquatic_worm_farm', 'minecraft:wheat_seeds'], worm[2])

    def test_environmental_evidence_is_narrow_and_source_backed(self):
        shells = {n for n in self.evidence if n.startswith('jugcraft:twigworks_')}
        self.assertEqual({'jugcraft:twigworks_' + color + '_seashell'
                          for color in ('opaline', 'tangerine', 'roseate', 'bronzed')}, shells)
        self.assertNotIn('jugcraft:twigworks_petrified_lichen', self.evidence)
        self.assertNotIn('jugcraft:jugtania_gaia_head', self.evidence)
        for records in self.evidence.values():
            for record in records:
                self.assertTrue((ROOT / record['source']).is_file(), record)
                self.assertTrue(record['note'])
        for row in self.rows:
            self.assertTrue((ROOT / row[0]).is_file(), row)
            self.assertTrue(row[2], row)
            self.assertTrue(set(row[3]).issubset(self.items), row)

    def test_paid_trade_and_cauldron_fill_are_not_free_sources(self):
        maraca = next(r for r in self.rows if r[3] == ['jugcraft:ribbits_maraca'])
        self.assertEqual(['minecraft:amethyst_shard'], maraca[2])
        dye = next(r for r in self.rows if r[3] == ['jugcraft:amends_dye_bottle'])
        self.assertEqual(['minecraft:cauldron', 'minecraft:water_bucket',
                          'minecraft:white_dye', 'minecraft:glass_bottle'], dye[2])

    def test_presentation_exclusions_match_actual_native_drops(self):
        self.assertEqual({'jugcraft:aquatic_farmland', 'jugcraft:wilds_raked_sand',
                          'jugcraft:wilds_red_raked_sand', 'jugcraft:jugtania_solid_vine',
                          'jugcraft:wilds_grant_suns_blessing', 'jugcraft:wilds_sunblock_staff'}, set(self.excluded))
        registry = (ROOT / 'src/main/java/io/github/jimbozoomer/jugcraft/wilds/OwnerWilds.java').read_text()
        self.assertIn('"grant_suns_blessing"->new Item(p)', registry)
        self.assertIn('BLESSING,-1', registry)
        self.assertNotIn('case "sunblock_staff"', registry)
        for item, drop in (('aquatic_farmland', 'minecraft:dirt'),
                           ('wilds_raked_sand', 'minecraft:sand'),
                           ('wilds_red_raked_sand', 'minecraft:red_sand')):
            path = ROOT / 'src/main/resources/data/jugcraft/loot_table/blocks' / (item + '.json')
            data = json.loads(path.read_text(encoding='utf-8'))
            self.assertEqual([drop], [e['name'] for pool in data['pools'] for e in pool['entries']])
        vine = ROOT / 'src/main/resources/data/jugcraft/loot_table/blocks/jugtania_solid_vine.json'
        data = json.loads(vine.read_text(encoding='utf-8'))
        self.assertEqual([{'type': 'minecraft:loot_table', 'value': 'minecraft:blocks/vine'}],
                         data['pools'][0]['entries'])


if __name__ == '__main__':
    unittest.main()
