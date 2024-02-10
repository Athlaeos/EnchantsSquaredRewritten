package me.athlaeos.enchantssquared.listeners;

import me.athlaeos.enchantssquared.managers.EntityEquipmentCacheManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class LeaveJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        EntityEquipmentCacheManager.getInstance().getAndCacheEquipment(e.getPlayer());
    }

}
