"""Batch 5 fixed Minecraft release profiles and premium beverages; no real formulation procedures."""
from starter_chemistry import form

XR = {
    'caffeine_xr': ('Caffeine XR', 'caffeine_concentrate', 24, 'release_matrix'),
    'methylphenidate_xr': ('Methylphenidate XR', 'methylphenidate_active', 4, 'release_matrix'),
    'amphetamine_xr': ('Amphetamine XR', 'amphetamine_active', 6, 'release_matrix'),
    'modafinil_xr': ('Modafinil XR', 'modafinil_active', 4, 'advanced_release_matrix'),
}
ITEMS = {
    'release_control_die': ('Release Control Die', 'general_chip_pattern'),
    'release_matrix_blend': ('Release Matrix Blend', 'flour'),
    'release_matrix': ('Release-control Matrix', 'flour'),
    'advanced_release_matrix': ('Advanced Release-control Matrix', 'silicone'),
    'empty_pharmacy_capsule': ('Empty Pharmacy Capsule', 'aspirin'),
    'pharmacy_patch_backing': ('Pharmacy Patch Backing', 'rubber'),
    'fentanyl_patch_stock': ('Unverified Fentanyl Patch Stock', 'aspirin'),
    'fentanyl_patch': ('Fentanyl Patch', 'aspirin'),
    'fentanyl_patch_formula_plate': ('Fentanyl Patch Formula Plate', 'general_chip_pattern'),
    'masterwork_blend': ('Masterwork Beverage Blend', 'glucose_syrup'),
    'masterwork_unconditioned_concentrate': ('Unconditioned Masterwork Concentrate', 'cola_syrup'),
    'masterwork_reserve_concentrate': ('Masterwork Reserve Concentrate', 'cola_syrup'),
    'masterwork_conditioning_insert': ('Masterwork Conditioning Insert', 'general_chip_pattern'),
    'masterwork_reserve_formula_plate': ('Masterwork Reserve Formula Plate', 'general_chip_pattern'),
    'masterwork_reserve': ('Alcohol (Masterwork Reserve)', 'stimulant'),
}
for key, (name, active, count, matrix) in XR.items():
    ITEMS.update({key: (name, 'aspirin'), key + '_core': (name + ' Coated Core', 'aspirin'),
                  key + '_formula_plate': (name + ' Formula Plate', 'general_chip_pattern')})
PRODUCTS = (*XR, 'fentanyl_patch', 'masterwork_reserve')
KEY = {'P': 'jugcraft:stainless_steel_plate', 'G': 'jugcraft:borosilicate_glass',
       'M': 'jugcraft:electric_motor', 'C': 'jugcraft:advanced_circuit', 'K': 'jugcraft:polymer_housing',
       'V': 'jugcraft:precision_stainless_valve', 'D': 'jugcraft:release_control_die'}
FORMS = {'release_coater': form('Release Coater', 'chemical_reactor', 3, 3, 3,
    ['release_water'], [], 'controlled_release', [
        ('power', 'ENERGY_IN', 0, 0, 0, 0, 'FRONT'),
        ('active_in', 'ITEM_IN', 0, 0, 0, 2, 'FRONT'),
        ('matrix_in', 'ITEM_IN', 1, 1, 0, 2, 'FRONT'),
        ('backing_in', 'ITEM_IN', 2, 2, 0, 2, 'FRONT'),
        ('cores_out', 'ITEM_OUT', 0, 2, 0, 0, 'FRONT'),
        ('water_in', 'FLUID_IN', 0, 0, 2, 0, 'BACK')],
    (['PGP', 'MCK', 'VDV'], KEY), items=(3, 1), sockets=[('release', 'jugcraft:release_coater_tools')])}
FORMS['release_coater']['features'] = ['machines', 'agriculture', 'nickel', 'silicon']
TANK_NAMES = {'release_water': 'Coating water'}

def recipe(name, cap, items, results, water=250, ticks=200, power=96, tool='release_control_die', fluids=None):
    result = dict(name='release_' + name, capability='jugcraft:' + cap, items=items, results=results,
        fluids=([('minecraft:water', water)] if water else []) if fluids is None else fluids,
        ticks=ticks, energy_per_tick=power, features=['machines', 'agriculture'])
    if tool: result['tool'] = 'jugcraft:' + tool
    return result

RECIPES = {'release_coater': [], 'laboratory_analyzer': [], 'formulation_press': [],
           'pharmaceutical_separator': [], 'mash_conditioning_barrel': []}
for key, (name, active, count, matrix) in XR.items():
    RECIPES['release_coater'].append(recipe('coat_' + key, 'controlled_release',
        [('jugcraft:' + active, count), ('jugcraft:' + matrix, 4)], [('jugcraft:' + key + '_core', 4)],
        water=500, ticks=300, power=128))
    RECIPES['laboratory_analyzer'].append(recipe('analyze_' + key, 'pharmacy_analysis',
        [('jugcraft:' + key + '_core', 1), ('jugcraft:blank_formula_plate', 1)],
        [('jugcraft:' + key + '_formula_plate', 1)], ticks=200))
    RECIPES['formulation_press'].append(recipe('finish_' + key, 'drug_formulation',
        [('jugcraft:' + key + '_core', 4), ('jugcraft:empty_pharmacy_capsule', 4)], [('jugcraft:' + key, 4)],
        tool=key + '_formula_plate', power=96))
RECIPES['release_coater'].append(recipe('coat_fentanyl_patch', 'controlled_release',
    [('jugcraft:fentanyl_active', 8), ('jugcraft:release_matrix', 4), ('jugcraft:pharmacy_patch_backing', 4)],
    [('jugcraft:fentanyl_patch_stock', 4)], water=500, ticks=300, power=128))
RECIPES['laboratory_analyzer'].append(recipe('analyze_fentanyl_patch', 'pharmacy_analysis',
    [('jugcraft:fentanyl_patch_stock', 1), ('jugcraft:blank_formula_plate', 1)],
    [('jugcraft:fentanyl_patch_formula_plate', 1)], ticks=200))
RECIPES['formulation_press'] += [
    recipe('matrix', 'drug_formulation', [('jugcraft:release_matrix_blend', 4)], [('jugcraft:release_matrix', 4)], water=500),
    recipe('advanced_matrix', 'drug_formulation', [('jugcraft:release_matrix', 4), ('jugcraft:silicone', 2), ('minecraft:amethyst_shard', 1)],
        [('jugcraft:advanced_release_matrix', 4)]),
    recipe('capsule_shells', 'drug_formulation', [('jugcraft:pharmaceutical_starch', 2), ('minecraft:paper', 2)],
        [('jugcraft:empty_pharmacy_capsule', 8)]),
    recipe('finish_fentanyl_patch', 'drug_formulation', [('jugcraft:fentanyl_patch_stock', 4), ('jugcraft:sterile_carrier', 1), ('minecraft:paper', 4)],
        [('jugcraft:fentanyl_patch', 4)], tool='fentanyl_patch_formula_plate'),
    recipe('bottle_masterwork', 'drug_formulation', [('jugcraft:masterwork_reserve_concentrate', 4), ('minecraft:sugar', 1), ('minecraft:glass_bottle', 4)],
        [('jugcraft:masterwork_reserve', 4)], tool='masterwork_reserve_formula_plate', water=0, ticks=160, power=128),
]
RECIPES['pharmaceutical_separator'].append(recipe('masterwork_stock', 'botanical_separation', [('jugcraft:masterwork_blend', 4)],
    [('jugcraft:masterwork_unconditioned_concentrate', 4)], fluids=[('jugcraft:refined_beverage_stock', 1000)],
    tool='precision_process_module', ticks=300, power=128))
RECIPES['mash_conditioning_barrel'].append(recipe('masterwork_conditioning', 'beverage_barrel',
    [('jugcraft:masterwork_unconditioned_concentrate', 4), ('minecraft:sweet_berries', 2)],
    [('jugcraft:masterwork_reserve_concentrate', 4)], water=0, ticks=4800, power=1, tool='masterwork_conditioning_insert'))
RECIPES['laboratory_analyzer'].append(recipe('analyze_masterwork', 'pharmacy_analysis',
    [('jugcraft:masterwork_reserve_concentrate', 1), ('jugcraft:blank_formula_plate', 1)],
    [('jugcraft:masterwork_reserve_formula_plate', 1)], ticks=200, tool='precision_process_module'))

CRAFTS = {
    'release_matrix_blend': (['jugcraft:pharmaceutical_starch'] * 4 + ['minecraft:paper'] * 4 + ['jugcraft:plastic_pellets'], 4),
    'pharmacy_patch_backing': (['#minecraft:wool', '#minecraft:wool', 'minecraft:paper', 'jugcraft:rubber'], 4),
    'masterwork_blend': (['jugcraft:high_proof_spirits_concentrate'] * 4 + ['minecraft:honeycomb'] * 2 + ['minecraft:gold_nugget', 'minecraft:sugar'], 4),
}

def write_extras(write, assets, data, lang, condition):
    conditions = condition('machines') + condition('agriculture')
    for key, (ingredients, count) in CRAFTS.items():
        write(data / 'recipe' / (key + '.json'), {'fabric:load_conditions': conditions, 'type': 'minecraft:crafting_shapeless',
            'category': 'misc', 'ingredients': ingredients, 'result': {'id': 'jugcraft:' + key, 'count': count}})
    for key, pattern, ingredients in (
        ('release_control_die', ['PAP', 'CMC', 'PSP'], {'P': 'jugcraft:stainless_steel_plate', 'A': 'minecraft:amethyst_shard',
            'C': 'jugcraft:advanced_circuit', 'M': 'jugcraft:precision_process_module', 'S': 'jugcraft:silicone'}),
        ('masterwork_conditioning_insert', ['PAP', 'COC', 'PSP'], {'P': 'jugcraft:stainless_steel_plate', 'A': 'minecraft:amethyst_shard',
            'C': 'jugcraft:advanced_circuit', 'O': 'jugcraft:oak_conditioning_insert', 'S': 'jugcraft:silicone'}),
    ):
        write(data / 'recipe' / (key + '.json'), {'fabric:load_conditions': conditions + condition('nickel') + condition('silicon'),
            'type': 'minecraft:crafting_shaped', 'category': 'misc', 'pattern': pattern, 'key': ingredients,
            'result': {'id': 'jugcraft:' + key}})
    # Extend the existing tool lists last, without changing any saved inventory layout.
    import pharmacy_factory, precision_pharmacy, alcohol_pharmacy
    press = [*pharmacy_factory.FORMULAS, *precision_pharmacy.PRESS, *list(alcohol_pharmacy.GRADES)[1:], *XR, 'fentanyl_patch', 'masterwork_reserve']
    write(data / 'tags/item/pharmacy_formulas.json', {'values': ['jugcraft:' + key + '_formula_plate' for key in press]})
    write(data / 'tags/item/conditioning_tools.json', {'values': ['jugcraft:oak_conditioning_insert', 'jugcraft:masterwork_conditioning_insert']})
    write(data / 'tags/item/release_coater_tools.json', {'values': ['jugcraft:release_control_die']})
    for key in PRODUCTS:
        lang['drug.jugcraft.' + key] = ITEMS[key][0]
        lang['effect.jugcraft.' + key] = ITEMS[key][0]
    for key in ITEMS:
        if key not in PRODUCTS: lang['tooltip.jugcraft.' + key] = 'Fixed game material for controlled release or premium beverage finishing. See its recipe and the pharmacy guide.'
    lang.update({
        'tooltip.jugcraft.release_control_die': 'Reusable release tool. Use the Coater release socket, or an Analyzer/Press upgrade slot. Locks during work.',
        'tooltip.jugcraft.caffeine_xr': 'Haste I and 10% faster walking for 30 min; 30 s onset. New doses replace their family for a full strain charge.',
        'tooltip.jugcraft.methylphenidate_xr': 'Haste II, 25% less aimed-gun hand sway and 25% tighter aimed firearm spread for 40 min; activity food cost +20%. Full-cost family replacement.',
        'tooltip.jugcraft.amphetamine_xr': 'Haste II and Speed I for 35 min; activity food cost +30%. Ends with 2 min Stimulant Fatigue. New doses replace their family for a full strain charge.',
        'tooltip.jugcraft.modafinil_xr': 'Haste I, 15% less aimed-gun hand sway, 15% less activity hunger and no phantom buildup while active for 60 min. Full-cost family replacement.',
        'tooltip.jugcraft.fentanyl_patch': 'Apply to yourself: 20% applicable damage reduction and 50% knockback resistance for 20 min. Movement/mining -10%. Naloxone reverses it. New doses replace their family for a full strain charge.',
        'tooltip.jugcraft.masterwork_reserve': '25 min of +30% melee damage and +60% knockback resistance. Spread +35%; movement -15%. Shared alcohol tolerance; replacement pays full new strain.',
        'container.jugcraft.form.socket.release': 'Release tool',
        'container.jugcraft.form.socket.precision': 'Process tool',
    })
    for name in [*CRAFTS, 'release_control_die', 'masterwork_conditioning_insert', 'release_coater']:
        write(data / 'advancement/recipes/controlled_release' / (name + '.json'), {'fabric:load_conditions': conditions,
            'parent': 'minecraft:recipes/root', 'criteria': {
                'has_ingredient': {'trigger': 'minecraft:inventory_changed', 'conditions': {'items': [{'items': 'jugcraft:pharmaceutical_starch'}]}},
                'has_recipe': {'trigger': 'minecraft:recipe_unlocked', 'conditions': {'recipes': ['jugcraft:' + name]}}},
            'requirements': [['has_ingredient', 'has_recipe']], 'rewards': {'recipes': ['jugcraft:' + name]}})

HANDBOOK = {'release_coater': [
    'A 3x3x3 advanced formed processor on the existing power, fluid, ports, escrow and recipe system. Three solid inlets, a rear water inlet and a coated-stock outlet. Its release socket keeps a Release Control Die; normal upgrades remain separate.',
    'Coat fixed named formulations only. Four matrices and 500 mB water accompany 24 Caffeine Concentrate, four Verified Methylphenidate Active, six Verified Amphetamine Active or four Verified Modafinil Active, producing four matching cores in 300 ticks at 128 JE/t. Modafinil uses Advanced Release Matrix.',
    'Eight Verified Fentanyl Active, four matrices, four patch backings and 500 mB water instead produce four unverified patch stock. No finished vial is multiplied into patches. Missing tools or output room spend nothing. Cancellation refunds solids and process water; breaking drops original solids/tooling and loses loose fluids as other processors do. Saved work resumes normally.',
]}

def handbook_pages():
    return [{'title': 'Controlled-release Materials', 'icon': 'jugcraft:release_control_die', 'text': [
        'Craft a Release Control Die from four stainless plates, amethyst, two Advanced Circuits, a Precision Process Module and Silicone Rubber. It is reusable: put it in the Coater release socket or an existing Analyzer/Press upgrade slot. It adds no speed or efficiency bonus and locks during a job.',
        'Four Pharmaceutical Starch, four paper and one Plastic Pellets craft four Release Matrix Blend. The Press turns four blend and 500 mB water into four Release-control Matrix. Four matrix, two Silicone Rubber and amethyst with 250 mB water make four Advanced Release-control Matrix. These jobs use the release die, 200 ticks and 96 JE/t.',
        'The same Press makes eight Empty Pharmacy Capsules from two starch, two paper and 250 mB water with the die. Two wool, paper and rubber craft four Patch Backing. Earlier crops, coffee and plastic production remain useful; these are fictional game materials.',
    ]}, {'title': 'Extended-release Pharmacy', 'icon': 'jugcraft:caffeine_xr', 'text': [
        'Coat stock in the Release Coater, then analyze one matching core/patch stock with a Blank Formula Plate and 250 mB water. Install the release die in an Analyzer upgrade slot. It takes 200 ticks at 96 JE/t and yields a reusable formula without needing the finished medicine.',
        'The Press combines four matching cores, four empty capsules and 250 mB water into four XR capsules. For patches, four stock, one Sterile Carrier, four paper and 250 mB water produce four patches. Matching formulas stay in its ordinary formula socket; each job takes 200 ticks at 96 JE/t.',
        'All XR stimulants begin after 30 s and allow full-cost replacement with shared tolerance. Caffeine XR: Haste I, +10% walking, 30 min, 10 strain. Methylphenidate XR: Haste II, 25% less aimed-gun hand sway and 25% tighter aimed firearm spread, +20% activity food cost, 40 min, 30 strain. Amphetamine XR: Haste II, Speed I, +30% activity food cost, 35 min, 40 strain, then 2 min Stimulant Fatigue. Modafinil XR: Haste I, 15% less supported hand sway, 15% less activity hunger and a native phantom rest counter held at zero while active, 60 min, 25 strain. No forced sleep or night vision.',
        'All four retain the existing stimulant family, tolerance and 120-second recovery. Fentanyl Patch begins after 30 s: 20% applicable damage reduction and +50% knockback resistance for 20 min at 40 strain, with -10% movement/mining. It shares opioid tolerance and Naloxone reversal. New opioid doses replace it for a full strain charge; recovery does not lock use. Applying a patch consumes no armor slot and returns no bottle. No injury or skin system is added.',
    ]}, {'title': 'Masterwork Reserve', 'icon': 'jugcraft:masterwork_reserve', 'text': [
        'This beverage uses no release coating. Craft four Masterwork Blend from four High-Proof Spirits Concentrate, two honeycomb, a gold nugget and sugar. The Pharmaceutical Separator uses four blend and an extra 1000 mB Refined Beverage Stock to make four unconditioned concentrate, retaining a Precision Process Module; 300 ticks at 128 JE/t.',
        'Craft a Masterwork Conditioning Insert from four stainless plates, amethyst, two Advanced Circuits, the existing Oak Conditioning Insert and Silicone Rubber. Install it in the rustic barrel conditioning socket. Four unconditioned concentrate and two sweet berries condition for 4800 loaded ticks into four Masterwork Reserve Concentrate. No offline progress or medical Coater is involved.',
        'Analyze one concentrate with a blank plate and 250 mB water, keeping the precision module, for its formula: 200 ticks at 96 JE/t. The Press uses four concentrate, sugar and four bottles to make four Masterwork Reserve, keeping that formula: 160 ticks at 128 JE/t.',
        'After 20 s onset, Masterwork Reserve gives +30% melee damage and +60% knockback resistance for 25 min at 60 strain. Intoxication: +35% supported gun spread and -15% movement. It shares the existing alcohol family/tolerance, adds 25 extra strain when overlapping opioids or Ketamine, and allows full-cost replacement with a fresh onset. Existing cider is unchanged.',
    ]}]
