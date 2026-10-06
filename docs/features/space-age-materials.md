# Space-age materials: chromium, cobalt, graphite and the first superalloys (batch 57)

Status: implemented on `claude/kind-edison-yz5iqg`, awaiting review. The data checks pass; the Gradle build and game tests could not be run where it was written (see Verification). **Not yet played.**
Proposal issue: the owner, 6 October 2026, asked for the technology to be mapped into tiers reaching rockets, space stations and the tiers beyond, with the base resources that plan needs added and implemented. The plan is [branches/HIGH_TECH_AND_SPACE.md](../branches/HIGH_TECH_AND_SPACE.md); this batch is its first step.
Owner: jimbozoomer-byte
Target milestone and tier: tier 5 (atomic and aerospace), reached from the steel and chemistry tiers.
Primary specialty and supported player role: metallurgy and mining; the engineer building towards the reactor and the launch pad.

## Player experience
- **Two new deep ores**, found with an iron pickaxe like nickel's and smelted the same way (blast furnace or arc furnace; the crusher, pulverizer, ore washer and acid leaching treat them like any ore):
  - **Chromium ore** (chromite), Y −64 to 24, and its deepslate form: iron-black grains with a steel-blue glint.
  - **Cobalt ore** (cobaltite), Y −64 to 0, rarer: silver-white grains with a pink bloom. Grinding nickel ore in the pulverizer also turns up cobalt dust one time in ten, as cobalt rides with nickel in the real sulfide ores (it was iron dust before).
  - Both have raw items and blocks, ingots, nuggets, storage blocks, dusts and washed ores, with the usual `c:` tags.
- **Graphite ore**, Y −40 to 40, any pickaxe, drops 1 to 3 graphite (4 in the crusher); and the **arc furnace bakes a coal coke into one graphite** in 10 seconds, so carbon for electrodes is never out of reach. Graphite has a storage block.
- **Three alloys** in the alloy smelter, metal conserved exactly:
  - **Stainless steel**: 3 steel + 1 chromium → 4 (12 seconds).
  - **Nichrome**: 4 nickel + 1 chromium → 5 (10 seconds).
  - **Superalloy**: 2 nichrome + 1 cobalt → 3 (15 seconds). The nickel superalloy is the real turbine-blade and rocket-nozzle metal.
  - Each has an ingot, nugget and block. The metal press makes **stainless steel plates** and **superalloy plates**.
- **Uses today**, so nothing sits in a chest:
  - **Chrome plating**: a chromium ingot in the electroplating bath doubles a tool's, weapon's or armor piece's maximum durability and repairs it (nickel gives ×1.5), under the bath's usual rules (one plating per item; the same metal repairs again).
  - **Superalloy rocket nozzles**: a superalloy ingot and a stainless steel plate make four rocket nozzles in the rocket workshop, against two from a tungsten ingot and a steel plate.
  - **Graphite anodes**: two alumina and a graphite make two aluminum ingots in the electrolytic cell in 5 seconds instead of the coke anode's 8; and two graphite replace two of the four aluminum plates in a lithium cell (two cells, the same lithium).
- Handbook: three pages in the Materials chapter (Chromium and Cobalt; Stainless Steel and Superalloy; Graphite), the ores on the Ores page, and a tenth stage, Space-Age Materials, in the Progression chapter.
- Advancements **Stainless** (stainless steel), **Hot Section** (superalloy) and **Pencil Lead** (graphite), after Forged in Coke.

## Connections
- Existing input producer: the ore machines (crusher, pulverizer, ore washer, chemical reactor leaching), the blast furnace and arc furnace, the coke oven, nickel and steel.
- Existing output consumer: the electroplating bath (chromium), the rocket workshop (superalloy, stainless steel), the electrolytic cell and the lithium cell recipe (graphite). The planned tier-5 machines (transformers, the centrifuge cascade, the reactor vessel, the vacuum furnace, the fuel fabricator) are their main consumers; see the plan.
- Technology connection: every existing ore route and the alloy smelter. Magic connection: none yet; the plan reserves cobalt blue (pigment) and graphite (inks) for the cosy and magic branches.
- Reachable entry path: chromium and cobalt need only an iron pickaxe and a blast furnace; the alloys need the alloy smelter (workshop tier) and steel; graphite needs nothing. No recipe needs its own output.
- Required vs optional: optional. Tungsten nozzles, coke anodes and nickel plating keep working.
- Trade and solo: solo-reachable; the ores are Overworld worldgen.
- How this specialty stays useful: a metallurgist now has three alloys and two platings to sell, and the first materials every space-age machine will ask for.

## Balance and automation
- **Metal accounting**: stainless 36 nugget units in, 36 out; nichrome 45 in, 45 out; superalloy 27 in, 27 out. The nozzle recipe consumes 18 units for four non-metal nozzles. The anode recipes consume no metal (graphite is a mineral). `tools/check_mod_data.py` audits all of them.
- **Byproducts**: chromium ore → nickel dust 10%, cobalt ore → nickel dust 10%, nickel ore → cobalt dust 10% (was iron). Each stays under the 25% byproduct share and is skipped when its feature switch is off.
- **Chrome plating** costs an ingot of a deeper, arc-furnace metal and 100 mB of acid for ×2 durability, once per item; it does not stack with nickel (one plating per item).
- **Graphite anode**: 60 ticks saved per two aluminum ingots at the cell's 256 JE/t, 15,360 JE, for a graphite worth about 12,800 JE of arc-furnace time from coke, or nothing when mined. A convenience, not a profit.
- Nothing here makes power or turns back into its inputs: no loop.
- Worldgen: chromium 3 veins of up to 6 a chunk, cobalt 2 of 5, graphite 4 of 9, trapezoid heights as the other ores. Starting targets to tune after measurement.

## Multiplayer and persistence
- No new block entities, screens, packets or tick logic: blocks, items, tags, recipes and worldgen only, plus one branch in `Electroplating.plate`.
- Three new feature switches in `config/jugcraft.properties`: `chromium`, `cobalt` and `graphite`. `false` stops that material's worldgen and recipes (and the cobalt byproduct); registered blocks and items always remain, so saves survive.
- No retrogeneration: existing chunks get none of the new ores.
- IDs are permanent from the first release: `chromium`, `cobalt`, `graphite`, `stainless_steel`, `nichrome`, `superalloy` and their forms.

## Dependencies and assets
No new dependencies. All 33 textures are original pixel art drawn by `tools/generate_textures.py` from fixed seeds (the new metals draw from their own seed range, so no existing texture changed). MIT.

## Verification
Run on 6 October 2026 in the cloud container where the batch was written (no Minecraft, no JDK 25, no access to the Fabric and Mojang Maven servers through its proxy):
- `python3 tools/generate_textures.py` and `python3 tools/generate_material_data.py`: ran clean; `git status` shows only the new textures and the generated JSON for the new content, plus the three nickel pulverizing recipes (byproduct change) and the convention tags.
- `python3 tools/check_mod_data.py`: **PASS for 1470 IDs** (models, textures, loot, tags, worldgen, Python ↔ Java sync for metals, minerals, components, features and worldgen, the recipe metal audit, the fluid recipe audit, the electroplating numbers and tooltips, handbook and advancement references).
- `python3 scripts/check_repository.py`: PASS.
- **Not run**: `./gradlew build` and the game tests. CI runs them on the pull request. A game test `spaceAgeMaterials` was added to `JugcraftGameTests`: the arc furnace's chromium, cobalt and graphite recipes, the three alloys at their ratios, the superalloy plate and nozzle recipes, nickel ore's cobalt byproduct, and a chromium ingot in the electroplating bath doubling a worn iron sword's durability and repairing it.
- Not run: client launch, dedicated server with two clients, worldgen sampling (finding the ores at their heights in a real world), performance.

## World and event applicability
Overworld ore worldgen only (the trapezoid heights above), in every Overworld biome like the other Jugcraft ores. No mobs, dimensions, loot or seasonal behaviour.

## Rollout and open questions
- The next batches of the plan (energy tiers, fluorine and zirconium, enrichment, the reactor, aerospace fabrication, the launch pad) are the consumers that make these materials matter; see [branches/HIGH_TECH_AND_SPACE.md](../branches/HIGH_TECH_AND_SPACE.md#batches-in-order).
- The drone tower's graphene lattice still comes from coke on copper; a second recipe from graphite is planned with the vacuum furnace.
- Nickel ore's byproduct changed from iron to cobalt. Worlds in play are unaffected (recipes are data); the handbook and the ore-processing table in [TECH_TREE.md](../TECH_TREE.md) say cobalt.
