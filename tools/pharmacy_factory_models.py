"""Pharmacy geometry using the established authored steel/olive machine kit; no new bitmap assets."""
from starter_chemistry_models import skid, cabinet, drawer, STEEL, OLIVE, CHROME, SILVER, RUBBER, GAUGE, LIGHT, RIBS
from steampunk_models import box, cyl


def analyzer():
    model = skid(2, 2)
    model += drawer(-14, 0, 5, 12, 4, 17)
    model += cabinet(3, 5, 4, 10, 21, 11)
    model += [box((-14, 5, 15), (14, 12, 30), STEEL),
              box((-14, 12, 4), (1, 29, 30), SILVER),
              box((-12, 15, 3.5), (-1, 25, 4), STEEL),
              box((-10, 17, 3), (-3, 23, 3.5), GAUGE),
              box((-13, 28, 17), (0, 30, 29), OLIVE)]
    model += cyl('y', -6, 23, 3, 29, 31.5, CHROME)
    model += cyl('x', 20, 25, 2, 1, 5, RUBBER)
    model += cyl('z', 8, 8, 2, 29, 31.5, STEEL)
    model.append(box((10, 26, 7), (12, 28, 9), LIGHT))
    return model


def separator():
    model = skid(2, 2)
    model += drawer(-14, 0, 5, 12, 3, 14)
    model += cabinet(3, 5, 3, 10, 18, 10)
    model += cyl('y', -4, 20, 10, 10, 40, SILVER)
    for y in (10, 24, 39):
        model += cyl('y', -4, 20, 10.5, y, y + 2, CHROME)
    model += cyl('y', -4, 20, 9, 41, 45, OLIVE)
    model += cyl('y', -4, 20, 4, 45, 47.5, STEEL)
    model += [box((5, 35, 4), (14, 46, 14), OLIVE),
              box((6, 44, 5), (13, 47, 13), STEEL),
              box((7, 15, 17), (14, 29, 28), RIBS),
              box((4, 29, 18), (15, 31, 27), CHROME),
              box((-14.5, 16, 18), (-14, 35, 21), LIGHT)]
    model += cyl('z', 9, 42, 2, 13, 20, STEEL)
    model += cyl('x', 42, 20, 2, -4, 9, STEEL)
    model += cyl('z', -4, 20, 4, 8.5, 10, CHROME)
    model += cyl('z', -4, 20, 3, 8, 8.5, GAUGE)
    # Closed lower service manifold joins the rear water fitting to the vessel's foot.
    model += cyl('z', 8, 8, 2, 20, 31.5, STEEL)
    model += cyl('x', 8, 20, 2, -4, 8, STEEL)
    model += cyl('y', -4, 20, 2, 8, 12, STEEL)
    return model


def press():
    model = skid(2, 3)
    model += [box((-14, 5, 4), (14, 9, 44), SILVER),
              box((-14, 9, 16), (-10, 30, 41), STEEL),
              box((10, 9, 16), (14, 30, 41), STEEL),
              box((-14, 27, 16), (14, 31, 41), OLIVE),
              box((-11, 18, 20), (7, 21, 37), CHROME),
              box((-10, 13, 4), (0, 25, 14), OLIVE),
              box((3, 13, 4), (13, 25, 14), OLIVE),
              box((-9, 14, 3.5), (-1, 24, 4), RIBS),
              box((4, 14, 3.5), (12, 24, 4), RIBS),
              box((-11, 10, 42), (11, 24, 46), SILVER)]
    model += drawer(-14, 0, 5, 12, 3, 16)
    model += cabinet(3, 5, 4, 10, 8, 8)
    model += cyl('y', -2, 28, 8, 9, 12, STEEL)
    model += cyl('y', -2, 28, 6, 21, 27, SILVER)
    model += cyl('y', -2, 28, 2, 21, 30, CHROME)
    model += cyl('z', 3, 18, 3, 40, 45, STEEL)
    for x in (-8, 8):
        model += cyl('z', x, 8, 2, 39, 47.5, STEEL)
    model.append(box((10, 28, 19), (12, 30, 22), LIGHT))
    return model


MODELS = {'laboratory_analyzer': analyzer(), 'pharmaceutical_separator': separator(), 'formulation_press': press()}
