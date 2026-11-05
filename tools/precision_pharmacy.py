"""Batch 4 precision drugs: authored Minecraft recipes, not real synthesis."""
FORMULAS = {
    'methylphenidate': ('Methylphenidate', 'minecraft:lapis_lazuli', 'minecraft:redstone'),
    'amphetamine': ('Amphetamine', 'minecraft:blaze_powder', 'minecraft:sugar'),
    'modafinil': ('Modafinil', 'minecraft:quartz', 'minecraft:glowstone_dust'),
    'ketamine': ('Ketamine', 'minecraft:amethyst_shard', 'minecraft:ender_pearl'),
    'fentanyl': ('Fentanyl', 'minecraft:quartz', 'minecraft:redstone'),
}
PRESS = ('methylphenidate', 'amphetamine', 'modafinil')
STERILE = ('ketamine', 'fentanyl')
ITEMS = {'precision_process_module': ('Precision Process Module', 'general_chip_pattern')}
for key, (display, first, second) in FORMULAS.items():
    ITEMS.update({key: (display, 'aspirin' if key in PRESS else 'stimulant'),
        key + '_reagent_blend': (display + ' Game Reagent Blend', 'salicylic_acid'),
        key + '_stock': (display + ' Game Stock', 'salicylic_acid'),
        key + '_active': ('Verified ' + display + ' Active', 'salicylic_acid'),
        key + '_formula_plate': (display + ' Formula Plate', 'general_chip_pattern')})
FLUIDS = {'organic_process_stock': {'display': 'Organic Process Stock', 'feature': 'agriculture',
    'colors': [(61, 43, 28), (112, 82, 49), (158, 121, 72), (215, 181, 121)],
    'tick_delay': 5, 'slope': 4, 'drop_off': 1}}
REACTOR_RECIPES = [dict(name='precision_organic_stock', items=[('jugcraft:pharmaceutical_base', 4), ('minecraft:oak_log', 1)],
    fluids=[('minecraft:water', 500)], fluid_results=[('jugcraft:organic_process_stock', 500)], ticks=200,
    features=['machines', 'agriculture'])]

def recipe(name, cap, items, results, fluids=(('minecraft:water', 250),), ticks=200, power=80, tool='jugcraft:precision_process_module'):
    return dict(name='precision_' + name, capability='jugcraft:' + cap, items=items, results=results,
        fluids=list(fluids), ticks=ticks, energy_per_tick=power, tool=tool, features=['machines', 'agriculture'])

RECIPES = {'bulk_chemical_reactor': [], 'pharmaceutical_separator': [], 'laboratory_analyzer': [],
    'formulation_press': [], 'sterile_filler': []}
for key in FORMULAS:
    RECIPES['bulk_chemical_reactor'].append(recipe('prepare_' + key, 'precision_preparation',
        [('jugcraft:' + key + '_reagent_blend', 1)], [('jugcraft:' + key + '_stock', 4)],
        fluids=[('jugcraft:organic_process_stock', 500)], ticks=240, power=96))
    RECIPES['pharmaceutical_separator'].append(recipe('verify_' + key, 'botanical_separation',
        [('jugcraft:' + key + '_stock', 4)], [('jugcraft:' + key + '_active', 4)], power=64))
    RECIPES['laboratory_analyzer'].append(recipe('analyze_' + key, 'pharmacy_analysis',
        [('jugcraft:' + key + '_active', 1), ('jugcraft:blank_formula_plate', 1)],
        [('jugcraft:' + key + '_formula_plate', 1)], ticks=160, power=48))
    machine = 'formulation_press' if key in PRESS else 'sterile_filler'
    items = [('jugcraft:' + key + '_active', 2), ('jugcraft:pharmaceutical_starch' if key in PRESS else 'jugcraft:sterile_carrier', 1)]
    if key in STERILE: items.append(('jugcraft:sterile_vial', 4))
    RECIPES[machine].append(recipe('fill_' + key, 'drug_formulation' if key in PRESS else 'sterile_filling',
        items, [('jugcraft:' + key, 4)], power=96, tool='jugcraft:' + key + '_formula_plate'))

def write_extras(write, assets, data, lang, condition):
    conditions = condition('machines') + condition('agriculture')
    for key, (display, first, second) in FORMULAS.items():
        write(data / 'recipe' / (key + '_reagent_blend.json'), {'fabric:load_conditions': conditions,
            'type': 'minecraft:crafting_shapeless', 'category': 'misc', 'ingredients': [first, second, 'jugcraft:pharmaceutical_base'],
            'result': {'id': 'jugcraft:' + key + '_reagent_blend'}})
        lang['drug.jugcraft.' + key] = display
        lang['effect.jugcraft.' + key] = display
        for suffix in ('reagent_blend', 'stock', 'active'):
            lang['tooltip.jugcraft.' + key + '_' + suffix] = 'Fictional precision-pharmacy ingredient. Use the matching shared machine job and reusable Precision Process Module.'
        lang['tooltip.jugcraft.' + key + '_formula_plate'] = 'Reusable ' + display + ' formula. Analyze verified active stock before first manufacture.'
    write(data / 'recipe/precision_process_module.json', {'fabric:load_conditions': conditions + condition('nickel') + condition('silicon'),
        'type': 'minecraft:crafting_shaped', 'category': 'misc', 'pattern': ['PQP', 'ACA', 'PVP'],
        'key': {'P': 'jugcraft:stainless_steel_plate', 'Q': 'minecraft:quartz', 'A': 'minecraft:amethyst_shard',
            'C': 'jugcraft:advanced_circuit', 'V': 'jugcraft:precision_stainless_valve'},
        'result': {'id': 'jugcraft:precision_process_module'}})
    lang.update({
        'tooltip.jugcraft.precision_process_module': 'Reusable precision tool. Install in a formed processor upgrade slot; locks while its job runs.',
        'container.jugcraft.form.socket.precision': 'Precision module',
        'tooltip.jugcraft.methylphenidate': 'Haste II, 25% less aimed-gun hand sway and 25% tighter aimed firearm spread; activity food cost +20%.',
        'tooltip.jugcraft.amphetamine': 'Haste III and Speed II; activity food cost +50%. Ends with two minutes of Stimulant Fatigue.',
        'tooltip.jugcraft.modafinil': 'Haste I, 15% less aimed-gun hand sway and 15% less activity hunger. Holds the phantom rest counter at zero while active.',
        'tooltip.jugcraft.ketamine': 'Echo View: begin while grounded and scout within 24 blocks for up to 30 s. Follow the body-direction HUD. Body stays vulnerable. Sneak or open a menu to return.',
        'tooltip.jugcraft.fentanyl': '40% applicable damage reduction and 75% knockback resistance. Opioid Drowsiness slows movement/mining 20%.',
        'effect.jugcraft.precision_focus': 'Precision Focus', 'effect.jugcraft.adrenaline_fatigue': 'Stimulant Fatigue',
        'message.jugcraft.drug.incompatible': 'Overlapping alcohol, opioids or Ketamine adds 25 combination strain per new dose.',
        'message.jugcraft.echo_view': 'Echo View · 24-block range · Sneak to return · Your body remains behind',
        'message.jugcraft.echo_unavailable': 'Stand on solid ground, dismount, close menus and return from any other remote view before starting Echo View.',
        'message.jugcraft.drug.wait': 'This drug family is still taking effect, active or recovering. Check this formulation’s dosing rules.',
    })
    for name in ['precision_process_module'] + [key + '_reagent_blend' for key in FORMULAS]:
        flags = conditions + (condition('nickel') + condition('silicon') if name == 'precision_process_module' else [])
        write(data / 'advancement/recipes/precision_pharmacy' / (name + '.json'), {
            'fabric:load_conditions': flags, 'parent': 'minecraft:recipes/root',
            'criteria': {'has_ingredient': {'trigger': 'minecraft:inventory_changed', 'conditions': {'items': [{'items': 'jugcraft:pharmaceutical_base'}]}},
                'has_recipe': {'trigger': 'minecraft:recipe_unlocked', 'conditions': {'recipes': ['jugcraft:' + name]}}},
            'requirements': [['has_ingredient', 'has_recipe']], 'rewards': {'recipes': ['jugcraft:' + name]}})

def handbook_pages():
    return [{'title': 'Precision Pharmacy', 'icon': 'jugcraft:precision_process_module', 'text': [
        'Craft a reusable Precision Process Module from existing stainless plates, advanced circuit, amethyst, quartz and a precision stainless valve. No precision medicine is required. Install the module in an existing formed processor upgrade slot; input/output slots and old saved inventories stay in place.',
        'Four Pharmaceutical Base, an oak log and 500 mB water make 500 mB Organic Process Stock in the compact Chemical Reactor. Each reagent blend combines one Pharmaceutical Base with two Minecraft ingredients: lapis + redstone for Methylphenidate; blaze powder + sugar for Amphetamine; quartz + glowstone dust for Modafinil; amethyst + ender pearl for Ketamine; quartz + redstone for Fentanyl. Every formulation is fictional.',
        'The existing Bulk Chemical Reactor processes one matching blend and 500 mB Organic Process Stock into four stock, using its liquid inlet and reusable precision module. Blends fit its existing one-solid-input layout. The gas tank remains empty.',
        'The Pharmaceutical Separator processes four stock and 250 mB water into four verified active, using the precision module. Analyze one active with a blank plate and 250 mB water, also with the module, to write its reusable formula. A finished medicine is never needed to unlock its own production.',
        'The Formulation Press makes four tablets from two matching active, one starch and 250 mB water with the formula. Ketamine and Fentanyl instead use the Sterile Filler: two active, one sterile carrier, four sterile vials and 250 mB water make four medicine vials. All formulas/modules are retained. Clear incompatible fluids and use the normal recipe picker.'
    ]}, {'title': 'Precision Drug Effects', 'icon': 'jugcraft:methylphenidate', 'text': [
        'Methylphenidate: 20 minutes, 20-second onset, 35 strain. Haste II, 25% less aimed-gun hand sway and 25% tighter aimed firearm spread; activity exhaustion costs 20% more. Hip fire is unchanged. Amphetamine: 15 minutes, 20-second onset, 55 strain. Haste III and Speed II; activity exhaustion costs 50% more and natural expiry gives two minutes of Stimulant Fatigue. Modafinil: 40 minutes, 20-second onset, 25 strain. Haste I, 15% less aimed-gun hand sway and 15% less activity hunger. It holds the native phantom rest counter at zero while active; it does not force sleep or grant night vision.',
        'These three share caffeine’s stimulant family, full-cost family replacement, 75%/50% repeat duration and 30-online-minute tolerance. Replacing caffeine or Stimulant retains that tolerance and restarts onset. Methylphenidate tightens supported aimed firearm errors after the strongest strain/drug spread drawback; flame coverage and arc acquisition cones stay unchanged. Modafinil and Salts use the better activity discount rather than multiplying.',
        'Fentanyl: eight minutes, five-second onset, 55 strain. 40% applicable damage reduction and 75% knockback resistance, with movement/mining 20% slower. Shares Morphine’s opioid family/tolerance; full-cost replacement and decaying strain. Existing Naloxone cancels the opioid benefit and Drowsiness without freeing strain or tolerance. Charcoal does not remove opioid Drowsiness. Stronger native potion protection wins without multiplying defenses.',
        'Ketamine: up to 30 seconds immediately, 40 strain. Begin while grounded, unmounted and with no open menu or other remote view. Echo View scouts within 24 blocks through ordinary open space. The body HUD shows its relative direction, distance and height even with cosmetics off. The body stays put and vulnerable; damage, cancellation, cleansing, menus, logout, death or dimension changes end the view. Normal solid walls and hidden ores stay hidden, and the view cannot attack, use blocks or move inventory. A new dose pays its full strain cost. Cancellation ends the view without erasing strain or tolerance.',
        'Overlapping a different alcohol, opioid or Ketamine family adds 25 strain per new dose. Ended doses do not create a surcharge or block use. New effects never create injury, infection or combat-triggered ailments. Fentanyl patches and Masterwork Reserve remain Batch 5.'
    ]}]
