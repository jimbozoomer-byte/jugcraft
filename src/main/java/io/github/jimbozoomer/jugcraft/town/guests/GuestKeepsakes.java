package io.github.jimbozoomer.jugcraft.town.guests;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.util.Prediction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.WrittenBookContent;
import org.jspecify.annotations.Nullable;

/**
 * Milestone rewards (worker D): a letter (a written book) or a modest keepsake (a named vanilla item with lore). Never
 * money, never a valuable item, never a plain stack: each carries a custom name, lore and a {@value #MARKER} custom-data
 * marker, so a guest never takes one as a gift (favors take plain stacks only), and the town shops buy none of these
 * items.
 *
 * <p><b>Two tones.</b> {@link Tone#FRIENDSHIP} is for everyone: first gift, first errand, first outing and the affection
 * thresholds, friendly in tone for every guest. {@link Tone#ROMANCE} (the ceremony's bouquet, anniversary letters) is made
 * only while {@link #romance} holds: the guest's {@code mayMarry()} is true and the ledger has the two married to each
 * other (owner rule, 11 November: romance is for unambiguous adults only). Held guests never get it.
 *
 * <p><b>Real people.</b> A fictional guest signs its own letter with original, generic, friendly text. A real-person
 * guest's letter comes from "The Town Post" and only notes game-world events: nothing is signed by, quoted from or
 * claimed about the person.
 */
public final class GuestKeepsakes {
	/** The custom-data key every keepsake carries: {@code {guest, key, set}}. */
	public static final String MARKER = "jugcraft_keepsake";

	public enum Tone {
		FRIENDSHIP, ROMANCE
	}

	/** The pressed flowers (one per guest, by a fixed hash of its id). */
	static final List<Item> FLOWERS = List.of(Items.POPPY, Items.CORNFLOWER, Items.BLUE_ORCHID, Items.DANDELION, Items.PINK_TULIP);
	/** The keepsake caps' dyes (one per guest). */
	static final int[] DYES = {0xB02E26, 0x3C44AA, 0x5E7C16, 0xF9801D, 0x8932B8, 0x169C9C, 0xF38BAA, 0xFED83D};
	/** Every item a reward is ever made of: all modest vanilla items. */
	public static final List<Item> ITEMS = List.of(Items.WRITTEN_BOOK, Items.POPPY, Items.CORNFLOWER, Items.BLUE_ORCHID, Items.DANDELION,
			Items.PINK_TULIP, Items.LEATHER_HELMET, Items.PAPER, Items.LILY_OF_THE_VALLEY);
	/** The longest title a written book keeps. */
	private static final int TITLE_MAX = 32;

	private GuestKeepsakes() {
	}

	/** Whether romantic material may be made for the player and guest now: mayMarry, and married to each other in the ledger. */
	public static boolean romance(MinecraftServer server, UUID player, String guest) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		GuestLedger ledger = GuestLedger.get(server);
		return entry != null && entry.mayMarry() && guest.equals(ledger.spouseOf(player)) && player.equals(ledger.spouseOf(guest));
	}

	/** The tone a milestone key's reward belongs to. */
	static Tone tone(String key) {
		return key.startsWith("ceremony_") || key.startsWith("anniversary_") ? Tone.ROMANCE : Tone.FRIENDSHIP;
	}

	/**
	 * The reward for milestone {@code key}, or empty when none may be made: a romance reward while {@link #romance} does
	 * not hold, an unknown key or guest.
	 */
	static ItemStack reward(MinecraftServer server, ServerPlayer player, String guest, String key) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		if (entry == null || tone(key) == Tone.ROMANCE && !romance(server, player.getUUID(), guest)) {
			return ItemStack.EMPTY;
		}
		boolean real = entry.origin() == GuestRoster.Origin.REAL_PERSON;
		Component name = Component.literal(entry.name());
		Component you = player.getName();
		if (key.startsWith("anniversary_")) {
			String[] parts = key.split("_");
			String days = parts.length == 3 ? parts[2] : "?";
			return letter(entry, key, Tone.ROMANCE, "Anniversary Letter", real
					? text("anniversary.town", "Dear %1$s,\n\nThe Church of Notch and the town hall wish you and the town guest %2$s a happy "
							+ "anniversary: %3$s days married in town. May there be many more.\n\nThe Town Post", you, name, days)
					: text("anniversary", "Dear %1$s,\n\n%3$s days married today. Thank you for every walk through town together, and for "
							+ "every lantern you lit on the way home. Here is to many more.\n\n%2$s", you, name, days));
		}
		if (key.startsWith("ceremony_")) {
			return keepsake(new ItemStack(Items.LILY_OF_THE_VALLEY), entry, key, Tone.ROMANCE,
					Component.translatableWithFallback("item.jugcraft.guest_keepsake.bouquet", "Ceremony Bouquet"),
					Component.translatableWithFallback("item.jugcraft.guest_keepsake.bouquet.lore",
							"From your ceremony with %s at the Church of Notch.", name));
		}
		return switch (key) {
			case "first_gift" -> letter(entry, key, Tone.FRIENDSHIP, "A Note of Thanks", real
					? text("first_gift.town", "Dear %1$s,\n\nThe town hall has noted your first gift to the town guest %2$s. Thank you for "
							+ "making visitors welcome.\n\nThe Town Post", you, name)
					: text("first_gift", "Dear %1$s,\n\nThank you for the gift. It was kind of you to think of me while I am a guest in "
							+ "town.\n\n%2$s", you, name));
			case "first_quest" -> letter(entry, key, Tone.FRIENDSHIP, "Thanks for the Errand", real
					? text("first_quest.town", "Dear %1$s,\n\nThe town hall has noted your first errand for the town guest %2$s. Helpers "
							+ "like you keep the town running.\n\nThe Town Post", you, name)
					: text("first_quest", "Dear %1$s,\n\nThank you for running that errand. The town feels a little smaller and a lot "
							+ "friendlier with helpers like you.\n\n%2$s", you, name));
			case "affection_500" -> letter(entry, key, Tone.FRIENDSHIP, "A Friendly Letter", real
					? text("affection_500.town", "Dear %1$s,\n\nThe town hall notes that you and the town guest %2$s have become good "
							+ "friends in town. Thank you for your many visits.\n\nThe Town Post", you, name)
					: text("affection_500", "Dear %1$s,\n\nWe have shared a good many afternoons in town now. I am glad we met, and glad "
							+ "you keep coming back.\n\n%2$s", you, name));
			case "affection_1000" -> letter(entry, key, Tone.FRIENDSHIP, "To a Close Friend", real
					? text("affection_1000.town", "Dear %1$s,\n\nThe town hall notes that you are among the town guest %2$s's closest "
							+ "friends in town. The town is glad of you both.\n\nThe Town Post", you, name)
					: text("affection_1000", "Dear %1$s,\n\nYou have become one of my closest friends in town. Thank you for every "
							+ "visit, every errand and every gift.\n\n%2$s", you, name));
			case "affection_250" -> {
				Item flower = FLOWERS.get(pick(guest, FLOWERS.size()));
				yield keepsake(new ItemStack(flower), entry, key, Tone.FRIENDSHIP,
						Component.translatableWithFallback("item.jugcraft.guest_keepsake.pressed_flower", "Pressed %s",
								new ItemStack(flower).getHoverName()),
						Component.translatableWithFallback("item.jugcraft.guest_keepsake.pressed_flower.lore",
								"A keepsake of your friendship with %s.", name));
			}
			case "affection_750" -> {
				ItemStack cap = new ItemStack(Items.LEATHER_HELMET);
				cap.set(DataComponents.DYED_COLOR, new DyedItemColor(DYES[pick(guest, DYES.length)]));
				yield keepsake(cap, entry, key, Tone.FRIENDSHIP,
						Component.translatableWithFallback("item.jugcraft.guest_keepsake.cap", "Friendship Cap"),
						Component.translatableWithFallback("item.jugcraft.guest_keepsake.cap.lore",
								"A keepsake of your friendship with %s.", name));
			}
			case "first_outing" -> keepsake(new ItemStack(Items.PAPER), entry, key, Tone.FRIENDSHIP,
					Component.translatableWithFallback("item.jugcraft.guest_keepsake.sketch", "Outing Sketch"),
					Component.translatableWithFallback("item.jugcraft.guest_keepsake.sketch.lore",
							"A little sketch map of your first outing with %s.", name));
			default -> ItemStack.EMPTY;
		};
	}

	/**
	 * Hands {@code stack} to the player: into the inventory, the rest dropped at their feet when it is full (the repo's
	 * give-or-drop pattern, {@code Inventory.placeItemBackInInventory}), so nothing is lost.
	 */
	static void give(ServerPlayer player, ItemStack stack) {
		if (stack.isEmpty()) {
			return;
		}
		player.getInventory().placeItemBackInInventory(stack, Prediction.SERVER_ONLY);
		player.getInventory().setChanged();
	}

	/** The tone a stack's keepsake marker names, or null when it is no keepsake. */
	public static @Nullable Tone toneOf(ItemStack stack) {
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		if (data == null) {
			return null;
		}
		CompoundTag marker = data.copyTag().getCompoundOrEmpty(MARKER);
		String set = marker.getStringOr("set", "");
		return set.equals("friendship") ? Tone.FRIENDSHIP : set.equals("romance") ? Tone.ROMANCE : null;
	}

	/** The milestone key a stack's keepsake marker names, or "" when it is no keepsake. */
	public static String keyOf(ItemStack stack) {
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		return data == null ? "" : data.copyTag().getCompoundOrEmpty(MARKER).getStringOr("key", "");
	}

	// ---------------------------------------------------------------- making them

	private static ItemStack letter(GuestRoster.Guest entry, String key, Tone set, String title, Component page) {
		boolean real = entry.origin() == GuestRoster.Origin.REAL_PERSON;
		String author = real ? "The Town Post" : entry.name();
		List<Filterable<Component>> pages = new ArrayList<>();
		pages.add(Filterable.passThrough(page));
		ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
		book.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(Filterable.passThrough(fit(title)), author, 0, pages, true));
		return mark(book, entry, key, set, null, Component.translatableWithFallback("item.jugcraft.guest_keepsake.letter.lore",
				real ? "Kept from the town post about %s." : "A letter from %s.", Component.literal(entry.name())));
	}

	private static ItemStack keepsake(ItemStack stack, GuestRoster.Guest entry, String key, Tone set, Component name, Component lore) {
		return mark(stack, entry, key, set, name, lore);
	}

	private static ItemStack mark(ItemStack stack, GuestRoster.Guest entry, String key, Tone set, @Nullable Component name, Component lore) {
		if (name != null) {
			stack.set(DataComponents.CUSTOM_NAME, name);
		}
		stack.set(DataComponents.LORE, new ItemLore(List.of(lore)));
		CompoundTag marker = new CompoundTag();
		marker.putString("guest", entry.id());
		marker.putString("key", key);
		marker.putString("set", set == Tone.ROMANCE ? "romance" : "friendship");
		CompoundTag tag = new CompoundTag();
		tag.put(MARKER, marker);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		return stack;
	}

	private static Component text(String key, String english, Object... args) {
		return Component.translatableWithFallback("book.jugcraft.guest_letter." + key, english, args);
	}

	/** {@code title} cut to what a written book keeps. */
	private static String fit(String title) {
		return title.length() <= TITLE_MAX ? title : title.substring(0, TITLE_MAX);
	}

	/** A fixed pick in [0, size) by FNV-1a over the guest id: the same on every machine and run. */
	static int pick(String guest, int size) {
		long hash = 0xCBF29CE484222325L;
		for (byte b : guest.getBytes(StandardCharsets.UTF_8)) {
			hash ^= b & 0xFF;
			hash *= 0x100000001B3L;
		}
		return (int) Long.remainderUnsigned(hash, size);
	}
}
