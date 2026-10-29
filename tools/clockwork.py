"""Clockwork Animatronics, decor set 9 (docs/features/clockwork-animatronics.md): Halloween figures moved by clockwork,
driven from the kinetic power network (docs/features/kinetic-power.md): a hand crank, a steam engine, or an electric
motor on the JE network, through iron shafts and brass gearboxes.

Each figure takes up to `need` KE a tick from a shaft, gearbox or source touching its lower block. It works only on its
full `need`: on a tick it gets all of it, it runs, and it keeps running `spin_down` ticks after its last full tick, as
a shaft keeps turning (KineticNetworks.SPIN_DOWN). The energy goes into its clockwork, so nothing comes back out. It
saves nothing; whether it runs is its block's `running`, set only when that changes.

Part 1, the Organ-Grinder Skeleton (agriculture/OrganGrinderSkeletonBlock, OrganGrinderSkeletonBlockEntity): a
skeleton 1.75 m tall in a battered top hat and a moth-eaten red waistcoat, standing behind a street barrel organ (a
hurdy-gurdy, as an organ-grinder's is called) on a two-wheeled cart, two blocks tall. While it runs:
- its right hand turns the organ's crank, once round every `crank_ticks` ticks, about its axle (`crank`: across,
  height, depth) at `handle` from it; the arm bends at the elbow to follow it (client/OrganGrinderSkeletonRenderer,
  `shoulder` and the arm's two lengths, `arm`);
- the organ plays its waltz (`tune`), a step every `step_ticks` ticks: the melody on the flute and the bass on the
  first step of each bar, as note blocks sound;
- its head nods once a bar about its neck (`neck`, by `nod` degrees), and its jaw drops on each melody note (about
  `jaw_hinge`, height and depth, by `jaw` degrees).
Stopped, the crank runs down over `ease` ticks and stops where it is, the head and the jaw settle, and the organ falls
silent.
Part 2, the Animatronic Werewolf (agriculture/AnimatronicWerewolfBlock, AnimatronicWerewolfBlockEntity): a werewolf
1.75 m tall hunched (taller upright), in torn trousers, standing on a brass turntable 69 cm across on a black iron
plinth, two blocks tall. While it runs:
- the turntable turns it slowly round, once every `turn_ticks` ticks (client/AnimatronicWerewolfRenderer);
- every `howl_every` ticks it howls: over `raise_ticks` ticks its arms swing up from `arms_rest` to `arms_raised`
  degrees about its shoulders (`shoulder`), its head goes back `head_back` degrees about its neck (`neck`) and its jaw
  drops `jaw` degrees about its hinge (`jaw_hinge`); it holds them `hold_ticks` ticks while it howls (`howl_sound`), and
  lowers them over `raise_ticks` again.
Stopped, the turntable runs down over `ease` ticks and stops where it is, and the werewolf lowers its arms.
Part 3, the Flying Crank Ghost (agriculture/FlyingCrankGhostBlock, FlyingCrankGhostBlockEntity): a hooded cheesecloth
ghost 1.3 m long hanging in the air from a crank on a black iron post 2 m tall, its drive carried up the post from a
brass gear housing at its foot to a brass gearbox on a crossbar at its top. Three wires hang it: one from the crank's
pin to the top of its hood, and one from the pin over a pulley at each end of the crossbar (`pulleys`, at `pulley_y`)
to each of its wrists. While it runs:
- the crank turns once round every `crank_ticks` ticks about its axle (`crank`), its pin `pin` from it; the ghost hangs
  from the pin, so it rides a circle that size, swaying `sway` degrees about the top of its hood (`head_top`) as it
  goes (client/FlyingCrankGhostRenderer);
- as the pin swings away from a pulley it draws that wire in, so each arm (pivoting at a shoulder, `shoulders` across
  at `shoulder_y`, its wrist `wrist` from it) rises from `arms_rest` degrees out from hanging straight down to
  `arms_rest` + `flap`, the two in turn;
- its eyes glow pale green (a lamp in its hood), and every `moan_every` ticks it moans (`moan_sound`).
Stopped, the crank runs down over `ease` ticks and stops where it is, and its eyes go dark. The wires all lie in one
plane across the figure (`wire_z` deep).
Sizes in pixels of the lower block, facing north (its front, the organ or the werewolf's face, to the north).
"""

# The waltz, original to the organ: A minor, three-four time, six steps (quavers) a bar, eight bars. Each step's
# melody is a note block pitch (0 is F#3, 12 F#4, 24 F#5) or None (held); each bar's bass is played on its first step.
_MELODY = [
    15, None, 18, None, 22, None,   # Am: A C E
    20, 18, 17, None, 15, None,     # Am: D C B, A
    11, None, 15, None, 20, None,   # Dm: F A D
    18, 17, 14, None, 10, None,     # E: C B G#, E
    15, None, 18, None, 22, None,   # Am: A C E
    23, 22, 20, None, 18, None,     # F: F E D, C
    17, None, 14, None, 10, None,   # E: B G# E
    15, None, None, None, None, None,  # Am: A
]
_BASS = [15, 15, 8, 10, 15, 11, 10, 15]

ORGAN_GRINDER = {
    "block": "organ_grinder_skeleton", "display": "Organ-Grinder Skeleton",
    "need": 4, "spin_down": 10,
    "crank_ticks": 20, "crank": (10.5, 14.0, 7.0), "handle": 1.5,
    "shoulder": (11.0, 23.5, 10.5), "arm": (5.25, 7.0),
    "neck": (8.0, 25.0, 11.0), "nod": 8.0, "jaw_hinge": (25.0, 11.5), "jaw": 20.0, "ease": 10.0,
    "step_ticks": 5, "steps_per_bar": 6, "melody": _MELODY, "bass": _BASS,
    "melody_sound": "minecraft:block.note_block.flute", "bass_sound": "minecraft:block.note_block.bass", "volume": 0.6,
}

WEREWOLF = {
    "block": "animatronic_werewolf", "display": "Animatronic Werewolf",
    "need": 8, "spin_down": 10, "ease": 20.0,
    "turn_ticks": 320, "howl_every": 160, "raise_ticks": 12, "hold_ticks": 24,
    "shoulder": (22.13, 5.09), "arms_rest": 22.5, "arms_raised": 130.0,
    "neck": (24.0, 6.5), "head_back": 35.0, "jaw_hinge": (23.75, 4.0), "jaw": 30.0,
    "howl_sound": "minecraft:entity.wolf.howl", "volume": 1.2, "pitch": 0.7,
}

FLYING_GHOST = {
    "block": "flying_crank_ghost", "display": "Flying Crank Ghost",
    "need": 4, "spin_down": 10, "ease": 10.0,
    "crank_ticks": 40, "crank": (8.0, 30.0), "pin": 1.0, "wire_z": 6.0,
    "pulleys": (1.5, 14.5), "pulley_y": 29.0, "head_top": (8.0, 26.5),
    "shoulders": (5.25, 10.75), "shoulder_y": 21.0, "wrist": 7.0,
    "arms_rest": 22.5, "flap": 45.0, "sway": 6.0,
    "moan_every": 200, "moan_sound": "minecraft:entity.ghast.ambient", "volume": 0.6, "pitch": 0.9,
}

RECIPES = [
    # The skeleton (two bone blocks), its top hat (black wool), its organ (a note block on planks) and its clockwork
    # (a clock).
    {"id": ORGAN_GRINDER["block"], "pattern": [" W ", "BNB", "PCP"],
     "key": {"W": "minecraft:black_wool", "B": "minecraft:bone_block", "N": "minecraft:note_block", "P": "#minecraft:planks",
             "C": "minecraft:clock"},
     "result": ORGAN_GRINDER["block"], "count": 1, "category": "building"},
    # The werewolf (a wolf's worth of brown wool over bones), its turntable (a gold block's brass on iron) and its
    # clockwork (a clock).
    {"id": WEREWOLF["block"], "pattern": ["BWB", "WCW", "IGI"],
     "key": {"B": "minecraft:bone", "W": "minecraft:brown_wool", "C": "minecraft:clock", "I": "minecraft:iron_ingot",
             "G": "minecraft:gold_ingot"},
     "result": WEREWOLF["block"], "count": 1, "category": "building"},
    # The ghost (white wool, its cheesecloth), its wires (string), its post and crossbar (iron) and its clockwork (a
    # clock).
    {"id": FLYING_GHOST["block"], "pattern": ["SCS", "WIW", " I "],
     "key": {"S": "minecraft:string", "C": "minecraft:clock", "W": "minecraft:white_wool", "I": "minecraft:iron_ingot"},
     "result": FLYING_GHOST["block"], "count": 1, "category": "building"},
]


def tune_length():
    return len(ORGAN_GRINDER["melody"])


def figures():
    """Every figure's table, in the order they were added."""
    return [ORGAN_GRINDER, WEREWOLF, FLYING_GHOST]


def blocks():
    return [spec["block"] for spec in figures()]


def items():
    return blocks()
