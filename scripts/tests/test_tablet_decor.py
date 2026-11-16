"""Economy boundaries for the reviewed decorative expansion."""
import importlib.util
import json
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[2]
spec = importlib.util.spec_from_file_location('decor', ROOT / 'tools/expand_tablet_decor.py')
decor = importlib.util.module_from_spec(spec)
spec.loader.exec_module(decor)

class TabletDecorTests(unittest.TestCase):
    def test_reviewed_candidates_are_materialized_and_have_models(self):
        tag = json.loads((decor.DATA / 'tags/item/tablet_outputs.json').read_text())['values']
        candidates = decor.candidates()
        self.assertGreater(len(candidates), 500)
        for item in candidates:
            self.assertIn(item, tag)
            model = ROOT / 'src/main/resources/assets/jugcraft/items' / (item.split(':', 1)[1]+'.json')
            self.assertTrue(model.exists(), item)

    def test_prices_and_excluded_categories_remain_separate(self):
        candidates = decor.candidates()
        for item in ('jugcraft:aspen_planks', 'jugcraft:chiselworks_braid/oak_planks',
                     'jugcraft:stoneworks_andesite_bricks'):
            self.assertIn(item, candidates)
        for item in ('minecraft:diamond', 'minecraft:gold_ingot', 'jugcraft:condenser_mk1',
                     'jugcraft:steel_crate', 'jugcraft:chiselworks_braid/gold_block',
                     'jugcraft:stoneworks_quartz_bricks', 'jugcraft:klein_star_1',
                     'jugcraft:cavern_thornwood_planks'):
            self.assertNotIn(item, candidates)
        values = json.loads((decor.DATA / 'alchemy/values.json').read_text())['items']
        for item in candidates:
            self.assertNotIn('fallback', values[item]['source'], item)
            self.assertNotIn('pending', values[item]['source'], item)

if __name__ == '__main__':
    unittest.main()
