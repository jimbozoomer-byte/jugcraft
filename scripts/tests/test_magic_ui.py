"""The magic interfaces' generator (tools/magic_ui.py; docs/features/magic-interfaces.md): the owner's kit as runtime art
and the short words the magic screens draw. The library is in the repository, so this runs offline; nothing in it is
executed and no original is written (each adapter is given the library's bytes)."""
import copy
import io
import json
from pathlib import Path
import sys
import unittest

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))
import magic_ui as mu  # noqa: E402
import owner_art  # noqa: E402

LANG = json.loads((ROOT / "src" / "main" / "resources" / "assets" / "jugcraft" / "lang" / "en_us.json").read_text(encoding="utf-8"))


def made(target):
    """What tools/owner_art.py writes for {target}, as an image (from the library, in memory)."""
    maker = mu.builder(target) if target in mu.BUILT else mu.adapter(target)
    return maker(mu.read_library)


class WordsTests(unittest.TestCase):
    def test_positional_placeholders_take_the_servers_arguments_by_position(self):
        self.assertEqual(mu.render("%1$s / %3$s days", (1, "Buer's Offerings", 7, 4)), "1 / 7 days")
        self.assertEqual(mu.render("%4$s ahead", (1, "x", 7, 4)), "4 ahead")

    def test_a_bare_placeholder_takes_the_next_argument_in_order(self):
        # Critique 3: why glances must not use it ("1 per Buer's Offerings days").
        self.assertEqual(mu.render("%s per %s days", (1, "Buer's Offerings", 7, 4)), "1 per Buer's Offerings days")
        self.assertEqual(mu.placeholders("%s per %s days"), (0, True))
        self.assertEqual(mu.placeholders("%1$s / %3$s days"), (3, False))
        self.assertEqual(mu.render("100%% of %1$s", ("it",)), "100% of it")

    def test_words_are_tokens_with_a_letter_or_digit(self):
        self.assertEqual(mu.nwords("06:00–18:00"), 1)
        self.assertEqual(mu.nwords("1 / 7 days · 4 ahead"), 5)

    def test_glance_keys_follow_the_server_keys(self):
        self.assertEqual(mu.glance_key("pact.term.upkeep"), "screen.jugcraft.pact.glance.term.upkeep")
        self.assertEqual(mu.glance_key("pact.term.upkeep", "cap"), "screen.jugcraft.pact.glance.term.upkeep.cap")
        self.assertEqual(mu.glance_key("garden_pact.obligation"), "screen.jugcraft.pact.glance.garden_pact.obligation")
        self.assertEqual(mu.glance_key("roster.next.offering"), "screen.jugcraft.roster.glance.next.offering")
        self.assertEqual(mu.card_key("pact.missing.owner"), "screen.jugcraft.pact.glance.card.missing.owner")
        self.assertEqual(mu.card_key("pact.strain_tending.missing"), "screen.jugcraft.pact.glance.card.strain_tending.missing")

    def test_every_glance_renders_its_english_within_its_limit(self):
        for server, glances in mu.GLANCES.items():
            for suffix, pattern, args, expected, kind in glances:
                with self.subTest(server=server, suffix=suffix):
                    self.assertEqual(mu.render(pattern, args), expected)
                    self.assertFalse(mu.placeholders(pattern)[1], "a glance names its arguments by position")
                    self.assertLessEqual(mu.nwords(expected), mu.LIMITS[kind])

    def test_the_short_labels_keep_to_their_limits(self):
        for name, (text, kind) in mu.SHORT.items():
            with self.subTest(name=name):
                self.assertLessEqual(mu.nwords(mu.render(text, mu.SHORT_SAMPLES.get(name, ()))), mu.LIMITS[kind])

    def test_lang_entries_write_every_glance_card_and_label(self):
        lang = {}
        mu.lang_entries(lang)
        self.assertEqual(lang["screen.jugcraft.pact.glance.term.hours"], "%1$s–%2$s")
        self.assertEqual(lang["screen.jugcraft.pact.glance.card.missing.observation"], "Observe a star first")
        self.assertEqual(lang["screen.jugcraft.pact.short.standing.hint"], "The same for every spirit")
        self.assertEqual(lang["screen.jugcraft.roster.glance.task.none"], "Nothing now")
        count = sum(len(glances) for glances in mu.GLANCES.values()) + len(mu.CARDS) + len(mu.SHORT)
        self.assertEqual(len(lang), count)

    def test_marks_follow_the_shape_of_the_server_key(self):
        self.assertEqual(mu.mark("pact.missing.owner"), "lock")
        self.assertEqual(mu.mark("pact.strain_tending.missing"), "lock")
        self.assertEqual(mu.mark("pact.row.state.active"), "tick")
        self.assertEqual(mu.mark("pact.row.state.upkeep_due"), "hourglass")
        self.assertEqual(mu.mark("pact.row.state.suspended.serves"), "moon")
        self.assertEqual(mu.mark("garden_pact.obligation_kept"), "tick")
        self.assertEqual(mu.mark("garden_pact.obligation"), "hourglass")
        self.assertEqual(mu.mark("pact.strain_tending.row.overworked_more"), "hourglass")
        self.assertEqual(mu.mark("pact.verdance_carrying.matter.two_roads"), "ask")
        self.assertIsNone(mu.mark("pact.term.area"))


class ArtTests(unittest.TestCase):
    def test_every_copy_is_rgba_even_from_a_palette_original(self):
        palette = [t for t, s in mu.OWNER_FILES.items() if Image.open(io.BytesIO(mu.read_library(s))).mode != "RGBA"]
        self.assertTrue(palette, "some originals are palette or colour-keyed PNGs (critique 17)")
        for target in palette:
            with self.subTest(target=target):
                image = Image.open(io.BytesIO(owner_art.adapt(mu.adapter(target))))
                self.assertEqual(image.mode, "RGBA")

    def test_every_adapted_copy_differs_only_in_its_stated_region(self):
        for target, entry in mu.ADAPTED.items():
            with self.subTest(target=target):
                self.assertEqual(mu.region_problems(target, entry, made(target)), [])

    def test_a_change_outside_the_region_is_caught(self):
        target = mu.S + "page_title_frame.png"
        image = made(target)
        image.putpixel((60, 40), (255, 0, 0, 255))
        self.assertTrue(mu.region_problems(target, mu.ADAPTED[target], image))

    def test_the_lettering_is_painted_out_and_nothing_else_changes(self):
        target = mu.S + "page_title_frame.png"
        image = made(target)
        x0, y0, x1, y1 = mu.SPLASH_WORDS
        self.assertTrue(all(image.getpixel((x, y))[3] == 0 for x in range(x0, x1 + 1) for y in range(y0, y1 + 1)))
        source = Image.open(io.BytesIO(mu.read_library(mu.ADAPTED[target]["source"]))).convert("RGBA")
        changed = [(x, y) for x in range(source.width) for y in range(source.height)
                   if source.getpixel((x, y)) != image.getpixel((x, y))]
        self.assertTrue(changed)
        self.assertTrue(all(x0 <= x <= x1 and y0 <= y <= y1 for x, y in changed))

    def test_the_header_keeps_no_painted_socket(self):
        header = made(mu.S + "relic_header.png")
        x0, y0, x1, y1 = mu.SOCKETS
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                self.assertEqual(header.getpixel((x, y)), header.getpixel((x, 60 + (y - y0) % 3)))

    def test_the_cosmos_keeps_eight_frames_cut_to_the_blob(self):
        cosmos = made(mu.COSMOS)
        self.assertEqual(cosmos.size, (407, 234 * 8))

    def test_glyphs_are_white_and_seven_rows(self):
        for name in mu.GLYPHS:
            with self.subTest(glyph=name):
                image = made(mu.S + f"glyph/{name}.png")
                self.assertEqual(image.height, 7)
                self.assertEqual({p[:3] for p in image.get_flattened_data() if p[3]}, {(255, 255, 255)})

    def test_every_nine_slice_fits_its_sprite(self):
        for target, meta in mu.MCMETA.items():
            scaling = meta["gui"]["scaling"]
            border = scaling["border"] if isinstance(scaling["border"], dict) else dict.fromkeys(
                ("left", "top", "right", "bottom"), scaling["border"])
            with self.subTest(target=target):
                self.assertEqual(made(target).size, (scaling["width"], scaling["height"]))
                self.assertLess(border["left"] + border["right"], scaling["width"])
                self.assertLess(border["top"] + border["bottom"], scaling["height"])
                self.assertIsNotNone(mu.sprite_id(target))

    def test_the_inks_read_on_their_fills(self):
        for target, ink, fill, ratio, _ in mu.contrast_rows(made):
            with self.subTest(sprite=target, ink=ink):
                self.assertGreaterEqual(ratio, 4.5)
        self.assertAlmostEqual(mu.contrast((0, 0, 0), (255, 255, 255)), 21.0, places=3)

    def test_nothing_never_imported_is_imported(self):
        for target in list(mu.OWNER_FILES) + list(mu.ADAPTED):
            with self.subTest(target=target):
                self.assertFalse(any(word in mu.source(target).lower() for word in mu.NEVER))


class CheckTests(unittest.TestCase):
    def test_the_committed_tree_holds(self):
        self.assertEqual(mu.problems(), [])

    def test_a_bare_placeholder_is_reported(self):
        saved = copy.deepcopy(mu.GLANCES)
        try:
            mu.GLANCES["pact.term.area"] = [("", "%s blocks", (8,), "8 blocks", "chip")]
            lang = dict(LANG)
            mu.lang_entries(lang)
            self.assertTrue(any("bare %s" in problem for problem in mu.problems(lang=lang)))
        finally:
            mu.GLANCES.clear()
            mu.GLANCES.update(saved)

    def test_a_glance_naming_an_argument_the_server_never_sends_is_reported(self):
        saved = copy.deepcopy(mu.GLANCES)
        try:
            mu.GLANCES["pact.term.grace"] = [("", "%2$s day grace", (1, 2), "2 day grace", "chip")]
            lang = dict(LANG)
            mu.lang_entries(lang)
            found = mu.problems(lang=lang)
            self.assertTrue(any("names argument 2" in problem for problem in found))
        finally:
            mu.GLANCES.clear()
            mu.GLANCES.update(saved)

    def test_a_new_work_without_its_glances_is_reported(self):
        import concordance_pacts as pacts
        saved = dict(pacts.SERVICE_WORDS)
        try:
            pacts.SERVICE_WORDS["seed_reading"] = {"messages": {"row.service": "Reading: %s of %s today"}}
            found = mu.problems()
            self.assertTrue(any("seed_reading: no WORK_GLANCES row" in problem for problem in found))
            self.assertTrue(any("pact.seed_reading.row.service" in problem for problem in found))
        finally:
            pacts.SERVICE_WORDS.clear()
            pacts.SERVICE_WORDS.update(saved)

    def test_a_beat_over_eighteen_words_is_reported(self):
        lang = dict(LANG)
        key = "message.jugcraft.concordance.pact.buer.beat.price"
        lang[key] = '"' + " ".join(["mend"] * 19) + '"'
        self.assertTrue(any("buer.beat.price" in problem and "over 18" in problem for problem in mu.problems(lang=lang)))

    def test_a_greeting_not_beginning_with_its_stage_direction_is_reported(self):
        lang = dict(LANG)
        lang["message.jugcraft.concordance.pact.stolas.beat.greeting.stage"] = "A raven lands."
        self.assertTrue(any("stolas.beat.greeting: does not begin" in problem for problem in mu.problems(lang=lang)))

    def test_every_beat_line_and_stage_keeps_to_its_limit(self):
        import concordance_pacts as pacts
        for name, spirit in pacts.SPIRITS.items():
            stage = spirit.get("stage", "")
            for beat, text in spirit["beats"].items():
                with self.subTest(spirit=name, beat=beat):
                    line = text[len(stage):].strip() if beat == "greeting" and stage else text
                    self.assertLessEqual(mu.nwords(line), mu.LIMITS["beat"])
            self.assertLessEqual(mu.nwords(stage), mu.LIMITS["stage"])
            self.assertTrue(spirit["beats"]["greeting"].startswith(stage))


class IconTests(unittest.TestCase):
    def test_rows_draw_their_icons_by_the_shape_of_their_key(self):
        self.assertEqual(mu.icon("pact.strain_tending.row.overworked"), "work")
        self.assertEqual(mu.icon("pact.verdance_carrying.row.poured"), "item:jugcraft:ley_pylon")
        self.assertEqual(mu.icon("garden_pact.obligation"), "glyph:pin")
        self.assertIsNone(mu.icon("garden_pact.obligation_kept"))
        self.assertIsNone(mu.icon("pact.row.state.active"))

    def test_every_work_has_its_icon_name_and_line(self):
        for work, rows in mu.WORK_GLANCES.items():
            with self.subTest(work=work):
                self.assertRegex(rows["icon"], r"^[a-z0-9_]+:[a-z0-9_/]+$")
                self.assertLessEqual(mu.nwords(mu.SERVICE_NAMES[work]), mu.LIMITS["label"])
                self.assertLessEqual(mu.nwords(mu.SERVICE_LINES[work]), mu.LIMITS["service"])
                self.assertEqual(LANG.get(mu.glance_key(f"pact.term.service.{work}", "name")), mu.SERVICE_NAMES[work])

    def test_an_icon_naming_no_glyph_is_reported(self):
        saved = mu.ICON_RULES
        try:
            mu.ICON_RULES = saved + (("exact", "pact.row.lost", "glyph:feather"),)
            self.assertTrue(any("glyph:feather" in problem for problem in mu.problems()))
        finally:
            mu.ICON_RULES = saved


if __name__ == "__main__":
    unittest.main()
