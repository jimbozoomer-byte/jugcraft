package io.github.jimbozoomer.jugcraft.lair.tyrant;

import io.github.jimbozoomer.jugcraft.weapons.DescribedItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * The Quench Flask (docs/features/cinder-tyrant-concordance.md): a stoppered flask of meltwater and snow, thrown as a
 * splash potion is. Where it breaks it splashes {@value ThrownQuenchFlask#RADIUS} blocks round ({@link ThrownQuenchFlask#splash}).
 * The ordinary player's preparation for the Cinder Tyrant's kiln, made from snow at a crafting table; the Rimekeepers'
 * Quench does the like for one creature, again and again.
 */
public class QuenchFlaskItem extends DescribedItem {
	public QuenchFlaskItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SPLASH_POTION_THROW, SoundSource.PLAYERS, 0.5F,
				0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
		if (level instanceof ServerLevel server) {
			ThrownQuenchFlask flask = new ThrownQuenchFlask(server, player, stack.copyWithCount(1));
			flask.shootFromRotation(player, player.getXRot(), player.getYRot(), -20.0F, 0.5F, 1.0F);
			server.addFreshEntity(flask);
		}
		player.awardStat(Stats.ITEM_USED.get(this));
		stack.consume(1, player);
		return InteractionResult.SUCCESS;
	}
}
