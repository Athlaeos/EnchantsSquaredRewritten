package me.athlaeos.enchantssquared.listeners;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.domain.EntityClassificationType;
import me.athlaeos.enchantssquared.domain.EntityEquipment;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.on_potion_effect.TriggerOnPotionEffectEnchantment;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import me.athlaeos.enchantssquared.utility.EntityUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;

public class EntityPotionEffectListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityPotionEffectEvent e){
        if (!e.isCancelled() &&
                e.getEntity() instanceof LivingEntity &&
                !EntityClassificationType.isMatchingClassification(e.getEntity().getType(), EntityClassificationType.UNALIVE)){
            LivingEntity affected = (LivingEntity) e.getEntity();

            if (EnchantsSquared.isWorldGuardAllowed(affected, affected.getLocation(), "es-deny-all")){
                EntityEquipment equipment = EntityUtils.getEntityEquipment(affected);

                for (CustomEnchant enchantment : CustomEnchantManager.getInstance().getEnchantmentsMatchingFilter(c -> c instanceof TriggerOnPotionEffectEnchantment)){
                    ((TriggerOnPotionEffectEnchantment) enchantment).onPotionEffect(e, enchantment.getLevelService(false, affected).getLevel(equipment));
                }
            }
        }
    }
}
