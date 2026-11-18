package io.github.jimbozoomer.jugcraft.town.guests;

import com.mojang.brigadier.CommandDispatcher;
import io.github.jimbozoomer.jugcraft.concordance.Authority;
import io.github.jimbozoomer.jugcraft.finance.LandRegistry;
import io.github.jimbozoomer.jugcraft.town.TownProtection;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

/**
 * Home visits (worker C): a player's home visit spot, and inviting a guest there ({@link GuestOutings} runs the visit as
 * the {@link GuestActivity} kind {@code visit}).
 *
 * <p><b>The spot</b> is set by the player themselves, with the guest menu's {@code set_visit_spot} or
 * {@code /guests visit spot}: the block their feet are in, in their current level, saved per player in
 * {@link GuestOutingState}. It must be loaded, standable and free apart from the player (and the guest beside them);
 * none of its floor, feet and head blocks shielded by the town or an operator region (whoever asks:
 * {@link TownProtection#shieldsBlock}); the player's own claim or anywhere else they may build
 * ({@link LandRegistry#mayBuildFor}, without the admins' bypass); and allowed by spawn protection and
 * {@link Authority#mayChange} for the player standing there. Nothing is placed or changed. {@code /guests visit clear}
 * forgets it.
 *
 * <p><b>An invitation</b> ({@code invite_home}) checks everything {@link GuestOutings#whyNot} does, then that the spot is
 * set, in the guest's level, within {@link #MAX_DISTANCE} blocks of the guest, every chunk along the straight way there
 * already loaded (sampled every {@link #SAMPLE} blocks), and still valid (the same checks but {@code Authority}, which is
 * asked only with the player standing there). The guest walks there only then.
 */
final class GuestVisits {
	/** How far (blocks) the spot may be from the guest when it is invited. */
	static final int MAX_DISTANCE = 128;
	/** The straight way to the spot is checked for loaded chunks every this many blocks. */
	static final int SAMPLE = 8;

	private GuestVisits() {
	}

	/**
	 * {@code /guests visit spot} and {@code /guests visit clear}, for players themselves. Registered by
	 * {@link GuestOutings#register} before the coordinator's operator commands under the same root.
	 */
	static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("guests")
				.then(Commands.literal("visit").requires(source -> source.getEntity() instanceof ServerPlayer)
						.then(Commands.literal("spot").executes(c -> spot(c.getSource())))
						.then(Commands.literal("clear").executes(c -> clear(c.getSource())))));
	}

	private static int spot(CommandSourceStack source) {
		ServerPlayer player = source.getPlayer();
		if (player == null) {
			source.sendFailure(Component.translatableWithFallback("message.jugcraft.guest.visit.players_only", "Only a player can set their own visit spot."));
			return 0;
		}
		GuestResult result = setSpot(player, null);
		if (!result.done()) {
			source.sendFailure(result.message());
			return 0;
		}
		source.sendSuccess(result::message, false);
		return 1;
	}

	private static int clear(CommandSourceStack source) {
		ServerPlayer player = source.getPlayer();
		if (player == null) {
			source.sendFailure(Component.translatableWithFallback("message.jugcraft.guest.visit.players_only", "Only a player can set their own visit spot."));
			return 0;
		}
		GuestOutingState state = GuestOutingState.get(player.level().getServer());
		if (state.spot(player.getUUID()) == null || !state.spot(player.getUUID(), null)) {
			source.sendFailure(Component.translatableWithFallback("message.jugcraft.guest.visit.spot.none", "You have no home visit spot set."));
			return 0;
		}
		source.sendSuccess(() -> Component.translatableWithFallback("message.jugcraft.guest.visit.spot.cleared", "Your home visit spot is cleared."), false);
		return 1;
	}

	/** Sets the player's spot where they stand ({@code beside}: the guest whose menu asked, which may stand there too). */
	static GuestResult setSpot(ServerPlayer player, @Nullable Townsfolk beside) {
		if (!player.isAlive() || player.isSpectator()) {
			return GuestResult.refused("message.jugcraft.guest.visit.spot.not_now", "You cannot set a home visit spot right now.");
		}
		ServerLevel level = player.level();
		BlockPos feet = player.blockPosition();
		Component invalid = invalid(level, player, feet, player, beside);
		if (invalid == null && (!level.mayInteract(player, feet) || !Authority.mayChange(level, player, feet))) {
			invalid = Component.translatableWithFallback("message.jugcraft.guest.visit.spot.no_build", "You may not build here, so it cannot be your home visit spot.");
		}
		if (invalid != null) {
			return new GuestResult(false, invalid);
		}
		MinecraftServer server = level.getServer();
		if (!GuestOutingState.get(server).spot(player.getUUID(), new GuestOutingState.Spot(level.dimension(), feet))) {
			return GuestResult.refused("message.jugcraft.guest.visit.spot.locked", "Your visit record cannot be changed right now; an operator can check it.");
		}
		return GuestResult.done("message.jugcraft.guest.visit.spot.set",
				"Your home visit spot is now %s. Guests you invite home will come here.", feet.toShortString());
	}

	/**
	 * Why {@code feet} cannot be the player's spot now, or null when it can (Authority aside): loaded, standable, free
	 * (but for the entities named), and protected for neither party.
	 */
	static @Nullable Component invalid(ServerLevel level, ServerPlayer player, BlockPos feet, @Nullable Entity mayStand, @Nullable Entity alsoMay) {
		if (!GuestOutingPlaces.loaded(level, feet.below()) || !GuestOutingPlaces.loaded(level, feet) || !GuestOutingPlaces.loaded(level, feet.above())) {
			return Component.translatableWithFallback("message.jugcraft.guest.visit.spot.unloaded", "That spot is not loaded.");
		}
		if (!GuestOutingPlaces.standable(level, feet)) {
			return Component.translatableWithFallback("message.jugcraft.guest.visit.spot.no_room",
					"There is no room to stand there: it needs a solid, safe floor and two clear blocks above it.");
		}
		if (!GuestOutingPlaces.free(level, feet, mayStand, alsoMay)) {
			return Component.translatableWithFallback("message.jugcraft.guest.visit.spot.occupied", "Something else is already there.");
		}
		for (BlockPos cell : List.of(feet.below(), feet, feet.above())) {
			if (TownProtection.shieldsBlock(level, cell)) {
				return Component.translatableWithFallback("message.jugcraft.guest.visit.spot.protected",
						"Town ground and protected areas cannot be a home visit spot.");
			}
			if (!LandRegistry.mayBuildFor(player.getUUID(), level, cell)) {
				return Component.translatableWithFallback("message.jugcraft.guest.visit.spot.claimed",
						"That land belongs to %s. Choose a spot in your own claim or where you may build.", LandRegistry.ownerAt(level, cell));
			}
		}
		return null;
	}

	/** Whether {@code invite_home} is offered: the cheap checks only (no block or chunk is read). */
	static boolean mayInvite(ServerPlayer player, Townsfolk npc, String guest) {
		if (GuestOutings.whyNot(player, npc, guest, GuestOutings.Kind.VISIT_HOME) != null) {
			return false;
		}
		GuestOutingState.Spot spot = GuestOutingState.get(player.level().getServer()).spot(player.getUUID());
		return spot != null && spot.dimension().equals(npc.level().dimension());
	}

	/** Invites the guest to the player's home visit spot. */
	static GuestResult invite(ServerPlayer player, Townsfolk npc, String guest) {
		GuestResult refused = GuestOutings.whyNot(player, npc, guest, GuestOutings.Kind.VISIT_HOME);
		if (refused != null) {
			return refused;
		}
		Component name = npc.getDisplayName();
		ServerLevel level = player.level();
		GuestOutingState.Spot spot = GuestOutingState.get(level.getServer()).spot(player.getUUID());
		if (spot == null) {
			return GuestResult.refused("message.jugcraft.guest.visit.no_spot",
					"Set your home visit spot first: stand there and use /guests visit spot.");
		}
		if (!spot.dimension().equals(level.dimension())) {
			return GuestResult.refused("message.jugcraft.guest.visit.other_level", "Your home visit spot is in another dimension.");
		}
		BlockPos from = npc.blockPosition();
		if (spot.pos().distSqr(from) > (double) MAX_DISTANCE * MAX_DISTANCE) {
			return GuestResult.refused("message.jugcraft.guest.visit.too_far",
					"Your home visit spot is too far for %s to walk (more than %s blocks).", name, MAX_DISTANCE);
		}
		if (!wayLoaded(level, from, spot.pos())) {
			return GuestResult.refused("message.jugcraft.guest.visit.unloaded",
					"The way to your home visit spot is not loaded. Invite %s when you are nearer home.", name);
		}
		Component invalid = invalid(level, player, spot.pos(), player, npc);
		if (invalid != null) {
			return GuestResult.refused("message.jugcraft.guest.visit.spot_invalid",
					"Your home visit spot cannot be used now: %s Set it again with /guests visit spot.", invalid);
		}
		return GuestOutings.begin(player, npc, guest, GuestOutings.Kind.VISIT_HOME, spot.pos());
	}

	/** Whether every chunk along the straight way from {@code from} to {@code to} is already loaded (never loads one). */
	static boolean wayLoaded(ServerLevel level, BlockPos from, BlockPos to) {
		double dx = to.getX() - from.getX();
		double dz = to.getZ() - from.getZ();
		int steps = Math.max(1, (int) Math.ceil(Math.sqrt(dx * dx + dz * dz) / SAMPLE));
		for (int i = 0; i <= steps; i++) {
			BlockPos at = BlockPos.containing(from.getX() + 0.5 + dx * i / steps, from.getY(), from.getZ() + 0.5 + dz * i / steps);
			if (!level.hasChunkAt(at)) {
				return false;
			}
		}
		return true;
	}
}
