# Pharmacy item art plan

**Status: Art Batches 1–6 implemented and verified locally.** Owner-directed work, 3–4 November 2026. Prepared with **OpenAI Codex (GPT-6 family)**. See [Batch 1 delivery](../../features/pharmacology-item-art-1.md), [Batches 2–4 delivery](../../features/pharmacology-item-art-234.md), [Batch 5 formula plates](../../features/pharmacology-item-art-5.md) and [Batch 6 HUD/icons/model review](../../features/pharmacology-item-art-6.md).

The owner finds the current vision effects acceptable and asked to improve them later. That deferred work is recorded in [the remaining-batches plan](../../features/pharmacology-remaining-batches.md#owner-review-and-later-visual-polish). This plan concerns recognizable pharmacy inventory/held items, their production materials and matching HUD symbols.

## Art direction

Use recognizable physical forms first: coffee bottle, tablet, powder packet, inhaler, injector, ampoule, patch, paper dose, softgel and distinct beverage bottles. White/colored pills are a fallback for drugs without a better identity. Pill shape, scoring, seams and fill should vary; a line of identical capsules with different hues is insufficient.

These are fictional, unbranded game identities. Preserve real drug names without copying a manufacturer's pill imprint, brand package or product logo.

Follow [the owner's item-icon rules](../../ITEM_ICONS.md): **16×16**, complete object, top-left light, clean flat tones, a one-pixel outline in each material's darkest shade, no pure-black blanket outline, blur, dithering, noisy wear or cropped ends. Center compact objects; put pens/long capsules on the established diagonal and bottles upright. Use at most four flat tones plus outline per material. Retain a transparent margin and read the icon at its actual 1× size.

Practical starting footprints: round tablets 8–10 pixels across, caplets/capsules about 10–12 pixels long and 5–7 thick, paper doses about 9×9, and bottles/devices roughly 11–14 pixels tall. These are layout guides; the whole-object/readability rules win. A new pill should not fill the slot like a block face.

Every finished item needs a recognizable silhouette plus a second useful cue: scoring, a seam, a visible fill, cap/neck arrangement, patch tab or bold mark. Color reinforces identity. Check similar items in greyscale and common color-vision simulations. The large motif can move to its label, stock tag, formula plate or HUD icon if it cannot fit clearly on the finished item. Do not cram every descriptive detail into a tiny pill.

Use warm off-white for pill bodies, cool silver for sterile hardware and restrained pale-cyan glass highlights. Suggested starting ramps include off-white `#EAE6D9`, coffee brown `#6D402A`, royal blue `#406CB8`, coral `#D57A65`, sage `#82966B`, emerald `#4E9B74`, violet `#A17BB7`, crimson `#A64755`, cyan `#82BCC7` and brass `#C8A35D`. Pick matching dark/mid/light tones under the shared rules; these are not mandatory flat fills.

## Coverage and source audit

The current canonical pharmacy map has **142 entries**, including the existing block item for the Dream Study Plinth. Add the two inherited finished items, Aspirin and Stimulant: **144 pharmacy catalog entries**, of which **32 are finished formulations**. The plan also includes **three existing process-fluid buckets**, bringing the item matrix to **147 rows**. All **39 generator-owned effect icons** have a matching plan.

- [Complete item matrix — exact IDs, forms, palettes, motifs and presentation tasks](item-texture-plan.csv).
- [All 32 finished-item designs](finished-tables.md).
- [Complete effect-icon matrix](effect-icon-plan.csv).

These matrices were checked against `pharmacology.ITEMS` and its existing factory/sterile/precision/alcohol/release/psychedelic writers. The three bucket item definitions exist. Counts describe the current local checkout, not future Clockwork trade goods.

Art Batches 1–5 replace the finished-dose, supply, processing-material and formula aliases with 146 individual sprites. All 32 finished formulations and all 32 formula plates are covered. Batch 6 delivers 39 dedicated effect icons, the ten-pill strain row above the hunger bar and the completed nine-model/catalog consistency review. At zero the empty strain row remains for three seconds, then hides. A renamed tooltip alone does not give an item a unique appearance.

The owner library and complete/magic catalogs were consulted. Suitable starting references include the supplied 16×16 `originals/Blocks/Guns/item/syringe.png` and `originals/Magic/assets/jymbaquary/textures/item/empty_potion_vial.png`, plus existing coffee/plant/alchemy art. Inspect candidate images before choosing direct reuse or adaptation, preserve originals/notices, and record actual source-to-runtime paths. Batch 1 inspected the syringe/vial references and authored new editable maps; it imported no library pixels and executed no library program.

## Finished formulations

The detailed table describes the intended final appearance. All 32 finished formulations, Batch 3–4 supply/material forms and Batch 5 formula plates are implemented locally, including the selected packaging/use alignment. HUD and final machine/model consistency work remain planned.

### Early pharmacy

| Item | Planned silhouette and identity |
| --- | --- |
| Caffeine Drink | **Small round coffee-tonic bottle**; coffee brown / cream; coffee bean. Dark drink, cream label and brown stopper. |
| Aspirin | **Round ivory tablet**; ivory / cool grey; cross score. Four bold scored quadrants. |
| Paracetamol | **Long rounded caplet**; ivory / soft teal; single score. Flat oblong outline, one teal end mark. |
| Rehydration Salts | **Folded foil sachet**; white / sky blue; water wave and salt grains. Torn top corner and broad blue lower stripe. |
| Activated Charcoal | **Three overlapping dark tablets**; charcoal / slate; porous tablet. One intact round tablet above two partial discs; no coal-ore silhouette. |
| Stimulant | **Angular hexagonal tablet**; golden orange / red; slanted lightning bolt. Clipped corners and red central slash. |

### Sterile and precision pharmacy

| Item | Planned silhouette and identity |
| --- | --- |
| Penicillin | **Short broad powder vial**; ivory / cobalt blue; three culture dots. Blue crimp cap and visible pale powder base. |
| Lidocaine | **Squat square vial**; ice cyan / silver; shield. Wide cyan label, flat shoulders and silver cap. |
| Adrenaline | **Slim auto-injector pen**; signal red / warm yellow; upright lightning bolt. Yellow safety cap and dark grip; complete device fits diagonally. |
| Salbutamol (Albuterol) | **L-shaped inhaler**; cobalt blue / silver; airflow arc. Large blue mouthpiece and visible silver canister. |
| Morphine | **Long tapered ampoule**; muted plum / ivory; crescent. Plum neck band and one narrow pale label. |
| Naloxone | **Short nasal spray device**; white / vivid orange; open ring. Offset nozzle, orange plunger and two projecting finger rests. |
| Methylphenidate | **Compact round split tablet**; white / royal blue; focus diamond. Blue half, central dividing line and round contour. |
| Amphetamine | **Broad diamond tablet**; warm coral / pale peach; double chevron. Strong four-corner silhouette and recessed double score. |
| Modafinil | **Flat rounded rectangular tablet**; white / sage olive; rising sun. Olive end, short central score and clipped lower corner. |
| Ketamine | **Broad round-shouldered vial**; lavender / blue grey; twin arch. Large lavender cap, broad body and twin-arch label. |
| Fentanyl | **Very narrow sealed ampoule**; deep crimson / slate; two vertical bars. Two crimson neck bands and a small square base. |

### Controlled release

| Item | Planned silhouette and identity |
| --- | --- |
| Caffeine XR | **Long two-part capsule**; white / coffee brown; coffee bean. Brown half, double collar and two visible inner beads. |
| Methylphenidate XR | **Long two-part capsule**; white / royal blue; focus diamond. Blue half, double collar and two visible inner beads. |
| Amphetamine XR | **Long two-part capsule**; white / warm coral; double chevron. Coral half, double collar and two visible inner beads. |
| Modafinil XR | **Long two-part capsule**; white / sage olive; rising sun. Olive half, double collar and two visible inner beads. |
| Fentanyl Patch | **Rounded rectangular patch**; tan / white / crimson; patch window. White central pad and a visibly peeled red corner tab. |

### Botanical and psychedelic pharmacy

| Item | Planned silhouette and identity |
| --- | --- |
| THC | **Translucent oil softgel**; amber / forest green; five-point leaf. Golden inner droplet, dark green seam and leaf seal. |
| Psilocybin | **Short mushroom-marked capsule**; white / muted violet; mushroom cap. Violet upper shell shaped like a small cap above the pale lower half. |
| Mescaline | **Angular-ended split capsule**; white / emerald; three-prong cactus. Emerald half with squared ends and a cactus-shaped negative-space seam. |
| LSD | **Single perforated paper dose**; cream / magenta violet; mirrored diamond. Small square, clearly notched paper edges and a bold central diamond. |
| DMT | **Clear capsule with crystalline core**; ice cyan / pale gold; small cog. Visible golden crystal core, cyan collar and transparent outer contour. |

### Alcohol

| Item | Planned silhouette and identity |
| --- | --- |
| Alcohol (Moonshine) | **Squat round clear bottle**; clear grey / hemp tan; rope knot. Short neck, simple cork and coarse paper wrap. |
| Alcohol (Refined Spirits) | **Tall straight-shouldered bottle**; pale gold / cool teal; two horizontal strokes. Clean straight sides and a narrow teal neck band. |
| Alcohol (Reserve Whiskey) | **Broad square amber bottle**; amber / oak brown; oak leaf. Heavy square shoulders, dark cork and broad brown label. |
| Alcohol (High-Proof Spirits) | **Very tall slender angular bottle**; clear blue grey / red; upward chevron. Long narrow neck, red seal and small angular shoulders. |
| Alcohol (Masterwork Reserve) | **Faceted decanter**; deep amber / brass gold; three-point crown. Cut shoulders, gold stopper and burgundy label; whole vessel visible. |

## Manufacturing stages should read immediately

Keep a drug family's color and large motif through the whole chain while changing the material/container silhouette at each stage:

| Stage | Visual language | How it differs from a usable drug |
| --- | --- | --- |
| Raw crop/harvest | Actual seed, fruit, mushroom, bean or resin silhouette | Organic form, no medical cap, formula frame or finished capsule seam |
| Reagent blend | Crimped paper pouch with mixed granules and outlined family mark | Folded bag shape rather than a pill, vial or drink |
| Unverified stock | Square amber bulk reagent jar; cork; uneven fill and outlined tag | Broad laboratory jar and outlined mark, distinct from the finished dose |
| Verified active | Small hexagonal weighing boat, clean material and solid family tab | Exposed preparation in a tray, never a sealed dose |
| XR coated core | Exposed layered bead/cutaway in the family color | Clearly lacks the closed outer capsule shell |
| Finished dose | Its own proposed product form, clean closure and strongest identifying cue | Ready-use appearance, different from every material stage |
| Formula | Notched slate card with a large family glyph and short color band | Flat reference object with a consistent card frame |

There are **31 named formula plates plus one blank**. Preserve all their existing IDs, including `caffeine_formula_plate` for Caffeine Drink. Base formulas use a single upper border; XR formulas add a double border and bead/hourglass corner. Patch formulas show the patch-window mark. The blank plate has an empty diagram. Do not use unreadable tiny drug names as the main identifier, or reuse the generic circuit-chip icon.

XR formulations keep their base drug's palette and motif. A longer two-part capsule, double collar and visible inner beads show controlled release. Regular Methylphenidate is a compact round tablet; its XR form is a long capsule. Regular Amphetamine is a diamond; its XR form is a capsule. Regular Modafinil is a flat caplet; its XR form is a capsule. Caffeine Drink and Caffeine XR share the coffee identity while keeping bottle/capsule silhouettes distinct.

## Supporting items

Every individual ID has a row in the item matrix. The intended material families are:

| Items | Planned identity |
| --- | --- |
| Pharmaceutical Starch / Sterile Carrier / Prepared Pharmaceutical Base | Open cream powder packet / sealed blue-edged foil packet / shallow ceramic tray of pale blocks |
| Sterile Vial | Empty thin clear vial with a silver cap; visibly unfilled |
| Azure Culture Starter / Culture Feed / Antibiotic Extract | Blue-colony culture dish / coarse brown feed pouch / bulk powder-extract jar |
| Sterile and precision stocks | Family-marked bulk jars, distinct from finished medical devices and ampoules |
| Precision blends and verified actives | Family-marked crimped pouches and weighing boats respectively |
| Empty Pharmacy Capsule / Botanical Capsule Shell | Two separated hollow halves; clear-white standard shells and cream/sage botanical shells |
| Release Matrix Blend / Matrix / Advanced Matrix | Composite granule packet / two-layer wafer / reinforced three-layer wafer |
| Patch Backing / Unverified Patch Stock / Finished Patch | Folded blank backing / stack of exposed unfinished sheets / one clean pad with a peeled red tab |
| Empty Mash Jug / Mash Jug / Fermented Mash Jug | The same whole ceramic handled vessel, differentiated by empty mouth / visible grain and straw tie / stopper and bubble seal |
| Beverage Culture Pack | Tan folded culture sachet with bubble motif; distinct from blue rehydration salts |
| Beverage concentrates | Wide-neck bulk flasks with the retail product's family mark, not the retail bottle silhouette |
| Masterwork Blend / Unconditioned Concentrate / Final Concentrate | Coarse golden blend pouch / muted bulk flask with hollow crown and grey tie / finished amber bulk flask with the solid crown family seal |
| Conditioning inserts | Three oak slats in a metal clamp; the masterwork insert adds brass framing and burgundy corner details |
| Precision Process Module / Release Control Die | Framed circuit/socket module / solid square press die with an hourglass recess |
| Botanical Resin / Dreamcap / Prism Cactus Fruit | Green/amber sticky lump / broad violet mushroom / angular green fruit with pink cut tip |
| Hemp Seeds / Dreamcap Spores / Prism Cactus Seeds | Teardrop seed cluster / violet spore pouch / ridged angular seed cluster |
| Dream / Prismatic Concentrate | Round violet material flask / angular emerald material flask |
| Perception Substrate / Oneiric Stock | Layered prepared inscription tile / faceted cyan/gold preparation prism |
| Mycelium Catalyst | Branching pale mycelium cluster with a violet node |
| Study Canvas / Lucid / Pattern / Oneiric Imprint | Blank curled sheet / eye impression / paired mirrored diamonds / cog within an arch |
| Dream Folio | Dark-plum bound book, brass clasp, large teal compass-eye seal and a visible spine |
| Dream Study Plinth | Retain the complete current 3D block-item view and its stone/crystal/copper identity |

Natural harvest/seed items follow [natural-texture rules](../../NATURAL_TEXTURES.md) where applicable. Match their parent crops; do not apply a heavy pill outline to a harvested mushroom or turn spores into another tablet. Existing Coffee Plant/Coffee Beans remain the caffeine producer. Match the coffee family to their brown bean identity rather than adding another crop. Keep current shared feedstocks such as willow/birch, beetroot, potatoes, corn, sugar, vinegar, charcoal, paper, glass, rubber and process chemicals in their existing registries/art families. `redleaf_medical_stock` currently uses the existing fictional beetroot-based recipe; this plan adds no Redleaf crop.

The three existing process buckets keep the whole standard bucket form. Organic Process Stock gets a dark brown liquid surface/process mark, Beverage Wash a cloudy ochre/grain mark, and Refined Beverage Stock a pale-gold/two-stroke mark. They remain machine inputs, visually distinct from bottled finished Alcohol. Existing generic glass bottles and vanilla feedstocks do not need duplicate pharmacy-only copies.

## Machines and held presentation

The eight pharmacy-added machine block items retain their complete **3D model inventory view**: Laboratory Analyzer, Pharmaceutical Separator, Formulation Press, Autoclave, Sterile Filler, Moonshine Still, Mash and Conditioning Barrel, and Release Coater. Show their existing major identity at slot scale—lens, separator drum, press head, sterilizer door, vial rack, copper pot/coil, wooden barrel and coating drum. Audit their model camera angle/scale and owner silver/olive/dark-steel materials; do not replace them with flat bottle icons or rebuild their machines. Existing shared reactors, fermenters and distillation equipment keep their shared models.

Batch 1 aligns the Rehydration sachet, Stimulant tablet, Adrenaline injector, Salbutamol inhaler and Naloxone spray with their packaging, native timed use and returned containers. Batch 2 aligns LSD's paper dose: four paper replace four botanical shells in its four-dose Press job; the dose keeps 0.8-second use and no bottle return. Existing doses, onset, benefits, strain, yields, timing and self-use authority are preserved. Packaging remains paid, reachable and compatible with existing machine slots. The linked delivery records document and test these changes.

THC softgel, Psilocybin capsule, Mescaline capsule and the crystal-filled DMT capsule intentionally retain a capsule identity. Raw harvested mushrooms/resin and raw/verified crystalline preparations keep their own production-stage forms. Do not depict the finished dose as a loose crop or mining ore.

Existing 3D held models, where present, remain; adapt their material/look consistently. Generated flat pills use restrained in-hand scale rather than appearing as full-size shields. Static item sprites are the default. No inventory flashing or animated kaleidoscope is required to recognize a psychedelic item.

## Matching effect icons

Use one large high-contrast symbol per 16×16 HUD icon, derived from the product's motif or actual game benefit. The [39-row effect matrix](effect-icon-plan.csv) specifies every existing generator-owned entry. Coffee uses a bean; focus a target/diamond; Adrenaline a bolt; Lidocaine a shield; Salbutamol airflow; Morphine a crescent; THC a leaf; Psilocybin a mushroom; Mescaline a cactus; LSD mirrored geometry; DMT a cog/arch. Alcohol grades use their respective bottle silhouettes.

Existing side effects get separate symbols: Jitters an uneven bolt, Appetite a bowl, Fatigue a dim bolt/down arrow, Drowsiness a closed eye, Pattern Interference a split target. Recovery uses a neutral clock wedge. These distinguish the existing states; the art pass adds no injury condition, withdrawal system or new gameplay debuff.

## Implementation batches

Use the [six-batch delivery plan](batches.md) for exact scopes, item lists, dependencies and completion criteria. The [assignment manifest](batch-assignments.csv) assigns every item/effect/model review once.

| Art batch | Concrete delivery | Size |
| --- | --- | --- |
| **1 — Everyday and medical drugs — locally complete** | Shared art foundation; early, sterile and precision finished drugs; matching use/packaging | **17 finished items** |
| **2 — Advanced drugs and alcohol — implemented locally** | Four XR drugs, patch, five botanical/psychedelic doses and five alcohol grades; LSD paper alignment | **15 finished items** |
| **3 — Ingredients, packaging and study supplies — implemented locally** | Binders, cultures, empty shells, matrices, crops/seeds, jugs, tools and study materials | **32 items** |
| **4 — Processing intermediates and fluids — implemented locally** | Reagent blends, stocks, verified actives, coated cores, concentrates and three process buckets | **50 items** |
| **5 — Formula plates — implemented locally** | All named formulas and the blank card | **32 items** |
| **6 — HUD, models and final consistency** | All effect icons; plinth and eight machine views; combined readability/alias/held-scale review | **39 HUD icons + 9 model checks** |

Batch 1 establishes the palette, silhouettes, canonical mappings and native preview workflow while delivering its finished drugs. Presentation/packaging alignment happens with the affected finished item in Batch 1 or 2. Every batch includes its own relevant resource and in-game checks; Batch 6 provides the final combined review. The plinth is already one of the 147 catalog items and retains a 3D view, so it is counted once. Art batches are separate from original manufacturing Batch 7 and the deferred vision-effects polish.

## Completion checks

- Each row resolves to the correct registered item/model; distinct finished IDs do not still alias the same generic sprite.
- The whole object reads at 16×16, on both inventory backgrounds, with native stack-count overlays; tiny label text is never the sole identifier.
- Silhouette/detail survives greyscale inspection, especially Aspirin/Paracetamol/Methylphenidate/Modafinil and the sterile vials.
- Base/XR, stock/active/finished, blank/printed formula, unconditioned/conditioned beverage and backing/patch stay distinguishable.
- Review the actual inventory, hotbar, held use, dropped item, item frame, machine slots, recipe viewer and handbook; a large preview sheet alone does not establish readability.
- Extend the existing canonical writers/icon maps; do not hand-edit only generated JSON that the next generation overwrites. Retain IDs and owner provenance. Run relevant resource/icon checks and native consumption/container checks for the presentation adjustments.

The original planning change was checked for complete catalog coverage and local documentation links. Art Batches 1–5's local implementation, packaging recipe changes and validation are recorded in the linked delivery notes. Art Batch 6 remains planned; the existing flat test world was not replaced or restarted.
