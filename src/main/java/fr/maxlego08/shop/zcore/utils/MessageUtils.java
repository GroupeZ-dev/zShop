package fr.maxlego08.shop.zcore.utils;

import fr.maxlego08.menu.api.utils.MetaUpdater;
import fr.maxlego08.shop.ShopPlugin;
import fr.maxlego08.shop.zcore.enums.Message;
import fr.maxlego08.shop.zcore.utils.nms.NmsVersion;
import fr.maxlego08.shop.zcore.utils.players.ActionBar;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Allows you to manage messages sent to players and the console
 *
 * @author Maxence
 */
public abstract class MessageUtils extends LocationUtils {

    public static String removeColorCodes(String input) {
        input = input.replaceAll("#[0-9a-fA-F]{6}", "");
        input = input.replaceAll("§[0-9a-fA-Fk-oK-OrR]", "");
        return input.replaceAll("&[0-9a-fA-Fk-oK-OrR]", "");
    }

    /**
     * @param player
     * @param message
     * @param args
     */
    public void messageWO(CommandSender player, Message message, Object... args) {
        player.sendMessage(getMessage(message, args));
    }

    /**
     * @param sender
     * @param message
     * @param args
     */
    public void messageWO(ShopPlugin plugin, CommandSender sender, Message message, Object... args) {
        MetaUpdater updater = plugin.getIManager().getMeta();

        if (sender instanceof ConsoleCommandSender) {
            if (message.getMessages().size() > 0) {
                message.getMessages().forEach(msg -> sender.sendMessage(removeColorCodes(Message.PREFIX.msg() + getMessage(msg, args))));
            } else {
                sender.sendMessage(removeColorCodes(Message.PREFIX.msg() + getMessage(message, args)));
            }
        } else {
            Player player = (Player) sender;
            if (message.getMessages().size() > 0) {
                message.getMessages().forEach(msg -> updater.sendMessage(sender, this.papi(getMessage(msg, args), player)));
            } else {
                updater.sendMessage(sender, this.papi(getMessage(message, args), player));
            }
        }
    }

    /**
     * @param player
     * @param message
     * @param args
     */
    public void messageWO(CommandSender player, String message, Object... args) {
        player.sendMessage(getMessage(message, args));
    }

    /**
     * @param sender
     * @param message
     * @param args
     */
    public void message(ShopPlugin plugin, CommandSender sender, String message, Object... args) {
        plugin.getIManager().getMeta().sendMessage(sender, Message.PREFIX.msg() + getMessage(message, args));
    }

    /**
     * Allows you to send a message to a command sender
     *
     * @param sender  User who sent the command
     * @param message The message - Using the Message enum for simplified message
     *                management
     * @param args    The arguments - The arguments work in pairs, you must put for
     *                example %test% and then the value
     */
    public void message(ShopPlugin plugin, CommandSender sender, Message message, Object... args) {

        MetaUpdater updater = plugin.getIManager().getMeta();

        if (sender instanceof ConsoleCommandSender) {
            if (message.getMessages().size() > 0) {
                message.getMessages().forEach(msg -> sender.sendMessage(removeColorCodes(Message.PREFIX.msg() + getMessage(msg, args))));
            } else {
                sender.sendMessage(removeColorCodes(Message.PREFIX.msg() + getMessage(message, args)));
            }
        } else {

            Player player = (Player) sender;
            switch (message.getType()) {
                case CENTER:
                    if (message.getMessages().size() > 0) {
                        message.getMessages().forEach(msg -> updater.sendMessage(sender, this.getCenteredMessage(this.papi(getMessage(msg, args), player))));
                    } else {
                        updater.sendMessage(sender, this.getCenteredMessage(this.papi(getMessage(message, args), player)));
                    }

                    break;
                case ACTION:
                    this.actionMessage(player, message, args);
                    break;
                case TCHAT:
                    if (message.getMessages().size() > 0) {
                        message.getMessages().forEach(msg -> updater.sendMessage(sender, this.papi(Message.PREFIX.msg() + getMessage(msg, args), player)));
                    } else {
                        updater.sendMessage(sender, this.papi(Message.PREFIX.msg() + getMessage(message, args), player));
                    }
                    break;
                case TITLE:
                    // title message management
                    String title = message.getTitle();
                    String subTitle = message.getSubTitle();
                    int fadeInTime = message.getStart();
                    int showTime = message.getTime();
                    int fadeOutTime = message.getEnd();
                    this.title(player, this.papi(this.getMessage(title, args), player), this.papi(this.getMessage(subTitle, args), player), fadeInTime, showTime, fadeOutTime);
                    break;
                default:
                    break;

            }

        }
    }

    public void broadcast(ShopPlugin plugin, Message message, Object... args) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            message(plugin, player, message, args);
        }
        message(plugin, Bukkit.getConsoleSender(), message, args);
    }

    public void actionMessage(Player player, Message message, Object... args) {
        ActionBar.sendActionBar(player, this.papi(getMessage(message, args), player));
    }

    protected String getMessage(Message message, Object... args) {
        return getMessage(message.getMessage(), args);
    }

    public String getMessage(String message, Object... args) {
        if (args.length % 2 != 0) {
            System.err.println("Impossible to apply the method for messages.");
        } else {
            for (int a = 0; a < args.length; a += 2) {
                String replace = args[a].toString();
                String to = args[a + 1].toString();
                message = message.replace(replace, to);
            }
        }
        return message;
    }

    protected final Class<?> getNMSClass(String name) {
        try {
            return Class.forName("net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + "." + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void title(Player player, String title, String subtitle, int fadeInTime, int showTime, int fadeOutTime) {

        if (NmsVersion.nmsVersion.isNewMaterial()) {
            player.sendTitle(title, subtitle, fadeInTime, showTime, fadeOutTime);
            return;
        }

        try {
            Object chatTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\": \"" + title + "\"}");
            Constructor<?> titleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"), int.class, int.class, int.class);
            Object packet = titleConstructor.newInstance(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE").get(null), chatTitle, fadeInTime, showTime, fadeOutTime);

            Object chatsTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\": \"" + subtitle + "\"}");
            Constructor<?> timingTitleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent"), int.class, int.class, int.class);
            Object timingPacket = timingTitleConstructor.newInstance(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("SUBTITLE").get(null), chatsTitle, fadeInTime, showTime, fadeOutTime);

            sendPacket(player, packet);
            sendPacket(player, timingPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param player
     * @param packet
     */
    protected final void sendPacket(Player player, Object packet) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param message
     * @return message
     */
    protected String getCenteredMessage(String message) {
        if (message == null || message.equals("")) return "";
        message = ChatColor.translateAlternateColorCodes('&', message);

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for (char c : message.toCharArray()) {
            if (c == '§') {
                previousCode = true;
            } else if (previousCode) {
                previousCode = false;
                isBold = c == 'l' || c == 'L';
            } else {
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int CENTER_PX = 154;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while (compensated < toCompensate) {
            sb.append(" ");
            compensated += spaceLength;
        }
        return sb + message;
    }

    protected void broadcastCenterMessage(List<String> messages) {
        messages.stream().map(e -> e = getCenteredMessage(e)).forEach(e -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                messageWO(player, e);
            }
        });
    }

    protected void broadcastAction(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            ActionBar.sendActionBar(player, papi(message, player));
        }
    }

}
