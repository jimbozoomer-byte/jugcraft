package io.github.jimbozoomer.jugcraft.test;

import com.geckolib.renderer.GeoEntityRenderer;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.client.PactClient;
import io.github.jimbozoomer.jugcraft.client.PactScreen;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceProgress;
import io.github.jimbozoomer.jugcraft.concordance.rules.ResearchState;
import io.github.jimbozoomer.jugcraft.concordance.sky.AstralClaims;
import io.github.jimbozoomer.jugcraft.concordance.sky.AstrolabeItem;
import io.github.jimbozoomer.jugcraft.concordance.sky.Sky;
import io.github.jimbozoomer.jugcraft.concordance.spirits.AgreementHolder;
import io.github.jimbozoomer.jugcraft.concordance.spirits.BoundWills;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactRequestPayload;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactStatePayload;
import io.github.jimbozoomer.jugcraft.concordance.worker.Pacts;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerConnection;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Concordance Chunk D, D-04 (unit 2): the pact perch's screen in a real client, on the game's own dedicated server, and
 * Stolas's provisional body. One client. Results are logged with the prefix "[pact-check]". The player is in survival, so
 * what is spent is spent.
 * <ul>
 * <li><b>The conversation</b>: with Stolas's Regard understood and two patterns observed, the owner's perch is used; Stolas
 * arrives and speaks his three beats, then his terms, reached by Enter alone, the keyboard on their first line, never on
 * Accept ({@code jugcraft_pact_conversation}, {@code jugcraft_pact_terms}). Every term is there; nothing is recorded.</li>
 * <li><b>Sizes</b>: the terms and a beat at GUI scale 4 in a 1920x1080 window and in a 640x360 window; every control on
 * screen, every reachable control with words for the narrator.</li>
 * <li><b>Acceptance by keyboard</b>: Accept without the compact's materials is refused as cannot_afford and spends nothing
 * ({@code jugcraft_pact_refused}); given them, Accept seals once and spends once ({@code jugcraft_pact_kept}).</li>
 * <li><b>His body</b>: the screen closed and the pact kept; Stolas on his perch drawn by GeckoLib ({@code jugcraft_pact_stolas}).</li>
 * </ul>
 * The perch is opened by the server's use of it (no aimed click), and each step that opens it waits for a menu newer than
 * the one open before, with its first state. The narrator is not heard, and one client is not two.
 */
public class ConcordancePactClientGameTests implements FabricClientGameTest {
	private static final int WAIT = 200;
	private static final String STOLAS = "jugcraft:stolas";
	private static final String PERCH = "jugcraft:pact_perch";
	private static final String REGARD = "jugcraft:stolas_regard";
	private static final String BINDING_ARTS = "jugcraft:binding_arts";
	private static final String FIRST_LIGHT = "jugcraft:first_light";
	private static final String FULL_MOON = "jugcraft:full_moon";
	private static final String LANTERN_STAR = "jugcraft:lantern_star";

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestDedicatedServerContext server = context.worldBuilder().createServer()) {
			try (TestDedicatedServerConnection connection = server.connect()) {
				connection.waitForChunksRender();
				server.runCommand("time set noon");
				server.runCommand("weather clear");
				server.runCommand("gamerule minecraft:spawn_mobs false");
				server.runCommand("gamemode survival @a");
				context.waitTicks(20);
				BlockPos stand = context.computeOnClient(client -> client.player.blockPosition());
				BlockPos perch = stand.south(2);
				server.runOnServer(minecraft -> prepare(minecraft, stand, perch));
				server.runCommand(String.format(Locale.ROOT, "tp @a %.1f %d %.1f 0 0", stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5));
				context.waitTicks(10);
				connection.waitForChunksRender();
				int scale = context.computeOnClient(client -> client.options.guiScale().get());
				context.runOnClient(client -> {
					client.options.guiScale().set(2);
					client.resizeGui();
				});
				conversation(context, server, perch);
				sizes(context);
				acceptance(context, server);
				body(context, server, connection, perch);
				context.runOnClient(client -> {
					client.options.guiScale().set(scale);
					client.resizeGui();
				});
			}
		}
	}

	/** Stolas's Regard understood, two patterns observed, a full astrolabe in hand, the owner's perch two blocks south. */
	private static void prepare(MinecraftServer minecraft, BlockPos stand, BlockPos perch) {
		check(BuiltInRegistries.BLOCK.containsKey(Identifier.parse(PERCH)) && BuiltInRegistries.ENTITY_TYPE.containsKey(Identifier.parse(STOLAS)),
				"The pact perch and Stolas are registered");
		ServerPlayer player = player(minecraft);
		ServerLevel level = minecraft.overworld();
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -1; dz <= 4; dz++) {
				level.setBlockAndUpdate(stand.offset(dx, -1, dz), Blocks.STONE.defaultBlockState());
				for (int dy = 0; dy <= 3; dy++) {
					level.setBlockAndUpdate(stand.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
				}
			}
		}
		level.setBlockAndUpdate(perch, BuiltInRegistries.BLOCK.getValue(Identifier.parse(PERCH)).defaultBlockState());
		check(level.getBlockEntity(perch) instanceof AgreementHolder.Holds, "The perch keeps its pact through the shared holder (D-02)");
		((AgreementHolder.Holds) level.getBlockEntity(perch)).holder().setOwner(player.getUUID());
		ConcordanceProgress.grant(player, FIRST_LIGHT, ResearchState.UNDERSTOOD);
		ConcordanceProgress.grant(player, BINDING_ARTS, ResearchState.UNDERSTOOD);
		ConcordanceProgress.grant(player, REGARD, ResearchState.UNDERSTOOD);
		AstralClaims claims = AstralClaims.of(minecraft);
		claims.observe(player.getUUID(), FULL_MOON, level.getGameTime());
		claims.observe(player.getUUID(), LANTERN_STAR, level.getGameTime());
		player.getInventory().clearContent();
		ItemStack astrolabe = new ItemStack(Sky.ASTROLABE);
		AstrolabeItem.setCharge(astrolabe, AstrolabeItem.CAPACITY);
		player.getInventory().setItem(0, astrolabe);
	}

	private static void conversation(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch) {
		open(context, server, perch);
		context.waitFor(client -> PactClient.state() != null && PactClient.state().mode() == PactStatePayload.NEGOTIATE
				&& stolas(client, perch) != null, WAIT);
		String shown = context.computeOnClient(client -> describe(PactClient.state()));
		Jugcraft.LOGGER.info("[pact-check] invited: {}", shown);
		check(context.computeOnClient(client -> PactClient.state().beats().size() == 3 && PactClient.state().notes().isEmpty()
				&& PactClient.state().offers(PactRequestPayload.ACCEPT)
				&& PactClient.state().terms().stream().map(PactStatePayload.Term::key).toList().equals(PactStatePayload.TERMS)),
				"Stolas speaks three beats, nothing stands in the way, and his terms name every term, in order: " + shown);
		fits(context, "a beat at GUI scale 2");
		named(context);
		shot(context, "jugcraft_pact_conversation");
		tabTo(context, "next");
		for (int beat = 0; beat < 3; beat++) {
			context.getInput().pressKey(InputConstants.KEY_RETURN);
			context.waitTicks(3);
		}
		check(context.computeOnClient(client -> screen(client).page() == PactScreen.TERMS_PAGE && "para:0".equals(screen(client).focusedKey())),
				"Enter three times reaches his terms, the keyboard on their first line, never on Accept");
		fits(context, "the terms at GUI scale 2");
		named(context);
		shot(context, "jugcraft_pact_terms");
		check(!server.computeOnServer(ConcordancePactClientGameTests::held), "Hearing his terms records nothing");
	}

	private static void sizes(ClientGameTestContext context) {
		int width = context.computeOnClient(client -> client.getWindow().getWidth());
		int height = context.computeOnClient(client -> client.getWindow().getHeight());
		context.getInput().resizeWindow(1920, 1080);
		context.runOnClient(client -> {
			client.options.guiScale().set(4);
			client.resizeGui();
		});
		context.waitTicks(10);
		int applied = context.computeOnClient(client -> client.getWindow().getGuiScale());
		check(applied == 4, "The high-scale shots are at GUI scale 4: " + applied);
		fits(context, "the terms at GUI scale 4");
		shot(context, "jugcraft_pact_terms_scale4");
		press(context, "back");
		fits(context, "a beat at GUI scale 4");
		shot(context, "jugcraft_pact_conversation_scale4");
		press(context, "next");
		context.runOnClient(client -> {
			client.options.guiScale().set(2);
			client.resizeGui();
		});
		context.getInput().resizeWindow(640, 360);
		context.waitTicks(10);
		fits(context, "the terms in a 640x360 window");
		shot(context, "jugcraft_pact_terms_small");
		press(context, "back");
		fits(context, "a beat in a 640x360 window");
		shot(context, "jugcraft_pact_conversation_small");
		press(context, "next");
		context.getInput().resizeWindow(width, height);
		context.waitTicks(10);
		check(context.computeOnClient(client -> screen(client).page() == PactScreen.TERMS_PAGE), "Back on his terms");
	}

	private static void acceptance(ClientGameTestContext context, TestDedicatedServerContext server) {
		tabTo(context, "accept");
		PactStatePayload.Result refused = answer(context);
		Jugcraft.LOGGER.info("[pact-check] Accept without the compact's materials: {} ({})", refused.reason(), refused.message().getString());
		check("cannot_afford".equals(refused.reason()) && !refused.done() && context.computeOnClient(client -> PactClient.state().mode()) == PactStatePayload.NEGOTIATE,
				"Without the compact's materials Accept is refused with its reason: " + refused.reason());
		check(!server.computeOnServer(ConcordancePactClientGameTests::held)
				&& server.computeOnServer(ConcordancePactClientGameTests::charge) == AstrolabeItem.CAPACITY, "A refusal records and spends nothing");
		shot(context, "jugcraft_pact_refused");
		List<String> cost = context.computeOnClient(client -> PactClient.state().compact().stream()
				.map(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()) + " " + stack.getCount()).toList());
		int resonance = context.computeOnClient(client -> PactClient.state().resonance());
		Jugcraft.LOGGER.info("[pact-check] the Open Hand Compact: {} and {} resonance", cost, resonance);
		check(!cost.isEmpty() && resonance > 0 && resonance <= AstrolabeItem.CAPACITY, "The compact names its materials and resonance (D-S13)");
		server.runOnServer(minecraft -> cost.forEach(line -> player(minecraft).getInventory().add(stack(line))));
		context.waitTicks(5);
		tabTo(context, "accept");
		PactStatePayload.Result sealed = answer(context);
		check(sealed.done() && "sealed".equals(sealed.reason()) && context.computeOnClient(client -> PactClient.state().mode()) == PactStatePayload.KEEP,
				"Accepting with the compact seals the pact: " + sealed.reason());
		String spent = server.computeOnServer(minecraft -> held(minecraft) + " " + charge(minecraft) + " "
				+ cost.stream().mapToInt(line -> player(minecraft).getInventory().countItem(stack(line).getItem())).sum());
		Jugcraft.LOGGER.info("[pact-check] sealed: held, astrolabe, materials left = {}", spent);
		check(spent.equals("true " + (AstrolabeItem.CAPACITY - resonance) + " 0"), "Sealed once, materials and resonance spent once: " + spent);
		fits(context, "the kept pact at GUI scale 2");
		named(context);
		shot(context, "jugcraft_pact_kept");
	}

	private static void body(ClientGameTestContext context, TestDedicatedServerContext server, TestDedicatedServerConnection connection,
			BlockPos perch) {
		context.getInput().pressKey(InputConstants.KEY_ESCAPE);
		context.waitForScreen(null);
		check(server.computeOnServer(ConcordancePactClientGameTests::held), "Closing the screen keeps the pact");
		check(context.computeOnClient(client -> {
			Entity stolas = stolas(client, perch);
			return stolas != null && client.getEntityRenderDispatcher().getRenderer(stolas) instanceof GeoEntityRenderer<?, ?>;
		}), "Stolas stays on his perch, drawn by GeckoLib (his provisional body, D-S5)");
		boolean hidden = context.computeOnClient(client -> client.gui.hud.isHidden());
		context.runOnClient(client -> {
			if (!client.gui.hud.isHidden()) {
				client.gui.hud.toggle();
			}
		});
		server.runCommand(String.format(Locale.ROOT, "tp @a %.1f %d %.1f 0 0", perch.getX() + 0.5, perch.getY(), perch.getZ() - 2.5));
		context.waitTicks(30);
		connection.waitForChunksRender();
		server.runCommand("tick freeze");
		shot(context, "jugcraft_pact_stolas");
		server.runCommand("tick unfreeze");
		context.runOnClient(client -> {
			if (client.gui.hud.isHidden() != hidden) {
				client.gui.hud.toggle();
			}
		});
	}

	/**
	 * The perch used by its owner on the server (its own interaction; no click is aimed), and its menu's first state. It waits
	 * for a menu newer than the one open before, so a use the server refused (its rate gate) fails here, never passing
	 * against the screen and state already shown.
	 */
	private static void open(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch) {
		int before = context.computeOnClient(client -> client.player.containerMenu.containerId);
		server.runOnServer(minecraft -> {
			ServerLevel level = minecraft.overworld();
			level.getBlockState(perch).useWithoutItem(level, player(minecraft), new BlockHitResult(Vec3.atCenterOf(perch), Direction.NORTH, perch, false));
		});
		context.waitForScreen(PactScreen.class);
		context.waitFor(client -> client.gui.screen() instanceof PactScreen screen && screen.getMenu().containerId != before
				&& PactClient.state() != null, WAIT);
	}

	private static void press(ClientGameTestContext context, String key) {
		tabTo(context, key);
		context.getInput().pressKey(InputConstants.KEY_RETURN);
		context.waitTicks(3);
	}

	private static void tabTo(ClientGameTestContext context, String key) {
		for (int i = 0; i < 48; i++) {
			if (key.equals(context.computeOnClient(client -> client.gui.screen() instanceof PactScreen screen ? screen.focusedKey() : ""))) {
				return;
			}
			context.getInput().pressKey(InputConstants.KEY_TAB);
			context.waitTicks(1);
		}
		throw new AssertionError("Tab never reached " + key);
	}

	/** Enter on the focused control, then the server's answer to it (its own sequence; never an earlier answer). */
	private static PactStatePayload.Result answer(ClientGameTestContext context) {
		int before = context.computeOnClient(client -> PactClient.lastAnswer());
		context.getInput().pressKey(InputConstants.KEY_RETURN);
		context.waitFor(client -> PactClient.lastAnswer() > before && !PactClient.waiting() && PactClient.state() != null, WAIT);
		return context.computeOnClient(client -> PactClient.state().result().orElseThrow());
	}

	private static void fits(ClientGameTestContext context, String where) {
		List<String> outside = context.computeOnClient(client -> screen(client).outside());
		check(outside.isEmpty(), "Every control of the pact screen is on screen (" + where + "): " + outside);
	}

	private static void named(ClientGameTestContext context) {
		List<String> blank = context.computeOnClient(client -> blank(client.gui.screen()));
		check(blank.isEmpty(), "Every reachable control has words for the narrator: " + blank);
	}

	private static List<String> blank(GuiEventListener parent) {
		List<String> out = new ArrayList<>();
		if (parent instanceof ContainerEventHandler container) {
			for (GuiEventListener child : container.children()) {
				out.addAll(blank(child));
			}
		} else if (parent instanceof AbstractWidget widget && widget.active && widget.getMessage().getString().isBlank()) {
			out.add(widget.getClass().getSimpleName() + " at " + widget.getX() + "," + widget.getY());
		}
		return out;
	}

	private static void shot(ClientGameTestContext context, String name) {
		context.runOnClient(client -> client.gui.toastManager().clear());
		context.waitTicks(2);
		context.takeScreenshot(name);
	}

	private static PactScreen screen(Minecraft client) {
		return (PactScreen) client.gui.screen();
	}

	private static @Nullable Entity stolas(Minecraft client, BlockPos perch) {
		EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(STOLAS));
		List<Entity> found = client.level.getEntities((Entity) null, new AABB(perch).inflate(4), entity -> entity.getType() == type);
		return found.isEmpty() ? null : found.getFirst();
	}

	private static String describe(@Nullable PactStatePayload state) {
		return state == null ? "no state" : "mode " + state.mode() + ", " + state.beats().size() + " beats, terms "
				+ state.terms().stream().map(PactStatePayload.Term::key).toList() + ", notes "
				+ state.notes().stream().map(Component::getString).toList();
	}

	private static ItemStack stack(String line) {
		String[] part = line.split(" ");
		return new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(part[0])), Integer.parseInt(part[1]));
	}

	private static ServerPlayer player(MinecraftServer minecraft) {
		return minecraft.getPlayerList().getPlayers().getFirst();
	}

	private static boolean held(MinecraftServer minecraft) {
		return Pacts.holds(BoundWills.of(minecraft).ledger(), player(minecraft).getUUID(), STOLAS);
	}

	private static int charge(MinecraftServer minecraft) {
		Inventory inventory = player(minecraft).getInventory();
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			if (inventory.getItem(slot).is(Sky.ASTROLABE)) {
				return AstrolabeItem.charge(inventory.getItem(slot));
			}
		}
		return -1;
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
