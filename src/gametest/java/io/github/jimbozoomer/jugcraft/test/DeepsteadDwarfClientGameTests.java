package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.client.DeepsteadDwarfRenderer;
import io.github.jimbozoomer.jugcraft.town.DeepsteadDwarf;
import io.github.jimbozoomer.jugcraft.town.DeepsteadWares;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.LightLayer;

/** Actual lit meshes at close range, with all three roles and both watch visor positions. */
public final class DeepsteadDwarfClientGameTests implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext context) {
        try(var world=WesternTerracedCityClientGameTests.reviewWorld(context,null)) {
            var server=world.getServer();
            server.runCommand("gamemode spectator @p");
            server.runCommand("gamerule minecraft:spawn_mobs false");
            // Light the complete indoor set before moving the camera into its tracking range.
            server.runCommand("forceload add 399 399 416 416");
            server.waitFor(s->{
                for(int x=24;x<=26;x++) for(int z=24;z<=26;z++)
                    if(!s.overworld().isPositionEntityTicking(new BlockPos(x<<4,100,z<<4))) return false;
                return true;
            },600);
            server.runCommand("fill 400 99 400 415 106 415 deepslate_bricks hollow");
            server.runCommand("fill 401 99 401 414 99 414 polished_deepslate");
            server.runCommand("fill 400 103 400 415 103 400 waxed_cut_copper");
            for(int x=401;x<=413;x+=4) {
                server.runCommand("fill "+x+" 100 401 "+x+" 105 401 stripped_dark_oak_log");
                server.runCommand("setblock "+x+" 102 402 minecraft:wall_torch[facing=south]");
                server.runCommand("fill "+x+" 105 401 "+x+" 105 413 dark_oak_log[axis=z]");
                server.runCommand("setblock "+x+" 104 406 minecraft:lantern[hanging=true]");
            }
            for(int x:new int[]{402,405,408,411,414}) {
                server.runCommand("setblock "+x+" 106 405 ochre_froglight");
                server.runCommand("setblock "+x+" 106 409 ochre_froglight");
                server.runCommand("setblock "+x+" 99 407 shroomlight");
                server.runCommand("setblock "+x+" 100 407 minecraft:iron_trapdoor[half=bottom]");
                server.runCommand("setblock "+x+" 102 407 minecraft:light[level=15]");
            }
            server.runCommand("fill 401 100 403 404 100 403 polished_blackstone_bricks");
            server.runCommand("setblock 403 102 402 minecraft:light[level=15]");
            server.runCommand("setblock 405 100 404 anvil");
            server.runCommand("setblock 407 100 402 blast_furnace[facing=south,lit=true]");
            server.runCommand("setblock 412 100 402 barrel[facing=up]");
            server.runCommand("setblock 413 100 403 smithing_table");
            int[] ids=server.computeOnServer(s->{
                var level=s.overworld();
                // Confirm the staging commands actually built the floor before standing NPCs on it.
                for(int x=401;x<=414;x++) for(int z=401;z<=414;z++)
                    if(z!=407 || (x-402)%3!=0)
                    if(!level.getBlockState(new BlockPos(x,99,z)).is(net.minecraft.world.level.block.Blocks.POLISHED_DEEPSLATE))
                        throw new AssertionError("Forge floor missing at "+x+","+z);
                level.setBlockAndUpdate(new BlockPos(402,101,403),DeepsteadWares.READING_LAMP.defaultBlockState()
                    .setValue(DeepsteadWares.BRIGHTNESS,2));
                level.setBlockAndUpdate(new BlockPos(403,100,403),DeepsteadWares.KNOT_TILE.defaultBlockState());
                level.setBlockAndUpdate(new BlockPos(404,101,403),DeepsteadWares.GALLERY_CHIMES.defaultBlockState());
                var roles=new DeepsteadDwarf.Role[]{DeepsteadDwarf.Role.MINER,DeepsteadDwarf.Role.MERCHANT,
                    DeepsteadDwarf.Role.GUARD,DeepsteadDwarf.Role.GUARD};
                var looks=new int[]{0,2,0,1};
                int[] result=new int[roles.length];
                for(int i=0;i<roles.length;i++) {
                    var home=new BlockPos(404+2*i,100,406);
                    var dwarf=DeepsteadDwarf.TYPE.create(level,EntitySpawnReason.COMMAND);
                    dwarf.configure(roles[i],roles[i].name(),home,looks[i]);
                    dwarf.setPos(home.getX()+.5,100,406.5);
                    dwarf.setYRot(i<2?-10:10); dwarf.setYHeadRot(dwarf.getYRot()); dwarf.setYBodyRot(dwarf.getYRot());
                    dwarf.setNoAi(true);
                    if(!level.addFreshEntity(dwarf)) throw new AssertionError("Dwarf failed to enter the forge");
                    result[i]=dwarf.getId();
                }
                return result;
            });
            server.waitFor(s->{
                for(int i=0;i<4;i++) if(s.overworld().getBrightness(LightLayer.BLOCK,new BlockPos(404+2*i,101,406))<10) return false;
                return true;
            },320);
            server.runCommand("tp @p 407.6 100.12 410.8 180 9");
            context.waitFor(c->{
                for(int id:ids) if(!(c.level.getEntity(id) instanceof DeepsteadDwarf)) return false;
                return true;
            },240);
            int[] light=new int[4];
            try {
                context.waitFor(c->{
                    boolean lit=true;
                    for(int i=0;i<ids.length;i++) {
                        light[i]=c.level.getBrightness(LightLayer.BLOCK,new BlockPos(404+2*i,101,406));
                        lit &= light[i]>=10;
                    }
                    return lit;
                },360);
            } catch(AssertionError e) {
                throw new AssertionError("Client forge light at all four faces: "+java.util.Arrays.toString(light),e);
            }
            context.runOnClient(c->{
                float open=0;
                for(int i=0;i<ids.length;i++) {
                    var dwarf=(DeepsteadDwarf)c.level.getEntity(ids[i]);
                    var renderer=(DeepsteadDwarfRenderer)c.getEntityRenderDispatcher().getRenderer(dwarf);
                    var state=renderer.createRenderState();
                    renderer.extractRenderState(dwarf,state,.5F);
                    if(c.getResourceManager().getResource(renderer.getTextureLocation(state)).isEmpty())
                        throw new AssertionError("Dwarf skin unresolved");
                    var model=renderer.getModel(); model.setupAnim(state);
                    var body=model.root().getChild("body"); var helm=body.getChild("head").getChild("guard_helmet");
                    var maul=body.getChild("right_arm").getChild("stone_maul");
                    var ledger=body.getChild("left_arm").getChild("ledger");
                    if(maul.visible!=(i==0) || ledger.visible!=(i==1) || helm.visible!=(i>=2))
                        throw new AssertionError("Wrong role silhouette for dwarf "+i);
                    var beard=body.getChild("head").getChild("beard");
                    if(beard.getChild("smith_beard").visible!=(i==0)
                            || beard.getChild("merchant_beard").visible!=(i==1)
                            || beard.getChild("watch_beard").visible!=(i>=2))
                        throw new AssertionError("Stale beard geometry on shared renderer for dwarf "+i);
                    if(i==2) open=helm.getChild("visor").xRot;
                    if(i==3 && Math.abs(open-helm.getChild("visor").xRot)<1)
                        throw new AssertionError("Open and closed visors must be distinct");
                }
            });
            boolean hud=context.computeOnClient(c->c.gui.hud.isHidden());
            if(!hud) context.runOnClient(c->c.gui.hud.toggle());
            try {
                server.runCommand("tp @p 407.6 100.12 410.8 180 9");
                context.waitTicks(40); world.getConnection().waitForChunksRender(false,400);
                context.takeScreenshot("deepstead_dwarves_guild");
                server.runCommand("tp @p 405.95 100.04 409.25 168 11");
                context.waitTicks(20); world.getConnection().waitForChunksRender(false,400);
                context.takeScreenshot("deepstead_dwarves_smith_and_lampwright");
                server.runCommand("tp @p 410.0 100.05 409.1 167 12");
                context.waitTicks(20); world.getConnection().waitForChunksRender(false,400);
                context.takeScreenshot("deepstead_dwarves_watch");
                portrait(context,world,server,"smith",405.28,408.02,153);
                portrait(context,world,server,"merchant",407.28,408.02,153);
                portrait(context,world,server,"open_visor",409.28,408.02,153);
                portrait(context,world,server,"closed_visor",409.78,408.06,-156);
                server.runCommand("tp @p 403.8 100.65 406.2 174 16");
                context.waitTicks(20); world.getConnection().waitForChunksRender(false,400);
                context.takeScreenshot("deepstead_wares");
                GuildGoodsDisplay.capture(context,world);
            } finally {
                if(!hud) context.runOnClient(c->c.gui.hud.toggle());
            }
        }
    }

    private static void portrait(ClientGameTestContext context,
            net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext world,
            net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server,
            String role,double x,double z,int yaw) {
        server.runCommand(String.format(java.util.Locale.ROOT,"tp @p %.2f 99.65 %.2f %d 15",x,z,yaw));
        context.waitTicks(20);
        world.getConnection().waitForChunksRender(false,400);
        context.takeScreenshot("deepstead_portrait_"+role);
    }
}
