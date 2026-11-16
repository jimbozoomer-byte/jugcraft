# Horse breeds

Status: integrated locally with selected combined Minecraft runtime, resource and independent-observer validation passed; see [the integration record](thanksgiving-expansion-nov14.md). Owner play review and publication remain separate.
Proposal issue: none (owner request, Thanksgiving batch 2)
Owner: the project owner
Target milestone and tier: travel and husbandry, available from the start of survival (wild herds); no research or season
Primary specialty and supported player role: travel, exploration and hauling, any player who can tame a horse

AI attribution: designed and written with Anthropic's Claude in Claude Code (the exact model is declared in the commit
trailers). The breed models, textures, saddles and bridles are the horses v2 art delivery (art/horse_breeds/README.md);
the saddlebags and spawn eggs were painted by script for this change.

## Player experience

Five horse breeds live in the wild, each with its own model, saddle and size, and each better at one job and worse at
another. Tame, saddle, feed, lead, ride and breed them exactly as vanilla horses; no research is needed.

| Breed | Id | Found wild | Good at | Worse at |
| --- | --- | --- | --- | --- |
| Icelandic Horse | `jugcraft:icelandic_horse` | snowy and windswept biomes, 2-4 | trail and snow: step +0.25, safe fall +2, walks on powder snow, never freezes, the lowest seat | speed x0.85, jump x0.9 |
| Mongolian Horse | `jugcraft:mongolian_horse` | plains and savannas, 2-4 | endurance: health x1.15, regains 1 health every 10 s once calm for 15 s, never freezes, step +0.1, safe fall +1 | jump x0.95 |
| Shire Horse | `jugcraft:shire_horse` | plains, rarer, 1-3 | cargo and health: saddlebags of 27 slots, health x1.2, knockback resistance +0.5 | the slowest (x0.75) and weakest jumper (x0.7), a wide hitbox |
| Giant Finnhorse | `jugcraft:giant_finnhorse` | taiga, rare, 1-2 | strength and stride: health x1.5, natural armour +4, knockback resistance +0.8, speed x1.1, step +0.25, saddlebags of 9 slots | clearance: a 2.625 x 2.875 hitbox (no doors, two-wide gates or low tunnels), jump x0.8 |
| Friesian Horse | `jugcraft:friesian_horse` | flowery biomes 2-3, plains 1-2 | road speed and jump: speed x1.15 (+10% more on road blocks), jump x1.2 | health x0.85 |
| Pegasus | `jugcraft:pegasus` | high mountain meadows, alone, rare | flight ([pegasus.md](pegasus.md)) | lighter cargo: a 9-slot pack on the saddle, no armour |

Multipliers apply to each horse's own vanilla-style roll (health 15-30, speed 0.1125-0.3375, jump 0.4-1.0), so two horses
of one breed still differ and a good Mongolian can outrun a poor Friesian. Every breed's spawn egg (creative Spawn Eggs
tab) explains its role, home and traits (hold Shift).

**Lineage card.** Use any breed horse with a stick to read its breed, ancestry, generation, parents and bag size.

**Horse armour** is not fitted to the breed models yet, so a breed's body slot is refused, as on the Pegasus.

### Saddlebags and cargo conservation

- **Fit:** the owner uses a chest on a tamed adult Shire or Giant Finnhorse (or a saddled Pegasus, whose small pack is
  strapped to the saddle). **Open:** the owner sneak-uses it with a stick (plain sneak-use stays vanilla's horse screen,
  as on the menagerie's mounts). Only the owner, where land claims and towns let them change things.
- **Storage:** every carrier stores 27 slots, saved with the horse in its own `JugcraftSaddlebags` compound (never the
  vanilla `Items` key). The menu shows its capacity: three rows for a pure Shire, one row for a Giant Finnhorse, the
  Pegasus pack and any cross that still carries bags.
- **Exactly once:** a pet recall and a knock-out store the whole entity in the Pets roster (a knock-out cancels the
  death, so nothing drops); a save and reload keep every slot; summoning brings it back once (the roster refuses a
  second copy and discards stale copies from unloaded chunks). Only a real death drops the chest and every stored stack,
  each removed from the bags as it drops.
- **Open menus:** the menu stays usable only while the horse is alive, present, within 8 blocks and still the player's;
  a recall closes nearby menus before the horse is removed, and vanilla ignores clicks in a menu that is no longer
  valid, so a recall, knock-out or unload with the bags open moves nothing. Recalling a ridden or leashed horse is refused
  by the Pets roster; a knock-out mid-ride throws the rider and keeps the cargo.
- **Smaller capacity never deletes** (a cross with fewer rows, an older save, a retuned breed): stacks stored past the
  capacity stay saved inside the horse until the owner next opens the bags. Then they move into empty visible slots, in
  order; whatever still does not fit is **handed back to the owner**: a copy of each stack goes into their inventory and
  whatever it does not take drops beside the horse (a creative player, whose full inventory would swallow a stack, gets
  every handed-back stack dropped whole instead), with a message saying how many stacks. A horse that can no longer carry bags at all gives back every
  stack and its chest when opened. Nothing is merged, split, doubled or voided (HorseCargo.plan; pure tests).
- **Armour and saddle:** the saddle is vanilla's equipment slot, saved and restored with the entity by recall, knock-out
  and reload. Breeds and the Pegasus refuse the body (armour) slot, so they carry no armour; armour already in that slot
  from elsewhere is kept by vanilla's equipment saving, not deleted.

### Crossbreeding

- **Who pairs:** the five breeds pair with each other; a Pegasus pairs only with a Pegasus. A vanilla horse or donkey
  never has a foal with a breed or a Pegasus (`HorseLineageMixin` refuses a pairing it starts; both lose their love).
- **Ancestry:** each horse records its share of each ground breed (out of 4096), its parents' breeds and its
  generation. A foal's share is the mean of its parents'.
- **The foal's breed** (look, size, saddle, tags) is the breed with the largest share; a tie is a coin toss among the
  tied breeds.
- **Traits** blend by share and are clamped to the range the pure breeds span, so no cross beats the best pure breed at
  anything. Saddlebag rows blend too and round **down** to a menu size: only a pure Shire carries three rows.
- **Base stats:** the parents' mean, moved by at most half their difference plus a tenth of the range, clamped to the
  vanilla ranges. No cross can drift past the ranges, however long a line is bred.
- **No breeding rewards:** breeding gives vanilla's breeding experience and nothing else.
- **Flight, one explicit rule:** a foal flies only when both parents are Pegasi; it is then a Pegasus with the line's
  fixed stats. Every other foal is a ground breed, and no ground horse can fly, whatever its ancestry.
- The breeder is told the foal's breed and ancestry.

## Giant Finnhorse rearing

The v2 art reported the giant's hind legs sinking 6.94 raw px into its rump when it reared, because vanilla's rearing
offsets are fixed for a horse half its size. `BreedHorseModel` overrides the protected hooks vanilla 26.3's
`AbstractEquineModel.setupAnim` calls (the supplied javap): for the giant only, the front legs rise 22 px (vanilla 12)
and come back 6 (4), the head rises 16 (8) and moves to z -6 (-4), and while it stands the body rises up to 8 px and
the hind legs step back up to 3 px, all linear in the stand amount (0 at rest), so the override is bounded and the rest,
walk and graze poses are vanilla's. Its saddle and saddlebags use the same rig.

Measured with the shared pose sweep's geometry over 87 rearing states (our transcription, not a game run): deepest
hind-leg sink 7.00 -> 3.26 raw px (vanilla 2.92; 5.26 allowed when scaled to the giant), tail against the hind legs
5.47 -> 3.61, front legs still attached to the chest (0.92 px, the art's own limit is 1.01), the neck root attached, no
new floor dips, tack crossing 0.91 px at the extreme head turn (vanilla 0.58, scaled 1.14).

## Connections

- Existing input producer: vanilla horse taming and its foods, the vanilla saddle, a vanilla chest for saddlebags.
- Existing output consumer: travel and hauling across every branch; the Pets roster, the stable screen, recall,
  knock-out and level bonuses treat a tamed breed exactly as a tamed horse (`Pets.owner` via `OwnableEntity`).
- Reachable entry path: wild herds in common biomes from the first day; the Pegasus on mountain meadows.
- Required vs optional: optional; nothing depends on them.

## Balance and automation

No breed is best overall: speed (Friesian) costs health, health (Giant) costs clearance, cargo (Shire) costs speed and
jump, terrain (Icelandic) costs road speed, sustain (Mongolian) leads nothing else. Trait values are starting points in
`horses/HorseBreed.java`. Saddlebags are storage, clearly: 27/9 slots, owner-only, protected by claims, dropped on death.
No item conversion, no breeding reward, no loop. Wild horses drop 0-2 leather (vanilla's horse table), once.

## Multiplayer and persistence

The server decides everything: traits are attribute modifiers with fixed ids, recomputed from the saved ancestry on
every load (never stacked); saddlebags change only on the server, for the owner. **Road speed:** the server syncs the
effective road bonus (the ancestry's, so a Shire-looking half Friesian has 0.05) in the entity's data whenever it applies
traits, so late tracking and reloads carry it. The bonus is applied only where the horse is moved: by its rider's own
client while a player rides it (vanilla's ridden-horse model), otherwise by the server, which drops its own modifier
once a player takes over; neither side ever removes the other's. Saved per horse: `JugcraftAncestry` (text,
validated on load: anything damaged reads as the breed's pure line), `JugcraftParents`, `JugcraftGeneration`,
`JugcraftBase` (the rolled base, three numbers), `JugcraftBagsFitted` and `JugcraftSaddlebags`. Removing the feature
would need a migration for saved `jugcraft:<breed>` entities.

## Dependencies and assets

No new dependency.
- Models: generated Java (`client/horses/BreedHorseMeshes.java`) from `art/horse_breeds/` by `tools/horse_breeds.py`
  (also `--check`); each adult layer scaled once with `MeshTransformer.scaling(1.1F)` as vanilla's horse; foals at 0.55.
- Sizes: `horses/BreedDimensions.java`, measured from the models against vanilla's horse (body length -> width, withers ->
  height, saddle top -> rider seat). The living 26.3 horse reference seat is 1.44375 blocks; the skeleton/zombie horse's 1.31875 is not used.
- Textures: the v2 PNGs byte for byte; saddlebags and eggs from `tools/horse_breeds_art.py`.
- Lang, items: `tools/horse_breeds_data.py` through `generate_material_data.py`.

## Verification

Current local integration: common/client/test compilation, 923,714 lineage assertions, eight final horse-resource checks and all horse server cases in the final 440-test combined cohort pass. Runtime review caught and fixed a two-pixel rider offset; the generator's reference seat is now compared with the live vanilla horse type. Corrected rider/movement galleries and two independent client JVMs pass. Codex inspected all five unobscured breed views plus grounded/airborne Pegasus, including fitted saddle/bags and rider alignment. Nested test callbacks were queued up front without changing their timing after the pinned test scheduler exposed an iterator crash. See [current combined evidence and limits](thanksgiving-expansion-nov14.md).

The following are the original worker's pre-integration checks:

Ran:
- `HorseLineageTests` (pure Java, no Minecraft classes) compiled and run with a local JDK 21 outside Gradle: every
  assertion passed (see the delivery logs for the count). Gradle task `horseLineageTest` not run.
- `python3 tools/horse_breeds.py --check`: the generated files match the sources.
- A tree-sitter parse of every Java file changed (syntax only).
- Generated previews (Python box renderer), labelled as such: render-reviewed, runtime pending.

- `python3 -m unittest discover -s scripts/tests -p test_horse_breeds_resources.py`: textures equal the PNGs their
  models embed at one texel a model pixel, whole-pixel cubes, eggs, loot, lang, tags, generated ids.
- `tools/check_pegasus_model.py` after the wing refinement: 0 problems.

Written, not run:
- `HorseBreedsGameTests` (server): registration, sizes, tags, the roll and traits applied once, save and reload, recall,
  knock-out mid-ride, recall with the bags open, an unloaded recall and a stale copy, a smaller capacity, overflow into
  full, partly full and creative inventories, owner-only bags and death drops, crossbreeding and the flight rule, road
  speed (also a Shire-looking half Friesian), recovery, the Pegasus pack.
- `HorseBreedsClientGameTests` (rendered client): the tacked row from two sides, walking, rearing, grazing, the player
  seated on each breed (compared with a vanilla horse ridden the same way), an observer watching a husk ride each breed
  (its client copy carries the rider as the server does), and a ridden half Friesian on a road whose road bonus matches
  on client and server, after late tracking and after a save and reload. Screenshots `jugcraft_horse_breeds_*`.
- `PegasusClientGameTests` extended: the light pack drawn throughout, the wings flapping while climbing, folded after
  landing (`jugcraft_pegasus_folded_with_pack`); `PegasusGameTests`' breeding test updated to Pegasus-only pairing.

## World and event applicability

Natural spawning in the overworld biomes above; no structures or seasons. Ownership and protection are vanilla horse
rules as Jugcraft already extends them.

## Rollout and open questions

- **Proposals not implemented:** horse armour fitted to the breed models; a second (camel-style) seat on the Shire;
  stamina, turning rate, acceleration and feed cost; snow-layer and packed-ice speed; village trades for the rare breeds;
  wingless half-Pegasus crosses.
- **Implemented defaults for local review:** spawn biomes and weights, bounded trait values, the giant's 2.625-wide hitbox, a nine-slot Pegasus pack and Pegasus-only winged breeding. These are adjustable after the owner's play review.
