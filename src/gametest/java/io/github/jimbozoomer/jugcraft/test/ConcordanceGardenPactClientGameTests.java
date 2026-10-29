package io.github.jimbozoomer.jugcraft.test;

import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.state.AnimationPoint;
import com.geckolib.renderer.GeoEntityRenderer;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.client.ConcordanceClientOptions;
import io.github.jimbozoomer.jugcraft.client.KeepFirstConfirmScreen;
import io.github.jimbozoomer.jugcraft.client.MagicScreen;
import io.github.jimbozoomer.jugcraft.client.PactClient;
import io.github.jimbozoomer.jugcraft.client.PactScreen;
import io.github.jimbozoomer.jugcraft.client.PactSpiritRenderers;
import io.github.jimbozoomer.jugcraft.client.RosterClient;
import io.github.jimbozoomer.jugcraft.client.RosterScreen;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceProgress;
import io.github.jimbozoomer.jugcraft.concordance.JugcraftConcordance;
import io.github.jimbozoomer.jugcraft.concordance.LeyPylonBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.garden.Garden;
import io.github.jimbozoomer.jugcraft.concordance.garden.GleanerBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.garden.OrganismCropBlock;
import io.github.jimbozoomer.jugcraft.concordance.garden.VerdantBedBlock;
import io.github.jimbozoomer.jugcraft.concordance.garden.VerdantBedBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.garden.VerdantHeartBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.rules.ResearchState;
import io.github.jimbozoomer.jugcraft.concordance.sky.AstralClaims;
import io.github.jimbozoomer.jugcraft.concordance.spirits.AgreementHolder;
import io.github.jimbozoomer.jugcraft.concordance.spirits.BathinEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.BoundWills;
import io.github.jimbozoomer.jugcraft.concordance.spirits.BuerEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.ForasEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.GardenPacts;
import io.github.jimbozoomer.jugcraft.concordance.spirits.GraftKeeping;
import io.github.jimbozoomer.jugcraft.concordance.spirits.HabitatReading;
import io.github.jimbozoomer.jugcraft.concordance.spirits.MaraxEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactKeeping;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactMenu;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactPerchBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactReading;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactRequestPayload;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactSpiritEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactStatePayload;
import io.github.jimbozoomer.jugcraft.concordance.spirits.RosterStatePayload;
import io.github.jimbozoomer.jugcraft.concordance.spirits.StolasEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.StrainTending;
import io.github.jimbozoomer.jugcraft.concordance.spirits.VerdanceCarrying;
import io.github.jimbozoomer.jugcraft.concordance.spirits.WorkerRoster;
import io.github.jimbozoomer.jugcraft.concordance.worker.Pacts;
import io.github.jimbozoomer.jugcraft.concordance.worker.Status;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerConnection;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.InputType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.ScreenRectangle;
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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
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
 * <li><b>Marax</b> (GC-06; docs/features/marax-pact.md), at the same perch: listed not ready with nothing planted, then
 * chosen by keyboard and treading up two blocks beside the perch; his conversation at three sizes, refused and sealed; a
 * reading on his page at three sizes, drawn as its one change, with his familiar frozen over the bed it names; Jade; eleven
 * poses, his night look among them; four bearings round where he stands; the end ({@code jugcraft_garden_marax_*}).</li>
 * </ul>
 * The helpers that name no spirit are Chunk D's, copied (its file stays as it is). The perch is opened by the server's use of
 * it (no aimed click), each open at least its rate gate's ten ticks after the last. Since the magic interfaces' unit 3
 * fixes: every chooser's cards are held wholly in view and the chooser's placement is checked for one to seven cards at
 * every size; each spirit's first beat draws its portrait whole; the facing pictures are framed whole from along each
 * speaker's bearing with nothing before the face; every picture but the tooltip's is taken with the cursor parked where
 * no control is. The narrator is not heard, and one client is not two.
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
				// The magic interfaces: no spirit's beats heard before, so each perch opens on them (spec 1.5).
				context.runOnClient(client -> ConcordanceClientOptions.forgetBeats());
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
				// GC-06 unit 3
				// GC-06: Marax at the same perch, chosen from the chooser (four spirits), to his departure.
				marax(context, server, connection, perch);
				// GC-07 unit 4
				// GC-07: Foras at the same perch, standing beside it, from the chooser to his departure.
				foras(context, server, connection, perch);
				// GC-08 unit 5
				// GC-09 unit 6
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
		cardsWhole(context, "the chooser without a bed");
		named(context);
		reasons(context);
		budget(context, chooserWords(context), "the chooser without a bed");
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
		cardsWhole(context, "the chooser at GUI scale 2");
		placements(context);
		named(context);
		reasons(context);
		budget(context, chooserWords(context), "the chooser");
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
		cardsWhole(context, "the chooser at GUI scale 4");
		named(context);
		budget(context, chooserWords(context), "the chooser at GUI scale 4");
		shot(context, "jugcraft_garden_chooser_scale4");
		context.runOnClient(client -> {
			client.options.guiScale().set(2);
			client.resizeGui();
		});
		context.getInput().resizeWindow(320, 180);
		context.waitTicks(10);
		context.waitFor(client -> client.gui.screen() instanceof PactScreen, WAIT);
		fits(context, "the chooser at 320x180");
		cardsWhole(context, "the chooser at 320x180");
		named(context);
		budget(context, chooserWords(context), "the chooser at 320x180");
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
		portraitWhole(context, BUER);
		budget(context, BEAT_WORDS, "his first beat");
		shot(context, "jugcraft_garden_buer_conversation");
		// Spec 1.5: his first beat's Terms control skips to his terms, the keyboard on their first line, never on Accept.
		press(context, "skip");
		check(context.computeOnClient(client -> screen(client).page() == PactScreen.TERMS_PAGE && "para:0".equals(screen(client).focusedKey())),
				"Terms from his first beat reaches his terms, the keyboard on their first line, never on Accept");
		fits(context, "the terms at GUI scale 2");
		named(context);
		budget(context, TERMS_WORDS, "his terms");
		shot(context, "jugcraft_garden_buer_terms");
		String words = context.computeOnClient(client -> String.join(" ", PactClient.state().terms().stream()
				.map(term -> term.value().getString()).toList()).toLowerCase(Locale.ROOT));
		Jugcraft.LOGGER.info("[garden-check] his terms: {}", words);
		check(!words.contains("astrolabe") && !words.contains("resonance"), "His terms ask for no astrolabe and no resonance (GB23): " + words);
		check(!server.computeOnServer(ConcordanceGardenPactClientGameTests::held), "Hearing his terms records nothing");
		more(context);
	}

	/**
	 * The magic interfaces (spec 4.5, D-S15 amended): More shows the six standing lines in place of the chips, every term then a
	 * control (all fourteen), the price still in view; it survives a state packet (the server's answer to a CHOOSE rebuilds
	 * the screen); Less puts the chips back.
	 */
	private static void more(ClientGameTestContext context) {
		press(context, "more");
		List<String> missing = context.computeOnClient(client -> PactStatePayload.TERMS.stream()
				.filter(key -> screen(client).control("term:" + key) == null).toList());
		check(context.computeOnClient(client -> screen(client).more()) && missing.isEmpty(), "More shows every term, the six standing lines among them: missing " + missing);
		budget(context, MORE_WORDS, "his terms with More");
		PactStatePayload before = context.computeOnClient(client -> PactClient.state());
		context.waitTicks(5);
		context.runOnClient(client -> PactClient.request(screen(client).getMenu(), PactRequestPayload.CHOOSE, 0));
		context.waitFor(client -> PactClient.state() != null && PactClient.state() != before, WAIT);
		context.waitTicks(3);
		check(context.computeOnClient(client -> screen(client).more() && screen(client).control("term:suspension") != null
				&& "more".equals(screen(client).focusedKey())), "More survives a state packet, the keyboard still on it");
		press(context, "more");
		check(context.computeOnClient(client -> !screen(client).more() && screen(client).control("term:standing") != null),
				"Less puts the chips back, the standing terms one chip");
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
		budget(context, TERMS_WORDS, "his terms at GUI scale 4");
		shot(context, "jugcraft_garden_buer_terms_scale4");
		press(context, "back");
		fits(context, "a beat at GUI scale 4");
		named(context);
		budget(context, BEAT_WORDS, "his last beat at GUI scale 4");
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
		budget(context, TERMS_WORDS, "his terms at 320x180");
		shot(context, "jugcraft_garden_buer_terms_small");
		press(context, "back");
		fits(context, "a beat in a 320x180 window");
		named(context);
		budget(context, BEAT_WORDS, "his last beat at 320x180");
		shot(context, "jugcraft_garden_buer_conversation_small");
		press(context, "next");
		context.getInput().resizeWindow(width, height);
		context.waitTicks(10);
		check(context.computeOnClient(client -> screen(client).page() == PactScreen.TERMS_PAGE), "Back on his terms");
	}

	/** Accept refused without his compact and spending nothing; given it, sealed once and spent once. */
	private static void acceptance(ClientGameTestContext context, TestDedicatedServerContext server) {
		// Spec 4.5: short of his compact, its materials are marked before Accept (display only: the server still decides).
		List<String> drawn = context.computeOnClient(client -> screen(client).drawn());
		check(drawn.stream().anyMatch(words -> words.matches("(.* )?0/4( .*)?")), "Short of his compact, it is marked before Accept: " + drawn);
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
		budget(context, TERMS_WORDS, "his refusal");
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
		budget(context, keptWords(context), "his kept pact");
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
		// Spec 2.12: a kept pact opens on how it stands, note:0, whose message is the server's state row whole.
		context.waitFor(client -> client.gui.screen() instanceof PactScreen kept && "note:0".equals(kept.focusedKey()), WAIT);
		check(context.computeOnClient(client -> screen(client).control("note:0").getMessage().getString().equals(PactClient.state().notes().getFirst().getString())),
				"His kept page opens on how it stands, its whole row the message");
		named(context);
		fits(context, "the kept page with his complaint");
		budget(context, keptWords(context), "his kept page with his complaint");
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
	 * the drawn yaw logged and he is photographed along his speaker's bearing, framed whole ({@link #frame}; the unit 3
	 * fixes: from where his speaker stood his petals ran off the top), the holder then back where they spoke from.
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
				Vec3 speaker = new Vec3(perch.getX() + 0.5 + 2 * side.dx(), perch.getY(), perch.getZ() + 0.5 + 2 * side.dz());
				server.runCommand("tick freeze");
				try {
					context.waitTicks(5);
					float body = context.computeOnClient(client -> bodyYaw(client, perch));
					Jugcraft.LOGGER.info("[garden-check] facing {}: speaker's bearing {}, drawn body yaw {}", side.name(), side.yaw(), body);
					check(Mth.degreesDifferenceAbs(body, side.yaw()) <= FACING_BODY_TOLERANCE,
							"Spoken to from the " + side.name() + ", he is drawn facing his speaker: body " + body + " against " + side.yaw());
					frame(context, server, perch, BUER, speaker, side.name());
					shot(context, side.shot());
				} finally {
					land(context, server, speaker);
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
		confirmBudget(context);
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
		budget(context, ENDED_WORDS, "the ended page");
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

	/** The magic interfaces' word budgets (spec 1.1): a beat page, his terms (a refusal included), More, the ended page. */
	private static final int BEAT_WORDS = 30;
	private static final int TERMS_WORDS = 50;
	private static final int MORE_WORDS = 70;
	private static final int ENDED_WORDS = 30;

	/**
	 * The chooser's budget: its title and Close (4 words), and at most 7 a card (a name, "Not yet" and a reason of at most 4
	 * words). Spec 1.1's 25 is three cards'; a session decision (Marax's review) lets it grow with the spirits listed, as a
	 * fourth card is drawn once Marax is offered (docs/features/magic-interfaces.md, Decisions), reviewable at the checkpoint.
	 */
	private static int chooserWords(ClientGameTestContext context) {
		settle(context);
		return 4 + 7 * context.computeOnClient(client -> PactClient.state().patterns().size());
	}

	/** A kept page's budget: 45, or 48 in the compact layout, which writes what is paid ahead as "0/4 ahead" (spec 1.1). */
	private static int keptWords(ClientGameTestContext context) {
		return context.computeOnClient(client -> screen(client).compact()) ? 48 : 45;
	}

	/**
	 * The words of a pending first task's row (at most 12, its act drawn short: check_magic_ui renders it with every act):
	 * the spec's kept-page budget was measured without one beside a matter, so a matter's shots allow that row's words too,
	 * a session decision recorded in docs/features/magic-interfaces.md (Decisions), reviewable at the checkpoint.
	 */
	private static int firstTask(ClientGameTestContext context) {
		return context.computeOnClient(client -> screen(client).drawn().stream().filter(line -> line.startsWith("First task"))
				.mapToInt(line -> (int) Arrays.stream(line.split("\\s+")).filter(token -> token.chars().anyMatch(Character::isLetterOrDigit)).count())
				.sum());
	}

	/**
	 * Waits until the pact screen shows the state the client holds and awaits no answer: a state packet marks it to rebuild
	 * on the next tick, and an awaited answer draws its own line, so the words, controls and budgets read the settled page.
	 */
	private static void settle(ClientGameTestContext context) {
		context.waitFor(client -> !PactClient.waiting() && (!(client.gui.screen() instanceof MagicScreen magic) || magic.settled()), WAIT);
	}

	/** Spec 1.1, 4.5: the pact screen draws at most {@code most} words without hovering, counted from what it drew. */
	private static void budget(ClientGameTestContext context, int most, String what) {
		settle(context);
		List<String> drawn = context.computeOnClient(client -> screen(client).drawn());
		int words = context.computeOnClient(client -> screen(client).words());
		Jugcraft.LOGGER.info("[garden-check] {} draws {} words (at most {}): {}", what, words, most, drawn);
		check(words <= most, what + " draws at most " + most + " words (spec 1.1): " + words + " " + drawn);
	}

	/** Spec 1.1: the end confirmation draws at most 25 words (its question, two lines, its two buttons). */
	private static void confirmBudget(ClientGameTestContext context) {
		List<String> drawn = context.computeOnClient(client -> client.gui.screen() instanceof KeepFirstConfirmScreen confirm ? confirm.drawn() : List.of());
		long words = drawn.stream().flatMap(line -> Arrays.stream(line.split("\\s+"))).filter(token -> token.chars().anyMatch(Character::isLetterOrDigit)).count();
		Jugcraft.LOGGER.info("[garden-check] the end confirmation draws {} words: {}", words, drawn);
		check(!drawn.isEmpty() && words <= 25, "The end confirmation draws at most 25 words: " + words + " " + drawn);
	}

	/**
	 * Spec 2.14: the chooser draws "Not yet" on every card not ready, and its reason under it, without a hover, wherever its
	 * card has room; since the unit 3 fixes a card with no room for its reason's lines carries it in its tip and narration
	 * (the server's whole sentence; PactScreen.tippedReasons), so seven cards stand whole on the smallest screen.
	 */
	private static void reasons(ClientGameTestContext context) {
		settle(context);
		String notYet = context.computeOnClient(client -> Component.translatable("screen.jugcraft.pact.short.not_yet").getString());
		List<String> drawn = context.computeOnClient(client -> screen(client).drawn());
		long waiting = context.computeOnClient(client -> PactClient.state().patterns().stream().filter(choice -> !choice.ready()).count());
		long marked = drawn.stream().filter(card -> card.contains(notYet)).count();
		long shown = drawn.stream().filter(card -> card.contains(notYet) && card.length() > card.indexOf(notYet) + notYet.length() + 1).count();
		int tipped = context.computeOnClient(client -> screen(client).tippedReasons().size());
		check(marked == waiting && shown + tipped == waiting, "Every card not ready draws Not yet, and its reason under it or, with no room, in "
				+ "its tip: " + marked + " marked, " + shown + " drawn and " + tipped + " in tips of " + waiting + " " + drawn);
	}

	/**
	 * The unit 3 fixes (CI's pictures: Marax's fourth card ran under Close): every card on the drawn chooser stands wholly in
	 * view, in the body above the footer (fits checks the body and its width).
	 */
	private static void cardsWhole(ClientGameTestContext context, String where) {
		settle(context);
		List<String> hidden = context.computeOnClient(client -> {
			List<String> out = new ArrayList<>();
			for (int i = 0; i < PactClient.state().patterns().size(); i++) {
				String key = "invite:" + i;
				if (screen(client).control(key) == null || !screen(client).inView(key)) {
					out.add(key);
				}
			}
			return out;
		});
		Jugcraft.LOGGER.info("[garden-check] {}: cards not wholly in view {}", where, hidden);
		check(hidden.isEmpty(), "Every card of the chooser is wholly in view above the footer (" + where + "): " + hidden);
	}

	/**
	 * The unit 3 fixes: the chooser's placement (PactScreen.chooserCards, pure) for one to seven cards (Stolas and the six
	 * garden spirits) at every size the client tests use (427x240, 320x180, 480x270 at GUI scale 4, and the smallest GUI,
	 * 320x240), full and compact: every card's rectangle inside the chooser's scroll, the scroll above the footer, no two
	 * cards overlapping.
	 */
	private static void placements(ClientGameTestContext context) {
		List<String> faults = context.computeOnClient(client -> {
			List<String> out = new ArrayList<>();
			int[][] sizes = {{427, 240}, {320, 180}, {480, 270}, {320, 240}};
			for (int[] size : sizes) {
				for (boolean compact : new boolean[] {false, true}) {
					ScreenRectangle area = PactScreen.chooserArea(size[0], size[1], compact);
					ScreenRectangle footer = PactScreen.chooserFooter(size[0], size[1], compact);
					String at = size[0] + "x" + size[1] + (compact ? " compact" : "");
					if (area.bottom() > footer.top()) {
						out.add(at + ": the scroll runs over the footer");
					}
					for (int count = 1; count <= 7; count++) {
						List<ScreenRectangle> cards = PactScreen.chooserCards(count, area);
						if (cards.size() != count) {
							out.add(at + ", " + count + " cards: " + cards.size() + " placed");
						}
						for (int i = 0; i < cards.size(); i++) {
							if (!area.encompasses(cards.get(i))) {
								out.add(at + ", " + count + " cards: card " + i + " " + cards.get(i) + " outside " + area);
							}
							for (int j = 0; j < i; j++) {
								if (cards.get(i).intersects(cards.get(j))) {
									out.add(at + ", " + count + " cards: cards " + j + " and " + i + " overlap");
								}
							}
						}
					}
				}
			}
			return out;
		});
		check(faults.isEmpty(), "Every chooser card whole in its scroll above the footer, none overlapping, for one to seven cards: " + faults);
	}

	/**
	 * The unit 3 fixes (CI's pictures: Buer's and Marax's windows were empty on their first beat): the header's portrait
	 * draws the spirit, and while this client still draws its arrival, draws it as it will stand (MagicPortrait.shown),
	 * never the arrival that hides it. Read in one go: a spirit arriving now was arriving at the frame drawn before.
	 */
	private static void portraitWhole(ClientGameTestContext context, String id) {
		settle(context);
		context.waitTicks(1);
		String seen = context.computeOnClient(client -> {
			BlockPos perch = screen(client).getMenu().opening.perch();
			boolean arriving = spiritAt(client, perch, id) instanceof PactSpiritEntity<?> spirit && PactSpiritRenderers.arriving(spirit);
			return screen(client).portrait() + (arriving ? " arriving" : "");
		});
		Jugcraft.LOGGER.info("[garden-check] {}'s portrait on his first beat: {}", id, seen);
		check(seen.equals("standing arriving") || seen.equals("standing") || seen.equals("live"),
				"His first beat's portrait draws him whole, as he will stand while he still arrives: " + seen);
	}

	private static void fits(ClientGameTestContext context, String where) {
		settle(context);
		List<String> outside = context.computeOnClient(client -> screen(client).outside());
		check(outside.isEmpty(), "Every control of the pact screen is on screen (" + where + "): " + outside);
	}

	private static void named(ClientGameTestContext context) {
		settle(context);
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

	/**
	 * A picture (the unit 3 fixes, CI's pictures: a tip over a row of the terms, the proud reading and the roster): first the
	 * cursor parked where no control is and the mouse made the last input ({@link #park}), so neither a hover's tip nor the
	 * keyboard's is drawn, then a tick for the hover to clear; the last input given back after, so the keyboard goes on as
	 * before. A shot whose name says it shows a tip or a hover keeps its cursor and input.
	 */
	private static void shot(ClientGameTestContext context, String name) {
		context.runOnClient(client -> client.gui.toastManager().clear());
		InputType input = name.contains("tip") || name.contains("hover") ? null : park(context);
		context.waitTicks(2);
		context.takeScreenshot(name);
		if (input != null) {
			context.runOnClient(client -> client.setLastInputType(input));
		}
	}

	/**
	 * With a screen open: the cursor moved to a corner of it that no control covers (one is asserted), and the mouse made the
	 * last input, since moving the mouse alone leaves the keyboard's (MouseHandler sets it only on a click) and a control
	 * focused by the keyboard shows its tip while the keyboard is the last input; then a tick. Returns the input it replaced,
	 * or null with no screen open (nothing is hovered then).
	 */
	private static @Nullable InputType park(ClientGameTestContext context) {
		if (!context.computeOnClient(client -> client.gui.screen() != null)) {
			return null;
		}
		double[] corner = context.computeOnClient(client -> {
			Screen screen = client.gui.screen();
			int[][] corners = {{1, 1}, {screen.width - 2, 1}, {1, screen.height - 2}, {screen.width - 2, screen.height - 2}};
			for (int[] at : corners) {
				boolean free = true;
				for (GuiEventListener child : screen.children()) {
					if (child instanceof AbstractWidget widget && widget.visible && widget.getRectangle().containsPoint(at[0], at[1])) {
						free = false;
					}
				}
				if (free) {
					int scale = client.getWindow().getGuiScale();
					return new double[] {(at[0] + 0.5) * scale, (at[1] + 0.5) * scale};
				}
			}
			return null;
		});
		check(corner != null, "A corner of the screen with no control under it, to park the cursor on before a picture");
		context.getInput().setCursorPos(corner[0], corner[1]);
		InputType before = context.computeOnClient(client -> client.getLastInputType());
		context.runOnClient(client -> client.setLastInputType(InputType.MOUSE));
		context.waitTicks(1);
		return before;
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
	 * facing), the world frozen, and it is photographed from its front along that speaker's bearing, framed whole with nothing
	 * before its face ({@link #frame}; the unit 3 fixes), as {@code shots} name; the player is then back where they spoke from.
	 */
	private static void spiritFacing(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch, String id, List<String> shots) {
		String[] sides = {"east", "south", "west", "north"};
		int[][] offsets = {{2, 0}, {0, 2}, {-2, 0}, {0, -2}};
		float[] bearings = {-90.0F, 0.0F, 90.0F, 180.0F};
		// The look goes to the spirit's middle, so a tall one (Bathin, coiled high on the perch) is framed head and all.
		double middle = server.computeOnServer(minecraft -> {
			PactSpiritEntity<?> spirit = presenceOf(minecraft, perch);
			return spirit.getY() + spirit.getBbHeight() / 2.0;
		});
		float pitch = (float) -Math.toDegrees(Math.atan2(middle - (perch.getY() + 1.62), 2.0));
		boolean hidden = hideHud(context);
		try {
			for (int i = 0; i < sides.length; i++) {
				float bearing = bearings[i];
				server.runCommand(String.format(Locale.ROOT, "tp @a %.1f %d %.1f %.1f %.1f", perch.getX() + 0.5 + offsets[i][0], perch.getY(),
						perch.getZ() + 0.5 + offsets[i][1], Mth.wrapDegrees(bearing + 180.0F), pitch));
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
				Vec3 speaker = new Vec3(perch.getX() + 0.5 + offsets[i][0], perch.getY(), perch.getZ() + 0.5 + offsets[i][1]);
				server.runCommand("tick freeze");
				try {
					context.waitTicks(3);
					frame(context, server, perch, id, speaker, sides[i]);
					shot(context, shots.get(i));
				} finally {
					land(context, server, speaker);
					server.runCommand("tick unfreeze");
				}
			}
		} finally {
			restoreHud(context, hidden);
		}
	}

	/**
	 * GC-06b (GC-P78; shared by GC-06 to GC-09): GC-02's facing rule for a standing body (Station.BESIDE), which stands where
	 * spiritFacing would put the player: from each side of the spirit itself in turn (east, south, west, north), three blocks
	 * off and facing it, the player opens the perch; its drawn body turns to them (GC-P8: within BATHIN_FACING_TOLERANCE);
	 * the screen is closed (the body keeps its facing), the world frozen, and it is photographed from its front along that
	 * speaker's bearing, framed whole with nothing before its face ({@link #frame}; the unit 3 fixes: the perch's post stood
	 * before Marax's face from the east), as {@code shots} name (literal names, in that order, so check_pact_screen sees
	 * each); the player is then back where they spoke from.
	 */
	private static void standingFacing(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch, String id, List<String> shots) {
		Vec3 at = server.computeOnServer(minecraft -> presenceOf(minecraft, perch).position());
		String[] sides = {"east", "south", "west", "north"};
		int[][] offsets = {{3, 0}, {0, 3}, {-3, 0}, {0, -3}};
		float[] bearings = {-90.0F, 0.0F, 90.0F, 180.0F};
		boolean hidden = hideHud(context);
		try {
			for (int i = 0; i < sides.length; i++) {
				float bearing = bearings[i];
				server.runCommand(String.format(Locale.ROOT, "tp @a %.1f %.1f %.1f %.1f 10", at.x + offsets[i][0], at.y, at.z + offsets[i][1],
						Mth.wrapDegrees(bearing + 180.0F)));
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
				Vec3 speaker = new Vec3(at.x + offsets[i][0], at.y, at.z + offsets[i][1]);
				server.runCommand("tick freeze");
				try {
					context.waitTicks(3);
					frame(context, server, perch, id, speaker, sides[i]);
					shot(context, shots.get(i));
				} finally {
					land(context, server, speaker);
					server.runCommand("tick unfreeze");
				}
			}
		} finally {
			restoreHud(context, hidden);
		}
	}

	/** How much of a facing picture's height and width a spirit's drawn body may fill: a tenth's margin at each edge. */
	private static final double FRAME_FILL = 0.8;
	/** The steepest a facing picture looks down at its spirit, tried in turn: a level look first, steeper only where needed. */
	private static final double[] FRAME_PITCHES = {15.0, 25.0, 35.0, 45.0};

	/**
	 * The unit 3 fixes (CI's pictures: Buer's petals ran off the top; the perch's post stood before Marax's face from the
	 * east, Buer's bed from the west): a facing picture's camera, the world frozen and the screen closed. Along the bearing
	 * from the spirit to {@code speaker} (the cell the test spoke from, so the picture still shows it facing them), as far
	 * back and as high as its drawn body needs (PactSpiritRenderers.standing) to stand whole in the picture with a tenth's
	 * margin at each edge, and from where no block's outline stands between the camera and its face: its eyes and a little
	 * below them, at its middle and at its front (a point inside a block cannot be seen from anywhere and is not looked for).
	 * A level look is tried first, a steeper one only where something stands before its face. Asserted found; the player
	 * flies there, and {@link #land} puts them back.
	 */
	private static void frame(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch, String id, Vec3 speaker,
			String side) {
		double[] camera = context.computeOnClient(client -> camera(client, perch, id, speaker));
		Jugcraft.LOGGER.info("[garden-check] {} framed from the {}: feet, yaw and pitch {}", id, side, Arrays.toString(camera));
		check(camera != null, id + " stands whole in its facing picture from the " + side + ", nothing before its face");
		server.runOnServer(minecraft -> {
			ServerPlayer player = player(minecraft);
			player.getAbilities().mayfly = true;
			player.getAbilities().flying = true;
			player.onUpdateAbilities();
		});
		server.runCommand(String.format(Locale.ROOT, "tp @a %.3f %.3f %.3f %.1f %.1f", camera[0], camera[1], camera[2], camera[3], camera[4]));
		Vec3 feet = new Vec3(camera[0], camera[1], camera[2]);
		context.waitFor(client -> client.player.position().distanceToSqr(feet) < 0.01, WAIT);
		context.waitTicks(3);
	}

	/** Back on the cell they spoke from, on their feet: the camera's flight ends. */
	private static void land(ClientGameTestContext context, TestDedicatedServerContext server, Vec3 speaker) {
		server.runCommand(String.format(Locale.ROOT, "tp @a %.3f %.3f %.3f", speaker.x, speaker.y, speaker.z));
		server.runOnServer(minecraft -> {
			ServerPlayer player = player(minecraft);
			player.getAbilities().flying = false;
			player.getAbilities().mayfly = false;
			player.onUpdateAbilities();
		});
		context.waitTicks(2);
	}

	/** The feet and look ({x, y, z, yaw, pitch}) {@link #frame} puts the player at, or null when no place within reach frames it so. */
	private static double @Nullable [] camera(Minecraft client, BlockPos perch, String id, Vec3 speaker) {
		PactSpiritEntity<?> spirit = spiritAt(client, perch, id);
		if (spirit == null) {
			return null;
		}
		AABB body = PactSpiritRenderers.standing(spirit);
		Vec3 centre = body.getCenter();
		Vec3 at = spirit.position();
		Vec3 toward = new Vec3(speaker.x - at.x, 0.0, speaker.z - at.z);
		double spoke = toward.horizontalDistance();
		Vec3 bearing = toward.normalize();
		double tall = FRAME_FILL * Math.tan(Math.toRadians(client.options.fov().get() / 2.0));
		double wide = tall * client.getWindow().getWidth() / Math.max(1, client.getWindow().getHeight());
		List<Vec3> face = new ArrayList<>();
		for (double forward : new double[] {0.0, spirit.getBbWidth() / 2.0}) {
			for (double below : new double[] {-0.05, 0.3}) {
				Vec3 point = spirit.getEyePosition().add(bearing.scale(forward)).subtract(0.0, below, 0.0);
				BlockPos cell = BlockPos.containing(point);
				VoxelShape shape = client.level.getBlockState(cell).getShape(client.level, cell);
				if (shape.isEmpty() || !shape.bounds().move(cell).inflate(1.0E-4).contains(point)) {
					face.add(point);
				}
			}
		}
		double eye = client.player.getEyeHeight();
		for (double steepest : FRAME_PITCHES) {
			for (double back = 0.0; back <= 8.0; back += 0.25) {
				for (double lift = 0.0; lift <= 5.0; lift += 0.25) {
					Vec3 from = new Vec3(at.x + bearing.x * (spoke + back), speaker.y + eye + lift, at.z + bearing.z * (spoke + back));
					Vec3 look = centre.subtract(from);
					double pitch = -Math.toDegrees(Math.atan2(look.y, look.horizontalDistance()));
					if (pitch < -10.0 || pitch > steepest || !frames(from, look, body, tall, wide) || !seen(client, from, face)) {
						continue;
					}
					float yaw = Mth.wrapDegrees((float) (Mth.atan2(look.z, look.x) * Mth.RAD_TO_DEG) - 90.0F);
					return new double[] {from.x, from.y - eye, from.z, yaw, pitch};
				}
			}
		}
		return null;
	}

	/** Whether every corner of {@code body} lies inside the picture's middle, seen from {@code from} looking along {@code look}. */
	private static boolean frames(Vec3 from, Vec3 look, AABB body, double tall, double wide) {
		Vec3 forward = look.normalize();
		Vec3 right = forward.cross(new Vec3(0.0, 1.0, 0.0)).normalize();
		Vec3 up = right.cross(forward);
		for (double x : new double[] {body.minX, body.maxX}) {
			for (double y : new double[] {body.minY, body.maxY}) {
				for (double z : new double[] {body.minZ, body.maxZ}) {
					Vec3 to = new Vec3(x, y, z).subtract(from);
					double depth = to.dot(forward);
					if (depth <= 0.1 || Math.abs(to.dot(up)) > tall * depth || Math.abs(to.dot(right)) > wide * depth) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/** Whether no block's outline stands on the line from {@code from} to any of {@code face} (the client's world). */
	private static boolean seen(Minecraft client, Vec3 from, List<Vec3> face) {
		for (Vec3 point : face) {
			if (client.level.clip(new ClipContext(from, point, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, client.player)).getType()
					!= HitResult.Type.MISS) {
				return false;
			}
		}
		return true;
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
		cardsWhole(context, "the chooser without his Heart");
		named(context);
		reasons(context);
		budget(context, chooserWords(context), "the chooser without his Heart");
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
		cardsWhole(context, "the chooser with three spirits");
		named(context);
		budget(context, chooserWords(context), "the chooser with three spirits");
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
		portraitWhole(context, BATHIN);
		budget(context, BEAT_WORDS, "his first beat");
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
		budget(context, TERMS_WORDS, "his terms");
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
		budget(context, TERMS_WORDS, "his terms at GUI scale 4");
		shot(context, "jugcraft_garden_bathin_terms_scale4");
		press(context, "back");
		fits(context, "his beat at GUI scale 4");
		named(context);
		budget(context, BEAT_WORDS, "his last beat at GUI scale 4");
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
		budget(context, TERMS_WORDS, "his terms at 320x180");
		shot(context, "jugcraft_garden_bathin_terms_small");
		press(context, "back");
		fits(context, "his beat at 320x180");
		named(context);
		budget(context, BEAT_WORDS, "his last beat at 320x180");
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
		budget(context, TERMS_WORDS, "his refusal");
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
		budget(context, keptWords(context), "his kept pact");
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
			// Magic interfaces unit 2: his relic card draws that state short, its whole sentence his card's narration and tip.
			context.waitFor(client -> client.gui.screen() instanceof RosterScreen screen && screen.settled() && !RosterClient.waiting(), WAIT);
			String card = context.computeOnClient(client -> Component.translatable("entity.jugcraft.bathin").getString() + " "
					+ Component.translatable("screen.jugcraft.glance.compose.jugcraft.worker.status.awaiting_answer").getString());
			List<String> drawn = context.computeOnClient(client -> ((RosterScreen) client.gui.screen()).drawn());
			Jugcraft.LOGGER.info("[garden-check] the roster's cards while his matter waits: {}", drawn);
			check(drawn.stream().anyMatch(text -> text.startsWith(card)), "Bathin's card draws his state short (\"" + card + "\"): " + drawn);
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
		// The state first, then the page built from it: the screen rebuilds a frame after its state arrives.
		context.waitFor(client -> PactClient.state() != null && PactClient.state().mode() == PactStatePayload.KEEP
				&& PactClient.state().spirit().answers().size() == 2 && screen(client).control("answer:1") != null, WAIT);
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
		budget(context, keptWords(context) + firstTask(context), "his matter");
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
		budget(context, keptWords(context) + firstTask(context), "his matter at GUI scale 4");
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
		budget(context, keptWords(context) + firstTask(context), "his matter at 320x180");
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
		budget(context, keptWords(context) + firstTask(context), "his matter answered");
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
		confirmBudget(context);
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
		budget(context, ENDED_WORDS, "the ended page");
		shot(context, "jugcraft_garden_bathin_ended");
		context.getInput().pressKey(InputConstants.KEY_ESCAPE);
		context.waitForScreen(null);
	}

	// GC-06 unit 3
	// GC-06: Marax (tools/concordance_pacts.py SPIRITS["marax"]; check_spirit_bodies).
	private static final String MARAX = "jugcraft:marax";
	private static final String MARAX_IDLE = "animation.marax.idle";
	private static final String MARAX_LISTEN = "animation.marax.listen";
	private static final String MARAX_WAITING = "animation.marax.waiting";
	private static final String MARAX_SUSPENDED = "animation.marax.suspended";
	private static final String MARAX_STARGAZE = "animation.marax.stargaze";
	/** His refusal at a perch with nothing planted of the inviter's near (Dossier 3 item 6). */
	private static final String NOTHING_GROWS = "My familiar reads what grows. Nothing grows here.";
	/** His compact (data/jugcraft/concordance/pact/marax.json): no resonance, so no astrolabe. */
	private static final List<String> MARAX_COMPACT = List.of("minecraft:book 1", "jugcraft:sunpetal 2", "jugcraft:dewmoss 2");

	// ---------------------------------------------------------------- GC-06: Marax (Dossier 3)

	/**
	 * GC-06: Marax at the perch Bathin left, from the chooser to his departure: listed not ready with nothing planted, chosen
	 * by keyboard and treading up beside the perch; his conversation at three sizes, refused and sealed; a reading on his
	 * page at three sizes with his familiar frozen over the bed it names; his page with a proud bed; Jade; eleven poses, his
	 * night look among them; four bearings round where he stands; the end.
	 */
	private static void marax(ClientGameTestContext context, TestDedicatedServerContext server, TestDedicatedServerConnection connection,
			BlockPos perch) {
		BlockPos poor = perch.offset(3, 0, 3);
		BlockPos plain = perch.offset(1, 0, 5);
		// The critique's item 5: the daylight cycle ran on through Buer's and Bathin's flows; his pictures and idle pose are by day.
		server.runCommand("time set noon");
		server.runCommand("weather clear");
		server.runOnServer(minecraft -> yard(minecraft, perch));
		standSouth(context, server, perch);
		connection.waitForChunksRender();
		nothingGrows(context, server, perch, poor);
		maraxChooser(context, server, perch, poor, plain);
		maraxConversation(context, server);
		maraxSizes(context);
		Hearing hearing = hear(context);
		maraxAcceptance(context, server);
		maraxHeard(context, server, hearing, perch);
		maraxReading(context, server, perch, poor, plain);
		maraxProud(context, server, perch, poor);
		maraxJade(context, server, perch);
		maraxPoses(context, server, perch);
		standingFacing(context, server, perch, MARAX, List.of("jugcraft_garden_marax_faces_east", "jugcraft_garden_marax_faces_south",
				"jugcraft_garden_marax_faces_west", "jugcraft_garden_marax_faces_north"));
		standSouth(context, server, perch);
		maraxEnding(context, server, perch);
	}

	/** Dossier 3 item 6: an awake bed of the player's in his area with nothing planted, and the chooser lists him not ready, in his words. */
	private static void nothingGrows(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch, BlockPos poor) {
		server.runOnServer(minecraft -> {
			minecraft.overworld().setBlockAndUpdate(poor, Garden.VERDANT_BED.defaultBlockState().setValue(VerdantBedBlock.MOISTURE, VerdantBedBlock.WET));
			bed(minecraft, poor).awaken(player(minecraft).getUUID());
		});
		context.waitTicks(OPEN_GAP);
		open(context, server, perch);
		context.waitFor(client -> PactClient.state() != null && PactClient.state().mode() == PactStatePayload.INVITE, WAIT);
		String entries = context.computeOnClient(client -> entries(PactClient.state()));
		Jugcraft.LOGGER.info("[garden-check] chooser with nothing planted: {}", entries);
		boolean listed = context.computeOnClient(client -> PactClient.state().patterns().stream()
				.anyMatch(choice -> choice.id().equals(MaraxEntity.SIGN) && !choice.ready() && choice.amount() == 1)
				&& PactClient.state().notes().stream().anyMatch(note -> note.getString().contains(NOTHING_GROWS)));
		check(listed, "Marax listed, not ready, in his words: " + entries);
		fits(context, "the chooser with nothing planted");
		cardsWhole(context, "the chooser with nothing planted");
		named(context);
		reasons(context);
		budget(context, chooserWords(context), "the chooser with nothing planted");
		shot(context, "jugcraft_garden_marax_nothing_grows");
		context.getInput().pressKey(InputConstants.KEY_ESCAPE);
		context.waitForScreen(null);
	}

	/**
	 * Two wet beds of the player's planted with a Sunpetal (the poor one holding no nutrients, the plain one four); the chooser
	 * lists him ready; chosen by keyboard, he treads up two blocks west of the perch (GC-P67), arriving with his gesture.
	 */
	private static void maraxChooser(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch, BlockPos poor,
			BlockPos plain) {
		server.runOnServer(minecraft -> {
			ServerLevel level = minecraft.overworld();
			ServerPlayer player = player(minecraft);
			for (BlockPos at : List.of(poor, plain)) {
				if (!(level.getBlockEntity(at) instanceof VerdantBedBlockEntity)) {
					level.setBlockAndUpdate(at, Garden.VERDANT_BED.defaultBlockState().setValue(VerdantBedBlock.MOISTURE, VerdantBedBlock.WET));
				}
				VerdantBedBlockEntity kept = bed(minecraft, at);
				kept.awaken(player.getUUID());
				kept.setNutrients(at.equals(poor) ? 0 : 4);
				level.setBlockAndUpdate(at.above(), ((OrganismCropBlock) Garden.SUNPETAL_CROP).getStateForAge(OrganismCropBlock.MAX_AGE));
			}
		});
		context.waitTicks(25);
		open(context, server, perch);
		context.waitFor(client -> PactClient.state() != null && PactClient.state().mode() == PactStatePayload.INVITE
				&& PactClient.state().patterns().stream().anyMatch(choice -> choice.id().equals(MaraxEntity.SIGN) && choice.ready()), WAIT);
		String entries = context.computeOnClient(client -> entries(PactClient.state()));
		Jugcraft.LOGGER.info("[garden-check] chooser with his beds planted: {}", entries);
		context.waitFor(client -> client.gui.screen() instanceof PactScreen, WAIT);
		fits(context, "the chooser with four spirits");
		cardsWhole(context, "the chooser with four spirits");
		named(context);
		reasons(context);
		budget(context, chooserWords(context), "the chooser with four spirits");
		shot(context, "jugcraft_garden_marax_chooser");
		String name = context.computeOnClient(client -> Component.translatable("entity.jugcraft.marax").getString());
		tabToNamed(context, name);
		context.getInput().pressKey(InputConstants.KEY_RETURN);
		context.waitFor(client -> PactClient.state() != null && PactClient.state().mode() == PactStatePayload.NEGOTIATE
				&& MaraxEntity.SIGN.equals(PactClient.state().spirit().sign()) && spiritAt(client, perch, MARAX) != null, WAIT);
		context.waitFor(client -> MaraxEntity.Gesture.ARRIVE.animation.equals(gesturedOf(client, perch, MARAX)), WAIT);
		Vec3 at = server.computeOnServer(minecraft -> presenceOf(minecraft, perch).position());
		Jugcraft.LOGGER.info("[garden-check] invited marax: he stands at {}", at);
		check(at.distanceTo(Vec3.atBottomCenterOf(perch.west(2))) < 1.0E-3,
				"Chosen, he treads up two blocks west of the perch, on the ground, never between it and the player (GC-P67): " + at);
	}

	/** His three beats, held to the magic interfaces' limits, then every term by keyboard (Enter three times), none naming an astrolabe or resonance (GB23). */
	private static void maraxConversation(ClientGameTestContext context, TestDedicatedServerContext server) {
		String shown = context.computeOnClient(client -> describe(PactClient.state()));
		Jugcraft.LOGGER.info("[garden-check] marax's offer: {}", shown);
		boolean whole = context.computeOnClient(client -> PactClient.state().beats().size() == 3 && PactClient.state().notes().isEmpty()
				&& PactClient.state().offers(PactRequestPayload.ACCEPT)
				&& PactClient.state().terms().stream().map(PactStatePayload.Term::key).toList().equals(PactStatePayload.TERMS));
		check(whole, "Marax speaks three beats, nothing stands in the way, and his terms name every term, in order: " + shown);
		List<String> beats = context.computeOnClient(client -> PactClient.state().beats().stream().map(Component::getString).toList());
		check(beats.get(0).contains("I keep a friend who knows the ground") && beats.get(1).contains("Only one")
				&& beats.get(2).contains("My familiar writes small"), "His beats keep the contract's closing words: " + beats);
		fits(context, "his beat at GUI scale 2");
		named(context);
		portraitWhole(context, MARAX);
		budget(context, BEAT_WORDS, "his first beat");
		shot(context, "jugcraft_garden_marax_conversation");
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
		check(!words.contains("astrolabe") && !words.contains("resonance") && words.contains("habitat reading"),
				"His terms name his service and no astrolabe or resonance: " + words);
		fits(context, "his terms at GUI scale 2");
		named(context);
		budget(context, TERMS_WORDS, "his terms");
		shot(context, "jugcraft_garden_marax_terms");
		boolean held = server.computeOnServer(minecraft -> holds(minecraft, MARAX));
		check(!held, "Hearing his terms records nothing");
	}

	/** His terms and a beat at GUI scale 4 (1920 x 1080) and in a 320 x 180 window. */
	private static void maraxSizes(ClientGameTestContext context) {
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
		budget(context, TERMS_WORDS, "his terms at GUI scale 4");
		shot(context, "jugcraft_garden_marax_terms_scale4");
		press(context, "back");
		fits(context, "his beat at GUI scale 4");
		named(context);
		budget(context, BEAT_WORDS, "his last beat at GUI scale 4");
		shot(context, "jugcraft_garden_marax_conversation_scale4");
		press(context, "next");
		context.runOnClient(client -> {
			client.options.guiScale().set(2);
			client.resizeGui();
		});
		context.getInput().resizeWindow(320, 180);
		context.waitTicks(10);
		String gui = context.computeOnClient(client -> client.getWindow().getGuiScaledWidth() + "x" + client.getWindow().getGuiScaledHeight());
		check("320x180".equals(gui), "A 320x180 window draws a 320x180 GUI: " + gui);
		fits(context, "his terms at 320x180");
		named(context);
		budget(context, TERMS_WORDS, "his terms at 320x180");
		shot(context, "jugcraft_garden_marax_terms_small");
		press(context, "back");
		fits(context, "his beat at 320x180");
		named(context);
		budget(context, BEAT_WORDS, "his last beat at 320x180");
		shot(context, "jugcraft_garden_marax_conversation_small");
		press(context, "next");
		context.getInput().resizeWindow(width, height);
		context.waitTicks(10);
		boolean back = context.computeOnClient(client -> screen(client).page() == PactScreen.TERMS_PAGE);
		check(back, "Back on his terms");
	}

	/**
	 * A book and 2 Sunpetal but no Dewmoss: Accept is refused and nothing is spent; with 2 Dewmoss too, sealed once and spent
	 * once. His kept page: today's readings 0 of 2, no reading yet, his first task, and his reading's plate.
	 */
	private static void maraxAcceptance(ClientGameTestContext context, TestDedicatedServerContext server) {
		server.runOnServer(minecraft -> {
			player(minecraft).getInventory().add(new ItemStack(Items.BOOK, 1));
			player(minecraft).getInventory().add(new ItemStack(Garden.SUNPETAL, 2));
		});
		context.waitTicks(5);
		tabTo(context, "accept");
		PactStatePayload.Result refused = answer(context);
		String said = refused.message().getString();
		int refusedMode = context.computeOnClient(client -> PactClient.state().mode());
		Jugcraft.LOGGER.info("[garden-check] Accept without his Dewmoss: {} ({})", refused.reason(), said);
		check("cannot_afford".equals(refused.reason()) && !refused.done() && !said.toLowerCase(Locale.ROOT).contains("astrolabe")
				&& refusedMode == PactStatePayload.NEGOTIATE, "Without his Dewmoss Accept is refused, worded without an astrolabe: " + said);
		String kept = server.computeOnServer(minecraft -> holds(minecraft, MARAX) + " " + player(minecraft).getInventory().countItem(Items.BOOK)
				+ " " + player(minecraft).getInventory().countItem(Garden.SUNPETAL));
		check("false 1 2".equals(kept), "A refusal records and spends nothing: " + kept);
		context.waitFor(client -> screen(client).control("result") instanceof PactScreen.Verdict verdict && verdict.refused(), WAIT);
		budget(context, TERMS_WORDS, "his refusal");
		shot(context, "jugcraft_garden_marax_refused");
		List<String> cost = context.computeOnClient(client -> PactClient.state().compact().stream()
				.map(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()) + " " + stack.getCount()).toList());
		int resonance = context.computeOnClient(client -> PactClient.state().resonance());
		Jugcraft.LOGGER.info("[garden-check] his compact: {} and {} resonance", cost, resonance);
		check(cost.equals(MARAX_COMPACT) && resonance == 0, "His compact: a book, 2 Sunpetal and 2 Dewmoss, no resonance: " + cost);
		server.runOnServer(minecraft -> player(minecraft).getInventory().add(new ItemStack(Garden.DEWMOSS, 2)));
		context.waitTicks(5);
		tabTo(context, "accept");
		PactStatePayload.Result sealed = answer(context);
		int sealedMode = context.computeOnClient(client -> PactClient.state().mode());
		check(sealed.done() && "sealed".equals(sealed.reason()) && sealedMode == PactStatePayload.KEEP, "Accepting with his compact seals the pact: " + sealed.reason());
		String spent = server.computeOnServer(minecraft -> holds(minecraft, MARAX) + " "
				+ cost.stream().mapToInt(line -> player(minecraft).getInventory().countItem(stack(line).getItem())).sum());
		Jugcraft.LOGGER.info("[garden-check] sealed: held, compact left = {}", spent);
		check("true 0".equals(spent), "Sealed once, his compact spent once: " + spent);
		context.waitFor(client -> screen(client).control("result") instanceof PactScreen.Verdict verdict && !verdict.refused()
				&& screen(client).control("work:0") != null, WAIT);
		List<String> rows = context.computeOnClient(client -> lines(PactClient.state()));
		String plate = context.computeOnClient(client -> screen(client).control("work:0").getMessage().getString());
		boolean ready = context.computeOnClient(client -> PactClient.state().patterns().stream()
				.anyMatch(choice -> choice.id().equals(HabitatReading.CHOICE) && choice.ready() && choice.amount() == 2));
		Jugcraft.LOGGER.info("[garden-check] his kept page: {}; plate {}", rows, plate);
		check(ready && plate.equals("Ask Marax for a reading") && rows.stream().anyMatch(row -> row.contains("0 of 2 readings given today"))
				&& rows.stream().anyMatch(row -> row.startsWith("No reading given yet")) && rows.stream().anyMatch(row -> row.startsWith("First obligation")),
				"His kept page: none of 2 read today, none given yet, his first task, and the plate to ask, 2 left: " + rows);
		fits(context, "his kept pact at GUI scale 2");
		named(context);
		budget(context, keptWords(context), "his kept pact");
		shot(context, "jugcraft_garden_marax_kept");
	}

	/** His refuse and assent fired by the server, their subtitles his, and his work's; heard where the machine can play sound. */
	private static void maraxHeard(ClientGameTestContext context, TestDedicatedServerContext server, Hearing hearing, BlockPos perch) {
		String refuse = context.computeOnClient(client -> Component.translatable("subtitles.jugcraft.marax.refuse").getString());
		String assent = context.computeOnClient(client -> Component.translatable("subtitles.jugcraft.marax.assent").getString());
		String work = context.computeOnClient(client -> Component.translatable("subtitles.jugcraft.marax.work").getString());
		boolean defined = context.computeOnClient(client -> refuse.equals(subtitle(client, "marax.refuse"))
				&& assent.equals(subtitle(client, "marax.assent")) && work.equals(subtitle(client, "marax.work")));
		check(!refuse.startsWith("subtitles.") && !work.startsWith("subtitles.") && defined,
				"His refuse, assent and work have their subtitles in the client's sounds: " + refuse + ", " + assent + ", " + work);
		List<String> cues = server.computeOnServer(minecraft -> presenceOf(minecraft, perch).cues());
		Jugcraft.LOGGER.info("[garden-check] Marax's cues so far: {}", cues);
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
	 * Dossier 3 items 3 and 7(b), the contract's shot: asked on his page, his familiar reads both beds from their samples, flies
	 * to the worst and hovers there while the answer is on screen. The answer is drawn as its change (the glance), the whole
	 * sentence its tip. Frozen as soon as the familiar is over the bed (the critique's item 11: SHOW_TICKS passing while the
	 * pictures are taken would clear it), photographed at three sizes and then from north of the perch with the screen
	 * closed; thawed, his next think clears the target and the familiar flies home.
	 */
	private static void maraxReading(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch, BlockPos poor,
			BlockPos plain) {
		for (BlockPos at : List.of(poor, plain)) {
			// Each bed's area read as a growing plant would (random ticks are off, so its plant has not), one request a tick.
			untilServer(context, server, minecraft -> bed(minecraft, at).habitat(minecraft.overworld()) != null, "The bed at " + at + " is sampled");
		}
		standSouth(context, server, perch);
		context.waitTicks(OPEN_GAP);
		open(context, server, perch);
		context.waitFor(client -> PactClient.state() != null && PactClient.state().mode() == PactStatePayload.KEEP
				&& PactClient.state().patterns().stream().anyMatch(choice -> choice.id().equals(HabitatReading.CHOICE) && choice.ready())
				&& screen(client).control("work:0") != null, WAIT);
		tabTo(context, "work:0");
		PactStatePayload.Result read = answer(context);
		String said = read.message().getString();
		Jugcraft.LOGGER.info("[garden-check] marax's reading: {} ({})", said, read.reason());
		check(read.done() && HabitatReading.READ.equals(read.reason()) && said.contains(poor.toShortString()) && said.contains("Nutrients")
				&& said.contains("That alone will move it on"), "It names the poor bed, its nutrients and the one change: " + said);
		context.waitFor(client -> screen(client).control("result") instanceof PactScreen.Verdict verdict && !verdict.refused(), WAIT);
		String drawn = context.computeOnClient(client -> ((PactScreen.Verdict) screen(client).control("result")).drawnText());
		Jugcraft.LOGGER.info("[garden-check] his reading drawn: {}", drawn);
		check("Nutrients 0 → 1".equals(drawn), "The answer is drawn as its one change, the whole sentence its tip: " + drawn);
		context.waitFor(client -> spiritAt(client, perch, MARAX) instanceof PactSpiritEntity<?> spirit && poor.equals(spirit.flown())
				&& spirit.flight(0.0F) >= 1.0F, WAIT);
		server.runCommand("tick freeze");
		try {
			fits(context, "the reading");
			named(context);
			budget(context, keptWords(context), "his reading");
			shot(context, "jugcraft_garden_marax_reading");
			readingSizes(context);
			context.getInput().pressKey(InputConstants.KEY_ESCAPE);
			context.waitForScreen(null);
			// From north of the perch, looking south over it: the perch, Marax on its west, the familiar over the poor bed.
			server.runCommand(String.format(Locale.ROOT, "tp @a %.1f %d %.1f 0 10", perch.getX() + 0.5, perch.getY(), perch.getZ() - 2.5));
			context.waitTicks(3);
			boolean hovering = context.computeOnClient(client -> spiritAt(client, perch, MARAX) instanceof PactSpiritEntity<?> spirit
					&& spirit.target().equals(Optional.of(poor)) && spirit.flight(0.0F) >= 1.0F);
			Jugcraft.LOGGER.info("[garden-check] frozen, the familiar over {}: {}", poor.toShortString(), hovering);
			check(hovering, "Frozen with the answer just closed, his familiar hovers over the bed it named");
			shot(context, "jugcraft_garden_marax_familiar");
		} finally {
			server.runCommand("tick unfreeze");
		}
		context.waitFor(client -> spiritAt(client, perch, MARAX) instanceof PactSpiritEntity<?> spirit && spirit.target().isEmpty()
				&& spirit.flight(0.0F) <= 0.0F, WAIT);
		boolean clear = server.computeOnServer(minecraft -> presenceOf(minecraft, perch).target().isEmpty());
		check(clear, "The conversation over, the target clears and the familiar flies home");
	}

	/**
	 * Dossier 3 item 5 (Marax's review: no shot measured it): his kept page with a proud bed keeps to its budget. The poor bed
	 * his familiar named on screen is named twice more on the server at the next game day's start (two readings a day;
	 * nothing on it changed, random ticks being off), so the familiar is proud of it; his page, opened again, draws the
	 * proud row beside the last reading and his first task.
	 */
	private static void maraxProud(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch, BlockPos poor) {
		standSouth(context, server, perch);
		String proud = server.computeOnServer(minecraft -> {
			ServerLevel level = minecraft.overworld();
			long day = (Math.floorDiv(level.getGameTime(), Pacts.DAY) + 1) * Pacts.DAY;
			StringBuilder out = new StringBuilder();
			for (long when : new long[] {day + 1_000L, day + 1_100L}) {
				PactMenu.Answered read = HabitatReading.read(level, player(minecraft), holder(minecraft, perch), level.getOverworldClockTime(), when);
				out.append(read.reason()).append(read.said().getString().contains(poor.toShortString()) ? " poor, " : " other, ");
			}
			PactSpiritEntity<?> marax = presenceOf(minecraft, perch);
			// Each reading turned him to the bed until its own game time plus his work gesture's: a day ahead, so that is ended
			// now, and he faces whoever speaks to him again (no screen was open, so no bed was shown).
			marax.attendWork(poor, 0, level.getGameTime());
			HabitatReading.Memory memory = marax.serviceMemory(HabitatReading.Memory.class, HabitatReading.Memory::new);
			return out.append(memory.proud(poor) ? "proud" : "not proud").toString();
		});
		Jugcraft.LOGGER.info("[garden-check] the poor bed named twice more: {}", proud);
		check(proud.equals("read poor, read poor, proud"), "Named three readings running, nothing changed, the poor bed is proud: " + proud);
		context.waitTicks(OPEN_GAP);
		open(context, server, perch);
		context.waitFor(client -> PactClient.state() != null && PactClient.state().mode() == PactStatePayload.KEEP
				&& lines(PactClient.state()).stream().anyMatch(row -> row.startsWith("Proud: his familiar will not read the bed at")), WAIT);
		List<String> rows = context.computeOnClient(client -> lines(PactClient.state()));
		Jugcraft.LOGGER.info("[garden-check] his kept page with a proud bed: {}", rows);
		fits(context, "his kept page with a proud bed");
		named(context);
		budget(context, keptWords(context), "his kept page with a proud bed");
		shot(context, "jugcraft_garden_marax_proud");
		context.getInput().pressKey(InputConstants.KEY_ESCAPE);
		context.waitForScreen(null);
	}

	/** The reading's page at GUI scale 4 (1920 x 1080) and in a 320 x 180 window, then as it was (the world frozen throughout). */
	private static void readingSizes(ClientGameTestContext context) {
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
		fits(context, "his reading at GUI scale 4");
		named(context);
		budget(context, keptWords(context), "his reading at GUI scale 4");
		shot(context, "jugcraft_garden_marax_reading_scale4");
		context.runOnClient(client -> {
			client.options.guiScale().set(2);
			client.resizeGui();
		});
		context.getInput().resizeWindow(320, 180);
		context.waitTicks(10);
		fits(context, "his reading at 320x180");
		named(context);
		budget(context, keptWords(context), "his reading at 320x180");
		shot(context, "jugcraft_garden_marax_reading_small");
		context.getInput().resizeWindow(width, height);
		context.waitTicks(10);
	}

	/** Jade's readings of his perch and of him (read on the server: no Jade class); with Jade loaded, his overlay. */
	private static void maraxJade(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch) {
		String readings = server.computeOnServer(minecraft -> {
			ServerLevel level = minecraft.overworld();
			CompoundTag ofPerch = PactReading.holder(level, (PactPerchBlockEntity) level.getBlockEntity(perch), level.getGameTime());
			CompoundTag ofHim = PactReading.presence(level, presenceOf(minecraft, perch), level.getGameTime());
			String both = ofPerch + " " + ofHim;
			boolean right = "kept".equals(ofPerch.getStringOr("standing", "")) && MARAX.equals(ofPerch.getStringOr("counterpart", ""))
					&& HabitatReading.WORK.equals(ofPerch.getStringOr("service", "")) && "active".equals(ofPerch.getStringOr("state", ""))
					&& ofPerch.getIntOr("due_days", -1) == 7 && "kept".equals(ofHim.getStringOr("standing", ""))
					&& PactReading.KEYS.containsAll(ofPerch.keySet()) && PactReading.KEYS.containsAll(ofHim.keySet())
					&& !both.contains(player(minecraft).getUUID().toString()) && !both.contains(player(minecraft).getName().getString());
			return (right ? "ok " : "bad ") + both;
		});
		Jugcraft.LOGGER.info("[garden-check] Jade's readings of his perch and of Marax: {}", readings);
		check(readings.startsWith("ok"), "Jade's readings: his pact, service, state and next offering, naming no player: " + readings);
		if (FabricLoader.getInstance().isModLoaded("jade")) {
			boolean hidden = context.computeOnClient(client -> client.gui.hud.isHidden());
			context.runOnClient(client -> {
				if (client.gui.hud.isHidden()) {
					client.gui.hud.toggle();
				}
			});
			try {
				maraxCamera(context, server, perch);
				context.getInput().lookAt(perch.west(2));
				context.waitTicks(20);
				shot(context, "jugcraft_garden_marax_jade");
			} finally {
				restoreHud(context, hidden);
			}
		} else {
			Jugcraft.LOGGER.info("[garden-check] Jade is not loaded: no Jade shot of Marax");
		}
	}

	/** The camera for his body: three blocks south of where he stands, at his middle's height in view, facing north (his front, once he faces whoever opened the perch there). */
	private static void maraxCamera(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch) {
		Vec3 at = server.computeOnServer(minecraft -> presenceOf(minecraft, perch).position());
		double middle = server.computeOnServer(minecraft -> presenceOf(minecraft, perch).getBbHeight() / 2.0);
		float pitch = (float) -Math.toDegrees(Math.atan2(middle - 1.62, 3.0));
		server.runCommand(String.format(Locale.ROOT, "tp @a %.1f %.1f %.1f 180 %.1f", at.x, at.y, at.z + 3.0, pitch));
		context.waitTicks(10);
	}

	/**
	 * His body from his front (three blocks south of him; he faces whoever opened the perch there): each loop as the server
	 * drives it and each gesture frozen inside its telling pose, the HUD hidden. Idle and the rest by day (the critique's item
	 * 5); his night look with the time set to midnight (Dossier 3 item 7a), then noon again; waiting with the Concordance
	 * switched off; his astir loop is not posed (an offering due would change the upkeep later steps read, as Buer's and
	 * Bathin's). Departing he turns, walks two steps and fades (held), and arriving replaces it.
	 */
	private static void maraxPoses(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch) {
		boolean hidden = hideHud(context);
		try {
			maraxCamera(context, server, perch);
			context.waitTicks(OPEN_GAP);
			open(context, server, perch);
			context.waitFor(client -> spiritAt(client, perch, MARAX) instanceof LivingEntity body
					&& Mth.degreesDifferenceAbs(body.yBodyRot, 0.0F) <= BATHIN_FACING_TOLERANCE, WAIT);
			context.getInput().pressKey(InputConstants.KEY_ESCAPE);
			context.waitForScreen(null);
			server.runCommand("time set noon");
			loopPoseOf(context, server, perch, MARAX, MARAX_IDLE, "jugcraft_garden_marax_idle", false);
			context.waitTicks(OPEN_GAP);
			open(context, server, perch);
			context.waitFor(client -> spiritAt(client, perch, MARAX) instanceof MaraxEntity marax && marax.listening(), WAIT);
			loopPoseOf(context, server, perch, MARAX, MARAX_LISTEN, "jugcraft_garden_marax_listen", true);
			context.waitFor(client -> MARAX_IDLE.equals(drawingOf(client, perch, MARAX, MAIN)), WAIT);
			AgreementHolder.Toggle suspended = server.computeOnServer(minecraft -> PactKeeping.suspend(minecraft.overworld(), player(minecraft),
					holder(minecraft, perch), minecraft.overworld().getGameTime()));
			check(suspended == AgreementHolder.Toggle.SUSPENDED, "Its holder suspends the pact: " + suspended);
			loopPoseOf(context, server, perch, MARAX, MARAX_SUSPENDED, "jugcraft_garden_marax_suspended", false);
			AgreementHolder.Toggle resumed = server.computeOnServer(minecraft -> PactKeeping.resume(minecraft.overworld(), player(minecraft),
					holder(minecraft, perch), minecraft.overworld().getGameTime()));
			check(resumed == AgreementHolder.Toggle.RESUMED, "and resumes it: " + resumed);
			context.waitFor(client -> MARAX_IDLE.equals(drawingOf(client, perch, MARAX, MAIN)), WAIT);
			boolean on = server.computeOnServer(minecraft -> JugcraftConfig.isFeatureEnabled(JugcraftConcordance.FEATURE));
			server.runOnServer(minecraft -> JugcraftConfig.setFeatureEnabled(JugcraftConcordance.FEATURE, false));
			try {
				loopPoseOf(context, server, perch, MARAX, MARAX_WAITING, "jugcraft_garden_marax_waiting", false);
			} finally {
				server.runOnServer(minecraft -> JugcraftConfig.setFeatureEnabled(JugcraftConcordance.FEATURE, on));
			}
			context.waitFor(client -> MARAX_IDLE.equals(drawingOf(client, perch, MARAX, MAIN)), WAIT);
			// Dossier 3 item 7(a): by night, between readings, he lifts his head to the sky (the client's own dark sky).
			server.runCommand("time set midnight");
			try {
				loopPoseOf(context, server, perch, MARAX, MARAX_STARGAZE, "jugcraft_garden_marax_stargaze", false);
			} finally {
				server.runCommand("time set noon");
			}
			context.waitFor(client -> MARAX_IDLE.equals(drawingOf(client, perch, MARAX, MAIN)), WAIT);
			clipPose(context, server, perch, MARAX, MaraxEntity.Gesture.INSPECT, 0.4, 1.2, "jugcraft_garden_marax_inspect");
			clipPose(context, server, perch, MARAX, MaraxEntity.Gesture.ASSENT, 0.3, 0.9, "jugcraft_garden_marax_assent");
			clipPose(context, server, perch, MARAX, MaraxEntity.Gesture.REFUSE, 0.3, 1.0, "jugcraft_garden_marax_refuse");
			clipPose(context, server, perch, MARAX, MaraxEntity.Gesture.WORK, 0.4, 1.2, "jugcraft_garden_marax_work");
			// He leaves (depart holds its last frame: nothing of him drawn) and comes back (his arrival replaces it).
			clipPose(context, server, perch, MARAX, MaraxEntity.Gesture.DEPART, 0.5, 1.1, "jugcraft_garden_marax_depart");
			clipPose(context, server, perch, MARAX, MaraxEntity.Gesture.ARRIVE, 0.9, 1.6, "jugcraft_garden_marax_arrive");
			context.waitFor(client -> drawingOf(client, perch, MARAX, GESTURES).isEmpty()
					&& MARAX_IDLE.equals(drawingOf(client, perch, MARAX, MAIN)), WAIT);
			boolean kept = server.computeOnServer(minecraft -> holds(minecraft, MARAX) && presenceOf(minecraft, perch).isAlive());
			check(kept, "His gestures change nothing: the pact is kept and he stands beside his perch");
		} finally {
			restoreHud(context, hidden);
		}
	}

	/** Dossier 3 item 6: End asks first; then his familiar settles, and he turns, walks two steps and fades; the pact ends once. */
	private static void maraxEnding(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch) {
		context.waitTicks(OPEN_GAP);
		open(context, server, perch);
		context.waitFor(client -> PactClient.state() != null && PactClient.state().mode() == PactStatePayload.KEEP, WAIT);
		UUID him = server.computeOnServer(minecraft -> presenceOf(minecraft, perch).getUUID());
		press(context, "end");
		context.waitForScreen(ConfirmScreen.class);
		boolean keepFocused = context.computeOnClient(client -> client.gui.screen().getFocused() instanceof AbstractWidget keep
				&& keep.getMessage().getString().equals(Component.translatable("screen.jugcraft.pact.end.no").getString()));
		check(keepFocused, "End asks first, the keyboard on Keep it");
		confirmBudget(context);
		shot(context, "jugcraft_garden_marax_end_confirm");
		context.getInput().pressKey(InputConstants.KEY_ESCAPE);
		context.waitForScreen(PactScreen.class);
		context.waitTicks(5);
		boolean stillHeld = server.computeOnServer(minecraft -> holds(minecraft, MARAX));
		check(stillHeld, "Keep it (Esc) ends nothing");
		press(context, "end");
		context.waitForScreen(ConfirmScreen.class);
		tabToLabel(context, Component.translatable("screen.jugcraft.pact.end.yes").getString());
		PactStatePayload.Result ended = answer(context);
		context.waitFor(client -> MaraxEntity.Gesture.DEPART.animation.equals(gesturedOf(client, perch, MARAX)), WAIT);
		RawAnimation going = context.computeOnClient(client -> gesturedOf(client, perch, MARAX));
		String after = server.computeOnServer(minecraft -> holds(minecraft, MARAX) + " "
				+ (minecraft.overworld().getEntity(him) instanceof MaraxEntity left ? "departing " + left.departing() : "gone"));
		int endedMode = context.computeOnClient(client -> PactClient.state().mode());
		Jugcraft.LOGGER.info("[garden-check] ended: {}; held, then his presence = {}", ended.reason(), after);
		check(ended.done() && "ended".equals(ended.reason()) && MaraxEntity.Gesture.DEPART.animation.equals(going)
				&& endedMode == PactStatePayload.CLOSED && after.startsWith("false"), "Ending ends the pact once; he turns, walks and fades: " + after);
		context.waitFor(client -> spiritAt(client, perch, MARAX) == null, WAIT);
		budget(context, ENDED_WORDS, "the ended page");
		shot(context, "jugcraft_garden_marax_ended");
		context.getInput().pressKey(InputConstants.KEY_ESCAPE);
		context.waitForScreen(null);
	}

	// GC-07 unit 4
	// ---------------------------------------------------------------- GC-07: Foras (Dossier 4)

	// GC-07: Foras (tools/concordance_pacts.py SPIRITS["foras"]; check_spirit_bodies).
	private static final String FORAS = "jugcraft:foras";
	private static final String FORAS_IDLE = "animation.foras.idle";
	private static final String FORAS_LISTEN = "animation.foras.listen";
	private static final String FORAS_WAITING = "animation.foras.waiting";
	private static final String FORAS_SUSPENDED = "animation.foras.suspended";
	/** His refusal at a perch with no store touching it (Dossier 4 item 2). */
	private static final String NO_STORE = "Where shall I keep the cuttings? Set a store beside me.";
	/** His compact (data/jugcraft/concordance/pact/foras.json): no resonance, so no astrolabe. */
	private static final List<String> FORAS_COMPACT = List.of("jugcraft:dewmoss 4", "minecraft:honeycomb 1");
	/** His facing shots in standingFacing's order (east, south, west, north), literal so check_pact_screen sees each. */
	private static final List<String> FORAS_FACES = List.of("jugcraft_garden_foras_faces_east", "jugcraft_garden_foras_faces_south",
			"jugcraft_garden_foras_faces_west", "jugcraft_garden_foras_faces_north");

	/**
	 * GC-07: Foras at the perch Bathin left, standing beside it (unit 3's Station.BESIDE), from the chooser to his departure;
	 * his matter raised, asked and answered on the kept page, and one graft frozen in his work. Noon and clear weather first,
	 * and noon again before his poses: in the merged order he follows Marax, whose flow may leave another hour or weather.
	 */
	private static void foras(ClientGameTestContext context, TestDedicatedServerContext server, TestDedicatedServerConnection connection,
			BlockPos perch) {
		server.runCommand("time set noon");
		server.runCommand("weather clear");
		BlockPos chest = perch.north();
		// A Sunpetal on a bed on the yard's floor, 3 east and 2 south of the perch.
		BlockPos host = perch.offset(3, 1, 2);
		server.runOnServer(minecraft -> yard(minecraft, perch));
		standSouth(context, server, perch);
		connection.waitForChunksRender();
		noStore(context, server, perch, host);
		forasChooser(context, server, perch, chest);
		forasConversation(context, server);
		forasSizes(context);
		Hearing hearing = hear(context);
		forasAcceptance(context, server, perch);
		forasHeard(context, server, hearing, perch);
		standSouth(context, server, perch);
		forasObjection(context, server, perch, host);
		forasMatter(context, server, perch);
		long grafted = forasGrafting(context, server, perch, chest, host);
		forasJade(context, server, perch);
		server.runCommand("time set noon");
		forasPoses(context, server, perch);
		standingFacing(context, server, perch, FORAS, FORAS_FACES);
		standSouth(context, server, perch);
		forasEnding(context, server, perch, grafted);
	}

	/** GC-07: Foras's presence at {@code perch}, asserted there (server side). */
	private static ForasEntity forasAt(MinecraftServer minecraft, BlockPos perch) {
		ServerLevel level = minecraft.overworld();
		PactSpiritEntity<?> found = ((PactPerchBlockEntity) level.getBlockEntity(perch)).presence(level);
		check(found instanceof ForasEntity, "Foras stands beside his perch: " + found);
		return (ForasEntity) found;
	}

	/**
	 * Dossier 4 item 2: a host of the player's (a Sunpetal on an awake, wet bed) but no store touching the perch: the chooser
	 * lists Foras not ready, one thing missing, his refusal in his words.
	 */
	private static void noStore(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch, BlockPos host) {
		server.runOnServer(minecraft -> {
			ServerLevel level = minecraft.overworld();
			level.setBlockAndUpdate(host.below(), Garden.VERDANT_BED.defaultBlockState().setValue(VerdantBedBlock.MOISTURE, VerdantBedBlock.WET));
			VerdantBedBlockEntity kept = bed(minecraft, host.below());
			kept.awaken(player(minecraft).getUUID());
			kept.setNutrients(4);
			level.setBlockAndUpdate(host, Garden.SUNPETAL_CROP.defaultBlockState().setValue(OrganismCropBlock.AGE, 1));
		});
		context.waitTicks(OPEN_GAP);
		open(context, server, perch);
		context.waitFor(client -> PactClient.state() != null && PactClient.state().mode() == PactStatePayload.INVITE, WAIT);
		String entries = context.computeOnClient(client -> entries(PactClient.state()));
		Jugcraft.LOGGER.info("[garden-check] chooser before his store: {}", entries);
		boolean listed = context.computeOnClient(client -> PactClient.state().patterns().stream()
				.anyMatch(choice -> choice.id().equals(ForasEntity.SIGN) && !choice.ready() && choice.amount() == 1)
				&& PactClient.state().notes().stream().anyMatch(note -> note.getString().contains(NO_STORE)));
		check(listed, "Foras listed, not ready, his store alone missing, in his words: " + entries);
		fits(context, "the chooser without his store");
		cardsWhole(context, "the chooser without his store");
		named(context);
		reasons(context);
		budget(context, chooserWords(context), "the chooser without his store");
		shot(context, "jugcraft_garden_foras_no_store");
		context.getInput().pressKey(InputConstants.KEY_ESCAPE);
		context.waitForScreen(null);
	}

	/**
	 * A chest touching the perch (north of it, so the side he takes stays clear); the chooser lists him ready; chosen by
	 * keyboard, he steps in with his gesture and stands on the ground two blocks west of the perch, a quarter turn clockwise
	 * from the player south of it (unit 3's Station.BESIDE, GC-P67).
	 */
	private static void forasChooser(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch, BlockPos chest) {
		server.runOnServer(minecraft -> minecraft.overworld().setBlockAndUpdate(chest, Blocks.CHEST.defaultBlockState()));
		context.waitTicks(25);
		open(context, server, perch);
		context.waitFor(client -> PactClient.state() != null && PactClient.state().mode() == PactStatePayload.INVITE
				&& PactClient.state().patterns().stream().anyMatch(choice -> choice.id().equals(ForasEntity.SIGN) && choice.ready()), WAIT);
		String entries = context.computeOnClient(client -> entries(PactClient.state()));
		Jugcraft.LOGGER.info("[garden-check] chooser with his store and host: {}", entries);
		context.waitFor(client -> client.gui.screen() instanceof PactScreen, WAIT);
		fits(context, "the chooser with his store");
		cardsWhole(context, "the chooser with his store");
		named(context);
		budget(context, chooserWords(context), "the chooser with his store");
		shot(context, "jugcraft_garden_foras_chooser");
		String name = context.computeOnClient(client -> Component.translatable("entity.jugcraft.foras").getString());
		tabToNamed(context, name);
		context.getInput().pressKey(InputConstants.KEY_RETURN);
		context.waitFor(client -> PactClient.state() != null && PactClient.state().mode() == PactStatePayload.NEGOTIATE
				&& ForasEntity.SIGN.equals(PactClient.state().spirit().sign()) && spiritAt(client, perch, FORAS) != null, WAIT);
		context.waitFor(client -> ForasEntity.Gesture.ARRIVE.animation.equals(gesturedOf(client, perch, FORAS)), WAIT);
		RawAnimation arrived = context.computeOnClient(client -> gesturedOf(client, perch, FORAS));
		Vec3 stands = server.computeOnServer(minecraft -> presenceOf(minecraft, perch).position());
		double off = stands.distanceTo(Vec3.atBottomCenterOf(perch.west(2)));
		Jugcraft.LOGGER.info("[garden-check] invited foras: gesture {}, stands at {} ({} from two blocks west of the perch)", arrived, stands, off);
		check(ForasEntity.Gesture.ARRIVE.animation.equals(arrived) && off < 1.0E-3,
				"Chosen, he steps in and stands on the ground two blocks west of the perch, clockwise of the player south of it: " + stands);
	}

	/** His three beats, then every term by keyboard (Enter three times), naming his grafts and no astrolabe or resonance (GB23). */
	private static void forasConversation(ClientGameTestContext context, TestDedicatedServerContext server) {
		String shown = context.computeOnClient(client -> describe(PactClient.state()));
		Jugcraft.LOGGER.info("[garden-check] foras's offer: {}", shown);
		boolean whole = context.computeOnClient(client -> PactClient.state().beats().size() == 3 && PactClient.state().notes().isEmpty()
				&& PactClient.state().offers(PactRequestPayload.ACCEPT)
				&& PactClient.state().terms().stream().map(PactStatePayload.Term::key).toList().equals(PactStatePayload.TERMS));
		check(whole, "Foras speaks three beats, nothing stands in the way, and his terms name every term, in order: " + shown);
		fits(context, "his beat at GUI scale 2");
		named(context);
		portraitWhole(context, FORAS);
		budget(context, BEAT_WORDS, "his first beat");
		shot(context, "jugcraft_garden_foras_conversation");
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
		check(!words.contains("astrolabe") && !words.contains("resonance") && words.contains("graft"),
				"His terms name his grafts and no astrolabe or resonance: " + words);
		fits(context, "his terms at GUI scale 2");
		named(context);
		budget(context, TERMS_WORDS, "his terms");
		shot(context, "jugcraft_garden_foras_terms");
		boolean held = server.computeOnServer(minecraft -> holds(minecraft, FORAS));
		check(!held, "Hearing his terms records nothing");
	}

	/** His terms and a beat at GUI scale 4 (1920 x 1080) and in a 320 x 180 window, the mouse off every chip. */
	private static void forasSizes(ClientGameTestContext context) {
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
		budget(context, TERMS_WORDS, "his terms at GUI scale 4");
		shot(context, "jugcraft_garden_foras_terms_scale4");
		press(context, "back");
		fits(context, "his beat at GUI scale 4");
		named(context);
		budget(context, BEAT_WORDS, "his last beat at GUI scale 4");
		shot(context, "jugcraft_garden_foras_conversation_scale4");
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
		budget(context, TERMS_WORDS, "his terms at 320x180");
		shot(context, "jugcraft_garden_foras_terms_small");
		press(context, "back");
		fits(context, "his beat at 320x180");
		named(context);
		budget(context, BEAT_WORDS, "his last beat at 320x180");
		shot(context, "jugcraft_garden_foras_conversation_small");
		press(context, "next");
		context.getInput().resizeWindow(width, height);
		context.waitTicks(10);
		boolean back = context.computeOnClient(client -> screen(client).page() == PactScreen.TERMS_PAGE);
		check(back, "Back on his terms");
	}

	/**
	 * Accept refused without his compact (worded without an astrolabe; he objects with his refuse gesture), then sealed with
	 * it: 4 Dewmoss and a honeycomb, spent once; the kept page opens.
	 */
	private static void forasAcceptance(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch) {
		tabTo(context, "accept");
		PactStatePayload.Result refused = answer(context);
		String said = refused.message().getString();
		int refusedMode = context.computeOnClient(client -> PactClient.state().mode());
		Jugcraft.LOGGER.info("[garden-check] Accept without his compact: {} ({})", refused.reason(), said);
		check("cannot_afford".equals(refused.reason()) && !refused.done() && !said.toLowerCase(Locale.ROOT).contains("astrolabe")
				&& refusedMode == PactStatePayload.NEGOTIATE, "Without his compact Accept is refused, worded without an astrolabe: " + said);
		context.waitFor(client -> ForasEntity.Gesture.REFUSE.animation.equals(gesturedOf(client, perch, FORAS)), WAIT);
		boolean heldAfterRefusal = server.computeOnServer(minecraft -> holds(minecraft, FORAS));
		check(!heldAfterRefusal, "A refusal records and spends nothing");
		context.waitFor(client -> screen(client).control("result") instanceof PactScreen.Verdict verdict && verdict.refused(), WAIT);
		budget(context, TERMS_WORDS, "his refusal");
		shot(context, "jugcraft_garden_foras_refused");
		List<String> cost = context.computeOnClient(client -> PactClient.state().compact().stream()
				.map(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()) + " " + stack.getCount()).toList());
		int resonance = context.computeOnClient(client -> PactClient.state().resonance());
		Jugcraft.LOGGER.info("[garden-check] his compact: {} and {} resonance", cost, resonance);
		check(cost.equals(FORAS_COMPACT) && resonance == 0, "His compact: 4 Dewmoss and a honeycomb, no resonance: " + cost);
		server.runOnServer(minecraft -> cost.forEach(line -> player(minecraft).getInventory().add(stack(line))));
		context.waitTicks(5);
		tabTo(context, "accept");
		PactStatePayload.Result sealed = answer(context);
		int sealedMode = context.computeOnClient(client -> PactClient.state().mode());
		check(sealed.done() && "sealed".equals(sealed.reason()) && sealedMode == PactStatePayload.KEEP, "Accepting with his compact seals the pact: " + sealed.reason());
		String spent = server.computeOnServer(minecraft -> holds(minecraft, FORAS) + " "
				+ cost.stream().mapToInt(line -> player(minecraft).getInventory().countItem(stack(line).getItem())).sum());
		Jugcraft.LOGGER.info("[garden-check] sealed: held, compact left = {}", spent);
		check("true 0".equals(spent), "Sealed once, his compact spent once: " + spent);
		context.waitFor(client -> screen(client).control("result") instanceof PactScreen.Verdict verdict && !verdict.refused(), WAIT);
		fits(context, "his kept pact at GUI scale 2");
		named(context);
		budget(context, keptWords(context), "his kept pact");
		shot(context, "jugcraft_garden_foras_kept");
	}

	/** His arrive, listen, refuse and assent have their subtitles; the server fired his refusal, then his assent; heard where sound plays. */
	private static void forasHeard(ClientGameTestContext context, TestDedicatedServerContext server, Hearing hearing, BlockPos perch) {
		List<String> missing = new ArrayList<>();
		for (String cue : List.of("arrive", "listen", "refuse", "assent")) {
			String words = context.computeOnClient(client -> Component.translatable("subtitles.jugcraft.foras." + cue).getString());
			boolean defined = context.computeOnClient(client -> words.equals(subtitle(client, "foras." + cue)));
			if (words.startsWith("subtitles.") || !defined) {
				missing.add(cue);
			}
		}
		check(missing.isEmpty(), "His cues have their subtitles in the client's sounds: missing " + missing);
		String refuse = context.computeOnClient(client -> Component.translatable("subtitles.jugcraft.foras.refuse").getString());
		String assent = context.computeOnClient(client -> Component.translatable("subtitles.jugcraft.foras.assent").getString());
		List<String> cues = server.computeOnServer(minecraft -> presenceOf(minecraft, perch).cues());
		Jugcraft.LOGGER.info("[garden-check] Foras's cues so far: {}", cues);
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
	 * Dossier 4 item 3 (GC-P86): his host's bed read (the yard lies in full sky at noon: the graft would not help it), his index
	 * forgotten and his think run in the same server step: he names the host, turning to it with his inspect gesture, and
	 * awaits the answer; the roster notes him awaiting it ([garden-check] only: unit 2 photographed the roster).
	 */
	private static void forasObjection(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch, BlockPos host) {
		context.getInput().pressKey(InputConstants.KEY_ESCAPE);
		context.waitForScreen(null);
		List<String> raised = server.computeOnServer(minecraft -> {
			ServerLevel level = minecraft.overworld();
			boolean read = bed(minecraft, host.below()).habitat(level) != null;
			ForasEntity foras = forasAt(minecraft, perch);
			boolean before = foras.cues().contains("inspect");
			GraftKeeping.Memory memory = foras.serviceMemory(GraftKeeping.Memory.class, GraftKeeping.Memory::new);
			memory.forget();
			Status status = foras.think(level, NOON, level.getGameTime());
			String inspect = before ? "already raised by his own think" : foras.cues().contains("inspect") ? "raised by this think" : "not raised";
			return List.of(status.id, String.valueOf(read), inspect, memory.useless().toString());
		});
		Jugcraft.LOGGER.info("[garden-check] his host read and his think: status {}, read {}, his matter {}, hosts he would not graft {}",
				raised.get(0), raised.get(1), raised.get(2), raised.get(3));
		check("awaiting_answer".equals(raised.get(0)) && "true".equals(raised.get(1)) && !"not raised".equals(raised.get(2))
				&& raised.get(3).equals(List.of(host).toString()), "He names the host the graft would not help and awaits the answer: " + raised);
		String roster = server.computeOnServer(minecraft -> {
			WorkerRoster.Entry entry = WorkerRoster.of(minecraft).workers(player(minecraft).getUUID()).get(forasAt(minecraft, perch).getUUID());
			return entry == null ? "none" : entry.status();
		});
		String awaiting = context.computeOnClient(client -> Component.translatable("compose.jugcraft.worker.status.awaiting_answer").getString());
		Jugcraft.LOGGER.info("[garden-check] the roster notes Foras {} (\"{}\")", roster, awaiting);
		check("awaiting_answer".equals(roster) && awaiting.contains("awaiting your answer"), "The roster says Foras is awaiting your answer: " + roster);
	}

	/**
	 * Dossier 4 items 3 and 8: his matter on his kept page, by keyboard: the row and the question with two answers, none kept,
	 * his matter a narrated line; it fits at GUI 2, GUI 4 and 320x180, every control named; Enter on "Graft every host" keeps
	 * it at the perch (RESOLVE), marked, and he assents. The page is closed at the end, so his graft is shown with the HUD.
	 */
	private static void forasMatter(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch) {
		context.waitTicks(OPEN_GAP);
		open(context, server, perch);
		// The state first, then the page built from it: the screen rebuilds a frame after its state arrives.
		context.waitFor(client -> PactClient.state() != null && PactClient.state().mode() == PactStatePayload.KEEP
				&& PactClient.state().spirit().answers().size() == 2 && screen(client).control("answer:1") != null, WAIT);
		settle(context);
		String shown = context.computeOnClient(client -> PactClient.state().spirit().matter().getString() + " / "
				+ PactClient.state().spirit().answers().stream().map(answer -> answer.name().getString() + "=" + answer.amount()).toList() + " / "
				+ PactClient.state().notes().stream().map(Component::getString).toList());
		Jugcraft.LOGGER.info("[garden-check] his matter on the kept page: {}", shown);
		boolean asked = context.computeOnClient(client -> PactClient.state().notes().stream().anyMatch(note -> note.getString().contains("awaits your answer"))
				&& PactClient.state().spirit().answers().stream().allMatch(answer -> answer.amount() == 0)
				&& screen(client).control("matter") != null && screen(client).control("answer:0") != null && screen(client).control("answer:1") != null);
		check(asked, "The page says a matter waits and asks it, two answers, none kept: " + shown);
		tabTo(context, "matter");
		String narrated = context.computeOnClient(client -> screen(client).control("matter").getMessage().getString());
		check(!narrated.isBlank(), "His matter is a focusable, narrated line: " + narrated);
		fits(context, "his matter at GUI scale 2");
		named(context);
		budget(context, keptWords(context) + firstTask(context), "his matter");
		shot(context, "jugcraft_garden_foras_matter");
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
		budget(context, keptWords(context) + firstTask(context), "his matter at GUI scale 4");
		shot(context, "jugcraft_garden_foras_matter_scale4");
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
		budget(context, keptWords(context) + firstTask(context), "his matter at 320x180");
		shot(context, "jugcraft_garden_foras_matter_small");
		context.getInput().resizeWindow(width, height);
		context.waitTicks(10);
		tabTo(context, "answer:1");
		PactStatePayload.Result always = answer(context);
		String kept = server.computeOnServer(minecraft -> String.valueOf(((PactPerchBlockEntity) minecraft.overworld().getBlockEntity(perch)).accord()));
		boolean marked = context.computeOnClient(client -> PactClient.state().spirit().answers().get(0).amount() == 0
				&& PactClient.state().spirit().answers().get(1).amount() == 1);
		check(always.done() && "resolved".equals(always.reason()) && GraftKeeping.ALWAYS.equals(kept) && marked,
				"Enter on Graft every host keeps it at the perch, marked: " + always.reason() + " " + kept);
		context.waitFor(client -> ForasEntity.Gesture.ASSENT.animation.equals(gesturedOf(client, perch, FORAS)), WAIT);
		context.waitFor(client -> screen(client).control("result") instanceof PactScreen.Verdict verdict && !verdict.refused(), WAIT);
		String focused = context.computeOnClient(client -> screen(client).focusedKey());
		check("answer:1".equals(focused), "The keyboard stays on the answer it pressed: " + focused);
		budget(context, keptWords(context) + firstTask(context), "his matter answered");
		shot(context, "jugcraft_garden_foras_answered");
		context.getInput().pressKey(InputConstants.KEY_ESCAPE);
		context.waitForScreen(null);
	}

	/**
	 * Dossier 4 details (a) and (b): two Dewmoss put into his store and his think run in the same server step (so his perch's
	 * own think cannot graft first): the host is grafted with one of them. The world is frozen 23 ticks into his work, inside
	 * its telling window (0.8 to 1.5 s), his cap tipped, the action bar saying his count; photographed with the HUD, no screen
	 * open (the action bar is the HUD's). Returns the game time of the graft (his count is a game day's).
	 */
	private static long forasGrafting(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch, BlockPos chest, BlockPos host) {
		List<String> did = server.computeOnServer(minecraft -> {
			ServerLevel level = minecraft.overworld();
			((ChestBlockEntity) level.getBlockEntity(chest)).setItem(0, new ItemStack(Garden.DEWMOSS, 2));
			ForasEntity foras = forasAt(minecraft, perch);
			foras.serviceMemory(GraftKeeping.Memory.class, GraftKeeping.Memory::new).forget();
			return List.of(foras.think(level, NOON, level.getGameTime()).id, String.valueOf(level.getGameTime()));
		});
		String status = did.get(0);
		long grafted = Long.parseLong(did.get(1));
		Jugcraft.LOGGER.info("[garden-check] foras's think with donors in his store: {} (game time {})", status, grafted);
		context.waitTicks(23);
		server.runCommand("tick freeze");
		try {
			context.waitTicks(5);
			String clip = ForasEntity.Gesture.WORK.clip();
			double at = context.computeOnClient(client -> clip.equals(drawingOf(client, perch, FORAS, GESTURES))
					? controllerOf(client, perch, FORAS, GESTURES).getCurrentAnimationTime() : -1.0);
			String graft = server.computeOnServer(minecraft -> bed(minecraft, host.below()).graft());
			int left = server.computeOnServer(minecraft -> ((ChestBlockEntity) minecraft.overworld().getBlockEntity(chest)).countItem(Garden.DEWMOSS));
			Jugcraft.LOGGER.info("[garden-check] foras grafted: status {}, graft {}, Dewmoss left {}, work clip at {} s", status, graft, left, at);
			check("working".equals(status) && "jugcraft:dewlit".equals(graft) && left == 1, "He grafts the host with one Dewmoss from his store");
			check(at >= 0.8 && at <= 1.5, "Frozen in his work's telling window, his cap tipped: " + at);
			context.waitForScreen(null);
			shot(context, "jugcraft_garden_foras_grafting");
		} finally {
			server.runCommand("tick unfreeze");
		}
		context.waitTicks(ForasEntity.Gesture.WORK.ticks - 23);
		return grafted;
	}

	/**
	 * Jade's readings of his perch and of him (read on the server: no Jade class): his pact, service and state, and his note
	 * that nothing is left to graft (his only host grafted), naming no player; with Jade loaded, his overlay.
	 */
	private static void forasJade(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch) {
		untilServer(context, server, minecraft -> "no_host".equals(PactReading.presence(minecraft.overworld(), forasAt(minecraft, perch),
				minecraft.overworld().getGameTime()).getStringOr("note", "")), "Foras's note says nothing is left to graft");
		String readings = server.computeOnServer(minecraft -> {
			ServerLevel level = minecraft.overworld();
			CompoundTag ofPerch = PactReading.holder(level, (PactPerchBlockEntity) level.getBlockEntity(perch), level.getGameTime());
			CompoundTag ofHim = PactReading.presence(level, forasAt(minecraft, perch), level.getGameTime());
			String both = ofPerch + " " + ofHim;
			boolean right = "kept".equals(ofPerch.getStringOr("standing", "")) && FORAS.equals(ofPerch.getStringOr("counterpart", ""))
					&& GraftKeeping.WORK.equals(ofPerch.getStringOr("service", "")) && "active".equals(ofPerch.getStringOr("state", ""))
					&& ofPerch.getIntOr("due_days", -1) == 7 && "kept".equals(ofHim.getStringOr("standing", ""))
					&& "no_host".equals(ofHim.getStringOr("note", "")) && GraftKeeping.ALWAYS.equals(ofPerch.getStringOr("accord", ""))
					&& PactReading.KEYS.containsAll(ofPerch.keySet()) && PactReading.KEYS.containsAll(ofHim.keySet())
					&& !both.contains(player(minecraft).getUUID().toString()) && !both.contains(player(minecraft).getName().getString());
			return (right ? "ok " : "bad ") + both;
		});
		Jugcraft.LOGGER.info("[garden-check] Jade's readings of his perch and of Foras: {}", readings);
		check(readings.startsWith("ok"), "Jade's readings: his pact, service, state, answer and note, naming no player: " + readings);
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
				Vec3 at = server.computeOnServer(minecraft -> forasAt(minecraft, perch).position());
				context.getInput().lookAt(BlockPos.containing(at.x, at.y + 1.1, at.z));
				context.waitTicks(20);
				shot(context, "jugcraft_garden_foras_jade");
			} finally {
				restoreHud(context, hidden);
			}
		} else {
			Jugcraft.LOGGER.info("[garden-check] Jade is not loaded: no Jade shot of Foras");
		}
	}

	/**
	 * His body from his front: the camera 4 blocks south of where he stands (read from the server), facing north at his
	 * middle (he faces whoever opened the perch there); four loops as the server drives them (idle, listen, suspended,
	 * waiting; astir not posed: an offering due would change the upkeep later steps read) and his six gestures, each frozen
	 * inside its telling pose, the HUD hidden. Waiting is posed with the Concordance switched off (he serves at any hour).
	 */
	private static void forasPoses(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch) {
		Vec3 at = server.computeOnServer(minecraft -> forasAt(minecraft, perch).position());
		double middle = server.computeOnServer(minecraft -> {
			ForasEntity foras = forasAt(minecraft, perch);
			return foras.getY() + foras.getBbHeight() / 2.0;
		});
		float pitch = (float) -Math.toDegrees(Math.atan2(middle - (at.y + 1.62), 4.0));
		boolean hidden = hideHud(context);
		try {
			server.runCommand(String.format(Locale.ROOT, "tp @a %.2f %.2f %.2f 180 %.1f", at.x, at.y, at.z + 4.0, pitch));
			context.waitTicks(OPEN_GAP);
			open(context, server, perch);
			context.waitFor(client -> spiritAt(client, perch, FORAS) instanceof LivingEntity body
					&& Mth.degreesDifferenceAbs(body.yBodyRot, 0.0F) <= BATHIN_FACING_TOLERANCE, WAIT);
			context.getInput().pressKey(InputConstants.KEY_ESCAPE);
			context.waitForScreen(null);
			loopPoseOf(context, server, perch, FORAS, FORAS_IDLE, "jugcraft_garden_foras_idle", false);
			context.waitTicks(OPEN_GAP);
			open(context, server, perch);
			context.waitFor(client -> spiritAt(client, perch, FORAS) instanceof ForasEntity foras && foras.listening(), WAIT);
			loopPoseOf(context, server, perch, FORAS, FORAS_LISTEN, "jugcraft_garden_foras_listen", true);
			context.waitFor(client -> FORAS_IDLE.equals(drawingOf(client, perch, FORAS, MAIN)), WAIT);
			AgreementHolder.Toggle suspended = server.computeOnServer(minecraft -> PactKeeping.suspend(minecraft.overworld(), player(minecraft),
					holder(minecraft, perch), minecraft.overworld().getGameTime()));
			check(suspended == AgreementHolder.Toggle.SUSPENDED, "Its holder suspends the pact: " + suspended);
			loopPoseOf(context, server, perch, FORAS, FORAS_SUSPENDED, "jugcraft_garden_foras_suspended", false);
			AgreementHolder.Toggle resumed = server.computeOnServer(minecraft -> PactKeeping.resume(minecraft.overworld(), player(minecraft),
					holder(minecraft, perch), minecraft.overworld().getGameTime()));
			check(resumed == AgreementHolder.Toggle.RESUMED, "and resumes it: " + resumed);
			context.waitFor(client -> FORAS_IDLE.equals(drawingOf(client, perch, FORAS, MAIN)), WAIT);
			boolean on = server.computeOnServer(minecraft -> JugcraftConfig.isFeatureEnabled(JugcraftConcordance.FEATURE));
			server.runOnServer(minecraft -> JugcraftConfig.setFeatureEnabled(JugcraftConcordance.FEATURE, false));
			try {
				loopPoseOf(context, server, perch, FORAS, FORAS_WAITING, "jugcraft_garden_foras_waiting", false);
			} finally {
				server.runOnServer(minecraft -> JugcraftConfig.setFeatureEnabled(JugcraftConcordance.FEATURE, on));
			}
			context.waitFor(client -> FORAS_IDLE.equals(drawingOf(client, perch, FORAS, MAIN)), WAIT);
			clipPose(context, server, perch, FORAS, ForasEntity.Gesture.INSPECT, 0.4, 1.2, "jugcraft_garden_foras_inspect");
			clipPose(context, server, perch, FORAS, ForasEntity.Gesture.ASSENT, 0.4, 1.0, "jugcraft_garden_foras_assent");
			clipPose(context, server, perch, FORAS, ForasEntity.Gesture.REFUSE, 0.4, 1.3, "jugcraft_garden_foras_refuse");
			clipPose(context, server, perch, FORAS, ForasEntity.Gesture.WORK, 0.8, 1.5, "jugcraft_garden_foras_work");
			// He fades (depart holds its last frame) and steps in again (his arrival replaces it).
			clipPose(context, server, perch, FORAS, ForasEntity.Gesture.DEPART, 0.4, 1.2, "jugcraft_garden_foras_depart");
			clipPose(context, server, perch, FORAS, ForasEntity.Gesture.ARRIVE, 0.6, 1.4, "jugcraft_garden_foras_arrive");
			context.waitFor(client -> drawingOf(client, perch, FORAS, GESTURES).isEmpty()
					&& FORAS_IDLE.equals(drawingOf(client, perch, FORAS, MAIN)), WAIT);
			boolean kept = server.computeOnServer(minecraft -> holds(minecraft, FORAS) && forasAt(minecraft, perch).isAlive());
			check(kept, "His gestures change nothing: the pact is kept and he stands beside his perch");
		} finally {
			restoreHud(context, hidden);
		}
	}

	/**
	 * Dossier 4 detail (b) and item 6: his kept page counts today's grafts against his quota (one, unless the game day turned
	 * since his graft); answered, with no matter raised now (his only host grafted), it draws his question in general, short,
	 * within the kept page's budget; End asks first; then he folds his satchel and fades (his depart held, removed after it),
	 * the pact ended once.
	 */
	private static void forasEnding(ClientGameTestContext context, TestDedicatedServerContext server, BlockPos perch, long grafted) {
		context.waitTicks(OPEN_GAP);
		open(context, server, perch);
		context.waitFor(client -> PactClient.state() != null && PactClient.state().mode() == PactStatePayload.KEEP, WAIT);
		settle(context);
		long now = server.computeOnServer(minecraft -> minecraft.overworld().getGameTime());
		int count = Math.floorDiv(now, Pacts.DAY) == Math.floorDiv(grafted, Pacts.DAY) ? 1 : 0;
		List<String> rows = context.computeOnClient(client -> PactClient.state().notes().stream().map(Component::getString).toList());
		List<String> drawn = context.computeOnClient(client -> screen(client).drawn());
		Jugcraft.LOGGER.info("[garden-check] his kept page ({} graft today expected): rows {}, drawn {}", count, rows, drawn);
		check(rows.stream().anyMatch(row -> row.contains(count + " of 8 grafts today")) && String.join(" ", drawn).contains(count + "/8 today"),
				"His kept page counts today's grafts against his quota (detail b): " + rows + " " + drawn);
		String question = context.computeOnClient(client -> Component.translatable("screen.jugcraft.pact.glance.graft_keeping.question").getString());
		check(String.join(" ", drawn).contains(question), "Answered, no matter raised now: his question in general, drawn short: " + drawn);
		budget(context, keptWords(context) + firstTask(context), "his kept page answered");
		shot(context, "jugcraft_garden_foras_counted");
		UUID him = server.computeOnServer(minecraft -> forasAt(minecraft, perch).getUUID());
		press(context, "end");
		context.waitForScreen(ConfirmScreen.class);
		boolean keepFocused = context.computeOnClient(client -> client.gui.screen().getFocused() instanceof AbstractWidget keep
				&& keep.getMessage().getString().equals(Component.translatable("screen.jugcraft.pact.end.no").getString()));
		check(keepFocused, "End asks first, the keyboard on Keep it");
		confirmBudget(context);
		shot(context, "jugcraft_garden_foras_end_confirm");
		context.getInput().pressKey(InputConstants.KEY_ESCAPE);
		context.waitForScreen(PactScreen.class);
		context.waitTicks(5);
		boolean stillHeld = server.computeOnServer(minecraft -> holds(minecraft, FORAS));
		check(stillHeld, "Keep it (Esc) ends nothing");
		press(context, "end");
		context.waitForScreen(ConfirmScreen.class);
		tabToLabel(context, Component.translatable("screen.jugcraft.pact.end.yes").getString());
		PactStatePayload.Result ended = answer(context);
		context.waitFor(client -> ForasEntity.Gesture.DEPART.animation.equals(gesturedOf(client, perch, FORAS)), WAIT);
		RawAnimation going = context.computeOnClient(client -> gesturedOf(client, perch, FORAS));
		String after = server.computeOnServer(minecraft -> holds(minecraft, FORAS) + " "
				+ (minecraft.overworld().getEntity(him) instanceof ForasEntity left ? "departing " + left.departing() : "gone"));
		int endedMode = context.computeOnClient(client -> PactClient.state().mode());
		Jugcraft.LOGGER.info("[garden-check] ended: {}; held, then his presence = {}", ended.reason(), after);
		check(ended.done() && "ended".equals(ended.reason()) && ForasEntity.Gesture.DEPART.animation.equals(going)
				&& endedMode == PactStatePayload.CLOSED && after.startsWith("false"), "Ending ends the pact once; he folds his satchel and fades: " + after);
		context.waitFor(client -> spiritAt(client, perch, FORAS) == null, WAIT);
		budget(context, ENDED_WORDS, "the ended page");
		shot(context, "jugcraft_garden_foras_ended");
		context.getInput().pressKey(InputConstants.KEY_ESCAPE);
		context.waitForScreen(null);
	}

	// GC-08 unit 5
	// GC-09 unit 6
}
