package io.github.jimbozoomer.jugcraft.concordance;

import io.github.jimbozoomer.jugcraft.concordance.ritual.StructurePattern;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * The loaded Circle Anchors, so a change to a block can void the cached report of the circles it might belong to
 * (roadmap step 12: bounded checks, invalidated by relevant changes rather than repeated every tick). Anchors join the
 * index when their block entity loads and leave it when it unloads or is removed. A change at a position voids every
 * anchor within {@link StructurePattern#MAX_REACH} blocks of it, which covers any structure a data pack can define.
 * <p>
 * The changes it hears about: a player breaking any block, a Ley Pylon or Warding Stone placed or removed, and a
 * pylon's charge changing. Anything else (an explosion, a piston, fluid flowing into a clearance) is caught by the
 * anchor's own checks: every ritual step checks the whole circle again, and an idle report is kept at most
 * {@link CircleAnchorBlockEntity#CACHE_TICKS} ticks. Server thread only.
 */
public final class Rituals {
	private static final Map<ResourceKey<Level>, Set<BlockPos>> ANCHORS = new HashMap<>();
	private static final List<Completed> COMPLETED = new CopyOnWriteArrayList<>();

	/**
	 * Something told when a ritual completes (roadmap step 25: a Concord Spire hears its Kindling): where its anchor
	 * stands, which ritual, and the participants present. Listeners only read: the ritual has already made its result.
	 */
	@FunctionalInterface
	public interface Completed {
		void completed(ServerLevel level, BlockPos anchor, String ritual, List<ServerPlayer> present);
	}

	private Rituals() {
	}

	static void register() {
		ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((blockEntity, level) -> {
			if (blockEntity instanceof CircleAnchorBlockEntity) {
				ANCHORS.computeIfAbsent(level.dimension(), unused -> new HashSet<>()).add(blockEntity.getBlockPos().immutable());
			}
		});
		ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((blockEntity, level) -> {
			if (blockEntity instanceof CircleAnchorBlockEntity) {
				Set<BlockPos> anchors = ANCHORS.get(level.dimension());
				if (anchors != null) {
					anchors.remove(blockEntity.getBlockPos());
				}
			}
		});
		PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
			if (level instanceof ServerLevel server) {
				changed(server, pos, player.getUUID());
			}
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> ANCHORS.clear());
	}

	/** A block that could be part of a circle changed at {@code pos}: anchors in reach check again when next asked. */
	public static void changed(ServerLevel level, BlockPos pos) {
		changed(level, pos, null);
	}

	/**
	 * As {@link #changed(ServerLevel, BlockPos)}, by {@code player} if a player broke it: an anchor in reach remembers
	 * them, so a containment that fails because of it is their doing (roadmap step 28).
	 */
	public static void changed(ServerLevel level, BlockPos pos, @Nullable UUID player) {
		Set<BlockPos> anchors = ANCHORS.get(level.dimension());
		if (anchors == null || anchors.isEmpty()) {
			return;
		}
		int reach = StructurePattern.MAX_REACH;
		for (BlockPos anchor : List.copyOf(anchors)) {
			if (Math.abs(anchor.getX() - pos.getX()) <= reach && Math.abs(anchor.getY() - pos.getY()) <= reach
					&& Math.abs(anchor.getZ() - pos.getZ()) <= reach
					&& level.getBlockEntity(anchor) instanceof CircleAnchorBlockEntity found) {
				found.invalidate();
				found.disturbedBy(player, level.getGameTime());
			}
		}
	}

	/** Adds a listener told whenever a ritual completes (at registration). */
	public static void listen(Completed listener) {
		COMPLETED.add(listener);
	}

	/** A ritual completed at {@code anchor} with {@code present} taking part (the anchor calls this; tests too). */
	public static void completed(ServerLevel level, BlockPos anchor, String ritual, List<ServerPlayer> present) {
		for (Completed listener : COMPLETED) {
			listener.completed(level, anchor, ritual, List.copyOf(present));
		}
	}

	/** How many anchors are loaded in a level (tests). */
	public static int loaded(ServerLevel level) {
		Set<BlockPos> anchors = ANCHORS.get(level.dimension());
		return anchors == null ? 0 : anchors.size();
	}
}
