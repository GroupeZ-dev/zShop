package fr.maxlego08.shop.buttons;

import fr.maxlego08.menu.api.Inventory;
import fr.maxlego08.menu.api.button.Button;
import fr.maxlego08.menu.api.engine.InventoryEngine;
import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.shop.ShopPlugin;
import fr.maxlego08.shop.api.PlayerCache;
import fr.maxlego08.shop.api.buttons.ItemButton;
import fr.maxlego08.shop.api.buttons.ShowItemButton;
import fr.maxlego08.shop.save.ConfirmAction;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public abstract class ConfirmationButton extends Button {

    protected ShopPlugin plugin;
    private Inventory inventory;

    public ConfirmationButton(Plugin plugin) {
        this.plugin = (ShopPlugin) plugin;
    }

    @Override
    public ItemStack getCustomItemStack(@NotNull Player player, boolean useCache, @NotNull Placeholders placeholders) {

        PlayerCache playerCache = this.plugin.getShopManager().getCache(player);
        ItemButton itemButton = playerCache.getItemButton();
        if (itemButton == null) return super.getCustomItemStack(player, useCache, placeholders);

        int amount = playerCache.getAmount();
        String sellPrice = itemButton.getSellPriceFormat(player, amount);
        String buyPrice = itemButton.getBuyPriceFormat(player, amount);

        placeholders.register("sellPrice", sellPrice);
        placeholders.register("buyPrice", buyPrice);

        return super.getCustomItemStack(player, useCache, placeholders);
    }

    protected void action(Player player, InventoryEngine inventory, ConfirmAction confirmAction, ShopPlugin plugin, PlayerCache cache) {
        var placeholders = new Placeholders();
        switch (confirmAction) {
            case CLOSE:
                player.closeInventory();
                break;
            case OPEN_BACK:
                if (this.inventory == null) return;

                List<Inventory> oldInventories = inventory.getOldInventories();
                oldInventories.remove(this.inventory);

                Inventory toInventory = this.inventory;
                plugin.getIManager().openInventory(player, toInventory, 1, oldInventories);
                break;
            case RESET_AMOUNT:
                cache.setItemAmount(1);
                inventory.getButtons().stream().filter(button -> button instanceof ShowItemButton).forEach(b -> inventory.buildButton(b, placeholders));
                break;
        }
    }

    @Override
    public void onInventoryOpen(Player player, InventoryEngine inventory, Placeholders placeholders) {
        super.onInventoryOpen(player, inventory, placeholders);
        List<Inventory> oldInventories = inventory.getOldInventories();
        if (!oldInventories.isEmpty()) {
            this.inventory = oldInventories.getLast();
        }
    }
}
