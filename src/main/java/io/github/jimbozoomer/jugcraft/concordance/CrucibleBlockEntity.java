package io.github.jimbozoomer.jugcraft.concordance;

import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import com.mojang.serialization.Codec;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.AlchemyCatalog;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Assay;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Axis;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Band;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Formula;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Heat;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Mixture;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Operation;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Outcome;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Preparation;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Vector;
import io.github.jimbozoomer.jugcraft.concordance.compose.Text;
import io.github.jimbozoomer.jugcraft.concordance.diagnostic.Code;
import io.github.jimbozoomer.jugcraft.concordance.diagnostic.Diagnostic;
import io.github.jimbozoomer.jugcraft.concordance.rime.Rime;
import io.github.jimbozoomer.jugcraft.concordance.rime.RimeSurvey;
import io.github.jimbozoomer.jugcraft.concordance.rules.Evidence;
import io.github.jimbozoomer.jugcraft.concordance.sign.Sign;
import io.github.jimbozoomer.jugcraft.concordance.sign.Signs;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Prediction;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * An Alembic Crucible (roadmap step 13): a {@link Mixture}, its temperature, and a formula to follow.
 * <ul>
 * <li><b>Heat</b> comes from the block beneath ({@link Heat#SOURCES}; a campfire only while lit) and moves one degree
 * a tick, worked out on the server every tick from what is there now.</li>
 * <li><b>By hand</b> (the Alembic Arts understood): water (a bucket is {@value #WATER_PER_BUCKET} parts, a water bottle
 * one), ingredients as they come or prepared, a stir (Stirring Rod or stick, at most one every {@value #STIR_TICKS}
 * ticks) at the band it is in, and bottling (a glass bottle makes a draught, a bowl a salve). Bottling records the
 * outcome as practice.</li>
 * <li><b>Sampling</b>: a spoon by anyone, an Assay Glass (more with the Alembic Arts understood, and the prediction once
 * mastered): {@link Assay}.</li>
 * <li><b>Formulas</b>: a blank formula records a fresh mixture that does something; a written one sets the crucible to
 * repeat it. Following a formula, every {@value #AUTOMATION_TICKS} ticks the crucible takes its next step when it can
 * (water from its tank, an ingredient from its {@value #BUFFER_SLOTS} buffer slots, a stir once the heat is right),
 * then bottles into its output while it has bottles or bowls. It never substitutes or guesses: it waits. It keeps why
 * ({@link #waiting()}). A formula the rules can no longer follow stays set, says which step and why, and does nothing
 * until they can again. Only someone who could change the block by hand changes a crucible (Concordance Chunk C,
 * C-01).</li>
 * </ul>
 * Hoppers and pipes: ingredients and reagents into the buffer, bottles and bowls into the bottle slot, from above or the
 * sides; brews out from the sides or below (the heat source usually sits below); water into the tank. The mixture is
 * saved with the crucible; broken, its slots drop and the mixture spills. Preserve (Rime) may hold it in its band; the
 * heat moves within the band. Keep the numbers equal to tools/concordance_alchemy.py.
 */
public class CrucibleBlockEntity extends BlockEntity implements GeoBlockEntity, WorldlyContainer {
	public static final int BUFFER_SLOTS = 5;
	public static final int BOTTLE_SLOT = 5;
	public static final int OUTPUT_SLOT = 6;
	public static final int SLOTS = 7;
	public static final int STIR_TICKS = 10;
	public static final int AUTOMATION_TICKS = 20;
	public static final int WATER_PER_BUCKET = 3;
	/** Preserve's hold: the game time it ends (optional; absent = not held). */
	public static final String HELD_UNTIL = "held_until";
	/** From above, only what goes in; from the sides, that and the output; from below, only the output. */
	private static final int[] ABOVE = {0, 1, 2, 3, 4, BOTTLE_SLOT};
	private static final int[] SIDES = {0, 1, 2, 3, 4, BOTTLE_SLOT, OUTPUT_SLOT};
	private static final int[] BELOW = {OUTPUT_SLOT};
	private static final RawAnimation EMPTY = RawAnimation.begin().thenLoop("animation.crucible.empty");
	private static final RawAnimation STILL = RawAnimation.begin().thenLoop("animation.crucible.still");
	private static final RawAnimation SIMMER = RawAnimation.begin().thenLoop("animation.crucible.simmer");

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
	private Mixture mixture = Mixture.EMPTY;
	private int temperature = Heat.AMBIENT;
	private @Nullable Formula program;
	private int step;
	public final local.peepo.CrucibleCompanion companion = new local.peepo.CrucibleCompanion(this);
	private long lastStir = Long.MIN_VALUE / 2;
	/** Preserve: game time until which the temperature stays in its band; 0 when not held. Saved only while held. */
	private long heldUntil;
	/** Why its formula waits now: what stopped its last pulse (Concordance Chunk C, C-01), or null. Not saved. Read through waiting(). */
	private @Nullable Diagnostic waiting;
	/** A saved formula this build can no longer read (C-01, C-P1): kept as written and saved back, never followed; "" if none. */
	private String unreadable = "";
	/** The rules revision (ConcordanceData.revision()) its formula was last replayed against; -1 to replay at the next look. Not saved. */
	private int checkedRevision = -1;
	/** Why the rules in force cannot follow its formula (as of checkedRevision), or null. Not saved. */
	private @Nullable Diagnostic incompatible;
	/** On a client: what the liquid looks like, as the server sent it (the spoon's reading, never the makeup). */
	private Assay.Look shown = Assay.Look.PLAIN;

	/** Water waiting for the formula's water steps: up to a bucket, water only, never drawn back out. */
	public final SingleFluidStorage water = new SingleFluidStorage() {
		@Override
		protected long getCapacity(FluidVariant variant) {
			return FluidConstants.BUCKET;
		}

		@Override
		protected boolean canInsert(FluidVariant variant) {
			return variant.isOf(Fluids.WATER);
		}

		@Override
		protected boolean canExtract(FluidVariant variant) {
			return false;
		}

		@Override
		protected void onFinalCommit() {
			setChanged();
		}
	};

	public CrucibleBlockEntity(BlockPos pos, BlockState state) {
		super(JugcraftConcordance.CRUCIBLE_ENTITY, pos, state);
	}

	public Mixture mixture() {
		return mixture;
	}

	public int temperature() {
		return temperature;
	}

	public Band band() {
		return Band.of(temperature);
	}

	/** What the liquid looks like (roadmap step 27): on the server, the spoon's reading of the mixture; on a client, what was sent. */
	public Assay.Look look() {
		return level != null && level.isClientSide() ? shown : Assay.Look.of(mixture, Alchemy.catalog());
	}

	public @Nullable Formula program() {
		return program;
	}

	public int step() {
		return step;
	}

	/** Tests and operators: sets the temperature at once (it then moves towards its source as usual). */
	public void setTemperature(int temperature) {
		this.temperature = temperature;
		setChanged();
	}

	public long heldUntil() {
		return heldUntil;
	}

	/** Ticks Preserve still holds the band at {@code now} (0 when not held). */
	public int held(long now) {
		return heldUntil > now ? (int) Math.min(Rime.MAX_HOLD_TICKS, heldUntil - now) : 0;
	}

	/** Preserve: holds the band it is in now for {@code ticks} (at most Rime.MAX_HOLD_TICKS; a recast restarts it); 0 ends a hold. */
	public void hold(long now, int ticks) {
		heldUntil = ticks <= 0 ? 0L : now + Math.min(ticks, Rime.MAX_HOLD_TICKS);
		setChanged();
	}

	// ---------------------------------------------------------------- heat and automation

	void serverTick(ServerLevel level) {
		int target = heatBelow(level);
		int next = Heat.toward(temperature, target);
		if (heldUntil != 0L) {
			next = holding(level, next);
		}
		if (next != temperature) {
			boolean newBand = Band.of(next) != Band.of(temperature);
			temperature = next;
			setChanged();
			if (newBand) {
				sync(level);
			}
		}
		if ((program != null || !unreadable.isEmpty()) && level.getGameTime() % AUTOMATION_TICKS == 0
				&& JugcraftConfig.isFeatureEnabled(JugcraftConcordance.FEATURE)) {
			automate(level);
		}
	}

	/** While held, the next temperature kept in its band; once over, forgotten. A hold further ahead than allowed is cut. */
	private int holding(ServerLevel level, int next) {
		long now = ConcordanceProgress.now(level);
		if (now >= heldUntil) {
			heldUntil = 0L;
			setChanged();
			return next;
		}
		if (heldUntil - now > Rime.MAX_HOLD_TICKS) {
			heldUntil = now + Rime.MAX_HOLD_TICKS;
			setChanged();
		}
		return band().keep(next);
	}

	/** The block beneath, read now: what heats it (serverTick) and what the Inspect page names (C-01). */
	public Heat.Below below(ServerLevel level) {
		BlockState below = level.getBlockState(worldPosition.below());
		return new Heat.Below(BuiltInRegistries.BLOCK.getKey(below.getBlock()).toString(),
				below.hasProperty(BlockStateProperties.LIT) && !below.getValue(BlockStateProperties.LIT));
	}

	/** The temperature the block beneath gives (a campfire only while lit). */
	private int heatBelow(ServerLevel level) {
		return below(level).target();
	}

	/**
	 * One pulse of its formula, every {@value #AUTOMATION_TICKS} ticks (or a test's, at a known moment): its next step if
	 * it can be taken now or, the formula done, one bottle into the output. Returns why it waits (waiting()), or null when
	 * it did something. A formula the rules in force cannot follow, or a saved one that no longer reads, keeps its step and
	 * does nothing at all, no step and no bottle, until they can again (C-01, C-P1, C-P23).
	 */
	public @Nullable Diagnostic automate(ServerLevel level) {
		Diagnostic refused = incompatibility();
		if (refused != null) {
			return pause(level, refused);
		}
		if (program == null) {
			return null;
		}
		List<Operation> operations = program.operations();
		if (step >= operations.size()) {
			if (!mixture.isEmpty()) {
				return bottleIntoOutput(level);
			}
			step = 0;
			waiting = null;
			setChanged();
			return null;
		}
		if (step == 0 && !mixture.isEmpty()) {
			return pause(level, Diagnostic.of(Code.NEEDS_EMPTYING)); // a repeat starts from an empty crucible
		}
		Operation operation = operations.get(step);
		int slot = -1;
		long water = 0;
		switch (operation) {
			case Operation.Water pour -> {
				water = pour.parts() * FluidConstants.BOTTLE;
				if (this.water.amount < water) {
					return pause(level, Diagnostic.needsWater(pour.parts(), tankParts()));
				}
			}
			case Operation.Add add -> {
				slot = bufferSlotFor(add);
				if (slot < 0) {
					return pause(level, missing(add));
				}
			}
			case Operation.Stir stir -> {
				if (band() != stir.band()) {
					// Waiting for the heat shows no sign, as it never did. Preserve's hold is the reason only while it keeps the
					// heat from the stir's band: unheld, the block beneath would move it that way (C-01). Over a magma block
					// (warm at most) a hot stir waits for heat, held or not.
					int held = held(ConcordanceProgress.now(level));
					Band heading = Band.of(heatBelow(level));
					boolean kept = held > 0 && Integer.compare(heading.ordinal(), band().ordinal()) == Integer.compare(stir.band().ordinal(), band().ordinal());
					return pause(level, kept ? Diagnostic.held(band().id, (held + 19) / 20) : Diagnostic.wrongHeat(stir.band().id, band().id));
				}
			}
		}
		Mixture.Step result = mixture.apply(operation, Alchemy.catalog());
		if (!(result instanceof Mixture.Applied applied)) {
			// The replay passed, so the live mixture refused: the formula is kept and the step named, never cleared (C-P1).
			return pause(level, program.incompatible(new Mixture.Refused(((Mixture.Refused) result).reason(), step)));
		}
		if (water > 0) {
			this.water.amount -= water;
			if (this.water.amount == 0) {
				this.water.variant = FluidVariant.blank();
			}
		}
		if (slot >= 0) {
			items.get(slot).shrink(1);
		}
		mixture = applied.mixture();
		step++;
		waiting = null;
		setChanged();
		sync(level);
		sound(level, operation instanceof Operation.Stir ? JugcraftConcordance.CRUCIBLE_STIR_SOUND : JugcraftConcordance.CRUCIBLE_ADD_SOUND);
		signStep(level, operation);
		return null;
	}

	/** Roadmap step 27: a step taken shows as work; a searing stir, which damages the mixture, as danger. */
	private void signStep(ServerLevel level, Operation operation) {
		Signs.show(level, worldPosition, operation instanceof Operation.Stir stir && stir.band() == Band.SEARING ? Sign.PERIL : Sign.WORK);
	}

	/**
	 * Its formula cannot go on now, for {@code reason}: kept for Jade, the Inspect page, the status line and the Peepo
	 * port, and shown once when the reason begins (C-P22): a shortage or a blockage as WANT, a fault as PERIL with the
	 * breaking sound, a wait for heat or time as nothing.
	 */
	private Diagnostic pause(ServerLevel level, Diagnostic reason) {
		if (waiting == null || waiting.code() != reason.code()) {
			switch (reason.code().kind) {
				case SHORTAGE, BLOCKED -> Signs.show(level, worldPosition, Sign.WANT);
				case FAULT -> {
					Signs.show(level, worldPosition, Sign.PERIL);
					sound(level, JugcraftConcordance.CIRCLE_BREAK_SOUND);
				}
				case CONDITION -> {
				}
			}
		}
		waiting = reason;
		return reason;
	}

	/** What an Add step lacks: the ingredient prepared otherwise, if the buffer holds it so; else the ingredient. */
	private Diagnostic missing(Operation.Add add) {
		Preparation plain = Alchemy.catalog().asItComes();
		for (int i = 0; i < BUFFER_SLOTS; i++) {
			ItemStack stack = items.get(i);
			if (stack.isEmpty()) {
				continue;
			}
			Reagent reagent = stack.get(JugcraftConcordance.REAGENT);
			String item = reagent != null ? reagent.item() : ConcordanceProgress.itemId(stack);
			String found = reagent != null ? reagent.preparation() : plain == null ? null : plain.id();
			if (found != null && item.equals(add.item()) && !found.equals(add.preparation())) {
				return Diagnostic.wrongPreparation(add.describe(), Operation.ingredient(item, found));
			}
		}
		return Diagnostic.missingIngredient(1, add.describe());
	}

	/** The buffer slot holding what an Add step needs: the item itself as it comes, or a reagent of that preparation. */
	private int bufferSlotFor(Operation.Add add) {
		for (int i = 0; i < BUFFER_SLOTS; i++) {
			ItemStack stack = items.get(i);
			if (stack.isEmpty()) {
				continue;
			}
			if (matchesIngredient(add, stack)) {
				return i;
			}
		}
		return -1;
	}

	/** Shared exact ingredient/preparation matching for native automation and companion deliveries. */
	public static boolean matchesIngredient(Operation.Add add, ItemStack stack) {
		if (stack.isEmpty()) return false;
		Preparation plain = Alchemy.catalog().asItComes();
		Reagent reagent = stack.get(JugcraftConcordance.REAGENT);
		return reagent != null ? reagent.item().equals(add.item()) && reagent.preparation().equals(add.preparation())
			: plain != null && plain.id().equals(add.preparation()) && ConcordanceProgress.itemId(stack).equals(add.item());
	}

	private @Nullable Diagnostic bottleIntoOutput(ServerLevel level) {
		ItemStack container = items.get(BOTTLE_SLOT);
		Item form = formFor(container);
		if (form == null) {
			return pause(level, Diagnostic.of(Code.NEEDS_CONTAINER));
		}
		Mixture.Bottled bottled = mixture.bottle();
		Outcome outcome = Outcome.of(bottled.dose(), bottled.contaminant(), Alchemy.catalog(), mixture.history());
		ItemStack made = Alchemy.brew(form, outcome);
		ItemStack output = items.get(OUTPUT_SLOT);
		if (!output.isEmpty() && !ItemStack.isSameItemSameComponents(output, made)) {
			return pause(level, Diagnostic.of(Code.OUTPUT_DIFFERS)); // paused; nothing voided (plan 03:64)
		}
		if (!output.isEmpty() && output.getCount() >= output.getMaxStackSize()) {
			return pause(level, Diagnostic.of(Code.INSUFFICIENT_CAPACITY));
		}
		container.shrink(1);
		if (output.isEmpty()) {
			items.set(OUTPUT_SLOT, made);
		} else {
			output.grow(1);
		}
		mixture = bottled.rest();
		if (mixture.isEmpty()) {
			step = 0;
		}
		waiting = null;
		setChanged();
		sync(level);
		sound(level, JugcraftConcordance.CRUCIBLE_BOTTLE_SOUND);
		Signs.show(level, worldPosition, Sign.DONE);
		return null;
	}

	private static @Nullable Item formFor(ItemStack container) {
		if (container.is(Items.GLASS_BOTTLE)) {
			return JugcraftConcordance.DRAUGHT;
		}
		return container.is(Items.BOWL) ? JugcraftConcordance.SALVE : null;
	}

	// ---------------------------------------------------------------- players

	/** A player uses an item on the crucible. TRY_WITH_EMPTY_HAND for an item it does nothing with. */
	public InteractionResult use(ServerPlayer player, ServerLevel level, ItemStack held, InteractionHand hand) {
		if (!JugcraftConfig.isFeatureEnabled(JugcraftConcordance.FEATURE)) {
			player.sendOverlayMessage(Component.translatable("message.jugcraft.concordance.disabled"));
			return InteractionResult.FAIL;
		}
		AlchemyCatalog catalog = Alchemy.catalog();
		if (held.is(JugcraftConcordance.SAMPLING_SPOON)) {
			Alchemy.tell(player, Assay.read(mixture, temperature, Assay.SPOON, catalog));
			return InteractionResult.SUCCESS;
		}
		if (held.is(JugcraftConcordance.ASSAY_GLASS)) {
			Alchemy.tell(player, Assay.read(mixture, temperature, Alchemy.glassLevel(player), catalog));
			return InteractionResult.SUCCESS;
		}
		boolean water = held.is(Items.WATER_BUCKET) || held.is(Items.POTION)
				&& held.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER);
		boolean stirrer = held.is(JugcraftConcordance.STIRRING_ROD) || held.is(Items.STICK);
		Reagent reagent = held.get(JugcraftConcordance.REAGENT);
		boolean ingredient = reagent != null || catalog.ingredient(ConcordanceProgress.itemId(held)) != null;
		Item form = formFor(held);
		boolean formula = held.is(JugcraftConcordance.FORMULA_ITEM);
		if (!water && !stirrer && !ingredient && form == null && !formula) {
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}
		if (!Alchemy.knows(player)) {
			player.sendOverlayMessage(Component.translatable("message.jugcraft.concordance.alchemy.unknown"));
			return InteractionResult.FAIL;
		}
		// C-01 (C-P5, C-P24): only someone who could change the block by hand changes a crucible (water, adds, stirs,
		// bottling, setting a formula). Reading stays open: the spoon and glass above, and recording its process on a blank.
		boolean recording = formula && held.get(JugcraftConcordance.FORMULA) == null;
		if (!recording && !Authority.mayChange(level, player, worldPosition)) {
			player.sendOverlayMessage(ComposeText.show(Diagnostic.of(Code.MISSING_PERMISSION).text()));
			return InteractionResult.FAIL;
		}
		if (formula) {
			return formula(player, held);
		}
		if (form != null) {
			return bottle(player, level, held, form);
		}
		if (program != null && step < program.operations().size()) {
			player.sendOverlayMessage(Component.translatable("message.jugcraft.concordance.alchemy.automation_busy"));
			return InteractionResult.FAIL;
		}
		if (stirrer) {
			long now = ConcordanceProgress.now(level);
			if (now - lastStir < STIR_TICKS) {
				player.sendOverlayMessage(Component.translatable("message.jugcraft.concordance.alchemy.settling"));
				return InteractionResult.FAIL;
			}
			if (apply(player, level, new Operation.Stir(band()))) {
				lastStir = now;
				player.sendOverlayMessage(Component.translatable("message.jugcraft.concordance.alchemy.stirred",
						ComposeText.name(new Text.Ref("band", band().id))));
				sound(level, JugcraftConcordance.CRUCIBLE_STIR_SOUND);
			}
			return InteractionResult.SUCCESS;
		}
		if (water) {
			boolean bucket = held.is(Items.WATER_BUCKET);
			if (apply(player, level, new Operation.Water(bucket ? WATER_PER_BUCKET : 1))) {
				player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, new ItemStack(bucket ? Items.BUCKET : Items.GLASS_BOTTLE)));
				player.sendOverlayMessage(Component.translatable("message.jugcraft.concordance.alchemy.water", mixture.parts()));
			}
			return InteractionResult.SUCCESS;
		}
		String item = reagent != null ? reagent.item() : ConcordanceProgress.itemId(held);
		Preparation preparation = reagent != null ? catalog.preparation(reagent.preparation()) : catalog.asItComes();
		if (preparation == null) {
			return InteractionResult.FAIL;
		}
		if (apply(player, level, new Operation.Add(item, preparation.id()))) {
			Component name = held.getHoverName();
			held.shrink(1);
			player.sendOverlayMessage(Component.translatable("message.jugcraft.concordance.alchemy.added", name));
			sound(level, JugcraftConcordance.CRUCIBLE_ADD_SOUND);
		}
		return InteractionResult.SUCCESS;
	}

	/** Applies an operation by hand, or tells the player why it cannot be done. */
	private boolean apply(ServerPlayer player, ServerLevel level, Operation operation) {
		Mixture.Step result = mixture.apply(operation, Alchemy.catalog());
		if (result instanceof Mixture.Refused refused) {
			player.sendOverlayMessage(ComposeText.show(refused.reason()));
			return false;
		}
		mixture = ((Mixture.Applied) result).mixture();
		setChanged();
		sync(level);
		signStep(level, operation);
		return true;
	}

	/** Bottles one dose by hand into a glass bottle (a draught) or a bowl (a salve), and records the outcome as practice. */
	private InteractionResult bottle(ServerPlayer player, ServerLevel level, ItemStack held, Item form) {
		Mixture.Bottled bottled = mixture.bottle();
		if (bottled == null) {
			player.sendOverlayMessage(Component.translatable("message.jugcraft.concordance.alchemy.nothing_to_bottle"));
			return InteractionResult.FAIL;
		}
		Outcome outcome = Outcome.of(bottled.dose(), bottled.contaminant(), Alchemy.catalog(), mixture.history());
		ItemStack made = Alchemy.brew(form, outcome);
		mixture = bottled.rest();
		if (program != null && mixture.isEmpty()) {
			step = 0;
		}
		held.shrink(1);
		player.getInventory().placeItemBackInInventory(made, Prediction.SERVER_ONLY);
		if (!outcome.effects().isEmpty()) {
			ConcordanceProgress.record(player, new Evidence.Practiced(Alchemy.ACTIVITY, outcome.key()));
		}
		RimeSurvey.bottled(player, bottled.dose()); // Rimekeeping's workshop study (Chunk B unit 3)
		player.sendOverlayMessage(Component.translatable("message.jugcraft.concordance.alchemy.bottled", made.getHoverName()));
		setChanged();
		sync(level);
		sound(level, JugcraftConcordance.CRUCIBLE_BOTTLE_SOUND);
		Signs.show(level, worldPosition, Sign.DONE);
		return InteractionResult.SUCCESS;
	}

	/** A blank formula records this mixture's process; a written one sets the crucible to repeat it. */
	private InteractionResult formula(ServerPlayer player, ItemStack held) {
		String written = held.get(JugcraftConcordance.FORMULA);
		if (written != null) {
			Formula formula = Formula.parse(written);
			if (formula == null) {
				player.sendOverlayMessage(Component.translatable("message.jugcraft.concordance.alchemy.formula_invalid"));
				return InteractionResult.FAIL;
			}
			AlchemyCatalog catalog = Alchemy.catalog();
			Component refused = refusal(formula, catalog);
			if (refused != null) {
				player.sendOverlayMessage(refused);
				return InteractionResult.FAIL;
			}
			follow(formula);
			// What is never hidden: the formula's own, and, set over a mixture already in it, that mixture's (C-01).
			List<Text> risks = Assay.risks(formula, mixture, !mixture.isEmpty(), Alchemy::harmfulStatus, catalog);
			if (risks.isEmpty()) {
				player.sendOverlayMessage(Component.translatable("message.jugcraft.concordance.alchemy.formula_set", formula.operations().size()));
			} else {
				MutableComponent risk = Component.empty();
				for (int i = 0; i < risks.size(); i++) {
					if (i > 0) {
						risk.append(" ");
					}
					risk.append(ComposeText.show(risks.get(i)));
				}
				player.sendOverlayMessage(Component.translatable("message.jugcraft.concordance.alchemy.formula_set_risk",
						formula.operations().size(), risk));
			}
			return InteractionResult.SUCCESS;
		}
		Formula formula = Formula.of(mixture);
		if (formula == null || mixture.outcome(Alchemy.catalog()).effects().isEmpty()) {
			player.sendOverlayMessage(Component.translatable("message.jugcraft.concordance.alchemy.no_formula"));
			return InteractionResult.FAIL;
		}
		ItemStack record = held.copyWithCount(1);
		record.set(JugcraftConcordance.FORMULA, formula.text());
		held.shrink(1);
		player.getInventory().placeItemBackInInventory(record, Prediction.SERVER_ONLY);
		player.sendOverlayMessage(Component.translatable("message.jugcraft.concordance.alchemy.recorded", formula.operations().size()));
		return InteractionResult.SUCCESS;
	}

	/**
	 * What the formula item's use tells when the rules in force cannot replay a written formula (plan 03:54; C-01): which
	 * step they refuse, and why; null when they can replay it. The use sends exactly this.
	 */
	public static @Nullable Component refusal(Formula formula, AlchemyCatalog catalog) {
		Diagnostic why = formula.incompatibility(catalog);
		return why == null ? null : Component.translatable("message.jugcraft.concordance.alchemy.formula_refused", ComposeText.show(why.text()));
	}

	/**
	 * Sets {@code formula} to repeat from its first step: the formula item's use after its checks, and tests and operators.
	 * Nothing is checked here; the rules in force are checked at its next look, as after a load.
	 */
	public void follow(Formula formula) {
		program = formula;
		unreadable = "";
		step = 0;
		waiting = null;
		incompatible = null;
		checkedRevision = -1;
		setChanged();
	}

	/** Empty-handed: the state of the pot and why its formula waits; sneaking, stop repeating a formula. */
	public void useEmpty(ServerPlayer player, boolean sneaking) {
		if (sneaking && (program != null || !unreadable.isEmpty())) {
			// Only an alembist may stop a formula (roadmap step 28), as only one may set it; since C-01 (C-P5), only one who
			// could change the block by hand.
			if (!Alchemy.knows(player)) {
				player.sendOverlayMessage(Component.translatable("message.jugcraft.concordance.alchemy.unknown"));
				return;
			}
			if (!Authority.mayChange(player.level(), player, worldPosition)) {
				player.sendOverlayMessage(ComposeText.show(Diagnostic.of(Code.MISSING_PERMISSION).text()));
				return;
			}
			program = null;
			unreadable = "";
			step = 0;
			waiting = null;
			incompatible = null;
			checkedRevision = -1;
			setChanged();
			player.sendOverlayMessage(Component.translatable("message.jugcraft.concordance.alchemy.formula_cleared"));
			return;
		}
		player.sendOverlayMessage(statusLine(ConcordanceProgress.now(player.level())));
	}

	/** The empty-hand status line (one overlay, so nothing replaces it): band, heat, volume, Preserve's hold, then why it waits. */
	public Component statusLine(long now) {
		int held = held(now);
		Component band = ComposeText.name(new Text.Ref("band", band().id));
		Component line = held > 0
				? Component.translatable("message.jugcraft.concordance.crucible.status_held", band, temperature, mixture.parts(), (held + 19) / 20)
				: Component.translatable("message.jugcraft.concordance.crucible.status", band, temperature, mixture.parts());
		Diagnostic reason = waiting();
		return reason == null ? line : Component.translatable("message.jugcraft.concordance.crucible.status_waiting", line, ComposeText.show(reason.text()));
	}

	/**
	 * Why its formula waits now (C-01), or null if it has none or is not waiting: unreadable, a step the rules in force
	 * refuse, or what stopped its last pulse (never saved; up to {@value #AUTOMATION_TICKS} ticks old). Jade, the Inspect
	 * page, the status line and the Peepo port read this one source. Server only.
	 */
	public @Nullable Diagnostic waiting() {
		Diagnostic refused = incompatibility();
		return refused != null ? refused : program == null ? null : waiting;
	}

	/**
	 * Why its formula cannot be followed (C-01, C-P1): a saved one that no longer reads, or the first step the rules in
	 * force refuse; null if it can or there is none. Replayed (at most Mixture.MAX_OPERATIONS operations) once per revision
	 * of the rules (ConcordanceData.revision(): every load, reload and replace) and after a load; a cache, never saved.
	 * Such a formula drives nothing: no step, no bottle, no Peepo demand (C-05's stand and C-06's filing read this too).
	 * Server only.
	 */
	public @Nullable Diagnostic incompatibility() {
		if (program == null) {
			return unreadable.isEmpty() ? null : Diagnostic.of(Code.UNREADABLE);
		}
		int revision = ConcordanceData.revision();
		if (checkedRevision != revision) {
			checkedRevision = revision;
			incompatible = program.incompatibility(Alchemy.catalog());
			if (incompatible == null && waiting != null && waiting.code() == Code.INCOMPATIBLE) {
				waiting = null; // the rules can follow it again: it carries on from its step
			}
		}
		return incompatible;
	}

	/** A saved formula this build cannot read, kept as written (C-01); "" if none. */
	public String unreadableProgram() {
		return unreadable;
	}

	private void sound(ServerLevel level, SoundEvent sound) {
		level.playSound(null, worldPosition, sound, SoundSource.BLOCKS, 0.7F, 1.0F);
	}

	// ---------------------------------------------------------------- hoppers and pipes

	@Override
	public int[] getSlotsForFace(Direction side) {
		return side == Direction.DOWN ? BELOW : side == Direction.UP ? ABOVE : SIDES;
	}

	@Override
	public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
		return side != Direction.DOWN && canPlaceItem(slot, stack);
	}

	@Override
	public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
		// The heat source sits beneath, so an extractor at a side must be able to take the brews too.
		return slot == OUTPUT_SLOT && side != Direction.UP;
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		if (slot < BUFFER_SLOTS) {
			return stack.has(JugcraftConcordance.REAGENT) || Alchemy.catalog().ingredient(ConcordanceProgress.itemId(stack)) != null;
		}
		return slot == BOTTLE_SLOT && formFor(stack) != null;
	}

	@Override
	public int getContainerSize() {
		return SLOTS;
	}

	@Override
	public boolean isEmpty() {
		return items.stream().allMatch(ItemStack::isEmpty);
	}

	@Override
	public ItemStack getItem(int slot) {
		return items.get(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int count) {
		ItemStack taken = ContainerHelper.removeItem(items, slot, count);
		if (!taken.isEmpty()) {
			setChanged();
		}
		return taken;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return ContainerHelper.takeItem(items, slot);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		items.set(slot, stack);
		setChanged();
	}

	@Override
	public boolean stillValid(Player player) {
		return false; // no screen: only hoppers, pipes and the crucible's own uses reach the slots
	}

	@Override
	public void clearContent() {
		items.clear();
	}

	// ---------------------------------------------------------------- saving and clients

	private void sync(ServerLevel level) {
		level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
		ContainerHelper.loadAllItems(input, items);
		water.readValue(input);
		companion.filter.load(input);
		temperature = input.getIntOr("temperature", Heat.AMBIENT);
		int parts = Math.clamp(input.getIntOr("parts", 0), 0, Mixture.MAX_PARTS);
		List<Operation> history = new ArrayList<>();
		boolean intact = true;
		for (String text : input.read("history", Codec.STRING.listOf()).orElse(List.of())) {
			Operation operation = Operation.parse(text);
			if (operation == null || !operation.text().equals(text) || history.size() >= Mixture.MAX_OPERATIONS) {
				intact = false;
				break;
			}
			history.add(operation);
		}
		mixture = parts == 0 ? Mixture.EMPTY : new Mixture(parts, Vector.fromList(input.read("pending", Codec.LONG.listOf()).orElse(List.of())),
				Vector.fromList(input.read("dissolved", Codec.LONG.listOf()).orElse(List.of())), Math.max(0L, input.getLongOr("contaminant", 0L)),
				// A history that does not read back can no longer be recorded as a formula.
				intact ? Math.max(0, input.getIntOr("drawn", 0)) : 1, intact ? history : List.of());
		// Only an update to a client carries the look (the server works it out from the mixture).
		Axis taste = Axis.fromId(input.getStringOr("taste", ""));
		shown = new Assay.Look(taste, input.getBooleanOr("murky", false));
		String saved = input.getStringOr("program", "");
		program = saved.isEmpty() ? null : Formula.parse(saved);
		// A formula this build cannot read is kept as written, saved back and reported, never dropped (C-01, C-P1).
		unreadable = program == null ? saved : "";
		step = saved.isEmpty() ? 0 : Math.clamp(input.getIntOr("step", 0), 0, program != null ? program.operations().size() : Mixture.MAX_OPERATIONS);
		// Never saved: worked out again, the formula replayed against the rules in force at its next look.
		waiting = null;
		incompatible = null;
		checkedRevision = -1;
		heldUntil = Math.max(0L, input.getLongOr(HELD_UNTIL, 0L));
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		Saved.stamp(output, 1);
		ContainerHelper.saveAllItems(output, items);
		water.writeValue(output);
		companion.filter.save(output);
		output.putInt("temperature", temperature);
		output.putInt("parts", mixture.parts());
		if (!mixture.isEmpty()) {
			output.store("pending", Codec.LONG.listOf(), mixture.pending().toList());
			output.store("dissolved", Codec.LONG.listOf(), mixture.dissolved().toList());
			output.putLong("contaminant", mixture.contaminant());
			output.putInt("drawn", mixture.drawn());
			List<String> history = new ArrayList<>();
			for (Operation operation : mixture.history()) {
				history.add(operation.text());
			}
			output.store("history", Codec.STRING.listOf(), history);
		}
		if (program != null || !unreadable.isEmpty()) {
			output.putString("program", program != null ? program.text() : unreadable);
			output.putInt("step", step);
		}
		if (heldUntil > 0L) {
			output.putLong(HELD_UNTIL, heldUntil);
		}
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	/**
	 * Clients get the temperature and volume (the animation and Jade's lines) and what the liquid looks like (its
	 * strongest property and whether it is murky: what anyone's spoon would tell), not the slots or the mixture's makeup.
	 */
	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = new CompoundTag();
		tag.putInt("temperature", temperature);
		tag.putInt("parts", mixture.parts());
		Assay.Look look = Assay.Look.of(mixture, Alchemy.catalog());
		tag.putString("taste", look.taste() == null ? "" : look.taste().id);
		tag.putBoolean("murky", look.murky());
		return tag;
	}

	/** What a client knows: the volume (the server's mixture is never sent). */
	public int parts() {
		return mixture.parts();
	}

	/** Whole parts of water in the tank (a bottle each; water piped in part-way counts once a part is whole). */
	public int tankParts() {
		return (int) (water.amount / FluidConstants.BOTTLE);
	}

	// ---------------------------------------------------------------- GeckoLib (drawn on the client)

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<CrucibleBlockEntity>("main", 5, test -> test.setAndContinue(
				test.animatable().parts() == 0 ? EMPTY : test.animatable().band().ordinal() >= Band.HOT.ordinal() ? SIMMER : STILL)));
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}
}
