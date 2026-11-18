"""Native Ribbits wood recipes retain the ordinary item and metal-safety audit."""
from copy import deepcopy
import json
from pathlib import Path
import sys
import tempfile
import unittest
from unittest.mock import patch

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / 'tools'))
import check_mod_data as checker
import newest_registry
import newest_ribbits


class RibbitsRecipeAuditTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.recipes = {p.stem: json.loads(p.read_text(encoding='utf-8')) for p in
                       (ROOT / 'src/main/resources/data/jugcraft/recipe').glob('ribbits_*.json')}
        cls.registered = newest_registry.entries()['item']

    def problems(self, recipes, registered=None):
        with tempfile.TemporaryDirectory() as directory:
            data = Path(directory)
            folder = data / 'jugcraft/recipe'
            folder.mkdir(parents=True)
            for name, recipe in recipes.items():
                (folder / (name + '.json')).write_text(json.dumps(recipe), encoding='utf-8')
            with patch.object(checker, 'DATA', data), patch.object(checker, 'err') as err:
                checker.check_recipes(self.registered if registered is None else registered)
            return [call.args[0] for call in err.call_args_list]

    def test_all_eleven_actual_native_wood_recipes_pass_without_fictitious_switches(self):
        self.assertEqual(11, len(self.recipes))
        self.assertEqual(set(checker.RIBBITS_WOOD_RECIPE_OUTPUTS), set(self.recipes))
        self.assertEqual(6, len(checker.RIBBITS_WOOD_ITEMS))
        self.assertTrue(checker.RIBBITS_WOOD_ITEMS <= self.registered)
        self.assertEqual(set(checker.RIBBITS_WOOD_ITEMS),
                         {r['result']['id'].removeprefix('jugcraft:') for r in self.recipes.values()})
        self.assertTrue(all(not r.get('fabric:load_conditions') for r in self.recipes.values()))
        imported = {}
        newest_ribbits.import_data_resources(writer=lambda p, v: imported.setdefault(p, v))
        for name, recipe in self.recipes.items():
            path = ROOT / 'src/main/resources/data/jugcraft/recipe' / (name + '.json')
            self.assertEqual(imported[path], recipe, 'Audit the actual preserved-source recipe bills')
        self.assertEqual([], self.problems(self.recipes))

    def test_registration_has_no_ribbits_feature_switch_to_fake_in_recipes(self):
        source = (ROOT / 'src/main/java/io/github/jimbozoomer/jugcraft/ribbits/OwnerRibbits.java').read_text()
        self.assertIn('public static void register()', source)
        self.assertNotIn('isFeatureEnabled', source)
        initializer = (ROOT / 'src/main/java/io/github/jimbozoomer/jugcraft/Jugcraft.java').read_text()
        self.assertIn('io.github.jimbozoomer.jugcraft.ribbits.OwnerRibbits.register();', initializer)

    def test_future_names_and_unregistered_results_do_not_inherit_the_exception(self):
        door = self.recipes['ribbits_mossy_oak_door']
        self.assertIn('ribbits_future_recipe: missing feature switch condition',
                      self.problems({'ribbits_future_recipe': door}))
        errors = self.problems({'ribbits_mossy_oak_door': door},
                               self.registered - {'ribbits_mossy_oak_door'})
        self.assertIn('ribbits_mossy_oak_door: missing feature switch condition', errors)
        self.assertIn('ribbits_mossy_oak_door: unknown result jugcraft:ribbits_mossy_oak_door', errors)

    def test_registered_inputs_and_metal_conservation_remain_checked(self):
        door = deepcopy(self.recipes['ribbits_mossy_oak_door'])
        door['key']['#'] = 'jugcraft:ribbits_unregistered_wood'
        errors = self.problems({'ribbits_mossy_oak_door': door})
        self.assertIn('ribbits_mossy_oak_door: unknown item jugcraft:ribbits_unregistered_wood', errors)
        self.assertIn('No metal content known for jugcraft:ribbits_unregistered_wood', errors)
        mint = deepcopy(self.recipes['ribbits_mossy_oak_door'])
        mint['result'] = {'id': 'minecraft:iron_ingot'}
        errors = self.problems({'ribbits_mossy_oak_door': mint})
        self.assertIn('ribbits_mossy_oak_door: missing feature switch condition', errors)
        self.assertIn('ribbits_mossy_oak_door: creates metal (0 units in, 9 out)', errors)

    def test_only_actual_wood_ids_have_zero_metal_accounting(self):
        with patch.object(checker, 'err') as err:
            for item in checker.RIBBITS_WOOD_ITEMS:
                self.assertEqual({}, checker.item_units('jugcraft:' + item))
            self.assertEqual({'iron': 9}, checker.item_units('minecraft:iron_ingot'))
            self.assertEqual({'gold': 1}, checker.item_units('#c:nuggets/gold'))
            err.assert_not_called()
            checker.item_units('jugcraft:ribbits_gold_ingot')
            err.assert_called_once_with('No metal content known for jugcraft:ribbits_gold_ingot')


if __name__ == '__main__':
    unittest.main()
