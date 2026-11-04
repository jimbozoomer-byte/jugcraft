# Drug display controls and alcohol vision

[Improvement Batch C](pharmacology-calm.md) adds calm/remote-view profiles and expands the same native uniform to 80 bytes.

The shared filter now also supports [Batch B focus, work and strain warnings](pharmacology-focus.md). The implementation below records the original alcohol delivery; B expands the uniform to 64 bytes while preserving the same settings and native chain.

Implementation tool: **OpenAI Codex (GPT-6 family)**. Owner-directed first visual batch for the existing local pharmacy. Minecraft 26.3, Fabric Loader 0.19.5 and Fabric API 0.161.0+26.3 remain pinned. Local implementation only.

## Player outcome

The five alcohol grades now request a native world post-effect while the local player has synchronized Intoxication. Slow peripheral sway, softened edges, symmetric doubled outlines, restrained warmth and a vignette distinguish stronger drinks. The center remains spatially sharp. Neither the camera's aim nor movement input is changed by this presentation. Existing server-side movement, firearm spread, melee and knockback rules remain authoritative.

| Formulation | Relative peak strength before local settings |
| --- | --- |
| Moonshine | 22% |
| Refined Spirits | 38% |
| Reserve Whiskey | 55% |
| High-Proof Spirits | 75% |
| Masterwork Reserve | 100% |

The default overall setting is 75%. Visuals begin only after the existing dose onset: five seconds rising, a peak through the first forty active seconds, then twenty seconds settling to 45% of the grade's peak. The final five seconds fade down. Removing Intoxication eases the remaining presentation away within two seconds; death, spectator mode, player replacement and disconnect clear it immediately. The envelope pauses with singleplayer. Joining with an active dose starts a fresh local presentation envelope without changing the dose. A redose with a renewed duration restarts that envelope.

The native post-effect runs before the GUI. Chat, tooltips, HUD and crosshair are rendered afterward. Opening any screen suspends this post-effect, including the world background, and closing it resumes the current dose presentation. There are no flashing overlays, forced camera turns, blackouts, extra entities, world scans or previous-frame trails.

## Controls and existing infrastructure

The existing required Cloth Config screen gains a **Drug visuals** category. Open **Jugcraft display settings** through Mod Menu or bind that action in Controls (its stable key ID remains `concordance_config`). Local choices use the existing `config/jugcraft-client.properties` file:

- Enable drug visuals, default on.
- Overall intensity, default 75%; zero turns the presentation off.
- Edge blur, default 70%.
- Visual sway, default 60%.
- Double vision, default 70%.

All percentages are bounded to 0–100, and malformed saved percentages fall back to their defaults. The first tab's shared **Reduced motion** switch sets sway to zero; the remaining color, blur and doubled outlines are static. Minecraft's **Distortion Effects** scale also limits the overall strength, including zero. No choice is sent to the server or changes benefits, strain, tolerance, compatibility, recovery, movement penalties or projectile spread. There is no extra configuration backend, drug ledger or network protocol.

`LocalPlayer.getActivePostEffects()` composes this request with existing effects. One native `PostChain` uses a single world target and two passes. Jugcraft's own named 32-byte uniform uses Minecraft's `MappableRingBuffer`; each pass owns and closes its buffers through native shader reload/close. Renderer work reads an extracted immutable presentation snapshot, not gameplay entities. The shader uses bounded texture samples and requires no Iris shader pack. Existing optional renderer integrations remain optional.

## Resources and scope

The owner library and catalog were consulted. This batch needs procedural sampling of the actual world rather than a bitmap overlay; no source art is replaced or copied. The original Jugcraft shader is `assets/jugcraft/shaders/post/drug_alcohol.fsh`. `tools/drug_visuals.py`, called by the existing pharmacy generator, owns the native post-effect JSON and display labels. Settings labels retain their existing canonical Concordance writer.

Only alcohol gains a new filter in this batch. The decaying-strain proposal, new drug benefits, other drugs' visual profiles and Clockwork dimension remain separate planned work. Existing saves and registrations are unchanged.

## Validation

Client compilation and the rendered `PharmacologyVisualClientGameTests` passed with the installed pinned Sodium/Iris/Fusion stack (no shader pack selected). The test consumed all five real formulations and checked their onset and increasing visual strength, native post-chain application, unchanged camera aim, Reduced Motion, the visual Off switch preserving movement penalties and 60 strain, Minecraft's Distortion Effects zero setting, native inventory and Cloth Config screens, the lighter sustained phase, complete shader resource reload and milk cleanup. The run produced **36 native screenshots**, including 24 successive motion frames. GIF timing comes from the captured game-tick sequence; PNGs are preserved unchanged.

Canonical post-effect JSON, local labels, existing key help and the runtime shader were checked against their writers/source. Repository structure/documentation checks and scoped whitespace checks passed. New resources and the shader were copied and byte-verified against the local test runtime. No recipe, machine, attachment or gameplay save schema changed.

Rendering without Sodium/Iris remains **unverified**. A broader `-PjugcraftOptionalIntegrations=false` attempt timed out at initial chunk rendering before reaching any alcohol checks, alongside existing model errors with all optional integrations removed. Focused runtime exclusions of Sodium/Iris also failed before the world opened: existing render classes referenced missing `net.irisshaders.iris.mixinterface.RenderTargetInterface` and `RenderTypeInterface`. Omitting GuiLib did not resolve that startup failure. These attempts are not counted as passes. The temporary exclusion harness was removed; production dependencies, platform pins, launcher settings and caches were not modified to bypass the issue.

Reproduction on the pinned local toolchain:

```powershell
.\build-local.ps1 -Tasks @('runClientGameTest', '-PclientTests=PharmacologyVisualClientGameTests')
```

Local captures, full logs and SHA-256 manifests are preserved outside Git under `../pharmacy-visuals-2026-11-03/`. These are automated local singleplayer results. No live-server deployment, human multiplayer test, external shader-pack compatibility or load benchmark is claimed.
