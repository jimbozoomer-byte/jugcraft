"""Component recipe choices must keep shared cost and reward-route audits accurate."""
from pathlib import Path
import sys
import unittest
from unittest.mock import patch

sys.path.insert(0,str(Path(__file__).resolve().parents[2]/'tools'))


class ComponentYieldConnectionTests(unittest.TestCase):
    def test_tower_block_and_module_costs_follow_current_circuit_recipes(self):
        import tower_costs
        errors=[]
        tower_costs.check(errors.append)
        self.assertEqual(errors,[])

    def test_prepared_motor_reward_route_is_declared_and_requires_the_new_recipe(self):
        import check_mod_data as checker
        import concordance_alchemy as alchemy
        drop='jugcraft:cydonian_shard'
        products={'jugcraft:mendvetch','minecraft:sugar'}
        self.assertEqual(alchemy.PRODUCT_REWARD_ROUTES[drop],products)
        edges=checker._recipe_edges(False)
        self.assertEqual(checker._made_from({drop},scale=False)&products,products)
        without_motor=[edge for edge in edges if not ('jugcraft:electric_motor' in edge[1]
                and 'jugcraft:neodymium_magnet' in edge[0] and 'jugcraft:polymer_housing' in edge[0])]
        self.assertEqual(len(edges)-len(without_motor),1)
        with patch.object(checker,'_recipe_edges',return_value=without_motor):
            self.assertEqual(checker._made_from({drop},scale=False)&products,set())
