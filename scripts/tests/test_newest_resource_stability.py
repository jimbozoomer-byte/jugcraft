"""Canonical imports keep native recipe IDs and lossless connected-texture outputs."""
import json
from pathlib import Path
import sys
import tempfile
import unittest

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / 'tools'))
import newest_connected
import newest_home


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value), encoding='utf-8')


class NewestResourceStabilityTests(unittest.TestCase):
    def test_home_calcite_tag_keeps_aliases_and_adds_registered_native_materials(self):
        source = ROOT / 'art/owner-library/originals/Edited-2026-10-29/home/data/c/tags/item/polished_calcite.json'
        original = source.read_bytes()
        supplied = json.loads(original)
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            target = root / 'resources/data/c/tags/item/polished_calcite.json'
            write_json(target, supplied)
            blocks = {'stoneworks_polished_calcite': {'source': 'stoneworks:polished_calcite', 'collection': 'stoneworks'},
                      'twigworks_polished_calcite': {'source': 'twigs:polished_calcite', 'collection': 'twigworks'}}
            for _ in range(2):
                newest_home.resources(root / 'source', root / 'resources', write_json,
                                      lambda source, target: self.fail('No fixture sounds to copy'), blocks)
            result = json.loads(target.read_text(encoding='utf-8'))
            self.assertEqual(supplied['values'] + ['jugcraft:stoneworks_polished_calcite',
                                                  'jugcraft:twigworks_polished_calcite'], result['values'])
            self.assertIs(False, result['replace'])
            registered = set(json.loads((ROOT / 'docs/audits/survival-item-registry.json').read_text(encoding='utf-8')))
            self.assertTrue({'jugcraft:stoneworks_polished_calcite', 'jugcraft:twigworks_polished_calcite'} <= registered)
        self.assertEqual(original, source.read_bytes())

    def test_synthetic_home_recipes_use_native_ingredient_ids(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            written = {}
            newest_home.resources(root / 'source', root / 'resources',
                                  lambda path, value: written.setdefault(path, value),
                                  lambda source, target: self.fail('No fixture sounds to copy'), {})
            recipe = written[root / 'resources/data/jugcraft/recipe/newest/homeworks/spruce_chandelier.json']
            self.assertEqual('minecraft:crafting_shapeless', recipe['type'])
            self.assertEqual(['minecraft:iron_chain', 'minecraft:lantern', 'minecraft:lantern',
                              'minecraft:spruce_planks'], recipe['ingredients'])
            self.assertEqual({'id': 'jugcraft:homeworks_spruce_chandelier', 'count': 1}, recipe['result'])
            vat = written[root / 'resources/data/jugcraft/recipe/newest/homeworks/dye_vat.json']
            self.assertEqual(['minecraft:cauldron', 'minecraft:oak_planks', 'minecraft:iron_ingot'],
                             vat['ingredients'])

    def test_connected_sheets_preserve_equivalent_png_bytes_and_animation(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            source, resources = root / 'source', root / 'resources'
            assets = source / 'fix/assets/chisel'
            refs = {}
            originals = {}
            expected = Image.new('RGBA', (10, 4))
            for index, key in enumerate(('particle', 'empty', 'vertical', 'horizontal', 'center')):
                refs[key] = 'chisel:block/' + key
                tile = Image.new('RGBA', (2, 4))
                for frame in range(2):
                    color = (20 + index * 30, 40 + frame * 90, 60, 255)
                    tile.paste(color, (0, frame * 2, 2, frame * 2 + 2))
                    expected.paste(color, (index * 2, frame * 2, index * 2 + 2, frame * 2 + 2))
                target = assets / 'textures/block' / (key + '.png')
                target.parent.mkdir(parents=True, exist_ok=True)
                tile.save(target)
                originals[target] = target.read_bytes()
                write_json(target.with_name(target.name + '.mcmeta'), {'animation': {'frametime': 2}})
            write_json(assets / 'blockstates/test/glass.json',
                       {'athena:loader': 'athena:ctm', 'ctm_textures': refs})
            write_json(assets / 'models/block/test/glass.json', {'parent': 'minecraft:block/cube_all'})
            output = resources / 'resourcepacks/fusion_textures/assets/jugcraft/textures/block/newest_connected/test/glass.png'
            output.parent.mkdir(parents=True, exist_ok=True)
            expected.save(output, compress_level=0)
            original_output = output.read_bytes()
            self.assertEqual(1, newest_connected.generate(source, resources, write_json))
            self.assertEqual(original_output, output.read_bytes(), 'Identical pixels must preserve the existing PNG encoding')
            sidecar = json.loads(output.with_name(output.name + '.mcmeta').read_text(encoding='utf-8'))
            self.assertEqual({'type': 'connecting', 'layout': 'pieced', 'render_type': 'translucent'}, sidecar['fusion'])
            self.assertEqual({'frametime': 2, 'width': 10, 'height': 2}, sidecar['animation'])
            for path, content in originals.items():
                self.assertEqual(content, path.read_bytes(), 'Supplied original pixels remain untouched')
            # A stale generated sheet must still be repaired, even if the dimensions match.
            stale = expected.copy()
            stale.putpixel((0, 0), (1, 2, 3, 4))
            stale.save(output)
            newest_connected.generate(source, resources, write_json)
            with Image.open(output) as repaired:
                self.assertEqual(expected.tobytes(), repaired.convert('RGBA').tobytes())

if __name__ == '__main__':
    unittest.main()
