"""Data regressions for the four simplified mineral-sand lines (tools/mineral_sand_processing.py).

Each sand must be acquirable, reach existing products through existing machines, keep old routes and saved
intermediates usable, never be recreated by a recipe, and never mint transmutation essence or items in a loop.
Runtime machine behavior is covered by MineralSandProcessingGameTests; these checks read the generated data.
"""
from collections import Counter, defaultdict
from fractions import Fraction
import json
from pathlib import Path
import sys
import unittest

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / 'tools'))
from mineral_sands import SANDS  # noqa: E402
import mineral_sand_processing as processing  # noqa: E402

RES = ROOT / 'src/main/resources'
RECIPES = RES / 'data/jugcraft/recipe'
VALUES = {k: Fraction(v['numerator'], v['denominator']) for k, v in json.loads(
    (RES / 'data/jugcraft/alchemy/values.json').read_text(encoding='utf-8'))['items'].items()}
SOURCES = {k: v['source'] for k, v in json.loads(
    (RES / 'data/jugcraft/alchemy/values.json').read_text(encoding='utf-8'))['items'].items()}
SAND_IDS = {'jugcraft:' + name for name in SANDS}
# Every item a sand line makes or passes through, including the old zirconia forms it shares.
CHAIN = SAND_IDS | {'jugcraft:' + name for name in [
    'raw_titanium', 'titanium_sponge', 'zircon_concentrate', 'zirconia_precursor', 'zirconia_powder',
    'zirconia_charge', 'zirconia_blank', 'fired_zirconia_blank', 'zirconia_insert', 'rare_earth_oxide',
    *processing.ITEMS]}
# Vanilla tags have no local file; price them by their cheapest vanilla member, as the builder does with the jar.
VANILLA_TAGS = {'#minecraft:wool': '_wool', '#minecraft:planks': '_planks'}


def tags():
    found = defaultdict(list)
    for path in sorted((RES / 'data').glob('*/tags/item/**/*.json')):
        namespace = path.relative_to(RES / 'data').parts[0]
        key = namespace + ':' + path.relative_to(RES / 'data' / namespace / 'tags/item').with_suffix('').as_posix()
        found[key] += json.loads(path.read_text(encoding='utf-8')).get('values', [])
    return found


TAGS = tags()


def expand(ref, seen=frozenset()):
    if isinstance(ref, dict):
        ref = ref.get('ingredient', ref.get('item', ref.get('id', '#' + ref['tag'] if 'tag' in ref else '')))
    if isinstance(ref, list):
        return set().union(*(expand(r, seen) for r in ref)) if ref else set()
    if not isinstance(ref, str) or not ref:
        return set()
    if not ref.startswith('#'):
        return {ref}
    if ref[1:] in seen:
        return set()
    if ref[1:] not in TAGS:
        return {ref}
    return set().union(*(expand(v if isinstance(v, str) else v.get('id', ''), seen | {ref[1:]}) for v in TAGS[ref[1:]]))


def parse(path):
    """(consumed [(options, count)], maximum outputs {item: count}, retained tool or None, raw recipe)."""
    r = json.loads(path.read_text(encoding='utf-8'))
    kind, consumed, made = r.get('type', ''), [], Counter()
    if isinstance(r.get('pattern'), list) and 'key' in r:
        for symbol, n in Counter(''.join(r['pattern'])).items():
            if symbol != ' ':
                consumed.append((expand(r['key'][symbol]), n))
    elif kind == 'minecraft:smithing_transform':
        consumed = [(expand(r[k]), 1) for k in ('template', 'base', 'addition') if k in r]
    else:
        for entry in r.get('items') or r.get('ingredients') or []:
            consumed.append((expand(entry), entry.get('count', 1) if isinstance(entry, dict) else 1))
        if 'ingredient' in r:
            consumed.append((expand(r['ingredient']), 1 if kind == 'minecraft:stonecutting' else r.get('input_count', 1)))
    results = r.get('results') if isinstance(r.get('results'), list) else []
    for out in results + ([r['result']] if 'result' in r else []) + list(r.get('byproducts') or []):
        if isinstance(out, str):
            made[out] += 1
        elif isinstance(out, dict) and (out.get('id') or out.get('item')):
            made[out.get('id') or out.get('item')] += out.get('count', 1)
    tool = r.get('tool')
    tool = tool.get('id') if isinstance(tool, dict) else tool
    return consumed, dict(made), tool, r


ALL = {path.relative_to(RECIPES).with_suffix('').as_posix(): parse(path) for path in sorted(RECIPES.rglob('*.json'))}


def recipe(name):
    return ALL[name]


def consumers(item):
    return [name for name, (consumed, _, tool, _) in ALL.items()
            if tool == item or any(item in options for options, _ in consumed)]


def cheapest(options):
    """The builder's slot cost: the cheapest priced option (vanilla tags by their vanilla members)."""
    prices = [VALUES[o] for o in options if o in VALUES]
    for tag, suffix in VANILLA_TAGS.items():
        if tag in options:
            prices += [v for k, v in VALUES.items() if k.startswith('minecraft:') and k.endswith(suffix)]
    if not prices:
        raise AssertionError(f'Unpriced ingredient options {sorted(options)}')
    return min(prices)


class MineralSandProcessingTests(unittest.TestCase):
    def test_every_sand_is_acquired_from_generated_beds_one_unit_per_block(self):
        for name in SANDS:
            with self.subTest(sand=name):
                loot = json.loads((RES / f'data/jugcraft/loot_table/blocks/{name}.json').read_text(encoding='utf-8'))
                self.assertEqual(1, len(loot['pools']))
                entry, = loot['pools'][0]['entries']
                self.assertEqual(('jugcraft:' + name, None), (entry['name'], entry.get('functions')),
                                 'Exactly the block itself, with no Fortune or count bonus')
                self.assertTrue((RES / f'data/jugcraft/worldgen/feature/ore_{name}.json').exists())
                self.assertTrue((RES / f'data/jugcraft/worldgen/placed_feature/ore_{name}.json').exists())
                biomes = json.loads((RES / f'data/jugcraft/tags/worldgen/biome/has_surface_sand/{name}.json')
                                    .read_text(encoding='utf-8'))['values']
                self.assertTrue(biomes)
                self.assertTrue((RES / f'assets/jugcraft/items/{name}.json').exists())

    def test_each_simplified_chain_reaches_existing_products_through_existing_machines(self):
        conditioning, firing, finishing = ('jugcraft:mineral_conditioning', 'jugcraft:ceramic_firing',
                                           'jugcraft:precision_finishing')
        chains = {
            'titanium': [('chemical_reaction/sands_titanium_feed', conditioning, {'jugcraft:titanium_sand': 2}, {'jugcraft:raw_titanium': 1}),
                         ('chemical_reaction/titanium_sponge', None, {'jugcraft:raw_titanium': 1, 'jugcraft:coke': 1}, {'jugcraft:titanium_sponge': 1}),
                         ('arc_smelting/titanium_sponge', None, {'jugcraft:titanium_sponge': 1}, {'jugcraft:titanium_ingot': 1})],
            'pigment': [('chemical_reaction/sands_titanium_white_dye', None, {'jugcraft:raw_titanium': 1}, {'minecraft:white_dye': 4})],
            'zircon': [('chemical_reaction/sands_zirconia_powder', None, {'jugcraft:zircon_sand': 4}, {'jugcraft:zirconia_powder': 1}),
                       ('chemical_reaction/sands_fire_zirconia_blank', firing, {'jugcraft:zirconia_powder': 1}, {'jugcraft:fired_zirconia_blank': 1}),
                       ('chemical_reaction/finish_zirconia_insert', finishing, {'jugcraft:fired_zirconia_blank': 1}, {'jugcraft:zirconia_insert': 2})],
            'rare_earth': [('chemical_reaction/sands_mixed_rare_earth_oxide', conditioning, {'jugcraft:rare_earth_sand': 2}, {'jugcraft:rare_earth_oxide': 1}),
                           ('chemical_reaction/sands_rare_earth_separation', None, {'jugcraft:rare_earth_oxide': 1},
                            {'jugcraft:neodymium_oxide': 1, 'jugcraft:cerium_oxide': 2, 'jugcraft:yttrium_oxide': 1}),
                           ('alloying/sands_neodymium_magnet', None, {'jugcraft:neodymium_oxide': 1, 'jugcraft:ferroboron': 1}, {'jugcraft:neodymium_magnet': 4})],
            'garnet': [('pulverizing/sands_garnet_grit', None, {'jugcraft:garnet_sand': 1}, {'jugcraft:garnet_grit': 1})],
        }
        for chain, steps in chains.items():
            for name, capability, inputs, outputs in steps:
                with self.subTest(chain=chain, step=name):
                    consumed, made, _, raw = recipe(name)
                    self.assertEqual(inputs, {next(iter(options)): n for options, n in consumed})
                    self.assertTrue(all(len(options) == 1 for options, _ in consumed), 'Exact item IDs, never a tag')
                    self.assertEqual(outputs, made)
                    self.assertEqual(capability, raw.get('capability'))
        # Every final product has a real consumer: a recipe ingredient or a retained machine tool.
        for product in ['jugcraft:titanium_ingot', 'jugcraft:zirconia_insert', 'jugcraft:neodymium_magnet',
                        'jugcraft:cerium_oxide', 'jugcraft:yttrium_oxide', 'jugcraft:garnet_grit',
                        'jugcraft:cerium_polishing_head', 'jugcraft:garnet_abrasive_head', 'jugcraft:garnet_sanding_block',
                        'jugcraft:polished_lens', 'jugcraft:precision_shaft']:
            with self.subTest(product=product):
                self.assertTrue(consumers(product), product + ' needs a consumer')
        heads = json.loads((RES / 'data/jugcraft/tags/item/finishing_heads.json').read_text(encoding='utf-8'))['values']
        self.assertTrue({'jugcraft:cerium_polishing_head', 'jugcraft:garnet_abrasive_head'} <= set(heads))
        self.assertEqual('jugcraft:cerium_polishing_head', recipe('chemical_reaction/sands_finish_lens_cerium')[2])
        self.assertEqual('jugcraft:garnet_abrasive_head', recipe('chemical_reaction/sands_finish_shaft_garnet')[2])
        self.assertEqual([0.5, 0.5, 0.5], recipe('chemical_reaction/sands_rare_earth_separation')[3]['result_chances'])

    def test_old_routes_and_saved_intermediates_stay_usable(self):
        for name in ['chemical_reaction/recover_trace_zircon', 'chemical_reaction/recover_regional_zircon',
                     'chemical_reaction/prepare_zirconia_precursor', 'chemical_reaction/calcine_zirconia',
                     'chemical_reaction/prepare_zirconia', 'pressing/ceramics_zirconia_blank',
                     'chemical_reaction/fire_zirconia', 'chemical_reaction/finish_zirconia_insert',
                     'chemical_reaction/finish_polished_lens', 'chemical_reaction/finish_precision_shaft',
                     'chemical_reaction/rare_earth_oxide', 'alloying/neodymium_magnet',
                     'alloying/neodymium_magnet_from_ferroboron', 'ceramics_efficiency_upgrade',
                     'chemical_reaction/titanium_sponge', 'chemical_reaction/chlorinate_titanium_mineral']:
            self.assertIn(name, ALL, 'Old route removed: ' + name)
        for item in ['zircon_concentrate', 'zirconia_precursor', 'zirconia_powder', 'zirconia_charge', 'zirconia_blank',
                     'fired_zirconia_blank', 'rare_earth_oxide', 'raw_titanium', 'titanium_sponge', 'monazite']:
            with self.subTest(item=item):
                self.assertTrue((RES / f'assets/jugcraft/items/{item}.json').exists(), 'Saved item ID stays registered')
                self.assertTrue(consumers('jugcraft:' + item), 'Saved intermediate keeps a use')
        # Saved concentrate also enters the short line; it spends the same four-sand allocation.
        self.assertEqual({'jugcraft:zircon_concentrate': 2},
                         {next(iter(o)): n for o, n in recipe('chemical_reaction/sands_zirconia_from_concentrate')[0]})
        self.assertEqual({'jugcraft:zircon_sand': 2},
                         {next(iter(o)): n for o, n in recipe('chemical_reaction/recover_regional_zircon')[0]})

    def test_no_recipe_recreates_a_reserve_or_feeds_on_generic_sand(self):
        for name, (consumed, made, _, _) in ALL.items():
            self.assertFalse(SAND_IDS & set(made), name + ' recreates a finite mineral-sand unit')
            for options, _ in consumed:
                if SAND_IDS & options:
                    self.assertEqual(1, len(options), name + ' must name one exact rich sand, never a shared tag')
        for item, expected in [('minecraft:sand', ['chemical_reaction/recover_trace_zircon']), ('minecraft:red_sand', [])]:
            self.assertEqual(expected, [n for n in consumers(item) if n.split('/')[-1].startswith(('sands_', 'recover_'))],
                             'Ordinary sand keeps only the old trace-zircon exception; red sand gains nothing')
        for tag, values in TAGS.items():
            self.assertFalse(SAND_IDS & set(v if isinstance(v, str) else v.get('id') for v in values),
                             f'#{tag} would let generic recipes consume rich sand')

    def test_sand_line_recipes_cannot_mint_essence(self):
        audited = 0
        for name, (consumed, made, _, raw) in ALL.items():
            if not any(CHAIN & options for options, _ in consumed):
                continue
            audited += 1
            with self.subTest(recipe=name):
                spent = sum(cheapest(options) * n for options, n in consumed)
                # Chanced coproducts count at their maximum: a lucky batch must not create value either.
                returned = sum(VALUES[item] * n for item, n in made.items())
                self.assertLessEqual(returned, spent, f'{name} returns {returned} essence for {spent}')
        self.assertGreater(audited, 30)

    def test_no_item_cycle_through_a_sand_line_multiplies_material(self):
        graph = defaultdict(set)
        for name, (consumed, made, _, _) in ALL.items():
            for options, _ in consumed:
                for item in options:
                    graph[item] |= set(made)
        def reaches(start, goal):
            seen, todo = set(), [start]
            while todo:
                for nxt in graph.get(todo.pop(), ()):
                    if nxt == goal:
                        return True
                    if nxt not in seen:
                        seen.add(nxt)
                        todo.append(nxt)
            return False
        cyclic = sorted(item for item in CHAIN if reaches(item, item))
        self.assertEqual(['jugcraft:raw_titanium'], cyclic, 'Only the existing storage-block pair may cycle')
        block, = (n for n, (c, m, _, _) in ALL.items() if m == {'jugcraft:raw_titanium_block': 1})
        back, = (n for n, (c, m, _, _) in ALL.items() if m == {'jugcraft:raw_titanium': 9})
        self.assertEqual(9, recipe(block)[0][0][1])
        self.assertEqual({'jugcraft:raw_titanium_block'}, recipe(back)[0][0][0])

    def test_hand_computed_essence_records_follow_the_builder_rule(self):
        mixed = VALUES['jugcraft:rare_earth_oxide']
        for oxide in ('neodymium_oxide', 'cerium_oxide', 'yttrium_oxide'):
            self.assertEqual(mixed / 4, VALUES['jugcraft:' + oxide])
        insert = VALUES['jugcraft:zirconia_insert']
        for form in ('zirconia_precursor', 'zirconia_powder', 'zirconia_charge', 'zirconia_blank', 'fired_zirconia_blank'):
            self.assertEqual(2 * insert, VALUES['jugcraft:' + form])
            self.assertEqual('local zirconia finishing accounting', SOURCES['jugcraft:' + form])
        self.assertEqual('fallback (balance pending)', SOURCES['jugcraft:garnet_grit'])
        # Recipe-derived records: sum of each slot's cheapest option divided by the output count.
        for item in ['neodymium_magnet', 'cerium_polishing_head', 'garnet_abrasive_head', 'garnet_sanding_block',
                     'neodymium_drone_motor', 'tilt_rotor_nacelle', 'superconducting_motor', 'superconducting_lift_fan']:
            with self.subTest(item=item):
                source = SOURCES['jugcraft:' + item]
                self.assertTrue(source.startswith('recipe jugcraft:'), source)
                consumed, made, _, _ = recipe(source.split(':', 1)[1])
                cost = sum(cheapest(options) * n for options, n in consumed) / made['jugcraft:' + item]
                self.assertEqual(cost, VALUES['jugcraft:' + item])
        magnets = [name for name, (_, made, _, _) in ALL.items() if 'jugcraft:neodymium_magnet' in made]
        for name in magnets:
            consumed, made, _, _ = recipe(name)
            self.assertLessEqual(VALUES['jugcraft:neodymium_magnet'] * made['jugcraft:neodymium_magnet'],
                                 sum(cheapest(o) * n for o, n in consumed), name)

    def test_handbook_starts_players_on_every_sand_line(self):
        book = json.loads((RES / 'assets/jugcraft/handbook/en_us.json').read_text(encoding='utf-8'))
        pages = [page for chapter in book['chapters'] for page in chapter['pages']]
        ores = next(page for page in pages if page['title'] == 'Ores')
        for name, info in SANDS.items():
            line, = (text for text in ores['text'] if text.startswith(info['display'] + ':'))
            self.assertIn('First step: ' + processing.FIRST_STEPS[name], line)
            self.assertNotIn('future', line)
        chapter = next(c for c in book['chapters'] if c['title'] == 'Mineral Sand Processing')
        self.assertTrue(chapter['pages'][0]['title'].startswith('Start here'))
        start = ' '.join(chapter['pages'][0]['text'])
        for words in ['Prospector', 'Pulverizer', 'Mineral Conditioning Station', 'Chemical Reactor', 'lye',
                      'sulfuric acid', 'chlorine', 'Lock'.lower()]:
            self.assertIn(words, start)
        for machine, notes in processing.HANDBOOK.items():
            for note in notes:
                self.assertTrue(any(note in page.get('text', []) for page in pages), machine)


if __name__ == '__main__':
    unittest.main()
