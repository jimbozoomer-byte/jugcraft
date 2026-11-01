"""Crossed grass planes finish identically across platform trig rounding."""
from copy import deepcopy
import math
from pathlib import Path
import sys
import unittest
from unittest.mock import patch

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / 'tools'))
import model_writer
import newest_building as building


class GrassWallGenerationTests(unittest.TestCase):
    def setUp(self):
        self.source = building.SOURCE / 'fences/assets/mcwfences/models/block/parent/grass_topped_wall_triple.json'
        self.target = building.ASSET / 'models/newest/fenceworks/block/parent/grass_topped_wall_triple.json'
        self.original = building.map_value(building.read(self.source), 'mcwfences', 'fenceworks', True)

    def test_trig_rounding_keeps_committed_geometry_and_source(self):
        source_bytes = self.source.read_bytes()
        sine, cosine = math.sin, math.cos
        expected = building.read(self.target)
        for sin_direction in (-math.inf, math.inf):
            for cos_direction in (-math.inf, math.inf):
                with self.subTest(sin=sin_direction, cos=cos_direction):
                    model = building.native_model(self.target, self.original)
                    with patch.object(model_writer.math, 'sin', side_effect=lambda v: math.nextafter(sine(v), sin_direction)), \
                         patch.object(model_writer.math, 'cos', side_effect=lambda v: math.nextafter(cosine(v), cos_direction)):
                        model_writer.finish_elements(model['elements'])
                    self.assertEqual(expected, model)
                    self.assertEqual(model, building.native_model(self.target, model))
                    for index in (5, 23):
                        self.assertEqual(self.original['elements'][index]['faces'], model['elements'][index]['faces'])
                        self.assertEqual(self.original['elements'][index]['rotation'], model['elements'][index]['rotation'])
        self.assertEqual(source_bytes, self.source.read_bytes())

    def test_changed_native_plane_is_rejected_and_other_models_are_untouched(self):
        changed = deepcopy(self.original)
        changed['elements'][5]['from'][0] = 7
        with self.assertRaisesRegex(ValueError, 'Changed native grass-wall plane'):
            building.native_model(self.target, changed)
        self.assertEqual(changed, building.native_model(self.target.with_name('unrelated.json'), changed))


if __name__ == '__main__':
    unittest.main()
