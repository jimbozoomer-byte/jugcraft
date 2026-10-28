package io.github.jimbozoomer.jugcraft.test;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceProgress;
import io.github.jimbozoomer.jugcraft.concordance.CrucibleBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.JugcraftConcordance;
import io.github.jimbozoomer.jugcraft.concordance.RateGate;
import io.github.jimbozoomer.jugcraft.concordance.ResearchNotes;
import io.github.jimbozoomer.jugcraft.concordance.ResearchNotesItem;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Band;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Formula;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Operation;
import io.github.jimbozoomer.jugcraft.concordance.garden.Garden;
import io.github.jimbozoomer.jugcraft.concordance.rules.ResearchState;
import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

/**
 * Gloam Ink (C-D6) in a real client: the ink's icon (the owner's Common Ink in the Gloamcap's violet) is among the client's
 * resources; in the survival inventory, a written original, three blank sheets and three Gloam Inks in the player's own
 * crafting grid show a fair copy in the result slot, as the server made it ({@code jugcraft_gloam_ink_fair_copy}, and at
 * GUI scale 4, {@code jugcraft_gloam_ink_fair_copy_scale4}); the copy's tooltip names its writer and says it is a fair
 * copy, the original's names its writer and does not, and the ink's says what it is for; and, beside the player, a
 * crucible over a campfire under a closed oak trapdoor following the worked Gloam Ink formula takes its first covered stir
 * hot ({@code jugcraft_gloam_ink_lidded_crucible}: the lid on the pot, for people to look at). The shots are for people
 * to look at; nothing here judges a picture. CI job {@code client}.
 */
public class ConcordanceGloamInkClientGameTests implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder()
				.adjustSettings(creator -> creator.setGameMode(WorldCreationUiState.SelectedGameMode.SURVIVAL)).create()) {
			singleplayer.getConnection().waitForChunksRender();
			TestServerContext server = singleplayer.getServer();
			server.runCommand("gamerule minecraft:send_command_feedback false");
			server.runCommand("time set noon");
			server.runCommand("weather clear");
			context.waitTicks(10);
			boolean texture = context.computeOnClient(client ->
					client.getResourceManager().getResource(Jugcraft.id("textures/item/gloam_ink.png")).isPresent());
			check(texture, "The client has no Gloam Ink icon (textures/item/gloam_ink.png)");

			server.runOnServer(minecraft -> lay(minecraft.getPlayerList().getPlayers().getFirst()));
			context.waitTicks(5);
			context.setScreen(() -> new InventoryScreen(Minecraft.getInstance().player));
			context.waitTicks(10);
			context.runOnClient(client -> client.gui.toastManager().clear());
			context.waitTicks(2);
			boolean inventory = context.computeOnClient(client -> client.gui.screen() instanceof InventoryScreen);
			ItemStack shown = context.computeOnClient(client ->
					client.player.inventoryMenu.getSlot(InventoryMenu.RESULT_SLOT).getItem().copy());
			context.takeScreenshot("jugcraft_gloam_ink_fair_copy");
			int scale = context.computeOnClient(client -> client.options.guiScale().get());
			int width = context.computeOnClient(client -> client.getWindow().getWidth());
			int height = context.computeOnClient(client -> client.getWindow().getHeight());
			// The test window is too small for GUI scale 4 (the game caps it), so it is widened first, as the pact tests do.
			context.getInput().resizeWindow(1920, 1080);
			context.runOnClient(client -> {
				client.options.guiScale().set(4);
				client.resizeGui();
				client.gui.toastManager().clear();
			});
			context.waitTicks(10);
			int applied = context.computeOnClient(client -> client.getWindow().getGuiScale());
			check(applied == 4, "The fair copy's scale-4 shot draws GUI scale " + applied);
			context.takeScreenshot("jugcraft_gloam_ink_fair_copy_scale4");
			context.runOnClient(client -> {
				client.options.guiScale().set(scale);
				client.resizeGui();
			});
			context.getInput().resizeWindow(width, height);
			context.waitTicks(10);
			List<String> copyLines = context.computeOnClient(client ->
					lines(client, client.player.inventoryMenu.getSlot(InventoryMenu.RESULT_SLOT).getItem()));
			List<String> originalLines = context.computeOnClient(client ->
					lines(client, client.player.inventoryMenu.getSlot(InventoryMenu.CRAFT_SLOT_START).getItem()));
			List<String> inkLines = context.computeOnClient(client ->
					lines(client, client.player.inventoryMenu.getSlot(InventoryMenu.CRAFT_SLOT_START + 2).getItem()));
			String fairCopy = context.computeOnClient(client -> Component.translatable("tooltip.jugcraft.research_notes.fair_copy").getString());
			String inkWords = context.computeOnClient(client -> Component.translatable("tooltip.jugcraft.gloam_ink").getString());
			String writer = context.computeOnClient(client -> Component.translatable("tooltip.jugcraft.research_notes.author",
					client.player.getName().getString()).getString());
			context.getInput().pressKey(InputConstants.KEY_ESCAPE);
			context.waitForScreen(null);
			ResearchNotes copy = shown.get(JugcraftConcordance.RESEARCH_NOTES);
			check(inventory, "The fair copy's shot shows another screen than the survival inventory");
			check(copy != null && copy.fairCopy(), "The client's result slot should show a fair copy, as the server made it: " + shown);
			check(copyLines.contains(fairCopy) && copyLines.contains(writer),
					"A copy's tooltip should name its writer and say it is a fair copy: " + copyLines);
			check(!originalLines.contains(fairCopy) && originalLines.contains(writer),
					"An original's tooltip should name its writer and not call it a copy: " + originalLines);
			check(inkLines.contains(inkWords), "Gloam Ink's tooltip should say what it is for: " + inkLines);

			// The lidded crucible (GI-P1): over a campfire, under a closed oak trapdoor, following the worked formula with its
			// water, Gloamcap, spider eye and bottles. Once its first covered stir is taken, the lid on the pot is photographed
			// for people to look at (the ladle against the lid's underside). The space above the lid is cleared, and the
			// campfire must give the crucible its 120 degrees, before the wait begins.
			BlockPos standing = context.computeOnClient(client -> client.player.blockPosition());
			BlockPos pot = standing.offset(0, 0, 3);
			String lidded = server.computeOnServer(minecraft -> {
				ServerLevel level = minecraft.overworld();
				level.setBlockAndUpdate(pot.above(2), Blocks.AIR.defaultBlockState());
				level.setBlockAndUpdate(pot.below(), Blocks.CAMPFIRE.defaultBlockState());
				level.setBlockAndUpdate(pot, JugcraftConcordance.CRUCIBLE.defaultBlockState());
				level.setBlockAndUpdate(pot.above(), Blocks.OAK_TRAPDOOR.defaultBlockState());
				if (!(level.getBlockEntity(pot) instanceof CrucibleBlockEntity crucible)) {
					return "no crucible";
				}
				crucible.follow(Formula.parse(ConcordanceGloamInkGameTests.GLOAM_INK)); // as the formula item sets it, without its checks
				try (Transaction transaction = Transaction.openOuter()) {
					crucible.water.insert(FluidVariant.of(Fluids.WATER), FluidConstants.BOTTLE * 3, transaction);
					transaction.commit();
				}
				crucible.setItem(0, new ItemStack(Garden.GLOAMCAP, 3));
				crucible.setItem(1, new ItemStack(Items.SPIDER_EYE));
				crucible.setItem(CrucibleBlockEntity.BOTTLE_SLOT, new ItemStack(Items.GLASS_BOTTLE, 3));
				if (crucible.below(level).target() != 120) {
					return "unheated: " + crucible.below(level).block();
				}
				return crucible.covered(level) ? "ok" : "no lid";
			});
			check("ok".equals(lidded), "The lidded crucible should be set up under its lid: " + lidded);
			for (int wait = 0; wait < 60 && !server.computeOnServer(minecraft -> minecraft.overworld().getBlockEntity(pot)
					instanceof CrucibleBlockEntity crucible && crucible.step() >= 6); wait++) {
				context.waitTicks(20);
			}
			Operation last = server.computeOnServer(minecraft -> minecraft.overworld().getBlockEntity(pot) instanceof CrucibleBlockEntity crucible
					&& !crucible.mixture().history().isEmpty() ? crucible.mixture().history().getLast() : null);
			check(new Operation.Stir(Band.HOT, true).equals(last), "Under its lid, the crucible should take its first covered stir hot: " + last);
			context.getInput().lookAt(pot);
			context.waitTicks(10);
			context.runOnClient(client -> client.gui.toastManager().clear());
			context.takeScreenshot("jugcraft_gloam_ink_lidded_crucible");
		}
	}

	/** An original sheet the player writes, three blank sheets and three Gloam Inks in their own two-by-two grid. */
	private static void lay(ServerPlayer player) {
		ConcordanceProgress.grant(player, "jugcraft:first_light", ResearchState.UNDERSTOOD);
		RateGate.forget(player.getUUID());
		player.getInventory().clearContent();
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(JugcraftConcordance.RESEARCH_NOTES_ITEM));
		ResearchNotesItem.write(player, InteractionHand.MAIN_HAND, player.getMainHandItem());
		ItemStack original = player.getMainHandItem().copy();
		player.getInventory().clearContent();
		player.inventoryMenu.getCraftSlots().setItem(0, original);
		player.inventoryMenu.getCraftSlots().setItem(1, new ItemStack(JugcraftConcordance.RESEARCH_NOTES_ITEM, 3));
		player.inventoryMenu.getCraftSlots().setItem(2, new ItemStack(JugcraftConcordance.GLOAM_INK, 3));
		player.inventoryMenu.broadcastChanges();
	}

	private static List<String> lines(Minecraft client, ItemStack stack) {
		return stack.getTooltipLines(Item.TooltipContext.of(client.level), client.player, TooltipFlag.NORMAL)
				.stream().map(Component::getString).toList();
	}

	private static void check(boolean ok, String message) {
		if (!ok) {
			throw new AssertionError(message);
		}
	}
}
