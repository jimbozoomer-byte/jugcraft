package io.github.jimbozoomer.jugcraft.pegasus;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client to server: the Pegasus pilot's flight keys (docs/features/pegasus.md). {@code vehicleId} is the entity the
 * client believes it pilots; {@code input} holds {@link PegasusFlight#INPUT_ASCEND} and
 * {@link PegasusFlight#INPUT_DESCEND} and nothing else; {@code sequence} counts up with every packet the client sends.
 * The server ({@link JugcraftPegasus#receive}) drops a packet that is malformed, out of order, for another entity or
 * from anyone but that entity's controlling passenger, and stops listening {@value PegasusFlight#INPUT_TIMEOUT_TICKS}
 * ticks after the last good one. Three variable-length integers: at most 15 bytes.
 */
public record PegasusInputPayload(int vehicleId, int input, int sequence) implements CustomPacketPayload {
	public static final Type<PegasusInputPayload> TYPE = new Type<>(Jugcraft.id("pegasus_input"));
	public static final StreamCodec<RegistryFriendlyByteBuf, PegasusInputPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, PegasusInputPayload::vehicleId,
			ByteBufCodecs.VAR_INT, PegasusInputPayload::input,
			ByteBufCodecs.VAR_INT, PegasusInputPayload::sequence,
			PegasusInputPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
