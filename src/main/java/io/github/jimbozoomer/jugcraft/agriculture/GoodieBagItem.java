package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.List;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * A Goodie Bag (Halloween Party Table, part 5; tools/party_table.py GOODIE_BAG): a clear cellophane treat bag tied with
 * curling ribbon, the party's favour for its guests to take home. Used, it is torn open and gives what it was filled
 * with ({@link #CONTENTS}, the same treats its recipe takes, so nothing is gained), into the inventory or at the
 * player's feet.
 */
public class GoodieBagItem extends Item {
	/** What a bag holds: tools/party_table.py GOODIE_BAG contents, which its recipe puts in. */
	public static final List<String> CONTENTS = List.of("candy_corn", "candy_corn", "ghost_meringue", "witch_finger");

	public GoodieBagItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!level.isClientSide()) {
			player.getItemInHand(hand).consume(1, player);
			for (String id : CONTENTS) {
				ItemStack treat = new ItemStack(JugcraftAgriculture.item(id));
				if (!player.getInventory().add(treat)) {
					Block.popResource(level, player.blockPosition(), treat);
				}
			}
			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.PLAYERS, 0.8F, 1.2F);
		}
		return InteractionResult.SUCCESS;
	}
}
