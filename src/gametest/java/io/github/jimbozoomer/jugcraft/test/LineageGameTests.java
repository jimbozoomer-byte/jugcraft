package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.concordance.RateGate;
import io.github.jimbozoomer.jugcraft.concordance.celestial.Pattern;
import io.github.jimbozoomer.jugcraft.concordance.rules.ResearchState;
import io.github.jimbozoomer.jugcraft.concordance.sky.Sky;
import io.github.jimbozoomer.jugcraft.lineage.Gift;
import io.github.jimbozoomer.jugcraft.lineage.Line;
import io.github.jimbozoomer.jugcraft.lineage.Lineage;
import io.github.jimbozoomer.jugcraft.lineage.Trait;
import io.github.jimbozoomer.jugcraft.mixin.CompanionShearingInvoker;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;

/**
 * Star-bred lines, phase 1 (docs/features/star-bred-lines.md), on a real server: a wild animal's line is the same every
 * time it is worked out and never holds the sky's grades; a baby takes one copy of each trait and of each gift from each
 * parent; a baby born under a pattern it can see takes the rarest one's sign, which raises its trait (and the Echo
 * Comet wakes a gift), and one born under a roof takes none; two animals bred for real give a baby whose line, health and
 * growing time are its own, and parents who rest by their Fertility; a line is kept through a save; each trait does what
 * the record's table says and 4 or below is vanilla; the gifts give what they say, in the animals' own loot tables
 * (death, shearing and laying); an Angora goat is shorn once a day; and the Astrawand's reading deepens with Celestial
 * Attunement. Each test passes its own sky where the sky decides, as the celestial tests do, so none waits on the
 * world's clock or weather.
 */
public class LineageGameTests {
	/** Midnight of day 0: the full moon is up. */
	private static final long NIGHT_ZERO = 18000L;
	private static final String FULL_MOON = "jugcraft:full_moon";
	private static final String ECHO_COMET = "jugcraft:echo_comet";
	private static final net.minecraft.world.item.Item BROWN_WOOL = net.minecraft.core.registries.BuiltInRegistries.ITEM
			.getValue(Identifier.withDefaultNamespace("brown_wool"));

	private static Line line(int grade, Map<String, Integer> gifts) {
		int[] grades = new int[Line.GRADES];
		Arrays.fill(grades, grade);
		return Line.of(grades, gifts, "", 0);
	}

	/** A line with these two copies of each trait, in Trait order. */
	private static Line line(int[] grades, Map<String, Integer> gifts) {
		return Line.of(grades, gifts, "", 0);
	}

	private static <T extends Animal> T animal(GameTestHelper helper, EntityType<T> type, BlockPos at, Line line) {
		T animal = helper.spawn(type, at);
		animal.setPersistenceRequired();
		animal.setAttached(Lineage.LINE, line);
		Lineage.applyVigour(animal, line);
		return animal;
	}

	private static Pattern pattern(String id) {
		return Sky.catalog().patterns().get(id);
	}

	/** A sky in which only {@code id} can be seen. */
	private static Function<Pattern, String> only(String id) {
		return pattern -> pattern.id().equals(id) ? null : "no_sky";
	}

	private static int count(List<ItemStack> drops, net.minecraft.world.item.Item item) {
		return drops.stream().filter(stack -> stack.is(item)).mapToInt(ItemStack::getCount).sum();
	}

	private static List<String> keys(List<Component> lines) {
		List<String> out = new ArrayList<>();
		for (Component line : lines) {
			keys(line, out);
		}
		return out;
	}

	/** Every translation key in a component: its own, its arguments' and its appended parts', depth first. */
	private static void keys(Component component, List<String> out) {
		if (component.getContents() instanceof TranslatableContents translatable) {
			out.add(translatable.getKey());
			for (Object arg : translatable.getArgs()) {
				if (arg instanceof Component part) {
					keys(part, out);
				}
			}
		}
		for (Component sibling : component.getSiblings()) {
			keys(sibling, out);
		}
	}

	/**
	 * A wild line is the same each time it is worked out from the same UUID; across many animals no copy is graded past 3
	 * (a wild trait is at most 4: every wild animal is vanilla), every grade 0 to 2 occurs, about 1 in {@value Lineage#WILD_CARRIER} carries one copy of a
	 * gift, always one of its own kind's, and none shows a gift.
	 */
	@GameTest
	public void aWildLineIsTheSameEveryTime(GameTestHelper helper) {
		UUID id = UUID.randomUUID();
		helper.assertTrue(Lineage.wild(id, EntityTypes.COW).equals(Lineage.wild(id, EntityTypes.COW)), "A wild line is the same each time");
		int carriers = 0;
		int animals = 1200;
		int[] seen = new int[Line.MAX_GRADE + 1];
		for (int i = 0; i < animals; i++) {
			Line line = Lineage.wild(UUID.randomUUID(), EntityTypes.CHICKEN);
			for (int grade : line.grades()) {
				seen[grade]++;
			}
			helper.assertTrue(line.showing().isEmpty() && line.generation() == 0 && line.sign().isEmpty(), "A wild line shows no gift, is generation 0 and has no sign");
			if (!line.gifts().isEmpty()) {
				carriers++;
				helper.assertTrue(line.gifts().equals(Map.of(Gift.DOUBLE_YOLK.id, 1)), "A wild chicken carries only its own kind's gift: " + line.gifts());
			}
		}
		helper.assertTrue(seen[3] == 0 && seen[4] == 0 && seen[5] == 0, "No wild copy is graded past 2, so every wild animal is vanilla");
		helper.assertTrue(seen[0] > 0 && seen[1] > 0 && seen[2] > 0, "Every wild grade 0 to 2 occurs");
		helper.assertTrue(carriers > animals / 12 && carriers < animals / 3, "About 1 in 6 wild animals carries a gift: " + carriers + " of " + animals);
		helper.assertTrue(Lineage.wild(UUID.randomUUID(), EntityTypes.WOLF).gifts().isEmpty(), "A kind with no gifts carries none");
		helper.succeed();
	}

	/** A baby takes one copy of each trait from each parent, a carrier's gift half the time and a shower's always. */
	@GameTest
	public void aBabyTakesACopyFromEachParent(GameTestHelper helper) {
		Line mother = line(Line.MAX_GRADE, Map.of(Gift.HEAVY_HIDE.id, 2));
		Line father = line(0, Map.of());
		RandomSource random = RandomSource.create(7L);
		for (int i = 0; i < 50; i++) {
			Line baby = Lineage.inherit(mother.withSign(FULL_MOON), father, random);
			for (Trait trait : Trait.values()) {
				helper.assertTrue(baby.grade(trait, 0) == Line.MAX_GRADE && baby.grade(trait, 1) == 0, "One copy of " + trait.id + " from each parent");
			}
			helper.assertTrue(baby.copies(Gift.HEAVY_HIDE) == 1 && baby.carriesUnseen() && !baby.shows(Gift.HEAVY_HIDE),
					"A shower's gift always passes on: the baby carries it");
			helper.assertTrue(baby.generation() == 1 && baby.sign().isEmpty(), "A baby is a generation on, and takes no sign from its parents");
		}
		Line carrier = line(2, Map.of(Gift.HEAVY_HIDE.id, 1));
		int showing = 0;
		for (int i = 0; i < 400; i++) {
			if (Lineage.inherit(carrier, carrier, random).shows(Gift.HEAVY_HIDE)) {
				showing++;
			}
		}
		helper.assertTrue(showing > 60 && showing < 150, "Two carriers have a baby in four that shows the gift: " + showing + " of 400");
		helper.succeed();
	}

	/**
	 * Born where only the Full Moon can be seen, a baby takes its sign and its weaker Yield copy rises by one; under a roof
	 * it takes none. Under the Echo Comet its Hardiness rises and one of its kind's gifts wakes. A grade never passes 5.
	 */
	@GameTest
	public void aSignRaisesItsTrait(GameTestHelper helper) {
		Pattern moon = pattern(FULL_MOON);
		helper.assertTrue(moon != null && Lineage.sign(NIGHT_ZERO, only(FULL_MOON)) == moon, "At midnight of day 0, a baby that can see the full moon takes its sign");
		helper.assertTrue(Lineage.sign(NIGHT_ZERO, pattern -> "no_sky") == null, "Under a roof, no sign");
		Line plain = line(3, Map.of());
		RandomSource random = RandomSource.create(3L);
		Line moonborn = Lineage.signed(plain, moon, EntityTypes.SHEEP, random);
		helper.assertTrue(moonborn.sign().equals(FULL_MOON) && moonborn.value(Trait.YIELD) == 7 && moonborn.grade(Trait.YIELD, 0) == 4
				&& moonborn.value(Trait.VIGOUR) == 6, "The Full Moon raises the weaker Yield copy (3+3 to 4+3), and nothing else: " + moonborn);
		Line again = Lineage.signed(moonborn, moon, EntityTypes.SHEEP, random);
		helper.assertTrue(again.grade(Trait.YIELD, 0) == 4 && again.grade(Trait.YIELD, 1) == 4, "The weaker copy rises: 4+3 to 4+4");
		Line top = Lineage.signed(line(Line.MAX_GRADE, Map.of()), moon, EntityTypes.SHEEP, random);
		helper.assertTrue(top.value(Trait.YIELD) == 10, "A grade never passes 5");
		Pattern comet = pattern(ECHO_COMET);
		Line cometborn = Lineage.signed(plain, comet, EntityTypes.COW, random);
		helper.assertTrue(cometborn.value(Trait.HARDINESS) == 7 && cometborn.copies(Gift.HEAVY_HIDE) == 1,
				"The Echo Comet raises Hardiness and wakes a copy of the cow's gift: " + cometborn);
		helper.assertTrue(Lineage.signed(plain, comet, EntityTypes.WOLF, random).gifts().isEmpty(), "Nothing to wake in a kind with no gifts");
		for (Map.Entry<String, Pattern> entry : Sky.catalog().patterns().entrySet()) {
			helper.assertTrue(Lineage.SIGNS.containsKey(entry.getKey()), "Every pattern's sign raises a trait: " + entry.getKey());
		}
		helper.succeed();
	}

	/**
	 * Two cows bred for real (through vanilla's breeding and the mixin): the calf's line is its parents' (every copy 5,
	 * Heavy Hide from both), a generation on; its Vigour 10 gives it 60% more health, full, and a growing time 24%
	 * shorter; and each parent's Fertility 10 shortens its rest to 70%.
	 */
	@GameTest(maxTicks = 40)
	public void breedingGivesTheBabyItsLine(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		Line best = line(Line.MAX_GRADE, Map.of(Gift.HEAVY_HIDE.id, 2));
		Animal mother = animal(helper, EntityTypes.COW, new BlockPos(1, 2, 1), best);
		Animal father = animal(helper, EntityTypes.COW, new BlockPos(2, 2, 1), best);
		mother.setAge(0);
		father.setAge(0);
		mother.spawnChildFromBreeding(level, father);
		List<Animal> calves = level.getEntitiesOfClass(Animal.class, new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(6),
				animal -> animal.getType() == EntityTypes.COW && animal.isBaby());
		helper.assertTrue(calves.size() == 1, "One calf: " + calves.size());
		Animal calf = calves.get(0);
		Line line = calf.getAttached(Lineage.LINE);
		helper.assertTrue(line != null && line.grades().stream().allMatch(grade -> grade == Line.MAX_GRADE) && line.shows(Gift.HEAVY_HIDE)
				&& line.generation() == 1, "The calf's line is its parents': " + line);
		helper.assertTrue(Math.abs(calf.getMaxHealth() - 16.0F) < 0.01F && Math.abs(calf.getHealth() - calf.getMaxHealth()) < 0.01F,
				"Vigour 10: 60% more health, and full: " + calf.getHealth() + " of " + calf.getMaxHealth());
		helper.assertTrue(calf.getAge() == -Lineage.growing(line) && Lineage.growing(line) == 18240, "Vigour 10: it grows up 24% sooner: " + calf.getAge());
		helper.assertTrue(mother.getAge() == 4200 && father.getAge() == 4200, "Fertility 10: the parents rest 70% of 6000: " + mother.getAge() + ", " + father.getAge());
		helper.succeed();
	}

	/** A line is kept through a save, with the animal. */
	@GameTest
	public void aLineIsKeptThroughASave(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		Line kept = Line.of(new int[] {5, 4, 3, 2, 1, 0, 5, 5}, Map.of(Gift.MOONFLEECE.id, 1), FULL_MOON, 4);
		Animal sheep = animal(helper, EntityTypes.SHEEP, new BlockPos(1, 2, 1), kept);
		TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		sheep.saveWithoutId(out);
		CompoundTag saved = out.buildResult();
		Animal copy = EntityTypes.SHEEP.create(level, EntitySpawnReason.LOAD);
		copy.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved));
		helper.assertTrue(kept.equals(copy.getAttached(Lineage.LINE)), "The line is kept through a save: " + copy.getAttached(Lineage.LINE));
		helper.succeed();
	}

	/**
	 * What each trait does, and 4 or below is vanilla: Vigour's health and speed (and putting them back is the same);
	 * Fertility's rest; Hardiness's harms spared from each number on and not below it.
	 */
	@GameTest
	public void eachTraitDoesWhatItSays(GameTestHelper helper) {
		Animal cow = animal(helper, EntityTypes.COW, new BlockPos(1, 2, 1), line(2, Map.of()));
		double baseSpeed = cow.getAttributeBaseValue(Attributes.MOVEMENT_SPEED);
		helper.assertTrue(Math.abs(cow.getMaxHealth() - 10.0F) < 0.01F && Math.abs(cow.getAttributeValue(Attributes.MOVEMENT_SPEED) - baseSpeed) < 1e-6,
				"Vigour 4 is vanilla");
		Line strong = line(new int[] {0, 0, 5, 5, 5, 5, 0, 0}, Map.of());
		cow.setAttached(Lineage.LINE, strong);
		Lineage.applyVigour(cow, strong);
		Lineage.applyVigour(cow, strong);
		helper.assertTrue(Math.abs(cow.getMaxHealth() - 16.0F) < 0.01F && Math.abs(cow.getAttributeValue(Attributes.MOVEMENT_SPEED) - baseSpeed * 1.15) < 1e-6,
				"Vigour 10: 60% more health and 15% more speed, however often put back: " + cow.getMaxHealth());
		Lineage.applyVigour(cow, line(2, Map.of()));
		helper.assertTrue(Math.abs(cow.getMaxHealth() - 10.0F) < 0.01F, "Back to Vigour 4, back to vanilla");
		helper.assertTrue(Lineage.restShare(line(2, Map.of())) == 1.0 && Math.abs(Lineage.restShare(strong) - 0.7) < 1e-9, "Fertility: vanilla at 4, 70% at 10");

		DamageSources sources = helper.getLevel().damageSources();
		Map<DamageSource, Integer> harms = new HashMap<>();
		harms.put(sources.cactus(), Lineage.SPARE_THORNS);
		harms.put(sources.sweetBerryBush(), Lineage.SPARE_THORNS);
		harms.put(sources.freeze(), Lineage.SPARE_FREEZING);
		harms.put(sources.fall(), Lineage.SPARE_FALLING);
		harms.put(sources.drown(), Lineage.SPARE_DROWNING);
		harms.put(sources.onFire(), Lineage.SPARE_BURNING);
		harms.put(sources.lightningBolt(), Lineage.SPARE_LIGHTNING);
		for (Map.Entry<DamageSource, Integer> harm : harms.entrySet()) {
			int at = harm.getValue();
			int[] grades = {0, 0, 0, 0, 0, 0, at / 2, at - at / 2};
			cow.setAttached(Lineage.LINE, line(grades, Map.of()));
			helper.assertTrue(!ServerLivingEntityEvents.ALLOW_DAMAGE.invoker().allowDamage(cow, harm.getKey(), 2.0F),
					"Hardiness " + at + " spares " + harm.getKey().getMsgId());
			grades[7] -= 1;
			cow.setAttached(Lineage.LINE, line(grades, Map.of()));
			helper.assertTrue(ServerLivingEntityEvents.ALLOW_DAMAGE.invoker().allowDamage(cow, harm.getKey(), 2.0F),
					"Hardiness " + (at - 1) + " does not spare " + harm.getKey().getMsgId());
		}
		cow.setAttached(Lineage.LINE, line(Line.MAX_GRADE, Map.of()));
		helper.assertTrue(ServerLivingEntityEvents.ALLOW_DAMAGE.invoker().allowDamage(cow, sources.generic(), 2.0F)
				&& ServerLivingEntityEvents.ALLOW_DAMAGE.invoker().allowDamage(cow, sources.lava(), 2.0F),
				"Hardiness 10 spares no other harm, and not lava");
		helper.succeed();
	}

	/**
	 * Yield and the gifts in the animals' own loot tables: a Yield 10 cow with Heavy Hide always drops at least three beef
	 * and a leather, and sometimes more beef than vanilla's most; a Yield 0 cow never more than vanilla's three; a Yield
	 * 10 sheep's shearing gives two more wool than vanilla's; a Lucky Foot rabbit always drops its foot; and a chicken's
	 * laying, Yield 10 and Double Yolk, comes as two or three eggs often, never more.
	 */
	@GameTest
	public void yieldAndTheGiftsAddToTheDrops(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		Animal rich = animal(helper, EntityTypes.COW, new BlockPos(1, 2, 1), line(Line.MAX_GRADE, Map.of(Gift.HEAVY_HIDE.id, 2)));
		Animal plain = animal(helper, EntityTypes.COW, new BlockPos(3, 2, 1), line(0, Map.of()));
		int most = 0;
		for (int roll = 0; roll < 40; roll++) {
			List<ItemStack> richDrops = death(level, rich);
			List<ItemStack> plainDrops = death(level, plain);
			helper.assertTrue(count(richDrops, Items.BEEF) >= 3 && count(richDrops, Items.LEATHER) >= 1,
					"Yield 10 and Heavy Hide: at least three beef and a leather: " + richDrops);
			helper.assertTrue(count(plainDrops, Items.BEEF) <= 3, "Yield 0: never more than vanilla's three beef: " + plainDrops);
			most = Math.max(most, count(richDrops, Items.BEEF));
		}
		helper.assertTrue(most > 3, "Yield 10 gives more beef than vanilla's most: " + most);

		Animal lucky = animal(helper, EntityTypes.RABBIT, new BlockPos(5, 2, 1), line(0, Map.of(Gift.LUCKY_FOOT.id, 2)));
		for (int roll = 0; roll < 10; roll++) {
			helper.assertTrue(count(death(level, lucky), Items.RABBIT_FOOT) == 1, "Lucky Foot: always one foot");
		}

		Animal sheep = animal(helper, EntityTypes.SHEEP, new BlockPos(1, 2, 3), line(Line.MAX_GRADE, Map.of()));
		for (int shear = 0; shear < 8; shear++) {
			List<ItemStack> wool = new ArrayList<>();
			((CompanionShearingInvoker) sheep).jugcraft$collectShearing(level, BuiltInLootTables.SHEAR_SHEEP, new ItemStack(Items.SHEARS),
					(where, stack) -> wool.add(stack));
			int got = wool.stream().mapToInt(ItemStack::getCount).sum();
			helper.assertTrue(got >= 3 && got <= 5, "Yield 10: vanilla's one to three wool and two more: " + got);
		}

		LootTable laying = level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,
				Identifier.withDefaultNamespace("gameplay/chicken_lay")));
		helper.assertTrue(laying != LootTable.EMPTY, "Vanilla's chicken laying table is gameplay/chicken_lay");
		Animal hen = animal(helper, EntityTypes.CHICKEN, new BlockPos(3, 2, 3), line(Line.MAX_GRADE, Map.of(Gift.DOUBLE_YOLK.id, 2)));
		Animal plainHen = animal(helper, EntityTypes.CHICKEN, new BlockPos(5, 2, 3), line(0, Map.of()));
		int twos = 0;
		for (int lay = 0; lay < 60; lay++) {
			LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, hen.position())
					.withParameter(LootContextParams.THIS_ENTITY, hen).create(LootContextParamSets.GIFT);
			int eggs = laying.getRandomItems(params).stream().mapToInt(ItemStack::getCount).sum();
			helper.assertTrue(eggs >= 1 && eggs <= 3, "A laying is one to three eggs: " + eggs);
			twos += eggs > 1 ? 1 : 0;
			LootParams plainParams = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, plainHen.position())
					.withParameter(LootContextParams.THIS_ENTITY, plainHen).create(LootContextParamSets.GIFT);
			helper.assertTrue(laying.getRandomItems(plainParams).stream().mapToInt(ItemStack::getCount).sum() == 1, "Yield 0: one egg, as vanilla");
		}
		helper.assertTrue(twos > 15, "Yield 10 and Double Yolk: a laying often comes as more than one egg: " + twos + " of 60");

		// The rules themselves, without the dice: what a line adds to each kind of drops.
		List<ItemStack> drops = new ArrayList<>(List.of(new ItemStack(Items.WOOL.white())));
		Lineage.more(Lineage.Drops.SHEARING, line(new int[] {3, 3, 0, 0, 0, 0, 0, 0}, Map.of(Gift.MOONFLEECE.id, 2)), drops, RandomSource.create(1L), true);
		helper.assertTrue(count(drops, Items.WOOL.white()) == 3, "Yield 6 and Moonfleece on an open night: one wool and two more");
		drops = new ArrayList<>(List.of(new ItemStack(Items.WOOL.white())));
		Lineage.more(Lineage.Drops.SHEARING, line(new int[] {3, 3, 0, 0, 0, 0, 0, 0}, Map.of(Gift.MOONFLEECE.id, 2)), drops, RandomSource.create(1L), false);
		helper.assertTrue(count(drops, Items.WOOL.white()) == 2, "By day, Moonfleece gives nothing more");
		ResourceKey<LootTable> dungeon = ResourceKey.create(Registries.LOOT_TABLE, Identifier.withDefaultNamespace("chests/simple_dungeon"));
		helper.assertTrue(Lineage.kind(rich, rich.getLootTable().orElseThrow()) == Lineage.Drops.DEATH
				&& Lineage.kind(sheep, BuiltInLootTables.SHEAR_SHEEP) == Lineage.Drops.SHEARING
				&& Lineage.kind(sheep, Lineage.COATS.get(Gift.ANGORA)) == Lineage.Drops.SHEARING
				&& Lineage.kind(hen, Lineage.LAYING) == Lineage.Drops.LAYING
				&& Lineage.kind(rich, sheep.getLootTable().orElseThrow()) == null && Lineage.kind(rich, dungeon) == null,
				"Only the tables an animal itself rolls are counted: its own death table, shearing, laying");
		helper.succeed();
	}

	private static List<ItemStack> death(ServerLevel level, Animal animal) {
		LootTable table = level.getServer().reloadableRegistries().getLootTable(animal.getLootTable().orElseThrow());
		LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.THIS_ENTITY, animal)
				.withParameter(LootContextParams.ORIGIN, animal.position()).withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().generic())
				.create(LootContextParamSets.ENTITY);
		return table.getRandomItems(params);
	}

	/**
	 * Shears used on an Angora goat give white wool and wear the shears; used again before a day has passed, nothing;
	 * a goat that only carries Angora, or a Woolly Coat pig's baby, is not shorn; a Woolly Coat pig gives brown wool.
	 */
	@GameTest(maxTicks = 40)
	public void anAngoraGoatIsShornOnceADay(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer player = TestPlayers.mock(helper);
		player.setGameMode(GameType.SURVIVAL);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));
		Animal goat = animal(helper, EntityTypes.GOAT, new BlockPos(1, 2, 1), line(0, Map.of(Gift.ANGORA.id, 2)));
		Animal carrier = animal(helper, EntityTypes.GOAT, new BlockPos(4, 2, 1), line(0, Map.of(Gift.ANGORA.id, 1)));
		Animal pig = animal(helper, EntityTypes.PIG, new BlockPos(1, 2, 4), line(0, Map.of(Gift.WOOLLY_COAT.id, 2)));
		Animal piglet = animal(helper, EntityTypes.PIG, new BlockPos(4, 2, 4), line(0, Map.of(Gift.WOOLLY_COAT.id, 2)));
		piglet.setAge(-24000);
		InteractionResult first = shear(player, level, goat);
		helper.assertTrue(first == InteractionResult.SUCCESS && player.getMainHandItem().getDamageValue() == 1, "An Angora goat is shorn, and the shears wear");
		helper.assertTrue(shear(player, level, goat) == InteractionResult.PASS, "Its coat has not grown back: not again today");
		helper.assertTrue(shear(player, level, carrier) == InteractionResult.PASS, "A goat that only carries Angora has no coat to shear");
		helper.assertTrue(shear(player, level, piglet) == InteractionResult.PASS, "A baby is not shorn");
		helper.assertTrue(shear(player, level, pig) == InteractionResult.SUCCESS, "A Woolly Coat pig is shorn");
		helper.assertTrue(Lineage.grown(goat, level.getGameTime() + Lineage.REGROW_TICKS), "Its coat has grown back a day later");
		helper.runAfterDelay(2, () -> {
			List<ItemEntity> white = level.getEntitiesOfClass(ItemEntity.class, goat.getBoundingBox().inflate(3), drop -> drop.getItem().is(Items.WOOL.white()));
			List<ItemEntity> brown = level.getEntitiesOfClass(ItemEntity.class, pig.getBoundingBox().inflate(3), drop -> drop.getItem().is(BROWN_WOOL));
			int whiteCount = white.stream().mapToInt(drop -> drop.getItem().getCount()).sum();
			int brownCount = brown.stream().mapToInt(drop -> drop.getItem().getCount()).sum();
			helper.assertTrue(whiteCount >= 1 && whiteCount <= 2 && brownCount >= 1 && brownCount <= 2,
					"One or two white wool from the goat and brown from the pig: " + whiteCount + ", " + brownCount);
			helper.succeed();
		});
	}

	private static InteractionResult shear(ServerPlayer player, ServerLevel level, Animal animal) {
		return UseEntityCallback.EVENT.invoker().interact(player, level, InteractionHand.MAIN_HAND, animal, null);
	}

	/**
	 * The Astrawand's reading: to anyone, the name and generation, the sign, the four numbers and the gifts shown; once
	 * Celestial Attunement is understood, a mark on each trait that breeds true; once mastered, that it carries a gift it
	 * does not show. A wild animal reads as wild. Used on an animal, the wand reads (the server's) and is used.
	 */
	@GameTest
	public void theAstrawandsReadingDeepens(GameTestHelper helper) {
		Line line = Line.of(new int[] {5, 4, 3, 3, 1, 0, 2, 2}, Map.of(Gift.MOONFLEECE.id, 2, Gift.DOUBLE_YOLK.id, 1), FULL_MOON, 3);
		Component name = Component.literal("Ewe");
		List<String> novice = keys(Lineage.reading(name, line, ResearchState.NONE));
		helper.assertTrue(novice.contains("message.jugcraft.lineage.bred") && novice.contains("message.jugcraft.lineage.sign")
				&& novice.contains("message.jugcraft.lineage.gifts") && novice.contains("lineage.jugcraft.gift.moonfleece")
				&& !novice.contains("message.jugcraft.lineage.trait_true") && !novice.contains("message.jugcraft.lineage.unseen"),
				"To anyone: generation, sign, numbers and the gifts shown, no marks: " + novice);
		List<String> understood = keys(Lineage.reading(name, line, ResearchState.UNDERSTOOD));
		helper.assertTrue(understood.contains("message.jugcraft.lineage.true_key") && !understood.contains("message.jugcraft.lineage.unseen"),
				"Understood: the traits that breed true are marked: " + understood);
		List<String> mastered = keys(Lineage.reading(name, line, ResearchState.MASTERED));
		helper.assertTrue(mastered.contains("message.jugcraft.lineage.unseen") && !mastered.contains("lineage.jugcraft.gift.double_yolk"),
				"Mastered: it carries something unseen, never named: " + mastered);
		List<String> wild = keys(Lineage.reading(name, Lineage.wild(UUID.randomUUID(), EntityTypes.SHEEP), ResearchState.NONE));
		helper.assertTrue(wild.contains("message.jugcraft.lineage.wild") && wild.contains("message.jugcraft.lineage.no_sign"), "A wild animal reads as wild: " + wild);

		ServerPlayer player = TestPlayers.mock(helper);
		RateGate.forget(player.getUUID());
		Animal cow = helper.spawn(EntityTypes.COW, new BlockPos(1, 2, 1));
		ItemStack wand = new ItemStack(Lineage.ASTRAWAND);
		player.setItemInHand(InteractionHand.MAIN_HAND, wand);
		helper.assertTrue(wand.interactLivingEntity(player, cow, InteractionHand.MAIN_HAND) == InteractionResult.SUCCESS
				&& cow.getAttached(Lineage.LINE) != null, "Pointed at a cow, the Astrawand reads it (a wild cow is given its line)");
		helper.assertTrue(JugcraftAgriculture.TURKEY.builtInRegistryHolder().is(Lineage.STAR_BRED), "Turkeys carry a line");
		helper.succeed();
	}
}
