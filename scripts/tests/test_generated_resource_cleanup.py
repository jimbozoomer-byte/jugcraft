"""Regeneration must remove stale generated packs while retaining authored integration data."""
from pathlib import Path
import sys
import tempfile
import unittest
from unittest.mock import patch

sys.path.insert(0, str(Path(__file__).resolve().parents[2] / "tools"))
import generate_material_data as generator


class GeneratedResourceCleanupTests(unittest.TestCase):
    def test_cleanup_preserves_authored_backpack_pack(self):
        with tempfile.TemporaryDirectory() as temporary:
            resources = Path(temporary) / "resources"
            generated = [resources / directory.relative_to(generator.RES) for directory in generator.GENERATED_DIRS]
            fixtures = {
                "resourcepacks/travelers_backpack/pack.mcmeta": b'{"pack": {}}',
                "resourcepacks/travelers_backpack/data/travelersbackpack/recipe/standard.json": b'{"type": "test"}',
                "fabric.mod.json": b'{"id": "jugcraft"}',
            }
            for relative in [*fixtures, "resourcepacks/alternate_machines/stale.json", "resourcepacks/fusion_textures/stale.json"]:
                path = resources / relative
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_bytes(fixtures.get(relative, b"stale"))
            with patch.object(generator, "RES", resources), patch.object(generator, "GENERATED_DIRS", generated):
                generator.clean_generated_resources()
            for relative, content in fixtures.items():
                self.assertEqual((resources / relative).read_bytes(), content)
            self.assertFalse((resources / "resourcepacks/alternate_machines").exists())
            self.assertFalse((resources / "resourcepacks/fusion_textures").exists())
