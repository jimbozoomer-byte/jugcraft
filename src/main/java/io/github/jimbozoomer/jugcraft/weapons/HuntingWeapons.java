package io.github.jimbozoomer.jugcraft.weapons;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.gear.TraitTooltips;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwingAnimationType;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * The woodland hunting weapons (Thanksgiving weapons; tools/thanksgiving_weapons.py; docs/features/thanksgiving-weapons.md).
 * They are everyday hunting tools, inspired by the woodland peoples' bows, hatchets and spears, and they belong to the
 * stone and wood tiers. Their numbers are in {@link HuntingWeaponStats}.
 * <ul>
 * <li><b>The Stone-headed Hatchet:</b> an arm built by {@link JugcraftArms#arm} in stone. It chops wood as a stone axe
 * does, and is thrown end over end as the arms' francisca is (a {@link ThrownArmItem}, flying as a {@link ThrownArm}).</li>
 * <li><b>The Hunting Spear:</b> an arm in stone. It jabs with a long reach, and is thrown far and straight as the arms'
 * javelin is.</li>
 * <li><b>The Woodland Hunting Bow:</b> an {@link ArmBowItem}, vanilla's bow drawn over its own draw. It is quicker to draw
 * than vanilla's bow, and its arrows fly a little slower.</li>
 * </ul>
 * None adds a combat path of its own. Blows go through vanilla's attack, and throws and arrows through the arms' and
 * vanilla's projectiles, so land claims, the protected town and the PvP rules treat them as every arm and bow.
 *
 * <p>Each shows its own trait and tooltip ({@code tooltip.jugcraft.arms.<trait>}). The thrown pair move, fly and are
 * thrown as the kind they answer from {@link ArmItem#kind}: the francisca and the javelin. That kind picks the motion
 * clips (client/arms/ArmsMotion), the flight (ThrownArm, client/arms/ThrownArmRenderer) and the throw's tooltip line,
 * as the Naval Cutlass moves as the sabre does.
 */
public final class HuntingWeapons {
	public static final String HATCHET_ID = "stone_headed_hatchet";
	public static final String SPEAR_ID = "hunting_spear";
	public static final String BOW_ID = "woodland_hunting_bow";

	/**
	 * The hatchet's numbers, for {@link JugcraftArms#arm}. It is named for the francisca, which chops: arm() makes a
	 * chopping kind an axe, and only the francisca's trait is looked up by this name. Its tooltip is the hatchet's own.
	 */
	public static final JugcraftArms.Kind HATCHET_KIND = new JugcraftArms.Kind("francisca", HuntingWeaponStats.HATCHET_DAMAGE,
			HuntingWeaponStats.HATCHET_SPEED, SwingAnimationType.WHACK, HuntingWeaponStats.HATCHET_SWING_TICKS, 0.0F,
			HuntingWeaponStats.HATCHET_REACH, 0.0F, HuntingWeaponStats.HATCHET_DISABLE, 1, 0.0F, 0.0F, false, false);
	public static final JugcraftArms.Thrown HATCHET_THROWN = new JugcraftArms.Thrown("francisca", "stone",
			HuntingWeaponStats.HATCHET_WIND, HuntingWeaponStats.HATCHET_THROW_SPEED, HuntingWeaponStats.HATCHET_GRAVITY,
			HuntingWeaponStats.HATCHET_THROW_DAMAGE);
	/** The spear's numbers: a plain thrusting arm (no trait), reaching farther than the hand. */
	public static final JugcraftArms.Kind SPEAR_KIND = new JugcraftArms.Kind(SPEAR_ID, HuntingWeaponStats.SPEAR_DAMAGE,
			HuntingWeaponStats.SPEAR_SPEED, SwingAnimationType.STAB, HuntingWeaponStats.SPEAR_SWING_TICKS, 0.0F,
			HuntingWeaponStats.SPEAR_REACH, HuntingWeaponStats.SPEAR_MARGIN, 0.0F, 1, 0.0F, 0.0F, false, false);
	public static final JugcraftArms.Thrown SPEAR_THROWN = new JugcraftArms.Thrown("javelin", "stone",
			HuntingWeaponStats.SPEAR_WIND, HuntingWeaponStats.SPEAR_THROW_SPEED, HuntingWeaponStats.SPEAR_GRAVITY,
			HuntingWeaponStats.SPEAR_THROW_DAMAGE);
	public static final JugcraftArms.Ranged BOW_RANGED = new JugcraftArms.Ranged("hunting_bow", "wood",
			HuntingWeaponStats.BOW_DRAW, HuntingWeaponStats.BOW_SPEED, HuntingWeaponStats.BOW_ARROW_DAMAGE);

	public static Item STONE_HEADED_HATCHET;
	public static Item HUNTING_SPEAR;
	public static Item WOODLAND_HUNTING_BOW;

	private HuntingWeapons() {
	}

	/**
	 * A thrown arm with its own trait and tooltip ({@code trait}) that moves, flies and is thrown as {@code moves} does
	 * (the francisca or the javelin).
	 */
	public static class HuntingThrownItem extends ThrownArmItem {
		private final String moves;

		public HuntingThrownItem(String trait, String moves, JugcraftArms.Thrown thrown, Properties properties) {
			super(trait, thrown, properties);
			this.moves = moves;
		}

		/** The kind the arms' shared tables read: motion clips, flight and the throw's tooltip line. */
		@Override
		public String kind() {
			return moves;
		}
	}

	/** The hunting bow: the longbow's draw and shot, with its own tooltip ("arrows fly N% slower than a bow's"). */
	public static class HuntingBowItem extends ArmBowItem {
		public HuntingBowItem(JugcraftArms.Ranged ranged, Properties properties) {
			super(ranged, properties);
		}

		@Override
		public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip,
				TooltipFlag flag) {
			String key = "tooltip.jugcraft.arms." + ranged().name();
			TraitTooltips traits = TraitTooltips.of(tooltip);
			traits.trait(key + ".trait", ChatFormatting.YELLOW, Component.translatable(key),
					Component.translatable(key + ".stats", String.format(Locale.ROOT, "%.2f", ranged().draw() / 20.0F),
							HuntingWeaponStats.slowerPercent(ranged().speed())));
			traits.end();
		}
	}

	/** Registers the three (Jugcraft's start-up, after the Naval Cutlass), in the Combat tab. */
	public static void register() {
		STONE_HEADED_HATCHET = item(HATCHET_ID, properties -> new HuntingThrownItem("hatchet", "francisca", HATCHET_THROWN,
				JugcraftArms.arm(properties, ToolMaterial.STONE, HATCHET_KIND)));
		HUNTING_SPEAR = item(SPEAR_ID, properties -> new HuntingThrownItem(SPEAR_ID, "javelin", SPEAR_THROWN,
				JugcraftArms.arm(properties, ToolMaterial.STONE, SPEAR_KIND)));
		WOODLAND_HUNTING_BOW = item(BOW_ID, properties -> new HuntingBowItem(BOW_RANGED,
				properties.durability(HuntingWeaponStats.BOW_DURABILITY).repairable(ToolMaterial.WOOD.repairItems())
						.enchantable(ToolMaterial.WOOD.enchantmentValue())));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT).register(output -> {
			output.accept(STONE_HEADED_HATCHET);
			output.accept(HUNTING_SPEAR);
			output.accept(WOODLAND_HUNTING_BOW);
		});
	}

	private static Item item(String id, Function<Item.Properties, Item> factory) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Jugcraft.id(id));
		return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(new Item.Properties().setId(key)));
	}
}
