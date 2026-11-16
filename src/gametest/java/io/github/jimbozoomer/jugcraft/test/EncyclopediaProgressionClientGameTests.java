package io.github.jimbozoomer.jugcraft.test;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.jimbozoomer.jugcraft.client.EncyclopediaClient;
import io.github.jimbozoomer.jugcraft.client.EncyclopediaRoutesScreen;
import io.github.jimbozoomer.jugcraft.client.EncyclopediaScreen;
import io.github.jimbozoomer.jugcraft.client.HandbookScreen;
import io.github.jimbozoomer.jugcraft.guide.encyclopedia.EncyclopediaPayload;
import io.github.jimbozoomer.jugcraft.guide.encyclopedia.ProgressionRoutes;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

/**
 * The route maps in the real Encyclopedia: the key opens it, the Technology route's Map tab shows every step as a tile,
 * the keyboard selects a step, Enter follows its item to the existing handbook page and Esc comes back to the same step;
 * Live recipe opens the route page at that step and its button returns to the map; the Magic route's Map follows an
 * item to its Encyclopedia lesson and back; and at the smallest GUI (320x240) every control stays on the screen and every
 * tile on the map's page.
 */
public final class EncyclopediaProgressionClientGameTests implements FabricClientGameTest {
	private static EncyclopediaRoutesScreen routes(Minecraft client) {
		return (EncyclopediaRoutesScreen) client.gui.screen();
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}

	private static void shot(ClientGameTestContext context, String name) {
		context.runOnClient(client -> client.gui.toastManager().clear());
		context.waitTicks(3);
		context.takeScreenshot(name);
	}

	/** The Encyclopedia screen's topic that starts with the page's {@code step}-th step row (its own splitting rule). */
	private static int sectionOf(String page, int step) {
		EncyclopediaPayload.Page found = EncyclopediaClient.navigator().pages().stream().filter(p -> p.id().equals(page)).findFirst().orElseThrow();
		List<EncyclopediaPayload.Row> starts = new ArrayList<>();
		boolean open = false;
		for (EncyclopediaPayload.Row row : found.rows()) {
			boolean split = row.kindOf() == EncyclopediaPayload.Kind.HEADING || row.kindOf() == EncyclopediaPayload.Kind.STEP;
			if (!open || split) {
				starts.add(row);
				open = true;
			}
		}
		int seen = 0;
		for (int i = 0; i < starts.size(); i++) {
			if (starts.get(i).kindOf() == EncyclopediaPayload.Kind.STEP && seen++ == step) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext world = context.worldBuilder().create()) {
			world.getServer().runCommand("gamemode survival @a");
			world.getConnection().waitForChunksRender();
			ProgressionRoutes.Route technology = ProgressionRoutes.byId("technology");
			ProgressionRoutes.Route magic = ProgressionRoutes.byId("magic");
			check(technology != null && magic != null, "Both route maps are on the client");

			context.getInput().pressKey(EncyclopediaClient.key());
			context.waitForScreen(EncyclopediaScreen.class);
			int requested = context.computeOnClient(client -> EncyclopediaClient.sequence());
			context.waitFor(client -> EncyclopediaClient.navigator().applied() >= requested
					&& EncyclopediaClient.navigator().pages().stream().anyMatch(page -> page.id().equals("route_technology"))
					&& EncyclopediaClient.navigator().pages().stream().anyMatch(page -> page.id().equals("route_magic")));
			context.clickScreenButton("ui.jugcraft.contents");
			context.waitTicks(2);

			// Technology: the Map tab shows a tile for every step.
			context.runOnClient(mc -> ((EncyclopediaScreen)mc.gui.screen()).pressChapter("technology"));
			context.waitForScreen(EncyclopediaRoutesScreen.class);
			context.clickScreenButton("ui.jugcraft.book.route_map");
			context.waitFor(client -> routes(client).mapShown());
			int tiles = context.computeOnClient(client -> routes(client).mapNodes().size());
			check(tiles == technology.steps().size(), "Every technology step is a tile: " + tiles);
			shot(context, "encyclopedia_route_map_technology");

			// The keyboard selects the second step (smelting tin); Enter follows it to its handbook page; Esc comes back.
			context.getInput().pressKey(InputConstants.KEY_DOWN);
			context.getInput().pressKey(InputConstants.KEY_DOWN);
			context.waitTicks(2);
			String selected = context.computeOnClient(client -> routes(client).selectedStep());
			check(selected.equals(technology.steps().get(1).id()), "Down twice selects the second step: " + selected);
			boolean live = context.computeOnClient(client -> client.gui.screen().children().stream().anyMatch(child -> child instanceof Button button
					&& button.getMessage().getString().equals(Component.translatable("ui.jugcraft.book.recipe_page").getString()) && button.active));
			check(live, "A made step offers this server's live recipe");
			shot(context, "encyclopedia_route_map_step");
			check(technology.steps().get(1).entry().type().equals("handbook"), "The tin ingot's entry is a handbook page");
			context.getInput().pressKey(InputConstants.KEY_RETURN);
			context.waitForScreen(HandbookScreen.class);
			shot(context, "encyclopedia_route_map_handbook_entry");
			context.getInput().pressKey(InputConstants.KEY_ESCAPE);
			context.waitForScreen(EncyclopediaRoutesScreen.class);
			check(context.computeOnClient(client -> routes(client).mapShown() && routes(client).route() == 0
					&& routes(client).selectedStep().equals(selected)), "Esc in the handbook returns to the same step on the map");

			// Live recipe: the route page opens at that step, and its button returns to the map.
			context.clickScreenButton("ui.jugcraft.book.recipe_page");
			context.waitForScreen(EncyclopediaScreen.class);
			context.waitFor(client -> ((EncyclopediaScreen) client.gui.screen()).page().equals("route_technology"));
			context.waitTicks(3);
			int wanted = context.computeOnClient(client -> sectionOf("route_technology", 1));
			int section = context.computeOnClient(client -> ((EncyclopediaScreen) client.gui.screen()).section());
			check(wanted > 0 && section == wanted, "The live page opens at the step's topic: " + section + " / " + wanted);
			shot(context, "encyclopedia_route_live_recipe");
			context.clickScreenButton("ui.jugcraft.book.back_to_route");
			context.waitForScreen(EncyclopediaRoutesScreen.class);
			check(context.computeOnClient(client -> routes(client).mapShown() && routes(client).selectedStep().equals(selected)),
					"The back button returns to the map at the same step");

			// Magic: its own map; an item's Encyclopedia lesson and back.
			context.getInput().pressKey(InputConstants.KEY_BACKSPACE);
			context.waitFor(client -> routes(client).route() < 0);
			context.runOnClient(client -> routes(client).showMap(1));
			context.waitFor(client -> routes(client).mapShown() && routes(client).mapNodes().size() == magic.steps().size());
			shot(context, "encyclopedia_route_map_magic");
			int scale = indexOf(magic, "assayers_scale");
			check(magic.steps().get(scale).entry().type().equals("page"), "The Assayer's Scale opens an Encyclopedia lesson");
			context.runOnClient(client -> routes(client).selectStep(scale));
			context.waitTicks(2);
			context.clickScreenButton("ui.jugcraft.book.open_entry");
			context.waitForScreen(EncyclopediaScreen.class);
			String lesson = magic.steps().get(scale).entry().target();
			context.waitFor(client -> ((EncyclopediaScreen) client.gui.screen()).page().equals(lesson));
			shot(context, "encyclopedia_route_map_magic_entry");
			context.clickScreenButton("ui.jugcraft.book.back_to_route");
			context.waitForScreen(EncyclopediaRoutesScreen.class);
			check(context.computeOnClient(client -> routes(client).mapShown() && routes(client).route() == 1
					&& routes(client).selectedStep().equals("assayers_scale")), "The lesson's back button returns to the magic map");

			// The smallest GUI: 640x480 at scale 2 is 320x240.
			int guiScale = context.computeOnClient(client -> client.options.guiScale().get());
			context.getInput().resizeWindow(640, 480);
			context.runOnClient(client -> {
				client.options.guiScale().set(2);
				client.resizeGui();
			});
			context.waitTicks(10);
			context.runOnClient(client -> routes(client).selectStep(1));
			context.waitTicks(3);
			String bounds = context.computeOnClient(client -> {
				int width = client.getWindow().getGuiScaledWidth();
				int height = client.getWindow().getGuiScaledHeight();
				if (width != 320 || height != 240) {
					return "GUI is " + width + "x" + height;
				}
				EncyclopediaRoutesScreen screen = routes(client);
				for (GuiEventListener child : screen.children()) {
					if (child instanceof AbstractWidget widget && (widget.getX() < 0 || widget.getY() < 0
							|| widget.getX() + widget.getWidth() > width || widget.getY() + widget.getHeight() > height)) {
						return "control off screen: " + widget.getMessage().getString() + " " + widget.getRectangle();
					}
				}
				for (Button tile : screen.mapNodes()) {
					if (tile.getX() < 0 || tile.getX() + tile.getWidth() > width / 2) {
						return "tile off the map's page: " + tile.getMessage().getString() + " " + tile.getRectangle();
					}
				}
				return screen.mapNodes().isEmpty() ? "no tiles" : "";
			});
			check(bounds.isEmpty(), "Every control fits at 320x240: " + bounds);
			shot(context, "encyclopedia_route_map_320x240");
			context.runOnClient(client -> {
				client.options.guiScale().set(guiScale);
				client.resizeGui();
			});
			context.setScreen(() -> null);
		}
	}

	private static int indexOf(ProgressionRoutes.Route route, String id) {
		List<ProgressionRoutes.Step> steps = route.steps();
		for (int i = 0; i < steps.size(); i++) {
			if (steps.get(i).id().equals(id)) {
				return i;
			}
		}
		throw new AssertionError("No step " + id + " on the " + route.id() + " route");
	}
}
