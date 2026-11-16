package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.horses.BreedHorse;
import io.github.jimbozoomer.jugcraft.horses.HorseBreed;
import io.github.jimbozoomer.jugcraft.horses.HorseBreeds;
import io.github.jimbozoomer.jugcraft.horses.HorseLineage;
import io.github.jimbozoomer.jugcraft.pegasus.JugcraftPegasus;
import io.github.jimbozoomer.jugcraft.pegasus.Pegasus;
import io.github.jimbozoomer.jugcraft.pets.Pets;
import io.github.jimbozoomer.jugcraft.pets.PetBehaviors;
import io.github.jimbozoomer.jugcraft.client.pegasus.PegasusClient;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserWhiteListEntry;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

/** Two real client JVMs: a player rides each breed while a late-joining observer checks replicated seats and tack.
 * Uses disposable loopback worlds/profiles, never the owner's launcher or server. Written for combined validation. */
public final class HorseBreedsNetworkTests implements FabricClientGameTest {
    private static final String PEER = "jugcraft.horseTestPeer";
    private static final String NAME = "HorsePeer";
    private static final SecondClient CLIENT = new SecondClient(PEER, "jugcraft.horseTestCoord", "horses", NAME,
            HorseBreedsNetworkTests.class);
    private static final int WAIT = 2400;

    @Override
    public void runTest(ClientGameTestContext context) {
        if (CLIENT.isPeer()) { peer(context); return; }
        Process peer = null;
        try {
            CLIENT.begin();
            Properties properties = new Properties();
            properties.setProperty("server-ip", "127.0.0.1");
            properties.setProperty("online-mode", "false");
            properties.setProperty("view-distance", "4");
            properties.setProperty("simulation-distance", "4");
            properties.setProperty("max-players", "2");
            try (var server = context.worldBuilder().adjustSettings(settings -> {
                settings.setWorldType(new WorldCreationUiState.WorldTypeEntry(settings.getSettings().worldgenLoadContext()
                        .lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT)));
                settings.setGenerateStructures(false);
            }).createServer(properties)) {
                try (var connection = server.connect()) {
                    connection.waitForChunksRender();
                    server.runCommand("gamerule minecraft:spawn_mobs false");
                    server.runCommand("time set noon");
                    server.runCommand("weather clear");
                    server.runCommand("gamemode creative @a");
                    UUID rider = context.computeOnClient(client -> client.player.getUUID());
                    BlockPos origin = context.computeOnClient(client -> client.player.blockPosition());
                    server.runCommand("fill %d %d %d %d %d %d minecraft:stone_bricks".formatted(
                            origin.getX()-8, origin.getY()-1, origin.getZ()-8,
                            origin.getX()+8, origin.getY()-1, origin.getZ()+8));
                    server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(
                            origin.getX()-8, origin.getY(), origin.getZ()-8,
                            origin.getX()+8, origin.getY()+8, origin.getZ()+8));
                    // Spawn before observer login: its first entity snapshot must contain the crossbreed value.
                    UUID[] horses = server.computeOnServer(minecraft -> {
                        var owner = minecraft.getPlayerList().getPlayer(rider);
                        UUID[] ids = new UUID[HorseBreed.GROUND.size()];
                        for (int i = 0; i < ids.length; i++) {
                            HorseBreed breed = HorseBreed.GROUND.get(i);
                            BreedHorse horse = HorseBreeds.type(breed).create(owner.level(), EntitySpawnReason.COMMAND);
                            horse.snapTo(origin.getX()+0.5, origin.getY(), origin.getZ()+0.5, 0, 0);
                            horse.finalizeSpawn(owner.level(), owner.level().getCurrentDifficultyAt(origin), EntitySpawnReason.COMMAND, null);
                            if (breed == HorseBreed.SHIRE) {
                                int[] ancestry = new int[HorseBreed.GROUND.size()];
                                ancestry[HorseBreed.GROUND.indexOf(HorseBreed.SHIRE)] = HorseLineage.TOTAL / 2;
                                ancestry[HorseBreed.GROUND.indexOf(HorseBreed.FRIESIAN)] = HorseLineage.TOTAL / 2;
                                horse.setLineage(ancestry, "shire_horse,friesian_horse", 1);
                                horse.applyTraits();
                            }
                            horse.tameWithName(owner);
                            horse.setNoAi(true);
                            horse.setPersistenceRequired();
                            horse.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
                            if (breed.carriesBags) horse.bags().setFitted(true);
                            // Park unused horses out of the camera and collision footprint.
                            horse.snapTo(origin.getX()+30+i*4, origin.getY(), origin.getZ()+0.5, 0, 0);
                            owner.level().addFreshEntity(horse);
                            // Pet catch-up runs outside vanilla AI: parked horses need the actual STAY order.
                            Pets.enroll(horse).putString("mode", PetBehaviors.STAY);
                            ids[i] = horse.getUUID();
                        }
                        return ids;
                    });
                    server.runOnServer(minecraft -> minecraft.getPlayerList().getWhiteList().add(
                            new UserWhiteListEntry(NameAndId.createOffline(NAME))));
                    InProcessServerConfigs.beforeAdditionalClient(server);
                    peer = CLIENT.launch(server.computeOnServer(minecraft -> minecraft.getPort()), PeerClientStartup.HEAP_OPTION);
                    Process joining = peer;
                    PeerClientStartup.await(() -> context.waitTicks(1), joining, CLIENT.coordination());
                    server.waitFor(minecraft -> minecraft.getPlayerList().getPlayerByName(NAME) != null, WAIT);
                    server.runCommand("gamemode creative " + NAME);
                    server.runOnServer(minecraft -> {
                        var observer = minecraft.getPlayerList().getPlayerByName(NAME);
                        observer.getAbilities().flying = true;
                        observer.onUpdateAbilities();
                    });
                    server.runCommand(String.format(Locale.ROOT, "tp %s %.1f %d %.1f 90 8", NAME,
                            origin.getX()+6.5, origin.getY()+1, origin.getZ()+0.5));
                    for (int i = 0; i < horses.length; i++) {
                        UUID id = horses[i];
                        int step = i;
                        server.runOnServer(minecraft -> {
                            var owner = minecraft.getPlayerList().getPlayer(rider);
                            if (owner.getVehicle() instanceof BreedHorse old) {
                                owner.stopRiding();
                                old.snapTo(origin.getX()+30+step*4, origin.getY(), origin.getZ()+0.5, 0, 0);
                                PetBehaviors.detach(old);
                            }
                            BreedHorse horse = (BreedHorse) owner.level().getEntity(id);
                            horse.snapTo(origin.getX()+0.5, origin.getY(), origin.getZ()+0.5, 0, 0);
                            owner.startRiding(horse, true, true);
                        });
                        context.waitFor(client -> client.player.getVehicle() instanceof BreedHorse h && h.getUUID().equals(id), 200);
                        context.waitTicks(20);
                        String expected = server.computeOnServer(minecraft -> {
                            var owner = minecraft.getPlayerList().getPlayer(rider);
                            BreedHorse horse = (BreedHorse) owner.getVehicle();
                            return id + "\n" + rider + "\n" + (owner.getY()-horse.getY()) + "\n"
                                    + horse.roadBonus() + "\n" + horse.bagsFitted();
                        });
                        CLIENT.mark("horse-"+i, expected);
                        CLIENT.awaitPeer(context, peer, "seen-"+i, WAIT);
                    }
                    UUID pegasus = server.computeOnServer(minecraft -> {
                        var owner = minecraft.getPlayerList().getPlayer(rider);
                        var previous = owner.getVehicle();
                        owner.stopRiding();
                        if (previous != null) {
                            previous.snapTo(origin.getX()+54.5, origin.getY(), origin.getZ()+0.5, 0, 0);
                            if (previous instanceof BreedHorse horse) PetBehaviors.detach(horse);
                        }
                        Pegasus mount = JugcraftPegasus.PEGASUS.create(owner.level(), EntitySpawnReason.COMMAND);
                        mount.snapTo(origin.getX()+0.5, origin.getY(), origin.getZ()+0.5, 0, 0);
                        mount.tameWithName(owner);
                        mount.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
                        mount.pack().setFitted(true);
                        owner.level().addFreshEntity(mount);
                        owner.startRiding(mount, true, false);
                        return mount.getUUID();
                    });
                    context.waitFor(c -> c.player.getVehicle() instanceof Pegasus p && p.getUUID().equals(pegasus), 200);
                    context.waitTicks(20);
                    CLIENT.mark("pegasus-ground", pegasus + "\n" + rider);
                    CLIENT.awaitPeer(context, peer, "pegasus-ground-seen", WAIT);
                    context.getInput().holdKey(options -> PegasusClient.ascendKey());
                    try {
                        context.waitTicks(50);
                    } finally {
                        context.getInput().releaseKey(options -> PegasusClient.ascendKey());
                    }
                    context.waitTicks(15);
                    double altitude = server.computeOnServer(minecraft -> {
                        Pegasus mount = (Pegasus) minecraft.overworld().getEntity(pegasus);
                        if (!mount.flightActive() || mount.getY() < origin.getY()+3) throw new AssertionError("Observer flight did not take off");
                        return mount.getY();
                    });
                    server.runCommand(String.format(Locale.ROOT, "tp %s %.1f %.2f %.1f 90 10", NAME,
                            origin.getX()+7.5, altitude+1, origin.getZ()+0.5));
                    CLIENT.mark("pegasus-flight", pegasus + "\n" + rider);
                    CLIENT.awaitPeer(context, peer, "pegasus-flight-seen", WAIT);
                    CLIENT.mark("finished", "");
                    CLIENT.awaitPeer(context, peer, "peer-finished", WAIT);
                    Process ending = peer;
                    context.waitFor(client -> !ending.isAlive(), WAIT);
                    if (peer.exitValue() != 0) throw CLIENT.failure("Horse observer exited " + peer.exitValue());
                    CLIENT.mark("passed", "Two client JVMs: five breeds, player seats, saddle/bag replication, late-tracked half-Friesian road value, and Pegasus rider/pack/grounded/airborne wing replication.");
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            if (peer != null && peer.isAlive()) peer.destroy();
        }
    }

    private static void peer(ClientGameTestContext context) {
        CLIENT.mark("peer-entered", "");
        String address = "127.0.0.1:" + CLIENT.port();
        context.runOnClient(client -> ConnectScreen.startConnecting(new TitleScreen(), client, ServerAddress.parseString(address),
                new ServerData("Horse disposable test", address, ServerData.Type.OTHER), false, null));
        context.waitFor(client -> {
            if (client.gui.screen() instanceof DisconnectedScreen) throw new AssertionError("Horse observer disconnected; see peer.log");
            return client.level != null && client.player != null;
        }, WAIT);
        context.runOnClient(c -> { c.gui.hud.getChat().clearMessages(true); if (!c.gui.hud.isHidden()) c.gui.hud.toggle(); });
        for (int i = 0; i < HorseBreed.GROUND.size(); i++) {
            CLIENT.awaitMark(context, "horse-"+i, WAIT);
            String[] expected = CLIENT.read("horse-"+i).split("\n");
            UUID id = UUID.fromString(expected[0]), rider = UUID.fromString(expected[1]);
            double seat = Double.parseDouble(expected[2]), road = Double.parseDouble(expected[3]);
            boolean bags = Boolean.parseBoolean(expected[4]);
            context.waitFor(client -> client.level.getEntitiesOfClass(BreedHorse.class, client.player.getBoundingBox().inflate(12))
                    .stream().anyMatch(h -> h.getUUID().equals(id) && h.bagsFitted() == bags
                            && !h.getItemBySlot(EquipmentSlot.SADDLE).isEmpty() && Math.abs(h.roadBonus()-road)<1e-6
                            && h.getPassengers().stream().anyMatch(p -> p.getUUID().equals(rider)
                                    && Math.abs((p.getY()-h.getY())-seat)<0.15)), 400);
            context.waitFor(client -> client.gui.screen() == null && client.levelRenderer.hasRenderedAllSections(), WAIT);
            context.waitFor(client -> client.level.getEntitiesOfClass(BreedHorse.class,
                    client.player.getBoundingBox().inflate(12)).stream().filter(h -> h.distanceToSqr(client.player)<144).count()==1, 200);
            context.waitTicks(10);
            context.takeScreenshot("jugcraft_horse_peer_" + HorseBreed.GROUND.get(i).id());
            CLIENT.mark("seen-"+i, "seat="+seat+" road="+road+" bags="+bags);
        }
        observePegasus(context, "pegasus-ground", false);
        observePegasus(context, "pegasus-flight", true);
        CLIENT.awaitMark(context, "finished", WAIT);
        CLIENT.mark("peer-finished", "");
        context.runOnClient(client -> { client.disconnectWithSavingScreen(); client.gui.setScreen(new TitleScreen()); });
        context.waitForScreen(TitleScreen.class);
    }

    private static void observePegasus(ClientGameTestContext context, String stage, boolean airborne) {
        CLIENT.awaitMark(context, stage, WAIT);
        String[] expected = CLIENT.read(stage).split("\n");
        UUID id = UUID.fromString(expected[0]), rider = UUID.fromString(expected[1]);
        context.waitFor(c -> c.level.getEntitiesOfClass(Pegasus.class, c.player.getBoundingBox().inflate(24)).stream()
                .anyMatch(p -> p.getUUID().equals(id) && p.packFitted() && p.getItemBySlot(EquipmentSlot.SADDLE).is(Items.SADDLE)
                        && p.flightActive() == airborne && (!airborne || (p.wingOpen(1.0F) > 0.9F && p.wingAmp(1.0F) > 0.1F))
                        && p.getPassengers().stream().anyMatch(r -> r.getUUID().equals(rider))), 400);
        context.waitFor(c -> c.gui.screen() == null && c.levelRenderer.hasRenderedAllSections(), WAIT);
        context.waitTicks(10);
        context.takeScreenshot("jugcraft_horse_peer_" + stage);
        CLIENT.mark(stage + "-seen", "rider, saddle, pack, airborne=" + airborne);
    }

    public static final class PeerStartup implements ClientModInitializer {
        @Override public void onInitializeClient() { CLIENT.mark("peer-initialized", ""); }
    }
}
