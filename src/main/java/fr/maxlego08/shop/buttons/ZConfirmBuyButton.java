package fr.maxlego08.shop.buttons;

import fr.maxlego08.menu.api.engine.InventoryEngine;
import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.shop.ShopPlugin;
import fr.maxlego08.shop.api.PlayerCache;
import fr.maxlego08.shop.api.buttons.EconomyAction;
import fr.maxlego08.shop.save.Config;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.Plugin;

public class ZConfirmBuyButton extends ConfirmationButton {

    public ZConfirmBuyButton(Plugin plugin) {
        super(plugin);
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event, InventoryEngine inventory, int slot, Placeholders placeholders) {
        super.onClick(player, event, inventory, slot, placeholders);

        PlayerCache cache = this.plugin.getShopManager().getCache(player);
        EconomyAction economyAction = cache.getEconomyAction();
        economyAction.buy(player, cache.getAmount());
        action(player, inventory, Config.purchaseConfirmationConfig, plugin, cache);
    }
}
