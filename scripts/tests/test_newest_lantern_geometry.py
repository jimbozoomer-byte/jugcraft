"""Native lantern linings preserve the supplied surface, rather than hiding it."""
from copy import deepcopy
from pathlib import Path
import sys
import unittest

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / 'tools'))
import model_writer
import newest_building
import imported_geometry_repairs as repairs
import newest_original_art


class NewestLanternGeometryTests(unittest.TestCase):
    def test_both_original_linings_keep_every_surface_and_texture_coordinate(self):
        for name in ('template_paper_lantern', 'template_paper_lantern_hanging'):
            with self.subTest(name=name):
                source = newest_building.SOURCE / 'twig/assets/twigs/models/block' / (name + '.json')
                original_bytes = source.read_bytes()
                original = newest_building.read(source)
                mapped = newest_building.map_value(original, 'twigs', 'twigworks', True)
                target = newest_building.ASSET / 'models/newest/twigworks/block' / (name + '.json')
                adapted = newest_building.native_model(target, mapped)
                record = repairs.records()[target.relative_to(repairs.MODELS).as_posix()]
                self.assertEqual(record['after_sha256'], repairs.digest(adapted['elements']))
                self.assertEqual(mapped['elements'], repairs.original_elements(target, adapted['elements']))
                self.assertEqual(len(mapped['elements']), len(adapted['elements']))
                for original_box, box in zip(mapped['elements'], adapted['elements']):
                    self.assertEqual(original_box['faces'], box['faces'], 'Every face, material and UV is retained')
                    self.assertEqual(original_box.get('rotation'), box.get('rotation'))
                    for corner in ('from', 'to'):
                        self.assertTrue(all(abs(a-b) <= .301 for a,b in zip(original_box[corner], box[corner])))
                lining = adapted['elements'][1]
                self.assertTrue(all(a > b for a,b in zip(lining['from'], lining['to'])), 'Authored inward-facing lining is retained')
                self.assertEqual(adapted, newest_building.native_model(target, deepcopy(adapted)))
                self.assertTrue(newest_original_art.intentional_reversed_elements(target, adapted['elements']))
                changed = deepcopy(adapted['elements'])
                changed[1]['from'][0] += 1
                self.assertFalse(newest_original_art.intentional_reversed_elements(target, changed))
                self.assertEqual(original_bytes, source.read_bytes())

    def test_changed_or_unrelated_reversed_geometry_is_still_rejected(self):
        source = newest_building.SOURCE / 'twig/assets/twigs/models/block/template_paper_lantern.json'
        changed = newest_building.read(source)
        changed['elements'][1]['from'][0] = 14
        target = newest_building.ASSET / 'models/newest/twigworks/block/template_paper_lantern.json'
        with self.assertRaisesRegex(ValueError, 'Imported geometry changed'):
            newest_building.native_model(target, changed)
        unrelated = newest_building.native_model(target.with_name('unrelated.json'), changed)
        with self.assertRaisesRegex(ValueError, 'runs backwards'):
            model_writer.finish_elements(unrelated['elements'])


if __name__ == '__main__':
    unittest.main()
