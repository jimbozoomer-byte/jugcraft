"""Facades and accessible roof gardens of the three western rental houses."""
from town_voxels import S
from western_city import door


def finish_tenement(v, house, storeys):
    height = 5 * storeys
    wall = ('bricks', 'white_terracotta', 'mud_bricks')[house-1]
    trim = ('stone_brick', 'smooth_quartz', 'mud_brick')[house-1]
    deck = ('stone_bricks', 'bricks', 'spruce_planks')[house-1]
    v.box(-1, height, -1, 31, height, 37, S(deck))
    v.walls(0, height+1, 0, 30, height+1, 36, S(wall))
    v.walls(0, height+2, 0, 30, height+2, 36, S(trim+'_slab'))

    # The penthouse straddles the last stair flight. The eastern door faces the
    # roof garden from the broad landing south of the last step.
    v.walls(13, height+1, 26, 19, height+4, 36, S(wall))
    door(v, 19, height+1, 34, 'east')
    v.box(14, height+2, 26, 18, height+3, 26, S('glass'))
    v.box(12, height+5, 25, 20, height+5, 37, S(trim+'_slab'))
    v.set(18, height+4, 34, S('lantern', hanging=True))
    for x in (4, 26):
        for z in (5, 24):
            v.box(x-1,height+1,z,x+1,height+1,z+2,S('mossy_stone_bricks'))
            v.box(x,height+2,z,x,height+2,z+2,S('azalea_leaves',persistent=True))
            v.set(x,height+2,z+1,S('flowering_azalea_leaves',persistent=True))
        v.box(x-1,height+1,14,x+1,height+1,14,S('spruce_stairs',facing='south'))
        v.set(x,height+1,3,S('lantern'))

    # Break up the tall facades with masonry cornices, shuttered arches or
    # projecting timber ends. Balcony doors have a full clear opening.
    for floor in range(storeys):
        top = floor * 5 + 4
        for centre in (5, 15, 25):
            if house == 1:
                v.box(centre-2,top,-1,centre+2,top,-1,S('brick_stairs',half='top',facing='south'))
                v.set(centre,top,-1,S('chiseled_stone_bricks'))
            elif house == 2:
                v.set(centre-1,top,-1,S('smooth_quartz_stairs',half='top',facing='west'))
                v.set(centre,top,-1,S('smooth_quartz_slab',type='top'))
                v.set(centre+1,top,-1,S('smooth_quartz_stairs',half='top',facing='east'))
                for x in (centre-2, centre+2):
                    v.box(x,top-2,-1,x,top-1,-1,S('dark_oak_trapdoor',open=True,facing='north'))
            else:
                v.box(centre-2,top,0,centre+2,top,0,S('stripped_spruce_log',axis='x'))
                for x in (centre-2,centre+2):
                    v.set(x,top,-1,S('stripped_spruce_log',axis='z'))
    if house == 1:
        v.box(-1,height,-2,31,height,-2,S('stone_brick_stairs',half='top',facing='south'))
        for x in (3, 26):
            v.box(x,height+1,28,x+1,height+7,29,S('bricks'))
            v.box(x-1,height+8,27,x+2,height+8,30,S('stone_brick_slab'))
    elif house == 2:
        for dy, half_width in ((2,5),(3,4),(4,2),(5,1)):
            v.box(15-half_width,height+dy,0,15+half_width,height+dy,0,S(wall))
            for x in (15-half_width,15+half_width):
                v.set(x,height+dy+1,0,S('smooth_quartz_slab'))
        v.set(15,height+4,0,S('bell',attachment='ceiling',facing='north'))
    else:
        for x in (2,8,23,29):
            for z in (6,21):
                v.box(x,height+1,z,x,height+3,z,S('spruce_fence'))
            v.box(x,height+4,5,x,height+4,22,S('stripped_spruce_log',axis='z'))
        for z in range(6,22,3):
            for x in (2,23):
                v.box(x,height+5,z,x+6,height+5,z,S('spruce_slab'))
