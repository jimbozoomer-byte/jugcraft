"""Thanksgiving, the pilgrim homestead and feast room (docs/features/thanksgiving-homestead.md): the furniture and
household things of a seventeenth-century colonial keeping room, set beside the feast table of the Thanksgiving
decorations (tools/thanksgiving_decor.py). Models and textures: tools/thanksgiving_homestead_data.py, built from boxes
at one texel to a model pixel and sharing the feast props' woods, china, linen, leaf and produce painters.

Each candidate on the owner's list was checked against what Jugcraft already has before anything was built (the record
lists every one with its decision). Six are new; two existing pieces are refined in place, keeping their IDs, states,
shapes and behaviour (REFINED): the Quilt Rail's quilt now drapes in folds with a bound hem, and the Butter Churn is a
tapered, three-hooped stave churn with a splash cup, still the working churn it was.

Every piece is decoration, craftable from the start and kept all year: nothing asks for research or follows the
calendar. Each is registered by JugcraftAgriculture.registerThanksgivingHomestead() (the Java must match KINDS and the
shapes below: scripts/tests/test_thanksgiving_homestead.py):
- "furniture": a HomesteadFurnitureBlock, a MultiDecorationBlock `size` blocks (across, up, deep) placed and broken as
  one, dropping once from its first block; `shapes` are its outline boxes in pixels in its frame, facing north;
- "settle": the same, a HomesteadSettleBlock, whose two lower blocks are seats (Seat; `seat` is the height sat at);
- "floor": a FacingDecorationBlock on the floor or a table, facing whoever places it, its outline `shape` ({x0, y0, z0,
  x1, y1, z1} in pixels, facing north);
- "candles": a CandleCenterpieceBlock, lit by flint and steel or a fire charge, snuffed by an empty hand
  (CandleLighting), giving `light` while lit;
- "wall": a WallDecorationBlock hung on the side of a solid block (`depth` out from it, `low` to `high` across and up),
  falling when its wall goes.
Every recipe follows the agriculture feature switch.
"""

FEATURE = "agriculture"

PROPS = {
    "oak_settle": {"kind": "settle", "display": "High-Backed Oak Settle", "size": (2, 2, 1), "seat": 8.5,
                   "shapes": [[1.5, 0.0, 3.5, 30.5, 8.5, 14.0], [1.5, 0.0, 12.5, 30.5, 25.5, 15.5], [0.0, 0.0, 3.0, 2.0, 13.0, 15.5],
                              [30.0, 0.0, 3.0, 32.0, 13.0, 15.5], [0.0, 13.0, 8.0, 1.5, 25.5, 15.5], [30.5, 13.0, 8.0, 32.0, 25.5, 15.5],
                              [0.0, 25.5, 10.0, 32.0, 27.5, 15.75], [11.0, 27.5, 11.5, 21.0, 29.5, 14.5]],
                   "color": "WOOD", "sound": "WOOD", "strength": 2.0},
    "carved_dish_dresser": {"kind": "furniture", "display": "Carved Dish Dresser", "size": (2, 2, 1),
                            "shapes": [[0.25, 0.0, 6.75, 31.75, 13.0, 16.0], [0.0, 13.0, 6.5, 32.0, 14.0, 16.0],
                                       [0.5, 14.0, 10.5, 31.5, 30.0, 16.0], [8.0, 14.0, 7.0, 24.0, 18.5, 10.5],
                                       [0.0, 30.0, 9.5, 32.0, 32.0, 16.0]],
                            "color": "WOOD", "sound": "WOOD", "strength": 2.0},
    "pilgrim_peg_rail": {"kind": "wall", "display": "Pilgrim Peg Rail", "depth": 8.5, "low": 0.5, "high": 15.5,
                         "color": "COLOR_BROWN", "sound": "WOOL", "strength": 0.6},
    "pewter_pitcher_and_basin": {"kind": "floor", "display": "Pewter Pitcher and Basin", "shape": [1.5, 0.0, 1.5, 14.5, 9.0, 14.5],
                                 "color": "METAL", "sound": "LANTERN", "strength": 0.8},
    "pewter_chamberstick": {"kind": "candles", "display": "Pewter Chamberstick", "light": 10,
                            "shape": [3.5, 0.0, 3.5, 15.0, 10.5, 12.5], "color": "METAL", "sound": "LANTERN", "strength": 0.5},
    "harvest_sampler": {"kind": "wall", "display": "Embroidered Harvest Sampler", "depth": 2.5, "low": 1.0, "high": 15.0,
                        "color": "WOOL", "sound": "WOOD", "strength": 0.4},
}
# Existing IDs refined in place: their registration, states, shapes, recipes, loot and behaviour are unchanged; only
# their models and textures are redrawn (tools/thanksgiving_homestead_data.py quilt_rail and churn, called from the
# builders that own them, tools/fall_garden_data.py and tools/barn_tools_data.py).
REFINED = ("quilt_rail", "butter_churn")
# What breaks each quickest (block tag minecraft:mineable/...): pewter a pickaxe, wood and cloth an axe.
PICKAXE = ("pewter_pitcher_and_basin", "pewter_chamberstick")
KINDS = {"settle": "HomesteadSettleBlock", "furniture": "HomesteadFurnitureBlock", "floor": "FacingDecorationBlock",
         "candles": "CandleCenterpieceBlock", "wall": "WallDecorationBlock"}

STICK = "minecraft:stick"
SHAPELESS = [
    {"id": "oak_settle", "inputs": ["minecraft:oak_planks", "minecraft:oak_planks", "minecraft:oak_planks", "minecraft:oak_planks",
                                    "minecraft:oak_slab", "minecraft:oak_slab", "#minecraft:wool"]},
    {"id": "carved_dish_dresser", "inputs": ["#minecraft:planks", "#minecraft:planks", "#minecraft:planks", "#minecraft:wooden_slabs",
                                             "#minecraft:wooden_slabs", "jugcraft:tin_ingot", "minecraft:clay_ball", "minecraft:clay_ball"]},
    {"id": "pilgrim_peg_rail", "inputs": ["#minecraft:wooden_slabs", STICK, "minecraft:black_wool", "#minecraft:wool", "jugcraft:flax"]},
    {"id": "pewter_pitcher_and_basin", "inputs": ["jugcraft:tin_ingot", "jugcraft:tin_ingot", "jugcraft:tin_nugget", "jugcraft:flax"]},
    {"id": "pewter_chamberstick", "inputs": ["jugcraft:tin_ingot", "jugcraft:tin_nugget", "minecraft:candle"]},
    {"id": "harvest_sampler", "inputs": [STICK, STICK, STICK, STICK, "jugcraft:flax", "minecraft:string", "minecraft:orange_dye",
                                         "minecraft:green_dye"]},
]
for _recipe in SHAPELESS:
    _recipe.setdefault("result", _recipe["id"])
    _recipe.setdefault("count", 1)
    _recipe.setdefault("category", "building")
SHAPED = []


def blocks():
    return list(PROPS)


def items():
    return blocks()


def names():
    return {f"block.jugcraft.{block}": spec["display"] for block, spec in PROPS.items()}


def java_box(values):
    """A shape as JugcraftAgriculture writes it: new double[] {x0, y0, z0, x1, y1, z1}."""
    return "new double[] {" + ", ".join(f"{float(v)}" for v in values) + "}"


def cells(size):
    """A multi-block piece's blocks in part order, as MultiDecorationBlock counts them (decor17_data.cells)."""
    w, h, d = size
    return [(r, u, a) for a in range(d) for u in range(h) for r in range(w)]


MAP_COLORS = {"WOOD": "MapColor.WOOD", "COLOR_BROWN": "MapColor.COLOR_BROWN", "METAL": "MapColor.METAL", "WOOL": "MapColor.WOOL"}
SOUNDS = {"WOOD": "SoundType.WOOD", "WOOL": "SoundType.WOOL", "LANTERN": "SoundType.LANTERN"}


def java_call(block):
    """The registerBlock call JugcraftAgriculture.registerThanksgivingHomestead() must make for `block`, up to its
    properties (the shapes and kind, from PROPS)."""
    spec = PROPS[block]
    kind = spec["kind"]
    if kind in ("settle", "furniture"):
        boxes = ",\n\t\t\t\t".join(java_box(b) for b in spec["shapes"])
        return f'registerBlock("{block}", props -> new {KINDS[kind]}(props,\n\t\t\t\t{boxes})'
    if kind in ("floor", "candles"):
        return f'registerBlock("{block}", props -> new {KINDS[kind]}(props, {java_box(spec["shape"])})'
    return f'registerBlock("{block}", props -> new {KINDS[kind]}(props, {spec["depth"]}, {spec["low"]}, {spec["high"]})'


def java_properties(block):
    """Its block properties as JugcraftAgriculture writes them."""
    spec = PROPS[block]
    kind = spec["kind"]
    colour, sound, strength = MAP_COLORS[spec["color"]], SOUNDS[spec["sound"]], f'{spec["strength"]}F'
    if kind in ("settle", "furniture"):
        return (f"BlockBehaviour.Properties.of().mapColor({colour}).strength({strength}).sound({sound}).noOcclusion().ignitedByLava()\n"
                "\t\t\t\t\t\t.pushReaction(PushReaction.IMMOVEABLE)")
    out = f"decorProperties({colour}, {sound}, {strength})"
    if block not in PICKAXE:
        out += ".ignitedByLava()"
    if kind == "wall":
        out += ".noCollision()"
    if kind == "candles":
        out += "\n\t\t\t\t\t\t.lightLevel(state -> state.getValue(CandleCenterpieceBlock.LIT) ? PEWTER_CHAMBERSTICK_LIGHT : 0)"
    return out


def java_registration():
    """The whole of JugcraftAgriculture.registerThanksgivingHomestead() and its light constant, as the shared hook adds
    them (scripts/tests/test_thanksgiving_homestead.py checks the file holds it)."""
    lines = ["\t/** The Pewter Chamberstick's light while lit (tools/thanksgiving_homestead.py PROPS). */",
             f"\tpublic static final int PEWTER_CHAMBERSTICK_LIGHT = {PROPS['pewter_chamberstick']['light']};",
             "",
             "\t/**",
             "\t * The Thanksgiving homestead and feast room (tools/thanksgiving_homestead.py; docs/features/thanksgiving-homestead.md):",
             "\t * the High-Backed Oak Settle (two seats) and the Carved Dish Dresser, each two blocks across and two up, placed and",
             "\t * broken as one; the Pewter Pitcher and Basin and the Pewter Chamberstick (lit by flint and steel, snuffed by hand);",
             "\t * the Pilgrim Peg Rail and the Embroidered Harvest Sampler on the wall. Decorations, craftable from the start and kept",
             "\t * all year; nothing here follows the calendar or asks for research.",
             "\t */",
             "\tprivate static void registerThanksgivingHomestead() {",
             "\t\tList<Block> homestead = new ArrayList<>();"]
    for block in PROPS:
        lines.append(f"\t\thomestead.add({java_call(block)},")
        lines.append(f"\t\t\t\t{java_properties(block)}));")
    lines += ["\t\tfor (Block block : homestead) {",
              "\t\t\tregisterItem(BuiltInRegistries.BLOCK.getKey(block).getPath(), props -> new BlockItem(block, props),",
              "\t\t\t\t\tnew Item.Properties().useBlockDescriptionPrefix(), BUILDING_TAB);",
              "\t\t}",
              "\t}"]
    return "\n".join(lines) + "\n"
