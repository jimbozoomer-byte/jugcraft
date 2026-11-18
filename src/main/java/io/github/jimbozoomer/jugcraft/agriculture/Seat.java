package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * What a player sits on when they sit on a {@link Sittable} block: an invisible entity at the seat's surface. It is
 * made when someone sits down and goes as soon as nobody is on it or its block is no longer a seat, so it never
 * outlives its sitter. (It can be saved, as players may only ride what can be: a player who leaves while sitting comes
 * back sitting, and a seat saved empty goes on its first tick.)
 */
public class Seat extends Entity {
	public Seat(EntityType<? extends Seat> type, Level level) {
		super(type, level);
		noPhysics = true;
		setNoGravity(true);
	}

	/** A block players can sit on. */
	public interface Sittable {
		/** How high above the block's base the sitter's seat is, in blocks. */
		double seatHeight(BlockState state);

		/**
		 * The block whose seat using {@code pos} means: the seat's one canonical cell, where its sitter's Seat stands and
		 * where a companion's reservation is kept (CompanionSeats). A seat of one block is its own; a seat reached from
		 * more than one block (a high-backed settle's half) answers its lower block from each of them.
		 */
		default BlockPos seatCell(BlockGetter level, BlockPos pos, BlockState state) {
			return pos;
		}
	}

	/** Sits {@code player} on the seat at {@code pos}, unless someone already sits there. Server side only. */
	public static boolean sit(ServerLevel level, BlockPos pos, BlockState state, Player player) {
		// Always at the seat's canonical cell, whichever of its blocks was used, so one sitter, player or companion, a seat.
		if (state.getBlock() instanceof Sittable seatBlock) {
			BlockPos cell = seatBlock.seatCell(level, pos, state);
			if (!cell.equals(pos)) {
				pos = cell;
				state = level.getBlockState(cell);
			}
		}
		if (local.peepo.CompanionSeats.isReserved(level,pos)) return false;
		if (!(state.getBlock() instanceof Sittable sittable) || player.isPassenger() || player.isSecondaryUseActive()) {
			return false;
		}
		if (!level.getEntitiesOfClass(Seat.class, new AABB(pos), Seat::isVehicle).isEmpty()) {
			return false;
		}
		Seat seat = JugcraftAgriculture.SEAT.create(level, EntitySpawnReason.TRIGGERED);
		if (seat == null) {
			return false;
		}
		seat.setPos(pos.getX() + 0.5, pos.getY() + sittable.seatHeight(state), pos.getZ() + 0.5);
		seat.setYRot(player.getYRot());
		if (!level.addFreshEntity(seat) || !player.startRiding(seat)) {
			seat.discard();
			return false;
		}
		return true;
	}

	/** Who sits at {@code pos}, if anyone. */
	public static List<Seat> at(Level level, BlockPos pos) {
		return level.getEntitiesOfClass(Seat.class, new AABB(pos), Seat::isVehicle);
	}

	@Override
	public void tick() {
		super.tick();
		if (!level().isClientSide() && (!isVehicle() || !(level().getBlockState(blockPosition()).getBlock() instanceof Sittable))) {
			ejectPassengers();
			discard();
		}
	}

	/** Getting up leaves the sitter standing on top of the seat. */
	@Override
	public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
		BlockPos pos = blockPosition();
		double top = level().getBlockState(pos).getCollisionShape(level(), pos).max(Direction.Axis.Y);
		return new Vec3(getX(), pos.getY() + Math.max(0.0, top), getZ());
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		return false;
	}

	@Override
	public boolean isPickable() {
		return false;
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
	}
}
