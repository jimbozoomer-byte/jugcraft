"""Found plastic (docs/features/found-plastic.md): old plastic things in chests, broken down by hand into plastic pellets.

A player who finds them skips the oil refinery for their first few plastic sheets. They come only from chests that
generate once with a structure (chemistry/FoundPlastic.java lists the chests and the odds), never from mobs, fishing,
trades or a recipe, so they cannot be farmed. Each breaks down in the crafting grid; the Metal Press still makes the
pellets into sheets.
"""
ITEMS = {
    'old_plastic_bottle': 'Old Plastic Bottle',
    'plastic_duck': 'Plastic Duck',
    'old_plastic_crate': 'Old Plastic Crate',
}
# Plastic pellets each one breaks down into.
PELLETS = {'old_plastic_bottle': 1, 'plastic_duck': 2, 'old_plastic_crate': 4}


def write_extras(write, assets, data, lang, condition):
    for name, count in PELLETS.items():
        write(data/'recipe'/('plastic_pellets_from_'+name+'.json'), {'fabric:load_conditions': condition('machines'),
              'type': 'minecraft:crafting_shapeless', 'category': 'misc', 'ingredients': ['jugcraft:'+name],
              'result': {'id': 'jugcraft:plastic_pellets', 'count': count}})
        lang['tooltip.jugcraft.'+name] = (f'Found in old chests. Breaks down into {count} plastic pellet'
                                          + ('s' if count > 1 else '') + ' in a crafting grid.')


def draw_all(save):
    import item_icons
    for name in ITEMS: save(item_icons.draw(name), 'item', name)
