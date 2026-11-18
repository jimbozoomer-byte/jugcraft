package io.github.jimbozoomer.jugcraft.town;

import io.github.jimbozoomer.jugcraft.finance.LandRegistry;
import io.github.jimbozoomer.jugcraft.furniture.DesignedFurnitureBlock;
import io.github.jimbozoomer.jugcraft.menagerie.HouseholdBlock;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** The tenant exception is limited to furnishing air and using/removing a recorded piece. */
public final class ApartmentRentals {
    private ApartmentRentals() {}

    static Jugs.Data data(ServerLevel level) { return Jugs.data(level.getServer()); }
    static ApartmentBook.Room at(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel server) || level.dimension() != Level.OVERWORLD) return null;
        return data(server).apartments.at(pos);
    }
    public static boolean protects(Level level, BlockPos pos) { return at(level, pos) != null; }
    public static boolean privateFixture(Level level, BlockPos pos) {
        var room=at(level,pos);
        return room!=null && room.furniture.containsKey(pos);
    }
    public static boolean customAt(Level level, BlockPos pos) {
        var room=at(level,pos);
        return room!=null && !room.generated;
    }
    public static boolean crossesBoundary(Level level, BlockPos a, BlockPos b) { return at(level,a) != at(level,b); }
    public static boolean touchesChunk(Level level, long key) {
        return level instanceof ServerLevel server && level.dimension() == Level.OVERWORLD && data(server).apartments.touchesChunk(key);
    }

    private static ApartmentBook.Room fromPlan(BlockPos origin, WesternDistrictPlan.Room definition) {
        var b=definition.bounds();
        var lo=origin.offset(b[0],b[1],b[2]); var hi=origin.offset(b[3],b[4],b[5]);
        return new ApartmentBook.Room(definition.id(),lo,hi,(b[3]-b[0]+1)*(b[5]-b[2]+1)>=190 ? 12 : 8,true);
    }
    /** Preflight the entire batch before registering any. Retries preserve every existing lease. */
    private static List<ApartmentBook.Room> pendingRooms(ServerLevel level, BlockPos origin, List<WesternDistrictPlan.Room> definitions) {
        if (level.dimension() != Level.OVERWORLD) return null;
        var data=data(level); var book=data.apartments; var pending=new ArrayList<ApartmentBook.Room>();
        for (var definition : definitions) {
            var room=fromPlan(origin,definition); var existing=book.named(room.id);
            if (existing!=null && existing.sameDefinition(room)) continue;
            if (!book.canAdd(room) || pending.stream().anyMatch(r -> r.id.equals(room.id) || r.overlaps(room))) return null;
            pending.add(room);
        }
        return pending;
    }
    public static boolean validateInstallRooms(ServerLevel level, BlockPos origin, List<WesternDistrictPlan.Room> definitions) {
        return pendingRooms(level,origin,definitions)!=null;
    }
    public static boolean installRooms(ServerLevel level, BlockPos origin, List<WesternDistrictPlan.Room> definitions) {
        var pending=pendingRooms(level,origin,definitions);
        if (pending==null) return false;
        var wallet=data(level);
        pending.forEach(wallet.apartments::add);
        if (!pending.isEmpty()) wallet.setDirty();
        return true;
    }
    public static boolean canRemoveRooms(ServerLevel level, BlockPos origin, List<WesternDistrictPlan.Room> definitions) {
        var book=data(level).apartments; long now=System.currentTimeMillis();
        for (var definition:definitions) {
            var room=book.named(definition.id());
            if (room!=null && (!room.sameDefinition(fromPlan(origin,definition)) || !room.available(now))) return false;
        }
        return true;
    }
    public static boolean removeRooms(ServerLevel level, BlockPos origin, List<WesternDistrictPlan.Room> definitions) {
        if (!canRemoveRooms(level,origin,definitions)) return false;
        var data=data(level);
        for (var definition:definitions) {
            var room=data.apartments.named(definition.id());
            if (room!=null) { data.apartments.remove(room); data.setDirty(); }
        }
        return true;
    }

    static String key(BlockState state) { return BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString(); }

    /** Single-cell furnishings only. Multi-block beds/wardrobes and machine items need separate ownership of every part. */
    static boolean furniture(Block block) {
        if (block instanceof GuildFurnishingBlock || block==DeepsteadWares.KNOT_TILE || block==DeepsteadWares.READING_LAMP || block==DeepsteadWares.GALLERY_CHIMES
                || block==Blocks.BARREL || block==Blocks.BOOKSHELF || block==Blocks.LANTERN || block==Blocks.SOUL_LANTERN
                || block instanceof WoolCarpetBlock || block instanceof DesignedFurnitureBlock) return true;
        if (block.getClass()==io.github.jimbozoomer.jugcraft.tower.FurnitureBlock.class
                || block instanceof io.github.jimbozoomer.jugcraft.nuclear.SeatFurnitureBlock
                || block instanceof io.github.jimbozoomer.jugcraft.nuclear.RowFurnitureBlock) return true;
        if (!(block instanceof HouseholdBlock household)) return false;
        var half=block.getStateDefinition().getProperty("half");
        var type=block.getStateDefinition().getProperty("type");
        if (half!=null && half.getValue("upper").isPresent() || type!=null && type.getValue("head").isPresent()) return false;
        return household.apartmentFurnishing();
    }

    private static boolean ownPiece(ApartmentBook.Room room, Player player, Level level, BlockPos pos) {
        return room!=null && !room.packing && player.getUUID().equals(room.tenant) && key(level.getBlockState(pos)).equals(room.furniture.get(pos))
            && LandRegistry.mayBuild(player,level,pos);
    }
    public static boolean mayRetrieve(Player player, Level level, BlockPos pos) {
        return player instanceof ServerPlayer && !player.isSpectator() && ownPiece(at(level,pos),player,level,pos);
    }
    public static boolean mayUseFurniture(Player player, Level level, BlockPos pos) {
        var room=at(level,pos);
        return room!=null && room.active(player.getUUID(),System.currentTimeMillis()) && !player.isSpectator()
            && ownPiece(room,player,level,pos);
    }
    static void removed(Level level, BlockPos pos) {
        var room=at(level,pos);
        if (room!=null && !room.packing && room.furniture.remove(pos)!=null) data((ServerLevel)level).setDirty();
    }

    /** Called by town protection before public furniture/use fallback. Clients never authorize writes. */
    static InteractionResult use(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        if (!(player instanceof ServerPlayer server) || player.isSpectator()) return InteractionResult.PASS;
        var pos=hit.getBlockPos(); var room=at(level,pos); var state=level.getBlockState(pos);
        boolean owns=ownPiece(room,player,level,pos);
        var stack=player.getItemInHand(hand);
        if (stack.getItem() instanceof BlockItem item && item.getClass()==BlockItem.class && furniture(item.getBlock())
                && (!owns || player.isShiftKeyDown() || state.getMenuProvider(level,pos)==null)) {
            var context=new BlockPlaceContext(player,hand,stack,hit); var target=context.getClickedPos();
            var destination=at(level,target);
            if (destination!=null && destination.active(player.getUUID(),System.currentTimeMillis()) && !doorway(level,target)
                    && level.getBlockState(target).isAir() && server.level().hasChunkAt(target)
                    && level.mayInteract(player,target) && player.mayUseItemAt(target,hit.getDirection(),stack)
                    && LandRegistry.mayBuild(player,level,pos) && LandRegistry.mayBuild(player,level,target)) {
                // Call the vetted single-block item's actual placement (collision, facing, consumption and components).
                var result=item.place(context);
                if (result.consumesAction() && level.getBlockState(target).is(item.getBlock())) {
                    destination.furniture.put(target.immutable(),key(level.getBlockState(target)));
                    data(server.level()).setDirty();
                }
                return result.consumesAction()?result:InteractionResult.FAIL;
            }
        }
        if (owns) {
            if (!room.active(player.getUUID(),System.currentTimeMillis())) {
                player.sendOverlayMessage(net.minecraft.network.chat.Component.literal("Lease expired. Break furniture to pack it, or /apartment renew "+room.id+"."));
                return InteractionResult.FAIL;
            }
            // Use the exact tenant-owned fixture. A failed action cannot fall through to a held tool/bucket.
            var result=state.useWithoutItem(level,player,hit);
            return result.consumesAction()?result:InteractionResult.FAIL;
        }
        // Residents may sleep in the furnished bed; both halves must belong to this same interior.
        if (room!=null && state.getBlock() instanceof BedBlock) {
            var facing=state.getValue(BedBlock.FACING);
            boolean head=state.getValue(BedBlock.PART)==net.minecraft.world.level.block.state.properties.BedPart.HEAD;
            var partner=pos.relative(head?facing.getOpposite():facing);
            if (room.active(player.getUUID(),System.currentTimeMillis()) && room.contains(partner) && level.getBlockState(partner).is(state.getBlock())
                    && LandRegistry.mayBuild(player,level,pos)) {
                var result=state.useWithoutItem(level,player,hit);
                return result.consumesAction()?result:InteractionResult.FAIL;
            }
            return InteractionResult.FAIL;
        }
        if (room!=null && room.furniture.containsKey(pos)) return InteractionResult.FAIL;
        return InteractionResult.PASS;
    }

    private static boolean doorway(Level level, BlockPos pos) {
        for (var direction:net.minecraft.core.Direction.Plane.HORIZONTAL)
            if (level.getBlockState(pos.relative(direction)).getBlock() instanceof DoorBlock) return true;
        return false;
    }
}

