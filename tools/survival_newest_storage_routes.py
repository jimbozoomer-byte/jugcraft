"""Native component-preserving upgrades and intentionally administrative stores."""
import json


PERSONAL_CONTAINERS = (
    'chest', 'trapped_chest', 'barrel', 'shulker_box', 'copper_chest',
    'exposed_copper_chest', 'weathered_copper_chest', 'oxidized_copper_chest',
    'decorated_pot', 'suspicious_sand', 'suspicious_gravel', 'inventory',
    'minecart', 'item_frame',
)


def extend(root, edge, evidence, items):
    # OwnerBackpackRecipe delegates matching to ShapedRecipe and preserves the
    # input carrier's components on assembly. Every ingredient is still paid.
    folder = root / 'src/main/resources/data/jugcraft/recipe/newest/packworks'
    for path in sorted(folder.glob('*.json')):
        recipe = json.loads(path.read_text(encoding='utf-8'))
        if recipe['type'] != 'jugcraft:packworks_tier_upgrade':
            continue
        ingredients = [recipe['key'][symbol] for symbol in ''.join(recipe['pattern']) if symbol != ' ']
        edge(path.relative_to(root).as_posix(), 'component_preserving_shaped_upgrade',
             ingredients + ['minecraft:crafting_table'], [recipe['result']['id']])

    exclusions = {}
    for name in PERSONAL_CONTAINERS:
        item = 'jugcraft:personal_loot_' + name
        if item in items:
            exclusions[item] = (
                'Structure/admin reward container, not a survival item: OwnerLoot and '
                'OwnerLootEntities configure private loot with gamemaster commands or '
                'convert unopened generated loot. Native block loot tables are empty; '
                'entity-item placement requires creative mode.')
    for name in ('infinity_upgrade', 'survival_infinity_upgrade'):
        item = 'jugcraft:packworks_' + name
        if item in items:
            exclusions[item] = (
                'Administrative template-storage upgrade without a survival recipe. '
                'OwnerBackpackInfinity restricts the admin upgrade to gamemasters; '
                'the separate survival template option is disabled by default and '
                'requires explicit server configuration and supplied upgrade items.')
    return exclusions
