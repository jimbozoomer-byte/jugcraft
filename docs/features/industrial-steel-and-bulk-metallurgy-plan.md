# Industrial steel and bulk metallurgy

Status: owner-selected planning direction, recorded 6 October 2026. The core route and charcoal alternative are selected; product and machine expansions below are proposals for discussion. The existing coke route is present in source. Charcoal steel and new expansion equipment are not implemented by this document.
Proposal issue: direct owner choices during industrial planning; no separate issue.
Owner: jimbozoomer-byte.
Target capability: accessible steel production alongside first electricity, followed by optional bulk metallurgy and useful steel products.
Primary specialty and supported player role: metallurgy, workshop engineering and industrial construction.
Related documentation: [starter workshop plan](industrial-starter-workshop-plan.md), [existing steel feature](steel-tier.md), [current steel technology](../TECH_TREE.md#steel-tier), [machine roadmap](../MACHINE_ROADMAP.md) and [testing](../TESTING.md).

## Explicit owner choices

- **Use Coke Oven -> Steel Foundry as the simple core steel route.** Additional refining operations belong to later expansions. Do not insert mandatory pig iron and a second refining machine before the first steel ingot.
- **Allow charcoal-based starter steel production.** Coke should suit efficient bulk production. The alternative uses the same Steel Foundry and produces the same steel material, rather than a separate inferior starter-steel currency.
- Retain the earlier selected industrial structure: a manageable essential route with many useful sideways products; steel develops in parallel with electricity. A first electrical generator or basic circuit does not acquire a new steel requirement.
- Earlier workshops remain useful and can accept motor drive. Larger replacements are optional for ordinary processes; new equipment is required only when a process actually needs a new capability. These principles come from the [starter workshop plan](industrial-starter-workshop-plan.md).

The owner selected the first two directions, not exact charcoal quantities, work rates or every expansion machine listed below.

## Player experience and reachable entry

Start with ordinary iron, bricks and furnace construction. Make the foundry's iron plates through the selected early hammer/bench route or reachable mechanical press. Load iron and charcoal to obtain a small amount of steel without a power grid or a coal-coking plant. Build a Coke Oven when coal supply and production volume justify it; send its coke to the same foundry.

The intended paths are:

1. **Starter alternative:** logs -> ordinary charcoal production -> charcoal + iron -> Steel Foundry -> steel.
2. **Core bulk route:** coal -> Coke Oven -> coke + iron -> Steel Foundry -> steel.
3. **Useful outputs:** steel -> tools/armor, workshop parts, industrial building materials and fluid storage/transport; expand into larger machinery as needed.

The charcoal entry means the Coke Oven is optional for the first steel batch. Coal/coke remains an important production branch rather than an entry toll. Both routes retain a foundry construction purchase; neither needs steel to construct its own first foundry. Basic circuits, oxygen supply, mineral-sand refining and completion of the farming/chemistry branches are not starter-steel requirements.

These are planned reachability rules. The hand-produced iron-plate route is selected in the starter plan but is not claimed to exist in current recipes. Audit the whole construction chain during implementation, including the ordinary blast furnace and hopper, rather than checking only the foundry's operating power.

Solo players can stage this production using common materials; trading for iron, charcoal, coke, plates or finished steel is an equally valid shortcut. Steel production should remain worthwhile for builders, toolmakers and workshop suppliers without requiring mastery of every specialty.

## Observed source baseline

At main snapshot `e76f9cec`, [tools/machines.py](../../tools/machines.py) defines the following. These observations are not new owner-approved balance numbers or gameplay test results.

| Equipment or process | Current source | Planning consequence |
| --- | --- | --- |
| Coke Oven construction | 4 brick blocks + 4 iron ingots + 1 furnace | Earlier materials; no steel or electrical part |
| Steel Foundry construction | 5 brick blocks + 1 hopper + 2 iron plates + 1 blast furnace | Earlier iron plates must be reachable before its first steel |
| Coke production | 1 coal -> 1 coke; 600 ticks; no electrical or separate heating-fuel input | Reuse the existing machine and material identities |
| Steel production | 1 iron ingot + 1 coke -> 1 steel ingot; 400 ticks; no electricity | Preserve the working coke route while adding the selected charcoal alternative |
| Optional oxygen boost | Foundry data accepts oxygen at 2 mB/t; the technology record describes doubled speed | An existing optional improvement, not a prerequisite or a newly designed refining step |

The Coke Oven currently occupies 2 x 2 blocks, 2 tall plus its chimney; the Steel Foundry occupies 2 x 2 blocks, 5 tall. Each is placed from one crafted machine item. The listed brick quantities are construction-recipe ingredients, not an instruction to build a second multiblock shell. Size/cost changes require their own reviewed design.

Current data contains only the coke-based foundry recipe. Do not imply that charcoal already works, that the approved manual iron-plate route is implemented, or that a separate heating-fuel slot already exists. Current foundry operation uses its charge rather than electricity; this plan does not silently add a third consumable.

## Carbon supply, material accounting and pacing

Both inputs produce ordinary `jugcraft:steel_ingot`. Preserve one iron ingot's metal in one steel ingot: 9 nugget units in and 9 out. Charcoal/coke pays for the process and does not authorize additional metal output. Plates and gears retain the shared material units and identities. Equivalent manual and machine forming recipes follow the selected equal-metal-yield policy.

**Proposed balance direction:** charcoal uses more carbon per steel and/or more foundry time than coke, while remaining practical for the first tools and workshop parts. Exact quantities and timings remain open. Coke earns its bulk role through operating economy and sustained output, without reducing the charcoal route's steel quality or adding a mandatory second steel-refining operation.

Compare complete production chains: charcoal includes wood acquisition and charcoal firing; coke includes coal acquisition, oven construction and coking time. An unboosted source-baseline foundry can consume coke every 400 ticks while one oven supplies it every 600 ticks, so a single oven limits sustained output. A faster foundry alone does not fix that supply bottleneck. Those rates are data observations, not a benchmark or a final required oven/foundry ratio.

Count operating fuel, carbon, power where applicable and upstream machine costs explicitly. The starter workshop's roughly 3–4 times manual-to-small-machine throughput target does not automatically assign the same multiplier to every steel expansion or oxygen boost. Larger equipment must conserve metal; speed or fuel improvements need distinct, audited costs.

Use the existing hopper, inventory, side-configuration and transport systems. Manual loading stays valid. Belts, pipes, buffers and filters are optional purchases rather than a compulsory factory before steel. The [conveyor performance follow-up](https://github.com/jimbozoomer-byte/jugcraft/pull/230) records the owner's conditional use of item pipes without visible moving items for costly late-game belt workloads.

## Useful steel products and sideways progression

This is a proposed expansion map, not approval of every product or a requirement to finish each branch. Reuse existing outputs and audit their actual recipes before adding new registrations. A product's visual name does not prove it currently consumes steel: for example, some Dieselworks plate/grating recipes use iron.

| Branch and end result | Forming/assembly route to develop | Existing connection and proposed depth |
| --- | --- | --- |
| Industrial buildings: beams, sheet panels, grating, catwalks, brackets and railings | Plates and shaped stock through workshop forming; larger forming equipment for batches | [Dieselworks](dieselworks.md) already records beams, panels and grating. Extend useful building sets where gaps remain, with consistent material accounting and the owner's clean steel art direction |
| Fluid works: tanks, pipes, valves, strainers and fittings | Steel plates plus reachable earlier pipe/tank components; ordinary assembly | [Fluid logistics](fluid-logistics.md) and the existing Steel Tank provide consumers. Larger storage and routing are optional improvements for farms, steam workshops and chemistry |
| Workshop equipment: gears, frames, press heads and upgrade components | Existing plate/gear routes and assembly; discuss larger press variants below | Steel plates/gears already feed [machine upgrades](machine-control.md). Proposed structural parts can support sturdier or larger equipment without adding a separate frame currency to every old recipe |
| Player equipment: tools, armor and selected workshop implements | Existing ingot-based crafting where available; forming for proposed specialist parts | [Tools and armor](tools-and-armor.md) already records steel sets. Review their reachability and usefulness; do not rebuild this branch as new electric-only gear or introduce routine machine-part replacement |
| Production industries: larger mill/press heads, cutting blades, machinery housings and supports | Shared forming plus the relevant assembly station | Candidate uses in crop milling/oil pressing, sawmilling, pumps and material handling. Choose consumers that gain a concrete capability or scale improvement rather than requiring every farmer to industrialize |
| Later alloy/refining works: durable equipment structures and suitable furnace/handling parts | Steel forming plus appropriately staged ceramics and other materials | Connect selected [mineral-sands/refining](mineral-sands-and-refining-plan.md) equipment. Steel is one construction input where sensible; producing all titanium, aluminum or rare-earth products is not needed to make ordinary steel |

Steel products should be useful as end results, trade goods and equipment materials. A building palette or tool set can be a satisfying destination independently of progressing to another tier. Existing bronze/brass products retain their roles. Magic connections may use shared ordinary materials where already appropriate; no new magic-school completion requirement is selected here.

## Proposed expansion machines and process roles

These are candidate roles for the next planning pass. Whether the first expansion wave emphasizes new larger stations or upgrades to current machines remains open. Reuse shared machines, recipe families and interfaces rather than creating unrelated specialist variants for every metal.

| Candidate expansion | Inputs and useful output | Why build it; entry constraint |
| --- | --- | --- |
| Larger Coke Oven or oven bank | Existing coal -> existing coke | Improve coal throughput/handling for several consumers. Standard ovens remain a valid route; construction uses previously obtainable steel and ceramics |
| Larger Steel Foundry | Earlier iron and carbon -> ordinary steel in larger batches | Optional sustained production and handling/operating economy. A starter foundry supplies its steel parts; no larger-foundry-only part in its own construction |
| Larger Metal Press / rolling attachment | Suitable metal stock -> plates and selected shaped construction stock | Batch forming for tanks/buildings/frames. Reuse the existing forming family; equal metal yields. Choose a separate larger station versus an installed attachment in the next pass |
| Heavier alloy furnace capability | Earlier metals and appropriate heat/lining -> selected alloys | Add genuinely new supported processes where needed. Identify each alloy's producer and consumer; do not require every alloy or add pig-iron refining to ordinary starter steel |
| Electric Arc Furnace steel option | Reachable iron/carbon and electricity -> ordinary steel | Consider a later bulk recipe on the already registered Arc Furnace, not a duplicate machine. Its existing registration does not mean an electric steel recipe is already present or required |

Early bronze/brass ceramic gear casting does not automatically permit steel casting in the same crucible. Steel casting, heavier stock forming and any higher-temperature lining are separate capability decisions still to develop. Furnace linings, molds and machine parts remain reusable construction equipment; this plan adds no routine lubricant, filter, mold or lining replacement requirement.

Coke-oven chemical byproducts can feed the [Chemistry branch](../branches/CHEMISTRY.md) later. Optional recovery needs useful consumers and explicit storage/full-output behavior, without making an entire chemical plant mandatory for first steel. Apply the separately selected [recovery/pollution planning direction](https://github.com/jimbozoomer-byte/jugcraft/pull/210) when defining industrial emissions; this brief adds no new appearance, crop-damage or maintenance effects.

## Multiplayer, persistence, dependencies and assets

This contribution changes documentation only: no runtime registrations, recipes, feature flags, saves, assets or platform pins. Rollback is a documentation revert. Follow Minecraft 26.3 + Fabric and the existing [platform pins](../PLATFORM.md); no additional dependency or copied asset is proposed. Original planning prose follows the repository's MIT license.

Later implementation must keep server-authoritative, atomic inventory/recipe handling, shared ownership/access rules and bounded transport work. Test simultaneous players and automation, full outputs, interruption, unload/reload and restart without item loss or duplicate steel. Preserve existing coke, steel and machine IDs and saved inventories; adding charcoal must not break existing coal/coke production or disable registered content in old saves.

Follow [art direction](../ART_DIRECTION.md): larger industrial machines should communicate scale and function, and steel building sets should tile cleanly. This plan creates no new art. Regional sand rules remain in the refining plan; starter steel introduces no new ore, biome, season, realm, boss or event dependency.

## Verification and next decisions

Documentation checks are repository/local-link validation and whitespace checks; record actual results in the contribution PR. No new gameplay, two-client, restart, timing or performance tests were run for this brief.

Before implementing the charcoal or expansion recipes, audit shared material conversions and the full survival construction path; compare charcoal/coke small and bulk production; test output blockage and carbon consumption atomically; and verify persistence and two-client operation. The Encyclopedia should explain both carbon options, each useful product branch and optional expansion machinery without quest-gating ordinary production.

Next unresolved design choices:

1. **Steel plate access:** extend hammer/bench forming to small steel batches, or require the already reachable shaft Metal Press for steel plates? Electricity remains unnecessary either way; this does not revisit manual starter iron plates.
2. **First bulk-equipment wave:** develop larger foundry/press stations alongside current upgrades, or initially emphasize installed upgrades on the small machines? New stations remain optional unless a genuinely new process capability needs them.
3. Set charcoal quantities/time and compare complete supply costs; review foundry construction/site cost, steel output consumers, expansion batch sizes and energy/fuel use. No exact new quantities are accepted yet.

AI-assisted planning documentation: OpenAI Codex, GPT-6 family. No gameplay feature implementation is included.
