"""Additive tag repairs preserve later generators without weakening exact repairs."""
import json
from pathlib import Path
import sys
import tempfile
import unittest
from unittest.mock import patch

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / 'tools'))
import owner_resource_patches


class OwnerResourcePatchTests(unittest.TestCase):
    def apply_patch(self, current, change, repeats=1, phase='canonical', resource_phase=None, extra_resources=()):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            resources = root / 'src/main/resources'
            target = resources / 'data/minecraft/tags/block/walls.json'
            target.parent.mkdir(parents=True)
            target.write_text(json.dumps({'replace': False, 'values': current}), encoding='utf-8')
            catalog = root / 'tools/data/owner-resource-patches.json'
            catalog.parent.mkdir(parents=True)
            resource = {'file': 'data/minecraft/tags/block/walls.json', 'changes': [change]}
            if resource_phase is not None:
                resource['phase'] = resource_phase
            catalog.write_text(json.dumps({'resources': [resource, *extra_resources]}), encoding='utf-8')
            with patch.object(owner_resource_patches, 'ROOT', root), patch.object(owner_resource_patches, 'RES', resources):
                for _ in range(repeats):
                    owner_resource_patches.apply(phase=phase)
            return json.loads(target.read_text(encoding='utf-8'))

    def change(self, operation='append_values'):
        return {'op': operation, 'path': ['values'], 'before': ['base'], 'after': ['base', 'authored']}

    def test_additions_preserve_new_members_order_and_are_idempotent(self):
        result = self.apply_patch(['newest', 'base'], self.change(), 2)
        self.assertEqual(result, {'replace': False, 'values': ['newest', 'base', 'authored']})

    def test_already_present_addition_is_not_duplicated(self):
        self.assertEqual(self.apply_patch(['base', 'authored', 'newest'], self.change())['values'],
                         ['base', 'authored', 'newest'])

    def test_nested_atlas_entries_use_structural_equality_without_reordering(self):
        base = {'type': 'single', 'details': {'sprite': 'base', 'frames': [0, 1]}}
        reordered = {'details': {'frames': [0, 1], 'sprite': 'base'}, 'type': 'single'}
        added = {'type': 'single', 'details': {'sprite': 'new', 'frames': [1, 0]}}
        change = {'op': 'append_values', 'path': ['values'], 'before': [base], 'after': [base, added, added]}
        self.assertEqual([reordered, 'later', added], self.apply_patch([reordered, 'later'], change, repeats=2)['values'])
        with self.assertRaisesRegex(ValueError, 'Generated resource changed'):
            self.apply_patch([{'type': 'single', 'details': {'sprite': 'base', 'frames': [1, 0]}}], change)

    def test_missing_baseline_member_is_rejected(self):
        with self.assertRaisesRegex(ValueError, 'Generated resource changed'):
            self.apply_patch(['newest'], self.change())

    def test_additive_operation_cannot_remove_members(self):
        change = self.change()
        change['after'] = ['authored']
        with self.assertRaisesRegex(ValueError, 'Invalid additive resource patch'):
            self.apply_patch(['base'], change)

    def test_replacement_operation_keeps_exact_precondition(self):
        with self.assertRaisesRegex(ValueError, 'Generated resource changed'):
            self.apply_patch(['base', 'newest'], self.change('replace'))

    def test_canonical_phase_leaves_final_additions_for_later(self):
        result = self.apply_patch(['base'], self.change(), resource_phase='final')
        self.assertEqual(result['values'], ['base'])

    def test_final_phase_filters_before_opening_earlier_resources(self):
        earlier = {'file': 'not-created-in-this-focused-run.json', 'changes': [self.change('replace')]}
        result = self.apply_patch(['base', 'later-generator'], self.change(), repeats=2,
                                  phase='final', resource_phase='final', extra_resources=[earlier])
        self.assertEqual(result['values'], ['base', 'later-generator', 'authored'])

    def test_final_phase_preserves_addition_conflict_guard(self):
        conflict = {'op': 'add', 'path': ['replace'], 'after': True}
        with self.assertRaisesRegex(ValueError, 'Authored resource addition conflicts'):
            self.apply_patch(['base'], conflict, phase='final', resource_phase='final')

    def test_final_phase_preserves_missing_baseline_guard(self):
        with self.assertRaisesRegex(ValueError, 'Generated resource changed'):
            self.apply_patch(['later-generator'], self.change(), phase='final', resource_phase='final')

    def test_final_catalog_keeps_imported_runtime_registrations(self):
        catalog = json.loads((ROOT / 'tools/data/owner-resource-patches.json').read_text(encoding='utf-8'))
        final = {entry['file']: entry['changes'] for entry in catalog['resources'] if entry.get('phase') == 'final'}
        language = {change['path'][0]: change['after'] for change in final['assets/jugcraft/lang/en_us.json']}
        sounds = {change['path'][0]: change['after'] for change in final['assets/jugcraft/sounds.json']}
        event = sounds['aquatic_tackle_box_open']
        self.assertIn(event['subtitle'], language)
        self.assertEqual(language['block.jugcraft.doorworks_cherry_waffle_door'], 'Cherry Waffle Door')
        self.assertEqual(language['item.jugcraft.jugtania_flugel_eye'], 'Eye of the Fl\u00fcgel')
        self.assertIn('10\u201320%', language['enchantment.jugcraft.reforged.cleaving_edge.desc'])
        self.assertTrue(language['item.jugcraft.wilds_grant_suns_blessing.text.3'].startswith('\u00a7lSunstrike'))
        for kind in ('item', 'block'):
            changes = final[f'data/minecraft/tags/{kind}/planks.json']
            self.assertTrue(any('jugcraft:ribbits_mossy_oak_planks' in c['after'] for c in changes))
        atlas = final['assets/minecraft/atlases/blocks.json'][0]
        restored = [entry for entry in atlas['after'] if entry not in atlas['before']]
        self.assertEqual({entry['resource'] for entry in restored}, {
            'jugcraft:newest/doorworks/item/cherry_waffle_door',
            'jugcraft:newest/supps/block/lumisene/lumisene',
        })

    def test_canonical_language_preserves_source_unicode_and_is_idempotent(self):
        # Null's unusual names are intentional source text; preserve those exact
        # code points rather than normalizing them or re-decoding UTF-8 as ANSI.
        source = json.loads((ROOT / 'art/owner-library/originals/Edited-2026-10-18-Part2/'
                            'jims biomes/lang/en_us.json').read_text(encoding='utf-8'))
        names = ('anomaly', 'liquid_null', 'null_block', 'null_end_stone',
                 'null_leaves', 'null_plant', 'unmapped_end_stone')
        expected = {'block.jugcraft.landscape_' + name: source['block.biomesoplenty.' + name]
                    for name in names}
        expected['item.jugcraft.landscape_liquid_null_bucket'] = source['item.biomesoplenty.liquid_null_bucket']
        expected.update({
            'item.jugcraft.wildlife_music_disc_death_by_hogs': 'Music Disc \u2014 Death by Hogs',
            'item.jugcraft.wildlife_music_disc_wild_ones': 'Music Disc \u2014 Wild Ones',
        })
        catalog = json.loads((ROOT / 'tools/data/owner-resource-patches.json').read_text(encoding='utf-8'))
        changes = [change for record in catalog['resources']
                   if record['file'] == 'assets/jugcraft/lang/en_us.json'
                   and record.get('phase', 'canonical') == 'canonical'
                   for change in record['changes'] if change['path'][-1] in expected]
        self.assertEqual(10, len(changes))
        self.assertEqual(expected, {change['path'][-1]: change['after'] for change in changes})
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            resources = root / 'src/main/resources'
            target = resources / 'assets/jugcraft/lang/en_us.json'
            target.parent.mkdir(parents=True)
            initial = {'fixture.unrelated': '\u00e9 \u2603'}
            target.write_text(json.dumps(initial, ensure_ascii=False), encoding='utf-8')
            patch_file = root / 'tools/data/owner-resource-patches.json'
            patch_file.parent.mkdir(parents=True)
            patch_file.write_text(json.dumps({'resources': [{
                'file': 'assets/jugcraft/lang/en_us.json', 'changes': changes,
            }]}, ensure_ascii=False), encoding='utf-8')
            with patch.object(owner_resource_patches, 'ROOT', root), patch.object(owner_resource_patches, 'RES', resources):
                owner_resource_patches.apply()
                first = target.read_bytes()
                owner_resource_patches.apply()
            self.assertEqual(first, target.read_bytes())
            self.assertEqual(initial | expected, json.loads(first.decode('utf-8')))

    def test_final_trinket_patches_restore_registered_equipment_after_slot_writers(self):
        catalog = json.loads((ROOT / 'tools/data/owner-resource-patches.json').read_text(encoding='utf-8'))
        paths = {'data/trinkets/tags/item/legs/belt.json': 4, 'data/trinkets/tags/item/legs/charm.json': 16}
        records = [r for r in catalog['resources'] if r['file'] in paths]
        self.assertEqual(set(paths), {r['file'] for r in records})
        registered = set(json.loads((ROOT / 'docs/audits/survival-item-registry.json').read_text(encoding='utf-8')))
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            resources = root / 'src/main/resources'
            patch_file = root / 'tools/data/owner-resource-patches.json'
            patch_file.parent.mkdir(parents=True)
            patch_file.write_text(json.dumps({'resources': records}), encoding='utf-8')
            for record in records:
                self.assertEqual('final', record['phase'])
                change, = record['changes']
                added = set(change['after']) - set(change['before'])
                self.assertEqual(paths[record['file']], len(added))
                self.assertTrue(added <= registered)
                target = resources / record['file']
                target.parent.mkdir(parents=True, exist_ok=True)
                target.write_text(json.dumps({'replace': False, 'values': change['before'] + ['fixture:later_addition']}),
                                  encoding='utf-8')
            with patch.object(owner_resource_patches, 'ROOT', root), patch.object(owner_resource_patches, 'RES', resources):
                owner_resource_patches.apply()
                for record in records:
                    actual = json.loads((resources / record['file']).read_text(encoding='utf-8'))
                    self.assertEqual(record['changes'][0]['before'] + ['fixture:later_addition'], actual['values'])
                owner_resource_patches.apply(phase='final')
                owner_resource_patches.apply(phase='final')
            for record in records:
                actual = json.loads((resources / record['file']).read_text(encoding='utf-8'))
                self.assertEqual(set(record['changes'][0]['after']) | {'fixture:later_addition'}, set(actual['values']))
                self.assertEqual(len(actual['values']), len(set(actual['values'])))
                self.assertIs(False, actual['replace'])


if __name__ == '__main__':
    unittest.main()
