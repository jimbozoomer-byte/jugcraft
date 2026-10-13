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
