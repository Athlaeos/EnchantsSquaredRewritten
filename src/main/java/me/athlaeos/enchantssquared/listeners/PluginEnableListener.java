package me.athlaeos.enchantssquared.listeners;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;

public class PluginEnableListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginEnable(PluginEnableEvent e){
        EnchantsSquared.getPlugin().getServer().getScheduler().runTaskLater(EnchantsSquared.getPlugin(), () -> {
            if (e.getPlugin().getName().equalsIgnoreCase("ValhallaMMO")){
                CustomEnchantManager.getInstance().registerValhallaEnchantments();
            }
        }, 100L);
    }
}
