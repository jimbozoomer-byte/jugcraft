package io.github.jimbozoomer.jugcraft.lineage;

import com.mojang.serialization.Codec;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.concordance.RateGate;
import io.github.jimbozoomer.jugcraft.concordance.Saved;
import io.github.jimbozoomer.jugcraft.concordance.celestial.Calendar;
import io.github.jimbozoomer.jugcraft.concordance.celestial.Pattern;
import io.github.jimbozoomer.jugcraft.concordance.rules.ResearchState;
import io.github.jimbozoomer.jugcraft.concordance.sky.Sky;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import io.github.jimbozoomer.jugcraft.mixin.CompanionShearingInvoker;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Star-bred lines, phase 1 (docs/features/star-bred-lines.md; the plan is docs/plans/star-bred-lines-2026-10-28.md).
 * <p>
 * Every animal of a kind in {@link #STAR_BRED} carries a {@link Line}, saved on it as the {@link #LINE} attachment. A
 * wild one is given its line the first time it loads, worked out from its own UUID ({@link #wild}), so loading it again
 * gives the same and there is nothing to re-roll. A baby bred from two takes one copy of each trait from each parent and
 * one of each gift's copies from each, at random ({@link #inherit}); born where a celestial pattern is up and can be
 * seen from its block, it takes the rarest such pattern's sign, which raises the weaker copy of that pattern's trait by
 * one grade ({@link #SIGNS}), and the Echo Comet also wakes a copy of one of its kind's gifts.
 * <p>
 * What a line does, all on the server: Yield more meat or wool, and eggs that come as two (the drops of the animal's
 * own loot tables, through Fabric's MODIFY_DROPS); Vigour health and speed (permanent attribute modifiers under fixed
 * ids, so putting them back on each load is idempotent) and babies that grow up sooner; Fertility less rest after
 * breeding; Hardiness damage it is spared (ALLOW_DAMAGE). Every effect starts past {@value #VANILLA_UP_TO}: a trait of 4
 * or below is exactly vanilla. The gifts are drops too, but for Angora and Woolly Coat, coats sheared with shears (a
 * use of shears on the animal, kept from repeating for {@value #REGROW_TICKS} ticks by the {@link #SHORN} attachment).
 * Nothing here runs per tick.
 * <p>
 * The Astrawand reads a line ({@link #reading}). How much it shows follows the reader's Celestial Attunement:
 * understood marks each trait that breeds true; mastered says whether it carries a gift it does not show.
 */
public final class Lineage {
	/** Lines are husbandry: they live and die with agriculture's switch. Signs also need the Concordance's sky. */
	public static final String FEATURE = JugcraftAgriculture.FEATURE;
	/** The kinds that carry a line (tools/lineage.py KINDS). */
	public static final TagKey<EntityType<?>> STAR_BRED = TagKey.create(Registries.ENTITY_TYPE, Jugcraft.id("star_bred"));
	/** What Yield gives more of at death: each kind's meat, raw or cooked. */
	public static final TagKey<Item> PRODUCE = TagKey.create(Registries.ITEM, Jugcraft.id("line_produce"));

	/** A trait of this or below is exactly vanilla. */
	public static final int VANILLA_UP_TO = 4;
	/** A wild copy's grade, 0 to 3, by these weights (in a hundred); grades 4 and 5 never occur wild. */
	public static final int[] WILD_WEIGHTS = {20, 35, 30, 15};
	/** 1 wild animal in this many carries one copy of one of its kind's gifts. */
	public static final int WILD_CARRIER = 6;
	/** Mixed into a wild line's seed, so a line is not the UUID's other uses of randomness. */
	private static final long WILD_SALT = 0x5741522D4C494E45L;

	/** Yield: one more of the produce from this number, two more from {@link #YIELD_TWO}. */
	public static final int YIELD_ONE = 6;
	public static final int YIELD_TWO = 9;
	/** Yield: an egg comes as two 1 time in this many at {@link #YIELD_ONE}, and in {@link #EGG_TWO} at {@link #YIELD_TWO}. */
	public static final int EGG_ONE = 4;
	public static final int EGG_TWO = 2;
	/** Double Yolk: an egg comes as two 1 time in this many. */
	public static final int DOUBLE_YOLK = 3;
	/** Vigour: health and speed, as a share of the base, for each point past {@value #VANILLA_UP_TO}. */
	public static final double HEALTH_PER_POINT = 0.1;
	public static final double SPEED_PER_POINT = 0.025;
	/** Vigour: a baby's growing time is this much shorter for each point. */
	public static final double GROW_PER_POINT = 0.04;
	/** Fertility: the rest after breeding is this much shorter for each point. */
	public static final double REST_PER_POINT = 0.05;
	/** Vanilla's growing time (AgeableMob's baby start age, made positive). */
	public static final int GROWING = 24000;
	/** Hardiness: the number from which each harm is spared. */
	public static final int SPARE_THORNS = 5;
	public static final int SPARE_FREEZING = 6;
	public static final int SPARE_FALLING = 7;
	public static final int SPARE_DROWNING = 8;
	public static final int SPARE_BURNING = 9;
	public static final int SPARE_LIGHTNING = 10;
	/** Angora and Woolly Coat: a coat grows back over a game day. */
	public static final int REGROW_TICKS = 24000;
	/** Moonfleece: the night it counts in, ticks of the day (19:00 to 05:00, as the moons' patterns). */
	public static final int NIGHT_FROM = 13000;
	public static final int NIGHT_TO = 23000;
	/** The Astrawand reads at most once in this many ticks for a player. */
	public static final int READ_TICKS = 10;

	/** The trait each pattern's sign raises (tools/lineage.py SIGNS); every pattern in the data has one. */
	public static final Map<String, Trait> SIGNS = Map.of(
			"jugcraft:full_moon", Trait.YIELD,
			"jugcraft:new_moon", Trait.FERTILITY,
			"jugcraft:lantern_star", Trait.VIGOUR,
			"jugcraft:echo_comet", Trait.HARDINESS,
			"jugcraft:winter_crown", Trait.HARDINESS,
			"jugcraft:harvest_moon", Trait.YIELD);
	/** The pattern whose sign also wakes a copy of a gift. */
	public static final String WAKING = "jugcraft:echo_comet";

	/** The coat each shearable gift's animals give, as a loot table (data/jugcraft/loot_table/lineage/). */
	public static final Map<Gift, ResourceKey<LootTable>> COATS = Map.of(
			Gift.ANGORA, ResourceKey.create(Registries.LOOT_TABLE, Jugcraft.id("lineage/angora")),
			Gift.WOOLLY_COAT, ResourceKey.create(Registries.LOOT_TABLE, Jugcraft.id("lineage/woolly_coat")));

	private static final Identifier VIGOUR_HEALTH = Jugcraft.id("lineage_vigour_health");
	private static final Identifier VIGOUR_SPEED = Jugcraft.id("lineage_vigour_speed");

	public static AttachmentType<Line> LINE;
	/** The game time a gift coat was last shorn. */
	public static AttachmentType<Long> SHORN;
	public static Item ASTRAWAND;

	private Lineage() {
	}

	public static void register() {
		// Before any world or entity loads (Fabric reads attachments as entities load).
		LINE = AttachmentRegistry.<Line>builder().persistent(Saved.versioned("lineage", Line.CODEC)).copyOnDeath()
				.buildAndRegister(Jugcraft.id("lineage"));
		SHORN = AttachmentRegistry.<Long>builder().persistent(Codec.LONG).copyOnDeath().buildAndRegister(Jugcraft.id("lineage_shorn"));
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Jugcraft.id("astrawand"));
		ASTRAWAND = Registry.register(BuiltInRegistries.ITEM, key,
				new AstrawandItem(new Item.Properties().setId(key).stacksTo(1).rarity(Rarity.UNCOMMON)));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(output -> output.accept(ASTRAWAND));
		PayloadTypeRegistry.clientboundPlay().register(AstrawandGesturePayload.TYPE, AstrawandGesturePayload.CODEC);
		ServerEntityEvents.ENTITY_LOAD.register(Lineage::loaded);
		ServerLivingEntityEvents.ALLOW_DAMAGE.register(Lineage::allowDamage);
		LootTableEvents.MODIFY_DROPS.register(Lineage::drops);
		UseEntityCallback.EVENT.register(Lineage::shear);
	}

	public static boolean enabled() {
		return JugcraftConfig.isFeatureEnabled(FEATURE);
	}

	public static boolean bred(Entity entity) {
		return entity instanceof Animal && entity.getType().builtInRegistryHolder().is(STAR_BRED);
	}

	// ---------------------------------------------------------------- lines

	/** The animal's line: its own, or (for one that has none yet) its wild line, which is then kept on it. */
	public static Line line(Animal animal) {
		Line line = animal.getAttached(LINE);
		if (line == null) {
			line = wild(animal.getUUID(), animal.getType());
			animal.setAttached(LINE, line);
		}
		return line;
	}

	/** The line of a wild animal with this UUID: the same every time. */
	public static Line wild(UUID id, EntityType<?> type) {
		RandomSource random = RandomSource.create(id.getMostSignificantBits() ^ Long.rotateLeft(id.getLeastSignificantBits(), 17) ^ WILD_SALT);
		int[] grades = new int[Line.GRADES];
		for (int i = 0; i < grades.length; i++) {
			grades[i] = wildGrade(random.nextInt(100));
		}
		Map<String, Integer> gifts = new HashMap<>();
		List<Gift> possible = Gift.of(type);
		if (!possible.isEmpty() && random.nextInt(WILD_CARRIER) == 0) {
			gifts.put(possible.get(random.nextInt(possible.size())).id, 1);
		}
		return Line.of(grades, gifts, "", 0);
	}

	/** The wild grade a roll of 0 to 99 gives, by {@link #WILD_WEIGHTS}. */
	public static int wildGrade(int roll) {
		int grade = 0;
		for (int weight : WILD_WEIGHTS) {
			if (roll < weight) {
				return grade;
			}
			roll -= weight;
			grade++;
		}
		return WILD_WEIGHTS.length - 1;
	}

	/** A baby's line before its sign: one copy of each trait from each parent, one of each gift's copies from each. */
	public static Line inherit(Line mother, Line father, RandomSource random) {
		int[] grades = new int[Line.GRADES];
		for (Trait trait : Trait.values()) {
			grades[trait.ordinal() * 2] = mother.grade(trait, random.nextInt(2));
			grades[trait.ordinal() * 2 + 1] = father.grade(trait, random.nextInt(2));
		}
		Map<String, Integer> gifts = new HashMap<>();
		for (Gift gift : Gift.values()) {
			int copies = passed(mother.copies(gift), random) + passed(father.copies(gift), random);
			if (copies > 0) {
				gifts.put(gift.id, copies);
			}
		}
		return Line.of(grades, gifts, "", Math.max(mother.generation(), father.generation()) + 1);
	}

	/** Whether a parent with {@code copies} of a gift passes one on: always with two, half the time with one. */
	private static int passed(int copies, RandomSource random) {
		return copies >= 2 ? 1 : copies == 1 ? random.nextInt(2) : 0;
	}

	/**
	 * The sign a baby takes: of the patterns up at world time {@code time} that {@code sky} lets be seen (null for seen,
	 * else why not, as {@link Sky#obscured}), the one paying the most resonance (the rarest); the first in id order of
	 * equals; null when none.
	 */
	public static @Nullable Pattern sign(long time, Function<Pattern, String> sky) {
		Pattern best = null;
		for (Pattern pattern : Sky.up(time)) {
			if (sky.apply(pattern) == null && (best == null || pattern.resonance() > best.resonance())) {
				best = pattern;
			}
		}
		return best;
	}

	/** {@code line} born under {@code sign}: the sign kept, its trait raised, and under the Echo Comet a gift woken. */
	public static Line signed(Line line, @Nullable Pattern sign, EntityType<?> type, RandomSource random) {
		if (sign == null) {
			return line;
		}
		Line out = line.withSign(sign.id());
		Trait trait = SIGNS.get(sign.id());
		if (trait != null) {
			out = out.raised(trait);
		}
		if (sign.id().equals(WAKING)) {
			out = out.woken(type, random);
		}
		return out;
	}

	// ---------------------------------------------------------------- births (LineageBreedingMixin)

	/**
	 * A baby bred from {@code parent} and {@code mate}, not yet added to the world (vanilla has just set the parents'
	 * rest): its line, sign, health and growing time, and the parents' rest by their Fertility.
	 */
	public static void born(ServerLevel level, Animal parent, Animal mate, @Nullable AgeableMob child) {
		if (!enabled() || !(child instanceof Animal baby) || !bred(baby) || !bred(parent) || !bred(mate)) {
			return;
		}
		BlockPos at = baby.blockPosition();
		Pattern sign = Sky.enabled() ? sign(Sky.time(level), pattern -> Sky.obscured(level, at, pattern)) : null;
		born(parent, mate, baby, sign, level.getRandom());
	}

	/** {@link #born(ServerLevel, Animal, Animal, AgeableMob)} under a given sign (tests choose it). */
	public static Line born(Animal parent, Animal mate, Animal baby, @Nullable Pattern sign, RandomSource random) {
		Line mother = line(parent);
		Line father = line(mate);
		Line line = signed(inherit(mother, father, random), sign, baby.getType(), random);
		baby.setAttached(LINE, line);
		applyVigour(baby, line);
		baby.setHealth(baby.getMaxHealth());
		baby.setAge(-growing(line));
		rest(parent, mother);
		rest(mate, father);
		return line;
	}

	/** A baby's growing time, in ticks: vanilla's, shortened by its Vigour. */
	public static int growing(Line line) {
		return (int) Math.round(GROWING * (1.0 - GROW_PER_POINT * line.points(Trait.VIGOUR)));
	}

	/** The parent's rest after breeding (set by vanilla, or whatever set it), shortened by its Fertility. */
	private static void rest(Animal parent, Line line) {
		if (parent.getAge() > 0) {
			parent.setAge((int) Math.round(parent.getAge() * restShare(line)));
		}
	}

	/** The share of the rest after breeding a line keeps: 1 at Fertility 4 or below, 0.7 at 10. */
	public static double restShare(Line line) {
		return 1.0 - REST_PER_POINT * line.points(Trait.FERTILITY);
	}

	// ---------------------------------------------------------------- what a line does

	private static void loaded(Entity entity, ServerLevel level) {
		if (enabled() && entity instanceof Animal animal && bred(animal)) {
			applyVigour(animal, line(animal));
		}
	}

	/** Vigour's health and speed, as permanent modifiers under fixed ids (so again is the same). */
	public static void applyVigour(Animal animal, Line line) {
		int points = line.points(Trait.VIGOUR);
		modify(animal.getAttribute(Attributes.MAX_HEALTH), VIGOUR_HEALTH, HEALTH_PER_POINT * points);
		modify(animal.getAttribute(Attributes.MOVEMENT_SPEED), VIGOUR_SPEED, SPEED_PER_POINT * points);
		if (animal.getHealth() > animal.getMaxHealth()) {
			animal.setHealth(animal.getMaxHealth());
		}
	}

	private static void modify(@Nullable AttributeInstance attribute, Identifier id, double amount) {
		if (attribute == null) {
			return;
		}
		if (amount <= 0.0) {
			attribute.removeModifier(id);
		} else {
			attribute.addOrReplacePermanentModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
		}
	}

	private static boolean allowDamage(LivingEntity victim, DamageSource source, float amount) {
		if (!enabled() || !(victim instanceof Animal animal) || !bred(animal)) {
			return true;
		}
		Line line = animal.getAttached(LINE);
		return line == null || !spared(source, line.value(Trait.HARDINESS));
	}

	/** Whether Hardiness {@code hardiness} spares a harm: each from its number on, and every one below it too. */
	public static boolean spared(DamageSource source, int hardiness) {
		return hardiness >= SPARE_THORNS && (source.is(DamageTypes.CACTUS) || source.is(DamageTypes.SWEET_BERRY_BUSH))
				|| hardiness >= SPARE_FREEZING && source.is(DamageTypeTags.IS_FREEZING)
				|| hardiness >= SPARE_FALLING && source.is(DamageTypeTags.IS_FALL)
				|| hardiness >= SPARE_DROWNING && source.is(DamageTypeTags.IS_DROWNING)
				|| hardiness >= SPARE_BURNING && source.is(DamageTypes.ON_FIRE)
				|| hardiness >= SPARE_LIGHTNING && source.is(DamageTypes.LIGHTNING_BOLT);
	}

	/** One or two more of the produce at death or shearing, by Yield. */
	public static int extra(Line line) {
		int yield = line.value(Trait.YIELD);
		return yield >= YIELD_TWO ? 2 : yield >= YIELD_ONE ? 1 : 0;
	}

	/** Vanilla's chicken laying table (Chicken lays through it, as a gift table). */
	public static final ResourceKey<LootTable> LAYING = ResourceKey.create(Registries.LOOT_TABLE, Identifier.withDefaultNamespace("gameplay/chicken_lay"));

	/** What kind of drops a table's are. */
	public enum Drops {
		DEATH, SHEARING, LAYING
	}

	/**
	 * What kind of drops {@code table}'s are for {@code animal}: its own death table, sheep shearing or a gift coat, or
	 * vanilla's laying; null for any other, a table nested in one of these among them (Fabric's MODIFY_DROPS sees nested
	 * tables too, so only the one the animal rolls is counted, and nothing twice).
	 */
	public static @Nullable Drops kind(Animal animal, ResourceKey<LootTable> table) {
		if (animal.getLootTable().map(table::equals).orElse(false)) {
			return Drops.DEATH;
		}
		if (table.equals(BuiltInLootTables.SHEAR_SHEEP) || COATS.containsValue(table)) {
			return Drops.SHEARING;
		}
		return table.equals(LAYING) ? Drops.LAYING : null;
	}

	private static void drops(Holder<LootTable> table, LootContext context, List<ItemStack> drops) {
		if (!enabled() || !(context.getOptional(LootContextParams.THIS_ENTITY) instanceof Animal animal) || !bred(animal)) {
			return;
		}
		Line line = animal.getAttached(LINE);
		Drops kind = table.unwrapKey().map(key -> kind(animal, key)).orElse(null);
		if (line == null || kind == null) {
			return;
		}
		boolean night = kind == Drops.SHEARING && animal.level() instanceof ServerLevel level && openNight(level, animal.blockPosition(), Sky.time(level));
		more(kind, line, drops, context.getRandom(), night);
	}

	/**
	 * What a line adds to drops of {@code kind}: at death, Yield's produce, Heavy Hide's leather and Lucky Foot's foot;
	 * shorn, Yield's wool and (on an open night) Moonfleece's; laid, Yield's and Double Yolk's second egg.
	 */
	public static void more(Drops kind, Line line, List<ItemStack> drops, RandomSource random, boolean openNight) {
		switch (kind) {
			case DEATH -> {
				grow(drops, stack -> stack.is(PRODUCE), extra(line));
				if (line.shows(Gift.HEAVY_HIDE) && !grow(drops, stack -> stack.is(Items.LEATHER), 1)) {
					drops.add(new ItemStack(Items.LEATHER));
				}
				if (line.shows(Gift.LUCKY_FOOT) && drops.stream().noneMatch(stack -> stack.is(Items.RABBIT_FOOT))) {
					drops.add(new ItemStack(Items.RABBIT_FOOT));
				}
			}
			case SHEARING -> grow(drops, stack -> stack.is(ItemTags.WOOL),
					extra(line) + (openNight && line.shows(Gift.MOONFLEECE) ? 1 : 0));
			case LAYING -> grow(drops, stack -> !stack.isEmpty(), eggs(line, random) - 1);
		}
	}

	/** How many eggs one laying gives: one, and one more by Yield's chance and again by Double Yolk's. */
	public static int eggs(Line line, RandomSource random) {
		int yield = line.value(Trait.YIELD);
		int eggs = 1;
		if (yield >= YIELD_TWO ? random.nextInt(EGG_TWO) == 0 : yield >= YIELD_ONE && random.nextInt(EGG_ONE) == 0) {
			eggs++;
		}
		if (line.shows(Gift.DOUBLE_YOLK) && random.nextInt(DOUBLE_YOLK) == 0) {
			eggs++;
		}
		return eggs;
	}

	/** Adds {@code count} to the first stack that matches; whether one did. */
	private static boolean grow(List<ItemStack> drops, java.util.function.Predicate<ItemStack> which, int count) {
		if (count <= 0) {
			return false;
		}
		for (ItemStack stack : drops) {
			if (which.test(stack)) {
				stack.grow(count);
				return true;
			}
		}
		return false;
	}

	/** Night under the open sky over {@code pos} in the Overworld (Moonfleece's). */
	public static boolean openNight(ServerLevel level, BlockPos pos, long time) {
		int tick = Calendar.tickOfDay(time);
		return level.dimension() == Level.OVERWORLD && tick >= NIGHT_FROM && tick < NIGHT_TO
				&& level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ()) <= pos.getY() + 1;
	}

	/** The gift whose coat an animal of this kind can grow, if any. */
	public static @Nullable Gift coat(EntityType<?> type) {
		for (Gift gift : COATS.keySet()) {
			if (Gift.of(type).contains(gift)) {
				return gift;
			}
		}
		return null;
	}

	/** Shears used on an Angora goat or a Woolly Coat pig whose coat has grown: its coat's wool. */
	private static InteractionResult shear(Player player, Level world, InteractionHand hand, Entity entity, @Nullable EntityHitResult hit) {
		ItemStack shears = player.getItemInHand(hand);
		if (!(world instanceof ServerLevel level) || player.isSpectator() || !shears.is(Items.SHEARS) || !enabled()
				|| !(entity instanceof Animal animal) || !bred(animal) || animal.isBaby()) {
			return InteractionResult.PASS;
		}
		Gift coat = coat(animal.getType());
		if (coat == null || !line(animal).shows(coat) || !grown(animal, level.getGameTime())) {
			return InteractionResult.PASS;
		}
		animal.setAttached(SHORN, level.getGameTime());
		((CompanionShearingInvoker) animal).jugcraft$collectShearing(level, COATS.get(coat), shears,
				(where, stack) -> animal.spawnAtLocation(where, stack));
		shears.hurtAndBreak(1, player, hand.asEquipmentSlot());
		level.playSound(null, animal, SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);
		animal.gameEvent(GameEvent.SHEAR, player);
		return InteractionResult.SUCCESS;
	}

	/** Whether a coat has grown back: never shorn, or shorn {@value #REGROW_TICKS} ticks ago or more. */
	public static boolean grown(Animal animal, long gameTime) {
		Long shorn = animal.getAttached(SHORN);
		return shorn == null || gameTime < shorn || gameTime - shorn >= REGROW_TICKS;
	}

	// ---------------------------------------------------------------- the Astrawand

	/** The Astrawand pointed at {@code animal} by {@code player}: the reading, the sparkles, the chime and the flourish. */
	public static void read(ServerPlayer player, Animal animal) {
		if (!RateGate.allow(player, "astrawand", READ_TICKS)) {
			return;
		}
		List<Component> lines = bred(animal)
				? reading(animal.getDisplayName(), line(animal), Sky.state(player))
				: List.of(Component.translatable("message.jugcraft.lineage.no_line", animal.getDisplayName()));
		lines.forEach(player::sendSystemMessage);
		flourish(player, animal);
	}

	/**
	 * What the Astrawand says of a line to a reader at {@code state} in Celestial Attunement: its name and generation
	 * (or that it is wild) and its sign; the four numbers, each that breeds true marked once understood; the gifts it
	 * shows; and once mastered, whether it carries one it does not show (never which).
	 */
	public static List<Component> reading(Component name, Line line, ResearchState state) {
		boolean marks = state.atLeast(ResearchState.UNDERSTOOD);
		List<Component> out = new ArrayList<>();
		Component birth = line.sign().isEmpty() ? Component.translatable("message.jugcraft.lineage.no_sign")
				: Component.translatable("message.jugcraft.lineage.sign", Sky.patternName(line.sign()));
		out.add(Component.translatable(line.generation() == 0 ? "message.jugcraft.lineage.wild" : "message.jugcraft.lineage.bred",
				name.copy().withStyle(ChatFormatting.GOLD), line.generation(), birth));
		List<Component> traits = new ArrayList<>();
		boolean anyTrue = false;
		for (Trait trait : Trait.values()) {
			boolean mark = marks && line.breedsTrue(trait);
			anyTrue |= mark;
			traits.add(Component.translatable(mark ? "message.jugcraft.lineage.trait_true" : "message.jugcraft.lineage.trait",
					Component.translatable(trait.key()), line.value(trait)));
		}
		out.add(Component.translatable("message.jugcraft.lineage.traits", traits.toArray()));
		List<Gift> showing = line.showing();
		if (showing.isEmpty()) {
			out.add(Component.translatable("message.jugcraft.lineage.no_gifts"));
		} else {
			MutableComponent names = Component.empty();
			for (int i = 0; i < showing.size(); i++) {
				names.append(i == 0 ? Component.empty() : Component.literal(", ")).append(Component.translatable(showing.get(i).key()));
			}
			out.add(Component.translatable("message.jugcraft.lineage.gifts", names));
		}
		if (state.atLeast(ResearchState.MASTERED) && line.carriesUnseen()) {
			out.add(Component.translatable("message.jugcraft.lineage.unseen").withStyle(ChatFormatting.LIGHT_PURPLE));
		}
		if (anyTrue) {
			out.add(Component.translatable("message.jugcraft.lineage.true_key").withStyle(ChatFormatting.GRAY));
		}
		return out;
	}

	/** Gold sparkles from the wand's tip to the animal, notes rising from it, a chime, and the player's flourish. */
	private static void flourish(ServerPlayer player, Animal animal) {
		ServerLevel level = player.level();
		Vec3 from = player.getEyePosition().add(player.getLookAngle().scale(0.8)).add(0.0, -0.3, 0.0);
		Vec3 to = animal.position().add(0.0, animal.getBbHeight() * 0.7, 0.0);
		for (int i = 0; i <= 8; i++) {
			Vec3 at = from.lerp(to, i / 8.0);
			level.sendParticles(ParticleTypes.WAX_ON, at.x, at.y, at.z, 1, 0.02, 0.02, 0.02, 0.0);
		}
		// A note's colour is its x offset when the count is 0.
		level.sendParticles(ParticleTypes.NOTE, to.x - 0.2, to.y + 0.5, to.z, 0, 0.35, 0.0, 0.0, 1.0);
		level.sendParticles(ParticleTypes.NOTE, to.x + 0.2, to.y + 0.7, to.z, 0, 0.75, 0.0, 0.0, 1.0);
		level.playSound(null, animal.getX(), animal.getY(), animal.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.4F);
		AstrawandGesturePayload payload = new AstrawandGesturePayload(player.getId());
		for (ServerPlayer watcher : PlayerLookup.tracking(player)) {
			if (ServerPlayNetworking.canSend(watcher, AstrawandGesturePayload.TYPE)) {
				ServerPlayNetworking.send(watcher, payload);
			}
		}
		if (ServerPlayNetworking.canSend(player, AstrawandGesturePayload.TYPE)) {
			ServerPlayNetworking.send(player, payload);
		}
	}
}
