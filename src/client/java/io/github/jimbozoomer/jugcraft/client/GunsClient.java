package io.github.jimbozoomer.jugcraft.client;

import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.renderer.GeoItemRenderer;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.client.guns.GunAnimations;
import io.github.jimbozoomer.jugcraft.client.guns.GunCasingParticle;
import io.github.jimbozoomer.jugcraft.client.guns.GunEffects;
import io.github.jimbozoomer.jugcraft.client.guns.GunRenderer;
import io.github.jimbozoomer.jugcraft.client.guns.GunScope;
import io.github.jimbozoomer.jugcraft.client.guns.GunView;
import io.github.jimbozoomer.jugcraft.guns.GunActionPayload;
import io.github.jimbozoomer.jugcraft.guns.GunHooks;
import io.github.jimbozoomer.jugcraft.guns.GunItem;
import io.github.jimbozoomer.jugcraft.guns.GunReloadPayload;
import io.github.jimbozoomer.jugcraft.guns.GunShotPayload;
import io.github.jimbozoomer.jugcraft.guns.GunSpinPayload;
import io.github.jimbozoomer.jugcraft.guns.GunStabPayload;
import io.github.jimbozoomer.jugcraft.guns.GunTracePayload;
import io.github.jimbozoomer.jugcraft.guns.GunShots;
import io.github.jimbozoomer.jugcraft.guns.GunSpec;
import io.github.jimbozoomer.jugcraft.guns.JugcraftGuns;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import java.util.HashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * The client's half of the guns (the server's is guns/GunShots): input, what the player sees and hears at once, and the
 * ammunition counter.
 * <ul>
 * <li>With a gun in the main hand the attack button is the trigger (it never mines or punches): a press fires a
 * single-shot gun, holding fires an automatic at its rate. Right click (held) aims. G reloads and H inspects (not R
 * and I, which Iris, in the pack, keeps for its shaders).</li>
 * <li>Each shot asks the server and at once plays the shot, its sound and a little kick of the view here; the server
 * may still refuse (it decides). The counter allows for shots the server has not answered yet.</li>
 * <li>Switching to a gun plays its draw, and it cannot fire until the draw is done.</li>
 * <li>Other players' guns are animated from the server's {@link GunActionPayload}.</li>
 * <li>Every shot, the player's own and others', shows its muzzle flash and black powder's smoke ({@link GunEffects}).</li>
 * <li>V stabs with a fitted bayonet (slice 7): the thrust and its swish at once, the blow from the server.</li>
 * <li>Aiming through a fitted scope shows the view through it, or a reflex sight's dot ({@link GunScope}).</li>
 * <li>Slice 8C: holding the trigger of a rotary gun spins its barrels up, telling the server each tick
 * ({@link GunSpinPayload}); it fires once they have spun for its spin-up, and they run down when it is let go.</li>
 * <li>Slice 8D: an energy weapon's shots are drawn where the server says they went ({@link GunTracePayload}); its
 * counter gives the shots its Energy Cells' charge holds.</li>
 * </ul>
 */
public final class GunsClient {
	/** Ticks a drawn gun takes before it fires. */
	static final int DRAW_TICKS = 10;
	/** Ticks without a shot after which unanswered shots are written off. */
	private static final int IN_FLIGHT_TICKS = 10;
	private static KeyMapping reloadKey;
	private static KeyMapping inspectKey;
	private static KeyMapping stabKey;
	private static long nextStab;
	private static long nextShot;
	private static long readyAt;
	private static long reloadingUntil;
	private static long lastShot;
	private static int inFlight;
	private static int lastSynced = -1;
	private static long heldId = Long.MIN_VALUE;
	private static boolean triggerHeld;
	/** When the trigger of the rotary gun in hand was pulled, and the tick it was last held (slice 8C); -1 when let go. */
	private static long spinSince = -1;
	private static long spunAt = -1;
	/** Each gun's GeoRenderProvider, made once. */
	private static final Map<GunItem, GeoRenderProvider> PROVIDERS = new HashMap<>();

	private GunsClient() {
	}

	public static void register() {
		GunHooks.controllers = GunAnimations::register;
		GunHooks.renderer = gun -> PROVIDERS.computeIfAbsent(gun, key -> new GeoRenderProvider() {
			private @Nullable GunRenderer renderer;

			@Override
			public GeoItemRenderer<?> getGeoItemRenderer() {
				if (renderer == null) {
					renderer = new GunRenderer(key);
				}
				return renderer;
			}
		});
		reloadKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.jugcraft.reload", InputConstants.KEY_G, PartyClient.CATEGORY));
		inspectKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.jugcraft.inspect", InputConstants.KEY_H, PartyClient.CATEGORY));
		stabKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.jugcraft.stab", InputConstants.KEY_V, PartyClient.CATEGORY));
		ClientPreAttackCallback.EVENT.register(GunsClient::attack);
		ClientTickEvents.END_CLIENT_TICK.register(GunsClient::tick);
		ClientPlayNetworking.registerGlobalReceiver(GunActionPayload.TYPE, (payload, context) -> receive(payload));
		ClientPlayNetworking.registerGlobalReceiver(GunTracePayload.TYPE, (payload, context) -> GunEffects.trace(payload));
		HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR, Jugcraft.id("gun_ammo"), GunsClient::hud);
		GunScope.register();
		JugcraftGuns.CASINGS.values().forEach(casing -> ParticleProviderRegistry.getInstance().register(casing, GunCasingParticle::provider));
	}

	/** The reload key (for the client game tests). */
	public static KeyMapping reloadKey() {
		return reloadKey;
	}

	/** The inspect key (for the client game tests). */
	public static KeyMapping inspectKey() {
		return inspectKey;
	}

	/** The bayonet stab key (for the client game tests). */
	public static KeyMapping stabKey() {
		return stabKey;
	}

	/** The attack button, with a gun in the main hand, is the trigger. */
	private static boolean attack(Minecraft client, LocalPlayer player, int clicks) {
		ItemStack stack = player.getMainHandItem();
		if (!(stack.getItem() instanceof GunItem gun) || player.isSpectator()) {
			return false;
		}
		boolean fresh = clicks > 0 && !triggerHeld;
		triggerHeld = true;
		int spinUp = JugcraftGuns.spinUp(gun);
		if (spinUp > 0 && client.level != null && !spin(client.level.getGameTime(), player, spinUp)
				&& !(fresh && predicted(stack) <= 0)) {
			return true; // the barrels are still spinning up (an empty gun still clicks at once)
		}
		if (fresh || gun.spec().auto()) {
			pull(client, player, stack, gun, fresh);
		}
		return true;
	}

	/**
	 * The rotary gun's trigger is held this tick: its barrels turn (here at once, and the server is told once a tick);
	 * whether they have spun for its spin-up.
	 */
	private static boolean spin(long now, LocalPlayer player, int spinUp) {
		if (spunAt == now) {
			return now - spinSince >= spinUp;
		}
		if (spinSince < 0 || now - spunAt > 1 || now < spunAt) {
			spinSince = now;
		}
		spunAt = now;
		ClientPlayNetworking.send(GunSpinPayload.INSTANCE);
		GunEffects.spinning(player);
		return now - spinSince >= spinUp;
	}

	private static void pull(Minecraft client, LocalPlayer player, ItemStack stack, GunItem gun, boolean fresh) {
		if (client.level == null) {
			return;
		}
		GunSpec spec = GunItem.spec(stack);
		long now = client.level.getGameTime();
		if (now < nextShot || now < readyAt || now < reloadingUntil && !spec.byShell()) {
			return;
		}
		if (predicted(stack) <= 0 && !player.hasInfiniteMaterials()) {
			if (fresh) {
				local(player, "dry_fire");
				nextShot = now + spec.interval();
				// An empty gun reloads itself on a fresh pull, when there is something to load.
				reload(client, player);
			}
			return;
		}
		ClientPlayNetworking.send(GunShotPayload.INSTANCE);
		nextShot = now + spec.interval();
		lastShot = now;
		if (!player.hasInfiniteMaterials()) {
			inFlight++; // creative spends nothing, so the server never settles it
		}
		reloadingUntil = 0;
		boolean aiming = GunItem.aiming(player, stack);
		GunAnimations.trigger(player, aiming ? "aim_shoot" : "shoot");
		GunEffects.shot(player);
		player.level().playLocalSound(player.getX(), player.getEyeY(), player.getZ(), JugcraftGuns.sound("guns." + gun.name() + ".fire"),
				SoundSource.PLAYERS, GunItem.volume(stack), 0.95F + player.getRandom().nextFloat() * 0.1F, false);
		// A little kick of the view: more for a heavier shot, less when aimed or with a brake, stock or grip.
		float kick = (0.5F + 0.125F * spec.damage() * spec.pellets()) * (aiming ? 0.6F : 1.0F) * GunItem.kick(stack);
		player.setXRot(player.getXRot() - kick);
	}

	private static void tick(Minecraft client) {
		LocalPlayer player = client.player;
		GunView.tick(player);
		if (player == null || client.level == null) {
			return;
		}
		long now = client.level.getGameTime();
		if (!client.options.keyAttack.isDown()) {
			triggerHeld = false;
			spinSince = -1;
		}
		ItemStack stack = player.getMainHandItem();
		long id = stack.getItem() instanceof GunItem ? GeoItem.getId(stack) : Long.MIN_VALUE;
		if (id != heldId) {
			heldId = id;
			spinSince = -1;
			inFlight = 0;
			lastSynced = -1;
			reloadingUntil = 0;
			if (stack.getItem() instanceof GunItem) {
				GunAnimations.trigger(player, "draw");
				readyAt = now + DRAW_TICKS;
			}
		}
		if (!(stack.getItem() instanceof GunItem)) {
			while (reloadKey.consumeClick()) {
				// nothing to reload
			}
			while (inspectKey.consumeClick()) {
				// nothing to inspect
			}
			while (stabKey.consumeClick()) {
				// nothing to stab with
			}
			return;
		}
		if (now - lastShot > IN_FLIGHT_TICKS) {
			inFlight = 0;
		}
		while (reloadKey.consumeClick()) {
			reload(client, player);
		}
		while (inspectKey.consumeClick()) {
			if (now >= reloadingUntil && now >= readyAt) {
				GunAnimations.trigger(player, "inspect");
			}
		}
		while (stabKey.consumeClick()) {
			stab(client, player, now);
		}
	}

	/** Stabs with the gun's bayonet, if it has one: asks the server, and plays the thrust and its swish here at once. */
	private static void stab(Minecraft client, LocalPlayer player, long now) {
		if (GunItem.bayonet(player.getMainHandItem()) == null || now < nextStab || now < readyAt || now < reloadingUntil) {
			return;
		}
		ClientPlayNetworking.send(GunStabPayload.INSTANCE);
		nextStab = now + GunShots.STAB_TICKS;
		GunEffects.stabbed(player);
		player.level().playLocalSound(player.getX(), player.getEyeY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS,
				0.8F, 1.3F, false);
	}

	/** Asks for a reload, if there is room and something to load, and plays it here. */
	private static void reload(Minecraft client, LocalPlayer player) {
		ItemStack stack = player.getMainHandItem();
		if (!(stack.getItem() instanceof GunItem gun) || client.level == null) {
			return;
		}
		long now = client.level.getGameTime();
		GunSpec spec = GunItem.spec(stack);
		int room = spec.capacity() - predicted(stack);
		int rounds = player.hasInfiniteMaterials() ? room : Math.min(room, GunShots.stocked(player.getInventory(), gun, spec));
		if (now < reloadingUntil || room <= 0) {
			return;
		}
		ClientPlayNetworking.send(GunReloadPayload.INSTANCE);
		if (rounds > 0) {
			GunAnimations.trigger(player, GunAnimations.reloadName(spec, rounds));
			reloadingUntil = now + spec.reloadTicks(rounds);
		}
	}

	/** Rounds loaded, less shots sent that the server has not answered yet. */
	private static int predicted(ItemStack stack) {
		int synced = GunItem.loaded(stack);
		if (lastSynced >= 0 && synced < lastSynced) {
			inFlight = Math.max(0, inFlight - (lastSynced - synced));
		} else if (synced > lastSynced) {
			inFlight = 0;
		}
		lastSynced = synced;
		return synced - inFlight;
	}

	private static void local(LocalPlayer player, String event) {
		player.level().playLocalSound(player.getX(), player.getEyeY(), player.getZ(), JugcraftGuns.sound("guns." + event),
				SoundSource.PLAYERS, 0.8F, 1.0F, false);
	}

	/** Another player fired or began to reload: play it on their gun. */
	private static void receive(GunActionPayload payload) {
		Minecraft client = Minecraft.getInstance();
		Entity entity = client.level == null ? null : client.level.getEntity(payload.entity());
		if (!(entity instanceof LivingEntity holder) || !(holder.getMainHandItem().getItem() instanceof GunItem gun)) {
			return;
		}
		switch (payload.action()) {
			case GunActionPayload.SHOOT -> {
				GunAnimations.trigger(holder, "shoot");
				GunEffects.shot(holder);
			}
			case GunActionPayload.AIM_SHOOT -> {
				GunAnimations.trigger(holder, "aim_shoot");
				GunEffects.shot(holder);
			}
			case GunActionPayload.RELOAD -> GunAnimations.trigger(holder, GunAnimations.reloadName(gun.spec(), payload.rounds()));
			case GunActionPayload.STAB -> GunEffects.stabbed(holder);
			case GunActionPayload.SPIN -> GunEffects.spun(holder, JugcraftGuns.spinUp(gun));
			default -> GunAnimations.trigger(holder, "idle");
		}
	}

	/**
	 * The counter above the hotbar's right end: loaded / capacity, and the rounds to hand (or "Reloading"); for an energy
	 * weapon, the shots its Energy Cells' charge holds.
	 */
	private static void hud(GuiGraphicsExtractor graphics, DeltaTracker delta) {
		Minecraft client = Minecraft.getInstance();
		LocalPlayer player = client.player;
		if (player == null || client.gui.hud.isHidden() || client.level == null
				|| !(player.getMainHandItem().getItem() instanceof GunItem gun)) {
			return;
		}
		ItemStack stack = player.getMainHandItem();
		GunSpec spec = GunItem.spec(stack);
		// The rounds loaded less the shots the server has not answered yet (each answer settles one).
		int loaded = Math.max(0, predicted(stack));
		Component count = Component.translatable("hud.jugcraft.guns.ammo", loaded, spec.capacity());
		Component below = client.level.getGameTime() < reloadingUntil ? Component.translatable("hud.jugcraft.guns.reloading")
				: JugcraftGuns.charge(gun) > 0 ? Component.translatable("hud.jugcraft.guns.in_cells", GunShots.stocked(player.getInventory(), gun, spec))
				: Component.literal(GunShots.count(player.getInventory(), JugcraftGuns.ammo(spec)) + " ")
						.append(Component.translatable(JugcraftGuns.ammo(spec).getDescriptionId()));
		int right = graphics.guiWidth() / 2 + 91 + 8;
		int bottom = graphics.guiHeight() - 22;
		graphics.text(client.font, count, right, bottom, loaded == 0 ? 0xFFFF5555 : 0xFFFFFFFF, true);
		graphics.text(client.font, below, right, bottom + 10, 0xFFC8C8C8, true);
	}
}
