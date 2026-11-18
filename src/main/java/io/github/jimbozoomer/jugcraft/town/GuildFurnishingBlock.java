package io.github.jimbozoomer.jugcraft.town;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

final class GuildFurnishingBlock extends Block {
    static final IntegerProperty SETTING = IntegerProperty.create("setting", 0, 2);
    enum Style {
        GUILD_BORDER_PAVER(Blocks.POLISHED_DEEPSLATE,16,16,16),
        ARCHMASTERS_KEYSTONE(Blocks.CHISELED_STONE_BRICKS,16,16,16),
        SCROLLWORK_CORBEL(Blocks.STONE_BRICKS,14,16,14),
        SURVEY_BENCHMARK(Blocks.POLISHED_DEEPSLATE,14,7,14),
        QUARRY_SAMPLE_CABINET(Blocks.SPRUCE_PLANKS,16,15,10),
        STONEMASONS_CUTTING_BENCH(Blocks.STONECUTTER,16,14,12),
        COPPER_SAMOVAR(Blocks.IRON_BLOCK,13,15,12),
        BRAIDED_BREAD_BASKET(Blocks.OAK_PLANKS,14,8,12),
        TANKARD_TREE(Blocks.SPRUCE_PLANKS,14,14,12),
        SHIFT_SUPPER_GONG(Blocks.IRON_BLOCK,16,16,8);
        final Block material;
        final boolean solid;
        final VoxelShape north,east;
        Style(Block material, int w, int h, int d) {
            this.material=material;
            solid=w==16 && d==16 && h==16;
            north=Block.box(8-w/2.0,0,8-d/2.0,8+w/2.0,h,8+d/2.0);
            east=Block.box(8-d/2.0,0,8-w/2.0,8+d/2.0,h,8+w/2.0);
        }
    }
    private final Style style;
    GuildFurnishingBlock(Properties properties,Style style) {
        super(properties);
        this.style=style;
        registerDefaultState(defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING,Direction.NORTH).setValue(SETTING,0));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b) { b.add(BlockStateProperties.HORIZONTAL_FACING,SETTING); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext c) {
        return defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING,c.getHorizontalDirection().getOpposite());
    }
    @Override protected BlockState rotate(BlockState s,Rotation r) {
        return s.setValue(BlockStateProperties.HORIZONTAL_FACING,r.rotate(s.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }
    @Override protected BlockState mirror(BlockState s,Mirror m) { return rotate(s,m.getRotation(s.getValue(BlockStateProperties.HORIZONTAL_FACING))); }
    @Override protected VoxelShape getShape(BlockState s,BlockGetter level,BlockPos p,CollisionContext c) {
        return s.getValue(BlockStateProperties.HORIZONTAL_FACING).getAxis()==Direction.Axis.Z ? style.north : style.east;
    }
    @Override protected @Nullable MenuProvider getMenuProvider(BlockState s,Level level,BlockPos pos) {
        if (style!=Style.STONEMASONS_CUTTING_BENCH) return null;
        return new SimpleMenuProvider((id,inv,visitor)->new StonecutterMenu(id,inv,ContainerLevelAccess.create(level,pos)){
            @Override public boolean stillValid(Player who) {
                return who.isAlive() && !who.isSpectator() && who.level()==level && level.getBlockState(pos).is(GuildFurnishingBlock.this)
                    && who.distanceToSqr(Vec3.atCenterOf(pos)) <= 64;
            }
        }, Component.translatable("block.jugcraft.stonemasons_cutting_bench"));
    }
    @Override protected InteractionResult useWithoutItem(BlockState s,Level level,BlockPos pos,Player player,BlockHitResult hit) {
        switch (style) {
            case SURVEY_BENCHMARK -> {
                if (!level.isClientSide()) player.sendOverlayMessage(Component.translatable("message.jugcraft.survey_benchmark",pos.toShortString()));
            }
            case QUARRY_SAMPLE_CABINET -> {
                if (!level.isClientSide()) {
                    var next=s.cycle(SETTING); level.setBlock(pos,next,Block.UPDATE_CLIENTS);
                    player.sendOverlayMessage(Component.translatable("message.jugcraft.quarry_sample."+next.getValue(SETTING)));
                }
            }
            case STONEMASONS_CUTTING_BENCH -> { if (!level.isClientSide()) player.openMenu(getMenuProvider(s,level,pos)); }
            case COPPER_SAMOVAR -> {
                if (level instanceof ServerLevel server) {
                    boolean heat=s.getValue(SETTING)==0;
                    level.setBlock(pos,s.setValue(SETTING,heat?1:0),Block.UPDATE_ALL);
                    if (heat) server.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,pos.getX()+.5,pos.getY()+.9,pos.getZ()+.5,5,.08,.08,.08,.015);
                    level.playSound(null,pos,SoundEvents.FIRE_EXTINGUISH,SoundSource.BLOCKS,.3F,heat?1.5F:1.0F);
                }
            }
            case SHIFT_SUPPER_GONG -> {
                if (level instanceof ServerLevel server && !server.getBlockTicks().hasScheduledTick(pos,this)) {
                    level.playSound(null,pos,SoundEvents.BELL_BLOCK,SoundSource.BLOCKS,1.8F,.6F);
                    level.playSound(null,pos,SoundEvents.NOTE_BLOCK_CHIME.value(),SoundSource.BLOCKS,.8F,.7F);
                    server.scheduleTick(pos,this,20);
                }
            }
            default -> {return InteractionResult.PASS;}
        }
        return InteractionResult.SUCCESS;
    }
}
