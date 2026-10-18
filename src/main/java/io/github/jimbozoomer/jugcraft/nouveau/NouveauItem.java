package io.github.jimbozoomer.jugcraft.nouveau;

import io.github.jimbozoomer.jugcraft.concordance.RateGate;
import io.github.jimbozoomer.jugcraft.concordance.Authority;
import io.github.jimbozoomer.jugcraft.concordance.spirits.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import java.util.function.Consumer;

public final class NouveauItem extends Item {
    public enum Kind { CHARM, WAND, STARBUNCLE }
    private final Kind kind;
    public NouveauItem(Properties properties,Kind kind){super(properties);this.kind=kind;}
    @Override public InteractionResult useOn(UseOnContext ctx){
        if(!(ctx.getLevel() instanceof ServerLevel level)||!(ctx.getPlayer() instanceof ServerPlayer player))return InteractionResult.SUCCESS;
        if(!JugNouveau.enabled()||!player.isAlive()||player.isSpectator()||!RateGate.allow(player,"jug_nouveau",4))return InteractionResult.FAIL;
        var pos=ctx.getClickedPos();var stack=ctx.getItemInHand();
        if(kind==Kind.STARBUNCLE){
            var roster=WorkerRoster.of(level.getServer());var all=roster.workers(player.getUUID());
            if(all.size()>=WorkerRoster.MAX_PER_PLAYER||all.values().stream().filter(e->e.kind().equals("starbuncle")).count()>=Starbuncle.MAX_PER_OWNER)
                return starMessage(player,"limit",false);
            var at=pos.relative(ctx.getClickedFace());
            if(!level.isLoaded(at)||!Authority.mayChange(level,player,at))return starMessage(player,"refused",false);
            var star=JugNouveau.STARBUNCLE.create(level,EntitySpawnReason.SPAWN_ITEM_USE);
            if(star==null)return InteractionResult.FAIL;
            star.setOwner(player.getUUID());star.color(stack.getOrDefault(JugNouveau.CHARM_COLOR,4));
            star.snapTo(at.getX()+.5,at.getY(),at.getZ()+.5,player.getYRot()+180,0);
            if(!level.noCollision(star)||!level.addFreshEntity(star))return starMessage(player,"refused",false);
            roster.note(player.getUUID(),star.getUUID(),star.kind(),star.status(),level.dimension().identifier().toString(),at);
            stack.consume(1,player);return starMessage(player,"summoned",true);
        }
        if(level.getBlockEntity(pos) instanceof StorageLectern lectern){
            if(!lectern.mayUse(player))return message(player,"refused",false);
            if(kind==Kind.WAND){stack.remove(JugNouveau.WAND_WORKER);stack.remove(Workers.PORTER_ROUTE);stack.set(JugNouveau.WAND_LECTERN,GlobalPos.of(level.dimension(),pos));return message(player,"selected",true);}
            if(lectern.bind(player)){stack.consume(1,player);return message(player,"bound",true);}
            return message(player,"full",false);
        }
        if(kind!=Kind.WAND)return InteractionResult.PASS;
        var worker=stack.get(JugNouveau.WAND_WORKER);
        if(worker!=null){
            if(!(level.getEntity(worker) instanceof Starbuncle star)||!star.mayServe(player)||star.distanceToSqr(player)>Starbuncle.RANGE*Starbuncle.RANGE
                ||NouveauStorage.accessible(level,player,pos)==null)return starMessage(player,"refused",false);
            var route=stack.get(Workers.PORTER_ROUTE);String dimension=level.dimension().identifier().toString();
            if(route==null||player.isShiftKeyDown()||!route.dimension().equals(dimension)||!route.source().equals(route.target())){
                stack.set(Workers.PORTER_ROUTE,new ClockworkPorterEntity.Route(pos.immutable(),pos.immutable(),dimension));
                return starMessage(player,"source",true);
            }
            var next=new ClockworkPorterEntity.Route(route.source(),pos.immutable(),dimension);
            if(!star.assign(player,next))return starMessage(player,"route_refused",false);
            stack.set(Workers.PORTER_ROUTE,next);return starMessage(player,"assigned",true);
        }
        var target=stack.get(JugNouveau.WAND_LECTERN);
        if(target==null)return message(player,"select_first",false);
        if(!target.dimension().equals(level.dimension())||!level.isLoaded(target.pos())
            ||!(level.getBlockEntity(target.pos()) instanceof StorageLectern lectern)||!lectern.mayUse(player))return message(player,"refused",false);
        if(player.isShiftKeyDown())return message(player,lectern.unlink(pos)?"unlinked":"refused",true);
        if(!lectern.link(player,pos))return message(player,"refused",false);
        player.sendOverlayMessage(Component.translatable("message.jugcraft.bookwyrm.linked",lectern.links().size(),lectern.capacity()));
        return InteractionResult.SUCCESS;
    }
    @Override public InteractionResult interactLivingEntity(ItemStack stack,Player player,LivingEntity target,InteractionHand hand){
        if(kind!=Kind.WAND||!(target instanceof Starbuncle star))return InteractionResult.PASS;
        if(!(player instanceof ServerPlayer server))return InteractionResult.SUCCESS;
        if(!JugNouveau.enabled()||!star.mayServe(player)||player.distanceToSqr(star)>64
            ||!Authority.mayChange(server.level(),player,star.blockPosition())||!RateGate.allow(server,"jug_nouveau",4))return InteractionResult.FAIL;
        stack.remove(JugNouveau.WAND_LECTERN);stack.remove(Workers.PORTER_ROUTE);stack.set(JugNouveau.WAND_WORKER,star.getUUID());
        return starMessage(server,"selected",true);
    }
    private static InteractionResult starMessage(ServerPlayer player,String id,boolean success){player.sendOverlayMessage(Component.translatable("message.jugcraft.starbuncle."+id));return success?InteractionResult.SUCCESS:InteractionResult.FAIL;}
    private static InteractionResult message(ServerPlayer player,String id,boolean success){player.sendOverlayMessage(Component.translatable("message.jugcraft.bookwyrm."+id));return success?InteractionResult.SUCCESS:InteractionResult.FAIL;}
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,TooltipDisplay display,Consumer<Component> tooltip,TooltipFlag flag){
        tooltip.accept(Component.translatable("tooltip.jugcraft."+switch(kind){case WAND->"dominion_wand";case CHARM->"bookwyrm_charm";case STARBUNCLE->"starbuncle_charm";}).withStyle(ChatFormatting.GRAY));
        if(kind==Kind.WAND&&stack.has(JugNouveau.WAND_WORKER)){
            var route=stack.get(Workers.PORTER_ROUTE);
            tooltip.accept(Component.translatable("tooltip.jugcraft.dominion_wand.starbuncle").withStyle(ChatFormatting.GOLD));
            if(route!=null)tooltip.accept(Component.translatable("tooltip.jugcraft.concordance.porter_key.route",route.source().toShortString(),route.source().equals(route.target())?"?":route.target().toShortString()));
        }
    }
}
