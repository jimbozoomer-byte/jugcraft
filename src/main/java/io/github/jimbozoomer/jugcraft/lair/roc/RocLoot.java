package io.github.jimbozoomer.jugcraft.lair.roc;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.TrickOrTreat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * The Storm Roc's loot, per participant (everyone who hurt him or his fledglings, or stood by the rod that grounded him,
 * and is still near the summit when he falls): each rolls {@code jugcraft:entities/storm_roc} for themselves (Roc
 * Feathers, a chance of one of his trophies from {@code jugcraft:bosses/storm_roc}, the Weather-Vane Lens and the Roc's
 * Crest), a player's first kill always brings a trophy (whoever has not yet earned Eye of the Storm), the
 * {@value StormRocEntity#EXPERIENCE} experience is shared out, and everyone earns Eye of the Storm. It goes straight into
 * their inventory, the rest at their feet, so nobody else can pick it up.
 */
public final class RocLoot {
	public static final ResourceKey<LootTable> TABLE = ResourceKey.create(Registries.LOOT_TABLE, Jugcraft.id("entities/storm_roc"));
	public static final ResourceKey<LootTable> TROPHIES = ResourceKey.create(Registries.LOOT_TABLE, Jugcraft.id("bosses/storm_roc"));
	public static final String ADVANCEMENT = "eye_of_the_storm";

	private RocLoot() {
	}

	/** Gives each participant their loot; returns what each got. */
	public static Map<ServerPlayer, List<ItemStack>> reward(ServerLevel level, StormRocEntity roc, List<ServerPlayer> participants) {
		Map<ServerPlayer, List<ItemStack>> given = new LinkedHashMap<>();
		if (participants.isEmpty()) {
			return given;
		}
		int share = Math.max(1, StormRocEntity.EXPERIENCE / participants.size());
		for (ServerPlayer player : participants) {
			List<ItemStack> loot = roll(level, roc, player);
			for (ItemStack stack : loot) {
				player.getInventory().placeItemBackInInventory(stack.copy(), Prediction.SERVER_ONLY);
			}
			player.giveExperiencePoints(share);
			TrickOrTreat.award(player, ADVANCEMENT);
			player.sendSystemMessage(Component.translatable("message.jugcraft.storm_roc.loot"));
			given.put(player, loot);
		}
		return given;
	}

	/** One participant's roll. */
	public static List<ItemStack> roll(ServerLevel level, StormRocEntity roc, ServerPlayer player) {
		List<ItemStack> loot = new ArrayList<>(table(level, TABLE, roc, player));
		if (firstKill(player) && loot.stream().noneMatch(RocLoot::trophy)) {
			loot.addAll(table(level, TROPHIES, roc, player));
		}
		return loot;
	}

	/** Whether {@code stack} is one of his trophies, the Stormcaller or the Galefeather. */
	public static boolean trophy(ItemStack stack) {
		return stack.is(JugcraftRoc.stormcaller()) || stack.is(JugcraftRoc.galefeather());
	}

	private static List<ItemStack> table(ServerLevel level, ResourceKey<LootTable> key, StormRocEntity roc, ServerPlayer player) {
		LootTable table = level.getServer().reloadableRegistries().getLootTable(key);
		LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, roc.position())
				.withParameter(LootContextParams.THIS_ENTITY, player).create(LootContextParamSets.GIFT);
		return List.copyOf(table.getRandomItems(params));
	}

	/** Whether this would be {@code player}'s first kill: they have not yet earned Eye of the Storm. */
	public static boolean firstKill(ServerPlayer player) {
		AdvancementHolder advancement = player.level().getServer().getAdvancements().get(Jugcraft.id(ADVANCEMENT));
		return advancement != null && !player.getAdvancements().getOrStartProgress(advancement).isDone();
	}
}
