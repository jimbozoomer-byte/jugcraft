package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.Set;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;

/** PROBE (never to be merged): logs 26.3 class files for weapon components, to read their signatures. */
public class WeaponProbeGameTests {
	@GameTest
	public void logWeaponClasses(GameTestHelper helper) {
		Set<String> names = new LinkedHashSet<>();
		for (String name : new String[] {"net/minecraft/world/item/Item", "net/minecraft/world/item/Item$Properties",
				"net/minecraft/core/component/DataComponents", "net/minecraft/world/item/ItemUseAnimation", "net/minecraft/world/item/ToolMaterial",
				"net/minecraft/world/item/MaceItem", "net/minecraft/world/item/component/ItemAttributeModifiers"}) {
			names.add(name);
		}
		for (Field field : DataComponents.class.getFields()) {
			String upper = field.getName();
			if (!(upper.contains("ATTACK") || upper.contains("KINETIC") || upper.contains("PIERCING") || upper.contains("WEAPON")
					|| upper.contains("USE_EFFECTS") || upper.contains("ANIMATION") || upper.contains("DAMAGE_TYPE") || upper.contains("SWING"))) {
				continue;
			}
			Type type = field.getGenericType();
			Jugcraft.LOGGER.info("[probe] FIELD {} {}", upper, type.getTypeName());
			if (type instanceof ParameterizedType parameterized && parameterized.getActualTypeArguments()[0] instanceof Class<?> value) {
				add(names, value);
			}
		}
		for (String name : names) {
			try (InputStream in = Jugcraft.class.getClassLoader().getResourceAsStream(name + ".class")) {
				Jugcraft.LOGGER.info("[probe] CLASS {} {}", name, in == null ? "missing" : java.util.Base64.getEncoder().encodeToString(in.readAllBytes()));
			} catch (Exception exception) {
				Jugcraft.LOGGER.info("[probe] CLASS {} unreadable: {}", name, exception.toString());
			}
		}
		helper.succeed();
	}

	private static void add(Set<String> names, Class<?> type) {
		if (type.getName().startsWith("net.minecraft") && names.add(type.getName().replace('.', '/'))) {
			for (Class<?> inner : type.getDeclaredClasses()) {
				add(names, inner);
			}
		}
	}
}
