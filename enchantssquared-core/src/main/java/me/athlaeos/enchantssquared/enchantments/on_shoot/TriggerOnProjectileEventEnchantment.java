package me.athlaeos.enchantssquared.enchantments.on_shoot;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public interface TriggerOnProjectileEventEnchantment {
    /**
     * Trigger on an ProjectileLaunchEvent. Enchantments of this type trigger when an entity possessing the
     * enchanted item shoots any projectile
     * @param e the event
     */
    void onShoot(ProjectileLaunchEvent e, int level, LivingEntity shooter);

    /**
     * Trigger on an ProjectileHitEvent. Enchantments of this type trigger when a projectile launched by an entity possessing
     * the enchanted item hits something with a projectile.
     * @param e the event
     */
    void onHit(ProjectileHitEvent e, int level, LivingEntity shooter);

    /**
     * Trigger on an EntityShootBowEvent. Enchantments of this type trigger when an entity possessing
     * the enchanted item fires a bow.
     * @param e the event
     */
    void onBowShot(EntityShootBowEvent e, int level, LivingEntity shooter);
}
