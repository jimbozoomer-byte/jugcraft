package io.github.jimbozoomer.jugcraft.town;

import io.github.jimbozoomer.jugcraft.agriculture.HalloweenSeason;
import io.github.jimbozoomer.jugcraft.season.JugcraftSeasons;
import io.github.jimbozoomer.jugcraft.season.SeasonCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * The town's seasonal decoration. Its theme is the running event (December, then Halloween, then the Harvest Feast)
 * or else the season, from the server's own calendar ({@link JugcraftSeasons}); with seasons off it is summer's.
 * When the theme changes, every decor site (window boxes, banners, awnings, planters, lamps, flags and the square's
 * centrepiece; tools/town_decor.py) is due a change. The town's decorators walk round and change the sites near them
 * one by one ({@link Townsfolk}); a site in a loaded chunk that no decorator has reached after {@link #GRACE} ticks is
 * changed directly, a few a tick, and a chunk that loads with stale sites gets the current ones at once (no one was
 * there to watch). Earned items and the town's blocks are never touched: only the sites' own blocks change.
 */
public final class TownDecor {
	/** How often (ticks) the theme is checked. */
	public static final int CHECK_TICKS = 100;
	/** How long decorators have before the rest is changed directly (5 minutes). */
	public static final int GRACE = 6000;
	public static final int DIRECT_PER_TICK = 4;
	private static final Map<String, BlockState> PARSED = new HashMap<>();
	/** Sites a decorator is walking to: site -> decorator. */
	private static final Map<Integer, UUID> CLAIMS = new HashMap<>();
	private static @Nullable String forced;

	private TownDecor() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(TownDecor::tick);
		ServerTickEvents.END_SERVER_TICK.register(server -> TownFeast.tick(server.overworld()));
		ServerTickEvents.END_SERVER_TICK.register(server -> TownHarvestCrafts.tick(server.overworld()));
		ServerChunkEvents.CHUNK_LOAD.register((level, chunk, generated) -> {
			if (level == level.getServer().overworld()) {
				TownState state = TownState.get(level);
				if (state.origin() != null && state.built(chunk.getPos().pack())) {
					chunkBuilt(level, chunk.getPos());
				}
			}
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			CLAIMS.clear();
			forced = null;
		});
	}

	/** Today's theme. */
	public static String theme() {
		if (forced != null) {
			return forced;
		}
		if (JugcraftSeasons.isActive(SeasonCalendar.Event.DECEMBER)) {
			return "december";
		}
		if (HalloweenSeason.active()) {
			return "halloween";
		}
		if (JugcraftSeasons.isActive(SeasonCalendar.Event.HARVEST_FEAST)
                || (TownData.get().centerpiece.containsKey("harvest_v2")
                    && harvestDisplayOn(JugcraftSeasons.settings().effectiveDate(JugcraftSeasons.date())))) {
			return "harvest";
		}
		String season = SeasonCalendar.seasonName(JugcraftSeasons.today());
		return season.equals("off") ? "summer" : season;
	}

    static boolean harvestDisplayOn(java.time.LocalDate date) {
        return date.getMonthValue() == 11 && date.getDayOfMonth() >= 4;
    }

    /** Version only persisted site layouts, leaving shop/dialogue/calendar theme keys stable. */
    static String layoutTheme(String theme) {
        return theme.equals("harvest") && TownData.get().centerpiece.containsKey("harvest_v2") ? "harvest_v2" : theme;
    }

	/** Holds a theme until the server stops (null: follow the calendar again), for operators and tests. */
	public static void force(@Nullable String theme) {
		forced = theme;
	}

	private static void tick(MinecraftServer server) {
		ServerLevel level = server.overworld();
		TownState state = TownState.get(level);
		BlockPos origin = state.origin();
		if (origin == null) {
			return;
		}
		long now = level.getGameTime();
		if (server.getTickCount() % CHECK_TICKS == 0) {
			String theme = theme();
			if (!theme.equals(state.theme())) {
				boolean first = state.theme().isEmpty();
				state.setTheme(theme, now);
				CLAIMS.clear();
				if (!first) {
					announce(level, theme);
				}
			}
			welcome(level, state);
		}
		if (now - state.themeSince() >= GRACE && server.getTickCount() % 20 == 0) {
			int done = 0;
			for (TownData.Site site : TownData.get().sites) {
				if (done >= DIRECT_PER_TICK) {
					break;
				}
				if (!state.siteTheme(site.index()).equals(layoutTheme(state.theme())) && !CLAIMS.containsKey(site.index()) && loaded(level, origin, site)) {
					apply(level, site.index());
					done++;
				}
			}
		}
	}

	/** Sets every stale site in a chunk that was just built or loaded. */
	public static void chunkBuilt(ServerLevel level, ChunkPos chunk) {
		TownState state = TownState.get(level);
		BlockPos origin = state.origin();
		if (origin == null) {
			return;
		}
		if (state.theme().isEmpty()) {
			state.setTheme(theme(), level.getGameTime());
		}
		for (TownData.Site site : TownData.get().sites) {
			BlockPos anchor = origin.offset(site.anchor());
			if ((anchor.getX() >> 4) == chunk.x() && (anchor.getZ() >> 4) == chunk.z()
					&& !state.siteTheme(site.index()).equals(layoutTheme(state.theme())) && loaded(level, origin, site)) {
				apply(level, site.index());
			}
		}
	}

	/** The nearest site still due a change within `reach` of `from`, claimed for `decorator`; -1 if none. */
	public static int claim(ServerLevel level, BlockPos from, UUID decorator, int reach) {
		TownState state = TownState.get(level);
		BlockPos origin = state.origin();
		if (origin == null) {
			return -1;
		}
		CLAIMS.values().removeIf(decorator::equals);
		int best = -1;
		double bestDistance = (double) reach * reach;
		for (TownData.Site site : TownData.get().sites) {
			if (state.siteTheme(site.index()).equals(layoutTheme(state.theme())) || CLAIMS.containsKey(site.index())) {
				continue;
			}
			double d = origin.offset(site.anchor()).distSqr(from);
			if (d < bestDistance && loaded(level, origin, site)) {
				best = site.index();
				bestDistance = d;
			}
		}
		if (best >= 0) {
			CLAIMS.put(best, decorator);
		}
		return best;
	}

	public static void release(int site) {
		CLAIMS.remove(site);
	}

	/** Where a site is in the world (its first block). */
	public static @Nullable BlockPos where(ServerLevel level, int site) {
		BlockPos origin = TownState.get(level).origin();
		return origin == null ? null : origin.offset(TownData.get().sites.get(site).anchor());
	}

	/** Whether a site still shows an older theme. */
	public static boolean stale(ServerLevel level, int site) {
		TownState state = TownState.get(level);
		return !state.siteTheme(site).equals(layoutTheme(state.theme()));
	}

	/** Changes a site to the current theme. */
	public static void apply(ServerLevel level, int index) {
		TownState state = TownState.get(level);
		BlockPos origin = state.origin();
		if (origin == null) {
			return;
		}
		TownData data = TownData.get();
		TownData.Site site = data.sites.get(index);
        if (!loaded(level, origin, site)) { CLAIMS.remove(index); return; }
		String theme = layoutTheme(state.theme());
        if (site.kind().equals("retired")) {
            state.setSiteTheme(index, theme); CLAIMS.remove(index); return;
        }
        applyCells(level, cells(data, site, origin, state.siteTheme(index), theme));
		state.setSiteTheme(index, theme);
		CLAIMS.remove(index);
	}

    record DecorCell(BlockPos pos, BlockState expected, BlockState wanted) {}

    private static List<DecorCell> cells(TownData data, TownData.Site site, BlockPos origin, String previous, String theme) {
        var cells = new java.util.ArrayList<DecorCell>();
        for (int[] b : site.blocks()) {
            var expected = previous.isEmpty() ? data.state(data.index(b[0], b[1], b[2]))
                : stateFor(data, site, previous, b[3]);
            cells.add(new DecorCell(origin.offset(b[0], b[1], b[2]), expected, stateFor(data, site, theme, b[3])));
        }
        return cells;
    }

    /** A multiblock display must remain whole. One conflict preserves its entire authored site. */
    static boolean blockedDisplay(ServerLevel level, List<DecorCell> cells) {
        boolean multi = cells.stream().anyMatch(c -> isMulti(c.expected()) || isMulti(c.wanted()));
        if (!multi) return false;
        var positions = new java.util.HashSet<BlockPos>();
        cells.forEach(c -> positions.add(c.pos()));
        for (DecorCell c : cells) {
            if (cellChange(level, c.pos(), c.expected(), c.wanted()) == CellChange.PRESERVE) return true;
            for (BlockState state : new BlockState[]{c.expected(), c.wanted()}) {
                if (state.getBlock() instanceof io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock block) {
                    BlockPos root = block.masterPos(c.pos(), state);
                    for (int part = 0; part < block.cells().length; part++) {
                        if (!positions.contains(block.partPos(root,
                                state.getValue(io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock.FACING), part))) return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isMulti(BlockState state) {
        return state.getBlock() instanceof io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock;
    }

    /** Preflight before writing; remove old roots first to avoid secondary-part item drops. */
    static boolean applyCells(ServerLevel level, List<DecorCell> cells) {
        if (blockedDisplay(level, cells)) {
            var progress = TownUpgrades.progress(level);
            cells.forEach(c -> progress.preservedDecor.add(c.pos().asLong()));
            progress.setDirty();
            return false;
        }
        if (cells.stream().noneMatch(c -> isMulti(c.expected()) || isMulti(c.wanted()))) {
            cells.forEach(c -> updateAuthoredDecor(level,c.pos(),c.expected(),c.wanted()));
            return true;
        }
        // All cells were checked together on the server thread. Removal callbacks can clear their siblings;
        // write from the validated snapshot rather than mistaking those cleared cells for player edits.
        for (DecorCell c : cells) {
            if (c.expected().getBlock() instanceof io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock block
                    && block.part(c.expected()) == 0 && c.expected() != c.wanted()
                    && matchesAuthored(level.getBlockState(c.pos()), c.expected())) TownBuilder.set(level,c.pos(),Blocks.AIR.defaultBlockState());
        }
        for (DecorCell c : cells) TownBuilder.set(level,c.pos(),c.wanted());
        return true;
    }

    /** Public lantern toggles are interaction state, not replacement of an authored prop. */
    private static boolean matchesAuthored(BlockState actual, BlockState expected) {
        if (actual == expected) return true;
        if (isMulti(expected) && actual.is(expected.getBlock())) {
            var lit = io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock.LIT;
            return actual.setValue(lit, expected.getValue(lit)) == expected;
        }
        return false;
    }

    enum CellChange { UNCHANGED, REPLACE, PRESERVE }

    /** Shared by the read-only preview and mutation; preview never records owner overrides. */
    static CellChange cellChange(ServerLevel level, BlockPos pos, BlockState expected, BlockState wanted) {
        if (TownUpgrades.preservedDecor(level, pos)) return CellChange.PRESERVE;
        if (level.getBlockState(pos) == wanted) return CellChange.UNCHANGED;
        var entity = level.getBlockEntity(pos);
        boolean plainBanner = entity instanceof net.minecraft.world.level.block.entity.BannerBlockEntity banner
            && banner.getCustomName() == null && banner.getPatterns().layers().isEmpty();
        if (!matchesAuthored(level.getBlockState(pos), expected) || (entity != null && !plainBanner) || TownUpgrades.fixtureNearby(level,pos)) {
            return CellChange.PRESERVE;
        }
        return CellChange.REPLACE;
    }

    /** Compare-and-set one seasonal cell, retaining a durable owner override after any conflict. */
    static boolean updateAuthoredDecor(ServerLevel level, BlockPos pos, BlockState expected, BlockState wanted) {
        CellChange change = cellChange(level, pos, expected, wanted);
        if (change == CellChange.PRESERVE) {
            var progress = TownUpgrades.progress(level);
            if (progress.preservedDecor.add(pos.asLong())) progress.setDirty();
            return false;
        }
        if (change == CellChange.REPLACE) TownBuilder.set(level,pos,wanted);
        return true;
    }

    /** Counts only loaded sites. Never loads chunks, changes themes, or marks sites migrated. */
    public record Preview(int replacements, int unchanged, int preserved, int unloadedSites) {}

    public static Preview preview(ServerLevel level, String theme) {
        TownData data = TownData.get();
        if (!data.themes.contains(theme)) throw new IllegalArgumentException("Unknown town theme: " + theme);
        TownState state = TownState.get(level);
        BlockPos origin = state.origin();
        if (origin == null) return new Preview(0, 0, 0, 0);
        int replacements = 0, unchanged = 0, preserved = 0, unloaded = 0;
        for (TownData.Site site : data.sites) {
            if (site.kind().equals("retired")) continue;
            if (!loaded(level, origin, site)) { unloaded++; continue; }
            var planned = cells(data, site, origin, state.siteTheme(site.index()), layoutTheme(theme));
            if (blockedDisplay(level, planned)) { preserved += planned.size(); continue; }
            for (DecorCell cell : planned) {
                switch (cellChange(level, cell.pos(), cell.expected(), cell.wanted())) {
                    case REPLACE -> replacements++;
                    case UNCHANGED -> unchanged++;
                    case PRESERVE -> preserved++;
                }
            }
        }
        return new Preview(replacements, unchanged, preserved, unloaded);
    }

	/** What a site's block shows in a theme (air where the theme leaves it empty). */
	public static BlockState stateFor(TownData data, TownData.Site site, String theme, int slot) {
		if (site.kind().equals("centerpiece")) {
			Map<Integer, String> scene = data.centerpiece.get(theme);
			String text = scene == null ? null : scene.get(slot);
			return text == null ? Blocks.AIR.defaultBlockState() : parse(text, site.facing());
		}
		Map<String, List<List<String>>> kind = data.decor.get(site.kind());
		List<List<String>> slots = kind == null ? null : kind.get(theme.equals("harvest_v2") ? "harvest" : theme);
		if (slots == null || slots.isEmpty()) {
			return Blocks.AIR.defaultBlockState();
		}
		List<String> choices = slots.get(Math.min(slot, slots.size() - 1));
		return parse(choices.get(site.index() % choices.size()), site.facing());
	}

	private static BlockState parse(String text, Direction facing) {
		String filled = text.replace("{facing}", facing.getSerializedName()).replace("{rotation}", Integer.toString(rotation(facing)));
		return PARSED.computeIfAbsent(filled, TownData::parse);
	}

	/** A standing banner's rotation facing this way (0 is south, counting clockwise in sixteenths). */
	static int rotation(Direction facing) {
		return switch (facing) {
			case WEST -> 4;
			case NORTH -> 8;
			case EAST -> 12;
			default -> 0;
		};
	}

	private static boolean loaded(ServerLevel level, BlockPos origin, TownData.Site site) {
		for (int[] b : site.blocks()) {
			int x = origin.getX() + b[0];
			int z = origin.getZ() + b[2];
			if (level.getChunkSource().getChunkNow(x >> 4, z >> 4) == null) {
				return false;
			}
		}
		return true;
	}

	private static void announce(ServerLevel level, String theme) {
		for (ServerPlayer player : level.players()) {
			if (Town.isInside(level, player.blockPosition())) {
				player.sendSystemMessage(Component.translatable("message.jugcraft.town.decorating",
						Component.translatable("theme.jugcraft." + theme)));
			}
		}
	}

	/** A player's first time inside the wall: the town welcomes them with a few Jugs. */
	private static void welcome(ServerLevel level, TownState state) {
		for (ServerPlayer player : level.players()) {
			if (Town.isInside(level, player.blockPosition()) && state.welcome(player.getUUID())) {
				long jugs = TownShops.get().welcomeJugs;
				Jugs.add(level.getServer(), player.getUUID(), jugs);
				player.sendSystemMessage(Component.translatable("message.jugcraft.town.welcome", jugs));
			}
		}
	}
}
