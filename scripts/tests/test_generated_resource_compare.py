"""PNG compression changes must not hide an actual artwork change."""
import io
import sys
import tempfile
import unittest
from pathlib import Path

from PIL import Image

sys.path.insert(0, str(Path(__file__).resolve().parents[2] / 'tools'))
from generated_resource_compare import matches


class GeneratedResourceCompareTests(unittest.TestCase):
    def test_compression_is_ignored_but_changed_pixels_and_dimensions_are_not(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / 'texture.png'
            art = Image.new('RGBA', (16, 16), (120, 70, 40, 255))
            art.save(path, compress_level=0)
            encoded = io.BytesIO()
            art.save(encoded, format='PNG', compress_level=9)
            self.assertNotEqual(path.read_bytes(), encoded.getvalue())
            self.assertTrue(matches(path, encoded.getvalue()))
            art.putpixel((8, 8), (121, 70, 40, 255))
            art.save(path)
            self.assertFalse(matches(path, encoded.getvalue()))
            Image.new('RGBA', (8, 32), (120, 70, 40, 255)).save(path)
            self.assertFalse(matches(path, encoded.getvalue()))

    def test_missing_files_and_non_png_content_remain_strict(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / 'model.json'
            self.assertFalse(matches(path, b'{}'))
            path.write_bytes(b'{}')
            self.assertTrue(matches(path, b'{}'))
            self.assertFalse(matches(path, b'{"model":1}'))


if __name__ == '__main__':
    unittest.main()
