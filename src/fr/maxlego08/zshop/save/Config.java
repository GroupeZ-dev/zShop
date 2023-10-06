package fr.maxlego08.zshop.save;

import fr.maxlego08.zshop.zcore.utils.storage.Persist;
import fr.maxlego08.zshop.zcore.utils.storage.Saveable;

public class Config implements Saveable {

    public static boolean enableDebug = true;
    public static boolean enableDebugTime = false;
    public static String defaultEconomy = "VAULT";
    public static String sellInventoryName = "shop_sell";
    public static String buyInventoryName = "shop_buy";

    /**
     * static Singleton instance.
     */
    private static volatile Config instance;


    /**
     * Private constructor for singleton.
     */
    private Config() {
    }

    /**
     * Return a singleton instance of Config.
     */
    public static Config getInstance() {
        // Double lock for thread safety.
        if (instance == null) {
            synchronized (Config.class) {
                if (instance == null) {
                    instance = new Config();
                }
            }
        }
        return instance;
    }

    public void save(Persist persist) {
        persist.save(getInstance());
    }

    public void load(Persist persist) {
        persist.loadOrSaveDefault(getInstance(), Config.class);
    }

}
