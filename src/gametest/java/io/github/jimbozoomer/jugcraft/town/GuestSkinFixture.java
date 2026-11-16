package io.github.jimbozoomer.jugcraft.town;

/** Test-only saved pending player-skin state, without a network request. */
public final class GuestSkinFixture {
    private GuestSkinFixture() {}

    public static void pendingPlayerSkin(Townsfolk npc, String name) {
        npc.custom().skinName = name;
        npc.custom().profile = null;
        CustomNpcs.showSkin(npc);
    }
}
