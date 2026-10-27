package io.github.jimbozoomer.jugcraft.lair;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

/**
 * The Glass Accord's holding up, which opens the Glass Chapel (docs/features/glass-chapel.md): the Accord held up in the
 * Overworld by day, in clear weather, standing in sunlight (full sky light where the holder's head is) that falls through
 * glass ({@code #jugcraft:glass_accord_glass}, any glass block or pane, within {@value #GLASS_REACH} blocks straight
 * above). The sunlight gathers in it, it dissolves into light and is used up, and Grey Mist (a {@link MistGateEntity})
 * rises where it was held; the holder stands in the chapel's narthex. For {@code lairs.gate_seconds} the mist is the
 * gate: anyone who uses it follows, while there is room. Nobody is taken in by standing near it.
 *
 * <p>When a step is missing (outside the Overworld, at night, in rain, out of the sun, no glass between the holder and
 * the sky, a mist already open within {@value #MIST_RANGE} blocks) the Accord only says which and nothing is used up;
 * when every instance of the chapel is taken, the same. The Glass Abbot is no Witching Season boss: the Accord works all
 * year, whatever {@code lairs.off_season} says.
 */
public final class GlassAccordRite {
	public static final TagKey<Block> GLASS = TagKey.create(Registries.BLOCK, Jugcraft.id("glass_accord_glass"));
	public static final int COOLDOWN = 40;
	/** How near another open mist into the chapel must not be (blocks). */
	public static final int MIST_RANGE = 3;
	/** How far over the holder's head the glass may be (blocks). */
	public static final int GLASS_REACH = 16;

	private GlassAccordRite() {
	}

	/** What the holding found missing here, in the order it is checked, or null when it is answered. */
	public enum Missing {
		DISABLED, NOT_OVERWORLD, NIGHT, CLOUDED, SHADED, NO_GLASS, MISTING, FULL
	}

	/** The result of a holding: what was missing (or null), and the instance opened. */
	public record Holding(@Nullable Missing missing, @Nullable LairInstance instance) {
	}

	/** Holds up {@code accord} where {@code player} stands. */
	public static Holding hold(ServerLevel level, ServerPlayer player, ItemStack accord) {
		return hold(level, player, accord, level.isBrightOutside(), !level.isRaining() && !level.isThundering());
	}

	/**
	 * Holds up {@code accord} where {@code player} stands, whether it is {@code day} and whether the weather is
	 * {@code clear} as given (game tests set them). Opens an instance of the chapel and takes the player in, or says what
	 * is missing and uses nothing.
	 */
	public static Holding hold(ServerLevel level, ServerPlayer player, ItemStack accord, boolean day, boolean clear) {
		BlockPos feet = HowlingTotemRite.feet(player);
		level.playSound(null, feet, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 1.2F);
		Missing missing = check(level, feet, day, clear);
		if (missing != null) {
			player.sendOverlayMessage(message(missing));
			return new Holding(missing, null);
		}
		LairInstance instance = Lairs.open(level.getServer(), Lair.GLASS_CHAPEL);
		if (instance == null) {
			player.sendOverlayMessage(message(Missing.FULL));
			return new Holding(Missing.FULL, null);
		}
		accord.consume(1, player);
		level.playSound(null, feet, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.4F);
		level.sendParticles(ParticleTypes.END_ROD, player.getX(), feet.getY() + 1.6, player.getZ(), 40, 0.4, 1.2, 0.4, 0.04);
		MistGateEntity.open(level, feet.below(), instance, Lairs.gateTicks());
		player.sendSystemMessage(Component.translatable("message.jugcraft.lair.accord.opened"));
		follow(player, instance);
		return new Holding(null, instance);
	}

	/** Checks a holding where someone stands with their feet in {@code feet}, without changing anything. */
	public static @Nullable Missing check(ServerLevel level, BlockPos feet, boolean day, boolean clear) {
		if (!JugcraftConfig.isFeatureEnabled(JugcraftLairs.FEATURE)) {
			return Missing.DISABLED;
		}
		if (level.dimension() != Level.OVERWORLD) {
			return Missing.NOT_OVERWORLD;
		}
		if (!day) {
			return Missing.NIGHT;
		}
		if (!clear) {
			return Missing.CLOUDED;
		}
		BlockPos head = feet.above();
		if (level.getBrightness(LightLayer.SKY, head) < 15) {
			return Missing.SHADED;
		}
		if (!underGlass(level, head)) {
			return Missing.NO_GLASS;
		}
		if (mist(level, feet) != null) {
			return Missing.MISTING;
		}
		return null;
	}

	/** Whether there is glass straight over {@code head}, within {@value #GLASS_REACH} blocks. */
	public static boolean underGlass(Level level, BlockPos head) {
		BlockPos.MutableBlockPos at = head.mutable();
		for (int i = 1; i <= GLASS_REACH; i++) {
			if (level.getBlockState(at.setY(head.getY() + i)).is(GLASS)) {
				return true;
			}
		}
		return false;
	}

	public static Component message(Missing missing) {
		return switch (missing) {
			case DISABLED -> Component.translatable("message.jugcraft.lair.rite.disabled");
			case NOT_OVERWORLD -> Component.translatable("message.jugcraft.lair.accord.not_overworld");
			case NIGHT -> Component.translatable("message.jugcraft.lair.accord.night");
			case CLOUDED -> Component.translatable("message.jugcraft.lair.accord.clouded");
			case SHADED -> Component.translatable("message.jugcraft.lair.accord.shaded");
			case NO_GLASS -> Component.translatable("message.jugcraft.lair.accord.no_glass", GLASS_REACH);
			case MISTING -> Component.translatable("message.jugcraft.lair.accord.misting");
			case FULL -> Component.translatable("message.jugcraft.lair.full", Component.translatable("lair.jugcraft.glass_chapel"));
		};
	}

	/** An open mist into the Glass Chapel within {@value #MIST_RANGE} blocks of {@code at}, or null. */
	public static @Nullable MistGateEntity mist(ServerLevel level, BlockPos at) {
		AABB near = new AABB(at.getX() - MIST_RANGE, at.getY() - MIST_RANGE, at.getZ() - MIST_RANGE, at.getX() + MIST_RANGE + 1,
				at.getY() + MIST_RANGE + 1, at.getZ() + MIST_RANGE + 1);
		for (MistGateEntity gate : level.getEntitiesOfClass(MistGateEntity.class, near, gate -> gate.lair() == Lair.GLASS_CHAPEL)) {
			if (gate.instance() != null) {
				return gate;
			}
		}
		return null;
	}

	/** Takes {@code player} into the chapel through the mist: the light takes them, and they stand in the narthex. */
	static boolean follow(ServerPlayer player, LairInstance instance) {
		ServerLevel from = (ServerLevel) player.level();
		BlockPos at = player.blockPosition();
		if (!Lairs.enter(player, instance)) {
			return false;
		}
		from.playSound(null, at, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 0.8F);
		from.sendParticles(ParticleTypes.END_ROD, at.getX() + 0.5, at.getY() + 1.0, at.getZ() + 0.5, 16, 0.3, 0.8, 0.3, 0.02);
		return true;
	}
}
