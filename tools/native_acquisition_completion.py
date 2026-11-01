"""Paid native routes for registered content missing from the imported recipe set.

Use existing workshops and materials. Equipment rituals transmute the paid base
stack so names, enchantments and damage survive; no new progression subsystem.
"""
from copy import deepcopy
from newest_building import RES, read


def generate(write):
    data = RES / 'data/jugcraft'
    from newest_building import SOURCE, map_value
    from newest_twigs import recipe as native_recipe
    botanical = SOURCE / 'Jugtania/data/botania/recipes'
    for path in sorted(botanical.rglob('*.json')):
        recipe = read(path)
        kind = recipe.get('type')
        if kind in ('botania:armor_upgrade', 'botania:water_bottle_matching_shaped'):
            recipe['type'] = 'jugcraft:botanical_armor_upgrade' if kind == 'botania:armor_upgrade' else 'jugcraft:botanical_water_crafting'
            write(data / 'recipe/jugtania' / path.relative_to(botanical), native_recipe(map_value(recipe, 'botania', 'jugtania')))
    # The supplied largemouth file accidentally repeats the smallmouth type.
    # Give the distinct registered fish an actual freshwater capture source.
    aquatic = read(RES / 'jugcraft/newest_aquatic.json')
    if not any(row['spawn']['type'] == 'jugcraft:aquatic_largemouth_bass' for row in aquatic['spawns']):
        aquatic['spawns'].append({'type': 'jugcraft:aquatic_fish_spawn',
            'spawn': {'type': 'jugcraft:aquatic_largemouth_bass', 'weight': 7, 'minCount': 1, 'maxCount': 2},
            'includeBiomes': ['minecraft:river', 'minecraft:swamp'], 'excludeBiomes': [], 'and': False})
    write(RES / 'jugcraft/newest_aquatic.json', aquatic)

    def item(name):
        return name if ':' in name else 'jugcraft:' + name

    def craft(name, ingredients, count=1):
        write(data / 'recipe/acquisition' / (name + '.json'), {
            'type': 'minecraft:crafting_shapeless',
            'ingredients': [item(value) for value in ingredients],
            'result': {'id': item(name), 'count': count}})

    # Retain the source furniture family's bills/yields for missing wood variants.
    def furniture_variant(name, sibling, replacements):
        recipe = deepcopy(read(data / 'recipe/newest/homeworks' / (sibling + '.json')))
        recipe['key'] = {key: replacements.get(value, value) for key, value in recipe['key'].items()}
        recipe['result']['id'] = item('homeworks_' + name)
        write(data / 'recipe/acquisition' / ('homeworks_' + name + '.json'), recipe)

    furniture_variant('jungle_wall_mirror', 'oak_wall_mirror', {'minecraft:oak_planks': 'minecraft:jungle_planks'})
    craft('homeworks_mangrove_lantern', ['minecraft:lantern', 'minecraft:mangrove_planks', 'minecraft:mangrove_planks'])
    for material in ('glass', 'iron'):
        for part in ('chair', 'wall_clock', 'grandfather_clock'):
            craft('homeworks_' + material + '_' + part,
                  ['homeworks_oak_' + part] + ['minecraft:glass' if material == 'glass' else 'minecraft:iron_ingot'] * 4)
    for style, material in (('ominous', 'minecraft:echo_shard'), ('undead', 'minecraft:bone')):
        for part in ('chair', 'grandfather_clock', 'lamp', 'table', 'wall_clock'):
            craft('homeworks_' + style + '_' + part, ['homeworks_oak_' + part, material, material])
    craft('homeworks_trader_cushion', ['homeworks_cushion', 'minecraft:blue_dye', 'minecraft:gold_nugget'])

    craft('holidayworks_bat_awake', ['holidayworks_bat_sleeping', 'minecraft:redstone'])
    for mood, dye in (('happy', 'yellow'), ('haunting', 'cyan')):
        craft('holidayworks_' + mood + '_ghost', ['holidayworks_ghost', 'minecraft:' + dye + '_dye'])
    for color in ('blue', 'brown', 'green', 'red', 'white'):
        craft('holidayworks_couple_' + color + '_stockings', ['holidayworks_' + color + '_stocking'] * 2)

    craft('ars_additions_lost_codex_entry', ['ars_additions_codex_entry'] * 3)
    craft('ars_additions_ancient_codex_entry', ['ars_additions_lost_codex_entry'] * 3)
    # 3*55 ->160 and 3*160 ->480 experience: consolidation cannot mint XP.
    craft('ars_additions_enchanting_wixie_cauldron',
          ['nouveau_wixie_cauldron', 'nouveau_enchanting_apparatus', 'minecraft:enchanting_table'])
    write(data / 'nouveau_recipe/acquisition/memory_crystal.json', {
        'type': 'enchanting_apparatus', 'reagent': {'item': 'minecraft:amethyst_shard'},
        'pedestalItems': [{'item': item(name)} for name in
                          ('nouveau_source_gem', 'nouveau_source_gem', 'minecraft:gold_ingot', 'minecraft:gold_ingot')],
        'sourceCost': 1000, 'keepNbtOfReagent': False,
        'result': {'id': item('ars_additions_memory_crystal'), 'count': 1}})

    craft('twigworks_twigs', ['twigworks_twig'] * 2)
    craft('twigworks_black_packed_silt', ['twigworks_packed_silt', 'minecraft:black_dye'])
    write(data / 'recipe/acquisition/twigworks_petrified_lichen.json', {
        'type': 'minecraft:smelting', 'ingredient': 'minecraft:glow_lichen',
        'result': {'id': item('twigworks_petrified_lichen')}, 'experience': 0.1, 'cookingtime': 200})
    craft('supps_edelwood_stick', ['forbidden_edelwood_log'], 4)
    craft('supps_propelplant_cane', ['minecraft:sugar_cane', 'minecraft:slime_ball'])
    craft('jugtania_living_root', ['jugtania_livingwood_twig', 'minecraft:wheat_seeds'], 2)
    craft('jugtania_root', ['jugtania_living_root'] * 4)
    write(data / 'recipe/acquisition/jugtania_pebble.json', {
        'type': 'minecraft:stonecutting', 'ingredient': 'minecraft:cobblestone',
        'result': {'id': item('jugtania_pebble'), 'count': 4}})
    # A paid conversion from the equivalent Ender Dagger retains its components.
    write(data / 'recipe/acquisition/jugtania_soulscribe.json', {
        'type': 'minecraft:smithing_transform', 'base': item('jugtania_ender_dagger'),
        'addition': 'minecraft:ink_sac', 'result': {'id': item('jugtania_soulscribe')}})
    craft('jugtania_lens_storm', ['jugtania_lens_normal', 'jugtania_rune_air', 'jugtania_rune_water', 'minecraft:lightning_rod'])
    craft('jugtania_gaia_head', ['minecraft:skeleton_skull'] + ['jugtania_life_essence'] * 4)

    craft('forbidden_arcane_crystal_dust_speck', ['forbidden_arcane_crystal_dust'], 9)
    craft('forbidden_splash_aureal_bottle', ['forbidden_aureal_bottle', 'minecraft:gunpowder'])
    craft('forbidden_mortar', ['forbidden_polished_darkstone'] * 3 + ['minecraft:flint'])

    def ritual(name, main, ingredients, tier=2, preserve=False):
        write(data / 'arcane_ritual/acquisition' / (name + '.json'), {
            'forge_tier': tier, 'main_ingredient': {'item': item(main)},
            'inputs': [{'amount': count, 'ingredient': {'item': item(ingredient)}}
                       for ingredient, count in ingredients],
            'essences': {'aureal': 400 * tier, 'blood': 200 * tier, 'experience': 200 * tier, 'souls': 3 * tier},
            'magic_circle': 'jugcraft:forbidden_create_item',
            'result': {'type': 'jugcraft:forbidden_transmute_input' if preserve else 'jugcraft:forbidden_create_item',
                       'result_item': item('forbidden_' + name)}})

    for tool in ('axe', 'hoe', 'pickaxe', 'shovel', 'sword'):
        ritual('draco_arcanus_' + tool, 'minecraft:netherite_' + tool,
               [('forbidden_dragon_scale', 2), ('forbidden_obsidiansteel_ingot', 3)], preserve=True)
        ritual('reinforced_deorum_' + tool, 'minecraft:netherite_' + tool,
               [('forbidden_deorum_ingot', 4), ('forbidden_stellarite_piece', 1)], tier=3, preserve=True)
    ritual('reinforced_deorum_blacksmith_gavel', 'forbidden_netherite_blacksmith_gavel',
           [('forbidden_deorum_ingot', 4), ('forbidden_stellarite_piece', 1)], tier=3, preserve=True)
    ritual('draco_arcanus_staff', 'forbidden_magic_wand',
           [('forbidden_dragon_scale', 2), ('forbidden_obsidiansteel_ingot', 3)])
    ritual('draco_arcanus_scepter', 'forbidden_draco_arcanus_staff',
           [('forbidden_dragon_scale', 2), ('forbidden_arcane_crystal', 4)], tier=3)
    for part in ('boots', 'chestplate', 'helmet', 'leggings'):
        ritual('mortem_' + part, 'minecraft:leather_' + part,
               [('forbidden_dark_matter', 2), ('forbidden_soul_binding_crystal', 1)], tier=3, preserve=True)
    for name, main, ingredients, tier in (
            ('corrupted_arcane_crystal', 'forbidden_arcane_crystal', [('forbidden_corrupt_soul', 1)], 1),
            ('corrupted_arcane_crystal_obelisk', 'forbidden_arcane_crystal_obelisk', [('forbidden_corrupt_soul', 3)], 2),
            ('aureal_wardstone', 'forbidden_arcane_crystal_block', [('forbidden_aureal_bottle', 4), ('minecraft:obsidian', 4)], 2),
            ('aurealic_obsidian_skull', 'forbidden_obsidian_skull', [('forbidden_aureal_bottle', 4), ('forbidden_deorum_ingot', 2)], 2),
            ('eternal_obsidian_skull', 'forbidden_obsidian_skull', [('forbidden_eternal_stella', 1)], 3),
            ('crescent_moon', 'minecraft:gold_ingot', [('forbidden_enchanted_soul', 1), ('minecraft:ender_pearl', 2)], 2),
            ('soul_crimson_stone', 'minecraft:crimson_nylium', [('forbidden_soul', 4), ('forbidden_arcane_crystal', 2)], 2),
            ('omega_arcoin', 'forbidden_deorum_ingot', [('forbidden_dark_nether_star', 1), ('forbidden_stellarite_piece', 4)], 4)):
        ritual(name, main, ingredients, tier)
