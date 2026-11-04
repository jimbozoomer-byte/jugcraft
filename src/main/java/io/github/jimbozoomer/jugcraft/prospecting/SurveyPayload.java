package io.github.jimbozoomer.jugcraft.prospecting;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Only rounded signal information crosses the network, never resource coordinates. */
public record SurveyPayload(List<OreSurvey.Reading> readings, int instrument, int diameter, int coverage,
        BlockPos origin, long sampledAt, String dimension) implements CustomPacketPayload {
    public SurveyPayload(List<OreSurvey.Reading> readings) { this(readings, 0, 3, 9, BlockPos.ZERO, 0, "minecraft:overworld"); }
    public SurveyPayload(List<OreSurvey.Reading> readings, int instrument, int diameter, int coverage, BlockPos origin, long sampledAt) {
        this(readings,instrument,diameter,coverage,origin,sampledAt,"minecraft:overworld");
    }
    public SurveyPayload { readings = List.copyOf(readings); }
    public static final Type<SurveyPayload> TYPE = new Type<>(Jugcraft.id("ore_survey"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SurveyPayload> CODEC = StreamCodec.of((buf, value) -> {
        buf.writeVarInt(value.instrument); buf.writeVarInt(value.diameter); buf.writeVarInt(value.coverage);
        buf.writeBlockPos(value.origin); buf.writeLong(value.sampledAt); buf.writeUtf(value.dimension,128); buf.writeVarInt(value.readings.size());
        for (var r : value.readings) {
            Identifier.STREAM_CODEC.encode(buf, r.icon()); buf.writeVarInt(r.signal()); buf.writeVarInt(r.depth());
            buf.writeUtf(r.label(), 128); buf.writeVarInt(r.distanceLow()); buf.writeVarInt(r.distanceHigh());
            buf.writeUtf(r.direction(), 16); buf.writeVarInt(r.depthLow()); buf.writeVarInt(r.depthHigh());
        }
    }, buf -> {
        int instrument = buf.readVarInt(), diameter = buf.readVarInt(), coverage = buf.readVarInt();
        BlockPos origin = buf.readBlockPos(); long sampledAt = buf.readLong(); String dimension=buf.readUtf(128); int count = buf.readVarInt();
        if (instrument < 0 || instrument > 2 || diameter < 1 || diameter > 7 || coverage < 0 || coverage > 49 || count < 0 || count > 64)
            throw new IllegalArgumentException("Invalid survey bounds");
        var readings = new java.util.ArrayList<OreSurvey.Reading>(count);
        for (int i = 0; i < count; i++) readings.add(new OreSurvey.Reading(Identifier.STREAM_CODEC.decode(buf),
            buf.readVarInt(), buf.readVarInt(), buf.readUtf(128), buf.readVarInt(), buf.readVarInt(), buf.readUtf(16), buf.readVarInt(), buf.readVarInt()));
        return new SurveyPayload(readings, instrument, diameter, coverage, origin, sampledAt, dimension);
    });
    public void save(net.minecraft.world.item.ItemStack stack, net.minecraft.core.RegistryAccess registries) {
        var buffer = new RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), registries);
        try {
            CODEC.encode(buffer, this); byte[] data = new byte[buffer.readableBytes()]; buffer.readBytes(data);
            if (data.length <= 32768) net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                stack, tag -> { tag.putByteArray("jugcraft_survey", data); tag.putInt("jugcraft_survey_version", 1); });
        } finally { buffer.release(); }
    }
    public static SurveyPayload saved(net.minecraft.world.item.ItemStack stack, net.minecraft.core.RegistryAccess registries) {
        var tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        byte[] data = tag.getByteArray("jugcraft_survey").orElse(new byte[0]);
        if (tag.getIntOr("jugcraft_survey_version", 0) != 1 || data.length == 0 || data.length > 32768) return null;
        var buffer = new RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.wrappedBuffer(data), registries);
        try { return CODEC.decode(buffer); } catch (RuntimeException invalid) { return null; } finally { buffer.release(); }
    }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
