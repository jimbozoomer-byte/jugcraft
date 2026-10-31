"""Starter progression must bootstrap from vanilla clay/metals and retain powered yields."""
import sys
import json
import unittest
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parents[2]/'tools'))
import starter_workshop as workshop
from audit_survival_recipes import build, reachable

class StarterWorkshopTests(unittest.TestCase):
    def test_cable_routes_reward_processing(self):
        recipes=Path(__file__).resolve().parents[2]/'src/main/resources/data/jugcraft/recipe'
        for name, count in [('copper_cable',4),('starter_workshop/insulated_copper_cable',8),('insulated_copper_cable',16)]:
            result=json.loads((recipes/(name+'.json')).read_text())['result']
            self.assertEqual({'id':'jugcraft:copper_cable','count':count},result,name)

    def test_approved_recipes_only_and_equal_material_yields(self):
        rows=dict(workshop.processes())
        original={f'{metal}_plate' for metal in ['iron','copper','tin','zinc','lead','bronze','brass']} | {
            'copper_wire','basic_circuit','bronze_ingot','brass_ingot','solder_ingot',
            'fire_lining','fire_crucible','fire_insulators','silicon'}
        sanding={'sands_smooth_sandstone','sands_smooth_red_sandstone'}
        self.assertEqual(original | sanding,set(rows))
        self.assertEqual(16,len(original))
        self.assertEqual(9,sum(rows[name]['station']==0 for name in original))
        self.assertEqual(11,sum(r['station']==0 for r in rows.values()))
        self.assertEqual((200,4),(rows['bronze_ingot']['time'],rows['bronze_ingot']['result']['count']))
        self.assertEqual((120,2),(rows['solder_ingot']['time'],rows['solder_ingot']['result']['count']))
        self.assertEqual(100,rows['copper_wire']['time'])
        self.assertEqual(3,rows['copper_wire']['result']['count'])
        self.assertEqual(200,rows['basic_circuit']['time'])
        self.assertTrue(all(r['time']==100 for n,r in rows.items() if n.endswith('_plate')))
        self.assertEqual(2,rows['fire_insulators']['ingredients'][0]['count'])
        self.assertFalse(any(c['feature']=='drones' for c in rows['fire_insulators']['fabric:load_conditions']))
        silicon=rows['silicon']
        self.assertTrue(silicon['high_heat'])
        self.assertEqual((2,200,1),(silicon['station'],silicon['time'],silicon['result']['count']))
        self.assertEqual([{'ingredient':'minecraft:quartz','count':1},{'ingredient':'minecraft:charcoal','count':1}],silicon['ingredients'])

    def test_approved_sanding_is_one_for_one_with_a_retained_tool(self):
        rows=dict(workshop.processes())
        for name,feed,output in [
            ('sands_smooth_sandstone','cut_sandstone','smooth_sandstone'),
            ('sands_smooth_red_sandstone','cut_red_sandstone','smooth_red_sandstone'),
        ]:
            with self.subTest(recipe=name):
                recipe=rows[name]
                self.assertEqual('jugcraft:starter_working',recipe['type'])
                self.assertEqual((0,200,False),(recipe['station'],recipe['time'],recipe['heated']))
                self.assertFalse(recipe.get('high_heat',False))
                self.assertEqual([{'ingredient':'minecraft:'+feed,'count':1}],recipe['ingredients'])
                self.assertEqual({'id':'minecraft:'+output,'count':1},recipe['result'])
                # The separate tool field is retained by WorkshopRecipe/WorkLane;
                # it must never become an ingredient consumed with every block.
                self.assertEqual({'id':'jugcraft:garnet_sanding_block'},recipe['tool'])
                self.assertNotIn('jugcraft:garnet_sanding_block',
                    [part['ingredient'] for part in recipe['ingredients']])
                self.assertEqual({'machines','mineral_sands'},
                    {condition['feature'] for condition in recipe['fabric:load_conditions']})

    def test_bootstrap_excludes_powered_stations_and_requires_installed_parts(self):
        report=build()
        edges=[e for e in report['edges'] if '/starter_workshop/' in e['source'] or e['source'].endswith('/iron_shaft.json')]
        seeds={i:'ordinary feed' for i in ['minecraft:crafting_table','minecraft:oak_planks','minecraft:cobblestone',
            'minecraft:iron_ingot','minecraft:copper_ingot','minecraft:stick','minecraft:bricks','minecraft:furnace','minecraft:clay_ball',
            'jugcraft:tin_ingot','jugcraft:zinc_ingot','jugcraft:lead_ingot','minecraft:quartz','minecraft:charcoal','minecraft:leather']}
        reached=reachable(edges,seeds)
        for name in [*workshop.NAMES,'bronze_ingot','brass_ingot','solder_ingot','basic_circuit','copper_cable','ceramic_insulator']:
            self.assertIn('jugcraft:'+name,reached,name)
        without_lining=[e for e in edges if 'jugcraft:basic_ceramic_lining' not in e['outputs']]
        self.assertNotIn('jugcraft:brass_ingot',reachable(without_lining,seeds))
        self.assertIn('jugcraft:silicon',reached)
        for part in ['crucible_heat_insert','crucible_bellows']:
            without=[e for e in edges if 'jugcraft:'+part not in e['outputs']]
            self.assertNotIn('jugcraft:silicon',reachable(without,seeds))
