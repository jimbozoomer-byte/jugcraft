package io.github.jimbozoomer.jugcraft.guns;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server to client: the player with this entity id fired or began to reload the gun in their hand, so the clients that
 * see them play its animation (the shooter's own client has already played it). {@code rounds} is how many shells a
 * shell-at-a-time reload loads.
 */
public record GunActionPayload(int entity, int action, int rounds) implements CustomPacketPayload {
	public static final int SHOOT = 0;
	public static final int AIM_SHOOT = 1;
	public static final int RELOAD = 2;
	public static final int STOP = 3;
	public static final Type<GunActionPayload> TYPE = new Type<>(Jugcraft.id("gun_action"));
	public static final StreamCodec<RegistryFriendlyByteBuf, GunActionPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, GunActionPayload::entity,
			ByteBufCodecs.VAR_INT, GunActionPayload::action,
			ByteBufCodecs.VAR_INT, GunActionPayload::rounds,
			GunActionPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
