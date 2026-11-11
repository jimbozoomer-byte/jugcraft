package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.client.GuestBody;
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
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;

/**
 * JZE guest expansion, worker A's rendered checks (docs/audits/jze-guest-appearances.md): a representative set of named
 * guests, placed by an operator's {@code /guests place} in a creative test world, seen as a player sees them, in their
 * normal outfit, then in the winter outfit chosen by {@code /guests outfit}, then back in normal:
 * <ul>
 * <li>an adult fictional guest (Tifa Lockhart: slim arms, modest tailoring);</li>
 * <li>a younger, held guest (Tokai Teio: classic arms, straight proportions, ears);</li>
 * <li>a real person (Lauren Chen: classic arms, clothing and hair only);</li>
 * <li>Sarah Stock's non-gory wedding dress (classic arms, the gown's modest flare).</li>
 * </ul>
 * Screenshots: {@code guest_wardrobe_normal}, {@code guest_wardrobe_winter}, {@code guest_wardrobe_winter_back},
 * {@code guest_wardrobe_normal_again}, and a close-up of each, {@code guest_wardrobe_<guest>_<outfit>}. It also checks
 * that every outfit of the set is packaged, that GuestBody follows each guest into its outfits, and the owner's body
 * rule over the whole roster: no held guest has slim arms or a flare, no real person has slim arms.
 *
 * <p>Needs the coordinator's TownsfolkModel/TownsfolkRenderer/CustomNpcs patch (worker A's report) for the seasonal
 * skins to be accepted and for slim arms and the flare to be drawn.
 */
public class GuestWardrobeClientGameTests implements FabricClientGameTest {
	/** The set, left to right as the player sees it. */
	private static final List<Sample> SET = List.of(new Sample("tifa_lockhart", true, false), new Sample("tokai_teio", false, false),
			new Sample("lauren_chen", false, false), new Sample("sarah_stock", false, true));
	private static final String WINTER = "winter";

	private record Sample(String guest, boolean slim, boolean flare) {
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		rule(context);
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
				at[i] = origin.offset(-3 + 2 * i, 0, 4);
				ids[i] = place(server, SET.get(i).guest(), at[i]);
			}
			face(server, ids, 180.0F);
			context.waitFor(mc -> wearing(mc, ids, GuestWardrobe.NORMAL), 100);
			context.runOnClient(mc -> {
				mc.gui.hud.getChat().clearMessages(true);
				if (!mc.gui.hud.isHidden()) {
					mc.gui.hud.toggle();
				}
			});
			try {
				shoot(context, server, origin, at, GuestWardrobe.NORMAL);
				for (Sample sample : SET) {
					server.runCommand("guests outfit " + sample.guest() + " " + WINTER);
				}
				context.waitFor(mc -> wearing(mc, ids, WINTER), 200);
				shoot(context, server, origin, at, WINTER);
				face(server, ids, 0.0F);
				context.getInput().lookAt(origin.offset(0, 1, 4));
				context.waitTicks(10);
				context.takeScreenshot("guest_wardrobe_winter_back");
				face(server, ids, 180.0F);
				server.runCommand("guests outfit all " + GuestWardrobe.NORMAL);
				context.waitFor(mc -> wearing(mc, ids, GuestWardrobe.NORMAL), 200);
				context.runOnClient(mc -> mc.gui.hud.getChat().clearMessages(true));
				context.getInput().lookAt(origin.offset(0, 1, 4));
				context.waitTicks(10);
				context.takeScreenshot("guest_wardrobe_normal_again");
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
