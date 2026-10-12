package io.github.jimbozoomer.jugcraft.test;

import com.klikli_dev.modonomicon.client.gui.book.node.BookParentNodeScreen;
import com.klikli_dev.modonomicon.data.BookDataManager;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.client.EncyclopediaClient;
import io.github.jimbozoomer.jugcraft.client.EncyclopediaScreen;
import io.github.jimbozoomer.jugcraft.client.HandbookScreen;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

/**
 * The Jugcraft Encyclopedia's first slice in a real client (docs/features/jugcraft-encyclopedia.md), with nothing in
 * hand, in survival:
 * <ul>
 * <li>the Encyclopedia key opens its home ({@code jugcraft_encyclopedia_home});</li>
 * <li>the technology route shows ({@code jugcraft_encyclopedia_technology}); Down then Enter opens its first milestone,
 * the handbook's Bronze Age page ({@code jugcraft_encyclopedia_handbook}), and Esc comes back to the route;</li>
 * <li>Backspace goes home; the magic route shows ({@code jugcraft_encyclopedia_magic}) with the codex loaded, and its
 * first milestone opens the Arcane Concordance codex without its book item ({@code jugcraft_encyclopedia_codex});</li>
 * <li>Esc closes the Encyclopedia to the game;</li>
 * <li>the survival inventory shows the book button ({@code jugcraft_encyclopedia_inventory}); it opens the Encyclopedia,
 * and Esc returns to the inventory.</li>
 * </ul>
 * CI job {@code client}.
 */
public class EncyclopediaClientGameTests implements FabricClientGameTest {
	/** GLFW's Escape key code. */
	private static final int ESCAPE = 256;

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			singleplayer.getServer().runCommand("gamemode survival @a");
			singleplayer.getConnection().waitForChunksRender();

			// The key, from the game.
			context.getInput().pressKey(EncyclopediaClient.key());
			context.waitForScreen(EncyclopediaScreen.class);
			context.waitTicks(2);
			context.takeScreenshot("jugcraft_encyclopedia_home");

			// Technology: a milestone opens the handbook at its stage, which closes back to the route.
			context.runOnClient(client -> ((EncyclopediaScreen) client.gui.screen()).showRoute(0));
			context.waitTicks(2);
			context.takeScreenshot("jugcraft_encyclopedia_technology");
			context.getInput().pressKey(InputConstants.KEY_DOWN);
			context.getInput().pressKey(InputConstants.KEY_RETURN);
			context.waitForScreen(HandbookScreen.class);
			context.waitTicks(2);
			context.takeScreenshot("jugcraft_encyclopedia_handbook");
			context.getInput().pressKey(ESCAPE);
			context.waitForScreen(EncyclopediaScreen.class);
			int route = context.computeOnClient(client -> ((EncyclopediaScreen) client.gui.screen()).route());
			if (route != 0) {
				throw new AssertionError("Esc in the handbook should return to the technology route, not " + route);
			}

			// Magic: the codex is loaded, and a milestone opens it at its category.
			context.getInput().pressKey(InputConstants.KEY_BACKSPACE);
			context.waitFor(client -> client.gui.screen() instanceof EncyclopediaScreen screen && screen.route() < 0);
			boolean codex = context.computeOnClient(client -> BookDataManager.get().getBook(Jugcraft.id("arcane_concordance")) != null);
			if (!codex) {
				throw new AssertionError("The Arcane Concordance codex is not loaded on the client");
			}
			context.runOnClient(client -> ((EncyclopediaScreen) client.gui.screen()).showRoute(1));
			context.waitTicks(2);
			context.takeScreenshot("jugcraft_encyclopedia_magic");
			context.getInput().pressKey(InputConstants.KEY_DOWN);
			context.getInput().pressKey(InputConstants.KEY_RETURN);
			context.waitForScreen(BookParentNodeScreen.class);
			context.waitTicks(5);
			context.takeScreenshot("jugcraft_encyclopedia_codex");
			context.setScreen(() -> null);

			// Esc closes it to the game.
			context.getInput().pressKey(EncyclopediaClient.key());
			context.waitForScreen(EncyclopediaScreen.class);
			context.getInput().pressKey(ESCAPE);
			context.waitForScreen(null);

			// The inventory's book button opens it, and Esc goes back to the inventory.
			context.getInput().pressKey(options -> options.keyInventory);
			context.waitForScreen(InventoryScreen.class);
			context.waitTicks(2);
			context.takeScreenshot("jugcraft_encyclopedia_inventory");
			context.clickScreenButton("screen.jugcraft.encyclopedia.open");
			context.waitForScreen(EncyclopediaScreen.class);
			context.getInput().pressKey(ESCAPE);
			context.waitForScreen(InventoryScreen.class);
			context.setScreen(() -> null);
		}
	}
}
