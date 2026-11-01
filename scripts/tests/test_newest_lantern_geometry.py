"""Native lantern linings preserve the supplied surface, rather than hiding it."""
from copy import deepcopy
from pathlib import Path
import sys
import unittest

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / 'tools'))
import model_writer
import newest_building


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
                self.assertEqual(len(mapped['elements']) + 5, len(adapted['elements']))
                self.assertEqual(mapped['elements'][0], adapted['elements'][0])
                self.assertEqual(mapped['elements'][2:], adapted['elements'][7:])
                lining = mapped['elements'][1]
                for plane in adapted['elements'][1:7]:
                    self.assertTrue(all(a <= b for a, b in zip(plane['from'], plane['to'])))
                    self.assertEqual(1, sum(a == b for a, b in zip(plane['from'], plane['to'])))
                    face = next(iter(plane['faces']))
                    self.assertEqual(lining['faces'][face]['texture'], plane['faces'][face]['texture'])
                    before = model_writer._face_frame(lining, face, None)
                    after = model_writer._face_frame(plane, face, None)
                    self.assertEqual({tuple(p) for p in before[0]}, {tuple(p) for p in after[0]})
                    for point in before[0]:
                        self.assertEqual(model_writer._uv_at(before, point), model_writer._uv_at(after, point))
                self.assertEqual(adapted, newest_building.native_model(target, deepcopy(adapted)))
                model_writer.finish_elements(adapted['elements'])
                self.assertTrue(all(all(a <= b for a, b in zip(e['from'], e['to'])) for e in adapted['elements']))
                self.assertEqual(original_bytes, source.read_bytes())

    def test_changed_or_unrelated_reversed_geometry_is_still_rejected(self):
        source = newest_building.SOURCE / 'twig/assets/twigs/models/block/template_paper_lantern.json'
        changed = newest_building.read(source)
        changed['elements'][1]['from'][0] = 14
        target = newest_building.ASSET / 'models/newest/twigworks/block/template_paper_lantern.json'
        with self.assertRaisesRegex(ValueError, 'Changed supplied paper-lantern lining'):
            newest_building.native_model(target, changed)
        unrelated = newest_building.native_model(target.with_name('unrelated.json'), changed)
        with self.assertRaisesRegex(ValueError, 'runs backwards'):
            model_writer.finish_elements(unrelated['elements'])


if __name__ == '__main__':
    unittest.main()
