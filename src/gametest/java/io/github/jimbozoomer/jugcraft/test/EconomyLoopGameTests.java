package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.concordance.assay.AlchemicalWorkshopBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.assay.Assaying;
import io.github.jimbozoomer.jugcraft.concordance.assay.TabletOutputs;
import io.github.jimbozoomer.jugcraft.concordance.equivalence.Assay;
import io.github.jimbozoomer.jugcraft.concordance.equivalence.Material;
import io.github.jimbozoomer.jugcraft.town.Jugs;
import io.github.jimbozoomer.jugcraft.town.ShopMenu;
import io.github.jimbozoomer.jugcraft.town.TownCommissions;
import io.github.jimbozoomer.jugcraft.town.TownShops;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

/**
 * The 12 November economy audit on a live server (docs/features/convenience-transmutation.md): essence formed by the
 * tablet never becomes Jugs at a town shop, the reproduced cobblestone/bucket loop stays closed, ordinary sales settle
 * once and replays fail, and every collector fuel upgrade costs at least the value it adds. Data-level checks are in
 * scripts/tests/test_economy_loops.py.
 */
public class EconomyLoopGameTests {
	/** The pre-audit General Store offers that paid Jugs for formable supplies. */
	private static final List<TownShops.Offer> REMOVED = List.of(
			new TownShops.Offer("minecraft:white_wool", 32, 1, null), new TownShops.Offer("minecraft:oak_log", 64, 1, null),
			new TownShops.Offer("minecraft:spruce_log", 64, 1, null), new TownShops.Offer("minecraft:birch_log", 64, 1, null),
			new TownShops.Offer("minecraft:cobblestone", 64, 1, null));

	/** A real connected survival player (vanilla's in-level mock always reports creative). */
	private static ServerPlayer survivor(GameTestHelper helper) {
		var level = helper.getLevel();
		var profile = new com.mojang.authlib.GameProfile(UUID.randomUUID(), "economy-loop-player");
		var player = new ServerPlayer(level.getServer(), level, profile, net.minecraft.server.level.ClientInformation.createDefault());
		var connection = new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
		new io.netty.channel.embedded.EmbeddedChannel(connection);
		level.getServer().getPlayerList().placeNewPlayer(connection, player, net.minecraft.server.network.CommonListenerCookie.createInitial(profile, false));
		TestPlayers.track(helper, player);
		player.setGameMode(GameType.SURVIVAL);
		helper.assertTrue(!player.isCreative() && !player.isSpectator(), "Fixture is an ordinary survival player");
		return player;
	}

	private static Item item(String id) {
		return BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
	}

	private static boolean formable(Item item) {
		return new ItemStack(item).is(TabletOutputs.ALLOWED);
	}

	private static Material material(Item item) {
		return AlchemicalWorkshopBlockEntity.material(new ItemStack(item));
	}

	/**
	 * The reproduced loop: 384 essence forms 384 cobblestone, six old 64-for-1 sales made 6 Jugs, a 6-Jug bucket dissolves
	 * for 768 essence. The sales are now refused, so no Jug appears, and dissolving the cobblestone returns exactly the 384.
	 */
	@GameTest(maxTicks = 20)
	public void formedCobblestoneCannotFundTheBucketLoop(GameTestHelper helper) {
		ServerPlayer player = survivor(helper);
		MinecraftServer server = helper.getLevel().getServer();
		try {
			Material cobblestone = material(Items.COBBLESTONE), bucket = material(Items.BUCKET);
			helper.assertTrue(Assay.form(cobblestone, 1) == 1 && Assay.dissolve(bucket, 1) == 768,
					"Audited prices: cobblestone forms for 1 essence, a bucket dissolves for 768");
			Assaying.setBalance(player, 384);
			for (int i = 0; i < 384; i++) {
				helper.assertTrue(Assaying.form(player, new ItemStack(Items.COBBLESTONE)), "Cobblestone is a permitted supply: " + i);
			}
			helper.assertTrue(Assaying.balance(player) == 0 && player.getInventory().countItem(Items.COBBLESTONE) == 384,
					"384 essence formed 384 cobblestone");
			for (int lap = 0; lap < 6; lap++) {
				helper.assertTrue(!ShopMenu.sell(player, REMOVED.get(4)), "The town refuses formed cobblestone, sale " + lap);
			}
			helper.assertTrue(Jugs.balance(server, player.getUUID()) == 0 && player.getInventory().countItem(Items.COBBLESTONE) == 384,
					"No Jug was paid and no cobblestone taken");
			TownShops.Offer bucketOffer = TownShops.get().shop("general").sells().stream()
					.filter(o -> o.item().equals("minecraft:bucket")).findFirst().orElseThrow();
			helper.assertTrue(bucketOffer.price() == 6 && !ShopMenu.buy(player, bucketOffer), "Without Jugs there is no 6-Jug bucket to dissolve");
			long returned = 0;
			for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
				ItemStack stack = player.getInventory().getItem(slot);
				long grains;
				while (stack.is(Items.COBBLESTONE) && (grains = Assaying.dissolve(player, stack, cobblestone)) > 0) {
					returned += grains;
				}
			}
			helper.assertTrue(returned == 384 && Assaying.balance(player) == 384 && Jugs.balance(server, player.getUUID()) == 0,
					"The round trip conserves essence exactly: " + returned + " returned, " + Assaying.balance(player) + " held");
		} finally {
			server.getPlayerList().remove(player);
		}
		helper.succeed();
	}

	/** Every formable supply is refused at any price, through every old offer and a generous one; live data buys none. */
	@GameTest(maxTicks = 20)
	public void townRefusesFormableSuppliesAtAnyPrice(GameTestHelper helper) {
		ServerPlayer player = survivor(helper);
		MinecraftServer server = helper.getLevel().getServer();
		try {
			for (TownShops.Offer offer : REMOVED) {
				Item supply = offer.itemType();
				helper.assertTrue(formable(supply) && ShopMenu.transmutable(supply), "Audited offer is a permitted tablet supply: " + offer.item());
				player.getInventory().clearContent();
				player.getInventory().add(new ItemStack(supply, 64));
				boolean old = ShopMenu.sell(player, offer);
				boolean generous = ShopMenu.sell(player, new TownShops.Offer(offer.item(), 1, 1000, null));
				helper.assertTrue(!old && !generous && Jugs.balance(server, player.getUUID()) == 0
						&& player.getInventory().countItem(supply) == 64, "The town pays nothing for " + offer.item());
			}
			// Laundering a dyed supply back to white wool (the wool extractor, or white dye) reaches the same refusal.
			helper.assertTrue(formable(item("minecraft:red_wool")) && formable(item("minecraft:white_wool")), "Both wool forms are tablet supplies");
			for (TownShops.Shop shop : TownShops.get().shops.values()) {
				for (TownShops.Offer offer : shop.buys()) {
					helper.assertTrue(!formable(offer.itemType()), shop.id() + " buys no formable supply: " + offer.item());
				}
			}
			for (TownCommissions.Order order : TownCommissions.ORDERS) {
				helper.assertTrue(!formable(order.itemType()), "Commission " + order.id() + " asks for no formable supply");
			}
		} finally {
			server.getPlayerList().remove(player);
		}
		helper.succeed();
	}

	/** An ordinary sale pays once for the whole count; a replay without the goods, or a short count, pays nothing. */
	@GameTest(maxTicks = 20)
	public void ordinarySalesSettleOnceAndReplaysFail(GameTestHelper helper) {
		ServerPlayer player = survivor(helper);
		MinecraftServer server = helper.getLevel().getServer();
		try {
			TownShops.Offer iron = TownShops.get().shop("general").buys().stream()
					.filter(o -> o.item().equals("minecraft:iron_ingot")).findFirst().orElseThrow();
			helper.assertTrue(iron.count() == 16 && iron.price() == 2 && !formable(Items.IRON_INGOT), "Live offer: 16 iron ingots for 2 Jugs");
			player.getInventory().add(new ItemStack(Items.IRON_INGOT, 16));
			helper.assertTrue(ShopMenu.sell(player, iron), "The first sale settles");
			helper.assertTrue(!ShopMenu.sell(player, iron), "Replaying the sale with no iron pays nothing");
			player.getInventory().add(new ItemStack(Items.IRON_INGOT, 15));
			helper.assertTrue(!ShopMenu.sell(player, iron), "A short count pays nothing");
			helper.assertTrue(Jugs.balance(server, player.getUUID()) == 2 && player.getInventory().countItem(Items.IRON_INGOT) == 15,
					"Exactly one payment, exactly 16 ingots taken");
			player.getInventory().add(new ItemStack(Items.IRON_INGOT, 1));
			helper.assertTrue(ShopMenu.sell(player, iron) && Jugs.balance(server, player.getUUID()) == 4
					&& player.getInventory().countItem(Items.IRON_INGOT) == 0, "Two whole counts, two payments: Jugs follow the goods");
		} finally {
			server.getPlayerList().remove(player);
		}
		helper.succeed();
	}

	/** Collector fuel upgrades charge max(1, form(next) - dissolve(current)): never less than the value they add. */
	@GameTest(maxTicks = 20)
	public void collectorFuelUpgradesCostWhatTheyAdd(GameTestHelper helper) {
		String[] fuels = AlchemicalWorkshopBlockEntity.FUELS;
		helper.assertTrue(fuels.length == 4 && fuels[0].equals("minecraft:coal"), "The audited fuel ladder");
		for (int stage = 0; stage + 1 < fuels.length; stage++) {
			Material current = Assaying.catalog().material(fuels[stage]), next = Assaying.catalog().material(fuels[stage + 1]);
			long price = Math.max(1, Assay.form(next, 1) - Assay.dissolve(current, 1));
			helper.assertTrue(Assay.dissolve(current, 1) + price >= Assay.dissolve(next, 1),
					fuels[stage + 1] + ": a coal-fed collector cannot dissolve more than it paid (" + price + ")");
			helper.assertTrue(!formable(item(fuels[stage + 1])), fuels[stage + 1] + " stays outside the convenience catalogue");
		}
		helper.succeed();
	}
}
