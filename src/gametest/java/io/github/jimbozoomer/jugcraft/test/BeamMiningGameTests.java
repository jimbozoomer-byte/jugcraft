package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import io.github.jimbozoomer.jugcraft.guns.GunItem;
import io.github.jimbozoomer.jugcraft.guns.GunShots;
import io.github.jimbozoomer.jugcraft.guns.GunSpec;
import io.github.jimbozoomer.jugcraft.guns.JugcraftGuns;
import java.util.Map;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * In-game tests for the beams that mine (the beam-mining follow-up, docs/features/guns.md), through the server's entry
 * points as {@link GunsGameTests} does: the Seam Cutter's and the Geode Cutter's beams cut the block they reach, at the
 * owner's mining speeds, and break it as their shooter would, where their shooter may. Shooters face south (+z), the
 * block three ahead.
 */
public class BeamMiningGameTests {
	/** Where each test's block stands, three blocks ahead of a shooter at (x, 2, 1). */
	private static final int AHEAD = 4;

	/**
	 * The beams that mine are the owner's, at the owner's speeds: the Seam Cutter's (the CR4K Mining Laser's) 14 and an
	 * iron pickaxe's harvest, the Geode Cutter's (the Shard Culler's) 1 and a diamond pickaxe's; no other gun's shot cuts.
	 * The Seam Cutter is through stone in two shots and the Geode Cutter through netherrack in four, and a block of no
	 * hardness goes at once. Mining is on unless the server turns it off.
	 */
	@GameTest
	public void miningBeamsAreTheOwners(GameTestHelper helper) {
		helper.assertTrue(JugcraftGuns.MINING.equals(Map.of("seam_cutter", new JugcraftGuns.Mining(14.0F, "iron"),
				"geode_cutter", new JugcraftGuns.Mining(1.0F, "diamond"))), "The mining beams are not the owner's: " + JugcraftGuns.MINING);
		for (String name : JugcraftGuns.MINING.keySet()) {
			helper.assertTrue(JugcraftGuns.shot(JugcraftGuns.GUNS.get(name)).equals(JugcraftGuns.BEAM), name + " does not fire a beam");
		}
		helper.assertTrue(JugcraftGuns.mining(JugcraftGuns.GUNS.get("beam_pistol")) == null
				&& JugcraftGuns.mining(JugcraftGuns.GUNS.get("ermine_rifle")) == null, "A beam that should not mine does");
		JugcraftGuns.Mining seam = JugcraftGuns.mining(JugcraftGuns.GUNS.get("seam_cutter"));
		JugcraftGuns.Mining geode = JugcraftGuns.mining(JugcraftGuns.GUNS.get("geode_cutter"));
		int seamEvery = JugcraftGuns.SPECS.get("seam_cutter").interval();
		int geodeEvery = JugcraftGuns.SPECS.get("geode_cutter").interval();
		helper.assertTrue(seam.cut(1.5F, seamEvery) < 1.0F && 2 * seam.cut(1.5F, seamEvery) >= 1.0F,
				"The Seam Cutter is not through stone in two shots");
		helper.assertTrue(3 * geode.cut(0.4F, geodeEvery) < 1.0F && 4 * geode.cut(0.4F, geodeEvery) >= 1.0F,
				"The Geode Cutter is not through netherrack in four shots");
		helper.assertTrue(geode.cut(0.0F, geodeEvery) >= 1.0F, "A block of no hardness does not go at once");
		helper.assertTrue(!seam.pickaxe().isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState())
				&& geode.pickaxe().isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState())
				&& seam.pickaxe().isCorrectToolForDrops(Blocks.IRON_ORE.defaultBlockState()),
				"The Seam Cutter's harvest is not an iron pickaxe's, or the Geode Cutter's a diamond pickaxe's");
		helper.assertTrue(JugcraftConfig.option("guns.beam_mining"), "Beam mining is off by default");
		helper.succeed();
	}

	/**
	 * The Seam Cutter's first shot at a stone block leaves it standing; its second, its interval later, breaks it, and it
	 * drops cobblestone as stone broken by hand with a pickaxe does. Each shot spends a round.
	 */
	@GameTest(structure = GunsGameTests.ARENA, maxTicks = 40)
	public void seamCutterCutsStone(GameTestHelper helper) {
		GunsGameTests.floor(helper);
		BlockPos stone = new BlockPos(1, 2, AHEAD);
		helper.setBlock(stone, Blocks.STONE);
		GunSpec spec = JugcraftGuns.SPECS.get("seam_cutter");
		ServerPlayer shooter = shooter(helper, "seam_cutter", 1, 10, GameType.SURVIVAL);
		helper.runAfterDelay(1, () -> {
			helper.assertTrue(GunShots.fire(shooter), "The Seam Cutter did not fire");
			helper.assertBlockPresent(Blocks.STONE, stone);
		});
		helper.runAfterDelay(1 + spec.interval(), () -> {
			helper.assertTrue(GunShots.fire(shooter), "The Seam Cutter did not fire its second shot");
			helper.assertBlockPresent(Blocks.AIR, stone);
			helper.assertTrue(dropped(helper, stone, Items.COBBLESTONE), "The stone dropped no cobblestone");
			helper.assertTrue(GunItem.loaded(shooter.getMainHandItem()) == 8, "Two shots did not spend two rounds");
			helper.succeed();
		});
	}

	/**
	 * The Geode Cutter cuts slowly, as the owner's Shard Culler: three shots leave a netherrack block standing, the fourth
	 * breaks it, and it drops itself.
	 */
	@GameTest(structure = GunsGameTests.ARENA, maxTicks = 60)
	public void geodeCutterCutsSlowly(GameTestHelper helper) {
		GunsGameTests.floor(helper);
		BlockPos rock = new BlockPos(1, 2, AHEAD);
		helper.setBlock(rock, Blocks.NETHERRACK);
		GunSpec spec = JugcraftGuns.SPECS.get("geode_cutter");
		ServerPlayer shooter = shooter(helper, "geode_cutter", 1, 10, GameType.SURVIVAL);
		for (int shot = 0; shot < 4; shot++) {
			int n = shot;
			helper.runAfterDelay(1 + spec.interval() * shot, () -> {
				helper.assertTrue(GunShots.fire(shooter), "The Geode Cutter did not fire shot " + (n + 1));
				if (n < 3) {
					helper.assertBlockPresent(Blocks.NETHERRACK, rock);
				}
			});
		}
		helper.runAfterDelay(2 + spec.interval() * 3, () -> {
			helper.assertBlockPresent(Blocks.AIR, rock);
			helper.assertTrue(dropped(helper, rock, Items.NETHERRACK), "The netherrack did not drop itself");
			helper.succeed();
		});
	}

	/**
	 * What a beam must leave whole, each shot at four times over the time stone would take: obsidian before the Seam
	 * Cutter (an iron pickaxe cannot harvest it), a chest (it holds a block entity), and stone before a shooter in
	 * adventure mode, who may not break blocks.
	 */
	@GameTest(structure = GunsGameTests.ARENA, maxTicks = 40)
	public void beamsLeaveWhatTheyMayNotCut(GameTestHelper helper) {
		GunsGameTests.floor(helper);
		BlockPos obsidian = new BlockPos(1, 2, AHEAD);
		BlockPos chest = new BlockPos(5, 2, AHEAD);
		BlockPos stone = new BlockPos(9, 2, AHEAD);
		helper.setBlock(obsidian, Blocks.OBSIDIAN);
		helper.setBlock(chest, Blocks.CHEST);
		helper.setBlock(stone, Blocks.STONE);
		GunSpec spec = JugcraftGuns.SPECS.get("seam_cutter");
		ServerPlayer onObsidian = shooter(helper, "seam_cutter", 1, 10, GameType.SURVIVAL);
		ServerPlayer onChest = shooter(helper, "seam_cutter", 5, 10, GameType.SURVIVAL);
		ServerPlayer adventurer = shooter(helper, "seam_cutter", 9, 10, GameType.ADVENTURE);
		for (int shot = 0; shot < 8; shot++) {
			int n = shot;
			helper.runAfterDelay(1 + spec.interval() * shot, () -> {
				for (ServerPlayer shooter : new ServerPlayer[] {onObsidian, onChest, adventurer}) {
					helper.assertTrue(GunShots.fire(shooter), "A Seam Cutter did not fire shot " + (n + 1));
				}
			});
		}
		helper.runAfterDelay(2 + spec.interval() * 7, () -> {
			helper.assertBlockPresent(Blocks.OBSIDIAN, obsidian);
			helper.assertBlockPresent(Blocks.CHEST, chest);
			helper.assertBlockPresent(Blocks.STONE, stone);
			helper.succeed();
		});
	}

	/**
	 * With the server option {@code guns.beam_mining} off, a Seam Cutter's shot leaves a slime block (no hardness)
	 * standing; with it on again, the next shot breaks it at once.
	 */
	@GameTest(structure = GunsGameTests.ARENA, maxTicks = 30)
	public void beamMiningCanBeTurnedOff(GameTestHelper helper) {
		GunsGameTests.floor(helper);
		BlockPos slime = new BlockPos(1, 2, AHEAD);
		helper.setBlock(slime, Blocks.SLIME_BLOCK);
		GunSpec spec = JugcraftGuns.SPECS.get("seam_cutter");
		ServerPlayer shooter = shooter(helper, "seam_cutter", 1, 10, GameType.SURVIVAL);
		helper.runAfterDelay(1, () -> {
			boolean fired;
			JugcraftConfig.setOption("guns.beam_mining", false);
			try {
				fired = GunShots.fire(shooter);
			} finally {
				JugcraftConfig.setOption("guns.beam_mining", true);
			}
			helper.assertTrue(fired, "The Seam Cutter did not fire");
			helper.assertBlockPresent(Blocks.SLIME_BLOCK, slime);
		});
		helper.runAfterDelay(1 + spec.interval(), () -> {
			helper.assertTrue(GunShots.fire(shooter), "The Seam Cutter did not fire its second shot");
			helper.assertBlockPresent(Blocks.AIR, slime);
			helper.succeed();
		});
	}

	/**
	 * A shooter of this gun, this many rounds loaded, standing at (x, 2, 1) and facing south, with its look on the middle
	 * of the block {@link #AHEAD} at (x, 2, AHEAD).
	 */
	private static ServerPlayer shooter(GameTestHelper helper, String gun, int x, int loaded, GameType mode) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(mode);
		BlockPos at = helper.absolutePos(new BlockPos(x, 2, 1));
		player.setPos(at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
		Vec3 aim = Vec3.atCenterOf(helper.absolutePos(new BlockPos(x, 2, AHEAD))).subtract(player.getEyePosition());
		player.setYRot(0.0F);
		player.setYHeadRot(0.0F);
		player.setXRot((float) Math.toDegrees(Math.atan2(-aim.y, Math.sqrt(aim.x * aim.x + aim.z * aim.z))));
		ItemStack stack = new ItemStack(JugcraftGuns.GUNS.get(gun));
		GunItem.setLoaded(stack, loaded);
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);
		return player;
	}

	/** Whether an item of this kind lies where the block at {@code pos} broke. */
	private static boolean dropped(GameTestHelper helper, BlockPos pos, Item item) {
		AABB around = new AABB(helper.absolutePos(pos)).inflate(1.5);
		return !helper.getLevel().getEntitiesOfClass(ItemEntity.class, around, entity -> entity.getItem().is(item)).isEmpty();
	}
}
