package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Jack-o'-Lantern String Lights: a strand of tiny pumpkin bulbs. Use it on one String Light Hook, then on another
 * at most {@value StringLightHookBlockEntity#MAX_LENGTH} blocks away in the same dimension, and it is strung between
 * them (one strand is used). Each hook holds one strand of its own (the first hook's if it has none, else the
 * second's) and any number may run to it, so hooks chain; two hooks are strung together once. Everything is
 * checked on the server when the second hook is used; the first hook is remembered per player until then.
 */
public class StringLightsItem extends Item {
	private static final Map<UUID, GlobalPos> FIRST = new HashMap<>();

	public enum Result {
		FIRST, STRUNG, SAME_HOOK, TOO_FAR, GONE, ALREADY_STRUNG
	}

	public StringLightsItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Player player = context.getPlayer();
		if (!(level.getBlockEntity(pos) instanceof StringLightHookBlockEntity) || player == null) {
			return InteractionResult.PASS;
		}
		if (!(level instanceof ServerLevel server) || !(player instanceof ServerPlayer stringer)) {
			return InteractionResult.SUCCESS;
		}
		GlobalPos first = FIRST.remove(player.getUUID());
		if (first == null || !first.dimension().equals(level.dimension()) || first.pos().equals(pos)) {
			FIRST.put(player.getUUID(), GlobalPos.of(level.dimension(), pos.immutable()));
			player.sendOverlayMessage(Component.translatable("message.jugcraft.string_lights.first", StringLightHookBlockEntity.MAX_LENGTH));
			return InteractionResult.SUCCESS;
		}
		Result result = string(server, first.pos(), pos);
		player.sendOverlayMessage(Component.translatable("message.jugcraft.string_lights." + result.name().toLowerCase(java.util.Locale.ROOT),
				StringLightHookBlockEntity.MAX_LENGTH));
		if (result != Result.STRUNG) {
			return InteractionResult.FAIL;
		}
		context.getItemInHand().consume(1, stringer);
		level.playSound(null, pos, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 1.0F, 1.2F);
		return InteractionResult.SUCCESS;
	}

	/** Strings hook {@code from} to hook {@code to} if the rules allow; the strand item is the caller's to use up. */
	public static Result string(ServerLevel level, BlockPos from, BlockPos to) {
		if (from.equals(to)) {
			return Result.SAME_HOOK;
		}
		if (from.distSqr(to) > (double) StringLightHookBlockEntity.MAX_LENGTH * StringLightHookBlockEntity.MAX_LENGTH) {
			return Result.TOO_FAR;
		}
		if (!level.isLoaded(from) || !(level.getBlockEntity(from) instanceof StringLightHookBlockEntity start)
				|| !(level.getBlockEntity(to) instanceof StringLightHookBlockEntity end)) {
			return Result.GONE;
		}
		if (to.equals(start.link()) || from.equals(end.link())) {
			return Result.ALREADY_STRUNG;
		}
		// The strand belongs to whichever hook has none of its own yet, the first one first.
		if (start.link() == null) {
			start.stringTo(to);
			return Result.STRUNG;
		}
		if (end.link() == null) {
			end.stringTo(from);
			return Result.STRUNG;
		}
		return Result.ALREADY_STRUNG;
	}
}
