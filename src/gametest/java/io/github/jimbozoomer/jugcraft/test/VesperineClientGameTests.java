package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.lair.JugcraftLairs;
import io.github.jimbozoomer.jugcraft.lair.Lair;
import io.github.jimbozoomer.jugcraft.lair.LairBrazierBlock;
import io.github.jimbozoomer.jugcraft.lair.LairInstance;
import io.github.jimbozoomer.jugcraft.lair.LairMoonBlock;
import io.github.jimbozoomer.jugcraft.lair.Lairs;
import io.github.jimbozoomer.jugcraft.lair.vesperine.HollowAcre;
import io.github.jimbozoomer.jugcraft.lair.vesperine.JugcraftVesperine;
import io.github.jimbozoomer.jugcraft.lair.vesperine.VesperineEntity;
import io.github.jimbozoomer.jugcraft.lair.vesperine.VesperineLoot;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Client game test for Vesperine, the Last Reaper (docs/features/vesperine.md), in a real Hollow Acre: unlike a
 * game-test server's world, it has the lair dimension. Her fight from end to end, with the one player in survival
 * (shielded by Resistance, so it can watch every attack): she waits on her throne, stepping into the Mown Circle wakes
 * her, the Last Toll turns the moon red, Death's Harvest puts out the wards and lighting them all ends it, and when she
 * falls the player has their loot and The Last Harvest, the moon is pale and Grey Mist stands in the circle. Along the
 * way, pictures of each part of the fight, the world frozen for each so the pose holds. CI job {@code client}.
 */
public class VesperineClientGameTests implements FabricClientGameTest {
	private static final Lair ACRE = Lair.HOLLOW_ACRE;

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder()
				.adjustSettings(creator -> creator.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()) {
			singleplayer.getConnection().waitForChunksRender();
			context.runOnClient(client -> {
				if (!client.gui.hud.isHidden()) {
					client.gui.hud.toggle();
				}
			});
			TestServerContext server = singleplayer.getServer();
			server.runCommand("gamerule minecraft:send_command_feedback false");
			server.runCommand("difficulty normal");  // she is a monster: in peaceful she would be gone
			server.runCommand("time set midnight");

			// An instance opens: the island is placed and she is seated on its throne, her skulls beside her.
			server.runOnServer(minecraft -> {
				LairInstance instance = Lairs.open(minecraft, ACRE);
				check(instance != null, "No Hollow Acre opened");
				check(Lairs.enter(player(minecraft), instance), "The player did not go in");
			});
			context.waitTicks(40);
			singleplayer.getConnection().waitForChunksRender();
			BlockPos o = ACRE.origin(0);
			String acre = ACRE.dimension.identifier().toString();
			Vec3 centre = new Vec3(o.getX() + HollowAcre.ARENA_X, o.getY() + HollowAcre.FLOOR, o.getZ() + HollowAcre.ARENA_Z);
			server.runOnServer(minecraft -> {
				VesperineEntity vesperine = vesperine(minecraft);
				BlockPos throne = o.offset(HollowAcre.THRONE);
				check(vesperine.phase() == VesperineEntity.Phase.SEATED, "She is not seated but " + vesperine.phase());
				check(vesperine.position().distanceTo(new Vec3(throne.getX() + 0.5, throne.getY() + 0.3, throne.getZ() + 0.5)) < 0.5,
						"She is not on her throne but at " + vesperine.position());
				check(vesperine.skullsAlive(lair(minecraft)) == 2, "Dirge and Requiem are not with her");
			});
			shoot(context, singleplayer, acre, o.getX() + 31.5, o.getY() + HollowAcre.FLOOR + 2.0, o.getZ() + 19.5, 180, 8, false,
					"jugcraft_vesperine_seated");

			// Stepping into the Mown Circle wakes her: she rises with her bar showing.
			server.runCommand("gamemode survival @a");
			server.runCommand("effect give @a minecraft:resistance infinite 4 true");
			server.runCommand("effect give @a minecraft:saturation infinite 0 true");
			server.runCommand(String.format(Locale.ROOT, "execute in %s run tp @a %.1f %.1f %.1f 180 0", acre, centre.x, centre.y + 1, centre.z + 6));
			context.waitTicks(20);
			server.runOnServer(minecraft -> check(vesperine(minecraft).phase() != VesperineEntity.Phase.SEATED,
					"Stepping into the circle did not wake her"));
			context.waitTicks(VesperineEntity.RISE_TICKS + 5);

			// Her attacks, each caught as it lands.
			pose(context, server, centre, (vesperine, level, player) -> { }, 2);
			shot(context, singleplayer, acre, centre, 0.0, 7.0, 180, 8, "jugcraft_vesperine_risen");
			pose(context, server, centre, (vesperine, level, player) -> vesperine.begin(level, VesperineEntity.Attack.REAPING_ARC, player), 10);
			shot(context, singleplayer, acre, centre, -4.5, 5.5, 140, 8, "jugcraft_vesperine_reaping_arc");
			pose(context, server, centre, (vesperine, level, player) -> vesperine.begin(level, VesperineEntity.Attack.SCYTHE_THROW, player), 22);
			shot(context, singleplayer, acre, centre, 7.0, 3.0, 110, 12, "jugcraft_vesperine_scythe_throw");
			pose(context, server, centre, (vesperine, level, player) -> vesperine.begin(level, VesperineEntity.Attack.GRAVE_CALL, player),
					VesperineEntity.Attack.GRAVE_CALL.windup + 12);
			shot(context, singleplayer, acre, centre, 3.0, 9.0, 165, 14, "jugcraft_vesperine_grave_call");

			// The Last Toll: she rises, the moon turns red, the skulls re-form.
			pose(context, server, centre, (vesperine, level, player) -> vesperine.startToll(level), 40);
			server.runCommand("effect clear @a minecraft:darkness");
			server.runOnServer(minecraft -> {
				BlockState moon = lair(minecraft).getBlockState(o.offset(ACRE.moon));
				check(moon.is(JugcraftLairs.LAIR_MOON) && moon.getValue(LairMoonBlock.RED), "The moon did not turn red");
				check(vesperine(minecraft).skullsAlive(lair(minecraft)) == 2, "The skulls did not re-form");
			});
			shot(context, singleplayer, acre, centre, 0.0, 11.0, 180, -22, "jugcraft_vesperine_last_toll");
			server.runCommand("tick unfreeze");
			context.waitTicks(VesperineEntity.TOLL_TICKS);
			pose(context, server, centre, (vesperine, level, player) -> vesperine.begin(level, VesperineEntity.Attack.TWIN_BEAM, player),
					VesperineEntity.Attack.TWIN_BEAM.windup + 12);
			shot(context, singleplayer, acre, centre, -6.0, 8.0, 150, 16, "jugcraft_vesperine_twin_beam");

			// Death's Harvest: the wards go out and souls stream from the black wheat; lighting the four wards ends it.
			pose(context, server, centre, (vesperine, level, player) -> {
				vesperine.setHealth(vesperine.getMaxHealth() * 0.2F);
				vesperine.startHarvest(level);
			}, 50);
			server.runCommand("effect clear @a minecraft:darkness");
			shot(context, singleplayer, acre, centre, 0.0, 14.0, 180, -30, "jugcraft_vesperine_deaths_harvest");
			server.runCommand("tick unfreeze");
			server.runOnServer(minecraft -> {
				ServerLevel level = lair(minecraft);
				VesperineEntity vesperine = vesperine(minecraft);
				List<BlockPos> wards = vesperine.wards(level);
				check(wards.size() == 4 && wards.stream().noneMatch(ward -> VesperineEntity.lit(level, ward)), "The wards did not go out");
				for (BlockPos ward : wards) {
					level.setBlock(ward, level.getBlockState(ward).setValue(LairBrazierBlock.LIT, true), Block.UPDATE_ALL);
				}
			});
			context.waitTicks(5);
			server.runOnServer(minecraft -> check(vesperine(minecraft).phase() == VesperineEntity.Phase.MOON,
					"Lighting the wards did not end the harvest"));

			// She falls: the player's loot and advancement, a pale moon, and Grey Mist in the circle.
			server.runOnServer(minecraft -> {
				ServerLevel level = lair(minecraft);
				VesperineEntity vesperine = vesperine(minecraft);
				check(VesperineLoot.firstKill(player(minecraft)), "The player had already won The Last Harvest");
				vesperine.took(player(minecraft), 10.0F);
				vesperine.hurtServer(level, level.damageSources().genericKill(), Float.MAX_VALUE);
			});
			context.waitTicks(10);
			server.runOnServer(minecraft -> {
				ServerPlayer player = player(minecraft);
				ServerLevel level = lair(minecraft);
				check(player.getInventory().countItem(JugcraftVesperine.REAPER_SHADE) >= 3, "No Reaper's Shade");
				check(player.getInventory().countItem(BuiltInRegistries.ITEM.getValue(Jugcraft.id("vesper_scythe"))) == 1,
						"A first kill must bring the Vesper Scythe");
				check(!VesperineLoot.firstKill(player), "The Last Harvest was not awarded");
				check(!level.getBlockState(o.offset(ACRE.moon)).getValue(LairMoonBlock.RED), "The moon is still red");
				check(level.getBlockState(BlockPos.containing(centre)).is(JugcraftLairs.LAIR_EXIT), "No Grey Mist in the circle");
				check(level.getEntitiesOfClass(VesperineEntity.class, island()).stream().noneMatch(VesperineEntity::isAlive),
						"She is still there");
			});
			shoot(context, singleplayer, acre, centre.x + 0.5, centre.y + 2.5, centre.z + 7.0, 180, 10, false, "jugcraft_vesperine_defeated");
			System.out.println("[vesperine] client game test: her fight passed end to end");
			server.runOnServer(minecraft -> Lairs.reset());
		}
	}

	@FunctionalInterface
	private interface Posing {
		void pose(VesperineEntity vesperine, ServerLevel level, ServerPlayer player);
	}

	/**
	 * Unfreezes the world, puts her in the middle of the circle facing south and the player five blocks in front of her
	 * (so her attacks land in view), does what {@code posing} asks, waits {@code ticks} for the pose and freezes the
	 * world on it.
	 */
	private static void pose(ClientGameTestContext context, TestServerContext server, Vec3 centre, Posing posing, int ticks) {
		server.runCommand("tick unfreeze");
		server.runOnServer(minecraft -> {
			ServerLevel level = lair(minecraft);
			VesperineEntity vesperine = vesperine(minecraft);
			player(minecraft).teleportTo(centre.x + 0.5, centre.y, centre.z + 5.0);
			vesperine.teleportTo(centre.x, centre.y + VesperineEntity.HOVER, centre.z);
			vesperine.setYRot(0.0F);
			vesperine.setYBodyRot(0.0F);
			vesperine.setYHeadRot(0.0F);
			posing.pose(vesperine, level, player(minecraft));
		});
		context.waitTicks(ticks);
		server.runCommand("tick freeze");
	}

	/** A picture of her from ({@code dx}, {@code dz}) off the circle's middle, 2.5 blocks up, looking along yaw and pitch. */
	private static void shot(ClientGameTestContext context, TestSingleplayerContext singleplayer, String dimension, Vec3 centre, double dx,
			double dz, int yaw, int pitch, String name) {
		shoot(context, singleplayer, dimension, centre.x + 0.5 + dx, centre.y + 2.5, centre.z + dz, yaw, pitch, true, name);
	}

	private static ServerPlayer player(MinecraftServer minecraft) {
		return minecraft.getPlayerList().getPlayers().get(0);
	}

	private static ServerLevel lair(MinecraftServer minecraft) {
		return minecraft.getLevel(ACRE.dimension);
	}

	/** The first slot's island, with room above it. */
	private static AABB island() {
		BlockPos o = ACRE.origin(0);
		return new AABB(o.getX(), o.getY() - 16, o.getZ(), o.getX() + ACRE.width, o.getY() + ACRE.height + 32, o.getZ() + ACRE.length);
	}

	private static VesperineEntity vesperine(MinecraftServer minecraft) {
		List<VesperineEntity> found = lair(minecraft).getEntitiesOfClass(VesperineEntity.class, island());
		check(!found.isEmpty(), "Vesperine is not in her Hollow Acre");
		return found.getFirst();
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}

	/** Stands the camera at (x, y, z) in {@code dimension}, looking along yaw and pitch, waits for the world to draw, and shoots. */
	private static void shoot(ClientGameTestContext context, TestSingleplayerContext singleplayer, String dimension, double x, double y,
			double z, int yaw, int pitch, boolean frozen, String name) {
		TestServerContext server = singleplayer.getServer();
		server.runOnServer(minecraft -> {
			// Flying, so the camera stays where it is put (set on the server, which tells the client).
			ServerPlayer player = player(minecraft);
			player.getAbilities().mayfly = true;
			player.getAbilities().flying = true;
			player.onUpdateAbilities();
		});
		server.runCommand(String.format(Locale.ROOT, "execute in %s run tp @a %.1f %.1f %.1f %d %d", dimension, x, y, z, yaw, pitch));
		context.waitTicks(frozen ? 20 : 30);
		singleplayer.getConnection().waitForChunksRender();
		context.waitTicks(10);
		context.takeScreenshot(name);
	}
}
