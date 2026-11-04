package io.github.jimbozoomer.jugcraft.pharmacology;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.util.Map;
import java.util.Collections;
import java.util.WeakHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/** Private server-authored HUD data. No client request can change drug accounting. */
public record StrainStatus(int tenths, int combinations, int nextCost, int extraCost, boolean dosing, int visuals, int rushCue) implements CustomPacketPayload {
	public static final Type<StrainStatus> TYPE=new Type<>(Jugcraft.id("pharmacy_strain"));
	public static final StrainStatus EMPTY=new StrainStatus(0,0,0,0,false,0,0);
	public static final StreamCodec<RegistryFriendlyByteBuf,StrainStatus> CODEC=new StreamCodec<>() {
		public StrainStatus decode(RegistryFriendlyByteBuf b) { return new StrainStatus(Math.max(0,b.readVarInt()),b.readUnsignedByte()&7,Math.clamp(b.readVarInt(),0,125),Math.clamp(b.readVarInt(),0,25),b.readBoolean(),b.readVarInt()&DrugPresentation.MASK,b.readUnsignedByte()); }
		public void encode(RegistryFriendlyByteBuf b,StrainStatus p) { b.writeVarInt(p.tenths);b.writeByte(p.combinations);b.writeVarInt(p.nextCost);b.writeVarInt(p.extraCost);b.writeBoolean(p.dosing);b.writeVarInt(p.visuals);b.writeByte(p.rushCue); }
	};
	private static final Map<ServerPlayer,StrainStatus> SENT=Collections.synchronizedMap(new WeakHashMap<>());
	private static final Map<ServerPlayer,Integer> RUSHES=Collections.synchronizedMap(new WeakHashMap<>());
	public static void register() {
		PayloadTypeRegistry.clientboundPlay().register(TYPE,CODEC);
		ServerPlayConnectionEvents.DISCONNECT.register((handler,server)-> {SENT.remove(handler.getPlayer());RUSHES.remove(handler.getPlayer());});
		ServerLifecycleEvents.SERVER_STOPPED.register(server-> {SENT.clear();RUSHES.clear();});
	}
	/** Cosmetic cue only: native timer corrections and unrelated doses cannot replay a settled rush. */
	static void dosed(ServerPlayer player,DrugProfile profile) {
		if(profile==SterilePharmacy.ADRENALINE)RUSHES.put(player,(RUSHES.getOrDefault(player,0)+1)&255);
	}
	public static void sync(ServerPlayer player) {
		if(!ServerPlayNetworking.canSend(player,TYPE)) return;
		var state=Pharmacology.state(player); int mask=Pharmacology.combinations(player);
		var held=Pharmacology.heldProfile(player); int extra=held==null?0:DrugStrain.surcharge(held.family(),mask);
		int visuals=0;
		for(var dose:state.doses()) { var profile=Pharmacology.profile(dose.drug()); if(Pharmacology.active(player,dose,profile))visuals|=DrugPresentation.flag(profile); }
		var status=new StrainStatus(state.strainTenths(),mask,held==null?0:held.strain()+extra,extra,Pharmacology.dosing(player),visuals,RUSHES.getOrDefault(player,0));
		var previous=SENT.get(player);
		if(!status.equals(previous)) {
			ServerPlayNetworking.send(player,status); SENT.put(player,status);
		}
	}
	@Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
