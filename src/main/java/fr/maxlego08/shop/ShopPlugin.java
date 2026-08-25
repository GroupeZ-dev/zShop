package fr.maxlego08.shop;

import fr.maxlego08.menu.api.ButtonManager;
import fr.maxlego08.menu.api.InventoryManager;
import fr.maxlego08.menu.api.command.CommandManager;
import fr.maxlego08.menu.api.loader.NoneLoader;
import fr.maxlego08.menu.api.pattern.PatternManager;
import fr.maxlego08.shop.api.ShopManager;
import fr.maxlego08.shop.api.economy.EconomyManager;
import fr.maxlego08.shop.api.history.HistoryManager;
import fr.maxlego08.shop.api.limit.LimiterManager;
import fr.maxlego08.shop.api.utils.TranslationManager;
import fr.maxlego08.shop.buttons.ZConfirmBuyButton;
import fr.maxlego08.shop.buttons.ZConfirmSellButton;
import fr.maxlego08.shop.buttons.ZShowConfirmItemButton;
import fr.maxlego08.shop.command.commands.CommandSell;
import fr.maxlego08.shop.command.commands.CommandSellAll;
import fr.maxlego08.shop.command.commands.CommandSellAllHand;
import fr.maxlego08.shop.command.commands.CommandSellHand;
import fr.maxlego08.shop.command.commands.CommandSellInventory;
import fr.maxlego08.shop.command.commands.CommandShop;
import fr.maxlego08.shop.economy.ZEconomyManager;
import fr.maxlego08.shop.history.ZHistoryManager;
import fr.maxlego08.shop.limit.ZLimitManager;
import fr.maxlego08.shop.listener.MenuListener;
import fr.maxlego08.shop.loader.AddButtonLoader;
import fr.maxlego08.shop.loader.BuyMoreLoader;
import fr.maxlego08.shop.loader.ItemButtonLoader;
import fr.maxlego08.shop.loader.ItemConfirmButtonLoader;
import fr.maxlego08.shop.loader.RemoveButtonLoader;
import fr.maxlego08.shop.loader.ShowItemButtonLoader;
import fr.maxlego08.shop.placeholder.LocalPlaceholder;
import fr.maxlego08.shop.placeholder.Placeholder;
import fr.maxlego08.shop.save.Config;
import fr.maxlego08.shop.save.MessageLoader;
import fr.maxlego08.shop.zcore.ZPlugin;
import fr.maxlego08.shop.zcore.logger.Logger;
import fr.maxlego08.shop.zcore.utils.ZTranslationManager;
import fr.maxlego08.shop.zcore.utils.plugins.Metrics;
import fr.maxlego08.shop.zcore.utils.plugins.Plugins;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import dev.faststats.bukkit.BukkitContext;
/**
 * System to create your plugins very simply Projet:
 * <a href="https://github.com/Maxlego08/TemplatePlugin">https://github.com/Maxlego08/TemplatePlugin</a>
 *
 * @author Maxlego08
 */
public class ShopPlugin extends ZPlugin {

    private final EconomyManager economyManager = new ZEconomyManager(this);
    private final ShopManager shopManager = new ZShopManager(this);
    private final ZLimitManager limitManager = new ZLimitManager(this);
    private final TranslationManager translationManager = new ZTranslationManager(this);
    public int minimumVersion = 1100;
    private HistoryManager historyManager;
    private InventoryManager inventoryManager;
    private CommandManager commandManager;
    private PatternManager patternManager;
    private ButtonManager buttonManager;
    private boolean isLoad = false;
    private final BukkitContext context = new BukkitContext.Factory(this, "ccfaa9ae0f7e0df979de73a0c8cb61a2")
            .metrics(dev.faststats.Metrics.Factory::create)
            .create();

    private String transformNumberToString(int number) {
        String numberStr = String.valueOf(number);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < numberStr.length(); i++) {
            result.append(numberStr.charAt(i));
            if (i < numberStr.length() - 1) {
                result.append('.');
            }
        }
        return result.toString();
    }

    @Override
    public void onEnable() {

        Plugin plugin = getPlugin(Plugins.ZMENU);
        int version = Integer.parseInt(plugin.getDescription().getVersion().replace(".", ""));
        if (version < this.minimumVersion) {
            Logger.info("");
            Logger.info("");
            Logger.info("You must use a newer version of zMenu. Minimum version: " + transformNumberToString(this.minimumVersion), Logger.LogType.ERROR);
            Logger.info("");
            Logger.info("");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        LocalPlaceholder placeholder = LocalPlaceholder.getInstance();
        placeholder.setPrefix("zshop");

        this.preEnable();

        if (isEnable(Plugins.PLACEHOLDER)) Placeholder.getPlaceholder();

        this.historyManager = new ZHistoryManager(this, this.getPersist());

        ServicesManager manager = this.getServer().getServicesManager();
        manager.register(EconomyManager.class, economyManager, this, ServicePriority.Normal);
        manager.register(ShopManager.class, shopManager, this, ServicePriority.Normal);
        manager.register(LimiterManager.class, limitManager, this, ServicePriority.Normal);
        manager.register(HistoryManager.class, historyManager, this, ServicePriority.Normal);

        this.inventoryManager = this.getProvider(InventoryManager.class);
        this.commandManager = this.getProvider(CommandManager.class);
        this.patternManager = this.getProvider(PatternManager.class);
        this.buttonManager = this.getProvider(ButtonManager.class);

        var menuListener = new MenuListener(this.shopManager);
        this.inventoryManager.registerFastEvent(this, menuListener);
        this.addListener(menuListener);

        this.registerCommand("zshoplugin", new CommandShop(this), "zpl");
        this.registerCommand("sell-all", new CommandSellAll(this), "zshop-sell-all");
        this.registerCommand("sell-hand", new CommandSellHand(this), "zshop-hand");
        this.registerCommand("sell-handall", new CommandSellAllHand(this), "zshop-handall");

        this.addSave(new MessageLoader(this));
        Config.load((YamlConfiguration) getConfig());

        Bukkit.getPluginManager().registerEvents(this.limitManager, this);
        Bukkit.getPluginManager().registerEvents(this.historyManager, this);
        this.addListener(this.shopManager);

        // Load limiter before
        this.limitManager.load(this.getPersist());

        this.saveDefaultConfig();
        this.economyManager.loadEconomies();
        this.loadButtons();

        this.loadFiles();
        this.shopManager.loadConfig();

        new Metrics(this, 5881);
        context.ready();

        this.limitManager.registerPlaceholders();
        this.shopManager.registerPlaceholders();

        this.limitManager.deletes();
        this.limitManager.verifyPlayersLimit();

        this.translationManager.loadTranslations();

        this.isLoad = true;

        if (Config.enableSellCommand) {
            this.registerCommand("sell", new CommandSell(this), "zsell");
        }

        if (Config.enableSellInventoryCommand) {
            this.registerCommand("sellinventory", new CommandSellInventory(this), "zsellinventory", "zsi", "si");
        }

        this.postEnable();
    }

    @Override
    public void onDisable() {

        this.preDisable();

        if (this.isLoad) {
            this.limitManager.save(this.getPersist());
            this.saveFiles();
        }

        context.shutdown();
        this.postDisable();
    }

    @Override
    public void reloadFiles() {

        this.limitManager.invalid();

        reloadConfig();
        Config.load((YamlConfiguration) getConfig());

        super.reloadFiles();
        this.reloadConfig();
        this.economyManager.loadEconomies();
        this.shopManager.loadConfig();

        this.limitManager.deletes();
        this.limitManager.verifyPlayersLimit();

        this.translationManager.loadTranslations();
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public InventoryManager getIManager() {
        return inventoryManager;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public CommandManager getCManager() {
        return commandManager;
    }

    public PatternManager getPatternManager() {
        return patternManager;
    }

    private void loadButtons() {

        this.buttonManager.unregisters(this);
        this.buttonManager.register(new ItemButtonLoader(this));
        this.buttonManager.register(new ItemConfirmButtonLoader(this));
        this.buttonManager.register(new ShowItemButtonLoader(this));
        this.buttonManager.register(new AddButtonLoader(this));
        this.buttonManager.register(new BuyMoreLoader(this));
        this.buttonManager.register(new RemoveButtonLoader(this));
        this.buttonManager.register(new NoneLoader(this, ZConfirmBuyButton.class, "zshop_confirm_buy"));
        this.buttonManager.register(new NoneLoader(this, ZConfirmSellButton.class, "zshop_confirm_sell"));
        this.buttonManager.register(new NoneLoader(this, ZShowConfirmItemButton.class, "zshop_show_confirm"));

    }

    public LimiterManager getLimiterManager() {
        return limitManager;
    }

    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    public ButtonManager getButtonManager() {
        return buttonManager;
    }

    public TranslationManager getTranslationManager() {
        return translationManager;
    }
}
