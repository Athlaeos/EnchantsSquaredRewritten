package me.athlaeos.enchantssquared.commands;

import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import me.athlaeos.enchantssquared.utility.ChatUtils;
import me.athlaeos.enchantssquared.utility.EntityUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RemoveEnchantCommand implements Command {
    private final String remove_enchant_description;
    private final String remove_enchant_successful;
    private final String remove_enchant_failed;

    public RemoveEnchantCommand(){
        remove_enchant_description = ConfigManager.getInstance().getConfig("translations.yml").get().getString("remove_enchant_description");
        remove_enchant_successful = ConfigManager.getInstance().getConfig("translations.yml").get().getString("remove_enchant_successful");
        remove_enchant_failed = ConfigManager.getInstance().getConfig("translations.yml").get().getString("remove_enchant_failed");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length <= 1) return false;
        if (!(sender instanceof Player) && args.length < 3) {
            sender.sendMessage(ChatUtils.chat("&cOnly players may do this"));
            return true;
        }
        Collection<Player> targets = new HashSet<>();
        if (args.length >= 3){
            targets.addAll(EntityUtils.selectPlayers(sender, args[2]));
        } else {
            targets.add((Player) sender);
        }
        boolean success = false;
        for (Player p : targets){
            ItemStack inHandItem = p.getInventory().getItemInMainHand();
            Map<CustomEnchant, Integer> enchantments = CustomEnchantManager.getInstance().getItemsEnchantments(inHandItem);
            if (enchantments.keySet().stream().anyMatch(en -> en.getType().equalsIgnoreCase(args[1]))) {
                if (CustomEnchantManager.getInstance().removeEnchant(inHandItem, args[1])) success = true;
            }
        }
        sender.sendMessage(ChatUtils.chat(success ? remove_enchant_successful : remove_enchant_failed));
        return true;
    }

    @Override
    public String[] getRequiredPermission() {
        return new String[]{"es.removeenchant"};
    }

    @Override
    public String getFailureMessage() {
        return "&c/es remove <enchantmen>";
    }

    @Override
    public String[] getHelpEntry() {
        return new String[]{
                ChatUtils.chat("&8&m                                             "),
                ChatUtils.chat("&d/es remove <enchantment>"),
                ChatUtils.chat("&7" + remove_enchant_description),
                ChatUtils.chat("&7> &des.removeenchant")
        };
    }

    @Override
    public List<String> getSubcommandArgs(CommandSender sender, String[] args) {
        if (args.length == 2 && sender instanceof Player){
            ItemStack inHandItem = ((Player) sender).getInventory().getItemInMainHand();
            return CustomEnchantManager.getInstance().getItemsEnchantments(inHandItem).keySet().stream().map(CustomEnchant::getType).collect(Collectors.toList());
        }
        return null;
    }
}
