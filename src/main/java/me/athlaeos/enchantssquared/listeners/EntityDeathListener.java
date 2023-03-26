package me.athlaeos.enchantssquared.listeners;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.domain.EntityClassificationType;
import me.athlaeos.enchantssquared.domain.EntityEquipment;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.on_death.TriggerOnDeathEnchantment;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import me.athlaeos.enchantssquared.managers.EntityEquipmentCacheManager;
import me.athlaeos.enchantssquared.utility.EntityUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class EntityDeathListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent e){
        if (!EntityClassificationType.isMatchingClassification(e.getEntity().getType(), EntityClassificationType.UNALIVE)){
            LivingEntity died = e.getEntity();

            if (EnchantsSquared.isWorldGuardAllowed(died, died.getLocation(), "es-deny-all")){
                EntityEquipment equipment = EntityEquipmentCacheManager.getInstance().getAndCacheEquipment(died); // EntityUtils.getEntityEquipment(died);

                for (CustomEnchant enchantment : CustomEnchantManager.getInstance().getEnchantmentsMatchingFilter(c -> c instanceof TriggerOnDeathEnchantment)){
                    ((TriggerOnDeathEnchantment) enchantment).onEntityDeath(e, enchantment.getLevelService(false, died).getLevel(equipment));
                }

                if (died.getKiller() != null){
                    EntityEquipment killerEquipment = EntityEquipmentCacheManager.getInstance().getAndCacheEquipment(died.getKiller()); // EntityUtils.getEntityEquipment(died.getKiller());

                    for (CustomEnchant enchantment : CustomEnchantManager.getInstance().getEnchantmentsMatchingFilter(c -> c instanceof TriggerOnDeathEnchantment)){
                        ((TriggerOnDeathEnchantment) enchantment).onEntityKilled(e, enchantment.getLevelService(false, died.getKiller()).getLevel(killerEquipment));
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent e){
        Player died = e.getEntity();

        if (EnchantsSquared.isWorldGuardAllowed(died, died.getLocation(), "es-deny-all")){
            EntityEquipment equipment = EntityEquipmentCacheManager.getInstance().getAndCacheEquipment(died); // EntityUtils.getEntityEquipment(died);

            for (CustomEnchant enchantment : CustomEnchantManager.getInstance().getEnchantmentsMatchingFilter(c -> c instanceof TriggerOnDeathEnchantment)){
                ((TriggerOnDeathEnchantment) enchantment).onPlayerDeath(e, enchantment.getLevelService(false, died).getLevel(equipment));
            }

            if (died.getKiller() != null){
                EntityEquipment killerEquipment = EntityEquipmentCacheManager.getInstance().getAndCacheEquipment(died.getKiller()); // EntityUtils.getEntityEquipment(died.getKiller());

                for (CustomEnchant enchantment : CustomEnchantManager.getInstance().getEnchantmentsMatchingFilter(c -> c instanceof TriggerOnDeathEnchantment)){
                    ((TriggerOnDeathEnchantment) enchantment).onPlayerKilled(e, enchantment.getLevelService(false, died.getKiller()).getLevel(killerEquipment));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerRespawnEvent e){
        Player respawned = e.getPlayer();

        if (EnchantsSquared.isWorldGuardAllowed(respawned, respawned.getLocation(), "es-deny-all")){
            EntityEquipment equipment = EntityEquipmentCacheManager.getInstance().getAndCacheEquipment(respawned); // EntityUtils.getEntityEquipment(respawned);

            for (CustomEnchant enchantment : CustomEnchantManager.getInstance().getEnchantmentsMatchingFilter(c -> c instanceof TriggerOnDeathEnchantment)){
                ((TriggerOnDeathEnchantment) enchantment).onPlayerRespawn(e, enchantment.getLevelService(false, respawned).getLevel(equipment));
            }
        }
    }
}
