package local.peepo;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.network.syncher.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.sounds.SoundEvents;
import java.util.EnumSet;
import java.util.Comparator;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class PeepoEntity extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> BLUSHING = SynchedEntityData.defineId(PeepoEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> PUMPKIN = SynchedEntityData.defineId(PeepoEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> EATING = SynchedEntityData.defineId(PeepoEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ENERGY = SynchedEntityData.defineId(PeepoEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FOOD_BONUS = SynchedEntityData.defineId(PeepoEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FOOD_TIME = SynchedEntityData.defineId(PeepoEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> REST = SynchedEntityData.defineId(PeepoEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> WHEEL_RUNNING = SynchedEntityData.defineId(PeepoEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> RECOVERING = SynchedEntityData.defineId(PeepoEntity.class, EntityDataSerializers.BOOLEAN);
    public final net.minecraft.world.SimpleContainer belongings=new net.minecraft.world.SimpleContainer(10){
        @Override public void setChanged(){syncBelongings();}
    };
    private void syncBelongings(){
        if(level().isClientSide())return;
        entityData.set(PUMPKIN,belongings.getItem(8).is(Items.JACK_O_LANTERN));
        if(!isEating())setItemSlot(EquipmentSlot.MAINHAND,belongings.getItem(9).copy());
    }
    @Override protected void dropCustomDeathLoot(ServerLevel server,DamageSource source,boolean playerKill){
        // The displayed hand is a copy; only the inventory owns the equipped item.
        if(!isEating())setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);
        super.dropCustomDeathLoot(server,source,playerKill);
        for(int i=0;i<belongings.getContainerSize();i++){
            var stack=belongings.removeItemNoUpdate(i);
            if(!stack.isEmpty())spawnAtLocation(server,stack);
        }
    }
    private CompanionRoutine routine;
    public final CompanionOrders orders=new CompanionOrders(this);
    public void resetCompanionRoutine(){if(routine!=null)routine.resetOrders();}
    private net.minecraft.core.BlockPos bedExit;
    public void setBedExit(net.minecraft.core.BlockPos pos) { bedExit = pos; }
    /** Also used after reload, so a companion never becomes stranded in an upper bunk. */
    public void leaveCompanionBed() {
        if (bedExit == null || level().isClientSide()) return;
        for (int radius = 0; radius <= 2; radius++) for (int dx = -radius; dx <= radius; dx++) for (int dz = -radius; dz <= radius; dz++) {
            var pos = bedExit.offset(dx, 0, dz);
            if (!level().hasChunkAt(pos)) continue;
            var at = net.minecraft.world.phys.Vec3.atBottomCenterOf(pos);
            if (level().getBlockState(pos.below()).isFaceSturdy(level(), pos.below(), net.minecraft.core.Direction.UP)
                && level().noCollision(new net.minecraft.world.phys.AABB(at.x - .22, at.y, at.z - .22, at.x + .22, at.y + 1, at.z + .22))) {
                snapTo(at.x, at.y, at.z, getYRot(), 0); setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO); resetFallDistance(); bedExit = null; return;
            }
        }
    }
    @Override protected EntityDimensions getDefaultDimensions(Pose pose) {
        return getRestMode() == CompanionEnergy.Rest.SLEEPING ? EntityDimensions.fixed(.4F, .3F).withEyeHeight(.2F) : super.getDefaultDimensions(pose);
    }
    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
        super.onSyncedDataUpdated(data);
        if (REST.equals(data)) refreshDimensions();
    }
    public boolean isRecovering() { return entityData.get(RECOVERING); }
    public void updateRecoveryState() {
        if(level().isClientSide())return;
        if(getEnergy()==0) { entityData.set(RECOVERING,true);setWheelRunning(false); }
        else if(getEnergy()>=CompanionEnergy.CAPACITY*80/100)entityData.set(RECOVERING,false);
    }
    private int wheelRunningTicks;
    public boolean isWheelRunning() { return entityData.get(WHEEL_RUNNING); }
    /** The occupied wheel refreshes this each server tick; stopping/unloading expires the pose. */
    public void setWheelRunning(boolean running) {
        if(level().isClientSide())return;
        boolean active=running && !isRecovering() && isAlive() && getEnergy()>0 && !isEating() && getRestMode()==CompanionEnergy.Rest.NONE;
        wheelRunningTicks=active?5:0;entityData.set(WHEEL_RUNNING,active);
        if(active)getNavigation().stop();
    }
    private final CompanionEnergy energyStore = new CompanionEnergy(this);
    public int getEnergy() { return entityData.get(ENERGY); }
    public int getEnergyCapacity() { return CompanionEnergy.CAPACITY; }
    void setStoredEnergy(int value) { entityData.set(ENERGY,Math.clamp(value,0,CompanionEnergy.CAPACITY)); }
    public int getFoodRegenBonus() { return entityData.get(FOOD_BONUS); }
    public int getFoodRegenTicks() { return entityData.get(FOOD_TIME); }
    public CompanionEnergy.Rest getRestMode() { return CompanionEnergy.Rest.values()[entityData.get(REST)]; }
    public int extractEnergy(int requested, net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext transaction) {
        return energyStore.extract(requested,transaction);
    }
    public boolean isRestNight() {
        if (!(level() instanceof ServerLevel server) || !level().dimension().equals(Level.OVERWORLD)) return false;
        long time=Math.floorMod(server.getOverworldClockTime(),24000L);
        return time>=13000 && time<23000;
    }
    /** Furniture integration hook. Caller must release the rest state when its seat/bed is removed. */
    public boolean setRestMode(CompanionEnergy.Rest rest) {
        if(level().isClientSide() || !isAlive() || (rest!=CompanionEnergy.Rest.NONE && isEating())
                || (rest==CompanionEnergy.Rest.SLEEPING && !isRestNight())) return false;
        if (getRestMode()==CompanionEnergy.Rest.SITTING && rest!=CompanionEnergy.Rest.SITTING) setNoGravity(false);
        entityData.set(REST,rest.ordinal());
        if(rest!=CompanionEnergy.Rest.NONE)getNavigation().stop();
        return true;
    }
    public int getEnergyRegenPerTick() {
        int passive=0;
        if(!isWheelRunning() && !energyStore.recentlyWorking()) {
            passive=switch(getRestMode()) { case NONE -> 2; case SITTING -> 8; case SLEEPING -> isRestNight()?16:2; };
        }
        return passive+(getFoodRegenTicks()>0 ? getFoodRegenBonus() : 0);
    }
    private void tickEnergy() {
        if(!isAlive())return;
        updateRecoveryState();
        if(getRestMode()==CompanionEnergy.Rest.SLEEPING && !isRestNight())setRestMode(CompanionEnergy.Rest.NONE);
        setStoredEnergy(getEnergy()+getEnergyRegenPerTick());
        updateRecoveryState();
        if(getFoodRegenTicks()>0) {
            entityData.set(FOOD_TIME,getFoodRegenTicks()-1);
            if(getFoodRegenTicks()==0)entityData.set(FOOD_BONUS,0);
        }
    }
    private void applyMealEnergy(net.minecraft.world.food.FoodProperties food) {
        var meal=CompanionEnergy.meal(food);
        // A weak snack cannot extend a stronger meal; equal/better meals refresh, never stack.
        if(meal.bonusPerTick()>=getFoodRegenBonus() || getFoodRegenTicks()==0) {
            entityData.set(FOOD_BONUS,meal.bonusPerTick());entityData.set(FOOD_TIME,meal.durationTicks());
        }
    }
    public static final int EAT_DURATION = 40;
    public static final int BLUSH_DURATION = 80;
    private int blushTicks;
    private net.minecraft.core.GlobalPos lunchOrigin;
    private boolean naturallySpawned;
    public boolean isNaturallySpawned() { return naturallySpawned; }
    @Override public SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level,
            net.minecraft.world.DifficultyInstance difficulty, EntitySpawnReason reason, SpawnGroupData group) {
        naturallySpawned = reason == EntitySpawnReason.NATURAL || reason == EntitySpawnReason.CHUNK_GENERATION;
        return super.finalizeSpawn(level, difficulty, reason, group);
    }
    private long nextGreeting;
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder); builder.define(BLUSHING, false); builder.define(PUMPKIN, false); builder.define(EATING,0);
        builder.define(ENERGY,CompanionEnergy.CAPACITY);builder.define(FOOD_BONUS,0);builder.define(FOOD_TIME,0);builder.define(REST,0);
        builder.define(WHEEL_RUNNING,false);
        builder.define(RECOVERING,false);
    }
    public boolean isJughead() { return getType()==PeepoMod.JUGHEAD || getType()==PeepoMod.LEGACY_JUGHEAD; }
    public boolean isWearingPumpkin() { return entityData.get(PUMPKIN); }
    @Override protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output); orders.save(output); output.putBoolean("PumpkinCostume", isWearingPumpkin()); output.putInt("EatingTicks",getEatingTicks());
        if(lunchOrigin!=null)output.store("LunchOrigin",net.minecraft.core.GlobalPos.CODEC,lunchOrigin);
        net.minecraft.world.ContainerHelper.saveAllItems(output.child("Belongings"),belongings.getItems());
        output.putBoolean("NaturallySpawned", naturallySpawned);
        output.putInt("Energy",getEnergy());output.putInt("FoodRegenBonus",getFoodRegenBonus());output.putInt("FoodRegenTicks",getFoodRegenTicks());
        output.putBoolean("Recovering",isRecovering());
        if (bedExit != null) output.store("CompanionBedExit", net.minecraft.core.BlockPos.CODEC, bedExit);
    }
    @Override protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input); orders.load(input); entityData.set(PUMPKIN,input.getBooleanOr("PumpkinCostume",false));
        naturallySpawned = input.getBooleanOr("NaturallySpawned", false);
        setStoredEnergy(input.getIntOr("Energy",CompanionEnergy.CAPACITY));
        entityData.set(RECOVERING,input.getBooleanOr("Recovering",false));updateRecoveryState();
        int foodTicks=Math.clamp(input.getIntOr("FoodRegenTicks",0),0,3800);
        entityData.set(FOOD_TIME,foodTicks);
        entityData.set(FOOD_BONUS,foodTicks>0 ? Math.clamp(input.getIntOr("FoodRegenBonus",0),0,32) : 0);
        // Furniture must re-establish a valid seat/bed after loading, never leave a phantom rest state.
        entityData.set(REST,0);
        bedExit = input.read("CompanionBedExit", net.minecraft.core.BlockPos.CODEC).orElse(null);
        if (bedExit != null) setNoGravity(false);
        lunchOrigin=input.read("LunchOrigin",net.minecraft.core.GlobalPos.CODEC).orElse(null);
        int remaining=Math.clamp(input.getIntOr("EatingTicks",0),0,EAT_DURATION);
        entityData.set(EATING,isEdible(getMainHandItem()) ? remaining : 0);
        belongings.getItems().replaceAll(stack->ItemStack.EMPTY);
        var saved=input.child("Belongings");
        if(saved.isPresent())net.minecraft.world.ContainerHelper.loadAllItems(saved.get(),belongings.getItems());
        else if(isWearingPumpkin())belongings.getItems().set(8,new ItemStack(Items.JACK_O_LANTERN));
        syncBelongings();
    }
    public boolean isBlushing() { return entityData.get(BLUSHING); }
    @Override public void tick() {
        if (!level().isClientSide() && bedExit != null && getRestMode() == CompanionEnergy.Rest.NONE) leaveCompanionBed();
        super.tick();
        if(!level().isClientSide()) {
            if(wheelRunningTicks>0)--wheelRunningTicks;
            if(wheelRunningTicks==0 || !isAlive() || getEnergy()==0 || isEating() || getRestMode()!=CompanionEnergy.Rest.NONE)
                setWheelRunning(false);
        }
        if(!level().isClientSide())tickEnergy();
        if (!level().isClientSide() && blushTicks > 0 && --blushTicks == 0) entityData.set(BLUSHING, false);
        if (level() instanceof ServerLevel server && isEating()) {
            getNavigation().stop();
            if (!isEdible(getMainHandItem())) { entityData.set(EATING,0); lunchOrigin=null;syncBelongings();return; }
            int remaining=getEatingTicks();
            if (remaining%8==0) {
                double angle=Math.toRadians(yBodyRot);
                double reach=isWearingPumpkin() ? .27 : .23;
                server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM,getMainHandItem().getItem()),
                    getX()-Math.sin(angle)*reach,getY()+.42,getZ()+Math.cos(angle)*reach,5,.04,.035,.04,.025);
                playSound(SoundEvents.GENERIC_EAT.value(),.45F,.95F+random.nextFloat()*.2F);
            }
            entityData.set(EATING,remaining-1);
            if (remaining==1) {
                var eaten=getMainHandItem();
                var food=eaten.get(DataComponents.FOOD);
                heal(Math.max(1,food.nutrition()));
                applyMealEnergy(food);
                var remainder=eaten.get(DataComponents.USE_REMAINDER);
                setItemSlot(EquipmentSlot.MAINHAND,belongings.getItem(9).copy());
                if(remainder!=null){
                    var leftover=remainder.convertInto().create();
                    if(lunchOrigin!=null && lunchOrigin.dimension().equals(level().dimension())
                            && level().hasChunkAt(lunchOrigin.pos()) && lunchOrigin.pos().distToCenterSqr(position())<=16
                            && level().getBlockEntity(lunchOrigin.pos()) instanceof LunchBlockEntity lunch)
                        leftover=lunch.returnRemainder(this,leftover);
                    if(!leftover.isEmpty())spawnAtLocation(server,leftover);
                }
                lunchOrigin=null;
                playSound(SoundEvents.PLAYER_BURP,.35F,1.3F);
            }
        }
    }
    public PeepoEntity(EntityType<? extends PeepoEntity> type, Level level) {
        super(type, level); setPersistenceRequired();
    }
    public static AttributeSupplier.Builder attributes() {
        return createMobAttributes().add(Attributes.MAX_HEALTH, 20).add(Attributes.MOVEMENT_SPEED, .20)
            .add(Attributes.FOLLOW_RANGE, 16);
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new EatInPlaceGoal());
        goalSelector.addGoal(1, new RestGoal());
        goalSelector.addGoal(2, new FindFoodGoal());
        goalSelector.addGoal(2, new FindLunchGoal(this));
        goalSelector.addGoal(3,new CompanionOrders.CommandGoal(this));
        routine=new CompanionRoutine(this);
        goalSelector.addGoal(3,routine);
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, .8));
        goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6));
        goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }
    @Override public boolean canAttack(LivingEntity target) { return false; }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.getEntity() instanceof Player) return false;
        return super.hurtServer(level,source,amount);
    }
    @Override public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!isAlive() || player.isSpectator()) return InteractionResult.PASS;
        if(player.isShiftKeyDown()) {
            if(!level().isClientSide()) {
                if(orders.allowed(player))player.openMenu(new net.minecraft.world.SimpleMenuProvider((id,inventory,p)->new CompanionMenu(id,inventory,this),getDisplayName()));
                else player.sendOverlayMessage(net.minecraft.network.chat.Component.literal(orders.tamed()?"Only the owner or an allowed party member can give commands.":"Feed this companion once to tame it."));
            }
            return InteractionResult.SUCCESS;
        }
        var stack=player.getItemInHand(hand);
        if (isEdible(stack)) {
            if ((orders.tamed() && !needsFood()) || isEating()) return InteractionResult.CONSUME;
            if (!level().isClientSide()) {
                if(!orders.tamed()) {
                    orders.tame(player);
                    ((ServerLevel)level()).sendParticles(ParticleTypes.HEART,getX(),getY()+.65,getZ(),7,.18,.1,.18,0);
                }
                beginEating(stack.copyWithCount(1));
                stack.consume(1,player);
            }
            return InteractionResult.SUCCESS;
        }
        if (stack.is(Items.JACK_O_LANTERN)) {
            if(!level().isClientSide() && orders.tamed() && !orders.allowed(player))return InteractionResult.CONSUME;
            if (!level().isClientSide() && !isWearingPumpkin()) {
                belongings.setItem(8,stack.copyWithCount(1));
                stack.consume(1,player);
            }
            return InteractionResult.SUCCESS;
        }
        if (!stack.isEmpty()) return super.mobInteract(player,hand);
        if (level() instanceof ServerLevel server) {
            blushTicks = BLUSH_DURATION;
            entityData.set(BLUSHING, true);
            if (server.getGameTime() < nextGreeting) return InteractionResult.SUCCESS;
            nextGreeting=server.getGameTime()+30;
            getLookControl().setLookAt(player,30,30);
            server.sendParticles(ParticleTypes.HEART,getX(),getY()+.65,getZ(),3,.18,.1,.18,0);
        }
        return InteractionResult.SUCCESS;
    }
    public static boolean isEdible(ItemStack stack) { return !stack.isEmpty() && stack.has(DataComponents.FOOD); }
    public boolean needsFood() { return isAlive() && (getHealth()<getMaxHealth() || getEnergy()<CompanionEnergy.CAPACITY*95/100); }
    /** Let an existing meal digest instead of eating every loose item while the reserve fills. */
    public boolean needsAutomaticFood() { return needsFood() && (getHealth()<getMaxHealth() || getFoodRegenTicks()==0); }
    public boolean isEating() { return getEatingTicks()>0; }
    public int getEatingTicks() { return entityData.get(EATING); }
    void beginLunchMeal(ItemStack stack,net.minecraft.core.GlobalPos source){beginEating(stack);lunchOrigin=source;}
    private void beginEating(ItemStack stack) {
        lunchOrigin=null;
        setWheelRunning(false);
        setRestMode(CompanionEnergy.Rest.NONE);
        setItemSlot(EquipmentSlot.MAINHAND,stack);
        setGuaranteedDrop(EquipmentSlot.MAINHAND);
        entityData.set(EATING,EAT_DURATION);
        getNavigation().stop();
    }
    private final class EatInPlaceGoal extends Goal {
        EatInPlaceGoal() { setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK)); }
        @Override public boolean canUse() { return isEating(); }
        @Override public void tick() { getNavigation().stop(); }
    }
    private final class RestGoal extends Goal {
        RestGoal() { setFlags(EnumSet.of(Flag.MOVE,Flag.JUMP)); }
        @Override public boolean canUse() { return (routine==null || !routine.isActive())
            && (getRestMode()!=CompanionEnergy.Rest.NONE || isWheelRunning()); }
        @Override public void tick() { getNavigation().stop(); }
    }
    private final class FindFoodGoal extends Goal {
        private ItemEntity target;
        private int repath,elapsed;
        private long nextSearch;
        FindFoodGoal() { setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK)); }
        private boolean valid(ItemEntity item) {
            return item!=null && item.isAlive() && !item.hasPickUpDelay() && isEdible(item.getItem())
                && distanceToSqr(item)<64 && orders.food(item.position());
        }
        @Override public boolean canUse() {
            if (!needsAutomaticFood() || isEating() || level().getGameTime()<nextSearch) return false;
            nextSearch=level().getGameTime()+20;
            target=level().getEntitiesOfClass(ItemEntity.class,getBoundingBox().inflate(8,3,8),this::valid)
                .stream().filter(item->getSensing().hasLineOfSight(item))
                .min(Comparator.comparingDouble(item->distanceToSqr(item))).orElse(null);
            return target!=null;
        }
        @Override public boolean canContinueToUse() { return needsAutomaticFood() && !isEating() && valid(target) && elapsed<200; }
        @Override public void start() { repath=0;elapsed=0; }
        @Override public void stop() { target=null;getNavigation().stop();nextSearch=level().getGameTime()+20; }
        @Override public void tick() {
            elapsed++;
            if (!valid(target)) return;
            getLookControl().setLookAt(target,20,30);
            if (--repath<=0) { repath=10;getNavigation().moveTo(getNavigation().createPath(target,0),1.15); }
            if (distanceToSqr(target)<.64 && getSensing().hasLineOfSight(target)) {
                var food=target.getItem();
                beginEating(food.copyWithCount(1));
                var rest=food.copy();rest.shrink(1);
                if(rest.isEmpty())target.discard();else target.setItem(rest);
            }
        }
    }

}


