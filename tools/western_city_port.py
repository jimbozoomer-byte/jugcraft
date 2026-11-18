"""West Gate Harbour, authored against the November 16 terrain copy.

Coordinates are relative to the saved town origin. The avenue ends in a quay
on piles with an open route to the sea between the two outer finger piers.
Building this blueprint never opens a save, generates water or dredges terrain.
"""
import random

from town_buildings import house, stall, style
from town_voxels import S, Voxels


DECK = -50
WATERLINE = -52
PIER_CENTRES = (76, 119, 147)

# First solid bed in each individual piling column. Published as elevations
# rather than reading a personal save when regenerating a distribution asset.
QUAY_BEDS = {
    50: (-65, -66, -67), 59: (-73, -67, -67), 68: (-68, -67, -68),
    77: (-68, -68, -68), 86: (-68, -68, -68), 95: (-68, -68, -69),
    104: (-64, -69, -68), 113: (-61, -68, -68), 122: (-66, -68, -67),
    131: (-68, -68, -67), 140: (-72, -67, -82), 149: (-67, -67, -67),
}
FINGER_BEDS = {
    75: (-64, -68, -68), 77: (-63, -68, -68), 118: (-60, -59, -60),
    120: (-61, -59, -60), 146: (-92, -72, -68), 148: (-81, -70, -68),
}
MOLE_BEDS = {49: (-66, -64, -65), 52: (-66, -67, -67)}


def pile(v, x, z, bed):
    # Rest on the sampled bed. No punching through it into caves or ruins.
    v.box(x, bed + 1, z, x, DECK - 1, z, S('stripped_dark_oak_log'))


def pontoons(v):
    # Timber sleepers under the quay carry the stone-and-timber promenade.
    for z, beds in QUAY_BEDS.items():
        for x, bed in zip((-241, -231, -221), beds):
            pile(v, x, z, bed)
        v.box(-243, DECK - 1, z, -219, DECK - 1, z, S('dark_oak_log', axis='x'))
    v.box(-243, DECK, 48, -219, DECK, 151, S('stone_bricks'))
    v.box(-240, DECK, 48, -238, DECK, 151, S('polished_andesite'))
    for x in (-243, -219):
        v.box(x, DECK, 48, x, DECK, 151, S('polished_tuff'))

    # Short staircase across the last strip of water to the gatehouse throat.
    for x, y in ((-216, -48), (-217, -48), (-218, -49), (-219, -50)):
        v.box(x, y - 1, 94, x, y - 1, 98, S('tuff_bricks'))
        state = S('stone_brick_stairs', facing='east') if x in (-218, -217) else S('stone_bricks')
        v.box(x, y, 94, x, y, 98, state)
    for z in (94, 98):
        pile(v, -216, z, -69)
        v.box(-216, DECK, z, -216, -49, z, S('tuff_bricks'))

    for z, beds in FINGER_BEDS.items():
        for x, bed in zip((-261, -253, -246), beds):
            pile(v, x, z, bed)
        v.box(-262, DECK - 1, z, -244, DECK - 1, z, S('dark_oak_log', axis='x'))
    for z in PIER_CENTRES:
        v.box(-262, DECK, z-1, -244, DECK, z+1, S('spruce_planks'))
        for x in (-261, -251):
            v.set(x, DECK+1, z+1, S('dark_oak_fence'))
        v.set(-261, DECK+2, z+1, S('lantern'))
        # Dock ladder is backed down to the water, so players can disembark
        # even when the nose of their boat is pointing away from the quay.
        for y in range(WATERLINE-1, DECK+1):
            v.set(-261, y, z-1, S('stripped_dark_oak_log'))
            v.set(-261, y, z-2, S('ladder', facing='north', waterlogged=y <= WATERLINE))

    # North mole turns the corner around the light, keeping a boat entrance
    # to its south open at sea level and at mast height.
    v.box(-266, DECK, 46, -258, DECK, 55, S('tuff_bricks'))
    v.box(-258, DECK, 49, -244, DECK, 52, S('stone_bricks'))
    for z, beds in MOLE_BEDS.items():
        for x, bed in zip((-263, -256, -249), beds):
            pile(v, x, z, bed)
        v.box(-265, DECK-1, z, -243, DECK-1, z, S('polished_tuff'))
    for z, bed in ((47,-65),(54,-67)):
        pile(v,-262,z,bed)
    v.box(-257,DECK+1,49,-244,DECK+1,49,S('stone_brick_wall'))

    # A wider, planked apron at the foot of the city steps serves the market.
    v.box(-249,DECK,90,-244,DECK,107,S('spruce_planks'))
    for z, bed in ((92,-67),(100,-65),(106,-62)):
        pile(v,-248,z,bed)
        v.box(-249,DECK-1,z,-242,DECK-1,z,S('dark_oak_log',axis='x'))


def harbour_light(v):
    x, z = -262, 50
    for h in range(1, 20):
        radius = 3 if h <= 8 else 2
        for dx in range(-radius, radius+1):
            for dz in range(-radius, radius+1):
                edge = max(abs(dx), abs(dz)) == radius and abs(dx) + abs(dz) < radius*2
                bevel = abs(dx) == radius-1 and abs(dz) == radius-1
                if edge or bevel:
                    material = 'stone_bricks' if h <= 3 else 'bricks' if h in (*range(7,10),*range(14,16)) else 'calcite'
                    if dz == 0 and dx == radius and h in (5,6,12,13):
                        material='glass_pane'
                    v.set(x+dx,DECK+h,z+dz,S(material))
        # Align the ladder with a continuous inner core all the way to the lamp room.
        v.set(x,DECK+h,z+1,S('stone_bricks'))
        v.set(x,DECK+h,z,S('ladder',facing='north'))
    for h, half in ((1,'lower'),(2,'upper')):
        v.set(x+3,DECK+h,z,S('spruce_door',facing='east',half=half,hinge='left'))
    # An iron-railed maintenance gallery with a walk-out from the lantern room.
    for dx in range(-3,4):
        for dz in range(-3,4):
            if abs(dx) + abs(dz) > 5: continue
            if (dx,dz) != (0,0):
                v.set(x+dx,DECK+19,z+dz,S('polished_tuff'))
            if max(abs(dx),abs(dz))==3 or abs(dx)+abs(dz)==5:
                v.set(x+dx,DECK+20,z+dz,S('iron_bars'))
    for dx in (-2,2):
        for dz in (-2,2):
            v.box(x+dx,DECK+20,z+dz,x+dx,DECK+23,z+dz,S('waxed_cut_copper'))
    v.set(x,DECK+20,z+1,S('polished_andesite'))
    v.set(x,DECK+21,z+1,S('ochre_froglight'))
    v.set(x,DECK+22,z+1,S('ochre_froglight'))
    # Broad drip edge, stepped verdigris cone, copper finial.
    for h, r in ((24,3),(25,2),(26,1)):
        for dx in range(-r,r+1):
            for dz in range(-r,r+1):
                if r == 1 or abs(dx)+abs(dz)<2*r:
                    v.set(x+dx,DECK+h,z+dz,S('waxed_oxidized_cut_copper'))
    v.set(x,DECK+27,z,S('lightning_rod',facing='up'))


def moored_cutter(v, cx, cz, sail, furled=False):
    """A pointed clinker hull, raised transom, tiller and a billowing lug sail."""
    for dx in range(-8,9):
        halfwidth = min(2, 8-abs(dx))
        v.box(cx+dx, WATERLINE-1, cz, cx+dx, WATERLINE-1, cz, S('dark_oak_log',axis='x'))
        v.box(cx+dx,WATERLINE,cz-halfwidth,cx+dx,WATERLINE,cz+halfwidth,S('dark_oak_planks'))
        for sign in (-1,1):
            v.set(cx+dx,WATERLINE+1,cz+sign*halfwidth,S('spruce_planks'))
        if halfwidth > 0:
            v.box(cx+dx,WATERLINE+1,cz-halfwidth+1,cx+dx,WATERLINE+1,cz+halfwidth-1,S('oak_planks'))
        if abs(dx)>=6:
            v.box(cx+dx,WATERLINE+2,cz-halfwidth,cx+dx,WATERLINE+2,cz+halfwidth,S('spruce_slab',type='top'))
    # Mizzen rail, rudder, bowsprit, and a separate sail handling winch.
    v.set(cx+8,WATERLINE,cz,S('dark_oak_stairs',facing='west'))
    v.set(cx+6,WATERLINE+3,cz,S('lever',face='floor',facing='west',powered=False))
    v.box(cx-10,WATERLINE+3,cz,cx-7,WATERLINE+3,cz,S('dark_oak_fence'))
    v.set(cx+4,WATERLINE+2,cz+1,S('barrel',facing='west'))
    v.set(cx+4,WATERLINE+3,cz+1,S('lantern'))
    v.box(cx,WATERLINE+2,cz,cx,WATERLINE+12,cz,S('stripped_spruce_log'))
    v.box(cx-4,WATERLINE+10,cz+1,cx+3,WATERLINE+10,cz+1,S('spruce_log',axis='x'))
    v.set(cx,WATERLINE+10,cz-1,S('spruce_fence'))
    for dx in range(-4,4):
        drop = 1 if furled else min(6,dx+5,10-dx)
        for h in range(drop):
            belly = 2 if 2 <= h <= 4 and -3 <= dx <= 2 else 1
            v.set(cx+dx,WATERLINE+9-h,cz+belly,S(sail if dx != -3 else 'white_wool'))
    v.box(cx,WATERLINE+13,cz,cx+2,WATERLINE+13,cz,S('red_wool' if not furled else 'blue_wool'))


def cargo_derrick(v, x, z):
    for dx in (-1,1):
        v.box(x+dx,DECK+1,z-1,x+dx,DECK+5,z-1,S('stripped_spruce_log'))
        v.box(x+dx,DECK+1,z+1,x+dx,DECK+5,z+1,S('stripped_spruce_log'))
        v.box(x+dx,DECK+6,z-1,x+dx,DECK+6,z+1,S('dark_oak_log',axis='z'))
    v.box(x-1,DECK+6,z,x+1,DECK+6,z,S('dark_oak_log',axis='x'))
    for i in range(4):
        v.set(x-i,DECK+7+i,z,S('stripped_oak_log',axis='x'))
        v.set(x-i-1,DECK+7+i,z,S('spruce_stairs',facing='east',half='top'))
    v.box(x-6,DECK+10,z,x-3,DECK+10,z,S('stripped_oak_log',axis='x'))
    v.box(x-6,DECK+6,z,x-6,DECK+9,z,S('iron_chain',axis='y'))
    v.set(x-6,DECK+5,z,S('iron_bars'))
    v.set(x,DECK+1,z,S('grindstone',face='floor',facing='south'))


def customs_clock(v):
    """An arcaded passage under the tide-clock, with the dial facing the sea."""
    x, z = -226, 96
    for dx in (-3,3):
        for dz in (-4,4):
            v.box(x+dx,DECK+1,z+dz,x+dx,DECK+6,z+dz,S('stone_bricks'))
            v.set(x+dx,DECK+1,z+dz,S('chiseled_tuff_bricks'))
        for dz, facing in ((-3,'north'),(3,'south')):
            v.set(x+dx,DECK+6,z+dz,S('stone_brick_stairs',facing=facing,half='top'))
        v.box(x+dx,DECK+7,z-4,x+dx,DECK+7,z+4,S('tuff_bricks'))
    for y in (8,15):
        v.box(x-4,DECK+y,z-5,x+4,DECK+y,z+5,S('polished_tuff'))
    v.walls(x-3,DECK+9,z-4,x+3,DECK+14,z+4,S('white_terracotta'))
    for dx in (-3,3):
        for dz in (-4,4):
            v.box(x+dx,DECK+9,z+dz,x+dx,DECK+14,z+dz,S('dark_oak_log'))
        v.box(x+dx,DECK+10,z-1,x+dx,DECK+13,z+1,S('smooth_sandstone'))
        for y, dz in ((11,0),(12,0),(12,1)):
            v.set(x+dx,DECK+y,z+dz,S('polished_blackstone'))
    for dz in (-4,4):
        v.box(x-1,DECK+10,z+dz,x+1,DECK+13,z+dz,S('glass_pane'))
    # Open belfry windows with arched lintels, an accessible maintenance hatch
    # and a real bell mounted to its crosspiece.
    for dx in (-2,2):
        for dz in (-2,2):
            v.box(x+dx,DECK+16,z+dz,x+dx,DECK+20,z+dz,S('polished_tuff'))
        v.box(x+dx,DECK+20,z-2,x+dx,DECK+20,z+2,S('dark_oak_log',axis='z'))
    v.box(x-2,DECK+20,z,x+2,DECK+20,z,S('dark_oak_log',axis='x'))
    v.set(x,DECK+19,z,S('bell',attachment='ceiling',facing='west'))
    for h,r in ((21,3),(22,2),(23,2),(24,1),(25,1),(26,0)):
        for dx in range(-r,r+1):
            for dz in range(-r,r+1):
                if r<=1 or abs(dx)+abs(dz)<2*r:
                    v.set(x+dx,DECK+h,z+dz,S('waxed_weathered_cut_copper'))
    v.set(x,DECK+27,z,S('lightning_rod',facing='up'))
    for h in range(1,16):
        v.set(x+3,DECK+h,z+4,S('stripped_dark_oak_log'))
        v.set(x+3,DECK+h,z+3,S('ladder',facing='north'))
    v.set(x,DECK+7,z,S('lantern',hanging=True))


def outfitting(v):
    # A herring rack and sailmaker's bench occupy the gaps between frontages;
    # the five-wide lane between them and the water stays open for carts.
    for z in (66,127):
        for x in (-231,-225):
            v.box(x,DECK+1,z,x,DECK+3,z,S('spruce_fence'))
        v.box(-231,DECK+4,z,-225,DECK+4,z,S('stripped_spruce_log',axis='x'))
    for x in (-230,-228,-226):
        v.set(x,DECK+3,66,S('iron_bars'))
        v.set(x,DECK+2,66,S('white_stained_glass_pane'))
    v.box(-230,DECK+2,127,-226,DECK+3,127,S('white_wool'))
    v.set(-229,DECK+1,126,S('loom',facing='west'))
    for z in (66,127):
        v.set(-231,DECK+1,z+1,S('barrel',facing='up'))
    for z in (54,68,86,109,127,145):
        # Chiselled paving medallions and cleated bollards along the quay edge.
        v.set(-239,DECK,z,S('chiseled_stone_bricks'))
        v.set(-243,DECK+1,z,S('polished_blackstone_wall'))
    for z in (63,82,111,134):
        v.box(-220,DECK+1,z,-220,DECK+1,z+1,S('barrel',facing='east'))
        v.set(-220,DECK+2,z,S('spruce_trapdoor',half='bottom',facing='east'))


def build_port(v):
    """Add the whole waterfront to a shared town-local voxel map and return plots."""
    rng = random.Random(1647)
    pontoons(v)
    harbour_light(v)
    plots = []
    # Four varied gabled frontages face the promenade. The two tallest sit at
    # opposite ends, framing the view through the gate to the water.
    for name, z, width, depth, levels, purpose, roof, infill in (
        ('harbour_chart_house',52,11,12,3,'shop','deepslate_tile','calcite'),
        ('harbour_chandlery',72,13,12,2,'shop','brick','white_terracotta'),
        ('harbour_smokehouse',109,11,11,2,'bakery','spruce','mud_bricks'),
        ('harbour_inn',131,13,12,3,'tavern','waxed_weathered_cut_copper','smooth_sandstone'),
    ):
        st = style(rng,roof=roof,plaster=infill,stone_ground=False,timber='stripped_dark_oak_log',wood='spruce')
        p = house(rng,width,depth,levels,purpose,gable_front=True,st=st)
        v.paste(p.v,-233,DECK,z,turns=3,origin_size=(width,depth))
        plots.append({'id':name,'bounds':[-233,DECK+1,z,-234+depth,DECK+levels*5+7,z+width-1]})
    v.set(-228,DECK+1,57,S('cartography_table'))
    v.set(-226,DECK+1,78,S('loom',facing='west'))
    v.set(-226,DECK+1,115,S('smoker',facing='west'))
    customs_clock(v)
    outfitting(v)
    for z, color in ((90,'yellow_wool'),(102,'blue_wool')):
        booth=stall(rng,5,4)
        for bx,by,bz,stripe in booth.sites[0]['blocks']:
            booth.v.set(bx,by,bz,S(color if stripe else 'white_wool'))
        v.paste(booth.v,-248,DECK,z,turns=1,origin_size=(5,4))
    cargo_derrick(v,-242,142)
    for z in (61,84,104,132):
        v.box(-242,DECK+1,z,-242,DECK+3,z,S('dark_oak_fence'))
        v.set(-242,DECK+4,z,S('lantern'))
    moored_cutter(v,-252,71,'white_wool')
    moored_cutter(v,-253,124,'light_gray_wool',furled=True)
    # Downward gangplank to each vessel's unobstructed main deck.
    for x,z,facing in ((-254,74,'south'),(-255,121,'north')):
        v.set(x,DECK,z,S('spruce_stairs',facing=facing))
    plots.extend([
        {'id':'harbour_customs_clock','bounds':[-230,DECK+1,91,-222,DECK+27,101]},
        {'id':'harbour_light','bounds':[-265,DECK+1,47,-259,DECK+27,53]},
        {'id':'harbour_fish_market','bounds':[-249,DECK+1,90,-244,DECK+4,106]},
    ])
    return plots
