/**
 * JZE town guests: named guest NPCs that players befriend with gifts and fetch quests and, for adult characters only,
 * may marry. Owner request, 10 November 2026; the feature record is docs/features/jze-town-relationships.md (Codex).
 *
 * <p><b>Shared contract (agreed before the work was split; change only through the coordinator).</b>
 * <ul>
 * <li>{@link io.github.jimbozoomer.jugcraft.town.guests.GuestRoster}: the only identities. Ids are saved: never rename or
 * reuse one. Every available guest has the same exclusive companionship mechanics; nothing is sexual for anyone.
 * Pending entries can be neither placed nor befriended. Guests depicted as minors or school students in their source
 * work have companionship but no proposal or marriage ({@code Guest.mayMarry()}).</li>
 * <li>A guest is a custom NPC ({@code CustomNpcs}) whose {@code Townsfolk.guest()} is a roster id, saved in the entity
 * under {@code jugcraft_guest}. Talking to it opens {@link io.github.jimbozoomer.jugcraft.town.guests.GuestMenu}
 * (an operator sneaking still selects it for {@code /npc}).</li>
 * <li>{@link io.github.jimbozoomer.jugcraft.town.guests.GuestLedger} (worker A) is the one persistent canonical record:
 * which entity is each guest, affection per player and guest, spouses and remarriage cooldowns. It is never cleared by
 * an entity's removal: a killed or removed guest keeps its spouse and its reservation, and only an operator's explicit
 * placement or repair binds a new entity. Any other loaded entity claiming the identity is a duplicate and leaves.</li>
 * <li>{@link io.github.jimbozoomer.jugcraft.town.guests.GuestFavors} (worker B): gifts and fetch quests, which add
 * affection only through {@code GuestLedger.addAffection}. No currency is paid or created.</li>
 * <li>{@link io.github.jimbozoomer.jugcraft.town.guests.GuestMarriage} (worker A): proposal and divorce, server side,
 * each asked for and then explicitly confirmed; the proposal item is taken only on a confirmed, committed marriage.</li>
 * <li>{@link io.github.jimbozoomer.jugcraft.town.guests.GuestMenu} (coordinator; worker C draws its screen): every
 * player action is a vanilla menu button checked on the server (the player alive, beside the canonical guest within
 * {@link io.github.jimbozoomer.jugcraft.town.guests.GuestBalance#INTERACT_RANGE} in the same level), carrying the
 * player's revision (moved on by every click and every menu opened) against stale and doubled clicks. That revision is
 * no security token: every protection (items, daily caps, quests done, pending confirmations, cooldowns) lives in the
 * ledger or favors and is checked there on every action; reopening a menu or reconnecting resets none of it. The menu
 * stays valid only while the guest is available, the entity still is that guest and is its canonical entity. No
 * land-claim requirement: guests are public.</li>
 * <li>Placement and appearance (worker C): placing never changes a block; an appearance without reference art is a
 * labelled placeholder, never presented as the character.</li>
 * <li>Messages to players use {@code Component.translatableWithFallback}; only worker C edits en_us.json.</li>
 * </ul>
 */
package io.github.jimbozoomer.jugcraft.town.guests;
