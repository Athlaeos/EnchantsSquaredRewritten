package me.athlaeos.enchantssquared.commands;

import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.domain.MinecraftVersion;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import me.athlaeos.enchantssquared.utility.ChatUtils;
import me.athlaeos.enchantssquared.utility.EntityUtils;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class GetEnchantedItemCommand implements Command {
	private final String give_item_successful;
	private final String error_invalid_item;
	private final String give_item_description;
	private final String error_player_not_found;
	private final String error_invalid_syntax;
	private final String reason_invalid_level;
	private final String reason_invalid_enchant;
	private final String warning_invalid_number;

	public GetEnchantedItemCommand(){
		give_item_successful = ConfigManager.getInstance().getConfig("translations.yml").get().getString("give_item_successful");
		give_item_description = ConfigManager.getInstance().getConfig("translations.yml").get().getString("give_item_description");
		error_invalid_item = ConfigManager.getInstance().getConfig("translations.yml").get().getString("error_invalid_item");
		error_invalid_syntax = ConfigManager.getInstance().getConfig("translations.yml").get().getString("error_invalid_syntax");
		reason_invalid_level = ConfigManager.getInstance().getConfig("translations.yml").get().getString("reason_invalid_level");
		error_player_not_found = ConfigManager.getInstance().getConfig("translations.yml").get().getString("error_player_not_found");
		reason_invalid_enchant = ConfigManager.getInstance().getConfig("translations.yml").get().getString("reason_invalid_enchant");
		warning_invalid_number = ConfigManager.getInstance().getConfig("translations.yml").get().getString("warning_invalid_number");
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (args.length <= 3) return false;
		Collection<Player> targets = EntityUtils.selectPlayers(sender, args[1]);
		if (targets.isEmpty()) return true;

		for (Player target : targets){
			Material itemType;
			try {
				itemType = Material.valueOf(args[2].toUpperCase());
			} catch (IllegalArgumentException ignored){
				sender.sendMessage(ChatUtils.chat(error_invalid_item));
				return true;
			}
			int amount;
			try {
				amount = Integer.parseInt(args[3]);
			} catch (IllegalArgumentException ignored){
				sender.sendMessage(ChatUtils.chat(warning_invalid_number));
				return true;
			}

			if (args.length == 4){
				Map<Integer, ItemStack> itemsLeft = target.getInventory().addItem(new ItemStack(itemType, amount));
				if (!itemsLeft.isEmpty()){
					for (ItemStack i : itemsLeft.values()){
						Item item = (Item) target.getWorld().spawnEntity(target.getLocation(), EntityType.valueOf(MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20_5) ? "ITEM" : "DROPPED_ITEM"));
						item.setItemStack(i);
					}
				}
				sender.sendMessage(ChatUtils.chat(give_item_successful));
				return true;
			}

			Map<Enchantment, Integer> vanillaEnchantments = new HashMap<>();
			Map<CustomEnchant, Integer> customEnchantments = new HashMap<>();
			List<String> lore = new ArrayList<>();
			String displayName = null;

			String[] customDataArgs = Arrays.copyOfRange(args, 4, args.length);
			for (String arg : customDataArgs){
				if (arg.contains("custom=")){
					String finalArg = arg.replace("custom=", "");
					String[] enchantArgs = finalArg.split(",");
					for (String enchantment : enchantArgs){
						String[] enchantDetails = enchantment.split(":");
						if (enchantDetails.length != 2){
							continue;
						}
						CustomEnchant enchant;
						try {
							enchant = CustomEnchantManager.getInstance().getEnchantmentFromType(enchantDetails[0].toUpperCase());
						} catch (IllegalArgumentException ignored){
							sender.sendMessage(ChatUtils.chat(error_invalid_syntax.replace("%reason%", reason_invalid_enchant)));
							return true;
						}
						int level;
						try {
							level = Integer.parseInt(enchantDetails[1]);
						} catch (IllegalArgumentException ignored){
							sender.sendMessage(ChatUtils.chat(error_invalid_syntax.replace("%reason%", reason_invalid_level)));
							return true;
						}
						customEnchantments.put(enchant, level);
					}
				} else if (arg.contains("name=")){
					String finalArg = arg.replace("name=", "");
					displayName = ChatUtils.chat(finalArg.replace("_", " "));
				} else if (arg.contains("lore=")){
					String finalArg = arg.replace("lore=", "");
					String[] loreArgs = finalArg.split("/n");
					for (String line : loreArgs){
						lore.add(ChatUtils.chat(line.replace("_", " ")));
					}
				} else if (arg.contains("enchants=")){
					String finalArg = arg.replace("enchants=", "");
					String[] enchantArgs = finalArg.split(",");
					for (String enchantment : enchantArgs){
						String[] enchantDetails = enchantment.split(":");
						if (enchantDetails.length != 2) continue;
						Enchantment enchant;
						try {
							enchant = MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20_5) ? Registry.ENCHANTMENT.get(NamespacedKey.minecraft(enchantDetails[0])) : Enchantment.getByKey(NamespacedKey.minecraft(enchantDetails[0]));
							if (enchant == null) throw new IllegalArgumentException();
						} catch (IllegalArgumentException ignored){
							sender.sendMessage(ChatUtils.chat(error_invalid_syntax.replace("%reason%", reason_invalid_enchant)));
							return true;
						}
						int level;
						try {
							level = Integer.parseInt(enchantDetails[1]);
						} catch (IllegalArgumentException ignored){
							sender.sendMessage(ChatUtils.chat(error_invalid_syntax.replace("%reason%", reason_invalid_level)));
							return true;
						}
						vanillaEnchantments.put(enchant, level);
					}
				}
			}

			ItemStack giveItem = new ItemStack(itemType, amount);
			ItemMeta itemMeta = giveItem.getItemMeta();
			assert itemMeta != null;
			if (displayName != null){
				itemMeta.setDisplayName(ChatUtils.chat(displayName));
			}

			if (!lore.isEmpty()){
				itemMeta.setLore(lore);
			}

			if (itemMeta instanceof EnchantmentStorageMeta){
				EnchantmentStorageMeta eMeta = (EnchantmentStorageMeta) itemMeta;

				for (Enchantment e : vanillaEnchantments.keySet()){
					eMeta.addStoredEnchant(e, vanillaEnchantments.get(e), true);
				}
				giveItem.setItemMeta(eMeta);
			} else {
				giveItem.setItemMeta(itemMeta);
				for (Enchantment e : vanillaEnchantments.keySet()){
					giveItem.addUnsafeEnchantment(e, vanillaEnchantments.get(e));
				}
			}

			if (!customEnchantments.isEmpty()){
				CustomEnchantManager.getInstance().setItemEnchants(giveItem, customEnchantments);
			}

			ItemUtils.addItem(target, giveItem, true);
		}

		sender.sendMessage(ChatUtils.chat(give_item_successful));

		return true;
	}

	@Override
	public String[] getRequiredPermission() {
		return new String[]{"es.enchant"};
	}

	@Override
	public String getFailureMessage() {
		return "&c/es give [player] [item] [amount] <custom=enchant:level,enchant:level...> <name=&8Example_name> <lore=&7Loreline_1/n&7Loreline_2> <enchants=enchant:level,enchant:level...>";
	}

	@Override
	public String[] getHelpEntry() {
		return new String[]{
				ChatUtils.chat("&8&m                                             "),
				ChatUtils.chat("&d/es give [player] [item] [amount] <custom=enchant:level,enchant:level...> <name=&8Example_name> <lore=&7Loreline_1/n&7Loreline_2> <enchants=enchant:level,enchant:level...>"),
				ChatUtils.chat("&7" + give_item_description),
				ChatUtils.chat("&7> &des.enchant")
		};
	}

	@SuppressWarnings("deprecation")
	@Override
	public List<String> getSubcommandArgs(CommandSender sender, String[] args) {
		if (args.length == 2){
			return null;
		}
		if (args.length == 3){
			return Arrays.stream(Material.values()).map(Material::toString).map(String::toLowerCase).collect(Collectors.toList());
		}
		if (args.length == 4) {
			return Arrays.asList(
					"1",
					"2",
					"3",
					"4",
					"5",
					"...");
		}
		if (args.length >= 5) {
			String currentArg = args[args.length - 1];
			if (currentArg.contains("custom=")) {
				currentArg = currentArg.replace("custom=", "");
				if (!currentArg.isEmpty()) {
					if (currentArg.contains(":")) {
						String[] currentEnchantArgs = currentArg.split(":");
						if (currentEnchantArgs.length % 2 == 1) {
							return Arrays.asList(
									"custom=" + currentEnchantArgs[0] + "1,",
									"custom=" + currentEnchantArgs[0] + "2,",
									"custom=" + currentEnchantArgs[0] + "3,",
									"custom=" + currentEnchantArgs[0] + "4,",
									"custom=" + currentEnchantArgs[0] + "5,",
									"...");
						}
					}
				}
				List<String> returns = new ArrayList<>();
				for (String c : CustomEnchantManager.getInstance().getAllEnchants().values().stream().map(CustomEnchant::getType).collect(Collectors
						.toList())) {
					returns.add("custom=" + c.toLowerCase() + ":");
				}
				return returns;
			}
			if (currentArg.contains("enchants=")) {
				currentArg = currentArg.replace("enchants=", "");
				if (currentArg.length() > 0) {
					if (currentArg.contains(":")) {
						String[] currentEnchantArgs = currentArg.split(":");
						if (currentEnchantArgs.length % 2 == 1) {
							return Arrays.asList(
									"enchants=" + currentEnchantArgs[0] + "1,",
									"enchants=" + currentEnchantArgs[0] + "2,",
									"enchants=" + currentEnchantArgs[0] + "3,",
									"enchants=" + currentEnchantArgs[0] + "4,",
									"enchants=" + currentEnchantArgs[0] + "5,",
									"...");
						}
					}
				}
				List<String> returns = new ArrayList<>();
				for (Enchantment c : (MinecraftVersion.currentVersionNewerThan(MinecraftVersion.MINECRAFT_1_20_5) ? Registry.ENCHANTMENT.stream().collect(Collectors.toSet()) : Arrays.asList(Enchantment.values()))) {
					returns.add("enchants=" + c.getKey().getKey() + ":");
				}
				return returns;
			} else if (currentArg.contains("name=") || currentArg.contains("lore=")) {
				return Collections.singletonList(currentArg);
			} else {
				return Arrays.asList("custom=", "enchants=", "name=", "lore=");
			}
		} else {
			return Arrays.asList("custom=", "enchants=", "name=", "lore=");
		}
	}
}
