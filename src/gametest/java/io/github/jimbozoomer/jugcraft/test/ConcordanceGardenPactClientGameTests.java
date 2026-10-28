package io.github.jimbozoomer.jugcraft.test;

import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.state.AnimationPoint;
import com.geckolib.renderer.GeoEntityRenderer;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.client.PactClient;
import io.github.jimbozoomer.jugcraft.client.PactScreen;
import io.github.jimbozoomer.jugcraft.client.RosterClient;
import io.github.jimbozoomer.jugcraft.client.RosterScreen;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceProgress;
import io.github.jimbozoomer.jugcraft.concordance.JugcraftConcordance;
import io.github.jimbozoomer.jugcraft.concordance.LeyPylonBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.garden.Garden;
import io.github.jimbozoomer.jugcraft.concordance.garden.GleanerBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.garden.VerdantBedBlock;
import io.github.jimbozoomer.jugcraft.concordance.garden.VerdantBedBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.garden.VerdantHeartBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.rules.ResearchState;
import io.github.jimbozoomer.jugcraft.concordance.sky.AstralClaims;
import io.github.jimbozoomer.jugcraft.concordance.spirits.AgreementHolder;
import io.github.jimbozoomer.jugcraft.concordance.spirits.BathinEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.BoundWills;
import io.github.jimbozoomer.jugcraft.concordance.spirits.BuerEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.GardenPacts;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactKeeping;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactPerchBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactReading;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactRequestPayload;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactSpiritEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactStatePayload;
import io.github.jimbozoomer.jugcraft.concordance.spirits.RosterStatePayload;
import io.github.jimbozoomer.jugcraft.concordance.spirits.StolasEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.StrainTending;
import io.github.jimbozoomer.jugcraft.concordance.spirits.VerdanceCarrying;
import io.github.jimbozoomer.jugcraft.concordance.worker.Pacts;
import io.github.jimbozoomer.jugcraft.concordance.worker.Status;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerConnection;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundEventListener;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Concordance garden cohort, GC-04 (GC-P30; GC-P18 for the chooser; docs/features/buer-pact.md): the garden pacts in a real
 * client, on the game's own dedicated server, as Chunk D's ConcordancePactClientGameTests shows Stolas. One client. Results
 * are logged with the prefix "[garden-check]"; shots are {@code jugcraft_garden_<spirit>_<view>}. The player is in survival,
 * so what is spent is spent. Random ticks are off, so no bed rests a strain away between steps, and command feedback is off,
 * so no chat line stands in a shot.
 * <ul>
 * <li><b>The Garden's Regard</b>: a garden specimen says the garden's spirits regard it ({@code jugcraft_garden_regard_tooltip}).</li>
 * <li><b>Nothing to mend</b>: with both Regards understood and a star observed but no bed, the chooser lists Buer not yet
 * ready, his own words saying why ({@code jugcraft_garden_buer_nothing_to_mend}).</li>
 * <li><b>The chooser</b> (GC-P18): an awake bed of the holder's beside the perch, both spirits ready, at three sizes
 * ({@code jugcraft_garden_chooser}, {@code _scale4}, {@code _small}); Buer chosen by keyboard alone.</li>
 * <li><b>His conversation and terms</b> at three sizes, nothing recorded; <b>acceptance</b> refused without the compact
 * ({@code jugcraft_garden_buer_refused}), sealed once with it ({@code jugcraft_garden_buer_kept}); his refuse and assent fired,
 * with their subtitles.</li>
 * <li><b>At work</b> (Dossier 1 item 3, detail b): he eases the bed's strain, turned to it, frozen inside his work clip
 * ({@code jugcraft_garden_buer_at_work}); <b>overworked</b>: a bed he eased that stands at the most strain again is named on
 * his kept page ({@code jugcraft_garden_buer_overworked}) until it is fed.</li>
 * <li><b>Jade's readings</b> of his perch and of him, naming no player ({@code jugcraft_garden_buer_jade} with Jade loaded).</li>
 * <li><b>His body</b>: each loop as the server drives it and each gesture frozen inside its telling pose, from his front
 * ({@code jugcraft_garden_buer_*}); from below ({@code jugcraft_garden_buer_below}); facing each side's speaker
 * ({@code jugcraft_garden_buer_faces_*}).</li>
 * <li><b>The end</b>: End asks first ({@code jugcraft_garden_buer_end_confirm}); yes ends it once and he departs
 * ({@code jugcraft_garden_buer_ended}).</li>
 * <li><b>Bathin</b> (GC-05; docs/features/bathin-pact.md), at the same perch: listed not ready without a Heart, then chosen
 * by keyboard and suddenly there in a ring of dust; his conversation at three sizes, refused and sealed; one carry frozen
 * with both ends shimmering; his matter on the roster ("awaiting your answer"), in Jade and on his kept page, answered
 * and changed back; Jade; ten poses; four bearings; the end ({@code jugcraft_garden_bathin_*},
 * {@code jugcraft_garden_roster_awaiting}).</li>
 * </ul>
 * The helpers that name no spirit are Chunk D's, copied (its file stays as it is). The perch is opened by the server's use of
 * it (no aimed click), each open at least its rate gate's ten ticks after the last. The narrator is not heard, and one
 * client is not two.
 */
public class ConcordanceGardenPactClientGameTests implements FabricClientGameTest {
	private static final int WAIT = 200;
	/** More than the perch's open gate (PactMenu.OPEN_TICKS, 10): a use sooner is refused. */
	private static final int OPEN_GAP = 12;
	// GC-02 (GC-P8): his drawn body within this of the speaker's bearing (tools/concordance_pacts.py FACING_BODY_TOLERANCE).
	private static final float FACING_BODY_TOLERANCE = 15.0F;
	private static final long NOON = 6000L;
	/** The roster's key has no default (RosterClient): bound to this for one look, as ConcordanceRosterClientGameTests does. */
	private static final int ROSTER_TEST_KEY = InputConstants.KEY_F7;

	/** GC-02: a side of the perch the holder speaks from, their look at him, his bearing to them, and its shot. */
	private record Facing(String name, int dx, int dz, float look, float yaw, String shot) {
	}

	/** GC-02's rule for Buer: two blocks east, south, west and north of the perch in turn. */
	private static final List<Facing> FACINGS = List.of(
			new Facing("east", 1, 0, 90.0F, -90.0F, "jugcraft_garden_buer_faces_east"),
			new Facing("south", 0, 1, 180.0F, 0.0F, "jugcraft_garden_buer_faces_south"),
			new Facing("west", -1, 0, -90.0F, 90.0F, "jugcraft_garden_buer_faces_west"),
			new Facing("north", 0, -1, 0.0F, 180.0F, "jugcraft_garden_buer_faces_north"));
	private static final String BUER = "jugcraft:buer";
	private static final String PERCH = "jugcraft:pact_perch";
	private static final String FIRST_LIGHT = "jugcraft:first_light";
	private static final String BINDING_ARTS = "jugcraft:binding_arts";
	private static final String STOLAS_REGARD = "jugcraft:stolas_regard";
	private static final String LANTERN_STAR = "jugcraft:lantern_star";
	/** The specimens' line (tools/concordance_garden_pacts.py; GC-03). */
	private static final String SPECIMEN_LINE = "The garden's spirits regard this: sneak and use it in the air to examine it";
	/** His refusal at a perch with no bed of the inviter's near (Dossier 1 item 6). */
	private static final String NOTHING_TO_MEND = "There is nothing here I could mend.";
	/** His kept page's words (message.jugcraft.concordance.pact.strain_tending.*). */
	private static final String TENDING = "message.jugcraft.concordance.pact.strain_tending.";
	private static final String CANNOT_AFFORD = "message.jugcraft.concordance.pact.answer.cannot_afford.items";
	/** His Open Hand Compact (data/jugcraft/concordance/pact/buer.json): no resonance, so no astrolabe. */
	private static final List<String> COMPACT = List.of("jugcraft:mendvetch 4", "minecraft:bone_meal 4");
	private static final String MAIN = "main";
	private static final String GESTURES = PactSpiritEntity.GESTURE_CONTROLLER;
	// His loops (tools/concordance_pacts.py SPIRITS["buer"]["body"]["loops"]; check_spirit_bodies).
	private static final String IDLE = "active";
	private static final String LISTEN = "animation.buer.listen";
	private static final String WAITING = "animation.buer.waiting";
	private static final String SUSPENDED = "animation.buer.suspended";
	// GC-05: Bathin (tools/concordance_pacts.py SPIRITS["bathin"]; check_spirit_bodies).
	private static final String BATHIN = "jugcraft:bathin";
	private static final String BATHIN_IDLE = "animation.bathin.idle";
	private static final String BATHIN_LISTEN = "animation.bathin.listen";
	private static final String BATHIN_WAITING = "animation.bathin.waiting";
	private static final String BATHIN_SUSPENDED = "animation.bathin.suspended";
	/** GC-P8's body tolerance, for his facing. */
	private static final float BATHIN_FACING_TOLERANCE = 15.0F;
	/** His refusal at a perch with no Heart of the inviter's near, or nowhere to carry (Dossier 2 item 6). */
	private static final String NOTHING_TO_CARRY = "Nothing here to carry, or nowhere to carry it.";
	/** His compact (data/jugcraft/concordance/pact/bathin.json): no resonance, so no astrolabe. */
	private static final List<String> BATHIN_COMPACT = List.of("minecraft:ender_pearl 2", "jugcraft:verdant_chaff 8");

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestDedicatedServerContext server = context.worldBuilder().createServer()) {
			try (TestDedicatedServerConnection connection = server.connect()) {
				// 26.3: the client shows LevelLoadingScreen while it joins; nothing is pictured until no screen is shown.
				context.waitForScreen(null);
				connection.waitForChunksRender();
				server.runCommand("time set noon");
				server.runCommand("weather clear");
				server.runCommand("gamerule minecraft:spawn_mobs false");
				server.runCommand("gamerule minecraft:random_tick_speed 0");
				server.runCommand("gamerule minecraft:send_command_feedback false");
				server.runCommand("gamemode survival @a");
				context.waitTicks(20);
				BlockPos stand = context.computeOnClient(client -> client.player.blockPosition());
				BlockPos perch = stand.south(2);
				BlockPos bed = perch.west(3);
				server.runOnServer(minecraft -> prepare(minecraft, stand, perch));
				server.runCommand(String.format(Locale.ROOT, "tp @a %.1f %d %.1f 0 0", stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5));
				context.waitTicks(10);
				connection.waitForChunksRender();
				int scale = context.computeOnClient(client -> client.options.guiScale().get());
				context.runOnClient(client -> {
					client.options.guiScale().set(2);
					client.resizeGui();
				});
				regardTooltip(context, server);
				nothingToMend(context, server, perch);
				chooser(context, server, perch, bed);
				conversation(context, server, perch);
				sizes(context);
				Hearing hearing = hear(context);
				acceptance(context, server);
				heard(context, server, hearing, perch);
				atWork(context, server, connection, perch, bed);
				overworked(context, server, perch, bed);
				jade(context, server, perch);
				poses(context, server, perch);
				below(context, server, connection, perch);
				facing(context, server, connection, perch);
				ending(context, server, perch);
				// GC-05: Bathin at the same perch, chosen from the chooser (Stolas's, Buer's and his entries), to his departure.
				bathin(context, server, connection, perch);
				context.runOnClient(client -> {
					client.options.guiScale().set(scale);
					client.resizeGui();
				});
			}
		}
	}

	/**
	 * The stone floor and air round the owner's perch two blocks south (wide enough for a bed three west of it and a camera
	 * five west); the Binding Arts, Verdant Husbandry, The Garden's Regard and Stolas's Regard understood and the Lantern Star
	 * observed, so the chooser lists Stolas and Buer (and, since GC-05, Bathin); an empty inventory. No bed yet.
	 */
	private static void prepare(MinecraftServer minecraft, BlockPos stand, BlockPos perch) {
		check(BuiltInRegistries.BLOCK.containsKey(Identifier.parse(PERCH)) && BuiltInRegistries.ENTITY_TYPE.containsKey(Identifier.parse(BUER)),
				"The pact perch and Buer are registered");
		ServerPlayer player = player(minecraft);
		ServerLevel level = minecraft.overworld();
		for (int dx = -6; dx <= 2; dx++) {
			for (int dz = -1; dz <= 6; dz++) {
				level.setBlockAndUpdate(stand.offset(dx, -1, dz), Blocks.STONE.defaultBlockState());
				for (int dy = 0; dy <= 3; dy++) {
					level.setBlockAndUpdate(stand.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
				}
			}
		}
		level.setBlockAndUpdate(perch, BuiltInRegistries.BLOCK.getValue(Identifier.parse(PERCH)).defaultBlockState());
		check(level.getBlockEntity(perch) instanceof AgreementHolder.Holds, "The perch keeps its pact through the shared holder (D-02)");
		((AgreementHolder.Holds) level.getBlockEntity(perch)).holder().setOwner(player.getUUID());
		for (String research : List.of(FIRST_LIGHT, BINDING_ARTS, Garden.RESEARCH, GardenPacts.RESEARCH, STOLAS_REGARD)) {
			ConcordanceProgress.grant(player, research, ResearchState.UNDERSTOOD);
		}
		AstralClaims.of(minecraft).observe(player.getUUID(), LANTERN_STAR, level.getGameTime());
		player.getInventory().clearContent();
	}

	/** GC-03 (GC-P35): a Sunpetal in hand; its tooltip says the garden's spirits regard it; hovered in the inventory. */
	private static void regardTooltip(ClientGameTestContext context, TestDedicatedServerContext server) {
		server.runCommand("item replace entity @p hotbar.0 with jugcraft:sunpetal");
		context.waitFor(client -> !client.player.getInventory().getItem(0).isEmpty(), WAIT);
		List<String> lines = context.computeOnClient(client -> client.player.getInventory().getItem(0)
				.getTooltipLines(Item.TooltipContext.of(client.level), client.player, TooltipFlag.NORMAL).stream().map(Component::getString).toList());
		Jugcraft.LOGGER.info("[garden-check] specimen tooltip: {}", lines);
		check(lines.contains(SPECIMEN_LINE), "A garden specimen says the garden's spirits regard it (The Garden's Regard): " + lines);
		context.setScreen(() -> new InventoryScreen(Minecraft.getInstance().player));
		context.waitForScreen(InventoryScreen.class);
		double[] at = context.computeOnClient(client -> {
			Screen screen = client.gui.screen();
			// The survival inventory is 176 by 166 GUI units, centred; its first hotbar slot's corner is at (8, 142).
			double left = (screen.width - 176) / 2;
			double top = (screen.height - 166) / 2;
			double guiPerPixelX = MouseHandler.getScaledXPos(client.getWindow(), 1.0);
			double guiPerPixelY = MouseHandler.getScaledYPos(client.getWindow(), 1.0);
			return new double[] {(left + 8 + 8) / guiPerPixelX, (top + 142 + 8) / guiPerPixelY};
		});
		context.getInput().setCursorPos(at[0], at[1]);
		context.waitTicks(5);
		shot(context, "jugcraft_garden_regard_tooltip");
		context.setScreen(() -> null);
		context.waitForScreen(null);
		server.runOnServer(minecraft -> player(minecraft).getInventory().setItem(0, ItemStack.EMPTY));
		context.waitFor(client -> client.player.getInventory().getItem(0).isEmpty(), WAIT);
	}

	/** Dossier 1 item 6: no awake bed of the inviter's within his radius, and Buer is listed not ready, in his own words. */
	private static void nothingToMend(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch) {
		open(context, server, perch);
		context.waitFor(client -> PactClient.state() != null && PactClient.state().mode() == PactStatePayload.INVITE, WAIT);
		String entries = context.computeOnClient(client -> entries(PactClient.state()));
		Jugcraft.LOGGER.info("[garden-check] chooser without a bed: {}", entries);
		int buer = context.computeOnClient(client -> index(PactClient.state().patterns(), BuerEntity.SIGN));
		int stolas = context.computeOnClient(client -> index(PactClient.state().patterns(), StolasEntity.SIGN));
		check(buer >= 0 && stolas >= 0, "The chooser lists both spirits the holder may invite: " + entries);
		boolean waiting = context.computeOnClient(client -> {
			PactStatePayload.Choice his = PactClient.state().patterns().get(buer);
			return !his.ready() && his.amount() == 1
					&& PactClient.state().notes().stream().anyMatch(note -> note.getString().contains(NOTHING_TO_MEND));
		});
		boolean ready = context.computeOnClient(client -> PactClient.state().patterns().get(stolas).ready());
		check(waiting && ready, "Without a bed Buer is not ready, one thing missing in his own words, and Stolas is: " + entries);
		fits(context, "the chooser without a bed");
		named(context);
		shot(context, "jugcraft_garden_buer_nothing_to_mend");
		context.getInput().pressKey(InputConstants.KEY_ESCAPE);
		context.waitForScreen(null);
	}

	/**
	 * GC-P18 (GC-P10): an awake Verdant Bed of the holder's three blocks west of the perch, and Stolas and Buer ready (Bathin,
	 * listed since GC-05, not yet: no Heart); the chooser at GUI scale 2, at GUI scale 4 in a 1920x1080 window and in a
	 * 320x180 window; Buer chosen by keyboard alone.
	 */
	private static void chooser(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch, BlockPos bed) {
		server.runOnServer(minecraft -> {
			ServerLevel level = minecraft.overworld();
			level.setBlockAndUpdate(bed, Garden.VERDANT_BED.defaultBlockState().setValue(VerdantBedBlock.MOISTURE, VerdantBedBlock.WET));
			VerdantBedBlockEntity kept = bed(minecraft, bed);
			kept.awaken(player(minecraft).getUUID());
			kept.setNutrients(4);
		});
		context.waitTicks(OPEN_GAP);
		open(context, server, perch);
		// GC-05 (GC-P59): Stolas's and Buer's entries ready; Bathin's listed, not yet (no Heart of the holder's near).
		context.waitFor(client -> PactClient.state() != null && PactClient.state().mode() == PactStatePayload.INVITE
				&& PactClient.state().patterns().stream().filter(choice -> choice.id().equals(StolasEntity.SIGN) || choice.id().equals(BuerEntity.SIGN))
						.filter(PactStatePayload.Choice::ready).count() == 2, WAIT);
		String ids = context.computeOnClient(client -> PactClient.state().patterns().stream().map(PactStatePayload.Choice::id).toList().toString());
		String flags = context.computeOnClient(client -> PactClient.state().patterns().stream().map(choice -> String.valueOf(choice.ready())).toList().toString());
		Jugcraft.LOGGER.info("[garden-check] chooser: {} ready={}", ids, flags);
		context.waitFor(client -> client.gui.screen() instanceof PactScreen, WAIT);
		fits(context, "the chooser at GUI scale 2");
		named(context);
		shot(context, "jugcraft_garden_chooser");
		int width = context.computeOnClient(client -> client.getWindow().getWidth());
		int height = context.computeOnClient(client -> client.getWindow().getHeight());
		context.getInput().resizeWindow(1920, 1080);
		context.runOnClient(client -> {
			client.options.guiScale().set(4);
			client.resizeGui();
		});
		context.waitTicks(10);
		int applied = context.computeOnClient(client -> client.getWindow().getGuiScale());
		check(applied == 4, "The high-scale chooser is at GUI scale 4: " + applied);
		context.waitFor(client -> client.gui.screen() instanceof PactScreen, WAIT);
		fits(context, "the chooser at GUI scale 4");
		named(context);
		shot(context, "jugcraft_garden_chooser_scale4");
		context.runOnClient(client -> {
			client.options.guiScale().set(2);
			client.resizeGui();
		});
		context.getInput().resizeWindow(320, 180);
		context.waitTicks(10);
		context.waitFor(client -> client.gui.screen() instanceof PactScreen, WAIT);
		fits(context, "the chooser at 320x180");
		named(context);
		shot(context, "jugcraft_garden_chooser_small");
		context.getInput().resizeWindow(width, height);
		context.waitTicks(10);
		String name = context.computeOnClient(client -> Component.translatable("entity.jugcraft.buer").getString());
		tabToNamed(context, name);
		context.getInput().pressKey(InputConstants.KEY_RETURN);
		context.waitFor(client -> PactClient.state() != null && PactClient.state().mode() == PactStatePayload.NEGOTIATE
				&& BuerEntity.SIGN.equals(PactClient.state().spirit().sign()) && buer(client, perch) != null, WAIT);
		String reason = context.computeOnClient(client -> PactClient.state().result().map(PactStatePayload.Result::reason).orElse(""));
		Jugcraft.LOGGER.info("[garden-check] invited buer ({})", reason);
	}

	/** Chunk D's conversation with Buer's names: he arrives with his gesture, facing his inviter, and speaks; nothing is recorded. */
	private static void conversation(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch) {
		context.waitFor(client -> BuerEntity.Gesture.ARRIVE.animation.equals(gestured(client, perch)), WAIT);
		// GC-02: invited from the north, he arrives facing north.
		float arrived = context.computeOnClient(client -> bodyYaw(client, perch));
		Jugcraft.LOGGER.info("[garden-check] facing on arrival: drawn body yaw {}, inviter's bearing {}", arrived, 180.0F);
		check(Mth.degreesDifferenceAbs(arrived, 180.0F) <= FACING_BODY_TOLERANCE, "Invited from the north, he arrives facing north: " + arrived);
		String shown = context.computeOnClient(client -> describe(PactClient.state()));
		Jugcraft.LOGGER.info("[garden-check] invited: {}", shown);
		check(context.computeOnClient(client -> PactClient.state().beats().size() == 3 && PactClient.state().notes().isEmpty()
				&& PactClient.state().offers(PactRequestPayload.ACCEPT)
				&& PactClient.state().terms().stream().map(PactStatePayload.Term::key).toList().equals(PactStatePayload.TERMS)),
				"Buer speaks three beats, nothing stands in the way, and his terms name every term, in order: " + shown);
		fits(context, "a beat at GUI scale 2");
		named(context);
		shot(context, "jugcraft_garden_buer_conversation");
		tabTo(context, "next");
		for (int beat = 0; beat < 3; beat++) {
			context.getInput().pressKey(InputConstants.KEY_RETURN);
			context.waitTicks(3);
		}
		check(context.computeOnClient(client -> screen(client).page() == PactScreen.TERMS_PAGE && "para:0".equals(screen(client).focusedKey())),
				"Enter three times reaches his terms, the keyboard on their first line, never on Accept");
		fits(context, "the terms at GUI scale 2");
		named(context);
		shot(context, "jugcraft_garden_buer_terms");
		String words = context.computeOnClient(client -> String.join(" ", PactClient.state().terms().stream()
				.map(term -> term.value().getString()).toList()).toLowerCase(Locale.ROOT));
		Jugcraft.LOGGER.info("[garden-check] his terms: {}", words);
		check(!words.contains("astrolabe") && !words.contains("resonance"), "His terms ask for no astrolabe and no resonance (GB23): " + words);
		check(!server.computeOnServer(ConcordanceGardenPactClientGameTests::held), "Hearing his terms records nothing");
	}

	/** Chunk D's sizes: his terms and a beat at GUI scale 4 in 1920x1080 and in a 320x180 window (the contract's size). */
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
		named(context);
		shot(context, "jugcraft_garden_buer_terms_scale4");
		press(context, "back");
		fits(context, "a beat at GUI scale 4");
		named(context);
		shot(context, "jugcraft_garden_buer_conversation_scale4");
		press(context, "next");
		context.runOnClient(client -> {
			client.options.guiScale().set(2);
			client.resizeGui();
		});
		context.getInput().resizeWindow(320, 180);
		context.waitTicks(10);
		fits(context, "the terms in a 320x180 window");
		named(context);
		shot(context, "jugcraft_garden_buer_terms_small");
		press(context, "back");
		fits(context, "a beat in a 320x180 window");
		named(context);
		shot(context, "jugcraft_garden_buer_conversation_small");
		press(context, "next");
		context.getInput().resizeWindow(width, height);
		context.waitTicks(10);
		check(context.computeOnClient(client -> screen(client).page() == PactScreen.TERMS_PAGE), "Back on his terms");
	}

	/** Accept refused without his compact and spending nothing; given it, sealed once and spent once. */
	private static void acceptance(ClientGameTestContext context, TestDedicatedServerContext server) {
		tabTo(context, "accept");
		PactStatePayload.Result refused = answer(context);
		String said = refused.message().getContents() instanceof TranslatableContents words ? words.getKey() : "";
		Jugcraft.LOGGER.info("[garden-check] Accept without the compact's materials: {} ({}: {})", refused.reason(), said, refused.message().getString());
		int mode = context.computeOnClient(client -> PactClient.state().mode());
		check("cannot_afford".equals(refused.reason()) && !refused.done() && CANNOT_AFFORD.equals(said) && mode == PactStatePayload.NEGOTIATE,
				"Without the compact's materials Accept is refused with its reason: " + refused.reason() + " " + said);
		check(!server.computeOnServer(ConcordanceGardenPactClientGameTests::held), "A refusal records nothing");
		context.waitFor(client -> screen(client).control("result") instanceof PactScreen.Verdict verdict && verdict.refused(), WAIT);
		check(context.computeOnClient(client -> "result".equals(screen(client).focusedKey()) && screen(client).inView("result")),
				"The refusal is in view above his terms, the keyboard on it and off Accept (no tooltip over it)");
		shot(context, "jugcraft_garden_buer_refused");
		List<String> cost = context.computeOnClient(client -> PactClient.state().compact().stream()
				.map(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()) + " " + stack.getCount()).toList());
		int resonance = context.computeOnClient(client -> PactClient.state().resonance());
		Jugcraft.LOGGER.info("[garden-check] Buer's Open Hand Compact: {} and {} resonance", cost, resonance);
		check(cost.equals(COMPACT) && resonance == 0, "His compact is four Mendvetch and four bone meal, no resonance: " + cost + " " + resonance);
		server.runOnServer(minecraft -> cost.forEach(line -> player(minecraft).getInventory().add(stack(line))));
		context.waitTicks(5);
		tabTo(context, "accept");
		PactStatePayload.Result sealed = answer(context);
		int kept = context.computeOnClient(client -> PactClient.state().mode());
		check(sealed.done() && "sealed".equals(sealed.reason()) && kept == PactStatePayload.KEEP, "Accepting with the compact seals the pact: " + sealed.reason());
		String spent = server.computeOnServer(minecraft -> held(minecraft) + " "
				+ cost.stream().map(line -> String.valueOf(player(minecraft).getInventory().countItem(stack(line).getItem()))).toList());
		Jugcraft.LOGGER.info("[garden-check] sealed: held, materials left = {}", spent);
		check(spent.equals("true [0, 0]"), "Sealed once, his compact spent once: " + spent);
		context.waitFor(client -> screen(client).control("result") instanceof PactScreen.Verdict verdict && !verdict.refused(), WAIT);
		fits(context, "the kept pact at GUI scale 2");
		named(context);
		shot(context, "jugcraft_garden_buer_kept");
	}

	/**
	 * Dossier 1 item 3 and detail (b): the screen closed, the camera west of the bed looking east at it and at him; the bed
	 * strained and his think in one server call: he eases one strain and turns to the bed, frozen inside his work clip.
	 */
	private static void atWork(ClientGameTestContext context, TestDedicatedServerContext server, TestDedicatedServerConnection connection,
			BlockPos perch, BlockPos bed) {
		context.getInput().pressKey(InputConstants.KEY_ESCAPE);
		context.waitForScreen(null);
		check(server.computeOnServer(ConcordanceGardenPactClientGameTests::held), "Closing the screen keeps the pact");
		check(context.computeOnClient(client -> buer(client, perch) instanceof BuerEntity him
				&& client.getEntityRenderDispatcher().getRenderer(him) instanceof GeoEntityRenderer<?, ?>),
				"Buer stays on his perch, drawn by GeckoLib (the owner's Decor Blossom, GC-S12)");
		boolean hidden = hideHud(context);
		try {
			BlockPos camera = perch.west(5);
			server.runCommand(String.format(Locale.ROOT, "tp @a %.1f %d %.1f -90 0", camera.getX() + 0.5, camera.getY(), camera.getZ() + 0.5));
			context.waitTicks(10);
			connection.waitForChunksRender();
			// One call: the bed strained and his think at once, so his own think cannot tend it first; the bed's 600 ticks then
			// keep him from tending it again.
			String worked = server.computeOnServer(minecraft -> {
				ServerLevel level = minecraft.overworld();
				VerdantBedBlockEntity kept = bed(minecraft, bed);
				kept.setStrain(2);
				Status status = presence(minecraft, perch).think(level, NOON, level.getGameTime());
				return status + " " + kept.strain();
			});
			Jugcraft.LOGGER.info("[garden-check] his think over a strained bed: status and strain = {}", worked);
			check(worked.equals(Status.WORKING + " 1"), "By daylight he eases one strain from the bed: " + worked);
			BuerEntity.Gesture work = BuerEntity.Gesture.WORK;
			// Two clocks (the unit review): his clip on the client, and his head then body turning after the server's think;
			// wait for both rather than a fixed count, then freeze (the 1.4 to 2.2 s check below still bounds it).
			context.waitFor(client -> clipTime(client, perch, work) >= 1.4
					&& Mth.degreesDifferenceAbs(bodyYaw(client, perch), 90.0F) <= FACING_BODY_TOLERANCE, WAIT);
			server.runCommand("tick freeze");
			try {
				context.waitTicks(5);
				double at = context.computeOnClient(client -> clipTime(client, perch, work));
				float body = context.computeOnClient(client -> bodyYaw(client, perch));
				Jugcraft.LOGGER.info("[garden-check] his work frozen {} s into its clip; drawn body yaw {} (the bed's bearing 90)", at, body);
				check(at >= 1.4 && at <= 2.2, "His work is caught in its telling pose, 1.4 to 2.2 s into it: " + at);
				check(Mth.degreesDifferenceAbs(body, 90.0F) <= FACING_BODY_TOLERANCE, "At work he faces the bed, west of him: " + body);
				shot(context, "jugcraft_garden_buer_at_work");
			} finally {
				server.runCommand("tick unfreeze");
			}
			int strain = server.computeOnServer(minecraft -> bed(minecraft, bed).strain());
			check(strain == 1, "The bed keeps the strain he left it: " + strain);
		} finally {
			restoreHud(context, hidden);
		}
	}

	/**
	 * GC-P22, GC-P23: the bed he eased stands at the most strain again; his think, past the bed's own window, makes his
	 * complaint and tends nothing; his kept page names the bed. Fed with bone meal (its own use), it eases to 2, the complaint
	 * no longer stands, and his next think forgets it; the bed is then left unstrained so no later step sees his work.
	 */
	private static void overworked(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch, BlockPos bed) {
		String complained = server.computeOnServer(minecraft -> {
			ServerLevel level = minecraft.overworld();
			bed(minecraft, bed).setStrain(3);
			BuerEntity buer = presence(minecraft, perch);
			Status status = buer.think(level, NOON, level.getGameTime() + 600);
			return status + " " + memory(buer).overworked(bed) + " " + bed(minecraft, bed).strain();
		});
		Jugcraft.LOGGER.info("[garden-check] overworked: status, complaint and strain = {}", complained);
		check(complained.equals(Status.IDLE + " true 3"), "A bed he eased that stands at the most strain again is overworked, and he tends it no more: " + complained);
		context.waitTicks(OPEN_GAP);
		open(context, server, perch);
		context.waitFor(client -> PactClient.state() != null && PactClient.state().mode() == PactStatePayload.KEEP, WAIT);
		List<String> lines = context.computeOnClient(client -> lines(PactClient.state()));
		Jugcraft.LOGGER.info("[garden-check] his kept page: {}", lines);
		check(lines.stream().filter(line -> line.startsWith("Overworked: the bed at")).count() == 1, "His kept page names the overworked bed: " + lines);
		named(context);
		fits(context, "the kept page with his complaint");
		shot(context, "jugcraft_garden_buer_overworked");
		context.getInput().pressKey(InputConstants.KEY_ESCAPE);
		context.waitForScreen(null);
		String fed = server.computeOnServer(minecraft -> {
			ServerLevel level = minecraft.overworld();
			ServerPlayer player = player(minecraft);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BONE_MEAL));
			InteractionResult used = player.gameMode.useItemOn(player, level, player.getMainHandItem(), InteractionHand.MAIN_HAND,
					new BlockHitResult(Vec3.atCenterOf(bed).add(0.0, 0.5, 0.0), Direction.UP, bed, false));
			VerdantBedBlockEntity kept = bed(minecraft, bed);
			int strain = kept.strain();
			List<String> rows = keys(StrainTending.rows(level, holder(minecraft, perch).record(level), NOON, level.getGameTime()));
			kept.setStrain(0);
			player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
			return used.consumesAction() + " " + strain + " " + rows;
		});
		Jugcraft.LOGGER.info("[garden-check] fed: used, strain and his rows = {}", fed);
		check(fed.equals("true 2 [" + TENDING + "row.service]"), "Bone meal eases the bed to 2 and his complaint no longer stands: " + fed);
		untilServer(context, server, minecraft -> !memory(presence(minecraft, perch)).overworked(bed), "His next think forgets the complaint");
	}

	/** Jade's readings of his perch and of him (PactReading, read on the server: no Jade class); with Jade loaded, its overlay. */
	private static void jade(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch) {
		String readings = server.computeOnServer(minecraft -> {
			ServerLevel level = minecraft.overworld();
			CompoundTag ofPerch = PactReading.holder(level, (PactPerchBlockEntity) level.getBlockEntity(perch), level.getGameTime());
			CompoundTag ofHim = PactReading.presence(level, presence(minecraft, perch), level.getGameTime());
			String both = ofPerch + " " + ofHim;
			boolean right = "kept".equals(ofPerch.getStringOr("standing", "")) && BUER.equals(ofPerch.getStringOr("counterpart", ""))
					&& StrainTending.WORK.equals(ofPerch.getStringOr("service", "")) && "active".equals(ofPerch.getStringOr("state", ""))
					&& ofPerch.getIntOr("due_days", -1) == 7 && "kept".equals(ofHim.getStringOr("standing", ""))
					&& !ofPerch.contains("note") && !ofHim.contains("note")
					&& PactReading.KEYS.containsAll(ofPerch.keySet()) && PactReading.KEYS.containsAll(ofHim.keySet())
					&& !both.contains(player(minecraft).getUUID().toString()) && !both.contains(player(minecraft).getName().getString());
			return (right ? "ok " : "bad ") + both;
		});
		Jugcraft.LOGGER.info("[garden-check] Jade's readings of the perch and of Buer: {}", readings);
		check(readings.startsWith("ok"), "Jade's readings: his pact, service, state and next offering, no note at noon with no bed overworked, naming no player: "
				+ readings);
		if (FabricLoader.getInstance().isModLoaded("jade")) {
			boolean hidden = context.computeOnClient(client -> client.gui.hud.isHidden());
			context.runOnClient(client -> {
				if (client.gui.hud.isHidden()) {
					client.gui.hud.toggle();
				}
			});
			try {
				standSouth(server, perch);
				context.waitTicks(10);
				context.getInput().lookAt(perch.above());
				context.waitTicks(20);
				shot(context, "jugcraft_garden_buer_jade");
			} finally {
				restoreHud(context, hidden);
			}
		} else {
			Jugcraft.LOGGER.info("[garden-check] Jade is not loaded: no Jade shot of Buer");
		}
	}

	/**
	 * His body from his front (south of the perch, facing north; spoken to from there first, so he faces the camera): each loop
	 * as the server's state drives it (his idle, the owner's "active"; listening while his perch's screen is open; asleep
	 * outside his hours; drooping while suspended) and each gesture as the server fires it, frozen inside its telling pose.
	 * His astir loop is not posed (as Stolas's: GC-P29). His gestures change nothing.
	 */
	private static void poses(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch) {
		boolean hidden = hideHud(context);
		try {
			standSouth(server, perch);
			context.waitTicks(OPEN_GAP);
			open(context, server, perch);
			context.waitFor(client -> Mth.degreesDifferenceAbs(bodyYaw(client, perch), 0.0F) <= FACING_BODY_TOLERANCE, WAIT);
			context.getInput().pressKey(InputConstants.KEY_ESCAPE);
			context.waitForScreen(null);
			context.getInput().lookAt(perch.above());
			loopPose(context, server, perch, IDLE, "jugcraft_garden_buer_idle", false);
			context.waitTicks(OPEN_GAP);
			open(context, server, perch);
			context.waitFor(client -> buer(client, perch) instanceof BuerEntity him && him.listening(), WAIT);
			loopPose(context, server, perch, LISTEN, "jugcraft_garden_buer_listen", true);
			context.waitFor(client -> IDLE.equals(drawing(client, perch, MAIN)), WAIT);
			server.runCommand("time set midnight");
			try {
				loopPose(context, server, perch, WAITING, "jugcraft_garden_buer_asleep", false);
				List<String> rows = server.computeOnServer(minecraft -> {
					ServerLevel level = minecraft.overworld();
					return keys(StrainTending.rows(level, holder(minecraft, perch).record(level), level.getOverworldClockTime(), level.getGameTime()));
				});
				Jugcraft.LOGGER.info("[garden-check] his kept page at midnight: {}", rows);
				check(rows.contains(TENDING + "row.asleep"), "At midnight his kept page says he sleeps until dawn: " + rows);
			} finally {
				server.runCommand("time set noon");
			}
			context.waitFor(client -> IDLE.equals(drawing(client, perch, MAIN)), WAIT);
			check(server.computeOnServer(minecraft -> PactKeeping.suspend(minecraft.overworld(), player(minecraft), holder(minecraft, perch),
					minecraft.overworld().getGameTime())) == AgreementHolder.Toggle.SUSPENDED, "Its holder suspends the pact");
			loopPose(context, server, perch, SUSPENDED, "jugcraft_garden_buer_suspended", false);
			check(server.computeOnServer(minecraft -> PactKeeping.resume(minecraft.overworld(), player(minecraft), holder(minecraft, perch),
					minecraft.overworld().getGameTime())) == AgreementHolder.Toggle.RESUMED, "and resumes it");
			context.waitFor(client -> IDLE.equals(drawing(client, perch, MAIN)), WAIT);
			gesturePose(context, server, perch, BuerEntity.Gesture.INSPECT, 0.4, 1.2, "jugcraft_garden_buer_inspect");
			gesturePose(context, server, perch, BuerEntity.Gesture.ASSENT, 0.5, 1.3, "jugcraft_garden_buer_assent");
			gesturePose(context, server, perch, BuerEntity.Gesture.REFUSE, 0.4, 1.3, "jugcraft_garden_buer_refuse");
			gesturePose(context, server, perch, BuerEntity.Gesture.WORK, 1.4, 2.2, "jugcraft_garden_buer_work");
			// He closes and sinks (depart holds its last frame) and unfurls again (his arrival replaces it).
			gesturePose(context, server, perch, BuerEntity.Gesture.DEPART, 1.1, 1.6, "jugcraft_garden_buer_depart");
			gesturePose(context, server, perch, BuerEntity.Gesture.ARRIVE, 1.2, 1.9, "jugcraft_garden_buer_arrive");
			context.waitFor(client -> drawing(client, perch, GESTURES).isEmpty() && IDLE.equals(drawing(client, perch, MAIN)), WAIT);
			check(server.computeOnServer(ConcordanceGardenPactClientGameTests::held) && server.computeOnServer(minecraft -> presence(minecraft, perch).isAlive()),
					"His gestures change nothing: the pact is kept and he is on his perch");
		} finally {
			restoreHud(context, hidden);
		}
	}

	/**
	 * His plate and pistil cap seen from below (no surface fighting): the floor south of the perch opened into a pit two deep,
	 * the camera in it looking up at him, the world frozen. A reviewer reads the shot; the log records the camera and his loop.
	 */
	private static void below(ClientGameTestContext context, TestDedicatedServerContext server, TestDedicatedServerConnection connection,
			BlockPos perch) {
		BlockPos pit = perch.south();
		List<BlockState> was = server.computeOnServer(minecraft -> List.of(minecraft.overworld().getBlockState(pit.below(2)),
				minecraft.overworld().getBlockState(pit.below(3))));
		boolean hidden = hideHud(context);
		try {
			server.runOnServer(minecraft -> {
				ServerLevel level = minecraft.overworld();
				level.setBlockAndUpdate(pit.below(), Blocks.AIR.defaultBlockState());
				level.setBlockAndUpdate(pit.below(2), Blocks.AIR.defaultBlockState());
				level.setBlockAndUpdate(pit.below(3), Blocks.STONE.defaultBlockState());
			});
			server.runCommand(String.format(Locale.ROOT, "tp @a %.1f %d %.1f 180 -60", pit.getX() + 0.5, pit.getY() - 2, pit.getZ() + 0.5));
			context.waitTicks(10);
			connection.waitForChunksRender();
			server.runCommand("tick freeze");
			try {
				context.waitTicks(5);
				String camera = context.computeOnClient(client -> client.player.getEyePosition() + " yaw " + client.player.getYRot() + " pitch "
						+ client.player.getXRot());
				String loop = context.computeOnClient(client -> drawing(client, perch, MAIN));
				Jugcraft.LOGGER.info("[garden-check] below him: camera at {}, his loop {}", camera, loop);
				shot(context, "jugcraft_garden_buer_below");
			} finally {
				server.runCommand("tick unfreeze");
			}
		} finally {
			standSouth(server, perch);
			server.runOnServer(minecraft -> {
				ServerLevel level = minecraft.overworld();
				level.setBlockAndUpdate(pit.below(), Blocks.STONE.defaultBlockState());
				level.setBlockAndUpdate(pit.below(2), was.get(0));
				level.setBlockAndUpdate(pit.below(3), was.get(1));
			});
			context.waitTicks(10);
			restoreHud(context, hidden);
		}
	}

	/**
	 * GC-02's rule for Buer: the holder opens the perch from two blocks east, south, west and north of it in turn; each time,
	 * once his drawn body yaw is within FACING_BODY_TOLERANCE of the holder's bearing, the screen is closed, the world frozen,
	 * the drawn yaw logged and he is photographed from where his speaker stood.
	 */
	private static void facing(ClientGameTestContext context, TestDedicatedServerContext server, TestDedicatedServerConnection connection,
			BlockPos perch) {
		boolean hidden = hideHud(context);
		try {
			for (Facing side : FACINGS) {
				server.runCommand(String.format(Locale.ROOT, "tp @a %.1f %d %.1f %.1f 0", perch.getX() + 0.5 + 2 * side.dx(), perch.getY(),
						perch.getZ() + 0.5 + 2 * side.dz(), side.look()));
				context.waitTicks(OPEN_GAP);
				connection.waitForChunksRender();
				open(context, server, perch);
				context.waitFor(client -> Mth.degreesDifferenceAbs(bodyYaw(client, perch), side.yaw()) <= FACING_BODY_TOLERANCE, WAIT);
				context.getInput().pressKey(InputConstants.KEY_ESCAPE);
				context.waitForScreen(null);
				context.getInput().lookAt(perch.above());
				context.waitTicks(5);
				server.runCommand("tick freeze");
				try {
					context.waitTicks(5);
					float body = context.computeOnClient(client -> bodyYaw(client, perch));
					Jugcraft.LOGGER.info("[garden-check] facing {}: speaker's bearing {}, drawn body yaw {}", side.name(), side.yaw(), body);
					check(Mth.degreesDifferenceAbs(body, side.yaw()) <= FACING_BODY_TOLERANCE,
							"Spoken to from the " + side.name() + ", he is drawn facing his speaker: body " + body + " against " + side.yaw());
					shot(context, side.shot());
				} finally {
					server.runCommand("tick unfreeze");
				}
			}
		} finally {
			restoreHud(context, hidden);
		}
	}

	/** Chunk D's end: End asks first; Esc keeps the pact; yes ends it once and he departs with his gesture. */
	private static void ending(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch) {
		context.waitTicks(OPEN_GAP);
		open(context, server, perch);
		context.waitFor(client -> PactClient.state() != null && PactClient.state().mode() == PactStatePayload.KEEP, WAIT);
		press(context, "end");
		context.waitForScreen(ConfirmScreen.class);
		check(context.computeOnClient(client -> client.gui.screen().getFocused() instanceof AbstractWidget keep
				&& keep.getMessage().getString().equals(Component.translatable("screen.jugcraft.pact.end.no").getString())),
				"End asks first, the keyboard on Keep it");
		shot(context, "jugcraft_garden_buer_end_confirm");
		context.getInput().pressKey(InputConstants.KEY_ESCAPE);
		context.waitForScreen(PactScreen.class);
		context.waitTicks(5);
		check(server.computeOnServer(ConcordanceGardenPactClientGameTests::held), "Keep it (Esc) ends nothing");
		press(context, "end");
		context.waitForScreen(ConfirmScreen.class);
		tabToLabel(context, Component.translatable("screen.jugcraft.pact.end.yes").getString());
		PactStatePayload.Result ended = answer(context);
		context.waitFor(client -> BuerEntity.Gesture.DEPART.animation.equals(gestured(client, perch)), WAIT);
		boolean still = server.computeOnServer(ConcordanceGardenPactClientGameTests::held);
		int mode = context.computeOnClient(client -> PactClient.state().mode());
		Jugcraft.LOGGER.info("[garden-check] ended: {}, held {}, mode {}", ended.reason(), still, mode);
		check(ended.done() && "ended".equals(ended.reason()) && !still && mode == PactStatePayload.CLOSED,
				"Ending ends the pact once and he departs with his gesture: " + ended.reason());
		context.waitFor(client -> buer(client, perch) == null, WAIT);
		shot(context, "jugcraft_garden_buer_ended");
		context.getInput().pressKey(InputConstants.KEY_ESCAPE);
		context.waitForScreen(null);
	}

	/** Waits until his main controller draws {@code clip} past its blend, freezes the world, closes the screen if asked, checks and photographs it, thaws it. */
	private static void loopPose(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch, String clip, String name,
			boolean closeScreen) {
		context.waitFor(client -> clip.equals(drawing(client, perch, MAIN)) && !blending(client, perch, MAIN), WAIT);
		server.runCommand("tick freeze");
		try {
			if (closeScreen) {
				context.getInput().pressKey(InputConstants.KEY_ESCAPE);
				context.waitForScreen(null);
			}
			context.waitTicks(5);
			// No gesture drawn over the loop: one that had finished, rewound by the freeze, once hid it (run 40605564166).
			check(context.computeOnClient(client -> clip.equals(drawing(client, perch, MAIN)) && drawing(client, perch, GESTURES).isEmpty()),
					"Frozen while he draws " + clip + ", no gesture over it");
			shot(context, name);
		} finally {
			server.runCommand("tick unfreeze");
		}
	}

	/**
	 * The server fires {@code gesture}; the world is frozen at the middle of its telling pose ({@code from} to {@code to}
	 * seconds into it), where the client's gesture controller must be drawing it; photographed, thawed, and played out.
	 */
	private static void gesturePose(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch, BuerEntity.Gesture gesture,
			double from, double to, String name) {
		int middle = (int) Math.round((from + to) * 10.0);
		server.runOnServer(minecraft -> presence(minecraft, perch).gesture(gesture));
		context.waitTicks(middle);
		server.runCommand("tick freeze");
		try {
			context.waitTicks(5);
			double at = context.computeOnClient(client -> clipTime(client, perch, gesture));
			Jugcraft.LOGGER.info("[garden-check] his {} frozen {} s into its clip", gesture.id, at);
			check(at >= from && at <= to, "His " + gesture.id + " is caught in its telling pose, " + from + " to " + to + " s into it: " + at);
			shot(context, name);
		} finally {
			server.runCommand("tick unfreeze");
		}
		context.waitTicks(gesture.ticks - middle + 5);
	}

	/** How far into {@code gesture}'s clip his gesture controller is, or -1 when it draws another or none. */
	private static double clipTime(Minecraft client, BlockPos perch, BuerEntity.Gesture gesture) {
		AnimationController<BuerEntity> gestures = controller(client, perch, GESTURES);
		return gestures != null && gesture.clip().equals(drawing(client, perch, GESTURES)) ? gestures.getCurrentAnimationTime() : -1.0;
	}

	private static @Nullable AnimationController<BuerEntity> controller(Minecraft client, BlockPos perch, String name) {
		if (!(buer(client, perch) instanceof BuerEntity buer)) {
			return null;
		}
		AnimatableManager<BuerEntity> manager = buer.getAnimatableInstanceCache().getManagerForId(buer.getId());
		return manager.getAnimationControllers().get(name);
	}

	/** The clip one of his controllers is drawing now ("" when it draws nothing). */
	private static String drawing(Minecraft client, BlockPos perch, String name) {
		AnimationController<BuerEntity> controller = controller(client, perch, name);
		AnimationPoint point = controller == null || !controller.isAnimatingBones() ? null : controller.getCurrentAnimationPoint();
		return point == null ? "" : point.animation().name();
	}

	private static boolean blending(Minecraft client, BlockPos perch, String name) {
		AnimationController<BuerEntity> controller = controller(client, perch, name);
		return controller != null && controller.isTransitioning();
	}

	/** The gesture last triggered on him (it stays set after it plays). */
	private static @Nullable RawAnimation gestured(Minecraft client, BlockPos perch) {
		AnimationController<BuerEntity> gestures = controller(client, perch, GESTURES);
		return gestures == null ? null : gestures.getCurrentRawAnimation();
	}

	private static float bodyYaw(Minecraft client, BlockPos perch) {
		return buer(client, perch) instanceof LivingEntity him ? him.yBodyRot : Float.NaN;
	}

	/** Buer at {@code perch} on the server (the presence, else the visitor; never one leaving). */
	private static BuerEntity presence(MinecraftServer minecraft, BlockPos perch) {
		ServerLevel level = minecraft.overworld();
		PactSpiritEntity<?> found = ((PactPerchBlockEntity) level.getBlockEntity(perch)).presence(level);
		check(found instanceof BuerEntity, "Buer is on his perch: " + found);
		return (BuerEntity) found;
	}

	private static StrainTending.Memory memory(BuerEntity buer) {
		return buer.serviceMemory(StrainTending.Memory.class, StrainTending.Memory::new);
	}

	private static AgreementHolder holder(MinecraftServer minecraft, BlockPos perch) {
		return ((AgreementHolder.Holds) minecraft.overworld().getBlockEntity(perch)).holder();
	}

	private static VerdantBedBlockEntity bed(MinecraftServer minecraft, BlockPos at) {
		VerdantBedBlockEntity found = minecraft.overworld().getBlockEntity(at) instanceof VerdantBedBlockEntity bed ? bed : null;
		check(found != null, "The Verdant Bed is at " + at);
		return found;
	}

	/** Waits, a tick at a time, until {@code done} holds on the server. */
	private static void untilServer(ClientGameTestContext context, TestDedicatedServerContext server, Predicate<MinecraftServer> done, String what) {
		for (int i = 0; i < WAIT; i++) {
			if (server.computeOnServer(done::test)) {
				return;
			}
			context.waitTicks(1);
		}
		throw new AssertionError(what);
	}

	/** The camera for his body: where a speaker south of his perch stands, 2.5 blocks from it, facing north. */
	private static void standSouth(TestDedicatedServerContext server, BlockPos perch) {
		server.runCommand(String.format(Locale.ROOT, "tp @a %.1f %d %.1f 180 0", perch.getX() + 0.5, perch.getY(), perch.getZ() + 3.0));
	}

	private static boolean hideHud(ClientGameTestContext context) {
		boolean hidden = context.computeOnClient(client -> client.gui.hud.isHidden());
		context.runOnClient(client -> {
			if (!client.gui.hud.isHidden()) {
				client.gui.hud.toggle();
			}
		});
		return hidden;
	}

	private static void restoreHud(ClientGameTestContext context, boolean hidden) {
		context.runOnClient(client -> {
			if (client.gui.hud.isHidden() != hidden) {
				client.gui.hud.toggle();
			}
		});
	}

	private record Hearing(List<String> heard, SoundEventListener listener, boolean sounding, boolean subtitles) {
	}

	/** Subtitles shown and a listener on the sound manager before the refusal and the acceptance. */
	private static Hearing hear(ClientGameTestContext context) {
		List<String> heard = new CopyOnWriteArrayList<>();
		SoundEventListener listener = (sound, events, range) -> {
			Component subtitle = events.getSubtitle();
			if (subtitle != null) {
				heard.add(subtitle.getString());
			}
		};
		boolean subtitles = context.computeOnClient(client -> client.options.showSubtitles().get());
		boolean sounding = context.computeOnClient(client -> {
			client.options.showSubtitles().set(true);
			client.getSoundManager().addListener(listener);
			// The sound engine starts only with a sound device; without one (CI's runners) nothing plays and no subtitle shows.
			return client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F)) != SoundEngine.PlayResult.NOT_STARTED;
		});
		return new Hearing(heard, listener, sounding, subtitles);
	}

	/** His refuse and assent fired by the server, their subtitles his; heard where the machine can play sound. */
	private static void heard(ClientGameTestContext context, TestDedicatedServerContext server, Hearing hearing, BlockPos perch) {
		String refuse = context.computeOnClient(client -> Component.translatable("subtitles.jugcraft.buer.refuse").getString());
		String assent = context.computeOnClient(client -> Component.translatable("subtitles.jugcraft.buer.assent").getString());
		check(!refuse.startsWith("subtitles.") && !assent.startsWith("subtitles.")
				&& context.computeOnClient(client -> refuse.equals(subtitle(client, "buer.refuse")) && assent.equals(subtitle(client, "buer.assent"))),
				"His refuse and assent have their subtitles in the client's sounds: " + refuse + ", " + assent);
		List<String> cues = server.computeOnServer(minecraft -> presence(minecraft, perch).cues());
		Jugcraft.LOGGER.info("[garden-check] Buer's cues so far: {}", cues);
		check(cues.contains("refuse") && "assent".equals(cues.getLast()), "The server fired his refusal, then his assent: " + cues);
		try {
			if (hearing.sounding()) {
				context.waitFor(client -> hearing.heard().contains(refuse) && hearing.heard().contains(assent), WAIT);
				Jugcraft.LOGGER.info("[garden-check] subtitles heard: {}", hearing.heard());
			} else {
				Jugcraft.LOGGER.info("[garden-check] no sound device: the sound engine is off, so no subtitle can show; checked as defined and as fired");
			}
		} finally {
			context.runOnClient(client -> {
				client.getSoundManager().removeListener(hearing.listener());
				client.options.showSubtitles().set(hearing.subtitles());
			});
		}
	}

	private static String subtitle(Minecraft client, String event) {
		WeighedSoundEvents events = client.getSoundManager().getSoundEvent(Jugcraft.id(event));
		Component subtitle = events == null ? null : events.getSubtitle();
		return subtitle == null ? "" : subtitle.getString();
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

	/** D-05: Tab until the focused control's words are {@code label} (a vanilla screen's button, as the confirmation's). */
	private static void tabToLabel(ClientGameTestContext context, String label) {
		for (int i = 0; i < 8; i++) {
			if (context.computeOnClient(client -> client.gui.screen().getFocused() instanceof AbstractWidget widget
					&& widget.getMessage().getString().equals(label))) {
				return;
			}
			context.getInput().pressKey(InputConstants.KEY_TAB);
			context.waitTicks(1);
		}
		throw new AssertionError("Tab never reached " + label);
	}

	/**
	 * GC-P18: Tab until the pact screen's focused control's words are {@code label}. The chooser's entries sit in the
	 * screen's scrolling body, so the control is found by the screen's own focused key, not by its direct focus.
	 */
	private static void tabToNamed(ClientGameTestContext context, String label) {
		for (int i = 0; i < 48; i++) {
			if (context.computeOnClient(client -> client.gui.screen() instanceof PactScreen screen
					&& screen.control(screen.focusedKey()) instanceof AbstractWidget widget && widget.getMessage().getString().equals(label))) {
				return;
			}
			context.getInput().pressKey(InputConstants.KEY_TAB);
			context.waitTicks(1);
		}
		throw new AssertionError("Tab never reached " + label);
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

	/** The client's Buer within 4 blocks of {@code perch}, or null. */
	private static @Nullable Entity buer(Minecraft client, BlockPos perch) {
		EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(BUER));
		List<Entity> found = client.level.getEntities((Entity) null, new AABB(perch).inflate(4), entity -> entity.getType() == type);
		return found.isEmpty() ? null : found.getFirst();
	}

	private static String describe(@Nullable PactStatePayload state) {
		return state == null ? "no state" : "mode " + state.mode() + ", " + state.beats().size() + " beats, terms "
				+ state.terms().stream().map(PactStatePayload.Term::key).toList() + ", notes "
				+ state.notes().stream().map(Component::getString).toList();
	}

	/** The chooser's entries: each sign, ready or how many things are missing, and the notes. */
	private static String entries(@Nullable PactStatePayload state) {
		return state == null ? "no state" : state.patterns().stream()
				.map(choice -> choice.id() + (choice.ready() ? " ready" : " missing " + choice.amount())).toList()
				+ ", notes " + state.notes().stream().map(Component::getString).toList();
	}

	/** A state's lines in words: its terms' values, then its notes (a kept page's rows). */
	private static List<String> lines(PactStatePayload state) {
		List<String> out = new ArrayList<>();
		state.terms().forEach(term -> out.add(term.value().getString()));
		state.notes().forEach(note -> out.add(note.getString()));
		return out;
	}

	/** Each row's translation key (or its words when it has none). */
	private static List<String> keys(List<Component> rows) {
		return rows.stream().map(row -> row.getContents() instanceof TranslatableContents words ? words.getKey() : row.getString()).toList();
	}

	/** D-05: the index of the choice {@code id} in a state's list, or -1. */
	private static int index(List<PactStatePayload.Choice> choices, String id) {
		for (int i = 0; i < choices.size(); i++) {
			if (choices.get(i).id().equals(id)) {
				return i;
			}
		}
		return -1;
	}

	private static ItemStack stack(String line) {
		String[] part = line.split(" ");
		return new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(part[0])), Integer.parseInt(part[1]));
	}

	private static ServerPlayer player(MinecraftServer minecraft) {
		return minecraft.getPlayerList().getPlayers().getFirst();
	}

	private static boolean held(MinecraftServer minecraft) {
		return Pacts.holds(BoundWills.of(minecraft).ledger(), player(minecraft).getUUID(), BUER);
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}

	// ---------------------------------------------------------------- GC-05: any pact spirit at the perch (Bathin's flow)

	/** The client's pact spirit of entity type {@code id} within 4 of the perch, or null. */
	private static @Nullable PactSpiritEntity<?> spiritAt(Minecraft client, BlockPos perch, String id) {
		EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(id));
		List<Entity> found = client.level.getEntities((Entity) null, new AABB(perch).inflate(4), entity -> entity.getType() == type);
		return !found.isEmpty() && found.getFirst() instanceof PactSpiritEntity<?> spirit ? spirit : null;
	}

	private static @Nullable AnimationController<?> controllerOf(Minecraft client, BlockPos perch, String id, String name) {
		PactSpiritEntity<?> spirit = spiritAt(client, perch, id);
		if (spirit == null) {
			return null;
		}
		AnimatableManager<PactSpiritEntity<?>> manager = spirit.getAnimatableInstanceCache().getManagerForId(spirit.getId());
		return manager.getAnimationControllers().get(name);
	}

	/** The clip one of its controllers is drawing now ("" when it draws nothing). */
	private static String drawingOf(Minecraft client, BlockPos perch, String id, String name) {
		AnimationController<?> controller = controllerOf(client, perch, id, name);
		AnimationPoint point = controller == null || !controller.isAnimatingBones() ? null : controller.getCurrentAnimationPoint();
		return point == null ? "" : point.animation().name();
	}

	private static boolean blendingOf(Minecraft client, BlockPos perch, String id, String name) {
		AnimationController<?> controller = controllerOf(client, perch, id, name);
		return controller != null && controller.isTransitioning();
	}

	/** The gesture last triggered on it (it stays set after it plays). */
	private static @Nullable RawAnimation gesturedOf(Minecraft client, BlockPos perch, String id) {
		AnimationController<?> gestures = controllerOf(client, perch, id, GESTURES);
		return gestures == null ? null : gestures.getCurrentRawAnimation();
	}

	/** The pact spirit at the perch on the server (its presence, else its visitor; never one leaving), asserted there. */
	private static PactSpiritEntity<?> presenceOf(MinecraftServer minecraft, BlockPos perch) {
		PactSpiritEntity<?> spirit = ((PactPerchBlockEntity) minecraft.overworld().getBlockEntity(perch)).presence(minecraft.overworld());
		check(spirit != null, "A pact spirit is on the perch");
		return spirit;
	}

	private static boolean holds(MinecraftServer minecraft, String counterpart) {
		return Pacts.holds(BoundWills.of(minecraft).ledger(), player(minecraft).getUUID(), counterpart);
	}

	/**
	 * Waits until its main controller draws {@code clip} past its blend with no gesture drawn over it, freezes the world,
	 * closes the screen if asked, checks it still draws the loop and no gesture (loopPose's guard, for any spirit), and
	 * photographs it, thaws it.
	 */
	private static void loopPoseOf(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch, String id, String clip,
			String name, boolean closeScreen) {
		context.waitFor(client -> clip.equals(drawingOf(client, perch, id, MAIN)) && !blendingOf(client, perch, id, MAIN)
				&& drawingOf(client, perch, id, GESTURES).isEmpty(), WAIT);
		server.runCommand("tick freeze");
		try {
			if (closeScreen) {
				context.getInput().pressKey(InputConstants.KEY_ESCAPE);
				context.waitForScreen(null);
			}
			context.waitTicks(5);
			// No gesture drawn over the loop: one that had finished, rewound by the freeze, once hid Buer's (run 40605564166).
			String drawn = context.computeOnClient(client -> drawingOf(client, perch, id, MAIN));
			String over = context.computeOnClient(client -> drawingOf(client, perch, id, GESTURES));
			check(clip.equals(drawn) && over.isEmpty(), "Frozen while it draws " + clip + ", no gesture over it: " + drawn + " / " + over);
			shot(context, name);
		} finally {
			server.runCommand("tick unfreeze");
		}
	}

	/**
	 * The server fires {@code clip} on the spirit at the perch; the world is frozen at the middle of its telling pose
	 * ({@code from} to {@code to} seconds in), where the client's gesture controller must be drawing it; photographed, thawed
	 * and played out (gesturePose, for any spirit).
	 */
	private static void clipPose(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch, String id,
			PactSpiritEntity.Clip clip, double from, double to, String name) {
		int middle = (int) Math.round((from + to) * 10.0);
		String clipName = clip.animation().getAnimationStages().getFirst().animationName();
		server.runOnServer(minecraft -> presenceOf(minecraft, perch).gesture(clip));
		context.waitTicks(middle);
		server.runCommand("tick freeze");
		try {
			context.waitTicks(5);
			double at = context.computeOnClient(client -> clipName.equals(drawingOf(client, perch, id, GESTURES))
					? controllerOf(client, perch, id, GESTURES).getCurrentAnimationTime() : -1.0);
			Jugcraft.LOGGER.info("[garden-check] {}'s {} frozen {} s into its clip", id, clip.id(), at);
			check(at >= from && at <= to, "Its " + clip.id() + " is caught in its telling pose, " + from + " to " + to + " s into it: " + at);
			shot(context, name);
		} finally {
			server.runCommand("tick unfreeze");
		}
		context.waitTicks(clip.ticks() - middle + 5);
	}

	/**
	 * GC-02's rule for any spirit: from each side of the perch in turn (east, south, west, north), two blocks off and facing
	 * it, the player opens the perch; the spirit's drawn body turns to them (GC-P8); the screen is closed (the body keeps its
	 * facing) and the world frozen and photographed from the player's eyes, so from the spirit's front, as {@code shots} name.
	 */
	private static void spiritFacing(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch, String id, List<String> shots) {
		String[] sides = {"east", "south", "west", "north"};
		int[][] offsets = {{2, 0}, {0, 2}, {-2, 0}, {0, -2}};
		float[] bearings = {-90.0F, 0.0F, 90.0F, 180.0F};
		boolean hidden = hideHud(context);
		try {
			for (int i = 0; i < sides.length; i++) {
				float bearing = bearings[i];
				server.runCommand(String.format(Locale.ROOT, "tp @a %.1f %d %.1f %.1f 10", perch.getX() + 0.5 + offsets[i][0], perch.getY(),
						perch.getZ() + 0.5 + offsets[i][1], Mth.wrapDegrees(bearing + 180.0F)));
				context.waitTicks(OPEN_GAP);
				open(context, server, perch);
				context.waitFor(client -> spiritAt(client, perch, id) instanceof LivingEntity body
						&& Mth.degreesDifferenceAbs(body.yBodyRot, bearing) <= BATHIN_FACING_TOLERANCE, WAIT);
				float yaw = context.computeOnClient(client -> spiritAt(client, perch, id) instanceof LivingEntity body ? body.yBodyRot : Float.NaN);
				float off = Mth.degreesDifferenceAbs(yaw, bearing);
				Jugcraft.LOGGER.info("[garden-check] {} faces the {}: body yaw {} against bearing {} ({} off)", id, sides[i], yaw, bearing, off);
				check(off <= BATHIN_FACING_TOLERANCE, id + " turns to whoever speaks, from the " + sides[i] + ": " + yaw);
				context.getInput().pressKey(InputConstants.KEY_ESCAPE);
				context.waitForScreen(null);
				server.runCommand("tick freeze");
				try {
					context.waitTicks(3);
					shot(context, shots.get(i));
				} finally {
					server.runCommand("tick unfreeze");
				}
			}
		} finally {
			restoreHud(context, hidden);
		}
	}

	// ---------------------------------------------------------------- GC-05: Bathin (Dossier 2)

	/** GC-05: Bathin at the perch Buer left, from the chooser to his departure; his matter asked and answered on the way. */
	private static void bathin(ClientGameTestContext context, TestDedicatedServerContext server, TestDedicatedServerConnection connection,
			BlockPos perch) {
		BlockPos heart = perch.offset(-5, 0, 3);
		BlockPos gleaner = perch.offset(2, 0, 3);
		BlockPos road = perch.offset(-5, 0, 6);
		server.runOnServer(minecraft -> yard(minecraft, perch));
		standSouth(context, server, perch);
		connection.waitForChunksRender();
		nothingToCarry(context, server, perch);
		bathinChooser(context, server, perch, heart, gleaner);
		bathinConversation(context, server);
		bathinSizes(context);
		Hearing hearing = hear(context);
		bathinAcceptance(context, server);
		bathinHeard(context, server, hearing, perch);
		carrying(context, server, connection, perch, heart, gleaner);
		standSouth(context, server, perch);
		rosterAwaiting(context, server, perch, road);
		standSouth(context, server, perch);
		matter(context, server, perch);
		bathinJade(context, server, perch);
		bathinPoses(context, server, perch);
		spiritFacing(context, server, perch, BATHIN, List.of("jugcraft_garden_bathin_faces_east", "jugcraft_garden_bathin_faces_south",
				"jugcraft_garden_bathin_faces_west", "jugcraft_garden_bathin_faces_north"));
		standSouth(context, server, perch);
		bathinEnding(context, server, perch);
	}

	/** The player south of the perch, facing it (his front; the camera Buer's poses use), and the server given its moment. */
	private static void standSouth(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch) {
		standSouth(server, perch);
		context.waitTicks(10);
	}

	/**
	 * A stone floor and clear air from 7 west to 4 east and 3 north to 10 south of the perch (the perch and Buer's bed kept):
	 * room for Bathin's Heart, his Gleaner beyond its reach, the Heart with its own pylon his matter is about, and the
	 * carrying camera.
	 */
	private static void yard(MinecraftServer minecraft, BlockPos perch) {
		ServerLevel level = minecraft.overworld();
		BlockPos buersBed = perch.west(3);
		for (int dx = -7; dx <= 4; dx++) {
			for (int dz = -3; dz <= 10; dz++) {
				level.setBlockAndUpdate(perch.offset(dx, -1, dz), Blocks.STONE.defaultBlockState());
				for (int dy = 0; dy <= 3; dy++) {
					BlockPos at = perch.offset(dx, dy, dz);
					if (!at.equals(perch) && !at.equals(buersBed)) {
						level.setBlockAndUpdate(at, Blocks.AIR.defaultBlockState());
					}
				}
			}
		}
	}

	/** Dossier 2 item 6: with no Heart of the player's near, the chooser lists Bathin not ready, his refusal in his words. */
	private static void nothingToCarry(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch) {
		context.waitTicks(OPEN_GAP);
		open(context, server, perch);
		context.waitFor(client -> PactClient.state() != null && PactClient.state().mode() == PactStatePayload.INVITE, WAIT);
		String entries = context.computeOnClient(client -> entries(PactClient.state()));
		Jugcraft.LOGGER.info("[garden-check] chooser before his Heart: {}", entries);
		boolean listed = context.computeOnClient(client -> PactClient.state().patterns().stream()
				.anyMatch(choice -> choice.id().equals(BathinEntity.SIGN) && !choice.ready() && choice.amount() == 1)
				&& PactClient.state().notes().stream().anyMatch(note -> note.getString().contains(NOTHING_TO_CARRY)));
		check(listed, "Bathin listed, not ready, in his words: " + entries);
		fits(context, "the chooser without his Heart");
		named(context);
		shot(context, "jugcraft_garden_bathin_nothing_to_carry");
		context.getInput().pressKey(InputConstants.KEY_ESCAPE);
		context.waitForScreen(null);
	}

	/**
	 * His Heart (on the stone floor, no bed: it makes no Verdance by itself) and a Gleaner beyond its reach; the chooser lists
	 * him ready; chosen by keyboard, he is suddenly on the perch in a ring of dust.
	 */
	private static void bathinChooser(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch, BlockPos heart,
			BlockPos gleaner) {
		server.runOnServer(minecraft -> {
			ServerLevel level = minecraft.overworld();
			ServerPlayer player = player(minecraft);
			level.setBlockAndUpdate(heart, Garden.VERDANT_HEART.defaultBlockState());
			((VerdantHeartBlockEntity) level.getBlockEntity(heart)).awaken(player.getUUID());
			level.setBlockAndUpdate(gleaner, Garden.GLEANER.defaultBlockState());
			((GleanerBlockEntity) level.getBlockEntity(gleaner)).awaken(player.getUUID());
		});
		context.waitTicks(25);
		open(context, server, perch);
		context.waitFor(client -> PactClient.state() != null && PactClient.state().mode() == PactStatePayload.INVITE
				&& PactClient.state().patterns().stream().anyMatch(choice -> choice.id().equals(BathinEntity.SIGN) && choice.ready()), WAIT);
		String entries = context.computeOnClient(client -> entries(PactClient.state()));
		Jugcraft.LOGGER.info("[garden-check] chooser with his Heart and Gleaner: {}", entries);
		context.waitFor(client -> client.gui.screen() instanceof PactScreen, WAIT);
		fits(context, "the chooser with three spirits");
		named(context);
		shot(context, "jugcraft_garden_bathin_chooser");
		String name = context.computeOnClient(client -> Component.translatable("entity.jugcraft.bathin").getString());
		tabToNamed(context, name);
		context.getInput().pressKey(InputConstants.KEY_RETURN);
		context.waitFor(client -> PactClient.state() != null && PactClient.state().mode() == PactStatePayload.NEGOTIATE
				&& BathinEntity.SIGN.equals(PactClient.state().spirit().sign()) && spiritAt(client, perch, BATHIN) != null, WAIT);
		context.waitFor(client -> BathinEntity.Gesture.ARRIVE.animation.equals(gesturedOf(client, perch, BATHIN)), WAIT);
		RawAnimation arrived = context.computeOnClient(client -> gesturedOf(client, perch, BATHIN));
		int rings = server.computeOnServer(minecraft -> bathinAt(minecraft, perch).rings());
		Jugcraft.LOGGER.info("[garden-check] invited bathin: gesture {}, rings of dust {}", arrived, rings);
		check(BathinEntity.Gesture.ARRIVE.animation.equals(arrived) && rings == 1, "Chosen, he is suddenly on the perch, in a ring of dust");
	}

	/** His three beats, then every term by keyboard (Enter three times), none naming an astrolabe or resonance (GB23). */
	private static void bathinConversation(ClientGameTestContext context, TestDedicatedServerContext server) {
		String shown = context.computeOnClient(client -> describe(PactClient.state()));
		Jugcraft.LOGGER.info("[garden-check] bathin's offer: {}", shown);
		boolean whole = context.computeOnClient(client -> PactClient.state().beats().size() == 3 && PactClient.state().notes().isEmpty()
				&& PactClient.state().offers(PactRequestPayload.ACCEPT)
				&& PactClient.state().terms().stream().map(PactStatePayload.Term::key).toList().equals(PactStatePayload.TERMS));
		check(whole, "Bathin speaks three beats, nothing stands in the way, and his terms name every term, in order: " + shown);
		fits(context, "his beat at GUI scale 2");
		named(context);
		shot(context, "jugcraft_garden_bathin_conversation");
		tabTo(context, "next");
		for (int beat = 0; beat < 3; beat++) {
			context.getInput().pressKey(InputConstants.KEY_RETURN);
			context.waitTicks(3);
		}
		boolean onTerms = context.computeOnClient(client -> screen(client).page() == PactScreen.TERMS_PAGE && "para:0".equals(screen(client).focusedKey()));
		check(onTerms, "Enter three times reaches his terms, the keyboard on their first line, never on Accept");
		String words = context.computeOnClient(client -> String.join(" ", PactClient.state().terms().stream()
				.map(term -> term.value().getString()).toList()).toLowerCase(Locale.ROOT));
		Jugcraft.LOGGER.info("[garden-check] his terms: {}", words);
		check(!words.contains("astrolabe") && !words.contains("resonance") && words.contains("verdance"),
				"His terms name his service and no astrolabe or resonance: " + words);
		fits(context, "his terms at GUI scale 2");
		named(context);
		shot(context, "jugcraft_garden_bathin_terms");
		boolean held = server.computeOnServer(minecraft -> holds(minecraft, BATHIN));
		check(!held, "Hearing his terms records nothing");
	}

	/** His terms and a beat at GUI scale 4 (1920 x 1080) and in a 320 x 180 window. */
	private static void bathinSizes(ClientGameTestContext context) {
		int width = context.computeOnClient(client -> client.getWindow().getWidth());
		int height = context.computeOnClient(client -> client.getWindow().getHeight());
		context.getInput().resizeWindow(1920, 1080);
		context.runOnClient(client -> {
			client.options.guiScale().set(4);
			client.resizeGui();
		});
		context.waitTicks(10);
		int applied = context.computeOnClient(client -> client.getWindow().getGuiScale());
		check(applied == 4, "His high-scale shots are at GUI scale 4: " + applied);
		fits(context, "his terms at GUI scale 4");
		named(context);
		shot(context, "jugcraft_garden_bathin_terms_scale4");
		press(context, "back");
		fits(context, "his beat at GUI scale 4");
		named(context);
		shot(context, "jugcraft_garden_bathin_conversation_scale4");
		press(context, "next");
		context.runOnClient(client -> {
			client.options.guiScale().set(2);
			client.resizeGui();
		});
		context.getInput().resizeWindow(320, 180);
		context.waitTicks(10);
		String gui = context.computeOnClient(client -> client.getWindow().getGuiScaledWidth() + "x" + client.getWindow().getGuiScaledHeight());
		Jugcraft.LOGGER.info("[garden-check] his terms in a 320x180 window: GUI {}", gui);
		check("320x180".equals(gui), "A 320x180 window draws a 320x180 GUI: " + gui);
		fits(context, "his terms at 320x180");
		named(context);
		shot(context, "jugcraft_garden_bathin_terms_small");
		press(context, "back");
		fits(context, "his beat at 320x180");
		named(context);
		shot(context, "jugcraft_garden_bathin_conversation_small");
		press(context, "next");
		context.getInput().resizeWindow(width, height);
		context.waitTicks(10);
		boolean back = context.computeOnClient(client -> screen(client).page() == PactScreen.TERMS_PAGE);
		check(back, "Back on his terms");
	}

	/** Accept refused without his compact (worded without an astrolabe), then sealed with it: 2 ender pearls and 8 chaff, spent once. */
	private static void bathinAcceptance(ClientGameTestContext context, TestDedicatedServerContext server) {
		tabTo(context, "accept");
		PactStatePayload.Result refused = answer(context);
		String said = refused.message().getString();
		int refusedMode = context.computeOnClient(client -> PactClient.state().mode());
		Jugcraft.LOGGER.info("[garden-check] Accept without his compact: {} ({})", refused.reason(), said);
		check("cannot_afford".equals(refused.reason()) && !refused.done() && !said.toLowerCase(Locale.ROOT).contains("astrolabe")
				&& refusedMode == PactStatePayload.NEGOTIATE, "Without his compact Accept is refused, worded without an astrolabe: " + said);
		boolean heldAfterRefusal = server.computeOnServer(minecraft -> holds(minecraft, BATHIN));
		check(!heldAfterRefusal, "A refusal records and spends nothing");
		context.waitFor(client -> screen(client).control("result") instanceof PactScreen.Verdict verdict && verdict.refused(), WAIT);
		shot(context, "jugcraft_garden_bathin_refused");
		List<String> cost = context.computeOnClient(client -> PactClient.state().compact().stream()
				.map(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()) + " " + stack.getCount()).toList());
		int resonance = context.computeOnClient(client -> PactClient.state().resonance());
		Jugcraft.LOGGER.info("[garden-check] his compact: {} and {} resonance", cost, resonance);
		check(cost.equals(BATHIN_COMPACT) && resonance == 0, "His compact: 2 ender pearls and 8 Verdant Chaff, no resonance: " + cost);
		server.runOnServer(minecraft -> cost.forEach(line -> player(minecraft).getInventory().add(stack(line))));
		context.waitTicks(5);
		tabTo(context, "accept");
		PactStatePayload.Result sealed = answer(context);
		int sealedMode = context.computeOnClient(client -> PactClient.state().mode());
		check(sealed.done() && "sealed".equals(sealed.reason()) && sealedMode == PactStatePayload.KEEP, "Accepting with his compact seals the pact: " + sealed.reason());
		String spent = server.computeOnServer(minecraft -> holds(minecraft, BATHIN) + " "
				+ cost.stream().mapToInt(line -> player(minecraft).getInventory().countItem(stack(line).getItem())).sum());
		Jugcraft.LOGGER.info("[garden-check] sealed: held, compact left = {}", spent);
		check("true 0".equals(spent), "Sealed once, his compact spent once: " + spent);
		context.waitFor(client -> screen(client).control("result") instanceof PactScreen.Verdict verdict && !verdict.refused(), WAIT);
		fits(context, "his kept pact at GUI scale 2");
		named(context);
		shot(context, "jugcraft_garden_bathin_kept");
	}

	/** His refuse and assent fired by the server, their subtitles his, and his carry's; heard where the machine can play sound. */
	private static void bathinHeard(ClientGameTestContext context, TestDedicatedServerContext server, Hearing hearing, BlockPos perch) {
		String refuse = context.computeOnClient(client -> Component.translatable("subtitles.jugcraft.bathin.refuse").getString());
		String assent = context.computeOnClient(client -> Component.translatable("subtitles.jugcraft.bathin.assent").getString());
		String carry = context.computeOnClient(client -> Component.translatable("subtitles.jugcraft.bathin.work").getString());
		boolean defined = context.computeOnClient(client -> refuse.equals(subtitle(client, "bathin.refuse"))
				&& assent.equals(subtitle(client, "bathin.assent")) && carry.equals(subtitle(client, "bathin.work")));
		check(!refuse.startsWith("subtitles.") && !carry.startsWith("subtitles.") && defined,
				"His refuse, assent and carry have their subtitles in the client's sounds: " + refuse + ", " + assent + ", " + carry);
		List<String> cues = server.computeOnServer(minecraft -> bathinAt(minecraft, perch).cues());
		Jugcraft.LOGGER.info("[garden-check] Bathin's cues so far: {}", cues);
		check(cues.contains("refuse") && "assent".equals(cues.getLast()), "The server fired his refusal, then his assent: " + cues);
		try {
			if (hearing.sounding()) {
				context.waitFor(client -> hearing.heard().contains(refuse) && hearing.heard().contains(assent), WAIT);
				Jugcraft.LOGGER.info("[garden-check] subtitles heard: {}", hearing.heard());
			} else {
				Jugcraft.LOGGER.info("[garden-check] no sound device: the sound engine is off, so no subtitle can show; checked as defined and as fired");
			}
		} finally {
			context.runOnClient(client -> {
				client.getSoundManager().removeListener(hearing.listener());
				client.options.showSubtitles().set(hearing.subtitles());
			});
		}
	}

	/**
	 * Dossier 2 item 3 and detail b, the service at work: one carry by his own think on the server, 8 Verdance leaving the
	 * player's Heart and arriving in the far Gleaner in the same tick, both ends shimmering and nothing between. Frozen two
	 * client ticks later (the shimmer's signs reach the client on its next tick) and photographed from where both ends and he
	 * are seen. The Heart is left empty, so nothing more is carried while later steps photograph him.
	 */
	private static void carrying(ClientGameTestContext context, TestDedicatedServerContext server, TestDedicatedServerConnection connection,
			BlockPos perch, BlockPos heart, BlockPos gleaner) {
		context.getInput().pressKey(InputConstants.KEY_ESCAPE);
		context.waitForScreen(null);
		boolean hidden = hideHud(context);
		try {
			server.runCommand(String.format(Locale.ROOT, "tp @a %.1f %d %.1f 180 25", perch.getX() - 1.0, perch.getY() + 1, perch.getZ() + 9.5));
			context.waitTicks(10);
			connection.waitForChunksRender();
			String moved = server.computeOnServer(minecraft -> {
				ServerLevel level = minecraft.overworld();
				VerdantHeartBlockEntity source = (VerdantHeartBlockEntity) level.getBlockEntity(heart);
				GleanerBlockEntity target = (GleanerBlockEntity) level.getBlockEntity(gleaner);
				source.setVerdance(8);
				BathinEntity bathin = bathinAt(minecraft, perch);
				Status status = bathin.think(level, NOON, level.getGameTime());
				BathinEntity.Shimmer shimmer = bathin.lastShimmer();
				boolean both = shimmer != null && shimmer.at() == level.getGameTime() && shimmer.from().equals(heart) && shimmer.to().equals(gleaner);
				return status.id + " " + source.verdance() + " " + target.verdance() + " " + both;
			});
			Jugcraft.LOGGER.info("[garden-check] one carry: status, Heart, Gleaner, both ends shimmering that tick = {}", moved);
			check("working 0 8 true".equals(moved), "One carry: 8 Verdance left the Heart and reached the far Gleaner in one tick, both ends shimmering: " + moved);
			context.waitTicks(2);
			server.runCommand("tick freeze");
			try {
				context.waitTicks(5);
				shot(context, "jugcraft_garden_bathin_shimmer");
			} finally {
				server.runCommand("tick unfreeze");
			}
		} finally {
			restoreHud(context, hidden);
		}
	}

	/** GC-05: Bathin's presence at {@code perch}, asserted there (server side). */
	private static BathinEntity bathinAt(MinecraftServer minecraft, BlockPos perch) {
		ServerLevel level = minecraft.overworld();
		PactSpiritEntity<?> found = ((PactPerchBlockEntity) level.getBlockEntity(perch)).presence(level);
		check(found instanceof BathinEntity, "Bathin is on his perch: " + found);
		return (BathinEntity) found;
	}

	/**
	 * GC-05: a Heart of the player's beside its own pylon at {@code at} (on the yard's stone floor, no bed), within his area
	 * (the Bathin flow's Gleaner already there); his device index forgotten (it served since his sealing), then his think
	 * raises the matter. Server side.
	 */
	private static String raise(MinecraftServer minecraft, BlockPos perch, BlockPos at) {
		ServerLevel level = minecraft.overworld();
		ServerPlayer player = player(minecraft);
		level.setBlockAndUpdate(at, Garden.VERDANT_HEART.defaultBlockState());
		VerdantHeartBlockEntity heart = (VerdantHeartBlockEntity) level.getBlockEntity(at);
		heart.awaken(player.getUUID());
		heart.setVerdance(16);
		level.setBlockAndUpdate(at.east(), JugcraftConcordance.LEY_PYLON.defaultBlockState());
		((LeyPylonBlockEntity) level.getBlockEntity(at.east())).setOwner(player.getUUID());
		BathinEntity bathin = bathinAt(minecraft, perch);
		bathin.serviceMemory(VerdanceCarrying.Memory.class, VerdanceCarrying.Memory::new).forget();
		return bathin.think(level, NOON, level.getGameTime()).id;
	}

	/** GC-05: the roster names the waiting choice, opened by its key: Bathin's row says "awaiting your answer", the waiting icon. */
	private static void rosterAwaiting(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch, BlockPos at) {
		String raised = server.computeOnServer(minecraft -> raise(minecraft, perch, at));
		check("awaiting_answer".equals(raised), "His matter waits on the player: " + raised);
		// The roster's key is unbound by default, so it is bound for this one look and put back whatever fails.
		KeyMapping key = RosterClient.key();
		context.runOnClient(client -> {
			key.setKey(InputConstants.Type.KEYBOARD.getOrCreate(ROSTER_TEST_KEY));
			KeyMapping.resetMapping();
		});
		try {
			String clash = context.computeOnClient(client -> Arrays.stream(client.options.keyMappings)
					.filter(other -> other != key && other.same(key)).map(KeyMapping::getName).toList().toString());
			check(clash.equals("[]"), "The test's binding for the roster shares no other key: " + clash);
			context.getInput().pressKey(key);
			context.waitForScreen(RosterScreen.class);
			context.waitFor(client -> RosterClient.state() != null, WAIT);
			context.waitTicks(3);
			String awaiting = context.computeOnClient(client -> Component.translatable("compose.jugcraft.worker.status.awaiting_answer").getString());
			String rows = context.computeOnClient(client -> RosterClient.state().rows().stream()
					.map(row -> row.name().getString() + ": " + row.waiting().getString() + " (icon " + row.icon() + ")").toList().toString());
			Jugcraft.LOGGER.info("[garden-check] roster while his matter waits: {}", rows);
			boolean says = context.computeOnClient(client -> RosterClient.state().rows().stream()
					.anyMatch(row -> row.waiting().getString().equals(awaiting) && row.icon() == RosterStatePayload.WAITING));
			check(says && awaiting.contains("awaiting your answer"), "Bathin's row says awaiting your answer, with the waiting icon: " + rows);
			shot(context, "jugcraft_garden_roster_awaiting");
			context.getInput().pressKey(InputConstants.KEY_ESCAPE);
			context.waitForScreen(null);
		} finally {
			context.runOnClient(client -> {
				key.setKey(key.getDefaultKey());
				KeyMapping.resetMapping();
			});
		}
		String reading = server.computeOnServer(minecraft -> {
			ServerLevel level = minecraft.overworld();
			return PactReading.holder(level, (PactPerchBlockEntity) level.getBlockEntity(perch), level.getGameTime()).toString();
		});
		Jugcraft.LOGGER.info("[garden-check] Jade's reading of his perch while his matter waits: {}", reading);
		check(reading.contains("two_roads") && reading.contains("awaiting_answer"), "Jade's reading names the matter and the waiting: " + reading);
		if (FabricLoader.getInstance().isModLoaded("jade")) {
			boolean hidden = context.computeOnClient(client -> client.gui.hud.isHidden());
			context.runOnClient(client -> {
				if (client.gui.hud.isHidden()) {
					client.gui.hud.toggle();
				}
			});
			try {
				standSouth(server, perch);
				context.waitTicks(10);
				context.getInput().lookAt(perch);
				context.waitTicks(20);
				shot(context, "jugcraft_garden_bathin_matter_jade");
			} finally {
				restoreHud(context, hidden);
			}
		} else {
			Jugcraft.LOGGER.info("[garden-check] Jade is not loaded: no Jade shot of his matter");
		}
	}

	/**
	 * GC-05: his matter on his kept page, by keyboard: the row and the question with two answers, none kept; it fits at GUI 2,
	 * GUI 4 and 320x180, every control named; Enter on "take from it" keeps it at the perch (RESOLVE), marked; then "leave
	 * it" again, so later steps start from the safe course.
	 */
	private static void matter(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch) {
		context.waitTicks(OPEN_GAP);
		open(context, server, perch);
		context.waitFor(client -> PactClient.state() != null && PactClient.state().mode() == PactStatePayload.KEEP
				&& PactClient.state().spirit().answers().size() == 2, WAIT);
		String shown = context.computeOnClient(client -> PactClient.state().spirit().matter().getString() + " / "
				+ PactClient.state().spirit().answers().stream().map(answer -> answer.name().getString() + "=" + answer.amount()).toList() + " / "
				+ PactClient.state().notes().stream().map(Component::getString).toList());
		Jugcraft.LOGGER.info("[garden-check] his matter on the kept page: {}", shown);
		boolean asked = context.computeOnClient(client -> PactClient.state().notes().stream().anyMatch(note -> note.getString().contains("awaits your answer"))
				&& PactClient.state().spirit().answers().stream().allMatch(answer -> answer.amount() == 0)
				&& screen(client).control("matter") != null && screen(client).control("answer:0") != null && screen(client).control("answer:1") != null);
		check(asked, "The page says a choice waits and asks it, two answers, none kept: " + shown);
		tabTo(context, "matter");
		String narrated = context.computeOnClient(client -> screen(client).control("matter").getMessage().getString());
		check(!narrated.isBlank(), "The matter is a focusable, narrated line: " + narrated);
		fits(context, "his matter at GUI scale 2");
		named(context);
		shot(context, "jugcraft_garden_bathin_matter");
		int width = context.computeOnClient(client -> client.getWindow().getWidth());
		int height = context.computeOnClient(client -> client.getWindow().getHeight());
		context.getInput().resizeWindow(1920, 1080);
		context.runOnClient(client -> {
			client.options.guiScale().set(4);
			client.resizeGui();
		});
		context.waitTicks(10);
		int applied = context.computeOnClient(client -> client.getWindow().getGuiScale());
		check(applied == 4, "At GUI scale 4: " + applied);
		fits(context, "his matter at GUI scale 4");
		named(context);
		shot(context, "jugcraft_garden_bathin_matter_scale4");
		context.runOnClient(client -> {
			client.options.guiScale().set(2);
			client.resizeGui();
		});
		context.getInput().resizeWindow(320, 180);
		context.waitTicks(10);
		String gui = context.computeOnClient(client -> client.getWindow().getGuiScaledWidth() + "x" + client.getWindow().getGuiScaledHeight());
		Jugcraft.LOGGER.info("[garden-check] his matter in a 320x180 window: GUI {}", gui);
		check("320x180".equals(gui), "A 320x180 window draws a 320x180 GUI: " + gui);
		fits(context, "his matter at 320x180");
		named(context);
		shot(context, "jugcraft_garden_bathin_matter_small");
		context.getInput().resizeWindow(width, height);
		context.waitTicks(10);
		tabTo(context, "answer:1");
		PactStatePayload.Result claimed = answer(context);
		String kept = server.computeOnServer(minecraft -> String.valueOf(((PactPerchBlockEntity) minecraft.overworld().getBlockEntity(perch)).accord()));
		boolean marked = context.computeOnClient(client -> PactClient.state().spirit().answers().get(0).amount() == 0
				&& PactClient.state().spirit().answers().get(1).amount() == 1);
		check(claimed.done() && "resolved".equals(claimed.reason()) && "claim".equals(kept) && marked,
				"Enter on take from it keeps it at the perch, marked: " + claimed.reason() + " " + kept);
		context.waitFor(client -> screen(client).control("result") instanceof PactScreen.Verdict verdict && !verdict.refused(), WAIT);
		String focused = context.computeOnClient(client -> screen(client).focusedKey());
		check("answer:1".equals(focused), "The keyboard stays on the answer it pressed: " + focused);
		shot(context, "jugcraft_garden_bathin_answered");
		tabTo(context, "answer:0");
		PactStatePayload.Result yielded = answer(context);
		String back = server.computeOnServer(minecraft -> String.valueOf(((PactPerchBlockEntity) minecraft.overworld().getBlockEntity(perch)).accord()));
		check(yielded.done() && "yield".equals(back), "Changed back at once: leave it: " + back);
		context.getInput().pressKey(InputConstants.KEY_ESCAPE);
		context.waitForScreen(null);
	}

	/** Jade's readings of his perch and of him (read on the server: no Jade class); with Jade loaded, his overlay. */
	private static void bathinJade(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch) {
		String readings = server.computeOnServer(minecraft -> {
			ServerLevel level = minecraft.overworld();
			CompoundTag ofPerch = PactReading.holder(level, (PactPerchBlockEntity) level.getBlockEntity(perch), level.getGameTime());
			CompoundTag ofHim = PactReading.presence(level, bathinAt(minecraft, perch), level.getGameTime());
			String both = ofPerch + " " + ofHim;
			boolean right = "kept".equals(ofPerch.getStringOr("standing", "")) && BATHIN.equals(ofPerch.getStringOr("counterpart", ""))
					&& VerdanceCarrying.WORK.equals(ofPerch.getStringOr("service", "")) && "active".equals(ofPerch.getStringOr("state", ""))
					&& ofPerch.getIntOr("due_days", -1) == 7 && "kept".equals(ofHim.getStringOr("standing", ""))
					&& PactReading.KEYS.containsAll(ofPerch.keySet()) && PactReading.KEYS.containsAll(ofHim.keySet())
					&& !both.contains(player(minecraft).getUUID().toString()) && !both.contains(player(minecraft).getName().getString());
			return (right ? "ok " : "bad ") + both;
		});
		Jugcraft.LOGGER.info("[garden-check] Jade's readings of his perch and of Bathin: {}", readings);
		check(readings.startsWith("ok"), "Jade's readings: his pact, service, state and next offering, naming no player: " + readings);
		if (FabricLoader.getInstance().isModLoaded("jade")) {
			boolean hidden = context.computeOnClient(client -> client.gui.hud.isHidden());
			context.runOnClient(client -> {
				if (client.gui.hud.isHidden()) {
					client.gui.hud.toggle();
				}
			});
			try {
				standSouth(server, perch);
				context.waitTicks(10);
				context.getInput().lookAt(perch.above());
				context.waitTicks(20);
				shot(context, "jugcraft_garden_bathin_jade");
			} finally {
				restoreHud(context, hidden);
			}
		} else {
			Jugcraft.LOGGER.info("[garden-check] Jade is not loaded: no Jade shot of Bathin");
		}
	}

	/**
	 * His body from his front (south of the perch, facing north; he faces whoever opened it there): each loop as the server
	 * drives it and each gesture frozen inside its telling pose, the HUD hidden. Waiting is posed with the Concordance
	 * switched off (he serves at any hour, so there is no night to sleep through); his astir loop is not posed (an offering
	 * due would change the upkeep later steps read, as Stolas's and Buer's: GC-P29). His depart and arrive show his ring of
	 * dust (the server's sign, frozen with the world).
	 */
	private static void bathinPoses(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch) {
		boolean hidden = hideHud(context);
		try {
			standSouth(server, perch);
			context.waitTicks(OPEN_GAP);
			open(context, server, perch);
			context.waitFor(client -> spiritAt(client, perch, BATHIN) instanceof LivingEntity body
					&& Mth.degreesDifferenceAbs(body.yBodyRot, 0.0F) <= BATHIN_FACING_TOLERANCE, WAIT);
			context.getInput().pressKey(InputConstants.KEY_ESCAPE);
			context.waitForScreen(null);
			context.getInput().lookAt(perch.above());
			loopPoseOf(context, server, perch, BATHIN, BATHIN_IDLE, "jugcraft_garden_bathin_idle", false);
			context.waitTicks(OPEN_GAP);
			open(context, server, perch);
			context.waitFor(client -> spiritAt(client, perch, BATHIN) instanceof BathinEntity bathin && bathin.listening(), WAIT);
			loopPoseOf(context, server, perch, BATHIN, BATHIN_LISTEN, "jugcraft_garden_bathin_listen", true);
			context.waitFor(client -> BATHIN_IDLE.equals(drawingOf(client, perch, BATHIN, MAIN)), WAIT);
			AgreementHolder.Toggle suspended = server.computeOnServer(minecraft -> PactKeeping.suspend(minecraft.overworld(), player(minecraft),
					holder(minecraft, perch), minecraft.overworld().getGameTime()));
			check(suspended == AgreementHolder.Toggle.SUSPENDED, "Its holder suspends the pact: " + suspended);
			loopPoseOf(context, server, perch, BATHIN, BATHIN_SUSPENDED, "jugcraft_garden_bathin_suspended", false);
			AgreementHolder.Toggle resumed = server.computeOnServer(minecraft -> PactKeeping.resume(minecraft.overworld(), player(minecraft),
					holder(minecraft, perch), minecraft.overworld().getGameTime()));
			check(resumed == AgreementHolder.Toggle.RESUMED, "and resumes it: " + resumed);
			context.waitFor(client -> BATHIN_IDLE.equals(drawingOf(client, perch, BATHIN, MAIN)), WAIT);
			boolean on = server.computeOnServer(minecraft -> JugcraftConfig.isFeatureEnabled(JugcraftConcordance.FEATURE));
			server.runOnServer(minecraft -> JugcraftConfig.setFeatureEnabled(JugcraftConcordance.FEATURE, false));
			try {
				loopPoseOf(context, server, perch, BATHIN, BATHIN_WAITING, "jugcraft_garden_bathin_waiting", false);
			} finally {
				server.runOnServer(minecraft -> JugcraftConfig.setFeatureEnabled(JugcraftConcordance.FEATURE, on));
			}
			context.waitFor(client -> BATHIN_IDLE.equals(drawingOf(client, perch, BATHIN, MAIN)), WAIT);
			clipPose(context, server, perch, BATHIN, BathinEntity.Gesture.INSPECT, 0.4, 1.2, "jugcraft_garden_bathin_inspect");
			clipPose(context, server, perch, BATHIN, BathinEntity.Gesture.ASSENT, 0.3, 0.9, "jugcraft_garden_bathin_assent");
			clipPose(context, server, perch, BATHIN, BathinEntity.Gesture.REFUSE, 0.3, 1.0, "jugcraft_garden_bathin_refuse");
			clipPose(context, server, perch, BATHIN, BathinEntity.Gesture.WORK, 0.15, 0.75, "jugcraft_garden_bathin_work");
			// He leaves (depart holds its last frame: nothing of him drawn) and comes back (his arrival replaces it), each in a ring of dust.
			clipPose(context, server, perch, BATHIN, BathinEntity.Gesture.DEPART, 0.25, 0.75, "jugcraft_garden_bathin_depart");
			clipPose(context, server, perch, BATHIN, BathinEntity.Gesture.ARRIVE, 0.1, 0.6, "jugcraft_garden_bathin_arrive");
			context.waitFor(client -> drawingOf(client, perch, BATHIN, GESTURES).isEmpty()
					&& BATHIN_IDLE.equals(drawingOf(client, perch, BATHIN, MAIN)), WAIT);
			boolean kept = server.computeOnServer(minecraft -> holds(minecraft, BATHIN) && bathinAt(minecraft, perch).isAlive());
			check(kept, "His gestures change nothing: the pact is kept and he is on his perch");
		} finally {
			restoreHud(context, hidden);
		}
	}

	/** Dossier 2 item 6: End asks first; then his coil tightens and he is gone in a ring of dust; the pact ends once. */
	private static void bathinEnding(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch) {
		context.waitTicks(OPEN_GAP);
		open(context, server, perch);
		context.waitFor(client -> PactClient.state() != null && PactClient.state().mode() == PactStatePayload.KEEP, WAIT);
		UUID him = server.computeOnServer(minecraft -> bathinAt(minecraft, perch).getUUID());
		press(context, "end");
		context.waitForScreen(ConfirmScreen.class);
		boolean keepFocused = context.computeOnClient(client -> client.gui.screen().getFocused() instanceof AbstractWidget keep
				&& keep.getMessage().getString().equals(Component.translatable("screen.jugcraft.pact.end.no").getString()));
		check(keepFocused, "End asks first, the keyboard on Keep it");
		shot(context, "jugcraft_garden_bathin_end_confirm");
		context.getInput().pressKey(InputConstants.KEY_ESCAPE);
		context.waitForScreen(PactScreen.class);
		context.waitTicks(5);
		boolean stillHeld = server.computeOnServer(minecraft -> holds(minecraft, BATHIN));
		check(stillHeld, "Keep it (Esc) ends nothing");
		press(context, "end");
		context.waitForScreen(ConfirmScreen.class);
		tabToLabel(context, Component.translatable("screen.jugcraft.pact.end.yes").getString());
		PactStatePayload.Result ended = answer(context);
		context.waitFor(client -> BathinEntity.Gesture.DEPART.animation.equals(gesturedOf(client, perch, BATHIN)), WAIT);
		RawAnimation going = context.computeOnClient(client -> gesturedOf(client, perch, BATHIN));
		String after = server.computeOnServer(minecraft -> holds(minecraft, BATHIN) + " "
				+ (minecraft.overworld().getEntity(him) instanceof BathinEntity left ? "departing " + left.departing() + ", rings " + left.rings() : "gone"));
		int endedMode = context.computeOnClient(client -> PactClient.state().mode());
		Jugcraft.LOGGER.info("[garden-check] ended: {}; held, then his presence = {}", ended.reason(), after);
		check(ended.done() && "ended".equals(ended.reason()) && BathinEntity.Gesture.DEPART.animation.equals(going)
				&& endedMode == PactStatePayload.CLOSED && after.startsWith("false"), "Ending ends the pact once; his coil tightens and he goes: " + after);
		context.waitFor(client -> spiritAt(client, perch, BATHIN) == null, WAIT);
		shot(context, "jugcraft_garden_bathin_ended");
		context.getInput().pressKey(InputConstants.KEY_ESCAPE);
		context.waitForScreen(null);
	}
}
