"""The moves of each kind of arm (batch 43, docs/features/arms-motion.md), authored with tools/arms_motion.py.

Each kind has a hold pose (its guard between blows), a combo of attacks played in turn, and, where it has one, the
pose held while using it (the longsword's and rapier's parry). Two-handed kinds keep the off hand on the grip or haft
every frame ("two_handed": how far below the right hand, in pixels); the spear and the lance keep vanilla's arms
(its spear animations) and add the body: the lunge, the lean and the braced legs.

Time runs over vanilla's swing for that kind (tools/arms.py: KINDS[kind]["swing"], 5 to 12 ticks). A hit lands the
moment the player clicks, so the blow arrives early (about a quarter of the way in) and the rest is follow-through and
recovery; the short cock-back before it is the anticipation. All original.

Bones: P(body=..., head=..., right_arm=..., left_arm=..., right_leg=..., left_leg=..., item=...), each (rx, ry, rz)
or (rx, ry, rz, x, y, z) in degrees and pixels. See tools/arms_motion.py for the axes; the item bone is the wrist (rx
above 0 tips the blade up, rz swings it sideways).
"""
from arms_motion import Clip, P, add, mix

# How long each arm is from the hand to its tip, in pixels (the preview draws it).
LENGTH = {"longsword": 20, "greatsword": 27, "rapier": 19, "flanged_mace": 14, "war_hammer": 16, "glaive": 29,
          "halberd": 30, "spear": 26, "lance": 32}

MOVES = {}


def clip(name, hold, *keys):
    """A clip from the hold pose through the keys (time, pose[, tension]) and back to the hold pose."""
    full = [(0.0, hold, 0.0)]
    for key in keys:
        tension = key[2] if len(key) > 2 else 0.0
        full.append((key[0], key[1], tension))
    full.append((1.0, hold, 0.0))
    return Clip(name, full)


def step(forward, side=0.0):
    """Feet for a step: the right foot forward by `forward` pixels (back if below 0), the left the other way."""
    return dict(right_leg=(-forward * 3.0, side, 0, 0, 0, -forward), left_leg=(forward * 2.5, side, 0, 0, 0, forward * 0.8))


def settle(a, hold, f=0.6):
    return mix(a, hold, f)


# ---------------------------------------------------------------- first person
#
# On screen only the held arm moves (vanilla draws no arm with an item in the hand). A first-person pose is
# F(rx, ry, rz, x, y, z): the arm turned about the hand (degrees, ModelPart's Z*Y*X order) and moved (pixels; x to the
# right, y up, z towards the eye), on top of where vanilla places the hand. rx below 0 tips the point away, into the
# screen; rz above 0 turns it anticlockwise; ry above 0 turns the point to the left. Each attack's on-screen keys share
# its third-person timing, so the blow crosses the crosshair at the moment the hit lands.


def F(rx=0.0, ry=0.0, rz=0.0, x=0.0, y=0.0, z=0.0):
    return (float(rx), float(ry), float(rz), float(x), float(y), float(z))


def fp(c, hold, *poses):
    """Gives clip c its first-person keys: one pose per inner key of the clip (same times), F(...) or (F(...), tension);
    a key's tension defaults to its third-person key's."""
    inner = c.keys[1:-1]
    assert len(poses) == len(inner), (c.name, len(poses), len(inner))
    keys = [(0.0, hold, 0.0)]
    for (t, _pose, tension), pose in zip(inner, poses):
        if isinstance(pose[0], tuple):
            pose, tension = pose
        keys.append((t, pose, tension))
    c.fp = keys + [(1.0, hold, 0.0)]
    return c


def scaled(pose, a):
    return tuple(v * a for v in pose)


def fsettle(a, hold, f=0.6):
    return tuple(x + (y - x) * f for x, y in zip(a, hold))


# The on-screen strokes, shared by the kinds (a: how big; the heavier arms swing wider and further down).
def fp_forehand(hold, a=1.0, keys=4):
    """Right to left and down: up past the right shoulder, across the crosshair, out low to the left."""
    wind, cut, follow = F(4, -6, -16, -2, 6), F(-20, 30, 60, -8, 4, -3), F(-30, 40, 84, -13, 0, -2)
    wind, cut, follow = scaled(wind, a), scaled(cut, a), scaled(follow, a)
    return [wind, cut, follow, fsettle(follow, hold)] if keys == 4 else [wind, cut, fsettle(cut, hold, 0.4)]


def fp_backhand(hold, a=1.0):
    """Left to right: drawn up to the left, swept back across the crosshair, out low to the right."""
    wind, cut, follow = F(6, 30, 50, -10, 5), F(-20, 40, -30, -2, 3, -3), F(-30, 40, -60, 2, -3, -2)
    follow = scaled(follow, a)
    return [scaled(wind, a), scaled(cut, a), follow, fsettle(follow, hold)]


def fp_thrust(hold, a=1.0):
    """Drawn back towards the eye, then driven point first at the crosshair."""
    draw, thrust = F(-50, 10, 4, 1, -1, 4), F(-80, 24, 0, -6, 3, -6)
    thrust = (thrust[0], thrust[1], thrust[2], thrust[3] * a, thrust[4], thrust[5] * a)
    return [draw, thrust, fsettle(thrust, hold, 0.35)]


def fp_overhead(hold, a=1.0):
    """Raised high over the head, brought straight down through the crosshair, to the ground."""
    raise_, smash, ground = F(15, 20, 15, -3, 10), F(-50, 40, 10, -5, 2, -4), F(-90, 40, 10, -5, -6 * a, -3)
    return [raise_, smash, ground, fsettle(ground, hold)]


def fp_sweep(hold, a=1.0):
    """A wide, flat sweep from the right to the far left."""
    wind, sweep, wide = F(4, 0, -22, -2, 5), F(-30, 40, 80, -8, 2, -3), F(-30, 50, 100, -16 * a, -1, -2)
    return [wind, sweep, wide, fsettle(wide, hold)]


def fp_rising(hold):
    """Dropped low to the left, then swept up across to the right."""
    low, rise = F(-30, 30, 80, -10, -6), F(0, 20, -12, -3, 5)
    return [low, rise, fsettle(rise, hold)]


# ---------------------------------------------------------------- longsword: one hand, guard with the blade up

LS_HOLD = P(body=(0, 8, 0), head=(0, -8, 0), right_arm=(-30, 6, 6), left_arm=(-8, 4, -6), item=(36, 0, 6))

LS = {
    "wind_r": P(body=(-2, 36, 0), head=(0, -30, 0), right_arm=(-150, 30, 24), left_arm=(-26, 20, -14), item=(60, 0, 20),
                **step(-0.6, 6)),
    "cut_l": P(body=(6, -32, 0), head=(-2, 26, 0), right_arm=(-84, -40, -6), left_arm=(12, -16, -16), item=(-6, 0, -40),
               **step(1.2, -6)),
    "follow_l": P(body=(10, -42, 0), head=(-2, 32, 0), right_arm=(-40, -54, -14), left_arm=(18, -18, -18), item=(-24, 0, -48),
                  **step(1.4, -8)),
    "wind_l": P(body=(0, -28, 0), head=(0, 24, 0), right_arm=(-40, -62, -28), left_arm=(-6, -10, -10), item=(10, 0, -70),
                **step(0.4, -4)),
    "cut_r": P(body=(4, 30, 0), head=(-2, -24, 0), right_arm=(-118, 40, 26), left_arm=(-20, 12, -12), item=(40, 0, 50),
               **step(1.0, 6)),
    "follow_r": P(body=(2, 40, 0), head=(0, -30, 0), right_arm=(-140, 56, 34), left_arm=(-24, 14, -12), item=(56, 0, 60),
                  **step(1.0, 8)),
    "draw": P(body=(-6, 18, 0), head=(0, -14, 0), right_arm=(-40, 26, 12, 0, 0, 3), left_arm=(-34, -8, -10), item=(-50, 0, 0),
              **step(-0.4)),
    "thrust": P(body=(12, -4, 0, 0, 0.4, -1), head=(-10, 4, 0), right_arm=(-92, -2, 0, 0, 0, -3), left_arm=(16, -6, -24),
                item=(-86, 0, 0), **step(2.2)),
}
LS_FP = F(rz=10, x=-1, y=1)
MOVES["longsword"] = {
    "hold": LS_HOLD, "fp_hold": LS_FP,
    "attacks": [
        fp(clip("forehand", LS_HOLD, (0.14, LS["wind_r"]), (0.3, LS["cut_l"], 0.15), (0.48, LS["follow_l"]),
                (0.74, settle(LS["follow_l"], LS_HOLD))), LS_FP, *fp_forehand(LS_FP)),
        fp(clip("backhand", LS_HOLD, (0.14, LS["wind_l"]), (0.3, LS["cut_r"], 0.15), (0.48, LS["follow_r"]),
                (0.74, settle(LS["follow_r"], LS_HOLD))), LS_FP, *fp_backhand(LS_FP)),
        fp(clip("thrust", LS_HOLD, (0.12, LS["draw"]), (0.26, LS["thrust"], 0.4), (0.52, settle(LS["thrust"], LS_HOLD, 0.35))),
           LS_FP, *fp_thrust(LS_FP)),
    ],
    # Holding use: the blade across the body, point up and to the left, the off hand behind the blade.
    "use": P(body=(0, -12, 0), head=(0, 10, 0), right_arm=(-76, -30, -10), left_arm=(-60, -24, 16), item=(70, 0, -40)),
}

# ---------------------------------------------------------------- greatsword: two hands, big committed swings

GS_HOLD = P(body=(0, 18, 0), head=(0, -16, 0), right_arm=(-34, 10, 4), item=(44, 0, -14), **step(0.6, 4))
GS = {
    "wind": P(body=(-4, 52, 0), head=(0, -44, 0), right_arm=(-110, 64, 30), item=(30, 0, 50), **step(-0.8, 10)),
    "sweep": P(body=(8, -36, 0), head=(-2, 30, 0), right_arm=(-80, -36, -4), item=(-10, 0, -50), **step(1.8, -10)),
    "follow": P(body=(12, -54, 0), head=(-4, 40, 0), right_arm=(-50, -56, -10), item=(-30, 0, -60), **step(2.0, -12)),
    "raise": P(body=(-10, 14, 0), head=(-8, -10, 0), right_arm=(-170, 10, 6), item=(80, 0, 0), **step(-0.4, 4)),
    "cleave": P(body=(22, 4, 0, 0, 0.8, -1.5), head=(10, -4, 0), right_arm=(-60, 4, 0), item=(-30, 0, 0), **step(2.4)),
    "ground": P(body=(26, 2, 0, 0, 1.0, -1.8), head=(14, -2, 0), right_arm=(-26, 2, 0), item=(-50, 0, 0), **step(2.6)),
}
GS_FP = F(-6, 30, 22, -3, -1)
MOVES["greatsword"] = {
    "hold": GS_HOLD, "two_handed": 4, "fp_hold": GS_FP,
    "attacks": [
        fp(clip("sweep", GS_HOLD, (0.16, GS["wind"]), (0.34, GS["sweep"], 0.1), (0.52, GS["follow"]),
                (0.78, settle(GS["follow"], GS_HOLD))), GS_FP, *fp_sweep(GS_FP)),
        fp(clip("cleave", GS_HOLD, (0.16, GS["raise"]), (0.34, GS["cleave"], 0.3), (0.5, GS["ground"]),
                (0.78, settle(GS["ground"], GS_HOLD))), GS_FP, *fp_overhead(GS_FP)),
    ],
}

# ---------------------------------------------------------------- rapier: en garde, lunges and quick cuts

RP_HOLD = P(body=(0, 22, 0), head=(0, -20, 0), right_arm=(-62, -6, 0), left_arm=(-150, 40, -30), item=(-40, 0, 0),
            **step(0.8, 8))
RP = {
    "coil": P(body=(-4, 28, 0), head=(0, -24, 0), right_arm=(-50, 10, 8, 0, 0, 2), left_arm=(-160, 44, -34), item=(-46, 0, 4),
              **step(0.4, 10)),
    "lunge": P(body=(14, 6, 0, 0, 0.4, -1.6), head=(-12, -4, 0), right_arm=(-94, -4, 0, 0, 0, -3), left_arm=(-10, 30, -40),
               item=(-86, 0, 0), right_leg=(-34, 8, 0, 0, 0, -4), left_leg=(22, 6, 0, 0, 0, 2.5)),
    "flick_in": P(body=(0, 30, 0), head=(0, -26, 0), right_arm=(-90, 34, 12), left_arm=(-150, 40, -30), item=(10, 0, 40),
                  **step(0.8, 8)),
    "flick": P(body=(4, -6, 0), head=(0, 6, 0), right_arm=(-84, -32, -6), left_arm=(-140, 36, -26), item=(-6, 0, -50),
               **step(1.2, 4)),
}
RP_FP = F(-40, 12, 4, -2, 1)
MOVES["rapier"] = {
    "hold": RP_HOLD, "fp_hold": RP_FP,
    "attacks": [
        fp(clip("lunge", RP_HOLD, (0.12, RP["coil"]), (0.3, RP["lunge"], 0.45), (0.56, settle(RP["lunge"], RP_HOLD, 0.3))),
           RP_FP, *fp_thrust(RP_FP, 1.3)),
        fp(clip("cut", RP_HOLD, (0.14, RP["flick_in"]), (0.3, RP["flick"], 0.2), (0.6, settle(RP["flick"], RP_HOLD))),
           RP_FP, *fp_forehand(RP_FP, 0.8, keys=3)),
    ],
    "use": P(body=(0, 10, 0), head=(0, -8, 0), right_arm=(-84, -20, -4), left_arm=(-150, 40, -30), item=(60, 0, -30),
             **step(0.8, 8)),
}

# ---------------------------------------------------------------- flanged mace: one hand, crushing blows

FM_HOLD = P(body=(0, 10, 0), head=(0, -10, 0), right_arm=(-24, 4, 6), left_arm=(-10, 0, -8), item=(54, 0, 4))
FM = {
    "raise": P(body=(-8, 26, 0), head=(-6, -20, 0), right_arm=(-170, 20, 22), left_arm=(-40, 10, -20), item=(70, 0, 10),
               **step(-0.6, 6)),
    "smash": P(body=(18, -6, 0, 0, 0.6, -1), head=(8, 4, 0), right_arm=(-64, -6, 0), left_arm=(10, -10, -18), item=(-40, 0, 0),
               **step(1.8)),
    "low": P(body=(22, -10, 0, 0, 0.8, -1.2), head=(12, 6, 0), right_arm=(-30, -10, -4), left_arm=(16, -10, -20), item=(-70, 0, 0),
             **step(2.0)),
    "wind_r": P(body=(-2, 40, 0), head=(0, -34, 0), right_arm=(-96, 70, 30), left_arm=(-20, 20, -14), item=(40, 0, 60),
                **step(-0.4, 8)),
    "swing_l": P(body=(6, -30, 0), head=(-2, 24, 0), right_arm=(-84, -34, -6), left_arm=(10, -12, -16), item=(10, 0, -40),
                 **step(1.4, -6)),
}
FM_FP = F(0, 0, 8, 0, 0)
MOVES["flanged_mace"] = {
    "hold": FM_HOLD, "fp_hold": FM_FP,
    "attacks": [
        fp(clip("smash", FM_HOLD, (0.16, FM["raise"]), (0.32, FM["smash"], 0.35), (0.48, FM["low"]),
                (0.76, settle(FM["low"], FM_HOLD))), FM_FP, *fp_overhead(FM_FP, 0.8)),
        fp(clip("swing", FM_HOLD, (0.16, FM["wind_r"]), (0.32, FM["swing_l"], 0.15),
                (0.5, add(FM["swing_l"], P(body=(4, -12, 0), right_arm=(20, -16, -4)))),
                (0.76, settle(FM["swing_l"], FM_HOLD))), FM_FP, *fp_forehand(FM_FP, 1.05)),
    ],
}

# ---------------------------------------------------------------- war hammer: two hands, the heaviest blows

WH_HOLD = P(body=(0, 24, 0), head=(0, -22, 0), right_arm=(-40, 14, 10), item=(70, 0, -30), **step(0.6, 6))
WH = {
    "heave": P(body=(-16, 18, 0, 0, -0.4, 0.8), head=(-12, -14, 0), right_arm=(-178, 14, 10), item=(84, 0, -6), **step(-0.6, 4)),
    "slam": P(body=(28, 2, 0, 0, 1.0, -2.0), head=(16, -2, 0), right_arm=(-62, 2, 0), item=(-34, 0, 0), **step(2.6)),
    "ground": P(body=(32, 0, 0, 0, 1.2, -2.4), head=(20, 0, 0), right_arm=(-30, 0, 0), item=(-60, 0, 0), **step(2.8)),
    "wind": P(body=(-6, 56, 0), head=(0, -46, 0), right_arm=(-96, 70, 30), item=(50, 0, 50), **step(-0.8, 12)),
    "swing": P(body=(10, -34, 0), head=(-2, 28, 0), right_arm=(-84, -34, -4), item=(0, 0, -50), **step(2.0, -10)),
    "carry": P(body=(14, -50, 0), head=(-4, 40, 0), right_arm=(-56, -50, -10), item=(-20, 0, -60), **step(2.2, -12)),
}
WH_FP = F(0, 30, 24, -3, -2)
MOVES["war_hammer"] = {
    "hold": WH_HOLD, "two_handed": 9, "fp_hold": WH_FP,
    "attacks": [
        fp(clip("slam", WH_HOLD, (0.18, WH["heave"]), (0.36, WH["slam"], 0.4), (0.5, WH["ground"], 0.2),
                (0.8, settle(WH["ground"], WH_HOLD))), WH_FP, *fp_overhead(WH_FP, 1.3)),
        fp(clip("swing", WH_HOLD, (0.18, WH["wind"]), (0.36, WH["swing"], 0.1), (0.54, WH["carry"]),
                (0.8, settle(WH["carry"], WH_HOLD))), WH_FP, *fp_sweep(WH_FP, 0.9)),
    ],
}

# ---------------------------------------------------------------- glaive: two hands on the haft, wide sweeps

GL_HOLD = P(body=(0, 26, 0), head=(0, -24, 0), right_arm=(-50, 16, 10), item=(56, 0, -20), **step(0.8, 8))
GL = {
    "wind": P(body=(-4, 60, 0), head=(0, -50, 0), right_arm=(-84, 74, 32), item=(30, 0, 60), **step(-0.6, 12)),
    "sweep": P(body=(6, -30, 0), head=(0, 26, 0), right_arm=(-70, -34, -6), item=(-6, 0, -56), **step(1.8, -10)),
    "wide": P(body=(10, -52, 0), head=(-2, 40, 0), right_arm=(-50, -52, -10), item=(-20, 0, -66), **step(2.0, -12)),
    "low": P(body=(12, -20, 0), head=(6, 16, 0), right_arm=(-30, -30, -10), item=(-30, 0, -40), **step(1.2, -6)),
    "rise": P(body=(-6, 34, 0), head=(-10, -28, 0), right_arm=(-150, 40, 20), item=(70, 0, 40), **step(1.6, 8)),
}
GL_FP = F(-8, 30, 26, -3, -2)
MOVES["glaive"] = {
    "hold": GL_HOLD, "two_handed": 11, "fp_hold": GL_FP,
    "attacks": [
        fp(clip("sweep", GL_HOLD, (0.16, GL["wind"]), (0.34, GL["sweep"], 0.1), (0.52, GL["wide"]),
                (0.78, settle(GL["wide"], GL_HOLD))), GL_FP, *fp_sweep(GL_FP, 1.15)),
        fp(clip("rising", GL_HOLD, (0.16, GL["low"]), (0.34, GL["rise"], 0.2), (0.78, settle(GL["rise"], GL_HOLD))),
           GL_FP, *fp_rising(GL_FP)),
    ],
}

# ---------------------------------------------------------------- halberd: two hands, thrusts and chops

HB_HOLD = P(body=(0, 24, 0), head=(0, -22, 0), right_arm=(-46, 12, 8), item=(64, 0, -16), **step(0.8, 6))
HB = {
    "draw": P(body=(-6, 30, 0), head=(0, -26, 0), right_arm=(-50, 30, 14, 0, 0, 3), item=(-50, 0, 0), **step(-0.2, 8)),
    "thrust": P(body=(14, 6, 0, 0, 0.4, -1.4), head=(-10, -4, 0), right_arm=(-92, 0, 0, 0, 0, -3), item=(-86, 0, 0), **step(2.6, 2)),
    "raise": P(body=(-12, 16, 0), head=(-10, -12, 0), right_arm=(-176, 14, 8), item=(80, 0, -10), **step(-0.4, 4)),
    "chop": P(body=(24, 4, 0, 0, 0.8, -1.6), head=(12, -4, 0), right_arm=(-70, 4, 0), item=(-30, 0, 0), **step(2.4)),
    "down": P(body=(28, 2, 0, 0, 1.0, -2.0), head=(16, -2, 0), right_arm=(-40, 2, 0), item=(-56, 0, 0), **step(2.6)),
}
HB_FP = F(-8, 30, 24, -3, -2)
MOVES["halberd"] = {
    "hold": HB_HOLD, "two_handed": 12, "fp_hold": HB_FP,
    "attacks": [
        fp(clip("thrust", HB_HOLD, (0.14, HB["draw"]), (0.32, HB["thrust"], 0.45), (0.6, settle(HB["thrust"], HB_HOLD, 0.35))),
           HB_FP, *fp_thrust(HB_FP, 1.2)),
        fp(clip("chop", HB_HOLD, (0.18, HB["raise"]), (0.36, HB["chop"], 0.35), (0.5, HB["down"]),
                (0.8, settle(HB["down"], HB_HOLD))), HB_FP, *fp_overhead(HB_FP, 1.2)),
    ],
}

# ---------------------------------------------------------------- spear and lance: vanilla's arms, the body added

SP_HOLD = P(body=(0, 10, 0), head=(0, -8, 0), **step(0.6, 4))
MOVES["spear"] = {
    "hold": SP_HOLD, "body_only": True,
    "attacks": [clip("jab", SP_HOLD, (0.14, P(body=(-4, 16, 0), head=(0, -12, 0), **step(0.2, 6))),
                     (0.32, P(body=(16, 2, 0, 0, 0.4, -1.4), head=(-12, 0, 0), **step(2.6)), 0.4),
                     (0.6, P(body=(8, 6, 0), head=(-6, -4, 0), **step(1.6, 2))))],
    "use": P(body=(12, 6, 0, 0, 0.3, -1.0), head=(-10, -4, 0), **step(2.0, 2)),
}
LN_HOLD = P(body=(0, 12, 0), head=(0, -10, 0), **step(0.6, 4))
MOVES["lance"] = {
    "hold": LN_HOLD, "body_only": True,
    "attacks": [clip("jab", LN_HOLD, (0.16, P(body=(-6, 18, 0), head=(0, -14, 0), **step(-0.2, 6))),
                     (0.36, P(body=(20, 0, 0, 0, 0.6, -1.8), head=(-16, 0, 0), **step(3.0)), 0.4),
                     (0.64, P(body=(10, 6, 0), head=(-8, -4, 0), **step(1.8, 2))))],
    # Couched for the charge: leaning into it, braced.
    "use": P(body=(18, 4, 0, 0, 0.5, -1.6), head=(-14, -2, 0), **step(2.4, 2)),
}
