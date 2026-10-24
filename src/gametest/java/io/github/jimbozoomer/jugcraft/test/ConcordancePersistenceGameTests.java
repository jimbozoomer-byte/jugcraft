package io.github.jimbozoomer.jugcraft.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceProgress;
import io.github.jimbozoomer.jugcraft.concordance.CrucibleBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.CrucibleStandBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.JugcraftConcordance;
import io.github.jimbozoomer.jugcraft.concordance.RateGate;
import io.github.jimbozoomer.jugcraft.concordance.Rituals;
import io.github.jimbozoomer.jugcraft.concordance.Saved;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Band;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Formula;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Heat;
import io.github.jimbozoomer.jugcraft.concordance.courier.CourierLedger;
import io.github.jimbozoomer.jugcraft.concordance.courier.CourierPostBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.courier.Couriers;
import io.github.jimbozoomer.jugcraft.concordance.ecology.Habitat;
import io.github.jimbozoomer.jugcraft.concordance.ecology.Organism;
import io.github.jimbozoomer.jugcraft.concordance.garden.Garden;
import io.github.jimbozoomer.jugcraft.concordance.garden.VerdantBedBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.logistics.Place;
import io.github.jimbozoomer.jugcraft.concordance.resource.BoundWill;
import io.github.jimbozoomer.jugcraft.concordance.resource.BoundWillLedger;
import io.github.jimbozoomer.jugcraft.concordance.rime.Rime;
import io.github.jimbozoomer.jugcraft.concordance.rime.RimeGear;
import io.github.jimbozoomer.jugcraft.concordance.ritual.StructurePattern;
import io.github.jimbozoomer.jugcraft.concordance.rules.ResearchState;
import io.github.jimbozoomer.jugcraft.concordance.sky.AstralClaims;
import io.github.jimbozoomer.jugcraft.concordance.spire.SpireRecord;
import io.github.jimbozoomer.jugcraft.concordance.spirits.AgreementHolder;
import io.github.jimbozoomer.jugcraft.concordance.spirits.BoundWills;
import io.github.jimbozoomer.jugcraft.concordance.spirits.GatheringShadeEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactHistory;
import io.github.jimbozoomer.jugcraft.concordance.spirits.SpiritAnchorBlock;
import io.github.jimbozoomer.jugcraft.concordance.spirits.SpiritAnchorBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.WorkerRoster;
import io.github.jimbozoomer.jugcraft.concordance.spirits.Workers;
import io.github.jimbozoomer.jugcraft.concordance.starbound.ConclaveProjects;
import io.github.jimbozoomer.jugcraft.concordance.worker.Agreement;
import io.github.jimbozoomer.jugcraft.concordance.worker.Pacts;
import io.github.jimbozoomer.jugcraft.concordance.worker.Status;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

/**
 * Roadmap step 30, persistence and migration (docs/features/arcane-concordance-persistence.md): every format the
 * Concordance saves in a world reads its own saves from before versions began, is written with its version, brings an
 * older version forward step by step and reads what it can of a newer one; a record keeps an entry it cannot read
 * exactly as written; block entities stamp their version; the crucible keeps Preserve's hold through a save (Concordance
 * Chunk B, unit 3); the Crucible Stand keeps its JE and setting (Concordance Chunk C, C-05); the crucible keeps its
 * formula's setter and its courier request (C-06); the circle index looks only at the chunks in reach; and a Verdant
 * Bed saved before harvest strain reads unstrained (C-08), and one saved before grafts ungrafted (C-09); a Spirit Anchor
 * keeps its owner and agreement through a save (Concordance Chunk D, D-01); a Bound Will saved before pacts reads without
 * one, a pact record and its escrow round-trip, the pact history keeps eight, and an anchor from before Chunk D moves its
 * suspension onto its Bound Will once (D-02).
 */
public class ConcordancePersistenceGameTests {
	private static JsonElement json(String text) {
		return JsonParser.parseString(text);
	}

	private static <T> Dynamic<T> rename(Dynamic<T> data, String from, String to) {
		return data.get(from).result().map(value -> data.remove(from).set(to, value)).orElse(data);
	}

	/**
	 * A format at version 2, whose step from version 1 renamed a field: a save from before versions, one at version 1
	 * and one at version 2 all read the same; one from a newer version reads what it understands; and it is written at
	 * version 2.
	 */
	@GameTest(maxTicks = 20)
	public void aVersionedFormatReadsEveryOlderSave(GameTestHelper helper) {
		Codec<Integer> body = RecordCodecBuilder.create(i -> i.group(Codec.INT.fieldOf("count").forGetter(count -> count)).apply(i, count -> count));
		List<UnaryOperator<Dynamic<?>>> upgrades = List.of(UnaryOperator.identity(), data -> rename(data, "amount", "count"));
		Codec<Integer> codec = Saved.versioned("test", 2, body, upgrades);
		helper.assertValueEqual(codec.parse(JsonOps.INSTANCE, json("{\"amount\": 3}")).result().orElse(-1), 3,
				"a save from before versions is brought forward through every step");
		helper.assertValueEqual(codec.parse(JsonOps.INSTANCE, json("{\"version\": 1, \"data\": {\"amount\": 4}}")).result().orElse(-1), 4,
				"a version 1 save takes the rename");
		helper.assertValueEqual(codec.parse(JsonOps.INSTANCE, json("{\"version\": 2, \"data\": {\"count\": 5}}")).result().orElse(-1), 5,
				"a version 2 save reads as it is");
		helper.assertValueEqual(codec.parse(JsonOps.INSTANCE, json("{\"version\": 3, \"data\": {\"count\": 6, \"more\": true}}")).result().orElse(-1), 6,
				"a newer save is read as far as this version understands it");
		helper.assertValueEqual(codec.encodeStart(JsonOps.INSTANCE, 7).result().orElse(null), json("{\"version\": 2, \"data\": {\"count\": 7}}"),
				"it is written at its version");
		helper.succeed();
	}

	/**
	 * A spire record read entry by entry: a spire it cannot read (written by a newer version, or damaged) is kept and
	 * written back exactly as it was, beside the spire it can read, which reads in full.
	 */
	@GameTest(maxTicks = 20)
	public void aRecordKeepsAnEntryItCannotRead(GameTestHelper helper) {
		JsonElement readable = json("""
				{"id": "minecraft:overworld|1|64|1", "wonder": "jugcraft:concord_spire", "configuration": "jugcraft:lantern_spire",
				 "keeper": [0, 30, 0, 1], "communal": false, "phase": 2, "phase_began": 100, "practiced": 3, "rite": true,
				 "sustained": 1, "next_day": 24000, "last_attended": 50, "supplied": true, "founded": 10,
				 "contributors": [[0, 30, 0, 1]]}""");
		JsonElement damaged = json("{\"id\": \"minecraft:overworld|9|64|9\", \"wonder\": \"jugcraft:concord_spire\", \"phase\": \"tall\"}");
		JsonObject legacy = new JsonObject();
		legacy.add("minecraft:overworld|1|64|1", readable);
		legacy.add("minecraft:overworld|9|64|9", damaged);
		SpireRecord record = SpireRecord.CODEC.parse(JsonOps.INSTANCE, legacy).result().orElse(null);
		helper.assertTrue(record != null, "one damaged spire does not lose the record");
		helper.assertTrue(record.spire("minecraft:overworld|1|64|1") != null && record.spire("minecraft:overworld|1|64|1").phase() == 2
				&& record.spire("minecraft:overworld|9|64|9") == null, "the readable spire reads in full; the damaged one is not guessed at");
		JsonElement written = SpireRecord.CODEC.encodeStart(JsonOps.INSTANCE, record).result().orElse(null);
		helper.assertTrue(written != null && written.getAsJsonObject().get("version").getAsInt() == 1, "written with its version: " + written);
		JsonObject data = written.getAsJsonObject().getAsJsonObject("data");
		helper.assertTrue(data.get("minecraft:overworld|9|64|9").equals(damaged), "the damaged spire is written back exactly as it was: " + data);
		helper.assertTrue(data.get("minecraft:overworld|1|64|1").equals(readable), "and the readable one as it was read");
		helper.succeed();
	}

	/**
	 * Every record the Concordance keeps in a world reads its save from before versions (the bare data) into the same
	 * record it was written from: the spires, the worker roster, the Bound Wills (Concordance Chunk D: with a pact and its
	 * escrow), the sky's claims, the Conclave's projects, the courier ledger and the pact history.
	 */
	@GameTest(maxTicks = 20)
	public void everyRecordReadsItsSaveFromBeforeVersions(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		MinecraftServer server = level.getServer();
		DynamicOps<JsonElement> ops = level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
		UUID owner = UUID.randomUUID();
		WorkerRoster.of(server).note(owner, UUID.randomUUID(), "construct", Status.IDLE, "minecraft:overworld", helper.absolutePos(BlockPos.ZERO));
		BoundWills.of(server).seal(new BoundWill(UUID.randomUUID(), "jugcraft:hearth_pact", "jugcraft:ember_sprite", owner, 0L, false));
		UUID pact = UUID.randomUUID();
		Place where = new Place("jugcraft:test", (int) pact.getMostSignificantBits(), 1, (int) pact.getLeastSignificantBits());
		BoundWills.of(server).seal(new BoundWill(pact, "jugcraft:test_pact", "jugcraft:stolas", owner, 0L, false, new BoundWill.Pact(where,
				BoundWill.Hold.NONE, 0L, Pacts.PERSONAL, null, Pacts.terms("minecraft:wool", 1, 7 * Agreement.DAY, Agreement.DAY, 4))));
		BoundWills.of(server).deposit(pact, new ItemStack(Items.WOOL.white(), 2), stack -> stack.is(ItemTags.WOOL), 4);
		PactHistory.of(server).add(owner, new Pacts.Departure(UUID.randomUUID(), "jugcraft:test_pact", "jugcraft:stolas", Pacts.ENDED, 0L, where));
		AstralClaims.of(server).claim(owner, "jugcraft:test", 1L, level.getGameTime(), 0L);
		sameAfterLegacy(helper, ops, "spires", SpireRecord.CODEC, SpireRecord.of(server));
		sameAfterLegacy(helper, ops, "worker roster", WorkerRoster.CODEC, WorkerRoster.of(server));
		sameAfterLegacy(helper, ops, "Bound Wills", BoundWills.CODEC, BoundWills.of(server));
		sameAfterLegacy(helper, ops, "astral claims", AstralClaims.CODEC, AstralClaims.of(server));
		sameAfterLegacy(helper, ops, "Conclave projects", ConclaveProjects.CODEC, ConclaveProjects.of(server));
		sameAfterLegacy(helper, ops, "courier ledger", CourierLedger.CODEC, CourierLedger.of(server));
		sameAfterLegacy(helper, ops, "pact history", PactHistory.CODEC, PactHistory.of(server));
		helper.succeed();
	}

	private static <T> void sameAfterLegacy(GameTestHelper helper, DynamicOps<JsonElement> ops, String name, Codec<T> codec, T value) {
		JsonElement written = codec.encodeStart(ops, value).result().orElse(null);
		helper.assertTrue(written != null && written.isJsonObject() && written.getAsJsonObject().get("version").getAsInt() == 1,
				name + " is written with its version");
		JsonElement legacy = written.getAsJsonObject().get("data");
		T read = codec.parse(ops, legacy).result().orElse(null);
		helper.assertTrue(read != null, name + " reads its save from before versions");
		JsonElement again = codec.encodeStart(ops, read).result().orElse(null);
		helper.assertTrue(again != null && canonical(written).equals(canonical(again)), name + " reads back to what it was: " + again);
	}

	/** {@code element} with every list in one order (some records keep sets, whose lists may come out in any order). */
	private static JsonElement canonical(JsonElement element) {
		if (element.isJsonArray()) {
			List<JsonElement> items = new ArrayList<>();
			element.getAsJsonArray().forEach(item -> items.add(canonical(item)));
			items.sort(Comparator.comparing(JsonElement::toString));
			JsonArray out = new JsonArray();
			items.forEach(out::add);
			return out;
		}
		if (element.isJsonObject()) {
			JsonObject out = new JsonObject();
			element.getAsJsonObject().entrySet().forEach(entry -> out.add(entry.getKey(), canonical(entry.getValue())));
			return out;
		}
		return element;
	}

	/**
	 * A Concordance block entity's save carries its version, for a later format to read. The crucible's stays 1 with
	 * Preserve's optional hold (Concordance Chunk B, unit 3); the Crucible Stand's is 1 (Concordance Chunk C, C-05); the
	 * Spirit Anchor's is 1, with its optional owner (Concordance Chunk D).
	 */
	@GameTest(maxTicks = 20)
	public void blockEntitiesStampTheirVersion(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos pylon = new BlockPos(1, 1, 1);
		BlockPos sconce = new BlockPos(3, 1, 1);
		BlockPos crucible = new BlockPos(5, 1, 1);
		BlockPos stand = new BlockPos(1, 1, 3);
		BlockPos anchor = new BlockPos(3, 1, 3);
		helper.setBlock(pylon, JugcraftConcordance.LEY_PYLON);
		helper.setBlock(sconce, JugcraftConcordance.LUMEN_SCONCE);
		helper.setBlock(crucible, JugcraftConcordance.CRUCIBLE);
		helper.setBlock(stand, JugcraftConcordance.CRUCIBLE_STAND);
		helper.setBlock(anchor, Workers.SPIRIT_ANCHOR);
		for (BlockPos at : List.of(pylon, sconce, crucible, stand, anchor)) {
			BlockEntity entity = level.getBlockEntity(helper.absolutePos(at));
			CompoundTag saved = entity.saveWithoutMetadata(level.registryAccess());
			helper.assertValueEqual(saved.getIntOr(Saved.VERSION, 0), 1, entity.getType() + " stamps its version");
		}
		helper.succeed();
	}

	/**
	 * The circle index is kept by chunk: a change looks only at anchors within reach, across a chunk's edge too, and an
	 * anchor removed and placed again is indexed once.
	 */
	@GameTest(maxTicks = 20)
	public void theCircleIndexLooksOnlyInReach(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos relative = new BlockPos(2, 1, 2);
		helper.setBlock(relative, JugcraftConcordance.CIRCLE_ANCHOR);
		BlockPos anchor = helper.absolutePos(relative);
		int reach = StructurePattern.MAX_REACH;
		helper.assertTrue(Rituals.near(level, anchor.offset(reach, 0, 0)).contains(anchor)
				&& Rituals.near(level, anchor.offset(-reach, 0, reach)).contains(anchor), "a change at the edge of reach finds it, whichever chunk it is in");
		helper.assertTrue(!Rituals.near(level, anchor.offset(reach + 1, 0, 0)).contains(anchor)
				&& !Rituals.near(level, anchor.above(reach + 1)).contains(anchor), "one beyond reach does not");
		int loaded = Rituals.loaded(level);
		helper.setBlock(relative, Blocks.AIR);
		helper.setBlock(relative, JugcraftConcordance.CIRCLE_ANCHOR);
		helper.assertValueEqual(Rituals.loaded(level), loaded, "removed and placed again, it is indexed once");
		helper.assertValueEqual(Rituals.near(level, anchor).stream().filter(anchor::equals).count(), 1L, "and found once");
		helper.succeed();
	}

	/**
	 * Concordance Chunk B, unit 3: a crucible saves Preserve's hold as an optional field and keeps version 1. A hold
	 * round-trips exactly, a water focus's two minutes (unit 4, B-P50) too; a save without the field (one from before the
	 * unit), with or without its version, reads as not held and keeps its heat; a crucible not held writes no field; and a
	 * hold read from a save further ahead than any hold may be is cut to the longest within two ticks.
	 */
	@GameTest(maxTicks = 20)
	public void theCrucibleKeepsPreservesHoldThroughASave(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos relative = new BlockPos(2, 2, 2);
		helper.setBlock(relative.below(), Blocks.STONE);
		helper.setBlock(relative, JugcraftConcordance.CRUCIBLE);
		CrucibleBlockEntity crucible = (CrucibleBlockEntity) level.getBlockEntity(helper.absolutePos(relative));
		crucible.setTemperature(120);
		long now = level.getGameTime();
		crucible.hold(now, Rime.HOLD_TICKS);
		CompoundTag saved = crucible.saveWithoutMetadata(level.registryAccess());
		helper.assertTrue(saved.getIntOr(Saved.VERSION, 0) == 1 && saved.getLongOr(CrucibleBlockEntity.HELD_UNTIL, 0L) == now + Rime.HOLD_TICKS,
				"The hold is saved, still at version 1: " + saved);
		helper.assertTrue(load(level, crucible, saved).heldUntil() == now + Rime.HOLD_TICKS, "and read back exactly");
		BlockPos focusAt = new BlockPos(4, 2, 2);
		helper.setBlock(focusAt.below(), Blocks.STONE);
		helper.setBlock(focusAt, JugcraftConcordance.CRUCIBLE);
		CrucibleBlockEntity focused = (CrucibleBlockEntity) level.getBlockEntity(helper.absolutePos(focusAt));
		focused.hold(now, RimeGear.FOCUS_HOLD_TICKS);
		long focusHeld = load(level, focused, focused.saveWithoutMetadata(level.registryAccess())).heldUntil();
		helper.assertTrue(focusHeld == now + RimeGear.FOCUS_HOLD_TICKS, "A Focus of Water's two-minute hold is read back whole: "
				+ (focusHeld - now));
		CompoundTag before = saved.copy();
		before.remove(CrucibleBlockEntity.HELD_UNTIL);
		CompoundTag unversioned = before.copy();
		unversioned.remove(Saved.VERSION);
		for (CompoundTag legacy : List.of(before, unversioned)) {
			CrucibleBlockEntity read = load(level, crucible, legacy);
			helper.assertTrue(read.heldUntil() == 0L && read.temperature() == 120, "A save from before reads as not held, its heat kept: "
					+ read.heldUntil() + ", " + read.temperature());
		}
		crucible.hold(now, 0);
		helper.assertTrue(!crucible.saveWithoutMetadata(level.registryAccess()).contains(CrucibleBlockEntity.HELD_UNTIL),
				"A crucible not held writes no hold");
		CompoundTag forged = saved.copy();
		forged.putLong(CrucibleBlockEntity.HELD_UNTIL, now + 1_000_000L);
		crucible.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), forged));
		helper.runAfterDelay(2, () -> {
			long later = level.getGameTime();
			helper.assertTrue(crucible.heldUntil() > later && crucible.heldUntil() <= later + Rime.MAX_HOLD_TICKS,
					"A hold too far ahead is cut to the longest: " + (crucible.heldUntil() - later));
			helper.succeed();
		});
	}

	/**
	 * Concordance Chunk B, unit 4 (B-10): each of the travel kit's fifteen items survives a save as it was, a worn piece
	 * damaged and enchanted, a focus or bangle named (vanilla stacks; no format of the kit's own). The bangle's count is
	 * never saved, so a relog gives no free Brace (B-P49).
	 */
	@GameTest(maxTicks = 20)
	public void theTravelKitSurvivesASave(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		var ops = level.registryAccess().createSerializationContext(NbtOps.INSTANCE);
		var protection = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.PROTECTION);
		List<String> ids = new ArrayList<>(List.of("lesser_water_focus", "water_focus", "water_bangle"));
		ids.addAll(RimeGear.ARMOR.keySet());
		helper.assertTrue(ids.size() == 15, "The kit is fifteen items: " + ids);
		for (String id : ids) {
			ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.getValue(io.github.jimbozoomer.jugcraft.Jugcraft.id(id)));
			if (stack.isDamageableItem()) {
				stack.setDamageValue(7);
				stack.enchant(protection, 2);
			} else {
				stack.set(DataComponents.CUSTOM_NAME, Component.literal("Rimekeeper's " + id));
			}
			ItemStack copy = ItemStack.CODEC.parse(ops, ItemStack.CODEC.encodeStart(ops, stack).getOrThrow()).getOrThrow();
			helper.assertTrue(!stack.isEmpty() && ItemStack.matches(stack, copy), id + " does not survive a save: " + stack + " / " + copy);
		}
		helper.assertTrue(!RimeGear.BANGLE_LAST.isPersistent(), "The Water Bangle's count is not saved");
		helper.succeed();
	}

	/**
	 * Concordance Chunk C, C-05: a stand saves its JE and setting, stamped 1, and reads them back exactly. An older save
	 * without them, stamped or not, reads as an empty stand set off. A setting this build does not know reads as off, and JE
	 * outside its store is brought within it. What it paid for last is never saved.
	 */
	@GameTest(maxTicks = 20)
	public void theCrucibleStandKeepsItsEnergyAndSettingThroughASave(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos at = new BlockPos(2, 1, 2);
		helper.setBlock(at, JugcraftConcordance.CRUCIBLE_STAND);
		CrucibleStandBlockEntity stand = helper.getBlockEntity(at, CrucibleStandBlockEntity.class);
		ServerPlayer player = TestPlayers.mock(helper);
		player.setGameMode(GameType.SURVIVAL);
		for (int i = 0; i < 2; i++) {
			RateGate.forget(player.getUUID());
			stand.useEmpty(player, false);
		}
		stand.energy.setAmount(300);
		CompoundTag saved = stand.saveWithoutMetadata(level.registryAccess());
		helper.assertTrue(saved.getIntOr(Saved.VERSION, 0) == 1 && saved.getLongOr("energy", -1L) == 300L && "hot".equals(saved.getStringOr("setting", ""))
				&& !saved.contains("given"), "Saved: its JE and setting, at version 1: " + saved);
		CrucibleStandBlockEntity read = loadStand(level, stand, saved);
		helper.assertTrue(read.energy.getAmount() == 300 && read.setting() == Band.HOT && read.given() == Heat.AMBIENT, "Read back exactly");
		CompoundTag unversioned = saved.copy();
		unversioned.remove(Saved.VERSION);
		helper.assertTrue(loadStand(level, stand, unversioned).energy.getAmount() == 300 && loadStand(level, stand, unversioned).setting() == Band.HOT,
				"An unstamped save reads the same");
		CrucibleStandBlockEntity older = loadStand(level, stand, new CompoundTag());
		helper.assertTrue(older.energy.getAmount() == 0 && older.setting() == Band.COLD, "A save without them reads as empty and off");
		CompoundTag forged = saved.copy();
		forged.putLong("energy", 1_000_000L);
		forged.putString("setting", "boiling");
		CrucibleStandBlockEntity odd = loadStand(level, stand, forged);
		helper.assertTrue(odd.energy.getAmount() == CrucibleStandBlockEntity.CAPACITY && odd.setting() == Band.COLD,
				"JE beyond its store is cut to it; an unknown setting is off");
		forged.putLong("energy", -5L);
		helper.assertTrue(loadStand(level, stand, forged).energy.getAmount() == 0, "Negative JE reads as none");
		helper.succeed();
	}

	/**
	 * Concordance Chunk C, C-06: a crucible saves its formula's setter and its open courier request, stamped 1, and reads
	 * them back exactly. A save from before (stamped or not) reads as nobody's formula with no request, its formula and
	 * step kept; a request numbered 0 reads as none; a crucible with no formula writes neither key; broken, its request
	 * goes with it.
	 */
	@GameTest(maxTicks = 20)
	public void theCrucibleKeepsItsRequestThroughASave(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		String twoGlow = "water 2; add minecraft:glowstone_dust jugcraft:raw; add minecraft:glowstone_dust jugcraft:raw; stir hot; stir hot";
		// The setter is here: a crucible files only while its setter answers (C-P53).
		ServerPlayer setter = TestPlayers.mock(helper);
		UUID owner = setter.getUUID();
		BlockPos postAt = new BlockPos(4, 2, 2);
		helper.setBlock(postAt, Couriers.COURIER_POST);
		helper.getBlockEntity(postAt, CourierPostBlockEntity.class).setOwner(owner);
		BlockPos relative = new BlockPos(2, 2, 2);
		helper.setBlock(relative.below(), Blocks.STONE);
		helper.setBlock(relative, JugcraftConcordance.CRUCIBLE);
		CrucibleBlockEntity crucible = helper.getBlockEntity(relative, CrucibleBlockEntity.class);
		crucible.follow(Formula.parse(twoGlow), owner);
		try (Transaction transaction = Transaction.openOuter()) {
			crucible.water.insert(FluidVariant.of(Fluids.WATER), 2 * FluidConstants.BOTTLE, transaction);
			transaction.commit();
		}
		crucible.automate(level);
		crucible.automate(level);
		long id = crucible.request();
		CourierLedger ledger = CourierLedger.of(level.getServer());
		helper.assertTrue(id > 0L && ledger.ledger().get(id) != null && crucible.step() == 1, "It has asked a courier for its glowstone: request " + id);
		CompoundTag saved = crucible.saveWithoutMetadata(level.registryAccess());
		helper.assertTrue(saved.getIntOr(Saved.VERSION, 0) == 1 && saved.getLongOr(CrucibleBlockEntity.REQUEST, -1L) == id
				&& saved.contains(CrucibleBlockEntity.FORMULA_OWNER), "Saved: its setter and request, still at version 1: " + saved);
		CrucibleBlockEntity read = load(level, crucible, saved);
		helper.assertTrue(owner.equals(read.formulaOwner()) && read.request() == id && read.step() == 1, "and read back exactly");
		CompoundTag before = saved.copy();
		before.remove(CrucibleBlockEntity.FORMULA_OWNER);
		before.remove(CrucibleBlockEntity.REQUEST);
		CompoundTag unversioned = before.copy();
		unversioned.remove(Saved.VERSION);
		for (CompoundTag legacy : List.of(before, unversioned)) {
			CrucibleBlockEntity older = load(level, crucible, legacy);
			helper.assertTrue(older.formulaOwner() == null && older.request() == -1L && older.program() != null
					&& twoGlow.equals(older.program().text()) && older.step() == 1,
					"A save from before reads as nobody's formula with no request, the formula and its step kept");
		}
		CompoundTag forged = saved.copy();
		forged.putLong(CrucibleBlockEntity.REQUEST, 0L);
		helper.assertTrue(load(level, crucible, forged).request() == -1L, "A request numbered 0 reads as none");
		BlockPos freshAt = new BlockPos(6, 2, 2);
		helper.setBlock(freshAt, JugcraftConcordance.CRUCIBLE);
		CompoundTag fresh = helper.getBlockEntity(freshAt, CrucibleBlockEntity.class).saveWithoutMetadata(level.registryAccess());
		helper.assertTrue(!fresh.contains(CrucibleBlockEntity.FORMULA_OWNER) && !fresh.contains(CrucibleBlockEntity.REQUEST),
				"A crucible with no formula writes neither key: " + fresh);
		helper.setBlock(relative, Blocks.AIR);
		helper.assertTrue(ledger.ledger().get(id) == null, "Broken, its request goes with it");
		helper.succeed();
	}

	/**
	 * Concordance Chunk C (C-08): a Verdant Bed saved before harvest strain, with or without its version, reads unstrained and
	 * unripened and keeps the rest; a strain from a save beyond its cap is held to it; the bed's version stays 1. (C-09) A
	 * graft is saved, never its host, still at version 1; a bed saved before grafts reads ungrafted and saves no graft.
	 */
	@GameTest(maxTicks = 20)
	public void anOlderBedLoadsWithoutItsNewKeys(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos relative = new BlockPos(2, 1, 2);
		helper.setBlock(relative, Garden.VERDANT_BED);
		VerdantBedBlockEntity bed = (VerdantBedBlockEntity) level.getBlockEntity(helper.absolutePos(relative));
		UUID keeper = new UUID(30L, 8L);
		bed.awaken(keeper);
		bed.setNutrients(17);
		bed.setStrain(2);
		CompoundTag saved = bed.saveWithoutMetadata(level.registryAccess());
		helper.assertTrue(saved.getIntOr(Saved.VERSION, 0) == 1 && saved.getIntOr(VerdantBedBlockEntity.STRAIN, 0) == 2,
				"The strain is saved, still at version 1: " + saved);
		CompoundTag before = saved.copy();
		before.remove(VerdantBedBlockEntity.STRAIN);
		before.remove(VerdantBedBlockEntity.RIPENED);
		CompoundTag unversioned = before.copy();
		unversioned.remove(Saved.VERSION);
		for (CompoundTag legacy : List.of(before, unversioned)) {
			VerdantBedBlockEntity read = loadBed(level, bed, legacy);
			helper.assertTrue(read.strain() == 0 && !read.ripened() && read.nutrients() == 17 && read.awake() && keeper.equals(read.keeper()),
					"A bed from before reads unstrained and keeps the rest: " + read.strain() + ", " + read.nutrients());
		}
		CompoundTag forged = saved.copy();
		forged.putInt(VerdantBedBlockEntity.STRAIN, 99);
		helper.assertTrue(loadBed(level, bed, forged).strain() == Habitat.MAX_STRAIN, "A strain past its cap is held to it");
		forged.putInt(VerdantBedBlockEntity.STRAIN, -4);
		helper.assertTrue(loadBed(level, bed, forged).strain() == 0, "and one below none reads as none");
		bed.setGraft("jugcraft:dewlit");
		CompoundTag grafted = bed.saveWithoutMetadata(level.registryAccess());
		helper.assertTrue(grafted.getStringOr(VerdantBedBlockEntity.GRAFT, "").equals("jugcraft:dewlit") && !grafted.contains(VerdantBedBlockEntity.GRAFT_HOST)
				&& grafted.getIntOr(Saved.VERSION, 0) == 1, "The graft is saved, never its host, still at version 1: " + grafted);
		CompoundTag older = grafted.copy();
		older.remove(VerdantBedBlockEntity.GRAFT);
		Organism petal = Garden.catalog().organism("jugcraft:sunpetal");
		VerdantBedBlockEntity read = loadBed(level, bed, older);
		helper.assertTrue(petal != null && read.graft().isEmpty() && read.niche(petal).equals(petal.niche()) && read.nutrients() == 17,
				"A bed from before grafts reads ungrafted");
		helper.assertTrue(!read.saveWithoutMetadata(level.registryAccess()).contains(VerdantBedBlockEntity.GRAFT), "and saves no graft");
		helper.succeed();
	}

	/**
	 * D-01 (D-S8, G10): an anchor saves who placed it beside its agreement (the day's task included) and spirit, at version 1;
	 * one from before owners (stamped or not) reads as nobody's; one set without a placer writes neither.
	 */
	@GameTest(maxTicks = 20)
	public void anAnchorsAgreementRoundTrips(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer owner = TestPlayers.mock(helper);
		owner.setGameMode(GameType.SURVIVAL);
		RateGate.forget(owner.getUUID());
		ConcordanceProgress.grant(owner, "jugcraft:first_light", ResearchState.UNDERSTOOD);
		ConcordanceProgress.grant(owner, Workers.RESEARCH, ResearchState.UNDERSTOOD);
		BlockPos at = new BlockPos(2, 1, 2);
		helper.setBlock(at, Workers.SPIRIT_ANCHOR);
		BlockPos absolute = helper.absolutePos(at);
		Workers.SPIRIT_ANCHOR.setPlacedBy(level, absolute, level.getBlockState(absolute), owner, new ItemStack(Workers.SPIRIT_ANCHOR));
		SpiritAnchorBlockEntity anchor = helper.getBlockEntity(at, SpiritAnchorBlockEntity.class);
		SpiritAnchorBlock.use(owner, level, anchor, false);
		anchor.taskDone(level.getGameTime());
		helper.assertTrue(owner.getUUID().equals(anchor.owner()) && anchor.agreement() != null && anchor.spirit() != null
				&& anchor.agreement().done(level.getGameTime()) == 1, "Placed, sealed by its owner, one task done");
		CompoundTag saved = anchor.saveWithoutMetadata(level.registryAccess());
		helper.assertTrue(saved.getIntOr(Saved.VERSION, 0) == 1 && saved.contains("owner") && saved.contains("agreement") && saved.contains("spirit"),
				"Saved: its owner beside its agreement and spirit, still at version 1: " + saved);
		SpiritAnchorBlockEntity read = loadAnchor(level, anchor, saved);
		helper.assertTrue(owner.getUUID().equals(read.owner()) && anchor.agreement().equals(read.agreement()) && anchor.spirit().equals(read.spirit()),
				"and read back exactly");
		CompoundTag before = saved.copy();
		before.remove("owner");
		CompoundTag unversioned = before.copy();
		unversioned.remove(Saved.VERSION);
		for (CompoundTag legacy : List.of(before, unversioned)) {
			SpiritAnchorBlockEntity older = loadAnchor(level, anchor, legacy);
			helper.assertTrue(older.owner() == null && anchor.agreement().equals(older.agreement()) && anchor.spirit().equals(older.spirit()),
					"An anchor from before owners reads as nobody's, its agreement and spirit kept");
		}
		BlockPos freshAt = new BlockPos(4, 1, 2);
		helper.setBlock(freshAt, Workers.SPIRIT_ANCHOR);
		CompoundTag fresh = helper.getBlockEntity(freshAt, SpiritAnchorBlockEntity.class).saveWithoutMetadata(level.registryAccess());
		helper.assertTrue(!fresh.contains("owner") && !fresh.contains("agreement"), "An anchor set without a placer writes no owner: " + fresh);
		helper.succeed();
	}

	/**
	 * D-02 (D-P1): a Bound Will saved before pacts reads with no pact and no escrow; a pact's missing keys read as their
	 * defaults (no service terms copied, no task counted); one it cannot read is kept, known, and written back as it was; the
	 * bare save reads the same.
	 */
	@GameTest(maxTicks = 20)
	public void aBoundWillReadsItsOlderSave(GameTestHelper helper) {
		DynamicOps<JsonElement> ops = helper.getLevel().registryAccess().createSerializationContext(JsonOps.INSTANCE);
		JsonElement older = json("""
				{"version": 1, "data": {
				 "00000000-0000-0d02-0000-000000000001": {"id": [0, 3330, 0, 1], "agreement": "jugcraft:gathering_shade", "counterpart": "gathering_shade",
				  "holder": [0, 3330, 0, 9], "sealed_at": 77, "transferable": false},
				 "00000000-0000-0d02-0000-000000000002": {"id": [0, 3330, 0, 2], "agreement": "jugcraft:gathering_shade",
				  "counterpart": "jugcraft:gathering_shade", "holder": [0, 3330, 0, 9], "sealed_at": 78, "transferable": false,
				  "pact": {"place": {"dimension": "jugcraft:test", "x": 1, "y": 2, "z": 3}}},
				 "00000000-0000-0d02-0000-000000000003": {"id": [0, 3330, 0, 3], "agreement": "jugcraft:gathering_shade", "counterpart": "gathering_shade",
				  "holder": [0, 3330, 0, 9], "sealed_at": 79, "transferable": false, "pact": {"hold": "asleep"}}}}""");
		BoundWills read = BoundWills.CODEC.parse(ops, older).result().orElse(null);
		UUID holder = new UUID(0xD02L, 9L);
		helper.assertTrue(read != null && new BoundWill(new UUID(0xD02L, 1L), "jugcraft:gathering_shade", "gathering_shade", holder, 77L, false)
				.equals(read.ledger().get(new UUID(0xD02L, 1L))) && read.escrow(new UUID(0xD02L, 1L)).isEmpty(),
				"A Bound Will saved before pacts reads with no pact and no escrow");
		BoundWill placed = read.ledger().get(new UUID(0xD02L, 2L));
		helper.assertTrue(placed != null && new BoundWill.Pact(new Place("jugcraft:test", 1, 2, 3), BoundWill.Hold.NONE, 0L, Pacts.PERSONAL, null, null,
				null, BoundWill.Pact.NO_WINDOW, 0).equals(placed.pact()), "A pact's missing keys read as their defaults: " + placed);
		helper.assertTrue(read.ledger().get(new UUID(0xD02L, 3L)) == null && read.unreadable(new UUID(0xD02L, 3L)),
				"One it cannot read is not guessed at, and is known to be kept");
		JsonElement written = BoundWills.CODEC.encodeStart(ops, read).result().orElse(null);
		helper.assertTrue(written != null && written.equals(older), "and every entry is written back as it was: " + written);
		BoundWills bare = BoundWills.CODEC.parse(ops, older.getAsJsonObject().get("data")).result().orElse(null);
		helper.assertTrue(bare != null && read.ledger().get(new UUID(0xD02L, 1L)).equals(bare.ledger().get(new UUID(0xD02L, 1L))),
				"and the bare save from before versions reads the same");
		helper.succeed();
	}

	/**
	 * D-02 (D-P1, D-S6, D-S23): a full pact record (its service terms and day's count among it, critique 4) and its escrow
	 * (exact stacks, components and all) are saved and read back exactly.
	 */
	@GameTest(maxTicks = 20)
	public void aPactRecordRoundTrips(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		DynamicOps<JsonElement> ops = level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
		BoundWills wills = BoundWills.of(level.getServer());
		UUID id = UUID.randomUUID();
		Place place = new Place("jugcraft:test", (int) id.getMostSignificantBits(), 0, (int) id.getLeastSignificantBits());
		BoundWill.Upkeep upkeep = new BoundWill.Upkeep("minecraft:wool", 1, 7 * Agreement.DAY, Agreement.DAY, 4, 900_000L, 924_000L, 899_000L);
		BoundWill record = new BoundWill(id, "jugcraft:test_pact", "jugcraft:stolas", UUID.randomUUID(), 5L, false,
				new BoundWill.Pact(place, BoundWill.Hold.UPKEEP, 924_000L, Pacts.PARTY, UUID.randomUUID(), upkeep,
						new BoundWill.Service("gather", 8, 13000, 23000, 64), 38L, 5));
		helper.assertValueEqual(wills.seal(record).outcome(), BoundWillLedger.Outcome.DONE, "A full pact record is sealed");
		ItemStack plain = new ItemStack(Items.WOOL.white(), 2);
		ItemStack named = new ItemStack(Items.WOOL.white(), 1);
		named.set(DataComponents.CUSTOM_NAME, Component.literal("Offering"));
		List<ItemStack> given = List.of(plain.copy(), named.copy());
		Predicate<ItemStack> wool = stack -> stack.is(ItemTags.WOOL);
		wills.deposit(id, plain, wool, 4);
		wills.deposit(id, named, wool, 4);
		helper.assertTrue(plain.isEmpty() && named.isEmpty() && wills.escrow(id).size() == 2, "Its escrow keeps exact stacks: " + wills.escrow(id));
		JsonElement written = BoundWills.CODEC.encodeStart(ops, wills).result().orElse(null);
		JsonObject entry = written == null ? null : written.getAsJsonObject().getAsJsonObject("data").getAsJsonObject(id.toString());
		helper.assertTrue(entry != null && entry.has("pact") && entry.getAsJsonObject("pact").has("service") && entry.getAsJsonObject("pact").has("window")
				&& entry.getAsJsonObject("pact").has("done_today") && entry.getAsJsonArray("escrow").size() == 2, "Saved with its pact and its escrow: " + entry);
		BoundWills read = BoundWills.CODEC.parse(ops, written).result().orElse(null);
		List<ItemStack> back = read == null ? List.of() : read.escrow(id);
		helper.assertTrue(read != null && record.equals(read.ledger().get(id)) && back.size() == 2 && ItemStack.matches(back.get(0), given.get(0))
				&& ItemStack.matches(back.get(1), given.get(1)), "and read back exactly, components and all");
		helper.assertTrue(read.problems().isEmpty(), "sound: " + read.problems());
		wills.release(id, record.holder());
		helper.succeed();
	}

	/** D-02 (D-S6): a holder's last eight departures are kept, a place or none; read back exactly; a list it cannot read is written back as it was. */
	@GameTest(maxTicks = 20)
	public void thePactHistoryKeepsEight(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		DynamicOps<JsonElement> ops = level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
		PactHistory history = PactHistory.of(level.getServer());
		UUID holder = UUID.randomUUID();
		List<UUID> records = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			UUID record = UUID.randomUUID();
			records.add(record);
			history.add(holder, new Pacts.Departure(record, "jugcraft:test_pact", "jugcraft:stolas", i % 2 == 0 ? Pacts.ENDED : Pacts.BROKEN, i,
					i == 9 ? null : new Place("jugcraft:test", i, 0, 0)));
		}
		List<Pacts.Departure> kept = history.history(holder);
		helper.assertTrue(kept.size() == Pacts.HISTORY && kept.get(0).record().equals(records.get(2)) && kept.get(7).record().equals(records.get(9)),
				"A holder's last eight departures are kept, the oldest dropped");
		JsonElement written = PactHistory.CODEC.encodeStart(ops, history).result().orElse(null);
		helper.assertTrue(written != null, "The pact history is written");
		JsonArray damaged = new JsonArray();
		damaged.add(json("{\"record\": \"not a uuid\"}"));
		written.getAsJsonObject().getAsJsonObject("data").add(UUID.randomUUID().toString(), damaged);
		PactHistory read = PactHistory.CODEC.parse(ops, written).result().orElse(null);
		helper.assertTrue(read != null && read.history(holder).equals(kept), "read back exactly, a place or none");
		JsonElement again = PactHistory.CODEC.encodeStart(ops, read).result().orElse(null);
		helper.assertTrue(again != null && again.equals(written), "and a list it cannot read is written back as it was");
		helper.succeed();
	}

	/**
	 * D-02 (D-S9, D-P4): an anchor from before Chunk D, suspended on the anchor, moves its suspension onto its Bound Will once,
	 * with the terms and day's count it keeps; the anchor's flag is written false.
	 */
	@GameTest(maxTicks = 20)
	public void aSuspendedAnchorMigratesItsHold(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BoundWills wills = BoundWills.of(level.getServer());
		UUID holder = UUID.randomUUID();
		UUID record = UUID.randomUUID();
		UUID spirit = UUID.randomUUID();
		wills.seal(new BoundWill(record, GatheringShadeEntity.DEFINITION, "gathering_shade", holder, 5L, false));
		BlockPos at = new BlockPos(2, 1, 2);
		helper.setBlock(at, Workers.SPIRIT_ANCHOR);
		SpiritAnchorBlockEntity anchor = helper.getBlockEntity(at, SpiritAnchorBlockEntity.class);
		anchor.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(),
				olderAnchor(level, record, holder, helper.absolutePos(at), spirit, true)));
		helper.assertTrue(anchor.agreement().suspended() && wills.ledger().get(record).pact() == null, "A save from before: suspended on the anchor, no pact");
		long now = level.getGameTime();
		helper.assertValueEqual(anchor.holder().verify(level, now), AgreementHolder.Standing.VALID, "Its own anchor claims it");
		BoundWill claimed = wills.ledger().get(record);
		helper.assertTrue(claimed.pact() != null && claimed.pact().hold() == BoundWill.Hold.HOLDER
				&& Couriers.place(level, helper.absolutePos(at)).equals(claimed.pact().place()) && spirit.equals(claimed.pact().presence())
				&& new BoundWill.Service("gather", 8, 13000, 23000, 64).equals(claimed.pact().service())
				&& "gathering_shade".equals(claimed.counterpart()) && !anchor.agreement().suspended()
				&& anchor.holder().held(level) == Status.SUSPENDED, "Its suspension and terms move onto the Bound Will; the anchor's flag is cleared: " + claimed);
		helper.assertTrue(anchor.holder().verify(level, now + 20) == AgreementHolder.Standing.VALID && wills.ledger().get(record).equals(claimed),
				"Verified again, nothing changes");
		helper.assertTrue(!anchor.saveWithoutMetadata(level.registryAccess()).getCompoundOrEmpty("agreement").getBooleanOr("suspended", true),
				"and the anchor saves its flag cleared");
		wills.release(record, holder);
		helper.succeed();
	}

	/** An anchor's save from before Chunk D: its agreement sealed at {@code at}, its spirit, no owner (shared with ConcordanceSharingGameTests). */
	static CompoundTag olderAnchor(ServerLevel level, UUID record, UUID holder, BlockPos at, UUID spirit, boolean suspended) {
		TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		out.putInt(Saved.VERSION, 1);
		out.store("agreement", AgreementHolder.AGREEMENT_CODEC, new Agreement(record, holder, "gather", level.dimension().identifier().toString(),
				at.getX(), at.getY(), at.getZ(), 8, 13000, 23000, 64, Long.MIN_VALUE, 0, suspended));
		out.store("spirit", UUIDUtil.CODEC, spirit);
		return out.buildResult();
	}

	private static SpiritAnchorBlockEntity loadAnchor(ServerLevel level, SpiritAnchorBlockEntity like, CompoundTag saved) {
		SpiritAnchorBlockEntity copy = new SpiritAnchorBlockEntity(like.getBlockPos(), like.getBlockState());
		copy.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved));
		return copy;
	}

	private static VerdantBedBlockEntity loadBed(ServerLevel level, VerdantBedBlockEntity like, CompoundTag saved) {
		VerdantBedBlockEntity copy = new VerdantBedBlockEntity(like.getBlockPos(), like.getBlockState());
		copy.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved));
		return copy;
	}

	private static CrucibleBlockEntity load(ServerLevel level, CrucibleBlockEntity like, CompoundTag saved) {
		CrucibleBlockEntity copy = new CrucibleBlockEntity(like.getBlockPos(), like.getBlockState());
		copy.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved));
		return copy;
	}

	private static CrucibleStandBlockEntity loadStand(ServerLevel level, CrucibleStandBlockEntity like, CompoundTag saved) {
		CrucibleStandBlockEntity copy = new CrucibleStandBlockEntity(like.getBlockPos(), like.getBlockState());
		copy.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved));
		return copy;
	}
}
