package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.pharmacology.DrugStrain;
import io.github.jimbozoomer.jugcraft.pharmacology.StrainStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Util;
import net.minecraft.world.entity.LivingEntity;

/** Additive, read-only HUD. Each pass paints over the previous full row; no gameplay threshold changes. */
public final class StrainPillHud {
	private static final Identifier TEXTURE=Jugcraft.id("textures/gui/strain_pills.png");
	private static final int[][] COLORS={
		{0x292b2c,0x434644,0x656962}, {0xaaa98d,0xe2dfbc,0xfff3ce},
		{0x9f7430,0xe1b54f,0xffe58a}, {0x9c4a25,0xdd833c,0xffc172},
		{0x852f36,0xca5559,0xff9891}, {0x573576,0x9260ba,0xd19aed}
	};
	private static long depletedAt=-1;
	private StrainPillHud() {}
	static void reset() {depletedAt=-1;}
	static void statusChanged(StrainStatus previous,StrainStatus next) {
		if(next.tenths()>0)depletedAt=-1;
		else if(previous.tenths()>0)depletedAt=Util.getMillis();
	}
	/** Presentation time only. Repeated zero packets or changing the held drug cannot extend the grace. */
	public static boolean lingering(long now,long depletedAt) {return depletedAt>=0&&now>=depletedAt&&now-depletedAt<3000;}

	/** Atlas row: empty, ivory, amber, orange, red, violet. Boundaries match DrugStrain. */
	public static int layer(int tenths) {
		return tenths<=1000?1:tenths<=1250?2:tenths<=1500?3:tenths<=2000?4:5;
	}
	public static int fillColumns(int tenths,int pill) {
		int amount=tenths<=1000?Math.max(0,tenths):tenths<=2000?tenths-1000:Math.min(1000,tenths-2000);
		int local=amount-pill*100;
		return local<=0?0:local>=100?5:Math.max(1,local*5/100);
	}
	public static int rowX(int width) {return width/2+10;}
	public static int rowY(int height,int vehicleRows,boolean air,boolean held) {
		// Hunger occupies one right-hand row; mounted health replaces it with up to three rows.
		int y=height-39-Math.max(1,vehicleRows)*10-(air?10:0);
		// Vanilla's selected-item title occupies height-59 through height-50. Leave it untouched.
		if(held && y+7>=height-60 && y<=height-50)y=height-70;
		// Native action-bar text (including the existing held-drug status) occupies height-72..-63.
		// Reserve its lane even between messages so stacked hearts cannot make this row jump.
		if(y+8>=height-73 && y<=height-62)y=height-82;
		return Math.max(2,y);
	}
	public static boolean visible(StrainStatus s,boolean alive,boolean spectator,boolean hidden,boolean survival) {
		return visible(s,alive,spectator,hidden,survival,false);
	}
	public static boolean visible(StrainStatus s,boolean alive,boolean spectator,boolean hidden,boolean survival,boolean lingering) {
		return alive&&!spectator&&!hidden&&survival&&(s.tenths()>0||lingering);
	}
	private static int pixel(int row,int x,int y) {
		if(x<2||x>6||y<3||y>5)return 0;
		return COLORS[row][x==4?0:y==3?2:y==5?0:1];
	}
	private static int currentPixel(int tenths,int pill,int x,int y) {
		int row=layer(tenths),base=row==1?0:row==5?4:1;
		return pixel(x-2<fillColumns(tenths,pill)?row:base,x,y);
	}
	private static void sprite(GuiGraphicsExtractor g,int x,int y,int row,int columns) {
		g.blit(RenderPipelines.GUI_TEXTURED,TEXTURE,x,y,columns*9,row*9,9,9,54,54);
	}
	public static void extract(GuiGraphicsExtractor g,StrainStatus s) {
		var c=Minecraft.getInstance();var p=c.player;
		if(p==null||c.gameMode==null||c.getCameraEntity()!=p||!visible(s,p.isAlive(),p.isSpectator(),c.gui.hud.isHidden(),c.gameMode.canHurtPlayer(),lingering(Util.getMillis(),depletedAt)))return;
		int x=rowX(g.guiWidth());
		boolean held=!p.getMainHandItem().isEmpty();
		int vehicleRows=0;
		if(p.getVehicle() instanceof LivingEntity mount && mount.showVehicleHealth()) {
			int hearts=Math.min(30,(int)(mount.getMaxHealth()+.5F)/2);
			vehicleRows=(int)Math.ceil(hearts/10.0);
		}
		boolean air=p.isEyeInFluid(FluidTags.WATER)||p.getAirSupply()<p.getMaxAirSupply();
		int y=rowY(g.guiHeight(),vehicleRows,air,held);
		int row=layer(s.tenths()),base=row==1?0:row==5?4:1;
		int nextCost=s.tenths()>0?s.nextCost():0;
		int future=DrugStrain.add(s.tenths(),nextCost);
		for(int i=0;i<10;i++) {
			int px=x+i*8;
			sprite(g,px,y,0,0);
			if(base>0)sprite(g,px,y,base,5);
			int columns=fillColumns(s.tenths(),i);
			if(columns>0)sprite(g,px,y,row,columns);
			if(nextCost>0)for(int yy=3;yy<=5;yy++)for(int xx=2;xx<=6;xx++) {
				int now=currentPixel(s.tenths(),i,xx,yy),next=currentPixel(future,i,xx,yy);
				if(now!=next&&(xx+yy)%2==0)g.fill(px+xx,y+yy,px+xx+1,y+yy+1,0xC0000000|next);
			}
		}
		if(s.tenths()>1000||nextCost>0) {
			int tx=x+85,available=Math.max(0,g.guiWidth()-tx-4),ty=Math.min(y,g.guiHeight()-(nextCost>0?94:49));
			String number=DrugStrain.format(s.tenths());
			String label=Component.translatable("hud.jugcraft.strain_pills",number,Component.translatable(DrugStrain.band(s.tenths()).key())).getString();
			if(c.font.width(label)>available)label=number;
			if(c.font.width(label)<=available)g.text(c.font,label,tx,ty,0xFF000000|DrugStrain.band(s.tenths()).color);
			if(nextCost>0) {
				String forecast=Component.translatable("hud.jugcraft.strain_pills_next",nextCost,DrugStrain.format(future)).getString();
				if(c.font.width(forecast)>available)forecast="→ "+DrugStrain.format(future);
				if(c.font.width(forecast)>available)forecast=DrugStrain.format(future);
				if(c.font.width(forecast)<=available)g.text(c.font,forecast,tx,ty+10,0xFF000000|DrugStrain.band(future).color);
			}
		}
	}
}
