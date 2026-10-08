# Armor designs of 8 October: Berserker, Paladin, Templar, Sentinel and Frost Knight

Status: implemented on `claude/epic-pascal-f8gpfg`, after the four designs of the day before ([four-armor-designs.md](four-armor-designs.md)) and stacked with them on `claude/pharaoh-armor` (#246, itself on #241 and #240), since the sets share the lists they are added to. **Not yet compiled, game-tested or played:** the Java compiles only in CI, which has not run on this branch.
Proposal issue: none. On 8 October 2026 the owner sent three more designs of their own ("Those are fantastic lets do these 3 next and I'll upload more that I want in here as well", then "I made these 3"): a Blockbench sheet of a horned red-and-white set, worn and piece by piece, and two renders each of a white knight with purple cloth and of a dark knight with red cloth. The brief is the one they gave the day before: "just create the armors for the modpack and make them usable in game", crafting and the rest "decided later". While these were being built they added: "Make sure you are making complex models for the armor using the most of what our mod has to offer really capturing the crazy unique geometry of each armor", so every shape below is built as its own box, plate or relief rather than painted on a flat one. Later the same day they sent two more, three renders of a gold-and-black knight with a sword and a star-shaped shield and two of a white knight crowned with ice with a glowing ice sword: "Just made these ones aswell want them done weapons too please". Their two swords are arms of the owner's armor sets ([arms-vii.md](arms-vii.md#the-sentinels-and-the-frost-knights-arms)); the shield is not built yet.

Owner: jimbozoomer-byte (design: the owner; implementation: Claude Opus 5.5).
Target milestone and tier: five new armor-only tiers beside the owner's other nine ([bloodthorn-armor.md](bloodthorn-armor.md) and the sets after it). Their place in the game, and how they are obtained, are the owner's to decide later; until then they are creative-only.
Primary specialty and supported player role: combat (defense). Everyone who wears armor.

**The names are placeholders.** The pictures carry no names, so each set is named for its look: Berserker (the horned set), Paladin (the white knight), Templar (the dark knight), Sentinel (the gold-and-black guard) and Frost Knight (the white knight crowned with ice). The swords take their sets' names. Nothing has been released, so renaming one now is a find-and-replace and a regeneration; once released, the IDs stay.

## Player experience
Each set is worn as a 3D model on the knight armor's toolkit ([knight-armor.md](knight-armor.md)), built from the owner's pictures part for part, and has four 16 × 16 icons of its own. The pictures show the front and a little of one side; the backs are drawn in each design's own words. Preview them with `python3 tools/armor_preview.py --set <set>` (`berserker`, `paladin`, `templar`, `sentinel`, `frost_knight`).

### Berserker (`tools/berserker_armor.py`)
- **Helmet:** an open-faced white cap with a rim round its foot; a lump of red on its crown, a smaller lump on that, the red spilling down over the brow in two drips; on each side a square horn standing straight up from a short foot, a narrower tip at its top and a grey band round its root; under the cap, round the jaw, a thin white frame: a post down each front corner, a bar back along each side of the jaw and a post up from its end, and the chin bar with four teeth standing up from it. The face shows, as drawn.
- **Chestplate:** a white breastplate keyed in grey, with a red band raised on each side of it, down its front, over the shoulder and down its back; a grey hoop and a red hoop round the waist, the red parted at the middle; stepped pauldrons: a red block on a grey-and-white trim, a raised step and a crown on its outer part and a white ridge along its inner edge; white bracers on the forearms, a flange at each end and two studs on the outer side. The upper arms are bare, as drawn.
- **Leggings:** a grey-topped red belt with a white buckle; on each leg dark grey mail under four thigh bands, white, red, white, red, each stepping further out down the thigh, the first wrapping the whole thigh up under the belt.
- **Boots:** grey boots with a white cuff, a dark band at the ankle, a red sole and a red toe cap.

### Paladin and Templar (`tools/crusader_armor.py`)
The two share one build below the neck, as the owner's do, and differ in colour and in their helms. The Paladin is white over dark mail with purple cloth and blue gems; the Templar slate with pale reliefs and dark red cloth.
- **Helmet:** a great helm with a row of four rivets down each side.
  - *Paladin:* its visor plate carries a raised white H, two uprights and a bar across at the eyes, over a dark breath; its sides dark mail; a white comb along the crown, stepped down behind, and behind it a purple sprig, a stem with two bars across it like a cross.
  - *Templar:* its visor barred with three raised ribs; a pale gable over the brow, two bars meeting at a peak and reaching out and down past the helm's sides; and a pale crest from the crown: a stalk leaning forward, a bend, and a flag tipped back.
- **Chestplate:** a breastplate shaped like a heater shield, in three steps narrowing to its foot, with a cross raised on it (white on the Paladin, pale on the Templar, whose collar shows red cloth); a back plate with a spine ridge; two hoops round the waist; square pauldrons carrying a square spiral, a dome on top and the spiral raised as a boss on the outer side, two lames fanning out under them; a mail sleeve, an elbow band with a spiral fan plate on its outer side, a banded vambrace, a cuff ring at the wrist and a glove.
- **Leggings:** a belt and buckle (the Paladin's with a blue gem); on each leg a cloth band round the hip, the cloth round the leg and three flaps (before, behind and outside it) flaring out to the knee; over them a front tasset and a side tasset, both hinged out from the hip, the front one with a raised spiral boss (the Paladin's with a blue gem).
- **Boots:** a banded greave with a knee cop, a sabaton with two instep lames overlapping toward the toe and a pointed toe.
- Closed all round: no part of the wearer shows, standing, walking or sneaking, from four views.

### Sentinel (`tools/sentinel_armor.py`)
The design is not symmetric (its pauldrons differ, and only the left thigh's plates are gold), so the left arm and leg are built as drawn rather than mirrored.
- **Helmet:** a gold bucket helm with a keyhole on its face: a dark window at the eyes in a raised pale frame (its top, its sides and its feet) and a dark slit down to the chin between two raised rims; on its crown a loop like a little house, two posts under a peaked roof; behind, a pale ridge down its middle between the two dark hooks of a meander; scratches on its left side, as drawn.
- **Chestplate:** a near-black coat under a gold mantle across the shoulders, and a dark baldric from the right shoulder to the left hip. On the right shoulder a great pauldron: a cap with a raised crown and three gold shells down the outer arm, each further out and further down and hinged out at its foot, the inner two riveted along their feet; a gold square ring on the upper arm. On the left shoulder a low crown and two flat gold plates in brown stripes, the lower reaching further out. Black sleeves with two gold bands each and gold gauntlets.
- **Leggings:** a dark belt; on each leg black hose, the coat's black skirt to the knee and a plate before and behind the thigh, hinged out from the hip: gold on the left, dark iron on the right.
- **Boots:** a gold cuff, a gold greave with a brown band, two instep lames overlapping toward the toe, a gold sabaton and a pale toe cap.
- Closed all round: no part of the wearer shows, standing, walking or sneaking, from four views.

### Frost Knight (`tools/frost_knight_armor.py`)
- **Helmet:** a frosted white helm whose face is a skull's grin: a dark mask, a white bar down the nose, four white teeth over a white jaw bar, all raised; a band round the brow set with ice; on the brow a crown of five ice crystals, each one to three stacked prisms turned on their edge and tapering, the middle one 7.5 pixels above the head and the outer ones leaning out; a mane of frost: four spikes out from each side, three of them flat blades with narrower tips, and two blades behind.
- **Chestplate:** a white cuirass with two raised chest plates and a plate below them; a navy strap from the left shoulder to the right hip, before and behind. On the left shoulder a navy pauldron with a dome, a white curl on its outer side and a white trim. On the right shoulder a mass of white frost, six spikes out of it and a flap down the outer arm with two more; behind that shoulder a white frost mantle, hinged out, three spikes out of its edge and three hanging from its foot. The forearms are bare, as drawn.
- **Leggings:** a navy belt with an ice gem at its buckle; on each leg a white cuisse, two lames over its front hinged out from their tops, and a knee cop.
- **Boots:** a white cuff at the knee, a white greave with two frost spikes at the outer ankle, the sabaton and a toe cap.
- The forearms aside, closed all round.

### Their weapons
- **Sentinel Longsword** (`jugcraft:sentinel_longsword`): a broad gold blade pale along its edge, a straight gold crossguard with square ends set with a jet square, a brown grip, a gold pommel with a jet stone. It fights as a longsword, and its boon is **Mark** (a struck foe glows for 4 s).
- **Frost Knight Greatsword** (`jugcraft:frost_knight_greatsword`): a long blade of glowing ice, a crossguard of white frost flaring into jagged spikes with an ice gem, a white grip and an ice pommel. It fights as a greatsword, and its boon is **Frost** (Slowness II, 3 s).
- Both are Arms VII set arms ([arms-vii.md](arms-vii.md#the-sentinels-and-the-frost-knights-arms)): epic, twice as hard-wearing as steel, a 3D model in the hand and a 16×16 icon of their own, creative only.
- **The Sentinel's star-shaped shield** is not built yet: shields are a different kind of item (Arms VI's), and a set's shield needs a path of its own.

### Shape, size and colour
| | Berserker | Paladin | Templar | Sentinel | Frost Knight |
|---|---|---|---|---|---|
| Parts | 77 | 83 | 80 | 62 | 94 |
| Quads: helmet, chestplate, leggings, boots | 132, 146, 65, 54 | 89, 167, 120, 68 | 94, 167, 105, 68 | 68, 143, 52, 80 | 197, 201, 59, 66 |
| Quads, full set (cap 900) | 397 | 444 | 434 | 343 | 523 |
| Atlas | 128 × 128 | 128 × 128 | 128 × 128 | 128 × 128 | 128 × 128 |
| Reach: to the side, above the head | 9.4, 5.8 | 10.3, 5.2 | 10.3, 6.0 | 12.5, 4.1 | 12.0, 7.5 |

Two pass the toolkit's warning lines (12 to the side, 6 above the head), as the owner drew them: the Sentinel's outermost pauldron shell reaches 12.5 pixels to the side, and the Frost Knight's tallest crystal rises 7.5 above the head. Either may pop out at the very edge of the screen.

Colours (`tools/armor_paint.py`): `BERSERKER`, `PALADIN`, `TEMPLAR`, `SENTINEL` and `FROST_KNIGHT`, sampled from the renders. In the Paladin's and the Templar's palettes "gold" names the Paladin's blue gems and the Templar's near-whites, and "leather" the cloth; in the Sentinel's "leather" is the browns of its engraving and the under-layer its black cloth; in the Frost Knight's "gold" is the ice's two cyans, "leather" the navy, and five more ice tones colour the crown by name.

**Toolkit additions** (`tools/armor_paint.py`): the `chevron` painter's corner `"o"` draws concentric square rings about a core (the owner's square spirals), and a `checker` painter draws mail. No earlier set's paint changes.

### The numbers
`tools/gear.py` `ARMOR_TIERS`, `JugcraftGear.BERSERKER_ARMOR` and the four after it: beside the owner's other sets in other strengths, each after its look. These are starting numbers for the owner to set; nothing was asked of them yet.

| | Berserker | Paladin | Templar | Sentinel | Frost Knight | Netherite |
|---|---|---|---|---|---|---|
| Defense (helmet, chestplate, leggings, boots) | 3, 8, 7, 3 (21) | 4, 8, 6, 3 (21) | 3, 9, 7, 3 (22) | 4, 8, 7, 3 (22) | 3, 8, 7, 3 (21) | 3, 8, 6, 3 (20) |
| Toughness (each piece) | 3.0 | 3.0 | 3.5 | 3.5 | 3.0 | 3.0 |
| Knockback resistance (each piece) | 0.15 | 0.1 | 0.15 | 0.15 | 0.15 | 0.1 |
| Durability (multiplier) | 495, 720, 675, 585 (45) | 517, 752, 705, 611 (47) | 473, 688, 645, 559 (43) | 506, 736, 690, 598 (46) | 484, 704, 660, 572 (44) | 407, 592, 555, 481 (37) |
| Enchantability | 16 | 22 | 14 | 20 | 18 | 15 |
| Fire resistant | no | no | yes | no | no | yes |
| Repaired with | quartz | amethyst shards | netherite ingots | gold ingots | blue ice | netherite ingots |
| Equip sound | iron's | diamond's | netherite's | gold's | diamond's | netherite's |

**Getting it:** for now only from the creative tab (Combat). No recipe, drop or trade exists, as the owner asked.

## Connections
- Existing input producer: none yet (creative only). The repair items come from vanilla (quartz, amethyst shards, netherite ingots, gold ingots, blue ice).
- Existing output consumer: the player's armor slots; each piece enchants, trims and equips as any armor of its slot (it is in `#minecraft:head_armor` and the other slot tags).
- Technology connection: none yet. Magic connection: none yet.
- Reachable entry path: not yet, by the owner's decision. Nothing requires these sets, so nothing is blocked on them.
- Which connections are required vs optional: all optional.

## Balance and automation
- **At or above netherite,** so they must come late. Their recipes or drops are the owner's to set, and should cost at least a netherite set's worth, whatever mends them.
- **No conversion loops:** nothing turns them back into materials.
- **Starting numbers,** not played.

## Multiplayer and persistence
- **Plain armor items:** nothing new is saved, and the server handles them as any armor (`JugcraftGear.armorTier`).
- **The 3D models are client-side only** (`client/WornModelLayer`), drawn from `assets/jugcraft/worn_models.json` (45 new entries; none of the earlier ones changed) and the atlases `textures/entity/equipment/3d/<set>.png`.
- **IDs:** `jugcraft:<set>_helmet`, `_chestplate`, `_leggings` and `_boots` for `berserker`, `paladin`, `templar`, `sentinel` and `frost_knight`, the repair tags `jugcraft:repairs_<set>_gear`, and the swords `jugcraft:sentinel_longsword` and `jugcraft:frost_knight_greatsword`. Placeholder names (above) until the owner confirms them; stable once released.
- **The swords** are ordinary items: their boons are worked on the server when they strike (`ArmItem`), as every Arms VII arm's are.
- **No equipment asset files:** every piece is drawn in 3D, so no `equipment/<set>.json` is written.

## The shared parts it uses and adds
- Each set: one `ARMOR_TIERS` entry, one Java material and one `armorTier(...)` call, a row in `ArmorTiersGameTests`, a palette and four icon maps; its module in `armor_models.SET_MODULES` (the Paladin and the Templar share one). `ArmorTiersClientGameTests` shows every tier, so it shoots these with no change.
- The two painters above.
- **Arms VII:** two set lines (`sentinel`, `frost_knight`) and their arms in `tools/arms_variants.py` and `ArmVariants`, their designs and materials in `tools/arms_variants_art.py`, two icon maps in `tools/arms_icons/`. `ArmsVIIClientGameTests` holds both from the front, and moves the patterns' frames to the bottom row of its wall.

## Dependencies and assets
- No new dependencies.
- The designs are the owner's; their pictures are not committed. Every model, texture and icon here is drawn by code from them: the four modules, the five palettes, `tools/armor_icons/<set>/`, the two swords' designs and their icon maps. Nothing is copied or traced from any mod or from vanilla, and no pixel of the owner's pictures is in the textures.
- The owner's asset library ([art/owner-library/README.md](../../art/owner-library/README.md)) was searched first, as for the four sets before: none of its armor is these designs, so nothing was taken from it.

## Verification
**Run locally on 8 October 2026,** on this branch:
- **Generators:** `generate_material_data.py`, `generate_textures.py`, then `generate_material_data.py` again, all exit 0. Besides this work's files, `generate_textures.py` redraws the same 24 flower and entity textures it redraws on main ([four-armor-designs.md](four-armor-designs.md)); they are left as committed.
- **`check_mod_data.py`:** PASS (1596 material IDs), with `check_armor_tiers`, `check_arms_variants` and the art check (`tools/art_check.py`): no new findings for the 45 new entries (holes H1, flicker Z2 and N2) or the swords' models; its allow-list is unchanged. A run before the last fixes caught the Sentinel leggings' hose open at its foot (3.3% see-through when the leggings are worn alone), now closed, and the two swords' first icons at 32 and 48 pixels, now 16 × 16 maps.
- **`check_repository.py`**, **`check_icon_maps.py`:** PASS. **`armor_smoke.py --no-render`:** all checks pass.
- **`armor_models.py`:** no problems; the two warnings in the size table above (the Sentinel's outer shell, the Frost Knight's tallest crystal), and none for the first three sets.
- **Java:** the changed files parse (a syntax-only parse with the JDK's own parser; nothing compiles outside CI): `JugcraftGear`, `ArmVariants`, `ArmorTiersGameTests` and `ArmsVIIClientGameTests`.
- **Head poses** (a scratch check of the head's faces against every other bone's, posed): with the head turned up to 75 degrees each way and looking from straight up to straight down, standing and sneaking, no face of a head piece lies within 0.15 pixel of a parallel face of another piece. It found the helms' sides, the visors' and the Templar's centre rib's sides near the planes of the cuirass, the shield and the cross; the Berserker's horn bands and jaw frame in the pauldrons' and the shoulder bands' top planes when looking straight up or down; the Paladin's comb in the pauldron domes' plane; the Sentinel's back ridge in the left pauldron's underside when the head turns; the Frost Knight's helm and visor sides near its cuirass's and chest plates', its jaw and teeth in the pauldrons' and the cuirass's planes, and its crown band against the mantle; all were moved apart.
- **Wearer audit** (`armor_preview.py --wearer`): the Paladin and the Templar show 0.00 model px² of the wearer over standing, walking and sneaking from four views each. Its first run found the hips showing when a leg swings back (about 5 px²), now closed by the cloth band round the hip. The Sentinel shows 0.00 likewise; its first run found the right shoulder showing between the pauldron's cap and the sleeve (0.75 px²), closed by starting the sleeve inside the cap. The Berserker leaves the face, the back of the head under the cap and the upper arms open by design (30 to 67 px² of them show), and the Frost Knight its forearms and upper arms below the pauldrons (11 to 60 px²); their legs and bodies show nothing.
- **Icons:** `check_icon_maps.py` and a scratch check: each part outlined in its own darkest tone, never pure black; no fill touching transparency or the edge; no outline two pixels thick; light from the top left.
- **Renders** with `armor_preview.py` (not committed): each set beside the owner's render, front, three-quarter, side and back.

**In CI:** not run yet. It will compile the five materials, the two swords and their registrations, run `ArmorTiersGameTests` and `ArmsVIIGameTests` on the server (the numbers above, typed in; every variant's kind, durability, rarity and boon), and run `ArmorTiersClientGameTests` and `ArmsVIIClientGameTests`, which write the `jugcraft_armor_tier_<set>_*` and `jugcraft_arms_vii_held_<arm>` shots.

**Not run:** the client by hand, a two-client dedicated server, and any play.

## World and event applicability
Not applicable yet: the sets are placed in no structure, loot table or boss drop.

## Rollout and open questions
- **Names:** the five names, and so the swords', are placeholders (above).
- **How they are obtained,** and **their numbers,** as the owner decides; until then creative-only.
- **Weapons not built yet:** the Sentinel's star-shaped shield (next), and the weapons in the Paladin's and the Templar's renders (a sword and a kite shield; a dark clawed axe), which were not asked for when those sets were sent.
- **What is open by design:** the Berserker's face, the back of its head under the cap and its upper arms; the Frost Knight's arms below its pauldrons (the owner's renders show their model's plain pale blue there). On an armor stand those parts show the stand.
- **Where the models differ from the designs** (each can be changed):
  - **Berserker:** the back of the cap is drawn plain (the sheet shows only the front); the jaw frame's back posts and side bars are guesses at what the front view hides.
  - **Paladin and Templar:** the backs are guesses in the designs' words (a back plate with a spine, flaps behind the cloth); the Templar's crest is three straight pieces where the owner's curls; the shield on the chest steps where the owner's curves.
  - **Sentinel:** the pauldron's shells are flat plates fanned out, where the owner's curve round the shoulder; the window and slit are a pixel wider than drawn, so they stay centred.
  - **Frost Knight:** the frost is built of straight spikes and blades, where the owner's is ragged and feathery; the mantle and its spikes behind the right shoulder are our reading of the white mass the renders show there; the bits of ice floating round the knight in the renders were taken as the scene, not the armor, so no particle effect is added (the owner may want one).
- **Known limits of the 3D layer,** as for every set: trims are kept but not drawn on the 3D models; babies wear nothing visible; mobs with odd proportions (a zombie villager's head, a skeleton's thin limbs) wear them loosely.
