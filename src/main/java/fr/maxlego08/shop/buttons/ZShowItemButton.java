package fr.maxlego08.shop.buttons;

import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.shop.ShopPlugin;
import fr.maxlego08.shop.api.PlayerCache;
import fr.maxlego08.shop.api.buttons.ItemButton;
import fr.maxlego08.shop.api.buttons.ShowItemButton;
import fr.maxlego08.shop.placeholder.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public class ZShowItemButton extends ShowItemButton {

    private final ShopPlugin plugin;
    private final List<String> lore;

    public ZShowItemButton(ShopPlugin plugin, List<String> lore) {
        this.plugin = plugin;
        this.lore = lore;
    }

    @Override
    public ItemStack getCustomItemStack(Player player, Placeholders placeholders) {

        PlayerCache playerCache = this.plugin.getShopManager().getCache(player);
        ItemButton itemButton = playerCache.getItemButton();
        if (itemButton == null) return super.getCustomItemStack(player, placeholders);

        ItemStack itemStack = itemButton.getCustomItemStack(player, placeholders);
        itemStack.setAmount(playerCache.getAmount());

        ItemMeta itemMeta = itemStack.getItemMeta();

        String sellPrice = itemButton.getSellPriceFormat(player, itemStack.getAmount());
        String buyPrice = itemButton.getBuyPriceFormat(player, itemStack.getAmount());

        List<String> itemLore = this.lore.stream().map(line -> {

            line = line.replace("%sellPrice%", sellPrice);
            line = line.replace("%buyPrice%", buyPrice);

            return Placeholder.getPlaceholder().setPlaceholders(player, line);
        }).collect(Collectors.toList());

        this.plugin.getIManager().getMeta().updateLore(itemMeta, itemLore, player);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    @Override
    public List<String> getLore() {
        return this.lore;
    }
}
