# Snow-country fluorite deposits

Status: implementation in progress on `codex/fluorite-deposits`. Owner-approved scope, 13 October 2026. This record initially announces the work; no test or completed-gameplay claim is made until results are recorded below.

## Approved player result

Find large, finite fluorite ore pockets below permanently snowy inland biomes. Small surface outcrops occasionally point to a successfully generated pocket below. Mine with an iron pickaxe, locate supplies with the existing prospector and automate extraction with the existing ore drill. Preserve the shared mineral, tag, configuration and world-generation systems.

- Vanilla: snowy plains, snowy taiga, ice spikes, grove, snowy slopes, frozen peaks and jagged peaks.
- Jugcraft: snowy coniferous forest, snowy forest, muskeg, frostlight garden and snowpetal grove.
- Eligibility comes from the surface biome above the deposit, including authored World Designer paint; cave biomes below do not remove eligibility. Seasonal snow, Alpine Spawn, Tundra, Cold Desert, beaches, rivers and oceans do not qualify.
- Underground centers: Y -32 through 48, triangular distribution peaking at Y 8; native ore size 48; one attempt in eight eligible chunks on average; exposed cave faces allowed.
- Outcrops: attempt above one quarter of successful deposits, three to eight exposed ore blocks within eight horizontal blocks of the deposit center. Skip water, structures and obstructed surfaces. Never create an outcrop for a failed underground placement.
- Stone/deepslate forms and one canonical fluorite item. New chunks only, no regeneration or rewriting existing saves. Disabling acquisition must retain registrations and saved items.

## Progression and integration

Primary specialty: regional mining and future industrial reagents. Reachable entry: iron pickaxe and snowy-country exploration; prospector and powered ore drill are optional existing workshop/steel automation. Solo mining and trade are both supported. The approved future HF/PTFE, electronics and electrolyte consumers remain separate chemistry work; this slice does not claim those recipes exist. No new dependency, currency, dimension or machine is required.

The current size/frequency are starting balance values. Ore-feature size is a shape parameter, not a guaranteed block count. Real generated-world measurements, cave intersections and final consumer costs determine tuning.

## Implementation and validation checklist

- [ ] Shared mineral registrations, data, tags, feature switch, loot and owner-library textures.
- [ ] Bounded, deterministic surface-biome-aware deposits and linked outcrops, including Designed worlds.
- [ ] Generation, exclusion, harvesting, prospecting/drilling and persistence checks.
- [ ] Real-world sampling on multiple seeds and a rendered client check.
- [ ] Local build, repository/data checks and applicable server/client tests.
- [ ] Saved inspection world and matching mod/pack for the owner, delivered outside Git.

## Compatibility and evidence

No terrain, generator preset, platform pin or existing mineral identity will be changed. The inspection save is disposable development output and will not be committed. Existing generated chunks remain unchanged. Human visual acceptance and two-independent-client testing must be distinguished from automated evidence.

AI-assisted implementation: OpenAI Codex (GPT-6), under the owner's direct instruction in this chat. No claim of Claude use.

## Implementation notes

`tools/materials.py` owns the mineral, feature switch and balance values. The shared generator writes both ore forms, their loot, iron-tier and `c:ores/fluorite` tags, reversible nine-item storage, and the existing crusher's standard two-item ore output. `OreSurvey` includes the new tag; the ore drill requires no special-case mining code. These are finite ordinary blocks with normal Silk Touch and Fortune behavior.

`SurfaceLinkedOreFeature` delegates underground geometry to Minecraft's `OreFeature`. The placed feature supplies rarity, horizontal spread and triangular depth. It deliberately has no underground `minecraft:biome` filter: the wrapper samples the actual surface biome from the world generation region. The Overworld dimension check prevents reused biomes in other dimensions acquiring deposits. The outcrop is a bounded connected footprint, with all candidate cells checked before any surface write. Water, non-natural supports, obstructions and native structure pieces reject it; structure lookups use the generation region's cached starts.

Surface heights use the live `OCEAN_FLOOR` map, which is maintained during decoration, rather than the earlier terrain-only `_WG` map. Grass/podzol support uses 26.3's separate `grass_blocks` tag. The pocket's center determines regional eligibility; the native ore shape can extend across a neighboring chunk or biome edge. Each outcrop cell independently checks the eligible surface biome.

The switch `fluorite.enabled=false` takes effect after restart, stopping new decoration and acquisition recipes while retaining registry identities, loot and saved materials. There is no load-time rescan or retrogeneration. An outcrop is a clue at generation time; later mining or player edits can remove the pocket or marker.

## Art provenance

- `art/owner-library/originals/Blocks/block_fluorite.png` is copied unchanged to `textures/block/fluorite_block.png` through `tools/owner_art.py`. Source SHA-256: `65e7a0b79bf42371b3b319a6028ac2fcc0fcff1d9d57d7f59ab5967fc9ffb8d8`.
- The ores use the existing shared hand-drawn ore overlay maps over referenced vanilla stone/deepslate. The fluorite crystal uses the existing `raw` pixel map. Their new icy-cyan palette matches the supplied storage block. No Mojang texture is copied, and adding the palette at the end preserves all existing ore layouts.
- The owner's `deepslate_fluorite_ore.png` was inspected; its opaque host does not fit the shared host/overlay model. The source remains untouched. Runtime art is validated by the existing material-map and owner-import checks.

## Validation commands

`./gradlew build` runs the server suite, including `FluoriteGameTests` for iron-tier harvesting, Silk Touch, Fortune, storage, biome policy, prospecting and the actual powered ore drill. `./gradlew runClientGameTest -PclientTests=FluoriteClientGameTests` generates two ordinary seeds and a Designed world, checks cave-biome behavior and excluded land, exercises failed underground placement/snow/water/roof/obstruction fixtures, saves/reopens natural ores, renders screenshots and exports a disposable inspection save under the test run directory. With fluorite disabled in the test process's startup configuration, the same client suite instead verifies absent decoration/recipes and persistent registered ores/items.

Completed results and the final inspection artifact are recorded after execution; compilation alone is not worldgen or visual evidence.
