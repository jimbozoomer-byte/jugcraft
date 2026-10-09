package io.github.jimbozoomer.jugcraft.walker;

import io.github.jimbozoomer.jugcraft.artillery.ArtilleryShell;
import io.github.jimbozoomer.jugcraft.artillery.JugcraftArtillery;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The Howitzer Walker (docs/features/howitzer-walker.md), after the owner's own model: a tall two-legged artillery
 * walker, a riveted olive hull carrying a howitzer on trunnions over reverse-jointed sprung legs. It walks, turns,
 * climbs, jumps and burns fuel exactly as the {@link DieselWalker} does. Holding use fires the howitzer where the pilot
 * looks, a Heavy Shell every {@value #CANNON_COOLDOWN} ticks from the pilot's own (none in creative), faster and so
 * farther than the Armoured Walker's hull gun: a damage-only blast, as every gun's. Attack stomps: everything within
 * {@value #STOMP_REACH} blocks of its feet is hit for {@value #STOMP_DAMAGE} and thrown. The pilot sits in a roof hatch,
 * head out. It is the toughest walker ({@value #HEALTH} to knock down) and has no drill.
 */
public class HowitzerWalker extends DieselWalker {
	public static final int CANNON_COOLDOWN = 60;
	public static final double CANNON_SPEED = 2.8;
	public static final int STOMP_DAMAGE = 10;
	public static final double STOMP_KNOCKBACK = 1.2;
	public static final double STOMP_REACH = 2.5;
	public static final int STOMP_COOLDOWN = 30;
	public static final int HEALTH = 100;
	public static final float WIDTH = 2.4F;
	public static final float HEIGHT = 5.2F;
	/** The barrel's mouth at rest, in pixels before turning with the walker (MUZZLE in tools/howitzer_walker.py). */
	public static final Vec3 MUZZLE_PIXELS = new Vec3(-7, 86.57, 35.99);
	/** The roof hatch the pilot sits in. */
	private static final Vec3 HATCH = new Vec3(0, 3.9, -0.2);
	private static final EntityDataAccessor<Integer> FIRED_AT = SynchedEntityData.defineId(HowitzerWalker.class, EntityDataSerializers.INT);
	private int lastShot = -CANNON_COOLDOWN;
	private boolean warned;
	/** Client side: the tick it last fired (for the gun's recoil). */
	private int firedAt = -100;
	/** Client side: how much it is walking, 0 to 1, eased so its bounce fades in and out rather than snapping. */
	private float gait;
	private float lastGait;
	private double lastX;
	private double lastZ;

	public HowitzerWalker(EntityType<? extends HowitzerWalker> type, Level level) {
		super(type, level);
	}

	/** Client side: ticks since the gun last fired. */
	public int sinceShot() {
		return tickCount - firedAt;
	}

	/** Client side: how much it is walking this frame, 0 standing to 1 striding. */
	public float gait(float partialTick) {
		return Mth.lerp(partialTick, lastGait, gait);
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide()) {
			double moved = Math.sqrt((getX() - lastX) * (getX() - lastX) + (getZ() - lastZ) * (getZ() - lastZ));
			lastX = getX();
			lastZ = getZ();
			lastGait = gait;
			gait = Mth.lerp(0.15F, gait, moved > 0.02 ? 1.0F : 0.0F);
		}
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		super.onSyncedDataUpdated(key);
		if (FIRED_AT.equals(key) && level().isClientSide() && entityData.get(FIRED_AT) >= 0) {
			firedAt = tickCount;
		}
	}

	/** Holding use: fire the howitzer, when it is loaded, toward where the pilot looks. Never counts as drilling. */
	@Override
	protected boolean useHeld(ServerLevel level, ServerPlayer pilot) {
		if (tickCount - lastShot < CANNON_COOLDOWN) {
			return false;
		}
		if (!pilot.getAbilities().instabuild) {
			int slot = pilot.getInventory().findSlotMatchingItem(new ItemStack(JugcraftArtillery.HEAVY_SHELL_ITEM));
			if (slot < 0) {
				if (!warned) {
					pilot.sendOverlayMessage(Component.translatable("message.jugcraft.artillery.no_shells",
							new ItemStack(JugcraftArtillery.HEAVY_SHELL_ITEM).getHoverName()));
					warned = true;
				}
				return false;
			}
			pilot.getInventory().removeItem(slot, 1);
		}
		warned = false;
		lastShot = tickCount;
		entityData.set(FIRED_AT, tickCount);
		Vec3 muzzle = position().add(MUZZLE_PIXELS.scale(1 / 16.0).yRot(-getYRot() * Mth.DEG_TO_RAD));
		Vec3 aim = pilot.getViewVector(1.0F);
		ArtilleryShell shell = new ArtilleryShell(JugcraftArtillery.HEAVY_SHELL, level, pilot);
		shell.setPos(muzzle);
		shell.shoot(aim.x, aim.y, aim.z, (float) CANNON_SPEED, 0.5F);
		level.addFreshEntity(shell);
		level.sendParticles(ParticleTypes.LARGE_SMOKE, muzzle.x, muzzle.y, muzzle.z, 14, 0.3, 0.3, 0.3, 0.03);
		level.sendParticles(ParticleTypes.FLAME, muzzle.x, muzzle.y, muzzle.z, 6, 0.1, 0.1, 0.1, 0.02);
		level.playSound(null, muzzle.x, muzzle.y, muzzle.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 2.5F, 0.6F);
		return false;
	}

	/** Attack: a stomp, into everything round its feet. */
	@Override
	protected void attack(ServerLevel level, ServerPlayer pilot, Vec3 heading) {
		Vec3 centre = position().add(0, 0.5, 0);
		AABB reach = new AABB(centre, centre).inflate(STOMP_REACH, 1.5, STOMP_REACH);
		for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, reach, e -> e != pilot && e.isAlive())) {
			if (target.hurtServer(level, level.damageSources().playerAttack(pilot), STOMP_DAMAGE)) {
				Vec3 away = target.position().subtract(position());
				away = away.horizontalDistanceSqr() < 1.0E-4 ? heading : away.normalize();
				target.setDeltaMovement(target.getDeltaMovement().add(away.x * STOMP_KNOCKBACK, 0.6, away.z * STOMP_KNOCKBACK));
				if (target instanceof ServerPlayer hit) {
					hit.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(hit));
				}
			}
		}
		level.playSound(null, centre.x, centre.y, centre.z, SoundEvents.IRON_GOLEM_STEP, SoundSource.PLAYERS, 1.5F, 0.5F);
		level.playSound(null, centre.x, centre.y, centre.z, SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.6F, 0.5F);
	}

	@Override
	protected int attackCooldown() {
		return STOMP_COOLDOWN;
	}

	@Override
	protected int health() {
		return HEALTH;
	}

	@Override
	protected ItemStack dropStack() {
		return new ItemStack(JugcraftWalkers.HOWITZER_WALKER_ITEM);
	}

	@Override
	protected Vec3 seat() {
		return HATCH;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(FIRED_AT, -100);
	}
}
