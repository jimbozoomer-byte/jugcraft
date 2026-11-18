package io.github.jimbozoomer.jugcraft.town;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LightLayer;

/** Small hand measurements on loaded terrain. No tool modifies or loads the surveyed blocks. */
final class QuarryInstrument extends Item {
    enum Kind { FAULT_SOUNDING_HAMMER, MASONS_SPIRIT_LEVEL, FOLDING_SPAN_RULE, LAMPWATCH_PHOTOMETER }
    private final Kind kind;
    QuarryInstrument(Properties settings,Kind kind) { super(settings); this.kind=kind; }

    @Override public InteractionResult useOn(UseOnContext context) {
        Player player=context.getPlayer();
        if(player==null || player.isSpectator()) return InteractionResult.PASS;
        if(context.getLevel() instanceof ServerLevel server)
            read(server,player,context.getClickedPos(),context.getClickedFace(),context.getItemInHand());
        return InteractionResult.SUCCESS;
    }
    void read(ServerLevel level,Player player,BlockPos start,Direction face,net.minecraft.world.item.ItemStack stack) {
        if (player.getCooldowns().isOnCooldown(stack)) return;
        player.getCooldowns().addCooldown(stack,8);
        if (!level.hasChunkAt(start)) return;
        Component reading;
        switch(kind) {
            case FAULT_SOUNDING_HAMMER -> {
                int depth=0;
                var line=start; var into=face.getOpposite();
                while(depth<8 && level.isInWorldBounds(line) && level.hasChunkAt(line) && solid(level,line)) {
                    depth++; line=line.relative(into);
                }
                reading=depth<8 && level.isInWorldBounds(line) && level.hasChunkAt(line)
                    ? Component.translatable("message.jugcraft.fault_sounding_hammer.open",depth)
                    : Component.translatable("message.jugcraft.fault_sounding_hammer.solid",depth);
                level.playSound(null,start,SoundEvents.STONE_HIT,SoundSource.PLAYERS,.6F,depth<8?.8F:1.25F);
            }
            case MASONS_SPIRIT_LEVEL -> {
                int good=0,raised=0,low=0,missing=0;
                var axis=player.getDirection().getClockWise();
                for(int i=-2;i<=2;i++) {
                    var surface=start.relative(axis,i);
                    if(!level.hasChunkAt(surface)) {missing++; continue;}
                    if(solid(level,surface.above())) raised++;
                    else if(!level.getBlockState(surface).isFaceSturdy(level,surface,Direction.UP)) low++;
                    else good++;
                }
                reading=Component.translatable("message.jugcraft.masons_spirit_level",good,raised,low,missing);
            }
            case FOLDING_SPAN_RULE -> {
                var axis=face.getAxis().isHorizontal()?face.getClockWise():player.getDirection().getClockWise();
                var block=level.getBlockState(start).getBlock();
                int width=1; boolean capped=false;
                for(var walk:new Direction[]{axis,axis.getOpposite()}) {
                    for(int d=1;d<=16;d++) {
                        var next=start.relative(walk,d);
                        if(!level.hasChunkAt(next)) {capped=true;break;}
                        if(!level.getBlockState(next).is(block)) break;
                        if(d==16) capped=true;
                        else width++;
                    }
                }
                reading=Component.translatable(capped?"message.jugcraft.folding_span_rule.at_least":"message.jugcraft.folding_span_rule.exact",width);
            }
            case LAMPWATCH_PHOTOMETER -> {
                var sample=start.relative(face);
                if (!level.isInWorldBounds(sample) || !level.hasChunkAt(sample)) return;
                reading=Component.translatable("message.jugcraft.lampwatch_photometer",
                    level.getBrightness(LightLayer.BLOCK,sample),level.getBrightness(LightLayer.SKY,sample));
            }
            default -> throw new IllegalStateException("Missing instrument "+kind);
        }
        player.sendOverlayMessage(reading);
    }
    private static boolean solid(BlockGetter level,BlockPos pos) {
        return !level.getBlockState(pos).getCollisionShape(level,pos).isEmpty() || !level.getFluidState(pos).isEmpty();
    }
}
