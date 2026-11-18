"""The pirate ship's model and textures (docs/features/pirate-ships.md, docs/features/pirate-ship-crews.md): a
two-masted brig in warm, aged wood under cream canvas, crewed by plunderers at the stations of
menagerie/PirateCrew.STATIONS.

Frame (the same as CrewedVessel): ship-local, x to port, y up from the waterline, z to the bow; the origin is the
entity's position, at the middle of the hull on the waterline, and yaw 0 faces +z. The model is exported unrotated in
pixels (16 a block); client/PirateShipRenderer turns it with the ship and moves its parts.

Fixed by the crew interface and kept here exactly: beam 6 (x -3..3) and length 16 (z -8..8) at the waterline, the deck
at y 1.0, the waist rail at y 2.0, the keel about 1.6 below the waterline, the main mast at z 1 up to y 12 and the fore
mast at z 5 up to y 10, gun ports in the bulwark at y 1.4..1.9 centred at z 2.5 and -2.0 on each side, the main top's
floor (the lookout's) at y 9.5 and the walkable deck x -2.5..2.5, y 1..3, z -7.5..7.5. Everything drawn stays inside
PirateWaters' clearance: under the waterline the 6 x 16 hull, above it the rig box (x within 4, z -9.5..11.75, 13 up),
with every part in every pose, on the swell and heeled.

The 14 November 2026 refinement (the owner rejected busy pinstriped timber and a ship reading as "a table of parcels"):
- **Form.** A sheer rising in steps to a quarterdeck and poop aft and a forecastle forward, a fine bow with a raked stem
  and a broad transom, a modelled wale and rail cap following it; fewer, larger masses.
- **Material.** Warm aged oak in boards two to a block with soft seams one tone down and sparse long grain (no thin
  repeating lines, no butt joints repeating every block); honey pine spars; cream canvas with one seam a block; antique
  brass. 1 texel to a model pixel: every long rotated spar or rope is cut into lengths of at most 16 pixels.
- **Sails and rigging.** Four square sails (courses and topsails) and a jib. Each square sail is three parts drawn about
  its yard: the yard (with its lifts, which turn with it), the canvas and the furled bundle. The renderer braces the yard
  round its own mast (PirateMotion.brace, within the sail's brace limit), sways the canvas, and sets or furls it (the
  canvas gathers up to the yard as the bundle grows). The sheets and braces are drawn by the renderer as rope between
  points that move with the sails (rig "ropes"), so every rope is fast at both ends in every pose. The jib sets and
  furls about the bowsprit's tip, its luff along the stay.
- **Flag** on a staff above the main truck, turned by the renderer to stream downwind.

Parts (assets/jugcraft/pirate_ship_quads.json, through DecorQuads; each moving part about its own pivot):
pirate_ship_hull; pirate_ship_yard_X, pirate_ship_sail_X and pirate_ship_furl_X for X in main, main_top, fore and
fore_top; pirate_ship_jib and pirate_ship_furl_jib; pirate_ship_flag; pirate_ship_wheel; pirate_ship_rudder;
pirate_ship_cannon (one gun about its trunnion pointing +z, drawn at each gun port); pirate_ship_rope (one 16-pixel length
of rope along +z, which the renderer lays end to end between two points). assets/jugcraft/pirate_ship_rig.json carries
the pivots, brace axes and limits, the ropes, the guns' trunnions, the culling box and the stations.

The crew stand at their seats with their feet exactly at the seat's height (a 0.6 x 1.95 figure): nothing drawn, in any
swept pose, reaches inside any of the ten figures (standing_clear checks it).
"""
import json
import math
import random

import numpy as np
from PIL import Image

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
# role but the gunners'. A natural ship mans 0, 3, 5, 6 and 7 (PirateCrew.SMALL_CREW_ORDER): three to five of them.
STATIONS = [
    ("HELM", (0.0, 1.0, -6.0), 0.0, 0.0, (0.0, 1.0, -6.0)),
    ("LOOKOUT", (0.0, 9.5, 1.4), 0.0, 180.0, (0.0, 9.5, 1.4)),
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
SMALL_CREW_ORDER = [0, 3, 5, 6, 7]

# ------------------------------------------------------------------ textures

HULL, HULL_SIDE, TAR, BULWARK, WALE = "ps_hull", "ps_hull_side", "ps_hull_tar", "ps_bulwark", "ps_wale"
DECK, TRIM, RED = "ps_deck", "ps_trim", "ps_red_paint"
SPAR, SPAR_H, SPAR_END = "ps_spar", "ps_spar_h", "ps_spar_end"
CANVAS, CANVAS_LIGHT, FURLED, PATCH = "ps_canvas", "ps_canvas_light", "ps_canvas_furled", "ps_patch_brown"
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

DECK_PX = 16
# The hull's half-width at the deck, slice by slice from the transom (-z) to the stem (+z). Breaks fall where the rail
# steps (z -112, -72, 80, 112) and never across a gun port (z 34..46 and -38..-26).
SLICES = [(-128, -124, 36), (-124, -116, 41), (-116, -112, 44), (-112, -104, 46), (-104, -88, 47), (-88, -72, 48),
          (-72, 48, 48), (48, 60, 47), (60, 70, 45), (70, 80, 42), (80, 90, 38), (90, 100, 33), (100, 108, 28),
          (108, 112, 24), (112, 116, 21), (116, 120, 17), (120, 124, 11), (124, 128, 6)]


def rail_height(z0, z1):
    """The bulwark's top over a slice (pixels): the waist at y 2.0, the quarterdeck 2.5 and the poop 2.75 aft, the
    forecastle 2.375 and its head 2.625 forward: the sheer, in four steps."""
    if z1 <= -112:
        return 44
    if z1 <= -72:
        return 40
    if z0 >= 112:
        return 42
    if z0 >= 80:
        return 38
    return 32


# Below the waterline the hull narrows and its ends rake in: (bottom, top, share of the deck width, aft end, fore end).
# Everything at or under the waterline stays inside x -3..3, z -8..8 even heeled (PirateWaters' hull box).
UNDERWATER = [(-12, 0, 0.8, -114, 120), (-24, -12, 0.45, -108, 108)]
KEEL_PX = -26
# The wale: a strake standing a pixel proud of the hull's side, y 8..11, the length of the hull but the ends.
WALE_Y = (8, 11)
# A crewman standing at his seat (pixels): half his width and his height.
FIGURE_HALF, FIGURE_HEIGHT = 0.3 * PX, 1.95 * PX
# Gun ports: an opening in the bulwark 12 px long at y 1.4..1.9, centred on each gunner's z.
PORT_LOW, PORT_HIGH, PORT_HALF = GUN_PORT_Y[0] * PX, GUN_PORT_Y[1] * PX, 6
# Each gun's trunnion (pixels): the barrel's muzzle face lies MUZZLE px out from it along the barrel, at the station's
# muzzle (3.2 blocks out at rest).
TRUNNION_X, TRUNNION_Y, MUZZLE = 43.0, 25.6, 8.2
MAIN_Z, FORE_Z = MAIN_MAST[0] * PX, FORE_MAST[0] * PX
MAIN_TOP_FLOOR = STATIONS[1][1][1] * PX  # 152: the lookout's seat is the main top's floor
MAIN_HEAD, FORE_HEAD = MAIN_MAST[1] * PX, FORE_MAST[1] * PX  # 192, 160
# The topmasts' axes (z): stepped aft of the lower mast heads, the main one clear of the lookout.
MAIN_TOPMAST_Z, FORE_TOPMAST_Z = 5.0, 74.25
WHEEL_HUB = (0.0, 40.0, -83.2)
RUDDER_HINGE = (0.0, 0.0, -117.0)
# The bowsprit: foot (y, z), length and rise (degrees; the tip is higher than the foot).
BOWSPRIT_FOOT, BOWSPRIT_LENGTH, BOWSPRIT_RISE = (26.0, 112.0), 66.0, 15.7
# The flagstaff above the main truck, and the flag's pivot on it.
FLAGSTAFF = (192.0, 202.0)
FLAG_PIVOT = (0.0, 197.0, MAIN_TOPMAST_Z)


def _round(value):
    return round(value * 4) / 4


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


def line(a, b, half=0.5, texture=ROPE, plane="yz"):
    """A rope or spar from a to b (pixels), square, `half` either side of its line, cut into lengths of at most 16
    pixels so its texture keeps 1 texel a pixel. Each length is a box along +y turned about x (plane "yz": a and b share
    their x) or about z (plane "xy": they share their z), about its own start."""
    a, b = [float(v) for v in a], [float(v) for v in b]
    d = [b[k] - a[k] for k in range(3)]
    length = math.sqrt(sum(v * v for v in d))
    if plane == "yz":
        assert abs(d[0]) < 1e-9, (a, b)
        angle, axis = math.degrees(math.atan2(d[2], d[1])), "x"
    else:
        assert abs(d[2]) < 1e-9, (a, b)
        angle, axis = math.degrees(math.atan2(-d[0], d[1])), "z"
    n = max(1, math.ceil(length / PX - 1e-9))
    out = []
    for i in range(n):
        s = [a[k] + d[k] * i / n for k in range(3)]
        seg = length / n
        out.append(box((s[0] - half, s[1], s[2] - half), (s[0] + half, s[1] + seg, s[2] + half), texture,
                       rotation=(axis, angle, tuple(s))))
    return out


def gun_ports():
    """(side, z0, z1) of each opening: port (+x) and starboard (-x), at each gunner's z."""
    out = []
    for role, seat, facing, arc, muzzle in STATIONS:
        if role == "GUNNER":
            side = 1 if seat[0] > 0 else -1
            out.append((side, muzzle[2] * PX - PORT_HALF, muzzle[2] * PX + PORT_HALF))
    return out


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


def hull_body():
    """The hull: aged oak sides darkening to a wet boot-top at the waterline, over a tarred bottom; the deck; the keel,
    the raked stem with its finial, and the deadwood and sternpost the rudder hangs on."""
    m = []
    for z0, z1, w in SLICES:
        m.append(box((-w, 0, z0), (w, DECK_PX, z1), {"*": HULL_SIDE, "up": DECK, "down": TAR}))
    for y0, y1, scale, z_min, z_max in UNDERWATER:
        for z0, z1, half in _layer_slices(scale, z_min, z_max):
            m.append(box((-half, y0, z0), (half, y1, z1), TAR))
    m.append(box((-2, KEEL_PX, -117), (2, -24, 114), TAR))
    # The stem, raked forward step by step to its head above the forecastle, and its brass finial.
    for y0, y1, z0, z1 in ((-24, -12, 108, 114), (-12, 0, 120, 126), (3, 46, 128, 132)):
        m.append(box((-2, y0, z0), (2, y1, z1), {"*": TAR if y1 <= 0 else HULL, "up": TRIM}))
    m.append(box((-1.5, 46, 128.5), (1.5, 49, 131.5), GOLD))
    m.append(box((-2.5, -24, -114), (2.5, -12, -108), TAR))
    m.append(box((-2.5, -24, -117), (2.5, 0, -114), TAR))
    return m


def wales():
    """The wale, a pixel proud of the side along the hull but its ends, stepping in with the slices."""
    m = []
    for z0, z1, w in SLICES:
        if z0 < -120 or z1 > 116:
            continue
        m.append(box((w, WALE_Y[0], z0), (w + 1, WALE_Y[1], z1), WALE))
        m.append(box((-w - 1, WALE_Y[0], z0), (-w, WALE_Y[1], z1), WALE))
    return m


def bulwarks():
    """Bulwarks round the deck rising with the sheer, a rail cap along their tops, the gun ports cut through them with
    their lids propped open, the transom with the cabin windows and the stern lantern, the bow's breastwork, and the
    channels the shrouds come down to."""
    m = []
    ports = gun_ports()
    for z0, z1, w in SLICES:
        top = rail_height(z0, z1)
        for side in (1, -1):
            x0, x1 = (w - 2, w) if side > 0 else (-w, -w + 2)
            outer = "east" if side > 0 else "west"
            texture = {"*": HULL, outer: BULWARK, "up": TRIM}
            cuts = sorted((p0, p1) for s, p0, p1 in ports if s == side and z0 < p0 and p1 < z1)
            start = z0
            for p0, p1 in cuts:
                m.append(box((x0, DECK_PX, start), (x1, top, p0), texture))
                m.append(box((x0, DECK_PX, p0), (x1, PORT_LOW, p1), texture))
                m.append(box((x0, PORT_HIGH, p0), (x1, top, p1), texture))
                start = p1
            m.append(box((x0, DECK_PX, start), (x1, top, z1), texture))
            # The rail cap: a pixel and a half of lighter oak, overhanging the bulwark outboard by half a pixel.
            c0, c1 = (w - 2.25, w + 0.5) if side > 0 else (-w - 0.5, -w + 2.25)
            m.append(box((c0, top, z0), (c1, top + 1.5, z1), TRIM))
    # Where the hull steps in, a wall across the step joins the wider slice's bulwark to the narrower one's.
    for (a0, a1, wa), (b0, b1, wb) in zip(SLICES, SLICES[1:]):
        if wa == wb:
            continue
        wide, narrow = max(wa, wb), min(wa, wb)
        z0, z1 = (a1 - 2, a1) if wa > wb else (b0, b0 + 2)
        top = max(rail_height(a0, a1), rail_height(b0, b1))
        for side in (1, -1):
            x0, x1 = (narrow - 2, wide) if side > 0 else (-wide, -narrow + 2)
            m.append(box((x0, DECK_PX, z0), (x1, top, z1), {"*": HULL, "up": TRIM}))
    # Gun port lids, hinged at the port's head and propped open 45 degrees, red ochre inside.
    for side, p0, p1 in ports:
        hinge = 48.0 * side
        frm, to = ((48, PORT_HIGH, p0 + 0.5), (49, PORT_HIGH + 8, p1 - 0.5)) if side > 0 else \
            ((-49, PORT_HIGH, p0 + 0.5), (-48, PORT_HIGH + 8, p1 - 0.5))
        inner = "west" if side > 0 else "east"
        m.append(box(frm, to, {"*": HULL, inner: RED}, rotation=("z", -45 * side, (hinge, PORT_HIGH, 0))))
    # The transom: its taffrail at the poop's height, three cabin windows in brass and the stern lantern on its post.
    m.append(box((-36, DECK_PX, -128), (36, 44, -126), {"*": HULL, "north": BULWARK, "up": TRIM}))
    m.append(box((-36.5, 44, -128.5), (36.5, 45.5, -125.5), TRIM))
    for x in (-20, 0, 20):
        m.append(box((x - 5, 24, -128.5), (x + 5, 34, -128), {"*": GOLD, "north": f"{WINDOW}!"}))
    m.append(box((-1, 45.5, -128), (1, 50, -126), GOLD))
    m.append(box((-3, 50, -130), (3, 58, -124), {"*": f"{LAMP}!", "up": GOLD, "down": GOLD}))
    m.append(box((-2, 58, -129), (2, 60, -125), GOLD))
    m.append(box((-6, DECK_PX, 125), (6, 42, 128), {"*": HULL, "south": BULWARK, "up": TRIM}))
    # Channels outside the rail, where the shrouds come down.
    for z0, z1, w in ((0, 16, 48), (64, 80, 44)):
        m.append(box((w, 27, z0), (w + 3, 30, z1), {"*": HULL, "up": TRIM}))
        m.append(box((-w - 3, 27, z0), (-w, 30, z1), {"*": HULL, "up": TRIM}))
    return m


def lower_masts():
    """The main and fore lower masts (from, to), pixels: square spars from the deck to their tops."""
    return [((-3.5, DECK_PX, MAIN_Z - 3.5), (3.5, MAIN_TOP_FLOOR - 4, MAIN_Z + 3.5)),
            ((-3.0, DECK_PX, FORE_Z - 3.0), (3.0, 120, FORE_Z + 3.0))]


def topmasts():
    """The main and fore topmasts (from, to), stepped aft of the lower mast heads, up to their trucks."""
    return [((-2, MAIN_TOP_FLOOR - 8, MAIN_TOPMAST_Z - 2), (2, MAIN_HEAD - 2, MAIN_TOPMAST_Z + 2)),
            ((-1.75, 118, FORE_TOPMAST_Z - 1.75), (1.75, FORE_HEAD - 2, FORE_TOPMAST_Z + 1.75))]


def masts():
    """Two masts of honey pine: lower masts with tops on trestles and topmasts stepped aft of their heads, the main top
    a railed crow's nest round the lookout's seat, the trucks, the flagstaff, and the bowsprit over the stem."""
    floor = MAIN_TOP_FLOOR
    m = [box(*lower_masts()[0], SPAR_Y)]
    m.append(box((-4, floor - 10, 2), (4, floor - 4, 23), HULL))  # trestle trees under the top
    m.append(box((-12, floor - 4, 2), (12, floor, 30), {"*": HULL, "up": DECK}))
    rail = {"*": HULL, "up": TRIM}
    m.append(box((10, floor, 2), (12, floor + 8, 30), rail))
    m.append(box((-12, floor, 2), (-10, floor + 8, 30), rail))
    m.append(box((-10, floor, 28), (10, floor + 8, 30), rail))
    m.append(box((-10, floor, 2), (10, floor + 8, 4), rail))
    main_top, fore_top = topmasts()
    m.append(box(*main_top, SPAR_Y))
    m.append(box((-2.5, MAIN_HEAD - 2, MAIN_TOPMAST_Z - 2.5), (2.5, MAIN_HEAD, MAIN_TOPMAST_Z + 2.5), GOLD))  # truck, y 12
    m.append(box((-0.75, FLAGSTAFF[0], MAIN_TOPMAST_Z - 0.75), (0.75, FLAGSTAFF[1], MAIN_TOPMAST_Z + 0.75), SPAR_Y))
    m.append(box(*lower_masts()[1], SPAR_Y))
    m.append(box((-3.5, 116, 70), (3.5, 120, 86), HULL))
    m.append(box((-9, 120, 70), (9, 123, 88), {"*": HULL, "up": DECK}))
    m.append(box(*fore_top, SPAR_Y))
    m.append(box((-2.25, FORE_HEAD - 2, FORE_TOPMAST_Z - 2.25), (2.25, FORE_HEAD, FORE_TOPMAST_Z + 2.25), GOLD))  # y 10
    y, z = BOWSPRIT_FOOT
    tip = bowsprit_tip()
    m += line((0, y, z), (0, tip[0], tip[1]), half=2.5, texture=SPAR_Z)
    return m


def bowsprit_tip():
    y, z = BOWSPRIT_FOOT
    rad = math.radians(BOWSPRIT_RISE)
    return y + BOWSPRIT_LENGTH * math.sin(rad), z + BOWSPRIT_LENGTH * math.cos(rad)


# The forestay runs from the fore top's fore edge down to the bowsprit's tip, clear in front of the fore course and below
# the fore topsail; the jib is set under it.
FORESTAY_HEAD = (121.0, FORE_Z + 6.0)
# The standing rigging as (from, to) in pixels, each fast at both ends: the shrouds from the channels to under each
# lower mast's top, the topmast shrouds from the tops' rims to under the trucks beside the topmasts, the forestay from the
# bowsprit's tip to the fore top, and the main stay from the main top down to the foremast.
SHROUDS = [((49.5, 4.0, MAIN_TOP_FLOOR - 10), (3.0, 9.0)), ((45.5, 3.5, 116.0), (67.0, 73.0))]
TOPMAST_SHROUDS = [((11.0, MAIN_TOP_FLOOR), (2.0, MAIN_HEAD - 6), (MAIN_TOPMAST_Z - 1.0, MAIN_TOPMAST_Z + 1.0)),
                   ((8.5, 123.0), (1.75, FORE_HEAD - 6), (FORE_TOPMAST_Z - 1.0, FORE_TOPMAST_Z + 1.0))]
# The main stay: from under the main top's fore edge down to the foremast, clear of the main course and its yard braced.
MAIN_STAY = ((0.0, MAIN_TOP_FLOOR - 4, 29.0), (0.0, 50.0, FORE_Z - 3.0))


def standing_lines():
    """The standing rigging as segments (pixels): every shroud, topmast shroud and stay, as rigging() draws them."""
    segs = []
    for (base_x, top_x, top_y), zs in SHROUDS:
        for side in (1, -1):
            for z in zs:
                segs.append(((base_x * side, 30.0, z), (top_x * side, top_y, z)))
    for (rim_x, rim_y), (head_x, head_y), zs in TOPMAST_SHROUDS:
        for side in (1, -1):
            for z in zs:
                segs.append(((rim_x * side, rim_y, z), (head_x * side, head_y, z)))
    tip = bowsprit_tip()
    segs.append(((0.0, tip[0], tip[1]), (0.0, FORESTAY_HEAD[0], FORESTAY_HEAD[1])))
    segs.append(MAIN_STAY)
    return segs


def rigging():
    """The standing rigging (standing_lines), with ratlines across each pair of lower shrouds in their plane."""
    m = []
    for a, b in standing_lines():
        m += line(a, b, plane="xy" if abs(a[2] - b[2]) < 1e-9 and abs(a[0] - b[0]) > 1e-9 else "yz")
    for (base_x, top_x, top_y), zs in SHROUDS:
        for side in (1, -1):
            x0, x1 = base_x * side, top_x * side
            length = math.hypot(x1 - x0, top_y - 30)
            angle = math.degrees(math.atan2(-(x1 - x0), top_y - 30))
            t = 16
            while t < length - 10:  # ratlines across the pair, their ends against the shrouds left out
                m.append(box((x0 - 0.35, 30 + t - 0.35, zs[0] + 0.5), (x0 + 0.35, 30 + t + 0.35, zs[1] - 0.5),
                             {"*": ROPE, "north": None, "south": None}, rotation=("z", angle, (x0, 30, 0))))
                t += 16
    return m


def deck_fittings():
    """The helm's wheel stand, the main hatch, the capstan, the anchors at their catheads and a few barrels, all clear
    of where the crew stand."""
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
    """The hull part: everything that does not move."""
    return hull_body() + wales() + bulwarks() + masts() + rigging() + deck_fittings()


# ------------------------------------------------------------------ sails

def _rows(top, bottom, head_half, foot_half, head_front, foot_front, step=16):
    """A sail's canvas as bands `step` pixels tall from its head (`top`) down to its foot: (top, bottom, half-width,
    front z at the band's foot), wider toward the foot. The canvas is one flat sheet raked forward from the head to the
    foot (canvas_boxes turns it about the head line), so the fronts are the sheet's, not steps."""
    rows = []
    y = top
    n = math.ceil((top - bottom) / step)
    for i in range(n):
        y1 = max(bottom, y - step)
        t = (i + 1) / n
        half = round(head_half + (foot_half - head_half) * t)
        front = head_front + (foot_front - head_front) * (top - y1) / (top - bottom)
        rows.append((y, y1, half, front))
        y = y1
    return rows


def rake(name):
    """How far a sail's canvas is raked forward from its head (degrees): the wind filling it."""
    rows = SAILS[name]["rows"]
    top, foot = rows[0][0], rows[-1][1]
    head_front = SAILS[name]["head_front"]
    return math.degrees(math.atan2(rows[-1][3] - head_front, top - foot))


# Each square sail: its yard (from, to; pixels, ship frame), the axis it is braced about (the z of its mast's axis), the
# half-width of its head, the front z of its canvas at the head, the canvas as bands of (top, bottom, half-width, front z)
# hanging from the yard (one sheet raked forward, wider toward the foot), and its canvas texture. The pivot (each of its
# parts' origin) is the yard's middle.
SAILS = {
    "main": {"yard": ((-46, 134, 19.5), (46, 137, 22.5)), "axis": MAIN_Z, "head_half": 42,
             "head_front": 23.0, "rows": _rows(136, 72, 42, 45, 23.0, 27.0),
             "canvas": CANVAS, "patch": ((-24, 105), (-14, 111))},
    "main_top": {"yard": ((-32, 186, 7), (32, 188.5, 9.5)), "axis": MAIN_TOPMAST_Z, "head_half": 28,
                 "head_front": 10.0, "rows": _rows(188, 168, 29, 31, 10.0, 11.6, step=10), "canvas": CANVAS_LIGHT,
                 "furl_on_top": True},
    "fore": {"yard": ((-40, 108, 83), (40, 111, 86)), "axis": FORE_Z, "head_half": 36,
             "head_front": 86.6, "rows": _rows(110, 64, 36, 38, 86.6, 89.6), "canvas": CANVAS},
    "fore_top": {"yard": ((-28, 152, 76), (28, 154.5, 78.5)), "axis": FORE_TOPMAST_Z, "head_half": 22,
                 "head_front": 79.4, "rows": _rows(154, 128, 23, 25, 79.4, 81.2, step=13), "canvas": CANVAS_LIGHT,
                 "furl_on_top": True},
}
SQUARE = ["main", "main_top", "fore", "fore_top"]
# Where each yard's lifts start: on its brace axis, just under its mast's head (a lift turns with its yard, both ends
# on the yard's turn).
LIFT_HEAD = {"main": MAIN_TOP_FLOOR - 6, "main_top": MAIN_HEAD - 4, "fore": 116.0, "fore_top": FORE_HEAD - 4}


def sail_pivot(name):
    (x0, y0, z0), (x1, y1, z1) = SAILS[name]["yard"]
    return ((x0 + x1) / 2, (y0 + y1) / 2, (z0 + z1) / 2)


def yard_boxes(name):
    """A yard, ship pixels: the spar, and the lifts from the mast's head (on the brace axis) to each yardarm."""
    spec = SAILS[name]
    (x0, y0, z0), (x1, y1, z1) = spec["yard"]
    m = [box((x0, y0, z0), (x1, y1, z1), SPAR_X)]
    mid_z = (z0 + z1) / 2
    for side in (1, -1):
        arm = x1 - 2 if side > 0 else x0 + 2
        m += _lift((0.0, LIFT_HEAD[name], spec["axis"]), (arm, y1, mid_z))
    return m


def _lift(head, arm):
    """A lift from a mast head down to a yardarm, in two straight lengths in axis-aligned planes: out along the yard's
    line (x-y plane, at the head's z) to above the yardarm, then forward down to it (y-z plane, at the arm's x)."""
    hx, hy, hz = head
    ax, ay, az = arm
    knee = (ax, ay + (hy - ay) * 0.35, hz)
    out = line(head, knee, plane="xy")
    if abs(knee[2] - az) > 1e-9:
        out += line(knee, (ax, ay, az), plane="yz")
    else:
        out += line(knee, (ax, ay, az), plane="xy")
    return out


def canvas_boxes(name):
    """A square sail's canvas, ship pixels: one sheet a pixel thick hanging from inside its yard, raked forward from the
    head (rake()), in tiles no more than 16 pixels each way (so its texture keeps 1 texel a pixel), wider band by band
    toward the foot; the tiles' shared edges are left undrawn (they meet inside the sheet), so nothing between them is
    coplanar. The main course carries one patch, sewn on its front."""
    spec = SAILS[name]
    front = spec["head_front"]
    top = spec["rows"][0][0]
    turn = ("x", -rake(name), (0.0, top, front))
    m = []
    rows = spec["rows"]
    for i, (y0, y1, half, _) in enumerate(rows):
        edges = sorted({-half, half, *[x for x in range(-160, 161, 16) if -half < x < half]})
        for x0, x1 in zip(edges, edges[1:]):
            faces = {"*": spec["canvas"]}
            if x0 > -half:
                faces["west"] = None
            if x1 < half:
                faces["east"] = None
            # Up and down faces show only where this band is wider than its neighbour (the leech's step) or at head and foot.
            above = rows[i - 1][2] if i > 0 else None
            below = rows[i + 1][2] if i + 1 < len(rows) else None
            if above is not None and max(abs(x0), abs(x1)) <= above:
                faces["up"] = None
            if below is not None and max(abs(x0), abs(x1)) <= below:
                faces["down"] = None
            m.append(box((x0, y1, front - 1), (x1, y0, front), faces, rotation=turn))
    if spec.get("patch"):
        (x0, y0), (x1, y1) = spec["patch"]
        m.append(box((x0, y0, front), (x1, y1, front + 0.5), {"*": PATCH, "south": f"{PATCH}!"}, rotation=turn))
    return m


def furl_boxes(name):
    """A square sail furled on its yard, ship pixels: the canvas rolled into a bundle on the yard's top-front, a little
    shorter than the yard, bound with gaskets every 12 pixels."""
    spec = SAILS[name]
    (x0, y0, z0), (x1, y1, z1) = spec["yard"]
    half = spec["head_half"]
    r = 2.5
    if spec.get("furl_on_top"):
        cy, cz = y1 + r - 0.5, z0 + r + 2.0  # on the yard's top, forward of its mast and truck, over the lookout
    else:
        cy, cz = y0 - 0.5, z1 + r - 0.5  # on the yard's front
    m = [box((-half, cy - r, cz - r), (half, cy + r, cz + r), FURLED)]
    x = -half + 6
    while x <= half - 6:
        m.append(box((x - 0.5, cy - r - 0.5, cz - r - 0.5), (x + 0.5, cy + r + 0.5, cz + r + 0.5), ROPE))
        x += 12
    return m


def _about(elements, pivot):
    return _shift(elements, -pivot[0], -pivot[1], -pivot[2])


def _forestay_z(y):
    (ty, tz), (hy, hz) = bowsprit_tip(), FORESTAY_HEAD
    return tz + (y - ty) * (hz - tz) / (hy - ty)


JIB_CLEW_Y, JIB_HEAD_Y, JIB_CLEW_Z = 50.0, 106.0, 122.0


def jib_boxes():
    """The jib, ship pixels: set under the forestay in steps from the clew over the forecastle up to its head, its luff
    two pixels under the stay."""
    m = []
    head_z = _forestay_z(JIB_HEAD_Y) - 2
    y0 = JIB_CLEW_Y
    while y0 < JIB_HEAD_Y:
        y1 = y0 + 8
        back = _round(JIB_CLEW_Z + ((y0 + y1) / 2 - JIB_CLEW_Y) * (head_z - JIB_CLEW_Z) / (JIB_HEAD_Y - JIB_CLEW_Y))
        front = _round(min(_forestay_z(y1), _forestay_z(y0)) - 2)
        if front - back < 3:
            break
        m.append(box((-0.5, y0, back), (0.5, y1, front), CANVAS_LIGHT))
        y0 = y1
    return m


def jib_pivot():
    tip_y, tip_z = bowsprit_tip()
    return (0.0, tip_y, tip_z)


def jib_furl_boxes():
    """The jib furled: rolled along the bowsprit's top just inboard of its tip, with two gaskets."""
    tip_y, tip_z = bowsprit_tip()
    rad = math.radians(BOWSPRIT_RISE)
    start = (0.0, tip_y - 6 * math.sin(rad) + 4.0, tip_z - 6 * math.cos(rad))
    end = (0.0, tip_y - 30 * math.sin(rad) + 4.0, tip_z - 30 * math.cos(rad))
    m = line(start, end, half=1.75, texture=FURLED)
    for t in (0.3, 0.75):
        p = [start[k] + (end[k] - start[k]) * t for k in range(3)]
        q = [start[k] + (end[k] - start[k]) * (t + 0.04) for k in range(3)]
        m += line(p, q, half=2.25, texture=ROPE)
    return m


def flag():
    """The black flag with skull and crossbones, flying aft from the flagstaff above the main truck (about its pivot on
    the staff, so the renderer turns it downwind about the staff)."""
    z1 = FLAG_PIVOT[2] - 0.75
    m = [box((-0.5, 193, z1 - 14.0), (0.5, 202, z1), {"*": FLAG_EDGE, "east": f"{FLAG}!", "west": f"{FLAG}!"})]
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
    """The rudder about its hinge on the sternpost (turning about y): a blade wider below, under the overhanging stern
    (its head reaches up into the hull), with iron pintle straps."""
    m = [box((-1.5, -22, -10.5), (1.5, -4, 0), {"*": HULL, "down": TAR}),
         box((-1.5, -4, -7), (1.5, 2, 0), HULL)]
    for y in (-16, -7):
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
    """A gun's carriage about the trunnions, pointing along +z: red ochre cheeks stepped down to the rear, a bed and
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
    """One gun about its trunnion, pointing +z: the renderer draws it at each gun port turned by -facing."""
    return cannon_carriage() + cannon_barrel()


def trunnions():
    """Each gun's trunnion (ship pixels) and its station's index and facing, in PirateCrew.STATIONS order."""
    out = []
    for index, (role, seat, facing, arc, muzzle) in enumerate(STATIONS):
        if role == "GUNNER":
            side = 1 if seat[0] > 0 else -1
            out.append((index, (TRUNNION_X * side, TRUNNION_Y, muzzle[2] * PX), facing))
    return out


def rope_unit():
    """One length of rope, 16 pixels along +z from the origin, a pixel square: the renderer lays these end to end."""
    return [box((-0.5, -0.5, 0), (0.5, 0.5, 16), ROPE)]


# ------------------------------------------------------------------ moving parts and poses

# The renderer's motion (client/PirateShipRenderer, menagerie/PirateMotion): how far each part swings about its own
# axis (degrees either way), the swell's roll and pitch with the most heel added, and the heave (pixels).
SWAY = 3.0
FLAG_FLUTTER = 8.0
ROLL, PITCH, HEAVE = 0.5 + 0.4, 0.2, 0.2
WHEEL_TURN, RUDDER_TURN = 120.0, 25.0
BRACE_MAX = 40.0
# The most each square sail is braced (degrees): within this its yard, canvas, bundle and running rigging keep clear of
# the shrouds, stays, masts, tops and crew in every pose (brace_clear and standing_clear check it).
# Found by brace_clear (the largest clear: main 22, main topsail 30, fore 24, fore topsail 22); one limit for all four,
# so the sails stand braced alike.
BRACE_LIMITS = {"main": 22.0, "main_top": 22.0, "fore": 22.0, "fore_top": 22.0}


def _rot(axis, degrees):
    """A 4 x 4 right-handed rotation, as PoseStack.rotateDegrees(Axis.XP/YP/ZP, degrees)."""
    a = math.radians(degrees)
    c, s = math.cos(a), math.sin(a)
    m = np.eye(4)
    if axis == "x":
        m[1:3, 1:3] = [[c, -s], [s, c]]
    elif axis == "y":
        m[0, 0], m[0, 2], m[2, 0], m[2, 2] = c, s, -s, c
    else:
        m[0:2, 0:2] = [[c, -s], [s, c]]
    return m


def _move(x, y, z):
    m = np.eye(4)
    m[:3, 3] = (x, y, z)
    return m


def _scale(x, y, z):
    return np.diag([x, y, z, 1.0])


def sail_frame(name, brace):
    """A yard's transform: to its brace axis at the yard's height, braced (within the sail's limit), then out to the
    yard's middle."""
    px, py, pz = sail_pivot(name)
    limit = BRACE_LIMITS[name]
    b = max(-limit, min(limit, brace))
    axis = SAILS[name]["axis"]
    return _move(0.0, py, axis) @ _rot("y", b) @ _move(px, 0.0, pz - axis)


def frames(brace=0.0, sway=0.0, set_=1.0, flag_turn=0.0, helm=0.0):
    """Each part's transforms (4 x 4, ship pixels) in one pose, exactly as client/PirateShipRenderer draws it:
    {part: [matrices]} (the gun at each port), an empty list for a part not drawn in that pose. `brace` is the yards'
    brace before each sail's limit, `set_` how far the sails are set (0 furled, 1 set), `helm` the rudder (-1..1)."""
    out = {"pirate_ship_hull": [np.eye(4)]}
    for name in SQUARE:
        yard = sail_frame(name, brace)
        out[f"pirate_ship_yard_{name}"] = [yard]
        out[f"pirate_ship_sail_{name}"] = [yard @ _rot("x", sway) @ _scale(1.0, set_, 1.0)] if set_ > SHOWN else []
        out[f"pirate_ship_furl_{name}"] = [yard @ _scale(1 - set_, 1 - set_, 1 - set_)] if 1 - set_ > SHOWN else []
    tip = _move(*jib_pivot())
    out["pirate_ship_jib"] = [tip @ _scale(set_, set_, set_)] if set_ > SHOWN else []
    out["pirate_ship_furl_jib"] = [tip @ _scale(1 - set_, 1 - set_, 1 - set_)] if 1 - set_ > SHOWN else []
    out["pirate_ship_flag"] = [_move(*FLAG_PIVOT) @ _rot("y", flag_turn)]
    out["pirate_ship_wheel"] = [_move(*WHEEL_HUB) @ _rot("z", helm * WHEEL_TURN)]
    out["pirate_ship_rudder"] = [_move(*RUDDER_HINGE) @ _rot("y", helm * RUDDER_TURN)]
    out["pirate_ship_cannon"] = [_move(*point) @ _rot("y", -facing) for index, point, facing in trunnions()]
    return out


# A part scaled to this share or less is not drawn (the renderer's threshold).
SHOWN = 0.04


def _apply(m, p):
    v = m @ np.array([p[0], p[1], p[2], 1.0])
    return (float(v[0]), float(v[1]), float(v[2]))


def _clew(name, side):
    """A sail's clew (a lower corner of its canvas), raked with it, in its part's coordinates."""
    spec = SAILS[name]
    top, bottom, half, _ = spec["rows"][-1]
    head_top = spec["rows"][0][0]
    a = math.radians(-rake(name))
    y, z = bottom + 0.5 - head_top, -0.5
    # The canvas's turn about x through (head top, head front): y' = y cos - z sin, z' = y sin + z cos.
    y, z = y * math.cos(a) - z * math.sin(a) + head_top, y * math.sin(a) + z * math.cos(a) + spec["head_front"]
    p = sail_pivot(name)
    return [side * (half - 1.0) - p[0], y - p[1], z - p[2]]


def _yardarm(name, side):
    (x0, y0, z0), (x1, y1, z1) = SAILS[name]["yard"]
    p = sail_pivot(name)
    return [(x1 - 1.0 if side > 0 else x0 + 1.0) - p[0], (y0 + y1) / 2 - p[1], (z0 + z1) / 2 - p[2]]


def ropes():
    """The running rigging the renderer draws between moving points (rig "ropes"): each end a frame and a point in that
    frame's part coordinates (about its pivot); "hull" is the ship's own frame. Sheets run from the courses' clews aft
    to the rail; braces from the courses' yardarms aft, the main's to the quarterdeck rail and the fore's to the main
    channels; the topsail sheets from the topsails' clews down to the course yardarms."""
    out = []
    for side in (1, -1):
        s = "port" if side > 0 else "starboard"
        out.append({"name": f"main_sheet_{s}", "from": {"frame": "sail_main", "point": _clew("main", side)},
                    "to": {"frame": "hull", "point": [side * 47.0, 33.5, -24.0]}})
        out.append({"name": f"fore_sheet_{s}", "from": {"frame": "sail_fore", "point": _clew("fore", side)},
                    "to": {"frame": "hull", "point": [side * 46.0, 33.5, 54.0]}})
        out.append({"name": f"main_brace_{s}", "from": {"frame": "yard_main", "point": _yardarm("main", side)},
                    "to": {"frame": "hull", "point": [side * 46.0, 41.5, -100.0]}})
        out.append({"name": f"fore_brace_{s}", "from": {"frame": "yard_fore", "point": _yardarm("fore", side)},
                    "to": {"frame": "hull", "point": [side * 49.5, 30.0, 8.0]}})
        out.append({"name": f"main_topsail_sheet_{s}", "from": {"frame": "sail_main_top", "point": _clew("main_top", side)},
                    "to": {"frame": "yard_main", "point": _yardarm("main", side)}})
        out.append({"name": f"fore_topsail_sheet_{s}", "from": {"frame": "sail_fore_top", "point": _clew("fore_top", side)},
                    "to": {"frame": "yard_fore", "point": _yardarm("fore", side)}})
    return out


def rope_ends(pose_frames, spec):
    """A running rope's two ends in ship pixels in a pose (frames()). A furled sail's clew is hauled up to its yard, so
    its sheet then runs from the yard (the canvas frame scaled to nothing: the clew point at the yard's middle row)."""
    ends = []
    for end in (spec["from"], spec["to"]):
        frame = end["frame"]
        if frame == "hull":
            ends.append(tuple(float(v) for v in end["point"]))
            continue
        drawn = pose_frames.get(f"pirate_ship_{frame}")
        if not drawn and frame.startswith("sail_"):
            name = frame[5:]
            # Furled: the clew at the yard, under the bundle.
            p = end["point"]
            ends.append(_apply(pose_frames[f"pirate_ship_yard_{name}"][0], [p[0], -3.0, p[2]]))
            continue
        ends.append(_apply(drawn[0], end["point"]))
    return ends


def rope_segments(a, b):
    """How the renderer lays rope between a and b (pixels): [(matrix, length)] of 16-pixel lengths along the line, the
    last shorter, each the unit rope (pirate_ship_rope) turned by yaw then pitch and scaled along its length."""
    d = [b[k] - a[k] for k in range(3)]
    length = math.sqrt(sum(v * v for v in d))
    if length < 1e-6:
        return []
    yaw = math.degrees(math.atan2(d[0], d[2]))
    pitch = -math.degrees(math.asin(max(-1.0, min(1.0, d[1] / length))))
    base = _move(*a) @ _rot("y", yaw) @ _rot("x", pitch)
    out = []
    done = 0.0
    while done < length - 1e-6:
        seg = min(PX, length - done)
        out.append((base @ _move(0, 0, done) @ _scale(1, 1, seg / PX), seg))
        done += seg
    return out


def poses(steps=5):
    """The poses the checks sweep: braced each way and square, set, half set and furled, the canvas swayed each way."""
    out = []
    for brace in np.linspace(-BRACE_MAX, BRACE_MAX, 2 * steps + 1):
        for set_ in (1.0, 0.5, 0.0):
            for sway in (-SWAY, SWAY):
                out.append(frames(brace=float(brace), sway=sway, set_=set_))
    for turn in (-180.0, -90.0, 90.0, 180.0):
        out.append(frames(flag_turn=turn + FLAG_FLUTTER, helm=1.0))
        out.append(frames(flag_turn=turn - FLAG_FLUTTER, helm=-1.0))
    return out


# ------------------------------------------------------------------ export

PARTS = (["pirate_ship_hull"] + [f"pirate_ship_{kind}_{name}" for name in SQUARE for kind in ("yard", "sail", "furl")]
         + ["pirate_ship_jib", "pirate_ship_furl_jib", "pirate_ship_flag", "pirate_ship_wheel", "pirate_ship_rudder",
            "pirate_ship_cannon", "pirate_ship_rope"])


def _tiled(elements, offset=(0, 0, 0)):
    """zeppelin.tiled_quads with its 16-pixel grid moved by `offset` (pixels): a part exported about its pivot keeps the
    ship's own 16-pixel grid (its texture lines up with the hull's), and a small part is not cut along its pivot's
    planes for nothing."""
    dx, dy, dz = offset
    quads = tiled_quads(_shift(elements, dx, dy, dz))
    for quad in quads:
        for v in quad["vertices"]:
            v[0], v[1], v[2] = round(v[0] - dx, 4), round(v[1] - dy, 4), round(v[2] - dz, 4)
    return quads


def _grid(pivot):
    return tuple(v % PX for v in pivot)


def export():
    out = {"pirate_ship_hull": tiled_quads(hull())}
    for name in SQUARE:
        pivot = sail_pivot(name)
        g = _grid(pivot)
        out[f"pirate_ship_yard_{name}"] = _tiled(_about(yard_boxes(name), pivot), g)
        out[f"pirate_ship_sail_{name}"] = _tiled(_about(canvas_boxes(name), pivot), g)
        out[f"pirate_ship_furl_{name}"] = _tiled(_about(furl_boxes(name), pivot), g)
    g = _grid(jib_pivot())
    out["pirate_ship_jib"] = _tiled(_about(jib_boxes(), jib_pivot()), g)
    out["pirate_ship_furl_jib"] = _tiled(_about(jib_furl_boxes(), jib_pivot()), g)
    out["pirate_ship_flag"] = _tiled(flag(), (8, 4, 0.75))
    out["pirate_ship_wheel"] = _tiled(helm_wheel(), (8, 8, 8))
    out["pirate_ship_rudder"] = _tiled(rudder(), (8, 0, 12))
    out["pirate_ship_cannon"] = _tiled(cannon(), (8, 10, 7))
    out["pirate_ship_rope"] = _tiled(rope_unit(), (8, 8, 0))
    return {part: out[part] for part in PARTS}


PIVOTS = {**{f"pirate_ship_{kind}_{name}": sail_pivot(name) for name in SQUARE for kind in ("yard", "sail", "furl")},
          "pirate_ship_jib": jib_pivot(), "pirate_ship_furl_jib": jib_pivot(), "pirate_ship_flag": FLAG_PIVOT,
          "pirate_ship_wheel": WHEEL_HUB, "pirate_ship_rudder": RUDDER_HINGE}
CANNONS = [point for index, point, facing in trunnions()]
CULLING_MARGIN = 0.5


def _points(quads, part):
    return np.array([v[:3] + [1.0] for q in quads[part] for v in q["vertices"]], dtype=float)


def placed_points(quads, pose_frames, cache=None):
    """Every drawn vertex in ship pixels in one pose, ropes' ends included."""
    pts = []
    for part, matrices in pose_frames.items():
        arr = cache[part] if cache is not None else _points(quads, part)
        for m in matrices:
            pts.append((arr @ m.T)[:, :3])
    for spec in ropes():
        pts.append(np.array(rope_ends(pose_frames, spec)))
    return np.concatenate(pts)


def rig(quads=None):
    """pirate_ship_rig.json, as client/PirateShipRenderer reads it: "pivots", "axes" (each square sail's brace axis, z
    pixels), "brace_limits" (degrees), "ropes", "cannons" in pixels and "culling" in blocks, ship-local and unrotated;
    then, for the crew and the checks, the stations, the walkable deck and the fixed dimensions, in blocks."""
    quads = quads or export()
    cache = {part: _points(quads, part) for part in PARTS}
    lo, hi = np.full(3, np.inf), np.full(3, -np.inf)
    for pose_frames in poses(2):
        pts = placed_points(quads, pose_frames, cache)
        lo, hi = np.minimum(lo, pts.min(0)), np.maximum(hi, pts.max(0))
    culling = [round(float(v) / PX - CULLING_MARGIN, 3) for v in lo] + [round(float(v) / PX + CULLING_MARGIN, 3) for v in hi]
    stations = []
    for index, ((role, seat, facing, arc, muzzle), name) in enumerate(zip(STATIONS, STATION_NAMES)):
        stations.append({"index": index, "name": name, "role": role, "seat": list(seat), "facing": facing, "arc": arc,
                         "muzzle": list(muzzle)})
    return {
        "pivots": {part: [round(v, 4) for v in pivot] for part, pivot in PIVOTS.items()},
        "axes": {name: SAILS[name]["axis"] for name in SQUARE},
        "brace_limits": dict(BRACE_LIMITS),
        "ropes": [{"name": r["name"], "from": {"frame": r["from"]["frame"], "point": [round(v, 4) for v in r["from"]["point"]]},
                   "to": {"frame": r["to"]["frame"], "point": [round(v, 4) for v in r["to"]["point"]]}} for r in ropes()],
        "cannons": [[round(v, 4) for v in point] for point in CANNONS],
        "culling": culling,
        "frame": "ship-local: +x port, +y up from the waterline, +z bow; origin the entity position at the hull's middle on "
                 "the waterline; yaw 0 faces +z; facing 0 bow, +90 starboard, -90 port; unrotated (turn by -yaw). "
                 "pivots, axes, ropes and cannons are in pixels (16 a block); culling, stations, local_deck and hull in "
                 "blocks. Each part in pirate_ship_quads.json is in pixels about its pivot; pirate_ship_cannon about its "
                 "trunnion, pointing +z; pirate_ship_rope one 16-pixel length along +z. A square sail X: translate to (0, "
                 "pivot y, axes[X]), turn about y by the brace (within brace_limits[X]), translate (pivot x, 0, pivot z - "
                 "axes[X]): draw yard_X; then turn about x by the sway and scale y by the set (0..1): draw sail_X; or, "
                 "from the yard, scale by 1 - set: draw furl_X. The jib and its furl scale by set and 1 - set about the "
                 "bowsprit's tip. A rope end's point is in its frame's part coordinates; a sheet whose sail is furled "
                 "runs from its clew's x and z at y -3 in the yard's frame.",
        "cannon": {"muzzle_from_trunnion": MUZZLE, "points": "+z", "turn": "-facing about y"},
        "stations": stations,
        "small_crew_order": list(SMALL_CREW_ORDER),
        "local_deck": list(LOCAL_DECK),
        "hull": {"beam": BEAM, "length": LENGTH, "deck_y": DECK_Y, "rail_y": RAIL_Y, "keel_y": KEEL_PX / PX,
                 "main_mast": {"z": MAIN_MAST[0], "top": MAIN_MAST[1]},
                 "fore_mast": {"z": FORE_MAST[0], "top": FORE_MAST[1]},
                 "gun_ports": [{"side": "port" if side > 0 else "starboard", "z": [p0 / PX, p1 / PX],
                                "y": list(GUN_PORT_Y)} for side, p0, p1 in gun_ports()]},
    }


# ------------------------------------------------------------------ checks shared with the tests

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


def _reaches_inside(poly, lo, hi):
    """Whether a quad (its corners) has area inside the box lo..hi."""
    for axis in range(3):
        poly = _clip(poly, axis, lo[axis], True)
        poly = _clip(poly, axis, hi[axis], False) if poly else poly
    return len(poly) >= 3 and _area(poly) > 1e-6


def _segment_in_box(a, b, lo, hi):
    """Whether the segment a..b passes through the open box lo..hi (slab test)."""
    t0, t1 = 0.0, 1.0
    for k in range(3):
        d = b[k] - a[k]
        if abs(d) < 1e-12:
            if not lo[k] < a[k] < hi[k]:
                return False
            continue
        u0, u1 = (lo[k] - a[k]) / d, (hi[k] - a[k]) / d
        if u0 > u1:
            u0, u1 = u1, u0
        t0, t1 = max(t0, u0), min(t1, u1)
        if t0 >= t1:
            return False
    return True


def figure(index):
    """The box (pixels) a crewman standing at station `index` fills: feet at the seat, 0.6 wide, 1.95 tall."""
    x, y, z = (v * PX for v in STATIONS[index][1])
    return ((x - FIGURE_HALF, y, z - FIGURE_HALF), (x + FIGURE_HALF, y + FIGURE_HEIGHT, z + FIGURE_HALF))


def _placed_quads(quads, part, matrices):
    """A part's quads placed (an n x 4 x 3 array per matrix)."""
    corners = np.array([[v[:3] + [1.0] for v in q["vertices"]] for q in quads[part]], dtype=float)
    return [(corners @ m.T)[:, :, :3] for m in matrices]


def _overlapping(placed, lo, hi):
    """Indices of placed quads whose bounding boxes overlap the box lo..hi."""
    qlo, qhi = placed.min(1), placed.max(1)
    return np.nonzero(np.all(qhi > np.array(lo), 1) & np.all(qlo < np.array(hi), 1))[0]


def standing_clear(quads=None, eps=0.01, pose_list=None):
    """[(station, part, texture or "rope")] for every drawn quad or running rope, in any swept pose, that reaches inside
    a standing crewman: empty when all ten stand clear. The floor under their feet only touches them."""
    quads = quads or export()
    problems = set()
    boxes = []
    for index in range(len(STATIONS)):
        lo, hi = figure(index)
        boxes.append((STATION_NAMES[index], [v + eps for v in lo], [v - eps for v in hi]))
    seen_static = set()
    for pose_frames in pose_list or poses(2):
        for part, matrices in pose_frames.items():
            key = (part, tuple(np.round(np.concatenate([m.ravel() for m in matrices]), 6)) if matrices else ())
            if key in seen_static:
                continue
            seen_static.add(key)
            for placed in _placed_quads(quads, part, matrices):
                for name, lo, hi in boxes:
                    for i in _overlapping(placed, lo, hi):
                        if _reaches_inside([tuple(p) for p in placed[i]], lo, hi):
                            problems.add((name, part, quads[part][i]["texture"]))
        for spec in ropes():
            a, b = rope_ends(pose_frames, spec)
            for name, lo, hi in boxes:
                if _segment_in_box(a, b, [v - 0.5 for v in lo], [v + 0.5 for v in hi]):
                    problems.add((name, spec["name"], "rope"))
    return sorted(problems)


def _segment_hits_quad(a, b, poly):
    """Whether the segment a..b crosses the planar quad poly (corners in order), inside it."""
    p0 = np.array(poly[0])
    n = np.cross(np.array(poly[1]) - p0, np.array(poly[2]) - p0)
    if np.linalg.norm(n) < 1e-9:
        n = np.cross(np.array(poly[2]) - p0, np.array(poly[3]) - p0)
    if np.linalg.norm(n) < 1e-9:
        return False
    da, db = float(np.dot(np.array(a) - p0, n)), float(np.dot(np.array(b) - p0, n))
    if da * db > 0 or abs(da - db) < 1e-12:
        return False
    t = da / (da - db)
    x = np.array(a) + t * (np.array(b) - np.array(a))
    sign = 0
    for i in range(len(poly)):
        e = np.array(poly[(i + 1) % len(poly)]) - np.array(poly[i])
        c = float(np.dot(np.cross(e, x - np.array(poly[i])), n))
        if abs(c) < 1e-9:
            continue
        if sign == 0:
            sign = 1 if c > 0 else -1
        elif (c > 0) != (sign > 0):
            return False
    return True


def _segment_box(a, b, pad=0.5):
    return np.minimum(a, b) - pad, np.maximum(a, b) + pad


SOLIDS = [*[b for b in lower_masts()], *[b for b in topmasts()],
          ((-12, MAIN_TOP_FLOOR - 4, 2), (12, MAIN_TOP_FLOOR + 8, 30)), ((-9, 120, 70), (9, 123, 88))]


def brace_clear(quads=None, steps=8):
    """[(sail, brace, what)] for every pose in which a braced sail's canvas, bundle or yard meets the standing rigging
    (shrouds, topmast shrouds, stays) or another sail's canvas, its canvas or bundle enters a mast or a top, or a running
    rope passes through a canvas it does not belong to: empty when every sail keeps clear within its limit."""
    quads = quads or export()
    problems = set()
    segs = [(np.array(a), np.array(b)) for a, b in standing_lines()]
    for brace in np.linspace(-BRACE_MAX, BRACE_MAX, 2 * steps + 1):
        for set_ in (1.0, 0.0):
            for sway in (-SWAY, SWAY):
                pf = frames(brace=float(brace), sway=sway, set_=set_)
                running = [(spec["name"], np.array(e[0]), np.array(e[1])) for spec in ropes() for e in [rope_ends(pf, spec)]]
                for name in SQUARE:
                    for kind in ("yard", "sail", "furl"):
                        part = f"pirate_ship_{kind}_{name}"
                        for placed in _placed_quads(quads, part, pf[part]):
                            qlo, qhi = placed.min(1), placed.max(1)
                            for a, b in segs:
                                slo, shi = _segment_box(a, b)
                                for i in np.nonzero(np.all(qhi > slo, 1) & np.all(qlo < shi, 1))[0]:
                                    if _segment_hits_quad(a, b, placed[i]):
                                        problems.add((name, round(float(brace), 1), f"{kind} meets the standing rigging"))
                            if kind == "yard":
                                continue
                            for lo, hi in SOLIDS:
                                lo2, hi2 = [v + 0.01 for v in lo], [v - 0.01 for v in hi]
                                for i in _overlapping(placed, lo2, hi2):
                                    if _reaches_inside([tuple(p) for p in placed[i]], lo2, hi2):
                                        problems.add((name, round(float(brace), 1), f"{kind} enters a mast or top"))
                            if kind == "sail":
                                for rope_name, a, b in running:
                                    if name in rope_name.replace("topsail", "top").split("_sheet")[0].split("_brace")[0]:
                                        continue
                                    slo, shi = _segment_box(a, b, 0.0)
                                    for i in np.nonzero(np.all(qhi > slo, 1) & np.all(qlo < shi, 1))[0]:
                                        if _segment_hits_quad(a, b, placed[i]):
                                            problems.add((name, round(float(brace), 1), f"{rope_name} passes through its canvas"))
    return sorted(problems)


def envelope(quads=None):
    """How far the drawn ship reaches, ship-local pixels, unturned by yaw: every vertex and rope end in every swept pose,
    on the swell and heeled (the heave, the roll, then the pitch, as the renderer's pose applies them).
    Returns ((lo, hi) of the points above the waterline, (lo, hi) of those at or below it)."""
    quads = quads or export()
    cache = {part: _points(quads, part) for part in PARTS}
    every = np.concatenate([placed_points(quads, pf, cache) for pf in poses(2)])
    above = [np.full(3, np.inf), np.full(3, -np.inf)]
    below = [np.full(3, np.inf), np.full(3, -np.inf)]
    homogeneous = np.concatenate([every, np.ones((len(every), 1))], 1)
    for roll in (-ROLL, -ROLL / 2, 0.0, ROLL / 2, ROLL):
        for pitch in (-PITCH, 0.0, PITCH):
            for heave in (-HEAVE, HEAVE):
                m = _move(0, heave, 0) @ _rot("z", roll) @ _rot("x", pitch)
                p = (homogeneous @ m.T)[:, :3]
                for box_, mask in ((above, p[:, 1] > 0), (below, p[:, 1] <= 0)):
                    if mask.any():
                        box_[0] = np.minimum(box_[0], p[mask].min(0))
                        box_[1] = np.maximum(box_[1], p[mask].max(0))
    return tuple((lo.tolist(), hi.tolist()) for lo, hi in (above, below))


def place(vertex, pivot, yaw):
    """A part's vertex (pixels about its pivot) in ship pixels, turned by the station facing as the renderer does
    (yRot(-facing): facing -90 points the gun's +z to port, +x)."""
    rad = math.radians(-yaw)
    x, y, z = vertex[:3]
    return (x * math.cos(rad) + z * math.sin(rad) + pivot[0], y + pivot[1], z * math.cos(rad) - x * math.sin(rad) + pivot[2])


# ------------------------------------------------------------------ data

# The ship's names and messages, as the game shows them (menagerie/PirateShip, PirateShipDeck).
LANG = {
    f"entity.{MOD}.pirate_ship": "Pirate Ship",
    f"entity.{MOD}.pirate_ship_deck": "Pirate Ship",
    f"container.{MOD}.pirate_ship_hold": "Pirate Ship's Hold",
    f"message.{MOD}.pirate_ship.crewed": "The crew still hold this ship. Beat them to open the hold.",
}


def merge_lang(text):
    """en_us.json's text with LANG in it, as a full regeneration lays it out: generate_material_data writes the base
    keys sorted (LANG goes in through write_all's lang), so a new key goes at its sorted place in that leading sorted
    block, a key already there keeps its place; everything else is unchanged (indent 2, ensure_ascii off, a final
    newline)."""
    import bisect
    pairs = json.loads(text, object_pairs_hook=list)
    ascii_layout = json.dumps(dict(pairs), indent=2, ensure_ascii=True) + "\n" == text
    if not ascii_layout and json.dumps(dict(pairs), indent=2, ensure_ascii=False) + "\n" != text:
        raise ValueError("en_us.json is not in the generators' layout")
    keys = [k for k, v in pairs]
    for key, value in LANG.items():
        if key in keys:
            pairs[keys.index(key)] = [key, value]
            continue
        sorted_end = 1
        while sorted_end < len(keys) and keys[sorted_end - 1] < keys[sorted_end]:
            sorted_end += 1
        at = bisect.bisect_left(keys, key, 0, sorted_end)
        pairs.insert(at, [key, value])
        keys.insert(at, key)
    return json.dumps(dict(pairs), indent=2, ensure_ascii=ascii_layout) + "\n"


def write_all(write, assets, data, lang, condition):
    lang.update(LANG)
    quads = export()
    # Written compact: it is thousands of quads.
    (assets / "pirate_ship_quads.json").write_text(json.dumps(quads, separators=(",", ":")) + "\n", encoding="utf-8")
    (assets / "pirate_ship_rig.json").write_text(json.dumps(rig(quads), indent=2) + "\n", encoding="utf-8")


# ------------------------------------------------------------------ art
#
# Every texture is 16 x 16, drawn texel by texel at the model's own pixel scale from four tones a material (a fifth for
# an accent), lit from the top left: broad boards two to a block with soft seams one tone down, a few long grain strokes
# and a knot or a butt joint here and there, never a fine repeating line or per-pixel noise.

def _pal(*hexes):
    return [tuple(int(h[i:i + 2], 16) for i in (0, 2, 4)) for h in hexes]


# Warm aged oak for the topsides, a step darker for the wale, warm near-black tar below the waterline.
OAK = _pal("4a2f1d", "5f3d25", "74502f", "8a6039")
OAK_WALE = _pal("3a2517", "4b301e", "5c3c25", "6e4a2e")
TARRED = _pal("261d17", "30251d", "3a2d23", "46372b")
DECK_OAK = _pal("7e5e3c", "947048", "a88256", "bc9666")
RAIL_OAK = _pal("8c6a42", "a27c4e", "b68e5c", "c8a06c")
PINE = _pal("80603a", "966f44", "ad8152", "c29562")
CANVAS_CREAM = _pal("a89978", "c0b18f", "d4c6a4", "e4d8b8")
CANVAS_PALE = _pal("b2a484", "cabb9a", "ddd0b0", "ebe1c4")
PATCH_TAN = _pal("6e5236", "866444", "9a7752", "ae8a62")
RED_OCHRE = _pal("5e2a1c", "763625", "8c4430", "a0543c")
ROPE_TAN = _pal("5a4128", "765737", "917049", "a8865a")
IRON_DARK = _pal("232326", "303035", "404047", "56565e")
BRASS = _pal("6c4c1e", "8e6828", "b08836", "cca84a", "e0c46a")
STAVES = _pal("4a2f17", "5c3b1d", "6c4624", "7c522c")
GLASS = _pal("1a4a50", "245e66", "33767c", "5e9a9a")
FLAG_BLACK = _pal("16151a", "1d1c22", "25242b", "2e2d35")
BONE = _pal("8a7a5c", "b5a684", "d6cba8", "ebe2c4")
SOCKET = _pal("231a24", "3a2c3a", "54404f")
LAMP_GLOW = [(214, 146, 52), (236, 178, 74), (248, 206, 104), (254, 228, 150)]


class _Canvas:
    """A 16x16 opaque texture."""
    w = h = 16

    def __init__(self, fill=(0, 0, 0)):
        self.img = Image.new("RGBA", (16, 16), tuple(fill) + (255,))

    def put(self, x, y, c):
        if 0 <= x < 16 and 0 <= y < 16:
            self.img.putpixel((int(x), int(y)), tuple(c[:3]) + (255,))

    def get(self, x, y):
        return self.img.getpixel((x, y))[:3]


def boards(pal, seed, count=2, vertical=False, wet=0):
    """Boards `count` to the tile (8 pixels each at two), running across (or, vertical, down) the texture: the base tone,
    each board's last row a soft seam one tone down, two or three long grain strokes a board and a knot in one board; no
    butt joints, which would repeat every block like a ruler, so the strakes read as long continuous boards. `wet` darkens
    that many rows at the bottom a tone or two (the boot-top at the waterline)."""
    rng = random.Random(seed)
    dark, mid, base, light = pal[:4]
    c = _Canvas(base)
    size = 16 // count

    def put(u, v, colour):  # u along the board, v across it
        if vertical:
            c.put(v, u, colour)
        else:
            c.put(u, v, colour)

    def get(u, v):
        return c.get(v, u) if vertical else c.get(u, v)
    for b in range(count):
        top = b * size
        for u in range(16):
            put(u, top + size - 1, mid)
        for stroke in range(rng.choice((2, 3))):
            v = top + rng.randrange(1, size - 2)
            start, length = rng.randrange(16), rng.randrange(4, 9)
            tone = light if stroke % 2 == 0 else mid
            for k in range(length):
                put((start + k) % 16, v, tone)
        if b == rng.randrange(count):
            u, v = rng.randrange(1, 13), top + rng.randrange(2, size - 3)
            put(u, v, dark)
            put(u + 1, v, dark)
            put(u, v - 1, mid)
            put(u + 2, v, mid)
    for v in range(16 - wet, 16):
        for u in range(16):
            col = get(u, v)
            k = pal.index(col) if col in pal else 2
            put(u, v, pal[max(0, k - 1 - (v - (16 - wet)) // 2)])
    return c.img


def grain(pal, seed, vertical=True, strokes=7):
    """Plain wood with long grain strokes along it (a spar, a rail cap): base tone, strokes a tone lighter and darker."""
    rng = random.Random(seed)
    dark, mid, base, light = pal[:4]
    c = _Canvas(base)
    for i in range(strokes):
        a, start, length = rng.randrange(16), rng.randrange(16), rng.randrange(5, 12)
        tone = light if i % 3 == 0 else mid
        for k in range(length):
            if vertical:
                c.put(a, (start + k) % 16, tone)
            else:
                c.put((start + k) % 16, a, tone)
    return c.img


def spar_end():
    """The end of a spar: square rings round a darker heart, as a log's end."""
    c = _Canvas()
    for y in range(16):
        for x in range(16):
            d = max(abs(x - 7.5), abs(y - 7.5))
            c.put(x, y, PINE[0] if d > 6.5 else PINE[1] if d < 1.5 else PINE[3 if int(d) % 3 == 0 else 2])
    return c.img


def sailcloth(pal, seed):
    """Canvas: a cloth a block wide with one seam (one tone down) at its right edge, three long three- and four-pixel
    strokes of the lighter and darker tones for the weave's unevenness."""
    rng = random.Random(seed)
    dark, mid, base, light = pal[:4]
    c = _Canvas(base)
    for i in range(3):
        x, y = rng.randrange(0, 12), rng.randrange(0, 15)
        tone = light if i % 2 == 0 else mid
        for dx in range(rng.choice((3, 4))):
            c.put(x + dx, y, tone)
    for y in range(16):
        c.put(15, y, mid)
    return c.img


def furled():
    """A furled sail's roll: a rounded form lit along its top and shaded under it, with the folds as offset strokes."""
    c = _Canvas()
    rows = [3, 3, 3, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 0, 0, 0]
    for y in range(16):
        for x in range(16):
            c.put(x, y, CANVAS_CREAM[rows[y]])
    for y, x0 in ((4, 1), (7, 9), (10, 3), (12, 11)):
        for k in range(5):
            c.put((x0 + k) % 16, y, CANVAS_CREAM[max(0, rows[y] - 1)])
    return c.img


def patch():
    """A patch sewn on the main course (stretched once over it): tan cloth inside a darker hem with light stitches."""
    c = _Canvas(PATCH_TAN[2])
    for i in range(16):
        for x, y in ((i, 0), (i, 15), (0, i), (15, i)):
            c.put(x, y, PATCH_TAN[0])
        if i % 3 == 1 and 1 <= i <= 14:
            for x, y in ((i, 1), (i, 14), (1, i), (14, i)):
                c.put(x, y, PATCH_TAN[3])
    for x, y in ((5, 6), (6, 6), (10, 9), (11, 9)):
        c.put(x, y, PATCH_TAN[1])
    return c.img


def rope():
    """Laid rope: strands twisted diagonally, each lit along one side."""
    c = _Canvas()
    for y in range(16):
        for x in range(16):
            c.put(x, y, ROPE_TAN[[0, 2, 3, 1][(x + y) % 4]])
    return c.img


def flat(pal, seed, clumps=5):
    """One ground tone with a few two-pixel clumps of the tones either side (iron, the bore)."""
    rng = random.Random(seed)
    c = _Canvas(pal[1])
    for i in range(clumps):
        x, y = rng.randrange(15), rng.randrange(15)
        tone = pal[2] if i % 2 == 0 else pal[0]
        c.put(x, y, tone)
        c.put(x + 1, y, tone)
    return c.img


def brass():
    """Antique brass, lit like a gold block: a frame lit along its top and left, a plate of one tone, one glint."""
    c = _Canvas(BRASS[2])
    for i in range(16):
        c.put(i, 0, BRASS[3])
        c.put(0, i, BRASS[3])
        c.put(i, 15, BRASS[1])
        c.put(15, i, BRASS[1])
    for x, y in ((3, 3), (4, 3), (3, 4)):
        c.put(x, y, BRASS[4])
    return c.img


def lamp():
    """A lantern pane, stretched once over each side: a brass frame round a glow in smooth bands, brightest low."""
    c = _Canvas()
    for y in range(16):
        for x in range(16):
            if x in (0, 15) or y in (0, 15):
                c.put(x, y, BRASS[1] if x == 15 or y == 15 else BRASS[3])
            elif x in (7, 8):
                c.put(x, y, BRASS[2])
            else:
                c.put(x, y, LAMP_GLOW[min(3, (y - 1) // 4)])
    return c.img


def window():
    """A stern cabin window, stretched once over it: a brass frame, oak mullions and four panes of dark glass, each lit
    along its top and left."""
    c = _Canvas()
    for y in range(16):
        for x in range(16):
            if x in (0, 15) or y in (0, 15):
                col = BRASS[1] if x == 15 or y == 15 else BRASS[3]
            elif x in (1, 14) or y in (1, 14) or x in (7, 8) or y in (7, 8):
                col = OAK[1]
            else:
                px, py = (x - 2) % 6 if x < 7 else (x - 9) % 6, (y - 2) % 6 if y < 7 else (y - 9) % 6
                col = GLASS[3] if (px == 0 or py == 0) else GLASS[0] if (px == 4 or py == 4) else GLASS[1]
            c.put(x, y, col)
    return c.img


def hatch():
    """A deck grating: oak bars every four pixels, lit along the top and left, over a dark hold."""
    c = _Canvas()
    for y in range(16):
        for x in range(16):
            if x % 4 == 0 or y % 4 == 0:
                col = DECK_OAK[3] if (x % 4 == 0 and y % 4 != 0) else DECK_OAK[2]
            elif x % 4 == 3 or y % 4 == 3:
                col = (40, 28, 18)
            else:
                col = (28, 19, 12)
            c.put(x, y, col)
    return c.img


def barrel():
    """Barrel staves four pixels wide, each lit along its left, with two iron hoops low in the tile."""
    c = _Canvas()
    for y in range(16):
        for x in range(16):
            k = x % 4
            col = STAVES[0] if k == 3 else STAVES[3] if k == 0 else STAVES[2 if (x // 4) % 2 else 1]
            if y in (7, 13):
                col = IRON_DARK[3]
            elif y in (8, 14):
                col = IRON_DARK[1]
            c.put(x, y, col)
    return c.img


def barrel_top():
    """A barrel's head: two boards across inside a dark rim."""
    c = _Canvas(STAVES[2])
    for x in range(16):
        c.put(x, 7, STAVES[1])
    for i in range(16):
        for x, y in ((i, 0), (0, i)):
            c.put(x, y, STAVES[0])
        for x, y in ((i, 15), (15, i)):
            c.put(x, y, IRON_DARK[1])
    return c.img


def flag_picture():
    """The black flag, stretched once over each side: a skull with square sockets (docs/ART_DIRECTION.md, creatures
    and faces) over crossed bones, in cream bone lit along the top and left."""
    c = _Canvas(FLAG_BLACK[1])
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
    save(boards(OAK, 6101), "block", HULL)
    save(boards(OAK, 6102, wet=3), "block", HULL_SIDE)
    save(boards(TARRED, 6103), "block", TAR)
    save(boards(OAK, 6104), "block", BULWARK)
    save(boards(OAK_WALE, 6105), "block", WALE)
    save(boards(DECK_OAK, 6106, vertical=True), "block", DECK)
    save(boards(RED_OCHRE, 6107), "block", RED)
    save(grain(RAIL_OAK, 6108, vertical=False), "block", TRIM)
    save(grain(PINE, 6109, vertical=True), "block", SPAR)
    save(grain(PINE, 6109, vertical=False), "block", SPAR_H)
    save(spar_end(), "block", SPAR_END)
    save(sailcloth(CANVAS_CREAM, 6112), "block", CANVAS)
    save(sailcloth(CANVAS_PALE, 6113), "block", CANVAS_LIGHT)
    save(furled(), "block", FURLED)
    save(patch(), "block", PATCH)
    save(rope(), "block", ROPE)
    save(flat(IRON_DARK, 6118), "block", IRON)
    save(flat(SOCKET + [SOCKET[2]], 6119), "block", BORE)
    save(brass(), "block", GOLD)
    save(lamp(), "block", LAMP)
    save(window(), "block", WINDOW)
    save(hatch(), "block", HATCH)
    save(barrel(), "block", BARREL)
    save(barrel_top(), "block", BARREL_TOP)
    save(flag_picture(), "block", FLAG)
    save(flat(FLAG_BLACK, 6121), "block", FLAG_EDGE)


TEXTURES = [HULL, HULL_SIDE, TAR, BULWARK, WALE, DECK, RED, TRIM, SPAR, SPAR_H, SPAR_END, CANVAS, CANVAS_LIGHT, FURLED,
            PATCH, ROPE, IRON, BORE, GOLD, LAMP, WINDOW, HATCH, BARREL, BARREL_TOP, FLAG, FLAG_EDGE]
# Textures the first version drew that this one no longer uses (running this file removes them).
RETIRED = ["ps_canvas_red", "ps_patch_cyan", "ps_patch_green"]


if __name__ == "__main__":
    # Writes only this file's assets (the full generators call write_all and draw_all with everything else).
    from pathlib import Path
    root = Path(__file__).resolve().parents[1] / "src" / "main" / "resources" / "assets" / MOD

    def save(img, kind, name):
        path = root / "textures" / kind / f"{name}.png"
        path.parent.mkdir(parents=True, exist_ok=True)
        img.save(path, optimize=True)
    draw_all(save)
    for name in RETIRED:
        (root / "textures" / "block" / f"{name}.png").unlink(missing_ok=True)
    write_all(None, root, None, {}, None)
    language = root / "lang" / "en_us.json"
    language.write_text(merge_lang(language.read_text(encoding="utf-8")), encoding="utf-8")
    quads = json.loads((root / "pirate_ship_quads.json").read_text(encoding="utf-8"))
    for problem in standing_clear(quads):
        print("in a standing crewman:", *problem)
    for problem in brace_clear(quads):
        print("braced sail:", *problem)
    (alo, ahi), (blo, bhi) = envelope(quads)
    print("above the waterline, blocks:", [round(v / PX, 4) for v in alo], [round(v / PX, 4) for v in ahi])
    print("at or below it, blocks:", [round(v / PX, 4) for v in blo], [round(v / PX, 4) for v in bhi])
