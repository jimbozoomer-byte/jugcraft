# Pharmacy Art Batch 1 — everyday and medical items

Implementation tool: **OpenAI Codex (GPT-6 family)**. Owner-directed local implementation, 3 November 2026. Minecraft 26.3, Java 25, Loader 0.19.5, Fabric API 0.161.0+26.3 and the pinned framework/toolchain versions remain unchanged.

## Delivery

This is the historical Art Batch 1 record. [Art Batches 2–4](pharmacology-item-art-234.md) continue its canonical map/model writer with 97 additional textures.

The [selected art batch](../plans/pharmacology-item-art/batches.md#art-batch-1--everyday-and-medical-drugs) delivers 17 distinct finished items: Caffeine Drink, Aspirin, Paracetamol, Rehydration Salts, Activated Charcoal, Stimulant, Penicillin, Lidocaine, Adrenaline, Salbutamol, Morphine, Naloxone, Methylphenidate, Amphetamine, Modafinil, Ketamine and Fentanyl. Later art batches and vision-effects polish are separate.

Every item has an editable `pharmacy_<id>.txt` source in the existing item-map system, named material ramps and a dedicated `textures/item/pharmacy/<id>.png`. The shared renderer uses 16×16 flat-tone pixels and the owner's outline/light/whole-object rules. No existing generic Aspirin/Stimulant texture is overwritten: later drug aliases, materials and effect icons keep their earlier delivery until their own batches.

The actual owner syringe and empty-vial source images were inspected alongside the supplied library/catalog and icon rules. Their paths are `art/owner-library/originals/Blocks/Guns/item/syringe.png` and `art/owner-library/originals/Magic/assets/jymbaquary/textures/item/empty_potion_vial.png`. They serve as form/style references; the delivered maps are newly authored, with no copied library pixels or executed library programs. Originals remain unchanged. Existing editable icon maps, renderer and lint are reused instead of introducing another art system or dependency.

`tools/pharmacy_item_art.py` owns deterministic texture rendering, 17 native item models/selectors, small-item held scale and three conditional using-item models. It is integrated with the pharmacy writer and the final material-data/texture generation stages so old aliases cannot overwrite these finished items. The inventory sprites stay flat; the devices change first-person pose through the native `using_item` model condition without a rendering mixin. Both hand transforms are supplied.

## Packaging and actual use

Rehydration Salts is now a paper sachet. Its existing shaped craft spends two paper instead of two glass bottles, still yields two doses and returns its water bucket once. The Press batch spends eight paper rather than eight bottles for eight doses. Stimulant is a tablet; its existing compact reactor uses one Pharmaceutical Starch instead of a glass bottle per dose, and its Press recipe uses four starch instead of four bottles for four doses. Starch retains the existing reachable potato/paper craft and powered alternatives. These are fictional Minecraft recipes.

Both items keep their former **1.6-second use time**, with an eating/packet pose and no glass return. Product counts, processing times, energy, fluid costs, effects, onset, duration, strain and tolerance remain unchanged. The packaging input changes are explicit; no bottle is paid and silently lost by either new form, and neither creates free glass.

Adrenaline's injector, Salbutamol's inhaler and Naloxone's spray retain their existing sterile stock/carrier/vial inputs. The sterile vial is the internal cartridge; use returns one ordinary empty glass container. They keep 1.6 seconds, their existing self-use authority, cooldowns and effects. Native `NONE` consumption animation plus a distinct using-item model produces a stable activation pose rather than bottle-drinking motion; the existing leather equipment sound supplies a restrained activation click. No new targeting action, injection hit, injury condition or clinical simulation is added.

The other finished medicines retain their existing dose/use/container behavior. No saved item ID, attachment, profile ledger, recipe ID, machine slot, network message or dependency changes. Raw stock/active production, analysis/formula locks and precision tooling remain the same. Canonical handbook/tooltips describe the revised sachets/tablets and device cartridges.

## Validation

Verified locally on branch `codex/pharmacology-caffeine`, base `f02bccb606c17a4305d5355a6ab6f75736da6c27`, with the existing owner changes preserved. The production Java compile passed. Canonical writers generated a bounded overlay of **512 resources**, byte-checked against the source and runtime copies; this includes the finished textures, models, selectors, recipes, handbook and language changes. The full catalog generator/data audit was not rerun.

**All 92 required native pharmacy server tests passed.** This includes the new packet/tablet use and zero-glass checks, the three devices' exact one-glass return, native paper-sachet crafting, the Stimulant binder input and the existing formed-machine recipe checks. An initial run found one old Stimulant assertion expecting its former bottle return; it was updated to the intentional tablet behavior, preserving its effect/strain assertions, before the passing rerun.

`PharmacologyItemArtClientGameTests` and `PharmacologyFocusClientGameTests` both passed in the combined run (`BUILD SUCCESSFUL in 13m 27s`). The art test renders all 17 items in native inventory/held views, actually uses the two packet/tablet forms and three devices, verifies dose/container counts and performs a native resource reload. The existing Focus suite checks its gameplay and visual behavior. This produced 59 preserved screenshots.

The final visual review then separated Modafinil's silhouette further from Paracetamol: Modafinil is a horizontal rounded rectangle with a sage end. Only that texture changed. The final art-only client rerun also added GUI-scale-1 inventory coverage and **passed in 6m 11s** at 23:29 local time on 3 November, producing **25 final screenshots**. Reviewed final native views include inventory at scales 1 and 2, Modafinil held, all three device-use poses and the post-reload appearance. The earlier 92-test gameplay and Focus results remain applicable; no gameplay changed after them.

All 17 editable maps pass the shared icon check with **zero errors**. Six qualitative small-material warnings were reviewed for intentionally tiny corks, caps, bands and labels; no lint exemption was added. All sprites are distinct, 16×16 RGBA, and regenerated pixels match the delivered PNGs. Labeled enlarged previews, individual previews, light/dark slots at 1×/2×/8× and greyscale were reviewed. Common color-vision simulation, human audio review, shaderpack combinations and two-human-client testing were not performed.

Commands used through `build-local.ps1` retained the pinned Java 25/Minecraft 26.3 toolchain and used the existing bounded resource overlay rather than regenerating the whole catalog:

```text
compileClientJava
runGameTest runClientGameTest -PpharmacyTestFilter=jugcraft-test:pharmacology* -PclientTests=PharmacologyItemArtClientGameTests,PharmacologyFocusClientGameTests -I ../early-pharmacy-focused.gradle -x processResources -x compileClientKotlin --offline --max-workers=2
runClientGameTest -PclientTests=PharmacologyItemArtClientGameTests -I ../early-pharmacy-focused.gradle -I ../force-pharmacy-art-capture.gradle -x processResources -x compileClientKotlin --offline --max-workers=2
```

The last temporary init script forces compilation of the updated client capture test. Neither helper is a production dependency. Repository structure/local Markdown-link checks and scoped whitespace checks passed.

Evidence is outside Git at `../pharmacy-art1-2026-11-03/`: `evidence-first/screenshots` contains the first 59 native captures, `evidence-final/screenshots` contains the final 25, and `evidence-final` holds the test/build/resource logs and manifests. `all-17-textures.png`, `previews/<id>.png`, `textures/<id>.png` and `17-pharmacy-textures.zip` show/export the exact finished source sprites. Source/evidence SHA-256 indexes identify this local delivery.

No release JAR was published and no live server or existing Pharmacy Flat Test world was stopped, replaced or restarted. A running playtest client needs a restart to pick up the common-code packaging changes. Art Batches 2–6, original Clockwork Batch 7 and later vision-effects polish remain pending.
