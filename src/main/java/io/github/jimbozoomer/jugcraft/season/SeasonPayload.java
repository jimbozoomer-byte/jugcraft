package io.github.jimbozoomer.jugcraft.season;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server to client: today's season day (see {@link SeasonCalendar}; 0 means off), for the seasonal colours. */
public record SeasonPayload(int day) implements CustomPacketPayload {
	public static final Type<SeasonPayload> TYPE = new Type<>(Jugcraft.id("season"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SeasonPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, SeasonPayload::day,
			SeasonPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
