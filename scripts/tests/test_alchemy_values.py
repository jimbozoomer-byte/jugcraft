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

    def test_titanium_and_magnesium_are_priced(self):
        self.assertEqual(value('jugcraft:titanium_ingot'),768)
        self.assertEqual(value('jugcraft:titanium_sponge'),value('jugcraft:titanium_ingot'))
        self.assertEqual(value('jugcraft:titanium_plate'),value('jugcraft:titanium_ingot'))
        self.assertEqual(value('jugcraft:magnesium_ingot'),128)
        # Electrolysing magnesium chloride gives back the magnesium it holds, no more.
        self.assertGreaterEqual(value('jugcraft:dry_magnesium_chloride'),value('jugcraft:magnesium_ingot'))
        self.assertGreater(value('jugcraft:titanium_pickaxe'),3*value('jugcraft:titanium_ingot'))

    def test_ore_processing_makes_no_essence(self):
        # Each ore, raw ore, washed ore and dust is worth what its best processing makes (fluids taken as free), so no
        # crushing, washing, leaching, centrifuging or smelting step returns more than went in.
        import re
        ore=re.compile(r'(_ore$|^raw_|_dust$|^washed_|^crushed_)')
        types={'crushing','pulverizing','ore_washing','arc_smelting','jimta_thermal_centrifuging','jimta_extracting','chemical_reaction'}
        def item(x):
            if isinstance(x,dict): x=x.get('ingredient',x); x=x.get('item',x.get('id')) if isinstance(x,dict) else x
            return x if isinstance(x,str) and not x.startswith('#') else None
        checked=0
        for path in (ROOT/'src/main/resources/data/jugcraft/recipe').rglob('*.json'):
            r=json.loads(path.read_text(encoding='utf-8'));t=r.get('type','')
            if t.split(':')[-1] not in types and t not in ('minecraft:smelting','minecraft:blasting'):continue
            if 'ingredient' in r: ins=[(item(r['ingredient']),int(r.get('input_count',1)))]
            elif isinstance(r.get('items'),list): ins=[(item(e),e.get('count',1) if isinstance(e,dict) else 1) for e in r['items']]
            else: continue
            if len(ins)!=1 or ins[0][0] not in VALUES or not ore.search(ins[0][0].split(':')[1]):continue
            outs=[]
            for o in ([r['result']] if isinstance(r.get('result'),dict) else [])+[o for o in r.get('results',[]) or [] if isinstance(o,dict)]:
                outs.append((o.get('id'),Fraction(o.get('count',1))))
            for b in r.get('byproducts',[]) or []:
                res=b.get('result',b);outs.append((res.get('id'),Fraction(str(b.get('chance',1)))*res.get('count',1)))
            if not outs or any(o not in VALUES for o,_ in outs):continue
            got=sum(value(o)*n for o,n in outs);cost=value(ins[0][0])*ins[0][1]
            self.assertLessEqual(got,cost,path.relative_to(ROOT).as_posix());checked+=1
        self.assertGreater(checked,150)
        for metal,ingot in (('iron',256),('gold',2048),('copper',128)):
            self.assertGreaterEqual(value('minecraft:raw_'+metal),ingot)
            for ore_block in (metal+'_ore','deepslate_'+metal+'_ore'):
                self.assertGreaterEqual(value('minecraft:'+ore_block),value('minecraft:raw_'+metal))
        self.assertGreaterEqual(value('minecraft:ancient_debris'),value('minecraft:netherite_scrap'))
        self.assertGreaterEqual(value('minecraft:diamond_ore'),value('minecraft:diamond'))

    def test_pyrium_and_spent_fuel_give_back_no_more(self):
        # Arcane debris drops pyrium, which blasts into five ancient debris; a reactor's depleted rods break into spent
        # assemblies, nine of which centrifuge into four uranium dust and a plutonium pellet.
        self.assertGreaterEqual(value('jugcraft:jimta_spellbooks_pyrium_ingot'),5*value('minecraft:ancient_debris'))
        spent=value('jugcraft:spent_fuel_assembly')
        self.assertGreaterEqual(9*spent,4*value('jugcraft:uranium_dust')+value('jugcraft:jimta_plutonium_pellet'))
        for fuel in ('uranium','mox'):
            for name,n in (('simple',1),('dual',2),('quad',4)):
                self.assertGreaterEqual(value('jugcraft:jimta_'+fuel+'_rod_'+name+'_depleted'),n*spent)
                self.assertLess(value('jugcraft:jimta_'+fuel+'_rod_'+name+'_depleted'),value('jugcraft:jimta_uranium_rod_'+name))

    def test_fluid_buckets_hold_the_bucket_and_their_fluid(self):
        buckets=[k for k,v in VALUES.items() if v['source']=='local fluid accounting']
        self.assertGreater(len(buckets),50)
        for bucket in buckets:
            self.assertGreaterEqual(value(bucket),value('minecraft:bucket'),bucket)
        # A free fluid is just the bucket; one made from mined sulfur costs more.
        self.assertEqual(value('jugcraft:seawater_bucket'),value('minecraft:bucket'))
        self.assertGreater(value('jugcraft:sulfuric_acid_bucket'),value('minecraft:bucket'))
        self.assertEqual(value('jugcraft:jimta_pahoehoe_lava_bucket'),value('minecraft:lava_bucket'))
        # Oil sand washes into half a bucket of crude oil (eight oil globs a bucket) and its sand.
        self.assertGreaterEqual(value('jugcraft:oil_sand'),value('minecraft:sand')+(value('jugcraft:crude_oil_bucket')-value('minecraft:bucket'))/2)

    def test_leftover_metals_are_priced(self):
        # Provisional prices (2 November 2026), with the minerals that make them worth what they make.
        for item, price in {'tungsten_ingot': 1024, 'thallite_ingot': 256, 'ferrochromium_ingot': 512, 'cobalt_ingot': 512,
                            'lithium_carbonate': 128, 'rare_earth_oxide': 512, 'vanadium_pentoxide': 256}.items():
            self.assertEqual(value('jugcraft:' + item), price, item)
        self.assertGreaterEqual(value('jugcraft:lepidolite'), 2 * value('jugcraft:lithium_carbonate'))
        self.assertGreaterEqual(value('jugcraft:monazite'), 2 * value('jugcraft:rare_earth_oxide'))
        self.assertGreaterEqual(value('jugcraft:cydonian_shard'), value('jugcraft:rare_earth_oxide'))
        for mineral, metal in (('chromite_concentrate', 'ferrochromium_ingot'), ('cobalt_concentrate', 'cobalt_ingot')):
            self.assertGreaterEqual(2 * value('jugcraft:' + mineral) + value('jugcraft:coke'), 2 * value('jugcraft:' + metal))
        self.assertGreater(value('jugcraft:thallite_pickaxe'), 3 * value('jugcraft:thallite_ingot'))
        self.assertEqual(value('jugcraft:tungsten_block'), 9 * value('jugcraft:tungsten_ingot'))

    def test_fluorite_phosphate_and_salt_are_priced(self):
        # Provisional (2 November 2026); salt stays cheap, since seawater and waste water make it for nothing.
        self.assertEqual(value('jugcraft:fluorite'), 32)
        self.assertEqual(value('jugcraft:phosphate'), 8)
        self.assertEqual(value('jugcraft:salt'), 1)
        self.assertGreaterEqual(4 * value('jugcraft:bat_guano'), value('jugcraft:phosphate'))
        self.assertEqual(value('jugcraft:fluorite_block'), 9 * value('jugcraft:fluorite'))
        for ore, mineral, n in (('fluorite_ore', 'fluorite', 2), ('phosphate_ore', 'phosphate', 4), ('salt_ore', 'salt', 5)):
            self.assertGreaterEqual(value('jugcraft:' + ore), n * value('jugcraft:' + mineral), ore)
