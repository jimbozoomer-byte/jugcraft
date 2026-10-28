"""Front-Yard Haunt Scenes, decor set 7 (docs/features/front-yard-haunt.md): the front yard dressed for Halloween night,
from the street to the door.

Part 1, the path:
- The Haunted Mailbox (agriculture/HauntedMailboxBlock): a rural mailbox of the large size, black sheet steel 31 cm wide,
  34 cm high and 62 cm long under its arched top, on a weathered wooden post one block across and two up (`size`), its
  arm under it on a brace, its red flag on its side, three claw scratches down its flank and a cobweb strung between the
  post and the arm. An empty hand raises its flag or lowers it (`flag`). It is haunted: every `rattle` ticks or so (each
  box a little apart from the next) its door rattles open a crack and shuts, and while anyone is within `stare` blocks of
  it, its door falls open `open` degrees about its hinge at its foot (`hinge`, height and depth) and two eyes stare out of
  the dark inside it, glowing (client/HauntedMailboxRenderer). The haunting is each player's own sight of it, drawn by
  their client; the box holds nothing.
- The Pumpkin Path Light (PumpkinPathLightBlock): a shepherd's hook of black wrought iron 1.3 m high, one block across
  and two up, with a foot step to tread it into the ground, its crook curling out over the path; from its tip a little
  jack-o'-lantern 25 cm across hangs on a chain. An empty hand lights it or puts it out (`light`, from its lower block;
  its carved face glows, steady). It sways on its chain (client/PumpkinPathLightRenderer: up to `sway` degrees about the
  crook's tip, `pivot`, height and depth, once to and fro in `period` ticks), never in step with the next along the path.
Sizes in pixels, facing north (the street to the north, the house behind it to the south).
"""

MAILBOX = {"block": "haunted_mailbox", "display": "Haunted Mailbox", "size": (1, 2, 1), "rattle": 160, "stare": 4.0, "open": 75.0,
           "hinge": (17.0, 2.75)}
PATH_LIGHT = {"block": "pumpkin_path_light", "display": "Pumpkin Path Light", "size": (1, 2, 1), "light": 10, "sway": 6.0,
              "pivot": (19.0, 3.5), "period": 90}

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
]
SHAPELESS = []


def blocks():
    return [MAILBOX["block"], PATH_LIGHT["block"]]


def items():
    return blocks()
