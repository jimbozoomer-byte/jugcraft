"""Rocketry, batch 38 (docs/features/rocketry.md): propellant chemistry, the rocket workshop and the first rockets.

Only rockets that come back down to earth: no space launches yet (the owner's choice, 3 October 2026).
- Chemistry: iodine from kelp (sulfuric acid), ammonium perchlorate from salt and ammonia, silver iodide.
- The rocket workshop (MachineKind.ROCKET_WORKSHOP, recipe type rocket_assembly) makes solid propellant, rocket
  parts and rockets.
- Rockets (Java: rocketry/RocketItem): a survey rocket, a cloud-seeding rocket, a clear-sky rocket, signal flares and
  illumination flares. Each rises as a firework and does its work at the top ({@link #LAUNCH_DELAY} ticks later).

tools/check_mod_data.py keeps the numbers here and in Java the same.
"""
import math

MOD = "jugcraft"

# Ticks from launch until a rocket does its work (it bursts at about that height).
LAUNCH_DELAY = 40
# Survey rocket: chunks surveyed in each direction (3: 7x7 chunks) and the column stride.
SURVEY_RADIUS = 3
SURVEY_STRIDE = 4
# Weather rockets: how long the rain or clear sky lasts, and the shared cooldown between weather rockets.
WEATHER_TICKS = 6_000
WEATHER_COOLDOWN = 2_400
# Illumination flare: hostile mobs within this many blocks glow for GLOW_TICKS.
FLARE_RADIUS = 48
GLOW_TICKS = 600
# Signal flare: players within this many blocks are told where it went up.
SIGNAL_RANGE = 512
# Cooldown after firing any rocket (ticks).
COOLDOWN = 20

ITEMS = {
    "iodine": "Iodine",
    "ammonium_perchlorate": "Ammonium Perchlorate",
    "silver_iodide": "Silver Iodide",
    "solid_propellant": "Solid Propellant",
    "rocket_casing": "Rocket Casing",
    "rocket_nozzle": "Rocket Nozzle",
    "guidance_unit": "Guidance Unit",
    "rocket_motor": "Rocket Motor",
    "survey_rocket": "Survey Rocket",
    "cloud_seeding_rocket": "Cloud-Seeding Rocket",
    "clear_sky_rocket": "Clear-Sky Rocket",
    "signal_flare": "Signal Flare",
    "illumination_flare": "Illumination Flare",
    # Batch 39: the rocket post.
    "delivery_rocket": "Delivery Rocket",
    "flight_plan": "Flight Plan",
}
ROCKETS = ["survey_rocket", "cloud_seeding_rocket", "clear_sky_rocket", "signal_flare", "illumination_flare"]
TOOLTIPS = {
    "iodine": "Leached from kelp with sulfuric acid.",
    "ammonium_perchlorate": "The oxidizer in solid rocket fuel.",
    "silver_iodide": "Seeds clouds: water freezes onto its crystals.",
    "solid_propellant": "Ammonium perchlorate and aluminum in a rubber binder.",
    "rocket_motor": "A loaded solid-fuel motor, ready for a payload.",
    "survey_rocket": "Fire it straight up: from the top it surveys the ores and oil under 7x7 chunks.",
    "cloud_seeding_rocket": "Fire it into the sky to bring rain for five minutes. Weather rockets share a two-minute "
                            "cooldown.",
    "clear_sky_rocket": "Fire it into the sky to clear rain and storms for five minutes. Weather rockets share a "
                        "two-minute cooldown.",
    "signal_flare": "A red star burst seen from far away; players within 512 blocks are told where it went up.",
    "illumination_flare": "A white burst that makes hostile mobs within 48 blocks glow for 30 seconds.",
    "delivery_rocket": "Carries a rocket pad's cargo to the pad its flight plan names. Used up on launch.",
}

# Batch 39 (docs/features/rocket-post.md): rocket pads send their cargo to another pad (Java: rocketry/RocketPost,
# RocketPadBlockEntity). Range in blocks, flight time (a minimum plus blocks per tick), how often waiting deliveries
# are checked, and the pad's slots. A delivery whose target area is not loaded waits and lands when it loads.
POST_RANGE = 4_096
POST_MIN_FLIGHT = 60
POST_BLOCKS_PER_TICK = 4
POST_CHECK_INTERVAL = 20
PAD_CARGO = 9
BLOCKS = {"rocket_pad": "Rocket Pad"}
PAD_RESULTS = {
    "none": "", "launched": "Launched!", "no_rocket": "No rocket", "no_plan": "No flight plan",
    "no_cargo": "No cargo", "same_pad": "That is this pad", "other_dimension": "Other dimension",
    "too_far": "Too far away", "no_pad": "No pad there", "no_sky": "Roof overhead",
}


def items():
    return list(ITEMS)


def blocks():
    return list(BLOCKS)


def workshop_recipes():
    """Rocket workshop recipes (up to three ingredients in any slots), for machines.machine_recipes()."""
    feature = ["machines"]
    rid = lambda path: f"{MOD}:{path}"
    return [
        {"inputs": [[rid("ammonium_perchlorate"), 2], [rid("aluminum_nugget"), 3], [rid("rubber"), 1]],
         "output": rid("solid_propellant"), "count": 2, "ticks": 200, "features": feature + ["aluminum"]},
        {"inputs": [[rid("steel_plate"), 3]], "output": rid("rocket_casing"), "count": 1, "ticks": 160,
         "features": feature},
        {"inputs": [[rid("tungsten_ingot"), 1], [rid("steel_plate"), 1]], "output": rid("rocket_nozzle"), "count": 2,
         "ticks": 160, "features": feature + ["tungsten"]},
        {"inputs": [[rid("processor"), 1], [rid("microchip"), 2], [rid("copper_wire"), 2]],
         "output": rid("guidance_unit"), "count": 1, "ticks": 300, "features": feature + ["silicon"]},
        {"inputs": [[rid("rocket_casing"), 1], [rid("rocket_nozzle"), 1], [rid("solid_propellant"), 2]],
         "output": rid("rocket_motor"), "count": 1, "ticks": 200, "features": feature},
        {"inputs": [[rid("rocket_motor"), 1], [rid("guidance_unit"), 1], [rid("sensor"), 1]],
         "output": rid("survey_rocket"), "count": 1, "ticks": 300, "features": feature},
        {"inputs": [[rid("rocket_motor"), 1], [rid("silver_iodide"), 2]],
         "output": rid("cloud_seeding_rocket"), "count": 1, "ticks": 200, "features": feature},
        {"inputs": [[rid("rocket_motor"), 1], [rid("guncotton"), 2]],
         "output": rid("clear_sky_rocket"), "count": 1, "ticks": 200, "features": feature},
        {"inputs": [[rid("rocket_motor"), 1], [rid("rocket_casing"), 1], [rid("guidance_unit"), 1]],
         "output": rid("delivery_rocket"), "count": 1, "ticks": 300, "features": feature},
        {"inputs": [[rid("solid_propellant"), 1], ["minecraft:paper", 2], ["minecraft:red_dye", 1]],
         "output": rid("signal_flare"), "count": 4, "ticks": 100, "features": feature},
        {"inputs": [[rid("solid_propellant"), 1], ["minecraft:paper", 2], ["minecraft:glowstone_dust", 1]],
         "output": rid("illumination_flare"), "count": 4, "ticks": 100, "features": feature},
    ]


# Chemical reactor recipes (tools/petro.py FLUID_RECIPES["chemical_reactor"]).
REACTOR_RECIPES = [
    # Iodine leached from kelp ash with sulfuric acid (where iodine was first found, and long made).
    {"name": "iodine", "items": [("minecraft:dried_kelp", 8)], "fluids": [("jugcraft:sulfuric_acid", 100)],
     "results": [("jugcraft:iodine", 1)], "ticks": 120, "features": ["machines", "sulfur"]},
    # Perchlorate from salt (electrolysed to perchlorate in the plant) neutralized with ammonia.
    {"name": "ammonium_perchlorate", "items": [("jugcraft:salt", 1)], "fluids": [("jugcraft:ammonia", 250)],
     "results": [("jugcraft:ammonium_perchlorate", 2)], "ticks": 120, "features": ["machines", "salt"]},
]


def write_all(write, assets, data, lang, condition, self_drop):
    for item, name in ITEMS.items():
        lang[f"item.{MOD}.{item}"] = name
        write(assets / "models" / "item" / f"{item}.json",
              {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MOD}:item/{item}"}})
        write(assets / "items" / f"{item}.json", {"model": {"type": "minecraft:model", "model": f"{MOD}:item/{item}"}})
    for key, text in TOOLTIPS.items():
        lang[f"tooltip.{MOD}.{key}"] = text
    lang[f"message.{MOD}.rocket.indoors"] = "No open sky above: a rocket needs a clear path up"
    lang[f"message.{MOD}.rocket.no_weather"] = "There is no weather here"
    lang[f"message.{MOD}.rocket.weather_cooldown"] = "The sky is still settling: wait %s seconds"
    lang[f"message.{MOD}.rocket.rain"] = "Clouds seeded: rain is coming"
    lang[f"message.{MOD}.rocket.clear"] = "Clouds dispersed: the sky is clearing"
    lang[f"message.{MOD}.rocket.signal"] = "%s fired a signal flare at %s, %s, %s"
    lang[f"message.{MOD}.rocket.illuminated"] = "The flare lights up %s hostile creatures"
    write(data / "recipe" / "silver_iodide.json", {
        "fabric:load_conditions": condition("machines"), "type": "minecraft:crafting_shapeless", "category": "misc",
        "ingredients": [f"{MOD}:silver_dust", f"{MOD}:iodine"], "result": {"id": f"{MOD}:silver_iodide", "count": 2}})
    write_post(write, assets, data, lang, condition, self_drop)


def write_post(write, assets, data, lang, condition, self_drop):
    """Batch 39: the rocket pad (block, model, loot, recipe) and its screen's text, and the flight plan."""
    for block, name in BLOCKS.items():
        lang[f"block.{MOD}.{block}"] = name
        write(data / "loot_table" / "blocks" / f"{block}.json", self_drop(block))
    lang[f"container.{MOD}.rocket_pad"] = "Rocket Pad"
    lang[f"screen.{MOD}.rocket_pad.launch"] = "Launch"
    for key, text in PAD_RESULTS.items():
        lang[f"screen.{MOD}.rocket_pad.{key}"] = text
    lang[f"message.{MOD}.flight_plan.set"] = "Flight plan: deliver to the pad at %s, %s, %s"
    lang[f"tooltip.{MOD}.flight_plan.blank"] = "Blank: sneak and use it on the rocket pad to deliver to"
    lang[f"tooltip.{MOD}.flight_plan.target"] = "Deliver to the pad at %s, %s, %s (%s)"
    textures = {"deck": f"{MOD}:block/dp_gunmetal", "edge": f"{MOD}:block/dp_hazard", "grille": f"{MOD}:block/dp_grille",
                "lamp": f"{MOD}:block/dp_lamp", "particle": f"{MOD}:block/dp_gunmetal"}
    write(assets / "models" / "block" / "rocket_pad.json", {"parent": "minecraft:block/block", "textures": textures,
                                                           "elements": pad_model()})
    write(assets / "blockstates" / "rocket_pad.json", {"variants": {
        "powered=false": {"model": f"{MOD}:block/rocket_pad"}, "powered=true": {"model": f"{MOD}:block/rocket_pad"}}})
    write(assets / "items" / "rocket_pad.json", {"model": {"type": "minecraft:model", "model": f"{MOD}:block/rocket_pad"}})
    write(data / "recipe" / "rocket_pad.json", {
        "fabric:load_conditions": condition("machines"), "type": "minecraft:crafting_shaped", "category": "redstone",
        "pattern": [" X ", "PPP", "BBB"],
        "key": {"X": f"{MOD}:microchip", "P": "#c:plates/steel", "B": "minecraft:smooth_stone"},
        "result": {"id": f"{MOD}:rocket_pad", "count": 1}})
    write(data / "recipe" / "flight_plan.json", {
        "fabric:load_conditions": condition("machines"), "type": "minecraft:crafting_shapeless", "category": "misc",
        "ingredients": ["minecraft:paper", "minecraft:compass", f"{MOD}:microchip"],
        "result": {"id": f"{MOD}:flight_plan", "count": 1}})


def _faces(texture, **overrides):
    return {side: {"texture": overrides.get(side, texture)} for side in ("north", "east", "south", "west", "up", "down")}


def pad_model():
    """A low gunmetal deck with hazard-striped edges, a grille in the middle where the rocket stands, a launch rail
    at each corner and an amber lamp on the front."""
    elements = [{"from": [0, 0, 0], "to": [16, 4, 16], "faces": _faces("#edge", up="#deck", down="#deck")},
                {"from": [4, 4, 4], "to": [12, 4.5, 12], "faces": _faces("#deck", up="#grille")}]
    for x, z in ((1, 1), (13, 1), (1, 13), (13, 13)):
        elements.append({"from": [x, 4, z], "to": [x + 2, 6, z + 2], "faces": _faces("#deck")})
    elements.append({"from": [7, 1, -0.5], "to": [9, 3, 0], "faces": _faces("#deck", north="#lamp")})
    return elements


# ------------------------------------------------------------------ art (64x64, tools/hd_art.py)

def _rocket(c, hd, body, band, nose, fins=True, tip=None):
    """A rocket standing at an angle: a body tube with a coloured band, a nose cone, fins and a nozzle."""
    import hd_art
    a, b = (16, 52), (44, 18)
    c.capsule(a, b, 7.5, body, flat_ends=True, bands=[(0.5, 0.62, band)])
    dx, dy = b[0] - a[0], b[1] - a[1]
    length = math.hypot(dx, dy)
    ux, uy = dx / length, dy / length
    nx, ny = -uy, ux
    tip_point = (b[0] + ux * 13, b[1] + uy * 13)
    c.polygon([(b[0] + nx * 7.5, b[1] + ny * 7.5), tip_point, (b[0] - nx * 7.5, b[1] - ny * 7.5)], nose)
    if tip:
        c.disc(tip_point, 1.6, tip, 0.8)
    if fins:
        for side in (1, -1):
            base = (a[0] + ux * 8 + nx * 7.5 * side, a[1] + uy * 8 + ny * 7.5 * side)
            c.polygon([base, (a[0] + nx * 15 * side - ux * 2, a[1] + ny * 15 * side - uy * 2),
                       (a[0] + nx * 7.5 * side - ux * 3, a[1] + ny * 7.5 * side - uy * 3)], hd_art.GUNMETAL)
    c.capsule((a[0] - ux * 4, a[1] - uy * 4), a, 5.0, hd_art.GUNMETAL, flat_ends=True)


def _flame(c, hd, a=(10, 58)):
    c.blob(a, 4.0, hd.SAFETY_YELLOW, seed=3801, lumps=5)
    c.blob((a[0] - 3, a[1] + 3), 2.6, hd.RED, seed=3802, lumps=4)


def survey_rocket():
    import hd_art as hd
    c = hd.Canvas()
    _rocket(c, hd, hd.WHITE_PAINT, hd.SAFETY_YELLOW, hd.GUNMETAL, tip=hd.GLASS)
    _flame(c, hd)
    return c.finish()


def cloud_seeding_rocket():
    import hd_art as hd
    c = hd.Canvas()
    _rocket(c, hd, hd.STEEL, hd.GLASS, hd.WHITE_PAINT)
    for x, y in ((48, 40), (54, 44), (50, 48)):
        c.disc((x, y), 2.0, hd.GLASS, 0.6)
    return c.finish()


def clear_sky_rocket():
    import hd_art as hd
    c = hd.Canvas()
    _rocket(c, hd, hd.STEEL, hd.SAFETY_YELLOW, hd.SAFETY_YELLOW)
    c.disc((52, 44), 5.0, hd.SAFETY_YELLOW, 0.4)
    return c.finish()


def _flare(colour_mat, cap):
    import hd_art as hd
    c = hd.Canvas()
    c.capsule((20, 56), (42, 14), 8.0, hd.PAPER, flat_ends=True, bands=[(0.15, 0.3, colour_mat), (0.7, 0.85, colour_mat)])
    c.capsule((42, 14), (45, 8), 8.0, cap, flat_ends=True)
    c.capsule((45, 8), (47, 4), 2.4, hd.HAZARD_BLACK)
    return c.finish()


def signal_flare():
    import hd_art as hd
    return _flare(hd.RED, hd.RED)


def illumination_flare():
    import hd_art as hd
    return _flare(hd.SAFETY_YELLOW, hd.WHITE_PAINT)


def rocket_motor():
    import hd_art as hd
    c = hd.Canvas()
    c.capsule((32, 8), (32, 46), 14, hd.STEEL, flat_ends=True, bands=[(0.1, 0.18, hd.GUNMETAL), (0.82, 0.9, hd.GUNMETAL)])
    c.polygon([(22, 46), (42, 46), (48, 60), (16, 60)], hd.GUNMETAL)
    c.box((32, 26), 6, 10, 0, hd.SAFETY_YELLOW, bevel=1.0, paint=hd.hazard(3))
    return c.finish()


def rocket_casing():
    import hd_art as hd
    c = hd.Canvas()
    c.capsule((32, 6), (32, 58), 14, hd.STEEL, flat_ends=True, bands=[(0.0, 0.06, hd.GUNMETAL), (0.94, 1.0, hd.GUNMETAL)])
    for y in (20, 32, 44):
        c.line((19, y), (45, y), (90, 96, 106))
    return c.finish()


def rocket_nozzle():
    import hd_art as hd
    c = hd.Canvas()
    c.capsule((32, 12), (32, 22), 6, hd.GUNMETAL, flat_ends=True)
    c.polygon([(26, 22), (38, 22), (48, 54), (16, 54)], hd.STEEL)
    c.polygon([(28, 26), (36, 26), (42, 50), (22, 50)], hd.GUNMETAL)
    return c.finish()


def guidance_unit():
    import hd_art as hd
    c = hd.Canvas()
    c.box((32, 34), 18, 16, 0, hd.GUNMETAL, bevel=3.0)
    c.box((32, 30), 12, 8, 0, hd.GLASS, bevel=1.5)
    c.ring((32, 30), 6, 4.5, hd.SAFETY_YELLOW)
    c.line((26, 30), (38, 30), (120, 240, 250))
    c.line((32, 24), (32, 36), (120, 240, 250))
    for x in (20, 26, 38, 44):
        c.capsule((x, 50), (x, 56), 1.0, hd.BRASS)
    return c.finish()


def _powder(colours, seed):
    import random
    import hd_art as hd
    c = hd.Canvas()
    c.capsule((14, 50), (50, 50), 7, hd.PAPER, flat_ends=True)  # a paper tray
    for i, (x, y, r) in enumerate(((32, 40, 12), (22, 44, 8), (42, 44, 8))):
        c.blob((x, y), r, colours[i % len(colours)], seed=seed + i, lumps=5)
    return c.finish()


def iodine():
    import hd_art as hd
    return _powder([hd.Material([(30, 20, 40), (50, 34, 64), (78, 52, 96), (110, 80, 130), (150, 120, 170)], 0.6, 20)], 3810)


def ammonium_perchlorate():
    import hd_art as hd
    return _powder([hd.WHITE_PAINT], 3811)


def silver_iodide():
    import hd_art as hd
    return _powder([hd.Material([(110, 100, 60), (150, 140, 90), (190, 180, 120), (220, 212, 160), (240, 236, 200)], 0.4, 14)], 3812)


def solid_propellant():
    import hd_art as hd
    c = hd.Canvas()
    for i, x in enumerate((22, 32, 42)):
        c.capsule((x, 14), (x, 52), 5.5, hd.Material([(60, 50, 44), (88, 76, 66), (118, 104, 92), (150, 136, 122),
                                                       (186, 172, 158)], 0.15, 6), flat_ends=True)
        c.disc((x, 14), 2.0, hd.HAZARD_BLACK, 0.2)
    return c.finish()


HD_ITEMS = {"survey_rocket": survey_rocket, "cloud_seeding_rocket": cloud_seeding_rocket,
            "clear_sky_rocket": clear_sky_rocket, "signal_flare": signal_flare, "illumination_flare": illumination_flare,
            "rocket_motor": rocket_motor, "rocket_casing": rocket_casing, "rocket_nozzle": rocket_nozzle,
            "guidance_unit": guidance_unit, "iodine": iodine, "ammonium_perchlorate": ammonium_perchlorate,
            "silver_iodide": silver_iodide, "solid_propellant": solid_propellant,
            "delivery_rocket": lambda: delivery_rocket(), "flight_plan": lambda: flight_plan()}


def delivery_rocket():
    """A stubby olive cargo rocket with a hazard band, a wide grey nose for the cargo bay and a yellow tip."""
    import hd_art as hd
    c = hd.Canvas()
    _rocket(c, hd, hd.OLIVE, hd.SAFETY_YELLOW, hd.STEEL, tip=hd.SAFETY_YELLOW)
    _flame(c, hd)
    return c.finish()


def flight_plan():
    """A folded sheet with a dashed flight arc from one pad marker to another and a compass rose."""
    import hd_art as hd
    c = hd.Canvas()
    c.box((32, 34), 22, 24, -0.08, hd.PAPER, bevel=1.0)
    c.disc((18, 48), 3.0, hd.RED, 0.5)
    c.disc((46, 22), 3.0, hd.RED, 0.5)
    def arc(t):
        return 18 + (46 - 18) * t, 48 + (22 - 48) * t - 16 * math.sin(math.pi * t)
    for i in range(1, 14, 2):  # a dashed arc between the two pads
        c.line(arc((i - 0.5) / 14), arc((i + 0.5) / 14), (48, 52, 64), width=2.2)
    c.ring((46, 48), 5, 3.8, hd.GUNMETAL)
    c.line((46, 42), (46, 54), (60, 60, 66))
    c.line((40, 48), (52, 48), (60, 60, 66))
    return c.finish()


def draw_all(save):
    for item, draw in HD_ITEMS.items():
        save(draw(), "item", item)
