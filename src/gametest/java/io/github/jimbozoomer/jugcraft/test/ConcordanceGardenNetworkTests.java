package io.github.jimbozoomer.jugcraft.test;

import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.client.ConcordanceClientOptions;
import io.github.jimbozoomer.jugcraft.client.MagicScreen;
import io.github.jimbozoomer.jugcraft.client.PactClient;
import io.github.jimbozoomer.jugcraft.client.PactScreen;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceProgress;
import io.github.jimbozoomer.jugcraft.concordance.garden.Garden;
import io.github.jimbozoomer.jugcraft.concordance.garden.VerdantBedBlock;
import io.github.jimbozoomer.jugcraft.concordance.garden.VerdantBedBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.rules.ResearchState;
import io.github.jimbozoomer.jugcraft.concordance.spirits.AgreementHolder;
import io.github.jimbozoomer.jugcraft.concordance.spirits.BoundWills;
import io.github.jimbozoomer.jugcraft.concordance.spirits.BuerEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.GardenPacts;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactMenu;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactPerchBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactRequestPayload;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactSpiritEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactStatePayload;
import io.github.jimbozoomer.jugcraft.concordance.spirits.Workers;
import io.github.jimbozoomer.jugcraft.concordance.worker.PactDefinition;
import io.github.jimbozoomer.jugcraft.concordance.worker.Pacts;
import io.github.jimbozoomer.jugcraft.concordance.worker.Status;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerConnection;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserWhiteListEntry;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Concordance garden cohort, GC-10 unit 7 (GC-S18, GC-P142): two client JVMs at one garden perch, on the pact network
 * test's recipe (ConcordancePactNetworkTests, D-09) and its launch, now shared (SecondClient, GC-P146). This client is the
 * holder, on the game's own dedicated server opened on 127.0.0.1 only, offline, room for two; a second client JVM
 * ("GardenPeer", its own temporary profile under owner-network-evidence/, whose peer.log the client shards and the opt-in
 * run upload) joins as a stranger with an awake bed of its own beside the holder's. Results are logged "[garden-network]".
 * <ul>
 * <li>The holder's Buer, sealed on the server without the screen (the peer's steps are the evidence, and both JVMs stay
 * light), eases one strain from the holder's bed and none from the stranger's; the stranger sees his work gesture and the
 * holder's bed eased on its own client while its own bed keeps its strain ({@code jugcraft_garden_peer_work}).</li>
 * <li>A think past the bed's window still eases nothing of the stranger's.</li>
 * <li>Used by the stranger, the kept perch says it is taken and offers no End; Buer turns to the stranger
 * ({@code jugcraft_garden_peer_faced}); the stranger's End, sent all the same, is refused as not the holder's
 * ({@code jugcraft_garden_peer_refused}); the holder's pact, escrow and Buer stay, and the stranger holds no record.</li>
 * </ul>
 * Light by design: three shots, no roster, one think pair. Two automated clients on one machine with offline names, no
 * people: not the manual two-account check (docs/TESTING.md). Written, not yet run anywhere.
 */
public final class ConcordanceGardenNetworkTests implements FabricClientGameTest {
	private static final String PEER = "jugcraft.gardenTestPeer";
	private static final String COORDINATION = "jugcraft.gardenTestCoord";
	private static final String PEER_NAME = "GardenPeer";
	private static final int WAIT = 200;
	/** Client ticks the other JVM may take for a step: it starts, joins and renders on its own. */
	private static final int PEER_WAIT = 2400;
	private static final long NOON = 6000L;
	private static final float FACING_TOLERANCE = 15.0F;
	private static final SecondClient CLIENT = new SecondClient(PEER, COORDINATION, "garden", PEER_NAME, ConcordanceGardenNetworkTests.class);

	@Override
	public void runTest(ClientGameTestContext context) {
		if (CLIENT.isPeer()) {
			peer(context);
			return;
		}
		Process peer = null;
		try {
			CLIENT.begin();
			Properties properties = new Properties();
			properties.setProperty("server-ip", "127.0.0.1");
			properties.setProperty("online-mode", "false");
			properties.setProperty("view-distance", "4");
			properties.setProperty("simulation-distance", "4");
			properties.setProperty("max-players", "2");
			try (TestDedicatedServerContext server = context.worldBuilder().adjustSettings(settings -> {
				settings.setWorldType(new WorldCreationUiState.WorldTypeEntry(settings.getSettings().worldgenLoadContext()
						.lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT)));
				settings.setGenerateStructures(false);
			}).createServer(properties)) {
				try (TestDedicatedServerConnection connection = server.connect()) {
					connection.waitForChunksRender();
					server.runCommand("time set noon");
					server.runCommand("weather clear");
					server.runCommand("gamerule minecraft:spawn_mobs false");
					server.runCommand("gamerule minecraft:random_tick_speed 0");
					context.waitTicks(20);
					BlockPos stand = context.computeOnClient(client -> client.player.blockPosition());
					BlockPos perch = stand.south(2);
					BlockPos holderBed = perch.west(2);
					BlockPos peerBed = perch.east(2);
					UUID holder = context.computeOnClient(client -> client.player.getUUID());
					server.runOnServer(minecraft -> prepare(minecraft, stand, perch, holderBed, holder));
					server.runOnServer(minecraft -> minecraft.getPlayerList().getWhiteList().add(new UserWhiteListEntry(NameAndId.createOffline(PEER_NAME))));
					InProcessServerConfigs.beforeAdditionalClient(server);
					// Main's models need more than 2 GB to bake (PeerClientStartup): the shared heap, and startup waited for
					// separately before the login deadline, as the other two-client tests do.
					peer = CLIENT.launch(server.computeOnServer(MinecraftServer::getPort), PeerClientStartup.HEAP_OPTION);
					Process joining = peer;
					PeerClientStartup.await(() -> context.waitTicks(1), joining, CLIENT.coordination());
					server.waitFor(minecraft -> {
						if (!joining.isAlive()) {
							throw CLIENT.failure("The second client exited before joining: " + joining.exitValue());
						}
						return minecraft.getPlayerList().getPlayerByName(PEER_NAME) != null;
					}, PEER_WAIT);
					server.runCommand("gamemode survival @a");
					server.runOnServer(minecraft -> awakeBed(minecraft, peerBed, stranger(minecraft).getUUID(), 2));
					server.runCommand(String.format(Locale.ROOT, "tp %s %.1f %d %.1f 180 0", PEER_NAME, perch.getX() + 0.5, perch.getY(), perch.getZ() + 3.0));
					CLIENT.mark("perch.txt", Long.toString(perch.asLong()));
					CLIENT.mark("beds.txt", holderBed.asLong() + " " + peerBed.asLong());
					CLIENT.mark("ready", "");
					CLIENT.awaitPeer(context, peer, "watching", PEER_WAIT);
					// One call: the holder's bed strained and his think at once, so his own think cannot tend it first.
					String worked = server.computeOnServer(minecraft -> {
						ServerLevel level = minecraft.overworld();
						bed(minecraft, holderBed).setStrain(2);
						Status status = buer(minecraft, perch).think(level, NOON, level.getGameTime());
						return status + " " + bed(minecraft, holderBed).strain() + " " + bed(minecraft, peerBed).strain();
					});
					Jugcraft.LOGGER.info("[garden-network] his think: status, the holder's bed, the stranger's bed = {}", worked);
					check(worked.equals(Status.WORKING + " 1 2"), "Buer eases one strain from his holder's bed and none from the stranger's: " + worked);
					CLIENT.mark("worked", "");
					CLIENT.awaitPeer(context, peer, "saw-work", PEER_WAIT);
					// Past the bed's window: the holder's bed left with nothing to ease (so no work gesture turns him away from the
					// stranger below), the stranger's bed still strained and eligible by its own clock; he eases nothing of it.
					String again = server.computeOnServer(minecraft -> {
						ServerLevel level = minecraft.overworld();
						bed(minecraft, holderBed).setStrain(0);
						Status status = buer(minecraft, perch).think(level, NOON, level.getGameTime() + Garden.TEND_TICKS);
						return status + " " + bed(minecraft, peerBed).strain();
					});
					Jugcraft.LOGGER.info("[garden-network] his think past the bed's window: status, the stranger's bed = {}", again);
					check(again.endsWith(" 2"), "Past the window, Buer still eases nothing of the stranger's bed: " + again);
					UUID record = server.computeOnServer(minecraft -> Pacts.pactWith(BoundWills.of(minecraft).ledger(), holder, BuerEntity.COUNTERPART).id());
					int escrow = server.computeOnServer(minecraft -> escrow(minecraft, record));
					// The stranger uses the holder's kept perch.
					server.runOnServer(minecraft -> use(minecraft, stranger(minecraft), perch));
					CLIENT.awaitPeer(context, peer, "peer-asked", PEER_WAIT);
					String kept = server.computeOnServer(minecraft -> Pacts.holds(BoundWills.of(minecraft).ledger(), holder, BuerEntity.COUNTERPART) + " "
							+ escrow(minecraft, record) + " " + (presence(minecraft, perch) instanceof BuerEntity) + " "
							+ BoundWills.of(minecraft).ledger().heldBy(stranger(minecraft).getUUID()).isEmpty());
					Jugcraft.LOGGER.info("[garden-network] the stranger's End: {}; held, escrow, Buer on his perch, the stranger holds nothing = {}",
							CLIENT.read("ended.txt"), kept);
					check(kept.equals("true " + escrow + " true true"), "The stranger ended nothing of the holder's: " + kept);
					CLIENT.mark("checked", "");
					CLIENT.awaitPeer(context, peer, "peer-finished", PEER_WAIT);
					Process leaving = peer;
					context.waitFor(client -> !leaving.isAlive(), PEER_WAIT);
					if (peer.exitValue() != 0) {
						throw CLIENT.failure("The second client failed: " + peer.exitValue());
					}
				}
			}
			CLIENT.mark("passed", "Two client JVMs at one garden perch: Buer eased the holder's bed and never the stranger's; the stranger's End was refused.");
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		} finally {
			if (peer != null && peer.isAlive()) {
				peer.destroy();
			}
		}
	}

	/** The second JVM: the stranger. Each step waits for the holder's mark and leaves its own. */
	private static void peer(ClientGameTestContext context) {
		CLIENT.mark("peer-entered", "");
		context.runOnClient(client -> ConcordanceClientOptions.forgetBeats());
		String address = "127.0.0.1:" + CLIENT.port();
		context.runOnClient(client -> ConnectScreen.startConnecting(new TitleScreen(), client, ServerAddress.parseString(address),
				new ServerData("Garden disposable test", address, ServerData.Type.OTHER), false, null));
		context.waitFor(client -> {
			if (client.gui.screen() instanceof DisconnectedScreen) {
				throw new AssertionError("The second client was disconnected while joining; see peer.log");
			}
			return client.level != null && client.player != null;
		}, PEER_WAIT);
		CLIENT.awaitMark(context, "ready", PEER_WAIT);
		BlockPos perch = BlockPos.of(Long.parseLong(CLIENT.read("perch.txt")));
		String[] beds = CLIENT.read("beds.txt").split(" ");
		BlockPos holderBed = BlockPos.of(Long.parseLong(beds[0]));
		BlockPos ownBed = BlockPos.of(Long.parseLong(beds[1]));
		context.waitFor(client -> client.player.blockPosition().closerThan(perch, 4.0), WAIT);
		context.runOnClient(client -> {
			if (!client.gui.hud.isHidden()) {
				client.gui.hud.toggle();
			}
		});
		// Its world drawn round the perch, Buer on it, and no loading screen, before it says it is watching.
		context.waitFor(client -> client.level.getBlockEntity(perch) instanceof PactPerchBlockEntity && buer(client, perch) != null
				&& client.levelRenderer.hasRenderedAllSections(), PEER_WAIT);
		context.waitFor(client -> client.gui.screen() == null, PEER_WAIT);
		CLIENT.mark("watching", "");
		CLIENT.awaitMark(context, "worked", PEER_WAIT);
		context.waitFor(client -> BuerEntity.Gesture.WORK.animation.equals(gestured(client, perch)) && strain(client, holderBed) == 1
				&& strain(client, ownBed) == 2, PEER_WAIT);
		Jugcraft.LOGGER.info("[garden-network] second client: Buer's work seen; the holder's bed {}, its own {}",
				context.computeOnClient(client -> strain(client, holderBed)), context.computeOnClient(client -> strain(client, ownBed)));
		shot(context, "jugcraft_garden_peer_work");
		CLIENT.mark("saw-work", "");
		context.waitFor(client -> client.gui.screen() instanceof PactScreen && PactClient.state() != null, PEER_WAIT);
		String taken = context.computeOnClient(client -> Component.translatable("message.jugcraft.concordance.pact.reading.taken").getString());
		check(context.computeOnClient(client -> PactClient.state().mode() == PactStatePayload.CLOSED && !PactClient.state().offers(PactRequestPayload.END)
				&& PactClient.state().notes().stream().anyMatch(note -> note.getString().equals(taken))),
				"The holder's kept perch tells the stranger it is taken, and offers no End");
		// Buer turns to whoever speaks at his perch (GC-02), the stranger too: it stands south of him (bearing 0).
		context.waitFor(client -> buer(client, perch) instanceof BuerEntity him && Mth.degreesDifferenceAbs(him.yBodyRot, 0.0F) <= FACING_TOLERANCE, PEER_WAIT);
		float yaw = context.computeOnClient(client -> buer(client, perch) instanceof BuerEntity him ? him.yBodyRot : Float.NaN);
		Jugcraft.LOGGER.info("[garden-network] second client: Buer's drawn body yaw {} against the stranger's bearing 0", yaw);
		// GC-10 unit 7 (review): spec 4.5, the taken page held to the closed page's budget before its picture.
		budget(context, ENDED_WORDS, "the stranger's taken page");
		shot(context, "jugcraft_garden_peer_faced");
		PactStatePayload.Result end = ask(context, PactRequestPayload.END);
		CLIENT.mark("ended.txt", end.reason());
		check(!end.done() && AgreementHolder.Ended.NOT_HOLDER.name().toLowerCase(Locale.ROOT).equals(end.reason()),
				"The stranger's End is refused as not the holder's: " + end.reason());
		// GC-10 unit 7 (review): the refusal drawn on the taken page, held to the budget for a page with a refusal (spec 1.1).
		budget(context, TERMS_WORDS, "the stranger's refused End");
		shot(context, "jugcraft_garden_peer_refused");
		context.getInput().pressKey(InputConstants.KEY_ESCAPE);
		context.waitForScreen(null);
		CLIENT.mark("peer-asked", "");
		CLIENT.awaitMark(context, "checked", PEER_WAIT);
		int own = context.computeOnClient(client -> strain(client, ownBed));
		check(own == 2, "The stranger's own bed keeps its strain on its client: " + own);
		CLIENT.mark("peer-finished", "");
		context.runOnClient(client -> {
			client.disconnectWithSavingScreen();
			client.gui.setScreen(new TitleScreen());
		});
		context.waitForScreen(TitleScreen.class);
	}

	/** The second JVM's own client entrypoint (its manifest names only this test): it says it started, for peer.log. */
	public static final class PeerStartup implements ClientModInitializer {
		@Override
		public void onInitializeClient() {
			Jugcraft.LOGGER.info("[garden-network] second client: test mode {}, port {}", System.getProperty("fabric.client.gametest"),
					System.getProperty(PEER));
			CLIENT.mark("peer-initialized", "");
		}
	}

	/**
	 * Open stone round the holder's place, their perch two blocks south with Buer sealed on it for them (as
	 * ConcordanceRosterClientGameTests seals Stolas: no screen, no compact), and their awake bed two west of it.
	 */
	private static void prepare(MinecraftServer minecraft, BlockPos stand, BlockPos perch, BlockPos holderBed, UUID holder) {
		ServerLevel level = minecraft.overworld();
		for (int dx = -4; dx <= 4; dx++) {
			for (int dz = -1; dz <= 6; dz++) {
				level.setBlockAndUpdate(stand.offset(dx, -1, dz), Blocks.STONE.defaultBlockState());
				for (int dy = 0; dy <= 3; dy++) {
					level.setBlockAndUpdate(stand.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
				}
			}
		}
		ServerPlayer player = minecraft.getPlayerList().getPlayer(holder);
		check(player != null, "The holder is on the server");
		for (String research : List.of("jugcraft:first_light", "jugcraft:binding_arts", Garden.RESEARCH, GardenPacts.RESEARCH)) {
			ConcordanceProgress.grant(player, research, ResearchState.UNDERSTOOD);
		}
		awakeBed(minecraft, holderBed, holder, 0);
		level.setBlockAndUpdate(perch, Workers.PACT_PERCH.defaultBlockState());
		check(level.getBlockEntity(perch) instanceof PactPerchBlockEntity, "The pact perch is placed");
		PactPerchBlockEntity at = (PactPerchBlockEntity) level.getBlockEntity(perch);
		at.holder().setOwner(holder);
		PactDefinition offer = PactPerchBlockEntity.definition(BuerEntity.COUNTERPART);
		check(offer != null && at.holder().seal(level, player, PactPerchBlockEntity.sealing(offer, null, null), level.getGameTime()).outcome()
				== Pacts.Outcome.DONE, "Buer's pact is sealed at the holder's perch");
		check(at.presence(level) instanceof BuerEntity, "Buer is on his perch");
	}

	/** An awake, wet Verdant Bed kept by {@code by} at {@code at}, 4 nutrients, at {@code strain}. */
	private static void awakeBed(MinecraftServer minecraft, BlockPos at, UUID by, int strain) {
		minecraft.overworld().setBlockAndUpdate(at, Garden.VERDANT_BED.defaultBlockState().setValue(VerdantBedBlock.MOISTURE, VerdantBedBlock.WET));
		VerdantBedBlockEntity kept = bed(minecraft, at);
		kept.awaken(by);
		kept.setNutrients(4);
		kept.setStrain(strain);
	}

	private static VerdantBedBlockEntity bed(MinecraftServer minecraft, BlockPos at) {
		VerdantBedBlockEntity found = minecraft.overworld().getBlockEntity(at) instanceof VerdantBedBlockEntity bed ? bed : null;
		check(found != null, "The Verdant Bed is at " + at);
		return found;
	}

	private static @Nullable PactSpiritEntity<?> presence(MinecraftServer minecraft, BlockPos perch) {
		ServerLevel level = minecraft.overworld();
		return ((PactPerchBlockEntity) level.getBlockEntity(perch)).presence(level);
	}

	private static BuerEntity buer(MinecraftServer minecraft, BlockPos perch) {
		PactSpiritEntity<?> found = presence(minecraft, perch);
		check(found instanceof BuerEntity, "Buer is on his perch: " + found);
		return (BuerEntity) found;
	}

	private static int escrow(MinecraftServer minecraft, UUID record) {
		return BoundWills.of(minecraft).escrow(record).stream().mapToInt(ItemStack::getCount).sum();
	}

	private static void use(MinecraftServer minecraft, ServerPlayer player, BlockPos perch) {
		ServerLevel level = minecraft.overworld();
		level.getBlockState(perch).useWithoutItem(level, player, new BlockHitResult(Vec3.atCenterOf(perch), Direction.NORTH, perch, false));
	}

	private static ServerPlayer stranger(MinecraftServer minecraft) {
		ServerPlayer player = minecraft.getPlayerList().getPlayerByName(PEER_NAME);
		check(player != null, "The second client is on the server");
		return player;
	}

	/** The bed's strain as this client holds it (the bed sends it with its update), or -1 with no bed there. */
	private static int strain(Minecraft client, BlockPos at) {
		return client.level != null && client.level.getBlockEntity(at) instanceof VerdantBedBlockEntity bed ? bed.strain() : -1;
	}

	private static @Nullable BuerEntity buer(Minecraft client, BlockPos perch) {
		if (client.level == null) {
			return null;
		}
		List<BuerEntity> found = client.level.getEntitiesOfClass(BuerEntity.class, new AABB(perch).inflate(4));
		return found.isEmpty() ? null : found.getFirst();
	}

	/** The gesture last triggered on him, as this client saw it (it stays set after it plays). */
	private static @Nullable RawAnimation gestured(Minecraft client, BlockPos perch) {
		BuerEntity buer = buer(client, perch);
		if (buer == null) {
			return null;
		}
		AnimatableManager<BuerEntity> manager = buer.getAnimatableInstanceCache().getManagerForId(buer.getId());
		AnimationController<BuerEntity> gestures = manager.getAnimationControllers().get(PactSpiritEntity.GESTURE_CONTROLLER);
		return gestures == null ? null : gestures.getCurrentRawAnimation();
	}

	/** Sends {@code action} for the pact menu this client has open, then waits for the server's answer to it. */
	private static PactStatePayload.Result ask(ClientGameTestContext context, int action) {
		int before = context.computeOnClient(client -> PactClient.lastAnswer());
		int sent = context.computeOnClient(client -> client.player.containerMenu instanceof PactMenu menu ? PactClient.request(menu, action, 0) : -1);
		check(sent > 0, "The request was sent");
		context.waitFor(client -> PactClient.lastAnswer() > before && !PactClient.waiting() && PactClient.state() != null, WAIT);
		return context.computeOnClient(client -> PactClient.state().result().orElseThrow());
	}

	// GC-10 unit 7 (review): the word budgets and their check, as ConcordancePactNetworkTests holds them.
	/** The magic interfaces' budget for a closed page (ConcordancePactClientGameTests' ended page). */
	private static final int ENDED_WORDS = 30;
	/** The magic interfaces' budget for his terms, a refusal included (spec 1.1). */
	private static final int TERMS_WORDS = 50;

	/**
	 * Waits until the pact screen shows the state the client holds and awaits no answer: a state packet marks it to rebuild
	 * on the next tick, and an awaited answer draws its own line, so the words read the settled page.
	 */
	private static void settle(ClientGameTestContext context) {
		context.waitFor(client -> !PactClient.waiting() && (!(client.gui.screen() instanceof MagicScreen magic) || magic.settled()), WAIT);
	}

	/** Spec 1.1, 4.5: the pact screen draws at most {@code most} words without hovering, counted from what it drew. */
	private static void budget(ClientGameTestContext context, int most, String what) {
		settle(context);
		List<String> drawn = context.computeOnClient(client -> client.gui.screen() instanceof PactScreen screen ? screen.drawn() : List.of());
		int words = context.computeOnClient(client -> client.gui.screen() instanceof PactScreen screen ? screen.words() : -1);
		Jugcraft.LOGGER.info("[garden-network] {} draws {} words (at most {}): {}", what, words, most, drawn);
		check(words >= 0 && words <= most, what + " draws at most " + most + " words (spec 1.1): " + words + " " + drawn);
	}

	/** A picture with no toast over it (the peer's own profile keeps it, under owner-network-evidence/). */
	private static void shot(ClientGameTestContext context, String name) {
		context.runOnClient(client -> client.gui.toastManager().clear());
		context.waitTicks(2);
		context.takeScreenshot(name);
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
