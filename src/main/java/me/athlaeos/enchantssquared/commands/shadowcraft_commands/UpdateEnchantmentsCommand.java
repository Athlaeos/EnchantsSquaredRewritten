package me.athlaeos.enchantssquared.commands.shadowcraft_commands;

import me.athlaeos.enchantssquared.commands.Command;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import me.athlaeos.enchantssquared.utility.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class UpdateEnchantmentsCommand implements Command {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }
        Player player = (Player) sender;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You have to be holding an item to use this command");
            return true;
        }

        CustomEnchantManager.getInstance().updateItem(item);

        player.sendMessage(ChatColor.GREEN + "Item successfully updated!");
        return true;
    }

    @Override
    public String[] getRequiredPermission() {
        return new String[]{"es.update"};
    }

    @Override
    public String getFailureMessage() {
        return "/es update";
    }

    @Override
    public String[] getHelpEntry() {
        return new String[]{
                ChatUtils.chat("&8&m                                             "),
                ChatUtils.chat("&d/es update"),
                ChatUtils.chat("&7Updates the Player's held item from the legacy enchantment system"),
                ChatUtils.chat("&7> &des.enchant")
        };
    }

    @Override
    public List<String> getSubcommandArgs(CommandSender sender, String[] args) {
        return null;
    }
}
