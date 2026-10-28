"""Front-Yard Haunt Scenes, decor set 7 (docs/features/front-yard-haunt.md): the front yard dressed for Halloween night,
from the street to the door.

Part 1, the path:
- The Haunted Mailbox (agriculture/HauntedMailboxBlock): a rural mailbox of the large size, black sheet steel 31 cm wide,
  34 cm high and 62 cm long under its arched top, on a weathered wooden post one block across and two up (`size`), its
  arm under it on a brace, its red flag on its side, an orange reflector strip down each flank and a cobweb strung between
  the post and the arm. An empty hand raises its flag or lowers it (`flag`). It is haunted: every `rattle` ticks or so (each
  box a little apart from the next) its door rattles open a crack and shuts, and while anyone is within `stare` blocks of
  it, its door falls open `open` degrees about its hinge at its foot (`hinge`, height and depth) and two eyes stare out of
  the dark inside it, glowing (client/HauntedMailboxRenderer). The haunting is each player's own sight of it, drawn by
  their client; the box holds nothing.
- The Pumpkin Path Light (PumpkinPathLightBlock): a shepherd's hook of black wrought iron 1.3 m high, one block across
  and two up, with a foot step to tread it into the ground, its crook curling out over the path; from its tip a little
  jack-o'-lantern 25 cm across hangs on a chain. An empty hand lights it or puts it out (`light`, from its lower block;
  its carved face glows, steady). It sways on its chain (client/PumpkinPathLightRenderer: up to `sway` degrees about the
  crook's tip, `pivot`, height and depth, once to and fro in `period` ticks), never in step with the next along the path.

Part 2, the bones and the risen:
- The Skeleton Dog, Skeleton Cat and Skeleton Crow (SkeletonAnimalBlock): the bleached bones of a big dog sitting, a cat
  arched with its tail up and a crow perched, each at real size, their skulls' eye sockets square, each within its
  `box`. Each faces whoever places it; an empty hand rattles its bones. Each has one moving part, drawn by
  client/SkeletonAnimalRenderer about its `pivot` (across, height and depth), each player's own sight of it:
  - the dog's tail sweeps the ground, `wag` degrees either way once in `wag_ticks` ticks, while anyone is within `near`
    blocks, and lies still when no one is;
  - the cat's tail sways from side to side, `sway` degrees either way, once in `period` ticks, never in step with
    another cat's;
  - the crow's skull turns to watch whoever is within `watch` blocks, up to `turn` degrees either way, in a bird's
    quick snaps every `snap` ticks; with no one near it glances about.
- The Rising Zombie (RisingZombieBlock): a zombie clawing its way out of a mound of dug earth, one block across and two
  up: its torso in a torn burial shirt out to the waist, its head lolling, its two arms raised from its shoulders and
  leaning out, the right `lean[0]` degrees and the left `lean[1]`, so the right reaches higher. Its arms claw at the air,
  to and fro `claw` degrees about its shoulders (`shoulder`, the right's across, height and depth; the left's is its
  mirror), once in `period` ticks, out of step; the nearer anyone comes (within `reach` blocks), the higher they reach,
  up to `lurch` degrees nearer upright (client/RisingZombieRenderer). An empty hand makes it groan.
Part 3, the doorway:
- The Gargoyle Door Knocker (GargoyleKnockerBlock): a bronze gargoyle's head 30 cm high on its back plate, a ring in its
  jaws and a stud below it for the ring to strike. It hangs on the side of a block or on either face of a door, as the
  wreaths do. An empty hand lifts its ring and raps `raps` times, `rap_ticks` apart, the ring swinging out `swing` degrees
  about its pivot in the jaws (`pivot`, height and depth) and the gargoyle's eyes glowing while it raps
  (client/GargoyleKnockerRenderer, and on a door client/DoorWreathRenderer). With a Candy Bag in hand it knocks for
  trick-or-treat at its own door, or at a wooden door beside the wall it hangs on. It is haunted: now and then (one
  random tick in `haunt` on a wall, one tick in `door_chance` on a door) it raps by itself.
- The Bat Wreath (BatWreathBlock): black twigs wound into a ring 75 cm across, three black paper bats flying off it and
  a purple satin bow at its foot. It hangs on the side of a block or on either face of a door, as the other wreaths do.
- The Beware Doormat (BewareDoormatBlock): a coir doormat 75 cm by 50 cm, a black border round a skull and crossbones.
  Now and then (one step in `heartbeat`) whoever treads on it hears a heart beating under the boards.
Sizes in pixels, facing north (the street to the north, the house behind it to the south).
"""

MAILBOX = {"block": "haunted_mailbox", "display": "Haunted Mailbox", "size": (1, 2, 1), "rattle": 160, "stare": 4.0, "open": 75.0,
           "hinge": (17.0, 2.75)}
PATH_LIGHT = {"block": "pumpkin_path_light", "display": "Pumpkin Path Light", "size": (1, 2, 1), "light": 10, "sway": 6.0,
              "pivot": (19.0, 3.5), "period": 90}

SKELETON_DOG = {"block": "skeleton_dog", "display": "Skeleton Dog", "box": (5.75, 0.0, 2.0, 10.25, 11.0, 14.5), "pivot": (8.0, 0.5, 12.0),
                "wag": 25.0, "wag_ticks": 10, "near": 6.0}
SKELETON_CAT = {"block": "skeleton_cat", "display": "Skeleton Cat", "box": (6.75, 0.0, 2.0, 9.25, 9.0, 12.5), "pivot": (8.0, 3.75, 10.85),
                "sway": 15.0, "period": 60}
SKELETON_CROW = {"block": "skeleton_crow", "display": "Skeleton Crow", "box": (6.5, 0.0, 2.5, 9.5, 7.0, 11.0), "pivot": (8.0, 5.5, 5.25),
                 "watch": 8.0, "turn": 70.0, "snap": 12}
RISING_ZOMBIE = {"block": "rising_zombie", "display": "Rising Zombie", "size": (1, 2, 1), "claw": 12.0, "period": 70, "reach": 5.0,
                 "lurch": 20.0, "shoulder": (11.5, 10.5, 8.0), "lean": (22.5, 45.0)}
ANIMALS = (SKELETON_DOG, SKELETON_CAT, SKELETON_CROW)

KNOCKER = {"block": "gargoyle_knocker", "display": "Gargoyle Door Knocker", "raps": 3, "rap_ticks": 6, "swing": 40.0, "pivot": (7.75, 15.0),
           "haunt": 3, "door_chance": 3000}
BAT_WREATH = {"block": "bat_wreath", "display": "Bat Wreath"}
DOORMAT = {"block": "beware_doormat", "display": "Beware Doormat", "heartbeat": 8}

ITEMS = {}

RECIPES = [
    # The mailbox: iron for its box, a spider eye for its haunting, red dye for its flag, a fence post to stand it on.
    {"id": MAILBOX["block"], "pattern": ["NEN", "NRN", " F "],
     "key": {"N": "minecraft:iron_nugget", "E": "minecraft:spider_eye", "R": "minecraft:red_dye", "F": "#minecraft:wooden_fences"},
     "result": MAILBOX["block"], "count": 1, "category": "misc"},
    # Two path lights: iron bars bent into two hooks, and a jack-o'-lantern carved into two little lanterns.
    {"id": PATH_LIGHT["block"], "pattern": ["BB", "BJ", "B "],
     "key": {"B": "minecraft:iron_bars", "J": "minecraft:jack_o_lantern"},
     "result": PATH_LIGHT["block"], "count": 2, "category": "misc"},
    # The Rising Zombie: rotten flesh round a bone, out of coarse dirt.
    {"id": RISING_ZOMBIE["block"], "pattern": ["RRR", "RBR", "DDD"],
     "key": {"R": "minecraft:rotten_flesh", "B": "minecraft:bone", "D": "minecraft:coarse_dirt"},
     "result": RISING_ZOMBIE["block"], "count": 1, "category": "misc"},
    # The knocker: a cast head of iron and its ring of nuggets.
    {"id": KNOCKER["block"], "pattern": ["NIN", "N N", " N "], "key": {"N": "minecraft:iron_nugget", "I": "minecraft:iron_ingot"},
     "result": KNOCKER["block"], "count": 1, "category": "misc"},
    # The bat wreath: sticks wound round, paper bats, black dye.
    {"id": BAT_WREATH["block"], "pattern": ["SPS", "S S", "SDS"], "key": {"S": "minecraft:stick", "P": "minecraft:paper", "D": "minecraft:black_dye"},
     "result": BAT_WREATH["block"], "count": 1, "category": "building"},
    # The doormat: straw bristles round a black device.
    {"id": DOORMAT["block"], "pattern": ["WBW", "WWW"], "key": {"W": "minecraft:wheat", "B": "minecraft:black_dye"},
     "result": DOORMAT["block"], "count": 1, "category": "building"},
]
# The bones are bones, tied with string; the crow's are a bird's, with a feather.
SHAPELESS = [
    {"id": SKELETON_DOG["block"], "inputs": ["minecraft:bone"] * 5 + ["minecraft:string"], "result": SKELETON_DOG["block"], "count": 1,
     "category": "misc"},
    {"id": SKELETON_CAT["block"], "inputs": ["minecraft:bone"] * 3 + ["minecraft:string"], "result": SKELETON_CAT["block"], "count": 1,
     "category": "misc"},
    {"id": SKELETON_CROW["block"], "inputs": ["minecraft:bone"] * 2 + ["minecraft:feather", "minecraft:string"],
     "result": SKELETON_CROW["block"], "count": 1, "category": "misc"},
]


def blocks():
    return [MAILBOX["block"], PATH_LIGHT["block"], SKELETON_DOG["block"], SKELETON_CAT["block"], SKELETON_CROW["block"],
            RISING_ZOMBIE["block"], KNOCKER["block"], BAT_WREATH["block"], DOORMAT["block"]]


def items():
    return blocks()
