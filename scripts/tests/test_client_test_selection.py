"""Client selection must discover every registered package and preserve all shards' coverage."""
import importlib.util
import json
from pathlib import Path
import unittest


ROOT = Path(__file__).resolve().parents[2]
spec = importlib.util.spec_from_file_location("select_client_tests", ROOT / "tools/select_client_tests.py")
selection = importlib.util.module_from_spec(spec)
spec.loader.exec_module(selection)


class ClientTestSelectionTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.entries = json.loads((ROOT / selection.MOD_JSON).read_text())["entrypoints"]["fabric-client-gametest"]
        cls.tests = selection.test_classes(cls.entries)

    def test_every_registered_source_is_found(self):
        for entry in self.entries:
            with self.subTest(entry=entry):
                self.assertTrue(self.tests[entry.rsplit(".", 1)[-1]]["source"].is_file())

    def test_companion_code_selects_its_runtime_regressions(self):
        shown = selection.showing("PeepoEntity", self.tests)
        self.assertIn("PeepoWorkSessionClientTests", shown)
        self.assertIn("PeepoCompanionClientTests", shown)
        self.assertIn("PeepoDeliveryClientTests", shown)

    def test_full_run_assigns_every_class_exactly_once(self):
        jobs = selection.share(set(self.tests), self.tests, 4)
        assigned = [name for _, names in jobs for name in names]
        self.assertCountEqual(assigned, self.tests)
        self.assertEqual(len(assigned), len(set(assigned)))
        # Keep room under the unchanged 30-minute timeout for setup/compilation.
        self.assertLess(max(seconds for seconds, _ in jobs), 25 * 60)
        self.assertNotEqual(
            next(i for i, (_, names) in enumerate(jobs) if "PeepoCompanionClientTests" in names),
            next(i for i, (_, names) in enumerate(jobs) if "BiomeClientGameTests" in names),
        )

    def test_unweighted_companion_uses_its_actual_source_length(self):
        name = "PeepoWorkSessionClientTests"
        old = selection.WEIGHTS.pop(name)
        try:
            seconds = selection.share({name}, self.tests, 1)[0][0]
            self.assertGreater(seconds, 8)
        finally:
            selection.WEIGHTS[name] = old


if __name__ == "__main__":
    unittest.main()
