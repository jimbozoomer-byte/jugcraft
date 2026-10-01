"""Models for the electronics tier in the cyan look (see docs/ART_DIRECTION.md, "Electric"): near-black casings with
lit cyan seams, cyan glass, cyan screens and violet conduits, following the owner's high-tech references. Built
with the steampunk helpers; steampunk_models.MODELS takes these after the electric ones. The classic style pack
keeps a plain look.
"""
from steampunk_models import box, cyl, dial

DARK, FRAME, GLASS, CONDUIT = "el_dark", "el_frame", "el_glass", "el_conduit"
CYAN, VIOLET, SCREEN, LAMP, VENT, PORT = "el_glow_cyan", "el_glow_violet", "el_screen_cyan", "el_lamp_on", "el_vent", "el_port"


def crystal_grower():
    """Two blocks tall: a dark control cabinet with a cyan screen, status lamp and vents, under a glass growth chamber
    where a silicon boule hangs from its pull rod over a glowing crucible ring, and a pull head on top. Violet
    conduits run up the back corners from the cabinet to the head; power ports sit on the cabinet's sides."""
    m = [box((0, 0, 0), (16, 1.5, 16), FRAME)]
    # Cabinet.
    m.append(box((1, 1.5, 1), (15, 13, 15), DARK))
    m.append(box((3, 6, 0.5), (13, 11.5, 1), {"*": FRAME, "north": SCREEN + "!"}))
    m.append(dial("north", (4.5, 3.5, 0.5), 1, texture=LAMP, body=FRAME))
    m.append(box((7, 2.5, 0.5), (13, 4.5, 1), {"*": FRAME, "north": VENT + "!"}))
    m.append(box((1.5, 12.5, 0.6), (14.5, 13, 1), CYAN))
    for x0, x1, face in ((0.25, 1, "west"), (15, 15.75, "east")):
        m.append(box((x0, 4.5, 6), (x1, 9.5, 10), {"*": FRAME, face: PORT + "!"}))
    m.append(box((0.5, 13, 0.5), (15.5, 14.5, 15.5), {"*": FRAME, "up": DARK}))
    # Growth chamber: corner posts, glass on the back and sides, a bezel round the open front.
    for x, z in ((1, 1), (13, 1), (1, 13), (13, 13)):
        m.append(box((x, 14.5, z), (x + 2, 27, z + 2), FRAME))
    m.append(box((3, 14.5, 14), (13, 27, 14.5), GLASS))
    m.append(box((1.5, 14.5, 3), (2, 27, 13), GLASS))
    m.append(box((14, 14.5, 3), (14.5, 27, 13), GLASS))
    for y0 in (14.5, 26):
        m.append(box((3, y0, 0.75), (13, y0 + 1, 1.5), FRAME))
    # Crucible with its heater ring, the boule and the pull rod.
    m += cyl("y", 8, 8, 3.5, 14.5, 16.5, FRAME)
    m += cyl("y", 8, 8, 3.75, 15.25, 15.75, CYAN)
    m += cyl("y", 8, 8, 2.75, 18, 23.5, "el_boule")
    m += cyl("y", 8, 8, 1.25, 17, 18, "el_boule")
    m += cyl("y", 8, 8, 0.5, 23.5, 27, FRAME)
    # Pull head with a vented top.
    m.append(box((0.5, 27, 0.5), (15.5, 29, 15.5), FRAME))
    m.append(box((2, 29, 2), (14, 31.5, 14), DARK))
    m.append(box((4.5, 31.5, 4.5), (11.5, 32, 11.5), {"*": FRAME, "up": VENT + "!"}))
    m.append(box((2, 28, 0.25), (14, 28.5, 0.5), CYAN))
    # Violet conduits up the back corners.
    for x in (3.5, 11.5):
        m.append(box((x, 13, 15), (x + 1, 29, 16), CONDUIT))
    return m


MODELS = {"crystal_grower": crystal_grower()}
