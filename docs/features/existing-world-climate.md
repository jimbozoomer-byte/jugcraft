# Existing-world climate bands

Owner instruction, November 15, 2026: apply spawn-relative climate bands to the
CURRENT hosted world for all future generation. Do not regenerate existing chunks.

Implementation: Claude Code supplied the shared climate core; OpenAI Codex
integrated/reviewed it, corrected deterministic biome lookup ties, implemented
existing-save migration and compatibility, and runs the local validation.

## Scope

This focused update starts from live release 20261103.1 / source 8075c6d58b. It
contains the climate runtime, biome pool/category tags, migration preparation tool,
read-only operator diagnostics and tests. It does not include the larger dirty
Thanksgiving/horse/ship feature batch or the separately prepared editor extension.
No global world override, chunk deletion, terrain reset or automatic migration.

The existing Overworld biome source is wrapped by `jugcraft:climate`; seed, noise
settings and other dimensions stay identical. The profile is saved in that
world's generator, so later `/setspawn` changes cannot move the boundaries.
Oceans, rivers, beaches and mountains retain their roles; cave biomes are preserved.
A 256-block transition blends adjacent bands, entirely inside the strict Arctic
limits. The central area keeps its original biome mix. Existing generated chunks
keep their stored biomes. Normal old/new terrain joins can show a vegetation or
climate boundary; this does not repaint old chunks to hide that boundary.

## Confirmed hosted-world anchor

Read-only snapshots: world spawn (1400,92,395), permanent town origin
(1296,114,400). Use town X1296/Z400 for the fixed climate origin.

| Boundary | World Z |
| --- | ---: |
| Arctic at/beyond 2500 blocks north | -2100 |
| Hot/desert/tropical/jungle starts 2000 south | 2400 |
| Temperate/forest/grassland/taiga starts 4000 south | 4400 |
| Arctic at/beyond 5500 south | 5900 |

## Preparation and deployment

Minecraft 26.3 stores these settings separately from level.dat:
`world/data/minecraft/world_gen_settings.dat`. The town record is
`world/dimensions/minecraft/overworld/data/jugcraft/town.dat`.

Run against downloaded metadata, with a NEW separate output directory:

```text
python tools/prepare_climate_migration.py --world-gen SNAPSHOT/world_gen_settings.dat --town SNAPSHOT/town.dat --out PREPARED
```

The tool writes prepared settings, byte-identical original settings and a hash/
boundary report. It never installs into the source world or opens region files.
Wrong versions, unexpected/custom/authored sources, missing town origin or a
conflicting existing climate profile fail closed. The same profile is byte-
idempotent. Unwrapping the output must reproduce the complete original NBT tree.

1. Validate the matching candidate mod and migration together; use the ordinary
   reviewed release process. The migration is not compatible with the old JAR.
2. Obtain owner restart confirmation. Warn players in-game and wait 30 seconds.
3. Make a fresh world/config/mod backup; stop the server cleanly. Download the
   final generation metadata and town record again. Re-run preparation and verify
   the fixed origin, source hash and all preserved fields before replacement.
4. Replace only the reviewed mod/release files and generation metadata. Never
   upload sampled level.dat, town/player data or empty test-world folders.
5. Start, verify normal startup and `jugcraft climate info` in the console. It
   must report X1296/Z400 and the above boundaries. Verify newly generated terrain.
6. Promote the matching client update only after server verification. Keep the
   original metadata/JAR and full world backup for rollback. If any new chunks
   were generated, rollback must consider those chunks too; changing the metadata
   back does not undo generated terrain.

## Compatibility and diagnostics

The inner multi-noise source retains Jugcraft region selection and the saved
Adam-ondi-Ahman state. Designed worlds remain excluded from procedural valley
ownership. Climate sources alone opt into stable spatial-tree tie handling, so
results do not depend on the previous lookup on a worker thread. Other worlds
retain the existing behavior. `jugcraft climate info` is operator-only/read-only;
the same profile summary is logged when the migrated Overworld loads.

## Validation

- Python migration regressions: 10 passed (narrow mutation, byte idempotence,
  origin/version/source refusal, custom dimension/type preservation).
- Biome pool/category audit: 13 passed against the deployed-source baseline.
- First real client migration test on the integration checkout passed with the
  actual prepared server metadata: 4608 old stored biome samples, player build,
  chest with 42 diamonds, town/spawn/valley state, new climate generation and two
  reopenings. 141 other saved files were byte-identical across the offline edit.
- Focused deployed-baseline candidate: JAR build passed; all 5 climate server
  tests passed; 7,928,159 pure Java assertions passed; the real client migration
  test passed with the actual prepared server metadata, two reopenings, 4,608
  preserved biome samples and 136 other saved files unchanged by the offline edit.
  The existing test town had a non-null saved origin and 19 built chunks.
- JAR comparison against verified 20261103.1 inventory: 51 climate-only additions,
  zero removed files; 79 existing asset byte differences are JSON-semantically
  identical. No test fixture classes/resources or unrelated feature changes.
  Candidate SHA-256: `5b796dddd76fc6c42b0d05461a85328d65a042529aea0479da82988f43dc4685`.
- Repository structure/local documentation audit passed. Migration tests are
  included in the standard scripts/tests discovery for CI.
- This is a disposable existing-save regression using the server's generator
  settings, not a full clone of the 16 GiB live world and not a human multiplayer
  playtest. Full GitHub checks, release publication and live activation remain.

No live world changes or restart have occurred. Logs/snapshots are local under
`build/nov15-climate-migration/` and are not committed.
