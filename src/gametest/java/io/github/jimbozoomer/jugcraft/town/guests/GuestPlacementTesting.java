package io.github.jimbozoomer.jugcraft.town.guests;

import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Worker C's test seam (game tests only): placement with its exact refusal, the roster's lines and a guest free to
 * place, for {@code GuestRosterGameTests} and {@code GuestClientGameTests}.
 */
public final class GuestPlacementTesting {
	public static boolean clearLawn(ServerLevel level, net.minecraft.core.BlockPos feet) {
		return GuestCityPlacement.clearLawn(level, feet);
	}

	private GuestPlacementTesting() {
	}

	/** What a placement did: the new guest or null, and the refusal's name ({@code "PLACED"} when it was placed). */
	public record Placed(@Nullable Townsfolk npc, String refusal, Component message) {
	}

	/** {@code /guests place} for {@code source}, standing the guest at {@code at} in {@code level}. */
	public static Placed place(CommandSourceStack source, String id, ServerLevel level, Vec3 at, float yaw) {
		GuestPlacement.Outcome outcome = GuestPlacement.place(source, id, level, at, yaw);
		return new Placed(outcome.npc(), outcome.refusal() == null ? "PLACED" : outcome.refusal().name(), outcome.message());
	}

	/** What {@code /guests roster} says, header first. */
	public static List<Component> rosterLines(MinecraftServer server) {
		return GuestPlacement.rosterLines(server);
	}

	/** {@code "UNPLACED"}, {@code "PLACED"} or {@code "MISSING"}, as the roster reports the guest. */
	public static String status(MinecraftServer server, String id) {
		return GuestPlacement.status(GuestLedger.get(server), id).name();
	}

	/** Whether placement treats {@code entity} as an owner's thing it must never push aside. */
	public static boolean ownerObject(Entity entity) {
		return GuestPlacement.ownerObject(entity);
	}

	/** The placeholder a guest missing from the fixed table would get. */
	public static String fallbackSkin(String id) {
		return GuestAppearances.fallback(id);
	}

	/**
	 * The first of {@code candidates}, in order, that is available and may be placed now: one never bound, else one lost;
	 * null when every candidate is placed. Tests share one server, so each test class names only the guests it owns, and
	 * a test places the guest in the same tick it picks it.
	 */
	public static @Nullable String freeGuest(MinecraftServer server, List<String> candidates) {
		GuestLedger ledger = GuestLedger.get(server);
		String lost = null;
		for (String id : candidates) {
			GuestRoster.Guest guest = GuestRoster.get(id);
			if (guest == null || !guest.available()) {
				continue;
			}
			GuestPlacement.Status status = GuestPlacement.status(ledger, id);
			if (status == GuestPlacement.Status.UNPLACED) {
				return id;
			}
			if (status == GuestPlacement.Status.MISSING && lost == null) {
				lost = id;
			}
		}
		return lost;
	}
}
