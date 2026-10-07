package io.github.jimbozoomer.jugcraft.guns;

import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * A gun ({@link JugcraftGuns}). Held right click aims down the sights (using the item, so the server and the other
 * players know); the trigger and reload come as payloads ({@link GunShots}). GeckoLib draws it from
 * assets/jugcraft/geckolib (models and the owner's animations, tools/guns.py) through the client's renderer.
 */
public class GunItem extends Item implements GeoItem {
	/** The ammunition gauge's colour: brass. */
	private static final int BAR_COLOR = 0xFFD8A848;
	private final String name;
	private final GunSpec spec;
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	public GunItem(String name, GunSpec spec, Properties properties) {
		super(properties);
		this.name = name;
		this.spec = spec;
	}

	public String name() {
		return name;
	}

	public GunSpec spec() {
		return spec;
	}

	public static int loaded(ItemStack stack) {
		return stack.getOrDefault(JugcraftGuns.LOADED, 0);
	}

	public static void setLoaded(ItemStack stack, int rounds) {
		int capacity = stack.getItem() instanceof GunItem gun ? gun.spec.capacity() : 0;
		stack.set(JugcraftGuns.LOADED, Math.max(0, Math.min(capacity, rounds)));
	}

	/** Aims down the sights while held (main hand only: the trigger is the attack button). */
	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (hand != InteractionHand.MAIN_HAND) {
			return InteractionResult.PASS;
		}
		player.startUsingItem(hand);
		return InteractionResult.CONSUME;
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.NONE;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return 72000;
	}

	/** Whether this player is aiming down the sights of this gun. */
	public static boolean aiming(LivingEntity entity, ItemStack stack) {
		return entity.isUsingItem() && entity.getUseItem() == stack;
	}

	/** Gives the stack its GeckoLib animation id (on the server, once), so its animations survive the stack's updates. */
	@Override
	public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
		GeoItem.getOrAssignId(stack, level);
	}

	/** Firing and loading change the stack; the gun should not dip out of view and back each time. */
	@Override
	public boolean allowComponentsUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack) {
		return !(oldStack.is(this) && newStack.is(this));
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return true;
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		return Math.round(13.0F * loaded(stack) / spec.capacity());
	}

	@Override
	public int getBarColor(ItemStack stack) {
		return BAR_COLOR;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip,
			TooltipFlag flag) {
		tooltip.accept(Component.translatable("tooltip.jugcraft.guns.ammo", loaded(stack), spec.capacity())
				.withStyle(ChatFormatting.GOLD));
		tooltip.accept(Component.translatable("tooltip.jugcraft.guns.stats", String.format(Locale.ROOT, "%.1f", spec.damage()),
				spec.pellets(), String.format(Locale.ROOT, "%.1f", 20.0F / spec.interval()), spec.range())
				.withStyle(ChatFormatting.BLUE));
		tooltip.accept(Component.translatable("tooltip.jugcraft.guns." + name).withStyle(ChatFormatting.GRAY));
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		GunHooks.controllers.accept(this, controllers);
	}

	/** The client's renderer for this gun ({@link GunHooks}); GeckoLib casts it to its GeoRenderProvider. */
	@Override
	public Object getRenderProvider() {
		return GunHooks.renderer.apply(this);
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}
}
