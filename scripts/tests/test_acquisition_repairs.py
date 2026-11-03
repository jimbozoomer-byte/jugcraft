"""Native acquisition data stays reproducible and retains progression prerequisites."""
import json
from pathlib import Path
import sys
import unittest
from unittest.mock import patch
ROOT=Path(__file__).resolve().parents[2]
sys.path.insert(0,str(ROOT/'tools'))
import acquisition_repairs as repairs

class AcquisitionRepairTests(unittest.TestCase):
    def test_generated_resources_match_committed_files(self):
        for shared in (False, True):
            with self.subTest(shared_writer=shared):
                written={}
                capture=lambda p,v:written.update({p:v})
                if shared:
                    repairs.generate(capture)
                else:
                    with patch.object(repairs,'write',side_effect=capture):
                        repairs.generate()
                self.assertEqual(49,len(written))
                from newest_acquisition_recipes import build
                reviewed={repairs.RES/'data/jugcraft/recipe'/(name+'.json') for name in build()}
                self.assertFalse(set(written) & reviewed, 'Upstream recipe IDs have one authoritative writer')
                for path,value in written.items():
                    self.assertEqual(value,json.loads(path.read_text()),str(path))
    def test_divine_pact_requires_boss_materials(self):
        recipe=json.loads((repairs.RES/'data/jugcraft/recipe/acquisition/divine_pact.json').read_text())
        self.assertEqual(set(recipe['key']),set(''.join(recipe['pattern']))-{' '})
        self.assertIn('minecraft:nether_star',recipe['key'].values())
        self.assertIn('minecraft:dragon_breath',recipe['key'].values())
    def test_special_armor_and_water_recipes_keep_source_patterns(self):
        from newest_jugtania import D,mapped,recipe,read
        for name in ('terrasteel_helmet','terrasteel_chestplate','terrasteel_leggings','terrasteel_boots','water_rod'):
            source=read(D/'recipes'/(name+'.json'))
            source['type']='jugcraft:botanical_water_shaped' if name=='water_rod' else 'jugcraft:botanical_armor_upgrade'
            self.assertEqual(recipe(mapped(source)),read(repairs.RES/'data/jugcraft/recipe/jugtania'/(name+'.json')))

if __name__=='__main__':unittest.main()
