package io.github.jimbozoomer.jugcraft.pharmacology;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.util.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/** Private authored-site information, never a client query. Mode 1/2/3 is sight; 4 is recorded-site resonance. */
public record DreamCue(int mode, boolean folio, List<BlockPos> sites) implements CustomPacketPayload {
	public static final DreamCue EMPTY=new DreamCue(0,false,List.of());
	public static final Type<DreamCue> TYPE=new Type<>(Jugcraft.id("pharmacy_dream_cues"));
	public DreamCue {
		if(mode<0 || mode>4 || sites.size()>4 || mode==4 && sites.size()>1)throw new IllegalArgumentException("Invalid dream cue");
		sites=sites.stream().map(BlockPos::immutable).toList();
	}
	public static final StreamCodec<RegistryFriendlyByteBuf,DreamCue> CODEC=new StreamCodec<>() {
		public DreamCue decode(RegistryFriendlyByteBuf b) {
			int mode=b.readUnsignedByte();boolean folio=b.readBoolean();int count=b.readVarInt();
			if(count<0 || count>4)throw new IllegalArgumentException("Too many dream sites");
			var sites=new ArrayList<BlockPos>(count);for(int i=0;i<count;i++)sites.add(b.readBlockPos());
			return new DreamCue(mode,folio,sites);
		}
		public void encode(RegistryFriendlyByteBuf b,DreamCue p) {b.writeByte(p.mode);b.writeBoolean(p.folio);b.writeVarInt(p.sites.size());for(var pos:p.sites)b.writeBlockPos(pos);}
	};
	private static final Map<ServerPlayer,Boolean> ACTIVE=Collections.synchronizedMap(new WeakHashMap<>());
	public static void register() {
		PayloadTypeRegistry.clientboundPlay().register(TYPE,CODEC);
		ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->ACTIVE.remove(handler.getPlayer()));
		ServerLifecycleEvents.SERVER_STOPPED.register(server->ACTIVE.clear());
	}
	/** Active heartbeats are bounded to the existing one-second perception clock; idle players get no stream. */
	static void sync(ServerPlayer player,DreamCue cue) {
		if(!ServerPlayNetworking.canSend(player,TYPE))return;
		if(cue.mode()>0 || ACTIVE.getOrDefault(player,false))ServerPlayNetworking.send(player,cue);
		if(cue.mode()>0)ACTIVE.put(player,true);else ACTIVE.remove(player);
	}
	@Override public Type<? extends CustomPacketPayload> type() {return TYPE;}
}
