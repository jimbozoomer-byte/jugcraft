package io.github.jimbozoomer.jugcraft.finance;

import io.github.jimbozoomer.jugcraft.client.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import java.util.Arrays;

/** Networked Exchange opening, every casino model/screen, and reconnect on a dedicated server. */
public class ExchangeClientGameTests implements FabricClientGameTest {
    private static void check(boolean value,String why) {if(!value)throw new AssertionError(why);}
    @Override public void runTest(ClientGameTestContext context) {
        try(var server=context.worldBuilder().createServer()) {
            try(var connection=server.connect()) {
                connection.waitForChunksRender();server.runCommand("time set noon");server.runCommand("weather clear");
                server.runCommand("gamerule minecraft:spawn_mobs false");
                String clashes=context.computeOnClient(c->Arrays.stream(c.options.keyMappings)
                    .filter(k->k!=FinanceClient.openExchange && k.getCategory()!=KeyMapping.Category.DEBUG && k.same(FinanceClient.openExchange))
                    .map(KeyMapping::getName).toList().toString());
                check(clashes.equals("[]"),"Exchange shortcut conflicts: "+clashes);
                context.getInput().pressKey(FinanceClient.openExchange);context.waitForScreen(ExchangeScreen.class);
                context.waitTicks(10);context.takeScreenshot("jugcraft_exchange_bank");
                for(String tab: new String[]{"Economy","Equities","Commodities","Requests","Contracts","Land"}) {
                    context.clickScreenButton(tab);context.waitTicks(4);context.takeScreenshot("jugcraft_exchange_"+tab.toLowerCase());
                }
                context.runOnClient(c->c.gui.setScreen(new ExchangeGuideScreen(null)));
                context.waitTicks(5);context.takeScreenshot("jugcraft_exchange_guide");context.runOnClient(c->c.gui.setScreen(null));
                BlockPos origin=server.computeOnServer(mc->mc.getPlayerList().getPlayers().getFirst().blockPosition());
                int x=origin.getX(),y=origin.getY(),z=origin.getZ();
                server.runCommand("fill %d %d %d %d %d %d minecraft:stone".formatted(x-5,y-1,z-12,x+9,y-1,z+3));
                server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x-5,y,z-12,x+9,y+8,z+3));
                BlockPos chestAt=new BlockPos(x,y,z-3);
                server.computeOnServer(mc->{var p=mc.getPlayerList().getPlayers().getFirst();var book=Ledger.get(mc).book();
                    book.grant(p.getUUID(),10000,FinanceService.day(mc));
                    check(Equities.get(mc).charter(p,book,"Exchange Test Company","TST",100,2000,5,new byte[256],false,FinanceService.day(mc)).ok(),"company fixture");
                    var level=mc.overworld();var state=ExchangeBlocks.MARKET_CHEST.defaultBlockState();level.setBlockAndUpdate(chestAt,state);
                    ExchangeBlocks.MARKET_CHEST.setPlacedBy(level,chestAt,state,p,new ItemStack(ExchangeBlocks.MARKET_CHEST));
                    ((MarketChestBlockEntity)level.getBlockEntity(chestAt)).open(p);return true;});
                context.waitForScreen(MarketChestScreen.class);context.waitTicks(5);context.takeScreenshot("jugcraft_exchange_market_chest");
                context.runOnClient(c->c.player.closeContainer());
                server.runCommand("setblock %d %d %d air".formatted(chestAt.getX(),chestAt.getY(),chestAt.getZ()));
                for(var game:CasinoBlock.Game.values()) {
                    BlockPos at=new BlockPos(x,y,z-5);
                    server.computeOnServer(mc->{var level=mc.overworld();var player=mc.getPlayerList().getPlayers().getFirst();
                        var block=(CasinoBlock)ExchangeBlocks.CASINO.get(game);var state=block.defaultBlockState().setValue(CasinoBlock.FACING,Direction.NORTH);
                        level.setBlockAndUpdate(at,state);block.setPlacedBy(level,at,state,player,new ItemStack(block));return true;});
                    server.runCommand("tp @a %d %d %d 180 15".formatted(x+1,y+1,z+2));context.waitTicks(15);connection.waitForChunksRender();
                    context.takeScreenshot("jugcraft_exchange_model_"+game.id);
                    server.computeOnServer(mc->{var p=mc.getPlayerList().getPlayers().getFirst();((CasinoBlockEntity)mc.overworld().getBlockEntity(at)).open(p);return true;});
                    context.waitFor(c->c.gui.screen() instanceof CasinoScreen,200);context.waitTicks(5);
                    context.takeScreenshot("jugcraft_exchange_screen_"+game.id);context.runOnClient(c->c.gui.setScreen(null));
                    server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x-5,y,z-12,x+9,y+8,z));
                }
                for(var design:PokerTableBlock.Design.values()) {
                    BlockPos at=new BlockPos(x,y,z-5);
                    server.computeOnServer(mc->{var p=mc.getPlayerList().getPlayers().getFirst();var level=mc.overworld();
                        var block=(PokerTableBlock)ExchangeBlocks.POKER_TABLES.get(design);var state=block.defaultBlockState().setValue(PokerTableBlock.FACING,Direction.NORTH);
                        level.setBlockAndUpdate(at,state);block.setPlacedBy(level,at,state,p,new ItemStack(block));return true;});
                    context.waitTicks(15);context.takeScreenshot("jugcraft_exchange_model_"+design.id);
                    server.computeOnServer(mc->{((PokerTableBlockEntity)mc.overworld().getBlockEntity(at)).open(mc.getPlayerList().getPlayers().getFirst());return true;});
                    context.waitForScreen(PokerScreen.class);context.waitTicks(5);context.takeScreenshot("jugcraft_exchange_screen_"+design.id);
                    context.runOnClient(c->c.gui.setScreen(null));
                    server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x-5,y,z-12,x+9,y+8,z));
                }
                server.computeOnServer(mc->{var p=mc.getPlayerList().getPlayers().getFirst();var ledger=Ledger.get(mc);ledger.book().grant(p.getUUID(),123,FinanceService.day(mc));ledger.setDirty();return true;});
            }
            io.github.jimbozoomer.jugcraft.test.InProcessServerConfigs.beforeReconnect(server);
            try(var connection=server.connect()) {
                connection.waitForChunksRender();context.getInput().pressKey(FinanceClient.openExchange);context.waitForScreen(ExchangeScreen.class);
                check(server.computeOnServer(mc->Ledger.get(mc).book().cash(mc.getPlayerList().getPlayers().getFirst().getUUID())>=123),"balance survived reconnect");
                context.takeScreenshot("jugcraft_exchange_reconnected");context.runOnClient(c->c.gui.setScreen(null));
            }
        }
    }
}
