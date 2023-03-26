package me.athlaeos.enchantssquared.commands;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.utility.ChatUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandManager implements TabExecutor {
	private final Map<String, Command> commands = new HashMap<>();
	private final String invalid_command;
	private final String warning_no_permission;
	private static CommandManager manager = null;

	public CommandManager() {
		invalid_command = ConfigManager.getInstance().getConfig("translations.yml").get().getString("warning_invalid_command");
		warning_no_permission = ConfigManager.getInstance().getConfig("translations.yml").get().getString("warning_no_permission");

		commands.put("help", new HelpCommand());
		commands.put("reload", new ReloadCommand());
		commands.put("enchant", new EnchantCommand());
		commands.put("remove", new RemoveEnchantCommand());
		commands.put("list", new GetEnchantListCommand());
		commands.put("menu", new GetEnchantMenuCommand());
		commands.put("give", new GetEnchantedItemCommand());

	    ((HelpCommand) commands.get("help")).giveCommandMap(commands);

		PluginCommand command = EnchantsSquared.getPlugin().getCommand("enchantssquared");
		if (command != null){
			command.setExecutor(this);
		}
	}

	public static CommandManager getInstance(){
		if (manager == null) manager = new CommandManager();

		return manager;
	}

	public void reload(){
		manager = new CommandManager();
	}

	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String name, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(ChatUtils.chat(String.format("&dEnchants Squared v%s by Athlaeos", EnchantsSquared.getPlugin().getDescription().getVersion())));
			sender.sendMessage(ChatUtils.chat("&7/es help"));
			return true;
		}
		
		for (String subCommand : commands.keySet()) {
			if (args[0].equalsIgnoreCase(subCommand)) {
				boolean hasPermission = false;
				for (String permission : commands.get(subCommand).getRequiredPermission()){
					if (sender.hasPermission(permission)){
						hasPermission = true;
						break;
					}
				}
				if (!hasPermission){
					sender.sendMessage(ChatUtils.chat(warning_no_permission));
					return true;
				}
				if (!commands.get(subCommand).execute(sender, args)) {
					sender.sendMessage(ChatUtils.chat(commands.get(subCommand).getFailureMessage()));
				}
				return true;
			}
		}
		sender.sendMessage(ChatUtils.chat(invalid_command));
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command cmd, String name, String[] args) {
		if (args.length == 1) {
			return new ArrayList<>(commands.keySet());
		} else if (args.length > 1) {
			for (String arg : commands.keySet()) {
				if (args[0].equalsIgnoreCase(arg)) {
					return commands.get(arg).getSubcommandArgs(sender, args);
				}
			}
		}
		return null;
	}
}
