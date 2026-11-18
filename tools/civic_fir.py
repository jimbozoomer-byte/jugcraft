"""Main-square Christmas fir. Coordinates are relative to the centre of the existing plinth."""
from functools import lru_cache
from math import atan2, cos, floor, hypot, sin, tau


def slot(x, y, z):
    # Historical saves identify every position in the original 7 x 7 x 13 scene by this number.
    if abs(x) <= 3 and abs(z) <= 3 and y <= 13:
        return x + 3 + 7 * (z + 3) + 49 * (y - 1)
    return 637 + (y - 1) * 361 + (z + 9) * 19 + x + 9


@lru_cache(maxsize=1)
def blocks():
    out = {}
    leaf = "minecraft:spruce_leaves[persistent=true]"
    log = "minecraft:spruce_log"
    # Each whorl extends out and down from the trunk. Shift the spokes between successive skirts
    # so there are glimpses of the trunk and sky between the scalloped, needle-heavy branch tips.
    tiers = [(6, 9, 7), (9, 8, 8), (12, 7, 7), (15, 6, 6), (18, 5, 7),
             (21, 4, 5), (23, 3, 5), (25, 2, 4)]
    for i, (base, reach, spokes) in enumerate(tiers):
        for x in range(-reach, reach + 1):
            for z in range(-reach, reach + 1):
                r, a = hypot(x, z), atan2(z, x)
                lobe = (1 + cos(spokes * a + i * 2.25)) / 2
                edge = reach * (.76 + .24 * lobe)
                if r > edge + .15:
                    continue
                underside = base + round(2.6 * (1 - r / edge))
                thickness = 1 + floor((1 - min(1, r / edge)) * 3 + .55 * lobe)
                for y in range(underside, underside + thickness):
                    out[x, y, z] = leaf
    # Expose the brown undersides of the lowest boughs under the canopy.
    for axis in ("x", "z"):
        for sign in (-1, 1):
            for r in range(1, 7):
                x, z = (sign * r, 0) if axis == "x" else (0, sign * r)
                out[x, 8 - r // 3, z] = log + "[axis=" + axis + "]"
    for y in range(2, 27):
        out[0, y, 0] = log
        if y <= 8:
            for x, z in ((-1, 0), (0, -1), (1, 0), (0, 1)):
                out[x, y, z] = log
    # Pin baubles to actual outer needles. The circling strings of lights follow the drape of each tier.
    for i, (base, reach, spokes) in enumerate(tiers[:-1]):
        count = max(5, round(reach * 2.7))
        for j in range(count):
            a = tau * j / count + i * .61
            r = reach
            while r > 0:
                x, z = round(r * cos(a)), round(r * sin(a))
                needles = [y for y in range(base, base + 4) if out.get((x, y, z)) == leaf]
                if needles:
                    y = min(needles) - 1
                    if (x, y, z) not in out:
                        out[x, y, z] = ("jugcraft:holidayworks_red_ornament[face=ceiling]",
                                        "minecraft:ochre_froglight",
                                        "jugcraft:holidayworks_yellow_ornament[face=ceiling]",
                                        "minecraft:ochre_froglight",
                                        "jugcraft:holidayworks_red_ornament[face=ceiling]")[j % 5]
                    break
                r -= .4
    # A gilded double-sided star is readable down both of the market square's crossing streets.
    for y, arms in ((26, (0,)), (27, (-1, 0, 1)), (28, (0,)), (29, (-2, -1, 0, 1, 2)),
                    (30, (-1, 0, 1)), (31, (0,))):
        for d in arms:
            for x, z in ((d, 0), (0, d)):
                out[x, y, z] = "minecraft:" + ("ochre_froglight" if d == 0 or abs(d) == 2 else "gold_block")
    for x in range(-2, 3):
        for z in range(-2, 3):
            # A single continuous woven skirt beneath the parcels; four exposed buttresses meet the plinth.
            out[x, 1, z] = log if abs(x) + abs(z) == 1 else "minecraft:red_carpet"
    for part, (x, z) in enumerate(((0, 0), (-1, -1), (0, -1), (1, -1), (1, 0),
                                   (1, 1), (0, 1), (-1, 1), (-1, 0))):
        out[x * 2, 1, z * 2] = f"jugcraft:civic_gift_skirt[facing=north,part={part},lit=true,parcels=0]"
    return out


def scene():
    return {slot(*position): state for position, state in blocks().items()}
