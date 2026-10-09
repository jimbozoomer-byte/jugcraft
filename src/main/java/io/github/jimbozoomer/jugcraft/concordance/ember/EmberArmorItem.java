package io.github.jimbozoomer.jugcraft.concordance.ember;

import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * A piece of the Pyromancer's set ({@link EmberGear}): leather's protection and half a point of fire Spell Power, worn
 * as the owner's GeckoLib model (assets/jugcraft/geckolib/models/armor/pyromancers.geo.json and textures/armor/
 * pyromancers.png, imported as supplied), which the client's renderer draws through {@link EmberHooks}. It has no
 * animations: GeckoLib poses its bones to the wearer as vanilla armour moves.
 */
public class EmberArmorItem extends Item implements GeoItem {
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	public EmberArmorItem(Properties properties) {
		super(properties);
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
	}

	/** The client's renderer for the set ({@link EmberHooks}); GeckoLib casts it to its GeoRenderProvider. */
	@Override
	public Object getRenderProvider() {
		return EmberHooks.armor.apply(this);
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip,
			TooltipFlag flag) {
		Identifier id = BuiltInRegistries.ITEM.getKey(this);
		tooltip.accept(Component.translatable("tooltip." + id.getNamespace() + "." + id.getPath()).withStyle(ChatFormatting.GRAY));
	}
}
