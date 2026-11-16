"""The horse breeds' resources (docs/features/horse-breeds.md): generated files current, textures the models' sizes,
egg items, loot that drops once, lang lines, tags, and the Java registry/model ids matching. Standard library only."""
import json
import re
import struct
import sys
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))
import horse_breeds  # noqa: E402
import horse_breeds_data  # noqa: E402

RES = ROOT / "src/main/resources"
ASSETS = RES / "assets/jugcraft"
DATA = RES / "data"
BREEDS = horse_breeds_data.BREEDS


def png_size(path):
    raw = path.read_bytes()
    assert raw[:8] == b"\x89PNG\r\n\x1a\n", path
    return struct.unpack(">II", raw[16:24])


class HorseBreedResourceTests(unittest.TestCase):
    def test_five_breeds_everywhere(self):
        self.assertEqual(tuple(horse_breeds.BREEDS), BREEDS)
        java = (ROOT / "src/main/java/io/github/jimbozoomer/jugcraft/horses/HorseBreed.java").read_text(encoding="utf-8")
        for breed in BREEDS:
            self.assertIn(f'"{breed}"', java, breed)

    def test_textures_match_their_models(self):
        for breed in BREEDS:
            horse, saddle, bags = horse_breeds.art_paths(breed)
            for model, name in ((horse, breed), (saddle, breed + "_saddle")) + (((bags, breed + "_saddlebags"),) if bags.exists() else ()):
                m = horse_breeds.load(model)
                png = ASSETS / "textures/entity/horse_breeds" / f"{name}.png"
                self.assertEqual(png.read_bytes(), horse_breeds.embedded_png(m), f"{name}.png is the PNG its model embeds")
                self.assertEqual(png_size(png), horse_breeds.resolution(m), f"{name}.png at one texel a model pixel")
        pack = horse_breeds.load(horse_breeds.ART / "pegasus/pegasus_saddlebags.bbmodel")
        self.assertEqual((ASSETS / "textures/entity/pegasus/pegasus_saddlebags.png").read_bytes(), horse_breeds.embedded_png(pack))

    def test_whole_pixel_cubes_and_no_element_rotation(self):
        for breed in BREEDS:
            for path in horse_breeds.art_paths(breed):
                if not path.exists():
                    continue
                for el in horse_breeds.load(path)["elements"]:
                    self.assertFalse(any(el.get("rotation", [0, 0, 0])), f"{path.name}/{el['name']}")
                    size = [el["to"][i] - el["from"][i] for i in range(3)]
                    self.assertTrue(all(abs(s - round(s)) < 1e-6 for s in size), f"{path.name}/{el['name']} {size}")

    def test_spawn_eggs(self):
        for breed in BREEDS:
            egg = f"{breed}_spawn_egg"
            item = json.loads((ASSETS / "items" / f"{egg}.json").read_text())
            self.assertEqual(item["model"]["model"], f"jugcraft:item/{egg}")
            model = json.loads((ASSETS / "models/item" / f"{egg}.json").read_text())
            self.assertEqual(model["textures"]["layer0"], f"jugcraft:item/{egg}")
            self.assertEqual(png_size(ASSETS / "textures/item" / f"{egg}.png"), (16, 16))

    def test_loot_drops_leather_once(self):
        for breed in list(BREEDS) + ["pegasus"]:
            loot = json.loads((DATA / "jugcraft/loot_table/entities" / f"{breed}.json").read_text())
            self.assertEqual(len(loot["pools"]), 1, breed)
            entries = loot["pools"][0]["entries"]
            self.assertEqual([e["name"] for e in entries], ["minecraft:leather"], breed)
            self.assertEqual(loot["pools"][0]["rolls"], 1, breed)
            modifiers = entries[0]['modifier']
            self.assertEqual([m['type'] for m in modifiers],
                             ['minecraft:set_count', 'minecraft:enchanted_count_increase'])
            self.assertEqual(modifiers[0]['count'], {'type':'minecraft:uniform','min':0,'max':2})
            self.assertEqual(modifiers[1]['enchantment'], 'minecraft:looting')
            self.assertNotIn('functions', entries[0], '26.3 reads modifier, not legacy functions')

    def test_lang_has_every_line(self):
        lang = json.loads((ASSETS / "lang/en_us.json").read_text(encoding="utf-8"))
        for key, text in horse_breeds_data.TEXT.items():
            self.assertEqual(lang.get(key), text, key)
        java = "\n".join(p.read_text(encoding="utf-8") for p in (ROOT / "src/main/java/io/github/jimbozoomer/jugcraft/horses").glob("*.java"))
        for key in re.findall(r'"((?:message|tooltip|container)\.jugcraft\.(?:horse_breeds|saddlebags)[a-z_.]*)"', java):
            if key.endswith("."):
                continue
            self.assertIn(key, lang, key)

    def test_tags(self):
        def values(path):
            return json.loads((DATA / path).read_text())["values"]
        saddle = values("minecraft/tags/entity_type/can_equip_saddle.json")
        for breed in BREEDS:
            self.assertIn(f"jugcraft:{breed}", saddle, "26.3 equips a saddle only on tagged types")
        self.assertEqual(values("minecraft/tags/entity_type/powder_snow_walkable_mobs.json"), ["jugcraft:icelandic_horse"])
        self.assertEqual(sorted(values("minecraft/tags/entity_type/freeze_immune_entity_types.json")),
                         ["jugcraft:icelandic_horse", "jugcraft:mongolian_horse"])
        self.assertIn("minecraft:dirt_path", values("jugcraft/tags/block/horse_roads.json"))

    def test_generated_java_matches_the_rig_and_sizes(self):
        meshes = (ROOT / "src/client/java/io/github/jimbozoomer/jugcraft/client/horses/BreedHorseMeshes.java").read_text(encoding="utf-8")
        dims = (ROOT / "src/main/java/io/github/jimbozoomer/jugcraft/horses/BreedDimensions.java").read_text(encoding="utf-8")
        for breed in BREEDS:
            self.assertIn(f'case "{breed}/main"', meshes)
            self.assertIn(f'case "{breed}/saddle"', meshes)
            self.assertIn(f'case "{breed}" -> new Size(', dims)
        self.assertIn('case "pegasus/saddlebags"', meshes)
        rig = horse_breeds.rig("giant_finnhorse")
        self.assertTrue(0 < rig["body_stand_lift"] <= 8 and 0 <= rig["hind_leg_stand_z"] <= 3, "the giant's rearing override stays bounded")


if __name__ == "__main__":
    unittest.main()
