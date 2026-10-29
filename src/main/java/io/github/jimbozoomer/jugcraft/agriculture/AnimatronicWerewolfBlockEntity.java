package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Animatronic Werewolf's clockwork, in its lower block ({@link ClockworkFigureBlockEntity}): while it runs it howls
 * once in every {@value AnimatronicWerewolfBlock#HOWL_EVERY} ticks, as its arms reach the top of their swing.
 */
public class AnimatronicWerewolfBlockEntity extends ClockworkFigureBlockEntity {
	public AnimatronicWerewolfBlockEntity(BlockPos pos, BlockState state) {
		super(JugcraftAgriculture.ANIMATRONIC_WEREWOLF_ENTITY, pos, state);
	}

	@Override
	protected void play(ServerLevel level, BlockPos pos, int played) {
		if (played % AnimatronicWerewolfBlock.HOWL_EVERY == AnimatronicWerewolfBlock.howlStart() + AnimatronicWerewolfBlock.RAISE_TICKS) {
			level.playSound(null, pos.above(), BuiltInRegistries.SOUND_EVENT.getOptional(Identifier.withDefaultNamespace("entity.wolf.howl"))
					.orElse(SoundEvents.ZOMBIE_AMBIENT), SoundSource.RECORDS, AnimatronicWerewolfBlock.VOLUME, AnimatronicWerewolfBlock.PITCH);
		}
	}
}
