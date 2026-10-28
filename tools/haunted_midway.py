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

Part 2, the ticket booth and the first game:
- The Ticket Booth (TicketBoothBlock): a kiosk one block across and two up, its ticket window glazed over a counter, a
  striped roof and a crest board painted with a ticket. On its counter stands a brass ticket dispenser with a roll of
  paper on top and a crank at its side. Paper used on it loads the roll, `per_sheet` tickets a sheet, up to `roll`;
  an empty hand turns the crank once round (client/TicketBoothRenderer, its runner model about the crank's axle `axle`,
  height and depth, over `crank_ticks`) and prints a Midway Ticket. A ticket is a round's admission, not money: it is
  printed from paper and buys nothing but a round; the mod's money stays the Jug. Broken, the booth gives back a sheet
  for each `per_sheet` tickets left on its roll.
- Whack-a-Zombie (WhackAZombieBlock): a game cabinet one block across and two up, its playfield at 94 cm with five
  holes (`holes`, x and z), a back board with `lamps` score lamps along its top and a crest. A Midway Ticket used on it
  starts a round of `round_ticks` for whoever paid. Square-faced zombie heads `head` pixels high pop up one at a time
  from the holes, rising over `rise_ticks` (client/WhackAZombieRenderer, its runner model), each up for `up_ticks`, with
  `gap_ticks` between; the player whacks one with a Carnival Mallet (left click, or use) to knock it down and light a
  lamp. Lighting all `lamps` wins a prize (the midway's prize table) and ends the round; when the time runs out first,
  the round is lost. Only the player who paid may whack, from within the High Striker's reach.

Part 3, the shooting gallery:
- The Shooting Gallery (ShootingGalleryBlock): a booth three blocks across and two up, a counter at 88 cm across its
  front, a night sky painted on its back wall, and two gold rails across it: on the upper rail `per_rail` tin bats run
  one way, on the lower `per_rail` tin ghosts the other (`rails`: each rail's height, depth, way and target), each
  target `targets` wide and high, at `speed` pixels a tick along `track` (x from and to), the rails' ends hidden behind
  covers so the targets go round out of sight. Its header board across the top is lettered SHOOTING, with `bulbs`
  bulbs under it. A Midway Ticket used on it starts a round of `round_ticks` for whoever paid; their projectiles (an
  arrow, a snowball, anything thrown or shot), from `min_distance` blocks or more, knock down a target where they cross
  its rail (client/ShootingGalleryRenderer draws the targets, standing or knocked flat). Knocking all of them down wins
  a prize (the midway's prize table) and ends the round; when the time runs out first, the round is lost and they stand
  up again. The targets move all the time, by the game's clock, so every client sees them where the server judges them.
Sizes in pixels, facing north (its back to the south).
"""

COTTON_CANDY = {"block": "cotton_candy_cart", "display": "Cotton-Candy Cart", "size": (1, 2, 1), "floss": 3, "spin": 18.0,
                "bowl": (8.0, 8.25), "apothem": 4.25, "counter": 12.75}
CANDY = {"item": "cotton_candy", "display": "Cotton Candy"}
POPCORN_WAGON = {"block": "popcorn_wagon", "display": "Caramel-Corn Wagon", "size": (1, 2, 1), "capacity": 8, "pop_ticks": 60,
                 "tip": 40.0, "tip_ticks": 20, "pivot": (21.0, 8.0), "light": 10, "counter": 13.75}

TICKET_BOOTH = {"block": "ticket_booth", "display": "Ticket Booth", "size": (1, 2, 1), "roll": 32, "per_sheet": 2, "crank_ticks": 10,
                "axle": (16.5, 2.1)}
TICKET = {"item": "midway_ticket", "display": "Midway Ticket"}
WHACK = {"block": "whack_a_zombie", "display": "Whack-a-Zombie", "size": (1, 2, 1), "field": 15.0, "head": 4.0, "lamps": 8,
         "holes": ((3.5, 3.75), (8.0, 3.75), (12.5, 3.75), (5.75, 8.75), (10.25, 8.75)), "round_ticks": 400, "up_ticks": 20, "gap_ticks": 10,
         "rise_ticks": 4}

GALLERY = {"block": "shooting_gallery", "display": "Shooting Gallery", "size": (3, 2, 1), "counter": 14.0, "board": 15.0,
           "rails": ((20.5, 13.5, 1, "bat"), (16.0, 11.5, -1, "ghost")), "targets": {"bat": (6, 3), "ghost": (5, 5)}, "per_rail": 4,
           "track": (2.0, 46.0), "speed": 0.25, "round_ticks": 600, "min_distance": 2.0, "bulbs": 11}

# Cotton Candy: spun sugar on a paper cone, light as air and gone in a moment (a candy-corn's worth of food).
ITEMS = {
    CANDY["item"]: {"display": CANDY["display"], "food": [2, 0.1], "compost": "low", "tags": ["c:foods/candy"]},
}

RECIPES = [
    {"id": COTTON_CANDY["block"], "pattern": ["WWW", "IBI", "PSP"],
     "key": {"W": "minecraft:purple_wool", "I": "minecraft:iron_ingot", "B": "minecraft:bucket", "P": "#minecraft:planks",
             "S": "minecraft:stick"},
     "result": COTTON_CANDY["block"], "count": 1, "category": "misc"},
    {"id": TICKET_BOOTH["block"], "pattern": ["GGG", "PDP", "PPP"],
     "key": {"G": "minecraft:glass_pane", "D": "minecraft:dispenser", "P": "#minecraft:planks"},
     "result": TICKET_BOOTH["block"], "count": 1, "category": "misc"},
    {"id": WHACK["block"], "pattern": ["TTT", "PFP", "PSP"],
     "key": {"T": "minecraft:torch", "F": "minecraft:rotten_flesh", "S": "minecraft:piston", "P": "#minecraft:planks"},
     "result": WHACK["block"], "count": 1, "category": "misc"},
    {"id": GALLERY["block"], "pattern": ["WWW", "PBP", "PPP"],
     "key": {"W": "minecraft:orange_wool", "B": "minecraft:bow", "P": "#minecraft:planks"},
     "result": GALLERY["block"], "count": 1, "category": "misc"},
    {"id": POPCORN_WAGON["block"], "pattern": ["GJG", "GCG", "PIP"],
     "key": {"G": "minecraft:glass_pane", "J": "minecraft:jack_o_lantern", "C": "minecraft:cauldron", "P": "#minecraft:planks",
             "I": "minecraft:iron_ingot"},
     "result": POPCORN_WAGON["block"], "count": 1, "category": "misc"},
]
SHAPELESS = []


def blocks():
    return [COTTON_CANDY["block"], POPCORN_WAGON["block"], TICKET_BOOTH["block"], WHACK["block"], GALLERY["block"]]


def items():
    """The stalls and the ticket (the Cotton Candy is in ITEMS)."""
    return blocks() + [TICKET["item"]]
