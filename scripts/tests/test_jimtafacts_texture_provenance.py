"""The relocated Mimic atlas keeps the same strict supplied-texture provenance checks."""
import json
from pathlib import Path
import sys
import tempfile
import unittest
from unittest.mock import patch

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / 'tools'))
import jimtafacts_catalog
from PIL import Image


class JimtafactsTextureProvenanceTests(unittest.TestCase):
    def tearDown(self):
        jimtafacts_catalog.texture_sources.cache_clear()

    def test_native_mimic_matches_relocated_original_and_model_uv_size(self):
        png = ROOT / 'src/main/resources/assets/jugcraft/textures/entity/jimtafacts/mimic.png'
        jimtafacts_catalog.texture_sources.cache_clear()
        self.assertTrue(jimtafacts_catalog.original_texture(png))
        with Image.open(png) as image:
            self.assertEqual((64, 32), image.size)
        renderer = (ROOT / 'src/client/java/io/github/jimbozoomer/jugcraft/client/JimtaMimicClient.java').read_text(encoding='utf-8')
        inside = renderer.split('static LayerDefinition inside(){', 1)[1].split('static LayerDefinition chest(){', 1)[0]
        self.assertIn('LayerDefinition.create(mesh,64,32)', inside)
        self.assertIn('textures/entity/jimtafacts/mimic.png', renderer)

    def test_unrecognized_sources_and_changed_runtime_bytes_are_not_exempt(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            allowed = 'art/owner-library/originals/Edited-2026-10-29/artifacts/assets/artifacts/textures/entity/mimic.png'
            unrelated = 'art/owner-library/originals/Edited-2026-10-29/unrelated/other.png'
            runtime = 'src/main/resources/assets/jugcraft/textures/entity/jimtafacts/mimic.png'
            other_runtime = 'src/main/resources/assets/jugcraft/textures/entity/jimtafacts/other.png'
            for relative in (allowed, unrelated, runtime, other_runtime):
                target = root / relative
                target.parent.mkdir(parents=True, exist_ok=True)
                Image.new('RGBA', (64, 32), (10, 20, 30, 255)).save(target)
            catalog = root / 'art/owner-library/catalog/edited-2026-10-23-jimtafacts/runtime-copies.json'
            catalog.parent.mkdir(parents=True)
            catalog.write_text(json.dumps([{'source': allowed, 'runtime': runtime},
                                           {'source': unrelated, 'runtime': other_runtime}]), encoding='utf-8')
            with patch.object(jimtafacts_catalog, 'ROOT', root):
                jimtafacts_catalog.texture_sources.cache_clear()
                self.assertTrue(jimtafacts_catalog.original_texture(root / runtime))
                self.assertFalse(jimtafacts_catalog.original_texture(root / other_runtime))
                Image.new('RGBA', (64, 32), (11, 20, 30, 255)).save(root / runtime)
                self.assertFalse(jimtafacts_catalog.original_texture(root / runtime))


if __name__ == '__main__':
    unittest.main()
