"""The pirate ship's model and textures (docs/features/pirate-ship-crews.md): a two-masted galleon in the palette of the
Supplementaries galleon structures (dark oak hull, birch deck, stripped birch spars, grey and light grey sails with red,
cyan, green, brown and black accents, rope, iron and gold), crewed by plunderers at the ten stations of
menagerie/PirateCrew.STATIONS.

Frame (the same as CrewedVessel): ship-local, x to port, y up from the waterline, z to the bow; the origin is the
entity's position, at the middle of the hull on the waterline, and yaw 0 faces +z. The model is exported unrotated in
pixels (16 a block); the renderer turns it with the ship (the zeppelin's pose.rotateDegrees(Axis.YP, -yRot)).

Fixed by the crew interface and kept here exactly: beam 6 (x -3..3) and length 16 (z -8..8), the deck at y 1.0 and the
rail tops at y 2.0, the keel about 1.6 below the waterline, the main mast at z 1 up to y 12 and the fore mast at z 5 up
to y 10, gun ports in the bulwark at y 1.4..1.9 centred at z 2.5 and -2.0 on each side, and the walkable deck
x -2.5..2.5, y 1..3, z -7.5..7.5. The bowsprit, rudder, stern lantern and castle rails are art outside that deck.

Parts (assets/jugcraft/pirate_ship_quads.json, drawn by client/PirateShipRenderer through DecorQuads): the hull, drawn
at the entity's origin, with its masts, yards, topsails, rigging and fittings; and the moving parts, each exported about
its own pivot (its local origin is the pivot), which the renderer translates to and turns there: the main and fore
courses (sway about x), the jib, the flag (flutter about y; left undrawn when the crew are beaten), the helm's wheel
(about z), the rudder (about y), and one gun (carriage and barrel) exported about its trunnion pointing +z and drawn at
each gunner's trunnion turned by -facing. assets/jugcraft/pirate_ship_rig.json carries the pivots and trunnions in
pixels, the culling box in blocks and, besides, the stations (mirroring PirateCrew.STATIONS); DecorQuads reads every
key of a quads file as a part, so nothing else goes in pirate_ship_quads.json.

The crew stand at their seats with their feet exactly at the seat's height (a 0.6 x 1.95 figure): the deck at y 1.0
under the helm, the gunners and the deck crew, the main top's floor at y 9.5 under the lookout, and nothing of the
model inside any of those ten figures (standing_clear checks it).
"""
import json
import math

from PIL import Image

import block_style as bs
from steampunk_models import box, cyl, wheel
from zeppelin import tiled_quads

MOD = "jugcraft"
PX = 16

# ------------------------------------------------------------------ the crew interface (blocks, ship-local)

BEAM, LENGTH = 6, 16
DECK_Y, RAIL_Y = 1.0, 2.0
MAIN_MAST, FORE_MAST = (1.0, 12.0), (5.0, 10.0)  # (z, top y)
LOCAL_DECK = (-2.5, 1.0, -7.5, 2.5, 3.0, 7.5)
GUN_PORT_Y = (1.4, 1.9)
GUN_PORT_Z = (2.5, -2.0)
# PirateCrew.STATIONS, in its index order: (role, seat, facing, arc, muzzle). Facing is a yaw relative to the ship's:
# 0 the bow, +90 starboard, -90 port; the arc is the half-width either side of it. The muzzle is the seat for every
# role but the gunners'.
STATIONS = [
    ("HELM", (0.0, 1.0, -6.0), 0.0, 0.0, (0.0, 1.0, -6.0)),
    ("LOOKOUT", (0.0, 9.5, 1.0), 0.0, 180.0, (0.0, 9.5, 1.0)),
    ("GUNNER", (2.0, 1.0, 2.5), -90.0, 45.0, (3.2, 1.6, 2.5)),
    ("GUNNER", (2.0, 1.0, -2.0), -90.0, 45.0, (3.2, 1.6, -2.0)),
    ("GUNNER", (-2.0, 1.0, 2.5), 90.0, 45.0, (-3.2, 1.6, 2.5)),
    ("GUNNER", (-2.0, 1.0, -2.0), 90.0, 45.0, (-3.2, 1.6, -2.0)),
    ("DECK", (1.0, 1.0, 5.5), 0.0, 0.0, (1.0, 1.0, 5.5)),
    ("DECK", (-1.0, 1.0, 5.5), 0.0, 0.0, (-1.0, 1.0, 5.5)),
    ("DECK", (1.0, 1.0, -3.5), 0.0, 0.0, (1.0, 1.0, -3.5)),
    ("DECK", (-1.0, 1.0, -3.5), 0.0, 0.0, (-1.0, 1.0, -3.5)),
]
STATION_NAMES = ["helm", "lookout", "gunner_port_fore", "gunner_port_aft", "gunner_starboard_fore",
                 "gunner_starboard_aft", "deck_port_fore", "deck_starboard_fore", "deck_port_aft",
                 "deck_starboard_aft"]

# ------------------------------------------------------------------ textures

HULL, HULL_SIDE, TAR, BULWARK = "ps_hull", "ps_hull_side", "ps_hull_tar", "ps_bulwark"
DECK, TRIM, RED = "ps_deck", "ps_trim", "ps_red_paint"
SPAR, SPAR_H, SPAR_END = "ps_spar", "ps_spar_h", "ps_spar_end"
CANVAS, CANVAS_LIGHT, CANVAS_RED = "ps_canvas", "ps_canvas_light", "ps_canvas_red"
PATCH_CYAN, PATCH_GREEN, PATCH_BROWN = "ps_patch_cyan", "ps_patch_green", "ps_patch_brown"
ROPE, IRON, BORE, GOLD = "ps_rope", "ps_iron", "ps_bore", "ps_gold"
LAMP, WINDOW, HATCH = "ps_lamp", "ps_window", "ps_hatch"
BARREL, BARREL_TOP = "ps_barrel", "ps_barrel_top"
FLAG, FLAG_EDGE = "ps_flag", "ps_flag_edge"

# Spars by the axis they run along: grain along the spar, end grain on its ends.
SPAR_Y = {"*": SPAR, "up": SPAR_END, "down": SPAR_END}
SPAR_X = {"*": SPAR_H, "east": SPAR_END, "west": SPAR_END}
SPAR_Z = {"*": SPAR_H, "up": SPAR, "down": SPAR, "north": SPAR_END, "south": SPAR_END}
KEG = {"*": BARREL, "up": BARREL_TOP, "down": BARREL_TOP}

# ------------------------------------------------------------------ geometry (pixels, ship-local)

DECK_PX, RAIL_PX = 16, 32
# The hull's half-width at the deck, slice by slice from the transom (-z) to the stem (+z).
SLICES = [(-128, -120, 38), (-120, -112, 44), (-112, -96, 47), (-96, 48, 48), (48, 64, 47), (64, 80, 44),
          (80, 96, 38), (96, 104, 32), (104, 112, 26), (112, 120, 18), (120, 128, 10)]
# Below the waterline the hull narrows and its ends rake in: (bottom, top, share of the deck width, aft end, fore end).
UNDERWATER = [(-12, 0, 0.8, -128, 120), (-24, -12, 0.45, -116, 108)]
KEEL_PX = -26
# The castles' raised rails (art only, outside the walkable deck's ends).
STERN_RAIL, BOW_RAIL = 40, 36
# A crewman standing at his seat (pixels): half his width and his height.
FIGURE_HALF, FIGURE_HEIGHT = 0.3 * PX, 1.95 * PX
# Gun ports: an opening in the bulwark 12 px long at y 1.4..1.9, centred on each gunner's z.
PORT_LOW, PORT_HIGH, PORT_HALF = GUN_PORT_Y[0] * PX, GUN_PORT_Y[1] * PX, 6
# Each gun's trunnion (pixels): the barrel's muzzle face lies MUZZLE px out from it along the barrel, at the station's
# muzzle (3.2 blocks out at rest).
TRUNNION_X, TRUNNION_Y, MUZZLE = 43.0, 25.6, 8.2
# Masts: (centre z, wide half, narrow half, foot, head) of each lower mast; the topmasts stand aft of the lower mast
# heads (the main one clear of the lookout, who sits on the main top over the mast's line).
MAIN_Z, FORE_Z = MAIN_MAST[0] * PX, FORE_MAST[0] * PX
MAIN_TOP_FLOOR = STATIONS[1][1][1] * PX  # 152: the lookout's seat is the main top's floor
MAIN_HEAD, FORE_HEAD = MAIN_MAST[1] * PX, FORE_MAST[1] * PX  # 192, 160
WHEEL_HUB = (0.0, 40.0, -83.2)
RUDDER_HINGE = (0.0, 0.0, -131.0)
FLAG_PIVOT = (0.0, 180.0, 5.0)
# The bowsprit: foot (y, z), length and rise (degrees about x; negative lifts it); the forestay runs from its tip.
BOWSPRIT_FOOT, BOWSPRIT_LENGTH, BOWSPRIT_ANGLE = (26.0, 112.0), 66.0, -15.7


def _shift(elements, dx, dy, dz):
    """Boxes moved by (dx, dy, dz), rotation origins too: a part built in ship pixels, exported about its pivot."""
    out = []
    for item in elements:
        frm, to, texture = item[:3]
        frm = (frm[0] + dx, frm[1] + dy, frm[2] + dz)
        to = (to[0] + dx, to[1] + dy, to[2] + dz)
        if len(item) > 3 and item[3].get("rotation"):
            axis, angle, origin = item[3]["rotation"][:3]
            out.append(box(frm, to, texture, rotation=(axis, angle, (origin[0] + dx, origin[1] + dy, origin[2] + dz))))
        else:
            out.append(box(frm, to, texture))
    return out


def _round(value):
    return round(value * 4) / 4


def _layer_slices(scale, z_min, z_max):
    """The underwater layer's slices: the deck slices inside z_min..z_max, narrowed, neighbours within a pixel of each
    other merged at the wider one."""
    out = []
    for z0, z1, w in SLICES:
        z0, z1 = max(z0, z_min), min(z1, z_max)
        if z1 <= z0:
            continue
        half = max(2, round(w * scale))
        if out and abs(out[-1][2] - half) <= 1 and out[-1][1] == z0:
            out[-1] = (out[-1][0], z1, max(half, out[-1][2]))
        else:
            out.append((z0, z1, half))
    return out


def gun_ports():
    """(side, z0, z1) of each opening: port (+x) and starboard (-x), at each gunner's z."""
    out = []
    for role, seat, facing, arc, muzzle in STATIONS:
        if role == "GUNNER":
            side = 1 if seat[0] > 0 else -1
            out.append((side, muzzle[2] * PX - PORT_HALF, muzzle[2] * PX + PORT_HALF))
    return out


def hull_body():
    """The hull: dark oak sides with a black wale painted along them (y 4..8) over a tarred bottom, the birch deck,
    keel, stem and sternpost."""
    m = []
    for z0, z1, w in SLICES:
        m.append(box((-w, 0, z0), (w, DECK_PX, z1), {"*": HULL_SIDE, "up": DECK, "down": TAR}))
    for y0, y1, scale, z_min, z_max in UNDERWATER:
        for z0, z1, half in _layer_slices(scale, z_min, z_max):
            m.append(box((-half, y0, z0), (half, y1, z1), TAR))
    m.append(box((-2, KEEL_PX, -131), (2, -24, 114), TAR))
    # The stem, raked forward step by step, and its gold finial.
    for y0, y1, z0, z1 in ((-24, -12, 108, 114), (-12, 0, 120, 126), (0, 38, 128, 132)):
        m.append(box((-2, y0, z0), (2, y1, z1), {"*": TAR if y1 <= 0 else HULL, "up": TRIM}))
    m.append(box((-1.5, 38, 128.5), (1.5, 41, 131.5), GOLD))
    # The deadwood and sternpost the rudder hangs on.
    m.append(box((-2.5, -24, -128), (2.5, -12, -116), TAR))
    m.append(box((-2.5, -24, -131), (2.5, 18, -128), {"*": TAR, "up": TRIM}))
    return m


def bulwarks():
    """Bulwarks round the deck to the rail at y 2.0, with a red band outside, the gun ports cut through them and their
    lids propped open, the raised castle rails at the ends, the transom with the cabin windows and lantern."""
    m = []
    ports = gun_ports()
    for z0, z1, w in SLICES:
        for side in (1, -1):
            x0, x1 = (w - 2, w) if side > 0 else (-w, -w + 2)
            outer = "east" if side > 0 else "west"
            texture = {"*": HULL, outer: BULWARK, "up": TRIM}
            cuts = sorted((p0, p1) for s, p0, p1 in ports if s == side and z0 < p0 and p1 < z1)
            start = z0
            for p0, p1 in cuts:
                m.append(box((x0, DECK_PX, start), (x1, RAIL_PX, p0), texture))
                m.append(box((x0, DECK_PX, p0), (x1, PORT_LOW, p1), texture))
                m.append(box((x0, PORT_HIGH, p0), (x1, RAIL_PX, p1), texture))
                start = p1
            m.append(box((x0, DECK_PX, start), (x1, RAIL_PX, z1), texture))
            if z1 <= -112:
                m.append(box((x0, RAIL_PX, z0), (x1, STERN_RAIL, z1), {"*": HULL, "up": TRIM}))
            elif z0 >= 112:
                m.append(box((x0, RAIL_PX, z0), (x1, BOW_RAIL, z1), {"*": HULL, "up": TRIM}))
    # Where the hull steps in, a short wall across the step joins the wider slice's bulwark to the narrower one's.
    for (a0, a1, wa), (b0, b1, wb) in zip(SLICES, SLICES[1:]):
        if wa == wb:
            continue
        wide, narrow = max(wa, wb), min(wa, wb)
        z0, z1 = (a1 - 2, a1) if wa > wb else (b0, b0 + 2)
        top = STERN_RAIL if z1 <= -112 else BOW_RAIL if z0 >= 112 else RAIL_PX
        for side in (1, -1):
            x0, x1 = (narrow - 2, wide) if side > 0 else (-wide, -narrow + 2)
            m.append(box((x0, DECK_PX, z0), (x1, top, z1), {"*": HULL, "up": TRIM}))
    # Gun port lids, hinged at the port's head and propped open 45 degrees, red inside.
    for side, p0, p1 in ports:
        hinge = 48.0 * side
        frm, to = ((48, PORT_HIGH, p0 + 0.5), (49, PORT_HIGH + 8, p1 - 0.5)) if side > 0 else \
            ((-49, PORT_HIGH, p0 + 0.5), (-48, PORT_HIGH + 8, p1 - 0.5))
        inner = "west" if side > 0 else "east"
        m.append(box(frm, to, {"*": HULL, inner: RED}, rotation=("z", -45 * side, (hinge, PORT_HIGH, 0))))
    # The transom across the stern, its cabin windows and the stern lantern; the bow's breastwork.
    m.append(box((-38, DECK_PX, -128), (38, STERN_RAIL, -126), {"*": HULL, "up": TRIM}))
    for x in (-24, -10, 10, 24):
        m.append(box((x - 4, 22, -128.5), (x + 4, 32, -128), {"*": GOLD, "north": f"{WINDOW}!"}))
    m.append(box((-1, STERN_RAIL, -128), (1, STERN_RAIL + 6, -126), GOLD))
    m.append(box((-3, STERN_RAIL + 6, -130), (3, STERN_RAIL + 14, -124), {"*": f"{LAMP}!", "up": GOLD, "down": GOLD}))
    m.append(box((-2, STERN_RAIL + 14, -129), (2, STERN_RAIL + 16, -125), GOLD))
    m.append(box((-8, DECK_PX, 125), (8, BOW_RAIL, 128), {"*": HULL, "up": TRIM}))
    # Channels outside the rail, where the shrouds come down.
    for z0, z1, w in ((0, 16, 48), (64, 80, 44)):
        m.append(box((w, 28, z0), (w + 3, 31, z1), {"*": HULL, "up": TRIM}))
        m.append(box((-w - 3, 28, z0), (-w, 31, z1), {"*": HULL, "up": TRIM}))
    return m


def masts():
    """Two masts of stripped birch: lower masts with tops on trestles and topmasts stepped aft of their heads, the
    main top a railed crow's nest round the lookout's seat, and the yards the sails hang from."""
    def mast(cz, half, y0, y1):
        return [box((-half, y0, cz - half), (half, y1, cz + half), SPAR_Y)]
    floor = MAIN_TOP_FLOOR
    m = mast(MAIN_Z, 3.5, DECK_PX, floor - 4)
    m.append(box((-4, floor - 10, 2), (4, floor - 4, 23), HULL))  # trestle trees under the top
    m.append(box((-12, floor - 4, 2), (12, floor, 30), {"*": HULL, "up": DECK}))
    rail = {"*": HULL, "up": TRIM}
    m.append(box((10, floor, 2), (12, floor + 8, 30), rail))
    m.append(box((-12, floor, 2), (-10, floor + 8, 30), rail))
    m.append(box((-10, floor, 28), (10, floor + 8, 30), rail))
    m.append(box((-10, floor, 2), (10, floor + 8, 4), rail))
    m.append(box((-2, floor - 8, 3), (2, MAIN_HEAD - 2, 7), SPAR_Y))  # main topmast
    m.append(box((-2.5, MAIN_HEAD - 2, 2.5), (2.5, MAIN_HEAD, 7.5), GOLD))  # its truck, at y 12
    m += mast(FORE_Z, 3, DECK_PX, 120)
    m.append(box((-3.5, 116, 70), (3.5, 120, 86), HULL))
    m.append(box((-9, 120, 70), (9, 123, 88), {"*": HULL, "up": DECK}))
    m.append(box((-1.75, 118, 72.5), (1.75, FORE_HEAD - 2, 76), SPAR_Y))  # fore topmast
    m.append(box((-2.25, FORE_HEAD - 2, 72), (2.25, FORE_HEAD, 76.5), GOLD))  # its truck, at y 10
    for sail in SAILS.values():
        m.append(box(*sail["yard"], SPAR_X))
    # The bowsprit, rising over the stem.
    y, z = BOWSPRIT_FOOT
    m.append(box((-2.5, y - 2.5, z), (2.5, y + 2.5, z + BOWSPRIT_LENGTH), SPAR_Z,
                 rotation=("x", BOWSPRIT_ANGLE, (0, y, z))))
    return m


def bowsprit_tip():
    y, z = BOWSPRIT_FOOT
    rad = math.radians(BOWSPRIT_ANGLE)
    return y - BOWSPRIT_LENGTH * math.sin(rad), z + BOWSPRIT_LENGTH * math.cos(rad)


def _stay(low, high):
    """A rope from low (y, z) up to high (y, z) at x 0: a thin box along +y turned about x at its low end."""
    (y0, z0), (y1, z1) = low, high
    length = math.hypot(y1 - y0, z1 - z0)
    angle = math.degrees(math.asin((z1 - z0) / length))
    return box((-0.5, y0, z0 - 0.5), (0.5, y0 + length, z0 + 0.5), ROPE, rotation=("x", angle, (0, y0, z0)))


FORESTAY_HEAD = (FORE_HEAD - 2, 76.0)


def rigging():
    """Shrouds with ratlines from the channels to each lower mast head, the forestay to the bowsprit and the main stay
    to the foremast's foot."""
    m = []
    for base_x, top_x, top_y, zs in ((49.5, 4.0, MAIN_TOP_FLOOR - 10, (4, 12)), (45.5, 3.5, 116, (68, 75))):
        for side in (1, -1):
            dx, dy = (top_x - base_x) * side, top_y - 31
            length = math.hypot(dx, dy)
            angle = math.degrees(math.asin(-dx / length))
            rotation = ("z", angle, (base_x * side, 31, 0))
            x = base_x * side
            for z in zs:
                m.append(box((x - 0.5, 31, z - 0.5), (x + 0.5, 31 + length, z + 0.5), ROPE, rotation=rotation))
            t = 16
            while t < length - 10:  # ratlines, their ends against the shrouds left out
                m.append(box((x - 0.35, 31 + t - 0.35, zs[0] + 0.5), (x + 0.35, 31 + t + 0.35, zs[1] - 0.5),
                             {"*": ROPE, "north": None, "south": None}, rotation=rotation))
                t += 16
    m.append(_stay(bowsprit_tip(), FORESTAY_HEAD))
    m.append(_stay((20.0, FORE_Z - 2), (MAIN_TOP_FLOOR - 6, MAIN_Z + 3.5)))
    return m


def deck_fittings():
    """The helm's wheel stand forward of the wheel, the hatch, the capstan, the anchors at their catheads and a few
    barrels, all clear of where the crew stand."""
    m = []
    hx, hy, hz = WHEEL_HUB
    m.append(box((-3, DECK_PX, hz + 3.2), (3, hy - 3, hz + 7.2), HULL))  # the wheel's stand
    m.append(box((-1, hy - 1, hz + 0.2), (1, hy + 1, hz + 9.2), IRON))  # its axle, into the wheel's hub
    m += cyl("z", 0, hy, 3, hz + 3.2, hz + 7.2, BARREL, HULL)  # the tiller-rope drum on the stand
    m.append(box((-10, DECK_PX, -32), (10, DECK_PX + 2, -12), {"*": TRIM, "up": HATCH}))
    m += cyl("y", 0, -2, 4.5, DECK_PX, DECK_PX + 12, HULL, TRIM)  # capstan
    for x, z in ((26, -84), (-26, -84), (18, 106), (-18, 106)):
        m.append(box((x - 4, DECK_PX, z - 4), (x + 4, DECK_PX + 12, z + 4), KEG))
    m += cyl("y", 24, 62, 4, DECK_PX, DECK_PX + 2, ROPE)  # a coil of rope by the foremast
    for side in (1, -1):
        def sx(a, b):
            return (a, b) if side > 0 else (-b, -a)
        x0, x1 = sx(32, 34)
        m.append(box((x0, 6, 99.5), (x1, 28, 101.5), IRON))  # shank
        m.append(box((x0, 4, 94), (x1, 6, 107), IRON))  # arms
        m.append(box((x0, 6, 94), (x1, 9, 96), IRON))  # flukes
        m.append(box((x0, 6, 105), (x1, 9, 107), IRON))
        s0, s1 = sx(31, 37)
        m.append(box((s0, 24.5, 99), (s1, 26.5, 102), HULL))  # stock
        r0, r1 = sx(32.5, 33.5)
        m.append(box((r0, 26.5, 100), (r1, 28, 101), IRON))  # ring
        c0, c1 = sx(30, 38)
        m.append(box((c0, 28, 98), (c1, 31, 103), {"*": HULL, "up": TRIM}))  # cathead
    return m


def hull():
    """The hull part: everything that does not move, the topsails included (they hang over the lookout, so they keep
    still)."""
    return hull_body() + bulwarks() + masts() + rigging() + deck_fittings() + sail_boxes("main_top") + \
        sail_boxes("fore_top")


# The sails: each hangs from its yard (from, to) in pieces of (top, bottom, half-width, front z, texture), pieces lower
# down bellied further forward, overlapping the piece above so the sail stays one closed shape; patches are placed
# wear sewn on the front. The pivot is the yard's middle.
SAILS = {
    "main": {"yard": ((-46, 134, 19.5), (46, 137, 22.5)),
             "pieces": [(136, 120, 42, 22.4, CANVAS), (120, 104, 42, 23.0, CANVAS), (104, 88, 42, 23.6, CANVAS_RED),
                        (88, 72, 42, 23.2, CANVAS)],
             "patch": ((-22, 106), (-12, 116), PATCH_CYAN)},
    "main_top": {"yard": ((-32, 186, 7), (32, 188.5, 9.5)),
                 "pieces": [(188, 178, 28, 9.0, CANVAS_LIGHT), (178, 168, 31, 9.6, CANVAS_LIGHT)],
                 "patch": ((8, 170), (16, 176), PATCH_BROWN)},
    "fore": {"yard": ((-40, 108, 83), (40, 111, 86)),
             "pieces": [(110, 96, 36, 85.6, CANVAS), (96, 80, 36, 86.4, CANVAS_RED), (80, 64, 36, 86.0, CANVAS)],
             "patch": ((14, 98), (24, 106), PATCH_GREEN)},
    "fore_top": {"yard": ((-28, 152, 76), (28, 154.5, 78.5)),
                 "pieces": [(154, 142, 22, 77.9, CANVAS_LIGHT), (142, 128, 25, 78.5, CANVAS_LIGHT)]},
}


def sail_pivot(name):
    (x0, y0, z0), (x1, y1, z1) = SAILS[name]["yard"]
    return ((x0 + x1) / 2, (y0 + y1) / 2, (z0 + z1) / 2)


def sail_boxes(name):
    """A sail in ship pixels."""
    spec = SAILS[name]
    m = []
    for top, bottom, half, front, texture in spec["pieces"]:
        m.append(box((-half, bottom, front - 1), (half, top, front), {"*": CANVAS if texture == CANVAS_RED else texture,
                                                                       "north": texture, "south": texture}))
    if spec.get("patch"):
        (x0, y0), (x1, y1), texture = spec["patch"]
        front = next(f for top, bottom, half, f, t in spec["pieces"] if bottom <= y0 and y1 <= top)
        m.append(box((x0, y0, front), (x1, y1, front + 0.3), {"*": texture, "south": f"{texture}!"}))
    return m


def sail(name):
    """A course, about its pivot (the middle of its yard)."""
    px, py, pz = sail_pivot(name)
    return _shift(sail_boxes(name), -px, -py, -pz)


def _forestay_z(y):
    (ty, tz), (hy, hz) = bowsprit_tip(), FORESTAY_HEAD
    return tz + (y - ty) * (hz - tz) / (hy - ty)


JIB_CLEW, JIB_HEAD_Y = (50.0, 124.0), 134.0
JIB_PIVOT = (0.0, 50.0, 124.0)


def jib():
    """The jib, light grey, set under the forestay in steps: its luff a little below the stay, its leech from the clew
    over the bow up to the head."""
    m = []
    cy, cz = JIB_CLEW
    head_z = _forestay_z(JIB_HEAD_Y) - 2
    y0 = cy
    while y0 < JIB_HEAD_Y:
        y1 = y0 + 8
        back = _round(cz + ((y0 + y1) / 2 - cy) * (head_z - cz) / (JIB_HEAD_Y - cy))
        front = _round(_forestay_z(y1) - 2)
        if front - back < 3:
            break
        m.append(box((-0.3, y0, back), (0.3, y1, front), CANVAS_LIGHT))
        y0 = y1
    return _shift(m, *(-v for v in JIB_PIVOT))


def flag():
    """The black flag with skull and crossbones, flying aft from the main topmast just under its truck."""
    m = [box((-0.5, 172, -12.5), (0.5, 188, 3.5), {"*": FLAG_EDGE, "east": f"{FLAG}!", "west": f"{FLAG}!"})]
    return _shift(m, *(-v for v in FLAG_PIVOT))


def helm_wheel():
    """The ship's wheel about its hub (axle along z): an eight-sided rim with eight spokes run out into handles."""
    m = wheel("z", 0, 0, 7, -0.75, 0.75, HULL, GOLD, rim=1.25)
    for angle in (45, -45):
        m.append(box((-0.35, -5.75, -0.55), (0.35, 5.75, 0.55), HULL, rotation=("z", angle, (0, 0, 0))))
    m += [box((-0.6, 7, -0.6), (0.6, 9.5, 0.6), HULL), box((-0.6, -9.5, -0.6), (0.6, -7, 0.6), HULL),
          box((-9.5, -0.6, -0.6), (-7, 0.6, 0.6), HULL), box((7, -0.6, -0.6), (9.5, 0.6, 0.6), HULL)]
    for angle in (45, 135, -45, -135):
        m.append(box((-0.6, 7, -0.6), (0.6, 9.5, 0.6), HULL, rotation=("z", angle, (0, 0, 0))))
    return m


def rudder():
    """The rudder about its hinge on the sternpost (turning about y): a blade wider below, iron pintle straps."""
    m = [box((-1.5, -22, -11), (1.5, -4, 0), {"*": HULL, "down": TAR}),
         box((-1.5, -4, -7), (1.5, 14, 0), {"*": HULL, "up": TRIM})]
    for y in (-16, -4, 8):
        m.append(box((-2, y, -3), (2, y + 1.5, 0), IRON))
    return m


def cannon_barrel():
    """A gun's barrel about its trunnions, pointing along +z: breech, chase and muzzle swell in iron, the bore dark.
    The muzzle face is MUZZLE px from the trunnion, at the station's muzzle when the gun points out of its port."""
    m = [box((-1, -1, -5.2), (1, 1, -4.2), IRON)]
    m += cyl("z", 0, 0, 2.75, -4.2, 0.5, IRON)
    m += cyl("z", 0, 0, 2.25, 0.5, 6.2, IRON)
    m += cyl("z", 0, 0, 2.75, 6.2, MUZZLE - 0.2, IRON)
    m.append(box((-1.25, -1.25, MUZZLE - 1.2), (1.25, 1.25, MUZZLE), BORE))
    m.append(box((-3.6, -0.9, -0.9), (3.6, 0.9, 0.9), IRON))
    return m


def cannon_carriage():
    """A gun's carriage about the trunnions, pointing along +z: red cheeks stepped down to the rear, a bed and
    axletrees on four trucks standing on the deck (local y -9.6), short enough to clear the gunner behind it."""
    deck = DECK_PX - TRUNNION_Y
    m = []
    for side in (1, -1):
        def sx(a, b):
            return (a, b) if side > 0 else (-b, -a)
        x0, x1 = sx(2.9, 4.1)
        m.append(box((x0, deck + 0.5, -5.5), (x1, deck + 5, -2.0), RED))
        m.append(box((x0, deck + 0.5, -2.0), (x1, -0.8, 1.8), RED))
        w0, w1 = sx(4.1, 5.3)
        for z in (0.0, -3.8):
            m.append(box((w0, deck + 0.1, z - 1.5), (w1, deck + 3.1, z + 1.5), HULL))
    m.append(box((-2.9, deck + 1, -5.0), (2.9, deck + 2.5, 1.5), RED))
    for z in (0.0, -3.8):
        m.append(box((-4.1, deck + 1, z - 0.6), (4.1, deck + 2.4, z + 0.6), HULL))
    return m


def cannon():
    """One gun about its trunnion, pointing +z: the renderer draws it at each gunner's trunnion turned by -facing."""
    return cannon_carriage() + cannon_barrel()


def trunnions():
    """Each gun's trunnion (ship pixels) and its station's index and facing, in PirateCrew.STATIONS order."""
    out = []
    for index, (role, seat, facing, arc, muzzle) in enumerate(STATIONS):
        if role == "GUNNER":
            side = 1 if seat[0] > 0 else -1
            out.append((index, (TRUNNION_X * side, TRUNNION_Y, muzzle[2] * PX), facing))
    return out


# ------------------------------------------------------------------ export

PARTS = ["pirate_ship_hull", "pirate_ship_sail_main", "pirate_ship_sail_fore", "pirate_ship_jib", "pirate_ship_flag",
         "pirate_ship_wheel", "pirate_ship_rudder", "pirate_ship_cannon"]
# The rig (pirate_ship_rig.json): each moving part's pivot, its local origin, in ship pixels, unrotated; and the guns'
# trunnions, in the order of gunner stations 2..5 (port fore, port aft, starboard fore, starboard aft).
PIVOTS = {"pirate_ship_sail_fore": sail_pivot("fore"), "pirate_ship_sail_main": sail_pivot("main"),
          "pirate_ship_jib": JIB_PIVOT, "pirate_ship_flag": FLAG_PIVOT, "pirate_ship_wheel": WHEEL_HUB,
          "pirate_ship_rudder": RUDDER_HINGE}
CANNONS = [point for index, point, facing in trunnions()]
# The culling box's room beyond the model (blocks), for the sails' sway.
CULLING_MARGIN = 0.5


def _tiled(elements, offset=(0, 0, 0)):
    """zeppelin.tiled_quads with its 16-pixel grid moved by `offset` (pixels): a small part exported about its pivot
    would otherwise be cut along the pivot's planes for nothing; a sail's grid runs along its pieces' seams."""
    dx, dy, dz = offset
    quads = tiled_quads(_shift(elements, dx, dy, dz))
    for quad in quads:
        for v in quad["vertices"]:
            v[0], v[1], v[2] = round(v[0] - dx, 4), round(v[1] - dy, 4), round(v[2] - dz, 4)
    return quads


def _seam_offset(pivot_y, seam_y):
    return (pivot_y - seam_y) % PX


def export():
    return {
        "pirate_ship_hull": tiled_quads(hull()),
        "pirate_ship_sail_main": _tiled(sail("main"), (0, _seam_offset(sail_pivot("main")[1], 120), 8)),
        "pirate_ship_sail_fore": _tiled(sail("fore"), (0, _seam_offset(sail_pivot("fore")[1], 96), 8)),
        "pirate_ship_jib": _tiled(jib(), (8, 0, 0)),
        "pirate_ship_flag": _tiled(flag(), (8, 8, 1.5)),
        "pirate_ship_wheel": _tiled(helm_wheel(), (8, 8, 8)),
        "pirate_ship_rudder": _tiled(rudder(), (8, 0, 12)),
        "pirate_ship_cannon": _tiled(cannon(), (8, 10, 7)),
    }


def placements():
    """Where the renderer draws each part, ship pixels: (part, pivot, yaw), yaw the station's facing for the guns."""
    out = [("pirate_ship_hull", (0.0, 0.0, 0.0), 0.0)]
    out += [(part, pivot, 0.0) for part, pivot in PIVOTS.items()]
    out += [("pirate_ship_cannon", point, facing) for index, point, facing in trunnions()]
    return out


def place(vertex, pivot, yaw):
    """A part's vertex (pixels about its pivot) in ship pixels, turned by the station facing as the renderer does
    (yRot(-facing): facing -90 points the gun's +z to port, +x)."""
    rad = math.radians(-yaw)
    x, y, z = vertex[:3]
    return (x * math.cos(rad) + z * math.sin(rad) + pivot[0], y + pivot[1], z * math.cos(rad) - x * math.sin(rad) + pivot[2])


def _clip(poly, axis, value, keep_above):
    out = []
    for i, a in enumerate(poly):
        b = poly[(i + 1) % len(poly)]
        a_in = a[axis] >= value if keep_above else a[axis] <= value
        b_in = b[axis] >= value if keep_above else b[axis] <= value
        if a_in:
            out.append(a)
        if a_in != b_in:
            t = (value - a[axis]) / (b[axis] - a[axis])
            out.append(tuple(a[k] + t * (b[k] - a[k]) for k in range(3)))
    return out


def _area(poly):
    total = [0.0, 0.0, 0.0]
    for i, a in enumerate(poly):
        b = poly[(i + 1) % len(poly)]
        total = [total[0] + a[1] * b[2] - a[2] * b[1], total[1] + a[2] * b[0] - a[0] * b[2],
                 total[2] + a[0] * b[1] - a[1] * b[0]]
    return 0.5 * math.sqrt(sum(v * v for v in total))


def figure(index):
    """The box (pixels) a crewman standing at station `index` fills: feet at the seat, 0.6 wide, 1.95 tall."""
    x, y, z = (v * PX for v in STATIONS[index][1])
    return ((x - FIGURE_HALF, y, z - FIGURE_HALF), (x + FIGURE_HALF, y + FIGURE_HEIGHT, z + FIGURE_HALF))


def standing_clear(quads=None, eps=0.01):
    """[(station, part, texture)] for every drawn quad (parts placed as the renderer places them) that reaches inside
    a standing crewman: empty when all ten stand clear. The floor under their feet only touches them."""
    quads = quads or export()
    problems = []
    for index in range(len(STATIONS)):
        lo, hi = figure(index)
        lo, hi = [v + eps for v in lo], [v - eps for v in hi]
        for part, pivot, yaw in placements():
            for quad in quads[part]:
                poly = [place(v, pivot, yaw) for v in quad["vertices"]]
                for axis in range(3):
                    poly = _clip(poly, axis, lo[axis], True)
                    poly = _clip(poly, axis, hi[axis], False) if poly else poly
                if len(poly) >= 3 and _area(poly) > 1e-6:
                    problems.append((STATION_NAMES[index], part, quad["texture"]))
    return problems


def rig(quads=None):
    """pirate_ship_rig.json, as client/PirateShipRenderer reads it: "pivots" and "cannons" in pixels and "culling" in
    blocks, ship-local and unrotated; then, for the crew and the checks, the stations (PirateCrew.STATIONS), the walkable
    deck and the fixed dimensions, in blocks."""
    quads = quads or export()
    lo, hi = [math.inf] * 3, [-math.inf] * 3
    for part, pivot, yaw in placements():
        for quad in quads[part]:
            for vertex in quad["vertices"]:
                p = place(vertex, pivot, yaw)
                lo = [min(lo[k], p[k]) for k in range(3)]
                hi = [max(hi[k], p[k]) for k in range(3)]
    culling = [round(v / PX - CULLING_MARGIN, 3) for v in lo] + [round(v / PX + CULLING_MARGIN, 3) for v in hi]
    stations = []
    for index, ((role, seat, facing, arc, muzzle), name) in enumerate(zip(STATIONS, STATION_NAMES)):
        stations.append({"index": index, "name": name, "role": role, "seat": list(seat), "facing": facing, "arc": arc,
                         "muzzle": list(muzzle)})
    return {
        "pivots": {part: [round(v, 4) for v in pivot] for part, pivot in PIVOTS.items()},
        "cannons": [[round(v, 4) for v in point] for point in CANNONS],
        "culling": culling,
        "frame": "ship-local: +x port, +y up from the waterline, +z bow; origin the entity position at the hull's middle on "
                 "the waterline; yaw 0 faces +z; facing 0 bow, +90 starboard, -90 port; unrotated (turn by -yaw). "
                 "pivots and cannons are in pixels (16 a block); culling, stations, local_deck and hull in blocks. Each "
                 "part in pirate_ship_quads.json is in pixels about its pivot; pirate_ship_cannon about its trunnion, "
                 "pointing +z.",
        "cannon": {"muzzle_from_trunnion": MUZZLE, "points": "+z", "turn": "-facing about y"},
        "stations": stations,
        "local_deck": list(LOCAL_DECK),
        "hull": {"beam": BEAM, "length": LENGTH, "deck_y": DECK_Y, "rail_y": RAIL_Y, "keel_y": KEEL_PX / PX,
                 "main_mast": {"z": MAIN_MAST[0], "top": MAIN_MAST[1]},
                 "fore_mast": {"z": FORE_MAST[0], "top": FORE_MAST[1]},
                 "gun_ports": [{"side": "port" if side > 0 else "starboard", "z": [p0 / PX, p1 / PX],
                                "y": list(GUN_PORT_Y)} for side, p0, p1 in gun_ports()]},
    }


# ------------------------------------------------------------------ data

def write_all(write, assets, data, lang, condition):
    quads = export()
    # Written compact: it is thousands of quads.
    (assets / "pirate_ship_quads.json").write_text(json.dumps(quads, separators=(",", ":")) + "\n", encoding="utf-8")
    (assets / "pirate_ship_rig.json").write_text(json.dumps(rig(quads), indent=2) + "\n", encoding="utf-8")


# ------------------------------------------------------------------ art

def _pal(*hexes):
    return [tuple(int(h[i:i + 2], 16) for i in (0, 2, 4)) for h in hexes]


DARK_OAK = _pal("2a1b0d", "372412", "432b16", "4f331b", "5c3d21")
TARRED = _pal("1a1614", "211c19", "28221e", "302924", "38302b")
BLACK_PAINT = _pal("141418", "1c1d22", "25262c", "2f3037", "3b3c44")
RED_PAINT = _pal("6a1a15", "80221c", "962a22", "a8342a", "b84236")
BIRCH = _pal("96805a", "ab966a", "c0af7b", "cebd89", "dccb98")
STRIPPED = _pal("a89268", "baa476", "c6b182", "d2bf91", "dfcda0")
GREY_WOOL = _pal("3a3f42", "42484b", "4b5154", "555b5e", "61676a")
LIGHT_WOOL = _pal("76766f", "84847c", "8f8f87", "9b9b93", "a8a8a0")
RED_WOOL = _pal("7c1e1a", "8e2420", "a02a24", "b0332b", "bf4034")
CYAN_WOOL = _pal("0f5f67", "137079", "17808a", "1c9099", "2aa0a8")
GREEN_WOOL = _pal("3e5214", "485f18", "546d1b", "607b22", "6d892b")
BROWN_WOOL = _pal("583618", "643e1e", "724728", "7e502e", "8c5b37")
FLAG_BLACK = _pal("121216", "17181c", "1d1e23", "24252b", "2c2d34")
ROPE_TAN = _pal("60482c", "765a38", "8c6e46", "a08054", "b29264")
IRON_DARK = _pal("1e1e22", "28282e", "34343a", "404048", "505058", "62626a")
GOLD_BARS = _pal("8c5612", "ba801a", "deaa2c", "f4cc46", "fce880")
STAVES = _pal("4a2f17", "5c3b1d", "6c4624", "7c522c", "8c5f35")
GLASS = _pal("14606a", "1c7884", "2a909a", "4aaab0", "8acfd0")
BONE = _pal("8a7a5c", "b5a684", "d6cba8", "ebe2c4", "f8f3e2")
SOCKET = _pal("231a24", "3a2c3a", "54404f")


class _Canvas:
    """A 16x16 opaque texture the block_style painters draw on."""
    w = h = 16

    def __init__(self, fill=(0, 0, 0)):
        self.img = Image.new("RGBA", (16, 16), tuple(fill) + (255,))

    def put(self, x, y, c):
        if 0 <= x < 16 and 0 <= y < 16:
            self.img.putpixel((int(x), int(y)), tuple(c[:3]) + (255,))

    px = put

    def get(self, x, y):
        return self.img.getpixel((x, y))[:3]


def _painted(painter):
    c = _Canvas()
    painter(c)
    return c


def planks(palette, seed, vertical=False):
    return _painted(bs.planks(palette, seed, boards=4, vertical=vertical)).img


def bulwark(seed):
    """Dark oak boards with the second board from the top painted red: the band runs between the gun ports."""
    c = _painted(bs.planks(DARK_OAK, seed, boards=4))
    red = _painted(bs.planks(RED_PAINT, seed + 1, boards=4))
    for y in range(4, 8):
        for x in range(16):
            c.put(x, y, red.get(x, y))
    return c.img


def hull_side(seed):
    """The hull's planking with its third board from the top (y 4..8 above the waterline) painted black: the wale."""
    c = _painted(bs.planks(DARK_OAK, seed, boards=4))
    black = _painted(bs.planks(BLACK_PAINT, seed + 3, boards=4))
    for y in range(8, 12):
        for x in range(16):
            c.put(x, y, black.get(x, y))
    return c.img


def canvas(palette, seed):
    """Sailcloth: soft wool-like clumps, cut into cloths half a block wide (a seam, then a lit edge)."""
    c = _painted(bs.cloth(palette, seed))
    for y in range(16):
        for x in (7, 15):
            c.put(x, y, palette[0])
        for x in (0, 8):
            c.put(x, y, palette[-1])
    return c.img


def patch(palette, seed):
    """A patch sewn on a sail (stretched once over it): cloth inside a dark hem with light stitches."""
    c = _painted(bs.cloth(palette, seed))
    for i in range(16):
        for x, y in ((i, 0), (i, 15), (0, i), (15, i)):
            c.put(x, y, palette[0])
        if i % 3 == 1 and 1 <= i <= 14:
            for x, y in ((i, 1), (i, 14), (1, i), (14, i)):
                c.put(x, y, palette[-1])
    return c.img


def rope():
    """Laid rope: strands twisted diagonally, each lit along one side."""
    c = _Canvas()
    for y in range(16):
        for x in range(16):
            c.put(x, y, ROPE_TAN[[0, 2, 4, 3][(x + y) % 4]])
    return c.img


def spar_end():
    """The end of a stripped spar: square rings round a darker heart, as a log's end."""
    c = _Canvas()
    for y in range(16):
        for x in range(16):
            d = max(abs(x - 7.5), abs(y - 7.5))
            c.put(x, y, STRIPPED[0] if d > 6.5 else STRIPPED[1] if d < 1.5 else STRIPPED[3 if int(d) % 2 else 2])
    return c.img


def lamp():
    """A lantern pane, stretched once over each side: a gold frame round a glow in smooth bands, brightest low."""
    c = _Canvas()
    bands = [(232, 160, 48), (246, 190, 70), (252, 214, 100), (255, 232, 150)]
    for y in range(16):
        for x in range(16):
            if x in (0, 15) or y in (0, 15):
                c.put(x, y, GOLD_BARS[1] if x == 15 or y == 15 else GOLD_BARS[3])
            elif x in (7, 8):
                c.put(x, y, GOLD_BARS[2])
            else:
                c.put(x, y, bands[min(3, (y - 1) // 4)])
    return c.img


def window():
    """A stern cabin window, stretched once over it: a gold frame, dark oak mullions and four panes of cyan glass, each
    lit along its top and left."""
    c = _Canvas()
    for y in range(16):
        for x in range(16):
            if x in (0, 15) or y in (0, 15):
                col = GOLD_BARS[1] if x == 15 or y == 15 else GOLD_BARS[3]
            elif x in (1, 14) or y in (1, 14) or x in (7, 8) or y in (7, 8):
                col = DARK_OAK[2] if (x, y) != (8, 8) else DARK_OAK[1]
            else:
                px, py = (x - 2) % 6 if x < 7 else (x - 9) % 6, (y - 2) % 6 if y < 7 else (y - 9) % 6
                col = GLASS[4] if (px == 0 or py == 0) else GLASS[1] if (px == 4 or py == 4) else GLASS[2]
                if px + py == 3:
                    col = GLASS[3]
            c.put(x, y, col)
    return c.img


def hatch():
    """A deck grating: birch bars every four pixels, lit along the top and left, over a dark hold."""
    c = _Canvas()
    for y in range(16):
        for x in range(16):
            if x % 4 == 0 or y % 4 == 0:
                col = BIRCH[3] if (x % 4 == 0 and y % 4 != 0) else BIRCH[2]
            elif x % 4 == 3 or y % 4 == 3:
                col = (34, 24, 14)
            else:
                col = (24, 17, 10)
            c.put(x, y, col)
    return c.img


def barrel():
    """Barrel staves four pixels wide, each lit along its left, with two iron hoops low in the tile (a keg stands on
    the deck, its ten pixels on the tile's lower rows)."""
    c = _Canvas()
    for y in range(16):
        for x in range(16):
            k = x % 4
            col = STAVES[0] if k == 3 else STAVES[3] if k == 0 else STAVES[2 if (x // 4) % 2 else 1]
            if y in (7, 13):
                col = IRON_DARK[4]
            elif y in (8, 14):
                col = IRON_DARK[2]
            c.put(x, y, col)
    return c.img


def barrel_top():
    """A barrel's head: boards across inside a dark rim."""
    c = _painted(bs.planks(STAVES, 6105, boards=4))
    for i in range(16):
        for x, y in ((i, 0), (0, i)):
            c.put(x, y, STAVES[0])
        for x, y in ((i, 15), (15, i)):
            c.put(x, y, IRON_DARK[1])
    return c.img


def flag_picture():
    """The black flag, stretched once over each side: a skull with square sockets (docs/ART_DIRECTION.md, creatures
    and faces) over crossed bones, in cream bone lit along the top and left."""
    c = _painted(bs.cloth(FLAG_BLACK, 6111))
    for y in range(2, 8):  # cranium
        for x in range(4, 12):
            if (x, y) in ((4, 2), (11, 2)):
                continue
            col = BONE[3] if y == 2 or x == 4 else BONE[1] if x == 11 or y == 7 else BONE[2]
            c.put(x, y, col)
    for y in (8, 9):  # jaw and teeth
        for x in range(5, 11):
            c.put(x, y, BONE[2] if y == 8 else BONE[1])
    for x in (6, 8):
        c.put(x, 9, SOCKET[0])
    for x0 in (5, 9):  # square sockets, a quarter of the face wide, darkest under the brow
        for y in (4, 5):
            for x in (x0, x0 + 1):
                c.put(x, y, SOCKET[0] if y == 4 else SOCKET[1])
    for t in range(11):  # crossed bones under the skull
        for x, y in ((2 + t, 11 + t // 3), (13 - t, 11 + t // 3)):
            c.put(x, y, BONE[2] if t % 2 else BONE[3])
    for x, y in ((1, 10), (2, 10), (1, 11), (13, 10), (14, 10), (14, 11), (1, 14), (2, 15), (14, 14), (13, 15)):
        c.put(x, y, BONE[3])
    return c.img


def draw_all(save):
    save(planks(DARK_OAK, 6101), "block", HULL)
    save(planks(TARRED, 6102), "block", TAR)
    save(bulwark(6103), "block", BULWARK)
    save(hull_side(6101), "block", HULL_SIDE)
    save(planks(BIRCH, 6106, vertical=True), "block", DECK)
    save(planks(RED_PAINT, 6107), "block", RED)
    save(_painted(bs.streaks(STRIPPED, 6108, vertical=True)).img, "block", TRIM)
    save(_painted(bs.streaks(STRIPPED, 6109, vertical=True)).img, "block", SPAR)
    save(_painted(bs.streaks(STRIPPED, 6109, vertical=False)).img, "block", SPAR_H)
    save(spar_end(), "block", SPAR_END)
    save(canvas(GREY_WOOL, 6112), "block", CANVAS)
    save(canvas(LIGHT_WOOL, 6113), "block", CANVAS_LIGHT)
    save(canvas(RED_WOOL, 6114), "block", CANVAS_RED)
    save(patch(CYAN_WOOL, 6115), "block", PATCH_CYAN)
    save(patch(GREEN_WOOL, 6116), "block", PATCH_GREEN)
    save(patch(BROWN_WOOL, 6117), "block", PATCH_BROWN)
    save(rope(), "block", ROPE)
    save(_painted(bs.cloth(IRON_DARK, 6118)).img, "block", IRON)
    save(_painted(bs.cloth(SOCKET + [SOCKET[2]], 6119)).img, "block", BORE)
    save(_painted(bs.metal(GOLD_BARS, 6120)).img, "block", GOLD)
    save(lamp(), "block", LAMP)
    save(window(), "block", WINDOW)
    save(hatch(), "block", HATCH)
    save(barrel(), "block", BARREL)
    save(barrel_top(), "block", BARREL_TOP)
    save(flag_picture(), "block", FLAG)
    save(_painted(bs.cloth(FLAG_BLACK, 6121)).img, "block", FLAG_EDGE)


TEXTURES = [HULL, HULL_SIDE, TAR, BULWARK, DECK, RED, TRIM, SPAR, SPAR_H, SPAR_END, CANVAS, CANVAS_LIGHT, CANVAS_RED,
            PATCH_CYAN, PATCH_GREEN, PATCH_BROWN, ROPE, IRON, BORE, GOLD, LAMP, WINDOW, HATCH, BARREL, BARREL_TOP,
            FLAG, FLAG_EDGE]


if __name__ == "__main__":
    # Writes only this file's assets (the full generators call write_all and draw_all with everything else).
    from pathlib import Path
    root = Path(__file__).resolve().parents[1] / "src" / "main" / "resources" / "assets" / MOD

    def save(img, kind, name):
        path = root / "textures" / kind / f"{name}.png"
        path.parent.mkdir(parents=True, exist_ok=True)
        img.save(path, optimize=True)
    draw_all(save)
    write_all(None, root, None, {}, None)
    for problem in standing_clear(json.loads((root / "pirate_ship_quads.json").read_text(encoding="utf-8"))):
        print("in a standing crewman:", *problem)
