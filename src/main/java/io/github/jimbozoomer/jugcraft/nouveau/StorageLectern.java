package io.github.jimbozoomer.jugcraft.nouveau;

import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.*;
import com.geckolib.util.GeckoLibUtil;
import io.github.jimbozoomer.jugcraft.concordance.Authority;
import io.github.jimbozoomer.jugcraft.party.JugcraftParties;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import org.jspecify.annotations.Nullable;

/** Links never contain copied item stacks. All access resolves the real, currently loaded chest again. */
public final class StorageLectern extends BlockEntity implements GeoBlockEntity {
    public static final int MAX_WYRMS=4, LINKS_PER_WYRM=8, MAX_LINKS=32, RANGE=16;
    private @Nullable UUID owner;
    private final List<BlockPos> links=new ArrayList<>();
    private final List<UUID> wyrms=new ArrayList<>();
    private final AnimatableInstanceCache cache=GeckoLibUtil.createInstanceCache(this);
    public StorageLectern(BlockPos pos,BlockState state){super(JugNouveau.LECTERN_ENTITY,pos,state);}
    public void placedBy(ServerPlayer player){owner=player.getUUID();setChanged();}
    public @Nullable UUID owner(){return owner;}
    public List<BlockPos> links(){return List.copyOf(links);}
    public List<UUID> wyrms(){return List.copyOf(wyrms);}
    public int capacity(){return wyrms.size()*LINKS_PER_WYRM;}
    public boolean mayUse(ServerPlayer player){
        return JugNouveau.enabled()&&owner!=null&&(owner.equals(player.getUUID())||JugcraftParties.sameParty(owner,player.getUUID()))
            &&player.isAlive()&&!player.isSpectator()&&level==player.level()&&level instanceof ServerLevel server
            &&Authority.mayChange(server,player,worldPosition);
    }
    public boolean bind(ServerPlayer player){
        if(!mayUse(player)||wyrms.size()>=MAX_WYRMS||!(level instanceof ServerLevel server))return false;
        BookWyrm wyrm=JugNouveau.BOOK_WYRM.create(server,EntitySpawnReason.MOB_SUMMONED);
        if(wyrm==null)return false;
        wyrm.bind(worldPosition,owner,wyrms.size()%JugNouveau.COLORS.length);
        wyrm.snapTo(worldPosition.getX()+.5,worldPosition.getY()+1.6,worldPosition.getZ()+.5,0,0);
        if(!server.addFreshEntity(wyrm))return false;
        wyrms.add(wyrm.getUUID());setChanged();return true;
    }
    public void detach(UUID id){if(wyrms.remove(id))setChanged();}
    public boolean link(ServerPlayer player,BlockPos pos){
        if(!mayUse(player)||pos.distSqr(worldPosition)>RANGE*RANGE||links.contains(pos)||links.size()>=capacity()
            ||accessible(player,pos)==null)return false;
        links.add(pos.immutable());setChanged();return true;
    }
    public boolean unlink(BlockPos pos){boolean changed=links.remove(pos);if(changed)setChanged();return changed;}
    /** Only ordinary vanilla storage is accepted: no furnace output slots, private mod inventories or Ender chests. */
    public @Nullable Container accessible(ServerPlayer player,BlockPos pos){
        if(!(level instanceof ServerLevel server)||!server.isLoaded(pos)||pos.distSqr(worldPosition)>RANGE*RANGE
            ||!Authority.mayChange(server,player,pos))return null;
        var entity=server.getBlockEntity(pos);
        if(!(entity instanceof ChestBlockEntity)&&!(entity instanceof BarrelBlockEntity))return null;
        var state=entity.getBlockState();
        if(!state.is(Blocks.CHEST)&&!state.is(Blocks.TRAPPED_CHEST)&&!state.is(Blocks.BARREL))return null;
        var chest=(BaseContainerBlockEntity)entity;
        // Locking an already-linked chest immediately removes it from this live view.
        // Locked containers are deliberately excluded, including when a key is held.
        return !chest.isLocked()?chest:null;
    }
    public @Nullable Container linkedContainer(ServerPlayer player,int index){
        if(!mayUse(player)||index<0||index>=links.size()||index>=capacity())return null;
        return accessible(player,links.get(index));
    }
    @Override public void preRemoveSideEffects(BlockPos pos,BlockState state){
        if(level instanceof ServerLevel server){
            int count=wyrms.size();var old=List.copyOf(wyrms);wyrms.clear();links.clear();
            for(UUID id:old)if(server.getEntity(id) instanceof BookWyrm wyrm)wyrm.discard();
            if(count>0)Block.popResource(server,pos,new ItemStack(JugNouveau.CHARM,count));
            setChanged();
        }
    }
    @Override protected void saveAdditional(ValueOutput out){
        super.saveAdditional(out);out.putInt("nouveau_schema",1);
        if(owner!=null)out.store("owner",UUIDUtil.CODEC,owner);
        out.store("links",BlockPos.CODEC.listOf(),links);out.store("wyrms",UUIDUtil.CODEC.listOf(),wyrms);
    }
    @Override protected void loadAdditional(ValueInput in){
        super.loadAdditional(in);owner=in.read("owner",UUIDUtil.CODEC).orElse(null);links.clear();wyrms.clear();
        in.read("links",BlockPos.CODEC.listOf()).orElse(List.of()).stream().distinct().filter(p->p.distSqr(worldPosition)<=RANGE*RANGE)
            .limit(MAX_LINKS).forEach(p->links.add(p.immutable()));
        in.read("wyrms",UUIDUtil.CODEC.listOf()).orElse(List.of()).stream().distinct().limit(MAX_WYRMS).forEach(wyrms::add);
    }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar out){
        out.add(new AnimationController<StorageLectern>("ledger",4,s->s.setAndContinue(RawAnimation.begin().thenLoop("ledger_float"))));
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache(){return cache;}
}
