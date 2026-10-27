package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import io.github.jimbozoomer.jugcraft.lair.ChapelMirrorBlock;
import io.github.jimbozoomer.jugcraft.lair.GlassAccordRite;
import io.github.jimbozoomer.jugcraft.lair.GlassChapel;
import io.github.jimbozoomer.jugcraft.lair.GlassChapel.Mirror;
import io.github.jimbozoomer.jugcraft.lair.JugcraftLairs;
import io.github.jimbozoomer.jugcraft.lair.Lair;
import io.github.jimbozoomer.jugcraft.lair.Lairs;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The Glass Chapel and the Glass Accord (docs/features/glass-chapel.md), as far as a game-test server can show them. Its
 * world is built from the flat preset alone, so it has no lair dimension: the chapel's own files load (dimension type,
 * biome and template), the Accord's checks hold (the day, clear weather, the sun and glass over the holder), an Accord
 * that can open no instance is not used up, the Accord is crafted from glass, gold, amethyst and a spyglass, the
 * mirrors turn round their stops and keep their rules, and the beam worked out from them comes home exactly when the two
 * marked mirrors stand at their pair's stops. Everything that needs the chapel itself (the mist rising, standing in the
 * narthex, following, the nave, the Grey Mist, the instance closing) runs in a real world, in
 * {@link GlassChapelClientGameTests}.
 */
public class GlassChapelGameTests {
	private static final Lair CHAPEL = Lair.GLASS_CHAPEL;
	private static final BlockPos STAND = new BlockPos(2, 2, 2);
	/** High over the test's floor, where the sky is open and full sky light falls. */
	private static final int HIGH = 120;

	private static ServerPlayer player(GameTestHelper helper, BlockPos standAt, int y) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		BlockPos absolute = helper.absolutePos(standAt);
		player.snapTo(absolute.getX() + 0.5, y, absolute.getZ() + 0.5, 180.0F, 0.0F);
		return player;
	}

	/** The Glass Chapel's dimension type and biome load from their data pack files. */
	@GameTest
	public void glassChapelDimensionFilesLoad(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		helper.assertTrue(level.registryAccess().lookupOrThrow(Registries.DIMENSION_TYPE).containsKey(CHAPEL.dimension.identifier()),
				"No dimension type " + CHAPEL.dimension.identifier());
		helper.assertTrue(level.registryAccess().lookupOrThrow(Registries.BIOME).containsKey(CHAPEL.dimension.identifier()),
				"No biome " + CHAPEL.dimension.identifier());
		helper.succeed();
	}

	/**
	 * The template loads in the running game with the mirrors, the Abbot's Lectern, the lancet and rose glass, the copper
	 * lines, the candles and the Grey Mist (DataVersion the game's), at the size tools/glass_chapel.py gives
	 * Lair.GLASS_CHAPEL, and with nothing in it that changes by itself once placed.
	 */
	@GameTest
	public void glassChapelTemplateLoads(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		StructureTemplate template = Lairs.template(level, CHAPEL);
		helper.assertTrue(template != null, "No template " + CHAPEL.template);
		helper.assertTrue(template.getSize().equals(new Vec3i(CHAPEL.width, CHAPEL.height, CHAPEL.length)),
				"The template is " + template.getSize() + ", not the Lair's size");
		Identifier path = Identifier.fromNamespaceAndPath(CHAPEL.template.getNamespace(), "structure/" + CHAPEL.template.getPath() + ".nbt");
		CompoundTag tag;
		try (InputStream in = level.getServer().getResourceManager().getResourceOrThrow(path).open()) {
			tag = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
		} catch (IOException e) {
			throw helper.assertionException("Could not read " + path + ": " + e);
		}
		int file = tag.getIntOr("DataVersion", -1);
		StructureTemplate loaded = new StructureTemplate();
		loaded.load(level.registryAccess().lookupOrThrow(Registries.BLOCK), tag);
		CompoundTag saved = loaded.save(new CompoundTag());
		helper.assertTrue(file == saved.getIntOr("DataVersion", -1), "The template's DataVersion " + file + " is not the game's");
		ListTag palette = saved.getListOrEmpty("palette");
		List<String> names = new ArrayList<>();
		for (int i = 0; i < palette.size(); i++) {
			names.add(palette.getCompoundOrEmpty(i).getStringOr("id", "?"));
		}
		for (String wanted : List.of("jugcraft:chapel_mirror", "jugcraft:abbot_lectern", "jugcraft:lancet_glass", "jugcraft:rose_glass",
				"jugcraft:lair_exit", "minecraft:waxed_cut_copper", "minecraft:candle", "minecraft:chiseled_stone_bricks")) {
			helper.assertTrue(names.contains(wanted), "The template has no " + wanted + ": " + names);
		}
		for (String name : names) {
			helper.assertTrue(!name.endsWith("_leaves") && !name.equals("minecraft:vine") && !name.equals("minecraft:grass_block"),
					"The template has " + name + ", which would change once placed");
		}
		helper.succeed();
	}

	/**
	 * The Accord's checks, changing nothing: by day, in clear weather, in full sunlight that falls through glass within
	 * {@value GlassAccordRite#GLASS_REACH} blocks over the holder's head (a pane, or a block of stained glass), it is
	 * answered; at night, in rain, with no glass over the holder, or with the glass too high, it is refused. Under a roof
	 * that keeps the sun out (once the light has settled) it is refused. Tinted glass, which keeps the light out, does not
	 * count. No mist rises where nobody held an Accord.
	 */
	@GameTest(maxTicks = 200)
	public void theAccordWantsTheSunThroughGlassByDay(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos feet = helper.absolutePos(STAND).atY(HIGH);
		BlockPos glass = feet.above(4);
		BlockPos roof = feet.above(8);
		// A few ticks for the light up here to be worked out, then the checks; then a roof, and the light settles under it.
		helper.runAfterDelay(5, () -> {
			helper.assertTrue(GlassAccordRite.check(level, feet, true, true) == GlassAccordRite.Missing.NO_GLASS,
					"With nothing over the holder the Accord was not refused for want of glass: " + GlassAccordRite.check(level, feet, true, true));
			level.setBlock(glass, Blocks.GLASS_PANE.defaultBlockState(), Block.UPDATE_ALL);
			helper.assertTrue(GlassAccordRite.check(level, feet, true, true) == null, "Under a pane by day the Accord was refused: "
					+ GlassAccordRite.check(level, feet, true, true));
			helper.assertTrue(GlassAccordRite.check(level, feet, false, true) == GlassAccordRite.Missing.NIGHT, "At night the Accord was not refused");
			helper.assertTrue(GlassAccordRite.check(level, feet, true, false) == GlassAccordRite.Missing.CLOUDED, "In rain the Accord was not refused");
			level.setBlock(glass, Blocks.BLUE_STAINED_GLASS.defaultBlockState(), Block.UPDATE_ALL);
			helper.assertTrue(GlassAccordRite.check(level, feet, true, true) == null, "Under stained glass the Accord was refused");
			level.setBlock(glass, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
			BlockPos far = feet.above(GlassAccordRite.GLASS_REACH + 2);
			level.setBlock(far, Blocks.GLASS.defaultBlockState(), Block.UPDATE_ALL);
			helper.assertTrue(GlassAccordRite.check(level, feet, true, true) == GlassAccordRite.Missing.NO_GLASS,
					"Glass " + (GlassAccordRite.GLASS_REACH + 1) + " blocks over the holder's head counted");
			level.setBlock(far, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
			helper.assertTrue(!Blocks.TINTED_GLASS.defaultBlockState().is(GlassAccordRite.GLASS), "Tinted glass counts as glass the sun falls through");
			helper.assertTrue(GlassAccordRite.mist(level, feet) == null, "A mist rose where nobody held an Accord");
			level.setBlock(glass, Blocks.GLASS.defaultBlockState(), Block.UPDATE_ALL);
			level.setBlock(roof, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		});
		helper.succeedWhen(() -> {
			helper.assertTrue(level.getBlockState(roof).is(Blocks.STONE) && GlassAccordRite.check(level, feet, true, true) == GlassAccordRite.Missing.SHADED,
					"Under a roof that keeps the sun out the Accord was not refused: " + GlassAccordRite.check(level, feet, true, true));
			level.setBlock(roof, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
			level.setBlock(glass, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
		});
	}

	/**
	 * The Accord held up in full when no instance can open (a game-test server has no lair dimension, and the one slot
	 * {@code lairs.instances} allows is taken if it has): the chapel is full, the Accord is kept, nobody moves and no mist
	 * rises. At night it is refused the same way. Held up from the hand, it is rested after its use, answered or not.
	 */
	@GameTest
	public void theAccordUsesNothingWhenNoChapelCanOpen(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		String instances = JugcraftConfig.textOption("lairs.instances");
		BlockPos glass = helper.absolutePos(STAND).atY(HIGH + 5);
		try {
			JugcraftConfig.setTextOption("lairs.instances", "1");
			Lairs.open(level.getServer(), CHAPEL);  // null here; takes the only slot where there is a Glass Chapel
			level.setBlock(glass, Blocks.GLASS.defaultBlockState(), Block.UPDATE_ALL);
			ServerPlayer player = player(helper, STAND, HIGH);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(JugcraftLairs.GLASS_ACCORD, 2));
			Vec3 stood = player.position();
			GlassAccordRite.Holding holding = GlassAccordRite.hold(level, player, player.getMainHandItem(), true, true);
			helper.assertTrue(holding.missing() == GlassAccordRite.Missing.FULL && holding.instance() == null, "The chapel was not full: " + holding);
			helper.assertTrue(player.getMainHandItem().getCount() == 2, "The Accord was used up: " + player.getMainHandItem());
			helper.assertTrue(player.level() == level && player.position().distanceTo(stood) < 1.0E-6 && player.getAttached(Lairs.VISIT) == null,
					"The player was moved");
			helper.assertTrue(GlassAccordRite.mist(level, BlockPos.containing(stood)) == null, "A mist rose into no chapel");
			helper.assertTrue(GlassAccordRite.hold(level, player, player.getMainHandItem(), false, true).missing() == GlassAccordRite.Missing.NIGHT
					&& player.getMainHandItem().getCount() == 2, "At night the Accord was not refused, or was used up");
			InteractionResult used = player.gameMode.useItem(player, level, player.getMainHandItem(), InteractionHand.MAIN_HAND);
			helper.assertTrue(used.consumesAction() && player.getMainHandItem().getCount() == 2
					&& player.getCooldowns().isOnCooldown(player.getMainHandItem()), "Held up from the hand, the Accord was used up or not rested: " + used);
		} finally {
			level.setBlock(glass, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
			JugcraftConfig.setTextOption("lairs.instances", instances);
			Lairs.reset();
		}
		helper.succeed();
	}

	/** A Glass Accord is crafted from four glass, two amethyst shards, two gold ingots and a spyglass. */
	@GameTest
	public void theAccordIsCraftedFromGlassGoldAmethystAndASpyglass(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ItemStack glass = new ItemStack(Items.GLASS);
		ItemStack amethyst = new ItemStack(Items.AMETHYST_SHARD);
		ItemStack gold = new ItemStack(Items.GOLD_INGOT);
		CraftingInput grid = CraftingInput.of(3, 3, List.of(glass, amethyst, glass.copy(), gold, new ItemStack(Items.SPYGLASS), gold.copy(),
				glass.copy(), amethyst.copy(), glass.copy()));
		ItemStack made = level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, level)
				.orElseThrow(() -> helper.assertionException("Glass, gold, amethyst and a spyglass craft nothing")).value().assemble(grid);
		helper.assertTrue(made.is(JugcraftLairs.GLASS_ACCORD) && made.getCount() == 1, "Glass, gold, amethyst and a spyglass made " + made);
		helper.succeed();
	}

	/**
	 * A Chapel Mirror stood lower and upper turns round its four stops, one at a use of either half, back to rest, both
	 * halves alike; a real use with an empty hand turns it. Marked, its frame gives light; veiled and unveiled, set back to
	 * rest. Air does not turn. Like the other lair-only blocks the mirrors, the lectern and the glass cannot be broken and
	 * have no item; only the mirrors are a fixture a player may use. The mirrors stand where their cells say, and the arrival
	 * is in the narthex.
	 */
	@GameTest
	public void theMirrorsTurnRoundTheirStops(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos lower = new BlockPos(1, 2, 1);
		BlockState rest = JugcraftLairs.CHAPEL_MIRROR.defaultBlockState().setValue(ChapelMirrorBlock.MIRROR, Mirror.EAST);
		helper.setBlock(lower, rest.setValue(ChapelMirrorBlock.HALF, DoubleBlockHalf.LOWER));
		helper.setBlock(lower.above(), rest.setValue(ChapelMirrorBlock.HALF, DoubleBlockHalf.UPPER));
		BlockPos base = helper.absolutePos(lower);
		helper.assertTrue(ChapelMirrorBlock.stop(level, base) == Mirror.REST, "A placed mirror is not at rest");
		for (int turn = 1; turn <= Mirror.STOPS; turn++) {
			BlockPos used = turn % 2 == 0 ? base : base.above();
			int stop = ChapelMirrorBlock.turn(level, used, null);
			helper.assertTrue(stop == turn % Mirror.STOPS && ChapelMirrorBlock.stop(level, base) == stop
					&& level.getBlockState(base.above()).getValue(ChapelMirrorBlock.STOP) == stop,
					"Turn " + turn + " left the mirror at " + stop + " (upper " + level.getBlockState(base.above()).getValue(ChapelMirrorBlock.STOP) + ")");
		}
		ServerPlayer player = player(helper, new BlockPos(3, 2, 1), helper.absolutePos(lower).getY());
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(base.above()), Direction.EAST, base.above(), false);
		player.gameMode.useItemOn(player, level, ItemStack.EMPTY, InteractionHand.MAIN_HAND, hit);
		helper.assertTrue(ChapelMirrorBlock.stop(level, base) == 1, "A use with an empty hand did not turn the mirror: " + ChapelMirrorBlock.stop(level, base));
		helper.assertTrue(level.getBlockState(base).getLightEmission() == 0, "An unmarked mirror gives light");
		helper.assertTrue(ChapelMirrorBlock.setMarked(level, base, true) && !ChapelMirrorBlock.setMarked(level, base, true),
				"The mirror did not mark, or marked again");
		helper.assertTrue(level.getBlockState(base).getLightEmission() == ChapelMirrorBlock.MARKED_LIGHT
				&& level.getBlockState(base.above()).getValue(ChapelMirrorBlock.MARKED), "A marked mirror's frame does not burn");
		helper.assertTrue(ChapelMirrorBlock.setVeiled(level, base, true) && level.getBlockState(base.above()).getValue(ChapelMirrorBlock.VEILED),
				"The mirror did not veil");
		BlockPos origin = base.subtract(Mirror.EAST.cell());
		ChapelMirrorBlock.reset(level, origin);
		BlockState after = level.getBlockState(base);
		helper.assertTrue(after.getValue(ChapelMirrorBlock.STOP) == Mirror.REST && !after.getValue(ChapelMirrorBlock.MARKED)
				&& !after.getValue(ChapelMirrorBlock.VEILED), "The mirror was not set back to rest: " + after);
		helper.assertTrue(GlassChapel.stops(level, origin).get(Mirror.EAST) == Mirror.REST, "The east mirror's stop is not read from its place");
		helper.assertTrue(ChapelMirrorBlock.turn(level, base.east(), null) == -1, "Air turned");

		TagKey<Block> fixtures = TagKey.create(Registries.BLOCK, Jugcraft.id("lair_fixtures"));
		for (Block block : List.of(JugcraftLairs.CHAPEL_MIRROR, JugcraftLairs.ABBOT_LECTERN, JugcraftLairs.LANCET_GLASS, JugcraftLairs.ROSE_GLASS)) {
			helper.assertTrue(block.defaultBlockState().getDestroySpeed(level, base) < 0.0F, block + " can be broken");
			helper.assertTrue(block.asItem() == Items.AIR, block + " has an item");
			helper.assertTrue(block.defaultBlockState().is(fixtures) == (block == JugcraftLairs.CHAPEL_MIRROR),
					block + (block == JugcraftLairs.CHAPEL_MIRROR ? " is not" : " is") + " a lair fixture");
		}
		helper.assertTrue(GlassChapel.inNarthex(CHAPEL.arrival.x, CHAPEL.arrival.z) && !GlassChapel.inNave(CHAPEL.arrival.x, CHAPEL.arrival.z),
				"The arrival is not in the narthex");
		for (Mirror mirror : Mirror.values()) {
			helper.assertTrue(GlassChapel.inNave(mirror.middleX(), mirror.middleZ()), "The " + mirror.getSerializedName() + " mirror is not in the nave");
		}
		helper.assertTrue(GlassChapel.inApse(GlassChapel.ABBOT_X, GlassChapel.ABBOT_Z) && GlassChapel.inArena(GlassChapel.ABBOT_X, GlassChapel.ABBOT_Z)
				&& !GlassChapel.inNave(GlassChapel.ABBOT_X, GlassChapel.ABBOT_Z), "The Abbot's place is not in the apse");
		helper.assertTrue(!GlassChapel.inApse(GlassChapel.APSE_WEST + 0.5, GlassChapel.APSE_NORTH + 0.5)
				&& GlassChapel.inApse(GlassChapel.APSE_WEST + 0.5, GlassChapel.APSE_SOUTH + 0.5), "The apse's back corners are not cut off");
		helper.succeed();
	}

	/**
	 * The beam, worked out by the Java as tools/glass_chapel.py works it out: for each pair of mirrors, each mirror's stop
	 * for that pair is the one tools/glass_chapel.py names; with both at it the beam aimed at either strikes both and
	 * comes home, along the Abbot's place, the two mirrors and back; and of all 64 settings of the three mirrors only those
	 * do. With the mirrors at rest, no pair's beam comes home. An unmarked mirror in its way stops it, and so does a mirror
	 * it has struck before (a mirror square to it sends it straight back).
	 */
	@GameTest
	public void theBeamComesHomeOnlyAtEachPairsStops(GameTestHelper helper) {
		// Each mirror's stop for each pair (tools/glass_chapel.py pair_steps, as indexes into STOPS).
		Map<Mirror, Map<Mirror, Integer>> expected = new EnumMap<>(Mirror.class);
		expected.put(Mirror.EAST, Map.of(Mirror.SOUTH, 1, Mirror.WEST, 2));
		expected.put(Mirror.SOUTH, Map.of(Mirror.EAST, 3, Mirror.WEST, 2));
		expected.put(Mirror.WEST, Map.of(Mirror.EAST, 1, Mirror.SOUTH, 2));
		for (Mirror mirror : Mirror.values()) {
			for (Map.Entry<Mirror, Integer> pair : expected.get(mirror).entrySet()) {
				helper.assertTrue(mirror.stopFor(pair.getKey()) == pair.getValue(), "The " + mirror.getSerializedName() + " mirror's stop for "
						+ pair.getKey().getSerializedName() + " is " + mirror.stopFor(pair.getKey()) + ", not " + pair.getValue());
			}
		}
		Mirror[] all = Mirror.values();
		for (int i = 0; i < all.length; i++) {
			for (int j = i + 1; j < all.length; j++) {
				Mirror a = all[i];
				Mirror b = all[j];
				int homes = 0;
				for (int code = 0; code < 64; code++) {
					Map<Mirror, Integer> stops = new EnumMap<>(Mirror.class);
					stops.put(Mirror.EAST, code % 4);
					stops.put(Mirror.SOUTH, code / 4 % 4);
					stops.put(Mirror.WEST, code / 16);
					boolean want = stops.get(a) == a.stopFor(b) && stops.get(b) == b.stopFor(a);
					boolean home = GlassChapel.comesHome(stops, a, b);
					helper.assertTrue(home == want, "The beam for " + a + "+" + b + (want ? " did not come" : " came") + " home at " + stops);
					homes += home ? 1 : 0;
				}
				helper.assertTrue(homes == 4, a + "+" + b + " came home in " + homes + " settings, not the four the third mirror takes");
				Map<Mirror, Integer> set = new EnumMap<>(Mirror.class);
				set.put(a, a.stopFor(b));
				set.put(b, b.stopFor(a));
				GlassChapel.Beam beam = GlassChapel.trace(set, List.of(a, b), a);
				helper.assertTrue(beam.home() && beam.struck().equals(List.of(a, b)) && beam.points().size() == 4,
						"The beam for " + a + "+" + b + " did not run the Abbot, " + a + ", " + b + " and home: " + beam.struck());
				GlassChapel.Beam dark = GlassChapel.trace(set, List.of(a), a);
				helper.assertTrue(!dark.home() && dark.struck().equals(List.of(a, b)), "An unmarked " + b + " did not stop the beam: " + dark.struck());
				Map<Mirror, Integer> rest = new EnumMap<>(Mirror.class);
				for (Mirror mirror : all) {
					rest.put(mirror, Mirror.REST);
				}
				helper.assertTrue(!GlassChapel.comesHome(rest, a, b), "At rest the beam for " + a + "+" + b + " came home");
			}
		}
		helper.succeed();
	}
}
