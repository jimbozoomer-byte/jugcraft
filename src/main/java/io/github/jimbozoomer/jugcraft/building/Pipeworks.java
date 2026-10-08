package io.github.jimbozoomer.jugcraft.building;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/**
 * Pipeworks (docs/features/pipeworks.md): twelve industrial pipe and tank props in the Dieselworks steel,
 * each a {@link PipeworksBlock} of several blocks. Keep the list, sizes and collision boxes in sync with
 * tools/pipeworks_models.py PROPS; tools/check_mod_data.py checks them.
 */
public final class Pipeworks {
	/** Every prop, by id, in registration order. */
	public static final Map<String, PipeworksBlock> BLOCKS = new LinkedHashMap<>();

	private Pipeworks() {
	}

	public static void register() {
		prop("pipe_stand_run", 1, 1, 4, new double[][] {{4.5, 0, 0, 11.5, 13.5, 64}});
		prop("blind_flange_stub", 1, 1, 1, new double[][] {{3, 0, 3, 13, 7, 13}, {3.5, 6, 0.5, 12.5, 15, 15.5}});
		prop("flanged_pipe", 1, 1, 3, new double[][] {{3, 0, 0, 13, 12.5, 48}});
		prop("pipe_rack", 2, 1, 4, new double[][] {{1.5, 0, 0, 30.5, 11.5, 64}, {13.5, 11, 0, 18.5, 15.5, 64}});
		prop("pipe_bridge", 1, 2, 6, new double[][] {{2, 0, 0, 14, 31.5, 96}});
		prop("standpipe_frame", 2, 2, 2, new double[][] {{2, 0, 2, 30, 32, 30}});
		prop("horizontal_tank", 2, 2, 4, new double[][] {{2, 0, 2, 30, 26.5, 62}, {11, 26, 19, 21, 32, 49}});
		prop("tank_walkway", 2, 3, 4, new double[][] {{1, 0, 0, 31, 31, 62}, {1, 31, 2, 31, 43, 62}});
		prop("stacked_tanks", 2, 4, 4, new double[][] {{1, 0, 0, 31, 52, 62}, {11, 52, 19, 21, 56, 29}});
		prop("pipeline_hoops", 1, 2, 6, new double[][] {{1.5, 0, 0, 14.5, 32, 96}});
		prop("ribbed_drum", 2, 2, 5, new double[][] {{3, 0, 2, 29, 29, 78}, {11, 28, 35, 21, 32, 45}});
		prop("pipe_overpass", 1, 3, 5, new double[][] {{1, 0, 1, 15, 39, 7}, {1, 0, 73, 15, 39, 79}, {3, 38, 0, 13, 48, 80}});
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS).register(output ->
				BLOCKS.values().forEach(output::accept));
	}

	private static void prop(String id, int across, int up, int away, double[][] boxes) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Jugcraft.id(id));
		// Steel, mined with a pickaxe; see-through between its pipes, and too big for a piston to shove.
		BlockBehaviour.Properties properties = BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).mapColor(MapColor.METAL)
				.strength(5.0F, 6.0F).requiresCorrectToolForDrops().noOcclusion().pushReaction(PushReaction.BLOCK).setId(key);
		PipeworksBlock block = Registry.register(BuiltInRegistries.BLOCK, key, new PipeworksBlock(properties, across, up, away, boxes));
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Jugcraft.id(id));
		Registry.register(BuiltInRegistries.ITEM, itemKey, new BlockItem(block, new Item.Properties().setId(itemKey)
				.useBlockDescriptionPrefix()) {
			@Override
			public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
					Consumer<Component> tooltip, TooltipFlag flag) {
				tooltip.accept(Component.translatable("tooltip.jugcraft." + id).withStyle(ChatFormatting.GRAY));
			}
		});
		BLOCKS.put(id, block);
	}
}
