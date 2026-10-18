"""Non-timber rainforest stems; shared trees_data owns the saplings and fronds."""
from decor_data import rid, self_drop


def assets(root, write, lang):
    pillar(root, write, 'rubber_log')
    pillar(root, write, 'tree_fern_trunk')
    lang['block.jugcraft.tree_fern_trunk'] = 'Tree Fern Trunk'
    lang['block.jugcraft.rubber_log'] = 'Rubber Log'
    lang['block.jugcraft.tree_tap'] = 'Tree Tap'
    lang['item.jugcraft.latex_bottle'] = 'Latex Bottle'
    for level in range(5):
        def cube(lo, hi, texture):
            return {'from': lo, 'to': hi, 'faces': {f: {'texture': texture} for f in ('up', 'down', 'north', 'south', 'east', 'west')}}
        elements = [cube([5, 1, 8], [11, 3, 15], '#wood'), cube([4, 1, 8], [5, 7, 15], '#wood'),
                    cube([11, 1, 8], [12, 7, 15], '#wood'), cube([5, 1, 8], [11, 7, 9], '#wood'),
                    cube([5, 1, 14], [11, 7, 15], '#wood'), cube([7, 6, 14], [9, 11, 16], '#metal'),
                    cube([7, 10, 11], [9, 12, 16], '#metal')]
        if level: elements.append(cube([5, 3, 9], [11, 3+level*.75, 14], '#latex'))
        write(root / 'models' / 'block' / f'tree_tap_{level}.json', {'parent': 'minecraft:block/block',
            'textures': {'particle': rid('block/tree_tap'), 'wood': rid('block/tree_tap'), 'metal': 'minecraft:block/iron_block', 'latex': rid('block/latex')}, 'elements': elements})
    write(root / 'blockstates' / 'tree_tap.json', {'variants': {
        f'facing={side},latex={level}': {'model': rid(f'block/tree_tap_{level}'), 'y': rotation}
        for side, rotation in [('north', 0), ('east', 90), ('south', 180), ('west', 270)] for level in range(5)}})
    write(root / 'items' / 'tree_tap.json', {'model': {'type': 'minecraft:model', 'model': rid('block/tree_tap_0')}})
    write(root / 'models' / 'item' / 'latex_bottle.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': rid('item/latex_bottle')}})
    write(root / 'items' / 'latex_bottle.json', {'model': {'type': 'minecraft:model', 'model': rid('item/latex_bottle')}})


def pillar(root, write, stem):
    for suffix, parent in [('', 'cube_column'), ('_horizontal', 'cube_column_horizontal')]:
        write(root / 'models' / 'block' / f'{stem}{suffix}.json', {'parent': 'minecraft:block/' + parent,
            'textures': {'end': rid('block/' + stem + '_top'), 'side': rid('block/' + stem)}})
    write(root / 'blockstates' / f'{stem}.json', {'variants': {
        'axis=y': {'model': rid('block/' + stem)},
        'axis=x': {'model': rid('block/' + stem + '_horizontal'), 'x': 90, 'y': 90},
        'axis=z': {'model': rid('block/' + stem + '_horizontal'), 'x': 90}}})
    write(root / 'items' / f'{stem}.json', {'model': {'type': 'minecraft:model', 'model': rid('block/' + stem)}})


def loot(out, write):
    for block in ('tree_fern_trunk', 'rubber_log', 'tree_tap'):
        write(out / f'{block}.json', self_drop(block))


def tags(tags):
    for registry in ('block', 'item'):
        tags.add(registry, 'minecraft:logs', rid('tree_fern_trunk'))
        tags.add(registry, 'minecraft:logs_that_burn', rid('tree_fern_trunk'))
    tags.add('block', 'minecraft:mineable/axe', rid('tree_fern_trunk'))
    tags.add('block', 'minecraft:mineable/axe', rid('rubber_log'))
    tags.add('block', 'minecraft:mineable/axe', rid('tree_tap'))
    for registry in ('block', 'item'): tags.add(registry, 'minecraft:logs', rid('rubber_log'))
