# Tooltip item preview crash — November 1, 2026

Bug fix by OpenAI Codex (GPT-6), targeting the existing Minecraft 26.3 / Fabric
Loader 0.19.5 platform and locked framework versions.

## Cause and behavior

Hovering a damageable item in a merchant screen or inventory uses
`OwnerTooltipFrames` to extract a rotating dropped-item preview. Minecraft 26.3
requires an explicit entity ID before `ItemModelResolver.updateForNonLiving`
reads it. The temporary `ItemEntity` had never received an ID, causing
`IllegalStateException: Tried to access entity ID before ID assignment`.

The preview now receives the stable nonzero display-only ID `-1` before render extraction.
It remains outside the level, uses a single-item copy, and keeps the native
item model, tooltip text, rotation and reduced-motion behavior. This client
presentation fix changes no trade prices, inventories, progression or saved data.

## Validation

`OwnerTooltipClientGameTests` exercises the deferred tooltip callback for a
damaged pickaxe, a bow and an emerald, checks that the original stacks retain
their count and damage, then hovers a pickaxe in the inventory and a real
wandering trader's offer and emerald cost in a disposable singleplayer world.
The trader has one controlled test offer sent through the native merchant menu.
The test is registered in the existing client game-test suite.

Validated November 1 against base commit `194d7d6485` plus this local fix, with
Java 25.0.4.1, Minecraft 26.3, Fabric API 0.161.0+26.3 and the locked framework
set (including the optional client integrations).

- **PASS:** `python scripts/check_repository.py` and `git diff --check`.
- **PASS:** client and game-test Java compilation.
- **PASS:** `gradlew.bat runClientGameTest -PclientTests=OwnerTooltipClientGameTests
  -I build/ci-focus.init.gradle -x processResources --offline --no-daemon
  --max-workers=2` in **3m 57s**. Evidence: `build/tooltip-verified.log`, including
  `[tooltip regression] Deferred previews, inventory and wandering trader tooltips passed`.
  The test asserts that damageable items produce a nonempty native item preview
  and an emerald does not, in addition to checking the original stack contents.
- Trader screenshots inspected: the pickaxe preview and ordinary emerald
  tooltip render. Captures are under `build/run/clientGameTest/screenshots/`.
  The hotbar tooltip capture reaches the bottom of the small test window;
  this fix does not change tooltip placement.
- The first normal resource preparation completed copying and generating its
  metadata but was stopped during slow output cataloging. The focused run reused
  those files; a separate coverage check confirmed every source resource, expanded
  mod metadata and the generated updater helper were present
  (`build/tooltip-prepared-resources.log`). The existing local compiler init file
  excludes resource files from the Java type classpath only. Runtime resources and
  assertions remain enabled. An earlier test-only merchant fixture was corrected
  to edit the server's actual offer list; its empty `overrideOffers` method is
  intended for client implementations and does not replace server offers.

The full server sweep, two independent clients and a clean launcher import were
not run for this client-only fix. No personal launcher profile, live server or
existing world was changed.

## Packaged local client build

**PASS:** `gradlew.bat jar -I build/ci-focus.init.gradle
-I build/tooltip-package.init.gradle -x processResources --offline --no-daemon
--max-workers=2` in **4m 1s** (`build/tooltip-jar.log`). The temporary packaging init
file disables repeat task-state cataloging for this one archive; checked-in build
tasks and CI checks are unchanged.

The delivered `tooltip-crash-fix-2026-11-01/jugcraft-0.1.0-alpha-tooltip-fix.jar`
is 344,222,934 bytes. Its three tooltip renderer classes match the compiled,
game-tested classes byte-for-byte; generated mod metadata and the declared nested
library are present, and the new test class is excluded. The output folder also
contains installation instructions and `SHA256SUMS.txt`.

SHA-256: `6fad6a85ac3ca64e51ca860796397c3d0d6c9c4947a669c9105075b56d7b524e`.
This is a local build, not a published release or an installed client update.
