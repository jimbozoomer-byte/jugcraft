package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.CarvedPumpkinBlock;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import io.github.jimbozoomer.jugcraft.lair.ChurchBridge;
import io.github.jimbozoomer.jugcraft.lair.ChurchBridge.Brazier;
import io.github.jimbozoomer.jugcraft.lair.HessiansBridleRite;
import io.github.jimbozoomer.jugcraft.lair.JugcraftLairs;
import io.github.jimbozoomer.jugcraft.lair.LairBrazierBlock;
import io.github.jimbozoomer.jugcraft.lair.Lair;
import io.github.jimbozoomer.jugcraft.lair.Lairs;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The Church Bridge and the Hessian's Bridle (docs/features/church-bridge.md), as far as a game-test server can show them.
 * Its world is built from the flat preset alone, so it has no lair dimension: the hollow's own files load (dimension type,
 * biome and template), the bridle's checks hold (a jack-o'-lantern on a fence or wall post, at night, out of the season,
 * under the open sky), a bridle that can open no instance is not used up, the bridle is crafted from leather, iron and a
 * jack-o'-lantern, and the hollow's parts are where its layout says. Everything that needs the hollow itself (the mist
 * rising, coming to on the road, following, the Grey Mist, the instance closing) runs in a real world, in
 * {@link ChurchBridgeClientGameTests}.
 */
public class ChurchBridgeGameTests {
	private static final Lair HOLLOW = Lair.CHURCH_BRIDGE;
	private static final BlockPos POST = new BlockPos(2, 2, 2);
	private static final BlockPos LANTERN = POST.above();

	private static ServerPlayer player(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		BlockPos absolute = helper.absolutePos(new BlockPos(2, 2, 4));
		player.snapTo(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() + 0.5, 180.0F, 0.0F);
		return player;
	}

	/** The hollow's dimension type and biome load from their data pack files. */
	@GameTest
	public void churchBridgeDimensionFilesLoad(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		helper.assertTrue(level.registryAccess().lookupOrThrow(Registries.DIMENSION_TYPE).containsKey(HOLLOW.dimension.identifier()),
				"No dimension type " + HOLLOW.dimension.identifier());
		helper.assertTrue(level.registryAccess().lookupOrThrow(Registries.BIOME).containsKey(HOLLOW.dimension.identifier()),
				"No biome " + HOLLOW.dimension.identifier());
		helper.succeed();
	}

	/**
	 * The template loads in the running game with the braziers, the Moonwood Leaves, the brook's water, the road, the bridge's
	 * planks, the belfry's bell and the Grey Mist (DataVersion the game's), at the size tools/church_bridge.py gives
	 * Lair.CHURCH_BRIDGE, and with nothing in it that changes by itself once placed: no vanilla leaves, no vines and no
	 * grass blocks.
	 */
	@GameTest
	public void churchBridgeTemplateLoads(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		StructureTemplate template = Lairs.template(level, HOLLOW);
		helper.assertTrue(template != null, "No template " + HOLLOW.template);
		helper.assertTrue(template.getSize().equals(new Vec3i(HOLLOW.width, HOLLOW.height, HOLLOW.length)),
				"The template is " + template.getSize() + ", not the Lair's size");
		Identifier path = Identifier.fromNamespaceAndPath(HOLLOW.template.getNamespace(), "structure/" + HOLLOW.template.getPath() + ".nbt");
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
		for (String wanted : List.of("jugcraft:lair_brazier", "jugcraft:moonwood_leaves", "jugcraft:lair_exit", "minecraft:water",
				"minecraft:dirt_path", "minecraft:spruce_planks", "minecraft:bell", "minecraft:dark_oak_stairs")) {
			helper.assertTrue(names.contains(wanted), "The template has no " + wanted + ": " + names);
		}
		for (String name : names) {
			helper.assertTrue(!(name.startsWith("minecraft:") && name.endsWith("_leaves")) && !name.equals("minecraft:vine")
					&& !name.equals("minecraft:grass_block"), "The template has " + name + ", which would change once placed");
		}
		helper.succeed();
	}

	/**
	 * The bridle's checks, changing nothing: on a jack-o'-lantern on a fence, at night, out of the season, under the open
	 * sky (a barrier, which cannot be seen, roofs nothing) it is answered, and on a lit hand-carved pumpkin on a wall too; on
	 * the lantern standing on stone, on an unlit carved pumpkin, in the season, by day, or under a roof it is refused, and
	 * answered again once the roof is gone. No mist rises where nobody used a bridle.
	 */
	@GameTest
	public void theBridleWantsALanternOnAPostAtNightOutOfSeason(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		helper.setBlock(POST, Blocks.OAK_FENCE);
		helper.setBlock(LANTERN, Blocks.JACK_O_LANTERN);
		BlockPos lantern = helper.absolutePos(LANTERN);
		BlockPos feet = helper.absolutePos(new BlockPos(2, 2, 4));
		BlockPos roof = lantern.above(3);
		try {
			helper.assertTrue(HessiansBridleRite.check(level, lantern, feet, true, false) == null,
					"A lantern on a fence at night out of season was refused: " + HessiansBridleRite.check(level, lantern, feet, true, false));
			helper.assertTrue(HessiansBridleRite.check(level, lantern, feet, true, true) == HessiansBridleRite.Missing.IN_SEASON,
					"In the season the bridle was not refused");
			helper.assertTrue(HessiansBridleRite.check(level, lantern, feet, false, false) == HessiansBridleRite.Missing.NOT_NIGHT,
					"By day the bridle was not refused");
			level.setBlock(roof, Blocks.BARRIER.defaultBlockState(), Block.UPDATE_ALL);
			helper.assertTrue(HessiansBridleRite.check(level, lantern, feet, true, false) == null, "Under a barrier the bridle was refused");
			level.setBlock(roof, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
			helper.assertTrue(HessiansBridleRite.check(level, lantern, feet, true, false) == HessiansBridleRite.Missing.ROOFED,
					"Under a roof the bridle was not refused");
			level.setBlock(roof, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
			helper.assertTrue(HessiansBridleRite.check(level, lantern, feet, true, false) == null, "With the roof gone the bridle was refused");

			helper.setBlock(POST, Blocks.STONE);
			helper.assertTrue(HessiansBridleRite.check(level, lantern, feet, true, false) == HessiansBridleRite.Missing.NOT_LANTERN,
					"A lantern standing on stone was taken for one on a post");
			helper.setBlock(POST, Blocks.COBBLESTONE_WALL);
			helper.setBlock(LANTERN, Blocks.CARVED_PUMPKIN);
			helper.assertTrue(HessiansBridleRite.check(level, lantern, feet, true, false) == HessiansBridleRite.Missing.NOT_LANTERN,
					"An unlit carved pumpkin was taken for a lantern");
			Block carved = JugcraftAgriculture.block("hand_carved_pumpkin");
			helper.setBlock(LANTERN, carved.defaultBlockState().setValue(CarvedPumpkinBlock.LIT, true));
			helper.assertTrue(HessiansBridleRite.check(level, lantern, feet, true, false) == null,
					"A lit hand-carved pumpkin on a wall was refused: " + HessiansBridleRite.check(level, lantern, feet, true, false));
			helper.setBlock(LANTERN, carved.defaultBlockState().setValue(CarvedPumpkinBlock.LIT, false));
			helper.assertTrue(HessiansBridleRite.check(level, lantern, feet, true, false) == HessiansBridleRite.Missing.NOT_LANTERN,
					"An unlit hand-carved pumpkin was taken for a lantern");
		} finally {
			level.setBlock(roof, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
		}
		helper.assertTrue(HessiansBridleRite.mist(level, feet) == null, "A mist rose where nobody used a bridle");
		helper.succeed();
	}

	/**
	 * The bridle used in full when no instance can open (a game-test server has no lair dimension, and the one slot
	 * {@code lairs.instances} allows is taken if it has): the hollow is full, the bridle is kept, nobody moves and no mist
	 * rises. In the season it is refused the same way. Used from the hand on the lantern it is rested after its use,
	 * answered or not; used on stone it only says what it wants.
	 */
	@GameTest
	public void theBridleUsesNothingWhenNoHollowCanOpen(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		helper.setBlock(POST, Blocks.SPRUCE_FENCE);
		helper.setBlock(LANTERN, Blocks.JACK_O_LANTERN);
		BlockPos lantern = helper.absolutePos(LANTERN);
		String instances = JugcraftConfig.textOption("lairs.instances");
		try {
			JugcraftConfig.setTextOption("lairs.instances", "1");
			Lairs.open(level.getServer(), HOLLOW);  // null here; takes the only slot where there is a Church Bridge
			ServerPlayer player = player(helper);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(JugcraftLairs.HESSIANS_BRIDLE, 2));
			Vec3 stood = player.position();
			HessiansBridleRite.Bridling bridling = HessiansBridleRite.bridle(level, player, lantern, player.getMainHandItem(), true, false);
			helper.assertTrue(bridling.missing() == HessiansBridleRite.Missing.FULL && bridling.instance() == null, "The hollow was not full: " + bridling);
			helper.assertTrue(player.getMainHandItem().getCount() == 2, "The bridle was used up: " + player.getMainHandItem());
			helper.assertTrue(player.level() == level && player.position().distanceTo(stood) < 1.0E-6 && player.getAttached(Lairs.VISIT) == null,
					"The player was moved");
			helper.assertTrue(HessiansBridleRite.mist(level, BlockPos.containing(stood)) == null, "A mist rose into no hollow");
			helper.assertTrue(HessiansBridleRite.bridle(level, player, lantern, player.getMainHandItem(), true, true).missing()
					== HessiansBridleRite.Missing.IN_SEASON && player.getMainHandItem().getCount() == 2,
					"In the season the bridle was not refused, or was used up");
			BlockPos stone = helper.absolutePos(new BlockPos(4, 1, 2));
			BlockHitResult onStone = new BlockHitResult(Vec3.atCenterOf(stone), Direction.UP, stone, false);
			JugcraftLairs.HESSIANS_BRIDLE.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, onStone));
			helper.assertTrue(player.getMainHandItem().getCount() == 2 && !player.getCooldowns().isOnCooldown(player.getMainHandItem()),
					"Used on stone, the bridle was used up or rested");
			// Answered or not (the season, the hour and the full hollow aside), a use on the lantern rests the bridle.
			BlockHitResult onLantern = new BlockHitResult(Vec3.atCenterOf(lantern), Direction.SOUTH, lantern, false);
			InteractionResult used = JugcraftLairs.HESSIANS_BRIDLE.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, onLantern));
			helper.assertTrue(used.consumesAction() && player.getMainHandItem().getCount() == 2
					&& player.getCooldowns().isOnCooldown(player.getMainHandItem()), "Used on the lantern, the bridle was used up or not rested: " + used);
		} finally {
			JugcraftConfig.setTextOption("lairs.instances", instances);
			Lairs.reset();
		}
		helper.succeed();
	}

	/** A Hessian's Bridle is crafted from three leather, two iron ingots and a jack-o'-lantern. */
	@GameTest
	public void theBridleIsCraftedFromLeatherIronAndALantern(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ItemStack leather = new ItemStack(Items.LEATHER);
		ItemStack iron = new ItemStack(Items.IRON_INGOT);
		CraftingInput grid = CraftingInput.of(3, 3, List.of(leather, ItemStack.EMPTY, leather.copy(),
				iron, new ItemStack(Items.JACK_O_LANTERN), iron.copy(), ItemStack.EMPTY, leather.copy(), ItemStack.EMPTY));
		ItemStack made = level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, level)
				.orElseThrow(() -> helper.assertionException("Leather, iron and a jack-o'-lantern craft nothing")).value().assemble(grid);
		helper.assertTrue(made.is(JugcraftLairs.HESSIANS_BRIDLE) && made.getCount() == 1, "Leather, iron and a jack-o'-lantern made " + made);
		helper.succeed();
	}

	/**
	 * The hollow's parts are where its layout says: the arrival on the wood road, south of the hollow; the braziers in the
	 * hollow, off the road, each with its neighbour round the ring; the bridge across the brook on the road; and where the
	 * Horseman waits, at the bridge's south foot, in the hollow. A brazier burns or not by its own state.
	 */
	@GameTest
	public void theHollowsPartsAreWhereTheLayoutSays(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		helper.assertTrue(ChurchBridge.onRoad(HOLLOW.arrival.x, HOLLOW.arrival.z) && !ChurchBridge.inHollow(HOLLOW.arrival.x, HOLLOW.arrival.z),
				"The arrival is not on the road, south of the hollow");
		for (Brazier brazier : Brazier.values()) {
			BlockPos at = ChurchBridge.BRAZIERS[brazier.ordinal()];
			helper.assertTrue(ChurchBridge.inHollow(at.getX() + 0.5, at.getZ() + 0.5) && !ChurchBridge.onRoad(at.getX() + 0.5, at.getZ() + 0.5),
					"The " + brazier.title() + " brazier is not in the hollow, off the road");
			helper.assertTrue(brazier.next() != brazier && brazier.next().ordinal() == (brazier.ordinal() + 1) % Brazier.values().length,
					"The " + brazier.title() + " brazier's neighbour is not the next round the ring");
		}
		for (int z = ChurchBridge.BROOK_NORTH; z <= ChurchBridge.BROOK_SOUTH; z++) {
			helper.assertTrue(ChurchBridge.onBridge(ChurchBridge.ROAD_WEST + 1.5, z + 0.5), "The bridge does not cross the brook at row " + z);
		}
		helper.assertTrue(ChurchBridge.inHollow(ChurchBridge.WAIT_X, ChurchBridge.WAIT_Z) && ChurchBridge.onRoad(ChurchBridge.WAIT_X, ChurchBridge.WAIT_Z)
				&& !ChurchBridge.onBridge(ChurchBridge.WAIT_X, ChurchBridge.WAIT_Z), "Where the Horseman waits is not at the bridge's foot");

		BlockPos base = new BlockPos(1, 2, 1);
		BlockPos origin = helper.absolutePos(base).subtract(ChurchBridge.BRAZIERS[Brazier.NORTH_EAST.ordinal()]);
		helper.setBlock(base, JugcraftLairs.LAIR_BRAZIER.defaultBlockState());
		helper.assertTrue(ChurchBridge.lit(level, origin, Brazier.NORTH_EAST), "A lit brazier is not found burning by its place");
		helper.setBlock(base, JugcraftLairs.LAIR_BRAZIER.defaultBlockState().setValue(LairBrazierBlock.LIT, false));
		helper.assertTrue(!ChurchBridge.lit(level, origin, Brazier.NORTH_EAST), "A dark brazier is found burning");
		helper.succeed();
	}
}
