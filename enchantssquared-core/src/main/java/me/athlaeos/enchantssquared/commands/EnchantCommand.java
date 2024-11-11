package me.athlaeos.enchantssquared.commands;

import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import me.athlaeos.enchantssquared.utility.ChatUtils;
import me.athlaeos.enchantssquared.utility.EntityUtils;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import me.athlaeos.valhallammo.utility.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class EnchantCommand implements Command {
	private final String enchant_success;
	private final String enchant_failed;
	private final String invalid_number;
	private final String enchant_warning;
	private final String enchant_description;

	public EnchantCommand(){
		enchant_success = ConfigManager.getInstance().getConfig("translations.yml").get().getString("enchant_successful");
		enchant_failed = ConfigManager.getInstance().getConfig("translations.yml").get().getString("enchant_failed");
		invalid_number = ConfigManager.getInstance().getConfig("translations.yml").get().getString("warning_invalid_number");
		enchant_warning = ConfigManager.getInstance().getConfig("translations.yml").get().getString("enchant_warning");
		enchant_description = ConfigManager.getInstance().getConfig("translations.yml").get().getString("enchant_description");
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (args.length <= 1) return false;
		if (!(sender instanceof Player) && args.length < 4) {
			sender.sendMessage(ChatUtils.chat("&cOnly players may do this"));
			return true;
		}
		Collection<Player> targets = new HashSet<>();
		if (args.length >= 4){
			targets.addAll(EntityUtils.selectPlayers(sender, args[3]));
		} else {
			targets.add((Player) sender);
		}
		String chosenEnchant = args[1].toUpperCase();
		CustomEnchant en = CustomEnchantManager.getInstance().getEnchantmentFromType(chosenEnchant);
		if (en == null) en = fromName(chosenEnchant);

		int chosenLevel = 1;
		if (args.length >= 3){
			try {
				chosenLevel = Integer.parseInt(args[2]);
				if (chosenLevel > 20){
					sender.sendMessage(ChatUtils.chat(enchant_warning));
				}
			} catch (IllegalArgumentException e){
				sender.sendMessage(ChatUtils.chat(invalid_number));
				return true;
			}
		}

		boolean success = false;
		for (Player p : targets){
			ItemStack inHandItem = p.getInventory().getItemInMainHand();
			if (!ItemUtils.isAirOrNull(inHandItem)) {
				if (inHandItem.getType() == Material.BOOK){
					inHandItem.setType(Material.ENCHANTED_BOOK);
				}
				CustomEnchantManager.getInstance().removeEnchant(inHandItem, en.getType());
				if (chosenLevel > 0) CustomEnchantManager.getInstance().addEnchant(inHandItem, en.getType(), chosenLevel);
				success = true;
			}
			p.updateInventory();
		}
		sender.sendMessage(ChatUtils.chat(success ? enchant_success : enchant_failed));
		return true;
	}

	@Override
	public String[] getRequiredPermission() {
		return new String[]{"es.enchant"};
	}

	@Override
	public String getFailureMessage() {
		return "&c/es enchant [enchantment] <level>";
	}

	@Override
	public String[] getHelpEntry() {
		return new String[]{
				ChatUtils.chat("&8&m                                             "),
				ChatUtils.chat("&d/es enchant [enchantment] <level>"),
				ChatUtils.chat("&7" + enchant_description),
				ChatUtils.chat("&7> &des.enchant")
		};
	}

	@Override
	public List<String> getSubcommandArgs(CommandSender sender, String[] args) {
		if (args.length == 2){
			List<String> returns = new ArrayList<>();
			for (String c : CustomEnchantManager.getInstance().getAllEnchants().values().stream().map(CustomEnchant::getType).toList()){
				returns.add(c.toLowerCase());
			}
			return returns;
		}
		if (args.length == 3){
			return Arrays.asList(
					"1",
					"2",
					"3",
					"...");
		}
		return null;
	}

	private CustomEnchant fromName(String name){
		return CustomEnchantManager.getInstance().getAllEnchants().values().stream().filter(e ->
				ChatColor.stripColor(Utils.chat(e.getDisplayEnchantment().replace("%lv_roman%", "").replace("%lv_normal%", ""))).trim().equalsIgnoreCase(name)
		).findFirst().orElse(null);
	}
}
