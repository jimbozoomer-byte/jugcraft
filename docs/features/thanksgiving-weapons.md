# Thanksgiving weapons: colonial and woodland arms (Thanksgiving batch 2, worker A)

Status: integrated locally with Codex repairs; common/client/test compilation and all selected server weapon regressions pass within the 440-test combined cohort. The clean client gallery passes for all seven selected weapons; Codex inspected actual held/use and first-person captures. Owner play review remains pending. Original worker checks below are historical, not the final combined result. [Combined evidence](thanksgiving-expansion-nov14.md).
Proposal issue: the owner's Thanksgiving batch 2 (14 November 2026): "blunderbuss; flintlock pistol; musket; naval
cutlass; stone-headed hatchet or tomahawk; woodland hunting bow; hunting spear", each added **only where no equivalent
exists**, otherwise refined or reused.

Owner: jimbozoomer-byte
Target milestone and tier: the steel age's arms (the `machines` feature), beside the sabre; iron and brass.
Primary specialty and supported player role: fighting, aboard boats and ships above all.

## The inventory
Every candidate was checked against `main` (`guns/JugcraftGuns.java` and `tools/guns.py`, the arms in
`weapons/JugcraftArms.java` and `tools/arms.py`, the cavern and wilds weapons, vanilla's items) and against the pirate
(`claude/nov10-pirate-ship`) and raider-crew (`claude/holiday-raiders-pets`) branches, neither of which has a cutlass,
pistol or musket of its own (the pirate crews carry golden swords). `tools/thanksgiving_weapons.py` `INVENTORY` holds
the same table, checked by `scripts/tests/test_thanksgiving_weapons.py`.

| Candidate | Decision | Item | Why |
|---|---|---|---|
| Blunderbuss | Covered | `jugcraft:bellmouth` | The owner's own flintlock blunderbuss (model and animations; `tools/guns.py` source `blunderbuss`): ten balls a shot from one Paper Cartridge, 12° hip and 9° aimed spread, 20 blocks, a 78-tick reload. |
| Flintlock pistol | Covered | `jugcraft:duelling_pistol` | The owner's own flintlock pistol (source `flintlock_pistol`): one heavy ball, a 74-tick muzzle reload. |
| Musket | Covered | `jugcraft:line_musket` (and the repeating `windlass_musket`) | The owner's own flintlock musket (source `musket`): one heavy ball, rammed home in 78 ticks. |
| Naval cutlass | **New** | `jugcraft:naval_cutlass` | No cutlass or naval sword exists: the sabres (bronze, steel, gilded, Hessian, the Moonfang trophy) are horseman's blades, 3 more damage from the saddle. |
| Stone-headed hatchet or tomahawk | **New** | `jugcraft:stone_headed_hatchet` | Stone-tier chopping and short tumbling throws, using the shared francisca animation/projectile with its own art and numbers. |
| Woodland hunting bow | **New** | `jugcraft:woodland_hunting_bow` | A 17-tick wooden bow with slower arrows, own draw sprites, and vanilla ammunition/enchantments. |
| Hunting spear | **New** | `jugcraft:hunting_spear` | Stone-tier 3.5-block jabs and straight throws through the existing javelin system, with its own art. |

The covered guns already keep the owner's rules for early guns (checked again here, see Verification): each holds one
round and spends one Paper Cartridge a shot (the Bellmouth's ten balls from one cartridge); each loads slowly (74 to 78
ticks, never by the round); every shot is a hit-scan that stops at the first block and breaks none (only the energy
Seam Cutter cuts blocks, as its shooter could by hand); every shot asks `AttackEntityCallback` as the shooter
(`GunShots.allowed`), so land claims, the town and the PvP rules apply; and ammunition is the held-gun HUD and the
tooltip, never a durability bar (`GunItem.isBarVisible` is false). They are the owner's own models, textures and
animations; nothing of them was changed.

The woodland designs use ordinary flint, wood and sinew motifs. They are not presented as a reconstruction of a particular nation or ceremonial object.

## Player experience: the Naval Cutlass
A sailor's short, broad, gently curved blade of iron with a brass shell guard and knuckle bow round a dark leather grip,
for fighting aboard. In the Combat tab; repaired with iron ingots; takes a sword's enchantments.

- **In the hand:** 5 a blow at 1.8 blows a second (9.0 a second; the iron sword's 6 at 1.6 is 9.6), reaching 2.75
  blocks, a quick whacking swing. It is swung with the sabre's motion clips.
- **Boarding Blade (its trait):** a blow deals **2 more** while its wielder or its foe is in water or aboard a boat
  (12.6 a second afloat). Ashore it is a little below the iron sword; at sea it is the better blade.
- **Parry:** use (hold right-click) to raise its shell guard: it stops **40%** of a blow from in front, as the
  longsword's parry stops 60%.
- **Wear:** 250 uses, as iron's.
- **Tooltip:** "A sailor's short, broad blade: quick cuts, and 2 more damage while you or your foe is in water or aboard a
  boat. Use to parry with its shell guard: blocks 40% of the damage from in front." under the trait name Boarding Blade
  (shown with Shift, as every arm's trait is).

## Connections
- **Input:** 2 iron ingots, a brass ingot (`#c:ingots/brass`) and leather, in a crafting table (the blade up the
  diagonal, the brass guard and the leather grip at its foot). The recipe follows the `machines` feature, as the arms'.
- **Output consumer:** a weapon; no machine takes it.
- **Not a gate:** no research or calendar lock.

## Balance and automation
- Per second ashore it stays below vanilla's iron sword (as the arms stay below their metal's sword); afloat it is
  12.6. Its parry is weaker than the longsword's; its reach shorter than the hand's.
- Essence value: its recipe's cost, 1217/2 (608.5), recorded in `data/jugcraft/alchemy/values.json` from the recipe
  (two iron ingots 256 each, a brass ingot 385/4, leather 1/4). Above 128 essence, so it stays crafting-only: no tablet
  output, no shop, no material conversion. Selling it back returns exactly what it was made of: no profit loop.

## Multiplayer and persistence
Plain item components; no saved data of its own. Every blow goes through vanilla's attack (`Player.attack`), so land
claims (`LandProtection`'s attack and damage checks), the protected town and the PvP rules treat it as any sword. It
adds no attack path. New id only; nothing renamed or removed.

## Dependencies and assets
No new dependencies.
- `weapons/NavalCutlassItem.java`: an `ArmItem` built by `JugcraftArms.arm()` from its own `Kind` ("cutlass": whack 6
  ticks, reach 0 to 2.75, parry 0.4, a sword); `kind()` answers "sabre" so `client/arms/ArmsMotion` swings it with the
  sabre's clips (its tooltip and trait stay its own); `getAttackDamageBonus` adds Boarding.
- `weapons/NavalCutlassStats.java`: its numbers and their arithmetic, free of game types (checked outside Gradle).
- `tools/thanksgiving_weapons.py`: the inventory, the cutlass's table, its 3D model and texture, display, item
  definition and recipe. Run `python3 tools/thanksgiving_weapons.py` (or `--check`).
- Icon: `tools/item_icons/naval_cutlass.txt`, a 16x16 map in the owner's manner (`docs/ITEM_ICONS.md`; materials iron,
  brass, dark wood and leather), drawn by `tools/item_icons.py` to `textures/item/naval_cutlass.png`.
- 3D model `models/item/naval_cutlass_in_hand.json` with `textures/item/naval_cutlass_model.png` (32x32): twelve
  axis-aligned boxes at one texel a model pixel (a blade four pixels broad in two long runs with one gentle step
  back between them, its back clipped a pixel a row to the point on the edge's line; a dark back and a lit edge; guard
  bar and shell dish,
  rear quillon, knuckle bow and its foot, leather grip, brass pommel), each face painted in flat bands (no noise, no
  pinstripes), every part overlapping the next by half a pixel (the knuckle bow's foot meets the bow face to face)
  so nothing floats and no two alike-facing faces share a plane. It is held where and
  as the sabre is: its display is solved from the sabre's (`display()`), not tuned by eye.
- The item definition shows the icon in the GUI and the 3D model everywhere else (hands, the ground, frames, shelves).

## Verification
- **Worker-run before final integration (Python):** `scripts/tests/test_thanksgiving_weapons.py` (the generator reproduces every committed file; the
  icon map passes `tools/check_icon_maps.py`; the model's boxes are axis-aligned, closed, opaque and one texel a pixel,
  and pass `tools/art_check.py`'s shared-plane, near-plane, UV, hole and backwards-box rules; the parts touch; the
  display puts the grip and blade where the sabre's are in every hand; the numbers match the Java; the inventory names
  real items; the recipe, lang, sword tag, essence value and registration are in place; the covered guns keep their
  rules).
- **Compiled and run with JDK 21 outside Gradle (pure logic only):** `NavalCutlassStats` (blow, rate, per second ashore
  and afloat, boarding, parry) and the covered guns' `GunSpec.reloadTicks`, against a small harness (not committed).
- **Written, not run in Minecraft:** `test/ColonialWeaponsGameTests` (the cutlass's components, its 40% parry, its
  boarding bonus aboard a boat and against a foe in water but not ashore, claims and PvP for its blows; the covered
  guns' one cartridge a shot and slow reload, no block broken by a shot, claims and PvP for their shots).
- **Not run:** Java compilation, the server and client game tests, in-game play and screenshots.

## World and event applicability
Seasonal in theme only: nothing is gated on the calendar; nothing spawns.

## Rollout and open questions
- Runtime screenshots of the cutlass in hand, on the ground and in a frame are the owner's acceptance; the previews are
  generated renders only.
- Proposed, not implemented: the pirate crews (`claude/nov10-pirate-ship`) could carry the cutlass instead of a golden
  sword; their code is the pirate branch's and the raids' are the owner's, so it is left to them.
- Implemented locally: Stone-headed Hatchet, Hunting Spear and Woodland Hunting Bow, with their own flint/wood/sinew art and survival recipes. The hatchet chops as a stone axe and tumbles when thrown; the spear reaches 3.5 blocks and throws straighter; the 17-tick bow trades a quicker draw for slower arrows. These reuse Arms projectile, bow, claim and animation paths. `HuntingWeaponsGameTests`, the colonial ownership regression and `ThanksgivingWeaponsClientGameTests` are written; combined validation follows.
