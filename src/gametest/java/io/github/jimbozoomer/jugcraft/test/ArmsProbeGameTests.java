package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.io.InputStream;
import java.util.Base64;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

/** PROBE (removed before review): logs 26.3 class files for bows, crossbows, arrows, shields and fire, to read their signatures. */
public class ArmsProbeGameTests {
	@GameTest
	public void logRangedClasses(GameTestHelper helper) {
		for (String name : new String[] {"net/minecraft/world/item/BowItem", "net/minecraft/world/item/CrossbowItem",
				"net/minecraft/world/item/CrossbowItem$ChargeType", "net/minecraft/world/item/CrossbowItem$ChargingSounds",
				"net/minecraft/world/item/ProjectileWeaponItem", "net/minecraft/world/item/ArrowItem",
				"net/minecraft/world/entity/projectile/arrow/AbstractArrow", "net/minecraft/world/entity/projectile/arrow/Arrow",
				"net/minecraft/world/entity/projectile/Projectile", "net/minecraft/world/item/component/ChargedProjectiles",
				"net/minecraft/world/item/FlintAndSteelItem", "net/minecraft/world/entity/Entity",
				"net/minecraft/core/particles/DustParticleOptions", "net/minecraft/core/particles/ParticleTypes"}) {
			try (InputStream in = Jugcraft.class.getClassLoader().getResourceAsStream(name + ".class")) {
				Jugcraft.LOGGER.info("[probe] CLASS {} {}", name, in == null ? "missing" : Base64.getEncoder().encodeToString(in.readAllBytes()));
			} catch (Exception exception) {
				Jugcraft.LOGGER.info("[probe] CLASS {} unreadable: {}", name, exception.toString());
			}
		}
		helper.succeed();
	}
}
