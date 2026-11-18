package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.FirGifts;
import io.github.jimbozoomer.jugcraft.agriculture.HarbourGiftFir;
import java.util.HashSet;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class HarbourGiftFirGameTests {
    private static final BlockPos CENTRE = new BlockPos(3, 2, 3);

    private static BlockHitResult hit(BlockPos pos) {
        return new BlockHitResult(Vec3.atCenterOf(pos).relative(Direction.UP,.5), Direction.UP, pos, false);
    }

    private static BlockPos plant(GameTestHelper h) {
        var tree = HarbourGiftFir.TREE;
        var master = h.absolutePos(CENTRE);
        h.getLevel().setBlockAndUpdate(master, tree.defaultBlockState());
        tree.setPlacedBy(h.getLevel(), master, tree.defaultBlockState(), null, ItemStack.EMPTY);
        return master;
    }

    @GameTest
    public void winterMarketChecksTheWholeRoomBeforePlacingOneTree(GameTestHelper h) {
        var level = h.getLevel();
        var tree = HarbourGiftFir.TREE;
        var master = h.absolutePos(CENTRE);
        var player = TestPlayers.survivor(h);
        player.setPos(master.getX()+.5, master.getY(), master.getZ()-2.5);
        player.setYRot(0);
        level.setBlockAndUpdate(master.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(master.above(4), Blocks.GLASS.defaultBlockState());
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(tree));
        var context = new UseOnContext(player, InteractionHand.MAIN_HAND, hit(master.below()));
        player.getMainHandItem().useOn(context);
        h.assertTrue(!level.getBlockState(master).is(tree) && player.getMainHandItem().getCount() == 1
            && level.getBlockState(master.above(4)).is(Blocks.GLASS), "An obstructed crown leaves the world and inventory intact");
        level.removeBlock(master.above(4), false);
        player.getMainHandItem().useOn(context);
        h.assertTrue(player.getMainHandItem().isEmpty() && level.getBlockEntity(master) instanceof FirGifts, "One item plants the tree");
        var facing = level.getBlockState(master).getValue(HarbourGiftFir.FACING);
        var positions = new HashSet<BlockPos>();
        for (int i = 0; i < tree.cells().length; i++) {
            var at = tree.partPos(master, facing, i);
            var state = level.getBlockState(at);
            h.assertTrue(state.is(tree) && state.getValue(HarbourGiftFir.PART) == i && tree.masterPos(at,state).equals(master),
                "Each visible section resolves to the same trunk: "+i);
            positions.add(at);
        }
        h.assertTrue(positions.size() == 33 && positions.contains(master.above(4)), "Stepped five-block crown");
        level.destroyBlock(tree.partPos(master,facing,24), true);
        h.assertTrue(positions.stream().noneMatch(p -> level.getBlockState(p).is(tree)) && drops(h,master,new ItemStack(tree))==1,
            "One broken bough removes all sections and drops one fir");
        h.succeed();
    }

    @GameTest
    public void winterMarketWrapsSixteenRealStacksAndRestoresTheirComponents(GameTestHelper h) {
        var master = plant(h);
        var level = h.getLevel();
        var gifts = (FirGifts)level.getBlockEntity(master);
        var originals = new ItemStack[16];
        for (int i = 0; i < originals.length; i++) {
            originals[i] = new ItemStack(Items.COOKIE, i+1);
            originals[i].set(DataComponents.CUSTOM_NAME,Component.literal("Winter parcel "+i));
            var held = originals[i].copy();
            h.assertTrue(gifts.deposit(held,-1) && held.isEmpty(), "Gift transfers once");
        }
        for (int i = 0; i < 8; i++) {
            var at = HarbourGiftFir.TREE.partPos(master,Direction.NORTH,i+1);
            h.assertTrue(level.getBlockState(at).getValue(HarbourGiftFir.PARCELS)==2, "Both parcels are visible at pile "+i);
        }
        var excess = new ItemStack(Items.EMERALD,3);
        h.assertTrue(!gifts.deposit(excess,-1) && excess.getCount()==3,"A full tree refuses without changing the offered stack");
        var saved = gifts.saveWithoutMetadata(level.registryAccess());
        var restored = new FirGifts(master,gifts.getBlockState());
        restored.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved));
        for (int i = 0; i < 8; i++) {
            h.assertTrue(ItemStack.matches(originals[i+8],restored.collect(i)) && ItemStack.matches(originals[i],restored.collect(i))
                && restored.collect(i).isEmpty(), "Layer order, exact quantities and custom names survive reload "+i);
        }
        h.succeed();
    }

    @GameTest
    public void winterMarketPublicUseWrapsAndUnwrapsWithoutPlacingHeldBlocks(GameTestHelper h) {
        var master = plant(h);
        var level = h.getLevel();
        var gifts = (FirGifts)level.getBlockEntity(master);
        var at = HarbourGiftFir.TREE.partPos(master,Direction.NORTH,2);
        var player = TestPlayers.survivor(h);
        var held = new ItemStack(Items.GOLD_BLOCK,7);
        player.snapTo(Vec3.atBottomCenterOf(at.north(2)));
        held.set(DataComponents.CUSTOM_NAME, Component.literal("For the market"));
        player.setItemInHand(InteractionHand.MAIN_HAND,held.copy());
        h.assertTrue(player.gameMode.useItemOn(player,level,player.getMainHandItem(),InteractionHand.MAIN_HAND,hit(at)).consumesAction()
            && gifts.count(1)==0 && ItemStack.matches(player.getMainHandItem(),held), "Ordinary use explains public gifting");
        player.setShiftKeyDown(true);
        h.assertTrue(player.gameMode.useItemOn(player,level,player.getMainHandItem(),InteractionHand.MAIN_HAND,hit(at)).consumesAction() &&
            player.getMainHandItem().isEmpty() && gifts.count(1)==1 && level.getBlockState(at.above()).is(HarbourGiftFir.TREE),
            "Crouch use gifts the held stack; it cannot place that block above the pile");
        var buyer = TestPlayers.survivor(h);
        buyer.snapTo(Vec3.atBottomCenterOf(at.north(2)));
        for (int s=1; s<36; s++) buyer.getInventory().setItem(s,new ItemStack(Items.COBBLESTONE,64));
        buyer.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        h.assertTrue(buyer.gameMode.useItemOn(buyer,level,buyer.getMainHandItem(),InteractionHand.MAIN_HAND,hit(at)).consumesAction()
            && ItemStack.matches(buyer.getMainHandItem(),held) && gifts.count(1)==0 &&
            level.getBlockState(at).getValue(HarbourGiftFir.PARCELS)==0, "A different player can collect directly into their empty hand");
        h.succeed();
    }

    @GameTest
    public void winterMarketBreakingAParcelReturnsEachStoredGiftOnce(GameTestHelper h) {
        var master = plant(h);
        var level = h.getLevel();
        var gifts = (FirGifts)level.getBlockEntity(master);
        var pick = new ItemStack(Items.IRON_PICKAXE);
        pick.setDamageValue(17);
        pick.set(DataComponents.CUSTOM_NAME,Component.literal("Old Tom's pick"));
        gifts.deposit(pick.copy(),5);
        gifts.deposit(new ItemStack(Items.APPLE,11),6);
        level.destroyBlock(HarbourGiftFir.TREE.partPos(master,Direction.NORTH,6),true);
        h.assertTrue(drops(h,master,pick)==1 && drops(h,master,new ItemStack(Items.APPLE))==11
            && drops(h,master,new ItemStack(HarbourGiftFir.TREE))==1 && level.getBlockEntity(master)==null,
            "Breaking a gift pile returns the named damaged tool, every apple, and one tree");
        h.succeed();
    }

    @GameTest
    public void winterMarketAProtectedCrownCannotBeEmptiedFromAnOutsidePile(GameTestHelper h) throws Exception {
        var master = plant(h);
        var level = h.getLevel();
        var gifts = (FirGifts)level.getBlockEntity(master);
        gifts.deposit(new ItemStack(Items.DIAMOND,2),0);
        var server = level.getServer();
        var commands = server.getCommands().getDispatcher();
        var warden = TestPlayers.mock(h);
        var op = server.createCommandSourceStack().withEntity(warden).withSuppressedOutput();
        var region = "gift_" + warden.getUUID().toString().replace("-","");
        var crown = master.above(4);
        warden.snapTo(Vec3.atBottomCenterOf(crown));
        commands.execute("protect pos1",op);
        commands.execute("protect pos2",op);
        commands.execute("protect create " + region,op);
        try {
            var visitor = TestPlayers.survivor(h);
            var pile = HarbourGiftFir.TREE.partPos(master,Direction.NORTH,1);
            visitor.snapTo(Vec3.atBottomCenterOf(pile.north(2)));
            visitor.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
            visitor.gameMode.useItemOn(visitor,level,visitor.getMainHandItem(),InteractionHand.MAIN_HAND,hit(pile));
            h.assertTrue(gifts.count(0)==1 && visitor.getMainHandItem().isEmpty(), "A remote region protects the whole fir");
            visitor.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.STONE,7));
            visitor.setShiftKeyDown(true);
            visitor.gameMode.useItemOn(visitor,level,visitor.getMainHandItem(),InteractionHand.MAIN_HAND,hit(pile));
            h.assertTrue(gifts.count(0)==1 && visitor.getMainHandItem().getCount()==7, "Refused gifts stay in hand");
            level.explode(null, master.getX()+.5, master.getY()+.5, master.getZ()+.5, 3,
                net.minecraft.world.level.Level.ExplosionInteraction.TNT);
            h.assertTrue(level.getBlockState(pile).is(HarbourGiftFir.TREE) && gifts.count(0)==1,
                "An explosion outside the region cannot tear down its protected crown or spill the gift");
        } finally {
            commands.execute("protect remove "+region,op);
        }
        h.succeed();
    }

    private static int drops(GameTestHelper h, BlockPos pos, ItemStack stack) {
        return h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(2),
            e -> ItemStack.isSameItemSameComponents(e.getItem(),stack)).stream().mapToInt(e -> e.getItem().getCount()).sum();
    }
}
