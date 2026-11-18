package io.github.jimbozoomer.jugcraft.town;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.finance.LandRegistry;
import io.github.jimbozoomer.jugcraft.server.OperatorRegions;
import io.github.jimbozoomer.jugcraft.server.ServerUtilities;
import java.time.Instant;
import java.util.*;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

final class ApartmentCommands {
    private ApartmentCommands() {}
    private static final int ROOMS_PER_PAGE = 8;
    private record Selection(BlockPos first, BlockPos second) {}
    private static final Map<UUID,Selection> selections = new HashMap<>();

    static void register() {
        ApartmentReclaim.register();
        CommandRegistrationCallback.EVENT.register((dispatcher,r,e)->{
            var cmd=Commands.literal("apartment").executes(c -> list(c.getSource().getPlayerOrException(), 1));
            cmd.then(Commands.literal("list").executes(c -> list(c.getSource().getPlayerOrException(), 1))
                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                    .executes(c -> list(c.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(c, "page")))));
            cmd.then(Commands.literal("reclaim").executes(c->reclaim(c.getSource().getPlayerOrException())));
            cmd.then(Commands.literal("desk").executes(c->desk(c.getSource().getPlayerOrException(),1))
                .then(Commands.argument("page",IntegerArgumentType.integer(1)).executes(c->
                    desk(c.getSource().getPlayerOrException(),IntegerArgumentType.getInteger(c,"page")))));
            for (var action:List.of("info","rent","renew","leave","remove","packout")) {
                var sub=Commands.literal(action);
                if (action.equals("remove") || action.equals("packout")) sub.requires(ServerUtilities::op);
                sub.then(Commands.argument("room",StringArgumentType.word()).suggests((c,b)->{
                    var book=Jugs.data(c.getSource().getServer()).apartments;
                    for (var room:book.rooms()) if(room.id.toLowerCase(Locale.ROOT).startsWith(b.getRemainingLowerCase())) b.suggest(room.id);
                    return b.buildFuture();
                }).executes(c -> action.equals("packout")?pack(c.getSource(),StringArgumentType.getString(c,"room"))
                    :act(c.getSource().getPlayerOrException(),action,StringArgumentType.getString(c,"room"))));
                cmd.then(sub);
            }
            cmd.then(Commands.literal("tool").requires(ServerUtilities::op).executes(c->giveTool(c.getSource().getPlayerOrException())));
            for (var corner:List.of("pos1","pos2"))
                cmd.then(Commands.literal(corner).requires(ServerUtilities::op).executes(c->{
                    var player=c.getSource().getPlayerOrException();
                    return select(player,player.blockPosition(),corner.equals("pos2"));
                }));
            cmd.then(Commands.literal("define").requires(ServerUtilities::op)
                .then(Commands.argument("name",StringArgumentType.word())
                .then(Commands.argument("weeklyJugs",IntegerArgumentType.integer(1,100000))
                .executes(c->define(c.getSource().getPlayerOrException(),StringArgumentType.getString(c,"name"),IntegerArgumentType.getInteger(c,"weeklyJugs"))))));
            dispatcher.register(cmd);
        });
        var phase=Jugcraft.id("apartment_survey");
        AttackBlockCallback.EVENT.addPhaseOrdering(phase,Event.DEFAULT_PHASE);
        UseBlockCallback.EVENT.addPhaseOrdering(phase,Event.DEFAULT_PHASE);
        AttackBlockCallback.EVENT.register(phase,(p,l,h,pos,dir)->{
            if (!tool(p.getItemInHand(h))) return InteractionResult.PASS;
            if (p instanceof ServerPlayer sp && ServerUtilities.op(sp.createCommandSourceStack())) select(sp,pos,false);
            return InteractionResult.SUCCESS;
        });
        UseBlockCallback.EVENT.register(phase,(p,l,h,hit)->{
            if (!tool(p.getItemInHand(h))) return InteractionResult.PASS;
            if (p instanceof ServerPlayer sp && ServerUtilities.op(sp.createCommandSourceStack())) select(sp,hit.getBlockPos(),true);
            return InteractionResult.SUCCESS;
        });
        ServerTickEvents.END_SERVER_TICK.register(s->{
            if (s.overworld().getGameTime()%20!=0) return;
            for (var p:s.getPlayerList().getPlayers()) {
                if (p.containerMenu!=p.inventoryMenu) {
                    for (var slot:p.containerMenu.slots) {
                        if (slot.container instanceof net.minecraft.world.level.block.entity.BlockEntity fixture
                                && ApartmentRentals.privateFixture(p.level(),fixture.getBlockPos())
                                && !TownProtection.exempt(p) && !ApartmentRentals.mayUseFurniture(p,p.level(),fixture.getBlockPos())) {
                            p.closeContainer(); break;
                        }
                    }
                }
                if (p.level().dimension()!=Level.OVERWORLD || !tool(p.getMainHandItem()) && !tool(p.getOffhandItem()) || !ServerUtilities.op(p.createCommandSourceStack())) continue;
                var selected=selections.get(p.getUUID());
                if (selected!=null && selected.first!=null && selected.second!=null)
                    OperatorRegions.outline(p,OperatorRegions.box(selected.first,selected.second),0xF1C550);
                for(var room:ApartmentRentals.data(p.level()).apartments.rooms()) {
                    if (room.near(p.blockPosition())) OperatorRegions.outline(p,OperatorRegions.box(room.lo,room.hi),0x5DD6BC);
                }
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((h,s)->selections.remove(h.player.getUUID()));
    }
    private static boolean tool(ItemStack stack) {
        return stack.is(Items.BLAZE_ROD) && stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getBooleanOr("jugcraft_apartment_wand",false);
    }
    private static int tell(ServerPlayer p, String message, boolean success) {
        p.sendSystemMessage(Component.literal(message));
        return success?1:0;
    }
    private static int giveTool(ServerPlayer p) {
        var stack=new ItemStack(Items.BLAZE_ROD);
        CustomData.update(DataComponents.CUSTOM_DATA,stack,t->t.putBoolean("jugcraft_apartment_wand",true));
        stack.set(DataComponents.CUSTOM_NAME,Component.literal("Apartment Surveyor"));
        if (!p.getInventory().add(stack)) p.drop(stack,false,net.minecraft.util.Prediction.SERVER_ONLY);
        return tell(p,"Apartment Surveyor: left-click and right-click the two interior corners (or stand in air and use /apartment pos1 and pos2). Gold outlines your selection; green outlines nearby units. Then /apartment define <name> <weeklyJugs>.",true);
    }
    private static int select(ServerPlayer p,BlockPos pos,boolean second) {
        if (p.level().dimension()!=Level.OVERWORLD) return tell(p,"Apartment survey areas are in the Overworld.",false);
        var old=selections.getOrDefault(p.getUUID(),new Selection(null,null));
        selections.put(p.getUUID(),second?new Selection(old.first,pos.immutable()):new Selection(pos.immutable(),old.second));
        return tell(p,"Interior corner "+(second?2:1)+" at "+pos.toShortString()+".",true);
    }
    private static int define(ServerPlayer p,String id,int price) {
        var selection=selections.get(p.getUUID());
        if (p.level().dimension()!=Level.OVERWORLD || selection==null || selection.first==null || selection.second==null)
            return tell(p,"Select both interior corners in the Overworld first.",false);
        if (!id.matches("[a-zA-Z0-9_-]{1,48}") || id.toLowerCase(Locale.ROOT).startsWith("west_"))
            return tell(p,"Use a short unique apartment name. west_ is reserved for the western district.",false);
        var a=selection.first; var b=selection.second;
        var lo=new BlockPos(Math.min(a.getX(),b.getX()),Math.min(a.getY(),b.getY()),Math.min(a.getZ(),b.getZ()));
        var hi=new BlockPos(Math.max(a.getX(),b.getX()),Math.max(a.getY(),b.getY()),Math.max(a.getZ(),b.getZ()));
        if (!ApartmentBook.Room.validBounds(lo,hi) || !p.level().isInWorldBounds(lo) || !p.level().isInWorldBounds(hi)
                || !p.level().getWorldBorder().isWithinBounds(lo) || !p.level().getWorldBorder().isWithinBounds(hi))
            return tell(p,"Choose an interior up to 64 × 32 × 64 and 16,384 blocks in volume, inside the world border.",false);
        var data=ApartmentRentals.data(p.level()); var book=data.apartments;
        var room=new ApartmentBook.Room(id,lo,hi,price,false);
        if (book.rooms().stream().filter(r -> !r.generated).count()>=ApartmentBook.MAX_CUSTOM || !book.canAdd(room))
            return tell(p,"Overlapping interior, existing name, or the custom apartment limit was reached.",false);
        for (long key:ApartmentBook.chunksFor(room))
            if (LandRegistry.get(p.level().getServer()).claim(key)!=null)
                return tell(p,"Part of this selection belongs to a land claim.",false);
        book.add(room); data.setDirty(); selections.remove(p.getUUID());
        return tell(p,"Opened "+id+" for "+price+" Jugs per seven real days. Players can inspect and rent at its entrance.",true);
    }

    private static String describe(ApartmentBook.Room room,UUID viewer,long now) {
        String status=room.available(now)?"available":room.until>now?"rented":"awaiting move-out";
        if (viewer.equals(room.tenant) && !room.available(now))
            status=room.until>now?"yours until "+Instant.ofEpochMilli(room.until):"your lease expired; collect your furniture or renew";
        return room.id+" · "+room.price+" Jugs/week · "+status;
    }
    private static int list(ServerPlayer p, int requestedPage) {
        var rooms = ApartmentRentals.data(p.level()).apartments.rooms();
        listingPage(rooms, p.blockPosition(), p.getUUID(), System.currentTimeMillis(), requestedPage).forEach(p::sendSystemMessage);
        return 1;
    }

    static List<Component> listingPage(Collection<ApartmentBook.Room> rooms, BlockPos position, UUID viewer, long now, int requestedPage) {
        return listingPage(rooms,position,viewer,now,requestedPage,null);
    }

    static int desk(ServerPlayer player,int page) {
        var manager=DistrictResidents.managerNear(player);
        if(manager==null) return tell(player,"Speak to an apartment manager in a court lobby.",false);
        var rooms=ApartmentRentals.data(player.level()).apartments.rooms().stream()
            .filter(r->servedBy(manager,r)).toList();
        long now=System.currentTimeMillis();
        long available=rooms.stream().filter(r->r.available(now)).count();
        player.sendSystemMessage(Component.literal(manager.name()+" · "+available+" of "+rooms.size()+
            " rooms available. Choose a room to see its price and rent it here."));
        listingPage(rooms,player.blockPosition(),player.getUUID(),now,page,manager).forEach(player::sendSystemMessage);
        return 1;
    }

    private static boolean servedBy(DistrictResidents.Post manager,ApartmentBook.Room room) {
        return manager!=null && room.generated && room.id.startsWith("west_"+manager.variant()+"_");
    }

    private static List<Component> listingPage(Collection<ApartmentBook.Room> rooms, BlockPos position, UUID viewer,
            long now, int requestedPage,DistrictResidents.Post desk) {
        if (rooms.isEmpty()) return List.of(Component.literal("There are currently no rentable apartments."));
        var sorted = rooms.stream().sorted(Comparator.comparingDouble((ApartmentBook.Room room) -> room.lo.distSqr(position))
            .thenComparing(room -> room.id)).toList();
        int pages = Math.ceilDiv(sorted.size(), ROOMS_PER_PAGE);
        int page = Math.clamp(requestedPage, 1, pages);
        int first = (page - 1) * ROOMS_PER_PAGE;
        var visible = sorted.subList(first, Math.min(first + ROOMS_PER_PAGE, sorted.size()));
        var lines = new ArrayList<Component>();
        lines.add(Component.literal("Apartments · Page " + page + " of " + pages + " · Nearest first"));
        lines.add(Component.literal("Select a listing for details, or sneak-click a room door with an empty hand. Use /apartment reclaim to locate items from an old lease."));
        for (var room : visible) lines.add(listing(room, viewer, now));
        for (var room : rooms) {
            if (viewer.equals(room.tenant) && !room.available(now) && !visible.contains(room))
                lines.add(Component.literal("Your apartment · ").append(listing(room, viewer, now)));
        }
        if (pages > 1) {
            var navigation = Component.empty();
            if (page > 1) navigation.append(pageLink("Previous", page - 1,desk));
            if (page > 1 && page < pages) navigation.append("   ");
            if (page < pages) navigation.append(pageLink("Next", page + 1,desk));
            lines.add(navigation);
        }
        return lines;
    }

    private static Component pageLink(String label, int page,DistrictResidents.Post desk) {
        String command = (desk==null?"/apartment list ":"/apartment desk ") + page;
        return Component.literal("[" + label + " · " + command + "]")
            .withStyle(s -> s.withColor(0x78D5C1).withUnderlined(true).withClickEvent(new ClickEvent.RunCommand(command)));
    }
    private static int reclaim(ServerPlayer p) {
        if (!io.github.jimbozoomer.jugcraft.concordance.RateGate.allow(p,"apartment_reclaim",40))
            return tell(p,"Wait two seconds between reclaim requests.",false);
        try { return tell(p,ApartmentReclaim.collect(p),true); }
        catch(RuntimeException failure) {
            org.slf4j.LoggerFactory.getLogger("jugcraft-apartments").error("Apartment reclaim paused",failure);
            return tell(p,"Reclaim paused. Your delivery receipt is retained.",false);
        }
    }

    private static int pack(net.minecraft.commands.CommandSourceStack source,String id) {
        var level=source.getServer().overworld(); var room=ApartmentRentals.data(level).apartments.named(id);
        if (room==null) { source.sendFailure(Component.literal("Unknown apartment.")); return 0; }
        try {
            var error=ApartmentReclaim.pack(level,room);
            if (error!=null) { source.sendFailure(Component.literal(error)); return 0; }
            source.sendSuccess(()->Component.literal("Room reopened. The previous tenant can use /apartment reclaim for their belongings."),false);
            return 1;
        } catch(RuntimeException failure) {
            org.slf4j.LoggerFactory.getLogger("jugcraft-apartments").error("Apartment move-out paused",failure);
            source.sendFailure(Component.literal("Move-out paused: "+failure.getMessage()+". Retry after resolving the obstruction."));
            return 0;
        }
    }

    private static Component listing(ApartmentBook.Room room,UUID viewer,long now) {
        return Component.literal(describe(room,viewer,now)).withStyle(s->s.withColor(0x78D5C1)
            .withClickEvent(new ClickEvent.RunCommand("/apartment info "+room.id)));
    }

    static InteractionResult readDoor(ServerPlayer player,BlockPos pos) {
        for (var side:net.minecraft.core.Direction.Plane.HORIZONTAL) {
            var room=ApartmentRentals.at(player.level(),pos.relative(side));
            if (room!=null) { act(player,"info",room.id); return InteractionResult.SUCCESS; }
        }
        return InteractionResult.PASS;
    }

    static int act(ServerPlayer p,String action,String id) {
        var data=ApartmentRentals.data(p.level()); var room=data.apartments.named(id);
        if (room==null) return tell(p,"Unknown apartment. Use /apartment list.",false);
        long now=System.currentTimeMillis();
        if (!room.packing && !action.equals("packout") && p.level().dimension()==Level.OVERWORLD && room.near(p.blockPosition())) {
            // A support update or an operator edit can remove a piece outside the break callback.
            // Never discard a receipt just because its chunk is unloaded.
            if(room.furniture.entrySet().removeIf(e->p.level().hasChunkAt(e.getKey())
                    && !ApartmentRentals.key(p.level().getBlockState(e.getKey())).equals(e.getValue()))) data.setDirty();
        }
        if (action.equals("info")) {
            String operation=room.available(now)?"rent":p.getUUID().equals(room.tenant)?"renew":null;
            var message=Component.literal(describe(room,p.getUUID(),now)+" · Overworld "+room.lo.toShortString()
                +". Furnish free interior space with chairs, tables, cabinets, carpet, bookshelves or lamps. Your own barrel is private. Only items you place may be removed.");
            if (operation!=null) message.append(Component.literal(" ["+(operation.equals("rent")?"Rent":"Renew")+" for "+room.price+" Jugs]")
                .withStyle(s->s.withColor(0xEAC16A).withClickEvent(new ClickEvent.SuggestCommand("/apartment "+operation+" "+room.id))));
            message.append(" After a staff move-out, return here and use /apartment reclaim for your furniture and stored items.");
            p.sendSystemMessage(message);
            return 1;
        }
        if (action.equals("remove")) {
            if (!ServerUtilities.op(p.createCommandSourceStack()) || room.generated || !room.available(now))
                return tell(p,"Only a vacant, cleared, custom apartment may be removed.",false);
            data.apartments.remove(room); data.setDirty();
            return tell(p,"Removed rental designation "+id+".",true);
        }
        if (room.packing) return tell(p,"Staff are packing this apartment. Use /apartment reclaim near it once finished.",false);
        if (p.isSpectator() || !p.isAlive()) return tell(p,"Leave spectator mode and respawn to manage a lease.",false);
        if (action.equals("leave")) {
            if (!p.getUUID().equals(room.tenant)) return tell(p,"You do not rent this room.",false);
            if (!room.furniture.isEmpty()) return tell(p,"Pack your "+room.furniture.size()+" remaining furnishings first. You can retrieve them after expiry.",false);
            room.tenant=null; room.until=0L; data.setDirty();
            return tell(p,"Moved out of "+room.id+". Unused time is surrendered.",true);
        }
        if (action.equals("rent") && (p.level().dimension()!=Level.OVERWORLD ||
                !room.near(p.blockPosition()) && !servedBy(DistrictResidents.managerNear(p),room)))
            return tell(p,"Visit this apartment or its court manager to rent it. Overworld "+room.lo.toShortString()+".",false);
        if (room.generated && !WesternDistrict.progress(p.level().getServer().overworld()).phase().equals("installed"))
            return tell(p,"Apartment rentals are paused while the district is being installed or restored.",false);
        var problem=data.apartments.pay(data,room,p.getUUID(),action.equals("renew"),now);
        if (problem!=null) return tell(p,problem,false);
        return tell(p,"Paid "+room.price+" Jugs. "+describe(room,p.getUUID(),now)+". Use /apartment renew "+room.id+" to extend.",true);
    }
}

