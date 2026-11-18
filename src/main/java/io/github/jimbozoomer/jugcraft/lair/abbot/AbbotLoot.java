package io.github.jimbozoomer.jugcraft.lair.abbot;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.TrickOrTreat;
import java.util.ArrayList;
import java.util.List;
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
 * The Glass Abbot's loot, for each player paid for his fall (lair/LairRewards, which calls {@link #pay}): everyone who
 * qualified in his fight (lair/Encounter: hurt him or his images, or whose tamed creature did, healed or warded others
 * enough, or turned a mirror for his light to come home) and is alive near the chapel when he falls, and anyone who died
 * in the fight or left the game during it, paid once they are back. Each rolls {@code jugcraft:entities/glass_abbot} for
 * themselves when paid (Prism Shards, a chance of the Glass Reliquary and of the Abbot's Cowl); a player's first win
 * always brings the reliquary (whoever has not yet earned Seen Clearly), the {@value GlassAbbotEntity#EXPERIENCE}
 * experience is shared out among everyone paid, now or later, and everyone earns Seen Clearly. It goes straight into their
 * inventory, the rest at their feet, so nobody else can pick it up.
 */
public final class AbbotLoot {
	public static final ResourceKey<LootTable> TABLE = ResourceKey.create(Registries.LOOT_TABLE, Jugcraft.id("entities/glass_abbot"));
	/** The name his rewards are kept under when they wait for a player (lair/LairRewards). */
	public static final String BOSS = "glass_abbot";
	public static final String ADVANCEMENT = "seen_clearly";

	private AbbotLoot() {
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
		player.sendSystemMessage(Component.translatable("message.jugcraft.glass_abbot.loot"));
		return loot;
	}

	/** One participant's roll, for a fall at {@code origin}: a first win always holds the reliquary. */
	public static List<ItemStack> roll(ServerLevel level, Vec3 origin, ServerPlayer player) {
		List<ItemStack> loot = new ArrayList<>(table(level, origin, player));
		if (firstKill(player) && loot.stream().noneMatch(stack -> stack.is(JugcraftAbbot.GLASS_RELIQUARY))) {
			loot.add(new ItemStack(JugcraftAbbot.GLASS_RELIQUARY));
		}
		return loot;
	}

	private static List<ItemStack> table(ServerLevel level, Vec3 origin, ServerPlayer player) {
		LootTable table = level.getServer().reloadableRegistries().getLootTable(TABLE);
		LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, origin)
				.withParameter(LootContextParams.THIS_ENTITY, player).create(LootContextParamSets.GIFT);
		return List.copyOf(table.getRandomItems(params));
	}

	/** Whether this would be {@code player}'s first win: they have not yet earned Seen Clearly. */
	public static boolean firstKill(ServerPlayer player) {
		AdvancementHolder advancement = player.level().getServer().getAdvancements().get(Jugcraft.id(ADVANCEMENT));
		return advancement != null && !player.getAdvancements().getOrStartProgress(advancement).isDone();
	}
}
