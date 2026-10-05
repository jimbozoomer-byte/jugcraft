package io.github.jimbozoomer.jugcraft.raiders;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * A raider camp (raider extras): a rare camp out in the plains, savanna and badlands, to find and clear. Inside a ring of
 * sandbags (with four gaps) a campfire burns between two olive tents, with a supply barrel (loot table
 * {@code jugcraft:chests/raider_camp}), held by an officer, two grunts and a grenadier who stay put until someone comes.
 * Only on flat ground (no more than {@value #MAX_SLOPE} blocks between its corners), and not with the raiders feature off.
 * Its raiders belong to no raid, never despawn and do not come back once killed.
 */
public class RaiderCampFeature extends Feature<NoneFeatureConfiguration> {
	public static final int RADIUS = 6;
	public static final int MAX_SLOPE = 2;
	public static final ResourceKey<LootTable> LOOT = ResourceKey.create(Registries.LOOT_TABLE, Jugcraft.id("chests/raider_camp"));

	public RaiderCampFeature() {
		super(NoneFeatureConfiguration.CODEC);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		if (!JugcraftConfig.isFeatureEnabled(JugcraftRaiders.FEATURE)) {
			return false;
		}
		return build(context.level(), context.origin(), context.random());
	}

	/** Builds a camp on the ground at {@code origin} (the surface there); false (and nothing built) if it is not flat. */
	public static boolean build(WorldGenLevel level, BlockPos origin, RandomSource random) {
		int y = origin.getY();
		for (int dx = -RADIUS; dx <= RADIUS; dx += RADIUS) {
			for (int dz = -RADIUS; dz <= RADIUS; dz += RADIUS) {
				int ground = level.getHeight(Heightmap.Types.WORLD_SURFACE, origin.getX() + dx, origin.getZ() + dz);
				if (Math.abs(ground - y) > MAX_SLOPE || !level.getFluidState(new BlockPos(origin.getX() + dx, ground - 1, origin.getZ() + dz)).isEmpty()) {
					return false;
				}
			}
		}
		// Level the ground: coarse dirt underfoot, clear air above.
		for (int dx = -RADIUS; dx <= RADIUS; dx++) {
			for (int dz = -RADIUS; dz <= RADIUS; dz++) {
				if (dx * dx + dz * dz > (RADIUS + 0.5) * (RADIUS + 0.5)) {
					continue;
				}
				BlockPos floor = origin.offset(dx, -1, dz);
				set(level, floor, (dx + dz) % 3 == 0 ? Blocks.GRAVEL.defaultBlockState() : Blocks.COARSE_DIRT.defaultBlockState());
				for (int dy = 0; dy < 5; dy++) {
					set(level, origin.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
				}
			}
		}
		// The sandbag ring, with a gap to each side.
		Block sandbags = BuiltInRegistries.BLOCK.getValue(Jugcraft.id("sandbags"));
		BlockState bags = sandbags == Blocks.AIR ? Blocks.MUD_BRICKS.defaultBlockState() : sandbags.defaultBlockState();
		for (int dx = -RADIUS; dx <= RADIUS; dx++) {
			for (int dz = -RADIUS; dz <= RADIUS; dz++) {
				double d = Math.sqrt(dx * dx + dz * dz);
				if (d > RADIUS - 0.5 && d <= RADIUS + 0.5 && Math.abs(dx) > 1 && Math.abs(dz) > 1) {
					set(level, origin.offset(dx, 0, dz), bags);
				}
			}
		}
		set(level, origin, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
		tent(level, origin.offset(-3, 0, -3));
		tent(level, origin.offset(2, 0, -3));
		BlockPos barrel = origin.offset(3, 0, 2);
		set(level, barrel, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP));
		RandomizableContainer.setBlockEntityLootTable(level, random, barrel, LOOT);
		set(level, origin.offset(-3, 0, 3), Blocks.CAULDRON.defaultBlockState());
		// The garrison.
		garrison(level, JugcraftRaiders.OFFICER, origin.offset(0, 0, 2), random);
		garrison(level, JugcraftRaiders.GRUNT, origin.offset(-2, 0, 0), random);
		garrison(level, JugcraftRaiders.GRUNT, origin.offset(2, 0, 0), random);
		garrison(level, JugcraftRaiders.GRENADIER, origin.offset(0, 0, -1), random);
		return true;
	}

	/** An olive A-frame tent, three blocks deep, open to the south. */
	private static void tent(WorldGenLevel level, BlockPos corner) {
		BlockState cloth = Blocks.GREEN_WOOL.defaultBlockState();
		for (int dz = 0; dz < 3; dz++) {
			set(level, corner.offset(0, 0, dz), cloth);
			set(level, corner.offset(2, 0, dz), cloth);
			set(level, corner.offset(1, 1, dz), cloth);
		}
		set(level, corner.offset(1, 0, 0), cloth);
		set(level, corner.offset(1, 0, 1), Blocks.BROWN_CARPET.defaultBlockState());
	}

	private static void garrison(WorldGenLevel level, EntityType<? extends Mob> type, BlockPos at, RandomSource random) {
		Mob raider = type.create(level.getLevel(), EntitySpawnReason.STRUCTURE);
		if (raider == null) {
			return;
		}
		raider.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
		raider.setPersistenceRequired();
		level.addFreshEntityWithPassengers(raider);
	}

	private static void set(WorldGenLevel level, BlockPos pos, BlockState state) {
		level.setBlock(pos, state, Block.UPDATE_CLIENTS);
	}
}
