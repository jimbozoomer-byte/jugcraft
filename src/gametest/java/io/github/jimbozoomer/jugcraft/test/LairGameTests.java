package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import io.github.jimbozoomer.jugcraft.lair.GraveGoods;
import io.github.jimbozoomer.jugcraft.lair.JugcraftLairs;
import io.github.jimbozoomer.jugcraft.lair.Lair;
import io.github.jimbozoomer.jugcraft.lair.LairInstance;
import io.github.jimbozoomer.jugcraft.lair.LairRules;
import io.github.jimbozoomer.jugcraft.lair.Lairs;
import io.github.jimbozoomer.jugcraft.lair.LastRites;
import io.github.jimbozoomer.jugcraft.lair.MistGateEntity;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The lairs and the Hollow Acre (docs/features/hollow-acre.md). The instances are shared by the whole server, so
 * everything that opens one runs in one test, step by step ({@link #lairsEndToEnd}); the dimension, the template and the
 * Last Rites' checks stand alone.
 */
public class LairGameTests {
	private static final Lair ACRE = Lair.HOLLOW_ACRE;

	private static ServerLevel acre(GameTestHelper helper) {
		ServerLevel level = helper.getLevel().getServer().getLevel(ACRE.dimension);
		if (level == null) {
			throw helper.assertionException("No dimension " + ACRE.dimension.identifier());
		}
		return level;
	}

	private static ServerPlayer player(GameTestHelper helper, BlockPos standAt, float yaw, float pitch) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		BlockPos absolute = helper.absolutePos(standAt);
		player.snapTo(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() + 0.5, yaw, pitch);
		return player;
	}

	private static Block block(String id) {
		return BuiltInRegistries.BLOCK.getValue(Jugcraft.id(id));
	}

	/** The Hollow Acre's dimension loads from its data pack files: a void with a fixed time and no ceiling, from y 0. */
	@GameTest
	public void hollowAcreDimensionLoads(GameTestHelper helper) {
		ServerLevel level = acre(helper);
		helper.assertTrue(Lairs.isLair(level) && !Lairs.isLair(helper.getLevel()), "Only the Hollow Acre's dimension is a lair");
		helper.assertTrue(level.dimensionType().hasFixedTime() && !level.dimensionType().hasCeiling() && level.dimensionType().minY() == 0,
				"The Hollow Acre's dimension type: fixed time, no ceiling, from y 0");
		helper.succeed();
	}

	/**
	 * The template loads in the running game with its blocks (DataVersion the game's), at the size tools/hollow_acre.py
	 * gives Lair.HOLLOW_ACRE.
	 */
	@GameTest
	public void hollowAcreTemplateLoads(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		Optional<StructureTemplate> template = level.getStructureManager().get(ACRE.template);
		helper.assertTrue(template.isPresent(), "No template " + ACRE.template);
		helper.assertTrue(template.get().getSize().equals(new Vec3i(ACRE.width, ACRE.height, ACRE.length)),
				"The template is " + template.get().getSize() + ", not the Lair's size");
		Identifier path = Identifier.fromNamespaceAndPath(ACRE.template.getNamespace(), "structure/" + ACRE.template.getPath() + ".nbt");
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
		for (String wanted : List.of("jugcraft:lych_gate", "jugcraft:lair_exit", "jugcraft:bone_throne", "jugcraft:blighted_soil",
				"jugcraft:lair_brazier", "jugcraft:black_wheat", "jugcraft:gothic_headstone")) {
			helper.assertTrue(names.contains(wanted), "The template has no " + wanted + ": " + names);
		}
		helper.succeed();
	}

	/**
	 * The Last Rites' checks, in order: a grave within reach, night, enough lit candles (a block of candles counting each
	 * of them), and a wreath on the grave. Each refusal names what is missing and changes nothing.
	 */
	@GameTest
	public void theLastRitesNeedEveryStep(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos grave = new BlockPos(2, 1, 2);
		helper.setBlock(grave, block("gothic_headstone").defaultBlockState());
		BlockPos at = helper.absolutePos(new BlockPos(2, 1, 0));
		helper.assertTrue(LastRites.check(level, at.offset(20, 0, 0), true).missing() == LastRites.Missing.NO_GRAVE,
				"No grave within reach");
		helper.assertTrue(LastRites.check(level, at, false).missing() == LastRites.Missing.NOT_NIGHT, "Not by day");
		BlockState lit = Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.LIT, true);
		helper.setBlock(new BlockPos(0, 1, 2), lit);
		helper.setBlock(new BlockPos(4, 1, 2), lit);
		helper.setBlock(new BlockPos(2, 1, 4), Blocks.CANDLE.defaultBlockState());  // unlit: it does not count
		LastRites.Ring ring = LastRites.check(level, at, true);
		helper.assertTrue(ring.missing() == LastRites.Missing.CANDLES && ring.candles() == 2, "Two lit candles are too few: " + ring);
		helper.setBlock(new BlockPos(2, 1, 4), lit.setValue(CandleBlock.CANDLES, 2));
		ring = LastRites.check(level, at, true);
		helper.assertTrue(ring.missing() == LastRites.Missing.NO_WREATH && ring.candles() == 4, "Four candles, but no wreath: " + ring);
		helper.setBlock(new BlockPos(2, 1, 3), JugcraftLairs.MOURNING_WREATH.defaultBlockState());
		ring = LastRites.check(level, at, true);
		helper.assertTrue(ring.missing() == null && helper.absolutePos(grave).equals(ring.grave()), "Every step done: " + ring);
		helper.assertBlockPresent(JugcraftLairs.MOURNING_WREATH, new BlockPos(2, 1, 3));
		helper.succeed();
	}

	/**
	 * A lair from end to end, step by step (the instances are the whole server's, so one test owns them):
	 * <ol>
	 * <li>the Death Knell rung at a prepared grave opens an instance, places the Hollow Acre fresh (its throne, exit,
	 * braziers and moon where the template puts them), takes the wreath, opens a gate over the grave and takes the ringer
	 * in, unable to build;</li>
	 * <li>a second player follows through the gate; with the party full a third is refused;</li>
	 * <li>building and using blocks are refused, the lair's exit is not;</li>
	 * <li>straying past the edge throws a player back for 4 damage, never below half a heart;</li>
	 * <li>dying there gathers what was dropped as Grave Goods, handed back on respawning;</li>
	 * <li>leaving takes a player back to exactly where they stood, facing the same way, able to build again;</li>
	 * <li>an instance left empty closes after 30 seconds; a restart closes them all and sends anyone inside home;</li>
	 * <li>no more instances open than lairs.instances allows.</li>
	 * </ol>
	 */
	@GameTest(maxTicks = 400)
	public void lairsEndToEnd(GameTestHelper helper) {
		MinecraftServer server = helper.getLevel().getServer();
		ServerLevel overworld = helper.getLevel();
		ServerLevel lair = acre(helper);
		String partySize = JugcraftConfig.textOption("lairs.party_size");
		String instances = JugcraftConfig.textOption("lairs.instances");
		List<ServerPlayer> players = new ArrayList<>();
		try {
			// 1. The rites, rung: an instance opens and the ringer goes in.
			BlockPos grave = new BlockPos(3, 1, 3);
			helper.setBlock(grave, block("gothic_headstone").defaultBlockState());
			BlockState lit = Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.LIT, true).setValue(CandleBlock.CANDLES, 2);
			helper.setBlock(new BlockPos(1, 1, 3), lit);
			helper.setBlock(new BlockPos(5, 1, 3), lit);
			helper.setBlock(new BlockPos(3, 1, 4), JugcraftLairs.MOURNING_WREATH.defaultBlockState());
			ServerPlayer ringer = player(helper, new BlockPos(3, 1, 1), 37.5F, 12.0F);
			players.add(ringer);
			Vec3 stood = ringer.position();
			LastRites.Ring ring = LastRites.ring(overworld, ringer, true);
			LairInstance instance = ring.instance();
			helper.assertTrue(ring.missing() == null && instance != null, "The rites opened no instance: " + ring);
			helper.assertBlockNotPresent(JugcraftLairs.MOURNING_WREATH, new BlockPos(3, 1, 4));
			BlockPos origin = instance.origin();
			helper.assertTrue(lair.getBlockState(origin.offset(31, 17, 13)).is(block("bone_throne")), "No Bone Throne in the chapel");
			helper.assertTrue(lair.getBlockState(origin.offset(31, 17, 69)).is(JugcraftLairs.LAIR_EXIT)
					&& lair.getBlockState(origin.offset(32, 18, 69)).is(JugcraftLairs.LAIR_EXIT), "No Grey Mist in the lych gate");
			helper.assertTrue(lair.getBlockState(origin.offset(17, 17, 21)).is(JugcraftLairs.LAIR_BRAZIER), "No ward brazier");
			helper.assertTrue(lair.getBlockState(origin.offset(ACRE.moon)).is(JugcraftLairs.LAIR_MOON), "No moon");
			helper.assertTrue(ringer.level() == lair && ringer.position().distanceTo(instance.arrival()) < 0.5,
					"The ringer did not arrive: " + ringer.level().dimension() + " " + ringer.position());
			helper.assertFalse(ringer.getAbilities().mayBuild, "In a lair a player cannot build");
			List<MistGateEntity> gates = overworld.getEntitiesOfClass(MistGateEntity.class, new AABB(helper.absolutePos(grave)).inflate(3));
			helper.assertTrue(gates.size() == 1 && gates.get(0).instance() == instance, "No gate over the grave");

			// 2. A follower through the gate; then, with the party full, nobody else.
			JugcraftConfig.setTextOption("lairs.party_size", "2");
			ServerPlayer follower = player(helper, new BlockPos(3, 1, 6), 0.0F, 0.0F);
			players.add(follower);
			gates.get(0).use(follower, InteractionHand.MAIN_HAND);
			helper.assertTrue(follower.level() == lair && Lairs.instance(follower.getAttached(Lairs.VISIT)) == instance,
					"The follower did not come through the gate");
			ServerPlayer late = player(helper, new BlockPos(4, 1, 6), 0.0F, 0.0F);
			players.add(late);
			gates.get(0).use(late, InteractionHand.MAIN_HAND);
			helper.assertTrue(late.level() == overworld && late.getAttached(Lairs.VISIT) == null, "A third came into a party of two");

			// 3. Nothing can be built or used, but the lair's own exit.
			helper.assertTrue(LairRules.denies(ringer, lair), "The lair lets the ringer change it");
			ringer.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STONE));
			BlockPos soil = origin.offset(30, 16, 60);
			BlockHitResult onSoil = new BlockHitResult(Vec3.atCenterOf(soil).relative(Direction.UP, 0.5), Direction.UP, soil, false);
			helper.assertTrue(LairRules.useBlock(ringer, lair, InteractionHand.MAIN_HAND, onSoil) == InteractionResult.FAIL,
					"A block could be placed in the lair");
			ringer.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
			helper.assertTrue(LairRules.useItem(ringer, lair, InteractionHand.MAIN_HAND) == InteractionResult.FAIL, "A bucket could be emptied");
			ringer.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.ENDER_PEARL));
			helper.assertTrue(LairRules.useItem(ringer, lair, InteractionHand.MAIN_HAND) == InteractionResult.PASS, "Ender pearls work");
			ringer.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
			BlockPos exit = origin.offset(31, 17, 69);
			BlockHitResult onExit = new BlockHitResult(Vec3.atCenterOf(exit), Direction.NORTH, exit, false);
			helper.assertTrue(LairRules.useBlock(ringer, lair, InteractionHand.MAIN_HAND, onExit) == InteractionResult.PASS,
					"The exit cannot be used");

			// 4. The edges.
			Vec3 arrival = instance.arrival();
			follower.teleportTo(arrival.x + ACRE.bounds + 5, arrival.y, arrival.z);
			follower.setHealth(20.0F);
			helper.assertTrue(Lairs.edges(follower, instance), "Flying off the island was not noticed");
			helper.assertTrue(follower.position().distanceTo(arrival) < 0.5 && follower.getHealth() == 20.0F - Lairs.EDGE_DAMAGE,
					"The mist did not throw the follower back for " + Lairs.EDGE_DAMAGE + ": " + follower.getHealth());
			follower.teleportTo(arrival.x, origin.getY() + ACRE.floor - 2.0, arrival.z);
			follower.invulnerableTime = 0;
			follower.setHealth(2.0F);
			helper.assertTrue(Lairs.edges(follower, instance) && follower.getHealth() == 1.0F, "Falling off took more than to half a heart");
			helper.assertFalse(Lairs.edges(follower, instance), "Standing at the arrival point is not off the edge");

			// 5. Grave Goods: what was dropped dying is gathered at once and handed back on respawning.
			ItemEntity dropped = new ItemEntity(lair, follower.getX(), follower.getY() + 1.0, follower.getZ(), new ItemStack(Items.DIAMOND, 3));
			lair.addFreshEntity(dropped);
			ExperienceOrb orb = new ExperienceOrb(lair, follower.getX(), follower.getY(), follower.getZ(), 7);
			lair.addFreshEntity(orb);
			Lairs.died(follower);
			GraveGoods goods = follower.getAttached(Lairs.GRAVE_GOODS);
			helper.assertTrue(goods != null && goods.count() == 3 && goods.experience() == 7 && dropped.isRemoved() && orb.isRemoved(),
					"The drops were not gathered as Grave Goods: " + goods);
			helper.assertTrue(follower.getAttached(Lairs.VISIT) == null, "A death in a lair ends the visit");
			BlockPos outside = helper.absolutePos(new BlockPos(1, 1, 6));
			follower.teleportTo(overworld, outside.getX() + 0.5, outside.getY(), outside.getZ() + 0.5, Set.of(), 0.0F, 0.0F, true);
			int before = follower.totalExperience;
			helper.assertTrue(Lairs.handBack(follower), "No Grave Goods were handed back");
			helper.assertTrue(follower.getInventory().countItem(Items.DIAMOND) == 3 && follower.totalExperience == before + 7
					&& follower.getAttached(Lairs.GRAVE_GOODS) == null, "The Grave Goods did not all come back");

			// 6. Leaving: back to exactly where the ringer stood, facing the same way, able to build.
			helper.assertTrue(Lairs.leave(ringer), "The ringer could not leave");
			helper.assertTrue(ringer.level() == overworld && ringer.position().distanceTo(stood) < 1.0E-6
					&& ringer.getYRot() == 37.5F && ringer.getXRot() == 12.0F, "The ringer came back elsewhere: " + ringer.position()
					+ " " + ringer.getYRot() + " " + ringer.getXRot());
			helper.assertTrue(ringer.getAbilities().mayBuild && ringer.getAttached(Lairs.VISIT) == null, "Out of the lair a player builds again");

			// 7. Empty for 30 seconds, the instance closes; a restart closes them all and sends anyone inside home.
			long now = lair.getGameTime();
			Lairs.check(server, lair, ACRE, now);
			helper.assertTrue(instance.isOpen(), "An instance closed the moment it was empty");
			Lairs.check(server, lair, ACRE, now + Lairs.EMPTY_SECONDS * 20L);
			helper.assertFalse(instance.isOpen(), "An instance empty for " + Lairs.EMPTY_SECONDS + " seconds stayed open");
			LairInstance second = Lairs.open(server, ACRE);
			helper.assertTrue(second != null, "No second instance opened");
			ServerPlayer stranded = player(helper, new BlockPos(1, 1, 1), 0.0F, 0.0F);
			players.add(stranded);
			Vec3 home = stranded.position();
			Lairs.enter(stranded, second);
			Lairs.reset();
			Lairs.check(server, lair, ACRE, lair.getGameTime());
			helper.assertTrue(stranded.level() == overworld && stranded.position().distanceTo(home) < 1.0E-6,
					"A player in a closed instance was not sent home");

			// 8. The cap: no more instances than lairs.instances.
			JugcraftConfig.setTextOption("lairs.instances", "2");
			LairInstance a = Lairs.open(server, ACRE);
			LairInstance b = Lairs.open(server, ACRE);
			helper.assertTrue(a != null && b != null && Lairs.open(server, ACRE) == null, "More instances opened than the cap");
			Lairs.close(server, a);
			helper.assertTrue(Lairs.open(server, ACRE) != null, "A closed instance's slot was not free again");
		} finally {
			JugcraftConfig.setTextOption("lairs.party_size", partySize);
			JugcraftConfig.setTextOption("lairs.instances", instances);
			for (ServerPlayer player : players) {
				if (player.getAttached(Lairs.VISIT) != null) {
					Lairs.leave(player);
				}
			}
			Lairs.reset();
		}
		helper.succeed();
	}
}
