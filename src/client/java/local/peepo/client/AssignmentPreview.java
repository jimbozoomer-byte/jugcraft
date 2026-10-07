package local.peepo.client;

import java.util.*;
import local.peepo.*;
import io.github.jimbozoomer.jugcraft.machine.MachineBlock;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.*;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import com.mojang.blaze3d.vertex.*;

/** Draw only the selected entity and eight explicit targets, never scan nearby world blocks. */
public final class AssignmentPreview {
    private record Frame(AABB box,int color){}
    public static void initialize(){
        LevelRenderEvents.COLLECT_SUBMITS.register(context->{
            var mc=Minecraft.getInstance();if(mc.player==null || mc.level==null)return;
            var stack=mc.player.getMainHandItem().is(AssignmentTool.ITEM)?mc.player.getMainHandItem():mc.player.getOffhandItem();
            var npc=AssignmentTool.selected(mc.level,stack);if(npc==null)return;
            var camera=context.levelState().cameraRenderState.pos;
            var frames=new ArrayList<Frame>(9);
            frames.add(new Frame(npc.getBoundingBox().inflate(.06).move(-camera.x,-camera.y,-camera.z),0xFF66FF88));
            for(var target:npc.assignments.view()){
                if(target==null || !target.local(mc.level) || !mc.level.hasChunkAt(target.at().pos()) || target.at().pos().distToCenterSqr(mc.player.position())>128*128)continue;
                BlockPos pos=target.at().pos();AABB box=new AABB(pos);boolean present=target.present(mc.level);
                if(present){
                    var state=mc.level.getBlockState(pos);
                    if(state.getBlock() instanceof WheelBlock)for(int i=1;i<4;i++)box=box.minmax(new AABB(WheelBlock.partPos(pos,state.getValue(WheelBlock.FACING),i)));
                    else if(state.getBlock() instanceof BedBlock)box=box.minmax(new AABB(pos.relative(state.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite())));
                    else if(state.getBlock() instanceof MachineBlock machine){var footprint=machine.footprint(state);for(int i=1;i<Math.min(256,footprint.size());i++)box=box.minmax(new AABB(footprint.partPos(pos,state.getValue(MachineBlock.FACING),i)));}
                }
                frames.add(new Frame(box.inflate(.025).move(-camera.x,-camera.y,-camera.z),present?0xFF55FF66:0xFFFF8844));
            }
            context.submitNodeCollector().submitCustomGeometry(context.poseStack(),RenderTypes.lines(),(pose,consumer)->{
                for(var frame:frames)draw(pose,consumer,frame.box,frame.color);
            });
        });
    }
    private static void line(PoseStack.Pose p,VertexConsumer c,double x,double y,double z,double a,double b,double d,int color){
        float nx=(float)(a-x),ny=(float)(b-y),nz=(float)(d-z);float length=(float)Math.sqrt(nx*nx+ny*ny+nz*nz);if(length==0)return;
        nx/=length;ny/=length;nz/=length;
        c.addVertex(p,(float)x,(float)y,(float)z).setColor(color).setNormal(p,nx,ny,nz).setLineWidth(2.5F);
        c.addVertex(p,(float)a,(float)b,(float)d).setColor(color).setNormal(p,nx,ny,nz).setLineWidth(2.5F);
    }
    private static void draw(PoseStack.Pose p,VertexConsumer c,AABB b,int color){
        for(double y:new double[]{b.minY,b.maxY}){
            line(p,c,b.minX,y,b.minZ,b.maxX,y,b.minZ,color);line(p,c,b.maxX,y,b.minZ,b.maxX,y,b.maxZ,color);
            line(p,c,b.maxX,y,b.maxZ,b.minX,y,b.maxZ,color);line(p,c,b.minX,y,b.maxZ,b.minX,y,b.minZ,color);
        }
        for(double x:new double[]{b.minX,b.maxX})for(double z:new double[]{b.minZ,b.maxZ})line(p,c,x,b.minY,z,x,b.maxY,z,color);
    }
}
