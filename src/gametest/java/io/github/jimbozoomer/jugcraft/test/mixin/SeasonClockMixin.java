package io.github.jimbozoomer.jugcraft.test.mixin;

import io.github.jimbozoomer.jugcraft.season.JugcraftSeasons;
import io.github.jimbozoomer.jugcraft.test.TestSeasonClock;
import java.time.LocalDate;
import java.time.ZoneId;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Long generation/rendering fixtures must not change season when CI crosses midnight. */
@Mixin(value = JugcraftSeasons.class, remap = false)
public abstract class SeasonClockMixin {
	@Redirect(method = "date", at = @At(value = "INVOKE", target = "Ljava/time/LocalDate;now(Ljava/time/ZoneId;)Ljava/time/LocalDate;"))
	private static LocalDate jugcraftTest$seasonDate(ZoneId zone) {
		return TestSeasonClock.date(zone);
	}
}
