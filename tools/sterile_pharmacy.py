"""Sterile pharmacy: fictional Minecraft formulations, never real laboratory procedures."""
from starter_chemistry import form

FORMULAS = {
    'penicillin': ('Penicillin', 'antibiotic_extract'),
    'lidocaine': ('Lidocaine', 'lidocaine_stock'),
    'adrenaline': ('Adrenaline', 'adrenaline_stock'),
    'salbutamol': ('Salbutamol (Albuterol)', 'salbutamol_stock'),
    'morphine': ('Morphine', 'redleaf_medical_stock'),
    'naloxone': ('Naloxone', 'naloxone_stock'),
}
ITEMS = {
    'sterile_vial': ('Sterile Vial', 'glucose_syrup'),
    'sterile_carrier': ('Sterile Carrier', 'flour'),
    'azure_culture_starter': ('Azure Culture Starter', 'salicylic_acid'),
    'culture_feed': ('Prepared Culture Feed', 'flour'),
    'antibiotic_extract': ('Antibiotic Extract', 'salicylic_acid'),
    'lidocaine_stock': ('Lidocaine Game Stock', 'salicylic_acid'),
    'adrenaline_stock': ('Adrenaline Game Stock', 'salicylic_acid'),
    'salbutamol_stock': ('Salbutamol Game Stock', 'salicylic_acid'),
    'redleaf_medical_stock': ('Redleaf Medical Stock', 'salicylic_acid'),
    'naloxone_stock': ('Naloxone Game Stock', 'salicylic_acid'),
    **{key: (name, 'stimulant') for key, (name, stock) in FORMULAS.items()},
    **{key + '_formula_plate': (name + ' Formula Plate', 'general_chip_pattern')
       for key, (name, stock) in FORMULAS.items()},
}
FLUIDS = {'antibiotic_broth': {'display': 'Azure Antibiotic Broth', 'feature': 'agriculture',
    'colors': [(24, 60, 76), (54, 109, 127), (91, 157, 172), (170, 214, 221)],
    'tick_delay': 5, 'slope': 4, 'drop_off': 1}}
KEY = {'P': 'jugcraft:stainless_steel_plate', 'M': 'jugcraft:electric_motor',
       'C': 'jugcraft:basic_circuit', 'K': 'jugcraft:machine_casing', 'W': 'jugcraft:copper_cable',
       'G': 'jugcraft:borosilicate_glass'}
FORMS = {
    'autoclave': form('Autoclave', 'chemical_reactor', 2, 3, 2,
        ['sterilizing_water'], [], 'sterile_preparation', [
            ('power', 'ENERGY_IN', 0, 0, 0, 0, 'FRONT'),
            ('load_in', 'ITEM_IN', 0, 0, 0, 1, 'FRONT'),
            ('sterile_out', 'ITEM_OUT', 0, 1, 0, 0, 'FRONT'),
            ('water_in', 'FLUID_IN', 0, 0, 2, 0, 'BACK')],
        (['PGP', 'MCK', 'PWP'], KEY), items=(1, 1)),
    'sterile_filler': form('Sterile Filler', 'chemical_reactor', 2, 2, 3,
        ['filling_water'], [], 'sterile_filling', [
            ('power', 'ENERGY_IN', 0, 0, 0, 0, 'FRONT'),
            ('stock_in', 'ITEM_IN', 0, 0, 0, 2, 'FRONT'),
            ('carrier_in', 'ITEM_IN', 1, 1, 0, 2, 'FRONT'),
            ('vial_in', 'ITEM_IN', 2, 1, 1, 1, 'RIGHT'),
            ('medicine_out', 'ITEM_OUT', 0, 1, 0, 0, 'FRONT'),
            ('water_in', 'FLUID_IN', 0, 0, 1, 0, 'BACK')],
        (['PGP', 'MCK', 'PWT'], {**KEY, 'T': 'minecraft:piston'}), items=(3, 1),
        sockets=[('formula', 'jugcraft:sterile_pharmacy_formulas')]),
}
for info in FORMS.values():
    info['features'] = ['machines', 'agriculture', 'silicon', 'nickel']

def recipe(name, capability, items, results=(), fluids=(('minecraft:water', 250),), ticks=200, power=64, **extra):
    return dict(name='sterile_' + name, capability='jugcraft:' + capability,
        items=items, results=list(results), fluids=list(fluids), ticks=ticks,
        energy_per_tick=power, features=['machines', 'agriculture'], **extra)

RECIPES = {
    'autoclave': [recipe('vials', 'sterile_preparation', [('minecraft:glass_bottle', 8)],
        [('jugcraft:sterile_vial', 8)], fluids=[('minecraft:water', 500)], power=80),
        recipe('carrier', 'sterile_preparation', [('jugcraft:pharmaceutical_starch', 8)],
        [('jugcraft:sterile_carrier', 8)], fluids=[('minecraft:water', 500)], power=80)],
    'sterile_filler': [recipe('fill_' + key, 'sterile_filling',
        [('jugcraft:' + stock, 2), ('jugcraft:sterile_carrier', 1), ('jugcraft:sterile_vial', 4)],
        [('jugcraft:' + key, 4)], tool='jugcraft:' + key + '_formula_plate', power=96)
        for key, (name, stock) in FORMULAS.items()],
    'laboratory_analyzer': [recipe('analyze_' + key, 'pharmacy_analysis',
        [('jugcraft:' + stock, 1), ('jugcraft:blank_formula_plate', 1)],
        [('jugcraft:' + key + '_formula_plate', 1)], ticks=160, power=32)
        for key, (name, stock) in FORMULAS.items()],
    'pharmaceutical_separator': [recipe('antibiotic_extract', 'botanical_separation', [],
        [('jugcraft:antibiotic_extract', 4)], fluids=[('jugcraft:antibiotic_broth', 500)], power=48)],
    'industrial_fermenter': [recipe('azure_culture', 'sterile_culture', [('jugcraft:culture_feed', 1)],
        fluids=[('minecraft:water', 500)], fluid_results=[('jugcraft:antibiotic_broth', 500, 0)], ticks=400, power=48)],
}
STOCKS = {
    'culture_feed': [('jugcraft:sterile_carrier', 2), ('jugcraft:azure_culture_starter', 1)],
    'lidocaine_stock': [('jugcraft:sterile_carrier', 2), ('minecraft:quartz', 1)],
    'adrenaline_stock': [('jugcraft:pharmaceutical_base', 2), ('minecraft:redstone', 1)],
    'salbutamol_stock': [('minecraft:kelp', 4), ('minecraft:lapis_lazuli', 1)],
    'redleaf_medical_stock': [('minecraft:beetroot', 4), ('minecraft:amethyst_shard', 1)],
    'naloxone_stock': [('jugcraft:pharmaceutical_base', 2), ('minecraft:quartz', 1)],
}
REACTOR_RECIPES = [dict(name='sterile_' + key, items=items, fluids=[('minecraft:water', 250)],
    results=[('jugcraft:' + key, 4)], ticks=160, features=['machines', 'agriculture'])
    for key, items in STOCKS.items()]
TANK_NAMES = {'sterilizing_water': 'Sterilizing water', 'filling_water': 'Filling water',
              'extraction_water': 'Extraction water / antibiotic broth'}
HANDBOOK = {
    'autoclave': ['Eight glass bottles and 500 mB water make eight Sterile Vials. Eight Pharmaceutical Starch and 500 mB water make eight Sterile Carrier. Each batch takes 200 ticks at 80 JE/t. Sterile items are stable; no contamination or maintenance timers.',
        'Load at the upper front-left, collect at lower front-right. Power is lower front-left; water is at the rear. Build with four stainless steel plates, borosilicate glass, a motor, basic circuit, casing and copper cable.'],
    'sterile_filler': ['Install the matching reusable sterile Formula Plate. Two prepared stock, one Sterile Carrier, four Sterile Vials and 250 mB water make four medicines in 200 ticks at 96 JE/t. The plate stays installed. Vials also serve as internal cartridges in injectors, inhalers and sprays; using a medicine returns one ordinary empty glass container.',
        'Stock and carrier enter at the upper front; vials enter at the marked right side. Medicine leaves at lower front-right. Power is lower front-left and water at the rear. The process conditions its own water; no additional sterile-fluid chore. Construction replaces one of the Autoclave’s four plates with a piston.',
        'Bootstrap each formula by analyzing its prepared stock with a Blank Formula Plate and 250 mB water in the existing Laboratory Analyzer. A finished medicine is never required to unlock its own manufacture.'],
}

def write_extras(write, assets, data, lang, condition):
    conditions = condition('machines') + condition('agriculture')
    write(data / 'tags/item/sterile_pharmacy_formulas.json',
          {'values': ['jugcraft:' + key + '_formula_plate' for key in FORMULAS]})
    write(data / 'recipe/azure_culture_starter.json', {'fabric:load_conditions': conditions,
        'type': 'minecraft:crafting_shapeless', 'category': 'misc',
        'ingredients': ['minecraft:blue_dye', 'minecraft:brown_mushroom', 'minecraft:sugar'],
        'result': {'id': 'jugcraft:azure_culture_starter', 'count': 2}})
    for key, (name, stock) in FORMULAS.items():
        lang['drug.jugcraft.' + key] = name
        lang['tooltip.jugcraft.' + key + '_formula_plate'] = f'Reusable {name} formula. Analyze prepared stock; install in the Sterile Filler.'
    lang.update({
        'tooltip.jugcraft.blank_formula_plate': 'Analyze a medicine sample or sterile stock to write a reusable manufacturing formula.',
        'tooltip.jugcraft.penicillin': 'Recovery II: 50% more natural healing, paying proportional food cost. Shares the Paracetamol family.',
        'tooltip.jugcraft.lidocaine': 'Four absorption hearts once per dose and 25% knockback resistance. Absorption potions take priority.',
        'tooltip.jugcraft.adrenaline': 'Speed II and Strength II for 45 s, then 60 s of Fatigue. New doses pay full strain and keep family tolerance.',
        'tooltip.jugcraft.salbutamol': '50% less sprint exhaustion and 15% more water movement acceleration. Also known as Albuterol.',
        'tooltip.jugcraft.morphine': '20% damage reduction and 50% knockback resistance; Drowsiness slows movement and mining 15%. Resistance takes priority.',
        'tooltip.jugcraft.naloxone': 'Ends your opioid benefit and Drowsiness. Keeps other drugs, strain and tolerance. 30 s cooldown.',
        'tooltip.jugcraft.sterile_vial': 'Stable sterile packaging. Filling consumes one vial per medicine; drinking returns an ordinary glass bottle.',
        'tooltip.jugcraft.sterile_carrier': 'Stable fictional carrier prepared in the Autoclave.',
        'tooltip.jugcraft.azure_culture_starter': 'A fictional azure culture, crafted from blue dye, mushroom and sugar.',
        'tooltip.jugcraft.culture_feed': 'Ferment with water in an Industrial or Bulk Fermenter to make Azure Antibiotic Broth.',
        'tooltip.jugcraft.antibiotic_extract': 'Separate Azure Antibiotic Broth in the Pharmaceutical Separator; analyze and fill for Penicillin.',
        'tooltip.jugcraft.drug.no_redose': 'New doses replace their family and pay full strain. Repeat duration is 75%, then 50%.',
        'effect.jugcraft.sterile_recovery': 'Recovery II', 'effect.jugcraft.lidocaine': 'Lidocaine',
        'effect.jugcraft.adrenaline': 'Adrenaline', 'effect.jugcraft.salbutamol': 'Salbutamol',
        'effect.jugcraft.morphine': 'Morphine', 'effect.jugcraft.pharmacy_absorption': 'Lidocaine Shield',
        'effect.jugcraft.adrenaline_fatigue': 'Adrenaline Fatigue', 'effect.jugcraft.opioid_drowsiness': 'Opioid Drowsiness',
        'tooltip.jugcraft.activated_charcoal': 'Clears Caffeine Jitters, Drug Appetite and Adrenaline Fatigue. Keeps strain and tolerance. 60 s cooldown.',
    })
    for stock in STOCKS:
        lang['tooltip.jugcraft.' + stock] = 'Fictional Minecraft process ingredient. See the Chemistry handbook for its fixed formulation.'
    for key, ingredient in [(key, 'jugcraft:basic_circuit') for key in FORMS] + [
        ('azure_culture_starter', 'minecraft:blue_dye')]:
        recipe_conditions = conditions + (condition('silicon') + condition('nickel') if key in FORMS else [])
        write(data / 'advancement/recipes/sterile_pharmacy' / (key + '.json'), {
            'fabric:load_conditions': recipe_conditions, 'parent': 'minecraft:recipes/root',
            'criteria': {'has_ingredient': {'trigger': 'minecraft:inventory_changed',
                'conditions': {'items': [{'items': ingredient}]}},
                'has_the_recipe': {'trigger': 'minecraft:recipe_unlocked', 'conditions': {'recipes': ['jugcraft:' + key]}}},
            'requirements': [['has_ingredient', 'has_the_recipe']], 'rewards': {'recipes': ['jugcraft:' + key]}})

def handbook_pages():
    return [{'title': FORMS[key]['display'], 'icon': 'jugcraft:' + key, 'text': text} for key, text in HANDBOOK.items()] + [
        {'title': 'Cultures and Sterile Stocks', 'icon': 'jugcraft:azure_culture_starter', 'text': [
            'All chains here are fictional game recipes. Blue dye, brown mushroom and sugar make two Azure Culture Starter. The Chemical Reactor combines two Sterile Carrier, one starter and 250 mB water into four Culture Feed.',
            'An Industrial or Bulk Fermenter turns one Culture Feed and 500 mB water into 500 mB Azure Antibiotic Broth in 400 ticks at 48 JE/t. Only the product outlet is used. The Pharmaceutical Separator consumes 500 mB broth for four Antibiotic Extract in 200 ticks at 48 JE/t. Analyze extract for the Penicillin formula.',
            'Each other stock recipe uses the Chemical Reactor with 250 mB water for four stock in 160 ticks: two Sterile Carrier + quartz for Lidocaine; two Pharmaceutical Base + redstone for Adrenaline; four kelp + lapis for Salbutamol; four beetroot + amethyst shard for Redleaf Medical Stock (Morphine); two Pharmaceutical Base + quartz for Naloxone.',
            'Analyze one stock with a blank plate to obtain the corresponding formula, then fill four medicines per batch. Tradeable plates work without a personal research lock. Sterile equipment needs the existing stainless steel and glass industries; no medicine is required to build it.'
        ]}, {'title': 'Sterile Medicine Effects', 'icon': 'jugcraft:penicillin', 'text': [
            'Penicillin: 20 minutes, 5-second onset, 15 strain. Recovery II gives 50% more natural healing with proportional food cost. It shares Paracetamol’s family. No infections or injury conditions are added.',
            'Lidocaine: 10 minutes, 5-second onset, 10 strain. Grants four absorption hearts once per dose and 25% knockback resistance. A spent shield never refills each second. Vanilla Absorption takes priority.',
            'Adrenaline: 45 seconds immediately, 35 strain. Speed II and Strength II, followed by 60 seconds of Fatigue: 10% slower movement and 15% slower mining. A new dose replaces the old benefit for a full strain charge. Strain decays independently of recovery. Charcoal clears Fatigue without resetting recovery.',
            'Salbutamol (Albuterol): 15 minutes immediately, 15 strain. Halves ground-sprint exhaustion and improves water movement acceleration by 15%. Salts and sprint savings use the better discount. No water breathing or discount on combat, mining or natural healing.',
            'Morphine: 12 minutes, 5-second onset, 35 strain. 20% damage reduction (Resistance takes priority) and 50% knockback resistance. Its Drowsiness slows movement and mining by 15%; charcoal does not clear it.',
            'Naloxone: self-administered utility, 30-second cooldown. Ends opioid benefits and Drowsiness while preserving their recovery, strain and tolerance. Other drug families and unrelated potion effects remain. Milk and death end drug benefits without freeing accounting.',
            'All timed formulations permit full-cost replacement; recovery no longer locks their family. Repeat duration falls to 75%, then 50%. Leaving pauses online timers. No combat-triggered debuffs or new medical upkeep are introduced.'
        ]}]
