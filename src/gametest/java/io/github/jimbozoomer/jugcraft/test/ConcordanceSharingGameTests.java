package io.github.jimbozoomer.jugcraft.test;

import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.JsonOps;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.compat.jade.ConcordanceDataProvider;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceData;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceProgress;
import io.github.jimbozoomer.jugcraft.concordance.Examination;
import io.github.jimbozoomer.jugcraft.concordance.JugcraftConcordance;
import io.github.jimbozoomer.jugcraft.concordance.KindledLanternItem;
import io.github.jimbozoomer.jugcraft.concordance.LanternCharge;
import io.github.jimbozoomer.jugcraft.concordance.LumenSconceBlock;
import io.github.jimbozoomer.jugcraft.concordance.LumenSconceBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.RateGate;
import io.github.jimbozoomer.jugcraft.concordance.ResearchNotes;
import io.github.jimbozoomer.jugcraft.concordance.ResearchNotesCopyRecipe;
import io.github.jimbozoomer.jugcraft.concordance.ResearchNotesItem;
import io.github.jimbozoomer.jugcraft.concordance.assay.Assaying;
import io.github.jimbozoomer.jugcraft.concordance.courier.Couriers;
import io.github.jimbozoomer.jugcraft.concordance.garden.Garden;
import io.github.jimbozoomer.jugcraft.concordance.resource.BoundWill;
import io.github.jimbozoomer.jugcraft.concordance.rules.Definitions;
import io.github.jimbozoomer.jugcraft.concordance.rules.Evidence;
import io.github.jimbozoomer.jugcraft.concordance.rules.EvidenceRule;
import io.github.jimbozoomer.jugcraft.concordance.rules.Knowledge;
import io.github.jimbozoomer.jugcraft.concordance.rules.ResearchState;
import io.github.jimbozoomer.jugcraft.concordance.resource.Transfers;
import io.github.jimbozoomer.jugcraft.concordance.sky.AstrolabeItem;
import io.github.jimbozoomer.jugcraft.concordance.sky.ObservatoryBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.AgreementHolder;
import io.github.jimbozoomer.jugcraft.concordance.spirits.BoundWills;
import io.github.jimbozoomer.jugcraft.concordance.spirits.GatheringShadeEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactHistory;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactKeeping;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactMenu;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactPerchBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactRequestPayload;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactStatePayload;
import io.github.jimbozoomer.jugcraft.concordance.spirits.SpiritAnchorBlock;
import io.github.jimbozoomer.jugcraft.concordance.spirits.SpiritAnchorBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.StarReckoning;
import io.github.jimbozoomer.jugcraft.concordance.spirits.StolasEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.StolasRegard;
import io.github.jimbozoomer.jugcraft.concordance.spirits.WorkerRoster;
import io.github.jimbozoomer.jugcraft.concordance.spirits.Workers;
import io.github.jimbozoomer.jugcraft.concordance.worker.Agreement;
import io.github.jimbozoomer.jugcraft.concordance.worker.Pacts;
import io.github.jimbozoomer.jugcraft.concordance.worker.Status;
import io.github.jimbozoomer.jugcraft.concordance.worker.WorkerDefinition;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import io.github.jimbozoomer.jugcraft.party.JugcraftParties;
import io.github.jimbozoomer.jugcraft.party.PartyManager;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Roadmap steps 6 and 7 in the game (docs/features/arcane-concordance-sharing.md): Research Notes carry one player's
 * instructions to another without carrying their observation or mastery, and the Lumen Sconce takes Radiance only
 * through the typed transfer rules (what fits, at the rate allowed, drawn back only by its owner); and (Concordance Chunk
 * D, D-02) a copied Spirit Anchor gives nothing and releases nothing, an anchor from before Chunk D claims its record only
 * at its own place, and ending a pact twice returns its escrow once; and (D-04) a copied pact perch is inert; and (D-05)
 * it recalls nothing, and nobody but its holder (their party, a stranger, a stand-in) uses, pays, suspends or ends their
 * pact or keeps its obligation; and (Gloam Ink, C-D6) a fair copy teaches exactly what its original does, is made only
 * from an original with one blank and one ink, in the player's own grid, gives Stolas's Regard nothing, and an older
 * sheet reads as an original.
 */
public class ConcordanceSharingGameTests {
	private static final String FIRST_LIGHT = "jugcraft:first_light";
	/** Concordance Chunk D: Gentle's upkeep numbers on the vanilla #minecraft:wool tag. */
	private static final BoundWill.Upkeep UPKEEP = Pacts.terms("minecraft:wool", 1, 7 * Agreement.DAY, Agreement.DAY, 4);

	private static ServerPlayer player(GameTestHelper helper, BlockPos standAt) {
		ServerPlayer player = TestPlayers.mock(helper);
		player.setGameMode(GameType.SURVIVAL);
		BlockPos absolute = helper.absolutePos(standAt);
		player.snapTo(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() + 0.5, 0.0F, 0.0F);
		RateGate.forget(player.getUUID());
		return player;
	}

	private static boolean earned(ServerPlayer player, Identifier id) {
		AdvancementHolder advancement = player.level().getServer().getAdvancements().get(id);
		return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
	}

	private static ResearchState firstLight(ServerPlayer player) {
		return ConcordanceProgress.knowledge(player).state(FIRST_LIGHT);
	}

	/** The written sheet in a player's inventory, or empty. */
	private static ItemStack writtenNotes(ServerPlayer player) {
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
			ItemStack stack = player.getInventory().getItem(slot);
			if (stack.has(JugcraftConcordance.RESEARCH_NOTES)) {
				return stack;
			}
		}
		return ItemStack.EMPTY;
	}

	/**
	 * Two players: one who understood First Light writes notes; the other reads them. The reader gains the evidence
	 * but no state (and no codex page) until they observe it themselves; then they understand it at once. Reading again
	 * adds nothing, the author cannot learn from their own notes, notes never carry mastery, and a sheet is written
	 * only by someone who knows something.
	 */
	@GameTest(maxTicks = 20)
	public void notesShareInstructionsNotExperience(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer author = player(helper, new BlockPos(1, 2, 1));
		ServerPlayer reader = player(helper, new BlockPos(3, 2, 1));
		ServerPlayer novice = player(helper, new BlockPos(5, 2, 1));
		Identifier understood = ConcordanceProgress.advancementFor(FIRST_LIGHT, ResearchState.UNDERSTOOD);

		ItemStack blanks = new ItemStack(JugcraftConcordance.RESEARCH_NOTES_ITEM, 2);
		novice.setItemInHand(InteractionHand.MAIN_HAND, blanks.copy());
		helper.assertTrue(ResearchNotesItem.write(novice, InteractionHand.MAIN_HAND, novice.getMainHandItem()) == InteractionResult.FAIL
				&& novice.getMainHandItem().getCount() == 2, "Someone who knows nothing writes nothing and keeps their paper");

		ConcordanceProgress.grant(author, FIRST_LIGHT, ResearchState.MASTERED);
		author.setItemInHand(InteractionHand.MAIN_HAND, blanks);
		ResearchNotesItem.write(author, InteractionHand.MAIN_HAND, author.getMainHandItem());
		ItemStack sheet = writtenNotes(author);
		ResearchNotes notes = sheet.get(JugcraftConcordance.RESEARCH_NOTES);
		helper.assertTrue(notes != null && notes.author().equals(author.getUUID()) && author.getMainHandItem().getCount() == 1
				&& notes.entries().get(FIRST_LIGHT) == ResearchState.UNDERSTOOD,
				"A mastered author's notes record First Light as understood (mastery is not written down): " + notes);
		helper.assertTrue(ResearchNotes.CODEC.parse(JsonOps.INSTANCE, ResearchNotes.CODEC.encodeStart(JsonOps.INSTANCE, notes).getOrThrow())
				.getOrThrow().equals(notes), "Notes round-trip through their codec");

		Knowledge before = ConcordanceProgress.knowledge(author);
		helper.assertTrue(ResearchNotesItem.read(author, notes) == InteractionResult.FAIL
				&& ConcordanceProgress.knowledge(author).equals(before), "An author learns nothing from their own notes");

		ResearchNotesItem.read(reader, notes);
		helper.assertTrue(firstLight(reader) == ResearchState.NONE && !earned(reader, understood)
				&& ConcordanceProgress.knowledge(reader).progress(FIRST_LIGHT).evidence().containsKey("notes:" + author.getUUID()),
				"Read first, the notes are kept as evidence, but the reader has no state and no codex page yet");
		Knowledge kept = ConcordanceProgress.knowledge(reader);
		ResearchNotesItem.read(reader, notes);
		helper.assertTrue(ConcordanceProgress.knowledge(reader).equals(kept), "Reading the same notes again adds nothing");

		ConcordanceProgress.record(reader, new Evidence.Examined("minecraft:amethyst_shard", 0));
		helper.assertTrue(firstLight(reader) == ResearchState.UNDERSTOOD && earned(reader, understood)
				&& ConcordanceProgress.knowledge(reader).invocationCost("jugcraft:kindle") == 4,
				"Observing it themselves, the reader understands at once through the notes, and the codex opens");
		helper.assertTrue(!earned(reader, ConcordanceProgress.advancementFor(FIRST_LIGHT, ResearchState.MASTERED)),
				"The reader has not mastered it: that takes their own practice");

		// Through the item's use: written sheets are read, at most once per half second.
		reader.setItemInHand(InteractionHand.MAIN_HAND, sheet.copy());
		RateGate.forget(reader.getUUID());
		helper.assertTrue(reader.getMainHandItem().getItem().use(level, reader, InteractionHand.MAIN_HAND) == InteractionResult.SUCCESS,
				"Using a written sheet reads it");
		helper.assertTrue(reader.getMainHandItem().getItem().use(level, reader, InteractionHand.MAIN_HAND) == InteractionResult.FAIL,
				"A second request at once is ignored");
		helper.assertTrue(reader.getMainHandItem().has(JugcraftConcordance.RESEARCH_NOTES), "Reading does not use the sheet up");
		helper.succeed();
	}

	private static ResearchState stolas(ServerPlayer player) {
		return ConcordanceProgress.knowledge(player).state(StolasRegard.RESEARCH);
	}

	/**
	 * Concordance Chunk D, D-03 (E1, E3): Stolas's Regard takes no notes. An author who has mastered it writes it down; a
	 * reader with the Binding Arts understood reads the sheet and gains nothing toward it, not even kept evidence. The reader
	 * earns each state only by their own examination, harvest and assay, and reading again after that adds nothing.
	 */
	@GameTest(maxTicks = 20)
	public void notesNeverStandInForStolasEvidence(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		Definitions.Research regard = ConcordanceData.rules().research(StolasRegard.RESEARCH);
		helper.assertTrue(regard != null && regard.states().values().stream().flatMap(List::stream)
				.noneMatch(rule -> rule.kind() == EvidenceRule.Kind.NOTES), "No state of Stolas's Regard takes notes: " + regard);
		ServerPlayer author = player(helper, new BlockPos(1, 2, 1));
		ServerPlayer reader = player(helper, new BlockPos(3, 2, 1));
		for (ServerPlayer each : List.of(author, reader)) {
			ConcordanceProgress.grant(each, FIRST_LIGHT, ResearchState.UNDERSTOOD);
			ConcordanceProgress.grant(each, Workers.RESEARCH, ResearchState.UNDERSTOOD);
		}
		ConcordanceProgress.grant(author, StolasRegard.RESEARCH, ResearchState.MASTERED);
		author.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(JugcraftConcordance.RESEARCH_NOTES_ITEM));
		ResearchNotesItem.write(author, InteractionHand.MAIN_HAND, author.getMainHandItem());
		ResearchNotes notes = writtenNotes(author).get(JugcraftConcordance.RESEARCH_NOTES);
		helper.assertTrue(notes != null && notes.entries().get(StolasRegard.RESEARCH) == ResearchState.UNDERSTOOD,
				"The author's notes name Stolas's Regard, understood: " + notes);
		ResearchNotesItem.read(reader, notes);
		Knowledge.Progress read = ConcordanceProgress.knowledge(reader).progress(StolasRegard.RESEARCH);
		helper.assertTrue(read.state() == ResearchState.NONE && read.evidence().isEmpty(),
				"Read, the notes give Stolas's Regard nothing, not even kept evidence: " + read);
		Examination.forget(reader.getUUID());
		reader.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FEATHER));
		Examination.examine(reader, level, reader.getMainHandItem());
		helper.assertValueEqual(stolas(reader), ResearchState.ENCOUNTERED, "Their own examination of a feather begins it");
		ConcordanceProgress.record(reader, new Evidence.Practiced(Garden.ACTIVITY, "jugcraft:sunpetal"));
		ConcordanceProgress.record(reader, new Evidence.Practiced(Assaying.ACTIVITY, "minecraft:raw_iron"));
		helper.assertValueEqual(stolas(reader), ResearchState.UNDERSTOOD, "Their own harvest and assay understand it");
		Knowledge.Progress earned = ConcordanceProgress.knowledge(reader).progress(StolasRegard.RESEARCH);
		ResearchNotesItem.read(reader, notes);
		helper.assertTrue(ConcordanceProgress.knowledge(reader).progress(StolasRegard.RESEARCH).equals(earned)
				&& earned.evidence().keySet().stream().noneMatch(key -> key.startsWith("notes:")), "Reading again adds nothing: " + earned);
		helper.succeed();
	}

	/**
	 * The sconce: anyone may pour Radiance in (what fits, from a lantern), only its owner may draw it out, at most 32
	 * moves through it per second, it lights while it holds any, it burns one measure a minute, and broken it keeps
	 * its Radiance on the item.
	 */
	@GameTest(maxTicks = 20)
	public void sconceTakesRadianceByTheRules(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		for (int x = 0; x <= 7; x++) {
			for (int z = 0; z <= 7; z++) {
				helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
			}
		}
		ServerPlayer owner = player(helper, new BlockPos(2, 2, 1));
		ServerPlayer stranger = player(helper, new BlockPos(4, 2, 1));
		BlockPos relative = new BlockPos(3, 2, 3);
		helper.setBlock(relative, JugcraftConcordance.LUMEN_SCONCE);
		BlockPos pos = helper.absolutePos(relative);
		JugcraftConcordance.LUMEN_SCONCE.setPlacedBy(level, pos, level.getBlockState(pos), owner, ItemStack.EMPTY);
		LumenSconceBlockEntity sconce = helper.getBlockEntity(relative, LumenSconceBlockEntity.class);
		long now = ConcordanceProgress.now(level);
		helper.assertTrue(sconce.owner() != null && sconce.owner().equals(owner.getUUID()) && sconce.remaining(now) == 0
				&& !level.getBlockState(pos).getValue(LumenSconceBlock.LIT), "Placed, it belongs to its placer and is dark");

		ItemStack strangerLantern = new ItemStack(JugcraftConcordance.KINDLED_LANTERN);
		KindledLanternItem.set(strangerLantern, 40, now, false);
		LumenSconceBlock.exchange(level, pos, stranger, strangerLantern, false);
		helper.assertTrue(sconce.remaining(now) == LumenSconceBlockEntity.POUR && KindledLanternItem.remaining(strangerLantern, now) == 24
				&& level.getBlockState(pos).getValue(LumenSconceBlock.LIT) && level.getBlockState(pos).getLightEmission() == 15,
				"Anyone may pour: 16 moves from the lantern and the sconce lights at 15");
		helper.assertTrue(LumenSconceBlock.exchange(level, pos, stranger, strangerLantern, false) == InteractionResult.FAIL
				&& sconce.remaining(now) == LumenSconceBlockEntity.POUR, "A second pour at once is ignored");

		Transfers.Transfer theft = sconce.draw(level, stranger.getUUID(), strangerLantern);
		helper.assertTrue(theft.outcome() == Transfers.Outcome.NOT_PERMITTED && sconce.remaining(now) == LumenSconceBlockEntity.POUR,
				"A stranger may not draw from it");
		ItemStack ownerLantern = new ItemStack(JugcraftConcordance.KINDLED_LANTERN);
		KindledLanternItem.set(ownerLantern, 60, now, false);
		Transfers.Transfer drawn = sconce.draw(level, owner.getUUID(), ownerLantern);
		helper.assertTrue(drawn.outcome() == Transfers.Outcome.PARTIAL && drawn.received() == 4
				&& KindledLanternItem.remaining(ownerLantern, now) == KindledLanternItem.CAPACITY && sconce.remaining(now) == 12,
				"The owner draws what their lantern has room for; the rest stays: " + drawn);
		Transfers.Transfer pour = sconce.pour(level, stranger.getUUID(), strangerLantern);
		helper.assertTrue(pour.received() == 12 && sconce.remaining(now) == 24, "The window's budget (32) allows 12 more: " + pour);
		helper.assertTrue(sconce.pour(level, stranger.getUUID(), strangerLantern).outcome() == Transfers.Outcome.RATE_LIMITED,
				"and then nothing until the next window");
		helper.assertTrue(sconce.remaining(now + 3L * LumenSconceBlockEntity.BURN_TICKS) == 21, "It burns one measure a minute");
		long total = sconce.remaining(now) + KindledLanternItem.remaining(strangerLantern, now) + KindledLanternItem.remaining(ownerLantern, now);
		helper.assertTrue(total == 40 + 60, "Every transfer conserved the Radiance: " + total);

		if (FabricLoader.getInstance().isModLoaded("jade")) {
			helper.assertTrue(ConcordanceDataProvider.snapshot(sconce, now).getIntOr("radiance", -1) == 24,
					"Jade sees the sconce's Radiance");
		}

		// Broken and placed again, it keeps what it held.
		LanternCharge kept = sconce.collectComponents().get(JugcraftConcordance.RADIANCE);
		helper.assertTrue(kept != null && kept.stored() == 24, "Broken, the sconce keeps its Radiance on the item: " + kept);
		ItemStack item = new ItemStack(JugcraftConcordance.LUMEN_SCONCE);
		item.set(JugcraftConcordance.RADIANCE, kept);
		BlockPos second = new BlockPos(5, 2, 5);
		helper.setBlock(second, JugcraftConcordance.LUMEN_SCONCE);
		LumenSconceBlockEntity again = helper.getBlockEntity(second, LumenSconceBlockEntity.class);
		again.applyComponentsFromItemStack(item);
		again.refresh(level, now);
		helper.assertTrue(again.remaining(now) == 24 && level.getBlockState(helper.absolutePos(second)).getValue(LumenSconceBlock.LIT),
				"Placed again, it burns what it kept");
		helper.succeed();
	}

	// ---------------------------------------------------------------- Concordance Chunk D, D-02: a copied anchor

	private static WorkerDefinition.Spirit terms() {
		return Workers.catalog().get(GatheringShadeEntity.DEFINITION, WorkerDefinition.Spirit.class);
	}

	private static String key(@Nullable Component line) {
		return line != null && line.getContents() instanceof TranslatableContents contents ? contents.getKey() : "";
	}

	private static int wool(GameTestHelper helper, AABB box) {
		return helper.getLevel().getEntitiesOfClass(ItemEntity.class, box, item -> item.isAlive() && item.getItem().is(Items.WOOL.white()))
				.stream().mapToInt(item -> item.getItem().getCount()).sum();
	}

	private static int escrowed(ServerLevel level, UUID record) {
		return BoundWills.of(level.getServer()).escrow(record).stream().mapToInt(ItemStack::getCount).sum();
	}

	private static SpiritAnchorBlockEntity loaded(GameTestHelper helper, BlockPos at, CompoundTag saved) {
		helper.setBlock(at, Workers.SPIRIT_ANCHOR);
		SpiritAnchorBlockEntity anchor = helper.getBlockEntity(at, SpiritAnchorBlockEntity.class);
		anchor.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), saved));
		return anchor;
	}

	/**
	 * D-02 (E3, 10:52, D-S9): an anchor's data copied to a second place is inert: it says another agreement covers it and
	 * suspends, ends, takes and pays nothing; broken, it releases nothing; the original broken releases once, its escrow
	 * dropped where it stood, the departure kept.
	 */
	@GameTest(maxTicks = 20)
	public void aCopiedAnchorGivesNothingAndReleasesNothing(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = player(helper, new BlockPos(4, 1, 4));
		BlockPos originalAt = new BlockPos(2, 1, 2);
		BlockPos copyAt = new BlockPos(6, 1, 2);
		helper.setBlock(originalAt, Workers.SPIRIT_ANCHOR);
		helper.setBlock(copyAt, Workers.SPIRIT_ANCHOR);
		SpiritAnchorBlockEntity original = helper.getBlockEntity(originalAt, SpiritAnchorBlockEntity.class);
		SpiritAnchorBlockEntity copy = helper.getBlockEntity(copyAt, SpiritAnchorBlockEntity.class);
		long now = level.getGameTime() + 1_000_000L;
		helper.assertValueEqual(original.holder().seal(level, holder, original.sealing(terms(), UPKEEP), now).outcome(), Pacts.Outcome.DONE, "Sealed with upkeep");
		BoundWills wills = BoundWills.of(level.getServer());
		UUID record = original.agreement().record();
		helper.assertValueEqual(original.holder().deposit(level, holder, new ItemStack(Items.WOOL.white(), 2), now), 2, "two periods in its escrow");
		GatheringShadeEntity shade = (GatheringShadeEntity) level.getEntity(original.spirit());
		copy.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), original.saveWithoutMetadata(level.registryAccess())));
		BoundWill before = wills.ledger().get(record);
		helper.assertTrue(copy.holder().verify(level, now) == AgreementHolder.Standing.ELSEWHERE && original.holder().verify(level, now) == AgreementHolder.Standing.VALID,
				"Copied to a second place, the copy is not the record's; the original is");
		helper.assertValueEqual(key(SpiritAnchorBlock.use(holder, level, copy, false)), "compose.jugcraft.diagnostic.conflicting_agreement",
				"The copy says another agreement covers it");
		SpiritAnchorBlock.use(holder, level, copy, true);
		helper.assertTrue(copy.holder().end(level, holder, now) == AgreementHolder.Ended.CONFLICTING
				&& copy.holder().toggle(level, holder, now) == AgreementHolder.Toggle.CONFLICTING
				&& copy.holder().look(level, now + 7 * Agreement.DAY) == Pacts.Event.NONE
				&& copy.holder().deposit(level, holder, new ItemStack(Items.WOOL.white(), 1), now) == 0
				&& wills.ledger().get(record).equals(before) && escrowed(level, record) == 2, "It suspends, ends, takes and pays nothing");
		helper.setBlock(copyAt, Blocks.AIR);
		helper.assertTrue(wills.ledger().get(record) != null && !shade.isRemoved()
				&& WorkerRoster.of(level.getServer()).workers(holder.getUUID()).containsKey(shade.getUUID()) && escrowed(level, record) == 2,
				"Broken, the copy releases nothing: the record, its escrow, the shade and its roster place stay");
		helper.assertTrue(original.holder().verify(level, now) == AgreementHolder.Standing.VALID && shade.think(level, 18000L, now) != Status.FINISHED
				&& wills.problems().isEmpty(), "The original keeps its agreement and its shade");
		helper.setBlock(originalAt, Blocks.AIR);
		List<Pacts.Departure> history = PactHistory.of(level.getServer()).history(holder.getUUID());
		helper.assertTrue(wills.ledger().get(record) == null && shade.isRemoved() && wool(helper, new AABB(helper.absolutePos(originalAt)).inflate(1.5)) == 2
				&& !history.isEmpty() && record.equals(history.getLast().record()) && Pacts.BROKEN.equals(history.getLast().reason()),
				"The original broken releases it once: its escrow drops where it stood, and the departure is kept");
		helper.succeed();
	}

	/**
	 * D-02 (E3, D-S9, D-P3): a copy of a pre-Chunk-D anchor, though it loads first, never claims its record; the anchor its
	 * terms were sealed at claims it once; one whose spirit is seated elsewhere does not.
	 */
	@GameTest(maxTicks = 20)
	public void aLegacyAnchorClaimsItsPlaceOnce(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BoundWills wills = BoundWills.of(level.getServer());
		UUID holder = UUID.randomUUID();
		UUID record = UUID.randomUUID();
		UUID spirit = UUID.randomUUID();
		wills.seal(new BoundWill(record, GatheringShadeEntity.DEFINITION, "gathering_shade", holder, 5L, false));
		BlockPos originalAt = new BlockPos(1, 1, 1);
		BlockPos copyAt = new BlockPos(5, 1, 1);
		CompoundTag older = ConcordancePersistenceGameTests.olderAnchor(level, record, holder, helper.absolutePos(originalAt), spirit, false);
		SpiritAnchorBlockEntity copy = loaded(helper, copyAt, older);
		SpiritAnchorBlockEntity original = loaded(helper, originalAt, older);
		long now = level.getGameTime();
		helper.assertTrue(copy.holder().verify(level, now) == AgreementHolder.Standing.ELSEWHERE && Pacts.unclaimed(wills.ledger().get(record)),
				"A copy from before Chunk D, though it loads first, never claims: its terms were sealed elsewhere");
		helper.assertValueEqual(original.holder().verify(level, now), AgreementHolder.Standing.VALID, "The anchor its terms were sealed at claims it");
		BoundWill claimed = wills.ledger().get(record);
		helper.assertTrue(claimed.pact() != null && Couriers.place(level, helper.absolutePos(originalAt)).equals(claimed.pact().place())
				&& spirit.equals(claimed.pact().presence()) && claimed.pact().hold() == BoundWill.Hold.NONE && "gathering_shade".equals(claimed.counterpart()),
				"once, writing its place and presence; its counterpart as written");
		helper.assertTrue(original.holder().verify(level, now + 20) == AgreementHolder.Standing.VALID && wills.ledger().get(record).equals(claimed)
				&& copy.holder().verify(level, now + 20) == AgreementHolder.Standing.ELSEWHERE, "Again, nothing changes; the copy stays inert");
		helper.setBlock(copyAt, Blocks.AIR);
		helper.assertTrue(wills.ledger().get(record) != null, "Broken, the copy releases nothing");
		UUID second = UUID.randomUUID();
		UUID seated = UUID.randomUUID();
		wills.seal(new BoundWill(second, GatheringShadeEntity.DEFINITION, "gathering_shade", holder, 6L, false));
		BlockPos thirdAt = new BlockPos(1, 1, 5);
		SpiritAnchorBlockEntity third = loaded(helper, thirdAt,
				ConcordancePersistenceGameTests.olderAnchor(level, second, holder, helper.absolutePos(thirdAt), seated, false));
		GatheringShadeEntity elsewhere = Workers.GATHERING_SHADE.create(level, EntitySpawnReason.LOAD);
		elsewhere.setUUID(seated);
		elsewhere.setOwner(holder);
		elsewhere.setAnchor(helper.absolutePos(new BlockPos(5, 1, 5)));
		Vec3 standing = helper.absoluteVec(new Vec3(5.5, 2.0, 5.5));
		elsewhere.snapTo(standing.x, standing.y, standing.z, 0.0F, 0.0F);
		level.addFreshEntity(elsewhere);
		helper.assertTrue(third.holder().verify(level, now) == AgreementHolder.Standing.ELSEWHERE && Pacts.unclaimed(wills.ledger().get(second)),
				"An anchor whose spirit is seated at another place does not claim");
		elsewhere.discard();
		wills.release(record, holder);
		wills.release(second, holder);
		helper.succeed();
	}

	/**
	 * D-02 (E4, 10:52, D-S6): ended, the escrow comes back once (what fits into the inventory, the rest at the holder's feet);
	 * the record, its escrow and the shade go; one departure kept; a copy forgets its terms; ending again or breaking the copy
	 * returns nothing more.
	 */
	@GameTest(maxTicks = 20)
	public void endingTwiceReturnsOnce(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = player(helper, new BlockPos(4, 1, 4));
		BlockPos at = new BlockPos(2, 1, 2);
		BlockPos copyAt = new BlockPos(6, 1, 2);
		helper.setBlock(at, Workers.SPIRIT_ANCHOR);
		helper.setBlock(copyAt, Workers.SPIRIT_ANCHOR);
		SpiritAnchorBlockEntity anchor = helper.getBlockEntity(at, SpiritAnchorBlockEntity.class);
		SpiritAnchorBlockEntity copy = helper.getBlockEntity(copyAt, SpiritAnchorBlockEntity.class);
		long now = level.getGameTime() + 1_000_000L;
		helper.assertValueEqual(anchor.holder().seal(level, holder, anchor.sealing(terms(), UPKEEP), now).outcome(), Pacts.Outcome.DONE, "Sealed with upkeep");
		UUID record = anchor.agreement().record();
		GatheringShadeEntity shade = (GatheringShadeEntity) level.getEntity(anchor.spirit());
		helper.assertValueEqual(anchor.holder().deposit(level, holder, new ItemStack(Items.WOOL.white(), 3), now), 3, "three periods in its escrow");
		copy.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), anchor.saveWithoutMetadata(level.registryAccess())));
		Inventory inventory = holder.getInventory();
		for (int slot = 0; slot < 36; slot++) {
			inventory.setItem(slot, slot == 0 ? new ItemStack(Items.WOOL.white(), 63) : new ItemStack(Items.STONE, 64));
		}
		helper.assertValueEqual(anchor.holder().end(level, holder, now), AgreementHolder.Ended.ENDED, "Its holder ends it");
		AABB feet = holder.getBoundingBox().inflate(3.0);
		helper.assertTrue(inventory.countItem(Items.WOOL.white()) == 64 && wool(helper, feet) == 2,
				"the escrow comes back once: what fits into the inventory, the rest at their feet");
		BoundWills wills = BoundWills.of(level.getServer());
		helper.assertTrue(wills.ledger().get(record) == null && wills.escrow(record).isEmpty() && shade.isRemoved()
				&& !WorkerRoster.of(level.getServer()).workers(holder.getUUID()).containsKey(shade.getUUID()) && anchor.agreement() == null,
				"the record and its escrow go, the shade departs and is struck off");
		List<Pacts.Departure> history = PactHistory.of(level.getServer()).history(holder.getUUID());
		helper.assertTrue(history.size() == 1 && history.getFirst().record().equals(record) && Pacts.ENDED.equals(history.getFirst().reason()),
				"and the departure is kept once: " + history);
		helper.assertTrue(copy.holder().verify(level, now) == AgreementHolder.Standing.NONE && copy.agreement() == null, "The copy, its record gone, forgets its terms");
		helper.setBlock(copyAt, Blocks.AIR);
		helper.assertValueEqual(AgreementHolder.end(level, null, holder, record, Pacts.ENDED, now + 20), AgreementHolder.Ended.NOTHING, "Ending again finds nothing");
		helper.assertTrue(inventory.countItem(Items.WOOL.white()) == 64 && wool(helper, feet) == 2 && PactHistory.of(level.getServer()).history(holder.getUUID()).size() == 1,
				"and nothing more comes back, by the copy's breaking or the second end");
		helper.succeed();
	}

	/** D-04 (E3, 10:52, D-S9): a perch's data copied to a second place is inert: its screen reads CONFLICTING_AGREEMENT; accepting, depositing, suspending, ending, seating and recalling there change nothing; its upkeep looks at nothing; no visitor comes; breaking it releases and unseats nothing; the original keeps its pact. */
	@GameTest(maxTicks = 20)
	public void aCopiedPerchIsInert(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer holder = ConcordanceWorkerGameTests.ready(level, player(helper, new BlockPos(4, 1, 4)));
		ConcordanceWorkerGameTests.pocket(holder, 1, AstrolabeItem.CAPACITY); // a survival player pays the Open Hand Compact
		BlockPos originalAt = new BlockPos(2, 1, 2);
		BlockPos copyAt = new BlockPos(6, 1, 2);
		PactPerchBlockEntity original = ConcordanceWorkerGameTests.perch(helper, originalAt, holder);
		PactMenu sealing = ConcordanceWorkerGameTests.open(helper, holder, original);
		helper.assertTrue(ConcordanceWorkerGameTests.answered(ConcordanceWorkerGameTests.send(holder, sealing, PactRequestPayload.ACCEPT, 0), PactMenu.SEALED),
				"Sealed at the original");
		holder.closeContainer();
		long now = level.getGameTime();
		BoundWill sealed = ConcordanceWorkerGameTests.recordOf(level, holder);
		helper.assertValueEqual(original.holder().deposit(level, holder, new ItemStack(Items.AMETHYST_SHARD, 2), now), 2, "two offerings kept");
		BoundWill record = BoundWills.of(level.getServer()).ledger().get(sealed.id());
		StolasEntity presence = (StolasEntity) level.getEntity(record.pact().presence());
		helper.setBlock(copyAt, Workers.PACT_PERCH);
		PactPerchBlockEntity copy = helper.getBlockEntity(copyAt, PactPerchBlockEntity.class);
		copy.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), original.saveWithoutMetadata(level.registryAccess())));
		helper.assertTrue(copy.holder().verify(level, now) == AgreementHolder.Standing.ELSEWHERE && original.holder().verify(level, now) == AgreementHolder.Standing.VALID,
				"The copy is not the record's; the original is");
		PactMenu menu = ConcordanceWorkerGameTests.open(helper, holder, copy);
		helper.assertTrue(menu.lastSent().mode() == PactStatePayload.CLOSED
				&& ConcordanceWorkerGameTests.keys(menu.lastSent().notes()).contains("compose.jugcraft.diagnostic.conflicting_agreement")
				&& copy.visitor() == null && ConcordanceWorkerGameTests.stolases(helper).size() == 1, "Its screen says another agreement covers it; no visitor");
		for (int action : List.of(PactRequestPayload.ACCEPT, PactRequestPayload.DEPOSIT, PactRequestPayload.SUSPEND, PactRequestPayload.RESUME,
				PactRequestPayload.END, PactRequestPayload.SEAT, PactRequestPayload.RECALL)) {
			PactStatePayload answer = ConcordanceWorkerGameTests.send(holder, menu, action, 0);
			helper.assertTrue(answer != null && answer.result().isPresent() && !answer.result().get().done()
					&& record.equals(BoundWills.of(level.getServer()).ledger().get(record.id()))
					&& ConcordanceWorkerGameTests.escrowed(level, record.id()) == 2, "At the copy, action " + action + " changes nothing: " + answer);
		}
		helper.assertTrue(copy.holder().look(level, now + 7 * Agreement.DAY) == Pacts.Event.NONE, "Its upkeep looks at nothing");
		holder.closeContainer();
		helper.setBlock(copyAt, Blocks.AIR);
		helper.assertTrue(record.equals(BoundWills.of(level.getServer()).ledger().get(record.id())) && !presence.gone()
				&& WorkerRoster.of(level.getServer()).workers(holder.getUUID()).containsKey(presence.getUUID())
				&& ConcordanceWorkerGameTests.escrowed(level, record.id()) == 2, "Broken, the copy releases and unseats nothing");
		helper.assertTrue(original.holder().verify(level, now) == AgreementHolder.Standing.VALID && presence.think(level, 0L, now) != Status.FINISHED
				&& BoundWills.of(level.getServer()).problems().isEmpty(), "The original keeps its pact and its Stolas");
		helper.succeed();
	}

	/** D-05 (E3, 10:52): a perch's data copied to a second place recalls nothing: it spends, claims, pays, looks, suspends, ends, seats and calls nothing; broken, it releases nothing; the original still serves. */
	@GameTest(maxTicks = 20)
	public void aCopiedPerchRecallsNothing(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = ConcordanceWorkerGameTests.regarded(helper);
		long g0 = ConcordanceWorkerGameTests.dayStart(level, 100);
		BlockPos originalAt = new BlockPos(2, 1, 2);
		BlockPos copyAt = new BlockPos(6, 1, 2);
		PactPerchBlockEntity original = ConcordanceWorkerGameTests.stolasPact(helper, keeper, originalAt, g0);
		UUID record = original.holder().agreement().record();
		ConcordanceWorkerGameTests.observed(level, keeper, ConcordanceWorkerGameTests.FULL_MOON);
		ItemStack held = ConcordanceWorkerGameTests.holding(keeper, AstrolabeItem.CAPACITY);
		ConcordanceWorkerGameTests.give(keeper, new ItemStack(Items.AMETHYST_SHARD, 2));
		helper.assertValueEqual(PactKeeping.deposit(level, keeper, original.holder(), Items.AMETHYST_SHARD, g0), 2, "Two paid ahead");
		helper.setBlock(copyAt, Workers.PACT_PERCH);
		PactPerchBlockEntity copy = helper.getBlockEntity(copyAt, PactPerchBlockEntity.class);
		copy.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), original.saveWithoutMetadata(level.registryAccess())));
		BoundWills wills = BoundWills.of(level.getServer());
		BoundWill before = wills.ledger().get(record);
		ConcordanceWorkerGameTests.beside(helper, keeper, copyAt);
		helper.assertTrue(copy.holder().verify(level, g0) == AgreementHolder.Standing.ELSEWHERE, "The copy is not the record's");
		helper.assertValueEqual(StarReckoning.reckon(level, keeper, copy.holder(), ConcordanceWorkerGameTests.FULL_MOON, 24006L, g0), Pacts.Reckoning.CONFLICTING,
				"At the copy, Stolas reckons nothing");
		ConcordanceWorkerGameTests.give(keeper, new ItemStack(Items.AMETHYST_SHARD, 1));
		helper.assertTrue(AstrolabeItem.charge(held) == AstrolabeItem.CAPACITY && !ConcordanceWorkerGameTests.recalledEver(level, keeper, ConcordanceWorkerGameTests.FULL_MOON)
				&& PactKeeping.deposit(level, keeper, copy.holder(), Items.AMETHYST_SHARD, g0) == 0 && ConcordanceWorkerGameTests.shards(keeper) == 1
				&& copy.holder().look(level, g0 + 7 * 24000L) == Pacts.Event.NONE && copy.holder().end(level, keeper, g0) == AgreementHolder.Ended.CONFLICTING
				&& PactKeeping.suspend(level, keeper, copy.holder(), g0) == AgreementHolder.Toggle.CONFLICTING
				&& copy.holder().seat(level, keeper, StolasEntity.COUNTERPART, g0) == AgreementHolder.Seat.NOTHING
				&& PactKeeping.page(level, keeper, copy.holder(), g0) == PactKeeping.Page.NONE
				&& wills.ledger().get(record).equals(before) && ConcordanceWorkerGameTests.escrowed(level, record) == 2,
				"nothing is spent, claimed, paid, held, ended, seated or shown through it");
		ConcordanceWorkerGameTests.stolas(level, original).remove(Entity.RemovalReason.KILLED);
		PactKeeping.tick(level, copy.holder(), copy.getBlockPos(), g0 + 20);
		helper.assertTrue(ConcordanceWorkerGameTests.stolas(level, original) == null && original.holder().spiritLost(level), "The copy's ticker calls nobody");
		PactKeeping.tick(level, original.holder(), original.getBlockPos(), g0 + 40);
		helper.assertTrue(ConcordanceWorkerGameTests.stolas(level, original) != null, "the original's calls Stolas back");
		helper.setBlock(copyAt, Blocks.AIR);
		helper.assertTrue(Pacts.seated(wills.ledger().get(record)) && ConcordanceWorkerGameTests.escrowed(level, record) == 2, "Broken, the copy releases nothing");
		ConcordanceWorkerGameTests.beside(helper, keeper, originalAt);
		helper.assertTrue(StarReckoning.reckon(level, keeper, original.holder(), ConcordanceWorkerGameTests.FULL_MOON, 24006L, g0) == Pacts.Reckoning.RECALLED
				&& wills.problems().isEmpty(), "The original still serves");
		helper.succeed();
	}

	/** D-05 (E1, D-S8): the holder's party, a stranger and a stand-in with the holder's identity cannot use, pay, suspend or end the holder's pact, and another's observation keeps no obligation of it. */
	@GameTest(maxTicks = 20)
	public void anotherPlayerCannotUseTheHoldersService(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer keeper = ConcordanceWorkerGameTests.regarded(helper);
		ServerPlayer friend = ConcordanceWorkerGameTests.regarded(helper);
		ServerPlayer stranger = ConcordanceWorkerGameTests.regarded(helper);
		BlockPos at = new BlockPos(2, 1, 2);
		PartyManager parties = JugcraftParties.manager();
		long ms = System.currentTimeMillis();
		try {
			helper.assertTrue(parties.invite(keeper.getUUID(), friend.getUUID(), ms) == PartyManager.Result.OK
					&& parties.accept(friend.getUUID(), ms) == PartyManager.Result.OK, "The holder and a friend form a party");
			long g0 = ConcordanceWorkerGameTests.dayStart(level, 100);
			PactPerchBlockEntity perch = ConcordanceWorkerGameTests.stolasPact(helper, keeper, at, g0);
			String full = ConcordanceWorkerGameTests.FULL_MOON;
			for (ServerPlayer player : List.of(keeper, friend, stranger)) {
				ConcordanceWorkerGameTests.observed(level, player, full);
				ConcordanceWorkerGameTests.holding(player, AstrolabeItem.CAPACITY);
			}
			ConcordanceWorkerGameTests.beside(helper, friend, at);
			ConcordanceWorkerGameTests.beside(helper, stranger, at);
			FakePlayer standIn = FakePlayer.get(level, new GameProfile(keeper.getUUID(), "[Concordance]"));
			helper.assertTrue(StarReckoning.reckon(level, friend, perch.holder(), full, 24006L, g0) == Pacts.Reckoning.NOT_HOLDER
					&& StarReckoning.reckon(level, stranger, perch.holder(), full, 24006L, g0) == Pacts.Reckoning.NOT_HOLDER
					&& StarReckoning.reckon(level, standIn, perch.holder(), full, 24006L, g0) == Pacts.Reckoning.NOT_HOLDER,
					"Its holder's party, a stranger and a stand-in are refused");
			helper.assertTrue(AstrolabeItem.charge(friend.getMainHandItem()) == AstrolabeItem.CAPACITY && !ConcordanceWorkerGameTests.recalledEver(level, friend, full)
					&& ConcordanceWorkerGameTests.done(level, perch, g0) == 0, "spending, claiming and counting nothing");
			ConcordanceWorkerGameTests.give(friend, new ItemStack(Items.AMETHYST_SHARD, 2));
			UUID record = perch.holder().agreement().record();
			helper.assertTrue(PactKeeping.deposit(level, friend, perch.holder(), Items.AMETHYST_SHARD, g0) == 0 && ConcordanceWorkerGameTests.shards(friend) == 2
					&& PactKeeping.suspend(level, friend, perch.holder(), g0) == AgreementHolder.Toggle.NOT_HOLDER
					&& perch.holder().end(level, friend, g0) == AgreementHolder.Ended.NOT_HOLDER
					&& ConcordanceWorkerGameTests.key(PactKeeping.end(level, friend, perch.holder(), StolasEntity.COUNTERPART, g0).said())
							.equals("message.jugcraft.concordance.workers.not_holder")
					&& PactKeeping.page(level, friend, perch.holder(), g0) == PactKeeping.Page.NONE
					&& !Pacts.benefits(BoundWills.of(level.getServer()).ledger().get(record), friend.getUUID(), true), "nor pay, suspend, end or read it; it benefits its holder alone");
			ObservatoryBlockEntity observatory = ConcordanceWorkerGameTests.observatory(helper, new BlockPos(0, 1, 6));
			helper.assertTrue(observatory.observe(friend, level, ConcordanceWorkerGameTests.NIGHT_ZERO, g0 + 20, ConcordanceWorkerGameTests.ONLY_MOON) == 1
					&& ConcordanceWorkerGameTests.regard(friend) == ResearchState.UNDERSTOOD && ConcordanceWorkerGameTests.regard(keeper) == ResearchState.UNDERSTOOD,
					"Another's observation keeps no one's obligation");
			ConcordanceWorkerGameTests.beside(helper, keeper, at);
			helper.assertValueEqual(StarReckoning.reckon(level, keeper, perch.holder(), full, 24006L, g0), Pacts.Reckoning.RECALLED, "Its holder is served");
		} finally {
			parties.adminDisband(keeper.getUUID());
		}
		helper.succeed();
	}

	private static ItemStack sheet(ResearchNotes notes) {
		ItemStack stack = new ItemStack(JugcraftConcordance.RESEARCH_NOTES_ITEM);
		stack.set(JugcraftConcordance.RESEARCH_NOTES, notes);
		return stack;
	}

	/** A three-by-three crafting grid holding copies of these stacks, the rest empty. */
	private static CraftingInput grid(ItemStack... stacks) {
		List<ItemStack> items = new ArrayList<>();
		for (ItemStack stack : stacks) {
			items.add(stack.copy());
		}
		while (items.size() < 9) {
			items.add(ItemStack.EMPTY);
		}
		return CraftingInput.of(3, 3, items);
	}

	/**
	 * Gloam Ink (C-D6): a fair copy teaches exactly what its original does. An author who understood First Light writes a
	 * sheet; the recipe the crafting table would find makes from it, a blank sheet and Gloam Ink one copy with the same
	 * author, entries and time written, marked a copy, and gives back the original and the ink's bottle. A reader of the copy
	 * keeps the author's notes as evidence and still has no state until they observe; the original then adds nothing, nor
	 * does the copy to a reader of the original; each holds one notes key, the author's; the copy's reader, observing,
	 * understands; and the author learns nothing from a copy of their own notes.
	 */
	@GameTest(maxTicks = 20)
	public void aFairCopyTeachesWhatItsOriginalDoes(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer author = player(helper, new BlockPos(1, 2, 1));
		ServerPlayer first = player(helper, new BlockPos(3, 2, 1));
		ServerPlayer second = player(helper, new BlockPos(5, 2, 1));
		ConcordanceProgress.grant(author, FIRST_LIGHT, ResearchState.UNDERSTOOD);
		author.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(JugcraftConcordance.RESEARCH_NOTES_ITEM));
		ResearchNotesItem.write(author, InteractionHand.MAIN_HAND, author.getMainHandItem());
		ItemStack original = writtenNotes(author).copy();
		ResearchNotes notes = original.get(JugcraftConcordance.RESEARCH_NOTES);
		helper.assertTrue(notes != null && !notes.fairCopy(), "A sheet written by hand is an original: " + notes);

		CraftingInput input = grid(original, new ItemStack(JugcraftConcordance.RESEARCH_NOTES_ITEM),
				new ItemStack(JugcraftConcordance.GLOAM_INK));
		Optional<RecipeHolder<CraftingRecipe>> recipe = level.recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, level);
		helper.assertTrue(recipe.isPresent() && recipe.get().value() instanceof ResearchNotesCopyRecipe
				&& recipe.get().id().equals(ResourceKey.create(Registries.RECIPE, Jugcraft.id("research_notes_copy"))),
				"An original, a blank sheet and Gloam Ink find the fair copy recipe");
		ItemStack made = recipe.get().value().assemble(input);
		ResearchNotes copy = made.get(JugcraftConcordance.RESEARCH_NOTES);
		helper.assertTrue(made.is(JugcraftConcordance.RESEARCH_NOTES_ITEM) && made.getCount() == 1 && copy != null && copy.fairCopy()
				&& copy.author().equals(notes.author()) && copy.authorName().equals(notes.authorName())
				&& copy.entries().equals(notes.entries()) && copy.written() == notes.written(),
				"One copy: the same author, entries and time written, marked a copy: " + copy);
		NonNullList<ItemStack> left = recipe.get().value().getRemainingItems(input);
		helper.assertTrue(ItemStack.isSameItemSameComponents(left.get(0), original) && left.get(0).getCount() == 1
				&& left.get(1).isEmpty() && left.get(2).is(Items.GLASS_BOTTLE) && left.get(2).getCount() == 1,
				"The original stays, the blank is used and the ink's bottle comes back: " + left);

		ResearchNotesItem.read(first, copy);
		Knowledge.Progress read = ConcordanceProgress.knowledge(first).progress(FIRST_LIGHT);
		helper.assertTrue(read.state() == ResearchState.NONE && read.evidence().containsKey("notes:" + author.getUUID()),
				"The copy is kept as the author's notes, with no state until the reader observes: " + read);
		Knowledge afterCopy = ConcordanceProgress.knowledge(first);
		ResearchNotesItem.read(first, notes);
		helper.assertTrue(ConcordanceProgress.knowledge(first).equals(afterCopy), "The original then adds nothing");
		ResearchNotesItem.read(second, notes);
		Knowledge afterOriginal = ConcordanceProgress.knowledge(second);
		ResearchNotesItem.read(second, copy);
		helper.assertTrue(ConcordanceProgress.knowledge(second).equals(afterOriginal), "Nor does the copy after the original");
		for (ServerPlayer reader : List.of(first, second)) {
			long keys = ConcordanceProgress.knowledge(reader).progress(FIRST_LIGHT).evidence().keySet().stream()
					.filter(key -> key.startsWith("notes:")).count();
			helper.assertTrue(keys == 1, "One notes key, the author's, whatever was read: " + keys);
		}
		ConcordanceProgress.record(first, new Evidence.Examined("minecraft:amethyst_shard", 0));
		helper.assertTrue(firstLight(first) == ResearchState.UNDERSTOOD,
				"Observing for themselves, the copy's reader understands, as the original's would");
		Knowledge authorBefore = ConcordanceProgress.knowledge(author);
		helper.assertTrue(ResearchNotesItem.read(author, copy) == InteractionResult.FAIL
				&& ConcordanceProgress.knowledge(author).equals(authorBefore), "An author learns nothing from a copy of their own notes");
		helper.succeed();
	}

	/**
	 * Gloam Ink (C-D6): the fair copy takes exactly one original written sheet, one blank sheet and one Gloam Ink, in any
	 * order, in the player's own two-by-two grid too. A fair copy is never copied; without the ink or the blank, with a
	 * second ink, blank or written sheet, with an ink sac or Gloam Sludge for the ink, or with anything else in the grid,
	 * nothing is made, by this recipe or by any the crafting table finds; with the Concordance switched off, nothing is
	 * copied; and the recipe loads.
	 */
	@GameTest(maxTicks = 20)
	public void onlyAnOriginalWithOneBlankAndOneInkIsCopied(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		helper.assertTrue(level.recipeAccess().byKey(ResourceKey.create(Registries.RECIPE, Jugcraft.id("research_notes_copy"))).isPresent(),
				"The fair copy recipe loads");
		ResearchNotes notes = new ResearchNotes(UUID.randomUUID(), "Scribe", Map.of(FIRST_LIGHT, ResearchState.UNDERSTOOD), 7L);
		ItemStack original = sheet(notes);
		ItemStack copy = sheet(notes.asFairCopy());
		ItemStack other = sheet(new ResearchNotes(UUID.randomUUID(), "Other", Map.of(FIRST_LIGHT, ResearchState.OBSERVED), 9L));
		ItemStack blank = new ItemStack(JugcraftConcordance.RESEARCH_NOTES_ITEM);
		ItemStack ink = new ItemStack(JugcraftConcordance.GLOAM_INK);
		ResearchNotesCopyRecipe recipe = ResearchNotesCopyRecipe.INSTANCE;
		helper.assertTrue(recipe.matches(grid(original, blank, ink), level), "An original, a blank and an ink are copied");
		helper.assertTrue(recipe.matches(CraftingInput.of(2, 2, List.of(ink.copy(), ItemStack.EMPTY, blank.copy(), original.copy())), level),
				"In any order, in the player's own two-by-two grid");
		Map<String, CraftingInput> refused = new LinkedHashMap<>();
		refused.put("a fair copy", grid(copy, blank, ink));
		refused.put("no ink", grid(original, blank));
		refused.put("no blank", grid(original, ink));
		refused.put("nothing written", grid(blank, blank, ink));
		refused.put("two inks", grid(original, blank, ink, ink));
		refused.put("two blanks", grid(original, blank, blank, ink));
		refused.put("two written sheets", grid(original, other, blank, ink));
		refused.put("an ink sac for the ink", grid(original, blank, new ItemStack(Items.INK_SAC)));
		refused.put("Gloam Sludge for the ink", grid(original, blank, new ItemStack(JugcraftConcordance.GLOAM_SLUDGE)));
		refused.put("something else too", grid(original, blank, ink, new ItemStack(Items.STICK)));
		for (Map.Entry<String, CraftingInput> each : refused.entrySet()) {
			helper.assertFalse(recipe.matches(each.getValue(), level) || !recipe.assemble(each.getValue()).isEmpty(),
					"Nothing is copied with " + each.getKey());
			helper.assertFalse(level.recipeAccess().getRecipeFor(RecipeType.CRAFTING, each.getValue(), level)
					.filter(holder -> holder.value() instanceof ResearchNotesCopyRecipe).isPresent(),
					"No crafting recipe copies with " + each.getKey());
		}
		// GI-P12: with the Concordance switched off, nothing is copied (the switch is toggled within this one body).
		boolean before = JugcraftConfig.isFeatureEnabled(JugcraftConcordance.FEATURE);
		try {
			JugcraftConfig.setFeatureEnabled(JugcraftConcordance.FEATURE, false);
			helper.assertFalse(recipe.matches(grid(original, blank, ink), level) || !recipe.assemble(grid(original, blank, ink)).isEmpty(),
					"With the Concordance switched off, nothing is copied");
		} finally {
			JugcraftConfig.setFeatureEnabled(JugcraftConcordance.FEATURE, before);
		}
		helper.succeed();
	}

	/**
	 * Gloam Ink (C-D6), through the server's own crafting: in a survival player's two-by-two grid, an original sheet, three
	 * blank sheets and three Gloam Inks show a fair copy in the result slot. Taking it leaves the original in its slot and
	 * uses one blank and one ink; shift-taking the rest makes one copy for each, the three copies stack, the original is
	 * still in the grid, and the three bottles come back (the last in the ink's emptied slot).
	 */
	@GameTest(maxTicks = 20)
	public void aFairCopyIsCraftedInThePlayersOwnGrid(GameTestHelper helper) {
		ServerPlayer author = player(helper, new BlockPos(1, 2, 1));
		ConcordanceProgress.grant(author, FIRST_LIGHT, ResearchState.UNDERSTOOD);
		author.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(JugcraftConcordance.RESEARCH_NOTES_ITEM));
		ResearchNotesItem.write(author, InteractionHand.MAIN_HAND, author.getMainHandItem());
		ItemStack original = writtenNotes(author).copy();
		author.getInventory().clearContent();
		InventoryMenu menu = author.inventoryMenu;
		menu.getCraftSlots().setItem(0, original.copy());
		menu.getCraftSlots().setItem(1, new ItemStack(JugcraftConcordance.RESEARCH_NOTES_ITEM, 3));
		menu.getCraftSlots().setItem(2, new ItemStack(JugcraftConcordance.GLOAM_INK, 3));
		ResearchNotes shown = menu.getSlot(InventoryMenu.RESULT_SLOT).getItem().get(JugcraftConcordance.RESEARCH_NOTES);
		helper.assertTrue(shown != null && shown.fairCopy(), "The result slot shows a fair copy: " + shown);
		menu.clicked(InventoryMenu.RESULT_SLOT, 0, ContainerInput.PICKUP, author);
		ResearchNotes carried = menu.getCarried().get(JugcraftConcordance.RESEARCH_NOTES);
		helper.assertTrue(menu.getCarried().getCount() == 1 && carried != null && carried.fairCopy(), "One copy taken");
		helper.assertTrue(ItemStack.isSameItemSameComponents(menu.getCraftSlots().getItem(0), original)
				&& menu.getCraftSlots().getItem(1).getCount() == 2 && menu.getCraftSlots().getItem(2).getCount() == 2,
				"The original stays in its slot; one blank and one ink are used");
		ItemStack taken = menu.getCarried().copy();
		menu.setCarried(ItemStack.EMPTY);
		author.getInventory().add(taken);
		menu.clicked(InventoryMenu.RESULT_SLOT, 0, ContainerInput.QUICK_MOVE, author);
		Inventory inventory = author.getInventory();
		int copies = 0;
		int bottles = 0;
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			ResearchNotes held = stack.get(JugcraftConcordance.RESEARCH_NOTES);
			if (held != null && held.fairCopy()) {
				copies += stack.getCount();
				helper.assertTrue(stack.getCount() == 3, "The copies of one sheet stack: " + stack.getCount());
			}
			if (stack.is(Items.GLASS_BOTTLE)) {
				bottles += stack.getCount();
			}
		}
		// The last ink's bottle stays in the ink's own slot, as a crafting grid gives a remainder back where its slot is empty
		// (26.3's ResultSlot.onTake); the two before it go to the inventory.
		ItemStack lastBottle = menu.getCraftSlots().getItem(2);
		helper.assertTrue(copies == 3 && lastBottle.is(Items.GLASS_BOTTLE) && bottles + lastBottle.getCount() == 3,
				"Three copies and three bottles in all: " + copies + ", " + bottles + " and " + lastBottle);
		helper.assertTrue(ItemStack.isSameItemSameComponents(menu.getCraftSlots().getItem(0), original)
				&& menu.getCraftSlots().getItem(1).isEmpty() && menu.getSlot(InventoryMenu.RESULT_SLOT).getItem().isEmpty(),
				"The original is still in the grid; the blanks and inks are used up; nothing more is shown");
		menu.getCraftSlots().clearContent();
		helper.succeed();
	}

	/**
	 * Gloam Ink (C-D6, D-S4): a fair copy of notes naming Stolas's Regard gives it nothing, as the original gives nothing
	 * (notesNeverStandInForStolasEvidence): no state and no kept evidence.
	 */
	@GameTest(maxTicks = 20)
	public void aFairCopyGivesStolasNothing(GameTestHelper helper) {
		ServerPlayer author = player(helper, new BlockPos(1, 2, 1));
		ServerPlayer reader = player(helper, new BlockPos(3, 2, 1));
		for (ServerPlayer each : List.of(author, reader)) {
			ConcordanceProgress.grant(each, FIRST_LIGHT, ResearchState.UNDERSTOOD);
			ConcordanceProgress.grant(each, Workers.RESEARCH, ResearchState.UNDERSTOOD);
		}
		ConcordanceProgress.grant(author, StolasRegard.RESEARCH, ResearchState.MASTERED);
		author.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(JugcraftConcordance.RESEARCH_NOTES_ITEM));
		ResearchNotesItem.write(author, InteractionHand.MAIN_HAND, author.getMainHandItem());
		ItemStack made = ResearchNotesCopyRecipe.INSTANCE.assemble(grid(writtenNotes(author),
				new ItemStack(JugcraftConcordance.RESEARCH_NOTES_ITEM), new ItemStack(JugcraftConcordance.GLOAM_INK)));
		ResearchNotes copy = made.get(JugcraftConcordance.RESEARCH_NOTES);
		helper.assertTrue(copy != null && copy.fairCopy() && copy.entries().get(StolasRegard.RESEARCH) == ResearchState.UNDERSTOOD,
				"The copy names Stolas's Regard as its original does: " + copy);
		ResearchNotesItem.read(reader, copy);
		Knowledge.Progress read = ConcordanceProgress.knowledge(reader).progress(StolasRegard.RESEARCH);
		helper.assertTrue(read.state() == ResearchState.NONE && read.evidence().isEmpty(),
				"Read, the copy gives Stolas's Regard nothing, not even kept evidence: " + read);
		helper.succeed();
	}

	/**
	 * Gloam Ink (C-D6), save compatibility: an original sheet saves exactly the fields it always had (author, author_name,
	 * entries, written; no fair_copy), so a sheet saved before Gloam Ink reads as an original; a fair copy saves fair_copy
	 * true and reads back as a copy, in its component and through a whole item stack.
	 */
	@GameTest(maxTicks = 20)
	public void anOlderSheetReadsAsAnOriginal(GameTestHelper helper) {
		ResearchNotes original = new ResearchNotes(UUID.randomUUID(), "Scribe", Map.of(FIRST_LIGHT, ResearchState.UNDERSTOOD), 42L);
		JsonElement saved = ResearchNotes.CODEC.encodeStart(JsonOps.INSTANCE, original).getOrThrow();
		helper.assertTrue(saved.getAsJsonObject().keySet().equals(Set.of("author", "author_name", "entries", "written")),
				"An original saves exactly the fields it always had: " + saved);
		ResearchNotes read = ResearchNotes.CODEC.parse(JsonOps.INSTANCE, saved).getOrThrow();
		helper.assertTrue(read.equals(original) && !read.fairCopy(), "A sheet without fair_copy reads as an original: " + read);
		ResearchNotes copy = original.asFairCopy();
		JsonElement copied = ResearchNotes.CODEC.encodeStart(JsonOps.INSTANCE, copy).getOrThrow();
		helper.assertTrue(copied.getAsJsonObject().has("fair_copy") && copied.getAsJsonObject().get("fair_copy").getAsBoolean()
				&& ResearchNotes.CODEC.parse(JsonOps.INSTANCE, copied).getOrThrow().equals(copy),
				"A copy saves and reads back as a copy: " + copied);
		RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess());
		ItemStack stack = sheet(copy);
		ItemStack back = ItemStack.CODEC.parse(ops, ItemStack.CODEC.encodeStart(ops, stack).getOrThrow()).getOrThrow();
		ResearchNotes kept = back.get(JugcraftConcordance.RESEARCH_NOTES);
		helper.assertTrue(ItemStack.isSameItemSameComponents(back, stack) && kept != null && kept.fairCopy(),
				"A copied sheet keeps its notes through a save: " + back);
		helper.succeed();
	}
}
