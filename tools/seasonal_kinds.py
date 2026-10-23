"""The seasonal and fall systems' reading of the biomes tools/biomes.py does not hold (docs/features/seasonal-biome-audit.md).

The landscape collection (tools/landscape_content.py), the Alpine Spawn, the caverns and the custom dimensions came after
the seasons and the fall content, and are in no vanilla or conventional biome tag, so every system that picks its biomes
by tag left them out. This table is the audit: every one of them is here, with
- whether it has four seasons (its grass and foliage turn with the date: #jugcraft:has_seasons, tools/seasons.py);
- whether winter rain falls there as snow (#jugcraft:has_winter_snow), for the seasonal ones;
- its kinds, the conventional biome tags it would be in (c:is_forest, c:is_taiga, ...): written as
  #jugcraft:seasonal_kinds/is_<kind>, which only the seasonal and fall systems read (the werewolves and the Howling
  Totem, the squirrels, the turkeys, the Hag Stone, the fall and Halloween wild patches, wolfsbane, the graveyard
  flowers and the wisps). Ores, raider camps, the other wild crops and the Menagerie's wildlife still read only the
  conventional tags; folding these kinds into those is the owner's choice (the record's open questions).

A biome that shares its name with one of tools/biomes.py takes that biome's seasons and its kinds from its tags, so the
two read alike. The rest follow the seasons' rule (tools/seasons.py): four seasons in temperate grassland, forest, taiga,
hills and swamps; not the tropics, the dry lands, the always-frozen lands, waters and shores, caves, the End and the
Nether, nor the biomes whose look is a fixed colour (the mystical ones, the Alpha islands, the four Skylands, each of which
is one season already). Mild-wintered seasonal biomes keep their colours but get no snow.

tools/check_mod_data.py holds the generated tags to this table, and fails on any Jugcraft biome that is in neither
tools/biomes.py nor here, so a new biome collection is classified before it ships.
"""

import biomes

# The conventional kinds the seasonal and fall systems read (agriculture/JugcraftAgriculture.java fallPatch, Wisps,
# and the generated tags below). Each is written as #jugcraft:seasonal_kinds/is_<kind>.
KINDS = ("plains", "forest", "taiga", "birch_forest", "swamp", "spooky", "snowy", "floral", "savanna", "jungle", "hill",
         "windswept", "river", "badlands")
FOLDER = "seasonal_kinds"


def tag(kind):
    return f"jugcraft:{FOLDER}/is_{kind}"


def seasonal(*kinds):
    """Four seasons, and snow in winter."""
    return {"kinds": kinds, "seasons": True, "winter_snow": True}


def mild(*kinds):
    """Four seasons' colours, but a mild winter: no snow."""
    return {"kinds": kinds, "seasons": True, "winter_snow": False}


def fixed(*kinds):
    """The same all year."""
    return {"kinds": kinds, "seasons": False, "winter_snow": False}


# The fall and Halloween wild patches (agriculture/JugcraftAgriculture.java fallPatch): the newer biomes of their kinds
# get them too. Every other wild crop keeps to the conventional tags (wildPatch).
FALL_PATCHES = ("wild_mandrake", "butternut_squash", "acorn_squash", "warty_gourd", "cranberry_bush", "chestnut_tree",
                "white_pumpkin", "jarrahdale_pumpkin", "cinderella_pumpkin", "red_kuri_pumpkin", "kabocha_pumpkin",
                "bottle_gourd", "mums", "chanterelle", "porcini", "puffball", "fly_agaric", "jack_o_lantern_mushroom",
                "wolfsbane")

# The landscape biomes that share a name with one of tools/biomes.py: they read as that biome does.
SHARED = ("bayou", "bog", "cold_desert", "coniferous_forest", "dead_forest", "dryland", "dune_beach", "field", "floodplain",
          "gravel_beach", "highland", "hot_springs", "lavender_field", "lush_desert", "lush_savanna", "maple_woods", "marsh",
          "mediterranean_forest", "muskeg", "orchard", "prairie", "rainforest", "redwood_forest", "scrubland",
          "seasonal_forest", "shield", "shrubland", "snowy_coniferous_forest", "spider_nest", "steppe", "subtropics",
          "tropics", "tundra", "volcano", "wasteland", "wetland", "woodland")


def shared(name):
    info = biomes.BIOMES[name]
    tags = biomes.biome_tags(name)
    kinds = tuple(k for k in KINDS if f"c:is_{k}" in tags or f"minecraft:is_{k}" in tags)
    return {"kinds": kinds, "seasons": bool(info["seasons"]), "winter_snow": bool(info["seasons"]) and info.get("winter_snow", True)}


# The rest of the landscape collection (jugcraft:landscape_<name>), by hand, from each biome's climate (its JSON's
# temperature, downfall and precipitation) and what grows there.
LANDSCAPE = {
    # Temperate grassland, forest, taiga, hills, cliffs and swamps: four seasons.
    "alpine_highlands": seasonal("hill", "plains"),
    "basalt_cliffs": seasonal("hill"),
    "birch_taiga": seasonal("taiga", "birch_forest"),
    "blooming_plateau": seasonal("plains", "floral", "hill"),
    "blooming_valley": seasonal("plains", "floral"),
    "caldera": seasonal("forest", "hill"),
    "cloud_forest": seasonal("forest", "hill"),
    "crag": seasonal("hill", "windswept"),
    "fir_clearing": seasonal("taiga"),
    "forested_field": seasonal("forest", "plains"),
    "forested_highlands": seasonal("forest", "hill"),
    "granite_cliffs": seasonal("hill"),
    "haze_mountain": seasonal("forest", "hill"),
    "highlands": seasonal("hill", "plains"),
    "lush_valley": seasonal("plains", "forest"),
    "moor": seasonal("plains", "hill"),
    "old_growth_dead_forest": seasonal(),
    "old_growth_woodland": seasonal("forest"),
    "ominous_woods": seasonal("forest", "spooky"),
    "origin_valley": seasonal("plains"),
    "overgrown_greens": seasonal("forest"),
    "pasture": seasonal("plains"),
    "pumpkin_patch": seasonal("forest", "spooky"),
    "rocky_shrubland": seasonal("plains"),
    "sakura_grove": seasonal("forest", "floral"),
    "sakura_valley": seasonal("plains", "floral"),
    "shield_clearing": seasonal("hill"),
    "siberian_grove": seasonal("taiga"),
    "siberian_taiga": seasonal("taiga"),
    "temperate_highlands": seasonal("hill", "plains"),
    "valley_clearing": seasonal("plains"),
    "white_cliffs": seasonal("hill"),
    "windswept_spires": seasonal("windswept", "hill"),
    "yellowstone": seasonal("taiga"),
    "yosemite_cliffs": seasonal("hill", "taiga"),
    "yosemite_lowlands": seasonal("taiga", "forest"),
    # The always-frozen lands: snowy all year.
    "alpha_islands_winter": fixed("snowy", "plains"),
    "alpine_grove": fixed("snowy", "taiga"),
    "auroral_garden": fixed("snowy"),
    "cold_shrubland": fixed("snowy", "plains"),
    "emerald_peaks": fixed("snowy"),
    "frozen_cliffs": fixed("snowy"),
    "glacial_chasm": fixed("snowy"),
    "gravel_desert": fixed("snowy"),
    "ice_marsh": fixed("snowy", "swamp"),
    "snowblossom_grove": fixed("snowy", "floral"),
    "snowy_badlands": fixed("snowy", "badlands"),
    "snowy_cherry_grove": fixed("snowy", "floral"),
    "snowy_fir_clearing": fixed("snowy", "taiga"),
    "snowy_maple_forest": fixed("snowy", "forest"),
    "snowy_maple_woods": fixed("snowy", "forest"),
    "snowy_shield": fixed("snowy", "taiga"),
    "wintry_forest": fixed("snowy", "forest"),
    "wintry_lowlands": fixed("snowy", "plains"),
    "wintry_origin_valley": fixed("snowy", "plains"),
    # The tropics: jungles, rainforests and warm swamps.
    "amethyst_rainforest": fixed("jungle"),
    "fungal_jungle": fixed("jungle"),
    "jade_cliffs": fixed("jungle", "hill"),
    "jungle_mountains": fixed("jungle", "hill"),
    "orchid_swamp": fixed("swamp", "jungle"),
    "rocky_jungle": fixed("jungle"),
    "rocky_rainforest": fixed("jungle"),
    "tropical_jungle": fixed("jungle"),
    # The dry lands: deserts, savannas, badlands, mesas and canyons.
    "ancient_sands": fixed(),
    "arid_highlands": fixed("savanna", "hill"),
    "ashen_savanna": fixed("savanna"),
    "brushland": fixed("savanna"),
    "bryce_canyon": fixed("badlands"),
    "desert_canyon": fixed(),
    "desert_oasis": fixed(),
    "desert_spires": fixed(),
    "fractured_savanna": fixed("savanna"),
    "hot_shrubland": fixed("savanna"),
    "red_oasis": fixed(),
    "rocky_mountains": fixed("hill"),
    "sandstone_valley": fixed(),
    "savanna_badlands": fixed("savanna", "badlands"),
    "savanna_slopes": fixed("savanna", "hill"),
    "stony_spires": fixed("hill"),
    "wasteland_steppe": fixed(),
    "white_mesa": fixed("badlands"),
    # A fixed colour is the look: the mystical biomes, the Alpha islands and the Skylands (each is one season already).
    "alpha_islands": fixed("plains"),
    "amethyst_canyon": fixed(),
    "lavender_forest": fixed("forest", "floral"),
    "lavender_valley": fixed("plains", "floral"),
    "mirage_isles": fixed(),
    "moonlight_grove": fixed("forest"),
    "moonlight_valley": fixed("plains"),
    "mystic_grove": fixed("forest"),
    "painted_mountains": fixed(),
    "scarlet_mountains": fixed(),
    "skylands_autumn": fixed("forest"),
    "skylands_spring": fixed("forest"),
    "skylands_summer": fixed("forest"),
    "skylands_winter": fixed("snowy", "forest"),
    "warped_mesa": fixed(),
    # Volcanic land.
    "volcanic_crater": fixed(),
    "volcanic_peaks": fixed(),
    "volcanic_plains": fixed(),
    # Waters and shores.
    "deep_warm_ocean": fixed(),
    "warm_river": fixed("river"),
    # Underground in the Overworld.
    "glowing_grotto": fixed(),
    # The End and the Nether (tools/biomes.py OWNER_END_BIOMES and OWNER_NETHER_POINTS).
    "crystalline_chasm": fixed(),
    "end_corruption": fixed(),
    "end_flats": fixed(),
    "end_reef": fixed(),
    "end_wilds": fixed(),
    "erupting_inferno": fixed(),
    "undergrowth": fixed(),
    "visceral_heap": fixed(),
    "withered_abyss": fixed(),
}

# Outside the landscape collection: the start, and the biomes with no sky or no seasons of their own (each lair is its
# own dimension at a fixed hour).
OTHERS = {
    # The start replaced every vanilla meadow, the turkeys' country, and is open grassland (no werewolves at spawn).
    "alpine_spawn": seasonal("plains"),
    "pixel_hollows": fixed(),
    "nouveau_jar": fixed(),
    "cydonian_sanctum": fixed(),
    "cavern_abyssal_chasm": fixed(),
    "cavern_candy_cavity": fixed(),
    "cavern_forlorn_hollows": fixed(),
    "cavern_magnetic_caves": fixed(),
    "cavern_primordial_caves": fixed(),
    "cavern_toxic_caves": fixed(),
    "hollow_acre": fixed(),
    "spindle_loft": fixed(),
    "glacier_hall": fixed(),
    "cinder_kiln": fixed(),
    "lantern_fen": fixed(),
    "crypt_archive": fixed(),
    "foundry_trench": fixed(),
    "moonlit_ring": fixed(),
}
# The landscape collection's caves (jugcraft:cave/landscape_<name>): underground, no seasons, no kinds.
CAVES = ("andesite_caves", "deep_caves", "diorite_caves", "frostfire_caves", "fungal_caves", "granite_caves",
         "infested_caves", "mantle_caves", "thermal_caves", "tuff_caves", "underground_jungle")


def table():
    """Every biome this audit covers, by its full ID: {"kinds", "seasons", "winter_snow"}."""
    out = {}
    for name in SHARED:
        out[f"jugcraft:landscape_{name}"] = shared(name)
    for name, info in LANDSCAPE.items():
        out[f"jugcraft:landscape_{name}"] = info
    for name, info in OTHERS.items():
        out[f"jugcraft:{name}"] = info
    for name in CAVES:
        out[f"jugcraft:cave/landscape_{name}"] = fixed()
    return out


def seasonal_biomes():
    """The biomes here with four seasons (for #jugcraft:has_seasons)."""
    return [biome for biome, info in table().items() if info["seasons"]]


def winter_snow_biomes():
    return [biome for biome, info in table().items() if info["seasons"] and info["winter_snow"]]


def kind_tags():
    """{tag ID: [biome, ...]} for every kind, in table order."""
    out = {tag(kind): [] for kind in KINDS}
    for biome, info in table().items():
        for kind in info["kinds"]:
            out.setdefault(tag(kind), []).append(biome)
    return out
