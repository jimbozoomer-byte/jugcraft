"""Native household tag alternatives retain exact nugget conservation checks."""
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


class HouseholdRecipeAuditTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.data = ROOT / 'src/main/resources/data'
        cls.registered = checker.registered_items()
        cls.recipes = {name: json.loads((cls.data / 'jugcraft/recipe' / (name + '.json')).read_text(encoding='utf-8'))
                       for name in ('supps_blackboard', 'supps_daub', 'supps_gold_gate')}

    def problems(self, recipes, tag_values=None):
        with tempfile.TemporaryDirectory() as directory:
            data = Path(directory)
            folder = data / 'jugcraft/recipe'
            folder.mkdir(parents=True)
            for name, recipe in recipes.items():
                (folder / (name + '.json')).write_text(json.dumps(recipe), encoding='utf-8')
            for relative in ('jugcraft/tags/item/supps_blackboard_white.json',
                             'jugcraft/tags/item/supps_straw.json', 'minecraft/tags/item/wooden_slabs.json'):
                target = data / relative
                target.parent.mkdir(parents=True, exist_ok=True)
                target.write_bytes((self.data / relative).read_bytes())
            if tag_values is not None:
                (data / 'jugcraft/tags/item/supps_straw.json').write_text(
                    json.dumps({'replace': True, 'values': tag_values}), encoding='utf-8')
            with patch.object(checker, 'DATA', data), patch.object(checker, 'err') as err:
                checker.check_recipes(self.registered)
            return [call.args[0] for call in err.call_args_list]

    def test_installed_native_tag_members_match_the_actual_registry(self):
        with patch.object(checker, 'err') as err:
            self.assertEqual({'minecraft:quartz', 'minecraft:white_dye'},
                             checker.household_tag_items('#jugcraft:supps_blackboard_white'))
            self.assertEqual({'jugcraft:supps_flax', 'minecraft:wheat'},
                             checker.household_tag_items('#jugcraft:supps_straw'))
            self.assertEqual({}, checker.item_units('#jugcraft:supps_blackboard_white'))
            self.assertEqual({}, checker.item_units('#jugcraft:supps_straw'))
            err.assert_not_called()

    def test_pinned_gold_tag_and_source_gate_bill_keep_exact_nugget_units(self):
        reference = checker.recipe_platform_reference()
        self.assertEqual(['minecraft:gold_nugget'], reference['tags']['c:nuggets/gold'])
        self.assertTrue(checker.tag_exists('item', 'c:nuggets/gold'))
        self.assertEqual({'gold': 1}, checker.item_units('#c:nuggets/gold'))
        self.assertEqual({'gold': 9}, checker.item_units('#c:ingots/gold'))
        gate = self.recipes['supps_gold_gate']
        inputs = [gate['key'][symbol] for symbol in ''.join(gate['pattern']) if symbol != ' ']
        self.assertEqual(4, inputs.count('#c:nuggets/gold'))
        self.assertEqual(2, inputs.count('#c:ingots/gold'))
        self.assertEqual(22, sum(sum(checker.item_units(ref).values()) for ref in inputs))
        self.assertEqual(2, gate['result']['count'])

    def test_actual_blackboard_daub_and_gold_gate_recipe_audits_pass(self):
        self.assertEqual([], self.problems(self.recipes))

    def test_four_nuggets_never_count_as_four_ingots(self):
        recipe = {'type': 'minecraft:crafting_shaped', 'pattern': ['11', '11'],
                  'key': {'1': '#c:nuggets/gold'}, 'result': {'id': 'minecraft:gold_nugget', 'count': 4}}
        self.assertEqual([], self.problems({'supps_gold_gate': recipe}))
        recipe['result']['count'] = 5
        self.assertIn('supps_gold_gate: creates metal (4 units in, 5 out)',
                      self.problems({'supps_gold_gate': recipe}))

    def test_tag_alternatives_use_the_cheapest_actual_member(self):
        recipe = {'type': 'minecraft:crafting_shapeless', 'ingredients': ['#jugcraft:supps_straw'],
                  'result': {'id': 'minecraft:gold_nugget'}}
        self.assertEqual([], self.problems({'supps_gold_gate': recipe}, ['minecraft:gold_nugget']))
        self.assertIn('supps_gold_gate: creates metal (0 units in, 1 out)',
                      self.problems({'supps_gold_gate': recipe}, ['minecraft:gold_nugget', 'minecraft:wheat']))

    def test_required_missing_members_empty_tags_and_cycles_are_errors(self):
        recipe = deepcopy(self.recipes['supps_daub'])
        cases = [(['uninstalled:straw'], 'Unknown required household ingredient: uninstalled:straw'),
                 ([{'id': 'uninstalled:straw', 'required': False}],
                  'Recipe uses empty household ingredient tag #jugcraft:supps_straw'),
                 (['#jugcraft:supps_straw'], 'Cyclic household ingredient tag: #jugcraft:supps_straw')]
        for values, expected in cases:
            with self.subTest(values=values):
                self.assertIn(expected, self.problems({'supps_daub': recipe}, values))

    def test_different_metals_do_not_substitute_for_each_other(self):
        recipe = {'type': 'minecraft:crafting_shapeless', 'ingredients': ['#jugcraft:supps_straw'],
                  'result': {'id': 'minecraft:iron_nugget'}}
        # The least total-mass member is iron, but choosing the gold member cannot
        # produce iron. Per-metal minima keep that alternative from hiding a mint.
        self.assertIn('supps_gold_gate: creates metal (0 units in, 1 out)',
                      self.problems({'supps_gold_gate': recipe}, ['minecraft:iron_nugget', 'minecraft:gold_ingot']))


if __name__ == '__main__':
    unittest.main()
