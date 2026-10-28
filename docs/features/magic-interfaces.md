# Magic interfaces: the owner's magic UI kit and far less reading

Status: **unit 1 in progress, not merged**: its first commit (this record's first version) imports the owner's magic
interface kit as runtime art and adds the shared Magic layer; **no screen uses it yet**. The pact screen moves onto it in
unit 1's next commit; unit 1 ends with an evidence checkpoint (the client shots of every pact state beside the design's
pictures). Built on the garden cohort's line (draft pull request
[#526](https://github.com/jimbozoomer-byte/jugcraft/pull/526): units 1 and 2, unit 3's shared prefix, merged with main).
Proposal issue: none; the owner's request of 28 October 2026 is the scope.
Owner: @jimbozoomer-byte. AI-assisted implementation with Claude Code; the model is named in the commit trailers.
Target milestone and tier: every tier's magic screens (the pact perch first, then the roster, the composer, the Field
Guide and the alchemy and Jug Nouveau workshops).
Primary specialty and supported player role: anyone who binds a spirit, keeps a roster or reads the codex.

The owner, 28 October 2026: "The pacts and honestly 99% of the magic USERINTERFACE. in general looks totally bland and
also is WAYYYY to wordy like needs to be simplified significantly interms of how much reading there is, but the style
needs to be seriously stepped up its awful."

The design is a spec with its critique applied, its surveys of the code, the kit and the words players read, and 65
mockups (kept with the session's design notes, not in the repository). Its answer, in short:

- **A hard text budget.** Numbers stand as numbers, items as item stacks, periods paid ahead as sockets, today's work
  as a gauge; each term is a chip (a glyph and at most three words). Every server sentence stays whole as the chip's or
  row's message: its narration and its hover or keyboard-focus tip. Buer's terms page goes from 267 words drawn to 34.
- **One look from the owner's kit** (variant B, the relic scroll card): the animated cosmos, the gilded portrait header
  with the live spirit, parchment scrolls on gold-capped rollers, wooden plates for actions, gold arrows for paging,
  purple bookmarks for tabs, gold-framed item slots and tooltips.

## Decisions

| Decision | Status |
|---|---|
| The pact screens, the roster and the composer take **variant B** (the relic scroll card); the book stays for reading (Field Guide, codex) | **Decided by the session** (recommended variant B; the owner was shown both; `MagicTheme` keeps `BOOK` for variant A) |
| **D-S15 amended:** before Accept every term is a chip, each narrated and hovered with its full sentence; the six standing terms share one chip, "Standing terms", whose hover says they are the same for every spirit, whose narration reads all six sentences, and whose More toggle shows their six lines in place of the chips; every blocker is drawn above the terms; the price stays in view | **Decided by the session, in answer to the owner's request for far less reading; reviewable at the checkpoint.** Not owner-accepted. This commit adds the words (the standing lines are the glances of `term.suspension` to `term.perch`, "Standing terms" and its hint); the screen applies it in unit 1's next commit, with `check_pact_screen` changed with it |
| **The beats shortened** (spec 1.5: at most 18 words a line, a stage direction of at most 8) and a `Terms` skip on beats 1 and 2 | **Decided by the session, in answer to the owner's request for far less reading; reviewable at the checkpoint.** Not owner-accepted. Not in this commit: the beats are rewritten with the screen |
| The service sentences (`term.service.<work>`) stay as shipped; GC-S7's two phrases stay in them | Spec 1.6 (a rewrite saved four to six words) |

## What this commit adds

- **`tools/magic_ui.py`**: the kit's runtime art and the short words.
  - `OWNER_FILES` (40 files copied unchanged, every one written as RGBA: twenty of the originals are palette PNGs with a
    transparency chunk and the gauge frame's sheet is RGB with a colour key);
  - `ADAPTED` (45 files), each made by a named adapter with a stated region; `check_magic_ui` proves each differs from
    its library file only there (the notches cleaned out of the scrolls' tiled middle, the header's painted sockets
    cleared, crops, plate tones, inked paper strips, pink bookmarks, arrows and action icons in every state, the glyph
    strip's glyphs made white, the "ARS NOUVEAU" lettering painted out of the splash frame, the cosmos cut to eight
    frames of its blob);
  - `BUILT` (20 glyphs and marks drawn for Jugcraft in the strip's one-pixel style: clock, ring, calendar, hourglass,
    person, party, scroll, pause, cross, lock, tick, pin, arrow, ask, bed, and the tick, lock, cross, ask and hourglass
    marks);
  - `MCMETA` (the nine-slice borders of spec 4.2: the plain scroll L22 T16 R24 B17, the ruled scroll L22 T30 R24 B44 so
    every height 88 + 14k keeps its rules fourteen apart, the header sideways only, plates L3 R7, the gold frame 3, the
    book never stretched);
  - `GLANCES`, `WORK_GLANCES`, `CARDS`, `ROSTER_GLANCES` and `SHORT`: 69 glances of server sentences (11 of them the roster's), 8 chooser
    reasons and 26 short labels, generated into the language file. A glance is a client key fed the server's own
    arguments **by position** (`%3$s`), so "1 / 7 days" can never read "1 per Buer's Offerings days" (critique 3).
- **Wiring:** `tools/owner_art.py` writes and checks the kit beside the other owner imports (`--provenance magic_ui`
  lists it); `tools/concordance.py` writes the words.
- **The Magic layer** (client, no screen uses it yet): `MagicArt` (sprites and drawing: panels sliced, never stretched;
  the cosmos and the gauge's fill by time, still under reduced motion; sockets; slots; glyphs in their surface's ink;
  marks; the gold-frame tip), `MagicTheme` (one palette: `SCROLL`, `BOOK`, `ALCHEMY`), `MagicLayout` (full from 240
  tall when the body's words fit, compact otherwise, narrow under 360 wide), `MagicScreen` (the keyed controls, rebuild
  and focus, `outside()` over every control, a record of the strings drawn for the word budgets), `MagicButton` (plates,
  paper, arrows, action icons, bookmarks; the message always the whole label; a footer tip below or beside the footer,
  never over the body), `MagicText`, `MagicChip`, `MagicStacks` (slots, counts, short marks, an offering tag cycling
  with its "+N"), `MagicPortrait` (the live spirit at the perch, or the sign) and `MagicPortraits` (the visual-only
  cache, ticked so a spirit plays its idle pose), `PactGlance` and `RosterGlance` (pure formatting from a sentence's
  key and arguments; anything unknown shows the whole sentence).
- **`PactAfford`** (common, pure): counts the inventory against a compact, the fullest carried astrolabe's charge
  included. `PactCompact.affords` now asks it and nothing else, so the screen's short marks and the seal's decision
  never disagree; the seal's rule is unchanged (infinite materials pay nothing; each material counted; one astrolabe
  holding at least the resonance).
- **`check_magic_ui`** (tools/check_mod_data.py) and **`scripts/tests/test_magic_ui.py`**.

## Record fields (CLAUDE.md)

| Field | The magic interfaces (this commit) |
|---|---|
| Tier | every tier's magic screens; nothing is gated by it |
| Input producer | the server's own sentences and arguments (PactStatePayload, RosterStatePayload: unchanged); the owner's library |
| Output consumer | the pact screen (unit 1's next commit), the roster and chooser (unit 2), the composer and Field Guide (unit 3), the alchemy and Nouveau screens (unit 4) |
| Costs | none in play; about 3 MB of texture memory for the cosmos, the sprites in the GUI atlas |
| Unlocks | nothing; presentation only |
| Failure behaviour | a sentence without a glance, or a glance naming more arguments than the server sent, shows the whole sentence; a spirit that is not standing at the perch shows its sign; the screen's short marks are display only, and the seal still decides through the same count |
| Test evidence | `check_magic_ui` (27 mutations caught), `scripts/tests/test_magic_ui.py` (22 tests) and the generators' checks, run locally; nothing compiled or run in Minecraft yet (no screen uses the layer) |

## Connections

- **Producers:** the pact menu and roster (their sentences and arguments, unchanged), the pact compact (its count).
- **Consumers:** the magic screens as each moves onto the layer (`tools/magic_ui.py` `MAGIC_SCREENS`), in the order of
  the design's units: the pact screen, the roster and chooser, the composer and Field Guide, the alchemy and Nouveau
  family.
- **Garden units 4 to 6** (Marax, Foras, Bifrons, Amdusias) add each work's row to `WORK_GLANCES` (its service line,
  kept-page rows, blockers and chooser reason); `check_magic_ui` names any row the server can send without one. Marks
  follow the shape of the server key (`MARK_RULES`), so a work's `missing`, `row.overworked`-like and `matter.*`
  sentences get theirs with no Java change.

## Multiplayer, server authority and persistence

No payload, request or server decision changes. The client reads the arguments the server already sends
(`ComponentSerialization.TRUSTED_STREAM_CODEC` keeps a translatable sentence's key and arguments). `PactAfford` runs on
both sides from the same inventory; on the client it only marks. Nothing is saved; no ID is added to a registry.

## Framework claims checked against Minecraft 26.3

- **A custom tooltip style exists in 26.3** (spec 2.8, critique 9): `Tooltip.create(Component, Optional<TooltipComponent>,
  Identifier style)`; `WidgetTooltipHolder` passes the style to `GuiGraphicsExtractor.setTooltipForNextFrame`; and
  `TooltipRenderUtil` draws `<namespace>:tooltip/<style>_background` and `_frame`, nine-sliced 12 out from the words.
  So `jugcraft:magic` draws `tooltip/magic_background` and `tooltip/magic_frame`. The layer draws its tips itself
  (`MagicArt.tip`, overriding `AbstractWidget.extractTooltipForNextRenderPass`) only so a footer tip can be placed by
  its own `ClientTooltipPositioner`; the style is vanilla's.
- **Modonomicon:** nothing of it is used in this unit (the codex theme is unit 3, after its jar is checked).

## Dependencies and assets

No new dependency, no mixin, no GuiLib. The art is the owner's magic collection
([MAGIC_ASSETS.md](../../art/owner-library/MAGIC_ASSETS.md)), authorized for direct use on 8 October 2026 and declared
remade by the owner on 9 October ([PROVENANCE.md](../../art/owner-library/PROVENANCE.md)). Its folders keep their
original labels (`ars_jimbaux`, `jymbelics`); nothing in the game uses them. The imported files carry no credits or
notices of their own; the collection's leftover metadata is recorded in the library's
[metadata inventory](../../art/owner-library/catalog/legacy-metadata.csv). The splash frame's "ARS NOUVEAU" lettering
is painted out of the runtime copy and checked; the Discord tab, the Starbuncle plush, the Lily and Nook icons and the
Waystones icons that copy vanilla faces and blocks are not imported (`magic_ui.NEVER`). The originals are unchanged and
nothing in the library is executed.

Every runtime file, its library source, the source's SHA-256 and what changed (`python3 tools/owner_art.py --provenance
magic_ui` prints the same rows with the adapter's name; `check_magic_ui` holds this table to the files):

| Runtime file (under `assets/jugcraft/`) | Library source (under `art/owner-library/`) | SHA-256 | Change |
|---|---|---|---|
| `textures/gui/magic/cosmos.png` | `originals/Magic/assets/jymbelics/textures/gui/description/general/background.png` | `4c4a88ffe3fd5878b3212a3e064ece5eed8bdde00a4cba01a66e25bbcb4905d2` | every other frame of sixteen, each cut to the blob's alpha box (4,14 407x234), stacked |
| `textures/gui/sprites/magic/action/end.png` | `originals/Magic/assets/jymbelics/textures/gui/description/ability/upgrade_button_active.png` | `4f2b06d9979e76fcc7ca1c2ed1b1e399ef51bbb669ddf92c2d15e8b7c022276f` | the upgrade frame's arrow covered (x 3-10, y 3-9) and a cross or two bars set in it; highlighted: the kit's outline |
| `textures/gui/sprites/magic/action/end_highlighted.png` | `originals/Magic/assets/jymbelics/textures/gui/description/ability/upgrade_button_active.png` | `4f2b06d9979e76fcc7ca1c2ed1b1e399ef51bbb669ddf92c2d15e8b7c022276f` | the upgrade frame's arrow covered (x 3-10, y 3-9) and a cross or two bars set in it; highlighted: the kit's outline |
| `textures/gui/sprites/magic/action/end_inactive.png` | `originals/Magic/assets/jymbelics/textures/gui/description/ability/upgrade_button_inactive.png` | `1448f0a53421bf1c62a8a70db7c224fcba05ecc983efcd91e0fa78cbc1eef41d` | the upgrade frame's arrow covered (x 3-10, y 3-9) and a cross or two bars set in it; highlighted: the kit's outline |
| `textures/gui/sprites/magic/action/offer.png` | `originals/Magic/assets/jymbelics/textures/gui/description/ability/upgrade_button_active.png` | `4f2b06d9979e76fcc7ca1c2ed1b1e399ef51bbb669ddf92c2d15e8b7c022276f` | none: written as RGBA |
| `textures/gui/sprites/magic/action/offer_highlighted.png` | `originals/Magic/assets/jymbelics/textures/gui/description/ability/upgrade_button_active.png` | `4f2b06d9979e76fcc7ca1c2ed1b1e399ef51bbb669ddf92c2d15e8b7c022276f` | the kit's action outline over it |
| `textures/gui/sprites/magic/action/offer_inactive.png` | `originals/Magic/assets/jymbelics/textures/gui/description/ability/upgrade_button_inactive.png` | `1448f0a53421bf1c62a8a70db7c224fcba05ecc983efcd91e0fa78cbc1eef41d` | none: written as RGBA |
| `textures/gui/sprites/magic/action/refresh.png` | `originals/Magic/assets/jymbelics/textures/gui/description/ability/reroll_button_active.png` | `a59b016ab74310cc15b8f45a681ac43ef857e92f268435ffe25fe4f278b96aa7` | none: written as RGBA |
| `textures/gui/sprites/magic/action/refresh_highlighted.png` | `originals/Magic/assets/jymbelics/textures/gui/description/ability/reroll_button_active.png` | `a59b016ab74310cc15b8f45a681ac43ef857e92f268435ffe25fe4f278b96aa7` | the kit's action outline over it |
| `textures/gui/sprites/magic/action/refresh_inactive.png` | `originals/Magic/assets/jymbelics/textures/gui/description/ability/reroll_button_inactive.png` | `e0e5d428474a577d2a54743aaccbdd21b2892f31abbf8e948d260b47e5a6f279` | none: written as RGBA |
| `textures/gui/sprites/magic/action/suspend.png` | `originals/Magic/assets/jymbelics/textures/gui/description/ability/upgrade_button_active.png` | `4f2b06d9979e76fcc7ca1c2ed1b1e399ef51bbb669ddf92c2d15e8b7c022276f` | the upgrade frame's arrow covered (x 3-10, y 3-9) and a cross or two bars set in it; highlighted: the kit's outline |
| `textures/gui/sprites/magic/action/suspend_highlighted.png` | `originals/Magic/assets/jymbelics/textures/gui/description/ability/upgrade_button_active.png` | `4f2b06d9979e76fcc7ca1c2ed1b1e399ef51bbb669ddf92c2d15e8b7c022276f` | the upgrade frame's arrow covered (x 3-10, y 3-9) and a cross or two bars set in it; highlighted: the kit's outline |
| `textures/gui/sprites/magic/action/suspend_inactive.png` | `originals/Magic/assets/jymbelics/textures/gui/description/ability/upgrade_button_inactive.png` | `1448f0a53421bf1c62a8a70db7c224fcba05ecc983efcd91e0fa78cbc1eef41d` | the upgrade frame's arrow covered (x 3-10, y 3-9) and a cross or two bars set in it; highlighted: the kit's outline |
| `textures/gui/sprites/magic/arrow/left.png` | `originals/Magic/assets/jymbelics/textures/hud/abilities/arrow_left.png` | `5286f4dbf1bfda3dab3e3d4d508eaf7812b2fde91ac06bda0e47a53694f4a844` | set 1 in on 13x32; highlighted: the kit's outline over it; disabled: desaturated and halved |
| `textures/gui/sprites/magic/arrow/left_disabled.png` | `originals/Magic/assets/jymbelics/textures/hud/abilities/arrow_left.png` | `5286f4dbf1bfda3dab3e3d4d508eaf7812b2fde91ac06bda0e47a53694f4a844` | set 1 in on 13x32; highlighted: the kit's outline over it; disabled: desaturated and halved |
| `textures/gui/sprites/magic/arrow/left_highlighted.png` | `originals/Magic/assets/jymbelics/textures/hud/abilities/arrow_left.png` | `5286f4dbf1bfda3dab3e3d4d508eaf7812b2fde91ac06bda0e47a53694f4a844` | set 1 in on 13x32; highlighted: the kit's outline over it; disabled: desaturated and halved |
| `textures/gui/sprites/magic/arrow/right.png` | `originals/Magic/assets/jymbelics/textures/hud/abilities/arrow_right.png` | `cb1b558ea763316ac7c1970ea10cf84b857dac3b062ba9d6fc320a354595d96c` | set 1 in on 13x32; highlighted: the kit's outline over it; disabled: desaturated and halved |
| `textures/gui/sprites/magic/arrow/right_disabled.png` | `originals/Magic/assets/jymbelics/textures/hud/abilities/arrow_right.png` | `cb1b558ea763316ac7c1970ea10cf84b857dac3b062ba9d6fc320a354595d96c` | set 1 in on 13x32; highlighted: the kit's outline over it; disabled: desaturated and halved |
| `textures/gui/sprites/magic/arrow/right_highlighted.png` | `originals/Magic/assets/jymbelics/textures/hud/abilities/arrow_right.png` | `cb1b558ea763316ac7c1970ea10cf84b857dac3b062ba9d6fc320a354595d96c` | set 1 in on 13x32; highlighted: the kit's outline over it; disabled: desaturated and halved |
| `textures/gui/sprites/magic/bookmark/documentation.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/documentation_tab.png` | `54a7d00dd8673fe1a40a2d09b2424d8fc320e3e33b8e151796032cab54fddc21` | none: written as RGBA |
| `textures/gui/sprites/magic/bookmark/documentation_selected.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/documentation_tab.png` | `54a7d00dd8673fe1a40a2d09b2424d8fc320e3e33b8e151796032cab54fddc21` | the ribbon's four purples swapped for the filter tabs' selected pinks |
| `textures/gui/sprites/magic/bookmark/familiar.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/familiar_tab.png` | `4ea8acb92ee31778c918cb11dd9d2c76755b3e48638119fd5202b9ecb3da5020` | none: written as RGBA |
| `textures/gui/sprites/magic/bookmark/familiar_selected.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/familiar_tab.png` | `4ea8acb92ee31778c918cb11dd9d2c76755b3e48638119fd5202b9ecb3da5020` | the ribbon's four purples swapped for the filter tabs' selected pinks |
| `textures/gui/sprites/magic/bookmark/settings.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/settings_tab.png` | `25265a745ea7e9c7b7f847ed7c999034ababab83ef3043572128b14579f6ae87` | none: written as RGBA |
| `textures/gui/sprites/magic/bookmark/settings_selected.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/settings_tab.png` | `25265a745ea7e9c7b7f847ed7c999034ababab83ef3043572128b14579f6ae87` | the ribbon's four purples swapped for the filter tabs' selected pinks |
| `textures/gui/sprites/magic/bookmark/spell_style.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/spell_style_tab.png` | `5431015695114f41c1be510c1d4d6cb749873f3c8f609ee4a23a110d06f2825f` | none: written as RGBA |
| `textures/gui/sprites/magic/bookmark/spell_style_selected.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/spell_style_tab.png` | `5431015695114f41c1be510c1d4d6cb749873f3c8f609ee4a23a110d06f2825f` | the ribbon's four purples swapped for the filter tabs' selected pinks |
| `textures/gui/sprites/magic/bookmark/summon_circle.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/summon_circle_bookmark.png` | `3d557b985120f1a757bd81578afcc988c50041ab76098dbf203b18c6ca2eb556` | none: written as RGBA |
| `textures/gui/sprites/magic/bookmark/summon_circle_selected.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/summon_circle_bookmark.png` | `3d557b985120f1a757bd81578afcc988c50041ab76098dbf203b18c6ca2eb556` | the ribbon's four purples swapped for the filter tabs' selected pinks |
| `textures/gui/sprites/magic/card/chains.png` | `originals/Magic/assets/jymbelics/textures/gui/description/relic/chains.png` | `586c6a63bc5d638a819f7e75c4b7550277f411c41e44f9027282d202d31aed20` | none: written as RGBA |
| `textures/gui/sprites/magic/card/completed.png` | `originals/Magic/assets/jymbelics/textures/gui/description/icons/completed.png` | `bbdbb802c760715a0bc614e9db3627370f2629846ce0274dbe55431cc0bd5555` | none: written as RGBA |
| `textures/gui/sprites/magic/card/frame_active.png` | `originals/Magic/assets/jymbelics/textures/hud/abilities/card_frame_active.png` | `bff42cd877afcd5180741919fc9bf036d4de27ab130ddfb1d47d4308dbdcaa0f` | none: written as RGBA |
| `textures/gui/sprites/magic/card/frame_inactive.png` | `originals/Magic/assets/jymbelics/textures/hud/abilities/card_frame_inactive.png` | `6ffb0230545c0e52ff4dc4e4638056e0f02f4ea0091f2b536409772205dc2694` | none: written as RGBA |
| `textures/gui/sprites/magic/card/lock.png` | `originals/Magic/assets/jymbelics/textures/gui/description/relic/icons/lock_inactive.png` | `146efe3d7cb59bd00e0a366c94fae6f8671ceba36ec73f5b6dad178f5df400ba` | none: written as RGBA |
| `textures/gui/sprites/magic/doc/arrow_down.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/documentation/doc_detail_arrow_down.png` | `7936be447352b05ea4b955e71d2a9722ee167b43949e26cb11961de817e4d3ab` | none: written as RGBA |
| `textures/gui/sprites/magic/doc/frame_image.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/documentation/doc_frame_image.png` | `dbb9df4fec35f4acb3702c4edac132bff785f4d25fcb9b14097eefa2ab716391` | none: written as RGBA |
| `textures/gui/sprites/magic/doc/frame_item.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/documentation/doc_detail_frame_item.png` | `15bf2ff13610b5998bc7cb523cc66529714b7d3df8c5bc6a87e4e93365a851ac` | none: written as RGBA |
| `textures/gui/sprites/magic/doc/frame_pedestal.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/documentation/doc_detail_frame_pedestal.png` | `53520917d83ccc177f5293c656f667756f9527b676f8b2c5a89b291fc0dd36ab` | none: written as RGBA |
| `textures/gui/sprites/magic/doc/page_left.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/documentation/doc_button_next_page_left.png` | `5549bc1f481708d689b4d944aee27654b843edf4c2dbe9b1ba5911c211850ca7` | none: written as RGBA |
| `textures/gui/sprites/magic/doc/page_left_hover.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/documentation/doc_button_next_page_left_hover.png` | `89064dbd2d3c90128c9bdac421ed796eaaa342b0e45fdbdc3624d1ce1f4ee64a` | none: written as RGBA |
| `textures/gui/sprites/magic/doc/page_right.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/documentation/doc_button_next_page_right.png` | `2b2bac00890c75a95f9e5a0b91e3b107f9cd6d6a9c582c6fe843feb7536cdf21` | none: written as RGBA |
| `textures/gui/sprites/magic/doc/page_right_hover.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/documentation/doc_button_next_page_right_hover.png` | `a7f275738f46a32ba8e4059a8cbea56ac295858abb7631da5adf19927d09da75` | none: written as RGBA |
| `textures/gui/sprites/magic/doc/underline.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/documentation/doc_detail_underline.png` | `97309f60953f1c42a853ad42fdc5ef9fa4a286889ed06da2287ab5e64d263ff0` | none: written as RGBA |
| `textures/gui/sprites/magic/glyph/inf.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/bookwyrm_storage_tabs.png` | `810ebc84c8dfe4227c9891b228164dd6134207edf5c87904ca6ee8d2f9c10129` | one glyph cut from the strip (rows 3-9) and made white |
| `textures/gui/sprites/magic/glyph/key.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/bookwyrm_storage_tabs.png` | `810ebc84c8dfe4227c9891b228164dd6134207edf5c87904ca6ee8d2f9c10129` | one glyph cut from the strip (rows 3-9) and made white |
| `textures/gui/sprites/magic/glyph/moon.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/bookwyrm_storage_tabs.png` | `810ebc84c8dfe4227c9891b228164dd6134207edf5c87904ca6ee8d2f9c10129` | one glyph cut from the strip (rows 3-9) and made white |
| `textures/gui/sprites/magic/glyph/sparkle.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/bookwyrm_storage_tabs.png` | `810ebc84c8dfe4227c9891b228164dd6134207edf5c87904ca6ee8d2f9c10129` | one glyph cut from the strip (rows 3-9) and made white |
| `textures/gui/sprites/magic/glyph/spiral.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/bookwyrm_storage_tabs.png` | `810ebc84c8dfe4227c9891b228164dd6134207edf5c87904ca6ee8d2f9c10129` | one glyph cut from the strip (rows 3-9) and made white |
| `textures/gui/sprites/magic/glyph/sun.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/bookwyrm_storage_tabs.png` | `810ebc84c8dfe4227c9891b228164dd6134207edf5c87904ca6ee8d2f9c10129` | one glyph cut from the strip (rows 3-9) and made white |
| `textures/gui/sprites/magic/gold_frame.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/manabar_gui_border.png` | `f258a8e68494ccc30d475a3e304068e9ddf9bf4205e02749d69b634cfc82c13a` | cut out (socket: 4,63 8x8; gold frame: 6,6 102x12) |
| `textures/gui/sprites/magic/mana.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/manabar_gui_mana.png` | `009f1eb8178086ddb23e568bd3f38a38b78bd97a397d461653c3174ab07255f6` | none: written as RGBA |
| `textures/gui/sprites/magic/mark/moon.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/bookwyrm_storage_tabs.png` | `810ebc84c8dfe4227c9891b228164dd6134207edf5c87904ca6ee8d2f9c10129` | the strip's moon in colour with a one-pixel dark edge |
| `textures/gui/sprites/magic/marker/collapsed.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/collapsed_marker.png` | `6ed03d0dc02b400b39a2406b6bd802fd9a30ebafcb9c61320360e7a6f9aa63c4` | none: written as RGBA |
| `textures/gui/sprites/magic/marker/expanded.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/expanded_marker.png` | `633a679180fb66a36e83f53824c960e0e22ac48391e5a5ff77ec72831b9f1047` | none: written as RGBA |
| `textures/gui/sprites/magic/page_title_frame.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/documentation/doc_frame_splash_page.png` | `3904cd9cedab27e2b53f0f94fef0bb5f2c3372a421cef0bff6c30c060f4407c1` | the "ARS NOUVEAU" lettering (x 32-91, y 11-15) cleared to transparency |
| `textures/gui/sprites/magic/paper/accept.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/save_bar.png` | `56fdafb178281b2396bb0aec82de7b027ceb77b4debb07b2db15b906ae923a10` | the grey tick or cross (#bfb391, x < 16) inked #2f5a1a or #8f2a17; highlighted: a tenth brighter in a gold rim |
| `textures/gui/sprites/magic/paper/accept_highlighted.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/save_bar.png` | `56fdafb178281b2396bb0aec82de7b027ceb77b4debb07b2db15b906ae923a10` | the grey tick or cross (#bfb391, x < 16) inked #2f5a1a or #8f2a17; highlighted: a tenth brighter in a gold rim |
| `textures/gui/sprites/magic/paper/decline.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/clear_bar.png` | `28d9983e2b8defd89295f639612a38fe4bbe26e651bcad0fbb4ea8f02d50eedd` | the grey tick or cross (#bfb391, x < 16) inked #2f5a1a or #8f2a17; highlighted: a tenth brighter in a gold rim |
| `textures/gui/sprites/magic/paper/decline_highlighted.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/clear_bar.png` | `28d9983e2b8defd89295f639612a38fe4bbe26e651bcad0fbb4ea8f02d50eedd` | the grey tick or cross (#bfb391, x < 16) inked #2f5a1a or #8f2a17; highlighted: a tenth brighter in a gold rim |
| `textures/gui/sprites/magic/plate.png` | `originals/Magic/assets/jymbelics/textures/gui/description/general/plate_background.png` | `e675c0939bd9bf6e91e31f20157ce42f10110b4b36daf5302ad17f1d89516389` | darkened to 70% / verdigris with the gold rim at 86% / desaturated and halved; highlighted: the kit's outline over it |
| `textures/gui/sprites/magic/plate_disabled.png` | `originals/Magic/assets/jymbelics/textures/gui/description/general/plate_background.png` | `e675c0939bd9bf6e91e31f20157ce42f10110b4b36daf5302ad17f1d89516389` | darkened to 70% / verdigris with the gold rim at 86% / desaturated and halved; highlighted: the kit's outline over it |
| `textures/gui/sprites/magic/plate_highlighted.png` | `originals/Magic/assets/jymbelics/textures/gui/description/general/plate_background.png` | `e675c0939bd9bf6e91e31f20157ce42f10110b4b36daf5302ad17f1d89516389` | darkened to 70% / verdigris with the gold rim at 86% / desaturated and halved; highlighted: the kit's outline over it |
| `textures/gui/sprites/magic/plate_outline.png` | `originals/Magic/assets/jymbelics/textures/gui/description/general/plate_outline.png` | `fa52c3490e12d9ea3a2f97b68785535ddd735fb7bdece9f32f193ae6dba166ba` | none: written as RGBA |
| `textures/gui/sprites/magic/plate_primary.png` | `originals/Magic/assets/jymbelics/textures/gui/description/general/plate_background.png` | `e675c0939bd9bf6e91e31f20157ce42f10110b4b36daf5302ad17f1d89516389` | darkened to 70% / verdigris with the gold rim at 86% / desaturated and halved; highlighted: the kit's outline over it |
| `textures/gui/sprites/magic/plate_primary_highlighted.png` | `originals/Magic/assets/jymbelics/textures/gui/description/general/plate_background.png` | `e675c0939bd9bf6e91e31f20157ce42f10110b4b36daf5302ad17f1d89516389` | darkened to 70% / verdigris with the gold rim at 86% / desaturated and halved; highlighted: the kit's outline over it |
| `textures/gui/sprites/magic/portrait_card.png` | `originals/Magic/assets/jymbelics/textures/gui/description/ability/ability_background.png` | `dd88b9fcd517cb54c91770b418eb0405907e1a4f693e2664ef5e5da95f80aeaa` | the cleaned header's first 48 columns |
| `textures/gui/sprites/magic/relic_header.png` | `originals/Magic/assets/jymbelics/textures/gui/description/ability/ability_background.png` | `dd88b9fcd517cb54c91770b418eb0405907e1a4f693e2664ef5e5da95f80aeaa` | five painted sockets (x 4-43, y 63-70) given the frame's wood (rows 60-62) |
| `textures/gui/sprites/magic/school/abjuration.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/schools/abjuration_tooltip.png` | `9eacd06f84d97db89f3421d1c6af7e6120906748376369b56334b5d86f8492f4` | none: written as RGBA |
| `textures/gui/sprites/magic/school/air.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/schools/air_tooltip.png` | `e046f56dc51e366c7f77b2b918b5f88f659699a0b72e4e0294c279a9cebc2cff` | none: written as RGBA |
| `textures/gui/sprites/magic/school/conjuration.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/schools/conjuration_tooltip.png` | `6f30fe1debfd57192d8fa79fa3168542cc427b9c9060a60879389b6839869d85` | none: written as RGBA |
| `textures/gui/sprites/magic/school/earth.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/schools/earth_tooltip.png` | `b9fbc26b0e40f8d217fb3e222e5a93968a48e37433a2376126af58852db3b8ed` | none: written as RGBA |
| `textures/gui/sprites/magic/school/fire.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/schools/fire_tooltip.png` | `e1aa459814b3bb14b5e97eb0c4dd95de324bc3e1e4fafb07b4c0d01062421428` | none: written as RGBA |
| `textures/gui/sprites/magic/school/manipulation.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/schools/manipulation_tooltip.png` | `0ea211faec58e5850181db379d9bd9c19804818c7791e64f33181744b631ff96` | none: written as RGBA |
| `textures/gui/sprites/magic/school/water.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/schools/water_tooltip.png` | `4f0fa7d8ca1492004edf9a23c6aff506be1ba83b3564260d4e2dec30b09e0ae8` | none: written as RGBA |
| `textures/gui/sprites/magic/scroll_ball.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/scroll_ball.png` | `c32f80fc55a541317431670bdc542a8f4bedc8fd06192c14c7ab0af42273738d` | none: written as RGBA |
| `textures/gui/sprites/magic/scroll_plain.png` | `originals/Magic/assets/jymbelics/textures/gui/description/relic/abilities_background.png` | `ff2eb02d7e3ec22cb403c4f13a5fe454ff0ce5df6884f0766415c8e28f1786c6` | torn notches in the tiled middle given a clean neighbour's columns (x 50-53, 168-173 rows 6-17; 104-108 rows 64-82; 24-35 rows 74-80) |
| `textures/gui/sprites/magic/scroll_ruled.png` | `originals/Magic/assets/jymbelics/textures/gui/description/ability/stats_background.png` | `6238fd8cc3f39758c9a8563fe5101bf2405768a64231283546a0a8147f6b86d9` | torn notches in the tiled middle given a clean neighbour's columns (x 50-53, 168-173 rows 6-17; 104-108 rows 64-82; 24-35 rows 74-80) |
| `textures/gui/sprites/magic/socket.png` | `originals/Magic/assets/jymbelics/textures/gui/description/ability/ability_background.png` | `dd88b9fcd517cb54c91770b418eb0405907e1a4f693e2664ef5e5da95f80aeaa` | cut out (socket: 4,63 8x8; gold frame: 6,6 102x12) |
| `textures/gui/sprites/magic/spell_book.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/spell_book_template.png` | `4a9bf3be143691a293f7e52d4c0fc396a738a6594047e4ea0532880582d7e326` | none: written as RGBA |
| `textures/gui/sprites/magic/star/big_active.png` | `originals/Magic/assets/jymbelics/textures/gui/description/general/big_star_active.png` | `29e14520838bd0f3d988419a065ee3316145043f980db2aad6729cb12eeab73d` | none: written as RGBA |
| `textures/gui/sprites/magic/star/big_inactive.png` | `originals/Magic/assets/jymbelics/textures/gui/description/general/big_star_inactive.png` | `5c5b543f0c25cee69eb14ff6e337e437b6026b01f3ca5af91c5fdc4cb5d7cfe6` | none: written as RGBA |
| `textures/gui/sprites/magic/star/small_active.png` | `originals/Magic/assets/jymbelics/textures/gui/description/general/small_star_active.png` | `52ac8b4e9289901eefec6ed959734a5ebb5da72681ffdccf4c11000a4b6a43cb` | none: written as RGBA |
| `textures/gui/sprites/magic/star/small_hole.png` | `originals/Magic/assets/jymbelics/textures/gui/description/general/small_star_hole.png` | `a4b71b9ddb2ad2986bbf69eec53b9ca0bf1495bdfc1e9c14f65136d48cbf6b3d` | none: written as RGBA |
| `textures/gui/sprites/tooltip/magic_background.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/manabar_gui_border.png` | `f258a8e68494ccc30d475a3e304068e9ddf9bf4205e02749d69b634cfc82c13a` | the gold frame set 7 in on a 116x26 canvas (frame: its well cleared) |
| `textures/gui/sprites/tooltip/magic_frame.png` | `originals/Magic/assets/ars_jimbaux/textures/gui/manabar_gui_border.png` | `f258a8e68494ccc30d475a3e304068e9ddf9bf4205e02749d69b634cfc82c13a` | the gold frame set 7 in on a 116x26 canvas (frame: its well cleared) |
| `textures/gui/sprites/magic/glyph/arrow.png` | drawn for Jugcraft |  | drawn for Jugcraft (no library file) |
| `textures/gui/sprites/magic/glyph/ask.png` | drawn for Jugcraft |  | drawn for Jugcraft (no library file) |
| `textures/gui/sprites/magic/glyph/bed.png` | drawn for Jugcraft |  | drawn for Jugcraft (no library file) |
| `textures/gui/sprites/magic/glyph/calendar.png` | drawn for Jugcraft |  | drawn for Jugcraft (no library file) |
| `textures/gui/sprites/magic/glyph/clock.png` | drawn for Jugcraft |  | drawn for Jugcraft (no library file) |
| `textures/gui/sprites/magic/glyph/cross.png` | drawn for Jugcraft |  | drawn for Jugcraft (no library file) |
| `textures/gui/sprites/magic/glyph/hourglass.png` | drawn for Jugcraft |  | drawn for Jugcraft (no library file) |
| `textures/gui/sprites/magic/glyph/lock.png` | drawn for Jugcraft |  | drawn for Jugcraft (no library file) |
| `textures/gui/sprites/magic/glyph/party.png` | drawn for Jugcraft |  | drawn for Jugcraft (no library file) |
| `textures/gui/sprites/magic/glyph/pause.png` | drawn for Jugcraft |  | drawn for Jugcraft (no library file) |
| `textures/gui/sprites/magic/glyph/person.png` | drawn for Jugcraft |  | drawn for Jugcraft (no library file) |
| `textures/gui/sprites/magic/glyph/pin.png` | drawn for Jugcraft |  | drawn for Jugcraft (no library file) |
| `textures/gui/sprites/magic/glyph/ring.png` | drawn for Jugcraft |  | drawn for Jugcraft (no library file) |
| `textures/gui/sprites/magic/glyph/scroll.png` | drawn for Jugcraft |  | drawn for Jugcraft (no library file) |
| `textures/gui/sprites/magic/glyph/tick.png` | drawn for Jugcraft |  | drawn for Jugcraft (no library file) |
| `textures/gui/sprites/magic/mark/ask.png` | drawn for Jugcraft |  | drawn for Jugcraft (no library file) |
| `textures/gui/sprites/magic/mark/cross.png` | drawn for Jugcraft |  | drawn for Jugcraft (no library file) |
| `textures/gui/sprites/magic/mark/hourglass.png` | drawn for Jugcraft |  | drawn for Jugcraft (no library file) |
| `textures/gui/sprites/magic/mark/lock.png` | drawn for Jugcraft |  | drawn for Jugcraft (no library file) |
| `textures/gui/sprites/magic/mark/tick.png` | drawn for Jugcraft |  | drawn for Jugcraft (no library file) |

## Deviations from the spec in this commit

- **Marks are nine rows tall and as wide as their glyph and two**, not all 9x9: the "?" mark is 7 wide and the moon 10,
  so no glyph is cut (`MagicArt.Mark` holds each width; `check_magic_ui` holds them to the files).
- **Every arrow state is 13x32:** the 11x30 arrow sits one in on its outline's canvas, so the normal, lit and disabled
  sprites share one size, as a `WidgetSprites` needs. Added for the same reason: the primary plate's lit sprite, the
  End and Suspend icons' inactive and lit sprites, and the Refresh and Offer icons' lit sprites.
- **The tooltip sprites are the gold frame set seven in on a 116x26 canvas** (nine-slice border 10): vanilla draws a
  tip's sprites twelve out from its words, so the rim sits two pixels clear of them.
- **Two chooser reasons beyond the spec's six:** "Research more first" (the first note of a spirit whose research is
  not far enough) and "Cannot be read here" (a definition this server cannot read).
- **The roster's glances (`ROSTER_GLANCES`) are added now**, with `RosterGlance`; the roster draws them in unit 2.
- **The six standing lines are the glances of the server's own `term.<key>` sentences** (`screen.jugcraft.pact.glance.term.suspension` and so on);
  the breach line is one line, "Only a missed offering suspends it. It never ends by itself."
- **The end confirmation's two short lines are `screen.jugcraft.pact.short.end.leaves` and `.end.undone`**, short labels
  of the client's own sentence, not a glance key.
- **A focused element's underline is the heading ink on parchment** and gold only on dark: gold on the scroll's
  `#eed1ad` is about 1.2:1 and could not be seen.
- **`term.compact.items` has no glance:** a compact without resonance is the price row alone ("Price" and its stacks,
  its sentence the message), listed in `FOLDED`.
- **The resonance "have" is the fullest carried astrolabe's charge** (the seal pays from one astrolabe that holds
  enough, so that is the number that decides), not the first astrolabe's.
- **The cosmos keeps frames 0, 2, ... 14** of sixteen, so its loop stays seamless at about eight frames a second.
- **Variant A's art is imported too** (the spellbook as `magic/spell_book`, the inked paper strips) so `MagicTheme.BOOK`
  is whole for the Field Guide (unit 3); the encyclopedia's own `book.png` stays until then.
- **Not yet in `check_magic_ui`:** the beats' and stage directions' limits (they come with the shortened beats, in the
  screen commit), the codex's 60 words a page (unit 3), and `tools/select_client_tests.py` `SHOWN_BY` entries (no
  client test shows the layer before the pact screen uses it). The "screens use MagicButton and MagicText" rule is in
  place with no screen in `MAGIC_SCREENS` yet.
- **`RosterScreen.Portraits` stays** until the roster moves (unit 2); `MagicPortraits` is a second cache with its own
  id range.

## Known limits

- **Nothing here has been compiled or run in Minecraft.** No Minecraft jar is available where this was written: every
  call was checked against the decompiled 26.3 sources and the repository's own uses, and the files parse. CI's Build
  is the first compile. No screen uses the layer, so no client test exercises it yet.
- A blocker drawn from an advancement's description keeps that description's words: The Garden's Regard's first
  ("Examine a garden specimen: ...") is 19 words, over the blockers' 14. Shortening it is the research's own text
  (also its advancement), left for the checkpoint.
- Stains still repeat on scrolls drawn much wider than their 243 pixels; the pact panel is 320.

## Verification

Run locally on this commit, all passing (none of it in CI, and nothing in Minecraft):

- `python3 tools/owner_art.py --check` (every imported owner file matches its source) and `python3 tools/magic_ui.py`.
- `python3 -B tools/check_mod_data.py`, with `check_magic_ui`: PASS.
- `python3 -m unittest discover -s scripts/tests`: 317 tests, among them the 22 of `test_magic_ui.py`.
- `python3 scripts/check_repository.py`, `python3 tools/concordance_journey.py --check`, `python3 tools/concordance_economy.py`
  (gaining cycles: none), `python3 tools/check_icon_maps.py`, `node --test tools/world-designer/model.test.cjs`,
  `git diff --check`.
- `check_magic_ui`'s mutations, each on a scratch copy of the tree: 27 made, 27 caught (the lettering restored, a pixel
  changed outside an adapter's region, a palette PNG, a bare `%s`, a glance naming an argument the server never sends, a
  sample in the wrong order, a chip over three words, a row without a glance, a new work without its glances, a
  nine-slice with no centre, an ink under 4.5:1, a stray glance key, a never-imported file listed, a path in two
  tables, the mark rules changed on either side, a screen listed but still on BookArt, an ink off the palette, a glyph's
  width or entry wrong, a sprite named but not written, the seal counting for itself, `PactAfford` naming a client class,
  literal text in the layer, a provenance row dropped, and a sprite edited by hand inside its region).
- Every runtime sprite, and screens composed from the generated files alone (the sprites, their `.mcmeta` slices and
  the language file), were rendered at GUI scale 2 and looked at beside the design's pictures: Buer's terms at 427x240,
  320x180 and 480x270, and a kept page's header with its sockets and gauge over a ruled beat scroll. The ruled scroll's
  rules stay fourteen apart at 130 tall, and the scrolls show no repeating notch.

## World and event applicability

None: presentation only.

## Rollout and open questions

- Unit 1's next commit moves the pact screen onto the layer in every mode, with the shortened beats, the amended D-S15
  and their checks; unit 1 ends at the owner's checkpoint with the client shots of every pact state.
- The owner reviews variant B, the D-S15 wording and the shortened beats at that checkpoint.
