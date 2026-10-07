# Companion commands

## Inventory and equipment

Shift-right-click an owned Peepo or Jughead to open the command screen. The left panel contains eight general storage slots in two rows of four, plus separate Costume and Hand slots. The lower inventory is the player's inventory.

- Put one Jack o'Lantern in Costume to equip the pumpkin outfit; removing it restores the default appearance. Existing equipped outfits migrate into this slot on loading.
- Hand accepts one item, including tools and torches, and displays it attached to the right hand. This equips the item visually; it does not add tool work or dynamic torch lighting.
- Eating temporarily displays the meal; the equipped item stays safely in its slot and returns afterward. You may change equipment while eating.
- Shift-click moves items between companion storage and player inventory; a Jack o'Lantern first fills an empty costume slot. Place held items directly in Hand.
- Storage and equipment retain exact slots and item components across saves. Contents drop once on normal death, under the existing mob-loot rules. Access uses the existing owner/party command permissions.

Manual checks: transfer full and partial stacks, equip/remove a costume, hold a torch/tool while walking and sitting, feed while equipped (including changing the hand item during a meal), reload during eating, and check normal death drops for duplication. No automated tests run.
