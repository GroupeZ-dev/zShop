package fr.maxlego08.shop.zcore.enums;

import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum Message {

    PREFIX("§8(§6zShop§8) ", "#757575(#f78d23zShop#757575) "),
    TIME_DAY("%02d %day% %02d %hour% %02d %minute% %02d %second%"),
    TIME_HOUR("%02d %hour% %02d minute(s) %02d %second%"),
    TIME_MINUTE("%02d %minute% %02d %second%"),
    TIME_SECOND("%02d %second%"),
    FORMAT_SECOND("second"),
    FORMAT_SECONDS("seconds"),
    FORMAT_MINUTE("minute"),
    FORMAT_MINUTES("minutes"),
    FORMAT_HOUR("hour"),
    FORMAT_HOURS("hours"),
    FORMAT_DAY("d"),
    FORMAT_DAYS("days"),
    COMMAND_SYNTAX_ERROR("§cYou must execute the command like this§7: §a%syntax%"),
    COMMAND_NO_PERMISSION("§cYou do not have permission to run this command."),
    COMMAND_NO_CONSOLE("§cOnly one player can execute this command."),
    COMMAND_NO_ARG("§cImpossible to find the command with its arguments."),
    COMMAND_SYNTAX_HELP("§f%syntax% §7» §7%description%"),
    RELOAD("§aYou have just reloaded the configuration files."),
    DESCRIPTION_RELOAD("Reload configuration files"),
    DESCRIPTION_LOGS("Display logs informations"),
    DESCRIPTION_CONVERT("Convert ShopGUIPlus config to zShop"),
    DESCRIPTION_SELL_ALL("Sell all items in your inventory"),
    DESCRIPTION_SELL_ALLHAND("Sell all the items that are in your hand"),
    DESCRIPTION_SELL_HAND("Sell the item in your hand"),
    CANT_SELL("§c✘ Unable to sell"),
    CANT_BUY("§c✘ Unable to buy"),
    INVENTORY_NOT_FOUND("§cImpossible to find the inventory §f%name%§c."),
    NOT_ENOUGH_PLACE("§cYour inventory is full."),
    SELL_ITEM("§fYou just sold §7x%amount% %item%§f for §b%price%§f.", "&fYou just sold #37e674x%amount% #10e674%item%&f for #49e4f2%price%&f."),
    BUY_ITEM("§fYou have just bought §7x%amount% %item%§f for §b%price%§f.", "&fYou have just bought #b188f7x%amount% #a77af5%item% &ffor #49e4f2%price%&f."),
    NOT_ENOUGH_ITEMS("§cYou do not have enough items in your inventory."),
    NOT_ITEMS("§cYou do not have this item in your inventory."),
    LIMIT_SERVER_BUY("§cYou cannot buy this, the server has exceeded the purchasing capacity."),
    LIMIT_SERVER_SELL("§cYou cannot sell this, the server has exceeded the selling capacity."),
    SELL_HAND_ALL("§aYou just sold §6%item% §afor §6%price%§a."),
    SELL_HAND_ALLITEM("§ax§e%amount% §2%item%§a"),
    SELL_HAND_EMPTY("§cUnable to find an item for sale."),
    SELL_HAND_AIR("§cYou can't sell air ! It’s like you’re trying to sell something empty."),
    SELL_ALL_MESSAGE("§fYou gave just sold §7%items%§7."),
    SELL_ALL_INFO("§7x%amount% %item%§f for §b%price%"),
    SELL_ALL_EMPTY("§cYou can’t sell anything."),
    SELL_ALL_COLOR_SEPARATOR("§f"),
    SELL_ALL_COLOR_INFO("§7"),
    AND("and"),
    LOG_INFO_HEADER(MessageType.TCHAT, "§7§m-------------------------------------", "§b%type% §flog of §a%player% §8(§7%page%§8/§7%maxPage%§8)"),
    LOG_INFO_MESSAGE("§7%date% §8- §f%message%"),
    LOG_INFO_FOOTER("§7§m-------------------------------------"),
    LOG_INFO_NONE("§cAucune log pour le joueur §f%player%§c."),
    DESCRIPTION_RESET_LIMIT("Reset global or player limits for items"),
    DESCRIPTION_RESET_LIMIT_ALL("Reset all limits"),
    DESCRIPTION_RESET_LIMIT_PLAYER("Reset player limits for an item (all items if no item specified)"),
    DESCRIPTION_RESET_LIMIT_SERVER("Reset server limits for an item (all items if no item specified)"),
    RESET_LIMIT_ALL_SUCCESS("§aYou have successfully reset all limits."),
    RESET_LIMIT_NOT_FOUND("§cLimit not found for §f%item%§c."),
    RESET_LIMIT_PLAYERS_SUCCESS("§aYou have successfully reset the player limits for all items."),
    RESET_LIMIT_PLAYER_ITEM_SUCCESS("§aYou have successfully reset the player limits %type% for §f%item%§a."),
    RESET_LIMIT_SERVERS_SUCCESS("§aYou have successfully reset the server limits for all items."),
    RESET_LIMIT_SERVER_ITEM_SUCCESS("§aYou have successfully reset the server limits %type% for §f%item%§a.")

    ;

    private List<String> messages;
    private String message;
    private String messageNewVersion;
    private Map<String, Object> titles = new HashMap<>();
    private boolean use = true;
    private MessageType type = MessageType.TCHAT;

    private ItemStack itemStack;

    /**
     * @param message
     */
     Message(String message) {
        this.message = message;
    }

    Message(String message, String messageNewVersion) {
        this.message = message;
        this.messageNewVersion = messageNewVersion;
    }

    /**
     * @param title
     * @param subTitle
     * @param a
     * @param b
     * @param c
     */
     Message(String title, String subTitle, int a, int b, int c) {
        this.titles.put("title", title);
        this.titles.put("subtitle", subTitle);
        this.titles.put("start", a);
        this.titles.put("time", b);
        this.titles.put("end", c);
        this.titles.put("isUse", true);
        this.type = MessageType.TITLE;
    }

    /**
     * @param message
     */
    Message(String... message) {
        this.messages = Arrays.asList(message);
    }

    /**
     * @param message
     */
    Message(MessageType type, String... message) {
        this.messages = Arrays.asList(message);
        this.type = type;
    }

    /**
     * @param message
     */
    Message(MessageType type, String message) {
        this.message = message;
        this.type = type;
    }

    /**
     * @param message
     * @param use
     */
     Message(String message, boolean use) {
        this.message = message;
        this.use = use;
    }

    public String getMessage() {
        return message;
    }

    public String toMsg() {
        return message;
    }

    public String msg() {
        return message;
    }

    public boolean isUse() {
        return use;
    }

    public List<String> getMessages() {
        return messages == null ? Arrays.asList(message) : messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public boolean isMessage() {
        return messages != null && messages.size() > 1;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return (String) titles.get("title");
    }

    public Map<String, Object> getTitles() {
        return titles;
    }

    public void setTitles(Map<String, Object> titles) {
        this.titles = titles;
        this.type = MessageType.TITLE;
    }

    public String getSubTitle() {
        return (String) titles.get("subtitle");
    }

    public boolean isTitle() {
        return titles.containsKey("title");
    }

    public int getStart() {
        return ((Number) titles.get("start")).intValue();
    }

    public int getEnd() {
        return ((Number) titles.get("end")).intValue();
    }

    public int getTime() {
        return ((Number) titles.get("time")).intValue();
    }

    public boolean isUseTitle() {
        return (boolean) titles.getOrDefault("isUse", "true");
    }

    public String replace(String a, String b) {
        return message.replace(a, b);
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public String getMessageNewVersion() {
        return messageNewVersion;
    }

    public boolean isValid() {

        switch (type) {
            case ACTION:
                return this.message != null;
            case CENTER:
            case TCHAT:
                return this.message != null || !this.messages.isEmpty();
            case TITLE:
            case NONE:
                return true;
        }

        return true;
    }
}

