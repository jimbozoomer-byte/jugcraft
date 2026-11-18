package io.github.jimbozoomer.jugcraft.lineage;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server to client: the player with entity id {@link #entity} has just read an animal with the Astrawand, so clients that
 * see them play the conducting flourish. Presentation only: the reading itself was the server's.
 */
public record AstrawandGesturePayload(int entity) implements CustomPacketPayload {
	public static final Type<AstrawandGesturePayload> TYPE = new Type<>(Jugcraft.id("astrawand_gesture"));
	public static final StreamCodec<RegistryFriendlyByteBuf, AstrawandGesturePayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, AstrawandGesturePayload::entity, AstrawandGesturePayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
