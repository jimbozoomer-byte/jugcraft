package io.github.jimbozoomer.jugcraft.test;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jimbozoomer.jugcraft.client.GuestPortraitModel;
import io.github.jimbozoomer.jugcraft.client.TownsfolkRenderer;
import io.github.jimbozoomer.jugcraft.client.GuestScreen;
import io.github.jimbozoomer.jugcraft.client.ScreenKit;
import io.github.jimbozoomer.jugcraft.finance.LandRegistry;
import io.github.jimbozoomer.jugcraft.town.TownProtection;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import io.github.jimbozoomer.jugcraft.town.guests.GuestBalance;
import io.github.jimbozoomer.jugcraft.town.guests.GuestFavors;
import io.github.jimbozoomer.jugcraft.town.guests.GuestFeature;
import io.github.jimbozoomer.jugcraft.town.guests.GuestFeatures;
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
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.UvMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

/**
 * Town guests (docs/features/jze-town-relationships.md): the guest menu as a player sees it, in a creative test world.
 * An operator's {@code /guests place} stands two guests in front of the player, one who may marry and one with
 * companionship only; the player talks to each (the server side of right-clicking it) and the {@link GuestScreen}
 * opens with the keys and states its synced values call for. The errand is accepted by clicking the screen's own key;
 * the Gifts key opens the gift list (the guest's favourites, every accepted gift, the daily limits; display only) and
 * closes it again; and the screen, with the list open, is checked to fit the smallest GUI. A guest that is not available (a stand-in menu for a pending
 * entry) shows only that it is not receiving visitors. Screenshots: {@code guest_menu_*} (among them
 * {@code guest_menu_gift_help}), {@code guest_character_skins}.
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
				Button activities = screen.activitiesKey();
				check(activities != null && activities.getY() + activities.getHeight() <= screen.key(GuestMenu.Action.PROPOSE).getY(),
						"Activities stays above the proposal controls and wrapped explanation");
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

			// The gift list (display only): this guest's favourites, every accepted gift and the daily limits.
			click(context, mc -> ((GuestScreen) mc.gui.screen()).giftListKey(), "The Gifts key");
			context.waitTicks(2);
			context.runOnClient(mc -> {
				GuestScreen screen = (GuestScreen) mc.gui.screen();
				check(screen.showsGiftList(), "The gift list is shown");
				check(screen.shownFavourites().equals(GuestFavors.favourites(marrying)) && screen.shownFavourites().size() == GuestFavors.FAVOURITES,
						"It shows " + marrying + "'s favourites: " + screen.shownFavourites());
				hidden(screen, GuestMenu.Action.values());
				within(screen, screen.giftListArea(), "gift list");
			});
			context.takeScreenshot("guest_menu_gift_help");
			fits(context);
			click(context, mc -> ((GuestScreen) mc.gui.screen()).giftListKey(), "The Back key");
			context.waitTicks(2);
			context.runOnClient(mc -> {
				GuestScreen screen = (GuestScreen) mc.gui.screen();
				check(!screen.showsGiftList() && screen.shownFavourites().isEmpty(), "The gift list closes again");
				shows(screen, GuestMenu.Action.QUEST_ACCEPT, true, false);
			});
			click(context, mc -> ((GuestScreen) mc.gui.screen()).activitiesKey(), "The Activities key");
			context.waitTicks(2);
			context.runOnClient(mc -> {
				GuestScreen screen = (GuestScreen) mc.gui.screen();
				check(screen.showsActivities() && java.util.Arrays.stream(screen.shownOptions()).anyMatch(i -> i >= 0),
						"Activities opens with server-offered options");
				hidden(screen, GuestMenu.Action.values());
			});
			context.takeScreenshot("guest_menu_activities");
			click(context, mc -> ((GuestScreen) mc.gui.screen()).activitiesKey(), "The Activities Back key");
			context.waitTicks(2);
			marriageButtons(context, server, marrying);
			context.runOnClient(mc -> mc.player.closeContainer());
			context.waitForScreen(null);

			// A friendship-only guest can receive gifts, but cannot be proposed to.
			int second = place(server, companion, origin.offset(2, 0, 3));
			talk(context, server, second);
			context.runOnClient(mc -> {
				GuestScreen screen = (GuestScreen) mc.gui.screen();
				GuestMenu menu = screen.getMenu();
				check(menu.opening().guest().equals(companion) && menu.get(GuestMenu.MAY_MARRY) == 0 && menu.get(GuestMenu.MAY_PROPOSE) == 0,
						companion + " remains friendship-only");
				shows(screen, GuestMenu.Action.GIFT, true, menu.get(GuestMenu.GIFTS_LEFT) > 0 && menu.get(GuestMenu.AFFECTION_LEFT_TODAY) > 0);
				hidden(screen, GuestMenu.Action.PROPOSE);
				hidden(screen, GuestMenu.Action.PROPOSE_CONFIRM, GuestMenu.Action.PROPOSE_CANCEL,
						GuestMenu.Action.DIVORCE, GuestMenu.Action.DIVORCE_CONFIRM, GuestMenu.Action.DIVORCE_CANCEL);
			});
			context.takeScreenshot("guest_menu_companionship");
			context.runOnClient(mc -> mc.player.closeContainer());
			context.waitForScreen(null);

			// Both guests in the world, wearing their labelled placeholders.
			context.getInput().lookAt(origin.offset(1, 1, 3));
			context.waitTicks(10);
			context.takeScreenshot("guest_character_skins");
			gallery(context, server, origin, first);
			for (var guest : GuestRoster.ALL) {
				if (guest.edition() != GuestRoster.Edition.ADULT_TIME_SKIP) continue;
				String id = guest.id();
				if (id.equals("asuka_langley") || id.equals("rei_ayanami")) {
					check(guest.timeSkipAge().orElse(-1) == 18, "EVA pilot is 18 in the Jugcraft time skip: " + id);
				}
				context.runOnClient(mc -> mc.gui.setScreen(new GuestScreen(
						new GuestMenu(0, mc.player.getInventory(), new GuestMenu.Opening(-1, id)),
						mc.player.getInventory(), Component.literal(guest.name()))));
				context.waitTicks(3);
				context.takeScreenshot("guest_time_skip_menu_" + id);
				context.runOnClient(mc -> mc.gui.setScreen(null));
			}

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

    /** Render every packaged skin on an actual NPC, including all head accessories. */
    private static void gallery(ClientGameTestContext context, TestServerContext server, BlockPos origin, int npcId) {
        double x = origin.getX() - 2.5, y = origin.getY(), z = origin.getZ() + 4.5;
        server.runOnServer(s -> {
            var npc = (Townsfolk) s.overworld().getEntity(npcId);
            npc.setNoAi(true);
            npc.getNavigation().stop();
            npc.setDeltaMovement(0, 0, 0);
            npc.snapTo(x, y, z, 180F, 0F);
        });
        server.runCommand("tp @a " + x + " " + y + " " + (z - 4) + " 0 8.5");
        context.waitFor(mc -> mc.level.getEntity(npcId).distanceToSqr(x, y, z) < .01
                && mc.player.distanceToSqr(x, y, z - 4) < .01, 100);
        context.runOnClient(mc -> { mc.gui.hud.getChat().clearMessages(true); if (!mc.gui.hud.isHidden()) mc.gui.hud.toggle(); });
        for (GuestRoster.Guest guest : GuestRoster.ALL) {
            if (!guest.available()) continue;
            var look = io.github.jimbozoomer.jugcraft.town.guests.GuestAppearances.of(guest.id());
            context.runOnClient(mc -> check(mc.getResourceManager().getResource(
                    io.github.jimbozoomer.jugcraft.Jugcraft.id("textures/entity/townsfolk/" + look.townSkin() + ".png")).isPresent(),
                    "Packaged skin " + guest.id()));
            server.runOnServer(s -> {
                Townsfolk npc = (Townsfolk) s.overworld().getEntity(npcId);
                check(io.github.jimbozoomer.jugcraft.town.CustomNpcs.setTownSkin(npc, look.townSkin()), "Skin accepted " + guest.id());
                npc.setNoAi(true);
                var viewer = s.getPlayerList().getPlayers().getFirst();
                float yaw = (float) Math.toDegrees(Math.atan2(npc.getX() - viewer.getX(), viewer.getZ() - npc.getZ()));
                npc.setYRot(yaw);
                npc.setYHeadRot(yaw);
                npc.setYBodyRot(yaw);
                npc.setCustomName(Component.literal(guest.name()));
            });
            context.waitFor(mc -> mc.level.getEntity(npcId) instanceof Townsfolk present
                    && present.skin().equals(look.townSkin()), 100);
            context.runOnClient(mc -> {
                var present = (Townsfolk) mc.level.getEntity(npcId);
                var renderer = (TownsfolkRenderer) mc.getEntityRenderDispatcher().getRenderer(present);
                var state = renderer.createRenderState();
                renderer.extractRenderState(present, state, 0);
                var submission = new SubmitNodeStorage() {
                    Model<?> body;
                    @Override public <S> void submitModel(Model<? super S> model, S frame, PoseStack pose,
                            RenderType type, int light, int overlay, int color, UvMapping uv, int outline) {
                        if (body == null) body = model;
                    }
                };
                renderer.submit(state, new PoseStack(), submission, new CameraRenderState());
                boolean detailed = GuestPortraitModel.GUESTS.contains(guest.id());
                check(submission.body == renderer.modelFor(state) && (submission.body instanceof GuestPortraitModel) == detailed,
                        "Submitted mesh matches the atlas for " + guest.id());
                check(mc.getResourceManager().getResource(renderer.getTextureLocation(state)).isPresent(),
                        "Visible rig has its own atlas for " + guest.id());
            });
            context.waitTicks(6);
            captureAngle(context, server, npcId, 0F, "guest_skin_" + guest.id());
            if (GuestPortraitModel.GUESTS.contains(guest.id())) {
                captureAngle(context, server, npcId, 90F, "guest_profile_" + guest.id());
                if (guest.id().equals("misato_katsuragi")) {
                    captureAngle(context, server, npcId, 180F, "guest_back_" + guest.id());
                    captureAngle(context, server, npcId, -90F, "guest_left_" + guest.id());
                }
            }
        }
        context.runOnClient(mc -> { if (mc.gui.hud.isHidden()) mc.gui.hud.toggle(); });
    }

    private static void captureAngle(ClientGameTestContext context, TestServerContext server, int id, float angle, String file) {
        server.runOnServer(s -> {
            Townsfolk npc = (Townsfolk) s.overworld().getEntity(id);
            var viewer = s.getPlayerList().getPlayers().getFirst();
            float yaw = angle + (float) Math.toDegrees(Math.atan2(npc.getX()-viewer.getX(), viewer.getZ()-npc.getZ()));
            npc.setYRot(yaw);
            npc.setYHeadRot(yaw);
            npc.setYBodyRot(yaw);
        });
        context.waitTicks(4);
        context.runOnClient(mc -> {
            var npc = (Townsfolk) mc.level.getEntity(id);
            float yaw = angle + (float) Math.toDegrees(Math.atan2(npc.getX()-mc.player.getX(), mc.player.getZ()-npc.getZ()));
            npc.setYRot(yaw);
            npc.yRotO = npc.yBodyRot = npc.yBodyRotO = npc.yHeadRot = npc.yHeadRotO = yaw;
        });
        context.takeScreenshot(file);
    }

	/** Exercise both confirmations over the real menu packets; only affection and the proposal item are fixture setup. */
	private static void marriageButtons(ClientGameTestContext context, TestServerContext server, String guest) {
		server.runOnServer(s -> {
			ServerPlayer p = s.getPlayerList().getPlayers().getFirst();
			GuestLedger.get(s).addAffection(p.getUUID(), guest, GuestBalance.MAX_AFFECTION);
			p.getInventory().setItem(0, new ItemStack(GuestBalance.PROPOSAL_ITEM, GuestBalance.PROPOSAL_COUNT));
		});
		context.waitFor(mc -> mc.gui.screen() instanceof GuestScreen screen && screen.key(GuestMenu.Action.PROPOSE).active, 100);
		context.waitTicks(GuestBalance.ACTION_GAP_TICKS + 2);
		click(context, GuestMenu.Action.PROPOSE);
		context.waitFor(mc -> mc.gui.screen() instanceof GuestScreen screen && screen.getMenu().get(GuestMenu.PROPOSAL_PENDING) == 1, 100);
		context.waitTicks(GuestBalance.ACTION_GAP_TICKS + 2);
		server.runOnServer(s -> {
			ServerPlayer p = s.getPlayerList().getPlayers().getFirst();
			check(GuestLedger.get(s).spouseOf(p.getUUID()) == null && p.getInventory().countItem(GuestBalance.PROPOSAL_ITEM) == GuestBalance.PROPOSAL_COUNT,
					"Requesting a proposal neither marries nor consumes its item");
		});
		context.takeScreenshot("guest_menu_proposal_confirmation");
		click(context, GuestMenu.Action.PROPOSE_CONFIRM);
		context.waitFor(mc -> mc.gui.screen() instanceof GuestScreen screen && screen.getMenu().get(GuestMenu.PLAYER_SPOUSE) == GuestMenu.EACH_OTHER, 100);
		context.waitTicks(GuestBalance.ACTION_GAP_TICKS + 2);
		server.runOnServer(s -> {
			ServerPlayer p = s.getPlayerList().getPlayers().getFirst();
			check(guest.equals(GuestLedger.get(s).spouseOf(p.getUUID())) && p.getInventory().countItem(GuestBalance.PROPOSAL_ITEM) == 0,
					"The real confirmation packet marries once and consumes the proposal item once");
		});
		context.takeScreenshot("guest_menu_married");
		activitiesAtTheSmallestGui(context);
		click(context, GuestMenu.Action.DIVORCE);
		context.waitFor(mc -> mc.gui.screen() instanceof GuestScreen screen && screen.getMenu().get(GuestMenu.DIVORCE_PENDING) == 1, 100);
		context.waitTicks(GuestBalance.ACTION_GAP_TICKS + 2);
		server.runOnServer(s -> check(guest.equals(GuestLedger.get(s).spouseOf(s.getPlayerList().getPlayers().getFirst().getUUID())),
				"Requesting divorce keeps the marriage until confirmation"));
		context.takeScreenshot("guest_menu_divorce_confirmation");
		click(context, GuestMenu.Action.DIVORCE_CONFIRM);
		context.waitFor(mc -> mc.gui.screen() instanceof GuestScreen screen && screen.getMenu().get(GuestMenu.PLAYER_SPOUSE) == GuestMenu.NOBODY
				&& screen.getMenu().get(GuestMenu.PLAYER_COOLDOWN_MINUTES) > 0 && screen.getMenu().get(GuestMenu.GUEST_COOLDOWN_MINUTES) > 0, 100);
		context.waitTicks(GuestBalance.ACTION_GAP_TICKS + 2);
		context.runOnClient(mc -> {
			GuestScreen screen = (GuestScreen) mc.gui.screen();
			shows(screen, GuestMenu.Action.PROPOSE, true, false);
			hidden(screen, GuestMenu.Action.DIVORCE, GuestMenu.Action.DIVORCE_CONFIRM);
		});
		context.takeScreenshot("guest_menu_remarriage_cooldowns");
	}

	/**
	 * The Activities panel at the smallest GUI (320 x 240) with the most options: the full feature set registered, the
	 * player married to the guest at full affection (so a couple's options are offered as well), and then every
	 * registered option marked offered on the client's copy of the menu (display only: nothing is sent, and the synced
	 * values are put back afterwards). Checked: twelve options to a page and the rest on the next; the page written
	 * between More and Back; every key and the page inside the GUI and the screen's panel and none overlapping another;
	 * the gift list, the Gifts key and the proposal and divorce keys hidden while it is open; every label whole, in at
	 * most two lines within its key; Tab reaching the shown options in reading order, then More, then Back, then the
	 * first option again, never a hidden key; Enter on More turning the page (back to the first after the last) and on
	 * Back closing the panel, which opens again on its first page; and, closed, the Activities key above the companionship
	 * controls and the proposal hints. Screenshots {@code guest_menu_activities_320x240_page1} and {@code _page2}.
	 */
	private static void activitiesAtTheSmallestGui(ClientGameTestContext context) {
		int scale = context.computeOnClient(mc -> mc.options.guiScale().get());
		int[] synced = new int[GuestFeatures.MAX_OPTIONS / 16];
		context.getInput().resizeWindow(ScreenKit.SMALLEST_GUI_WIDTH * 2, ScreenKit.SMALLEST_GUI_HEIGHT * 2);
		context.runOnClient(mc -> {
			mc.options.guiScale().set(0);
			mc.resizeGui();
		});
		context.waitTicks(2);
		try {
			int offeredNow = context.computeOnClient(mc -> {
				GuestScreen screen = (GuestScreen) mc.gui.screen();
				check(screen.width == ScreenKit.SMALLEST_GUI_WIDTH && screen.height == ScreenKit.SMALLEST_GUI_HEIGHT,
						"The GUI is the smallest, 320 x 240: " + screen.width + " x " + screen.height);
				List<String> features = GuestFeatures.all().stream().map(GuestFeature::id).toList();
				check(features.containsAll(List.of("personality", "outings", "milestones"))
						&& GuestFeatures.options().size() > screen.optionKeys().size(),
						"The full feature set is registered, with more options than one page holds: " + features + ", "
								+ GuestFeatures.options().size());
				int offered = 0;
				for (GuestFeatures.Entry entry : GuestFeatures.options()) {
					offered += screen.getMenu().optionAvailable(entry.index()) ? 1 : 0;
				}
				return offered;
			});
			click(context, mc -> ((GuestScreen) mc.gui.screen()).activitiesKey(), "The Activities key");
			context.waitTicks(2);
			context.runOnClient(mc -> {
				GuestScreen screen = (GuestScreen) mc.gui.screen();
				check(screen.showsActivities() && shown(screen) == Math.min(offeredNow, screen.optionKeys().size()),
						"The married couple's own options are shown: " + shown(screen) + " of " + offeredNow);
				// The most options: every registered option offered, on the client's copy only.
				GuestMenu menu = screen.getMenu();
				for (int i = 0; i < synced.length; i++) {
					synced[i] = menu.get(GuestMenu.OPTIONS_AVAILABLE + i);
				}
				for (GuestFeatures.Entry entry : GuestFeatures.options()) {
					int field = GuestMenu.OPTIONS_AVAILABLE + entry.index() / 16;
					menu.setData(field, menu.get(field) | 1 << entry.index() % 16);
				}
			});
			context.waitTicks(2);
			context.runOnClient(mc -> {
				GuestScreen screen = (GuestScreen) mc.gui.screen();
				int all = GuestFeatures.options().size(), perPage = screen.optionKeys().size();
				check(screen.activitiesPage() == 0 && screen.activitiesPages() == (all + perPage - 1) / perPage && shown(screen) == perPage,
						"Every option offered: a full first page of " + perPage + ", " + screen.activitiesPages() + " pages");
				panel(mc, screen);
			});
			context.takeScreenshot("guest_menu_activities_320x240_page1");

			// Tab: the shown options in reading order, then More, then Back, then round to the first option.
			context.runOnClient(mc -> mc.gui.screen().setFocused(null));
			int stops = context.computeOnClient(mc -> tabOrder((GuestScreen) mc.gui.screen()).size());
			for (int i = 0; i <= stops; i++) {
				context.getInput().pressKey(InputConstants.KEY_TAB);
				context.waitTick();
				int stop = i % stops;
				context.runOnClient(mc -> {
					GuestScreen screen = (GuestScreen) mc.gui.screen();
					List<Button> order = tabOrder(screen);
					check(screen.getFocused() == order.get(stop), "Tab " + (stop + 1) + " focuses " + order.get(stop).getMessage().getString()
							+ ", not " + (screen.getFocused() instanceof Button b ? b.getMessage().getString() : String.valueOf(screen.getFocused())));
				});
			}
			// Enter on More turns the page; the next page shows the rest, as well laid out.
			focus(context, mc -> ((GuestScreen) mc.gui.screen()).pageKey());
			context.getInput().pressKey(InputConstants.KEY_RETURN);
			context.waitTicks(2);
			context.runOnClient(mc -> {
				GuestScreen screen = (GuestScreen) mc.gui.screen();
				int all = GuestFeatures.options().size(), perPage = screen.optionKeys().size();
				check(screen.activitiesPage() == 1 && shown(screen) == Math.min(perPage, all - perPage), "More shows the second page: "
						+ screen.activitiesPage() + ", " + shown(screen) + " options");
				panel(mc, screen);
				check(tabOrder(screen).stream().allMatch(b -> b.visible && b.active), "Only shown keys take the focus");
			});
			context.takeScreenshot("guest_menu_activities_320x240_page2");
			int pages = context.computeOnClient(mc -> ((GuestScreen) mc.gui.screen()).activitiesPages());
			for (int page = 2; page <= pages; page++) {
				context.getInput().pressKey(InputConstants.KEY_RETURN);
				context.waitTicks(2);
			}
			check(context.computeOnClient(mc -> ((GuestScreen) mc.gui.screen()).activitiesPage()) == 0, "After the last page More goes back to the first");
			// Closed on its second page by Enter on Back, the panel opens again on its first page.
			focus(context, mc -> ((GuestScreen) mc.gui.screen()).pageKey());
			context.getInput().pressKey(InputConstants.KEY_RETURN);
			context.waitTicks(2);
			check(context.computeOnClient(mc -> ((GuestScreen) mc.gui.screen()).activitiesPage()) == 1, "(on the second page)");
			focus(context, mc -> ((GuestScreen) mc.gui.screen()).activitiesKey());
			context.getInput().pressKey(InputConstants.KEY_RETURN);
			context.waitTicks(2);
			context.runOnClient(mc -> {
				GuestScreen screen = (GuestScreen) mc.gui.screen();
				check(!screen.showsActivities() && !screen.showsGiftList() && screen.pageLabelArea().width() == 0, "Back closes the panel");
				Button activities = screen.activitiesKey();
				check(activities != null && activities.visible, "The Activities key is shown");
				ScreenRectangle key = area(activities);
				for (GuestMenu.Action action : GuestMenu.Action.values()) {
					Button other = action == GuestMenu.Action.OPTION ? null : screen.key(action);
					check(other == null || !other.visible || !overlap(key, area(other)), "The Activities key does not cover " + action);
				}
				Button divorce = screen.key(GuestMenu.Action.DIVORCE);
				check(divorce != null && divorce.visible && key.bottom() <= divorce.getY(), "The Activities key lies above the companionship controls");
				within(screen, key, "Activities key");
			});
			context.getInput().pressKey(InputConstants.KEY_RETURN);
			context.waitTicks(2);
			context.runOnClient(mc -> {
				GuestScreen screen = (GuestScreen) mc.gui.screen();
				check(screen.showsActivities() && screen.activitiesPage() == 0, "Opened again, the panel starts on its first page");
			});
			context.getInput().pressKey(InputConstants.KEY_RETURN);
			context.waitTicks(2);
			check(context.computeOnClient(mc -> !((GuestScreen) mc.gui.screen()).showsActivities()), "and Back closes it again");
		} finally {
			context.runOnClient(mc -> {
				if (mc.gui.screen() instanceof GuestScreen screen) {
					for (int i = 0; i < synced.length; i++) {
						screen.getMenu().setData(GuestMenu.OPTIONS_AVAILABLE + i, synced[i]);
					}
				}
				mc.options.guiScale().set(scale);
			});
			context.getInput().resizeWindow(TEST_WINDOW_WIDTH, TEST_WINDOW_HEIGHT);
			context.runOnClient(Minecraft::resizeGui);
			context.waitTicks(2);
		}
	}

	/**
	 * The open Activities panel's layout: its shown keys and the page inside the GUI and the screen's panel, none
	 * overlapping another, the gift list and every action key hidden, and each shown option's label whole in at most two
	 * lines within its key.
	 */
	private static void panel(Minecraft mc, GuestScreen screen) {
		check(screen.showsActivities() && !screen.showsGiftList() && screen.giftListKey() != null && !screen.giftListKey().visible,
				"The gift list and its key are hidden under the Activities panel");
		hidden(screen, GuestMenu.Action.values());
		List<ScreenRectangle> areas = new ArrayList<>();
		List<String> names = new ArrayList<>();
		for (Button key : tabOrder(screen)) {
			areas.add(area(key));
			names.add(key.getMessage().getString());
		}
		check(screen.activitiesPages() < 2 || screen.pageLabelArea().width() > 0, "The page is shown");
		if (screen.pageLabelArea().width() > 0) {
			areas.add(screen.pageLabelArea());
			names.add("the page");
		}
		for (int i = 0; i < areas.size(); i++) {
			within(screen, areas.get(i), names.get(i));
			for (int j = i + 1; j < areas.size(); j++) {
				check(!overlap(areas.get(i), areas.get(j)), names.get(i) + " overlaps " + names.get(j));
			}
		}
		int[] options = screen.shownOptions();
		for (int slot = 0; slot < options.length; slot++) {
			if (options[slot] < 0) {
				continue;
			}
			GuestFeatures.Entry entry = GuestFeatures.option(options[slot]);
			String label = entry.feature().label(entry.option()).getString();
			List<String> lines = screen.optionLines(slot);
			Button key = screen.optionKeys().get(slot);
			check(!lines.isEmpty() && lines.size() <= 2 && String.join("", lines).replace(" ", "").equals(label.replace(" ", "")),
					"\"" + label + "\" is shown whole, in at most two lines: " + lines);
			for (String line : lines) {
				check(mc.font.width(line) <= key.getWidth() - 4, "\"" + line + "\" fits its key");
			}
		}
	}

	/** The Activities panel's shown keys in the order Tab reaches them: the options in reading order, More, Back. */
	private static List<Button> tabOrder(GuestScreen screen) {
		List<Button> order = new ArrayList<>();
		for (Button key : screen.optionKeys()) {
			if (key.visible && key.active) {
				order.add(key);
			}
		}
		for (Button key : java.util.Arrays.asList(screen.pageKey(), screen.activitiesKey())) {
			if (key != null && key.visible && key.active) {
				order.add(key);
			}
		}
		return order;
	}

	private static int shown(GuestScreen screen) {
		int shown = 0;
		for (int option : screen.shownOptions()) {
			shown += option >= 0 ? 1 : 0;
		}
		return shown;
	}

	/** Focuses a key of the open screen, as Tab would leave it. */
	private static void focus(ClientGameTestContext context, java.util.function.Function<Minecraft, Button> which) {
		context.runOnClient(mc -> {
			Button key = which.apply(mc);
			check(key != null && key.visible && key.active, "The key can take the focus");
			mc.gui.screen().setFocused(key);
		});
	}

	private static ScreenRectangle area(Button key) {
		return new ScreenRectangle(key.getX(), key.getY(), key.getWidth(), key.getHeight());
	}

	private static boolean overlap(ScreenRectangle a, ScreenRectangle b) {
		return a.left() < b.right() && b.left() < a.right() && a.top() < b.bottom() && b.top() < a.bottom();
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
		click(context, mc -> ((GuestScreen) mc.gui.screen()).key(action), action.toString());
	}

	/** Clicks a key of the open screen with the mouse. */
	private static void click(ClientGameTestContext context, java.util.function.Function<Minecraft, Button> which, String name) {
		double[] xy = context.computeOnClient(mc -> {
			Button key = which.apply(mc);
			check(key != null && key.visible && key.active, name + " can be pressed");
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
				within(screen, screen.panelArea(), "panel");
				if (screen.showsGiftList()) {
					within(screen, screen.giftListArea(), "gift list");
				}
			});
			context.takeScreenshot("guest_menu_fit_320x240");
		} finally {
			context.runOnClient(mc -> mc.options.guiScale().set(scale));
			context.getInput().resizeWindow(TEST_WINDOW_WIDTH, TEST_WINDOW_HEIGHT);
			context.runOnClient(Minecraft::resizeGui);
			context.waitTicks(2);
		}
	}

	/** {@code area} lies inside the GUI and, unless it is the panel itself, inside the screen's panel. */
	private static void within(GuestScreen screen, ScreenRectangle area, String what) {
		ScreenRectangle panel = screen.panelArea();
		check(area.left() >= 0 && area.top() >= 0 && area.right() <= screen.width && area.bottom() <= screen.height,
				"The guest screen's " + area.width() + " x " + area.height() + " " + what + " fits the " + screen.width + " x " + screen.height + " GUI");
		check(area.left() >= panel.left() && area.top() >= panel.top() && area.right() <= panel.right() && area.bottom() <= panel.bottom(),
				"The " + what + " stays inside the screen's panel");
	}

	private static void shows(GuestScreen screen, GuestMenu.Action action, boolean visible, boolean active) {
		Button key = screen.key(action);
		check(key != null && key.visible == visible && key.active == (visible && active),
				action + " should be " + (visible ? active ? "shown and active" : "shown and dimmed" : "hidden"));
	}

	private static void hidden(GuestScreen screen, GuestMenu.Action... actions) {
		for (GuestMenu.Action action : actions) {
			if (action == GuestMenu.Action.OPTION) {
				check(screen.key(action) == null, "Feature options use the Activities slots, not a single action key");
				continue;
			}
			shows(screen, action, false, false);
		}
	}

	private static void check(boolean ok, String message) {
		if (!ok) {
			throw new AssertionError(message);
		}
	}
}
