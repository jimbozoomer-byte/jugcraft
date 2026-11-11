package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.client.ExchangeScreen.Plate;
import io.github.jimbozoomer.jugcraft.town.guests.GuestAppearances;
import io.github.jimbozoomer.jugcraft.town.guests.GuestBalance;
import io.github.jimbozoomer.jugcraft.town.guests.GuestFavors;
import io.github.jimbozoomer.jugcraft.town.guests.GuestMenu;
import io.github.jimbozoomer.jugcraft.town.guests.GuestRoster;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * A conversation with a town guest ({@link GuestMenu}), in the trade look. It only shows what the server synced and
 * sends button ids: every key sends {@code GuestMenu.button(action, revision)} with the revision the server last synced,
 * and the server decides everything (distance, identity, items, caps, quests, proposals, cooldowns). Keys are dimmed
 * from the synced values only as a convenience, and after a click until the server's next revision arrives, so a key
 * is not sent twice by accident; the server refuses stale and doubled clicks itself.
 *
 * <p>It shows the guest's name, series (or "real person") and the note that the appearance is a placeholder; affection
 * and what is left of it today; gifts left today and the gift key; today's errand; and companionship: for a guest who
 * may not marry, a neutral note and no proposal controls; otherwise a proposal (asked, then confirmed naming the
 * diamond it costs, or cancelled), marriage status on both sides, divorce (asked, then confirmed or cancelled) and both
 * remarriage waits. A guest that is unknown or pending shows only that it is not receiving visitors.
 *
 * <p>The Gifts key (top right) opens a gift list in place of the rest: the guest's favourites, labelled as a game
 * preference with their bonus, every gift any guest accepts with its value (the hovered one named), and the daily
 * limits, all read from {@link GuestFavors}. It is display only: opening or closing it sends nothing.
 */
public final class GuestScreen extends AbstractContainerScreen<GuestMenu> implements ScreenKit.Fitted {
	private static final int WIDTH = 300, HEIGHT = 214;
	/** Ticks a click waits for the server's next revision before its keys light again. */
	private static final int SETTLE_TICKS = 20;
	private static final int BAR_BACK = 0xFF171E21;

	/** The gift list's panel, from the body's corner; the gift grid's columns, column width and row height. */
	private static final int LIST_X = 8, LIST_Y = 34, LIST_W = 284, LIST_H = 174, GRID_X = 14, GRID_Y = 84, COLUMNS = 12,
			COLUMN = 23, ROW = 30;

	private final Map<GuestMenu.Action, Plate> keys = new EnumMap<>(GuestMenu.Action.class);
	private @Nullable Plate giftListKey;
	private boolean giftList;
	private int sentRevision = -1;
	private int settling;

	public GuestScreen(GuestMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, WIDTH, HEIGHT);
	}

	@Override
	protected void init() {
		super.init();
		inventoryLabelY = 10000;
		clearWidgets();
		keys.clear();
		key(GuestMenu.Action.GIFT, 10, 80, 136, ui("gift", "Give held item"));
		key(GuestMenu.Action.QUEST_ACCEPT, 160, 102, 62, ui("accept", "Accept"));
		key(GuestMenu.Action.QUEST_TURN_IN, 226, 102, 62, ui("turn_in", "Turn in"));
		key(GuestMenu.Action.PROPOSE, 10, 172, 136, ui("propose", "Propose"));
		key(GuestMenu.Action.PROPOSE_CONFIRM, 10, 172, 136, proposalConfirmLabel());
		key(GuestMenu.Action.PROPOSE_CANCEL, 150, 172, 70, ui("cancel", "Cancel"));
		key(GuestMenu.Action.DIVORCE, 10, 172, 136, ui("divorce", "Divorce"));
		key(GuestMenu.Action.DIVORCE_CONFIRM, 10, 172, 136, ui("divorce_confirm", "Confirm divorce"));
		key(GuestMenu.Action.DIVORCE_CANCEL, 150, 172, 70, ui("cancel", "Cancel"));
		giftListKey = addRenderableWidget(new Plate(leftPos + 236, topPos + 4, 56, 14, ui("gift_list", "Gifts").getString(), Plate.Kind.WOOD,
				b -> {
					// Display only: nothing is sent.
					giftList = !giftList;
					updateKeys();
				}));
		updateKeys();
	}

	private void key(GuestMenu.Action action, int x, int y, int width, Component label) {
		Plate plate = addRenderableWidget(new Plate(leftPos + x, topPos + y, width, 18, label.getString(), Plate.Kind.BRASS, b -> send(action)));
		keys.put(action, plate);
	}

	/** Sends the click with the revision the server last synced; nothing else. */
	private void send(GuestMenu.Action action) {
		if (minecraft == null || minecraft.gameMode == null || settling()) {
			return;
		}
		int revision = menu.get(GuestMenu.REVISION);
		minecraft.gameMode.handleInventoryButtonClick(menu.containerId, GuestMenu.button(action, revision));
		sentRevision = revision;
		settling = SETTLE_TICKS;
		updateKeys();
	}

	/** A click is on its way and the server has not synced a new revision yet. */
	private boolean settling() {
		return settling > 0 && menu.get(GuestMenu.REVISION) == sentRevision;
	}

	@Override
	protected void containerTick() {
		super.containerTick();
		if (settling > 0) {
			settling--;
		}
		updateKeys();
	}

	private void updateKeys() {
		boolean open = available() && !settling();
		boolean shown = available() && !giftList;
		if (giftListKey != null) {
			giftListKey.visible = available();
			giftListKey.active = available();
			giftListKey.setMessage(giftList ? ui("gift_list.close", "Back") : ui("gift_list", "Gifts"));
		}
		boolean mayMarry = menu.get(GuestMenu.MAY_MARRY) == 1;
		boolean ours = menu.get(GuestMenu.PLAYER_SPOUSE) == GuestMenu.EACH_OTHER;
		boolean proposing = menu.get(GuestMenu.PROPOSAL_PENDING) == 1;
		boolean divorcing = menu.get(GuestMenu.DIVORCE_PENDING) == 1;
		GuestFavors.QuestState quest = menu.questState();
		ItemStack wanted = menu.questItem();

		show(GuestMenu.Action.GIFT, shown, open && menu.get(GuestMenu.GIFTS_LEFT) > 0 && menu.get(GuestMenu.AFFECTION_LEFT_TODAY) > 0
				&& menu.get(GuestMenu.AFFECTION) < menu.get(GuestMenu.MAX_AFFECTION));
		show(GuestMenu.Action.QUEST_ACCEPT, shown, open && quest == GuestFavors.QuestState.OFFERED);
		show(GuestMenu.Action.QUEST_TURN_IN, shown, open && quest == GuestFavors.QuestState.ACTIVE && !wanted.isEmpty()
				&& menu.get(GuestMenu.QUEST_CARRIED) >= wanted.getCount());
		show(GuestMenu.Action.PROPOSE, shown && mayMarry && !ours && !proposing, open && menu.get(GuestMenu.MAY_PROPOSE) == 1);
		show(GuestMenu.Action.PROPOSE_CONFIRM, shown && mayMarry && !ours && proposing, open);
		show(GuestMenu.Action.PROPOSE_CANCEL, shown && mayMarry && !ours && proposing, open);
		show(GuestMenu.Action.DIVORCE, shown && ours && !divorcing, open);
		show(GuestMenu.Action.DIVORCE_CONFIRM, shown && ours && divorcing, open);
		show(GuestMenu.Action.DIVORCE_CANCEL, shown && ours && divorcing, open);
	}

	private void show(GuestMenu.Action action, boolean visible, boolean active) {
		Plate plate = keys.get(action);
		if (plate != null) {
			plate.visible = visible;
			plate.active = visible && active;
		}
	}

	/** The guest is in the roster and not pending. */
	private boolean available() {
		GuestRoster.Guest guest = menu.guest();
		return guest != null && guest.available();
	}

	// ---------------------------------------------------------------- what the screen shows (for tests)

	/** The key for {@code action} (it may be hidden or dimmed); null before the screen is laid out. */
	public @Nullable Button key(GuestMenu.Action action) {
		return keys.get(action);
	}

	/** Whether the screen shows the guest as not receiving visitors. */
	public boolean showsUnavailable() {
		return !available();
	}

	/** The key that opens and closes the gift list; null before the screen is laid out. */
	public @Nullable Button giftListKey() {
		return giftListKey;
	}

	/** Whether the gift list is shown. */
	public boolean showsGiftList() {
		return giftList && available();
	}

	/** The favourites the gift list shows (none while it is closed). */
	public List<Item> shownFavourites() {
		GuestRoster.Guest guest = menu.guest();
		return showsGiftList() && guest != null ? GuestFavors.favourites(guest.id()) : List.of();
	}

	/** Where the gift list is drawn, in GUI pixels. */
	public ScreenRectangle giftListArea() {
		return new ScreenRectangle(leftPos + LIST_X, topPos + LIST_Y, LIST_W, LIST_H);
	}

	// ---------------------------------------------------------------- drawing

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
		extractBackground(g, mx, my, delta);
		super.extractRenderState(g, mx, my, delta);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float delta) {
		TradeLook.frame(g, leftPos, topPos, leftPos + imageWidth, topPos + imageHeight);
		TradeLook.rule(g, leftPos + 8, topPos + 30, leftPos + 292);
		if (showsGiftList()) {
			TradeLook.tray(g, leftPos + LIST_X, topPos + LIST_Y, leftPos + LIST_X + LIST_W, topPos + LIST_Y + LIST_H);
		} else if (available()) {
			TradeLook.tray(g, leftPos + 154, topPos + 34, leftPos + 292, topPos + 126);
			TradeLook.rule(g, leftPos + 8, topPos + 132, leftPos + 292);
		}
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor g, int mx, int my) {
		GuestRoster.Guest guest = menu.guest();
		String name = ScreenKit.fit(font, guest != null ? guest.name() : title.getString(), 150);
		g.text(font, name, 10, 8, TradeLook.GOLD, false);
		if (guest == null || !guest.available()) {
			int y = 60;
			for (FormattedCharSequence line : font.split(ui("unavailable", "This guest is not receiving visitors."), 270)) {
				g.text(font, line, (imageWidth - font.width(line)) / 2, y, TradeLook.CREAM, false);
				y += 11;
			}
			return;
		}
		String series = guest.origin() == GuestRoster.Origin.REAL_PERSON || guest.series().isEmpty()
				? ui("real_person", "Real person").getString() : guest.series();
		// The series sits between the name and the Gifts key.
		series = ScreenKit.fit(font, series, Math.max(0, 230 - (18 + font.width(name))));
		g.text(font, series, 230 - font.width(series), 8, TradeLook.TAN, false);
		GuestAppearances.Appearance look = GuestAppearances.of(guest.id());
		Component note = look == null ? GuestAppearances.note() : look.note();
		g.text(font, ScreenKit.fit(font, capitalised(note.getString()), 280), 10, 19, TradeLook.DIM, false);
		if (giftList) {
			giftList(g, guest, mx - leftPos, my - topPos);
			return;
		}

		// Affection and gifts.
		int affection = menu.get(GuestMenu.AFFECTION), most = Math.max(1, menu.get(GuestMenu.MAX_AFFECTION));
		g.text(font, ui("affection", "Affection"), 10, 36, TradeLook.TAN, false);
		String value = ui("affection_value", "%s / %s", affection, most).getString();
		g.text(font, value, 146 - font.width(value), 36, TradeLook.CREAM, false);
		g.fill(10, 47, 146, 53, BAR_BACK);
		g.fill(10, 47, 10 + 136 * Math.clamp(affection, 0, most) / most, 53, TradeLook.GAIN);
		g.text(font, ScreenKit.fit(font, ui("affection_today", "Affection left today: %s", menu.get(GuestMenu.AFFECTION_LEFT_TODAY)).getString(), 136),
				10, 57, TradeLook.CREAM, false);
		g.text(font, ScreenKit.fit(font, ui("gifts_today", "Gifts left today: %s", menu.get(GuestMenu.GIFTS_LEFT)).getString(), 136),
				10, 68, TradeLook.CREAM, false);
		int y = 102;
		for (FormattedCharSequence line : font.split(ui("gift_hint", "Hold the gift in your main hand."), 136)) {
			g.text(font, line, 10, y, TradeLook.DIM, false);
			y += 10;
		}

		// Today's errand.
		g.text(font, ui("errand", "Today's errand"), 160, 38, TradeLook.GOLD, false);
		GuestFavors.QuestState quest = menu.questState();
		Component state = switch (quest) {
			case NONE -> ui("quest.none", "No errand today");
			case OFFERED -> ui("quest.offered", "Offered: will you help?");
			case ACTIVE -> ui("quest.active", "In progress");
			case DONE -> ui("quest.done", "Done for today");
		};
		g.text(font, ScreenKit.fit(font, state.getString(), 128), 160, 50, quest == GuestFavors.QuestState.DONE ? TradeLook.GAIN : TradeLook.CREAM, false);
		ItemStack wanted = menu.questItem();
		if (!wanted.isEmpty()) {
			g.item(wanted, 160, 62);
			g.text(font, ScreenKit.fit(font, wanted.getHoverName().getString(), 106), 180, 66, TradeLook.CREAM, false);
			g.text(font, ui("quest.carried", "Carried: %s / %s", menu.get(GuestMenu.QUEST_CARRIED), wanted.getCount()), 160, 86,
					menu.get(GuestMenu.QUEST_CARRIED) >= wanted.getCount() ? TradeLook.GAIN : TradeLook.CREAM, false);
		}

		// Companionship.
		g.text(font, ui("companionship", "Companionship"), 10, 138, TradeLook.GOLD, false);
		boolean ours = menu.get(GuestMenu.PLAYER_SPOUSE) == GuestMenu.EACH_OTHER;
		if (menu.get(GuestMenu.MAY_MARRY) != 1 && !ours) {
			y = 150;
			for (FormattedCharSequence line : font.split(ui("companionship_only",
					"Friendship and companionship only. There is no proposal with this guest."), 280)) {
				g.text(font, line, 10, y, TradeLook.TAN, false);
				y += 10;
			}
			return;
		}
		Component status = switch (menu.get(GuestMenu.GUEST_SPOUSE)) {
			case GuestMenu.EACH_OTHER -> ui("spouse.yours", "Married to you.");
			case GuestMenu.SOMEONE_ELSE -> ui("spouse.other", "Married to someone else.");
			default -> ui("spouse.none", "Not married.");
		};
		String line = status.getString();
		if (menu.get(GuestMenu.PLAYER_SPOUSE) == GuestMenu.SOMEONE_ELSE) {
			line += " " + ui("you_married_other", "You are married to someone else.").getString();
		}
		g.text(font, ScreenKit.fit(font, line, 280), 10, 150, TradeLook.CREAM, false);
		g.text(font, ScreenKit.fit(font, ui("cooldowns", "Your wait to remarry: %s. Their wait: %s.",
				duration(menu.get(GuestMenu.PLAYER_COOLDOWN_MINUTES)), duration(menu.get(GuestMenu.GUEST_COOLDOWN_MINUTES))).getString(), 280),
				10, 161, TradeLook.TAN, false);
		boolean proposing = menu.get(GuestMenu.PROPOSAL_PENDING) == 1;
		boolean divorcing = menu.get(GuestMenu.DIVORCE_PENDING) == 1;
		ItemStack gift = new ItemStack(GuestBalance.PROPOSAL_ITEM, GuestBalance.PROPOSAL_COUNT);
		int seconds = GuestBalance.CONFIRM_TICKS / 20;
		Component hint;
		if (ours) {
			hint = divorcing ? ui("hint.divorce_pending", "Confirm within %s seconds. Afterwards neither of you may remarry for %s days.",
					seconds, GuestBalance.REMARRIAGE_COOLDOWN_MILLIS / (24L * 60 * 60 * 1000))
					: ui("hint.married", "Divorce asks you to confirm first.");
		} else if (proposing) {
			g.item(gift, 226, 173);
			hint = ui("hint.propose_pending", "Confirm within %s seconds. The %s is taken only if the marriage goes ahead.", seconds,
					gift.getHoverName());
		} else {
			hint = ui("hint.propose", "A proposal needs full affection, both of you free to marry, and %s %s.", GuestBalance.PROPOSAL_COUNT,
					gift.getHoverName());
		}
		y = 194;
		for (FormattedCharSequence hintLine : font.split(hint, 280)) {
			g.text(font, hintLine, 10, y, TradeLook.DIM, false);
			y += 10;
		}
	}

	/**
	 * The gift list, over the body under the header: the guest's favourites (a game preference, with their bonus), every
	 * gift guests accept with its value, the one under the mouse named, and the daily limits. {@code mx}, {@code my}: the
	 * mouse, from the body's corner.
	 */
	private void giftList(GuiGraphicsExtractor g, GuestRoster.Guest guest, int mx, int my) {
		List<Item> favourites = GuestFavors.favourites(guest.id());
		int left = LIST_X + 6, width = LIST_W - 12;
		g.text(font, ScreenKit.fit(font, ui("gift_list.favourites", "Favourite gifts (game preference, +%s affection)",
				GuestFavors.FAVOURITE_BONUS).getString(), width), left, LIST_Y + 4, TradeLook.GOLD, false);
		for (int i = 0; i < favourites.size(); i++) {
			ItemStack stack = new ItemStack(favourites.get(i));
			int x = left + i * 92;
			TradeLook.slot(g, x, LIST_Y + 16);
			g.item(stack, x, LIST_Y + 16);
			g.text(font, ScreenKit.fit(font, stack.getHoverName().getString(), 70), x + 20, LIST_Y + 20, TradeLook.CREAM, false);
		}
		g.text(font, ui("gift_list.accepted", "Accepted gifts"), left, LIST_Y + 38, TradeLook.TAN, false);
		Component hovered = null;
		for (int i = 0; i < GuestFavors.GIFTS.size(); i++) {
			GuestFavors.Gift gift = GuestFavors.GIFTS.get(i);
			int x = GRID_X + (i % COLUMNS) * COLUMN, y = GRID_Y + (i / COLUMNS) * ROW;
			boolean favourite = favourites.contains(gift.item());
			if (favourite) {
				g.outline(x - 2, y - 2, 20, 20, TradeLook.GOLD);
			}
			ItemStack stack = new ItemStack(gift.item());
			g.item(stack, x, y);
			String value = "+" + gift.value();
			g.text(font, value, x + 8 - font.width(value) / 2, y + 18, favourite ? TradeLook.GOLD : TradeLook.DIM, false);
			if (mx >= x - 2 && mx < x + COLUMN - 2 && my >= y - 2 && my < y + ROW - 2) {
				hovered = favourite
						? ui("gift_list.item_favourite", "%s: +%s affection, +%s more as a favourite", stack.getHoverName(), gift.value(),
								GuestFavors.FAVOURITE_BONUS)
						: ui("gift_list.item", "%s: +%s affection", stack.getHoverName(), gift.value());
			}
		}
		int y = GRID_Y + ((GuestFavors.GIFTS.size() + COLUMNS - 1) / COLUMNS) * ROW;
		g.text(font, ScreenKit.fit(font, (hovered != null ? hovered : ui("gift_list.point", "Point at a gift for its name.")).getString(), width),
				left, y, hovered != null ? TradeLook.CREAM : TradeLook.DIM, false);
		y += 14;
		for (FormattedCharSequence line : font.split(ui("gift_list.limits",
				"Each day, for each guest: up to %s gifts and %s affection (gifts and the errand together).", GuestFavors.GIFTS_PER_DAY,
				GuestFavors.DAILY_AFFECTION_CAP), width)) {
			g.text(font, line, left, y, TradeLook.TAN, false);
			y += 10;
		}
		g.text(font, ScreenKit.fit(font, ui("gift_list.hand", "Give one at a time, held in your main hand.").getString(), width), left, y + 4,
				TradeLook.DIM, false);
	}

	private static Component proposalConfirmLabel() {
		return ui("propose_confirm", "Confirm: give %s %s", GuestBalance.PROPOSAL_COUNT,
				new ItemStack(GuestBalance.PROPOSAL_ITEM).getHoverName());
	}

	/** Whole days and hours (rounded up) of a wait in minutes; "none" when there is none. */
	static Component duration(int minutes) {
		if (minutes <= 0) {
			return ui("duration.none", "none");
		}
		int hours = (minutes + 59) / 60;
		return ui("duration", "%sd %sh", hours / 24, hours % 24);
	}

	private static String capitalised(String text) {
		return text.isEmpty() ? text : Character.toUpperCase(text.charAt(0)) + text.substring(1);
	}

	private static Component ui(String key, String english, Object... args) {
		return Component.translatableWithFallback("screen.jugcraft.guest." + key, english, args);
	}

	@Override
	public ScreenRectangle panelArea() {
		return new ScreenRectangle(leftPos - 3, topPos - 3, imageWidth + 6, imageHeight + 6);
	}
}
