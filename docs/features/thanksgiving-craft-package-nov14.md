# Thanksgiving craft package — November 14

Status: all four external Claude workers and their four bounded follow-ups delivered and integrated locally. Implementation, selected combined local validation and personal rendered-model review are complete. Coordination and local review: OpenAI Codex. Claude's UI remains on Opus 5.5 Extra, unchanged. Local work only; no release, updater, profile or live-world changes.

The owner requested a substantial Thanksgiving package, with Pilgrim and Native American decorations and the warmth/composition of Leyendecker and Norman Rockwell illustrations, translated into Minecraft's existing block style. The 32-candidate plan resolved to **27 new props, three refinements and two existing-system reuse decisions**. Runtime acceptance is recorded below separately from implementation.

## Four parallel groups

| Worker | Candidate pieces | Behavior and scope |
|---|---|---|
| A — Pilgrim homestead and feast room | High-backed oak settle; carved dish dresser; hat/bonnet/cloak peg rail; pewter pitcher and basin; candleholder/snuffer; embroidered harvest sampler; quilt rail; butter churn | Proper seats where applicable; supported wall decor; candle lighting. Reuse existing `quilt_rail` and functional `butter_churn`, confirmed in the local language registry. Inventory other furniture before adding IDs. |
| B — Woodland harvest crafts | Lidded twined basket; carrying basket; birch-bark box; connected rush mats; carved bowls/spoons; incised earthenware vessel; corn-drying frame; wooden corn mortar/pestle display | Specifically referenced everyday Wampanoag/Northeastern woodland forms. Floor mats connect with deliberate edge states. Decorative containers are labeled as such; no accidental hidden inventories or duplicate alchemy mortar. |
| C — Harvest town details | Timber entrance arch; covered farm-stall/canopy kit; pottery/herb rack; folded linens; copper warming dish; pumpkin-carving workbench display; autumn window box; feast wayfinding signpost | Player-placeable first. Reuse existing stall/window-box/sign systems where equivalent. Doorways and arches have genuinely open collision. No town template or migration changes. |
| D — Festival toys and lively props | Corn-husk dolls; turkey pull-toy; spinning top; tabletop sailing ship; harvest photo backdrop; beanbag/corn toss set; pumpkin prize rosette; harvest percussion display | Bounded use animations/sounds where appropriate. A simple toss interaction must validate nearby users on the server, reset cleanly and have no currency or wagering rewards. No new giant minigame framework. |

There are 32 initial candidates, including deliberate reuse candidates. Each worker must record new, enhanced, covered and pending separately. Equivalent existing items count as covered, never as newly implemented.

## Existing content to preserve and reuse

The [earlier decoration record](thanksgiving-decor.md) already covers nineteen props and an Autumn Leaf Table Runner. Feast tables, harvest place settings, baskets, bread basket, gravy boat, butter dish, pie stand, wall garland, pumpkin centerpiece, wreaths, hay seating, corn/wheat bundles, scarecrows, cider barrels, produce carts and cornucopias already have implementations. Current town centerpiece/feast work remains in its own migration ledger. Do not add duplicates or overwrite owner-placed blocks, including the church slot machines.

Other unfinished local deliveries (Nov13 A–D polish, the aircraft aim follow-up, E–H progression/economy/raids/performance, horses and Pegasus) remain separate. New assignments do not mark those integrated or tested.

## Art references and acceptance

- Warm ochre, rust, cream and natural wood, composed feast scenes and readable silhouettes; no paintings pasted onto cubes.
- Follow `docs/ART_DIRECTION.md`, `docs/FOOD_MODELS.md` and the existing feast/basket palettes and generators. Inspect Shepherd's Pie, Gleaming Salad and Nachos as modeling references.
- Vanilla-consistent texel density; restrained material tones; closed vessels and modeled rims, handles and joints; no floating details, noisy smudges, paper-thin fake food or item-icon squares standing in for models.
- The woodland set uses everyday craft references from [Plimoth Patuxet's pottery collection](https://plimoth.org/yath/unit-1/wampanoag-pottery-sherd), [Peabody's Wampanoag objects](https://collections.peabody.harvard.edu/groups/wampanoag/results) and [Wampanoag voices](https://peabody.harvard.edu/listening-wampanoag-voices-beyond-1620-transcripts). Record the actual reference used for each piece; describe game adaptations as inspired designs rather than exact reconstructions. Do not combine unrelated nations' ceremonial motifs as interchangeable decoration.
- Review textured fronts, backs, undersides, four facings, connected/end states and normal-distance silhouettes alongside an existing feast prop and vanilla scale reference. Generated previews are labeled as such, not called Minecraft or Blockbench screenshots.
- Coordinator must open the images and return weak work for revision. A worker's assertion, element count or successful registration is not visual acceptance.

## Gameplay and integration acceptance

Use reachable crafting materials without research or calendar locks. Supply correct models, item definitions, blockstates, drops, recipes, names, support checks and voxel shapes. Multiblocks place atomically without replacing occupied cells, break cleanly and drop once. Existing claims protect edits/removal; public interactions are allowed only for appropriate non-container behavior. Persist earned decor after the season.

Workers own separate modules/worktrees; the Claude coordinator owns shared registration/language/test-hook merging. Deliver stable base hashes, ordered patches, editable generators/model sources, provenance, checksums, preview contact sheets and actual test evidence. Codex reviews integration and runs relevant combined local checks. Cloud syntax or resource checks cannot substitute for Minecraft compilation and game tests.

## Checkpoint

- Owner's four-worker brief sent to the existing Claude session on November 14. Claude began main/content inventory before allocating worktrees.
- Codex found existing `quilt_rail` and `butter_churn` during the local duplicate audit.
- No holiday delivery accepted or game-tested at this checkpoint.

### Four workers verified running

The Claude UI showed all four tasks running: `TG-A: Pilgrim homestead feast room`,
`TG-B: Woodland harvest crafts`, `TG-C: Harvest town details`, and
`TG-D: Festival toys lively props`. Local screenshot:
`build/nov14-review/four-thanksgiving-workers.png`. They use separate sparse
worktrees based on `8075c6d58b`; existing dirty cloud worktrees were preserved.
The coordinator prepared a shared real-texture preview renderer with back-face
culling, all-facing/underside views and a vanilla texel-scale comparison. The
renderer is review tooling, not proof that the new pieces render in Minecraft.
Workers were specifically told to reuse existing quilt, churn, window-box and
wayfinding systems and to check data-tag dependencies as well as Java behavior.
All 32 candidates still require a recorded new/refined/covered decision and
actual visual review; no new holiday content is accepted at this checkpoint.

### Early visual review

Codex opened A's textured settle preview: restrained rust cushions, supported
arms, panelled front, plain-board back and closed underside are a promising
direction. Two independent seats and all-facing multiblock behavior remain
unverified. B's first-pass contact sheet was opened; it is not accepted. The
Claude coordinator returned the box lid, pot profile, mortar, carrying basket
and mat weave for revision. Codex supplied summaries from the Plimoth pottery
page and Peabody Wampanoag voices transcripts after reading them with the web
tool. The collection-results page returned 403. No Claude environment/network
permissions were changed. Sources and adaptations must be attributed honestly.

### Personal review of A, B and D sheets

Codex opened all five A sheets. Settle, dresser and pitcher/basin are promising.
Returned cloak/coif silhouettes, sampler stitches, snuffer shape and churn: the
churn revision was noisier than the original and must preserve its clean coopered
body. B baskets, bark box, drying frame and joined rush mats passed the rendered
preview review; the pottery square collar was returned for one further revision.
D photo frame, spinning top, toss board and percussion passed preview review.
D dolls, ship and turkey tail were revised by Claude; revised sheets and runtime
behavior are still pending local inspection. No Thanksgiving patch bundle has
been accepted locally yet. These are generated model previews, not game captures.

### Four initial bundles received and reviewed

All four ZIPs are downloaded and their coordinator SHA-256 values verified in
`build/nov14-review/tg-{a,b,c,d}`. Scope resolves to **27 new props, three existing
pieces refined (quilt rail, churn, planter contents), and two reuse decisions
(farm-stall/canopy and wayfinding)**. These are implementation candidates, not
27 runtime-accepted items. No town placement or migration is included.

Codex personally reviewed the revised A sheets, final B pot, all C pieces, and
D's revised dolls, turkey cord, ship and top poses. The clean churn body was
restored, the pot now reads as terracotta rather than a wooden turret, and the
ship has readable sails and a continuous supported hull. The arch has a genuine
3-by-3 opening. These revisions pass generated-preview review; in-game inspection
is still required. A's module and shared hooks are integrated locally with
backups; its 14 Python checks and common Java compilation pass. B–D modules are
staged separately for integration after the running test build finishes.

The same four Claude workers now have bounded follow-ups: A canonical seats and
public/private interactions; B cross-group crafting and essence economy; C a
real client gallery; D multiblock removal/claims safety. No recurring heartbeat
or extra worker was created. Narrow public corn-toss use is approved as an
ordinary non-container festival interaction; player claims must remain intact.

B–D's 244 module files and B/C/D shared hooks are now integrated, with all prior
files backed up under `build/nov14-review/before-{bcd-integration,d-hooks}`. Local
compilation is running. All **38 combined Thanksgiving Python tests pass**.
Codex corrected cross-platform validation rather than changing artwork: six PNGs
had identical dimensions/RGBA pixels but different zlib encodings, so B/C compare
decoded pixels; the lying top's computed translation is rounded to six decimals
to eliminate libm tail-bit differences in JSON. Two comparator regressions prove
that changed pixels/dimensions still fail. The initial homestead implementation
passed all eight server tests as part of the 30-test raid/homestead run. Later
interaction fixes and the remaining three groups still require runtime checks.

### Combined implementation checks

All four groups compile together (`thanksgiving-compile-r1.log`). The full data/art
check passes: 15,365 material IDs; 38,900 block/item models, 3,786 quad parts and
185 combined models (`thanksgiving-data-r1.log`, existing allowlists retained).
This is structural validation, not visual acceptance in Minecraft.

Codex found and fixed a generator-ownership bug: nine woodland PNG files were
listed in the material JSON manifest, so a later material regeneration would
prune them. Only those nine entries were removed; textures remain owned by the
texture pipeline. A regression performs real stale-file pruning in a temporary
resource tree and proves that every woodland texture survives.

C's client gallery is integrated. B's economy audit is integrated with 27 explicit
recipe-derived values, preserving all local price changes. The Pegasus creative
spawn egg now has the generator's explicit one-essence fallback record; this does
not add a survival source or tablet output. Twenty economy/value tests pass.
Following the owner's standing inexpensive-building-decor policy, 19 of the new
props at or below the existing 128-essence ceiling are now explicit tablet outputs.
The eight costlier props stay crafting-only. No town shop buys these props; no
recipe consumes them into other materials; the toss game has no economic reward.
A full values rebuild is running into a review-only file, not overwriting prices.

A/D seating and cross-part protection follow-ups, the final combined server run,
and actual client screenshots still remain. Nothing has been published or deployed.


### Final follow-up integration

All four Claude follow-ups are received; all workers are idle. A's shared
before-break guard protects every part of multi-block props, including non-OP
creative breaking; explosions cannot tear a claimed part down through an
unclaimed part. Settle halves share one canonical seat per cushion between
players and companions, and teardown ejects sitters and releases reservations.
Furniture refuses water/entity overlap. D's backdrop and public corn-toss tests
are integrated. The corn toss has one narrow `town_usable` tag entry, keeping
private claims and container protections intact. Codex additionally made the
harvest and haunted arches fluid-resistant without filling their open passages.

All 62 combined Thanksgiving/resource/economy Python tests pass after these fixes.
A full review-only value regeneration initially exposed four products locked at
fallback before their crop ingredients resolved. The builder now assigns the
same established crop-root budget first; all **27 new records reproduce exactly**
against Minecraft 26.3. Existing unrelated generated-price differences were not
silently applied. The combined 17-class server test run is now underway.

Pre-existing follow-ups outside this craft batch: canonicalize porch-swing seats;
review fire affecting a candy-chute's unclaimed part; audit other multiblock implementations with separate
teardown. These are not claimed fixed by the new prop-specific checks.

Private-claim lighting toggles are now checked over every part as well; town
public-use behavior is retained. The owner/stranger toggle assertions and all
Thanksgiving server cases passed in combined run r2. The four initial holiday
failures were fixture setup problems, detailed in the local integration ledger.
The remaining r2 failures concerned the older aircraft/raid cohort and have
local corrections awaiting the final rerun. Actual client-gallery review remains.

The corrected combined server run now passes **all 118 required tests** across
19 selected classes (`final-combined-server-r3.log`), including every Thanksgiving
server case and the integrated earlier work. Rendered client checks are underway.

### Rendered review

All six selected client suites passed together in `final-combined-client-r1.log`
(105 preserved screenshots in `build/nov14-review/client-r1/`). Codex inspected
the day/night homestead, arch front/back, vessel/craft silhouettes, connected mats,
backdrop lighting, and representative shots of every earlier client suite.
A second gallery added ten close-ups for small craft details: basket rims and
weaves, bark-box seams, bowls/spoons, the four-point pottery rim, corn bindings,
mortar/pestle, supported crock rack, gourd planter, doll folds, turkey cord,
spinning top, ship stand/rigging and fair props. All ten were personally viewed
and accepted as Minecraft-rendered pieces, not just offline model previews.
Those captures are preserved under `build/nov14-review/client-closeups-r2/`.

The two-class close-up/HUD rerun then exposed a test-state leak: the gallery
left Minecraft's HUD hidden and the HUD suite relied on another prior test
restoring it. Both fixtures now explicitly restore their prior HUD state.
The bounded pair is being rerun; no production asset change was needed.

### Local acceptance — final

- Four initial Claude bundles and four follow-ups are integrated and reviewed.
  Final scope: **27 new props, three refinements, two existing-system reuse decisions**.
- **118/118 selected server tests** pass together; six selected client suites
  pass together. The final two-class visual/isolation rerun also passes after
  explicit HUD-state restoration, with 30 captures preserved in
  `build/nov14-review/client-final/`. The new close-ups and clean HUD views were
  personally inspected by Codex.
- **62 Thanksgiving/resource/economy Python checks** and **37 aircraft/raid/test
  isolation checks** pass. Full data/art validation passes: 15,365 material IDs,
  38,900 block/item models, 3,786 quad parts and 185 combined models, retaining
  the repository's existing allowlists. This does not mean every older model
  in the repository has received a new visual review.
- All 27 explicit new essence values reproduce from the pinned Minecraft JAR;
  19 inexpensive props are approved tablet outputs, eight remain crafting-only.
- No automatic placement/migration touched the owner's town. No commits,
  pushes, PRs, releases, client profile/updater changes or live server actions.
- These are selected local regressions and one rendered client JVM, not the
  whole repository suite, a two-client public-server test or a load benchmark.

**Owner review correction, November 14:** the owner rejected the decoration
preview, particularly the festival backdrop and toys. The previous coordinator
visual acceptance above is withdrawn for that group; passing functional tests
remain recorded but do not establish appearance quality. Remodeling and fresh
in-game review are required in [the expansion record](thanksgiving-expansion-nov14.md).

The separate five-ground-breed
gameplay, giant Finnhorse rearing intersection, Pegasus survival source and
observer-client test, broader economy repricing and measured performance
baselines remain open in their own records; they are not counted as holiday
package completion.


## Follow-up to the rejected decoration screenshot

The owner-rejected backdrop, turkey pull toy, tabletop ship, corn-toss board and spinning top were remodeled in the [November 14 expansion](thanksgiving-expansion-nov14.md). Codex reviewed the revised actual Minecraft gallery personally, including front/rear/detail views, the hole through the toss board and the top's resting/spinning/wobbling states. The seasonal client cohort passes. This restores coordinator visual acceptance for the revised models, not the rejected earlier versions; owner play review is still separate. The in-game comparison is saved at `build/nov14-expansion-review/client-seasonal-r1/screenshots/0012_jugcraft_thanksgiving_d_toys_close.png`.
