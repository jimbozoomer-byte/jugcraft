package local.peepo;

import java.util.*;
import com.google.gson.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import io.github.jimbozoomer.jugcraft.machine.*;
import io.github.jimbozoomer.jugcraft.agriculture.CookingPotBlockEntity;

/** Five bounded, persistent links. No chunk loads; block identities detect removed/replaced targets. */
public final class CompanionAssignments {
    public record Target(GlobalPos at,Identifier block){
        public String name(){return BuiltInRegistries.BLOCK.getValue(block).getName().getString();}
        public boolean local(Level level){return at.dimension().equals(level.dimension());}
        public boolean present(Level level){return local(level)&&level.hasChunkAt(at.pos())&&BuiltInRegistries.BLOCK.getKey(level.getBlockState(at.pos()).getBlock()).equals(block);}
    }
    private final PeepoEntity npc;
    private final Target[] targets=new Target[5];
    private boolean homeManaged,workManaged;
    private String cached="";private List<Target> clientTargets=Collections.nCopies(5,null);
    CompanionAssignments(PeepoEntity npc){this.npc=npc;}
    public Target get(int slot){return targets[slot];}
    public boolean homeManaged(){return homeManaged;}
    public boolean workManaged(){return workManaged;}
    public static BlockPos canonical(Level level,BlockPos pos){
        var state=level.getBlockState(pos);
        if(state.getBlock() instanceof WheelBlock)return WheelBlock.master(pos,state);
        if(state.getBlock() instanceof MachineBlock machine)return machine.masterPos(pos,state);
        if(state.getBlock() instanceof BedBlock && state.getValue(BlockStateProperties.BED_PART)==BedPart.FOOT)return pos.relative(state.getValue(BlockStateProperties.HORIZONTAL_FACING));
        return pos;
    }
    public static boolean bed(Level level,BlockPos pos){var b=level.getBlockState(pos).getBlock();return b instanceof CompanionBedBlock || b instanceof BedBlock;}
    public static boolean work(Level level,BlockPos pos){var be=level.getBlockEntity(pos);return be instanceof WheelBlockEntity || be instanceof MachineBlockEntity || be instanceof CookingPotBlockEntity;}
    public String assign(Level level,BlockPos clicked){
        var pos=canonical(level,clicked);
        if(!level.hasChunkAt(pos))return "That target is not loaded.";
        boolean home=bed(level,pos);
        if(!home && !work(level,pos))return "Choose a bed, Generator Wheel, machine, or cooking pot.";
        if(pos.distToCenterSqr(npc.position())>64*64)return "Keep assignments within 64 blocks of the companion.";
        var at=GlobalPos.of(level.dimension(),pos);
        for(var t:targets)if(t!=null && t.at.equals(at))return "Already assigned to this companion.";
        int slot=0;
        if(!home){slot=1;while(slot<5 && targets[slot]!=null)slot++;if(slot==5)return "All four work slots are full. Left-click an assigned station to remove it.";}
        targets[slot]=new Target(at,BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()));
        if(home)homeManaged=true;workManaged=true;
        changed();npc.orders.assigned(home,at);
        return (home?"Home assigned: ":"Work "+slot+" assigned: ")+targets[slot].name()+(home || level.getBlockEntity(pos) instanceof WheelBlockEntity?"":" (work behavior not implemented yet)");
    }
    public String remove(Level level,BlockPos clicked){
        var pos=canonical(level,clicked);var at=GlobalPos.of(level.dimension(),pos);
        for(int i=0;i<5;i++)if(targets[i]!=null && targets[i].at.equals(at)){clear(i);return i==0?"Home removed.":"Work assignment removed.";}
        return "This block is not assigned to the selected companion.";
    }
    public void clear(int slot){if(slot<0 || slot>=5 || targets[slot]==null)return;targets[slot]=null;changed();npc.orders.assignmentRemoved(slot==0);}
    private void changed(){npc.syncAssignments(encode());}
    public List<BlockPos> loadedStations(){
        var result=new ArrayList<BlockPos>(5);
        for(var t:targets)if(t!=null && t.present(npc.level()))result.add(t.at.pos());
        return result;
    }
    public boolean assignedWork(BlockPos pos){for(int i=1;i<5;i++)if(targets[i]!=null && targets[i].present(npc.level()) && targets[i].at.pos().equals(pos))return true;return false;}
    public boolean foodNear(Vec3 point,int radius){for(var t:targets)if(t!=null && t.local(npc.level()) && t.at.pos().distToCenterSqr(point)<=radius*radius)return true;return false;}
    public Vec3 homeApproach(){return approach(targets[0]);}
    public Vec3 workApproach(){
        Vec3 best=null;double distance=Double.MAX_VALUE;
        for(int i=1;i<5;i++){
            var t=targets[i];if(t==null || !t.present(npc.level()))continue;
            // Only wheel assignments currently have a runnable companion job.
            if(!(npc.level().getBlockEntity(t.at.pos()) instanceof WheelBlockEntity wheel) || wheel.energySpace()==0 || !wheel.availableTo(npc))continue;
            var at=wheel.approachPosition();double d=npc.position().distanceToSqr(at);if(d<distance){best=at;distance=d;}
        }
        return best;
    }
    private Vec3 approach(Target target){
        if(target==null || !target.present(npc.level()))return null;
        var pos=target.at.pos();var be=npc.level().getBlockEntity(pos);
        if(be instanceof CompanionBedEntity bed)return bed.entrance();
        if(be instanceof CompanionStation station)return station.approachPosition();
        var vanilla=AssignedVanillaBed.create(npc,pos);return vanilla==null?null:vanilla.approachPosition();
    }
    public void save(ValueOutput out){
        out.putBoolean("AssignedHomeManaged",homeManaged);out.putBoolean("AssignedWorkManaged",workManaged);
        for(int i=0;i<5;i++)if(targets[i]!=null){var child=out.child("Assignment"+i);child.store("At",GlobalPos.CODEC,targets[i].at);child.putString("Block",targets[i].block.toString());}
    }
    public void load(ValueInput in){
        homeManaged=in.getBooleanOr("AssignedHomeManaged",false);workManaged=in.getBooleanOr("AssignedWorkManaged",false);
        Arrays.fill(targets,null);
        for(int i=0;i<5;i++){
            var child=in.child("Assignment"+i);if(child.isEmpty())continue;
            var at=child.get().read("At",GlobalPos.CODEC).orElse(null);var id=Identifier.tryParse(child.get().getStringOr("Block",""));
            if(at!=null && id!=null)targets[i]=new Target(at,id);
        }
        changed();
    }
    private String encode(){
        var array=new JsonArray();
        for(var t:targets){if(t==null){array.add(JsonNull.INSTANCE);continue;}var o=new JsonObject();o.addProperty("d",t.at.dimension().identifier().toString());o.addProperty("p",t.at.pos().asLong());o.addProperty("b",t.block.toString());array.add(o);}
        return array.toString();
    }
    public List<Target> view(){
        if(!npc.level().isClientSide())return Arrays.asList(targets);
        String data=npc.assignmentData();if(data.equals(cached))return clientTargets;cached=data;
        var list=new ArrayList<Target>(Collections.nCopies(5,null));
        try{var array=JsonParser.parseString(data).getAsJsonArray();for(int i=0;i<Math.min(5,array.size());i++)if(array.get(i).isJsonObject()){
            var o=array.get(i).getAsJsonObject();var dim=ResourceKey.create(Registries.DIMENSION,Identifier.parse(o.get("d").getAsString()));
            list.set(i,new Target(GlobalPos.of(dim,BlockPos.of(o.get("p").getAsLong())),Identifier.parse(o.get("b").getAsString())));
        }}catch(RuntimeException ignored){}clientTargets=Collections.unmodifiableList(list);return clientTargets;
    }
}
