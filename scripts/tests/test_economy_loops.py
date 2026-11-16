"""Economy conversion audit (12 November 2026): essence, Jugs and the convenience-only output rule.

Essence is renewable by design (daylight collectors) and every default item dissolves into it, so the transmutation
tablet, condensers and density gems may only form the convenience supplies in jugcraft:tablet_outputs. These checks
read the shipped data and guard the places where those supplies could still turn into currency or more essence:
the town's shops, the civic commissions, collector fuel upgrades and single recipe conversions. Runtime transactions
(dissolving, forming, selling, replays) are covered by EconomyLoopGameTests; vanilla's own recipes are not in the
repository, so conversions through them are reasoned in docs/features/convenience-transmutation.md instead.
"""
from collections import defaultdict
from copy import deepcopy
from fractions import Fraction
import json
import math
from pathlib import Path
import re
import sys
import unittest

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / 'tools'))
import town_shops  # noqa: E402

RES = ROOT / 'src/main/resources'
VALUES = {k: Fraction(v['numerator'], v['denominator']) for k, v in json.loads(
    (RES / 'data/jugcraft/alchemy/values.json').read_text(encoding='utf-8'))['items'].items()}
FORMABLE = {v if isinstance(v, str) else v['id'] for v in json.loads(
    (RES / 'data/jugcraft/tags/item/tablet_outputs.json').read_text(encoding='utf-8'))['values']}
RUNTIME_SHOPS = json.loads((RES / 'data/jugcraft/town/shops.json').read_text(encoding='utf-8'))['shops']
# The pre-audit General Store offers that paid Jugs for formable supplies (tools/town_shops.py before 12 November).
REMOVED_OFFERS = [['minecraft:white_wool', 32, 1], ['minecraft:oak_log', 64, 1], ['minecraft:spruce_log', 64, 1],
                  ['minecraft:birch_log', 64, 1], ['minecraft:cobblestone', 64, 1]]
FREE_FLUIDS = {'minecraft:water'}


def essence(item):
    """Catalogue value; an unlisted item takes the runtime's explicit one-essence fallback (AlchemyValues.java)."""
    return VALUES.get(item, Fraction(1))


def item_tags():
    """26.3's vanilla and convention item tags (tools/data/survival-reference-26.3.json), then the mod's own files."""
    found = defaultdict(list)
    for tag, values in json.loads((ROOT / 'tools/data/survival-reference-26.3.json').read_text(encoding='utf-8'))['tags'].items():
        found[tag] += values
    for path in sorted((RES / 'data').glob('*/tags/item/**/*.json')):
        namespace = path.relative_to(RES / 'data').parts[0]
        key = namespace + ':' + path.relative_to(RES / 'data' / namespace / 'tags/item').with_suffix('').as_posix()
        data = json.loads(path.read_text(encoding='utf-8'))
        found[key] = ([] if data.get('replace') else found[key]) + [v if isinstance(v, str) else v.get('id', '') for v in data.get('values', [])]
    return found


TAGS = item_tags()


def expand(ref, seen=frozenset()):
    if isinstance(ref, dict):
        ref = ref.get('ingredient', ref.get('item', ref.get('id', '#' + ref['tag'] if 'tag' in ref else ref.get('items', ''))))
    if isinstance(ref, list):
        return set().union(*(expand(r, seen) for r in ref)) if ref else set()
    if not isinstance(ref, str) or not ref:
        return set()
    if not ref.startswith('#'):
        return {ref}
    if ref[1:] in seen or ref[1:] not in TAGS:
        return set()
    return set().union(*(expand(v, seen | {ref[1:]}) for v in TAGS[ref[1:]]))


def parse(path):
    """(consumed [(options, count)], consumed fluids, outputs [(item, count, chance)]) with every chanced output listed."""
    r = json.loads(path.read_text(encoding='utf-8'))
    kind, consumed, outputs = r.get('type', ''), [], []
    if isinstance(r.get('pattern'), list) and 'key' in r:
        for symbol in set(''.join(r['pattern'])) - {' '}:
            consumed.append((expand(r['key'][symbol]), ''.join(r['pattern']).count(symbol)))
    elif kind == 'minecraft:smithing_transform':
        consumed = [(expand(r[k]), 1) for k in ('template', 'base', 'addition') if k in r]
    else:
        for entry in (r.get('items') if isinstance(r.get('items'), list) else None) or r.get('ingredients') or []:
            consumed.append((expand(entry), entry.get('count', 1) if isinstance(entry, dict) and 'ingredient' in entry else 1))
        if 'ingredient' in r:
            consumed.append((expand(r['ingredient']), r.get('input_count', 1)))
    fluids = [f.get('fluid') for f in r.get('fluids') or [] if isinstance(f, dict) and not f.get('retained')]
    made = r.get('results') if isinstance(r.get('results'), list) else []
    made = [m if isinstance(m, dict) and 'result' not in m else (m or {}).get('result', m) for m in made]
    chances = r.get('result_chances') or []
    for i, out in enumerate(made + ([r['result']] if isinstance(r.get('result'), (str, dict)) else [])):
        item = out if isinstance(out, str) else out.get('id') or out.get('item')
        if item:
            outputs.append((item, 1 if isinstance(out, str) else out.get('count', 1), chances[i] if i < len(chances) else 1))
    for extra in r.get('byproducts') or []:
        out = extra.get('result', extra)
        outputs.append((out.get('id') or out.get('item'), out.get('count', 1), extra.get('chance', 1)))
    return consumed, fluids, outputs


RECIPES = {path.relative_to(RES / 'data').with_suffix('').as_posix(): parse(path)
           for path in sorted((RES / 'data').glob('*/recipe/**/*.json'))}


def formable_conversions():
    """Every recipe whose each slot can be filled with formable supplies and whose fluids are only water.

    Yields (recipe, essence spent forming the cheapest formable inputs, essence of the maximum output). Chanced outputs
    count whole: a lucky batch must not mint essence either (the rare-earth allocation rule)."""
    for name, (consumed, fluids, outputs) in RECIPES.items():
        if not consumed or not outputs or any(f not in FREE_FLUIDS for f in fluids):
            continue
        slots = [[essence(o) for o in options if o in FORMABLE] for options, _ in consumed]
        if any(not prices for prices in slots):
            continue
        spent = sum(min(prices) * n for prices, (_, n) in zip(slots, consumed))
        returned = sum(essence(item) * n for item, n, chance in outputs if chance > 0)
        yield name, spent, returned


# Machine-time conversions that return more essence than forming their inputs costs. They are recorded audit
# findings awaiting the owner's balance decision (docs/features/convenience-transmutation.md), not accepted balance:
# each needs a recipe-yield or reference-value change that this audit must not make on its own. None reaches Jugs or a
# metal/gem the tablet denies except the gravel sifter, whose cobblestone input is free from any cobblestone generator.
# recipe: (essence spent, maximum essence returned)
RECORDED_MINTING = {
    # 1 mud (reviewed decorative root: five essence) -> 4 clay balls at the 16-essence reference.
    'jugcraft/recipe/water_treatment/mud': (5, 64),
    # Sawmill: 1 log (32) -> 6 planks (8 each) + a chanced sawdust (fallback 1); a crafting grid makes 4 planks.
    **{f'jugcraft/recipe/sawing/{wood}_logs': (32, 49) for wood in
       ('acacia', 'birch', 'cherry', 'dark_oak', 'jungle', 'mangrove', 'oak', 'pale_oak', 'spruce',
        'aspen', 'bald_cypress', 'baobab', 'cedar', 'chestnut', 'tamarisk', 'walnut', 'willow', 'yew', 'yucca')},
    # The local walnut cutting recipes strip a 32-essence log/wood and return one sawdust (1).
    **{f'jugcraft/recipe/ecology/cutting/walnut_{part}': (32, 33) for part in ('log', 'wood')},
    # Sawmill: 1 plank (8) -> 3 sticks (4 each); a crafting grid makes 4 sticks from 2 planks.
    'jugcraft/recipe/sawing/planks': (8, 12),
    # Crusher: 1 cobblestone (1) -> 1 gravel (4).
    'jugcraft/recipe/crushing/cobblestone': (1, 4),
    # Sifter: 1 gravel (4) -> flint (4), 12% iron nugget and 8% tin nugget (256/9 each), all counted whole.
    'jugcraft/recipe/sifting/gravel': (4, Fraction(548, 9)),
    # Sifter: 1 coarse dirt (5/2) -> dirt (1), 15% cotton seeds (1) and 10% wheat seeds (16), counted whole.
    'jugcraft/recipe/sifting/coarse_dirt': (Fraction(5, 2), 18),
    # The combined catalogue derives cinder bricks and mud furnishings from
    # their recipes. Their 22 former fallback-price exceptions are now closed.
}


def collector_fuel_price(current, upgraded):
    """AlchemicalWorkshopBlockEntity.upgradeFuel: max(1, form(next) - dissolve(current)), forming rounds up."""
    return max(1, math.ceil(essence(upgraded)) - math.floor(essence(current)))


class TownShopEssenceTests(unittest.TestCase):
    def test_no_town_purchase_is_a_formable_supply(self):
        self.assertEqual(town_shops.loop_errors(), [])
        self.assertEqual(RUNTIME_SHOPS, town_shops.data(), 'shops.json is regenerated from tools/town_shops.py')
        for shop, info in RUNTIME_SHOPS.items():
            for offer in info['buys']:
                self.assertFalse(offer['item'] in FORMABLE, f"{shop} would pay Jugs for essence: {offer['item']}")
        # Ordinary farm, mob and mine output still sells (the 1 gold = 1 Jug anchor is unchanged).
        bought = {o['item']: (o['count'], o['price']) for o in RUNTIME_SHOPS['general']['buys']}
        self.assertEqual(bought['minecraft:gold_ingot'], (4, 4))
        self.assertEqual(len(bought), 28)

    def test_the_audited_formable_offers_are_reported(self):
        """Negative case: restoring any pre-audit offer fails the generator's loop check."""
        for offer in REMOVED_OFFERS:
            with self.subTest(offer=offer[0]):
                shops = deepcopy(town_shops.SHOPS)
                shops['general']['buys'].append(list(offer))
                errors = town_shops.loop_errors(shops)
                self.assertEqual(1, len(errors), errors)
                self.assertIn(offer[0], errors[0])
        # The check reads the tag: an item the tag does not list stays buyable.
        self.assertEqual([], town_shops.loop_errors(formable=set()))

    def test_essence_cannot_round_trip_through_the_town(self):
        """The reproduced loop, in exact numbers, and the invariant that closes it."""
        sold = {o['item']: Fraction(o['price'], o['count']) for info in RUNTIME_SHOPS.values() for o in info['sells']}
        # Essence a Jug buys at the town: the dearest return is the Curiosities jukebox (a diamond inside).
        best = max(essence(item) / price for item, price in sold.items())
        self.assertEqual(Fraction(688), best)
        # Pre-audit: 64 cobblestone (one essence each) sold for 1 Jug, so a Jug cost 64 essence; a 6-Jug bucket
        # dissolves for 768. 384 formed cobblestone -> 6 Jugs -> 1 bucket -> 768 essence: double, every lap.
        cobble_jug = 64 * essence('minecraft:cobblestone')
        self.assertEqual(64, cobble_jug)
        self.assertEqual(768, essence('minecraft:bucket'))
        self.assertEqual(2, essence('minecraft:bucket') / (6 * cobble_jug))
        for item, count, price in REMOVED_OFFERS:
            self.assertIn(item, FORMABLE)
        # Logs and white wool sat at 2,048 and 1,536 essence a Jug: no lap gain, but daylight collectors made Jugs.
        self.assertEqual(2048, 64 * essence('minecraft:oak_log'))
        self.assertEqual(1536, 32 * essence('minecraft:white_wool'))
        # Now nothing formable is bought, so no essence reaches Jugs through a shop at any price.
        bought = {o['item'] for info in RUNTIME_SHOPS.values() for o in info['buys']}
        self.assertFalse(bought & FORMABLE)

    def test_town_trades_cannot_profit_through_recipes(self):
        """Buy anything the town sells, process it with the mod's recipes, sell the product back: never a gain."""
        cost = {}
        for info in RUNTIME_SHOPS.values():
            for o in info['sells']:
                cost[o['item']] = min(cost.get(o['item'], Fraction(10 ** 9)), Fraction(o['price'], o['count']))
        for _ in range(32):
            changed = False
            for name, (consumed, fluids, outputs) in RECIPES.items():
                if not consumed or any(f not in FREE_FLUIDS for f in fluids):
                    continue
                slots = [[cost[o] for o in options if o in cost] for options, _ in consumed]
                if any(not prices for prices in slots):
                    continue
                spent = sum(min(prices) * n for prices, (_, n) in zip(slots, consumed))
                for item, n, chance in outputs:
                    if chance >= 1 and (item not in cost or spent / n < cost[item]):
                        cost[item] = spent / n
                        changed = True
            if not changed:
                break
        for info in RUNTIME_SHOPS.values():
            for o in info['buys']:
                if o['item'] in cost:
                    self.assertGreaterEqual(cost[o['item']], Fraction(o['price'], o['count']),
                                            f"{o['item']} is made from town goods for less than the town pays")

    def test_commission_orders_are_not_formable(self):
        source = (ROOT / 'src/main/java/io/github/jimbozoomer/jugcraft/town/TownCommissions.java').read_text(encoding='utf-8')
        orders = re.findall(r'new Order\("(\w+)", "[^"]*", "[^"]*", "([a-z_]+:[a-z_/]+)", (\d+), (\d+)\)', source)
        self.assertEqual(4, len(orders), orders)
        for order, item, count, reward in orders:
            self.assertNotIn(item, FORMABLE, f'Commission {order} would pay Jugs for essence')
            self.assertLessEqual(int(reward), 2, 'Hourly civic rewards stay modest at the gold peg')


class TransmutationOutputConversionTests(unittest.TestCase):
    def test_recorded_minting_conversions_keep_their_audited_numbers(self):
        found = {name: (spent, returned) for name, spent, returned in formable_conversions() if returned > spent}
        self.assertEqual(sorted(RECORDED_MINTING), sorted(found),
                         'A new conversion mints essence from formable supplies (or a recorded one changed): '
                         + str({k: (str(v[0]), str(v[1])) for k, v in found.items() if k not in RECORDED_MINTING}))
        for name, numbers in RECORDED_MINTING.items():
            self.assertEqual(numbers, found[name], name)

    def test_conversion_audit_detects_a_minting_recipe(self):
        """Negative case: the scan sees a formable input that returns more essence than it costs."""
        found = {name: (spent, returned) for name, spent, returned in formable_conversions()}
        self.assertEqual((5, 64), found['jugcraft/recipe/water_treatment/mud'])
        # A conserving conversion is scanned too and passes: dyed wool washes back to white wool, 48 for 48.
        self.assertEqual((48, 48), found['jugcraft/recipe/jimtafacts_industry/extract_red_wool'])

    def test_no_single_conversion_turns_formable_supplies_into_town_purchases(self):
        bought = {o['item'] for info in RUNTIME_SHOPS.values() for o in info['buys']}
        for name, (consumed, fluids, outputs) in RECIPES.items():
            if not consumed or any(f not in FREE_FLUIDS for f in fluids):
                continue
            if all(options & FORMABLE for options, _ in consumed):
                self.assertFalse({item for item, _, _ in outputs} & bought, name + ' turns formable supplies into Jugs')


class CollectorFuelUpgradeTests(unittest.TestCase):
    FUELS = ['minecraft:coal', 'jugcraft:alchemical_coal', 'jugcraft:mobius_fuel', 'jugcraft:aeternalis_fuel']

    def test_java_fuel_ladder_matches(self):
        source = (RES.parent / 'java/io/github/jimbozoomer/jugcraft/concordance/assay/AlchemicalWorkshopBlockEntity.java').read_text(encoding='utf-8')
        ladder = re.search(r'FUELS=\{([^}]*)\}', source).group(1)
        self.assertEqual(self.FUELS, re.findall(r'"([^"]+)"', ladder))

    def test_each_upgrade_pays_at_least_the_value_it_adds(self):
        for current, upgraded in zip(self.FUELS, self.FUELS[1:]):
            with self.subTest(upgrade=upgraded):
                price = collector_fuel_price(current, upgraded)
                self.assertGreaterEqual(essence(current) + price, essence(upgraded))
        # coal 128 + 393 -> alchemical coal 2081/4; + 601 -> mobius 2241/2; + 3385 -> aeternalis 4505.
        self.assertEqual([393, 601, 3385], [collector_fuel_price(a, b) for a, b in zip(self.FUELS, self.FUELS[1:])])

    def test_no_recipe_turns_a_fuel_into_more_essence(self):
        """Fuels only climb (crafting or the collector) and feed Dark Matter; nothing returns a lower tier for profit."""
        fuels = set(self.FUELS[1:]) | {fuel + '_block' for fuel in self.FUELS[1:]}
        audited = 0
        for name, (consumed, fluids, outputs) in RECIPES.items():
            if not any(options & fuels for options, _ in consumed):
                continue
            audited += 1
            spent = sum(min(essence(o) for o in options) * n for options, n in consumed)
            returned = sum(essence(item) * n for item, n, _ in outputs)
            self.assertLessEqual(returned, spent, name)
            self.assertFalse({item for item, _, _ in outputs} & ({'minecraft:coal'} | FORMABLE), name)
        self.assertGreaterEqual(audited, 8)


if __name__ == '__main__':
    unittest.main()
