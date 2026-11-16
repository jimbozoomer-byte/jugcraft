"""The Thanksgiving side dishes (tools/thanksgiving_dishes.py; docs/features/thanksgiving-dishes.md): the generated files,
Java, hooks, names, recipes, balance, outlines and essence records agree (tools/thanksgiving_dishes_check.py), and the
table's own rules hold."""
import json
from pathlib import Path
import sys
import unittest

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))

import thanksgiving_dishes as td  # noqa: E402


class ThanksgivingDishesTests(unittest.TestCase):
    def test_everything_agrees(self):
        import thanksgiving_dishes_check
        self.assertEqual(thanksgiving_dishes_check.problems(ROOT), [])

    def test_six_new_dishes_none_already_in_the_game(self):
        # The coordinator's candidates that Jugcraft already had were substituted (the record's inventory table): no
        # dish reuses an existing ID or name, and none is the dish it stands in for.
        self.assertEqual(len(td.DISHES), 6)
        lang = json.loads((ROOT / "src/main/resources/assets/jugcraft/lang/en_us.json").read_text(encoding="utf-8"))
        names = {spec["display"] for spec in td.DISHES.values()}
        self.assertEqual(len(names), 6, "six different names")
        for existing in ("baked_acorn_squash", "cranberry_pie", "three_sisters_stew", "cornbread", "herb_roasted_potatoes"):
            self.assertNotIn(existing, td.DISHES)
            self.assertTrue(f"item.jugcraft.{existing}" in lang or f"block.jugcraft.{existing}" in lang,
                            f"{existing}, which a dish stands in for or beside, is in the game")
        others = {text for key, text in lang.items() if key.split(".")[-1] not in td.DISHES}
        self.assertFalse(names & others, "no dish shares a name with anything else")

    def test_every_dish_is_a_bowl_food_that_costs_its_ingredients(self):
        import thanksgiving_dishes_check as check
        values = json.loads((ROOT / "src/main/resources/data/jugcraft/alchemy/values.json").read_text(encoding="utf-8"))
        for dish, spec in td.DISHES.items():
            self.assertEqual(spec["inputs"][0], "minecraft:bowl", f"{dish} starts from the bowl eating it gives back")
            cost = check.recipe_cost(spec["inputs"], values["items"], values["fallback"])
            self.assertGreater(cost, values["fallback"], f"{dish} is worth more than the one-essence fallback")
            hunger, _ = spec["food"]
            self.assertLessEqual(hunger, sum(td.MOST_HUNGER[i] for i in spec["inputs"]) + td.COOK_BONUS, dish)
            self.assertIn(spec["shape"], check.OUTLINES)

    def test_the_menus_meals_never_see_the_dishes(self):
        # The dishes are lent to the menu's meal modules only while one is built (thanksgiving_dishes_data.lent).
        import menu_meal_models as mm
        import menu_meal_textures as mt
        import thanksgiving_dishes_data as tdd
        before = (set(mm.MEALS), set(mt.MEALS))
        tdd.block_model("glazed_carrots")
        tdd.textures()
        self.assertEqual((set(mm.MEALS), set(mt.MEALS)), before)
        self.assertFalse(set(td.DISHES) & before[0])
        self.assertEqual(set(tdd.MODELS), set(td.DISHES))
        self.assertEqual(set(tdd.TEXTURES), set(td.DISHES))


if __name__ == "__main__":
    unittest.main()
