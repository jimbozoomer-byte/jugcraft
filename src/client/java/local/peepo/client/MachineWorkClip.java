package local.peepo.client;

import local.peepo.WorkAnimation;
import net.minecraft.util.Mth;

/** One reusable pose per render state. Props and hands share these exact transforms. */
final class MachineWorkClip {
    static final float VALVE_RADIUS=1.35F, LEVER_GRIP=-2F, WRENCH_GRIP=1.55F;
    float x,y,z,xRot,zRot,leftX,leftY,leftZ,rightX,rightY,rightZ;

    void evaluate(WorkAnimation clip,float phase,boolean pumpkin){
        x=0;y=20;z=-4.2F-(pumpkin?.65F:0);xRot=zRot=0;
        float gripY=0, spread=.34F;
        switch(clip){
            case VALVE -> {zRot=Mth.sin(phase)*.70F;spread=VALVE_RADIUS;}
            case LEVER -> {y=21.5F;xRot=Mth.sin(phase)*.5F;gripY=LEVER_GRIP;}
            case MALLET -> {
                // Rest on contact, slow wind-up, quick strike, then hold contact through the loop seam.
                float t=phase/Mth.TWO_PI;
                float strike=t<.15F?1:t<.65F?1-smooth((t-.15F)/.5F):t<.8F?smooth((t-.65F)/.15F):1;
                y=19.8F+strike*.55F;xRot=-.10F+strike*1.25F;
            }
            case WRENCH -> {
                y=19.4F;gripY=WRENCH_GRIP;
                // A deliberate tightening stroke followed by a shorter, faster ratchet return.
                float t=phase/Mth.TWO_PI;
                float turn=t<.7F?smooth(t/.7F):1-smooth((t-.7F)/.3F);
                zRot=-.55F+turn*1.1F;
            }
            default -> {return;}
        }
        float cosX=Mth.cos(xRot), sinX=Mth.sin(xRot), cosZ=Mth.cos(zRot), sinZ=Mth.sin(zRot);
        // ModelPart applies Z * Y * X. These clips have no Y rotation.
        leftX=x+spread*cosZ-gripY*cosX*sinZ;
        rightX=x-spread*cosZ-gripY*cosX*sinZ;
        leftY=y+spread*sinZ+gripY*cosX*cosZ;
        rightY=y-spread*sinZ+gripY*cosX*cosZ;
        leftZ=rightZ=z+gripY*sinX;
    }
    private static float smooth(float t){return t*t*(3-2*t);}
}
