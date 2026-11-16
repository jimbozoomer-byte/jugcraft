"""Batch 4 beverages on shared process machinery. Fixed fictional grades, no real distilling settings."""
from starter_chemistry import form

GRADES = {
    'moonshine': ('Alcohol (Moonshine)', 480, 10, 20, .10, .20, 1.15, .05, 120, True),
    'refined_spirits': ('Alcohol (Refined Spirits)', 720, 10, 30, .15, .30, 1.20, .075, 120, True),
    'reserve_whiskey': ('Alcohol (Reserve Whiskey)', 960, 10, 40, .20, .40, 1.25, .10, 180, False),
    'high_proof_spirits': ('Alcohol (High-Proof Spirits)', 1200, 15, 50, .25, .50, 1.30, .125, 180, False),
}
ITEMS = {key: (info[0], 'stimulant') for key, info in GRADES.items()}
for key, texture in zip(GRADES, ('jimtafacts/antidote_vessel', 'glucose_syrup', 'cola_syrup', 'stimulant')):
    ITEMS[key] = (GRADES[key][0], texture)
ITEMS.update({
    'empty_mash_jug': ('Empty Mash Jug', 'mason_jar'), 'mash_jug': ('Mash Jug', 'glucose_syrup'),
    'fermented_mash_jug': ('Fermented Mash Jug', 'glucose_syrup'),
    'beverage_culture_pack': ('Beverage Culture Pack', 'flour'),
    'oak_conditioning_insert': ('Oak Conditioning Insert', 'general_chip_pattern'),
    **{key + '_concentrate': (info[0].replace('Alcohol (', '').rstrip(')') + ' Beverage Concentrate', 'glucose_syrup')
        for key, info in GRADES.items() if key != 'moonshine'},
    **{key + '_formula_plate': (info[0] + ' Formula Plate', 'general_chip_pattern') for key, info in GRADES.items() if key != 'moonshine'},
})
FLUIDS = {
    'beverage_wash': {'display': 'Beverage Wash', 'feature': 'agriculture',
        'colors': [(69, 43, 20), (128, 86, 39), (181, 137, 70), (225, 198, 135)], 'tick_delay': 5, 'slope': 4, 'drop_off': 1},
    'refined_beverage_stock': {'display': 'Refined Beverage Stock', 'feature': 'agriculture',
        'colors': [(99, 72, 30), (157, 121, 54), (204, 173, 99), (238, 219, 169)], 'tick_delay': 5, 'slope': 4, 'drop_off': 1},
}
FORMS = {
    'mash_conditioning_barrel': form('Mash and Conditioning Barrel', 'chemical_reactor', 2, 2, 2, [], [], 'beverage_barrel', [
        ('mash_in', 'ITEM_IN', 0, 0, 0, 1, 'FRONT'), ('flavor_in', 'ITEM_IN', 1, 1, 1, 1, 'BACK'),
        ('batch_out', 'ITEM_OUT', 0, 1, 0, 0, 'FRONT')],
        (['PIP', 'BCB', 'PIP'], {'P': 'minecraft:oak_planks', 'I': 'minecraft:iron_ingot', 'B': 'minecraft:barrel', 'C': 'minecraft:copper_ingot'}),
        items=(2, 1), sockets=[('conditioning', 'jugcraft:conditioning_tools')]),
    'moonshine_still': form('Moonshine Still', 'chemical_reactor', 2, 2, 2, [], [], 'moonshine_distillation', [
        ('mash_in', 'ITEM_IN', 0, 0, 0, 1, 'FRONT'), ('bottles_in', 'ITEM_IN', 1, 1, 1, 1, 'BACK'),
        ('fuel_in', 'ITEM_IN', 2, 1, 0, 0, 'RIGHT'), ('drinks_out', 'ITEM_OUT', 0, 1, 0, 0, 'FRONT'),
        ('jug_out', 'ITEM_OUT', 1, 0, 1, 0, 'BACK')],
        (['CGC', 'IFI', 'CBC'], {'C': 'minecraft:copper_ingot', 'G': 'minecraft:glass', 'I': 'minecraft:iron_ingot',
            'F': 'minecraft:furnace', 'B': 'minecraft:brick'}), items=(3, 2)),
}
for info in FORMS.values():
    info['upgrades'] = False
    info['warmup'] = 0
    info['features'] = ['machines', 'agriculture']
FORMS['mash_conditioning_barrel']['ambient'] = True
FORMS['moonshine_still']['fuelled'] = True

def recipe(name, cap, items=(), results=(), fluids=(), fluid_results=(), ticks=200, power=48, tool=None):
    result = dict(name='beverage_' + name, capability='jugcraft:' + cap, items=list(items), results=list(results),
        fluids=list(fluids), fluid_results=list(fluid_results), ticks=ticks, energy_per_tick=power,
        features=['machines', 'agriculture'])
    if tool: result['tool'] = 'jugcraft:' + tool
    return result

RECIPES = {
    'mash_conditioning_barrel': [
        recipe('ferment_mash', 'beverage_barrel', [('jugcraft:mash_jug', 1)], [('jugcraft:fermented_mash_jug', 1)], ticks=1200, power=1),
        recipe('reserve_conditioning', 'beverage_barrel', [('jugcraft:refined_spirits_concentrate', 4), ('minecraft:honeycomb', 1)],
            [('jugcraft:reserve_whiskey_concentrate', 4)], ticks=2400, power=1, tool='oak_conditioning_insert'),
    ],
    'moonshine_still': [recipe('moonshine', 'moonshine_distillation',
        [('jugcraft:fermented_mash_jug', 1), ('minecraft:glass_bottle', 4)],
        [('jugcraft:moonshine', 4), ('jugcraft:empty_mash_jug', 1)], power=1)],
    'industrial_fermenter': [recipe('wash', 'beverage_culture', [('jugcraft:beverage_culture_pack', 1)],
        fluids=[('jugcraft:grain_mash', 1000)], fluid_results=[('jugcraft:beverage_wash', 1000, 0)], ticks=400)],
    'chemical_distillation_station': [recipe('refine_stock', 'chemical_distillation', fluids=[('jugcraft:beverage_wash', 1000)],
        fluid_results=[('jugcraft:refined_beverage_stock', 500, 0), ('jugcraft:stillage', 500, 1)], ticks=240, power=64)],
    'pharmaceutical_separator': [
        recipe('refined_concentrate', 'botanical_separation', [('minecraft:sugar', 2)], [('jugcraft:refined_spirits_concentrate', 8)],
            fluids=[('jugcraft:refined_beverage_stock', 1000)]),
        recipe('high_proof_concentrate', 'botanical_separation', [('jugcraft:reserve_whiskey_concentrate', 4)], [('jugcraft:high_proof_spirits_concentrate', 4)],
            fluids=[('jugcraft:refined_beverage_stock', 500)], power=80, tool='precision_process_module'),
    ],
    'formulation_press': [recipe('bulk_moonshine', 'drug_formulation',
        [('jugcraft:refined_spirits_concentrate', 1), ('minecraft:sugar', 1), ('minecraft:glass_bottle', 4)],
        [('jugcraft:moonshine', 4)], tool='refined_spirits_formula_plate', ticks=160, power=64)], 'laboratory_analyzer': [],
}
for key in GRADES:
    if key == 'moonshine': continue
    RECIPES['formulation_press'].append(recipe('bottle_' + key, 'drug_formulation',
        [('jugcraft:' + key + '_concentrate', 4), ('minecraft:sugar', 1), ('minecraft:glass_bottle', 4)],
        [('jugcraft:' + key, 4)], tool=key + '_formula_plate', ticks=160, power=64))
    RECIPES['laboratory_analyzer'].append(recipe('analyze_' + key, 'pharmacy_analysis',
        [('jugcraft:' + key + '_concentrate', 1), ('jugcraft:blank_formula_plate', 1)], [('jugcraft:' + key + '_formula_plate', 1)],
        fluids=[('minecraft:water', 250)], ticks=160, power=32,
        tool='precision_process_module' if key == 'high_proof_spirits' else None))

HANDBOOK = {
    'mash_conditioning_barrel': ['A shared rustic barrel variant for new mash and conditioning jobs. It leaves all existing Cider Barrel items, recipes, saved contents and ageing unchanged. No electricity, generator or motor. New jobs advance only while the machine is loaded, and saved work/cancellation follow the common process rules.',
        'One Mash Jug ferments into one Fermented Mash Jug in 1200 loaded ticks. Later, four Refined Spirits Concentrate plus honeycomb condition into four Reserve Whiskey Concentrate in 2400 ticks, retaining an Oak Conditioning Insert. Install that insert in the conditioning socket; the initial mash job needs no tool.'],
    'moonshine_still': ['A fueled copper still with no power connections. One Fermented Mash Jug, four empty glass bottles and 200 furnace-fuel ticks make four Moonshine and return one Empty Mash Jug. Fuel burns only for paid processing; idle, pause, missing tools and blocked output do not consume fuel.',
        'Upper front mash, upper rear bottles, lower right fuel. Lower front drink output, lower rear returned-jug outlet. Fuel remainder containers stay in the fuel slot exactly once. This is an abstract game recipe with no real distillation controls.'],
}

def write_extras(write, assets, data, lang, condition):
    import pharmacy_factory, sterile_pharmacy, precision_pharmacy
    conditions = condition('machines') + condition('agriculture')
    def shaped(name, pattern, key, count=1):
        write(data / 'recipe' / (name + '.json'), {'fabric:load_conditions': conditions, 'type': 'minecraft:crafting_shaped',
            'category': 'misc', 'pattern': pattern, 'key': key, 'result': {'id': 'jugcraft:' + name, 'count': count}})
    def shapeless(name, ingredients, count=1, suffix=''):
        write(data / 'recipe' / (name + suffix + '.json'), {'fabric:load_conditions': conditions, 'type': 'minecraft:crafting_shapeless',
            'category': 'misc', 'ingredients': ingredients, 'result': {'id': 'jugcraft:' + name, 'count': count}})
    shaped('empty_mash_jug', ['B B', 'B B', ' B '], {'B': 'minecraft:brick'}, 2)
    for crop in ('wheat', 'corn'):
        crop_id = 'minecraft:wheat' if crop == 'wheat' else 'jugcraft:corn'
        shapeless('mash_jug', [crop_id] * 4 + ['minecraft:sugar'] * 2 + ['minecraft:water_bucket', 'jugcraft:empty_mash_jug'], suffix='_from_' + crop)
    shapeless('beverage_culture_pack', ['minecraft:sugar', 'minecraft:brown_mushroom', 'minecraft:paper'], 2)
    shaped('oak_conditioning_insert', ['PWP', 'WCW', 'PWP'], {'P': 'jugcraft:stainless_steel_plate', 'W': 'minecraft:oak_planks', 'C': 'minecraft:charcoal'})
    write(data / 'tags/item/conditioning_tools.json', {'values': ['jugcraft:oak_conditioning_insert']})
    press = list(pharmacy_factory.FORMULAS) + list(precision_pharmacy.PRESS) + [key for key in GRADES if key != 'moonshine']
    sterile = list(sterile_pharmacy.FORMULAS) + list(precision_pharmacy.STERILE)
    write(data / 'tags/item/pharmacy_formulas.json', {'values': ['jugcraft:' + key + '_formula_plate' for key in press]})
    write(data / 'tags/item/sterile_pharmacy_formulas.json', {'values': ['jugcraft:' + key + '_formula_plate' for key in sterile]})
    for key, info in GRADES.items():
        display, seconds, onset, strain, melee, knockback, spread, slow, recovery, redose = info
        lang['drug.jugcraft.' + key] = display
        lang['effect.jugcraft.' + key] = display
        lang['tooltip.jugcraft.' + key] = f'Melee damage +{melee:.0%}; knockback resistance +{knockback:.0%}. Intoxication: weapon spread +{spread - 1:.0%}, movement -{slow:.1%}.'
        if key != 'moonshine': lang['tooltip.jugcraft.' + key + '_formula_plate'] = 'Reusable beverage formula. Analyze matching concentrate; install in the Formulation Press.'
    for key in ITEMS:
        lang.setdefault('tooltip.jugcraft.' + key, 'Fictional beverage-process ingredient. See Alcohol and Spirits in the Chemistry handbook.')
    lang.update({'effect.jugcraft.intoxication': 'Intoxication', 'container.jugcraft.form.socket.conditioning': 'Conditioning insert',
        'container.jugcraft.form.heat': 'Fuel ticks', 'container.jugcraft.form.waiting_fuel': 'Waiting for fuel',
        'container.jugcraft.form.add_fuel': 'Add ordinary furnace fuel.', 'container.jugcraft.form.passive': 'Loaded processing',
        'container.jugcraft.tagline.mash_conditioning_barrel': 'Ferments mash and conditions reserve beverage stock without electricity.',
        'container.jugcraft.tagline.moonshine_still': 'Burns furnace fuel directly for small moonshine batches.'})
    for name in [*FORMS, 'empty_mash_jug', 'mash_jug_from_wheat', 'mash_jug_from_corn', 'beverage_culture_pack', 'oak_conditioning_insert']:
        write(data / 'advancement/recipes/alcohol_pharmacy' / (name + '.json'), {
            'fabric:load_conditions': conditions, 'parent': 'minecraft:recipes/root',
            'criteria': {'has_ingredient': {'trigger': 'minecraft:inventory_changed', 'conditions': {'items': [{'items': 'minecraft:sugar'}]}},
                'has_recipe': {'trigger': 'minecraft:recipe_unlocked', 'conditions': {'recipes': ['jugcraft:' + name]}}},
            'requirements': [['has_ingredient', 'has_recipe']], 'rewards': {'recipes': ['jugcraft:' + name]}})

def handbook_pages():
    return [{'title': FORMS[key]['display'], 'icon': 'jugcraft:' + key, 'text': text} for key, text in HANDBOOK.items()] + [
        {'title': 'Alcohol and Spirits', 'icon': 'jugcraft:moonshine', 'text': [
            'Craft two Empty Mash Jugs from five bricks. Four wheat or four corn, two sugar, a water bucket and an empty jug make one Mash Jug, returning the bucket. Ferment the jug in the Mash and Conditioning Barrel, then fuel the Moonshine Still with it and four empty bottles. Four Moonshine and the empty jug are returned once.',
            'All grades share one alcohol family and tolerance: Moonshine gives +10% melee damage/+20% knockback resistance for eight minutes at 20 strain; Refined Spirits +15%/+30% for twelve minutes at 30; Reserve Whiskey +20%/+40% for sixteen minutes at 40; High-Proof Spirits +25%/+50% for twenty minutes at 50. Their onset is ten seconds, except fifteen for High-Proof. Masterwork Reserve remains Batch 5.',
            'Intoxication raises supported gun spread by 15/20/25/30% and slows movement 5/7.5/10/12.5% respectively. Visual motion is not required. All grades may replace the alcohol family for a full new strain charge, a fresh onset and the shared repeat-duration penalty. Unused duration never accumulates. Recovery records do not block another dose.',
            'For steel-era supply use existing Mashing Station grain mash. An Industrial or Bulk Fermenter combines 1000 mB grain mash with one Beverage Culture Pack for 1000 mB Beverage Wash; no exhaust. The Chemical Distillation Station turns 1000 mB wash into 500 mB Refined Beverage Stock and 500 mB stillage. These are separate beverage jobs; industrial bioethanol is never drinkable.',
            'For efficient bulk Moonshine, the Formulation Press combines one Refined Spirits Concentrate, one sugar and four bottles into four Moonshine. It retains the Refined Spirits Formula Plate. Producing stronger Refined Spirits costs four concentrate for the same four bottles; bulk bottling does not silently upgrade Moonshine.',
            'The Pharmaceutical Separator uses two sugar and 1000 mB Refined Beverage Stock for eight Refined Spirits Concentrate. Reserve conditioning uses four concentrate and honeycomb in the rustic barrel with a reusable stainless/oak conditioning insert. The Separator turns four reserve concentrate plus another 500 mB beverage stock into four High-Proof Concentrate, with a reusable Precision Process Module in an upgrade slot.',
            'Analyze one matching concentrate with a blank plate and 250 mB water for its formula. High-Proof analysis also needs the precision module. The Formulation Press then bottles four concentrate, one sugar and four bottles into four matching drinks with the retained formula. Higher grades consume extra stock and processing; no infinite re-bottling upgrade loop. Drink returns one bottle.',
            'A new alcohol dose overlapping an active or pending opioid or Ketamine dose adds 25 combination strain. Ended doses do not add this charge. Existing food and cider retain their original behavior; every new consumable is self-administered. These are fixed Minecraft effects and recipes, without injury or illness management.'
        ]}]
