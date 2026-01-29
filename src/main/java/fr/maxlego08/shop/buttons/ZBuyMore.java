package fr.maxlego08.shop.buttons;

import fr.maxlego08.menu.api.engine.InventoryEngine;
import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.shop.ShopPlugin;
import fr.maxlego08.shop.api.buttons.BuyMore;
import fr.maxlego08.shop.api.buttons.ItemButton;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public class ZBuyMore extends BuyMore {

    private final ShopPlugin plugin;
    private final int amount;

    public ZBuyMore(ShopPlugin plugin, int stack) {
        this.plugin = plugin;
        this.amount = stack;
    }

    @Override
    public void onClick(@NotNull Player player, @NotNull InventoryClickEvent event, @NotNull InventoryEngine inventory, int slot, @NotNull Placeholders placeholders) {
        super.onClick(player, event, inventory, slot, placeholders);

        ItemButton itemButton = this.plugin.getShopManager().getCache(player).getItemButton();
        if (itemButton == null) return;

        itemButton.buy(player, amount);
    }
}
