# Reported black-line rendering artifacts

Owner report, 28 October 2026: several players see black lines, possibly near lights or reflections. The owner subsequently confirmed that all tested shader packs are affected and disabling shaders entirely removes the lines. Investigation by OpenAI Codex alongside the local living-city work. A shared GeckoLib/Iris integration defect has been reproduced and a local fix has passed scoped rendered validation; disappearance of the owner's intermittent streaks still requires confirmation in the affected scene.

## Evidence

- The locked pack includes Sodium 0.9.2, Iris 1.11.7 and LambDynamicLights 4.13.0 for Minecraft 26.3. It bundles no shader pack. Sodium is the terrain renderer; Iris loads selected shader packs ([Sodium installation documentation](https://github.com/CaffeineMC/sodium/wiki/Installation)).
- Read-only inspection of the owner's latest launcher log (28 October) identifies **Bliss_v2.1.2_(Chocapic13_Shaders_edit).zip at 22:12**, switching to **BSL_v10.1.8.zip at 22:19**, RTX 5070 and NVIDIA driver 610.88. The saved Iris configuration selects BSL. Which pack was active at the reported sighting is not confirmed. Other players' shader packs, graphics hardware and reproductions are not known.
- During the initial Bliss load Iris reports compatibility initialization of unassigned shader inputs in deferred, entity-eyes, beacon and other shader programs. These warnings alone do not establish the cause of the visible lines, and must not be attributed to the later BSL load.
- Jugcraft's shared QuadModel renderer supplies UV, light, overlay and normals for each vertex; no missing attribute was established from this inspection. Runtime resources contain no replacement core-shader files. No speculative renderer patch or global shader-setting reset was made.
- Iris has an upstream report about stripes from damaged wolf armor ([issue 3337](https://github.com/IrisShaders/Iris/issues/3337)), but there is no evidence linking the owner's scene to it. Do not treat a similar-looking issue as a confirmed diagnosis.

## Remaining visual confirmation

The owner confirmed that switching shader packs does not remove the lines, while disabling shaders does. Do not ask them to repeat that comparison. The remaining confirmation is the originally affected scene with the local compatibility fix. If streaks remain, capture that scene's nearby blocks/entities and camera position for a targeted reproduction. Do not reset the owner's settings or change dependency pins speculatively.

The local disposable client test can use `build/living-city-bsl.init.gradle` to copy the existing BSL archive into its own temporary game directory. That local-only helper is not distributed. Passing a clean test scene does not prove the intermittent live-world artifact is resolved.

## Related confirmed leaf defect

Aspen's `season=bare` blockstate selected a twig-like texture that the owner described as webs. It now selects the existing green foliage model; gold autumn foliage stays. This is a resource mapping change, independent of the unconfirmed shader artifact.


## Local BSL result

`build/living-city-balance-bsl-combined.log`: both town/commission rendered suites passed with BSL 10.1.8 loaded in the disposable test instance. The inspected square/noticeboard and foliage screenshots did not show the reported black-line streaks. This is not a reproduction of the owner's affected scene and does not close the issue. The test log also reported an illegal default anisotropic-filter value before loading BSL; the owner's inspected log did not report that warning. No evidence connects it to the owner's artifact.

## Concrete shared-pipeline defect and local fix

Further focused inspection of the owner's log found `Missing program geckolib:pipeline/emissive in override list` at 22:15:10, with the stack running through Iris's shadow renderer. This was not visible in the earlier broad warning summary. GeckoLib 5.5.7's `AutoGlowingGeoLayer.EmissiveRenderType` creates a custom `geckolib:pipeline/emissive` pipeline on Windows; on other platforms it returns vanilla eyes. The custom pipeline was missing Iris program associations.

`GeckoGlowClientGameTests` renders three actual Cinderlings with their original GeckoLib geometry and auto-glow masks, day and night in a disposable frozen world. The unpatched BSL baseline reproduced **the same missing-pipeline error** (`build/gecko-iris-baseline.log`, 23:09:28). The scene itself did not show the long black streaks, so this establishes the integration error, not complete visual causality.

`client/compat/GeckoIrisCompat` uses the pinned public Iris API to map that exact pipeline to `EMISSIVE_ENTITIES` and `SHADOW_ENTITIES` during client initialization. A mod-presence guard isolates the optional API. Vanilla/non-Windows pipeline mappings are left alone; existing mappings from another adapter are retained. Registration is idempotent and does not depend on a shader being active at startup, allowing later shader toggles. Original models, masks, animation, blending and depth setup remain GeckoLib-owned. No mixin, dependency upgrade, shader-pack edit or personal settings reset is introduced.

## Post-fix validation — 28 October

- BSL 10.1.8: `build/gecko-iris-fixed-bsl.log`, BUILD SUCCESSFUL at 23:13:49. The log confirms the actual shader pack and both pipeline associations. The baseline's missing-program error is absent.
- Bliss 2.1.2: `build/gecko-iris-fixed-bliss.log`, BUILD SUCCESSFUL at 23:16:30. The same day/night gallery passed with the actual Bliss shader pack active; the missing-program error is absent.
- Optional integrations disabled, including Iris and Sodium: `build/gecko-glow-no-iris.log`, BUILD SUCCESSFUL at 23:18:31. The same gallery passed; the Iris adapter did not load. This validates the optional-class absence path as well as ordinary unshaded rendering.
- Day/night screenshots are preserved under `build/rendering-lines-bsl-fixed/`, `build/rendering-lines-bliss-fixed/` and `build/rendering-lines-no-iris/`. Representative shader screenshots were visually inspected and retain the intended glow without visible streaks. The unpatched baseline did not visually reproduce the reported streaks either; the confirmed before/after result is removal of the missing-pipeline error, not proof that every live artifact is cured.
- The local JAR includes the new adapter and client entrypoint, with aspen/shop resources matching source. SHA-256: `700c56b439c85545f3c283af4f4d9e02fe1071697bcb53e24e069480f313513d` (`build/living-city-rendering-jar-final.log`).
- Non-fatal Windows performance-counter and anisotropic-option diagnostics remain in the disposable runs; no claim is made that all unrelated warnings are resolved.

Work remains local. No release, personal profile, updater or live-server changes. Owner confirmation in the previously affected scene remains outstanding.
