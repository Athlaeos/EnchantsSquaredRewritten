package me.athlaeos.enchantssquared.enchantments.on_damaged;

import org.bukkit.event.entity.EntityDamageEvent;

public interface TriggerOnDamagedEnchantment {
    /**
     * Trigger on an EntityDamageEvent. Enchantments of this type trigger when an entity possessing the
     * enchanted item takes damage of any kind
     * @param e the event
     */
    void onDamaged(EntityDamageEvent e, int level);
}
