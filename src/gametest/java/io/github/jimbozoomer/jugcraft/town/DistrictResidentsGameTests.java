package io.github.jimbozoomer.jugcraft.town;

import java.util.HashSet;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public final class DistrictResidentsGameTests {
    @GameTest public void permanentPostsHaveDistinctIdentitiesAndSafeOriginalPatrols(GameTestHelper h) {
        var origin=new BlockPos(100,67,200);
        var ids=new HashSet<UUID>();
        var town=TownData.get();
        for(var p:DistrictResidents.POSTS) {
            h.assertTrue(p==DistrictResidents.post(p.id()),"Stable saved post number "+p.name());
            var id=DistrictResidents.identity(p,origin,"first");
            h.assertTrue(ids.add(id),"Unique post identity");
            h.assertTrue(p.original()==id.equals(DistrictResidents.identity(p,origin,"second")),"Only the old-city residents survive waterfront replacement");
            if(p.original()) for(var at:java.util.List.of(p.local(),p.local().offset(p.patrol()))) {
                h.assertTrue(town.state(town.index(at.getX(),at.getY(),at.getZ())).isAir() &&
                    town.state(town.index(at.getX(),at.getY()+1,at.getZ())).isAir(),"Clear original-city patrol at "+at);
                h.assertTrue(!town.state(town.index(at.getX(),at.getY()-1,at.getZ())).isAir(),"Original road at "+at);
            }
        }
        h.succeed();
    }

    /** Called after the full production installer has opened rentals on the disposable world. */
    public static void installed(ServerLevel level,ServerPlayer player,BlockPos origin,String token) {
        var state=TownState.get(level);
        DistrictResidents.maintain(level);
        DistrictResidents.maintain(level);
        for(var p:DistrictResidents.POSTS) if(!p.original() && (p.id()<=19 || WesternDistrict.guildShopsOpen(level))) {
            var id=DistrictResidents.identity(p,origin,token);
            if(!(level.getEntity(id) instanceof Townsfolk npc) || !id.equals(state.townsperson(p.rosterKey())) ||
                    !DistrictResidents.belongsToInstallation(npc,origin,token))
                throw new AssertionError("Missing installed resident "+p.name()+" at "+origin.offset(p.local()));
            if(!DistrictResidents.canStand(level,origin.offset(p.local().offset(p.patrol()))))
                throw new AssertionError("Blocked post or patrol destination for "+p.name());
        }
        var manager=(Townsfolk)level.getEntity(DistrictResidents.identity(DistrictResidents.post(8),origin,token));
        try {
            var server=level.getServer();
            var commands=server.getCommands().getDispatcher();
            var source=player.createCommandSourceStack().withSuppressedOutput();
            player.setGameMode(GameType.SURVIVAL);
            player.snapTo(manager.position());
            if(DistrictResidents.managerNear(player)!=DistrictResidents.post(8) || ApartmentCommands.desk(player,1)!=1)
                throw new AssertionError("Brick Court lobby ledger unavailable");
            Jugs.add(server,player.getUUID(),24);
            long start=Jugs.balance(server,player.getUUID());
            String room="west_1_7L1";
            if(commands.execute("apartment rent west_2_6L1",source)!=0 || Jugs.balance(server,player.getUUID())!=start)
                throw new AssertionError("Manager leased a room in a different building");
            if(commands.execute("apartment rent "+room,source)!=1 || Jugs.balance(server,player.getUUID())>=start)
                throw new AssertionError("Could not lease an upper-floor room from its lobby");
            if(commands.execute("apartment leave "+room,source)!=1) throw new AssertionError("Could not end test tenancy");
        } catch(com.mojang.brigadier.exceptions.CommandSyntaxException e) { throw new AssertionError(e); }
        finally { player.setGameMode(GameType.SPECTATOR); }
    }

    public static void guildKeepers(ServerLevel level,BlockPos origin,String token,boolean open) {
        if (WesternDistrict.guildShopsOpen(level)!=open) throw new AssertionError("Incorrect shop opening state");
        DistrictResidents.maintain(level);
        DistrictResidents.maintain(level);
        var area=new net.minecraft.world.phys.AABB(
            net.minecraft.world.phys.Vec3.atLowerCornerOf(origin.offset(44,-96,43)),
            net.minecraft.world.phys.Vec3.atLowerCornerOf(origin.offset(131,-26,129)));
        for(int id : new int[]{20,21}) {
            var p=DistrictResidents.post(id);
            var identity=DistrictResidents.identity(p,origin,token);
            var crowd=level.getEntitiesOfClass(DeepsteadDwarf.class,area,n->n.civicPost==p.id());
            if(!open) {
                if(!crowd.isEmpty() || level.getEntity(identity)!=null || TownState.get(level).townsperson(p.rosterKey())!=null)
                    throw new AssertionError("Guild merchant appeared in unfinished shop "+p.name());
                continue;
            }
            var expected=id==20 ? DeepsteadWares.STONEWRIGHT : DeepsteadWares.COPPER_KETTLE;
            if(crowd.size()!=1 || !crowd.getFirst().getUUID().equals(identity) || !crowd.getFirst().shop().equals(expected)
                    || !crowd.getFirst().home().equals(origin.offset(p.local())) || !crowd.getFirst().wearsGuildColors(expected)
                    || !identity.equals(TownState.get(level).townsperson(p.rosterKey())))
                throw new AssertionError("Expected exactly one "+expected+" at "+origin.offset(p.local()));
        }
    }

    public static int guildKeeperId(ServerLevel level,BlockPos origin,String token,int postId) {
        var actor=level.getEntity(DistrictResidents.identity(DistrictResidents.post(postId),origin,token));
        if(!(actor instanceof DeepsteadDwarf)) throw new AssertionError("Missing guild dwarf "+postId);
        return actor.getId();
    }

    public static void buyFromGuildKeeper(ServerLevel level,ServerPlayer player,BlockPos origin,String token,int postId) {
        var p=DistrictResidents.post(postId);
        if(!(level.getEntity(DistrictResidents.identity(p,origin,token)) instanceof DeepsteadDwarf merchant))
            throw new AssertionError("Missing guild merchant "+p.name());
        var customer=origin.offset(postId==20?new BlockPos(91,-81,56):new BlockPos(87,-67,118));
        player.setGameMode(GameType.SURVIVAL);
        try {
            player.snapTo(net.minecraft.world.phys.Vec3.atBottomCenterOf(customer));
            if(!DistrictResidents.canStand(level,customer) || !player.hasLineOfSight(merchant))
                throw new AssertionError("Customer cannot reach or see "+p.name()+" across the counter");
            merchant.talkTo(player,net.minecraft.world.InteractionHand.MAIN_HAND);
            var key=postId==20?DeepsteadWares.STONEWRIGHT:DeepsteadWares.COPPER_KETTLE;
            if(!(player.containerMenu instanceof ShopMenu menu) || !key.equals(menu.opening.shop())
                    || menu.opening.keeper()!=merchant.getId() || menu.sales().isEmpty())
                throw new AssertionError("Speaking to "+p.name()+" did not open their stocked counter");
            var offer=menu.sales().getFirst();
            Jugs.add(level.getServer(),player.getUUID(),offer.price());
            long balance=Jugs.balance(level.getServer(),player.getUUID());
            int count=player.getInventory().countItem(offer.itemType());
            if(!menu.clickMenuButton(player,0) || Jugs.balance(level.getServer(),player.getUUID())!=balance-offer.price()
                    || player.getInventory().countItem(offer.itemType())!=count+offer.count())
                throw new AssertionError("Purchase at "+p.name()+" failed to debit and deliver exactly once");
        } finally {
            player.closeContainer();
            player.setGameMode(GameType.SPECTATOR);
        }
    }
    public static void removed(ServerLevel level,BlockPos origin,String token) {
        for(var p:DistrictResidents.POSTS) if(!p.original() &&
            (level.getEntity(DistrictResidents.identity(p,origin,token))!=null || TownState.get(level).townsperson(p.rosterKey())!=null))
            throw new AssertionError("Retired resident still present: "+p.name());
    }
}
