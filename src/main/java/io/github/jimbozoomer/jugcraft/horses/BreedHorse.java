package io.github.jimbozoomer.jugcraft.horses;

import com.mojang.serialization.Codec;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.Holder;
import org.jspecify.annotations.Nullable;

/**
 * A horse of one of the five breeds (docs/features/horse-breeds.md): {@code jugcraft:icelandic_horse},
 * {@code mongolian_horse}, {@code shire_horse}, {@code giant_finnhorse} and {@code friesian_horse}, one class for all
 * five (its breed is its entity type). It is a vanilla horse in every way it is tamed, saddled, fed, led, ridden, owned
 * and kept as a pet, with:
 *
 * <ul>
 * <li>its own model, saddle and size (BreedDimensions: hitbox, eyes and rider seat measured from the model);</li>
 * <li>a vanilla-style rolled base health, speed and jump, kept in {@value #BASE_TAG}, and its breed's bounded traits on
 * top as attribute modifiers with fixed ids, recomputed from its ancestry whenever it loads (so they never stack);</li>
 * <li>an ancestry ({@value #ANCESTRY_TAG}), its parents' breeds and its generation, inherited by its foals
 * (HorseLineage), shown to anyone who uses it with a stick;</li>
 * <li>saddlebags on the Shire and the Giant Finnhorse (Saddlebags, HorseCargo);</li>
 * <li>road speed (Friesian blood, on {@code jugcraft:horse_roads}) and calm recovery (Mongolian blood).</li>
 * </ul>
 * Horse armour is not fitted to these models: the body slot is refused, as on the Pegasus. Breeds pair only with
 * breeds; a vanilla horse, donkey or Pegasus does not pair with them (HorseLineage.mayPair, HorseLineageMixin).
 */
public class BreedHorse extends Horse {
	private static final EntityDataAccessor<Boolean> BAGS = SynchedEntityData.defineId(BreedHorse.class, EntityDataSerializers.BOOLEAN);
	/**
	 * The effective road bonus (its ancestry's, HorseLineage.traits), set by the server whenever its traits are applied and
	 * synced to every tracking client, so a rider's client (which moves a ridden horse) uses the very same value.
	 */
	private static final EntityDataAccessor<Float> ROAD_BONUS = SynchedEntityData.defineId(BreedHorse.class, EntityDataSerializers.FLOAT);
	public static final String ANCESTRY_TAG = "JugcraftAncestry";
	public static final String PARENTS_TAG = "JugcraftParents";
	public static final String GENERATION_TAG = "JugcraftGeneration";
	public static final String BASE_TAG = "JugcraftBase";
	public static final TagKey<Block> ROADS = TagKey.create(Registries.BLOCK, Jugcraft.id("horse_roads"));
	public static final Identifier HEALTH = Jugcraft.id("horse_breed_health");
	public static final Identifier SPEED = Jugcraft.id("horse_breed_speed");
	public static final Identifier JUMP = Jugcraft.id("horse_breed_jump");
	public static final Identifier STEP = Jugcraft.id("horse_breed_step");
	public static final Identifier FALL = Jugcraft.id("horse_breed_safe_fall");
	public static final Identifier KNOCKBACK = Jugcraft.id("horse_breed_knockback");
	public static final Identifier ARMOR = Jugcraft.id("horse_breed_armor");
	public static final Identifier ROAD = Jugcraft.id("horse_breed_road");
	private static final int SATURATED = 1_000_000;

	private int[] ancestry;
	private String parents = "";
	private int generation;
	private double[] base = HorseLineage.sane(Double.NaN, Double.NaN, Double.NaN);
	private HorseLineage.Traits traits;
	private final Saddlebags bags = new Saddlebags(this, fitted -> entityData.set(BAGS, fitted));
	private float lastHealth = -1.0F;
	private int calmTicks;

	public BreedHorse(EntityType<? extends Horse> type, Level level) {
		super(type, level);
		ancestry = HorseLineage.pure(breed());
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Horse.createBaseHorseAttributes();
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(BAGS, false);
		builder.define(ROAD_BONUS, 0.0F);
	}

	/** Its breed: its entity type's. */
	public HorseBreed breed() {
		return HorseBreeds.breedOf(getType());
	}

	public int[] ancestry() {
		return ancestry.clone();
	}

	public String parents() {
		return parents;
	}

	public int generation() {
		return generation;
	}

	/** Its rolled base {health, speed, jump}, before breed traits. */
	public double[] base() {
		return base.clone();
	}

	public HorseLineage.Traits traits() {
		if (traits == null) {
			traits = HorseLineage.traits(breed(), ancestry);
		}
		return traits;
	}

	public Saddlebags bags() {
		return bags;
	}

	/** Whether saddlebags are fitted (synced, for every client's renderer). */
	public boolean bagsFitted() {
		return entityData.get(BAGS);
	}

	// ------------------------------------------------------------------------------------------------ spawning and traits

	@Override
	public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
			@Nullable SpawnGroupData group) {
		SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, group);
		setLineage(HorseLineage.pure(breed()), "", 0);
		setBase(HorseLineage.roll(random::nextDouble));
		applyTraits();
		setHealth(getMaxHealth());
		return data;
	}

	/** Server, foals and tests: its ancestry, parents' breeds and generation (traits follow at {@link #applyTraits}). */
	public void setLineage(int[] shares, String parentBreeds, int generation) {
		int sum = 0;
		for (int share : shares) {
			sum += share;
		}
		ancestry = sum == HorseLineage.TOTAL && shares.length == HorseBreed.GROUND.size() ? shares.clone() : HorseLineage.pure(breed());
		parents = parentBreeds == null ? "" : parentBreeds;
		this.generation = Math.clamp(generation, 0, HorseLineage.MAX_GENERATION);
		traits = null;
	}

	/** Server, foals and tests: its rolled base {health, speed, jump}, clamped to vanilla's horse ranges. */
	public void setBase(double[] values) {
		base = HorseLineage.sane(values[0], values[1], values[2]);
		setBaseValue(Attributes.MAX_HEALTH, base[0]);
		setBaseValue(Attributes.MOVEMENT_SPEED, base[1]);
		setBaseValue(Attributes.JUMP_STRENGTH, base[2]);
	}

	private void setBaseValue(Holder<Attribute> attribute, double value) {
		AttributeInstance instance = getAttribute(attribute);
		if (instance != null) {
			instance.setBaseValue(value);
		}
	}

	/** Its breed traits as permanent modifiers with fixed ids: replaced, never added twice. */
	public void applyTraits() {
		traits = null;
		HorseLineage.Traits t = traits();
		modifier(Attributes.MAX_HEALTH, HEALTH, t.health() - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
		modifier(Attributes.MOVEMENT_SPEED, SPEED, t.speed() - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
		modifier(Attributes.JUMP_STRENGTH, JUMP, t.jump() - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
		modifier(Attributes.STEP_HEIGHT, STEP, t.step(), AttributeModifier.Operation.ADD_VALUE);
		modifier(Attributes.SAFE_FALL_DISTANCE, FALL, t.safeFall(), AttributeModifier.Operation.ADD_VALUE);
		modifier(Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK, t.knockback(), AttributeModifier.Operation.ADD_VALUE);
		modifier(Attributes.ARMOR, ARMOR, t.armor(), AttributeModifier.Operation.ADD_VALUE);
		entityData.set(ROAD_BONUS, (float) t.roadBonus());
		if (getHealth() > getMaxHealth()) {
			setHealth(getMaxHealth());
		}
	}

	private void modifier(Holder<Attribute> attribute, Identifier id, double amount, AttributeModifier.Operation operation) {
		AttributeInstance instance = getAttribute(attribute);
		if (instance == null) {
			return;
		}
		AttributeModifier current = instance.getModifier(id);
		if (current != null && current.amount() == amount) {
			return;
		}
		if (current != null) {
			instance.removeModifier(id);
		}
		if (Math.abs(amount) > 1e-9) {
			instance.addPermanentModifier(new AttributeModifier(id, amount, operation));
		}
	}

	// ------------------------------------------------------------------------------------------------ ticking

	@Override
	public void tick() {
		super.tick();
		roadSpeed();
		if (level() instanceof ServerLevel) {
			recover();
		}
	}

	/** The road bonus every side uses: the server's synced value, clamped to the Friesian's. */
	public double roadBonus() {
		return Math.clamp(entityData.get(ROAD_BONUS), 0.0F, (float) HorseBreed.FRIESIAN.roadBonus);
	}

	/**
	 * Friesian blood: faster on road blocks. Applied only where the horse is moved: by its rider's own client while a
	 * player rides it, otherwise by the server. The other side never touches it (the server only drops its own modifier
	 * once a player takes over), so client and server never undo each other's; both use the synced bonus.
	 */
	private void roadSpeed() {
		AttributeInstance speed = getAttribute(Attributes.MOVEMENT_SPEED);
		if (speed == null) {
			return;
		}
		boolean ridden = getControllingPassenger() instanceof Player;
		boolean moves = level().isClientSide() ? getControllingPassenger() instanceof Player rider && rider.isLocalPlayer() : !ridden;
		AttributeModifier current = speed.getModifier(ROAD);
		if (!moves) {
			if (!level().isClientSide() && current != null) {
				speed.removeModifier(ROAD);
			}
			return;
		}
		double bonus = roadBonus();
		boolean road = bonus > 0 && onGround() && level().getBlockState(getOnPos()).is(ROADS);
		if (road && (current == null || Math.abs(current.amount() - bonus) > 1e-6)) {
			if (current != null) {
				speed.removeModifier(ROAD);
			}
			speed.addTransientModifier(new AttributeModifier(ROAD, bonus, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
		} else if (!road && current != null) {
			speed.removeModifier(ROAD);
		}
	}

	/** Mongolian blood: a calm horse slowly regains health (HorseLineage.recover). */
	private void recover() {
		double recovery = traits().recovery();
		if (recovery <= 0) {
			return;
		}
		float health = getHealth();
		calmTicks = lastHealth >= 0 && health < lastHealth - 0.001F ? 0 : Math.min(calmTicks + 1, SATURATED);
		if (tickCount % HorseLineage.RECOVERY_TICKS == 0) {
			float heal = HorseLineage.recover(recovery, calmTicks, health, getMaxHealth());
			if (heal > 0) {
				heal(heal);
			}
		}
		lastHealth = getHealth();
	}

	// ------------------------------------------------------------------------------------------------ breeding

	/** Breeds pair only with breeds (HorseLineage.mayPair); vanilla's own conditions (tamed, adult, in love) still apply. */
	@Override
	public boolean canMate(Animal other) {
		return other instanceof BreedHorse && super.canMate(other);
	}

	/** The foal (HorseLineage.foal): its breed from the shared ancestry, base stats from its parents', traits capped. */
	@Override
	public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob mate) {
		if (!(mate instanceof BreedHorse other)) {
			return null;
		}
		HorseLineage.Foal foal = HorseLineage.foal(breed(), ancestry, other.breed(), other.ancestry, random.nextDouble());
		if (foal == null) {
			return null;
		}
		BreedHorse child = HorseBreeds.type(foal.breed()).create(level, EntitySpawnReason.BREEDING);
		if (child == null) {
			return null;
		}
		child.setLineage(foal.shares(), breed().id() + "," + other.breed().id(), HorseLineage.generation(generation, other.generation));
		child.setBase(HorseLineage.inherit(base, other.base, random::nextDouble));
		child.applyTraits();
		child.setHealth(child.getMaxHealth());
		ServerPlayer breeder = getLoveCause();
		if (breeder != null) {
			breeder.sendSystemMessage(Component.translatable("message.jugcraft.horse_breeds.foal",
					Component.translatable("entity.jugcraft." + foal.breed().id()), ancestryText(foal.shares())));
		}
		return child;
	}

	/** "50% Shire, 50% Friesian", in the reader's language. */
	public static Component ancestryText(int[] shares) {
		MutableComponent text = Component.empty();
		boolean first = true;
		boolean[] done = new boolean[shares.length];
		for (int k = 0; k < shares.length; k++) {
			int best = -1;
			for (int i = 0; i < shares.length; i++) {
				if (!done[i] && shares[i] > 0 && (best < 0 || shares[i] > shares[best])) {
					best = i;
				}
			}
			if (best < 0) {
				break;
			}
			done[best] = true;
			if (!first) {
				text.append(Component.literal(", "));
			}
			first = false;
			text.append(Component.literal(HorseLineage.percent(shares[best]) + "% ")).append(Component.translatable("entity.jugcraft." + HorseBreed.GROUND.get(best).id()));
		}
		return text;
	}

	// ------------------------------------------------------------------------------------------------ using it

	/**
	 * Saddlebags first (Saddlebags.interact: a chest fits them, sneak-use with a stick opens them); a plain use with a
	 * stick shows its lineage; everything else is the vanilla horse's.
	 */
	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		InteractionResult cargo = bags.interact(player, hand, traits().cargoRows(), false,
				Component.translatable("container.jugcraft.saddlebags", getDisplayName()));
		if (cargo != null) {
			return cargo;
		}
		ItemStack held = player.getItemInHand(hand);
		if (held.is(Items.STICK) && !player.isSecondaryUseActive()) {
			if (player instanceof ServerPlayer server) {
				server.sendSystemMessage(lineageCard());
			}
			return InteractionResult.SUCCESS;
		}
		return super.mobInteract(player, hand);
	}

	/** Its breed, ancestry, generation and parents, and how much its bags hold. */
	public Component lineageCard() {
		MutableComponent card = Component.translatable("message.jugcraft.horse_breeds.card", getDisplayName(),
				Component.translatable("entity.jugcraft." + breed().id()), ancestryText(ancestry), generation);
		if (!parents.isEmpty()) {
			String[] pair = parents.split(",");
			if (pair.length == 2) {
				card.append(Component.translatable("message.jugcraft.horse_breeds.card_parents", Component.translatable("entity.jugcraft." + pair[0]),
						Component.translatable("entity.jugcraft." + pair[1])));
			}
		}
		int rows = traits().cargoRows();
		if (rows > 0) {
			card.append(Component.translatable("message.jugcraft.horse_breeds.card_bags", HorseCargo.capacity(rows)));
		}
		return card;
	}

	/** Horse armour is not fitted to the breed models: the body slot is refused (saddle and the rest as a horse). */
	@Override
	public boolean canUseSlot(EquipmentSlot slot) {
		return slot != EquipmentSlot.BODY && super.canUseSlot(slot);
	}

	/** A real death drops the bags and everything in them once (a pet knock-out cancels the death and drops nothing). */
	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
		super.dropCustomDeathLoot(level, source, recentlyHit);
		bags.dropAll(level);
	}

	// ------------------------------------------------------------------------------------------------ saving

	@Override
	protected void addAdditionalSaveData(ValueOutput saved) {
		super.addAdditionalSaveData(saved);
		saved.putString(ANCESTRY_TAG, HorseLineage.encode(ancestry));
		saved.putString(PARENTS_TAG, parents);
		saved.putInt(GENERATION_TAG, generation);
		saved.store(BASE_TAG, Codec.DOUBLE.listOf(), List.of(base[0], base[1], base[2]));
		bags.save(saved);
	}

	/**
	 * Its lineage, base roll and bags read back, then its traits recomputed from them: a recall, a knock-out and a reload
	 * all come back with the same breed, ancestry, stats and cargo. A damaged ancestry reads as its breed's pure line.
	 */
	@Override
	protected void readAdditionalSaveData(ValueInput saved) {
		super.readAdditionalSaveData(saved);
		setLineage(HorseLineage.decode(saved.getStringOr(ANCESTRY_TAG, ""), breed()), saved.getStringOr(PARENTS_TAG, ""),
				saved.getIntOr(GENERATION_TAG, 0));
		List<Double> values = saved.read(BASE_TAG, Codec.DOUBLE.listOf()).orElse(List.of());
		setBase(values.size() == 3 ? new double[] {values.get(0), values.get(1), values.get(2)}
				: new double[] {Double.NaN, Double.NaN, Double.NaN});
		bags.load(saved);
		applyTraits();
	}
}
