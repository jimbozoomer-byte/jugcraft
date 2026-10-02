package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.CookingPotBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.CookingPotRecipe;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.MooncakeItem;
import io.github.jimbozoomer.jugcraft.agriculture.SkyLantern;
import io.github.jimbozoomer.jugcraft.agriculture.SkyLanternItem;
import io.github.jimbozoomer.jugcraft.agriculture.SkyLanterns;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * In-game tests for the sky lantern festival: a lantern let go in its colour and with its wish, rising and drifting with
 * the shared wind, dimming as it burns out, gone above the world or torn by a blow; eight let go together make a festival
 * (Luck, the message and the advancement for those near), seven don't, nor eight spread too far apart, and the same
 * place holds no second festival that day; mooncakes bake four at a time and know a full-moon night.
 *
 * <p>Every lantern a test lets go is gone before it ends. The festival test forgets the server's recent releases first,
 * and does its releases within one tick, so no other test's lanterns count towards its festivals.
 */
public class LanternGameTests {
	private static void floor(GameTestHelper helper) {
		for (int x = 0; x <= 7; x++) {
			for (int z = 0; z <= 7; z++) {
				helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
				// The test's barrier ceiling would stop a lantern: lift it.
				for (int y = 3; y <= 24; y++) {
					if (helper.getBlockState(new BlockPos(x, y, z)).is(Blocks.BARRIER)) {
						helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
					}
				}
			}
		}
	}

	private static ServerPlayer player(GameTestHelper helper, BlockPos standAt, ItemStack held) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		BlockPos absolute = helper.absolutePos(standAt);
		player.setPos(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() + 0.5);
		player.setItemInHand(InteractionHand.MAIN_HAND, held);
		return player;
	}

	private static List<SkyLantern> lanterns(GameTestHelper helper) {
		return helper.getLevel().getEntitiesOfClass(SkyLantern.class, new AABB(helper.absolutePos(new BlockPos(4, 2, 4))).inflate(6.0, 30.0, 6.0));
	}

	/** Used, a dyed, named lantern goes up in its colour carrying its wish (one used); it rises and drifts with the wind. */
	@GameTest(maxTicks = 60)
	public void aLanternIsLetGoAndRises(GameTestHelper helper) {
		floor(helper);
		ItemStack held = new ItemStack(JugcraftAgriculture.item("sky_lantern"), 2);
		held.set(DataComponents.DYED_COLOR, new DyedItemColor(0x3A6AE0));
		held.set(DataComponents.CUSTOM_NAME, Component.literal("A good harvest"));
		ServerPlayer player = player(helper, new BlockPos(4, 2, 4), held);
		player.gameMode.useItem(player, helper.getLevel(), held, InteractionHand.MAIN_HAND);
		helper.assertTrue(player.getMainHandItem().getCount() == 1, "One lantern is used");
		List<SkyLantern> released = lanterns(helper);
		helper.assertTrue(released.size() == 1, "One lantern goes up: " + released.size());
		SkyLantern lantern = released.get(0);
		helper.assertTrue(lantern.colour() == 0x3A6AE0 && lantern.getCustomName() != null && lantern.getCustomName().getString().equals("A good harvest"),
				"It is blue and carries its wish");
		long left = lantern.burnsOut() - helper.getLevel().getGameTime();
		helper.assertTrue(left >= SkyLantern.LIFETIME && left <= SkyLantern.LIFETIME + SkyLantern.LIFETIME_SPREAD, "It burns for two minutes or so: " + left);
		double startY = lantern.getY();
		Vec3 start = lantern.position();
		helper.runAfterDelay(20, () -> {
			Vec3 moved = lantern.position().subtract(start);
			Vec3 wind = SkyLantern.wind(helper.getLevel().getGameTime());
			helper.assertTrue(lantern.getY() - startY > 20 * SkyLantern.RISE * 0.6, "It rises: " + (lantern.getY() - startY));
			helper.assertTrue(moved.x * wind.x + moved.z * wind.z > 0.0, "and drifts with the wind: moved " + moved + ", wind " + wind);
			lantern.discard();
			helper.succeed();
		});
	}

	/** Lanterns let go together drift the same way; one dims over its last seconds; one above the world is gone; a blow tears one. */
	@GameTest(maxTicks = 40)
	public void lanternsDriftTogetherAndBurnOut(GameTestHelper helper) {
		floor(helper);
		ServerLevel level = helper.getLevel();
		ItemStack plain = new ItemStack(JugcraftAgriculture.item("sky_lantern"));
		SkyLantern one = SkyLanternItem.release(level, Vec3.atCenterOf(helper.absolutePos(new BlockPos(2, 3, 2))), plain);
		SkyLantern two = SkyLanternItem.release(level, Vec3.atCenterOf(helper.absolutePos(new BlockPos(6, 3, 6))), plain);
		helper.assertTrue(one.colour() == SkyLantern.DEFAULT_COLOUR && one.getCustomName() == null, "An undyed lantern is warm red, with no wish");
		long out = one.burnsOut();
		helper.assertTrue(one.brightness(out - SkyLantern.FADE_TICKS - 1) == 1.0F && Math.abs(one.brightness(out - SkyLantern.FADE_TICKS / 2) - 0.5F) < 0.01F
				&& one.brightness(out) == 0.0F, "It burns bright, then dims over its last five seconds");
		helper.runAfterDelay(5, () -> {
			Vec3 a = one.getDeltaMovement();
			Vec3 b = two.getDeltaMovement();
			helper.assertTrue(Math.abs(a.x - b.x) < 1.0E-9 && Math.abs(a.z - b.z) < 1.0E-9 && a.y > 0.0 && b.y > 0.0,
					"Both drift the same way, rising: " + a + " and " + b);
			one.setPos(one.getX(), level.getMaxY() + 9.0, one.getZ());
		});
		helper.runAfterDelay(8, () -> {
			helper.assertTrue(one.isRemoved(), "Above the world, it is gone");
			two.hurtServer(level, level.damageSources().generic(), 1.0F);
			helper.assertTrue(two.isRemoved(), "A blow tears it and puts it out");
			helper.succeed();
		});
	}

	/**
	 * Eight lanterns let go within 32 blocks of each other in two minutes make a festival: Luck, the message and the
	 * advancement for players near; seven don't, nor eight with one too far off; the same place holds no second festival.
	 */
	@GameTest(maxTicks = 20)
	public void eightLanternsMakeAFestival(GameTestHelper helper) {
		floor(helper);
		ServerLevel level = helper.getLevel();
		ServerPlayer near = player(helper, new BlockPos(1, 2, 1), ItemStack.EMPTY);
		ItemStack plain = new ItemStack(JugcraftAgriculture.item("sky_lantern"));
		SkyLanterns.forget();
		Vec3 here = Vec3.atCenterOf(helper.absolutePos(new BlockPos(4, 3, 4)));
		for (int i = 0; i < SkyLanterns.FESTIVAL_LANTERNS - 1; i++) {
			SkyLanternItem.release(level, here.add(i % 3 - 1, 0.0, i / 3 - 1), plain);
		}
		helper.assertFalse(near.hasEffect(MobEffects.LUCK), "Seven lanterns make no festival");
		helper.assertFalse(SkyLanterns.released(level, here.add(SkyLanterns.FESTIVAL_RADIUS + 8, 0.0, 0.0)),
				"nor an eighth let go too far off");
		SkyLantern eighth = SkyLanternItem.release(level, here, plain);
		AdvancementHolder advancement = level.getServer().getAdvancements().get(Jugcraft.id("lantern_festival"));
		helper.assertTrue(near.hasEffect(MobEffects.LUCK) && near.getEffect(MobEffects.LUCK).getDuration() > SkyLanterns.LUCK_TICKS - 20,
				"The eighth makes a festival: Luck for those near");
		helper.assertTrue(advancement != null && near.getAdvancements().getOrStartProgress(advancement).isDone(), "and A Sky Full of Wishes");
		near.removeEffect(MobEffects.LUCK);
		boolean again = false;
		for (int i = 0; i < SkyLanterns.FESTIVAL_LANTERNS; i++) {
			again |= SkyLanterns.released(level, here);
		}
		helper.assertTrue(!again && !near.hasEffect(MobEffects.LUCK), "The same place holds no second festival that day");
		for (SkyLantern lantern : lanterns(helper)) {
			lantern.discard();
		}
		helper.assertTrue(eighth.isRemoved(), "Every lantern let go is gone");
		SkyLanterns.forget();
		helper.succeed();
	}

	/** Mooncakes bake four at a time, fill a little, and know a full-moon night (the first of eight) from any other. */
	@GameTest(maxTicks = 20)
	public void mooncakesForAFullMoon(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		int day = 24000;
		helper.assertTrue(MooncakeItem.fullMoonNight(MooncakeItem.NIGHT_START) && MooncakeItem.fullMoonNight(8L * day + 18000),
				"The first night, and the ninth, are full-moon nights");
		helper.assertTrue(!MooncakeItem.fullMoonNight(6000) && !MooncakeItem.fullMoonNight(day + 18000) && !MooncakeItem.fullMoonNight(MooncakeItem.NIGHT_END),
				"Noon, the second night and dawn are not");
		for (String cake : JugcraftAgriculture.MOONCAKES) {
			helper.assertTrue(level.recipeAccess().byKey(ResourceKey.create(Registries.RECIPE, Jugcraft.id("pot_cooking/" + cake))).isPresent(),
					"The " + cake + " recipe loads");
		}
		List<ItemStack> slots = new ArrayList<>(List.of(new ItemStack(Items.WHEAT, 2), new ItemStack(Items.SUGAR), new ItemStack(Items.EGG),
				new ItemStack(JugcraftAgriculture.item("roasted_chestnuts"), 2)));
		while (slots.size() < CookingPotBlockEntity.INPUTS) {
			slots.add(ItemStack.EMPTY);
		}
		Optional<CookingPotRecipe.Match> baked = CookingPotRecipe.find(level.getServer(), slots);
		ItemStack made = baked.isPresent() ? baked.get().recipe().output().create() : ItemStack.EMPTY;
		helper.assertTrue(made.is(JugcraftAgriculture.item("chestnut_mooncake")) && made.getCount() == 4,
				"Wheat, sugar, an egg and roasted chestnuts bake four chestnut mooncakes: " + made);
		ServerPlayer player = player(helper, new BlockPos(1, 2, 1), ItemStack.EMPTY);
		player.getFoodData().setFoodLevel(10);
		ItemStack mooncake = new ItemStack(JugcraftAgriculture.item("red_bean_mooncake"), 2);
		mooncake.finishUsingItem(level, player);
		helper.assertTrue(player.getFoodData().getFoodLevel() == 13 && mooncake.getCount() == 1, "A mooncake is three food: " + player.getFoodData().getFoodLevel());
		helper.succeed();
	}
}
