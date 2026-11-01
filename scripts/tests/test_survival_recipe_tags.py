"""Optional tag compatibility follows the actual registry without hiding required errors."""
from pathlib import Path
import sys
import unittest

sys.path.insert(0, str(Path(__file__).resolve().parents[2] / 'tools'))
from audit_survival_recipes import expand, reachable


class SurvivalTagTests(unittest.TestCase):
    def test_absent_optional_entries_do_not_become_ingredients(self):
        tags = {'c:material': [{'id': 'minecraft:polished_calcite', 'required': False},
                               {'id': 'absent:material', 'required': False},
                               {'id': '#absent:materials', 'required': False},
                               'jugcraft:stoneworks_polished_calcite']}
        self.assertEqual({'jugcraft:stoneworks_polished_calcite'},
                         expand('#c:material', tags, known_items={'jugcraft:stoneworks_polished_calcite'}))

    def test_present_optional_item_remains_an_allowed_alternative(self):
        tags = {'c:material': [{'id': 'present:material', 'required': False}, 'jugcraft:material']}
        self.assertEqual({'present:material', 'jugcraft:material'},
                         expand('#c:material', tags, known_items={'present:material', 'jugcraft:material'}))

    def test_required_missing_entries_remain_visible_to_diagnostics(self):
        tags = {'c:material': ['minecraft:missing_string', {'id': 'minecraft:missing_default'},
                               {'id': 'jugcraft:missing_required', 'required': True},
                               {'id': 'minecraft:missing_optional', 'required': False}]}
        self.assertEqual({'minecraft:missing_string', 'minecraft:missing_default', 'jugcraft:missing_required'},
                         expand('#c:material', tags, known_items=set()))

    def test_nested_optional_tag_does_not_hide_required_members(self):
        tags = {'outer': [{'id': '#inner', 'required': False}],
                'inner': [{'id': 'missing:optional', 'required': False}, 'jugcraft:missing_required', '#outer']}
        self.assertEqual({'jugcraft:missing_required'}, expand('#outer', tags, known_items=set()))

    def test_all_absent_optional_tag_stays_empty_and_cannot_seed_a_recipe(self):
        tags = {'c:material': [{'id': 'minecraft:missing', 'required': False}]}
        ingredients = expand('#c:material', tags, known_items={'minecraft:stone'})
        self.assertEqual(set(), ingredients)
        self.assertEqual({}, reachable([{'source': 'fixture', 'inputs': [ingredients], 'outputs': ['product']}], {}))


if __name__ == '__main__':
    unittest.main()
