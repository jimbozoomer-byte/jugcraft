package local.peepo.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

/** Small closed-cube tools, with separate existing wood/iron textures and no item entities. */
final class WorkPropsModel extends EntityModel<PeepoState> {
    private final ModelPart valve,lever,mallet,wrench,valveMount,leverMount;
    WorkPropsModel(ModelPart root){
        super(root);
        valve=root.getChild("valve");lever=root.getChild("lever");
        mallet=root.getChild("mallet");wrench=root.getChild("wrench");
        valveMount=root.getChild("valve_mount");leverMount=root.getChild("lever_mount");
    }
    @Override public void setupAnim(PeepoState state){
        super.setupAnim(state);
        valve.visible=lever.visible=mallet.visible=wrench.visible=valveMount.visible=leverMount.visible=false;
        ModelPart active=switch(state.work){
            case VALVE -> valve;
            case LEVER -> lever;
            case MALLET -> mallet;
            case WRENCH -> wrench;
            default -> null;
        };
        if(active==null)return;
        var p=state.toolPose;
        active.visible=true;active.x=p.x;active.y=p.y;active.z=p.z;active.xRot=p.xRot;active.zRot=p.zRot;
        ModelPart mount=state.work==local.peepo.WorkAnimation.VALVE?valveMount:
            state.work==local.peepo.WorkAnimation.LEVER?leverMount:null;
        if(mount!=null){mount.visible=true;mount.x=p.x;mount.y=p.y;mount.z=p.z;}
    }
    static LayerDefinition create(boolean metal){
        var mesh=new MeshDefinition();var root=mesh.getRoot();
        var valve=root.addOrReplaceChild("valve",CubeListBuilder.create(),PartPose.ZERO);
        var lever=root.addOrReplaceChild("lever",CubeListBuilder.create(),PartPose.ZERO);
        var mallet=root.addOrReplaceChild("mallet",CubeListBuilder.create(),PartPose.ZERO);
        var wrench=root.addOrReplaceChild("wrench",CubeListBuilder.create(),PartPose.ZERO);
        var valveMount=root.addOrReplaceChild("valve_mount",CubeListBuilder.create(),PartPose.ZERO);
        var leverMount=root.addOrReplaceChild("lever_mount",CubeListBuilder.create(),PartPose.ZERO);
        if(metal){
            // Octagonal rim, four spokes and a fixed mounting shaft behind the rotating handwheel.
            for(int i=0;i<8;i++){
                float angle=i*Mth.PI/4;
                valve.addOrReplaceChild("rim_"+i,box(-.17F,-.58F,-.17F,.34F,1.16F,.34F),
                    PartPose.offsetAndRotation(Mth.cos(angle)*MachineWorkClip.VALVE_RADIUS,
                        Mth.sin(angle)*MachineWorkClip.VALVE_RADIUS,0,0,0,angle));
            }
            valve.addOrReplaceChild("spokes",box(-1.25F,-.10F,-.12F,2.5F,.2F,.24F)
                .texOffs(0,0).addBox(-.10F,-1.25F,-.12F,.2F,2.5F,.24F),PartPose.ZERO);
            valveMount.addOrReplaceChild("shaft",box(-.20F,-.20F,-1.4F,.4F,.4F,1.4F),PartPose.ZERO);
            valveMount.addOrReplaceChild("plate",box(-.6F,-.6F,-1.5F,1.2F,1.2F,.22F),PartPose.ZERO);
            lever.addOrReplaceChild("shaft",box(-.28F,MachineWorkClip.LEVER_GRIP,-.28F,.56F,3.7F,.56F),PartPose.ZERO);
            leverMount.addOrReplaceChild("pivot",box(-.8F,-.4F,-.5F,1.6F,.8F,1),PartPose.ZERO);
            leverMount.addOrReplaceChild("bracket",box(-.6F,-.25F,-1.5F,1.2F,.5F,1.25F),PartPose.ZERO);
            mallet.addOrReplaceChild("band",box(-.24F,-2.135F,-.485F,.48F,.97F,.97F),PartPose.ZERO);
            wrench.addOrReplaceChild("handle",box(-.36F,-.05F,-.27F,.72F,3.5F,.54F),PartPose.ZERO);
            wrench.addOrReplaceChild("jaw_base",box(-1.05F,-.5F,-.32F,2.1F,.7F,.64F),PartPose.ZERO);
            wrench.addOrReplaceChild("jaw_left",box(-1.05F,-1.5F,-.32F,.52F,1,.64F),PartPose.ZERO);
            wrench.addOrReplaceChild("jaw_right",box(.53F,-1.5F,-.32F,.52F,1,.64F),PartPose.ZERO);
        }else{
            lever.addOrReplaceChild("grip",box(-1.05F,MachineWorkClip.LEVER_GRIP-.32F,-.35F,2.1F,.64F,.7F),PartPose.ZERO);
            mallet.addOrReplaceChild("handle",box(-.21F,-1.85F,-.21F,.42F,2.2F,.42F),PartPose.ZERO);
            mallet.addOrReplaceChild("head",box(-1.1F,-2.1F,-.45F,2.2F,.9F,.9F),PartPose.ZERO);
        }
        return LayerDefinition.create(mesh,16,16);
    }
    private static CubeListBuilder box(float x,float y,float z,float dx,float dy,float dz){
        return CubeListBuilder.create().texOffs(0,0).addBox(x,y,z,dx,dy,dz);
    }
}
