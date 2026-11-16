# Pharmacology: coffee-first caffeine

Current shared strain, replacement and combination rules are recorded in [decaying strain — Batch A](pharmacology-strain.md); the original batch behavior below is historical.

Status: locally implemented and verified; not deployed to the live server. Prepared with OpenAI Codex (GPT-6 family) under the owner's implementation request.

Current expansion: [Early pharmacy — Batch 1](pharmacology-early-pharmacy.md) adds the medicine roster and migrates aspirin/Stimulant to the shared ledger. The verification below records the original caffeine slice; the expansion records its own results.

## Player outcome and scope

An optional early-game farm-to-drink loop: grow the existing Coffee Plant, harvest Coffee Cherries, roast them using existing furnace/smoker/campfire recipes, prepare concentrate and craft a caffeine drink. No new machine, crop, research gate, injury system or combat-triggered debuff is added. Existing stimulant, aspirin, first-aid and coffee-food identities/recipes remain unchanged.

- Two Coffee Beans craft into two Caffeine Concentrate. Four cocoa beans craft into one concentrate, a four-times-lower yield per ingredient.
- Two concentrates, one sugar, two glass bottles and one water bucket craft into two drinks. Crafting returns the empty bucket; successful consumption returns one glass bottle.
- After ten seconds of onset, caffeine gives Haste I and 10% faster movement for five minutes. It reserves 10 of the shared 100 strain capacity.
- Stronger Haste/Speed effects retain priority. Caffeine's movement modifier is suspended during a vanilla Speed effect. Short native Haste pulses preserve stronger/longer potion effects without owning or deleting them.
- The named Caffeine status shows the benefit; holding a drink shows remaining onset/benefit/recovery time and strain in the action bar. Tooltips and a dedicated Pharmacology handbook chapter explain production and dosing.

All recipes, doses and effect numbers are fictional gameplay definitions, not real chemical instructions or medical claims.

## Shared foundation and balancing

`pharmacology/DrugProfile` describes a fixed formulation and its family, duration, onset, strain and marker effect. An immutable, bounded `DrugState` attachment persists up to eight doses and eight family histories using remaining online seconds. The server validates both use start and consumption completion. Rejected drinks consume nothing and generate no bottle.

An active family cannot be switched. The same formulation can be redosed only in its final quarter; it refreshes within its current tolerated cap instead of accumulating time. Early redosing gives 60 seconds of Caffeine Jitters: the existing server firearm-spread calculation increases 20%. First use has no crash. Repeat doses have 75%, then 50% duration; thirty online minutes without an active stimulant clears tolerance. No full withdrawal/dependence system is included in this slice.

Strain remains reserved for two minutes after ending or cleansing a dose. This tail also blocks another dose of that family. Cleansing during onset cancels the pending benefit. Milk does not reset strain, tolerance or recovery. Death ends active/pending benefits and starts recovery, keeping tolerance. Logout pauses saved dose, onset, recovery and tolerance clocks; no wall-clock/offline processing is used.

Respawn normalization runs in a phase after Fabric's default attachment-copy phase. This prevents the copy from restoring the old active dose over the new recovery state, regardless of mod initialization order.

The original first slice used this ledger only for caffeine. The owner-directed [early pharmacy batch](pharmacology-early-pharmacy.md) now explicitly migrates aspirin and Stimulant while preserving their item IDs and manufacturing routes. The aim drawback uses the gun system's existing spread rather than adding an independent camera-sway renderer.

## Inputs, consumers and reachability

- Producers: existing coffee crop and roasting recipes, vanilla cocoa, sugar cane, glassmaking and water.
- Consumers: players preparing for work or travel; the concentrate remains a reusable pharmaceutical input for later tiers.
- Tier: early manual preparation, independently useful before electricity or steel.
- Feature switch: acquisition recipes follow `agriculture`; registrations and saved items remain present when disabled.
- No required trade, dimensional ingredient, industrial reagent, reactor or machine output is needed for entry.
- All recipes consume their inputs; no output converts back into concentrate or beans. Water/consumption containers return once through native crafting/use remainders.

## Assets and infrastructure

No new dependencies or rendering framework. Fabric's existing persistent attachments and player/tick events handle authority/lifecycle; native status effects, attribute updates, tooltips and action-bar messages handle feedback. Per-tick work is bounded to online players with drug state, with state updates once per second and no world scanning.

Art was checked against the shared owner library/catalog. Concentrate directly references the already-integrated `textures/item/jimtafacts/crops/coffee_powder.png`; the drink references the existing authored `textures/item/stimulant.png`. New status icons reuse Jugcraft's authored `textures/item/coffee_beans.png` byte-for-byte. Originals are untouched. `tools/pharmacology.py` maintains deterministic definitions, recipe discovery, names, model references and effect-icon copies.

New stable IDs: items `caffeine_concentrate`, `caffeine_drink`; effects `caffeinated`, `caffeine_jitters`; attachment `pharmacology_v1`; crafting recipes `caffeine_concentrate_from_coffee`, `caffeine_concentrate_from_cocoa`, `caffeine_drink`. The attachment codec has bounded lists/timers and defaults for missing state. There is no migration of existing medicine IDs or items.

## Verification

Verified locally on 2 November 2026:

- Main, client and game-test sources compiled against the pinned toolchain.
- Full material-resource generation completed with `PYTHONUTF8=1` and the existing DejaVu font directory configured. The final recipe-discovery correction was refreshed through the caffeine generator and verified by the actual game's registry loader.
- `python tools/check_mod_data.py`: passed the 15,302-material-ID data/recipe audit, including the art checks (38,254 block/item models, 3,777 quad parts and 182 composed models).
- `python tools/check_icon_maps.py`: passed; existing unrelated palette/legacy warnings remain.
- `python scripts/check_repository.py`: passed repository structure and documentation links.
- `PharmacologyGameTests`: **all five required tests passed** on the headless game-test server. Coverage includes loaded roasting/coffee/cocoa/drink recipes, bucket and bottle accounting, onset, rejected redosing, potion priority, cleansing during onset, expiry, recovery, duration/tolerance limits, Jitters, persistent codec, actual player-file save/reconnect and native death/respawn.
- `PharmacologyClientGameTests`: **passed** with one rendered client and its integrated server. Server-applied Caffeine and Haste reached the client; the inventory screenshot showed the ingredient/drink/effect icons; native milk consumption removed the visible benefit while retaining strain and the redosing block.

The focused local runtime invocation used complete copied resources and already-compiled, unchanged client source outputs to avoid repeated snapshots of the large asset tree. Main and game-test source changes were recompiled. This is focused runtime/compilation evidence, not a claim that the unfiltered `build` task or every unrelated client gallery ran.

Not run: human survival acquisition/playtesting, two independent clients, a separate dedicated-server process restart, or public-server deployment. The crop's existing growth/harvest behavior was reused rather than changed; the new tests validate the loaded processing routes and consumption/lifecycle behavior.
