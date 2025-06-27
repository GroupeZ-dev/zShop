package fr.maxlego08.shop.save;

import org.bukkit.configuration.file.YamlConfiguration;

public class LogConfig {

    public static boolean enableLog = true;
    public static boolean enableLogInFile = true;
    public static boolean enableLogInConsole = true;
    public static String dateFormatLog = "d/M/yyyy HH:mm:ss";
    public static long expireLogDay = 14;
    public static String buyMessage = "%player% (%uuid%) has purchased x%amount% %item% for %price%.";
    public static String buyConfirmMessage = "%player% (%uuid%) has purchased %name% for %price%.";
    public static String sellMessage = "%player% (%uuid%) has just sold x%amount% %item% for %price%.";
    public static String sellAllMessage = "%players% (%uuid%) sold %items% (Sell all)";

    private LogConfig() {
    }

    public static void load(YamlConfiguration configuration) {
        enableLog = configuration.getBoolean("log.enableLog");
        enableLogInFile = configuration.getBoolean("log.enableLogInFile");
        enableLogInConsole = configuration.getBoolean("log.enableLogInConsole");
        dateFormatLog = configuration.getString("log.dateFormatLog");
        expireLogDay = configuration.getInt("log.expireLogDay");
        buyMessage = configuration.getString("log.buyMessage");
        buyConfirmMessage = configuration.getString("log.buyConfirmMessage");
        sellMessage = configuration.getString("log.sellMessage");
        sellAllMessage = configuration.getString("log.sellAllMessage");
    }
}
