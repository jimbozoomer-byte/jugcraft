"""Magic interfaces (docs/features/magic-interfaces.md): the owner's magic interface kit as Jugcraft's runtime art, and the
short words the magic screens draw in place of whole sentences.

The owner asked on 28 October 2026 for the pact screens, and most of the magic interface, to read far less and look far
richer. This module holds the shared layer's data (the client classes are client/Magic*.java, PactGlance and
RosterGlance; the common counting is concordance/spirits/PactAfford.java):

- OWNER_FILES: kit pieces copied unchanged from the owner's library (art/owner-library/originals/Magic), each written as
  RGBA (several originals are palette or colour-keyed PNGs).
- ADAPTED: kit pieces changed for Jugcraft, each by a named adapter that states the region it may change; check_magic_ui
  (problems() here, run by tools/check_mod_data.py) proves every copy differs from its source only there. Among them the
  splash frame's "ARS NOUVEAU" lettering is painted out to transparency.
- BUILT: the few glyphs and marks drawn for Jugcraft in the kit's one-pixel line style (no library source).
- MCMETA: each sprite's gui.scaling (nine-slice borders), written beside it.
- GLANCES, CARDS, SHORT: the short words, generated into the language file by lang_entries(). A glance is a client key
  fed the server's own arguments by position (screen.jugcraft.pact.glance.<server key>): the server's sentence stays
  the message, the narration and the hover; the glance is what is drawn. Each is rendered here with the generator's
  sample arguments and compared with its expected English.

tools/owner_art.py writes and checks the art (OWNER_FILES and ADAPTED as adaptations, BUILT as builds, MCMETA beside
them); tools/concordance.py calls lang_entries(). Nothing in the library is executed; the originals are never written.
A future garden spirit's unit adds its work's row to WORK_GLANCES (its service line and name, icon, kept-page rows,
blockers and chooser reason), a glance for its first task's act when the row would run long (ACT_GLANCES), and its
icon's line in client/PactGlance.WORK_ICONS, which check_magic_ui holds equal to WORK_GLANCES; its marks and rows' icons
follow the shape of their keys (MARK_RULES, ICON_RULES).
"""
import io
import json
import os
import re

from PIL import Image, ImageEnhance

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
MAGIC = os.path.join(ROOT, "art", "owner-library", "originals", "Magic")
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "jugcraft")

# Library folders (under originals/Magic/).
AJ = "assets/ars_jimbaux/textures/gui/"
DOC = AJ + "documentation/"
JB = "assets/jymbelics/textures/gui/description/"
JH = "assets/jymbelics/textures/hud/abilities/"
# Runtime folders (under assets/jugcraft/): the GUI atlas, and one plain texture.
S = "textures/gui/sprites/magic/"
TIP = "textures/gui/sprites/tooltip/"
COSMOS = "textures/gui/magic/cosmos.png"

# ------------------------------------------------------------------------------------------------ copied (RGBA)
OWNER_FILES = {
    S + "plate_outline.png": JB + "general/plate_outline.png",
    S + "card/frame_active.png": JH + "card_frame_active.png",
    S + "card/frame_inactive.png": JH + "card_frame_inactive.png",
    S + "card/chains.png": JB + "relic/chains.png",
    S + "card/lock.png": JB + "relic/icons/lock_inactive.png",
    S + "card/completed.png": JB + "icons/completed.png",
    S + "star/big_active.png": JB + "general/big_star_active.png",
    S + "star/big_inactive.png": JB + "general/big_star_inactive.png",
    S + "star/small_active.png": JB + "general/small_star_active.png",
    S + "star/small_hole.png": JB + "general/small_star_hole.png",
    S + "action/refresh.png": JB + "ability/reroll_button_active.png",
    S + "action/refresh_inactive.png": JB + "ability/reroll_button_inactive.png",
    S + "action/offer.png": JB + "ability/upgrade_button_active.png",
    S + "action/offer_inactive.png": JB + "ability/upgrade_button_inactive.png",
    S + "scroll_ball.png": AJ + "scroll_ball.png",
    S + "marker/collapsed.png": AJ + "collapsed_marker.png",
    S + "marker/expanded.png": AJ + "expanded_marker.png",
    S + "doc/frame_image.png": DOC + "doc_frame_image.png",
    S + "doc/underline.png": DOC + "doc_detail_underline.png",
    S + "doc/frame_item.png": DOC + "doc_detail_frame_item.png",
    S + "doc/frame_pedestal.png": DOC + "doc_detail_frame_pedestal.png",
    S + "doc/arrow_down.png": DOC + "doc_detail_arrow_down.png",
    S + "doc/page_left.png": DOC + "doc_button_next_page_left.png",
    S + "doc/page_left_hover.png": DOC + "doc_button_next_page_left_hover.png",
    S + "doc/page_right.png": DOC + "doc_button_next_page_right.png",
    S + "doc/page_right_hover.png": DOC + "doc_button_next_page_right_hover.png",
    S + "mana.png": AJ + "manabar_gui_mana.png",
    S + "spell_book.png": AJ + "spell_book_template.png",
    **{S + f"bookmark/{name}.png": AJ + source for name, source in (
        ("familiar", "familiar_tab.png"), ("documentation", "documentation_tab.png"),
        ("summon_circle", "summon_circle_bookmark.png"), ("spell_style", "spell_style_tab.png"),
        ("settings", "settings_tab.png"))},
    **{S + f"school/{name}.png": AJ + f"schools/{name}_tooltip.png" for name in (
        "abjuration", "air", "conjuration", "earth", "fire", "manipulation", "water")},
}
# Library files never imported (spec 2.10): words, a foreign mascot, named pets and vanilla-derived faces and blocks.
NEVER = ("discord_tab", "starbuncle", "lily", "nook", "waystones", "/groups/players", "/groups/villages",
         "sort_distance", "dimension/minecraft")

# ------------------------------------------------------------------------------------------------ the palette
# The inks of spec 2.3, by name (Java: client/MagicTheme). On scroll and paper: no shadow; on the cosmos, wood and the
# gold frame: vanilla's shadow.
PALETTE = {
    "ink": 0x403527, "dim": 0x64523C, "heading": 0x7C281C, "name": 0x5B2216, "green": 0x2F5A1A, "need": 0x704808,
    "red": 0x8F2A17, "cream": 0xFFFAEE, "lavender": 0xB6A8D8, "gold": 0xFFC43B, "good": 0xB9E05A, "warn": 0xFFA53B,
    "bad": 0xFF7466, "grey": 0xC8BCAC,
}
# Each sprite's main fill (the most common opaque colour inside the box) against every ink drawn on it: 4.5:1 or more
# (spec 2.3; critique 6). The stain ratio (the second fill) is reported by contrast_rows(), not held.
CONTRAST = {
    S + "scroll_plain.png": ((24, 18, 218, 70), ("ink", "dim", "heading", "name", "green", "need", "red")),
    S + "scroll_ruled.png": ((24, 18, 218, 70), ("ink", "dim")),
    S + "relic_header.png": ((60, 20, 220, 60), ("ink", "dim", "name", "heading", "green", "need")),
    S + "plate.png": ((3, 3, 47, 16), ("cream",)),
    S + "plate_primary.png": ((3, 3, 47, 16), ("cream",)),
    # A disabled plate's label (spec 2.3's rule; WCAG itself exempts disabled controls).
    S + "plate_disabled.png": ((3, 3, 47, 16), ("grey",)),
    S + "gold_frame.png": ((3, 3, 99, 9), ("cream", "gold", "lavender", "good", "warn", "bad")),
    S + "spell_book.png": ((30, 100, 120, 140), ("ink", "dim", "heading", "red", "green")),
    S + "paper/accept.png": ((18, 3, 50, 12), ("ink",)),
}

# ------------------------------------------------------------------------------------------------ adapted
# Each entry: runtime path -> {"source": library path, "adapter": a function below, its arguments, and "region": what it
# may change}. A region is a canvas built from pieces of the source ("pieces": (sx, sy, w, h, dx, dy) on a "size"
# canvas; by default the whole source), and the pixels that may differ from it: "boxes" (x0, y0, x1, y1, inclusive),
# "colours" (source colours that may be replaced), "overlay" (a library file whose opaque pixels may cover it), or "all"
# (a change of tone everywhere, keeping the alpha when "alpha" is true).
NOTCHES = ((50, 53, 6, 17, 46), (168, 173, 6, 17, 163), (104, 108, 64, 82, 99), (24, 35, 74, 80, 40))
SOCKETS = (4, 63, 43, 70)  # the header's five painted sockets (x0, y0, x1, y1)
PINK = {0x5B2B61: 0xC85FD5, 0x713D92: 0xF886FF, 0x592A5D: 0xBF58C1, 0x3D1F2F: 0x864467}
GREY_INK = 0xBFB391  # the paper strips' tick and cross
SPLASH_WORDS = (32, 11, 91, 15)  # "ARS NOUVEAU" on doc_frame_splash_page.png
COSMOS_FRAME = (418, 256)
COSMOS_CROP = (4, 14, 407, 234)  # the blob's alpha box in each frame (critique 10)
COSMOS_FRAMES = tuple(range(0, 16, 2))  # every other frame of sixteen: one loop at eight frames
GOLD_FRAME = (6, 6, 102, 12)  # the empty gauge frame in manabar_gui_border.png
TIP_MARGIN = 7  # tooltip sprites: vanilla draws them 12 out from the text; the gold rim sits 7 to 10 out
# The bookwyrm strip's glyphs: each one's columns (x0, x1, inclusive; survey-kit.md group 11, measured to the pixel), cut
# from the strip's rows 3 to 9, so every glyph is seven rows tall and the shallow ones (inf, key) sit centred.
WYRM = {"inf": (4, 14), "spiral": (24, 30), "sparkle": (42, 48), "moon": (59, 66), "key": (95, 104), "sun": (114, 120)}
WYRM_ROWS = (3, 9)
# Glyphs drawn for Jugcraft in the strip's one-pixel style, seven rows each ('#' set).
DRAWN = {
    "clock": ["..###..", ".#...#.", "#..#..#", "#..##.#", "#.....#", ".#...#.", "..###.."],
    "ring": ["..###..", ".#...#.", "#.....#", "#..#..#", "#.....#", ".#...#.", "..###.."],
    "calendar": [".#...#.", "#######", "#.....#", "#.#.#.#", "#.....#", "#.#.#.#", "#######"],
    "hourglass": ["#######", ".#...#.", "..#.#..", "...#...", "..#.#..", ".#.#.#.", "#######"],
    "person": ["..###..", "..#.#..", "..###..", ".......", ".#####.", "#.....#", "#.....#"],
    "party": ["..###...###.", "..#.#...#.#.", "..###...###.", "............", ".#####.####.", "#.....#....#",
              "#.....#....#"],
    "scroll": ["######.", "#....##", "#.##..#", "#.....#", "#.###.#", "#.....#", "#######"],
    "pause": ["##.##", "##.##", "##.##", "##.##", "##.##", "##.##", "##.##"],
    "cross": ["#.....#", ".#...#.", "..#.#..", "...#...", "..#.#..", ".#...#.", "#.....#"],
    "lock": ["..###..", ".#...#.", ".#...#.", "#######", "#..#..#", "#..#..#", "#######"],
    "tick": ["......#", ".....##", "#...##.", "##.##..", ".###...", "..#....", "......."],
    "pin": ["..###..", ".#...#.", ".#.#.#.", ".#...#.", "..###..", "...#...", "...#..."],
    "arrow": [".......", "...#...", "....#..", "######.", "....#..", "...#...", "......."],
    "ask": [".###.", "#...#", "....#", "..##.", "..#..", ".....", "..#.."],
    "bed": [".......", "#......", "#.#####", "#######", "#.....#", ".......", "......."],
    # The narrow footer's back plate (the arrow's mirror): cream on the plate, as the documentation's dark page arrows
    # could not be (1.3 to 1.9:1).
    "back": [".......", "...#...", "..#....", ".######", "..#....", "...#...", "......."],
}
GLYPHS = tuple(WYRM) + tuple(DRAWN)  # Java: MagicArt.Glyph, in this order
# The state marks (spec 2.5): a glyph in colour with a one-pixel dark edge, nine rows tall and its glyph's width and two
# wide (Java: MagicArt.Mark, in this order).
MARKS = {"tick": ("tick", 0x5FAE2A, 0x1D3A0C), "lock": ("lock", 0x8D7CB8, 0x2A1D3E), "cross": ("cross", 0xD0442C, 0x4A0F06),
         "ask": ("ask", 0xE0A21C, 0x4A2E04), "moon": ("moon", 0xB6A8D8, 0x2A1D3E), "hourglass": ("hourglass", 0xE09A1C, 0x4A2E04)}


def _rgb(value):
    return (value >> 16) & 255, (value >> 8) & 255, value & 255


ADAPTED = {
    S + "scroll_plain.png": {"source": JB + "relic/abilities_background.png", "adapter": "clean_notches",
                             "region": {"boxes": [(x0, y0, x1, y1) for x0, x1, y0, y1, _ in NOTCHES]}},
    S + "scroll_ruled.png": {"source": JB + "ability/stats_background.png", "adapter": "clean_notches",
                             "region": {"boxes": [(x0, y0, x1, y1) for x0, x1, y0, y1, _ in NOTCHES]}},
    S + "relic_header.png": {"source": JB + "ability/ability_background.png", "adapter": "clear_sockets",
                             "region": {"boxes": [SOCKETS]}},
    S + "socket.png": {"source": JB + "ability/ability_background.png", "adapter": "crop", "box": (4, 63, 8, 8),
                       "region": {"pieces": [(4, 63, 8, 8, 0, 0)], "size": (8, 8)}},
    S + "portrait_card.png": {"source": JB + "ability/ability_background.png", "adapter": "portrait_card",
                              "region": {"pieces": [(0, 0, 48, 77, 0, 0)], "size": (48, 77),
                                         "boxes": [(SOCKETS[0], SOCKETS[1], 43, SOCKETS[3])]}},
    S + "gold_frame.png": {"source": AJ + "manabar_gui_border.png", "adapter": "crop", "box": GOLD_FRAME,
                           "region": {"pieces": [GOLD_FRAME + (0, 0)], "size": GOLD_FRAME[2:]}},
    TIP + "magic_background.png": {"source": AJ + "manabar_gui_border.png", "adapter": "tooltip", "frame_only": False,
                                   "region": {"pieces": [GOLD_FRAME + (TIP_MARGIN, TIP_MARGIN)],
                                              "size": (GOLD_FRAME[2] + 2 * TIP_MARGIN, GOLD_FRAME[3] + 2 * TIP_MARGIN)}},
    TIP + "magic_frame.png": {"source": AJ + "manabar_gui_border.png", "adapter": "tooltip", "frame_only": True,
                              "region": {"pieces": [GOLD_FRAME + (TIP_MARGIN, TIP_MARGIN)],
                                         "size": (GOLD_FRAME[2] + 2 * TIP_MARGIN, GOLD_FRAME[3] + 2 * TIP_MARGIN),
                                         "boxes": [(TIP_MARGIN + 3, TIP_MARGIN + 3, TIP_MARGIN + GOLD_FRAME[2] - 4,
                                                    TIP_MARGIN + GOLD_FRAME[3] - 4)]}},
    S + "plate.png": {"source": JB + "general/plate_background.png", "adapter": "plate", "kind": "normal",
                      "region": {"all": True, "alpha": True}},
    S + "plate_primary.png": {"source": JB + "general/plate_background.png", "adapter": "plate", "kind": "primary",
                              "region": {"all": True, "alpha": True}},
    S + "plate_disabled.png": {"source": JB + "general/plate_background.png", "adapter": "plate", "kind": "disabled",
                               "region": {"all": True, "alpha": True}},
    S + "plate_highlighted.png": {"source": JB + "general/plate_background.png", "adapter": "plate", "kind": "normal",
                                  "outline": JB + "general/plate_outline.png",
                                  "region": {"all": True, "alpha": True, "overlay": JB + "general/plate_outline.png"}},
    S + "plate_primary_highlighted.png": {"source": JB + "general/plate_background.png", "adapter": "plate",
                                          "kind": "primary", "outline": JB + "general/plate_outline.png",
                                          "region": {"all": True, "alpha": True,
                                                     "overlay": JB + "general/plate_outline.png"}},
    S + "paper/accept.png": {"source": AJ + "save_bar.png", "adapter": "ink_paper", "ink": PALETTE["green"],
                             "region": {"colours": [GREY_INK], "boxes": [(0, 0, 15, 14)], "both": True}},
    S + "paper/accept_highlighted.png": {"source": AJ + "save_bar.png", "adapter": "ink_paper", "ink": PALETTE["green"],
                                         "lit": True, "region": {"all": True, "alpha": True}},
    S + "paper/decline.png": {"source": AJ + "clear_bar.png", "adapter": "ink_paper", "ink": PALETTE["red"],
                              "region": {"colours": [GREY_INK], "boxes": [(0, 0, 15, 14)], "both": True}},
    S + "paper/decline_highlighted.png": {"source": AJ + "clear_bar.png", "adapter": "ink_paper", "ink": PALETTE["red"],
                                          "lit": True, "region": {"all": True, "alpha": True}},
    **{S + f"bookmark/{name}_selected.png": {"source": AJ + source, "adapter": "pink",
                                             "region": {"colours": list(PINK)}} for name, source in (
        ("familiar", "familiar_tab.png"), ("documentation", "documentation_tab.png"),
        ("summon_circle", "summon_circle_bookmark.png"), ("spell_style", "spell_style_tab.png"),
        ("settings", "settings_tab.png"))},
    **{S + f"arrow/{side}{state}.png": {"source": JH + f"arrow_{side}.png", "adapter": "arrow", "state": state,
                                        "side": side,
                                        "region": {"pieces": [(0, 0, 11, 30, 1, 1)], "size": (13, 32),
                                                   **({"overlay": JH + f"arrow_{side}_outline.png"} if state == "_highlighted"
                                                      else {"all": True, "alpha": True} if state == "_disabled" else {})}}
       for side in ("left", "right") for state in ("", "_highlighted", "_disabled")},
    **{S + f"action/{name}{state}.png": {"source": JB + f"ability/upgrade_button_{'inactive' if state == '_inactive' else 'active'}.png",
                                         "adapter": "action", "glyph": glyph, "state": state,
                                         "region": {"boxes": [(3, 3, 10, 9)],
                                                    **({"overlay": JB + "ability/action_button_outline.png"}
                                                       if state == "_highlighted" else {})}}
       for name, glyph in (("end", "cross"), ("suspend", "pause")) for state in ("", "_inactive", "_highlighted")},
    **{S + f"action/{name}_highlighted.png": {"source": JB + f"ability/{source}_button_active.png", "adapter": "outlined",
                                              "outline": JB + "ability/action_button_outline.png",
                                              "region": {"overlay": JB + "ability/action_button_outline.png"}}
       for name, source in (("refresh", "reroll"), ("offer", "upgrade"))},
    **{S + f"glyph/{name}.png": {"source": AJ + "bookwyrm_storage_tabs.png", "adapter": "wyrm_glyph", "glyph": name,
                                 "region": {"pieces": [(x0, WYRM_ROWS[0], x1 - x0 + 1, 7, 0, 0)], "size": (x1 - x0 + 1, 7),
                                            "all": True, "alpha": True}}
       for name, (x0, x1) in WYRM.items()},
    S + "mark/moon.png": {"source": AJ + "bookwyrm_storage_tabs.png", "adapter": "wyrm_mark", "mark": "moon",
                          "region": {"pieces": [(WYRM["moon"][0], WYRM_ROWS[0], WYRM["moon"][1] - WYRM["moon"][0] + 1, 7, 1, 1)],
                                     "size": (WYRM["moon"][1] - WYRM["moon"][0] + 3, 9), "all": True}},
    S + "page_title_frame.png": {"source": DOC + "doc_frame_splash_page.png", "adapter": "paint_out", "box": SPLASH_WORDS,
                                 "region": {"boxes": [SPLASH_WORDS]}},
    COSMOS: {"source": JB + "general/background.png", "adapter": "cosmos",
             "region": {"pieces": [(COSMOS_CROP[0], n * COSMOS_FRAME[1] + COSMOS_CROP[1], COSMOS_CROP[2], COSMOS_CROP[3],
                                    0, i * COSMOS_CROP[3]) for i, n in enumerate(COSMOS_FRAMES)],
                        "size": (COSMOS_CROP[2], COSMOS_CROP[3] * len(COSMOS_FRAMES))}},
}
# Drawn for Jugcraft (no library source): the glyphs above that the strip lacks, and the marks built on them.
BUILT = {**{S + f"glyph/{name}.png": {"builder": "drawn_glyph", "glyph": name} for name in DRAWN},
         **{S + f"mark/{name}.png": {"builder": "drawn_mark", "mark": name} for name in MARKS if name != "moon"}}


def _nine(width, height, left, top, right, bottom):
    border = left if left == top == right == bottom else {"left": left, "top": top, "right": right, "bottom": bottom}
    return {"gui": {"scaling": {"type": "nine_slice", "width": width, "height": height, "border": border}}}


# gui.scaling for the sprites drawn at more than their own size (spec 4.2); every other sprite is drawn at its size.
MCMETA = {
    S + "scroll_plain.png": _nine(243, 88, 22, 16, 24, 17),
    S + "scroll_ruled.png": _nine(243, 88, 22, 30, 24, 44),  # one rule period tiles: every 88 + 14k keeps the rules even
    S + "relic_header.png": _nine(243, 77, 49, 0, 18, 0),  # sideways only, at its own height
    **{S + f"{name}.png": _nine(54, 19, 3, 0, 7, 0) for name in (
        "plate", "plate_primary", "plate_disabled", "plate_highlighted", "plate_primary_highlighted")},
    S + "plate_outline.png": _nine(54, 19, 3, 3, 7, 3),
    S + "paper/accept.png": _nine(56, 15, 16, 0, 5, 0),
    S + "paper/accept_highlighted.png": _nine(56, 15, 16, 0, 5, 0),
    S + "paper/decline.png": _nine(47, 15, 16, 0, 5, 0),
    S + "paper/decline_highlighted.png": _nine(47, 15, 16, 0, 5, 0),
    S + "gold_frame.png": _nine(102, 12, 3, 3, 3, 3),
    TIP + "magic_background.png": _nine(116, 26, 10, 10, 10, 10),
    TIP + "magic_frame.png": _nine(116, 26, 10, 10, 10, 10),
    S + "doc/frame_image.png": _nine(108, 81, 9, 9, 9, 9),
    S + "doc/underline.png": _nine(118, 6, 8, 0, 8, 0),
    S + "spell_book.png": _nine(290, 188, 144, 60, 145, 60),  # never stretched: native width, rows 60..127 tiled out
}


# ------------------------------------------------------------------------------------------------ adapters
def _open(data):
    return Image.open(io.BytesIO(data)).convert("RGBA")


def _source(read, entry):
    return _open(read(entry["source"]))


def rgba_copy(read, entry):
    """OWNER_FILES: the library file unchanged, written as RGBA."""
    return _source(read, entry)


def clean_notches(read, entry):
    """magic/scroll_plain and scroll_ruled: the torn notches that would repeat in the tiled middle (critique 11) take a
    clean neighbour's pixels, column for column; the stains stay."""
    img = _source(read, entry)
    px = img.load()
    for x0, x1, y0, y1, ref in NOTCHES:
        for x in range(x0, x1 + 1):
            for y in range(y0, y1 + 1):
                px[x, y] = px[ref + (x - x0) % 3, y]
    return img


def clear_sockets(read, entry):
    """magic/relic_header: the five painted sockets under the portrait window take the frame's wood (rows 60 to 62),
    so the screen draws exactly as many as the pact's cap."""
    img = _source(read, entry)
    px = img.load()
    x0, y0, x1, y1 = SOCKETS
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            px[x, y] = px[x, 60 + (y - y0) % 3]
    return img


def crop(read, entry):
    x, y, w, h = entry["box"]
    return _source(read, entry).crop((x, y, x + w, y + h))


def portrait_card(read, entry):
    """magic/portrait_card: the chooser's card, the cleaned header's gilded frame (its first 48 columns)."""
    return clear_sockets(read, entry).crop((0, 0, 48, 77))


def tooltip(read, entry):
    """tooltip/magic_background and _frame (the style jugcraft:magic): the gold frame set 7 in, where vanilla's tooltip
    sprites keep their margin; the frame is its gold rim alone, the background the rim and its purple well."""
    x, y, w, h = GOLD_FRAME
    out = Image.new("RGBA", (w + 2 * TIP_MARGIN, h + 2 * TIP_MARGIN))
    piece = _source(read, entry).crop((x, y, x + w, y + h))
    if entry["frame_only"]:
        piece.paste((0, 0, 0, 0), (3, 3, w - 3, h - 3))
    out.alpha_composite(piece, (TIP_MARGIN, TIP_MARGIN))
    return out


def _plate(img, kind):
    alpha = img.getchannel("A")
    rgb = img.convert("RGB")
    if kind == "normal":
        rgb = ImageEnhance.Brightness(rgb).enhance(0.7)
    elif kind == "primary":
        px = rgb.load()
        for y in range(rgb.height):
            for x in range(rgb.width):
                r, g, b = px[x, y]
                if r > 200 and g > 150 and b < 120:
                    continue  # the gold rim stays gold
                v = (r * 0.3 + g * 0.59 + b * 0.11) / 255
                px[x, y] = (int(40 + 70 * v), int(70 + 90 * v), int(40 + 50 * v))
        rgb = ImageEnhance.Brightness(rgb).enhance(0.86)
    elif kind == "disabled":
        rgb = ImageEnhance.Brightness(ImageEnhance.Color(rgb).enhance(0.25)).enhance(0.5)
    out = rgb.convert("RGBA")
    out.putalpha(alpha)
    return out


def plate(read, entry):
    """magic/plate (darkened to 70% so cream reads on it), plate_primary (a verdigris tint with the gold rim kept, at
    86%), plate_disabled (desaturated and halved), and the highlighted plates (the plate with the kit's outline)."""
    out = _plate(_source(read, entry), entry["kind"])
    if "outline" in entry:
        out.alpha_composite(_open(read(entry["outline"])))
    return out


def ink_paper(read, entry):
    """magic/paper/*: the strip's grey tick or cross (#bfb391, left of x 16) inked green or red; highlighted, the inked
    strip a tenth brighter in a one-pixel gold rim."""
    img = _source(read, entry)
    px = img.load()
    for y in range(img.height):
        for x in range(16):
            r, g, b, a = px[x, y]
            if a and (r, g, b) == _rgb(GREY_INK):
                px[x, y] = _rgb(entry["ink"]) + (a,)
    if entry.get("lit"):
        alpha = img.getchannel("A")
        img = ImageEnhance.Brightness(img.convert("RGB")).enhance(1.1).convert("RGBA")
        img.putalpha(alpha)
        px = img.load()
        rim = _rgb(PALETTE["gold"]) + (255,)
        for y in range(img.height):
            for x in range(img.width):
                if alpha.getpixel((x, y)) and any(not (0 <= x + dx < img.width and 0 <= y + dy < img.height)
                                                  or not alpha.getpixel((x + dx, y + dy))
                                                  for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                    px[x, y] = rim
    return img


def pink(read, entry):
    """magic/bookmark/*_selected: the ribbon's four purples swapped for the filter tabs' selected pinks."""
    img = _source(read, entry)
    px = img.load()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = px[x, y]
            key = (r << 16) | (g << 8) | b
            if a and key in PINK:
                px[x, y] = _rgb(PINK[key]) + (a,)
    return img


def _dim(img, factor=0.5, saturation=0.2):
    alpha = img.getchannel("A")
    rgb = ImageEnhance.Brightness(ImageEnhance.Color(img.convert("RGB")).enhance(saturation)).enhance(factor)
    out = rgb.convert("RGBA")
    out.putalpha(alpha)
    return out


def arrow(read, entry):
    """magic/arrow/*: the gold page arrow set 1 in on a 13x32 canvas (its outline's size), so every state draws at one
    size; highlighted with the kit's outline over it, disabled desaturated and halved."""
    out = Image.new("RGBA", (13, 32))
    img = _source(read, entry)
    if entry["state"] == "_disabled":
        img = _dim(img)
    out.alpha_composite(img, (1, 1))
    if entry["state"] == "_highlighted":
        out.alpha_composite(_open(read(JH + f"arrow_{entry['side']}_outline.png")))
    return out


def _glyph_rows(name):
    rows = DRAWN[name]
    img = Image.new("RGBA", (len(rows[0]), len(rows)))
    for y, row in enumerate(rows):
        for x, c in enumerate(row):
            if c == "#":
                img.putpixel((x, y), (255, 255, 255, 255))
    return img


def _wyrm(read, name):
    x0, x1 = WYRM[name]
    band = _open(read(AJ + "bookwyrm_storage_tabs.png")).crop((x0, WYRM_ROWS[0], x1 + 1, WYRM_ROWS[1] + 1))
    white = Image.new("RGBA", band.size, (255, 255, 255, 255))
    white.putalpha(band.getchannel("A"))
    return white


def _tint(img, colour):
    out = Image.new("RGBA", img.size, _rgb(colour) + (255,))
    out.putalpha(img.getchannel("A"))
    return out


def action(read, entry):
    """magic/action/end and suspend: the upgrade frame with its arrow covered and a cross (End: it no longer resembles
    Refresh) or two bars (Suspend) set in it; inactive from the inactive frame; highlighted with the kit's outline."""
    img = _source(read, entry)
    inactive = entry["state"] == "_inactive"
    fill, ink = (0x8E3E28, 0x40190E) if inactive else (0xC46A10, 0xFFF2C0)
    img.paste(_rgb(fill) + (255,), (3, 3, 11, 10))
    mark = _glyph_rows(entry["glyph"])
    img.alpha_composite(_tint(mark, ink), ((14 - mark.width) // 2, 3))
    if entry["state"] == "_highlighted":
        img.alpha_composite(_open(read(JB + "ability/action_button_outline.png")))
    return img


def outlined(read, entry):
    """magic/action/refresh_highlighted, offer_highlighted: the kit's action button with its outline over it."""
    img = _source(read, entry)
    img.alpha_composite(_open(read(entry["outline"])))
    return img


def wyrm_glyph(read, entry):
    """magic/glyph/<name> from the bookwyrm strip: its glyph cut to its own size and made white, so the screen tints it
    with the ink of the surface it sits on (spec 2.5)."""
    return _wyrm(read, entry["glyph"])


def _mark(glyph, fill, edge):
    out = Image.new("RGBA", (glyph.width + 2, glyph.height + 2))
    shadow = _tint(glyph, edge)
    for dx, dy in ((0, 1), (1, 0), (2, 1), (1, 2)):
        out.alpha_composite(shadow, (dx, dy))
    out.alpha_composite(_tint(glyph, fill), (1, 1))
    return out


def wyrm_mark(read, entry):
    """magic/mark/moon: the strip's moon in colour with a one-pixel dark edge."""
    _, fill, edge = MARKS[entry["mark"]]
    return _mark(_wyrm(read, "moon"), fill, edge)


def paint_out(read, entry):
    """magic/page_title_frame: the splash frame with its "ARS NOUVEAU" lettering cleared to transparency (the plaque bar
    under it stays; Jugcraft draws its own words there at runtime)."""
    img = _source(read, entry)
    x0, y0, x1, y1 = entry["box"]
    img.paste((0, 0, 0, 0), (x0, y0, x1 + 1, y1 + 1))
    return img


def cosmos(read, entry):
    """textures/gui/magic/cosmos.png: every other frame of the animated backdrop, each cut to the blob's alpha box (its
    ragged edge kept), stacked: eight frames of 407x234."""
    src = _source(read, entry)
    x, y, w, h = COSMOS_CROP
    out = Image.new("RGBA", (w, h * len(COSMOS_FRAMES)))
    for i, n in enumerate(COSMOS_FRAMES):
        out.paste(src.crop((x, n * COSMOS_FRAME[1] + y, x + w, n * COSMOS_FRAME[1] + y + h)), (0, i * h))
    return out


def drawn_glyph(read, entry):
    """magic/glyph/<name>: drawn for Jugcraft, white, seven rows (spec 2.5)."""
    return _glyph_rows(entry["glyph"])


def drawn_mark(read, entry):
    """magic/mark/<name>: a drawn glyph in colour with a one-pixel dark edge."""
    glyph, fill, edge = MARKS[entry["mark"]]
    return _mark(_glyph_rows(glyph), fill, edge)


# What each runtime file changes from its library file, for the provenance table (docs/features/magic-interfaces.md, which
# check_magic_ui holds to every runtime file, every library file it is made from and their SHA-256s): each file its own
# change, by its adapter, kind and state; a file composited over a copy (an outline) has its own row.
def extra_sources(target):
    """The library files an adapted copy is made from besides its source: the outline its region lets cover it."""
    entry = ADAPTED.get(target)
    overlay = entry and entry["region"].get("overlay")
    return [overlay] if overlay else []


def change(target, source=None):
    """What {target} changes from its library file {source} (its main source when None or the same)."""
    if target in BUILT:
        entry = BUILT[target]
        if entry["builder"] == "drawn_glyph":
            return f"the {entry['glyph']} glyph, drawn for Jugcraft in the strip's one-pixel style, white (no library file)"
        return f"the drawn {MARKS[entry['mark']][0]} glyph in colour with a one-pixel dark edge (no library file)"
    if target in OWNER_FILES:
        return "none: copied, written as RGBA"
    entry = ADAPTED[target]
    if source is not None and source != entry["source"]:
        return "its lit outline: this file drawn over the copy"
    kind = entry["adapter"]
    outline = {"plate": ", the kit's plate outline drawn over it", "arrow": ", the kit's arrow outline drawn over it",
               "action": ", the kit's action outline drawn over it"}
    if kind == "clean_notches":
        return ("the torn notches in the tiled middle given a clean neighbour's columns (x 50-53 and 168-173, rows 6-17; "
                "104-108, rows 64-82; 24-35, rows 74-80)")
    if kind == "clear_sockets":
        return "the five painted sockets (x 4-43, y 63-70) given the frame's wood (rows 60-62)"
    if kind == "crop":
        x, y, w, h = entry["box"]
        return f"cut out: {w}x{h} at {x},{y}"
    if kind == "portrait_card":
        return "the painted sockets cleared (x 4-43, y 63-70) and its first 48 columns cut out (the gilded frame, 48x77)"
    if kind == "tooltip":
        return ("the gauge frame (102x12 at 6,6) set 7 in on a 116x26 canvas: "
                + ("its gold rim alone, the purple well cleared" if entry["frame_only"] else "its gold rim and purple well"))
    if kind == "plate":
        tone = {"normal": "darkened to 70%", "primary": "a verdigris tint at 86%, the gold rim kept",
                "disabled": "desaturated to a quarter and darkened to half"}[entry["kind"]]
        return tone + (outline["plate"] if "outline" in entry else "")
    if kind == "ink_paper":
        mark, colour = ("tick", "green") if entry["ink"] == PALETTE["green"] else ("cross", "red")
        return (f"the grey {mark} (#bfb391, x < 16) inked {colour} #{entry['ink']:06x}"
                + (", a tenth brighter, in a one-pixel gold rim" if entry.get("lit") else ""))
    if kind == "pink":
        return "the ribbon's four purples swapped for the filter tabs' selected pinks"
    if kind == "arrow":
        return "set 1 in on a 13x32 canvas" + {"": "", "_highlighted": outline["arrow"],
                                               "_disabled": ", desaturated to a fifth and darkened to half"}[entry["state"]]
    if kind == "action":
        frame = "the inactive upgrade frame's" if entry["state"] == "_inactive" else "the upgrade frame's"
        mark = "a cross" if entry["glyph"] == "cross" else "two bars"
        return (f"{frame} arrow covered (x 3-10, y 3-9) and {mark} set in it"
                + (outline["action"] if entry["state"] == "_highlighted" else ""))
    if kind == "outlined":
        return "the kit's action outline drawn over it"
    if kind == "wyrm_glyph":
        x0, x1 = WYRM[entry["glyph"]]
        return f"the {entry['glyph']} glyph cut from the strip (x {x0}-{x1}, rows 3-9) and made white"
    if kind == "wyrm_mark":
        x0, x1 = WYRM["moon"]
        return f"the strip's moon (x {x0}-{x1}, rows 3-9) in colour with a one-pixel dark edge"
    if kind == "paint_out":
        return "the \"ARS NOUVEAU\" lettering (x 32-91, y 11-15) cleared to transparency"
    if kind == "cosmos":
        return "every other frame of sixteen, each cut to the blob's alpha box (4,14 407x234), stacked"
    raise KeyError(f"{target}: no change written for its adapter {kind}")


def provenance_row(target, source, digest):
    """{target}'s row of the provenance table for its library file {source} (under art/owner-library/; "" when built)."""
    if not source:
        return f"| `{target}` | drawn for Jugcraft |  | {change(target)} |"
    library = source[len("originals/Magic/"):] if source.startswith("originals/Magic/") else source
    return f"| `{target}` | `{source}` | `{digest}` | {change(target, library)} |"


ADAPTERS = {f.__name__: f for f in (rgba_copy, clean_notches, clear_sockets, crop, portrait_card, tooltip, plate,
                                    ink_paper, pink, arrow, action, outlined, wyrm_glyph, wyrm_mark, paint_out, cosmos,
                                    drawn_glyph, drawn_mark)}


def _named(name, fn):
    fn.__name__ = name
    fn.__qualname__ = name
    fn.__module__ = "magic_ui"
    return fn


def adapter(target):
    """The adapter tools/owner_art.py calls for {target} with its library reader: an RGBA copy for OWNER_FILES, else
    the entry's named adapter. Its __name__ is the adapter's (for the provenance table)."""
    if target in OWNER_FILES:
        entry = {"source": OWNER_FILES[target]}
        return _named("rgba_copy", lambda read: rgba_copy(read, entry))
    entry = ADAPTED[target]
    return _named(entry["adapter"], lambda read: ADAPTERS[entry["adapter"]](read, entry))


def builder(target):
    entry = BUILT[target]
    return _named(entry["builder"], lambda read: ADAPTERS[entry["builder"]](read, entry))


def source(target):
    return OWNER_FILES[target] if target in OWNER_FILES else ADAPTED[target]["source"]


def read_library(path):
    with open(os.path.join(MAGIC, path), "rb") as handle:
        return handle.read()


def sprite_id(target):
    """A GUI-atlas runtime path's sprite id (jugcraft:magic/plate), or None for a plain texture."""
    prefix = "textures/gui/sprites/"
    return "jugcraft:" + target[len(prefix):-4] if target.startswith(prefix) else None


# ------------------------------------------------------------------------------------------------ the words
# A word is a whitespace token holding a letter or digit (spec 1.1): "06:00–18:00" is one, "1 / 7 days" three.
_WORD = re.compile(r"[A-Za-z0-9]")
_PLACE = re.compile(r"%(?:(\d+)\$)?s")
LIMITS = {"chip": 3, "service": 10, "row": 12, "blocker": 14, "answer": 6, "standing": 12, "card": 4, "button": 2,
          "label": 3, "tip": 20, "fact": 4, "beat": 18, "stage": 8, "act": 6}
# The default font's advances in pixels (Minecraft's Font.width: each character's advance, 6 unless named here; measured
# from the game's ASCII sheet: a table of numbers, no glyph copied). The pixel limits below use them for English; the
# client measures at runtime, so other languages and Force Unicode reflow (and a header line too long is cut with "…").
ADVANCE = {"!": 2, "'": 2, ",": 2, ".": 2, ":": 2, ";": 2, "i": 2, "|": 2, "`": 3, "·": 3, " ": 4, '"': 4, "(": 4, ")": 4,
           "I": 4, "[": 4, "]": 4, "l": 4, "t": 4, "{": 4, "}": 4, "*": 5, "/": 5, "<": 5, ">": 5, "\\": 5, "f": 5, "k": 5,
           "$": 7, "%": 7, "@": 7, "~": 7}
# The narrowest room a line has (the pact screen at 320 wide: its scroll's text 262 wide): a blocker after its lock mark
# (12) and a row after its item (19), each two lines at most (MagicText.maxLines(2), spec 1.1); the header's service line
# after its item (19) in the full layout's 230 (320 - 8 wide less the header's portrait and roll).
BLOCKER_WIDTH = 262 - 12 - 2
ROW_WIDTH = 262 - 19 - 2
SERVICE_ROOM = 312 - 56 - 26 - 19
# Rows the server sends that are drawn by another element (spec 1.7), never as their own glance.
FOLDED = ("pact.row.area", "pact.row.area_any", "pact.row.end", "pact.row.matter.awaiting", "pact.row.matter.answered",
          # A compact without resonance is the price row alone: "Price" and its stacks, the sentence its message.
          "pact.term.compact.items")


# The screens drawn with the Magic layer (client/<name>.java): each must build its controls from MagicButton and its words
# from MagicText, with no BookArt.Tab or BookArt.Paragraph left (check_magic_ui). Each joins as it moves: PactScreen with
# unit 1's screen commit, then the roster (unit 2), the composer and the Field Guide (unit 3).
MAGIC_SCREENS = ["PactScreen", "RosterScreen"]


def nwords(text):
    return sum(1 for token in text.split() if _WORD.search(token))


def width(text):
    """{text}'s width in the default font (Font.width), in pixels."""
    return sum(ADVANCE.get(c, 6) for c in text)


def lines(text, room):
    """How many lines {text} wraps to in {room} pixels (Font.split: words kept whole, broken at spaces)."""
    count, line = 1, ""
    for word in text.split(" "):
        wider = word if not line else line + " " + word
        if width(wider) <= room or not line:
            line = wider
        else:
            count, line = count + 1, word
    return count


def render(pattern, args):
    """Minecraft's TranslatableContents: %n$s by position, a bare %s takes the next argument in order."""
    order = iter(args)

    def place(match):
        return str(args[int(match.group(1)) - 1]) if match.group(1) else str(next(order))
    return _PLACE.sub(place, pattern.replace("%%", "\0")).replace("\0", "%")


def placeholders(pattern):
    """(the highest position a pattern names, whether it uses a bare %s)."""
    found = _PLACE.findall(pattern.replace("%%", ""))
    return max((int(n) for n in found if n), default=0), any(not n for n in found)


def glance_key(server, suffix=""):
    """A glance's language key for a server key under message.jugcraft.concordance. (Java: PactGlance.key): pact.<rest>
    -> screen.jugcraft.pact.glance.<rest>, roster.<rest> -> screen.jugcraft.roster.glance.<rest>, anything else (the
    garden pacts' garden_pact.*) -> screen.jugcraft.pact.glance.<server key>; a second glance of one sentence adds its
    suffix."""
    family, _, rest = server.partition(".")
    base = {"pact": "screen.jugcraft.pact.glance." + rest,
            "roster": "screen.jugcraft.roster.glance." + rest}.get(family, "screen.jugcraft.pact.glance." + server)
    return base + ("." + suffix if suffix else "")


SERVER_PREFIX = "message.jugcraft.concordance."
# The one answer from outside the Concordance's messages (AgreementHolder's conflicting agreement): read as
# "diagnostic.<code>" (Java: PactGlance.server and DIAGNOSTIC).
DIAGNOSTIC_PREFIX = "compose.jugcraft.diagnostic."
STEP_PREFIX = "advancements.jugcraft."


def full_key(server):
    """The language key of the server sentence {server} names (a diagnostic's under compose.jugcraft.diagnostic.)."""
    if server.startswith("diagnostic."):
        return DIAGNOSTIC_PREFIX + server[len("diagnostic."):]
    return SERVER_PREFIX + server


def nested_key(key):
    """The glance an argument's own sentence ({key}) is drawn by when nested in another's (Java: PactGlance.nestedKey): a
    server sentence's glance, or a research step description's (screen.jugcraft.pact.glance.step.<advancement>); None
    for others."""
    if key.startswith(SERVER_PREFIX):
        return glance_key(key[len(SERVER_PREFIX):])
    if key.startswith(STEP_PREFIX) and key.endswith(".description"):
        return "screen.jugcraft.pact.glance.step." + key[len(STEP_PREFIX):-len(".description")]
    return None


def card_key(server):
    """A chooser card's reason (Java: PactGlance.card): screen.jugcraft.pact.glance.card.<server key after pact.>."""
    family, _, rest = server.partition(".")
    return "screen.jugcraft.pact.glance.card." + (rest if family == "pact" else server)


_BUER = ("06:00", "18:00", 5, 16)
# glance: server key -> [(suffix, pattern, sample arguments in the server's order, expected English, kind)]. Every
# argument list is the Java's (PactMenu, PactKeeping, PactObligations, StrainTending, VerdanceCarrying), checked by
# rendering. Each work's own rows are in WORK_GLANCES below.
GLANCES = {
    # Term chips (D-S15 under the amended wording; each chip's message stays the server sentence).
    "pact.term.hours": [("", "%1$s–%2$s", ("06:00", "18:00"), "06:00–18:00", "chip")],
    "pact.term.hours.any": [("", "Any hour", (), "Any hour", "chip")],
    "pact.term.area": [("", "%1$s blocks", (8,), "8 blocks", "chip")],
    "pact.term.quota": [("", "%1$s a day", (16,), "16 a day", "chip")],
    "pact.term.upkeep": [("", "%1$s / %3$s days", (1, "Buer's Offerings", 7, 4), "1 / 7 days", "chip"),
                         ("cap", "%4$s ahead", (1, "Buer's Offerings", 7, 4), "4 ahead", "chip")],
    "pact.term.grace": [("", "%1$s day grace", (1,), "1 day grace", "chip")],
    "pact.term.beneficiaries.personal": [("", "Only you", (), "Only you", "chip")],
    "pact.term.beneficiaries.party": [("", "You + party", (), "You + party", "chip")],
    "pact.term.compact": [("", "%1$s resonance", (8, "2 × Amethyst Shard, 3 × Feather"), "8 resonance", "chip")],
    "pact.term.compact_paid": [("", "Paid at sealing", (), "Paid at sealing", "chip")],
    # The six standing terms: their lines under More (spec 1.6), every clause that matters to recovery kept.
    "pact.term.suspension": [("", "Suspended: nothing lost; an offering resumes it.", (),
                              "Suspended: nothing lost; an offering resumes it.", "standing")],
    "pact.term.duration": [("", "Lasts until you end it.", (), "Lasts until you end it.", "standing")],
    "pact.term.breach": [("", "Only a missed offering suspends it. It never ends by itself.", (),
                          "Only a missed offering suspends it. It never ends by itself.", "standing")],
    "pact.term.ending": [("", "End it at the perch or from your roster.", (), "End it at the perch or from your roster.",
                          "standing")],
    "pact.term.returns": [("", "Offerings paid ahead come back to you, once.", (),
                           "Offerings paid ahead come back to you, once.", "standing")],
    "pact.term.perch": [("", "Perch broken? Pact and offerings stay yours.", (),
                         "Perch broken? Pact and offerings stay yours.", "standing")],
    # The kept page's rows (each keeps its number and its action).
    "pact.row.state.active": [("", "Active", (), "Active", "row")],
    "pact.row.state.suspended": [("", "Suspended by you", (), "Suspended by you", "row")],
    "pact.row.state.suspended.serves": [("", "Suspended by you", ("Buer",), "Suspended by you", "row")],
    "pact.row.state.upkeep_due": [("", "Offering due: %3$s days left", (1, "Buer's Offerings", 2),
                                   "Offering due: 2 days left", "row")],
    "pact.row.state.upkeep_held": [("", "Suspended: an offering resumes it", (1, "Buer's Offerings"),
                                    "Suspended: an offering resumes it", "row")],
    "pact.row.lost": [("", "Away: back when you are near, if the roster has room", ("Buer", 16),
                       "Away: back when you are near, if the roster has room", "row")],
    "pact.row.service_unknown": [("", "Unknown service (%1$s): kept as it was", ("jugcraft:foo",),
                                  "Unknown service (jugcraft:foo): kept as it was", "row")],
    "pact.row.next": [("", "Next in %3$s days", (4, "Bathin's Offerings", 7), "Next in 7 days", "row")],
    "pact.row.escrow": [("", "%1$s/%2$s ahead", (0, 4), "0/4 ahead", "chip")],
    "pact.row.service": [("", "%2$s left today", (1, 1), "1 left today", "row")],
    "pact.row.obligation": [("", "First task: record a sky observation at an observatory", (),
                             "First task: record a sky observation at an observatory", "row")],
    "pact.row.obligation_kept": [("", "First task kept", (), "First task kept", "row")],
    "garden_pact.obligation": [("", "First task: %1$s by hand here", ("tend a strained Verdant Bed",),
                                "First task: tend a strained Verdant Bed by hand here", "row")],
    "garden_pact.obligation_kept": [("", "First task kept: %1$s of %2$s spirits", (1, 3), "First task kept: 1 of 3 spirits",
                                     "row")],
    # Answers (the result line at the body's top; the server's sentence, with "Nothing was spent", is narrated).
    "pact.answer.sealed": [("", "Sealed: %1$s keeps this perch", ("Buer",), "Sealed: Buer keeps this perch", "answer")],
    "pact.answer.cannot_afford": [("", "Short of the price", (8,), "Short of the price", "answer")],
    "pact.answer.cannot_afford.items": [("", "Short of the price", (), "Short of the price", "answer")],
    "pact.answer.missing_evidence": [("", "Not yet: see what is missing", ("Bathin",), "Not yet: see what is missing",
                                      "answer")],
    "pact.answer.changed": [("", "His terms changed: read them again", (), "His terms changed: read them again", "answer")],
    "pact.answer.resolved": [("", "Answer kept", ("Leave it",), "Answer kept", "answer")],
    "pact.deposited": [("", "%1$s %2$s paid ahead", (1, "Amethyst Shard", 1, 4), "1 Amethyst Shard paid ahead", "answer")],
    "pact.reckoning.recalled": [("", "Recalled: %1$s", ("Lantern Star",), "Recalled: Lantern Star", "answer")],
    # Every other answer the pact screen can show (spec 1.1: a refusal one line of at most 6 words; ANSWERS lists them by
    # the Java that sends them, and check_magic_ui reads that Java): PactMenu's own, AgreementHolder.told's (sealing,
    # suspending, ending, seating), the deposits and Star-reckoning's.
    **{server: [("", pattern, args, expected, kind)] for server, (pattern, args, expected, kind) in {
        "pact.answer.too_fast": ("One moment", (), "One moment", "answer"),
        "pact.answer.declined": ("Declined: %1$s leaves", ("Buer",), "Declined: Buer leaves", "answer"),
        "pact.answer.invited": ("%1$s comes to the perch", ("Buer",), "Buer comes to the perch", "answer"),
        "pact.answer.not_offered": ("Not possible here now", (), "Not possible here now", "answer"),
        "pact.deposit_none": ("Nothing taken: full or none carried", (), "Nothing taken: full or none carried", "answer"),
        "pact.unavailable": ("%1$s's terms cannot be read here", ("Bathin",), "Bathin's terms cannot be read here", "answer"),
        "pact.reading.unreadable": ("Unreadable here; kept as it was", (), "Unreadable here; kept as it was", "row"),
        "pact.reading.taken": ("Another's pact is kept here", (), "Another's pact is kept here", "row"),
        "disabled": ("The Concordance is off here", (), "The Concordance is off here", "answer"),
        "diagnostic.conflicting_agreement": ("A copy: another agreement covers it", (),
                                             "A copy: another agreement covers it", "answer"),
        "workers.seal_done": ("Sealed", (), "Sealed", "answer"),
        "workers.seal_not_permitted": ("You may not seal here", (), "You may not seal here", "answer"),
        "workers.seal_taken": ("This perch is already taken", (), "This perch is already taken", "answer"),
        "workers.seal_already_held": ("You already keep this spirit", (), "You already keep this spirit", "answer"),
        "workers.seal_holder_full": ("%1$s Bound Wills held: end one", (16,), "16 Bound Wills held: end one", "answer"),
        "workers.seal_roster_full": ("Roster full (%1$s): release one", (16,), "Roster full (16): release one", "answer"),
        "workers.seal_unavailable": ("No spirit answers now", (), "No spirit answers now", "answer"),
        "workers.seal_cannot_afford": ("Short of the price", (), "Short of the price", "answer"),
        "workers.suspended": ("Suspended", (), "Suspended", "answer"),
        "workers.resumed": ("Resumed", (), "Resumed", "answer"),
        "workers.held_for_upkeep": ("Only an offering resumes it", (), "Only an offering resumes it", "answer"),
        "workers.no_agreement": ("No pact is kept here", (), "No pact is kept here", "answer"),
        "workers.not_holder": ("Only its holder may", (), "Only its holder may", "answer"),
        "workers.ended": ("Ended: the spirit departs", (), "Ended: the spirit departs", "answer"),
        "workers.end_not_permitted": ("Only its holder, in person", (), "Only its holder, in person", "answer"),
        "workers.seat_seated": ("Seated here", (), "Seated here", "answer"),
        "workers.seat_not_holder": ("Only its holder may seat it", (), "Only its holder may seat it", "answer"),
        "workers.seat_nothing": ("No pact to seat here", (), "No pact to seat here", "answer"),
        "workers.seat_not_permitted": ("Only this perch's owner may", (), "Only this perch's owner may", "answer"),
        "workers.seat_taken": ("This perch is already taken", (), "This perch is already taken", "answer"),
        "workers.seat_roster_full": ("Roster full (%1$s): release one", (16,), "Roster full (16): release one", "answer"),
        "workers.seat_unavailable": ("No spirit answers now", (), "No spirit answers now", "answer"),
        "pact.reckoning.nothing": ("No pact with Stolas here", (), "No pact with Stolas here", "answer"),
        "pact.reckoning.not_holder": ("Only its holder may ask", (), "Only its holder may ask", "answer"),
        "pact.reckoning.suspended": ("Suspended: resume it first", (), "Suspended: resume it first", "answer"),
        "pact.reckoning.upkeep_held": ("Suspended: an offering resumes it", (), "Suspended: an offering resumes it", "answer"),
        "pact.reckoning.outside_area": ("Stand within %1$s blocks", (8,), "Stand within 8 blocks", "answer"),
        "pact.reckoning.outside_hours": ("Only %1$s–%2$s", ("18:00", "06:00"), "Only 18:00–06:00", "answer"),
        "pact.reckoning.quota_spent": ("Done for today", (), "Done for today", "answer"),
        "pact.reckoning.not_observed": ("Only a pattern you observed", (), "Only a pattern you observed", "answer"),
        "pact.reckoning.no_astrolabe": ("Carry an astrolabe", (), "Carry an astrolabe", "answer"),
        "pact.reckoning.too_little": ("Needs %1$s resonance", (6, "Lantern Star"), "Needs 6 resonance", "answer"),
        "pact.reckoning.recalled_already": ("Already recalled: %1$s", ("Lantern Star",), "Already recalled: Lantern Star",
                                            "answer"),
    }.items()},
    # A first task's act, as its row draws it (garden_pact.obligation's argument; spec 1.1: the row at most 12 words). An
    # act short enough as the server words it needs none (ACT_GLANCES' acts are the long ones).
    "garden_pact.act.device": [("", "use an awake living device", (), "use an awake living device", "act")],
    # Blockers (every one drawn above the terms as a lock line, keeping its action).
    "pact.missing.owner": [("", "Only the perch's owner may invite %1$s", ("Bathin",),
                            "Only the perch's owner may invite Bathin", "blocker")],
    "pact.missing.prerequisite": [("", "First understand %1$s", ("Binding Arts",), "First understand Binding Arts",
                                   "blocker")],
    "pact.missing.step": [("", "%3$s", ("The Garden's Regard", "Observed", "Harvest a ripe organism crop from a Verdant Bed by hand"),
                           "Harvest a ripe organism crop from a Verdant Bed by hand", "blocker")],
    "pact.missing.held": [("", "You already keep a pact with %1$s", ("Bathin",), "You already keep a pact with Bathin",
                           "blocker")],
    "pact.missing.observation": [("", "Record a star at an observatory while a pattern is up", (),
                                  "Record a star at an observatory while a pattern is up", "blocker")],
    "pact.seat.offer": [("", "No perch: seat it here or end it", ("Stolas",), "No perch: seat it here or end it", "blocker")],
    "pact.ended_here": [("", "Pact ended. Use the perch to invite %1$s again", ("Buer",),
                         "Pact ended. Use the perch to invite Buer again", "row")],
    "pact.departed": [("", "%1$s departs; %3$s offerings came back", ("Buer", "ended by its holder", 4),
                       "Buer departs; 4 offerings came back", "row")],
}
# The answers the pact screen can show at its body's top (D-06), each a glance above (check_magic_ui reads the Java that
# sends them and names any sentence it can send that is not here): PactMenu.java's own lines and its disabled answer,
# AgreementHolder.told (Outcome, Toggle, Ended, Seat, with the conflicting-agreement diagnostic), PactKeeping.deposited
# and .end, and StarReckoning.told. The closed page's notes (the reading, the Concordance off) are among them.
ANSWERS = tuple(key for key in GLANCES if key.startswith(("pact.answer.", "pact.reckoning.", "workers.", "pact.reading."))) + (
    "pact.deposited", "pact.deposit_none", "pact.unavailable", "pact.departed", "pact.ended_here", "disabled",
    "diagnostic.conflicting_agreement")
# A research step's own description, drawn short where pact.missing.step names it (its third argument; spec 1.1: a blocker
# at most 14 words and two lines, keeping its action): screen.jugcraft.pact.glance.step.<advancement>. A step whose
# description already fits needs none.
STEP_GLANCES = {
    "concordance_garden_regard_encountered": "Examine a garden specimen: sneak and use it in the air",
}
# Each work's own row (a garden spirit's unit adds its own): its service line ("line": the header's line and the service
# chip's glance, at most 10 words and one header line), its name ("name": the beats' and the ended page's header, a second
# glance of the same sentence, screen.jugcraft.pact.glance.term.service.<work>.name), its icon (the item its service line
# and its own rows draw; one line in client/PactGlance.WORK_ICONS too), its kept-page rows, its blockers and its chooser
# card's reason. Row keys are under message.jugcraft.concordance.pact.<work>. (SERVICE_WORDS' messages); a work's
# "row.service" is today's gauge, "%1$s/%2$s today" (done, then the day's quota), and nothing else.
WORK_GLANCES = {
    "star_reckoning": {"card": None, "icon": "jugcraft:astrolabe", "name": "Star-reckoning",
                       "line": "Recalls a star you have seen"},
    "strain_tending": {
        "icon": "jugcraft:verdant_bed", "name": "Strain tending", "line": "Eases strained beds, one at a time",
        "row.service": [("", "%1$s/%2$s today", (5, 16), "5/16 today", "chip")],
        "row.asleep": [("", "Asleep until %1$s · %3$s/%4$s today", _BUER, "Asleep until 06:00 · 5/16 today", "row")],
        "row.overworked": [("", "Overworked at %1$s: feed it or let it rest", ("102, 64, -18",),
                            "Overworked at 102, 64, -18: feed it or let it rest", "row")],
        "row.overworked_more": [("", "%1$s overworked, nearest %2$s: feed them", (3, "102, 64, -18"),
                                 "3 overworked, nearest 102, 64, -18: feed them", "row")],
        "missing": [("", "Needs your awake Verdant Bed within %1$s blocks", (8,), "Needs your awake Verdant Bed within 8 blocks",
                     "blocker")],
        "card": ("Needs a bed near", (8,), "Needs a bed near"),
    },
    "verdance_carrying": {
        "icon": "jugcraft:verdant_heart", "name": "Verdance carrying", "line": "Carries your Hearts' Verdance, instantly",
        "row.service": [("", "%1$s/%2$s today", (3, 12), "3/12 today", "chip")],
        "row.moved": [("", "Last: %1$s Verdance to the Gleaner at %3$s", (8, "12, 64, -30", "40, 63, -2"),
                       "Last: 8 Verdance to the Gleaner at 40, 63, -2", "row")],
        "row.poured": [("", "Last: %1$s Verdance to a pylon, +%4$s charge", (8, "12, 64, -30", "14, 64, -30", 6),
                        "Last: 8 Verdance to a pylon, +6 charge", "row")],
        "row.leaving": [("", "Hearts left to their own pylons: %1$s", (1,), "Hearts left to their own pylons: 1", "row")],
        "matter.two_roads": [("", "Heart at %1$s: take from it too?", ("12, 64, -30",), "Heart at 12, 64, -30: take from it too?",
                              "row")],
        # His matter in general, drawn where none is raised now and an answer is kept (PactAccords: <work>.question).
        "question": [("", "Take from Hearts that have a road?", (), "Take from Hearts that have a road?", "row")],
        "missing": [("", "Needs your awake Heart, a Gleaner or pylon, in %1$s blocks", (10, 10),
                     "Needs your awake Heart, a Gleaner or pylon, in 10 blocks", "blocker")],
        "card": ("Needs Heart and Gleaner", (10, 10), "Needs Heart and Gleaner"),
    },
    # GC-07 (Foras; docs/features/foras-pact.md): graft keeping. Two refusals, each its own blocker and card; today's gauge
    # is his count (detail b); his matter asks whether to graft hosts that would gain nothing, or would fare worse, naming
    # the nearest (as Bathin's names his Heart); his service line names the host's donor and its cost.
    "graft_keeping": {
        "icon": "jugcraft:dewmoss", "name": "Graft keeping", "line": "Grafts your hosts, one Dewmoss each",
        "row.service": [("", "%1$s/%2$s today", (3, 8), "3/8 today", "chip")],
        "row.last": [("", "Last: the host at %1$s grafted", ("12, 64, -30", "13, 64, -28"),
                      "Last: the host at 12, 64, -30 grafted", "row")],
        "row.no_donor": [("", "No Dewmoss he may take", (), "No Dewmoss he may take", "row")],
        "matter.no_gain": [("", "Gain nothing or worse: %1$s, nearest %2$s. Graft anyway?", (3, "12, 64, -30"),
                            "Gain nothing or worse: 3, nearest 12, 64, -30. Graft anyway?", "row")],
        # His matter in general, drawn where none is raised now and an answer is kept (PactAccords: <work>.question).
        "question": [("", "Graft hosts that gain nothing?", (), "Graft hosts that gain nothing?", "row")],
        "missing.store": [("", "Needs a chest or store touching the perch", (), "Needs a chest or store touching the perch",
                           "blocker")],
        "missing.host": [("", "Needs a Sunpetal on your bed within %1$s blocks", (8,),
                          "Needs a Sunpetal on your bed within 8 blocks", "blocker")],
        # Two words each: with Stolas ready and Buer, Bathin and Foras not, the chooser draws 25 words (its budget).
        "card": {"missing.store": ("Store needed", (), "Store needed"), "missing.host": ("Host needed", (8,), "Host needed")},
    },
}
# A work's fields that are not its rows' glances: its chooser reason, its icon, its name and its service line.
WORK_FIELDS = ("card", "icon", "name", "line")


def work_cards(rows):
    """A work's chooser reasons, as (its refusal's key under pact.<work>., the reason): its "card" is the reason of its one
    refusal (pact.<work>.missing), or, for a work with several (GC-07: Foras's missing.store and missing.host), a dict of
    each refusal's key to its reason."""
    card = rows.get("card")
    if not card:
        return []
    return list(card.items()) if isinstance(card, dict) else [("missing", card)]


for _work, _rows in WORK_GLANCES.items():
    # A row without its name or line is reported by problems(), not a failure to load this module.
    if _rows.get("line"):
        GLANCES[f"pact.term.service.{_work}"] = [("", _rows["line"], (), _rows["line"], "service")] + (
            [("name", _rows["name"], (), _rows["name"], "label")] if _rows.get("name") else [])
    for _key, _glances in _rows.items():
        if _key not in WORK_FIELDS:
            GLANCES[f"pact.{_work}.{_key}"] = _glances
# Chooser card reasons (spec 1.7; at most 4 words): the first missing note's server key -> (pattern, sample, English).
CARDS = {
    "pact.missing.observation": ("Observe a star first", (), "Observe a star first"),
    "pact.missing.owner": ("Not your perch", ("Bathin",), "Not your perch"),
    "pact.missing.prerequisite": ("Understand %1$s first", ("Binding Arts",), "Understand Binding Arts first"),
    "pact.missing.held": ("Already your pact", ("Bathin",), "Already your pact"),
    # Not in the spec's list: the first note of a spirit whose research is not far enough, and of one this server
    # cannot read.
    "pact.missing.step": ("Research more first", ("The Garden's Regard", "Observed", "Harvest a ripe organism crop"),
                          "Research more first"),
    "pact.unavailable": ("Cannot be read here", ("Bathin",), "Cannot be read here"),
    **{f"pact.{work}.{key}": card for work, rows in WORK_GLANCES.items() for key, card in work_cards(rows)},
}
# The roster's one fact per card (unit 2 draws them; at most 4 words): the server's next and task lines.
ROSTER_GLANCES = {
    "roster.next.offering": [("", "due in %3$s days", (1, "Stolas's Offerings", 7, 0, 4), "due in 7 days", "fact")],
    "roster.next.at_visit": [("", "due when you visit", (1, "Stolas's Offerings"), "due when you visit", "fact")],
    "roster.next.warned": [("", "due: %3$s days left", (1, "Stolas's Offerings", 1), "due: 1 days left", "fact")],
    "roster.next.held": [("", "Offer to resume", (1, "Stolas's Offerings"), "Offer to resume", "fact")],
    "roster.next.suspended": [("", "suspended by you", (), "suspended by you", "fact")],
    "roster.next.quota": [("", "%1$s/%2$s tasks today", (2, 8), "2/8 tasks today", "fact")],
    "roster.next.none": [("", "nothing owed", (), "nothing owed", "fact")],
    "roster.next.delivered": [("", "%1$s/%2$s delivered", (0, 16, 0), "0/16 delivered", "fact")],
    "roster.task.carrying": [("", "Carrying #%1$s", (3, 16), "Carrying #3", "fact")],
    "roster.task.claimed": [("", "On request #%1$s", (3,), "On request #3", "fact")],
    "roster.task.none": [("", "Nothing now", (), "Nothing now", "fact")],
    # Unit 2 (the roster's cards): a request card's name and kind, a card's state (its row's waiting reason, beside the
    # mark its icon draws; at most three words and STATE_ROOM pixels) and the roster's own answers (at most six words).
    "roster.request": [("", "Request #%1$s", (3, 16, "Copper Ingot"), "Request #3", "label")],
    "roster.kind.request": [("", "Courier", (), "Courier", "label")],
    "roster.waiting.active": [("", "At work", (), "At work", "state")],
    "roster.waiting.unclaimed": [("", "Not yet claimed", (), "Not yet claimed", "state")],
    "roster.waiting.without_place": [("", "Without a place", (), "Without a place", "state")],
    "roster.waiting.stranded_place": [("", "Place gone", (), "Place gone", "state")],
    "roster.waiting.lost": [("", "Presence lost", (), "Presence lost", "state")],
    "roster.waiting.unloaded": [("", "Not loaded", (), "Not loaded", "state")],
    "roster.waiting.reserved": [("", "%1$s reserved", (16, "12, 64, -30 in the Overworld"), "16 reserved", "state")],
    "roster.waiting.reserved_unloaded": [("", "Not loaded", (16, "12, 64, -30 in the Overworld"), "Not loaded", "state")],
    "roster.waiting.in_transit": [("", "In transit", (), "In transit", "state")],
    "roster.waiting.destination_unloaded": [("", "Not loaded", (), "Not loaded", "state")],
    "roster.waiting.stranded": [("", "Stranded", (), "Stranded", "state")],
    "roster.waiting.returning": [("", "Going back", (), "Going back", "state")],
    "roster.waiting.disabled": [("", "Switched off", (), "Switched off", "state")],
    "roster.answer.not_listed": [("", "Not on your roster now", (), "Not on your roster now", "answer")],
    "roster.answer.changed": [("", "Changed: read it again", (), "Changed: read it again", "answer")],
    "roster.answer.too_fast": [("", "One moment", (), "One moment", "answer")],
    "roster.answer.not_offered": [("", "Cannot be done now", (), "Cannot be done now", "answer")],
    "roster.answer.not_permitted": [("", "Only you, in person", (), "Only you, in person", "answer")],
}
GLANCES.update(ROSTER_GLANCES)
# The state mark a glance draws, by the shape of its server key (Java: PactGlance.MARKS, in this order; the first rule
# that matches decides; none: no mark). A garden spirit's rows named as these are get their marks with no change here.
MARK_RULES = (
    # A work's blockers: pact.<work>.missing, or one of several refusals' (GC-07: pact.graft_keeping.missing.store).
    ("prefix", "pact.missing.", "lock"), ("suffix", ".missing", "lock"), ("contains", ".missing.", "lock"),
    ("exact", "pact.seat.offer", "lock"),
    ("exact", "pact.unavailable", "lock"), ("exact", "pact.row.state.active", "tick"),
    ("exact", "pact.row.state.upkeep_due", "hourglass"), ("prefix", "pact.row.state.", "moon"),
    ("exact", "pact.row.lost", "moon"), ("suffix", ".row.asleep", "moon"), ("contains", ".row.overworked", "hourglass"),
    ("suffix", "obligation_kept", "tick"), ("suffix", "obligation", "hourglass"), ("contains", ".matter.", "ask"),
    # GC-07: the row that waits on the player (a donor to put in his store) among the rows that need them.
    ("exact", "pact.graft_keeping.row.no_donor", "hourglass"),
)


def _matches(kind, text, server):
    return (kind == "prefix" and server.startswith(text) or kind == "suffix" and server.endswith(text)
            or kind == "exact" and server == text or kind == "contains" and text in server)


def mark(server):
    for kind, text, name in MARK_RULES:
        if _matches(kind, text, server):
            return name
    return None


# The icon a kept page's or closed page's row draws, by the shape of its server key (Java: PactGlance.ICONS, in this
# order; the first rule that matches decides, and a row with an icon draws it instead of its mark): "work" is the work's
# own item (WORK_GLANCES[work]["icon"]), "glyph:<name>" a glyph of GLYPHS in the row's ink, "item:<id>" an item. A row
# with neither icon nor mark draws its words alone. A garden spirit's rows named as these are get their icons with no
# change here.
ICON_RULES = (
    ("contains", ".row.overworked", "work"),
    # Bathin's own rows, by their exact keys (a later work's rows of the same names draw their own icons, or none).
    ("exact", "pact.verdance_carrying.row.poured", "item:jugcraft:ley_pylon"),
    ("exact", "pact.verdance_carrying.row.moved", "item:jugcraft:gleaner"),
    ("exact", "pact.verdance_carrying.row.leaving", "item:jugcraft:verdant_heart"),
    # Foras's own rows: the donor he waits for, and the host he grafted last.
    ("exact", "pact.graft_keeping.row.no_donor", "item:jugcraft:dewmoss"),
    ("exact", "pact.graft_keeping.row.last", "item:jugcraft:sunpetal"),
    ("suffix", "obligation", "glyph:pin"),
    ("exact", "pact.ended_here", "glyph:pin"),
    ("exact", "pact.departed", "glyph:spiral"),
)


def icon(server):
    for kind, text, name in ICON_RULES:
        if _matches(kind, text, server):
            return name
    return None


# Short labels (screen.jugcraft.pact.short.<name>): each control's full label stays its message; these are drawn.
SHORT = {
    "accept": ("Accept", "button"), "decline": ("Decline", "button"), "leave": ("Leave", "button"),
    "terms": ("Terms", "button"), "suspend": ("Suspend", "button"), "resume": ("Resume", "button"),
    "seat": ("Seat here", "button"), "offer": ("Offer", "button"), "more": ("More", "button"), "less": ("Less", "button"),
    "close": ("Close", "button"), "end": ("End...", "button"), "recall": ("Recall", "button"),
    "carried": ("×%1$s carried", "label"), "price": ("Price", "label"), "standing": ("Standing terms", "label"),
    "ended": ("Ended", "label"), "ready": ("Ready", "label"), "not_yet": ("Not yet", "label"),
    "invite": ("Invite a spirit", "label"), "used": ("used", "label"),
    "resonance": ("%1$s/%2$s resonance", "label"), "any_of": ("Any of: %1$s", "tip"),
    "standing.hint": ("The same for every spirit", "tip"), "accept.hint": ("Pays the price and seals the pact", "tip"),
    "accept.narration": ("Nothing is spent before this", "tip"), "skip.hint": ("Skip to his terms", "tip"),
    "end.leaves": ("He leaves; offerings paid ahead come back once.", "tip"),
    "end.undone": ("This cannot be undone.", "tip"),
    # The price chip's narration before Accept when anything is short (spec 2.6; the marks drawn are display only).
    "have": ("%1$s %2$s/%3$s", "tip"), "short_of": ("Short: %1$s", "tip"),
}
SHORT_SAMPLES = {"carried": (12,), "resonance": (6, 8), "any_of": ("Stolas Starflower, Amethyst Shard",),
                 "have": ("Ender Pearl", 1, 2), "short_of": ("Ender Pearl 1/2, 6/8 resonance",)}

# ------------------------------------------------------------------------------------------------ unit 2: the roster and Jade
# Unit 2 (spec 4.6, 2.14): the roster as cards, and Jade's short lines. Its limits: a card's state and a Jade line.
LIMITS.update({"state": 3, "jade": 4})
# A roster card's text room at the narrowest card (the smallest GUI, 320 wide: two cards of 153, its text 37 in, its
# actions in a column 19 from its right; client/RosterScreen.Card): a state after its mark (at most 10 wide, and 3 apart),
# and a fact without its icon (the card drops the icon, then the kind, before it cuts a word).
CARD_TEXT = 153 - 19 - 3 - 37
STATE_ROOM = CARD_TEXT - 10 - 3
FACT_ROOM = CARD_TEXT
# The short form of a whole sentence that is not one of the pact screen's server glances: a roster card's state (a row's
# waiting reason that is a worker's status or a courier's state or note) and Jade's lines on the pact perch, its spirits,
# beds and living devices (screen.jugcraft.glance.<the sentence's own key>; Java: client/MagicGlance). Fed the sentence's
# own arguments by position; a sentence without one, or given fewer arguments than its short form names, is shown whole.
# The whole sentence stays the narration, and Jade's extended view (sneak) shows every line whole.
# Its key -> (pattern, sample arguments in the sentence's order, expected English, kind).
_STATUS = "compose.jugcraft.worker.status."
_COURIER = "compose.jugcraft.courier."
_ECOLOGY = "compose.jugcraft.ecology."
_JADE = "tooltip.jugcraft.concordance.jade."
SENTENCES = {
    # A worker's status: a roster card's state (beside its icon's mark) and Jade's first line on a spirit.
    **{_STATUS + status: (text, (), text, "state") for status, text in {
        "awaiting_answer": "Awaits answer", "blocked_by_access": "No access", "cannot_navigate": "No way there",
        "destination_unloaded": "Not loaded", "disabled": "Switched off", "finished": "Done for today",
        "following": "Following you", "full": "Full: no room", "idle": "Idle", "needs_repair": "Needs repair",
        "no_energy": "Out of Charge", "other_dimension": "Another world", "outside_agreement": "Off its terms",
        "owner_offline": "You are away", "returning": "Going home", "supporting": "Mending you", "suspended": "Suspended",
        "travelling": "On its way", "upkeep_due": "Offering due", "upkeep_held": "Upkeep held",
        "waiting_for_resources": "No resources", "working": "At work"}.items()},
    # A courier request's state and its last note: a request card's state.
    **{_COURIER + key: (text, (), text, "state") for key, text in {
        "state.claimed": "Claimed", "state.in_transit": "In transit", "state.open": "No courier yet",
        "state.returning": "Going back", "state.stranded": "Stranded", "note.agreement_ended": "Courier ended",
        "note.blocked": "No access", "note.cancelled": "Cancelled", "note.cannot_navigate": "No way there",
        "note.destination_gone": "Drop-off gone", "note.full": "Drop-off full", "note.lapsed": "Courier lapsed",
        "note.no_source": "No source", "note.nowhere_to_return": "No room back", "note.reassigned": "Courier busy",
        "note.source_empty": "Source empty", "note.source_gone": "Source gone", "note.source_unloaded": "Source away"}.items()},
    # Jade on the pact perch, the Spirit Anchor and a pact spirit (PactReading's lines).
    _JADE + "pact.visiting": ("%1$s visits", ("Stolas",), "Stolas visits", "jade"),
    _JADE + "pact.unreadable": ("Cannot be read", (), "Cannot be read", "jade"),
    _JADE + "pact.keeps": ("With %1$s", ("Stolas",), "With Stolas", "jade"),
    _JADE + "pact.state.suspended": ("Suspended", (), "Suspended", "jade"),
    _JADE + "pact.state.upkeep_due": ("Offering due", (), "Offering due", "jade"),
    _JADE + "pact.state.upkeep_held": ("Upkeep held", (), "Upkeep held", "jade"),
    _JADE + "pact.next": ("Next in %1$s days", (7,), "Next in 7 days", "jade"),
    _JADE + "pact.grace": ("Grace: %1$s days", (1,), "Grace: 1 days", "jade"),
    _JADE + "pact.lost": ("Presence lost", (), "Presence lost", "jade"),
    _JADE + "pact.accord": ("Answer: %1$s", ("Leave it",), "Answer: Leave it", "jade"),
    _JADE + "pact.service.courier": ("Courier work", (), "Courier work", "jade"),
    _JADE + "pact.matter.verdance_carrying.two_roads": ("Heart has own pylon", (), "Heart has own pylon", "jade"),
    _JADE + "pact.note.strain_tending.asleep": ("Asleep until dawn", (), "Asleep until dawn", "jade"),
    _JADE + "pact.note.strain_tending.overworked": ("Overworked beds: %1$s", (3,), "Overworked beds: 3", "jade"),
    _JADE + "pact.note.verdance_carrying.empty": ("Nothing to carry", (), "Nothing to carry", "jade"),
    _JADE + "pact.note.verdance_carrying.nowhere": ("Nowhere to carry it", (), "Nowhere to carry it", "jade"),
    _JADE + "pact.note.verdance_carrying.two_roads": ("Two roads: %1$s", (2,), "Two roads: 2", "jade"),
    _JADE + "pact.matter.graft_keeping.no_gain": ("Hosts gain nothing", (), "Hosts gain nothing", "jade"),
    _JADE + "pact.note.graft_keeping.no_gain": ("Would gain nothing: %1$s", (3,), "Would gain nothing: 3", "jade"),
    _JADE + "pact.note.graft_keeping.no_reading": ("Unread hosts: %1$s", (2,), "Unread hosts: 2", "jade"),
    _JADE + "pact.note.graft_keeping.no_donor": ("No donor", (), "No donor", "jade"),
    _JADE + "pact.note.graft_keeping.no_host": ("Nothing to graft", (), "Nothing to graft", "jade"),
    "message.jugcraft.concordance.disabled": ("Switched off", (), "Switched off", "jade"),
    "compose.jugcraft.diagnostic.conflicting_agreement": ("A copy: does nothing", (), "A copy: does nothing", "jade"),
    _JADE + "body": ("%1$s/%2$s · %3$s/%4$s Charge", (10, 20, 300, 400), "10/20 · 300/400 Charge", "jade"),
    # Jade on a Verdant Bed: dormant, its strain, and its reasons (Verdict.reasons; the factor named first).
    _JADE + "dormant": ("Dormant", (), "Dormant", "jade"),
    _ECOLOGY + "strain": ("Strain %1$s/%2$s", (2, 8), "Strain 2/8", "jade"),
    _ECOLOGY + "short": ("%1$s too low", ("Moisture", 3, 5), "Moisture too low", "jade"),
    _ECOLOGY + "excess": ("%1$s too high", ("Moisture", 9, 7), "Moisture too high", "jade"),
    _ECOLOGY + "tolerable": ("%1$s tolerable", ("Moisture", 6, 3, 5), "Moisture tolerable", "jade"),
    _ECOLOGY + "strained.excess": ("%1$s too high", ("Disturbance", 6, 2, 5), "Disturbance too high", "jade"),
    _ECOLOGY + "strained.tolerable": ("%1$s tolerable", ("Disturbance", 4, 2, 0, 3), "Disturbance tolerable", "jade"),
    # Jade on a living device: its status.
    _ECOLOGY + "status.beds_full": ("Beds full", (), "Beds full", "jade"),
    _ECOLOGY + "status.disabled": ("Switched off", (), "Switched off", "jade"),
    _ECOLOGY + "status.keeper_away": ("Keeper away", (), "Keeper away", "jade"),
    _ECOLOGY + "status.no_bed": ("Not on a bed", (), "Not on a bed", "jade"),
    _ECOLOGY + "status.no_verdance": ("No Verdance", (), "No Verdance", "jade"),
    _ECOLOGY + "status.stalled": ("Held back", (), "Held back", "jade"),
    _ECOLOGY + "status.starved": ("Bed too poor", (), "Bed too poor", "jade"),
}
# The mark Jade draws before a sentence's line, short or whole, by the sentence's own key (Java: client/MagicGlance.MARKS,
# held equal by check_magic_ui); a line without one draws its own icon (the spirit's sign, the work's item, a glyph) or
# none. The roster's cards draw the mark of their row's icon (client/RosterScreen.mark).
SENTENCE_MARKS = {
    **{_STATUS + status: mark for status, mark in {
        "awaiting_answer": "ask", "blocked_by_access": "cross", "cannot_navigate": "cross", "destination_unloaded": "moon",
        "disabled": "lock", "finished": "tick", "following": "tick", "full": "hourglass", "idle": "moon",
        "needs_repair": "hourglass", "no_energy": "hourglass", "other_dimension": "moon", "outside_agreement": "moon",
        "owner_offline": "moon", "returning": "tick", "supporting": "tick", "suspended": "moon", "travelling": "tick",
        "upkeep_due": "hourglass", "upkeep_held": "moon", "waiting_for_resources": "hourglass", "working": "tick"}.items()},
    _JADE + "pact.visiting": "ask", _JADE + "pact.unreadable": "lock", _JADE + "pact.state.active": "tick",
    _JADE + "pact.state.suspended": "moon", _JADE + "pact.state.upkeep_due": "hourglass", _JADE + "pact.state.upkeep_held": "moon",
    _JADE + "pact.lost": "moon", _JADE + "pact.accord": "tick", _JADE + "pact.matter.verdance_carrying.two_roads": "ask",
    _JADE + "pact.note.strain_tending.asleep": "moon", _JADE + "pact.note.strain_tending.overworked": "hourglass",
    _JADE + "pact.note.verdance_carrying.empty": "moon", _JADE + "pact.note.verdance_carrying.nowhere": "hourglass",
    _JADE + "pact.note.verdance_carrying.two_roads": "ask", _JADE + "pact.matter.graft_keeping.no_gain": "ask",
    _JADE + "pact.note.graft_keeping.no_gain": "ask", _JADE + "pact.note.graft_keeping.no_reading": "hourglass",
    _JADE + "pact.note.graft_keeping.no_donor": "hourglass", _JADE + "pact.note.graft_keeping.no_host": "moon",
    "message.jugcraft.concordance.disabled": "lock",
    "compose.jugcraft.diagnostic.conflicting_agreement": "cross", _JADE + "dormant": "moon",
    _ECOLOGY + "growth.thriving": "tick", _ECOLOGY + "growth.tolerating": "hourglass", _ECOLOGY + "growth.stalled": "cross",
    _ECOLOGY + "strain": "hourglass", _ECOLOGY + "short": "hourglass", _ECOLOGY + "excess": "hourglass",
    _ECOLOGY + "tolerable": "hourglass", _ECOLOGY + "strained.excess": "hourglass", _ECOLOGY + "strained.tolerable": "hourglass",
    _ECOLOGY + "mixed": "tick",
    **{_ECOLOGY + "status." + status: mark for status, mark in {
        "beds_full": "hourglass", "disabled": "lock", "dormant": "moon", "full": "hourglass", "hungry": "moon", "idle": "moon",
        "keeper_away": "moon", "no_bed": "cross", "no_verdance": "cross", "stalled": "cross", "starved": "cross",
        "waiting": "moon", "working": "tick"}.items()},
}
SENTENCE_PREFIX = "screen.jugcraft.glance."
# The roster screen's own short words (screen.jugcraft.roster.short.<name>; its whole sentences stay the messages, the
# narration and the tips): name -> (text, kind); ROSTER_SAMPLES gives the arguments each is shown with.
ROSTER_SHORT = {
    "page": ("%1$s / %2$s", "label"),
    "empty": ("Nothing on your roster yet", "row"),
    "empty.hint": ("Bond a familiar, seal a pact or file a request", "row"),
    "more": ("%1$s more not listed", "fact"),
    "disabled": ("Concordance off: nothing can be done", "row"),
    "unavailable": ("No roster on this server", "row"),
    "suspend.tip": ("Suspend %1$s", "label"),
    "resume.tip": ("Resume %1$s", "label"),
    "end.tip": ("End %1$s...", "label"),
    "cancel.tip": ("Cancel %1$s", "label"),
    "recover.tip": ("Recover %1$s", "label"),
    "end.leaves": ("It leaves; anything held comes back once.", "row"),
}
ROSTER_SAMPLES = {"page": (1, 8), "more": (16,), **{f"{action}.tip": ("Bathin",) for action in ("suspend", "resume", "end")},
                  "cancel.tip": ("Request #3",), "recover.tip": ("Request #3",)}


def lang_entries(lang):
    """The glances, chooser reasons and short labels (English only, D-S21)."""
    for server, glances in GLANCES.items():
        for suffix, pattern, _, _, _ in glances:
            lang[glance_key(server, suffix)] = pattern
    for server, (pattern, _, _) in CARDS.items():
        lang[card_key(server)] = pattern
    for step, text in STEP_GLANCES.items():
        lang[f"screen.jugcraft.pact.glance.step.{step}"] = text
    for name, (text, _) in SHORT.items():
        lang[f"screen.jugcraft.pact.short.{name}"] = text
    for key, (pattern, _, _, _) in SENTENCES.items():
        lang[SENTENCE_PREFIX + key] = pattern
    for name, (text, _) in ROSTER_SHORT.items():
        lang[f"screen.jugcraft.roster.short.{name}"] = text


# ------------------------------------------------------------------------------------------------ the check
def _lum(rgb):
    def channel(v):
        v /= 255
        return v / 12.92 if v <= 0.03928 else ((v + 0.055) / 1.055) ** 2.4
    r, g, b = rgb
    return 0.2126 * channel(r) + 0.7152 * channel(g) + 0.0722 * channel(b)


def contrast(a, b):
    la, lb = sorted((_lum(a), _lum(b)), reverse=True)
    return (la + 0.05) / (lb + 0.05)


def fills(img, box):
    """The opaque colours inside {box} (x0, y0, x1, y1, exclusive end), most common first."""
    counts = {}
    for p in img.crop(box).get_flattened_data():
        if p[3] == 255:
            counts[p[:3]] = counts.get(p[:3], 0) + 1
    return [c for c, _ in sorted(counts.items(), key=lambda kv: -kv[1])]


def contrast_rows(load):
    """(sprite, ink, main fill, ratio on it, ratio on its second fill) for every pair CONTRAST holds; a box with no opaque
    pixel gives (sprite, ink, None, None, None), which problems() reports."""
    rows = []
    for target, (box, inks) in CONTRAST.items():
        found = fills(load(target), box)
        if not found:
            rows.extend((target, ink, None, None, None) for ink in inks)
            continue
        for ink in inks:
            colour = _rgb(PALETTE[ink])
            rows.append((target, ink, found[0], contrast(colour, found[0]),
                         contrast(colour, found[1]) if len(found) > 1 else None))
    return rows


def baseline(entry, src):
    """The canvas an adapted copy is held to: the source, or the pieces its region names."""
    region = entry["region"]
    if "pieces" not in region:
        return src.copy()
    out = Image.new("RGBA", region["size"])
    for sx, sy, w, h, dx, dy in region["pieces"]:
        out.paste(src.crop((sx, sy, sx + w, sy + h)), (dx, dy))
    return out


def region_problems(target, entry, made, read=read_library):
    """Where {made} differs from its source outside the region its entry states."""
    region = entry["region"]
    src = _open(read(entry["source"]))
    base = baseline(entry, src)
    if made.size != base.size:
        return [f"{target}: {made.size[0]}x{made.size[1]}, but its region makes {base.size[0]}x{base.size[1]}"]
    overlay = _open(read(region["overlay"])) if "overlay" in region else None
    colours = {_rgb(c) for c in region.get("colours", ())}
    out = []
    mp, bp = made.load(), base.load()
    bad = 0
    for y in range(made.height):
        for x in range(made.width):
            m, b = mp[x, y], bp[x, y]
            if m == b or not m[3] and not b[3]:
                continue  # a clear pixel is clear, whatever colour it carries
            by_overlay = overlay is not None and overlay.getpixel((x, y))[3] > 0
            if region.get("all"):
                # A change of tone everywhere; with "alpha", the shape kept, except where an overlay's opaque pixels lie.
                if region.get("alpha") and m[3] != b[3] and not by_overlay:
                    bad += 1
                continue
            in_box = any(x0 <= x <= x1 and y0 <= y <= y1 for x0, y0, x1, y1 in region.get("boxes", ()))
            by_colour = b[3] and b[:3] in colours
            allowed = (in_box and by_colour) if region.get("both") else (in_box or by_colour or by_overlay)
            if not allowed:
                bad += 1
    if bad:
        out.append(f"{target}: {bad} pixel(s) differ from {entry['source']} outside the region its adapter states")
    return out


def problems(lang=None, load=None, read=read_library):
    """Everything check_magic_ui holds here (tools/check_mod_data.py adds the Java's side). {lang}: the generated
    language file (default: this module's own entries over the committed file); {load}: a runtime image by its path
    under assets/jugcraft (default: the committed files)."""
    out = []
    if lang is None:
        path = os.path.join(ASSETS, "lang", "en_us.json")
        with open(path, encoding="utf-8") as handle:
            lang = json.load(handle)
    if load is None:
        def load(target):
            with open(os.path.join(ASSETS, target), "rb") as handle:
                return Image.open(io.BytesIO(handle.read()))

    # The art: every copy decodes as RGBA, every adapted copy changes only its stated region, every .mcmeta fits.
    taken = {}
    for table in (OWNER_FILES, ADAPTED, BUILT):
        for target in table:
            if taken.setdefault(target, table) is not table:
                out.append(f"{target}: listed twice in tools/magic_ui.py")
    for target in list(OWNER_FILES) + list(ADAPTED):
        if any(word in source(target).lower() for word in NEVER):
            out.append(f"{target}: {source(target)} is never imported (spec 2.10)")
    for target in list(OWNER_FILES) + list(ADAPTED) + list(BUILT):
        try:
            img = load(target)
            img.load()
        except (OSError, ValueError):
            out.append(f"{target} is missing or unreadable: run tools/owner_art.py")
            continue
        if img.mode != "RGBA":
            out.append(f"{target} is {img.mode}, not RGBA (critique 17): run tools/owner_art.py")
            continue
        if target in OWNER_FILES:
            if img.tobytes() != _open(read(OWNER_FILES[target])).tobytes():
                out.append(f"{target}: differs from {OWNER_FILES[target]} (an RGBA copy changes no pixel)")
        elif target in ADAPTED:
            out.extend(region_problems(target, ADAPTED[target], img, read))
    for target, meta in MCMETA.items():
        scaling = meta["gui"]["scaling"]
        border = scaling["border"]
        border = border if isinstance(border, dict) else dict.fromkeys(("left", "top", "right", "bottom"), border)
        try:
            img = load(target)
        except (OSError, ValueError):
            continue
        if (scaling["width"], scaling["height"]) != img.size:
            out.append(f"{target}.mcmeta: {scaling['width']}x{scaling['height']}, but the sprite is {img.size[0]}x{img.size[1]}")
        if border["left"] + border["right"] >= scaling["width"] or border["top"] + border["bottom"] >= scaling["height"]:
            out.append(f"{target}.mcmeta: its borders leave no centre slice")
        if sprite_id(target) is None:
            out.append(f"{target}.mcmeta: gui.scaling applies only to GUI-atlas sprites")
    splash = ADAPTED[S + "page_title_frame.png"]
    x0, y0, x1, y1 = splash["box"]
    try:
        painted = load(S + "page_title_frame.png")
        if any(painted.getpixel((x, y))[3] for x in range(x0, x1 + 1) for y in range(y0, y1 + 1)):
            out.append("magic/page_title_frame: the \"ARS NOUVEAU\" lettering is not painted out")
    except (OSError, ValueError):
        pass
    try:
        for target, ink, fill, ratio, _ in contrast_rows(lambda t: load(t).convert("RGBA")):
            if fill is None:
                out.append(f"{target}: no opaque fill inside its contrast box, so {ink} cannot be measured (spec 2.3)")
            elif ratio < 4.5:
                out.append(f"{target}: {ink} on its fill #{'%02x%02x%02x' % fill} is {ratio:.2f}:1, under 4.5:1 (spec 2.3)")
    except (OSError, ValueError):
        pass

    # The words: every glance renders its expected English from the server's real arguments by position, names a real
    # server sentence with that many arguments, and keeps to its limit (in words, and for blockers, rows and the service
    # line in the pixels the narrowest pact screen gives them, in the default font's English).
    keys = {}

    def fits(key, got, kind):
        found = []
        if nwords(got) > LIMITS[kind]:
            found.append(f"{key}: {nwords(got)} words, over the {kind} limit of {LIMITS[kind]} (spec 1.1): {got!r}")
        room = {"blocker": BLOCKER_WIDTH, "row": ROW_WIDTH}.get(kind)
        if room and lines(got, room) > 2:
            found.append(f"{key}: {lines(got, room)} lines at {room} px, over a {kind}'s two (spec 1.1): {got!r}")
        if kind == "service" and width(got) > SERVICE_ROOM:
            found.append(f"{key}: {width(got)} px, over the header's {SERVICE_ROOM} for its service line: {got!r}")
        return found
    for server, glances in GLANCES.items():
        full = lang.get(full_key(server))
        if full is None:
            out.append(f"glance {server}: no server sentence {full_key(server)}")
            continue
        arity = len(_PLACE.findall(full.replace("%%", "")))
        for suffix, pattern, args, expected, kind in glances:
            key = glance_key(server, suffix)
            if keys.setdefault(key, server) != server:
                out.append(f"{key}: the glance of two server sentences")
            if lang.get(key) != pattern:
                out.append(f"{key}: the language file is not tools/magic_ui.py's (run tools/generate_material_data.py)")
            top, bare = placeholders(pattern)
            if bare:
                out.append(f"{key}: a bare %s; glances name their arguments by position (critique 3)")
            if len(args) != arity:
                out.append(f"{key}: its sample has {len(args)} argument(s), the server sentence {arity}")
            if top > arity:
                out.append(f"{key}: names argument {top}, the server sentence has {arity}")
            try:
                got = render(pattern, args)
            except (IndexError, StopIteration):
                out.append(f"{key}: does not render with its sample arguments")
                continue
            if got != expected:
                out.append(f"{key}: renders {got!r}, expected {expected!r}")
            out.extend(fits(key, got, kind))
    # A glance whose argument is another sentence of the server's (a first task's act, a research step's description) is
    # also rendered with every one that argument can be, each drawn as the client draws it: by its own glance when it has
    # one (nested_key), else whole.
    import concordance_pacts as pacts
    import concordance_garden_pacts as garden
    steps = []
    for name, spirit in pacts.SPIRITS.items():
        research = spirit["pact"]["research"].split(":", 1)[-1].replace("/", "_")
        steps += [f"{STEP_PREFIX}concordance_{research}_{state}.description" for state in ("encountered", "observed", "understood")]
    nested = {"garden_pact.obligation": (1, [full_key(f"garden_pact.act.{act}") for act in garden.ACTS]),
              "pact.missing.step": (3, sorted(set(steps)))}
    for server, (position, options) in nested.items():
        for suffix, pattern, args, _, kind in GLANCES.get(server, ()):
            for option in options:
                if option not in lang:
                    out.append(f"{glance_key(server, suffix)}: {option}, which it can be given, has no language entry")
                    continue
                inner = nested_key(option)
                drawn = lang[inner] if inner in lang else lang[option]
                if placeholders(drawn) != (0, False):
                    out.append(f"{inner or option}: drawn inside {server}, so it takes no arguments of its own")
                    continue
                given = list(args)
                given[position - 1] = drawn
                out.extend(fits(f"{glance_key(server, suffix)} with {option}", render(pattern, given), kind))
    for step, text in STEP_GLANCES.items():
        key = f"screen.jugcraft.pact.glance.step.{step}"
        keys[key] = step
        if f"{STEP_PREFIX}{step}.description" not in lang:
            out.append(f"{key}: no advancement description {STEP_PREFIX}{step}.description")
        if lang.get(key) != text:
            out.append(f"{key}: the language file is not tools/magic_ui.py's (run tools/generate_material_data.py)")
        if placeholders(text) != (0, False):
            out.append(f"{key}: a step's glance takes no arguments")
        out.extend(fits(key, text, "blocker"))
    servers = {k[len(SERVER_PREFIX):] for k in lang if k.startswith(SERVER_PREFIX)}
    for server in servers:
        if glance_key(server) in keys and keys[glance_key(server)] != server:
            out.append(f"{glance_key(server)}: a glance key that is also a server sentence's own")
    for server, (pattern, args, expected) in CARDS.items():
        key = card_key(server)
        full = lang.get(full_key(server))
        if full is None:
            out.append(f"card {server}: no server sentence")
            continue
        if lang.get(key) != pattern:
            out.append(f"{key}: the language file is not tools/magic_ui.py's (run tools/generate_material_data.py)")
        top, bare = placeholders(pattern)
        if bare or top > len(_PLACE.findall(full)) or len(args) != len(_PLACE.findall(full)):
            out.append(f"{key}: its arguments do not fit {server}")
        elif render(pattern, args) != expected or nwords(expected) > LIMITS["card"]:
            out.append(f"{key}: renders {render(pattern, args)!r}, expected {expected!r} in at most {LIMITS['card']} words")
    for name, (text, kind) in SHORT.items():
        shown = render(text, SHORT_SAMPLES.get(name, ()))
        if nwords(shown) > LIMITS[kind]:
            out.append(f"screen.jugcraft.pact.short.{name}: {nwords(shown)} words, over the {kind} limit of {LIMITS[kind]}")
        if lang.get(f"screen.jugcraft.pact.short.{name}") != text:
            out.append(f"screen.jugcraft.pact.short.{name}: the language file is not tools/magic_ui.py's")
    for key in lang:
        if key.startswith(("screen.jugcraft.pact.glance.", "screen.jugcraft.roster.glance.")) and key not in keys \
                and key not in {card_key(s) for s in CARDS}:
            out.append(f"{key}: a glance tools/magic_ui.py does not hold")
    # Every row, blocker, term and answer the server can send has a glance or is folded into another element (spec 1.7).
    sent = [f"pact.term.{term}" for term in ("hours", "hours.any", "area", "quota", "upkeep", "grace", "suspension",
                                             "duration", "breach", "ending", "returns", "perch", "beneficiaries.personal",
                                             "beneficiaries.party", "compact", "compact.items", "compact_paid")]
    sent += [k for k in pacts.MESSAGES if k.startswith(("pact.row.", "pact.missing.", "pact.answer.", "pact.reckoning."))]
    sent += [k for k in garden.MESSAGES if k.startswith("garden_pact.obligation")]
    sent += list(ANSWERS)
    for work, words in pacts.SERVICE_WORDS.items():
        if work not in WORK_GLANCES:
            out.append(f"{work}: no WORK_GLANCES row in tools/magic_ui.py (its service line, rows, blockers and card)")
        sent += [f"pact.{work}.{key}" for key in words["messages"] if key.startswith(("row.", "missing", "matter."))]
        # The matter in general, which the kept page draws when none is raised now and an answer is kept (PactAccords).
        sent += [f"pact.{work}.question"] if words.get("accords") else []
    for work in pacts.WORK:
        if work in pacts.SERVICE_WORDS or work == pacts.RECKONING_WORK:
            sent.append(f"pact.term.service.{work}")
    for server in sent:
        if server not in GLANCES and server not in FOLDED:
            out.append(f"{server}: the server can send it, but it has neither a glance nor a place in FOLDED (spec 1.7)")
    # Every blocker is drawn as a lock line (spec 1.1, 1.2: never colour alone), so its key takes the lock mark.
    for server, glances in GLANCES.items():
        if any(kind == "blocker" for *_, kind in glances) and mark(server) != "lock":
            out.append(f"{server}: a blocker, but MARK_RULES gives it {mark(server)!r}, not the lock (spec 1.1, 1.2)")
    for work, rows in WORK_GLANCES.items():
        carded = {key for key, _ in work_cards(rows)}
        for key in pacts.SERVICE_WORDS.get(work, {}).get("messages", {}):
            if key.startswith("missing") and key not in carded:
                out.append(f"{work}: no chooser card reason for {key}")
        if not re.fullmatch(r"[a-z0-9_.-]+:[a-z0-9_/.-]+", rows.get("icon") or ""):
            out.append(f"{work}: no icon (an item id) in WORK_GLANCES (its service line's and its own rows' item)")
        if not rows.get("name") or not rows.get("line"):
            out.append(f"{work}: no name or line in WORK_GLANCES (the beats' header and the service line)")
        # Today's gauge reads its first two arguments as done of quota (PactScreen.work), so its row says exactly that.
        for suffix, pattern, *_ in rows.get("row.service", ()):
            if suffix == "" and pattern != "%1$s/%2$s today":
                out.append(f"pact.{work}.row.service: {pattern!r}, but the gauge draws \"%1$s/%2$s today\" (done, then the "
                           "day's quota) and nothing else")
    for kind, text, name in ICON_RULES:
        if kind not in ("prefix", "suffix", "exact", "contains") or not (
                name == "work" or name.startswith("glyph:") and name[6:] in GLYPHS
                or re.fullmatch(r"item:[a-z0-9_.-]+:[a-z0-9_/.-]+", name)):
            out.append(f"ICON_RULES {kind} {text!r}: {name!r} is not work, glyph:<one of GLYPHS> or item:<id>")
    # The beats (spec 1.5): at most three, each line at most 18 words; the greeting's stage direction (its own key, which
    # the screen draws dim) at most 8 words and the greeting's first words.
    for name, spirit in pacts.SPIRITS.items():
        if len(spirit["beats"]) > 3:
            out.append(f"{name}: {len(spirit['beats'])} beats, over the three before the terms (spec 1.1)")
        stage = lang.get(f"message.jugcraft.concordance.pact.{name}.beat.greeting.stage")
        for beat in spirit["beats"]:
            text = lang.get(f"message.jugcraft.concordance.pact.{name}.beat.{beat}", "")
            line = text
            if beat == "greeting" and stage:
                if not text.startswith(stage + " "):
                    out.append(f"{name}.beat.greeting: does not begin with its stage direction {stage!r}")
                else:
                    line = text[len(stage):].strip()
                if nwords(stage) > LIMITS["stage"]:
                    out.append(f"{name}.beat.greeting.stage: {nwords(stage)} words, over {LIMITS['stage']} (spec 1.1)")
            if nwords(line) > LIMITS["beat"]:
                out.append(f"{name}.beat.{beat}: his line is {nwords(line)} words, over {LIMITS['beat']} (spec 1.1)")
    out.extend(unit2_problems(lang))
    return out


def unit2_problems(lang):
    """Unit 2's words (the roster's cards, Jade's short lines): every roster state and answer the server sends has its
    glance; a card's state and fact fit the narrowest card; every short form of a whole sentence renders its expected
    English from the sentence's own arguments by position, within its limit; every mark names a sentence and a mark."""
    out = []
    import concordance_roster as roster
    for key in roster.MESSAGES:
        if key.startswith(("roster.waiting.", "roster.answer.")) and key not in ROSTER_GLANCES:
            out.append(f"{key}: the roster can send it, but ROSTER_GLANCES holds no glance for it (a card's state, an answer)")
    for server, glances in ROSTER_GLANCES.items():
        for _, _, _, expected, kind in glances:
            room = {"state": STATE_ROOM, "fact": FACT_ROOM}.get(kind)
            if room and width(expected) > room:
                out.append(f"{glance_key(server)}: {width(expected)} px, over a card's {room} for its {kind}: {expected!r}")
    for key, (pattern, args, expected, kind) in SENTENCES.items():
        short = SENTENCE_PREFIX + key
        full = lang.get(key)
        if full is None:
            out.append(f"{short}: no sentence {key}")
            continue
        if lang.get(short) != pattern:
            out.append(f"{short}: the language file is not tools/magic_ui.py's (run tools/generate_material_data.py)")
        top, bare = placeholders(pattern)
        arity = len(_PLACE.findall(full.replace("%%", "")))
        if bare:
            out.append(f"{short}: a bare %s; a short form names its arguments by position")
        if len(args) != arity or top > arity:
            out.append(f"{short}: its arguments do not fit {key} ({arity})")
            continue
        got = render(pattern, args)
        if got != expected:
            out.append(f"{short}: renders {got!r}, expected {expected!r}")
        if nwords(got) > LIMITS[kind]:
            out.append(f"{short}: {nwords(got)} words, over the {kind} limit of {LIMITS[kind]}: {got!r}")
        if kind == "state" and width(got) > STATE_ROOM:
            out.append(f"{short}: {width(got)} px, over a card's {STATE_ROOM} for its state: {got!r}")
    for key, mark_name in SENTENCE_MARKS.items():
        if key not in lang:
            out.append(f"SENTENCE_MARKS {key}: no such sentence")
        if mark_name not in MARKS:
            out.append(f"SENTENCE_MARKS {key}: {mark_name} is not one of MARKS")
    for name, (text, kind) in ROSTER_SHORT.items():
        key = f"screen.jugcraft.roster.short.{name}"
        shown = render(text, ROSTER_SAMPLES.get(name, ()))
        if lang.get(key) != text:
            out.append(f"{key}: the language file is not tools/magic_ui.py's (run tools/generate_material_data.py)")
        if placeholders(text)[1]:
            out.append(f"{key}: a bare %s")
        if nwords(shown) > LIMITS[kind]:
            out.append(f"{key}: {nwords(shown)} words, over the {kind} limit of {LIMITS[kind]}: {shown!r}")
    for key in lang:
        if key.startswith(SENTENCE_PREFIX) and key[len(SENTENCE_PREFIX):] not in SENTENCES:
            out.append(f"{key}: a short form tools/magic_ui.py's SENTENCES does not hold")
        if key.startswith("screen.jugcraft.roster.short.") and key[len("screen.jugcraft.roster.short."):] not in ROSTER_SHORT:
            out.append(f"{key}: a roster short word tools/magic_ui.py's ROSTER_SHORT does not hold")
    return out


if __name__ == "__main__":
    found = problems()
    print("\n".join(found) or "Magic interfaces: art, glances and short labels hold.")
    raise SystemExit(1 if found else 0)
