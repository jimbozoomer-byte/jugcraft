"""Multipart state ordering is deterministic without reordering collision boxes."""
import json
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[2]


class NewestShapeOrderTests(unittest.TestCase):
    def test_hash_seeds_preserve_catalog_bytes_geometry_and_registry_order(self):
        with tempfile.TemporaryDirectory() as directory:
            assets = Path(directory)
            models = assets / 'models/block'
            models.mkdir(parents=True)
            # Deliberately non-sorted boxes: collision array order is authored.
            elements = [
                {'from': [9, 3, 5], 'to': [12, 8, 14]},
                {'from': [1, 0, 2], 'to': [6, 4, 7]},
            ]
            (models / 'fixture.json').write_text(json.dumps({'elements': elements}), encoding='utf-8')
            script = r'''
import copy, json, sys
from pathlib import Path
sys.path.insert(0, sys.argv[1])
from newest_shapes import variants, order_shape_keys
state = {'multipart': [
    {'apply': {'model': 'fixture:block/fixture'}},
    {'when': {'side': 'low|tall'}, 'apply': {'model': 'fixture:block/fixture', 'y': 90}},
]}
properties = {'side': {'none', 'low', 'tall'}, 'facing': {'north', 'south', 'east', 'west'},
              'waterlogged': {'true', 'false'}}
shapes = variants(Path(sys.argv[2]), 'fixture', state, properties)
raw_keys = list(shapes)
catalog = {'blocks': {'z_first': {'shapes': shapes}, 'a_second': {'shapes': {'': [[8, 0, 0, 16, 2, 4]]}}},
           'items': {'z_first': {'label': 'first'}, 'a_second': {'label': 'second'}},
           'paintings': ['fixture:z_first', 'fixture:a_second']}
before = copy.deepcopy(catalog)
order_shape_keys(catalog['blocks'])
assert catalog == before
text = json.dumps(catalog, separators=(',', ':'), ensure_ascii=True) + '\n'
order_shape_keys(catalog['blocks'])
assert text == json.dumps(catalog, separators=(',', ':'), ensure_ascii=True) + '\n'
print(json.dumps({'raw_keys': raw_keys, 'text': text}))
'''
            runs = [json.loads(subprocess.check_output(
                [sys.executable, '-c', script, str(ROOT / 'tools'), str(assets)],
                env={**os.environ, 'PYTHONHASHSEED': str(seed)}).decode('utf-8'))
                for seed in (1, 2, 3, 41)]
        self.assertGreater(len({tuple(run['raw_keys']) for run in runs}), 1,
                           'The fixture must reproduce hash-dependent multipart input order')
        self.assertEqual(1, len({run['text'] for run in runs}))
        actual = json.loads(runs[0]['text'])
        self.assertEqual(['z_first', 'a_second'], list(actual['blocks']))
        self.assertEqual(['z_first', 'a_second'], list(actual['items']))
        self.assertEqual(['fixture:z_first', 'fixture:a_second'], actual['paintings'])
        shapes = actual['blocks']['z_first']['shapes']
        self.assertEqual(sorted(shapes), list(shapes))
        self.assertEqual([element['from'] + element['to'] for element in elements],
                         shapes['facing=north,side=none,waterlogged=false'])


if __name__ == '__main__':
    unittest.main()
