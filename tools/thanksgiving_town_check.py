"""Offline checks of the Thanksgiving harvest town details (tools/thanksgiving_town.py,
docs/features/thanksgiving-town-details.md), for tools/check_mod_data.py (check_thanksgiving_town) and
scripts/tests/test_thanksgiving_town.py. NOT a game test: it reads the files and the Java source as text.

problems() lists what is wrong:
- every piece is registered by JugcraftAgriculture.registerThanksgivingTown() as its kind's block with the table's shape
  and properties, and its item in the building tab; the arch's Java cells and shapes are the table's;
- its name is its text; it drops itself once (the arch from its first block only); it is mined by its tag; its recipe was
  generated from the table, follows the agriculture switch and needs no research or calendar;
- its texture exists; its model lies in its block and its outline covers it; its blockstate covers every state of its
  kind (facing; lit; the arch's facing, lit and eleven parts);
- every file these pieces own is exactly what tools/thanksgiving_town_data.py makes now (regeneration changes nothing);
- the planters' harvest tag holds the table's items and PlanterBlock takes it.
art_problems() runs tools/art_check.py's block-model rules (I1, U1, Z1, N1, O1) on these pieces' models, and its rules
for models drawn together (X1, X2) on the arch's eleven parts at their places.
"""
import json
import re
from pathlib import Path

import thanksgiving_town as tt
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


def _read(path):
    return path.read_text(encoding="utf-8") if path.is_file() else ""


def registration_calls():
    """{block: the start of the Java registration call it must have}."""
    out = {}
    for block, spec in tt.PROPS.items():
        kind = spec["kind"]
        if kind in ("floor", "candles", "hanging"):
            out[block] = f'registerBlock("{block}", props -> new {tt.KINDS[kind]}(props, {tt.java_box(spec["shape"])})'
        else:
            out[block] = f'registerBlock("{block}", {tt.KINDS[kind]}::new'
    return out


def model_bounds(block):
    """The box round every face-bearing element of the piece's model drawn whole (tools/thanksgiving_town_data.py)."""
    import thanksgiving_town_data as ttd
    return ttd.bounds(ttd.build(block).models["whole"])


def problems(check_java=True, check_generated=True):
    out = []
    main = _read(JAVA / "JugcraftAgriculture.java")
    if check_java:
        if "registerThanksgivingTown();" not in main:
            out.append("JugcraftAgriculture.java must call registerThanksgivingTown()")
        body = main[main.find("private static void registerThanksgivingTown()"):]
        body = body[:body.find("\n\t}\n")]
        for block, call in registration_calls().items():
            if call not in body:
                out.append(f"JugcraftAgriculture.registerThanksgivingTown: {block} must be registered as {call}...")
            spec = tt.PROPS[block]
            for needed in (f"MapColor.{spec['color']}", f"SoundType.{spec['sound']}", f"{float(spec['strength'])}F"):
                line = next((l for l in body.splitlines() if f'"{block}"' in l), "")
                rest = body[body.find(line):body.find(line) + 400] if line else ""
                if needed not in rest:
                    out.append(f"JugcraftAgriculture.registerThanksgivingTown: {block} must use {needed} (tools/thanksgiving_town.py PROPS)")
        if "new BlockItem(block, props)" not in body or "BUILDING_TAB" not in body:
            out.append("JugcraftAgriculture.registerThanksgivingTown: every piece needs its BlockItem in the building tab")
        arch = _read(JAVA / "HarvestTownArchBlock.java")
        cells = "{" + ", ".join(f"{{{r}, {u}}}" for r, u in tt.ARCH["cells"]) + "}"
        if f"int[][] CELLS = {cells};" not in arch:
            out.append(f"HarvestTownArchBlock.CELLS must be tools/thanksgiving_town.py ARCH cells {cells}")
        for box in tt.ARCH["shapes"]:
            if "new double[] {" + ", ".join(f"{float(v)}" for v in box) + "}" not in arch:
                out.append(f"HarvestTownArchBlock.SHAPES must include ARCH shape {box}")
        for name in ("WIDTH", "HEIGHT", "OPENING", "CLEAR"):
            if f"{name} = {tt.ARCH[name.lower()]};" not in arch:
                out.append(f"HarvestTownArchBlock.{name} must be {tt.ARCH[name.lower()]}")
        decor = _read(JAVA / "HarvestTownDecor.java")
        if f"CHAFING_DISH_LIGHT = {tt.PROPS['copper_chafing_dish']['light']};" not in decor:
            out.append("HarvestTownDecor.CHAFING_DISH_LIGHT must be the table's light (tools/thanksgiving_town.py PROPS)")
        if "lightLevel(HarvestTownDecor::chafingDishLight)" not in body:
            out.append("JugcraftAgriculture.registerThanksgivingTown: the chafing dish's light is HarvestTownDecor.chafingDishLight")
        planter = _read(JAVA / "PlanterBlock.java")
        tag = tt.PLANTER_HARVEST["tag"].split(":")[1]
        if f'Jugcraft.id("{tag}")' not in planter or "stack.is(HARVEST)" not in planter:
            out.append(f"PlanterBlock must take #{tt.PLANTER_HARVEST['tag']} besides small flowers (HARVEST in plantable)")
    lang = _load(ASSETS / "lang" / "en_us.json") or {}
    tags = {kind: set((_load(DATA / "minecraft" / "tags" / "block" / "mineable" / f"{kind}.json") or {}).get("values", []))
            for kind in ("axe", "pickaxe")}
    for block, spec in tt.PROPS.items():
        if lang.get(f"block.{MOD}.{block}") != spec["display"]:
            out.append(f"block.{MOD}.{block}: the text must be {spec['display']}")
        pools = (_load(DATA / MOD / "loot_table" / "blocks" / f"{block}.json") or {}).get("pools", [])
        if len(pools) != 1 or [e.get("name") for e in pools[0].get("entries", [])] != [f"{MOD}:{block}"]:
            out.append(f"loot_table/blocks/{block}.json: it drops itself, once")
        elif spec["kind"] == "arch":
            terms = pools[0].get("condition", {}).get("terms", [])
            if {"type": "minecraft:match_block", "blocks": f"{MOD}:{block}", "state": {"part": "0"}} not in terms:
                out.append(f"loot_table/blocks/{block}.json: the arch drops from its first block only")
        tool = "pickaxe" if block in tt.PICKAXE else "axe"
        if f"{MOD}:{block}" not in tags[tool]:
            out.append(f"tags/block/mineable/{tool}.json must list {block}")
        other = "axe" if tool == "pickaxe" else "pickaxe"
        if f"{MOD}:{block}" in tags[other]:
            out.append(f"tags/block/mineable/{other}.json must not list {block}")
        if not (ASSETS / "textures" / "block" / f"{block}.png").is_file():
            out.append(f"textures/block/{block}.png is missing")
        variants = (_load(ASSETS / "blockstates" / f"{block}.json") or {}).get("variants", {})
        if spec["kind"] == "arch":
            wanted = {f"facing={f},lit={l},part={p}": f"{block}_{p}" for f in FACINGS for l in ("false", "true")
                      for p in range(len(tt.ARCH["cells"]))}
        elif spec["kind"] == "candles":
            wanted = {f"facing={f},lit={l}": block + ("_lit" if l == "true" else "") for f in FACINGS for l in ("false", "true")}
        else:
            wanted = {f"facing={f}": block for f in FACINGS}
        if set(variants) != set(wanted):
            out.append(f"blockstates/{block}.json: its variants must be exactly its states {sorted(wanted)[:3]}...")
        for state, model in wanted.items():
            if variants.get(state, {}).get("model") != f"{MOD}:block/{model}":
                out.append(f"blockstates/{block}.json: {state} must draw block/{model}")
            if not (ASSETS / "models" / "block" / f"{model}.json").is_file():
                out.append(f"models/block/{model}.json is missing")
        recipe = _load(DATA / MOD / "recipe" / f"{block}.json") or {}
        made = next(r for r in tt.SHAPELESS if r["id"] == block)
        if recipe.get("ingredients") != made["inputs"] or recipe.get("result", {}).get("id") != f"{MOD}:{block}":
            out.append(f"recipe/{block}.json must be generated from tools/thanksgiving_town.py SHAPELESS")
        if recipe.get("fabric:load_conditions") != [{"condition": f"{MOD}:feature_enabled", "feature": tt.FEATURE}]:
            out.append(f"recipe/{block}.json must follow the {tt.FEATURE} switch alone (no research, no calendar)")
        # The model in its block (the arch in its frame), its outline covering it to half a pixel.
        x0, y0, z0, x1, y1, z1 = model_bounds(block)
        if spec["kind"] == "arch":
            w, h = tt.ARCH["width"] * 16, tt.ARCH["height"] * 16
            if x0 < -0.5 or y0 < -1e-6 or z0 < -0.5 or x1 > w + 0.5 or y1 > h + 1e-6 or z1 > 16.5:
                out.append(f"{block}: its model must lie in its frame, not {[x0, y0, z0, x1, y1, z1]}")
        else:
            if x0 < -0.5 or z0 < -0.5 or y0 < -1e-6 or x1 > 16.5 or y1 > 16.0 + 1e-6 or z1 > 16.5:
                out.append(f"{block}: its model must lie in its block, not {[x0, y0, z0, x1, y1, z1]}")
            sx0, sy0, sz0, sx1, sy1, sz1 = spec["shape"]
            if x0 < sx0 - 0.5 or z0 < sz0 - 0.5 or x1 > sx1 + 0.5 or z1 > sz1 + 0.5 or y1 > sy1 + 0.5 or y0 < sy0 - 0.5:
                out.append(f"{block}: its shape {spec['shape']} must cover its model {[round(v, 2) for v in (x0, y0, z0, x1, y1, z1)]}")
    harvest = _load(DATA / MOD / "tags" / "item" / f"{tt.PLANTER_HARVEST['tag'].split(':')[1]}.json") or {}
    if harvest.get("values") != tt.PLANTER_HARVEST["items"]:
        out.append(f"tags/item/planter_harvest.json must hold {tt.PLANTER_HARVEST['items']}")
    if check_generated:
        import thanksgiving_town_data as ttd
        files, names, _ = ttd.files()
        for rel, data in files.items():
            path = RES / rel
            if not matches(path, data):
                out.append(f"{rel}: not what tools/thanksgiving_town_data.py makes (run it)")
        if names != tt.names():
            out.append("tools/thanksgiving_town_data.py must name every piece from the table")
    return out


def arch_members():
    """The arch's part models at their places in its frame (px): MultiDecorationBlock's cells turned to the frame."""
    w = tt.ARCH["width"]
    return [(ASSETS / "models" / "block" / f"{tt.ARCH['block']}_{part}.json", ((w - 1 - r) * 16, u * 16, 0))
            for part, (r, u) in enumerate(tt.ARCH["cells"])]


def art_problems():
    """tools/art_check.py's rules on these pieces' block and item models (I1, U1, Z1, N1, O1), with none of its
    allow-lists, and its rules for models drawn together (X1, X2) on the arch's parts."""
    import art_check as ac
    out = []
    paths = []
    for block, spec in tt.PROPS.items():
        if spec["kind"] == "arch":
            paths += [p for p, _ in arch_members()]
        else:
            paths.append(ASSETS / "models" / "block" / f"{block}.json")
            if spec["kind"] == "candles":
                paths.append(ASSETS / "models" / "block" / f"{block}_lit.json")
        paths.append(ASSETS / "models" / "item" / f"{block}.json")
    for path in paths:
        model = _load(path)
        if model is None:
            out.append(f"{path.relative_to(RES)}: missing")
            continue
        elements, lookup, _ = ac._resolve(model, (ASSETS,))
        name = str(path.relative_to(RES))
        if any(any(el["from"][k] > el["to"][k] + 1e-6 for k in range(3)) for el in elements):
            out.append(f"I1 {name}: an element runs backwards")
        faces, raw = ac._model_faces(elements, lookup)
        if any(min(uv) < -1e-3 or max(uv) > 16 + 1e-3 for uv in raw):
            out.append(f"U1 {name}: a face reads outside its sprite")
        for i, j, gap, overlap, u, v, n, d in ac._pairs(faces, ac.N1_GAP):
            if gap == 0 and ac._layered(elements, faces[i], faces[j]):
                continue
            if ac._differ(faces[i], faces[j], overlap, u, v, n, d):
                out.append(f"{'Z1' if gap == 0 else 'N1'} {name}: element {faces[i].label} / {faces[j].label} share a plane")
                break
        open_faces = ac._open_faces(elements, faces)
        if open_faces:
            i, face = open_faces[0]
            out.append(f"O1 {name}: {len(open_faces)} left-out face(s) exposed, e.g. the {face} face of element {i}")
    members = arch_members()
    faces, boxes = ac._assembly(members, (ASSETS,))
    out += ac._cross_errors(f"{tt.ARCH['block']} (parts)", faces, boxes, {tuple(o // 16 for o in offset) for _p, offset in members})
    return out


if __name__ == "__main__":
    import sys
    found = problems() + art_problems()
    for problem in found:
        print(problem)
    print(f"thanksgiving town check: {len(found)} problem(s)")
    sys.exit(1 if found else 0)
