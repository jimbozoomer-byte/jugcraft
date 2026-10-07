# Companion regression tests

Owner requested companion-only automated testing on 7 October 2026, superseding the earlier instruction to skip tests. Implemented and run with OpenAI Codex (GPT-6).

## Result

**19 groups passed, 143 assertions, zero failed groups.** The successful run finished on 7 October 2026 at 14:09 local time, on `peepo-companion`, against base commit `c9fb3d14` plus the standing cooking animation change.

Runtime: Minecraft 26.3, Fabric Loader 0.19.5, Fabric API 0.161.0+26.3, Java 25.0.4.1+1, with the repository's normal development integrations. Tests used newly generated, disposable flat worlds. No player saves were opened. Only `PeepoCompanionClientTests` executed; unrelated test classes share the compilation source set but their suites did not run.

Run from the repository root in PowerShell:

```powershell
.\build-local.ps1 -Tasks @('runClientGameTest','-PclientTests=PeepoCompanionClientTests')
```

The equivalent Gradle command is `./gradlew runClientGameTest -PclientTests=PeepoCompanionClientTests`. Do not use unfiltered `build` or `runClientGameTest` when requesting companion-only coverage.

## Coverage

- Feeding and first-feed ownership, friendly fire, blush, costume equipment, full-health/full-energy refusal, the 95% energy threshold, healing, meal buffs and restoration of the held torch after eating.
- Initial energy reserve, transactional rollback, the shared 64 JE/t extraction limit, no regeneration during work, moving passive regeneration, chair regeneration, daytime sleep refusal, food quality and recovery hysteresis.
- Eight cargo slots and two equipment slots, rollback, inventory/owner/equipment serialization, costume-slot restrictions and invalid companion-menu inputs.
- Home plus four jobs, rejection of a fifth job, ordering, removal compaction and assignment serialization.
- Lunch crate and chest-cover item conservation, full cargo, and foreign lunch-source ownership.
- Actual wheel energy conservation, repeated same-tick calls, full buffer, exclusive occupancy, release and side-only energy lookup on both lower blocks.
- Stool edge position and cardinal/diagonal rail seat geometry. This exercises the diagonal surface adapter directly; it does not establish compatibility with a separately installed diagonal-fence mod.
- Sixteen bed registrations, shared upper/lower bunk entrance, Jughead sleeping, increased sleep regeneration and ground-level exit.
- Sixteen simultaneous budget requests: four searches and eight paths admitted in one server tick, with the rest deferred. This is a budget assertion, not a server-load benchmark.
- Cooking recipe plans, invalid recipe IDs, required heat/ingredients, full outputs, ingredient filtering, one-helper exclusivity, energy cost, repeated same-tick assistance and release.
- Both companions automatically approach and stir a pot. Measured progress over 40 server ticks matches 1.5x speed and 16 JE/t energy cost. Client animation state arrives and both rigs render with the angled spoon. Feet match the rim surface and edge, and the entity remains stationary throughout stirring.
- Model pose assertions for closed sleeping eyes, Jughead clothing, pumpkin visibility, general two-arm work, forward spoon grip, moving stirring arms, planted feet, alternating seated kicks and running strides.
- Automatic loose-food pickup and eating; swamp spawn registration with groups of 1–4, separate six-per-variant local caps and saved natural-spawn origin.
- Actual companion ghost-recipe slot synchronization and slot packets: setting/clearing preserves the four real cursor items, a one-item ghost remains separate, and shift-click, creative clone, drop and hotbar swap cannot extract it. The ordinary pot GUI rejects recipe-edit buttons. Companion command packets, world save/reopen, and a real in-process dedicated server with one client disconnecting/reconnecting. Owner, cargo and preferences survive reconnect.
- Automatic seated companion unload: release does not move the entity during section removal; its safe exit occurs on a subsequent tick. A world holding the seated companion closes and reopens successfully.

Runtime screenshots of the companion GUI, ghost recipe slot, ordinary pot and both stirring variants were inspected. Local evidence is under `build/peepo-rim-evidence/`; Gradle's disposable run files are under `build/run/clientGameTest/`. These generated logs, images and worlds are not committed.

## Earlier regression retained

Releasing a seated companion from Fabric's entity-unload callback could teleport it into another entity section while Minecraft was removing its old tracking entry. This produced `IllegalStateException: Entity is already tracked!` during shutdown and prevented a clean save close.

Unload now releases the routine with movement deferred. Bed, seat and cooking safe-exit markers remain available for the next active tick/load. Wheel release similarly records a safe exit instead of moving from the unload callback. Ordinary releases still move immediately. No data schema or dependency changes were needed.

## Limits

This is not a two-player concurrency or multiplayer-capacity certification. Large populations across dimensions, representative machine loads, every biome, survival crafting, external furniture/diagonal-fence integrations, optional dependencies absent, and dynamic-light/shader appearance remain outside this run. The GUI tests validate data and button packets; they do not automate every mouse interaction or every GUI scale. Existing Blockbench exports were not regenerated.
