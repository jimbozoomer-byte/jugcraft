"""Combined imported collections stay distinguishable without repeated suffixes."""
import sys
from pathlib import Path
import unittest

sys.path.insert(0, str(Path(__file__).resolve().parents[2] / "tools"))
import display_names as names


class DisplayNameTests(unittest.TestCase):
    def test_original_and_imported_names_remain_distinct_and_idempotent(self):
        values = {
            "scarecrow": "Scarecrow", "holidayworks_scarecrow": "Scarecrow",
            "homeworks_acacia_table": "Acacia Table", "furnitureworks_acacia_table": "Acacia Table",
            "twigworks_acacia_table": "Acacia Table",
            "factoryworks_fan": "Factory Block", "factoryworks_fan_on": "Factory Block",
            "jugtania_azulejo_0": "Azulejo", "jugtania_azulejo_1": "Azulejo",
            "twigworks_paper_lantern": "Paper Lantern", "twigworks_allium_paper_lantern": "Paper Lantern",
            "ars_additions_golden_chain": "Golden Chain", "lightworks_golden_chain": "Golden Chain",
        }
        lang = {"block.jugcraft." + item: value for item, value in values.items()}
        names.disambiguate(lang, values)
        self.assertFalse(names.duplicates(lang, values))
        self.assertEqual("Scarecrow", lang["block.jugcraft.scarecrow"])
        self.assertEqual("Running Factory Fan", lang["block.jugcraft.factoryworks_fan_on"])
        self.assertEqual("Azulejo Tile II", lang["block.jugcraft.jugtania_azulejo_1"])
        self.assertEqual("Allium Paper Lantern", lang["block.jugcraft.twigworks_allium_paper_lantern"])
        before = dict(lang)
        self.assertEqual(0, names.disambiguate(lang, values))
        self.assertEqual(before, lang)


if __name__ == "__main__":
    unittest.main()
