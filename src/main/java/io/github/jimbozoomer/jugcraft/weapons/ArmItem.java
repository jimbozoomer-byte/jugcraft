package io.github.jimbozoomer.jugcraft.weapons;

import io.github.jimbozoomer.jugcraft.town.TownProtection;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;

/**
 * An arm (batches 42 and 45, {@link JugcraftArms}): its numbers and most traits are item components, with a grey line
 * saying what its kind does, from {@code tooltip.jugcraft.arms.<kind>}. An Arms II kind's {@link JugcraftArms.Trait}
 * is worked here, on the server: a bonus to the blow (backstab, saddle, armor pierce, riders), a daze on a hit, or the
 * scythe's reaping. Chopping is the axe's own tool component.
 */
public class ArmItem extends Item {
	private final String kind;
	private final JugcraftArms.Trait trait;

	public ArmItem(String kind, Properties properties) {
		super(properties);
		this.kind = kind;
		this.trait = JugcraftArms.TRAITS.get(kind);
	}

	/**
	 * The kind of arm: longsword, greatsword, rapier, flanged_mace, war_hammer, glaive, halberd, spear, lance, dagger,
	 * sabre, estoc, battle_axe, flail, scythe, quarterstaff or pike.
	 */
	public String kind() {
		return kind;
	}

	/** The kind's Arms II trait, or null. */
	public JugcraftArms.Trait trait() {
		return trait;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip,
			TooltipFlag flag) {
		tooltip.accept(Component.translatable("tooltip.jugcraft.arms." + kind).withStyle(ChatFormatting.GRAY));
	}

	@Override
	public float getAttackDamageBonus(Entity target, float damage, DamageSource source) {
		return super.getAttackDamageBonus(target, damage, source) + traitBonus(trait, target, damage, source.getEntity());
	}

	/** What a trait adds to a blow of `damage` from `attacker` (null if none) on `target`. */
	static float traitBonus(JugcraftArms.Trait trait, Entity target, float damage, Entity attacker) {
		if (trait == null) {
			return 0.0F;
		}
		return switch (trait) {
			case BACKSTAB -> attacker != null && target instanceof LivingEntity living && behind(living, attacker)
					? damage * JugcraftArms.BACKSTAB : 0.0F;
			case SADDLE -> attacker != null && attacker.isPassenger() ? JugcraftArms.SADDLE : 0.0F;
			case ARMOR_PIERCE -> target instanceof LivingEntity living
					? Math.min(JugcraftArms.ARMOR_PIERCE_MAX, JugcraftArms.ARMOR_PIERCE * living.getArmorValue()) : 0.0F;
			case RIDERS -> target.isPassenger() || target.isVehicle() ? damage * JugcraftArms.RIDERS : 0.0F;
			default -> 0.0F;
		};
	}

	/** Whether `attacker` stands within BACKSTAB_ANGLE degrees of straight behind `target`'s body. */
	static boolean behind(LivingEntity target, Entity attacker) {
		double dx = attacker.getX() - target.getX();
		double dz = attacker.getZ() - target.getZ();
		double length = Math.sqrt(dx * dx + dz * dz);
		if (length < 1.0E-4) {
			return false;
		}
		// The body faces (-sin yaw, cos yaw); straight behind is the opposite way.
		double yaw = Math.toRadians(target.yBodyRot);
		double facing = (-Math.sin(yaw) * dx + Math.cos(yaw) * dz) / length;
		return facing <= -Math.cos(Math.toRadians(JugcraftArms.BACKSTAB_ANGLE));
	}

	@Override
	public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		super.hurtEnemy(stack, target, attacker);
		if (trait == JugcraftArms.Trait.DAZE && !target.level().isClientSide()) {
			target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, JugcraftArms.DAZE_TICKS, JugcraftArms.DAZE_AMPLIFIER), attacker);
		}
	}

	/**
	 * The scythe, used on a crop (or the farmland under it): every ripe crop within REAP_RADIUS is harvested, its drops
	 * left on the ground less one seed, which replants it. Each costs REAP_WEAR durability. Only crops the player may
	 * change are touched.
	 */
	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (trait != JugcraftArms.Trait.REAP) {
			return super.useOn(context);
		}
		Level level = context.getLevel();
		BlockPos center = context.getClickedPos();
		if (!(level.getBlockState(center).getBlock() instanceof CropBlock)) {
			center = center.above();
			if (!(level.getBlockState(center).getBlock() instanceof CropBlock)) {
				return InteractionResult.PASS;
			}
		}
		Player player = context.getPlayer();
		ItemStack stack = context.getItemInHand();
		int r = JugcraftArms.REAP_RADIUS;
		List<BlockPos> ripe = new ArrayList<>();
		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-r, 0, -r), center.offset(r, 0, r))) {
			BlockState state = level.getBlockState(pos);
			if (state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state) && (player == null
					|| player.mayUseItemAt(pos, context.getClickedFace(), stack) && level.mayInteract(player, pos)
							&& !TownProtection.denies(player, level, pos))) {
				ripe.add(pos.immutable());
			}
		}
		if (ripe.isEmpty()) {
			return InteractionResult.PASS;
		}
		if (level instanceof ServerLevel server) {
			for (BlockPos pos : ripe) {
				reap(server, pos, player, stack);
			}
			if (player != null) {
				stack.hurtAndBreak(ripe.size() * JugcraftArms.REAP_WEAR, player, context.getHand());
			}
		}
		return InteractionResult.SUCCESS;
	}

	/** Harvests one ripe crop: its drops, less one seed, which replants it (or, with no seed, the crop is gone). */
	static void reap(ServerLevel level, BlockPos pos, Entity reaper, ItemStack tool) {
		BlockState state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof CropBlock crop)) {
			return;
		}
		List<ItemStack> drops = new ArrayList<>(Block.getDrops(state, level, pos, null, reaper, tool));
		Item seed = crop.asItem();
		boolean replant = false;
		for (ItemStack drop : drops) {
			if (!replant && drop.is(seed)) {
				drop.shrink(1);
				replant = true;
			}
		}
		level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
		level.setBlock(pos, replant ? crop.getStateForAge(0) : Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
		for (ItemStack drop : drops) {
			if (!drop.isEmpty()) {
				Block.popResource(level, pos, drop);
			}
		}
	}
}
