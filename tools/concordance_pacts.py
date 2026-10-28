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

Java: concordance/spirits/StolasRegard.java, PactRegards, PactPerchBlock(Entity), PactMenu, PactRequestPayload,
PactStatePayload, PactSessions, PactCompact, PactKeeping, PactSpiritEntity (every pact spirit's shared body: the garden
cohort, GC-01), PactSpirits, PactService, PactServices, PactAccords (a pact's accord: the garden cohort, GC-05),
StarReckoning, StolasEntity, BuerEntity and StrainTending (the garden cohort, GC-04: Buer, his body adapted by
tools/garden_bodies.py), BathinEntity and VerdanceCarrying (GC-05: Bathin, his body built by tools/garden_bodies.py from
BUILT); concordance/worker/PactDefinition,
PactParser, PactCatalog; client/PactClient, PactScreen and PactSpiritRenderers (each spirit's renderer from its SPIRITS
row; Stolas's raven body, D-06 (D-S5): the Flashjack rig's geometry copied by tools/owner_art.py from OWNER_FILES, its
skin, glowmask and animations adapted by tools/stolas_body.py from ADAPTED). tools/concordance.py merges these tables into
its own; Styx's words are tools/styx.py's; tools/check_mod_data.py (check_stolas_regard, check_pacts, check_pact_screen,
check_spirit_bodies, check_garden_pacts) holds the Java and the data to them.
"""

import concordance_ecology as ecology
import concordance_workers as workers

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


# ------------------------------------------------------------------------------------------- D-04: the pact perch

# Java: worker/PactParser.java (check_pacts compares).
PARSER = {"MAX_COMPACT_ITEMS": 4, "MAX_COMPACT_COUNT": 64, "MAX_RESONANCE": 32, "MAX_BEATS": 3}
WORK = ["star_reckoning", "strain_tending", "verdance_carrying",
        # GC-06 unit 3
        # GC-07 unit 4
        # GC-08 unit 5
        # GC-09 unit 6
        ]
# The works that serve only their holder (Java: PactParser.HOLDER_ONLY): a definition with one must be "personal".
HOLDER_ONLY = ["star_reckoning"]
# Each work's service class (Java: spirits/<class>.java implements PactService, its WORK constant; registered once in
# Workers.register). Append with each work (GC-04: "strain_tending": "StrainTending"; GC-05: "verdance_carrying").
SERVICE_CLASSES = {"star_reckoning": "StarReckoning", "strain_tending": "StrainTending", "verdance_carrying": "VerdanceCarrying",
                   # GC-06 unit 3
                   # GC-07 unit 4
                   # GC-08 unit 5
                   # GC-09 unit 6
                   }
# Java: spirits/PactStatePayload.java (MAX_*), PactSessions.java (TIMEOUT_TICKS), PactMenu.java (RATE_TICKS),
# PactPerchBlockEntity.java (LOOK_TICKS), PactKeeping.java (MAX_OFFERINGS).
STATE_LIMITS = {"MAX_BEATS": 3, "MAX_TERMS": 16, "MAX_NOTES": 12, "MAX_CHOICES": 8, "MAX_COMPACT": 8, "MAX_KEY": 64,
                "MAX_ID": 32767}
SESSION = {"TIMEOUT_TICKS": 6000, "RATE_TICKS": 4, "LOOK_TICKS": 20, "MAX_OFFERINGS": 8}
# Sent as numbers or ids (PactRequestPayload.ACTIONS, PactStatePayload's modes and TERMS, PactMenu's own reasons): append only.
ACTIONS = ["choose", "accept", "decline", "recall", "deposit", "suspend", "resume", "end", "seat", "invite"]
MODES = ["negotiate", "keep", "seat", "closed", "invite"]
TERMS = ["service", "hours", "area", "quota", "upkeep", "grace", "suspension", "duration", "breach", "ending", "returns", "perch",
         "beneficiaries", "compact"]
ANSWERS = ["changed", "too_fast", "not_offered", "sealed", "declined", "disabled", "no_definition", "missing_evidence",
           "no_observation", "deposited", "nothing_deposited", "invited"]
FLASHJACK = "assets/ars_jymbaumental/"
# The owner's Decor Blossom, Buer's body (the garden cohort, GC-04; art plan). Catalog rows: art/owner-library/catalog/
# files.csv:19364 (geometry), :19091 (animations), :20417 (texture, 32x32).
BLOSSOM = "assets/ars_jimbaux/"
# The owner's paintings Bathin's skin is painted from (the garden cohort, GC-05; art plan): under
# art/owner-library/originals/Blocks/, catalog files.csv:2164 (the willow log's bark: his coil and his skin) and :2069 (the
# Spanish moss: his mantle, beard, hair, belly scutes and eyes). Colours only are read; no texel, shape or layout is copied.
WILLOW = "biomes and tree blocks/willow_log.png"
MOSS = "biomes and tree blocks/spanish_moss.png"
# GC-06 unit 3
# GC-07 unit 4
# GC-08 unit 5
# GC-09 unit 6
# Concordance garden cohort, GC-01 (SG11; docs/features/concordance-garden-cohort.md): one row a pact spirit. Its offer
# (data/jugcraft/concordance/pact/<name>.json), offerings tag, three beats, service term, cues and body are written from
# here; PACTS, ENTITY_NAMES, OFFERINGS, SOUND_EVENTS, SOUND_FILES, OWNER_FILES, ADAPTED and Stolas's body tables below are
# derived from it, so with Stolas's row alone every generated file is as Chunk D left it. Java: Workers (the entity's size,
# MISC, fire immune, tracking 8), spirits/PactSpirits (its sign), spirits/<java> (its cues and gestures),
# client/PactSpiritRenderers (its render row); worker/PactParser.REQUIRED lists the rows in order. check_pacts and
# check_spirit_bodies hold them together. A row's key is its entity path and its counterpart jugcraft:<key>.
# Stolas's offer (data/jugcraft/concordance/pact/stolas.json; Java: worker/PactParser.java). Starting values, recorded in the
# As built note: 8 blocks round the perch, any hour, once a game day; one offering every 7 game days, a day of grace, up to
# four ahead (D-S3); the Open Hand Compact two amethyst shards, three feathers and 8 Astral Resonance (D-S13). Ending a
# pact and sealing it anew could give a second Star-reckoning in one game day, but only for a whole new compact (its
# materials and resonance) each time, which makes nothing: no positive-gain loop (critique 10; the balance note says so).
SPIRITS = {
    "stolas": {
        "name": "Stolas",
        "java": "StolasEntity",
        "entity": {"width": 0.7, "height": 1.45, "eye": 1.2},
        "station": "perch",
        "sign": rid("stolas_starflower"),
        # His offer, as PACTS["stolas"] was (its beats are the keys of "beats" below, in order).
        "pact": {
            "agreement": rid("stolas_pact"),
            "research": rid("stolas_regard"),
            "service": {"work": "star_reckoning", "radius": 8, "from": 0, "to": 24000, "quota": 1},
            "upkeep": {"offering": rid("stolas_offerings"), "count": 1, "period_days": 7, "grace_days": 1, "cap": 4},
            "beneficiaries": "personal",
            "compact": {"items": [{"item": "minecraft:amethyst_shard", "count": 2}, {"item": "minecraft:feather", "count": 3}],
                        "resonance": 8},
        },
        # The offerings (D-S3): the three Styx flowers and, reachable without Styx, an amethyst shard. The tag ships with
        # the definition (D-04); D-05's upkeep consumes it.
        "offerings": {"name": "Stolas's Offerings",
                      "items": [rid("stolas_starflower"), rid("ravenquill_lupine"), rid("amethyst_mourningbell"),
                                "minecraft:amethyst_shard"]},
        # His three beats (provisional words; his character is a checkpoint question). Shortened for the magic interfaces
        # (docs/features/magic-interfaces.md, spec 1.5; decided by the session in answer to the owner's request for far
        # less reading, reviewable at the checkpoint): a line of at most 18 words each, the greeting's stage direction (at
        # most 8) first and also its own key (stage, below), so the screen can dim it.
        "stage": "A raven settles and studies you.",
        "beats": {
            "greeting": "A raven settles and studies you. \"You watched the sky and asked the flowers. I am Stolas.\"",
            "service": "\"Once a day, here, I turn the heavens back to a star you saw.\"",
            "price": "\"An offering every seven days, kept ahead. Miss one and I wait.\"",
        },
        # Its service's term (D-S15): moved from MESSAGES "pact.term.service.star_reckoning", verbatim.
        "term": "Star-reckoning: once a game day at this perch you recall any sky pattern you have "
                "observed yourself, paying its recall cost in Astral Resonance from your astrolabe",
        # Its cues, in StolasEntity.SOUND_IDS' order: (subtitle, runtime files): moved from SOUND_EVENTS and SOUND_FILES.
        "cues": {
            "arrive": ("Stolas alights", [f"menagerie/beast/flutter_flap_{n}" for n in range(3)]),
            "listen": ("Stolas listens", ["menagerie/cavern/block/spelunkery_table/spelunkery_table_paper_insert"]),
            "inspect": ("Stolas inspects", ["menagerie/cavern/block/spelunkery_table/spelunkery_table_paper_remove"]),
            "assent": ("Stolas assents", ["household/block/doorbell/chime"]),
            "refuse": ("Stolas refuses", [f"menagerie/beast/crow_idle_{n}" for n in range(4)]),
            "depart": ("Stolas departs", [f"menagerie/cavern/mob/vesper/vesper_flap_{n}" for n in range(4)]),
        },
        # His body (D-S5, D-06): moved from OWNER_FILES, ADAPTED, BODY_SCALE, LOOPS, GESTURES, GESTURE_TIMES, LOOP_LENGTHS,
        # ATTEND, EYE_TEXELS and BODY_SHOTS verbatim, with the renderer's numbers Chunk D's client/WorkerClient.java drew him
        # with (scale, shadow, culling box, glowmask).
        "body": {
            "owner_files": {"geckolib/models/entity/stolas.geo.json": FLASHJACK + "geo/flashjack.geo.json"},
            "adapted": {
                "textures/entity/stolas.png": (FLASHJACK + "textures/entity/flashjack.png", "skin"),
                "textures/entity/stolas_glowmask.png": (FLASHJACK + "textures/entity/flashjack.png", "glowmask"),
                "geckolib/animations/entity/stolas.animation.json": (FLASHJACK + "animations/flashjack.animation.json", "animations"),
            },
            "scale": 1.4,
            "render": {"shadow": 0.4, "wide": 1.2, "low": 0.1, "above": 2.0, "glow": True},
            "loops": {"idle": "animation.stolas.idle", "listen": "animation.stolas.listen", "waiting": "animation.stolas.waiting",
                      "suspended": "animation.stolas.suspended", "astir": "idle.flapping"},
            "gestures": {"arrive": "animation.stolas.arrive", "inspect": "animation.stolas.inspect", "assent": "animation.stolas.assent",
                         "refuse": "animation.stolas.refuse", "depart": "animation.stolas.depart"},
            "gesture_times": {"arrive": (2.0, (0.70, 1.20)), "inspect": (2.5, (0.40, 1.10)), "assent": (2.0, (0.45, 1.25)),
                              "refuse": (2.0, (0.50, 1.30)), "depart": (2.0, (0.30, 0.80))},
            "loop_lengths": {"idle": 4.0, "listen": 3.0, "waiting": 4.0, "suspended": 6.0},
            "attend": {"head": (6, 8, -16), "head_position": (0, 0, -0.5), "body": (4, 0, 0), "wing": (0, 0, -6)},
            "eye_texels": ((9, 10), (9, 11), (14, 10), (14, 11)),
            "shots": ["jugcraft_stolas_idle", "jugcraft_stolas_listen", "jugcraft_stolas_suspended", "jugcraft_stolas_waiting",
                      "jugcraft_stolas_inspect", "jugcraft_stolas_assent", "jugcraft_stolas_refuse", "jugcraft_stolas_depart",
                      "jugcraft_stolas_arrive"],
        },
    },
    # Concordance garden cohort, GC-04 (Dossier 1; docs/features/buer-pact.md): Buer, the tenth spirit, strain tending.
    "buer": {
        "name": "Buer",
        "java": "BuerEntity",
        "entity": {"width": 1.2, "height": 1.6, "eye": 1.4},
        "station": "perch",
        "sign": rid("mendvetch"),
        "pact": {
            "agreement": rid("buer_pact"),
            "research": rid("garden_regard"),
            "service": {"work": "strain_tending", "radius": 8, "from": 0, "to": 12000, "quota": 16},
            "upkeep": {"offering": rid("buer_offerings"), "count": 1, "period_days": 7, "grace_days": 1, "cap": 4},
            "beneficiaries": "party",
            "compact": {"items": [{"item": rid("mendvetch"), "count": 4}, {"item": "minecraft:bone_meal", "count": 4}],
                        "resonance": 0},
        },
        # His offering (GC-P21): Mendvetch, the healing crop; no Styx gift.
        "offerings": {"name": "Buer's Offerings", "items": [rid("mendvetch")]},
        # His three beats (GC-P20; GC-P29 adds the stage direction before the greeting, as Stolas's has). No source
        # quotation in game (GC-S14). Shortened for the magic interfaces (spec 1.5; decided by the session, reviewable at
        # the checkpoint), as Stolas's are.
        "stage": "A great blossom opens on the perch.",
        "beats": {
            "greeting": "A great blossom opens on the perch. \"A bed that is worked must also be mended. I am Buer; I "
                        "mend.\"",
            "service": "\"By daylight I ease your harvests' strain, one bed at a time.\"",
            "price": "\"One Mendvetch a week. Feed an overworked bed before you ask again.\"",
        },
        # His numbers from the tables that hold the Java's (garden/Garden.TEND_TICKS, worker/Pacts.REACH); his service's
        # two conditions, the holder near and only what the holder could change by hand, named before anything is spent
        # (the unit review).
        "term": "Strain tending: by daylight Buer eases one harvest strain from one awake Verdant Bed of yours or your "
                "party's in his area at a time, each bed no sooner than its own tending allows (the "
                f"{ecology.TEND_TICKS // 20} seconds a Greenwarden's hand waits, shared with yours). He moves no nutrients "
                f"and no items. He works only while you are within {workers.PACT['REACH']} blocks of the perch, and never "
                "on a bed you could not change by hand",
        # His cues, in BuerEntity.SOUND_IDS' order: (subtitle, runtime files). The art plan's mono files (GC-S13); two are
        # one recording, so a pitch tells listen, refuse and depart apart (GC-P26, "pitch" below).
        "cues": {
            "arrive": ("Buer unfurls", ["menagerie/fable/butterfly_heal"]),
            "listen": ("Buer listens", ["alchemy/peheal"]),
            "inspect": ("Buer considers", ["alchemy/peheal"]),
            "assent": ("Buer assents", ["menagerie/grove/entity/cactem/entity.cactem.heal"]),
            "refuse": ("Buer refuses", ["alchemy/peheal"]),
            "depart": ("Buer closes and sinks away", ["menagerie/fable/butterfly_heal"]),
            "work": ("Buer mends a bed", ["menagerie/grove/item/item.heal_spell_book.player_heal"]),
        },
        "pitch": {"listen": 1.25, "refuse": 0.6, "depart": 0.7},
        # His body (GC-S12, GC-P24, GC-P25): the Decor Blossom's texture byte for byte; its geometry, glowmask and
        # animations adapted by tools/garden_bodies.py (adapter "garden_bodies.<function>").
        "body": {
            "owner_files": {"textures/entity/buer.png": BLOSSOM + "textures/block/decor_blossom.png"},
            "adapted": {
                "geckolib/models/entity/buer.geo.json": (BLOSSOM + "geo/decor_blossom.geo.json", "garden_bodies.buer_model"),
                "textures/entity/buer_glowmask.png": (BLOSSOM + "textures/block/decor_blossom.png", "garden_bodies.buer_glowmask"),
                "geckolib/animations/entity/buer.animation.json": (BLOSSOM + "animations/decor_blossom_animations.json",
                                                                   "garden_bodies.buer_animations"),
            },
            "scale": 2.2,
            "render": {"shadow": 0.6, "wide": 0.4, "low": 1.0, "above": 0.5, "glow": True},
            # His loops (BuerEntity.Loop; "idle" is the owner's own "active") and gestures (BuerEntity.Gesture).
            "loops": {"idle": "active", "listen": "animation.buer.listen", "waiting": "animation.buer.waiting",
                      "suspended": "animation.buer.suspended", "astir": "animation.buer.astir"},
            "gestures": {"arrive": "animation.buer.arrive", "inspect": "animation.buer.inspect",
                         "assent": "animation.buer.assent", "refuse": "animation.buer.refuse",
                         "depart": "animation.buer.depart", "work": "animation.buer.work"},
            # (seconds, (the telling pose's window)): ConcordanceGardenPactClientGameTests freezes the world inside it.
            "gesture_times": {"arrive": (2.5, (1.2, 1.9)), "inspect": (2.0, (0.4, 1.2)), "assent": (2.0, (0.5, 1.3)),
                              "refuse": (2.0, (0.4, 1.3)), "depart": (2.0, (1.1, 1.6)), "work": (3.0, (1.4, 2.2))},
            "loop_lengths": {"listen": 3.0, "waiting": 6.0, "suspended": 8.0, "astir": 2.0},
            # His listening pose (degrees): the pistil leaned toward his speaker; every screen gesture starts and ends on it.
            "attend": {"pistil": (12, 0, 0)},
            # The texels his glowmask lights (the pistil cap's up face, red full; measured from the owner's sheet).
            "glow_texels": ((16, 6), (16, 7), (17, 7)),
            # The stem cube repeated this many times upward, and the faces dropped (bone, cube index after the copies).
            "stem_copies": 1,
            "dropped": {("base", 2): "down", ("pistil", 0): "down", ("pistil", 1): "north", ("pistil", 2): "north"},
            "shots": ["jugcraft_garden_buer_idle", "jugcraft_garden_buer_listen", "jugcraft_garden_buer_asleep",
                      "jugcraft_garden_buer_suspended", "jugcraft_garden_buer_inspect", "jugcraft_garden_buer_assent",
                      "jugcraft_garden_buer_refuse", "jugcraft_garden_buer_work", "jugcraft_garden_buer_depart",
                      "jugcraft_garden_buer_arrive"],
        },
    },
    # Concordance garden cohort, GC-05 (Dossier 2; docs/features/bathin-pact.md): Bathin, the eighteenth spirit, Verdance
    # carrying. His body is built (tools/garden_bodies.py), not adapted: no owner rig fits a strong man on a serpent's coil.
    "bathin": {
        "name": "Bathin",
        "java": "BathinEntity",
        # His box holds his coil (0.875 across); his shoulders (1.25) reach past it as a vanilla mob's arms do (GC-P56).
        "entity": {"width": 0.9, "height": 1.9, "eye": 1.65},
        "station": "perch",
        # His sign: the ender pearl, the thing that goes from one place to another at once (GC-P58).
        "sign": "minecraft:ender_pearl",
        "pact": {
            "agreement": rid("bathin_pact"),
            "research": rid("garden_regard"),
            "service": {"work": "verdance_carrying", "radius": 10, "from": 0, "to": 24000, "quota": 12},
            "upkeep": {"offering": rid("bathin_offerings"), "count": 4, "period_days": 7, "grace_days": 1, "cap": 4},
            "beneficiaries": "party",
            "compact": {"items": [{"item": "minecraft:ender_pearl", "count": 2}, {"item": rid("verdant_chaff"), "count": 8}],
                        "resonance": 0},
        },
        # His offering (GC-P41): Verdant Chaff, what a harvest leaves; no Styx gift.
        "offerings": {"name": "Bathin's Offerings", "items": [rid("verdant_chaff")]},
        # His three beats (GC-P40; GC-P58 adds the stage direction before the greeting, as Stolas's and Buer's have). No
        # source quotation in game (GC-S14). Shortened for the magic interfaces (spec 1.5; decided by the session,
        # reviewable at the checkpoint), as Stolas's are.
        "stage": "A serpent-tailed man coils on the perch.",
        "beats": {
            "greeting": "A serpent-tailed man coils on the perch. \"Roads are for those who must walk them. I am Bathin.\"",
            "service": "\"What your Hearts hold, I set down where it is wanted.\"",
            "price": "\"Chaff, four to the week. I carry the green; keep me in husk.\"",
        },
        # His numbers from the tables that hold the Java's (garden/GleanerBlockEntity: the Gleaner's own draw and reach;
        # worker/Pacts.REACH); his service's two conditions, the holder near and only what the holder could change by hand,
        # named before anything is spent (as Buer's, GC-04's unit review).
        "term": "Verdance carrying: at any hour Bathin moves Verdance at once from an awake Verdant Heart of yours or your "
                f"party's in his area, up to {ecology.GLEANER_DRAW} at a time into a Gleaner of yours or your party's more "
                f"than {ecology.HEART_REACH} blocks from that Heart, or into a Ley Pylon of yours, your party's or no one's, "
                "not beside it, at the Heart's own 3 for 2; one move at a time, nothing ever on the way and none kept by him. A Heart that already pours into its own "
                f"pylon he leaves alone until you answer. He works only while you are within {workers.PACT['REACH']} blocks of "
                "the perch, and never on a Heart, Gleaner or pylon you could not change by hand",
        # His cues, in BathinEntity.SOUND_IDS' order: (subtitle, runtime files). The art plan's mono files (GC-S13): the
        # conch's two recordings and the sea staff's three bubbles, told apart by pitch (GC-P58, "pitch" below).
        "cues": {
            "arrive": ("Bathin is suddenly here", ["menagerie/cavern/item/magic_conch/magic_conch_summon"]),
            "listen": ("Bathin listens", ["menagerie/cavern/item/sea_staff/sea_staff_bubble_1"]),
            "inspect": ("Bathin weighs it", ["menagerie/cavern/item/sea_staff/sea_staff_bubble_2"]),
            "assent": ("Bathin assents", ["menagerie/cavern/item/magic_conch/magic_conch_cast"]),
            "refuse": ("Bathin refuses", ["menagerie/cavern/item/magic_conch/magic_conch_cast"]),
            "depart": ("Bathin is gone", ["menagerie/cavern/item/magic_conch/magic_conch_cast"]),
            "work": ("Bathin carries Verdance", ["menagerie/cavern/item/sea_staff/sea_staff_bubble_0"]),
        },
        # The departing conch at 1.8 lasts 1.0 s, his departure's length: an entity's sound stops when it is gone.
        "pitch": {"arrive": 1.5, "listen": 1.2, "refuse": 0.7, "depart": 1.8},
        # His body (GC-S12, GC-P56): every file built by tools/garden_bodies.py ("built": runtime path -> (the Blocks-folder
        # paintings it reads, its builder)), written and checked by tools/owner_art.py.
        "body": {
            "built": {
                "geckolib/models/entity/bathin.geo.json": ((), "garden_bodies.bathin_model"),
                "geckolib/animations/entity/bathin.animation.json": ((), "garden_bodies.bathin_animations"),
                "textures/entity/bathin.png": ((WILLOW, MOSS), "garden_bodies.bathin_skin"),
                "textures/entity/bathin_glowmask.png": ((WILLOW, MOSS), "garden_bodies.bathin_glowmask"),
            },
            "scale": 1.0,
            "render": {"shadow": 0.5, "wide": 0.3, "low": 0.1, "above": 0.3, "glow": True},
            # His loops (BathinEntity.Loop) and gestures (BathinEntity.Gesture), every clip his own.
            "loops": {"idle": "animation.bathin.idle", "listen": "animation.bathin.listen", "waiting": "animation.bathin.waiting",
                      "suspended": "animation.bathin.suspended", "astir": "animation.bathin.astir"},
            "gestures": {"arrive": "animation.bathin.arrive", "inspect": "animation.bathin.inspect",
                         "assent": "animation.bathin.assent", "refuse": "animation.bathin.refuse",
                         "depart": "animation.bathin.depart", "work": "animation.bathin.work"},
            # (seconds, (the telling pose's window)): ConcordanceGardenPactClientGameTests freezes the world inside it.
            "gesture_times": {"arrive": (1.5, (0.1, 0.6)), "inspect": (2.0, (0.4, 1.2)), "assent": (1.5, (0.3, 0.9)),
                              "refuse": (1.6, (0.3, 1.0)), "depart": (1.0, (0.25, 0.75)), "work": (1.5, (0.15, 0.75))},
            "loop_lengths": {"idle": 4.0, "listen": 3.0, "waiting": 2.0, "suspended": 6.0, "astir": 1.5},
            # His listening pose (degrees): arms folded, chest up, head inclined to his speaker; every screen gesture starts
            # and ends on it.
            "attend": {"waist": (-4, 0, 0), "head": (8, 0, 0), "arm_left": (-60, 0, 40), "arm_right": (-60, 0, -40)},
            # The texels his glowmask lights: his two eyes on the head's front face (garden_bodies.BATHIN_FACE).
            "eye_texels": ((83, 27), (86, 27)),
            # Detail (a): the ring of dust he arrives and departs in. Detail (b): the shimmer at each end of a carry. Each is
            # one of the Concordance's shared signs (tools/concordance_signs.py SIGNS; Java BathinEntity.RING, SHIMMER):
            # nothing in concordance/ sends particles of its own, so every client draws them as its settings allow.
            "ring": "shove",
            "shimmer": "work",
            "shots": ["jugcraft_garden_bathin_idle", "jugcraft_garden_bathin_listen", "jugcraft_garden_bathin_waiting",
                      "jugcraft_garden_bathin_suspended", "jugcraft_garden_bathin_inspect", "jugcraft_garden_bathin_assent",
                      "jugcraft_garden_bathin_refuse", "jugcraft_garden_bathin_work", "jugcraft_garden_bathin_depart",
                      "jugcraft_garden_bathin_arrive"],
        },
    },
    # GC-06 unit 3
    # GC-07 unit 4
    # GC-08 unit 5
    # GC-09 unit 6
}
# Derived (keep these names: tools/concordance.py, tools/owner_art.py, tools/stolas_body.py and check_mod_data read them).
PACTS = {name: dict(spirit["pact"], beats=list(spirit["beats"])) for name, spirit in SPIRITS.items()}
ENTITY_NAMES = {name: spirit["name"] for name, spirit in SPIRITS.items()}
# The order worker/PactParser.REQUIRED lists the shipped definitions in (the perch offers them in this order; GC-P15).
REQUIRED = [rid(name) for name in SPIRITS]
# Stolas's offerings tag and items, by their Chunk D names (each spirit's tag is its pact's upkeep offering).
OFFERINGS_TAG = SPIRITS["stolas"]["pact"]["upkeep"]["offering"]
OFFERINGS = SPIRITS["stolas"]["offerings"]["items"]
BLOCKS = {"pact_perch": {"name": "Pact Perch"}}
# One sentence, a clause a spirit (GC-06b: the clauses after Bathin's each under its unit's line, so their merges never meet).
PERCH_TOOLTIP = ("Use: invite a pact spirit whose Regard you understand (Stolas also wants a star you observed yourself; "
                 f"Buer an awake Verdant Bed of yours within {SPIRITS['buer']['pact']['service']['radius']} blocks; Bathin an "
                 "awake Verdant Heart of yours and an awake Gleaner of yours or a Ley Pylon of yours or no one's within "
                 f"{SPIRITS['bathin']['pact']['service']['radius']} blocks"
                 # GC-06 unit 3
                 # GC-07 unit 4
                 # GC-08 unit 5
                 # GC-09 unit 6
                 "); or keep the pact seated here. Break it and the pact stays yours, without its perch.")
# Polished deepslate, not a slab: the progression graph follows only listed vanilla items (a slab is not in
# concordance_progression.VANILLA_SOURCES).
RECIPE = {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": ["SAS", " L ", " D "],
          "key": {"S": "minecraft:stick", "A": "minecraft:amethyst_shard", "L": "minecraft:dark_oak_log",
                  "D": "minecraft:polished_deepslate"},
          "result": {"id": rid("pact_perch"), "count": 1}}


def _box(lo, hi, texture):
    return {"from": lo, "to": hi, "faces": {d: {"texture": f"#{texture}"} for d in ("north", "east", "south", "west", "up", "down")}}


# The block's shape is PactPerchBlock.SHAPE: a stone foot, a post and a crossbar (Jugcraft's own bench wood and anchor
# stone; provisional, D-06 may refine).
PERCH_MODEL = {"parent": "minecraft:block/block",
               "textures": {"particle": rid("block/lampwright_bench_wood"), "wood": rid("block/lampwright_bench_wood"),
                            "stone": rid("block/spirit_anchor_top")},
               "elements": [_box([3, 0, 3], [13, 2, 13], "stone"), _box([7, 2, 7], [9, 14, 9], "wood"),
                            _box([2, 14, 7], [14, 16, 9], "wood")]}

MESSAGES.update({
    "pact.perch_refused": "A pact perch stands only where you may build, placed by your own hand, and never in Styx's home",
    # Each spirit's three beats and its service's term (SPIRITS; GC-01).
    **{f"pact.{name}.beat.{beat}": text for name, spirit in SPIRITS.items() for beat, text in spirit["beats"].items()},
    # The greeting's stage direction alone (the magic interfaces draw it dim above his line; the beat keeps both words).
    **{f"pact.{name}.beat.greeting.stage": spirit["stage"] for name, spirit in SPIRITS.items() if spirit.get("stage")},
    **{f"pact.term.service.{spirit['pact']['service']['work']}": spirit["term"] for spirit in SPIRITS.values()},
    # Each term's value (D-S15); the screen labels them (screen.jugcraft.pact.term.<key>).
    "pact.term.hours.any": "Any hour of the day or night",
    "pact.term.hours": "From %s to %s",
    "pact.term.area": "Within %s blocks of the perch",
    "pact.term.quota": "%s a game day",
    "pact.term.upkeep": "%s × %s every %s game days, paid ahead into the pact's keeping, at most %s periods; the Open Hand "
                        "Compact pays the first",
    "pact.term.grace": "A missed offering: a warning, then %s game day of grace",
    "pact.term.suspension": "The service stops; nothing is taken, nothing is lost, nothing accrues; an offering resumes it. "
                            "Time you are away never counts",
    "pact.term.duration": "Until you end it",
    "pact.term.breach": "Only a missed offering suspends it; nothing else breaches it, and it never ends by itself",
    "pact.term.ending": "At the perch, or from your roster",
    "pact.term.returns": "The offerings kept for it come back to you, once",
    "pact.term.perch": "If the perch is broken or blown up, the pact and its offerings stay yours: seat it at a new perch, or end it",
    "pact.term.beneficiaries.personal": "You alone",
    "pact.term.beneficiaries.party": "You and your party",
    "pact.term.compact": "Paid as you accept: %s Astral Resonance from your astrolabe, and %s",
    "pact.term.compact_item": "%s × %s",
    "pact.term.compact_paid": "Paid when the pact was sealed",
    # What is missing. GC-01 (SG10, GC-P12): the shared lines that named Stolas take the spirit's name (%s).
    "pact.missing.owner": "Only this perch's owner may invite %s here",
    "pact.missing.prerequisite": "Not yet: first understand %s",
    "pact.missing.step": "Not yet: %s, %s: %s",
    "pact.missing.observation": "Not yet: Stolas wants a star you have seen yourself. Record one at an observatory while a "
                                "pattern is up",
    "pact.missing.held": "You already keep a pact with %s",
    # Readings.
    "pact.reading.taken": "This perch keeps another's pact",
    "pact.reading.unreadable": "This perch's pact cannot be read by this version; it is kept as it was",
    "pact.seat.offer": "Your pact with %s is without its perch: seat it here, or end it",
    "pact.unavailable": "%s's terms could not be read on this server (the pact's definition did not load); nothing can be "
                        "agreed until it does",
    "pact.ended_here": "The pact is ended. Use the perch again to invite %s anew",
    # Answers.
    "pact.answer.sealed": "The pact is sealed: %s keeps this perch",
    "pact.answer.declined": "You decline; %s leaves. Nothing was spent",
    "pact.answer.changed": "The terms changed since you read them; read them again. Nothing was agreed or spent",
    "pact.answer.too_fast": "One moment",
    "pact.answer.not_offered": "That cannot be done here now",
    "pact.answer.missing_evidence": "%s will not agree yet: see what is missing. Nothing was spent",
    "pact.answer.cannot_afford": "The Open Hand Compact asks for the materials listed and %s Astral Resonance in one astrolabe; you "
                                 "are short. Nothing was spent",
    # Keeping (PactKeeping).
    "pact.deposited": "%s %s paid ahead (%s of at most %s periods now held)",
    "pact.deposit_none": "Nothing was taken: the escrow holds all it may, or you carry none of that offering",
    "pact.departed": "%s departs (%s); %s offerings came back to you",
    "pact.returned": "%s returns to the perch; nothing was spent",
    "pact.row.state.active": "The pact is active",
    "pact.row.state.suspended": "Suspended by you: Stolas reckons nothing and nothing falls due until you resume it",
    "pact.row.state.upkeep_due": "An offering is due: give %s %s within %s game days, or the pact is suspended (nothing is lost)",
    "pact.row.state.upkeep_held": "Suspended for its upkeep: nothing is taken and nothing accrues; %s %s resumes it",
    "pact.row.lost": "%s is not at the perch: he returns while you are near it, if your roster (at most %s) has room",
    "pact.row.next": "Next offering: %s %s in %s game days (time away is never counted)",
    "pact.row.escrow": "Paid ahead: %s of at most %s periods",
    "pact.row.area": "At this perch, within %s blocks, from %s to %s",
    "pact.row.area_any": "At this perch, within %s blocks, at any hour",
    "pact.row.end": "Ending returns what is paid ahead to you, once, and %s departs; breaking the perch ends nothing",
})
# Concordance garden cohort, GC-01 (SG4, SG6, GB23; GC-P4): the chooser, a compact without resonance, a work this version
# does not know.
MESSAGES.update({
    # GC-01 (SG4): the chooser and its answer.
    "pact.invite.missing": "%s: %s",
    "pact.answer.invited": "You invite %s to the perch. Nothing is spent or agreed until you accept",
    # GB23 (GC-P4): a compact without resonance names no astrolabe.
    "pact.term.compact.items": "Paid as you accept: %s",
    "pact.answer.cannot_afford.items": "The Open Hand Compact asks for the materials listed; you are short. Nothing was spent",
    # SG6: a pact whose work this version does not know.
    "pact.row.state.suspended.serves": "Suspended by you: %s does no work and nothing falls due until you resume it",
    "pact.row.service_unknown": "Its service (%s) is not known to this version: the pact is kept as it was, and may be suspended or ended",
})
JOURNAL.update({
    # GC-01 (SG7, GB17): the journal's pacts lines of every other research a perch's offers need (spirits/PactRegards).
    "pacts.research.needs": "%s: first understand %s",
    "pacts.research.next": "%s: %s",
    "pacts.research.complete": "%s is mastered",
    "pacts.research.invite": "%s is understood: invite %s at a pact perch",
})


def lang_entries(lang):
    lang[f"tag.item.{STOLAS_SPECIMEN_TAG.replace(':', '.')}"] = "Things Stolas Regards"
    for key, text in JOURNAL.items():
        lang[f"journal.{MOD}.{key}"] = text
    for key, name in ENTITY_NAMES.items():
        lang[f"entity.{MOD}.{key}"] = name
    lang[f"container.{MOD}.pact_perch"] = BLOCKS["pact_perch"]["name"]
    lang[f"tooltip.{MOD}.pact_perch"] = PERCH_TOOLTIP
    for spirit in SPIRITS.values():
        lang[f"tag.item.{spirit['pact']['upkeep']['offering'].replace(':', '.')}"] = spirit["offerings"]["name"]
    screen_lang(lang)
    presence_lang(lang)


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
            ("text", "Understood", "Stolas will hear an invitation now: build a "
             "[pact perch](entry://jugcraft:arcane_concordance/binding/stolas_pact) where you may build, and use it. He names "
             "his terms before anything is spent.", (research, "understood")),
            ("text", "Mastered", "You kept your pact's first obligation.", (research, "mastered")),
        ]}}
    pact = PACTS["stolas"]
    service, upkeep, compact = pact["service"], pact["upkeep"], pact["compact"]
    entries[("binding", "stolas_pact")] = {
        "name": "Stolas's Pact", "x": 2, "y": 4, "icon": rid("pact_perch"),
        "condition": (research, "understood"), "description": "The pact perch, Stolas's terms and the Open Hand Compact",
        "pages": [
            ("crafting_recipe", "Pact Perch", "Build it where you may build, at your own base (never in Styx's home), and use "
             "it to invite Stolas. It holds nothing: what you pay ahead is kept with the pact, and breaking the perch never "
             "ends it.", rid("pact_perch")),
            ("text", "His Terms",
             f"Stolas's screen gives his three words, then every term before anything is spent: his service, any hour, within "
             f"{service['radius']} blocks of the perch, {service['quota']} a game day; one offering every "
             f"{upkeep['period_days']} game days, up to {upkeep['cap']} paid ahead, a warning and {upkeep['grace_days']} game "
             "day of grace, then suspension with nothing lost; until you end it; the offerings back to you, once. **Decline**, "
             "or any refusal, spends nothing and names why."),
            ("text", "The Open Hand Compact",
             f"**Accept** pays the compact in the same moment the pact is sealed: {compact['resonance']} Astral Resonance from "
             "an astrolabe you carry (an Orrery Observatory fills it for its keeper's party, and only a Starwatcher, who "
             "understands Celestial Attunement, keeps one), and " + ", ".join(f"{c['count']} × {c['item'].split(':')[1].replace('_', ' ')}"
                                                         for c in compact["items"])
             + ". It counts as the first offering. Only this perch's owner may accept here, and one pact with Stolas a holder."),
        ]}
    entries[("binding", "stolas_pact")]["pages"] += pact_pages()
    return entries


def tags(tags):
    for ref in {**STYX_SPECIMENS, **PLAIN_SPECIMENS}:
        tags.add("item", STOLAS_SPECIMEN_TAG, ref)
    for spirit in SPIRITS.values():
        for ref in spirit["offerings"]["items"]:
            tags.add("item", spirit["pact"]["upkeep"]["offering"], ref)
    tags.add("block", "minecraft:mineable/axe", rid("pact_perch"))


def write_all(write, assets, data, lang, condition, self_drop):
    write(assets / "models" / "block" / "pact_perch.json", PERCH_MODEL)
    write(assets / "blockstates" / "pact_perch.json", {"variants": {"": {"model": rid("block/pact_perch")}}})
    write(assets / "items" / "pact_perch.json", {"model": {"type": "minecraft:model", "model": rid("item/pact_perch")}})
    write(assets / "models" / "item" / "pact_perch.json",
          {"parent": "minecraft:item/generated", "textures": {"layer0": rid("item/pact_perch")}})
    write(data / "loot_table" / "blocks" / "pact_perch.json", self_drop("pact_perch"))
    write(data / "recipe" / "pact_perch.json", dict({"fabric:load_conditions": condition("concordance")}, **RECIPE))


def write_data(write, data):
    for key, info in PACTS.items():
        write(data / "concordance" / "pact" / f"{key}.json", dict({"schema": 1}, **info))


# ------------------------------------------------------------------------------------------- D-04: the screen and body
# The terms page's label for each term (D-S15), in TERMS' order (check_pact_screen).
TERM_LABELS = {
    "service": "Service", "hours": "Hours", "area": "Area", "quota": "Daily quota", "upkeep": "Upkeep", "grace": "Grace",
    "suspension": "If suspended", "duration": "How long", "breach": "Breach", "ending": "How to end it",
    "returns": "What comes back", "perch": "If the perch is broken", "beneficiaries": "Who benefits",
    "compact": "The Open Hand Compact",
}
# The pact perch's screen (Java: client/PactScreen.java, ui("<key>")). English only (D-S21).
SCREEN = {
    "screen.jugcraft.pact.opening": "He settles on the perch...",
    "screen.jugcraft.pact.page": "%s of %s",
    "screen.jugcraft.pact.back": "Back",
    "screen.jugcraft.pact.next": "Next",
    "screen.jugcraft.pact.hear_terms": "Hear his terms",
    "screen.jugcraft.pact.leave": "Leave",
    "screen.jugcraft.pact.leave.hint": "Nothing is spent or agreed; he leaves the perch",
    "screen.jugcraft.pact.terms": "His terms",
    "screen.jugcraft.pact.term_row": "%s: %s",
    "screen.jugcraft.pact.terms_note": "Nothing is spent or agreed until you accept.",
    "screen.jugcraft.pact.in_the_way": "Before he can agree:",
    "screen.jugcraft.pact.waiting": "He considers your answer...",
    "screen.jugcraft.pact.accept": "Accept the pact",
    "screen.jugcraft.pact.accept.hint": "Pays the Open Hand Compact and seals the pact, both at once",
    "screen.jugcraft.pact.decline": "Decline",
    "screen.jugcraft.pact.decline.hint": "Nothing is spent; he leaves the perch",
    "screen.jugcraft.pact.kept": "Your pact",
    "screen.jugcraft.pact.standing": "How it stands:",
    "screen.jugcraft.pact.tab.service": "His service",
    "screen.jugcraft.pact.tab.terms": "Terms",
    "screen.jugcraft.pact.recall_heading": "Star-reckoning: recall a pattern you have observed",
    "screen.jugcraft.pact.no_patterns": "You have observed no sky pattern yet: record one at an observatory.",
    "screen.jugcraft.pact.pattern.cost": "%s resonance from your astrolabe",
    "screen.jugcraft.pact.pattern.chosen": "Chosen: %s resonance from your astrolabe",
    "screen.jugcraft.pact.pattern.claimed": "Already recalled this occurrence",
    "screen.jugcraft.pact.recall": "Recall %s",
    "screen.jugcraft.pact.recall.none": "Choose a pattern to recall",
    "screen.jugcraft.pact.offerings_heading": "Offerings for his upkeep",
    "screen.jugcraft.pact.no_offerings": "You carry none of the offerings he takes.",
    "screen.jugcraft.pact.offer": "Offer %s (%s carried)",
    "screen.jugcraft.pact.offer.hint": "Paid ahead into the pact's keeping, as many as it has room for",
    "screen.jugcraft.pact.suspend": "Suspend the pact",
    "screen.jugcraft.pact.suspend.hint": "His service stops; nothing is taken and nothing is lost",
    "screen.jugcraft.pact.resume": "Resume the pact",
    "screen.jugcraft.pact.resume.hint": "His service starts again",
    "screen.jugcraft.pact.end": "End the pact",
    "screen.jugcraft.pact.end.hint": "He leaves; what you paid ahead comes back to you once",
    "screen.jugcraft.pact.end.title": "End your pact with %s?",
    "screen.jugcraft.pact.end.confirm": "%s will leave, and what you paid ahead comes back to you once. This cannot be undone.",
    "screen.jugcraft.pact.end.yes": "End the pact",
    "screen.jugcraft.pact.end.no": "Keep it",
    "screen.jugcraft.pact.seat": "Seat the pact here",
    "screen.jugcraft.pact.seat.hint": "Your pact without its perch is kept at this perch from now on",
    "screen.jugcraft.pact.without_perch": "Without its perch",
    "screen.jugcraft.pact.closed": "At this perch:",
    "screen.jugcraft.pact.close": "Close",
    "screen.jugcraft.pact.help": "Tab and Shift+Tab move between his words and the choices; Enter or Space chooses; Esc closes. "
                                 "Closing before you accept agrees to nothing.",
}
# Stolas's body (D-S5, D-06): the owner library's Flashjack rig in his raven form (art/owner-library/catalog/files.csv:21291,
# 21330, 21676; SHA-256 d23b5372..., c4015c93..., 567b44a5...). Its geometry is copied unchanged by tools/owner_art.py
# (OWNER_FILES); his skin, the glowmask that lights his eyes and his animations are adapted from the rig's own files by
# tools/stolas_body.py (ADAPTED: runtime path -> (library file, adapter)), which tools/owner_art.py writes and checks.
# Its folder's leftover metadata labels it Ars Elemental by Alexthw, "GNU Lesser General Public License v3.0"
# (ports/jug-nouveau/ars_jymbaumental-1.21.1-0.7.10.4/META-INF/neoforge.mods.toml:7, 15, 25); the owner's declaration is
# art/owner-library/PROVENANCE.md:3-7. Not imported: flashjack.bbmodel, flapjack.png, bluejay.png; attack and idle.air
# stay in his animation file, unused. check_spirit_bodies holds the Java, the client test and the files to these tables.
# Derived from SPIRITS["stolas"]["body"] (GC-01): every spirit's owner files and adaptations; Stolas's own tables by
# their Chunk D names.
OWNER_FILES = {target: source for spirit in SPIRITS.values() for target, source in spirit["body"].get("owner_files", {}).items()}
ADAPTED = {target: entry for spirit in SPIRITS.values() for target, entry in spirit["body"].get("adapted", {}).items()}
# Files of a pact spirit's body built from code (the garden cohort, GC-05 on: Bathin's), by runtime path: (the Blocks-folder
# paintings it reads, its builder "garden_bodies.<function>"); tools/owner_art.py writes and checks them beside the copied
# and adapted files.
BUILT = {target: entry for spirit in SPIRITS.values() for target, entry in spirit["body"].get("built", {}).items()}
_STOLAS = SPIRITS["stolas"]["body"]
BODY_SCALE = _STOLAS["scale"]  # client/PactSpiritRenderers: Stolas's row
LOOPS = _STOLAS["loops"]
GESTURES = _STOLAS["gestures"]
GESTURE_TIMES = _STOLAS["gesture_times"]
LOOP_LENGTHS = _STOLAS["loop_lengths"]
ATTEND = _STOLAS["attend"]
EYE_TEXELS = _STOLAS["eye_texels"]
BODY_SHOTS = _STOLAS["shots"]
# What ConcordancePactClientGameTests photographs (D-05 appends its own).
CLIENT_SHOTS = ["jugcraft_pact_conversation", "jugcraft_pact_terms", "jugcraft_pact_terms_scale4", "jugcraft_pact_conversation_scale4",
                "jugcraft_pact_terms_small", "jugcraft_pact_conversation_small", "jugcraft_pact_refused", "jugcraft_pact_kept",
                "jugcraft_pact_stolas"]


def screen_lang(lang):
    """The pact screen's words (D-S21: English only)."""
    lang.update(SCREEN)
    for key, label in TERM_LABELS.items():
        lang[f"screen.{MOD}.pact.term.{key}"] = label


# ------------------------------------------------------------------------------------------- D-05: Star-reckoning
# Java: worker/Pacts.Reckoning and serves; spirits/StarReckoning; sky/AstrolabeItem.recall (the one recall). Ids are worded:
# append only. check_pacts holds the Java to these.
RECKONING_WORK = "star_reckoning"  # Java: StarReckoning.WORK (Stolas's service.work in PACTS; one of WORK)
RECKONING_CHOICES = 8  # Java: StarReckoning.MAX_CHOICES (= STATE_LIMITS["MAX_CHOICES"]; the catalog has six)
RECKONING = ["recalled", "disabled", "nothing", "conflicting", "not_holder", "suspended", "upkeep_held", "outside_area",
             "outside_hours", "quota_spent", "not_observed", "no_astrolabe", "too_little", "recalled_already"]
CLIENT_SHOTS += ["jugcraft_pact_service", "jugcraft_pact_end_confirm", "jugcraft_pact_ended"]

MESSAGES.update({
    "pact.reckoning.recalled": "Stolas reckons %s for you: its effect is yours for two minutes, whatever the sky",
    "pact.reckoning.nothing": "No pact with Stolas is kept at this perch",
    "pact.reckoning.not_holder": "Only the pact's holder may ask Stolas to reckon",
    "pact.reckoning.suspended": "The pact is suspended: resume it, and Stolas will reckon again",
    "pact.reckoning.upkeep_held": "The pact is suspended for its upkeep: an offering resumes it",
    "pact.reckoning.outside_area": "Stand at the perch, within %s blocks, to ask Stolas",
    "pact.reckoning.outside_hours": "Stolas reckons only between %s and %s",
    "pact.reckoning.quota_spent": "Stolas has reckoned for you this game day; ask again tomorrow",
    "pact.reckoning.not_observed": "Stolas recalls only a pattern you observed yourself at an observatory",
    "pact.reckoning.no_astrolabe": "Carry an astrolabe: its stored resonance pays for the recall",
    "pact.reckoning.too_little": "Your astrolabe needs %s resonance to recall %s",
    "pact.reckoning.recalled_already": "You have recalled %s since it last rose",
    "pact.row.service": "Star-reckoning: recall a pattern you observed, %s a game day (%s left today), paid from your astrolabe",
    "pact.row.obligation": "First obligation: record a sky observation at an observatory while the pact is active",
    "pact.row.obligation_kept": "First obligation kept: Stolas's Regard is mastered",
    "pact.obligation_kept": "Your pact's first obligation is kept: %s regards you",
})


def pact_pages():
    """D-05's pages of the binding/stolas_pact codex entry, from Stolas's offer (PACTS)."""
    import concordance_celestial as celestial
    service, upkeep = PACTS["stolas"]["service"], PACTS["stolas"]["upkeep"]
    return [
        ("text", "Star-reckoning",
         f"While your pact is active, stand at the perch (within {service['radius']} blocks) and ask Stolas to **reckon**: "
         "choose any sky pattern you observed yourself at an observatory, and your astrolabe pays that pattern's recall "
         f"price, {service['quota']} a game day. Its effect is yours for {celestial.RECALL_TICKS // 1200} minutes wherever "
         "you go, whatever the sky or the season. Each rising of a pattern pays one recall, whether Stolas or a master's "
         "own recall asks first."),
        ("text", "His Upkeep",
         "Pay **offerings** ahead at the perch: a Stolas Starflower, Ravenquill Lupine, Amethyst Mourningbell or an amethyst "
         f"shard. One is taken every {upkeep['period_days']} game days; up to {upkeep['cap']} wait paid ahead, and the "
         f"compact paid the first. With none waiting you are warned and have {upkeep['grace_days']} game day of grace; then "
         "the pact is suspended: nothing is taken or lost, and an offering resumes it. Time you are away, or the perch "
         "unloaded, is never owed, and a pact never ends by itself."),
        ("text", "Ending and Moving",
         "Its holder may **suspend** and **resume** it, or **end** it: what is paid ahead comes back to you once (at your "
         "feet if your pockets are full) and Stolas departs. Breaking the perch ends nothing: Stolas leaves the world and the "
         "pact waits, with all it holds, until you **seat** it at a new perch of your own. Your first obligation: record a "
         "sky observation at an observatory while the pact is active."),
    ]


# ------------------------------------------------------------------------------------------- D-06: Stolas's presence
# Java: spirits/StolasEntity.CUE_TICKS (the least time between two of his cues of one kind).
CUE_TICKS = 10
# Every spirit's cues, derived from SPIRITS (GC-01), Stolas's first. His, in StolasEntity.SOUND_IDS' order:
# jugcraft:stolas.<cue>, each with its subtitle (D-S5), bound to .ogg files already in runtime (no new audio, no voice;
# the raven's call is beast_crow's too). Being in SOUND_FILES, none is drawn by tools/concordance_sounds.py. Every file
# is mono, since OpenAL places only a mono source, so each is heard from him and fades with distance (check_pacts reads
# each file's channels; the unit review, D1): the leaves of a spelunkery table's paper for listening and inspecting, and
# a vesper's wing beats for his going (the cave book's pages and the ghost's weep that were first bound are stereo).
SOUND_EVENTS = {f"{name}.{cue}": subtitle for name, spirit in SPIRITS.items() for cue, (subtitle, _) in spirit["cues"].items()}
SOUND_FILES = {f"{name}.{cue}": files for name, spirit in SPIRITS.items() for cue, (_, files) in spirit["cues"].items()}
# What Jade shows (Java: spirits/PactReading; client/compat/JugcraftJadeClient). Sent as ids; worded on the client.
JADE_KEYS = ["standing", "visiting", "departing", "counterpart", "service", "state", "due_days", "grace_days", "waiting", "visitor",
             "note", "note_count"]
JADE_STANDINGS = ["none", "kept", "copy", "unreadable", "disabled"]
JADE_STATES = ["active", "suspended", "upkeep_due", "upkeep_held"]
# Every work's short name (concordance_workers.WORK and WORK here; check_pacts holds the tables together).
SERVICE_NAMES = {"gather": "Gathering", "courier": "Carrying its post's requests", "star_reckoning": "Star-reckoning",
                 "strain_tending": "Strain tending", "verdance_carrying": "Verdance carrying",
                 # GC-06 unit 3
                 # GC-07 unit 4
                 # GC-08 unit 5
                 # GC-09 unit 6
                 }
JADE_PROVIDERS = {"pact_perch": "Pact Perch", "spirit_anchor": "Spirit Anchor"}
TOOLTIPS.update({
    "jade.pact.none": "Keeps no agreement",
    "jade.pact.visiting": "%s is visiting: he waits for an answer",
    "jade.pact.departing": "Departing",
    "jade.pact.unreadable": "Its agreement cannot be read by this version; it is kept as it was",
    "jade.pact.keeps": "Keeps an agreement with %s",
    "jade.pact.service": "Service: %s",
    "jade.pact.state.active": "Active",
    "jade.pact.state.suspended": "Suspended by its holder",
    "jade.pact.state.upkeep_due": "An offering is due; it serves on through its grace",
    "jade.pact.state.upkeep_held": "Suspended for its upkeep: nothing is lost, and an offering resumes it",
    "jade.pact.next": "Next offering in %s game days",
    "jade.pact.grace": "Grace: %s game days left",
    # Its presence's own status word (compose.jugcraft.worker.status.*) follows, which may start "waiting: ...".
    "jade.pact.waiting": "Its presence: %s",
    "jade.pact.lost": "Its presence is lost: its holder, near its place, calls it back at no cost",
    **{f"jade.pact.service.{work}": name for work, name in SERVICE_NAMES.items()},
})
SCREEN.update({
    "screen.jugcraft.pact.result.done": "Done: %s",
    "screen.jugcraft.pact.result.refused": "Refused: %s",
})
# Concordance garden cohort, GC-01 (SG4, GB4): the chooser and the seat page's list (client/PactScreen.java).
SCREEN.update({
    "screen.jugcraft.pact.invite.heading": "Whom will you invite?",
    "screen.jugcraft.pact.invite.ready": "Ready to hear you",
    "screen.jugcraft.pact.invite.not_ready": "Not yet: see what is missing below",
    "screen.jugcraft.pact.invite.in_the_way": "Still missing:",
    "screen.jugcraft.pact.stranded.heading": "Your pacts without their perch:",
})
CLIENT_SHOTS += ["jugcraft_pact_perch_jade", "jugcraft_pact_stolas_jade"]


def presence_lang(lang):
    """D-06: Jade's provider names (its plugin settings list them)."""
    for key, name in JADE_PROVIDERS.items():
        lang[f"config.jade.plugin_{MOD}.{key}"] = name


# ------------------------------------------------------------------------------------------- D-09: the evidence bundle
# A timed sequence through his refusal (08:176; 03:159, the tell a player most needs to read without words):
# ConcordancePactClientGameTests.refusalSequence freezes the world at each time (seconds into the clip) and photographs it.
REFUSAL_SEQUENCE = (0.25, 0.65, 1.05, 1.45)
SEQUENCE_SHOTS = ["jugcraft_stolas_refuse_seq_1", "jugcraft_stolas_refuse_seq_2", "jugcraft_stolas_refuse_seq_3",
                  "jugcraft_stolas_refuse_seq_4"]
# What ConcordancePactNetworkTests' second client photographs (D-09, D-S22).
NETWORK_SHOTS = ["jugcraft_pact_peer_arrival", "jugcraft_pact_peer_refused", "jugcraft_pact_peer_departure"]
# ------------------------------------------------------------------------------------------- garden cohort, GC-05
# The accord machinery (Java: spirits/PactAccords, PactPerchBlockEntity's "accord", PactMenu's RESOLVE, PactKeeping's matter
# row, PactScreen's matter section, PactReading's "matter" and "accord"; GC-S11, GC-P42 to GC-P49). A work whose matter is a
# choice names in its SERVICE_WORDS row its "accords" (value -> its words; the safe course first), its "question" and its
# "matters" (id -> Jade's short line). check_pacts holds the Java to them and pins each shipped list (GC05_ACCORDS).
# The wire's appends, each after unit 1's (append only): RESOLVE (10) and its answer, three answers at most, and Jade's
# matter and accord after every earlier key. They are appended here rather than in the literals above so that each
# literal stays as the units before this one wrote it.
STATE_LIMITS["MAX_ANSWERS"] = 3  # Java: PactStatePayload.MAX_ANSWERS
ACTIONS += ["resolve"]  # Java: PactRequestPayload.RESOLVE
ANSWERS += ["resolved"]  # Java: PactMenu.RESOLVED
JADE_KEYS += ["matter", "accord"]  # Java: PactReading.KEYS
ACCORD_MAX = 16  # Java: PactAccords.MAX_ACCORD (the perch's saved string)
# The shared words (every pronoun "he": GC-P12). Each work's own (its answers, its question, its matters' Jade lines) are
# generated from its SERVICE_WORDS row, beside that table, by the first work that names "accords" (GC-05's Bathin).
MESSAGES.update({
    "pact.row.matter.awaiting": "%s raises a matter and awaits your answer below. Until you answer, he keeps to: %s",
    "pact.row.matter.answered": "Your answer to %s's matter stands: %s. You may change it below at any time",
    "pact.answer.resolved": "Your answer is kept at this perch: %s. Nothing was spent",
})
SCREEN.update({
    "screen.jugcraft.pact.matter.heading": "A matter to settle",
    "screen.jugcraft.pact.matter.kept": "Your answer: he keeps to this",
    "screen.jugcraft.pact.matter.safe": "His course until you answer",
    "screen.jugcraft.pact.matter.choose": "Answer this",
})
# Jade shows a reading to whoever looks, so its accord line is in the third person like every Jade pact line (the
# devices and accord review; PA section 3.1's "Your answer: %s" amended).
TOOLTIPS.update({"jade.pact.accord": "Its holder's answer: %s"})
# ------------------------------------------------------------------- garden cohort, GC-02: facing whoever speaks
# ConcordancePactClientGameTests.facing: the holder speaks from two blocks east, south, west and north of the perch in
# turn, north last (where every later picture of Stolas is taken from; D-P133 superseded), and Stolas's drawn body yaw is
# held to the speaker's bearing (Minecraft's yaw: 0 south, 90 west, 180 north, -90 east). The server tests hold the head
# to FACING_HEAD_TOLERANCE 40 ticks after a screen opens and the body to FACING_BODY_TOLERANCE 60 ticks after (GC-P8).
FACINGS = {"east": (1, 0), "south": (0, 1), "west": (-1, 0), "north": (0, -1)}
FACING_SHOTS = ["jugcraft_pact_stolas_faces_east", "jugcraft_pact_stolas_faces_south", "jugcraft_pact_stolas_faces_west",
                "jugcraft_pact_stolas_faces_north"]
FACING_HEAD_TOLERANCE = 10
FACING_BODY_TOLERANCE = 15
# ------------------------------------------------------------------------------------------- garden cohort, GC-04
# Each garden work's own words (Java: spirits/<SERVICE_CLASSES[work]>.java): its kept-page rows and its refusal
# (message.jugcraft.concordance.pact.<work>.<key>), its kept page's service section (screen.jugcraft.pact.service.<work>.*),
# its Jade notes (tooltip.jugcraft.concordance.jade.pact.note.<work>.<note>, their argument the note's count) and the reason
# ids it answers with (its class's constants; never one of PactMenu's ANSWERS). check_pacts holds the Java to them.
SERVICE_WORDS = {
    "strain_tending": {
        "messages": {
            "missing": "\"There is nothing here I could mend.\" Buer comes only where an awake Verdant Bed you keep lies "
                       "within %s blocks of the perch",
            "row.service": "Strain tending: %s of %s beds eased today, one at a time, each by its own tending rule",
            "row.asleep": "Buer is asleep until dawn, his petals shut (he mends from %s to %s); %s of %s beds eased today",
            "row.overworked": "Overworked: the bed at %s was worked faster than it heals. Buer will not mend it until it "
                              "is fed (bone meal, fertilizer or a Mulch Maw) or rests fallow",
            "row.overworked_more": "Overworked: %s beds, the nearest at %s, were worked faster than they heal. Buer will "
                                   "not mend them until each is fed (bone meal, fertilizer or a Mulch Maw) or rests fallow",
        },
        "screen": {
            "heading": "Strain tending",
            "about": "Buer works by himself, by daylight: the rows above say how many beds he has eased today and any "
                     "bed he will not mend yet. You need ask him nothing here.",
        },
        "notes": {
            "overworked": "Overworked beds: %s (he mends each again once it is fed or rested)",
            "asleep": "Asleep until dawn: he mends only by daylight",
        },
        "answers": ["nothing_to_mend"],
    },
    # GC-05 (Dossier 2 items 2, 3 and 5; GC-P48 to GC-P52, GC-P58): Bathin's Verdance carrying.
    "verdance_carrying": {
        "messages": {
            "missing": "\"Nothing here to carry, or nowhere to carry it.\" Bathin comes only where an awake Verdant Heart "
                       "you keep lies within %s blocks of the perch, with an awake Gleaner of yours or a Ley Pylon of yours or "
                       "no one's within %s blocks too",
            "row.service": "Verdance carrying: %s of %s moves today, each at most 8 Verdance into a far Gleaner or 3 "
                           "batches into a Ley Pylon (3 Verdance for 2 Ley Charge), with nothing ever on the way",
            "row.moved": "Last: %s Verdance from the Heart at %s set down in the Gleaner at %s, both in the same moment",
            "row.poured": "Last: %s Verdance from the Heart at %s set down in the Ley Pylon at %s as %s Ley Charge, both "
                          "in the same moment",
            "row.leaving": "Leaving %s of your Hearts alone: each pours into a pylon of its own beside it, as you answered",
            "matter.two_roads": "\"Your Heart already has a road. Shall I take from it too?\" (the Heart at %s pours into "
                                "the Ley Pylon beside it)",
        },
        "screen": {
            "heading": "Verdance carrying",
            "about": "Bathin works by himself, at any hour: the rows above say how many moves he has made today and his "
                     "last. A Heart that already pours into a pylon beside it waits for your answer.",
        },
        # Jade shows a reading to whoever looks, so his notes are worded in the third person like every Jade pact line (the
        # C3 review's rule for the accord line).
        "notes": {
            "two_roads": "Hearts with their own road: %s, waiting for his holder's answer at the perch",
            "empty": "Nothing to carry: the Hearts in his area hold no Verdance he may take",
            "nowhere": "Nowhere to carry it: every Gleaner and pylon he may fill is full, or too near its Heart",
        },
        "answers": ["nothing_to_carry"],
        # The accord machinery's words (GC-P49): his answers, the safe course first (Java VerdanceCarrying.ACCORDS, in this
        # order; append only; each at most ACCORD_MAX characters), his matter in general (shown with a kept answer when none
        # is raised now) and each matter his complication raises, in Jade's short words (third person: Jade's reader may be
        # anyone).
        "accords": {"yield": "Leave it", "claim": "Take from it"},
        "question": "\"Shall I take from a Heart that already has a road?\" (a Heart of yours that pours into a pylon of its own)",
        "matters": {"two_roads": "His matter: a Heart here already pours into a pylon of its own"},
    },
    # GC-06 unit 3
    # GC-07 unit 4
    # GC-08 unit 5
    # GC-09 unit 6
}
MESSAGES.update({f"pact.{work}.{key}": text for work, words in SERVICE_WORDS.items() for key, text in words["messages"].items()})
SCREEN.update({f"screen.{MOD}.pact.service.{work}.{key}": text for work, words in SERVICE_WORDS.items()
               for key, text in words["screen"].items()})
TOOLTIPS.update({f"jade.pact.note.{work}.{note}": text for work, words in SERVICE_WORDS.items()
                 for note, text in words["notes"].items()})
# GC-05 (the accord machinery, GC-P49): a work whose matter is a choice words its answers, its question and its matters' Jade
# lines here (none before Bathin's).
MESSAGES.update({f"pact.{work}.accord.{value}": text for work, words in SERVICE_WORDS.items()
                 for value, text in words.get("accords", {}).items()})
MESSAGES.update({f"pact.{work}.question": words["question"] for work, words in SERVICE_WORDS.items() if words.get("accords")})
TOOLTIPS.update({f"jade.pact.matter.{work}.{matter}": text for work, words in SERVICE_WORDS.items()
                 for matter, text in words.get("matters", {}).items()})
# Each cue's pitch where its SPIRITS row gives one (tools/concordance.py sounds(); a sounds.json entry's "pitch").
SOUND_PITCH = {f"{name}.{cue}": pitch for name, spirit in SPIRITS.items() for cue, pitch in spirit.get("pitch", {}).items()}
# The cue files the art plan names for each garden spirit (docs/features/concordance-garden-cohort.md, "Sounds"; every one
# mono, checked there): a garden cue plays only these, and never one of Stolas's.
ART_SOUNDS = {"buer": ["menagerie/fable/butterfly_heal", "menagerie/grove/entity/cactem/entity.cactem.heal",
                       "menagerie/grove/item/item.heal_spell_book.player_heal", "alchemy/peheal"]}
# What ConcordanceGardenPactClientGameTests photographs (GC-P30; GC-P18 for the chooser) besides each row's body shots.
GARDEN_CLIENT_SHOTS = ["jugcraft_garden_regard_tooltip", "jugcraft_garden_chooser", "jugcraft_garden_chooser_scale4",
                       "jugcraft_garden_chooser_small", "jugcraft_garden_buer_nothing_to_mend", "jugcraft_garden_buer_conversation",
                       "jugcraft_garden_buer_conversation_scale4", "jugcraft_garden_buer_conversation_small",
                       "jugcraft_garden_buer_terms", "jugcraft_garden_buer_terms_scale4", "jugcraft_garden_buer_terms_small",
                       "jugcraft_garden_buer_refused", "jugcraft_garden_buer_kept", "jugcraft_garden_buer_at_work",
                       "jugcraft_garden_buer_overworked", "jugcraft_garden_buer_jade", "jugcraft_garden_buer_below",
                       "jugcraft_garden_buer_faces_east", "jugcraft_garden_buer_faces_south", "jugcraft_garden_buer_faces_west",
                       "jugcraft_garden_buer_faces_north", "jugcraft_garden_buer_end_confirm", "jugcraft_garden_buer_ended"]
# ------------------------------------------------------------------------------------------- garden cohort, GC-05: Bathin
ART_SOUNDS["bathin"] = ["menagerie/cavern/item/magic_conch/magic_conch_summon", "menagerie/cavern/item/magic_conch/magic_conch_cast",
                        "menagerie/cavern/item/sea_staff/sea_staff_bubble_0", "menagerie/cavern/item/sea_staff/sea_staff_bubble_1",
                        "menagerie/cavern/item/sea_staff/sea_staff_bubble_2"]
# What ConcordanceGardenPactClientGameTests photographs of Bathin besides his body shots (GC-P59).
GARDEN_CLIENT_SHOTS += ["jugcraft_garden_bathin_nothing_to_carry", "jugcraft_garden_bathin_chooser",
                        "jugcraft_garden_bathin_conversation", "jugcraft_garden_bathin_conversation_scale4",
                        "jugcraft_garden_bathin_conversation_small", "jugcraft_garden_bathin_terms",
                        "jugcraft_garden_bathin_terms_scale4", "jugcraft_garden_bathin_terms_small", "jugcraft_garden_bathin_refused",
                        "jugcraft_garden_bathin_kept", "jugcraft_garden_bathin_shimmer", "jugcraft_garden_bathin_jade",
                        "jugcraft_garden_bathin_faces_east", "jugcraft_garden_bathin_faces_south",
                        "jugcraft_garden_bathin_faces_west", "jugcraft_garden_bathin_faces_north",
                        "jugcraft_garden_bathin_end_confirm", "jugcraft_garden_bathin_ended"]
# What it photographs of the accord (GC-05; the accord machinery's client steps), appended to Buer's and Bathin's.
GARDEN_CLIENT_SHOTS += ["jugcraft_garden_roster_awaiting", "jugcraft_garden_bathin_matter_jade", "jugcraft_garden_bathin_matter",
                        "jugcraft_garden_bathin_matter_scale4", "jugcraft_garden_bathin_matter_small", "jugcraft_garden_bathin_answered"]
# ------------------------------------------------------------------- garden cohort, GC-06b: shared by GC-06 to GC-09
# GC-P74: each choice of a work's on-request action on its kept page, a Recall button with what is left today
# (client/PactScreen.workService; Marax's reading its first user).
SCREEN.update({f"screen.{MOD}.pact.work.left": "%s left today", f"screen.{MOD}.pact.work.spent": "None left today"})
# GC-06 unit 3
# GC-07 unit 4
# GC-08 unit 5
# GC-09 unit 6
