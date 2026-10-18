package io.github.jimbozoomer.jugcraft.nouveau;

import io.github.jimbozoomer.jugcraft.concordance.RateGate;
import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import java.util.function.Consumer;

public final class NouveauItem extends Item {
    public enum Kind { CHARM, WAND }
    private final Kind kind;
    public NouveauItem(Properties properties,Kind kind){super(properties);this.kind=kind;}
    @Override public InteractionResult useOn(UseOnContext ctx){
        if(!(ctx.getLevel() instanceof ServerLevel level)||!(ctx.getPlayer() instanceof ServerPlayer player))return InteractionResult.SUCCESS;
        if(!JugNouveau.enabled()||!player.isAlive()||player.isSpectator()||!RateGate.allow(player,"jug_nouveau",4))return InteractionResult.FAIL;
        var pos=ctx.getClickedPos();var stack=ctx.getItemInHand();
        if(level.getBlockEntity(pos) instanceof StorageLectern lectern){
            if(!lectern.mayUse(player))return message(player,"refused",false);
            if(kind==Kind.WAND){stack.set(JugNouveau.WAND_LECTERN,GlobalPos.of(level.dimension(),pos));return message(player,"selected",true);}
            if(lectern.bind(player)){stack.consume(1,player);return message(player,"bound",true);}
            return message(player,"full",false);
        }
        if(kind!=Kind.WAND)return InteractionResult.PASS;
        var target=stack.get(JugNouveau.WAND_LECTERN);
        if(target==null)return message(player,"select_first",false);
        if(!target.dimension().equals(level.dimension())||!level.isLoaded(target.pos())
            ||!(level.getBlockEntity(target.pos()) instanceof StorageLectern lectern)||!lectern.mayUse(player))return message(player,"refused",false);
        if(player.isShiftKeyDown())return message(player,lectern.unlink(pos)?"unlinked":"refused",true);
        if(!lectern.link(player,pos))return message(player,"refused",false);
        player.sendOverlayMessage(Component.translatable("message.jugcraft.bookwyrm.linked",lectern.links().size(),lectern.capacity()));
        return InteractionResult.SUCCESS;
    }
    private static InteractionResult message(ServerPlayer player,String id,boolean success){player.sendOverlayMessage(Component.translatable("message.jugcraft.bookwyrm."+id));return success?InteractionResult.SUCCESS:InteractionResult.FAIL;}
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,TooltipDisplay display,Consumer<Component> tooltip,TooltipFlag flag){
        tooltip.accept(Component.translatable("tooltip.jugcraft."+(kind==Kind.WAND?"dominion_wand":"bookwyrm_charm")).withStyle(ChatFormatting.GRAY));
    }
}
