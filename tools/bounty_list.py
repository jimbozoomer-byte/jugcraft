"""The Bounty List (docs/features/bounty-list.md): a written book the town gives a player for the first quest they
complete, a city order or a Halloween errand, naming every lair boss and how its lair is opened.

Pure data. Java: town/BountyList.BOSSES, which tools/check_mod_data.py holds to BOSSES here, in this order.
"""

MOD = "jugcraft"

# Each boss: (its entity id, the item that opens its lair, its page's words). A page holds about 200 characters
# under the boss's name; check_mod_data holds each to that.
BOSSES = [
    ("vesperine", "death_knell",
     "Craft a Death Knell and a Mourning Wreath. At night, lay the wreath on a grave among lit candles and ring the "
     "knell: the Last Rites open the Hollow Acre."),
    ("tatterlace", "cursed_spindle",
     "Craft a Cursed Spindle and a Spinning Wheel. Use the spindle on the wheel at night: prick your finger, and wake "
     "in the Spindle Loft."),
    ("yeti_king", "frost_horn",
     "Craft a Frost Horn. Blow it at night, standing on snow or ice, and the snow opens onto the Glacier Hall."),
    ("cinder_tyrant", "kiln_seal",
     "Craft a Kiln Seal. Press it into a magma block in the Nether or a volcanic land, and the magma opens onto the "
     "Cinder Kiln."),
    ("mire_hag", "hag_stone",
     "Craft a Hag Stone. At night, stand in shallow water or on mud in a wetland and look through it: the mist takes "
     "you to the Lantern Fen."),
    ("crypt_lich", "exhumation_writ",
     "Craft a Writ of Exhumation. Serve it on a stone sarcophagus in a crypt, under a roof: the lid grinds aside on a "
     "stair down to the Archive of Last Names."),
    ("iron_dreadnought", "sealed_orders",
     "Craft Sealed Orders. Use them on a Field Telephone standing in a trench, under the open sky: a whistle calls you "
     "to the Foundry Trench."),
    ("alpha_werewolf", "howling_totem",
     "Craft a Howling Totem. Use it at night in werewolf country, under the open sky: something answers, and the mist "
     "takes you to the Moonlit Ring."),
    ("cydonian_sentinel", "altar_keystone",
     "Craft an Altar Keystone. At night, set it into the Ancient Altar of Adam-ondi-Ahman: the altars kindle red, and "
     "the red mist opens onto Mars."),
    ("pumpkin_king", "harvest_crown",
     "Craft a Harvest Crown. At night, use it on a scarecrow that wears a head, out among the pumpkins: the crows "
     "burst up, and the mist takes you to the Crowfield."),
    ("hessian_horseman", "hessians_bridle",
     "In the Halloween season he rides at midnight. Out of it, craft his Bridle and use it at night on a "
     "jack-o'-lantern on a fence post under the open sky: the Church Bridge."),
]

PAGE_LIMIT = 200


def lang():
    """The book's words: the first page, the line that comes with it, and each boss's page."""
    out = {
        f"bounty_list.{MOD}.intro": "WANTED\n\nBy order of the Mayor: every terror known to the town, and how each is "
                                    "called out of hiding. Turn the page.",
        f"message.{MOD}.bounty_list.given": "The town thanks you for your first job, and hands you its Bounty List: "
                                             "every boss known, and how to find them.",
    }
    for boss, _item, words in BOSSES:
        out[f"bounty_list.{MOD}.{boss}"] = words
    return out
