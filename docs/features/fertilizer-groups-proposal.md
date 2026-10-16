# Named fertilizers and crop groups (proposal)

Status: **proposal for the owner's review; nothing here is implemented.** It answers the to-do item asking to define named nitrogen/phosphate fertilizers, broad crop-group applications and optional mixed-farm blends before code is written. All numbers are first guesses for the owner to change.

## What exists today

Every fertilizer is a `FertilizerItem`: used on a crop or the ground, it gives every block in the `minecraft:crops` tag within its reach some vanilla bone-meal doses, and is used up once if anything grew.

| Item | Reach | Doses per crop | Route |
| --- | --- | --- | --- |
| Bone meal (vanilla) | one crop | 1 | bones |
| Bat guano | 3×3 | 1 | caves |
| Hearth ash | 3×3 | 1 | fires |
| Fertilizer (`jugcraft:fertilizer`, superphosphate) | 5×5 | 2 | phosphate rock + sulfuric acid |
| Ammonium nitrate | 5×5 | 3 | nitric acid + ammonia |
| Triple superphosphate | 5×5 | 4 | phosphoric-acid branch (separate PR) |

Every crop responds the same way to every fertilizer. The `c:crops` tag holds 59 crop groups, and no potash or potassium exists in the mod.

## Proposal

### 1. Crop groups as item/block tags

Add eight block tags under `jugcraft:crop_groups/` and a matching item tag per group, for the encyclopedia and recipe viewers:

| Group | Members |
| --- | --- |
| Grains | wheat, corn, oat, barley, rice, ornamental corn |
| Roots | carrot, potato, beetroot, sweet potato, turnip, radish, onion, garlic, ginger |
| Leafy | cabbage, lettuce, spinach |
| Legumes | beans, peas |
| Fruiting and vine | tomato, pepper, cucumber, eggplant, zucchini, squashes, pumpkins and melons (their stems), gourds |
| Berries | strawberry, blueberry, cranberry |
| Orchard | pear, peach, lemon, orange, plum, banana, chestnut, coffee |
| Herbs and fibre | basil, mint, rosemary, thyme, parsley, sage, dill, chives, mustard, saffron, vanilla, black pepper, flax, sunflower |

Mandrake and other magic crops stay outside all groups. Fertilizers never affect them, as today. A crop in no group just gets a fertilizer's base doses.

Groups are data-driven tags, so add-ons and later crops can join without code changes. Membership is checked by a game test that lists every `c:crops` member that is in no group, so new crops are not forgotten.

### 2. Nitrogen and phosphate fertilizers

The two industrial fertilizers keep their items, IDs, reach and base doses. Each gains a **favoured** set of groups that get **+1 dose** (one more bone-meal growth step):

| Fertilizer | Nutrient | Favoured groups (+1 dose) | Weak group (−1 dose, minimum 1) |
| --- | --- | --- | --- |
| Ammonium nitrate | nitrogen | grains, leafy, herbs and fibre | legumes (they fix their own nitrogen) |
| Superphosphate | phosphate | roots, fruiting and vine, berries | none |
| Triple superphosphate | phosphate | roots, fruiting and vine, berries, orchard | none |

So on a favoured crop, ammonium nitrate gives 4 doses, superphosphate 3 and triple superphosphate 5. Choosing the right fertilizer for a field matters, but the wrong one still works.

Guano, hearth ash, bone meal and compost stay exactly as they are and favour nothing. Ordinary farming never needs the industrial chain.

### 3. Optional mixed-farm blend (NP fertilizer)

Add a **mixed fertilizer** crafted shapelessly from 1 ammonium nitrate, 1 triple superphosphate and 1 bat guano or hearth ash, giving 2.

- Reach 5×5, 3 doses, and +1 dose on every group in either favoured list.
- It suits mixed farms that grow many crops side by side.
- Per item it is slightly weaker than the matching single fertilizer on its favoured crops: three inputs make two blends.
- It costs no extra energy. The cost is the inputs, the crafting step, and needing both chemical chains.

It is optional. Specialised farms do better with the single fertilizer that matches their crops. The blend is the only new item, and it is the main new consumer of ammonium nitrate and triple superphosphate.

### 4. Feedback loops and balance

- Fertilizers only speed growth. They never add drops, so there is no positive-gain loop. A crop grown faster still gives normal yields.
- Crops into the anaerobic digester give digestate fertilizer residue. That residue stays a plain, unfavoured fertilizer, so digesting crops cannot make a stronger fertilizer than its inputs.
- Ammonium nitrate is also needed for grenades and HE rockets. That gives it a choice of uses, not a shortage: the nitric-acid and ammonia chains are already reachable.
- Companion auto-fertilizing gives the same doses through the same `fertilize` method, so companions get group bonuses automatically. Their four-item supply cap stays.

### 5. Implementation sketch (after approval)

- Add `crop_groups/*` block and item tags in `tools/generate_material_data.py`.
- Change `FertilizerItem` to take an optional favoured tag set and weak tag set. In `fertilize`, the dose count per crop becomes `base + bonus − penalty`, minimum 1. The current constructors keep today's behaviour.
- Add the mixed fertilizer item, its icon (a sack in two tones) and its recipe, behind the existing agriculture feature condition.
- Game tests will check:
  - the ammonium nitrate grain bonus;
  - the legume penalty;
  - the superphosphate root bonus;
  - the blend on a mixed field;
  - a magic crop untouched;
  - every `c:crops` member being grouped.
- Write a feature doc with tier, inputs, outputs, costs and test evidence. Add encyclopedia lines naming each fertilizer's favoured groups.

## Questions for the owner

1. Are eight groups right, or would you prefer fewer (for example grains, roots, greens, fruit) or more?
2. Is "+1 dose favoured, −1 dose weak" the right size? It could instead be more reach, or a chance of an extra harvest. (Extra harvests would need careful loop checks.)
3. Should potash, a third nutrient, be added later? It would need a new mineral or a wood-ash route; hearth ash could become a potash source.
4. Should the mixed blend exist, or should players simply choose between the single fertilizers?
