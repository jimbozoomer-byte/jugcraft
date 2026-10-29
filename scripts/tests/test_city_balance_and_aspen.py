"""Owner currency anchor and winter-leaf resource regressions."""
from pathlib import Path
import json
import sys
import unittest

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))
import town_shops
import trees_data


class CityBalanceAndAspenTests(unittest.TestCase):
    def test_runtime_prices_match_source_and_gold_anchor(self):
        runtime = json.loads((ROOT / "src/main/resources/data/jugcraft/town/shops.json").read_text(encoding="utf-8"))
        self.assertEqual(runtime["shops"], town_shops.data())
        buys = {item: (count, price) for item, count, price, _ in town_shops.offers("general", "buys")}
        self.assertEqual(buys["minecraft:gold_ingot"], (4, 4))
        for crop in ("wheat", "carrot", "potato", "beetroot", "sugar_cane", "melon_slice"):
            self.assertEqual(buys[f"minecraft:{crop}"], (64, 1))
        self.assertNotIn("minecraft:diamond", buys, "Do not invent the owner's undecided diamond exchange rate")
        self.assertEqual(town_shops.loop_errors(), [])

    def test_all_saved_aspen_seasons_resolve_to_foliage(self):
        generated = {}
        trees_data.assets(Path("assets"), lambda p, d: generated.__setitem__(str(p), d))
        path = Path("assets/blockstates/aspen_leaves.json")
        states = generated[str(path)]
        runtime = json.loads((ROOT / "src/main/resources/assets/jugcraft/blockstates/aspen_leaves.json").read_text())
        self.assertEqual(runtime, states)
        self.assertEqual(set(states["variants"]), {"season=green", "season=gold", "season=bare"})
        for variant in states["variants"].values():
            name = variant["model"].split("/", 1)[1]
            model = generated[str(Path(f"assets/models/block/{name}.json"))]
            self.assertNotIn("bare", model["textures"]["all"])
            texture = ROOT / f"src/main/resources/assets/jugcraft/textures/block/{name}.png"
            self.assertTrue(texture.is_file())


if __name__ == "__main__":
    unittest.main()
