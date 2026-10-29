package io.github.jimbozoomer.jugcraft.town;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A three-wide, three-high public noticeboard placed by one item. All nine cells are real occupied blocks. */
public final class CommissionBoardBlock extends AtmBlock {
    public static final IntegerProperty PART = IntegerProperty.create("part",0,8);
    public CommissionBoardBlock(Properties properties) {
        super(properties); registerDefaultState(defaultBlockState().setValue(PART,0));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) {
        super.createBlockStateDefinition(builder); builder.add(PART);
    }
    public static BlockPos partPos(BlockPos root,Direction facing,int part) {
        return root.relative(facing.getClockWise(),part%3).above(part/3);
    }
    public static BlockPos rootPos(BlockPos pos,BlockState state) {
        return pos.relative(state.getValue(FACING).getCounterClockWise(),state.getValue(PART)%3).below(state.getValue(PART)/3);
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        var level=context.getLevel(); var player=context.getPlayer();
        Direction facing=context.getHorizontalDirection().getOpposite();
        for(int part=0;part<9;part++) {
            BlockPos at=partPos(context.getClickedPos(),facing,part);
            if(!level.isLoaded(at) || level.isOutsideBuildHeight(at) || !level.getWorldBorder().isWithinBounds(at)
                || !level.getBlockState(at).canBeReplaced(BlockPlaceContext.at(context,at,Direction.UP))
                || level.getBlockEntity(at)!=null
                || player!=null && (!level.mayInteract(player,at) || !player.mayUseItemAt(at,Direction.UP,context.getItemInHand())
                    || TownProtection.denies(player,level,at)
                    || !io.github.jimbozoomer.jugcraft.finance.LandRegistry.mayBuild(player,level,at)
                    || level instanceof ServerLevel server && !io.github.jimbozoomer.jugcraft.concordance.Authority.mayChange(server,player,at))) return null;
        }
        return defaultBlockState().setValue(FACING,facing);
    }
    @Override public void setPlacedBy(Level level,BlockPos pos,BlockState state,LivingEntity placer,ItemStack stack) {
        super.setPlacedBy(level,pos,state,placer,stack);
        if(!level.isClientSide()) for(int part=1;part<9;part++)
            level.setBlock(partPos(pos,state.getValue(FACING),part),state.setValue(PART,part),Block.UPDATE_ALL);
    }
    @Override protected void affectNeighborsAfterRemoval(BlockState state,ServerLevel level,BlockPos pos,boolean moved) {
        super.affectNeighborsAfterRemoval(state,level,pos,moved);
        if(level.getBlockState(pos).is(this)) return;
        // The broken part drops the single item. Remove matching partners without drops; never erase a replacement.
        var root=rootPos(pos,state);var facing=state.getValue(FACING);
        for(int part=0;part<9;part++) {
            var at=partPos(root,facing,part);
            if(at.equals(pos)||!level.isLoaded(at))continue;
            var other=level.getBlockState(at);
            if(other.is(this)&&other.getValue(FACING)==facing&&other.getValue(PART)==part)level.removeBlock(at,false);
        }
    }
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit) {
        var root=rootPos(pos,state);
        if(player instanceof ServerPlayer server && level.getBlockState(root).is(this)) CommissionMenu.open(server,root);
        return InteractionResult.SUCCESS;
    }
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) {
        // Each tile has its own thin collision/selection volume, including the roof overhang.
        int row=state.getValue(PART)/3;
        double front=row==2?2:5,back=row==2?14:11;
        return state.getValue(FACING).getAxis()==Direction.Axis.X
            ? box(front,0,0,back,16,16):box(0,0,front,16,16,back);
    }
    @Override protected BlockState rotate(BlockState state,net.minecraft.world.level.block.Rotation rotation) {
        return state.setValue(FACING,rotation.rotate(state.getValue(FACING)));
    }
    @Override protected BlockState mirror(BlockState state,net.minecraft.world.level.block.Mirror mirror) {
        // Reflect the across index as well as facing so structure mirrors retain one valid assembly.
        return rotate(state,mirror.getRotation(state.getValue(FACING))).setValue(PART,
            mirror==net.minecraft.world.level.block.Mirror.NONE?state.getValue(PART):state.getValue(PART)/3*3+2-state.getValue(PART)%3);
    }
}
