"""Packaged guest skins: UV coverage, reproducibility, complete roster wiring, seasonal outfits and the body rule."""
import hashlib
import re
import sys
import unittest
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / 'tools'))
import guest_skins
from guest_skins import DESIGNS, FLARE, FLARE_UV, FITTED, HELD, OUTFITS, PETITE, REAL, SLIM, make, skin_name
from town_skins import OUT, PARTS, faces

GUESTS = ROOT / 'src/main/java/io/github/jimbozoomer/jugcraft/town/guests'
CLIENT = ROOT / 'src/client/java/io/github/jimbozoomer/jugcraft/client'
BASE = ('head', 'body', 'right_arm', 'left_arm', 'right_leg', 'left_leg')
# The owner's binding rule (11 November 2026): held out of romance and of any body-shape change.
OWNER_HELD = {'asa_mitaka', 'tokai_teio', 'agnes_tachyon', 'gold_ship', 'oguri_cap', 'special_week', 'rei_ayanami',
              'asuka_langley', 'rem', 'megumin', 'rin_ishtar', 'power', 'reze', 'zero_two'}


def java_strings(path, field):
    """The string literals of a `FIELD = Set.of(...)` / `List.of(...)` in a Java file (constants resolved)."""
    text = path.read_text(encoding='utf-8')
    body = re.search(field + r'\s*=\s*(?:Set|List)\.of\(([^;]*)\);', text).group(1)
    constants = dict(re.findall(r'static final String (\w+) = "([^"]*)"', text))
    out = []
    for token in (t.strip() for t in body.split(',')):
        out.append(token.strip('"') if token.startswith('"') else constants[token])
    return out


def used_regions(name):
    """Every pixel a guest skin may use: every part's faces (classic layout, which covers slim), the accessory islands
    and, for a flared gown, the flare island."""
    pixels = set()
    for part in PARTS:
        for x, y, w, h in faces(part).values():
            pixels.update((i, j) for i in range(x, x + w) for j in range(y, y + h))
    for u in (0, 24):
        pixels.update((u + i, j) for i in range(8) for j in range(8))
    if name in FLARE:
        u, v, w, h = FLARE_UV
        pixels.update((u + i, v + j) for i in range(w) for j in range(h))
    return pixels


class GuestSkinAssets(unittest.TestCase):
    def test_every_authored_roster_skin_is_packaged_and_unique(self):
        java = (GUESTS / 'GuestAppearances.java').read_text(encoding='utf-8')
        mapped = dict(re.findall(r'Map.entry\("([^"]+)", "([^"]+)"\)', java))
        self.assertEqual(set(mapped), set(DESIGNS))
        hashes = set()
        for name, spec in DESIGNS.items():
            with self.subTest(guest=name), Image.open(OUT / (mapped[name] + '.png')) as skin:
                self.assertEqual(skin.size, (64, 64))
                self.assertEqual(skin.mode, 'RGBA')
                self.assertEqual(skin.tobytes(), make(name, spec).img.tobytes())
                hashes.add(hashlib.sha256(skin.tobytes()).hexdigest())
                for part in BASE:
                    for x, y, w, h in faces(part).values():
                        self.assertEqual(skin.crop((x, y, x + w, y + h)).getchannel('A').getextrema(), (255, 255))
                # The overlay must not turn into an opaque box around the face.
                self.assertEqual(skin.getpixel((44, 14))[3], 0)
                # Accessory geometry samples this otherwise unused island.
                self.assertEqual(skin.crop((0, 0, 8, 8)).getchannel('A').getextrema(), (255, 255))
        self.assertEqual(len(hashes), len(DESIGNS))

    def test_every_seasonal_outfit_is_packaged_reproducible_and_in_the_player_layout(self):
        hashes = set()
        for name, spec in DESIGNS.items():
            for outfit in OUTFITS:
                with self.subTest(guest=name, outfit=outfit), Image.open(OUT / (skin_name(name, outfit) + '.png')) as skin:
                    self.assertEqual((skin.size, skin.mode), ((64, 64), 'RGBA'))
                    self.assertEqual(skin.tobytes(), make(name, spec, outfit).img.tobytes(), 'regenerate with tools/guest_skins.py')
                    hashes.add(hashlib.sha256(skin.tobytes()).hexdigest())
                    # The base layer has no transparency, in the arms it is drawn for and in classic arms too.
                    for part in BASE:
                        for x, y, w, h in list(faces(part).values()) + list(guest_skins.faces(part, name in SLIM).values()):
                            self.assertEqual(skin.crop((x, y, x + w, y + h)).getchannel('A').getextrema(), (255, 255), part)
                    self.assertEqual(skin.getpixel((44, 14))[3], 0)
                    for u in (0, 24):
                        self.assertEqual(skin.crop((u, 0, u + 8, 8)).getchannel('A').getextrema(), (255, 255))
                    u, v, w, h = FLARE_UV
                    self.assertEqual(skin.crop((u, v, u + w, v + h)).getchannel('A').getextrema(), (255, 255) if name in FLARE else (0, 0))
                    # Nothing outside the layout's regions; no outer pixel faint enough for the cutout to drop.
                    allowed = used_regions(name)
                    alpha = skin.getchannel('A').load()
                    for x in range(64):
                        for y in range(64):
                            if alpha[x, y]:
                                self.assertIn((x, y), allowed)
                                self.assertGreaterEqual(alpha[x, y], 26)
        self.assertEqual(len(hashes), len(DESIGNS) * len(OUTFITS), 'every outfit of every guest is its own skin')
        packaged = {p.stem for p in OUT.glob('guest_*.png')}
        self.assertEqual(packaged, {skin_name(n, o) for n in DESIGNS for o in OUTFITS}, 'no stray or missing guest skin')

    def test_an_outfit_keeps_the_face_and_the_hair(self):
        """Seasonal layers never touch the head; on the hat layer only a flower crown (row 0) or a leaf pin is added."""
        pin = {('front', 6, 0), ('front', 7, 1), ('front', 6, 1), ('front', 7, 0), ('left', 0, 1), ('left', 1, 0)}
        for name, spec in DESIGNS.items():
            normal = make(name, spec)
            for outfit in OUTFITS[1:]:
                seasonal = make(name, spec, outfit)
                with self.subTest(guest=name, outfit=outfit):
                    for face, (x, y, w, h) in faces('head').items():
                        self.assertEqual(normal.img.crop((x, y, x + w, y + h)).tobytes(), seasonal.img.crop((x, y, x + w, y + h)).tobytes(), face)
                    for face, (x, y, w, h) in faces('hat').items():
                        for j in range(h):
                            for i in range(w):
                                if normal.px[x + i, y + j] != seasonal.px[x + i, y + j]:
                                    crown = outfit == 'spring' and j == 0 and face != 'top'
                                    self.assertTrue(crown or outfit == 'harvest' and (face, i, j) in pin, (face, i, j))

    def test_the_owners_body_rule(self):
        groups = [set(HELD), set(REAL), set(FITTED), set(PETITE)]
        self.assertEqual(set().union(*groups), set(DESIGNS), 'every guest is in a group')
        self.assertEqual(sum(map(len, groups)), len(DESIGNS), 'and in only one')
        self.assertEqual(set(HELD), OWNER_HELD, 'the 14 guests the owner holds out of shaping')
        roster = (GUESTS / 'GuestRoster.java').read_text(encoding='utf-8')
        self.assertEqual(set(HELD), set(re.findall(r'(?:student|uncertain)\("([a-z0-9_]+)"', roster)), 'held = not marriage-eligible')
        self.assertEqual(set(REAL), set(re.findall(r'\breal\("([a-z0-9_]+)"', roster)), 'the real people')
        self.assertEqual(set(FITTED) | set(PETITE), set(re.findall(r'\bfiction\("([a-z0-9_]+)"', roster)), 'adult fictional guests')
        self.assertFalse(set(SLIM) & (set(HELD) | set(REAL)), 'slim arms for adult fictional guests only')
        self.assertFalse(set(FLARE) & set(HELD), 'no flare geometry for a held guest')
        self.assertEqual(set(java_strings(CLIENT / 'GuestBody.java', 'SLIM')), set(SLIM))
        self.assertEqual(set(java_strings(CLIENT / 'GuestBody.java', 'FLARE')), set(FLARE))
        self.assertEqual(tuple(java_strings(GUESTS / 'GuestWardrobe.java', 'OUTFITS')), OUTFITS)
        # Shaping is drawn only where the rule allows: the packaged files differ from an unshaped drawing exactly for FITTED.
        original = guest_skins.FITTED
        try:
            guest_skins.FITTED = ()
            unshaped = {name: make(name, spec).img.tobytes() for name, spec in DESIGNS.items()}
        finally:
            guest_skins.FITTED = original
        for name, spec in DESIGNS.items():
            with self.subTest(guest=name):
                self.assertEqual(make(name, spec).img.tobytes() != unshaped[name], name in FITTED)


if __name__ == '__main__':
    unittest.main()
