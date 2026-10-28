package io.github.jimbozoomer.jugcraft.lair;

import io.github.jimbozoomer.jugcraft.agriculture.CarvedPumpkinBlock;
import io.github.jimbozoomer.jugcraft.agriculture.HalloweenSeason;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

/**
 * The Hessian's Bridle's rite, which opens the Church Bridge (docs/features/church-bridge.md), the Headless Horseman's
 * lair out of the Halloween season: the bridle used on a jack-o'-lantern (or a lit hand-carved pumpkin) standing on a fence
 * or wall post, at night, in the Overworld, the lantern under the open sky, while the Halloween season is not running.
 * Hoofbeats come out of the dark, the lantern flares, the bridle is used up, and Grey Mist (a {@link MistGateEntity})
 * rises where the user stands; they come to on the wood road. For {@code lairs.gate_seconds} the mist is the gate: anyone
 * who uses it follows, while there is room. Nobody is taken in by standing near it. The lantern and its post are left as
 * they were.
 *
 * <p>When a step is missing (no lantern on a post, outside the Overworld, by day, in the season, under a roof, a mist
 * already open within {@value #MIST_RANGE} blocks) the bridle only says which and nothing is used up; when every instance
 * of the hollow is taken, the same. In the season he rides the Overworld instead (summoned at midnight with a lit
 * scarecrow head), so the bridle says so; {@code lairs.off_season} does not change this.
 */
public final class HessiansBridleRite {
	public static final int COOLDOWN = 40;
	/** How near another open mist into the hollow must not be (blocks). */
	public static final int MIST_RANGE = 3;

	private HessiansBridleRite() {
	}

	/** What the rite found missing here, in the order it is checked, or null when it is answered. */
	public enum Missing {
		NOT_LANTERN, DISABLED, NOT_OVERWORLD, NOT_NIGHT, IN_SEASON, ROOFED, MISTING, FULL
	}

	/** The result of a use: what was missing (or null), and the instance opened. */
	public record Bridling(@Nullable Missing missing, @Nullable LairInstance instance) {
	}

	/** Uses {@code bridle} on the lantern at {@code lantern}. */
	public static Bridling bridle(ServerLevel level, ServerPlayer player, BlockPos lantern, ItemStack bridle) {
		return bridle(level, player, lantern, bridle, !level.isBrightOutside(), HalloweenSeason.active());
	}

	/**
	 * Uses {@code bridle} on the lantern at {@code lantern}, whether it is {@code night} and whether the Halloween season
	 * runs ({@code inSeason}) as given (game tests set them). Opens an instance of the hollow and takes the player in, or
	 * says what is missing and uses nothing.
	 */
	public static Bridling bridle(ServerLevel level, ServerPlayer player, BlockPos lantern, ItemStack bridle, boolean night,
			boolean inSeason) {
		BlockPos feet = HowlingTotemRite.feet(player);
		Missing missing = check(level, lantern, feet, night, inSeason);
		if (missing != null) {
			player.sendOverlayMessage(message(missing));
			level.playSound(null, lantern, SoundEvents.SKELETON_HORSE_AMBIENT, SoundSource.NEUTRAL, 0.6F, 0.6F);
			return new Bridling(missing, null);
		}
		LairInstance instance = Lairs.open(level.getServer(), Lair.CHURCH_BRIDGE);
		if (instance == null) {
			player.sendOverlayMessage(message(Missing.FULL));
			return new Bridling(Missing.FULL, null);
		}
		bridle.consume(1, player);
		// Hoofbeats out of the dark, a horse's scream, a cackle, and the lantern flares.
		level.playSound(null, lantern, SoundEvents.HORSE_GALLOP, SoundSource.HOSTILE, 2.0F, 0.6F);
		level.playSound(null, lantern, SoundEvents.SKELETON_HORSE_AMBIENT, SoundSource.HOSTILE, 2.0F, 0.7F);
		level.playSound(null, lantern, SoundEvents.WITCH_CELEBRATE, SoundSource.HOSTILE, 1.5F, 0.5F);
		level.sendParticles(ParticleTypes.FLAME, lantern.getX() + 0.5, lantern.getY() + 0.8, lantern.getZ() + 0.5, 24, 0.3, 0.4, 0.3, 0.04);
		level.sendParticles(ParticleTypes.LARGE_SMOKE, lantern.getX() + 0.5, lantern.getY() + 1.2, lantern.getZ() + 0.5, 12, 0.3, 0.4, 0.3, 0.02);
		level.sendParticles(ParticleTypes.CLOUD, player.getX(), feet.getY() + 0.2, player.getZ(), 30, 0.9, 0.2, 0.9, 0.01);
		MistGateEntity.open(level, feet.below(), instance, Lairs.gateTicks());
		player.sendSystemMessage(Component.translatable("message.jugcraft.lair.bridle.opened"));
		follow(player, instance);
		return new Bridling(null, instance);
	}

	/** Whether the block at {@code pos} is a jack-o'-lantern (or a lit hand-carved pumpkin) standing on a fence or wall post. */
	public static boolean lanternOnPost(Level level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		boolean lantern = state.is(Blocks.JACK_O_LANTERN)
				|| state.getBlock() instanceof CarvedPumpkinBlock && state.getValue(CarvedPumpkinBlock.LIT);
		BlockState post = level.getBlockState(pos.below());
		return lantern && (post.is(BlockTags.FENCES) || post.is(BlockTags.WALLS));
	}

	/** Checks a use on the lantern at {@code lantern}, by someone with their feet in {@code feet}, without changing anything. */
	public static @Nullable Missing check(ServerLevel level, BlockPos lantern, BlockPos feet, boolean night, boolean inSeason) {
		if (!lanternOnPost(level, lantern)) {
			return Missing.NOT_LANTERN;
		}
		if (!JugcraftConfig.isFeatureEnabled(JugcraftLairs.FEATURE)) {
			return Missing.DISABLED;
		}
		if (level.dimension() != Level.OVERWORLD) {
			return Missing.NOT_OVERWORLD;
		}
		if (!night) {
			return Missing.NOT_NIGHT;
		}
		if (inSeason) {
			return Missing.IN_SEASON;
		}
		if (HowlingTotemRite.roofed(level, lantern)) {
			return Missing.ROOFED;
		}
		if (mist(level, feet) != null) {
			return Missing.MISTING;
		}
		return null;
	}

	public static Component message(Missing missing) {
		return switch (missing) {
			case NOT_LANTERN -> Component.translatable("message.jugcraft.lair.bridle.not_lantern");
			case DISABLED -> Component.translatable("message.jugcraft.lair.rite.disabled");
			case NOT_OVERWORLD -> Component.translatable("message.jugcraft.lair.bridle.not_overworld");
			case NOT_NIGHT -> Component.translatable("message.jugcraft.lair.bridle.not_night");
			case IN_SEASON -> Component.translatable("message.jugcraft.lair.bridle.in_season");
			case ROOFED -> Component.translatable("message.jugcraft.lair.bridle.roofed");
			case MISTING -> Component.translatable("message.jugcraft.lair.bridle.misting");
			case FULL -> Component.translatable("message.jugcraft.lair.full", Component.translatable("lair.jugcraft.church_bridge"));
		};
	}

	/** An open mist into the Church Bridge within {@value #MIST_RANGE} blocks of {@code at}, or null. */
	public static @Nullable MistGateEntity mist(ServerLevel level, BlockPos at) {
		AABB near = new AABB(at.getX() - MIST_RANGE, at.getY() - MIST_RANGE, at.getZ() - MIST_RANGE, at.getX() + MIST_RANGE + 1,
				at.getY() + MIST_RANGE + 1, at.getZ() + MIST_RANGE + 1);
		for (MistGateEntity gate : level.getEntitiesOfClass(MistGateEntity.class, near, gate -> gate.lair() == Lair.CHURCH_BRIDGE)) {
			if (gate.instance() != null) {
				return gate;
			}
		}
		return null;
	}

	/** Takes {@code player} into the Church Bridge through the mist: hoofbeats, and they come to on the wood road. */
	static boolean follow(ServerPlayer player, LairInstance instance) {
		ServerLevel from = (ServerLevel) player.level();
		BlockPos at = player.blockPosition();
		if (!Lairs.enter(player, instance)) {
			return false;
		}
		from.playSound(null, at, SoundEvents.HORSE_GALLOP, SoundSource.PLAYERS, 0.8F, 0.6F);
		from.sendParticles(ParticleTypes.CLOUD, at.getX() + 0.5, at.getY() + 0.8, at.getZ() + 0.5, 12, 0.3, 0.5, 0.3, 0.02);
		return true;
	}
}
