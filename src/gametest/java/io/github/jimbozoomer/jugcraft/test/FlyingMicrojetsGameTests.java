package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.guns.GunShots;
import io.github.jimbozoomer.jugcraft.guns.GunSpec;
import io.github.jimbozoomer.jugcraft.guns.JugcraftGuns;
import io.github.jimbozoomer.jugcraft.guns.Microjet;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * In-game tests for the flying-microjets follow-up (docs/features/guns.md): the Maelstrom Gun's Microjets fly to their
 * mark at the owner's speed instead of striking at once, and the heavy guns slow whoever holds them, at the owner's
 * speedModifier. Shooters face south (+z).
 */
public class FlyingMicrojetsGameTests {
	/** A pig's own speed, given back to the pigs that hold the guns (the gun tests' pigs stand still). */
	private static final double PIG_SPEED = 0.25;

	/**
	 * The guns that fire Microjets that fly are the owner's three built microjet guns, at the owner's speeds: the Maelstrom
	 * Gun's 1.5 blocks a tick, and slice 16B's Comet Pistol's 2.5 and Narwhal Lance's 3. The heavy guns that slow their
	 * holder are the owner's eight, each to the owner's share of their speed, and each gun's item carries it as a
	 * main-hand movement-speed modifier, multiplied in last; a gun without any, the Kreuzer Pistol, has none.
	 */
	@GameTest
	public void flyingMicrojetsAreTheOwners(GameTestHelper helper) {
		Map<String, Float> jets = Map.of("maelstrom_gun", 1.5F, "comet_pistol", 2.5F, "narwhal_lance", 3.0F);
		helper.assertTrue(JugcraftGuns.JET_SPEED.equals(jets), "The guns' Microjets do not fly at the owner's speeds: " + JugcraftGuns.JET_SPEED);
		jets.forEach((name, speed) -> helper.assertTrue(JugcraftGuns.shot(JugcraftGuns.GUNS.get(name)).equals(JugcraftGuns.JET)
				&& JugcraftGuns.jetSpeed(JugcraftGuns.GUNS.get(name)) == speed, "The " + name + " does not fire Microjets that fly " + speed
				+ " blocks a tick"));
		helper.assertTrue(JugcraftGuns.SHOTS.entrySet().stream().filter(entry -> entry.getValue().equals(JugcraftGuns.JET)).count() == 3,
				"Another gun fires Microjets that fly");
		helper.assertTrue(JugcraftGuns.MICROJET != null, "The Microjet in flight is not registered");
		Map<String, Float> owners = Map.ofEntries(Map.entry("thresher", 0.85F), Map.entry("stoker", 0.8F), Map.entry("earthmover", 0.95F),
				Map.entry("borer", 0.8F), Map.entry("cloudburst_gun", 0.8F), Map.entry("harrow", 0.95F), Map.entry("maelstrom_gun", 0.9F),
				Map.entry("geode_cutter", 0.9F));
		helper.assertTrue(JugcraftGuns.CARRY_SPEED.equals(owners), "The guns that slow their holder are not the owner's: " + JugcraftGuns.CARRY_SPEED);
		JugcraftGuns.GUNS.forEach((name, gun) -> {
			var weights = new ItemStack(gun).getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,
					ItemAttributeModifiers.EMPTY).modifiers().stream().filter(entry -> entry.modifier().id().equals(JugcraftGuns.WEIGHT)).toList();
			Float share = owners.get(name);
			if (share == null) {
				helper.assertTrue(weights.isEmpty() && JugcraftGuns.carrySpeed(gun) == 1.0F, "The " + name + " slows its holder");
				return;
			}
			helper.assertTrue(weights.size() == 1 && weights.getFirst().attribute().equals(Attributes.MOVEMENT_SPEED)
					&& Math.abs(weights.getFirst().modifier().amount() - (share - 1.0)) < 1.0E-6
					&& weights.getFirst().modifier().operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
					&& JugcraftGuns.carrySpeed(gun) == share, "The " + name + " does not slow its holder to " + share + ": " + weights);
		});
		helper.succeed();
	}

	/**
	 * A Maelstrom Gun fired at a pig seven blocks off: at once there is one Microjet, the gun's, with the gun's damage
	 * and range, and the pig is unhurt. A tick later it has flown about 1.5 blocks, and still the pig is unhurt; by the
	 * tenth tick it has reached the pig, dealt the gun's damage and gone. A wall behind the pig stops a stray one.
	 */
	@GameTest(structure = GunsGameTests.ARENA, maxTicks = 40)
	public void microjetFliesToItsMark(GameTestHelper helper) {
		GunsGameTests.floor(helper);
		wall(helper, 10);
		Mob pig = NetherGunsGameTests.sturdyPig(helper, new BlockPos(1, 2, 8));
		GunSpec spec = JugcraftGuns.SPECS.get("maelstrom_gun");
		ServerPlayer shooter = GunsGameTests.shooter(helper, "maelstrom_gun", 1, pig, GameType.SURVIVAL);
		helper.assertTrue(GunShots.fire(shooter), "The loaded Maelstrom Gun did not fire");
		List<Microjet> jets = helper.getLevel().getEntitiesOfClass(Microjet.class, new AABB(shooter.blockPosition()).inflate(3.0));
		helper.assertTrue(jets.size() == 1 && pig.getHealth() == 200.0F, jets.size() + " Microjets, not one, and the pig took "
				+ (200.0F - pig.getHealth()) + " at once");
		Microjet jet = jets.getFirst();
		helper.assertTrue(jet.getOwner() == shooter && jet.gun().equals("maelstrom_gun") && jet.damage() == spec.damage()
				&& jet.left() == spec.range(), "The Microjet is not the gun's: " + jet.gun() + ", " + jet.damage() + ", " + jet.left());
		Vec3[] seen = new Vec3[1];
		helper.runAfterDelay(1, () -> seen[0] = jet.position());
		helper.runAfterDelay(2, () -> {
			double step = jet.position().distanceTo(seen[0]);
			helper.assertTrue(!jet.isRemoved() && step > 1.4 && step < 1.55 && jet.getDeltaMovement().z > 1.4,
					"The Microjet flew " + step + " blocks in a tick, not about 1.5, or it is gone");
			helper.assertTrue(pig.getHealth() == 200.0F, "The pig took " + (200.0F - pig.getHealth()) + " before the Microjet reached it");
		});
		helper.runAfterDelay(10, () -> {
			helper.assertTrue(jet.isRemoved(), "The Microjet flew on past the pig");
			helper.assertTrue(Math.abs(200.0F - pig.getHealth() - spec.damage()) < 1.0E-3F,
					"The Microjet took " + (200.0F - pig.getHealth()) + ", not " + spec.damage());
			helper.succeed();
		});
	}

	/**
	 * A wall between a Maelstrom Gun and a pig stops its Microjet: the pig is unhurt and the Microjet gone. Two more set
	 * flying along the open arena, their range 4 and 12 blocks: the first is gone by the sixth tick and the second flies
	 * on, with what it has flown taken off its range.
	 */
	@GameTest(structure = GunsGameTests.ARENA, maxTicks = 40)
	public void microjetStopsAtABlockOrItsRange(GameTestHelper helper) {
		GunsGameTests.floor(helper);
		wall(helper, 5);
		Mob pig = NetherGunsGameTests.sturdyPig(helper, new BlockPos(1, 2, 8));
		ServerPlayer shooter = GunsGameTests.shooter(helper, "maelstrom_gun", 1, pig, GameType.SURVIVAL);
		helper.assertTrue(GunShots.fire(shooter), "The loaded Maelstrom Gun did not fire");
		Microjet shortJet = launch(helper, shooter, new BlockPos(8, 3, 1), 4.0);
		Microjet longJet = launch(helper, shooter, new BlockPos(12, 3, 1), 12.0);
		Vec3 start = longJet.position();
		helper.runAfterDelay(1, () -> helper.assertTrue(!shortJet.isRemoved() && !longJet.isRemoved(),
				"A Microjet stopped before it had flown its range"));
		helper.runAfterDelay(6, () -> {
			helper.assertTrue(pig.getHealth() == 200.0F, "The Microjet went through the wall: the pig took " + (200.0F - pig.getHealth()));
			List<Microjet> left = helper.getLevel().getEntitiesOfClass(Microjet.class, new AABB(helper.absolutePos(new BlockPos(1, 3, 3)))
					.inflate(4.0));
			helper.assertTrue(left.isEmpty(), "The Maelstrom Gun's Microjet did not stop at the wall");
			helper.assertTrue(shortJet.isRemoved(), "The Microjet of 4 blocks' range flew on past it");
			double flown = longJet.position().distanceTo(start);
			helper.assertTrue(!longJet.isRemoved() && flown > 6.0 && Math.abs(longJet.left() - (12.0 - flown)) < 1.0E-6,
					"The Microjet of 12 blocks' range stopped, or has " + longJet.left() + " left after " + flown);
			helper.succeed();
		});
	}

	/**
	 * Three pigs, each given back a pig's speed: one holds a Maelstrom Gun in its main hand, one in its other hand, and
	 * one a Kreuzer Pistol. Once they have ticked, only the first is slowed, to 0.9 of its speed.
	 */
	@GameTest(structure = GunsGameTests.ARENA, maxTicks = 20)
	public void heavyGunSlowsItsHolder(GameTestHelper helper) {
		GunsGameTests.floor(helper);
		Mob held = holder(helper, new BlockPos(2, 2, 2), "maelstrom_gun", InteractionHand.MAIN_HAND);
		Mob other = holder(helper, new BlockPos(6, 2, 2), "maelstrom_gun", InteractionHand.OFF_HAND);
		Mob light = holder(helper, new BlockPos(10, 2, 2), "kreuzer_pistol", InteractionHand.MAIN_HAND);
		// A held item's attribute modifiers are put on its holder as it ticks, so they are read here, not at once.
		helper.runAfterDelay(3, () -> {
			AttributeModifier weight = held.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(JugcraftGuns.WEIGHT);
			helper.assertTrue(weight != null && Math.abs(held.getAttributeValue(Attributes.MOVEMENT_SPEED) - PIG_SPEED * 0.9) < 1.0E-6,
					"The Maelstrom Gun in the main hand leaves its holder " + held.getAttributeValue(Attributes.MOVEMENT_SPEED) + ", not "
							+ PIG_SPEED * 0.9);
			helper.assertTrue(other.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(JugcraftGuns.WEIGHT) == null
					&& Math.abs(other.getAttributeValue(Attributes.MOVEMENT_SPEED) - PIG_SPEED) < 1.0E-9,
					"The Maelstrom Gun in the other hand slows its holder");
			helper.assertTrue(light.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(JugcraftGuns.WEIGHT) == null
					&& Math.abs(light.getAttributeValue(Attributes.MOVEMENT_SPEED) - PIG_SPEED) < 1.0E-9, "The Kreuzer Pistol slows its holder");
			helper.succeed();
		});
	}

	/** A stone wall across the shooters' lane (x 0 to 2) at {@code z}, from the floor up. */
	private static void wall(GameTestHelper helper, int z) {
		for (int x = 0; x <= 2; x++) {
			for (int y = 2; y <= 4; y++) {
				helper.setBlock(new BlockPos(x, y, z), Blocks.STONE);
			}
		}
	}

	/** A Maelstrom Gun's Microjet of this range, set flying south at 1.5 blocks a tick from the middle of {@code at}. */
	private static Microjet launch(GameTestHelper helper, ServerPlayer shooter, BlockPos at, double range) {
		Microjet jet = new Microjet(helper.getLevel(), shooter, new ItemStack(JugcraftGuns.ROUNDS.get("microjet")));
		jet.arm("maelstrom_gun", JugcraftGuns.SPECS.get("maelstrom_gun").damage(), range);
		BlockPos pos = helper.absolutePos(at);
		jet.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
		jet.setDeltaMovement(0.0, 0.0, 1.5);
		helper.getLevel().addFreshEntity(jet);
		return jet;
	}

	/** A gun test pig given back a pig's speed, holding this gun in this hand. */
	private static Mob holder(GameTestHelper helper, BlockPos at, String gun, InteractionHand hand) {
		Mob pig = GunsGameTests.pig(helper, at);
		pig.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(PIG_SPEED);
		pig.setItemInHand(hand, new ItemStack(JugcraftGuns.GUNS.get(gun)));
		return pig;
	}
}
