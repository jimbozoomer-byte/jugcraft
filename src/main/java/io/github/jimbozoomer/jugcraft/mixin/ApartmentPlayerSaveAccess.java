package io.github.jimbozoomer.jugcraft.mixin;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Save the one player receiving a reclaim delivery before acknowledging it to the source chunk. */
@Mixin(PlayerList.class)
public interface ApartmentPlayerSaveAccess {
    @Invoker("save") void jugcraft$saveApartmentRecipient(ServerPlayer player);
}

