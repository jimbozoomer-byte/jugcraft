package io.github.jimbozoomer.jugcraft.mixin;
import io.github.jimbozoomer.jugcraft.nouveau.NouveauScrying;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class NouveauScryInputMixin {
    @Shadow public ServerPlayer player;
    @Inject(method={"handleMovePlayer","handleUseItemOn","handleUseItem","handleInteract","handleAttack","handlePunch","handlePlayerInput"},at=@At("HEAD"),cancellable=true)
    private void nouveau$viewOnly(CallbackInfo ci){if(NouveauScrying.active(player))ci.cancel();}
    @Inject(method={"handlePlayerAction","handleContainerClick","handleSetCreativeModeSlot","handlePlaceRecipe","handlePickItemFromBlock","handlePickItemFromEntity","handleBundleItemSelectedPacket","handleContainerSlotStateChanged","handleContainerButtonClick"},at=@At("HEAD"),cancellable=true)
    private void nouveau$viewInventory(CallbackInfo ci){
        if(!NouveauScrying.active(player))return;
        var owner=player;
        // These HEAD guards can run before vanilla schedules a packet onto the server thread.
        owner.level().getServer().execute(()->{
            if(NouveauScrying.active(owner)&&io.github.jimbozoomer.jugcraft.concordance.RateGate.allow(owner,"remote_view_inventory",2)){
                owner.inventoryMenu.sendAllDataToRemote();
                if(owner.containerMenu!=owner.inventoryMenu)owner.containerMenu.sendAllDataToRemote();
            }
        });
        ci.cancel();
    }
}
