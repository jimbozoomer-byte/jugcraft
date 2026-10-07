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
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.PlayerModelType;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Draws a gun with GeckoLib: the model rebuilt from the owner's parts (assets/jugcraft/geckolib/models/item/&lt;gun&gt;),
 * the owner's atlas (textures/item/guns/&lt;gun&gt;) and animations. In the player's own first-person view it also
 * draws their arms where the animations put them ({@link GunArmsLayer}) and, while they aim, slides the gun so its
 * sight sits on the middle of the screen.
 * <p>
 * The model's coordinates are the owner's item-model pixels less (8, 0, 8), so moving it by half a block puts it where
 * the owner's item model was, and the owner's display transforms (the base model, models/item/&lt;gun&gt;) apply as
 * they were made.
 */
public final class GunRenderer extends GeoItemRenderer<GunItem> {
	/** The entity holding the gun (for where its sounds play). */
	public static final DataTicket<Integer> OWNER = DataTicket.create("jugcraft_gun_owner", Integer.class);
	/** The player's own first-person view: what the arms and aiming need. */
	public static final DataTicket<View> VIEW = DataTicket.create("jugcraft_gun_view", View.class);

	public GunRenderer(GunItem gun) {
		super(new DefaultedItemGeoModel<GunItem>(Jugcraft.id(gun.name())).withAltTexture(Jugcraft.id("guns/" + gun.name())));
		withRenderLayer(new GunArmsLayer(this));
	}

	@Override
	public void addRenderData(GunItem gun, RenderData data, GeoRenderState state, float partialTick) {
		Entity owner = data.itemOwner() instanceof Entity entity ? entity : null;
		if (owner != null) {
			state.addGeckolibData(OWNER, owner.getId());
		}
		Minecraft client = Minecraft.getInstance();
		if (data.renderPerspective().firstPerson() && owner instanceof AbstractClientPlayer player && player == client.player) {
			boolean slim = player.getSkin().model() == PlayerModelType.SLIM;
			state.addGeckolibData(VIEW, new View(player.getSkin().body().texturePath(), slim, GunView.aim(partialTick)));
		}
	}

	/**
	 * The Thunderpipe's shell rests out of place: the reload loop's offsets bring it into the breech, and every other
	 * animation hides it, except the inspect, which leaves it alone. So it shows only while an animation moves it.
	 */
	@Override
	public void adjustModelBonesForRender(RenderPassInfo<GeoRenderState> info, BoneSnapshots snapshots) {
		snapshots.ifPresent("shell", shell -> shell.skipRender(!shell.hasTranslation()));
	}

	@Override
	public void adjustRenderPose(RenderPassInfo<GeoRenderState> info) {
		PoseStack poseStack = info.poseStack();
		View view = info.getGeckolibData(VIEW);
		if (view != null && view.aim() > 0) {
			GeoLocator sight = info.model().getLocator("sight").orElse(null);
			if (sight != null) {
				// Where the sight is on screen now, then the move that puts it on the crosshair (in view space).
				Matrix4f pose = new Matrix4f(poseStack.last().pose()).translate(0.5F, 0.0F, 0.5F);
				Vector3f at = pose.transformPosition(new Vector3f(sight.offsetX() / 16.0F, sight.offsetY() / 16.0F, sight.offsetZ() / 16.0F));
				poseStack.last().pose().translateLocal(-at.x() * view.aim(), -at.y() * view.aim(), 0.0F);
			}
		}
		poseStack.translate(0.5F, 0.0F, 0.5F);
	}

	/**
	 * @param skin the player's skin texture
	 * @param slim whether their arms are three pixels wide
	 * @param aim  how far into aiming down the sights (0 to 1)
	 */
	public record View(net.minecraft.resources.Identifier skin, boolean slim, float aim) {
	}
}
