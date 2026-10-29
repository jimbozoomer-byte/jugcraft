"""The garden cohort: six garden spirits on the shared pact record (docs/features/concordance-garden-cohort.md).

GC-03 (GC-S9, GC-P6, GC-P7): The Garden's Regard, the cohort's one research, and the groundwork every garden pact shares.
- The research (Java: concordance/spirits/GardenPacts.RESEARCH): the Spiritbinders' practitioner entry, begun once the
  Binding Arts and Verdant Husbandry are understood. Its evidence is the player's own and server-made: examine a garden
  specimen (#jugcraft:garden_regard_specimens: the four organism crops' produce and Verdant Chaff); harvest a ripe organism
  crop by hand (the Greenwardens' cultivation); keep a bound worker at its work (the Binding Arts' worker_service); and, for
  mastery, keep the first obligation of MASTERY_SPIRITS different garden spirits' pacts (the practice
  jugcraft:garden_pact_service, one key a spirit, recorded only by spirits/PactObligations; its gate is
  tools/concordance_progression.py's PRACTICES, and its distinct count is the graph's GB26 rule there). No state takes
  notes. Mastery unlocks nothing in this cohort.
- The first obligations (Java: spirits/PactObligations.Act, ACTS here, append only): each garden service names one act by
  hand (OBLIGATIONS: work -> act, each act one work), whose one-line hook is HOOKS'.
- The bed index (Java: spirits/GardenReach; REACH here).

tools/concordance.py merges these tables into its own; tools/check_mod_data.py (check_garden_pacts) holds the Java and the
data to them. Each spirit's unit adds its work to OBLIGATIONS and CONSTANTS and its codex page to PAGES (GC-04: Buer's;
GC-05: Bathin's), and its row to DISTINCT.
"""

import concordance_ecology as ecology
import concordance_pacts as pacts
import concordance_workers as workers

MOD = "jugcraft"


def rid(path):
    return f"{MOD}:{path}"


REGARD = rid("garden_regard")  # Java: spirits/GardenPacts.RESEARCH
GARDEN_PACT_SERVICE = rid("garden_pact_service")  # Java: GardenPacts.ACTIVITY (its gate: concordance_progression.PRACTICES)
CULTIVATION = rid("cultivation")  # Java: garden/Garden.ACTIVITY
WORKER_SERVICE = rid("worker_service")  # Java: spirits/Workers.ACTIVITY
SPECIMEN_TAG = f"{MOD}:garden_regard_specimens"  # Java: GardenPacts.SPECIMENS
SPECIMENS = {rid("sunpetal"): "Sunpetal", rid("dewmoss"): "Dewmoss", rid("gloamcap"): "Gloamcap",
             rid("mendvetch"): "Mendvetch", rid("verdant_chaff"): "Verdant Chaff"}
MASTERY_SPIRITS = 3  # Java: GardenPacts.MASTERY_SPIRITS: the mastered rule's distinct (GC-S9)
# Java: PactObligations.Act, in order; append only (unit 2 device, 3 read, 4 graft, 5 feed, 6 harvest).
ACTS = ["tend", "device",
        # GC-06 unit 3
        # GC-07 unit 4
        "graft",
        # GC-08 unit 5
        # GC-09 unit 6
        ]
# Each act's one-line hook: (Java file under concordance/, the member it is in, the line, what it follows, what it precedes).
HOOKS = {"tend": ("garden/VerdantBedBlock.java", "static void tendBy(",
                  "PactObligations.performed(level, player, PactObligations.Act.TEND, pos);",
                  "if (bed.tend(level.getGameTime())) {", "} else {"),
         # GC-05: an awake living device read or set with an empty hand (after useEmpty; a dormant one returns before it, and
         # its return at four tabs never matches the method's last at two).
         "device": ("garden/LivingDeviceBlock.java", "protected InteractionResult useWithoutItem(",
                    "PactObligations.performed(serverLevel, server, PactObligations.Act.DEVICE, pos);",
                    "useEmpty(device, server, serverLevel);", "}\n\t\treturn InteractionResult.SUCCESS;"),
         # GC-06 unit 3
         # GC-07 unit 4
         # GC-07: a crop grafted by hand with its donor (after the donor is spent, before the gesture's success).
         "graft": ("garden/OrganismCropBlock.java", "private InteractionResult graftBed(",
                   "PactObligations.performed(level, player, PactObligations.Act.GRAFT, pos.below());",
                   "donor.consume(1, player);", "return InteractionResult.SUCCESS;"),
         # GC-08 unit 5
         # GC-09 unit 6
         }
# work -> act (each act at most one work); GC-04: Buer's; GC-05: Bathin's; GC-06 on, each unit's under its line.
OBLIGATIONS = {"strain_tending": "tend", "verdance_carrying": "device",
               # GC-06 unit 3
               # GC-07 unit 4
               "graft_keeping": "graft",
               # GC-08 unit 5
               # GC-09 unit 6
               }
# work -> {Java file under concordance/: {constant: value}}; each spirit's unit adds its own (check_garden_pacts).
# GC-04: Buer remembers today's eases of at most 16 beds, and asks GardenPacts.target about at most 8 beds a think.
# GC-05: a move of Bathin's sets down at most 8 (the Gleaner's own draw) or pours at most 3 batches; at most 8 places asked a
# think.
CONSTANTS = {"strain_tending": {"spirits/StrainTending.java": {"MEMORY": 16, "MAX_ASKED": 8}},
             "verdance_carrying": {"spirits/VerdanceCarrying.java": {"MOVE_MAX": 8, "MAX_BATCHES": 3, "MAX_ASKED": 8}},
             # GC-06 unit 3
             # GC-07 unit 4
             # GC-07: at most 8 places asked a think.
             "graft_keeping": {"spirits/GraftKeeping.java": {"MAX_ASKED": 8}},
             # GC-08 unit 5
             # GC-09 unit 6
             }
REACH = {"MAX_BEDS": 64, "REFRESH_TICKS": 200}  # Java: spirits/GardenReach (GC-P2)
# GE8: mechanically distinct services. Each garden work changes one kind of state through one call that only its service
# class makes, under its own verb; check_garden_pacts holds the classes to the rows and no two rows alike. Each spirit's
# unit adds its row (GC-04: Buer's).
DISTINCT = {
    "strain_tending": {"verb": "ease", "state": "a Verdant Bed's harvest strain", "call": "bed.tend(now)",
                       "class": "StrainTending"},
    # GC-05: Bathin's; "also" is his second call (the pylon road), made once by his class (check_garden_pacts).
    "verdance_carrying": {"verb": "move an essence at once", "state": "a Verdant Heart's and a Gleaner's or Ley Pylon's reservoirs",
                          "call": "gleaner.receiveFrom(heart, record.holder(), MOVE_MAX, level)", "class": "VerdanceCarrying",
                          "also": "heart.pourInto(pylon, level, MAX_BATCHES)"},
    # GC-06 unit 3
    # GC-07 unit 4
    # GC-07: Foras's; "also" is his second call (the donor's one transaction), made once by his class (check_garden_pacts).
    "graft_keeping": {"verb": "transform, spending a donor", "state": "a Verdant Bed's graft",
                      "call": "Grafting.apply(level, host.crop(), host.bed(), host.graft())", "class": "GraftKeeping",
                      "also": "spend(storage, donor)"},
    # GC-08 unit 5
    # GC-09 unit 6
}
CODEX_PLACE = (6, 4)  # the Binding category's map: (0..6, 0..4) holds eleven entries; (6, 4) is free (check_concordance)
# GC-05 (GC-P45, GC-P54): a device index (GardenReach.devices) holds at most this many Hearts, Gleaners and Ley Pylons, each.
REACH["MAX_DEVICES"] = 16  # Java: spirits/GardenReach
# GC-07 (GC-P82, GC-P83): a store index (GardenReach.withStores) holds at most this many plain containers touching the perch.
REACH["MAX_STORES"] = 16  # Java: spirits/GardenReach

RESEARCH = {"garden_regard": {
    "name": "The Garden's Regard", "principle": "tether", "tradition": "spiritbinders", "stage": "practitioner",
    "icon": rid("verdant_bed"),
    "requires": [{"research": rid("binding_arts"), "state": "understood"},
                 {"research": rid("verdant_husbandry"), "state": "understood"}],
    "states": {
        "encountered": [{"type": "examine", "specimens": f"#{SPECIMEN_TAG}"}],
        "observed": [{"type": "practice", "activity": CULTIVATION}],
        "understood": [{"type": "practice", "activity": WORKER_SERVICE}],
        "mastered": [{"type": "practice", "activity": GARDEN_PACT_SERVICE, "distinct": MASTERY_SPIRITS}],
    },
    "unlocks": {},
    # (title, description, frame, icon). The journal's pacts section (PactRegards, GC-01) shows the next state's description
    # as the next step, so each is worded as one.
    "advancements": {
        "encountered": ("Fruits of the Bed", "Examine a garden specimen: a Sunpetal, Dewmoss, Gloamcap, Mendvetch or Verdant "
                        "Chaff (sneak and use it in the air)", "task", rid("sunpetal")),
        "observed": ("By Your Own Hand", "Harvest a ripe organism crop from a Verdant Bed by hand", "task", rid("mendvetch")),
        "understood": ("The Garden's Regard", "Understand The Garden's Regard: keep a bound familiar, spirit or construct at "
                       "its work", "goal", rid("verdant_bed")),
        "mastered": ("Kept Three Ways", "Master The Garden's Regard: keep the first obligation of three garden spirits' pacts",
                     "challenge", rid("pact_perch")),
    },
    "locked": {"encountered": "Examine a garden specimen first", "observed": "Harvest an organism crop by hand first",
               "understood": "Understand The Garden's Regard first", "mastered": "Master The Garden's Regard first"},
}}

# Under message.jugcraft.concordance. (tools/concordance.py MESSAGES).
MESSAGES = {
    "examine.garden_regard": "You look at the %s as the garden's spirits would: for what its bed gave it",
    "garden_pact.obligation": "First obligation: %s by hand inside the pact's area while it is active",
    "garden_pact.obligation_kept": "First obligation kept: %s of %s garden spirits toward mastering The Garden's Regard",
    **{f"garden_pact.act.{act}": text for act, text in {
        "tend": "tend a strained Verdant Bed",
        "device": "read or set an awake Verdant Heart, Gleaner, Mulch Maw or Habitat Gauge",
        # GC-06 unit 3
        # GC-07 unit 4
        "graft": "graft a host crop with its donor",
        # GC-08 unit 5
        # GC-09 unit 6
    }.items()},
}
# Under tooltip.jugcraft.concordance. (tools/concordance.py TOOLTIPS).
TOOLTIPS = {"garden_regard_specimen": "The garden's spirits regard this: sneak and use it in the air to examine it"}

# Each spirit's codex pages, in its unit: ("text", title, words, ("garden_regard", "understood")). GC-04: Buer's (Dossier 1),
# their numbers read from his offer (tools/concordance_pacts.py SPIRITS) and the tables that hold the Java's (the unit review).
_BUER = pacts.SPIRITS["buer"]["pact"]
_BUER_ITEMS = {rid("mendvetch"): "Mendvetch", "minecraft:bone_meal": "bone meal"}  # his compact's and offering's words
_BUER_OFFERING = _BUER_ITEMS[pacts.SPIRITS["buer"]["offerings"]["items"][0]]
# GC-05: Bathin's (Dossier 2), their numbers read from his offer and the tables that hold the Java's, as Buer's.
_BATHIN = pacts.SPIRITS["bathin"]["pact"]
_BATHIN_ITEMS = {"minecraft:ender_pearl": "ender pearls", rid("verdant_chaff"): "Verdant Chaff"}  # his compact's and offering's words


def _count(n):
    return "one" if n == 1 else str(n)


PAGES = [
    ("text", "Buer",
     "Buer, a spirit of the old books, mends what a garden's harvests strain. Build a **Pact Perch** (its recipe is this "
     f"entry's third page) with an **awake Verdant Bed** of yours within {_BUER['service']['radius']} blocks, and use it: "
     "he comes rooted on the perch, a great blossom, and names every term before anything is spent. Without a bed of yours "
     "there he will not come: \"There is nothing here I could mend.\"", ("garden_regard", "understood")),
    ("text", "His Service",
     f"By daylight ({workers.clock(_BUER['service']['from'])} to {workers.clock(_BUER['service']['to'])}), while you are "
     f"within {workers.PACT['REACH']} blocks of the perch, he eases **one harvest strain** from one awake bed of yours or "
     f"your party's in his area at a time, the most strained first, at most {_BUER['service']['quota']} a game day. Each "
     f"bed is eased no sooner than its own tending allows: the {ecology.TEND_TICKS // 20} seconds a Greenwarden's hand "
     "waits, shared with yours. He moves no nutrients and no items, and never touches a bed you could not change by hand.",
     ("garden_regard", "understood")),
    ("text", "The Overworked Bed",
     "A bed he mended today that is worked back to its worst strain is **overworked**: he will not mend it again until it "
     "eases by its own rules: fed with bone meal or fertilizer, a Mulch Maw's nutrient, or left fallow. The pact's page "
     "and Jade name it. Nothing is lost while it waits, and a restart forgets his complaint; nothing is ever owed.",
     ("garden_regard", "understood")),
    ("text", "His Price",
     "The compact, paid as you accept (no astrolabe): **"
     + " and ".join(f"{cost['count']} {_BUER_ITEMS[cost['item']]}" for cost in _BUER["compact"]["items"])
     + f"**. Then {_count(_BUER['upkeep']['count'])} {_BUER_OFFERING} every {_BUER['upkeep']['period_days']} game days, "
     f"up to {_BUER['upkeep']['cap']} paid ahead; a missed one is warned and he works on through "
     + ("a day's grace" if _BUER["upkeep"]["grace_days"] == 1 else f"{_BUER['upkeep']['grace_days']} days' grace")
     + ", then the pact is suspended with nothing lost. Your first obligation: tend a strained bed in his area yourself, "
     "with an empty hand, while the pact is active.", ("garden_regard", "understood")),
    # GC-05: Bathin's (Dossier 2). No source quotation (GC-S14); his matter's page is the accord's.
    ("text", "Bathin",
     "Bathin, a spirit of the old books, moves what is wanted from where it is to where it should be, and never by a road. "
     f"Build a **Pact Perch** with an awake **Verdant Heart** of yours within {_BATHIN['service']['radius']} blocks of it "
     f"and an awake **Gleaner** of yours or a **Ley Pylon** of yours or no one's within {_BATHIN['service']['radius']} "
     "blocks too, and use it: he "
     "is suddenly coiled on the perch, a strong man on a serpent's tail, and names every term before anything is spent. "
     "Without them he does not come: \"Nothing here to carry, or nowhere to carry it.\"", ("garden_regard", "understood")),
    ("text", "His Service",
     f"At any hour, while you are within {workers.PACT['REACH']} blocks of the perch, he takes **Verdance** from a Heart of "
     f"yours or your party's and sets it down at once: up to {ecology.GLEANER_DRAW} into a **Gleaner** beyond that Heart's "
     f"own reach (more than {ecology.HEART_REACH} blocks from it), or into a **Ley Pylon** your party or no one owns that is "
     "not beside it, at the Heart's own 3 for 2. One move at a time, at most "
     f"{_BATHIN['service']['quota']} a game day. "
     f"From a Heart still beating he waits until it holds a whole move of {ecology.GLEANER_DRAW}; from one that has stopped "
     "he takes whatever is left (into a pylon, whole batches of 3 only). Nothing is ever on the way: the Heart and its "
     "target shimmer in the same moment, and he holds none.", ("garden_regard", "understood")),
    ("text", "Two Roads from One Heart",
     "A Heart with a Ley Pylon of its own beside it already pours into it. Bathin leaves such a Heart alone and asks once, "
     "on the pact's page at the perch: **Leave it**, or **Take from it**. Until you answer, the roster says \"awaiting your "
     "answer\" and he keeps to leaving it. You may change your answer whenever you like; nothing is spent either way.",
     ("garden_regard", "understood")),
    ("text", "His Price",
     "The compact, paid as you accept: **"
     + " and ".join(f"{cost['count']} {_BATHIN_ITEMS[cost['item']]}" for cost in _BATHIN["compact"]["items"])
     + f"**. Then {_count(_BATHIN['upkeep']['count'])} {_BATHIN_ITEMS[pacts.SPIRITS['bathin']['offerings']['items'][0]]} every "
     f"{_BATHIN['upkeep']['period_days']} game days, up to {_BATHIN['upkeep']['cap']} weeks paid ahead; a missed week is "
     "warned and he carries on through "
     + ("a day's grace" if _BATHIN["upkeep"]["grace_days"] == 1 else f"{_BATHIN['upkeep']['grace_days']} days' grace")
     + ", then the pact is suspended with nothing lost. Your first obligation: use an awake living device in his area with "
     "an empty hand while the pact is active. Ending it returns what was paid ahead once: his coil tightens, and he is gone "
     "in a ring of dust.", ("garden_regard", "understood")),
    # GC-06 unit 3
    # GC-07 unit 4
    # GC-07: Foras's (Dossier 4), their numbers read from his offer and the tables that hold the Java's, as Buer's and
    # Bathin's. No source quotation (GC-S14); his objection's page is the accord's, worded with his answers' own words.
    ("text", "Foras",
     "Foras, a spirit of the old books, keeps what would be lost and argues kindly with whoever wastes it. Build a **Pact "
     "Perch** with a **chest** (or another plain container) touching it and a crop that takes a graft (a **Sunpetal**, "
     "which takes **Dewlit** from a Dewmoss) on a **Verdant Bed** of yours within "
     f"{pacts.SPIRITS['foras']['pact']['service']['radius']} blocks, and use it: a tall elder in a robe of bark and moss "
     "stands beside the perch, tips his acorn cap, and names every term before anything is spent. Without a store he does "
     "not come: \"Where shall I keep the cuttings? Set a store beside me.\"", ("garden_regard", "understood")),
    ("text", "His Service",
     f"At any hour, while you are within {workers.PACT['REACH']} blocks of the perch, he grafts a host crop on a bed of "
     "yours or your party's in his area with one donor (a **Dewmoss** for a Sunpetal) from the store beside him, one graft "
     f"at a time, at most {pacts.SPIRITS['foras']['pact']['service']['quota']} a game day. He takes only from a store you "
     "could open and change yourself, grafts only a crop and bed you could change by hand, and never replaces a graft the "
     "rules know. A graft stays through every harvest and replanting of its host; if another crop grows there it is lost, "
     "and when the host returns he grafts it again. After each graft he says how many he has made today.",
     ("garden_regard", "understood")),
    ("text", "The Logician's Objection",
     "A Dewlit graft narrows what a Sunpetal can bear: on a dry bed it would harm. So Foras grafts only where the graft "
     "makes the host's place suit it better, read from the bed's last reading, and names the rest: \"Hosts here a graft "
     "would not help, or would harm: 3, the nearest at x, y, z. Shall I graft them anyway?\" Answer on the pact's page at "
     "the perch: **"
     + pacts.SERVICE_WORDS["graft_keeping"]["accords"]["prudent"] + "**, or **"
     + pacts.SERVICE_WORDS["graft_keeping"]["accords"]["always"] + "**. Until you answer, the roster says \"awaiting your "
     "answer\" and he keeps to the first. You may change your answer whenever you like; nothing is spent either way.",
     ("garden_regard", "understood")),
    ("text", "His Price",
     "The compact, paid as you accept: **"
     + " and ".join(("a" if cost["count"] == 1 else str(cost["count"])) + " "
                    + {rid("dewmoss"): "Dewmoss", "minecraft:honeycomb": "honeycomb"}[cost["item"]]
                    for cost in pacts.SPIRITS["foras"]["pact"]["compact"]["items"])
     + "**. Then a honeycomb every " + str(pacts.SPIRITS["foras"]["pact"]["upkeep"]["period_days"]) + " game days, up to "
     + str(pacts.SPIRITS["foras"]["pact"]["upkeep"]["cap"]) + " weeks paid ahead; a missed week is warned and he carries "
     "on through a day's grace, then the pact is suspended with nothing lost. Your first obligation: graft a host by hand "
     "in his area while the pact is active. Ending it returns what was paid ahead once: he bows, folds his satchel and "
     "fades.", ("garden_regard", "understood")),
    # GC-08 unit 5
    # GC-09 unit 6
]


def codex():
    research = "garden_regard"
    specimens = "\\\n".join(f"- {name}" for name in SPECIMENS.values())
    x, y = CODEX_PLACE
    pages = [
        ("text", "The Garden's Regard",
         "Some spirits keep gardens, and each treats only with someone who has grown and harvested for themselves. Once you "
         "understand the Binding Arts and Verdant Husbandry, **sneak and use** one of these in the air (on a bed it plants "
         "instead) to examine it:\\\n" + specimens + "\\\n\\\nEach says so in its tooltip. Once you have begun, your journal's "
         "**Pacts** section names each next step."),
        ("text", "Its Evidence",
         "Then, in any order: **harvest** a ripe organism crop by hand, and keep a **bound worker** at its work: a "
         "familiar's mending, a spirit's delivery or a construct's trip. Only what you do once you understand both counts, "
         "so do each again if you did it before. **Research Notes** never stand in for any of it."),
        ("crafting_recipe", "Pact Perch",
         "A garden spirit comes to a perch you placed, beside your own Verdant Beds, where you may build. It holds nothing, "
         "and breaking it never ends a pact.", rid("pact_perch")),
        ("text", "Understood",
         "The garden's spirits will hear an invitation now. Use your perch: each names its terms before anything is spent, "
         "none asks for an astrolabe, and each works only your beds (or your party's) while you are near.",
         (research, "understood")),
    ] + PAGES
    return {("binding", research): {
        "name": "The Garden's Regard", "x": x, "y": y, "icon": rid("verdant_bed"),
        "condition": ("binding_arts", "understood"),
        "description": "What the garden's spirits ask before they will come: research", "pages": pages}}


def lang_entries(lang):
    lang[f"tag.item.{SPECIMEN_TAG.replace(':', '.')}"] = "Things the Garden's Spirits Regard"


def tags(tags):
    for ref in SPECIMENS:
        tags.add("item", SPECIMEN_TAG, ref)
