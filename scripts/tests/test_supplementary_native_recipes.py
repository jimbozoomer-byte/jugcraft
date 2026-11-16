"""Original load conditions must not masquerade as missing recipe ingredients."""
import copy
import importlib
import json
from pathlib import Path
import sys
import unittest
from unittest.mock import patch

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / 'tools'))
from newest_supplementaries import S, native_recipe, write_native_recipes


class NativeSupplementaryRecipesTest(unittest.TestCase):
    def source(self, name):
        return json.loads((S / 'data/supplementaries/recipe' / (name + '.json')).read_text(encoding='utf-8'))

    def test_native_flags_and_tag_ingredients_preserve_source_recipes(self):
        for name in ('awnings/awning', 'daub', 'blackboard', 'item_shelf', 'planter', 'fine_wood', 'gold_gate', 'fire_pit'):
            original = self.source(name)
            before = copy.deepcopy(original)
            result = native_recipe(original)
            self.assertIsNotNone(result, name)
            self.assertEqual(original, before)
            self.assertFalse(any('conditions' in key for key in result))
            self.assertEqual(result['pattern'], original['pattern'])
            self.assertEqual(result['result']['count'], original['result']['count'])
        self.assertIn('#jugcraft:supps_straw', native_recipe(self.source('daub'))['key'].values())
        self.assertIn('#jugcraft:supps_blackboard_white', native_recipe(self.source('blackboard'))['key'].values())

    def test_disabled_external_and_unknown_conditions_remain_disabled(self):
        self.assertIsNone(native_recipe(self.source('fine_wood_2')))
        self.assertIsNone(native_recipe(self.source('sconce_ender')))
        self.assertIsNone(native_recipe(self.source('fire_pit_2')))
        value=self.source('item_shelf')
        value['fabric:load_conditions']=[{'condition':'fabric:not','value':{'condition':'future:unknown'}}]
        self.assertIsNone(native_recipe(value))
        value['fabric:load_conditions']=[{'condition':'supplementaries:flag','flag':'not_implemented'}]
        self.assertIsNone(native_recipe(value))
        value['fabric:load_conditions']=[]
        value.pop('neoforge:conditions',None)
        value['key']['0']={'item':'uninstalled:missing_plank'}
        self.assertIsNone(native_recipe(value))

    def test_bounded_writer_writes_only_native_recipes(self):
        rows=[]
        paths=write_native_recipes(lambda path,value: rows.append((path,value)))
        self.assertEqual(paths,[path for path,_ in rows])
        self.assertGreater(len(paths),100)
        self.assertEqual(len(paths),len(set(paths)))
        for path,value in rows:
            self.assertTrue(path.is_relative_to(ROOT/'src/main/resources/data/jugcraft/recipe'))
            self.assertTrue(path.name.startswith('supps_') or path.parent.name.startswith('supps_'))
            self.assertTrue(value['type'].startswith('minecraft:'))

    def test_import_does_not_generate_household_or_field_resources(self):
        import newest_supplementaries
        with patch('newest_building.write') as writer, patch('newest_building.copy') as copier:
            importlib.reload(newest_supplementaries)
            writer.assert_not_called()
            copier.assert_not_called()
        importlib.reload(newest_supplementaries)


if __name__=='__main__':
    unittest.main()
