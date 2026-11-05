"""Pharmacy generation must compare pixels, not platform-specific PNG compression."""
from contextlib import ExitStack
import hashlib
from pathlib import Path
import shutil
import sys
import tempfile
import unittest
from unittest.mock import patch

from PIL import Image, PngImagePlugin

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))
import pharmacy_item_art as items
import pharmacy_hud_art as hud
import pharmacology


class PharmacyArtTests(unittest.TestCase):
    def test_real_generators_preserve_encoding_but_replace_changed_pixels(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            maps = root / "tools/item_icons"
            maps.mkdir(parents=True)
            for name in [*("pharmacy_" + key for key in items.ITEMS),
                         *("pharmacy_effect_" + key for key in hud.EFFECTS)]:
                shutil.copyfile(ROOT / "tools/item_icons" / (name + ".txt"), maps / (name + ".txt"))
            (root / "tools/hud_icons").mkdir()
            shutil.copyfile(ROOT / "tools/hud_icons/strain_pill.txt", root / "tools/hud_icons/strain_pill.txt")
            assets = root / "src/main/resources/assets/jugcraft"
            # Earlier pharmacology passes used these aliases before final art.
            # Supply them so this detects an intermediate overwrite as well.
            for texture in set(pharmacology.EFFECT_TEXTURES.values()):
                source = ROOT / "src/main/resources/assets/jugcraft/textures/item" / (texture + ".png")
                target = assets / "textures/item" / (texture + ".png")
                target.parent.mkdir(parents=True, exist_ok=True)
                shutil.copyfile(source, target)
            with patch.object(items, "ROOT", root), patch.object(hud, "ROOT", root), patch.object(hud, "ASSETS", assets):
                items.generate_textures()
                paths = sorted((assets / "textures/item/pharmacy").glob("*.png"))
                paths += sorted((assets / "textures/mob_effect").glob("*.png"))
                paths.append(assets / "textures/gui/strain_pills.png")
                self.assertEqual(len(items.ITEMS) + len(hud.EFFECTS) + 1, len(paths))
                metadata = PngImagePlugin.PngInfo()
                metadata.add_text("encoding-test", "same pixels, alternate compression")
                for path in paths:
                    with Image.open(path) as image:
                        image.copy().save(path, compress_level=0, pnginfo=metadata)
                before = {path: hashlib.sha256(path.read_bytes()).digest() for path in paths}
                # Exercise the earlier entry point too. Unrelated metadata/crop
                # importers need no owner-library copies in this PNG regression.
                with ExitStack() as patches:
                    patches.enter_context(patch.object(pharmacology.drug_visuals, "write_all"))
                    for module in (pharmacology.sterile_pharmacy, pharmacology.precision_pharmacy,
                                   pharmacology.alcohol_pharmacy, pharmacology.controlled_release,
                                   pharmacology.psychedelic_pharmacy):
                        patches.enter_context(patch.object(module, "write_extras"))
                    pharmacology.write_all(lambda *args: None, assets, root / "data", {}, lambda *args: [])
                items.generate_textures()
                self.assertEqual(before, {path: hashlib.sha256(path.read_bytes()).digest() for path in paths})
                changed = assets / "textures/item/pharmacy/caffeine_drink.png"
                with Image.open(changed) as image:
                    damaged = image.copy()
                damaged.putpixel((0, 0), (255, 0, 255, 255))
                damaged.save(changed)
                items.generate_textures()
                with Image.open(changed) as restored:
                    self.assertEqual(items.render("caffeine_drink").tobytes(), restored.convert("RGBA").tobytes())


if __name__ == "__main__":
    unittest.main()
