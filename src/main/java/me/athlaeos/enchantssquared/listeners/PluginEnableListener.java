package me.athlaeos.enchantssquared.listeners;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.hooks.valhallammo.CustomEnchantmentAddModifier;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import me.athlaeos.valhallammo.crafting.DynamicItemModifierManager;
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
                for (CustomEnchant enchant : CustomEnchantManager.getInstance().getAllEnchants().values()){
                    try {
                        DynamicItemModifierManager.modifiersToRegister.add(new CustomEnchantmentAddModifier(enchant));
                    } catch (Error | Exception ignored){
                        // If this happens, ValhallaMMO is not up to date yet as the modifiersToRegister feature was
                        // implemented before Valhalla was properly updated on spigot with it
                    }
                }
            }
        }, 100L);
    }
}
