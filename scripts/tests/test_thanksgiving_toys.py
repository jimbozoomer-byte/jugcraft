"""The Thanksgiving festival toys (tools/thanksgiving_toys.py; docs/features/thanksgiving-toys.md): the generated files,
Java numbers, drops, tags, names and recipes agree (tools/thanksgiving_toys_check.py), and the table's own rules hold."""
import json
from pathlib import Path
import sys
import unittest

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))

import thanksgiving_toys as tt  # noqa: E402


class ThanksgivingToysTests(unittest.TestCase):
    def test_everything_agrees(self):
        import thanksgiving_toys_check
        self.assertEqual(thanksgiving_toys_check.problems(ROOT), [])

    def test_every_piece_is_mined_with_an_axe(self):
        # The one block tag these pieces need: the axe breaks them quickest (none needs a tool to drop).
        tag = json.loads((ROOT / "src/main/resources/data/minecraft/tags/block/mineable/axe.json").read_text(encoding="utf-8"))
        for block in tt.PROPS:
            self.assertIn(f"jugcraft:{block}", tag["values"])

    def test_a_spin_is_bounded(self):
        delays = [tt.top_delay(n) for n in range(1, tt.TOP["spin"] + 1)]
        self.assertTrue(all(d >= 1 for d in delays))
        self.assertEqual(tt.TOP_TICKS, sum(delays))
        self.assertLessEqual(tt.TOP_TICKS, 200, "a spin is a few seconds, never endless")
        # It winds down: never quicker as it slows.
        self.assertEqual(delays, sorted(delays, reverse=True))

    def test_the_corn_toss_racks_and_turns(self):
        import thanksgiving_toys_data as ttd
        bags = tt.CORN_TOSS["bags"]
        for tossed in range(2 * bags + 1):
            rust = sum(tossed in ttd.toss_rack_values("rust", k) for k in range(bags))
            cream = sum(tossed in ttd.toss_rack_values("cream", k) for k in range(bags))
            self.assertEqual(rust, bags - (tossed + 1) // 2, f"rust's racked bags after {tossed} tosses")
            self.assertEqual(cream, bags - tossed // 2, f"cream's racked bags after {tossed} tosses")
        self.assertEqual(tt.CORN_TOSS["points"], {"hole": 3, "board": 1, "miss": 0})

    def test_no_wager_prize_or_reward(self):
        # Only words: the messages never speak of money, prizes or winnings, and the set makes nothing but itself.
        for key, text in tt.MESSAGES.items():
            for word in ("prize", "win", "jug", "coin", "bet", "wager", "reward"):
                self.assertNotIn(word, text.lower(), key)
        loot = json.loads((ROOT / "src/main/resources/data/jugcraft/loot_table/blocks/corn_toss_set.json").read_text(encoding="utf-8"))
        self.assertEqual([e["name"] for p in loot["pools"] for e in p["entries"]], ["jugcraft:corn_toss_set"])

    def test_the_backdrop_leaves_its_middle_open(self):
        import thanksgiving_toys_data as ttd
        cells = {tuple(c) for c in tt.BACKDROP["cells"]}
        for cell in tt.BACKDROP["open"]:
            self.assertNotIn(tuple(cell), cells)
        self.assertEqual(len(cells), tt.BACKDROP["across"] * tt.BACKDROP["up"] - len(tt.BACKDROP["open"]))
        parts = ttd.frame_cut(ttd.build("harvest_photo_backdrop").models["frame_whole"])
        self.assertEqual(len(parts), len(tt.BACKDROP["cells"]))
        self.assertTrue(all(parts), "every part has a model")
        # The sill under the opening is low enough to step over.
        self.assertLessEqual(tt.BACKDROP["sill_height"], 2.0)

    def test_recipes_are_reachable(self):
        lang = json.loads((ROOT / "src/main/resources/assets/jugcraft/lang/en_us.json").read_text(encoding="utf-8"))
        for recipe in tt.SHAPELESS:
            for ingredient in recipe["inputs"]:
                if ingredient.startswith("jugcraft:"):
                    name = ingredient.split(":", 1)[1]
                    self.assertTrue(f"item.jugcraft.{name}" in lang or f"block.jugcraft.{name}" in lang,
                                    f"{recipe['id']} needs {ingredient}, which must exist")
                self.assertNotIn(ingredient.split(":", 1)[-1], tt.PROPS, "no piece needs another new piece")


if __name__ == "__main__":
    unittest.main()
