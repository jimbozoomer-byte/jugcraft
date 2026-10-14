# Regional vanadium-bearing iron deposits

Status: implementation in progress, 14 October 2026. Public branch `speedygroyper:codex/vanadium-iron-deposits`. The owner approved this scope and requested tests and an inspection world. Actual implementation tool: OpenAI Codex (GPT-6).

## Approved scope

- Primary surface biomes: Shield and Volcano; native ore size 48, one placement attempt per twelve eligible chunks on average.
- Secondary surface biomes: Highland, Basin, Windswept Hills, Windswept Gravelly Hills, Windswept Forest and Stony Peaks; size 40, one attempt per twenty-four eligible chunks.
- Overworld centers Y -48..32, triangular peak Y -8. Irregular native ore pockets, cave exposure allowed. Size is geometry, not a guaranteed block count.
- One third of successful pockets attempt the existing stone-supported outcrop: 3-8 exposed ore blocks, approximately 4-6 blocks across with buried foundations. Volcano has underground deposits only. Existing fluorite/spodumene placement remains unchanged.
- Surface biome selection also applies below cave biomes and in Designed worlds. No retrogeneration or replenishing reserves.
- Stone/deepslate ores, raw feed and reversible nine-item raw storage. Iron-tier mining, fixed one-feed-unit drops even with Fortune; Silk Touch and the Ore Drill recover the whole ore block. Prospecting uses the shared survey.
- One raw feed or intact ore block -> one existing `jugcraft:vanadium_iron_concentrate` in the Crusher: 200 ticks at its base 16 JE/t. No ore multipliers or free iron coproduct.
- Retain the existing concentrate -> oxide Ore Washer route: 500 mB water, 200 ticks at base 16 JE/t. Oxide and two bricks supply the reusable contact bed without sulfuric acid.
- Fresh servers use the mined supply. `vanadium.legacy_iron_recipes=true` explicitly restores the two ordinary-iron shortcuts; disabling vanadium acquisition also restores them. Existing saved concentrate, oxides and beds retain their identities. The bulk lye route gains a named-concentrate input, preserving its pentoxide output and other costs.
- Later iron recovery and battery-grade electrolyte refining remain separate work.

## Connections and compatibility

This is an iron-tool mining and workshop processing route into the steel-era acid catalyst. The existing Crusher, Ore Washer, electrical supply and ceramics are independently reachable. Trade and the Ore Drill are optional. No new processing machine, dependency, platform change or automatic world rewrite is needed.

`vanadium.enabled=false` stops new ore decoration and new mined-feed processing after restart while retaining registered ore, raw stock and saved content. Existing concentrate extraction remains usable. The legacy recipe option is a server startup setting, not a per-player unlock.

## Validation planned

Verify ordinary/Silk Touch/Fortune drops, raw compaction, both ore forms and raw feed through the powered Crusher, water/energy-paid washing and catalyst crafting, exact biome policy, survey and drill depletion, blocked-output behavior, config fallback, and saved content. Sample at least two normal seeds and a Designed world, including the richer districts, excluded biomes, cave biomes, volcano outcrop exclusion and existing fluorite/spodumene behavior. Run applicable server/client, data, repository and packaging checks; export a naturally generated world with a matching build. Report measured yields and unperformed checks honestly. Human visual acceptance remains pending.

## Art provenance

The owner-library catalog was searched for vanadium and magnetite; no named matching asset was found. Use the established mineral overlay and raw-item maps with a dark iron-grey mineral palette; preserve existing palettes and the original library. Runtime host stone/deepslate references use the established layered models.
