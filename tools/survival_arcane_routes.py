"""Native Forbidden acquisition paths not represented by vanilla recipes.

Rituals retain their real forge tier, essence sources, enhancer requirements and
floor materials. This is qualitative acquisition, not a simulator for quantities,
component contents, durability, growth time or ritual duration.
"""
import json


def extend(root, edge, evidence, items):
    res = root / 'src/main/resources'
    data = res / 'data/jugcraft'
    java = 'src/main/java/io/github/jimbozoomer/jugcraft/arcane/'

    def read(path):
        return json.loads(path.read_text(encoding='utf-8'))

    def f(name):
        return 'jugcraft:forbidden_' + name

    def output(value):
        return value if isinstance(value, str) else value.get('id', value.get('item'))

    def route(source, kind, needs, *results):
        edge(source, kind, needs, [output(result) for result in results])

    # Strong Regeneration is component-sensitive in ArcaneAurealRecipe. Retain
    # its actual vanilla brewing prerequisites instead of accepting any potion.
    route(java + 'ArcaneAurealRecipe.java', 'craft_with_strong_regeneration',
          [f('arcane_crystal_dust'), 'minecraft:brewing_stand', 'minecraft:blaze_powder',
           'minecraft:glass_bottle', 'fluid:minecraft:water', 'minecraft:nether_wart',
           'minecraft:ghast_tear', 'minecraft:glowstone_dust'], f('aureal_bottle'))
    route(java + 'ArcaneEssences.java', 'collect_blood_with_offhand_tube',
          [f('test_tube'), 'minecraft:wooden_sword'], f('blood_test_tube'))
    route(java + 'ArcaneEssences.java', 'extract_soul_from_soul_sand',
          [f('soul_extractor'), 'minecraft:soul_sand'], f('soul'), f('soulless_sand'))
    route(java + 'ArcaneGrowth.java', 'corrupt_sapling',
          [f('corrupt_soul'), ['minecraft:oak_sapling', 'minecraft:dead_bush']], f('growing_edelwood'))
    route(java + 'ArcaneGrowth.java', 'grow_edelwood',
          [f('growing_edelwood'), 'minecraft:dirt'],
          f('edelwood_log'), f('carved_edelwood_log'), f('edelwood_branch'))
    route(java + 'ArcaneGrowth.java', 'bonemeal_fungyss',
          [f('fungyss'), 'minecraft:bone_meal', 'minecraft:stone'], f('fungyss_stem'), f('fungyss_block'))
    route(java + 'EdelwoodBlocks.java', 'bottle_regenerating_edelwood_oil',
          [[f('edelwood_log'), f('carved_edelwood_log')], 'minecraft:glass_bottle'], f('edelwood_oil'))
    route(java + 'ArcaneBlackHole.java', 'form_black_hole_and_absorb_experience_orbs',
          [f('dark_matter'), f('corrupti_dust'), 'minecraft:experience_bottle'], f('xpetrified_orb'))
    route(java + 'ArcaneJar.java', 'fill_and_pick_up_essence_jar',
          [f('utrem_jar'), [f(name) for name in ('aureal_bottle', 'soul', 'corrupt_soul',
                                              'enchanted_soul', 'blood_test_tube', 'xpetrified_orb')]],
          f('essence_utrem_jar'))
    for fluid in ('water', 'lava'):
        route(java + 'ArcaneBuckets.java', 'fill_edelwood_bucket',
              [f('edelwood_bucket'), 'fluid:minecraft:' + fluid], f('edelwood_' + fluid + '_bucket'))
    route(java + 'ArcaneBuckets.java', 'milk_adult_cow_or_goat',
          [f('edelwood_bucket')], f('edelwood_milk_bucket'))
    route(java + 'ArcaneBuckets.java', 'collect_powder_snow_source',
          [f('edelwood_bucket'), 'minecraft:powder_snow_bucket'], f('edelwood_powder_snow_bucket'))
    route(java + 'ArcaneObelisk.java', 'form_three_cell_obelisk_and_collect_lower_part',
          [f('arcane_polished_darkstone'), f('arcane_crystal_block'), f('mundabitur_dust'), '#minecraft:pickaxes'],
          f('arcane_crystal_obelisk'))
    for before, after in zip(('obsidian', 'cracked_obsidian', 'fragmented_obsidian'),
                             ('cracked_obsidian', 'fragmented_obsidian', 'fading_obsidian')):
        route(java + 'ArcaneSkulls.java', 'wear_skull_until_fire_protection_expires',
              [f(before + '_skull'), 'minecraft:flint_and_steel'], f(after + '_skull'))
    route(java + 'ForbiddenArcane.java', 'strip_aurum_log_with_axe',
          [f('aurum_log'), '#minecraft:axes'], f('stripped_aurum_log'))

    floor = [f(name) for name in ('gilded_chiseled_polished_darkstone',
                                 'chiseled_arcane_polished_darkstone', 'polished_darkstone')]
    route(java + 'ArcaneDust.java', 'form_hephaestus_forge_over_complete_floor',
          ['minecraft:smithing_table', f('mundabitur_dust')] + floor, f('hephaestus_forge_tier_1'))
    essence_sources = {
        'aureal': f('aureal_bottle'), 'souls': [f('soul'), f('corrupt_soul'), f('enchanted_soul')],
        'blood': f('blood_test_tube'), 'experience': f('xpetrified_orb'),
    }
    for path in sorted((data / 'arcane_ritual').rglob('*.json')):
        recipe = read(path)
        result = recipe['result']
        tier = recipe.get('forge_tier', 1)
        tiers = [f('hephaestus_forge_tier_' + str(n)) for n in range(tier, 6)]
        if recipe.get('match_tier_exact') or result['type'] == 'jugcraft:forbidden_upgrade_tier':
            tiers = [f('hephaestus_forge_tier_' + str(tier))]
        needs = [recipe['main_ingredient'], tiers, '#jugcraft:forbidden_blacksmith_gavel'] + floor
        needs += [slot['ingredient'] for slot in recipe['inputs']]
        if recipe['inputs']:
            needs.append([f('darkstone_pedestal'), f('magnetized_darkstone_pedestal')])
        needs += [source for name, source in essence_sources.items() if recipe.get('essences', {}).get(name, 0) > 0]
        if recipe.get('enhancers'):
            enhancers = recipe['enhancers']
            needs += enhancers if isinstance(enhancers, list) else [enhancers]
        if result['type'] == 'jugcraft:forbidden_upgrade_tier':
            produced = f('hephaestus_forge_tier_' + str(result['result_tier']))
        elif result['type'] in ('jugcraft:forbidden_create_item', 'jugcraft:forbidden_transmute_input'):
            produced = result['result_item']
        else:
            continue
        route(path.relative_to(root).as_posix(), 'native_arcane_ritual', needs, produced)

    route(java + 'ArcaneMixture.java', 'magnetize_pedestal',
          [f('darkstone_pedestal'), f('ferrognetic_mixture')], f('magnetized_darkstone_pedestal'))
    clibano = [f(name) for name in ('clibano_core', 'polished_darkstone',
                                   'polished_darkstone_bricks', 'mundabitur_dust')]
    catalog = read(res / 'jugcraft/newest_forbidden.json')
    for path in sorted((data / 'arcane_combustion').glob('*.json')):
        recipe = read(path)
        ingredients = recipe['ingredients']
        needs = ([ingredients['first'], ingredients['second']] if 'first' in ingredients else [ingredients])
        needs += clibano + ['#minecraft:coals']
        if recipe.get('enhancer'):
            needs.append(recipe['enhancer'])
        fire = recipe['fire_type']
        if fire != 'fire':
            needs.append('#jugcraft:forbidden_clibano/creates_' + fire)
        source = path.relative_to(root).as_posix()
        route(source, 'native_clibano_combustion', needs, recipe['result'])
        residue = recipe.get('residue')
        if residue and residue.get('chance', 0) > 0:
            name = residue['type'].removeprefix('jugcraft:forbidden_')
            # Residues are generated only with soul/enchanted fire, even when
            # the input recipe itself accepts ordinary fire.
            fire_source = ['#jugcraft:forbidden_clibano/creates_soul_fire',
                           '#jugcraft:forbidden_clibano/creates_enchanted_fire']
            info = catalog['residues'][name]['combine_info']
            if info['required_amount'] > 0:
                route(source, 'recover_clibano_residue', needs + [fire_source], info['result'])
    return {f('magical_farmland'): 'In-world soil state: ArcaneGrowth.Meal converts farmland; its native loot always returns dirt, including with Silk Touch.'}
