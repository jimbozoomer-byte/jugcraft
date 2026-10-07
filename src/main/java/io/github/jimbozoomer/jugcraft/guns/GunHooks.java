package io.github.jimbozoomer.jugcraft.guns;

import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.manager.AnimatableManager;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Where the client hands GeckoLib a gun's animation controllers and renderer (client/GunsClient sets both), so the
 * shared {@link GunItem} names no client class. On a dedicated server they stay empty: GeckoLib asks for neither there.
 */
public final class GunHooks {
	public static BiConsumer<GunItem, AnimatableManager.ControllerRegistrar> controllers = (gun, registrar) -> {
	};
	public static BiConsumer<GunItem, Consumer<GeoRenderProvider>> renderer = (gun, consumer) -> {
	};

	private GunHooks() {
	}
}
