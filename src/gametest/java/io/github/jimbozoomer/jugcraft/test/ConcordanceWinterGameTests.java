package io.github.jimbozoomer.jugcraft.test;

import eu.pb4.trinkets.api.TrinketsApi;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.concordance.Alchemy;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceData;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceEffects;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceProgress;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceSpells;
import io.github.jimbozoomer.jugcraft.concordance.CrucibleBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.JugcraftConcordance;
import io.github.jimbozoomer.jugcraft.concordance.RateGate;
import io.github.jimbozoomer.jugcraft.concordance.effect.Cause;
import io.github.jimbozoomer.jugcraft.concordance.effect.EffectSpec;
import io.github.jimbozoomer.jugcraft.concordance.effect.Ledger;
import io.github.jimbozoomer.jugcraft.concordance.ember.EmberArmorItem;
import io.github.jimbozoomer.jugcraft.concordance.ember.EmberGear;
import io.github.jimbozoomer.jugcraft.concordance.rime.Frostward;
import io.github.jimbozoomer.jugcraft.concordance.rime.Rime;
import io.github.jimbozoomer.jugcraft.concordance.rime.RimeGear;
import io.github.jimbozoomer.jugcraft.concordance.rime.RimeTrinketItem;
import io.github.jimbozoomer.jugcraft.concordance.rules.FocusPool;
import io.github.jimbozoomer.jugcraft.concordance.rules.ResearchState;
import io.github.jimbozoomer.jugcraft.concordance.strata.Brace;
import io.github.jimbozoomer.jugcraft.concordance.strata.Strata;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.internals.SpellExecution;
import net.spell_engine.internals.casting.SpellCast;
import net.spell_engine.internals.casting.SpellCaster;
import net.spell_engine.internals.container.SpellContainerSource;
import net.spell_engine.internals.target.SpellTarget;

/**
 * Concordance Chunk B, unit 4 (docs/features/arcane-concordance-winter.md), as far as a game-test server shows it.
 * <ul>
 * <li>B-08, Winter Brine's Frostward: a creature under it gathers no frost in powder snow, frost it had before is gone at
 * its next tick, and the status keeps its time through a save. (The brew itself is in {@link ConcordanceAlchemyGameTests},
 * and the Yeti King's chill passing a Frostward drinker by in {@link YetiKingGameTests}.)</li>
 * <li>B-10, the Rimekeeper's travel kit (the owner's water sets, foci and bangle; owner decision B-D2): the fifteen items
 * are made and worn as designed, within their bounds; every piece keeps powder snow from freezing its wearer, and none
 * walks on it; a focus of water worn, and only worn, lengthens Preserve's hold, through the effect boundary and through
 * Spell Engine; the Water Bangle braces a Rimekeeper who has gone thirty seconds without a Brace, as nobody's cast; and
 * Spell Power's attribute enchantments are refused on the water sets. (A kit wearer under the Yeti King's frost is in
 * {@link YetiKingGameTests}; the kit's saves in {@link ConcordancePersistenceGameTests}.)</li>
 * </ul>
 * B-10's Frost-boon practice joins this class with them.
 */
public class ConcordanceWinterGameTests {
	private static final String ARENA = "jugcraft-test:arms_arena";
	private static final String FIRST_LIGHT = "jugcraft:first_light";
	private static final String WAND = "jugcraft:initiate_wand";
	/**
	 * From (1, 2, 1) facing south, the eye (1.62 up) looks down to a point 0.4 above the foot of the block two south, just
	 * inside its north face: Preserve's touch meets a crucible standing at (1, 2, 3) (as ConcordanceRimeStrataGameTests).
	 */
	private static final float AIM = (float) Math.toDegrees(Math.atan2(1.62 - 0.4, 1.56));
	/** Vanilla's speed modifier for frost gathered (LivingEntity.SPEED_MODIFIER_POWDER_SNOW_ID). */
	private static final Identifier POWDER_SNOW = Identifier.withDefaultNamespace("powder_snow");
	private static final List<String> TRINKETS = List.of("lesser_water_focus", "water_focus", "water_bangle");
	private static final List<String> SETS = List.of("nereids", "aquamancers", "winterguards");
	/** Each set's pieces, head to feet. */
	private static final Map<String, List<String>> SET_PIECES = Map.of(
			"nereids", List.of("nereids_hood", "nereids_tunic", "nereids_pants", "nereids_shoes"),
			"aquamancers", List.of("aquamancers_hat", "aquamancers_robes", "aquamancers_leggings", "aquamancers_boots"),
			"winterguards", List.of("winterguards_helmet", "winterguards_chestplate", "winterguards_leggings", "winterguards_boots"));
	/** What each set may not exceed, slot by slot (B-P47): the Pyromaniac's, leather and iron, read from the live items. */
	private static final Map<String, List<String>> BOUNDS = Map.of(
			"nereids", List.of("jugcraft:pyromaniacs_hood", "jugcraft:pyromaniacs_tunic", "jugcraft:pyromaniacs_pants", "jugcraft:pyromaniacs_shoes"),
			"aquamancers", List.of("minecraft:leather_helmet", "minecraft:leather_chestplate", "minecraft:leather_leggings", "minecraft:leather_boots"),
			"winterguards", List.of("minecraft:iron_helmet", "minecraft:iron_chestplate", "minecraft:iron_leggings", "minecraft:iron_boots"));
	/** The GeckoLib model each set is worn as: the owner's medium model (Ember's import), or the heavy set's own. */
	private static final Map<String, String> MODELS = Map.of("nereids", EmberGear.ARMOR_MODEL, "aquamancers", EmberGear.ARMOR_MODEL,
			"winterguards", RimeGear.HEAVY_MODEL);
	private static final Map<String, int[]> DEFENSE = Map.of("nereids", new int[] {1, 2, 1, 1}, "aquamancers", new int[] {1, 3, 2, 1},
			"winterguards", new int[] {2, 6, 5, 2});
	/** Vanilla's per-slot base durability (11, 16, 15, 13) times each set's 7, 10 and 15. */
	private static final Map<String, int[]> DURABILITY = Map.of("nereids", new int[] {77, 112, 105, 91},
			"aquamancers", new int[] {110, 160, 150, 130}, "winterguards", new int[] {165, 240, 225, 195});
	private static final Map<String, Integer> ENCHANTABILITY = Map.of("nereids", 15, "aquamancers", 15, "winterguards", 9);
	private static final List<EquipmentSlot> SLOTS = List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
	private static final List<String> SLOT_TAGS = List.of("head", "chest", "leg", "foot");

	/** A stone floor under the first seven by three blocks, so a creature in powder snow stands on it. */
	private static void floor(GameTestHelper helper) {
		for (int x = 0; x <= 6; x++) {
			for (int z = 0; z <= 2; z++) {
				helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
			}
		}
	}

	/** {@code block} under the first {@code size} by {@code size} blocks. */
	private static void lay(GameTestHelper helper, Block block, int size) {
		for (int x = 0; x < size; x++) {
			for (int z = 0; z < size; z++) {
				helper.setBlock(new BlockPos(x, 1, z), block);
			}
		}
	}

	/** An item by its id: a bare path is Jugcraft's. */
	private static Item item(String id) {
		return BuiltInRegistries.ITEM.getValue(id.contains(":") ? Identifier.parse(id) : Jugcraft.id(id));
	}

	/** A vanilla item by its id (as ArmorTiersGameTests looks them up: not every item has an Items constant in 26.3). */
	private static Item vanilla(String path) {
		return BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(path));
	}

	private static boolean in(ItemStack stack, String tag) {
		return stack.is(TagKey.create(Registries.ITEM, Identifier.parse(tag)));
	}

	private static double amount(ItemStack stack, String attribute) {
		return stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).modifiers().stream()
				.filter(entry -> entry.attribute().getRegisteredName().equals(attribute))
				.mapToDouble(entry -> entry.modifier().amount()).sum();
	}

	/** A survival player with the Initiate's Wand on {@code standAt}, facing south (+Z), looking down {@code pitch} degrees. */
	private static ServerPlayer player(GameTestHelper helper, BlockPos standAt, float pitch) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		BlockPos absolute = helper.absolutePos(standAt);
		player.snapTo(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() + 0.5, 0.0F, pitch);
		player.setYHeadRot(0.0F);
		player.setYBodyRot(0.0F);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(JugcraftConcordance.INITIATE_WAND));
		RateGate.forget(player.getUUID());
		return player;
	}

	private static void refreshSpells(ServerPlayer player) {
		SpellContainerSource.setDirty(player, ConcordanceSpells.SOURCE);
		SpellContainerSource.setDirty(player, "main_hand");
		SpellContainerSource.update(player);
	}

	/** First Light understood (every entry's prerequisite), then {@code research} at {@code state}. */
	private static void learn(ServerPlayer player, String research, ResearchState state) {
		ConcordanceProgress.grant(player, FIRST_LIGHT, ResearchState.UNDERSTOOD);
		ConcordanceProgress.grant(player, research, state);
		refreshSpells(player);
	}

	private static void cast(ServerPlayer player, String id) {
		ServerLevel level = player.level();
		((SpellCaster.Player) player).getCooldownManager().reset(null);
		Holder<Spell> spell = SpellRegistry.from(level).get(Identifier.parse(id)).orElseThrow();
		SpellExecution.performSpell(level, player, spell, SpellTarget.SearchResult.empty(), SpellCast.Action.RELEASE, 1.0F);
	}

	/** The first effect of an invocation's composed form on the Initiate's Wand: what its cast applies. */
	private static EffectSpec invoked(String id) {
		return ConcordanceData.rules().authored(id, WAND).plan().root().steps().getFirst().effect();
	}

	private static ConcordanceEffects.Context context(GameTestHelper helper, ServerPlayer actor) {
		Cause cause = Cause.of(actor.getUUID(), Cause.Origin.INVOCATION, "test:winter", ConcordanceEffects.nextSerial());
		return new ConcordanceEffects.Context(helper.getLevel(), cause, actor, new Ledger(new Ledger.Limits(16, 256, 0)), "0/0/0",
				actor.position());
	}

	/** {@code spec} applied at {@code at} through the effect boundary, as {@code actor}'s own new event. */
	private static ConcordanceEffects.Result apply(GameTestHelper helper, ServerPlayer actor, EffectSpec spec, BlockPos at) {
		return ConcordanceEffects.apply(context(helper, actor), spec, helper.absolutePos(at));
	}

	private static CrucibleBlockEntity crucible(GameTestHelper helper, BlockPos at, Block below) {
		helper.setBlock(at.below(), below);
		helper.setBlock(at, JugcraftConcordance.CRUCIBLE);
		return (CrucibleBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(at));
	}

	/** Pours a bottle of water into {@code crucible} as someone who understands the Alembic Arts. */
	private static void pour(GameTestHelper helper, CrucibleBlockEntity crucible, BlockPos standAt) {
		ServerPlayer pourer = player(helper, standAt, 0.0F);
		ConcordanceProgress.grant(pourer, FIRST_LIGHT, ResearchState.UNDERSTOOD);
		ConcordanceProgress.grant(pourer, Alchemy.RESEARCH, ResearchState.UNDERSTOOD);
		ItemStack water = PotionContents.createItemStack(Items.POTION, Potions.WATER);
		RateGate.forget(pourer.getUUID());
		pourer.setItemInHand(InteractionHand.MAIN_HAND, water);
		crucible.use(pourer, pourer.level(), water, InteractionHand.MAIN_HAND);
	}

	/** The anchoring practice {@code player} holds (Stratawrighting's evidence keys). */
	private static List<String> anchoring(ServerPlayer player) {
		return ConcordanceProgress.knowledge(player).progress(Strata.RESEARCH).evidence().keySet().stream()
				.filter(key -> key.startsWith("practice:" + Strata.ACTIVITY + ":")).toList();
	}

	/**
	 * Frostward clears frost in its own effect tick, whatever gives it: a pig standing in powder snow gathers frost, one
	 * under Frostward beside it ends each tick with at most the one tick of frost powder snow has just added (never frozen
	 * through), and a pig on stone frozen 100 ticks' worth before is clear (melting alone, two a tick, would leave it 20).
	 * Saved and loaded, the warded pig's Frostward keeps its time and level.
	 */
	@GameTest(maxTicks = 60)
	public void frostwardClearsFrostAndKeepsItsTime(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		helper.setBlock(new BlockPos(1, 2, 1), Blocks.POWDER_SNOW);
		helper.setBlock(new BlockPos(3, 2, 1), Blocks.POWDER_SNOW);
		Mob bare = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(1, 2, 1));
		Mob warded = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(3, 2, 1));
		warded.addEffect(new MobEffectInstance(Frostward.FROSTWARD, 1200));
		Mob thawing = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(5, 2, 1));
		thawing.setTicksFrozen(100);
		thawing.addEffect(new MobEffectInstance(Frostward.FROSTWARD, 1200));
		helper.runAfterDelay(40, () -> {
			helper.assertTrue(bare.getTicksFrozen() >= 30, "A bare pig in powder snow gathers frost: " + bare.getTicksFrozen());
			helper.assertTrue(warded.getTicksFrozen() <= 1 && !warded.isFullyFrozen(),
					"Under Frostward it gathers none: " + warded.getTicksFrozen());
			helper.assertTrue(thawing.getTicksFrozen() == 0, "Frost gathered before is cleared: " + thawing.getTicksFrozen());
			TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
			warded.saveWithoutId(out);
			Mob copy = EntityTypes.PIG.create(level, EntitySpawnReason.COMMAND);
			copy.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), out.buildResult()));
			MobEffectInstance before = warded.getEffect(Frostward.FROSTWARD);
			MobEffectInstance after = copy.getEffect(Frostward.FROSTWARD);
			helper.assertTrue(before != null && after != null && after.getDuration() == before.getDuration()
					&& after.getAmplifier() == before.getAmplifier(), "Frostward keeps its time through a save: " + before + " / " + after);
			copy.discard();
			helper.succeed();
		});
	}

	// ---------------------------------------------------------------- B-10: the travel kit

	/**
	 * The fifteen items (B-P47, B-P51): the foci and the bangle are RimeGear's trinkets, one to a stack, not fire resistant,
	 * with no attribute of their own, worn only in the Spell Focus and Bracelet slots. Every piece of the three sets is
	 * Ember's GeckoLib armour on its model, worn in its slot as jugcraft:&lt;set&gt;, with its protection (never more than the
	 * Pyromaniac's, leather's or iron's piece, read from the live items), no toughness, knockback resistance or other
	 * attribute (no Spell Power), its durability and enchantability, repaired by its own tag (wool for the cloth sets, iron
	 * for Winterguard's), armour of its slot, and freeze-immune.
	 */
	@GameTest(maxTicks = 20)
	public void theTravelKitIsMadeAndWornAsDesigned(GameTestHelper helper) {
		List<String> wrong = new ArrayList<>();
		Map<String, Item> fields = Map.of("lesser_water_focus", RimeGear.LESSER_WATER_FOCUS, "water_focus", RimeGear.WATER_FOCUS,
				"water_bangle", RimeGear.WATER_BANGLE);
		for (String id : TRINKETS) {
			ItemStack stack = new ItemStack(item(id));
			if (stack.isEmpty() || stack.getItem() != fields.get(id) || !(stack.getItem() instanceof RimeTrinketItem)) {
				wrong.add(id + " is not registered as RimeGear's trinket");
				continue;
			}
			if (stack.getMaxStackSize() != 1 || stack.has(DataComponents.DAMAGE_RESISTANT)) {
				wrong.add(id + " is not one to a stack, or resists fire");
			}
			if (!stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).modifiers().isEmpty()) {
				wrong.add(id + " carries attributes of its own");
			}
		}
		for (String id : List.of("lesser_water_focus", "water_focus")) {
			if (!in(new ItemStack(item(id)), "trinkets:chest/spell_focus") || in(new ItemStack(item(id)), "trinkets:chest/necklace")) {
				wrong.add(id + " is not worn in the Spell Focus slot only");
			}
		}
		if (!in(new ItemStack(RimeGear.WATER_BANGLE), "trinkets:hand/bracelet") || in(new ItemStack(RimeGear.WATER_BANGLE), "trinkets:hand/ring")) {
			wrong.add("the Water Bangle is not worn in a Bracelet slot only");
		}
		var repairableType = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(Identifier.withDefaultNamespace("repairable"));
		for (String set : SETS) {
			TagKey<Item> repairs = TagKey.create(Registries.ITEM, Jugcraft.id("repairs_" + set + "_gear"));
			boolean cloth = !set.equals("winterguards");
			if (new ItemStack(vanilla("white_wool")).is(repairs) != cloth || new ItemStack(Items.IRON_INGOT).is(repairs) == cloth) {
				wrong.add("#jugcraft:repairs_" + set + "_gear is not " + (cloth ? "wool" : "iron"));
			}
			for (int p = 0; p < SLOTS.size(); p++) {
				String id = SET_PIECES.get(set).get(p);
				ItemStack stack = new ItemStack(item(id));
				if (stack.isEmpty() || !(stack.getItem() instanceof EmberArmorItem piece) || stack.getItem() != RimeGear.ARMOR.get(id)) {
					wrong.add(id + " is not registered as RimeGear's armour");
					continue;
				}
				if (!piece.set().equals(set) || !piece.model().equals(MODELS.get(set))) {
					wrong.add(id + " is worn as " + piece.set() + " on " + piece.model() + ", not " + set + " on " + MODELS.get(set));
				}
				var equippable = stack.get(DataComponents.EQUIPPABLE);
				if (equippable == null || equippable.slot() != SLOTS.get(p)
						|| !equippable.assetId().equals(Optional.of(ResourceKey.create(EquipmentAssets.ROOT_ID, Jugcraft.id(set))))) {
					wrong.add(id + " is not worn in " + SLOTS.get(p) + " as jugcraft:" + set + ": " + equippable);
				}
				double armor = amount(stack, "minecraft:armor");
				double bound = amount(new ItemStack(item(BOUNDS.get(set).get(p))), "minecraft:armor");
				if (armor != DEFENSE.get(set)[p] || armor > bound) {
					wrong.add(id + " has protection " + armor + ", not " + DEFENSE.get(set)[p] + " within " + BOUNDS.get(set).get(p) + "'s " + bound);
				}
				if (amount(stack, "minecraft:armor_toughness") != 0.0 || amount(stack, "minecraft:knockback_resistance") != 0.0) {
					wrong.add(id + " has toughness or knockback resistance");
				}
				List<String> others = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).modifiers().stream()
						.map(entry -> entry.attribute().getRegisteredName())
						.filter(name -> !name.equals("minecraft:armor") && !name.equals("minecraft:armor_toughness")).toList();
				if (!others.isEmpty()) {
					wrong.add(id + " carries other attributes (Spell Power among them?): " + others);
				}
				if (stack.getMaxDamage() != DURABILITY.get(set)[p]) {
					wrong.add(id + " lasts " + stack.getMaxDamage() + ", not " + DURABILITY.get(set)[p]);
				}
				var enchantable = stack.get(DataComponents.ENCHANTABLE);
				if (enchantable == null || enchantable.value() != ENCHANTABILITY.get(set)) {
					wrong.add(id + " is not enchantable at " + ENCHANTABILITY.get(set));
				}
				Object repairable = repairableType == null ? null : stack.get(repairableType);
				if (repairable == null || !String.valueOf(repairable).contains("jugcraft:repairs_" + set + "_gear")) {
					wrong.add(id + " is not repaired with #jugcraft:repairs_" + set + "_gear: " + repairable);
				}
				if (stack.has(DataComponents.DAMAGE_RESISTANT)) {
					wrong.add(id + " resists fire");
				}
				for (String tag : List.of(SLOT_TAGS.get(p) + "_armor", "enchantable/armor", "enchantable/" + SLOT_TAGS.get(p) + "_armor",
						"enchantable/durability", "enchantable/equippable", "freeze_immune_wearables")) {
					if (!in(stack, "minecraft:" + tag)) {
						wrong.add(id + " is not in #minecraft:" + tag);
					}
				}
			}
		}
		helper.assertTrue(wrong.isEmpty(), "The travel kit: " + String.join("; ", wrong));
		helper.succeed();
	}

	/**
	 * Twelve pigs in powder snow, each in one piece of the kit, and one bare (B-P46). After two seconds the bare one has
	 * gathered frost and is slowed by it; each wearer has gathered none, is not slowed and cannot freeze. None of the kit's
	 * boots walks on powder snow (vanilla keeps that to leather boots, which do).
	 */
	@GameTest(structure = ARENA, maxTicks = 60)
	public void everyPieceKeepsPowderSnowFromFreezingItsWearer(GameTestHelper helper) {
		lay(helper, Blocks.STONE, 16);
		List<String> pieces = SETS.stream().flatMap(set -> SET_PIECES.get(set).stream()).toList();
		Map<String, Mob> wearers = new LinkedHashMap<>();
		for (int i = 0; i < pieces.size(); i++) {
			Mob pig = pigInSnow(helper, i);
			ItemStack piece = new ItemStack(item(pieces.get(i)));
			pig.setItemSlot(piece.get(DataComponents.EQUIPPABLE).slot(), piece);
			wearers.put(pieces.get(i), pig);
		}
		Mob bare = pigInSnow(helper, pieces.size());
		helper.runAfterDelay(40, () -> {
			List<String> wrong = new ArrayList<>();
			if (bare.getTicksFrozen() < 30 || bare.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(POWDER_SNOW) == null) {
				wrong.add("a bare pig in powder snow gathered no frost, or is not slowed by it: " + bare.getTicksFrozen());
			}
			for (Map.Entry<String, Mob> wearer : wearers.entrySet()) {
				Mob pig = wearer.getValue();
				if (pig.getTicksFrozen() != 0 || pig.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(POWDER_SNOW) != null || pig.canFreeze()) {
					wrong.add("a pig in " + wearer.getKey() + " gathered frost (" + pig.getTicksFrozen() + "), is slowed by it, or can freeze");
				}
				if (wearer.getKey().endsWith("_shoes") || wearer.getKey().endsWith("_boots")) {
					if (PowderSnowBlock.canEntityWalkOnPowderSnow(pig)) {
						wrong.add(wearer.getKey() + " walk on powder snow");
					}
				}
			}
			bare.setItemSlot(EquipmentSlot.FEET, new ItemStack(vanilla("leather_boots")));
			if (!PowderSnowBlock.canEntityWalkOnPowderSnow(bare)) {
				wrong.add("leather boots no longer walk on powder snow (vanilla's own rule has changed)");
			}
			helper.assertTrue(wrong.isEmpty(), "Powder snow and the kit: " + String.join("; ", wrong));
			helper.succeed();
		});
	}

	/** A pig standing in its own block of powder snow, the {@code i}th of a row of seven, then a second row. */
	private static Mob pigInSnow(GameTestHelper helper, int i) {
		BlockPos at = new BlockPos(1 + 2 * (i % 7), 2, 2 + 4 * (i / 7));
		helper.setBlock(at, Blocks.POWDER_SNOW);
		return helper.spawnWithNoFreeWill(EntityTypes.PIG, at);
	}

	/**
	 * A focus of water worn in the Spell Focus slot lengthens Preserve's hold (B-P24, B-P50): with nothing worn, or a Focus
	 * of Water only in the hand, or a Focus of Fire in the slot, Preserve holds a crucible a minute; a Lesser Focus of Water
	 * worn, 90 seconds; a Focus of Water, two minutes, the crucible's longest, and a recast restarts it, never adds. Cast
	 * through Spell Engine with the focus worn, Preserve holds two minutes for its usual 3 Focus.
	 */
	@GameTest(maxTicks = 20)
	public void theWaterFociLengthenPreservesHold(GameTestHelper helper) {
		lay(helper, Blocks.STONE, 8);
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = player(helper, new BlockPos(1, 2, 1), AIM);
		learn(keeper, Rime.RESEARCH, ResearchState.UNDERSTOOD);
		BlockPos at = new BlockPos(1, 2, 3);
		CrucibleBlockEntity crucible = crucible(helper, at, Blocks.STONE);
		pour(helper, crucible, new BlockPos(6, 2, 6));
		helper.assertTrue(crucible.parts() == 1, "The crucible holds water");
		helper.assertTrue(RimeGear.holdTicks(null) == Rime.HOLD_TICKS && RimeGear.holdTicks(keeper) == Rime.HOLD_TICKS,
				"With nothing worn, Preserve holds its minute: " + RimeGear.holdTicks(keeper));
		EffectSpec preserve = invoked("jugcraft:preserve");
		long now = ConcordanceProgress.now(level);
		helper.assertTrue(apply(helper, keeper, preserve, at).applied() && crucible.held(now) == Rime.HOLD_TICKS,
				"Preserve holds the crucible a minute: " + crucible.held(now));
		keeper.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(RimeGear.WATER_FOCUS));
		helper.assertTrue(RimeGear.holdTicks(keeper) == Rime.HOLD_TICKS && apply(helper, keeper, preserve, at).applied()
				&& crucible.held(now) == Rime.HOLD_TICKS, "A Focus of Water in the hand does nothing: " + crucible.held(now));
		keeper.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(JugcraftConcordance.INITIATE_WAND));
		var focus = TrinketsApi.getAttachment(keeper).getInventory("chest/spell_focus");
		helper.assertTrue(focus != null, "Players have the Spell Focus slot");
		focus.setItem(0, new ItemStack(EmberGear.FIRE_FOCUS));
		helper.assertTrue(apply(helper, keeper, preserve, at).applied() && crucible.held(now) == Rime.HOLD_TICKS,
				"A Focus of Fire worn does nothing for Preserve: " + crucible.held(now));
		focus.setItem(0, new ItemStack(RimeGear.LESSER_WATER_FOCUS));
		helper.assertTrue(RimeGear.holdTicks(keeper) == RimeGear.LESSER_FOCUS_HOLD_TICKS && apply(helper, keeper, preserve, at).applied()
				&& crucible.held(now) == RimeGear.LESSER_FOCUS_HOLD_TICKS, "A Lesser Focus of Water worn: 90 seconds: " + crucible.held(now));
		focus.setItem(0, new ItemStack(RimeGear.WATER_FOCUS));
		helper.assertTrue(apply(helper, keeper, preserve, at).applied() && crucible.held(now) == RimeGear.FOCUS_HOLD_TICKS
				&& RimeGear.FOCUS_HOLD_TICKS == Rime.MAX_HOLD_TICKS, "A Focus of Water worn: two minutes, the longest: " + crucible.held(now));
		helper.assertTrue(apply(helper, keeper, preserve, at).applied() && crucible.held(now) == Rime.MAX_HOLD_TICKS
				&& crucible.heldUntil() == now + Rime.MAX_HOLD_TICKS, "A recast restarts it, never adds: " + (crucible.heldUntil() - now));
		// The same through Spell Engine, the focus still worn.
		crucible.hold(now, 0);
		ConcordanceProgress.setFocus(keeper, FocusPool.MAX);
		refreshSpells(keeper);
		cast(keeper, "jugcraft:preserve");
		long later = ConcordanceProgress.now(level);
		helper.assertTrue(crucible.held(later) == RimeGear.FOCUS_HOLD_TICKS && ConcordanceProgress.currentFocus(keeper) == FocusPool.MAX - 3,
				"Cast with the focus worn, Preserve holds two minutes for 3 Focus: " + crucible.held(later) + ", "
						+ ConcordanceProgress.currentFocus(keeper));
		focus.setItem(0, ItemStack.EMPTY);
		helper.succeed();
	}

	/**
	 * The Water Bangle (B-P25, B-P49), each look at its wearer given its own time. Before Rimekeeping is understood it
	 * does nothing. Then the first look only starts the count; 600 ticks with no Brace brace the wearer for Brace's own 100
	 * ticks (knockback resistance up by Brace's, Focus untouched); while braced the count starts again; spent, the next
	 * Brace comes 600 ticks after the last. A Brace from anywhere else starts the count too. A second bangle adds nothing,
	 * and a bangle taken off, or only held, does nothing.
	 */
	@GameTest(maxTicks = 20)
	public void theWaterBangleBracesARimekeeperEveryThirtySeconds(GameTestHelper helper) {
		int r = RimeGear.BANGLE_RECHARGE_TICKS;
		ServerPlayer wearer = player(helper, new BlockPos(1, 2, 1), 0.0F);
		var bracelets = TrinketsApi.getAttachment(wearer).getInventory("hand/bracelet");
		helper.assertTrue(bracelets != null, "Players have the Bracelet slots");
		bracelets.setItem(0, new ItemStack(RimeGear.WATER_BANGLE));
		helper.assertTrue(RimeGear.wearsBangle(wearer), "The bangle in a Bracelet slot is worn");
		ConcordanceProgress.grant(wearer, FIRST_LIGHT, ResearchState.UNDERSTOOD);
		helper.assertTrue(RimeGear.bangle(wearer, 1000) == null && RimeGear.bangle(wearer, 7000) == null && !wearer.hasEffect(Brace.BRACED),
				"Before Rimekeeping is understood the bangle does nothing");
		ConcordanceProgress.grant(wearer, Rime.RESEARCH, ResearchState.UNDERSTOOD);
		double resistance = wearer.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
		int focus = ConcordanceProgress.currentFocus(wearer);
		helper.assertTrue(RimeGear.bangle(wearer, 5000) == null, "The first look only starts the count");
		helper.assertTrue(RimeGear.bangle(wearer, 5000 + r - 1) == null, "A tick short of thirty seconds, nothing");
		ConcordanceEffects.Result first = RimeGear.bangle(wearer, 5000 + r);
		MobEffectInstance braced = wearer.getEffect(Brace.BRACED);
		helper.assertTrue(first != null && first.applied() && braced != null && braced.getDuration() == RimeGear.BANGLE_BRACE_TICKS
				&& braced.getAmplifier() == 0, "Thirty seconds without a Brace brace the wearer for 5 seconds: " + first + ", " + braced);
		helper.assertTrue(wearer.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) == resistance + Brace.RESISTANCE
				&& ConcordanceProgress.currentFocus(wearer) == focus, "with Brace's knockback resistance, and no Focus spent");
		long t1 = 5000 + r + 20;
		helper.assertTrue(RimeGear.bangle(wearer, t1) == null, "Braced, the count starts again");
		wearer.removeEffect(Brace.BRACED);
		helper.assertTrue(RimeGear.bangle(wearer, t1 + r - 1) == null, "Spent, nothing before thirty seconds more");
		ConcordanceEffects.Result second = RimeGear.bangle(wearer, t1 + r);
		helper.assertTrue(second != null && second.applied(), "and then a Brace again: " + second);
		wearer.removeEffect(Brace.BRACED);
		long t2 = t1 + r + 100;
		ConcordanceEffects.Result cast = ConcordanceEffects.apply(context(helper, wearer), invoked("jugcraft:brace"), wearer);
		helper.assertTrue(cast.applied() && RimeGear.bangle(wearer, t2) == null, "A Brace cast is a Brace had: " + cast);
		wearer.removeEffect(Brace.BRACED);
		helper.assertTrue(RimeGear.bangle(wearer, t2 + r - 1) == null, "so the count starts from it");
		bracelets.setItem(1, new ItemStack(RimeGear.WATER_BANGLE));
		ConcordanceEffects.Result two = RimeGear.bangle(wearer, t2 + r);
		MobEffectInstance once = wearer.getEffect(Brace.BRACED);
		helper.assertTrue(two != null && two.applied() && once != null && once.getDuration() == RimeGear.BANGLE_BRACE_TICKS
				&& once.getAmplifier() == 0, "Two bangles give one Brace, no longer and no stronger: " + once);
		wearer.removeEffect(Brace.BRACED);
		bracelets.setItem(0, ItemStack.EMPTY);
		bracelets.setItem(1, ItemStack.EMPTY);
		wearer.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(RimeGear.WATER_BANGLE));
		helper.assertTrue(!RimeGear.wearsBangle(wearer) && RimeGear.bangle(wearer, t2 + 4L * r) == null && !wearer.hasEffect(Brace.BRACED),
				"Taken off, or only held, the bangle does nothing");
		helper.succeed();
	}

	/**
	 * The bangle's Brace is nobody's cast: on a Stratawright standing on grass it takes, but names no caster and teaches
	 * no anchoring (no living ground); spent, it braces as any Brace (half the push let through), it goes when the tick
	 * ends, and nobody is credited with holding it.
	 */
	@GameTest(maxTicks = 10)
	public void theWaterBanglesBraceIsNobodysCast(GameTestHelper helper) {
		lay(helper, Blocks.GRASS_BLOCK, 4);
		ServerPlayer wearer = player(helper, new BlockPos(1, 2, 1), 0.0F);
		wearer.setOnGround(true);
		ConcordanceProgress.grant(wearer, FIRST_LIGHT, ResearchState.UNDERSTOOD);
		ConcordanceProgress.grant(wearer, Rime.RESEARCH, ResearchState.UNDERSTOOD);
		ConcordanceProgress.grant(wearer, Strata.RESEARCH, ResearchState.UNDERSTOOD);
		ConcordanceEffects.Result braced = RimeGear.brace(wearer);
		helper.assertTrue(braced.applied() && wearer.hasEffect(Brace.BRACED) && Brace.caster(wearer) == null,
				"The bangle's Brace takes and names no caster: " + braced + ", " + Brace.caster(wearer));
		helper.assertTrue(anchoring(wearer).isEmpty(), "It teaches no anchoring: " + anchoring(wearer));
		helper.assertTrue(Brace.spend(wearer) == 1.0 - Brace.RESISTANCE, "Spent, it lets half the push through");
		helper.runAfterDelay(1, () -> {
			helper.assertTrue(!wearer.hasEffect(Brace.BRACED) && anchoring(wearer).isEmpty(),
					"It goes at the tick's end, and nobody is credited with holding it: " + anchoring(wearer));
			helper.succeed();
		});
	}

	/** Spell Power's attribute enchantments (Sunfire) are refused on the three water sets (EmberGear's refusal of EmberArmorItem); Protection is not. */
	@GameTest(maxTicks = 20)
	public void spellPowerEnchantmentsAreRefusedOnTheWaterSets(GameTestHelper helper) {
		var enchantments = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
		Holder<Enchantment> sunfire = enchantments.getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, Identifier.parse("spell_power:sunfire")));
		Holder<Enchantment> protection = enchantments.getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, Identifier.withDefaultNamespace("protection")));
		for (String set : SETS) {
			for (String id : SET_PIECES.get(set)) {
				ItemStack piece = new ItemStack(item(id));
				for (EnchantingContext context : EnchantingContext.values()) {
					helper.assertTrue(!piece.canBeEnchantedWith(sunfire, context), id + " takes Sunfire (" + context + ")");
				}
				helper.assertTrue(piece.canBeEnchantedWith(protection, EnchantingContext.ACCEPTABLE), id + " refuses Protection");
			}
		}
		helper.succeed();
	}
}
