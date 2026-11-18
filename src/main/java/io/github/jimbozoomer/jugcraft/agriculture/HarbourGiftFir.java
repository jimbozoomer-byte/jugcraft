package io.github.jimbozoomer.jugcraft.agriculture;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.finance.LandRegistry;
import io.github.jimbozoomer.jugcraft.server.OperatorRegions;
import io.github.jimbozoomer.jugcraft.town.ApartmentRentals;
import io.github.jimbozoomer.jugcraft.town.TownProtection;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/** A five-block market fir with a shared gift pile on each side and corner of the skirt. */
public class HarbourGiftFir extends MultiDecorationBlock implements EntityBlock {
    public static final String ID = "harbour_gift_fir";
    // Stored in the placer's right/up/away coordinate system used by MultiDecorationBlock.
    private static final int[][] FLOOR = {{0,0},{1,-1},{0,-1},{-1,-1},{-1,0},{-1,1},{0,1},{1,1},{1,0}};
    private static final int[][] CELLS = makeCells();
    public static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
    public static final IntegerProperty PARCELS = IntegerProperty.create("parcels", 0, 2);
    public static HarbourGiftFir TREE;
    public static HarbourGiftFir CITY_SKIRT;

    private static final class CivicSkirt extends HarbourGiftFir {
        private static final IntegerProperty SKIRT_PART = IntegerProperty.create("part", 0, 8);
        // Leave room for the civic fir's broad trunk between the eight public parcel piles.
        private static final int[][] SKIRT = java.util.Arrays.stream(FLOOR)
            .map(cell -> new int[]{cell[0] * 2, 0, cell[1] * 2}).toArray(int[][]::new);
        CivicSkirt(Properties properties) { super(properties); }
        @Override public int[][] cells() { return SKIRT; }
        @Override public IntegerProperty partProperty() { return SKIRT_PART; }
        @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
            return part(state) == MASTER ? Shapes.block() : super.getShape(state, level, pos, ctx);
        }
    }
    public static BlockEntityType<FirGifts> GIFT_ENTITY;
    private static final Map<Direction,VoxelShape[][]> SHAPES = makeShapes();

    public static void register() {
        var id = Jugcraft.id(ID);
        var key = ResourceKey.create(Registries.BLOCK, id);
        TREE = Registry.register(BuiltInRegistries.BLOCK, key, new HarbourGiftFir(
            Properties.of().setId(key).mapColor(MapColor.PLANT).strength(1.3F).sound(SoundType.WOOD)
                .noOcclusion().lightLevel(s -> s.getValue(LIT) && s.getValue(PART) == 32 ? 12 : 0)
                .pushReaction(PushReaction.IMMOVEABLE)));
        var skirtKey = ResourceKey.create(Registries.BLOCK, Jugcraft.id("civic_gift_skirt"));
        CITY_SKIRT = Registry.register(BuiltInRegistries.BLOCK, skirtKey, new CivicSkirt(
            Properties.of().setId(skirtKey).mapColor(MapColor.WOOD).strength(1.3F).sound(SoundType.WOOD)
                .noOcclusion().noLootTable().pushReaction(PushReaction.IMMOVEABLE)));
        var itemKey = ResourceKey.create(Registries.ITEM, id);
        var item = Registry.register(BuiltInRegistries.ITEM, itemKey,
            new BlockItem(TREE, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix()
                    .component(DataComponents.LORE, new ItemLore(List.of(
                            Component.translatable("item.jugcraft.harbour_gift_fir.space"),
                            Component.translatable("item.jugcraft.harbour_gift_fir.giving"),
                            Component.translatable("item.jugcraft.harbour_gift_fir.taking"))))));
        GIFT_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id,
            FabricBlockEntityTypeBuilder.create(FirGifts::new, TREE, CITY_SKIRT).build());
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(output -> output.accept(item));
        // Minecraft skips block use when crouching with an item; predict the wrapping action on both sides.
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            var state = level.getBlockState(hit.getBlockPos());
            if (!(state.getBlock() instanceof HarbourGiftFir fir) || !player.isShiftKeyDown() || OperatorRegions.tool(player.getItemInHand(hand)))
                return InteractionResult.PASS;
            return fir.interact(state, level, hit.getBlockPos(), player, hand);
        });
    }

    private HarbourGiftFir(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(PARCELS, 0));
    }

    private static int[][] makeCells() {
        List<int[]> cells = new ArrayList<>();
        for (int y = 0; y < 4; y++) {
            for (int[] xz : FLOOR) {
                if (y != 3 || xz[0] == 0 || xz[1] == 0) cells.add(new int[] {xz[0], y, xz[1]});
            }
        }
        cells.add(new int[] {0,4,0});
        return cells.toArray(int[][]::new);
    }

    @Override public int[][] cells() { return CELLS; }
    @Override public IntegerProperty partProperty() { return PART; }
    @Override public int across() { return 3; }
    @Override public int deep() { return 3; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(PARCELS);
    }

    private static Map<Direction,VoxelShape[][]> makeShapes() {
        Map<Direction,VoxelShape[][]> out = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            VoxelShape[][] shapes = new VoxelShape[CELLS.length][3];
            for (int i = 0; i < CELLS.length; i++) {
                int[] cell = CELLS[i];
                // A round canopy stepped in towards its crown. Empty corners between limbs stay walkable.
                int reach = switch (cell[1]) { case 1 -> 23; case 2 -> 18; case 3 -> 12; default -> 5; };
                if (cell[1] > 0 && cell[0] != 0 && cell[2] != 0) reach -= 5;
                int cx = 8 + 16 * cell[0], cz = 8 - 16 * cell[2];
                for (int n = 0; n <= 2; n++) {
                    if (i > 0 && i < 9) {
                        // Each bare pile has its own shallow piece of woven tree skirt to aim at.
                        shapes[i][n] = LongDecorationBlock.shape(new double[][]{{1,.0,1,15, n == 0 ? 1 : n == 1 ? 7 : 12,15}}, direction);
                    } else {
                        int x0 = Math.max(0, cx-reach), z0 = Math.max(0, cz-reach);
                        int x1 = Math.min(16, cx+reach), z1 = Math.min(16, cz+reach);
                        shapes[i][n] = x0 >= x1 || z0 >= z1 ? Shapes.empty() :
                            LongDecorationBlock.shape(new double[][]{{x0,0,z0,x1,16,z1}}, direction);
                    }
                }
            }
            out.put(direction, shapes);
        }
        return out;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING))[part(state)][state.getValue(PARCELS)];
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos master = context.getClickedPos();
        BlockPos below = master.below();
        if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) return null;
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockState tree = defaultBlockState().setValue(FACING, facing);
        for (int i = 0; i < cells().length; i++) {
            BlockPos at = partPos(master, facing, i);
            if (!level.hasChunkAt(at) || !level.getBlockState(at).isAir()
                    || !level.isUnobstructed(tree.setValue(partProperty(), i), at, CollisionContext.empty())) return null;
        }
        return super.getStateForPlacement(context);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return part(state) == MASTER ? new FirGifts(pos, state) : null;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hit) {
        return interact(state, level, pos, player, hand);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return interact(state, level, pos, player, InteractionHand.MAIN_HAND);
    }

    /** Handles the block action alone, including in protected public spaces. A held block never falls through to placement. */
    public InteractionResult interact(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (player.isSpectator()) return InteractionResult.FAIL;
        var master = masterPos(pos, state);
        var facing = state.getValue(FACING);
        if (!(level.getBlockEntity(master) instanceof FirGifts gifts) || gifts.getBlockState().getValue(FACING) != facing)
            return InteractionResult.FAIL;
        boolean staff = TownProtection.exempt(player);
        for (int i = 0; i < cells().length; i++) {
            BlockPos at = partPos(master, facing, i);
            if (!LandRegistry.mayBuild(player, level, at) || (!staff && (OperatorRegions.protects(level, at)
                || (ApartmentRentals.protects(level, at) && !ApartmentRentals.mayUseFurniture(player, level, at)))))
                return InteractionResult.FAIL;
        }
        ItemStack held = player.getItemInHand(hand);
        int part = part(state);
        int pile = part > 0 && part <= 8 ? part - 1 : -1;
        if (!held.isEmpty()) {
            if (this == CITY_SKIRT && !io.github.jimbozoomer.jugcraft.town.TownDecor.cityTreeInSeason()) {
                player.sendOverlayMessage(Component.translatable("message.jugcraft.gift_fir.collection_only"));
                return InteractionResult.SUCCESS;
            }
            if (!player.isShiftKeyDown()) {
                player.sendOverlayMessage(Component.translatable("message.jugcraft.gift_fir.offer"));
                return InteractionResult.SUCCESS;
            }
            if (!gifts.deposit(held, pile)) {
                player.sendOverlayMessage(Component.translatable("message.jugcraft.gift_fir.full"));
                return InteractionResult.SUCCESS;
            }
            player.sendOverlayMessage(Component.translatable("message.jugcraft.gift_fir.wrapped"));
        } else {
            ItemStack received = gifts.collect(pile);
            if (received.isEmpty()) {
                player.sendOverlayMessage(Component.translatable("message.jugcraft.gift_fir.empty"));
                return InteractionResult.SUCCESS;
            }
            player.setItemInHand(hand, received);
            player.sendOverlayMessage(Component.translatable("message.jugcraft.gift_fir.received", received.getHoverName()));
        }
        level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, .8F, 1.2F);
        return InteractionResult.SUCCESS;
    }
}
