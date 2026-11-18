"""Batch 2 pharmacy: fixed game formulations on the shared industrial machine foundation."""
from starter_chemistry import form

MOD = 'jugcraft'
FORMULAS = {
    'caffeine': ('Caffeine', 'caffeine_drink'), 'aspirin': ('Aspirin', 'aspirin'),
    'paracetamol': ('Paracetamol', 'paracetamol'), 'rehydration_salts': ('Rehydration Salts', 'rehydration_salts'),
    'activated_charcoal': ('Activated Charcoal', 'activated_charcoal'), 'stimulant': ('Stimulant', 'stimulant'),
}
ITEMS = {
    'blank_formula_plate': ('Blank Formula Plate', 'general_chip_pattern'),
    'pharmaceutical_base': ('Prepared Pharmaceutical Base', 'salicylic_acid'),
    **{f'{key}_formula_plate': (f'{name} Formula Plate', 'general_chip_pattern')
       for key, (name, sample) in FORMULAS.items()},
}
KEY = {'P': '#c:plates/steel', 'M': 'jugcraft:electric_motor', 'C': 'jugcraft:basic_circuit',
       'K': 'jugcraft:machine_casing', 'W': 'jugcraft:copper_cable'}
FORMS = {
    'laboratory_analyzer': form('Laboratory Analyzer', 'chemical_reactor', 2, 2, 2,
        ['analysis_water'], [], 'pharmacy_analysis', [
            ('power', 'ENERGY_IN', 0, 0, 0, 0, 'FRONT'),
            ('sample_in', 'ITEM_IN', 0, 0, 0, 1, 'FRONT'),
            ('plate_in', 'ITEM_IN', 1, 1, 0, 1, 'FRONT'),
            ('formula_out', 'ITEM_OUT', 0, 1, 0, 0, 'FRONT'),
            ('water_in', 'FLUID_IN', 0, 0, 1, 0, 'BACK')],
        (['PGP', 'MCK', 'PWP'], {**KEY, 'G': 'minecraft:glass_pane'}), items=(2, 1)),
    'pharmaceutical_separator': form('Pharmaceutical Separator', 'chemical_reactor', 2, 2, 3,
        ['extraction_water'], [], 'botanical_separation', [
            ('power', 'ENERGY_IN', 0, 0, 0, 0, 'FRONT'),
            ('feed_in', 'ITEM_IN', 0, 0, 0, 2, 'TOP'),
            ('extract_out', 'ITEM_OUT', 0, 1, 0, 0, 'FRONT'),
            ('water_in', 'FLUID_IN', 0, 0, 1, 0, 'BACK')],
        (['PTP', 'MCK', 'PWP'], {**KEY, 'T': 'jugcraft:fluid_tank'}), items=(1, 1)),
    'formulation_press': form('Formulation Press', 'chemical_reactor', 2, 3, 2,
        ['formulation_water', 'formulation_reagent'], [], 'drug_formulation', [
            ('power', 'ENERGY_IN', 0, 0, 0, 0, 'FRONT'),
            ('active_in', 'ITEM_IN', 0, 0, 0, 1, 'FRONT'),
            ('binder_in', 'ITEM_IN', 1, 1, 0, 1, 'FRONT'),
            ('packaging_in', 'ITEM_IN', 2, 1, 1, 1, 'RIGHT'),
            ('medicine_out', 'ITEM_OUT', 0, 1, 0, 0, 'FRONT'),
            ('water_in', 'FLUID_IN', 0, 0, 2, 0, 'BACK'),
            ('reagent_in', 'FLUID_IN', 1, 1, 2, 0, 'BACK')],
        (['PGP', 'MCK', 'PWP'], {**KEY, 'G': 'minecraft:piston'}), items=(3, 1),
        sockets=[('formula', 'jugcraft:pharmacy_formulas')]),
}
for info in FORMS.values():
    info['features'] = ['machines', 'agriculture']
FORMS['laboratory_analyzer']['warmup'] = 20
FORMS['formulation_press']['warmup'] = 20

def rec(name, machine, items, results, water=500, reagent=None, ticks=160, power=48, tool=None, features=()):
    data = dict(name='pharmacy_' + name, capability=FORMS[machine]['capabilities'][0],
        items=items, results=results, fluids=[('minecraft:water', water)], ticks=ticks,
        energy_per_tick=power, features=['machines', 'agriculture', *features])
    if reagent: data['fluids'].append(reagent)
    if tool: data['tool'] = f'jugcraft:{tool}_formula_plate'
    return data

RECIPES = {
    'laboratory_analyzer': [rec('analyze_' + key, 'laboratory_analyzer',
        [('jugcraft:' + sample, 1), ('jugcraft:blank_formula_plate', 1)],
        [('jugcraft:' + key + '_formula_plate', 1)], water=250, power=32,
        features=['salt'] if key == 'rehydration_salts' else [])
        for key, (name, sample) in FORMULAS.items()],
    'pharmaceutical_separator': [
        rec('coffee_extract', 'pharmaceutical_separator', [('jugcraft:coffee_beans', 4)], [('jugcraft:caffeine_concentrate', 8)]),
        rec('cocoa_extract', 'pharmaceutical_separator', [('minecraft:cocoa_beans', 8)], [('jugcraft:caffeine_concentrate', 4)]),
        rec('willow_extract', 'pharmaceutical_separator', [('jugcraft:willow_log', 4)], [('jugcraft:salicylic_acid', 4)], features=['biomes']),
        rec('birch_extract', 'pharmaceutical_separator', [('minecraft:birch_log', 4)], [('jugcraft:salicylic_acid', 2)]),
        rec('potato_binder', 'pharmaceutical_separator', [('minecraft:potato', 4)], [('jugcraft:pharmaceutical_starch', 8)]),
        rec('corn_binder', 'pharmaceutical_separator', [('jugcraft:corn', 4)], [('jugcraft:pharmaceutical_starch', 4)]),
    ],
    'formulation_press': [
        rec('batch_caffeine', 'formulation_press', [('jugcraft:caffeine_concentrate', 8), ('minecraft:sugar', 4),
            ('minecraft:glass_bottle', 8)], [('jugcraft:caffeine_drink', 8)], water=2000, power=64, tool='caffeine'),
        rec('batch_aspirin', 'formulation_press', [('jugcraft:salicylic_acid', 4), ('jugcraft:cider_vinegar', 2),
            ('jugcraft:pharmaceutical_starch', 2)], [('jugcraft:aspirin', 8)], water=250,
            reagent=('jugcraft:sulfuric_acid', 50), power=64, tool='aspirin', features=['sulfur']),
        rec('batch_paracetamol', 'formulation_press', [('jugcraft:pharmaceutical_base', 2), ('jugcraft:pharmaceutical_starch', 2)],
            [('jugcraft:paracetamol', 8)], water=250, power=64, tool='paracetamol'),
        rec('batch_salts', 'formulation_press', [('jugcraft:salt', 4), ('minecraft:sugar', 2), ('minecraft:paper', 8)],
            [('jugcraft:rehydration_salts', 8)], water=2000, power=64, tool='rehydration_salts', features=['salt']),
        rec('batch_charcoal', 'formulation_press', [('minecraft:charcoal', 4), ('minecraft:redstone', 2),
            ('jugcraft:pharmaceutical_starch', 2)], [('jugcraft:activated_charcoal', 8)], water=250,
            power=64, tool='activated_charcoal'),
        rec('batch_stimulant', 'formulation_press', [('jugcraft:caffeine_concentrate', 4), ('jugcraft:pharmaceutical_base', 2),
            ('jugcraft:pharmaceutical_starch', 4)], [('jugcraft:stimulant', 4)], water=500,
            reagent=('jugcraft:bioethanol', 500), power=64, tool='stimulant'),
    ],
}
# Existing compact reactor, deliberately fictional: no additional plant needed to enter this chain.
REACTOR_RECIPES = [dict(name='pharmacy_base', items=[('jugcraft:pharmaceutical_starch', 2), ('minecraft:sugar', 1)],
    fluids=[('minecraft:water', 250)], results=[('jugcraft:pharmaceutical_base', 4)], ticks=160,
    features=['machines', 'agriculture'])]
TANK_NAMES = {'analysis_water': 'Analysis water', 'extraction_water': 'Extraction water',
              'formulation_water': 'Formulation water', 'formulation_reagent': 'Acid / ethanol reagent'}
HANDBOOK = {
    'laboratory_analyzer': ['Consumes one finished medicine sample and one Blank Formula Plate with 250 mB water to write its reusable Formula Plate. 160 ticks at 32 JE/t. Each sample has a fixed authored result; no random grades, bad batches or personal research requirements.',
        'Upper front sample and blank-plate drawers; lower front-left power and front-right formula output; rear water inlet. Formula plates are tradeable items. Their initial samples come from the existing manual or compact-reactor recipes.'],
    'pharmaceutical_separator': ['Powered wet separation improves crop yields: four coffee beans give eight concentrate, eight cocoa beans give four; four willow logs give four salicylic acid, four birch logs give two; four potatoes give eight pharmaceutical starch, four corn give four. Each uses 500 mB water and 160 ticks at 48 JE/t. Water is consumed as extraction loss; no waste-management chore is added.',
        'Feed hatch on top, lower front-right extract drawer, lower front-left power, and water at the rear. The birch/potato/cocoa alternatives remain reachable without custom biome ingredients.'],
    'formulation_press': ['Install the matching reusable Formula Plate in the formula socket. The machine reserves output space and consumes the listed solids and fluids once, keeping the plate. Six fixed recipes produce eight caffeine drinks, eight aspirin, eight paracetamol, eight salts sachets, eight charcoal or four stimulant tablets. Every recipe takes 160 ticks at 64 JE/t.',
        'Upper front active and binder drawers; the side drawer takes the third solid input, usually bottles. Lower front-right medicine output, lower front-left power. Rear water and reagent are separate inlets. Aspirin also needs sulfuric acid; Stimulant also needs bioethanol. The other four recipes leave the reagent tank unused. Empty incompatible fluids before switching jobs and use the existing recipe picker and lock.',
        'Every bottled output consumes one empty bottle. No free bottle or bucket is produced by machine work. Drinking still returns its normal bottle. Bulk production changes yields and automation, never duration, potency, strain or tolerance.'],
}

def write_extras(write, assets, data, lang, condition):
    lang['container.jugcraft.form.socket.formula'] = 'Formula plate'
    write(data / 'tags/item/pharmacy_formulas.json', {'values': [f'jugcraft:{key}_formula_plate' for key in FORMULAS]})
    write(data / 'recipe/blank_formula_plate.json', {
        'fabric:load_conditions': condition('machines') + condition('agriculture'),
        'type': 'minecraft:crafting_shapeless', 'category': 'misc',
        'ingredients': ['#c:plates/copper', 'minecraft:paper', 'minecraft:glass_pane'],
        'result': {'id': 'jugcraft:blank_formula_plate', 'count': 2}})
    lang['tooltip.jugcraft.blank_formula_plate'] = 'Analyze a medicine sample or sterile stock to write a reusable manufacturing formula.'
    lang['tooltip.jugcraft.pharmaceutical_base'] = 'A fictional prepared base for Paracetamol and Stimulant manufacturing.'
    for key, (name, sample) in FORMULAS.items():
        lang[f'tooltip.jugcraft.{key}_formula_plate'] = f'Reusable {name} manufacturing tool. Install in the Formulation Press; never consumed.'
    for name, ingredient in [(key, 'jugcraft:basic_circuit') for key in FORMS] + [
            ('blank_formula_plate', 'minecraft:paper'), ('chemical_reaction/pharmacy_base', 'jugcraft:pharmaceutical_starch')]:
        write(data / 'advancement/recipes/pharmacy_factory' / f'{name}.json', {
            'fabric:load_conditions': condition('machines') + condition('agriculture'),
            'parent': 'minecraft:recipes/root', 'criteria': {
                'has_ingredient': {'trigger': 'minecraft:inventory_changed', 'conditions': {'items': [{'items': ingredient}]}},
                'has_the_recipe': {'trigger': 'minecraft:recipe_unlocked', 'conditions': {'recipes': ['jugcraft:' + name]}}},
            'requirements': [['has_ingredient', 'has_the_recipe']], 'rewards': {'recipes': ['jugcraft:' + name]}})

def handbook_pages():
    return [{'title': FORMS[key]['display'], 'icon': 'jugcraft:' + key, 'text': text} for key, text in HANDBOOK.items()] + [
        {'title': 'Building a Pharmacy', 'icon': 'jugcraft:blank_formula_plate', 'text': [
            'Build the three steel-era stations from earlier steel plates, basic circuits, motors, machine casings and copper cable. Their recipes need no formula plate or pharmaceutical output. A copper plate, paper and glass pane make two blank formula plates.',
            'Prepare each first medicine by its existing route, then sacrifice one sample to the Analyzer. Move the resulting formula into the Press socket once; pipes and hoppers cannot steal tools. Different medicines need different plates. There is no personal research lock on traded equipment or plates.',
            'Use the Separator for improved concentrate, bark extract and starch yields. The existing Chemical Reactor turns two starch, sugar and 250 mB water into four Prepared Pharmaceutical Base, which supplies both Paracetamol and Stimulant batches. Sugar keeps this recipe distinct from the earlier redstone-based Paracetamol route. All chains are fictional game formulations.',
            'Connect existing cables, fluid pipes, hoppers or conveyors only at the marked ports. Full output, missing tools, power loss, pause, cancellation and saved work follow the shared machine rules. Original recipes remain useful for first samples and small-scale preparation.',
            'Batch production makes the same stable medicine items used by early pharmacy. Saved drug rules and positive effects are unchanged. No additional injury conditions, medical debuffs, purity rolls or maintenance timers are introduced.'
        ]}]
