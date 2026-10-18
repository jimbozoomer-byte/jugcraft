# Jug Nouveau integration

Owner request: 18 October 2026. Implementation: OpenAI Codex. Local branch:
`local/jug-nouveau-oct18`, based on `78861d6fd`. No publication is authorized by
this implementation record. Existing local technology and town work is preserved.

## Source and scope

The supplied name in the archive is `ars_jimbaux`. Its complete source is already
preserved in the owner library: **2,210 asset files and 1,397 data files**. The
[full manifest](jug-nouveau-source-manifest.json) records all 3,607 paths and their
catalogued SHA-256 hashes, with the runtime destinations used by this port.
Earlier imports have their own feature records and owner-art manifest entries.

The collection has textures, geometry, animations, audio, models, recipes, loot,
tags, world generation and other data. It does **not** contain Java/Kotlin gameplay
source. Two compiled owner archives were subsequently found in Downloads and
inspected without running them. The [archive cross-check](../../art/owner-library/ports/jug-nouveau/README.md)
accounts for all **4,847 resource entries** from core and elemental together:
4,819 already matched the library byte-for-byte, and 28 conflicting elemental
variants were preserved separately without overwriting the original upload.
Original metadata/notices are also preserved. The archives contain compiled
NeoForge classes, including embedded dependencies, not a Fabric 26.3 source
project. Old names, recipe serializers, shaders and NeoForge
data are not a working Fabric implementation. Preserve them in the library;
port their behavior and references into Jugcraft when their feature is wired.

Use the supplied art directly. Do not redraw, recolor or rename it just to make
it different. Preserve originals and notices. Technical runtime adaptations
(Jugcraft IDs, supported data schemas, renderer paths) are expected. Do not run
programs from the source library. See [owner authorization](../../art/owner-library/MAGIC_ASSETS.md).

## First player outcome: Book Wyrm storage

This first integration adds a craftable Storage Lectern, Book Wyrm Charm and
Dominion Wand. Book Wyrms use the supplied geometry, flight animation and six
textures. The lectern uses the supplied geometry, texture and floating-ledger
animation for its placed and item renderers. They belong to the existing
Concordance feature switch and authority/party system; there is no second mod,
currency or mandatory research gate.

1. Craft and place the Storage Lectern (book, two amethyst shards, vanilla
   lectern and gold ingot).
2. Craft a Book Wyrm Charm (book, two amethyst shards and two gold ingots) and use
   it on the lectern. Each wyrm allows eight links, up to four wyrms / 32 links.
3. Craft a Dominion Wand (amethyst shard, stick and gold nugget). Use it on the
   lectern, then on ordinary chests or barrels within 16 blocks.
4. Use the lectern empty-handed. Previous/Next selects a chest; ordinary clicks
   and shift-click transfer its real items. Put down cursor-held items before
   changing pages. Unlink removes the connection, not the contents.
5. Dye a wyrm black, blue, green, purple, red or white. Its color persists.

This is a first storage integration, not complete parity with the older mod's
storage/crafting system or its Book Wyrm familiar. Aggregated search, crafting
requests, physical item-delivery jobs and familiar effects remain port work.
The menu shows one real linked container at a time. Double-chest halves are
separate links. Wyrms fly between their home and loaded links; they are not a
second item-transport inventory. Existing delivery workers remain separate.
The Binding section of the Concordance encyclopedia includes the complete
crafting and use guide without a research condition.

## Authority, persistence and bounds

- The owner and their current party may use the lectern, subject to the existing
  change-permission checks on the lectern and each chest.
- Locked containers must be unlocked directly before linking/access; Ender
  chests, furnaces and arbitrary mod inventories are excluded.
- No chest stacks are copied into a persistent network inventory. Every access
  resolves the real container, so a removed, unloaded or newly protected chest
  cannot leave a withdrawable cache behind.
- Opening, linking and page actions are rate limited. Menu access ends when the
  player walks away, dies, becomes a spectator or loses authorization.
- No chunk tickets or world searches. Home/link checks occur once per second
  per wyrm; each network has at most four wyrms and 32 links within 16 blocks.
- Save schema 1 keeps owner, linked positions, bound entity UUIDs and colors.
  Load deduplicates and bounds lists. Losing a wyrm disables excess links without
  deleting storage. Breaking the lectern returns bound charms and removes loaded
  wyrms; an unloaded orphan discards itself when it sees its home is gone.
- Disabling Concordance stops access/flight; registrations and saved inventories
  remain. Do not remove the mod from a world containing its entities and blocks.

## Remaining port families

These are requested work, **not implemented by the existence of their files**.
Use the complete source manifest rather than treating this family summary as an
exhaustive item list.

| Family | Supplied examples | Integration work still required |
|---|---|---|
| Other helpers | Starbuncle, Drygmy, Whirlisprig, Wixie, Amethyst Golem | Ownership, jobs, bounded automation, animations, acquisition and interfaces; reuse existing worker authority and logistics |
| Enchanting and crafting | Enchanting/alteration apparatus, imbuement chamber, glyph press, scribes table | Supported recipe serializers, work inventories, previews, animation states and existing Concordance connections |
| Potions | Potion melder/stirrer/diffuser and flask cannons | Potion component preservation, recipes, costs, claims and usable controls |
| Source infrastructure | Jars, sourcelinks, relays, splitters, collectors/deposits/warps | Translate into existing magical power systems, transfer limits and diagnostics; avoid a duplicate currency |
| Spell devices | Turrets, timers, mirrors, prisms, runes and lenses | Connect to the existing spell compiler, payment, claims, PvP rules and bounded targets |
| Flora and building | Archwood sets, magebloom/manabloom, archfruit, sourcestone and decorations | Survival routes, crop/tree lifecycle, full block variants, loot and compatible world generation |
| Gear and familiars | Armor, tools, bows/crossbows, foci, familiar items and threads | Map existing gear first, then missing equipment behavior, animation, attachments and clear controls |
| Creatures and encounters | Alakarkinos, Weald Walkers/Waddlers, Wilden mobs and Chimera | AI, encounter spawning, attacks/telegraphs, reward eligibility, loot and persistence |
| Glyphs, rituals and realms | Glyph recipes, ritual data, structures and dimension metadata | Map to existing Concordance operations; implement missing effects and safe travel rather than load foreign serializers |
| Companion collections | `ars_jymbaumental`, `ars_nouveau` and cross-mod data | Audit dependencies against the core port; keep unrelated library collections distinct |

## Validation

Verified locally on 18 October 2026 with Minecraft 26.3, Fabric Loader 0.19.5,
Java 25.0.4.1 and the unchanged 18-library lock (including GeckoLib 5.5.7):

- `gradlew.bat build`: **all 1,937 required server game tests passed**, including
  eight new Book Wyrm tests; common, client and game-test compilation passed.
- `gradlew.bat runClientGameTest -PclientTests=JugNouveauClientGameTests`:
  passed in an independent disposable world. Inspected screenshots of the
  lectern/item renderer, animated wyrms in six colors, live linked contents,
  next-chest navigation and GUI scales 1/2. Keyboard focus is exercised.
- `python -X utf8 tools/generate_material_data.py`: completed. The generated
  sound-map key order was retained for CI consistency; sound definitions are
  semantically unchanged.
- `python -X utf8 tools/check_mod_data.py`: passed, including 3,024 material IDs,
  recipe/data checks, 8,522 block/item models and the owner-art checks.
- `python -X utf8 tools/owner_art.py --check`, `scripts/check_repository.py`,
  `scripts/package_modrinth.py check`, and `git diff --check`: passed.
- All **4,870** preserved archive entries (4,847 resources and 23 metadata,
  notice/logo/configuration files) match their recorded size and SHA-256.

During implementation, API and recipe-condition mismatches were corrected.
The locked-chest fixture was changed to preserve the actual inventory while
setting its lock. The gallery fixture was fixed to keep its camera on solid
ground. The final successful runs include those corrections. Test logs/worlds
are local build outputs, not committed player data.

`JugNouveauGameTests` covers ownership/capacity, real-container withdrawals,
unlinking and capacity loss, invalid targets, saved identities/colors and menu
invalidation, ordinary wand/charm clicks, survival-recipe loading and immediate
lock revocation. `JugNouveauClientGameTests` uses a separate disposable automated
test world for models, colors, live inventory sync, page switching and GUI scales.
It never operates an owner's Modrinth playtest.

Still required before release: owner visual/survival feedback, two independent
players exercising simultaneous access, restart/chunk-boundary field testing
and measured performance at a stated network count. Automated single-client
checks must not be described as independent multiplayer testing.
