package io.github.jimbozoomer.jugcraft.town;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.pets.Pets;
import java.util.EnumSet;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/** Town residents of the mine galleries. Their posts, rather than the surface gates, bound their shifts. */
public final class DeepsteadDwarf extends Townsfolk {
    public enum Role {
        MINER("deepstead_miner"), MERCHANT("deepstead_merchant"), GUARD("deepstead_guard");
        final String id;
        Role(String id) { this.id=id; }
    }
    public static EntityType<DeepsteadDwarf> TYPE;
    private int talkingUntil;
    private static final byte WORK_EVENT=68;
    private int workStarted=-1000;
    private BlockPos workFace;

    public static void register() {
        var key=ResourceKey.create(Registries.ENTITY_TYPE,Jugcraft.id("deepstead_dwarf"));
        TYPE=Registry.register(BuiltInRegistries.ENTITY_TYPE,key,EntityType.Builder.<DeepsteadDwarf>of(DeepsteadDwarf::new,MobCategory.MISC)
            .sized(.93F,1.55F).eyeHeight(1.23F).clientTrackingRange(10).fireImmune().noLootTable().build(key));
        FabricDefaultAttributeRegistry.register(TYPE,Townsfolk.createAttributes().add(Attributes.MAX_HEALTH,32)
            .add(Attributes.ATTACK_DAMAGE,8).add(Attributes.KNOCKBACK_RESISTANCE,.6).add(Attributes.MOVEMENT_SPEED,.25));
    }

    public DeepsteadDwarf(EntityType<? extends DeepsteadDwarf> type,Level world) { super(type,world); }

    public void configure(Role job,String name,BlockPos home,int look) {
        configure(job,name,home,look,null);
    }
    void configure(Role job,String name,BlockPos home,int look,String guild) {
        String shop=job!=Role.MERCHANT?null:Math.floorMod(look,3)==1?DeepsteadWares.OUTFITTER:DeepsteadWares.LAMPWRIGHT;
        String wardrobe=job.id;
        if(job==Role.MERCHANT && guild!=null) {
            shop=guild;
            wardrobe=job.id+"_"+guild;
        }
        setUp(new TownData.Spot(-1,job.id,shop,name,wardrobe+"_"+Math.floorMod(look,3),home),home);
    }

    /** The inherited skin key is synchronized and saved with the town identity. */
    public Role dwarfRole() {
        String skin=skin();
        if(skin.startsWith(Role.GUARD.id)) return Role.GUARD;
        if(skin.startsWith(Role.MERCHANT.id)) return Role.MERCHANT;
        return Role.MINER;
    }
    public int appearance() {
        String skin=skin();
        return skin.endsWith("_2")?2:skin.endsWith("_1")?1:0;
    }
    public boolean wearsGuildColors(String guild) {
        return skin().startsWith(Role.MERCHANT.id+"_"+guild+"_");
    }
    @Override public boolean isGuard() { return Role.GUARD.id.equals(role()); }
    public boolean isMerchant() { return Role.MERCHANT.id.equals(role()); }

    public float workTicks(float partial) { return tickCount-workStarted+partial; }
    @Override public void handleEntityEvent(byte event) {
        if(event==WORK_EVENT) workStarted=tickCount;
        else super.handleEntityEvent(event);
    }
    @Override protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if(dwarfRole()!=Role.MINER || tickCount<talkingUntil || !getNavigation().isDone()) {
            workFace=null;
            return;
        }
        if((tickCount+getId())%130==0) {
            for(var p:BlockPos.betweenClosed(blockPosition().offset(-1,-1,-1),blockPosition().offset(1,0,1))) {
                var block=level.getBlockState(p);
                if(block.is(Blocks.ANVIL) || block.is(Blocks.SMITHING_TABLE) || block.is(Blocks.STONECUTTER)) {
                    workFace=p.immutable();
                    workStarted=tickCount;
                    level.broadcastEntityEvent(this,WORK_EVENT);
                    break;
                }
            }
        }
        if(workFace!=null && tickCount-workStarted<=18) {
            getLookControl().setLookAt(workFace.getX()+.5,workFace.getY()+1,workFace.getZ()+.5);
            if(tickCount-workStarted==14)
                level.playSound(null,workFace,SoundEvents.ANVIL_USE,SoundSource.NEUTRAL,.24F,.8F);
        }
    }

    @Override public boolean canAttack(LivingEntity other) {
        return isGuard() && other instanceof Monster monster && Pets.owner(monster)==null
            && Math.abs(other.getY()-home().getY())<=6 && home().closerThan(other.blockPosition(),24)
            && super.canAttack(other);
    }

    @Override protected void registerGoals() {
        goalSelector.addGoal(0,new FloatGoal(this));
        goalSelector.addGoal(1,new MeleeAttackGoal(this,1.1,true) {
            @Override public boolean canUse() { return isGuard() && super.canUse(); }
            @Override public boolean canContinueToUse() {
                return getTarget()!=null && DeepsteadDwarf.this.canAttack(getTarget()) && super.canContinueToUse();
            }
        });
        goalSelector.addGoal(2,new GalleryShift());
        goalSelector.addGoal(5,new LookAtPlayerGoal(this,Player.class,7));
        goalSelector.addGoal(6,new RandomLookAroundGoal(this));
        targetSelector.addGoal(1,new NearestAttackableTargetGoal<>(this,Monster.class,true,true) {
            @Override public boolean canUse() { return isGuard() && super.canUse(); }
            @Override public boolean canContinueToUse() {
                return getTarget()!=null && DeepsteadDwarf.this.canAttack(getTarget()) && super.canContinueToUse();
            }
        });
    }

    private class GalleryShift extends Goal {
        int retry;
        GalleryShift() { setFlags(EnumSet.of(Flag.MOVE)); }
        @Override public boolean canUse() {
            return tickCount>=retry && tickCount>=talkingUntil && tickCount-workStarted>22 && getTarget()==null;
        }
        @Override public void start() {
            retry=tickCount+80+random.nextInt(60);
            var post=home();
            int radius=isGuard()?6:isMerchant()?0:3;
            BlockPos to=post;
            if(radius>0 && post.closerThan(blockPosition(),radius+2)) {
                // Stay on this landing. A clearance check alone can choose the void under a bridge.
                var candidate=post.offset(random.nextIntBetweenInclusive(-radius,radius),0,
                    random.nextIntBetweenInclusive(-radius,radius));
                if(level().getBlockState(candidate.below()).isFaceSturdy(level(),candidate.below(),Direction.UP)
                        && level().getBlockState(candidate).isAir() && level().getBlockState(candidate.above()).isAir()) to=candidate;
            }
            if(distanceToSqr(to.getX()+.5,to.getY(),to.getZ()+.5)<.5) return;
            var path=getNavigation().createPath(to,0);
            if(path!=null && path.canReach()) getNavigation().moveTo(path,.8);
        }
        @Override public boolean canContinueToUse() {
            return !getNavigation().isDone() && getTarget()==null && tickCount>=talkingUntil;
        }
        @Override public void stop() { getNavigation().stop(); }
    }

    @Override public InteractionResult talkTo(Player player,InteractionHand hand) {
        if(player instanceof ServerPlayer visitor && hand==InteractionHand.MAIN_HAND) {
            getLookControl().setLookAt(visitor,35,30);
            getNavigation().stop();
            talkingUntil=tickCount+70;
            if(isMerchant()) DeepsteadWares.open(visitor,this);
            else {
                var text=Component.translatable("message.jugcraft.deepstead."+(isGuard()?"guard":"miner")+"."+appearance());
                visitor.sendSystemMessage(Component.translatable("message.jugcraft.townsfolk.says",getDisplayName(),text));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
