package io.github.jimbozoomer.jugcraft.test;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.NoopRenderer;

/** Gives the test-only stub vessel ({@link TestVessels}) an invisible renderer, so a client never meets it unrendered. */
public final class TestVesselsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(TestVessels.STUB, NoopRenderer::new);
	}
}
