"""Two rustic beverage forms on the established native copper/steel/wood geometry kit."""
from starter_chemistry_models import skid, cabinet, drawer, STEEL, OLIVE, CHROME, SILVER, RUBBER, GAUGE, LIGHT
from steampunk_models import box, cyl

def barrel():
    m = skid(2, 2)
    m += [box((-14, 5, 4), (14, 9, 28), STEEL), box((-12, 7, 5), (-8, 13, 27), 'sp_copper'),
          box((8, 7, 5), (12, 13, 27), 'sp_copper')]
    m += cyl('z', 0, 20, 11, 5, 28, 'cider_barrel_staves')
    for z in (5, 15, 26): m += cyl('z', 0, 20, 11.5, z, z + 2, STEEL)
    m += cyl('z', 0, 20, 9, 4, 5, 'cider_barrel_head')
    m += drawer(3, 14, 5, 11, 3, 13)
    m += cyl('z', -8, 12, 2, 1, 6, CHROME)
    m += cyl('y', -8, 2, 2, 9, 12, STEEL)
    return m

def still():
    m = skid(2, 2) + drawer(-14, 0, 5, 13, 3, 18)
    m += cyl('y', -5, 18, 9, 11, 25, 'sp_copper')
    m += cyl('y', -5, 18, 7, 25, 29, 'sp_copper')
    m += cyl('y', -5, 18, 2, 29, 31, CHROME)
    m += cyl('x', 29, 18, 2, -5, 8, 'sp_copper')
    m += cyl('y', 8, 22, 5, 8, 29, SILVER)
    for y in (10, 15, 20, 25): m += cyl('y', 8, 22, 5.5, y, y + 1, 'sp_copper')
    m += cyl('z', 8, 29, 2, 18, 22, 'sp_copper')
    m += drawer(2, 14, 5, 12, 3, 15)
    m += [box((-11, 7, 1), (-3, 11, 3), RUBBER), box((-10, 8, .5), (-4, 10, 1), LIGHT)]
    return m

MODELS = {'mash_conditioning_barrel': barrel(), 'moonshine_still': still()}
