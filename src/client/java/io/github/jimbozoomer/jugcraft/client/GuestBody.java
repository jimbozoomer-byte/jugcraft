package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.town.guests.GuestWardrobe;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.PartDefinition;
import org.jspecify.annotations.Nullable;

/**
 * The named guests' body on the townsfolk model (worker A; the art and its rules are docs/audits/jze-guest-appearances.md
 * and tools/guest_skins.py). Client only. {@link TownsfolkModel} and {@link TownsfolkRenderer} call it:
 * <ul>
 * <li>which packaged guest a skin belongs to, in any outfit ({@code guest_<id>} or {@code guest_<id>_<outfit>},
 * {@link GuestWardrobe#OUTFITS}), so head accessories and the rest follow the guest through a seasonal outfit;</li>
 * <li>slim (3-pixel) arms, the model's slim arms, for the clearly adult fictional guests whose skins are drawn that way
 * ({@link #SLIM}); everyone else keeps classic arms;</li>
 * <li>a modest A-line flare for a long gown or robe ({@link #FLARE}): four closed panels hung at the hip from the body,
 * sampling the skin's otherwise unused island at x 56-63, y 16-31. The front and back panels swing with the
 * forward-most and backward-most leg every frame, so no walking leg passes through them. Clothing only: never body
 * shape, and never on a guest the owner's rule holds out of shaping.</li>
 * </ul>
 * The owner's body rule (binding, 11 November 2026): characters shown as minors or school students, or whose age is
 * uncertain, keep straight proportions with no shaping in skins or geometry; real people get clothing and hair only;
 * modest shaping only for clearly adult fictional characters. Nothing here changes any model part's size, and a player
 * skin on a guest NPC is always drawn as that player's own skin, without any of these parts.
 */
public final class GuestBody {
	/** Clearly adult fictional guests drawn with slim arms (tools/guest_skins.py SLIM). None of the held guests, no real person. */
	public static final Set<String> SLIM = Set.of("makima", "himeno", "misato_katsuragi", "tifa_lockhart", "yorha_2b", "chun_li",
			"ada_wong", "lara_croft", "kafka", "miyabi", "nicole_demara", "frieren");
	/** Guests whose gown or robe flares at the hip (tools/guest_skins.py FLARE): clothing, never a held guest. */
	public static final Set<String> FLARE = Set.of("sarah_stock", "frieren");
	/** Head accessories (closed boxes sampling the skin's reserved 8x8 islands), by guest, as TownsfolkModel had them. */
	public static final Set<String> EARS = Set.of("tokai_teio", "agnes_tachyon", "gold_ship", "oguri_cap", "special_week", "miyabi");
	public static final Set<String> HORNS = Set.of("power", "zero_two");
	public static final Set<String> ELF_EARS = Set.of("frieren");
	public static final Set<String> MAGE_HAT = Set.of("megumin");
	/** How far the flare's panels stand out from the vertical (radians): a modest A-line. */
	public static final float FLARE_ANGLE = 0.12F;
	private static final String PREFIX = "guest_";
	private static final String[] OUTFIT_SUFFIXES = suffixes();

	private GuestBody() {
	}

	private static String[] suffixes() {
		List<String> out = new ArrayList<>();
		for (String outfit : GuestWardrobe.OUTFITS) {
			if (!outfit.equals(GuestWardrobe.NORMAL)) {
				out.add("_" + outfit);
			}
		}
		return out.toArray(new String[0]);
	}

	/** The guest id a packaged guest skin belongs to, in any outfit; "" for any other skin. */
	public static String guest(String skin) {
		if (!skin.startsWith(PREFIX)) {
			return "";
		}
		String rest = skin.substring(PREFIX.length());
		for (String suffix : OUTFIT_SUFFIXES) {
			if (rest.endsWith(suffix)) {
				return rest.substring(0, rest.length() - suffix.length());
			}
		}
		return rest;
	}

	/** Whether this town skin is drawn with slim arms (TownsfolkRenderer, for a town skin only; a player skin says its own). */
	public static boolean slim(String skin) {
		return SLIM.contains(guest(skin));
	}

	// ---------------------------------------------------------------- the flare

	/**
	 * Adds the flare to the skinnable layer's body ({@code TownsfolkModel.createSkinnableLayer}): front and back panels
	 * of three closed 3x4x1 boxes, and side panels of two closed 1x4x3 boxes, hinged at the hip. Hidden unless
	 * {@link Flare#setup} shows it.
	 */
	public static void addParts(PartDefinition body) {
		PartDefinition flare = body.addOrReplaceChild("guest_flare", CubeListBuilder.create(), PartPose.ZERO);
		CubeListBuilder front = CubeListBuilder.create().texOffs(56, 16);
		CubeListBuilder back = CubeListBuilder.create().texOffs(56, 16);
		for (int i = 0; i < 3; i++) {
			front.addBox(-4.5F + 3.0F * i, 0.0F, -1.0F, 3.0F, 4.0F, 1.0F);
			back.addBox(-4.5F + 3.0F * i, 0.0F, 0.0F, 3.0F, 4.0F, 1.0F);
		}
		flare.addOrReplaceChild("front", front, PartPose.offset(0.0F, 12.0F, -2.1F));
		flare.addOrReplaceChild("back", back, PartPose.offset(0.0F, 12.0F, 2.1F));
		CubeListBuilder right = CubeListBuilder.create().texOffs(56, 24);
		CubeListBuilder left = CubeListBuilder.create().texOffs(56, 24);
		for (int i = 0; i < 2; i++) {
			right.addBox(-1.0F, 0.0F, -3.0F + 3.0F * i, 1.0F, 4.0F, 3.0F);
			left.addBox(0.0F, 0.0F, -3.0F + 3.0F * i, 1.0F, 4.0F, 3.0F);
		}
		flare.addOrReplaceChild("right", right, PartPose.offsetAndRotation(-4.1F, 12.0F, 0.0F, 0.0F, 0.0F, FLARE_ANGLE));
		flare.addOrReplaceChild("left", left, PartPose.offsetAndRotation(4.1F, 12.0F, 0.0F, 0.0F, 0.0F, -FLARE_ANGLE));
	}

	/** The flare in a baked model ({@link #of}), posed every frame, since one model draws everyone. */
	public static final class Flare {
		private final ModelPart root, front, back;

		private Flare(ModelPart root) {
			this.root = root;
			this.front = root.getChild("front");
			this.back = root.getChild("back");
		}

		/** The flare under {@code body}, or null for a model built without it (the plain townsfolk layer). */
		public static @Nullable Flare of(ModelPart body) {
			return body.hasChild("guest_flare") ? new Flare(body.getChild("guest_flare")) : null;
		}

		/**
		 * Shows the flare for a flared guest's town skin (never over a player skin) and swings its front and back
		 * panels with the legs: the front with the leg furthest forward, the back with the leg furthest back, each
		 * relative to the body's own lean, so neither leg passes through it. Call after the legs are posed.
		 */
		public void setup(String skin, boolean playerSkin, ModelPart body, ModelPart rightLeg, ModelPart leftLeg) {
			root.visible = !playerSkin && FLARE.contains(guest(skin));
			if (!root.visible) {
				return;
			}
			float forward = Math.min(0.0F, Math.min(rightLeg.xRot, leftLeg.xRot));
			float backward = Math.max(0.0F, Math.max(rightLeg.xRot, leftLeg.xRot));
			front.xRot = forward - body.xRot - FLARE_ANGLE;
			back.xRot = backward - body.xRot + FLARE_ANGLE;
		}
	}
}
