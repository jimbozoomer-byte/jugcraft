"""Keep native acquisition auditing faithful to implemented station requirements."""
from collections import defaultdict
import json
from pathlib import Path
import sys
import unittest

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / 'tools'))
from survival_newest_routes import extend


class NativeAcquisitionRoutesTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.items = set(json.loads((ROOT / 'docs/audits/survival-item-registry.json').read_text(encoding='utf-8')))
        cls.routes = []
        cls.evidence = defaultdict(list)
        cls.exclusions = extend(ROOT, lambda source, kind, inputs, outputs:
                                cls.routes.append((source, kind, inputs, outputs)), cls.evidence, cls.items)

    def produced(self, item, kind):
        return [r for r in self.routes if item in r[3] and r[1] == kind]

    def test_botanical_stations_and_catalysts_are_not_free_inputs(self):
        daisy = self.produced('jugcraft:jugtania_livingrock', 'native_botanical_pure_daisy')[0]
        self.assertEqual(daisy[2], ['jugcraft:jugtania_pure_daisy', 'minecraft:stone'])
        endoflame = self.produced('jugcraft:jugtania_endoflame', 'native_botanical_petal_apothecary')[0]
        self.assertIn('minecraft:water_bucket', endoflame[2])
        self.assertTrue(any(isinstance(x, list) and 'jugcraft:jugtania_apothecary_default' in x for x in endoflame[2]))
        for source, kind, inputs, _ in self.routes:
            if kind != 'native_botanical_mana_infusion':
                continue
            recipe = json.loads((ROOT / source).read_text(encoding='utf-8'))
            if recipe.get('mana', 0):
                self.assertIn('jugcraft:jugtania_endoflame', inputs)
                self.assertIn('jugcraft:jugtania_mana_spreader', inputs)
                self.assertIn('#minecraft:coals', inputs)
            if recipe.get('catalyst'):
                self.assertIn(recipe['catalyst']['block'], inputs)
        self.assertEqual(len(self.evidence), 16)
        self.assertTrue(all('mystical_flower' in item for item in self.evidence))

    def test_arcane_progression_preserves_forge_essences_and_enhancers(self):
        upgrade = self.produced('jugcraft:forbidden_hephaestus_forge_tier_3', 'native_arcane_ritual')[0]
        self.assertIn(['jugcraft:forbidden_hephaestus_forge_tier_2'], upgrade[2])
        self.assertIn('jugcraft:forbidden_blood_test_tube', upgrade[2])
        self.assertIn('jugcraft:forbidden_aureal_bottle', upgrade[2])
        self.assertIn('#jugcraft:forbidden_blacksmith_gavel', upgrade[2])
        self.assertIn('jugcraft:forbidden_gilded_chiseled_polished_darkstone', upgrade[2])
        prism = self.produced('jugcraft:forbidden_sea_prism', 'native_arcane_ritual')[0]
        self.assertIn('jugcraft:forbidden_elementarium', prism[2])
        self.assertIn(['jugcraft:forbidden_hephaestus_forge_tier_3',
                       'jugcraft:forbidden_hephaestus_forge_tier_4',
                       'jugcraft:forbidden_hephaestus_forge_tier_5'], prism[2])
        steel = self.produced('jugcraft:forbidden_obsidiansteel_ingot', 'native_clibano_combustion')[0]
        self.assertIn('jugcraft:forbidden_artisan_relic', steel[2])
        self.assertIn('jugcraft:forbidden_clibano_core', steel[2])
        self.assertIn({'item': 'minecraft:obsidian'}, steel[2])
        for _, kind, inputs, _ in self.routes:
            if kind == 'recover_clibano_residue':
                self.assertIn(['#jugcraft:forbidden_clibano/creates_soul_fire',
                               '#jugcraft:forbidden_clibano/creates_enchanted_fire'], inputs)

    def test_routes_are_sourced_and_actual_gaps_stay_visible(self):
        self.assertGreater(len(self.routes), 320)
        for source, _, inputs, outputs in self.routes:
            self.assertTrue((ROOT / source).is_file(), source)
            self.assertTrue(inputs, source)
            self.assertTrue(outputs, source)
            self.assertNotIn(None, outputs, source)
            for item in outputs:
                if item.startswith('jugcraft:'):
                    self.assertIn(item, self.items, source)
        self.assertEqual(len(self.exclusions), 7)
        self.assertIn('jugcraft:forbidden_magical_farmland', self.exclusions)
        farmland = json.loads((ROOT / 'src/main/resources/data/jugcraft/loot_table/blocks/forbidden_magical_farmland.json').read_text())
        self.assertEqual(['minecraft:dirt'], [entry['name'] for pool in farmland['pools'] for entry in pool['entries']])
        self.assertNotIn('jugcraft:jugtania_stripped_livingwood_log', self.exclusions)
        self.assertNotIn('jugcraft:supps_awning', self.exclusions)
        self.assertNotIn('jugcraft:forbidden_draco_arcanus_staff', self.exclusions)


if __name__ == '__main__':
    unittest.main()
