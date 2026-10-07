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

/** Home, four jobs, lunch, supply and output links. No chunk loads. */
public final class CompanionAssignments {
    public record Target(GlobalPos at,Identifier block,Direction face){
        public Target(GlobalPos at,Identifier block){this(at,block,Direction.UP);}
        public String name(){return BuiltInRegistries.BLOCK.getValue(block).getName().getString();}
        public boolean local(Level level){return at.dimension().equals(level.dimension());}
        public boolean present(Level level){return local(level)&&level.hasChunkAt(at.pos())&&BuiltInRegistries.BLOCK.getKey(level.getBlockState(at.pos()).getBlock()).equals(block);}
    }
    private final PeepoEntity npc;
    public static final int LUNCH=5,SUPPLY=6,OUTPUT=7,COUNT=8;
    private final Target[] targets=new Target[COUNT];
    private boolean homeManaged,workManaged;
    private String cached="";private List<Target> clientTargets=Collections.nCopies(COUNT,null);
    CompanionAssignments(PeepoEntity npc){this.npc=npc;}
    public Target get(int slot){return targets[slot];}
    public boolean homeManaged(){return homeManaged;}
    public boolean workManaged(){return workManaged;}
    public static BlockPos canonical(Level level,BlockPos pos){
        var state=level.getBlockState(pos);
        if(state.getBlock() instanceof ChestBlock && state.getValue(ChestBlock.TYPE)==ChestType.RIGHT)return pos.relative(ChestBlock.getConnectedDirection(state));
        if(state.getBlock() instanceof WheelBlock)return WheelBlock.master(pos,state);
        if(state.getBlock() instanceof MachineBlock machine)return machine.masterPos(pos,state);
        if(state.getBlock() instanceof BedBlock && state.getValue(BlockStateProperties.BED_PART)==BedPart.FOOT)return pos.relative(state.getValue(BlockStateProperties.HORIZONTAL_FACING));
        return pos;
    }
    public static boolean bed(Level level,BlockPos pos){var b=level.getBlockState(pos).getBlock();return b instanceof CompanionBedBlock || b instanceof BedBlock;}
    public static boolean work(Level level,BlockPos pos){var be=level.getBlockEntity(pos);return be instanceof WheelBlockEntity || be instanceof MachineBlockEntity || be instanceof CookingPotBlockEntity || be instanceof io.github.jimbozoomer.jugcraft.agriculture.CiderPressBlockEntity || be instanceof io.github.jimbozoomer.jugcraft.agriculture.CanningKettleBlockEntity || be instanceof io.github.jimbozoomer.jugcraft.kinetic.HandCrankBlockEntity;}
    public String assignContainer(Level level,BlockPos pos,Direction face,boolean output){
        if(!level.hasChunkAt(pos) || pos.distToCenterSqr(npc.position())>64*64)return "Keep a loaded container within 64 blocks of the companion.";
        var target=new Target(GlobalPos.of(level.dimension(),pos),BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()),face);
        var storage=CompanionStorage.find(npc,target);
        if(storage==null || (output?!storage.supportsInsertion():!storage.supportsExtraction()))return "This container face is unavailable, locked, or does not support that transfer.";
        for(int i=0;i<COUNT;i++)if(i!=(output?OUTPUT:SUPPLY) && targets[i]!=null && targets[i].at.equals(target.at))return "This container already has another role for this companion.";
        targets[output?OUTPUT:SUPPLY]=target;changed();
        return (output?"Output":"Supply")+" container assigned: "+target.name();
    }
    public String assign(Level level,BlockPos clicked){
        var pos=canonical(level,clicked);
        if(!level.hasChunkAt(pos))return "That target is not loaded.";
        boolean home=bed(level,pos);
        boolean lunch=level.getBlockEntity(pos) instanceof LunchBlockEntity;
        if(lunch && !((LunchBlockEntity)level.getBlockEntity(pos)).feeds(npc))return "This lunch source is locked or does not allow this companion.";
        if(!home && !lunch && !work(level,pos))return "Choose a bed, workstation, lunch crate, or lunch cover.";
        if(pos.distToCenterSqr(npc.position())>64*64)return "Keep assignments within 64 blocks of the companion.";
        var at=GlobalPos.of(level.dimension(),pos);
        for(var t:targets)if(t!=null && t.at.equals(at))return "Already assigned to this companion.";
        int slot=0;
        if(lunch)slot=LUNCH;
        else if(!home){slot=1;while(slot<5 && targets[slot]!=null)slot++;if(slot==5)return "All four work slots are full. Left-click an assigned station to remove it.";}
        targets[slot]=new Target(at,BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()));
        if(home)homeManaged=true;if(!lunch)workManaged=true;
        changed();
        if(lunch){npc.report.lunch(CompanionStatus.READY);return "Lunch source assigned: "+targets[slot].name();}
        npc.orders.assigned(home,at);
        return (home?"Home assigned: ":"Work "+slot+" assigned: ")+targets[slot].name()+(home || CompanionJobs.resolve(npc,pos)!=null || CompanionLogistics.resolve(npc,targets[slot])!=null?"":" (work behavior not implemented yet)");
    }
    public String remove(Level level,BlockPos clicked){
        var pos=canonical(level,clicked);var at=GlobalPos.of(level.dimension(),pos);
        for(int i=0;i<COUNT;i++)if(targets[i]!=null && targets[i].at.equals(at)){clear(i);return i==0?"Home removed.":i==LUNCH?"Lunch source removed.":i==SUPPLY?"Supply removed.":i==OUTPUT?"Output removed.":"Work assignment removed.";}
        return "This block is not assigned to the selected companion.";
    }
    public void clear(int slot){
        if(slot<0 || slot>=COUNT || targets[slot]==null)return;
        targets[slot]=null;if(slot>0 && slot<LUNCH)compactWork();changed();if(slot<LUNCH)npc.orders.assignmentRemoved(slot==0);
        if(slot==LUNCH)npc.report.lunch(CompanionStatus.READY);
    }
    private void compactWork(){
        int next=1;
        for(int i=1;i<5;i++)if(targets[i]!=null)targets[next++]=targets[i];
        while(next<5)targets[next++]=null;
    }
    public boolean moveWork(int slot,int direction){
        if(npc.level().isClientSide() || slot<1 || slot>4 || Math.abs(direction)!=1)return false;
        int other=slot+direction;
        if(other<1 || other>4 || targets[slot]==null || targets[other]==null)return false;
        var swap=targets[slot];targets[slot]=targets[other];targets[other]=swap;
        changed();npc.orders.workReordered();return true;
    }
    public int workPriority(BlockPos pos){
        for(int i=1;i<5;i++)if(targets[i]!=null && targets[i].local(npc.level()) && targets[i].at.pos().equals(pos))return i;
        return 5;
    }
    private void changed(){npc.syncAssignments(encode());}
    public List<BlockPos> loadedStations(){
        var result=new ArrayList<BlockPos>(5);
        for(int i=0;i<5;i++){var t=targets[i];if(t!=null && t.present(npc.level()))result.add(t.at.pos());}
        return result;
    }
    public boolean assignedWork(BlockPos pos){for(int i=1;i<5;i++)if(targets[i]!=null && targets[i].present(npc.level()) && targets[i].at.pos().equals(pos))return true;return false;}
    public boolean foodNear(Vec3 point,int radius){for(var t:targets)if(t!=null && t.local(npc.level()) && t.at.pos().distToCenterSqr(point)<=radius*radius)return true;return false;}
    public Vec3 homeApproach(){return approach(targets[0]);}
    public Vec3 workApproach(){
        for(int i=1;i<5;i++){
            var t=targets[i];if(t==null || !t.present(npc.level()))continue;
            var job=CompanionJobs.resolve(npc,t.at.pos());
            if(job==null || !CompanionJobs.permitted(npc,t.at.pos()) || job.workStatus(npc)!=CompanionStatus.READY || !npc.isUsingJobAt(t.at.pos()) && !job.worthStarting(npc))continue;
            return job.approachPosition();
        }
        return null;
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
        for(int i=0;i<COUNT;i++)if(targets[i]!=null){var child=out.child("Assignment"+i);child.store("At",GlobalPos.CODEC,targets[i].at);child.putString("Block",targets[i].block.toString());child.putInt("Face",targets[i].face.ordinal());}
    }
    public void load(ValueInput in){
        homeManaged=in.getBooleanOr("AssignedHomeManaged",false);workManaged=in.getBooleanOr("AssignedWorkManaged",false);
        Arrays.fill(targets,null);
        for(int i=0;i<COUNT;i++){
            var child=in.child("Assignment"+i);if(child.isEmpty())continue;
            var at=child.get().read("At",GlobalPos.CODEC).orElse(null);var id=Identifier.tryParse(child.get().getStringOr("Block",""));
            if(at!=null && id!=null)targets[i]=new Target(at,id,Direction.values()[Math.clamp(child.get().getIntOr("Face",Direction.UP.ordinal()),0,5)]);
        }
        compactWork();changed();
    }
    private String encode(){
        var array=new JsonArray();
        for(var t:targets){if(t==null){array.add(JsonNull.INSTANCE);continue;}var o=new JsonObject();o.addProperty("d",t.at.dimension().identifier().toString());o.addProperty("p",t.at.pos().asLong());o.addProperty("b",t.block.toString());o.addProperty("f",t.face.ordinal());array.add(o);}
        return array.toString();
    }
    public List<Target> view(){
        if(!npc.level().isClientSide())return Arrays.asList(targets);
        String data=npc.assignmentData();if(data.equals(cached))return clientTargets;cached=data;
        var list=new ArrayList<Target>(Collections.nCopies(COUNT,null));
        try{var array=JsonParser.parseString(data).getAsJsonArray();for(int i=0;i<Math.min(COUNT,array.size());i++)if(array.get(i).isJsonObject()){
            var o=array.get(i).getAsJsonObject();var dim=ResourceKey.create(Registries.DIMENSION,Identifier.parse(o.get("d").getAsString()));
            list.set(i,new Target(GlobalPos.of(dim,BlockPos.of(o.get("p").getAsLong())),Identifier.parse(o.get("b").getAsString()),Direction.values()[Math.clamp(o.has("f")?o.get("f").getAsInt():Direction.UP.ordinal(),0,5)]));
        }}catch(RuntimeException ignored){}clientTargets=Collections.unmodifiableList(list);return clientTargets;
    }
}
