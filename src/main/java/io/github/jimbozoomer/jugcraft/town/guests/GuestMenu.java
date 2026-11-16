package io.github.jimbozoomer.jugcraft.town.guests;

import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import java.util.UUID;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * A player's conversation with a town guest (coordinator's seam; worker C draws its screen). Vanilla menu
 * synchronization only: the client sends nothing but a button id, and every value it shows is computed on the server.
 *
 * <p>Each button id is an {@link Action} and the player's current revision ({@link #button}). The server acts only while
 * the player is alive, not a spectator, in the guest's level within {@link GuestBalance#INTERACT_RANGE} of the guest's
 * canonical entity, and only on a click carrying the current revision; any accepted click, done or refused, moves the
 * revision on, and opening any guest menu moves it on too, so a click from an older view, or the same click sent twice,
 * is refused. The revision and the gap between actions belong to the player ({@link GuestService#session}), not to this
 * menu. The revision only stops stale and doubled clicks: it wraps, and the client knows it, so it is no security token.
 * Every economic and proposal rule (items, caps, quests done, pending confirmations, cooldowns) is checked against the
 * ledger's and favors' own server state on every action, and reopening a menu resets none of it. The container button
 * packet carries the id as a VarInt (26.3's ServerboundContainerButtonClickPacket: CONTAINER_ID, then VAR_INT; Codex's
 * pinned javap evidence, build/nov11-guest-button-packet-review.txt), so the revision has 14 bits, the most the 16-bit
 * synced field holds as a positive value. Guests are public: no land-claim check here.
 */
public final class GuestMenu extends AbstractContainerMenu {
	/** What a player may ask; the ordinal is the low 4 bits of a button id. */
	public enum Action {
		GIFT, QUEST_ACCEPT, QUEST_TURN_IN, PROPOSE, PROPOSE_CONFIRM, PROPOSE_CANCEL, DIVORCE, DIVORCE_CONFIRM, DIVORCE_CANCEL,
		/** A {@link GuestFeature} option: its flattened index is carried above the revision ({@link #option}). */
		OPTION
	}

	/** The synced fields (16-bit values). */
	public static final int AFFECTION = 0, MAX_AFFECTION = 1, GUEST_SPOUSE = 2, PLAYER_SPOUSE = 3, PLAYER_COOLDOWN_MINUTES = 4,
			GUEST_COOLDOWN_MINUTES = 5, QUEST_STATE = 6, QUEST_ITEM = 7, QUEST_COUNT = 8, QUEST_CARRIED = 9, GIFTS_LEFT = 10,
			REVISION = 11, DIVORCE_PENDING = 12, MAY_PROPOSE = 13, AFFECTION_LEFT_TODAY = 14, PROPOSAL_PENDING = 15, MAY_MARRY = 16,
			OPTIONS_AVAILABLE = 17, FIELDS = 17 + GuestFeatures.MAX_OPTIONS / 16;
	/** Bits of a button id: 4 of action, then {@link #REVISIONS}' 14 of revision, then (for {@link Action#OPTION}) the option. */
	private static final int REVISION_SHIFT = 4, OPTION_SHIFT = 18;
	/** {@link #GUEST_SPOUSE} and {@link #PLAYER_SPOUSE}: unmarried, married to each other, married to someone else. */
	public static final int NOBODY = 0, EACH_OTHER = 1, SOMEONE_ELSE = 2;
	/** Revisions wrap at 14 bits, so the synced field (16 bits) stays positive; a button id is 4 bits of action above them. */
	public static final int REVISIONS = 1 << 14;

	/** What the client is told when the menu opens: the guest's entity (network id) and roster id. */
	public record Opening(int entity, String guest) {
		public static final StreamCodec<RegistryFriendlyByteBuf, Opening> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.VAR_INT, Opening::entity, ByteBufCodecs.STRING_UTF8, Opening::guest, Opening::new);
	}

	private final Opening opening;
	private final ContainerData data;
	private final @Nullable ServerPlayer owner;
	private final @Nullable Townsfolk npc;

	/** The client's copy. */
	public GuestMenu(int id, Inventory inventory, Opening opening) {
		this(id, opening, null, null);
	}

	private GuestMenu(int id, Opening opening, @Nullable ServerPlayer owner, @Nullable Townsfolk npc) {
		super(GuestService.MENU, id);
		this.opening = opening;
		this.owner = owner;
		this.npc = npc;
		this.data = owner == null ? new SimpleContainerData(FIELDS) : new Live(owner, opening.guest());
		addDataSlots(data);
	}

	/** Opens the conversation with {@code npc}, the guest {@code guest}. Server side. */
	public static void open(ServerPlayer player, Townsfolk npc, String guest) {
		GuestService.session(player.getUUID()).invalidate();
		player.openMenu(new ExtendedMenuProvider<Opening>() {
			@Override
			public Opening getScreenOpeningData(ServerPlayer p) {
				return new Opening(npc.getId(), guest);
			}

			@Override
			public Component getDisplayName() {
				return npc.getDisplayName();
			}

			@Override
			public AbstractContainerMenu createMenu(int id, Inventory inventory, Player p) {
				return new GuestMenu(id, new Opening(npc.getId(), guest), player, npc);
			}
		});
	}

	/** The button id for {@code action} at {@code revision} (the client sends the revision it last saw). */
	public static int button(Action action, int revision) {
		return action.ordinal() | (Math.floorMod(revision, REVISIONS) << REVISION_SHIFT);
	}

	/** The button id for feature option {@code index} ({@link GuestFeatures}) at {@code revision}. */
	public static int option(int index, int revision) {
		return button(Action.OPTION, revision) | (index << OPTION_SHIFT);
	}

	/** Whether option {@code index} is offered now, as last synced. */
	public boolean optionAvailable(int index) {
		return index >= 0 && index < GuestFeatures.MAX_OPTIONS && ((data.get(OPTIONS_AVAILABLE + index / 16) & 0xFFFF) >>> (index % 16) & 1) == 1;
	}

	@Override
	public boolean stillValid(Player player) {
		if (owner == null) {
			return true;
		}
		GuestRoster.Guest guest = GuestRoster.get(opening.guest());
		return player == owner && owner.isAlive() && !owner.isSpectator() && guest != null && guest.available() && npc != null
				&& npc.isAlive() && !npc.isRemoved() && opening.guest().equals(npc.guest()) && npc.level() == owner.level()
				&& npc.getBoundingBox().distanceToSqr(owner.getEyePosition()) <= GuestBalance.INTERACT_RANGE * GuestBalance.INTERACT_RANGE
				&& npc.getUUID().equals(GuestLedger.get(owner.level().getServer()).canonical(opening.guest()));
	}

	@Override
	public boolean clickMenuButton(Player player, int id) {
		if (owner == null || npc == null || player != owner || id < 0 || !stillValid(player)) {
			return false;
		}
		int ordinal = id & 15;
		int optionIndex = id >>> OPTION_SHIFT;
		GuestService.Session session = GuestService.session(owner.getUUID());
		GuestFeatures.Entry entry = GuestFeatures.option(optionIndex);
		if (ordinal >= Action.values().length || (id >>> REVISION_SHIFT & (REVISIONS - 1)) != session.revision
				|| (ordinal == Action.OPTION.ordinal() ? entry == null : optionIndex != 0)) {
			return false; // an unknown action, or a stale or replayed click
		}
		session.invalidate();
		long now = owner.level().getGameTime();
		if (now - session.lastAction < GuestBalance.ACTION_GAP_TICKS) {
			broadcastChanges();
			return false;
		}
		session.lastAction = now;
		String guest = opening.guest();
		GuestResult result = switch (Action.values()[ordinal]) {
			case GIFT -> GuestFavors.gift(owner, npc, guest);
			case QUEST_ACCEPT -> GuestFavors.acceptQuest(owner, npc, guest);
			case QUEST_TURN_IN -> GuestFavors.turnIn(owner, npc, guest);
			case PROPOSE -> GuestMarriage.requestProposal(owner, npc, guest);
			case PROPOSE_CONFIRM -> GuestMarriage.confirmProposal(owner, npc, guest);
			case PROPOSE_CANCEL -> GuestMarriage.cancelProposal(owner);
			case DIVORCE -> GuestMarriage.requestDivorce(owner, guest);
			case DIVORCE_CONFIRM -> GuestMarriage.confirmDivorce(owner, guest);
			case DIVORCE_CANCEL -> GuestMarriage.cancelDivorce(owner);
			case OPTION -> entry.feature().act(owner, npc, guest, entry.option());
		};
		owner.sendOverlayMessage(result.message());
		broadcastChanges();
		return result.done();
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slot) {
		return ItemStack.EMPTY;
	}

	// ---------------------------------------------------------------- what the screen reads (either side)

	public Opening opening() {
		return opening;
	}

	public GuestRoster.@Nullable Guest guest() {
		return GuestRoster.get(opening.guest());
	}

	public int get(int field) {
		return data.get(field);
	}

	public int revision() {
		return data.get(REVISION) & 0xFFFF;
	}

	public GuestFavors.QuestState questState() {
		GuestFavors.QuestState[] states = GuestFavors.QuestState.values();
		int state = data.get(QUEST_STATE);
		return state >= 0 && state < states.length ? states[state] : GuestFavors.QuestState.NONE;
	}

	/** The item and count the current quest asks for; empty when there is none. */
	public ItemStack questItem() {
		int count = data.get(QUEST_COUNT);
		ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.byId(data.get(QUEST_ITEM) & 0xFFFF));
		return count <= 0 || stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(count);
	}

	/** The server's values, read live from the ledger and favors each time the menu syncs. */
	private final class Live implements ContainerData {
		private final ServerPlayer player;
		private final String guest;

		Live(ServerPlayer player, String guest) {
			this.player = player;
			this.guest = guest;
		}

		@Override
		public int get(int field) {
			GuestLedger ledger = GuestLedger.get(player.level().getServer());
			UUID me = player.getUUID();
			return switch (field) {
				case AFFECTION -> ledger.affection(me, guest);
				case MAX_AFFECTION -> GuestBalance.MAX_AFFECTION;
				case GUEST_SPOUSE -> {
					UUID spouse = ledger.spouseOf(guest);
					yield spouse == null ? NOBODY : spouse.equals(me) ? EACH_OTHER : SOMEONE_ELSE;
				}
				case PLAYER_SPOUSE -> {
					String spouse = ledger.spouseOf(me);
					yield spouse == null ? NOBODY : spouse.equals(guest) ? EACH_OTHER : SOMEONE_ELSE;
				}
				case PLAYER_COOLDOWN_MINUTES -> minutes(ledger.cooldownMillis(me));
				case GUEST_COOLDOWN_MINUTES -> minutes(ledger.cooldownMillis(guest));
				case QUEST_STATE -> GuestFavors.questState(player, guest).ordinal();
				case QUEST_ITEM -> {
					ItemStack wanted = GuestFavors.questItem(player, guest);
					yield wanted.isEmpty() ? 0 : BuiltInRegistries.ITEM.getId(wanted.getItem());
				}
				case QUEST_COUNT -> GuestFavors.questItem(player, guest).getCount();
				case QUEST_CARRIED -> Math.min(Short.MAX_VALUE, GuestFavors.carried(player, guest));
				case GIFTS_LEFT -> GuestFavors.giftsLeft(player, guest);
				case REVISION -> GuestService.session(me).revision;
				case DIVORCE_PENDING -> GuestMarriage.divorcePending(player) ? 1 : 0;
				case MAY_PROPOSE -> GuestMarriage.mayPropose(player, guest) ? 1 : 0;
				case AFFECTION_LEFT_TODAY -> GuestFavors.affectionLeftToday(player, guest);
				case PROPOSAL_PENDING -> guest.equals(GuestMarriage.proposalPending(player)) ? 1 : 0;
				case MAY_MARRY -> {
					GuestRoster.Guest entry = GuestRoster.get(guest);
					yield entry != null && entry.mayMarry() ? 1 : 0;
				}
				default -> field >= OPTIONS_AVAILABLE && field < FIELDS ? optionBits(field - OPTIONS_AVAILABLE) : 0;
			};
		}

		/** Which of options {@code 16 * word} to {@code 16 * word + 15} are offered now (a feature that throws offers nothing). */
		private int optionBits(int word) {
			int bits = 0;
			if (npc == null) {
				return 0;
			}
			for (GuestFeatures.Entry entry : GuestFeatures.options()) {
				if (entry.index() / 16 != word) {
					continue;
				}
				boolean offered;
				try {
					offered = entry.feature().available(player, npc, guest, entry.option());
				} catch (RuntimeException e) {
					offered = false;
				}
				if (offered) {
					bits |= 1 << (entry.index() % 16);
				}
			}
			return bits;
		}

		@Override
		public void set(int field, int value) {
		}

		@Override
		public int getCount() {
			return FIELDS;
		}
	}

	/** Whole minutes, rounded up, of {@code millis}; at most 16 bits' worth (a seven-day cooldown is 10,080). */
	static int minutes(long millis) {
		return millis <= 0 ? 0 : (int) Math.min(Short.MAX_VALUE, (millis + 59_999L) / 60_000L);
	}
}
