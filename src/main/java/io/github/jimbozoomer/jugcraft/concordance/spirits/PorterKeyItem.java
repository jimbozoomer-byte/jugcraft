package io.github.jimbozoomer.jugcraft.concordance.spirits;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;

/**
 * The Porter Key (roadmap step 17): sets a Clockwork Porter's route. Used on a container it records it as the source,
 * then as the target (component {@code jugcraft:porter_route}); used on a porter its keeper's party commands, it gives
 * the porter that route. Sneaking with it on a container starts again.
 */
public class PorterKeyItem extends Item {
	public PorterKeyItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (!(context.getLevel() instanceof ServerLevel level) || !(context.getPlayer() instanceof ServerPlayer player)) {
			return InteractionResult.SUCCESS;
		}
		BlockPos pos = context.getClickedPos();
		if (!(level.getBlockEntity(pos) instanceof Container)) {
			return InteractionResult.PASS;
		}
		ItemStack key = context.getItemInHand();
		ClockworkPorterEntity.Route route = key.get(Workers.PORTER_ROUTE);
		String dimension = level.dimension().identifier().toString();
		if (route == null || player.isShiftKeyDown() || !route.dimension().equals(dimension) || !route.source().equals(route.target())) {
			key.set(Workers.PORTER_ROUTE, new ClockworkPorterEntity.Route(pos.immutable(), pos.immutable(), dimension));
			player.sendOverlayMessage(Component.translatable("message.jugcraft.concordance.workers.key_source", pos.getX(), pos.getY(), pos.getZ()));
		} else {
			key.set(Workers.PORTER_ROUTE, new ClockworkPorterEntity.Route(route.source(), pos.immutable(), dimension));
			player.sendOverlayMessage(Component.translatable("message.jugcraft.concordance.workers.key_target", pos.getX(), pos.getY(), pos.getZ()));
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
		if (!(target instanceof ClockworkPorterEntity porter) || !(player instanceof ServerPlayer server)) {
			return InteractionResult.PASS;
		}
		ClockworkPorterEntity.Route route = stack.get(Workers.PORTER_ROUTE);
		if (!porter.mayServe(server)) {
			server.sendOverlayMessage(Component.translatable("message.jugcraft.concordance.workers.not_yours"));
			return InteractionResult.FAIL;
		}
		if (route == null || route.source().equals(route.target())) {
			server.sendOverlayMessage(Component.translatable("message.jugcraft.concordance.workers.key_unset"));
			return InteractionResult.FAIL;
		}
		porter.setRoute(route);
		server.sendOverlayMessage(Component.translatable("message.jugcraft.concordance.workers.routed"));
		return InteractionResult.SUCCESS;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip,
			TooltipFlag flag) {
		ClockworkPorterEntity.Route route = stack.get(Workers.PORTER_ROUTE);
		if (route != null) {
			tooltip.accept(Component.translatable("tooltip.jugcraft.concordance.porter_key.route", route.source().toShortString(),
					route.source().equals(route.target()) ? "?" : route.target().toShortString()).withStyle(ChatFormatting.GOLD));
		}
		tooltip.accept(Component.translatable("tooltip.jugcraft.porter_key").withStyle(ChatFormatting.GRAY));
	}
}
