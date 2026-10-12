import json,sys,unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
sys.path.insert(0,str(ROOT/'tools'))
import encyclopedia
class EncyclopediaCatalogTests(unittest.TestCase):
    def setUp(self):
        self.book=json.loads((ROOT/'src/main/resources/assets/jugcraft/encyclopedia/en_us.json').read_text(encoding='utf-8'))
        self.ids=json.loads((ROOT/'src/main/resources/data/jugcraft/encyclopedia/quests.json').read_text(encoding='utf-8'))['advancements']
    def test_every_goal_and_research_gate_has_a_real_server_advancement(self):
        self.assertLessEqual(len(self.ids),512)
        self.assertEqual(len(self.ids),len(set(self.ids)))
        for id in self.ids:self.assertTrue((ROOT/'src/main/resources/data/jugcraft/advancement'/f"{id.split(':')[1]}.json").exists(),id)
        for entry in self.book['entries']:
            if entry['gate']:self.assertIn(entry['gate'],self.ids)
        self.assertTrue(any(e['gate'] for e in self.book['entries'] if e['group']=='Magic'))
    def test_catalog_is_generated_from_current_content_without_duplicate_entries(self):
        self.assertEqual(self.book['entries'],encyclopedia.build()['entries'])
        ids=[e['id'] for e in self.book['entries']];self.assertEqual(len(ids),len(set(ids)))
        for e in self.book['entries']:
            self.assertTrue(e['page']['title']);self.assertTrue(e['page']['icon']);self.assertTrue(e['page']['text'])
    def test_new_acquisition_goals_cannot_grant_rewards_or_require_other_goals(self):
        for p in (ROOT/'src/main/resources/data/jugcraft/advancement').glob('encyclopedia_*.json'):
            data=json.loads(p.read_text(encoding='utf-8'))
            self.assertNotIn('rewards',data)
            self.assertEqual(data['criteria']['obtain']['trigger'],'minecraft:inventory_changed')
    def test_retained_acid_is_explicit_in_process_guide(self):
        entry=next(e for e in self.book['entries'] if e['id']=='process/chemical_reactor/absorb_sulfur_trioxide')
        self.assertIn('1000 mB Sulfuric Acid (retained; not consumed)',' '.join(entry['page']['text']))
if __name__=='__main__':unittest.main()
