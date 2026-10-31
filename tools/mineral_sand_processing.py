"""Four short mineral-sand lines on existing general processors; fictional game quantities.

Independent rare-earth recovery uses the shared FluidRecipe result_chances field.
No block, fluid, machine, reserve or terrain registration belongs to this module.
"""

ITEMS = {
    'neodymium_oxide': 'Neodymium Oxide',
    'cerium_oxide': 'Cerium Oxide',
    'yttrium_oxide': 'Yttrium Oxide',
    'garnet_grit': 'Garnet Grit',
    'cerium_polishing_head': 'Cerium Polishing Head',
    'garnet_abrasive_head': 'Garnet Abrasive Head',
    'garnet_sanding_block': 'Garnet Sanding Block',
}


def recipe(name, items, results, ticks, *, fluids=(), features=(), capability=None,
           power=None, tool=None, returns=(), chances=None):
    row = dict(name=name, items=list(items), results=list(results), fluids=list(fluids),
               fluid_results=list(returns), ticks=ticks, features=['machines', *features])
    if capability:
        row['capability'] = 'jugcraft:' + capability
    if power is not None:
        row['energy_per_tick'] = power
    if tool:
        row['tool'] = 'jugcraft:' + tool
    if chances is not None:
        row['result_chances'] = list(chances)
    return row


REACTOR_RECIPES = [
    recipe('sands_zirconia_powder', [('jugcraft:zircon_sand', 4)], [('jugcraft:zirconia_powder', 1)],
           320, fluids=[('jugcraft:lye', 500), ('minecraft:water', 250)], features=['mineral_sands', 'salt']),
    recipe('sands_zirconia_from_concentrate', [('jugcraft:zircon_concentrate', 2)],
           [('jugcraft:zirconia_powder', 1)], 320,
           fluids=[('jugcraft:lye', 500), ('minecraft:water', 250)], features=['salt']),
    recipe('sands_titanium_white_dye', [('jugcraft:raw_titanium', 1)], [('minecraft:white_dye', 4)],
           240, fluids=[('jugcraft:sulfuric_acid', 250), ('minecraft:water', 250)],
           features=['titanium', 'sulfur']),
    recipe('sands_rare_earth_separation', [('jugcraft:rare_earth_oxide', 1)],
           [('jugcraft:neodymium_oxide', 1), ('jugcraft:cerium_oxide', 2), ('jugcraft:yttrium_oxide', 1)],
           240, fluids=[('jugcraft:sulfuric_acid', 250)], features=['rare_earths', 'sulfur'],
           chances=[0.5, 0.5, 0.5]),
]

FORM_RECIPES = {
    'mineral_conditioner': [
        recipe('sands_titanium_feed', [('jugcraft:titanium_sand', 2)], [('jugcraft:raw_titanium', 1)],
               200, fluids=[('minecraft:water', 500)], features=['mineral_sands', 'titanium'],
               capability='mineral_conditioning', power=32),
        recipe('sands_mixed_rare_earth_oxide', [('jugcraft:rare_earth_sand', 2)],
               [('jugcraft:rare_earth_oxide', 1)], 200, fluids=[('minecraft:water', 500)],
               features=['mineral_sands', 'rare_earths'], capability='mineral_conditioning', power=32),
    ],
    'alumina_calciner': [
        recipe('sands_fire_zirconia_blank', [('jugcraft:zirconia_powder', 1)],
               [('jugcraft:fired_zirconia_blank', 1)], 320, features=['salt'],
               capability='ceramic_firing', power=80),
    ],
    'precision_finisher': [
        recipe('sands_finish_lens_cerium', [('jugcraft:optical_blank', 1)], [('jugcraft:polished_lens', 1)],
               160, fluids=[('minecraft:water', 125)], returns=[('minecraft:water', 125, 0)],
               features=['rare_earths', 'silicon'], capability='precision_finishing', power=48,
               tool='cerium_polishing_head'),
        recipe('sands_finish_shaft_garnet', [('jugcraft:steel_shaft_blank', 1)],
               [('jugcraft:precision_shaft', 1)], 300, fluids=[('minecraft:water', 125)],
               returns=[('minecraft:water', 125, 0)], features=['mineral_sands'],
               capability='precision_finishing', power=48, tool='garnet_abrasive_head'),
    ],
}

MACHINE_RECIPES = {
    'pulverizer': [dict(name='sands_garnet_grit', input='jugcraft:garnet_sand',
                       output='jugcraft:garnet_grit', count=1, ticks=120, features=['machines', 'mineral_sands'])],
    'alloy_smelter': [dict(name='sands_neodymium_magnet',
                          inputs=[('jugcraft:neodymium_oxide', 1), ('jugcraft:ferroboron', 1)],
                          output='jugcraft:neodymium_magnet', count=4, ticks=200,
                          features=['machines', 'rare_earths', 'silicon'])],
}


def workshop_recipes():
    return [(name, {
        'type': 'jugcraft:starter_working', 'station': 0,
        'ingredients': [dict(ingredient='minecraft:' + feed, count=1)],
        'result': dict(id='minecraft:' + result, count=1), 'time': 200,
        'tool': dict(id='jugcraft:garnet_sanding_block'), 'heated': False,
        'fabric:load_conditions': [dict(condition='jugcraft:feature_enabled', feature=f)
                                  for f in ['machines', 'mineral_sands']],
    }) for name, feed, result in [
        ('sands_smooth_sandstone', 'cut_sandstone', 'smooth_sandstone'),
        ('sands_smooth_red_sandstone', 'cut_red_sandstone', 'smooth_red_sandstone'),
    ]]


def write_extras(write, assets, data, lang, condition):
    for name, display in ITEMS.items():
        lang['item.jugcraft.' + name] = display
    write(data / 'tags/item/finishing_heads.json', dict(values=[
        'jugcraft:abrasive_head', 'jugcraft:polishing_head',
        'jugcraft:cerium_polishing_head', 'jugcraft:garnet_abrasive_head']))
    for material in ['neodymium', 'cerium', 'yttrium']:
        write(data.parent / 'c/tags/item/dusts' / (material + '_oxide.json'),
              dict(values=['jugcraft:' + material + '_oxide']))

    def shapeless(name, ingredients, output, features):
        write(data / 'recipe' / ('sands_' + name + '.json'), {
            'fabric:load_conditions': [c for f in ['machines', *features] for c in condition(f)],
            'type': 'minecraft:crafting_shapeless', 'category': 'misc', 'ingredients': ingredients,
            'result': dict(id='jugcraft:' + output, count=1)})

    shapeless('cerium_polishing_head', ['jugcraft:cerium_oxide'] * 2 + ['jugcraft:polishing_head'],
              'cerium_polishing_head', ['rare_earths', 'aluminum'])
    shapeless('garnet_sanding_block', ['jugcraft:garnet_grit', '#minecraft:wool', '#minecraft:planks'],
              'garnet_sanding_block', ['mineral_sands'])
    shapeless('garnet_abrasive_head', ['jugcraft:garnet_grit'] * 4 + ['minecraft:iron_ingot', 'jugcraft:copper_wire'],
              'garnet_abrasive_head', ['mineral_sands'])
    write(data / 'recipe/sands_efficiency_upgrade.json', {
        'fabric:load_conditions': [c for f in ['machines', 'rare_earths'] for c in condition(f)],
        'type': 'minecraft:crafting_shaped', 'category': 'misc', 'pattern': ['WYW', 'PCP'],
        'key': {'W': '#c:wires/copper', 'Y': 'jugcraft:yttrium_oxide', 'P': '#c:plates/steel',
                'C': 'jugcraft:basic_circuit'}, 'result': dict(id='jugcraft:efficiency_upgrade', count=1)})


GUIDE_PAGES = [
    dict(title='Four sands, familiar machines', icon='jugcraft:titanium_sand', text=[
        'Each regional mineral-bearing sand has its own useful short processing route. Shovel startup feed or use the existing powered Ore Drill; the sand blocks themselves are the finite reserve. No extra extractor, separator or waste-processing line is required.',
        'Titanium: two sand and 500 mB water in the Mineral Conditioner give one Raw Titanium. The existing Chemical Reactor takes that raw mineral, one Coke and 250 mB chlorine into Titanium Sponge; the Arc Furnace melts it into an ingot. Existing titanium equipment uses the result.',
        'The reactor can instead turn one Raw Titanium, 250 mB sulfuric acid and 250 mB water into four White Dye. Pigment and metal are separate choices that each spend their feed. Ordinary sand does not gain these recipes.']),
    dict(title='Short zirconia tooling', icon='jugcraft:zirconia_insert', text=[
        'The Chemical Reactor prepares one Zirconia Powder from four Zircon-rich Mineral Sand, 500 mB lye and 250 mB water, in 320 ticks. Two existing Zircon Concentrate can replace the four sand.',
        'The Industrial Oven prepares and fires one powder directly into one Fired Zirconia Blank in 320 ticks at 80 JE/t. The Precision Finisher with its retained Diamond Abrasive Head makes two cutting inserts in 200 ticks at 48 JE/t and returns its 125 mB cooling water.',
        'Inserts already make reusable polymer molds and extrusion dies. The older full ceramic route, regional concentrate recipe and expensive trace-sand recipe remain available for existing factories.']),
    dict(title='Independent rare-earth recovery', icon='jugcraft:rare_earth_oxide', text=[
        'Two Rare-earth-rich Mineral Sand and 500 mB water give one mixed Rare Earth Oxide in the Mineral Conditioner. The Chemical Reactor uses one mixed oxide and 250 mB sulfuric acid for one 240-tick recovery batch.',
        'Each batch rolls three independent 50% chances: one Neodymium Oxide, two Cerium Oxide and one Yttrium Oxide. Any combination is possible: all three occurs 12.5% of the time, and none also occurs 12.5%. A failed roll still spends the batch inputs and power. Reserve room for every possible output before processing; pausing or reloading never grants another roll.',
        'One Neodymium Oxide and one Ferroboron make four existing Neodymium Magnets in the Alloy Smelter. Two Cerium Oxide upgrade a Felt Polishing Head into a reusable Cerium Polishing Head: one lens in 160 rather than 200 ticks. Yttrium Oxide replaces the ceramic body in an alternate existing Efficiency Upgrade recipe, with unchanged upgrade behavior.']),
    dict(title='Garnet abrasives', icon='jugcraft:garnet_grit', text=[
        'The Pulverizer makes one Garnet Grit from one Garnet-rich Mineral Sand in 120 ticks. Craft a grit, wool and plank into a reusable sanding block; the Workshop Bench smooths cut sandstone or cut red sandstone, one block for one, in 200 ticks.',
        'Four grit, an iron ingot and copper wire make a reusable Garnet Abrasive Head. In the same Precision Finisher socket it finishes a steel shaft blank in 300 ticks at 48 JE/t, returning its 125 mB cooling water. The diamond head is faster and still handles hard fired ceramics; garnet is not an optical polish.',
        'All added heads are retained tools. There is no routine replacement, extra machine or abrasive refill. Existing machine recipe locks and blocked-output protections apply.']),
]


def draw_all(save):
    import item_icons
    for name in ITEMS:
        save(item_icons.draw(name), 'item', name)
