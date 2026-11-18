package io.github.jimbozoomer.jugcraft.lair.dreadnought;

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
 * The Iron Dreadnought's loot, for each player paid for his fall (lair/LairRewards, which calls {@link #pay}): everyone
 * who qualified in his fight (lair/Encounter: hurt him or his Sparkers, or whose tamed creature did, or healed or warded
 * enough, or routed a discharge or worked at a fouled breaker) and is alive within 64 blocks of his trench when he falls,
 * and anyone who died in the fight or left the game during it and is still away, paid once they are back. Each rolls {@code jugcraft:entities/iron_dreadnought} for
 * themselves (Salvaged Coil, a chance of one of his trophies from {@code jugcraft:bosses/iron_dreadnought}, the Grounding
 * Strap and the Dreadnought Visor), a player's first kill always brings a trophy (whoever has not yet earned Grounded),
 * the {@value IronDreadnoughtEntity#EXPERIENCE} experience is shared out, and everyone earns Grounded. He is no Witching
 * Season boss: no seasonal extras. It goes straight into their inventory, the rest at their feet, so nobody else can pick
 * it up.
 */
public final class IronDreadnoughtLoot {
	public static final ResourceKey<LootTable> TABLE = ResourceKey.create(Registries.LOOT_TABLE, Jugcraft.id("entities/iron_dreadnought"));
	public static final ResourceKey<LootTable> TROPHIES = ResourceKey.create(Registries.LOOT_TABLE, Jugcraft.id("bosses/iron_dreadnought"));
	/** The name his rewards are kept under when they wait for a player (lair/LairRewards): saved, so never renamed. */
	public static final String BOSS = "iron_dreadnought";
	public static final String ADVANCEMENT = "grounded";

	private IronDreadnoughtLoot() {
	}

	/** Gives each participant their loot; returns what each got. */
	public static Map<ServerPlayer, List<ItemStack>> reward(ServerLevel level, IronDreadnoughtEntity dreadnought, List<ServerPlayer> participants) {
		Map<ServerPlayer, List<ItemStack>> given = new LinkedHashMap<>();
		if (participants.isEmpty()) {
			return given;
		}
		int share = Math.max(1, IronDreadnoughtEntity.EXPERIENCE / participants.size());
		for (ServerPlayer player : participants) {
			given.put(player, pay(level, dreadnought.position(), player, share));
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
		player.sendSystemMessage(Component.translatable("message.jugcraft.iron_dreadnought.loot"));
		return loot;
	}

	/** One participant's roll. */
	public static List<ItemStack> roll(ServerLevel level, IronDreadnoughtEntity dreadnought, ServerPlayer player) {
		return roll(level, dreadnought.position(), player);
	}

	/** One participant's roll, for a fall at {@code origin}. */
	public static List<ItemStack> roll(ServerLevel level, Vec3 origin, ServerPlayer player) {
		List<ItemStack> loot = new ArrayList<>(table(level, TABLE, origin, player));
		if (firstKill(player) && loot.stream().noneMatch(IronDreadnoughtLoot::trophy)) {
			loot.addAll(table(level, TROPHIES, origin, player));
		}
		return loot;
	}

	/** Whether {@code stack} is one of his trophies, the Dynamo Halberd or the Piston Hammer. */
	public static boolean trophy(ItemStack stack) {
		return stack.is(JugcraftDreadnought.dynamoHalberd()) || stack.is(JugcraftDreadnought.pistonHammer());
	}

	private static List<ItemStack> table(ServerLevel level, ResourceKey<LootTable> key, Vec3 origin, ServerPlayer player) {
		LootTable table = level.getServer().reloadableRegistries().getLootTable(key);
		LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, origin)
				.withParameter(LootContextParams.THIS_ENTITY, player).create(LootContextParamSets.GIFT);
		return List.copyOf(table.getRandomItems(params));
	}

	/** Whether this would be {@code player}'s first kill: they have not yet earned Grounded. */
	public static boolean firstKill(ServerPlayer player) {
		AdvancementHolder advancement = player.level().getServer().getAdvancements().get(Jugcraft.id(ADVANCEMENT));
		return advancement != null && !player.getAdvancements().getOrStartProgress(advancement).isDone();
	}
}
