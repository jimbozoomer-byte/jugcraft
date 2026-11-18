package io.github.jimbozoomer.jugcraft.town;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;

/** Permanent posts. Numbered identities are saved independently of the old blueprint's indexed townsfolk. */
public final class DistrictResidents {
    enum Job { OLD_WATCH, WATCH, MANAGER, MINER, MERCHANT, DWARF_WATCH }
    record Post(int id, Job job, String name, int variant, BlockPos local, BlockPos patrol, String guild) {
        int rosterKey() { return -1000-id; }
        boolean original() { return job==Job.OLD_WATCH; }
    }
    private static Post post(int id, Job job, String name, int variant, int x,int y,int z) {
        return new Post(id,job,name,variant,new BlockPos(x,y,z),BlockPos.ZERO,null);
    }
    private static Post patrol(int id, Job job,String name,int variant,int x,int y,int z,int dx,int dz) {
        return new Post(id,job,name,variant,new BlockPos(x,y,z),new BlockPos(dx,0,dz),null);
    }
    private static Post guild(int id,String name,int variant,String site,String shop) {
        return new Post(id,Job.MERCHANT,name,variant,DeepsteadShopPlan.get().shops.get(site).merchant(),BlockPos.ZERO,shop);
    }
    static final List<Post> POSTS=List.of(
        patrol(1,Job.OLD_WATCH,"Market Watch · Signe",0,68,1,96,12,0),
        patrol(2,Job.OLD_WATCH,"East Street Watch · Ivar",1,128,1,96,12,0),
        patrol(3,Job.WATCH,"Quay Gate Watch · Ulf",0,-171,-47,96,15,0),
        patrol(4,Job.WATCH,"Bridge Watch · Astrid",1,-60,-11,96,15,0),
        patrol(5,Job.WATCH,"Exchange Watch · Einar",0,-105,-29,96,11,0),
        patrol(6,Job.WATCH,"Customs Watch · Liv",1,-237,-49,95,0,12),
        patrol(7,Job.WATCH,"Dock Watch · Nils",0,-235,-49,119,0,13),
        post(8,Job.MANAGER,"Ingrid · Brick Court Manager",1,-164,-47,57),
        post(9,Job.MANAGER,"Erik · Bell Court Manager",2,-111,-29,54),
        post(10,Job.MANAGER,"Hilda · Timber Court Manager",3,-56,-11,74),
        post(11,Job.MINER,"Runa Copperbraid",0,121,-53,73),
        post(12,Job.MINER,"Borin Coalhand",1,124,-67,84),
        post(13,Job.MINER,"Yrsa Flintcutter",2,122,-81,96),
        post(14,Job.MINER,"Orrik Chainkeeper",3,55,-81,84),
        post(15,Job.MERCHANT,"Brynja · Deepstead Lampwright",0,64,-67,62),
        post(16,Job.MERCHANT,"Torvi · Miners’ Outfitter",1,106,-67,62),
        patrol(17,Job.DWARF_WATCH,"High Gallery Watch · Haldor",0,87,-53,102,0,10),
        patrol(18,Job.DWARF_WATCH,"Market Watch · Inga",1,62,-67,86,10,0),
        patrol(19,Job.DWARF_WATCH,"Waterworks Watch · Dagr",2,110,-81,86,4,0),
        guild(20,"Dagna · Guild Stonewright",2,"stonewright",DeepsteadWares.STONEWRIGHT),
        guild(21,"Bruni · Copper Kettle Keeper",1,"copper_kettle",DeepsteadWares.COPPER_KETTLE));

    private DistrictResidents() {}
    static Post post(int id) {
        return id>0 && id<=POSTS.size()?POSTS.get(id-1):null;
    }
    static UUID identity(Post p, BlockPos origin, String installation) {
        String key="jugcraft/city-resident/"+origin.asLong()+"/"+(p.original()?"old-city":installation)+"/"+p.id;
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
    }
    static void register() {
        ServerTickEvents.END_SERVER_TICK.register(s->{if(s.getTickCount()%100==0) maintain(s.overworld());});
        ServerEntityEvents.ENTITY_UNLOAD.register((e,level)->{
            if(e instanceof Townsfolk person && person.civicPost!=0 && e.getRemovalReason()!=null && e.getRemovalReason().shouldDestroy()) {
                var p=post(person.civicPost);
                if(p!=null) TownState.get(level).removeTownsperson(p.rosterKey(),e.getUUID());
            }
        });
    }
    static void maintain(ServerLevel level) {
        var state=TownState.get(level);
        var origin=state.origin();
        if(origin==null) return;
        var installation=WesternDistrict.populatedInstallation(level);
        for(var p:POSTS) {
            var at=origin.offset(p.local);
            if(!level.isPositionEntityTicking(at)) continue;
            if(p.original() ? !state.built(ChunkPos.containing(at).pack()) : installation==null) continue;
            if(p.guild!=null && !WesternDistrict.guildShopsOpen(level)) continue;
            UUID id=identity(p,origin,installation);
            if(id.equals(state.townsperson(p.rosterKey()))) continue; // Includes residents in unloaded chunks.
            if(level.getEntity(id) instanceof Townsfolk existing && existing.civicPost==p.id) {
                state.setTownsperson(p.rosterKey(),id);
            } else if(canStand(level,at)) {
                var person=create(level,p,at);
                if(person==null) continue;
                person.setUUID(id);
                person.civicPost=p.id;
                person.snapTo(at.getX()+0.5,at.getY(),at.getZ()+0.5,p.job==Job.MERCHANT?180:0,0);
                if(level.noCollision(person) && level.getEntities(person,person.getBoundingBox(),
                        e->e instanceof net.minecraft.world.entity.LivingEntity && !e.isSpectator()).isEmpty()
                        && level.addFreshEntity(person)) state.setTownsperson(p.rosterKey(),id);
            }
        }
    }
    static boolean canStand(ServerLevel level,BlockPos at) {
        return level.getBlockState(at.below()).isFaceSturdy(level,at.below(),Direction.UP)
            && level.getBlockState(at).getCollisionShape(level,at).isEmpty()
            && level.getBlockState(at.above()).getCollisionShape(level,at.above()).isEmpty()
            && level.getFluidState(at).isEmpty() && level.getFluidState(at.above()).isEmpty();
    }
    private static Townsfolk create(ServerLevel level,Post p,BlockPos at) {
        if(p.job==Job.MINER || p.job==Job.MERCHANT || p.job==Job.DWARF_WATCH) {
            var dwarf=DeepsteadDwarf.TYPE.create(level,EntitySpawnReason.STRUCTURE);
            if(dwarf!=null) dwarf.configure(switch(p.job) {
                case MINER -> DeepsteadDwarf.Role.MINER; case MERCHANT -> DeepsteadDwarf.Role.MERCHANT;
                default -> DeepsteadDwarf.Role.GUARD;
            },p.name,at,p.variant,p.guild);
            return dwarf;
        }
        var human=JugcraftTown.TOWNSFOLK.create(level,EntitySpawnReason.STRUCTURE);
        if(human!=null) {
            boolean manager=p.job==Job.MANAGER;
            String skin=manager ? switch(p.variant){case 1 -> "banker"; case 2 -> "innkeeper"; default -> "baker";} :
                p.variant==0?"guard_blue":"guard_red";
            human.setUp(new TownData.Spot(-1,manager?"apartment_manager":"guard",null,p.name,skin,p.local),at);
            if(manager) human.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.WRITABLE_BOOK));
        }
        return human;
    }
    static boolean belongsToInstallation(Entity e,BlockPos origin,String token) {
        if(!(e instanceof Townsfolk person)) return false;
        var p=post(person.civicPost);
        return p!=null && !p.original() && identity(p,origin,token).equals(e.getUUID());
    }
    static void withdraw(ServerLevel level,BlockPos origin,String token) {
        var state=TownState.get(level);
        for(var p:POSTS) if(!p.original()) {
            UUID id=identity(p,origin,token);
            var entity=level.getEntity(id);
            if(belongsToInstallation(entity,origin,token)) entity.discard();
            state.removeTownsperson(p.rosterKey(),id);
        }
    }
    /** Loaded later, or moved across dimensions: retire only an entity carrying a managed post. */
    static boolean retireIfStale(ServerLevel level,Townsfolk person) {
        var p=post(person.civicPost);
        if(p==null) return false;
        var origin=TownState.get(level).origin();
        String token=p.original()?"":WesternDistrict.populatedInstallation(level);
        if(level==level.getServer().overworld() && origin!=null && token!=null && identity(p,origin,token).equals(person.getUUID())) {
            if(p.guild==null || WesternDistrict.guildShopsOpen(level)) return false;
            TownState.get(level).removeTownsperson(p.rosterKey(),person.getUUID());
        }
        person.discard();
        return true;
    }
    static BlockPos patrolAnchor(Townsfolk person,Post p) {
        boolean change=((person.level().getGameTime()/240+p.id)&1)==1;
        return change?person.home().offset(p.patrol):person.home();
    }
    static Post managerNear(ServerPlayer player) {
        if(player.level()!=player.level().getServer().overworld()) return null;
        var level=player.level();
        var origin=TownState.get(level).origin();
        var token=WesternDistrict.populatedInstallation(level);
        if(origin==null || token==null) return null;
        for(var p:POSTS) if(p.job==Job.MANAGER && level.getEntity(identity(p,origin,token)) instanceof Townsfolk m
                && m.civicPost==p.id && m.isAlive() && m.distanceToSqr(player)<=25 && player.hasLineOfSight(m)) return p;
        return null;
    }
}
