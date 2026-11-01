"""Acquisition edges for implemented NEWEST systems outside vanilla recipes.

This reads the native data consumed by the game, not the owner's unported source
recipes. Stations, catalysts and activation ingredients remain prerequisites.
Counts, mana quantities, elapsed time and encounter difficulty are not simulated.
"""
import json


def extend(root, edge, evidence, items):
    res = root / 'src/main/resources'
    data = res / 'data/jugcraft'
    java = 'src/main/java/io/github/jimbozoomer/jugcraft/'

    def read(path):
        return json.loads(path.read_text(encoding='utf-8'))

    def j(name):
        return 'jugcraft:jugtania_' + name

    def outputs(value):
        if isinstance(value, str):
            return [value]
        if isinstance(value, list):
            return [item for part in value for item in outputs(part)]
        if isinstance(value, dict):
            return outputs(next((value[key] for key in ('id', 'item', 'block', 'name') if key in value), []))
        return []

    reference = read(root / 'tools/data/survival-reference-26.3.json')
    vanilla_items = set(reference['items'])

    def block_tag(tag, seen=frozenset()):
        if tag in seen:
            raise ValueError('Cyclic botanical input block tag: ' + tag)
        namespace, path = tag.split(':', 1)
        source = res / 'data' / namespace / 'tags/block' / (path + '.json')
        if not source.is_file():
            # The native logs predicate has the same membership as the pinned
            # vanilla item tag. Do not invent an item equivalent for other tags.
            if tag == 'minecraft:logs':
                return ['#minecraft:logs']
            raise ValueError('Unresolved botanical input block tag: ' + tag)
        choices = []
        for entry in read(source)['values']:
            value = entry if isinstance(entry, str) else entry['id']
            if value.startswith('#'):
                choices.extend(block_tag(value[1:], seen | {tag}))
            elif value in items or value in vanilla_items:
                choices.append(value)
        return choices

    def block_input(value):
        if isinstance(value, dict) and value.get('type') == 'tag':
            return block_tag(value['tag'])
        name = outputs(value)[0]
        return 'fluid:' + name if name in ('minecraft:water', 'minecraft:lava') else name

    def route(source, kind, needs, result):
        edge(source, kind, needs, outputs(result))

    def found(names, source, note):
        for name in names:
            if name in items:
                evidence[name].append({'kind': 'native_acquisition', 'source': source, 'note': note})

    catalog = read(res / 'jugcraft/newest_jugtania.json')
    apothecaries = [j(name) for name in sorted(catalog['blocks']) if name.startswith('apothecary_')]
    pools = [j(name) for name in ('mana_pool', 'diluted_pool', 'fabulous_pool')]
    # One real, renewable mana-production route. The audit remains qualitative;
    # it does not waive the workstations' actual per-recipe mana costs.
    mana_supply = [j('endoflame'), j('mana_spreader'), '#minecraft:coals']
    for path in sorted((data / 'jugtania_recipe').rglob('*.json')):
        recipe = read(path)
        kind = recipe['type']
        source = path.relative_to(root).as_posix()
        needs = list(recipe.get('ingredients', []))
        result = recipe.get('output', recipe.get('result'))
        if kind == 'petal_apothecary':
            needs += [apothecaries, 'minecraft:water_bucket', recipe['reagent']]
        elif kind in ('pure_daisy', 'state_copying_pure_daisy'):
            needs = [j('pure_daisy'), block_input(recipe['input'])]
        elif kind == 'mana_infusion':
            needs = [pools, recipe['input']] + (mana_supply if recipe.get('mana', 0) else [])
            if recipe.get('catalyst'):
                needs.append(block_input(recipe['catalyst']))
        elif kind == 'runic_altar':
            needs += [j('runic_altar'), j('livingrock')] + mana_supply
        elif kind == 'terra_plate':
            needs += [j('terra_plate'), block_tag('jugcraft:jugtania_terra_plate_base'), 'minecraft:lapis_block'] + mana_supply
        elif kind == 'elven_trade':
            needs += [j('alfheim_portal'), j('natura_pylon'), pools, j('twig_wand'),
                      j('livingwood_log'), j('glimmering_livingwood_log')] + mana_supply
            # BotanicalWorkshop.craft currently requires a single output stack.
            if isinstance(result, list) and len(result) != 1:
                continue
        elif kind in ('orechid', 'orechid_ignem', 'marimorphosis'):
            needs = [j(kind), block_input(recipe['input'])] + mana_supply
        elif kind == 'brew':
            for empty, filled in (('vial', 'brew_vial'), ('flask', 'brew_flask')):
                route(source, 'native_botanical_brew', needs + [j('brewery'), j(empty)] + mana_supply, j(filled))
            continue
        else:
            continue  # Unknown future types stay unaudited rather than acquiring a free route.
        route(source, 'native_botanical_' + kind, needs, result)

    colors = 'white orange magenta light_blue yellow lime pink gray light_gray cyan purple blue brown green red black'.split()
    found([j(color + '_mystical_flower') for color in colors], java + 'botany/BotanicalFlowers.java',
          'The registered botanical_flowers feature places these flowers in newly generated Overworld terrain; harvesting retains their item drops.')
    for color in colors:
        route(java + 'botany/BotanicalFlowers.java', 'bonemeal_mystical_flower',
              [j(color + '_mystical_flower'), 'minecraft:bone_meal'], j(color + '_double_flower'))
    route(java + 'botany/BotanicalEnderAir.java', 'bottle_air_in_the_end',
          ['minecraft:glass_bottle', 'minecraft:ender_eye'], j('ender_air_bottle'))
    route(java + 'botany/BotanicalWaterBowl.java', 'fill_bowl_from_water_source',
          ['minecraft:bowl', 'fluid:minecraft:water'], j('water_bowl'))
    for color in ('dry', 'golden', 'vivid', 'scorched', 'infused', 'mutated'):
        route(java + 'botany/BotanicalLandscaping.java', 'pasture_seed_then_silk_touch',
              [j(color + '_seeds'), 'minecraft:dirt', 'minecraft:enchanted_book'], j(color + '_grass'))
    for relic in ('infinite_fruit', 'king_key', 'flugel_eye', 'thor_ring', 'odin_ring', 'loki_ring'):
        route(java + 'botany/BotanicalRelics.java', 'roll_owned_gaia_die', [j('dice')], j(relic))

    # These registered presentation items represent explicitly constructed or
    # Creative-only blocks. Their actual block drops cannot yield these items.
    exclusions = {
        j('creative_pool'): 'Creative-only infinite mana source: BotanicalWorkshop.step refills it unconditionally; there is no Survival recipe.',
        j('corporea_spark_creative'): 'Creative-only Corporea source: CorporeaInventory supplies requested items from kind-2 sources without inventory extraction.',
        j('infrangible_platform'): 'Creative-only unbreakable platform: BotanicalPlatforms requires infinite materials to edit it and assigns unbreakable strength.',
        j('cacophonium_block'): 'In-world instrument state: BotanicalCacophonium applies a Cacophonium to a Note Block; its native block loot returns the Note Block, not this presentation item.',
        j('enchanter'): 'In-world ritual core: BotanicalEnchanter forms it from the complete Lapis/Obsidian/Mana Pylon pattern; its native block loot returns Lapis, not this presentation item.',
        j('enchanted_soil'): 'In-world soil state: BotanicalTreasures.Overgrowth converts grass with an Overgrowth Seed; native loot and BotanicalGuide explicitly return dirt when broken.',
    }
    from survival_arcane_routes import extend as arcane_routes
    exclusions.update(arcane_routes(root, edge, evidence, items))
    return {item: reason for item, reason in exclusions.items() if item in items}
