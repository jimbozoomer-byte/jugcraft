"""Offline checks for the woodland harvest crafts (tools/thanksgiving_woodland.py; docs/features/thanksgiving-woodland-crafts.md).
NOT a game test. tools/check_mod_data.py runs errors() (check_thanksgiving_woodland) and scripts/tests/
test_thanksgiving_woodland.py runs each part.

- resources(): every resource the group owns is on disk exactly as thanksgiving_woodland_data generates it (models,
  blockstates, items, loot, recipes, textures), so nothing was hand-edited;
- models(): each model lies in its block, each floor piece's outline covers its model, each blockstate covers every state
  (facing and water; the mat's axis and four joins, by multipart), and art_check's block-model rules (Z1, N1, U1, O1,
  I1) find nothing on the group's models;
- data(): each piece drops itself once, its recipe follows the agriculture switch only (no research, no calendar) and
  takes reachable items (vanilla, a vanilla tag, or a registered jugcraft item);
- java(): WoodlandHarvestCrafts registers every piece as its kind's block with the table's shape, map colour, sound,
  strength and flammability; WoodlandCraftBlock takes water and needs its support; the mat joins only rush mats;
- hooks(): the shared hooks are in place: the agriculture pipeline writes the group (agriculture_data, crop_textures,
  agriculture.SHAPELESS), JugcraftAgriculture registers it with its items, the names and JEI pages are in en_us.json,
  each piece is in its mineable tag (the only data tag its behaviour relies on), JEI shows its page, and the game tests
  are listed in the gametest fabric.mod.json.
"""
import json
import re
from pathlib import Path

import thanksgiving_woodland as tw
import thanksgiving_woodland_data as twd
from generated_resource_compare import matches

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "src" / "main" / "resources"
ASSETS = RES / "assets" / "jugcraft"
DATA = RES / "data"
JAVA = ROOT / "src" / "main" / "java" / "io" / "github" / "jimbozoomer" / "jugcraft" / "agriculture"
MOD = "jugcraft"
FACINGS = ("north", "east", "south", "west")


def _load(path):
    return json.loads(path.read_text(encoding="utf-8")) if path.is_file() else None


def resources():
    errors = []
    for rel, data in twd.outputs().items():
        path = RES / rel
        if not path.is_file():
            errors.append(f"{rel} is missing: run tools/thanksgiving_woodland_data.py")
        elif not matches(path, data):
            errors.append(f"{rel} differs from what tools/thanksgiving_woodland_data.py generates")
    return errors


def _art_rules(path):
    """art_check's block-model rules (Z1, N1, U1, O1, I1) on one model file: its findings."""
    import art_check as ac
    model = _load(path)
    elements, lookup, _ = ac._resolve(model, (ASSETS,))
    if not elements:
        return []
    out = []
    name = path.relative_to(RES).as_posix()
    backwards = [i for i, el in enumerate(elements) if any(el["from"][k] > el["to"][k] + 1e-6 for k in range(3))]
    if backwards:
        out.append(f"I1 {name}: element {backwards[0]} runs backwards")
    faces, raw = ac._model_faces(elements, lookup)
    if any(min(uv) < -1e-3 or max(uv) > 16 + 1e-3 for uv in raw):
        out.append(f"U1 {name}: a face reads outside its sprite")
    for i, j, gap, overlap, u, v, n, d in ac._pairs(faces, ac.N1_GAP):
        if gap == 0 and ac._layered(elements, faces[i], faces[j]):
            continue
        if ac._differ(faces[i], faces[j], overlap, u, v, n, d):
            out.append(f"{'Z1' if gap == 0 else 'N1'} {name}: element {faces[i].label} / {faces[j].label} share a plane")
            break
    if ac._open_faces(elements, faces):
        out.append(f"O1 {name}: a left-out face of an opaque box is exposed")
    return out


def models():
    errors = []
    for block, spec in tw.PROPS.items():
        if spec["kind"] == "floor":
            sc = twd.build(block)
            solid = [e for e in sc.models["whole"] if all(e["to"][k] - e["from"][k] > 1e-6 for k in range(3))]
            x0, y0, z0, x1, y1, z1 = twd.bounds(solid)
            if x0 < -1e-6 or z0 < -1e-6 or y0 < -1e-6 or x1 > 16 + 1e-6 or y1 > 16 + 1e-6 or z1 > 16 + 1e-6:
                errors.append(f"{block}: its model must lie in its block, not {[round(v, 2) for v in (x0, y0, z0, x1, y1, z1)]}")
            sx0, sy0, sz0, sx1, sy1, sz1 = spec["shape"]
            if x0 < sx0 - 0.5 or z0 < sz0 - 0.5 or y0 < sy0 - 0.5 or x1 > sx1 + 0.5 or y1 > sy1 + 0.5 or z1 > sz1 + 0.5:
                errors.append(f"{block}: its shape {spec['shape']} must cover its model {[round(v, 2) for v in (x0, y0, z0, x1, y1, z1)]}")
            variants = (_load(ASSETS / "blockstates" / f"{block}.json") or {}).get("variants", {})
            for f in FACINGS:
                for w in ("false", "true"):
                    if variants.get(f"facing={f},waterlogged={w}", {}).get("model") != f"{MOD}:block/{block}":
                        errors.append(f"blockstates/{block}.json: facing={f},waterlogged={w} must draw block/{block}")
            names = [block]
            if sc.atlas.size not in (64, 128):
                errors.append(f"{block}: its texture must be 64 or 128 texels square")
        else:
            state = _load(ASSETS / "blockstates" / f"{block}.json") or {}
            if state != twd.mat_blockstate():
                errors.append(f"blockstates/{block}.json must be thanksgiving_woodland_data.mat_blockstate()")
            parts = state.get("multipart", [])
            # Every state draws its middle and exactly one model for each side and corner.
            for axis in ("x", "z"):
                for bits in range(16):
                    joins = {side: bool(bits >> n & 1) for n, side in enumerate(FACINGS)}

                    def holds(cond):
                        if "OR" in cond:
                            return any(holds(c) for c in cond["OR"])
                        return all((v == axis) if k == "axis" else (v == str(joins[k]).lower()) for k, v in cond.items())
                    drawn = [p["apply"]["model"] for p in parts if holds(p["when"])]
                    if len(drawn) != 9 or len(set(drawn)) != 9:
                        errors.append(f"{block}: axis={axis} {joins} must draw 9 distinct parts, not {drawn}")
            names = list(twd.mat_models())
        for name in names:
            path = ASSETS / "models" / "block" / f"{name}.json"
            if not path.is_file():
                errors.append(f"models/block/{name}.json is missing")
                continue
            errors += _art_rules(path)
        errors += _art_rules(ASSETS / "models" / "item" / f"{block}.json") if (ASSETS / "models" / "item" / f"{block}.json").is_file() else [f"models/item/{block}.json is missing"]
    for name in list(twd.BUILDERS) + list(twd.MAT_TEXTURES.values()):
        path = ASSETS / "textures" / "block" / f"{name}.png"
        if not path.is_file():
            errors.append(f"textures/block/{name}.png is missing")
    # Short palettes: three to five tones a material (docs/ART_DIRECTION.md).
    for name, value in vars(twd).items():
        if name.isupper() and isinstance(value, tuple) and value and isinstance(value[0], tuple) and len(value[0]) == 3:
            if not 2 <= len(value) <= 5:
                errors.append(f"tools/thanksgiving_woodland_data.py {name}: a material keeps three to five tones, not {len(value)}")
    return errors


def _known_items():
    lang = _load(ASSETS / "lang" / "en_us.json") or {}
    return {key.split(".", 2)[2] for key in lang if key.count(".") == 2 and key.split(".")[0] in ("item", "block") and key.split(".")[1] == MOD}


def data():
    errors = []
    known = _known_items()
    for block in tw.PROPS:
        pools = (_load(DATA / MOD / "loot_table" / "blocks" / f"{block}.json") or {}).get("pools", [])
        if len(pools) != 1 or [e.get("name") for e in pools[0].get("entries", [])] != [f"{MOD}:{block}"] or pools[0].get("rolls") != 1:
            errors.append(f"loot_table/blocks/{block}.json: it drops itself, once")
        recipe = _load(DATA / MOD / "recipe" / f"{block}.json") or {}
        made = next(r for r in tw.SHAPELESS if r["id"] == block)
        if recipe.get("ingredients") != made["inputs"] or recipe.get("result", {}).get("id") != f"{MOD}:{block}":
            errors.append(f"recipe/{block}.json must be generated from tools/thanksgiving_woodland.py SHAPELESS")
        if recipe.get("fabric:load_conditions") != [{"condition": f"{MOD}:feature_enabled", "feature": tw.FEATURE}]:
            errors.append(f"recipe/{block}.json follows the agriculture switch and nothing else (no research, no calendar)")
        for ingredient in made["inputs"]:
            namespace, _, path = ingredient.lstrip("#").partition(":")
            if namespace == MOD and path not in known:
                errors.append(f"recipe/{block}.json: {ingredient} is not a registered Jugcraft item")
            elif namespace not in ("minecraft", MOD):
                errors.append(f"recipe/{block}.json: {ingredient} is not reachable")
    return errors


def java():
    errors = []
    text = (JAVA / "WoodlandHarvestCrafts.java").read_text(encoding="utf-8")
    for block, spec in tw.PROPS.items():
        factory = (f"props -> new {tw.KINDS[spec['kind']]}(props, {tw.java_box(spec['shape'])})" if spec["kind"] == "floor"
                   else f"{tw.KINDS[spec['kind']]}::new")
        name = "RUSH_FLOOR_MAT" if block == "rush_floor_mat" else f'"{block}"'
        props = f"properties(MapColor.{spec['color']}, SoundType.{spec['sound']}, {spec['strength']}F)"
        call = f"blocks.register({name}, {factory},"
        start = text.find(call)
        if start < 0:
            errors.append(f"WoodlandHarvestCrafts.register: {block} must be registered as {call}... (tools/thanksgiving_woodland.py)")
            continue
        line = text[start:text.find(";", start)]
        if props not in line:
            errors.append(f"WoodlandHarvestCrafts.register: {block} must use {props}")
        if spec["burns"] != (".ignitedByLava()" in line):
            errors.append(f"WoodlandHarvestCrafts.register: {block} {'burns' if spec['burns'] else 'does not burn'} (PROPS)")
    floor = re.search(r"FLOOR = List\.of\(([^)]*)\)", text)
    if not floor or re.findall(r'"([a-z_]+)"', floor.group(1)) != tw.blocks("floor"):
        errors.append("WoodlandHarvestCrafts.FLOOR must list the floor pieces in the table's order")
    craft = (JAVA / "WoodlandCraftBlock.java").read_text(encoding="utf-8")
    for needed in ("extends FacingDecorationBlock implements SimpleWaterloggedBlock", "BlockStateProperties.WATERLOGGED",
                   "Block.canSupportCenter(level, pos.below(), Direction.UP)", "Fluids.WATER.getSource(false)"):
        if needed not in craft:
            errors.append(f"WoodlandCraftBlock: missing {needed}")
    mat = (JAVA / "WoodlandRushMatBlock.java").read_text(encoding="utf-8")
    for needed in ("BlockStateProperties.HORIZONTAL_AXIS", "Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0)", "return neighbor.is(this);",
                   "!level.isEmptyBlock(pos.below())"):
        if needed not in mat:
            errors.append(f"WoodlandRushMatBlock: missing {needed}")
    for source in (text, craft, mat):
        if "BlockEntity" in source or "Container" in source or "useItemOn" in source or "useWithoutItem" in source:
            errors.append("the woodland crafts are decoration only: no block entity, storage or use action")
    return errors


def hooks():
    errors = []
    lang = _load(ASSETS / "lang" / "en_us.json") or {}
    for key, text in tw.names().items():
        if lang.get(key) != text:
            errors.append(f"en_us.json: {key} must be {text!r} (tools/thanksgiving_woodland.py)")
    for block, text in tw.INFO.items():
        if "not a historical reconstruction" not in text:
            errors.append(f"tools/thanksgiving_woodland.py INFO {block}: say it is inspired game decor, not a historical reconstruction")
    tags = {tool: set((_load(DATA / "minecraft" / "tags" / "block" / "mineable" / f"{tool}.json") or {}).get("values", [])) for tool in tw.TOOLS}
    for block, spec in tw.PROPS.items():
        for tool in tw.TOOLS:
            if (f"{MOD}:{block}" in tags[tool]) != (tool == spec["tool"]):
                errors.append(f"tags/block/mineable/{tool}.json {'must' if tool == spec['tool'] else 'must not'} list {block}")
    sources = {
        "tools/agriculture_data.py": ("import thanksgiving_woodland_data", "thanksgiving_woodland_data.assets(root, write, lang)",
                                      "thanksgiving_woodland_data.loot(out, write)", "thanksgiving_woodland_data.tags(tags)"),
        "tools/crop_textures.py": ("thanksgiving_woodland_data import textures",),
        "tools/agriculture.py": ("import thanksgiving_woodland", "SHAPELESS += thanksgiving_woodland.SHAPELESS", "thanksgiving_woodland.blocks()",
                                 "thanksgiving_woodland.items()"),
        "src/main/java/io/github/jimbozoomer/jugcraft/agriculture/JugcraftAgriculture.java": (
            "WoodlandHarvestCrafts.register(JugcraftAgriculture::registerBlock)",),
        "src/client/java/io/github/jimbozoomer/jugcraft/client/compat/JugcraftJeiPlugin.java": ("WoodlandJeiInfo.addInformation(registration);",),
        "src/gametest/resources/fabric.mod.json": ("io.github.jimbozoomer.jugcraft.test.WoodlandHarvestCraftsGameTests",),
        "tools/check_mod_data.py": ("check_thanksgiving_woodland()",),
    }
    for rel, needles in sources.items():
        path = ROOT / rel
        text = path.read_text(encoding="utf-8") if path.is_file() else ""
        for needle in needles:
            if needle not in text:
                errors.append(f"{rel} must contain {needle!r} (shared hook for the woodland crafts)")
    manifest = set(_load(ROOT / "tools" / "data" / "generated-material-resources.json") or [])
    # Material generation owns JSON; crop_textures owns the PNGs. Claiming PNGs
    # here makes generate_material_data's stale-file pruning delete them.
    outputs = twd.outputs()
    missing = sorted(rel for rel in outputs if rel.endswith('.json') and rel not in manifest)
    if missing:
        errors.append(f"tools/data/generated-material-resources.json must list the group's {len(missing)} resources, e.g. {missing[0]}")
    wrongly_owned = sorted(rel for rel in outputs if rel.endswith('.png') and rel in manifest)
    if wrongly_owned:
        errors.append(f"material manifest must not own texture-generator PNGs: {wrongly_owned}")
    return errors


def errors():
    return resources() + models() + data() + java() + hooks()


if __name__ == "__main__":
    found = errors()
    for e in found:
        print(e)
    print(f"{len(found)} finding(s)")
    raise SystemExit(1 if found else 0)
