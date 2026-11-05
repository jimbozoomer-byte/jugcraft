"""Closed, supported release drum on the existing late-industrial material and geometry kit."""
from starter_chemistry_models import skid, cabinet, drawer, STEEL, OLIVE, CHROME, SILVER, RUBBER, GAUGE, LIGHT, RIBS
from steampunk_models import box, cyl

def coater():
    # A three-column form runs from x=-32 to x=16; its controller/first port is at x=8.
    m = skid(3, 3) + drawer(-30, -16, 3, 16, 3, 16) + drawer(2, 14, 3, 14, 3, 16)
    m += [box((-30, 8, 7), (14, 14, 44), STEEL), box((-30, 14, 5), (-26, 46, 10), OLIVE),
          box((10, 14, 5), (14, 46, 10), OLIVE), box((-30, 38, 3), (14, 46, 8), SILVER),
          box((-28, 13, 13), (-22, 29, 37), STEEL), box((0, 13, 13), (6, 29, 37), STEEL)]
    m += cyl('z', -12, 29, 14, 10, 39, SILVER)
    for z in (10, 24, 37): m += cyl('z', -12, 29, 14.5, z, z + 2, CHROME)
    m += cyl('z', -12, 29, 12, 8, 10, STEEL) + cyl('z', -12, 29, 10, 7, 8, RUBBER)
    m += cyl('z', -12, 29, 8.5, 6.5, 7, SILVER)
    m += [box((-19, 28, 5.5), (-5, 30, 6.5), CHROME), box((-13, 22, 5.5), (-11, 36, 6.5), CHROME)]
    m += cabinet(6, 15, 21, 8, 27, 22)
    m += [box((6, 14, 21), (14, 15, 43), STEEL), box((6, 18, 29), (14, 34, 39), RIBS), box((-22, 21, 39), (-4, 37, 46), OLIVE)]
    m += cyl('z', -12, 29, 4, 38, 47, STEEL)
    m += cyl('z', -12, 39, 2, 36, 45, STEEL) + cyl('x', 39, 45, 2, -12, 8, STEEL)
    m += cyl('y', 8, 45, 2, 8, 39, STEEL) + cyl('z', 8, 8, 2, 45, 47.5, STEEL)
    m += cyl('z', 10, 36, 2, 19, 22, CHROME) + cyl('z', 10, 36, 3, 18.5, 19, GAUGE)
    m.append(box((8, 39, 20), (12, 41, 21), LIGHT))
    return m

MODELS = {'release_coater': coater()}
