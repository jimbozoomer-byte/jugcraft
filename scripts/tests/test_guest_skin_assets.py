"""Packaged guest skins: UV coverage, reproducibility and complete roster wiring."""
import hashlib
import re
import sys
import unittest
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / 'tools'))
from guest_skins import DESIGNS, make
from town_skins import OUT, faces


class GuestSkinAssets(unittest.TestCase):
    def test_every_authored_roster_skin_is_packaged_and_unique(self):
        java = (ROOT / 'src/main/java/io/github/jimbozoomer/jugcraft/town/guests/GuestAppearances.java').read_text(encoding='utf-8')
        mapped = dict(re.findall(r'Map.entry\("([^"]+)", "([^"]+)"\)', java))
        self.assertEqual(set(mapped), set(DESIGNS))
        hashes = set()
        for name, spec in DESIGNS.items():
            with self.subTest(guest=name), Image.open(OUT / (mapped[name] + '.png')) as skin:
                self.assertEqual(skin.size, (64, 64))
                self.assertEqual(skin.mode, 'RGBA')
                self.assertEqual(skin.tobytes(), make(name, spec).img.tobytes())
                hashes.add(hashlib.sha256(skin.tobytes()).hexdigest())
                for part in ('head','body','right_arm','left_arm','right_leg','left_leg'):
                    for x,y,w,h in faces(part).values():
                        self.assertEqual(skin.crop((x,y,x+w,y+h)).getchannel('A').getextrema(), (255,255))
                # The overlay must not turn into an opaque box around the face.
                self.assertEqual(skin.getpixel((44,14))[3], 0)
                # Accessory geometry samples this otherwise unused island.
                self.assertEqual(skin.crop((0,0,8,8)).getchannel('A').getextrema(), (255,255))
        self.assertEqual(len(hashes), len(DESIGNS))


if __name__ == '__main__':
    unittest.main()
