"""Resource and number checks for the Thanksgiving weapons (tools/thanksgiving_weapons.py;
docs/features/thanksgiving-weapons.md): the inventory's decisions, the Naval Cutlass's numbers against its Java, its icon
map, its 3D model (one texel a model pixel, closed, nothing sharing a plane, held where the sabre is held), its recipe,
item definition, lang, tags and essence value, and the covered black-powder guns' rules. Offline checks, not a game test."""
import json
import math
import re
import sys
import unittest
from fractions import Fraction
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))

import thanksgiving_weapons as tw  # noqa: E402

ASSETS = ROOT / "src" / "main" / "resources" / "assets" / "jugcraft"
DATA = ROOT / "src" / "main" / "resources" / "data"
JAVA = ROOT / "src" / "main" / "java" / "io" / "github" / "jimbozoomer" / "jugcraft"
C = tw.CUTLASS


def load(path):
    return json.loads(Path(path).read_text(encoding="utf-8"))


def java_float(source, name):
    found = re.search(rf"\b{name}\s*=\s*(-?[\d.]+)F?;", source)
    return float(found.group(1)) if found else None


class Inventory(unittest.TestCase):
    def test_every_candidate_has_a_decision_and_its_items(self):
        self.assertEqual(set(tw.INVENTORY), {"blunderbuss", "flintlock pistol", "musket", "naval cutlass",
                                             "stone-headed hatchet or tomahawk", "woodland hunting bow", "hunting spear"})
        lang = load(ASSETS / "lang" / "en_us.json")
        for candidate, (decision, items, reason) in tw.INVENTORY.items():
            self.assertIn(decision, {"New", "Refine", "Reuse", "Covered"}, candidate)
            self.assertTrue(items and len(reason) > 40, candidate)
            for item in items:
                space, path = item.split(":")
                if space == "jugcraft":
                    self.assertIn(f"item.jugcraft.{path}", lang, f"{candidate}: {item} is not a registered, named item")
        self.assertEqual({c for c, (d, _i, _r) in tw.INVENTORY.items() if d == "New"},
                         {"naval cutlass", "stone-headed hatchet or tomahawk", "woodland hunting bow", "hunting spear"})

    def test_the_covered_guns_keep_the_owners_rules(self):
        """Slow single-shot muzzle-loaders on one Paper Cartridge a shot; the blunderbuss's balls a short, controlled
        cone (tools/guns.py, mirrored in guns/JugcraftGuns.java)."""
        import guns
        java = (JAVA / "guns" / "JugcraftGuns.java").read_text(encoding="utf-8")
        for gun in ("duelling_pistol", "line_musket", "bellmouth"):
            spec = guns.GUNS[gun]
            self.assertEqual((spec["capacity"], spec["ammo"]), (1, "paper_cartridge"), gun)
            self.assertGreaterEqual(spec["reload"], 70, gun)
            self.assertIn(f'SPECS.put("{gun}", new GunSpec(', java)
        bellmouth = guns.GUNS["bellmouth"]
        self.assertGreater(bellmouth["pellets"], 1)
        self.assertLessEqual(bellmouth["range"], 24)
        self.assertLessEqual(max(bellmouth["spread"]), 12.0)
        gun_item = (JAVA / "guns" / "GunItem.java").read_text(encoding="utf-8")
        self.assertRegex(gun_item, r"isBarVisible\(ItemStack stack\) \{\s*return false;", "a gun's durability bar is no ammo bar")


class CutlassNumbers(unittest.TestCase):
    def test_the_java_has_the_tables_numbers(self):
        stats = (JAVA / "weapons" / "NavalCutlassStats.java").read_text(encoding="utf-8")
        for name, value in (("DAMAGE", C["damage"]), ("SPEED", C["speed"]), ("REACH", C["reach"]), ("PARRY", C["parry"]),
                            ("BOARDING", C["boarding"]), ("IRON_BONUS", tw.IRON_BONUS)):
            self.assertEqual(java_float(stats, name), value, name)
        self.assertIn(f"SWING_TICKS = {C['swing'][1]};", stats)
        item = (JAVA / "weapons" / "NavalCutlassItem.java").read_text(encoding="utf-8")
        self.assertIn(f'ID = "{C["id"]}"', item)
        self.assertIn(f'new JugcraftArms.Kind("{C["kind"]}", NavalCutlassStats.DAMAGE', item)
        self.assertIn("SwingAnimationType.WHACK", item)
        self.assertIn("JugcraftArms.arm(new Item.Properties().setId(key), ToolMaterial.IRON, KIND)", item)

    def test_balance(self):
        """Ashore it stays below the iron sword a second; afloat it is the better blade; its parry is the longsword's
        lesser cousin; it reaches less than the hand."""
        self.assertLess(tw.per_second(), tw.IRON_SWORD)
        self.assertGreater((tw.blow() + C["boarding"]) * (4.0 + C["speed"]), tw.IRON_SWORD)
        self.assertTrue(0.0 < C["parry"] < 0.6)
        self.assertLess(C["reach"], 3.0)
        self.assertEqual(C["durability"], 250)  # vanilla iron


class CutlassArt(unittest.TestCase):
    def test_generator_reproduces_every_committed_file(self):
        self.assertEqual(tw.generate(check=True), [])

    def test_the_generation_pipeline_makes_every_file(self):
        """An ordinary full regeneration reproduces this module's files and keeps its lang, tags and recipes: the
        pipeline calls its hooks, lists its JSON (and only JSON) in the manifest, and the shared files carry its entries."""
        self.assertEqual(tw.hook_problems(), [])
        listed = set(load(tw.MANIFEST))
        ids = (C["id"], "stone_headed_hatchet", "hunting_spear", "woodland_hunting_bow")
        ours = {rel for rel in listed if any(rel.endswith(f"/{c}.json") or f"/{c}_" in rel for c in ids)}
        self.assertEqual(ours, set(tw.json_files()))
        self.assertTrue(all(rel.endswith(".json") for rel in tw.json_files()))
        written, saved, table = {}, {}, {}
        tw.write_all(lambda path, obj: written.__setitem__(path.relative_to(tw.RES).as_posix(), obj),
                     tw.RES / "assets" / "jugcraft", tw.RES / "data" / "jugcraft", table, tw._condition)
        tw.draw_all(lambda img, kind, name: saved.__setitem__((kind, name), img))
        self.assertEqual(set(written), set(tw.json_files()))
        self.assertEqual(table, tw.lang())
        self.assertEqual(set(saved), set(tw.textures()))

    def test_icon_map_passes_the_icon_rules(self):
        import check_icon_maps
        errors, warnings = check_icon_maps.check([str(ROOT / "tools" / "item_icons" / f"{C['id']}.txt")])
        self.assertEqual(errors, [])
        self.assertEqual(warnings, [])

    def model(self):
        return load(ASSETS / "models" / "item" / f"{C['id']}_in_hand.json")

    def test_model_is_square_texelled_closed_and_opaque(self):
        from PIL import Image
        model = self.model()
        img = Image.open(ASSETS / "textures" / "item" / f"{C['id']}_model.png").convert("RGBA")
        unit = img.width / 16
        self.assertEqual(img.size, (tw.TEXTURE, tw.TEXTURE))
        for i, e in enumerate(model["elements"]):
            self.assertNotIn("rotation", e, "every box is axis-aligned, its texels square")
            self.assertEqual(set(e["faces"]), {"north", "south", "east", "west", "up", "down"}, f"element {i} is closed")
            lo, hi = e["from"], e["to"]
            for side, face in e["faces"].items():
                w, h = tw.face_size(side, lo, hi)
                u0, v0, u1, v1 = face["uv"]
                self.assertAlmostEqual(abs(u1 - u0) * unit, w, delta=0.02, msg=f"element {i} {side}: one texel a pixel")
                self.assertAlmostEqual(abs(v1 - v0) * unit, h, delta=0.02, msg=f"element {i} {side}: one texel a pixel")
                box = (int(round(min(u0, u1) * unit)), int(round(min(v0, v1) * unit)),
                       int(math.ceil(max(u0, u1) * unit - 1e-6)), int(math.ceil(max(v0, v1) * unit - 1e-6)))
                self.assertEqual(img.crop(box).getextrema()[3][0], 255, f"element {i} {side} reads a see-through texel")

    def test_model_passes_the_art_checks(self):
        """art_check's Z1 (shared planes), N1 (near planes), U1 (UVs off the sprite), O1 (holes) and I1 (backwards
        boxes), run on this model alone as check_block_models runs them on every model."""
        import art_check
        elements, lookup, _textures = art_check._resolve(self.model(), (ASSETS,))
        self.assertFalse([e for e in elements if any(e["from"][k] > e["to"][k] for k in range(3))])
        faces, raw = art_check._model_faces(elements, lookup)
        self.assertFalse([uv for uv in raw if min(uv) < -1e-3 or max(uv) > 16 + 1e-3])
        clashes = [(i, j) for i, j, gap, overlap, u, v, n, d in art_check._pairs(faces, art_check.N1_GAP)
                   if not (gap == 0 and art_check._layered(elements, faces[i], faces[j]))
                   and art_check._differ(faces[i], faces[j], overlap, u, v, n, d)]
        self.assertEqual(clashes, [])
        self.assertEqual(art_check._open_faces(elements, faces), [])

    def test_parts_touch(self):
        """Nothing floats: every box overlaps or touches another, and all are one piece."""
        boxes = [(lo, hi) for _n, _m, lo, hi in tw.BOXES]

        def touch(a, b):
            return all(a[0][k] <= b[1][k] and b[0][k] <= a[1][k] for k in range(3))
        reached, todo = {0}, [0]
        while todo:
            i = todo.pop()
            for j in range(len(boxes)):
                if j not in reached and touch(boxes[i], boxes[j]):
                    reached.add(j)
                    todo.append(j)
        self.assertEqual(len(reached), len(boxes))

    def test_held_where_the_sabre_is_held(self):
        """In each hand the cutlass's grip lands where the sabre's does, and its blade points the way the sabre's does."""
        display = self.model()["display"]

        def place(tr, left, p):
            T, R, s = tw._runtime(tr, left)
            v = tw._apply(R, [s * (p[k] / 16.0 - 0.5) for k in range(3)])
            return [T[k] + v[k] for k in range(3)]

        def sabre_point(p):
            # the sabre's boxes are built upright about its grip and turned -45 degrees about z there
            g = tw.SABRE_GRIP
            d = tw._apply(tw._rot("z", tw.SABRE_TURN), [p[k] - g[k] for k in range(3)])
            return [g[k] + d[k] for k in range(3)]
        up = [0.0, 1.0, 0.0]
        for context, tr in tw.SABRE_DISPLAY.items():
            left = context.endswith("lefthand")
            ours = display[context]
            grip_sabre = place(tr, left, list(tw.SABRE_GRIP))
            grip_ours = place(ours, left, list(tw.GRIP_CENTRE))
            for k in range(3):
                self.assertAlmostEqual(grip_sabre[k], grip_ours[k], places=3, msg=context)
            tip_sabre = [a - b for a, b in zip(place(tr, left, sabre_point([tw.SABRE_GRIP[k] + up[k] for k in range(3)])), grip_sabre)]
            tip_ours = [a - b for a, b in zip(place(ours, left, [tw.GRIP_CENTRE[k] + up[k] for k in range(3)]), grip_ours)]
            norm = lambda v: [c / math.sqrt(sum(x * x for x in v)) for c in v]  # noqa: E731
            for a, b in zip(norm(tip_sabre), norm(tip_ours)):
                self.assertAlmostEqual(a, b, places=4, msg=context)
        sabre = load(ASSETS / "models" / "item" / "arms_sabre.json")["display"] if (ASSETS / "models" / "item" / "arms_sabre.json").exists() else None
        if sabre is not None:
            self.assertEqual({k: sabre[k] for k in tw.SABRE_DISPLAY}, {k: {"rotation": v["rotation"], "translation": v["translation"],
                             "scale": v["scale"]} for k, v in tw.SABRE_DISPLAY.items()})

    def test_item_definition_icon_and_model(self):
        definition = load(ASSETS / "items" / f"{C['id']}.json")["model"]
        self.assertEqual(definition["cases"][0]["when"], ["gui"])
        self.assertEqual(definition["cases"][0]["model"]["model"], f"jugcraft:item/{C['id']}")
        self.assertEqual(definition["fallback"]["model"], f"jugcraft:item/{C['id']}_in_hand")
        icon = load(ASSETS / "models" / "item" / f"{C['id']}.json")
        self.assertEqual(icon["textures"]["layer0"], f"jugcraft:item/{C['id']}")
        from PIL import Image
        self.assertEqual(Image.open(ASSETS / "textures" / "item" / f"{C['id']}.png").size, (16, 16))
        for context in ("thirdperson_righthand", "thirdperson_lefthand", "firstperson_righthand", "firstperson_lefthand",
                        "ground", "fixed", "on_shelf", "head"):
            self.assertIn(context, self.model()["display"])


class CutlassData(unittest.TestCase):
    def test_recipe(self):
        recipe = load(DATA / "jugcraft" / "recipe" / f"{C['id']}.json")
        self.assertEqual(recipe["type"], "minecraft:crafting_shaped")
        self.assertEqual(recipe["fabric:load_conditions"][0]["feature"], C["feature"])
        self.assertEqual(recipe["result"], {"id": f"jugcraft:{C['id']}", "count": 1})
        used = set("".join(recipe["pattern"])) - {" "}
        self.assertEqual(used, set(recipe["key"]))
        self.assertEqual(recipe["key"]["B"], "#c:ingots/brass")

    def test_lang_and_tags(self):
        lang = load(ASSETS / "lang" / "en_us.json")
        for key, value in tw.lang().items():
            self.assertEqual(lang.get(key), value, key)
        swords = load(DATA / "minecraft" / "tags" / "item" / "swords.json")["values"]
        self.assertIn(f"jugcraft:{C['id']}", swords)

    def test_essence_value_is_its_recipe_cost(self):
        """Selling it back is never worth more than what it was made of (no fallback value, no profit loop)."""
        values = load(DATA / "jugcraft" / "alchemy" / "values.json")["items"]
        record = values.get(f"jugcraft:{C['id']}")
        self.assertIsNotNone(record)
        self.assertEqual(record["source"], f"recipe jugcraft:{C['id']}")
        pattern, key = C["recipe"]
        cost = Fraction(0)
        for symbol in "".join(pattern).replace(" ", ""):
            ingredient = key[symbol]
            item = "jugcraft:brass_ingot" if ingredient == "#c:ingots/brass" else ingredient
            entry = values[item]
            cost += Fraction(entry["numerator"], entry["denominator"])
        self.assertEqual(Fraction(record["numerator"], record["denominator"]), cost)

    def test_registered_with_the_arms(self):
        boot = (JAVA / "Jugcraft.java").read_text(encoding="utf-8")
        arms = boot.index("weapons.JugcraftArms.register();")
        self.assertIn("weapons.NavalCutlassItem.register();", boot[arms:arms + 200])


if __name__ == "__main__":
    unittest.main()
