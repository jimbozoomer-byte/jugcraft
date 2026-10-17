# Tree batches 8, 8b and 8c: dry-country trees

Status: implementation in progress, owner-authorized 17 October 2026. Public branch: `codex/tree-generation-batch-8abc` in `speedygroyper/jugcraft`. Actual tool/model: OpenAI Codex (GPT-6). Starts at `8b98d4c4d`, retaining approved batches 6, 6b and 7 (PRs #399, #404 and #408) above main `2804ba015`. No later batch is included.

## Authorized plan

Batch 8 adds umbrella pine (9–11 tall, 7–9 wide), alpine stone pine (6–10 tall, 5–7 wide), pinyon (4–6 tall, 4–6 wide), wattle (4–6 tall, 4–6 wide) and wattle bushes (2–3 tall, 4–5 wide). Nut pines use approved pine timber and distinct evergreen cone-bearing needles. Wattle uses acacia timber, grey-green phyllodes and golden flowers. Pine nuts are renewable empty-hand harvests, roast at a campfire, and feed squirrels. Decorative leaves do not produce food.

Keep existing tree-attempt budgets. Mediterranean Forest: cypress 35%, olive 25%, olive bush 15%, umbrella pine 15%, lemon 5%, orange 5%. Heathland replaces its 25% vanilla pine with umbrella pine. Dryland replaces its 45% vanilla pine with pinyon; Canyon its 40%. Alpine Spawn changes to larch 50%, stone pine 20%, spruce 20%, juniper bush 10%, retaining 0–2 attempts/chunk and independent feature support. Eucalyptus Forest replaces its 20% oak bushes with wattle bushes. Gilded Shrubland becomes wattle bushes 70%, wattle 30%. Dune Beach stays treeless.

Batch 8b adds tamarisk: low forks and connected feathery crowns, 4–6 tall and 5–7 wide, sparse dusty pink plumes over grey-green leaves, and its own dusty-rose timber/dark furrowed bark. Wasteland: tamarisk 15%, dead tree 45%, snags 40% at one attempt/chunk on suitable soil. Oasis: tamarisk 10%, palm 75%, small palm 15%, retaining 2–3 attempts/chunk. Dry River: one candidate/chunk, 40% tamarisk on exposed suitable banks; the remaining 60% stays empty until batch 10's date palm. Tamarisk grows on sand and appropriate dirt, not bare salt/calcite or underwater.

Batch 8c adds Joshua trees, 4–6 tall and about 5–7 wide, with two or three irregular upturned arms and visibly supported spiky rosettes. Pale shaggy bark and khaki-grey yucca timber form a full wood set; the plant retains the Joshua Tree name. Lush Desert: Joshua 35%, desert acacia 45%, small palm 20%. Xeric Shrubland: Joshua 30%, desert acacia 45%, juniper 25%. Both retain 1–2 attempts/chunk. Natural generation and planted saplings support sand, red sand and appropriate dirt soils.

## Integration and validation contract

Discovery-tier building, forestry and optional food; gathering and renewable saplings provide the solo entry. Existing wood crafting, stripping, sawmill and tree-farm systems remain the consumers. No new dependency, machine or progression gate. Existing chunks are not rewritten; saved registrations remain when generation is disabled. Server-side harvest authority, regrowth and repeated-click protection reuse the existing fruit-leaf system.

Implement all three batches before running validation. Check generated data and art, biome shares and independent Alpine configuration, varied shapes and connected non-decaying crowns, soil acceptance/rejection, saplings, harvest/regrowth, recipes and wood processing. Build and run server tests, then a rendered client inspection covering natural biome stops, all tree forms, wood sets and a usable harvest demonstration. Deliver a matching ordinary-client world and pack. Tests have not run for this batch yet; two-client multiplayer and release deployment are not claimed.
