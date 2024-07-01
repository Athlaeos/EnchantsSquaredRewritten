package me.athlaeos.enchantssquared.enchantments.on_heal;

import org.bukkit.event.entity.EntityRegainHealthEvent;

public interface TriggerOnHealthRegainedEnchantment {
    /**
     * Trigger on an EntityRegainHealthEvent. Enchantments of this type trigger when a player possessing the
     * enchanted item regains health for any reason, whether it be natural regeneration or potion effects
     * @param e the event
     */
    void onHeal(EntityRegainHealthEvent e, int level);
}
