package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.styx.*;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;

/** Real-client proof of the complete placement path and character/flower renderers, with review screenshots. */
public final class StyxClientGameTests implements FabricClientGameTest {
	@Override public void runTest(ClientGameTestContext context) {
		try(TestSingleplayerContext world=context.worldBuilder().adjustSettings(c -> c.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()) {
			world.getConnection().waitForChunksRender();TestServerContext server=world.getServer();
			server.runCommand("time set noon");server.runCommand("weather clear");
			BlockPos base=server.computeOnServer(s -> {
				var p=s.getPlayerList().getPlayers().getFirst();int y=s.overworld().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,p.blockPosition().getX(),p.blockPosition().getZ())-1;
				return new BlockPos(p.blockPosition().getX()-13,y,p.blockPosition().getZ()-10);
			});
			// Prepare only this disposable test world's site; the player command never clears terrain.
			server.runOnServer(s -> {
				for(int x=-2;x<29;x++) for(int z=-2;z<34;z++) {
					s.overworld().setBlock(base.offset(x,0,z),net.minecraft.world.level.block.Blocks.GRASS_BLOCK.defaultBlockState(),3);
					for(int y=1;y<28;y++) s.overworld().setBlock(base.offset(x,y,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);
				}
			});
			server.runCommand("execute as @p at @s run jugcraft styx preview");
			server.runCommand("execute as @p at @s run jugcraft styx place");
			server.runOnServer(s -> {if(!StyxState.get(s.overworld()).origin.equals(java.util.Optional.of(base)))throw new AssertionError("Operator commands did not begin at the previewed origin");});
			for(int i=0;i<300;i++) {
				context.waitTicks(1);
				if(server.computeOnServer(s -> StyxState.get(s.overworld()).resident.isPresent()))break;
			}
			server.runOnServer(s -> {
				var state=StyxState.get(s.overworld());if(state.resident.isEmpty())throw new AssertionError("Conservatory did not complete");
				var npc=(Styxhexenhammer)s.overworld().getEntity(state.resident.get());
				if(npc==null)throw new AssertionError("Resident missing");
				npc.setNoAi(true);npc.snapTo(base.getX()+13.5,base.getY()+1,base.getZ()+24.5,0,0);npc.setYHeadRot(0);npc.yBodyRot=0;
			});
			context.getInput().pressKey(options -> options.keyToggleGui);
			context.runOnClient(c -> {c.options.fov().set(42);});
			view(context,server,base,10.5,1.6,29.5,-149,8);world.getConnection().waitForChunksRender();context.takeScreenshot("jugcraft_styx_reference_angle");
			view(context,server,base,13.5,1.6,30,180,8);context.takeScreenshot("jugcraft_styx_front");
			view(context,server,base,8,1.6,24.5,-90,8);context.takeScreenshot("jugcraft_styx_profile");
			view(context,server,base,13.5,1.6,19,0,8);context.takeScreenshot("jugcraft_styx_back");
			context.runOnClient(c -> c.options.fov().set(70));
			view(context,server,base,38,21,43,143,8);world.getConnection().waitForChunksRender();context.takeScreenshot("jugcraft_styx_conservatory");
			view(context,server,base,19,2.2,12,180,14);world.getConnection().waitForChunksRender();context.takeScreenshot("jugcraft_styx_greenhouse");
			server.runCommand("time set midnight");context.runOnClient(c -> c.options.fov().set(42));view(context,server,base,10.5,1.6,29.5,-149,8);context.takeScreenshot("jugcraft_styx_night");
			server.runCommand("time set noon");context.runOnClient(c -> c.options.fov().set(30));
			view(context,server,base,11.2,1.8,28.5,-150,8);context.takeScreenshot("jugcraft_styx_face_closeup");
			context.runOnClient(c -> {c.options.fov().set(70);});
			context.getInput().pressKey(options -> options.keyToggleGui);
			server.runOnServer(s -> {var npc=(Styxhexenhammer)s.overworld().getEntity(StyxState.get(s.overworld()).resident.orElseThrow());npc.snapTo(base.getX()+8.5,base.getY()+1,base.getZ()+10.5,0,0);npc.setNoAi(false);});
			for(int phase=0;phase<4;phase++) {
				server.runCommand("time set "+(phase*6000+100));
				BlockPos destination=base.offset(phase==0 ? 18 : 8,phase==1 ? 8 : phase==2 ? 15 : 1,phase==0 ? 11 : phase==2 ? 9 : 10);
				boolean arrived=false;
				for(int i=0;i<80;i++) {
					context.waitTicks(10);
					arrived=server.computeOnServer(s -> {
						var npc=s.overworld().getEntity(StyxState.get(s.overworld()).resident.orElseThrow());
						return npc.distanceToSqr(destination.getX()+.5,destination.getY(),destination.getZ()+.5)<3;
					});
					if(arrived)break;
				}
				if(!arrived)throw new AssertionError("Styx cannot reach routine phase "+phase+": "+server.computeOnServer(s -> s.overworld().getEntity(StyxState.get(s.overworld()).resident.orElseThrow()).position()));
			}
		}
	}
	private static void view(ClientGameTestContext context,TestServerContext server,BlockPos base,double x,double y,double z,float yaw,float pitch) {
		server.runCommand(String.format(Locale.ROOT,"tp @p %.2f %.2f %.2f %.1f %.1f",base.getX()+x,base.getY()+y,base.getZ()+z,yaw,pitch));
		context.runOnClient(c -> {c.player.getAbilities().flying=true;c.player.onUpdateAbilities();});context.waitTicks(20);
	}
}
