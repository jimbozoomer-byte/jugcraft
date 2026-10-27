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
data to them. Each spirit's unit adds its work to OBLIGATIONS and CONSTANTS and its codex page to PAGES (GC-04: Buer's).
"""

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
ACTS = ["tend"]
# Each act's one-line hook: (Java file under concordance/, the member it is in, the line, what it follows, what it precedes).
HOOKS = {"tend": ("garden/VerdantBedBlock.java", "static void tendBy(",
                  "PactObligations.performed(level, player, PactObligations.Act.TEND, pos);",
                  "if (bed.tend(level.getGameTime())) {", "} else {")}
OBLIGATIONS = {}  # work -> act (each act at most one work). GC-04: "strain_tending": "tend"
CONSTANTS = {}  # work -> {Java file under concordance/: {constant: value}}; each spirit's unit adds its own (check_garden_pacts)
REACH = {"MAX_BEDS": 64, "REFRESH_TICKS": 200}  # Java: spirits/GardenReach (GC-P2)
CODEX_PLACE = (6, 4)  # the Binding category's map: (0..6, 0..4) holds eleven entries; (6, 4) is free (check_concordance)
# GC-05 (GC-P45, GC-P54): a device index (GardenReach.devices) holds at most this many Hearts, Gleaners and Ley Pylons, each.
REACH["MAX_DEVICES"] = 16  # Java: spirits/GardenReach

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
    **{f"garden_pact.act.{act}": text for act, text in {"tend": "tend a strained Verdant Bed"}.items()},
}
# Under tooltip.jugcraft.concordance. (tools/concordance.py TOOLTIPS).
TOOLTIPS = {"garden_regard_specimen": "The garden's spirits regard this: sneak and use it in the air to examine it"}

# Each spirit's codex page, in its unit (GC-04 appends Buer's): ("text", name, words, ("garden_regard", "understood")).
PAGES = []


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
