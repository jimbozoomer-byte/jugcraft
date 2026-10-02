"""Offline checks for Jugcraft's data files. This is NOT a Minecraft build or game test.

Verifies that every material ID has its blockstate/model/item definition/loot
table/texture/name, that every reference resolves, that Java registration matches
tools/materials.py, and that no recipe creates metal from nothing.
"""
import json
import re
import sys
from pathlib import Path

from PIL import Image

from materials import (MOD, METALS, MINERALS, ROCKS, ITEMS, FEATURES, COMPONENTS, PART_UNITS, CIRCUITS, WASHED_ORES,
                       all_blocks, all_items, feature_of)
import agriculture as ag
import petro
import deposits
import tank_display
from machines import (CROPS, MACHINES, STATS, ORE_PROCESSING_MULTIPLIER, ORE_WASHING_MULTIPLIER, BYPRODUCT_SHARE,
                      RENEWABLE_UNITS, WOODS, machine_blocks, machine_items, machine_recipes)

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "src" / "main" / "resources"
ASSETS = RES / "assets" / MOD
DATA = RES / "data"
JAVA_ROOT = ROOT / "src" / "main" / "java" / "io" / "github" / "jimbozoomer" / "jugcraft"
JAVA = JAVA_ROOT / "materials" / "JugcraftMaterials.java"
CONFIG = JAVA_ROOT / "config" / "JugcraftConfig.java"
WORLDGEN = JAVA_ROOT / "materials" / "JugcraftWorldgen.java"
MACHINE_JAVA = JAVA_ROOT / "machine" / "MachineKind.java"
AGRICULTURE_JAVA = JAVA_ROOT / "agriculture"
STYLE_PACK = RES / "resourcepacks" / "alternate_machines"

# Tags that Jugcraft reads but that vanilla/Fabric API define.
EXTERNAL_TAGS = ({"c:ingots/copper", "c:ingots/iron", "minecraft:stone_ore_replaceables",
                  "minecraft:deepslate_ore_replaceables", "minecraft:planks", "minecraft:campfires", "minecraft:mineable/axe",
                  "minecraft:mineable/shovel", "minecraft:leaves", "minecraft:eggs", "minecraft:dirt", "minecraft:mud",
                  "minecraft:grass_blocks", "minecraft:sand", "minecraft:wool", "minecraft:logs", "minecraft:candles",
                  "minecraft:stairs", "minecraft:slabs", "minecraft:walls", "minecraft:coals"}
                 | {f"minecraft:{tag}" for tag in WOODS.values()})

errors = []


def err(message):
    errors.append(message)


def load(path):
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except (OSError, ValueError) as exc:
        err(f"Unreadable JSON {path.relative_to(ROOT)}: {exc}")
        return None


def split(ref):
    ns, _, path = ref.partition(":")
    return ns, path


def tag_exists(registry, ref):
    ns, path = split(ref)
    return ref in EXTERNAL_TAGS or (DATA / ns / "tags" / registry / f"{path}.json").is_file()


def texture(ref):
    ns, path = split(ref)
    if ns == "minecraft":
        return
    png = RES / "assets" / ns / "textures" / f"{path}.png"
    if not png.is_file():
        err(f"Missing texture {ref}")
        return
    animated = png.with_name(png.name + ".mcmeta").is_file()
    with Image.open(png) as img:
        # Animated textures are a vertical strip of 16x16 frames with an .mcmeta beside them.
        width, height = img.size
        if animated and not (width == 16 and height % 16 == 0 and height > 16):
            err(f"Animated texture {ref} is {img.size}, expected a 16-wide strip of 16x16 frames")
        elif not animated and img.size != (16, 16):
            err(f"Texture {ref} is {img.size}, expected 16x16")


def model(ref):
    ns, path = split(ref)
    if ns == "minecraft":
        return
    data = load(RES / "assets" / ns / "models" / f"{path}.json")
    if data is None:
        return
    for tex in data.get("textures", {}).values():
        texture(tex)


def check_assets(registered):
    lang = load(ASSETS / "lang" / "en_us.json") or {}
    for block in all_blocks() + machine_blocks() + ag.all_blocks() + petro.petro_blocks() + list(deposits.DEPOSITS) + list(tank_display.BLOCKS):
        state = load(ASSETS / "blockstates" / f"{block}.json")
        if state:
            for variant in state.get("variants", {}).values():
                model(variant["model"])
            for part in state.get("multipart", []):
                model(part["apply"]["model"])
        if f"block.{MOD}.{block}" not in lang:
            err(f"Missing name for block {block}")
        if not (DATA / MOD / "loot_table" / "blocks" / f"{block}.json").is_file():
            err(f"Missing loot table for {block}")
    # Crops, stems, the cranberry bush and the chestnut sapling have no item of their own: what plants them stands in.
    for item in registered:
        if item in CROPS or item in ag.itemless_blocks():
            continue
        definition = load(ASSETS / "items" / f"{item}.json")
        if definition:
            for ref in item_models(definition["model"]):
                model(ref)
        if (item not in all_blocks() + machine_blocks() + ag.all_blocks() + petro.petro_blocks() + list(deposits.DEPOSITS) + list(tank_display.BLOCKS)
                and f"item.{MOD}.{item}" not in lang):
            err(f"Missing name for item {item}")


def item_models(definition):
    """Every model an item definition can show: a plain model, or each case and the fallback of a select."""
    if definition.get("type") == "minecraft:select":
        return [ref for case in definition["cases"] for ref in item_models(case["model"])] + item_models(definition["fallback"])
    return [definition["model"]]
def check_petro():
    """Petroleum fluids: Java registers exactly tools/petro.py's fluids, each with its block, textures and names."""
    java = (JAVA_ROOT / "chemistry" / "PetroFluids.java").read_text(encoding="utf-8")
    declared = re.findall(r'= fluid\("([a-z_]+)", (\d+), (\d+), (\d+)', java)
    expected = [(f, str(i["tick_delay"]), str(i["slope"]), str(i["drop_off"])) for f, i in petro.FLUIDS.items()]
    if declared != expected:
        err(f"PetroFluids.java fluids {declared} != tools/petro.py {expected}")
    items_java = re.findall(r'JugcraftRegistry\.item\("([a-z_]+)"[,)]',
                            (JAVA_ROOT / "chemistry" / "PetroItems.java").read_text(encoding="utf-8"))
    if items_java != list(petro.ITEMS):
        err(f"PetroItems.java items {items_java} != tools/petro.py {list(petro.ITEMS)}")
    blocks_java = re.findall(r'= register\("([a-z_]+)"', (JAVA_ROOT / "chemistry" / "PetroBlocks.java").read_text(encoding="utf-8"))
    if blocks_java != list(petro.BLOCKS):
        err(f"PetroBlocks.java blocks {blocks_java} != tools/petro.py {list(petro.BLOCKS)}")
    gases = re.findall(r'= gas\("([a-z_]+)"', java)
    if gases != list(petro.GASES):
        err(f"PetroFluids.java gases {gases} != tools/petro.py {list(petro.GASES)}")
    fuels_java = (JAVA_ROOT / "chemistry" / "FluidFuels.java").read_text(encoding="utf-8")
    for machine, fuels in petro.FLUID_FUELS.items():
        if f"case {machine.upper()} ->" not in fuels_java:
            err(f"FluidFuels.java has no case for {machine}")
        for fuel, value in fuels.items():
            if fuel not in petro.FLUIDS and fuel not in petro.GASES:
                err(f"{machine}: unknown fuel {fuel}")
            accessor = "fluid()" if fuel in petro.GASES else "source()"
            # A machine may have its own constant for a fuel, such as ADVANCED_DIESEL.
            if not re.search(rf"int (\w+_)?{fuel.upper()} = {value};", fuels_java) or f"PetroFluids.{fuel.upper()}.{accessor}" not in fuels_java:
                err(f"{machine}: {fuel} at {value} JE/mB in tools/petro.py does not match FluidFuels.java")
    lang = load(ASSETS / "lang" / "en_us.json") or {}
    for gas in petro.GASES:
        if f"block.{MOD}.{gas}" not in lang:
            err(f"Missing name for gas {gas}")
    for fluid in petro.FLUIDS:
        if f"block.{MOD}.{fluid}" not in lang:
            err(f"Missing name for fluid {fluid}")
        for form in ("still", "flow"):
            texture(rid_of(f"block/{fluid}_{form}"))
            if not (ASSETS / "textures" / "block" / f"{fluid}_{form}.png.mcmeta").is_file():
                err(f"Fluid texture {fluid}_{form} is not animated")
        state = load(ASSETS / "blockstates" / f"{fluid}.json")
        if state:
            model(state["variants"][""]["model"])


def rid_of(path):
    return f"{MOD}:{path}"


# Keys of the pre-26.x loot format: Minecraft 26.x ignores them without a warning, so a table that used them would lose
# its conditions and functions (counts, Fortune, which half of a block drops) silently.
OLD_LOOT_KEYS = {"conditions", "functions", "function"}


def old_loot_keys(node):
    if isinstance(node, dict):
        return (OLD_LOOT_KEYS & set(node)) | {key for value in node.values() for key in old_loot_keys(value)}
    if isinstance(node, list):
        return {key for value in node for key in old_loot_keys(value)}
    return set()


def check_loot(registered):
    for path in sorted((DATA / MOD / "loot_table").rglob("*.json")):
        text = path.read_text(encoding="utf-8")
        stale = old_loot_keys(load(path))
        if stale:
            err(f"{path.name}: uses the pre-26.x loot keys {sorted(stale)}; use \"condition\" and \"modifier\"")
        for name in re.findall(r'"name": "jugcraft:([a-z_]+)"', text):
            if name not in registered:
                err(f"{path.name} drops unknown item {name}")


# Metal content in nugget units. Tags stand for the same forms from any mod.
UNITS = {"ingots": 9, "nuggets": 1, "raw_materials": 9, "ores": 9, "storage_blocks": 81,
         **{f"{form}s": units for form, units in PART_UNITS.items()}}


NON_METAL = {"sawdust"} | set(MINERALS) | set(ITEMS) | set(machine_blocks()) | set(machine_items()) | set(CIRCUITS) | {b for m in MINERALS for b in (f"{m}_ore", f"deepslate_{m}_ore", f"{m}_block")} | {"oil_sand"} | set(petro.petro_items()) | set(ag.all_blocks()) | set(ag.all_items()) | set(petro.petro_blocks()) | set(tank_display.BLOCKS)


def item_units(ref):
    """Returns {metal: units} for an item or tag reference."""
    ns, path = split(ref.lstrip("#"))
    if ref.startswith("#") and (ns == "minecraft" or ref[1:] in (ag.WOOD_TAG, ag.HEIRLOOM_TAG)):
        return {}  # vanilla tags used here (logs, planks), chestnut logs and heirloom pumpkins hold no metal
    if ref.startswith("#"):
        form, _, metal = path.partition("/")
        if metal in MINERALS or path in {info["tag"] for info in ITEMS.values()}:
            return {}
        if form not in UNITS or not metal:
            err(f"Recipe uses unsupported tag {ref}")
            return {}
        if metal.startswith("raw_"):
            return {metal[4:]: 81}
        return {metal: UNITS[form]}
    if ns != MOD:
        vanilla = {}
        for metal in ("copper", "iron", "gold"):
            vanilla.update({f"{metal}_ingot": (metal, 9), f"raw_{metal}": (metal, 9), f"{metal}_ore": (metal, 9),
                            f"deepslate_{metal}_ore": (metal, 9), f"{metal}_nugget": (metal, 1)})
        if ns == "minecraft" and path in vanilla:
            metal, units = vanilla[path]
            return {metal: units}
        return {}
    for metal in METALS:
        table = {f"{metal}_ingot": 9, f"{metal}_nugget": 1, f"{metal}_block": 81,
                 f"raw_{metal}": 9, f"raw_{metal}_block": 81, f"{metal}_ore": 9, f"deepslate_{metal}_ore": 9}
        if path in table:
            return {metal: table[path]}
    if path.startswith("washed_") and path.endswith("_ore"):
        return {path[len("washed_"):-len("_ore")]: 9}
    if path == "bronze_blend":
        return {"bronze": 9}
    for form, metals in COMPONENTS.items():
        for metal in metals:
            if path == f"{metal}_{form}":
                return {metal: PART_UNITS[form]}
    if path == "bauxite":
        # One bauxite holds two ingots of aluminum (it is about half alumina): the Bayer route (chemical reactor and
        # electrolytic cell) recovers all of it, the arc furnace stand-in half, the blast-furnace stand-in a nugget.
        return {"aluminum": 18}
    if path == "titanium_sponge":
        # Kroll-process sponge: one ingot of titanium each, melted in the arc furnace.
        return {"titanium": 9}
    if path == "alumina":
        # Bayer-process alumina: one ingot of aluminum each, smelted out in the electrolytic cell.
        return {"aluminum": 9}
    if path in NON_METAL:
        return {}
    err(f"No metal content known for {ref}")
    return {}


def check_recipes(registered):
    for path in sorted((DATA / MOD / "recipe").glob("*.json")):
        recipe = load(path)
        if recipe is None:
            continue
        name = path.stem
        conditions = recipe.get("fabric:load_conditions", [])
        features = [c.get("feature") for c in conditions if c.get("condition") == f"{MOD}:feature_enabled"]
        if not features:
            err(f"{name}: missing feature switch condition")
        elif split(recipe["result"]["id"])[1] in registered and feature_of(split(recipe["result"]["id"])[1]) not in features:
            err(f"{name}: gated by {features} but its result belongs to {feature_of(split(recipe['result']['id'])[1])}")

        kind = recipe["type"]
        if kind == "minecraft:crafting_shaped":
            symbols = "".join(recipe["pattern"])
            inputs = [recipe["key"][ch] for ch in symbols if ch != " "]
        elif kind == "minecraft:crafting_shapeless":
            inputs = recipe["ingredients"]
        else:
            inputs = [recipe["ingredient"]]

        for ref in inputs:
            if ref.startswith("#"):
                if not tag_exists("item", ref[1:]):
                    err(f"{name}: unknown tag {ref}")
            elif split(ref)[0] == MOD and split(ref)[1] not in registered:
                err(f"{name}: unknown item {ref}")

        result = recipe["result"]["id"]
        if split(result)[0] == MOD and split(result)[1] not in registered:
            err(f"{name}: unknown result {result}")
        count = recipe["result"].get("count", 1)

        total_in = sum(sum(item_units(ref).values()) for ref in inputs)
        out = {metal: units * count for metal, units in item_units(result).items()}
        if sum(out.values()) > total_in:
            err(f"{name}: creates metal ({total_in} units in, {sum(out.values())} out)")
        if any(item_units(ref).keys() == {"bronze"} for ref in inputs) and set(out) - {"bronze"}:
            err(f"{name}: turns bronze back into its ingredients")


def check_machine_recipe_files(registered):
    """Machine recipes are data-driven files under recipe/<type>/; each must resolve and match its type."""
    from generate_material_data import RECIPE_TYPES
    kinds = MACHINE_JAVA.read_text(encoding="utf-8")
    for machine, kind in RECIPE_TYPES.items():
        if f'"{kind}"' not in kinds:
            err(f"MachineKind.recipeType() has no \"{kind}\" (tools say {machine} uses it)")
    expected = sum(len(recipes) for recipes in machine_recipes().values())
    files = sorted(path for path in (DATA / MOD / "recipe").glob("*/*.json") if path.parent.name in RECIPE_TYPES.values())
    if len(files) != expected:
        err(f"{len(files)} machine recipe files, but tools/machines.py defines {expected}")
    for path in files:
        recipe = load(path)
        if recipe is None:
            continue
        label = f"recipe/{path.parent.name}/{path.name}"
        if recipe.get("type") != f"{MOD}:{path.parent.name}":
            err(f"{label}: type {recipe.get('type')} does not match its folder")
        if not recipe.get("fabric:load_conditions"):
            err(f"{label}: missing feature switch condition")
        refs = [part["ingredient"] for part in recipe.get("ingredients", [])] or [recipe.get("ingredient", "")]
        refs += [entry["result"]["id"] for entry in recipe.get("byproducts", [])]
        for ref in refs + [recipe["result"]["id"]]:
            if ref.startswith("#"):
                if not tag_exists("item", ref[1:]):
                    err(f"{label}: unknown tag {ref}")
            elif split(ref)[0] == MOD and split(ref)[1] not in registered:
                err(f"{label}: unknown item {ref}")


def check_fluid_recipes(registered):
    """Fluid recipes (tools/petro.py): every item and fluid resolves, slots and tanks exist, and none makes fluid from
    nothing. Recipes with no item input must not give out more fluid than they take in; recipes with item input must
    say how much fluid they release from it ("source")."""
    from generate_material_data import RECIPE_TYPES
    java = MACHINE_JAVA.read_text(encoding="utf-8")
    fluids = {f"{MOD}:{f}" for f in list(petro.FLUIDS) + list(petro.GASES)} | {"minecraft:water", "minecraft:lava"}
    for machine, spec in petro.FLUID_MACHINES.items():
        match = re.search(r"case " + machine.upper() + r" -> new FluidMachineSpec\(List\.of\(([^)]*)\),\s*List\.of\(([^)]*)\),\s*"
                          r"(\d+), (\d+)\)", java)
        if not match:
            err(f"MachineKind.fluidSpec() has no case for {machine}")
        else:
            def tanks(text):
                return [CONSTANTS.get(v.strip(), None) or int(v.strip().replace("_", "")) for v in text.split(",") if v.strip()]
            CONSTANTS = {name: int(value.replace("_", "")) for name, value in
                         re.findall(r"public static final int (\w+) = ([\d_]+);", java)}
            found = (tanks(match.group(1)), tanks(match.group(2)), int(match.group(3)), int(match.group(4)))
            if found != (spec["inputs"], spec["outputs"], spec["item_inputs"], spec["item_outputs"]):
                err(f"{machine}: fluid spec {found} in Java, {spec} in tools/petro.py")
        if spec["recipe_type"] is None:
            continue
        if f'"{spec["recipe_type"]}"' not in java:
            err(f"MachineKind.recipeType() has no \"{spec['recipe_type']}\" for {machine}")
        if spec["recipe_type"] in RECIPE_TYPES.values():
            err(f"Fluid recipe type {spec['recipe_type']} is also an item machine's")
    expected = sum(len(r) for r in petro.FLUID_RECIPES.values())
    types = {spec["recipe_type"] for spec in petro.FLUID_MACHINES.values() if spec["recipe_type"]}
    files = [p for p in (DATA / MOD / "recipe").glob("*/*.json") if p.parent.name in types]
    if len(files) != expected:
        err(f"{len(files)} fluid recipe files, but tools/petro.py defines {expected}")
    for machine, recipes in petro.FLUID_RECIPES.items():
        spec = petro.FLUID_MACHINES[machine]
        for recipe in recipes:
            label = f"{machine} recipe {recipe['name']}"
            if len(recipe.get("items", [])) > spec["item_inputs"] or len(recipe.get("results", [])) > spec["item_outputs"]:
                err(f"{label}: more items than the machine has slots")
            if len(recipe.get("fluids", [])) > len(spec["inputs"]) or len(recipe.get("fluid_results", [])) > len(spec["outputs"]):
                err(f"{label}: more fluids than the machine has tanks")
            for i, (fluid, mb) in enumerate(recipe.get("fluids", [])):
                if fluid not in fluids:
                    err(f"{label}: unknown fluid {fluid}")
                if mb > spec["inputs"][i]:
                    err(f"{label}: needs {mb} mB of {fluid} but its tank holds {spec['inputs'][i]}")
            for i, (fluid, mb) in enumerate(recipe.get("fluid_results", [])):
                if fluid not in fluids:
                    err(f"{label}: unknown fluid {fluid}")
                if mb > spec["outputs"][i]:
                    err(f"{label}: makes {mb} mB of {fluid} but its tank holds {spec['outputs'][i]}")
            # Metal is conserved like in the item machines: no recipe gives out more than its items hold.
            metal_in, metal_out = {}, {}
            for ref, count in recipe.get("items", []):
                for metal, units in item_units(ref).items():
                    metal_in[metal] = metal_in.get(metal, 0) + units * count
            for ref, count in recipe.get("results", []):
                for metal, units in item_units(ref).items():
                    metal_out[metal] = metal_out.get(metal, 0) + units * count
            for metal, units in metal_out.items():
                if units > metal_in.get(metal, 0):
                    err(f"{label}: gives {units} {metal} units from {metal_in.get(metal, 0)}")
            for ref, _ in recipe.get("items", []) + recipe.get("results", []):
                if ref.startswith("#"):
                    if not tag_exists("item", ref[1:]):
                        err(f"{label}: unknown tag {ref}")
                elif split(ref)[0] == MOD and split(ref)[1] not in registered:
                    err(f"{label}: unknown item {ref}")
            fluid_in = sum(mb for _, mb in recipe.get("fluids", [])) + recipe.get("source", 0)
            fluid_out = sum(mb for _, mb in recipe.get("fluid_results", []))
            if recipe.get("items") and "source" not in recipe and fluid_out > sum(mb for _, mb in recipe.get("fluids", [])):
                err(f"{label}: makes fluid from items without saying how much (\"source\")")
            if fluid_out > fluid_in:
                err(f"{label}: {fluid_out} mB out from {fluid_in} mB in")


def check_tags():
    for path in sorted(DATA.rglob("tags/*/**/*.json")):
        registry = path.relative_to(DATA).parts[2]
        for value in (load(path) or {}).get("values", []):
            if value.startswith("#"):
                if not tag_exists(registry, value[1:]):
                    err(f"{path.relative_to(ROOT)}: unknown tag {value}")
            elif registry == "fluid":
                if split(value)[1] not in petro.fluid_ids():
                    err(f"{path.relative_to(ROOT)}: unknown fluid {value}")
            elif registry == "villager_trade":
                namespace, trade = split(value)
                if namespace == MOD and not (DATA / MOD / "villager_trade" / f"{trade}.json").exists():
                    err(f"{path.relative_to(ROOT)}: unknown villager trade {value}")
            elif split(value)[0] == MOD and split(value)[1] not in (all_blocks() + all_items() + machine_blocks()
                                                                    + machine_items() + petro.petro_blocks()
                                                                    + petro.petro_items() + list(deposits.DEPOSITS) + list(tank_display.BLOCKS)
                                                                    + ag.all_blocks() + ag.all_items()):
                err(f"{path.relative_to(ROOT)}: unknown entry {value}")


def check_worldgen():
    if (DATA / MOD / "worldgen" / "configured_feature").exists():
        err("worldgen/configured_feature/ is the pre-26.x layout; Minecraft 26.3 reads worldgen/feature/")
    for path in sorted((DATA / MOD / "worldgen" / "feature").glob("*.json")):
        feature = load(path) or {}
        if "config" in feature:
            err(f"{path.name}: 26.x features have no \"config\" wrapper")
        for target in feature.get("targets", []):
            block = split(target["state"])[1]
            if block not in all_blocks():
                err(f"{path.name}: places unknown block {block}")
    for path in sorted((DATA / MOD / "worldgen" / "placed_feature").glob("*.json")):
        feature = split((load(path) or {})["feature"])[1]
        if not (DATA / MOD / "worldgen" / "feature" / f"{feature}.json").is_file():
            err(f"{path.name}: unknown configured feature {feature}")


def check_java():
    source = JAVA.read_text(encoding="utf-8")
    declared = {}
    for name, chain in re.findall(r'MetalFamily\.builder\("([a-z_]+)"\)([^;]*)\.build\(\)', source):
        declared[name] = {"mined": ".mined()" in chain, "extras": re.findall(r'extraItem\("([a-z_]+)"\)', chain)}
    expected = {name: {"mined": info["mined"], "extras": info.get("extras", [])} for name, info in METALS.items()}
    if declared != expected:
        err(f"JugcraftMaterials.java metals {declared} != tools/materials.py {expected}")

    minerals = re.findall(r'MineralFamily\.register\("([a-z_]+)"\)', source)
    if minerals != list(MINERALS):
        err(f"JugcraftMaterials.java minerals {minerals} != {list(MINERALS)}")
    rocks = re.findall(r'JugcraftRegistry\.block\("([a-z_]+)"', source)
    if rocks != list(ROCKS):
        err(f"JugcraftMaterials.java rocks {rocks} != {list(ROCKS)}")
    items = re.findall(r'JugcraftRegistry\.item\("([a-z_]+)"\)', source)
    if items != list(ITEMS):
        err(f"JugcraftMaterials.java items {items} != {list(ITEMS)}")

    components = (JAVA_ROOT / "materials" / "JugcraftComponents.java").read_text(encoding="utf-8")
    for name, expected_list in (("PLATES", COMPONENTS["plate"]), ("GEARS", COMPONENTS["gear"]),
                                ("WIRES", COMPONENTS["wire"]), ("CIRCUITS", list(CIRCUITS)),
                                ("DUSTS", COMPONENTS["dust"]), ("WASHED_ORES", WASHED_ORES)):
        found = re.findall(r'"([a-z_]+)"', re.search(name + r' = \{([^}]*)\}', components).group(1))
        if found != expected_list:
            err(f"JugcraftComponents.{name} {found} != tools/materials.py {expected_list}")

    features = re.findall(r'"([a-z_]+)"', CONFIG.read_text(encoding="utf-8").split("List.of(")[1].split(");")[0])
    if features != FEATURES:
        err(f"JugcraftConfig.FEATURES {features} != {FEATURES}")

    worldgen = WORLDGEN.read_text(encoding="utf-8")
    placed = sorted(p.stem[4:] for p in (DATA / MOD / "worldgen" / "placed_feature").glob("ore_*.json"))
    in_java = sorted(set(re.findall(r'\{"([a-z_]+)", "[a-z_]+"\}', worldgen)) | set(re.findall(r'add\("([a-z_]+)"', worldgen)))
    if placed != in_java:
        err(f"JugcraftWorldgen adds {in_java}, data defines {placed}")
    for name, feature in re.findall(r'\{"([a-z_]+)", "([a-z_]+)"\}', worldgen) + re.findall(r'add\("([a-z_]+)", "([a-z_]+)"', worldgen):
        owner = feature_of(name if name in ROCKS else f"{name}_ore")
        if feature != owner:
            err(f"JugcraftWorldgen gates {name} by {feature}, expected {owner}")


def check_machines(registered):
    """Audits machine recipes for metal and keeps Java machine stats in sync with tools/machines.py."""
    for machine, recipes in machine_recipes().items():
        for recipe in recipes:
            inputs = recipe.get("inputs") or [[recipe["input"], 1]]
            label = f"{machine} {' + '.join(ref for ref, _ in inputs)}"
            for ref in [ref for ref, _ in inputs] + [recipe["output"]]:
                if split(ref)[0] == MOD and split(ref)[1] not in registered:
                    err(f"{label}: unknown item {ref}")
            for feature in recipe["features"]:
                if feature not in FEATURES:
                    err(f"{label}: unknown feature {feature}")
            units_in = sum(sum(item_units(ref).values()) * count for ref, count in inputs)
            units_out = sum(item_units(recipe["output"]).values()) * recipe["count"]
            bonus = recipe.get("ore_bonus") or (ORE_PROCESSING_MULTIPLIER if recipe.get("ore") else 1)
            allowed = units_in * bonus
            is_ore = all(ref.endswith("_ore") and not split(ref)[1].startswith("washed_") for ref, _ in inputs)
            if bonus > 1 and not is_ore:
                err(f"{label}: only ores get the ore-processing bonus")
            if bonus > ORE_WASHING_MULTIPLIER:
                err(f"{label}: ore bonus {bonus} exceeds the washing route's {ORE_WASHING_MULTIPLIER}")
            if units_out > allowed:
                err(f"{label}: creates metal ({units_in} in, {units_out} out, {allowed} allowed)")
            # Byproducts: [item, count, chance, feature]. Expected metal stays a small share of the input,
            # or, for renewable recipes (sieve), under a nugget per operation.
            extra = 0.0
            for item, count, chance, feature in recipe.get("byproducts", []):
                if split(item)[0] == MOD and split(item)[1] not in registered:
                    err(f"{label}: unknown byproduct {item}")
                if not 0 < chance <= 1:
                    err(f"{label}: byproduct chance {chance} outside (0, 1]")
                if feature is not None and feature not in FEATURES:
                    err(f"{label}: byproduct gated by unknown feature {feature}")
                extra += sum(item_units(item).values()) * count * chance
            limit = RENEWABLE_UNITS if recipe.get("renewable") else units_in * BYPRODUCT_SHARE
            if extra > limit + 1e-9:
                err(f"{label}: byproducts add {extra:.2f} metal units on average, {limit:.2f} allowed")
            if recipe.get("renewable") and units_in:
                err(f"{label}: renewable recipes must not consume metal")

    kinds = MACHINE_JAVA.read_text(encoding="utf-8")
    for machine, stats in STATS.items():
        match = re.search(r'\("' + machine + r'", ([\d_]+), ([\d_]+), ([\d_]+), ([\d_]+),', kinds)
        if not match:
            err(f"MachineKind.java has no entry for {machine}")
            continue
        capacity, max_in, max_out, use = (int(v.replace("_", "")) for v in match.groups())
        if capacity != stats["capacity"]:
            err(f"{machine}: capacity {capacity} in Java, {stats['capacity']} in machines.py")
        expected_use = stats.get("use_per_tick", 0)
        if use != expected_use:
            err(f"{machine}: use {use} in Java, {expected_use} in machines.py")
        if "boost" in stats:
            if f'case {machine.upper()} -> "{stats["boost"]}";' not in kinds:
                err(f"{machine}: MachineKind.boostGas() is not {stats['boost']}")
            if stats["boost"] not in petro.GASES:
                err(f"{machine}: boost gas {stats['boost']} is not a gas in tools/petro.py")
            per_tick = re.search(r"case " + machine.upper() + r" -> (\w+);\s*(?:case|default)", kinds.split("public int boostPerTick()")[1])
            constants = dict(re.findall(r"public static final int (\w+) = ([\d_]+);", kinds))
            if not per_tick or int(constants.get(per_tick.group(1), "-1").replace("_", "")) != stats["boost_per_tick"]:
                err(f"{machine}: MachineKind.boostPerTick() does not give {stats['boost_per_tick']}")
    if set(MACHINES) != set(re.findall(r'\("([a-z_]+)", [\d_]+,', kinds)):
        err("MachineKind.java and tools/machines.py list different machines")


def check_large_machines():
    """Footprints must match MachineKind.footprint(); model elements must stay in Minecraft's -16..32 range."""
    from large_machines import FOOTPRINTS
    kinds = MACHINE_JAVA.read_text(encoding="utf-8")
    for machine, footprint in FOOTPRINTS.items():
        match = re.search(r"case " + machine.upper() + r" -> Footprint\.(tall|of|cuboid)\((.*?)\);", kinds, re.S)
        if not match:
            err(f"MachineKind.footprint() has no case for {machine}")
            continue
        if match.group(1) == "tall":
            java = [(0, y, 0) for y in range(int(match.group(2)))]
        elif match.group(1) == "cuboid":
            from large_machines import cuboid
            java = cuboid(*(int(v) for v in match.group(2).split(",")))
        else:
            java = [(0, 0, 0) if part.strip() == "Vec3i.ZERO"
                    else tuple(int(v) for v in re.fullmatch(r"\s*new Vec3i\((-?\d+), (-?\d+), (-?\d+)\)\s*", part).groups())
                    for part in re.split(r",\s*(?=new|Vec3i)", match.group(2))]
        if java != [tuple(offset) for offset in footprint]:
            err(f"{machine}: footprint {java} in Java, {footprint} in large_machines.py")
    from large_machines import POWER_PORTS
    java_ports = {kind.lower(): (int(part), face.lower()) for kind, part, face in
                  re.findall(r"(\w+) \? new PowerPort\((\d+), Direction\.(\w+)\)", kinds)}
    if java_ports != POWER_PORTS:
        err(f"Power ports differ: {java_ports} in MachineKind.java, {POWER_PORTS} in large_machines.py")
    for path in sorted((ASSETS / "models").rglob("*.json")) + sorted(STYLE_PACK.rglob("models/**/*.json")):
        for element in (load(path) or {}).get("elements", []):
            if min(element["from"] + element["to"]) < -16 or max(element["from"] + element["to"]) > 32:
                err(f"{path.name}: element outside -16..32")


def check_handbook(registered):
    """Every Jugcraft item the Engineer's Handbook shows must exist."""
    book = load(ASSETS / "handbook" / "en_us.json")
    if book is None:
        err("assets/jugcraft/handbook/en_us.json is missing")
        return
    refs = []
    for chapter in book["chapters"]:
        refs.append(chapter["icon"])
        for page in chapter["pages"]:
            refs.append(page["icon"])
            craft = page.get("craft")
            if craft:
                refs += [ref for ref in craft["grid"] if ref] + [craft["result"]]
            refs += [step["item"] for step in page.get("steps", [])]
            for row in page.get("recipes", []):
                refs += [ref for ref, _ in row["in"]] + [row["out"][0]] + [ref for ref, _ in row.get("extra", [])]
    for ref in refs:
        if split(ref)[0] == MOD and split(ref)[1] not in registered:
            err(f"handbook: unknown item {ref}")


VANILLA_ADVANCEMENT_ROOTS = {"minecraft:story/root", "minecraft:nether/root", "minecraft:end/root", "minecraft:adventure/root",
                             "minecraft:husbandry/root"}


def check_advancements(registered):
    """Every advancement names real Jugcraft items, a translated title and a parent that exists."""
    lang = load(ASSETS / "lang" / "en_us.json") or {}
    folder = DATA / MOD / "advancement"
    names = {path.stem for path in folder.glob("*.json")}
    for path in sorted(folder.glob("*.json")):
        data = load(path)
        text = path.read_text(encoding="utf-8")
        for name in re.findall(r'"jugcraft:([a-z_]+)"', text):
            if name not in registered and name not in names and not name.startswith("block/"):
                err(f"advancement {path.stem}: unknown item jugcraft:{name}")
        if data["display"]["title"]["translate"] not in lang:
            err(f"advancement {path.stem}: untranslated title")
        parent = data.get("parent")
        # A vanilla tab's root may be the parent (the Halloween advancements live in Husbandry).
        if parent and parent not in VANILLA_ADVANCEMENT_ROOTS and split(parent)[1] not in names:
            err(f"advancement {path.stem}: missing parent {parent}")


def check_style_pack():
    """Both machine styles must cover every machine completely, and everything they reference must exist."""
    from machines import PARTS, FLUID_BLOCKS
    pack_assets = STYLE_PACK / "assets" / MOD
    if not (STYLE_PACK / "pack.mcmeta").is_file():
        err("resourcepacks/alternate_machines has no pack.mcmeta")

    def resolve(ref, root):
        ns, path = split(ref)
        if ns == "minecraft":
            return
        file = root / "models" / f"{path}.json"
        data = load(file if file.is_file() else ASSETS / "models" / f"{path}.json")
        if data is None:
            return
        for value in data.get("textures", {}).values():
            if not value.startswith("#"):
                texture(value)
        if data.get("parent", "").startswith(MOD + ":"):
            resolve(data["parent"], root)

    for block in list(MACHINES) + list(PARTS) + list(FLUID_BLOCKS):
        keys = {}
        for root, label in ((ASSETS, "default style"), (pack_assets, "alternate style pack")):
            path = root / "blockstates" / f"{block}.json"
            if not path.is_file():
                err(f"The {label} has no blockstate for {block}")
                continue
            variants = (load(path) or {}).get("variants", {})
            keys[label] = set(variants)
            for variant in variants.values():
                resolve(variant["model"], root)
            item = root / "items" / f"{block}.json"
            if not item.is_file():
                err(f"The {label} has no item model for {block}")
            else:
                resolve((load(item) or {})["model"]["model"], root)
        if len(keys) == 2 and len(set(map(frozenset, keys.values()))) != 1:
            err(f"{block}: the two styles cover different block states")


def check_agriculture():
    """Agriculture: Java matches tools/agriculture.py, every crop state has a model, no recipe loop."""
    java = {path.stem: path.read_text(encoding="utf-8") for path in AGRICULTURE_JAVA.glob("*.java")}

    tall = {name.lower(): {"block": block, "seed": seed, "heights": [int(h) for h in heights.split(",")], "produce": produce,
                           "pick": [int(low), int(high)], "reset": int(reset), "growth": float(growth), "trellis": trellis == "true"}
            for name, block, seed, heights, produce, low, high, reset, growth, trellis in re.findall(
                r'(\w+)\("([a-z_]+)", "([a-z_]+)", new int\[\] \{([\d, ]+)\}, "([a-z_]+)", (\d+), (\d+), (\d+), ([\d.]+)F, (true|false)\)',
                java.get("TallCrop", ""))}
    expected = {name: {"block": info["block"], "seed": info["seed"], "heights": info["heights"], "produce": info["pick"]["item"],
                       "pick": [info["pick"]["min"], info["pick"]["max"]], "reset": info["pick_reset"],
                       "growth": info["growth_time"], "trellis": bool(info.get("trellis"))} for name, info in ag.TALL_CROPS.items()}
    if tall != expected:
        err(f"TallCrop.java {tall} != tools/agriculture.py {expected}")
    for name, info in ag.TALL_CROPS.items():
        if len(info["heights"]) != 8 or len(info["textures"]) != 8:
            err(f"{name}: needs 8 ages of heights and textures")
        elif any(len(textures) != info["heights"][age] for age, textures in enumerate(info["textures"])):
            err(f"{name}: each age needs one texture per block of height")
        elif info["heights"][info["pick_reset"]] != info["heights"][7] or max(info["heights"]) > ag.TALL_SECTIONS:
            err(f"{name}: picking must keep the plant's height, and it may be at most {ag.TALL_SECTIONS} tall")

    main = java.get("JugcraftAgriculture", "")
    crops = {block: {"seed": seed, "legume": legume == "true"} for block, seed, legume in
             re.findall(r'\bcrop\("([a-z_]+)", "([a-z_]+)", (true|false)\)', main)}
    expected = {info["block"]: {"seed": info["seed"], "legume": info["legume"]} for info in ag.CROPS.values()}
    if crops != expected:
        err(f"JugcraftAgriculture.java crops {crops} != tools/agriculture.py {expected}")

    items = {}
    for name, n, sat, compost in re.findall(r'\bfood\("([a-z_]+)", (\d+), ([\d.]+)F, COMPOST_(\w+)\)', main):
        items[name] = ("food", int(n), float(sat), compost.lower(), None)
    for name, crop, compost in re.findall(r'\bseeds\("([a-z_]+)", "([a-z_]+)", COMPOST_(\w+)\)', main):
        items[name] = ("seeds", None, None, compost.lower(), crop)
    for name, crop, compost in re.findall(r'\btrellisSeeds\("([a-z_]+)", TallCrop\.(\w+), COMPOST_(\w+)\)', main):
        items[name] = ("seeds", None, None, compost.lower(), ag.TALL_CROPS.get(crop.lower(), {}).get("block"))
    for name, n, sat in re.findall(r'\bmeal\("([a-z_]+)", (\d+), ([\d.]+)F\)', main):
        items[name] = ("food", int(n), float(sat), None, None)
    for name, crop, n, sat, compost in re.findall(r'\bedibleSeeds\("([a-z_]+)", "([a-z_]+)", (\d+), ([\d.]+)F, COMPOST_(\w+)\)', main):
        items[name] = ("seeds", int(n), float(sat), compost.lower(), crop)
    for name, compost in re.findall(r'\bplain\("([a-z_]+)", COMPOST_(\w+)\)', main):
        items[name] = ("plain", None, None, compost.lower(), None)
    for name, n, sat in re.findall(r'\bstew\("([a-z_]+)", (\d+), ([\d.]+)F\)', main):
        items[name] = ("stew", int(n), float(sat), None, None)
    for name, n, sat in re.findall(r'\btreat\("([a-z_]+)", (\d+), ([\d.]+)F\)', main):
        items[name] = ("treat", int(n), float(sat), None, None)
    for name, n, sat, effect, seconds in re.findall(r'\bsweet\("([a-z_]+)", (\d+), ([\d.]+)F, MobEffects\.(\w+), (\d+)\)', main):
        items[name] = ("sweet", int(n), float(sat), None, None)
        if ag.ITEMS.get(name, {}).get("sweet") != [effect, int(seconds)]:
            err(f"JugcraftAgriculture.java sweet {name} gives {effect} for {seconds} s, not as tools/agriculture.py says")
    expected = {}
    for name, info in ag.ITEMS.items():
        food = info.get("food") or [None, None]
        kind = ("stew" if info.get("stew") else "treat" if info.get("treat") else "sweet" if info.get("sweet") else "seeds" if "plants" in info
                else "food" if "food" in info else "plain")
        expected[name] = (kind, food[0], food[1], info.get("compost"), info.get("plants"))
    if items != expected:
        err(f"JugcraftAgriculture.java items {items} != tools/agriculture.py {expected}")

    sickles = {name: (int(radius), int(durability)) for name, radius, durability in
               re.findall(r'\bsickle\("([a-z_]+)", (\d+), (\d+)\)', main)}
    if sickles != {name: (info["radius"], info["durability"]) for name, info in ag.SICKLES.items()}:
        err(f"JugcraftAgriculture.java sickles {sickles} differ from tools/agriculture.py")
    if re.findall(r'\bwild\("([a-z_]+)"\)', main) != list(ag.WILD_CROPS):
        err("JugcraftAgriculture.java wild plants differ from tools/agriculture.py")
    patches = {name: re.findall(r"ConventionalBiomeTags\.(\w+)", biomes)
               for name, biomes in re.findall(r'wildPatch\("([a-z_]+)", ([^;]*)\);', main)}
    expected = {name: info["biomes"] for name, info in ag.WILD_CROPS.items()}
    expected.update({name: info["biomes"] for name, info in ag.FOUND_WILD.items()})
    expected["chestnut_tree"] = ag.CHESTNUT_TREES["biomes"]
    expected["mums"] = ag.MUM_PATCH["biomes"]
    if patches != expected:
        err(f"JugcraftAgriculture.java wild patch biomes {patches} differ from tools/agriculture.py")
    seeds = re.search(r'GRASS_SEEDS = List\.of\(([^)]*)\)', main)
    chance = re.search(r'GRASS_SEED_CHANCE = ([\d.]+)F', main)
    if not seeds or re.findall(r'"([a-z_]+)"', seeds.group(1)) != ag.GRASS_SEEDS or not chance or float(chance.group(1)) != ag.GRASS_SEED_CHANCE:
        err("JugcraftAgriculture.java grass seeds differ from tools/agriculture.py")
    for name, info in ag.ITEMS.items():
        plants = info.get("plants")
        if plants and bool(info.get("trellis_seed")) != (plants in ag.trellis_crops()):
            err(f"{name}: a seed is a trellis seed exactly when it plants a climbing crop")
        if plants and bool(info.get("bog_seed")) != (plants == ag.CRANBERRY["block"]):
            err(f"{name}: a seed is a bog seed exactly when it plants the cranberry bush")
    check_festival(java, main)
    check_carving(java, main)
    check_halloween(java, main)
    check_regatta(java, main)
    check_festivities(java, main)
    check_night(java, main)
    check_decor(java)
    check_decor2(java)
    check_decor3(java)
    check_decor4(java)
    check_decor5(java)
    check_decor6(java)
    check_decor7(java)
    check_decor8(java)
    check_decor9(java)
    check_decor10(java)
    check_decor11(java)
    check_decor12(java)
    pot = java.get("CookingPotBlockEntity", "")
    inputs = re.search(r'int INPUTS = (\d+);', pot)
    outputs = re.search(r'int OUTPUTS = (\d+);', pot)
    cooling = re.search(r'int COOLING = (\d+);', pot)
    if (not inputs or int(inputs.group(1)) != ag.POT_INPUTS or not outputs or int(outputs.group(1)) != ag.POT_OUTPUTS
            or not cooling or int(cooling.group(1)) != ag.POT_COOLING
            or f'Jugcraft.id("{ag.HEAT_TAG.split(":")[1]}")' not in pot):
        err("CookingPotBlockEntity.java differs from POT_INPUTS, POT_OUTPUTS, POT_COOLING or HEAT_TAG in tools/agriculture.py")
    growth = java.get("CropGrowth", "")
    bonus = re.search(r'LEGUME_BONUS = ([\d.]+)F', growth)
    if not bonus or float(bonus.group(1)) != ag.LEGUME_BONUS or f'Jugcraft.id("{ag.LEGUME_TAG.split(":")[1]}")' not in growth:
        err("CropGrowth.java legume rules differ from tools/agriculture.py")

    # Every crop state has a model, and every seed plants a real crop.
    for info in ag.TALL_CROPS.values():
        variants = set((load(ASSETS / "blockstates" / f"{info['block']}.json") or {}).get("variants", {}))
        if variants != {f"age={a},section={s}" for a in range(8) for s in range(ag.TALL_SECTIONS)}:
            err(f"{info['block']}: blockstate does not cover every age and section")
    for info in ag.CROPS.values():
        variants = set((load(ASSETS / "blockstates" / f"{info['block']}.json") or {}).get("variants", {}))
        if variants != {f"age={a}" for a in range(8)}:
            err(f"{info['block']}: blockstate does not cover every age")
    for name, info in ag.ITEMS.items():
        if "plants" in info and info["plants"] not in ag.planted_blocks():
            err(f"{name} plants unknown crop {info['plants']}")
    for texture in ag.textures():
        if not (ASSETS / "textures" / "block" / f"{texture}.png").is_file():
            err(f"Missing crop texture {texture}")

    # Cooking Pot recipes: one file each, known items, room in the pot, and no two with the same ingredients.
    registered = set(ag.all_items()) | set(all_items()) | set(all_blocks())
    folder = DATA / MOD / "recipe" / "pot_cooking"
    if sorted(path.stem for path in folder.glob("*.json")) != sorted(ag.POT_RECIPES):
        err("recipe/pot_cooking/ files differ from POT_RECIPES in tools/agriculture.py")
    seen = {}
    for result, info in ag.POT_RECIPES.items():
        refs = list(info["inputs"])
        for ref in refs + [f"{MOD}:{result}"]:
            if split(ref)[0] == MOD and split(ref)[1] not in registered:
                err(f"pot_cooking/{result}: unknown item {ref}")
        if len(refs) > ag.POT_INPUTS:
            err(f"pot_cooking/{result}: {len(refs)} ingredients do not fit in {ag.POT_INPUTS} slots")
        key = frozenset(refs)
        if key in seen:
            err(f"pot_cooking/{result} uses the same ingredients as {seen[key]}; the pot could not tell them apart")
        seen[key] = result
        recipe = load(folder / f"{result}.json") or {}
        if recipe.get("type") != f"{MOD}:pot_cooking" or not recipe.get("fabric:load_conditions"):
            err(f"pot_cooking/{result}: wrong type or missing feature switch condition")

    # No recipe loop among agriculture items: every conversion leads away from where it started.
    edges = {}

    def edge(ref, result):
        edges.setdefault(split(ref.lstrip("#"))[1], set()).add(split(result)[1] if ":" in result else result)
    for recipe in ag.SHAPELESS:
        for ref in recipe["inputs"]:
            edge(ref, recipe["result"])
    for recipe in ag.SHAPED:
        for ref in recipe["key"].values():
            edge(ref, recipe["result"])
    for result, info in ag.POT_RECIPES.items():
        for ref in info["inputs"]:
            edge(ref, result)
    for result, info in ag.COOKING.items():
        edges.setdefault(info["input"], set()).add(result)

    def reaches(start, target, seen):
        for nxt in edges.get(start, ()):
            if nxt == target or (nxt not in seen and reaches(nxt, target, seen | {nxt})):
                return True
        return False
    for start in edges:
        if reaches(start, start, {start}):
            err(f"Agriculture recipes form a loop through {start}")


def check_festival(java, main):
    """Festival crops: Java matches tools/agriculture.py, and every stage and fruit state has a model."""
    gourds = {name: (seed, float(growth)) for name, seed, growth in
              re.findall(r'\bgourd\("([a-z_]+)", "([a-z_]+)", ([\d.]+)F, MapColor\.\w+\)', main)}
    if gourds != {name: (info["seed"], info["growth_time"]) for name, info in ag.GOURDS.items()}:
        err(f"JugcraftAgriculture.java gourds {gourds} differ from GOURDS in tools/agriculture.py")
    for gourd, info in ag.GOURDS.items():
        if ag.ITEMS.get(info["seed"], {}).get("plants") != ag.stem(gourd):
            err(f"{info['seed']} must plant {ag.stem(gourd)}")
    bush = java.get("CranberryBushBlock", "")
    numbers = {name: int(value) for name, value in re.findall(r'int (GROWTH_CHANCE|PICK_MIN|PICK_MAX|PICK_RESET) = (\d+);', bush)}
    cranberry = ag.CRANBERRY
    if (numbers != {"GROWTH_CHANCE": cranberry["growth_chance"], "PICK_MIN": cranberry["pick"]["min"],
                    "PICK_MAX": cranberry["pick"]["max"], "PICK_RESET": cranberry["pick_reset"]}
            or f'Jugcraft.id("{ag.BOG_SOIL_TAG.split(":")[1]}")' not in bush):
        err("CranberryBushBlock.java differs from CRANBERRY or BOG_SOIL_TAG in tools/agriculture.py")
    leaves = java.get("ChestnutLeavesBlock", "")
    numbers = {name: int(value) for name, value in re.findall(r'int (FRUIT_CHANCE|PICK_MIN|PICK_MAX) = (\d+);', leaves)}
    chestnut = ag.CHESTNUT
    if numbers != {"FRUIT_CHANCE": chestnut["fruit_chance"], "PICK_MIN": chestnut["pick"]["min"], "PICK_MAX": chestnut["pick"]["max"]}:
        err("ChestnutLeavesBlock.java differs from CHESTNUT in tools/agriculture.py")
    for block in list(ag.WOOD) + list(ag.TREE_BLOCKS) + list(ag.DECOR) + [ag.CRANBERRY["block"]]:
        if f'registerBlock("{block}"' not in main:
            err(f"JugcraftAgriculture.java does not register {block}")
    for block, info in ag.DECOR.items():
        if f"lightLevel(state -> {info['light']})" not in main:
            err(f"{block}: light level differs from DECOR in tools/agriculture.py")
    expected_states = {ag.stem(g): {f"age={a}" for a in range(8)} for g in ag.GOURDS}
    expected_states[ag.CRANBERRY["block"]] = {f"age={a}" for a in range(len(ag.CRANBERRY["stages"]))}
    expected_states[ag.CHESTNUT["leaves"]] = {f"fruit={f}" for f in range(3)}
    for block, variants in expected_states.items():
        if set((load(ASSETS / "blockstates" / f"{block}.json") or {}).get("variants", {})) != variants:
            err(f"{block}: blockstate does not cover every stage")


def check_carving(java, main):
    """Pumpkin carving: Java matches CARVING in tools/agriculture.py."""
    info = ag.CARVING
    carving = java.get("PumpkinCarving", "")
    numbers = {name: re.search(rf"int {name} = (\d+);", carving) for name in ("SIZE", "FACES")}
    if any(match is None for match in numbers.values()) or int(numbers["SIZE"].group(1)) != info["size"] \
            or int(numbers["FACES"].group(1)) != info["faces"]:
        err("PumpkinCarving.java SIZE or FACES differ from CARVING in tools/agriculture.py")
    glow = info["glow"]
    expected = f"Math.min({glow['max']}, {glow['base']} + count(CUT) / {glow['per_holes']} + count(SHAVED) / {glow['per_shaved']})"
    if expected not in carving:
        err(f"PumpkinCarving.glow() differs from CARVING['glow'] (expected {expected})")
    session = re.search(r"SESSION_TICKS = (\d+);", java.get("PumpkinCarvings", ""))
    if not session or int(session.group(1)) != info["session_ticks"]:
        err("PumpkinCarvings.SESSION_TICKS differs from CARVING['session_ticks']")
    durability = re.search(r"CARVING_KNIFE_DURABILITY = (\d+);", main)
    if not durability or int(durability.group(1)) != info["durability"]:
        err("JugcraftAgriculture.CARVING_KNIFE_DURABILITY differs from CARVING['durability']")
    for registered in (info["block"], info["knife"]):
        if f'"{registered}"' not in main:
            err(f"JugcraftAgriculture.java does not register {registered}")
    templates = re.findall(r'template\("([a-z_]+)",', java.get("CarvingTemplates", ""))
    if templates != info["templates"]:
        err(f"CarvingTemplates.java {templates} differ from CARVING['templates'] {info['templates']}")
    rows = re.findall(r'"([.s#]+)"[,)]', java.get("CarvingTemplates", ""))
    if len(rows) != info["size"] * len(info["templates"]) or any(len(row) != info["size"] for row in rows):
        err(f"CarvingTemplates.java: every starter face is {info['size']} rows of {info['size']} pixels")
    lang = load(ASSETS / "lang" / "en_us.json") or {}
    for template in info["templates"]:
        if f"carving.{MOD}.template.{template}" not in lang:
            err(f"Missing name for carving template {template}")
    results = java.get("PumpkinCarvings", "").partition("enum Result {")[2].partition("}")[0]
    if not results:
        err("PumpkinCarvings.java has no Result enum")
    for result in re.findall(r"\b([A-Z_]+)\b", results):
        if result not in ("CARVED", "UNCHANGED") and f"message.{MOD}.carving.{result.lower()}" not in lang:
            err(f"Missing carving message for {result}")


CRAFTING_CATEGORIES = {"building", "redstone", "equipment", "misc"}
COOKING_CATEGORIES = {"food", "blocks", "misc"}


def check_recipe_categories():
    """Minecraft 26.3 refuses a whole datapack over one recipe with a category its type doesn't have."""
    for path in sorted((DATA / MOD / "recipe").rglob("*.json")):
        recipe = load(path) or {}
        kind, category = recipe.get("type", ""), recipe.get("category")
        if category is None:
            continue
        allowed = CRAFTING_CATEGORIES if kind.startswith("minecraft:crafting") else COOKING_CATEGORIES \
            if kind in ("minecraft:smelting", "minecraft:smoking", "minecraft:blasting", "minecraft:campfire_cooking") else None
        if allowed is not None and category not in allowed:
            err(f"{path.relative_to(ROOT)}: category {category} is not one of {sorted(allowed)} for {kind}")


def check_halloween(java, main):
    """Halloween harvest: Java matches tools/agriculture.py, and every giant pumpkin and scarecrow state has a model."""
    def number(source, name, kind=r"\d+"):
        match = re.search(rf"\b{name} = ({kind})[FL]?;", java.get(source, ""))
        return match.group(1) if match else None

    giant = ag.GIANT_PUMPKIN
    entity = {name: number("GiantPumpkinBlockEntity", name) for name in
              ("GROW_TO_TWO", "GROW_TO_THREE", "BONE_MEAL_POINTS", "START_WEIGHT", "WEIGHT_PER_POINT", "MAX_WEIGHT", "WATERED_TICKS")}
    expected = {"GROW_TO_TWO": giant["grow_to_two"], "GROW_TO_THREE": giant["grow_to_three"], "BONE_MEAL_POINTS": giant["bone_meal_points"],
                "START_WEIGHT": giant["start_weight"], "WEIGHT_PER_POINT": giant["weight_per_point"], "MAX_WEIGHT": giant["max_weight"],
                "WATERED_TICKS": giant["watered_ticks"]}
    if {k: int(v) if v else None for k, v in entity.items()} != expected:
        err(f"GiantPumpkinBlockEntity.java {entity} differs from GIANT_PUMPKIN in tools/agriculture.py")
    if number("GiantPumpkinBlock", "MAX_SIZE") != str(giant["max_size"]) or giant["face_size"] != 16 * giant["max_size"]:
        err("GiantPumpkinBlock.MAX_SIZE differs from GIANT_PUMPKIN['max_size'], or face_size is not 16 per block")
    if number("GiantPumpkinVineBlock", "GROWTH_TIME", r"[\d.]+") is None or \
            float(number("GiantPumpkinVineBlock", "GROWTH_TIME", r"[\d.]+")) != giant["vine_growth_time"]:
        err("GiantPumpkinVineBlock.GROWTH_TIME differs from GIANT_PUMPKIN['vine_growth_time']")
    reach = number("PumpkinCarvings", "GIANT_REACH", r"[\d.]+")
    if reach is None or float(reach) != giant["reach"] or f'SCOOP_TABLE = "{ag.SCOOP["table"]}"' not in java.get("PumpkinCarvings", ""):
        err("PumpkinCarvings.GIANT_REACH or SCOOP_TABLE differs from tools/agriculture.py")
    glow = giant["glow"]
    expected_glow = f"Math.min({glow['max']}, {glow['base']} + cut / {glow['per_holes']} + shaved / {glow['per_shaved']})"
    if expected_glow not in java.get("GiantPumpkinBlockEntity", ""):
        err(f"GiantPumpkinBlockEntity.glow() differs from GIANT_PUMPKIN['glow'] (expected {expected_glow})")
    if sorted(giant["drops"]) != list(range(1, giant["max_size"] + 1)):
        err("GIANT_PUMPKIN['drops'] needs one entry per size")

    scale = ag.HARVEST_SCALE
    ribbons = re.search(r"RIBBONS = List\.of\(([^)]*)\)", java.get("HarvestScaleBlockEntity", ""))
    if (number("HarvestScaleBlockEntity", "BOARD") != str(scale["board"]) or number("HarvestScaleBlockEntity", "REMEMBERED") != str(scale["remembered"])
            or not ribbons or re.findall(r'"([a-z_]+)"', ribbons.group(1)) != list(scale["ribbons"]) or len(scale["ribbons"]) != scale["board"]):
        err("HarvestScaleBlockEntity.java differs from HARVEST_SCALE in tools/agriculture.py (one ribbon per place on the board)")
    if number("GourdCanteenItem", "CAPACITY") != str(ag.CANTEEN["capacity"]):
        err("GourdCanteenItem.CAPACITY differs from CANTEEN['capacity']")
    varieties = re.search(r'for \(String variety : List\.of\(([^)]*)\)\)', main)
    if (not varieties or re.findall(r'"([a-z_]+)"', varieties.group(1)) != list(ag.CARVED_VARIETIES)
            or any(carved != f"hand_carved_{variety}" for variety, carved in ag.CARVED_VARIETIES.items())
            or any(not ag.GOURDS.get(variety, {}).get("cube") for variety in ag.CARVED_VARIETIES)):
        err("JugcraftAgriculture.java carvable heirloom pumpkins differ from CARVED_VARIETIES (each a whole-block gourd)")
    mums = {name: (effect, float(seconds)) for name, effect, seconds in
            re.findall(r'\bmum\("([a-z_]+)", MobEffects\.(\w+), ([\d.]+)F\)', main)}
    if mums != {name: (info["effect"], info["seconds"]) for name, info in ag.MUMS.items()}:
        err(f"JugcraftAgriculture.java mums {mums} differ from MUMS in tools/agriculture.py")
    if f"DEFAULT_SHIRT = DyeColor.{ag.SCARECROW_SHIRT.upper()};" not in java.get("ScarecrowBlock", ""):
        err("ScarecrowBlock.DEFAULT_SHIRT differs from SCARECROW_SHIRT")
    for block in [giant["block"], scale["block"]] + list(ag.HALLOWEEN_DECOR):
        if f'"{block}"' not in main:
            err(f"JugcraftAgriculture.java does not register {block}")
    for crop in ag.STALKS["crops"]:
        if crop not in ag.TALL_CROPS:
            err(f"STALKS: {crop} is not a tall crop")
    for wild in ag.WILD_BONUS:
        if wild not in ag.WILD_CROPS:
            err(f"WILD_BONUS: {wild} is not a wild plant")

    # Minecraft 26.3 refuses a model element with no faces (the hidden middle of a giant pumpkin must have no element).
    for path in sorted((ASSETS / "models").rglob("*.json")):
        for element in (load(path) or {}).get("elements", []):
            if not element.get("faces"):
                err(f"{path.relative_to(ROOT)}: a model element needs at least one face")

    max_size = giant["max_size"]
    states = set((load(ASSETS / "blockstates" / f"{giant['block']}.json") or {}).get("variants", {}))
    if states != {f"part={p},size={size}" for size in range(1, max_size + 1) for p in range(max_size ** 3)}:
        err("giant_pumpkin: blockstate does not cover every size and part")
    states = set((load(ASSETS / "blockstates" / "scarecrow.json") or {}).get("variants", {}))
    expected = {f"facing={f},half=lower" for f in ("north", "east", "south", "west")}
    expected |= {f"facing={f},half=upper,shirt={c}" for f in ("north", "east", "south", "west") for c in ag.DYE_COLORS}
    if states != expected:
        err("scarecrow: blockstate does not cover every facing, half and shirt colour")


def check_regatta(java, main):
    """The pumpkin regatta and trick-or-treating: Java matches tools/agriculture.py, and every buoy number has a model."""
    def number(source, name, kind=r"[\d.]+"):
        match = re.search(rf"\b{name} = ({kind})[FLD]?;", java.get(source, ""))
        return float(match.group(1)) if match else None

    regatta, trick = ag.REGATTA, ag.TRICK_OR_TREAT
    expected = {("PumpkinBoat", "RACER_BASE_WEIGHT"): ag.RACER_BASE_WEIGHT, ("PumpkinBoat", "WATER_FRICTION"): ag.WATER_FRICTION,
                ("PumpkinBoat", "MARK_RADIUS"): regatta["mark_radius"], ("PumpkinBoat", "FINISH_RADIUS"): regatta["finish_radius"],
                ("PumpkinBoat", "COUNTDOWN_TICKS"): regatta["countdown_ticks"], ("PumpkinBoat", "MAX_RACE_TICKS"): regatta["max_race_ticks"],
                ("PumpkinBoat", "MAX_SPEED"): regatta["max_speed"], ("RegattaBuoyBlock", "MAX_NUMBER"): regatta["max_number"],
                ("RegattaFlagBlockEntity", "REMEMBERED"): regatta["remembered"], ("RegattaFlagBlockEntity", "COURSE_RANGE"): regatta["course_range"],
                ("RegattaFlagBlockEntity", "COURSE_HEIGHT"): regatta["course_height"], ("TrickOrTreat", "DUSK"): trick["dusk"],
                ("TrickOrTreat", "MIDNIGHT"): trick["midnight"], ("TrickOrTreat", "ANSWER_TICKS"): trick["answer_ticks"],
                ("TrickOrTreat", "KNOCK_COOLDOWN"): trick["knock_cooldown"], ("TrickOrTreat", "PORCH_RADIUS"): trick["porch_radius"],
                ("TrickOrTreat", "HOME_RADIUS"): trick["home_radius"], ("TrickOrTreat", "FULL_BAG"): trick["full_bag"],
                ("TrickOrTreat", "COSTUME_BONUS_CHANCE"): trick["costume_bonus"]}
    for (source, name), value in expected.items():
        if number(source, name) != value:
            err(f"{source}.{name} = {number(source, name)} differs from tools/agriculture.py ({value})")
    if regatta["board"] != ag.HARVEST_SCALE["board"] or "BOARD = HarvestScaleBlockEntity.BOARD;" not in java.get("RegattaFlagBlockEntity", ""):
        err("The regatta board must hand out the Harvest Scale's ribbons, one per place")
    for source, name, value in (("TrickOrTreat", "TREAT_TABLE", trick["table"]), ("PumpkinCarvings", "HOLLOW_TABLE", ag.HOLLOW["table"])):
        if f'{name} = "{value}"' not in java.get(source, ""):
            err(f"{source}.{name} differs from tools/agriculture.py ({value})")
    tags = {"COSTUMES": ag.COSTUME_TAG, "COSTUME_HATS": ag.COSTUME_HAT_TAG, "PORCH_LIGHTS": ag.PORCH_LIGHT_TAG}
    for field, tag in tags.items():
        if f'{field} = TagKey.create(Registries.{"BLOCK" if field == "PORCH_LIGHTS" else "ITEM"}, Jugcraft.id("{split(tag)[1]}"))' \
                not in java.get("TrickOrTreat", ""):
            err(f"TrickOrTreat.{field} is not the tag {tag}")

    # Each boat kind: Java's Kind constant and entity size match PUMPKIN_BOATS.
    kinds = {name: values for name, values in re.findall(r'^\t\t(BARGE|RACER)\("[a-z_]+", ([^;]*?)\)[,;]', java.get("PumpkinBoat", ""), re.M | re.S)}
    for boat, info in ag.PUMPKIN_BOATS.items():
        values = [v.strip().removesuffix("F") for v in re.sub(r"\s+", " ", kinds.get(info["kind"], "")).split(",")]
        width, height, face_top = info["shell"]
        want = [info["seats"], width, height, face_top, info["floor"], info["seat"], info["fastest"], info["slowest"]]
        try:
            got = [float(v) for v in values[:6] + values[-2:]]
        except ValueError:
            got = []
        if got != want:
            err(f"PumpkinBoat.Kind.{info['kind']} {values} differs from PUMPKIN_BOATS['{boat}'] in tools/agriculture.py")
        if f'"{boat}"' not in java.get("PumpkinBoat", "") or \
                f"boat(PumpkinBoat.Kind.{info['kind']}, {info['hitbox'][0]}F, {info['hitbox'][1]}F)" not in main:
            err(f"JugcraftAgriculture.java registers {boat} with a different id or hitbox than {info['hitbox']}")
    if 'List.of("' + '", "'.join(ag.COSTUMES) + '")' not in main:
        err("JugcraftAgriculture.COSTUMES differs from COSTUMES in tools/agriculture.py")

    # The Halloween window's defaults, and the treats: candy and sweets only, weights given.
    config = (JAVA_ROOT / "config" / "JugcraftConfig.java").read_text(encoding="utf-8")
    for key, value in trick["window"].items():
        if f'"{key}", "{value}"' not in config:
            err(f"JugcraftConfig.TEXT_OPTIONS {key} default differs from {value}")
    for item, weight, (low, high) in trick["treats"]:
        if weight <= 0 or not 1 <= low <= high:
            err(f"trick-or-treat: {item} needs a positive weight and count")
    states = set((load(ASSETS / "blockstates" / f"{regatta['buoy']}.json") or {}).get("variants", {}))
    if states != {f"number={n}" for n in range(1, regatta["max_number"] + 1)}:
        err("regatta_buoy: blockstate does not cover every number")


def check_festivities(java, main):
    """The Halloween festivities: Java matches tools/agriculture.py, the Peddler only sells, and the gravestones' shapes are their models."""
    def number(source, name):
        match = re.search(rf"\b{name} = ([\d.]+)[FLD]?;", java.get(source, ""))
        return float(match.group(1)) if match else None

    contest, mobs, peddler = ag.CONTEST, ag.COSTUMED_MOBS, ag.PEDDLER
    expected = {("CarvingContest", "CHECK_TICKS"): contest["check_ticks"], ("CarvingContest", "MAX_VOTERS"): contest["max_voters"],
                ("CarvingContest", "MAX_CONTESTS"): contest["max_contests"], ("CostumedMobs", "CHANCE"): mobs["chance"],
                ("GravestoneBlockEntity", "MAX_LENGTH"): ag.ENGRAVING["max_length"], ("CandleSkullBlock", "LIGHT"): ag.CANDLE_SKULL["light"]}
    for (source, name), value in expected.items():
        if number(source, name) != value:
            err(f"{source}.{name} = {number(source, name)} differs from tools/agriculture.py ({value})")
    if contest["places"] != ag.HARVEST_SCALE["board"] or "PLACES = HarvestScaleBlockEntity.BOARD;" not in java.get("CarvingContest", ""):
        err("The carving contest must hand out the Harvest Scale's ribbons, one per place")
    source = java.get("CostumedMobs", "")
    for field, values in (("MOBS", mobs["mobs"]), ("COSTUMES", mobs["costumes"])):
        found = re.search(rf"{field} = List\.of\(([^;]*)\);", source)
        if not found or re.findall(r'"([a-z_:]+)"', found.group(1)) != values:
            err(f"CostumedMobs.{field} differs from COSTUMED_MOBS in tools/agriculture.py")
    if f'COSTUMED = "{mobs["tag"]}"' not in source or f'Jugcraft.id("{mobs["table"]}")' not in source:
        err("CostumedMobs's tag or candy table differs from tools/agriculture.py")
    if f'Jugcraft.id("{peddler["trade_set"]}")' not in java.get("HalloweenPeddler", ""):
        err("HalloweenPeddler.TRADES differs from tools/agriculture.py")
    for item, weight, (low, high) in mobs["candy"]:
        if weight <= 0 or not 1 <= low <= high or (split(item)[0] == MOD and split(item)[1] not in ag.ITEMS):
            err(f"costumed mob candy: {item} needs to be a known food with a positive weight and count")
    for costume in mobs["costumes"]:
        if split(costume)[0] == MOD and costume.split(":")[1] not in ag.COSTUMES:
            err(f"costumed mobs: {costume} is not a costume")

    # The Peddler wants emeralds and gives Jugcraft goods that have another route; it never gives emeralds back.
    if not 1 <= peddler["amount"] <= len(peddler["trades"]):
        err("The Peddler must offer between one and all of its trades")
    crafted = {r["result"] for r in ag.SHAPED + ag.SHAPELESS} | set(ag.POT_RECIPES)
    for trade, info in peddler["trades"].items():
        item, count = info["gives"]
        name = split(item)[1]
        if item == "minecraft:emerald" or count < 1 or info["wants"] < 1 or info["max_uses"] < 1:
            err(f"Peddler trade {trade}: it must take emeralds and give something else")
        elif name not in crafted and name not in ag.ITEMS:
            err(f"Peddler trade {trade}: {item} has no route besides the Peddler")

    # Gravestones: the Java styles' boxes and engraving places are tools/agriculture.py's, and so are the models.
    source = java.get("GravestoneBlock", "")
    for stone, info in ag.GRAVESTONES.items():
        found = re.search(rf'{info["style"]}\("{stone}", new double\[\]\[\] (\{{\{{.*?\}}\}}),\s*([^)]*)\)', source, re.S)
        boxes = [tuple(float(v) for v in b.split(",")) for b in re.findall(r"\{([^{}]*)\}", found.group(1))] if found else []
        engraving = [v.strip() for v in found.group(2).split(",")] if found else []
        want = [f"{v}F" if isinstance(v, float) else str(v) for v in info["engraving"]]
        if boxes != [tuple(float(v) for v in b) for b in info["boxes"]] or engraving != want:
            err(f"GravestoneBlock.Style.{info['style']} differs from GRAVESTONES['{stone}'] in tools/agriculture.py")
        model = load(ASSETS / "models" / "block" / f"{stone}.json") or {}
        if [tuple(e["from"] + e["to"]) for e in model.get("elements", [])] != [tuple(b) for b in info["boxes"]]:
            err(f"{stone}: model boxes differ from its shape")


def check_night(java, main):
    """Halloween nights: Java matches tools/agriculture.py, every trebuchet state has a model, and the Horseman's loot
    is tools/agriculture.py's (dropped only when a player defeats him)."""
    def number(source, name):
        match = re.search(rf"\b{name} = ([\d.]+)[FLD]?;", java.get(source, ""))
        return float(match.group(1)) if match else None

    wisps, moon, treb, horseman = ag.WISPS, ag.HARVEST_MOON, ag.TREBUCHET, ag.HORSEMAN
    expected = {("Wisps", "SPAWN_TICKS"): wisps["spawn_ticks"], ("Wisps", "SPAWN_CHANCE"): wisps["spawn_chance"],
                ("Wisps", "MIN_DISTANCE"): wisps["min"], ("Wisps", "MAX_DISTANCE"): wisps["max"],
                ("Wisps", "CORN_RADIUS"): wisps["corn_radius"], ("Wisps", "NEAR_CAP"): wisps["near_cap"],
                ("Wisps", "NEAR_RANGE"): wisps["near_range"], ("Wisps", "LEVEL_CAP"): wisps["level_cap"],
                ("WillOWisp", "FLEE_RADIUS"): wisps["flee"], ("WillOWisp", "SNEAK_FLEE_RADIUS"): wisps["sneak_flee"],
                ("JugcraftAgriculture", "WISP_JAR_LIGHT"): wisps["jar_light"],
                ("HarvestMoon", "GROWTH_BONUS"): moon["growth_bonus"], ("HarvestMoon", "DUSK"): moon["dusk"],
                ("HarvestMoon", "DAWN"): moon["dawn"], ("HarvestMoon", "CHECK_TICKS"): moon["check_ticks"],
                ("TrebuchetBlockEntity", "REMEMBERED"): treb["remembered"], ("TrebuchetBlockEntity", "BASE_SPEED"): treb["base_speed"],
                ("TrebuchetBlockEntity", "GUST"): treb["gust"], ("TrebuchetBlockEntity", "MIN_ANGLE"): treb["min_angle"],
                ("TrebuchetBlockEntity", "MAX_ANGLE"): treb["max_angle"], ("TrebuchetBlockEntity", "ANGLE_STEP"): treb["angle_step"],
                ("TrebuchetBlockEntity", "DEFAULT_ANGLE"): treb["default_angle"],
                ("TrebuchetBlockEntity", "ADVANCEMENT_DISTANCE"): treb["advancement_distance"],
                ("TrebuchetBlock", "RESET_TICKS"): treb["reset_ticks"], ("FlyingPumpkin", "MAX_FLIGHT"): treb["max_flight"],
                ("ThrowMarker", "LIFETIME"): treb["marker_ticks"],
                ("HeadlessHorseman", "MAX_HEALTH"): horseman["health"], ("HeadlessHorseman", "ARENA_RADIUS"): horseman["arena_radius"],
                ("HeadlessHorseman", "LEAVE_RANGE"): horseman["leave_range"], ("HeadlessHorseman", "LONELY_TICKS"): horseman["lonely_ticks"],
                ("HeadlessHorseman", "THROW_COOLDOWN"): horseman["throw_cooldown"],
                ("HeadlessHorseman", "ENRAGED_THROW_COOLDOWN"): horseman["enraged_throw_cooldown"],
                ("HeadlessHorseman", "THROW_RANGE"): horseman["throw_range"], ("HorsemanSummoning", "MIDNIGHT"): horseman["midnight"],
                ("HorsemanSummoning", "HOUR_WINDOW"): horseman["hour_window"], ("HorsemanSummoning", "ONE_AT_A_TIME"): horseman["one_at_a_time"],
                ("FlamingPumpkin", "DAMAGE"): horseman["pumpkin_damage"], ("FlamingPumpkin", "SPLASH_RADIUS"): horseman["splash"],
                ("FlamingPumpkin", "FIRE_SECONDS"): horseman["fire_seconds"]}
    for (source, name), value in expected.items():
        if number(source, name) != value:
            err(f"{source}.{name} = {number(source, name)} differs from tools/agriculture.py ({value})")
    if treb["board"] != ag.HARVEST_SCALE["board"] or "BOARD = HarvestScaleBlockEntity.BOARD;" not in java.get("TrebuchetBlockEntity", ""):
        err("The trebuchet's board must hand out the Harvest Scale's ribbons, one per place")
    if not treb["min_angle"] <= treb["default_angle"] <= treb["max_angle"] or (treb["default_angle"] - treb["min_angle"]) % treb["angle_step"]:
        err("The trebuchet's default angle must be one of its angles")

    # Throwing factors: Java's are the table's, each is a pumpkin that exists, and the ammunition tag lists exactly them.
    source = java.get("TrebuchetBlockEntity", "")
    factors = {item: float(factor) for item, factor in re.findall(r'Map\.entry\("([a-z_:]+)", ([\d.]+)\)', source)}
    if factors != treb["factors"]:
        err(f"TrebuchetBlockEntity.FACTORS {factors} differs from tools/agriculture.py {treb['factors']}")
    for item, factor in treb["factors"].items():
        if split(item)[0] == MOD and split(item)[1] not in ag.all_items():
            err(f"trebuchet: {item} is not a Jugcraft item")
        if not 0.8 <= factor <= 1.2:
            err(f"trebuchet: {item}'s factor {factor} is outside 0.8 to 1.2")
    ammo = load(DATA / MOD / "tags" / "item" / f"{split(treb['ammo_tag'])[1]}.json") or {}
    if sorted(ammo.get("values", [])) != sorted(treb["factors"]):
        err("The trebuchet's ammunition tag must list exactly the pumpkins that have a throwing factor")
    treats = load(DATA / MOD / "tags" / "item" / f"{split(ag.CANDY_BAG['treat_tag'])[1]}.json") or {}
    if treats.get("values") != ag.CANDY_BAG["treats"]:
        err("The candy bag's treat tag differs from tools/agriculture.py")

    # The trebuchet's blockstate covers every facing and arm position, with a model for each arm.
    states = (load(ASSETS / "blockstates" / f"{treb['block']}.json") or {}).get("variants", {})
    want = {f"arm={arm},facing={facing}" for arm in ("ready", "loaded", "released") for facing in ("north", "east", "south", "west")}
    if set(states) != want:
        err(f"{treb['block']}: blockstate variants {sorted(states)} differ from every arm position and facing")
    arms = re.search(r"enum Arm[^{]*\{\s*([^;]+);", java.get("TrebuchetBlock", ""))
    if not arms or re.findall(r'[A-Z_]+\("([a-z_]+)"\)', arms.group(1)) != ["ready", "loaded", "released"]:
        err("TrebuchetBlock.Arm differs from the arm positions the models draw")
    for arm in ("ready", "loaded", "released"):
        if load(ASSETS / "models" / "block" / f"{treb['block']}_{arm}.json") is None:
            err(f"{treb['block']}: no model for arm position {arm}")

    # The Harvest Moon's day is configured, defaulting to tools/agriculture.py's.
    config = (JAVA_ROOT / "config" / "JugcraftConfig.java").read_text(encoding="utf-8")
    if f'"halloween.harvest_moon", "{moon["day"]}"' not in config:
        err(f"JugcraftConfig's halloween.harvest_moon default differs from tools/agriculture.py ({moon['day']})")
    month, day = (int(v) for v in moon["day"].split("-"))
    if f"DEFAULT_HARVEST_MOON = MonthDay.of({month}, {day})" not in java.get("HalloweenSeason", ""):
        err("HalloweenSeason.DEFAULT_HARVEST_MOON differs from tools/agriculture.py")

    # The Horseman drops tools/agriculture.py's loot, and only to a player's kill.
    table = load(DATA / MOD / "loot_table" / f"{horseman['table']}.json") or {}
    drops = []
    for pool in table.get("pools", []):
        if pool.get("condition", {}).get("type") != "minecraft:killed_by_player":
            err(f"{horseman['table']}: every pool needs a player's kill")
        for entry in pool.get("entries", []):
            modifier = entry.get("modifier", {})
            count = modifier.get("count", 1) if modifier.get("type") == "minecraft:set_count" else 1
            low, high = (count["min"], count["max"]) if isinstance(count, dict) else (count, count)
            drops.append((entry.get("name"), [low, high]))
    if drops != [(item, list(counts)) for item, counts in horseman["loot"]]:
        err(f"{horseman['table']}: drops {drops} differ from tools/agriculture.py {horseman['loot']}")
    # Every way a summoning can fail has a message for the player.
    lang = load(ASSETS / "lang" / "en_us.json") or {}
    results = re.search(r"enum Result \{([A-Z_,\s]+)\}", java.get("HorsemanSummoning", ""))
    for result in (re.findall(r"[A-Z_]+", results.group(1)) if results else []):
        if result != "SUMMONED" and f"message.jugcraft.horseman.{result.lower()}" not in lang:
            err(f"HorsemanSummoning.Result.{result} has no message.jugcraft.horseman.{result.lower()} text")
    if not results:
        err("HorsemanSummoning.Result not found")
    registration = re.search(r'HEADLESS_HORSEMAN = entity\("([a-z_]+)"[^;]*;', main)
    if not registration or f"entities/{registration.group(1)}" != horseman["table"] or "noLootTable" in registration.group(0):
        err(f"The Headless Horseman must be registered with his loot table {horseman['table']}")

def check_decor(java):
    """The first decorations batch: Java matches tools/agriculture.py, every block state has a model, every result has
    its message, and the portraits' eyes sit inside their frames where the textures paint them."""
    def number(source, name):
        match = re.search(rf"\b{name} = ([\d.]+)[FLD]?;", java.get(source, ""))
        return float(match.group(1)) if match else None

    def numbers(source, name):
        match = re.search(rf"\b{name} = \{{([\d, ]+)\}};", java.get(source, ""))
        return [int(v) for v in match.group(1).split(",")] if match else None

    lights, bowl, coffin, portrait, fog = ag.STRING_LIGHTS, ag.CANDY_BOWL, ag.COFFIN, ag.HAUNTED_PORTRAIT, ag.FOG_MACHINE
    expected = {("StringLightHookBlockEntity", "MAX_LENGTH"): lights["max_length"], ("StringLightHookBlockEntity", "USE"): lights["use"],
                ("StringLightHookBlockEntity", "CAPACITY"): lights["capacity"], ("StringLightHookBlockEntity", "INPUT"): lights["input"],
                ("StringLightHookBlockEntity", "CHECK_TICKS"): lights["check_ticks"], ("StringLightHookBlock", "LIGHT"): lights["light"],
                ("CandyBowlBlockEntity", "CAPACITY"): bowl["capacity"], ("CandyBowlBlockEntity", "VISITORS"): bowl["visitors"],
                ("CoffinBlockEntity", "SLOTS"): coffin["slots"], ("FogMachineBlockEntity", "USE"): fog["use"],
                ("FogMachineBlockEntity", "CAPACITY"): fog["capacity"], ("FogMachineBlockEntity", "INPUT"): fog["input"],
                ("FogMachineBlockEntity", "PARTICLES_PER_TICK"): fog["particles_per_tick"], ("FogMachineBlockEntity", "BUDGET"): fog["budget"],
                ("FogMachineBlockEntity", "VIEW"): fog["view"]}
    for (source, name), value in expected.items():
        if number(source, name) != value:
            err(f"{source}.{name} = {number(source, name)} differs from tools/agriculture.py ({value})")
    if numbers("CandyBowlBlockEntity", "FILL") != bowl["fill"] or numbers("FogMachineBlockEntity", "RADII") != fog["radii"]:
        err("CandyBowlBlockEntity.FILL or FogMachineBlockEntity.RADII differs from tools/agriculture.py")
    if fog["particles_per_tick"] > fog["budget"] or 2 + len(fog["radii"]) - 1 > fog["particles_per_tick"]:
        err("The fog machine's per-machine puffs must fit its per-tick budget")

    # The portraits: Java's eyes are the table's, inside the frame, and the textures paint a white where each eye is.
    found = {name: [tuple(int(v) for v in eye.split(",")) for eye in re.findall(r"new int\[\] \{([\d, ]+)\}", eyes)]
             for name, eyes in re.findall(r'[A-Z]+\("([a-z]+)", List\.of\(([^;]*?)\)\)', java.get("HauntedPortraitBlock", ""))}
    if found != {name: [tuple(e) for e in eyes] for name, eyes in portrait["portraits"].items()}:
        err(f"HauntedPortraitBlock.Portrait eyes {found} differ from tools/agriculture.py")
    for name, eyes in portrait["portraits"].items():
        image = ASSETS / "textures" / "block" / f"{portrait['block']}_{name}.png"
        pixels = Image.open(image).convert("RGBA") if image.exists() else None
        for x, y, w, h in eyes:
            if x < 1 or y < 1 or x + w > 15 or y + h > 15:
                err(f"{name}: an eye at {(x, y, w, h)} runs into the frame")
            elif pixels is not None and len({pixels.getpixel((x + i, y + j)) for i in range(w) for j in range(h)}) != 1:
                err(f"{name}: the eye at {(x, y, w, h)} is not one painted white")

    # Every combination of each block's state properties has a model.
    def variants(block):
        return set((load(ASSETS / "blockstates" / f"{block}.json") or {}).get("variants", {}))
    horizontal = ("north", "east", "south", "west")
    wanted = {
        lights["hook"]: {f"face={a},facing={f},lit={l}" for a in ("floor", "wall", "ceiling") for f in horizontal for l in ("false", "true")},
        bowl["block"]: {f"facing={f},fill={n}" for f in horizontal for n in range(4)},
        coffin["block"]: {f"facing={f},open={o},part={p}" for f in horizontal for o in ("false", "true") for p in ("head", "foot")},
        portrait["block"]: {f"facing={f},portrait={n}" for f in horizontal for n in portrait["portraits"]},
        fog["block"]: {f"facing={f},running={r}" for f in horizontal for r in ("false", "true")},
    }
    for block, keys in wanted.items():
        if variants(block) != keys:
            err(f"{block}: blockstate variants differ from its properties")

    # Every outcome a player can be told about has its text.
    lang = load(ASSETS / "lang" / "en_us.json") or {}
    for source, enum, prefix in (("StringLightsItem", "Result", "message.jugcraft.string_lights."), ("CandyBowlBlockEntity", "Taken", "message.jugcraft.candy_bowl.")):
        values = re.search(rf"enum {enum} \{{([A-Z_,\s]+)\}}", java.get(source, ""))
        for value in re.findall(r"[A-Z_]+", values.group(1)) if values else []:
            if prefix + value.lower() not in lang:
                err(f"{source}.{enum}.{value} has no {prefix}{value.lower()} text")
        if not values:
            err(f"{source}.{enum} not found")
    for name in portrait["portraits"]:
        if f"message.jugcraft.haunted_portrait.{name}" not in lang:
            err(f"The {name} portrait has no name")

def check_decor2(java):
    """The second decorations batch: Java matches tools/agriculture.py (lights, the floating candles' places, the strand
    kinds), every block state has a model, the bag's walls have a face cut through them and its inside none (so the
    face shines with the candlelit far wall), and the soul-lit carving icon uses the client's soul colours."""
    def number(source, name):
        match = re.search(rf"\b{name} = ([\d.]+)[FLD]?;", java.get(source, ""))
        return float(match.group(1)) if match else None

    bag, candle, sconce, soul, bunting = ag.LUMINARIA, ag.FLOATING_CANDLE, ag.SCONCE, ag.SOUL_CARVING, ag.BAT_BUNTING
    expected = {("LuminariaBlock", "LIGHT"): bag["light"], ("FloatingCandleBlock", "MAX"): candle["max"],
                ("FloatingCandleBlock", "LIGHT_PER_CANDLE"): candle["light_per_candle"], ("FloatingCandleBlock", "BOB"): candle["bob"],
                ("FloatingCandleBlock", "BOB_TICKS"): candle["bob_ticks"], ("SkeletonHandSconceBlock", "LIGHT"): sconce["light"],
                ("CarvedPumpkinBlock", "SOUL_LIGHT"): soul["light"]}
    for (source, name), value in expected.items():
        if number(source, name) != value:
            err(f"{source}.{name} = {number(source, name)} differs from tools/agriculture.py ({value})")
    source = java.get("FloatingCandleBlock", "")
    table = re.search(r"CANDLE = \{(.*?)\};", source, re.S)
    places = [[tuple(float(v.rstrip("F")) for v in spot.split(",")) for spot in re.findall(r"\{([\d., F]+)\}", group)]
              for group in re.findall(r"\{(\{[^{}]*\}(?:, \{[^{}]*\})*)\}", table.group(1))] if table else []
    if places != [[tuple(float(v) for v in spot) for spot in group] for group in candle["candles"]]:
        err(f"FloatingCandleBlock.CANDLE {places} differs from tools/agriculture.py")
    heights = re.search(r"HEIGHT = \{([\d., F]+)\};", source)
    if not heights or [float(v.strip().rstrip("F")) for v in heights.group(1).split(",")] != [float(h) for h in candle["heights"]]:
        err("FloatingCandleBlock.HEIGHT differs from tools/agriculture.py")
    if any(len(group) != n + 1 for n, group in enumerate(candle["candles"])) or len(candle["heights"]) != candle["max"]:
        err("FLOATING_CANDLE needs one place per candle and a height for each place")
    if candle["max"] * candle["light_per_candle"] > 15:
        err("Floating candles would give more than light 15")
    strands = re.findall(r'[A-Z]+\("([a-z]+)", "([a-z_]+)"\)', java.get("StringLightHookBlockEntity", ""))
    if dict(strands) != {"lights": ag.STRING_LIGHTS["strand"], "bunting": bunting["item"]}:
        err(f"StringLightHookBlockEntity.Strand {strands} differs from the strand items in tools/agriculture.py")

    def variants(block):
        return set((load(ASSETS / "blockstates" / f"{block}.json") or {}).get("variants", {}))
    horizontal = ("north", "east", "south", "west")
    booleans = ("false", "true")
    wanted = {
        bag["block"]: {f"color={c},lit={l}" for c in ag.DYE_COLORS for l in booleans},
        candle["block"]: {f"candles={n},lit={l}" for n in range(1, candle["max"] + 1) for l in booleans},
        sconce["block"]: {f"facing={f},lit={l}" for f in horizontal for l in booleans},
    }
    for block, keys in wanted.items():
        if variants(block) != keys:
            err(f"{block}: blockstate variants differ from its properties")

    # The bag's walls are cut with a face where the model shows them (x 4-11, y 6-15); the inside is whole.
    for color in ag.DYE_COLORS:
        for suffix in ("", "_lit"):
            image = ASSETS / "textures" / "block" / f"luminaria_{color}{suffix}.png"
            if not image.exists():
                err(f"Missing texture {image.name}")
                continue
            pixels = Image.open(image).convert("RGBA")
            if not any(pixels.getpixel((x, y))[3] == 0 for x in range(4, 12) for y in range(6, 16)):
                err(f"{image.name}: no face cut through the bag")
    for name in ("luminaria_inside", "luminaria_inside_lit"):
        image = ASSETS / "textures" / "block" / f"{name}.png"
        if not image.exists() or Image.open(image).convert("RGBA").getextrema()[3][0] < 255:
            err(f"{name}.png: missing, or see-through (the inside of the bag must be whole)")

    # The soul-lit icon and the client's soul colours agree.
    client = (ROOT / "src" / "client" / "java" / "io" / "github" / "jimbozoomer" / "jugcraft" / "client" / "CarvingTextures.java")
    colors = dict(re.findall(r"int (HOLE_SOUL|HOLE_WALL_SOUL) = 0xFF([0-9A-Fa-f]{6});", client.read_text(encoding="utf-8")))
    icon = ASSETS / "textures" / "item" / "hand_carved_pumpkin_soul.png"
    if icon.exists():
        pixels = Image.open(icon).convert("RGBA")
        used = {"%02X%02X%02X" % pixels.getpixel((x, y))[:3] for x in range(16) for y in range(16) if pixels.getpixel((x, y))[3]}
        if not {c.upper() for c in colors.values()} <= used or len(colors) != 2:
            err("The soul-lit carved pumpkin icon should use CarvingTextures' HOLE_SOUL and HOLE_WALL_SOUL")
    else:
        err("Missing texture hand_carved_pumpkin_soul.png")


def check_decor3(java):
    """The graveyard batch: the scare props' timings match tools/agriculture.py, every block state of the fence, gate,
    pillar, door, mound, angel and pop-up skeleton has a model, and the fence, gate and door are in vanilla's tags."""
    def number(source, name):
        match = re.search(rf"\b{name} = ([\d.]+)[FLD]?;", java.get(source, ""))
        return float(match.group(1)) if match else None

    expected = {("ScarePropBlockEntity", "PERIOD"): ag.SCARE_PERIOD}
    for source, info in (("GraveMoundBlock", ag.GRAVE_MOUND), ("PopUpSkeletonBlock", ag.POP_UP_SKELETON)):
        expected.update({(source, "REACH"): info["reach"], (source, "UP_TICKS"): info["up_ticks"],
                         (source, "COOLDOWN_TICKS"): info["cooldown_ticks"]})
        if f"implements ScareProp" not in java.get(source, ""):
            err(f"{source} is not a ScareProp")
        if info["up_ticks"] < ag.SCARE_PERIOD:
            err(f"{source}: it must stay up at least one look ({ag.SCARE_PERIOD} ticks)")
    for (source, name), value in expected.items():
        if number(source, name) != value:
            err(f"{source}.{name} = {number(source, name)} differs from tools/agriculture.py ({value})")

    def variants(block):
        return set((load(ASSETS / "blockstates" / f"{block}.json") or {}).get("variants", {}))
    horizontal = ("north", "east", "south", "west")
    booleans = ("false", "true")
    fence, gate, crypt = ag.CEMETERY_FENCE["fence"], ag.CEMETERY_FENCE["gate"], ag.CRYPT
    wanted = {
        gate: {f"facing={f},in_wall={w},open={o}" for f in horizontal for w in booleans for o in booleans},
        crypt["pillar"]: {"axis=x", "axis=y", "axis=z"},
        crypt["door"]: {f"facing={f},half={h},hinge={g},open={o}" for f in horizontal for h in ("lower", "upper") for g in ("left", "right")
                        for o in booleans},
        ag.GRAVE_MOUND["block"]: {f"facing={f},raised={r}" for f in horizontal for r in booleans},
        ag.POP_UP_SKELETON["block"]: {f"facing={f},raised={r}" for f in horizontal for r in booleans},
        ag.MOURNING_ANGEL["block"]: {f"facing={f},half={h}" for f in horizontal for h in ("lower", "upper")},
    }
    for block, keys in wanted.items():
        if variants(block) != keys:
            err(f"{block}: blockstate variants differ from its properties")
    parts = (load(ASSETS / "blockstates" / f"{fence}.json") or {}).get("multipart", [])
    if sorted(next(iter(p.get("when", {"post": 1}))) for p in parts) != sorted(["post", "north", "east", "south", "west"]):
        err(f"{fence}: the multipart needs the post and a side for each direction")
    for tag, entry in (("fences", fence), ("fence_gates", gate), ("doors", crypt["door"])):
        for registry in ("block", "item"):
            values = (load(RES / "data" / "minecraft" / "tags" / registry / f"{tag}.json") or {}).get("values", [])
            if f"{MOD}:{entry}" not in values:
                err(f"{entry} is missing from the {registry} tag minecraft:{tag}")


def check_decor4(java):
    """The witch's cottage: Java matches tools/agriculture.py (lights, timings, the brews, arrangements, fortunes and
    spreads), every block state has a model, every fortune and spell has its text, and every brew its item tag."""
    def number(source, name):
        match = re.search(rf"\b{name} = ([\d.]+)[FLD]?;", java.get(source, ""))
        return float(match.group(1)) if match else None

    cauldron, shelf, ball, grimoire, broom = ag.CAULDRON, ag.APOTHECARY_SHELF, ag.CRYSTAL_BALL, ag.GRIMOIRE, ag.BROOM
    expected = {("BubblingCauldronBlock", "BREW_LIGHT"): cauldron["light"], ("ApothecaryShelfBlock", "ARRANGEMENTS"): shelf["arrangements"],
                ("CrystalBallBlock", "LIGHT"): ball["light"], ("CrystalBallBlock", "GAZING_LIGHT"): ball["gazing_light"],
                ("CrystalBallBlock", "FORTUNES"): len(ball["fortunes"]), ("CrystalBallBlock", "GAZE_TICKS"): ball["gaze_ticks"],
                ("GrimoireStandBlock", "LIGHT"): grimoire["light"]}
    for (source, name), value in expected.items():
        if number(source, name) != value:
            err(f"{source}.{name} = {number(source, name)} differs from tools/agriculture.py ({value})")

    def enum(source, name):
        match = re.search(rf"enum {name} implements StringRepresentable \{{\s*([A-Z_, ]+);", java.get(source, ""))
        return [v.strip().lower() for v in match.group(1).split(",")] if match else None
    if enum("BubblingCauldronBlock", "Brew") != ["empty", "water"] + list(cauldron["brews"]):
        err(f"BubblingCauldronBlock.Brew {enum('BubblingCauldronBlock', 'Brew')} differs from CAULDRON's brews")
    if enum("GrimoireStandBlock", "Spread") != list(grimoire["spreads"]):
        err(f"GrimoireStandBlock.Spread {enum('GrimoireStandBlock', 'Spread')} differs from GRIMOIRE's spreads")

    def variants(block):
        return set((load(ASSETS / "blockstates" / f"{block}.json") or {}).get("variants", {}))
    horizontal = ("north", "east", "south", "west")
    wanted = {
        cauldron["block"]: {f"contents={c}" for c in ["empty", "water"] + list(cauldron["brews"])},
        shelf["block"]: {f"arrangement={n},facing={f}" for f in horizontal for n in range(shelf["arrangements"])},
        ball["block"]: {"gazing=false", "gazing=true"},
        grimoire["block"]: {f"facing={f},page={p}" for f in horizontal for p in grimoire["spreads"]},
        broom["block"]: {f"facing={f}" for f in horizontal},
    }
    for block, keys in wanted.items():
        if variants(block) != keys:
            err(f"{block}: blockstate variants differ from its properties")
    lang = load(ASSETS / "lang" / "en_us.json") or {}
    for n in range(len(ball["fortunes"])):
        if f"message.{MOD}.crystal_ball.fortune.{n}" not in lang:
            err(f"Crystal ball fortune {n} has no text")
    for spread in grimoire["spreads"]:
        if f"message.{MOD}.grimoire.{spread}" not in lang:
            err(f"The {spread} spread has no name")
    for colour, items in cauldron["brews"].items():
        values = (load(DATA / MOD / "tags" / "item" / "brew" / f"{colour}.json") or {}).get("values", [])
        if sorted(values) != sorted(items):
            err(f"The item tag {MOD}:brew/{colour} differs from CAULDRON's brews")


def check_decor5(java):
    """The harvest party: Java matches tools/agriculture.py (the tub's apples, odds and splash, the crate's capacity
    and tag, the bale's seat height and softening, the wreath's flowers, the piles' layers, softening and colours), every
    block state has a model, and the tub's messages have their text."""
    def number(source, name):
        match = re.search(rf"\b{name} = ([\d.]+)[FLD]?;", java.get(source, ""))
        return float(match.group(1)) if match else None

    tub, crate, bale, wreath, piles = ag.BOBBING_TUB, ag.PUMPKIN_CRATE, ag.HAY_BALE_SEAT, ag.AUTUMN_WREATH, ag.LEAF_PILES
    expected = {("BobbingTubBlock", "MAX_APPLES"): tub["max_apples"], ("BobbingTubBlock", "CHANCE"): tub["chance"],
                ("BobbingTubBlock", "SPLASH_TICKS"): tub["splash_ticks"], ("PumpkinCrateBlockEntity", "CAPACITY"): crate["capacity"],
                ("HayBaleSeatBlock", "FALL_SOFTENING"): bale["fall_softening"], ("LeafPileBlock", "MAX_LAYERS"): piles["max_layers"],
                ("LeafPileBlock", "LAYER_PIXELS"): piles["layer_pixels"], ("LeafPileBlock", "SOFTENING_PER_LAYER"): piles["softening_per_layer"],
                ("LeafPileBlock", "SCATTER_CHANCE"): piles["scatter_chance"]}
    for (source, name), value in expected.items():
        if number(source, name) is None or abs(number(source, name) - value) > 1e-9:
            err(f"{source}.{name} = {number(source, name)} differs from tools/agriculture.py ({value})")
    height = re.search(r"HEIGHT = ([\d.]+) / ([\d.]+);", java.get("HayBaleSeatBlock", ""))
    if not height or abs(float(height.group(1)) / float(height.group(2)) - bale["height"]) > 1e-9:
        err(f"HayBaleSeatBlock.HEIGHT differs from HAY_BALE_SEAT's height ({bale['height']})")
    if f'Jugcraft.id("{crate["produce_tag"].split(":")[1]}")' not in java.get("PumpkinCrateBlock", ""):
        err(f"PumpkinCrateBlock.PRODUCE is not {crate['produce_tag']}")
    mums = re.search(r"enum Mums implements StringRepresentable \{\s*([A-Z_, ]+);", java.get("AutumnWreathBlock", ""))
    if not mums or [v.strip().lower() for v in mums.group(1).split(",")] != wreath["flowers"]:
        err("AutumnWreathBlock.Mums differs from AUTUMN_WREATH's flowers")
    if f"Mums.{wreath['default'].upper()}" not in java.get("AutumnWreathBlock", ""):
        err("AutumnWreathBlock's default flowers differ from AUTUMN_WREATH's default")
    colours = re.search(r"LEAF_PILE_COLOURS = List\.of\(([^)]*)\)", java.get("JugcraftAgriculture", ""))
    if not colours or re.findall(r'"(\w+)"', colours.group(1)) != list(piles["colours"]):
        err("JugcraftAgriculture.LEAF_PILE_COLOURS differs from LEAF_PILES' colours")

    def variants(block):
        return set((load(ASSETS / "blockstates" / f"{block}.json") or {}).get("variants", {}))
    horizontal = ("north", "east", "south", "west")
    wanted = {
        tub["block"]: {f"apples={n},splashing={s}" for n in range(tub["max_apples"] + 1) for s in ("false", "true")},
        crate["block"]: {f"facing={f}" for f in horizontal},
        bale["block"]: {f"facing={f}" for f in horizontal},
        wreath["block"]: {f"facing={f},flowers={c}" for f in horizontal for c in wreath["flowers"]},
        **{pile: {f"layers={n}" for n in range(1, piles["max_layers"] + 1)} for pile in ag.leaf_piles()},
    }
    for block, keys in wanted.items():
        if variants(block) != keys:
            err(f"{block}: blockstate variants differ from its properties")
    lang = load(ASSETS / "lang" / "en_us.json") or {}
    for key in tub["messages"]:
        if f"message.{MOD}.{tub['block']}.{key}" not in lang:
            err(f"The tub's {key} message has no text")
    values = (load(DATA / MOD / "tags" / "item" / f"{crate['produce_tag'].split(':')[1]}.json") or {}).get("values", [])
    if sorted(values) != sorted(crate["produce"]):
        err(f"The item tag {crate['produce_tag']} differs from PUMPKIN_CRATE's produce")

def check_decor6(java):
    """The haunted house and yard: Java matches tools/agriculture.py (the chair's seat and rocking, the eyes' distance
    and blinking, the window's designs and light, the music box's tune length and every note in range, the spider's
    drop and sway), every block state has a model, and the quads, glowing papers and eye sprite the client draws exist."""
    def number(source, name):
        match = re.search(rf"\b{name} = ([\d.]+)[FLD]?;", java.get(source, ""))
        return float(match.group(1)) if match else None

    chair, eyes, window, box_, spider = ag.ROCKING_CHAIR, ag.LURKING_EYES, ag.SILHOUETTE_WINDOW, ag.MUSIC_BOX, ag.GIANT_FAKE_SPIDER
    expected = {("RockingChairBlock", "HAUNTED_ROCK"): chair["haunted_rock"], ("RockingChairBlock", "SITTER_ROCK"): chair["sitter_rock"],
                ("RockingChairBlock", "ROCK_PERIOD"): chair["rock_period"], ("LurkingEyesBlock", "HIDE_DISTANCE"): eyes["hide_distance"],
                ("LurkingEyesBlock", "BLINK_PERIOD"): eyes["blink_period"], ("LurkingEyesBlock", "BLINK_TICKS"): eyes["blink_ticks"],
                ("SilhouetteWindowBlock", "GLOW_LIGHT"): window["glow_light"], ("MusicBoxBlockEntity", "TICKS_PER_BEAT"): box_["ticks_per_beat"],
                ("MusicBoxBlockEntity", "BEATS"): box_["beats"], ("GiantFakeSpiderBlock", "MAX_DROP"): spider["max_drop"],
                ("GiantFakeSpiderBlock", "SWAY_PERIOD"): spider["sway_period"], ("GiantFakeSpiderBlock", "SWAY_DEGREES"): spider["sway_degrees"]}
    for (source, name), value in expected.items():
        if number(source, name) is None or abs(number(source, name) - value) > 1e-9:
            err(f"{source}.{name} = {number(source, name)} differs from tools/agriculture.py ({value})")
    seat = re.search(r"SEAT_HEIGHT = ([\d.]+) / ([\d.]+);", java.get("RockingChairBlock", ""))
    if not seat or abs(float(seat.group(1)) / float(seat.group(2)) - chair["seat_height"]) > 1e-9:
        err(f"RockingChairBlock.SEAT_HEIGHT differs from ROCKING_CHAIR's seat_height ({chair['seat_height']})")
    designs = re.search(r"enum Design implements StringRepresentable \{\s*([A-Z_, ]+);", java.get("SilhouetteWindowBlock", ""))
    if not designs or [v.strip().lower() for v in designs.group(1).split(",")] != window["designs"]:
        err("SilhouetteWindowBlock.Design differs from SILHOUETTE_WINDOW's designs")
    tune = re.search(r"TUNE = \{(.*?)\};", java.get("MusicBoxBlockEntity", ""), re.S)
    notes = re.findall(r"\{(\d+), (\d+), (BELL|HARP)\}", tune.group(1)) if tune else []
    if not notes:
        err("MusicBoxBlockEntity.TUNE has no notes")
    for beat, note, _ in notes:
        if not 0 <= int(beat) < box_["beats"] or not 0 <= int(note) <= 24:
            err(f"Music box note {{{beat}, {note}}} is outside the tune or a note block's range")

    def variants(block):
        return set((load(ASSETS / "blockstates" / f"{block}.json") or {}).get("variants", {}))
    horizontal = ("north", "east", "south", "west")
    wanted = {
        chair["block"]: {f"facing={f}" for f in horizontal},
        eyes["block"]: {f"facing={f}" for f in horizontal + ("up", "down")},
        window["block"]: {f"design={d},facing={f}" for f in horizontal for d in window["designs"]},
        box_["block"]: {f"facing={f},open={o},powered={p}" for f in horizontal for o in ("false", "true") for p in ("false", "true")},
        spider["block"]: {f"drop={n}" for n in range(1, spider["max_drop"] + 1)},
    }
    for block, keys in wanted.items():
        if variants(block) != keys:
            err(f"{block}: blockstate variants differ from its properties")
    quads = load(ASSETS / "decor_quads.json") or {}
    for name in ("rocking_chair", "giant_fake_spider"):
        if not quads.get(name):
            err(f"decor_quads.json has no quads for {name}")
        for quad in quads.get(name, []):
            if not (ASSETS / "textures" / "block" / f"{quad['texture']}.png").exists():
                err(f"decor_quads.json: {name} uses a missing texture {quad['texture']}")
                break
    for texture in [f"silhouette_window_{d}_lit" for d in window["designs"]] + ["lurking_eyes"]:
        if not (ASSETS / "textures" / "entity" / f"{texture}.png").exists():
            err(f"Missing entity texture {texture}")

def check_decor7(java):
    """The haunted house inside: Java matches tools/agriculture.py (the chandelier's candles, gusts and sway, the
    organ's size, tune length and phantom chance, the suit's watching, the sheet's tag and breathing, the mirror's face,
    the curtains' drape and sway, the doll's glances), every note of the organ's tune is in range, every block state
    has a model, and the quads and textures the client draws exist."""
    import decor7_data

    def number(source, name):
        match = re.search(rf"\b{name} = ([\d.]+)[FLD]?;", java.get(source, ""))
        return float(match.group(1)) if match else None

    chandelier, organ, armor, sheet = ag.HAUNTED_CHANDELIER, ag.PIPE_ORGAN, ag.SUIT_OF_ARMOR, ag.DUST_SHEET
    mirror, curtains, doll = ag.SPIRIT_MIRROR, ag.TATTERED_CURTAINS, ag.CREEPY_DOLL
    expected = {("HauntedChandelierBlock", "CANDLES"): chandelier["candles"], ("HauntedChandelierBlock", "GUST_CHANCE"): chandelier["gust_chance"],
                ("HauntedChandelierBlock", "RELIGHT_TICKS"): chandelier["relight_ticks"],
                ("HauntedChandelierBlock", "SWAY_DEGREES"): chandelier["sway_degrees"], ("HauntedChandelierBlock", "SWAY_PERIOD"): chandelier["sway_period"],
                ("HauntedChandelierBlock", "RING_RADIUS"): decor7_data.RING,
                ("PipeOrganBlock", "WIDTH"): organ["width"], ("PipeOrganBlock", "HEIGHT"): organ["height"],
                ("PipeOrganBlock", "PHANTOM_CHANCE"): organ["phantom_chance"], ("PipeOrganBlockEntity", "TUNE_TICKS"): organ["tune_ticks"],
                ("SuitOfArmorBlock", "WATCH_RANGE"): armor["watch_range"], ("SuitOfArmorBlock", "TURN_SPEED"): armor["turn_speed"],
                ("SuitOfArmorBlock", "MAX_TURN"): armor["max_turn"], ("DustSheetBlock", "BREATHE_CHANCE"): sheet["breathe_chance"],
                ("SpiritMirrorBlock", "PERIOD"): mirror["period"], ("SpiritMirrorBlock", "VISIBLE"): mirror["visible"],
                ("SpiritMirrorBlock", "FADE"): mirror["fade"], ("SpiritMirrorBlock", "RANGE"): mirror["range"],
                ("TatteredCurtainsBlock", "MAX_DROP"): curtains["max_drop"], ("TatteredCurtainsBlock", "SWAY"): curtains["sway"],
                ("TatteredCurtainsBlock", "NIGHT_SWAY"): curtains["night_sway"], ("TatteredCurtainsBlock", "SWAY_PERIOD"): curtains["sway_period"],
                ("CreepyDollBlock", "UNSEEN_TICKS"): doll["unseen_ticks"], ("CreepyDollBlock", "ELSEWHERE_CHANCE"): doll["elsewhere_chance"]}
    for (source, name), value in expected.items():
        if number(source, name) is None or abs(number(source, name) - value) > 1e-9:
            err(f"{source}.{name} = {number(source, name)} differs from tools/agriculture.py ({value})")
    tune = re.search(r"TUNE = \{(.*?)\};", java.get("PipeOrganBlockEntity", ""), re.S)
    notes = re.findall(r"\{(\d+), (\d+), (FLUTE|HARP|BASS)\}", tune.group(1)) if tune else []
    if not notes:
        err("PipeOrganBlockEntity.TUNE has no notes")
    for tick, note, _ in notes:
        if not 0 <= int(tick) < organ["tune_ticks"] or not 0 <= int(note) <= 24:
            err(f"Organ note {{{tick}, {note}}} is outside the tune or a note block's range")
    if f'Jugcraft.id("{sheet["tag"].split(":")[1]}")' not in java.get("JugcraftAgriculture", ""):
        err(f"JugcraftAgriculture.DUST_SHEET_COVERABLE is not {sheet['tag']}")
    values = (load(DATA / MOD / "tags" / "block" / f"{sheet['tag'].split(':')[1]}.json") or {}).get("values", [])
    if sorted(values) != sorted(sheet["coverable"]):
        err(f"The block tag {sheet['tag']} differs from DUST_SHEET's coverable blocks")

    def variants(block):
        return set((load(ASSETS / "blockstates" / f"{block}.json") or {}).get("variants", {}))
    horizontal = ("north", "east", "south", "west")
    booleans = ("false", "true")
    wanted = {
        chandelier["block"]: {f"burning={n},lit={b}" for n in range(chandelier["candles"] + 1) for b in booleans},
        organ["block"]: {f"facing={f},part={p},playing={a},powered={b}" for f in horizontal for p in range(organ["width"] * organ["height"])
                         for a in booleans for b in booleans},
        armor["block"]: {f"facing={f},half={h}" for f in horizontal for h in ("lower", "upper")},
        sheet["block"]: {""},
        mirror["block"]: {f"facing={f}" for f in horizontal},
        curtains["block"]: {f"facing={f},open={o},part={p}" for f in horizontal for o in booleans for p in ("single", "top", "middle", "bottom")},
        doll["block"]: {f"facing={f}" for f in horizontal},
    }
    for block, keys in wanted.items():
        if variants(block) != keys:
            err(f"{block}: blockstate variants differ from its properties")
    quads = load(ASSETS / "decor7_quads.json") or {}
    for name in ("haunted_chandelier", "suit_of_armor_helmet", "creepy_doll_head"):
        if not quads.get(name):
            err(f"decor7_quads.json has no quads for {name}")
        for quad in quads.get(name, []):
            if not (ASSETS / "textures" / "block" / f"{quad['texture']}.png").exists():
                err(f"decor7_quads.json: {name} uses a missing texture {quad['texture']}")
                break
    for kind, texture in (("entity", "haunted_chandelier_flame"), ("entity", "suit_of_armor_glow"), ("entity", "dust_sheet_side"),
                          ("entity", "spirit_mirror_face"), ("entity", "tattered_curtains"), ("entity", "tattered_curtains_hem"),
                          ("block", "phantom_pipe_organ_ivory"), ("block", "phantom_pipe_organ_ebony"), ("block", "tattered_curtains_rod"),
                          ("block", "dust_sheet")):
        if not (ASSETS / "textures" / kind / f"{texture}.png").exists():
            err(f"Missing {kind} texture {texture}")
    lang = load(ASSETS / "lang" / "en_us.json") or {}
    for key in ("day", "night"):
        if f"message.{MOD}.{mirror['block']}.{key}" not in lang:
            err(f"The spirit mirror's {key} message has no text")


def check_decor8(java):
    """The mad scientist and monsters: Java matches tools/agriculture.py (the coil's power, range and arcs, the table's
    sitting and twitching, the jar's specimens, light and bob, the sarcophagus's timing and swing, the raven's watching
    and ruffling, the cat's reach, hiss and swish), every block state has a model, and the quads and textures the client
    draws exist."""
    def number(source, name):
        match = re.search(rf"\b{name} = ([\d.]+)[FLD]?;", java.get(source, ""))
        return float(match.group(1)) if match else None

    coil, table, jar, sarcophagus, raven, cat = ag.TESLA_COIL, ag.LAB_TABLE, ag.SPECIMEN_JAR, ag.SARCOPHAGUS, ag.RAVEN, ag.BLACK_CAT
    expected = {("TeslaCoilBlockEntity", "USE"): coil["use"], ("TeslaCoilBlockEntity", "CAPACITY"): coil["capacity"],
                ("TeslaCoilBlockEntity", "INPUT"): coil["input"], ("TeslaCoilBlockEntity", "RANGE"): coil["range"],
                ("TeslaCoilBlockEntity", "ARC_MIN"): coil["arc_min"], ("TeslaCoilBlockEntity", "ARC_SPREAD"): coil["arc_spread"],
                ("TeslaCoilBlockEntity", "ARC_TICKS"): coil["arc_ticks"], ("TeslaCoilBlock", "LIGHT"): coil["light"],
                ("LabTableBlock", "SIT_DEGREES"): table["sit_degrees"], ("LabTableBlock", "SIT_SPEED"): table["sit_speed"],
                ("LabTableBlock", "TWITCH_PERIOD"): table["twitch_period"], ("LabTableBlock", "TWITCH_TICKS"): table["twitch_ticks"],
                ("SpecimenJarBlock", "LIGHT"): jar["light"], ("SpecimenJarBlock", "BOB"): jar["bob"], ("SpecimenJarBlock", "BOB_TICKS"): jar["bob_ticks"],
                ("MummySarcophagusBlock", "OPEN_TICKS"): sarcophagus["open_ticks"], ("MummySarcophagusBlock", "LID_DEGREES"): sarcophagus["lid_degrees"],
                ("MummySarcophagusBlock", "LURCH"): sarcophagus["lurch"], ("RavenPerchBlock", "WATCH_RANGE"): raven["watch_range"],
                ("RavenPerchBlock", "MAX_TURN"): raven["max_turn"], ("RavenPerchBlock", "RUFFLE_PERIOD"): raven["ruffle_period"],
                ("RavenPerchBlock", "RUFFLE_TICKS"): raven["ruffle_ticks"], ("BlackCatBlock", "REACH"): cat["reach"],
                ("BlackCatBlock", "HISS_TICKS"): cat["hiss_ticks"], ("BlackCatBlock", "COOLDOWN_TICKS"): cat["cooldown_ticks"],
                ("BlackCatBlock", "SWISH_PERIOD"): cat["swish_period"], ("BlackCatBlock", "SWISH_DEGREES"): cat["swish_degrees"]}
    for (source, name), value in expected.items():
        if number(source, name) is None or abs(number(source, name) - value) > 1e-9:
            err(f"{source}.{name} = {number(source, name)} differs from tools/agriculture.py ({value})")
    specimens = re.search(r"enum Specimen implements StringRepresentable \{\s*([A-Z_, ]+);", java.get("SpecimenJarBlock", ""))
    if not specimens or [v.strip().lower() for v in specimens.group(1).split(",")] != jar["specimens"]:
        err("SpecimenJarBlock.Specimen differs from SPECIMEN_JAR's specimens")

    def variants(block):
        return set((load(ASSETS / "blockstates" / f"{block}.json") or {}).get("variants", {}))
    horizontal = ("north", "east", "south", "west")
    booleans = ("false", "true")
    wanted = {
        coil["block"]: {f"active={a},enabled={e},facing={f},half={h}" for a in booleans for e in booleans for f in horizontal
                        for h in ("lower", "upper")},
        table["block"]: {f"facing={f},part={p},powered={b}" for f in horizontal for p in ("foot", "head") for b in booleans},
        jar["block"]: {f"specimen={s}" for s in jar["specimens"]},
        sarcophagus["block"]: {f"facing={f},half={h},open={o},powered={p}" for f in horizontal for h in ("lower", "upper") for o in booleans
                               for p in booleans},
        raven["block"]: {f"facing={f}" for f in horizontal},
        cat["block"]: {f"facing={f},hissing={h}" for f in horizontal for h in booleans},
    }
    for block, keys in wanted.items():
        if variants(block) != keys:
            err(f"{block}: blockstate variants differ from its properties")
    quads = load(ASSETS / "decor8_quads.json") or {}
    names = (["lab_table_legs", "lab_table_torso", "mummy_sarcophagus_lid", "mummy_sarcophagus_mummy", "mummy_sarcophagus_arms", "raven_body",
              "raven_head", "raven_left_wing", "raven_right_wing", "black_cat_tail", "black_cat_tail_up"]
             + [f"specimen_{s}" for s in jar["specimens"]])
    for name in names:
        if not quads.get(name):
            err(f"decor8_quads.json has no quads for {name}")
        for quad in quads.get(name, []):
            if not (ASSETS / "textures" / "block" / f"{quad['texture']}.png").exists():
                err(f"decor8_quads.json: {name} uses a missing texture {quad['texture']}")
                break
    for texture in ("tesla_coil_arc", "specimen_jar_bubble", "black_cat_eyes"):
        if not (ASSETS / "textures" / "entity" / f"{texture}.png").exists():
            err(f"Missing entity texture {texture}")
    lang = load(ASSETS / "lang" / "en_us.json") or {}
    for key in [f"tesla_coil.{k}" for k in ("on", "off")] + [f"specimen_jar.{s}" for s in jar["specimens"]]:
        if f"message.{MOD}.{key}" not in lang:
            err(f"Message {key} has no text")


def check_decor9(java):
    """The yard and porch: Java matches tools/agriculture.py (the inflatables' filling, light and wobble, the witch's
    reach, cackle, rest and stirring, the hands' grab and rest, the chimes' swing and clacking, the vanes' turning, the
    archway's and tree's sizes and light), the designs, poses and words agree, every block state has a model, the
    block models turn only by 22.5 or 45 degrees, and the quads and textures the client draws exist."""
    def number(source, name):
        match = re.search(rf"\b{name} = ([\d.]+)[FLD]?;", java.get(source, ""))
        return float(match.group(1)) if match else None

    inf, witch, hands, chimes, vanes = ag.INFLATABLES, ag.PORCH_WITCH, ag.GRASPING_HANDS, ag.WIND_CHIMES, ag.WEATHERVANES
    arch, tree = ag.HAUNTED_ARCHWAY, ag.DEAD_TREE
    expected = {("InflatableBlock", "INFLATE_TICKS"): inf["inflate_ticks"], ("InflatableBlock", "DEFLATE_TICKS"): inf["deflate_ticks"],
                ("InflatableBlock", "LIGHT"): inf["light"], ("InflatableBlock", "WOBBLE_DEGREES"): inf["wobble_degrees"],
                ("InflatableBlock", "WOBBLE_PERIOD"): inf["wobble_period"],
                ("PorchWitchBlock", "REACH"): witch["reach"], ("PorchWitchBlock", "CACKLE_TICKS"): witch["cackle_ticks"],
                ("PorchWitchBlock", "COOLDOWN_TICKS"): witch["cooldown_ticks"], ("PorchWitchBlock", "STIR_PERIOD"): witch["stir_period"],
                ("PorchWitchBlock", "FAST_STIR_PERIOD"): witch["fast_stir_period"], ("PorchWitchBlock", "WATCH_RANGE"): witch["watch_range"],
                ("GraspingHandsBlock", "SLOW_TICKS"): hands["slow_ticks"], ("GraspingHandsBlock", "SLOWNESS_LEVEL"): hands["slowness_level"],
                ("GraspingHandsBlock", "GRAB_TICKS"): hands["grab_ticks"], ("GraspingHandsBlock", "REST_TICKS"): hands["rest_ticks"],
                ("BoneWindChimesBlock", "BONES"): chimes["bones"], ("BoneWindChimesBlock", "CALM_SWING"): chimes["calm_swing"],
                ("BoneWindChimesBlock", "RAIN_SWING"): chimes["rain_swing"], ("BoneWindChimesBlock", "STORM_SWING"): chimes["storm_swing"],
                ("BoneWindChimesBlock", "CALM_CHANCE"): chimes["calm_chance"], ("BoneWindChimesBlock", "RAIN_CHANCE"): chimes["rain_chance"],
                ("BoneWindChimesBlock", "STORM_CHANCE"): chimes["storm_chance"], ("WeathervaneBlock", "TURN_SPEED"): vanes["turn_speed"],
                ("HauntedArchwayBlock", "WIDTH"): arch["width"], ("HauntedArchwayBlock", "HEIGHT"): arch["height"],
                ("HauntedArchwayBlock", "LIGHT"): arch["light"], ("DeadHollowTreeBlock", "HEIGHT"): tree["height"],
                ("DeadHollowTreeBlock", "LIGHT"): tree["light"]}
    for (source, name), value in expected.items():
        if number(source, name) is None or abs(number(source, name) - value) > 1e-9:
            err(f"{source}.{name} = {number(source, name)} differs from tools/agriculture.py ({value})")
    main = java.get("JugcraftAgriculture", "")
    for name, designs in (("INFLATABLE_DESIGNS", inf["designs"]), ("WEATHERVANE_DESIGNS", vanes["designs"])):
        match = re.search(rf"{name} = List\.of\(([^)]*)\);", main)
        if not match or re.findall(r'"([a-z_]+)"', match.group(1)) != designs:
            err(f"JugcraftAgriculture.{name} differs from tools/agriculture.py")
    for source, enum, values in (("PoseableSkeletonBlock", "Pose", ag.POSEABLE_SKELETON["poses"]), ("SpookySignBlock", "Words", ag.SPOOKY_SIGN["words"]),
                                 ("GraspingHandsBlock", "Phase", ["rest", "grab", "recover"])):
        match = re.search(rf"enum {enum} implements StringRepresentable \{{\s*([A-Z_, ]+);", java.get(source, ""))
        if not match or [v.strip().lower() for v in match.group(1).split(",")] != values:
            err(f"{source}.{enum} differs from tools/agriculture.py")
    if number("GravestoneBlockEntity", "MAX_LENGTH") != ag.SPOOKY_SIGN["max_length"]:
        err("SPOOKY_SIGN's max_length differs from GravestoneBlockEntity.MAX_LENGTH")

    def variants(block):
        return set((load(ASSETS / "blockstates" / f"{block}.json") or {}).get("variants", {}))
    horizontal = ("north", "east", "south", "west")
    booleans = ("false", "true")
    halves = ("lower", "upper")
    wanted = {ag.PORCH_WITCH["block"]: {f"cackling={c},facing={f},half={h}" for c in booleans for f in horizontal for h in halves},
              ag.GRASPING_HANDS["block"]: {f"facing={f},phase={p}" for f in horizontal for p in ("rest", "grab", "recover")},
              ag.POSEABLE_SKELETON["block"]: {f"facing={f},half={h},pose={p}" for f in horizontal for h in halves for p in ag.POSEABLE_SKELETON["poses"]},
              ag.WIND_CHIMES["block"]: {""},
              ag.SPOOKY_SIGN["block"]: {f"facing={f},words={w}" for f in horizontal for w in ag.SPOOKY_SIGN["words"]},
              arch["block"]: {f"facing={f},lit={l},part={p}" for f in horizontal for l in booleans for p in range(7)},
              tree["block"]: {f"facing={f},lit={l},part={p}" for f in horizontal for l in booleans for p in range(tree["height"])}}
    for design in inf["designs"]:
        wanted[ag.inflatable(design)] = {f"facing={f},half={h},on={o},powered={p}" for f in horizontal for h in halves for o in booleans for p in booleans}
    for design in vanes["designs"]:
        wanted[ag.weathervane(design)] = {""}
    for block, keys in wanted.items():
        if variants(block) != keys:
            err(f"{block}: blockstate variants differ from its properties")
        for variant in ((load(ASSETS / "blockstates" / f"{block}.json") or {}).get("variants", {})).values():
            model = load(ASSETS / "models" / f"{variant['model'].split(':')[1]}.json") or {}
            for element in model.get("elements", []):
                angle = element.get("rotation", {}).get("angle", 0)
                if angle not in (-45, -22.5, 0, 22.5, 45):
                    err(f"{variant['model']}: a box turns by {angle} degrees; block models only turn by 22.5 or 45")
    quads = load(ASSETS / "decor9_quads.json") or {}
    names = ([f"inflatable_{d}" for d in inf["designs"]] + ["porch_witch_arm", "porch_witch_head", "wind_chime_bone", "wind_chime_skull"]
             + [f"weathervane_{d}" for d in vanes["designs"]])
    for name in names:
        if not quads.get(name):
            err(f"decor9_quads.json has no quads for {name}")
        for quad in quads.get(name, []):
            if not (ASSETS / "textures" / "block" / f"{quad['texture']}.png").exists():
                err(f"decor9_quads.json: {name} uses a missing texture {quad['texture']}")
                break
    if not (ASSETS / "textures" / "entity" / "porch_witch_eyes.png").exists():
        err("Missing entity texture porch_witch_eyes")
    lang = load(ASSETS / "lang" / "en_us.json") or {}
    for key in [f"spooky_sign.{w}" for w in ag.SPOOKY_SIGN["words"]] + ["spooky_sign.painted", "spooky_sign.unnamed_tag"]:
        if f"message.{MOD}.{key}" not in lang:
            err(f"Message {key} has no text")


def check_decor10(java):
    """Lighting and glow: Java matches tools/agriculture.py (the black light's light and range, the brazier's light and
    flames, the lamp's light, turning and reach, the pumpkins' and hat's light, the hat's bob and turning), the glow
    paint's designs agree, every block state has a model, and the textures and quads the client draws exist."""
    def number(source, name):
        match = re.search(rf"\b{name} = ([\d.]+)[FLD]?;", java.get(source, ""))
        return float(match.group(1)) if match else None

    light, paint, brazier, lamp, pumpkins, hat = ag.BLACK_LIGHT, ag.GLOW_PAINT, ag.BRAZIER, ag.SHADOW_LAMP, ag.MINI_PUMPKINS, ag.FLOATING_HAT
    expected = {("BlackLightBlock", "LIGHT"): light["light"], ("BlackLightBlock", "RANGE"): light["range"],
                ("WitchFireBrazierBlock", "LIGHT"): brazier["light"], ("ShadowPuppetLampBlock", "LIGHT"): lamp["light"],
                ("ShadowPuppetLampBlock", "TURN_TICKS"): lamp["turn_ticks"], ("ShadowPuppetLampBlock", "RANGE"): lamp["range"],
                ("MiniPumpkinStackBlock", "LIGHT"): pumpkins["light"], ("FloatingWitchHatBlock", "LIGHT"): hat["light"],
                ("FloatingWitchHatBlock", "BOB"): hat["bob"], ("FloatingWitchHatBlock", "BOB_TICKS"): hat["bob_ticks"],
                ("FloatingWitchHatBlock", "TURN_TICKS"): hat["turn_ticks"]}
    for (source, name), value in expected.items():
        if number(source, name) is None or abs(number(source, name) - value) > 1e-9:
            err(f"{source}.{name} = {number(source, name)} differs from tools/agriculture.py ({value})")
    designs = re.search(r"enum Design implements StringRepresentable \{\s*([A-Z_, ]+);", java.get("GlowPaintBlock", ""))
    if not designs or [v.strip().lower() for v in designs.group(1).split(",")] != paint["designs"]:
        err("GlowPaintBlock.Design differs from GLOW_PAINT's designs")
    flames = re.findall(r"\b([A-Z]+)\(0x[0-9A-F]+, DyeColor\.([A-Z_]+)\)", java.get("WitchFireBrazierBlock", ""))
    if [(f.lower(), f"{d.lower()}_dye") for f, d in flames] != list(brazier["flames"].items()):
        err("WitchFireBrazierBlock.Flame differs from BRAZIER's flames and dyes")

    def variants(block):
        return set((load(ASSETS / "blockstates" / f"{block}.json") or {}).get("variants", {}))
    horizontal = ("north", "east", "south", "west")
    booleans = ("false", "true")
    wanted = {light["block"]: {f"facing={f},lit={l},powered={p}" for f in horizontal for l in booleans for p in booleans},
              paint["block"]: {f"design={d},facing={f}" for d in paint["designs"] for f in horizontal + ("up", "down")},
              brazier["block"]: {f"flame={c},lit={l}" for c in brazier["flames"] for l in booleans},
              lamp["block"]: {f"lit={l}" for l in booleans},
              pumpkins["block"]: {f"facing={f},lit={l}" for f in horizontal for l in booleans},
              hat["block"]: {f"lit={l}" for l in booleans}}
    for block, keys in wanted.items():
        if variants(block) != keys:
            err(f"{block}: blockstate variants differ from its properties")
    entity = ASSETS / "textures" / "entity"
    for texture in ([f"glow_paint_{d}_glow" for d in paint["designs"]] + ["witch_fire_flame"]
                    + [f"shadow_puppet_lamp_{d}_shadow" for d in ("bat", "cat", "witch")]):
        if not (entity / f"{texture}.png").exists():
            err(f"Missing entity texture {texture}")
    for design in ("bat", "cat", "witch"):
        if not (ASSETS / "textures" / "block" / f"shadow_puppet_lamp_paper_{design}.png").exists():
            err(f"Missing lamp paper {design}")
    quads = load(ASSETS / "decor10_quads.json") or {}
    for name in ("floating_witch_hat", "floating_witch_hat_flame"):
        if not quads.get(name):
            err(f"decor10_quads.json has no quads for {name}")


def check_decor11(java):
    """Party games: Java matches tools/agriculture.py (the trap's reach and timing, the contest's round, range and
    entries, the bowling pumpkin's roll and the lane's reach, the dance floor's reach and light, ghost tag's round,
    range, players and tag-backs, the fortune table's fortunes, cards and cooldown), every block state has a model, every
    message the games show has its words (each fortune, each vote, each candy cache answer), and the textures and quads
    the client draws exist."""
    def number(source, name):
        match = re.search(rf"\b{name} = ([\d.]+)[FLD]?;", java.get(source, ""))
        return float(match.group(1)) if match else None

    scare, contest, bowling, dance, tag, fortune = ag.JUMP_SCARE, ag.COSTUME_CONTEST, ag.BOWLING, ag.DANCE_FLOOR, ag.GHOST_TAG, ag.FORTUNE_TABLE
    expected = {("JumpScareTrapBlock", "REACH"): scare["reach"], ("JumpScareTrapBlock", "POP_TICKS"): scare["pop_ticks"],
                ("JumpScareTrapBlock", "RESET_TICKS"): scare["reset_ticks"], ("JudgesTableBlockEntity", "ROUND_TICKS"): contest["round_ticks"],
                ("JudgesTableBlockEntity", "RANGE"): contest["range"], ("JudgesTableBlockEntity", "MAX_CONTESTANTS"): contest["max_contestants"],
                ("BowlingPumpkin", "SPEED"): bowling["speed"], ("BowlingPumpkin", "FRICTION"): bowling["friction"],
                ("BowlingPumpkin", "DOMINO_CHANCE"): bowling["domino_chance"], ("BowlingScoreboardBlock", "LANE_REACH"): bowling["lane_reach"],
                ("BowlingScore", "FRAMES"): bowling["frames"], ("DanceFloorBlock", "REACH"): dance["reach"], ("DanceFloorBlock", "LIGHT"): dance["light"],
                ("GhostBellBlockEntity", "ROUND_TICKS"): tag["round_ticks"], ("GhostBellBlockEntity", "RANGE"): tag["range"],
                ("GhostBellBlockEntity", "TAG_BACK_TICKS"): tag["tag_back_ticks"], ("GhostBellBlockEntity", "MAX_PLAYERS"): tag["max_players"],
                ("FortuneTellerTableBlock", "FORTUNES"): fortune["fortunes"], ("FortuneTellerTableBlock", "CARDS"): fortune["cards"],
                ("FortuneTellerTableBlock", "COOLDOWN_TICKS"): fortune["cooldown_ticks"]}
    for (source, name), value in expected.items():
        if number(source, name) is None or abs(number(source, name) - value) > 1e-9:
            err(f"{source}.{name} = {number(source, name)} differs from tools/agriculture.py ({value})")
    if "extends CandyBowlBlock" not in java.get("CandyCacheBlock", "") or "cache).build()" not in java.get("JugcraftAgriculture", ""):
        err("The Candy Cache must be a Candy Bowl with the same block entity (no second candy store)")

    def variants(block):
        return set((load(ASSETS / "blockstates" / f"{block}.json") or {}).get("variants", {}))
    horizontal = ("north", "east", "south", "west")
    booleans = ("false", "true")
    wanted = {scare["block"]: {f"facing={f},phase={p},powered={w}" for f in horizontal for p in ("ready", "popped", "resetting") for w in booleans},
              contest["runway"]: {"axis=x", "axis=z"},
              contest["table"]: {f"facing={f},open={o}" for f in horizontal for o in booleans},
              bowling["pin"]: {f"down={d},facing={f}" for d in booleans for f in horizontal},
              bowling["scoreboard"]: {f"facing={f}" for f in horizontal},
              ag.CANDY_CACHE["block"]: {f"facing={f},fill={n}" for f in horizontal for n in range(4)},
              dance["block"]: {f"distance={d}" for d in range(dance["reach"] + 1)},
              tag["block"]: {f"facing={f},ringing={r}" for f in horizontal for r in booleans},
              fortune["block"]: {f"facing={f}" for f in horizontal}}
    for block, keys in wanted.items():
        if variants(block) != keys:
            err(f"{block}: blockstate variants differ from its properties")

    lang = load(ASSETS / "lang" / "en_us.json") or {}
    votes = re.search(r"enum Vote \{\s*([A-Z_, ]+?)\s*\}", java.get("JudgesTableBlockEntity", ""))
    taken = re.search(r"enum Taken \{\s*([A-Z_, ]+?)\s*\}", java.get("CandyBowlBlockEntity", ""))
    keys = [f"message.jugcraft.fortune.{n}" for n in range(1, fortune["fortunes"] + 1)]
    keys += [f"message.jugcraft.judges_table.{v.strip().lower()}" for v in (votes.group(1).split(",") if votes else [])]
    keys += [f"message.jugcraft.candy_cache.{t.strip().lower()}" for t in (taken.group(1).split(",") if taken else [])]
    keys += [f"message.jugcraft.candy_cache.{k}" for k in ("filled", "full", "count")]
    for source in ("JudgesTableBlockEntity", "GhostBellBlockEntity", "BowlingScoreboardBlock"):
        keys += re.findall(r'"(message\.jugcraft\.[a-z_.]+[a-z_])"', java.get(source, ""))
    if not votes or not taken:
        err("JudgesTableBlockEntity.Vote or CandyBowlBlockEntity.Taken not found")
    for key in keys:
        if key not in lang:
            err(f"Missing words for {key}")
    for key in ("frame", "score", "game_over", "ready"):
        if f"message.jugcraft.bowling_scoreboard.{key}" not in lang:
            err(f"Missing words for the scoreboard's {key}")

    if not (ASSETS / "textures" / "entity" / "dance_floor_glow.png").exists():
        err("Missing entity texture dance_floor_glow")
    for card in range(fortune["cards"]):
        if not (ASSETS / "textures" / "block" / f"fortune_card_{card}.png").exists():
            err(f"Missing tarot card {card}")
    quads = load(ASSETS / "decor11_quads.json") or {}
    for name in ("jump_scare_lid", "jump_scare_ghost", "jump_scare_spring", "bowling_pumpkin", "ghost_bell", "ghost_bell_clapper",
                 "fortune_planchette"):
        if not quads.get(name):
            err(f"decor11_quads.json has no quads for {name}")


def check_decor12(java):
    """Night events: Java matches tools/agriculture.py (the trick-or-treaters' timing, chances, group sizes, distances,
    prank and costumes and their gift table, the toilet paper's reach and strands, the hayride's seats and spooks, the
    bonfire's light, skewers and speed, and toasting's reach and times), every block state has a model, the gift table
    holds the gifts, every message has its words, and the textures and quads the client draws exist."""
    def number(source, name):
        match = re.search(rf"\b{name} = ([\d.]+)[FLD]?;", java.get(source, ""))
        return float(match.group(1)) if match else None

    kids, paper, ride, fire = ag.TRICK_OR_TREATERS, ag.TOILET_PAPER, ag.HAYRIDE, ag.BONFIRE
    expected = {("TrickOrTreaters", "CHECK_TICKS"): kids["check_ticks"], ("TrickOrTreaters", "CHANCE"): kids["chance"],
                ("TrickOrTreaters", "MAX_GROUPS"): kids["max_groups"], ("TrickOrTreaters", "MIN_KIDS"): kids["kids"][0],
                ("TrickOrTreaters", "MAX_KIDS"): kids["kids"][1], ("TrickOrTreaters", "MIN_DISTANCE"): kids["spawn_distance"][0],
                ("TrickOrTreaters", "MAX_DISTANCE"): kids["spawn_distance"][1], ("TrickOrTreaters", "PLAYER_RANGE"): kids["player_range"],
                ("TrickOrTreaters", "GIVE_UP_TICKS"): kids["give_up_ticks"], ("TrickOrTreaters", "WAIT_TICKS"): kids["wait_ticks"],
                ("TrickOrTreaters", "LEAVE_TICKS"): kids["leave_ticks"], ("TrickOrTreaters", "PRANK_REACH"): kids["prank_reach"],
                ("TrickOrTreaters", "STREAMERS"): kids["streamers"], ("ToiletPaperRoll", "STREAMERS"): paper["streamers"],
                ("ToiletPaperRoll", "REACH"): paper["reach"], ("ToiletPaperStreamerBlock", "MAX_LENGTH"): paper["max_length"],
                ("HauntedHayride", "SEATS"): ride["seats"], ("HauntedHayride", "SPOOK_MIN"): ride["spook_ticks"][0],
                ("HauntedHayride", "SPOOK_MAX"): ride["spook_ticks"][1], ("HalloweenBonfireBlock", "LIGHT"): fire["light"],
                ("HalloweenBonfireBlockEntity", "SLOTS"): fire["slots"], ("HalloweenBonfireBlockEntity", "SPEED"): fire["speed"],
                ("MarshmallowStickItem", "BONFIRE_REACH"): fire["reach"], ("MarshmallowStickItem", "CAMPFIRE_REACH"): fire["campfire_reach"],
                ("MarshmallowStickItem", "TOAST_TICKS"): fire["toast_ticks"], ("MarshmallowStickItem", "BURN_TICKS"): fire["burn_ticks"]}
    for (source, name), value in expected.items():
        if number(source, name) is None or abs(number(source, name) - value) > 1e-9:
            err(f"{source}.{name} = {number(source, name)} differs from tools/agriculture.py ({value})")
    source = java.get("TrickOrTreaters", "")
    if f'GIFT_TABLE = "{kids["gift_table"]}"' not in source:
        err("TrickOrTreaters.GIFT_TABLE differs from TRICK_OR_TREATERS' gift table")
    costumes = re.search(r"COSTUMES = List\.of\(([^)]*)\)", source)
    if not costumes or re.findall(r'"([a-z_:]+)"', costumes.group(1)) != kids["costumes"]:
        err("TrickOrTreaters.COSTUMES differs from TRICK_OR_TREATERS' costumes")

    def variants(block):
        return set((load(ASSETS / "blockstates" / f"{block}.json") or {}).get("variants", {}))
    if variants(paper["block"]) != {"draped=false", "draped=true"}:
        err(f"{paper['block']}: blockstate variants differ from its properties")
    if variants(fire["block"]) != {"lit=false", "lit=true"}:
        err(f"{fire['block']}: blockstate variants differ from its properties")

    table = load(DATA / "jugcraft" / "loot_table" / f"{kids['gift_table']}.json") or {}
    gifts = [(entry.get("name"), entry.get("weight")) for pool in table.get("pools", []) for entry in pool.get("entries", [])]
    if gifts != [tuple(gift) for gift in kids["gifts"]]:
        err(f"The trick-or-treaters' gift table differs from TRICK_OR_TREATERS' gifts: {gifts}")

    lang = load(ASSETS / "lang" / "en_us.json") or {}
    for source in ("TrickOrTreaters", "MarshmallowStickItem"):
        for key in re.findall(r'"(message\.jugcraft\.[a-z_.]+[a-z_])"', java.get(source, "")):
            if key not in lang:
                err(f"Missing words for {key}")
    if not (ASSETS / "textures" / "entity" / "bonfire_flame.png").exists():
        err("Missing entity texture bonfire_flame")
    quads = load(ASSETS / "decor12_quads.json") or {}
    for name in ("hayride_wagon", "hayride_lantern"):
        if not quads.get(name):
            err(f"decor12_quads.json has no quads for {name}")


def check_model_uvs():
    """Minecraft 26.3 refuses to bake a block model face that reads outside its texture when the texture has
    see-through pixels ("Cannot compute translucency out of bounds"). Faces without a "uv" take theirs from the
    element's position, so a box reaching outside the block must pin its UVs."""
    def default_uv(side, lo, hi):
        fx, fy, fz = lo
        tx, ty, tz = hi
        return {"down": (fx, 16 - tz, tx, 16 - fz), "up": (fx, fz, tx, tz), "north": (16 - tx, 16 - ty, 16 - fx, 16 - fy),
                "south": (fx, 16 - ty, tx, 16 - fy), "west": (fz, 16 - ty, tz, 16 - fy), "east": (16 - tz, 16 - ty, 16 - fz, 16 - fy)}[side]

    see_through = {}

    def translucent(texture):
        if texture not in see_through:
            namespace, path = split(texture) if ":" in texture else (MOD, texture)
            image = ASSETS.parent / namespace / "textures" / f"{path}.png"
            see_through[texture] = image.exists() and Image.open(image).convert("RGBA").getextrema()[3][0] < 255
        return see_through[texture]

    for path in sorted((ASSETS / "models" / "block").glob("*.json")):
        model = load(path) or {}
        textures = model.get("textures", {})
        for element in model.get("elements", []):
            for side, face in element.get("faces", {}).items():
                texture = face.get("texture", "")
                for _ in range(4):
                    if texture.startswith("#"):
                        texture = textures.get(texture[1:], "")
                if not texture or not translucent(texture):
                    continue
                uv = face.get("uv") or default_uv(side, element["from"], element["to"])
                if min(uv) < 0 or max(uv) > 16:
                    err(f"{path.relative_to(ROOT)}: the {side} face reads {list(uv)} outside its see-through texture {texture}; pin its uv")


def check_deposits():
    """Surface deposits: Java registration, worldgen and capacity match tools/deposits.py."""
    java = (JAVA_ROOT / "deposit" / "JugcraftDeposits.java").read_text(encoding="utf-8")
    found = dict(re.findall(r'DEPOSITS\.put\("([a-z_]+)", [^;]*?"([a-z_]+)"\)\);', java))
    expected = {name: info["yield"].split(":")[1] for name, info in deposits.DEPOSITS.items()}
    if found != expected:
        err(f"JugcraftDeposits.java deposits {found} != tools/deposits.py {expected}")
    if f"CAPACITY = {deposits.CAPACITY:_}" not in (JAVA_ROOT / "deposit" / "Deposits.java").read_text(encoding="utf-8"):
        err(f"Deposits.CAPACITY is not {deposits.CAPACITY:_} as in tools/deposits.py")
    worldgen = WORLDGEN.read_text(encoding="utf-8")
    for name, info in deposits.DEPOSITS.items():
        call = "addDeposit(stonyHills, " + ", ".join(f'"{v}"' for v in [name] + info["features"]) + ");"
        if call not in worldgen:
            err(f"JugcraftWorldgen does not add {name} with features {info['features']}")
        for feature in info["features"]:
            if feature not in FEATURES:
                err(f"{name}: unknown feature {feature}")
        if not (DATA / MOD / "worldgen" / "placed_feature" / f"{name}.json").is_file():
            err(f"{name}: no placed feature")
    for item in (info["yield"] for info in deposits.DEPOSITS.values()):
        if split(item)[0] == MOD and split(item)[1] not in all_items():
            err(f"Deposit yield {item} is not a Jugcraft item")
    stats = STATS["deposit_drill"]
    kinds = MACHINE_JAVA.read_text(encoding="utf-8")
    for key, constant in (("ticks", "DEPOSIT_TICKS"), ("units", "DEPOSIT_UNITS"), ("reach", "DEPOSIT_REACH"),
                          ("depth", "DEPOSIT_DEPTH")):
        if f"int {constant} = {stats[key]};" not in kinds:
            err(f"MachineKind.{constant} is not {stats[key]} as in tools/machines.py")


def main():
    registered = (set(all_blocks()) | set(all_items()) | set(machine_blocks()) | set(machine_items())
                  | set(ag.all_blocks()) | set(ag.all_items()) | set(petro.petro_items()) | set(petro.petro_blocks())
                  | set(deposits.DEPOSITS) | set(tank_display.BLOCKS))
    check_assets(sorted(registered))
    check_petro()
    check_loot(registered)
    check_recipes(registered)
    check_machine_recipe_files(registered)
    check_fluid_recipes(registered)
    check_tags()
    check_worldgen()
    check_java()
    check_deposits()
    check_machines(registered)
    check_large_machines()
    check_style_pack()
    check_handbook(registered)
    check_agriculture()
    check_recipe_categories()
    check_advancements(registered)
    check_model_uvs()
    for path in RES.rglob("*.json"):
        load(path)
    if errors:
        print("\n".join(errors), file=sys.stderr)
        sys.exit(1)
    print(f"PASS: {len(registered)} material IDs, data files and recipe audit. No Minecraft build or game test performed.")


if __name__ == "__main__":
    main()
