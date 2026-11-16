package io.github.jimbozoomer.jugcraft.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Six ticks of view angles, never world frames. Only immutable extracted offsets reach GPU work. */
final class DrugTrails {
	private static final float[] yaw=new float[8], pitch=new float[8], x=new float[3], y=new float[3];
	private static int head;
	private static boolean seeded;
	private static ClientLevel level;
	private static Vec3 position;
	private DrugTrails() {}
	static void clear() {seeded=false;level=null;position=null;java.util.Arrays.fill(x,0);java.util.Arrays.fill(y,0);}
	static void tick(LocalPlayer player,ClientLevel current,boolean enabled) {
		if(!enabled) {clear();return;}
		float horizontal=player.getYRot(),vertical=player.getXRot();
		boolean jump=!seeded || level!=current || position==null || position.distanceToSqr(player.position())>64
				|| Math.abs(Mth.wrapDegrees(horizontal-yaw[head]))>45 || Math.abs(vertical-pitch[head])>45;
		if(jump) {
			java.util.Arrays.fill(yaw,horizontal);java.util.Arrays.fill(pitch,vertical);
			java.util.Arrays.fill(x,0);java.util.Arrays.fill(y,0);head=0;seeded=true;
		} else {
			head=(head+1)&7;yaw[head]=horizontal;pitch[head]=vertical;
			for(int i=0;i<3;i++) {
				int sample=(head-2*(i+1))&7;
				x[i]=Math.clamp(Mth.wrapDegrees(yaw[sample]-horizontal)*.002F,-.018F,.018F);
				y[i]=Math.clamp((pitch[sample]-vertical)*.002F,-.018F,.018F);
			}
		}
		level=current;position=player.position();
	}
	static float x(int i) {return x[i];}
	static float y(int i) {return y[i];}
}
