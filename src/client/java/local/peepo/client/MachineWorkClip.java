package local.peepo.client;

import local.peepo.WorkAnimation;
import net.minecraft.util.Mth;

/** One reusable pose per render state. Props and hands share these exact transforms. */
final class MachineWorkClip {
    static final float VALVE_RADIUS=2.6F, VALVE_Y=19.6F, LEVER_GRIP=-3.4F, WRENCH_GRIP=2.25F;
    float x,y,z,xRot,zRot,leftX,leftY,leftZ,rightX,rightY,rightZ;
    float bodyPitch,bodyRoll,bodyX,bodyY;

    void evaluate(WorkAnimation clip,float phase,boolean pumpkin){
        x=0;y=20;z=-4.2F-(pumpkin?.65F:0);xRot=zRot=0;
        bodyPitch=bodyRoll=bodyX=bodyY=0;
        float gripY=0, spread=.34F;
        float gripSeparation=0;
        switch(clip){
            case VALVE -> {
                float effort=Mth.sin(phase), brace=1-Mth.cos(phase*2);
                y=VALVE_Y;zRot=effort*.65F;spread=VALVE_RADIUS;
                bodyRoll=-effort*.18F;bodyX=-effort*.30F;
                bodyPitch=.04F+brace*.04F;bodyY=brace*.08F;
            }
            case LEVER -> {
                float effort=Mth.sin(phase);
                y=22.3F;z=pumpkin?-4.65F:-4.4F;
                // Keep the enlarged grip in front of the machine face, including the costume.
                xRot=pumpkin?-.015F+effort*.165F:-.08F+effort*.24F;
                gripY=LEVER_GRIP;spread=.65F;
                bodyPitch=.10F+effort*.16F;
            }
            case MALLET -> {
                // Rest on contact, slow wind-up, quick strike, then hold contact through the loop seam.
                float t=phase/Mth.TWO_PI;
                float strike=t<.15F?1:t<.65F?1-smooth((t-.15F)/.5F):t<.8F?smooth((t-.65F)/.15F):1;
                // Lift the larger head above the brow, then bring the torso into the downstroke.
                y=16.8F+strike*2.8F;xRot=-.05F+strike*.65F;
                bodyPitch=-.12F+strike*.40F;bodyY=strike*.16F;
            }
            case WRENCH -> {
                y=18.5F;gripY=WRENCH_GRIP;
                // A deliberate tightening stroke followed by a shorter, faster ratchet return.
                float t=phase/Mth.TWO_PI;
                float turn=t<.7F?smooth(t/.7F):1-smooth((t-.7F)/.3F);
                zRot=-.90F+turn*1.8F;
                float effort=Mth.sin(zRot);
                bodyRoll=-zRot*.24F;bodyX=-effort*.45F;
                // Slide the far hand towards the jaws while the near hand supplies leverage.
                gripSeparation=effort*.55F;spread=.15F;
            }
            default -> {return;}
        }
        float cosX=Mth.cos(xRot), sinX=Mth.sin(xRot), cosZ=Mth.cos(zRot), sinZ=Mth.sin(zRot);
        // ModelPart applies Z * Y * X. These clips have no Y rotation.
        float leftGrip=gripY-gripSeparation,rightGrip=gripY+gripSeparation;
        leftX=x+spread*cosZ-leftGrip*cosX*sinZ;
        rightX=x-spread*cosZ-rightGrip*cosX*sinZ;
        leftY=y+spread*sinZ+leftGrip*cosX*cosZ;
        rightY=y-spread*sinZ+rightGrip*cosX*cosZ;
        leftZ=z+leftGrip*sinX;rightZ=z+rightGrip*sinX;
    }
    private static float smooth(float t){return t*t*(3-2*t);}
}
