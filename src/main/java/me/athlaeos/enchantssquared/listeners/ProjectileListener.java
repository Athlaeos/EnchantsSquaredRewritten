package me.athlaeos.enchantssquared.listeners;

import me.athlaeos.enchantssquared.EnchantsSquared;
import me.athlaeos.enchantssquared.domain.EntityClassificationType;
import me.athlaeos.enchantssquared.domain.EntityEquipment;
import me.athlaeos.enchantssquared.domain.MaterialClassType;
import me.athlaeos.enchantssquared.enchantments.CustomEnchant;
import me.athlaeos.enchantssquared.enchantments.on_attack.TriggerOnAttackEnchantment;
import me.athlaeos.enchantssquared.enchantments.on_attacked.TriggerOnAttackedEnchantment;
import me.athlaeos.enchantssquared.enchantments.on_shoot.TriggerOnProjectileEventEnchantment;
import me.athlaeos.enchantssquared.managers.CustomEnchantManager;
import me.athlaeos.enchantssquared.managers.EntityEquipmentCacheManager;
import me.athlaeos.enchantssquared.utility.EntityUtils;
import me.athlaeos.enchantssquared.utility.ItemUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;

public class ProjectileListener implements Listener {

    @EventHandler
    public void onHit(ProjectileHitEvent e){
        // Listener may only proceed if event is not cancelled, if the attacked entity is a living entity, and if this entity isn't
        // an unalive entity such as ARMOR_STAND
        if (!e.isCancelled() &&
                e.getEntity() instanceof LivingEntity &&
                !EntityClassificationType.isMatchingClassification(e.getEntity().getType(), EntityClassificationType.UNALIVE)){
            Projectile projectile = e.getEntity();
            LivingEntity shooter = null;
            if (projectile.getShooter() != null){
                if (projectile.getShooter() instanceof LivingEntity) {
                    shooter = (LivingEntity) projectile.getShooter();
                }
            }
            if (shooter == null) return;

            if (EnchantsSquared.isWorldGuardAllowed(shooter, shooter.getLocation(), "es-deny-all")){
                // fetch equipment enchantments only if attacker isn't in a region blocking all enchantments
                EntityEquipment equipment = EntityEquipmentCacheManager.getInstance().getAndCacheEquipment(shooter); // EntityUtils.getEntityEquipment(victim);
                boolean offHand = ItemUtils.isAirOrNull(equipment.getMainHand());
                for (CustomEnchant enchantment : CustomEnchantManager.getInstance().getEnchantmentsMatchingFilter(c -> c instanceof TriggerOnProjectileEventEnchantment)){
                    ((TriggerOnProjectileEventEnchantment) enchantment).onHit(e, enchantment.getLevelService(offHand, shooter).getLevel(equipment), shooter);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onShoot(ProjectileLaunchEvent e){
        // Listener may only proceed if event is not cancelled, if the attacked entity is a living entity, and if this entity isn't
        // an unalive entity such as ARMOR_STAND
        if (!e.isCancelled() &&
                e.getEntity() instanceof LivingEntity &&
                !EntityClassificationType.isMatchingClassification(e.getEntity().getType(), EntityClassificationType.UNALIVE)){
            Projectile projectile = e.getEntity();
            LivingEntity shooter = null;
            if (projectile.getShooter() != null){
                if (projectile.getShooter() instanceof LivingEntity) {
                    shooter = (LivingEntity) projectile.getShooter();
                }
            }
            if (shooter == null) return;

            if (EnchantsSquared.isWorldGuardAllowed(shooter, shooter.getLocation(), "es-deny-all")){
                // fetch equipment enchantments only if attacker isn't in a region blocking all enchantments
                EntityEquipment equipment = EntityEquipmentCacheManager.getInstance().getAndCacheEquipment(shooter); // EntityUtils.getEntityEquipment(victim);
                boolean offHand = ItemUtils.isAirOrNull(equipment.getMainHand());
                for (CustomEnchant enchantment : CustomEnchantManager.getInstance().getEnchantmentsMatchingFilter(c -> c instanceof TriggerOnProjectileEventEnchantment)){
                    ((TriggerOnProjectileEventEnchantment) enchantment).onShoot(e, enchantment.getLevelService(offHand, shooter).getLevel(equipment), shooter);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onShoot(EntityShootBowEvent e){
        // Listener may only proceed if event is not cancelled, if the attacked entity is a living entity, and if this entity isn't
        // an unalive entity such as ARMOR_STAND
        if (!e.isCancelled() &&
                !EntityClassificationType.isMatchingClassification(e.getEntity().getType(), EntityClassificationType.UNALIVE)){
            LivingEntity shooter = e.getEntity();

            if (EnchantsSquared.isWorldGuardAllowed(shooter, shooter.getLocation(), "es-deny-all")){
                // fetch equipment enchantments only if attacker isn't in a region blocking all enchantments
                EntityEquipment equipment = EntityEquipmentCacheManager.getInstance().getAndCacheEquipment(shooter); // EntityUtils.getEntityEquipment(victim);
                boolean offHand = ItemUtils.isAirOrNull(equipment.getMainHand());
                for (CustomEnchant enchantment : CustomEnchantManager.getInstance().getEnchantmentsMatchingFilter(c -> c instanceof TriggerOnProjectileEventEnchantment)){
                    ((TriggerOnProjectileEventEnchantment) enchantment).onBowShot(e, enchantment.getLevelService(offHand, shooter).getLevel(equipment), shooter);
                }
            }
        }
    }
}
