package fr.maxlego08.shop.zcore.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;

import fr.maxlego08.shop.ZShop;
import fr.maxlego08.shop.zcore.ZPlugin;
import fr.maxlego08.shop.zcore.utils.enums.Message;
import fr.maxlego08.shop.zcore.utils.enums.Permission;
import net.milkbowl.vault.economy.Economy;

@SuppressWarnings("deprecation")
public abstract class ZUtils {

	private static transient List<String> teleportPlayers = new ArrayList<String>();
	protected transient ZShop plugin = ZShop.i();

	/**
	 * @param location
	 *            as String
	 * @return string as location
	 */
	protected Location changeStringLocationToLocation(String s) {
		String[] a = s.split(",");
		if (a.length == 6)
			return changeStringLocationToLocationEye(s);
		World w = Bukkit.getServer().getWorld(a[0]);
		float x = Float.parseFloat(a[1]);
		float y = Float.parseFloat(a[2]);
		float z = Float.parseFloat(a[3]);
		return new Location(w, x, y, z);
	}

	/**
	 * @param location
	 *            as string
	 * @return string as locaiton
	 */
	protected Location changeStringLocationToLocationEye(String s) {
		String[] a = s.split(",");
		World w = Bukkit.getServer().getWorld(a[0]);
		float x = Float.parseFloat(a[1]);
		float y = Float.parseFloat(a[2]);
		float z = Float.parseFloat(a[3]);
		float yaw = Float.parseFloat(a[4]);
		float pitch = Float.parseFloat(a[5]);
		return new Location(w, x, y, z, yaw, pitch);
	}

	/**
	 * @param location
	 * @return location as string
	 */
	protected String changeLocationToString(Location location) {
		String ret = location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + ","
				+ location.getBlockZ();
		return ret;
	}

	/**
	 * @param location
	 * @return location as String
	 */
	protected String changeLocationToStringEye(Location location) {
		String ret = location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + ","
				+ location.getBlockZ() + "," + location.getYaw() + "," + location.getPitch();
		return ret;
	}

	/**
	 * @param chunk
	 * @return string as Chunk
	 */
	protected Chunk changeStringChuncToChunk(String chunk) {
		String[] a = chunk.split(",");
		World w = Bukkit.getServer().getWorld(a[0]);
		return w.getChunkAt(Integer.valueOf(a[1]), Integer.valueOf(a[2]));
	}

	/**
	 * @param chunk
	 * @return chunk as string
	 */
	protected String changeChunkToString(Chunk chunk) {
		String c = chunk.getWorld().getName() + "," + chunk.getX() + "," + chunk.getZ();
		return c;
	}

	/**
	 * @param {@link
	 * 			Cuboid}
	 * @return cuboid as string
	 */
	protected String changeCuboidToString(Cuboid cuboid) {
		return cuboid.getWorld().getName() + "," + cuboid.getLowerX() + "," + cuboid.getLowerY() + ","
				+ cuboid.getLowerZ() + "," + ";" + cuboid.getWorld().getName() + "," + cuboid.getUpperX() + ","
				+ cuboid.getUpperY() + "," + cuboid.getUpperZ();
	}

	/**
	 * @param str
	 * @return {@link Cuboid}
	 */
	protected Cuboid changeStringToCuboid(String str) {

		String parsedCuboid[] = str.split(";");
		String parsedFirstLoc[] = parsedCuboid[0].split(",");
		String parsedSecondLoc[] = parsedCuboid[1].split(",");

		String firstWorldName = parsedFirstLoc[0];
		double firstX = Double.valueOf(parsedFirstLoc[1]);
		double firstY = Double.valueOf(parsedFirstLoc[2]);
		double firstZ = Double.valueOf(parsedFirstLoc[3]);

		String secondWorldName = parsedSecondLoc[0];
		double secondX = Double.valueOf(parsedSecondLoc[1]);
		double secondY = Double.valueOf(parsedSecondLoc[2]);
		double secondZ = Double.valueOf(parsedSecondLoc[3]);

		Location l1 = new Location(Bukkit.getWorld(firstWorldName), firstX, firstY, firstZ);

		Location l2 = new Location(Bukkit.getWorld(secondWorldName), secondX, secondY, secondZ);

		return new Cuboid(l1, l2);

	}

	/**
	 * @param item
	 * @return the encoded item
	 */
	protected String encode(ItemStack item) {
		return ItemDecoder.serializeItemStack(item);
	}

	/**
	 * @param item
	 * @return the decoded item
	 */
	protected ItemStack decode(String item) {
		return ItemDecoder.deserializeItemStack(item);
	}

	/**
	 * @param material
	 * @return he name of the material with a better format
	 */
	protected String betterMaterial(Material material) {
		return TextUtil.getMaterialLowerAndMajAndSpace(material);
	}

	/**
	 * @param a
	 * @param b
	 * @return number between a and b
	 */
	protected int getNumberBetween(int a, int b) {
		return ThreadLocalRandom.current().nextInt(a, b);
	}

	/**
	 * @param player
	 * @return true if the player's inventory is full
	 */
	protected boolean hasInventoryFull(Player player) {
		int slot = 0;
		ItemStack[] arrayOfItemStack;
		int x = (arrayOfItemStack = player.getInventory().getContents()).length;
		for (int i = 0; i < x; i++) {
			ItemStack contents = arrayOfItemStack[i];
			if ((contents == null))
				slot++;
		}
		return slot == 0;
	}

	protected boolean give(ItemStack item, Player player) {
		if (hasInventoryFull(player))
			return false;
		player.getInventory().addItem(item);
		return true;
	}

	/**
	 * Gives an item to the player, if the player's inventory is full then the
	 * item will drop to the ground
	 * 
	 * @param player
	 * @param item
	 */
	protected void give(Player player, ItemStack item) {
		if (hasInventoryFull(player))
			player.getWorld().dropItem(player.getLocation(), item);
		else
			player.getInventory().addItem(item);
	}

	private static transient Material[] byId;

	static {
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

	/**
	 * @param id
	 * @return the material according to his id
	 */
	protected Material getMaterial(int id) {
		return byId.length > id && id >= 0 ? byId[id] : null;
	}

	/**
	 * Check if the item name is the same as the given string
	 * 
	 * @param stack
	 * @param name
	 * @return true if the item name is the same as string
	 */
	protected boolean same(ItemStack stack, String name) {
		return stack.hasItemMeta() && stack.getItemMeta().hasDisplayName()
				&& stack.getItemMeta().getDisplayName().equals(name);
	}

	/**
	 * Check if the item name contains the given string
	 * 
	 * @param stack
	 * @param name
	 * @return true if the item name contains the string
	 */
	protected boolean contains(ItemStack stack, String name) {
		return stack.hasItemMeta() && stack.getItemMeta().hasDisplayName()
				&& stack.getItemMeta().getDisplayName().contains(name);
	}

	/**
	 * Remove the item from the player's hand
	 * 
	 * @param player
	 * @param number
	 *            of items to withdraw
	 */
	protected void removeItemInHand(Player player) {
		removeItemInHand(player, 64);
	}

	/**
	 * Remove the item from the player's hand
	 * 
	 * @param player
	 * @param number
	 *            of items to withdraw
	 */
	protected void removeItemInHand(Player player, int how) {
		if (player.getItemInHand().getAmount() > how)
			player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
		else
			player.setItemInHand(new ItemStack(Material.AIR));
		player.updateInventory();
	}

	/**
	 * Check if two locations are identical
	 * 
	 * @param first
	 *            location
	 * @param second
	 *            location
	 * @return true if both rentals are the same
	 */
	protected boolean same(Location l, Location l2) {
		return (l.getBlockX() == l2.getBlockX()) && (l.getBlockY() == l2.getBlockY())
				&& (l.getBlockZ() == l2.getBlockZ()) && l.getWorld().getName().equals(l2.getWorld().getName());
	}

	/**
	 * Teleport a player to a given location with a given delay
	 * 
	 * @param player
	 *            who will be teleported
	 * @param delay
	 *            before the teleportation of the player
	 * @param location
	 *            where the player will be teleported
	 */
	protected void teleport(Player player, int delay, Location location) {
		teleport(player, delay, location, null);
	}

	/**
	 * Teleport a player to a given location with a given delay
	 * 
	 * @param player
	 *            who will be teleported
	 * @param delay
	 *            before the teleportation of the player
	 * @param location
	 *            where the player will be teleported
	 * @param code
	 *            executed when the player is teleported or not
	 */
	protected void teleport(Player player, int delay, Location location, Consumer<Boolean> cmd) {
		if (teleportPlayers.contains(player.getName())) {
			message(player, Message.TELEPORT_ERROR);
			return;
		}
		ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
		Location playerLocation = player.getLocation();
		AtomicInteger verif = new AtomicInteger(delay);
		teleportPlayers.add(player.getName());
		if (!location.getChunk().isLoaded())
			location.getChunk().load();
		ses.scheduleWithFixedDelay(() -> {
			if (!same(playerLocation, player.getLocation())) {
				message(player, Message.TELEPORT_MOVE);
				ses.shutdown();
				teleportPlayers.remove(player.getName());
				if (cmd != null)
					cmd.accept(false);
				return;
			}
			int currentSecond = verif.getAndDecrement();
			if (!player.isOnline()) {
				ses.shutdown();
				teleportPlayers.remove(player.getName());
				return;
			}
			if (currentSecond == 0) {
				ses.shutdown();
				teleportPlayers.remove(player.getName());
				player.teleport(location);
				message(player, Message.TELEPORT_SUCCESS);
				if (cmd != null)
					cmd.accept(true);
			} else
				message(player, Message.TELEPORT_MESSAGE, currentSecond);
		}, 0, 1, TimeUnit.SECONDS);
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

	private transient Economy economy = ZPlugin.z().getEconomy();

	/**
	 * Player bank
	 * 
	 * @param player
	 * @return player bank
	 */
	protected double getBalance(Player player) {
		return economy.getBalance(player);
	}

	/**
	 * Player has money
	 * 
	 * @param player
	 * @param int
	 *            value
	 * @return player has value in his bank
	 */
	protected boolean hasMoney(Player player, int value) {
		return hasMoney(player, (double) value);
	}

	/**
	 * Player has money
	 * 
	 * @param player
	 * @param float
	 *            value
	 * @return player has value in his bank
	 */
	protected boolean hasMoney(Player player, float value) {
		return hasMoney(player, (double) value);
	}

	/**
	 * Player has money
	 * 
	 * @param player
	 * @param long
	 *            value
	 * @return player has value in his bank
	 */
	protected boolean hasMoney(Player player, long value) {
		return hasMoney(player, (double) value);
	}

	/**
	 * Player has money
	 * 
	 * @param player
	 * @param double
	 *            value
	 * @return player has value in his bank
	 */
	protected boolean hasMoney(Player player, double value) {
		return getBalance(player) >= value;
	}

	/**
	 * Deposit player money
	 * 
	 * @param player
	 * @param double
	 *            value
	 */
	protected void depositMoney(Player player, double value) {
		economy.depositPlayer(player, value);
	}

	/**
	 * Deposit player money
	 * 
	 * @param player
	 * @param long
	 *            value
	 */
	protected void depositMoney(Player player, long value) {
		economy.depositPlayer(player, (double) value);
	}

	/**
	 * Deposit player money
	 * 
	 * @param player
	 * @param int
	 *            value
	 */
	protected void depositMoney(Player player, int value) {
		economy.depositPlayer(player, (double) value);
	}

	/**
	 * Deposit player money
	 * 
	 * @param player
	 * @param float
	 *            value
	 */
	protected void depositMoney(Player player, float value) {
		economy.depositPlayer(player, (double) value);
	}

	/**
	 * With draw player money
	 * 
	 * @param player
	 * @param double
	 *            value
	 */
	protected void withdrawMoney(Player player, double value) {
		economy.withdrawPlayer(player, value);
	}

	/**
	 * With draw player money
	 * 
	 * @param player
	 * @param long
	 *            value
	 */
	protected void withdrawMoney(Player player, long value) {
		economy.withdrawPlayer(player, (double) value);
	}

	/**
	 * With draw player money
	 * 
	 * @param player
	 * @param int
	 *            value
	 */
	protected void withdrawMoney(Player player, int value) {
		economy.withdrawPlayer(player, (double) value);
	}

	/**
	 * With draw player money
	 * 
	 * @param player
	 * @param float
	 *            value
	 */
	protected void withdrawMoney(Player player, float value) {
		economy.withdrawPlayer(player, (double) value);
	}

	/**
	 * 
	 * @return {@link Economy}
	 */
	protected Economy getEconomy() {
		return economy;
	}

	protected void removeItems(Player player, int item, ItemStack itemStack) {
		for (ItemStack is : player.getInventory().getContents()) {
			if (is != null && is.isSimilar(itemStack)) {
				int currentAmount = is.getAmount() - item;
				item -= is.getAmount();
				if (currentAmount <= 0)
					player.getInventory().removeItem(is);
				else
					is.setAmount(currentAmount);
			}
		}
		player.updateInventory();
	}

	protected void schedule(long delay, Runnable runnable) {
		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {
				if (runnable != null)
					runnable.run();
			}
		}, delay);
	}

	protected String name(String string) {
		return TextUtil.name(string);
	}

	protected int getMaxPage(Collection<?> items) {
		return (items.size() / 45) + 1;
	}

	protected int getMaxPage(Collection<?> items, int a) {
		return (items.size() / a) + 1;
	}

	protected double percent(double value, double total) {
		return (double) ((value * 100) / total);
	}

	protected double percentNum(double total, double percent) {
		return (double) (total * (percent / 100));
	}

	protected void schedule(long delay, int count, Runnable runnable) {
		new Timer().scheduleAtFixedRate(new TimerTask() {
			int tmpCount = 0;

			@Override
			public void run() {

				if (!ZPlugin.z().isEnabled()) {
					cancel();
					return;
				}

				if (tmpCount > count) {
					cancel();
					return;
				}

				tmpCount++;
				Bukkit.getScheduler().runTask(ZPlugin.z(), runnable);

			}
		}, 0, delay);
	}

	protected void message(CommandSender player, Message message) {
		player.sendMessage(Message.PREFIX.msg() + " " + message.msg());
	}

	protected void message(CommandSender player, String message) {
		player.sendMessage(Message.PREFIX.msg() + " " + message);
	}

	protected void messageWO(CommandSender player, Message message) {
		player.sendMessage(message.msg());
	}

	protected void messageWO(CommandSender player, Message message, Object... args) {
		player.sendMessage(String.format(message.msg(), args));
	}

	protected void message(CommandSender player, Message message, Object... args) {
		player.sendMessage(Message.PREFIX.msg() + " " + String.format(message.msg(), args));
	}

	protected void createInventory(Player player, int inventoryId) {
		createInventory(player, inventoryId, 1);
	}

	protected void createInventory(Player player, int inventoryId, int page) {
		createInventory(player, inventoryId, page, new Object() {
		});
	}

	protected void createInventory(Player player, int inventoryId, int page, Object... objects) {
		plugin.getInventoryManager().createInventory(inventoryId, player, page, objects);
	}

	protected boolean hasPermission(Permissible permissible, Permission permission) {
		return permissible.hasPermission(permission.getPermission());
	}

	protected void scheduleFix(long delay, BiConsumer<TimerTask, Boolean> runnable) {
		new Timer().scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (!ZPlugin.z().isEnabled()) {
					cancel();
					runnable.accept(this, false);
					return;
				}
				Bukkit.getScheduler().runTask(ZPlugin.z(), () -> runnable.accept(this, true));
			}
		}, delay, delay);
	}

	protected <T> T randomElement(List<T> element) {
		if (element.size() == 0)
			return null;
		if (element.size() == 1)
			return element.get(0);
		Random random = new Random();
		return element.get(random.nextInt(element.size() - 1));
	}

	public String getItemName(ItemStack item) {
		if (item.hasItemMeta() && item.getItemMeta().hasDisplayName())
			return item.getItemMeta().getDisplayName();
		try {
			if (item.hasItemMeta() && item.getItemMeta().hasLocalizedName())
				return item.getItemMeta().getLocalizedName();
		} catch (Exception e) { }
		String name = item.serialize().get("type").toString().replace("_", " ").toLowerCase();
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}

	public String color(String message) {
		return message.replace("&", "�");
	}

	protected String itemName(ItemStack item) {
		return item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName()
				: TextUtil.getMaterialLowerAndMajAndSpace(item.getType());
	}

	public List<String> color(List<String> messages) {
		return messages.stream().map(message -> color(message)).collect(Collectors.toList());
	}

	public ItemFlag getFlag(String flagString) {
		for (ItemFlag flag : ItemFlag.values()) {
			if (flag.name().equalsIgnoreCase(flagString))
				return flag;
		}
		return null;
	}
}
