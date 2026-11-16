"""Ribbit village loot and crafting retain source content under native runtime paths."""
import json
from pathlib import Path
import sys
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / 'tools'))
import newest_ribbits as ribbits
import nouveau_nbt


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value), encoding='utf-8')


class RibbitsDataTests(unittest.TestCase):
    def test_supplied_data_resolves_real_village_chests_and_native_block_drops(self):
        originals = {p: p.read_bytes() for folder in ('loot_table', 'recipe', 'advancement')
                     for p in (ribbits.D / folder).rglob('*.json')}
        with tempfile.TemporaryDirectory() as directory:
            resources = Path(directory)
            outputs = ribbits.import_data_resources(resources=resources)
            self.assertEqual(37, len(outputs))
            self.assertEqual(set(outputs), set(resources.rglob('*' + '.json')))
            data = resources / 'data/jugcraft'
            self.assertEqual(20, len(list((data / 'loot_table').rglob('*.json'))))
            self.assertEqual(11, len(list((data / 'recipe').rglob('*.json'))))
            self.assertEqual(6, len(list((data / 'advancement').rglob('*.json'))))
            expected = {'jugcraft:ribbits_chests/' + name for name in
                        ('fisherman_main', 'fisherman_storage', 'gardener', 'merchant', 'nitwit', 'sorcerer')}
            links = []
            for template in (ROOT / 'src/main/resources/data/jugcraft/structure').rglob('*.nbt'):
                if not template.relative_to(ROOT / 'src/main/resources/data/jugcraft/structure').as_posix().startswith('ribbits_'):
                    continue
                root = nouveau_nbt.read(template.read_bytes())[2]
                for block in root.get('blocks', (9, (10, [])))[1][1]:
                    table = block.get('nbt', (10, {}))[1].get('LootTable', (8, ''))[1]
                    if table.startswith('jugcraft:ribbits_'):
                        links.append(table)
                        self.assertTrue((data / 'loot_table' / (table.split(':', 1)[1] + '.json')).is_file(), table)
            self.assertEqual(expected, set(links), 'All six authored village chest types must have actual template bindings')
            self.assertGreaterEqual(len(links), 59, 'Exercise the previously broken template links')
            for source in (ribbits.D / 'loot_table/blocks').glob('*.json'):
                self.assertTrue((data / 'loot_table/blocks' / ('ribbits_' + source.name)).is_file())
            self.assertFalse((data / 'loot_table/ribbits_blocks').exists(), 'Block drops are not free-standing loot roots')
            chest = json.loads((data / 'loot_table/ribbits_chests/fisherman_main.json').read_text())
            entry = chest['pools'][0]['entries'][0]
            self.assertNotIn('functions', entry)
            self.assertEqual('minecraft:set_count', entry['modifier'][0]['type'])
            block = json.loads((data / 'loot_table/blocks/ribbits_swamp_lantern.json').read_text())
            self.assertEqual('jugcraft:ribbits_swamp_lantern', block['pools'][0]['entries'][0]['name'])
            self.assertEqual({'type': 'minecraft:survives_explosion'}, block['pools'][0]['condition'])
            for name, weight in (('fisherman_main', 15), ('sorcerer', 1)):
                table = json.loads((data / 'loot_table/ribbits_chests' / (name + '.json')).read_text())
                empty = [entry for pool in table['pools'] for entry in pool['entries']
                         if entry.get('type') == 'minecraft:empty']
                self.assertEqual([{'type': 'minecraft:empty', 'weight': weight}], empty)
                self.assertNotIn('minecraft:air', json.dumps(table))
            door = json.loads((data / 'recipe/ribbits_mossy_oak_door.json').read_text())
            self.assertEqual({'#': 'jugcraft:ribbits_mossy_oak_planks'}, door['key'])
            self.assertEqual({'id': 'jugcraft:ribbits_mossy_oak_door', 'count': 3}, door['result'])
            for path in (data / 'advancement').rglob('*.json'):
                advancement = json.loads(path.read_text())
                for reward in advancement['rewards']['recipes']:
                    self.assertTrue((data / 'recipe' / (reward.split(':', 1)[1] + '.json')).is_file())
                unlocked = advancement['criteria']['has_the_recipe']['conditions']
                self.assertEqual({'recipes': advancement['rewards']['recipes'][0]}, unlocked)
                self.assertEqual(set(advancement['criteria']),
                                 {key for group in advancement['requirements'] for key in group})
            self.assertFalse(any('ribbits:' in p.read_text() for p in outputs))
        self.assertEqual(originals, {p: p.read_bytes() for p in originals}, 'Preserved source data is never rewritten')

    def test_both_folder_formats_deduplicate_and_preserve_typed_loot_links(self):
        with tempfile.TemporaryDirectory() as directory:
            source, resources = Path(directory) / 'source', Path(directory) / 'resources'
            block = {'type': 'minecraft:block', 'pools': []}
            for folder in ('loot_tables', 'loot_table'):
                write_json(source / folder / 'blocks/test.json', block)
            write_json(source / 'loot_tables/chests/linked.json', {'pools': [{'rolls': 1, 'entries': [
                {'type': 'minecraft:loot_table', 'name': 'ribbits:blocks/test'},
                {'type': 'minecraft:loot_table', 'value': 'ribbits:chests/linked'}]}]})
            craft = {'type': 'minecraft:crafting_shapeless', 'ingredients': [{'item': 'minecraft:oak_planks'}],
                     'result': {'item': 'ribbits:test'}}
            write_json(source / 'recipes/test.json', craft)
            write_json(source / 'recipe/test.json', craft)
            write_json(source / 'advancements/test.json', {'rewards': {'recipes': ['ribbits:test']}})
            outputs = ribbits.import_data_resources(source, resources)
            self.assertEqual(4, len(outputs))
            linked = json.loads((resources / 'data/jugcraft/loot_table/ribbits_chests/linked.json').read_text())
            self.assertEqual(['jugcraft:blocks/ribbits_test', 'jugcraft:ribbits_chests/linked'],
                             [e['value'] for e in linked['pools'][0]['entries']])
            written = {}
            write_json(source / 'loot_table/blocks/test.json', {'type': 'minecraft:block', 'pools': [{'rolls': 2}]})
            with self.assertRaisesRegex(ValueError, 'Conflicting Ribbits loot_table'):
                ribbits.import_data_resources(source, resources, lambda p, v: written.setdefault(p, v))
            self.assertFalse(written, 'Conflicting author definitions fail before any output writes')

    def test_custom_recipe_serializer_is_not_silently_imported(self):
        with tempfile.TemporaryDirectory() as directory:
            source = Path(directory) / 'source'
            write_json(source / 'recipe/unsupported.json', {'type': 'ribbits:unimplemented'})
            with self.assertRaisesRegex(ValueError, 'Unsupported Ribbits recipe type'):
                ribbits.import_data_resources(source, Path(directory) / 'resources')

    def test_recipe_unlock_migration_is_idempotent_and_rejects_ambiguous_predicates(self):
        original = {'criteria': {'unlock': {'trigger': 'minecraft:recipe_unlocked',
                                          'conditions': {'recipe': 'ribbits:test'}}}}
        native = ribbits.advancement_value(original)
        self.assertEqual({'recipes': 'jugcraft:ribbits_test'}, native['criteria']['unlock']['conditions'])
        self.assertEqual(native, ribbits.advancement_value(native))
        self.assertEqual({'recipe': 'ribbits:test'}, original['criteria']['unlock']['conditions'])
        original['criteria']['unlock']['conditions']['recipes'] = 'ribbits:other'
        with self.assertRaisesRegex(ValueError, 'Conflicting Ribbits recipe-unlocked'):
            ribbits.advancement_value(original)


if __name__ == '__main__':
    unittest.main()
