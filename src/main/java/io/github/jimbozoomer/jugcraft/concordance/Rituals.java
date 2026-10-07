package io.github.jimbozoomer.jugcraft.concordance;

import io.github.jimbozoomer.jugcraft.concordance.ritual.StructurePattern;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

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
				changed(server, pos);
			}
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> ANCHORS.clear());
	}

	/** A block that could be part of a circle changed at {@code pos}: anchors in reach check again when next asked. */
	public static void changed(ServerLevel level, BlockPos pos) {
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
			}
		}
	}

	/** How many anchors are loaded in a level (tests). */
	public static int loaded(ServerLevel level) {
		Set<BlockPos> anchors = ANCHORS.get(level.dimension());
		return anchors == null ? 0 : anchors.size();
	}
}
