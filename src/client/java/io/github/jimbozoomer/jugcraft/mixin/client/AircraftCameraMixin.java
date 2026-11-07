package io.github.jimbozoomer.jugcraft.mixin.client;

import io.github.jimbozoomer.jugcraft.aircraft.Aircraft;
import io.github.jimbozoomer.jugcraft.walker.DieselWalker;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Expand both F5 views before vanilla's terrain obstruction check, for aircraft and for walkers (whose hulls would
 * otherwise fill vanilla's four blocks); first person is unchanged.
 */
@Mixin(Camera.class)
public abstract class AircraftCameraMixin {
    @Shadow private Entity entity;
    @ModifyArg(method="alignWithEntity", at=@At(value="INVOKE", target="Lnet/minecraft/client/Camera;getMaxZoom(F)F"), index=0)
    private float jugcraft$aircraftDistance(float distance){
        return entity!=null && entity.getVehicle() instanceof Aircraft aircraft
            ? Math.max(distance,aircraft.spec().cameraDistance())
            : entity!=null && entity.getVehicle() instanceof DieselWalker walker ? Math.max(distance,walker.cameraDistance()) : distance;
    }
}
