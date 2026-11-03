package io.github.jimbozoomer.jugcraft.agriculture;

import local.peepo.DisplayCompanion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Unticked companion settings; native party-display servings remain in the block state. */
public final class ServingDisplayBlockEntity extends BlockEntity {
    public final DisplayCompanion companion=new DisplayCompanion(this);
    public ServingDisplayBlockEntity(BlockPos pos,BlockState state){super(JugcraftAgriculture.SERVING_DISPLAY_ENTITY,pos,state);}
    @Override protected void saveAdditional(ValueOutput out){super.saveAdditional(out);companion.save(out);}
    @Override protected void loadAdditional(ValueInput in){super.loadAdditional(in);companion.load(in);}
}
