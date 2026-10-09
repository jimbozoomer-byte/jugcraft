package io.github.jimbozoomer.jugcraft.client;

import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.renderer.GeoArmorRenderer;
import io.github.jimbozoomer.jugcraft.client.ember.EmberArmorRenderer;
import io.github.jimbozoomer.jugcraft.concordance.ember.EmberArmorItem;
import io.github.jimbozoomer.jugcraft.concordance.ember.EmberHooks;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Ember on the client: the Pyromancer's set's GeckoLib renderer, handed to GeckoLib through {@link EmberHooks} (as the
 * guns' are, GunsClient). One renderer draws all four pieces; it is made the first time a piece is drawn.
 */
final class EmberClient {
	/** Each piece's GeoRenderProvider, made once. */
	private static final Map<EmberArmorItem, GeoRenderProvider> PROVIDERS = new HashMap<>();
	private static @Nullable EmberArmorRenderer renderer;

	private EmberClient() {
	}

	static void register() {
		EmberHooks.armor = item -> PROVIDERS.computeIfAbsent(item, key -> new GeoRenderProvider() {
			@Override
			public GeoArmorRenderer<?, ?> getGeoArmorRenderer(ItemStack stack, EquipmentSlot slot) {
				if (renderer == null) {
					renderer = new EmberArmorRenderer();
				}
				return renderer;
			}
		});
	}
}
