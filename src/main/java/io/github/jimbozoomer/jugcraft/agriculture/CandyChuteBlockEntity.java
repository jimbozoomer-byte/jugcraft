package io.github.jimbozoomer.jugcraft.agriculture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The treats sliding down a Trick-or-Treat Candy Chute ({@link CandyChuteBlock}, on its first block): each with the game
 * time it went in at the funnel. The server delivers each at the mouth {@value CandyChuteBlock#SLIDE_TICKS} ticks after:
 * into the Candy Bag of a player within {@value CandyChuteBlock#CATCH} blocks of the mouth carrying one, or out onto the
 * path. Every client is sent the list to draw them sliding (client/CandyChuteRenderer); it is saved with the world, and
 * what is in the chute when it is broken drops.
 */
public class CandyChuteBlockEntity extends BlockEntity {
	/** A treat in the chute, and when it went in. */
	public record Sliding(ItemStack treat, long start) {
		static final Codec<Sliding> CODEC = RecordCodecBuilder.create(i -> i.group(
				ItemStack.CODEC.fieldOf("treat").forGetter(Sliding::treat),
				Codec.LONG.fieldOf("start").forGetter(Sliding::start)).apply(i, Sliding::new));
	}

	private final List<Sliding> sliding = new ArrayList<>();
	private long lastDrop = Long.MIN_VALUE;

	public CandyChuteBlockEntity(BlockPos pos, BlockState state) {
		super(JugcraftAgriculture.CANDY_CHUTE_ENTITY, pos, state);
	}

	/** What is sliding down the chute now. */
	public List<Sliding> sliding() {
		return List.copyOf(sliding);
	}

	/**
	 * One of {@code treat} goes in at the funnel at game time {@code now}, if it is a treat, the chute holds fewer than
	 * {@value CandyChuteBlock#HOLD} and the last went in {@value CandyChuteBlock#DROP_TICKS} ticks ago or more. Returns
	 * whether it went in; {@code treat} is one fewer if so.
	 */
	public boolean drop(ItemStack treat, long now) {
		if (treat.isEmpty() || !treat.is(CandyBagItem.TREATS) || sliding.size() >= CandyChuteBlock.HOLD
				|| (lastDrop != Long.MIN_VALUE && now - lastDrop < CandyChuteBlock.DROP_TICKS && now >= lastDrop)) {
			return false;
		}
		sliding.add(new Sliding(treat.copyWithCount(1), now));
		treat.shrink(1);
		lastDrop = now;
		changed();
		return true;
	}

	/** The server's tick: each treat that has slid the whole way is delivered at the mouth. Nothing to do with the chute empty. */
	public void serverTick(ServerLevel level) {
		if (sliding.isEmpty()) {
			return;
		}
		long now = level.getGameTime();
		boolean delivered = false;
		for (Iterator<Sliding> it = sliding.iterator(); it.hasNext(); ) {
			Sliding treat = it.next();
			if (now - treat.start() >= CandyChuteBlock.SLIDE_TICKS) {
				it.remove();
				deliver(level, treat.treat().copy());
				delivered = true;
			}
		}
		if (delivered) {
			changed();
		}
	}

	/** At the mouth: into the bag of whoever stands there with one, or out onto the path. */
	private void deliver(ServerLevel level, ItemStack treat) {
		Direction facing = getBlockState().getValue(MultiDecorationBlock.FACING);
		Vec3 mouth = CandyChuteBlock.mouth(worldPosition, facing);
		for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, new AABB(mouth, mouth).inflate(CandyChuteBlock.CATCH),
				player -> !player.isSpectator())) {
			ItemStack bag = TrickOrTreat.candyBag(player);
			if (!bag.isEmpty()) {
				CandyBagItem.fill(bag, treat);
				if (treat.isEmpty()) {
					level.playSound(null, mouth.x, mouth.y, mouth.z, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.4F, 1.4F);
					return;
				}
			}
		}
		ItemEntity out = new ItemEntity(level, mouth.x, mouth.y, mouth.z, treat, facing.getStepX() * 0.12, 0.08, facing.getStepZ() * 0.12);
		out.setDefaultPickUpDelay();
		level.addFreshEntity(out);
	}

	private void changed() {
		setChanged();
		if (level != null) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
		}
	}

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		if (level != null) {
			for (Sliding treat : sliding) {
				Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), treat.treat());
			}
			sliding.clear();
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		sliding.clear();
		sliding.addAll(input.read("sliding", Sliding.CODEC.listOf()).orElse(List.of()));
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.store("sliding", Sliding.CODEC.listOf(), List.copyOf(sliding));
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
