"""Haunted Midway, decor set 6 (docs/features/haunted-midway.md): the fall fair's midway after dark, in its own livery of
midnight violet and pumpkin orange with gold trim and bone-white boards.

Part 1, the sweet stalls:
- The Cotton-Candy Cart (agriculture/CottonCandyCartBlock): a cart one block across and two up (`size`), its
  cabinet on two spoked wheels with a push handle behind, a steel spinning bowl 53 cm across on its counter and a
  striped canopy on two posts above. An empty hand switches its motor on or off (`lit`, placed off). While it runs, the
  spinner head in the bowl spins (client/CottonCandyCartRenderer, `spin` degrees a tick about the bowl's middle,
  `bowl`) and its heater glows. Sugar used on it while it runs is spun into floss on the bowl's wall, one serving a
  sugar, up to `floss`; a Paper used on it winds a serving onto a paper cone: a Cotton Candy (a food, ITEMS). Broken, it
  gives back a sugar for each serving left in the bowl.
- The Caramel-Corn Wagon (PopcornWagonBlock): an old popcorn wagon one block across and two up, a cabinet on two big
  spoked wheels under a glass case, a steel kettle hung in the case on a pivot, a roof with a cresting board and a
  jack-o'-lantern on top. Lit with flint and steel or a fire charge (a shovel puts it out), its case lamp and the
  lantern glow (`light`, from its upper block). Corn kernels used on it while it is lit go in the kettle, one at a time:
  after `pop_ticks` they pop, and the kettle tips forward `tip` degrees and back over `tip_ticks`
  (client/PopcornWagonRenderer, about its pivot `pivot`, height and depth), spilling the popcorn into the case, one
  Popcorn a kernel (as smelting gives), up to `capacity`. An empty hand takes a Popcorn out; Caramel turns two of them
  into a Caramel Popcorn (the crafting recipe's own, tools/menu.py). Broken, it gives back the popcorn in its case and a
  kernel still in its kettle. Neither stall burns fuel.
Sizes in pixels, facing north (its back to the south).
"""

COTTON_CANDY = {"block": "cotton_candy_cart", "display": "Cotton-Candy Cart", "size": (1, 2, 1), "floss": 3, "spin": 18.0,
                "bowl": (8.0, 8.25), "apothem": 4.25, "counter": 12.75}
CANDY = {"item": "cotton_candy", "display": "Cotton Candy"}
POPCORN_WAGON = {"block": "popcorn_wagon", "display": "Caramel-Corn Wagon", "size": (1, 2, 1), "capacity": 8, "pop_ticks": 60,
                 "tip": 40.0, "tip_ticks": 20, "pivot": (21.0, 8.0), "light": 10, "counter": 13.75}

# Cotton Candy: spun sugar on a paper cone, light as air and gone in a moment (a candy-corn's worth of food).
ITEMS = {
    CANDY["item"]: {"display": CANDY["display"], "food": [2, 0.1], "compost": "low", "tags": ["c:foods/candy"]},
}

RECIPES = [
    {"id": COTTON_CANDY["block"], "pattern": ["WWW", "IBI", "PSP"],
     "key": {"W": "minecraft:purple_wool", "I": "minecraft:iron_ingot", "B": "minecraft:bucket", "P": "#minecraft:planks",
             "S": "minecraft:stick"},
     "result": COTTON_CANDY["block"], "count": 1, "category": "misc"},
    {"id": POPCORN_WAGON["block"], "pattern": ["GJG", "GCG", "PIP"],
     "key": {"G": "minecraft:glass_pane", "J": "minecraft:jack_o_lantern", "C": "minecraft:cauldron", "P": "#minecraft:planks",
             "I": "minecraft:iron_ingot"},
     "result": POPCORN_WAGON["block"], "count": 1, "category": "misc"},
]
SHAPELESS = []


def blocks():
    return [COTTON_CANDY["block"], POPCORN_WAGON["block"]]


def items():
    """The stalls (the Cotton Candy is in ITEMS)."""
    return blocks()
