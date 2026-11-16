# Five horse variants for Blockbench

Owner requested five editable variants based on Minecraft's actual horse model. Local art/review scope; no gameplay registration or release yet.

Coordination: OpenAI Codex; external modeling workers requested through the owner's Claude Code session (UI shows Opus 5.5 Extra; unchanged).

## Assigned batch
- Shared verified vanilla base plus small Icelandic and Mongolian horses.
- Shire and deliberately oversized fantasy Finnhorse.
- Friesian (chosen fifth): black coat, full mane, arched neck and feathered hooves.
- Coordinator: shared hierarchy/UV review, previews, downloadable bundle and checksums.

Require separate editable .bbmodel files, source/version provenance, preserved head/leg/tail hierarchy and saddle compatibility, side/front/three-quarter views and a vanilla comparison lineup. Do not report generated previews as actual game validation.

## Evidence and status
- Horse batch instructions sent to Claude; base discovery started. Individual worker starts and finished deliverables remain to be verified.
- Local pinned 26.3 minecraft-client-only.jar contains HorseModel, AbstractEquineModel and EquineSaddleModel under net.minecraft.client.model.animal.equine.
- Read-only javap output saved to build/horse-reference/vanilla-equine-javap.txt as a local geometry reference. No model export or Blockbench import has been completed locally yet.
- Earlier A-H deliveries remain separate and require local integration/review; do not overwrite those changes.

Verified follow-up: Claude UI shows Running 3: W1 base/Icelandic/Mongolian; W2 Shire/giant Finnhorse; W3 Friesian. Exact local javap reference attached to Claude after cloud source discovery failed. Delivery remains pending. Screenshot: build/horse-reference/claude-horse-workers.png.

## Refinement pass approved

V1 delivered: all five .bbmodel variants, textures, fitted small-horse saddles, geometry-only large-horse saddle proposals, generated previews. Codex downloaded the bundle and verified 171 checksums. ZIP SHA256: 26cae07bf96d4fceacdc52d1a068adeeae7ccbc675e97c96d35c5d0b4916e979. Extracted under build/horse-review/jugcraft-horse-variants. No Blockbench or game validation yet.

Owner requested refinement. Sent three-worker v2 brief to existing Claude session:
- Icelandic/Mongolian silhouette, mane and restrained coat detail.
- Shire/Finnhorse shoulders, haunches, feathering, flaxen mane and complete saddle textures.
- Friesian arched crest, draped mane, tapered tail and readable black coat.
- All five: complete fitted saddle/bridle art, combined editable preview projects, sampled walk/graze/rear poses, fix tail-ground penetration, before/after comparisons. Preserve v1 and earlier A-H work; no deployment.

Codex resolved the two v1 source assumptions using the local pinned 26.3 LayerDefinitions bytecode: createBodyMesh receives CubeDeformation.NONE and LayerDefinition.create uses 64x64. Additional discovery: HORSE and HORSE_SADDLE apply MeshTransformer.scaling(1.1f). MeshTransformer applies PartPose.scaled(scale), then Y translation 24.016*(1-scale), i.e. approximately -2.4016. Thus v1 is the raw mesh, not the final adult layer. Do not double-bake scale into game integration. Full evidence saved locally to build/horse-reference/vanilla-horse-layer-javap.txt. Refinement deliveries remain pending.

## Breed gameplay proposal and Pegasus assignment

Owner requests differentiation ideas plus a working flying Pegasus, delegated to another Claude agent. These five-breed traits are design proposals, not implemented statistics:

| Breed | Specialty | Proposed mechanic | Tradeoff |
|---|---|---|---|
| Icelandic | Narrow trails and snowy exploration | Compact collision size; reduced snow slowdown and modest safe-step traversal | Lower top speed and carrying capacity; no universal fall immunity |
| Mongolian | Long expeditions | Best sustained travel efficiency and recovery if a shared stamina system is added | Moderate sprint speed/jump; endurance is not an existing feature yet |
| Shire | Supply hauling | Largest optional pannier inventory, strong health and knockback resistance | Slow acceleration, lower jump and large clearance; cargo is new work |
| Giant Finnhorse | Heavy wilderness mount | Highest health, long stride and modest terrain step-up | Broad collision box, wide turns and limited tight-space access; no terrain destruction |
| Friesian | Fast travel and riding courses | Best road sprint speed, responsive steering and good jumping | Less cargo/durability than draft breeds, less endurance than Mongolian |
| Pegasus | Aerial exploration | Mounted flight, Space ascent, Ctrl descent, visible flapping/folding wings | Lower cargo and ground speed than specialist land breeds; flight stamina remains a future balance proposal |

Common rules: tame/saddle normally, no research locks; modest within-breed variation must not erase specialties. Show eventual breed traits in the mount UI. No towing/cart compatibility promised without actual integration.

Pegasus runtime brief sent to Claude coordinator: fourth isolated agent; server-authoritative rider controls, rebindable scoped Space/Ctrl bindings (resolve sprint conflict), Shift dismount, WASD horizontal flight, smooth takeoff/landing and collision; synchronize animation for observers; folded grounded wings, unfold on takeoff, articulated flap in flight; persist ownership/identity, reset stale controls. Preserve existing art workers and all local changes. Supply test acquisition and record survival source pending if absent. Review focused tests and actual compilation/gameplay separately. No deployment authorized. Agent launch pending UI confirmation.

## November 14 delivery review

All three refinement workers and the fourth Pegasus worker finished in Claude. Codex retrieved and safely extracted their archives under `build/nov14-review`, verified the archive hashes and every top-level checksum entry:

- Horse v2: `jugcraft-horse-variants-v2.zip`, SHA256 `ad7e03a0779bda9830e2eb9989b026088f9ca8aada031bf067e4c3a05005bfa8`; 316 file checksums verified. All five breeds now include painted fitted saddle/bridle models and combined editable previews.
- Pegasus: `jugcraft-pegasus-delivery.zip`, SHA256 `84d3b3f32adb999e756d6304e2a0609c5e75d8b8b1c2fdfde4e7400340b3c8e7`; 79 file checksums verified. Seven patches on `8075c6d58b`, plus art, tools, checks and reviews.
- Codex viewed the v1/v2 three-quarter lineup, saddled side lineup and Pegasus wing-pose sheet. These are generated previews, not game screenshots. Shire feathering, Friesian mane and fitted saddles are improved. The giant Finnhorse retains a 6.94-model-pixel hind-leg/rump intersection at maximum rearing, explicitly unaccepted for final gameplay integration. Smaller reported grazing intersections also remain documented in the delivery.
- Ground-breed gameplay differentiation, hitbox/seat changes and survival sources remain proposals. The art delivery alone does not put five new breeds in the game.
- Pegasus patches were applied first to a scratch copy of the exact touched local files. Existing client initialization and language edits were preserved when two shared-file hunks conflicted. The resulting 35-file change was copied into the dirty local checkout only after checking that those originals had not changed concurrently. No commits/index operations or remote actions.
- Local pinned 26.3 `KeyMapping` bytecode resolves the cloud's single-binding concern: both `click` and `set` invoke `forAllKeyMappings`, iterating a list for each physical key. This supports retaining Space/Ctrl defaults; actual client input tests are still required.
- Local Java25 compilation and focused validation started; results will be recorded separately. The cloud reported 773 pure flight assertions, not Minecraft runtime success.

The four external workers were reassigned to the [Thanksgiving craft package](thanksgiving-craft-package-nov14.md); their holiday work must not overwrite these deliveries or earlier A–H work.


## Subsequent local gameplay integration

The five v2 ground breeds are now registered gameplay, with biome spawning, fitted saddles, measured hitboxes, distinct bounded traits, saved crossbreed ancestry and cargo where supported. Pegasus has survival spawning, Pegasus-only breeding and a nine-slot pack. These supersede the earlier art-only/proposal status; authoritative behavior and current evidence are in [horse breeds](horse-breeds.md) and [the November 14 integration](thanksgiving-expansion-nov14.md).

Codex inspected actual walking, grazing and rearing captures. The final giant Finnhorse rearing view no longer shows the conspicuous old rump/leg intersection; no claim of mathematically intersection-free cubes at every frame is made. Actual rider tests caught and fixed the generator's two-pixel reference-seat error. All five rendered rider comparisons, reload/road synchronization, final combined server cases and two-client state checks pass. All seven clear observer pictures were captured and personally reviewed with unused pets ordered to STAY. Owner play review remains separate.
