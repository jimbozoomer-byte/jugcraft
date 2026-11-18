"""Focused resource checks for the Thanksgiving homestead and feast room (tools/thanksgiving_homestead.py,
tools/thanksgiving_homestead_data.py; docs/features/thanksgiving-homestead.md). Offline: no Minecraft is needed."""
import json
import math
import re
import sys
import tempfile
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))

import thanksgiving_homestead as th  # noqa: E402

RES = ROOT / "src" / "main" / "resources"
ASSETS = RES / "assets" / "jugcraft"
DATA = RES / "data"
AGRI = ROOT / "src" / "main" / "java" / "io" / "github" / "jimbozoomer" / "jugcraft" / "agriculture"
FACINGS = ("north", "east", "south", "west")


def load(path):
    return json.loads(path.read_text(encoding="utf-8"))


def squash(text):
    return re.sub(r"\s+", "", text)


def multi(block):
    return th.PROPS[block]["kind"] in ("settle", "furniture")


def part_models(block):
    spec = th.PROPS[block]
    if multi(block):
        return [f"{block}_{p}" for p in range(len(th.cells(spec["size"])))]
    return [block] + ([f"{block}_lit"] if spec["kind"] == "candles" else [])


class HomesteadResources(unittest.TestCase):
    def test_ids_are_new_and_distinct_from_the_refined_ones(self):
        self.assertEqual(len(set(th.PROPS)), 6)
        self.assertFalse(set(th.PROPS) & set(th.REFINED))
        for block in th.PROPS:
            self.assertRegex(block, r"^[a-z_]+$")

    def test_generator_reproduces_every_committed_file(self):
        import thanksgiving_homestead_data as thd
        from PIL import Image, ImageChops
        with tempfile.TemporaryDirectory() as tmp:
            written, _lang, _tags = thd.generate(tmp)
            self.assertTrue(written)
            for path in written:
                rel = path.relative_to(tmp)
                mine = RES / rel
                self.assertTrue(mine.is_file(), f"{rel} is generated but not committed")
                if path.suffix == ".png":
                    a, b = Image.open(path).convert("RGBA"), Image.open(mine).convert("RGBA")
                    self.assertEqual(a.size, b.size, str(rel))
                    self.assertIsNone(ImageChops.difference(a, b).getbbox(), f"{rel}: committed texture differs from the generator's")
                else:
                    self.assertEqual(path.read_text(encoding="utf-8"), mine.read_text(encoding="utf-8"), f"{rel}: regenerate it")

    def test_blockstates_cover_every_state(self):
        for block, spec in th.PROPS.items():
            variants = load(ASSETS / "blockstates" / f"{block}.json")["variants"]
            kind = spec["kind"]
            if multi(block):
                n = len(th.cells(spec["size"]))
                wanted = {f"facing={f},lit={l},part={p}": f"{block}_{p}" for f in FACINGS for l in ("false", "true") for p in range(n)}
            elif kind == "candles":
                wanted = {f"facing={f},lit={l}": block + ("_lit" if l == "true" else "") for f in FACINGS for l in ("false", "true")}
            else:
                wanted = {f"facing={f}": block for f in FACINGS}
            self.assertEqual(set(variants), set(wanted), block)
            for state, model in wanted.items():
                self.assertEqual(variants[state]["model"], f"jugcraft:block/{model}", f"{block} {state}")
                self.assertEqual(variants[state].get("y", 0), {"north": 0, "east": 90, "south": 180, "west": 270}[state.split(",")[0][7:]])
            for model in part_models(block):
                self.assertTrue((ASSETS / "models" / "block" / f"{model}.json").is_file(), model)
            self.assertTrue((ASSETS / "models" / "item" / f"{block}.json").is_file())
            self.assertEqual(load(ASSETS / "items" / f"{block}.json"), {"model": {"type": "minecraft:model", "model": f"jugcraft:item/{block}"}})

    def test_multi_block_footprint_and_parts(self):
        """The settle and the dresser are 2 x 2 x 1 (four parts, 0 its first block): the Java's layout, the part property's
        range, the blockstates' parts and the part models, each of which stays inside its own block."""
        furniture = (AGRI / "HomesteadFurnitureBlock.java").read_text(encoding="utf-8")
        self.assertIn("box(2, 2, 1)", furniture)
        self.assertIn('IntegerProperty.create("part", 0, CELLS.length - 1)', furniture)
        for block in ("oak_settle", "carved_dish_dresser"):
            spec = th.PROPS[block]
            self.assertEqual(tuple(spec["size"]), (2, 2, 1))
            self.assertEqual(th.cells(spec["size"]), [(0, 0, 0), (1, 0, 0), (0, 1, 0), (1, 1, 0)])
            for model in part_models(block):
                elements = load(ASSETS / "models" / "block" / f"{model}.json")["elements"]
                self.assertTrue(elements, model)
                for e in elements:
                    if "rotation" in e:
                        continue
                    for k in range(3):
                        self.assertGreaterEqual(e["from"][k], -0.5, f"{model}: {e['from']}")
                        self.assertLessEqual(e["to"][k], 16.5, f"{model}: {e['to']}")

    def test_seat_height_is_the_squab_top(self):
        settle = (AGRI / "HomesteadSettleBlock.java").read_text(encoding="utf-8")
        self.assertIn(f"SEAT = {th.PROPS['oak_settle']['seat']} / 16.0;", settle)
        self.assertIn("implements Seat.Sittable", settle)
        import thanksgiving_homestead_data as thd
        whole = thd.build("oak_settle").models["whole"]
        # The squabs' tops, from the model itself: the highest point of every box over the seat board in the seat's
        # depth is the squab's.
        tops = [e["to"][1] for e in whole if e["from"][1] >= 7.5 and e["to"][1] <= 9.0 and e["from"][2] >= 3.9 and e["to"][2] <= 12.3]
        self.assertEqual(max(tops), th.PROPS["oak_settle"]["seat"])

    def test_each_settle_half_is_one_seat(self):
        """Either block of a settle half seats at its lower block, for players and companions alike, and a broken settle
        stands its sitters up and releases any companion's reservation (static checks of the sources)."""
        seat = (AGRI / "Seat.java").read_text(encoding="utf-8")
        settle = (AGRI / "HomesteadSettleBlock.java").read_text(encoding="utf-8")
        seats = (ROOT / "src" / "main" / "java" / "local" / "peepo" / "CompanionSeats.java").read_text(encoding="utf-8")
        self.assertIn("default BlockPos seatCell(BlockGetter level, BlockPos pos, BlockState state)", seat)
        self.assertLess(seat.index("seatCell(level, pos, state)"), seat.index("CompanionSeats.isReserved(level,pos)"))
        self.assertIn("public BlockPos seatCell(BlockGetter level, BlockPos pos, BlockState state)", settle)
        self.assertIn("seat.ejectPassengers();", settle)
        self.assertIn("CompanionSeats.release(level, pos);", settle)
        self.assertIn("this.pos=seatCell(level,pos,state);", seats)
        self.assertIn("if(!seatCell(npc.level(),pos,state).equals(pos))continue;", seats)

    def test_every_piece_drops_once(self):
        for block in th.PROPS:
            table = load(DATA / "jugcraft" / "loot_table" / "blocks" / f"{block}.json")
            self.assertEqual(len(table["pools"]), 1)
            pool = table["pools"][0]
            self.assertEqual([e["name"] for e in pool["entries"]], [f"jugcraft:{block}"])
            self.assertEqual(pool["rolls"], 1)
            if multi(block):
                terms = pool["condition"]["terms"]
                self.assertIn({"type": "minecraft:match_block", "blocks": f"jugcraft:{block}", "state": {"part": "0"}}, terms)
            else:
                self.assertEqual(pool["condition"], {"type": "minecraft:survives_explosion"})

    def test_recipes_come_from_the_table_with_no_research(self):
        self.assertEqual({r["id"] for r in th.SHAPELESS}, set(th.PROPS))
        for recipe in th.SHAPELESS:
            data = load(DATA / "jugcraft" / "recipe" / f"{recipe['id']}.json")
            self.assertEqual(data["type"], "minecraft:crafting_shapeless")
            self.assertEqual(data["fabric:load_conditions"], [{"condition": "jugcraft:feature_enabled", "feature": "agriculture"}])
            self.assertEqual(data["ingredients"], recipe["inputs"])
            self.assertEqual(data["result"], {"id": f"jugcraft:{recipe['id']}", "count": 1})
            self.assertLessEqual(len(recipe["inputs"]), 9)
            self.assertNotIn("research", json.dumps(data))

    def test_outlines_cover_the_models(self):
        import thanksgiving_homestead_data as thd
        for block, spec in th.PROPS.items():
            whole = thd.build(block).models["whole"]
            x0, y0, z0, x1, y1, z1 = thd.bounds(whole)
            if spec["kind"] in ("floor", "candles"):
                sx0, sy0, sz0, sx1, sy1, sz1 = spec["shape"]
                self.assertTrue(x0 >= sx0 - 0.5 and z0 >= sz0 - 0.5 and x1 <= sx1 + 0.5 and z1 <= sz1 + 0.5 and y1 <= sy1 + 0.5,
                                f"{block}: shape {spec['shape']} must cover {[x0, y0, z0, x1, y1, z1]}")
            elif spec["kind"] == "wall":
                self.assertGreaterEqual(z0, 16.0 - spec["depth"] - 0.5, block)
                for lo, hi in ((x0, x1), (y0, y1)):
                    self.assertGreaterEqual(lo, spec["low"] - 0.5, block)
                    self.assertLessEqual(hi, spec["high"] + 0.5, block)
            else:
                boxes = [[b[k] - 0.75 if k < 3 else b[k] + 0.75 for k in range(6)] for b in spec["shapes"]]
                for e in whole:
                    if "rotation" in e:
                        continue
                    centre = [(e["from"][k] + e["to"][k]) / 2 for k in range(3)]
                    self.assertTrue(any(all(b[k] <= centre[k] <= b[k + 3] for k in range(3)) for b in boxes),
                                    f"{block}: no outline box holds the element at {e['from']}..{e['to']}")
                self.assertTrue(x1 <= 32.0 and y1 <= 32.0 and z1 <= 16.0 and min(x0, y0, z0) >= 0.0, f"{block} stays in its 2 x 2 x 1 frame")

    def test_art_rules_on_every_model_and_across_the_multi_block_seams(self):
        """art_check's block-model rules (Z1 shared planes, N1 near planes, U1 UVs outside the sprite, O1 open faces, I1
        backwards boxes) on each model, and X1/X2 across each multi-block piece's part models at their places."""
        import art_check as ac
        roots = (ASSETS,)
        models = [m for b in th.PROPS for m in part_models(b)] + ["quilt_rail", "butter_churn", "butter_churn_runner"]
        for name in models:
            model = load(ASSETS / "models" / "block" / f"{name}.json")
            elements, lookup, _ = ac._resolve(model, roots)
            self.assertTrue(elements, name)
            self.assertFalse([e for e in elements if any(e["from"][k] > e["to"][k] + 1e-6 for k in range(3))], f"I1 {name}")
            faces, raw = ac._model_faces(elements, lookup)
            self.assertFalse([uv for uv in raw if min(uv) < -1e-3 or max(uv) > 16 + 1e-3], f"U1 {name}")
            found = [(faces[i].label, faces[j].label, gap) for i, j, gap, overlap, u, v, n, d in ac._pairs(faces, ac.N1_GAP)
                     if not (gap == 0 and ac._layered(elements, faces[i], faces[j])) and ac._differ(faces[i], faces[j], overlap, u, v, n, d)]
            self.assertFalse(found, f"Z1/N1 {name}: {found[:3]}")
            self.assertFalse(ac._open_faces(elements, faces), f"O1 {name}")
        for block in ("oak_settle", "carved_dish_dresser"):
            size = th.PROPS[block]["size"]
            members = []
            for part, (r, u, a) in enumerate(th.cells(size)):
                members.append((ASSETS / "models" / "block" / f"{block}_{part}.json", [16 * (size[0] - 1 - r), 16 * u, 16 * a]))
            faces, boxes = ac._assembly(members, roots)
            cells = {tuple(o // 16 for o in offset) for _p, offset in members}
            self.assertEqual(ac._cross_errors(block, faces, boxes, cells), [], block)

    def test_textures_are_opaque_where_drawn_and_one_texel_a_pixel(self):
        from PIL import Image
        for block in list(th.PROPS) + list(th.REFINED):
            img = Image.open(ASSETS / "textures" / "block" / f"{block}.png").convert("RGBA")
            self.assertIn(img.size, ((64, 64), (128, 128)), block)
            alphas = set(img.getchannel("A").getdata())
            self.assertTrue(alphas <= {0, 255}, f"{block}: no half-transparent texels")
            names = part_models(block) if block in th.PROPS else ([block, f"{block}_runner"] if block == "butter_churn" else [block])
            for name in names:
                for e in load(ASSETS / "models" / "block" / f"{name}.json")["elements"]:
                    for side, face in e["faces"].items():
                        u0, v0, u1, v1 = face["uv"]
                        axes = {"north": (0, 1), "south": (0, 1), "east": (2, 1), "west": (2, 1), "up": (0, 2), "down": (0, 2)}[side]
                        for span_uv, k in ((abs(u1 - u0), axes[0]), (abs(v1 - v0), axes[1])):
                            px = e["to"][k] - e["from"][k]
                            if px >= 1.0:
                                density = span_uv / 16 * img.size[0] / px
                                self.assertTrue(0.8 <= density <= 1.6, f"{name} {side}: {density:.2f} texels a pixel")
                        x0, x1 = sorted((u0, u1))
                        y0, y1 = sorted((v0, v1))
                        box = (int(x0 / 16 * img.size[0] + 0.01), int(y0 / 16 * img.size[1] + 0.01),
                               max(int(x0 / 16 * img.size[0] + 0.01) + 1, math.ceil(x1 / 16 * img.size[0] - 0.01)),
                               max(int(y0 / 16 * img.size[1] + 0.01) + 1, math.ceil(y1 / 16 * img.size[1] - 0.01)))
                        if name.startswith("pewter_chamberstick") and min(img.getchannel("A").crop(box).getdata()) == 0:
                            continue  # the flame's cut-out sprite (thanksgiving_decor_data.flame_sprite)
                        self.assertEqual(min(img.getchannel("A").crop(box).getdata()), 255, f"{name} {side} reads see-through texels")

    def test_registration_light_and_names(self):
        main = (AGRI / "JugcraftAgriculture.java").read_text(encoding="utf-8")
        self.assertIn("registerThanksgivingHomestead();", main)
        self.assertIn(squash(th.java_registration()), squash(main), "JugcraftAgriculture must hold registerThanksgivingHomestead() as the table says")
        lang = load(ASSETS / "lang" / "en_us.json")
        for key, name in th.names().items():
            self.assertEqual(lang.get(key), name)

    def test_shared_data_entries(self):
        tags = {tool: set(load(DATA / "minecraft" / "tags" / "block" / "mineable" / f"{tool}.json")["values"]) for tool in ("axe", "pickaxe")}
        for block in th.PROPS:
            tool = "pickaxe" if block in th.PICKAXE else "axe"
            self.assertIn(f"jugcraft:{block}", tags[tool], f"mineable/{tool} must list {block}")
            self.assertNotIn(f"jugcraft:{block}", tags["axe" if tool == "pickaxe" else "pickaxe"])
        mod = load(ROOT / "src" / "gametest" / "resources" / "fabric.mod.json")
        listed = json.dumps(mod)
        self.assertIn("io.github.jimbozoomer.jugcraft.test.ThanksgivingHomesteadGameTests", listed)
        manifest = set(load(ROOT / "tools" / "data" / "generated-material-resources.json"))
        for block in th.PROPS:
            for rel in (f"assets/jugcraft/blockstates/{block}.json", f"data/jugcraft/recipe/{block}.json",
                        f"data/jugcraft/loot_table/blocks/{block}.json"):
                self.assertIn(rel, manifest)

    def test_no_behaviour_relies_on_an_unlisted_tag(self):
        """26.3 validates some behaviour against data-tag allowlists. The helpers these pieces use (seats, multi-block
        placement, candle lighting, wall and facing decorations) check no TagKey, so the mining tags above are the only
        tag entries they need; if a helper starts checking a tag, this test asks for its entry to be added."""
        for name in ("Seat", "MultiDecorationBlock", "HomesteadFurnitureBlock", "HomesteadSettleBlock", "CandleLighting",
                     "CandleCenterpieceBlock", "WallDecorationBlock", "FacingDecorationBlock", "LongDecorationBlock"):
            source = (AGRI / f"{name}.java").read_text(encoding="utf-8")
            self.assertNotIn("TagKey", source, f"{name} checks a tag: add the homestead pieces' entries to it")
            self.assertNotIn("BlockTags.", source, name)

    def test_refined_pieces_keep_their_contract(self):
        """The Quilt Rail and the Butter Churn keep their IDs, states, recipes, loot and the churn's working: only their
        models and textures change."""
        quilt = load(ASSETS / "blockstates" / "quilt_rail.json")["variants"]
        self.assertEqual(set(quilt), {f"facing={f}" for f in FACINGS})
        self.assertTrue(all(v["model"] == "jugcraft:block/quilt_rail" for v in quilt.values()))
        churn = load(ASSETS / "blockstates" / "butter_churn.json")["variants"]
        self.assertEqual(set(churn), {f"facing={f},full={b},runner={r}" for f in FACINGS for b in ("false", "true") for r in ("false", "true")})
        for state, variant in churn.items():
            self.assertEqual(variant["model"], "jugcraft:block/butter_churn" + ("_runner" if "runner=true" in state else ""))
        self.assertEqual(load(DATA / "jugcraft" / "recipe" / "quilt_rail.json")["ingredients"],
                         ["#minecraft:wooden_fences", "#minecraft:wool", "#minecraft:wool", "#minecraft:wool"])
        # The churn's dasher passes through the splash cup's hole with room all round, at rest and at the bottom of its
        # stroke (HandWorkedRenderer.DASHER_DEPTH, 5 pixels down), and the body stays in ButterChurnBlock's outline.
        base = load(ASSETS / "models" / "block" / "butter_churn.json")["elements"]
        runner = load(ASSETS / "models" / "block" / "butter_churn_runner.json")["elements"]
        handle = next(e for e in runner if e["from"][1] <= 12.0)
        self.assertEqual((handle["from"][0], handle["to"][0], handle["from"][2], handle["to"][2]), (7.5, 8.5, 7.5, 8.5))
        for e in base:
            if e["from"][1] >= 12.4:
                inside = all(not (e["from"][k] < 8.5 and e["to"][k] > 7.5) for k in (0,)) or all(
                    not (e["from"][k] < 8.5 and e["to"][k] > 7.5) for k in (2,))
                self.assertTrue(inside, f"the lid's part {e['from']}..{e['to']} must leave the dasher's way clear")
            self.assertTrue(4.5 <= e["from"][0] and e["to"][0] <= 11.5 and 4.5 <= e["from"][2] and e["to"][2] <= 11.5, e)
        lowest = min(e["from"][1] for e in runner if e["from"][0] < 7.5 - 1e-6) - 5.0
        lid_top = max(e["to"][1] for e in base)
        self.assertGreater(lowest, lid_top, "the grip stays clear of the lid at the bottom of the stroke")
        self.assertIn("SHAPE = Block.box(4.75, 0.0, 4.75, 11.25, 13.5, 11.25)", (AGRI / "ButterChurnBlock.java").read_text(encoding="utf-8"))


if __name__ == "__main__":
    unittest.main()
