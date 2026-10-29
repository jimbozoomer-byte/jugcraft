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
        count = sum(len(glances) for glances in mu.GLANCES.values()) + len(mu.CARDS) + len(mu.SHORT) + len(mu.STEP_GLANCES)
        count += len(mu.SENTENCES) + len(mu.ROSTER_SHORT)  # unit 2: whole sentences' short forms, the roster's short words
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

    def test_a_contrast_box_with_no_opaque_fill_is_reported_not_raised(self):
        def clear(target):
            image = made(target)
            return Image.new("RGBA", image.size) if target == mu.S + "plate.png" else image
        rows = mu.contrast_rows(clear)
        self.assertTrue(any(target == mu.S + "plate.png" and fill is None for target, _, fill, _, _ in rows))

    def test_a_lit_copy_keeps_its_shape_outside_its_outline(self):
        # The unit review: the lit plates and paper strips may change tone everywhere, never their alpha (bar the outline's).
        for name in ("plate_highlighted", "paper/accept_highlighted"):
            with self.subTest(name=name):
                target = mu.S + name + ".png"
                image = made(target)
                image.putpixel((0, 0), (255, 255, 255, 128 if image.getpixel((0, 0))[3] != 128 else 255))
                image.putpixel((image.width // 2, image.height - 1), (0, 0, 0, 0))
                self.assertTrue(mu.region_problems(target, mu.ADAPTED[target], image))

    def test_each_runtime_file_has_its_own_change_and_outline_row(self):
        rows = [mu.provenance_row(t, s, d) for t, s, d, _ in owner_art.provenance() if t in mu.ADAPTED or t in mu.OWNER_FILES]
        lit = [row for row in rows if "plate_highlighted.png` | `originals/Magic/" in row]
        self.assertEqual(len(lit), 2)
        self.assertTrue(any("plate_outline.png" in row and "lit outline" in row for row in lit))
        self.assertNotEqual(mu.change(mu.S + "plate.png"), mu.change(mu.S + "plate_disabled.png"))
        self.assertIn("a cross", mu.change(mu.S + "action/end_inactive.png"))
        self.assertIn("two bars", mu.change(mu.S + "action/suspend_highlighted.png"))

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

    def test_a_long_act_or_step_inside_its_row_is_reported(self):
        # The unit review: a first task is rendered with every act, a missing step with every step's description, each drawn
        # as the client draws it (by its own glance when it has one).
        saved_glances, saved_steps = copy.deepcopy(mu.GLANCES), dict(mu.STEP_GLANCES)
        try:
            del mu.GLANCES["garden_pact.act.device"]
            mu.STEP_GLANCES.clear()
            lang = dict(LANG)
            for key in [k for k in lang if k.startswith(("screen.jugcraft.pact.glance.step.", "screen.jugcraft.pact.glance.garden_pact.act."))]:
                del lang[key]
            found = mu.problems(lang=lang)
            self.assertTrue(any("garden_pact.obligation with" in p and "act.device" in p and "row limit" in p for p in found))
            self.assertTrue(any("missing.step with" in p and "garden_regard_encountered" in p for p in found))
        finally:
            mu.GLANCES.clear()
            mu.GLANCES.update(saved_glances)
            mu.STEP_GLANCES.clear()
            mu.STEP_GLANCES.update(saved_steps)

    def test_an_answer_the_server_sends_without_a_glance_is_reported(self):
        saved = copy.deepcopy(mu.GLANCES)
        try:
            del mu.GLANCES["workers.seal_not_permitted"]
            lang = dict(LANG)
            del lang["screen.jugcraft.pact.glance.workers.seal_not_permitted"]
            found = mu.problems(lang=lang)
            self.assertTrue(any("workers.seal_not_permitted: the server can send it" in p for p in found))
        finally:
            mu.GLANCES.clear()
            mu.GLANCES.update(saved)

    def test_the_glances_nest_as_the_client_reads_them(self):
        self.assertEqual(mu.nested_key("message.jugcraft.concordance.garden_pact.act.device"),
                         "screen.jugcraft.pact.glance.garden_pact.act.device")
        self.assertEqual(mu.nested_key("advancements.jugcraft.concordance_garden_regard_encountered.description"),
                         "screen.jugcraft.pact.glance.step.concordance_garden_regard_encountered")
        self.assertIsNone(mu.nested_key("item.jugcraft.gleaner"))
        self.assertEqual(mu.full_key("diagnostic.conflicting_agreement"), "compose.jugcraft.diagnostic.conflicting_agreement")

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
                self.assertLessEqual(mu.nwords(rows["name"]), mu.LIMITS["label"])
                self.assertLessEqual(mu.nwords(rows["line"]), mu.LIMITS["service"])
                self.assertLessEqual(mu.width(rows["line"]), mu.SERVICE_ROOM)
                self.assertEqual(LANG.get(mu.glance_key(f"pact.term.service.{work}", "name")), rows["name"])

    def test_bathins_rows_draw_their_icons_by_their_own_keys(self):
        # The unit review: a later work's row.moved draws no Gleaner.
        self.assertEqual(mu.icon("pact.verdance_carrying.row.moved"), "item:jugcraft:gleaner")
        self.assertIsNone(mu.icon("pact.root_running.row.moved"))

    def test_a_work_without_its_line_is_reported_not_a_failure_to_load(self):
        saved = copy.deepcopy(mu.WORK_GLANCES)
        try:
            mu.WORK_GLANCES["verdance_carrying"].pop("line")
            self.assertTrue(any("verdance_carrying: no name or line" in problem for problem in mu.problems()))
        finally:
            mu.WORK_GLANCES.clear()
            mu.WORK_GLANCES.update(saved)

    def test_a_gauge_row_that_is_not_done_of_quota_is_reported(self):
        saved = copy.deepcopy(mu.WORK_GLANCES)
        try:
            mu.WORK_GLANCES["strain_tending"]["row.service"] = [("", "%2$s left of %1$s", (5, 16), "16 left of 5", "chip")]
            self.assertTrue(any("pact.strain_tending.row.service" in problem and "gauge" in problem for problem in mu.problems()))
        finally:
            mu.WORK_GLANCES.clear()
            mu.WORK_GLANCES.update(saved)

    def test_an_icon_naming_no_glyph_is_reported(self):
        saved = mu.ICON_RULES
        try:
            mu.ICON_RULES = saved + (("exact", "pact.row.lost", "glyph:feather"),)
            self.assertTrue(any("glyph:feather" in problem for problem in mu.problems()))
        finally:
            mu.ICON_RULES = saved



class RosterAndJadeTests(unittest.TestCase):
    """Unit 2: the roster's cards (their state, fact, name and kind) and Jade's short lines."""

    def test_short_forms_render_from_the_sentences_own_arguments(self):
        lang = {}
        mu.lang_entries(lang)
        self.assertEqual(lang["screen.jugcraft.glance.compose.jugcraft.worker.status.awaiting_answer"], "Awaits answer")
        self.assertEqual(lang["screen.jugcraft.glance.tooltip.jugcraft.concordance.jade.pact.next"], "Next in %1$s days")
        self.assertEqual(lang["screen.jugcraft.roster.short.page"], "%1$s / %2$s")
        self.assertEqual(lang["screen.jugcraft.roster.glance.request"], "Request #%1$s")
        for key, (pattern, args, expected, kind) in mu.SENTENCES.items():
            with self.subTest(key=key):
                self.assertEqual(mu.render(pattern, args), expected)
                self.assertFalse(mu.placeholders(pattern)[1])
                self.assertLessEqual(mu.nwords(expected), mu.LIMITS[kind])

    def test_a_cards_state_and_fact_fit_the_narrowest_card(self):
        # The smallest GUI (320 wide): two cards of 153, the text 37 in, the actions in a column 19 from the right.
        self.assertEqual(mu.CARD_TEXT, 94)
        for server, glances in mu.ROSTER_GLANCES.items():
            for _, _, _, expected, kind in glances:
                with self.subTest(server=server):
                    if kind == "state":
                        self.assertLessEqual(mu.width(expected), mu.STATE_ROOM)
                    if kind == "fact":
                        self.assertLessEqual(mu.width(expected), mu.FACT_ROOM)
        for key, (_, _, expected, kind) in mu.SENTENCES.items():
            if kind == "state":
                with self.subTest(key=key):
                    self.assertLessEqual(mu.width(expected), mu.STATE_ROOM)

    def test_a_state_too_wide_for_a_card_is_reported(self):
        saved = copy.deepcopy(mu.SENTENCES)
        try:
            key = "compose.jugcraft.worker.status.awaiting_answer"
            mu.SENTENCES[key] = ("Awaits your answer", (), "Awaits your answer", "state")
            lang = dict(LANG)
            mu.lang_entries(lang)
            self.assertTrue(any(key in p and "for its state" in p for p in mu.unit2_problems(lang)))
        finally:
            mu.SENTENCES.clear()
            mu.SENTENCES.update(saved)

    def test_a_roster_state_or_answer_without_its_glance_is_reported(self):
        saved = copy.deepcopy(mu.ROSTER_GLANCES)
        try:
            del mu.ROSTER_GLANCES["roster.waiting.lost"]
            self.assertTrue(any("roster.waiting.lost" in p and "no glance" in p for p in mu.unit2_problems(LANG)))
        finally:
            mu.ROSTER_GLANCES.clear()
            mu.ROSTER_GLANCES.update(saved)

    def test_a_short_form_naming_an_argument_the_sentence_lacks_is_reported(self):
        saved = copy.deepcopy(mu.SENTENCES)
        try:
            key = "tooltip.jugcraft.concordance.jade.pact.next"
            mu.SENTENCES[key] = ("Next in %2$s days", (7,), "Next in 7 days", "jade")
            lang = dict(LANG)
            mu.lang_entries(lang)
            self.assertTrue(any(key in p and "do not fit" in p for p in mu.unit2_problems(lang)))
        finally:
            mu.SENTENCES.clear()
            mu.SENTENCES.update(saved)

    def test_a_stray_short_form_and_a_bad_mark_are_reported(self):
        lang = dict(LANG)
        lang["screen.jugcraft.glance.compose.jugcraft.worker.status.dreaming"] = "Dreaming"
        found = mu.unit2_problems(lang)
        self.assertTrue(any("status.dreaming" in p and "SENTENCES does not hold" in p for p in found))
        saved = dict(mu.SENTENCE_MARKS)
        try:
            mu.SENTENCE_MARKS["compose.jugcraft.worker.status.idle"] = "star"
            self.assertTrue(any("status.idle" in p and "not one of MARKS" in p for p in mu.unit2_problems(LANG)))
        finally:
            mu.SENTENCE_MARKS.clear()
            mu.SENTENCE_MARKS.update(saved)


if __name__ == "__main__":
    unittest.main()
