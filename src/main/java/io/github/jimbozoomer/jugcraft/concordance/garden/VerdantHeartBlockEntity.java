package io.github.jimbozoomer.jugcraft.concordance.garden;

import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceData;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceProgress;
import io.github.jimbozoomer.jugcraft.concordance.LeyPylonBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.ecology.Beat;
import io.github.jimbozoomer.jugcraft.concordance.ecology.Habitat;
import io.github.jimbozoomer.jugcraft.concordance.ecology.Organism;
import io.github.jimbozoomer.jugcraft.concordance.ecology.Verdict;
import io.github.jimbozoomer.jugcraft.concordance.resource.Conversion;
import io.github.jimbozoomer.jugcraft.concordance.resource.Overflow;
import io.github.jimbozoomer.jugcraft.concordance.resource.Ownership;
import io.github.jimbozoomer.jugcraft.concordance.resource.RateBudget;
import io.github.jimbozoomer.jugcraft.concordance.resource.Reservoir;
import io.github.jimbozoomer.jugcraft.concordance.resource.Transfers;
import io.github.jimbozoomer.jugcraft.party.JugcraftParties;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * The Verdant Heart (roadmap step 14): the garden's producer. Set on a Verdant Bed, it beats every
 * {@value #BEAT_TICKS} ticks; each beat in its niche (data: the {@code producer} organism) spends its cost in nutrients
 * from the bed and makes Verdance ({@code essence/verdance}), twice as much while the garden is thriving, up to
 * {@value #CAPACITY}. It makes nothing outside its niche, on a bed too poor, or when full, and says which.
 * <p>
 * Its Verdance leaves only through the shared transfer rules: a Gleaner draws it ({@link #drawInto}), and it pours
 * into the Ley Pylons beside it through the conversion {@code jugcraft:verdance_to_ley} (3 for 2), into pylons its
 * keeper's party owns or no one does. Nothing turns back into Verdance. Since the garden cohort (GC-05) a pact spirit may
 * name the Gleaner ({@link GleanerBlockEntity#receiveFrom}) or the pylon ({@link #pourInto}) at a distance; the rules are
 * the same ones.
 */
public class VerdantHeartBlockEntity extends LivingDeviceBlockEntity implements GeoBlockEntity {
	public static final int BEAT_TICKS = 200;
	public static final int CAPACITY = 64;
	private static final RawAnimation DORMANT = RawAnimation.begin().thenLoop("animation.verdant_heart.dormant");
	private static final RawAnimation BEATING = RawAnimation.begin().thenLoop("animation.verdant_heart.beating");
	private static final RawAnimation THRIVING = RawAnimation.begin().thenLoop("animation.verdant_heart.thriving");

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private long verdance;
	/** The last beat's pace (thriving, tolerating, stalled), for the animation. */
	private String growth = "";

	public VerdantHeartBlockEntity(BlockPos pos, BlockState state) {
		super(Garden.HEART_ENTITY, pos, state);
	}

	public long verdance() {
		return verdance;
	}

	public String growth() {
		return growth;
	}

	public Reservoir reservoir() {
		return new Reservoir(Garden.VERDANCE, verdance, CAPACITY);
	}

	/** Operators and tests: sets its Verdance. */
	public void setVerdance(long amount) {
		verdance = Math.clamp(amount, 0L, CAPACITY);
		setChanged();
		sync();
	}

	void serverTick(ServerLevel level) {
		if (due(level, BEAT_TICKS)) {
			beat(level);
		}
	}

	/** One beat (its pulse, or a test's); returns the status it leaves. */
	public String beat(ServerLevel level) {
		Organism heart = Garden.organism(getBlockState());
		if (!Garden.enabled() || heart == null || heart.crop()) {
			status("disabled");
			return status;
		}
		if (!awake) {
			status("dormant");
			return status;
		}
		if (!(level.getBlockEntity(worldPosition.below()) instanceof VerdantBedBlockEntity bed)) {
			status("no_bed");
			return status;
		}
		Habitat habitat = bed.habitat(level);
		if (habitat == null) {
			status("waiting");
			return status;
		}
		Verdict verdict = Verdict.of(heart.niche(), habitat);
		bed.note(verdict, habitat, heart);
		if (!verdict.growth().id.equals(growth)) {
			growth = verdict.growth().id;
			sync();
		}
		Beat beat = Beat.of(heart, verdict, bed.nutrients(), CAPACITY - verdance);
		switch (beat.outcome()) {
			case STALLED -> status("stalled");
			case STARVED -> status("starved");
			case FULL -> status("full");
			case BEAT -> {
				if (bed.take(beat.spent())) {
					verdance += beat.made();
					setChanged();
					status("working");
					sync();
				}
			}
		}
		pour(level);
		return status;
	}

	/** Pours whole batches of Verdance into the Ley Pylons beside it that its keeper's party (or no one) owns. */
	private void pour(ServerLevel level) {
		Conversion conversion = conversion();
		if (conversion == null) {
			return;
		}
		for (Direction side : Direction.values()) {
			if (verdance < conversion.fromAmount()) {
				return;
			}
			BlockPos at = worldPosition.relative(side);
			if (level.isLoaded(at) && level.getBlockEntity(at) instanceof LeyPylonBlockEntity pylon) {
				pourBatches(level, conversion, pylon, Long.MAX_VALUE);
			}
		}
	}

	/**
	 * Concordance garden cohort, GC-05 (Bathin's pylon road): pours at most {@code maxBatches} whole batches into the named
	 * {@code pylon}, wherever it stands in this level, by the rule of its pour beside it: the data conversion
	 * jugcraft:verdance_to_ley (3 Verdance for 2 Ley Charge), only into a pylon its keeper's party owns or no one does, only
	 * what fits in whole batches. The Verdance spent and the Ley Charge made change in this one call; nothing is on the way.
	 * Returns the Ley Charge made: 0 when nothing moved (switched off, asleep, another level, not its keeper's party's, too
	 * little Verdance or too little room).
	 */
	public long pourInto(LeyPylonBlockEntity pylon, ServerLevel level, long maxBatches) {
		Conversion conversion = conversion();
		if (conversion == null || !Garden.enabled() || !awake || maxBatches < 1 || pylon.isRemoved() || pylon.getLevel() != level) {
			return 0L;
		}
		return pourBatches(level, conversion, pylon, maxBatches);
	}

	/**
	 * GC-05: whether {@link #pourInto} may pour at least one batch into {@code pylon} by its owner and both amounts alone
	 * (not whether this Heart is awake or the garden switched on; pourInto checks those). Reads only.
	 */
	public boolean mayPourInto(LeyPylonBlockEntity pylon) {
		Conversion conversion = conversion();
		return conversion != null && mayFeed(pylon.owner()) && conversion.batches(verdance, LeyPylonBlockEntity.CAPACITY - pylon.ley(), 1L) > 0;
	}

	/** GC-05: whether {@code pos} shares a face with this Heart (where its own pour reaches). */
	public boolean beside(BlockPos pos) {
		return worldPosition.distManhattan(pos) == 1;
	}

	/**
	 * GC-05 (the two roads): whether a Ley Pylon on one of its faces is one its own pour feeds (its keeper's party's or no
	 * one's), full or not: a Heart with its own road. At most six reads, in loaded chunks only; changes nothing.
	 */
	public boolean feedsAPylonBeside(ServerLevel level) {
		for (Direction side : Direction.values()) {
			BlockPos at = worldPosition.relative(side);
			if (level.isLoaded(at) && level.getBlockEntity(at) instanceof LeyPylonBlockEntity pylon && mayFeed(pylon.owner())) {
				return true;
			}
		}
		return false;
	}

	/** The one conversion its Verdance leaves by (data: Garden.CONVERSION), or null while that is not Verdance into Ley Charge. */
	private static @Nullable Conversion conversion() {
		Conversion conversion = ConcordanceData.rules().conversions().get(Garden.CONVERSION);
		return conversion != null && conversion.from().equals(Garden.VERDANCE) && conversion.to().equals(LeyPylonBlockEntity.LEY) ? conversion : null;
	}

	/** Whole batches into {@code pylon}, at most {@code maxBatches}, if its keeper's party (or no one) owns it; the Ley Charge made. */
	private long pourBatches(ServerLevel level, Conversion conversion, LeyPylonBlockEntity pylon, long maxBatches) {
		if (!mayFeed(pylon.owner())) {
			return 0L;
		}
		long batches = conversion.batches(verdance, LeyPylonBlockEntity.CAPACITY - pylon.ley(), maxBatches);
		if (batches <= 0) {
			return 0L;
		}
		long made = pylon.fill(level, batches * conversion.toAmount());
		verdance -= made / conversion.toAmount() * conversion.fromAmount();
		setChanged();
		sync();
		return made;
	}

	private boolean mayFeed(@Nullable UUID owner) {
		return owner == null || keeper == null || owner.equals(keeper) || JugcraftParties.sameParty(owner, keeper);
	}

	/**
	 * Moves up to {@code max} Verdance into another container through {@link Transfers}: only for an actor this Heart
	 * trusts (its keeper's party). Returns the transfer; the caller keeps the target it gives back.
	 */
	public Transfers.Transfer drawInto(Reservoir target, Ownership targetOwnership, UUID actor, long max, ServerLevel level) {
		long now = ConcordanceProgress.now(level);
		Transfers.Transfer transfer = Transfers.move(new Transfers.Side(reservoir(), ownership()), new Transfers.Side(target, targetOwnership),
				actor, max, RateBudget.of(max, 1), now, Overflow.FILL);
		if (transfer.outcome().moved()) {
			verdance = transfer.source().amount();
			setChanged();
			sync();
		}
		return transfer;
	}

	// ---------------------------------------------------------------- saving and clients

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		verdance = Math.clamp(input.getLongOr("verdance", 0L), 0L, CAPACITY);
		growth = input.getStringOr("growth", "");
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putLong("verdance", verdance);
		output.putString("growth", growth);
	}

	@Override
	protected void clientData(CompoundTag tag) {
		tag.putLong("verdance", verdance);
		tag.putString("growth", growth);
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<VerdantHeartBlockEntity>("main", 5, test -> {
			VerdantHeartBlockEntity heart = test.animatable();
			boolean beating = heart.awake() && heart.status().equals("working");
			return test.setAndContinue(!beating ? DORMANT : heart.growth().equals("thriving") ? THRIVING : BEATING);
		}));
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}
}
