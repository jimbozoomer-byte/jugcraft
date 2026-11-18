"""GeckoLib bodies for the Storm Roc and his fight (docs/features/storm-roc.md): the Roc himself and his Gale Fledglings.
Geometry and animations only, as plain dicts; no I/O. tools/storm_roc_art.py paints their sheets on the same box-UV
regions, at TEXTURE_SCALE times the size the models declare (docs/ART_DIRECTION.md, "High resolution"). The builder and
the posing helpers are Vesperine's (tools/vesperine_models.py), which also sets out the conventions: pixels (16 = one
block), y = 0 at the feet, every model facing north (-z) with its left on +x; +x pitches a bone's top forward, -x raises
a hanging arm forward, a right arm turns outwards with +z and a left one with -z, and +y turns the front to the model's
right. A keyframed rotation adds to a bone's rest rotation.

Every cube is named by its part; cubes of one part share one box-UV region (a left wing's mirrored cubes reuse the
right's), and `Model.pack` lays the regions out on the sheet without overlaps. MODELS, ANIMATIONS and CLIPS are keyed by
model; CLIPS lists the clips the Java plays.
"""
from vesperine_models import Model, TEXTURE_SCALE, animations, clip, keys, posed, still, wave

__all__ = ["TEXTURE_SCALE", "MODELS", "ANIMATIONS", "CLIPS", "CONTROLLERS", "controller_of"]

# ------------------------------------------------------------------------------------------------- the Storm Roc

# An eagle as big as a cart, slate-grey with a storm-blue sheen: a deep chest and a broad back; legs in feathered
# trousers, bare yellow shanks and black talons; a neck and head held high, a hooked beak of dark horn, eyes that burn
# electric blue, and a crest of feathers swept back from the brow; a fan of a tail. His wings are two pairs of bones,
# never both shown: folded along his sides as he stands, and spread (inner wing and primaries, their tips white like
# lightning) as he rears, flies, dives or lies grounded, some four and three-quarter blocks across.
#
# His bones fall to two controllers, which never share one: "body" (his poses, the spread wings' beats among them) and
# "wings" (which pair shows: wings_folded or wings_spread, by scale).
SPAN = 38  # how far each spread wing reaches from the middle (pixels)


def storm_roc_model():
    m = Model("storm_roc", 128, (5.0, 3.5))
    m.bone("root")
    # His body, chest and tail.
    m.bone("body", "root", (0, 18, 2))
    m.cube("body", "torso", (-7, 12, -8), (14, 12, 20))
    m.cube("body", "breast", (-6, 13, -11), (12, 10, 4))
    m.bone("tail", "body", (0, 18, 12))
    m.cube("tail", "tail_fan", (-6, 17, 12), (12, 1, 10))
    # His neck and head: a hooked beak, his eyes, a swept-back crest.
    m.bone("neck", "body", (0, 22, -8))
    m.cube("neck", "neck", (-4, 21, -14), (8, 10, 7))
    m.bone("head", "neck", (0, 30, -12))
    m.cube("head", "head", (-4.5, 28, -20), (9, 8, 10))
    m.cube("head", "beak", (-2, 29, -25), (4, 4, 5))
    m.cube("head", "beak_hook", (-1.5, 27, -26), (3, 3, 2))
    m.cube("head", "eye", (-5, 32, -18), (1, 1, 2))
    m.cube("head", "eye", (4, 32, -18), (1, 1, 2), mirror=True)
    m.cube("head", "crest", (-1, 35, -16), (2, 4, 6), rotation=(-25, 0, 0), pivot=(0, 35, -13))
    # His legs: feathered trousers, bare shanks, and talons gripping forward.
    for side, name in ((-1, "right"), (1, "left")):
        mirror = side > 0

        def ox(v, w):  # a right-side origin x for a cube `w` wide, mirrored to the left
            return v if side < 0 else -v - w

        m.bone(f"leg_{name}", "root", (side * 4, 13, 2))
        m.cube(f"leg_{name}", "thigh", (ox(-6, 4), 7, -0.5), (4, 7, 5), mirror=mirror)
        m.bone(f"shank_{name}", f"leg_{name}", (side * 4, 7, 2))
        m.cube(f"shank_{name}", "shank", (ox(-5, 2), 2, 1), (2, 5, 2), mirror=mirror)
        m.cube(f"shank_{name}", "talons", (ox(-6.5, 5), 0, -2.5), (5, 2, 6), mirror=mirror)
    # His wings folded: a long plate along each side, sloping from the shoulder to past the tail's root.
    m.bone("wings_folded", "body", (0, 22, -4))
    for side, name in ((-1, "right"), (1, "left")):
        m.bone(f"fold_{name}", "wings_folded", (side * 7, 23, -4), rotation=(-8, 0, 0))
        m.cube(f"fold_{name}", "folded_wing", ((-9 if side < 0 else 7), 12, -6), (2, 12, 19), mirror=side > 0)
        m.cube(f"fold_{name}", "folded_tips", ((-9 if side < 0 else 7), 12, 13), (2, 12, 2), mirror=side > 0)
    # His wings spread: the inner wing from the shoulder, the primaries from its end, their tips white like lightning.
    m.bone("wings_spread", "body", (0, 23, -2))
    for side, name in ((-1, "right"), (1, "left")):
        mirror = side > 0
        m.bone(f"wing_{name}", "wings_spread", (side * 6, 23, -2))
        m.cube(f"wing_{name}", "inner_wing", ((-20 if side < 0 else 6), 22, -6), (14, 2, 14), mirror=mirror)
        m.bone(f"tip_{name}", f"wing_{name}", (side * 20, 23, -2))
        m.cube(f"tip_{name}", "primaries", ((-SPAN + 2 if side < 0 else 20), 22.5, -5), (16, 1, 13), mirror=mirror)
        m.cube(f"tip_{name}", "primary_tips", ((-SPAN if side < 0 else SPAN - 2), 22.5, -5), (2, 1, 13), mirror=mirror)
    return m


# The wings controller's bones; the body's clips never key them (they key the spread wings' own bones instead).
WING_BONES = ("wings_folded", "wings_spread")

# Standing: chest out, head up and level, tail a little low.
STAND = {"body": (-12, 0, 0), "neck": (-6, 0, 0), "head": (14, 0, 0), "tail": (18, 0, 0)}
# In the nest: hunched, head drawn in and turned a little, tail low.
PERCH = {"root": {"position": (0, -3, 0)}, "body": (6, 0, 0), "neck": (16, 0, 0), "head": (-10, 18, 0), "tail": (14, 0, 0),
         "leg_right": (-30, 0, 0), "leg_left": (-30, 0, 0), "shank_right": (40, 0, 0), "shank_left": (40, 0, 0)}
# Wings spread wide and level, as he soars or rears.
SPREAD = {"wing_right": (0, 0, 0), "wing_left": (0, 0, 0), "tip_right": (0, 0, 0), "tip_left": (0, 0, 0)}
# Wings raised high over his back, at the top of a beat.
UP = {"wing_right": (0, 0, -40), "wing_left": (0, 0, 40), "tip_right": (0, 0, -20), "tip_left": (0, 0, 20)}
# Wings swept down, at the bottom of a beat.
DOWN = {"wing_right": (0, 0, 35), "wing_left": (0, 0, -35), "tip_right": (0, 0, 15), "tip_left": (0, 0, -15)}
# The Wing Buffet: rearing back, wings flung up and back, head high; then the wings swept forward and down.
REAR = {"body": (-30, 0, 0), "neck": (-10, 0, 0), "head": (14, 0, 0), "tail": (30, 0, 0),
        "wing_right": (0, -30, -30), "wing_left": (0, 30, 30), "tip_right": (0, 0, -15), "tip_left": (0, 0, 15)}
SWEEP = {"body": (12, 0, 0), "neck": (8, 0, 0), "head": (-6, 0, 0), "tail": (-6, 0, 0),
         "wing_right": (0, 45, 30), "wing_left": (0, -45, -30), "tip_right": (0, 20, 10), "tip_left": (0, -20, -10)}
# Screeching: head thrown back, beak to the sky, wings half open.
SCREECH = {"body": (-14, 0, 0), "neck": (-30, 0, 0), "head": (-30, 0, 0), "tail": (16, 0, 0),
           "wing_right": (0, 20, -14), "wing_left": (0, -20, 14), "tip_right": (0, 30, 0), "tip_left": (0, -30, 0)}
# Calling to the nest: on foot, head up and turned north, wings folded.
CALL = {"body": (-12, 0, 0), "neck": (-24, 0, 0), "head": (-12, 0, 0), "tail": (24, 0, 0)}
# Diving: wings swept back like an arrowhead, head and body stretched forward and down.
DIVE = {"body": (40, 0, 0), "neck": (14, 0, 0), "head": (-10, 0, 0), "tail": (-10, 0, 0),
        "wing_right": (0, 55, -8), "wing_left": (0, -55, 8), "tip_right": (0, 30, 0), "tip_left": (0, -30, 0),
        "leg_right": (60, 0, 0), "leg_left": (60, 0, 0)}
# Hovering over the edge, marking his lane: body upright, wings beating, legs hanging, head fixed forward.
HOVER = {"body": (-35, 0, 0), "neck": (20, 0, 0), "head": (14, 0, 0), "tail": (20, 0, 0), "leg_right": (20, 0, 0),
         "leg_left": (20, 0, 0)}
# Grounded: sprawled on the summit, wings spread flat and limp, head down.
GROUNDED = {"root": {"position": (0, -6, 0)}, "body": (10, 0, 4), "neck": (30, 0, 0), "head": (20, -20, 10),
            "tail": (-4, 0, 0), "wing_right": (0, 10, 18), "wing_left": (0, -16, -20), "tip_right": (0, 0, 8),
            "tip_left": (0, 0, -6), "leg_right": (-60, 0, 10), "leg_left": (-50, 0, -10)}
# Dead: on his side, a wing over him.
DEAD = {"root": {"position": (0, -6, 0), "rotation": (0, 0, 80)}, "neck": (30, 0, 0), "head": (20, 0, 0),
        "wing_right": (0, 0, 20), "wing_left": (0, 0, 70), "tip_left": (0, 0, 30)}


def _beat(length, cycles=1, base=None, depth=1.0):
    """Wing beats: the spread wings up and down `cycles` times a clip, the primaries lagging behind."""
    base = base or {}
    out = {}
    for bone, amp, lag in (("wing_right", 38, 0.0), ("wing_left", -38, 0.0), ("tip_right", 22, 0.12), ("tip_left", -22, 0.12)):
        rest = base.get(bone, (0, 0, 0))
        out[bone] = {"rotation": wave(length, cycles, rest, (0, 0, amp * depth), phase=lag)}
    return out


def _bob(length, cycles=2, height=1.0):
    return {"root": {"position": wave(length, cycles, (0, 0, 0), (0, height, 0))}}


def _hop(length):
    """Stalking on foot: long strides, the body rocking, the head bobbing."""
    return {
        "leg_right": {"rotation": wave(length, 1, (0, 0, 0), (26, 0, 0))},
        "leg_left": {"rotation": wave(length, 1, (0, 0, 0), (26, 0, 0), phase=0.5)},
        "shank_right": {"rotation": wave(length, 1, (0, 0, 0), (14, 0, 0), phase=0.25)},
        "shank_left": {"rotation": wave(length, 1, (0, 0, 0), (14, 0, 0), phase=0.75)},
        "body": {"rotation": wave(length, 1, STAND["body"], (0, 0, 4))},
        "neck": {"rotation": wave(length, 2, STAND["neck"], (6, 0, 0))},
        "root": {"position": wave(length, 2, (0, 0, 0), (0, 0.8, 0))},
    }


def storm_roc_animations():
    """His body's clips (the "body" controller) and his wings' (the "wings" controller: folded or spread). The body's never
    key wings_folded or wings_spread."""
    beat = _beat(0.8)
    return animations({
        "animation.storm_roc.perch": posed(4.0, [(0.0, PERCH), (4.0, PERCH)], loop=True, waves={
            "head": {"rotation": wave(4.0, 1, PERCH["head"], (4, 30, 0))}}),
        "animation.storm_roc.wake": posed(2.5, [(0.0, PERCH), (0.5, SCREECH), (1.2, SCREECH), (2.5, SPREAD)],
                                          waves=_beat(2.5, 3, depth=0.8)),
        "animation.storm_roc.idle": posed(3.0, [(0.0, STAND), (3.0, STAND)], loop=True, waves={
            "head": {"rotation": wave(3.0, 1, STAND["head"], (4, 20, 0), phase=0.2)},
            "tail": {"rotation": wave(3.0, 2, STAND["tail"], (4, 0, 0))}}),
        "animation.storm_roc.walk": clip(1.2, _hop(1.2)),
        "animation.storm_roc.buffet_windup": posed(1.0, [(0.0, {**STAND, **SPREAD}), (0.7, REAR), (1.0, REAR)]),
        "animation.storm_roc.buffet": posed(0.7, [(0.0, REAR), (0.15, SWEEP), (0.45, SWEEP), (0.7, {**STAND, **SPREAD})]),
        "animation.storm_roc.screech": posed(1.5, [(0.0, {**STAND, **SPREAD}), (0.4, SCREECH), (1.5, SCREECH)], waves={
            "head": {"rotation": wave(1.5, 4, SCREECH["head"], (4, 6, 0))}}),
        "animation.storm_roc.strike": posed(0.8, [(0.0, SCREECH), (0.2, {**STAND, **SPREAD}), (0.8, {**STAND, **SPREAD})]),
        "animation.storm_roc.call": posed(1.0, [(0.0, STAND), (0.3, CALL), (1.0, CALL)], waves={
            "head": {"rotation": wave(1.0, 3, CALL["head"], (5, 0, 0))}}),
        "animation.storm_roc.strips_windup": posed(1.5, [(0.0, {**STAND, **SPREAD}), (0.6, UP), (1.5, UP)]),
        "animation.storm_roc.strips": clip(0.6, {**_beat(0.6, 1, depth=1.2), "body": {"rotation": still((-20, 0, 0))}}),
        "animation.storm_roc.fly": clip(0.8, {**beat, **_bob(0.8, 1, 1.5), "body": {"rotation": still((8, 0, 0))},
                                              "leg_right": {"rotation": still((50, 0, 0))},
                                              "leg_left": {"rotation": still((50, 0, 0))}}),
        "animation.storm_roc.hover": posed(0.6, [(0.0, HOVER), (0.6, HOVER)], loop=True,
                                           waves={**_beat(0.6, 1, depth=1.1), **_bob(0.6, 1, 1.0)}),
        "animation.storm_roc.dive": posed(0.5, [(0.0, DIVE), (0.5, DIVE)], loop=True, waves={
            "tip_right": {"rotation": wave(0.5, 3, DIVE["tip_right"], (0, 4, 0))},
            "tip_left": {"rotation": wave(0.5, 3, DIVE["tip_left"], (0, -4, 0))}}),
        "animation.storm_roc.grounded": posed(2.0, [(0.0, GROUNDED), (2.0, GROUNDED)], loop=True, waves={
            "head": {"rotation": wave(2.0, 3, GROUNDED["head"], (6, 8, 4))},
            "tip_right": {"rotation": wave(2.0, 4, GROUNDED["tip_right"], (0, 0, 6))}}),
        "animation.storm_roc.circle": clip(0.8, {**beat, **_bob(0.8, 1, 1.0), "root": {"rotation": still((0, 0, -18))},
                                                 "neck": {"rotation": still((-20, 0, 0))},
                                                 "head": {"rotation": wave(0.8, 2, (-24, 0, 0), (6, 0, 0))}}),
        "animation.storm_roc.death": posed(1.2, [(0.0, {**STAND, **SPREAD}), (0.8, DEAD), (1.2, DEAD)]),
        # His wings: folded along his sides, or spread.
        "animation.storm_roc.wings_folded": clip(1.0, {"wings_folded": {"scale": still((1, 1, 1))},
                                                       "wings_spread": {"scale": still((0, 0, 0))}}),
        "animation.storm_roc.wings_spread": clip(1.0, {"wings_folded": {"scale": still((0, 0, 0))},
                                                       "wings_spread": {"scale": still((1, 1, 1))}}),
    })


# ------------------------------------------------------------------------------------------------- a Gale Fledgling

# A half-grown roc, the size of a big dog: the same slate feathers, still fluffed with grey down at the breast, eyes as
# blue as his; always on the wing.

def gale_fledgling_model():
    m = Model("gale_fledgling", 64, (2.5, 1.5))
    m.bone("root")
    m.bone("body", "root", (0, 8, 1))
    m.cube("body", "torso", (-4, 5, -5), (8, 7, 11))
    m.cube("body", "down", (-3, 5, -6), (6, 5, 2))
    m.cube("body", "tail_fan", (-3, 9, 6), (6, 1, 5))
    m.bone("head", "body", (0, 11, -5))
    m.cube("head", "head", (-3, 10, -10), (6, 5, 6))
    m.cube("head", "beak", (-1, 10, -13), (2, 2, 3))
    m.cube("head", "eye", (-3.5, 12, -9), (1, 1, 1))
    m.cube("head", "eye", (2.5, 12, -9), (1, 1, 1), mirror=True)
    for side, name in ((-1, "right"), (1, "left")):
        mirror = side > 0
        m.bone(f"wing_{name}", "body", (side * 4, 11, -1))
        m.cube(f"wing_{name}", "inner_wing", ((-12 if side < 0 else 4), 10.5, -4), (8, 1, 8), mirror=mirror)
        m.bone(f"tip_{name}", f"wing_{name}", (side * 12, 11, -1))
        m.cube(f"tip_{name}", "primaries", ((-22 if side < 0 else 12), 10.5, -3), (10, 1, 7), mirror=mirror)
        m.bone(f"leg_{name}", "body", (side * 2, 5, 1))
        m.cube(f"leg_{name}", "talons", ((-3 if side < 0 else 1), 3, 0), (2, 2, 3), mirror=mirror)
    return m


def gale_fledgling_animations():
    def beat(length, cycles, depth):
        out = {}
        for bone, amp, lag in (("wing_right", 42, 0.0), ("wing_left", -42, 0.0), ("tip_right", 26, 0.12),
                               ("tip_left", -26, 0.12)):
            out[bone] = {"rotation": wave(length, cycles, (0, 0, 0), (0, 0, amp * depth), phase=lag)}
        return out
    return animations({
        "animation.gale_fledgling.rise": clip(1.0, {**beat(1.0, 3, 1.2), "body": {"rotation": still((-30, 0, 0))}}),
        "animation.gale_fledgling.fly": clip(0.4, {**beat(0.4, 1, 1.0), "root": {"position": wave(0.4, 1, (0, 0, 0), (0, 0.8, 0))},
                                                   "leg_right": {"rotation": still((40, 0, 0))},
                                                   "leg_left": {"rotation": still((40, 0, 0))}}),
        "animation.gale_fledgling.peck": posed(0.5, [(0.0, {"body": (0, 0, 0), "head": (0, 0, 0)}),
                                                     (0.15, {"body": (30, 0, 0), "head": (40, 0, 0)}),
                                                     (0.5, {"body": (0, 0, 0), "head": (0, 0, 0)})], waves=beat(0.5, 1, 0.6)),
    })


MODELS = {"storm_roc": storm_roc_model, "gale_fledgling": gale_fledgling_model}
ANIMATIONS = {"storm_roc": storm_roc_animations, "gale_fledgling": gale_fledgling_animations}
# The clips the Java names (StormRocEntity, GaleFledglingEntity).
CLIPS = {
    "storm_roc": ("perch", "wake", "idle", "walk", "buffet_windup", "buffet", "screech", "strike", "call",
                  "strips_windup", "strips", "fly", "hover", "dive", "grounded", "circle", "death", "wings_folded",
                  "wings_spread"),
    "gale_fledgling": ("rise", "fly", "peck"),
}
# The bones each of the Roc's controllers owns besides "body": no bone is animated by two.
CONTROLLERS = {"wings": WING_BONES}


def controller_of(clip_name):
    """Which of his controllers plays a clip of the Roc's."""
    return "wings" if clip_name.split(".")[-1].startswith("wings_") else "body"


def _check():
    """Every model packs; no clip of one of the Roc's controllers keys another's bone."""
    for build in MODELS.values():
        build().pack()
    owned = {bone: controller for controller, bones in CONTROLLERS.items() for bone in bones}
    for clip_name, body in storm_roc_animations()["animations"].items():
        for bone in body["bones"]:
            if owned.get(bone, "body") != controller_of(clip_name):
                raise ValueError(f"{clip_name} keys {bone}")
    return True


if __name__ == "__main__":
    print(_check())
