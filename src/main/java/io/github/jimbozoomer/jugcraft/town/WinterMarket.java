package io.github.jimbozoomer.jugcraft.town;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.util.List;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

public final class WinterMarket {
    private WinterMarket() {}
    public static Counter POST, BAKERY, TOYS;
    public static Item WRAPPED;
    public static ExtendedMenuType<WinterMarketMenu,WinterMarketMenu.Opening> MENU;

    record Offer(String id,int count,int price) {
        ItemStack stack() { return new ItemStack(BuiltInRegistries.ITEM.getValue(Jugcraft.id(id)),count); }
    }

    public static void register() {
        POST = counter("winter_wrapping_counter",List.of());
        BAKERY = counter("winter_bakery_counter",List.of(new Offer("gingerbread_cookie",3,1),
            new Offer("apple_cider_donut",2,1),new Offer("mulled_cider",1,2)));
        TOYS = counter("winter_toy_counter",List.of(new Offer("spinning_top",1,2),
            new Offer("turkey_pull_toy",1,3),new Offer("mayflower_model_ship",1,5)));
        var key = ResourceKey.create(Registries.ITEM,Jugcraft.id("addressed_winter_gift"));
        WRAPPED = Registry.register(BuiltInRegistries.ITEM,key,new Item(new Item.Properties().setId(key).stacksTo(1)) {
            @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
                if (player instanceof ServerPlayer sp)
                    WinterGiftPost.get(sp.level().getServer()).unwrap(sp,WinterGiftPost.parcelId(sp.getItemInHand(hand)),hand);
                return InteractionResult.SUCCESS;
            }
        });
        MENU = Registry.register(BuiltInRegistries.MENU,Jugcraft.id("winter_market"),
            new ExtendedMenuType<>(WinterMarketMenu::new, WinterMarketMenu.Opening.CODEC));
        WinterGiftPost.register();
        net.fabricmc.fabric.api.event.player.UseEntityCallback.EVENT.register((player,level,hand,entity,hit) -> {
            if (!(entity instanceof net.minecraft.world.entity.npc.villager.Villager)) return InteractionResult.PASS;
            var tags = entity.entityTags();
            Counter booth = tags.contains("winter_market_post") ? POST : tags.contains("winter_market_bakery") ? BAKERY
                : tags.contains("winter_market_toys") ? TOYS : null;
            if (booth == null) return InteractionResult.PASS;
            if (hand == InteractionHand.MAIN_HAND && player instanceof ServerPlayer customer) {
                for (var direction : Direction.Plane.HORIZONTAL) {
                    var pos = entity.blockPosition().relative(direction);
                    if (WinterMarketMenu.canUse(customer,pos,booth)) {
                        WinterMarketMenu.open(customer,pos,booth,false,0);
                        break;
                    }
                }
            }
            return InteractionResult.SUCCESS;
        });
    }

    private static Counter counter(String id,List<Offer> offers) {
        var key = ResourceKey.create(Registries.BLOCK,Jugcraft.id(id));
        var b = Registry.register(BuiltInRegistries.BLOCK,key,new Counter(
            BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_PLANKS).setId(key).noOcclusion(),offers));
        var ik = ResourceKey.create(Registries.ITEM,Jugcraft.id(id));
        var item = Registry.register(BuiltInRegistries.ITEM,ik,new BlockItem(b,new Item.Properties().setId(ik).useBlockDescriptionPrefix()));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(o -> o.accept(item));
        return b;
    }

    public static final class Counter extends Block {
        final List<Offer> offers;
        Counter(Properties properties,List<Offer> offers) {
            super(properties);
            this.offers = offers;
            registerDefaultState(defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,Direction.NORTH));
        }
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) {
            builder.add(HorizontalDirectionalBlock.FACING);
        }
        @Override public BlockState getStateForPlacement(BlockPlaceContext ctx) {
            return defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,ctx.getHorizontalDirection().getOpposite());
        }
        @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit) {
            if (player instanceof ServerPlayer sp && !sp.isSpectator() && WinterMarketMenu.canUse(sp,pos,this))
                WinterMarketMenu.open(sp,pos,this,false,0);
            return InteractionResult.SUCCESS;
        }
        @Override protected InteractionResult useItemOn(ItemStack held,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit) {
            return useWithoutItem(state,level,pos,player,hit);
        }
    }
}
