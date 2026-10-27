package io.github.jimbozoomer.jugcraft.lair.abbot;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.lair.ChapelMirrorBlock;
import io.github.jimbozoomer.jugcraft.lair.GlassChapel.Mirror;
import io.github.jimbozoomer.jugcraft.lair.Lair;
import io.github.jimbozoomer.jugcraft.lair.LairRewards;
import io.github.jimbozoomer.jugcraft.lair.Lairs;
import io.github.jimbozoomer.jugcraft.weapons.DescribedItem;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.AABB;

/**
 * The Glass Abbot's registrations (docs/features/glass-abbot.md): him and his Glass Images; his loot (the Prism Shard, the
 * Glass Reliquary and the two things it makes, the Lantern Lens and the Mirror Inscription, and the Abbot's Cowl); and
 * Abbey Glass, stained glass that glows faintly, made with a Prism Shard. He is knelt at the lectern of every Glass Chapel
 * as it is placed (part 1, docs/features/glass-chapel.md), and told when a player turns one of its mirrors.
 */
public final class JugcraftAbbot {
	/** Abbey Glass's light. */
	public static final int ABBEY_GLASS_LIGHT = 4;
	public static EntityType<GlassAbbotEntity> GLASS_ABBOT;
	public static EntityType<GlassImageEntity> GLASS_IMAGE;
	public static Item PRISM_SHARD;
	public static Item GLASS_RELIQUARY;
	public static Item LANTERN_LENS;
	public static Item MIRROR_INSCRIPTION;
	public static Item ABBOTS_COWL;
	public static Block ABBEY_GLASS;
	public static Item ABBEY_GLASS_ITEM;

	private JugcraftAbbot() {
	}

	public static void register() {
		// His rewards may wait for a player (lair/LairRewards).
		LairRewards.payer(AbbotLoot.BOSS, AbbotLoot::pay);
		GLASS_ABBOT = entity("glass_abbot", EntityType.Builder.<GlassAbbotEntity>of(GlassAbbotEntity::new, MobCategory.MONSTER)
				.sized(1.2F, 2.7F).eyeHeight(2.3F).noLootTable().clientTrackingRange(12));
		FabricDefaultAttributeRegistry.register(GLASS_ABBOT, GlassAbbotEntity.createAttributes());
		GLASS_IMAGE = entity("glass_image", EntityType.Builder.<GlassImageEntity>of(GlassImageEntity::new, MobCategory.MONSTER)
				.sized(1.2F, 2.7F).eyeHeight(2.3F).noLootTable().noSave().clientTrackingRange(12));
		FabricDefaultAttributeRegistry.register(GLASS_IMAGE, GlassImageEntity.createAttributes());

		// Each says what it is for in a grey line (tooltip.jugcraft.<id>).
		PRISM_SHARD = item("prism_shard", new Item.Properties().rarity(Rarity.UNCOMMON));
		GLASS_RELIQUARY = item("glass_reliquary", new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
		LANTERN_LENS = item("lantern_lens", new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
		MIRROR_INSCRIPTION = item("mirror_inscription", new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
		// Worn on the head, it is drawn there as its own model (its "head" display), as the costume hats are.
		ABBOTS_COWL = item("abbots_cowl", new Item.Properties().stacksTo(1).rarity(Rarity.RARE).component(
				DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).setEquipSound(SoundEvents.ARMOR_EQUIP_LEATHER).build()));
		ResourceKey<Block> glassKey = ResourceKey.create(Registries.BLOCK, Jugcraft.id("abbey_glass"));
		ABBEY_GLASS = Registry.register(BuiltInRegistries.BLOCK, glassKey, new TransparentBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)
				.lightLevel(state -> ABBEY_GLASS_LIGHT).setId(glassKey)) {
		});
		ResourceKey<Item> glassItemKey = ResourceKey.create(Registries.ITEM, Jugcraft.id("abbey_glass"));
		ABBEY_GLASS_ITEM = Registry.register(BuiltInRegistries.ITEM, glassItemKey,
				new BlockItem(ABBEY_GLASS, new Item.Properties().useBlockDescriptionPrefix().setId(glassItemKey)));

		Lairs.onPlaced(Lair.GLASS_CHAPEL, GlassAbbotEntity::summon);
		ChapelMirrorBlock.onTurned(JugcraftAbbot::mirrorTurned);
		LanternLens.register();
		MirrorInscription.register();
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(output -> {
			output.accept(PRISM_SHARD);
			output.accept(GLASS_RELIQUARY);
		});
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COLORED_BLOCKS).register(output -> output.accept(ABBEY_GLASS_ITEM));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT).register(output -> {
			output.accept(LANTERN_LENS);
			output.accept(MIRROR_INSCRIPTION);
			output.accept(ABBOTS_COWL);
		});
	}

	/** A player turned a chapel mirror: the Abbot whose chapel it is (if he is near) hears of it. */
	private static void mirrorTurned(ServerLevel level, BlockPos base, Mirror mirror, int stop, ServerPlayer player) {
		for (GlassAbbotEntity abbot : level.getEntitiesOfClass(GlassAbbotEntity.class, new AABB(base).inflate(48.0), GlassAbbotEntity::isAlive)) {
			if (abbot.mirror(mirror).equals(base)) {
				abbot.turned(player, mirror, stop);
				return;
			}
		}
	}

	private static <T extends net.minecraft.world.entity.Entity> EntityType<T> entity(String id, EntityType.Builder<T> builder) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Jugcraft.id(id));
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
	}

	private static Item item(String id, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Jugcraft.id(id));
		return Registry.register(BuiltInRegistries.ITEM, key, new DescribedItem(properties.setId(key)));
	}
}
