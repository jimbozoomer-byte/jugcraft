# Arms VII: variant arms, crafted styles and boss trophies (batch 56)

Status: implemented on `claude/arms-variants`, awaiting review. Compiles and tests in CI only; **not yet played**.
Proposal issue: the owner, 4 October 2026, after the arms restyle ([arms-restyle.md](arms-restyle.md)): "refine and make more variants". Asked how, they chose:
- **New weapon items:** "they dont all need recipes yet a bunch can be drops from bosses ill make later you can theme them around them though example: Yeti King".
- **All four styles:** gilded, dieselpunk, bone & beast, runic glow.
- **Some craftable:** "make some for boss drops brainstorm bosses we could make also make some craftible".

Owner: jimbozoomer-byte
Target milestone and tier: steel age (the machines feature), on [arms.md](arms.md) to [arms-vi.md](arms-vi.md).
Primary specialty and supported player role: fighting; smithing for crafters; trophies for a future boss branch ([branches/BOSSES.md](../branches/BOSSES.md)).

## Player experience
32 named arms, each a variant of an existing kind with its own look and a perk or boon. All are in the creative Combat tab. Every one fights as its kind does: the same swing, reach, trait, two-handed blow, weapon art and motion.

**Crafted styles: 16 arms in four styles.** Each is made at a smithing table from:
- the style's pattern (a smithing template, crafted);
- a steel arm of the kind;
- the style's material.

The arm keeps its enchantments and wear.

| Style | Pattern (recipe) | Material | Arms | Perk |
|---|---|---|---|---|
| Gilded: polished steel, gold, royal-blue velvet, sapphires | Gilder's Pattern: 8 gold nuggets round paper | gold ingot | longsword, rapier, sabre, halberd | takes enchantments as gold does (22; steel's 12) |
| Ironclad (dieselpunk): gun steel, olive drab, hazard stripes, bolts, rubber grips | Ironclad Pattern: yellow and black dye round a steel plate | steel plate | zweihander, maul, war pick, battle axe | lasts twice as long (1,800) |
| Bonecarved: bone, horn, leather, a garnet eye | Bonecarver's Pattern: bone, flint, leather, paper | bone block | dagger, flail (a skull on a chain of vertebrae), glaive (a jawbone blade), labrys (shoulder-blade bits) | **Gravebane:** 20% harder against the undead |
| Runebound: void-dark steel with runes that glow cyan | Runecarver's Pattern: amethyst, ectoplasm, paper | ectoplasm | nodachi, moonblade, staff (quarterstaff), war hammer | **Mark:** a struck foe glows for 4 s, seen through walls |

**Boss trophies: 16 arms, two for each of eight bosses still to be made.** They have no recipe. Each boss's loot table is ready to drop one of its two (see [branches/BOSSES.md](../branches/BOSSES.md)). They last twice as long as steel, carry epic rarity, and have a boon:

| Boss | Trophies | Boon |
|---|---|---|
| the Yeti King | Glacier Maul (maul), Rimeclaw (katar) | **Frost:** Slowness II, 3 s |
| the Cinder Tyrant | Cinderbrand (greatsword), Magmaw (earthbreaker) | **Ember:** sets the foe alight, 3 s |
| the Mire Hag | Hagthorn (scythe), Bogfang (kama) | **Venom:** Poison, 4 s |
| the Crypt Lich | Soulreaver (moonblade) | **Drain:** each hit heals you half a heart |
| the Crypt Lich | Gravewarden (executioner) | **Wither:** Wither, 3 s |
| the Iron Dreadnought | Dynamo Halberd (halberd), Piston Hammer (war hammer) | **Shock:** arcs to the nearest other foe within 4 blocks, for 30% of the blow |
| the Alpha Werewolf | Moonfang (sabre), Howler (twinblade) | **Howl:** Weakness, 3 s |
| the Storm Roc | Stormcaller (glaive), Galefeather (estoc) | **Gale:** throws the foe up and back |
| the Abyssal Leviathan | Tidebreaker (war fork), Leviathan's Hook (bill) | **Tide:** 25% harder against a foe in water or rain |

**Looks:**
- Each arm is drawn with the restyle's toolkit: a pixel-art icon on the diagonal and a 3D model in the hand.
- Glowing parts are lit at full brightness in the hand, so they show in the dark: runes, magma, venom, soul fire, charged coils and lightning.
- Tooltips name the kind's trait or art, the boon (in aqua) and the line (in purple: the style's perk, or "A trophy of …").

## Connections
- **Existing input producer:**
  - steel arms (the steel foundry and the arms' own recipes);
  - gold, steel plates (the plate press), bone blocks;
  - ectoplasm (ghost hunting: catching restless spirits);
  - paper, dyes, flint, leather, amethyst.
- **Existing output consumer:** the player against mobs and, where PvP is on, players.
- **Technology connection:** steel and the plate press (ironclad); the arms' tiers.
- **Magic connection:** the runebound style needs ectoplasm, from the ghost-hunting branch.
- **Reachable entry path:** a steel arm, then the pattern's ingredients, then a smithing table. All existing and craftable solo. No circular unlock.
- **Required vs optional:** all optional.
  - The styles can be crafted solo or traded.
  - The trophies wait for their bosses. Until then they are creative-only, and that is on purpose: [branches/BOSSES.md](../branches/BOSSES.md) is a proposal, and no core progression needs a trophy.
- **How the specialty stays useful:** a style is a look and a small perk, not a stronger tier. The arms of batches 42 to 55 stay as good.

## Balance and automation
- **A variant's blow is its kind's in steel** (the same attack damage and speed). What differs is its line's perk or boon.
- **Every boon is bounded:**
  - an effect lasts at most 5 s at amplifier at most 1, and another hit refreshes it, never stacks it;
  - a share is at most half a blow.
- **`check_arms_variants`:** no variant deals as much a second as a netherite sword (12.8), even with its boon at its best. Its bonus share is counted, as are fire (1 a second), Poison (0.8) and Wither (0.5). The best is the Bonecarved Dagger against the undead, at 12.42. Gravebane was first a flat 2.5 and Tide 40%; the check caught those at 16.1 and 13.5, and they were made shares.
- **Shock** strikes only one other foe, and only for a player wielder and a foe that player may strike (allies, mounts and protected foes are spared, as the two-handed blows spare them).
- **Smithing:** each style variant costs the steel arm plus its material and pattern. Nothing is returned or recycled, so there is no conversion loop. The recipe audit counts a style variant as its steel arm plus its material's metal.

## Multiplayer and persistence
- **Server authority:** every boon is worked on the server, in `ArmItem.hurtEnemy` and `getAttackDamageBonus`, when the arm strikes. Clients only see the effects and particles.
- **Saved state:** none beyond ordinary items with stable ids:
  - the 32 variants: `jugcraft:gilded_longsword` … `jugcraft:leviathans_hook`, as in `tools/arms_variants.py`;
  - the four patterns: `jugcraft:gilders_pattern`, `ironclad_pattern`, `bonecarvers_pattern`, `runecarvers_pattern`.
- **Disabling the `machines` feature** removes the recipes, not the items.

## Dependencies and assets
- **No new dependencies.**
- **The art:** original, drawn by `tools/arms_variants_art.py` on the restyle's toolkit (`tools/arms_pixel.py`, which now also marks glowing materials' boxes with `light_emission`).
- **Generated by:** `python3 tools/generate_textures.py` and `python3 tools/generate_material_data.py` (`tools/arms_variants.py`).

## Verification
- **`python3 tools/check_mod_data.py`:** PASS (local). Its new `check_arms_variants` holds `weapons/ArmVariants.java` and `tools/arms_variants.py` together:
  - the variants in order, with kind, line and boon; every number;
  - each style's smithing recipe, and each boss's loot table (exactly its trophies);
  - the tooltips;
  - the bounds, and the per-second ceiling above.
- **`python3 scripts/check_repository.py`:** PASS (local).
- **Game tests** (`ArmsVIIGameTests`): pass in CI on ca45a035 (the `mod` job, with every other server test).
  - every variant is an arm of its kind, with its line's durability, enchantability, rarity and boon, and its kind's blow in steel;
  - Frost, Venom, Wither, Howl and Mark put their effect on a pig at their length and strength; Ember sets it alight;
  - Drain heals the wielder; Gale throws the foe up and away; Shock arcs to the near pig for its share and not to one beyond reach;
  - Gravebane adds its share on a husk and nothing on a pig; Tide adds its share in water and nothing on land;
  - every style recipe and pattern recipe loads, and each boss's table drops exactly its two trophies over 40 rolls.
- **Client game test** (`ArmsVIIClientGameTests`): passes in CI on ca45a035 (shard 0), and its shots show the variants in frames and on racks, trophies held by day, and the runes, magma and venom glowing at midnight. The Glacier Maul's blow left the pig at Slowness II with 43 ticks left. The first-person shot was blank: an earlier test in the shard leaves the GUI hidden, and hiding it hides the hand. The test now shows the GUI for that shot alone, as ArmsMotionClientGameTests does:
  - every variant and pattern in frames, and the variants on armor-stand racks;
  - trophies held from the front by day, glowing ones at midnight, and one in first person;
  - a Glacier Maul's blow with the real attack key, its frost read back from the server.
- **Not run:**
  - play;
  - two players;
  - how the boons feel against real mobs.

## World and event applicability
- **Loot rarity and abilities:** trophies are epic and styles uncommon. Abilities are bounded, as above.
- **Boss containment:** the bosses themselves are not built. [branches/BOSSES.md](../branches/BOSSES.md) lists the rules each must meet (arena, readable attacks, recovery, scaling, no griefing, no farming loop).
- **No seasonal content:** the Pumpkin King idea there would have to keep its drops after the season.

## Rollout and open questions
- **Each design is a first pass for the owner to judge.** They are easy to change in `tools/arms_variants_art.py`.
- **Which boss first, and with what arena?** See [branches/BOSSES.md](../branches/BOSSES.md).
- **More styles or kinds per style** can be added to the tables; each style's pattern is shared by its arms.
