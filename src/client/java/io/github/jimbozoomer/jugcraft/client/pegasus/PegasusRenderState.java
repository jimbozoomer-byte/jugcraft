package io.github.jimbozoomer.jugcraft.client.pegasus;

import net.minecraft.client.renderer.entity.state.EquineRenderState;

/** A vanilla horse's render state plus the Pegasus's smoothed wings and its saddle (docs/features/pegasus.md). */
public class PegasusRenderState extends EquineRenderState {
	/** 0 folded, 0.5 raised, 1 spread (PegasusFlight.wingPose). */
	public float wingOpen;
	/** Beat phase, radians, and stroke size 0..1. */
	public float wingPhase;
	public float wingAmp;
	/** Whether the vanilla saddle slot holds a saddle (the saddle layer draws it). */
	public boolean saddled;
	/** Whether the synced flight state is airborne. */
	public boolean flying;
	/** Whether its light pack is fitted (drawn while saddled too). */
	public boolean pack;
}
