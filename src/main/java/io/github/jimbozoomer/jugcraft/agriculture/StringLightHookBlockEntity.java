package io.github.jimbozoomer.jugcraft.agriculture;

import io.github.jimbozoomer.jugcraft.energy.SimpleEnergyStorage;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * A String Light Hook's strand and power. A hook holds at most one strand, to another hook at most
 * {@value #MAX_LENGTH} blocks away ({@link #link}): Jack-o'-Lantern String Lights or Bat Bunting ({@link Strand});
 * the client draws it from here. Every {@value #PERIOD} ticks the
 * hook lights if it has a redstone signal; failing that, if its buffer holds enough, it draws {@value #USE} JE a tick
 * from the electric network (buffer {@value #CAPACITY}, taking up to {@value #INPUT} a tick) and lights. Every
 * {@value #CHECK_TICKS} ticks a strand whose far hook is gone comes down (dropped as an item). Breaking the hook drops
 * its strand too. A strand that lights along its length (Café String Lights) also, while it glows, puts a lamp (a
 * vanilla light block, light {@value #LAMP_LIGHT}) in each empty spot along it, which this hook remembers and puts out
 * when it goes dark, comes down or the hook is broken.
 */
public class StringLightHookBlockEntity extends BlockEntity {
	public static final int MAX_LENGTH = 16;
	public static final int USE = 1;
	public static final int CAPACITY = 200;
	public static final int INPUT = 20;
	public static final int PERIOD = 10;
	public static final int CHECK_TICKS = 100;
	/** A strand that lights along its length: its lamps' light, and about how far apart they are, in blocks. */
	public static final int LAMP_LIGHT = 12;
	public static final double LAMP_SPACING = 2.0;

	/** What can be strung between hooks, and the item each is. */
	public enum Strand {
		LIGHTS("lights", "jack_o_lantern_string_lights"),
		BUNTING("bunting", "bat_bunting"),
		/** Pumpkin Night (tools/decor20.py GARLANDS): a twisting pumpkin vine and a twine of autumn leaves, both with warm bulbs. */
		PUMPKIN_VINE("pumpkin_vine", "pumpkin_vine_garland"),
		AUTUMN_LEAVES("autumn_leaves", "autumn_leaf_garland"),
		/**
		 * Fall Garden & Porch, part 4 (tools/fall_garden.py CAFE_LIGHTS): café lights, warm Edison bulbs on a black cord,
		 * which light the air along their length while they glow ({@link #lamps}).
		 */
		CAFE("cafe", "cafe_string_lights", true),
		/**
		 * The Harvest Festival Grounds, part 4 (tools/harvest_festival.py BUNTING): a twine cord hung with leaf-shaped
		 * pennants, a red maple leaf, a gold aspen leaf and a hessian pennant in turn.
		 */
		HARVEST("harvest", "harvest_bunting");

		public final String name;
		public final String item;
		/** Whether, while it glows, it lights the air along it, not just its hooks. */
		public final boolean lightsAlong;

		Strand(String name, String item) {
			this(name, item, false);
		}

		Strand(String name, String item, boolean lightsAlong) {
			this.name = name;
			this.item = item;
			this.lightsAlong = lightsAlong;
		}

		public static Strand byName(String name) {
			for (Strand strand : values()) {
				if (strand.name.equals(name)) {
					return strand;
				}
			}
			return LIGHTS;
		}
	}

	final SimpleEnergyStorage energy = new SimpleEnergyStorage(CAPACITY, INPUT, 0, this::setChanged);
	private @Nullable BlockPos link;
	private Strand strand = Strand.LIGHTS;
	/** The lamps (vanilla light blocks) this hook's strand has put in the air along it, to put out again. */
	private final List<BlockPos> lamps = new ArrayList<>();

	public StringLightHookBlockEntity(BlockPos pos, BlockState state) {
		super(JugcraftAgriculture.STRING_LIGHT_HOOK_ENTITY, pos, state);
	}

	public SimpleEnergyStorage energy() {
		return energy;
	}

	/** The hook this one's strand runs to, or null. */
	public @Nullable BlockPos link() {
		return link;
	}

	/** What this hook's strand is (meaningful while it has one). */
	public Strand strand() {
		return strand;
	}

	/** Strings this hook to the hook at {@code other} with string lights (the caller has checked the rules). */
	public void stringTo(BlockPos other) {
		stringTo(other, Strand.LIGHTS);
	}

	/** Strings this hook to the hook at {@code other} with {@code strand} (the caller has checked the rules). */
	public void stringTo(BlockPos other, Strand strand) {
		link = other.immutable();
		this.strand = strand;
		changed();
	}

	/** Takes this hook's strand down, dropping it as an item if {@code drop}. */
	public void unstring(boolean drop) {
		if (link == null) {
			return;
		}
		if (level instanceof ServerLevel server) {
			putOut(server);
		}
		link = null;
		if (drop && level != null) {
			Block.popResource(level, worldPosition, new ItemStack(JugcraftAgriculture.item(strand.item)));
		}
		changed();
	}

	/** Whether the strand between hooks {@code a} and {@code b} glows: either hook is lit. */
	public static boolean glows(BlockGetter level, BlockPos a, BlockPos b) {
		return lit(level.getBlockState(a)) || lit(level.getBlockState(b));
	}

	private static boolean lit(BlockState state) {
		return state.getBlock() instanceof StringLightHookBlock && state.getValue(StringLightHookBlock.LIT);
	}

	void serverTick(ServerLevel level) {
		long time = level.getGameTime() + worldPosition.asLong();
		if (Math.floorMod(time, PERIOD) == 0) {
			update(level);
		}
		if (link != null && Math.floorMod(time, CHECK_TICKS) == 0 && level.isLoaded(link)
				&& !(level.getBlockState(link).getBlock() instanceof StringLightHookBlock)) {
			unstring(true);
		}
	}

	/** Works out whether the hook is lit (redstone first, else electricity for the next {@value #PERIOD} ticks). */
	public boolean update(ServerLevel level) {
		boolean lit = level.hasNeighborSignal(worldPosition);
		if (!lit && energy.getAmount() >= (long) USE * PERIOD) {
			energy.setAmount(energy.getAmount() - (long) USE * PERIOD);
			setChanged();
			lit = true;
		}
		BlockState state = getBlockState();
		if (state.getValue(StringLightHookBlock.LIT) != lit) {
			level.setBlock(worldPosition, state.setValue(StringLightHookBlock.LIT, lit), Block.UPDATE_ALL);
		}
		if (link != null && strand.lightsAlong && level.isLoaded(link)) {
			light(level, glows(level, worldPosition, link));
		}
		return lit;
	}

	/** The lamps its strand has lit (empty unless it lights along its length and glows). */
	public List<BlockPos> lamps() {
		return List.copyOf(lamps);
	}

	/**
	 * Where a strand that lights along its length puts its lamps: about every {@value #LAMP_SPACING} blocks along it, just
	 * under the cord where it sags (as client/StringLightsRenderer draws it), never in either hook's block.
	 */
	public List<BlockPos> lampSpots() {
		List<BlockPos> spots = new ArrayList<>();
		if (link == null) {
			return spots;
		}
		Vec3 start = Vec3.atCenterOf(worldPosition);
		Vec3 offset = Vec3.atLowerCornerOf(link.subtract(worldPosition));
		double length = offset.length();
		double sag = 0.15 + 0.05 * length;
		int count = Math.max(1, (int) Math.floor(length / LAMP_SPACING));
		for (int i = 1; i <= count; i++) {
			double t = i / (double) (count + 1);
			BlockPos spot = BlockPos.containing(start.add(offset.scale(t)).subtract(0.0, sag * 4.0 * t * (1.0 - t) + 0.2, 0.0));
			if (!spot.equals(worldPosition) && !spot.equals(link) && !spots.contains(spot)) {
				spots.add(spot);
			}
		}
		return spots;
	}

	private static boolean isLamp(BlockState state) {
		return state.is(Blocks.LIGHT) && state.getValue(LightBlock.LEVEL) == LAMP_LIGHT;
	}

	/**
	 * While its strand glows, puts a lamp in each of its spots that is empty air in a loaded chunk (never over anything);
	 * while it doesn't, puts its own lamps out. Work bounded by the strand's length, every {@value #PERIOD} ticks.
	 */
	private void light(ServerLevel level, boolean glowing) {
		if (!glowing) {
			putOut(level);
			return;
		}
		for (BlockPos spot : lampSpots()) {
			if (!level.isLoaded(spot)) {
				continue;
			}
			BlockState there = level.getBlockState(spot);
			if (there.isAir()) {
				level.setBlock(spot, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, LAMP_LIGHT), Block.UPDATE_ALL);
				if (!lamps.contains(spot)) {
					lamps.add(spot);
				}
				setChanged();
			} else if (lamps.contains(spot) && !isLamp(there)) {
				// Built over: no longer ours.
				lamps.remove(spot);
				setChanged();
			}
		}
	}

	/** Puts out the lamps it lit that are still lamps; those in unloaded chunks wait for the next time. */
	private void putOut(ServerLevel level) {
		if (lamps.isEmpty()) {
			return;
		}
		lamps.removeIf(pos -> {
			if (!level.isLoaded(pos)) {
				return false;
			}
			if (isLamp(level.getBlockState(pos))) {
				level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
			}
			return true;
		});
		setChanged();
	}

	private void changed() {
		setChanged();
		if (level != null) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
		}
	}

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		if (level instanceof ServerLevel server) {
			putOut(server);
		}
		if (link != null && level != null) {
			Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(JugcraftAgriculture.item(strand.item)));
			link = null;
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		link = input.getLong("link").map(BlockPos::of).orElse(null);
		strand = Strand.byName(input.getStringOr("strand", Strand.LIGHTS.name));
		energy.setAmount(input.getLong("energy").orElse(0L));
		lamps.clear();
		lamps.addAll(input.read("lamps", BlockPos.CODEC.listOf()).orElse(List.of()));
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if (link != null) {
			output.putLong("link", link.asLong());
			output.putString("strand", strand.name);
		}
		output.putLong("energy", energy.getAmount());
		if (!lamps.isEmpty()) {
			output.store("lamps", BlockPos.CODEC.listOf(), List.copyOf(lamps));
		}
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return saveCustomOnly(registries);
	}
}
