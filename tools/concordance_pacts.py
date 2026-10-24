"""Pacts with the Goetia's spirits, Stolas first (Concordance Chunk D; docs/features/concordance-chunk-d.md and
docs/features/stolas-pact.md).

D-03 (D-S12): Stolas's Regard, the research a player completes before Stolas will hear them, found through
Styxhexenhammer's hints or, without him, through the codex's Binding category and a feather.
- Its evidence is personal and server-made, and each kind is the deed itself: examine a thing Stolas regards
  (#jugcraft:stolas_specimens: three of Styx's flowers and a feather, which anyone has without Styx); harvest a ripe
  organism crop by hand (the Greenwardens' cultivation); dissolve a sample at the Assayer's Scale (the Balancewrights'
  assay); and, for mastery, keep a pact's first obligation (the practice jugcraft:pact_service, which only the pact
  records, D-05; its gate is tools/concordance_progression.py's PRACTICES). No state takes notes.
- The sky step (one observation recorded at an Orrery Observatory) is the invitation's precondition, not research, so
  Styx and the journal name it apart: ResearchEngine.explain does not cover it.
- In the game only the Binding Arts gate the entry (its requires); the practices' gates put Verdant Husbandry and the
  Assay understood before their practice in the progression graph, which so asks more than the game, never less.
D-04 (D-S6, D-S7, D-S8, D-S13 to D-S15, D-S19, D-S23): the pact perch, Stolas's offer as data, the Open Hand Compact
and the pact menu's protocol. D-05 (D-S1, D-S3, D-S6, D-S12, D-S16): Star-reckoning, the first obligation and keeping.

Java: concordance/spirits/StolasRegard.java, PactPerchBlock(Entity), PactMenu, PactRequestPayload, PactStatePayload,
PactSessions, PactCompact, PactKeeping, StarReckoning, StolasEntity; concordance/worker/PactDefinition, PactParser,
PactCatalog. tools/concordance.py merges these tables into its own; Styx's words are tools/styx.py's;
tools/check_mod_data.py (check_stolas_regard, check_pacts, check_pact_screen) holds the Java and the data to them.
"""

MOD = "jugcraft"


def rid(path):
    return f"{MOD}:{path}"


REGARD = rid("stolas_regard")  # Java: spirits/StolasRegard.RESEARCH
PACT_SERVICE = rid("pact_service")  # Java: StolasRegard.ACTIVITY (its gate: concordance_progression.PRACTICES)
CULTIVATION = rid("cultivation")  # Java: garden/Garden.ACTIVITY
ASSAY = rid("assay")  # Java: assay/Assaying.ACTIVITY
STOLAS_SPECIMEN_TAG = f"{MOD}:stolas_specimens"  # Java: StolasRegard.SPECIMENS
STYX_SPECIMENS = {rid("stolas_starflower"): "Stolas Starflower", rid("ravenquill_lupine"): "Ravenquill Lupine",
                  rid("amethyst_mourningbell"): "Amethyst Mourningbell"}
# The specimen anyone has without Styx, who is missing from old saves and never respawned (G12; CLAUDE.md).
PLAIN_SPECIMENS = {"minecraft:feather": "Feather"}
# What Stolas waits for next (Java: StolasRegard.Step, in this order); Styx's words for each are tools/styx.py's.
STEPS = ["binding", "examine", "cultivate", "assay", "serve", "kept", "journal"]

RESEARCH = {"stolas_regard": {
    "name": "Stolas's Regard", "principle": "tether", "tradition": "spiritbinders", "stage": "practitioner",
    "icon": rid("stolas_starflower"),
    "requires": [{"research": rid("binding_arts"), "state": "understood"}],
    "states": {
        "encountered": [{"type": "examine", "specimens": f"#{STOLAS_SPECIMEN_TAG}"}],
        "observed": [{"type": "practice", "activity": CULTIVATION}],
        "understood": [{"type": "practice", "activity": ASSAY}],
        "mastered": [{"type": "practice", "activity": PACT_SERVICE}],
    },
    "unlocks": {},
    "advancements": {
        "encountered": ("Regarded", "Examine something Stolas would look at closely: a feather, or one of "
                        "Styxhexenhammer's flowers", "task", rid("stolas_starflower")),
        "observed": ("What Grows", "Harvest a ripe organism crop by hand", "task", rid("sunpetal")),
        "understood": ("Stolas's Regard", "Understand Stolas's Regard: dissolve a sample at an Assayer's Scale", "goal",
                       rid("assayers_scale")),
        "mastered": ("Star-Reckoned", "Keep your pact with Stolas: record a sky observation while it holds", "challenge",
                     rid("observatory")),
    },
    "locked": {"encountered": "Examine a feather or one of Styxhexenhammer's flowers first",
               "observed": "Harvest an organism crop first", "understood": "Understand Stolas's Regard first",
               "mastered": "Master Stolas's Regard first"},
}}

MESSAGES = {"examine.regard": "You look at the %s as closely as Stolas would"}
TOOLTIPS = {"stolas_specimen": "Stolas would look at this closely: sneak and use to examine it"}
# The journal's pacts section (Java: StolasRegard.report): Stolas's Regard's next step, and the sky step apart.
JOURNAL = {
    "pacts.regard.binding": "Stolas's Regard: understand the Binding Arts first",
    "pacts.regard.examine": "Stolas's Regard: examine a feather or one of Styxhexenhammer's flowers (sneak and use it)",
    "pacts.regard.cultivate": "Stolas's Regard: harvest a ripe organism crop by hand",
    "pacts.regard.assay": "Stolas's Regard: dissolve a sample at an Assayer's Scale",
    "pacts.regard.serve": "Stolas's Regard is understood: invite him at a pact perch and keep the pact's first obligation",
    "pacts.regard.kept": "Stolas's Regard is mastered",
    "pacts.regard.journal": "Stolas's Regard: the Research section says what comes next",
    "pacts.sky_needed": "Before Stolas will come: record a sky observation at an Orrery Observatory while a pattern is "
                        "up there (this is not research, and nothing is recorded for a sky you did not watch)",
    "pacts.sky_recorded": "A sky observation is recorded: Stolas will come when invited",
    "pacts.sky_count": "(%s patterns observed)",
}


def lang_entries(lang):
    lang[f"tag.item.{STOLAS_SPECIMEN_TAG.replace(':', '.')}"] = "Things Stolas Regards"
    for key, text in JOURNAL.items():
        lang[f"journal.{MOD}.{key}"] = text


def codex():
    import concordance_celestial as celestial
    research = "stolas_regard"
    specimens = "\\\n".join(f"- {name}" for name in {**STYX_SPECIMENS, **PLAIN_SPECIMENS}.values())

    def when(pattern):
        info = celestial.PATTERNS[pattern]
        return f"every {info['period']} days {'in clear weather' if info['clear_sky'] else 'in any weather'}"
    entries = {("binding", research): {
        "name": "Stolas's Regard", "x": 0, "y": 4, "icon": rid("stolas_starflower"),
        "condition": ("binding_arts", "understood"), "description": "Stolas, the raven who teaches the stars: research",
        "pages": [
            ("text", "Stolas's Regard",
             "Stolas, a raven among the spirits, teaches the stars, herbs and stones, and treats only with someone who has "
             "done each for themselves. Once you understand the Binding Arts, **sneak and use** one of these to examine "
             "it:\\\n" + specimens + "\\\n\\\nStyxhexenhammer grows the flowers in his conservatory and tells whoever "
             "asks how far they have come; a feather serves as well, so nobody needs him."),
            ("text", "His Evidence",
             "Then, in any order: **harvest** a ripe organism crop by hand (a Verdant Bed's, which a Greenwarden wakes), "
             "and **dissolve** a sample at an **Assayer's Scale**. Only what you do once you understand the Binding Arts "
             "counts, so do each again if you did it before. **Research Notes** never stand in for any of it."),
            ("text", "The Sky",
             "Stolas comes only to someone who has seen a star themselves: record a sky observation at an **Orrery "
             "Observatory** while a pattern is up and visible there. It is not research, so your journal's **Pacts** "
             "section names it apart. An observation made long ago still counts; nothing is recorded for a sky you did "
             f"not watch. The Lantern Star comes round {when('lantern_star')}, the New Moon {when('new_moon')}."),
            ("text", "Understood", "Stolas will hear an invitation now: build a **pact perch** where you may build, and "
             "use it. He names his terms before anything is spent.", (research, "understood")),
            ("text", "Mastered", "You kept your pact's first obligation.", (research, "mastered")),
        ]}}
    return entries


def tags(tags):
    for ref in {**STYX_SPECIMENS, **PLAIN_SPECIMENS}:
        tags.add("item", STOLAS_SPECIMEN_TAG, ref)
