package me.athlaeos.enchantssquared.listeners.shadowcraft_listeners;

import me.athlaeos.enchantssquared.config.ConfigManager;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerJoinListener implements Listener {
    /* Updates PDC to match lore on player join */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        YamlConfiguration sc2Config = ConfigManager.getInstance().getConfig("config_shadowcraft.yml").get();
        if (sc2Config.getBoolean("enableLegacyEnchantmentUpdateOnRelog")) {
            Player player = e.getPlayer();
            Bukkit.getLogger().info("Updating relics in Player " + player.getName() + "'s Inventory!");
            CustomEnchantManager manager = CustomEnchantManager.getInstance();
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null) {
                    manager.updateItem(item);
                }
            }
        }
    }
}
