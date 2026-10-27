# Herobrine: player coverage and seasonal scheduling

Owner-directed collaboration, 27 October 2026. Scheduling, persistence, adapter and tests: OpenAI Codex (GPT-6). Entity, renderer, placement, world clues and encounter content: the owner's Claude session on `claude/herobrine-haunting`. No deployment is authorized by this feature record.

## Player outcome

Every eligible active player gets their own opportunity to see Herobrine. Until **November 3, 2026 inclusive**, the first attempt becomes due after 5–10 active minutes and subsequent sightings after 10–20 active minutes. From **November 4 at midnight America/New_York**, first attempts take 45–90 active minutes and repeats 1–3 active hours. These are placement targets, not promises that a blocked/unsafe location or unseen apparition counts as a sighting. The encounter adapter's one-minute join/respawn grace precedes eligible active play.

Unseen players take priority, then active time overdue and oldest sighting. Each attempt reserves its player. No more than two attempts per second or 32 live reservations are permitted. Failure or unseen expiry produces a 30-active-second retry, never the successful-sighting cooldown. Reservations expire after 180 simulation seconds as a fallback for a lost callback; current entity lifetimes are much shorter. A successful shared sighting only credits the participants the server actually observed witnessing it. Menus, idle time, sleep, death and offline time do not accumulate scheduler credit. The adapter also checks live witness safety when a callback arrives.

First encounters are distant watchers. Close encounters require two recorded sightings, a calm player and a separate 45-active-minute event cooldown (three hours in quiet mode), plus a one-in-eight selection roll when otherwise eligible. Normal sightings continue during that longer cooldown. Claude's encounter adapter may apply stricter eligibility. Operator-forced encounters deliberately do not mutate natural scheduler progress.

This is atmospheric content, with no required crafting input, progression gate, loot or currency output. It connects to exploration, the seasonal event, Styx and the Cursed Mirror; these narrative connections are owned by the encounter/content implementation.

## Implementation boundary

- `herobrine/HerobrineSeason`: explicit local-date cutoff converted through `ZoneId`; no UTC day or fixed-offset/DST mistake.
- `herobrine/HerobrineSchedule`: dependency-free, server-thread-only scheduling with injected clock input and random generator. One `step` per server simulation second. UUID-based persistent progress; ephemeral reservations and eligible-player snapshots.
- `herobrine/HerobrineProgress`: versioned shared SavedData codec, stored once in the Overworld and marked dirty on changed progress. State survives reconnect/restart. There is no cross-world static progress map. No offline catch-up or repeated first-sighting reset.
- `herobrine/HerobrineScheduleDirector`: implements Claude's `creatures/herobrine/HerobrineDirector`. Installs a fresh world-bound director on `SERVER_STARTED`; resets on stop. `due`, `placed`, `failed`, `seen` and `ended` preserve the reservation/encounter identity. Wrong/stale callbacks do nothing. Outcomes and witness lists cannot grant credit on their own.
- `HerobrineEncounters` owns active/safety detection, bounded loaded-chunk searches, entity lifetime and sustained visible-witness detection. It never treats a successful spawn as proof the target saw him.

Server configuration in `config/jugcraft.properties`:

```properties
herobrine.haunting=on
herobrine.event_last_day=2026-11-03
herobrine.timezone=America/New_York
```

Date/timezone are loaded at server start. Invalid values log a warning and use the defaults. This is an explicit 2026 event window; it does not silently turn back on next October. Existing progress remains stored when haunting is disabled. Calendar transitions rescale the remaining fraction of a pending cooldown to the new pace; already-overdue players remain due.

## Full collaboration plan and acceptance work

Claude received the owner's plan and acknowledged the division of work in the existing session. Its scope includes distant watchers, footsteps, second-look relocation, tunnel eyes, shared sightings and rare close encounters; leafless groves, 2×2 tunnels, shrines, signs, small pyramids and rare explorable brick shoreline pyramids; actual clue chains, modest non-farmable rewards, Mirror/guards/Styx connections. Those are not claimed implemented by this scheduling patch.

Terrain changes must protect claims, towns and unclaimed player work, and require loaded, checked wilderness sites with revalidation and bounded incremental construction. Reversal must only touch unchanged event-owned placements. Non-destructive sightings must remain independent of terrain-site availability. No automatic live-world deployment, regeneration or server restart is included.

## Validation

Run `./gradlew herobrineScheduleTest` for deterministic calendar/DST, active-time, first/repeat pacing, fairness, reservations, visibility callbacks, shared witnesses, failure retries, restart snapshots, strong-scare limits and concurrency checks. This is also a `check` dependency.

Run the filtered `HerobrineScheduleGameTests` for real Minecraft SavedData codec round trips, dirty marking, corrupt-state rejection, cutoff behavior and independent shared-witness progress. These are not rendered two-client tests.

Current evidence and outstanding integration results are recorded below as they are actually run. Player perception/rendering and true two-client witness checks still need the completed encounter branch; a server-side look vector cannot certify exactly what an individual client rendered or consciously noticed. No live server or player profile was changed.

- Standalone Java 25 scheduler tests: **393 assertions passed**, including all 200 simulated active players receiving a first confirmed turn when placement succeeds.
- Common and client Java compilation completed after replacing unsupported `Codec.longRange` with a validated long codec. The tool session interrupted the overall Gradle process before its final result; the complete invocation and server checks are being rerun.
