package me.athlaeos.enchantssquared.listeners;

import me.athlaeos.enchantssquared.AttributeEnchantment;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import me.athlaeos.enchantssquared.managers.EntityEquipmentCacheManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class LeaveJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        EntityEquipmentCacheManager.getInstance().getAndCacheEquipment(e.getPlayer());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        for (CustomEnchant enchant : CustomEnchantManager.getInstance().getAllEnchants().values()){
            if (enchant instanceof AttributeEnchantment attributeEnchantment){
                attributeEnchantment.cleanAttribute(e.getPlayer());
            }
        }
    }
}
