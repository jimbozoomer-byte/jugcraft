package io.github.jimbozoomer.jugcraft.guns;

import com.mojang.serialization.Codec;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.materials.JugcraftRegistry;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.UseEffects;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * Guns (docs/features/guns.md): the owner's guns, built from the owner's models and played with the owner's animations
 * through GeckoLib, the rounds they fire and the attachments fitted to them.
 * <ul>
 * <li>Left click fires (held, for an automatic), right click (held) aims down the sights, G reloads, H inspects
 * (client/GunsClient). The client asks; the server fires, loads and checks everything ({@link GunShots}).</li>
 * <li>A gun holds its loaded rounds in {@link #LOADED}; reloading takes rounds from the inventory.</li>
 * <li>Shots are hitscan: each bullet (or pellet) follows the shooter's look, strayed by the gun's spread, to the first
 * block or creature within range.</li>
 * <li>Attachments ({@link #ATTACHMENTS}), one a slot, are fitted in a crafting grid ({@link GunAttachmentRecipe}) and
 * held in {@link #FITTED}; they change the gun's numbers ({@link GunItem#spec(ItemStack)}) and show on its model.</li>
 * </ul>
 * The numbers are tools/guns.py's GUNS; tools/check_mod_data.py keeps the two the same.
 */
public final class JugcraftGuns {
	/** Each gun's numbers, in the order the creative tab shows them. */
	public static final Map<String, GunSpec> SPECS = new LinkedHashMap<>();

	static {
		SPECS.put("rust_midge", new GunSpec(2.0F, 1, 3, true, 20, 47, 0, 0, 0, 4.0F, 1.5F, 48, "light_round"));
		SPECS.put("patchwork_carbine", new GunSpec(6.0F, 1, 6, false, 10, 48, 0, 0, 0, 2.0F, 0.25F, 96, "rifle_round"));
		SPECS.put("thunderpipe", new GunSpec(2.5F, 8, 8, false, 2, 0, 5, 12, 13, 9.0F, 6.0F, 24, "buckshot_shell"));
		SPECS.put("warden_pistol", new GunSpec(4.0F, 1, 5, false, 12, 47, 0, 0, 0, 2.5F, 0.75F, 56, "light_round"));
		SPECS.put("riveter_smg", new GunSpec(2.5F, 1, 3, true, 30, 45, 0, 0, 0, 3.0F, 1.0F, 48, "light_round"));
		SPECS.put("haymaker", new GunSpec(3.0F, 8, 14, false, 5, 0, 7, 11, 23, 7.0F, 5.0F, 28, "buckshot_shell"));
		SPECS.put("longhorn_rifle", new GunSpec(8.0F, 1, 13, false, 6, 0, 8, 11, 14, 1.5F, 0.15F, 120, "rifle_round"));
		SPECS.put("drover_rifle", new GunSpec(6.5F, 1, 13, false, 10, 0, 8, 12, 17, 2.0F, 0.3F, 100, "rifle_round"));
		SPECS.put("coach_gun", new GunSpec(3.0F, 8, 8, false, 2, 0, 12, 13, 13, 6.0F, 4.0F, 32, "buckshot_shell"));
		SPECS.put("duelling_pistol", new GunSpec(9.0F, 1, 20, false, 1, 74, 0, 0, 0, 4.0F, 2.0F, 32, "paper_cartridge"));
		SPECS.put("line_musket", new GunSpec(14.0F, 1, 8, false, 1, 78, 0, 0, 0, 2.5F, 0.75F, 64, "paper_cartridge"));
		SPECS.put("bellmouth", new GunSpec(2.5F, 10, 10, false, 1, 78, 0, 0, 0, 12.0F, 9.0F, 20, "paper_cartridge"));
	}

	/** The attachments, in the order the creative tab shows them (tools/guns.py ATTACHMENTS). */
	public static final Map<String, GunAttachment> ATTACHMENTS = new LinkedHashMap<>();

	static {
		ATTACHMENTS.put("silencer", new GunAttachment("barrel", false, 0.95F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.35F));
		ATTACHMENTS.put("baffled_silencer", new GunAttachment("barrel", false, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.2F));
		ATTACHMENTS.put("muzzle_brake", new GunAttachment("barrel", false, 1.0F, 1.0F, 1.0F, 0.85F, 1.0F, 1.0F, 0.5F, 1.0F));
		ATTACHMENTS.put("extended_barrel", new GunAttachment("barrel", true, 1.0F, 1.3F, 0.85F, 0.85F, 1.0F, 1.0F, 1.0F, 1.0F));
		ATTACHMENTS.put("extended_magazine", new GunAttachment("magazine", true, 1.0F, 1.0F, 1.0F, 1.0F, 1.5F, 1.15F, 1.0F, 1.0F));
		ATTACHMENTS.put("speed_magazine", new GunAttachment("magazine", true, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.65F, 1.0F, 1.0F));
		ATTACHMENTS.put("light_stock", new GunAttachment("stock", true, 1.0F, 1.0F, 0.85F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F));
		ATTACHMENTS.put("weighted_stock", new GunAttachment("stock", true, 1.0F, 1.0F, 1.0F, 0.7F, 1.0F, 1.0F, 0.6F, 1.0F));
		ATTACHMENTS.put("wooden_stock", new GunAttachment("stock", true, 1.0F, 1.0F, 0.9F, 0.85F, 1.0F, 1.0F, 0.75F, 1.0F));
		ATTACHMENTS.put("light_grip", new GunAttachment("grip", false, 1.0F, 1.0F, 0.8F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F));
		ATTACHMENTS.put("vertical_grip", new GunAttachment("grip", false, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.65F, 1.0F));
	}

	/** The attachment slots: a gun takes one attachment in each (tools/guns.py SLOTS). */
	public static final List<String> SLOTS = List.of("barrel", "magazine", "stock", "grip");
	/** The attachments each gun takes: those it has a part of its own for (tools/guns.py fits()). */
	public static final Map<String, List<String>> ACCEPTS = new LinkedHashMap<>();

	static {
		ACCEPTS.put("rust_midge", List.of("silencer", "baffled_silencer", "muzzle_brake", "extended_barrel",
				"extended_magazine", "speed_magazine", "light_stock", "weighted_stock", "wooden_stock"));
		ACCEPTS.put("patchwork_carbine", List.of("silencer", "baffled_silencer", "muzzle_brake", "extended_barrel",
				"extended_magazine", "speed_magazine", "light_stock", "weighted_stock", "wooden_stock", "light_grip",
				"vertical_grip"));
		ACCEPTS.put("thunderpipe", List.of("silencer", "baffled_silencer", "muzzle_brake", "extended_barrel",
				"light_stock", "weighted_stock", "wooden_stock", "light_grip", "vertical_grip"));
		ACCEPTS.put("warden_pistol", List.of("silencer", "baffled_silencer", "muzzle_brake", "extended_barrel",
				"extended_magazine", "speed_magazine"));
		ACCEPTS.put("riveter_smg", List.of("silencer", "baffled_silencer", "muzzle_brake", "extended_barrel",
				"extended_magazine", "speed_magazine", "light_stock", "weighted_stock", "wooden_stock"));
		ACCEPTS.put("haymaker", List.of("silencer", "baffled_silencer", "muzzle_brake", "extended_barrel"));
		ACCEPTS.put("longhorn_rifle", List.of("silencer", "baffled_silencer", "muzzle_brake", "extended_barrel",
				"light_stock", "weighted_stock", "wooden_stock", "light_grip", "vertical_grip"));
	}

	/** The rounds. */
	public static final List<String> AMMO = List.of("light_round", "rifle_round", "buckshot_shell", "paper_cartridge");
	/** The sound events the animations and the guns play (assets/jugcraft/sounds.json, written by tools/guns.py). */
	public static final List<String> SOUND_EVENTS = List.of("bolt", "bolt_pull", "bolt_release", "dry_fire",
			"gun_rustle", "insert", "jam", "lever", "metal", "rack", "reload_end", "reload_mag_in", "reload_mag_out",
			"shell_in", "slap");
	/** Walking speed while aiming down the sights (vanilla's using an item is 0.2). */
	public static final float AIM_SPEED = 0.6F;

	public static final Map<String, GunItem> GUNS = new LinkedHashMap<>();
	public static final Map<String, Item> ROUNDS = new LinkedHashMap<>();
	public static final Map<String, SoundEvent> SOUNDS = new LinkedHashMap<>();
	public static final Map<String, Item> ATTACHMENT_ITEMS = new LinkedHashMap<>();
	public static final ResourceKey<DamageType> BULLET = ResourceKey.create(Registries.DAMAGE_TYPE, Jugcraft.id("bullet"));
	/** Rounds loaded in a gun. */
	public static DataComponentType<Integer> LOADED;
	/** The attachments fitted to a gun, oldest first; one a slot, so four at most. */
	public static DataComponentType<List<String>> FITTED;
	public static RecipeSerializer<GunAttachmentRecipe> ATTACHMENT_SERIALIZER;
	public static RecipeSerializer<GunAttachmentRemovalRecipe> ATTACHMENT_REMOVAL_SERIALIZER;

	private JugcraftGuns() {
	}

	public static void register() {
		LOADED = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Jugcraft.id("loaded_rounds"),
				DataComponentType.<Integer>builder().persistent(Codec.intRange(0, 64)).networkSynchronized(ByteBufCodecs.VAR_INT)
						.build());
		FITTED = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Jugcraft.id("attachments"),
				DataComponentType.<List<String>>builder().persistent(Codec.STRING.listOf(0, 4))
						.networkSynchronized(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list(4))).build());
		ATTACHMENT_SERIALIZER = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Jugcraft.id("gun_attachment"),
				GunAttachmentRecipe.SERIALIZER);
		ATTACHMENT_REMOVAL_SERIALIZER = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Jugcraft.id("gun_attachment_removal"),
				GunAttachmentRemovalRecipe.SERIALIZER);
		for (String event : SOUND_EVENTS) {
			sound("guns." + event);
		}
		for (String gun : SPECS.keySet()) {
			sound("guns." + gun + ".fire");
		}
		for (String round : AMMO) {
			ROUNDS.put(round, JugcraftRegistry.item(round, AmmoItem::new));
		}
		SPECS.forEach((name, spec) -> GUNS.put(name, (GunItem) JugcraftRegistry.item(name, properties -> new GunItem(name, spec,
				properties.stacksTo(1).rarity(Rarity.UNCOMMON).component(LOADED, 0)
						.component(DataComponents.USE_EFFECTS, new UseEffects(false, true, AIM_SPEED))))));
		ATTACHMENTS.keySet().forEach(name -> ATTACHMENT_ITEMS.put(name, JugcraftRegistry.item(name,
				properties -> new AttachmentItem(name, properties.stacksTo(16)))));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT).register(output -> {
			GUNS.values().forEach(output::accept);
			ROUNDS.values().forEach(output::accept);
			ATTACHMENT_ITEMS.values().forEach(output::accept);
		});
		GunShots.register();
	}

	/** The sound event guns.&lt;name&gt; or guns.&lt;gun&gt;.fire. */
	public static SoundEvent sound(String path) {
		SoundEvent known = SOUNDS.get(path);
		if (known != null) {
			return known;
		}
		Identifier id = Jugcraft.id(path);
		SoundEvent event = Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
		SOUNDS.put(path, event);
		return event;
	}

	/** The round a gun fires. */
	public static Item ammo(GunSpec spec) {
		return ROUNDS.get(spec.ammo());
	}
}
