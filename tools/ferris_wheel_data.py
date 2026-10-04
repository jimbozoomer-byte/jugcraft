"""JSON resources for the Ferris wheel (fall addition 27), from tools/ferris_wheel.py: the booth's block and item models,
its blockstate, names and messages, loot and tags, and the quads the client draws the wheel from
(assets/jugcraft/ferris_wheel_quads.json): the frame (two A-frames of lattice steel on their footings, braced, with the
axle through their bearings), the wheel (a sixteenth of its two trussed rims with their spokes, drawn sixteen times round,
the hub plates, and a car's pivot bar, drawn at each car), its lights (a sixteenth's worth), and a car in each of the four
colours.

Called from agriculture_data.py. The advancements' and recipe's data come through HALLOWEEN_ADVANCEMENTS and SHAPED in
tools/agriculture.py. Every shape is in model pixels (16 to a block) in the wheel's own frame: x along the wheel, y up,
z along the axle, the front at the low z side (north), as block models face; the client turns it to its facing.
  - The frame's origin is the middle of the booth's foot.
  - The wheel's parts' and the lights' origin is the hub; the client turns them about z.
  - A car's origin is its pivot; the client hangs it there, upright.
"""
import json
import math

from decor_data import MOD, rid, box, block_model, flat_item, self_drop, turned
from decor6_data import quads
from ferris_wheel import FERRIS_WHEEL, CAR_COLOURS
import ferris_wheel

HORIZONTAL = ("north", "east", "south", "west")
FULL = (0, 0, 16, 16)
ALL = ("north", "south", "east", "west", "up", "down")
LONG = ("north", "south", "east", "west")

HUB = FERRIS_WHEEL["hub"] * 16
R = FERRIS_WHEEL["radius"] * 16
FEET = FERRIS_WHEEL["frame_feet"] * 16
R_IN = R - 12
RIMS = (-12.0, 12.0)          # the rims' middles along the axle
FRAMES = (-20.0, 20.0)        # the A-frames' middles along the axle
SEGMENTS = 16
SPOKES = 16

FRAME_TEXTURES = {"lattice": "ferris_wheel_lattice", "steel": "ferris_wheel_steel", "axle": "ferris_wheel_axle",
                  "footing": "ferris_wheel_footing", "brass": "ferris_wheel_brass"}
WHEEL_TEXTURES = {"rim": "ferris_wheel_rim", "steel": "ferris_wheel_steel", "hub": "ferris_wheel_hub", "brass": "ferris_wheel_brass",
                  "bulb": "ferris_wheel_bulb"}
CUTOUT = {"ferris_wheel_lattice", "ferris_wheel_valance_pumpkin", "ferris_wheel_valance_cranberry", "ferris_wheel_valance_mustard",
          "ferris_wheel_valance_spruce"}


def car_textures(colour):
    return {"car": f"ferris_wheel_car_{colour}", "canopy": f"ferris_wheel_canopy_{colour}", "valance": f"ferris_wheel_valance_{colour}",
            "seat": "ferris_wheel_seat", "floor": "ferris_wheel_floor", "brass": "ferris_wheel_brass", "steel": "ferris_wheel_steel"}


def with_uvs(element, uvs):
    for side, uv in uvs.items():
        if side in element["faces"]:
            element["faces"][side]["uv"] = list(uv)
    return element


def bar(a, b, width, z0, z1, texture, pieces=1, faces=ALL):
    """A straight bar in the wheel's plane from point `a` to point `b` (x, y), `width` across, from z0 to z1 along the
    axle, in `pieces` lengths each showing the whole texture along it."""
    dx, dy = b[0] - a[0], b[1] - a[1]
    length = math.hypot(dx, dy)
    angle = math.degrees(math.atan2(-dx, dy))
    out = []
    step = length / pieces
    for i in range(pieces):
        # Only the bar's two ends are capped: the joints between its lengths are hidden.
        sides = [f for f in faces if f in LONG or (f == "down" and i == 0) or (f == "up" and i == pieces - 1)]
        e = box((a[0] - width / 2, a[1] + i * step, z0), (a[0] + width / 2, a[1] + (i + 1) * step, z1), texture, faces=sides)
        with_uvs(e, {side: FULL for side in ALL})
        if abs(angle) > 1e-6:
            e["rotations"] = [{"origin": [a[0], a[1], 0.0], "axis": "z", "angle": round(angle, 4)}]
        out.append(e)
    return out


def ring_segment(radius, thick, z0, z1, angle, length, texture):
    """One straight length of a ring `radius` pixels round, `thick` deep (radially), centred at `angle` degrees."""
    # Its ends are hidden in the next lengths.
    e = box((-length / 2, radius - thick / 2, z0), (length / 2, radius + thick / 2, z1), texture, faces=("north", "south", "up", "down"))
    with_uvs(e, {side: FULL for side in ALL})
    e["rotations"] = [{"origin": [0.0, 0.0, 0.0], "axis": "z", "angle": round(angle - 90.0, 4)}]
    return e


def polar(radius, degrees):
    return radius * math.cos(math.radians(degrees)), radius * math.sin(math.radians(degrees))


# ---------------------------------------------------------------- the frame

def leg_x(y):
    """Where an A-frame's leg is across the wheel at height y (its right-hand leg; the left mirrors it)."""
    top = HUB - 4
    return FEET + (4 - FEET) * y / top


def frame():
    """Two A-frames either side of the wheel: lattice legs from footings on the ground to a bearing at the hub, two
    braces across, a cross of bracing between them; beams along the axle tying the frames' feet; the axle."""
    e = []
    top = HUB - 4
    for z in FRAMES:
        z0, z1 = z - 2, z + 2
        for side in (-1, 1):
            e += bar((side * FEET, 0), (side * 4, top), 5, z0, z1, "#lattice", pieces=10)
            # The footing under each leg.
            e.append(with_uvs(box((side * FEET - 7, 0, z - 6), (side * FEET + 7, 2, z + 6), "#footing"), {s: FULL for s in ALL}))
        for y in (44, 92):
            x = leg_x(y)
            e += bar((-x, y), (x, y), 3, z - 1.5, z + 1.5, "#steel", pieces=max(1, round(2 * x / 18)))
        # A cross of bracing between the two braces.
        for side in (-1, 1):
            e += bar((side * leg_x(44), 44), (-side * leg_x(92), 92), 2, z - 1, z + 1, "#steel", pieces=4)
        # The bearing at the top, and its brass cap.
        e.append(with_uvs(box((-7, top - 4, z - 3.5), (7, HUB + 6, z + 3.5), "#steel"), {s: FULL for s in ALL}))
        cap = z - 4.5 if z < 0 else z + 3.5
        e.append(with_uvs(box((-4, HUB - 4, cap), (4, HUB + 4, cap + 1), "#brass"), {s: FULL for s in ALL}))
    # Beams along the axle tying the two frames' feet, and the axle through the bearings.
    for side in (-1, 1):
        e.append(with_uvs(box((side * FEET - 2, 0.5, FRAMES[0]), (side * FEET + 2, 4, FRAMES[1]), "#steel"), {s: FULL for s in ALL}))
    e.append(with_uvs(box((-3, HUB - 3, FRAMES[0] - 4), (3, HUB + 3, FRAMES[1] + 4), "#axle"), {s: FULL for s in ALL}))
    return e


# ---------------------------------------------------------------- the wheel

def section():
    """One sixteenth of the turning wheel, at its top, for the client to draw sixteen times round: for each rim a length
    of the outer ring and of the inner one, the two ties trussing them together, and a spoke from the hub between this
    length and the next."""
    e = []
    outer = 2 * R * math.tan(math.pi / SEGMENTS) + 1.0
    inner = 2 * R_IN * math.tan(math.pi / SEGMENTS) + 1.0
    a = 90.0
    for z in RIMS:
        z0, z1 = z - 1, z + 1
        e.append(ring_segment(R, 3, z0, z1, a, outer, "#rim"))
        e.append(ring_segment(R_IN, 2, z0 + 0.25, z1 - 0.25, a, inner, "#rim"))
        for lo in (a - 11.25, a + 11.25):
            e += bar(polar(R_IN, lo), polar(R - 1.5, a), 1.5, z0 + 0.25, z1 - 0.25, "#steel")
        spoke = a + 360.0 / SPOKES / 2
        e += bar(polar(10, spoke), polar(R_IN, spoke), 2, z0 + 0.25, z1 - 0.25, "#steel", pieces=2)
    return e


def hub():
    """The hub plates on each rim: two squares turned an eighth from each other, the hub's face on the outside."""
    e = []
    for z in RIMS:
        face = "north" if z < 0 else "south"
        zf0, zf1 = (z - 2.5, z + 0.5) if z < 0 else (z - 0.5, z + 2.5)
        for turn in (0.0, 45.0):
            plate = with_uvs(box((-11, -11, zf0), (11, 11, zf1), "#steel", textures={face: "#hub"}), {s: FULL for s in ALL})
            if turn:
                plate["rotations"] = [{"origin": [0.0, 0.0, 0.0], "axis": "z", "angle": turn}]
            e.append(plate)
    return e


def pivot():
    """A car's pivot bar across both rims at the top of the wheel, with a brass collar either side of where the car
    hangs; the client turns it to each car's place."""
    e = [with_uvs(box((-1.5, R - 1.5, RIMS[0] - 1), (1.5, R + 1.5, RIMS[1] + 1), "#steel"), {s: FULL for s in ALL})]
    for zc in (-7.5, 6.5):
        e.append(with_uvs(box((-2, R - 2, zc), (2, R + 2, zc + 1), "#brass"), {s: FULL for s in ALL}))
    return e


def lights():
    """The bulbs of one sixteenth of the wheel, as `section`: on the outer face of each rim one on its outer ring, one
    on its inner ring and two along its spoke (without the face against the steel)."""
    e = []
    a = 90.0
    spoke = a + 360.0 / SPOKES / 2
    for z in RIMS:
        z0, z1 = (z - 2, z - 1) if z < 0 else (z + 1, z + 2)
        shown = ("north", "east", "west", "up", "down") if z < 0 else ("south", "east", "west", "up", "down")
        for (x, y), r in ((polar(R, a), 1.25), (polar(R_IN, spoke), 1.0), (polar(36, spoke), 1.0), (polar(60, spoke), 1.0)):
            e.append(with_uvs(box((x - r, y - r, z0), (x + r, y + r, z1), "#bulb", faces=shown), {s: FULL for s in ALL}))
    return e


# ---------------------------------------------------------------- the cars

def car():
    """A car hanging from its pivot: a yoke and hanger down to a striped canopy with a scalloped valance, brass posts at
    its corners, and the car below, a painted tub with a tufted bench across its back for two, a grab bar across its
    front, on a plank floor. The front (low z) wall is lower, to see out over."""
    e = []
    full = {s: FULL for s in ALL}
    # The yoke round the pivot bar and the hanger.
    e.append(with_uvs(box((-2.5, -2.5, -6.5), (2.5, 2.5, -5), "#brass"), full))
    e.append(with_uvs(box((-2.5, -2.5, 5), (2.5, 2.5, 6.5), "#brass"), full))
    e.append(with_uvs(box((-1, -6, -6.5), (1, -2.5, -5), "#steel"), full))
    e.append(with_uvs(box((-1, -6, 5), (1, -2.5, 6.5), "#steel"), full))
    # The canopy: a striped roof, peaked in the middle, and its valance.
    e.append(with_uvs(box((-13, -7, -10.5), (13, -6, 10.5), "#canopy"), full))
    e.append(with_uvs(box((-10, -6, -8), (10, -5, 8), "#canopy"), full))
    e.append(with_uvs(box((-13, -10, -10.5), (13, -7, -10.5), "#valance", faces=("north", "south")), {"north": FULL, "south": FULL}))
    e.append(with_uvs(box((-13, -10, 10.5), (13, -7, 10.5), "#valance", faces=("north", "south")), {"north": FULL, "south": FULL}))
    e.append(with_uvs(box((-13, -10, -10.5), (-13, -7, 10.5), "#valance", faces=("east", "west")), {"east": FULL, "west": FULL}))
    e.append(with_uvs(box((13, -10, -10.5), (13, -7, 10.5), "#valance", faces=("east", "west")), {"east": FULL, "west": FULL}))
    # Brass posts at the corners, from the tub's rim to the canopy, tall enough for a rider sitting up under it.
    for x in (-11.5, 10.5):
        for z in (-8.5, 7.5):
            e.append(with_uvs(box((x, -27, z), (x + 1, -7, z + 1), "#brass"), full))
    # The tub, hung low so a rider sits with their head under the canopy: floor, back wall, side walls and the lower
    # front wall, the painted panels outside.
    e.append(with_uvs(box((-12, -36, -9), (12, -34, 9), "#steel", textures={"up": "#floor"}), full))
    e.append(with_uvs(box((-12, -34, 8), (12, -26, 9), "#car"), full))
    e.append(with_uvs(box((-12, -34, -9), (-11, -26, 8), "#car"), full))
    e.append(with_uvs(box((11, -34, -9), (12, -26, 8), "#car"), full))
    e.append(with_uvs(box((-11, -34, -9), (11, -29, -8), "#car"), full))
    # A brass rim round the tub's top edge.
    e.append(with_uvs(box((-12.25, -26, 8), (12.25, -25.5, 9.25), "#brass"), full))
    e.append(with_uvs(box((-12.25, -26, -9.25), (-11, -25.5, 8), "#brass"), full))
    e.append(with_uvs(box((11, -26, -9.25), (12.25, -25.5, 8), "#brass"), full))
    e.append(with_uvs(box((-11, -29, -9.25), (11, -28.5, -8), "#brass"), full))
    # The bench and its back, and the grab bar.
    e.append(with_uvs(box((-11, -34, 2), (11, -28, 8), "#seat"), full))
    e.append(with_uvs(box((-11, -28, 6.5), (11, -20, 8), "#seat"), full))
    e.append(with_uvs(box((-10, -25, -7.5), (10, -24, -6.5), "#brass"), full))
    for x in (-10, 9):
        e.append(with_uvs(box((x, -29, -7.5), (x + 1, -25, -6.5), "#brass"), full))
    return e


def drawn(elements, textures):
    out = quads(elements, textures)
    for quad in out:
        if quad["texture"] in CUTOUT:
            quad["cutout"] = True
    return out


def wheel_quads():
    out = {"ferris_wheel_frame": drawn(frame(), FRAME_TEXTURES), "ferris_wheel_section": drawn(section(), WHEEL_TEXTURES),
           "ferris_wheel_hub": drawn(hub(), WHEEL_TEXTURES), "ferris_wheel_pivot": drawn(pivot(), WHEEL_TEXTURES),
           "ferris_wheel_lights": drawn(lights(), WHEEL_TEXTURES)}
    for colour in CAR_COLOURS:
        out[f"ferris_wheel_car_{colour}"] = drawn(car(), car_textures(colour))
    return out


# ---------------------------------------------------------------- the booth

def booth():
    """The booth: a loading platform with a plank deck, its panelled sides painted, and the operator's controls on its
    front: a brass lever leaning in its slot, a speed gauge and two buttons."""
    t = {"side": "ferris_wheel_booth_side", "front": "ferris_wheel_booth_front", "deck": "ferris_wheel_booth_deck",
         "brass": "ferris_wheel_brass", "steel": "ferris_wheel_steel", "gauge": "ferris_wheel_gauge"}
    e = [box((0, 0, 0), (16, 13, 16), "#side", textures={"north": "#front", "up": "#deck"}),
         box((-0.5, 13, -0.5), (16.5, 16, 16.5), "#deck", textures={"down": "#steel"}),
         # The lever in its slot, leaning forward, and its knob.
         box((2.5, 6, -0.5), (5.5, 7, 0), "#steel"),
         box((3.5, 6.5, -1.5), (4.5, 11.5, -0.5), "#brass", rotation={"origin": [4, 6.5, -1], "axis": "x", "angle": -22.5}),
         box((3, 11, -2.5), (5, 13, -0.5), "#brass", rotation={"origin": [4, 6.5, -1], "axis": "x", "angle": -22.5}),
         # The gauge, and two buttons under it.
         box((9, 7, -0.75), (14, 12, 0), "#steel", textures={"north": "#gauge"}, uvs={"north": FULL}),
         box((9.5, 4, -0.75), (11, 5.5, 0), "#brass"), box((12, 4, -0.75), (13.5, 5.5, 0), "#brass")]
    return block_model(t, e, "ferris_wheel_booth_side")


def assets(root, write, lang):
    name = FERRIS_WHEEL["block"]
    write(root / "models" / "block" / f"{name}.json", booth())
    write(root / "blockstates" / f"{name}.json", {"variants": {f"facing={f}": turned(rid(f"block/{name}"), f) for f in HORIZONTAL}})
    flat_item(root, write, name)
    # One quad to a line: the file is large, and this keeps it a quarter the size of the indented form.
    path = root / "ferris_wheel_quads.json"
    path.write_text("{\n" + ",\n".join(json.dumps(name) + ": [\n" + ",\n".join(json.dumps(q, separators=(",", ":")) for q in qs) + "\n]"
                                         for name, qs in wheel_quads().items()) + "\n}\n", encoding="utf-8")
    lang[f"block.{MOD}.{name}"] = FERRIS_WHEEL["display"]
    lang[f"entity.{MOD}.{FERRIS_WHEEL['entity']}"] = FERRIS_WHEEL["entity_display"]
    lang[f"message.{MOD}.ferris_wheel.no_room"] = "The Ferris Wheel needs a clear space %s blocks across, %s high and %s deep"
    lang[f"message.{MOD}.ferris_wheel.full"] = "The car at the bottom is full: wait for the next"
    lang[f"message.{MOD}.ferris_wheel.jammed"] = "The Ferris Wheel is jammed: something is in a car's way"


def loot(out, write):
    write(out / f"{FERRIS_WHEEL['block']}.json", self_drop(FERRIS_WHEEL["block"]))


def tags(tags):
    tags.add("block", "minecraft:mineable/pickaxe", rid(FERRIS_WHEEL["block"]))
