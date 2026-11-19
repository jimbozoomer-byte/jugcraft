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


def box_uv(u, v, w, h, d):
    """Every texel a closed model box samples: texOffs (u, v) and size (w, h, d), in the cube layout faces() uses."""
    pixels = set()
    for x, y, fw, fh in ((u + d, v, w, d), (u + d + w, v, w, d), (u, v + d, d, h), (u + d, v + d, w, h),
                         (u + d + w, v + d, d, h), (u + d + w + d, v + d, w, h)):
        pixels.update((x + i, y + j) for i in range(fw) for j in range(fh))
    return pixels


# The closed boxes the model hangs on a guest (client/GuestBody.java), as (texOffs u, v, size w, h, d) for each
# GuestBody set that shows them: the head accessories sample the reserved 8x8 islands, the flare its own island.
MODEL_BOXES = {
    'EARS': [(0, 0, 2, 4, 1)],
    'HORNS': [(0, 0, 1, 3, 1)],
    'ELF_EARS': [(0, 0, 1, 2, 2)],
    'MAGE_HAT': [(0, 0, 2, 1, 2), (24, 0, 2, 2, 2), (0, 0, 2, 2, 2)],
    'FLARE': [(56, 16, 3, 4, 1), (56, 24, 1, 4, 3)],
}
ISLANDS = ((0, 0, 8, 8), (24, 0, 8, 8), FLARE_UV)


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
    def test_all_available_guests_have_detailed_models_and_matching_textures(self):
        assets = ROOT / 'src/main/resources/assets/jugcraft'
        guests = set(DESIGNS)
        self.assertEqual(set(java_strings(CLIENT / 'GuestPortraitModel.java', 'GUESTS')), guests)
        self.assertEqual({p.stem for p in (assets / 'town_models/guests').glob('*.json')}, guests)
        self.assertEqual({p.stem.removeprefix('guest_') for p in
                          (assets / 'textures/entity/town_guests').glob('guest_*.png')}, guests)

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

    def test_every_guest_has_all_four_outfits_and_no_pending_guest_has_any(self):
        """Every available roster guest has a skin in each of the four outfits; a pending one (identity unknown) none."""
        roster = (GUESTS / 'GuestRoster.java').read_text(encoding='utf-8')
        available = set(re.findall(r'\b(?:real|fiction|timeSkip)\("([a-z0-9_]+)"', roster))
        pending = set(re.findall(r'\bpending\("([a-z0-9_]+)"', roster))
        self.assertEqual(available, set(DESIGNS), 'one design for each available guest')
        self.assertTrue(pending and not pending & available, pending)
        self.assertEqual(OUTFITS, ('normal', 'winter', 'harvest', 'spring'))
        for name in available:
            for outfit in OUTFITS:
                with self.subTest(guest=name, outfit=outfit):
                    self.assertTrue((OUT / (skin_name(name, outfit) + '.png')).is_file())
        for name in pending:
            with self.subTest(pending=name):
                self.assertEqual(list(OUT.glob('guest_' + name + '*.png')), [], 'no skin is guessed for a pending guest')

    def test_the_model_boxes_sample_opaque_islands_in_every_outfit(self):
        """Each accessory and flare box GuestBody hangs on a guest samples only its reserved island, opaque in every
        outfit of every guest that shows it; the head accessories keep their colours through every outfit (they are
        hair, ears, horns or the hat), and the flare panels take each outfit's own skirt colours."""
        islands = set()
        for u, v, w, h in ISLANDS:
            islands.update((u + i, v + j) for i in range(w) for j in range(h))
        for field, boxes in MODEL_BOXES.items():
            guests = java_strings(CLIENT / 'GuestBody.java', field)
            self.assertTrue(guests and set(guests) <= set(DESIGNS), field)
            for name in guests:
                normal = make(name, DESIGNS[name]).img
                for outfit in OUTFITS:
                    with self.subTest(field=field, guest=name, outfit=outfit), Image.open(OUT / (skin_name(name, outfit) + '.png')) as skin:
                        skin = skin.convert('RGBA')
                        for box in boxes:
                            for x, y in box_uv(*box):
                                self.assertIn((x, y), islands, box)
                                self.assertEqual(skin.getpixel((x, y))[3], 255, (box, x, y))
                                if field != 'FLARE':
                                    self.assertEqual(skin.getpixel((x, y)), normal.getpixel((x, y)), (box, x, y))
        for name in FLARE:
            spec = DESIGNS[name]
            for outfit in OUTFITS:
                with self.subTest(flare=name, outfit=outfit):
                    p = make(name, spec, outfit)
                    for j in range(4):
                        pants = p.get('right_pants', 'front', 1, j)
                        skirt = pants if pants[3] else p.get('right_leg', 'front', 1, j)
                        for top in (17, 27):
                            self.assertEqual(p.px[FLARE_UV[0], top + j][:3], skirt[:3], (top, j))

    def test_the_owners_body_rule(self):
        groups = [set(HELD), set(REAL), set(FITTED), set(PETITE)]
        self.assertEqual(set().union(*groups), set(DESIGNS), 'every guest is in a group')
        self.assertEqual(sum(map(len, groups)), len(DESIGNS), 'and in only one')
        self.assertEqual(set(HELD), OWNER_HELD, 'the 14 guests the owner holds out of shaping')
        roster = (GUESTS / 'GuestRoster.java').read_text(encoding='utf-8')
        self.assertEqual(set(HELD), set(re.findall(r'\btimeSkip\("([a-z0-9_]+)"', roster)), 'time-skip editions use the classic fallback layout')
        self.assertEqual(set(REAL), set(re.findall(r'\breal\("([a-z0-9_]+)"', roster)), 'the real people')
        self.assertEqual(set(FITTED) | set(PETITE), set(re.findall(r'\bfiction\("([a-z0-9_]+)"', roster)), 'existing adult editions outside the time-skip group')
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
