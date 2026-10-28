package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.concordance.spirits.PactSpirits;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * A spirit's portrait window (docs/features/magic-interfaces.md, spec 2.7): filled first with a piece of the cosmos, then
 * the creature drawn inside it, facing the reader. While a spirit visits or keeps the perch it is the real one standing
 * there ({@link #atPerch}: ticked by the world, playing its real gestures); for the chooser and the roster it is
 * {@link MagicPortraits}' visual-only one; with neither, the spirit's sign item sits centred. A pact that has ended draws
 * its portrait greyed.
 */
final class MagicPortrait {
	/** How far from the perch the real spirit is looked for: it stands on the perch, so a block or two. */
	private static final double REACH = 3.0;
	private static final int GREY = 0xA0302A38;

	private MagicPortrait() {
	}

	/**
	 * The live spirit at {@code perch} whose sign is {@code sign} (each pact spirit's sign and entity type,
	 * PactSpirits), the nearest if there are two; null when none is there or loaded.
	 */
	static @Nullable LivingEntity atPerch(BlockPos perch, String sign) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null || sign.isEmpty()) {
			return null;
		}
		for (PactSpirits.Spirit spirit : PactSpirits.all()) {
			if (!spirit.sign().equals(sign)) {
				continue;
			}
			List<LivingEntity> near = level.getEntitiesOfClass(LivingEntity.class, new AABB(perch).inflate(REACH),
					creature -> creature.getType() == spirit.type() && creature.isAlive());
			Vec3 centre = Vec3.atCenterOf(perch);
			LivingEntity nearest = null;
			for (LivingEntity creature : near) {
				if (nearest == null || creature.distanceToSqr(centre) < nearest.distanceToSqr(centre)) {
					nearest = creature;
				}
			}
			return nearest;
		}
		return null;
	}

	/**
	 * A visual-only portrait of the pact spirit whose sign is {@code sign} ({@link MagicPortraits}: the chooser's cards, a
	 * pact without its perch, a spirit that has departed), or null when no pact spirit has that sign.
	 */
	static @Nullable LivingEntity ofSign(String sign) {
		for (PactSpirits.Spirit spirit : PactSpirits.all()) {
			if (spirit.sign().equals(sign)) {
				return MagicPortraits.of(spirit.counterpart());
			}
		}
		return null;
	}

	/** The counterpart (an entity id) of the pact spirit whose sign is {@code sign}, or "" (the beats heard before are kept by it). */
	static String counterpart(String sign) {
		for (PactSpirits.Spirit spirit : PactSpirits.all()) {
			if (spirit.sign().equals(sign)) {
				return spirit.counterpart();
			}
		}
		return "";
	}

	/**
	 * The window at (x, y), w x h: the cosmos, then {@code creature} (or {@code sign}'s item when it is null), greyed when
	 * {@code ended}.
	 */
	static void draw(GuiGraphicsExtractor g, int x, int y, int w, int h, @Nullable LivingEntity creature, String sign, boolean ended) {
		MagicArt.cosmosWindow(g, x, y, w, h);
		if (creature != null) {
			int size = (int) Math.max(4.0F, Math.min((h - 2) / Math.max(0.1F, creature.getBbHeight()),
					(w - 2) / Math.max(0.1F, creature.getBbWidth())));
			// Facing the reader and still: it does not follow the mouse.
			InventoryScreen.extractEntityInInventoryFollowsMouse(g, x, y, x + w, y + h, size, 0.0625F, x + w / 2.0F, y + h / 2.0F, creature);
		} else {
			Identifier id = Identifier.tryParse(sign);
			if (id != null && BuiltInRegistries.ITEM.containsKey(id)) {
				g.item(new ItemStack(BuiltInRegistries.ITEM.getValue(id)), x + (w - 16) / 2, y + (h - 16) / 2);
			}
		}
		if (ended) {
			g.fill(x, y, x + w, y + h, GREY);
		}
	}
}
