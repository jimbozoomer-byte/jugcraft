package io.github.jimbozoomer.jugcraft.concordance;

import com.mojang.serialization.MapCodec;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.concordance.rules.FocusPool;
import io.github.jimbozoomer.jugcraft.concordance.rules.Knowledge;
import java.util.function.Function;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/**
 * The Arcane Concordance (docs/ARCANE_CONCORDANCE.md, docs/features/arcane-concordance-first-light.md): Jugcraft's
 * magic, learned by observing the world rather than bought with a mana bar. This registers milestone 1, First Light:
 * the luminous specimens to examine, the Lampwright's Bench to study at, the Kindle invocation (cast through Spell
 * Engine), the Kindled mote it leaves and the Kindled Lantern.
 * <p>
 * Everything stays registered whatever the config says, so saved blocks, items and player data are never lost;
 * {@code concordance.enabled=false} stops the recipes, examination, study, workings and casts.
 */
public final class JugcraftConcordance {
	public static final String FEATURE = "concordance";
	/** What can be examined, studied and infused (tools/concordance.py SPECIMENS). */
	public static final TagKey<Item> SPECIMENS = TagKey.create(Registries.ITEM, Jugcraft.id("luminous_specimens"));
	/** What casts Concordance invocations from the main hand. */
	public static final TagKey<Item> INSTRUMENTS = TagKey.create(Registries.ITEM, Jugcraft.id("concordance_instruments"));

	/** The Radiance in a Kindled Lantern. */
	public static DataComponentType<LanternCharge> RADIANCE;
	/** Present while a Kindled Lantern is lit: the lit model and LambDynamicLights both look for it. */
	public static DataComponentType<Unit> LANTERN_LIT;

	public static Block LUMEN_MOTE;
	public static Block LAMPWRIGHT_BENCH;
	public static Item INITIATE_WAND;
	public static Item KINDLED_LANTERN;
	public static BlockEntityType<LampwrightBenchBlockEntity> BENCH_ENTITY;
	public static ExtendedMenuType<LampwrightBenchMenu, BlockPos> BENCH_MENU;

	/** What a player has learned (saved with them, kept through death, sent only to them). */
	public static AttachmentType<Knowledge> KNOWLEDGE;
	/** A player's Focus (likewise). */
	public static AttachmentType<FocusPool> FOCUS;

	public static SoundEvent KINDLE_GATHER_SOUND;
	public static SoundEvent KINDLE_SOUND;
	public static SoundEvent EXAMINE_SOUND;
	public static SoundEvent STUDY_COMPLETE_SOUND;
	public static SoundEvent LANTERN_IGNITE_SOUND;
	public static SoundEvent LANTERN_SNUFF_SOUND;

	private JugcraftConcordance() {
	}

	public static void register() {
		RADIANCE = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Jugcraft.id("radiance"),
				DataComponentType.<LanternCharge>builder().persistent(LanternCharge.CODEC).networkSynchronized(LanternCharge.STREAM_CODEC).build());
		LANTERN_LIT = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Jugcraft.id("lantern_lit"),
				DataComponentType.<Unit>builder().persistent(MapCodec.unitCodec(Unit.INSTANCE)).networkSynchronized(Unit.STREAM_CODEC).build());

		KNOWLEDGE = AttachmentRegistry.<Knowledge>builder().persistent(ConcordanceCodecs.KNOWLEDGE).copyOnDeath()
				.syncWith(ConcordanceCodecs.KNOWLEDGE_STREAM, AttachmentSyncPredicate.targetOnly())
				.buildAndRegister(Jugcraft.id("concordance_knowledge"));
		FOCUS = AttachmentRegistry.<FocusPool>builder().persistent(ConcordanceCodecs.FOCUS).copyOnDeath()
				.syncWith(ConcordanceCodecs.FOCUS_STREAM, AttachmentSyncPredicate.targetOnly())
				.buildAndRegister(Jugcraft.id("concordance_focus"));

		KINDLE_GATHER_SOUND = sound("concordance.kindle_gather");
		KINDLE_SOUND = sound("concordance.kindle");
		EXAMINE_SOUND = sound("concordance.examine");
		STUDY_COMPLETE_SOUND = sound("concordance.study_complete");
		LANTERN_IGNITE_SOUND = sound("concordance.lantern_ignite");
		LANTERN_SNUFF_SOUND = sound("concordance.lantern_snuff");

		// The Kindled mote is light in the air: nothing to see, hit, break or hold.
		LUMEN_MOTE = block("lumen_mote", LumenMoteBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.NONE)
				.replaceable().noCollision().noOcclusion().instabreak().noLootTable().pushReaction(PushReaction.POPPED)
				.lightLevel(LumenMoteBlock::light));
		LAMPWRIGHT_BENCH = block("lampwright_bench", LampwrightBenchBlock::new, BlockBehaviour.Properties.of()
				.mapColor(MapColor.WOOD).strength(2.0F).sound(SoundType.WOOD).noOcclusion()
				.lightLevel(state -> state.getValue(LampwrightBenchBlock.WORKING) ? 7 : 0));
		Item bench = item("lampwright_bench", properties -> new BlockItem(LAMPWRIGHT_BENCH, properties),
				new Item.Properties().useBlockDescriptionPrefix());
		INITIATE_WAND = item("initiate_wand", InitiateWandItem::new, new Item.Properties().stacksTo(1));
		KINDLED_LANTERN = item("kindled_lantern", KindledLanternItem::new, new Item.Properties().stacksTo(1)
				.rarity(Rarity.UNCOMMON).component(RADIANCE, LanternCharge.EMPTY));

		BENCH_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Jugcraft.id("lampwright_bench"),
				FabricBlockEntityTypeBuilder.create(LampwrightBenchBlockEntity::new, LAMPWRIGHT_BENCH).build());
		BENCH_MENU = Registry.register(BuiltInRegistries.MENU, Jugcraft.id("lampwright_bench"),
				new ExtendedMenuType<>((containerId, inventory, pos) -> new LampwrightBenchMenu(containerId, inventory), BlockPos.STREAM_CODEC.cast()));

		ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(ConcordanceData.ID, ConcordanceData.INSTANCE);
		// New rules can teach or take away invocations: work them out again for everyone online, and for each joiner.
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resources, success) -> ConcordanceProgress.relearnAll(server));
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> ConcordanceProgress.relearn(handler.player));

		Examination.register();
		ConcordanceSpells.register();
		ConcordanceCommand.register();

		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(output -> {
			output.accept(INITIATE_WAND);
			output.accept(KINDLED_LANTERN);
			ItemStack full = new ItemStack(KINDLED_LANTERN);
			KindledLanternItem.set(full, KindledLanternItem.CAPACITY, 0L, false);
			output.accept(full);
		});
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(output -> output.accept(bench));
	}

	private static SoundEvent sound(String id) {
		Identifier key = Jugcraft.id(id);
		return Registry.register(BuiltInRegistries.SOUND_EVENT, key, SoundEvent.createVariableRangeEvent(key));
	}

	private static Block block(String id, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Jugcraft.id(id));
		return Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(properties.setId(key)));
	}

	private static Item item(String id, Function<Item.Properties, Item> factory, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Jugcraft.id(id));
		return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties.setId(key)));
	}
}
