package io.github.jimbozoomer.jugcraft.town;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.finance.LandRegistry;
import java.util.Set;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Flush one-cell landings in a clear vertical shaft. Operated at the player's own feet. */
public final class CounterweightLift extends Block {
    public static final int MAX_SPAN = 32;
    public static CounterweightLift BLOCK;
    private static final Component CONTROLS=Component.translatable("hint.jugcraft.counterweight_lift");
    private CounterweightLift(Properties properties) { super(properties); }

    static void register() {
        var id = Jugcraft.id("counterweight_lift");
        var blockKey = ResourceKey.create(Registries.BLOCK,id);
        BLOCK = Registry.register(BuiltInRegistries.BLOCK,blockKey,new CounterweightLift(
            BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_PLANKS).strength(2.5F).setId(blockKey)));
        var itemKey = ResourceKey.create(Registries.ITEM,id);
        var item = Registry.register(BuiltInRegistries.ITEM,itemKey,new BlockItem(BLOCK,
            new Item.Properties().useBlockDescriptionPrefix().setId(itemKey)
                .component(net.minecraft.core.component.DataComponents.LORE,
                    new net.minecraft.world.item.component.ItemLore(java.util.List.of(CONTROLS)))));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
            .register(output->output.accept(item));
    }

    @Override public void stepOn(Level level,BlockPos pos,BlockState state,net.minecraft.world.entity.Entity entity) {
        if(entity instanceof ServerPlayer p && !p.isSpectator() &&
                (net.minecraft.util.Mth.floor(p.xOld)!=pos.getX() || net.minecraft.util.Mth.floor(p.zOld)!=pos.getZ()))
            p.sendOverlayMessage(CONTROLS);
        super.stepOn(level,pos,state,entity);
    }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        if (player instanceof ServerPlayer rider) ride(rider, pos, rider.isShiftKeyDown());
        return InteractionResult.SUCCESS;
    }

    public static boolean ride(ServerPlayer player, BlockPos start, boolean down) {
        ServerLevel level = player.level();
        if (!level.getBlockState(start).is(BLOCK) || player.isSpectator() || player.isPassenger() || !player.isAlive()
                || !player.blockPosition().equals(start.above()) || Math.abs(player.getY()-start.getY()-1) > .125)
            return tell(player,"Stand on the lift. Use it to go up, or sneak and use it to go down.");
        for (int distance=1; distance<=MAX_SPAN; distance++) {
            var next = start.above(down ? -distance : distance);
            if (level.isOutsideBuildHeight(next)) break;
            var state = level.getBlockState(next);
            if (state.is(BLOCK)) {
                var feet = next.above();
                var room = ApartmentRentals.at(level,feet);
                if (!LandRegistry.mayBuild(player,level,next) || room!=null && !room.active(player.getUUID(),System.currentTimeMillis()))
                    return tell(player,"That landing is private.");
                Vec3 at = Vec3.atBottomCenterOf(feet);
                var box = player.getBoundingBox().move(at.subtract(player.position()));
                if (!level.getBlockState(feet).isAir() || !level.getBlockState(feet.above()).isAir()
                        || !level.getWorldBorder().isWithinBounds(box) || !level.noCollision(player,box)
                        || !level.getEntities(player,box,e->e.isAlive() && !e.isSpectator() && e.isPickable()).isEmpty())
                    return tell(player,"The landing is occupied or blocked.");
                if (!player.teleportTo(level,at.x,at.y,at.z,Set.of(),player.getYRot(),player.getXRot(),true)) return false;
                player.setDeltaMovement(Vec3.ZERO);
                player.resetFallDistance();
                level.playSound(null,start,SoundEvents.CHAIN_PLACE,SoundSource.BLOCKS,.65F,.75F);
                level.playSound(null,next,SoundEvents.PISTON_EXTEND,SoundSource.BLOCKS,.45F,1.4F);
                return true;
            }
            if (!state.isAir()) return tell(player,"The counterweight shaft is obstructed.");
        }
        return tell(player,down ? "This is the lowest stop." : "This is the highest stop.");
    }

    private static boolean tell(ServerPlayer player,String text) {
        player.sendOverlayMessage(Component.literal(text));
        return false;
    }
}
