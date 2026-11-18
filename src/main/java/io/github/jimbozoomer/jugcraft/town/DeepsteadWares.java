package io.github.jimbozoomer.jugcraft.town;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Deepstead's three-shift guild counter. Prices and sales are served by the ordinary town shop. */
public final class DeepsteadWares {
    public static final String LAMPWRIGHT = "deepstead_lampwright";
    public static final String OUTFITTER = "deepstead_outfitter";
    public static final String STONEWRIGHT = "deepstead_stonewright";
    public static final String COPPER_KETTLE = "deepstead_copper_kettle";
    public static Block KNOT_TILE;
    public static Block READING_LAMP;
    public static Block GALLERY_CHIMES;
    public static Item SHAFT_PLUMMET;
    public static Item SURVEYORS_REEL;
    public static final IntegerProperty BRIGHTNESS = IntegerProperty.create("brightness", 0, 2);

    private DeepsteadWares() {}

    public static void register() {
        KNOT_TILE = furnishing("miners_knot_tile", BlockBehaviour.Properties.ofFullCopy(Blocks.POLISHED_DEEPSLATE), Block::new);
        READING_LAMP = furnishing("geode_reading_lamp", BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK)
                .noOcclusion().lightLevel(s -> s.getValue(BRIGHTNESS) * 7), ReadingLamp::new);
        GALLERY_CHIMES = furnishing("gallery_chimes", BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                .strength(1.0F).noOcclusion(), GalleryChimesBlock::new);
        SHAFT_PLUMMET = tool("shaft_plummet", ShaftPlummet::new);
        SURVEYORS_REEL = tool("surveyors_reel", SurveyorsReel::new);
        DeepsteadSpecialties.register();
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(output -> {
            output.accept(KNOT_TILE); output.accept(READING_LAMP); output.accept(GALLERY_CHIMES);
        });
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(output -> {
            output.accept(SHAFT_PLUMMET); output.accept(SURVEYORS_REEL);
        });
    }

    static Item.Properties properties(String name, ResourceKey<Item> key) {
        return new Item.Properties().setId(key).component(DataComponents.LORE,
                new ItemLore(List.of(Component.translatable("tooltip.jugcraft." + name))));
    }

    static Block furnishing(String name, BlockBehaviour.Properties settings, Function<BlockBehaviour.Properties, Block> create) {
        var key = ResourceKey.create(Registries.BLOCK, Jugcraft.id(name));
        var block = Registry.register(BuiltInRegistries.BLOCK, key, create.apply(settings.setId(key)));
        var item = ResourceKey.create(Registries.ITEM, Jugcraft.id(name));
        Registry.register(BuiltInRegistries.ITEM, item, new BlockItem(block, properties(name, item).useBlockDescriptionPrefix()));
        return block;
    }

    static Item tool(String name, Function<Item.Properties, Item> create) {
        var key = ResourceKey.create(Registries.ITEM, Jugcraft.id(name));
        return Registry.register(BuiltInRegistries.ITEM, key, create.apply(properties(name, key).stacksTo(1)));
    }

    public static boolean isGuildShop(String shop) {
        return LAMPWRIGHT.equals(shop) || OUTFITTER.equals(shop) || STONEWRIGHT.equals(shop) || COPPER_KETTLE.equals(shop);
    }

    public static void open(ServerPlayer player, DeepsteadDwarf merchant) {
        double range = TownShops.get().tradeRange;
        if (!player.isSpectator() && merchant.isMerchant() && merchant.isAlive() && player.level() == merchant.level()
                && isGuildShop(merchant.shop()) && player.distanceToSqr(merchant) <= range * range) ShopMenu.open(player, merchant);
    }

    private static final class ReadingLamp extends Block {
        private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 14, 14);
        ReadingLamp(Properties settings) {
            super(settings);
            registerDefaultState(defaultBlockState().setValue(BRIGHTNESS, 2));
        }
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(BRIGHTNESS); }
        @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos at, CollisionContext context) { return SHAPE; }
        @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos at, Player player, BlockHitResult hit) {
            if (!level.isClientSide()) {
                var next = state.cycle(BRIGHTNESS);
                level.setBlock(at, next, Block.UPDATE_ALL);
                level.playSound(null, at, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.4F, 0.8F + next.getValue(BRIGHTNESS) * 0.2F);
            }
            return InteractionResult.SUCCESS;
        }
    }

    public record Sounding(String surface, int depth, int height) {}
    /** The line hits the first liquid or collision surface; it cannot probe through solid rock. */
    public static Sounding soundShaft(ServerLevel level, BlockPos lip) {
        if (!level.hasChunkAt(lip)) return new Sounding("unavailable", 0, lip.getY());
        for (int drop = 0; drop <= 64 && lip.getY() - drop >= level.getMinY(); drop++) {
            var point = lip.below(drop);
            var state = level.getBlockState(point);
            if (state.getFluidState().is(net.minecraft.tags.FluidTags.WATER)) return new Sounding("water", drop, point.getY());
            if (state.getFluidState().is(net.minecraft.tags.FluidTags.LAVA)) return new Sounding("lava", drop, point.getY());
            if (!state.getFluidState().isEmpty()) return new Sounding("liquid", drop, point.getY());
            if (!state.getCollisionShape(level, point).isEmpty()) return new Sounding("stone", drop, point.getY());
        }
        return new Sounding("unreached", 0, lip.getY());
    }

    private static final class ShaftPlummet extends Item {
        ShaftPlummet(Properties settings) { super(settings); }
        @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
            if (level instanceof ServerLevel server) {
                var reading = soundShaft(server, player.blockPosition().relative(player.getDirection()));
                player.sendOverlayMessage(Component.translatable("message.jugcraft.shaft_plummet." + reading.surface(), reading.depth(), reading.height()));
                level.playSound(null, player.blockPosition(), SoundEvents.CHAIN_HIT, SoundSource.PLAYERS, 0.5F, 1.3F);
                player.getCooldowns().addCooldown(player.getItemInHand(hand), 8);
            }
            return InteractionResult.SUCCESS;
        }
    }

    private static final class SurveyorsReel extends Item {
        SurveyorsReel(Properties settings) { super(settings); }
        @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
            if (!level.isClientSide()) {
                var reel = player.getItemInHand(hand);
                var here = player.blockPosition();
                var saved = reel.get(DataComponents.LODESTONE_TRACKER);
                if (player.isShiftKeyDown()) {
                    reel.set(DataComponents.LODESTONE_TRACKER, new LodestoneTracker(Optional.of(GlobalPos.of(level.dimension(), here)), false));
                    player.sendOverlayMessage(Component.translatable("message.jugcraft.surveyors_reel.set", here.toShortString()));
                    level.playSound(null, here, SoundEvents.TRIPWIRE_CLICK_ON, SoundSource.PLAYERS, 0.45F, 1.0F);
                } else if (saved == null || saved.target().isEmpty()) {
                    player.sendOverlayMessage(Component.translatable("tooltip.jugcraft.surveyors_reel"));
                } else {
                    var mark = saved.target().get();
                    if (mark.dimension() != level.dimension()) {
                        player.sendOverlayMessage(Component.translatable("message.jugcraft.surveyors_reel.other", mark.dimension().identifier().toString()));
                    } else {
                        var delta = here.subtract(mark.pos());
                        long span = Math.round(Math.hypot(delta.getX(), delta.getZ()));
                        player.sendOverlayMessage(Component.translatable("message.jugcraft.surveyors_reel.distance",
                                span, delta.getY(), delta.getX(), delta.getZ()));
                    }
                }
                player.getCooldowns().addCooldown(reel, 5);
            }
            return InteractionResult.SUCCESS;
        }
    }
}
