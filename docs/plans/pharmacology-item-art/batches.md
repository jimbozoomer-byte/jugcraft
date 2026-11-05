# Pharmacy item art — delivery batches

Status: **Art Batches 1–6 implemented and verified locally.** Prepared with **OpenAI Codex (GPT-6 family)**, 3–4 November 2026, at the owner's request. This six-batch delivery split supersedes the earlier four-batch sketch in the [art plan](README.md). [Batches 2–4 delivery](../../features/pharmacology-item-art-234.md), [Batch 5 formula-plate delivery and checks](../../features/pharmacology-item-art-5.md), [Batch 6 HUD/icons/model review](../../features/pharmacology-item-art-6.md).

## Order and scope

Use **Art Batch 1–6** when referring to these tasks. These numbers are separate from original manufacturing Batch 7 and the completed visual improvement Batches A–E. Vision-effects improvements remain deferred. Art Batches 1–6 are implemented locally; the existing test world was not replaced or restarted.

| Art batch | Delivery | Assigned size |
| --- | --- | --- |
| **1 — Everyday and medical drugs — locally complete** | Shared pixel-art foundation and the complete early/sterile/precision finished-drug set; [delivery and checks](../../features/pharmacology-item-art-1.md) | **17 finished items** |
| **2 — Advanced drugs and alcohol — implemented locally** | XR, patch, botanical/psychedelic doses and all five alcohol grades | **15 finished items** |
| **3 — Ingredients, packaging and study supplies — implemented locally** | Cultures, binders, empty packaging, matrices, tools, crops/seeds, jugs and study materials | **32 items** |
| **4 — Processing intermediates and fluids — implemented locally** | Blends, stocks, verified actives, coated cores, concentrates and fluid buckets | **50 items** |
| **5 — Formula plates — implemented locally** | Every named formula and the blank plate | **32 items: 31 formulas + blank** |
| **6 — HUD, models and final consistency — implemented locally** | Every effect icon, plinth/eight machine inventory views and cross-catalog review | **39 HUD icons + 9 model checks** |

The [assignment manifest](batch-assignments.csv) assigns all **147 item-matrix entries, 39 effect icons and eight additional machine-view checks exactly once**: 194 review entries. The plinth is already one of the 147 items and is reviewed as a block model, not drawn again as a flat sprite. `form:` keys identify existing machine forms for this review; they are not proposed item registrations. Shared vanilla/feedstock art is reviewed for consistency without duplicating those items into the pharmacy catalog.

## Shared delivery rules

- Work through batches in order, carrying forward the established palettes, glyphs and template shapes. Finish each batch's listed assets, canonical model mappings and required integration before starting the next.
- Use the owner's 16×16 rules and suitable supplied assets. Keep silhouettes readable at actual hotbar size; represent each finished formulation with the approved physical form and a second identifying detail.
- Preview light/dark slots, stack overlays, actual-size icons and greyscale. Capture the real inventory/hotbar and held appearance. A large image sheet alone does not finish a batch.
- Each batch includes its own relevant checks. Texture-only work needs resource/icon validation and native visual inspection; recipe/container/use-presentation changes also need targeted acquisition, consumption, refund/return and inventory checks. Batch 6 provides the final combined review.
- Preserve drug effects, timing, strain, yields, stable IDs, saved formats and self-use authority. Presentation alignment belongs in the batch that introduces the new form. Do not defer a visibly incorrect returned container or use pose to the end.
- Batches 1 and 2 may temporarily retain the existing processing-material/formula/HUD art until those assigned batches land. Each finished dose delivered must already be complete and usable.


## Art Batch 1 — Everyday and medical drugs

**Implemented and verified locally:** all 17 finished sprites, native model mappings, packaging/use alignment, 92 passing pharmacy server tests and rendered client checks. See [the delivery record](../../features/pharmacology-item-art-1.md) for recipes, evidence and limits.

Build the shared palette ramps, icon-map/model ownership and preview workflow while completing the 17 finished drugs. Prototype representative round/oblong/diamond tablets, capsule proportions, powder packet, vial/ampoule, injector, inhaler, spray and coffee bottle within this batch before applying their families.

Align Rehydration Salts' sachet, Stimulant's tablet and Adrenaline/Salbutamol/Naloxone devices with current packaging, timed use and returned-container behavior. Preserve each existing duration and manufacturing yield; test paid packaging and reachable production through the existing slots. No injury-management mechanic or new delivery-to-other-players action is implied.

**Done when:** all 17 have distinct finished sprites in their canonical models; the actual hotbar/held forms are readable; use consumes exactly one authorized dose and has coherent packaging/returns; relevant native checks and reviewed captures pass.

| Assigned item | ID |
| --- | --- |
| Caffeine Drink | `jugcraft:caffeine_drink` |
| Paracetamol | `jugcraft:paracetamol` |
| Rehydration Salts | `jugcraft:rehydration_salts` |
| Activated Charcoal | `jugcraft:activated_charcoal` |
| Penicillin | `jugcraft:penicillin` |
| Lidocaine | `jugcraft:lidocaine` |
| Adrenaline | `jugcraft:adrenaline` |
| Salbutamol (Albuterol) | `jugcraft:salbutamol` |
| Morphine | `jugcraft:morphine` |
| Naloxone | `jugcraft:naloxone` |
| Methylphenidate | `jugcraft:methylphenidate` |
| Amphetamine | `jugcraft:amphetamine` |
| Modafinil | `jugcraft:modafinil` |
| Ketamine | `jugcraft:ketamine` |
| Fentanyl | `jugcraft:fentanyl` |
| Aspirin | `jugcraft:aspirin` |
| Stimulant | `jugcraft:stimulant` |

## Art Batch 2 — Advanced drugs and alcohol

**Implemented locally:** all 15 sprites, family-related XR forms, five distinct alcohol vessels, native timed use/returns and the paper-packaged LSD recipe. See [the delivery record](../../features/pharmacology-item-art-234.md).

Complete the other 15 finished products using Batch 1's material treatment. Keep the four XR versions related to their base drug through palette/glyph, with the longer capsule, double collar and bead detail. Make the patch, five botanical/psychedelic doses and five alcohol silhouettes recognizable as their own products.

Align LSD's proposed paper dose with the existing capsule-shell formulation/use presentation during this batch. THC, Psilocybin, Mescaline and DMT keep their planned capsule/softgel identities; finished doses remain distinguishable from loose harvested crops and processing crystals.

**Done when:** all 32 finished formulations can be compared together in native inventory/hotbar views; base/XR and all five alcohol grades remain distinct; real use, paid packaging and returns match the delivered appearance. This adds item art, not another vision-effects pass.

| Assigned item | ID |
| --- | --- |
| Alcohol (Moonshine) | `jugcraft:moonshine` |
| Alcohol (Refined Spirits) | `jugcraft:refined_spirits` |
| Alcohol (Reserve Whiskey) | `jugcraft:reserve_whiskey` |
| Alcohol (High-Proof Spirits) | `jugcraft:high_proof_spirits` |
| Fentanyl Patch | `jugcraft:fentanyl_patch` |
| Alcohol (Masterwork Reserve) | `jugcraft:masterwork_reserve` |
| Caffeine XR | `jugcraft:caffeine_xr` |
| Methylphenidate XR | `jugcraft:methylphenidate_xr` |
| Amphetamine XR | `jugcraft:amphetamine_xr` |
| Modafinil XR | `jugcraft:modafinil_xr` |
| THC | `jugcraft:thc` |
| Psilocybin | `jugcraft:psilocybin` |
| Mescaline | `jugcraft:mescaline` |
| LSD | `jugcraft:lsd` |
| DMT | `jugcraft:dmt` |

## Art Batch 3 — Ingredients, packaging and study supplies

**Implemented locally:** all 32 item forms, including distinct empty shells, three jug states, supply packets, natural harvests/seeds and study papers. See [the delivery record](../../features/pharmacology-item-art-234.md).

Complete the raw materials, carrier/binder/culture items, empty shells, release matrices, patch backing, jugs, tools and study supplies listed below. Match seeds/harvests to their existing plants and natural-texture rules. Preserve the existing Coffee Plant/Bean supply chain and shared feedstock identities.

Study Canvas, Lucid/Pattern/Oneiric Imprints and the Folio get their own sheet/book silhouettes and meaningful symbols. The plinth's existing 3D block-item view is assigned to Batch 6.

**Done when:** all 32 entries have clear individual identities; empty packaging cannot be mistaken for a usable dose; all three mash-jug states, matrix tiers and study papers can be distinguished in native slots. No new crop, machine or chemistry backend is added.

| Assigned item | ID |
| --- | --- |
| Pharmaceutical Starch | `jugcraft:pharmaceutical_starch` |
| Prepared Pharmaceutical Base | `jugcraft:pharmaceutical_base` |
| Sterile Vial | `jugcraft:sterile_vial` |
| Sterile Carrier | `jugcraft:sterile_carrier` |
| Azure Culture Starter | `jugcraft:azure_culture_starter` |
| Prepared Culture Feed | `jugcraft:culture_feed` |
| Precision Process Module | `jugcraft:precision_process_module` |
| Empty Mash Jug | `jugcraft:empty_mash_jug` |
| Mash Jug | `jugcraft:mash_jug` |
| Fermented Mash Jug | `jugcraft:fermented_mash_jug` |
| Beverage Culture Pack | `jugcraft:beverage_culture_pack` |
| Oak Conditioning Insert | `jugcraft:oak_conditioning_insert` |
| Release Control Die | `jugcraft:release_control_die` |
| Release Matrix Blend | `jugcraft:release_matrix_blend` |
| Release-control Matrix | `jugcraft:release_matrix` |
| Advanced Release-control Matrix | `jugcraft:advanced_release_matrix` |
| Empty Pharmacy Capsule | `jugcraft:empty_pharmacy_capsule` |
| Pharmacy Patch Backing | `jugcraft:pharmacy_patch_backing` |
| Masterwork Conditioning Insert | `jugcraft:masterwork_conditioning_insert` |
| Botanical Resin | `jugcraft:botanical_resin` |
| Dreamcap | `jugcraft:dreamcap` |
| Prism Cactus Fruit | `jugcraft:prism_cactus_fruit` |
| Mycelium Catalyst | `jugcraft:mycelium_catalyst` |
| Botanical Capsule Shell | `jugcraft:botanical_capsule_shell` |
| Dream Study Canvas | `jugcraft:study_canvas` |
| Lucid Imprint | `jugcraft:lucid_imprint` |
| Pattern Imprint | `jugcraft:pattern_imprint` |
| Oneiric Imprint | `jugcraft:oneiric_imprint` |
| Dream Folio | `jugcraft:dream_folio` |
| Hemp Seeds | `jugcraft:hemp_seeds` |
| Dreamcap Spores | `jugcraft:dreamcap_seeds` |
| Prism Cactus Seeds | `jugcraft:prism_cactus_seeds` |

## Art Batch 4 — Processing intermediates and fluids

**Implemented locally:** all 50 sprites/models, family glyphs across distinct stages, separate Masterwork states and three whole process buckets. See [the delivery record](../../features/pharmacology-item-art-234.md).

Apply the established family palettes/glyphs to every processing stage below. Separate blended pouches, unverified bulk jars, verified-active weighing boats, exposed XR cores and beverage concentrates by silhouette. Cover the special older sterile stocks and advanced preparation materials as well as the regular suffix-based families.

The three existing fluid buckets retain their standard bucket form and gain coherent liquid/process identities. They remain machine ingredients. Show the complete precursor → stock → active → finished route in actual recipe/machine views where applicable; the final products from Batches 1–2 are the reference.

**Done when:** all 50 entries are mapped, no stock/active pair still aliases an identical generic sprite, unconditioned/conditioned Masterwork materials differ visibly, and the rendered production chains are readable without relying on tiny text.

| Assigned item | ID |
| --- | --- |
| Caffeine Concentrate | `jugcraft:caffeine_concentrate` |
| Antibiotic Extract | `jugcraft:antibiotic_extract` |
| Lidocaine Game Stock | `jugcraft:lidocaine_stock` |
| Adrenaline Game Stock | `jugcraft:adrenaline_stock` |
| Salbutamol Game Stock | `jugcraft:salbutamol_stock` |
| Redleaf Medical Stock | `jugcraft:redleaf_medical_stock` |
| Naloxone Game Stock | `jugcraft:naloxone_stock` |
| Methylphenidate Game Reagent Blend | `jugcraft:methylphenidate_reagent_blend` |
| Methylphenidate Game Stock | `jugcraft:methylphenidate_stock` |
| Verified Methylphenidate Active | `jugcraft:methylphenidate_active` |
| Amphetamine Game Reagent Blend | `jugcraft:amphetamine_reagent_blend` |
| Amphetamine Game Stock | `jugcraft:amphetamine_stock` |
| Verified Amphetamine Active | `jugcraft:amphetamine_active` |
| Modafinil Game Reagent Blend | `jugcraft:modafinil_reagent_blend` |
| Modafinil Game Stock | `jugcraft:modafinil_stock` |
| Verified Modafinil Active | `jugcraft:modafinil_active` |
| Ketamine Game Reagent Blend | `jugcraft:ketamine_reagent_blend` |
| Ketamine Game Stock | `jugcraft:ketamine_stock` |
| Verified Ketamine Active | `jugcraft:ketamine_active` |
| Fentanyl Game Reagent Blend | `jugcraft:fentanyl_reagent_blend` |
| Fentanyl Game Stock | `jugcraft:fentanyl_stock` |
| Verified Fentanyl Active | `jugcraft:fentanyl_active` |
| Refined Spirits Beverage Concentrate | `jugcraft:refined_spirits_concentrate` |
| Reserve Whiskey Beverage Concentrate | `jugcraft:reserve_whiskey_concentrate` |
| High-Proof Spirits Beverage Concentrate | `jugcraft:high_proof_spirits_concentrate` |
| Unverified Fentanyl Patch Stock | `jugcraft:fentanyl_patch_stock` |
| Masterwork Beverage Blend | `jugcraft:masterwork_blend` |
| Unconditioned Masterwork Concentrate | `jugcraft:masterwork_unconditioned_concentrate` |
| Masterwork Reserve Concentrate | `jugcraft:masterwork_reserve_concentrate` |
| Caffeine XR Coated Core | `jugcraft:caffeine_xr_core` |
| Methylphenidate XR Coated Core | `jugcraft:methylphenidate_xr_core` |
| Amphetamine XR Coated Core | `jugcraft:amphetamine_xr_core` |
| Modafinil XR Coated Core | `jugcraft:modafinil_xr_core` |
| Dream Concentrate | `jugcraft:dream_concentrate` |
| Prismatic Concentrate | `jugcraft:prismatic_concentrate` |
| Perception Substrate | `jugcraft:perception_substrate` |
| Oneiric Stock | `jugcraft:oneiric_stock` |
| THC Game Stock | `jugcraft:thc_stock` |
| THC Verified Active | `jugcraft:thc_active` |
| Psilocybin Game Stock | `jugcraft:psilocybin_stock` |
| Psilocybin Verified Active | `jugcraft:psilocybin_active` |
| Mescaline Game Stock | `jugcraft:mescaline_stock` |
| Mescaline Verified Active | `jugcraft:mescaline_active` |
| LSD Game Stock | `jugcraft:lsd_stock` |
| LSD Verified Active | `jugcraft:lsd_active` |
| DMT Game Stock | `jugcraft:dmt_stock` |
| DMT Verified Active | `jugcraft:dmt_active` |
| Organic Process Stock Bucket | `jugcraft:organic_process_stock_bucket` |
| Beverage Wash Bucket | `jugcraft:beverage_wash_bucket` |
| Refined Beverage Stock Bucket | `jugcraft:refined_beverage_stock_bucket` |

## Art Batch 5 — Formula plates

**Implemented locally:** all 32 notched cards, family glyphs/bands, a neutral blank, double-border XR marks and a distinct patch window. The [delivery record](../../features/pharmacology-item-art-5.md) documents native inventory, held and machine/recipe/tool views and unchanged gameplay.

Create a consistent notched slate-card template, then complete the blank plate and all 31 named formulas. Use each finished drug's family glyph/palette. The blank has an empty diagram; base formulas have one upper border; XR has a double border and bead/hourglass corner; the patch uses its patch-window mark.

Keep the current IDs, including `caffeine_formula_plate`, and existing production/progression. Reuse the family design established in Batches 1–2 rather than inventing 31 unrelated logos.

**Done when:** all 32 cards are identifiable in a mixed native inventory and the Analyzer/Press/Filler/Coater recipe/tool views point to the right art. Base/XR and ordinary/patch formulas remain distinct. No unreadable miniature drug name is the only cue.

| Assigned item | ID |
| --- | --- |
| Blank Formula Plate | `jugcraft:blank_formula_plate` |
| Caffeine Formula Plate | `jugcraft:caffeine_formula_plate` |
| Aspirin Formula Plate | `jugcraft:aspirin_formula_plate` |
| Paracetamol Formula Plate | `jugcraft:paracetamol_formula_plate` |
| Rehydration Salts Formula Plate | `jugcraft:rehydration_salts_formula_plate` |
| Activated Charcoal Formula Plate | `jugcraft:activated_charcoal_formula_plate` |
| Stimulant Formula Plate | `jugcraft:stimulant_formula_plate` |
| Penicillin Formula Plate | `jugcraft:penicillin_formula_plate` |
| Lidocaine Formula Plate | `jugcraft:lidocaine_formula_plate` |
| Adrenaline Formula Plate | `jugcraft:adrenaline_formula_plate` |
| Salbutamol (Albuterol) Formula Plate | `jugcraft:salbutamol_formula_plate` |
| Morphine Formula Plate | `jugcraft:morphine_formula_plate` |
| Naloxone Formula Plate | `jugcraft:naloxone_formula_plate` |
| Methylphenidate Formula Plate | `jugcraft:methylphenidate_formula_plate` |
| Amphetamine Formula Plate | `jugcraft:amphetamine_formula_plate` |
| Modafinil Formula Plate | `jugcraft:modafinil_formula_plate` |
| Ketamine Formula Plate | `jugcraft:ketamine_formula_plate` |
| Fentanyl Formula Plate | `jugcraft:fentanyl_formula_plate` |
| Alcohol (Refined Spirits) Formula Plate | `jugcraft:refined_spirits_formula_plate` |
| Alcohol (Reserve Whiskey) Formula Plate | `jugcraft:reserve_whiskey_formula_plate` |
| Alcohol (High-Proof Spirits) Formula Plate | `jugcraft:high_proof_spirits_formula_plate` |
| Fentanyl Patch Formula Plate | `jugcraft:fentanyl_patch_formula_plate` |
| Masterwork Reserve Formula Plate | `jugcraft:masterwork_reserve_formula_plate` |
| Caffeine XR Formula Plate | `jugcraft:caffeine_xr_formula_plate` |
| Methylphenidate XR Formula Plate | `jugcraft:methylphenidate_xr_formula_plate` |
| Amphetamine XR Formula Plate | `jugcraft:amphetamine_xr_formula_plate` |
| Modafinil XR Formula Plate | `jugcraft:modafinil_xr_formula_plate` |
| THC Formula Plate | `jugcraft:thc_formula_plate` |
| Psilocybin Formula Plate | `jugcraft:psilocybin_formula_plate` |
| Mescaline Formula Plate | `jugcraft:mescaline_formula_plate` |
| LSD Formula Plate | `jugcraft:lsd_formula_plate` |
| DMT Formula Plate | `jugcraft:dmt_formula_plate` |

## Art Batch 6 — HUD, models and final consistency

**Implemented and verified locally:** all 39 effect icons; the owner's ten horizontal strain pills above the hunger bar, layered fills/forecasts and three-second zero grace; nine retained whole models; and the final catalog review. The [delivery record](../../features/pharmacology-item-art-6.md) records 94 passing pharmacy server tests, native checks and evidence.

Complete all 39 existing effect icons from the effect matrix. Keep positive-effect family motifs coherent with the items and make existing Jitters, Appetite, Fatigue, Drowsiness, Interference and Recovery symbols distinct. This adds no debuffs.

Review the full 3D inventory views of the Dream Study Plinth and eight pharmacy-added machines; retain complete models and readable camera/scale. Finish a cross-catalog pass for aliases, held scale, stack overlays, color-vision/greyscale readability, shared Coffee/crop/input palettes and handbook/recipe-view consistency. Preserve unrelated shared art when it already works.

**Done when:** every assignment is accounted for, all 32 finished products and their full manufacturing chains read coherently, all 39 HUD symbols are usable at native size, required checks pass, and reviewed native captures document the final catalog. The running/local save and live server are not replaced or restarted as a consequence of planning.

| Assigned item | ID |
| --- | --- |
| Dream Study Plinth | `jugcraft:dream_study_plinth` |

The other eight model checks are: Laboratory Analyzer, Pharmaceutical Separator, Formulation Press, Autoclave, Sterile Filler, Moonshine Still, Mash and Conditioning Barrel, Release Coater.

All 39 exact effect IDs are listed in the [effect matrix](effect-icon-plan.csv) and [assignment manifest](batch-assignments.csv).
