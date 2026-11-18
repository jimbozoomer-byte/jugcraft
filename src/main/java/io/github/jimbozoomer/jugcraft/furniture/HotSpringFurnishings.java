package io.github.jimbozoomer.jugcraft.furniture;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.Seat;
import io.github.jimbozoomer.jugcraft.agriculture.WallDecorationBlock;
import io.github.jimbozoomer.jugcraft.tower.FurnitureBlock;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public final class HotSpringFurnishings {
    private HotSpringFurnishings() {}

    public static Block BENCH, PEG_RAIL, BOOT_TRAY, SPOUT, PAIL, LANTERN;
    public static Item CHANGING_KIT, POOL_KIT;

    public static void register() {
        BENCH = block("hot_spring_bench", Blocks.SPRUCE_PLANKS, Bench::new);
        PEG_RAIL = block("hot_spring_peg_rail", Blocks.SPRUCE_PLANKS, p -> new WallDecorationBlock(p, 6, 0, 16));
        BOOT_TRAY = block("hot_spring_boot_tray", Blocks.SPRUCE_PLANKS, p -> new FloorPiece(p,
                box(0,0,2,16,2,14), box(2,2,4,8,7,12), box(8,2,3,14,7,11)));
        SPOUT = block("hot_spring_overflow_spout", Blocks.POLISHED_BASALT, OverflowSpout::new);
        PAIL = block("hot_spring_dipper_pail", Blocks.SPRUCE_PLANKS, p -> new FloorPiece(p,
                box(2,0,3,13,9,14), box(4,9,7,11,13,10), box(10,6,3,15,9,8)));
        LANTERN = block("hot_spring_pool_lantern", Blocks.POLISHED_BASALT, p -> new FloorPiece(p.lightLevel(s -> 13),
                box(2,0,2,14,4,14), box(4,4,4,12,12,12), box(2,12,2,14,14,14), box(5,14,5,11,16,11)));
        CHANGING_KIT = kit("hot_spring_changing_kit", List.of(BENCH, PEG_RAIL, BOOT_TRAY));
        POOL_KIT = kit("hot_spring_pool_kit", List.of(SPOUT, PAIL, LANTERN));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(out -> {
            out.accept(CHANGING_KIT);
            out.accept(POOL_KIT);
            for (Block piece : List.of(BENCH, PEG_RAIL, BOOT_TRAY, SPOUT, PAIL, LANTERN)) out.accept(piece);
        });
    }

    private static double[] box(double x, double y, double z, double u, double v, double w) {
        return new double[]{x,y,z,u,v,w};
    }

    private static Block block(String path, Block material, Function<BlockBehaviour.Properties, Block> create) {
        var id = Jugcraft.id(path);
        var key = ResourceKey.create(Registries.BLOCK, id);
        var piece = Registry.register(BuiltInRegistries.BLOCK, key, create.apply(BlockBehaviour.Properties.ofFullCopy(material)
                .setId(key).noOcclusion().strength(1.5F)));
        var item = ResourceKey.create(Registries.ITEM, id);
        Registry.register(BuiltInRegistries.ITEM, item, new BlockItem(piece, new Item.Properties().setId(item).useBlockDescriptionPrefix()));
        return piece;
    }

    private static Item kit(String path, List<Block> contents) {
        var key = ResourceKey.create(Registries.ITEM, Jugcraft.id(path));
        var lore = new java.util.ArrayList<Component>();
        lore.add(Component.translatable("item.jugcraft.hot_spring_kit.open"));
        for (Block block : contents) lore.add(block.getName());
        return Registry.register(BuiltInRegistries.ITEM, key, new Kit(new Item.Properties().setId(key).stacksTo(1)
                .component(DataComponents.LORE, new ItemLore(lore)), contents));
    }

    static final class Kit extends Item {
        private final List<Block> contents;

        Kit(Properties properties, List<Block> contents) {
            super(properties);
            this.contents = List.copyOf(contents);
        }

        @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
            if (player.isSpectator()) return InteractionResult.PASS;
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            var inventory = player.getInventory();
            var held = player.getItemInHand(hand);
            boolean consume = !player.hasInfiniteMaterials();
            var storage = inventory.getNonEquipmentItems();
            long available = storage.stream().filter(ItemStack::isEmpty).count();
            if (consume && hand == InteractionHand.MAIN_HAND && held.getCount() == 1) available++;
            long newSlots = contents.stream().filter(block -> inventory.getSlotWithRemainingSpace(new ItemStack(block)) < 0).count();
            if (available < newSlots) {
                player.sendSystemMessage(Component.translatable("item.jugcraft.hot_spring_kit.full"));
                return InteractionResult.FAIL;
            }
            if (consume) held.shrink(1);
            for (Block piece : contents) inventory.add(new ItemStack(piece));
            player.containerMenu.broadcastChanges();
            level.playSound(null, player.blockPosition(), SoundEvents.BARREL_OPEN, SoundSource.PLAYERS, .65F, 1.25F);
            return InteractionResult.SUCCESS;
        }
    }

    private static class FloorPiece extends FurnitureBlock {
        FloorPiece(Properties properties, double[]... boxes) { super(properties, boxes); }

        @Override protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
            return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
        }

        @Override public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
            var state = defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
            return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
        }

        @Override protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                Direction direction, BlockPos neighbour, BlockState neighbourState, RandomSource random) {
            if (direction == Direction.DOWN && !state.canSurvive(level, pos)) return Blocks.AIR.defaultBlockState();
            return super.updateShape(state, level, ticks, pos, direction, neighbour, neighbourState, random);
        }

        @Override protected BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }
        @Override protected BlockState mirror(BlockState state, Mirror mirror) { return state.rotate(mirror.getRotation(state.getValue(FACING))); }
    }

    private static final class Bench extends FloorPiece implements Seat.Sittable {
        Bench(Properties properties) {
            super(properties, HotSpringFurnishings.box(0,6,2,16,8,13), HotSpringFurnishings.box(1,0,3,4,6,12), HotSpringFurnishings.box(12,0,3,15,6,12));
        }

        @Override public double seatHeight(BlockState state) { return 0.5; }

        @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
            if (player.isSecondaryUseActive() || !level.getBlockState(pos.above()).getCollisionShape(level,pos.above()).isEmpty())
                return InteractionResult.PASS;
            if (level instanceof ServerLevel server && !Seat.sit(server, pos, state, player)) return InteractionResult.PASS;
            return InteractionResult.SUCCESS;
        }
    }

    static final class OverflowSpout extends FloorPiece {
        static final BooleanProperty OPEN = BlockStateProperties.OPEN;
        static final BooleanProperty FLOWING = BooleanProperty.create("flowing");

        OverflowSpout(Properties properties) {
            super(properties, HotSpringFurnishings.box(2,0,1,14,2,15), HotSpringFurnishings.box(4,2,8,12,12,15), HotSpringFurnishings.box(6,8,3,10,10,9));
            registerDefaultState(defaultBlockState().setValue(OPEN, false).setValue(FLOWING, false));
        }

        private static boolean fed(LevelReader level, BlockPos pos, BlockState state) {
            var fluid = level.getFluidState(pos.relative(state.getValue(FACING).getOpposite()));
            return fluid.is(FluidTags.WATER) && fluid.isSource();
        }

        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            super.createBlockStateDefinition(builder);
            builder.add(OPEN, FLOWING);
        }

        @Override protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                Direction direction, BlockPos neighbour, BlockState neighbourState, RandomSource random) {
            if (direction == state.getValue(FACING).getOpposite())
                return state.setValue(FLOWING, state.getValue(OPEN) && fed(level, pos, state));
            return super.updateShape(state, level, ticks, pos, direction, neighbour, neighbourState, random);
        }

        @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
            if (!level.isClientSide()) {
                boolean open = !state.getValue(OPEN);
                boolean running = open && fed(level, pos, state);
                level.setBlockAndUpdate(pos, state.setValue(OPEN, open).setValue(FLOWING, running));
                level.playSound(null, pos, running ? SoundEvents.BUCKET_EMPTY : SoundEvents.LEVER_CLICK,
                        SoundSource.BLOCKS, .4F, running ? 1.2F : .75F);
                if (open && !running) player.sendSystemMessage(Component.translatable("block.jugcraft.hot_spring_overflow_spout.dry"));
            }
            return InteractionResult.SUCCESS;
        }
    }
}

