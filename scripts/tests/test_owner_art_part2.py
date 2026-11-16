"""The owner's second edited part as a root a built pact-spirit body may read (tools/owner_art.py; the garden cohort,
GC-06b, for GC-07's acorn cap): a path under its folder name is read there and named there, any other is a Blocks-folder
painting as before. The library is in the repository, so this runs offline; nothing in it is executed."""
import hashlib
from pathlib import Path
import sys
import unittest

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))
import owner_art  # noqa: E402


class OwnerArtPart2Tests(unittest.TestCase):
    def test_a_part2_path_is_named_under_its_own_folder(self):
        self.assertEqual(owner_art._origin("Edited-2026-10-18-Part2/jims mobs/x.png"),
                         "originals/Edited-2026-10-18-Part2/jims mobs/x.png")

    def test_a_blocks_path_is_named_as_before(self):
        self.assertEqual(owner_art._origin("biomes and tree blocks/fir_log.png"),
                         "originals/Blocks/biomes and tree blocks/fir_log.png")

    def test_a_part2_file_is_read_from_the_library(self):
        data = owner_art._read_owner("Edited-2026-10-18-Part2/jims mobs/geo/item/armor/acorn.geo.json")
        # The catalogued SHA-256 (catalog/edited-2026-10-18-part2/files.json files[23467]).
        self.assertEqual(hashlib.sha256(data).hexdigest(), "913962495527ed87e4bbe2d0e5fa29a79c04200bb762c602ded852b34f658b13")

    def test_a_blocks_painting_is_read_as_before(self):
        path = "biomes and tree blocks/spanish_moss.png"
        self.assertEqual(owner_art._read_owner(path), owner_art._read_blocks(path))


if __name__ == "__main__":
    unittest.main()
