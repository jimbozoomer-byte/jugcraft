package io.github.jimbozoomer.jugcraft.lair;

import io.github.jimbozoomer.jugcraft.lair.NameStoneBlock.Rune;
import net.minecraft.core.BlockPos;

/**
 * Where the Archive of Last Names' parts lie (docs/features/crypt-archive.md), relative to its template's corner: the
 * numbers of tools/crypt_archive.py, checked against it by tools/check_mod_data.py. An instance's part is at
 * {@code Lair.CRYPT_ARCHIVE.origin(slot)} offset by these.
 */
public final class CryptArchive {
	/** Where anyone in the hall stands, in the throne's apse and on the landing. */
	public static final int STAND = 5;
	public static final int APSE_STAND = 7;
	public static final int LANDING_STAND = 11;
	/** The hall's open cells, inclusive: west to east (x) and north to south (z). */
	public static final int HALL_WEST = 12;
	public static final int HALL_EAST = 52;
	public static final int HALL_NORTH = 14;
	public static final int HALL_SOUTH = 54;
	public static final BlockPos LECTERN = new BlockPos(32, 5, 53);
	public static final BlockPos PHYLACTERY = new BlockPos(32, 6, 34);
	public static final BlockPos THRONE = new BlockPos(32, 7, 9);
	/** Each name's grave seal, in the order of {@link Rune}: the Sexton's (spade), the Crowned's, the Scribe's. */
	public static final BlockPos[] SEALS = {new BlockPos(5, 6, 34), new BlockPos(32, 8, 5), new BlockPos(59, 6, 34)};
	/** Each name's name-stone, over its seal, in the same order. */
	public static final BlockPos[] STONES = {new BlockPos(5, 7, 34), new BlockPos(32, 9, 5), new BlockPos(59, 7, 34)};

	private CryptArchive() {
	}

	public static BlockPos seal(BlockPos origin, Rune rune) {
		return origin.offset(SEALS[rune.ordinal()]);
	}

	public static BlockPos stone(BlockPos origin, Rune rune) {
		return origin.offset(STONES[rune.ordinal()]);
	}

	/** The name-stone of {@code rune}, found from where the lectern stands. */
	public static BlockPos stoneFrom(BlockPos lectern, Rune rune) {
		return stone(lectern.subtract(LECTERN), rune);
	}
}
