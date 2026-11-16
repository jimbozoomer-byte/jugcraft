package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.concordance.spirits.PactSpiritEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactSpirits;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/**
 * A spirit's portrait window (docs/features/magic-interfaces.md, spec 2.7): filled first with a piece of the cosmos, then
 * the creature drawn inside it, facing the reader. While a spirit visits or keeps the perch it is the real one standing
 * there ({@link #atPerch}: ticked by the world, playing its real gestures); for the chooser and the roster it is
 * {@link MagicPortraits}' visual-only one; with neither, the spirit's sign item sits centred. A pact that has ended draws
 * its portrait greyed. A spirit still arriving is drawn as it will stand ({@link #shown}), so its window is never empty
 * on its first beat.
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

	/** The glow behind a portrait's spirit: warm cream at the window's foot, clear at a third of its height. */
	private static final int GLOW = 0x60F2DCA8;

	/**
	 * What a portrait draws for {@code creature} (the unit 3 fixes, CI's pictures: Buer's and Marax's windows were empty on
	 * their first beat): a pact spirit whose arrival this client still draws (PactSpiritRenderers.arriving; Marax grows from
	 * nothing, Buer rises from a small bud) is drawn as it will stand, its visual-only portrait (MagicPortraits, playing its
	 * idle pose), so the window shows the spirit whole whatever its arrival hides; the world draws the arrival as before,
	 * and the window draws the real one again once it has arrived. Anything else is drawn as it is.
	 */
	static @Nullable LivingEntity shown(@Nullable LivingEntity creature) {
		if (creature instanceof PactSpiritEntity<?> spirit && PactSpiritRenderers.arriving(spirit)) {
			LivingEntity standing = MagicPortraits.of(spirit.counterpart());
			if (standing != null) {
				return standing;
			}
		}
		return creature;
	}

	/**
	 * The window at (x, y), w x h: the cosmos, then {@code live} as {@link #shown} draws it (or {@code sign}'s item when it
	 * is null), greyed when {@code ended}.
	 */
	static void draw(GuiGraphicsExtractor g, int x, int y, int w, int h, @Nullable LivingEntity live, String sign, boolean ended) {
		LivingEntity creature = shown(live);
		MagicArt.cosmosWindow(g, x, y, w, h);
		// A warm glow rising behind the spirit, so a dark one (Stolas's raven) stands off the cosmos (CI's pictures).
		g.fillGradient(x, y + h / 3, x + w, y + h, GLOW & 0x00FFFFFF, GLOW);
		if (creature != null) {
			int size = (int) Math.max(4.0F, Math.min((h - 2) / Math.max(0.1F, creature.getBbHeight()),
					(w - 2) / Math.max(0.1F, creature.getBbWidth())));
			entity(g, x, y, x + w, y + h, size, creature);
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

	/**
	 * {@code creature} in the window (x0, y0)-(x1, y1), facing the reader and still (it does not follow the mouse):
	 * InventoryScreen.extractEntityInInventoryFollowsMouse's own steps with the mouse at the window's centre, but extracted
	 * at this frame's partial tick rather than 1. GeckoLib's AnimationController advances an animation only when the partial
	 * tick is not 1, so at 1 a visual-only portrait (MagicPortraits, aged by tick()) would hold its first frame; under
	 * reduced motion it is extracted at 1, and holds still.
	 */
	private static void entity(GuiGraphicsExtractor g, int x0, int y0, int x1, int y1, int size, LivingEntity creature) {
		Minecraft minecraft = Minecraft.getInstance();
		float partialTick = ConcordanceClientOptions.reducedMotion() ? 1.0F
				: Math.min(0.999F, minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false));
		EntityRenderState state = minecraft.getEntityRenderDispatcher().extractEntity(creature, partialTick);
		state.shadowPieces.clear();
		state.outlineColor = 0;
		if (state instanceof LivingEntityRenderState living) {
			living.bodyRot = 180.0F;
			living.yRot = 0.0F;
			living.xRot = 0.0F;
			living.boundingBoxWidth /= living.scale;
			living.boundingBoxHeight /= living.scale;
			living.scale = 1.0F;
		}
		Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
		Quaternionf xRotation = new Quaternionf();
		g.entity(state, size, new Vector3f(0.0F, state.boundingBoxHeight / 2.0F + 0.0625F, 0.0F), rotation, xRotation, x0, y0, x1, y1);
	}
}
