package fr.maxlego08.shop.command;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;

import fr.maxlego08.shop.ZShop;
import fr.maxlego08.shop.command.commands.zshop.CommandZShopPlugin;
import fr.maxlego08.shop.save.Lang;
import fr.maxlego08.shop.zcore.enums.Message;
import fr.maxlego08.shop.zcore.logger.Logger;
import fr.maxlego08.shop.zcore.logger.Logger.LogType;
import fr.maxlego08.shop.zcore.utils.ZUtils;
import fr.maxlego08.shop.zcore.utils.commands.CommandType;
import net.minelink.ctplus.CombatTagPlus;

public class CommandManager extends ZUtils implements CommandExecutor, TabCompleter, Listener {

	private final ZShop plugin;
	private final List<VCommand> commands = new ArrayList<VCommand>();

	public CommandManager(ZShop template) {
		this.plugin = template;
	}

	public void registerCommands() {

		this.registerCommand("zshoplugin", new CommandZShopPlugin(this), Arrays.asList("zpl"), false);

		plugin.getLog().log("Loading " + getUniqueCommand() + " commands", LogType.SUCCESS);
		this.commandChecking();
	}

	public VCommand addCommand(VCommand command) {
		commands.add(command);
		return command;
	}

	/**
	 * @param string
	 * @param command
	 * @return
	 */
	public VCommand addCommand(String string, VCommand command) {
		commands.add(command.addSubCommand(string));
		plugin.getCommand(string).setExecutor(this);
		plugin.getCommand(string).setTabCompleter(this);
		return command;
	}

	public void clear() {

		this.commands.clear();
		this.registerCommand("zshoplugin", new CommandZShopPlugin(this), Arrays.asList("zpl"), true);

	}

	/**
	 * Register command whitout plugin.yml
	 * 
	 * @param string
	 * @param vCommand
	 * @param aliases
	 */
	public void registerCommand(String string, VCommand vCommand, List<String> aliases, boolean customRegister) {
		try {
			Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			bukkitCommandMap.setAccessible(true);

			CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());

			Class<? extends PluginCommand> class1 = PluginCommand.class;
			Constructor<? extends PluginCommand> constructor = class1.getDeclaredConstructor(String.class,
					Plugin.class);
			constructor.setAccessible(true);

			PluginCommand command = constructor.newInstance(string, plugin);
			command.setExecutor(this);
			command.setTabCompleter(this);
			command.setAliases(aliases);

			commands.add(vCommand.addSubCommand(string));
			for (String cmd : aliases)
				vCommand.addSubCommand(cmd);

			if (customRegister)
				commandMap.register(command.getName(), plugin.getDescription().getName(), command);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String useless, String[] args) {
		for (VCommand command : commands) {
			if (command.getSubCommands().contains(cmd.getName().toLowerCase())) {
				if ((args.length == 0 || command.isIgnoreParent()) && command.getParent() == null) {
					CommandType type = processRequirements(command, sender, args);
					if (!type.equals(CommandType.CONTINUE))
						return true;
				}
			} else if (args.length >= 1 && command.getParent() != null
					&& canExecute(args, cmd.getName().toLowerCase(), command)) {
				CommandType type = processRequirements(command, sender, args);
				if (!type.equals(CommandType.CONTINUE))
					return true;
			}
		}
		message(sender, Lang.commandError);
		return true;
	}

	/**
	 * @param args
	 * @param cmd
	 * @param command
	 * @return true if can execute
	 */
	private boolean canExecute(String[] args, String cmd, VCommand command) {
		for (int index = args.length - 1; index > -1; index--) {
			if (command.getSubCommands().contains(args[index].toLowerCase())) {
				if (command.isIgnoreArgs()
						&& (command.getParent() != null ? canExecute(args, cmd, command.getParent(), index - 1) : true))
					return true;
				if (index < args.length - 1)
					return false;
				return canExecute(args, cmd, command.getParent(), index - 1);
			}
		}
		return false;
	}

	/**
	 * @param args
	 * @param cmd
	 * @param command
	 * @param index
	 * @return
	 */
	private boolean canExecute(String[] args, String cmd, VCommand command, int index) {
		if (index < 0 && command.getSubCommands().contains(cmd.toLowerCase()))
			return true;
		else if (index < 0)
			return false;
		else if (command.getSubCommands().contains(args[index].toLowerCase()))
			return canExecute(args, cmd, command.getParent(), index - 1);
		else
			return false;
	}

	/**
	 * @param command
	 * @param sender
	 * @param strings
	 * @return
	 */
	private CommandType processRequirements(VCommand command, CommandSender sender, String[] strings) {

		if (!(sender instanceof Player) && !command.isConsoleCanUse()) {
			message(sender, Message.COMMAND_NO_CONSOLE);
			return CommandType.DEFAULT;
		}
		if (command.getPermission() == null || hasPermission(sender, command.getPermission())) {

			if (command.runAsync) {
				Bukkit.getScheduler().runTask(plugin, () -> {
					CommandType returnType = command.prePerform(plugin, sender, strings);
					if (returnType == CommandType.SYNTAX_ERROR)
						message(sender, Lang.syntaxeError, command.getSyntaxe());
				});
				return CommandType.DEFAULT;
			}

			CommandType returnType = command.prePerform(plugin, sender, strings);
			if (returnType == CommandType.SYNTAX_ERROR)
				message(sender, Lang.syntaxeError.replace("%command%", command.getSyntaxe()));
			return returnType;
		}
		message(sender, Lang.noPermission);
		return CommandType.DEFAULT;
	}

	public List<VCommand> getCommands() {
		return commands;
	}

	private int getUniqueCommand() {
		return (int) commands.stream().filter(command -> command.getParent() == null).count();
	}

	/**
	 * @param commandString
	 * @param sender
	 */
	public void sendHelp(String commandString, CommandSender sender) {
		commands.forEach(command -> {
			if (isValid(command, commandString)
					&& (command.getPermission() == null || hasPermission(sender, command.getPermission()))) {
				message(sender, Message.COMMAND_SYNTAXE_HELP, command.getSyntaxe(), command.getDescription());
			}
		});
	}

	/**
	 * @param command
	 * @param commandString
	 * @return
	 */
	public boolean isValid(VCommand command, String commandString) {
		return command.getParent() != null ? isValid(command.getParent(), commandString)
				: command.getSubCommands().contains(commandString.toLowerCase());
	}

	/**
	 * Check if your order is ready for use
	 */
	private void commandChecking() {
		commands.forEach(command -> {
			if (command.sameSubCommands()) {
				Logger.info(command.toString() + " command to an argument similar to its parent command !",
						LogType.ERROR);
				plugin.getPluginLoader().disablePlugin(plugin);
			}
		});
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String str, String[] args) {

		for (VCommand command : commands) {

			if (command.getSubCommands().contains(cmd.getName().toLowerCase())) {
				if (args.length == 1 && command.getParent() == null) {
					return proccessTab(sender, command, args);
				}
			} else {
				String[] newArgs = Arrays.copyOf(args, args.length - 1);
				if (newArgs.length >= 1 && command.getParent() != null
						&& canExecute(newArgs, cmd.getName().toLowerCase(), command)) {
					return proccessTab(sender, command, args);
				}
			}
		}

		return null;
	}

	/**
	 * 
	 * @param sender
	 * @param command
	 * @param args
	 * @return
	 */
	private List<String> proccessTab(CommandSender sender, VCommand command, String[] args) {

		CommandType type = command.getTabCompleter();
		if (type.equals(CommandType.DEFAULT)) {

			String startWith = args[args.length - 1];

			List<String> tabCompleter = new ArrayList<>();
			for (VCommand vCommand : commands) {
				if ((vCommand.getParent() != null && vCommand.getParent() == command)) {
					String cmd = vCommand.getSubCommands().get(0);
					if (vCommand.getPermission() == null || sender.hasPermission(vCommand.getPermission()))
						if (startWith.length() == 0 || cmd.startsWith(startWith))
							tabCompleter.add(cmd);
				}
			}
			return tabCompleter.size() == 0 ? null : tabCompleter;

		} else if (type.equals(CommandType.SUCCESS))
			return command.toTab(plugin, sender, args);

		return null;
	}

	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event) {

		if (event.isCancelled())
			return;

		String message = event.getMessage();
		message = message.replace("/", "");
		String[] messages = message.split(" ");
		String commands = messages[0];

		if (!this.commands.stream().filter(cmd -> {
			return cmd.getParent() == null
					&& cmd.getSubCommands().stream().filter(e -> e.equalsIgnoreCase(commands)).findAny().isPresent();
		}).findAny().isPresent())
			return;

		Command command = new Command(commands) {
			@Override
			public boolean execute(CommandSender arg0, String arg1, String[] arg2) {
				return true;
			}
		};
		String[] args = getArgs(messages);
		if (this.onCustomCommands(event.getPlayer(), command, commands, args))
			event.setCancelled(true);
	}

	private String[] getArgs(String[] oldArgs) {
		if (oldArgs.length < 2)
			return new String[] {};
		String[] args = new String[oldArgs.length - 1];
		for (int a = 1; a != oldArgs.length; a++)
			args[a - 1] = oldArgs[a];
		return args;
	}

	/**
	 * 
	 * @param sender
	 * @param cmd
	 * @param useless
	 * @param args
	 * @return
	 */
	private boolean onCustomCommands(CommandSender sender, Command cmd, String useless, String[] args) {

		Plugin plugin = Bukkit.getPluginManager().getPlugin("CombatTagPlus");
		if (plugin != null && plugin.isEnabled() && sender instanceof Player) {

			CombatTagPlus combatTagPlus = (CombatTagPlus) plugin;
			if (combatTagPlus.getTagManager().isTagged(((Player) sender).getUniqueId()))
				return false;

		}

		for (VCommand command : commands) {
			if (command.getSubCommands().contains(cmd.getName().toLowerCase())) {
				if ((args.length == 0 || command.isIgnoreParent()) && command.getParent() == null) {
					CommandType type = processRequirements(command, sender, args);
					if (!type.equals(CommandType.CONTINUE))
						return true;
				}
			} else if (args.length >= 1 && command.getParent() != null
					&& canExecute(args, cmd.getName().toLowerCase(), command)) {
				CommandType type = processRequirements(command, sender, args);
				if (!type.equals(CommandType.CONTINUE))
					return true;
			}
		}
		return false;
	}

}