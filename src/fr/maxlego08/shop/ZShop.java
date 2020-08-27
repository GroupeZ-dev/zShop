package fr.maxlego08.shop;

import org.bukkit.plugin.ServicePriority;

import fr.maxlego08.shop.api.IEconomy;
import fr.maxlego08.shop.api.ShopManager;
import fr.maxlego08.shop.command.CommandManager;
import fr.maxlego08.shop.inventory.InventoryLoader;
import fr.maxlego08.shop.inventory.InventoryManager;
import fr.maxlego08.shop.inventory.inventories.InventoryShop;
import fr.maxlego08.shop.inventory.inventories.InventoryConfirm;
import fr.maxlego08.shop.inventory.inventories.InventoryDefault;
import fr.maxlego08.shop.listener.AdapterListener;
import fr.maxlego08.shop.zcore.ZPlugin;
import fr.maxlego08.shop.zcore.enums.EnumInventory;

public class ZShop extends ZPlugin {

	private final IEconomy economy = new ZEconomy(this);
	private ShopManager shopManager = new ZShopManager(this, economy);
	private final fr.maxlego08.shop.api.InventoryManager inventory = new InventoryLoader(this, economy);

	@Override
	public void onEnable() {

		preEnable();

		commandManager = new CommandManager(this);

		if (!isEnabled())
			return;

		inventoryManager = new InventoryManager(this);

		/* Register provider */
		getServer().getServicesManager().register(fr.maxlego08.shop.api.InventoryManager.class, inventory, this,
				ServicePriority.High);

		/* Load inventories */
		try {
			inventory.loadInventories();
		} catch (Exception e) {
			e.printStackTrace();
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		/* Load Commands */
		try {
			shopManager.loadCommands();
		} catch (Exception e) {
			e.printStackTrace();
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		this.registerInventory(EnumInventory.INVENTORY_DEFAULT, new InventoryDefault());
		this.registerInventory(EnumInventory.INVENTORY_SHOP, new InventoryShop());
		this.registerInventory(EnumInventory.INVENTORY_CONFIRM, new InventoryConfirm());

		/* Add Listener */

		addListener(new AdapterListener(this));
		addListener(inventoryManager);

		/* Add Saver */
		// addSave(Config.getInstance());
		// addSave(new CooldownBuilder());

		getSavers().forEach(saver -> saver.load(getPersist()));

		postEnable();
	}

	@Override
	public void onDisable() {

		if (!isLoaded) {
			getLog().log("=== IMPOSSIBLE TO SWITCH OFF THE PLUGIN PROPERLY ===");
			return;
		}

		preDisable();

		getSavers().forEach(saver -> saver.save(getPersist()));

		postDisable();

	}

	public fr.maxlego08.shop.api.InventoryManager getInventory() {
		return inventory;
	}

	public ShopManager getShopManager() {
		return shopManager;
	}

}
