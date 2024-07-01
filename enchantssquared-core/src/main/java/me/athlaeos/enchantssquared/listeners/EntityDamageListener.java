package me.athlaeos.enchantssquared.listeners;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.domain.EntityClassificationType;
import me.athlaeos.enchantssquared.domain.EntityEquipment;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.on_attack.TriggerOnAttackEnchantment;
import me.athlaeos.enchantssquared.enchantments.on_attacked.TriggerOnAttackedEnchantment;
import me.athlaeos.enchantssquared.enchantments.on_damaged.TriggerOnDamagedEnchantment;
import me.athlaeos.enchantssquared.hooks.WorldGuardHook;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import me.athlaeos.enchantssquared.managers.EntityEquipmentCacheManager;
import me.athlaeos.enchantssquared.utility.EntityUtils;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamageListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityTakeDamage(EntityDamageEvent e){
        if (!e.isCancelled() && e.getDamage() > 0 &&
                e.getEntity() instanceof LivingEntity entity &&
                !EntityClassificationType.isMatchingClassification(e.getEntity().getType(), EntityClassificationType.UNALIVE)){
            if (EnchantsSquared.isWorldGuardAllowed(entity, entity.getLocation(), "es-deny-all")){
                // fetch equipment enchantments only if attacker isn't in a region blocking all enchantments
                EntityEquipment equipment = EntityEquipmentCacheManager.getInstance().getAndCacheEquipment(entity); // EntityUtils.getEntityEquipment(victim);
                boolean offHand = ItemUtils.isAirOrNull(equipment.getMainHand());
                for (CustomEnchant enchantment : CustomEnchantManager.getInstance().getEnchantmentsMatchingFilter(c -> c instanceof TriggerOnDamagedEnchantment)){
                    ((TriggerOnDamagedEnchantment) enchantment).onDamaged(e, enchantment.getLevelService(offHand, entity).getLevel(equipment));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamagedByEntity(EntityDamageByEntityEvent e){
        if (e.getDamager() instanceof Player && e.getEntity() instanceof Player &&
                EnchantsSquared.isWorldGuardHooked() && WorldGuardHook.getHook().isPVPDenied((Player) e.getDamager())) return;
        // Listener may only proceed if event is not cancelled, if the attacked entity is a living entity, and if this entity isn't
        // an unalive entity such as ARMOR_STAND
        if (!e.isCancelled() && e.getDamage() > 0 &&
                e.getEntity() instanceof LivingEntity &&
                !EntityClassificationType.isMatchingClassification(e.getEntity().getType(), EntityClassificationType.UNALIVE)){
            LivingEntity victim = (LivingEntity) e.getEntity();
            LivingEntity attacker = EntityUtils.getRealAttacker(e.getDamager());

            if (EnchantsSquared.isWorldGuardAllowed(victim, victim.getLocation(), "es-deny-all")){
                // fetch equipment enchantments only if attacker isn't in a region blocking all enchantments
                EntityEquipment victimEquipment = EntityEquipmentCacheManager.getInstance().getAndCacheEquipment(victim); // EntityUtils.getEntityEquipment(victim);
                boolean offHand = ItemUtils.isAirOrNull(victimEquipment.getMainHand());
                for (CustomEnchant enchantment : CustomEnchantManager.getInstance().getEnchantmentsMatchingFilter(c -> c instanceof TriggerOnAttackedEnchantment)){
                    ((TriggerOnAttackedEnchantment) enchantment).onAttacked(e, enchantment.getLevelService(offHand, victim).getLevel(victimEquipment), attacker);
                }
            }
            if (e.isCancelled()) return; // if the defensive enchantment for whatever reason cancelled the attack, the attack enchantments will not proceed.

            if (attacker != null && EnchantsSquared.isWorldGuardAllowed(attacker, attacker.getLocation(), "es-deny-all")){
                EntityEquipment attackerEquipment = EntityEquipmentCacheManager.getInstance().getAndCacheEquipment(attacker); // EntityUtils.getEntityEquipment(attacker);

                boolean mainHand = !ItemUtils.isAirOrNull(attackerEquipment.getMainHand());
                if (e.getDamager() instanceof Projectile && attacker.getEquipment() != null){
                    if (e.getDamager() instanceof Trident){
                        // if the damager was a trident, simply consider the main hand item of the attacker the trident they threw
                        // this does not set the trident to the attacker's main hand
                        attackerEquipment.setMainHand(((Trident) e.getDamager()).getItem());
                        attackerEquipment.setMainHandEnchantments(CustomEnchantManager.getInstance().getItemsEnchantsFromPDC(((Trident) e.getDamager()).getItem()));
                    } else {
                        MaterialClassType mainType = MaterialClassType.getClass(attacker.getEquipment().getItemInMainHand().getType());
                        if (!(mainType == MaterialClassType.BOWS || mainType == MaterialClassType.CROSSBOWS)){
                            MaterialClassType offType = MaterialClassType.getClass(attacker.getEquipment().getItemInOffHand().getType());
                            if (offType == MaterialClassType.BOWS || offType == MaterialClassType.CROSSBOWS) mainHand = false;
                        }
                    }
                }
                for (CustomEnchant enchantment : CustomEnchantManager.getInstance().getEnchantmentsMatchingFilter(c -> c instanceof TriggerOnAttackEnchantment)){
                    ((TriggerOnAttackEnchantment) enchantment).onAttack(e, enchantment.getLevelService(!mainHand, attacker).getLevel(attackerEquipment), attacker);
                }
            }
        }
    }
}
