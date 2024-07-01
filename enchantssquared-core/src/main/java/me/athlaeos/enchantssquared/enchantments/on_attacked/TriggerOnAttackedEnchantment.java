package me.athlaeos.enchantssquared.enchantments.on_attacked;

import me.athlaeos.enchantssquared.domain.EntityClassificationType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public interface TriggerOnAttackedEnchantment {
    /**
     * Trigger on an EntityDamageByEntityEvent. Enchantments of this type trigger when an entity possessing the
     * enchanted item is attacked by another entity.
     * The victim is asserted to be a LivingEntity that's not of {@link EntityClassificationType}.UNALIVE
     * @param e the event
     * @param realAttacker the real attacker in the event, usually representing the shooter of a projectile if they are an entity
     */
    void onAttacked(EntityDamageByEntityEvent e, int level, LivingEntity realAttacker);
}
