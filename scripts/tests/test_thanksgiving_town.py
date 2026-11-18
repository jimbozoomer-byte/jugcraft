"""Thanksgiving harvest town details (tools/thanksgiving_town.py, docs/features/thanksgiving-town-details.md): resource and
art checks run offline. These read files; they are not game tests."""
import json
import sys
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))

import thanksgiving_town as tt  # noqa: E402
import thanksgiving_town_check as check  # noqa: E402

RES = ROOT / "src" / "main" / "resources"
LANG = RES / "assets" / "jugcraft" / "lang" / "en_us.json"


class ThanksgivingTownResources(unittest.TestCase):
    def test_every_piece_is_registered_named_tagged_dropped_and_regenerates(self):
        self.assertEqual(check.problems(), [])

    def test_models_pass_the_art_rules(self):
        self.assertEqual(check.art_problems(), [])

    def test_the_arch_opening_has_no_block_and_no_solid_box(self):
        w, h, opening, clear = (tt.ARCH[k] for k in ("width", "height", "opening", "clear"))
        cells = set(tt.ARCH["cells"])
        self.assertEqual(len(cells), len(tt.ARCH["cells"]), "each cell once")
        self.assertEqual((0, 0), tt.ARCH["cells"][0], "part 0, which alone drops the arch, is the left post's foot")
        for right in range(1, 1 + opening):
            for up in range(clear):
                self.assertNotIn((right, up), cells, "no block in the opening")
        # Every solid box stays clear of the opening's columns below the beam (frame x runs to the placer's left).
        for x0, y0, z0, x1, y1, z1 in tt.ARCH["shapes"]:
            opens = x1 > 16 + 1e-6 and x0 < 16 * (w - 1) - 1e-6
            if opens:
                self.assertGreaterEqual(y0, 16 * clear, f"a box over the opening must start above it: {[x0, y0, x1, y1]}")
        self.assertEqual(w, opening + 2)
        self.assertEqual(h, clear + 1)

    def test_recipes_are_reachable_and_ungated(self):
        lang = json.loads(LANG.read_text(encoding="utf-8"))
        for recipe in tt.SHAPELESS:
            self.assertLessEqual(len(recipe["inputs"]), 9)
            self.assertEqual(recipe["count"], 1)
            for ingredient in recipe["inputs"]:
                if ingredient.startswith("jugcraft:"):
                    name = ingredient.split(":")[1]
                    self.assertTrue(f"item.jugcraft.{name}" in lang or f"block.jugcraft.{name}" in lang,
                                    f"{recipe['id']}: {ingredient} must be an existing Jugcraft item")
                    self.assertNotIn(name, tt.PROPS, "no piece needs another new piece")
                else:
                    self.assertTrue(ingredient.startswith(("minecraft:", "#minecraft:")), ingredient)
            data = json.loads((RES / "data" / "jugcraft" / "recipe" / f"{recipe['id']}.json").read_text(encoding="utf-8"))
            self.assertEqual(data["fabric:load_conditions"], [{"condition": "jugcraft:feature_enabled", "feature": "agriculture"}])

    def test_required_tags_are_present(self):
        """26.3 reads some behaviour from tags; these pieces need the mining tags and the planters' harvest tag."""
        mine = {kind: json.loads((RES / "data" / "minecraft" / "tags" / "block" / "mineable" / f"{kind}.json").read_text(encoding="utf-8"))["values"]
                for kind in ("axe", "pickaxe")}
        for block in tt.PROPS:
            self.assertIn(f"jugcraft:{block}", mine["pickaxe" if block in tt.PICKAXE else "axe"])
        harvest = json.loads((RES / "data" / "jugcraft" / "tags" / "item" / "planter_harvest.json").read_text(encoding="utf-8"))
        self.assertEqual(harvest, {"replace": False, "values": tt.PLANTER_HARVEST["items"]})

    def test_planter_harvest_fits_every_planter(self):
        """Each harvest piece's own block model, drawn at each planter's spots, soil and scale as client/PlanterRenderer
        draws it, stays inside that planter's outline across and deep (PlanterBlock's boxes)."""
        import re
        import subprocess
        import fall_garden as fg
        java = (ROOT / "src/main/java/io/github/jimbozoomer/jugcraft/agriculture/PlanterBlock.java").read_text(encoding="utf-8")
        for item in tt.PLANTER_HARVEST["items"]:
            name = item.split(":")[1]
            rel = f"src/main/resources/assets/jugcraft/models/block/{name}.json"
            text = (ROOT / rel).read_text(encoding="utf-8") if (ROOT / rel).is_file() else subprocess.run(
                ["git", "-C", str(ROOT), "show", f"HEAD:{rel}"], capture_output=True, text=True, check=True).stdout
            elements = json.loads(text)["elements"]
            lo = [min(e["from"][k] for e in elements) for k in range(3)]
            hi = [max(e["to"][k] for e in elements) for k in range(3)]
            for planter in tt.PLANTER_HARVEST["planters"]:
                spec = fg.PLANTERS[planter]
                found = re.search(r"Planter " + spec["java"] + r" = new Planter\(new double\[\]\[\] \{(.*?)\},\s*new double", java, re.S)
                self.assertIsNotNone(found, planter)
                boxes = [[float(v) for v in b.split(",")] for b in re.findall(r"\{([^{}]*)\}", found.group(1))]
                bx0, bz0 = min(b[0] for b in boxes), min(b[2] for b in boxes)
                bx1, bz1 = max(b[3] for b in boxes), max(b[5] for b in boxes)
                for sx, sz in spec["spots"]:
                    x0, x1 = sx + (lo[0] - 8) * spec["scale"], sx + (hi[0] - 8) * spec["scale"]
                    z0, z1 = sz + (lo[2] - 8) * spec["scale"], sz + (hi[2] - 8) * spec["scale"]
                    self.assertTrue(bx0 - 1e-6 <= x0 and x1 <= bx1 + 1e-6 and bz0 - 1e-6 <= z0 and z1 <= bz1 + 1e-6,
                                    f"{name} at {planter}'s spot {(sx, sz)}: {[x0, z0, x1, z1]} outside {[bx0, bz0, bx1, bz1]}")

if __name__ == "__main__":
    unittest.main()
