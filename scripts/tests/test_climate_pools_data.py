"""Climate bands' biome data: every Overworld biome has exactly one category, and every pool fits its zone.

The Overworld's biomes are worked out from the repository's own metadata, as the game assembles them: vanilla 26.3's
biomes (tools/biome_bases.py) without the Nether's and End's, the region rules' biomes (tools/biomes.py), the
Landscape catalogue's Overworld entries and CaveWorkshop's caverns (menagerie/LandscapeBiomes), and the biomes the
Overworld builder mixin adds itself (Alpine Spawn, the Pixel Hollows, Adam-ondi-Ahman). There is no default category:
a new Overworld biome missing from every #jugcraft:climate/category/* tag fails here.
"""
import json
from pathlib import Path
import re
import sys
import unittest

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))
import biome_bases  # noqa: E402
import biomes  # noqa: E402

DATA = ROOT / "src/main/resources/data"
BIOME_TAGS = DATA / "jugcraft/tags/worldgen/biome"
CLIMATE = BIOME_TAGS / "climate"
ZONES = ("frigid", "hot", "temperate")
POOLED = ("land", "ocean", "deep_ocean", "river", "beach", "mountain")
CATEGORIES = ("preserve",) + POOLED
WATER = {"ocean", "deep_ocean", "river"}
VANILLA_ELSEWHERE = {"basalt_deltas", "crimson_forest", "nether_wastes", "soul_sand_valley", "warped_forest",
                     "end_barrens", "end_highlands", "end_midlands", "small_end_islands", "the_end"}
# Vanilla biome data with a frozen temperature modifier (not recorded in tools/biome_bases.py).
VANILLA_FROZEN = {"minecraft:frozen_ocean", "minecraft:deep_frozen_ocean"}
LANDSCAPE_NETHER = {"erupting_inferno", "visceral_heap", "withered_abyss", "undergrowth", "crystalline_chasm"}
# Snow falls (and water freezes) below this temperature (Biome.warmEnoughToRain).
SNOW_BELOW = 0.15


def tag_values(path):
    return json.loads(path.read_text())["values"]


def explicit(path):
    """A tag's plain biome IDs (no tag references, no optional objects)."""
    return [v for v in tag_values(path) if isinstance(v, str) and not v.startswith("#")]


def landscape_entries():
    return json.loads((ROOT / "src/main/resources/jugcraft/menagerie/landscape.json").read_text())["biomes"]


def caverns():
    source = (ROOT / "src/main/java/io/github/jimbozoomer/jugcraft/menagerie/CaveWorkshop.java").read_text()
    names = re.search(r"CAVES=List\.of\(([^)]*)\)", source).group(1)
    return {f"jugcraft:cavern_{n.strip().strip(chr(34))}" for n in names.split(",")}


def overworld():
    out = {f"minecraft:{name}" for name in biome_bases.BASES if name not in VANILLA_ELSEWHERE}
    out |= {rule["biome"] for rule in biomes.RULES}
    for entry in landscape_entries():
        if not entry["source"].startswith("end_") and entry["source"] not in LANDSCAPE_NETHER:
            out.add(entry["id"])
    out |= caverns()
    out |= {"jugcraft:alpine_spawn", "jugcraft:pixel_hollows", "jugcraft:adam_ondi_ahman"}
    return out


def climate_of(biome):
    """(temperature, precipitation, frozen modifier) from the biome's data."""
    namespace, path = biome.split(":")
    if namespace == "minecraft":
        base = biome_bases.BASES[path]
        return base["climate"][0], base.get("precipitation", True) is not False, biome in VANILLA_FROZEN
    data = json.loads((DATA / "jugcraft/worldgen/biome" / f"{path}.json").read_text())
    return data["temperature"], data["has_precipitation"], data.get("temperature_modifier") == "frozen"


def downfall(biome):
    namespace, path = biome.split(":")
    if namespace == "minecraft":
        return biome_bases.BASES[path]["climate"][1]
    return json.loads((DATA / "jugcraft/worldgen/biome" / f"{path}.json").read_text())["downfall"]


def category_members():
    out = {c: set(explicit(CLIMATE / "category" / f"{c}.json")) for c in POOLED}
    out["preserve"] = set(explicit(CLIMATE / "preserve.json"))
    return out


def pool(zone, category):
    return tag_values(CLIMATE / zone / f"{category}.json")


class ClimateCategoryTests(unittest.TestCase):
    def setUp(self):
        self.overworld = overworld()
        self.members = category_members()

    def test_every_overworld_biome_has_exactly_one_category(self):
        for biome in sorted(self.overworld):
            found = [c for c in CATEGORIES if biome in self.members[c]]
            self.assertEqual(len(found), 1, f"{biome} is in categories {found}; it needs exactly one")
        self.assertGreaterEqual(len(self.overworld), 300)

    def test_categories_hold_only_overworld_biomes(self):
        for category, ids in self.members.items():
            self.assertFalse(ids - self.overworld, f"{category} holds non-Overworld biomes {sorted(ids - self.overworld)}")

    def test_preserve_category_is_the_preserve_tag(self):
        self.assertEqual(tag_values(CLIMATE / "category/preserve.json"), ["#jugcraft:climate/preserve"])
        optional = [v for v in tag_values(CLIMATE / "preserve.json") if not isinstance(v, str)]
        self.assertEqual(optional, [{"id": "#c:is_cave", "required": False}, {"id": "#c:is_underground", "required": False}])

    def test_every_jugcraft_biome_is_categorized_or_elsewhere(self):
        elsewhere = set(explicit(DATA / "minecraft/tags/worldgen/biome/is_nether.json"))
        elsewhere |= set(explicit(DATA / "minecraft/tags/worldgen/biome/is_end.json"))
        elsewhere |= {e["id"] for e in landscape_entries() if e["source"].startswith("end_") or e["source"] in LANDSCAPE_NETHER}
        for dimension in (DATA / "jugcraft/dimension").glob("*.json"):
            elsewhere |= set(re.findall(r'"biome":\s*"([a-z0-9_:/]+)"', dimension.read_text()))
        categorized = set().union(*self.members.values())
        for file in (DATA / "jugcraft/worldgen/biome").rglob("*.json"):
            biome = "jugcraft:" + file.relative_to(DATA / "jugcraft/worldgen/biome").with_suffix("").as_posix()
            self.assertTrue(biome in categorized or biome in elsewhere,
                            f"{biome} is neither categorized nor a Nether, End or lair biome")
            self.assertFalse(biome in categorized and biome in elsewhere, f"{biome} is categorized but lives elsewhere")

    def test_overworld_tag_members_are_categorized(self):
        categorized = set().union(*self.members.values())
        for biome in explicit(DATA / "minecraft/tags/worldgen/biome/is_overworld.json"):
            self.assertIn(biome, categorized)

    def test_custom_water_and_shores_follow_their_metadata(self):
        """Custom oceans, rivers and beaches are never land: the role tags and Landscape habitats decide."""
        role = {"is_deep_ocean": "deep_ocean", "is_river": "river", "is_beach": "beach"}
        for tag, category in role.items():
            for biome in explicit(DATA / f"minecraft/tags/worldgen/biome/{tag}.json"):
                self.assertIn(biome, self.members[category], f"#{tag} {biome}")
        for biome in explicit(DATA / "minecraft/tags/worldgen/biome/is_ocean.json"):
            self.assertTrue(biome in self.members["ocean"] or biome in self.members["deep_ocean"], biome)
        for entry in landscape_entries():
            source, biome = entry["source"], entry["id"]
            if biome not in self.overworld:
                continue
            if "ocean" in source:
                self.assertIn(biome, self.members["deep_ocean" if "deep" in source else "ocean"], biome)
            elif "river" in source:
                self.assertIn(biome, self.members["river"], biome)
            elif "beach" in source:
                self.assertIn(biome, self.members["beach"], biome)
        for biome in explicit(BIOME_TAGS / "landscape_habitat/mountain.json"):
            self.assertIn(biome, self.members["mountain"], biome)

    def test_every_cave_is_preserved(self):
        caves = set(explicit(BIOME_TAGS / "landscape_caves.json")) | caverns()
        caves |= set(explicit(DATA / "c/tags/worldgen/biome/is_cave.json")) | set(explicit(DATA / "c/tags/worldgen/biome/is_underground.json"))
        caves |= {e["id"] for e in landscape_entries()
                  if e["source"].startswith(("cavern_", "cave/")) or e["source"] in ("glowing_grotto", "spider_nest")}
        caves |= {rule["biome"] for rule in biomes.RULES if rule["replaces"] in ("minecraft:dripstone_caves", "minecraft:lush_caves",
                                                                                 "minecraft:deep_dark")}
        caves |= {"minecraft:dripstone_caves", "minecraft:lush_caves", "minecraft:deep_dark", "minecraft:sulfur_caves"}
        self.assertEqual(caves, self.members["preserve"], "The preserve tag is exactly the cave and underground biomes")
        for biome in ("minecraft:mushroom_fields", "jugcraft:flower_isle", "jugcraft:landscape_alpha_islands",
                      "jugcraft:landscape_mirage_isles"):
            self.assertIn(biome, self.members["land"], f"{biome} is a surface special: remapped as land")


class ClimatePoolTests(unittest.TestCase):
    def setUp(self):
        self.members = category_members()
        self.category = {b: c for c, ids in self.members.items() for b in ids}
        self.seasons = set(explicit(BIOME_TAGS / "has_seasons.json"))
        self.winter_snow = set(explicit(BIOME_TAGS / "has_winter_snow.json"))

    def test_every_pool_is_a_nonempty_list_of_overworld_biomes(self):
        for zone in ZONES:
            for category in POOLED:
                values = pool(zone, category)
                self.assertTrue(values, f"{zone}/{category} is empty")
                self.assertEqual(len(values), len(set(values)), f"{zone}/{category} repeats a biome")
                for biome in values:
                    self.assertIsInstance(biome, str)
                    self.assertFalse(biome.startswith("#"), f"{zone}/{category}: pools list biomes explicitly")
                    self.assertIn(biome, self.category, f"{zone}/{category}: {biome} is not a categorized Overworld biome")
        self.assertEqual(sorted(p.name for p in CLIMATE.iterdir() if p.is_dir()), ["category", "frigid", "hot", "temperate"])
        for zone in ZONES:
            self.assertEqual(sorted(p.stem for p in (CLIMATE / zone).glob("*.json")), sorted(POOLED))

    def test_pools_keep_roles(self):
        """Oceans stay oceans, rivers rivers, beaches beaches; land never becomes water; caves are never targets."""
        allowed = {"ocean": {"ocean", "deep_ocean"}, "deep_ocean": {"ocean", "deep_ocean"}, "river": {"river"},
                   "beach": {"beach"}, "land": {"land", "mountain"}, "mountain": {"land", "mountain"}}
        for zone in ZONES:
            for category in POOLED:
                for biome in pool(zone, category):
                    self.assertIn(self.category[biome], allowed[category], f"{zone}/{category}: {biome} is {self.category[biome]}")

    def test_frigid_pools_are_really_frozen(self):
        """Snow, or no precipitation at all, everywhere at sea level, except vanilla's two frozen oceans.

        The frozen temperature modifier lifts noise patches to 0.2, where it rains and water stays open, so a frigid
        member may carry it only if it is one of those documented vanilla exceptions; every other member is below 0.15
        with no modifier (snow where it has precipitation; cold_desert and landscape_siberian_taiga have none).
        """
        exceptions = {}
        for category in POOLED:
            for biome in pool("frigid", category):
                temperature, _, frozen = climate_of(biome)
                if frozen:
                    exceptions[biome] = category
                else:
                    self.assertLess(temperature, SNOW_BELOW, f"frigid/{category}: {biome} at {temperature} is not cold enough to snow")
                self.assertNotIn(biome, self.seasons, f"frigid/{category}: {biome} has seasons (always-frozen biomes have none)")
                self.assertNotIn(biome, self.winter_snow, f"frigid/{category}: {biome} gets seasonal winter snow, so it is not always frozen")
        self.assertEqual(exceptions, {"minecraft:frozen_ocean": "ocean", "minecraft:deep_frozen_ocean": "deep_ocean"},
                         "Only vanilla's frozen oceans may rain in patches inside the strict belt")
        for biome in ("jugcraft:landscape_cold_shrubland", "jugcraft:landscape_gravel_desert", "jugcraft:landscape_ice_marsh"):
            self.assertNotIn(biome, pool("frigid", "land"), f"{biome} rains in frozen-modifier patches (decision A4)")
            self.assertIn(biome, self.members["land"], f"{biome} keeps its category")

    def test_hot_pools_are_really_hot(self):
        for category in POOLED:
            for biome in pool("hot", category):
                temperature, _, frozen = climate_of(biome)
                self.assertFalse(frozen, f"hot/{category}: {biome} is frozen")
                self.assertGreaterEqual(temperature, 0.5 if category in WATER else 0.8, f"hot/{category}: {biome} at {temperature}")
                self.assertNotIn(biome, self.winter_snow, f"hot/{category}: {biome} gets winter snow")
                self.assertNotIn(biome, self.seasons, f"hot/{category}: {biome} has autumn and winter colours")

    def test_temperate_pools_are_temperate(self):
        for category in POOLED:
            for biome in pool("temperate", category):
                temperature, rains, frozen = climate_of(biome)
                self.assertTrue(SNOW_BELOW <= temperature <= 0.9 and rains and not frozen,
                                f"temperate/{category}: {biome} at {temperature}, rain {rains}, frozen {frozen}")

    def test_pools_run_dry_to_wet(self):
        """ClimateSelection picks humidity groups from the tag order: driest first."""
        for zone in ZONES:
            for category in POOLED:
                falls = [downfall(b) for b in pool(zone, category)]
                self.assertEqual(falls, sorted(falls), f"{zone}/{category} is not ordered dry to wet")


if __name__ == "__main__":
    unittest.main()
