"""Batch 44: sixteen one-block machines rebuilt as big, weathered dieselpunk multi-blocks.

The look follows the owner's reference pictures: rusted riveted steel on skid feet, green patina copper housings with
perforated covers, ribbed coil stacks and banded domes, copper and rusty pipe runs with hex-nut fittings, chipped red
paint, a big turbine fan, blue pipe caps and glowing amber conduits (textures: tools/dieselrust_textures.py).

Each model is in structure space like tools/large_machines.py: facing north (front at z = 0), the master block at
0..16 on every axis, the machine reaching to its right (-x), up and back (+z). Footprints are in
large_machines.FOOTPRINTS and MachineKind.footprint(). The old one-block models stay as the "compact" look of copies
built before batch 44 (steampunk_models.COMPACT).
"""
from steampunk_models import box, cyl, dial, pipe, wheel

RUST, RUST_BARE, PATINA, PERFORATED = "dr_rust", "dr_rust_bare", "dr_patina", "dr_perforated"
RIB_PATINA, RIB_RUST, DOME = "dr_ribbed_patina", "dr_ribbed_rust", "dr_dome"
COPPER, RUST_PIPE, NUT, RED, BAND = "dr_copper_pipe", "dr_rust_pipe", "dr_nut", "dr_red", "dr_band"
SKID, GRATE, AMBER, DOOR, FAN = "dr_skid", "dr_grate", "dr_amber", "dr_fire_door", "dr_fan"
COIL, BLUE, BLADE, CRT, LEAVES = "dr_coil", "dr_blue", "dr_blade", "dr_crt", "dr_leaves"
WASH, BATH, FROST = "dr_wash_water", "dr_plating_bath", "dr_frost"
GAUGE, SOOT, HAZARD, RUBBER = "sp_gauge", "dr_soot", "dp_hazard", "dp_rubber_ribbed"


# ------------------------------------------------------------------ parts

def skids(x0, x1, z0, z1, deck=GRATE):
    """Skid feet: two rails along z under each side, cross members and a grated deck on top (y 0..3)."""
    m = []
    for x in (x0 + 1, x1 - 4):
        m.append(box((x, 0, z0), (x + 3, 2, z1), SKID))
    for z in range(int(z0) + 2, int(z1) - 2, 12):
        m.append(box((x0 + 4, 0.5, z), (x1 - 4, 1.75, z + 2), SKID))
    m.append(box((x0, 2, z0), (x1, 3, z1), {"*": RUST, "up": deck}))
    return m


def stack(axis, cu, cv, r, a0, a1, side=RIB_PATINA, every=8, cap=PATINA):
    """A ribbed column (coil stack, boiler, drum) with riveted bands every `every` pixels."""
    m = cyl(axis, cu, cv, r, a0, a1, side, cap)
    a = a0 + every / 2
    while a < a1 - 1:
        m += cyl(axis, cu, cv, r + 0.6, a, a + 1, BAND)
        a += every
    return m


def dome(cx, cz, r, y0, steps=4, texture=DOME):
    """A banded dome: stacked rings shrinking towards a blue-capped top valve."""
    m, y = [], y0
    for i in range(steps):
        ri = r * (1 - (i / steps) ** 1.6)
        h = max(1.5, r / steps * (1.2 - i * 0.15))
        m += cyl("y", cx, cz, ri, y, y + h, texture)
        y += h
    m += cyl("y", cx, cz, max(1, r * 0.18), y, y + 2, BLUE)
    return m


def nut(axis, cu, cv, r, a):
    """A hex-nut fitting round a pipe: a short wider block with the nut drawn on its ends."""
    faces = {"x": ("east", "west"), "y": ("up", "down"), "z": ("north", "south")}[axis]
    texture = {"*": RUST_BARE, faces[0]: f"{NUT}!", faces[1]: f"{NUT}!"}
    s = r + 0.75
    if axis == "x":
        return box((a, cu - s, cv - s), (a + 1.5, cu + s, cv + s), texture)
    if axis == "y":
        return box((cu - s, a, cv - s), (cu + s, a + 1.5, cv + s), texture)
    return box((cu - s, cv - s, a), (cu + s, cv + s, a + 1.5), texture)


def pipe_run(points, r=1.25, texture=COPPER):
    """Square pipe stock between axis-aligned points, a nut at every joint and end."""
    m = []
    for (ax, ay, az), (bx, by, bz) in zip(points, points[1:]):
        lo = (min(ax, bx) - r, min(ay, by) - r, min(az, bz) - r)
        hi = (max(ax, bx) + r, max(ay, by) + r, max(az, bz) + r)
        m.append(pipe(lo, hi, texture))
    for i, (x, y, z) in enumerate(points):
        if i == 0:
            other = points[1]
        else:
            other = points[i - 1]
        if other[0] != x:
            m.append(nut("x", y, z, r, x - 0.75))
        elif other[1] != y:
            m.append(nut("y", x, z, r, y - 0.75))
        else:
            m.append(nut("z", x, y, r, z - 0.75))
    return m


def flange_cylinder(axis, cu, cv, r, a0, a1, body, cap, flanges):
    """A cylinder with flange rings at the given positions along its axis (the reference's radial cylinders)."""
    m = cyl(axis, cu, cv, r, a0, a1, body, cap)
    for a in flanges:
        m += cyl(axis, cu, cv, r + 1.25, a, a + 1.25, BAND)
    return m


def lamp(face, center, size=2.5):
    return dial(face, center, size, texture=AMBER, body=RUST_BARE)


def gauge(face, center, size=3.5):
    return dial(face, center, size, texture=GAUGE, body=COPPER)


def valve(axis, cu, cv, r, a):
    """A red handwheel on a short stem."""
    return wheel(axis, cu, cv, r, a, a + 0.75, "dr_red", NUT)


# ------------------------------------------------------------------ machines

def coal_generator():
    """Two wide, two tall, three long: a riveted firebox with a glowing door and a coal hopper at the front, a banded
    rust boiler barrel with a steam dome running back to a green perforated dynamo housing, a smokestack, copper steam
    pipes with hex fittings, an amber conduit down the side and power terminals at the back. All on skid feet."""
    m = skids(-16, 16, 0, 48)
    m.append(box((-14, 3, 1), (14, 21, 14), {"*": RUST, "north": RUST_BARE}))
    m.append(box((-6, 5, 0.25), (6, 16, 1), {"*": RUST_BARE, "north": f"{DOOR}!"}))
    for y in (3, 20):
        m.append(box((-14.5, y, 0.5), (14.5, y + 1.25, 14.5), BAND))
    m.append(box((-12, 21, 3), (-1, 27, 12), {"*": RUST, "up": SOOT}))
    m.append(box((-13, 27, 2), (0, 28.5, 13), {"*": BAND, "up": SOOT}))
    m.append(gauge("north", (9.5, 15, 0.25)))
    m.append(gauge("north", (-10, 15, 0.25), 3))
    m.append(lamp("north", (9.5, 8, 0.25)))
    m += valve("z", -10, 8, 2.5, -0.25)
    # The boiler barrel and its steam dome.
    m += stack("z", 0, 16, 11, 14, 36, RIB_RUST, 7, RUST)
    m += dome(0, 26, 4.5, 27, 3)
    # The dynamo: a green perforated drum with flanges, a copper winding band and a blue hub.
    m += flange_cylinder("z", 0, 15, 11.5, 36, 47, PERFORATED, PATINA, (36, 41, 45.75))
    m += cyl("z", 0, 15, 12.2, 38.5, 40, COIL)
    m += cyl("z", 0, 15, 3, 47, 48, BLUE)
    # The smokestack, front right.
    m += stack("y", -9, 7, 3, 21, 44, RUST_PIPE, 7, SOOT)
    m += cyl("y", -9, 7, 4, 44, 46, BAND, SOOT)
    # Steam pipes from the dome over to the dynamo, and an amber conduit along the left side.
    m += pipe_run([(4, 30, 26), (4, 30, 40), (4, 27, 40)])
    m += pipe_run([(-6, 25, 20), (-6, 25, 40), (-6, 27, 40)], 1, RUST_PIPE)
    m.append(box((14.75, 8, 16), (15.75, 11, 44), {"*": RUST_BARE, "east": AMBER}))
    # Power terminals on the back.
    for x in (-8, 8):
        m += cyl("z", x, 8, 1.5, 46, 48, "sp_ceramic")
        m += cyl("z", x, 8, 0.6, 46, 48.75, COPPER)
    return m


def steam_generator():
    """Three wide, three tall, two deep: a banded rust boiler under a ribbed copper dome with a blue valve cap, its
    firebox door glowing at the front, feeding through a copper steam main with hex fittings into a long green
    turbine casing with flanges, a perforated cover and a big intake fan at the far end; a red valve wheel, gauges and
    a tall amber water glass."""
    m = skids(-32, 16, 0, 32)
    # The boiler on the left.
    m += stack("y", 0, 16, 13, 3, 30, RIB_RUST, 7, RUST)
    m.append(box((-5, 4, 2.25), (5, 13, 3.5), {"*": RUST_BARE, "north": f"{DOOR}!"}))
    m += dome(0, 16, 13, 30, 5)
    m += cyl("y", 0, 16, 13.6, 29, 30.5, BAND)
    m.append(box((10.5, 8, 4), (12, 26, 5.5), {"*": RUST_BARE, "north": AMBER, "east": AMBER}))
    m.append(gauge("north", (6.5, 20, 3.4)))
    # The turbine casing along x on the right, with a perforated middle cover and the intake fan.
    m += flange_cylinder("x", 16, 16, 10, -31, -12, PATINA, PATINA, (-31, -24, -18, -13.25))
    m += cyl("x", 16, 16, 10.6, -23, -19, PERFORATED)
    m.append(box((-31.75, 5, 5), (-31, 27, 27), {"*": RUST, "west": f"{FAN}!"}))
    # The generator drum between them, with its copper winding band.
    m += cyl("x", 14, 16, 8, -12, -6, COIL, PATINA)
    m += cyl("x", 14, 16, 8.6, -10, -8, BAND)
    # The steam main from the dome top over and down into the turbine.
    m += pipe_run([(0, 40, 16), (-20, 40, 16), (-20, 27, 16)], 1.5)
    m += pipe_run([(-6, 10, 6), (-26, 10, 6), (-26, 3, 6)], 1, RUST_PIPE)
    m += valve("z", -14, 30, 3, 6)
    m.append(box((-15, 26, 6.75), (-13, 34, 8.5), NUT))
    m.append(gauge("north", (-26, 30, 4)))
    m.append(lamp("north", (-26, 22, 4)))
    return m


def electric_furnace():
    """Two by two by two: an induction furnace. A red-painted crucible housing with an amber sight window sits between
    two green ribbed induction coil stacks on a rusted plinth; copper bus pipes with hex fittings feed the coils, a
    hood with a vent fan sits on top and a riveted control cabinet stands at the back."""
    m = skids(-16, 16, 0, 32)
    m.append(box((-10, 3, 2), (10, 22, 20), {"*": RED, "up": RUST}))
    m.append(box((-6, 7, 1.25), (6, 16, 2), {"*": RUST_BARE, "north": f"{AMBER}!"}))
    for y in (6, 17):
        m.append(box((-10.5, y, 1.5), (10.5, y + 1, 20.5), BAND))
    for x in (-13, 13):
        m += stack("y", x, 11, 3, 3, 28, RIB_PATINA, 6, PATINA)
        m += cyl("y", x, 11, 1.5, 28, 31, BLUE)
    m.append(box((-12, 22, 3), (12, 26, 19), {"*": RUST, "up": PATINA}))
    m.append(box((-6, 26, 6), (6, 27, 16), {"*": RUST_BARE, "up": f"{FAN}!"}))
    m.append(box((-14, 3, 22), (14, 26, 31), {"*": PATINA, "north": PERFORATED, "south": PERFORATED}))
    m.append(gauge("north", (-4, 21.5, 21.75)))
    m.append(lamp("north", (4, 21.5, 21.75)))
    m += pipe_run([(13, 20, 11), (13, 20, 24)], 1)
    m += pipe_run([(-13, 20, 11), (-13, 20, 24)], 1)
    m += pipe_run([(0, 26.5, 18), (0, 29, 18), (0, 29, 27)], 1, RUST_PIPE)
    return m


def crusher():
    """Two wide, three tall, two deep: a jaw crusher. A rust hopper with a riveted lip feeds the jaws in a red frame
    with a glowing amber inspection slot; two big flywheels turn either side, driven by a green ribbed motor at the
    back through a rubber belt; ore falls into a chute at the bottom front."""
    m = skids(-16, 16, 0, 32)
    m.append(box((-11, 3, 4), (11, 28, 24), {"*": RED, "north": RED}))
    for y in (8, 18, 27):
        m.append(box((-11.5, y, 3.5), (11.5, y + 1, 24.5), BAND))
    m.append(box((-7, 12, 3.25), (7, 22, 4), {"*": RUST_BARE, "north": "sp_crusher_jaws!"}))
    m.append(box((-6, 23, 3.25), (6, 25, 4), {"*": RUST_BARE, "north": AMBER}))
    m.append(box((-6, 3, 0.5), (6, 8, 4), {"*": RUST, "up": SOOT}))
    # The hopper: stepped out above the frame.
    m.append(box((-12, 28, 3), (12, 36, 25), {"*": RUST, "up": SOOT}))
    m.append(box((-15, 36, 1), (15, 42, 27), {"*": RUST, "up": SOOT}))
    m.append(box((-15.5, 42, 0.5), (15.5, 43.5, 27.5), {"*": BAND, "up": SOOT}))
    # Flywheels on the shaft either side, and the shaft.
    for x0 in (-15.5, 12.5):
        m += wheel("x", 17, 14, 9, x0, x0 + 3, RUST_PIPE, NUT)
    m += cyl("x", 17, 14, 1.25, -16, 16, NUT)
    # The motor at the back, belted to the left flywheel.
    m += stack("x", 8, 28, 4, -10, 6, RIB_PATINA, 5, BLUE)
    m.append(box((13, 8, 15), (14.5, 17, 28), RUBBER))
    m.append(gauge("north", (-9, 30, 2.5), 3))
    m.append(lamp("north", (9, 30, 2.5), 2))
    m += pipe_run([(-8, 34, 25), (-8, 34, 30), (-8, 12, 30)], 1, RUST_PIPE)
    return m


def metal_press():
    """Two wide, three tall, two deep: a hydraulic press. Four riveted rust columns carry a crown with two green
    hydraulic cylinders; a red ram block with an amber stroke light hangs over the die on a heavy bed; copper hydraulic
    lines with hex fittings run from a pump with a red valve wheel at the back, and a gauge board sits at the side."""
    m = skids(-16, 16, 0, 32)
    m.append(box((-14, 3, 2), (14, 10, 22), {"*": RUST, "up": "sp_die"}))
    m.append(box((-14.5, 9, 1.5), (14.5, 10.25, 22.5), BAND))
    for x in (-14, 10):
        for z in (2, 18):
            m.append(box((x, 10, z), (x + 4, 40, z + 4), {"*": RUST_PIPE}))
            for y in (18, 30):
                m.append(box((x - 0.5, y, z - 0.5), (x + 4.5, y + 1, z + 4.5), BAND))
    m.append(box((-15, 40, 1), (15, 47, 23), {"*": RUST, "north": RUST_BARE}))
    m.append(box((-15.5, 43, 0.5), (15.5, 44, 23.5), BAND))
    for x in (-6, 6):
        m += flange_cylinder("y", x, 12, 4, 30, 40, PATINA, PATINA, (31, 38))
        m += cyl("y", x, 12, 1.5, 22, 30, "dp_chrome")
    m.append(box((-10, 16, 5), (10, 22, 19), {"*": RED, "down": "sp_die"}))
    m.append(box((-4, 18, 4.25), (4, 20, 5), {"*": RUST_BARE, "north": AMBER}))
    # The hydraulic pump at the back, with its lines up to the crown.
    m += stack("z", -2, 8, 5, 23, 31, RIB_PATINA, 4, BLUE)
    m += valve("x", 8, 27, 3, 4)
    m += pipe_run([(-8, 11, 27), (-8, 44, 27), (-8, 44, 20)], 1.25)
    m += pipe_run([(4, 11, 27), (12, 11, 27), (12, 46, 27), (12, 46, 20)], 1)
    m.append(gauge("north", (-12.5, 34, 1.5)))
    m.append(lamp("north", (12.5, 34, 1.5), 2))
    return m


def wire_drawer():
    """Four wide, one tall, two deep: a long wire drawing bench. A riveted rust bed carries a pay-off reel at the right,
    a row of drawing dies in green ribbed die boxes with hex fittings, copper wire running through them to a big
    take-up capstan with a red rim at the left, an amber lamp and gauge on a control post, and a geared motor
    underneath the capstan."""
    m = [box((-48, 0, 0), (16, 2, 32), {"*": SKID, "up": GRATE})]
    m.append(box((-47, 2, 3), (15, 7, 29), {"*": RUST, "up": RUST_BARE}))
    m.append(box((-47.5, 6, 2.5), (15.5, 7, 29.5), BAND))
    # The pay-off reel at the right end.
    m += wheel("z", -40, 12, 6, 14, 15, RUST_PIPE, NUT)
    m += cyl("z", -40, 12, 4, 15, 19, COIL)
    m += wheel("z", -40, 12, 6, 19, 20, RUST_PIPE, NUT)
    # Die boxes along the bed, with the wire running through them.
    for x in (-30, -18, -6):
        m.append(box((x - 3, 7, 12), (x + 3, 13, 20), {"*": RIB_PATINA, "up": PATINA}))
        m.append(nut("x", 10, 16, 1, x - 3.75))
        m.append(nut("x", 10, 16, 1, x + 3))
    m.append(box((-36, 9.75, 15.75), (4, 10.25, 16.25), COPPER))
    # The capstan at the left, with a red rim, on a geared motor.
    m += cyl("y", 8, 16, 7, 7, 11, COIL, RUST_BARE)
    m += cyl("y", 8, 16, 7.6, 11, 12, "dr_red")
    m += cyl("y", 8, 16, 2, 12, 14, BLUE)
    m.append(box((1, 2, 26), (15, 7, 31), {"*": PATINA, "south": PERFORATED}))
    # Control post at the front left.
    m.append(box((10, 2, 1), (14, 14, 5), {"*": RUST, "north": RUST_BARE}))
    m.append(gauge("north", (12, 11, 0.75), 3))
    m.append(lamp("north", (12, 6.5, 0.75), 2))
    m += pipe_run([(-44, 4, 30), (-4, 4, 30)], 0.75, RUST_PIPE)
    return m


def circuit_assembler():
    """Three wide, two tall, two deep: a pick-and-place line. A riveted rust bench with a feeder conveyor carries
    boards under a gantry with a green ribbed head on a red carriage; an amber CRT console with a gauge and a lamp
    stands at the left, a perforated cabinet of reels at the back, and copper air lines with hex fittings feed the
    head."""
    m = skids(-32, 16, 0, 32)
    m.append(box((-31, 3, 4), (15, 13, 26), {"*": RUST, "up": RUST_BARE}))
    m.append(box((-31.5, 12, 3.5), (15.5, 13, 26.5), BAND))
    m.append(box((-30, 13, 10), (-2, 14, 18), {"*": RUBBER, "up": "sp_belt"}))
    for x in (-26, -18, -10):
        m.append(box((x, 14, 11.5), (x + 4, 14.75, 16.5), "el_seams"))
    # The gantry: two posts, the beam and the carriage with its head.
    for x in (-29, -3):
        m.append(box((x, 13, 5), (x + 2, 30, 7), RUST_PIPE))
        m.append(box((x, 13, 21), (x + 2, 30, 23), RUST_PIPE))
    for z in (5, 21):
        m.append(box((-29, 28, z), (-1, 30, z + 2), RUST))
    m.append(box((-21, 28, 7), (-11, 30, 21), {"*": RUST, "up": GRATE}))
    m.append(box((-20, 22, 9), (-12, 28, 19), {"*": "dr_red"}))
    m += stack("y", -16, 14, 2.5, 16, 22, RIB_PATINA, 3, NUT)
    # The console at the left, its screen glowing amber.
    m.append(box((1, 3, 1), (15, 26, 12), {"*": PATINA, "north": RUST_BARE, "up": RUST}))
    m.append(box((3, 13, 0.25), (13, 23, 1), {"*": RUST_BARE, "north": f"{CRT}!"}))
    m.append(gauge("north", (5, 8, 0.75), 3))
    m.append(lamp("north", (11, 8, 0.75), 2))
    # Reel cabinet at the back.
    m.append(box((-30, 13, 24), (-4, 30, 31), {"*": PATINA, "north": PERFORATED, "south": PERFORATED}))
    for x in (-25, -17, -9):
        m += cyl("z", x, 22, 3, 23, 24, COIL)
    m += pipe_run([(-2, 29, 14), (-14, 29, 14)], 0.75)
    m += pipe_run([(-30, 6, 28), (-30, 30, 28)], 1, RUST_PIPE)
    return m


def pulverizer():
    """Three wide, two tall, two deep: a ball mill. A long banded rust drum lies on trunnion cradles with a big ring
    gear, driven by a green ribbed motor through a pinion; a feed hopper at the right end, a discharge chute with an
    amber lamp at the left and copper water lines with hex fittings."""
    m = skids(-32, 16, 0, 32)
    for x in (-27, 5):
        m.append(box((x, 3, 6), (x + 6, 10, 26), {"*": RUST, "up": RUST_BARE}))
    m += flange_cylinder("x", 17, 16, 11, -26, 10, DOME, RUST, (-26, -16, -2, 8.75))
    m += cyl("x", 17, 16, 13, -11, -8, RUST_PIPE, NUT)
    m += cyl("x", 17, 16, 13.6, -10, -9, BAND)
    # The motor and pinion at the back of the drum.
    m += stack("x", 6, 28, 4, -18, -12, RIB_PATINA, 3, BLUE)
    m += cyl("x", 9, 28, 2, -12, -8, NUT)
    # Feed hopper at the right end, discharge chute at the left end.
    m.append(box((-32, 18, 10), (-26, 30, 22), {"*": RUST, "up": SOOT}))
    m.append(box((-32, 30, 8), (-24, 32, 24), {"*": BAND, "up": SOOT}))
    m.append(box((10, 3, 9), (16, 14, 23), {"*": RUST, "north": RUST_BARE}))
    m.append(lamp("north", (13, 10, 8.75), 2))
    m.append(gauge("north", (-20, 8, 5.75), 3))
    m += pipe_run([(-30, 32, 4), (-30, 24, 4), (-6, 24, 4)], 0.75)
    m += pipe_run([(14, 3, 28), (14, 22, 28), (2, 22, 28)], 1, RUST_PIPE)
    m += valve("z", 13, 6, 2, 8.25)
    return m


def ore_washer():
    """Two wide, two tall, four long: a washing line. A green rotary washing drum with flanges sits over a long
    sluice of riveted tanks full of churning water; ore climbs a rubber feed belt at the front, copper spray bars with
    hex fittings run over the tanks, a settling tank with a blue cap stands at the back and an amber lamp and gauges
    sit on the front panel."""
    m = skids(-16, 16, 0, 64)
    # The sluice tanks: rust walls round a water surface.
    m.append(box((-14, 3, 18), (14, 12, 62), {"*": RUST, "up": WASH}))
    for z in (18, 32, 46, 61):
        m.append(box((-14.5, 3, z), (14.5, 13, z + 1), BAND))
    # The rotary drum over the front half, on a cradle.
    m.append(box((-12, 3, 2), (12, 8, 16), {"*": RUST, "north": RUST_BARE}))
    m += flange_cylinder("z", 0, 17, 9, 2, 30, PERFORATED, PATINA, (2, 10, 20, 28.75))
    m += cyl("z", 0, 17, 9.6, 14, 16, COIL)
    # The feed belt up into the drum mouth.
    m.append(box((-4, 8, -0.5), (4, 9, 3), {"*": RUBBER, "up": "sp_belt"}))
    # Spray bars over the tanks.
    for z in (38, 52):
        m += pipe_run([(-12, 18, z), (12, 18, z)], 0.75)
    m += pipe_run([(12, 18, 38), (12, 18, 52), (12, 6, 52)], 0.75, RUST_PIPE)
    # The settling tank at the back.
    m += stack("y", -6, 56, 6, 12, 30, RIB_RUST, 6, RUST)
    m += cyl("y", -6, 56, 2, 30, 32, BLUE)
    m.append(lamp("north", (-10, 10, 1.75), 2))
    m.append(gauge("north", (10, 10, 1.75), 3))
    m += valve("x", 8, 56, 2.5, 14.25)
    return m


def sieve():
    """Two wide, two tall, three long: a shaker screen. A rust hopper feeds an inclined deck of mesh screens in a
    red-painted frame on coil springs; an eccentric green ribbed vibrator motor sits on the side, chutes at the end
    drop the fines and the oversize, and an amber lamp, a gauge and copper dust lines finish it."""
    m = skids(-16, 16, 0, 48)
    for z in (6, 38):
        for x in (-13, 9):
            m += stack("y", x + 2, z, 1.75, 3, 12, COIL, 2, NUT)
    m.append(box((-15, 12, 2), (15, 16, 46), {"*": "dr_red", "up": "sp_mesh"}))
    m.append(box((-13, 16, 4), (13, 17, 44), {"*": RUST_BARE, "up": "sp_mesh"}))
    for z in (2, 15, 29, 45):
        m.append(box((-15.5, 11.5, z), (15.5, 16.5, z + 1), BAND))
    # Feed hopper at the front.
    m.append(box((-10, 17, 2), (10, 26, 14), {"*": RUST, "up": SOOT}))
    m.append(box((-12, 26, 1), (12, 28, 15), {"*": BAND, "up": SOOT}))
    # The vibrator motor on the left side.
    m += stack("z", 12, 20, 4, 16, 30, RIB_PATINA, 4, BLUE)
    m.append(box((15, 18, 22), (16, 22, 26), NUT))
    # Chutes at the back end.
    m.append(box((-12, 3, 44), (-2, 12, 48), {"*": RUST, "up": SOOT}))
    m.append(box((2, 3, 44), (12, 12, 48), {"*": RUST, "up": SOOT}))
    m.append(lamp("north", (-11, 8, 0.75), 2))
    m.append(gauge("north", (11, 22, 0.75), 3))
    m += pipe_run([(-12, 30, 8), (-12, 30, 40), (-12, 17, 40)], 0.75)
    return m


def sawmill():
    """Two wide, two tall, five long: a log sawmill. A long riveted rust bed carries a log carriage on rails past a
    big saw blade in a red guard, driven by a belt from a green ribbed motor; an amber cutting lamp, a dust hood with
    copper ducting and hex fittings, and a log on the carriage."""
    m = skids(-16, 16, 0, 80)
    m.append(box((-14, 3, 2), (14, 9, 78), {"*": RUST, "up": RUST_BARE}))
    for z in range(2, 78, 15):
        m.append(box((-14.5, 8, z), (14.5, 9.25, z + 1), BAND))
    for x in (-10, 8):
        m.append(box((x, 9, 1), (x + 2, 10.5, 79), "dp_chrome"))
    # The carriage with a log on it, near the front.
    m.append(box((-12, 10.5, 6), (12, 13, 30), {"*": RUST, "up": GRATE}))
    m += cyl("z", -1, 19, 6, 7, 29, "sp_bark", "sp_wood")
    for z in (8, 26):
        m.append(box((-12, 13, z), (-9, 21, z + 2), RUST_PIPE))
    # The saw: a big blade in a red guard at the middle, its motor beside it.
    m += cyl("x", 18, 44, 13, -1.5, 0.5, BAND, BLADE)
    m.append(box((-3, 24, 30), (3, 33, 58), {"*": "dr_red"}))
    m.append(box((-3.5, 25, 30), (3.5, 26, 58), BAND))
    m += stack("x", 15, 66, 5, -14, -3, RIB_PATINA, 4, BLUE)
    m.append(box((-3, 12, 45), (-2, 20, 66), RUBBER))
    m.append(lamp("north", (0, 30, 29.75), 2.5))
    # Dust hood and duct to the back.
    m += pipe_run([(0, 33, 44), (0, 38, 44), (0, 38, 74), (0, 9, 74)], 2, COPPER)
    m.append(gauge("north", (10, 14, 1.75), 3))
    m += valve("x", 12, 70, 3, 14)
    return m


def fuel_cell():
    """Two by two by two: a fuel cell stack. Rows of green ribbed cell plates between red end plates are clamped by
    rust tie-rods with hex nuts; copper hydrogen and oxygen manifolds with blue caps run along the top, an amber
    status conduit glows on the front and a perforated cabinet holds the inverter at the back."""
    m = skids(-16, 16, 0, 32)
    m.append(box((-14, 3, 2), (-11, 24, 22), "dr_red"))
    m.append(box((11, 3, 2), (14, 24, 22), "dr_red"))
    m.append(box((-11, 4, 3), (11, 22, 21), {"*": RIB_PATINA}))
    for y in (6, 20):
        for z in (4, 19):
            m.append(box((-15, y - 0.5, z - 0.5), (15, y + 0.5, z + 0.5), RUST_PIPE))
            m.append(nut("x", y, z, 0.5, -16))
            m.append(nut("x", y, z, 0.5, 14.5))
    m.append(box((-9, 9, 2.25), (9, 12, 3), {"*": RUST_BARE, "north": AMBER}))
    for z, cap in ((8, BLUE), (15, "dr_red")):
        m += cyl("x", 25, z, 2, -15, 15, COPPER, cap)
        for x in (-8, 0, 8):
            m += pipe_run([(x, 22, z), (x, 25, z)], 0.75)
    m.append(box((-14, 3, 23), (14, 26, 31), {"*": PATINA, "north": PERFORATED, "south": PERFORATED}))
    m.append(gauge("north", (7, 30, 22.75), 3))
    m.append(lamp("north", (-7, 30, 22.75), 2))
    m.append(box((-14, 26, 23), (14, 28, 31), {"*": RUST, "up": f"{FAN}!"}))
    return m


def hydroponic_bay():
    """Three wide, two tall, three deep: a grow house. A rust frame of riveted posts and a grated roof covers three
    long trays of leafy crops lit by amber grow lamps, fed by copper nutrient lines with hex fittings from a ribbed
    green nutrient tank with a blue cap at the back; a red pump with a valve wheel and a gauge post at the front."""
    m = skids(-32, 16, 0, 48)
    for x in (-31, -9, 13):
        for z in (1, 21, 44):
            m.append(box((x, 3, z), (x + 3, 30, z + 3), RUST_PIPE))
    for z in (1, 21, 44):
        m.append(box((-32, 30, z), (16, 32, z + 3), RUST))
    for x in (-31, -9, 13):
        m.append(box((x, 30, 4), (x + 3, 32, 44), RUST))
    for z0 in (4, 18, 30):
        m.append(box((-28, 28, z0 + 4), (12, 30, z0 + 6), RUST_PIPE))
    # Trays of crops on stands, an amber grow light over each.
    for z0 in (4, 18, 30):
        m.append(box((-28, 8, z0), (12, 11, z0 + 10), {"*": RUST, "up": "sp_soil"}))
        m.append(box((-27, 3, z0 + 4), (-24, 8, z0 + 6), RUST_PIPE))
        m.append(box((8, 3, z0 + 4), (11, 8, z0 + 6), RUST_PIPE))
        for x in range(-26, 10, 6):
            m.append(box((x, 11, z0 + 1), (x + 4, 14, z0 + 9), LEAVES))
            m.append(box((x + 1, 14, z0 + 2), (x + 3, 17, z0 + 8), LEAVES))
        m.append(box((-26, 26, z0 + 3), (10, 27.5, z0 + 7), {"*": RUST_BARE, "down": AMBER}))
    # The nutrient tank at the back left and its feed lines.
    m += stack("y", 6, 42, 4, 3, 26, RIB_PATINA, 6, PATINA)
    m += cyl("y", 6, 42, 1.5, 26, 28, BLUE)
    m += pipe_run([(1, 20, 42), (-20, 20, 42), (-20, 12, 42)], 0.75)
    m += pipe_run([(6, 12, 37), (6, 12, 26), (-2, 12, 26)], 0.75)
    m.append(box((-6, 3, 1), (2, 9, 4), {"*": "dr_red", "north": RUST_BARE}))
    m += valve("z", -2, 6, 2.5, 0)
    m.append(gauge("north", (14.5, 20, 0.75), 2.5))
    return m


def electroplating_bath():
    """Four wide, two tall, two deep: a plating line. Three riveted rust tanks of plating solution stand in a row
    under an overhead gantry; a red hoist carriage on the gantry beam lowers a work rack into the middle tank on
    rubber cables; copper busbars with hex fittings run along the tank rims from a green ribbed rectifier with a
    perforated cover, a gauge and an amber lamp at the left end."""
    m = skids(-48, 16, 0, 32)
    for x0 in (-46, -30, -14):
        m.append(box((x0, 3, 6), (x0 + 14, 15, 28), {"*": RUST, "up": BATH}))
        m.append(box((x0 - 0.5, 14, 5.5), (x0 + 14.5, 15.25, 28.5), BAND))
    for z in (7, 27):
        m.append(box((-46, 15.25, z - 0.5), (-2, 16.25, z + 0.5), COPPER))
        m.append(nut("x", 15.75, z, 0.5, -47))
    # The gantry over the tanks and its hoist.
    for x in (-47, -2):
        for z in (2, 29):
            m.append(box((x, 3, z), (x + 2, 31, z + 2), RUST_PIPE))
    for z in (2, 29):
        m.append(box((-47, 29, z), (0, 31, z + 2), RUST))
    m.append(box((-30, 29, 4), (-16, 31, 29), {"*": RUST, "up": GRATE}))
    m.append(box((-28, 24, 12), (-18, 29, 22), "dr_red"))
    m.append(box((-24, 18, 16.5), (-22, 24, 17.5), RUBBER))
    m.append(box((-28, 10, 14), (-18, 18, 20), {"*": RUST_BARE, "north": "sp_mesh", "south": "sp_mesh"}))
    # The rectifier at the left end.
    m.append(box((1, 3, 3), (15, 26, 29), {"*": PATINA, "north": PERFORATED, "up": RUST}))
    m += stack("y", 8, 16, 3, 26, 31, RIB_PATINA, 3, BLUE)
    m.append(gauge("north", (5, 20, 2.75)))
    m.append(lamp("north", (11, 20, 2.75), 2.5))
    m += valve("z", 8, 10, 3, 2)
    m += pipe_run([(2, 16, 7), (-2, 16, 7)], 0.75)
    m += pipe_run([(2, 16, 27), (-2, 16, 27)], 0.75)
    return m


def ammonia_chiller():
    """Two wide, three tall, two deep: an ammonia refrigeration plant. A frosted green ribbed condenser tower with
    rivet bands rises at the left under a turbine fan; a rust compressor with flywheel and red valve wheel sits at the
    bottom right, a banded receiver drum lies at the back, and frosted copper lines with hex fittings link them past
    an amber sight glass and a gauge."""
    m = skids(-16, 16, 0, 32)
    m += stack("y", 6, 12, 8, 3, 42, RIB_PATINA, 7, PATINA)
    for y in (12, 26):
        m += cyl("y", 6, 12, 8.4, y, y + 4, FROST)
    m.append(box((-2, 42, 4), (14, 44, 20), {"*": RUST, "up": f"{FAN}!"}))
    # Compressor at the bottom right with its flywheel.
    m.append(box((-15, 3, 2), (-3, 16, 18), {"*": RUST, "north": RUST_BARE}))
    m += wheel("x", 10, 10, 6, -16, -15, RUST_PIPE, NUT)
    m += valve("z", -9, 12, 2.5, 1.25)
    m.append(box((-12, 5, 1.25), (-6, 8, 2), {"*": RUST_BARE, "north": AMBER}))
    # The receiver drum along x at the back.
    m += flange_cylinder("x", 10, 26, 5, -15, 15, RIB_RUST, BLUE, (-14, -2, 13.75))
    # Frosted lines.
    m += pipe_run([(-9, 16, 10), (-9, 34, 10), (-2, 34, 10)], 1, FROST)
    m += pipe_run([(-9, 10, 18), (-9, 10, 21)], 1, COPPER)
    m.append(gauge("north", (-9, 24, 4.75), 3))
    m.append(lamp("north", (6, 20, 3.5), 2))
    return m


def rocket_workshop():
    """Five wide, three tall, three deep: a rocket assembly hall. Riveted rust posts and an overhead gantry crane with
    a red trolley span a grated floor; a white rocket with a hazard band lies in cradles under a green ribbed hoist
    winch, a long workbench with a red vice and an amber CRT console stands along the back, a tall propellant tank
    with a blue cap and copper lines with hex fittings stands at the left, and amber floodlamps light the bay."""
    m = skids(-64, 16, 0, 48)
    # Posts and the gantry beams.
    for x in (-63, 13):
        for z in (1, 44):
            m.append(box((x, 3, z), (x + 3, 46, z + 3), RUST_PIPE))
            for y in (14, 30):
                m.append(box((x - 0.5, y, z - 0.5), (x + 3.5, y + 1, z + 3.5), BAND))
    for z in (1, 44):
        m.append(box((-63, 42, z), (16, 46, z + 3), RUST))
    m.append(box((-30, 43, 4), (-16, 46, 44), {"*": RUST_BARE, "up": GRATE}))
    m.append(box((-30.5, 44, 4), (-15.5, 45, 44), BAND))
    # The crane trolley and its hook block over the rocket.
    m.append(box((-28, 36, 14), (-18, 43, 24), "dr_red"))
    m += stack("x", 40, 19, 3, -32, -28, RIB_PATINA, 2, BLUE)
    m.append(box((-23.5, 24, 18.5), (-22.5, 36, 19.5), RUBBER))
    m.append(box((-26, 21, 16), (-20, 24, 22), {"*": RUST, "down": NUT}))
    # The rocket in its cradles.
    for x in (-46, -10):
        m.append(box((x, 3, 12), (x + 4, 10, 26), RUST))
    m += cyl("x", 15, 19, 6, -52, -6, "el_white", "dp_chrome")
    m += cyl("x", 15, 19, 6.4, -36, -32, HAZARD)
    m += cyl("x", 15, 19, 4, -6, 0, "dp_chrome")
    m += cyl("x", 15, 19, 2, 0, 4, "dp_chrome")
    for z in (12.5, 25.5):
        m.append(box((-56, 9, z - 1), (-52, 21, z), RUST_BARE))
    m += cyl("x", 15, 19, 4, -58, -52, RUST_PIPE, SOOT)
    # The back bench with its vice and the console.
    m.append(box((-60, 3, 36), (-12, 14, 44), {"*": RUST, "up": RUST_BARE}))
    m.append(box((-56, 14, 38), (-50, 18, 42), "dr_red"))
    m.append(box((-44, 14, 38), (-30, 16, 42), PATINA))
    m.append(box((-10, 3, 34), (10, 28, 44), {"*": PATINA, "north": RUST_BARE}))
    m.append(box((-7, 14, 33.25), (7, 24, 34), {"*": RUST_BARE, "north": f"{CRT}!"}))
    m.append(gauge("north", (-4, 8, 33.75), 3))
    m.append(lamp("north", (4, 8, 33.75), 2))
    # The propellant tank at the front left, with its lines.
    m += stack("y", 6, 10, 6, 3, 38, RIB_RUST, 7, RUST)
    m += cyl("y", 6, 10, 2, 38, 41, BLUE)
    m += pipe_run([(0, 20, 10), (-6, 20, 10), (-6, 20, 19)], 1)
    m += valve("z", 6, 26, 3, 3.25)
    # Floodlamps on the posts.
    for x in (-61.5, 14.5):
        m.append(lamp("north", (x, 38, 0.75), 3))
    return m


MODELS = {
    "coal_generator": coal_generator(),
    "steam_generator": steam_generator(),
    "electric_furnace": electric_furnace(),
    "crusher": crusher(),
    "metal_press": metal_press(),
    "wire_drawer": wire_drawer(),
    "circuit_assembler": circuit_assembler(),
    "pulverizer": pulverizer(),
    "ore_washer": ore_washer(),
    "sieve": sieve(),
    "sawmill": sawmill(),
    "fuel_cell": fuel_cell(),
    "hydroponic_bay": hydroponic_bay(),
    "electroplating_bath": electroplating_bath(),
    "ammonia_chiller": ammonia_chiller(),
    "rocket_workshop": rocket_workshop(),
}
