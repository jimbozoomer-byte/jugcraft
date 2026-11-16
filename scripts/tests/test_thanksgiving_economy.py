"""Thanksgiving package (groups A to D): acquisition, loop and essence checks for every new or refined item.

Offline data checks, not game tests:
- each item has an explicit essence record. New ones are derived from their one recipe by the formula
  tools/build_alchemy_values.py uses: the sum of each ingredient's cheapest option, divided by the count. Item tags are
  read from the repository, and 26.3's vanilla item tags from tools/data/survival-reference-26.3.json. No item sits at
  the one-essence fallback;
- only explicitly priced inexpensive decor is formable; no town shop buys it,
  and no material-processing recipe can convert it into valuables;
- its recipe follows the agriculture switch only (no research, no calendar), and nothing else consumes it;
- breaking it drops exactly the one item, once (a multiblock from its part 0 only).
"""
import json
import unittest
from collections import Counter
from fractions import Fraction
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DATA = ROOT / "src" / "main" / "resources" / "data"

NEW = {
    "A": ("oak_settle", "carved_dish_dresser", "pilgrim_peg_rail", "pewter_pitcher_and_basin", "pewter_chamberstick", "harvest_sampler"),
    "B": ("lidded_twined_basket", "twined_pack_basket", "birch_bark_box", "rush_floor_mat", "carved_bowls_and_spoons",
          "incised_earthenware_pot", "corn_drying_frame", "wooden_corn_mortar"),
    "C": ("timber_harvest_arch", "hanging_crock_rack", "folded_linen_stack", "copper_chafing_dish", "pumpkin_carving_bench"),
    "D": ("corn_husk_dolls", "turkey_pull_toy", "spinning_top", "mayflower_model_ship", "harvest_photo_backdrop", "corn_toss_set",
          "prize_pumpkin_display", "harvest_percussion_set"),
}
REFINED = ("quilt_rail", "butter_churn", "window_box")
MULTIBLOCKS = ("oak_settle", "carved_dish_dresser", "timber_harvest_arch", "harvest_photo_backdrop")
ALL_NEW = tuple(i for ids in NEW.values() for i in ids)


def load(path):
    return json.loads(path.read_text(encoding="utf-8"))


VALUES = load(DATA / "jugcraft" / "alchemy" / "values.json")["items"]


def value(item):
    record = VALUES[item]
    return Fraction(record["numerator"], record["denominator"])


def item_tags():
    reference = load(ROOT / "tools" / "data" / "survival-reference-26.3.json")["tags"]
    tags = {k: list(v) if isinstance(v, list) else v.get("values", []) for k, v in reference.items()}
    for path in sorted(DATA.glob("*/tags/item/**/*.json")):
        namespace = path.relative_to(DATA).parts[0]
        key = namespace + ":" + path.relative_to(DATA / namespace / "tags" / "item").with_suffix("").as_posix()
        data = load(path)
        tags[key] = ([] if data.get("replace") else tags.get(key, [])) + [v if isinstance(v, str) else v.get("id") for v in data.get("values", [])]
    return tags


TAGS = item_tags()


def expand(x, seen=frozenset()):
    if isinstance(x, dict):
        x = x.get("item", x.get("id", "#" + x["tag"] if "tag" in x else ""))
    if isinstance(x, list):
        return set().union(*(expand(i, seen) for i in x))
    if not isinstance(x, str) or not x:
        return set()
    if not x.startswith("#"):
        return {x}
    tag = x[1:]
    return set() if tag in seen else set().union(*(expand(v, seen | {tag}) for v in TAGS.get(tag, [])))


def recipe(item):
    return load(DATA / "jugcraft" / "recipe" / f"{item}.json")


def recipe_value(r):
    """tools/build_alchemy_values.py's recipe cost: each ingredient's cheapest valued option, summed, per output."""
    if r["type"] == "minecraft:crafting_shapeless":
        ingredients = [expand(x) for x in r["ingredients"]]
    else:
        ingredients = []
        for symbol, n in Counter("".join(r["pattern"])).items():
            if symbol != " ":
                ingredients.extend([expand(r["key"][symbol])] * n)
    costs = [min(value(i) for i in options if i in VALUES) for options in ingredients]
    return sum(costs) / r["result"].get("count", 1)


class ThanksgivingEconomy(unittest.TestCase):
    def test_every_item_has_an_explicit_record_above_the_fallback(self):
        for item in ALL_NEW + REFINED:
            with self.subTest(item=item):
                self.assertIn("jugcraft:" + item, VALUES)
                self.assertGreater(value("jugcraft:" + item), 1)
                self.assertNotIn("fallback", VALUES["jugcraft:" + item]["source"])

    def test_new_records_are_their_recipe_costs(self):
        for item in ALL_NEW:
            with self.subTest(item=item):
                self.assertEqual(VALUES["jugcraft:" + item]["source"], "recipe jugcraft:" + item)
                self.assertEqual(value("jugcraft:" + item), recipe_value(recipe(item)))

    def test_only_inexpensive_decor_is_formable_and_no_shop_buys_it(self):
        formable = set(load(DATA / "jugcraft" / "tags" / "item" / "tablet_outputs.json")["values"])
        shops = load(DATA / "jugcraft" / "town" / "shops.json")["shops"]
        bought = {offer["item"] for shop in shops.values() for offer in shop.get("buys", [])}
        for item in ALL_NEW + REFINED:
            self.assertNotIn("jugcraft:" + item, bought, item)
        for item in ALL_NEW:
            self.assertEqual("jugcraft:" + item in formable, value("jugcraft:" + item) <= 128, item)

    def test_recipes_follow_the_agriculture_switch_only(self):
        for item in ALL_NEW + REFINED:
            with self.subTest(item=item):
                r = recipe(item)
                self.assertEqual(r["fabric:load_conditions"], [{"condition": "jugcraft:feature_enabled", "feature": "agriculture"}])
                self.assertEqual(r["result"]["id"], "jugcraft:" + item)

    def test_nothing_else_consumes_them(self):
        wanted = {"jugcraft:" + item for item in ALL_NEW}
        for path in sorted(DATA.glob("*/recipe/**/*.json")):
            text = path.read_text(encoding="utf-8")
            if not any(item in text for item in wanted):
                continue
            r = json.loads(text)
            result = r.get("result", {})
            produced = result.get("id") if isinstance(result, dict) else result
            consumed = json.dumps({k: v for k, v in r.items() if k != "result"})
            self.assertFalse([item for item in wanted if f'"{item}"' in consumed], f"{path.name} consumes a Thanksgiving item")
            self.assertIn(produced, wanted | {None}, path.name)

    def test_each_breaks_into_exactly_one_item(self):
        for item in ALL_NEW + REFINED:
            with self.subTest(item=item):
                pools = load(DATA / "jugcraft" / "loot_table" / "blocks" / f"{item}.json")["pools"]
                self.assertEqual(len(pools), 1)
                self.assertEqual(pools[0]["rolls"], 1)
                self.assertEqual([(e["type"], e["name"]) for e in pools[0]["entries"]], [("minecraft:item", "jugcraft:" + item)])
                self.assertFalse(any(e.get("functions") for e in pools[0]["entries"]))
                condition = json.dumps(pools[0].get("condition"))
                if item in MULTIBLOCKS:
                    self.assertIn('"state": {"part": "0"}', condition)
                else:
                    self.assertEqual(pools[0]["condition"], {"type": "minecraft:survives_explosion"})


if __name__ == "__main__":
    unittest.main()
