# Early pharmacy — Batch 1

Current shared strain, replacement and combination rules are recorded in [decaying strain — Batch A](pharmacology-strain.md); the original batch behavior below is historical.

Status: implemented and verified locally. Not deployed.

Manufacturing follow-up: [Pharmacy manufacturing — Batch 2](pharmacology-factory.md) adds automated production of these same medicines without changing their dosing profiles.

Owner-directed implementation with OpenAI Codex (GPT-6 family). Extends the [caffeine foundation](pharmacology-caffeine.md), using the existing shared machines and stable medicine IDs. All recipes, timings and benefits are fictional game mechanics.

## Player outcome and balance

| Formulation | Benefit | Full duration | Onset | Strain | Family |
| --- | --- | --- | --- | --- | --- |
| Caffeine | Haste I; 10% movement bonus, suppressed by stronger Speed | 5 minutes | 10 seconds | 10 | stimulant |
| Aspirin | Fortitude: 10% less armor-sensitive physical damage | 12 minutes | 5 seconds | 10 | fortitude |
| Paracetamol | Recovery: 25% more natural food-based healing per interval | 12 minutes | 5 seconds | 10 | recovery |
| Rehydration Salts | Endurance: 20% less activity exhaustion | 15 minutes | 5 seconds | 5 | endurance |
| Stimulant | Haste II and Speed II; Stimulant Appetite | 60 seconds | immediate | 25 | stimulant |

Aspirin no longer gives its old short Regeneration effect or removes nausea. Its item ID, salicylic-acid inputs and three existing reactor recipe IDs/yields remain unchanged. Existing Stimulant keeps its item ID, cocoa/bioethanol recipe, full first-dose duration and positive effect strengths. Its Hunger drawback becomes a distinct **Stimulant Appetite** status, adding 0.1 activity exhaustion per online second while present. This lets charcoal clear drug side effects without erasing unrelated Hunger.

Fortitude applies after ordinary armor/protection to damage that does not bypass armor and is not tagged fire. Stronger vanilla Resistance suppresses it. It does not protect against falling, starvation, magic or other armor-bypassing sources. Recovery wraps only vanilla food regeneration: its food threshold, saturation logic and natural-regeneration gamerule remain authoritative. Additional healing pays proportional food exhaustion. It does not multiply instant healing, potions or other calls to heal. Endurance changes player activity exhaustion, leaving natural healing's direct food cost intact.

No injury, illness, combat-triggered debuffs or mandatory treatment loops are added. The original instant First Aid Kit and Antidote remain available with their existing behavior. Only the player consuming a formulation receives it; there is no forced dosing or dispenser application.

## Ingredients and manufacturing

| Producer / recipe | Inputs | Output / consumer |
| --- | --- | --- |
| Existing coffee crop and roasting | Cherries, then beans; existing recipes | Existing caffeine chain, unchanged |
| Crafting, Pharmaceutical Starch | 2 potatoes + paper | 2 starch, a shared pharmacy binder |
| Existing Chemical Reactor, Paracetamol | 2 starch + redstone + 250 mB water; 160 ticks | 4 tablets |
| Crafting, Rehydration Salts | 2 salt + sugar + 2 glass bottles + water bucket | 2 drinks; one empty bucket returned |
| Crafting, Activated Charcoal | 2 charcoal + redstone + paper | 2 consumables |
| Existing aspirin and stimulant chains | Existing inputs and fluid recipes | Existing item IDs, with shared dosing rules |

The starch/redstone formulation is deliberately fictional. No new machine, ore, plant, reagent fluid, dependency or dimensional ingredient is required. Manual preparation stays useful before steel; Paracetamol and the existing reactor medicines use the established steel-era factory. Salt retains its existing acquisition routes. Agriculture gates acquisition of the new crafting recipes; Salts additionally require the salt feature, and the Paracetamol reactor recipe additionally requires machines. Registrations persist when acquisition is disabled.

Every recipe consumes its ingredients. Tablets and charcoal leave no container. Salts and stimulant drinks return one bottle through the native consumption remainder; successful stacked drinks return a bottle without replacing the remaining stack. Rejected timed doses spend neither an item nor a bottle. No medicine converts back into its ingredients.

## Shared dosing, side effects and feedback

All five timed profiles use the same bounded, saved `pharmacology_v1` attachment. Different families combine within 100 strain; caffeine and Stimulant cannot bypass one another. The same formulation may refresh only in its final quarter. A refresh replaces the timer rather than adding duration. Repeat doses last 75%, then 50% of the profile duration. An active stimulant redose causes 60 seconds of Caffeine Jitters, increasing existing firearm spread by 20%; other families do not acquire stimulant drawbacks.

Activated Charcoal is an instant utility, not another timed family. One use clears only Caffeine Jitters and Stimulant Appetite, leaves benefits and unrelated statuses alone, and has a native 60-second cooldown. It does not change saved dose, strain, tolerance or recovery values. These are game effects, not medical guidance.

Native effects retain their highest amplifier; passive benefit multipliers choose the strongest applicable value rather than multiplying same-purpose drug bonuses. Short Haste/Speed pulses preserve unrelated stronger or longer effects and expire within two seconds after cancellation. Passive bonuses stop immediately when their marker is removed. Hold any timed formulation to see its family member's onset, active or recovery timer and total strain, including when another drug in that family blocks it. Tooltips describe the benefit, duration, onset, strain and redosing rules. The Pharmacology handbook chapter and encyclopedia cover the full roster and recipes.

Milk cancels pending and active benefits without erasing accounting. Strain remains reserved for two minutes after expiry/cancellation and blocks that family during recovery. Tolerance clears after 30 online minutes without an active family dose. Logout pauses all timers. Death clears benefits and preserves recovery/history, normalized after Fabric copies the saved attachment. Existing caffeine state uses the same codec and IDs; no world rewrite is required. Existing ordinary medicine stacks acquire the new item defaults; deliberately customized stack components are not rewritten.

## Implementation and assets

Profiles describe native and passive benefits centrally. Server-authoritative item checks run both at use start and consumption finish. Three small mixins extend the existing damage, FoodData healing and player-exhaustion paths; no second injury or nutrition subsystem is introduced. Work is bounded to saved doses (at most eight), online players and existing engine events, with no world searches. Client presentation uses native status effects, tooltips and action-bar messages; no client rendering framework or optional dependency is needed.

`tools/pharmacology.py` owns new item models, recipes, names, recipe-discovery advancements and handbook pages. `tools/petro.py` owns the new shared-reactor recipe. Art references reuse already-integrated runtime textures unchanged: `item/aspirin.png`, `item/flour.png`, `item/glucose_syrup.png`, `item/stimulant.png`, `item/first_aid_kit.png`, `item/coffee_beans.png`, `item/jimtafacts/crops/coffee_powder.png` and `item/jimtafacts/industry/jimta_coal_dust.png`. Status icons are byte-for-byte copies of those same authored textures. Library originals are untouched; no downloaded media or new pixel painting.

New stable items: `pharmaceutical_starch`, `paracetamol`, `rehydration_salts`, `activated_charcoal`. New effects: `fortitude`, `pharmacy_recovery`, `endurance`, `stimulated`, `stimulant_appetite`. Existing caffeine, aspirin, stimulant, ingredient and recipe IDs remain registered.

## Verification

Verified locally on 2 November 2026 against Minecraft 26.3, Fabric Loader 0.19.5, Fabric API 0.161.0+26.3 and Java 25:

- Main and game-test Java sources compiled. Unchanged client Java/Kotlin outputs were reused from the verified caffeine baseline.
- Full `tools/generate_material_data.py` completed successfully. A focused refresh then applied the final Antidote wording to the language file, handbook and encyclopedia, preserving the language writer's existing Unicode formatting.
- **14 required server checks passed across focused runs:** all 12 `PharmacologyGameTests` cases and both `AspirinGameTests` cases. Coverage includes loaded recipes/remainders, independent families/players, actual physical damage and Resistance priority, vanilla healing and its food cost/gamerule, actual activity exhaustion, selective charcoal cleanup, tolerance and actual player-file reconnect/death persistence. The damage fixture was corrected to complete the native client-loading handshake and reset Minecraft 26.3's separate hit cooldown before comparing hits; the production defense code was not changed to accommodate the fixture. Previously passing cases were retained, and only affected cases were rerun.
- **Rendered `PharmacologyClientGameTests` passed** with one client and an integrated server. The client received all independent benefits, native milk removed benefits without freeing strain, the stimulant stayed active over real online ticks and redosed in its final quarter, and native charcoal consumption removed the synchronized drawbacks while keeping benefits and saved accounting. Both inventory screenshots were inspected: item/status icons render correctly. This is an automated client check, not a human playtest.
- `python tools/check_icon_maps.py`: passed, 285 maps and 2,017 item icons; 28 existing unrelated warnings.
- `python tools/check_mod_data.py`: passed the 15,306-material-ID data/recipe audit, including 38,254 block/item models, 3,777 quad parts and 182 composed-model checks.
- `python scripts/check_repository.py`: passed repository structure and documentation links. `git diff --check`: passed.

The local runtime used the existing complete resource copy plus an explicit refresh of every pharmacy resource and the shared guide/catalog files. `processResources` and unchanged client compilation were excluded to avoid repeating full asset-tree snapshots. This is focused runtime/compilation evidence, not an unfiltered `build` or a run of unrelated client galleries.

Human survival playtesting, two independent clients, separate-process server restart and live deployment are outside this local verification.
