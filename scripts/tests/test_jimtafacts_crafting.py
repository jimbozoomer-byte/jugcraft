"""Distinct native crafting bills, including competitors outside the collection."""
from collections import Counter, defaultdict
import json
from pathlib import Path
import sys
import unittest

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / 'tools'))
from jimtafacts_crafting import distinct


class JimtafactsCraftingTests(unittest.TestCase):
    def test_iron_scaffold_pattern_preserves_bill_and_avoids_portcullis(self):
        from jimtafacts_construction import iron_scaffold_recipe
        scaffold = iron_scaffold_recipe()
        door = json.loads((ROOT / 'src/main/resources/data/jugcraft/recipe/newest/doorworks/iron_portcullis.json').read_text(encoding='utf-8'))

        def grid(recipe):
            return tuple(tuple(recipe['key'].get(c, '') for c in row) for row in recipe['pattern'])

        self.assertEqual(scaffold['type'], 'minecraft:crafting_shaped')
        self.assertEqual(scaffold['result'], {'id': 'jugcraft:jimta_iron_scaffold', 'count': 6})
        cells = grid(scaffold)
        self.assertEqual(Counter(item for row in cells for item in row),
                         Counter({'minecraft:iron_ingot': 3, 'minecraft:iron_bars': 3}))
        self.assertNotEqual(cells, grid(door))
        self.assertNotEqual(tuple(tuple(reversed(row)) for row in cells), grid(door))

    def test_refinement_preserves_callers_and_nine_slot_limit(self):
        original = ['minecraft:brick'] * 3
        self.assertEqual(distinct('jimtafacts_crops/mug', original), original + ['minecraft:stick'])
        self.assertEqual(original, ['minecraft:brick'] * 3)
        with self.assertRaises(ValueError):
            distinct('jimtafacts_crops/mug', ['minecraft:brick'] * 9)

    def test_no_identical_concrete_bills_with_conflicting_shapeless_outputs(self):
        groups = defaultdict(list)
        base = ROOT / 'src/main/resources/data/jugcraft/recipe'
        for path in base.rglob('*.json'):
            row = json.loads(path.read_text(encoding='utf-8'))
            kind = row.get('type')
            if kind == 'minecraft:crafting_shapeless':
                ingredients = row['ingredients']
            elif kind == 'minecraft:crafting_shaped':
                ingredients = [row['key'][c] for line in row['pattern'] for c in line if c != ' ']
            else:
                continue
            if not all(isinstance(x, str) and not x.startswith('#') for x in ingredients):
                continue  # Runtime fixtures cover tags using actual registry matching.
            output = row['result']
            groups[tuple(sorted(ingredients))].append((path.relative_to(base).as_posix(), kind,
                                                       output['id'], output.get('count', 1)))
        conflicts = []
        for ingredients, rows in groups.items():
            if not any('jimta' in row[0] for row in rows):
                continue
            if any(row[1].endswith('shapeless') for row in rows) and len({row[2:] for row in rows}) > 1:
                conflicts.append((ingredients, rows))
        self.assertEqual(conflicts, [])
