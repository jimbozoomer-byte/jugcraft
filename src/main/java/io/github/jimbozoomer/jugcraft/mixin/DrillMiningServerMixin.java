package io.github.jimbozoomer.jugcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.jimbozoomer.jugcraft.tools.DrillMiningContext;
import io.github.jimbozoomer.jugcraft.tools.DrillMiningTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Latch START's face, including instant breaks, without replacing native validation or timing. */
@Mixin(ServerPlayerGameMode.class)
public abstract class DrillMiningServerMixin implements DrillMiningContext {
    @Shadow @Final protected ServerPlayer player;
    @Shadow private boolean isDestroyingBlock;
    @Shadow private boolean hasDelayedDestroy;
    @Unique private @Nullable DrillMiningTarget jugcraft$pendingDrill;
    @Unique private @Nullable DrillMiningTarget jugcraft$activeDrill;

    @WrapMethod(method = "handleBlockBreakAction")
    private void jugcraft$startOrCancel(BlockPos pos, Action action, Direction face, int maxHeight, int sequence,
                                      Operation<Void> original) {
        if (action == Action.START_DESTROY_BLOCK) jugcraft$pendingDrill = DrillMiningTarget.start(player, pos, face);
        else if (action == Action.ABORT_DESTROY_BLOCK) jugcraft$pendingDrill = null;
        try {
            original.call(pos, action, face, maxHeight, sequence);
        } finally {
            if (!isDestroyingBlock && !hasDelayedDestroy) jugcraft$pendingDrill = null;
        }
    }

    @WrapMethod(method = "tick")
    private void jugcraft$expireMiningInput(Operation<Void> original) {
        if (jugcraft$pendingDrill != null && !jugcraft$pendingDrill.matches(player, jugcraft$pendingDrill.pos()))
            jugcraft$pendingDrill = null;
        try {
            original.call();
        } finally {
            if (!isDestroyingBlock && !hasDelayedDestroy) jugcraft$pendingDrill = null;
        }
    }

    @WrapMethod(method = "destroyBlock")
    private boolean jugcraft$scopeFaceToRoot(BlockPos pos, Operation<Boolean> original) {
        DrillMiningTarget previous = jugcraft$activeDrill;
        DrillMiningTarget pending = jugcraft$pendingDrill;
        // Only the block the face was latched for uses it up: a lagged, delayed destroy of an earlier block must not
        // clear the face the player has since started on another.
        boolean root = pending != null && pending.matches(player, pos);
        if (root) jugcraft$pendingDrill = null;
        jugcraft$activeDrill = root ? pending : null;
        try {
            return original.call(pos);
        } finally {
            jugcraft$activeDrill = previous;
        }
    }

    @Inject(method = "setLevel", at = @At("HEAD"))
    private void jugcraft$clearOnLevelChange(CallbackInfo ci) {
        jugcraft$pendingDrill = null;
        jugcraft$activeDrill = null;
    }

    @Override public @Nullable Direction jugcraft$drillFace(BlockPos pos) {
        return jugcraft$activeDrill != null && jugcraft$activeDrill.matches(player, pos) ? jugcraft$activeDrill.face() : null;
    }
}
