# Companion commands

## Inventory and equipment

Shift-right-click an owned Peepo or Jughead to open the command screen. The left panel contains eight general storage slots in two rows of four, plus separate Costume and Hand slots. The lower inventory is the player's inventory.

- Put one Jack o'Lantern in Costume to equip the pumpkin outfit; removing it restores the default appearance. Existing equipped outfits migrate into this slot on loading.
- Hand accepts one item, including tools and torches, and displays it attached to the right hand. This equips the item visually; it does not add tool work or dynamic torch lighting.
- Eating temporarily displays the meal; the equipped item stays safely in its slot and returns afterward. You may change equipment while eating.
- Shift-click moves items between companion storage and player inventory; a Jack o'Lantern first fills an empty costume slot. Place held items directly in Hand.
- Storage and equipment retain exact slots and item components across saves. Contents drop once on normal death, under the existing mob-loot rules. Access uses the existing owner/party command permissions.

Manual checks: transfer full and partial stacks, equip/remove a costume, hold a torch/tool while walking and sitting, feed while equipped (including changing the hand item during a meal), reload during eating, and check normal death drops for duplication. No automated tests run.

## Held lights and tiki torches

Companions raise their right arm when holding a light, including while sitting or running. Eating and sleeping keep their existing poses. The held-light tag `peepo_companion:held_lights` covers torches/lanterns and the tiki torch; other block items with an emissive default state also qualify. This pose does not introduce dynamic lighting.

Craft one Tiki Torch with coal above a stick above another stick in a crafting table. It occupies two vertical blocks on a sturdy floor and has a wooden shaft, bound basket, coal and embers. Its upper half emits light level 14 and client-side flame/smoke particles. Breaking it removes both halves and drops one torch in survival. It can also be equipped in the companion Hand slot.
