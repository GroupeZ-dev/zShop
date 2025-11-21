package fr.maxlego08.shop;

import fr.maxlego08.menu.api.Inventory;
import fr.maxlego08.menu.api.InventoryManager;
import fr.maxlego08.menu.api.button.Button;
import fr.maxlego08.menu.api.command.CommandManager;
import fr.maxlego08.menu.api.event.events.ButtonLoadEvent;
import fr.maxlego08.menu.api.exceptions.InventoryException;
import fr.maxlego08.menu.api.pattern.PatternManager;
import fr.maxlego08.shop.api.PlayerCache;
import fr.maxlego08.shop.api.PriceModifier;
import fr.maxlego08.shop.api.PriceType;
import fr.maxlego08.shop.api.ShopManager;
import fr.maxlego08.shop.api.buttons.EconomyAction;
import fr.maxlego08.shop.api.buttons.ItemButton;
import fr.maxlego08.shop.api.buttons.ItemConfirmButton;
import fr.maxlego08.shop.api.economy.ShopEconomy;
import fr.maxlego08.shop.api.event.ShopAction;
import fr.maxlego08.shop.api.event.events.ZShopSellAllEvent;
import fr.maxlego08.shop.api.history.History;
import fr.maxlego08.shop.api.history.HistoryType;
import fr.maxlego08.shop.api.limit.Limit;
import fr.maxlego08.shop.api.limit.LimiterManager;
import fr.maxlego08.shop.api.limit.PlayerLimit;
import fr.maxlego08.shop.api.utils.PriceModifierCache;
import fr.maxlego08.shop.history.ZHistory;
import fr.maxlego08.shop.inventory.SellInventoryHolder;
import fr.maxlego08.shop.placeholder.ItemButtonPlaceholder;
import fr.maxlego08.shop.placeholder.LocalPlaceholder;
import fr.maxlego08.shop.save.AbbreviateNumberConfig;
import fr.maxlego08.shop.save.Config;
import fr.maxlego08.shop.save.LogConfig;
import fr.maxlego08.shop.save.PriceFormatConfig;
import fr.maxlego08.shop.zcore.enums.Message;
import fr.maxlego08.shop.zcore.logger.Logger;
import fr.maxlego08.shop.zcore.utils.ItemStackSpawnerHelper;
import fr.maxlego08.shop.zcore.utils.Pair;
import fr.maxlego08.shop.zcore.utils.ZUtils;
import fr.maxlego08.shop.zcore.utils.nms.NmsVersion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ZShopManager extends ZUtils implements ShopManager {

    private final ShopPlugin plugin;
    private final Map<UUID, PlayerCache> cachePlayers = new HashMap<>();
    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");
    private final List<ItemButton> itemButtons = new ArrayList<>();

    public ZShopManager(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void loadConfig() {

        Bukkit.getOnlinePlayers().forEach(player -> {
            Optional<Inventory> optional = this.plugin.getIManager().getCurrentPlayerInventory(player);
            if (optional.isPresent()) {
                Inventory inventory = optional.get();
                if (inventory.getPlugin() == this.plugin) {
                    player.closeInventory();
                }
            }
        });

        this.loadPatterns();
        this.loadInventories();
        this.loadCommands();
    }

    /**
     * Register default inventories files
     */
    private void registerDefaultFiles() {
        List<String> files = new ArrayList<>();
        files.add("inventories/main_shop.yml");
        files.add("inventories/categories/armors.yml");
        files.add("inventories/categories/blocks.yml");
        files.add("inventories/categories/farms.yml");
        files.add("inventories/categories/mics.yml");
        files.add("inventories/categories/mobs.yml");
        files.add("inventories/categories/ores.yml");
        files.add("inventories/categories/redstones.yml");
        files.add("inventories/shop_buy.yml");
        files.add("inventories/shop_sell.yml");
        files.add("inventories/confirm.yml");
        files.add("inventories/rank.yml");
        files.add("inventories/buy_more.yml");

        files.forEach(e -> {
            if (!new File(this.plugin.getDataFolder(), e).exists()) {

                if (NmsVersion.nmsVersion.isNewMaterial()) {
                    plugin.saveResource(e.replace("inventories/", "inventories/1_13/"), e, false);
                } else {
                    plugin.saveResource(e, false);
                }
            }
        });
    }

    /**
     * Register default patterns files
     */
    private void registerDefaultPatterns() {
        List<String> files = new ArrayList<>();
        files.add("patterns/decoration.yml");
        files.add("patterns/return_home.yml");
        files.add("patterns/pagination.yml");
        files.add("patterns/back.yml");
        files.add("patterns/choose_amount.yml");

        files.forEach(e -> {
            if (!new File(this.plugin.getDataFolder(), e).exists()) {

                if (NmsVersion.nmsVersion.isNewMaterial()) {
                    plugin.saveResource(e.replace("patterns/", "patterns/1_13/"), e, false);
                } else {
                    plugin.saveResource(e, false);
                }
            }
        });
    }

    @Override
    public void registerPlaceholders() {
        LocalPlaceholder localPlaceholder = LocalPlaceholder.getInstance();

        /*localPlaceholder.register("item_max", (player, args) -> {
            PlayerCache playerCache = getCache(player);
            ItemButton itemButton = playerCache.getItemButton();
            return itemButton == null ? "0" : String.valueOf(itemButton.getMaxStack());
        });*/
        localPlaceholder.register("modifier_sell", (player, args) -> priceModifierPrice(getPriceModifier(player, PriceType.SELL), args.isEmpty()));
        localPlaceholder.register("modifier_buy", (player, args) -> priceModifierPrice(getPriceModifier(player, PriceType.BUY), args.isEmpty()));

        localPlaceholder.register("item_", new ItemButtonPlaceholder(this.plugin, this));
    }

    private String priceModifierPrice(Optional<PriceModifier> optional, boolean isValue) {
        if (isValue) {
            return optional.map(priceModifier -> String.valueOf(priceModifier.getModifier())).orElse("1");
        } else {
            double percent = (optional.map(PriceModifier::getModifier).orElse(1.0) * 100) - 100;
            return decimalFormat.format(percent);
        }
    }

    @Override
    public void loadInventories() {

        // Check if file exist
        File folder = new File(this.plugin.getDataFolder(), "inventories");
        if (!folder.exists()) {
            folder.mkdir();
            registerDefaultFiles();
        }

        InventoryManager inventoryManager = this.plugin.getIManager();
        inventoryManager.deleteInventories(this.plugin);
        this.itemButtons.clear();

        files(folder, file -> {
            try {
                inventoryManager.loadInventory(this.plugin, file);
            } catch (InventoryException e1) {
                e1.printStackTrace();
            }
        });
    }

    @Override
    public void loadPatterns() {
        // Check if file exist
        File folder = new File(this.plugin.getDataFolder(), "patterns");
        if (!folder.exists()) {
            folder.mkdir();
            registerDefaultPatterns();
        }

        PatternManager patternManager = this.plugin.getPatternManager();

        files(folder, file -> {
            try {
                patternManager.loadPattern(file);
            } catch (InventoryException e1) {
                e1.printStackTrace();
            }
        });
    }

    private void files(File folder, Consumer<File> consumer) {
        try (Stream<Path> s = Files.walk(Paths.get(folder.getPath()))) {
            s.skip(1).map(Path::toFile).filter(File::isFile).filter(e -> e.getName().endsWith(".yml")).forEach(consumer);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void loadCommands() {

        File file = new File(this.plugin.getDataFolder(), "commands.yml");
        if (!file.exists()) {
            this.plugin.saveResource("commands.yml", true);
        }

        CommandManager commandManager = this.plugin.getCManager();
        commandManager.unregisterCommands(this.plugin);
        commandManager.loadCommand(this.plugin, file);
    }

    @Override
    public List<String> getDefaultLore() {
        return Config.defaultLore;
    }

    @Override
    public String transformPrice(double price) {

        if (AbbreviateNumberConfig.enable) {
            String[] suffixes = {"", AbbreviateNumberConfig.thousand, AbbreviateNumberConfig.millions, AbbreviateNumberConfig.billion, AbbreviateNumberConfig.trillion, AbbreviateNumberConfig.quadrillion, AbbreviateNumberConfig.quintillion};

            if (price >= 1000) {
                long value = (long) price;
                int sNum = (int) (Math.log10(value) / 3);
                double sVal = value / Math.pow(1000.0, sNum);

                sVal = Double.parseDouble(new DecimalFormat("#.##").format(sVal).replace(",", "."));

                if (sVal % 1 != 0) return String.format("%.1f%s", sVal, suffixes[sNum]);
                else return String.format("%.0f%s", sVal, suffixes[sNum]);
            }
        }

        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(Locale.getDefault());
        decimalFormatSymbols.setDecimalSeparator(PriceFormatConfig.decimalSeparator);
        decimalFormatSymbols.setGroupingSeparator(PriceFormatConfig.groupingSeparator);

        DecimalFormat decimalFormat = new DecimalFormat(PriceFormatConfig.format, decimalFormatSymbols);
        decimalFormat.setMinimumIntegerDigits(PriceFormatConfig.minimumIntegerDigits);
        decimalFormat.setMaximumIntegerDigits(PriceFormatConfig.maximumIntegerDigits);
        decimalFormat.setMinimumFractionDigits(PriceFormatConfig.minimumFractionDigits);
        decimalFormat.setMaximumFractionDigits(PriceFormatConfig.maximumFractionDigits);

        return decimalFormat.format(price);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.cachePlayers.remove(event.getPlayer().getUniqueId());
    }

    @Override
    public void openBuy(Player player, ItemButton itemButton, String inventoryName) {
        this.openInventory(player, itemButton, inventoryName == null ? Config.buyInventoryName : inventoryName);
    }

    @Override
    public void openSell(Player player, ItemButton itemButton, String inventoryName) {
        this.openInventory(player, itemButton, inventoryName == null ? Config.sellInventoryName : inventoryName);
    }

    @Override
    public void openConfirm(Player player, ItemConfirmButton itemButton, String inventoryName) {
        this.openInventory(player, itemButton, inventoryName == null ? Config.confirmInventoryName : inventoryName);
    }

    /**
     * Open an inventory for purchase or sell an item
     *
     * @param player        who open inventory
     * @param economyAction current item
     * @param inventoryName inventory name (buy or sell)
     */
    private void openInventory(Player player, EconomyAction economyAction, String inventoryName) {
        PlayerCache playerCache = getCache(player);

        if (economyAction instanceof ItemButton button) {
            playerCache.setItemButton(button);
            playerCache.setItemAmount(button.getItemStack().parseAmount(player));
        }
        playerCache.setEconomyAction(economyAction);

        InventoryManager inventoryManager = this.plugin.getIManager();
        List<Inventory> inventories = new ArrayList<>();
        inventoryManager.getCurrentPlayerInventory(player).ifPresent(inventories::add);
        Optional<Inventory> optional = inventoryManager.getInventory(this.plugin, inventoryName);

        if (optional.isPresent()) inventoryManager.openInventory(player, optional.get(), 1, inventories);
        else message(this.plugin, player, Message.INVENTORY_NOT_FOUND, "%name%", inventoryName);
    }

    @Override
    public PlayerCache getCache(Player player) {
        if (this.cachePlayers.containsKey(player.getUniqueId())) {
            return this.cachePlayers.get(player.getUniqueId());
        }
        PlayerCache playerCache = new ZPlayerCache();
        this.cachePlayers.put(player.getUniqueId(), playerCache);
        return playerCache;
    }

    @Override
    public Optional<PriceModifier> getPriceModifier(Player player, PriceType priceType) {

        PlayerCache playerCache = getCache(player);
        PriceModifierCache cache = playerCache.getPriceModifier(priceType);

        if (!cache.isExpired()) return cache.getPriceModifier();

        Stream<PriceModifier> stream = Config.priceModifiers.stream().filter(modifier -> {
            // Check if type is the same and check if player has an effective permission
            return modifier.getType() == priceType && player.hasPermission(modifier.getPermission());
        });
        Optional<PriceModifier> optional = priceType == PriceType.BUY ? stream.min(Comparator.comparingDouble(PriceModifier::getModifier)) : stream.max(Comparator.comparingDouble(PriceModifier::getModifier));

        // 5 seconds of cache
        playerCache.setPriceModifier(priceType, new PriceModifierCache(System.currentTimeMillis() + 5000, optional));
        return optional;
    }

    @Override
    public Optional<ItemButton> getItemButton(Material material) {
        return getItemButton(material.name());
    }

    @Override
    public Optional<ItemButton> getItemButton(String material) {
        return this.itemButtons.stream().filter(button -> button.getItemStack().getMaterial() != null && button.getItemStack().getMaterial().equalsIgnoreCase(material)).findFirst();
    }

    @Override
    public Collection<ItemButton> getItemButtons() {
        return Collections.unmodifiableCollection(this.itemButtons);
    }

    @Override
    public Optional<ItemButton> getItemButton(Player player, ItemStack itemStack) {
        return this.itemButtons.stream().filter(button -> button.getItemStack().build(player, false).isSimilar(itemStack)).findFirst();
    }

    @Override
    public void onButtonLoad(ButtonLoadEvent event) {
        Button button = event.getButton();
        if (button instanceof ItemButton itemButton) {
            this.itemButtons.add(itemButton);
        }
    }

    @Override
    public void sellAll(Player player) {
        sellAllContent(player, player.getInventory());
    }

    public void sellAllContent(Player player, org.bukkit.inventory.Inventory inventory) {

        List<ShopAction> shopActions = new ArrayList<>();
        List<Pair<ItemStack, ItemButton>> buttons = this.itemButtons.stream().map(button -> {
            ItemStack itemStack = button.getItemStack().build(player, false);
            return new Pair<>(itemStack, button);
        }).toList();

        /* SCAN ITEMS */
        for (int slot = 0; slot != inventory.getContents().length; slot++) {
            ItemStack itemStack = inventory.getContents()[slot];
            if (itemStack == null) continue;

            Optional<ItemButton> optional = buttons.stream().filter(e -> e.first.isSimilar(itemStack)).map(e -> e.second).findFirst();
            if (optional.isEmpty()) continue;

            ItemButton itemButton = optional.get();
            if (!itemButton.canSell()) continue;

            double price = itemButton.getSellPrice(player, itemStack.getAmount());
            ShopAction shopAction = new ShopAction(itemStack.clone(), itemButton, price);
            shopActions.add(shopAction);
        }

        /* BUKKIT EVENT */
        ZShopSellAllEvent event = new ZShopSellAllEvent(player, shopActions);
        event.call();

        if (event.isCancelled()) return;
        /* END BUKKIT EVENT */

        /* SELL ITEMS */
        Map<ShopEconomy, Double> prices = new HashMap<>();
        shopActions.forEach(action -> {

            ItemButton button = action.getItemButton();
            ItemStack itemStack = action.getItemStack();

            int requestedAmount = itemStack.getAmount();
            int actualSellAmount = requestedAmount; // Amount that will actually be sold
            int newServerLimitAmount = 0;
            int newPlayerLimitAmount = 0;
            String material = button.getItemStack().getMaterial();

            Optional<Limit> optionalServer = button.getServerSellLimit();
            Optional<Limit> optionalPlayer = button.getPlayerSellLimit();
            LimiterManager limiterManager = this.plugin.getLimiterManager();

            /* SERVER LIMIT */
            if (optionalServer.isPresent()) {
                Limit serverSellLimit = optionalServer.get();
                int currentServerAmount = serverSellLimit.getAmount();
                int maxCanSell = serverSellLimit.getLimit() - currentServerAmount;

                if (maxCanSell <= 0) {
                    action.setTotalAmount(0);
                    return;
                }
                actualSellAmount = Math.min(actualSellAmount, maxCanSell);
                newServerLimitAmount = currentServerAmount + actualSellAmount;
            }
            /* END SERVER LIMIT */

            /* PLAYER LIMIT */
            if (optionalPlayer.isPresent()) {
                Limit playerSellLimit = optionalPlayer.get();
                Optional<PlayerLimit> optional = limiterManager.getLimit(player);
                int currentPlayerAmount = optional.map(e -> e.getSellAmount(material)).orElse(0);
                int maxCanSell = playerSellLimit.getLimit() - currentPlayerAmount;

                if (maxCanSell <= 0) {
                    action.setTotalAmount(0);
                    return;
                }
                actualSellAmount = Math.min(actualSellAmount, maxCanSell);
                newPlayerLimitAmount = currentPlayerAmount + actualSellAmount;
            }
            /* END PLAYER LIMIT */

            if (actualSellAmount != requestedAmount) {
                itemStack.setAmount(actualSellAmount);
                double actualPrice = button.getSellPrice(player, actualSellAmount);
                action.setPrice(actualPrice);
            }
            action.setTotalAmount(actualSellAmount);

            /* UPDATE LIMIT VALUES */
            if (optionalServer.isPresent()) optionalServer.get().setAmount(newServerLimitAmount);
            if (newPlayerLimitAmount > 0)
                limiterManager.getOrCreate(player).setSellAmount(material, newPlayerLimitAmount);
            /* END LIMIT VALUES */

            /* REMOVE ITEMS AND UPDATE MONEY */
            prices.put(button.getEconomy(), prices.getOrDefault(button.getEconomy(), 0.0) + action.getPrice());
            InventoryUtils.removeItem(inventory, itemStack, actualSellAmount);
        });

        if (prices.isEmpty()) {
            message(this.plugin, player, Message.SELL_ALL_EMPTY);
            return;
        }

        List<ShopAction> fixedShopActions = new ArrayList<>();

        shopActions.forEach(action -> {
            if (action.getTotalAmount() == 0) return;

            Optional<ShopAction> optional = fixedShopActions.stream().filter(e -> e.getItemStack().isSimilar(action.getItemStack())).findFirst();
            if (optional.isPresent()) {
                ShopAction currentAction = optional.get();
                currentAction.setPrice(currentAction.getPrice() + action.getPrice());
                currentAction.addAmount(action.getTotalAmount());
            } else fixedShopActions.add(action);
        });

        var translationManager = this.plugin.getTranslationManager();

        String results = toList(fixedShopActions.stream().map(action -> getMessage(Message.SELL_ALL_INFO, "%amount%", action.getTotalAmount(), "%item%", translationManager.translateItemStack(action.getItemStack()), "%price%", action.getItemButton().getEconomy().format(transformPrice(action.getPrice()), action.getPrice()))).collect(Collectors.toList()), Message.SELL_ALL_COLOR_SEPARATOR.msg(), Message.SELL_ALL_COLOR_INFO.msg());
        if (results == null) {
            Logger.info("Error with results on sellall !");
            player.sendMessage("§cError with results on sellall !");
        }

        String resultsReason = toList(fixedShopActions.stream().map(action -> getMessage(Config.depositAllLine, "%amount%", action.getTotalAmount(), "%item%", translationManager.translateItemStack(action.getItemStack()), "%price%", action.getItemButton().getEconomy().format(transformPrice(action.getPrice()), action.getPrice()))).collect(Collectors.toList()), "", "");
        prices.forEach((economy, price) -> economy.depositMoney(player, price, Config.depositAllReason.replace("%items%", resultsReason == null ? "" : resultsReason)));

        message(this.plugin, player, Message.SELL_ALL_MESSAGE, "%items%", results == null ? "ERROR" : results);

        if (LogConfig.enableLog) {
            String logMessage = LogConfig.sellAllMessage;

            logMessage = logMessage.replace("%items%", results == null ? "ERROR" : results);
            logMessage = logMessage.replace("%player%", player.getName());
            logMessage = logMessage.replace("%uuid%", player.getUniqueId().toString());

            if (LogConfig.enableLogInConsole) Logger.info(logMessage);

            if (LogConfig.enableLogInFile) {
                History history = new ZHistory(HistoryType.SELL, logMessage);
                this.plugin.getHistoryManager().asyncValue(player.getUniqueId(), history);
            }
        }
    }

    @Override
    public void sellHand(Player player, int amount) {

        ItemStack itemInHand = player.getItemInHand(); // Use old method for 1.8 support
        if (itemInHand.getType().equals(Material.AIR)) {
            message(this.plugin, player, Message.SELL_HAND_AIR);
            return;
        }

        Optional<ItemButton> optional = getItemButton(player, itemInHand);
        if (!optional.isPresent()) message(this.plugin, player, Message.SELL_HAND_EMPTY);
        else {
            ItemButton button = optional.get();
            if (button.canSell()) button.sell(player, amount);
            else message(this.plugin, player, Message.SELL_HAND_EMPTY);
        }
    }

    @Override
    public void sellAllHand(Player player) {
        sellHand(player, 0);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {

        if (event.isCancelled()) return;

        ItemStack itemStack = event.getItemInHand();
        EntityType type = getEntityType(itemStack);
        if (type == EntityType.UNKNOWN) return;

        Block block = event.getBlock();
        CreatureSpawner creatureSpawner = (CreatureSpawner) block.getState();
        creatureSpawner.setSpawnedType(type);
        creatureSpawner.update();
    }

    private EntityType getEntityType(ItemStack itemStack) {
        if (itemStack == null) return EntityType.UNKNOWN;

        return ItemStackSpawnerHelper.getEntityType(itemStack);
    }

    @Override
    public void openSellInventory(Player player) {
        org.bukkit.inventory.Inventory inventory = new SellInventoryHolder(this.plugin.getIManager().getMeta()).getInventory();
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof SellInventoryHolder) {
            Player player = (Player) event.getPlayer();
            sellAllContent(player, event.getInventory());

            for (@Nullable ItemStack content : event.getInventory().getContents()) {
                if (content != null) {
                    give(player, content);
                }
            }
        }
    }
}
