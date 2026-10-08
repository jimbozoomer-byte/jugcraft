"""Pipeworks: twelve industrial pipe and tank props, after the owner's own set of grey-box renders of
8 October 2026 (pipe runs on pedestal stands, flanged segments, a four-line pipe rack, lattice pipe bridges, a hooped
pipeline, horizontal tanks on saddles with walkways and ladders, stacked tanks in a frame, a ribbed drum and a tall
pipe overpass). They are decoration in the clean Dieselworks steel (docs/ART_DIRECTION.md, Texturing: keep it clean),
placed and broken as one like the haunted archway (building/PipeworksBlock.java).

Each prop is ONE model in structure space, in pixels, facing north: the master block (part 0) is 0..16 on every axis,
the prop reaches to the placer's right (-x), up (+y) and away from them (+z). tools/pipeworks.py cuts it into one model
a block with model_writer.split_model and writes a Blockbench project of the whole prop to art/pipeworks/.

Every shape is built from the steampunk helpers (box, cyl, dial, wheel): stepped round prisms for every pipe, flange,
collar and tank, so nothing round is a flat cube. Rotated elements (the trusses' diagonals) stay inside one block.
"""
import math

from steampunk_models import box, cyl, dial, wheel, q

STEEL, BORE, FLANGE, TREAD = "pw_steel", "pw_bore", "pw_flange", "pw_tread"
PLATE, RIVET, BAND, HAZARD, RED, GAUGE = "dw_steel_plate", "dw_steel_plate_riveted", "dw_steel_band", "dp_hazard", "dr_red", "sp_gauge"

# ------------------------------------------------------------------ parts


def pipe(axis, cu, cv, r, a0, a1, cap=STEEL):
    """A round pipe along `axis`, centred at (cu, cv) in the other two axes (x, y, z order)."""
    return cyl(axis, cu, cv, r, a0, a1, STEEL, cap)


def open_pipe(axis, cu, cv, r, a0, a1):
    """A pipe whose ends are open (dark bores), so runs placed end to end read as one line."""
    return cyl(axis, cu, cv, r, a0, a1, STEEL, BORE)


def flange(axis, cu, cv, r, a, width=1.5):
    """A bolted flange ring round a pipe of radius r, `width` thick from `a` along the axis."""
    return cyl(axis, cu, cv, r + 1.5, a, a + width, STEEL, FLANGE)


def joint(axis, cu, cv, r, a):
    """A flanged joint at `a`: two flanges with a thin gasket between them."""
    return (flange(axis, cu, cv, r, a - 2) + cyl(axis, cu, cv, r + 0.75, a - 0.5, a + 0.5, BAND)
            + flange(axis, cu, cv, r, a + 0.5))


def collar(axis, cu, cv, r, a, width=1.5):
    """A riveted strap or hoop round a pipe or tank."""
    return cyl(axis, cu, cv, r + 0.6, a, a + width, BAND)


def pedestal(cx, cz, top, base=8, column=4):
    """A pipe stand: a riveted base plate, a round column and a square saddle block whose top is at `top`."""
    h = base / 2
    c = column / 2
    m = [box((cx - h, 0, cz - h), (cx + h, 1, cz + h), {"*": PLATE, "up": RIVET})]
    m += cyl("y", cx, cz, c, 1, top - 1.75, STEEL)
    m.append(box((cx - c - 1, top - 1.75, cz - c - 1), (cx + c + 1, top, cz + c + 1), PLATE))
    return m


def bracket(cx, cz, top, width=10, depth=6):
    """A wall-style pipe support: a foot plate, a web and a cradle whose top is at `top`."""
    w, d = width / 2, depth / 2
    return [box((cx - w, 0, cz - d), (cx + w, 1, cz + d), {"*": PLATE, "up": RIVET}),
            box((cx - 1.5, 1, cz - 1.5), (cx + 1.5, top - 1, cz + 1.5), STEEL),
            box((cx - w + 2, top - 1, cz - 2), (cx + w - 2, top, cz + 2), PLATE)]


def post(x0, y0, z0, x1, y1, z1, foot=True):
    """A square steel post standing on a riveted foot plate."""
    m = [box((x0, y0, z0), (x1, y1, z1), STEEL)]
    if foot:
        m.append(box((x0 - 1, y0, z0 - 1), (x1 + 1, y0 + 1, z1 + 1), {"*": PLATE, "up": RIVET}))
    return m


def truss(x0, x1, y0, y1, z0, z1, panel=8):
    """A lattice girder along z between x0..x1 and y0..y1: two plate chords, a vertical every `panel` pixels and a
    45-degree diagonal in each panel, alternating, on both faces. Each diagonal stays inside its own block."""
    m = [box((x0, y0, z0), (x1, y0 + 2, z1), PLATE), box((x0, y1 - 2, z0), (x1, y1, z1), PLATE)]
    gap = y1 - y0 - 4
    yc = (y0 + y1) / 2
    span = gap + 1.2  # pokes 0.6 into each chord
    length = span * math.sqrt(2)
    panels = int(round((z1 - z0) / panel))
    for i in range(panels + 1):
        z = z0 + i * panel
        zl, zh = (z, z + 1.5) if i == 0 else (z - 1.5, z) if i == panels else (z - 0.75, z + 0.75)
        for x in (x0, x1 - 1.5):
            m.append(box((x, y0 + 1.5, zl), (x + 1.5, y1 - 1.5, zh), STEEL))
    for i in range(panels):
        zc = z0 + (i + 0.5) * panel
        angle = 45 if i % 2 == 0 else -45
        for x in (x0 + 0.25, x1 - 1.25):
            m.append(box((x, yc - length / 2, zc - 0.5), (x + 1, yc + length / 2, zc + 0.5), STEEL,
                         rotation=("x", angle, (x + 0.5, yc, zc))))
    return m


def railing(x0, x1, z0, z1, y, height=12, spacing=16, open_front=0):
    """A handrail round a deck: posts every `spacing` along both long sides, a top and a knee rail, end rails at the
    back and (unless `open_front` leaves a ladder opening that wide in the middle) at the front."""
    m = []
    zs = [z0 + 0.5]
    while zs[-1] + spacing < z1 - 0.5:
        zs.append(zs[-1] + spacing)
    zs.append(z1 - 0.5)
    for x in (x0 + 0.5, x1 - 0.5):
        for z in zs:
            m.append(box((x - 0.5, y, z - 0.5), (x + 0.5, y + height, z + 0.5), STEEL))
        for ry in (y + height - 1, y + height / 2 - 0.5):
            m.append(box((x - 0.4, ry, z0 + 0.1), (x + 0.4, ry + 1, z1 - 0.1), STEEL))
    for z, open_ in ((z1 - 0.5, 0), (z0 + 0.5, open_front)):
        for ry in (y + height - 1, y + height / 2 - 0.5):
            if open_:
                mid = (x0 + x1) / 2
                m.append(box((x0 + 0.9, ry, z - 0.4), (mid - open_ / 2, ry + 1, z + 0.4), STEEL))
                m.append(box((mid + open_ / 2, ry, z - 0.4), (x1 - 0.9, ry + 1, z + 0.4), STEEL))
            else:
                m.append(box((x0 + 0.9, ry, z - 0.4), (x1 - 0.9, ry + 1, z + 0.4), STEEL))
    return m


def ladder(cx, y0, y1, z, half=4, rung=4):
    """A steel ladder on the front (-z) face at z..z+1: two stiles and a rung every `rung` pixels."""
    m = [box((cx - half - 0.5, y0, z), (cx - half + 0.5, y1, z + 1), STEEL),
         box((cx + half - 0.5, y0, z), (cx + half + 0.5, y1, z + 1), STEEL)]
    y = y0 + rung / 2
    while y + 0.75 < y1 - 1:
        m.append(box((cx - half + 0.5, y, z + 0.1), (cx + half - 0.5, y + 0.75, z + 0.9), STEEL))
        y += rung
    return m


def valve(axis, cu, cv, r, a):
    """A red handwheel on a short stem, its rim at a..a+0.75."""
    return wheel(axis, cu, cv, r, a, a + 0.75, RED, STEEL)


def stem_valve_up(cx, cz, y, r=2.5):
    """A valve standing up from a pipe or tank top: a short stem and a red wheel lying flat."""
    return cyl("y", cx, cz, 0.75, y, y + 2, STEEL) + valve("y", cx, cz, r, y + 2)


def gauge(face, center, size=3.5):
    return [dial(face, center, size, texture=GAUGE, body=STEEL)]


def nozzle_up(cx, cz, y0, y1, r=3):
    """A flanged nozzle standing up out of a tank, with a valve wheel on top."""
    return pipe("y", cx, cz, r, y0, y1) + flange("y", cx, cz, r, y1 - 1.5) + stem_valve_up(cx, cz, y1, r)


def manway(cx, cz, y, r=4):
    """A bolted manway on a tank top: a short wide neck, a flange and a cover with a hinge lug."""
    return (pipe("y", cx, cz, r, y, y + 3) + flange("y", cx, cz, r, y + 3)
            + [box((cx - 1, y + 4.5, cz + r - 0.5), (cx + 1, y + 5.5, cz + r + 1.5), STEEL)])


def heads(axis, cu, cv, r, a0, a1):
    """Dished ends on a tank shell that runs a0..a1 along the axis: two shrinking steps at each end."""
    m = []
    for a, d in ((a0, -1), (a1, 1)):
        for k, (rk, w) in enumerate(((0.86, 2.5), (0.62, 1.5))):
            start = a + d * (2.5 * k if k else 0)
            lo, hi = (start - w, start) if d < 0 else (start, start + w)
            m += cyl(axis, cu, cv, r * rk, lo, hi, STEEL)
    return m


def tank(cz0, cz1, cy, r=11, cx=0):
    """A horizontal tank along z: a shell with dished heads at both ends."""
    return pipe("z", cx, cy, r, cz0, cz1) + heads("z", cx, cy, r, cz0, cz1)


def saddle(cx, cz, top, half=12, depth=2.5):
    """A tank saddle: a riveted foot plate and a block up to `top`, where the shell sits."""
    return [box((cx - half - 2, 0, cz - depth - 1), (cx + half + 2, 1, cz + depth + 1), {"*": PLATE, "up": RIVET}),
            box((cx - half, 1, cz - depth), (cx + half, top, cz + depth), PLATE)]


# ------------------------------------------------------------------ props

def pipe_stand_run():
    """One wide, one tall, four long: a steel pipe on two pedestal stands, strapped down, with a flanged joint in the
    middle; its ends are open, so runs set end to end read as one line."""
    m = open_pipe("z", 8, 10, 3, 0, 64)
    m += joint("z", 8, 10, 3, 32)
    for z in (8, 56):
        m += pedestal(8, z, 7.5)
        m += collar("z", 8, 10, 3, z - 0.75)
    return m


def blind_flange_stub():
    """One block: a short pipe on a riveted plinth, blanked off with a bolted flange at each end, a valve on top."""
    m = [box((3, 0, 3), (13, 7, 13), {"*": RIVET, "up": PLATE})]
    m += pipe("z", 8, 10.5, 3, 2, 14)
    m += flange("z", 8, 10.5, 3, 0.5)
    m += flange("z", 8, 10.5, 3, 14)
    m += stem_valve_up(8, 8, 13.5, 2)
    return m


def flanged_pipe():
    """One wide, three long: a pipe on two low brackets with flanged joints at both block seams and open ends."""
    m = open_pipe("z", 8, 9, 3, 0, 48)
    for z in (16, 32):
        m += joint("z", 8, 9, 3, z)
    for z in (6, 42):
        m += bracket(8, z, 6.5)
        m += collar("z", 8, 9, 3, z - 0.75)
    return m


def pipe_rack():
    """Two wide, four long: three pipes side by side on two cradle stands, strapped down, and a fourth line on a
    second tier above the middle one."""
    m = []
    for z in (8, 56):
        for x in (-14, 11):
            m += post(x, 0, z - 1.5, x + 3, 4, z + 1.5)
        m.append(box((-14.5, 4, z - 2), (14.5, 6, z + 2), PLATE))
        for x in (-5, 3):
            m.append(box((x, 6, z - 1), (x + 2, 10, z + 1), STEEL))
        m.append(box((-6, 10, z - 1.5), (6, 11.25, z + 1.5), PLATE))
    for x in (-9, 0, 9):
        m += open_pipe("z", x, 8.5, 2.5, 0, 64)
        for z in (8, 56):
            m += collar("z", x, 8.5, 2.5, z - 0.75)
    m += open_pipe("z", 0, 13.25, 2.25, 0, 64)
    m += joint("z", 0, 13.25, 2.25, 32)
    for z in (8, 56):
        m += collar("z", 0, 13.25, 2.25, z - 0.75)
    return m


def pipe_bridge():
    """One wide, two tall, six long: a lattice girder on four legs carrying two pipes."""
    m = truss(2, 14, 16, 26, 0, 96)
    for z0 in (2, 91):
        for x0 in (2.5, 10.5):
            m += post(x0, 0, z0, x0 + 3, 16, z0 + 3)
        m.append(box((5.5, 7, z0 + 0.5), (10.5, 9, z0 + 2.5), PLATE))
    for x in (5, 11):
        m += open_pipe("z", x, 28.5, 2.5, 0, 96)
        for z in (16, 48, 80):
            m += collar("z", x, 28.5, 2.5, z - 0.75)
    m += joint("z", 5, 28.5, 2.5, 32)
    m += joint("z", 11, 28.5, 2.5, 64)
    return m


def standpipe_frame():
    """Two wide, two tall, two deep: three capped risers with handwheels and gauges on a header pipe, inside a steel
    frame with a tread-plate floor."""
    m = [box((-14, 0, 2), (14, 1, 30), {"*": PLATE, "up": TREAD})]
    for x0 in (-14, 11):
        for z0 in (2, 27):
            m.append(box((x0, 1, z0), (x0 + 3, 32, z0 + 3), STEEL))
    for y in (1, 30):
        for z0 in (2, 27):
            m.append(box((-11, y, z0 + 0.5), (11, y + 2, z0 + 2.5), PLATE))
        for x0 in (-13.5, 11.5):
            m.append(box((x0, y, 5), (x0 + 2, y + 2, 27), PLATE))
    m += pipe("x", 10, 16, 2.5, -12, 12)
    m += flange("x", 10, 16, 2.5, -13.5)
    m += flange("x", 10, 16, 2.5, 12)
    for x in (-8, 0, 8):
        m += pipe("y", x, 16, 2.5, 1, 27)
        m += flange("y", x, 16, 2.5, 25.5)
        m += cyl("y", x, 16, 2.9, 27.25, 29, STEEL)
        m += cyl("z", x, 20, 0.75, 13.75, 16, STEEL)
        m += valve("z", x, 20, 2.5, 13)
        m += gauge("north", (x, 24, 13), 3)
    return m


def horizontal_tank():
    """Two wide, two tall, four long: a horizontal tank with dished heads on two riveted saddles, strapped down, with a
    valved nozzle and a manway on top, a flanged side outlet and a gauge on its front head."""
    m = tank(6, 58, 15)
    for z in (16, 48):
        m += saddle(0, z, 5)
        m += collar("z", 0, 15, 11, z - 0.75)
    m += nozzle_up(0, 24, 24, 29.5)
    m += manway(0, 44, 25.5)
    m += pipe("x", 15, 50, 2.5, 9, 16)
    m += flange("x", 15, 50, 2.5, 13.5)
    m += gauge("north", (0, 15, 1.75))
    return m


def tank_walkway():
    """Two wide, three tall, four long: the horizontal tank under a tread-plate walkway with handrails, reached by a
    ladder on its front end; the nozzle comes up through the deck."""
    m = tank(6, 58, 15)
    for z in (16, 48):
        m += saddle(0, z, 5)
        m += collar("z", 0, 15, 11, z - 0.75)
        for x0 in (-14.5, 12.5):
            m.append(box((x0, 5, z - 1), (x0 + 2, 29, z + 1), STEEL))
    m.append(box((-15, 29, 2), (15, 31, 62), {"*": PLATE, "up": TREAD}))
    m += railing(-15, 15, 2, 62, 31, open_front=10)
    m += ladder(0, 0, 44, 0.5)
    m += nozzle_up(0, 24, 24, 34.5)
    m += manway(0, 44, 31)
    m += pipe("x", 15, 50, 2.5, 9, 16)
    m += flange("x", 15, 50, 2.5, 13.5)
    m += gauge("north", (8, 15, 1.75))
    return m


def stacked_tanks():
    """Two wide, four tall, four long: two horizontal tanks in a steel frame, one over the other, strapped to their
    saddles, a drain between them, a valved nozzle on top and a ladder up the front."""
    m = []
    for cy in (12, 40):
        m += tank(6, 58, cy, r=10)
    for x0 in (-15, 12):
        for z0 in (2, 59):
            m.append(box((x0, 0, z0), (x0 + 3, 52, z0 + 3), STEEL))
        for y in (0, 26, 50):
            m.append(box((x0, y, 5), (x0 + 3, y + 2, 59), PLATE))
    for y in (0, 26, 50):
        for z0 in (2, 59):
            m.append(box((-12, y, z0 + 0.5), (12, y + 2, z0 + 2.5), PLATE))
    for z in (16, 48):
        for y in (0, 26):
            m.append(box((-11, y, z - 2.5), (11, y + 2.5 if y else 2, z + 2.5), PLATE))
        m += collar("z", 0, 12, 10, z - 0.75)
        m += collar("z", 0, 40, 10, z - 0.75)
    m += pipe("y", 0, 40, 2, 21, 31)
    m += flange("y", 0, 40, 2, 22)
    m += flange("y", 0, 40, 2, 28.5)
    m += nozzle_up(0, 24, 49, 55.5)
    m += ladder(0, 0, 54, 0.5)
    m += gauge("north", (8, 12, 1.75), 3)
    m += gauge("north", (8, 40, 1.75), 3)
    return m


def pipeline_hoops():
    """One wide, two tall, six long: a big pipe in riveted hoops on a lattice girder, a flanged joint midway."""
    m = truss(2, 14, 10, 20, 0, 96)
    for z0 in (2, 91):
        for x0 in (2.5, 10.5):
            m += post(x0, 0, z0, x0 + 3, 10, z0 + 3)
    m += open_pipe("z", 8, 26, 6, 0, 96)
    for z in (8, 24, 40, 56, 72, 88):
        m += collar("z", 8, 26, 6, z - 1, 2)
    m += joint("z", 8, 26, 6, 48)
    return m


def ribbed_drum():
    """Two wide, two tall, five long: a long ribbed drum with bolted ends on cradles over two sill beams, a manway
    on top and a gauge on its front end."""
    m = pipe("z", 0, 17, 11, 4, 76)
    for z in range(8, 76, 8):
        m += cyl("z", 0, 17, 11.75, z - 0.5, z + 0.5, STEEL)
    for z0, z1 in ((2, 4), (76, 78)):
        m += cyl("z", 0, 17, 9.5, z0, z1, STEEL, FLANGE)
    for x0 in (-13, 10):
        m.append(box((x0, 0, 2), (x0 + 3, 2, 78), PLATE))
    for z in (6, 22, 38, 54, 70):
        m.append(box((-13, 2, z - 1.5), (13, 6.5, z + 1.5), PLATE))
    m += manway(0, 40, 27.5)
    m += gauge("north", (0, 17, 1.5), 3.5)
    return m


def pipe_overpass():
    """One wide, three tall, five long: a big pipe carried high on two portal frames, hooped where it rests on the
    crossbeams, with a flanged joint midway."""
    m = []
    for z0 in (2, 74):
        for x0 in (2, 11):
            m += post(x0, 0, z0, x0 + 3, 36, z0 + 4)
        m.append(box((1.5, 36, z0 - 0.5), (14.5, 39, z0 + 4.5), PLATE))
        m.append(box((4.5, 30, z0 + 1), (11.5, 32, z0 + 3), PLATE))
    m += open_pipe("z", 8, 43.5, 4.5, 0, 80)
    for z in (4, 76):
        m += collar("z", 8, 43.5, 4.5, z - 1, 2)
    m += joint("z", 8, 43.5, 4.5, 40)
    return m


# id: (display name, (across, up, away) in blocks, model, collision boxes {x0, y0, z0, x1, y1, z1} in structure space)
PROPS = {
    "pipe_stand_run": ("Pipe Run", (1, 1, 4), pipe_stand_run,
                       [(4.5, 0, 0, 11.5, 13.5, 64)]),
    "blind_flange_stub": ("Capped Pipe Stub", (1, 1, 1), blind_flange_stub,
                          [(3, 0, 3, 13, 7, 13), (3.5, 6, 0.5, 12.5, 15, 15.5)]),
    "flanged_pipe": ("Flanged Pipe", (1, 1, 3), flanged_pipe,
                     [(3, 0, 0, 13, 12.5, 48)]),
    "pipe_rack": ("Pipe Rack", (2, 1, 4), pipe_rack,
                  [(-14.5, 0, 0, 14.5, 11.5, 64), (-2.5, 11, 0, 2.5, 15.5, 64)]),
    "pipe_bridge": ("Pipe Bridge", (1, 2, 6), pipe_bridge,
                    [(2, 0, 0, 14, 31.5, 96)]),
    "standpipe_frame": ("Standpipe Frame", (2, 2, 2), standpipe_frame,
                        [(-14, 0, 2, 14, 32, 30)]),
    "horizontal_tank": ("Horizontal Tank", (2, 2, 4), horizontal_tank,
                        [(-14, 0, 2, 14, 26.5, 62), (-5, 26, 19, 5, 32, 49)]),
    "tank_walkway": ("Tank Walkway", (2, 3, 4), tank_walkway,
                     [(-15, 0, 0, 15, 31, 62), (-15, 31, 2, 15, 43, 62)]),
    "stacked_tanks": ("Stacked Tanks", (2, 4, 4), stacked_tanks,
                      [(-15, 0, 0, 15, 52, 62), (-5, 52, 19, 5, 56, 29)]),
    "pipeline_hoops": ("Hooped Pipeline", (1, 2, 6), pipeline_hoops,
                       [(1.5, 0, 0, 14.5, 32, 96)]),
    "ribbed_drum": ("Ribbed Drum", (2, 2, 5), ribbed_drum,
                    [(-13, 0, 2, 13, 29, 78), (-5, 28, 35, 5, 32, 45)]),
    "pipe_overpass": ("Pipe Overpass", (1, 3, 5), pipe_overpass,
                      [(1, 0, 1, 15, 39, 7), (1, 0, 73, 15, 39, 79), (3, 38, 0, 13, 48, 80)]),
}


def cells(size):
    """The blocks of a prop in part order, as PipeworksBlock counts them: right (toward -x) first, then up, then
    away, part = right + across x (up + up-count x away)."""
    w, h, d = size
    return [(r, u, a) for a in range(d) for u in range(h) for r in range(w)]


def footprint(size):
    """The same blocks as model_writer offsets (x east, y up, z south) from the master block."""
    return [(-r, u, a) for r, u, a in cells(size)]


MODELS = {name: spec[2]() for name, spec in PROPS.items()}
