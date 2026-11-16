package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.pharmacology.*;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.fabricmc.fabric.api.client.rendering.v1.hud.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/** Read-only personal strain HUD. Cosmetic display switches cannot affect server-owned penalties. */
public final class StrainClient {
	private static StrainStatus state=StrainStatus.EMPTY;
	private StrainClient() {}
	public static StrainStatus state() { return state; }
	public static void register() {
		ClientPlayNetworking.registerGlobalReceiver(StrainStatus.TYPE,(packet,context)->{StrainPillHud.statusChanged(state,packet);state=packet;});
		ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->{state=StrainStatus.EMPTY;StrainPillHud.reset();});
		HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR,Jugcraft.id("pharmacy_strain"),(g,delta)->StrainPillHud.extract(g,state));
		ItemTooltipCallback.EVENT.register((stack,context,flag,lines)-> {
			if(!(stack.getItem() instanceof DrugDrinkItem item) || Minecraft.getInstance().player==null)return;
			int extra=DrugStrain.surcharge(item.profile().family(),state.combinations());
			int total=DrugStrain.add(state.tenths(),item.profile().strain()+extra);
			lines.add(Component.translatable("tooltip.jugcraft.strain_forecast",item.profile().strain()+extra,DrugStrain.format(total),Component.translatable(DrugStrain.band(total).key())).withStyle(ChatFormatting.GRAY));
			if(extra>0) lines.add(Component.translatable("hud.jugcraft.strain_combination",extra).withStyle(ChatFormatting.GOLD));
		});
	}
}
