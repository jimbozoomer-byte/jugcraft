package io.github.jimbozoomer.jugcraft.agriculture;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server to client: open the carving screen for one side of a pumpkin, showing what is already carved
 * there ({@code face}, 16 rows) and whether this server allows free drawing or only starter faces.
 */
public record OpenCarvingPayload(BlockPos pos, Direction side, int[] face, boolean freeDraw) implements CustomPacketPayload {
	public static final Type<OpenCarvingPayload> TYPE = new Type<>(Jugcraft.id("open_carving"));
	public static final StreamCodec<RegistryFriendlyByteBuf, OpenCarvingPayload> CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, OpenCarvingPayload::pos,
			Direction.STREAM_CODEC, OpenCarvingPayload::side,
			PumpkinCarving.FACE_STREAM_CODEC, OpenCarvingPayload::face,
			ByteBufCodecs.BOOL, OpenCarvingPayload::freeDraw,
			OpenCarvingPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
