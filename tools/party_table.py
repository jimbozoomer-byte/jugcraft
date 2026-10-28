"""Halloween Party Table, decor set 8 (docs/features/halloween-party-table.md): the spread for a Halloween party, from the
candy to the cake.

Part 1, the candy:
- The Candy Corn Dispenser (agriculture/CandyCornDispenserBlock): a jumbo tabletop gumball machine 86 cm high, its glass
  globe 41 cm across on a black cast-iron pedestal banded in orange enamel, a brass crank and a brass flap over its chute
  on its front, a black fill lid with a brass knob on top. It stands on a table or the floor, facing whoever places it.
  Candy corn used on it pours in, as much as fits, up to `capacity` pieces; the globe shows how full it is (`fill`, 0 for
  empty and 1 to `levels`). An empty hand turns its crank once round in `crank_ticks` ticks (client/CandyCornDispenserRenderer
  turns it about its axle, `crank`, height and depth), and as it comes round one piece drops out of its chute, its flap
  swinging out `flap` degrees about its top (`hinge`, height and depth) and back in `flap_ticks` ticks. It turns again only
  once it has come round, and an empty one only rattles. What it holds is saved with the world, and dropped if it is
  broken.
Part 2, the cake stands (each a table piece: on a Feast Table, whose top is `table_top` pixels up, it is drawn
`table_drop` pixels lower, so it stands on the table rather than above it):
- The Spider Cupcake Tower (SpiderCupcakeTowerBlock): a party cupcake stand 50 cm high, three round tiers of black
  enamelled steel on a centre rod. It holds Spiderweb Cupcakes (`serves`), up to `capacity`:
  eight round the bottom tier, four on the middle and one on top, every third with a licorice spider on its frosting. A
  cupcake used on it goes on, filling it from the bottom tier up; an empty hand takes one off, from the top down.
- The Ghost Meringue Platter (GhostMeringuePlatterBlock): an oval black-glazed platter 35 cm long on a low foot, holding up
  to `capacity` Ghost Meringues (`serves`) in three rows: piped meringue ghosts 9 cm high, their peaks curled over, two
  cocoa eyes on each. A meringue used on it goes on; an empty hand takes one off. What each holds is its block's, dropped
  with it when it is broken.
- The Ghost Meringue (an item): a food, piped from egg white and sugar with cocoa for its eyes, `meringue` (nutrition,
  saturation); six from an egg, two sugar and a cocoa bean. It is candy, so a Candy Bag takes it.
Part 3, the trays (table pieces too, holding a food each as the cake stands do):
- The Mummy Sausage Tray (MummySausageTrayBlock): a black baking tray 40 cm by 28 cm with low sides, a ramekin of mustard
  in its middle, holding up to `capacity` Mummy Sausages in two rows: sausages wound in strips of pastry.
- The Witch-Finger Cauldron (WitchFingerCauldronBlock): a little cast-iron cauldron 25 cm across on three stubby legs,
  filled with cookie crumbs, holding up to `capacity` Witch Fingers poking up out of it at angles, green shortbread with
  an almond nail and a knuckle crease.
- The Eyeball Jelly Mould (EyeballJellyMouldBlock): a white plate on which a ring of red jelly 22 cm across stands turned
  out of its mould, in `capacity` segments round its hole, an eyeball set in each. An Eyeball Jelly used on it sets in
  the next segment; an empty hand takes one.
- Their foods (items): the Mummy Sausage (`food`; four from a cooked porkchop and pastry dough), the Witch Finger (eight,
  as cookies are, from two wheat, sugar and green dye) and the Eyeball Jelly (four from sugar, two sweet berries, bone
  meal for its gelatin and a glow berry for the eyes).
Sizes in pixels, facing north (its front, with the crank, to the north).
"""

DISPENSER = {"block": "candy_corn_dispenser", "display": "Candy Corn Dispenser", "capacity": 64, "levels": 4,
             "crank_ticks": 20, "crank": (3.75, 4.75), "flap": 55.0, "flap_ticks": 12, "hinge": (3.0, 4.75)}

TABLE = {"table_top": 13.0, "table_drop": 3.0}
CUPCAKE_TOWER = {"block": "spider_cupcake_tower", "display": "Spider Cupcake Tower", "serves": "spiderweb_cupcake", "capacity": 13,
                 "tiers": ((6.0, 1.0, 8), (4.5, 3.5, 4), (2.5, 6.0, 1))}
MERINGUE_PLATTER = {"block": "ghost_meringue_platter", "display": "Ghost Meringue Platter", "serves": "ghost_meringue", "capacity": 9}
MERINGUE = {"item": "ghost_meringue", "display": "Ghost Meringue", "meringue": (1, 0.1)}
MUMMY_TRAY = {"block": "mummy_sausage_tray", "display": "Mummy Sausage Tray", "serves": "mummy_sausage", "capacity": 8}
FINGER_CAULDRON = {"block": "witch_finger_cauldron", "display": "Witch-Finger Cauldron", "serves": "witch_finger", "capacity": 10}
JELLY_MOULD = {"block": "eyeball_jelly_mould", "display": "Eyeball Jelly Mould", "serves": "eyeball_jelly", "capacity": 8}
# Their foods; the sausage, being meat, doesn't compost.
FOODS = {"mummy_sausage": {"display": "Mummy Sausage", "food": (3, 0.5), "compost": None, "tags": ["c:foods/cooked_meat"]},
         "witch_finger": {"display": "Witch Finger", "food": (2, 0.1), "compost": "medium_high", "tags": ["c:foods"]},
         "eyeball_jelly": {"display": "Eyeball Jelly", "food": (2, 0.2), "compost": "medium_high", "tags": ["c:foods"]}}

ITEMS = {
    MERINGUE["item"]: {"display": MERINGUE["display"], "food": list(MERINGUE["meringue"]), "compost": "medium_high", "tags": ["c:foods/candy"]},
    **{item: {"display": f["display"], "food": list(f["food"]), **({"compost": f["compost"]} if f["compost"] else {}), "tags": f["tags"]}
       for item, f in FOODS.items()},
}

RECIPES = [
    # The dispenser: a glass globe on a cast-iron pedestal, blacked.
    {"id": DISPENSER["block"], "pattern": ["GGG", "G G", "IBI"],
     "key": {"G": "minecraft:glass", "I": "minecraft:iron_ingot", "B": "minecraft:black_dye"},
     "result": DISPENSER["block"], "count": 1, "category": "building"},
    # The tower: iron nuggets for its tiers and rod, blacked.
    {"id": CUPCAKE_TOWER["block"], "pattern": ["NBN", " N ", "NNN"],
     "key": {"N": "minecraft:iron_nugget", "B": "minecraft:black_dye"},
     "result": CUPCAKE_TOWER["block"], "count": 1, "category": "building"},
    # The platter: clay, glazed black.
    {"id": MERINGUE_PLATTER["block"], "pattern": ["CBC"],
     "key": {"C": "minecraft:clay_ball", "B": "minecraft:black_dye"},
     "result": MERINGUE_PLATTER["block"], "count": 1, "category": "building"},
    # The tray: an iron plate, blacked, with a little bowl for the mustard.
    {"id": MUMMY_TRAY["block"], "pattern": ["NBN", "III"],
     "key": {"N": "minecraft:iron_nugget", "B": "minecraft:bowl", "I": "minecraft:iron_ingot"},
     "result": MUMMY_TRAY["block"], "count": 1, "category": "building"},
    # The cauldron: a small cast pot of iron, on its three legs.
    {"id": FINGER_CAULDRON["block"], "pattern": ["I I", "III", "N N"],
     "key": {"I": "minecraft:iron_ingot", "N": "minecraft:iron_nugget"},
     "result": FINGER_CAULDRON["block"], "count": 1, "category": "building"},
    # The mould's plate: white clay, glazed with bone meal (the jelly is put on it, segment by segment).
    {"id": JELLY_MOULD["block"], "pattern": ["CBC"],
     "key": {"C": "minecraft:clay_ball", "B": "minecraft:bone_meal"},
     "result": JELLY_MOULD["block"], "count": 1, "category": "building"},
]
# Meringue: an egg's white whipped with sugar, piped into ghosts, with cocoa for their eyes.
SHAPELESS = [
    {"id": MERINGUE["item"], "inputs": ["minecraft:egg", "minecraft:sugar", "minecraft:sugar", "minecraft:cocoa_beans"],
     "result": MERINGUE["item"], "count": 6, "category": "misc"},
    # A cooked porkchop cut into four sausages, each wound in pastry.
    {"id": "mummy_sausage", "inputs": ["minecraft:cooked_porkchop", "jugcraft:pastry_dough"], "result": "mummy_sausage", "count": 4,
     "category": "misc"},
    # Shortbread, tinted green, rolled into fingers: eight, as cookies are.
    {"id": "witch_finger", "inputs": ["minecraft:wheat", "minecraft:wheat", "minecraft:sugar", "minecraft:green_dye"], "result": "witch_finger",
     "count": 8, "category": "misc"},
    # Berry jelly set with gelatin from bone, a glow berry for the eyes.
    {"id": "eyeball_jelly", "inputs": ["minecraft:sugar", "minecraft:sweet_berries", "minecraft:sweet_berries", "minecraft:bone_meal",
                                       "minecraft:glow_berries"], "result": "eyeball_jelly", "count": 4, "category": "misc"},
]


def blocks():
    return [DISPENSER["block"], CUPCAKE_TOWER["block"], MERINGUE_PLATTER["block"], MUMMY_TRAY["block"], FINGER_CAULDRON["block"],
            JELLY_MOULD["block"]]


def serving():
    """The table pieces that hold a food."""
    return [CUPCAKE_TOWER, MERINGUE_PLATTER, MUMMY_TRAY, FINGER_CAULDRON, JELLY_MOULD]


def items():
    """The table pieces (the Ghost Meringue is in ITEMS)."""
    return blocks()
