package local.peepo;

import java.util.Locale;
import local.peepo.client.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.*;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

/** Native costume previews and cosmetic-only/visibility regressions on both companion rigs. */
public final class PeepoSeasonalCostumeClientTests implements FabricClientGameTest {
    private int checks;
    private PeepoEntity npc;
    private void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    @Override public void runTest(ClientGameTestContext context){
        context.runOnClient(c->{
            var model=new PeepoModel(PeepoGeometry.create().bakeRoot());
            var state=new PeepoState();state.jughead=true;
            for(var type:CompanionUniforms.Type.values()){
                state.uniform=type;model.setupAnim(state);
                boolean hidden=type==CompanionUniforms.Type.PILGRIM || type==CompanionUniforms.Type.FEATHERED_REGALIA;
                check(model.root().getChild("head").getChild("jughead_jug").visible==!hidden,"jug and child bump visibility for "+type);
                check(model.root().getChild("jughead_shorts").visible==!hidden,"shorts hidden only for full seasonal costumes");
            }
            state.uniform=CompanionUniforms.Type.NONE;model.setupAnim(state);
            check(model.root().getChild("head").getChild("jughead_jug").visible,"removing costume restores jug and bump");
        });
        try(var world=context.worldBuilder().adjustSettings(c->{
            c.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);
            c.setWorldType(new WorldCreationUiState.WorldTypeEntry(c.getSettings().worldgenLoadContext().lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT)));
            c.setGenerateStructures(false);
        }).create()){
            world.getConnection().waitForChunksRender();var server=world.getServer();
            var base=context.computeOnClient(c->c.player.blockPosition().above(4));
            server.runCommand("time set noon");server.runCommand("weather clear");server.runCommand("gamerule minecraft:random_tick_speed 0");server.runCommand("gamemode spectator @a");
            server.runOnServer(s->{for(var p:BlockPos.betweenClosed(base.offset(-6,-1,-6),base.offset(6,-1,6)))s.overworld().setBlock(p,Blocks.SMOOTH_QUARTZ.defaultBlockState(),2);});
            context.getInput().pressKey(o->o.keyToggleGui);
            try{
                for(var type:new CompanionUniforms.Type[]{CompanionUniforms.Type.PILGRIM,CompanionUniforms.Type.FEATHERED_REGALIA})for(boolean jug:new boolean[]{false,true}){
                    server.runOnServer(s->{
                        if(npc!=null)npc.discard();
                        npc=(jug?PeepoMod.JUGHEAD:PeepoMod.PEEPO).create(s.overworld(),EntitySpawnReason.COMMAND);
                        npc.setNoAi(true);npc.snapTo(base.getX()+.5,base.getY(),base.getZ()+.5,180,0);npc.setOnGround(true);s.overworld().addFreshEntity(npc);
                        npc.preferences.social=false;npc.preferences.feedback=0;
                        var item=BuiltInRegistries.ITEM.getValue(PeepoMod.id(type.path));
                        check(CompanionUniforms.costume(new ItemStack(item)),"registered costume accepted by existing slot");
                        npc.belongings.setItem(8,new ItemStack(item));
                        check(npc.uniform()==type,"costume synchronizes from equipped slot");
                        check(npc.uniforms.saving(type)==0 && npc.uniforms.duration(type,40)==40,"cosmetic costume has no own savings or speed bonus");
                        for(var job:CompanionUniforms.Type.values())if(job!=type){
                            check(npc.uniforms.saving(job)==0,"no specialist savings");
                            check(npc.uniforms.duration(job,40)==40,"no specialist action speed");
                        }
                        try(var tx=net.fabricmc.fabric.api.transfer.v1.transaction.Transaction.openOuter()){
                            check(!npc.uniforms.bonus(type,tx),"cosmetic outfit never creates bonus products");
                        }
                    });
                    int id=npc.getId();context.waitTicks(20);context.waitFor(c->c.level.getEntity(id) instanceof PeepoEntity,100);
                    server.runCommand(String.format(Locale.ROOT,"tp @a %.3f %.3f %.3f",base.getX()+.95,base.getY()-1.05,base.getZ()-.6));
                    context.runOnClient(c->c.options.fov().set(25));context.waitTicks(3);context.getInput().lookAt(base);context.waitTicks(3);
                    context.runOnClient(c->{var shown=(PeepoEntity)c.level.getEntity(id);shown.setYRot(180);shown.yRotO=180;shown.yBodyRot=shown.yBodyRotO=shown.yHeadRot=shown.yHeadRotO=180;check(shown.uniform()==type,"client receives equipped costume");});
                    context.takeScreenshot("seasonal_"+type.path+"_"+(jug?"jughead":"peepo"));
                }
            }finally{server.runOnServer(s->{if(npc!=null)npc.discard();});context.getInput().pressKey(o->o.keyToggleGui);}
        }
        org.slf4j.LoggerFactory.getLogger("peepo-costume-test").info("[peepo-costume-test] PASS {} assertions",checks);
    }
}
