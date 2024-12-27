package fr.maxlego08.zshop.buttons;

import fr.maxlego08.menu.api.requirement.Action;
import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.button.ZButton;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import fr.maxlego08.zshop.ShopPlugin;
import fr.maxlego08.zshop.ZShopManager;
import fr.maxlego08.zshop.api.buttons.ItemConfirmButton;
import fr.maxlego08.zshop.api.economy.ShopEconomy;
import fr.maxlego08.zshop.api.history.History;
import fr.maxlego08.zshop.api.history.HistoryType;
import fr.maxlego08.zshop.history.ZHistory;
import fr.maxlego08.zshop.save.LogConfig;
import fr.maxlego08.zshop.zcore.logger.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class ZItemConfirmButton extends ZButton implements ItemConfirmButton {

    private final ShopPlugin plugin;
    private final ShopEconomy shopEconomy;
    private final double price;
    private final boolean enableLog;
    private final String name;
    private final String inventoryConfirm;
    private final String withdrawReason;
    private final List<Action> confirmActions;

    public ZItemConfirmButton(ShopPlugin plugin, ShopEconomy economy, double price, boolean enableLog, String name, String inventoryConfirm, String withdrawReason, List<Action> confirmActions) {
        this.plugin = plugin;
        this.shopEconomy = economy;
        this.price = price;
        this.enableLog = enableLog;
        this.name = name;
        this.inventoryConfirm = inventoryConfirm;
        this.withdrawReason = withdrawReason;
        this.confirmActions = confirmActions;
    }

    @Override
    public double getPrice() {
        return this.price;
    }

    @Override
    public ShopEconomy getShopEconomy() {
        return this.shopEconomy;
    }

    @Override
    public boolean enableLog() {
        return this.enableLog;
    }

    @Override
    public void buy(Player player, int amount) {

        ZShopManager manager = (ZShopManager) this.plugin.getShopManager();
        double currentPrice = getPrice();

        /* ECONOMY CHECK */
        if (!this.shopEconomy.hasMoney(player, currentPrice)) {
            manager.message(this.plugin, player, this.shopEconomy.getDenyMessage());
            return;
        }

        this.shopEconomy.withdrawMoney(player, currentPrice, this.withdrawReason);

        /* END ECONOMY CHECK */

        Placeholders placeholders = new Placeholders();
        placeholders.register("player", player.getName());

        for (Action confirmAction : this.confirmActions) {
            confirmAction.preExecute(player, this, this.plugin.getIManager().getFakeInventory(), placeholders);
        }

        if (LogConfig.enableLog || this.enableLog) {

            String logMessage = LogConfig.buyConfirmMessage;

            logMessage = logMessage.replace("%name%", name);
            logMessage = logMessage.replace("%price%", String.valueOf(price));
            logMessage = logMessage.replace("%player%", player.getName());
            logMessage = logMessage.replace("%uuid%", player.getUniqueId().toString());

            if (LogConfig.enableLogInConsole) Logger.info(logMessage);

            if (LogConfig.enableLogInFile) {
                History history = new ZHistory(HistoryType.BUY, logMessage);
                this.plugin.getHistoryManager().asyncValue(player.getUniqueId(), history);
            }
        }
    }

    @Override
    public void sell(Player player, int amount) {

    }

    @Override
    public void onClick(Player player, InventoryClickEvent event, InventoryDefault inventory, int slot, Placeholders placeholders) {
        super.onClick(player, event, inventory, slot, placeholders);
        this.plugin.getShopManager().openConfirm(player, this, this.inventoryConfirm);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getInventoryConfirm() {
        return this.inventoryConfirm;
    }
}