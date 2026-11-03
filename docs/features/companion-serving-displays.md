# Companion serving-display restocking

Owner-requested 30 October 2026, implemented with OpenAI Codex (GPT-6). Extends the existing Planner, job Filter and physical Supply deliveries. No new art, recipes, progression gates or production multiplier.

## Serving and market displays

Assign a display as an ordinary job and link its Supply containers. Peepo or Jughead brings the real finished food using existing bulk hauling, load-scaled energy and handling gestures. The display keeps its native capacity; no resident helper or new ticking block entity is needed. Serving displays do not expose Output extraction, so food remains available to players.

- Spider Cupcake Tower: thirteen Spiderweb Cupcakes.
- Ghost Meringue Platter: nine Ghost Meringues.
- Mummy Sausage Tray: eight Mummy Sausages.
- Eyeball Jelly Mould: eight Eyeball Jellies.
- Witch Finger Cauldron: ten Witch Fingers.
- Goodie Bag Tree: six Goodie Bags.
- Skull Goblets: four Witch's Brew Punch servings.
- Candy Corn Dispenser: sixty-four Candy Corn, with the native globe fill and manual crank/dispensing behavior retained.
- Plate, Platter and Serving Tray: one, four and four native display places respectively. Place the desired item in each position once; the exact item and components remain remembered after removal. Replacing a displayed item changes that place's template. Empty unconfigured places are not filled arbitrarily. The Filter limits which remembered item kinds may be replenished.
- Pumpkin Wheelbarrow, Pumpkin Wagon and Pumpkin Pyramid: fill their native display spots from the native produce tag. Use the Filter to choose allowed pumpkin, melon, squash or gourd kinds. Empty Filters allow any native produce. Handled items retain their exact components.

Fixed-food displays accept only their native serving. Their native representation stores a count rather than individual item components, so automation refuses custom-component variants instead of silently erasing names or other data. Existing manual interactions and drops remain unchanged. Curiosity cabinets, bell jars, broom racks and decorative props are not serving jobs.

The existing Filter is an allow-list, not a production recipe or output stock target for these displays. Their physical serving capacity is the restocking target. Supply Off stops delivery. Output is marked unsupported in the companion menu.

## Persistence, transactions and server work

Filters and remembered food layouts are saved on each display. The seven party display blocks gain one unticked settings block entity; their native serving counts remain in block states. Existing placed displays retain those counts. Generic food and produce displays retain native inventory keys and drops. No duplicate companion inventory is introduced.

Insertion probes and nested aborts restore real slots/counts and remembered templates. Visible fill/client/comparator updates happen only after the outer transfer commits. Food-display layout observation only checks its at-most-four places. Produce insertion is bounded by its native display capacity, up to twenty-five places. No nearby-world scans or chunk loading are added.

All accesses require the current loaded station, a tamed assigned companion and live owner permissions. Produce displays normalize every part to the master and validate every loaded part's identity, orientation and permissions. An incomplete structure, replaced block, removed assignment or revoked trust refuses a cached transfer. Native manual removal immediately makes room for replenishment.

## Focused validation

`PeepoServingDisplayClientTests` covers all seven party families, all three generic food displays, all three produce displays and the candy dispenser. It checks native capacities, transactional/nested rollback, component preservation or explicit count-store refusal, remembered per-place templates, native save/load, Filter menu/ghost behavior, missing multiblock parts and live whole-footprint claim/trust changes. Autonomous cases exercise each of the four display families for both companions using real Supply containers, with manual-consumption replenishment and no Output draining.

### Result — 30 October 2026

`build-local.ps1 -Tasks @('runClientGameTest','-PclientTests=PeepoServingDisplayClientTests')` passed **353 assertions** with compilation in a disposable flat world. Evidence: `build/peepo-serving-display-tests.log`. All eight autonomous routes (four display families for each companion) completed native-capacity replenishment and replaced manually consumed servings without draining food to Output. Existing party display states retained their counts when the new settings block entity was created.

Validation used the working tree based on `22a573abef948af56dadbbb8b914df4ddac426be`, Minecraft 26.3, Fabric Loader 0.19.5 and Fabric API 0.161.0+26.3, with the repository's pinned development dependencies. No full modpack suite, two-client playtest, distribution update or large-server benchmark ran.

The first run exposed fixture contamination: removing an earlier full dispenser dropped 64 candies into the later crank test. Cleanup now discards loose items within the disposable fixture after removing blocks, while the exact-one crank assertion remains. The cleanup's coordinate constructor was also corrected for Minecraft 26.3 before the passing rerun. Neither correction changed runtime behavior.
