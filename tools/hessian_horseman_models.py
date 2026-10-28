"""GeckoLib bodies for the Headless Horseman in his lair (docs/features/hessian-horseman.md): the Horseman on his black
horse, his phantom riders, the pumpkin he tosses (the Pumpkin Toss) and the lantern head he hurls (the Head Hurl).
Geometry and animations only, as plain dicts; no I/O. tools/hessian_horseman_art.py paints their sheets on the same box-UV
regions, at TEXTURE_SCALE times the size the models declare (docs/ART_DIRECTION.md, "High resolution"). The builder and the
posing helpers are Vesperine's (tools/vesperine_models.py), which also sets out the conventions: pixels (16 = one block),
y = 0 at the hooves, the model facing north (-z) with its left on +x; +x pitches a bone's top forward, -x raises a hanging
arm forward, a right arm turns outwards with +z and a left one with -z, and +y turns the front to the model's right. A
keyframed rotation adds to a bone's rest rotation.

Every cube is named by its part; cubes of one part share one box-UV region, and `Model.pack` lays the regions out on the
sheet without overlaps. CLIPS lists the clips the Java plays; CONTROLLERS the bones each of the Horseman's controllers
besides "body" owns (a controller sets every channel of each bone it animates, so no bone has two).
"""
import math

from vesperine_models import Model, TEXTURE_SCALE, animations, clip, keys, posed, still, wave

__all__ = ["TEXTURE_SCALE", "MODELS", "ANIMATIONS", "CLIPS", "CONTROLLERS", "STANDS"]

# ------------------------------------------------------------------------------------------------- the Horseman
# A big black warhorse (a hand taller than any horse of the Overworld), its mane and tail long and ragged, a red-eyed
# head in a plain bridle, a saddle on a red shabraque edged with tarnished gold, a pumpkin in a net at each side of the
# saddle's back, and on the pommel the jack-o'-lantern he carries for a head. On it the Hessian trooper: tall riding boots
# in the stirrups, buff breeches, a dark blue coat with red facings and white crossbelts, its tails over the horse's
# back, fringed epaulettes, a collar with nothing above it but embers; a black cloak streaming from his shoulders;
# a curved cavalry sabre in his right hand and an iron lantern in his left. The lantern's fire and the saddle head's face
# are the "lantern" controller's (dark, orange or white); the lantern, the pumpkin or the head he holds up, and the head
# on the pommel, are the "held" controller's.
LEG_X = 3.5
FRONT_Z = -9.0
HIND_Z = 9.0
KNEE_Y = 9
BELLY_Y = 17
BACK_Y = 28
SEAT_Y = 30
HIP_PIVOT = (0, 18, 9)     # where the horse rears about: over his hind legs
SHOULDER_Y = 44
ARM_X = 5.5
HAND_Y = 26
# How tall each model stands, in pixels, from its hooves to the top of its tallest part at rest (the audit compares them
# with the entities' sizes in Java): the rider's collar and the embers in it.
STANDS = {"hessian_horseman": 47, "phantom_rider": 47}
# His sabre: a curved cavalry blade, upright from the grip in his right hand, in segments curving back: length, angle
# from the last (degrees about x).
SABRE_GRIP = (-ARM_X, HAND_Y + 1.5, 0.0)
SABRE = ((6, 0), (6, -6), (6, -7), (4, -9))


def horse(m, phantom):
    """The horse: hind legs from the root (they stay planted when he rears), the rest from the "horse" bone, which rears
    about his hips."""
    m.bone("root")
    for z, end in ((HIND_Z, "hind"), (FRONT_Z, "front")):
        parent = "root" if end == "hind" else "horse"
        if end == "front":
            m.bone("horse", "root", HIP_PIVOT)
        for side, name in ((-1, "right"), (1, "left")):
            mirror = side > 0
            x = side * LEG_X
            m.bone(f"leg_{end}_{name}", parent, (x, BELLY_Y + 1, z))
            m.cube(f"leg_{end}_{name}", "leg", (x - 2, KNEE_Y, z - 2.5), (4, 10, 5), mirror=mirror)
            m.bone(f"shin_{end}_{name}", f"leg_{end}_{name}", (x, KNEE_Y, z))
            m.cube(f"shin_{end}_{name}", "cannon", (x - 1.5, 3, z - 1.5), (3, 7, 3), mirror=mirror)
            m.cube(f"shin_{end}_{name}", "feather", (x - 2, 1.5, z - 2), (4, 3, 4), mirror=mirror)
            m.cube(f"shin_{end}_{name}", "hoof", (x - 2, 0, z - 2.5), (4, 2, 5), mirror=mirror)
    # His barrel, the deep chest before it and the round rump behind.
    m.cube("horse", "barrel", (-5.5, BELLY_Y, -12), (11, 11, 24))
    m.cube("horse", "breast", (-5, BELLY_Y + 1, -14), (10, 9, 3))
    m.cube("horse", "rump", (-6, BELLY_Y + 2, 7), (12, 10, 7))
    # His neck, pitched forward, the mane along its crest; his head, a bridle on it, red eyes and laid-back ears.
    m.bone("neck", "horse", (0, 26, -11), rotation=(32, 0, 0))
    m.cube("neck", "neck", (-3, 24, -14), (6, 16, 7))
    m.cube("neck", "mane", (-1, 25, -8), (2, 17, 3))
    m.bone("head", "neck", (0, 38, -11))
    m.cube("head", "skull", (-3.5, 35, -17), (7, 7, 10))
    m.cube("head", "muzzle", (-2.5, 34, -23), (5, 6, 7))
    m.cube("head", "forelock", (-1.5, 41, -15), (3, 2, 5))
    m.cube("head", "ear", (-3, 42, -10), (2, 4, 2), rotation=(-20, 0, -10), pivot=(-2, 42, -9))
    m.cube("head", "ear", (1, 42, -10), (2, 4, 2), rotation=(-20, 0, 10), pivot=(2, 42, -9), mirror=True)
    m.cube("head", "noseband", (-3, 36, -20), (6, 1, 4))
    m.cube("head", "eye", (-3.55, 39, -15), (0, 1, 2))
    m.cube("head", "eye", (3.55, 39, -15), (0, 1, 2), mirror=True)
    # His tail, long and ragged, in two parts.
    m.bone("tail", "horse", (0, 27, 13), rotation=(22, 0, 0))
    m.cube("tail", "tail", (-1.5, 13, 12.5), (3, 14, 4))
    m.bone("tail_tip", "tail", (0, 13, 14.5))
    m.cube("tail_tip", "tail_tip", (-1.5, 4, 13), (3, 10, 3))
    # The saddle on its shabraque, the stirrups, and the netted pumpkins at the saddle's back.
    m.cube("horse", "shabraque", (-6, 21, -6), (12, 7, 15))
    m.cube("horse", "seat", (-4.5, BACK_Y, -4), (9, 2, 10))
    m.cube("horse", "pommel", (-2, BACK_Y + 2, -5), (4, 2, 2))
    m.cube("horse", "cantle", (-3.5, BACK_Y + 2, 5), (7, 3, 2))
    for side in (-1, 1):
        m.cube("horse", "stirrup", (side * 7 - 0.5, 14, -2), (1, 2, 3), mirror=side > 0)
        m.cube("horse", "stirrup_leather", (side * 6.5 - 0.5, 16, -1), (1, 7, 1), mirror=side > 0)
        if not phantom:
            m.cube("horse", "bag_pumpkin", (side * 8 - 2, 22, 4), (4, 4, 4), mirror=side > 0)
            m.cube("horse", "bag_net", (side * 8 - 2.5, 21.5, 3.5), (5, 5, 5), mirror=side > 0)


def rider(m, phantom):
    """The rider, astride: his hips on the seat, thighs down the horse's flanks, boots in the stirrups; his coat, crossbelts
    and coat-tails; his cloak; his arms; his high collar with the embers in it."""
    m.bone("rider", "horse", (0, SEAT_Y, 0))
    m.cube("rider", "breeches", (-4, SEAT_Y, -2.5), (8, 4, 5))
    for side, name in ((-1, "right"), (1, "left")):
        mirror = side > 0
        x = side * 6.5
        m.bone(f"thigh_{name}", "rider", (side * 4, SEAT_Y + 2, 0), rotation=(-38, 0, side * -8))
        m.cube(f"thigh_{name}", "thigh", (x - 1.5, 23, -2.5), (3, 10, 5), mirror=mirror)
        m.bone(f"boot_{name}", f"thigh_{name}", (x, 24, 0), rotation=(44, 0, 0))
        m.cube(f"boot_{name}", "boot", (x - 1.5, 14, -1.5), (3, 10, 3), mirror=mirror)
        m.cube(f"boot_{name}", "boot_cuff", (x - 2, 21, -2), (4, 3, 4), mirror=mirror)
        m.cube(f"boot_{name}", "boot_foot", (x - 1.5, 14, -4.5), (3, 2, 5), mirror=mirror)
        m.cube(f"boot_{name}", "spur", (x - 0.5, 15, 1.5), (1, 1, 2), mirror=mirror)
    m.bone("torso", "rider", (0, SEAT_Y + 4, 0))
    m.cube("torso", "coat", (-4.5, SEAT_Y + 4, -2.5), (9, 11, 5))
    m.cube("torso", "collar", (-3.5, SHOULDER_Y + 1, -2.5), (7, 2, 5))
    m.cube("torso", "embers", (-2.5, SHOULDER_Y + 3.05, -1.5), (5, 0, 3))
    for side in (-1, 1):
        m.cube("torso", "epaulette", (side * 5.5 - 1, SHOULDER_Y - 1, -2.5), (2, 2, 5), mirror=side > 0)
    m.bone("coat_tails", "torso", (0, SEAT_Y + 4, 2.5))
    m.cube("coat_tails", "coat_tail", (-4.5, SEAT_Y - 4, 2.5), (9, 8, 1))
    # His cloak, from his shoulders, streaming back, in two parts.
    m.bone("cloak", "torso", (0, SHOULDER_Y, 2.5), rotation=(14, 0, 0))
    m.cube("cloak", "cloak", (-6, SHOULDER_Y - 11, 2.5), (12, 11, 1))
    m.bone("cloak_low", "cloak", (0, SHOULDER_Y - 11, 3))
    m.cube("cloak_low", "cloak_low", (-6, SHOULDER_Y - 23, 2.5), (12, 12, 1))
    for side, name in ((-1, "right"), (1, "left")):
        mirror = side > 0
        x = side * ARM_X
        m.bone(f"arm_{name}", "torso", (x, SHOULDER_Y, 0))
        m.cube(f"arm_{name}", "sleeve", (x - 1.5, SHOULDER_Y - 9, -1.5), (3, 9, 3), mirror=mirror)
        m.bone(f"forearm_{name}", f"arm_{name}", (x, SHOULDER_Y - 8, 0))
        m.cube(f"forearm_{name}", "forearm", (x - 1.5, HAND_Y + 2, -1.5), (3, 8, 3), mirror=mirror)
        m.cube(f"forearm_{name}", "gauntlet", (x - 2, HAND_Y + 2, -2), (4, 3, 4), mirror=mirror)
        m.bone(f"hand_{name}", f"forearm_{name}", (x, HAND_Y + 2, 0))
        m.cube(f"hand_{name}", "glove", (x - 1.5, HAND_Y - 1, -1.5), (3, 3, 3), mirror=mirror)
    # His sabre, turned in his hand by the body's clips through its grip: the grip, a brass knuckle-bow, and the blade.
    gx, gy, gz = SABRE_GRIP
    m.bone("sabre_grip", "hand_right", SABRE_GRIP)
    m.cube("sabre_grip", "hilt", (gx - 0.5, gy - 2, gz - 0.5), (1, 3, 1))
    m.cube("sabre_grip", "guard", (gx - 1, gy + 1, gz - 2), (2, 1, 4))
    m.cube("sabre_grip", "knuckle_bow", (gx - 0.5, gy - 2, gz - 2), (1, 3, 1))
    y, z, angle = gy + 2, gz, 0.0
    for i, (length, turn) in enumerate(SABRE):
        angle += turn
        m.cube("sabre_grip", f"blade_{i}", (gx - 0.5, y, z - 1), (1, length, 2), rotation=(angle, 0, 0), pivot=(gx, y, z))
        a = math.radians(angle)
        y, z = y + (length - 0.5) * math.cos(a), z - (length - 0.5) * math.sin(a)
    if phantom:
        return
    # His lantern, hanging from his left hand (the "held" controller's), its fire the "lantern" controller's: orange, or
    # white when he is revealed.
    lx = ARM_X
    m.bone("lantern_body", "hand_left", (lx, HAND_Y, 0))
    m.cube("lantern_body", "lantern_bail", (lx - 0.5, HAND_Y - 3, -0.5), (1, 3, 1))
    m.cube("lantern_body", "lantern_cap", (lx - 2.5, HAND_Y - 4, -2.5), (5, 1, 5))
    m.cube("lantern_body", "lantern_cage", (lx - 2, HAND_Y - 11, -2), (4, 7, 4))
    m.cube("lantern_body", "lantern_foot", (lx - 2.5, HAND_Y - 12, -2.5), (5, 1, 5))
    m.bone("lantern_flame", "lantern_body", (lx, HAND_Y - 8, 0))
    m.cube("lantern_flame", "fire", (lx - 1, HAND_Y - 10, -1), (2, 5, 2))
    m.bone("lantern_white", "lantern_body", (lx, HAND_Y - 8, 0))
    m.cube("lantern_white", "fire_white", (lx - 1.2, HAND_Y - 10.2, -1.2), (2, 5, 2))
    # What he holds up in his left hand to throw: a pumpkin from his saddle, or his lantern head.
    m.bone("held_pumpkin", "hand_left", (lx, HAND_Y, 0))
    m.cube("held_pumpkin", "held", (lx - 3, HAND_Y + 1, -3), (6, 5, 6))
    m.cube("held_pumpkin", "held_face", (lx - 2, HAND_Y + 2, -3.05), (4, 3, 0))
    m.bone("held_head", "hand_left", (lx, HAND_Y, 0))
    m.cube("held_head", "jack", (lx - 3.5, HAND_Y + 1, -3.5), (7, 6, 7))
    m.cube("held_head", "jack_stem", (lx - 0.5, HAND_Y + 7, -0.5), (1, 2, 1))
    m.cube("held_head", "jack_face", (lx - 2.5, HAND_Y + 2, -3.55), (5, 4, 0))


def saddle_head(m):
    """The jack-o'-lantern on his saddle's pommel, where his head should be (the "held" controller's; hidden while he holds
    it up to hurl), its face the "lantern" controller's."""
    m.bone("saddle_head", "horse", (0, BACK_Y + 4, -6))
    m.cube("saddle_head", "jack", (-3.5, BACK_Y + 4, -9.5), (7, 6, 7))
    m.cube("saddle_head", "jack_stem", (-0.5, BACK_Y + 10, -6.5), (1, 2, 1))
    m.bone("head_flame", "saddle_head", (0, BACK_Y + 6, -9.5))
    m.cube("head_flame", "jack_face", (-2.5, BACK_Y + 5, -9.55), (5, 4, 0))
    m.bone("head_white", "saddle_head", (0, BACK_Y + 6, -9.5))
    m.cube("head_white", "jack_face_white", (-2.5, BACK_Y + 5, -9.6), (5, 4, 0))


def hessian_horseman_model():
    m = Model("hessian_horseman", 256, (3.0, 3.6))
    horse(m, phantom=False)
    rider(m, phantom=False)
    saddle_head(m)
    return m


def phantom_rider_model():
    """A phantom rider: the same horse and trooper, without lantern, saddle head or netted pumpkins; all blue fire."""
    m = Model("phantom_rider", 256, (3.0, 3.6))
    horse(m, phantom=True)
    rider(m, phantom=True)
    return m


# The bones each of his controllers owns besides "body": no bone is animated by two.
CONTROLLERS = {"lantern": ("lantern_flame", "lantern_white", "head_flame", "head_white"),
               "held": ("lantern_body", "held_pumpkin", "held_head", "saddle_head")}

# ------------------------------------------------------------------------------------------------- his poses
# Each a dict of bone -> rotation (added to the bone's rest rotation), or {"rotation": ..., "position": ...}.

REST = {}
# Mounted at the bridge's foot, waiting: his horse's head low, his sabre resting across his thigh, his lantern hanging.
WAIT = {"neck": (12, 0, 0), "head": (10, 0, 0), "arm_right": (-25, 0, 5), "forearm_right": (-35, 0, 0),
        "sabre_grip": (60, 0, 0), "arm_left": (-10, 0, -4)}
# His horse reared: forehand up off the ground, forelegs folded, neck and head flung up; the rider leaning into it.
REARED = {"horse": (-38, 0, 0), "leg_front_right": (-55, 0, 0), "shin_front_right": (75, 0, 0),
          "leg_front_left": (-35, 0, 0), "shin_front_left": (60, 0, 0), "leg_hind_right": (12, 0, 0),
          "leg_hind_left": (6, 0, 0), "neck": (-22, 0, 0), "head": (-15, 0, 0), "tail": (-15, 0, 0),
          "rider": (30, 0, 0), "arm_right": (-150, 0, 15), "forearm_right": (-10, 0, 0), "sabre_grip": (-20, 0, 0),
          "arm_left": (-30, 0, -15), "cloak": (-10, 0, 0)}
# Down again with a crash: forelegs planted, head low.
LANDED = {"horse": (6, 0, 0), "root": {"position": (0, -1, 0)}, "leg_front_right": (10, 0, 0),
          "leg_front_left": (8, 0, 0), "neck": (18, 0, 0), "head": (10, 0, 0), "rider": (-6, 0, 0),
          "arm_right": (-60, 0, 20), "sabre_grip": (-40, 0, 0)}
# Sabre lowered along his line for the Sabre Pass, the horse gathered to spring.
LOWERED = {"root": {"position": (0, -1, 0)}, "horse": (4, 0, 0), "neck": (15, 0, 0), "head": (15, 0, 0),
           "rider": (18, 0, 0), "arm_right": (-80, 0, 10), "forearm_right": (-5, 0, 0), "sabre_grip": (-12, 0, 0),
           "arm_left": (-20, 0, -10), "leg_hind_right": (-18, 0, 0), "leg_hind_left": (-18, 0, 0),
           "shin_hind_right": (25, 0, 0), "shin_hind_left": (25, 0, 0), "cloak": (12, 0, 0)}
# Lifting a pumpkin from his saddle high in his left hand; then the throw.
HEFT = {"arm_left": (-170, 0, -15), "forearm_left": (-10, 0, 0), "torso": (-6, 10, 0), "arm_right": (-30, 0, 15),
        "sabre_grip": (40, 0, 0)}
THROW = {"arm_left": (-60, 0, -10), "forearm_left": (-5, 0, 0), "torso": (14, -15, 0), "arm_right": (-25, 0, 20),
         "sabre_grip": (40, 0, 0)}
# Wheeling and lifting his lantern high over the hollow (the Dark Road); held high the longer at the Hollow Wakes.
WHEEL = {"horse": (-10, 0, 0), "root": {"rotation": (0, 25, 0)}, "arm_left": (-175, 0, -10), "forearm_left": (-5, 0, 0),
         "torso": (-8, 0, 0), "neck": (-10, 15, 0), "leg_front_right": (-30, 0, 0), "shin_front_right": (45, 0, 0),
         "cloak": (-5, 0, 0)}
LIFTED = {"horse": (-14, 0, 0), "arm_left": (-178, 0, -5), "forearm_left": (0, 0, 0), "torso": (-12, 0, 0),
          "neck": (-15, 0, 0), "head": (-10, 0, 0), "arm_right": (-40, 0, 25), "sabre_grip": (30, 0, 0),
          "leg_front_left": (-25, 0, 0), "shin_front_left": (40, 0, 0)}
# Drawing back his lantern head to hurl it; then the hurl.
DRAW_BACK = {"arm_left": (-150, -20, -30), "forearm_left": (-30, 0, 0), "torso": (-14, 25, 0), "rider": (-6, 0, 0),
             "arm_right": (-40, 0, 25), "sabre_grip": (40, 0, 0), "horse": (-6, 0, 0)}
HURLED = {"arm_left": (-45, 0, -5), "forearm_left": (-5, 0, 0), "torso": (20, -25, 0), "rider": (8, 0, 0),
          "arm_right": (-25, 0, 20), "sabre_grip": (40, 0, 0)}
# His sabre raised high to call his phantom riders; held there while they ride.
RAISED = {"arm_right": (-178, 0, 8), "forearm_right": (-5, 0, 0), "sabre_grip": (0, 0, 0), "torso": (-10, 0, 0),
          "horse": (-12, 0, 0), "leg_front_right": (-35, 0, 0), "shin_front_right": (55, 0, 0), "neck": (-12, 0, 0),
          "head": (-10, 0, 0), "cloak": (-8, 0, 0)}
# Shying from the light (revealed): reared and twisting, the rider thrown back.
SHY = {**REARED, "horse": (-30, 0, 0), "root": {"rotation": (0, -20, 0)}, "rider": (-10, 0, 0), "torso": (-25, 0, 0),
       "arm_right": (-60, 0, 50), "arm_left": (-70, 0, -50), "neck": (-30, 25, 0)}
# Fallen: the horse down on its side, the rider slumped over its neck.
FALLEN = {"root": {"rotation": (0, 0, 80), "position": (6, -6, 0)}, "leg_front_right": (-30, 0, 0),
          "leg_front_left": (-50, 0, 0), "leg_hind_right": (30, 0, 0), "leg_hind_left": (45, 0, 0), "neck": (40, 0, 0),
          "head": (20, 0, 0), "rider": (40, 0, 0), "torso": (35, 0, 0), "arm_right": (-20, 0, 30), "arm_left": (-15, 0, -40),
          "sabre_grip": (80, 0, 0), "tail": (40, 0, 0)}


def looped(length, pose, waves):
    """A looping clip holding `pose`, with `waves` ({bone: {channel: keyframes}}) moving those of its bones about their
    place in it (each wave's base is the pose's own), and the rest of it held still."""
    bones = {}
    for bone, value in pose.items():
        for channel, held in (value if isinstance(value, dict) else {"rotation": value}).items():
            bones.setdefault(bone, {})[channel] = still(held)
    for bone, extra in waves.items():
        bones.setdefault(bone, {}).update(extra)
    return clip(length, bones)


def _rot(pose, bone):
    value = pose.get(bone, (0, 0, 0))
    return value["rotation"] if isinstance(value, dict) else value


def _breath(length, pose=None, scale=1.0):
    """Standing: the horse breathing, his tail swishing, his ears turning; the cloak stirring."""
    p = pose or {}
    s = scale
    return {
        "neck": {"rotation": wave(length, 1, _rot(p, "neck"), (3 * s, 2 * s, 0))},
        "head": {"rotation": wave(length, 1, _rot(p, "head"), (4 * s, 0, 2 * s), phase=0.2)},
        "tail": {"rotation": wave(length, 1, _rot(p, "tail"), (3 * s, 0, 10 * s), phase=0.3)},
        "tail_tip": {"rotation": wave(length, 1, (0, 0, 0), (4 * s, 0, 12 * s), phase=0.45)},
        "cloak": {"rotation": wave(length, 1, _rot(p, "cloak"), (3 * s, 0, 1 * s), phase=0.1)},
        "cloak_low": {"rotation": wave(length, 1, (0, 0, 0), (5 * s, 0, 2 * s), phase=0.25)},
        "coat_tails": {"rotation": wave(length, 1, (0, 0, 0), (2 * s, 0, 0), phase=0.35)},
    }


def _trot(length, scale=1.0):
    """Trotting: the legs in diagonal pairs (right fore with left hind), each shin folding as its hoof lifts; the head
    nodding, the tail and cloak swinging, the rider rising in the saddle."""
    s = scale
    out = {}
    for leg, phase in (("front_right", 0.0), ("hind_left", 0.0), ("front_left", 0.5), ("hind_right", 0.5)):
        sign = 1 if leg.startswith("front") else -1
        out[f"leg_{leg}"] = {"rotation": wave(length, 1, (0, 0, 0), (-26 * s * sign, 0, 0), phase=phase)}
        out[f"shin_{leg}"] = {"rotation": wave(length, 1, (18 * s * sign, 0, 0), (18 * s * sign, 0, 0), phase=phase + 0.25)}
    out["neck"] = {"rotation": wave(length, 2, (4, 0, 0), (4 * s, 0, 0))}
    out["head"] = {"rotation": wave(length, 2, (4, 0, 0), (3 * s, 0, 0), phase=0.15)}
    out["tail"] = {"rotation": wave(length, 2, (10, 0, 0), (6 * s, 0, 4 * s), phase=0.3)}
    out["tail_tip"] = {"rotation": wave(length, 2, (6, 0, 0), (8 * s, 0, 6 * s), phase=0.45)}
    out["rider"] = {"position": wave(length, 2, (0, 0, 0), (0, 0.8 * s, 0), phase=0.25)}
    out["cloak"] = {"rotation": wave(length, 2, (10, 0, 0), (5 * s, 0, 2 * s), phase=0.1)}
    out["cloak_low"] = {"rotation": wave(length, 2, (6, 0, 0), (8 * s, 0, 2 * s), phase=0.3)}
    out["coat_tails"] = {"rotation": wave(length, 2, (4, 0, 0), (4 * s, 0, 0), phase=0.35)}
    out["arm_right"] = {"rotation": wave(length, 1, (-25, 0, 5), (6 * s, 0, 0))}
    out["forearm_right"] = {"rotation": still((-35, 0, 0))}
    out["sabre_grip"] = {"rotation": still((60, 0, 0))}
    out["arm_left"] = {"rotation": wave(length, 1, (-10, 0, -4), (6 * s, 0, 0), phase=0.5)}
    return out


def _gallop(length, lowered=True, scale=1.0):
    """At the gallop: forelegs reaching together and hind legs driving together, the barrel rocking, the mane and cloak
    streaming; the rider low over the neck, his sabre out along his line (or raised, for his phantoms)."""
    s = scale
    out = {}
    for end, phase in (("front", 0.0), ("hind", 0.45)):
        sign = 1 if end == "front" else -1
        for name, lag in (("right", 0.0), ("left", 0.08)):
            out[f"leg_{end}_{name}"] = {"rotation": wave(length, 1, (0, 0, 0), (-45 * s * sign, 0, 0), phase=phase + lag)}
            out[f"shin_{end}_{name}"] = {"rotation": wave(length, 1, (30 * s * sign, 0, 0), (30 * s * sign, 0, 0),
                                                          phase=phase + lag + 0.25)}
    out["horse"] = {"rotation": wave(length, 1, (0, 0, 0), (6 * s, 0, 0), phase=0.1)}
    out["root"] = {"position": wave(length, 1, (0, 0.5, 0), (0, 1.5 * s, 0), phase=0.35)}
    out["neck"] = {"rotation": wave(length, 1, (-6, 0, 0), (8 * s, 0, 0), phase=0.2)}
    out["head"] = {"rotation": wave(length, 1, (8, 0, 0), (5 * s, 0, 0), phase=0.3)}
    out["tail"] = {"rotation": wave(length, 2, (55, 0, 0), (8 * s, 0, 6 * s))}
    out["tail_tip"] = {"rotation": wave(length, 2, (15, 0, 0), (10 * s, 0, 8 * s), phase=0.2)}
    out["cloak"] = {"rotation": wave(length, 2, (60, 0, 0), (8 * s, 0, 3 * s))}
    out["cloak_low"] = {"rotation": wave(length, 2, (20, 0, 0), (14 * s, 0, 4 * s), phase=0.2)}
    out["coat_tails"] = {"rotation": wave(length, 2, (25, 0, 0), (8 * s, 0, 0), phase=0.3)}
    out["rider"] = {"rotation": still((22, 0, 0))}
    if lowered:
        out["arm_right"] = {"rotation": still((-85, 0, 10))}
        out["sabre_grip"] = {"rotation": still((-10, 0, 0))}
    else:
        out["arm_right"] = {"rotation": wave(length, 2, (-170, 0, 8), (6 * s, 0, 0))}
    out["arm_left"] = {"rotation": still((-25, 0, -12))}
    return out


def _lantern(flame, white, flicker=0.0):
    """A lantern clip: the orange fire (his lantern's and the saddle head's face) at `flame`, flickering by `flicker`, and
    the white fire at `white`."""
    lit = wave(1.0, 4, (flame, flame, flame), (flicker, flicker * 1.5, flicker)) if flicker else still((flame, flame, flame))
    on = still((white, white, white))
    return clip(1.0, {"lantern_flame": {"scale": lit}, "head_flame": {"scale": lit}, "lantern_white": {"scale": on},
                      "head_white": {"scale": on}})


def _held(lantern, pumpkin, head):
    """A held clip: the lantern in his hand, the pumpkin or the head held up, and the head on his pommel (gone while he
    holds it up)."""
    def scale(on):
        return {"scale": still((1, 1, 1) if on else (0, 0, 0))}
    return clip(1.0, {"lantern_body": scale(lantern), "held_pumpkin": scale(pumpkin), "held_head": scale(head),
                      "saddle_head": scale(not head)})


def hessian_horseman_animations():
    """His body's clips (the "body" controller), his lantern (dark, lit, white) and what he holds. The body's never key a
    bone of the others, nor theirs a body bone."""
    return animations({
        "animation.hessian_horseman.wait": looped(4.0, WAIT, _breath(4.0, WAIT, 0.6)),
        "animation.hessian_horseman.rear_wake": posed(2.5, [(0.0, WAIT), (0.5, REARED), (1.2, REARED), (1.6, LANDED),
                                                             (2.0, LANDED), (2.5, REST)]),
        "animation.hessian_horseman.idle": clip(2.5, _breath(2.5)),
        "animation.hessian_horseman.walk": clip(0.8, _trot(0.8)),
        "animation.hessian_horseman.pass_windup": posed(1.0, [(0.0, REST), (0.5, LOWERED), (1.0, LOWERED)], waves={
            "leg_front_right": {"rotation": wave(1.0, 3, (0, 0, 0), (-25, 0, 0))},
            "shin_front_right": {"rotation": wave(1.0, 3, (15, 0, 0), (15, 0, 0), phase=0.25)}}),
        "animation.hessian_horseman.pass": clip(0.5, _gallop(0.5, lowered=True)),
        "animation.hessian_horseman.toss_windup": posed(1.0, [(0.0, REST), (0.6, HEFT), (1.0, HEFT)]),
        "animation.hessian_horseman.toss": posed(0.6, [(0.0, HEFT), (0.15, THROW), (0.6, REST)]),
        "animation.hessian_horseman.stomp_windup": posed(0.8, [(0.0, REST), (0.5, REARED), (0.8, REARED)]),
        "animation.hessian_horseman.stomp": posed(0.7, [(0.0, REARED), (0.15, LANDED), (0.4, LANDED), (0.7, REST)]),
        "animation.hessian_horseman.snuff": posed(1.2, [(0.0, REST), (0.5, WHEEL), (1.0, WHEEL), (1.2, REST)]),
        "animation.hessian_horseman.hurl_windup": posed(1.5, [(0.0, REST), (0.7, HEFT), (1.2, DRAW_BACK),
                                                              (1.5, DRAW_BACK)]),
        "animation.hessian_horseman.hurl": posed(0.7, [(0.0, DRAW_BACK), (0.15, HURLED), (0.7, REST)]),
        "animation.hessian_horseman.riders_windup": posed(1.5, [(0.0, REST), (0.6, RAISED), (1.5, RAISED)]),
        "animation.hessian_horseman.riders": looped(1.0, RAISED, {
            "leg_front_right": {"rotation": wave(1.0, 2, _rot(RAISED, "leg_front_right"), (12, 0, 0))},
            "neck": {"rotation": wave(1.0, 2, _rot(RAISED, "neck"), (5, 6, 0), phase=0.2)},
            "cloak_low": {"rotation": wave(1.0, 2, (10, 0, 0), (10, 0, 3))}}),
        "animation.hessian_horseman.stagger": looped(1.0, SHY, {
            "horse": {"rotation": wave(1.0, 2, _rot(SHY, "horse"), (6, 0, 4))},
            "neck": {"rotation": wave(1.0, 2, _rot(SHY, "neck"), (8, 10, 0), phase=0.2)},
            "leg_front_left": {"rotation": wave(1.0, 2, _rot(SHY, "leg_front_left"), (20, 0, 0), phase=0.4)}}),
        "animation.hessian_horseman.lift": posed(2.0, [(0.0, REST), (0.5, LIFTED), (2.0, LIFTED)], waves={
            "cloak_low": {"rotation": wave(2.0, 2, (6, 0, 0), (8, 0, 3))},
            "tail": {"rotation": wave(2.0, 2, (0, 0, 0), (4, 0, 12))}}),
        "animation.hessian_horseman.death": posed(2.5, [(0.0, REST), (0.4, REARED), (1.0, REARED), (1.8, FALLEN),
                                                        (2.5, FALLEN)]),
        # His lantern and the saddle head's face: dark while he waits and when he falls, burning orange, flaring white.
        "animation.hessian_horseman.lantern_dark": _lantern(0.0, 0.0),
        "animation.hessian_horseman.lantern_lit": _lantern(1.0, 0.0, flicker=0.06),
        "animation.hessian_horseman.lantern_white": _lantern(0.0, 1.0),
        # What he holds.
        "animation.hessian_horseman.held_none": _held(True, False, False),
        "animation.hessian_horseman.held_pumpkin": _held(False, True, False),
        "animation.hessian_horseman.held_head": _held(False, False, True),
    })


def phantom_rider_animations():
    return animations({"animation.phantom_rider.gallop": clip(0.4, _gallop(0.4, lowered=False))})


# ------------------------------------------------------------------------------------------------- what he throws
# The pumpkin he tosses: a pumpkin from the net at his saddle, carved, burning inside, tumbling as it flies. His lantern
# head: the jack-o'-lantern from his pommel, bigger, its grin wide, blazing.

def hessian_pumpkin_model():
    m = Model("hessian_pumpkin", 64, (1.0, 1.0))
    m.bone("tumble", None, (0, 4, 0))
    m.cube("tumble", "small_pumpkin", (-4.5, 0, -4.5), (9, 8, 9))
    m.cube("tumble", "small_stem", (-1, 8, -1), (2, 2, 2))
    m.cube("tumble", "small_face", (-3, 2, -4.55), (6, 4, 0))
    return m


def hessian_head_model():
    m = Model("hessian_head", 128, (1.4, 1.4))
    m.bone("tumble", None, (0, 6, 0))
    m.cube("tumble", "big_pumpkin", (-7, 0, -7), (14, 12, 14))
    m.cube("tumble", "big_stem", (-1.5, 12, -1.5), (3, 3, 3), rotation=(0, 0, 10), pivot=(0, 12, 0))
    m.cube("tumble", "big_face", (-5, 2, -7.05), (10, 8, 0))
    return m


def hessian_pumpkin_animations():
    return animations({"animation.hessian_pumpkin.tumble": clip(0.6, {"tumble": {"rotation": keys({0.0: (0, 0, 0),
                                                                                                   0.6: (360, 180, 0)})}})})


def hessian_head_animations():
    return animations({"animation.hessian_head.tumble": clip(0.8, {"tumble": {"rotation": keys({0.0: (0, 0, 0),
                                                                                               0.8: (360, 0, 90)})}})})


# ------------------------------------------------------------------------------------------------- the tables

MODELS = {"hessian_horseman": hessian_horseman_model, "phantom_rider": phantom_rider_model,
          "hessian_pumpkin": hessian_pumpkin_model, "hessian_head": hessian_head_model}
ANIMATIONS = {"hessian_horseman": hessian_horseman_animations, "phantom_rider": phantom_rider_animations,
              "hessian_pumpkin": hessian_pumpkin_animations, "hessian_head": hessian_head_animations}
# The clips the Java names (HessianHorsemanEntity, PhantomRiderEntity, HessianPumpkinEntity).
CLIPS = {
    "hessian_horseman": ("wait", "rear_wake", "idle", "walk", "pass_windup", "pass", "toss_windup", "toss", "stomp_windup",
                         "stomp", "snuff", "hurl_windup", "hurl", "riders_windup", "riders", "stagger", "lift", "death",
                         "lantern_dark", "lantern_lit", "lantern_white", "held_none", "held_pumpkin", "held_head"),
    "phantom_rider": ("gallop",),
    "hessian_pumpkin": ("tumble",),
    "hessian_head": ("tumble",),
}
# The clip that belongs to each of the Horseman's controllers besides "body", by its name's start.
CONTROLLER_CLIPS = {"lantern": "lantern_", "held": "held_"}


def owner(clip_name):
    """Which of the Horseman's controllers plays `clip_name` (its last part)."""
    for controller, prefix in CONTROLLER_CLIPS.items():
        if clip_name.startswith(prefix):
            return controller
    return "body"


def _check():
    """The models pack; no clip of the Horseman's body keys a bone of his other controllers, nor theirs any other bone;
    every clip the Java plays is here; every bone a clip keys exists."""
    for build in MODELS.values():
        build().pack()
    owned = {bone: controller for controller, bones in CONTROLLERS.items() for bone in bones}
    names = {b["name"] for b in hessian_horseman_model().bones}
    for clip_name, body in hessian_horseman_animations()["animations"].items():
        mine = owner(clip_name.split(".")[-1])
        for bone in body["bones"]:
            if bone not in names:
                raise ValueError(f"{clip_name} keys {bone}, which the model lacks")
            if owned.get(bone, "body") != mine:
                raise ValueError(f"{clip_name} keys {bone}")
    rider_names = {b["name"] for b in phantom_rider_model().bones}
    for clip_name, body in phantom_rider_animations()["animations"].items():
        for bone in body["bones"]:
            if bone not in rider_names:
                raise ValueError(f"{clip_name} keys {bone}, which the phantom lacks")
    for entity, build in ANIMATIONS.items():
        have = {c.split(".")[-1] for c in build()["animations"]}
        if set(CLIPS[entity]) != have:
            raise ValueError(f"{entity} clips {sorted(set(CLIPS[entity]) ^ have)}")
    return True


if __name__ == "__main__":
    print(_check())
