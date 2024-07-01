package me.athlaeos.enchantssquared.commands;

import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.menus.EnchantmentOverviewMenu;
import me.athlaeos.enchantssquared.menus.PlayerMenuUtilManager;
import me.athlaeos.enchantssquared.utility.ChatUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class GetEnchantMenuCommand implements Command {
	private final String menu_description;

	public GetEnchantMenuCommand(){
		menu_description = ConfigManager.getInstance().getConfig("translations.yml").get().getString("menu_description");
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		new EnchantmentOverviewMenu(PlayerMenuUtilManager.getInstance().getPlayerMenuUtility((Player) sender)).open();

		return true;
	}

	@Override
	public String[] getRequiredPermission() {
		return new String[]{"es.menu"};
	}

	@Override
	public String getFailureMessage() {
		return "&4/es menu";
	}

	@Override
	public String[] getHelpEntry() {
		return new String[]{
				ChatUtils.chat("&8&m                                             "),
				ChatUtils.chat("&d/es menu"),
				ChatUtils.chat("&7" + menu_description),
				ChatUtils.chat("&7> &des.menu")
		};
	}

	@Override
	public List<String> getSubcommandArgs(CommandSender sender, String[] args) {
		return null;
	}
}
