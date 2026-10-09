package io.github.jimbozoomer.jugcraft.client.guns;

import com.geckolib.cache.model.GeoLocator;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.DefaultedItemGeoModel;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.guns.GunItem;
import io.github.jimbozoomer.jugcraft.guns.JugcraftGuns;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.ItemDisplayContext;
import java.util.List;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Draws a gun with GeckoLib: the model rebuilt from the owner's parts (assets/jugcraft/geckolib/models/item/&lt;gun&gt;),
 * the owner's atlas (textures/item/guns/&lt;gun&gt;) and animations. In the player's own first-person view it also
 * draws their arms where the animations put them ({@link GunArmsLayer}) and, while they aim, slides the gun so its
 * sight sits on the middle of the screen. The gun's fitted attachments show as its own parts, in place of the standard
 * parts they replace (tools/guns.py effective_bones()).
 * <p>
 * The model's coordinates are the owner's item-model pixels less (8, 0, 8), so moving it by half a block puts it where
 * the owner's item model was, and the owner's display transforms (the base model, models/item/&lt;gun&gt;) apply as
 * they were made.
 * <p>
 * Slice 6: just after a shot, a gun in someone's hand shows its muzzle flash ({@link GunFlashLayer}).
 * <p>
 * Slice 7: aiming slides a fitted scope's eyepiece onto the middle of the screen ("sight_&lt;scope&gt;", in place of
 * "sight"); when the view through a magnifying scope fills the screen ({@link GunScope}) the gun drops out of sight.
 */
public final class GunRenderer extends GeoItemRenderer<GunItem> {
	/** The entity holding the gun (for where its sounds play). */
	public static final DataTicket<Integer> OWNER = DataTicket.create("jugcraft_gun_owner", Integer.class);
	/** The player's own first-person view: what the arms and aiming need. */
	public static final DataTicket<View> VIEW = DataTicket.create("jugcraft_gun_view", View.class);
	/** The gun's fitted attachments. */
	public static final DataTicket<Fitted> FITTED = DataTicket.create("jugcraft_gun_attachments", Fitted.class);
	/** A muzzle flash to draw: the shot was moments ago and nothing fitted hides it. */
	public static final DataTicket<Flash> FLASH = DataTicket.create("jugcraft_gun_flash", Flash.class);
	/** How far the gun drops, out of sight, while the view through its scope fills the screen (blocks). */
	private static final float PUT_AWAY = 1.5F;
	/** How far a bayonet stab drives the gun forward, and down, on screen at full thrust (blocks). */
	private static final float THRUST_REACH = 0.35F;
	private static final float THRUST_DROP = 0.06F;
	/** A slot's attachment bones, and a second set where the slot is on two bones (the Warden Pistol's spare magazine). */
	private static final List<String> SETS = List.of("", "_2");

	public GunRenderer(GunItem gun) {
		super(new DefaultedItemGeoModel<GunItem>(Jugcraft.id(gun.name())).withAltTexture(Jugcraft.id("guns/" + gun.name())));
		withRenderLayer(new GunArmsLayer(this));
		withRenderLayer(new GunFlashLayer(this));
	}

	@Override
	public void addRenderData(GunItem gun, RenderData data, GeoRenderState state, float partialTick) {
		List<String> fitted = GunItem.attachments(data.itemStack());
		state.addGeckolibData(FITTED, new Fitted(fitted));
		Entity owner = data.itemOwner() instanceof Entity entity ? entity : null;
		if (owner != null) {
			state.addGeckolibData(OWNER, owner.getId());
			float age = GunEffects.flashAge(owner.getId(), owner.level().getGameTime(), partialTick);
			if (age >= 0.0F && inHand(data.renderPerspective()) && fitted.stream().noneMatch(GunLooks.HIDE_FLASH::contains)) {
				// From the muzzle, or from the front of a barrel attachment that lengthens it.
				String locator = fitted.stream().filter(name -> JugcraftGuns.ATTACHMENTS.get(name).slot().equals("barrel")).findFirst()
						.map(name -> "muzzle_" + name).orElse("muzzle");
				state.addGeckolibData(FLASH, new Flash(age, GunEffects.lastShot(owner.getId()),
						GunLooks.FLASH_SIZES.getOrDefault(gun.spec().ammo(), 6.0F), locator));
			}
		}
		Minecraft client = Minecraft.getInstance();
		if (data.renderPerspective().firstPerson() && owner instanceof AbstractClientPlayer player && player == client.player) {
			boolean slim = player.getSkin().model() == PlayerModelType.SLIM;
			String optic = GunItem.inSlot(data.itemStack(), "optic");
			state.addGeckolibData(VIEW, new View(player.getSkin().body().texturePath(), slim, GunView.aim(partialTick),
					GunEffects.thrust(player.getId(), player.level().getGameTime(), partialTick), optic == null ? "sight" : "sight_" + optic,
					GunScope.viewing(partialTick)));
		}
	}

	/** Whether the gun is drawn in someone's hand (not in a slot, on the ground or in a frame). */
	private static boolean inHand(ItemDisplayContext context) {
		return context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
				|| context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
	}

	/** The props the animations move on bones of their own (tools/guns.py PROPS). */
	private static final List<String> PROPS = List.of("shell", "ball", "ram", "flash");

	/**
	 * A prop (a shell, a ball, a ramrod, a priming flash) rests out of place: an animation's offsets bring it where it
	 * belongs, and an animation that leaves it alone (an inspect, the idle) would show it at its rest. So it shows only
	 * while an animation moves it.
	 * <p>
	 * Each attachment's bone ("att_&lt;id&gt;") shows only while it is fitted, and a slot's standard part
	 * ("std_&lt;slot&gt;") only while nothing fitted replaces it.
	 */
	@Override
	public void adjustModelBonesForRender(RenderPassInfo<GeoRenderState> info, BoneSnapshots snapshots) {
		for (String prop : PROPS) {
			snapshots.ifPresent(prop, bone -> bone.skipRender(!bone.hasTranslation()));
		}
		Fitted fitted = info.getGeckolibData(FITTED);
		List<String> on = fitted == null ? List.of() : fitted.attachments();
		for (String set : SETS) {
			for (String name : JugcraftGuns.ATTACHMENTS.keySet()) {
				snapshots.ifPresent("att_" + name + set, bone -> bone.skipRender(!on.contains(name)));
			}
			for (String slot : JugcraftGuns.SLOTS) {
				boolean replaced = on.stream().map(JugcraftGuns.ATTACHMENTS::get)
						.anyMatch(attachment -> attachment.slot().equals(slot) && attachment.replaces());
				snapshots.ifPresent("std_" + slot + set, bone -> bone.skipRender(replaced));
			}
		}
	}

	@Override
	public void adjustRenderPose(RenderPassInfo<GeoRenderState> info) {
		PoseStack poseStack = info.poseStack();
		View view = info.getGeckolibData(VIEW);
		if (view != null && view.aim() > 0) {
			GeoLocator sight = info.model().getLocator(view.sight()).or(() -> info.model().getLocator("sight")).orElse(null);
			if (sight != null) {
				// Where the sight is on screen now, then the move that puts it on the crosshair (in view space).
				Matrix4f pose = new Matrix4f(poseStack.last().pose()).translate(0.5F, 0.0F, 0.5F);
				Vector3f at = pose.transformPosition(new Vector3f(sight.offsetX() / 16.0F, sight.offsetY() / 16.0F, sight.offsetZ() / 16.0F));
				poseStack.last().pose().translateLocal(-at.x() * view.aim(), -at.y() * view.aim(), 0.0F);
			}
		}
		if (view != null && view.thrust() > 0) {
			// A bayonet stab drives the gun forward and a little down, and back (in view space).
			poseStack.last().pose().translateLocal(0.0F, -THRUST_DROP * view.thrust(), -THRUST_REACH * view.thrust());
		}
		if (view != null && view.putAway()) {
			// The view through the scope fills the screen; the gun would only stand in it.
			poseStack.last().pose().translateLocal(0.0F, -PUT_AWAY, 0.0F);
		}
		poseStack.translate(0.5F, 0.0F, 0.5F);
	}

	/**
	 * @param skin    the player's skin texture
	 * @param slim    whether their arms are three pixels wide
	 * @param aim     how far into aiming down the sights (0 to 1)
	 * @param thrust  how far into a bayonet stab's thrust (0 to 1; {@link GunEffects#thrust})
	 * @param sight   the locator aiming puts on the middle of the screen: "sight", or a fitted scope's "sight_&lt;scope&gt;"
	 * @param putAway whether the view through a scope fills the screen, so the gun is out of sight ({@link GunScope})
	 */
	public record View(net.minecraft.resources.Identifier skin, boolean slim, float aim, float thrust, String sight,
			boolean putAway) {
	}

	/** @param attachments the attachments fitted to the gun drawn ({@link GunItem#attachments}) */
	public record Fitted(List<String> attachments) {
	}

	/**
	 * @param age     ticks since the shot
	 * @param shot    the shot's game time (picks the frame and its turn)
	 * @param size    across, in the model's pixels ({@link GunLooks#FLASH_SIZES})
	 * @param locator where it comes out: "muzzle", or "muzzle_&lt;attachment&gt;" for a barrel attachment
	 */
	public record Flash(float age, long shot, float size, String locator) {
	}
}
