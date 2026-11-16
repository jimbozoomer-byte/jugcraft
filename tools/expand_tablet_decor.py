"""Materialize reviewed inexpensive decor into the shared, explicit output allowlist.

Not a runtime suffix rule: additions still require review of this generated tag.
No prices are changed and no fallback-priced products are admitted.
"""
import json
from fractions import Fraction
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DATA = ROOT / 'src/main/resources/data/jugcraft'
WOODS = ('aspen', 'cedar', 'chestnut', 'walnut', 'willow', 'baobab',
         'tamarisk', 'yew', 'yucca', 'aged_maple', 'aged_pine', 'bald_cypress')
PARTS = {'log': 32, 'wood': 32, 'planks': 8, 'stairs': 12, 'slab': 4,
         'fence': Fraction(40, 3), 'fence_gate': 32, 'door': 16,
         'trapdoor': 24, 'button': 8, 'pressure_plate': 16}
BASES = ('stone', 'cobblestone', 'andesite', 'diorite', 'granite', 'tuff',
         'deepslate', 'cobbled_deepslate', 'sandstone', 'red_sandstone',
         'netherrack', 'blackstone', 'basalt', 'bricks', 'stone_bricks',
         'oak_planks', 'spruce_planks', 'birch_planks', 'jungle_planks',
         'acacia_planks', 'dark_oak_planks', 'crimson_planks', 'warped_planks')

def candidates():
    values = json.loads((DATA / 'alchemy/values.json').read_text())['items']
    result = {}
    def add(item, floor, reason):
        row = values.get(item)
        if not row or 'fallback' in row['source'] or 'pending' in row['source']:
            return
        price = Fraction(row['numerator'], row['denominator'])
        # Reject underpriced conversions; do not silently reprice the economy.
        if price >= floor and price <= 128:
            result[item] = reason
    for wood in WOODS:
        for part, price in PARTS.items():
            add(f'jugcraft:{wood}_{part}', price, f'reviewed ordinary wood: {wood}')
        for part in ('log', 'wood'):
            add(f'jugcraft:stripped_{wood}_{part}', 32, f'reviewed ordinary wood: {wood}')
    for base in BASES:
        row = values.get('minecraft:' + base)
        if not row:
            continue
        floor = Fraction(row['numerator'], row['denominator'])
        tag = DATA / f'tags/item/chiselworks/{base}.json'
        if tag.exists():
            for item in json.loads(tag.read_text())['values']:
                if isinstance(item, str) and item.startswith('jugcraft:chiselworks_'):
                    add(item, floor, f'chiselworks equivalent of minecraft:{base}')
    for path in (DATA / 'recipe/newest/stoneworks').glob('*.json'):
        recipe = json.loads(path.read_text())
        ingredient = recipe.get('ingredient')
        if recipe.get('type') != 'minecraft:stonecutting' or not isinstance(ingredient, str) or ingredient not in {'minecraft:'+b for b in BASES}:
            continue
        output = recipe.get('result', {})
        if not isinstance(output, dict) or not output.get('id', '').startswith('jugcraft:stoneworks_'):
            continue
        row = values[ingredient]
        add(output['id'], Fraction(row['numerator'], row['denominator']) / output.get('count', 1),
            f'stonecutting equivalent of {ingredient}')
    # These reviewed harvest pieces drop only themselves, expose no storage or
    # material-processing output, and are not bought by town shops. Keep the
    # usual 128-essence ceiling; the costly metal pieces remain crafting-only.
    for module_name in ('thanksgiving_homestead', 'thanksgiving_woodland',
                        'thanksgiving_town', 'thanksgiving_toys'):
        module = __import__(module_name)
        for name in module.PROPS:
            add('jugcraft:' + name, Fraction(2), 'reviewed harvest decoration')
    return result

if __name__ == '__main__':
    path = DATA / 'tags/item/tablet_outputs.json'
    tag = json.loads(path.read_text())
    before = set(tag['values'])
    additions = candidates()
    tag['values'] = sorted(before | additions.keys())
    path.write_text(json.dumps(tag, indent=2) + '\n')
    print(f'Added {len(set(additions) - before)} reviewed decorative outputs; {len(tag["values"])} total.')
