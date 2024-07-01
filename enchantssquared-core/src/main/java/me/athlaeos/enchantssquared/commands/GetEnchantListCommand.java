package me.athlaeos.enchantssquared.commands;

import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import me.athlaeos.enchantssquared.utility.ChatUtils;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.stream.Collectors;

public class GetEnchantListCommand implements Command {
	private final String list_description;
	private final boolean eslist_include_weight;
	private final boolean eslist_include_max_level;
	private final boolean eslist_include_compatible_items;
	private final String weight_translation;
	private final String max_level_translation;
	private final String compatible_item_translation;
	private int extraAdditionalLines = 0;

	public GetEnchantListCommand(){
		list_description = ConfigManager.getInstance().getConfig("translations.yml").get().getString("list_description");
		weight_translation = ConfigManager.getInstance().getConfig("translations.yml").get().getString("eslist_weight");
		max_level_translation = ConfigManager.getInstance().getConfig("translations.yml").get().getString("max_level_translation");
		compatible_item_translation = ConfigManager.getInstance().getConfig("translations.yml").get().getString("compatible_item_translation");
		eslist_include_weight = ConfigManager.getInstance().getConfig("config.yml").get().getBoolean("eslist_include_weight");
		eslist_include_compatible_items = ConfigManager.getInstance().getConfig("config.yml").get().getBoolean("eslist_include_compatible_items");
		eslist_include_max_level = ConfigManager.getInstance().getConfig("config.yml").get().getBoolean("eslist_include_max_level");
		if (eslist_include_max_level) extraAdditionalLines++;
		if (eslist_include_weight) extraAdditionalLines++;
		if (eslist_include_compatible_items) extraAdditionalLines++;
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Map<Integer, ArrayList<String>> enchantPagesMap;
		List<String> helpLines = new ArrayList<>();

		for (CustomEnchant c : CustomEnchantManager.getInstance().getAllEnchants().values()) {
			if (c.isEnabled()){
				helpLines.add(ChatUtils.chat(c.getDisplayEnchantment()));
				helpLines.add(ChatUtils.chat(c.getDescription()));
				if (eslist_include_weight){
					helpLines.add(ChatUtils.chat(weight_translation + c.getWeight()));
				}
				if (eslist_include_max_level){
					helpLines.add(ChatUtils.chat(max_level_translation
							.replace("%lv_roman%", ChatUtils.toRoman(c.getMaxLevel())
									.replace("%lv_number%", "" + c.getMaxTableLevel()))));
				}
				if (eslist_include_compatible_items){
					helpLines.add(ChatUtils.chat(compatible_item_translation +
							Arrays.stream(MaterialClassType.values()).filter(
									m -> m.getMatches().stream().findFirst().filter(e -> c.isNaturallyCompatible(e) && c.isFunctionallyCompatible(e)).isPresent()
									).map(MaterialClassType::toString).collect(Collectors.joining(", "))
									.toLowerCase()));
				}
				helpLines.add(ChatUtils.chat("&8&m                                             "));
			}
		}

		enchantPagesMap = ChatUtils.paginateTextList(9 + (extraAdditionalLines * 3), helpLines);

		if (enchantPagesMap.size() == 0) {
			return true;
		}
		
		// args[0] is "help" and args.length > 0
		if (args.length == 1) {
			for (String line : enchantPagesMap.get(0)) {
				sender.sendMessage(ChatUtils.chat(line));
			}
			ChatUtils.chat("&8&m                                             ");
			sender.sendMessage(ChatUtils.chat(String.format("&8[&e1&8/&e%s&8]", enchantPagesMap.size())));
			return true;
		}

		if (args.length >= 2) {
			int pageNumber;
			try {
				pageNumber = Integer.parseInt(args[1]);

				if (pageNumber < 1) {
					pageNumber = 1;
				}
				if (pageNumber > enchantPagesMap.size()) {
					pageNumber = enchantPagesMap.size();
				}
			} catch (NumberFormatException nfe) {
				try {
					String type = args[1].toUpperCase();
					if (CustomEnchantManager.getInstance().getAllEnchants().values().stream().map(CustomEnchant::getType).collect(Collectors
							.toList()).contains(type)) {
						CustomEnchant enchant = CustomEnchantManager.getInstance().getEnchantmentFromType(type);
						sender.sendMessage(ChatUtils.chat("&8&m                                             "));
						sender.sendMessage(ChatUtils.chat(enchant.getDisplayEnchantment()));
						sender.sendMessage(ChatUtils.chat(enchant.getDescription()));
						if (eslist_include_weight){
							sender.sendMessage(ChatUtils.chat(weight_translation + enchant.getWeight()));
						}
						if (eslist_include_max_level){
							sender.sendMessage(ChatUtils.chat(max_level_translation
									.replace("%lv_roman%", ChatUtils.toRoman(enchant.getMaxLevel())
											.replace("%lv_number%", "" + enchant.getMaxLevel()))));
						}
						if (eslist_include_compatible_items){
							sender.sendMessage(ChatUtils.chat(compatible_item_translation +
									Arrays.stream(MaterialClassType.values()).filter(
													m -> m.getMatches().stream().findFirst().filter(e -> enchant.isNaturallyCompatible(e) && enchant.isFunctionallyCompatible(e)).isPresent()
											).map(MaterialClassType::toString).collect(Collectors.joining(", "))
											.toLowerCase()));
						}
						sender.sendMessage(ChatUtils.chat("&8&m                                             "));
						return true;
					} else {
						throw new IllegalArgumentException();
					}
				} catch (IllegalArgumentException ignored){
					helpLines.clear();
					pageNumber = 1;
					for (CustomEnchant e : CustomEnchantManager.getInstance().getAllEnchants().values()){
						if (e.isEnabled()){
							if (e.getDisplayEnchantment().toLowerCase().contains(args[1].toLowerCase())){
								helpLines.add(ChatUtils.chat(e.getDisplayEnchantment()));
								helpLines.add(ChatUtils.chat(e.getDescription()));
								if (eslist_include_weight){
									helpLines.add(ChatUtils.chat(weight_translation + e.getWeight()));
								}
								if (eslist_include_max_level){
									helpLines.add(ChatUtils.chat(max_level_translation
											.replace("%lv_roman%", ChatUtils.toRoman(e.getMaxLevel())
													.replace("%lv_number%", "" + e.getMaxLevel()))));
								}
								if (eslist_include_compatible_items){
									helpLines.add(ChatUtils.chat(compatible_item_translation +
											Arrays.stream(MaterialClassType.values()).filter(
															m -> m.getMatches().stream().findFirst().filter(c -> e.isNaturallyCompatible(c) && e.isFunctionallyCompatible(c)).isPresent()
													).map(MaterialClassType::toString).collect(Collectors.joining(", "))
													.toLowerCase()));
								}
								helpLines.add(ChatUtils.chat("&8&m                                             "));
							}
						}
					}
					if (args.length == 3){
						try {
							pageNumber = Integer.parseInt(args[2]);

							if (pageNumber < 1) {
								pageNumber = 1;
							}
							if (pageNumber > enchantPagesMap.size()) {
								pageNumber = enchantPagesMap.size();
							}
						} catch (NumberFormatException ignored1) {
						}
					}
					enchantPagesMap = ChatUtils.paginateTextList(9 + (extraAdditionalLines * 3), helpLines);

					if (enchantPagesMap.size() == 0) {
						return true;
					}
					for (String entry : enchantPagesMap.get(pageNumber - 1)) {
						sender.sendMessage(ChatUtils.chat(entry));
					}
					sender.sendMessage(ChatUtils.chat(String.format("&8[&e%s&8/&e%s&8]", pageNumber, enchantPagesMap.size())));
					return true;
				}
			}
			
			for (String entry : enchantPagesMap.get(pageNumber - 1)) {
				sender.sendMessage(ChatUtils.chat(entry));
			}
			sender.sendMessage(ChatUtils.chat(String.format("&8[&e%s&8/&e%s&8]", pageNumber, enchantPagesMap.size())));
			return true;
		}

		return false;
	}

	@Override
	public String[] getRequiredPermission() {
		return new String[]{"es.list"};
	}

	@Override
	public String getFailureMessage() {
		return "&4/es list";
	}

	@Override
	public String[] getHelpEntry() {
		return new String[]{
				ChatUtils.chat("&8&m                                             "),
				ChatUtils.chat("&d/es list <page/enchant/search term>"),
				ChatUtils.chat("&7" + list_description),
				ChatUtils.chat("&7> &des.list")
		};
	}

	@Override
	public List<String> getSubcommandArgs(CommandSender sender, String[] args) {
		if (args.length == 2) {
			List<String> returns = new ArrayList<>();
			for (String c : CustomEnchantManager.getInstance().getAllEnchants().values().stream().map(CustomEnchant::getType).collect(Collectors
					.toList())){
				returns.add(c.toLowerCase());
			}
			return returns;
		}
		List<String> subargs = new ArrayList<>();
		subargs.add(" ");
		return subargs;
	}
}
