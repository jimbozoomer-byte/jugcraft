package io.github.jimbozoomer.jugcraft.guns;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The server's half of the guns: it alone fires, spends and loads rounds and deals damage. The client only asks
 * ({@link GunShotPayload}, {@link GunReloadPayload}) and plays the animations and sounds at once for its own player.
 * <ul>
 * <li>A shot needs a gun in the main hand, a loaded round (a creative player spends none) and the trigger's interval:
 * each player has a little credit that refills at one shot per interval, up to {@link #BURST}, so a late packet does
 * not cost a shot and a fast client gains none.</li>
 * <li>A magazine reload takes the gun's reload time and then loads what the inventory holds; a shell-at-a-time reload
 * loads one round after each shell's time, and a shot cuts it short. Switching away from the gun stops a reload.</li>
 * <li>Each bullet goes from the eye along the look, strayed by the gun's spread (less when aiming), to the first block
 * or creature in range. Creatures the shooter may not strike (allies, their own mount, marker stands, or anything
 * other code refuses through AttackEntityCallback, such as the town's protection) are passed through. A shotgun's
 * pellets on one creature land as one hit.</li>
 * </ul>
 */
public final class GunShots {
	/** Shots a player may bank (see the class comment). */
	static final double BURST = 2.0;
	/** How far a bullet's box test reaches past a creature's hitbox (blocks). */
	private static final double HIT_MARGIN = 0.15;

	private static final Map<UUID, Trigger> TRIGGERS = new HashMap<>();
	private static final Map<UUID, Reload> RELOADS = new LinkedHashMap<>();

	private GunShots() {
	}

	static void register() {
		PayloadTypeRegistry.serverboundPlay().register(GunShotPayload.TYPE, GunShotPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(GunReloadPayload.TYPE, GunReloadPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(GunActionPayload.TYPE, GunActionPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(GunShotPayload.TYPE, (payload, context) -> fire(context.player()));
		ServerPlayNetworking.registerGlobalReceiver(GunReloadPayload.TYPE, (payload, context) -> reload(context.player()));
		ServerTickEvents.END_SERVER_TICK.register(GunShots::tick);
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> forget(handler.getPlayer()));
	}

	/** Whether this player is reloading. */
	public static boolean reloading(ServerPlayer player) {
		return RELOADS.containsKey(player.getUUID());
	}

	/** Pulls the trigger of the gun in the player's main hand; whether it fired. */
	public static boolean fire(ServerPlayer player) {
		ItemStack stack = player.getMainHandItem();
		if (!(stack.getItem() instanceof GunItem gun) || !player.isAlive() || player.isSpectator()) {
			return false;
		}
		GunSpec spec = GunItem.spec(stack);
		ServerLevel level = (ServerLevel) player.level();
		Reload loading = RELOADS.get(player.getUUID());
		if (loading != null) {
			if (!spec.byShell()) {
				return false; // the magazine is out
			}
			RELOADS.remove(player.getUUID());
			announce(player, GunActionPayload.STOP, 0);
		}
		boolean free = player.hasInfiniteMaterials();
		int loaded = GunItem.loaded(stack);
		if (loaded <= 0 && !free) {
			return false;
		}
		if (!spend(player, spec, level.getGameTime())) {
			return false;
		}
		boolean aiming = GunItem.aiming(player, stack);
		shoot(level, player, spec, aiming ? spec.aimSpread() : spec.hipSpread());
		if (!free) {
			GunItem.setLoaded(stack, loaded - 1);
		}
		player.awardStat(Stats.ITEM_USED.get(gun));
		player.gameEvent(GameEvent.PROJECTILE_SHOOT);
		Vec3 eye = player.getEyePosition();
		// The shooter's client plays its own shot; everyone else hears it here.
		level.playSound(player, eye.x, eye.y, eye.z, JugcraftGuns.sound("guns." + gun.name() + ".fire"), SoundSource.PLAYERS,
				GunItem.volume(stack), 0.95F + player.getRandom().nextFloat() * 0.1F);
		announce(player, aiming ? GunActionPayload.AIM_SHOOT : GunActionPayload.SHOOT, 0);
		return true;
	}

	/** Starts reloading the gun in the player's main hand; whether it started. */
	public static boolean reload(ServerPlayer player) {
		ItemStack stack = player.getMainHandItem();
		if (!(stack.getItem() instanceof GunItem gun) || !player.isAlive() || player.isSpectator()
				|| RELOADS.containsKey(player.getUUID())) {
			return false;
		}
		GunSpec spec = GunItem.spec(stack);
		int room = spec.capacity() - GunItem.loaded(stack);
		if (room <= 0) {
			return false;
		}
		int rounds = player.hasInfiniteMaterials() ? room : Math.min(room, count(player.getInventory(), JugcraftGuns.ammo(spec)));
		if (rounds <= 0) {
			player.sendOverlayMessage(Component.translatable("message.jugcraft.guns.no_ammo",
					Component.translatable(JugcraftGuns.ammo(spec).getDescriptionId())));
			return false;
		}
		RELOADS.put(player.getUUID(), new Reload(stack, spec, ((ServerLevel) player.level()).getGameTime(), rounds));
		announce(player, GunActionPayload.RELOAD, rounds);
		return true;
	}

	private static void tick(MinecraftServer server) {
		Iterator<Map.Entry<UUID, Reload>> it = RELOADS.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<UUID, Reload> entry = it.next();
			ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
			Reload reload = entry.getValue();
			if (player == null || !player.isAlive() || player.getMainHandItem() != reload.stack) {
				it.remove();
				if (player != null) {
					announce(player, GunActionPayload.STOP, 0);
				}
				continue;
			}
			long elapsed = ((ServerLevel) player.level()).getGameTime() - reload.start;
			GunSpec spec = reload.spec;
			if (spec.byShell()) {
				// One shell lands at the end of each shell's time; the gun closes after the last.
				while (reload.done < reload.rounds && elapsed >= spec.shellStart() + (long) spec.shellEach() * (reload.done + 1)) {
					if (load(player, reload.stack, spec, 1) == 0) {
						reload.rounds = reload.done; // ran out (or the gun is full)
						break;
					}
					reload.done++;
				}
				if (elapsed >= spec.reloadTicks(reload.rounds)) {
					it.remove();
				}
			} else if (elapsed >= spec.reload()) {
				load(player, reload.stack, spec, reload.rounds);
				it.remove();
			}
		}
	}

	/** Moves up to this many rounds from the inventory into the gun (any it has room for, free in creative). */
	private static int load(ServerPlayer player, ItemStack stack, GunSpec spec, int wanted) {
		int room = spec.capacity() - GunItem.loaded(stack);
		int rounds = Math.min(wanted, room);
		if (rounds <= 0) {
			return 0;
		}
		if (!player.hasInfiniteMaterials()) {
			rounds = take(player.getInventory(), JugcraftGuns.ammo(spec), rounds);
		}
		GunItem.setLoaded(stack, GunItem.loaded(stack) + rounds);
		return rounds;
	}

	/** How many of this item the inventory holds. */
	public static int count(Inventory inventory, Item item) {
		int total = 0;
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack stack = inventory.getItem(i);
			if (stack.is(item)) {
				total += stack.getCount();
			}
		}
		return total;
	}

	private static int take(Inventory inventory, Item item, int wanted) {
		int taken = 0;
		for (int i = 0; i < inventory.getContainerSize() && taken < wanted; i++) {
			ItemStack stack = inventory.getItem(i);
			if (stack.is(item)) {
				int n = Math.min(wanted - taken, stack.getCount());
				stack.shrink(n);
				taken += n;
			}
		}
		if (taken > 0) {
			inventory.setChanged();
		}
		return taken;
	}

	/** Spends a shot of the player's trigger credit, if they have one (see the class comment). */
	private static boolean spend(ServerPlayer player, GunSpec spec, long now) {
		Trigger trigger = TRIGGERS.computeIfAbsent(player.getUUID(), id -> new Trigger(now));
		trigger.credit = Math.min(BURST, trigger.credit + Math.max(0, now - trigger.last) / (double) spec.interval());
		trigger.last = now;
		if (trigger.credit < 1.0) {
			return false;
		}
		trigger.credit -= 1.0;
		return true;
	}

	/** Fires every pellet of one shot and deals what hit. */
	private static void shoot(ServerLevel level, ServerPlayer player, GunSpec spec, float spread) {
		Vec3 eye = player.getEyePosition();
		Vec3 look = player.getLookAngle();
		RandomSource random = player.getRandom();
		Map<LivingEntity, Float> hits = new LinkedHashMap<>();
		for (int i = 0; i < spec.pellets(); i++) {
			Vec3 direction = stray(look, spread, random);
			Vec3 end = eye.add(direction.scale(spec.range()));
			BlockHitResult block = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
			if (block.getType() != HitResult.Type.MISS) {
				end = block.getLocation();
			}
			LivingEntity target = null;
			double nearest = Double.MAX_VALUE;
			for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, new AABB(eye, end).inflate(1.0),
					candidate -> target(player, candidate))) {
				var clip = candidate.getBoundingBox().inflate(HIT_MARGIN).clip(eye, end);
				if (clip.isPresent() && clip.get().distanceToSqr(eye) < nearest) {
					nearest = clip.get().distanceToSqr(eye);
					target = candidate;
				}
			}
			if (target != null && allowed(player, level, target)) {
				hits.merge(target, spec.damage(), Float::sum);
				Vec3 at = eye.add(direction.scale(Math.sqrt(nearest)));
				level.sendParticles(ParticleTypes.CRIT, at.x, at.y, at.z, 2, 0.05, 0.05, 0.05, 0.1);
			} else if (block.getType() != HitResult.Type.MISS) {
				Vec3 at = block.getLocation();
				level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, level.getBlockState(block.getBlockPos())), at.x, at.y,
						at.z, 4, 0.05, 0.05, 0.05, 0.1);
				level.sendParticles(ParticleTypes.SMOKE, at.x, at.y, at.z, 1, 0.02, 0.02, 0.02, 0.01);
			}
		}
		DamageSource source = new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE)
				.getOrThrow(JugcraftGuns.BULLET), player, player);
		// Guns fire faster than the half second a creature is shielded after a hit; the bullet damage type is tagged
		// minecraft:bypasses_cooldown, so each shot counts.
		hits.forEach((target, damage) -> target.hurtServer(level, source, damage));
		Vec3 muzzle = eye.add(look.scale(0.9)).add(0.0, -0.15, 0.0);
		level.sendParticles(ParticleTypes.SMOKE, muzzle.x, muzzle.y, muzzle.z, 2, 0.03, 0.03, 0.03, 0.01);
	}

	/** A direction at most {@code degrees} off {@code look}, evenly over the cone's face. */
	static Vec3 stray(Vec3 look, float degrees, RandomSource random) {
		if (degrees <= 0) {
			return look;
		}
		double angle = Math.toRadians(degrees) * Math.sqrt(random.nextDouble());
		double around = random.nextDouble() * Math.PI * 2;
		Vec3 right = look.cross(new Vec3(0, 1, 0));
		if (right.lengthSqr() < 1.0E-6) {
			right = new Vec3(1, 0, 0);
		}
		right = right.normalize();
		Vec3 up = right.cross(look).normalize();
		Vec3 off = right.scale(Mth.cos((float) around)).add(up.scale(Mth.sin((float) around)));
		return look.scale(Math.cos(angle)).add(off.scale(Math.sin(angle))).normalize();
	}

	/** Creatures a bullet may hit (as weapons/TwoHanded's foes). */
	static boolean target(ServerPlayer player, LivingEntity foe) {
		return foe != player && foe.isAlive() && !foe.isSpectator() && !player.isAlliedTo(foe) && !foe.isAlliedTo(player)
				&& !(foe instanceof ArmorStand stand && stand.isMarker()) && foe.getRootVehicle() != player.getRootVehicle();
	}

	/** Whether other code (AttackEntityCallback: the town's protection, other mods) lets this player strike this foe. */
	static boolean allowed(ServerPlayer player, ServerLevel level, LivingEntity foe) {
		return AttackEntityCallback.EVENT.invoker().interact(player, level, InteractionHand.MAIN_HAND, foe, null) == InteractionResult.PASS;
	}

	/** Tells the clients that see this player (not the player's own, which has played it already). */
	private static void announce(ServerPlayer player, int action, int rounds) {
		GunActionPayload payload = new GunActionPayload(player.getId(), action, rounds);
		for (ServerPlayer watcher : PlayerLookup.tracking(player)) {
			if (watcher != player && ServerPlayNetworking.canSend(watcher, GunActionPayload.TYPE)) {
				ServerPlayNetworking.send(watcher, payload);
			}
		}
	}

	private static void forget(ServerPlayer player) {
		TRIGGERS.remove(player.getUUID());
		RELOADS.remove(player.getUUID());
	}

	private static final class Trigger {
		double credit = BURST;
		long last;

		Trigger(long now) {
			last = now;
		}
	}

	private static final class Reload {
		final ItemStack stack;
		final GunSpec spec;
		final long start;
		int rounds;
		int done;

		Reload(ItemStack stack, GunSpec spec, long start, int rounds) {
			this.stack = stack;
			this.spec = spec;
			this.start = start;
			this.rounds = rounds;
		}
	}
}
