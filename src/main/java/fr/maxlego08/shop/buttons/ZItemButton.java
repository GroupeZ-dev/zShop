package fr.maxlego08.shop.buttons;

import fr.maxlego08.menu.api.MenuItemStack;
import fr.maxlego08.menu.api.engine.InventoryEngine;
import fr.maxlego08.menu.api.utils.MetaUpdater;
import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.shop.ShopPlugin;
import fr.maxlego08.shop.ZShopManager;
import fr.maxlego08.shop.api.PriceModifier;
import fr.maxlego08.shop.api.PriceType;
import fr.maxlego08.shop.api.ShopManager;
import fr.maxlego08.shop.api.buttons.ItemButton;
import fr.maxlego08.shop.api.economy.ShopEconomy;
import fr.maxlego08.shop.api.event.events.ZShopBuyEvent;
import fr.maxlego08.shop.api.event.events.ZShopSellEvent;
import fr.maxlego08.shop.api.history.History;
import fr.maxlego08.shop.api.history.HistoryType;
import fr.maxlego08.shop.api.limit.Limit;
import fr.maxlego08.shop.api.limit.LimiterManager;
import fr.maxlego08.shop.api.limit.PlayerLimit;
import fr.maxlego08.shop.history.ZHistory;
import fr.maxlego08.shop.placeholder.Placeholder;
import fr.maxlego08.shop.save.Config;
import fr.maxlego08.shop.save.LogConfig;
import fr.maxlego08.shop.zcore.enums.Message;
import fr.maxlego08.shop.zcore.logger.Logger;
import fr.maxlego08.shop.zcore.utils.ItemStackSpawnerHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class ZItemButton extends ItemButton {

    protected final ShopManager shopManager;
    protected final ShopPlugin plugin;
    private final double sellPrice;
    private final double buyPrice;
    private final int maxStack;
    private final List<String> lore;
    private final ShopEconomy shopEconomy;
    private final List<String> buyCommands;
    private final List<String> sellCommands;
    private final boolean giveItem;
    private final Limit serverSellLimit;
    private final Limit serverBuyLimit;
    private final Limit playerSellLimit;
    private final Limit playerBuyLimit;
    private final boolean enableLog;
    private final boolean affectByPriceModifier;
    private final String mob;
    private final String inventoryBuy;
    private final String inventorySell;
    private final boolean unstackable;
    private final String withdrawReason;
    private final String depositReason;

    public ZItemButton(ShopPlugin plugin, double sellPrice, double buyPrice, int maxStack, List<String> lore, ShopEconomy shopEconomy, List<String> buyCommands, List<String> sellCommands, boolean giveItem, Limit serverSellLimit, Limit serverBuyLimit, Limit playerSellLimit, Limit playerBuyLimit, boolean enableLog, boolean affectByPriceModifier, String mob, String inventoryBuy, String inventorySell, boolean unstackable, String withdrawReason, String depositReason) {
        this.plugin = plugin;
        this.shopManager = plugin.getShopManager();
        this.sellPrice = sellPrice;
        this.buyPrice = buyPrice;
        this.maxStack = maxStack;
        this.lore = lore;
        this.shopEconomy = shopEconomy;
        this.buyCommands = buyCommands;
        this.sellCommands = sellCommands;
        this.giveItem = giveItem;
        this.serverSellLimit = serverSellLimit;
        this.serverBuyLimit = serverBuyLimit;
        this.playerSellLimit = playerSellLimit;
        this.playerBuyLimit = playerBuyLimit;
        this.enableLog = enableLog;
        this.affectByPriceModifier = affectByPriceModifier;
        this.mob = mob;
        this.inventoryBuy = inventoryBuy;
        this.inventorySell = inventorySell;
        this.unstackable = unstackable;
        this.withdrawReason = withdrawReason;
        this.depositReason = depositReason;
    }

    @Override
    public double getSellPrice() {
        return this.sellPrice;
    }

    @Override
    public double getBuyPrice() {
        return this.buyPrice;
    }

    @Override
    public double getSellPrice(int amount) {
        return amount * getUnitSellPrice();
    }

    @Override
    public double getBuyPrice(int amount) {
        return amount * getUnitBuyPrice();
    }

    @Override
    public double getSellPrice(Player player, int amount) {
        AtomicReference<Double> price = new AtomicReference<>(getSellPrice(amount));
        Optional<PriceModifier> optional = this.shopManager.getPriceModifier(player, PriceType.SELL);
        if (this.affectByPriceModifier())
            optional.ifPresent(modifier -> price.updateAndGet(v -> v * modifier.getModifier()));
        return price.get();
    }

    @Override
    public double getBuyPrice(Player player, int amount) {
        AtomicReference<Double> price = new AtomicReference<>(getBuyPrice(amount));
        Optional<PriceModifier> optional = this.shopManager.getPriceModifier(player, PriceType.BUY);
        if (this.affectByPriceModifier())
            optional.ifPresent(modifier -> price.updateAndGet(v -> v * modifier.getModifier()));
        return price.get();
    }

    @Override
    public double getUnitSellPrice() {
        return getSellPrice() / Double.parseDouble(this.getItemStack().getAmount());
    }

    @Override
    public double getUnitBuyPrice() {
        return getBuyPrice() / Double.parseDouble(this.getItemStack().getAmount());
    }

    @Override
    public boolean canSell() {
        return this.sellPrice > 0;
    }

    @Override
    public boolean canBuy() {
        return this.buyPrice > 0;
    }

    @Override
    public int getMaxStack() {
        return this.maxStack;
    }

    @Override
    public ShopEconomy getEconomy() {
        return this.shopEconomy;
    }

    @Override
    public List<String> getLore() {
        return this.lore;
    }

    @Override
    public String getSellPriceFormat(Player player, int amount) {
        if (!this.canSell()) return Message.CANT_SELL.msg();
        double price = getSellPrice(player, amount);
        return this.shopEconomy.format(this.shopManager.transformPrice(price), price);
    }

    @Override
    public String getBuyPriceFormat(Player player, int amount) {
        if (!this.canBuy()) return Message.CANT_BUY.msg();
        double price = getBuyPrice(player, amount);
        return this.shopEconomy.format(this.shopManager.transformPrice(price), price);
    }

    @Override
    public String getSellPriceFormat(int amount) {
        double sellPrice = getSellPrice(amount);
        return this.canSell() ? this.shopEconomy.format(this.shopManager.transformPrice(sellPrice), sellPrice) : Message.CANT_SELL.msg();
    }

    @Override
    public String getBuyPriceFormat(int amount) {
        double buyPrice = getBuyPrice(amount);
        return this.canBuy() ? this.shopEconomy.format(this.shopManager.transformPrice(buyPrice), buyPrice) : Message.CANT_BUY.msg();
    }

    @Override
    public void buy(Player player, int amount) {

        ZShopManager manager = (ZShopManager) this.plugin.getShopManager();
        double currentPrice = this.getBuyPrice(player, amount);

        /* If the price is invalid, then we stop */
        if (currentPrice < 0) return;

        /* ECONOMY CHECK */
        if (!this.shopEconomy.hasMoney(player, currentPrice)) {
            manager.message(this.plugin, player, this.shopEconomy.getDenyMessage());
            return;
        }
        /* END ECONOMY CHECK */

        /* INVENTORY SLOT CHECK */
        if (manager.hasInventoryFull(player) && Config.enableInventoryFullBuy) {
            manager.message(this.plugin, player, Message.NOT_ENOUGH_PLACE);
            return;
        }
        /* END INVENTORY SLOT CHECK */

        /* SERVER LIMIT */
        int newServerLimitAmount = 0;
        int newPlayerLimitAmount = 0;
        String material = this.getItemStack().getMaterial();
        LimiterManager limiterManager = this.plugin.getLimiterManager();

        if (serverBuyLimit != null) {
            newServerLimitAmount = serverBuyLimit.getAmount() + amount;
            if (newServerLimitAmount > serverBuyLimit.getLimit()) {
                manager.message(this.plugin, player, Message.LIMIT_SERVER_BUY);
                return;
            }
        }
        /* END SERVER LIMIT */

        /* PLAYER LIMIT */
        if (playerBuyLimit != null) {
            // We will recover the value without creating a new player limit
            Optional<PlayerLimit> optional = limiterManager.getLimit(player);
            newPlayerLimitAmount = optional.map(e -> e.getBuyAmount(material)).orElse(0) + amount;
            if (newPlayerLimitAmount > playerBuyLimit.getLimit()) {
                manager.message(this.plugin, player, Message.LIMIT_SERVER_SELL);
                return;
            }
        }
        /* END PLAYER LIMIT */

        /* BUKKIT EVENT */
        ZShopBuyEvent event = new ZShopBuyEvent(player, this, amount, currentPrice, newServerLimitAmount, newPlayerLimitAmount);
        event.call();

        if (event.isCancelled()) return;

        newServerLimitAmount = event.getServerLimit();
        newPlayerLimitAmount = event.getPlayerLimit();

        /* If the purchase is confirmed, then we will change the limits */
        if (serverBuyLimit != null && newServerLimitAmount > 0) serverBuyLimit.setAmount(newServerLimitAmount);
        if (newPlayerLimitAmount > 0) limiterManager.getOrCreate(player).setBuyAmount(material, newPlayerLimitAmount);

        amount = event.getAmount();
        currentPrice = event.getPrice();
        /* END BUKKIT EVENT */

        ItemStack itemStack = super.getItemStack().build(player, false, new Placeholders()).clone();

        var translationManager = this.plugin.getTranslationManager();
        String itemName = translationManager.translateItemStack(itemStack);

        /* We withdraw the money if the price is greater than 0  */
        if (currentPrice > 0) {
            this.shopEconomy.withdrawMoney(player, currentPrice, this.withdrawReason.replace("%amount%", String.valueOf(amount)).replace("%item%", itemName));
        }

        /* BUILD ITEM AND GIVE IT TO PLAYER */

        if (this.mob != null) {
            ItemStackSpawnerHelper.setEntityType(itemStack, this.mob.toUpperCase());
        }

        itemStack.setAmount(amount);

        if (this.giveItem) {

            if (this.isUnStackable()) { // If the item cannot be stacked, create the item with an amount of 1 and add it x times in the inventory

                ItemStack clonedItemStack = itemStack.clone();
                clonedItemStack.setAmount(1);

                for (int i = 0; i != amount; i++) {
                    manager.give(player, clonedItemStack);
                }

            } else {
                if (amount > 64) {
                    manager.giveItem(player, amount, itemStack);
                } else manager.give(player, itemStack);
            }
        }
        /* END BUILD ITEM AND GIVE IT TO PLAYER */

        String buyPrice = this.shopEconomy.format(this.shopManager.transformPrice(currentPrice), currentPrice);
        manager.message(this.plugin, player, Message.BUY_ITEM, "%amount%", String.valueOf(amount), "%item%", itemName, "%price%", buyPrice);

        commands(amount, itemName, buyPrice, HistoryType.BUY, player);
        log(amount, itemName, buyPrice, player.getName(), player.getUniqueId(), HistoryType.BUY);
    }

    @Override
    public void sell(Player player, int amount) {
        ZShopManager manager = (ZShopManager) this.plugin.getShopManager();

        ItemStack itemStack = getItemStack().build(player, false);
        int items = 0;

        for (int a = 0; a != 36; a++) {
            ItemStack is = player.getInventory().getContents()[a];
            if (is != null && is.isSimilar(itemStack)) {
                items += is.getAmount();
            }
        }

        if (items <= 0) {
            manager.message(this.plugin, player, Message.NOT_ITEMS);
            return;
        }

        if (items < amount) {
            manager.message(this.plugin, player, Message.NOT_ENOUGH_ITEMS);
            return;
        }

        items = amount == 0 ? items : amount;
        int realAmount = items;

        int newServerLimitAmount = 0;
        int newPlayerLimitAmount = 0;
        String material = this.getItemStack().getMaterial();
        LimiterManager limiterManager = this.plugin.getLimiterManager();

        /* SERVER LIMIT */
        if (serverSellLimit != null) {
            newServerLimitAmount = serverSellLimit.getAmount() + realAmount;
            if (newServerLimitAmount > serverSellLimit.getLimit()) {
                manager.message(this.plugin, player, Message.LIMIT_SERVER_SELL);
                return;
            }
        }
        /* END SERVER LIMIT */

        /* PLAYER LIMIT */
        if (playerSellLimit != null) {
            Optional<PlayerLimit> optional = limiterManager.getLimit(player);
            newPlayerLimitAmount = optional.map(e -> e.getSellAmount(material)).orElse(0) + realAmount;
            if (newPlayerLimitAmount > playerSellLimit.getLimit()) {
                manager.message(this.plugin, player, Message.LIMIT_SERVER_SELL);
                return;
            }
        }
        /* END PLAYER LIMIT */

        double currentPrice = this.getSellPrice(player, realAmount);

        int slot = 0;

        /* BUKKIT EVENT */

        ZShopSellEvent event = new ZShopSellEvent(player, this, realAmount, currentPrice, newServerLimitAmount, newPlayerLimitAmount);
        event.call();

        if (event.isCancelled()) return;

        newServerLimitAmount = event.getServerLimit();
        newPlayerLimitAmount = event.getPlayerLimit();

        /* If the sell is confirmed, then we will change the limits */
        if (serverSellLimit != null && newServerLimitAmount > 0) serverSellLimit.setAmount(newServerLimitAmount);
        if (newPlayerLimitAmount > 0) limiterManager.getOrCreate(player).setSellAmount(material, newPlayerLimitAmount);

        realAmount = event.getAmount();
        currentPrice = event.getPrice();

        /* END BUKKIT EVENT */

        /* Items are then removed from the player’s inventory */
        for (ItemStack is : player.getInventory().getContents()) {

            if (is != null && is.isSimilar(itemStack) && items > 0) {

                int currentAmount = is.getAmount() - items;
                items -= is.getAmount();

                if (currentAmount <= 0) {
                    if (slot == 40) player.getInventory().setItemInOffHand(null);
                    else player.getInventory().removeItem(is);
                } else is.setAmount(currentAmount);
            }
            slot++;
        }
        player.updateInventory();
        /* END ITEMS */

        /* We withdraw the money if the price is greater than 0  */
        var translationManager = this.plugin.getTranslationManager();
        String itemName = translationManager.translateItemStack(itemStack);

        if (currentPrice > 0) {
            this.shopEconomy.depositMoney(player, currentPrice, this.depositReason.replace("%amount%", String.valueOf(realAmount)).replace("%item%", itemName));
        }

        String sellPrice = this.shopEconomy.format(this.shopManager.transformPrice(currentPrice), currentPrice);
        manager.message(this.plugin, player, Message.SELL_ITEM, "%amount%", String.valueOf(realAmount), "%item%", itemName, "%price%", sellPrice);

        commands(realAmount, itemName, sellPrice, HistoryType.SELL, player);
        log(realAmount, itemName, sellPrice, player.getName(), player.getUniqueId(), HistoryType.SELL);
    }

    private void commands(int amount, String itemName, String price, HistoryType type, Player player) {
        plugin.getIManager().getScheduler().runAtLocation(player.getLocation(), w -> {
            for (String command : (type == HistoryType.SELL ? sellCommands : buyCommands)) {
                command = command.replace("%amount%", String.valueOf(amount));
                command = command.replace("%item%", itemName);
                command = command.replace("%price%", price);
                command = command.replace("%player%", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Placeholder.getPlaceholder().setPlaceholders(player, command));
            }
        });
    }

    @Override
    public void log(int amount, String itemName, String price, String playerName, UUID uuid, HistoryType type) {
        if (LogConfig.enableLog || this.enableLog) {

            String logMessage = ((ZShopManager) this.plugin.getShopManager()).getMessage(type == HistoryType.SELL ? LogConfig.sellMessage : LogConfig.buyMessage, "%amount%", String.valueOf(amount), "%item%", itemName, "%price%", price, "%player%", playerName, "%uuid%", uuid.toString());

            if (LogConfig.enableLogInConsole) Logger.info(logMessage);

            if (LogConfig.enableLogInFile) {
                History history = new ZHistory(type, logMessage);
                this.plugin.getHistoryManager().asyncValue(uuid, history);
            }
        }
    }

    @Override
    public boolean affectByPriceModifier() {
        return this.affectByPriceModifier;
    }

    @Override
    public String getMob() {
        return this.mob;
    }

    @Override
    public String getInventoryBuy() {
        return this.inventoryBuy;
    }

    @Override
    public String getInventorySell() {
        return this.inventorySell;
    }

    @Override
    public List<String> getBuyCommands() {
        return this.buyCommands;
    }

    @Override
    public List<String> getSellCommands() {
        return this.sellCommands;
    }

    @Override
    public boolean giveItem() {
        return this.giveItem;
    }

    @Override
    public Optional<Limit> getServerBuyLimit() {
        return Optional.ofNullable(this.serverBuyLimit);
    }

    @Override
    public Optional<Limit> getServerSellLimit() {
        return Optional.ofNullable(this.serverSellLimit);
    }

    @Override
    public Optional<Limit> getPlayerBuyLimit() {
        return Optional.ofNullable(this.playerBuyLimit);
    }

    @Override
    public Optional<Limit> getPlayerSellLimit() {
        return Optional.ofNullable(this.playerSellLimit);
    }

    @Override
    public ItemStack getCustomItemStack(@NotNull Player player, boolean useCache, @NotNull Placeholders placeholders) {
        // ItemStack itemStack = super.getCustomItemStack(player);
        ItemStack itemStack = this.getItemStack().build(player, false);
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (Config.disableItemFlag) {
            for (ItemFlag itemFlag : ItemFlag.values()) {
                itemMeta.addItemFlags(itemFlag);
            }
        }

        MetaUpdater metaUpdater = plugin.getIManager().getMeta();
        metaUpdater.updateLore(itemMeta, buildLore(player), player);

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    @Override
    public void onRightClick(@NotNull Player player, @NotNull InventoryClickEvent event, @NotNull InventoryEngine inventory, int slot) {
        super.onRightClick(player, event, inventory, slot);
        if (canSell()) {
            this.plugin.getShopManager().openSell(player, this, this.inventorySell);
        }
    }

    @Override
    public void onMiddleClick(@NotNull Player player, @NotNull InventoryClickEvent event, @NotNull InventoryEngine inventory, int slot) {
        super.onMiddleClick(player, event, inventory, slot);
        if (canSell()) {
            sell(player, 0);
        }
    }

    @Override
    public void onLeftClick(@NotNull Player player, @NotNull InventoryClickEvent event, @NotNull InventoryEngine inventory, int slot) {
        super.onLeftClick(player, event, inventory, slot);
        if (canBuy()) {
            this.plugin.getShopManager().openBuy(player, this, this.inventoryBuy);
        }
    }

    @Override
    public List<String> buildLore(Player player) {

        MenuItemStack menuItemStack = this.getItemStack();
        int amount = menuItemStack.parseAmount(player);

        List<String> itemLore = new ArrayList<>();
        if (menuItemStack.getLore() != null && !menuItemStack.getLore().isEmpty())
            itemLore = new ArrayList<>(menuItemStack.getLore());

        String sellPrice = getSellPriceFormat(player, amount);
        String buyPrice = getBuyPriceFormat(player, amount);

        String realSellPrice = getSellPriceFormat(amount);
        String realBuyPrice = getBuyPriceFormat(amount);

        for (String line : new ArrayList<>(this.lore)) {
            line = line.replace("%sellPrice%", sellPrice);
            line = line.replace("%buyPrice%", buyPrice);
            line = line.replace("%realSellPrice%", realSellPrice);
            line = line.replace("%realBuyPrice%", realBuyPrice);

            /* SERVER LIMIT */
            line = line.replace("%serverSellLimit%", serverSellLimit == null ? "-1" : String.valueOf(serverSellLimit.getLimit()));
            line = line.replace("%serverBuyLimit%", serverBuyLimit == null ? "-1" : String.valueOf(serverBuyLimit.getLimit()));

            line = line.replace("%serverSellAmount%", serverSellLimit == null ? "0" : String.valueOf(serverSellLimit.getAmount()));
            line = line.replace("%serverBuyAmount%", serverBuyLimit == null ? "0" : String.valueOf(serverBuyLimit.getAmount()));
            /* END SERVER LIMIT */

            /* PLAYER LIMIT */
            line = line.replace("%playerSellLimit%", playerSellLimit == null ? "-1" : String.valueOf(playerSellLimit.getLimit()));
            line = line.replace("%playerBuyLimit%", playerBuyLimit == null ? "-1" : String.valueOf(playerBuyLimit.getLimit()));

            String material = this.getItemStack().getMaterial();
            Optional<PlayerLimit> optional = this.plugin.getLimiterManager().getLimit(player);
            line = line.replace("%playerSellAmount%", optional.map(limit -> String.valueOf(limit.getSellAmount(material))).orElse("0"));
            line = line.replace("%playerBuyAmount%", optional.map(playerLimit -> String.valueOf(playerLimit.getBuyAmount(material))).orElse("0"));
            /* END PLAYER LIMIT */

            itemLore.add(Placeholder.getPlaceholder().setPlaceholders(player, line));
        }
        return itemLore;
    }

    @Override
    public boolean enableLog() {
        return this.enableLog;
    }

    @Override
    public boolean isUnStackable() {
        return this.unstackable;
    }
}
