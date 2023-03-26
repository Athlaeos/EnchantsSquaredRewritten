package me.athlaeos.enchantssquared.listeners;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.domain.EntityEquipment;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.on_interact.TriggerOnInteractEnchantment;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import me.athlaeos.enchantssquared.utility.EntityUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class InteractListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityHeal(PlayerInteractEvent e){
        Player who = e.getPlayer();
        Location interactedLocation = e.getClickedBlock() != null ? e.getClickedBlock().getLocation() : e.getPlayer().getLocation();

        if (EnchantsSquared.isWorldGuardAllowed(who, interactedLocation, "es-deny-all")){
            EntityEquipment equipment = EntityUtils.getEntityEquipment(who);

            for (CustomEnchant enchantment : CustomEnchantManager.getInstance().getEnchantmentsMatchingFilter(c -> c instanceof TriggerOnInteractEnchantment)){
                ((TriggerOnInteractEnchantment) enchantment).onInteract(e, enchantment.getLevelService(false, who).getLevel(equipment));
            }
        }
    }
}
