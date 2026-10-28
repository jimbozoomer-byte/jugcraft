package io.github.jimbozoomer.jugcraft.lair.sentinel;

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
import net.minecraft.world.phys.Vec3;

/**
 * The Cydonian Sentinel's loot, for each player paid for its fall (lair/LairRewards, which calls {@link #pay}): everyone
 * who qualified in its fight (lair/Encounter: hurt it or its sentries, or whose tamed creature did, or healed or warded
 * enough) and is alive within 64 blocks of its crater when it falls, and anyone who died in the fight or left the game
 * during it and is still away, paid once they are back. Each rolls {@code jugcraft:entities/cydonian_sentinel} for themselves (Cydonian Shards, a chance of one of
 * its trophies from {@code jugcraft:bosses/cydonian_sentinel}, the Lowfall Core and the Sentinel's Visor), a player's
 * first kill always brings a trophy (whoever has not yet earned Red Planet), the
 * {@value CydonianSentinelEntity#EXPERIENCE} experience is shared out, and everyone earns Red Planet. It goes straight
 * into their inventory, the rest at their feet, so nobody else can pick it up.
 */
public final class SentinelLoot {
	public static final ResourceKey<LootTable> TABLE = ResourceKey.create(Registries.LOOT_TABLE, Jugcraft.id("entities/cydonian_sentinel"));
	public static final ResourceKey<LootTable> TROPHIES = ResourceKey.create(Registries.LOOT_TABLE, Jugcraft.id("bosses/cydonian_sentinel"));
	/** The name its rewards are kept under when they wait for a player (lair/LairRewards): saved, so never renamed. */
	public static final String BOSS = "cydonian_sentinel";
	public static final String ADVANCEMENT = "red_planet";

	private SentinelLoot() {
	}

	/** Gives each participant their loot; returns what each got. */
	public static Map<ServerPlayer, List<ItemStack>> reward(ServerLevel level, CydonianSentinelEntity sentinel, List<ServerPlayer> participants) {
		Map<ServerPlayer, List<ItemStack>> given = new LinkedHashMap<>();
		if (participants.isEmpty()) {
			return given;
		}
		int share = Math.max(1, CydonianSentinelEntity.EXPERIENCE / participants.size());
		for (ServerPlayer player : participants) {
			given.put(player, pay(level, sentinel.position(), player, share));
		}
		return given;
	}

	/**
	 * Pays one player their loot from a fall at {@code origin}: rolled now, into their inventory (the rest at their feet),
	 * their {@code experience} share and the advancement. Rewards paid later (lair/LairRewards) come this way too.
	 */
	public static List<ItemStack> pay(ServerLevel level, Vec3 origin, ServerPlayer player, int experience) {
		List<ItemStack> loot = roll(level, origin, player);
		for (ItemStack stack : loot) {
			player.getInventory().placeItemBackInInventory(stack.copy(), Prediction.SERVER_ONLY);
		}
		player.giveExperiencePoints(experience);
		TrickOrTreat.award(player, ADVANCEMENT);
		player.sendSystemMessage(Component.translatable("message.jugcraft.cydonian_sentinel.loot"));
		return loot;
	}

	/** One participant's roll. */
	public static List<ItemStack> roll(ServerLevel level, CydonianSentinelEntity sentinel, ServerPlayer player) {
		return roll(level, sentinel.position(), player);
	}

	/** One participant's roll, for a fall at {@code origin}. */
	public static List<ItemStack> roll(ServerLevel level, Vec3 origin, ServerPlayer player) {
		List<ItemStack> loot = new ArrayList<>(table(level, TABLE, origin, player));
		if (firstKill(player) && loot.stream().noneMatch(SentinelLoot::trophy)) {
			loot.addAll(table(level, TROPHIES, origin, player));
		}
		return loot;
	}

	/** Whether {@code stack} is one of its trophies, the Corelance or the Redstar Cleaver. */
	public static boolean trophy(ItemStack stack) {
		return stack.is(JugcraftSentinel.corelance()) || stack.is(JugcraftSentinel.redstarCleaver());
	}

	private static List<ItemStack> table(ServerLevel level, ResourceKey<LootTable> key, Vec3 origin, ServerPlayer player) {
		LootTable table = level.getServer().reloadableRegistries().getLootTable(key);
		LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, origin)
				.withParameter(LootContextParams.THIS_ENTITY, player).create(LootContextParamSets.GIFT);
		return List.copyOf(table.getRandomItems(params));
	}

	/** Whether this would be {@code player}'s first kill: they have not yet earned Red Planet. */
	public static boolean firstKill(ServerPlayer player) {
		AdvancementHolder advancement = player.level().getServer().getAdvancements().get(Jugcraft.id(ADVANCEMENT));
		return advancement != null && !player.getAdvancements().getOrStartProgress(advancement).isDone();
	}
}
