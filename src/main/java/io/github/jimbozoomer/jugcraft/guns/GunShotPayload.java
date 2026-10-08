package io.github.jimbozoomer.jugcraft.guns;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client to server: the player pulled the trigger of the gun in their main hand (once per shot; a held automatic sends
 * one each time its interval comes round). The server decides whether it fires ({@link GunShots#fire}).
 */
public record GunShotPayload() implements CustomPacketPayload {
	public static final GunShotPayload INSTANCE = new GunShotPayload();
	public static final Type<GunShotPayload> TYPE = new Type<>(Jugcraft.id("gun_shot"));
	public static final StreamCodec<RegistryFriendlyByteBuf, GunShotPayload> CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
