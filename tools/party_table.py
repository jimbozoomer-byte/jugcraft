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
Sizes in pixels, facing north (its front, with the crank, to the north).
"""

DISPENSER = {"block": "candy_corn_dispenser", "display": "Candy Corn Dispenser", "capacity": 64, "levels": 4,
             "crank_ticks": 20, "crank": (3.75, 4.75), "flap": 55.0, "flap_ticks": 12, "hinge": (3.0, 4.75)}

ITEMS = {}

RECIPES = [
    # The dispenser: a glass globe on a cast-iron pedestal, blacked.
    {"id": DISPENSER["block"], "pattern": ["GGG", "G G", "IBI"],
     "key": {"G": "minecraft:glass", "I": "minecraft:iron_ingot", "B": "minecraft:black_dye"},
     "result": DISPENSER["block"], "count": 1, "category": "building"},
]
SHAPELESS = []


def blocks():
    return [DISPENSER["block"]]


def items():
    return blocks()
