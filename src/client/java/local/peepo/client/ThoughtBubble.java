package local.peepo.client;

import com.mojang.blaze3d.vertex.*;
import local.peepo.CompanionFeedbackRules.Kind;
import local.peepo.CompanionUniforms.Type;
import local.peepo.PeepoMod;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

/** Camera-facing Minecraft pixel bubble, rendered with native geometry and native item models. */
final class ThoughtBubble {
    private ThoughtBubble(){}
    static void submit(PeepoState state,PoseStack pose,SubmitNodeCollector collector,CameraRenderState camera){
        if(state.thought==Kind.NONE || state.isInvisible || state.deathTime>0 || state.distanceToCameraSq>144)return;
        float age=state.thoughtTime,fade=Math.max(0,Math.min(1,Math.min(age/4,(40-age)/6)));
        if(fade<=0)return;
        pose.pushPose();
        // Highest costume geometry is the chef hat, including the hat above Jughead's jug.
        double top=state.jughead && !state.pumpkin?1.12:.77;
        if(state.uniform==Type.CHEF && !state.pumpkin)top+=.27;
        pose.translate(0,top+.1+Math.sin(Math.min(age,8)*Math.PI/16)*.025,0);
        pose.rotate(camera.orientation);pose.scale(.018F*fade,.018F*fade,.018F*fade);
        // Opaque depth-writing quads keep small symbols intact with the pack's OIT renderer.
        // Reuse the existing solid eye-white palette texel; no extra texture or transparent layers.
        collector.submitCustomGeometry(pose,RenderTypes.entitySolid(PeepoMod.id("textures/entity/peepo.png")),(matrix,c)->{
            rect(matrix,c,-12,-8,12,8,0,0xFF252A32);rect(matrix,c,-14,-6,14,6,0,0xFF252A32);
            rect(matrix,c,-11,-7,11,7,.1F,0xFFF7F3E7);rect(matrix,c,-13,-5,13,5,.1F,0xFFF7F3E7);
            rect(matrix,c,-4,-12,0,-9,0,0xFF252A32);rect(matrix,c,-3,-11,-1,-10,.1F,0xFFF7F3E7);
            rect(matrix,c,-7,-15,-5,-13,0,0xFF252A32);
            String[] mark=symbol(!state.thoughtItem.isEmpty() && state.thought==Kind.TOOL?Kind.SUPPLIES:state.thought);
            float x=state.thoughtItem.isEmpty()?-mark[0].length()*.75F:state.thought==Kind.HARVEST || state.thought==Kind.CATCH?4.5F:7;
            float glyphTop=state.thought==Kind.HARVEST || state.thought==Kind.CATCH?2.5F:4;
            for(int y=0;y<mark.length;y++)for(int col=0;col<mark[y].length();col++)if(mark[y].charAt(col)=='#')
                rect(matrix,c,x+col*1.5F,glyphTop-y*1.5F,x+(col+1)*1.5F,glyphTop+1.5F-y*1.5F,.2F,0xFF25303A);
            if(state.thought==Kind.BONUS){rect(matrix,c,8,3,9,8,.2F,0xFFB57F18);rect(matrix,c,6,5,11,6,.2F,0xFFB57F18);}
        });
        if(!state.thoughtItem.isEmpty()){
            pose.translate(-3,0,.4);pose.scale(12,12,.1F);
            state.thoughtItem.submit(pose,collector,15728880,OverlayTexture.NO_OVERLAY,0);
        }
        pose.popPose();
    }
    private static String[] symbol(Kind kind){return switch(kind){
        case STOCKED,RELIEVED,DELIVERY,COOK -> new String[]{"      #","     ##","#   ## ","## ##  "," ###   ","  #    "};
        case HARVEST,CATCH -> new String[]{"    #","   ##","# ## ","###  "," #   "};
        case BONUS -> new String[]{"  #  ","  #  ","#####","  #  ","  #  "};
        case TIRED -> new String[]{"#####","   # ","  #  "," #   ","#####"};
        case POWER -> new String[]{"  ###"," ### ","#####","  ## "," ##  "," #   "};
        case HEAT -> new String[]{"  #  ","  ## "," # # ","## ##","#####"," ### "};
        case OUTPUT -> new String[]{"##","##","##","##","  ","##"};
        case TOOL -> new String[]{"# #  ","###  "," #   ","  #  ","   # ","    #"};
        default -> new String[]{"###","  #"," ##"," # ","   "," # "};
    };}
    private static void rect(PoseStack.Pose m,VertexConsumer c,float x0,float y0,float x1,float y1,float z,int color){
        vertex(m,c,x0,y0,z,color);vertex(m,c,x1,y0,z,color);vertex(m,c,x1,y1,z,color);vertex(m,c,x0,y1,z,color);
    }
    private static void vertex(PoseStack.Pose m,VertexConsumer c,float x,float y,float z,int color){
        c.addVertex(m,x,y,z).setColor(color).setUv(9.5F/256,73.5F/256).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(m,0,0,1);
    }
}
