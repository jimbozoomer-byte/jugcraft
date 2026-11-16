"""The Encyclopedia's route maps (tools/encyclopedia_progression.py -> jugcraft/progression_routes.json) against the data.

Every route item, machine and recipe reference must resolve to a registered id and to an existing recipe whose inputs and
outputs match what the route says. These checks re-read the recipe, tag, loot-table, handbook, codex and Java sources
on their own (they do not reuse the generator's derivation), so a recipe changed by hand or by another generator shows
up here. The item registry is the game-exported snapshot docs/audits/survival-item-registry.json; the server game test
ProgressionRoutesGameTests repeats the recipe checks against a live recipe manager.
"""
import json
import re
import sys
import unittest
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))
import encyclopedia_progression as progression  # noqa: E402

RES = ROOT / "src/main/resources"
DATA = RES / "data"
JAVA = ROOT / "src/main/java/io/github/jimbozoomer/jugcraft"
CLIENT = ROOT / "src/client/java/io/github/jimbozoomer/jugcraft/client"
ROUTES = RES / "jugcraft/progression_routes.json"
REGISTRY = ROOT / "docs/audits/survival-item-registry.json"

# Members of the vanilla and Fabric convention tags these recipes use, as the pinned 26.3 game defines them (not in
# this repository). Only the members a route names are needed.
EXTERNAL = {
    "c:ingots/copper": {"minecraft:copper_ingot"},
    "c:ingots/iron": {"minecraft:iron_ingot"},
    "c:ingots/gold": {"minecraft:gold_ingot"},
    "c:nuggets/gold": {"minecraft:gold_nugget"},
    "c:gems/lapis": {"minecraft:lapis_lazuli"},
    "c:glass_blocks": {"minecraft:glass"},
    "minecraft:planks": {"minecraft:oak_planks"},
}
VANILLA_STATIONS = {"minecraft:crafting_shaped": "minecraft:crafting_table",
                    "minecraft:crafting_shapeless": "minecraft:crafting_table",
                    "minecraft:smelting": "minecraft:furnace", "minecraft:blasting": "minecraft:blast_furnace"}
# What PageWriter draws as a grid: crafting, stonecutting, and furnace, machine and starter-workshop processes.
DRAWN = set(VANILLA_STATIONS) | {"minecraft:stonecutting", "jugcraft:starter_working"}


def load(path):
    return json.loads(path.read_text(encoding="utf-8"))


def split(ref):
    return ref.split(":", 1) if ":" in ref else ("minecraft", ref)


def members(tag, seen=frozenset()):
    if tag in EXTERNAL:
        return set(EXTERNAL[tag])
    namespace, path = split(tag)
    file = DATA / namespace / "tags/item" / f"{path}.json"
    if tag in seen or not file.is_file():
        return set()
    out = set()
    for value in load(file)["values"]:
        ref = value["id"] if isinstance(value, dict) else value
        out |= members(ref[1:], seen | {tag}) if ref.startswith("#") else {ref}
    return out


def accepts(ingredient):
    """The items an ingredient accepts, as the recipe file states it."""
    if isinstance(ingredient, list):
        return set().union(*(accepts(option) for option in ingredient))
    if isinstance(ingredient, dict):
        return accepts(ingredient["item"]) if "item" in ingredient else members(ingredient["tag"])
    return members(ingredient[1:]) if ingredient.startswith("#") else {ingredient}


def recipe_parts(data):
    """[(accepted items, count)] for a recipe file, one entry per distinct ingredient."""
    kind = data["type"]
    if kind == "minecraft:crafting_shaped":
        raw = [json.dumps(data["key"][c], sort_keys=True) for row in data["pattern"] for c in row if c != " "]
        counted = Counter(raw)
        return [(accepts(json.loads(key)), count) for key, count in counted.items()]
    if kind == "minecraft:crafting_shapeless":
        counted = Counter(json.dumps(i, sort_keys=True) for i in data["ingredients"])
        return [(accepts(json.loads(key)), count) for key, count in counted.items()]
    if "ingredients" in data:
        return [(accepts(part["ingredient"]), part.get("count", 1)) for part in data["ingredients"]]
    if kind == "imbuement":
        return [(accepts(data["input"]), 1)]
    return [(accepts(data["ingredient"]), data.get("input_count", 1))]


def recipe_data(step_source, rid):
    namespace, path = split(rid)
    folder = "nouveau_recipe" if step_source == "nouveau" else "recipe"
    file = DATA / namespace / folder / f"{path}.json"
    return load(file) if file.is_file() else None


def machine_stations():
    """jugcraft:<recipe type> -> jugcraft:<machine item>, read from MachineKind.java (its enum ids and recipeType())."""
    source = (JAVA / "machine/MachineKind.java").read_text(encoding="utf-8")
    ids = dict(re.findall(r'^\t([A-Z_]+)\("([a-z_]+)"', source, re.M))
    body = source[source.index("public String recipeType()"):]
    body = body[:body.index("default -> null;")]
    types = dict(re.findall(r'case ([A-Z_]+) -> "([a-z_]+)";', body))
    return {f"jugcraft:{kind}": f"jugcraft:{ids[name]}" for name, kind in types.items() if name in ids}


def workshop_stations():
    source = (JAVA / "workshop/JugcraftWorkshop.java").read_text(encoding="utf-8")
    found = re.search(r'STATIONS = \{([^}]*)\}', source)
    return ["jugcraft:" + name for name in re.findall(r'"([a-z_]+)"', found.group(1))]


def station_of(source, data):
    kind = data["type"]
    if source == "nouveau":
        java = (JAVA / "nouveau/NouveauRecipes.java").read_text(encoding="utf-8")
        assert 'case "imbuement" -> station.equals("imbuement_chamber");' in java
        return "jugcraft:nouveau_imbuement_chamber" if kind == "imbuement" else None
    if kind == "jugcraft:starter_working":
        return workshop_stations()[data["station"]]
    return VANILLA_STATIONS.get(kind) or machine_stations().get(kind)


def result_of(data):
    result = data.get("result") or data.get("output")
    return result["id"], result.get("count", 1)


def java_int(path, name):
    found = re.search(rf"\b{name} = (\d+);", path.read_text(encoding="utf-8"))
    return int(found.group(1))


class ProgressionRouteDataTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.data = load(ROUTES)
        cls.registry = set(load(REGISTRY))
        cls.handbook = load(RES / "assets/jugcraft/handbook/en_us.json")
        cls.steps = [(route, step) for route in cls.data["routes"] for group in route["groups"] for step in group["steps"]]

    def test_file_is_the_generators_output(self):
        self.assertEqual(self.data, progression.build(self.handbook))

    def test_two_routes_with_their_pages(self):
        self.assertEqual([(r["id"], r["page"]) for r in self.data["routes"]],
                         [("technology", "route_technology"), ("magic", "route_magic")])
        service = (JAVA / "guide/encyclopedia/Encyclopedia.java").read_text(encoding="utf-8")
        pages = re.findall(r'page\("([a-z_]+)", ', service)
        self.assertEqual(pages[-2:], ["route_technology", "route_magic"])
        writer = (JAVA / "guide/encyclopedia/ProgressionRoutes.java").read_text(encoding="utf-8")
        self.assertIn('TECHNOLOGY = "route_technology"', writer)
        self.assertIn('MAGIC = "route_magic"', writer)
        self.assertIn('RESOURCE = "/jugcraft/progression_routes.json"', writer)

    def test_every_item_and_machine_is_registered(self):
        for route, step in self.steps:
            refs = [step["item"], step.get("machine"), step.get("from"), step.get("tool")]
            refs += [part["item"] for part in step["inputs"] + step["outputs"]]
            refs += [part["how"].get("machine") for part in step["inputs"] if "how" in part]
            refs += [alt["machine"] for alt in step["alternatives"]]
            for ref in filter(None, refs):
                with self.subTest(route=route["id"], step=step["id"], item=ref):
                    self.assertIn(ref, self.registry)

    def test_every_recipe_exists_and_matches_the_step(self):
        for route, step in self.steps:
            if step["source"] not in ("recipe", "nouveau"):
                continue
            with self.subTest(route=route["id"], step=step["id"]):
                data = recipe_data(step["source"], step["recipe"])
                self.assertIsNotNone(data, f"{step['recipe']} has no file")
                self.assertEqual(result_of(data), (step["outputs"][0]["item"], step["outputs"][0]["count"]))
                self.assertEqual(step["item"], step["outputs"][0]["item"])
                self.assertEqual(station_of(step["source"], data), step["machine"])
                expected = recipe_parts(data)
                self.assertEqual(len(expected), len(step["inputs"]), "one route input per distinct ingredient")
                for (items, count), part in zip(expected, step["inputs"]):
                    self.assertIn(part["item"], items, f"{part['item']} is not accepted by its ingredient")
                    self.assertEqual(part["count"], count)
                if data["type"] == "jugcraft:starter_working":
                    self.assertEqual(step.get("tool"), data.get("tool", {}).get("id"))

    def test_alternatives_and_off_route_inputs_name_real_recipes(self):
        for route, step in self.steps:
            for alt in step["alternatives"]:
                with self.subTest(step=step["id"], alternative=alt["recipe"]):
                    data = recipe_data("recipe", alt["recipe"])
                    self.assertIsNotNone(data)
                    self.assertEqual(result_of(data)[0], step["item"])
                    self.assertEqual(station_of("recipe", data), alt["machine"])
            for part in step["inputs"]:
                how = part.get("how")
                if part.get("from_step"):
                    self.assertIsNone(how)
                    continue
                with self.subTest(step=step["id"], input=part["item"]):
                    self.assertIsNotNone(how, "an input either comes from a step or says how it is made")
                    if how["method"] == "vanilla":
                        self.assertEqual(split(part["item"])[0], "minecraft")
                        continue
                    data = recipe_data("recipe", how["recipe"])
                    self.assertIsNotNone(data)
                    self.assertEqual(result_of(data)[0], part["item"])
                    self.assertEqual(station_of("recipe", data), how["machine"])

    def test_gathered_materials_drop_from_their_blocks(self):
        for route, step in self.steps:
            if step["source"] != "gathering":
                continue
            with self.subTest(step=step["id"]):
                if step["vanilla"]:
                    self.assertEqual(split(step["from"])[0], "minecraft", "only vanilla blocks may skip the loot check")
                    continue
                namespace, path = split(step["from"])
                table = (DATA / namespace / "loot_table/blocks" / f"{path}.json").read_text(encoding="utf-8")
                self.assertIn(f'"name": "{step["item"]}"', table.replace('"name":"', '"name": "'))

    def test_connections_point_back_and_required_steps_need_no_optional_ones(self):
        for route in self.data["routes"]:
            seen = {}
            for group in route["groups"]:
                for step in group["steps"]:
                    with self.subTest(route=route["id"], step=step["id"]):
                        self.assertIn(step["role"], ("required", "recommended", "alternative"))
                        for need in step["needs"] + step["alt_needs"]:
                            self.assertIn(need, seen, "a connection points to an earlier step")
                        if step["role"] == "required":
                            self.assertTrue(all(seen[need] == "required" for need in step["needs"]),
                                            "no hidden gate: a required step needs only required steps")
                        if step["kind"] == "milestone":
                            self.assertTrue(step["needs"], "a milestone is reached through earlier steps")
                        for part in step["inputs"]:
                            if part.get("from_step"):
                                self.assertIn(part["from_step"], step["needs"] + step["alt_needs"])
                        seen[step["id"]] = step["role"]
            starts = [step for group in route["groups"] for step in group["steps"] if not step["needs"]]
            self.assertTrue(starts and starts[0] is route["groups"][0]["steps"][0], "a route begins with a step of its own")
            self.assertTrue(any(group["milestone"] for group in route["groups"]))

    def test_entries_open_existing_pages(self):
        service = (JAVA / "guide/encyclopedia/Encyclopedia.java").read_text(encoding="utf-8")
        pages = set(re.findall(r'page\("([a-z_]+)", ', service))
        categories = DATA / "jugcraft/modonomicon/books/arcane_concordance/categories"
        for route, step in self.steps:
            link = step["link"]
            with self.subTest(step=step["id"], link=link):
                if link["type"] == "handbook":
                    chapter = self.handbook["chapters"][link["chapter"]]
                    self.assertLess(link["page"], len(chapter["pages"]))
                elif link["type"] == "page":
                    self.assertIn(link["target"], pages)
                else:
                    self.assertEqual(link["type"], "codex")
                    self.assertTrue((categories / f"{split(link['category'])[1]}.json").is_file())

    def test_pages_fit_their_row_and_grid_bounds(self):
        payload = JAVA / "guide/encyclopedia/EncyclopediaPayload.java"
        max_rows = java_int(payload, "MAX_ROWS")
        max_grids = java_int(JAVA / "guide/encyclopedia/PageWriter.java", "MAX_GRIDS")
        shops = load(DATA / "jugcraft/town/shops.json")["shops"]
        offers = Counter(offer["item"] for shop in shops.values() for offer in shop.get("sells", []))
        for route in self.data["routes"]:
            rows = len(route["summary"])
            grids = 0
            for group in route["groups"]:
                rows += 1 + bool(group["text"])
                for step in group["steps"]:
                    rows += 1 + bool(step["note"])
                    if step["kind"] == "milestone":
                        continue
                    rows += step["source"] in ("recipe", "nouveau")
                    rows += bool(step["inputs"]) + any("machine" in p.get("how", {}) for p in step["inputs"])
                    rows += bool(step.get("tool")) + len(step["alternatives"])
                    rows += offers[step["item"]] + sum(offers[p["item"]] for p in step["inputs"])
                    recipes = [step["recipe"]] if step["source"] == "recipe" else []
                    recipes += [alt["recipe"] for alt in step["alternatives"]]
                    for rid in recipes:
                        kind = recipe_data("recipe", rid)["type"]
                        grids += kind in DRAWN or kind in machine_stations()
            with self.subTest(route=route["id"]):
                self.assertLess(rows, max_rows, "every row fits, with the overflow row to spare")
                self.assertLessEqual(grids, max_grids, "every recipe is drawn as a grid")

    def test_every_word_is_generated(self):
        words = dict(progression.LANG)
        sources = [JAVA / "guide/encyclopedia/ProgressionRoutes.java", CLIENT / "EncyclopediaRouteMap.java",
                   CLIENT / "EncyclopediaRoutesScreen.java", CLIENT / "EncyclopediaScreen.java", CLIENT / "EncyclopediaWidgets.java"]
        lang = load(RES / "assets/jugcraft/lang/en_us.json")
        for path in sources:
            text = path.read_text(encoding="utf-8")
            keys = set(re.findall(r'"((?:encyclopedia|screen)\.jugcraft\.[a-z_.]+)"', text))
            keys |= {"ui.jugcraft." + key for key in re.findall(r'\bui\("([a-z_.]+)"', text)}
            for key in sorted(keys):
                if key.endswith("."):
                    continue
                with self.subTest(file=path.name, key=key):
                    self.assertTrue(key in words or key in lang, key)
        methods = {step["method"] for _route, step in self.steps}
        for method in methods:
            self.assertIn(f"encyclopedia.jugcraft.progression.method.{method}", words)
            if method != "milestone":
                self.assertIn(f"ui.jugcraft.book.map.method.{method}", words)
        for role in ("required", "recommended", "alternative", "milestone"):
            self.assertIn(f"ui.jugcraft.book.map.role.{role}", words)

    def test_language_file_has_the_route_words(self):
        """Holds once the coordinator has merged the generated words into the language file."""
        lang = load(RES / "assets/jugcraft/lang/en_us.json")
        missing = sorted(set(progression.LANG) - set(lang))
        missing += [key for key in ("encyclopedia.jugcraft.page.route_technology", "encyclopedia.jugcraft.page.route_magic",
                                    "encyclopedia.jugcraft.edge.alternative") if key not in lang]
        self.assertEqual(missing, [])


if __name__ == "__main__":
    unittest.main()
