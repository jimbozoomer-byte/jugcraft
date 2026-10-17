package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.concordance.assay.AlchemyStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;

public final class AlchemyStorageClient {
    private AlchemyStorageClient(){}
    public static void register(){
        MenuScreens.register(AlchemyStorage.MENU,AlchemyStorageScreen::new);
        MenuScreens.register(AlchemyStorage.CATALOGUE_MENU,AlchemyStorageCatalogueScreen::new);
        AlchemyStorage.openManual=()->Minecraft.getInstance().gui.setScreen(new AlchemyStorageManualScreen());
    }
}
