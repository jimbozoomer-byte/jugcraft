package io.github.jimbozoomer.jugcraft.test;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.client.GuestBody;
import io.github.jimbozoomer.jugcraft.client.TownsfolkModel;
import io.github.jimbozoomer.jugcraft.client.TownsfolkRenderer;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import io.github.jimbozoomer.jugcraft.town.guests.GuestLedger;
import io.github.jimbozoomer.jugcraft.town.guests.GuestRoster;
import io.github.jimbozoomer.jugcraft.town.guests.GuestWardrobe;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * JZE guest expansion, worker A's rendered checks (docs/audits/jze-guest-appearances.md): a representative set of named
 * guests, placed by an operator's {@code /guests place} in a creative test world, seen as a player sees them, in their
 * normal outfit, then in each seasonal outfit chosen by {@code /guests outfit} (winter, harvest, spring), then back in
 * normal:
 * <ul>
 * <li>an adult fictional guest (Tifa Lockhart: slim arms, modest tailoring);</li>
 * <li>a younger, held guest (Tokai Teio: classic arms, straight proportions, ears);</li>
 * <li>a real person (Lauren Chen: classic arms, clothing and hair only);</li>
 * <li>Sarah Stock's knit top and jeans (classic arms, no flare);</li>
 * <li>Frieren (an adult fictional guest: slim arms, the robe's flare and her ears).</li>
 * </ul>
 * Screenshots: {@code guest_wardrobe_<outfit>} and {@code guest_wardrobe_<outfit>_back} for winter, harvest and spring,
 * {@code guest_wardrobe_normal}, {@code guest_wardrobe_normal_again}, and a close-up of each guest in each outfit,
 * {@code guest_wardrobe_<guest>_<outfit>}. It also checks that every outfit of the set is packaged, that GuestBody
 * follows each guest into its outfits (by name, and in each outfit on the model the renderer actually poses: slim arms,
 * the flare and the head accessories), and the owner's body rule over the whole roster: no held guest has slim arms or
 * a flare, no real person has slim arms.
 *
 * <p>Pose alignment (the model's own geometry against the body): the slim arms' pivots, where a held item is placed,
 * are vanilla's slim player's (half a pixel in and down) while the arms stand exactly where vanilla's do; Frieren's ears
 * begin on the hair layer's sides; and the flare's front and back panels keep every walking and seated leg out of them.
 * Close-ups {@code guest_pose_held_slim} and {@code guest_pose_held_classic} show a book in a slim and a classic hand.
 *
 * <p>Needs the coordinator's TownsfolkModel/TownsfolkRenderer/CustomNpcs patch (worker A's report) for the seasonal
 * skins to be accepted and for slim arms and the flare to be drawn.
 */
public class GuestWardrobeClientGameTests implements FabricClientGameTest {
	/** The set, left to right as the player sees it. */
	private static final List<Sample> SET = List.of(new Sample("tifa_lockhart", true, false), new Sample("tokai_teio", false, false),
			new Sample("lauren_chen", false, false), new Sample("sarah_stock", false, false), new Sample("frieren", true, true));
	/** The seasonal outfits, in the order they are worn. */
	private static final List<String> SEASONS = List.of("winter", "harvest", "spring");

	private record Sample(String guest, boolean slim, boolean flare) {
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		rule(context);
		geometry(context);
		try (TestSingleplayerContext world = context.worldBuilder()
				.adjustSettings(s -> s.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()) {
			world.getConnection().waitForChunksRender();
			TestServerContext server = world.getServer();
			server.runCommand("time set noon");
			server.runCommand("weather clear");
			BlockPos origin = context.computeOnClient(mc -> mc.player.blockPosition());
			// A clear, floored patch in front of the player.
			server.runOnServer(s -> {
				var level = s.overworld();
				for (int x = -6; x <= 6; x++) {
					for (int z = 1; z <= 8; z++) {
						level.setBlockAndUpdate(origin.offset(x, -1, z), Blocks.STONE.defaultBlockState());
						for (int y = 0; y < 4; y++) {
							level.setBlockAndUpdate(origin.offset(x, y, z), Blocks.AIR.defaultBlockState());
						}
					}
				}
			});
			int[] ids = new int[SET.size()];
			BlockPos[] at = new BlockPos[SET.size()];
			for (int i = 0; i < SET.size(); i++) {
				at[i] = origin.offset(-4 + 2 * i, 0, 4);
				ids[i] = place(server, SET.get(i).guest(), at[i]);
			}
			face(server, ids, 180.0F);
			context.waitFor(mc -> wearing(mc, ids, GuestWardrobe.NORMAL), 100);
			follows(context, ids, GuestWardrobe.NORMAL);
			context.runOnClient(mc -> {
				mc.gui.hud.getChat().clearMessages(true);
				if (!mc.gui.hud.isHidden()) {
					mc.gui.hud.toggle();
				}
			});
			try {
				shoot(context, server, origin, at, GuestWardrobe.NORMAL);
				for (String outfit : SEASONS) {
					for (Sample sample : SET) {
						server.runCommand("guests outfit " + sample.guest() + " " + outfit);
					}
					context.waitFor(mc -> wearing(mc, ids, outfit), 200);
					follows(context, ids, outfit);
					shoot(context, server, origin, at, outfit);
					face(server, ids, 0.0F);
					context.getInput().lookAt(origin.offset(0, 1, 4));
					context.waitTicks(10);
					context.takeScreenshot("guest_wardrobe_" + outfit + "_back");
					face(server, ids, 180.0F);
				}
				server.runCommand("guests outfit all " + GuestWardrobe.NORMAL);
				context.waitFor(mc -> wearing(mc, ids, GuestWardrobe.NORMAL), 200);
				follows(context, ids, GuestWardrobe.NORMAL);
				context.runOnClient(mc -> mc.gui.hud.getChat().clearMessages(true));
				context.getInput().lookAt(origin.offset(0, 1, 4));
				context.waitTicks(10);
				context.takeScreenshot("guest_wardrobe_normal_again");
				hands(context, server, at, ids);
			} finally {
				context.runOnClient(mc -> {
					if (mc.gui.hud.isHidden()) {
						mc.gui.hud.toggle();
					}
				});
			}
		}
	}

	/** The owner's body rule over the whole roster, and the set's packaged outfits as GuestBody sees them. */
	private static void rule(ClientGameTestContext context) {
		context.runOnClient(mc -> {
			for (GuestRoster.Guest guest : GuestRoster.ALL) {
				if (!guest.available()) {
					continue;
				}
				if (!guest.mayMarry()) {
					check(!GuestBody.SLIM.contains(guest.id()) && !GuestBody.FLARE.contains(guest.id()),
							guest.id() + " is held out of shaping: classic arms, no flare");
				}
				if (guest.origin() == GuestRoster.Origin.REAL_PERSON) {
					check(!GuestBody.SLIM.contains(guest.id()), guest.id() + " is a real person: clothing and hair only, classic arms");
				}
			}
			for (Sample sample : SET) {
				for (String outfit : GuestWardrobe.OUTFITS) {
					String skin = skin(sample.guest(), outfit);
					check(mc.getResourceManager().getResource(Jugcraft.id("textures/entity/townsfolk/" + skin + ".png")).isPresent(), skin + " is packaged");
					check(GuestBody.guest(skin).equals(sample.guest()), skin + " belongs to " + sample.guest());
					check(GuestBody.slim(skin) == sample.slim(), skin + (sample.slim() ? " has" : " has no") + " slim arms");
					check(GuestBody.FLARE.contains(GuestBody.guest(skin)) == sample.flare(), skin + (sample.flare() ? " has" : " has no") + " flare");
				}
			}
		});
	}

	/**
	 * In this outfit, each guest of the set as the renderer actually draws it: its own render state extracted from the
	 * entity by the townsfolk renderer, posed on the renderer's model, shows slim arms exactly for the slim sample, the
	 * flare exactly for the flared one, and exactly the head accessories GuestBody gives its guest.
	 */
	private static void follows(ClientGameTestContext context, int[] ids, String outfit) {
		context.runOnClient(mc -> {
			for (int i = 0; i < ids.length; i++) {
				Sample sample = SET.get(i);
				TownsfolkModel model = posed(mc, ids[i]);
				String where = sample.guest() + " in " + outfit;
				if (io.github.jimbozoomer.jugcraft.client.GuestPortraitModel.GUESTS.contains(sample.guest())) {
					check(model instanceof io.github.jimbozoomer.jugcraft.client.GuestPortraitModel, where + " uses the fitted guest rig");
					check(model.body.getChild("winter_collar").visible == outfit.equals("winter")
							&& model.head.getChild("spring_pin").visible == outfit.equals("spring")
							&& model.head.getChild("harvest_pin").visible == outfit.equals("harvest"), where + " switches seasonal layers");
					check(model.rightArm.x < -2F && near(model.leftArm.x, -model.rightArm.x)
							&& near(model.leftArm.y, model.rightArm.y)
							&& near(model.rightArm.x, model.rightArm.getInitialPose().x()), where + " keeps both authored arm pivots");
					continue;
				}
				check(model.rightArm.getChild("slim").visible == sample.slim() && model.leftArm.getChild("slim").visible == sample.slim()
						&& model.rightArm.getChild("wide").visible != sample.slim() && model.leftArm.getChild("wide").visible != sample.slim(),
						where + (sample.slim() ? " is drawn on slim arms" : " is drawn on classic arms"));
				check(model.body.getChild("guest_flare").visible == sample.flare(), where + (sample.flare() ? " shows" : " shows no") + " flare");
				// Vanilla's arms: classic at (+-5, 2); slim boxes at (+-5, 2.5), their pivots (a held item's) half a pixel in.
				float shift = sample.slim() ? TownsfolkModel.SLIM_SHIFT : 0.0F;
				ModelPart rightShown = model.rightArm.getChild(sample.slim() ? "slim" : "wide");
				ModelPart leftShown = model.leftArm.getChild(sample.slim() ? "slim" : "wide");
				check(near(model.rightArm.x, -5.0F + shift) && near(model.leftArm.x, 5.0F - shift) && near(model.rightArm.y, 2.0F + shift)
						&& near(model.leftArm.y, 2.0F + shift), where + ": the arms' pivots, where a held item is placed, are vanilla's: "
						+ model.rightArm.x + ", " + model.rightArm.y);
				check(near(model.rightArm.x + rightShown.x, -5.0F) && near(model.leftArm.x + leftShown.x, 5.0F)
						&& near(model.rightArm.y + rightShown.y, 2.0F + shift) && near(model.leftArm.y + leftShown.y, 2.0F + shift),
						where + ": the arms stand where vanilla's " + (sample.slim() ? "slim" : "classic") + " arms stand");
				check(model.head.getChild("guest_ears").visible == GuestBody.EARS.contains(sample.guest())
						&& model.head.getChild("guest_horns").visible == GuestBody.HORNS.contains(sample.guest())
						&& model.head.getChild("guest_elf_ears").visible == GuestBody.ELF_EARS.contains(sample.guest())
						&& model.head.getChild("guest_mage_hat").visible == GuestBody.MAGE_HAT.contains(sample.guest()),
						where + " wears exactly its own head accessories");
			}
		});
	}

	/** The renderer's model, posed for the entity as a frame would pose it (render thread). */
	private static TownsfolkModel posed(Minecraft mc, int id) {
		if (!(mc.level.getEntity(id) instanceof Townsfolk npc) || !(mc.getEntityRenderDispatcher().getRenderer(npc) instanceof TownsfolkRenderer renderer)) {
			throw new AssertionError("Guest " + id + " is not loaded on the client or not drawn by the townsfolk renderer");
		}
		TownsfolkRenderer.State state = renderer.createRenderState();
		renderer.extractRenderState(npc, state, 0.0F);
		TownsfolkModel model = renderer.modelFor(state);
		model.setupAnim(state);
		return model;
	}

	/**
	 * A book in the hand of the slim-armed and the classic-armed guest of the set, each close up (the item is placed from
	 * the arm's pivot, which {@link #follows} checks), then the hands emptied again. Guests hold nothing in play; this is
	 * the test's own fixture.
	 */
	private static void hands(ClientGameTestContext context, TestServerContext server, BlockPos[] at, int[] ids) {
		int slim = index("tifa_lockhart"), classic = index("lauren_chen");
		server.runOnServer(s -> {
			for (int i : new int[] {slim, classic}) {
				if (s.overworld().getEntity(ids[i]) instanceof Townsfolk npc) {
					npc.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOOK));
				}
			}
		});
		try {
			context.waitFor(mc -> mc.level.getEntity(ids[slim]) instanceof Townsfolk a && a.getMainHandItem().is(Items.BOOK)
					&& mc.level.getEntity(ids[classic]) instanceof Townsfolk b && b.getMainHandItem().is(Items.BOOK), 100);
			for (int i : new int[] {slim, classic}) {
				BlockPos guest = at[i];
				// Close, a little towards the guest's right hand (east: the player's left as they face each other).
				server.runCommand("tp @a " + coordinate(guest.getX() + 1.0) + " " + guest.getY() + " " + coordinate(guest.getZ() - 1.5));
				context.waitTicks(5);
				context.getInput().lookAt(guest.above());
				context.waitTicks(5);
				context.takeScreenshot(i == slim ? "guest_pose_held_slim" : "guest_pose_held_classic");
			}
		} finally {
			server.runOnServer(s -> {
				for (int i : new int[] {slim, classic}) {
					if (s.overworld().getEntity(ids[i]) instanceof Townsfolk npc) {
						npc.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
					}
				}
			});
		}
	}

	private static int index(String guest) {
		for (int i = 0; i < SET.size(); i++) {
			if (SET.get(i).guest().equals(guest)) {
				return i;
			}
		}
		throw new IllegalArgumentException(guest);
	}

	/**
	 * The skinnable layer's own geometry, baked as the renderer bakes it, against the body:
	 * <ul>
	 * <li>Frieren's ears begin on the hair (hat) layer's sides (the head's 4 pixels and the hair's {@link GuestBody#HAIR}),
	 * so each one-pixel box stands wholly proud of the hair, at the height they always had;</li>
	 * <li>the slim arm boxes are offset back out by exactly what the pivots move in ({@link TownsfolkModel#SLIM_SHIFT});</li>
	 * <li>the flare's front and back panels, posed by {@link GuestBody.Flare#setup} for walking legs (either leg up to a
	 * radian forward or back) and for vanilla's seated pose, never put a point of themselves inside either leg; at rest
	 * their inner top edge lies on the hip line, where the body ends, as before.</li>
	 * </ul>
	 */
	private static void geometry(ClientGameTestContext context) {
		context.runOnClient(mc -> {
			ModelPart root = TownsfolkModel.createSkinnableLayer().bakeRoot();
			ModelPart elf = root.getChild("head").getChild("guest_elf_ears");
			ModelPart right = elf.getChild("right"), left = elf.getChild("left");
			float hair = 4.0F + GuestBody.HAIR;
			check(near(right.x, -hair) && near(left.x, hair) && near(GuestBody.ELF_EAR_INNER, hair),
					"Frieren's ears begin on the hair layer's sides: " + right.x + ", " + left.x);
			check(near(right.y, -4.0F) && near(left.y, -4.0F) && near(right.z, 0.0F) && near(left.z, 0.0F), "at the height they always had");
			ModelPart rightSlim = root.getChild("right_arm").getChild("slim"), leftSlim = root.getChild("left_arm").getChild("slim");
			check(near(rightSlim.x, -TownsfolkModel.SLIM_SHIFT) && near(leftSlim.x, TownsfolkModel.SLIM_SHIFT) && near(rightSlim.y, 0.0F)
					&& near(leftSlim.y, 0.0F), "The slim arm boxes are offset back out by the pivots' shift");

			ModelPart body = root.getChild("body"), rightLeg = root.getChild("right_leg"), leftLeg = root.getChild("left_leg");
			ModelPart flare = body.getChild("guest_flare");
			GuestBody.Flare panels = GuestBody.Flare.of(body);
			check(panels != null, "The skinnable layer carries the flare");
			List<float[]> poses = new java.util.ArrayList<>();
			for (int step = -10; step <= 10; step++) {
				float a = step / 10.0F;
				poses.add(new float[] {a, 0.0F, 0.0F, -a, 0.0F, 0.0F});
			}
			// Vanilla's passenger pose: thighs forward, splayed a little.
			poses.add(new float[] {-1.4137167F, 0.31415927F, 0.07853982F, -1.4137167F, -0.31415927F, -0.07853982F});
			for (float[] pose : poses) {
				rightLeg.resetPose();
				leftLeg.resetPose();
				rightLeg.setRotation(pose[0], pose[1], pose[2]);
				leftLeg.setRotation(pose[3], pose[4], pose[5]);
				panels.setup("guest_frieren", false, body, rightLeg, leftLeg);
				check(flare.visible, "The flare shows for Frieren's robe");
				for (String side : List.of("front", "back")) {
					String inside = intrusion(body, flare, flare.getChild(side), side.equals("front"), rightLeg, leftLeg);
					check(inside == null, "No leg passes through the flare's " + side + " panel (legs " + java.util.Arrays.toString(pose) + "): " + inside);
				}
			}
			rightLeg.resetPose();
			leftLeg.resetPose();
			panels.setup("guest_frieren", false, body, rightLeg, leftLeg);
			Vector3f top = at(body, flare, flare.getChild("front"), 0.0F, GuestBody.FLARE_DROP, -GuestBody.FLARE_GAP);
			check(Math.abs(top.y - 12.0F) < 0.02F && Math.abs(top.z + 2.1F) < 0.05F, "At rest the front panel's inner top edge lies on the hip line: " + top);
			panels.setup("guest_frieren", true, body, rightLeg, leftLeg);
			check(!flare.visible, "and never over a player skin");
		});
	}

	/**
	 * A point of the panel's three boxes (a grid through each) that lies inside either leg's own box (shrunk by a
	 * hundredth of a pixel), or null. Pixels, model space.
	 */
	private static String intrusion(ModelPart body, ModelPart flare, ModelPart panel, boolean front, ModelPart rightLeg, ModelPart leftLeg) {
		Matrix4f[] legs = {inverse(rightLeg), inverse(leftLeg)};
		float z0 = front ? -GuestBody.FLARE_GAP - 1.0F : GuestBody.FLARE_GAP;
		for (int box = 0; box < 3; box++) {
			for (int i = 0; i <= 6; i++) {
				for (int j = 0; j <= 8; j++) {
					for (int k = 0; k <= 2; k++) {
						Vector3f point = at(body, flare, panel, -4.5F + 3.0F * box + i * 0.5F, GuestBody.FLARE_DROP + j * 0.5F, z0 + k * 0.5F);
						for (Matrix4f leg : legs) {
							Vector3f local = leg.transformPosition(point.x / 16.0F, point.y / 16.0F, point.z / 16.0F, new Vector3f()).mul(16.0F);
							float e = 0.01F;
							if (local.x > -2 + e && local.x < 2 - e && local.y > e && local.y < 12 - e && local.z > -2 + e && local.z < 2 - e) {
								return point + " (in the leg at " + local + ")";
							}
						}
					}
				}
			}
		}
		return null;
	}

	/** Where a point of {@code panel} (its own coordinates, pixels) stands in model space (pixels). */
	private static Vector3f at(ModelPart body, ModelPart flare, ModelPart panel, float x, float y, float z) {
		PoseStack pose = new PoseStack();
		body.translateAndRotate(pose);
		flare.translateAndRotate(pose);
		panel.translateAndRotate(pose);
		return pose.last().pose().transformPosition(x / 16.0F, y / 16.0F, z / 16.0F, new Vector3f()).mul(16.0F);
	}

	/** Model space to the leg's own coordinates (blocks). */
	private static Matrix4f inverse(ModelPart leg) {
		PoseStack pose = new PoseStack();
		leg.translateAndRotate(pose);
		return new Matrix4f(pose.last().pose()).invert();
	}

	private static boolean near(float a, float b) {
		return Math.abs(a - b) < 1.0E-4F;
	}

	/** The set together, then each guest close up, from where a player would stand. */
	private static void shoot(ClientGameTestContext context, TestServerContext server, BlockPos origin, BlockPos[] at, String outfit) {
		context.getInput().lookAt(origin.offset(0, 1, 4));
		context.waitTicks(10);
		context.takeScreenshot("guest_wardrobe_" + outfit);
		for (int i = 0; i < SET.size(); i++) {
			BlockPos guest = at[i];
			// Two and a half blocks in front of the guest, as a visitor talking to it would stand.
			server.runCommand("tp @a " + coordinate(guest.getX() + 0.5) + " " + guest.getY() + " " + coordinate(guest.getZ() - 2.0));
			context.waitTicks(5);
			context.getInput().lookAt(guest.above());
			context.waitTicks(5);
			context.takeScreenshot("guest_wardrobe_" + SET.get(i).guest() + "_" + outfit);
		}
		server.runCommand("tp @a " + coordinate(origin.getX() + 0.5) + " " + origin.getY() + " " + coordinate(origin.getZ() + 0.5));
		context.waitTicks(5);
	}

	/** A command coordinate (Double.toString never uses a locale's decimal comma). */
	private static String coordinate(double value) {
		return Double.toString(value);
	}

	/** Whether every guest of the set, as the client sees it, wears this outfit's skin. */
	private static boolean wearing(Minecraft mc, int[] ids, String outfit) {
		for (int i = 0; i < ids.length; i++) {
			if (!(mc.level.getEntity(ids[i]) instanceof Townsfolk npc) || !npc.skin().equals(skin(SET.get(i).guest(), outfit))) {
				return false;
			}
		}
		return true;
	}

	private static String skin(String guest, String outfit) {
		return outfit.equals(GuestWardrobe.NORMAL) ? "guest_" + guest : "guest_" + guest + "_" + outfit;
	}

	/** Holds the guests still, facing {@code yaw} (180: towards the player). */
	private static void face(TestServerContext server, int[] ids, float yaw) {
		server.runOnServer(s -> {
			for (int id : ids) {
				if (s.overworld().getEntity(id) instanceof Townsfolk npc) {
					npc.setNoAi(true);
					npc.setYRot(yaw);
					npc.setYHeadRot(yaw);
					npc.setYBodyRot(yaw);
				}
			}
		});
	}

	/** {@code /guests place} by the server console; the placed guest's network id. */
	private static int place(TestServerContext server, String id, BlockPos at) {
		server.runCommand("guests place %s %d %d %d 180".formatted(id, at.getX(), at.getY(), at.getZ()));
		return server.computeOnServer(s -> {
			UUID canonical = GuestLedger.get(s).canonical(id);
			Entity entity = canonical == null ? null : s.overworld().getEntity(canonical);
			if (!(entity instanceof Townsfolk npc) || !id.equals(npc.guest())) {
				throw new AssertionError("/guests place did not place " + id + " at " + at);
			}
			return npc.getId();
		});
	}

	private static void check(boolean ok, String message) {
		if (!ok) {
			throw new AssertionError(message);
		}
	}
}
