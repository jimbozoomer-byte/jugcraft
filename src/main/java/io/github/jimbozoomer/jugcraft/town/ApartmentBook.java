package io.github.jimbozoomer.jugcraft.town;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jimbozoomer.jugcraft.finance.LandRegistry;
import java.time.Duration;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;

/** Overworld rental interiors. Persisted in the same save record as the rent payment. */
public final class ApartmentBook {
    public static final long WEEK = Duration.ofDays(7).toMillis();
    static final int MAX_CUSTOM = 256;
    private final Map<String, Room> rooms = new TreeMap<>();
    private final Map<Long, List<Room>> chunks = new HashMap<>();

    public record Piece(BlockPos pos, String block) {
        static final Codec<Piece> CODEC = RecordCodecBuilder.create(i -> i.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(Piece::pos),
            Codec.STRING.fieldOf("block").forGetter(Piece::block)).apply(i, Piece::new));
    }

    public static final class Room {
        final String id;
        final BlockPos lo, hi;
        final int price;
        final boolean generated;
        UUID tenant;
        long until;
        boolean packing;
        final Map<BlockPos, String> furniture = new HashMap<>();

        static final Codec<Room> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("id").forGetter(r -> r.id),
            BlockPos.CODEC.fieldOf("from").forGetter(r -> r.lo),
            BlockPos.CODEC.fieldOf("to").forGetter(r -> r.hi),
            Codec.intRange(1, 100000).fieldOf("weekly").forGetter(r -> r.price),
            Codec.BOOL.fieldOf("generated").forGetter(r -> r.generated),
            UUIDUtil.STRING_CODEC.optionalFieldOf("tenant").forGetter(r -> Optional.ofNullable(r.tenant)),
            Codec.LONG.optionalFieldOf("until", 0L).forGetter(r -> r.until),
            Piece.CODEC.listOf().optionalFieldOf("furniture", List.of()).forGetter(r -> r.furniture.entrySet().stream()
                .map(e -> new Piece(e.getKey(), e.getValue())).toList()),
            Codec.BOOL.optionalFieldOf("packing",false).forGetter(r->r.packing)).apply(i, Room::new));

        Room(String id, BlockPos lo, BlockPos hi, int price, boolean generated, Optional<UUID> tenant, long until, List<Piece> pieces, boolean packing) {
            this.id = id; this.lo = lo.immutable(); this.hi = hi.immutable(); this.price = price; this.generated = generated;
            this.tenant = tenant.orElse(null); this.until = until;
            this.packing = packing;
            if (!validBounds(lo, hi)) throw new IllegalArgumentException("Invalid apartment bounds: " + id);
            for (var piece : pieces) {
                if (!contains(piece.pos)) throw new IllegalArgumentException("Furniture outside apartment " + id);
                furniture.put(piece.pos.immutable(), piece.block);
            }
        }

        Room(String id, BlockPos lo, BlockPos hi, int price, boolean generated) {
            this(id, lo, hi, price, generated, Optional.empty(), 0L, List.of(),false);
        }

        static boolean validBounds(BlockPos lo, BlockPos hi) {
            int w = hi.getX()-lo.getX()+1, h = hi.getY()-lo.getY()+1, d = hi.getZ()-lo.getZ()+1;
            return w >= 1 && w <= 64 && h >= 1 && h <= 32 && d >= 1 && d <= 64 && w*h*d <= 16384;
        }

        boolean contains(BlockPos pos) {
            return pos.getX() >= lo.getX() && pos.getX() <= hi.getX() && pos.getY() >= lo.getY()
                && pos.getY() <= hi.getY() && pos.getZ() >= lo.getZ() && pos.getZ() <= hi.getZ();
        }
        boolean overlaps(Room other) {
            return lo.getX() <= other.hi.getX() && hi.getX() >= other.lo.getX() && lo.getY() <= other.hi.getY()
                && hi.getY() >= other.lo.getY() && lo.getZ() <= other.hi.getZ() && hi.getZ() >= other.lo.getZ();
        }
        boolean available(long now) { return !packing && until <= now && furniture.isEmpty(); }
        boolean near(BlockPos p) {
            long dx = (long)p.getX() - Math.clamp(p.getX(), lo.getX(), hi.getX());
            long dy = (long)p.getY() - Math.clamp(p.getY(), lo.getY(), hi.getY());
            long dz = (long)p.getZ() - Math.clamp(p.getZ(), lo.getZ(), hi.getZ());
            return dx*dx + dy*dy + dz*dz <= 25;
        }
        boolean active(UUID player, long now) { return player.equals(tenant) && until > now; }
        boolean sameDefinition(Room other) {
            return generated && other.generated && id.equals(other.id) && lo.equals(other.lo) && hi.equals(other.hi) && price == other.price;
        }
    }

    static final Codec<ApartmentBook> CODEC = Room.CODEC.listOf().xmap(ApartmentBook::new, b -> List.copyOf(b.rooms.values()));
    ApartmentBook() {}
    private ApartmentBook(List<Room> saved) {
        for (var room : saved) {
            if (!canAdd(room)) throw new IllegalArgumentException("Overlapping or duplicate apartment " + room.id);
            add(room);
        }
    }
    Collection<Room> rooms() { return Collections.unmodifiableCollection(rooms.values()); }
    Room named(String id) { return rooms.get(id); }
    boolean canAdd(Room room) { return !rooms.containsKey(room.id) && rooms.values().stream().noneMatch(room::overlaps); }
    static List<Long> chunksFor(Room room) {
        var keys = new ArrayList<Long>();
        for (int x=room.lo.getX()>>4; x<=room.hi.getX()>>4; x++)
            for (int z=room.lo.getZ()>>4; z<=room.hi.getZ()>>4; z++) keys.add(LandRegistry.key(x,z));
        return keys;
    }
    void add(Room room) {
        rooms.put(room.id, room);
        for (long key : chunksFor(room)) chunks.computeIfAbsent(key, k -> new ArrayList<>()).add(room);
    }
    void remove(Room room) {
        rooms.remove(room.id);
        for (long key : chunksFor(room)) {
            var list=chunks.get(key); list.remove(room); if (list.isEmpty()) chunks.remove(key);
        }
    }
    boolean touchesChunk(long key) { return chunks.containsKey(key); }
    Room at(BlockPos pos) {
        var list = chunks.get(LandRegistry.keyOf(pos));
        if (list != null) for (var room : list) if (room.contains(pos)) return room;
        return null;
    }

    /** All validation precedes the debit. Renewals explicitly buy a single additional week. */
    String pay(Jugs.Data wallet, Room room, UUID player, boolean renew, long now) {
        if (room.packing) return "This apartment is being packed.";
        if (renew ? !player.equals(room.tenant) : !room.available(now)) return renew ? "You do not rent that apartment." : "That apartment is occupied.";
        if (rooms.values().stream().anyMatch(r -> r != room && player.equals(r.tenant) && !r.available(now)))
            return "Move out of your current apartment first.";
        if (renew && room.until > now + WEEK) return "You already have more than a week remaining.";
        if (!wallet.take(player, room.price)) return "Rent costs " + room.price + " Jugs. Your balance is too low.";
        room.until = (renew ? Math.max(now,room.until) : now) + WEEK;
        room.tenant = player;
        wallet.setDirty();
        return null;
    }
}

