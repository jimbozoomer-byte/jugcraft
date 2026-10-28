package local.peepo;

import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Private, bounded diagnostic event. Only a registry item ID, never inventory contents or components. */
public record CompanionFeedbackPayload(int entity,UUID uuid,int kind,long started,int item,boolean audible) implements CustomPacketPayload {
    public static final Type<CompanionFeedbackPayload> TYPE=new Type<>(PeepoMod.id("companion_feedback"));
    public static final StreamCodec<RegistryFriendlyByteBuf,CompanionFeedbackPayload> CODEC=StreamCodec.composite(
        ByteBufCodecs.VAR_INT,CompanionFeedbackPayload::entity,UUIDUtil.STREAM_CODEC,CompanionFeedbackPayload::uuid,
        ByteBufCodecs.VAR_INT,CompanionFeedbackPayload::kind,ByteBufCodecs.VAR_LONG,CompanionFeedbackPayload::started,
        ByteBufCodecs.VAR_INT,CompanionFeedbackPayload::item,ByteBufCodecs.BOOL,CompanionFeedbackPayload::audible,CompanionFeedbackPayload::new);
    public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
