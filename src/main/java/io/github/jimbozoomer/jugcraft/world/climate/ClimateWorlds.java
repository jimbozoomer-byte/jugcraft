package io.github.jimbozoomer.jugcraft.world.climate;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

/** Read-only operator diagnostics for explicitly migrated procedural worlds. */
public final class ClimateWorlds {
    private ClimateWorlds() { }

    public static void register() {
        ServerLevelEvents.LOAD.register((server, level) -> {
            if (level.dimension() == Level.OVERWORLD && level.getChunkSource().getGenerator().getBiomeSource() instanceof ClimateBiomeSource source)
                Jugcraft.LOGGER.info("[climate] {}", describe(source.climate()));
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> dispatcher.register(Commands.literal("jugcraft")
                .then(Commands.literal("climate").requires(s -> Commands.LEVEL_GAMEMASTERS.check(s.permissions()))
                        .then(Commands.literal("info").executes(context -> {
                            var source = context.getSource().getServer().overworld().getChunkSource().getGenerator().getBiomeSource();
                            String message = source instanceof ClimateBiomeSource climate ? describe(climate.climate())
                                    : "No procedural climate migration is active in this Overworld.";
                            context.getSource().sendSuccess(() -> Component.literal(message), false);
                            return source instanceof ClimateBiomeSource ? 1 : 0;
                        })))));
    }

    private static String describe(ClimateProfile profile) {
        var geometry = profile.geometry();
        long z = profile.anchorZ();
        return "Climate bands active for future Overworld generation. Fixed origin X " + profile.anchorX() + ", Z " + z
                + "; north frigid Z <= " + (z - geometry.northFrigid())
                + "; hot starts Z " + (z + geometry.southHotStart())
                + "; temperate starts Z " + (z + geometry.southHotEnd())
                + "; south frigid Z >= " + (z + geometry.southFrigid())
                + "; transition " + geometry.transition() + " blocks. Existing saved chunks are retained.";
    }
}
