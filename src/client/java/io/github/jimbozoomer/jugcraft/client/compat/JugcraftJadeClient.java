package io.github.jimbozoomer.jugcraft.client.compat;

import io.github.jimbozoomer.jugcraft.compat.jade.ConcordanceDataProvider;
import io.github.jimbozoomer.jugcraft.compat.jade.JugcraftJadePlugin;
import io.github.jimbozoomer.jugcraft.compat.jade.MachineDataProvider;
import io.github.jimbozoomer.jugcraft.concordance.KindledLanternItem;
import io.github.jimbozoomer.jugcraft.concordance.CircleAnchorBlock;
import io.github.jimbozoomer.jugcraft.concordance.LampwrightBenchBlock;
import io.github.jimbozoomer.jugcraft.concordance.LeyPylonBlock;
import io.github.jimbozoomer.jugcraft.concordance.LeyPylonBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.LumenSconceBlock;
import io.github.jimbozoomer.jugcraft.machine.MachineBlock;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.config.IPluginConfig;

/** Resolved only from Jade's client-registration callback. */
public final class JugcraftJadeClient implements JugcraftJadePlugin.ClientRegistration {
	@Override
	public void register(IWailaClientRegistration registration) {
		registration.registerBlockComponent(MachineTooltip.INSTANCE, MachineBlock.class);
		registration.registerBlockComponent(ConcordanceTooltip.BENCH, LampwrightBenchBlock.class);
		registration.registerBlockComponent(ConcordanceTooltip.SCONCE, LumenSconceBlock.class);
		registration.registerBlockComponent(ConcordanceTooltip.ANCHOR, CircleAnchorBlock.class);
		registration.registerBlockComponent(ConcordanceTooltip.PYLON, LeyPylonBlock.class);
	}

	private enum MachineTooltip implements IBlockComponentProvider {
		INSTANCE;

		@Override
		public Identifier getUid() {
			return MachineDataProvider.ID;
		}

		@Override
		public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
			accessor.getServerData().getCompound(MachineDataProvider.DATA_KEY).ifPresent(data -> {
				long capacity = data.getLongOr("capacity", 0);
				if (capacity > 0) {
					tooltip.add(Component.translatableWithFallback("tooltip.jugcraft.machine_energy",
							"Energy: %s / %s JE", data.getLongOr("energy", 0), capacity));
				}
				int duration = data.getIntOr("duration", 0);
				if (duration > 0) {
					int percent = (int) Math.clamp(100L * data.getIntOr("progress", 0) / duration, 0, 100);
					tooltip.add(Component.translatableWithFallback("tooltip.jugcraft.machine_progress",
							"Processing: %s%%", percent));
				}
			});
		}
	}

	/** A Circle Anchor's phase line, as the anchor itself says it (CircleAnchorBlockEntity.phaseText). */
	private static Component anchorPhase(String phase, CompoundTag data) {
		String key = "message.jugcraft.concordance.circle.phase." + phase;
		return switch (phase) {
			case "gathering" -> Component.translatable(key, data.getIntOr("joined", 0), "?");
			case "channeling" -> Component.translatable(key, data.getIntOr("done", 0) + 1, data.getIntOr("steps", 0));
			default -> Component.translatable(key);
		};
	}

	/** One structural fault, as CircleAnchorBlockEntity.faultText says it. */
	private static Component faultText(String role, String problem, String where) {
		Component roleName = Component.translatable("message.jugcraft.concordance.circle.role." + role);
		String key = "message.jugcraft.concordance.circle.fault." + problem;
		return switch (problem) {
			case "missing", "incompatible" -> Component.translatable(key, roleName, where);
			case "unloaded" -> Component.translatable(key, roleName);
			default -> Component.translatable(key, where);
		};
	}

	/** The Concordance's blocks: a study, notes waiting and Radiance held (ConcordanceDataProvider). */
	private enum ConcordanceTooltip implements IBlockComponentProvider {
		BENCH(ConcordanceDataProvider.BENCH),
		SCONCE(ConcordanceDataProvider.SCONCE),
		ANCHOR(ConcordanceDataProvider.ANCHOR),
		PYLON(ConcordanceDataProvider.PYLON);

		private final ConcordanceDataProvider provider;

		ConcordanceTooltip(ConcordanceDataProvider provider) {
			this.provider = provider;
		}

		@Override
		public Identifier getUid() {
			return provider.id;
		}

		@Override
		public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
			accessor.getServerData().getCompound(provider.dataKey).ifPresent(data -> {
				int study = data.getIntOr("study", -1);
				if (study >= 0) {
					tooltip.add(Component.translatable("tooltip.jugcraft.concordance.jade.study", study));
				}
				if (data.getIntOr("notes", 0) != 0) {
					tooltip.add(Component.translatable("tooltip.jugcraft.concordance.jade.notes"));
				}
				int radiance = data.getIntOr("radiance", -1);
				if (radiance >= 0) {
					tooltip.add(Component.translatable("tooltip.jugcraft.concordance.lantern.charge", radiance, KindledLanternItem.CAPACITY));
				}
				int ley = data.getIntOr("ley", -1);
				if (ley >= 0) {
					tooltip.add(Component.translatable("tooltip.jugcraft.concordance.jade.ley", ley, LeyPylonBlockEntity.CAPACITY));
				}
				String phase = data.getStringOr("phase", "");
				if (!phase.isEmpty()) {
					tooltip.add(anchorPhase(phase, data));
					int faults = data.getIntOr("faults", 0);
					for (int i = 0; i < Math.min(faults, ConcordanceDataProvider.SHOWN_FAULTS); i++) {
						String[] fault = data.getStringOr("fault" + i, "").split("\\|");
						if (fault.length == 3) {
							tooltip.add(faultText(fault[0], fault[1], fault[2]));
						}
					}
					if (faults > ConcordanceDataProvider.SHOWN_FAULTS) {
						tooltip.add(Component.literal("+" + (faults - ConcordanceDataProvider.SHOWN_FAULTS)));
					}
				}
			});
		}
	}
}
