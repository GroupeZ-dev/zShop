package fr.maxlego08.shop.listener;

import fr.maxlego08.menu.api.event.FastEvent;
import fr.maxlego08.menu.api.event.events.ButtonLoadEvent;
import fr.maxlego08.shop.api.ShopManager;

public class MenuListener extends FastEvent {

    private final ShopManager shopManager;

    public MenuListener(ShopManager shopManager) {
        this.shopManager = shopManager;
    }

    @Override
    public void onButtonLoad(ButtonLoadEvent event) {
        shopManager.onButtonLoad(event);
    }
}
