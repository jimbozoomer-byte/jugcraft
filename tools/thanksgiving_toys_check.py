"""Checks for the Thanksgiving festival toys (tools/thanksgiving_toys.py; docs/features/thanksgiving-toys.md).

problems(repo) lists everything wrong, as text, empty when all is well. tools/check_mod_data.py reports each as an
error (check_thanksgiving_toys) and scripts/tests/test_thanksgiving_toys.py asserts there are none. It checks that:
- the Java registers each piece as its kind's block with the table's outline (FestivalToys), and the interactive
  blocks' numbers are the table's (the top's steps and delays, the percussion set's cooldown, aim and sounds, the corn
  toss's bags, distances, cooldown, scatter, hole and board, the backdrop's cells, parts, bulbs, light and outline);
- JugcraftAgriculture calls FestivalToys.register, and the game tests are listed for the gametest entrypoint;
- every generated file on disk is exactly what tools/thanksgiving_toys_data.py makes now (models, blockstates, items,
  loot, recipes and textures), and listed in the generated-resource manifest;
- each piece drops itself once (the backdrop from its first block only), is mined quickest with an axe, has its name
  (and, if it does something, its hint) and its messages in en_us.json;
- every recipe is shapeless, follows the agriculture switch, needs no research and makes one of its piece;
- each single-block model lies in its block, on the floor, inside its outline; the backdrop leaves its middle open;
- the corn toss's hole and board in the table are where the slanted board's model puts them;
- the Corn Toss Set holds no items and gives nothing: no wager, prize, payout or reward in its code.
"""
import io
import json
import re
import sys
from pathlib import Path

MOD = "jugcraft"


def _read(path):
    return path.read_text(encoding="utf-8") if path.is_file() else ""


def _load(path):
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except (OSError, ValueError):
        return None


def problems(repo):
    repo = Path(repo)
    tools = repo / "tools"
    if str(tools) not in sys.path:
        sys.path.insert(0, str(tools))
    import thanksgiving_toys as tt
    import thanksgiving_toys_data as ttd

    out = []
    res = repo / "src" / "main" / "resources"
    assets, data = res / "assets" / MOD, res / "data" / MOD
    java = repo / "src" / "main" / "java" / "io" / "github" / "jimbozoomer" / "jugcraft" / "agriculture"
    reg = _read(java / "FestivalToys.java")

    # Registration and the Java's numbers.
    for block, spec in tt.PROPS.items():
        const = block.upper()
        if f'public static final String {const} = "{block}";' not in reg:
            out.append(f"FestivalToys.java must name {block} as {const}")
        if spec["kind"] == "backdrop":
            call = f"blocks.register({const}, FestivalToyBackdropBlock::new"
        else:
            call = f"blocks.register({const}, props -> new {spec['class']}(props, {tt.java_box(spec['shape'])})"
        if call not in reg:
            out.append(f"FestivalToys.register: {block} must be registered as {call}... (tools/thanksgiving_toys.py PROPS)")
        if f"MapColor.{spec['color']}" not in reg or f"SoundType.{spec['sound']}" not in reg:
            out.append(f"FestivalToys.register: {block} must use MapColor.{spec['color']} and SoundType.{spec['sound']}")
    for block in tt.HINTS:
        if block.upper() not in reg[reg.find("HINTED"):reg.find("HINTED") + 300]:
            out.append(f"FestivalToys.HINTED must list {block}")
    top = _read(java / "FestivalToySpinningTopBlock.java")
    for needed in (f"STEPS = {tt.TOP['spin']};", f"WOBBLE = {tt.TOP['wobble']};",
                   "return new int[] {" + ", ".join(str(tt.top_delay(n)) for n in range(1, tt.TOP["wobble"] + 1)) + "}[step - 1];"):
        if needed not in top:
            out.append(f"FestivalToySpinningTopBlock must have {needed} (tools/thanksgiving_toys.py TOP, top_delay)")
    thresholds = [(n, tt.top_delay(n)) for n in range(tt.TOP["wobble"] + 1, tt.TOP["spin"] + 1)]
    bounds = [(tt.TOP["wobble"] if b == "WOBBLE" else int(b), int(r))
              for b, r in re.findall(r"if \(step > (\w+)\) \{\s*return (\d+);", top) if b.isdigit() or b == "WOBBLE"]
    java_delay = {}
    for n in range(tt.TOP["wobble"] + 1, tt.TOP["spin"] + 1):
        java_delay[n] = next((r for b, r in bounds if n > b), None)
    if any(java_delay[n] != d for n, d in thresholds):
        out.append("FestivalToySpinningTopBlock.delay must step as tools/thanksgiving_toys.py top_delay does")
    perc = _read(java / "FestivalToyPercussionBlock.java")
    p = tt.PERCUSSION
    for needed in (f"COOLDOWN = {p['cooldown']};", f"SPLIT_X = {p['split_x']};", f"SHAKER_TOP = {p['shaker_top']};",
                   f"SHAKER_Z = {p['shaker_z']};", f"case DRUM -> SoundEvents.{p['instruments']['drum']}.value();",
                   f"case WASHBOARD -> SoundEvents.{p['instruments']['washboard']}.value();",
                   f"default -> SoundEvents.{p['instruments']['shaker']}.value();"):
        if needed not in perc:
            out.append(f"FestivalToyPercussionBlock must have {needed} (tools/thanksgiving_toys.py PERCUSSION)")
    toss = _read(java / "FestivalToyCornTossBlock.java")
    c = tt.CORN_TOSS
    for needed in (f"BAGS = {c['bags']};", f"HOLE_POINTS = {c['points']['hole']};", f"BOARD_POINTS = {c['points']['board']};",
                   f"COOLDOWN = {c['cooldown']};", f"MIN_DISTANCE = {c['min_distance']};", f"MAX_DISTANCE = {c['max_distance']};",
                   f"FRONT_COS = {c['front_cos']};", f"SCATTER = {c['scatter']};", f"HEAR = {c['hear']};",
                   f"HOLE_X = {c['hole'][0]};", f"HOLE_Z = {c['hole'][1]};", f"HOLE_R = {c['hole_r']};",
                   f"BOARD_X0 = {c['board'][0]};", f"BOARD_Z0 = {c['board'][1]};", f"BOARD_X1 = {c['board'][2]};",
                   f"BOARD_Z1 = {c['board'][3]};", f"AIM_HEIGHT = {c['aim_height']};"):
        if needed not in toss:
            out.append(f"FestivalToyCornTossBlock must have {needed} (tools/thanksgiving_toys.py CORN_TOSS)")
    score = _read(java / "FestivalToyCornTossBlockEntity.java")
    for name, text in (("FestivalToyCornTossBlock", toss), ("FestivalToyCornTossBlockEntity", score)):
        for banned in ("Container", "addItem", "spawnAtLocation", "Ledger", "FinanceBook", "Jugs", "LootTable", "ItemStack", "award"):
            if banned in text:
                out.append(f"{name} must not hold, give, pay or award anything ({banned})")
    back = _read(java / "FestivalToyBackdropBlock.java")
    b = tt.BACKDROP
    cells = "{" + ", ".join("{" + f"{r}, {u}" + "}" for r, u in b["cells"]) + "}"
    for needed in (f"CELLS = {cells};", f"PARTS = {len(b['cells'])};", f"BULBS_LEFT = {b['bulb_parts'][0]};",
                   f"BULBS_RIGHT = {b['bulb_parts'][1]};", f"LIGHT = {b['light']};", f"ACROSS = {b['across']};", f"UP = {b['up']};",
                   "LandRegistry.mayBuild(player, level, at)", "TownProtection.denies(player, level, at)"):
        if needed not in back:
            out.append(f"FestivalToyBackdropBlock must have {needed} (tools/thanksgiving_toys.py BACKDROP)")
    for box in b["boxes"]:
        if tt.java_box(box) not in back:
            out.append(f"FestivalToyBackdropBlock's outline must have the box {box} (tools/thanksgiving_toys.py BACKDROP boxes)")
    agriculture = _read(java / "JugcraftAgriculture.java")
    if "FestivalToys.register(JugcraftAgriculture::registerBlock" not in agriculture:
        out.append("JugcraftAgriculture must call FestivalToys.register(JugcraftAgriculture::registerBlock, ...)")
    gametest = _read(repo / "src" / "gametest" / "resources" / "fabric.mod.json")
    if "io.github.jimbozoomer.jugcraft.test.FestivalToyGameTests" not in gametest:
        out.append("src/gametest/resources/fabric.mod.json must list io.github.jimbozoomer.jugcraft.test.FestivalToyGameTests")

    # The generated files, exactly as the generator makes them now.
    expected = {}

    def keep(path, obj):
        import model_writer
        if isinstance(obj, dict) and obj.get("elements") and "models" in path.parts:
            model_writer.finish_elements(obj["elements"])
        expected[path] = json.dumps(obj, indent=2) + "\n"
    lang = {}
    ttd.assets(assets, keep, lang)
    ttd.loot(data / "loot_table" / "blocks", keep)
    ttd.recipes(data / "recipe", keep, ttd._conditions)
    for path, text in expected.items():
        if _read(path) != text:
            out.append(f"{path.relative_to(repo)} is not what tools/thanksgiving_toys_data.py makes (regenerate it)")
    from PIL import Image
    for (kind, name), img in ttd.textures().items():
        path = assets / "textures" / kind / f"{name}.png"
        if not path.is_file():
            out.append(f"{path.relative_to(repo)} is missing")
            continue
        on_disk = Image.open(path).convert("RGBA")
        if on_disk.size != img.size or on_disk.tobytes() != img.convert("RGBA").tobytes():
            out.append(f"{path.relative_to(repo)} is not what tools/thanksgiving_toys_data.py paints (regenerate it)")
    manifest = _load(repo / "tools" / "data" / "generated-material-resources.json") or []
    listed = set(manifest)
    for path in expected:
        rel = path.relative_to(res).as_posix()
        if rel not in listed:
            out.append(f"tools/data/generated-material-resources.json must list {rel}")

    # Drops, tool, names.
    for block, spec in tt.PROPS.items():
        pools = (_load(data / "loot_table" / "blocks" / f"{block}.json") or {}).get("pools", [])
        if len(pools) != 1 or [e.get("name") for e in pools[0].get("entries", [])] != [f"{MOD}:{block}"]:
            out.append(f"loot_table/blocks/{block}.json: it drops itself, once")
        elif spec["kind"] == "backdrop" and "\"part\": \"0\"" not in json.dumps(pools[0].get("condition", {})).replace("'", '"'):
            out.append(f"loot_table/blocks/{block}.json: only its first block drops it")
    axe = set((_load(res / "data" / "minecraft" / "tags" / "block" / "mineable" / "axe.json") or {}).get("values", []))
    en = _load(assets / "lang" / "en_us.json") or {}
    for block in tt.AXE:
        if f"{MOD}:{block}" not in axe:
            out.append(f"data/minecraft/tags/block/mineable/axe.json must list {MOD}:{block}")
    for key, text in tt.names().items():
        if en.get(key) != text:
            out.append(f"en_us.json: {key} must be {text!r}")

    # Recipes: shapeless, the agriculture switch, one of its piece, no research.
    for recipe in tt.SHAPELESS:
        made = _load(data / "recipe" / f"{recipe['id']}.json") or {}
        if made.get("type") != "minecraft:crafting_shapeless" or made.get("result") != {"id": f"{MOD}:{recipe['id']}", "count": 1}:
            out.append(f"recipe/{recipe['id']}.json must be a shapeless recipe making one {recipe['id']}")
        if made.get("fabric:load_conditions") != [{"condition": f"{MOD}:feature_enabled", "feature": tt.FEATURE}]:
            out.append(f"recipe/{recipe['id']}.json must follow the {tt.FEATURE} switch only (no research)")
        if not 1 <= len(recipe["inputs"]) <= 9:
            out.append(f"recipe/{recipe['id']}.json must fit a crafting grid")

    # Models: in their block, on the floor, inside their outline; the backdrop's middle left open.
    for block, spec in tt.PROPS.items():
        if spec["kind"] == "backdrop":
            continue
        for name, elements in ttd.block_models(block).items():
            solid = [e for e in elements if all(e["to"][k] - e["from"][k] > 1e-6 for k in range(3))]
            x0, y0, z0, x1, y1, z1 = ttd.tdd.bounds(solid)
            sx0, sy0, sz0, sx1, sy1, sz1 = spec["shape"]
            if y0 < -1e-6 or x0 < -0.5 or z0 < -0.5 or x1 > 16.5 or z1 > 16.5 or y1 > 16.0 + 1e-6:
                out.append(f"{name}: its model must lie in its block, on the floor, not {[round(v, 2) for v in (x0, y0, z0, x1, y1, z1)]}")
            if x0 < sx0 - 0.5 or z0 < sz0 - 0.5 or x1 > sx1 + 0.5 or z1 > sz1 + 0.5 or y1 > sy1 + 0.5:
                out.append(f"{name}: the outline {spec['shape']} must cover its model {[round(v, 2) for v in (x0, y0, z0, x1, y1, z1)]}")
    try:
        sc = ttd.build("harvest_photo_backdrop")
        ttd.frame_cut(sc.models["frame_whole"])
    except AssertionError as problem:
        out.append(f"harvest_photo_backdrop: {problem}")
    states = (_load(assets / "blockstates" / "harvest_photo_backdrop.json") or {}).get("variants", {})
    if len(states) != 4 * 2 * len(tt.BACKDROP["cells"]):
        out.append("blockstates/harvest_photo_backdrop.json must cover every facing, lit and part")
    top_states = (_load(assets / "blockstates" / "spinning_top.json") or {}).get("variants", {})
    if len(top_states) != 4 * (tt.TOP["spin"] + 1):
        out.append("blockstates/spinning_top.json must cover every facing and spin step")

    # The corn toss's hole and board, seen from above, are where the slanted board's model puts them.
    hx0, hz0, hx1, hz1 = ttd.BOARD_HOLE
    hole_z, hole_y = ttd.projected((hz0 + hz1) / 2, ttd.BOARD_Y[1])
    front, _ = ttd.projected(ttd.BOARD_Z[0], ttd.BOARD_Y[1])
    back, _ = ttd.projected(ttd.BOARD_Z[1], ttd.BOARD_Y[1])
    c = tt.CORN_TOSS
    for what, have, want in (("hole x", c["hole"][0], (hx0 + hx1) / 2), ("hole z", c["hole"][1], hole_z), ("aim height", c["aim_height"], hole_y),
                             ("board x0", c["board"][0], ttd.BOARD_X[0]), ("board x1", c["board"][2], ttd.BOARD_X[1]),
                             ("board z0", c["board"][1], front), ("board z1", c["board"][3], back)):
        if abs(have - want) > 0.3:
            out.append(f"tools/thanksgiving_toys.py CORN_TOSS {what} {have} must be the model's {round(want, 2)}")
    return out


if __name__ == "__main__":
    found = problems(Path(__file__).resolve().parent.parent)
    for line in found:
        print(line)
    print(f"{len(found)} problems")
    sys.exit(1 if found else 0)
