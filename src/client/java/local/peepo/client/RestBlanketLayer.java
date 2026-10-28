package local.peepo.client;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;

/** Native bed fabric with a hand-held fold, side drapes and a covered foot end. */
final class RestBlanketLayer extends RenderLayer<PeepoState,PeepoModel>{
    private static final Identifier[] FABRIC=java.util.Arrays.stream(DyeColor.values()).map(c->Identifier.withDefaultNamespace("textures/block/"+c.getName()+"_bed_foot_up.png")).toArray(Identifier[]::new);
    RestBlanketLayer(PeepoRenderer renderer){super(renderer);}
    @Override public void submit(PoseStack pose,SubmitNodeCollector collector,int light,PeepoState s,float yaw,float pitch){
        var r=s.rest;if(s.isInvisible || !r.active || !r.clip.bed() || r.climbing || r.lie<.9F || r.color<0)return;
        var edge=RestPose.blanketEdge(s,getParentModel());
        float width=s.pumpkin?4.5F:3.8F,top=edge.y,hem=top+.40F,foot=24.5F;
        // Keep fabric outside the body/costume, including the small breathing offset.
        float z=Math.min(edge.z-.06F,(s.pumpkin?-3.95F:-2.85F)+r.breathe),fold=z-.12F;
        // The skirt stops above the mattress while enclosing the heels and toes.
        float skirt=s.pumpkin?3.75F:2.30F;
        var texture=FABRIC[Math.clamp(r.color,0,FABRIC.length-1)];
        collector.submitCustomGeometry(pose,RenderTypes.entityCutout(texture),(matrix,c)->{
            // Adjacent panels meet at their edges; the hem no longer overlays a second sheet.
            quad(matrix,c,-width,top,fold,width,top,fold,width,hem,z,-width,hem,z,light,0,.05F,1,.20F);
            quad(matrix,c,-width,hem,z,width,hem,z,width,foot,z,-width,foot,z,light,0,.20F,1,1);
            quad(matrix,c,-width,top,fold,-width,hem,z,-width,hem,skirt,-width,top,skirt,light,0,.05F,.25F,.20F);
            quad(matrix,c,-width,hem,z,-width,foot,z,-width,foot,skirt,-width,hem,skirt,light,0,.20F,.25F,1);
            quad(matrix,c,width,hem,z,width,top,fold,width,top,skirt,width,hem,skirt,light,.75F,.05F,1,.20F);
            quad(matrix,c,width,foot,z,width,hem,z,width,hem,skirt,width,foot,skirt,light,.75F,.20F,1,1);
            quad(matrix,c,-width,foot,z,width,foot,z,width,foot,skirt,-width,foot,skirt,light,0,.75F,1,1);
        });
    }
    private static void quad(PoseStack.Pose m,VertexConsumer c,float ax,float ay,float az,float bx,float by,float bz,float cx,float cy,float cz,float dx,float dy,float dz,int light,float u0,float v0,float u1,float v1){
        float ux=bx-ax,uy=by-ay,uz=bz-az,vx=cx-ax,vy=cy-ay,vz=cz-az;
        float nx=uz*vy-uy*vz,ny=ux*vz-uz*vx,nz=uy*vx-ux*vy;
        float length=(float)Math.sqrt(nx*nx+ny*ny+nz*nz);nx/=length;ny/=length;nz/=length;
        // entityCutout already renders both sides. Reversed copies caused coplanar overdraw.
        vertex(m,c,dx,dy,dz,u0,v1,light,nx,ny,nz);vertex(m,c,cx,cy,cz,u1,v1,light,nx,ny,nz);
        vertex(m,c,bx,by,bz,u1,v0,light,nx,ny,nz);vertex(m,c,ax,ay,az,u0,v0,light,nx,ny,nz);
    }
    private static void vertex(PoseStack.Pose m,VertexConsumer c,float x,float y,float z,float u,float v,int light,float nx,float ny,float nz){c.addVertex(m,x/16,y/16,z/16).setColor(-1).setUv(u,v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m,nx,ny,nz);}
}
