# Cryogenic warm-back (the Rotary Condensator's role)

Status: implemented on the contributor's fork, pending review.

Proposal issue: none. TODO item: "… Use selected **ordinary Rotary Condensator and upgraded Cryogenic Liquefier roles** … and insulated shared-tank variants. … account for powered conversion without gas/liquid duplication." The Encyclopedia item says to map the Rotary Condensator's role "onto existing shared systems; preserve current IDs". This change does that. It adds no new machine.

**This change's choices:** the role sits on the existing Cryogenic Liquefier, warming is lossless, and it takes half the condensing time. The owner can review all three.

Owner: jimbozoomer-byte
Target milestone and tier: rocketry, alongside the liquefier
Primary specialty and supported player role: chemistry and rocketry; players who store fuel as liquid and want gas back for generators or chemistry

## Player experience

The Cryogenic Liquefier now works both ways, like a rotary condensator.

- **Gas in:** it condenses it, as before.
- **Liquid in:** it warms it back into the gas it was made from.

| Liquid in (input tank) | Gas out | Time |
| --- | --- | --- |
| 250 mB liquid oxygen | 1,000 mB oxygen | 40 ticks |
| 250 mB liquid methane | 1,000 mB methane | 40 ticks |
| 125 mB liquid hydrogen | 1,000 mB hydrogen | 80 ticks |

Each is exactly its condensing recipe reversed, at half the time. The machine's screen tagline now reads "Liquefies oxygen, methane or hydrogen, and warms them back."

## Connections

- **Input producers:** the liquefier's own liquids. They can be shipped and stored as liquid, which is four or eight times denser than the gas.
- **Output consumers:** every existing user of oxygen, methane and hydrogen: generators, the Chemical Reactor, the Synthesis Converter and the fuel cell.
- **Machines:** the existing Cryogenic Liquefier (`MachineKind.CRYOGENIC_LIQUEFIER`, recipe type `liquefaction`). No new ID.

## Balance

- Gas is conserved both ways: 1,000 → 250 → 1,000 mB (hydrogen 1,000 → 125 → 1,000). Nothing is made or lost.
- Both directions cost the liquefier's power (96 JE/t), so a round trip only spends energy. There is no positive loop.

## Multiplayer and persistence

- **New recipe IDs:** `jugcraft:liquefaction/oxygen_from_liquid`, `methane_from_liquid` and `hydrogen_from_liquid`.
- The block, its tanks and its saved data are unchanged.

## Verification

**Local checks** (no Gradle or game): `python tools/generate_material_data.py`, `python tools/check_mod_data.py` and `python scripts/check_repository.py` passed.

**Two audit changes in `tools/check_mod_data.py`**, needed because a recipe now gives more mB than it takes:

- **The fluid-volume rule.** A recipe may give more mB than it takes only when it names (`"reverses"`) a recipe of the same machine and is that recipe's exact mirror: the same fluids and amounts, swapped.
- **The power-loop audit's cheapest-electricity costing.** It no longer counts a route that needs the fluid being costed as a free way to make it. Before this change, liquid hydrogen warmed back into hydrogen made hydrogen look as cheap as running the liquefier.

**Server game test for CI:** `MethaloxGameTests.liquefierWarmsLiquidsBackToTheirGas`. For oxygen, methane and hydrogen, it checks two things:

- each warm-back recipe takes exactly the liquid its condensing recipe gives, and gives back exactly the gas that recipe used;
- the liquefier's input tank accepts the liquid.

**Results:** the fork's Build workflow passed on commit 8d8a06cb ([run 39108216361](https://github.com/mmme123-spec/jugcraftfork/actions/runs/39108216361)): compilation, the headless server game tests including `liquefierWarmsLiquidsBackToTheirGas`, and the rendered-client shards.

**Not run:** an in-game test of a liquefier running a warm-back batch end to end.

## AI attribution

Implemented with Claude Opus 5.5 (Claude Code) for the contributor; the contributor is responsible for the output.
