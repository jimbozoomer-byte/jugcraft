package io.github.jimbozoomer.jugcraft.curse;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

/**
 * Registers the Cursed Mirror (docs/features/cursed-mirror.md): one block and its item, and puts the item in the
 * functional-blocks creative tab. The block's behaviour is in {@link CursedMirrorBlock}; tools/cursed_mirror.py draws
 * and describes it, and tools/check_mod_data.py checks the two against each other.
 */
public final class CursedMirror {
	public static final String ID = "cursed_mirror";
	public static Block BLOCK;
	public static Item ITEM;

	private CursedMirror() {
	}

	public static void register() {
		var blockKey = ResourceKey.create(Registries.BLOCK, Jugcraft.id(ID));
		BLOCK = Registry.register(BuiltInRegistries.BLOCK, blockKey, new CursedMirrorBlock(
				BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(blockKey).strength(1.0F).noOcclusion()
						.pushReaction(PushReaction.DESTROY)));
		var itemKey = ResourceKey.create(Registries.ITEM, Jugcraft.id(ID));
		ITEM = Registry.register(BuiltInRegistries.ITEM, itemKey,
				new BlockItem(BLOCK, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix()));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(out -> out.accept(ITEM));
	}
}
