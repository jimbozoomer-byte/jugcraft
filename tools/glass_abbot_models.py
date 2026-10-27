"""The GeckoLib body of the Glass Abbot (docs/features/glass-abbot.md), who is also his Glass Images' likeness. Geometry and
animations only, as plain dicts; no I/O. tools/glass_abbot_art.py paints the sheet on the same box-UV regions, at
TEXTURE_SCALE times the size the model declares (docs/ART_DIRECTION.md, "High resolution"). The builder and the posing
helpers are Vesperine's (tools/vesperine_models.py), which also sets out the conventions: pixels (16 = one block), y = 0 at
the feet, every model facing north (-z) with its left on +x; +x pitches a bone's top forward, -x raises a hanging arm
forward, a right arm turns outwards with +z and a left one with -z, and +y turns the front to the model's right. A
keyframed rotation adds to a bone's rest rotation.

Every cube is named by its part; cubes of one part share one box-UV region (the left sleeve's mirrored cubes reuse the
right's), and `Model.pack` lays the regions out on the sheet without overlaps. MODELS, ANIMATIONS and CLIPS are keyed by
model; CLIPS lists the clips the Java plays. His bones all belong to one controller ("body").
"""
from vesperine_models import Model, TEXTURE_SCALE, animations, clip, posed, still, wave

__all__ = ["TEXTURE_SCALE", "MODELS", "ANIMATIONS", "CLIPS", "CONTROLLERS", "controller_of"]

# A tall, faceless figure in a monk's habit of grey wool, two and a half blocks high: a long robe to the floor, a rope at
# the waist, wide sleeves; over his shoulders a mantle of leaded glass panes, clear and amber and rose, front and back,
# with a pane over each shoulder; a deep hood, and where his face should be a round window of glass that glows like the
# sun through a rose window. His hands are glass too.


def glass_abbot_model():
    m = Model("glass_abbot", 128, (1.2, 2.7))
    m.bone("root")
    m.bone("body", "root", (0, 20, 0))
    m.cube("body", "robe", (-6, 0, -4), (12, 20, 8))
    m.cube("body", "hem", (-7, 0, -5), (14, 3, 10))
    m.cube("body", "chest", (-5, 20, -3), (10, 11, 6))
    m.cube("body", "rope", (-6.5, 19, -4.5), (13, 1, 9))
    # The glass mantle: panes front and back, and one over each shoulder.
    m.bone("mantle", "body", (0, 30, 0))
    m.cube("mantle", "mantle_front", (-6, 21, -4.5), (12, 10, 1))
    m.cube("mantle", "mantle_back", (-6, 19, 3.5), (12, 12, 1))
    m.cube("mantle", "mantle_shoulder", (-8, 29, -4), (4, 2, 8))
    m.cube("mantle", "mantle_shoulder", (4, 29, -4), (4, 2, 8), mirror=True)
    # The hood, and the rose window where his face should be.
    m.bone("head", "body", (0, 31, 0))
    m.cube("head", "hood", (-4.5, 31, -4.5), (9, 10, 9))
    m.cube("head", "hood_peak", (-2, 40, -2), (4, 2, 6))
    m.cube("head", "face", (-3, 33, -5), (6, 6, 1))
    # His arms: wide sleeves, and hands of glass.
    for side, name in ((-1, "right"), (1, "left")):
        mirror = side > 0

        def ox(v, w):  # a right-side origin x for a cube `w` wide, mirrored to the left
            return v if side < 0 else -v - w

        m.bone(f"arm_{name}", "body", (side * 6, 30, 0))
        m.cube(f"arm_{name}", "sleeve", (ox(-9, 3), 17, -2), (3, 13, 4), mirror=mirror)
        m.cube(f"arm_{name}", "cuff", (ox(-9.5, 4), 15, -2.5), (4, 3, 5), mirror=mirror)
        m.cube(f"arm_{name}", "hand", (ox(-8.5, 2), 12, -1), (2, 3, 2), mirror=mirror)
    return m


# Standing: upright, hands loose at his sides, hood a little bowed.
STAND = {"head": (6, 0, 0), "arm_right": (0, 0, 4), "arm_left": (0, 0, -4)}
# Kneeling at his lectern (waiting, gathering, dazed): sunk to the floor, robe folded under him, back bent.
KNEEL = {"root": {"position": (0, -9, 0)}, "body": (14, 0, 0), "head": (14, 0, 0), "arm_right": (-30, 0, 10), "arm_left": (-30, 0, -10)}
# Praying, as he waits: hands raised together before his hood.
PRAY = {**KNEEL, "arm_right": (-70, -20, 0), "arm_left": (-70, 20, 0)}
# Gathering the sun: kneeling, arms raised wide, head thrown back to the rose window.
GATHER = {"root": {"position": (0, -9, 0)}, "body": (-10, 0, 0), "head": (-30, 0, 0), "arm_right": (-160, 0, 30),
          "arm_left": (-160, 0, -30)}
# Dazed, his mantle cracked: kneeling, slumped, arms hanging.
DAZED = {"root": {"position": (0, -9, 0)}, "body": (30, 0, 6), "head": (30, 0, -10), "arm_right": (10, 0, 6), "arm_left": (14, 0, -4)}
# The Shard Fan: his right hand raised high (shards glint over it), then flung forward.
RAISE = {"body": (-6, 0, 0), "head": (-6, 0, 0), "arm_right": (-170, 0, 10), "arm_left": (0, 0, -8)}
FLING = {"body": (10, 0, 0), "head": (4, 0, 0), "arm_right": (-70, 0, 0), "arm_left": (0, 0, -8)}
# The Prism Line: both hands held out before him, palms together, pointing down the line.
AIM = {"body": (4, 0, 0), "head": (10, 0, 0), "arm_right": (-80, 10, 0), "arm_left": (-80, -10, 0)}
LANCE = {"body": (-4, 0, 0), "head": (0, 0, 0), "arm_right": (-90, 14, 0), "arm_left": (-90, -14, 0)}
# False Images: arms crossed over the mantle, then flung wide as the images step apart.
CROSS = {"head": (10, 0, 0), "arm_right": (-60, -40, 0), "arm_left": (-60, 40, 0)}
SPREAD = {"body": (-8, 0, 0), "head": (-10, 0, 0), "arm_right": (0, 0, 70), "arm_left": (0, 0, -70)}
# The Veil: hands folded at his breast, head bowed low.
FOLD = {"head": (24, 0, 0), "arm_right": (-50, -30, 0), "arm_left": (-50, 30, 0)}
# Turning a mirror: one hand pointing out to his right, the other at his breast.
POINT = {"body": (0, -20, 0), "head": (0, -30, 0), "arm_right": (-90, 0, 40), "arm_left": (-40, 30, 0)}
# Dead: fallen forward on his knees, then down.
DEAD = {"root": {"position": (0, -12, 0), "rotation": (80, 0, 0)}, "head": (20, 0, 0), "arm_right": (-150, 0, 20),
        "arm_left": (-150, 0, -20)}


def _glide(length):
    """Walking: the robe sways, the body rocks a little, the sleeves swing."""
    return {
        "body": {"rotation": wave(length, 1, (0, 0, 0), (0, 0, 3))},
        "arm_right": {"rotation": wave(length, 1, STAND["arm_right"], (14, 0, 0))},
        "arm_left": {"rotation": wave(length, 1, STAND["arm_left"], (14, 0, 0), phase=0.5)},
        "head": {"rotation": wave(length, 2, STAND["head"], (3, 0, 0))},
        "root": {"position": wave(length, 2, (0, 0, 0), (0, 0.5, 0))},
    }


def glass_abbot_animations():
    """His clips, all on the one "body" controller."""
    return animations({
        "animation.glass_abbot.pray": posed(4.0, [(0.0, PRAY), (4.0, PRAY)], loop=True, waves={
            "head": {"rotation": wave(4.0, 1, PRAY["head"], (4, 0, 0))}}),
        "animation.glass_abbot.wake": posed(2.5, [(0.0, PRAY), (1.0, GATHER), (1.8, GATHER), (2.5, STAND)]),
        "animation.glass_abbot.idle": posed(3.0, [(0.0, STAND), (3.0, STAND)], loop=True, waves={
            "head": {"rotation": wave(3.0, 1, STAND["head"], (3, 10, 0), phase=0.2)},
            "mantle": {"rotation": wave(3.0, 1, (0, 0, 0), (1, 0, 0))}}),
        "animation.glass_abbot.walk": clip(1.6, _glide(1.6)),
        "animation.glass_abbot.shard_windup": posed(0.8, [(0.0, STAND), (0.5, RAISE), (0.8, RAISE)], waves={
            "arm_right": {"rotation": wave(0.8, 3, RAISE["arm_right"], (0, 0, 4))}}),
        "animation.glass_abbot.shard": posed(0.6, [(0.0, RAISE), (0.12, FLING), (0.6, STAND)]),
        "animation.glass_abbot.prism_windup": posed(1.0, [(0.0, STAND), (0.5, AIM), (1.0, AIM)]),
        "animation.glass_abbot.prism": posed(0.8, [(0.0, AIM), (0.1, LANCE), (0.6, LANCE), (0.8, STAND)]),
        "animation.glass_abbot.images_windup": posed(1.5, [(0.0, STAND), (0.6, CROSS), (1.5, CROSS)]),
        "animation.glass_abbot.images": posed(0.5, [(0.0, CROSS), (0.15, SPREAD), (0.5, STAND)]),
        "animation.glass_abbot.gather": posed(3.0, [(0.0, KNEEL), (0.6, GATHER), (3.0, GATHER)], waves={
            "head": {"rotation": wave(3.0, 3, GATHER["head"], (3, 0, 0))}}),
        "animation.glass_abbot.dazed": posed(2.0, [(0.0, DAZED), (2.0, DAZED)], loop=True, waves={
            "head": {"rotation": wave(2.0, 2, DAZED["head"], (6, 8, 0))}}),
        "animation.glass_abbot.veil": posed(2.0, [(0.0, STAND), (0.6, FOLD), (2.0, FOLD)]),
        "animation.glass_abbot.turn": posed(1.5, [(0.0, STAND), (0.5, POINT), (1.5, POINT)]),
        "animation.glass_abbot.death": posed(1.5, [(0.0, DAZED), (1.0, DEAD), (1.5, DEAD)]),
        "animation.glass_abbot.shatter": posed(0.4, [(0.0, STAND), (0.4, {"root": {"position": (0, -4, 0)}, **SPREAD})]),
    })


MODELS = {"glass_abbot": glass_abbot_model}
ANIMATIONS = {"glass_abbot": glass_abbot_animations}
# The clips the Java names (GlassAbbotEntity, GlassImageEntity).
CLIPS = {
    "glass_abbot": ("pray", "wake", "idle", "walk", "shard_windup", "shard", "prism_windup", "prism", "images_windup",
                    "images", "gather", "dazed", "veil", "turn", "death", "shatter"),
}
# The bones each of his controllers owns besides "body": none.
CONTROLLERS = {}


def controller_of(clip_name):
    """Which of his controllers plays a clip of his: his one."""
    return "body"


def _check():
    """The model packs, and every clip the Java names is there."""
    MODELS["glass_abbot"]().pack()
    names = set(glass_abbot_animations()["animations"])
    missing = [c for c in CLIPS["glass_abbot"] if f"animation.glass_abbot.{c}" not in names]
    if missing:
        raise ValueError(f"missing clips {missing}")
    return True


if __name__ == "__main__":
    print(_check())
