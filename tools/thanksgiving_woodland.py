"""Woodland harvest crafts (docs/features/thanksgiving-woodland-crafts.md): everyday harvest and craft pieces of the
Thanksgiving package, inspired by Wampanoag and wider Northeastern Woodlands everyday forms (twined baskets, a bark
box, a rush mat, carved bowls and spoons, an incised earthenware cooking pot, a corn drying frame and a wooden corn
mortar). They are inspired game decor, not historical reconstructions; the record's source notes say which form each
piece follows and what the cited sources do and do not show. No ceremonial, sacred or regalia item is made here.

Models and textures: tools/thanksgiving_woodland_data.py, built from boxes at one texel to a model pixel and sharing the
Harvest Festival Grounds' braid, the Pumpkin Patch Market's wood, strap and wrap painters and the Thanksgiving
decorations' palettes. Every piece is decoration: none stores, crafts, cooks or grinds anything, none follows the
calendar and none asks for research. Registered by WoodlandHarvestCrafts.register() (the Java must match PROPS:
tools/thanksgiving_woodland_check.py):
- "floor": a WoodlandCraftBlock, a FacingDecorationBlock that takes water, stands on the centre of the block below and
  drops when that goes, facing whoever places it, its outline `shape` ({x0, y0, z0, x1, y1, z1} in pixels, facing north);
- "mat": a WoodlandRushMatBlock, a carpet-thin mat (one pixel) whose rushes run the way the player faced; mats side by
  side join into one mat, bound only along its outer edges, and a mat needs a block under it.
Every recipe follows the agriculture feature switch.
"""

FEATURE = "agriculture"

# Each piece: its kind, name, outline (pixels, facing north: its front towards whoever placed it), how it shows on maps,
# sounds and breaks, which tool takes it quickest (block tag minecraft:mineable/<tool>), and whether lava lights it.
PROPS = {
    "lidded_twined_basket": {"kind": "floor", "display": "Lidded Twined Basket", "shape": [2.0, 0.0, 2.0, 14.0, 13.0, 14.0],
                             "color": "SAND", "sound": "GRASS", "strength": 0.5, "tool": "hoe", "burns": True},
    "twined_pack_basket": {"kind": "floor", "display": "Twined Pack Basket", "shape": [3.0, 0.0, 4.5, 13.0, 16.0, 13.5],
                           "color": "SAND", "sound": "GRASS", "strength": 0.5, "tool": "hoe", "burns": True},
    "birch_bark_box": {"kind": "floor", "display": "Birch-Bark Box", "shape": [1.5, 0.0, 3.0, 14.5, 8.0, 13.0],
                       "color": "QUARTZ", "sound": "WOOD", "strength": 0.5, "tool": "axe", "burns": True},
    "rush_floor_mat": {"kind": "mat", "display": "Rush Floor Mat", "shape": [0.0, 0.0, 0.0, 16.0, 1.0, 16.0],
                       "color": "SAND", "sound": "GRASS", "strength": 0.2, "tool": "hoe", "burns": True},
    "carved_bowls_and_spoons": {"kind": "floor", "display": "Carved Bowls and Spoons", "shape": [0.5, 0.0, 0.0, 15.5, 5.5, 15.0],
                                "color": "WOOD", "sound": "WOOD", "strength": 0.5, "tool": "axe", "burns": True},
    "incised_earthenware_pot": {"kind": "floor", "display": "Incised Earthenware Pot", "shape": [0.0, 0.0, 0.0, 16.0, 15.5, 16.0],
                                "color": "TERRACOTTA_BROWN", "sound": "DECORATED_POT", "strength": 0.6, "tool": "pickaxe",
                                "burns": False},
    "corn_drying_frame": {"kind": "floor", "display": "Corn Drying Frame", "shape": [0.5, 0.0, 5.5, 15.5, 16.0, 10.5],
                          "color": "COLOR_YELLOW", "sound": "WOOD", "strength": 0.5, "tool": "axe", "burns": True},
    "wooden_corn_mortar": {"kind": "floor", "display": "Wooden Corn Mortar", "shape": [1.0, 0.0, 0.0, 14.0, 16.0, 15.0],
                           "color": "WOOD", "sound": "WOOD", "strength": 1.0, "tool": "axe", "burns": True},
}
KINDS = {"floor": "WoodlandCraftBlock", "mat": "WoodlandRushMatBlock"}
TOOLS = ("axe", "hoe", "pickaxe")
# How many of the mat one recipe makes.
MAT_COUNT = 4

# The information page JEI shows for each piece (jei.jugcraft.woodland.<id>): what it is, what it follows, and that it is
# decoration only. "Inspired game decor, not a historical reconstruction" is said on every page.
INFO = {
    "lidded_twined_basket": "A lidded storage basket of open twining, its weft rows twisted round cord warps, with a braided "
                            "lid edge and knob. Inspired by Wampanoag basketry; inspired game decor, not a historical "
                            "reconstruction. Decoration only: it holds nothing.",
    "twined_pack_basket": "A twined carrying basket with shoulder straps sewn on, set down with a few ears of corn in it. "
                          "Inspired by Wampanoag carrying baskets; inspired game decor, not a historical reconstruction. "
                          "Decoration only: it holds nothing.",
    "birch_bark_box": "A lidded box of folded birch bark, its corners and lid edge sewn with root. A plain everyday "
                      "Northeastern Woodlands form; inspired game decor, not a historical reconstruction. Decoration only: "
                      "it holds nothing.",
    "rush_floor_mat": "A thin mat of rushes twined together with cord. Mats laid side by side join into one mat, bound "
                      "only round its outer edge; walk on it like a carpet. Inspired game decor, not a historical "
                      "reconstruction.",
    "carved_bowls_and_spoons": "A carved wooden bowl of samp with a spoon resting in it, a small carved bowl with a spoon "
                               "across it and a shallow twined basket of cranberries: a table display of everyday "
                               "Northeastern Woodlands forms. Inspired game decor, not a historical reconstruction. "
                               "Decoration only.",
    "incised_earthenware_pot": "A round-bodied earthenware cooking pot set among hearthstones over a cold bed of ash, its "
                               "rim rising in four peaks over an incised band. Inspired by Wampanoag pottery; inspired "
                               "game decor, not a historical reconstruction. Decoration only: it does not cook.",
    "corn_drying_frame": "A frame of forked poles lashed with bark cord, hung with ears of corn braided by their husks. "
                         "Unlike the Drying Rack it dries nothing. Inspired game decor, not a historical reconstruction.",
    "wooden_corn_mortar": "A wooden corn mortar hollowed from a log, cracked corn in its bowl and a long stone-ended pestle "
                          "standing in it. Not the alchemy Mortar and Pestle: it grinds nothing. A Northeastern Woodlands "
                          "everyday form; inspired game decor, not a historical reconstruction.",
}

STICK, WHEAT, STRING, FLAX, CORN = "minecraft:stick", "minecraft:wheat", "minecraft:string", "jugcraft:flax", "jugcraft:corn"
# Twined grass is wheat on string warps; bark cord is flax; bark is a birch log's; carved wood is a log; earthenware is
# clay; hearthstones and the pestle's stone are cobblestone; the harvest in them is the farm's corn and cranberries.
SHAPELESS = [
    {"id": "lidded_twined_basket", "inputs": [WHEAT, WHEAT, WHEAT, WHEAT, STRING, STRING]},
    {"id": "twined_pack_basket", "inputs": [WHEAT, WHEAT, WHEAT, STRING, FLAX, FLAX, CORN]},
    {"id": "birch_bark_box", "inputs": ["minecraft:birch_log", STRING, FLAX]},
    {"id": "rush_floor_mat", "inputs": [WHEAT, WHEAT, WHEAT, WHEAT, STRING, FLAX], "count": MAT_COUNT},
    {"id": "carved_bowls_and_spoons", "inputs": ["#minecraft:logs", "minecraft:bowl", "minecraft:bowl", CORN, "jugcraft:cranberries"]},
    {"id": "incised_earthenware_pot", "inputs": ["minecraft:clay_ball", "minecraft:clay_ball", "minecraft:clay_ball", "minecraft:cobblestone",
                                                 "minecraft:cobblestone", STICK]},
    {"id": "corn_drying_frame", "inputs": [STICK, STICK, STICK, FLAX, CORN, CORN, CORN]},
    {"id": "wooden_corn_mortar", "inputs": ["#minecraft:logs", "#minecraft:logs", STICK, "minecraft:cobblestone", CORN]},
]
for _recipe in SHAPELESS:
    _recipe.setdefault("result", _recipe["id"])
    _recipe.setdefault("count", 1)
    _recipe.setdefault("category", "building")
SHAPED = []


def blocks(kind=None):
    return [block for block, spec in PROPS.items() if kind is None or spec["kind"] == kind]


def items():
    return blocks()


def names():
    """Every text this group adds: the blocks' names and their JEI information pages."""
    out = {f"block.jugcraft.{block}": spec["display"] for block, spec in PROPS.items()}
    out.update({f"jei.jugcraft.woodland.{block}": text for block, text in INFO.items()})
    return out


def java_box(values):
    """A shape as WoodlandHarvestCrafts writes it: new double[] {x0, y0, z0, x1, y1, z1}."""
    return "new double[] {" + ", ".join(f"{float(v)}" for v in values) + "}"
