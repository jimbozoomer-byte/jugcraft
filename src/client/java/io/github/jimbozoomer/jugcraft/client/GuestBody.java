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
 * sampling the skin's otherwise unused island at x 56-63, y 16-31. The front and back panels turn about the legs' own
 * hip axis with the forward-most and backward-most leg every frame, so no leg passes through them, walking or seated.
 * Clothing only: never body shape, and never on a guest the owner's rule holds out of shaping;</li>
 * <li>the head accessories ({@link #EARS}, {@link #HORNS}, {@link #ELF_EARS}, {@link #MAGE_HAT}): closed boxes sampling
 * the skin's reserved 8x8 islands, seated against the hair (hat) layer, which stands {@link #HAIR} out from the head.</li>
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
	/** Head accessories (closed boxes sampling the skin's reserved 8x8 islands), by guest ({@link #addHeadParts}). */
	public static final Set<String> EARS = Set.of("tokai_teio", "agnes_tachyon", "gold_ship", "oguri_cap", "special_week", "miyabi");
	public static final Set<String> HORNS = Set.of("power", "zero_two");
	public static final Set<String> ELF_EARS = Set.of("frieren");
	public static final Set<String> MAGE_HAT = Set.of("megumin");
	/** How far the flare's panels stand out from the vertical (radians): a modest A-line. */
	public static final float FLARE_ANGLE = 0.12F;
	/**
	 * How far the front and back panels' inner faces stand from the hip axis (the legs' pivot line): in front of and
	 * behind the legs' own faces (2 pixels), so a leg turning with its panel never passes through it.
	 */
	public static final float FLARE_GAP = 2.1F;
	/**
	 * How far below the hip the front and back panels begin, along themselves: at rest ({@link #FLARE_ANGLE}) their inner
	 * top edge then lies on the hip line, where the body ends.
	 */
	public static final float FLARE_DROP = FLARE_GAP * (float) Math.tan(FLARE_ANGLE);
	/** How far the hair (hat) layer stands out from the head on every side: the model's CubeDeformation of 0.5. */
	public static final float HAIR = 0.5F;
	/**
	 * Where Frieren's ears begin, from the head's middle: on the hair layer's sides, so each one-pixel ear stands wholly
	 * proud of the hair (the head is 8 pixels wide). They used to start on the head itself, half a pixel inside the hair.
	 */
	public static final float ELF_EAR_INNER = 4.0F + HAIR;
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

	// ---------------------------------------------------------------- the head accessories

	/**
	 * Adds the head accessories to the skinnable layer's head ({@code TownsfolkModel.createSkinnableLayer}), each hidden
	 * unless {@link Head#setup} shows it: Uma and fox ears and horns rising through the top of the hair layer; Frieren's
	 * ears ({@code guest_elf_ears}: a {@code right} and a {@code left} box whose pivots lie on the hair layer's sides,
	 * {@link #ELF_EAR_INNER}); and Megumin's stepped hat, its brim enclosing the top of the hair.
	 */
	public static void addHeadParts(PartDefinition head) {
		head.addOrReplaceChild("guest_ears", CubeListBuilder.create().texOffs(0, 0)
				.addBox(-3.5F, -11.5F, -0.5F, 2, 4, 1)
				.texOffs(0, 0).addBox(1.5F, -11.5F, -0.5F, 2, 4, 1), PartPose.ZERO);
		head.addOrReplaceChild("guest_horns", CubeListBuilder.create().texOffs(0, 0)
				.addBox(-3, -10.5F, -2, 1, 3, 1)
				.texOffs(0, 0).addBox(2, -10.5F, -2, 1, 3, 1), PartPose.ZERO);
		PartDefinition elf = head.addOrReplaceChild("guest_elf_ears", CubeListBuilder.create(), PartPose.ZERO);
		elf.addOrReplaceChild("right", CubeListBuilder.create().texOffs(0, 0).addBox(-1, -1, -1, 1, 2, 2), PartPose.offset(-ELF_EAR_INNER, -4, 0));
		elf.addOrReplaceChild("left", CubeListBuilder.create().texOffs(0, 0).addBox(0, -1, -1, 1, 2, 2), PartPose.offset(ELF_EAR_INNER, -4, 0));
		// Tiled closed cubes sample the reserved 8x8 UV island without touching face/hair UVs.
		CubeListBuilder mageHat = CubeListBuilder.create();
		for (int tier = 0; tier < 5; tier++) {
			int width = tier == 0 ? 12 : 10 - tier * 2;
			float y = tier == 0 ? -9 : -9 - tier * 2;
			int height = tier == 0 ? 1 : 2;
			for (int x = -width / 2; x < width / 2; x += 2) {
				for (int z = -width / 2; z < width / 2; z += 2) {
					mageHat.texOffs(tier == 1 ? 24 : 0, 0).addBox(x, y, z, 2, height, 2);
				}
			}
		}
		head.addOrReplaceChild("guest_mage_hat", mageHat, PartPose.ZERO);
	}

	/** The head accessories in a baked model ({@link #of}), shown every frame by the skin, since one model draws everyone. */
	public static final class Head {
		private final ModelPart ears, horns, elfEars, mageHat;

		private Head(ModelPart head) {
			this.ears = head.getChild("guest_ears");
			this.horns = head.getChild("guest_horns");
			this.elfEars = head.getChild("guest_elf_ears");
			this.mageHat = head.getChild("guest_mage_hat");
		}

		/** The accessories under {@code head}, or null for a model built without them (the plain townsfolk layer). */
		public static @Nullable Head of(ModelPart head) {
			return head.hasChild("guest_ears") ? new Head(head) : null;
		}

		/** Shows exactly the accessories of the guest a town skin belongs to, in any outfit; none over a player skin. */
		public void setup(String skin, boolean playerSkin) {
			String guest = playerSkin ? "" : guest(skin);
			ears.visible = EARS.contains(guest);
			horns.visible = HORNS.contains(guest);
			elfEars.visible = ELF_EARS.contains(guest);
			mageHat.visible = MAGE_HAT.contains(guest);
		}
	}

	// ---------------------------------------------------------------- the flare

	/**
	 * Adds the flare to the skinnable layer's body ({@code TownsfolkModel.createSkinnableLayer}): front and back panels
	 * of three closed 3x4x1 boxes, turning about the hip axis (the legs' pivot line) with their inner faces
	 * {@link #FLARE_GAP} from it, and side panels of two closed 1x4x3 boxes, hinged at the hip's sides. Hidden unless
	 * {@link Flare#setup} shows it.
	 */
	public static void addParts(PartDefinition body) {
		PartDefinition flare = body.addOrReplaceChild("guest_flare", CubeListBuilder.create(), PartPose.ZERO);
		CubeListBuilder front = CubeListBuilder.create().texOffs(56, 16);
		CubeListBuilder back = CubeListBuilder.create().texOffs(56, 16);
		for (int i = 0; i < 3; i++) {
			front.addBox(-4.5F + 3.0F * i, FLARE_DROP, -FLARE_GAP - 1.0F, 3.0F, 4.0F, 1.0F);
			back.addBox(-4.5F + 3.0F * i, FLARE_DROP, FLARE_GAP, 3.0F, 4.0F, 1.0F);
		}
		flare.addOrReplaceChild("front", front, PartPose.offset(0.0F, 12.0F, 0.0F));
		flare.addOrReplaceChild("back", back, PartPose.offset(0.0F, 12.0F, 0.0F));
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
		 * relative to the body's own lean and about the legs' own hip axis, so each panel keeps the same place in front
		 * of (or behind) its leg at any angle, walking or seated, and neither leg passes through it. Call after the legs
		 * are posed.
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
