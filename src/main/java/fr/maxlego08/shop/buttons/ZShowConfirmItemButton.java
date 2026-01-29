package fr.maxlego08.shop.buttons;

import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.shop.ShopPlugin;
import fr.maxlego08.shop.api.PlayerCache;
import fr.maxlego08.shop.api.buttons.EconomyAction;
import fr.maxlego08.shop.api.buttons.ShowConfirmItemButton;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class ZShowConfirmItemButton extends ShowConfirmItemButton {

    private final ShopPlugin plugin;

    public ZShowConfirmItemButton(Plugin plugin) {
        this.plugin = (ShopPlugin) plugin;
    }

    @Override
    public ItemStack getCustomItemStack(Player player, Placeholders placeholders) {

        PlayerCache playerCache = this.plugin.getShopManager().getCache(player);
        EconomyAction itemButton = playerCache.getEconomyAction();
        if (itemButton == null) return super.getCustomItemStack(player, placeholders);

        return itemButton.getCustomItemStack(player, placeholders);
    }
}
