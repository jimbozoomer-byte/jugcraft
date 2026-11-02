package io.github.jimbozoomer.jugcraft.season;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Beardshy: the shy, apologetic side of Beardson, the bearded millennial who runs the Retro Game Shop (the owner's
 * Jimboverse character; see docs/features/beardshy.md). He has the villager's body in his own look (a pink hoodie,
 * round glasses, curly hair and a ginger beard, "uwu" inked on his hands), and he will be the Harvest Feast's
 * antagonist: he wants Thanksgiving cancelled.
 *
 * <p>He is shy. A player walking up startles him and he hurries off; a player who crouches can come close and talk to
 * him. Spoken to, he says something soft, hesitant and apologetic (a startled apology when you didn't crouch). Hit, he
 * apologises and runs; he never fights back. He trades nothing, drops nothing and never spawns on his own yet: the
 * Harvest Feast story will bring him (for now {@code /summon jugcraft:beardshy}). Everything he does is on the server;
 * the client only bows his head when a player is near.
 */
public class Beardshy extends PathfinderMob {
	public static final String ID = "beardshy";
	/** A player standing (not crouching) this near startles him, in blocks. */
	public static final float STARTLE_RANGE = 5.0F;
	/** Ticks between two things he says to anyone (a blow always gets an apology). */
	public static final int TALK_COOLDOWN = 30;
	/** How many lines he has for a player who crouched to talk, a player who startled him, and one who hit him. */
	public static final int TALK_LINES = 18;
	public static final int STARTLED_LINES = 4;
	public static final int HURT_LINES = 3;
	public static final double SPEED = 0.5;

	public static EntityType<Beardshy> TYPE;

	/** When he last spoke, in game time; far enough back that he can speak at once (never Long.MIN_VALUE: now minus it overflows). */
	private long lastSpoke = -TALK_COOLDOWN;
	private int lastLine = -1;

	public Beardshy(EntityType<? extends Beardshy> type, Level level) {
		super(type, level);
		setPersistenceRequired();
	}

	/** Registers his entity type and attributes (always, whatever the switches, so a saved Beardshy always loads). */
	public static void register() {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Jugcraft.id(ID));
		TYPE = Registry.register(BuiltInRegistries.ENTITY_TYPE, key, EntityType.Builder.<Beardshy>of(Beardshy::new, MobCategory.CREATURE)
				.sized(0.6F, 1.95F).eyeHeight(1.62F).clientTrackingRange(10).noLootTable().build(key));
		FabricDefaultAttributeRegistry.register(TYPE, createAttributes());
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.MOVEMENT_SPEED, SPEED);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new PanicGoal(this, 0.8));
		goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Player.class, STARTLE_RANGE, 0.6, 0.75, Beardshy::startles));
		goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.5));
		// He glances at someone close by now and then, and looks away.
		goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 4.0F, 0.01F));
		goalSelector.addGoal(5, new RandomLookAroundGoal(this));
	}

	/** Whether {@code entity} startles him: a player who is not crouching (spectators are not seen). */
	public static boolean startles(LivingEntity entity) {
		return entity instanceof Player player && !player.isSpectator() && !player.isShiftKeyDown();
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (hand != InteractionHand.MAIN_HAND) {
			return InteractionResult.PASS;
		}
		if (player instanceof ServerPlayer server) {
			speak(server);
		}
		return InteractionResult.SUCCESS;
	}

	/**
	 * Says something to {@code player} if he hasn't just spoken: a startled apology if they are standing, otherwise one of
	 * his lines (never the same one twice running). Returns whether he spoke.
	 */
	public boolean speak(ServerPlayer player) {
		long now = level().getGameTime();
		if (now - lastSpoke < TALK_COOLDOWN) {
			return false;
		}
		lastSpoke = now;
		boolean startled = startles(player);
		say(player, startled ? "startled." + random.nextInt(STARTLED_LINES) : "talk." + nextLine());
		if (!startled) {
			getLookControl().setLookAt(player);
		}
		return true;
	}

	private int nextLine() {
		int line = random.nextInt(TALK_LINES - 1);
		if (line >= lastLine && lastLine >= 0) {
			line++;
		}
		lastLine = line;
		return line;
	}

	private void say(ServerPlayer player, String line) {
		player.sendSystemMessage(Component.translatable("message.jugcraft.townsfolk.says", getDisplayName(),
				Component.translatable("message.jugcraft.beardshy." + line)));
		playSound(SoundEvents.VILLAGER_AMBIENT, getSoundVolume(), getVoicePitch());
	}

	/** Hit, he apologises to whoever hit him (as if it were his fault) and runs; he never fights back. */
	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		boolean hurt = super.hurtServer(level, source, amount);
		if (hurt && isAlive() && source.getEntity() instanceof ServerPlayer player) {
			lastSpoke = level.getGameTime();
			say(player, "hurt." + random.nextInt(HURT_LINES));
		}
		return hurt;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.VILLAGER_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.VILLAGER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.VILLAGER_DEATH;
	}

	/** A soft voice. */
	@Override
	protected float getSoundVolume() {
		return 0.6F;
	}

	/** A little higher than a villager's, and nervous. */
	@Override
	public float getVoicePitch() {
		return 1.2F + random.nextFloat() * 0.15F;
	}

	/** He keeps quiet most of the time. */
	@Override
	public int getAmbientSoundInterval() {
		return 400;
	}
}
