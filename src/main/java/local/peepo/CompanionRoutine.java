package local.peepo;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Bounded loaded-chunk station discovery; food goals can interrupt work/rest at any time. */
final class CompanionRoutine extends Goal {
    private final PeepoEntity npc;
    private CompanionStation station;
    private BlockEntity block;
    private long nextLeisure, chairUntil;
    private boolean active;
    private long nextSearch,deadline;
    private int repath;
    private final Map<BlockPos,Long> unreachable=new HashMap<>();
    CompanionRoutine(PeepoEntity npc) { this.npc=npc;nextSearch=npc.level().getGameTime()+Math.floorMod(npc.getId(),80);setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK)); }
    void resetOrders(){release();active=false;nextSearch=0;}
    boolean isActive() { return active; }
    @Override public boolean requiresUpdateEveryTick() { return true; }
    private boolean needsRest() { return npc.isRecovering() || npc.getEnergy()<npc.getEnergyCapacity()*95/100; }
    private boolean useful(CompanionStation s) {
        if(!npc.orders.station(s) || !s.availableTo(npc))return false;
        return switch(s.kind()) {
            case WHEEL -> !npc.isRecovering() && npc.getEnergy()>0 && s.energySpace()>0;
            case CHAIR -> needsRest() || npc.level().getGameTime() >= nextLeisure;
            case BED -> needsRest() && npc.isRestNight();
        };
    }
    private int rank(CompanionStation s) {
        if(s.kind()==CompanionStation.Kind.WHEEL)return 0;
        return s.kind()==CompanionStation.Kind.BED?1:2;
    }
    private int pathRange(){return npc.assignments.workManaged()||npc.assignments.homeManaged()?64:16;}
    private BlockPos positionOf(CompanionStation s){return s instanceof BlockEntity be?be.getBlockPos():s instanceof AssignedVanillaBed bed?bed.pos:((CompanionSeats.Surface)s).pos;}
    private void search() {
        long now=npc.level().getGameTime();
        if(now<nextSearch)return;
        nextSearch=now+80+Math.floorMod(npc.getId(),20);unreachable.entrySet().removeIf(e->e.getValue()<=now);
        List<CompanionStation> candidates=new ArrayList<>();
        for(var assigned:npc.assignments.loadedStations()){
            var be=npc.level().getBlockEntity(assigned);
            CompanionStation s=be instanceof CompanionStation station?station:AssignedVanillaBed.create(npc,assigned);
            if(s!=null && useful(s) && !unreachable.containsKey(assigned))candidates.add(s);
        }
        int cx=npc.blockPosition().getX()>>4,cz=npc.blockPosition().getZ()>>4;
        for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++) {
            var pos=new BlockPos((cx+dx)*16,npc.blockPosition().getY(),(cz+dz)*16);
            if(!npc.level().hasChunkAt(pos))continue;
            for(var stationPos:CompanionStationIndex.positions(npc.level(),pos)) {
                var be=npc.level().getBlockEntity(stationPos);
                if(be instanceof CompanionStation s && !be.isRemoved() && useful(s)
                        && npc.position().distanceToSqr(s.approachPosition())<=256
                        && !unreachable.containsKey(be.getBlockPos()) && !candidates.contains(s))candidates.add(s);
            }
        }
        candidates.sort(Comparator.<CompanionStation>comparingInt(this::rank)
            .thenComparingDouble(b->npc.position().distanceToSqr(b.approachPosition())));
        int stationPaths=0;
        for(var candidate:candidates) {
            if(stationPaths++>=2)break;
            var s=candidate;
            var path=npc.getNavigation().createPath(BlockPos.containing(s.approachPosition()),0,pathRange());
            if(path==null || !path.canReach()) { unreachable.put(positionOf(candidate),now+200);continue; }
            if(s.claim(npc)) {
                station=s;block=candidate instanceof BlockEntity be?be:null;deadline=now+600;chairUntil=now+600;repath=0;return;
            }
        }
        if (needsRest() || now >= nextLeisure) {
            var surface = CompanionSeats.find(npc, unreachable);
            if (surface != null && surface.claim(npc)) {
                station=surface;block=null;deadline=now+200;chairUntil=now+600;repath=0;
            }
        }
    }
    @Override public boolean canUse() {
        if(!npc.orders.routineAllowed())return false;
        if(npc.isEating() || npc.isWheelRunning() || npc.getRestMode()!=CompanionEnergy.Rest.NONE)return false;
        search();return station!=null || npc.isRecovering();
    }
    @Override public boolean canContinueToUse() { return npc.orders.routineAllowed() && !npc.isEating() && (station!=null || npc.isRecovering()); }
    @Override public void start() { active=true; }
    private void release() {
        if(station!=null)station.release(npc);
        station=null;block=null;npc.setWheelRunning(false);npc.setRestMode(CompanionEnergy.Rest.NONE);
        npc.getNavigation().stop();
    }
    @Override public void stop() { release();active=false; }
    @Override public void tick() {
        if(station!=null && station.kind()==CompanionStation.Kind.CHAIR && !npc.isRecovering()
                && npc.level().getGameTime()>=chairUntil) {
            nextLeisure=npc.level().getGameTime()+600;release();
        }
        if(station!=null && ((block!=null && (block.isRemoved() || !npc.level().hasChunkAt(block.getBlockPos())
                || npc.level().getBlockEntity(block.getBlockPos())!=block)) || !useful(station)))release();
        if(station==null)search();
        if(station==null) { npc.getNavigation().stop();return; }
        var target=station.approachPosition();
        if(npc.position().distanceToSqr(target)>.64) {
            if(npc.level().getGameTime()>deadline) {
                unreachable.put(positionOf(station),npc.level().getGameTime()+200);release();return;
            }
            if(--repath<=0) { repath=40;npc.getNavigation().moveTo(npc.getNavigation().createPath(BlockPos.containing(target),0,pathRange()),1); }
            return;
        }
        npc.getNavigation().stop();
        if(!station.occupy(npc)) { release();return; }
        switch(station.kind()) {
            case WHEEL -> { npc.setRestMode(CompanionEnergy.Rest.NONE);if(CompanionWork.transfer(npc,station)==0)release(); }
            case CHAIR -> npc.setRestMode(CompanionEnergy.Rest.SITTING);
            case BED -> npc.setRestMode(CompanionEnergy.Rest.SLEEPING);
        }
    }
}
