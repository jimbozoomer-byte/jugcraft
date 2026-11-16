package io.github.jimbozoomer.jugcraft.mixin;

import local.peepo.CompanionMusic;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Observe native playback; never change songs, their timing, inventory or sound packets. */
@Mixin(JukeboxBlockEntity.class)
public abstract class CompanionJukeboxMixin {
    @Unique private boolean peepo$observedMusic;
    @Inject(method="onSongChanged",at=@At("TAIL"))
    private void peepo$songChanged(CallbackInfo ci){CompanionMusic.changed((JukeboxBlockEntity)(Object)this);peepo$observedMusic=true;}
    @Inject(method="tick",at=@At("HEAD"))
    private static void peepo$loadedSong(Level level,BlockPos pos,BlockState state,JukeboxBlockEntity box,CallbackInfo ci){
        if(level.isClientSide())return;var hook=(CompanionJukeboxMixin)(Object)box;
        if(!hook.peepo$observedMusic){hook.peepo$observedMusic=true;CompanionMusic.changed(box);}
    }
    @Inject(method="setRemoved",at=@At("TAIL"))
    private void peepo$removed(CallbackInfo ci){CompanionMusic.removed((JukeboxBlockEntity)(Object)this);peepo$observedMusic=false;}
}
