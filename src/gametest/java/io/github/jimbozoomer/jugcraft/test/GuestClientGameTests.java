package io.github.jimbozoomer.jugcraft.test;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.jimbozoomer.jugcraft.client.GuestScreen;
import io.github.jimbozoomer.jugcraft.client.ScreenKit;
import io.github.jimbozoomer.jugcraft.finance.LandRegistry;
import io.github.jimbozoomer.jugcraft.town.TownProtection;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import io.github.jimbozoomer.jugcraft.town.guests.GuestBalance;
import io.github.jimbozoomer.jugcraft.town.guests.GuestFavors;
import io.github.jimbozoomer.jugcraft.town.guests.GuestLedger;
import io.github.jimbozoomer.jugcraft.town.guests.GuestMenu;
import io.github.jimbozoomer.jugcraft.town.guests.GuestPlacementTesting;
import io.github.jimbozoomer.jugcraft.town.guests.GuestRoster;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;

/**
 * Town guests (docs/features/jze-town-relationships.md): the guest menu as a player sees it, in a creative test world.
 * An operator's {@code /guests place} stands two guests in front of the player, one who may marry and one with
 * companionship only; the player talks to each (the server side of right-clicking it) and the {@link GuestScreen}
 * opens with the keys and states its synced values call for. The errand is accepted by clicking the screen's own key,
 * and the screen is checked to fit the smallest GUI. A guest that is not available (a stand-in menu for a pending
 * entry) shows only that it is not receiving visitors. Screenshots: {@code guest_menu_*}, {@code guest_placeholders}.
 *
 * <p>Written for the integrated build: it needs worker A's ledger and marriage and worker B's favors (a fresh day
 * offers each player an errand, GuestFavors).
 */
public class GuestClientGameTests implements FabricClientGameTest {
	/** The window Fabric's client game tests run in, put back after the fit check. */
	private static final int TEST_WINDOW_WIDTH = 854;
	private static final int TEST_WINDOW_HEIGHT = 480;

	@Override
	public void runTest(ClientGameTestContext context) {
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
				for (int x = -4; x <= 4; x++) {
					for (int z = 1; z <= 6; z++) {
						level.setBlockAndUpdate(origin.offset(x, -1, z), Blocks.STONE.defaultBlockState());
						for (int y = 0; y < 4; y++) {
							level.setBlockAndUpdate(origin.offset(x, y, z), Blocks.AIR.defaultBlockState());
						}
					}
				}
			});
			String marrying = server.computeOnServer(s -> unplaced(s, true));
			String companion = server.computeOnServer(s -> unplaced(s, false));
			check(marrying != null && companion != null, "Two guests are free to place");

			// A guest who may marry: placed by command, talked to, its menu checked, its errand accepted by a click.
			int first = place(server, marrying, origin.offset(0, 0, 3));
			talk(context, server, first);
			context.runOnClient(mc -> {
				GuestScreen screen = (GuestScreen) mc.gui.screen();
				GuestMenu menu = screen.getMenu();
				check(menu.opening().guest().equals(marrying) && !screen.showsUnavailable(), "The menu is " + marrying + "'s");
				check(menu.get(GuestMenu.AFFECTION) == 0 && menu.get(GuestMenu.MAX_AFFECTION) == GuestBalance.MAX_AFFECTION,
						"A new acquaintance: 0 of " + GuestBalance.MAX_AFFECTION + " affection");
				check(menu.get(GuestMenu.MAY_MARRY) == 1 && menu.get(GuestMenu.MAY_PROPOSE) == 0, "May marry, but no proposal yet");
				check(menu.get(GuestMenu.GUEST_SPOUSE) == GuestMenu.NOBODY && menu.get(GuestMenu.PLAYER_SPOUSE) == GuestMenu.NOBODY
						&& menu.get(GuestMenu.PLAYER_COOLDOWN_MINUTES) == 0 && menu.get(GuestMenu.GUEST_COOLDOWN_MINUTES) == 0,
						"Nobody married, no remarriage wait");
				shows(screen, GuestMenu.Action.GIFT, true, menu.get(GuestMenu.GIFTS_LEFT) > 0 && menu.get(GuestMenu.AFFECTION_LEFT_TODAY) > 0);
				shows(screen, GuestMenu.Action.PROPOSE, true, false);
				hidden(screen, GuestMenu.Action.PROPOSE_CONFIRM, GuestMenu.Action.PROPOSE_CANCEL, GuestMenu.Action.DIVORCE,
						GuestMenu.Action.DIVORCE_CONFIRM, GuestMenu.Action.DIVORCE_CANCEL);
				check(menu.questState() == GuestFavors.QuestState.OFFERED && !menu.questItem().isEmpty(), "Today's errand is offered");
				shows(screen, GuestMenu.Action.QUEST_ACCEPT, true, true);
				shows(screen, GuestMenu.Action.QUEST_TURN_IN, true, false);
			});
			context.takeScreenshot("guest_menu_offered");
			int revision = context.computeOnClient(mc -> ((GuestScreen) mc.gui.screen()).getMenu().get(GuestMenu.REVISION));
			click(context, GuestMenu.Action.QUEST_ACCEPT);
			context.waitFor(mc -> mc.gui.screen() instanceof GuestScreen screen && screen.getMenu().questState() == GuestFavors.QuestState.ACTIVE, 100);
			context.runOnClient(mc -> {
				GuestScreen screen = (GuestScreen) mc.gui.screen();
				check(screen.getMenu().get(GuestMenu.REVISION) != revision, "The accepted click moved the revision on");
				shows(screen, GuestMenu.Action.QUEST_ACCEPT, true, false);
				shows(screen, GuestMenu.Action.QUEST_TURN_IN, true, false);
			});
			context.takeScreenshot("guest_menu_errand_accepted");
			fits(context);
			context.runOnClient(mc -> mc.player.closeContainer());
			context.waitForScreen(null);

			// A guest with companionship only: no proposal controls at all.
			int second = place(server, companion, origin.offset(2, 0, 3));
			talk(context, server, second);
			context.runOnClient(mc -> {
				GuestScreen screen = (GuestScreen) mc.gui.screen();
				GuestMenu menu = screen.getMenu();
				check(menu.opening().guest().equals(companion) && menu.get(GuestMenu.MAY_MARRY) == 0 && menu.get(GuestMenu.MAY_PROPOSE) == 0,
						companion + " has companionship without marriage");
				shows(screen, GuestMenu.Action.GIFT, true, menu.get(GuestMenu.GIFTS_LEFT) > 0 && menu.get(GuestMenu.AFFECTION_LEFT_TODAY) > 0);
				hidden(screen, GuestMenu.Action.PROPOSE, GuestMenu.Action.PROPOSE_CONFIRM, GuestMenu.Action.PROPOSE_CANCEL,
						GuestMenu.Action.DIVORCE, GuestMenu.Action.DIVORCE_CONFIRM, GuestMenu.Action.DIVORCE_CANCEL);
			});
			context.takeScreenshot("guest_menu_companionship");
			context.runOnClient(mc -> mc.player.closeContainer());
			context.waitForScreen(null);

			// Both guests in the world, wearing their labelled placeholders.
			context.getInput().lookAt(origin.offset(1, 1, 3));
			context.waitTicks(10);
			context.takeScreenshot("guest_placeholders");

			// A guest that is not available: a stand-in menu for a pending entry shows only that.
			context.runOnClient(mc -> mc.gui.setScreen(new GuestScreen(new GuestMenu(0, mc.player.getInventory(), new GuestMenu.Opening(-1, "rooni")),
					mc.player.getInventory(), Component.literal("Rooni"))));
			context.waitTicks(2);
			try {
				context.runOnClient(mc -> {
					GuestScreen screen = (GuestScreen) mc.gui.screen();
					check(screen.showsUnavailable(), "A pending guest is shown as not receiving visitors");
					hidden(screen, GuestMenu.Action.values());
				});
				context.takeScreenshot("guest_menu_unavailable");
			} finally {
				context.runOnClient(mc -> {
					mc.player.containerMenu = mc.player.inventoryMenu;
					mc.gui.setScreen(null);
				});
			}
		}
	}

	/** An available guest, from the end of the roster, that may (or may not) marry and has never been placed. */
	private static String unplaced(MinecraftServer server, boolean mayMarry) {
		List<GuestRoster.Guest> all = new ArrayList<>(GuestRoster.ALL);
		Collections.reverse(all);
		for (GuestRoster.Guest guest : all) {
			if (guest.available() && guest.mayMarry() == mayMarry && GuestPlacementTesting.status(server, guest.id()).equals("UNPLACED")) {
				return guest.id();
			}
		}
		return null;
	}

	/** {@code /guests place} by the server console; the placed guest's network id. */
	private static int place(TestServerContext server, String id, BlockPos at) {
		server.runCommand("guests place %s %d %d %d 180".formatted(id, at.getX(), at.getY(), at.getZ()));
		return server.computeOnServer(s -> {
			UUID canonical = GuestLedger.get(s).canonical(id);
			Entity entity = canonical == null ? null : s.overworld().getEntity(canonical);
			if (!(entity instanceof Townsfolk npc) || !id.equals(npc.guest()) || !npc.blockPosition().equals(at)) {
				throw new AssertionError("/guests place did not place " + id + " at " + at + " (protected: "
						+ TownProtection.shieldsBlock(s.overworld(), at) + ", claimed: " + LandRegistry.isClaimed(s.overworld(), at) + ")");
			}
			return npc.getId();
		});
	}

	/** The player talks to the guest (what right-clicking it does on the server), and its screen opens. */
	private static void talk(ClientGameTestContext context, TestServerContext server, int npc) {
		server.runOnServer(s -> {
			ServerPlayer player = s.getPlayerList().getPlayers().getFirst();
			if (!(s.overworld().getEntity(npc) instanceof Townsfolk guest)) {
				throw new AssertionError("The guest is not loaded");
			}
			guest.talkTo(player, InteractionHand.MAIN_HAND);
		});
		context.waitForScreen(GuestScreen.class);
		context.waitTicks(5);
	}

	/** Clicks the screen's own key for {@code action} with the mouse. */
	private static void click(ClientGameTestContext context, GuestMenu.Action action) {
		double[] xy = context.computeOnClient(mc -> {
			Button key = ((GuestScreen) mc.gui.screen()).key(action);
			check(key != null && key.visible && key.active, action + " can be pressed");
			return new double[] {(key.getX() + key.getWidth() / 2.0) / MouseHandler.getScaledXPos(mc.getWindow(), 1),
					(key.getY() + key.getHeight() / 2.0) / MouseHandler.getScaledYPos(mc.getWindow(), 1)};
		});
		context.getInput().setCursorPos(xy[0], xy[1]);
		context.waitTick();
		context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
		context.waitTick();
		context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);
	}

	/** The screen fits the smallest GUI Minecraft's automatic scale gives (a 640 x 480 window, 320 x 240 GUI pixels). */
	private static void fits(ClientGameTestContext context) {
		int scale = context.computeOnClient(mc -> mc.options.guiScale().get());
		context.getInput().resizeWindow(ScreenKit.SMALLEST_GUI_WIDTH * 2, ScreenKit.SMALLEST_GUI_HEIGHT * 2);
		context.runOnClient(mc -> {
			mc.options.guiScale().set(0);
			mc.resizeGui();
		});
		context.waitTicks(2);
		try {
			context.runOnClient(mc -> {
				GuestScreen screen = (GuestScreen) mc.gui.screen();
				ScreenRectangle area = screen.panelArea();
				check(area.left() >= 0 && area.top() >= 0 && area.right() <= screen.width && area.bottom() <= screen.height,
						"The guest screen's " + area.width() + " x " + area.height() + " panel fits the " + screen.width + " x " + screen.height + " GUI");
			});
			context.takeScreenshot("guest_menu_fit_320x240");
		} finally {
			context.runOnClient(mc -> mc.options.guiScale().set(scale));
			context.getInput().resizeWindow(TEST_WINDOW_WIDTH, TEST_WINDOW_HEIGHT);
			context.runOnClient(Minecraft::resizeGui);
			context.waitTicks(2);
		}
	}

	private static void shows(GuestScreen screen, GuestMenu.Action action, boolean visible, boolean active) {
		Button key = screen.key(action);
		check(key != null && key.visible == visible && key.active == (visible && active),
				action + " should be " + (visible ? active ? "shown and active" : "shown and dimmed" : "hidden"));
	}

	private static void hidden(GuestScreen screen, GuestMenu.Action... actions) {
		for (GuestMenu.Action action : actions) {
			shows(screen, action, false, false);
		}
	}

	private static void check(boolean ok, String message) {
		if (!ok) {
			throw new AssertionError(message);
		}
	}
}
