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

Part 4, the ghost train:
- The Ghost Train (GhostTrainBlock): a dark ride's front, five blocks across and four up, a haunted house's clapboard
  wall lettered GHOST TRAIN under its eaves, two lit windows with ghosts in them and two doorways at track level, IN
  and OUT, two blocks high and one across (`doors`, each doorway's block across: the doorways are not its blocks, so a
  track of rails runs through them). Each doorway has double doors (`leaf` wide and high), hinged at its sides. Every
  `look_ticks` its first block looks for a minecart in each doorway or the block before or after it (`reach` blocks):
  with one there, that doorway's doors swing open (over `swing_ticks`, client/GhostTrainRenderer, the IN doors in, the
  OUT doors out), and stay open until `hold_ticks` after the last cart has gone. As the IN doors open, a ghost springs
  up over them with a cackle (`spook_ticks`). An empty hand lights its windows or puts them out (`light`, from its upper
  blocks). The cars are vanilla minecarts on a track the player lays: the ride itself, behind the front, is theirs to
  build. It takes no ticket.

Part 5, the haunted carousel:
- The Haunted Carousel (HauntedCarouselBlock, its middle; HauntedCarousel, the entity that turns): a travelling
  merry-go-round `width` blocks across and through and `height` up. Its middle, the drive house with its band organ's
  pipes on the front, is its one real block. Placed where it has room, it sets the carousel turning round itself: a deck
  of boards a hand high (`deck` blocks), a mirrored column, a striped canopy on rounding boards hung with bulbs and a
  scalloped valance, and a jack-o'-lantern on top. Round its outer ring (`outer` blocks out), a slot every eighth of a
  turn, stand the Pumpkin Coach and black horses and bats by turns (`mounts`, from slot 0); four more horses stand on its
  inner ring (`inner` blocks out) beside the outer ones (`inner_slots`). The horses and bats ride up and down their brass
  poles `bobs` times a turn, `bob` blocks either way, by turns, and the bats beat their wings as they go; the coach stays
  on the deck. Each mount seats one rider, the coach two side by side (`coach_across` either side of its middle): `seats`
  is each kind's seat over the deck, `seat_back` how far behind its pole. It turns on kinetic power through its middle,
  as the Ferris wheel does: `need` KE a tick turns it at full speed, once round in `turn_ticks`, less slower; it speeds up
  by `accel` of full speed a tick and slows by `decel`. Its power comes up from under its middle (a shaft or source
  beside it would stand in the mounts' way). While it turns, its band organ plays a waltz of its own in E minor, a beat
  every `beat_ticks` (`tune`: each bar's three beats of tune, -1 a rest; `bass`, each bar's first beat; `chord`, its
  second and third; in note-block steps, 0 the F sharp a note block plays unstruck). While it turns or anyone rides, its
  bulbs light and its middle gives light (`light`). An empty hand on its middle boards the free mount nearest the player,
  within `board_reach` blocks of its middle; sneaking, it switches the organ off or on. A rider turns with their mount,
  facing the way it goes; getting off, they are set down on the ground just outside it. A block where a mount is about to
  go stops it, jammed, until it is cleared. It takes no ticket.
Sizes in pixels, facing north (its back to the south), but the carousel's, in blocks.
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

GHOST_TRAIN = {"block": "ghost_train", "display": "Ghost Train", "size": (5, 4, 1), "doors": (1, 3), "leaf": (8.0, 30.0), "wall": (2.0, 5.0),
               "look_ticks": 4, "reach": 1.0, "swing_ticks": 4, "hold_ticks": 20, "spook_ticks": 40, "light": 9}

CAROUSEL = {"block": "haunted_carousel", "display": "Haunted Carousel", "entity": "haunted_carousel", "entity_display": "Haunted Carousel",
            "width": 7, "height": 5, "deck": 0.1875, "outer": 2.55, "inner": 1.45,
            "mounts": ("coach", "horse", "bat", "horse", "bat", "horse", "bat", "horse"), "inner_slots": (1, 3, 5, 7),
            "seats": {"horse": 1.25, "bat": 1.15, "coach": 0.45}, "seat_back": 0.125, "coach_across": 0.33, "bob": 0.2, "bobs": 3,
            "need": 8, "turn_ticks": 240, "accel": 1 / 60, "decel": 1 / 80, "board_reach": 5.0, "light": 12, "beat_ticks": 8,
            "tune": ((10, 13, 17), (22, -1, 21), (22, 18, 15), (17, -1, 13), (12, 15, 18), (17, 15, 12), (13, 12, 9), (10, -1, -1)),
            "bass": (10, 10, 3, 10, 5, 5, 10, 10), "chord": (13, 13, 18, 13, 21, 21, 13, 13)}

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
    {"id": GHOST_TRAIN["block"], "pattern": ["PGP", "DLD", "PRP"],
     "key": {"P": "#minecraft:planks", "G": "minecraft:glass_pane", "D": "#minecraft:wooden_doors", "L": "minecraft:jack_o_lantern",
             "R": "minecraft:rail"},
     "result": GHOST_TRAIN["block"], "count": 1, "category": "misc"},
    # The carousel: orange wool for its canopy, a lamp for its lights, iron bars for its poles, a note block for its organ,
    # planks for its deck and horses and gold for its trim. Its power comes from the kinetic network.
    {"id": CAROUSEL["block"], "pattern": ["WLW", "BNB", "PGP"],
     "key": {"W": "minecraft:orange_wool", "L": "minecraft:redstone_lamp", "B": "minecraft:iron_bars", "N": "minecraft:note_block",
             "P": "#minecraft:planks", "G": "minecraft:gold_ingot"},
     "result": CAROUSEL["block"], "count": 1, "category": "misc"},
    {"id": POPCORN_WAGON["block"], "pattern": ["GJG", "GCG", "PIP"],
     "key": {"G": "minecraft:glass_pane", "J": "minecraft:jack_o_lantern", "C": "minecraft:cauldron", "P": "#minecraft:planks",
             "I": "minecraft:iron_ingot"},
     "result": POPCORN_WAGON["block"], "count": 1, "category": "misc"},
]
SHAPELESS = []


def blocks():
    return [COTTON_CANDY["block"], POPCORN_WAGON["block"], TICKET_BOOTH["block"], WHACK["block"], GALLERY["block"],
            GHOST_TRAIN["block"], CAROUSEL["block"]]


def items():
    """The stalls and the ticket (the Cotton Candy is in ITEMS)."""
    return blocks() + [TICKET["item"]]
