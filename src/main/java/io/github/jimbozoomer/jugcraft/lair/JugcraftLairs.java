package io.github.jimbozoomer.jugcraft.lair;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.util.function.Function;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/**
 * The lairs' registrations (docs/features/hollow-acre.md): the lair-only blocks (unbreakable, no items, no drops), the
 * Mourning Wreath and the Death Knell of the Last Rites, the Mist Gate, and the lairs' events, rules and commands.
 * Registered whatever the feature switch says, so saved worlds keep them; the switch ({@value #FEATURE}) stops the
 * rituals.
 */
public final class JugcraftLairs {
	public static final String FEATURE = "agriculture";
	public static Block BLIGHTED_SOIL;
	public static Block BLACK_WHEAT;
	public static Block MOWN_STUBBLE;
	public static Block LAIR_BRAZIER;
	public static Block LAIR_MOON;
	public static Block LAIR_EXIT;
	public static Block MOURNING_WREATH;
	public static Item MOURNING_WREATH_ITEM;
	public static Item DEATH_KNELL;
	public static EntityType<MistGateEntity> MIST_GATE;

	private JugcraftLairs() {
	}

	public static void register() {
		// The lair-only blocks: like bedrock, they cannot be broken or blown up; their loot tables are empty.
		BLIGHTED_SOIL = fixture("blighted_soil", Block::new, MapColor.COLOR_BLACK, SoundType.ROOTED_DIRT, 0, false);
		BLACK_WHEAT = fixture("black_wheat", p -> new LairPlantBlock(14, p), MapColor.COLOR_BLACK, SoundType.CROP, 0, true);
		MOWN_STUBBLE = fixture("mown_stubble", p -> new LairPlantBlock(4, p), MapColor.TERRACOTTA_BROWN, SoundType.CROP, 0, true);
		LAIR_BRAZIER = fixture("lair_brazier", LairBrazierBlock::new, MapColor.COLOR_BLACK, SoundType.LANTERN, -1, false);
		LAIR_MOON = fixture("lair_moon", LairMoonBlock::new, MapColor.GOLD, SoundType.STONE, 15, false);
		LAIR_EXIT = fixture("lair_exit", LairExitBlock::new, MapColor.COLOR_LIGHT_GRAY, SoundType.WOOL, 6, true);

		ResourceKey<Block> wreathKey = ResourceKey.create(Registries.BLOCK, Jugcraft.id("mourning_wreath"));
		MOURNING_WREATH = Registry.register(BuiltInRegistries.BLOCK, wreathKey, new MourningWreathBlock(BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_GREEN).strength(0.2F).sound(SoundType.AZALEA_LEAVES).noOcclusion().noCollision()
				.pushReaction(PushReaction.POPPED).setId(wreathKey)));
		ResourceKey<Item> wreathItemKey = ResourceKey.create(Registries.ITEM, Jugcraft.id("mourning_wreath"));
		MOURNING_WREATH_ITEM = Registry.register(BuiltInRegistries.ITEM, wreathItemKey,
				new BlockItem(MOURNING_WREATH, new Item.Properties().useBlockDescriptionPrefix().setId(wreathItemKey)));
		ResourceKey<Item> knellKey = ResourceKey.create(Registries.ITEM, Jugcraft.id("death_knell"));
		DEATH_KNELL = Registry.register(BuiltInRegistries.ITEM, knellKey, new DeathKnellItem(new Item.Properties().stacksTo(1).setId(knellKey)));

		ResourceKey<EntityType<?>> gateKey = ResourceKey.create(Registries.ENTITY_TYPE, Jugcraft.id("mist_gate"));
		MIST_GATE = Registry.register(BuiltInRegistries.ENTITY_TYPE, gateKey, EntityType.Builder.<MistGateEntity>of(MistGateEntity::new,
				MobCategory.MISC).sized(1.6F, 2.6F).noSummon().clientTrackingRange(8).updateInterval(20).build(gateKey));
		UseEntityCallback.EVENT.register((player, level, hand, entity, hit) ->
				entity instanceof MistGateEntity gate ? gate.use(player, hand) : InteractionResult.PASS);

		Lairs.register();
		LairRules.register();
		LairCommands.register();
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(output -> output.accept(DEATH_KNELL));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(output -> output.accept(MOURNING_WREATH_ITEM));
	}

	private static Block fixture(String id, Function<BlockBehaviour.Properties, Block> make, MapColor colour, SoundType sound, int light,
			boolean passable) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Jugcraft.id(id));
		BlockBehaviour.Properties properties = BlockBehaviour.Properties.of().mapColor(colour).sound(sound).strength(-1.0F, 3600000.0F)
				.pushReaction(PushReaction.IMMOVEABLE).setId(key);
		if (light > 0) {
			properties = properties.lightLevel(state -> light);
		} else if (light < 0) {
			properties = properties.lightLevel(state -> state.getValue(LairBrazierBlock.LIT) ? 12 : 0);
		}
		if (passable) {
			properties = properties.noCollision().noOcclusion();
		}
		return Registry.register(BuiltInRegistries.BLOCK, key, make.apply(properties));
	}
}
