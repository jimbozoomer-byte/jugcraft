# Companion confection stations

Owner-requested 30 October 2026; implemented with **OpenAI Codex (GPT-6)**. Extends existing companion assignments, nine-item Filter, production stock targets, physical Supply/Output cargo and transfer animations. No new recipes, assets, dependencies, fake player, world scans or block tickers.

## Caramel Apple Stand

Assign any part of the stand with the Planner. Supply provides vanilla Apples, Sticks and Caramel; each apple consumes one of each native ingredient and occupies one of the six existing cooling-tray spots. Native cooling remains **600 game ticks**. Output collects only set caramel apples; warm apples stay on the tray. The pot, stick jar and cooling timestamps remain the original saved native state, and player interactions still work. The companion limits ingredient deliveries to available tray capacity and configured stock demand instead of filling unnecessary reserves.

## Cotton Candy Cart and Caramel Corn Wagon

The player still switches on the Cotton Candy Cart and lights the Caramel Corn Wagon; companions respect these controls. The native wagon does not consume fuel, so this integration adds none.

- Cotton Candy Cart: one Sugar makes one native floss serving, up to three at once; one Paper winds one serving into one Cotton Candy. Finished cones wait in a saved output tray capped at **64 total items**.
- Caramel Corn Wagon (`popcorn_wagon`): one Corn Kernel uses the original **60-tick** scheduled popping action and native case. Filter for Popcorn to deliver plain servings, or Caramel Popcorn to reserve two popped servings and combine them with one Caramel. Blank filters prefer caramel popcorn when caramel is available, otherwise plain popcorn. Existing stock priorities select among allowed products. A started caramel pair retains its mode until completion and may fetch its missing second kernel even when its projected batch already covers the target. Finished coated popcorn uses the same bounded 64-item tray.

Output never takes the plain intermediate popcorn reserved for caramel coating. Existing filters may be changed during work without deleting products. Empty-hand sneak-use on either cart retrieves its buffered companion products; breaking it drops that buffer once in addition to the native block contents. Collect buffers before downgrading to a version that does not save them.

## Shared limits and compatibility

All three use ordinary load-scaled companion delivery effort; passive cooling/popping costs no additional companion energy. No speed bonus, item duplication chance or new standing work action is added. The existing transfer gestures show physical deliveries. Native multiblocks are navigation obstacles rather than paths through their partial collision shapes.

Transactional snapshots cover native counters, cooling times, floss/popcorn, pending popping and products. Fill-state changes and scheduled popping are published only after commit, so failed simulations neither spend supplies nor schedule free production. Native item-component identity is enforced when extracting property-backed products. Native state changes invalidate existing readiness caches.

Cotton deliveries contain at most one native three-serving sugar/paper load per planning transaction. They do not depend on repeated simulated conversions that cannot be reproduced by delivering merged cargo stacks in order. Shared stock hysteresis is not persisted by a rolled-back delivery-planning simulation; a projected completion cannot stop an unfinished replenishment cycle.

The bounded, loaded footprint must remain complete, matching and accessible on every part. Every cached port rechecks the active assignment and current claim permissions. No neighboring chunks are loaded. The shared stock system counts finished goods, cooling apples, floss, popping/paired kernels and registered inbound/outbound deliveries without treating each recipe ingredient as a separate output. Existing native save keys and block entity type IDs are retained; two animated-cart entity types gain optional persisted companion state, while unrelated decorations remain empty.

## Focused validation

`PeepoCaramelAppleClientTests` covers native dipping/cooling boundaries, the full tray, nested rollback, display updates, native save/load, arbitrary-component rejection, incomplete structures, revoked assignments and foreign claims. Both Peepo and Jughead execute autonomous two-apple stock-target Supply/Output routes while leaving excess ingredients in Supply.

`PeepoMidwayConfectionClientTests` covers the off/unlit controls, sugar/paper conservation, full buffers, native scheduled popping, transactional probes, caramel intermediate reservation and second-kernel completion, save/load, component identity and permission/structure guards. Both companions run Cotton Candy (target eight, larger than the native bowl), plain Popcorn and Caramel Popcorn production with stock targets and real delivery routes. A separate in-progress refill starts with six of eight cones in Output, above its restart threshold of two, verifies repeated simulations do not cancel replenishment, then delivers exactly the remaining two.

### Results — 30 October 2026

Validation used the working tree based on `22a573abef948af56dadbbb8b914df4ddac426be`, Minecraft 26.3, Fabric Loader 0.19.5 and Fabric API 0.161.0+26.3, with the repository's pinned development dependencies.

`build-local.ps1 -Tasks @('runClientGameTest','-PclientTests=PeepoCaramelAppleClientTests')` passed **86 assertions** with compilation in a disposable flat world. Evidence: `build/peepo-caramel-apple-tests.log`. Both companions completed real stock-limited Supply/Output routes, including the full native cooling delay.

`build-local.ps1 -Tasks @('runClientGameTest','-PclientTests=PeepoMidwayConfectionClientTests')` passed **140 assertions** in a separate disposable flat world. Evidence: `build/peepo-midway-confection-tests.log`. Both companions completed Cotton Candy, plain Popcorn and Caramel Popcorn deliveries, including the larger eight-cone target and the above-threshold in-progress refill regression.

No full modpack suite, two-client multiplayer playtest, distribution update or live-server deployment ran.
