package me.athlaeos.enchantssquared.listeners;

import me.athlaeos.enchantssquared.managers.CooldownManager;
import me.athlaeos.enchantssquared.managers.EntityEquipmentCacheManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MovementListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e){
        if (e.isCancelled()) return;
        if (!CooldownManager.getInstance().isCooldownPassed(e.getPlayer().getUniqueId(), "delay_equipment_updates")) return;

        CooldownManager.getInstance().setCooldown(e.getPlayer().getUniqueId(), 10000, "delay_equipment_updates");
        EntityEquipmentCacheManager.getInstance().resetEquipment(e.getPlayer());
    }
}
