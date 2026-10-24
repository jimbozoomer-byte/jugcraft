package io.github.jimbozoomer.jugcraft.concordance.spirits;

import io.github.jimbozoomer.jugcraft.concordance.Authority;
import io.github.jimbozoomer.jugcraft.concordance.ComposeText;
import io.github.jimbozoomer.jugcraft.concordance.RateGate;
import io.github.jimbozoomer.jugcraft.concordance.diagnostic.Code;
import io.github.jimbozoomer.jugcraft.concordance.diagnostic.Diagnostic;
import io.github.jimbozoomer.jugcraft.concordance.worker.Agreement;
import io.github.jimbozoomer.jugcraft.concordance.worker.Pacts;
import io.github.jimbozoomer.jugcraft.concordance.worker.Status;
import io.github.jimbozoomer.jugcraft.concordance.worker.WorkerDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * The Spirit Anchor's block (roadmap step 17). An empty hand: someone who understands Binding Arts and may seal here (its
 * owner or their party, where they may build; an anchor placed before owners were recorded, whoever may build there) seals
 * an agreement, or is told the one reason not; its holder, sneaking, suspends or resumes it, and calls another shade when
 * theirs is lost; anyone reads its terms and how its spirit is. One whose Bound Will does not keep it says another
 * agreement covers it. Breaking it releases the agreement ({@link SpiritAnchorBlockEntity#preRemoveSideEffects}).
 */
public class SpiritAnchorBlock extends BaseEntityBlock {
	public SpiritAnchorBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SpiritAnchorBlockEntity(pos, state);
	}

	/** Placed: the anchor records who placed it (Concordance Chunk D, D-01, D-S8); a stand-in placing it records nobody. */
	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (level instanceof ServerLevel && placer instanceof ServerPlayer player && !Authority.isStandIn(player)
				&& level.getBlockEntity(pos) instanceof SpiritAnchorBlockEntity anchor) {
			anchor.holder().setOwner(player.getUUID());
		}
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!(player instanceof ServerPlayer server) || !(level instanceof ServerLevel serverLevel)
				|| !(level.getBlockEntity(pos) instanceof SpiritAnchorBlockEntity anchor)) {
			return InteractionResult.SUCCESS;
		}
		if (!RateGate.allow(server, "workers", 4)) {
			return InteractionResult.FAIL;
		}
		use(server, serverLevel, anchor, player.isShiftKeyDown());
		return InteractionResult.SUCCESS;
	}

	/** One use of the anchor (tests call this directly; the block asks RateGate first). Returns the last line it told the player. */
	public static Component use(ServerPlayer player, ServerLevel level, SpiritAnchorBlockEntity anchor, boolean sneaking) {
		WorkerDefinition.Spirit terms = Workers.catalog().get(GatheringShadeEntity.DEFINITION, WorkerDefinition.Spirit.class);
		if (!Workers.enabled() || terms == null) {
			return overlay(player, Component.translatable("message.jugcraft.concordance.disabled"));
		}
		long now = level.getGameTime();
		AgreementHolder holder = anchor.holder();
		AgreementHolder.Standing standing = holder.verify(level, now);
		Agreement agreement = holder.agreement();
		if (agreement == null) {
			if (!Workers.knows(player)) {
				return overlay(player, Component.translatable("message.jugcraft.concordance.workers.unknown"));
			}
			AgreementHolder.Sealed sealed = anchor.seal(level, player, terms, now);
			return say(player, sealed.outcome() == Pacts.Outcome.DONE ? Component.translatable("message.jugcraft.concordance.workers.sealed",
					terms.radius(), Workers.clock(terms.from()), Workers.clock(terms.to()), terms.quota()) : AgreementHolder.told(sealed.outcome()));
		}
		if (standing != AgreementHolder.Standing.VALID) {
			return overlay(player, ComposeText.show(Diagnostic.of(Code.CONFLICTING_AGREEMENT).text()));
		}
		boolean holds = !Authority.isStandIn(player) && agreement.holder().equals(player.getUUID());
		if (sneaking && holds) {
			return overlay(player, AgreementHolder.told(holder.toggle(level, player, now)));
		}
		if (holds && holder.spiritLost(level)) {
			return say(player, AgreementHolder.told(holder.recall(level, player, now)));
		}
		Status state = holder.state(level);
		player.sendSystemMessage(Component.translatable("message.jugcraft.concordance.workers.agreement", agreement.radius(),
				Workers.clock(agreement.from()), Workers.clock(agreement.to()), agreement.done(now), agreement.quota(),
				Component.translatable(state == null ? "message.jugcraft.concordance.workers.is_active" : "compose.jugcraft.worker.status." + state.id)));
		Entity spirit = anchor.spirit() == null ? null : level.getEntity(anchor.spirit());
		return say(player, spirit instanceof GatheringShadeEntity shade && !shade.isRemoved() ? shade.describe()
				: Component.translatable(holder.spiritLost(level) ? "message.jugcraft.concordance.workers.spirit_lost"
						: "message.jugcraft.concordance.workers.spirit_away"));
	}

	private static Component say(ServerPlayer player, Component line) {
		player.sendSystemMessage(line);
		return line;
	}

	private static Component overlay(ServerPlayer player, Component line) {
		player.sendOverlayMessage(line);
		return line;
	}
}
