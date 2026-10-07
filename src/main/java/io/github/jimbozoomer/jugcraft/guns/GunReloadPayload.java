package io.github.jimbozoomer.jugcraft.guns;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client to server: the player pressed reload with a gun in their main hand ({@link GunShots#reload}). */
public record GunReloadPayload() implements CustomPacketPayload {
	public static final GunReloadPayload INSTANCE = new GunReloadPayload();
	public static final Type<GunReloadPayload> TYPE = new Type<>(Jugcraft.id("gun_reload"));
	public static final StreamCodec<RegistryFriendlyByteBuf, GunReloadPayload> CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
