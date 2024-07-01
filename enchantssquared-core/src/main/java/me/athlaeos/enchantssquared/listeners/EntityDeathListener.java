package me.athlaeos.enchantssquared.listeners;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.domain.EntityClassificationType;
import me.athlaeos.enchantssquared.domain.EntityEquipment;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.on_death.TriggerOnDeathEnchantment;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import me.athlaeos.enchantssquared.managers.EntityEquipmentCacheManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EntityDeathListener implements Listener {
    private static final Map<UUID, DeathResponsibility> responsibilityTracker = new HashMap<>();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent e){
        if (!EntityClassificationType.isMatchingClassification(e.getEntity().getType(), EntityClassificationType.UNALIVE)){
            LivingEntity died = e.getEntity();

            if (EnchantsSquared.isWorldGuardAllowed(died, died.getLocation(), "es-deny-all")){
                EntityEquipment equipment = EntityEquipmentCacheManager.getInstance().getAndCacheEquipment(died); // EntityUtils.getEntityEquipment(died);

                for (CustomEnchant enchantment : CustomEnchantManager.getInstance().getEnchantmentsMatchingFilter(c -> c instanceof TriggerOnDeathEnchantment)){
                    ((TriggerOnDeathEnchantment) enchantment).onEntityDeath(e, enchantment.getLevelService(false, died).getLevel(equipment));
                }

                Player killer = died.getKiller() == null ? getResponsible(e.getEntity().getUniqueId()) : died.getKiller();
                if (killer != null){
                    EntityEquipment killerEquipment = EntityEquipmentCacheManager.getInstance().getAndCacheEquipment(killer); // EntityUtils.getEntityEquipment(died.getKiller());

                    for (CustomEnchant enchantment : CustomEnchantManager.getInstance().getEnchantmentsMatchingFilter(c -> c instanceof TriggerOnDeathEnchantment)){
                        ((TriggerOnDeathEnchantment) enchantment).onEntityKilled(e, enchantment.getLevelService(false, killer).getLevel(killerEquipment));
                    }
                }
            }

            responsibilityTracker.remove(e.getEntity().getUniqueId());
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

    public static Player getResponsible(UUID victim){
        if (responsibilityTracker.containsKey(victim) && responsibilityTracker.get(victim).until > System.currentTimeMillis())
            return EnchantsSquared.getPlugin().getServer().getPlayer(responsibilityTracker.get(victim).responsible);
        return null;
    }

    public static void setResponsible(UUID victim, UUID aggressor, long responsibleDuration){
        responsibilityTracker.put(victim, new DeathResponsibility(aggressor, System.currentTimeMillis() + responsibleDuration));
    }

    private record DeathResponsibility(UUID responsible, long until){}
}
