package io.github.jimbozoomer.jugcraft.compat.jade;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.concordance.JugcraftConcordance;
import io.github.jimbozoomer.jugcraft.concordance.KindledLanternItem;
import io.github.jimbozoomer.jugcraft.concordance.LampwrightBenchBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.LumenSconceBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

/**
 * The Concordance's blocks for Jade: the Lampwright's Bench (how far its study has come, whether notes wait for
 * someone, the Radiance in the lantern on it) and the Lumen Sconce (its Radiance). Bounded snapshots of the one block
 * looked at; they name no player, reveal no one's research and change nothing.
 */
public enum ConcordanceDataProvider implements IServerDataProvider<BlockAccessor> {
	BENCH("lampwright_bench"),
	SCONCE("lumen_sconce");

	public final Identifier id;
	public final String dataKey;

	ConcordanceDataProvider(String path) {
		this.id = Jugcraft.id(path);
		this.dataKey = Jugcraft.MOD_ID + ":" + path;
	}

	@Override
	public Identifier getUid() {
		return id;
	}

	@Override
	public void appendServerData(CompoundTag data, BlockAccessor accessor) {
		long now = accessor.getLevel().getGameTime();
		if (this == BENCH && accessor.getBlockEntity() instanceof LampwrightBenchBlockEntity bench) {
			data.put(dataKey, snapshot(bench, now));
		} else if (this == SCONCE && accessor.getBlockEntity() instanceof LumenSconceBlockEntity sconce) {
			data.put(dataKey, snapshot(sconce, now));
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

	public static CompoundTag snapshot(LumenSconceBlockEntity sconce, long now) {
		CompoundTag snapshot = new CompoundTag();
		snapshot.putInt("radiance", (int) sconce.remaining(now));
		return snapshot;
	}
}
