# Plastic from naphtha

Status: implemented on the contributor's fork, pending review.

Proposal issue: none. Item 6 of the contributor's list of the most convoluted parts of the pack (26 October 2026): a plastic sheet cost 2.5 buckets of crude oil. The contributor chose to have it built (27 October 2026); it is item 6 of the simplification proposals on the contributor's `claude/simplification-proposals` branch.

Owner: jimbozoomer-byte
Target milestone and tier: petrochemistry, the first plastic
Primary specialty and supported player role: factories; anyone who needs plastic sheets

## Player experience

The Polymerization Reactor now takes naphtha as well as refinery gas: a bucket of either gives four plastic pellets. The Metal Press still flattens each pellet into a sheet.

Each bucket of crude oil gives only 100 mB of refinery gas but 250 mB of naphtha, so the first plastic gets much cheaper:

| From one bucket of crude oil | Sheets before | Sheets now |
| --- | --- | --- |
| Reactor and Metal Press (the first route) | 0.4 (2.5 buckets a sheet) | 1.4 (0.71 bucket a sheet) |
| Monomer Preparation Unit, HDPE and the Polymer Extruder (the later route) | 0.4 | 1.9 (0.53 bucket a sheet) |

The later route stays ahead: the Monomer Preparation Unit now also cracks naphtha, 1,000 mB to 750 mB of ethylene (refinery gas gives 500), and 1,000 mB of ethylene makes four HDPE pellets, which the Polymer Extruder turns into eight sheets. In real refineries naphtha is the main feedstock for ethylene and plastics.

The proposal first suggested making refinery gas give 16 pellets. That alone would have made the first route four times better than the later HDPE route, which gives the same 4 sheets per bucket of refinery gas, so the change uses naphtha instead.

## Connections

- **Input producer:** the Distillation Tower (250 mB of naphtha per bucket of crude) and the Catalytic Cracker (300 mB per bucket of heavy fuel oil).
- **Output consumer:** plastic pellets and sheets, as before; ethylene feeds HDPE, LDPE and the PVC chain, as before.
- **Competing uses:** naphtha still reforms into gasoline in the Catalytic Cracker and cracks into butadiene in the Chemical Reactor; players choose.
- **Failure behavior:** unchanged.

## Balance and automation

Neither new recipe makes anything that turns back into naphtha or crude. The recipe audit in `check_mod_data` covers conversion loops and power loops; see Verification. The numbers are provisional and flagged for the owner.

## Multiplayer and persistence

Two new recipes (`jugcraft:polymerization/naphtha`, `jugcraft:chemical_reaction/crack_naphtha_to_ethylene`). No IDs, items or saved data change.

## Verification

- New server game test `PetroGameTests.reactorMakesPlasticPelletsFromNaphtha`: a bucket of naphtha gives four pellets and empties the tank.
- `PolymerGameTests.everyLoadedRecipePaysItsLedgerAndRetainsItsTool` runs every Monomer Preparation Unit recipe, the new naphtha cracking included.
- Local: `python3 tools/generate_material_data.py`, then `python3 tools/check_mod_data.py` passed (8025 material IDs, data files and recipe audit) and `python3 scripts/check_repository.py` passed.
- CI on the fork: pending.

Not run: a human running a plastic line.

## AI attribution

Implemented with Claude Opus 5.5 (Claude Code) for the contributor; the contributor is responsible for the output.
