package io.github.jimbozoomer.jugcraft.agriculture;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client to server: the epitaph a player wrote for the headstone they opened with the Stonemason's Chisel (its part 0 at
 * {@code pos}). At most {@value Epitaph#LINES} lines, each short (the codec refuses more); the server checks the rest
 * ({@link Epitaphs#engrave}).
 */
public record EngravePayload(BlockPos pos, List<String> lines) implements CustomPacketPayload {
	public static final Type<EngravePayload> TYPE = new Type<>(Jugcraft.id("engrave_epitaph"));
	public static final StreamCodec<RegistryFriendlyByteBuf, EngravePayload> CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, EngravePayload::pos,
			Epitaph.LINES_STREAM_CODEC, EngravePayload::lines,
			EngravePayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
