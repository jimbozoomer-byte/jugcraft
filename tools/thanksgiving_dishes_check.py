"""Checks for the Thanksgiving side dishes (tools/thanksgiving_dishes.py; docs/features/thanksgiving-dishes.md).

problems(repo) lists everything wrong, as text, empty when all is well. tools/check_mod_data.py reports each as an
error (check_thanksgiving_dishes) and scripts/tests/test_thanksgiving_dishes.py asserts there are none. It checks that:
- the Java lists each dish with the table's food and set-down shape, in the table's order (ThanksgivingDishes), eaten
  as a bowl food one to a stack; JugcraftAgriculture registers them in the food tab and the pie's set-down phase; the
  game tests are listed for the gametest entrypoint; and tools/agriculture.py, agriculture_data.py and crop_textures.py
  know the dishes, so the full generator writes what tools/thanksgiving_dishes_data.py writes;
- every generated file on disk is exactly what tools/thanksgiving_dishes_data.py makes now (models, blockstates, items,
  loot, recipes and textures), and every JSON one is listed in the generated-resource manifest;
- each dish drops its food once, faces every way, and has its item and block names in en_us.json;
- every recipe is shapeless, follows the agriculture switch only, makes one dish from at most nine ingredients, one of
  them the bowl it gives back, none a filled container, none another dish, and none unknown;
- each dish's food follows docs/BALANCE.md: at most COOK_BONUS more hunger than its ingredients could give;
- each model lies on the floor of its block, inside its set-down outline (PlacedDishBlock.DishShape);
- each dish's essence record (data/jugcraft/alchemy/values.json) is its recipe cost, by tools/build_alchemy_values.py's
  formula, never the one-essence fallback; no dish is formable from essence (jugcraft:tablet_outputs), sold or bought
  by a town shop, or taken or made by any other recipe, so no essence or Jugs loop runs through one.
"""
import json
import re
import sys
from fractions import Fraction
from pathlib import Path

MOD = "jugcraft"
# PlacedDishBlock.DishShape's outlines (x0, y0, z0, x1, y1, z1, in pixels) for the shapes the dishes use.
OUTLINES = {"PLATE": (1.0, 0.0, 1.0, 15.0, 5.5, 15.0), "BOWL": (3.0, 0.0, 3.0, 13.0, 6.5, 13.0)}
# What no dish recipe may take: a filled container (the value builder skips recipes that give one back).
CONTAINERS = ("bucket", "bottle")


def _read(path):
    return path.read_text(encoding="utf-8") if path.is_file() else ""


def _load(path):
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except (OSError, ValueError):
        return None


def _body(text, header):
    """The text of a Python function from its `def` line to the next top-level line."""
    start = text.find(header)
    if start < 0:
        return ""
    end = re.search(r"\n(?=\S)", text[start + len(header):])
    return text[start:start + len(header) + end.start()] if end else text[start:]


def _java_float(value):
    return f"{value}F"


def recipe_cost(inputs, values, fallback):
    """tools/build_alchemy_values.py's recipe cost for a shapeless recipe of plain items making one: each ingredient's
    value (the fallback when it has none), summed."""
    total = Fraction(0)
    for ingredient in inputs:
        record = values.get(ingredient)
        total += Fraction(record["numerator"], record["denominator"]) if record else Fraction(fallback)
    return total


def problems(repo):
    repo = Path(repo)
    tools = repo / "tools"
    if str(tools) not in sys.path:
        sys.path.insert(0, str(tools))
    import thanksgiving_decor_data as decor
    import thanksgiving_dishes as td
    import thanksgiving_dishes_data as tdd

    out = []
    res = repo / "src" / "main" / "resources"
    assets, data = res / "assets" / MOD, res / "data" / MOD
    java = repo / "src" / "main" / "java" / "io" / "github" / "jimbozoomer" / "jugcraft" / "agriculture"

    # The Java: the table's dishes, foods and shapes, in order; eaten as a bowl food; registered and set down.
    reg = _read(java / "ThanksgivingDishes.java")
    listed = re.findall(r'new Dish\("([a-z_]+)"', reg)
    if listed != list(td.DISHES):
        out.append(f"ThanksgivingDishes.DISHES must list {list(td.DISHES)} in that order (tools/thanksgiving_dishes.py), not {listed}")
    for dish, spec in td.DISHES.items():
        hunger, saturation = spec["food"]
        entry = f'new Dish("{dish}", {hunger}, {_java_float(saturation)}, DishShape.{spec["shape"]})'
        if entry not in reg:
            out.append(f"ThanksgivingDishes.DISHES must have {entry} (tools/thanksgiving_dishes.py DISHES)")
        if spec["shape"] not in OUTLINES:
            out.append(f"{dish}: shape {spec['shape']} must be one of {sorted(OUTLINES)}")
    for needed in (".food(food).usingConvertsTo(Items.BOWL).stacksTo(1)", "new PlacedDishBlock(item, dish.shape(), props)",
                   "UseBlockCallback.EVENT.register(setDownPhase, (player, level, hand, hit) -> PlacedDishBlock.setDown(placed, player, level, hand, hit));"):
        if needed not in reg:
            out.append(f"ThanksgivingDishes.register must have {needed}")
    placed = _read(java / "PlacedDishBlock.java")
    for shape, box in OUTLINES.items():
        if f"{shape}(Block.box({', '.join(str(v) for v in box)}))" not in placed:
            out.append(f"PlacedDishBlock.DishShape.{shape} must be the outline {box} this check measures the models against")
    agriculture = _read(java / "JugcraftAgriculture.java")
    hook = ("ThanksgivingDishes.register(JugcraftAgriculture::registerBlock, (id, factory, props) -> registerItem(id, factory, props, FOOD_TAB), "
            "SET_DOWN_PHASE);")
    if hook not in agriculture:
        out.append(f"JugcraftAgriculture must call {hook}")
    elif agriculture.find(hook) < agriculture.find("registerPlacedDishes();"):
        out.append("JugcraftAgriculture must register the Thanksgiving dishes after registerPlacedDishes() (the set-down phase is ordered by then)")
    gametest = _read(repo / "src" / "gametest" / "resources" / "fabric.mod.json")
    if "io.github.jimbozoomer.jugcraft.test.ThanksgivingDishGameTests" not in gametest:
        out.append("src/gametest/resources/fabric.mod.json must list io.github.jimbozoomer.jugcraft.test.ThanksgivingDishGameTests")
    lists = _read(tools / "agriculture.py")
    for header, needed in (("def itemless_blocks():", "thanksgiving_dishes.blocks()"), ("def all_blocks():", "thanksgiving_dishes.blocks()"),
                           ("def all_items():", "thanksgiving_dishes.items()")):
        if needed not in _body(lists, header):
            out.append(f"tools/agriculture.py {header[4:-1]} must include {needed}")
    if "\nimport thanksgiving_dishes\n" not in lists:
        out.append("tools/agriculture.py must import thanksgiving_dishes")
    generator = _read(tools / "agriculture_data.py")
    for needed in ("\nimport thanksgiving_dishes_data\n", "thanksgiving_dishes_data.assets(root, write, lang)",
                   "thanksgiving_dishes_data.loot(out, write)", "thanksgiving_dishes_data.recipes(out, write, conditions)"):
        if needed not in generator:
            out.append(f"tools/agriculture_data.py must call {needed.strip()}")
    if "from thanksgiving_dishes_data import textures" not in _read(tools / "crop_textures.py"):
        out.append("tools/crop_textures.py must paint the dishes' textures (from thanksgiving_dishes_data import textures)")

    # The generated files, exactly as the generator makes them now.
    expected = {}

    def keep(path, obj):
        import model_writer
        if isinstance(obj, dict) and obj.get("elements") and "models" in path.parts:
            model_writer.finish_elements(obj["elements"])
        expected[path] = json.dumps(obj, indent=2) + "\n"
    lang = {}
    tdd.assets(assets, keep, lang)
    tdd.loot(data / "loot_table" / "blocks", keep)
    tdd.recipes(data / "recipe", keep, tdd._conditions)
    for path, text in expected.items():
        if _read(path) != text:
            out.append(f"{path.relative_to(repo)} is not what tools/thanksgiving_dishes_data.py makes (regenerate it)")
    from PIL import Image
    for (kind, name), img in tdd.textures().items():
        path = assets / "textures" / kind / f"{name}.png"
        if not path.is_file():
            out.append(f"{path.relative_to(repo)} is missing")
            continue
        on_disk = Image.open(path).convert("RGBA")
        if on_disk.size != img.size or on_disk.tobytes() != img.convert("RGBA").tobytes():
            out.append(f"{path.relative_to(repo)} is not what tools/thanksgiving_dishes_data.py paints (regenerate it)")
    manifest = set(_load(tools / "data" / "generated-material-resources.json") or [])
    for path in expected:
        rel = path.relative_to(res).as_posix()
        if rel not in manifest:
            out.append(f"tools/data/generated-material-resources.json must list {rel}")

    # Drops, facings, names.
    en = _load(assets / "lang" / "en_us.json") or {}
    for dish in td.DISHES:
        pools = (_load(data / "loot_table" / "blocks" / f"{dish}.json") or {}).get("pools", [])
        if len(pools) != 1 or pools[0].get("rolls") != 1 or [e.get("name") for e in pools[0].get("entries", [])] != [f"{MOD}:{dish}"]:
            out.append(f"loot_table/blocks/{dish}.json: it drops its food, once")
        states = (_load(assets / "blockstates" / f"{dish}.json") or {}).get("variants", {})
        if sorted(states) != sorted(f"facing={f}" for f in ("north", "east", "south", "west")):
            out.append(f"blockstates/{dish}.json must cover every facing, and nothing else")
    for key, text in td.names().items():
        if en.get(key) != text:
            out.append(f"en_us.json: {key} must be {text!r}")

    # Recipes, and the food each gives against its ingredients (docs/BALANCE.md).
    for dish, spec in td.DISHES.items():
        made = _load(data / "recipe" / f"{dish}.json") or {}
        if made.get("type") != "minecraft:crafting_shapeless" or made.get("result") != {"id": f"{MOD}:{dish}", "count": 1}:
            out.append(f"recipe/{dish}.json must be a shapeless recipe making one {dish}")
        if made.get("fabric:load_conditions") != [{"condition": f"{MOD}:feature_enabled", "feature": td.FEATURE}]:
            out.append(f"recipe/{dish}.json must follow the {td.FEATURE} switch only (no research, no calendar)")
        inputs = spec["inputs"]
        if not 1 <= len(inputs) <= 9:
            out.append(f"recipe/{dish}.json must fit a crafting grid")
        if inputs.count("minecraft:bowl") != 1:
            out.append(f"recipe/{dish}.json must take exactly one bowl, the one eating it gives back")
        for ingredient in inputs:
            name = ingredient.split(":", 1)[1]
            if any(word in name for word in CONTAINERS):
                out.append(f"recipe/{dish}.json must take no filled container ({ingredient})")
            if name in td.DISHES:
                out.append(f"recipe/{dish}.json must not take another dish ({ingredient})")
            if ingredient.startswith(f"{MOD}:") and f"item.{MOD}.{name}" not in en and f"block.{MOD}.{name}" not in en:
                out.append(f"recipe/{dish}.json needs {ingredient}, which must exist")
            if ingredient not in td.MOST_HUNGER:
                out.append(f"tools/thanksgiving_dishes.py MOST_HUNGER must give {ingredient}'s most hunger")
        hunger, saturation = spec["food"]
        most = sum(td.MOST_HUNGER.get(i, 0) for i in inputs) + td.COOK_BONUS
        if not 1 <= hunger <= most:
            out.append(f"{dish}: its food {hunger} must be at least 1 and at most its ingredients' {most - td.COOK_BONUS} + {td.COOK_BONUS}")
        if not 0 < saturation <= 1.0:
            out.append(f"{dish}: its saturation modifier {saturation} must be above 0 and at most 1.0")

    # Models: on the floor of their block, inside the set-down outline (every facing turns about the block's middle,
    # and the outlines are square, so the south-facing model stands for all four).
    for dish, spec in td.DISHES.items():
        box = OUTLINES.get(spec["shape"])
        if not box:
            continue
        elements = tdd.block_model(dish)["elements"]
        x0, y0, z0, x1, y1, z1 = decor.bounds(elements)
        bx0, by0, bz0, bx1, by1, bz1 = box
        if abs(y0) > 1e-6:
            out.append(f"{dish}: its model must stand on the floor, not at y {round(y0, 3)}")
        if x0 < bx0 - 1e-6 or z0 < bz0 - 1e-6 or x1 > bx1 + 1e-6 or z1 > bz1 + 1e-6 or y1 > by1 + 1e-6:
            out.append(f"{dish}: the {spec['shape']} outline {box} must cover its model "
                       f"{[round(v, 3) for v in (x0, y0, z0, x1, y1, z1)]}")

    # Essence: each record is its recipe cost, never the fallback; nothing forms, sells, buys or takes a dish.
    book = _load(data / "alchemy" / "values.json") or {}
    values, fallback = book.get("items", {}), book.get("fallback", 1)
    for dish, spec in td.DISHES.items():
        record = values.get(f"{MOD}:{dish}")
        want = recipe_cost(spec["inputs"], values, fallback)
        if not record:
            out.append(f"data/jugcraft/alchemy/values.json must have a record for {MOD}:{dish} ({want}, its recipe cost)")
            continue
        have = Fraction(record.get("numerator", 0), record.get("denominator", 1))
        if have != want or record.get("source") != f"recipe {MOD}:{dish}":
            out.append(f"values.json {MOD}:{dish} must be {want.numerator}/{want.denominator} from \"recipe {MOD}:{dish}\", not {record}")
        if str(record.get("source", "")).startswith("fallback"):
            out.append(f"values.json {MOD}:{dish} must not sit at the one-essence fallback")
    formable = set((_load(data / "tags" / "item" / "tablet_outputs.json") or {}).get("values", []))
    shops = _read(data / "town" / "shops.json")
    for dish in td.DISHES:
        if f"{MOD}:{dish}" in formable:
            out.append(f"tags/item/tablet_outputs.json must not list {MOD}:{dish}: a dish is not formed from essence")
        if f'"{MOD}:{dish}"' in shops:
            out.append(f"town/shops.json must not sell or buy {MOD}:{dish}")
    for path in sorted(res.glob("data/*/recipe/**/*.json")):
        if path.parent == data / "recipe" and path.stem in td.DISHES:
            continue
        text = _read(path)
        for dish in td.DISHES:
            if f'"{MOD}:{dish}"' in text:
                out.append(f"{path.relative_to(repo)} must not take or make {MOD}:{dish} (its one recipe is its only route)")
    return out


if __name__ == "__main__":
    found = problems(Path(__file__).resolve().parent.parent)
    for line in found:
        print(line)
    print(f"{len(found)} problems")
    sys.exit(1 if found else 0)
