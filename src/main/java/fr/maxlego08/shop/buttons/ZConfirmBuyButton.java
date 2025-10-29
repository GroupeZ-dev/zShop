package fr.maxlego08.shop.buttons;

import fr.maxlego08.menu.api.engine.InventoryEngine;
import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.shop.ShopPlugin;
import fr.maxlego08.shop.api.PlayerCache;
import fr.maxlego08.shop.api.buttons.EconomyAction;
import fr.maxlego08.shop.api.buttons.ItemButton;
import fr.maxlego08.shop.placeholder.Placeholder;
import fr.maxlego08.shop.save.Config;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.stream.Collectors;

public class ZConfirmBuyButton extends ConfirmationButton {

    private final ShopPlugin plugin;

    public ZConfirmBuyButton(Plugin plugin) {
        this.plugin = (ShopPlugin) plugin;
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event, InventoryEngine inventory, int slot, Placeholders placeholders) {
        super.onClick(player, event, inventory, slot, placeholders);

        PlayerCache cache = this.plugin.getShopManager().getCache(player);
        EconomyAction economyAction = cache.getEconomyAction();
        economyAction.buy(player, cache.getAmount());
        action(player, inventory, Config.purchaseConfirmationConfig, plugin, cache);
    }

    @Override
    public ItemStack getCustomItemStack(Player player) {
        ItemStack itemStack = super.getCustomItemStack(player);
        
        PlayerCache playerCache = this.plugin.getShopManager().getCache(player);
        ItemButton itemButton = playerCache.getItemButton();
        if (itemButton == null) return itemStack;
        
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null || itemMeta.getLore() == null) return itemStack;
        
        String buyPrice = itemButton.getBuyPriceFormat(player, playerCache.getAmount());
        String sellPrice = itemButton.getSellPriceFormat(player, playerCache.getAmount());
        
        List<String> updatedLore = itemMeta.getLore().stream().map(line -> {
            line = line.replace("%buyPrice%", buyPrice);
            line = line.replace("%sellPrice%", sellPrice);
            return Placeholder.getPlaceholder().setPlaceholders(player, line);
        }).collect(Collectors.toList());
        
        itemMeta.setLore(updatedLore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
