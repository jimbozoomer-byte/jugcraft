# Flow battery grid storage: proposed specification

Status: **proposal for the owner's approval.** No code yet. Every number below is a suggestion to accept, change or reject.

TODO item: "Extend existing vanadium electrolyte/Flow Battery for VOSO4-based formulated electrolyte and major grid storage, with paired functional sides, modular tank capacity additions and cell-stack charge/discharge output upgrades. Add the selected specifically vanadium-bearing iron-concentrate supply alongside refinery residue, attached paired modules around a bounded controller/cell stack and optional local priorities/minimum-charge reserve. Define sizes, limits, costs, control semantics, losses and atomic shared storage accounting; no routine cycle aging, no free energy from electrolyte."

The [flow battery](flow-batteries.md) and the [twelfth owner batch](industrial-chemistry-and-fuels-plan.md#grid-storage-synthetic-fuels-and-cryogenics-decisions-twelfth-batch) leave these to specify: source and refining, paired-side composition, module geometry and bounds, costs, losses, output, and reserve behaviour. This page proposes all of them, so implementation can follow one approved plan.

## What stays the same

- The existing **Flow Battery** keeps its ID, model, recipe and behaviour. It becomes the **controller and cell stack** of a larger installation.
- A Flow Battery with nothing attached works exactly as today: 64,000 mB of electrolyte, 1,000 JE per mB, up to 64,000,000 JE, 8,192 JE/t each way.
- Electrolyte still sets the ceiling and is never used up. Energy never comes from electrolyte.

## 1. Electrolyte supply: a mineral route beside refinery residue

Today, two asphalt binder and 1,000 mB of sulfuric acid give 1,000 mB of vanadium electrolyte in the Chemical Reactor.

**Proposed addition:** one **vanadium pentoxide** and 1,000 mB of sulfuric acid in the Chemical Reactor give 1,000 mB of vanadium electrolyte. This is the formulated vanadyl-sulfate electrolyte.

Vanadium pentoxide already comes from the mineral line: washed iron ore roasted with lye in the Bulk Chemical Reactor. That makes it the "vanadium-bearing iron-concentrate supply" the item asks for, without a new ore. Both routes give the same fluid, `jugcraft:vanadium_electrolyte`.

A named VOSO4 electrolyte would need its own fluid. That would split existing batteries' contents, so this proposal keeps one fluid.

## 2. Paired tank modules: capacity

**New block: Electrolyte Tank Module.** It is one block wide, three tall and two deep, matching the controller's height and depth. It looks like one of the controller's tall tanks with a blue sight glass.

- **Placement:** modules attach in a row to the controller's **left and right faces**, outward, up to **three deep on each side**.
- **Pairs:** a module counts only when the matching position on the other side also holds a module. This is the "paired functional sides": positive electrolyte on one side, negative on the other. An unpaired module shows a red lamp and adds nothing.
- **Capacity:** each counted pair adds **32,000 mB** to the shared reservoir.

| Pairs | Electrolyte (mB) | Capacity (JE) | Footprint |
| --- | --- | --- | --- |
| 0 (today) | 64,000 | 64,000,000 | 3 × 3 × 2 |
| 1 | 96,000 | 96,000,000 | 5 × 3 × 2 |
| 2 | 128,000 | 128,000,000 | 7 × 3 × 2 |
| 3 (maximum) | 160,000 | 160,000,000 | 9 × 3 × 2 |

- **Bounded:** three pairs at most, so the installation never exceeds 9 × 3 × 2. Checking only those six positions keeps the per-tick work bounded.
- **Recipe:** two borosilicate glass, a steel tank, two steel plates, a copper cable and a capacitor bank. That is the controller's bill without its processor.

## 3. Cell stack upgrades: charge and discharge rate

**New item: Cell Stack.** It fits into **two new upgrade slots** in the controller's screen, the same kind of slots the processors already have. This needs a small screen addition.

- Each cell stack adds **8,192 JE/t** to both charging and discharging. Two stacks give 24,576 JE/t each way, which fills or empties a full 160,000,000 JE installation in about 6,500 ticks (5.4 minutes).
- Stacks do not raise capacity, and capacity modules do not raise the rate. That keeps the selected split between capacity and output.
- **Recipe:** four nickel plates (the electrodes), two plastic sheets (the membrane), a processor and a borosilicate glass. These are suggestions.

## 4. Control: priority and minimum-charge reserve

Two settings on the controller's screen, both local to this one installation. Remote control stays later, as the batch says.

- **Minimum-charge reserve:** 0 %, 10 %, 25 % or 50 % of current capacity. Below the reserve the battery **stops discharging**, but it keeps charging. The default is 0 %, so existing batteries behave as today.
- **Priority:** *Normal*, *Charge first* or *Discharge last*.
  - *Charge first* takes surplus before other batteries on the same network.
  - *Discharge last* gives power only after other batteries are empty.
  - The default is Normal, which is today's behaviour.

## 5. Losses

The item asks for losses. **Proposed:** a **90 % charging efficiency**. Of every 10 JE pushed in, 9 are stored; discharging is lossless. That makes a round trip 90 %. There is no cycle aging or upkeep, as selected.

This changes how today's Flow Battery behaves, so the owner may prefer to apply it only to installations with modules attached.

## 6. Atomic shared accounting

- The controller holds **one reservoir and one energy store** for the whole installation. Modules hold nothing themselves; they only raise the controller's limits. Electrolyte and energy are therefore counted once.
- **Removing a module pair:** capacity drops by 32,000 mB. Electrolyte above the new limit goes with the broken module, as a module item carrying that electrolyte (like a steel tank). Energy above the new ceiling is lost. Nothing is duplicated.
- **Breaking the controller:** it drops with all the installation's electrolyte, as today. Its charge is lost, as today.
- **Saving:** module positions are re-detected on load, never trusted from the save. Electrolyte loads before charge, as today, so a full battery reloads full.

## 7. Tests to write with it

- Pairs counted only when matched; an unpaired module adds nothing; the bound is three pairs.
- Capacity follows pairs; removing a pair drops capacity and gives back the excess electrolyte exactly.
- Cell stacks raise the rate; capacity unchanged.
- The reserve stops discharge at the set level but allows charging.
- The vanadium pentoxide route gives 1,000 mB of electrolyte.
- A lone Flow Battery behaves exactly as before.

## Questions for the owner

1. Are module size, pair size (+32,000 mB) and the three-pair limit right?
2. Two cell stack slots at +8,192 JE/t each: right numbers?
3. Should the 90 % charging loss apply to every Flow Battery or only to installations with modules?
4. Is the vanadium pentoxide route enough as the "mineral-derived" supply, or should there be a separate VOSO4 fluid?
