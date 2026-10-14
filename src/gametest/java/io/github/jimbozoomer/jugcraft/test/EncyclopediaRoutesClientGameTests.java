package io.github.jimbozoomer.jugcraft.test;

import com.klikli_dev.modonomicon.client.gui.book.node.BookParentNodeScreen;
import com.klikli_dev.modonomicon.data.BookDataManager;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.client.EncyclopediaClient;
import io.github.jimbozoomer.jugcraft.client.EncyclopediaScreen;
import java.util.List;
import net.minecraft.client.InputType;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import io.github.jimbozoomer.jugcraft.client.EncyclopediaRoutesScreen;
import io.github.jimbozoomer.jugcraft.client.HandbookScreen;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

/** Both guide routes open from the shared encyclopedia and preserve handbook/back navigation. */
public class EncyclopediaRoutesClientGameTests implements FabricClientGameTest {
	/** Use the pinned game's input codes (26.3 uses SDL, not GLFW). */
	private static final int ESCAPE = InputConstants.KEY_ESCAPE;

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			singleplayer.getServer().runCommand("gamemode survival @a");
			singleplayer.getConnection().waitForChunksRender();

			// The key, from the game.
			context.getInput().pressKey(EncyclopediaClient.key());
			context.waitForScreen(EncyclopediaScreen.class);
			context.runOnClient(client -> ((Button) ((EncyclopediaScreen) client.gui.screen())
					.find(Component.translatable("screen.jugcraft.routes.open")))
					.onPress(new MouseButtonInfo(InputConstants.MOUSE_BUTTON_LEFT, 0)));
			context.waitForScreen(EncyclopediaRoutesScreen.class);
			context.waitTicks(2);
			context.takeScreenshot("jugcraft_encyclopedia_home");
			// Concordance Chunk B (B-11): each route card shows its whole overview and its whole link. Run 38519234934 cut
			// the Magic card's link to "Open Magic: the Arcane Concorda…" and both overviews at this size (CI's 427×240 GUI).
			List<EncyclopediaRoutesScreen.HomeCard> home = home(context, "on opening");
			check(home.getFirst().top() == 0, "The home should open at the top of its cards: " + home);
			// Tab reaches a card; a card taller than the room shows from its top, not its foot.
			InputType input = context.computeOnClient(client -> client.getLastInputType());
			context.runOnClient(client -> client.setLastInputType(InputType.KEYBOARD_TAB));
			context.getInput().pressKey(InputConstants.KEY_TAB);
			context.waitTicks(1);
			home = home(context, "after Tab");
			check(home.stream().anyMatch(card -> card.focused() && card.top() == 0),
					"Tab should reach a card and show it from its top: " + home);
			// The wheel scrolls to the cards' foot, where every link is in view.
			context.runOnClient(client -> {
				EncyclopediaRoutesScreen screen = (EncyclopediaRoutesScreen) client.gui.screen();
				screen.mouseScrolled(screen.width / 2.0, screen.height - 40.0, 0.0, -100.0);
			});
			context.waitTicks(2);
			context.takeScreenshot("jugcraft_encyclopedia_home_end");
			home = home(context, "scrolled to the end");
			EncyclopediaRoutesScreen.HomeCard last = home.getLast();
			check(last.top() + last.height() <= last.view(), "Scrolled to the end, the last card's foot should be in view: " + home);
			// A larger GUI (scale 1, the whole window), where the cards fit unscrolled.
			int scale = context.computeOnClient(client -> client.options.guiScale().get());
			context.runOnClient(client -> {
				client.options.guiScale().set(1);
				client.resizeGui();
			});
			context.waitTicks(2);
			home(context, "at GUI scale 1");
			context.takeScreenshot("jugcraft_encyclopedia_home_scale1");
			context.runOnClient(client -> {
				client.options.guiScale().set(scale);
				client.resizeGui();
				client.setLastInputType(input);
			});
			context.waitTicks(2);

			// Technology: a milestone opens the handbook at its stage, which closes back to the route.
			context.runOnClient(client -> ((EncyclopediaRoutesScreen) client.gui.screen()).showRoute(0));
			context.waitTicks(2);
			context.takeScreenshot("jugcraft_encyclopedia_technology");
			context.getInput().pressKey(InputConstants.KEY_DOWN);
			context.getInput().pressKey(InputConstants.KEY_RETURN);
			context.waitForScreen(HandbookScreen.class);
			context.waitTicks(2);
			context.takeScreenshot("jugcraft_encyclopedia_handbook");
			context.getInput().pressKey(ESCAPE);
			context.waitForScreen(EncyclopediaRoutesScreen.class);
			int route = context.computeOnClient(client -> ((EncyclopediaRoutesScreen) client.gui.screen()).route());
			if (route != 0) {
				throw new AssertionError("Esc in the handbook should return to the technology route, not " + route);
			}

			// Magic: the codex is loaded, and a milestone opens it at its category.
			context.getInput().pressKey(InputConstants.KEY_BACKSPACE);
			context.waitFor(client -> client.gui.screen() instanceof EncyclopediaRoutesScreen screen && screen.route() < 0);
			boolean codex = context.computeOnClient(client -> BookDataManager.get().getBook(Jugcraft.id("arcane_concordance")) != null);
			if (!codex) {
				throw new AssertionError("The Arcane Concordance codex is not loaded on the client");
			}
			context.runOnClient(client -> ((EncyclopediaRoutesScreen) client.gui.screen()).showRoute(1));
			context.waitTicks(2);
			context.takeScreenshot("jugcraft_encyclopedia_magic");
			context.getInput().pressKey(InputConstants.KEY_DOWN);
			context.getInput().pressKey(InputConstants.KEY_RETURN);
			context.waitForScreen(BookParentNodeScreen.class);
			context.waitTicks(5);
			context.takeScreenshot("jugcraft_encyclopedia_codex");
			context.setScreen(() -> null);

			// The guide directory is a child of the live encyclopedia; closing returns there.
			context.getInput().pressKey(EncyclopediaClient.key());
			context.waitForScreen(EncyclopediaScreen.class);
			context.runOnClient(client -> ((Button) ((EncyclopediaScreen) client.gui.screen())
					.find(Component.translatable("screen.jugcraft.routes.open")))
					.onPress(new MouseButtonInfo(InputConstants.MOUSE_BUTTON_LEFT, 0)));
			context.waitForScreen(EncyclopediaRoutesScreen.class);
			context.getInput().pressKey(ESCAPE);
			context.waitForScreen(EncyclopediaScreen.class);
			context.getInput().pressKey(ESCAPE);
			context.waitForScreen(null);
		}
	}

	/** The home's cards as laid out, logged; each shown whole, with its foot within the body's reach. */
	private static List<EncyclopediaRoutesScreen.HomeCard> home(ClientGameTestContext context, String when) {
		String size = context.computeOnClient(client -> client.gui.screen().width + "x" + client.gui.screen().height);
		List<EncyclopediaRoutesScreen.HomeCard> cards = context.computeOnClient(client -> ((EncyclopediaRoutesScreen) client.gui.screen()).homeCards());
		Jugcraft.LOGGER.info("[encyclopedia-check] the home at {} {}: {}", size, when, cards);
		check(cards.size() == 2, "The home at " + size + " should show the two route cards: " + cards);
		for (EncyclopediaRoutesScreen.HomeCard card : cards) {
			String at = "At " + size + " " + when + ", the " + card.title() + " card ";
			check(card.lines() <= card.room(), at + "lays out " + card.lines() + " lines of overview and link in room for " + card.room());
			String whole = Component.translatable("screen.jugcraft.routes.open_route", card.title()).getString() + " ›";
			check(String.join("", card.link()).replace(" ", "").equals(whole.replace(" ", "")), at + "should show its whole link: " + card.link());
			check(card.titleWhole(), at + "should show its whole title");
			check(card.foot() <= card.reach(), at + "should scroll its foot (" + card.foot() + ") into reach (" + card.reach() + ")");
		}
		return cards;
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
