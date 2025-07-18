package fr.maxlego08.shop.zcore.utils;

import com.google.common.base.Strings;
import fr.maxlego08.shop.ShopPlugin;
import fr.maxlego08.shop.zcore.enums.EnumInventory;
import fr.maxlego08.shop.zcore.enums.Message;
import fr.maxlego08.shop.zcore.enums.Permission;
import fr.maxlego08.shop.zcore.utils.builder.CooldownBuilder;
import fr.maxlego08.shop.zcore.utils.builder.TimerBuilder;
import fr.maxlego08.shop.zcore.utils.nms.NmsVersion;
import fr.maxlego08.shop.zcore.utils.players.ActionBar;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("deprecation")
public abstract class ZUtils extends MessageUtils {

    private static final List<String> teleportPlayers = new ArrayList<String>();
    // For plugin support from 1.8 to 1.12
    private static Material[] byId;

    static {
        if (!NmsVersion.nmsVersion.isNewMaterial()) {
            byId = new Material[0];
            for (Material material : Material.values()) {
                if (byId.length > material.getId()) {
                    byId[material.getId()] = material;
                } else {
                    byId = Arrays.copyOfRange(byId, 0, material.getId() + 2);
                    byId[material.getId()] = material;
                }
            }
        }
    }

    /**
     * Allows to obtain a random number between a and b
     *
     * @param a
     * @param b
     * @return number between a and b
     */
    protected int getNumberBetween(int a, int b) {
        return ThreadLocalRandom.current().nextInt(a, b);
    }

    /**
     * Allows you to check if the inventory is full
     *
     * @param player
     * @return true if the player's inventory is full
     */
    public boolean hasInventoryFull(Player player) {
        int slot = 0;
        PlayerInventory inventory = player.getInventory();
        for (int a = 0; a != 36; a++) {
            ItemStack itemStack = inventory.getContents()[a];
            if (itemStack == null) slot++;
        }
        return slot == 0;
    }

    /**
     * Gives an item to the player, if the player's inventory is full then the
     * item will drop to the ground
     *
     * @param player
     * @param item
     */
    public void give(Player player, ItemStack item) {
        if (hasInventoryFull(player)) player.getWorld().dropItem(player.getLocation(), item);
        else player.getInventory().addItem(item);
    }

    public void giveItem(Player player, long value, ItemStack itemStack) {
        itemStack = itemStack.clone();
        if (value > 64) {
            value -= 64;
            itemStack.setAmount(64);
            give(player, itemStack);
            giveItem(player, value, itemStack);
        } else {
            itemStack.setAmount((int) value);
            give(player, itemStack);
        }
    }

    /**
     * Allows to return a material according to its ID Works only for plugins
     * from 1.8 to 1.12
     *
     * @param id
     * @return the material according to his id
     */
    protected Material getMaterial(int id) {
        return byId.length > id && id >= 0 ? byId[id] : Material.AIR;
    }

    /**
     * Allows to check if an itemstack has a display name
     *
     * @return boolean
     */
    protected boolean hasDisplayName(ItemStack itemStack) {
        return itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName();
    }

    protected boolean same(ItemStack itemStack, String name) {
        return this.hasDisplayName(itemStack) && itemStack.getItemMeta().getDisplayName().equals(name);
    }

    protected boolean contains(ItemStack itemStack, String name) {
        return this.hasDisplayName(itemStack) && itemStack.getItemMeta().getDisplayName().contains(name);
    }

    protected void removeItemInHand(Player player) {
        removeItemInHand(player, 64);
    }

    protected void removeItemInHand(Player player, int how) {
        if (player.getItemInHand().getAmount() > how)
            player.getItemInHand().setAmount(player.getItemInHand().getAmount() - how);
        else player.setItemInHand(new ItemStack(Material.AIR));
        player.updateInventory();
    }

    protected boolean same(Location l, Location l2) {
        return (l.getBlockX() == l2.getBlockX()) && (l.getBlockY() == l2.getBlockY()) && (l.getBlockZ() == l2.getBlockZ()) && l.getWorld().getName().equals(l2.getWorld().getName());
    }

    /**
     * Format a double in a String
     *
     * @param decimal
     * @return formatting current duplicate
     */
    protected String format(double decimal) {
        return format(decimal, "#.##");
    }

    /**
     * Format a double in a String
     *
     * @param decimal
     * @param format
     * @return formatting current double according to the given format
     */
    protected String format(double decimal, String format) {
        DecimalFormat decimalFormat = new DecimalFormat(format);
        return decimalFormat.format(decimal);
    }

    /**
     * Remove a certain number of items from a player's inventory
     *
     * @param player    - Player who will have items removed
     * @param amount    - Number of items to remove
     * @param itemStack - ItemStack to be removed
     */
    protected void removeItems(Player player, int amount, ItemStack itemStack) {
        int slot = 0;
        for (ItemStack is : player.getInventory().getContents()) {
            if (is != null && is.isSimilar(itemStack) && amount > 0) {
                int currentAmount = is.getAmount() - amount;
                amount -= is.getAmount();
                if (currentAmount <= 0) {
                    if (slot == 40) {
                        player.getInventory().setItemInOffHand(null);
                    } else {
                        player.getInventory().removeItem(is);
                    }
                } else {
                    is.setAmount(currentAmount);
                }
            }
            slot++;
        }
        player.updateInventory();
    }

    /**
     * @param delay
     * @param runnable
     */
    protected void schedule(long delay, Runnable runnable) {
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                if (runnable != null) runnable.run();
            }
        }, delay);
    }

    /**
     * @param string
     * @return
     */
    protected String name(String string) {
        String name = string.replace("_", " ").toLowerCase();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    /**
     * @param string
     * @return
     */
    protected String name(Material string) {
        String name = string.name().replace("_", " ").toLowerCase();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    protected String name(ItemStack itemStack) {
        return this.getItemName(itemStack);
    }

    /**
     * @param items
     * @return
     */
    protected int getMaxPage(Collection<?> items) {
        return (items.size() / 45) + 1;
    }

    /**
     * @param items
     * @param a
     * @return
     */
    protected int getMaxPage(Collection<?> items, int a) {
        return (items.size() / a) + 1;
    }

    /**
     * @param value
     * @param total
     * @return
     */
    protected double percent(double value, double total) {
        return (value * 100) / total;
    }

    /**
     * @param total
     * @param percent
     * @return
     */
    protected double percentNum(double total, double percent) {
        return total * (percent / 100);
    }

    /**
     * Schedule task with timer
     *
     * @param delay
     * @param count
     * @param runnable
     */
    protected void schedule(Plugin plugin, long delay, int count, Runnable runnable) {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            int tmpCount = 0;

            @Override
            public void run() {

                if (!plugin.isEnabled()) {
                    this.cancel();
                    return;
                }

                if (tmpCount > count) {
                    this.cancel();
                    return;
                }

                tmpCount++;
                Bukkit.getScheduler().runTask(plugin, runnable);

            }
        }, 0, delay);
    }

    protected void createInventory(ShopPlugin plugin, Player player, EnumInventory inventory) {
        createInventory(plugin, player, inventory, 1);
    }

    protected void createInventory(ShopPlugin plugin, Player player, EnumInventory inventory, int page) {
        createInventory(plugin, player, inventory, page, new Object() {
        });
    }

    protected void createInventory(ShopPlugin plugin, Player player, EnumInventory inventory, int page, Object... objects) {
        plugin.getInventoryManager().createInventory(inventory, player, page, objects);
    }

    /**
     * @param player
     * @param inventory
     * @param page
     * @param objects
     */
    protected void createInventory(ShopPlugin plugin, Player player, int inventory, int page, Object... objects) {
        plugin.getInventoryManager().createInventory(inventory, player, page, objects);
    }

    /**
     * @param permissible
     * @param permission
     * @return
     */
    protected boolean hasPermission(Permissible permissible, Permission permission) {
        return permissible.hasPermission(permission.getPermission());
    }

    /**
     * @param permissible
     * @param permission
     * @return
     */
    protected boolean hasPermission(Permissible permissible, String permission) {
        return permissible.hasPermission(permission);
    }

    protected TimerTask scheduleFix(Plugin plugin, long delay, BiConsumer<TimerTask, Boolean> consumer) {
        return this.scheduleFix(plugin, delay, delay, consumer);
    }

    protected TimerTask scheduleFix(Plugin plugin, long startAt, long delay, BiConsumer<TimerTask, Boolean> consumer) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (!plugin.isEnabled()) {
                    cancel();
                    consumer.accept(this, false);
                    return;
                }
                Bukkit.getScheduler().runTask(plugin, () -> consumer.accept(this, true));
            }
        };
        new Timer().scheduleAtFixedRate(task, startAt, delay);
        return task;
    }

    protected <T> T randomElement(List<T> element) {
        if (element.size() == 0) {
            return null;
        }
        if (element.size() == 1) {
            return element.get(0);
        }
        Random random = new Random();
        return element.get(random.nextInt(element.size()));
    }

    /**
     * @param message
     * @return
     */
    public String color(String message) {
        if (message == null) return null;
        if (NmsVersion.nmsVersion.isHexVersion()) {
            Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
            Matcher matcher = pattern.matcher(message);
            while (matcher.find()) {
                String color = message.substring(matcher.start(), matcher.end());
                message = message.replace(color, String.valueOf(net.md_5.bungee.api.ChatColor.of(color)));
                matcher = pattern.matcher(message);
            }
        }
        return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * @param message
     * @return
     */
    protected String colorReverse(String message) {
        return message == null ? null : message.replace("§", "&");
    }

    /**
     * @param messages
     * @return
     */
    protected List<String> color(List<String> messages) {
        return messages.stream().map(this::color).collect(Collectors.toList());
    }

    /**
     * @param messages
     * @return
     */
    protected List<String> colorReverse(List<String> messages) {
        return messages.stream().map(this::colorReverse).collect(Collectors.toList());
    }

    /**
     * @param flagString
     * @return
     */
    protected ItemFlag getFlag(String flagString) {
        for (ItemFlag flag : ItemFlag.values()) {
            if (flag.name().equalsIgnoreCase(flagString)) return flag;
        }
        return null;
    }

    /**
     * @param list
     * @return
     */
    protected <T> List<T> reverse(List<T> list) {
        List<T> tmpList = new ArrayList<>();
        for (int index = list.size() - 1; index != -1; index--)
            tmpList.add(list.get(index));
        return tmpList;
    }

    /**
     * @param price
     * @return
     */
    protected String price(long price) {
        return String.format("%,d", price);
    }

    /**
     * Allows to generate a string
     *
     * @param length
     * @return
     */
    protected String generateRandomString(int length) {
        RandomString randomString = new RandomString(length);
        return randomString.nextString();
    }

    /**
     * @param message
     * @return
     */
    protected TextComponent buildTextComponent(String message) {
        return new TextComponent(message);
    }

    protected TextComponent setHoverMessage(TextComponent component, String... messages) {
        BaseComponent[] list = new BaseComponent[messages.length];
        for (int a = 0; a != messages.length; a++)
            list[a] = new TextComponent(messages[a] + (messages.length - 1 == a ? "" : "\n"));
        component.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, list));
        return component;
    }

    protected TextComponent setHoverMessage(TextComponent component, List<String> messages) {
        BaseComponent[] list = new BaseComponent[messages.size()];
        for (int a = 0; a != messages.size(); a++)
            list[a] = new TextComponent(messages.get(a) + (messages.size() - 1 == a ? "" : "\n"));
        component.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, list));
        return component;
    }

    /**
     * @param component
     * @param action
     * @param command
     * @return
     */
    protected TextComponent setClickAction(TextComponent component, net.md_5.bungee.api.chat.ClickEvent.Action action, String command) {
        component.setClickEvent(new ClickEvent(action, command));
        return component;
    }

    /**
     * @param value
     * @return
     */
    protected String getDisplayBalence(double value) {
        if (value < 10000) return format(value, "#.#");
        else if (value < 1000000) return Integer.valueOf((int) (value / 1000)) + "k ";
        else if (value < 1000000000) return format((value / 1000) / 1000, "#.#") + "m ";
        else if (value < 1000000000000L) return Integer.valueOf((int) (((value / 1000) / 1000) / 1000)) + "M ";
        else return "to much";
    }

    /**
     * @param value
     * @return
     */
    protected String getDisplayBalence(long value) {
        if (value < 10000) return format(value, "#.#");
        else if (value < 1000000) return Integer.valueOf((int) (value / 1000)) + "k ";
        else if (value < 1000000000) return format((value / 1000) / 1000, "#.#") + "m ";
        else if (value < 1000000000000L) return Integer.valueOf((int) (((value / 1000) / 1000) / 1000)) + "M ";
        else return "to much";
    }

    /**
     * Allows you to count the number of items in inventory
     *
     * @param inventory
     * @param material
     * @return
     */
    protected int count(org.bukkit.inventory.Inventory inventory, Material material) {
        int count = 0;
        for (ItemStack itemStack : inventory.getContents())
            if (itemStack != null && itemStack.getType().equals(material)) count += itemStack.getAmount();
        return count;
    }

    protected Enchantment enchantFromString(String str) {
        for (Enchantment enchantment : Enchantment.values())
            if (enchantment.getName().equalsIgnoreCase(str)) return enchantment;
        return null;
    }

    /**
     * @param direction
     * @return
     */
    protected BlockFace getClosestFace(float direction) {

        direction = direction % 360;

        if (direction < 0) direction += 360;

        direction = Math.round(direction / 45);

        switch ((int) direction) {
            case 0:
                return BlockFace.WEST;
            case 1:
                return BlockFace.NORTH_WEST;
            case 2:
                return BlockFace.NORTH;
            case 3:
                return BlockFace.NORTH_EAST;
            case 4:
                return BlockFace.EAST;
            case 5:
                return BlockFace.SOUTH_EAST;
            case 6:
                return BlockFace.SOUTH;
            case 7:
                return BlockFace.SOUTH_WEST;
            default:
                return BlockFace.WEST;
        }
    }

    /**
     * @param price
     * @return
     */
    protected String betterPrice(long price) {
        String betterPrice = "";
        String[] splitPrice = String.valueOf(price).split("");
        int current = 0;
        for (int a = splitPrice.length - 1; a > -1; a--) {
            current++;
            if (current > 3) {
                betterPrice += ".";
                current = 1;
            }
            betterPrice += splitPrice[a];
        }
        StringBuilder builder = new StringBuilder().append(betterPrice);
        builder.reverse();
        return builder.toString();
    }

    /**
     * @param enchantment
     * @param itemStack
     * @return
     */
    protected boolean hasEnchant(Enchantment enchantment, ItemStack itemStack) {
        return itemStack.hasItemMeta() && itemStack.getItemMeta().hasEnchants() && itemStack.getItemMeta().hasEnchant(enchantment);
    }

    /**
     * @param player
     * @param cooldown
     * @return
     */
    protected String timerFormat(Player player, String cooldown) {
        return TimerBuilder.getStringTime(CooldownBuilder.getCooldownPlayer(cooldown, player) / 1000);
    }

    /**
     * @param player
     * @param cooldown
     * @return
     */
    protected boolean isCooldown(Player player, String cooldown) {
        return isCooldown(player, cooldown, 0);
    }

    /**
     * @param player
     * @param cooldown
     * @param timer
     * @return
     */
    protected boolean isCooldown(Player player, String cooldown, int timer) {
        if (CooldownBuilder.isCooldown(cooldown, player)) {
            ActionBar.sendActionBar(player, String.format("§cVous devez attendre encore §6%s §cavant de pouvoir faire cette action.", timerFormat(player, cooldown)));
            return true;
        }
        if (timer > 0) CooldownBuilder.addCooldown(cooldown, player, timer);
        return false;
    }

    /**
     * @param list
     * @return
     */
    protected String toList(Stream<String> list) {
        return toList(list.collect(Collectors.toList()), "§e", "§6");
    }

    /**
     * @param list
     * @return
     */
    protected String toList(List<String> list) {
        return toList(list, "§e", "§6§n");
    }

    protected String toList(List<String> list, String color, String color2) {
        if (list == null || list.size() == 0) return null;
        if (list.size() == 1) return list.get(0);
        StringBuilder stringBuilder = new StringBuilder();
        for (int a = 0; a != list.size(); a++) {
            if (a == list.size() - 1)
                stringBuilder.append(color).append(" ").append(Message.AND.msg()).append(" ").append(color2);
            else if (a != 0) stringBuilder.append(color).append(", ").append(color2);
            stringBuilder.append(list.get(a));
        }
        return stringBuilder.toString();
    }

    /**
     * @param message
     * @return
     */
    protected String removeColor(String message) {
        for (ChatColor color : ChatColor.values())
            message = message.replace("§" + color.getChar(), "").replace("&" + color.getChar(), "");
        return message;
    }

    /**
     * @param l
     * @return
     */
    protected String format(long l) {
        return format(l, ' ');
    }

    /**
     * @param l
     * @param c
     * @return
     */
    protected String format(long l, char c) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(c);
        formatter.setDecimalFormatSymbols(symbols);
        return formatter.format(l);
    }

    /**
     * Permet d'obtenir la tête d'un joueur en utilisation le système de
     * configuration des inventaires
     *
     * @param itemStack
     * @param player
     * @return itemstack
     */
    public ItemStack playerHead(ItemStack itemStack, OfflinePlayer player) {
        String name = itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() ? itemStack.getItemMeta().getDisplayName() : null;
        if (NmsVersion.nmsVersion.isNewMaterial()) {
            if (itemStack.getType().equals(Material.PLAYER_HEAD) && name != null && name.startsWith("HEAD")) {
                SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
                name = name.replace("HEAD", "");
                if (name.length() == 0) meta.setDisplayName(null);
                else meta.setDisplayName(name);
                meta.setOwningPlayer(player);
                itemStack.setItemMeta(meta);
            }
        } else {
            if (itemStack.getType().equals(getMaterial(397)) && itemStack.getData().getData() == 3 && name != null && name.startsWith("HEAD")) {
                SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
                name = name.replace("HEAD", "");
                if (name.length() == 0) meta.setDisplayName(null);
                else meta.setDisplayName(name);
                meta.setOwner(player.getName());
                itemStack.setItemMeta(meta);
            }
        }
        return itemStack;
    }

    /**
     * Allows you to get an itemstack to create a player's head
     *
     * @return itemstack
     */
    protected ItemStack playerHead() {
        return NmsVersion.nmsVersion.isNewMaterial() ? new ItemStack(Material.PLAYER_HEAD) : new ItemStack(getMaterial(397), 1, (byte) 3);
    }

    /**
     * Allows to obtain a class according to the provider
     *
     * @param plugin
     * @param classz
     * @return T
     */
    protected <T> T getProvider(Plugin plugin, Class<T> classz) {
        RegisteredServiceProvider<T> provider = plugin.getServer().getServicesManager().getRegistration(classz);
        if (provider == null) return null;
        return provider.getProvider() != null ? provider.getProvider() : null;
    }

    /**
     * @param configuration
     * @return
     */
    protected PotionEffectType getPotion(String configuration) {
        for (PotionEffectType effectType : PotionEffectType.values()) {
            if (effectType.getName().equalsIgnoreCase(configuration)) {
                return effectType;
            }
        }
        return null;
    }

    protected void runAsync(ShopPlugin plugin, Runnable runnable) {
        plugin.getIManager().getScheduler().runAsync(w -> runnable.run());
    }

    protected Object getPrivateField(Object object, String field) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Class<?> clazz = object.getClass();
        Field objectField = field.equals("commandMap") ? clazz.getDeclaredField(field) : field.equals("knownCommands") ? NmsVersion.nmsVersion.isNewMaterial() ? clazz.getSuperclass().getDeclaredField(field) : clazz.getDeclaredField(field) : null;
        objectField.setAccessible(true);
        Object result = objectField.get(object);
        objectField.setAccessible(false);
        return result;
    }

    protected void unRegisterBukkitCommand(Plugin plugin, PluginCommand command) {
        try {
            Object result = getPrivateField(plugin.getServer().getPluginManager(), "commandMap");
            SimpleCommandMap commandMap = (SimpleCommandMap) result;

            Object map = getPrivateField(commandMap, "knownCommands");
            @SuppressWarnings("unchecked") HashMap<String, Command> knownCommands = (HashMap<String, Command>) map;
            knownCommands.remove(command.getName());
            for (String alias : command.getAliases()) {
                knownCommands.remove(alias);
            }
            knownCommands.remove(plugin.getName() + ":" + command.getName());
            for (String alias : command.getAliases()) {
                knownCommands.remove(plugin.getName() + ":" + alias);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void clearPlayer(Player player) {
        player.getInventory().clear();
        player.getInventory().setBoots(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setHelmet(null);
        player.getPlayer().setItemOnCursor(null);
        player.getPlayer().setFireTicks(0);
        player.getPlayer().getOpenInventory().getTopInventory().clear();
        player.setGameMode(GameMode.SURVIVAL);
        player.getPlayer().getActivePotionEffects().forEach(e -> {
            player.getPlayer().removePotionEffect(e.getType());
        });
    }

    public String getProgressBar(int current, int max, int totalBars, char symbol, String completedColor, String notCompletedColor) {
        float percent = (float) current / max;
        int progressBars = (int) (totalBars * percent);

        return Strings.repeat(completedColor + symbol, progressBars) + Strings.repeat(notCompletedColor + symbol, totalBars - progressBars);
    }

    public String getProgressBar(int current, int max, ProgressBar progressBar) {
        return this.getProgressBar(current, max, progressBar.getLenght(), progressBar.getSymbol(), progressBar.getCompletedColor(), progressBar.getNotCompletedColor());
    }

    protected boolean inventoryHasItem(Player player) {

        ItemStack itemStack = player.getInventory().getBoots();
        if (itemStack != null) {
            return true;
        }

        itemStack = player.getInventory().getChestplate();
        if (itemStack != null) {
            return true;
        }

        itemStack = player.getInventory().getLeggings();
        if (itemStack != null) {
            return true;
        }

        itemStack = player.getInventory().getHelmet();
        if (itemStack != null) {
            return true;
        }

        for (ItemStack itemStack1 : player.getInventory().getContents()) {
            if (itemStack1 != null) {
                return true;
            }
        }

        return false;
    }

}
