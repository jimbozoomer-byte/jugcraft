"""Architectural shape generation must work before Gradle creates its cache."""
import json
from pathlib import Path
import sys
import tempfile
import unittest
from unittest.mock import patch
import zipfile

sys.path.insert(0, str(Path(__file__).resolve().parents[2] / 'tools'))
import newest_shapes as shapes


class NewestShapeAssetsTests(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.root = Path(self.directory.name)
        self.archive = self.root / 'prepared-minecraft-client.jar'
        self.patches = patch.multiple(shapes, ROOT=self.root, VANILLA=self.archive)
        self.patches.start()
        shapes.vanilla_model.cache_clear()

    def tearDown(self):
        shapes.vanilla_model.cache_clear()
        self.patches.stop()
        self.directory.cleanup()

    def write_archive(self, path, model='block/cube'):
        path.parent.mkdir(parents=True, exist_ok=True)
        value = {'elements': [{'from': [0, 0, 0], 'to': [16, 16, 16]}]}
        with zipfile.ZipFile(path, 'w') as archive:
            archive.writestr('assets/minecraft/models/' + model + '.json', json.dumps(value))
        return value

    def test_prepared_archive_supplies_real_shape_without_gradle_cache(self):
        expected = self.write_archive(self.archive)
        self.assertFalse((self.root / '.gradle').exists())
        self.assertEqual(expected, shapes.vanilla_model('block/cube'))
        self.assertEqual([[0, 0, 0, 16, 16, 16]], shapes.boxes_for(
            self.root, 'custom', {'model': 'minecraft:block/cube'}))

    def test_renamed_farmland_parent_uses_the_same_prepared_archive(self):
        expected = self.write_archive(self.archive, 'block/template_cube_bottom_top_indented')
        self.assertEqual(expected, shapes.vanilla_model('block/template_farmland'))

    def test_existing_loom_cache_remains_supported(self):
        path = self.root / '.gradle/loom-cache/minecraftMaven/net/minecraft/minecraft-clientOnly-test/26.3/client.jar'
        self.assertEqual(self.write_archive(path), shapes.vanilla_model('block/cube'))

    def test_missing_reference_still_fails_instead_of_inventing_collision(self):
        self.write_archive(self.archive)
        with self.assertRaisesRegex(ValueError, 'Pinned vanilla shape reference unavailable'):
            shapes.vanilla_model('block/missing')
