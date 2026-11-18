package io.github.jimbozoomer.jugcraft.weapons;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.menagerie.PirateDecks;
import io.github.jimbozoomer.jugcraft.menagerie.PirateShip;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwingAnimationType;
import net.minecraft.world.item.ToolMaterial;
import org.jspecify.annotations.Nullable;

/**
 * The Naval Cutlass (Thanksgiving weapons, tools/thanksgiving_weapons.py; docs/features/thanksgiving-weapons.md): a
 * sailor's short, broad, curved blade of iron with a brass shell guard, for fighting aboard. It is an arm
 * ({@link ArmItem}): its numbers ({@link NavalCutlassStats}) are vanilla item components, made by the arms' own
 * {@link JugcraftArms#arm} from {@link #KIND}, so the game swings it, reaches with it, parries with its guard (the
 * blocks-attacks component, on the use key) and wears it as it does every arm, and it is repaired with iron and
 * enchanted as a sword. Its trait, Boarding, is {@link #getAttackDamageBonus}: a blow deals
 * {@link NavalCutlassStats#BOARDING} more while its wielder or its target is in water, aboard a boat, or aboard a pirate
 * ship, seated on it or standing on its deck ({@link #afloat}). Its tooltip is
 * {@code tooltip.jugcraft.arms.cutlass}. It is swung with the sabre's motion clips ({@link #kind}).
 *
 * <p>It adds no attack path of its own: every blow goes through vanilla's attack, so land claims, the protected town
 * and PvP rules (AttackEntityCallback, the damage events) treat it as they treat any sword.
 */
public class NavalCutlassItem extends ArmItem {
	public static final String ID = "naval_cutlass";
	/** Its kind of arm: one-handed, a quick whacking swing, a short reach, a parrying guard, a sword's enchantments. */
	public static final JugcraftArms.Kind KIND = new JugcraftArms.Kind("cutlass", NavalCutlassStats.DAMAGE,
			NavalCutlassStats.SPEED, SwingAnimationType.WHACK, NavalCutlassStats.SWING_TICKS, 0.0F, NavalCutlassStats.REACH,
			0.0F, 0.0F, 1, 0.0F, NavalCutlassStats.PARRY, true, false);
	/** The kind whose motion clips it is swung with (client/arms/ArmsMotion): the sabre's, the other curved blade. */
	public static final String MOTION = "sabre";
	public static Item CUTLASS;

	public NavalCutlassItem(Properties properties) {
		super(KIND.name(), properties);
	}

	/**
	 * The kind the arms' shared tables read: the motion clips (client/arms/ArmsMotion), two-handed blows and weapon arts
	 * ({@link TwoHanded}, {@link WeaponArts}). The cutlass moves as the sabre does and has neither of the others; its
	 * tooltip and trait are its own.
	 */
	@Override
	public String kind() {
		return MOTION;
	}

	/** Boarding: a blow deals more while its wielder or its target is afloat. */
	@Override
	public float getAttackDamageBonus(Entity target, float damage, DamageSource source) {
		return super.getAttackDamageBonus(target, damage, source)
				+ NavalCutlassStats.boardingBonus(afloat(source.getEntity()), afloat(target));
	}

	/**
	 * Whether {@code entity} is at sea. That is one of:
	 * <ul>
	 * <li>in water;</li>
	 * <li>riding a boat, or something a boat carries;</li>
	 * <li>riding a pirate ship: its seated crew at every station, and any other passenger;</li>
	 * <li>standing on a pirate ship's deck.</li>
	 * </ul>
	 * Boarders do not ride a pirate ship: they stand on its deck squares. So the deck is asked of
	 * {@link PirateDecks#standingOnDeck}, which counts only real supported feet contact with a deck. The shore beside
	 * the ship, a block set on the deck and the air above it do not count.
	 *
	 * <p>It is asked only when a blow lands ({@link #getAttackDamageBonus}). The water and boat checks are unchanged
	 * and come first. The deck query, the only one that searches for entities, runs last, after the others have said
	 * no.
	 */
	public static boolean afloat(@Nullable Entity entity) {
		if (entity == null) {
			return false;
		}
		Entity carrier = entity.getRootVehicle();
		return entity.isInWater() || carrier instanceof AbstractBoat || carrier instanceof PirateShip
				|| (entity instanceof LivingEntity living && PirateDecks.standingOnDeck(living));
	}

	/** Registers the cutlass (Jugcraft's start-up, with the other arms), in the Combat tab. */
	public static void register() {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Jugcraft.id(ID));
		CUTLASS = Registry.register(BuiltInRegistries.ITEM, key,
				new NavalCutlassItem(JugcraftArms.arm(new Item.Properties().setId(key), ToolMaterial.IRON, KIND)));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT).register(output -> output.accept(CUTLASS));
	}
}
