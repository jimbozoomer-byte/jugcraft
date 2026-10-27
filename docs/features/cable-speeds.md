# Industrial cable speeds follow their metal

Status: implemented on the contributor's fork, pending review.

Proposal issue: none. The contributor, 27 October 2026, on reading the cable speeds: two copper cables at different speeds, and iron carrying more than copper, gold and silver, make no sense against real conductivity or how rare each metal is.

Owner: jimbozoomer-byte
Target milestone and tier: power cables, early to mid technology
Primary specialty and supported player role: factories; anyone wiring machines

## Player experience

Every power cable carries Jugcraft Energy and connects to every other. The Jimtafacts (Industrial) cables kept their source mod's speeds, set by its voltage classes rather than by their metal. Two of them did not fit beside Jugcraft's cables:

| Cable | JE per tick before | After |
| --- | --- | --- |
| Industrial Tin (and Insulated) | 32 | 32 |
| Industrial Iron (and Insulated, Double and Quad Insulated) | 2,048 | **64** |
| Industrial Copper (and Insulated) | 128 | **256**, the same as Jugcraft's copper cable |
| Jugcraft Copper | 256 | 256 |
| Industrial Gold (and Insulated, Double Insulated) | 512 | 512 |
| Jugcraft Silver | 1,024 | 1,024 |
| Jugcraft Aluminum | 4,096 | 4,096 |
| Industrial Glass Fibre, Detector, Splitter | 8,192 | 8,192 |

- **Copper** is one metal and now one speed.
- **Iron** is about as poor a conductor as tin and cheap to make (three ingots and a nugget give six), so it now sits just above tin instead of second from the top.
- The rest stay. Silver is the best conductor of any metal, so it stays above gold; gold, though a worse conductor than copper, stays above it because it is rarer. Aluminum conducts less well than copper but is light and is what real long-distance lines use, and in Jugcraft it comes late (bauxite and electrolysis). Glass fibre stays the top cable because it needs diamonds.

Insulation still changes only thickness and dye. The double- and quad-insulated iron cables now differ from plain iron only in look.

## Balance and automation

A line of iron cable that fed a large machine now carries at most 64 JE a tick. Builds that relied on it should switch to gold, silver, aluminum or glass fibre cable. The numbers are flagged for the owner.

## Multiplayer and persistence

No IDs, recipes or saved data change. Placed cables keep working at their new speed.

## Verification

- New game test `JimtaWorkshopTableGameTests.industrialCablesCarryWhatTheirMetalDoes`: Industrial copper carries Jugcraft copper's rate, every iron cable carries 64, and tin < iron < copper.
- The existing splitter and detector tests still use the copper and splitter cables.
- Local: a tree-sitter parse of the changed Java found no syntax errors; `python tools/check_mod_data.py` (8,025 material IDs) and `python scripts/check_repository.py` passed.
- CI: pending.

Not run: a human wiring machines with these cables.

## AI attribution

Implemented with Claude Opus 5.5 (Claude Code) for the contributor; the contributor is responsible for the output.
