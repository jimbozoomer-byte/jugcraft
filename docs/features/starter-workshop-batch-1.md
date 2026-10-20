# Starter workshop — Batch 1

Status: implementation in progress, owner authorized 20 October 2026.
AI assistance: OpenAI Codex (GPT-6).

## Scope

Implement the first slice of the [industrial starter workshop plan](industrial-starter-workshop-plan.md): a two-block-wide Workshop Bench, a one-block Small Ceramic Kiln and a two-block-tall Small Crucible Furnace. The bench uses interchangeable, permanent hammer, wire cutters and fuel-heated soldering tools. No tool wear or installed-part maintenance.

Construction follows the accepted provisional costs: bench = crafting table, four planks, two cobblestone; hammer = two iron and a stick; cutters = two iron and two sticks; soldering tool = copper and a stick; kiln and furnace body each = four brick blocks and a furnace. Shape a lining from four clay balls and a crucible from seven, then fire them and install both in the furnace. One clay ball makes two insulator blanks, fired into the existing ceramic insulators.

## Processing contract

- Manual plates: iron, copper, tin, zinc, lead, bronze and brass, one ingot to one plate, 400 active-work ticks.
- Manual copper wire: one copper ingot to three wires, 400 active-work ticks.
- Manual basic circuit: existing silicon, three copper wires and solder to one circuit, 800 active-work ticks with ordinary heating fuel.
- Kiln: two insulator blanks in 200 ticks; lining in 400 ticks; crucible in 600 ticks.
- Furnace: three copper and one tin to four bronze, or three copper and one zinc to four brass, in 800 ticks; one tin and one lead to two solder in 480 ticks.
- Preserve machine yields and existing powered recipes, bronze-blend fallback, current copper cable and silicon routes. Add six wires, one tin and two insulators to six copper cables as an alternate starter recipe.

Select a recipe, insert ingredients and hold Work for manual production. Server-validated work pauses when released or the player leaves. Fuel credit, committed ingredients and partial progress survive saves; blocked output or missing requirements spend no fuel. Inventory automation never supplies player work or removes a tool during an active batch. Installed lining and crucible are reusable.

Registration remains stable with features disabled; acquisition uses the machines feature without requiring drones for early insulators. Use shared recipes, inventory, fuel and permission systems. New stations must handle interruption, multiple viewers and breaking either multiblock part without duplicate drops.

## Boundaries and overlap check

Current main, open pull requests and public fork branches were reviewed before implementation. The upstream starter-workshop branch contains planning documentation only. Advanced ceramics and powered-tool work are adjacent, not this implementation; retain their identities and recipes.

Batch 2 is excluded: no quartz world generation, silicon heat upgrade, bellows, reusable gear molds, mechanical waterwheel or existing machine cost changes. No terrain generation changes.

## Validation

Pending implementation: focused server tests for acquisition, yields, fuel, partial progress, blocked output, concurrent requests, automation and multiblock drops; focused real-client inspection and save/reopen; generated-data and recipe-loop checks; compile and package a separate inspection world with matching runtime. Record observed results here before review. Do not infer separate-process two-player testing from automated single-client coverage.
