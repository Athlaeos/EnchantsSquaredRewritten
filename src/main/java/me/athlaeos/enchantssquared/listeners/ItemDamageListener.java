package me.athlaeos.enchantssquared.listeners;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.domain.EntityClassificationType;
import me.athlaeos.enchantssquared.domain.EntityEquipment;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.on_attack.TriggerOnAttackEnchantment;
import me.athlaeos.enchantssquared.enchantments.on_attacked.TriggerOnAttackedEnchantment;
import me.athlaeos.enchantssquared.enchantments.on_item_damage.TriggerOnItemDamageEnchantment;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import me.athlaeos.enchantssquared.managers.EntityEquipmentCacheManager;
import me.athlaeos.enchantssquared.utility.EntityUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;

public class ItemDamageListener implements Listener {

    @EventHandler
    public void onEntityTakeDamage(PlayerItemDamageEvent e){
        if (!e.isCancelled()){
            Player player = e.getPlayer();

            if (EnchantsSquared.isWorldGuardAllowed(player, player.getLocation(), "es-deny-all")){
                EntityEquipment equipment = EntityEquipmentCacheManager.getInstance().getAndCacheEquipment(player); // EntityUtils.getEntityEquipment(player);
                for (CustomEnchant enchantment : CustomEnchantManager.getInstance().getEnchantmentsMatchingFilter(c -> c instanceof TriggerOnItemDamageEnchantment)){
                    ((TriggerOnItemDamageEnchantment) enchantment).onItemDamage(e, enchantment.getLevelService(false, player).getLevel(equipment));
                }
            }
        }
    }
}
