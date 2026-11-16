package io.github.jimbozoomer.jugcraft.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.jimbozoomer.jugcraft.tools.DrillMiningContext;
import io.github.jimbozoomer.jugcraft.tools.DrillMiningTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keep the outline on START's face until this native mining action ends. */
@Mixin(MultiPlayerGameMode.class)
public abstract class DrillMiningClientMixin implements DrillMiningContext {
    @Shadow @Final private Minecraft minecraft;
    @Shadow private boolean isDestroying;
    @Shadow private BlockPos destroyBlockPos;
    @Unique private @Nullable DrillMiningTarget jugcraft$drillTarget;

    @WrapMethod(method = "startDestroyBlock")
    private boolean jugcraft$latchFace(BlockPos pos, Direction face, Operation<Boolean> original) {
        if (minecraft.player != null && (!isDestroying || !destroyBlockPos.equals(pos)
            || jugcraft$drillTarget == null || !jugcraft$drillTarget.matches(minecraft.player, pos)))
            jugcraft$drillTarget = DrillMiningTarget.start(minecraft.player, pos, face);
        try {
            return original.call(pos, face);
        } finally {
            if (!isDestroying) jugcraft$drillTarget = null;
        }
    }

    @Inject(method = "stopDestroyBlock", at = @At("HEAD"))
    private void jugcraft$cancelFace(CallbackInfo ci) { jugcraft$drillTarget = null; }

    @Override public @Nullable Direction jugcraft$drillFace(BlockPos pos) {
        if (!isDestroying || minecraft.player == null || jugcraft$drillTarget == null
            || !jugcraft$drillTarget.matches(minecraft.player, pos) || !destroyBlockPos.equals(pos)) return null;
        return jugcraft$drillTarget.face();
    }
}
