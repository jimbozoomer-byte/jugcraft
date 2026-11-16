"""Batch 6: fictional Minecraft botanical/perception recipes, never real synthesis."""
PRODUCTS = {'thc': 'THC', 'psilocybin': 'Psilocybin', 'mescaline': 'Mescaline', 'lsd': 'LSD', 'dmt': 'DMT'}
CROPS = {'hemp': ('Hemp', 'botanical_resin'), 'dreamcap': ('Dreamcap', 'dreamcap'),
         'prism_cactus': ('Prism Cactus', 'prism_cactus_fruit')}
BLOCKS = ['dream_study_plinth', *(key + '_crop' for key in CROPS)]
ITEMS = {
    'botanical_resin': ('Botanical Resin', 'dragonblood_resin'),
    'dreamcap': ('Dreamcap', 'jimtafacts/civil/mushroom'),
    'prism_cactus_fruit': ('Prism Cactus Fruit', 'dreamglass'),
    'dream_concentrate': ('Dream Concentrate', 'dreamglass'),
    'prismatic_concentrate': ('Prismatic Concentrate', 'dreamglass'),
    'perception_substrate': ('Perception Substrate', 'dreamglass'),
    'oneiric_stock': ('Oneiric Stock', 'dreamglass'),
    'mycelium_catalyst': ('Mycelium Catalyst', 'jimtafacts/civil/mushroom'),
    'botanical_capsule_shell': ('Botanical Capsule Shell', 'aspirin'),
    'study_canvas': ('Dream Study Canvas', 'general_chip_pattern'),
    'lucid_imprint': ('Lucid Imprint', 'dreamglass'),
    'pattern_imprint': ('Pattern Imprint', 'dreamglass'),
    'oneiric_imprint': ('Oneiric Imprint', 'dreamglass'),
    'dream_folio': ('Dream Folio', 'general_chip_pattern'),
    'dream_study_plinth': ('Dream Study Plinth', 'dreamglass'),
}
for key, (name, produce) in CROPS.items():
    ITEMS[key + '_seeds'] = (name + (' Spores' if key == 'dreamcap' else ' Seeds'), 'jimtafacts/crops/seed_hops' if key == 'hemp' else 'jimtafacts/crops/seed_brown_mushroom')
for key, name in PRODUCTS.items():
    ITEMS[key] = (name, 'aspirin')
    for suffix, label, texture in [('stock', 'Game Stock', 'dreamglass'), ('active', 'Verified Active', 'dreamglass'), ('formula_plate', 'Formula Plate', 'general_chip_pattern')]:
        ITEMS[key + '_' + suffix] = (name + ' ' + label, texture)

def job(name, cap, items, results, tool=None, water=250, ticks=180, power=64, fluids=None):
    result = dict(name='perception_' + name, capability='jugcraft:' + cap, items=items, results=results,
        fluids=[('minecraft:water', water)] if fluids is None else fluids, ticks=ticks, energy_per_tick=power,
        features=['machines', 'agriculture'])
    if tool: result['tool'] = 'jugcraft:' + tool
    return result

RECIPES = {'pharmaceutical_separator': [], 'laboratory_analyzer': [], 'formulation_press': [], 'bulk_chemical_reactor': []}
RECIPES['pharmaceutical_separator'].append(job('resin_stock', 'botanical_separation',
    [('jugcraft:botanical_resin', 4)], [('jugcraft:thc_stock', 4)], water=500, power=48))
for key in PRODUCTS:
    precision = key in ('lsd', 'dmt')
    RECIPES['pharmaceutical_separator'].append(job('verify_' + key, 'botanical_separation',
        [('jugcraft:' + key + '_stock', 4)], [('jugcraft:' + key + '_active', 4)],
        tool='precision_process_module' if precision else None))
    RECIPES['laboratory_analyzer'].append(job('analyze_' + key, 'pharmacy_analysis',
        [('jugcraft:' + key + '_active', 1), ('jugcraft:blank_formula_plate', 1)], [('jugcraft:' + key + '_formula_plate', 1)],
        tool='precision_process_module' if precision else None, power=48))
    RECIPES['formulation_press'].append(job('finish_' + key, 'drug_formulation',
        [('jugcraft:' + key + '_active', 4), ('minecraft:paper' if key == 'lsd' else 'jugcraft:empty_pharmacy_capsule' if key == 'dmt' else 'jugcraft:botanical_capsule_shell', 4)],
        [('jugcraft:' + key, 4)], tool=key + '_formula_plate', power=96 if precision else 64))
for key, stock in [('lsd', 'perception_substrate'), ('dmt', 'oneiric_stock')]:
    RECIPES['bulk_chemical_reactor'].append(job('prepare_' + key, 'precision_preparation',
        [('jugcraft:' + stock, 1)], [('jugcraft:' + key + '_stock', 4)], tool='precision_process_module',
        ticks=240, power=96, fluids=[('jugcraft:organic_process_stock', 500)]))
REACTOR_RECIPES = [dict(name='perception_' + name, items=items, fluids=[('minecraft:water', 500)], results=[(result, 4)],
    ticks=180, features=['machines', 'agriculture']) for name, items, result in [
    ('dream_concentrate', [('jugcraft:dreamcap', 4), ('jugcraft:mycelium_catalyst', 1)], 'jugcraft:dream_concentrate'),
    ('prismatic_concentrate', [('jugcraft:prism_cactus_fruit', 4), ('minecraft:amethyst_shard', 1)], 'jugcraft:prismatic_concentrate'),
    ('psilocybin_stock', [('jugcraft:dream_concentrate', 4), ('minecraft:redstone', 1)], 'jugcraft:psilocybin_stock'),
    ('mescaline_stock', [('jugcraft:prismatic_concentrate', 4), ('minecraft:quartz', 1)], 'jugcraft:mescaline_stock'),
]]
CRAFTS = {
    'hemp_seeds': (['minecraft:wheat_seeds', 'minecraft:honeycomb', 'minecraft:bone_meal'], 2),
    'dreamcap_seeds': (['minecraft:brown_mushroom', 'minecraft:amethyst_shard', 'minecraft:bone_meal'], 2),
    'prism_cactus_seeds': (['minecraft:cactus', 'minecraft:amethyst_shard', 'minecraft:bone_meal'], 2),
    'mycelium_catalyst': (['minecraft:red_mushroom', 'minecraft:bone_meal', 'minecraft:amethyst_shard'], 4),
    'botanical_capsule_shell': (['jugcraft:pharmaceutical_starch'] * 2 + ['minecraft:paper'] * 2, 4),
    'dream_folio': (['minecraft:book', 'minecraft:amethyst_shard', 'minecraft:compass'], 1),
    'study_canvas': (['minecraft:paper'] * 3 + ['minecraft:lapis_lazuli', 'minecraft:amethyst_shard'], 4),
    'perception_substrate': (['jugcraft:dream_concentrate'] * 4 + ['jugcraft:lucid_imprint', 'minecraft:redstone', 'minecraft:amethyst_shard'], 1),
    'oneiric_stock': (['jugcraft:dream_concentrate'] * 4 + ['minecraft:chorus_fruit'] * 2 + ['minecraft:echo_shard', 'jugcraft:pattern_imprint'], 1),
}

def write_extras(write, assets, data, lang, condition):
    import pharmacy_factory, precision_pharmacy, alcohol_pharmacy, controlled_release
    import shutil
    # Minecraft 26.3 separates block and item atlases. A block model cannot sample the item atlas.
    tablet = assets / 'textures/block/perception/dreamglass_tablet.png'
    tablet.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(assets / 'textures/item/dreamglass.png', tablet)
    for stage in range(4):
        destination = assets / 'textures/block/perception' / ('sourceberry' + str(stage) + '.png')
        destination.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(assets.parents[4] / 'art/owner-library/originals/Magic/assets/ars_jimbaux/textures/block/sourceberry' / ('sourceberry' + str(stage) + '.png'), destination)
    conditions = condition('agriculture')
    for key, (ingredients, count) in CRAFTS.items():
        write(data / 'recipe' / (key + '.json'), {'fabric:load_conditions': conditions, 'type': 'minecraft:crafting_shapeless',
            'category': 'misc', 'ingredients': ingredients, 'result': {'id': 'jugcraft:' + key, 'count': count}})
    write(data / 'recipe/dream_study_plinth.json', {'fabric:load_conditions': conditions, 'type': 'minecraft:crafting_shaped', 'category': 'misc',
        'pattern': [' A ', 'CBC', 'SSS'], 'key': {'A': 'minecraft:amethyst_shard', 'C': 'minecraft:copper_ingot', 'B': 'minecraft:book', 'S': 'minecraft:stone'},
        'result': {'id': 'jugcraft:dream_study_plinth'}})
    for key in [*CRAFTS, 'dream_study_plinth']:
        write(data / 'advancement/recipes/perception' / (key + '.json'), {'fabric:load_conditions': conditions,
            'parent': 'minecraft:recipes/root', 'criteria': {
                'has_ingredient': {'trigger': 'minecraft:inventory_changed', 'conditions': {'items': [{'items': 'minecraft:amethyst_shard'}]}},
                'has_recipe': {'trigger': 'minecraft:recipe_unlocked', 'conditions': {'recipes': ['jugcraft:' + key]}}},
            'requirements': [['has_ingredient', 'has_recipe']], 'rewards': {'recipes': ['jugcraft:' + key]}})
    formulas = [*pharmacy_factory.FORMULAS, *precision_pharmacy.PRESS, *list(alcohol_pharmacy.GRADES)[1:], *controlled_release.XR, 'fentanyl_patch', 'masterwork_reserve', *PRODUCTS]
    write(data / 'tags/item/pharmacy_formulas.json', {'values': ['jugcraft:' + key + '_formula_plate' for key in formulas]})
    for key, name in PRODUCTS.items():
        lang['drug.jugcraft.' + key] = name
        lang['effect.jugcraft.' + key] = name
    for key in ITEMS:
        lang['tooltip.jugcraft.' + key] = 'Fictional botanical pharmacy material. See its recipe and the Dream Studies guide.'
    lang.update({
        'effect.jugcraft.botanical_relaxation': 'Botanical Relaxation', 'effect.jugcraft.pattern_interference': 'Pattern Interference',
        'effect.jugcraft.stimulant_appetite': 'Drug Appetite',
        'tooltip.jugcraft.thc': 'Luck I and 25% less stationary fishing wait for 10 min. Mining -10%; ends with 60 s Drug Appetite. New doses replace their family for a full strain charge.',
        'tooltip.jugcraft.psilocybin': 'Dream Sight: guidance and optional wisps for recorded visible plinths within 16 blocks; Memory study for 8 min. Carry a Dream Folio.',
        'tooltip.jugcraft.mescaline': 'Resonance Sense: persistent direction, distance and height toward the nearest recorded, loaded plinth within 96 blocks for 15 min. Carry a Dream Folio.',
        'tooltip.jugcraft.lsd': 'Pattern Sight for 25 min: recorded visible plinths within 24 blocks, geometric visuals and optional short afterimages. Mirror cards reveal reversed inputs. Supported aimed-gun hand sway +20%.',
        'tooltip.jugcraft.dmt': 'Deep Reverie for 5 min: all three Overworld studies, readable advanced inputs and recorded visible plinths within 24 blocks. Optional deep geometry, short afterimages and cosmetic Clockwork glimpses.',
        'tooltip.jugcraft.dream_folio': 'Carry this and use a Dream Study Plinth to record it. Keeps the 16 most recent Overworld sites; Mescaline points toward loaded sites within 96 blocks.',
        'tooltip.jugcraft.study_canvas': 'One canvas pays for one successful lesson. Wrong symbols reset the attempt without consuming it.',
        'tooltip.jugcraft.oneiric_imprint': 'A collectible imprint earned by completing the Deep Reverie lesson.',
        'tooltip.jugcraft.lucid_imprint': 'Memory lesson reward. Combine with Dream Concentrate, redstone and amethyst to prepare Perception Substrate for LSD.',
        'tooltip.jugcraft.pattern_imprint': 'Mirror lesson reward. Combine with Dream Concentrate, Chorus Fruit and an Echo Shard to prepare Oneiric Stock for DMT.',
        'block.jugcraft.dream_study_plinth': 'Dream Study Plinth',
        'message.jugcraft.dream_study.folio': 'Carry a Dream Folio and reach the plinth in the Overworld to study it.',
        'message.jugcraft.dream_study.resonance': 'Dream resonance: %s · %s blocks',
        'message.jugcraft.dream_study.direction_north': 'North', 'message.jugcraft.dream_study.direction_south': 'South',
        'message.jugcraft.dream_study.direction_east': 'East', 'message.jugcraft.dream_study.direction_west': 'West',
        'screen.jugcraft.dream_study.lesson_0': 'Memory', 'screen.jugcraft.dream_study.lesson_1': 'Mirror', 'screen.jugcraft.dream_study.lesson_2': 'Reverie',
        'screen.jugcraft.dream_study.rule_0': 'Repeat the revealed sequence.',
        'screen.jugcraft.dream_study.rule_1': 'Read the revealed sequence in reverse.',
        'screen.jugcraft.dream_study.rule_2': 'Next: Sun > Wave > Leaf > Star > Sun.',
        'screen.jugcraft.dream_study.hidden': '?', 'screen.jugcraft.dream_study.progress': 'Symbols traced: %s / 3',
        'screen.jugcraft.dream_study.status_0': 'Requires Psilocybin / LSD / DMT for this layer.',
        'screen.jugcraft.dream_study.requires_0': 'Requires Psilocybin, LSD or DMT.',
        'screen.jugcraft.dream_study.requires_1': 'Requires LSD or DMT.',
        'screen.jugcraft.dream_study.requires_2': 'Requires DMT.',
        'screen.jugcraft.dream_study.trace_reward': 'Trace three symbols for %s.',
        'screen.jugcraft.dream_study.status_1': 'Trace three symbols to receive an imprint.',
        'screen.jugcraft.dream_study.status_2': 'Imprint received. Reopen for another paid study.',
        'screen.jugcraft.dream_study.status_3': 'Carry a Study Canvas and make room for the imprint.',
        'screen.jugcraft.dream_study.cost': 'Cost: 1 Study Canvas per completed lesson.',
    })
    for i, name in enumerate(['Sun', 'Wave', 'Leaf', 'Star']): lang['screen.jugcraft.dream_study.glyph_' + str(i)] = name
    for key, (name, produce) in CROPS.items(): lang['block.jugcraft.' + key + '_crop'] = name
    # Low stone writing desk, copper frame, amethyst focus and an inlaid dreamglass tablet.
    def box(a, b, texture):
        return {'from': a, 'to': b, 'faces': {face: {'texture': '#' + texture, 'uv': [0, 0, 16, 16]} for face in ['north', 'south', 'east', 'west', 'up', 'down']}}
    elements = [box([1, 0, 1], [15, 3, 15], 'stone'), box([4, 3, 4], [12, 9, 12], 'stone'),
        box([1, 9, 1], [15, 11, 15], 'copper'), box([2, 11, 2], [14, 12, 14], 'stone'),
        box([4, 12, 4], [12, 12.5, 11], 'tablet'), box([6, 12, 12], [10, 15, 15], 'amethyst')]
    model = {'textures': {'stone': 'minecraft:block/polished_deepslate', 'copper': 'minecraft:block/copper_block',
        'amethyst': 'minecraft:block/amethyst_block', 'tablet': 'jugcraft:block/perception/dreamglass_tablet', 'particle': 'minecraft:block/polished_deepslate'}, 'elements': elements}
    write(assets / 'models/block/dream_study_plinth.json', model)
    write(assets / 'blockstates/dream_study_plinth.json', {'variants': {'': {'model': 'jugcraft:block/dream_study_plinth'}}})
    write(assets / 'models/item/dream_study_plinth.json', {'parent': 'jugcraft:block/dream_study_plinth'})
    write(data / 'loot_table/blocks/dream_study_plinth.json', {'type': 'minecraft:block', 'pools': [
        {'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': 'jugcraft:dream_study_plinth'}]}]})
    # Shared native crop age, bone meal, farmland and harvest behavior; no separate farming backend.
    for key, (name, produce) in CROPS.items():
        crop = key + '_crop'
        variants = {}
        for age in range(8):
            model = crop + '_stage' + str(age)
            if key == 'hemp': texture = 'jugcraft:block/jimtafacts/crops/hops_' + str(min(age, 6))
            elif key == 'dreamcap': texture = 'jugcraft:block/brown_mushroom_colony_stage' + str(age // 2)
            else: texture = 'jugcraft:block/perception/sourceberry' + str(age // 2)
            write(assets / 'models/block' / (model + '.json'), {'parent': 'minecraft:block/cross', 'textures': {'cross': texture}})
            variants['age=' + str(age)] = {'model': 'jugcraft:block/' + model}
        write(assets / 'blockstates' / (crop + '.json'), {'variants': variants})
        write(data / 'loot_table/blocks' / (crop + '.json'), {'type': 'minecraft:block', 'modifier': {'type': 'minecraft:explosion_decay'}, 'pools': [
            {'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': 'jugcraft:' + key + '_seeds'}]},
            {'rolls': 1, 'condition': {'type': 'minecraft:match_block', 'blocks': 'jugcraft:' + crop, 'state': {'age': '7'}},
             'entries': [{'type': 'minecraft:item', 'name': 'jugcraft:' + produce, 'modifier': [{'type': 'minecraft:set_count', 'count': 2}]}]},
            {'rolls': 1, 'condition': {'type': 'minecraft:match_block', 'blocks': 'jugcraft:' + crop, 'state': {'age': '7'}},
             'entries': [{'type': 'minecraft:item', 'name': 'jugcraft:' + key + '_seeds'}]},
        ]})

def handbook_pages():
    return [{'title': 'Botanical Perception Crops', 'icon': 'jugcraft:dreamcap', 'text': [
        'Craft Hemp Seeds from wheat seeds, honeycomb and bone meal. Dreamcap Spores use brown mushroom, amethyst and bone meal. Prism Cactus Seeds use cactus, amethyst and bone meal. These fictional cultivated varieties all use lit farmland, bone meal and native crop growth. Breaking a mature crop gives two produce and two seeds; an immature crop returns one seed.',
        'Four Botanical Resin and 500 mB water in the Separator make four THC Game Stock. Craft Mycelium Catalyst from red mushroom, bone meal and amethyst (four); brown mushroom makes starter spores instead. In the compact Chemical Reactor, four Dreamcap plus one catalyst and 500 mB water make four Dream Concentrate; four Prism Cactus Fruit plus amethyst and the same water make four Prismatic Concentrate.',
        'In that reactor, four Dream Concentrate plus redstone make four Psilocybin Stock; four Prismatic Concentrate plus quartz make four Mescaline Stock. Each compact job takes 180 ticks. None specifies real extraction or synthesis.',
    ]}, {'title': 'Perception Manufacturing', 'icon': 'jugcraft:psilocybin', 'text': [
        'The Separator verifies four matching stock into four active with 250 mB water, 180 ticks at 64 JE/t. THC, Psilocybin and Mescaline need no precision tool. LSD and DMT require the existing Precision Process Module in an upgrade slot.',
        'Analyze one matching active with one Blank Formula Plate and 250 mB water for a reusable formula (180 ticks, 48 JE/t). LSD and DMT analysis requires the precision module. The Press combines four verified active, four paid packaging items and 250 mB water into four doses with that formula (180 ticks, 64 JE/t; LSD/DMT 96 JE/t). THC, Psilocybin and Mescaline use Botanical Capsule Shells, crafted from two starch and two paper (four); DMT uses advanced Empty Pharmacy Capsules. LSD uses four paper for four perforated paper doses. Paper/capsule doses leave no bottle and retain their 0.8-second use time.',
        'Complete Memory with Psilocybin to earn a Lucid Imprint. Craft it with four Dream Concentrate, redstone and amethyst into Perception Substrate. The existing Bulk Chemical Reactor with a precision module and 500 mB Organic Process Stock makes four LSD Stock (240 ticks, 96 JE/t).',
        'Complete Mirror with LSD to earn a Pattern Imprint. Craft it with four Dream Concentrate, two Chorus Fruit and an Echo Shard into Oneiric Stock. The same precision reactor job makes four DMT Stock, using Overworld and End supplies. These are wholly fictional recipes.',
    ]}, {'title': 'Perception Effects and Dream Study', 'icon': 'jugcraft:dream_folio', 'text': [
        'THC: Luck I and 25% less pre-bite fishing wait while stationary for 10 min, 10 s onset, 20 strain. It does not stack its fishing bonus with stronger Allure. Mining is 10% slower; natural expiry causes 60 s Drug Appetite. Charcoal clears that appetite. Luck uses vanilla fishing rules and loot tables.',
        'Psilocybin: 8 min Dream Sight, 10 s onset, 20 strain, plinth clues within 16 blocks and line of sight. Mescaline: 15 min Resonance Sense, 15 s onset, 25 strain, compass pulses toward the nearest recorded loaded plinth within 96 blocks. No undiscovered structures, ores, mobs or player bases are searched.',
        'LSD: 25 min Pattern Sight, 20 s onset, 35 strain; visible recorded plinths within 24 blocks, Memory and Mirror lessons. Supported aimed-gun hand sway is 20% higher, without altering shot spread. DMT: 5 min Overworld Deep Reverie, 20 s onset, 50 strain; all three lessons and 24-block sight.',
        'Craft a Dream Folio from a book, compass and amethyst. Craft a Dream Study Plinth from three stone, two copper, a book and amethyst. Carry the folio and use the plinth to record one of up to 16 Overworld sites. The last 16 physically visited sites persist in the folio; no chunks are force-loaded.',
        'At the plinth, Memory repeats the visible sequence, Mirror reverses it, and Reverie advances each symbol through Sun, Wave, Leaf, Star. These layers require Psilocybin or stronger, LSD or DMT, and DMT respectively. Three correct symbols consume one Study Canvas and award one Lucid, Pattern or Oneiric Imprint. Four canvases craft from three paper, lapis and amethyst. Wrong input simply restarts; no combat or injury debuff is created.',
        'Reopen the plinth to start another paid study. Clues are private and remain readable with reduced magic visuals. Studying uses your own carried canvas and ordinary inventory; Censer expeditions remain their own activity. All four psychedelics share family tolerance and decaying strain; THC has its own family. New doses replace their family for a full strain charge. All use the same strain, online timers, milk and death rules.',
    ]}]
