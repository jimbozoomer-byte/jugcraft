"""Server game-test fixtures release what they hold, heavy fixtures run alone, and benchmarks stay opt-in.

Static checks of src/gametest and build.gradle (docs/features/server-load-benchmark.md, "Full-suite memory" and
"Opt-in workload benchmark"); they read source text only and need no Minecraft build.
"""
import json
import re
from pathlib import Path
import unittest


ROOT = Path(__file__).resolve().parents[2]
GAMETEST = ROOT / "src/gametest/java"
RESOURCES = ROOT / "src/gametest/resources"
TEST_PACKAGE = GAMETEST / "io/github/jimbozoomer/jugcraft/test"

# Classes that join a player themselves and keep it on purpose, with the reason.
OWN_PLAYER_LIFETIME = {
    # The harness itself: it joins survivors and rejoins identities, and tracks them.
    "TestPlayers.java": "the fixture harness",
    # The opt-in Concordance load cards measure what their one player is sent; each runs alone in its own server.
    "ConcordanceWorkshopLoadGameTests.java": "opt-in load card, alone in its own server",
    "ConcordancePactHallLoadGameTests.java": "opt-in load card, alone in its own server",
}


def server_test_sources():
    for path in sorted(GAMETEST.rglob("*.java")):
        # Client game tests run in their own JVM per shard, against an integrated server.
        if "Client" in path.name:
            continue
        yield path


def statements_after(text, index, count=4):
    """The text of the next few statements after the one containing index."""
    end = index
    for _ in range(count):
        end = text.find(";", end + 1)
        if end < 0:
            return text[index:]
    return text[index:end + 1]


def camel_to_snake(name):
    # Fabric's TestAnnotationLocator.camelToSnake.
    return re.sub(r"([a-z])([A-Z])", r"\1_\2", name).lower()


def environment_file(environment):
    namespace, path = environment.split(":", 1)
    return RESOURCES / "data" / namespace / "test_environment" / (path + ".json")


class DirectJoinFixturesAreTracked(unittest.TestCase):
    def test_every_server_fixture_join_is_tracked(self):
        untracked = []
        for path in server_test_sources():
            if path.name in OWN_PLAYER_LIFETIME:
                continue
            text = path.read_text(encoding="utf-8")
            for match in re.finditer(r"\.placeNewPlayer\(", text):
                if "TestPlayers.track(" not in statements_after(text, match.start()):
                    line = text.count("\n", 0, match.start()) + 1
                    untracked.append(f"{path.relative_to(ROOT)}:{line}")
        self.assertEqual(untracked, [], "A fixture that joins a player itself must hand it to TestPlayers.track right "
                         "after placeNewPlayer, so it leaves (and its transport closes) when its test ends")

    def test_known_direct_join_helpers_are_covered(self):
        # The helpers about 180 server tests share; each was left in the game for the whole run before 12 November 2026.
        helpers = ["test/JimtaRobotGameTests.java", "test/ConnectedAlchemyGameTests.java", "test/AlchemyPlayabilityGameTests.java",
                   "test/AlchemyArtifactGameTests.java", "test/WildenStalkerGameTests.java", "test/MenagerieGameTests.java",
                   "test/TownGameTests.java", "town/LivingCityCommissionGameTests.java", "finance/ServerDefenseGameTests.java",
                   "finance/ClaimCombatGameTests.java"]
        for helper in helpers:
            with self.subTest(helper=helper):
                text = (GAMETEST / "io/github/jimbozoomer/jugcraft" / helper).read_text(encoding="utf-8")
                self.assertIn("placeNewPlayer(", text)
                self.assertIn("TestPlayers.track(", text)

    def test_allow_list_names_real_files(self):
        for name in OWN_PLAYER_LIFETIME:
            with self.subTest(name=name):
                self.assertTrue((TEST_PACKAGE / name).is_file())


class StandInTrackersAreReleased(unittest.TestCase):
    def test_sweep_runs_on_the_server_tick(self):
        text = (TEST_PACKAGE / "MockPlayerChannels.java").read_text(encoding="utf-8")
        self.assertIn("releaseAbsentStandIns(server)", text)
        self.assertIn("instanceof FakePlayer", text)
        self.assertIn("list.getPlayer(entry.getKey()) != null", text, "an online identity's tracker is never released")
        # Minecraft 26.3 names the advancement-listener cleanup clearTriggers.
        self.assertIn("entry.getValue().clearTriggers();", text)


class EnvironmentsExist(unittest.TestCase):
    def test_every_named_environment_has_a_definition(self):
        missing = []
        for path in GAMETEST.rglob("*.java"):
            text = path.read_text(encoding="utf-8")
            for match in re.finditer(r'environment\s*=\s*"([a-z0-9_.-]+:[a-z0-9_/.-]+)"', text):
                environment = match.group(1)
                if environment.startswith("minecraft:"):
                    continue
                file = environment_file(environment)
                if not file.is_file():
                    missing.append(f"{path.name}: {environment}")
                else:
                    definition = json.loads(file.read_text(encoding="utf-8"))
                    self.assertIn("type", definition, file)
        # Constants: the workload benchmark names its environment through WorkloadBenchmark.ENVIRONMENT.
        benchmark = (TEST_PACKAGE / "WorkloadBenchmark.java").read_text(encoding="utf-8")
        constant = re.search(r'ENVIRONMENT = "([^"]+)"', benchmark).group(1)
        if not environment_file(constant).is_file():
            missing.append(f"WorkloadBenchmark.ENVIRONMENT: {constant}")
        self.assertEqual(missing, [])


class HeavyNuclearFixturesRunAlone(unittest.TestCase):
    def test_each_whole_plant_build_has_an_environment_of_its_own(self):
        text = (TEST_PACKAGE / "NuclearPlantGameTests.java").read_text(encoding="utf-8")
        methods = list(re.finditer(r"@GameTest(\([^)]*\))?\s*public void (\w+)\(", text))
        environments = {}
        for number, method in enumerate(methods):
            body = text[method.end():methods[number + 1].start() if number + 1 < len(methods) else len(text)]
            if "PlantBuilder.build(" not in body:
                continue
            annotation = method.group(1) or ""
            found = re.search(r'environment\s*=\s*"([^"]+)"', annotation)
            self.assertIsNotNone(found, f"{method.group(2)} builds whole plants but shares the default batch")
            environments[method.group(2)] = found.group(1)
        self.assertEqual(len(environments), 6, environments)
        self.assertEqual(len(set(environments.values())), len(environments), "one environment, so one batch, each")
        for environment in environments.values():
            self.assertTrue(environment_file(environment).is_file(), environment)


class BenchmarksAreOptIn(unittest.TestCase):
    def test_workload_benchmark_passes_at_once_unless_selected(self):
        text = (TEST_PACKAGE / "ServerLoadGameTests.java").read_text(encoding="utf-8")
        method = text[text.index("public void workloadBenchmark("):]
        method = method[:method.index("new WorkloadBenchmark(helper).begin();")]
        self.assertRegex(method, r"if \(WorkloadBenchmark\.selected\(\)\.isEmpty\(\)\) \{\s*helper\.succeed\(\);\s*return;")
        benchmark = (TEST_PACKAGE / "WorkloadBenchmark.java").read_text(encoding="utf-8")
        self.assertIn('String value = System.getProperty(PROPERTY);', benchmark)
        self.assertIn('PROPERTY = "jugcraft.benchmark"', benchmark)

    def test_benchmark_id_matches_fabric_naming_and_the_gradle_filter(self):
        benchmark = (TEST_PACKAGE / "WorkloadBenchmark.java").read_text(encoding="utf-8")
        declared = re.search(r'static final String ID = "([^"]+)"', benchmark).group(1)
        expected = "jugcraft-test:" + camel_to_snake("ServerLoadGameTests_workloadBenchmark")
        self.assertEqual(declared, expected)
        gradle = (ROOT / "build.gradle").read_text(encoding="utf-8")
        self.assertIn("-Dfabric-api.gametest.filter=" + expected, gradle)
        self.assertIn('providers.gradleProperty("jugcraftBenchmark")', gradle)

    def test_frame_benchmark_is_opt_in_when_present(self):
        path = TEST_PACKAGE / "FrameBenchmarkClientGameTests.java"
        if not path.is_file():
            self.skipTest("no client frame benchmark in this tree")
        text = path.read_text(encoding="utf-8")
        self.assertRegex(text, r"if \(!selected\(\)\) \{\s*return;")
        self.assertIn('PROPERTY = "jugcraft.frameBenchmark"', text)
        gradle = (ROOT / "build.gradle").read_text(encoding="utf-8")
        self.assertIn('providers.gradleProperty("jugcraftFrameBenchmark")', gradle)


class GradleBudgetIsUnchangedByDefault(unittest.TestCase):
    def test_default_heaps_and_exhaustion_exit_remain(self):
        gradle = (ROOT / "build.gradle").read_text(encoding="utf-8")
        self.assertIn('maxHeapSize = "6G"', gradle)
        self.assertIn('jvmArgs "-XX:+ExitOnOutOfMemoryError"', gradle)
        # Every local override is behind an explicit property.
        for name in ["jugcraftGameTestHeap", "jugcraftHeapDump", "jugcraftGcLog", "jugcraftBenchmark"]:
            with self.subTest(name=name):
                self.assertIn(f'providers.gradleProperty("{name}").getOrElse("")', gradle)


if __name__ == "__main__":
    unittest.main()
