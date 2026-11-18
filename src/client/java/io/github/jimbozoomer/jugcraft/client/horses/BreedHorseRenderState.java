package io.github.jimbozoomer.jugcraft.client.horses;

import net.minecraft.client.renderer.entity.state.EquineRenderState;

/** A vanilla horse's render state plus whether a breed horse wears its saddle and saddlebags (docs/features/horse-breeds.md). */
public class BreedHorseRenderState extends EquineRenderState {
	/** Whether the vanilla saddle slot holds a saddle (the saddle layer draws it). */
	public boolean saddled;
	/** Whether saddlebags are fitted (synced; the saddlebag layer draws them). */
	public boolean bags;
}
