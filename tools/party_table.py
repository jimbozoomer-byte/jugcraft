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
Sizes in pixels, facing north (its front, with the crank, to the north).
"""

DISPENSER = {"block": "candy_corn_dispenser", "display": "Candy Corn Dispenser", "capacity": 64, "levels": 4,
             "crank_ticks": 20, "crank": (3.75, 4.75), "flap": 55.0, "flap_ticks": 12, "hinge": (3.0, 4.75)}

TABLE = {"table_top": 13.0, "table_drop": 3.0}
CUPCAKE_TOWER = {"block": "spider_cupcake_tower", "display": "Spider Cupcake Tower", "serves": "spiderweb_cupcake", "capacity": 13,
                 "tiers": ((6.0, 1.0, 8), (4.5, 3.5, 4), (2.5, 6.0, 1))}
MERINGUE_PLATTER = {"block": "ghost_meringue_platter", "display": "Ghost Meringue Platter", "serves": "ghost_meringue", "capacity": 9}
MERINGUE = {"item": "ghost_meringue", "display": "Ghost Meringue", "meringue": (1, 0.1)}

ITEMS = {
    MERINGUE["item"]: {"display": MERINGUE["display"], "food": list(MERINGUE["meringue"]), "compost": "medium_high", "tags": ["c:foods/candy"]},
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
]
# Meringue: an egg's white whipped with sugar, piped into ghosts, with cocoa for their eyes.
SHAPELESS = [
    {"id": MERINGUE["item"], "inputs": ["minecraft:egg", "minecraft:sugar", "minecraft:sugar", "minecraft:cocoa_beans"],
     "result": MERINGUE["item"], "count": 6, "category": "misc"},
]


def blocks():
    return [DISPENSER["block"], CUPCAKE_TOWER["block"], MERINGUE_PLATTER["block"]]


def serving():
    """The table pieces that hold a food."""
    return [CUPCAKE_TOWER, MERINGUE_PLATTER]


def items():
    """The table pieces (the Ghost Meringue is in ITEMS)."""
    return blocks()
