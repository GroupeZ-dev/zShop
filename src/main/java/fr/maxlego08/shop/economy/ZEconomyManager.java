package fr.maxlego08.shop.economy;

import fr.maxlego08.shop.ShopPlugin;
import fr.maxlego08.shop.api.economy.EconomyManager;
import fr.maxlego08.shop.api.economy.ShopEconomy;
import fr.maxlego08.shop.api.event.events.ZShopEconomyRegisterEvent;
import fr.maxlego08.shop.save.Config;
import fr.maxlego08.shop.zcore.logger.Logger;
import fr.traqueur.currencies.Currencies;
import fr.traqueur.currencies.CurrencyProvider;
import fr.traqueur.currencies.providers.ZMenuItemProvider;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class ZEconomyManager implements EconomyManager {

    private final ShopPlugin plugin;
    private final List<ShopEconomy> shopEconomies = new ArrayList<>();

    public ZEconomyManager(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Collection<ShopEconomy> getEconomies() {
        return Collections.unmodifiableCollection(this.shopEconomies);
    }

    @Override
    public boolean registerEconomy(ShopEconomy economy) {
        Optional<ShopEconomy> optional = getEconomy(economy.getName());
        if (!optional.isPresent()) {
            this.shopEconomies.add(economy);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeEconomy(ShopEconomy economy) {
        return this.shopEconomies.remove(economy);
    }

    @Override
    public Optional<ShopEconomy> getEconomy(String economyName) {
        return this.shopEconomies.stream().filter(e -> e.getName().equalsIgnoreCase(economyName)).findFirst();
    }

    @Override
    public void loadEconomies() {

        File file = new File(this.plugin.getDataFolder(), "economies.yml");
        if (!file.exists()) {
            this.plugin.saveResource("economies.yml", true);
        }

        this.shopEconomies.clear();

        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        for (String key : configuration.getConfigurationSection("economies.").getKeys(false)) {
            try {

                String path = "economies." + key + ".";

                if (!configuration.getBoolean(path + "isEnable", configuration.getBoolean(path + "is-enable")))
                    continue;

                String name = configuration.getString(path + "name", "VAULT");
                String type = configuration.getString(path + "type", "VAULT");
                String currency = configuration.getString(path + "currency", "$");
                String denyMessage = configuration.getString(path + "denyMessage", configuration.getString(path + "deny-message"));

                Currencies currencies = Currencies.valueOf(type.toUpperCase());
                CurrencyProvider currencyProvider = switch (currencies) {
                    case ZMENUITEMS, ITEM -> {
                        var itemStack = this.plugin.getIManager().loadItemStack(configuration, path + "item.", file);
                        yield new ZMenuItemProvider(plugin, itemStack);
                    }
                    case ZESSENTIALS, ECOBITS, COINSENGINE, REDISECONOMY, EXCELLENTEECONOMY -> {
                        String currencyName = configuration.getString(path + "currencyName", configuration.getString(path + "currency-name"));
                        yield currencies.createProvider(currencyName);
                    }
                    default -> currencies.createProvider();
                };

                if (Config.enableDebug) Logger.info("Register " + name + " economy");
                registerEconomy(new ZShopEconomy(name, currency, denyMessage, currencyProvider));
            } catch (Exception exception) {
                this.plugin.getLogger().severe("Error while loading " + key + " economy");
                exception.printStackTrace();
            }
        }

        ZShopEconomyRegisterEvent event = new ZShopEconomyRegisterEvent(this);
        event.call();
    }
}
