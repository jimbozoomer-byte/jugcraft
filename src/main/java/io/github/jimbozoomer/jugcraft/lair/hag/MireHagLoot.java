package io.github.jimbozoomer.jugcraft.lair.hag;

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
 * The Mire Hag's loot, for each player paid for her fall (lair/LairRewards, which calls {@link #pay}): everyone who
 * qualified in her fight (lair/Encounter: hurt her, her Likenesses or the Root Knot, or whose tamed creature did, or
 * healed or warded enough, or relit a lamp that showed her) and is alive within 64 blocks of her marsh when she falls,
 * and anyone who died in the fight or left the game during it and is still away, paid once they are back. Each rolls {@code jugcraft:entities/mire_hag} for themselves (Hagmoss, a chance of one of her trophies
 * from {@code jugcraft:bosses/mire_hag}, the Hag's Eye and the Hag's Shawl), a player's first kill always brings a trophy
 * (whoever has not yet earned Seen Through), the {@value MireHagEntity#EXPERIENCE} experience is shared out, and everyone
 * earns Seen Through. She is no Witching Season boss: no seasonal extras. It goes straight into their inventory, the rest
 * at their feet, so nobody else can pick it up.
 */
public final class MireHagLoot {
	public static final ResourceKey<LootTable> TABLE = ResourceKey.create(Registries.LOOT_TABLE, Jugcraft.id("entities/mire_hag"));
	public static final ResourceKey<LootTable> TROPHIES = ResourceKey.create(Registries.LOOT_TABLE, Jugcraft.id("bosses/mire_hag"));
	/** The name her rewards are kept under when they wait for a player (lair/LairRewards): saved, so never renamed. */
	public static final String BOSS = "mire_hag";
	public static final String ADVANCEMENT = "seen_through";

	private MireHagLoot() {
	}

	/** Gives each participant their loot; returns what each got. */
	public static Map<ServerPlayer, List<ItemStack>> reward(ServerLevel level, MireHagEntity hag, List<ServerPlayer> participants) {
		Map<ServerPlayer, List<ItemStack>> given = new LinkedHashMap<>();
		if (participants.isEmpty()) {
			return given;
		}
		int share = Math.max(1, MireHagEntity.EXPERIENCE / participants.size());
		for (ServerPlayer player : participants) {
			given.put(player, pay(level, hag.position(), player, share));
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
		player.sendSystemMessage(Component.translatable("message.jugcraft.mire_hag.loot"));
		return loot;
	}

	/** One participant's roll. */
	public static List<ItemStack> roll(ServerLevel level, MireHagEntity hag, ServerPlayer player) {
		return roll(level, hag.position(), player);
	}

	/** One participant's roll, for a fall at {@code origin}. */
	public static List<ItemStack> roll(ServerLevel level, Vec3 origin, ServerPlayer player) {
		List<ItemStack> loot = new ArrayList<>(table(level, TABLE, origin, player));
		if (firstKill(player) && loot.stream().noneMatch(MireHagLoot::trophy)) {
			loot.addAll(table(level, TROPHIES, origin, player));
		}
		return loot;
	}

	/** Whether {@code stack} is one of her trophies, the Hagthorn or the Bogfang. */
	public static boolean trophy(ItemStack stack) {
		return stack.is(JugcraftHag.hagthorn()) || stack.is(JugcraftHag.bogfang());
	}

	private static List<ItemStack> table(ServerLevel level, ResourceKey<LootTable> key, Vec3 origin, ServerPlayer player) {
		LootTable table = level.getServer().reloadableRegistries().getLootTable(key);
		LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, origin)
				.withParameter(LootContextParams.THIS_ENTITY, player).create(LootContextParamSets.GIFT);
		return List.copyOf(table.getRandomItems(params));
	}

	/** Whether this would be {@code player}'s first kill: they have not yet earned Seen Through. */
	public static boolean firstKill(ServerPlayer player) {
		AdvancementHolder advancement = player.level().getServer().getAdvancements().get(Jugcraft.id(ADVANCEMENT));
		return advancement != null && !player.getAdvancements().getOrStartProgress(advancement).isDone();
	}
}
