package me.athlaeos.enchantssquared.commands;

import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import me.athlaeos.enchantssquared.utility.ChatUtils;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ReloadCommand implements Command {
	private final String reload_successful;
	private final String reload_description;

	public ReloadCommand(){
		reload_successful = ConfigManager.getInstance().getConfig("translations.yml").get().getString("reload_successful");
		reload_description = ConfigManager.getInstance().getConfig("translations.yml").get().getString("reload_description");
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		for (String config : ConfigManager.getInstance().getConfigs().keySet()){
			ConfigManager.getInstance().getConfigs().get(config).reload();
		}

		CommandManager.getInstance().reload();
		CustomEnchantManager.getInstance().reload();

		sender.sendMessage(ChatUtils.chat(reload_successful));
		return true;
	}

	@Override
	public String[] getRequiredPermission() {
		return new String[]{"es.reload"};
	}

	@Override
	public String getFailureMessage() {
		return "&4/es reload";
	}

	@Override
	public String[] getHelpEntry() {
		return new String[]{
				ChatUtils.chat("&8&m                                             "),
				ChatUtils.chat("&d/es reload"),
				ChatUtils.chat("&7" + reload_description),
				ChatUtils.chat("&7> &des.reload")
		};
	}

	@Override
	public List<String> getSubcommandArgs(CommandSender sender, String[] args) {
		return null;
	}
}
