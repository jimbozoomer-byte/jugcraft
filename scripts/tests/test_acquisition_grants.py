"""First-win audit recognizes real grants, never a differently named or unconditional reward."""
from pathlib import Path
import sys
import unittest

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / 'tools'))
from check_mod_data import _first_win_grants


class FirstWinGrantTests(unittest.TestCase):
    def test_existing_method_and_constant_grants(self):
        for package, cls, item in (
            ('vesperine', 'VesperineLoot', 'vesper_scythe'),
            ('tatterlace', 'TatterlaceLoot', 'needle_rapier'),
            ('abbot', 'AbbotLoot', 'glass_reliquary'),
        ):
            source = (ROOT / 'src/main/java/io/github/jimbozoomer/jugcraft/lair' / package / (cls + '.java')).read_text(encoding='utf-8')
            with self.subTest(item=item):
                self.assertTrue(_first_win_grants(source, item))
                self.assertFalse(_first_win_grants(source, 'wrong_reward'))
                self.assertFalse(_first_win_grants(source.replace('firstKill(player)', 'false'), item))

    def test_unconditional_and_wrong_item_grants_are_rejected(self):
        self.assertFalse(_first_win_grants('loot.add(new ItemStack(Items.GLASS_RELIQUARY));', 'glass_reliquary'))
        self.assertFalse(_first_win_grants('if (firstKill(player)) { loot.add(new ItemStack(Items.GLASS)); }', 'glass_reliquary'))


if __name__ == '__main__':
    unittest.main()
