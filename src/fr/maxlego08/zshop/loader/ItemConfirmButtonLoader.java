package fr.maxlego08.zshop.loader;

import fr.maxlego08.menu.api.button.Button;
import fr.maxlego08.menu.api.button.DefaultButtonValue;
import fr.maxlego08.menu.api.loader.ButtonLoader;
import fr.maxlego08.menu.api.requirement.Action;
import fr.maxlego08.zshop.ShopPlugin;
import fr.maxlego08.zshop.api.buttons.ItemConfirmButton;
import fr.maxlego08.zshop.api.economy.ShopEconomy;
import fr.maxlego08.zshop.buttons.ZItemConfirmButton;
import fr.maxlego08.zshop.exceptions.EconomyNotFoundException;
import fr.maxlego08.zshop.save.Config;
import fr.maxlego08.zshop.zcore.logger.Logger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ItemConfirmButtonLoader implements ButtonLoader {

    private final ShopPlugin plugin;

    public ItemConfirmButtonLoader(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Class<? extends Button> getButton() {
        return ItemConfirmButton.class;
    }

    @Override
    public String getName() {
        return "zshop_item_confirm";
    }

    @Override
    public Plugin getPlugin() {
        return this.plugin;
    }

    @Override
    public Button load(YamlConfiguration configuration, String path, DefaultButtonValue defaultButtonValue) {

        String defaultEconomy = configuration.getString("economy", Config.defaultEconomy);
        double price = Double.parseDouble(configuration.getString(path + "price", "0.0"));

        if (price == 0.0) {
            Logger.info("Attention, the price is 0 for " + path, Logger.LogType.ERROR);
        }

        boolean enableLog = configuration.getBoolean(path + "enableLog", true);
        String name = configuration.getString(path + "name", path);
        String reason = configuration.getString(path + "reason", "No reason");

        String economyName = configuration.getString(path + "economy", defaultEconomy);
        Optional<ShopEconomy> optional = plugin.getEconomyManager().getEconomy(economyName);
        if (!optional.isPresent()) {
            throw new EconomyNotFoundException("Economy " + economyName + " was not found for button " + path);
        }
        ShopEconomy shopEconomy = optional.get();
        String inventoryConfirm = configuration.getString(path + "inventoryConfirm", null);

        List<Action> confirmActions = new ArrayList<>();
        List<?> list = configuration.getList(path + "confirm-actions");
        if (list != null) {
            try {
                List<Map<String, Object>> maps = (List<Map<String, Object>>) list;
                confirmActions = this.plugin.getButtonManager().loadActions(maps, path, new File(plugin.getDataFolder(), "?.yml"));
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        return new ZItemConfirmButton(plugin, shopEconomy, price, enableLog, name, inventoryConfirm, reason, confirmActions);
    }
}
