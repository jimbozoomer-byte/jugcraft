"""Data-level conservation checks for the universal catalogue (runtime transactions have game tests)."""
from fractions import Fraction
import json
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[2]
VALUES=json.loads((ROOT/'src/main/resources/data/jugcraft/alchemy/values.json').read_text(encoding='utf-8'))['items']

def value(item):
    v=VALUES[item];return Fraction(v['numerator'],v['denominator'])

class AlchemyValueTests(unittest.TestCase):
    def test_overworld_quartz_does_not_multiply_essence_by_mining_or_crushing(self):
        for ore in ('quartz_ore','deepslate_quartz_ore'):
            self.assertGreaterEqual(value('jugcraft:'+ore),4*value('minecraft:quartz'))
    def test_storage_crate_routes_cannot_create_essence(self):
        metals = ('bronze', 'steel', 'invar', 'stainless_steel', 'aluminum', 'titanium', 'tungsten')
        previous = 'minecraft:chest'
        for tier, metal in enumerate(metals):
            crate = 'jugcraft:' + metal + '_crate'
            plate = value('jugcraft:' + metal + '_plate')
            self.assertLessEqual(value(crate), value(previous) + 4 * plate)
            if tier:
                kit = 'jugcraft:' + metal + '_storage_crate_upgrade'
                self.assertLessEqual(value(crate), value('minecraft:chest') + 8 * plate)
                self.assertLessEqual(value(kit), value('minecraft:iron_nugget') + 4 * plate)
                self.assertLessEqual(value(crate), value(previous) + value(kit))
            previous = crate

    def test_historical_roots_are_not_the_old_reduced_values(self):
        for item,expected in {'diamond':8192,'gold_ingot':2048,'iron_ingot':256,'coal':128,'oak_log':32,'oak_planks':8}.items():
            self.assertEqual(value('minecraft:'+item),expected,item)

    def test_appended_mod_tags_preserve_vanilla_wood(self):
        for name in ('oak','spruce','birch','jungle','acacia','dark_oak','mangrove','cherry'):
            self.assertEqual(value('minecraft:'+name+'_log'),32)
            self.assertEqual(value('minecraft:'+name+'_planks'),8)

    def test_block_and_nugget_round_trips(self):
        for metal in ('iron','gold','copper'):
            unit=value('minecraft:'+metal+'_ingot')
            self.assertEqual(value('minecraft:'+metal+'_block'),9*unit)
            if 'minecraft:'+metal+'_nugget' in VALUES:self.assertEqual(9*value('minecraft:'+metal+'_nugget'),unit)

    def test_filled_buckets_cannot_create_a_cheaper_empty_shell(self):
        for item in ('water_bucket','lava_bucket','milk_bucket','cod_bucket','powder_snow_bucket'):
            self.assertGreaterEqual(value('minecraft:'+item),value('minecraft:bucket'))

    def test_all_authored_item_types_have_explicit_records(self):
        for p in (ROOT/'src/main/resources/assets/jugcraft/items').glob('*.json'):
            self.assertIn('jugcraft:'+p.stem,VALUES,p.name)

    def test_prices_are_positive_bounded_rationals_and_provenanced(self):
        for item,v in VALUES.items():
            self.assertGreater(value(item),0,item);self.assertLessEqual(value(item),1_000_000_000,item)
            self.assertTrue(v['source'],item)

    def test_matter_uses_actual_local_recipe_inputs(self):
        self.assertEqual(value('jugcraft:dark_matter'),8*value('jugcraft:aeternalis_fuel')+value('minecraft:diamond_block'))
        self.assertEqual(value('jugcraft:klein_star_1'),value('jugcraft:dark_matter')+4*value('minecraft:gold_ingot')+4*value('minecraft:lapis_lazuli'))

    def test_smithing_upgrades_pay_for_base_addition_and_template(self):
        for result,base,addition in [('gem_helmet','red_matter_helmet','minecraft:diamond_block'),
                                     ('condenser_mk2','condenser_mk1','jugcraft:dark_matter'),
                                     ('klein_star_6','klein_star_5','jugcraft:dark_matter')]:
            self.assertEqual(value('jugcraft:'+result),value('jugcraft:'+base)+value(addition)+value('jugcraft:high_covalence_dust'))
            self.assertTrue(VALUES['jugcraft:'+result]['source'].startswith('recipe '))

    def test_chemistry_remainders_and_molding_do_not_create_essence(self):
        self.assertGreaterEqual(value('jugcraft:glucose_syrup'),
                                3*value('minecraft:sugar')+value('minecraft:glass_bottle'))
        self.assertGreaterEqual(value('jugcraft:borax'),2*value('jugcraft:boric_acid'))
        self.assertGreaterEqual(2*value('jugcraft:silicone'),3*value('jugcraft:gasket'))
        for stock in ('plastic_pellets','pvc_resin','hdpe_pellets'):
            self.assertGreaterEqual(value('jugcraft:'+stock),2*value('jugcraft:plastic_sheet'))

    def test_aged_wood_has_consistent_shaped_values(self):
        for wood in ('pine','maple'):
            plank=value('jugcraft:aged_'+wood+'_planks')
            self.assertEqual(plank,value('jugcraft:'+wood+'_planks'))
            self.assertEqual(6*value('jugcraft:aged_'+wood+'_slab'),3*plank)
            self.assertEqual(4*value('jugcraft:aged_'+wood+'_stairs'),6*plank)

    def test_tablet_outputs_have_real_prices(self):
        """Every tablet output is priced from recipes or references, never the one-essence fallback (docs/features/tablet-decorations.md)."""
        tag=json.loads((ROOT/'src/main/resources/data/jugcraft/tags/item/tablet_outputs.json').read_text(encoding='utf-8'))['values']
        for entry in tag:
            item=entry if isinstance(entry,str) else entry['id']
            self.assertIn(item,VALUES,item)
            self.assertFalse(VALUES[item]['source'].startswith('fallback'),item)
        names={e if isinstance(e,str) else e['id'] for e in tag}
        for item in ('quartz_block','quartz_slab','copper_block','copper_bulb','exposed_copper_bulb','waxed_cut_copper'):
            self.assertNotIn('minecraft:'+item,names,item)

    def test_manipulation_essence_makes_no_calcite_or_tuff_from_tablet_stone(self):
        essence=value('jugcraft:nouveau_manipulation_essence')
        self.assertLessEqual(8*value('minecraft:calcite'),8*value('minecraft:tuff')+essence)
        self.assertLessEqual(8*value('minecraft:tuff'),8*value('minecraft:deepslate')+essence)

    def test_sulfur_dust_makes_no_gunpowder_profit(self):
        self.assertLessEqual(value('minecraft:gunpowder'),2*value('jugcraft:cavern_sulfur_dust')+value('minecraft:gravel'))

    def test_decorative_woods_cost_like_vanilla(self):
        for wood in ('landscape_maple','landscape_redwood','cavern_pewen'):
            self.assertEqual(value('jugcraft:'+wood+'_planks'),value('minecraft:oak_planks'),wood)

    def test_magic_and_archwood_saplings_cost_more(self):
        self.assertEqual(value('jugcraft:landscape_magic_sapling'),64)
        for colour in ('blue','green','purple','red'):
            self.assertEqual(value('jugcraft:nouveau_'+colour+'_archwood_sapling'),128,colour)
        self.assertEqual(value('jugcraft:landscape_magic_log'),64)
        self.assertEqual(value('jugcraft:landscape_magic_planks'),16)
        self.assertEqual(value('jugcraft:nouveau_red_archwood_log'),128)
        self.assertEqual(value('jugcraft:nouveau_archwood_planks'),32)
        # Manipulation essence turns one archwood sapling into the next: it must not make essence.
        self.assertLessEqual(value('jugcraft:nouveau_blue_archwood_sapling'),value('jugcraft:nouveau_manipulation_essence')+value('jugcraft:nouveau_red_archwood_sapling'))

    def test_metal_chain_is_priced(self):
        """Steel is iron and coke, plates are pressed from ingots and brass is three copper and a zinc (docs/features/tablet-decorations.md)."""
        self.assertEqual(value('jugcraft:coke'),value('minecraft:coal'))
        self.assertEqual(value('jugcraft:steel_ingot'),value('minecraft:iron_ingot')+value('jugcraft:coke'))
        self.assertEqual(value('jugcraft:steel_plate'),value('jugcraft:steel_ingot'))
        self.assertEqual(value('jugcraft:zinc_ingot'),value('minecraft:copper_ingot'))
        self.assertEqual(4*value('jugcraft:brass_ingot'),3*value('minecraft:copper_ingot')+value('jugcraft:zinc_ingot'))
        self.assertEqual(9*value('jugcraft:steel_plate'),value('jugcraft:jimta_dense_steel_plate'))
        self.assertEqual(3*value('jugcraft:silver_wire'),value('jugcraft:silver_ingot'))

    def test_mud_bricks_follow_mud(self):
        self.assertEqual(value('minecraft:mud'),5)
        self.assertEqual(value('minecraft:packed_mud'),value('minecraft:mud')+value('minecraft:wheat'))
