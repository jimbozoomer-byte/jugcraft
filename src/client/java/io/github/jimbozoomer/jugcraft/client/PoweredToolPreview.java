package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.tools.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.phys.BlockHitResult;
import java.util.*;

/** Bounded, cosmetic preview of bulk targets. The server still validates every actual break. */
public final class PoweredToolPreview {
    private static java.util.List<BlockPos> active=java.util.List.of();
    private static net.minecraft.world.item.Item activeItem;
    private static net.minecraft.client.multiplayer.ClientLevel activeLevel;
    private static final net.minecraft.client.renderer.rendertype.RenderType OUTLINE=net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucentEmissive(io.github.jimbozoomer.jugcraft.Jugcraft.id("textures/misc/blueprint_ghost.png"));
    private PoweredToolPreview() {}
    public static void clear(){active=java.util.List.of();activeItem=null;activeLevel=null;}
    public static int targetCount(){return active.size();}
    public static java.util.List<BlockPos> targets(){return active;}
    public static void register(){
        net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents.COLLECT_SUBMITS.register(context->{
            var mc=Minecraft.getInstance();var frames=active;
            if(mc.player==null||mc.level!=activeLevel||mc.gui.screen()!=null||mc.player.getMainHandItem().getItem()!=activeItem||frames.isEmpty())return;
            var origin=frames.getFirst();var camera=context.levelState().cameraRenderState.pos;var pose=context.poseStack();
            pose.pushPose();pose.translate(origin.getX()-camera.x,origin.getY()-camera.y,origin.getZ()-camera.z);
            context.submitNodeCollector().submitCustomGeometry(pose,OUTLINE,(matrix,vertices)->{
                for(var block:frames)frame(matrix,vertices,block.getX()-origin.getX(),block.getY()-origin.getY(),block.getZ()-origin.getZ());
            });pose.popPose();
        });
    }
    private static void frame(com.mojang.blaze3d.vertex.PoseStack.Pose matrix,com.mojang.blaze3d.vertex.VertexConsumer vertices,float x,float y,float z){
        float lo=-.006F,hi=1.006F,w=.014F;int color=0xDF45D0FF;
        for(int a=0;a<2;a++)for(int b=0;b<2;b++){
            float ax=x+(a==0?lo:hi-w),ay=y+(b==0?lo:hi-w);
            LinePreview.cube(matrix,vertices,ax,ay,z+lo,ax+w,ay+w,z+hi,color);
            float bx=x+(a==0?lo:hi-w),bz=z+(b==0?lo:hi-w);
            LinePreview.cube(matrix,vertices,bx,y+lo,bz,bx+w,y+hi,bz+w,color);
            float cy=y+(a==0?lo:hi-w),cz=z+(b==0?lo:hi-w);
            LinePreview.cube(matrix,vertices,x+lo,cy,cz,x+hi,cy+w,cz+w,color);
        }
    }
    public static void tick(Minecraft client) {
        if(client.level==null||client.player==null){clear();return;}
        var held = client.player.getMainHandItem();
        boolean drillPreview = held.getItem() instanceof MiningDrillItem || activeItem instanceof MiningDrillItem;
        if(!drillPreview && client.level.getGameTime()%10!=0)return;
        clear();
        if(client.gui.screen()!=null||!(client.hitResult instanceof BlockHitResult hit))return;
        var stack = client.player.getMainHandItem();
        String id=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        boolean laser=id.equals("jugcraft:jimta_mining_laser"), surface=id.equals("jugcraft:jimta_obscurator"), paint=id.equals("jugcraft:jimta_color_applicator");
        if (!(stack.getItem() instanceof PoweredToolItem) && !laser && !surface && !paint || Chargeable.energy(stack) <= 0) return;
        BlockHitResult aimed=hit;int laserMode=Math.floorMod(stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag().getIntOr("LaserMode",0),7);
        if(laser){var distant=client.player.pick(laserMode==1?4:laserMode==2?48:24,1,false);if(distant instanceof BlockHitResult block)aimed=block;}
        if(aimed.getType()!=net.minecraft.world.phys.HitResult.Type.BLOCK)return;
        BlockPos root = aimed.getBlockPos(); var original = client.level.getBlockState(root);
        Set<BlockPos> positions = new LinkedHashSet<>(); positions.add(root);
        if(laser){
            var into=laserMode==3?client.player.getDirection():aimed.getDirection().getOpposite();
            for(var pos:MiningLaserPattern.targets(root,into,aimed.getDirection(),laserMode))if(client.level.hasChunkAt(pos)&&!client.level.getBlockState(pos).isAir()&&!client.level.getBlockState(pos).hasBlockEntity())positions.add(pos);
            client.gui.hud.setOverlayMessage(net.minecraft.network.chat.Component.literal("Laser preview: "+positions.size()+" candidate blocks"),false);
        }else if(surface){
            var sample=stack.get(io.github.jimbozoomer.jugcraft.jimtafacts.network.DigitalFacades.MATERIAL);
            if(sample!=null)client.gui.hud.setOverlayMessage(net.minecraft.network.chat.Component.literal("Surface preview: ").append(sample.getBlock().getName()).append(" | 5,000 JE/application"),false);
        }else if(paint){
            var material=client.player.getOffhandItem();if(material.isEmpty())material=stack.getOrDefault(io.github.jimbozoomer.jugcraft.jimtafacts.network.DigitalEquipment.AMMO,net.minecraft.world.item.ItemStack.EMPTY);
            if(!material.isEmpty())client.gui.hud.setOverlayMessage(net.minecraft.network.chat.Component.literal("Paint preview: ").append(material.getHoverName()).append(" | 100 JE/application"),false);
        }else if (!client.player.isShiftKeyDown() && stack.getItem() instanceof MiningDrillItem
            && (MiningDrillItem.mode(stack) == MiningDrillItem.AREA || MiningDrillItem.mode(stack) == MiningDrillItem.AREA_LARGE)) {
            var face = client.gameMode == null ? null : ((DrillMiningContext) client.gameMode).jugcraft$drillFace(root);
            if (face == null) face = aimed.getDirection();
            for (BlockPos pos : MiningDrillItem.areaTargets(stack, root, face)) {
                var state = client.level.getBlockState(pos);
                if (!state.hasBlockEntity() && state.getDestroySpeed(client.level,pos) >= 0 && state.getDestroySpeed(client.level,pos) <= original.getDestroySpeed(client.level,root)
                    && stack.isCorrectToolForDrops(state)) positions.add(pos);
            }
        } else if (!client.player.isShiftKeyDown() && stack.getItem() instanceof MiningDrillItem && MiningDrillItem.mode(stack)==MiningDrillItem.VEIN
            && original.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK,net.minecraft.resources.Identifier.fromNamespaceAndPath("c","ores")))) {
            var queue=new ArrayDeque<BlockPos>();var seen=new HashSet<BlockPos>();queue.add(root);seen.add(root);int limit=MiningDrillItem.veinLimit(stack);
            while(!queue.isEmpty()&&positions.size()<limit){
                var pos=queue.poll();
                for(var next:BlockPos.betweenClosed(pos.offset(-1,-1,-1),pos.offset(1,1,1))){
                    if(seen.size()>limit*8||positions.size()>=limit)break;
                    if(client.level.hasChunkAt(next)&&seen.add(next.immutable())&&client.level.getBlockState(next).is(original.getBlock())&&positions.add(next.immutable()))queue.add(next.immutable());
                }
            }
        } else if (!client.player.isShiftKeyDown() && stack.getItem() instanceof ChainsawItem && original.is(BlockTags.LOGS)
            && EquipmentSettings.number(stack,"saw_mode",0) == 0) {
            positions.addAll(ChainsawTree.targets(client.level,root,original));
        }
        active=positions.stream().limit(64).toList();activeItem=stack.getItem();activeLevel=client.level;
    }
}
