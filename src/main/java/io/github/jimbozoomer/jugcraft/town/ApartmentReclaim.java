package io.github.jimbozoomer.jugcraft.town;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.mixin.ApartmentPlayerSaveAccess;
import java.util.*;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;

/** Staffed move-out. Source blocks and the inventory packed from them share the same physical chunk save. */
final class ApartmentReclaim {
    record Stack(UUID owner,long serial,ItemStack item) {
        static final Codec<Stack> CODEC=RecordCodecBuilder.create(i->i.group(UUIDUtil.STRING_CODEC.fieldOf("owner").forGetter(Stack::owner),
            Codec.LONG.fieldOf("serial").forGetter(Stack::serial),ItemStack.CODEC.fieldOf("item").forGetter(Stack::item)).apply(i,Stack::new));
    }
    record Store(long revision,List<Stack> stacks) {
        static final Store EMPTY=new Store(0,List.of());
        static final Codec<Store> CODEC=RecordCodecBuilder.create(i->i.group(Codec.LONG.fieldOf("revision").forGetter(Store::revision),
            Stack.CODEC.listOf().fieldOf("stacks").forGetter(Store::stacks)).apply(i,Store::new));
    }
    private record Piece(BlockPos pos,List<ItemStack> items) {}
    private static final String CARGO="apartment_held_furniture", CLAIM="apartment_collections";
    static AttachmentType<Store> cargo;
    private static AttachmentType<Map<String,Long>> claimed;
    static void register() {
        cargo=AttachmentRegistry.<Store>builder().persistent(Store.CODEC).buildAndRegister(Jugcraft.id(CARGO));
        claimed=AttachmentRegistry.<Map<String,Long>>builder().persistent(Codec.unboundedMap(Codec.STRING,Codec.LONG)).copyOnDeath().buildAndRegister(Jugcraft.id(CLAIM));
    }
    private ApartmentReclaim() {}
    private static List<ItemStack> contents(ServerLevel l,BlockPos p) {
        var state=l.getBlockState(p); var stacks=new ArrayList<ItemStack>();
        var furniture=state.getCloneItemStack(l,p,false);
        if (furniture.isEmpty()) throw new IllegalStateException("Cannot recover furniture at "+p.toShortString());
        // Clone without inventory components, then transfer the actual occupied slots exactly once.
        furniture.remove(DataComponents.CONTAINER);
        furniture.remove(DataComponents.BLOCK_ENTITY_DATA);
        var blockEntity=l.getBlockEntity(p);
        if (blockEntity instanceof Nameable named && named.hasCustomName()) furniture.set(DataComponents.CUSTOM_NAME,named.getCustomName());
        stacks.add(furniture);
        if (blockEntity instanceof Container inventory) {
            for (var player:l.players())
                if (player.containerMenu!=player.inventoryMenu && player.containerMenu.slots.stream().anyMatch(s->s.container==inventory))
                    throw new IllegalStateException("Close the open container at "+p.toShortString()+" first.");
            for (int i=0;i<inventory.getContainerSize();i++) if (!inventory.getItem(i).isEmpty()) stacks.add(inventory.getItem(i).copy());
        }
        return stacks;
    }
    private static Optional<CompoundTag> attachment(CompoundTag nbt,String id) {
        return nbt.getCompound("fabric:attachments").flatMap(t->t.getCompound("jugcraft:"+id));
    }
    private static boolean saved(ServerLevel level,LevelChunk chunk) {
        var store=chunk.getAttachedOrElse(cargo,Store.EMPTY);
        if (store.revision==0) return true;
        return level.getChunkSource().chunkMap.read(chunk.getPos()).join().flatMap(t->attachment(t,CARGO))
            .map(t->t.getLongOr("revision",-1)==store.revision).orElse(false);
    }
    static String pack(ServerLevel level,ApartmentBook.Room room) {
        if (room.tenant==null || room.until>System.currentTimeMillis()) return "Only expired leases can be packed.";
        if (room.furniture.isEmpty()) return "The room is already clear.";
        // Group by source chunk before changing anything. A compact room might still cross four or more chunks.
        var batches=new LinkedHashMap<LevelChunk,List<Piece>>();
        for (var entry:room.furniture.entrySet()) {
            var p=entry.getKey(); var chunk=level.getChunkSource().getChunkNow(p.getX()>>4,p.getZ()>>4);
            if (chunk==null) return "Bring all apartment chunks into view first.";
            var batch=batches.computeIfAbsent(chunk,k->new ArrayList<>());
            var state=level.getBlockState(p); if (state.isAir()) continue; // Previous pass already packed this cell.
            if (!ApartmentRentals.key(state).equals(entry.getValue()) || !ApartmentRentals.furniture(state.getBlock()))
                return "Furniture has changed at "+p.toShortString()+". Inspect it before packing.";
            batch.add(new Piece(p,contents(level,p)));
        }
        for (var entry:batches.entrySet()) {
            var bucket=entry.getKey().getAttachedOrElse(cargo,Store.EMPTY);
            if (bucket.stacks.size()+entry.getValue().stream().mapToInt(p->p.items.size()).sum()>8192)
                return "This chunk's reclaim storage is full. Collect existing deliveries first.";
        }
        room.packing=true;
        ApartmentRentals.data(level).setDirty(); level.getDataStorage().saveAndJoin();
        for (var entry:batches.entrySet()) {
            var chunk=entry.getKey(); var old=chunk.getAttachedOrElse(cargo,Store.EMPTY);
            long serial=old.revision; var output=new ArrayList<>(old.stacks);
            // Suppress neighbor/drop callbacks. Nothing spills, including stacked lamps and cabinet contents.
            // Every successful removal gets its chunk attachment before control returns to the tick loop.
            try {
                for (var piece:entry.getValue()) {
                    if (!level.setBlock(piece.pos,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS|Block.UPDATE_SKIP_ALL_SIDEEFFECTS|Block.UPDATE_KNOWN_SHAPE))
                        throw new IllegalStateException("Could not remove the verified furnishing at "+piece.pos.toShortString());
                    for (var item:piece.items) output.add(new Stack(room.tenant,++serial,item));
                }
            } finally {
                if (serial!=old.revision) {
                    chunk.setAttached(cargo,new Store(serial,List.copyOf(output)));
                    chunk.markUnsaved();
                }
            }
        }
        // A synchronous chunk flush and disk read-back precede withdrawing the lease receipts.
        level.getChunkSource().save(true);
        if (batches.keySet().stream().anyMatch(c->!saved(level,c))) return "Chunk save unconfirmed. The room is still reserved; retry packout.";
        var wallet=ApartmentRentals.data(level);
        var hints=new HashMap<Long,BlockPos>();
        for (BlockPos p:wallet.reclaimLocations.getOrDefault(room.tenant,List.of())) hints.put(ChunkPos.pack(p),p);
        for (BlockPos p:room.furniture.keySet()) hints.put(ChunkPos.pack(p),p);
        wallet.reclaimLocations.put(room.tenant,List.copyOf(hints.values()));
        room.furniture.clear(); room.tenant=null; room.until=0; room.packing=false;
        wallet.setDirty(); level.getDataStorage().saveAndJoin();
        return null;
    }

    /** One source chunk per invocation. Durable per-player watermarks make retries idempotent even after a crash. */
    static String collect(ServerPlayer p) {
        var level=p.level(); if(level.dimension()!=Level.OVERWORLD || p.isSpectator() || !p.isAlive()) return "Visit the former room in the Overworld to reclaim.";
        var center=p.chunkPosition();
        for (int dz=-1;dz<=1;dz++) for (int dx=-1;dx<=1;dx++) {
            var chunk=level.getChunkSource().getChunkNow(center.x()+dx,center.z()+dz); if(chunk==null) continue;
            var store=chunk.getAttachedOrElse(cargo,Store.EMPTY);
            if (store.stacks.stream().noneMatch(s->s.owner.equals(p.getUUID()))) continue;
            String key=Long.toString(chunk.getPos().pack());
            var history=p.getAttachedOrElse(claimed,Map.of());
            long previous=history.getOrDefault(key,0L), last=previous;
            var slots=new ArrayList<Integer>();
            for (int i=0;i<p.getInventory().getNonEquipmentItems().size();i++) if(p.getInventory().getItem(i).isEmpty()) slots.add(i);
            var delivery=new ArrayList<Stack>();
            for (var s:store.stacks) if (s.owner.equals(p.getUUID()) && s.serial>previous && delivery.size()<slots.size()) { delivery.add(s); last=s.serial; }
            if (delivery.isEmpty() && store.stacks.stream().noneMatch(s->s.owner.equals(p.getUUID()) && s.serial<=previous))
                return "Make space in your main inventory and use /apartment reclaim again.";
            if (!saved(level,chunk)) { level.getChunkSource().save(true); if(!saved(level,chunk)) return "Storage save pending. Try again shortly."; }
            if (!delivery.isEmpty()) {
                for (int i=0;i<delivery.size();i++) p.getInventory().setItem(slots.get(i),delivery.get(i).item.copy());
                var receipts=new HashMap<>(history); receipts.put(key,last); p.setAttached(claimed,Map.copyOf(receipts));
                p.getInventory().setChanged(); p.inventoryMenu.broadcastChanges();
            }
            boolean confirmed=false;
            try {
                var players=level.getServer().getPlayerList();
                ((ApartmentPlayerSaveAccess)players).jugcraft$saveApartmentRecipient(p);
                var disk=players.loadPlayerData(p.nameAndId());
                confirmed=disk.flatMap(t->attachment(t,CLAIM)).map(t->t.getLongOr(key,0L)).orElse(0L)>=last;
            } finally {
                // On an I/O error the player must never gain a spendable, unconfirmed duplicate.
                // A successfully saved player image always contains its matching high-water mark.
                if (!confirmed && !delivery.isEmpty()) {
                    for (int i=0;i<delivery.size();i++) p.getInventory().setItem(slots.get(i),ItemStack.EMPTY);
                    p.setAttached(claimed,history);
                    p.getInventory().setChanged(); p.inventoryMenu.broadcastChanges();
                }
            }
            if (!confirmed) return "Player save unconfirmed. Your belongings remain in reclaim storage.";
            final long committed=last;
            var rest=store.stacks.stream().filter(s->!s.owner.equals(p.getUUID()) || s.serial>committed).toList();
            chunk.setAttached(cargo,new Store(store.revision+1,rest)); chunk.markUnsaved();
            return delivery.isEmpty()?"Previous delivery confirmed.":"Recovered "+delivery.size()+" item stacks. Use /apartment reclaim again for remaining stacks.";
        }
        var wallet=ApartmentRentals.data(level);
        var known=wallet.reclaimLocations.getOrDefault(p.getUUID(),List.of());
        // Loaded nearby chunks were checked above. Retire only hints we actually verified.
        var stillHeld=known.stream().filter(at->Math.abs((at.getX()>>4)-center.x())>1 || Math.abs((at.getZ()>>4)-center.z())>1
            || level.getChunkSource().getChunkNow(at.getX()>>4,at.getZ()>>4)==null).toList();
        if (known.size()!=stillHeld.size()) {
            if (stillHeld.isEmpty()) wallet.reclaimLocations.remove(p.getUUID());
            else wallet.reclaimLocations.put(p.getUUID(),stillHeld);
            wallet.setDirty();
        }
        var nearest=stillHeld.stream().min(Comparator.comparingDouble(at->at.distSqr(p.blockPosition())));
        return nearest.map(at->"Your packed furniture is in the Overworld near "+at.toShortString()+". Use /apartment reclaim there.")
            .orElse("No packed furniture nearby. Stand in or beside your former apartment.");
    }
}

