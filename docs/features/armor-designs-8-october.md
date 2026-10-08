# Armor designs of 8 October: Berserker, Paladin and Templar

Status: implemented on `claude/epic-pascal-f8gpfg`, after the four designs of the day before ([four-armor-designs.md](four-armor-designs.md)) and stacked with them on `claude/pharaoh-armor` (#246, itself on #241 and #240), since the sets share the lists they are added to. **Not yet compiled, game-tested or played:** the Java compiles only in CI, which has not run on this branch.
Proposal issue: none. On 8 October 2026 the owner sent three more designs of their own ("Those are fantastic lets do these 3 next and I'll upload more that I want in here as well", then "I made these 3"): a Blockbench sheet of a horned red-and-white set, worn and piece by piece, and two renders each of a white knight with purple cloth and of a dark knight with red cloth. The brief is the one they gave the day before: "just create the armors for the modpack and make them usable in game", crafting and the rest "decided later". While these were being built they added: "Make sure you are making complex models for the armor using the most of what our mod has to offer really capturing the crazy unique geometry of each armor", so every shape below is built as its own box, plate or relief rather than painted on a flat one.

Owner: jimbozoomer-byte (design: the owner; implementation: Claude Opus 5.5).
Target milestone and tier: three new armor-only tiers beside the owner's other nine ([bloodthorn-armor.md](bloodthorn-armor.md) and the sets after it). Their place in the game, and how they are obtained, are the owner's to decide later; until then they are creative-only.
Primary specialty and supported player role: combat (defense). Everyone who wears armor.

**The names are placeholders.** The pictures carry no names, so each set is named for its look: Berserker (the horned set), Paladin (the white knight) and Templar (the dark knight). Nothing has been released, so renaming one now is a find-and-replace and a regeneration; once released, the IDs stay.

## Player experience
Each set is worn as a 3D model on the knight armor's toolkit ([knight-armor.md](knight-armor.md)), built from the owner's pictures part for part, and has four 16 × 16 icons of its own. The pictures show the front and a little of one side; the backs are drawn in each design's own words. Preview them with `python3 tools/armor_preview.py --set <set>` (`berserker`, `paladin`, `templar`).

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

### Shape, size and colour
| | Berserker | Paladin | Templar |
|---|---|---|---|
| Parts | 77 | 83 | 80 |
| Quads: helmet, chestplate, leggings, boots | 132, 146, 65, 54 | 89, 167, 120, 68 | 94, 167, 105, 68 |
| Quads, full set (cap 900) | 397 | 444 | 434 |
| Atlas | 128 × 128 | 128 × 128 | 128 × 128 |
| Reach: to the side, above the head | 9.4, 5.8 | 10.3, 5.2 | 10.3, 6.0 |

Nothing passes the toolkit's warning lines (12 to the side, 6 above the head).

Colours (`tools/armor_paint.py`): `BERSERKER`, `PALADIN` and `TEMPLAR`, sampled from the renders. In the Paladin's and the Templar's palettes "gold" names the Paladin's blue gems and the Templar's near-whites, and "leather" the cloth.

**Toolkit additions** (`tools/armor_paint.py`): the `chevron` painter's corner `"o"` draws concentric square rings about a core (the owner's square spirals), and a `checker` painter draws mail. No earlier set's paint changes.

### The numbers
`tools/gear.py` `ARMOR_TIERS`, `JugcraftGear.BERSERKER_ARMOR` and the two after it: beside the owner's other sets in other strengths, each after its look. These are starting numbers for the owner to set; nothing was asked of them yet.

| | Berserker | Paladin | Templar | Netherite |
|---|---|---|---|---|
| Defense (helmet, chestplate, leggings, boots) | 3, 8, 7, 3 (21) | 4, 8, 6, 3 (21) | 3, 9, 7, 3 (22) | 3, 8, 6, 3 (20) |
| Toughness (each piece) | 3.0 | 3.0 | 3.5 | 3.0 |
| Knockback resistance (each piece) | 0.15 | 0.1 | 0.15 | 0.1 |
| Durability (multiplier) | 495, 720, 675, 585 (45) | 517, 752, 705, 611 (47) | 473, 688, 645, 559 (43) | 407, 592, 555, 481 (37) |
| Enchantability | 16 | 22 | 14 | 15 |
| Fire resistant | no | no | yes | yes |
| Repaired with | quartz | amethyst shards | netherite ingots | netherite ingots |
| Equip sound | iron's | diamond's | netherite's | netherite's |

**Getting it:** for now only from the creative tab (Combat). No recipe, drop or trade exists, as the owner asked.

## Connections
- Existing input producer: none yet (creative only). The repair items come from vanilla (quartz, amethyst shards, netherite ingots).
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
- **The 3D models are client-side only** (`client/WornModelLayer`), drawn from `assets/jugcraft/worn_models.json` (27 new entries; none of the earlier ones changed) and the atlases `textures/entity/equipment/3d/<set>.png`.
- **IDs:** `jugcraft:<set>_helmet`, `_chestplate`, `_leggings` and `_boots` for `berserker`, `paladin` and `templar`, and the repair tags `jugcraft:repairs_<set>_gear`. Placeholder names (above) until the owner confirms them; stable once released.
- **No equipment asset files:** every piece is drawn in 3D, so no `equipment/<set>.json` is written.

## The shared parts it uses and adds
- Each set: one `ARMOR_TIERS` entry, one Java material and one `armorTier(...)` call, a row in `ArmorTiersGameTests`, a palette and four icon maps; the Berserker's module and the Paladin and Templar's shared one in `armor_models.SET_MODULES`. `ArmorTiersClientGameTests` shows every tier, so it shoots these with no change.
- The two painters above.

## Dependencies and assets
- No new dependencies.
- The designs are the owner's; their pictures are not committed. Every model, texture and icon here is drawn by code from them: the two modules, the three palettes and `tools/armor_icons/<set>/`. Nothing is copied or traced from any mod or from vanilla, and no pixel of the owner's pictures is in the textures.
- The owner's asset library ([art/owner-library/README.md](../../art/owner-library/README.md)) was searched first, as for the four sets before: none of its armor is these designs, so nothing was taken from it.

## Verification
**Run locally on 8 October 2026,** on this branch:
- **Generators:** `generate_material_data.py`, `generate_textures.py`, then `generate_material_data.py` again, all exit 0. Besides this work's files, `generate_textures.py` redraws the same 24 flower and entity textures it redraws on main ([four-armor-designs.md](four-armor-designs.md)); they are left as committed.
- **`check_mod_data.py`:** PASS (1586 material IDs), with `check_armor_tiers` and the art check (`tools/art_check.py`): no new findings for the 27 new entries (holes H1, flicker Z2 and N2); its allow-list is unchanged.
- **`check_repository.py`**, **`check_icon_maps.py`:** PASS. **`armor_smoke.py --no-render`:** all checks pass.
- **`armor_models.py`:** no problems and no warnings for the three sets.
- **Java:** the two changed files parse (a syntax-only parse with the JDK's own parser; nothing compiles outside CI).
- **Head poses** (a scratch check of the head's faces against every other bone's, posed): with the head turned up to 75 degrees each way and looking from straight up to straight down, standing and sneaking, no face of a head piece lies within 0.15 pixel of a parallel face of another piece. It found the helms' sides, the visors' and the Templar's centre rib's sides near the planes of the cuirass, the shield and the cross; the Berserker's horn bands and jaw frame in the pauldrons' and the shoulder bands' top planes when looking straight up or down; and the Paladin's comb in the pauldron domes' plane; all were moved apart.
- **Wearer audit** (`armor_preview.py --wearer`): the Paladin and the Templar show 0.00 model px² of the wearer over standing, walking and sneaking from four views each. Its first run found the hips showing when a leg swings back (about 5 px²), now closed by the cloth band round the hip. The Berserker leaves the face, the back of the head under the cap and the upper arms open by design (30 to 67 px² of them show); its legs and body show nothing.
- **Icons:** `check_icon_maps.py` and a scratch check: each part outlined in its own darkest tone, never pure black; no fill touching transparency or the edge; no outline two pixels thick; light from the top left.
- **Renders** with `armor_preview.py` (not committed): each set beside the owner's render, front, three-quarter, side and back.

**In CI:** not run yet. It will compile the three materials and their registrations, run `ArmorTiersGameTests` on the server (the numbers above, typed in), and run `ArmorTiersClientGameTests`, which writes the `jugcraft_armor_tier_<set>_*` shots.

**Not run:** the client by hand, a two-client dedicated server, and any play.

## World and event applicability
Not applicable yet: the sets are placed in no structure, loot table or boss drop.

## Rollout and open questions
- **Names:** the three names are placeholders (above).
- **How they are obtained,** and **their numbers,** as the owner decides; until then creative-only.
- **The weapons** in the Paladin's and the Templar's renders (a sword and a kite shield; a dark clawed axe) are not armor and are not in this record.
- **What is open by design:** the Berserker's face, the back of its head under the cap and its upper arms, as drawn. On an armor stand those parts show the stand.
- **Where the models differ from the designs** (each can be changed):
  - **Berserker:** the back of the cap is drawn plain (the sheet shows only the front); the jaw frame's back posts and side bars are guesses at what the front view hides.
  - **Paladin and Templar:** the backs are guesses in the designs' words (a back plate with a spine, flaps behind the cloth); the Templar's crest is three straight pieces where the owner's curls; the shield on the chest steps where the owner's curves.
- **Known limits of the 3D layer,** as for every set: trims are kept but not drawn on the 3D models; babies wear nothing visible; mobs with odd proportions (a zombie villager's head, a skeleton's thin limbs) wear them loosely.
