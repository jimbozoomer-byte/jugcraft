"""Ritual serializer names must not become materials or join unrelated recipes."""
import json
from pathlib import Path
import sys
import tempfile
import unittest
from unittest.mock import patch

sys.path.insert(0, str(Path(__file__).resolve().parents[2] / 'tools'))


class ArcaneRecipeGraphTests(unittest.TestCase):
    def test_actual_results_and_paid_bases_are_kept_without_serializer_bridges(self):
        import check_mod_data as checker
        with tempfile.TemporaryDirectory() as temp:
            data = Path(temp) / 'data'
            folder = data / 'jugcraft/arcane_ritual'
            folder.mkdir(parents=True)
            for name, base, output, result_type in (
                    ('dust', 'minecraft:bone_meal', {'id': 'jugcraft:forbidden_dragon_scale'}, 'create_item'),
                    ('wool', 'minecraft:white_wool', 'jugcraft:forbidden_arcane_crystal', 'create_item'),
                    ('tool', 'minecraft:netherite_axe', 'jugcraft:forbidden_draco_arcanus_axe', 'transmute_input')):
                (folder / (name + '.json')).write_text(json.dumps({
                    'magic_circle': 'jugcraft:forbidden_create_item',
                    'main_ingredient': {'item': base},
                    'inputs': [{'amount': 1, 'ingredient': {'item': 'minecraft:diamond'}}],
                    'result': {'type': 'jugcraft:forbidden_' + result_type, 'result_item': output}}))
            with patch.object(checker, 'DATA', data):
                edges = checker._read_recipe_edges()
            self.assertEqual(3, len(edges))
            self.assertEqual({'jugcraft:forbidden_dragon_scale', 'jugcraft:forbidden_arcane_crystal',
                              'jugcraft:forbidden_draco_arcanus_axe'}, set().union(*(made for _, made in edges)))
            self.assertTrue(all('minecraft:diamond' in takes for takes, _ in edges))
            self.assertTrue(any('minecraft:netherite_axe' in takes and 'jugcraft:forbidden_draco_arcanus_axe' in made
                                for takes, made in edges))
            with patch.object(checker, '_recipe_edges', return_value=edges):
                self.assertEqual({'jugcraft:forbidden_dragon_scale'}, checker._made_from({'minecraft:bone_meal'}, False))
                self.assertEqual({'jugcraft:forbidden_arcane_crystal'}, checker._made_from({'minecraft:white_wool'}, False))
            self.assertFalse(any('jugcraft:forbidden_create_item' in takes | made for takes, made in edges))


if __name__ == '__main__':
    unittest.main()
