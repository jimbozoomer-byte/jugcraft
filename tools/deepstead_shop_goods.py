"""The twenty guild counter specialties. Quantities and prices are per purchase."""
STONEWRIGHT = "deepstead_stonewright"
COPPER_KETTLE = "deepstead_copper_kettle"
SHOPS = {STONEWRIGHT: "Stonewright's Hall", COPPER_KETTLE: "The Copper Kettle"}
STONE = (
    ("guild_border_paver", 4, 2, "Guild Border Paver", "A repeatable band of brass chevrons set in black slate."),
    ("archmasters_keystone", 1, 3, "Archmaster's Keystone", "A cut voussoir with a polished carnelian boss."),
    ("scrollwork_corbel", 1, 4, "Scrollwork Corbel", "A carved bracket for doorways and gallery balconies."),
    ("survey_benchmark", 1, 5, "Survey Benchmark", "Use the bronze witness mark to read its coordinates."),
    ("quarry_sample_cabinet", 1, 7, "Quarry Sample Cabinet", "Use to exhibit amethyst, copper or sky geode."),
    ("stonemasons_cutting_bench", 1, 12, "Stonemason's Cutting Bench", "Use the guild bench for standard stonecutting recipes."),
    ("fault_sounding_hammer", 1, 9, "Fault-Sounding Hammer", "Tap a face to sound up to eight blocks inward for a hollow."),
    ("masons_spirit_level", 1, 8, "Mason's Spirit Level", "Use on a floor to check the five positions across your view."),
    ("folding_span_rule", 1, 7, "Folding Span Rule", "Use a face to measure the matching blocks across a course."),
    ("lampwatch_photometer", 1, 6, "Lampwatch Photometer", "Use a face to read sky light and block light beside it."),
)
KETTLE = (
    ("ember_onion_pasty", 2, 3, "Ember Onion Pasty", "Charred onions in crimped rye pastry."),
    ("saltcap_skewers", 3, 3, "Saltcap Skewers", "Griddled mushroom caps threaded on juniper sticks."),
    ("golden_leek_tart", 2, 4, "Golden Leek Tart", "Leek rings and aged cheese in a scalloped crust."),
    ("emberpepper_hotpot", 1, 4, "Emberpepper Hotpot", "An ironstone bowl of peppers, mushrooms and beans."),
    ("caveberry_shrub", 1, 3, "Caveberry Shrub", "A tart cordial that clears nausea."),
    ("glowroot_steep", 1, 4, "Glowroot Steep", "Golden root tea grants 45 seconds of night vision."),
    ("copper_samovar", 1, 8, "Copper Samovar", "Use to light or extinguish its charcoal brazier."),
    ("braided_bread_basket", 1, 4, "Braided Bread Basket", "Two scored loaves in a low, woven serving tray."),
    ("tankard_tree", 1, 5, "Tankard Tree", "A three-armed drying stand hung with banded mugs."),
    ("shift_supper_gong", 1, 7, "Shift-Supper Gong", "Strike the brass gong to call the mine shift to supper."),
)
STOCK = {STONEWRIGHT: STONE, COPPER_KETTLE: KETTLE}
FURNISHINGS = tuple(row[0] for row in (*STONE[:6], *KETTLE[6:]))
TOOLS = tuple(row[0] for row in STONE[6:])
FOODS = {row[0]: family for row, family in zip(KETTLE[:6], ("bread", "meat", "slice", "bowl", "bottle", "bottle"))}
WOOD = {"quarry_sample_cabinet", "braided_bread_basket", "tankard_tree"}
LANG = {
    "message.jugcraft.guild_shop.full": "Make room in your pack for the whole order first.",
    "message.jugcraft.survey_benchmark": "Guild benchmark  ·  %s",
    "message.jugcraft.quarry_sample.0": "Specimen I  ·  Violet amethyst",
    "message.jugcraft.quarry_sample.1": "Specimen II  ·  Native copper",
    "message.jugcraft.quarry_sample.2": "Specimen III  ·  Sky geode",
    "message.jugcraft.fault_sounding_hammer.open": "Hollow after %s solid blocks.",
    "message.jugcraft.fault_sounding_hammer.solid": "Sounded %s solid blocks. No open space found in reach.",
    "message.jugcraft.masons_spirit_level": "Level %s/5  ·  Raised %s  ·  Low %s  ·  Unloaded %s",
    "message.jugcraft.folding_span_rule.at_least": "Course spans at least %s blocks.",
    "message.jugcraft.folding_span_rule.exact": "Course spans %s blocks.",
    "message.jugcraft.lampwatch_photometer": "Light at face  ·  Block %s / 15  ·  Sky %s / 15",
}
for key, _, _, name, tip in (*STONE, *KETTLE):
    LANG[("block" if key in FURNISHINGS else "item") + ".jugcraft." + key] = name
    LANG["tooltip.jugcraft." + key] = tip
