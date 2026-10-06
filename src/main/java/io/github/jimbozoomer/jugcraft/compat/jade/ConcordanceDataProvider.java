package io.github.jimbozoomer.jugcraft.compat.jade;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.concordance.JugcraftConcordance;
import io.github.jimbozoomer.jugcraft.concordance.KindledLanternItem;
import io.github.jimbozoomer.jugcraft.concordance.LampwrightBenchBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

/**
 * The Lampwright's Bench for Jade: how far its study has come, whether notes wait for someone, and the Radiance in the
 * lantern on it. A bounded snapshot of the one bench looked at; it names no player and changes nothing.
 */
public enum ConcordanceDataProvider implements IServerDataProvider<BlockAccessor> {
	INSTANCE;

	public static final Identifier ID = Jugcraft.id("lampwright_bench");
	public static final String DATA_KEY = "jugcraft:lampwright_bench";

	@Override
	public Identifier getUid() {
		return ID;
	}

	@Override
	public void appendServerData(CompoundTag data, BlockAccessor accessor) {
		if (accessor.getBlockEntity() instanceof LampwrightBenchBlockEntity bench) {
			data.put(DATA_KEY, snapshot(bench, accessor.getLevel().getGameTime()));
		}
	}

	public static CompoundTag snapshot(LampwrightBenchBlockEntity bench, long now) {
		CompoundTag snapshot = new CompoundTag();
		if (bench.studying()) {
			snapshot.putInt("study", Math.clamp(100 * bench.progress() / LampwrightBenchBlockEntity.STUDY_TICKS, 0, 100));
		}
		if (bench.notesWaiting()) {
			snapshot.putInt("notes", 1);
		}
		ItemStack work = bench.getItem(LampwrightBenchBlockEntity.WORK);
		if (work.is(JugcraftConcordance.KINDLED_LANTERN)) {
			snapshot.putInt("radiance", KindledLanternItem.remaining(work, now));
		}
		return snapshot;
	}
}
