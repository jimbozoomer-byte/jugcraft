package local.peepo.client;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
public final class PeepoState extends LivingEntityRenderState { public final ItemStackRenderState held = new ItemStackRenderState(); public final ItemStackRenderState food = new ItemStackRenderState(); public boolean holdingLight; public boolean sitting; public boolean sleeping; public boolean eating; public boolean wheelRunning; public float eatingTime; public boolean blushing; public boolean pumpkin; public boolean jughead;
    public local.peepo.WorkAnimation work=local.peepo.WorkAnimation.NONE;
    public float workPhase;
}

