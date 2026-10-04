"""The moves of each kind of arm (batch 43, docs/features/arms-motion.md), authored with tools/arms_motion.py.

Each kind has a hold pose (its guard between blows), a combo of attacks played in turn, and, where it has one, the
pose held while using it (the longsword's and rapier's parry). Two-handed kinds keep the off hand on the grip or haft
every frame ("two_handed": how far below the right hand, in pixels); the spear and the lance keep vanilla's arms
(its spear animations) and add the body: the lunge, the lean and the braced legs.

Time runs over vanilla's swing for that kind (tools/arms.py: KINDS[kind]["swing"], 4 to 24 ticks). A one-handed hit
lands the moment the player clicks, so the blow arrives early (about a quarter of the way in) and the rest is
follow-through and recovery; the short cock-back before it is the anticipation. A two-handed kind's blow lands later,
when its animation lands it (Arms III, batch 46): "blow" is that moment, as a share of the swing, and every one of its
attacks has a key there; tools/arms.py's TWO_HANDED strike is it in ticks, and the last attack is the finishing blow.
All original.

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
    "hold": GS_HOLD, "two_handed": 4, "fp_hold": GS_FP, "blow": 0.34,
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
    "hold": WH_HOLD, "two_handed": 9, "fp_hold": WH_FP, "blow": 0.36,
    "attacks": [
        fp(clip("swing", WH_HOLD, (0.18, WH["wind"]), (0.36, WH["swing"], 0.1), (0.54, WH["carry"]),
                (0.8, settle(WH["carry"], WH_HOLD))), WH_FP, *fp_sweep(WH_FP, 0.9)),
        fp(clip("slam", WH_HOLD, (0.18, WH["heave"]), (0.36, WH["slam"], 0.4), (0.5, WH["ground"], 0.2),
                (0.8, settle(WH["ground"], WH_HOLD))), WH_FP, *fp_overhead(WH_FP, 1.3)),
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
    "hold": GL_HOLD, "two_handed": 11, "fp_hold": GL_FP, "blow": 0.34,
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

# ================================================================ Arms II (batch 45)

# ---------------------------------------------------------------- dagger: low guard, quick stabs and a slash

LENGTH.update({"dagger": 11, "sabre": 19, "estoc": 22, "battle_axe": 20, "flail": 18, "scythe": 26, "quarterstaff": 30,
               "pike": 34})

DG_HOLD = P(body=(0, 16, 0), head=(0, -14, 0), right_arm=(-38, 2, 4), left_arm=(-28, 14, -10), item=(-48, 0, 0),
            **step(0.7, 6))
DG = {
    "coil": P(body=(-2, 22, 0), head=(0, -18, 0), right_arm=(-30, 14, 10, 0, 0, 2.5), left_arm=(-34, 12, -12),
              item=(-56, 0, 4), **step(0.5, 8)),
    "stab": P(body=(10, 2, 0, 0, 0.3, -1.0), head=(-8, 0, 0), right_arm=(-90, -6, 0, 0, 0, -2.5), left_arm=(-10, 16, -16),
              item=(-88, 0, 0), **step(1.8, 2)),
    "high_in": P(body=(-4, 26, 0), head=(0, -22, 0), right_arm=(-120, 30, 20), left_arm=(-30, 10, -12), item=(40, 0, 30),
                 **step(0.4, 8)),
    "slash": P(body=(6, -24, 0), head=(-2, 20, 0), right_arm=(-76, -36, -6), left_arm=(6, -10, -14), item=(-10, 0, -40),
               **step(1.2, -4)),
}
DG_FP = F(-30, 10, 4, -1, 0)
MOVES["dagger"] = {
    "hold": DG_HOLD, "fp_hold": DG_FP,
    "attacks": [
        fp(clip("stab", DG_HOLD, (0.12, DG["coil"]), (0.3, DG["stab"], 0.45), (0.58, settle(DG["stab"], DG_HOLD, 0.35))),
           DG_FP, *fp_thrust(DG_FP, 0.8)),
        fp(clip("slash", DG_HOLD, (0.14, DG["high_in"]), (0.32, DG["slash"], 0.2), (0.62, settle(DG["slash"], DG_HOLD))),
           DG_FP, *fp_forehand(DG_FP, 0.8, keys=3)),
    ],
}

# ---------------------------------------------------------------- sabre: a light cavalry blade, quick cuts both ways

SB_HOLD = P(body=(0, 10, 0), head=(0, -10, 0), right_arm=(-34, 8, 8), left_arm=(-6, 2, -6), item=(44, 0, 10))
SB = {
    "wind_r": P(body=(-2, 34, 0), head=(0, -28, 0), right_arm=(-146, 34, 26), left_arm=(-20, 18, -12), item=(64, 0, 24),
                **step(-0.4, 6)),
    "cut_l": P(body=(6, -30, 0), head=(-2, 24, 0), right_arm=(-80, -42, -8), left_arm=(10, -14, -16), item=(-4, 0, -44),
               **step(1.0, -6)),
    "follow_l": P(body=(8, -40, 0), head=(-2, 30, 0), right_arm=(-44, -56, -14), left_arm=(16, -16, -18), item=(-22, 0, -52),
                  **step(1.2, -8)),
    "wind_l": P(body=(0, -30, 0), head=(0, 24, 0), right_arm=(-44, -64, -30), left_arm=(-6, -10, -10), item=(14, 0, -74),
                **step(0.3, -4)),
    "cut_r": P(body=(4, 32, 0), head=(-2, -26, 0), right_arm=(-112, 42, 28), left_arm=(-18, 12, -12), item=(42, 0, 54),
               **step(0.9, 6)),
    "follow_r": P(body=(2, 42, 0), head=(0, -32, 0), right_arm=(-136, 58, 36), left_arm=(-22, 14, -12), item=(58, 0, 64),
                  **step(0.9, 8)),
}
SB_FP = F(0, 0, 12, -1, 1)
MOVES["sabre"] = {
    "hold": SB_HOLD, "fp_hold": SB_FP,
    "attacks": [
        fp(clip("forehand", SB_HOLD, (0.14, SB["wind_r"]), (0.3, SB["cut_l"], 0.15), (0.48, SB["follow_l"]),
                (0.74, settle(SB["follow_l"], SB_HOLD))), SB_FP, *fp_forehand(SB_FP)),
        fp(clip("backhand", SB_HOLD, (0.14, SB["wind_l"]), (0.3, SB["cut_r"], 0.15), (0.48, SB["follow_r"]),
                (0.74, settle(SB["follow_r"], SB_HOLD))), SB_FP, *fp_backhand(SB_FP)),
    ],
}

# ---------------------------------------------------------------- estoc: point forward, deep thrusts

ES_HOLD = P(body=(0, 20, 0), head=(0, -18, 0), right_arm=(-58, -4, 2), left_arm=(-40, 26, -16), item=(-44, 0, 0),
            **step(0.7, 6))
ES = {
    "draw": P(body=(-6, 26, 0), head=(0, -22, 0), right_arm=(-46, 16, 10, 0, 0, 3), left_arm=(-46, 24, -18), item=(-50, 0, 2),
              **step(0.2, 8)),
    "thrust": P(body=(14, 4, 0, 0, 0.4, -1.6), head=(-12, -2, 0), right_arm=(-94, -2, 0, 0, 0, -3), left_arm=(-14, 24, -30),
                item=(-88, 0, 0), right_leg=(-30, 6, 0, 0, 0, -3.5), left_leg=(20, 6, 0, 0, 0, 2)),
    "high_draw": P(body=(-8, 22, 0), head=(-6, -18, 0), right_arm=(-130, 20, 16, 0, 0, 2), left_arm=(-46, 24, -18),
                   item=(-40, 0, 6), **step(0.2, 8)),
    "high": P(body=(10, 6, 0, 0, 0.2, -1.0), head=(-4, -4, 0), right_arm=(-112, -4, 0, 0, 0, -3), left_arm=(-12, 20, -28),
              item=(-92, 0, 0), **step(1.8, 2)),
}
ES_FP = F(-38, 12, 4, -2, 1)
MOVES["estoc"] = {
    "hold": ES_HOLD, "fp_hold": ES_FP,
    "attacks": [
        fp(clip("thrust", ES_HOLD, (0.14, ES["draw"]), (0.32, ES["thrust"], 0.45), (0.6, settle(ES["thrust"], ES_HOLD, 0.3))),
           ES_FP, *fp_thrust(ES_FP, 1.3)),
        fp(clip("high_thrust", ES_HOLD, (0.14, ES["high_draw"]), (0.32, ES["high"], 0.45), (0.6, settle(ES["high"], ES_HOLD, 0.3))),
           ES_FP, *fp_thrust(ES_FP, 1.1)),
    ],
}

# ---------------------------------------------------------------- battle axe: two hands, chops from high and from the side

BA_HOLD = P(body=(0, 22, 0), head=(0, -20, 0), right_arm=(-36, 12, 10), item=(66, 0, -26), **step(0.6, 6))
BA = {
    "heave": P(body=(-14, 16, 0, 0, -0.4, 0.8), head=(-10, -12, 0), right_arm=(-176, 12, 10), item=(86, 0, -8), **step(-0.6, 4)),
    "chop": P(body=(26, 4, 0, 0, 1.0, -2.0), head=(14, -4, 0), right_arm=(-64, 4, 0), item=(-30, 0, 0), **step(2.4)),
    "bite": P(body=(30, 2, 0, 0, 1.1, -2.2), head=(18, -2, 0), right_arm=(-34, 2, 0), item=(-56, 0, 0), **step(2.6)),
    "wind": P(body=(-6, 54, 0), head=(0, -44, 0), right_arm=(-94, 68, 30), item=(48, 0, 50), **step(-0.8, 12)),
    "swing": P(body=(10, -32, 0), head=(-2, 26, 0), right_arm=(-82, -34, -4), item=(2, 0, -50), **step(2.0, -10)),
    "carry": P(body=(14, -48, 0), head=(-4, 38, 0), right_arm=(-56, -50, -10), item=(-18, 0, -60), **step(2.2, -12)),
}
BA_FP = F(0, 30, 24, -3, -2)
MOVES["battle_axe"] = {
    "hold": BA_HOLD, "two_handed": 9, "fp_hold": BA_FP, "blow": 0.36,
    "attacks": [
        fp(clip("swing", BA_HOLD, (0.18, BA["wind"]), (0.36, BA["swing"], 0.1), (0.54, BA["carry"]),
                (0.8, settle(BA["carry"], BA_HOLD))), BA_FP, *fp_sweep(BA_FP, 0.95)),
        fp(clip("chop", BA_HOLD, (0.18, BA["heave"]), (0.36, BA["chop"], 0.4), (0.5, BA["bite"], 0.2),
                (0.8, settle(BA["bite"], BA_HOLD))), BA_FP, *fp_overhead(BA_FP, 1.2)),
    ],
}

# ---------------------------------------------------------------- flail: the ball swung round overhead and brought down

FL_HOLD = P(body=(0, 12, 0), head=(0, -10, 0), right_arm=(-20, 6, 10), left_arm=(-12, 0, -8), item=(40, 0, 10))
FL = {
    "whirl": P(body=(-10, 30, 0), head=(-8, -22, 0), right_arm=(-172, 30, 26), left_arm=(-40, 12, -20), item=(90, 0, 30),
               **step(-0.6, 6)),
    "smash": P(body=(20, -8, 0, 0, 0.6, -1.0), head=(10, 6, 0), right_arm=(-60, -10, -2), left_arm=(12, -10, -18),
               item=(-50, 0, -10), **step(1.8)),
    "drag": P(body=(24, -14, 0, 0, 0.8, -1.2), head=(14, 8, 0), right_arm=(-26, -14, -6), left_arm=(16, -12, -20),
              item=(-80, 0, -10), **step(2.0)),
    "wind_r": P(body=(-2, 42, 0), head=(0, -36, 0), right_arm=(-98, 72, 32), left_arm=(-20, 20, -14), item=(50, 0, 70),
                **step(-0.4, 8)),
    "swing_l": P(body=(6, -32, 0), head=(-2, 26, 0), right_arm=(-82, -36, -6), left_arm=(10, -12, -16), item=(10, 0, -60),
                 **step(1.4, -6)),
}
FL_FP = F(0, 0, 10, 0, -1)
MOVES["flail"] = {
    "hold": FL_HOLD, "fp_hold": FL_FP,
    "attacks": [
        fp(clip("overhead", FL_HOLD, (0.18, FL["whirl"]), (0.34, FL["smash"], 0.35), (0.5, FL["drag"]),
                (0.78, settle(FL["drag"], FL_HOLD))), FL_FP, *fp_overhead(FL_FP, 0.9)),
        fp(clip("swing", FL_HOLD, (0.16, FL["wind_r"]), (0.32, FL["swing_l"], 0.15),
                (0.5, add(FL["swing_l"], P(body=(4, -14, 0), right_arm=(22, -18, -4)))),
                (0.76, settle(FL["swing_l"], FL_HOLD))), FL_FP, *fp_forehand(FL_FP, 1.1)),
    ],
}

# ---------------------------------------------------------------- scythe: two hands on the snath, long low reaping sweeps

SC_HOLD = P(body=(0, 28, 0), head=(0, -26, 0), right_arm=(-44, 18, 12), item=(50, 0, -24), **step(0.9, 8))
SC = {
    "wind": P(body=(-2, 62, 0), head=(0, -52, 0), right_arm=(-70, 76, 34), item=(20, 0, 64), **step(-0.4, 14)),
    "reap": P(body=(14, -28, 0, 0, 0.4, -0.6), head=(4, 24, 0), right_arm=(-50, -36, -8), item=(-20, 0, -60),
              **step(1.8, -10)),
    "through": P(body=(16, -52, 0, 0, 0.4, -0.8), head=(2, 40, 0), right_arm=(-36, -54, -12), item=(-30, 0, -70),
                 **step(2.0, -12)),
    "back_wind": P(body=(4, -44, 0), head=(0, 36, 0), right_arm=(-40, -60, -20), item=(-20, 0, -70), **step(1.2, -10)),
    "return": P(body=(10, 36, 0, 0, 0.3, -0.5), head=(2, -30, 0), right_arm=(-64, 50, 20), item=(10, 0, 60), **step(1.6, 10)),
}
SC_FP = F(-8, 30, 26, -3, -2)
MOVES["scythe"] = {
    "hold": SC_HOLD, "two_handed": 16, "fp_hold": SC_FP, "blow": 0.34,
    "attacks": [
        fp(clip("reap", SC_HOLD, (0.16, SC["wind"]), (0.34, SC["reap"], 0.1), (0.52, SC["through"]),
                (0.78, settle(SC["through"], SC_HOLD))), SC_FP, *fp_sweep(SC_FP, 1.2)),
        fp(clip("return", SC_HOLD, (0.16, SC["back_wind"]), (0.34, SC["return"], 0.1), (0.52, settle(SC["return"], SC_HOLD, 0.3)),
                (0.78, settle(SC["return"], SC_HOLD))), SC_FP, *fp_backhand(SC_FP, 1.1)),
    ],
}

# ---------------------------------------------------------------- quarterstaff: two hands wide apart, strikes and jabs

QS_HOLD = P(body=(0, 24, 0), head=(0, -22, 0), right_arm=(-48, 10, 8), item=(30, 0, -50), **step(0.8, 6))
QS = {
    "raise": P(body=(-8, 30, 0), head=(-6, -24, 0), right_arm=(-140, 24, 18), item=(60, 0, -40), **step(0.2, 8)),
    "strike": P(body=(16, -6, 0, 0, 0.5, -1.0), head=(8, 6, 0), right_arm=(-70, -10, -4), item=(-10, 0, -50), **step(1.8)),
    "draw": P(body=(-6, 30, 0), head=(0, -26, 0), right_arm=(-46, 28, 14, 0, 0, 3), item=(-40, 0, 0), **step(-0.2, 8)),
    "jab": P(body=(12, 6, 0, 0, 0.4, -1.2), head=(-8, -4, 0), right_arm=(-90, 0, 0, 0, 0, -3), item=(-84, 0, 0), **step(2.2, 2)),
    "wind": P(body=(-4, 50, 0), head=(0, -42, 0), right_arm=(-86, 64, 28), item=(30, 0, 40), **step(-0.4, 10)),
    "sweep": P(body=(8, -30, 0), head=(-2, 24, 0), right_arm=(-74, -32, -6), item=(-10, 0, -60), **step(1.6, -8)),
}
QS_FP = F(-10, 30, 40, -4, -3)
MOVES["quarterstaff"] = {
    "hold": QS_HOLD, "two_handed": 13, "fp_hold": QS_FP, "blow": 0.3,
    "attacks": [
        fp(clip("strike", QS_HOLD, (0.14, QS["raise"]), (0.3, QS["strike"], 0.3), (0.6, settle(QS["strike"], QS_HOLD))),
           QS_FP, *fp_forehand(QS_FP, 0.9, keys=3)),
        fp(clip("jab", QS_HOLD, (0.14, QS["draw"]), (0.3, QS["jab"], 0.45), (0.56, settle(QS["jab"], QS_HOLD, 0.35))),
           QS_FP, *fp_thrust(QS_FP, 1.0)),
        fp(clip("sweep", QS_HOLD, (0.14, QS["wind"]), (0.3, QS["sweep"], 0.1), (0.6, settle(QS["sweep"], QS_HOLD))),
           QS_FP, *fp_forehand(QS_FP, 1.0, keys=3)),
    ],
}

# ---------------------------------------------------------------- pike: two hands, levelled, long thrusts

PK_HOLD = P(body=(0, 26, 0), head=(0, -24, 0), right_arm=(-30, 14, 10), item=(-30, 0, 0), **step(0.9, 8))
PK = {
    "draw": P(body=(-6, 32, 0), head=(0, -28, 0), right_arm=(-26, 30, 16, 0, 0, 3), item=(-36, 0, 0), **step(0.0, 10)),
    "thrust": P(body=(16, 8, 0, 0, 0.5, -1.8), head=(-12, -6, 0), right_arm=(-80, 2, 0, 0, 0, -3), item=(-80, 0, 0),
                **step(2.8, 2)),
    "high_draw": P(body=(-8, 30, 0), head=(-6, -26, 0), right_arm=(-110, 26, 14, 0, 0, 2), item=(-56, 0, 0), **step(0.0, 10)),
    "high": P(body=(12, 10, 0, 0, 0.4, -1.4), head=(-6, -8, 0), right_arm=(-118, 0, 0, 0, 0, -3), item=(-90, 0, 0),
              **step(2.4, 2)),
}
PK_FP = F(-50, 20, 6, -3, -2)
MOVES["pike"] = {
    "hold": PK_HOLD, "two_handed": 14, "fp_hold": PK_FP, "blow": 0.32,
    "attacks": [
        fp(clip("thrust", PK_HOLD, (0.14, PK["draw"]), (0.32, PK["thrust"], 0.45), (0.62, settle(PK["thrust"], PK_HOLD, 0.35))),
           PK_FP, *fp_thrust(PK_FP, 1.4)),
        fp(clip("high_thrust", PK_HOLD, (0.14, PK["high_draw"]), (0.32, PK["high"], 0.45), (0.62, settle(PK["high"], PK_HOLD, 0.35))),
           PK_FP, *fp_thrust(PK_FP, 1.2)),
    ],
}

# ================================================================ Arms III (batch 46): two-handed arms
#
# Every one of these swings two-handed (tools/arms.py: TWO_HANDED): the blow lands at "blow", the last attack is the
# finishing blow, and the swings are long and heavy, so the wind-up reads before the blow.

LENGTH.update({"zweihander": 32, "maul": 19, "executioner": 26, "bill": 31})

# ---------------------------------------------------------------- zweihander: the widest two-handed cleaves

ZW_HOLD = P(body=(0, 20, 0), head=(0, -18, 0), right_arm=(-40, 12, 6), item=(52, 0, -10), **step(0.7, 5))
ZW = {
    "wind": P(body=(-4, 58, 0), head=(0, -48, 0), right_arm=(-116, 68, 32), item=(34, 0, 56), **step(-0.9, 10)),
    "sweep": P(body=(8, -40, 0), head=(-2, 32, 0), right_arm=(-82, -40, -4), item=(-12, 0, -54), **step(2.0, -10)),
    "follow": P(body=(12, -58, 0), head=(-4, 44, 0), right_arm=(-50, -60, -10), item=(-32, 0, -64), **step(2.2, -12)),
    "low": P(body=(10, -40, 0), head=(-2, 30, 0), right_arm=(-30, -50, -10), item=(-40, 0, -70), **step(1.0, -8)),
    "rise": P(body=(-2, 36, 0), head=(0, -28, 0), right_arm=(-140, 50, 28), item=(60, 0, 50), **step(1.6, 8)),
    "raise": P(body=(-12, 14, 0), head=(-10, -10, 0), right_arm=(-174, 10, 6), item=(84, 0, 0), **step(-0.5, 4)),
    "cleave": P(body=(24, 4, 0, 0, 0.9, -1.6), head=(12, -4, 0), right_arm=(-58, 4, 0), item=(-32, 0, 0), **step(2.6)),
    "ground": P(body=(28, 2, 0, 0, 1.1, -2.0), head=(16, -2, 0), right_arm=(-24, 2, 0), item=(-54, 0, 0), **step(2.8)),
}
ZW_FP = F(-4, 30, 20, -3, -1)
MOVES["zweihander"] = {
    "hold": ZW_HOLD, "two_handed": 5, "fp_hold": ZW_FP, "blow": 0.34,
    "attacks": [
        fp(clip("sweep", ZW_HOLD, (0.16, ZW["wind"]), (0.34, ZW["sweep"], 0.1), (0.52, ZW["follow"]),
                (0.78, settle(ZW["follow"], ZW_HOLD))), ZW_FP, *fp_sweep(ZW_FP, 1.1)),
        fp(clip("rising", ZW_HOLD, (0.16, ZW["low"]), (0.34, ZW["rise"], 0.2), (0.78, settle(ZW["rise"], ZW_HOLD))),
           ZW_FP, *fp_rising(ZW_FP)),
        fp(clip("cleave", ZW_HOLD, (0.16, ZW["raise"]), (0.34, ZW["cleave"], 0.35), (0.5, ZW["ground"]),
                (0.8, settle(ZW["ground"], ZW_HOLD))), ZW_FP, *fp_overhead(ZW_FP, 1.2)),
    ],
    # Holding use: the guard, the blade across the body, point up and to the left.
    "use": P(body=(0, -10, 0), head=(0, 8, 0), right_arm=(-78, -28, -10), item=(74, 0, -42)),
}

# ---------------------------------------------------------------- maul: the heaviest swings, a ground-shaking slam

MA_HOLD = P(body=(0, 24, 0), head=(0, -22, 0), right_arm=(-36, 14, 10), item=(76, 0, -34), **step(0.7, 6))
MA = {
    "wind": P(body=(-8, 60, 0), head=(0, -48, 0), right_arm=(-90, 74, 32), item=(54, 0, 54), **step(-0.9, 12)),
    "swing": P(body=(10, -36, 0), head=(-2, 30, 0), right_arm=(-84, -36, -4), item=(0, 0, -52), **step(2.2, -10)),
    "carry": P(body=(16, -54, 0), head=(-4, 42, 0), right_arm=(-54, -52, -10), item=(-20, 0, -62), **step(2.4, -12)),
    "heave": P(body=(-20, 16, 0, 0, -0.6, 1.0), head=(-14, -12, 0), right_arm=(-180, 12, 10), item=(90, 0, -6),
               **step(-0.8, 4)),
    "slam": P(body=(32, 2, 0, 0, 1.2, -2.2), head=(18, -2, 0), right_arm=(-60, 2, 0), item=(-36, 0, 0), **step(2.8)),
    "ground": P(body=(36, 0, 0, 0, 1.4, -2.6), head=(22, 0, 0), right_arm=(-28, 0, 0), item=(-64, 0, 0), **step(3.0)),
}
MA_FP = F(0, 30, 26, -3, -2)
MOVES["maul"] = {
    "hold": MA_HOLD, "two_handed": 9, "fp_hold": MA_FP, "blow": 0.36,
    "attacks": [
        fp(clip("swing", MA_HOLD, (0.18, MA["wind"]), (0.36, MA["swing"], 0.1), (0.54, MA["carry"]),
                (0.8, settle(MA["carry"], MA_HOLD))), MA_FP, *fp_sweep(MA_FP, 0.9)),
        fp(clip("slam", MA_HOLD, (0.18, MA["heave"]), (0.36, MA["slam"], 0.45), (0.5, MA["ground"], 0.25),
                (0.82, settle(MA["ground"], MA_HOLD))), MA_FP, *fp_overhead(MA_FP, 1.4)),
    ],
}

# ---------------------------------------------------------------- executioner's sword: a broad cut and a falling chop

EX_HOLD = P(body=(0, 16, 0), head=(0, -14, 0), right_arm=(-38, 8, 4), item=(50, 0, -6), **step(0.6, 4))
EX = {
    "wind": P(body=(-4, 54, 0), head=(0, -44, 0), right_arm=(-112, 64, 30), item=(32, 0, 52), **step(-0.8, 10)),
    "cut": P(body=(8, -34, 0), head=(-2, 28, 0), right_arm=(-82, -34, -4), item=(-10, 0, -50), **step(1.8, -10)),
    "follow": P(body=(12, -50, 0), head=(-4, 38, 0), right_arm=(-52, -54, -10), item=(-28, 0, -58), **step(2.0, -12)),
    "raise": P(body=(-12, 12, 0), head=(-10, -8, 0), right_arm=(-176, 8, 6), item=(86, 0, 0), **step(-0.4, 4)),
    "chop": P(body=(26, 2, 0, 0, 0.9, -1.7), head=(12, -2, 0), right_arm=(-62, 2, 0), item=(-28, 0, 0), **step(2.4)),
    "down": P(body=(30, 0, 0, 0, 1.1, -2.0), head=(16, 0, 0), right_arm=(-30, 0, 0), item=(-48, 0, 0), **step(2.6)),
}
EX_FP = F(-6, 30, 20, -3, -1)
MOVES["executioner"] = {
    "hold": EX_HOLD, "two_handed": 5, "fp_hold": EX_FP, "blow": 0.36,
    "attacks": [
        fp(clip("cut", EX_HOLD, (0.18, EX["wind"]), (0.36, EX["cut"], 0.15), (0.54, EX["follow"]),
                (0.8, settle(EX["follow"], EX_HOLD))), EX_FP, *fp_sweep(EX_FP, 0.95)),
        fp(clip("chop", EX_HOLD, (0.18, EX["raise"]), (0.36, EX["chop"], 0.4), (0.5, EX["down"], 0.2),
                (0.8, settle(EX["down"], EX_HOLD))), EX_FP, *fp_overhead(EX_FP, 1.2)),
    ],
}

# ---------------------------------------------------------------- bill: a hooking draw and an overhead chop

BL_HOLD = P(body=(0, 26, 0), head=(0, -24, 0), right_arm=(-48, 16, 10), item=(50, 0, -24), **step(0.8, 8))
BL = {
    "reach": P(body=(-4, 50, 0), head=(0, -42, 0), right_arm=(-96, 60, 26), item=(20, 0, 56), **step(-0.4, 10)),
    "hook": P(body=(8, -24, 0, 0, 0, 0.8), head=(-2, 20, 0), right_arm=(-60, -30, -6, 0, 0, 2), item=(-10, 0, -50),
              **step(0.4, -8)),
    "drag": P(body=(4, -36, 0, 0, 0, 1.2), head=(0, 30, 0), right_arm=(-40, -40, -8, 0, 0, 3), item=(-24, 0, -56),
              **step(-0.4, -10)),
    "raise": P(body=(-10, 16, 0), head=(-8, -12, 0), right_arm=(-168, 12, 8), item=(80, 0, -4), **step(-0.4, 6)),
    "chop": P(body=(22, 4, 0, 0, 0.8, -1.5), head=(10, -2, 0), right_arm=(-64, 4, 0), item=(-30, 0, 0), **step(2.2)),
    "down": P(body=(26, 2, 0, 0, 1.0, -1.8), head=(14, -2, 0), right_arm=(-32, 2, 0), item=(-50, 0, 0), **step(2.4)),
}
BL_FP = F(-10, 30, 30, -3, -1)
MOVES["bill"] = {
    "hold": BL_HOLD, "two_handed": 13, "fp_hold": BL_FP, "blow": 0.34,
    "attacks": [
        fp(clip("hook", BL_HOLD, (0.16, BL["reach"]), (0.34, BL["hook"], 0.2), (0.52, BL["drag"]),
                (0.78, settle(BL["drag"], BL_HOLD))), BL_FP, *fp_sweep(BL_FP, 0.8)),
        fp(clip("chop", BL_HOLD, (0.16, BL["raise"]), (0.34, BL["chop"], 0.3), (0.5, BL["down"]),
                (0.78, settle(BL["down"], BL_HOLD))), BL_FP, *fp_overhead(BL_FP, 1.1)),
    ],
}

# ================================================================ Arms IV (batch 47): the ornate arms
#
# The labrys, battleblade and war fork swing two-handed (tools/arms.py: TWO_HANDED, "blow" as there); the kama and war
# pick are one-handed and quick.

LENGTH.update({"labrys": 20, "battleblade": 27, "war_fork": 30, "kama": 12, "war_pick": 15})

# ---------------------------------------------------------------- labrys: a side swing, then a whirl right round

LB_HOLD = P(body=(0, 22, 0), head=(0, -20, 0), right_arm=(-38, 14, 10), item=(72, 0, -30), **step(0.7, 6))
LB = {
    "wind": P(body=(-6, 58, 0), head=(0, -46, 0), right_arm=(-92, 72, 30), item=(52, 0, 52), **step(-0.8, 12)),
    "swing": P(body=(10, -34, 0), head=(-2, 28, 0), right_arm=(-84, -34, -4), item=(0, 0, -50), **step(2.0, -10)),
    "carry": P(body=(14, -52, 0), head=(-4, 40, 0), right_arm=(-56, -50, -10), item=(-20, 0, -60), **step(2.2, -12)),
    # The whirl: wound far round to the right, then the axe carried flat all the way round to the far left.
    "coil": P(body=(-4, 64, 0), head=(0, -52, 0), right_arm=(-96, 80, 34), item=(40, 0, 66), **step(-1.0, 14)),
    "spin": P(body=(6, -60, 0), head=(-2, 48, 0), right_arm=(-90, -60, -8), item=(-6, 0, -70), **step(2.2, -14)),
    "round": P(body=(8, -64, 0), head=(0, 52, 0), right_arm=(-80, -72, -12), item=(-16, 0, -78), **step(2.4, -16)),
}
LB_FP = F(0, 30, 24, -3, -2)
MOVES["labrys"] = {
    "hold": LB_HOLD, "two_handed": 9, "fp_hold": LB_FP, "blow": 0.36,
    "attacks": [
        fp(clip("swing", LB_HOLD, (0.18, LB["wind"]), (0.36, LB["swing"], 0.1), (0.54, LB["carry"]),
                (0.8, settle(LB["carry"], LB_HOLD))), LB_FP, *fp_sweep(LB_FP, 0.95)),
        fp(clip("whirl", LB_HOLD, (0.18, LB["coil"]), (0.36, LB["spin"], 0.05), (0.56, LB["round"]),
                (0.84, settle(LB["round"], LB_HOLD))), LB_FP, *fp_sweep(LB_FP, 1.25)),
    ],
}

# ---------------------------------------------------------------- battleblade: a broad sweep and a falling cleave

BB_HOLD = P(body=(0, 18, 0), head=(0, -16, 0), right_arm=(-36, 10, 4), item=(48, 0, -12), **step(0.6, 4))
BB = {
    "wind": P(body=(-4, 54, 0), head=(0, -46, 0), right_arm=(-112, 66, 30), item=(32, 0, 52), **step(-0.8, 10)),
    "sweep": P(body=(8, -38, 0), head=(-2, 30, 0), right_arm=(-80, -38, -4), item=(-10, 0, -52), **step(1.9, -10)),
    "follow": P(body=(12, -56, 0), head=(-4, 42, 0), right_arm=(-50, -58, -10), item=(-30, 0, -62), **step(2.1, -12)),
    "raise": P(body=(-12, 14, 0), head=(-10, -10, 0), right_arm=(-174, 10, 6), item=(84, 0, 0), **step(-0.5, 4)),
    "cleave": P(body=(24, 4, 0, 0, 0.9, -1.6), head=(12, -4, 0), right_arm=(-58, 4, 0), item=(-32, 0, 0), **step(2.5)),
    "ground": P(body=(28, 2, 0, 0, 1.1, -2.0), head=(16, -2, 0), right_arm=(-24, 2, 0), item=(-54, 0, 0), **step(2.7)),
}
BB_FP = F(-6, 30, 22, -3, -1)
MOVES["battleblade"] = {
    "hold": BB_HOLD, "two_handed": 5, "fp_hold": BB_FP, "blow": 0.34,
    "attacks": [
        fp(clip("sweep", BB_HOLD, (0.16, BB["wind"]), (0.34, BB["sweep"], 0.1), (0.52, BB["follow"]),
                (0.78, settle(BB["follow"], BB_HOLD))), BB_FP, *fp_sweep(BB_FP, 1.05)),
        fp(clip("cleave", BB_HOLD, (0.16, BB["raise"]), (0.34, BB["cleave"], 0.35), (0.5, BB["ground"]),
                (0.8, settle(BB["ground"], BB_HOLD))), BB_FP, *fp_overhead(BB_FP, 1.2)),
    ],
}

# ---------------------------------------------------------------- war fork: levelled and braced, a thrust and a driving thrust

WF_HOLD = P(body=(0, 24, 0), head=(0, -22, 0), right_arm=(-34, 14, 10), item=(-28, 0, 0), **step(1.0, 8))
WF = {
    "draw": P(body=(-6, 30, 0), head=(0, -26, 0), right_arm=(-30, 30, 16, 0, 0, 3), item=(-34, 0, 0), **step(0.1, 10)),
    "thrust": P(body=(16, 8, 0, 0, 0.5, -1.8), head=(-12, -6, 0), right_arm=(-82, 2, 0, 0, 0, -3), item=(-82, 0, 0),
                **step(2.8, 2)),
    "set": P(body=(6, 26, 0, 0, 0.6, 0.4), head=(-4, -22, 0), right_arm=(-40, 24, 14, 0, 0, 2), item=(-40, 0, 0),
             **step(1.6, 12)),
    "drive": P(body=(20, 4, 0, 0, 0.8, -2.2), head=(-14, -4, 0), right_arm=(-90, 0, 0, 0, 0, -4), item=(-88, 0, 0),
               **step(3.0, 0)),
}
WF_FP = F(-50, 20, 6, -3, -2)
MOVES["war_fork"] = {
    "hold": WF_HOLD, "two_handed": 13, "fp_hold": WF_FP, "blow": 0.34,
    "attacks": [
        fp(clip("thrust", WF_HOLD, (0.16, WF["draw"]), (0.34, WF["thrust"], 0.45), (0.62, settle(WF["thrust"], WF_HOLD, 0.35))),
           WF_FP, *fp_thrust(WF_FP, 1.35)),
        fp(clip("drive", WF_HOLD, (0.16, WF["set"]), (0.34, WF["drive"], 0.5), (0.64, settle(WF["drive"], WF_HOLD, 0.35))),
           WF_FP, *fp_thrust(WF_FP, 1.5)),
    ],
}

# ---------------------------------------------------------------- kama: quick hooking cuts, forehand and back

SK_HOLD = P(body=(0, 12, 0), head=(0, -12, 0), right_arm=(-40, 8, 10), left_arm=(-8, 2, -6), item=(30, 0, 30))
SK = {
    "wind_r": P(body=(-2, 34, 0), head=(0, -28, 0), right_arm=(-140, 34, 26), left_arm=(-20, 18, -12), item=(70, 0, 40),
                **step(-0.3, 6)),
    "hook_l": P(body=(8, -28, 0), head=(-2, 22, 0), right_arm=(-76, -40, -8, 0, 0, 1), left_arm=(12, -14, -16), item=(0, 0, -30),
                **step(1.0, -6)),
    "draw_l": P(body=(6, -36, 0), head=(-2, 28, 0), right_arm=(-40, -50, -14, 0, 0, 2), left_arm=(16, -16, -18), item=(-20, 0, -40),
                **step(0.6, -8)),
    "wind_l": P(body=(0, -28, 0), head=(0, 22, 0), right_arm=(-48, -60, -30), left_arm=(-6, -10, -10), item=(20, 0, -60),
                **step(0.3, -4)),
    "hook_r": P(body=(4, 30, 0), head=(-2, -24, 0), right_arm=(-110, 40, 26, 0, 0, 1), left_arm=(-18, 12, -12), item=(50, 0, 50),
                **step(0.9, 6)),
}
SK_FP = F(0, 0, 20, -1, 1)
MOVES["kama"] = {
    "hold": SK_HOLD, "fp_hold": SK_FP,
    "attacks": [
        fp(clip("hook", SK_HOLD, (0.12, SK["wind_r"]), (0.28, SK["hook_l"], 0.2), (0.46, SK["draw_l"]),
                (0.72, settle(SK["draw_l"], SK_HOLD))), SK_FP, *fp_forehand(SK_FP, 0.8)),
        fp(clip("backhook", SK_HOLD, (0.12, SK["wind_l"]), (0.28, SK["hook_r"], 0.2), (0.62, settle(SK["hook_r"], SK_HOLD))),
           SK_FP, *fp_forehand(SK_FP, 0.7, keys=3)),
    ],
}

# ---------------------------------------------------------------- war pick: a downward peck and a side swing

WP_HOLD = P(body=(0, 10, 0), head=(0, -10, 0), right_arm=(-26, 4, 6), left_arm=(-10, 0, -8), item=(56, 0, 6))
WP = {
    "raise": P(body=(-8, 24, 0), head=(-6, -18, 0), right_arm=(-168, 20, 22), left_arm=(-40, 10, -20), item=(76, 0, 10),
               **step(-0.5, 6)),
    "peck": P(body=(18, -6, 0, 0, 0.6, -1), head=(8, 4, 0), right_arm=(-66, -6, 0), left_arm=(10, -10, -18), item=(-36, 0, 0),
              **step(1.7)),
    "low": P(body=(20, -8, 0, 0, 0.7, -1.1), head=(10, 6, 0), right_arm=(-36, -8, -4), left_arm=(14, -10, -20), item=(-62, 0, 0),
             **step(1.9)),
    "wind_r": P(body=(-2, 38, 0), head=(0, -32, 0), right_arm=(-96, 68, 30), left_arm=(-20, 20, -14), item=(42, 0, 60),
                **step(-0.4, 8)),
    "swing_l": P(body=(6, -30, 0), head=(-2, 24, 0), right_arm=(-84, -34, -6), left_arm=(10, -12, -16), item=(12, 0, -40),
                 **step(1.3, -6)),
}
WP_FP = F(0, 0, 10, 0, 0)
MOVES["war_pick"] = {
    "hold": WP_HOLD, "fp_hold": WP_FP,
    "attacks": [
        fp(clip("peck", WP_HOLD, (0.16, WP["raise"]), (0.32, WP["peck"], 0.35), (0.48, WP["low"]),
                (0.76, settle(WP["low"], WP_HOLD))), WP_FP, *fp_overhead(WP_FP, 0.8)),
        fp(clip("swing", WP_HOLD, (0.16, WP["wind_r"]), (0.32, WP["swing_l"], 0.15),
                (0.5, add(WP["swing_l"], P(body=(4, -12, 0), right_arm=(20, -16, -4)))),
                (0.76, settle(WP["swing_l"], WP_HOLD))), WP_FP, *fp_forehand(WP_FP, 1.0)),
    ],
}
