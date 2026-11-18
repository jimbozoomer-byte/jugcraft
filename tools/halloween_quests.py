"""Halloween quests (docs/features/halloween-quests.md): during the Halloween season, town residents ask for things
from the pack's other Halloween content, and the commission board lists the same errands. Each is done once a
Halloween per player, by talking to its resident or at the board, and pays Jugs and a Halloween decoration.

Pure data. Java: town/HalloweenQuests.QUESTS, which tools/check_mod_data.py holds to QUESTS here, in this order.
"""

MOD = "jugcraft"

# Each errand: (id, the job of the residents who give it (town blueprint jobs), the item asked for, how many, the Jugs
# paid, the decoration given, how many; who asks (the board's card), what they say when asked, and their thanks).
QUESTS = [
    ("graveyard_blessing", "priest", "jugcraft:reaper_shade", 6, 8, "jugcraft:mourning_wreath", 1, "Priest of Notch",
     "The Last Reaper walks the Hollow Acre again. Bring me 6 Reaper's Shade from her and I will bless the town's graves.",
     "Bless you. The graves will rest easy, and this wreath will see you through the Last Rites again."),
    ("costume_silk", "vendor", "jugcraft:gossamer_silk", 4, 6, "jugcraft:witch_hat", 1, "Market Vendor",
     "Madame Tatterlace's silk makes the finest costumes in town. Bring me 4 Gossamer Silk from the Spindle Loft.",
     "Oh, it shimmers! Here, take a hat from the first batch."),
    ("horseman_proof", "mayor", "jugcraft:horseman_lantern", 1, 8, "jugcraft:haunted_portrait", 1, "The Mayor",
     "The Headless Horseman has the council afraid of the bridge road. Bring me his lantern as proof he has been beaten.",
     "His very lantern! The council will hang your portrait. Well, a portrait."),
    ("harvest_bonfire", "gardener", "jugcraft:kings_ember", 1, 6, "jugcraft:mini_pumpkin_stack", 2, "Town Gardener",
     "An ember from the Pumpkin King would light a harvest bonfire that burns all night. Bring me one from the Crowfield.",
     "Still warm! Take some pumpkins for your porch."),
    ("party_treats", "baker", "jugcraft:candy_corn", 16, 3, "jugcraft:candy_bag", 1, "The Baker",
     "The Halloween party needs treats and my oven is full of bread. Bring me 16 Candy Corn.",
     "Perfect. Take a Candy Bag for your own trick-or-treating."),
    ("wisp_lanterns", "lamplighter", "jugcraft:wisp_in_a_jar", 3, 3, "jugcraft:jack_o_lantern_string_lights", 4,
     "Lamplighter",
     "Wisp light never needs oil. Catch me 3 Wisps in Jars over the swamps or cornfields on a Halloween night.",
     "They glow beautifully. Have some string lights for your trouble."),
    ("street_pumpkins", "decorator", "minecraft:jack_o_lantern", 8, 2, "jugcraft:bat_bunting", 4, "Decorator",
     "The streets need more pumpkins! Bring me 8 Jack o'Lanterns.",
     "Wonderful! Here's some bunting I had left over."),
    ("ghost_samples", "watch", "jugcraft:ectoplasm", 4, 3, "jugcraft:black_cat_figure", 1, "City Watch",
     "Something rattles the graveyard at night. Bring me 4 Ectoplasm so the watch can prove it's ghosts.",
     "Ghosts, then. The watch will keep its distance. Take this, for luck."),
]


def lang():
    """The quests' words: who asks, what they ask and their thanks, and the board's and the residents' shared lines."""
    out = {
        f"message.{MOD}.halloween_quest.purse_full": "%s: Your purse is too full for my Jugs. Spend some and come back.",
        f"commission.{MOD}.halloween.title": "HALLOWEEN ERRANDS",
        f"commission.{MOD}.halloween.next": "More errands",
        f"commission.{MOD}.halloween.open": "Halloween",
        f"commission.{MOD}.halloween.back": "City orders",
        f"commission.{MOD}.halloween.deliver": "Hand over for the reward",
        f"commission.{MOD}.halloween.done": "Done this Halloween",
        f"commission.{MOD}.halloween.bag": "In your bag: %s / %s",
        f"commission.{MOD}.halloween.reward": "Reward: %s Jugs and %s",
        f"commission.{MOD}.halloween.footer": "Or bring it to them. Each errand once per Halloween.",
    }
    for quest, _job, _item, _count, _jugs, _gift, _gifts, patron, ask, thanks in QUESTS:
        out[f"halloween_quest.{MOD}.{quest}.patron"] = patron
        out[f"halloween_quest.{MOD}.{quest}.ask"] = ask
        out[f"halloween_quest.{MOD}.{quest}.thanks"] = thanks
    return out
