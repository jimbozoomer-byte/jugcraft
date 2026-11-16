package io.github.jimbozoomer.jugcraft.test.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.jimbozoomer.jugcraft.season.JugcraftSeasons;
import io.github.jimbozoomer.jugcraft.test.TestSeasonClock;
import java.time.LocalDate;
import java.time.ZoneId;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Keeps a scoped client tour's real date stable across midnight, preserving the configured zone and calendar. */
@Mixin(value = JugcraftSeasons.class, remap = false)
public abstract class SeasonDateSnapshotMixin {
	@WrapOperation(method = "date", at = @At(value = "INVOKE",
			target = "Ljava/time/LocalDate;now(Ljava/time/ZoneId;)Ljava/time/LocalDate;"))
	private static LocalDate jugcraftTest$seasonDate(ZoneId zone, Operation<LocalDate> original) {
		var snapshot = TestSeasonClock.frozenInstant();
		return snapshot == null ? original.call(zone) : LocalDate.ofInstant(snapshot, zone);
	}
}
