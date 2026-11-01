"""Beardshy (docs/features/beardshy.md): his lines, his name and his skin stay in step with the code that uses them."""
import json
import re
import sys
import unittest
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))
import beardshy  # noqa: E402
import beardshy_look  # noqa: E402
from retro_trader_look import box_faces  # noqa: E402

JAVA = (ROOT / "src/main/java/io/github/jimbozoomer/jugcraft/season/Beardshy.java").read_text(encoding="utf-8")
LANG = json.loads((ROOT / "src/main/resources/assets/jugcraft/lang/en_us.json").read_text(encoding="utf-8"))


def constant(name):
    return int(re.search(rf"\b{name}\s*=\s*(\d+);", JAVA).group(1))


class BeardshyTests(unittest.TestCase):
    def test_java_line_counts_match_the_lines(self):
        self.assertEqual(constant("TALK_LINES"), len(beardshy.TALK))
        self.assertEqual(constant("STARTLED_LINES"), len(beardshy.STARTLED))
        self.assertEqual(constant("HURT_LINES"), len(beardshy.HURT))

    def test_language_file_has_his_name_and_every_line(self):
        expected = {}
        beardshy.lang(expected)
        for key, text in expected.items():
            self.assertEqual(LANG.get(key), text, key)
        self.assertEqual(LANG["entity.jugcraft.beardshy"], "Beardshy")

    def test_skin_is_what_the_script_draws(self):
        on_disk = Image.open(beardshy_look.OUT).convert("RGBA")
        self.assertEqual(on_disk.size, (64, 64))
        self.assertEqual(on_disk.tobytes(), beardshy_look.skin().tobytes())

    def test_every_face_the_villager_model_shows_is_painted(self):
        # His skin is drawn alone (no villager underneath), so a clear pixel on a shown face would be a hole.
        skin = Image.open(beardshy_look.OUT).convert("RGBA")
        parts = {"head": (0, 0, 8, 10, 8), "nose": (24, 0, 2, 4, 2), "body": (16, 20, 8, 12, 6), "robe": (0, 38, 8, 20, 6),
                 "arm": (44, 22, 4, 8, 4), "forearms": (40, 38, 8, 4, 4), "leg": (0, 22, 4, 12, 4)}
        for name, (u, v, w, h, d) in parts.items():
            for face, (x0, y0, fw, fh) in box_faces(u, v, w, h, d).items():
                for y in range(y0, y0 + fh):
                    for x in range(x0, x0 + fw):
                        self.assertEqual(skin.getpixel((x, y))[3], 255, f"{name} {face} at {x},{y}")


if __name__ == "__main__":
    unittest.main()
