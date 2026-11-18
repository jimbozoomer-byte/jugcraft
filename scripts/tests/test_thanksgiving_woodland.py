"""Woodland harvest crafts (tools/thanksgiving_woodland.py; docs/features/thanksgiving-woodland-crafts.md): focused
offline resource tests. Not game tests: they read the generated resources, the Java source and the shared files."""
import json
import sys
import tempfile
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))

import thanksgiving_woodland as tw  # noqa: E402
import thanksgiving_woodland_check as check  # noqa: E402
import thanksgiving_woodland_data as twd  # noqa: E402


class WoodlandHarvestCrafts(unittest.TestCase):
    def test_resources_are_generated_unedited(self):
        self.assertEqual(check.resources(), [])

    def test_regeneration_is_deterministic(self):
        first = twd.outputs()
        twd._built.clear()
        self.assertEqual(first, twd.outputs())

    def test_models_outlines_states_and_art_rules(self):
        self.assertEqual(check.models(), [])

    def test_drops_recipes_and_reachable_inputs(self):
        self.assertEqual(check.data(), [])

    def test_java_registers_the_table(self):
        self.assertEqual(check.java(), [])

    def test_shared_hooks_names_tags_and_registration(self):
        self.assertEqual(check.hooks(), [])

    def test_material_regeneration_cannot_prune_woodland_textures(self):
        from generated_resource_cleanup import prune
        outputs = twd.outputs()
        previous = set(json.loads((ROOT / "tools/data/generated-material-resources.json").read_text(encoding="utf-8")))
        # Model generation re-emits only JSON. The separate texture generator's
        # files must survive this real cleanup operation, even when run alone.
        current = {rel for rel in outputs if rel.endswith(".json")}
        with tempfile.TemporaryDirectory() as directory:
            resources = Path(directory)
            textures = [rel for rel in outputs if rel.endswith(".png")]
            for rel in textures:
                path = resources / rel
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_bytes(outputs[rel])
            prune(resources, previous.intersection(outputs), current)
            for rel in textures:
                self.assertEqual((resources / rel).read_bytes(), outputs[rel], rel)

    def test_each_piece_is_in_exactly_its_mineable_tag(self):
        # The only data tag these blocks' behaviour relies on (FacingDecorationBlock, SimpleWaterloggedBlock and the mat
        # check no tag); 26.3 validates tool speed against it.
        for block, spec in tw.PROPS.items():
            path = ROOT / "src/main/resources/data/minecraft/tags/block/mineable" / f"{spec['tool']}.json"
            self.assertIn(f"jugcraft:{block}", json.loads(path.read_text(encoding="utf-8"))["values"], block)

    def test_one_texel_to_a_model_pixel(self):
        # Every face of a sculpted piece reads as many texels as it is pixels (rounded up), and the mat reads by position.
        for block in twd.BUILDERS:
            sc = twd.build(block)
            unit = sc.atlas.size / 16
            for e in sc.models["whole"]:
                for side, face in e["faces"].items():
                    u0, v0, u1, v1 = face["uv"]
                    w, h = {"north": (0, 1), "south": (0, 1), "east": (2, 1), "west": (2, 1), "up": (0, 2), "down": (0, 2)}[side]
                    size = (e["to"][w] - e["from"][w], e["to"][h] - e["from"][h])
                    texels = (abs(u1 - u0) * unit, abs(v1 - v0) * unit)
                    for px, tx in zip(size, texels):
                        if px >= 1:
                            self.assertLessEqual(abs(tx - px), 1.0, f"{block} {side} {e['from']}..{e['to']}")

    def test_the_mat_joins_only_at_inner_edges(self):
        # A lone mat is bound on all four sides; joined on a side, that side's edge is plain mat at the mat's height.
        state = twd.mat_blockstate()["multipart"]
        alone = [p["apply"]["model"] for p in state if p["when"].get("axis") == "z" and all(v in ("z", "false") for v in p["when"].values())]
        self.assertTrue(all(m.endswith(("_bound", "_middle")) for m in alone), alone)
        for region in ("north", "north_east"):
            self.assertLess(twd.mat_part(region, False)["to"][1], twd.mat_part(region, True)["to"][1])

    def test_texts_say_inspired_decor_not_reconstruction(self):
        for block, text in tw.INFO.items():
            self.assertIn("not a historical reconstruction", text, block)
            for word in ("ceremon", "sacred", "regalia", "1621", "authentic"):
                self.assertNotIn(word, text.lower(), block)

    def test_decoration_only(self):
        for name in ("WoodlandCraftBlock", "WoodlandRushMatBlock", "WoodlandHarvestCrafts"):
            source = (check.JAVA / f"{name}.java").read_text(encoding="utf-8")
            for forbidden in ("BlockEntity", "MenuProvider", "Container", "useItemOn", "useWithoutItem", "randomTick"):
                self.assertNotIn(forbidden, source, name)


if __name__ == "__main__":
    unittest.main()
