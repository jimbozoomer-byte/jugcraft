"""Closed sterile equipment built from the existing authored machine material kit."""
from starter_chemistry_models import skid, cabinet, drawer, STEEL, OLIVE, CHROME, SILVER, RUBBER, GAUGE, LIGHT, RIBS
from steampunk_models import box, cyl

def autoclave():
    m = skid(2, 3) + drawer(-14, 0, 5, 10, 3, 16) + cabinet(3, 5, 3, 10, 18, 10)
    m += [box((-14, 5, 12), (14, 10, 44), STEEL), box((-11, 9, 40), (11, 26, 46), OLIVE)]
    m += cyl('z', -2, 20, 10, 7, 41, SILVER)
    for z in (8, 23, 39): m += cyl('z', -2, 20, 10.5, z, z + 2, CHROME)
    m += cyl('z', -2, 20, 8, 6, 8, STEEL) + cyl('z', -2, 20, 6.5, 5.5, 6, RUBBER)
    m += [box((-7, 19, 4.5), (3, 21, 5.5), CHROME), box((-3, 15, 4.5), (-1, 25, 5.5), CHROME)]
    m += cyl('y', 9, 35, 2, 9, 29, STEEL) + cyl('z', 9, 8, 2, 35, 47.5, STEEL)
    m += cyl('z', 9, 27, 2, 30, 35, STEEL) + cyl('z', 9, 27, 3, 29.5, 30, GAUGE)
    m.append(box((11, 26, 7), (13, 28, 9), LIGHT))
    return m

def filler():
    m = skid(2, 2) + drawer(-14, 0, 5, 12, 3, 16) + cabinet(3, 5, 3, 10, 12, 10)
    m += [box((-14, 5, 5), (14, 10, 29), STEEL), box((-14, 10, 17), (-11, 46, 29), OLIVE),
          box((11, 10, 17), (14, 46, 29), OLIVE), box((-14, 41, 5), (14, 46, 29), SILVER),
          box((-10, 21, 5), (9, 24, 27), CHROME), box((-10, 24, 25), (9, 40, 29), STEEL)]
    for x in (-6, 5):
        m += cyl('y', x, 17, 4, 34, 41, SILVER) + cyl('y', x, 17, 1, 26, 34, CHROME)
        m += cyl('y', x, 17, 2, 24, 26, RUBBER)
    m += cyl('z', 8, 8, 2, 25, 31.5, STEEL) + cyl('y', 8, 25, 2, 8, 40, STEEL)
    m.append(box((10, 43, 6), (12, 45, 9), LIGHT))
    return m

MODELS = {'autoclave': autoclave(), 'sterile_filler': filler()}
