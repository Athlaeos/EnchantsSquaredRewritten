package me.athlaeos.enchantssquared.listeners;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.domain.EntityEquipment;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.on_heal.TriggerOnHealthRegainedEnchantment;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import me.athlaeos.enchantssquared.managers.EntityEquipmentCacheManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public class HealthRegenerationListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityHeal(EntityRegainHealthEvent e){
        if (!e.isCancelled()){
            LivingEntity healer = (LivingEntity) e.getEntity();

            if (EnchantsSquared.isWorldGuardAllowed(healer, healer.getLocation(), "es-deny-all")){
                EntityEquipment equipment = EntityEquipmentCacheManager.getInstance().getAndCacheEquipment(healer);

                for (CustomEnchant enchantment : CustomEnchantManager.getInstance().getEnchantmentsMatchingFilter(c -> c instanceof TriggerOnHealthRegainedEnchantment)){
                    ((TriggerOnHealthRegainedEnchantment) enchantment).onHeal(e, enchantment.getLevelService(false, healer).getLevel(equipment));
                }
            }
        }
    }
}
