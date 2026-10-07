package local.peepo;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.shapes.*;

/** Two-block floor torch; vanilla double-block placement and removal handle both halves. */
public final class TikiTorch extends DoublePlantBlock {
    public static final TagKey<Item> HELD_LIGHTS=TagKey.create(Registries.ITEM,PeepoMod.id("held_lights"));
    public TikiTorch(Properties properties){super(properties);}
    public static boolean isHeldLight(ItemStack stack){
        return stack.is(HELD_LIGHTS) || stack.getItem() instanceof BlockItem item && item.getBlock().defaultBlockState().getLightEmission()>0;
    }
    protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){
        return state.getValue(HALF)==DoubleBlockHalf.LOWER?Shapes.or(Block.box(7,0,7,9,6,9),Block.box(5.5,6,5.5,10.5,16,10.5)):Shapes.or(Block.box(5.5,0,5.5,10.5,2,10.5),Block.box(5,2,5,11,14,11));
    }
    protected boolean canSurvive(BlockState state,LevelReader level,BlockPos pos){
        return state.getValue(HALF)==DoubleBlockHalf.UPPER?super.canSurvive(state,level,pos):level.getBlockState(pos.below()).isFaceSturdy(level,pos.below(),Direction.UP);
    }
    public BlockState getStateForPlacement(BlockPlaceContext context){
        if(!context.getLevel().getFluidState(context.getClickedPos()).isEmpty() || !context.getLevel().getFluidState(context.getClickedPos().above()).isEmpty())return null;
        return super.getStateForPlacement(context);
    }
    public void animateTick(BlockState state,Level level,BlockPos pos,RandomSource random){
        if(state.getValue(HALF)!=DoubleBlockHalf.UPPER)return;
        level.addParticle(ParticleTypes.FLAME,pos.getX()+.5,pos.getY()+.91,pos.getZ()+.5,0,0,0);
        level.addParticle(ParticleTypes.SMOKE,pos.getX()+.5,pos.getY()+.97,pos.getZ()+.5,0,0,0);
    }
    public static void initialize(){
        var id=PeepoMod.id("tiki_torch");
        var block=Registry.register(BuiltInRegistries.BLOCK,id,new TikiTorch(Properties.of().setId(ResourceKey.create(Registries.BLOCK,id)).instabreak().sound(SoundType.WOOD).noCollision().noOcclusion().lightLevel(s->s.getValue(HALF)==DoubleBlockHalf.UPPER?14:0)));
        var item=Registry.register(BuiltInRegistries.ITEM,id,new BlockItem(block,new Item.Properties().setId(ResourceKey.create(Registries.ITEM,id)).useBlockDescriptionPrefix()));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(e->e.accept(item));
    }
}
