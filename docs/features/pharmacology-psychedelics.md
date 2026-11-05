# Batch 6 — Overworld perception and botanical pharmacy

[Improvement Batch C](pharmacology-calm.md) adds THC's warm botanical presentation. [Improvement Batch D](pharmacology-dream-visuals.md) adds Psilocybin/Mescaline identities, persistent recorded-site guidance and readable study cards. [Improvement Batch E](pharmacology-reverie-visuals.md) adds LSD/DMT geometry, directional afterimages, Clockwork previews and explicit advanced inputs.

Current shared strain, replacement and combination rules are recorded in [decaying strain — Batch A](pharmacology-strain.md); the original batch behavior below is historical.

Implementation tool: **OpenAI Codex (GPT-6 family)**. Owner-directed continuation of Batches 1–5. Local work only; no live-server restart or deployment is authorized by this batch.

## Selected outcome

Add THC, Psilocybin, Mescaline, LSD and DMT as fixed self-use formulations on the existing `pharmacology_v1` attachment. Reuse shared crop growth, compact/reactor chemistry, formed processors, formulas, native status sync, magic presentation and native container screens. No real synthesis recipes, combat/injury ailments, inventory escrow or new dimension. Clockwork travel, anchors and trading remain Batch 7.

| Drug | Benefit | Duration / onset | Strain | Drug drawback |
| --- | --- | --- | --- | --- |
| THC | Luck I, 25% less stationary fishing pre-bite wait | 10 min / 10 s | 20 | Mining −10%; 60 s Drug Appetite on natural expiry |
| Psilocybin | Dream Sight; recorded, visible study sites within 16 blocks; Memory lesson | 8 min / 10 s | 20 | Shared psychedelic tolerance/recovery |
| Mescaline | Compass pulses toward nearest recorded, loaded site within 96 blocks | 15 min / 15 s | 25 | Shared psychedelic tolerance/recovery |
| LSD | Pattern Sight within 24 blocks; Memory and Mirror lessons | 25 min / 20 s | 35 | Supported aimed-gun hand sway +20% |
| DMT | Deep Reverie within 24 blocks; all three lessons | 5 min / 20 s | 50 | Shared psychedelic tolerance/recovery |

THC uses `cannabinoid`; the other four use `psychedelic`. All prohibit active redosing, retain the existing 100 strain cap and use 120 s recovery. Online counters, saved doses, milk cancellation, death handling and family history remain shared. Early capsules need no controlled-release die. DMT uses the Batch 5 advanced capsule shells.

## Manufacturing and progression

Three cultivated crops reuse `JugcraftCropBlock` and `CropGrowth`: Hemp → Botanical Resin; Dreamcap → Dreamcap; Prism Cactus → Prism Cactus Fruit. Fictional starter seeds/spores craft from vanilla inputs. Lit farmland and bone meal provide deterministic solo entry. All three join native `crops` and `maintains_farmland` tags, so dry farmland stays planted. Mature harvest returns two produce and two seeds, immature one seed. They are cultivated variants; this batch adds no world generation.

The Separator prepares THC stock from resin. The compact Chemical Reactor prepares dream/prismatic concentrate, then Psilocybin/Mescaline stock. Separator verification → Analyzer sample and blank formula → Press active plus shells produces four capsules. These three early chains need no precision module. LSD/DMT require the existing Precision Process Module for reactor preparation, verification and analysis. Machine inventories, ports, fluids, locks, escrow, power and save formats remain unchanged.

The full exact ingredients, times, yields, energy costs, tool requirements and handbook text live in `tools/psychedelic_pharmacy.py`, included by the canonical `pharmacology.py`, `petro.py` and `industrial_forms.py` writers. Recipes remain visible through the existing handbook, encyclopedia and recipe viewer.

Paid study establishes the later chain without circular research: Psilocybin → Memory → Lucid Imprint → Perception Substrate → LSD stock. LSD → Mirror → Pattern Imprint → Oneiric Stock → DMT stock. DMT → Reverie → Oneiric Imprint reserved for future content. Oneiric Stock also consumes dream concentrate, chorus fruit and an echo shard. These are authored Minecraft abstractions.

## Authored Overworld study and privacy

Craft a Dream Study Plinth and carry a Dream Folio. Physical interaction within six blocks and line of sight records the site in the folio's item data, capped at 16 most recent distinct Overworld positions. No new player research or drug ledger. Mescaline reads only that bounded list, within 96 blocks and already-loaded chunks. Sight reads only recorded plinths within 16/24 blocks and line of sight, at most four private presentation events per second. A bounded loaded-chunk preflight covers the whole sightline before native clipping. It never scans ores, mobs, structures, inventories, claims or bases, and never loads a chunk.

Native menu lessons: repeat a three-symbol sequence (Memory); reverse it (Mirror); advance each symbol through Sun/Wave/Leaf/Star (Reverie). Only the appropriate active post-onset dose reveals that layer. A successful lesson consumes one Study Canvas and gives one matching imprint. Wrong answers reset the attempt without a penalty. One completion per menu, owner/container identity, physical reach, line of sight, current dose, payment and output room are all checked on the server. No inventory slots or client-provided pattern/reward/coordinates exist in this menu.

The body, inventory and game mode remain ordinary. Existing Censer expedition escrow and remote-view modes exclude plinth use. The native magic `SignPayload` is sent privately, never through broadcast `Signs.show`. The owner-authored alchemy frame and native narrated buttons show readable clues independently of cosmetic visual intensity. No camera distortion, nausea or flashing overlay is imposed.

## Asset reuse

Owner approval and provenance: `art/owner-library/README.md`, `MAGIC_ASSETS.md`, `PROVENANCE.md` and `docs/ART_DIRECTION.md`. Original library files remain unchanged.

- Prism cultivated stages copy `art/owner-library/originals/Magic/assets/ars_jimbaux/textures/block/sourceberry/sourceberry0.png` through `sourceberry3.png` byte-for-byte into `textures/block/perception/`. These are fantasy prism-bearing cultivated plants; no new third-party download.
- Hemp stages reuse existing runtime `jimtafacts/crops/hops_0` through `_6`; Dreamcap stages reuse existing `brown_mushroom_colony_stage0` through `3`.
- Ingredients/effect markers reuse existing dreamglass, resin, capsule, plate and seed sprites. Existing owner alchemy UI art is drawn by `OwnerAlchemyArt`; no new rendering or UI framework.
- The plinth's stone writing surface, copper rim and amethyst focus use native Minecraft textures. Its tablet copies the existing dreamglass sprite byte-for-byte into `textures/block/perception/dreamglass_tablet.png`: Minecraft 26.3 requires block-model sprites in the block atlas.

## Validation

Common and client Java compilation passed on the pinned platform. The final server run passed **65 required pharmacology checks**, including all 98 shared factory/beverage item-output recipes, the four new compact reactor jobs, both new precision-stock jobs, native crop harvests and dry-farmland survival, distinct brown-mushroom starter/red-mushroom catalyst crafts, fishing-timer behavior, onset, expiry, family limits, persistence, line of sight, unloaded-chunk rejection, payment, inventory capacity and stale-menu/replay rejection. All 22 new processing jobs ran on their production machines.

The native client test passed block interaction, hidden sober clues, all three paid lesson sequences, synchronized rewards, normal screen closure, Mescaline's compass pulse, a world-ticked Separator job and actual THC consumption/Luck/side-effect sync. **12 screenshots were reviewed**, including the corrected block-atlas plinth, readable lesson rules, all three crops remaining planted through study, navigation and manufacturing. Existing native narrated buttons and owner alchemy art remain the UI foundation.

The complete production data audit passed **15,454 material IDs with zero errors**. Fresh final recipe, machine, guide, shared-tag and agriculture checks also passed. The icon lint passed 285 maps and 2,021 item icon sizes with the same 28 existing warnings. Repository structure/documentation links and scoped whitespace checks passed. The bounded canonical generation copied and byte-verified **466 runtime resources**, retaining all previous generator ownership and recording the additional generated JSON paths.

Local evidence: `../batch6-pharmacy-2026-11-03/evidence-final/` from the checkout, with screenshots, logs and a SHA-256 manifest. These are local test-server and singleplayer-client results; no dedicated multiplayer playtest, live-server deployment, restart or owner live-world acceptance is claimed. Clockwork travel, return anchors and trading remain Batch 7.
